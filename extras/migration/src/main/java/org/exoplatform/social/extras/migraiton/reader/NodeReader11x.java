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

package org.exoplatform.social.extras.migraiton.reader;

import org.exoplatform.social.extras.migraiton.io.NodeStreamHandler;

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
public class NodeReader11x implements NodeReader {

  private Session session;
  private Node rootNode;
  private NodeStreamHandler writer;

  public NodeReader11x(final Session session) throws RepositoryException {
    this.session = session;
    this.rootNode = session.getRootNode();
    this.writer = new NodeStreamHandler();
  }

  public void readIdentities(OutputStream os) throws RepositoryException, IOException {

    Node rootIdentity = rootNode.getNode("exo:applications/Social_Identity");
    readFrom(rootIdentity, os);

  }

  public void readSpaces(OutputStream os) throws RepositoryException, IOException {

    Node rootSpace = rootNode.getNode("exo:applications/Social_Space/Space");
    readFrom(rootSpace, os);
    
  }

  public void readProfiles(OutputStream os) throws RepositoryException, IOException {
  }

  public void readActivities(OutputStream os) throws RepositoryException, IOException {

    Node rootOrganizationActivity = rootNode.getNode("exo:applications/Social_Activity/organization");
    NodeIterator userIt = rootOrganizationActivity.getNodes();
    while (userIt.hasNext()) {
      Node currentUser = userIt.nextNode();
      Node publishedNode = currentUser.getNode("published");
      readFrom(publishedNode, os);
    }

  }

  public void readRelationships(OutputStream os) throws RepositoryException, IOException {

    Node rootRelationship = rootNode.getNode("exo:applications/Social_Relationship");
    readFrom(rootRelationship, os);

  }

  private void readFrom(Node node, OutputStream os) throws RepositoryException, IOException {

    NodeIterator it = node.getNodes();
    while(it.hasNext()) {
      writer.writeNode((Node) it.next(), os);
    }

  }
  
}
