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

package org.exoplatform.social.extras.migration.rw;

import org.exoplatform.social.extras.migration.MigrationException;
import org.exoplatform.social.extras.migration.io.NodeStreamHandler;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import java.io.IOException;
import java.io.OutputStream;

/**
 * @author <a href="mailto:alain.defrance@exoplatform.com">Alain Defrance</a>
 * @version $Revision$
 */
public class NodeReader_11x_12x implements NodeReader {

  private Session session;
  private Node rootNode;
  private NodeStreamHandler writer;

  public NodeReader_11x_12x(final Session session) throws RepositoryException {
    this.session = session;
    this.rootNode = session.getRootNode();
    this.writer = new NodeStreamHandler();
  }

  public void readIdentities(OutputStream os) throws RepositoryException, IOException {
    run(new IdentityRunnable(os));
  }

  public void readSpaces(OutputStream os) throws RepositoryException, IOException {
    run(new SpaceRunnable(os));
  }

  public void readProfiles(OutputStream os) throws RepositoryException, IOException {
    run(new ProfileRunnable(os));
  }

  public void readActivities(OutputStream os) throws RepositoryException, IOException {
    run(new ActivityRunnable(os));
  }

  public void readRelationships(OutputStream os) throws RepositoryException, IOException {
    run(new RelationshipRunnable(os));
  }

  private void readFrom(Node node, OutputStream os) throws RepositoryException, IOException {

    NodeIterator it = node.getNodes();
    while(it.hasNext()) {
      writer.writeNode((Node) it.next(), os);
    }

  }

  public void checkData() throws MigrationException {

    try {
      rootNode.getNode("exo:applications");
    }
    catch (RepositoryException e) {
      throw new MigrationException("No data found for this version");
    }

  }

  private void run(Runnable r) {
    new Thread(r).start();
  }

  class IdentityRunnable implements Runnable {
    
    private OutputStream os;

    IdentityRunnable(final OutputStream os) {
      this.os = os;
    }

    public void run() {
      try {
        Node rootIdentity = rootNode.getNode("exo:applications/Social_Identity");
        readFrom(rootIdentity, os);
        os.close();
      }
      catch (Exception e) {
        throw new MigrationException(e);
      }
    }

  }

  class RelationshipRunnable implements Runnable {

    private OutputStream os;

    RelationshipRunnable(final OutputStream os) {
      this.os = os;
    }

    public void run() {
      try {
        Node rootRelationship = rootNode.getNode("exo:applications/Social_Relationship");
        readFrom(rootRelationship, os);
        os.close();
      }
      catch (Exception e) {
        throw new MigrationException(e);
      }
    }

  }

  class SpaceRunnable implements Runnable {

    private OutputStream os;

    SpaceRunnable(final OutputStream os) {
      this.os = os;
    }

    public void run() {
      try {
        Node rootSpace = rootNode.getNode("exo:applications/Social_Space/Space");
        readFrom(rootSpace, os);
        os.close();
      }
      catch (Exception e) {
        throw new MigrationException(e);
      }
    }
  }

  class ActivityRunnable implements Runnable {

    private OutputStream os;

    ActivityRunnable(final OutputStream os) {
      this.os = os;
    }

    public void run() {
      try {
        Node rootOrganizationActivity = rootNode.getNode("exo:applications/Social_Activity/organization");
        NodeIterator userItOrganization = rootOrganizationActivity.getNodes();
        while (userItOrganization.hasNext()) {
          Node currentUser = userItOrganization.nextNode();
          Node publishedNode = currentUser.getNode("published");
          readFrom(publishedNode, os);
        }

        Node rootSpaceActivity = rootNode.getNode("exo:applications/Social_Activity/space");
        NodeIterator userItSpace = rootSpaceActivity.getNodes();
        while(userItSpace.hasNext()) {
          Node currentUser = userItSpace.nextNode();
          Node publishedNode = currentUser.getNode("published");
          readFrom(publishedNode, os);
        }
        os.close();
      }
      catch (Exception e) {
        throw new MigrationException(e);
      }
    }

  }

  class ProfileRunnable implements Runnable {

    private OutputStream os;

    ProfileRunnable(final OutputStream os) {
      this.os = os;
    }

    public void run() {
      try {
        Node rootSpace = rootNode.getNode("exo:applications/Social_Profile");
        readFrom(rootSpace, os);
        os.close();
      }
      catch (Exception e) {
        throw new MigrationException(e);
      }
    }

  }

}
