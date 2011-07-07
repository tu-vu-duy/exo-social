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

package org.exoplatform.social.core.storage.cache.model.data;

import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href="mailto:alain.defrance@exoplatform.com">Alain Defrance</a>
 * @version $Revision$
 */
public class BlockCacheData<T> implements CacheData<List<T>> {

  private final List<CacheData<T>> cachedData;

  public BlockCacheData(final List<T> data, final Class<? extends CacheData<T>> clazz) {
    this.cachedData = new ArrayList<CacheData<T>>();

    try {
      for (T d : data) {
        CacheData<T> newData = clazz.getConstructor(d.getClass()).newInstance(d);
        cachedData.add(newData);
      }
    }
    catch (Exception e) {
      e.printStackTrace();
    }
  }

  public List<T> build() {
    List<T> list = new ArrayList<T>();

    for (CacheData<T> currentData : cachedData) {
      list.add(currentData.build());
    }

    return list;
  }

}
