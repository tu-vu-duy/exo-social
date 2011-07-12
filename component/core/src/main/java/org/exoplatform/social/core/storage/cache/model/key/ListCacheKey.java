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

import org.apache.poi.hssf.record.formula.functions.Offset;

/**
 * @author <a href="mailto:alain.defrance@exoplatform.com">Alain Defrance</a>
 * @version $Revision$
 */
public class ListCacheKey implements CacheKey {

  private final CacheKey key;
  private final long offset;
  private final long limit;

  public ListCacheKey(final CacheKey key, final long offset, final long limit) {
    this.key = key;
    this.offset = offset;
    this.limit = limit;
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof ListCacheKey)) {
      return false;
    }

    ListCacheKey that = (ListCacheKey) o;

    if (limit != that.limit) {
      return false;
    }
    if (offset != that.offset) {
      return false;
    }
    if (key != null ? !key.equals(that.key) : that.key != null) {
      return false;
    }

    return true;
  }

  @Override
  public int hashCode() {
    int result = key != null ? key.hashCode() : 0;
    result = 31 * result + (int) (offset ^ (offset >>> 32));
    result = 31 * result + (int) (limit ^ (limit >>> 32));
    return result;
  }

}
