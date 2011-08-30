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

import org.exoplatform.social.extras.migration.io.NodeData;
import org.exoplatform.social.extras.migration.io.NodeStreamHandler;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href="mailto:alain.defrance@exoplatform.com">Alain Defrance</a>
 * @version $Revision$
 */
public class NodeDataBuilderTestCase extends AbstractMigrationTestCase {

  public void testReadNumber() throws Exception {

    InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(Utils.DATA_DIR + "/identities");

    assertNotNull(is);

    NodeStreamHandler handler = new NodeStreamHandler();
    List<NodeData> data = new ArrayList<NodeData>();
    NodeData currentData;
    while ((currentData = handler.readNode(is)) != null) {
      data.add(currentData);
    }
    assertNotNull(data);
    assertEquals(13, data.size());

  }

  public void testReadData() throws Exception {

    InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(Utils.DATA_DIR + "/identities");

    assertNotNull(is);

    NodeStreamHandler handler = new NodeStreamHandler();
    NodeData data = handler.readNode(is);
    assertEquals("/exo:applications/Social_Identity/exo:identity", data.getPath());
    assertEquals(0, data.getChilds().size());
    assertEquals("organization", data.get("exo:providerId"));
    assertEquals("user_idA", data.get("exo:remoteId"));
    assertNotNull(data.get("jcr:uuid"));
    assertEquals(1, ((String[]) data.get("jcr:mixinTypes")).length);
    assertEquals("mix:referenceable", ((String[]) data.get("jcr:mixinTypes"))[0]);
    assertEquals("exo:identity", data.get("jcr:primaryType"));

  }
}
