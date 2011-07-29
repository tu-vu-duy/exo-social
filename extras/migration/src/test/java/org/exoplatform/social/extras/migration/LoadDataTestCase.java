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

import org.exoplatform.social.extras.migraiton.loading.DataLoader;

import javax.jcr.Node;

/**
 * @author <a href="mailto:alain.defrance@exoplatform.com">Alain Defrance</a>
 * @version $Revision$
 */
public class LoadDataTestCase extends AbstractMigrationTestCase {

  private DataLoader loader;
  private Node rootNode;

  @Override
  public void setUp() throws Exception {

    super.setUp();
    loader = new DataLoader("testLoader.xml");
    loader.load();
    rootNode = loader.getSession().getRootNode();

  }

  @Override
  public void tearDown() throws Exception {

    rootNode.getNode("fooA").remove();
    rootNode.getNode("fooB").remove();
    rootNode.getNode("fooC").remove();
    
  }

  public void testRoot() throws Exception {

    assertNotNull(rootNode.getNode("fooA"));
    assertNotNull(rootNode.getNode("fooB"));
    assertNotNull(rootNode.getNode("fooC"));

  }

  public void testChildBar() throws Exception {

    assertNotNull(rootNode.getNode("fooA").getNode("barAA"));
    assertNotNull(rootNode.getNode("fooA").getNode("barAB"));
    assertNotNull(rootNode.getNode("fooA").getNode("barAC"));
    assertNotNull(rootNode.getNode("fooB").getNode("barBA"));
    assertNotNull(rootNode.getNode("fooB").getNode("barBB"));
    assertNotNull(rootNode.getNode("fooB").getNode("barBC"));
    assertNotNull(rootNode.getNode("fooC").getNode("barCA"));
    assertNotNull(rootNode.getNode("fooC").getNode("barCB"));
    assertNotNull(rootNode.getNode("fooC").getNode("barCC"));

  }

  public void testChildFooBar() throws Exception {

    assertNotNull(rootNode.getNode("fooA").getNode("barAA").getNode("foobarAAA"));
    assertNotNull(rootNode.getNode("fooA").getNode("barAA").getNode("foobarAAB"));
    assertNotNull(rootNode.getNode("fooA").getNode("barAA").getNode("foobarAAC"));
    assertNotNull(rootNode.getNode("fooA").getNode("barAB").getNode("foobarABA"));
    assertNotNull(rootNode.getNode("fooA").getNode("barAB").getNode("foobarABB"));
    assertNotNull(rootNode.getNode("fooA").getNode("barAB").getNode("foobarABC"));
    assertNotNull(rootNode.getNode("fooA").getNode("barAC").getNode("foobarACA"));
    assertNotNull(rootNode.getNode("fooA").getNode("barAC").getNode("foobarACB"));
    assertNotNull(rootNode.getNode("fooA").getNode("barAC").getNode("foobarACC"));

  }

  public void testSibling() throws Exception {

    assertNotNull(rootNode.getNode("fooC").getNode("barCA").getNode("sibling"));
    assertNotNull(rootNode.getNode("fooC").getNode("barCA").getNode("sibling[2]"));
    assertNotNull(rootNode.getNode("fooC").getNode("barCA").getNode("sibling[3]"));

  }

  public void testProperties() throws Exception {

    assertNotNull(rootNode.getNode("fooB").getNode("barBB").getProperty("nameBBA"));
    assertEquals("valueBBA", rootNode.getNode("fooB").getNode("barBB").getProperty("nameBBA").getString());

    assertNotNull(rootNode.getNode("fooB").getNode("barBB").getProperty("nameBBB"));
    assertEquals("valueBBB", rootNode.getNode("fooB").getNode("barBB").getProperty("nameBBB").getString());

    assertNotNull(rootNode.getNode("fooB").getNode("barBB").getProperty("nameBBC"));
    assertEquals("valueBBC", rootNode.getNode("fooB").getNode("barBB").getProperty("nameBBC").getString());

    assertNotNull(rootNode.getNode("fooA").getNode("barAB").getProperty("somewhere"));
    assertEquals("somewhereValue", rootNode.getNode("fooA").getNode("barAB").getProperty("somewhere").getString());
    
  }
}
