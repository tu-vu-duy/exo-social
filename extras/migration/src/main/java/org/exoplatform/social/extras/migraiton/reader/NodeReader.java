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

import org.exoplatform.social.extras.migraiton.MigrationException;

import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import java.io.IOException;
import java.io.OutputStream;

/**
 * @author <a href="mailto:alain.defrance@exoplatform.com">Alain Defrance</a>
 * @version $Revision$
 */
public interface NodeReader {

  void readIdentities(OutputStream os) throws RepositoryException, IOException;

  void readSpaces(OutputStream os) throws RepositoryException, IOException;

  void readProfiles(OutputStream os) throws RepositoryException, IOException;

  void readActivities(OutputStream os) throws RepositoryException, IOException;

  void readRelationships(OutputStream os) throws RepositoryException, IOException;

  void checkData() throws MigrationException;

}
