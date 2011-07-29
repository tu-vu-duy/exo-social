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
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.staxnav.Naming;
import org.staxnav.StaxNavigator;
import org.staxnav.StaxNavigatorImpl;

import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.InputStream;
import java.util.Map;

/**
 * @author <a href="mailto:alain.defrance@exoplatform.com">Alain Defrance</a>
 * @version $Revision$
 */
public class DataLoader {

  private final StaxNavigator<QName> navigator;

  private Session session;

  private final String DATA_PACKAGE = "org.exoplatform.social.extras.migration";

  private static final Log LOG = ExoLogger.getLogger(DataLoader.class);

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
      navigator = new StaxNavigatorImpl<QName>(new Naming.Qualified(), stream);
    }
    catch (XMLStreamException e) {
      throw new RuntimeException(e);
    }

  }

  public void load() throws RepositoryException {

    Node rootNode = getSession().getRootNode();

    writeNode(rootNode, navigator);
    
  }

  private void writeNode(Node node, StaxNavigator<QName> nav) throws RepositoryException {

    QName current = nav.child();

    while (current != null) {

      if ("loader".equals(current.getPrefix())) {
        current = nav.sibling();
        continue;
      }

      String name = current.getLocalPart();
      if (!"".equals(current.getPrefix())) {
        name = current.getPrefix() + ":" + name;
      }

      String type = nav.getAttribute(new QName("loader", "type"));
      Node created;
      if (type == null) {
        created = node.addNode(name);
      }
      else {
        created = node.addNode(name, type);
      }
      LOG.info("Create node : " + created.getPath());

      writeNode(created, nav.fork());
      handleMixins(created, nav.fork());
      writeProperty(created, nav.fork());

      current = nav.sibling();

    }
  }

  private void writeProperty(Node node, StaxNavigator<QName> nav) throws RepositoryException {

    Map<QName, String> attributes = nav.getQualifiedAttributes();
    if (attributes.isEmpty()) {
      return;
    }

    for (QName key : attributes.keySet()) {


      if ("loader".equals(key.getPrefix())) {
        continue;
      }

      String name = key.getLocalPart();
      if (!"".equals(key.getPrefix())) {
        name = key.getPrefix() + ":" + name;
      }

      Property p = node.setProperty(name, attributes.get(key));
      LOG.info("Create property : " + p.getPath());
    }

  }

  private void handleMixins(Node node, StaxNavigator<QName> nav) throws RepositoryException {

    boolean found = nav.child(new QName("loader", "mixin"));

    while (found) {

      node.addMixin(nav.getContent());

      found = nav.sibling(new QName("loader", "mixin"));

    }

  }
  
}
