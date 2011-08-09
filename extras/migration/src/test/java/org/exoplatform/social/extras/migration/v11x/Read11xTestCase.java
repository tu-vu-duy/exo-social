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

package org.exoplatform.social.extras.migration.v11x;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.services.organization.Group;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.social.extras.migraiton.MigrationConst;
import org.exoplatform.social.extras.migraiton.loading.DataLoader;
import org.exoplatform.social.extras.migraiton.reader.NodeReader;
import org.exoplatform.social.extras.migraiton.reader.NodeReader11x;
import org.exoplatform.social.extras.migration.AbstractMigrationTestCase;
import org.exoplatform.social.extras.migration.Utils;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collection;

/**
 * @author <a href="mailto:alain.defrance@exoplatform.com">Alain Defrance</a>
 * @version $Revision$
 */
public class Read11xTestCase extends AbstractMigrationTestCase {

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

  public void testReadIdentity() throws Exception {

    NodeReader reader = new NodeReader11x(session);

    //
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    reader.readIdentities(new FileOutputStream("identities"));
    reader.readIdentities(baos);

    //
    ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
    DataInputStream dis = new DataInputStream(bais);

    checkIdentity(dis, "exo:identity", "user_idA", "organization");
    checkIdentity(dis, "exo:identity[2]", "user_idB", "organization");
    checkIdentity(dis, "exo:identity[3]", "user_idC", "organization");
    checkIdentity(dis, "exo:identity[4]", "user_idD", "organization");
    checkIdentity(dis, "exo:identity[5]", "user_idE", "organization");

    checkIdentity(dis, "user_a", "user_a", "organization");
    checkIdentity(dis, "user_b", "user_b", "organization");
    checkIdentity(dis, "user_c", "user_c", "organization");
    checkIdentity(dis, "user_d", "user_d", "organization");
    checkIdentity(dis, "user_e", "user_e", "organization");

    String uuid = rootNode.getNode("exo:applications/Social_Space/Space/exo:space").getUUID();
    checkIdentity(dis, uuid, uuid, "space");

    uuid = rootNode.getNode("exo:applications/Social_Space/Space/exo:space[2]").getUUID();
    checkIdentity(dis, uuid, uuid, "space");

    uuid = rootNode.getNode("exo:applications/Social_Space/Space/exo:space[3]").getUUID();
    checkIdentity(dis, uuid, uuid, "space");

  }

  public void testReadRelationship() throws Exception {

    NodeReader reader = new NodeReader11x(session);

    //
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    reader.readIdentities(new FileOutputStream("relationships"));
    reader.readRelationships(baos);

    //
    ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
    DataInputStream dis = new DataInputStream(bais);

    String identity1Id = rootNode.getNode("exo:applications/Social_Identity/exo:identity[3]").getUUID();
    String identity2Id = rootNode.getNode("exo:applications/Social_Identity/exo:identity[2]").getUUID();
    checkRelationship(dis, "exo:relationship", identity1Id, identity2Id, "CONFIRM");

    identity1Id = rootNode.getNode("exo:applications/Social_Identity/exo:identity[3]").getUUID();
    identity2Id = rootNode.getNode("exo:applications/Social_Identity/exo:identity[4]").getUUID();
    checkRelationship(dis, "exo:relationship[2]", identity1Id, identity2Id, "PENDING");

    identity1Id = rootNode.getNode("exo:applications/Social_Identity/user_a").getUUID();
    identity2Id = rootNode.getNode("exo:applications/Social_Identity/exo:identity[4]").getUUID();
    checkRelationship(dis, "exo:relationship[3]", identity1Id, identity2Id, "CONFIRM");

    identity1Id = rootNode.getNode("exo:applications/Social_Identity/user_d").getUUID();
    identity2Id = rootNode.getNode("exo:applications/Social_Identity/exo:identity").getUUID();
    checkRelationship(dis, "exo:relationship[4]", identity1Id, identity2Id, "CONFIRM");

    identity1Id = rootNode.getNode("exo:applications/Social_Identity/user_b").getUUID();
    identity2Id = rootNode.getNode("exo:applications/Social_Identity/user_a").getUUID();
    checkRelationship(dis, "exo:relationship[5]", identity1Id, identity2Id, "PENDING");

    identity1Id = rootNode.getNode("exo:applications/Social_Identity/user_c").getUUID();
    identity2Id = rootNode.getNode("exo:applications/Social_Identity/user_d").getUUID();
    checkRelationship(dis, "exo:relationship[6]", identity1Id, identity2Id, "CONFIRM");

    identity1Id = rootNode.getNode("exo:applications/Social_Identity/user_c").getUUID();
    identity2Id = rootNode.getNode("exo:applications/Social_Identity/user_a").getUUID();
    checkRelationship(dis, "ec1bbdea2e8902a901cf62bd95f0bdc8", identity1Id, identity2Id, "CONFIRM");

  }

  public void testReadSpaces() throws Exception {

    NodeReader reader = new NodeReader11x(session);

    //
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    reader.readIdentities(new FileOutputStream("spaces"));
    reader.readSpaces(baos);

    //
    ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
    DataInputStream dis = new DataInputStream(bais);

    checkSpace(dis, "exo:space", "a", new String[]{"user_a","user_b","user_d"}, null);
    checkSpace(dis, "exo:space[2]", "b", null, null);
    checkSpace(dis, "exo:space[3]", "c", null, new String[]{"user_a","user_d"});
    checkSpace(dis, "exo:space[4]", "d", null, new String[]{"user_a","user_d"});
    checkSpace(dis, "exo:space[5]", "e", new String[]{"user_c"}, new String[]{"user_a","user_d"});

  }

  private void checkIdentity(DataInputStream dis, String nodeName, String remoteId, String providerId) throws IOException, RepositoryException {

    String path;

    assertEquals(MigrationConst.START_NODE, dis.readInt());
    assertEquals("/exo:applications/Social_Identity/" + nodeName, path = dis.readUTF());

    assertEquals(MigrationConst.PROPERTY_SINGLE, dis.readInt());
    assertEquals("jcr:primaryType", dis.readUTF());
    assertEquals("exo:identity", dis.readUTF());

    assertEquals(MigrationConst.PROPERTY_MULTI, dis.readInt());
    assertEquals("jcr:mixinTypes", dis.readUTF());
    assertEquals("mix:referenceable", dis.readUTF());

    assertEquals(MigrationConst.PROPERTY_SINGLE, dis.readInt());
    assertEquals("jcr:uuid", dis.readUTF());
    assertEquals(rootNode.getNode(path.substring(1)).getUUID(), dis.readUTF());

    assertEquals(MigrationConst.PROPERTY_SINGLE, dis.readInt());
    assertEquals("exo:providerId", dis.readUTF());
    assertEquals(providerId, dis.readUTF());

    assertEquals(MigrationConst.PROPERTY_SINGLE, dis.readInt());
    assertEquals("exo:remoteId", dis.readUTF());
    assertEquals(remoteId, dis.readUTF());

    assertEquals(MigrationConst.END_NODE, dis.readInt());

  }

  private void checkRelationship(DataInputStream dis, String nodeName, String identitiy1Id, String identitiy2Id, String status) throws IOException {

    assertEquals(MigrationConst.START_NODE, dis.readInt());
    assertEquals("/exo:applications/Social_Relationship/" + nodeName, dis.readUTF());

    assertEquals(MigrationConst.PROPERTY_SINGLE, dis.readInt());
    assertEquals("jcr:primaryType", dis.readUTF());
    assertEquals("exo:relationship", dis.readUTF());

    assertEquals(MigrationConst.PROPERTY_SINGLE, dis.readInt());
    assertEquals("exo:identity1Id", dis.readUTF());
    assertEquals(identitiy1Id, dis.readUTF());

    assertEquals(MigrationConst.PROPERTY_SINGLE, dis.readInt());
    assertEquals("exo:identity2Id", dis.readUTF());
    assertEquals(identitiy2Id, dis.readUTF());

    assertEquals(MigrationConst.PROPERTY_SINGLE, dis.readInt());
    assertEquals("exo:status", dis.readUTF());
    assertEquals(status, dis.readUTF());

    assertEquals(MigrationConst.END_NODE, dis.readInt());

  }

  private void checkSpace(DataInputStream dis, String nodeName, String suffix, String[] pendingUsers, String[] invitedUsers) throws IOException, RepositoryException {

    String path;

    assertEquals(MigrationConst.START_NODE, dis.readInt());
    assertEquals("/exo:applications/Social_Space/Space/" + nodeName, path = dis.readUTF());

    assertEquals(MigrationConst.PROPERTY_SINGLE, dis.readInt());
    assertEquals("jcr:primaryType", dis.readUTF());
    assertEquals("exo:space", dis.readUTF());

    assertEquals(MigrationConst.PROPERTY_MULTI, dis.readInt());
    assertEquals("jcr:mixinTypes", dis.readUTF());
    assertEquals("mix:referenceable", dis.readUTF());

    assertEquals(MigrationConst.PROPERTY_SINGLE, dis.readInt());
    assertEquals("jcr:uuid", dis.readUTF());
    assertEquals(rootNode.getNode(path.substring(1)).getUUID(), dis.readUTF());

    assertEquals(MigrationConst.PROPERTY_SINGLE, dis.readInt());
    assertEquals("exo:description", dis.readUTF());
    assertEquals("foo " + suffix, dis.readUTF());
    
    assertEquals(MigrationConst.PROPERTY_SINGLE, dis.readInt());
    assertEquals("exo:groupId", dis.readUTF());
    assertEquals("/spaces/name" + suffix, dis.readUTF());

    if (invitedUsers != null) {
      assertEquals(MigrationConst.PROPERTY_MULTI, dis.readInt());
      assertEquals("exo:invitedUsers", dis.readUTF());
      for (String invitedUser : invitedUsers) {
        assertEquals(invitedUser, dis.readUTF());
      }
    }

    assertEquals(MigrationConst.PROPERTY_SINGLE, dis.readInt());
    assertEquals("exo:name", dis.readUTF());
    assertEquals("Name " + suffix, dis.readUTF());

    if (pendingUsers != null) {
      assertEquals(MigrationConst.PROPERTY_MULTI, dis.readInt());
      assertEquals("exo:pendingUsers", dis.readUTF());
      for (String pendingUser : pendingUsers) {
        assertEquals(pendingUser, dis.readUTF());
      }
    }

    assertEquals(MigrationConst.PROPERTY_SINGLE, dis.readInt());
    assertEquals("exo:priority", dis.readUTF());
    assertEquals("2", dis.readUTF());

    assertEquals(MigrationConst.PROPERTY_SINGLE, dis.readInt());
    assertEquals("exo:registration", dis.readUTF());
    assertEquals("validation", dis.readUTF());

    assertEquals(MigrationConst.PROPERTY_SINGLE, dis.readInt());
    assertEquals("exo:type", dis.readUTF());
    assertEquals("classic", dis.readUTF());

    assertEquals(MigrationConst.PROPERTY_SINGLE, dis.readInt());
    assertEquals("exo:url", dis.readUTF());
    assertEquals("name" + suffix, dis.readUTF());

    assertEquals(MigrationConst.PROPERTY_SINGLE, dis.readInt());
    assertEquals("exo:visibility", dis.readUTF());
    assertEquals("private", dis.readUTF());

    assertEquals(MigrationConst.END_NODE, dis.readInt());

  }

}
