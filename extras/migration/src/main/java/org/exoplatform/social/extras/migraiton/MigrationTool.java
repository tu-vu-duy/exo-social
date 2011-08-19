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

package org.exoplatform.social.extras.migraiton;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.container.component.RequestLifeCycle;
import org.exoplatform.services.organization.OrganizationService;
import org.exoplatform.social.core.storage.api.ActivityStorage;
import org.exoplatform.social.core.storage.api.IdentityStorage;
import org.exoplatform.social.core.storage.api.RelationshipStorage;
import org.exoplatform.social.core.storage.api.SpaceStorage;
import org.exoplatform.social.core.storage.impl.ActivityStorageImpl;
import org.exoplatform.social.core.storage.impl.IdentityStorageImpl;
import org.exoplatform.social.core.storage.impl.RelationshipStorageImpl;
import org.exoplatform.social.core.storage.impl.SpaceStorageImpl;
import org.exoplatform.social.extras.migraiton.io.WriterContext;
import org.exoplatform.social.extras.migraiton.reader.NodeReader;
import org.exoplatform.social.extras.migraiton.reader.NodeReader11x;
import org.exoplatform.social.extras.migraiton.writer.NodeWriter;
import org.exoplatform.social.extras.migraiton.writer.NodeWriter12x;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;

/**
 * @author <a href="mailto:alain.defrance@exoplatform.com">Alain Defrance</a>
 * @version $Revision$
 */
public class MigrationTool {

  public static final int START_NODE        = 1;
  public static final int PROPERTY_SINGLE   = 2;
  public static final int PROPERTY_MULTI    = 3;
  public static final int END_NODE          = 4;

  private final IdentityStorage identityStorage;
  private final RelationshipStorage relationshipStorage;
  private final SpaceStorage spaceStorage;
  private final ActivityStorage activityStorage;

  private final OrganizationService organizationService;

  public MigrationTool() {

    PortalContainer container = PortalContainer.getInstance();

    identityStorage = (IdentityStorage) container.getComponentInstanceOfType(IdentityStorageImpl.class);
    relationshipStorage = (RelationshipStorage) container.getComponentInstanceOfType(RelationshipStorageImpl.class);
    spaceStorage = (SpaceStorage) container.getComponentInstanceOfType(SpaceStorageImpl.class);
    activityStorage = (ActivityStorage) container.getComponentInstanceOfType(ActivityStorageImpl.class);
    organizationService = (OrganizationService) container.getComponentInstance(OrganizationService.class);

  }

  private void runAll(String oldVersion, String newVersion, Session session) throws IOException, RepositoryException {

    NodeReader reader = createReader(oldVersion, session);
    reader.checkData();

    NodeWriter writer = createWriter(newVersion, session);
    WriterContext ctx = new WriterContext();

    runIdentities(reader, writer, ctx);
    runSpaces(reader, writer, ctx);
    runRelationships(reader, writer, ctx);
    runActivities(reader, writer, ctx);

  }

  public void runIdentities(NodeReader reader, NodeWriter writer, WriterContext ctx) throws IOException, RepositoryException {

    reader.checkData();

    PipedOutputStream os = new PipedOutputStream();
    PipedInputStream is = new PipedInputStream(os);

    reader.readIdentities(os);
    writer.writeIdentities(is, ctx);

  }

  public void runProfiles(NodeReader reader, NodeWriter writer, WriterContext ctx) throws IOException, RepositoryException {

    reader.checkData();

    PipedOutputStream os = new PipedOutputStream();
    PipedInputStream is = new PipedInputStream(os);

    reader.readProfiles(os);
    writer.writeProfiles(is, ctx);

  }

  public void runSpaces(NodeReader reader, NodeWriter writer, WriterContext ctx) throws IOException, RepositoryException {

    reader.checkData();

    PipedOutputStream os = new PipedOutputStream();
    PipedInputStream is = new PipedInputStream(os);

    reader.readSpaces(os);

    RequestLifeCycle.begin(PortalContainer.getInstance());
    writer.writeSpaces(is, ctx);
    RequestLifeCycle.end();

  }

  public void runRelationships(NodeReader reader, NodeWriter writer, WriterContext ctx) throws IOException, RepositoryException {

    reader.checkData();

    PipedOutputStream os = new PipedOutputStream();
    PipedInputStream is = new PipedInputStream(os);

    reader.readRelationships(os);
    writer.writeRelationships(is, ctx);

  }

  public void runActivities(NodeReader reader, NodeWriter writer, WriterContext ctx) throws IOException, RepositoryException {

    reader.checkData();

    PipedOutputStream os = new PipedOutputStream();
    PipedInputStream is = new PipedInputStream(os);

    reader.readActivities(os);
    writer.writeActivities(is, ctx);

  }

  public NodeReader createReader(String version, Session session) throws RepositoryException {

    // TODO : use version
    return new NodeReader11x(session);
  }

  public NodeWriter createWriter(String version, Session session) throws RepositoryException {

    // TODO : use version
    return new NodeWriter12x(identityStorage, relationshipStorage, spaceStorage, activityStorage, organizationService, session);
  }



  /*public void exportToStream(Session session, String version, String fileName) {

    File file = new File(fileName);
    if (file.isDirectory()) {
      throw new RuntimeException();
    }

    File identitiesFile = new File(file, "identities");
    File relationshipFile = new File(file, "relationships");
    File spacesFile = new File(file, "spaces");
    File activitiesFile = new File(file, "activities");

    try {

      FileOutputStream identitiesOs = new FileOutputStream(identitiesFile);
      FileOutputStream relationshipsOs = new FileOutputStream(relationshipFile);
      FileOutputStream spacesOs = new FileOutputStream(spacesFile);
      FileOutputStream activitiesOs = new FileOutputStream(activitiesFile);
      exportToStream(session, version, identitiesOs, relationshipsOs, spacesOs, activitiesOs);

    }
    catch (FileNotFoundException e) {
      e.printStackTrace();
    }


  }

  private void exportToStream(Session session, String version, OutputStream identitiesOs, OutputStream relationshipsOs, OutputStream spacesOs, OutputStream activitiesOs) {

    try {

      NodeReader reader = new NodeReader11x(session);
      reader.readIdentities(identitiesOs);
      reader.readRelationships(relationshipsOs);
      reader.readSpaces(spacesOs);
      reader.readActivities(activitiesOs);

    }
    catch (FileNotFoundException e) {
      e.printStackTrace();
    }
    catch (RepositoryException e) {
      e.printStackTrace();
    }
    catch (IOException e) {
      e.printStackTrace();
    }

  }

  private void importFromStream(Session session, String version, String fileName) {

    File file = new File(fileName);
    if (file.isDirectory()) {
      throw new RuntimeException();
    }

    File identitiesFile = new File(file, "identities");
    File relationshipFile = new File(file, "relationships");
    File spacesFile = new File(file, "spaces");
    File activitiesFile = new File(file, "activities");

    try {

      FileInputStream identitiesOs = new FileInputStream(identitiesFile);
      FileInputStream relationshipsOs = new FileInputStream(relationshipFile);
      FileInputStream spacesOs = new FileInputStream(spacesFile);
      FileInputStream activitiesOs = new FileInputStream(activitiesFile);
      exportFromStream(session, version, identitiesOs, relationshipsOs, spacesOs, activitiesOs);

    }
    catch (FileNotFoundException e) {
      e.printStackTrace();
    }
    
  }

  private void exportFromStream(Session session, String version, InputStream identitiesOs, InputStream relationshipsOs, InputStream spacesOs, InputStream activitiesOs) {

    NodeWriter writer = new NodeWriter12x(identityStorage, relationshipStorage, spaceStorage, activityStorage, organizationService, session);
    WriterContext ctx = new WriterContext();
    writer.writeIdentities(identitiesOs, ctx);
    writer.writeRelationships(relationshipsOs, ctx);
    writer.writeSpaces(spacesOs, ctx);
    writer.writeActivities(activitiesOs, ctx);

  }*/

}
