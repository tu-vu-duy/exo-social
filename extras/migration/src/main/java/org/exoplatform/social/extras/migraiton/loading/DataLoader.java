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

package org.exoplatform.social.extras.migraiton.loading;

import org.exoplatform.container.PortalContainer;
import org.exoplatform.services.jcr.RepositoryService;
import org.exoplatform.services.jcr.core.ManageableRepository;
import org.staxnav.Naming;
import org.staxnav.StaxNavigator;
import org.staxnav.StaxNavigatorImpl;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.InputStream;

/**
 * @author <a href="mailto:alain.defrance@exoplatform.com">Alain Defrance</a>
 * @version $Revision$
 */
public class DataLoader {

  private final StaxNavigator<String> navigator;

  private Session session;

  private final String DATA_PACKAGE = "org.exoplatform.social.extras.migration";

  public Session getSession() throws RepositoryException {
    if (session == null) {
      PortalContainer container = PortalContainer.getInstance();
      RepositoryService repositoryService = (RepositoryService) container.getComponentInstance(RepositoryService.class);
      ManageableRepository repository = repositoryService.getCurrentRepository();
      session = repository.getSystemSession("portal-test");
    }
    return session;
  }

  public DataLoader(final String name) {
    
    String fullName = DATA_PACKAGE.replace('.', '/') + '/' + name;

    InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(fullName);

    if (is == null) {
      throw new NullPointerException(name + " not found");
    }

    try {
      XMLInputFactory factory = XMLInputFactory.newInstance();
      XMLStreamReader stream = factory.createXMLStreamReader(is);
      navigator = new StaxNavigatorImpl<String>(new Naming.Local(), stream);
    }
    catch (XMLStreamException e) {
      throw new RuntimeException(e);
    }

  }

  public void load() throws RepositoryException {

    Node rootNode = getSession().getRootNode();

    writeNode(rootNode, navigator);
    
  }

  private void writeNode(Node node, StaxNavigator<String> nav) throws RepositoryException {

    boolean found = nav.child("node");

    while (found) {

      Node created = node.addNode(nav.getAttribute("name"));
      
      writeNode(created, nav.fork());
      writeProperty(created, nav.fork());

      found = nav.sibling("node");

    }
  }

  private void writeProperty(Node node, StaxNavigator<String> nav) throws RepositoryException {

    boolean found = nav.child("property");

    while (found) {

      node.setProperty(nav.getAttribute("name"), nav.getContent());

      found = nav.sibling("property");

    }

  }
  
}
