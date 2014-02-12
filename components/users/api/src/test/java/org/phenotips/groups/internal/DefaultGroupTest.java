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

import org.xwiki.component.manager.ComponentLookupException;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.EntityReference;
import org.xwiki.query.QueryException;
import org.xwiki.users.User;

import java.util.Iterator;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import com.xpn.xwiki.XWiki;
import com.xpn.xwiki.XWikiContext;
import com.xpn.xwiki.doc.XWikiDocument;
import com.xpn.xwiki.objects.BaseObject;

/**
 * Tests for the default {@link Group} implementation, {@link DefaultGroup}.
 *
 * @version $Id$
 */
public class DefaultGroupTest
{
    /** Basic tests for {@link DefaultGroup#getReference()}. */
    @Test
    public void getReference() throws ComponentLookupException, QueryException
    {
        Group g = new DefaultGroup(null);
        Assert.assertNull(g.getReference());

        DocumentReference a = new DocumentReference("xwiki", "Groups", "Group A");
        Assert.assertEquals(a, new DefaultGroup(a).getReference());
    }

    /** There's a nicer toString implementation showing the group name. */
    @Test
    public void toStringTest()
    {
        DocumentReference a = new DocumentReference("xwiki", "Groups", "Group A");
        Assert.assertTrue(new DefaultGroup(a).toString().contains("Group A"));
    }

    @Test
    public void addMemebershipApplicant() throws Exception
    {
        DocumentReference a = new DocumentReference("xwiki", "Groups", "Group A");
        DefaultGroup g = new DefaultGroup(a);

        User user = Mockito.mock(User.class);
        XWikiContext context = Mockito.mock(XWikiContext.class);
        XWiki wiki = Mockito.mock(XWiki.class);
        XWikiDocument doc = Mockito.mock(XWikiDocument.class);
        BaseObject baseObject = Mockito.mock(BaseObject.class);

        Mockito.when(context.getWiki()).thenReturn(wiki);
        Mockito.when(wiki.getDocument(Mockito.any(DocumentReference.class),
            Mockito.any(XWikiContext.class))).thenReturn(doc);
        Mockito.when(doc.getXObjects(Mockito.any(EntityReference.class))).thenReturn(null);
        Mockito.when(doc.newXObject(Mockito.any(EntityReference.class), Mockito.any(XWikiContext.class)))
            .thenReturn(baseObject);
        Mockito.doNothing().when(wiki).saveDocument(doc, context);

        g.addMembershipApplicant(user, context);
    }

    @Test(expected = java.lang.Exception.class)
    public void addMemebershipApplicantNull() throws Exception
    {
        DocumentReference a = new DocumentReference("xwiki", "Groups", "Group A");
        DefaultGroup g = new DefaultGroup(a);

        User user = Mockito.mock(User.class);
        XWikiContext context = Mockito.mock(XWikiContext.class);
        XWiki wiki = Mockito.mock(XWiki.class);
        XWikiDocument doc = Mockito.mock(XWikiDocument.class);
        BaseObject baseObject = Mockito.mock(BaseObject.class);
        @SuppressWarnings( "unchecked" )
        List<BaseObject> appList = Mockito.mock(List.class);
        @SuppressWarnings( "unchecked" )
        Iterator<BaseObject> appListI = Mockito.mock(Iterator.class);
        String userId = "id";

        Mockito.when(context.getWiki()).thenReturn(wiki);
        Mockito.when(wiki.getDocument(a, context)).thenReturn(doc);
        Mockito.when(user.getId()).thenReturn(userId);
        Mockito.when(doc.getXObjects(Mockito.any(EntityReference.class))).thenReturn(appList);
        Mockito.when(appList.iterator()).thenReturn(appListI);
        Mockito.when(appListI.hasNext()).thenReturn(true, false);
        Mockito.when(appListI.next()).thenReturn(baseObject);
        Mockito.when(baseObject.getStringValue(Mockito.anyString())).thenReturn(userId);

        g.addMembershipApplicant(user, context);
    }
}
