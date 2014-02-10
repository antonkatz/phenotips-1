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
package org.phenotips.groups;

import org.phenotips.Constants;

import org.xwiki.model.EntityType;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.EntityReference;
import org.xwiki.stability.Unstable;
import org.xwiki.users.User;

import com.xpn.xwiki.XWikiContext;

/**
 * A group of users.
 *
 * @version $Id$
 * @since 1.0M9
 */
@Unstable
public interface Group
{
    /** The XClass used for storing work groups. */
    EntityReference CLASS_REFERENCE = new EntityReference("PhenoTipsGroupClass", EntityType.DOCUMENT,
        Constants.CODE_SPACE_REFERENCE);
    /** The XClass used for storing individual applicant data. */
    EntityReference APPLICANT_REFERENCE = new EntityReference("PhenoTipsGroupApplicant",
        EntityType.DOCUMENT, Constants.CODE_SPACE_REFERENCE);

    /**
     * Get a reference to the XDocument where this group is defined.
     *
     * @return a valid document reference
     */
    DocumentReference getReference();

    /**
     * Does it need to check for belonging to the group already? There will be no apply button show if the user belongs
     * to the group already: not secure, but security is not needed.
     *
     * @param user holds user data
     * @param context used to create new xobjects
     * @throws java.lang.Exception if the user has already applied for membership
     */
    void addMembershipApplicant(User user, XWikiContext context) throws Exception;
}
