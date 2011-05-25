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

import org.chromattic.api.annotations.Id;
import org.chromattic.api.annotations.Name;
import org.chromattic.api.annotations.PrimaryType;
import org.chromattic.api.annotations.Property;
import org.exoplatform.social.core.storage.query.PropertyLiteralExpression;

import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href="mailto:alain.defrance@exoplatform.com">Alain Defrance</a>
 * @version $Revision$
 */
@PrimaryType(name = "soc:spacedefinition")
public abstract class SpaceEntity {

  @Id
  public abstract String getId();
  
  @Name
  public abstract String getName();

  @Property(name = "soc:app")
  public abstract String getApp();
  public abstract void setApp(String app);

  @Property(name = "soc:name")
  public abstract String getPrettyName();
  public abstract void setPrettyName(String prettyName);
  public static final PropertyLiteralExpression name = new PropertyLiteralExpression(String.class, "soc:displayName");

  @Property(name = "soc:displayName")
  public abstract String getDisplayName();
  public abstract void setDisplayName(String displayName);
  public static final PropertyLiteralExpression displayName = new PropertyLiteralExpression(String.class, "soc:displayName");

  @Property(name = "soc:registration")
  public abstract String getRegistration();
  public abstract void setRegistration(String registration);

  @Property(name = "soc:description")
  public abstract String getDescription();
  public abstract void setDescription(String description);
  public static final PropertyLiteralExpression description = new PropertyLiteralExpression(String.class, "soc:description");

  @Property(name = "soc:type")
  public abstract String getType();
  public abstract void setType(String type);

  @Property(name = "soc:visibility")
  public abstract String getVisibility();
  public abstract void setVisibility(String visibility);

  @Property(name = "soc:priority")
  public abstract String getPriority();
  public abstract void setPriority(String priority);

  @Property(name = "soc:groupId")
  public abstract String getGroupId();
  public abstract void setGroupId(String groupId);
  public static final PropertyLiteralExpression groupId = new PropertyLiteralExpression(String.class, "soc:groupId");

  @Property(name = "soc:url")
  public abstract String getURL();
  public abstract void setURL(String url);

  @Property(name = "soc:membersId")
  public abstract List<String> getMembersId();
  public abstract void setMembersId(List<String> membersId);
  public static final PropertyLiteralExpression membersId = new PropertyLiteralExpression(String.class, "soc:membersId");

  @Property(name = "soc:pendingMembersId")
  public abstract List<String> getPendingMembersId();
  public abstract void setPendingMembersId(List<String> pendingMembersId);
  public static final PropertyLiteralExpression pendingMembersId = new PropertyLiteralExpression(String.class, "soc:pendingMembersId");

  @Property(name = "soc:invitedMembersId")
  public abstract List<String> getInvitedMembersId();
  public abstract void setInvitedMembersId(List<String> invitedMembersId);
  public static final PropertyLiteralExpression invitedMembersId = new PropertyLiteralExpression(String.class, "soc:invitedMembersId");

  @Property(name = "soc:managerMembersId")
  public abstract List<String> getManagerMembersId();
  public abstract void setManagerMembersId(List<String> managerMembersId);
  public static final PropertyLiteralExpression managerMembersId = new PropertyLiteralExpression(String.class, "soc:managerMembersId");

  public List<String> safeGetMembersId() {

    List<String> ids = getMembersId();

    if (ids == null) {
      return new ArrayList<String>();
    }

    return ids;
  }

  public List<String> safeGetManagerMembersId() {

    List<String> ids = getManagerMembersId();

    if (ids == null) {
      return new ArrayList<String>();
    }

    return ids;
  }

  public List<String> safeGetPendingMembersId() {

    List<String> ids = getPendingMembersId();

    if (ids == null) {
      return new ArrayList<String>();
    }

    return ids;
  }

  public List<String> safeGetInvitedMembersId() {

    List<String> ids = getInvitedMembersId();

    if (ids == null) {
      return new ArrayList<String>();
    }

    return ids;
  }

}
