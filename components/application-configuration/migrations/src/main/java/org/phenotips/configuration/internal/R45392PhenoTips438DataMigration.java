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

package org.phenotips.configuration.internal;

import org.phenotips.Constants;

import org.xwiki.component.annotation.Component;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.DocumentReferenceResolver;
import org.xwiki.model.reference.EntityReferenceSerializer;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;

import com.xpn.xwiki.XWiki;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.XWikiException;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;
import com.xpn.xwiki.store.XWikiHibernateBaseStore.HibernateCallback;
import com.xpn.xwiki.store.XWikiHibernateStore;
import com.xpn.xwiki.store.migration.DataMigrationException;
import com.xpn.xwiki.store.migration.XWikiDBVersion;
import com.xpn.xwiki.store.migration.hibernate.AbstractHibernateDataMigration;

/**
 * Migration for PhenoTips issue #438: Automatically migrate existing old
 * {@code ClinicalInformationCode.DBConfigurationClass} to the new {@code PhenoTips.DBConfigurationClass}.
 * 
 * @version $Id$
 * @since 1.0M6
 */
@Component
@Named("R45392Phenotips#438")
@Singleton
public class R45392PhenoTips438DataMigration extends AbstractHibernateDataMigration
{
    /** Resolves unprefixed document names to the current wiki. */
    @Inject
    @Named("current")
    private DocumentReferenceResolver<String> resolver;

    /** Serializes the class name without the wiki prefix, to be used in the database query. */
    @Inject
    @Named("compactwiki")
    private EntityReferenceSerializer<String> serializer;

    @Override
    public String getDescription()
    {
        return "Migrate existing old configuration-related objects from the ClinicalInformationCode space to the new"
            + " PhenoTips space";
    }

    @Override
    public XWikiDBVersion getVersion()
    {
        return new XWikiDBVersion(45392);
    }

    @Override
    public void hibernateMigrate() throws DataMigrationException, XWikiException
    {
        getStore().executeWrite(getXWikiContext(), new MigrateObjectsCallback("DBConfigurationClass"));
        getStore().executeWrite(getXWikiContext(), new MigrateObjectsCallback("MenuConfigurationClass"));
    }

    /**
     * Searches for all documents containing objects from a class in the {@code ClinicalInformationCode} space, and for
     * each such document and for each such object, creates a new object with the class changed to the {@code PhenoTips}
     * space, copies all the values from the deprecated object to the new one, and then deletes the old objects.
     */
    private class MigrateObjectsCallback implements HibernateCallback<Object>
    {
        /** The name of the class to migrate. */
        private final String className;

        /**
         * Simple constructor.
         * 
         * @param className the name of the class to migrate, without a space prefix, for example {@code PatientClass}
         */
        public MigrateObjectsCallback(String className)
        {
            this.className = className;
        }

        @Override
        public Object doInHibernate(Session session) throws HibernateException, XWikiException
        {
            XWikiContext context = getXWikiContext();
            XWiki xwiki = context.getWiki();
            DocumentReference oldClassReference =
                new DocumentReference(context.getDatabase(), "ClinicalInformationCode", this.className);
            DocumentReference newClassReference =
                new DocumentReference(context.getDatabase(), Constants.CODE_SPACE, this.className);
            String localOldClassName = R45392PhenoTips438DataMigration.this.serializer.serialize(oldClassReference);
            Query q =
                session.createQuery("select distinct o.name from BaseObject o where o.className = '"
                    + localOldClassName + "'");
            @SuppressWarnings("unchecked")
            List<String> documents = q.list();
            for (String docName : documents) {
                XWikiDocument doc =
                    xwiki.getDocument(R45392PhenoTips438DataMigration.this.resolver.resolve(docName), context);
                for (BaseObject oldObject : doc.getXObjects(oldClassReference)) {
                    BaseObject newObject = oldObject.duplicate();
                    newObject.setXClassReference(newClassReference);
                    doc.addXObject(newObject);
                }
                // Also update the ConfigurableClass settings to use the new objects
                String configurableClassProperty = "configurationClass";
                BaseObject configurable =
                    doc.getXObject(new DocumentReference(context.getDatabase(), "XWiki", "ConfigurableClass"),
                        configurableClassProperty, localOldClassName, false);
                if (configurable != null) {
                    configurable.set(configurableClassProperty,
                        R45392PhenoTips438DataMigration.this.serializer.serialize(newClassReference), context);
                }
                doc.removeXObjects(oldClassReference);
                doc.setContent("");
                doc.setComment("Migrated configuration data in class " + this.className);
                doc.setMinorEdit(true);
                // Preserve the old author so that we don't lose admin rights on the page
                doc.setMetaDataDirty(false);
                doc.setContentDirty(false);
                try {
                    // There's a bug in XWiki which prevents saving an object in the same session that it was loaded,
                    // so we must clear the session cache first.
                    session.clear();
                    ((XWikiHibernateStore) getStore()).saveXWikiDoc(doc, context, false);
                    session.flush();
                } catch (DataMigrationException e) {
                    // We're in the middle of a migration, we're not expecting another migration
                }
            }
            return null;
        }
    }
}
