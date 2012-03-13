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

package org.exoplatform.social.core.identity.provider;

import org.exoplatform.social.core.identity.IdentityProvider;
import org.exoplatform.social.core.identity.model.Identity;
import org.exoplatform.social.core.identity.model.Profile;

/**
 * @author <a href="mailto:alain.defrance@exoplatform.com">Alain Defrance</a>
 */
public class IntegrationIdentityProvider extends IdentityProvider<Identity> {

  /** The Constant NAME. */
  public final static String NAME = "integration";
  
  @Override
  public String getName() {
    return NAME;
  }

  @Override
  public Identity findByRemoteId(final String remoteId) {
    return new Identity(NAME, remoteId);
  }

  @Override
  public Identity createIdentity(final Identity remoteObject) {
    return remoteObject;
  }

  @Override
  public void populateProfile(final Profile profile, final Identity remoteObject) {
  }
}
