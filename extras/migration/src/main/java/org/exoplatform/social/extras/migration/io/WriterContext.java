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

package org.exoplatform.social.extras.migration.io;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * @author <a href="mailto:alain.defrance@exoplatform.com">Alain Defrance</a>
 * @version $Revision$
 */
public class WriterContext extends HashMap<String, String> {

  public enum DataType {
    IDENTITIES,
    SPACES,
    PROFILES,
    RELATIONSHIPS,
    ACTIVITIES;
  }

  private final String from;
  private final String to;

  private final Map<DataType, Boolean> completion;
  private final Map<DataType, Long> done;


  public WriterContext(final int i, final float v, final String from, final String to) {
    super(i, v);
    this.from = from;
    this.to = to;
    this.completion = new HashMap<DataType, Boolean>();
    this.done = new HashMap<DataType, Long>();
  }

  public WriterContext(final int i, final String from, final String to) {
    super(i);
    this.from = from;
    this.to = to;
    this.completion = new HashMap<DataType, Boolean>();
    this.done = new HashMap<DataType, Long>();
  }

  public WriterContext(final String from, final String to) {
    this.from = from;
    this.to = to;
    this.completion = new HashMap<DataType, Boolean>();
    this.done = new HashMap<DataType, Long>();
  }

  public WriterContext(final Map<? extends String, ? extends String> map, final String from, final String to) {
    super(map);
    this.from = from;
    this.to = to;
    this.completion = new HashMap<DataType, Boolean>();
    this.done = new HashMap<DataType, Long>();
  }

  public void load(InputStream is) {
    throw new RuntimeException();
  }

  public void write(OutputStream os) {
    throw new RuntimeException();
  }

  public String getFrom() {
    return from;
  }

  public String getTo() {
    return to;
  }

  public boolean isCompleted(DataType type) {
    return (completion.get(type) != null && completion.get(type));
  }

  public void setCompleted(DataType type) {
    completion.put(type, true);
  }
  
}
