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

package org.exoplatform.social.core.storage.cache.model.key;

import org.exoplatform.social.core.relationship.model.Relationship;
import org.exoplatform.social.core.storage.cache.CachedListKey;

import java.io.Serializable;

/**
 * @author <a href="mailto:alain.defrance@exoplatform.com">Alain Defrance</a>
 * @version $Revision$
 */
public class RelationshipListKey extends CachedListKey implements Serializable {

  private final IdentityKey identityKey;

  private final Relationship.Type type;

  public RelationshipListKey(final int offset, final IdentityKey identityKey, final Relationship.Type type) {
    super(offset);
    this.identityKey = identityKey;
    this.type = type;
  }

  public IdentityKey getIdentityKey() {
    return identityKey;
  }

  public Relationship.Type getType() {
    return type;
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof RelationshipListKey)) {
      return false;
    }
    if (!super.equals(o)) {
      return false;
    }

    RelationshipListKey that = (RelationshipListKey) o;

    if (identityKey != null ? !identityKey.equals(that.identityKey) : that.identityKey != null) {
      return false;
    }
    if (type != that.type) {
      return false;
    }

    return true;
  }

  @Override
  public int hashCode() {
    int result = super.hashCode();
    result = 31 * result + (identityKey != null ? identityKey.hashCode() : 0);
    result = 31 * result + (type != null ? type.hashCode() : 0);
    return result;
  }
}
