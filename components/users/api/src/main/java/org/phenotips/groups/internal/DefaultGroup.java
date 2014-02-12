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
package org.phenotips.groups.internal;

import org.phenotips.groups.Group;

import org.xwiki.model.reference.DocumentReference;
import org.xwiki.users.User;

import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;

/**
 * Default implementation for {@link Group}.
 *
 * @version $Id$
 * @since 1.0M9
 */
public class DefaultGroup implements Group
{
    private static final String USER_ID = "userId";

    /** @see #getReference() */
    private final DocumentReference reference;

    /**
     * Simple constructor.
     *
     * @param reference the reference to the document where this group is defined
     */
    public DefaultGroup(DocumentReference reference)
    {
        this.reference = reference;
    }

    @Override
    public DocumentReference getReference()
    {
        return this.reference;
    }

    @Override
    public String toString()
    {
        return "Group " + getReference().getName();
    }

    @Override
    public void addMembershipApplicant(User user, XWikiContext context) throws Exception
    {
        XWikiDocument groupDoc = context.getWiki().getDocument(this.reference, context);
        String userId = user.getId();
        List<BaseObject> applicants = groupDoc.getXObjects(Group.APPLICANT_REFERENCE);
        if (applicants != null) {
            for (BaseObject applicant : applicants) {
                if (StringUtils.equalsIgnoreCase(userId, applicant.getStringValue(USER_ID))) {
                    //TODO Change to a narrower exception
                    throw new Exception("The user is already in the applicants list");
                }
            }
        }
        BaseObject applicant = groupDoc.newXObject(Group.APPLICANT_REFERENCE, context);
        applicant.set(USER_ID, userId, context);
        context.getWiki().saveDocument(groupDoc, context);
    }
}
