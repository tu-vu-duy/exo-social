/*
* Copyright (C) 2003-2009 eXo Platform SAS.
*
* This is free software; you can redistribute it and/or modify it
* under the terms of the GNU Lesser General Public License as
* published by the Free Software Foundation; either version 2.1 of
* the License, or (at your option) any later version.
*
* This software is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
* Lesser General Public License for more details.
*
* You should have received a copy of the GNU Lesser General Public
* License along with this software; if not, write to the Free
* Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
* 02110-1301 USA, or see the FSF site: http://www.fsf.org.
*/

package org.exoplatform.social.core.chromattic.entity;

import org.chromattic.api.annotations.*;
import org.chromattic.ext.ntdef.NTFile;
import org.exoplatform.social.core.storage.query.PropertyLiteralExpression;

import java.util.List;
import java.util.Map;

/**
 * @author <a href="mailto:alain.defrance@exoplatform.com">Alain Defrance</a>
 * @version $Revision$
 */
@PrimaryType(name = "soc:profiledefinition")
public abstract class ProfileEntity {

  @Id
  public abstract String getId();

  @MappedBy("soc:avatar")
  @OneToOne
  @Owner
  public abstract NTFile getAvatar();
  public abstract void setAvatar(NTFile avatar);

  @MappedBy("soc:profile")
  @OneToOne
  public abstract IdentityEntity getIdentity();
  public abstract void setIdentity(IdentityEntity identity);

  @Properties
  public abstract Map<String, Object> getProperties();
  public static final PropertyLiteralExpression firstName = new PropertyLiteralExpression(String.class, "firstName");
  public static final PropertyLiteralExpression fullName = new PropertyLiteralExpression(String.class, "fullName");
  public static final PropertyLiteralExpression position = new PropertyLiteralExpression(String.class, "position");
  public static final PropertyLiteralExpression gender = new PropertyLiteralExpression(String.class, "gender");

  @Create
  public abstract NTFile createAvatar();

  public Object getProperty(String key) {
    return getProperties().get(key);
  }

  public void setProperty(String key, Object value) {
    getProperties().put(key, value);
  }
}