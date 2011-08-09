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

package org.exoplatform.social.extras.migration.v12x;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.services.organization.Group;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.social.core.storage.api.ActivityStorage;
import org.exoplatform.social.core.storage.api.IdentityStorage;
import org.exoplatform.social.core.storage.api.RelationshipStorage;
import org.exoplatform.social.core.storage.api.SpaceStorage;
import org.exoplatform.social.core.storage.impl.ActivityStorageImpl;
import org.exoplatform.social.core.storage.impl.IdentityStorageImpl;
import org.exoplatform.social.core.storage.impl.RelationshipStorageImpl;
import org.exoplatform.social.core.storage.impl.SpaceStorageImpl;
import org.exoplatform.social.extras.migraiton.loading.DataLoader;
import org.exoplatform.social.extras.migraiton.writer.NodeWriter;
import org.exoplatform.social.extras.migraiton.writer.NodeWriter12x;
import org.exoplatform.social.extras.migration.AbstractMigrationTestCase;
import org.exoplatform.social.extras.migration.Utils;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import java.io.InputStream;
import java.util.Collection;

/**
 * @author <a href="mailto:alain.defrance@exoplatform.com">Alain Defrance</a>
 * @version $Revision$
 */
public class Write12xTestCase extends AbstractMigrationTestCase {

  private IdentityStorage identityStorage;
  private RelationshipStorage relationshipStorage;
  private SpaceStorage spaceStorage;
  private ActivityStorage activityStorage;
  private OrganizationService organizationService;

  private Session session;

  private Node rootNode;

  @Override
  public void setUp() throws Exception {

    super.setUp();

    PortalContainer container = PortalContainer.getInstance();

    identityStorage = (IdentityStorage) container.getComponentInstanceOfType(IdentityStorageImpl.class);
    relationshipStorage = (RelationshipStorage) container.getComponentInstanceOfType(RelationshipStorageImpl.class);
    spaceStorage = (SpaceStorage) container.getComponentInstanceOfType(SpaceStorageImpl.class);
    activityStorage = (ActivityStorage) container.getComponentInstanceOfType(ActivityStorageImpl.class);
    organizationService = (OrganizationService) container.getComponentInstance(OrganizationService.class);

    session = Utils.getSession();
    rootNode = session.getRootNode();
    DataLoader loader = new DataLoader("migrationdata-11x.xml", session);
    loader.load();

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
    NodeIterator providers = rootNode.getNode("production").getNode("soc:providers").getNodes();
    while(providers.hasNext()) {
      providers.nextNode().remove();
    }

    super.tearDown();

  }

  public void testWriteIdentities() throws Exception {

    InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(Utils.DATA_DIR + "/identities");
    
    NodeWriter writer = new NodeWriter12x(identityStorage, relationshipStorage, spaceStorage, activityStorage, session);
    writer.writeIdentities(is);

    checkIdentity("organization", "user_idA");
    checkIdentity("organization", "user_idB");
    checkIdentity("organization", "user_idC");
    checkIdentity("organization", "user_idD");
    checkIdentity("organization", "user_idE");

    checkIdentity("organization", "user_a");
    checkIdentity("organization", "user_b");
    checkIdentity("organization", "user_c");
    checkIdentity("organization", "user_d");
    checkIdentity("organization", "user_e");

    // TODO : fix spaces identity

    /*checkIdentity("space", "namea");
    checkIdentity("space", "nameb");
    checkIdentity("space", "namec");*/

  }

  public void testWriteRelationships() throws Exception {

    InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(Utils.DATA_DIR + "/relationships");

    NodeWriter writer = new NodeWriter12x(identityStorage, relationshipStorage, spaceStorage, activityStorage, session);
    writer.writeRelationships(is);

    checkRelationship("organization", "user_idC", "user_idB", "relationship");
    checkRelationship("organization", "user_idB", "user_idC", "relationship");

    checkRelationship("organization", "user_idC", "user_idD", "sender");
    checkRelationship("organization", "user_idD", "user_idC", "receiver");

    checkRelationship("organization", "user_a", "user_idD", "relationship");
    checkRelationship("organization", "user_idD", "user_a", "relationship");

    checkRelationship("organization", "user_d", "user_idA", "relationship");
    checkRelationship("organization", "user_idA", "user_d", "relationship");

    checkRelationship("organization", "user_b", "user_a", "sender");
    checkRelationship("organization", "user_a", "user_b", "receiver");

    checkRelationship("organization", "user_c", "user_d", "relationship");
    checkRelationship("organization", "user_d", "user_c", "relationship");

  }

  private void checkIdentity(String providerId, String remoteId) throws RepositoryException {

    rootNode.getNode("production/soc:providers/soc:" + providerId + "/soc:" + remoteId);

  }

  private void checkRelationship(String providerId, String remoteId, String contactId, String type) throws RepositoryException {

    rootNode.getNode("production/soc:providers/soc:" + providerId + "/soc:" + remoteId + "/soc:" + type + "/soc:" + contactId);

  }

}
