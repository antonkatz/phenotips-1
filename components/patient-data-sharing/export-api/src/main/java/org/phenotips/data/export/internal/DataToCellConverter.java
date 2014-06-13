/*
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
/*
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.phenotips.data.export.internal;

import org.phenotips.data.Feature;
import org.phenotips.data.FeatureMetadatum;
import org.phenotips.data.Patient;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

public class DataToCellConverter
{
    private Map<String, Set<String>> enabledHeaderIdsBySection = new HashMap<String, Set<String>>();

    private ConversionHelpers helpers;

    public DataToCellConverter()
    {
        helpers = new ConversionHelpers();
    }

    public void featureSetUp(Set<String> enabledFields) throws Exception
    {
        String sectionName = "phenotype";
        String[] fieldIds =
            { "phenotype", "phenotype_code", "phenotype_combined", "phenotype_code_meta", "phenotype_meta",
                "negative_phenotype", "phenotype_by_section" };
        /* FIXME These will not work properly in different configurations */
        String[][] headerIds =
            { { "phenotype" }, { "code" }, { "phenotype", "code" }, { "meta_code" }, { "meta" }, { "negative" },
                { "category" } };
        Set<String> present = new HashSet<String>();

        int counter = 0;
        for (String fieldId : fieldIds) {
            if (enabledFields.remove(fieldId)) {
                for (String headerId : headerIds[counter]) {
                    present.add(headerId);
                }
            }
            counter++;
        }
        enabledHeaderIdsBySection.put(sectionName, present);

        helpers.featureSetUp(present.contains("phenotype"), present.contains("negative"), present.contains("category"));
    }

    public DataSection featuresHeader() throws Exception
    {
        String sectionName = "phenotype";
        Set<String> present = enabledHeaderIdsBySection.get(sectionName);
        if (present.isEmpty()) {
            return null;
        }

        DataSection section = new DataSection(sectionName);
        List<String> orderedHeaderIds = new LinkedList<String>();
        orderedHeaderIds.add("category");
        orderedHeaderIds.add("phenotype");
        orderedHeaderIds.add("code");
        orderedHeaderIds.add("meta");
        orderedHeaderIds.add("meta_code");
        List<String> orderedHeaderNames = new LinkedList<String>();
        orderedHeaderNames.add("Category");
        orderedHeaderNames.add("Label");
        orderedHeaderNames.add("ID");
        orderedHeaderNames.add("Meta");
        orderedHeaderNames.add("ID");

        int counter = 0;
        int hX = 0;
        if (present.contains("phenotype") && present.contains("negative")) {
            DataCell cell = new DataCell("Present", hX, 1, StyleOption.HEADER);
            section.addCell(cell);
            hX++;
        }
        for (String headerId : orderedHeaderIds) {
            if (!present.contains(headerId)) {
                counter++;
                continue;
            }
            DataCell cell = new DataCell(orderedHeaderNames.get(counter), hX, 1, StyleOption.HEADER);
            section.addCell(cell);
            hX++;
            counter++;
        }
        DataCell sectionHeader = new DataCell("Phenotype", 0, 0, StyleOption.HEADER);
        sectionHeader.addStyle(StyleOption.LARGE_HEADER);
        section.addCell(sectionHeader);

        return section;
    }

    public DataSection featuresBody(Patient patient) throws Exception
    {
        String sectionName = "phenotype";
        Set<String> present = enabledHeaderIdsBySection.get(sectionName);
        if (present.isEmpty()) {
            return null;
        }

        Boolean bothTypes = present.contains("phenotype") && present.contains("negative");
        DataSection section = new DataSection(sectionName);

        int x;
        int y = 0;
        Set<? extends Feature> features = patient.getFeatures();
        helpers.newPatient();
        Boolean categoriesEnabled = present.contains("category");
        List<Feature> sortedFeatures;
        Map<String, String> sectionFeatureLookup = new HashMap<String, String>();
        if (!categoriesEnabled) {
            sortedFeatures = helpers.sortFeaturesSimple(features);
        } else {
            sortedFeatures = helpers.sortFeaturesWithSections(features);
            sectionFeatureLookup = helpers.getSectionFeatureTree();
        }

        Boolean lastStatus = false;
        String lastSection = "";
        for (Feature feature : sortedFeatures) {
            x = 0;

            if (bothTypes && lastStatus != feature.isPresent()) {
                lastStatus = feature.isPresent();
                lastSection = "";
                DataCell cell = new DataCell(lastStatus ? "Yes" : "No", x, y);
                if (!lastStatus) {
                    cell.addStyle(StyleOption.YES_NO_SEPARATOR);
                }
                cell.addStyle(lastStatus ? StyleOption.YES : StyleOption.NO);
                section.addCell(cell);
            }
            if (bothTypes) {
                x++;
            }
            if (categoriesEnabled) {
                String currentSection = sectionFeatureLookup.get(feature.getId());
                if (!StringUtils.equals(currentSection, lastSection)) {
                    DataCell cell = new DataCell(currentSection, x, y);
                    section.addCell(cell);
                    lastSection = currentSection;
                }
                x++;
            }
            if (present.contains("phenotype")) {
                DataCell cell = new DataCell(feature.getName(), x, y, StyleOption.FEATURE_SEPARATOR);
                section.addCell(cell);
                x++;
            }
            if (present.contains("code")) {
                DataCell cell = new DataCell(feature.getId(), x, y, StyleOption.FEATURE_SEPARATOR);
                section.addCell(cell);
                x++;
            }
            if (present.contains("meta") || present.contains("meta_code")) {
                int mX = x;
                int mCX = x + 1;
                Collection<? extends FeatureMetadatum> featureMetadatum = feature.getMetadata().values();
                Boolean metaPresent = featureMetadatum.size() > 0;
                for (FeatureMetadatum meta : featureMetadatum) {
                    if (present.contains("meta")) {
                        DataCell cell = new DataCell(meta.getName(), mX, y);
                        section.addCell(cell);
                    }
                    if (present.contains("meta_code")) {
                        DataCell cell = new DataCell(meta.getId(), mCX, y);
                        section.addCell(cell);
                    }
                    y++;
                }
                if (metaPresent) {
                    y--;
                }
            }
            y++;
        }

//        section.finalizeToMatrix();
        return section;
    }

    protected DataSection idHeader(Set<String> enabledFields) throws Exception
    {
        String sectionName = "id";
        Set<String> present = new HashSet<String>();
        if (enabledFields.remove("doc.name")) {
            present.add("external_id");
        } else {
            return null;
        }
        DataSection section = new DataSection(sectionName);
        enabledHeaderIdsBySection.put(sectionName, present);
        DataCell cell = new DataCell("Identifier", 0, 0, StyleOption.HEADER);
        cell.addStyle(StyleOption.LARGE_HEADER);
        section.addCell(cell);

//        section.finalizeToMatrix();
        return section;
    }

    protected DataSection idBody(Patient patient) throws Exception
    {
        String sectionName = "id";
        Set<String> present = enabledHeaderIdsBySection.get(sectionName);
        if (present == null || present.isEmpty()) {
            return null;
        }
        DataSection section = new DataSection(sectionName);

        DataCell cell = new DataCell(patient.getExternalId(), 0, 0);
        section.addCell(cell);

//        section.finalizeToMatrix();
        return section;
    }
}
