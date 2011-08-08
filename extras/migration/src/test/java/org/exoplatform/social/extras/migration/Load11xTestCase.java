/*
 * Copyright (C) 2003-2011 eXo Platform SAS.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package org.exoplatform.social.extras.migration;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.services.organization.Group;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.social.extras.migraiton.loading.DataLoader;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Session;
import java.util.Collection;

/**
 * @author <a href="mailto:alain.defrance@exoplatform.com">Alain Defrance</a>
 * @version $Revision$
 */
public class Load11xTestCase extends AbstractMigrationTestCase {

  private DataLoader loader;
  private Node rootNode;
  private Session session;
  private OrganizationService organizationService;

  @Override
  public void setUp() throws Exception {

    super.setUp();
    session = Utils.getSession();
    loader = new DataLoader("migrationdata-11x.xml", session);
    loader.load();
    rootNode = session.getRootNode();
    
    PortalContainer container = PortalContainer.getInstance();
    organizationService = (OrganizationService) container.getComponentInstance(OrganizationService.class);

  }

  @Override
  public void tearDown() throws Exception {

    NodeIterator it = rootNode.getNode("exo:applications").getNode("Social_Identity").getNodes();

    while(it.hasNext()) {
      String userName = ((Node) it.next()).getProperty("exo:remoteId").getString();
      organizationService.getUserHandler().removeUser(userName, true);
    }

    Group spaces = organizationService.getGroupHandler().findGroupById("/spaces");
    for (Group group : (Collection<Group>) organizationService.getGroupHandler().findGroups(spaces)) {
      organizationService.getGroupHandler().removeGroup(group, true);
    }

    rootNode.getNode("exo:applications").remove();

    super.tearDown();

  }

  public void testLoad() throws Exception {

    assertNotNull(rootNode.getNode("exo:applications").getNode("Social_Identity"));
    assertNotNull(rootNode.getNode("exo:applications").getNode("Social_Space"));
    assertNotNull(rootNode.getNode("exo:applications").getNode("Social_Relationship"));
    assertNotNull(rootNode.getNode("exo:applications").getNode("Social_Profile"));
    assertNotNull(rootNode.getNode("exo:applications").getNode("Social_Activity"));

  }

}
