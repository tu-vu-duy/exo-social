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
public class MigrationTestCase extends AbstractMigrationTestCase {

  private DataLoader loader;
  private Node rootNode;

  @Override
  public void setUp() throws Exception {

    super.setUp();
    loader = new DataLoader("migrationdata-11x.xml");
    loader.load();
    rootNode = loader.getSession().getRootNode();

  }

  @Override
  public void tearDown() throws Exception {

    rootNode.getNode("exo:applications").remove();

  }

  public void testLoad() throws Exception {

    assertNotNull(rootNode.getNode("exo:applications").getNode("Social_Identity"));
    assertNotNull(rootNode.getNode("exo:applications").getNode("Social_Space"));
    assertNotNull(rootNode.getNode("exo:applications").getNode("Social_Relationship"));
    assertNotNull(rootNode.getNode("exo:applications").getNode("Social_Profile"));
    assertNotNull(rootNode.getNode("exo:applications").getNode("Social_Activity"));

  }
}
