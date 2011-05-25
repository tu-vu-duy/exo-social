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

package org.exoplatform.social.core.storage;


import org.chromattic.api.query.Query;
import org.chromattic.api.query.QueryBuilder;
import org.chromattic.api.query.QueryResult;
import org.exoplatform.social.core.chromattic.entity.IdentityEntity;
import org.exoplatform.social.core.chromattic.entity.SpaceEntity;
import org.exoplatform.social.core.chromattic.entity.SpaceRef;
import org.exoplatform.social.core.chromattic.entity.SpaceRootEntity;
import org.exoplatform.social.core.space.SpaceFilter;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.storage.exception.NodeNotFoundException;
import org.exoplatform.social.core.storage.query.QueryFunction;
import org.exoplatform.social.core.storage.query.WhereExpression;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * @author <a href="mailto:alain.defrance@exoplatform.com">Alain Defrance</a>
 * @version $Revision$
 */
public class SpaceStorage extends AbstractStorage {

  /*
    Private
   */
  private void _fillSpaceFormEntity (SpaceEntity entity, Space space) {

    space.setApp(entity.getApp());
    space.setId(entity.getId());
    space.setPrettyName(entity.getPrettyName());
    space.setDisplayName(entity.getDisplayName());
    space.setRegistration(entity.getRegistration());
    space.setDescription(entity.getDescription());
    space.setType(entity.getType());
    space.setVisibility(entity.getVisibility());
    space.setPriority(entity.getPriority());
    space.setGroupId(entity.getGroupId());
    space.setUrl(entity.getURL());

    List<String> memberIds = entity.getMembersId();
    List<String> managerIds = entity.getManagerMembersId();
    List<String> pendingIds = entity.getPendingMembersId();
    List<String> invitedIds = entity.getInvitedMembersId();

    if (memberIds != null) {
      space.setMembers(memberIds.toArray(new String[]{}));
    }
    if (managerIds != null) {
      space.setManagers(managerIds.toArray(new String[]{}));
    }
    if (pendingIds != null) {
      space.setPendingUsers(pendingIds.toArray(new String[]{}));
    }
    if (invitedIds != null) {
      space.setInvitedUsers(invitedIds.toArray(new String[]{}));
    }

  }

  private void _fillEntityFormSpace(Space space, SpaceEntity entity) {

    entity.setApp(space.getApp());
    entity.setPrettyName(space.getPrettyName());
    entity.setDisplayName(space.getDisplayName());
    entity.setRegistration(space.getRegistration());
    entity.setDescription(space.getDescription());
    entity.setType(space.getType());
    entity.setVisibility(space.getVisibility());
    entity.setPriority(space.getPriority());
    entity.setGroupId(space.getGroupId());
    entity.setURL(space.getUrl());

  }

  private void _createRefs(SpaceEntity spaceEntity, Space space) throws NodeNotFoundException {
    
    //
    String[] membersIds = space.getMembers();
    String[] managerIds = space.getManagers();
    String[] pendingIds = space.getPendingUsers();
    String[] invitedIds = space.getInvitedUsers();

    List<String> membersIdsToDelete = spaceEntity.getMembersId();
    if (membersIdsToDelete != null) {
      membersIdsToDelete.removeAll(Arrays.asList(membersIds));
      for (String id : membersIdsToDelete) {
        IdentityEntity identityEntity = _findById(IdentityEntity.class, id);
        SpaceRef ref = identityEntity.getSpaces().getRef(spaceEntity.getName());
        identityEntity.getSpaces().getRefs().remove(ref);
      }
      spaceEntity.getMembersId().removeAll(membersIdsToDelete);
    }

    List<String> managerIdsToDelete = spaceEntity.getManagerMembersId();
    if (managerIdsToDelete != null) {
      managerIdsToDelete.removeAll(Arrays.asList(managerIds));
      for (String id : managerIdsToDelete) {
        IdentityEntity identityEntity = _findById(IdentityEntity.class, id);
        SpaceRef ref = identityEntity.getManagerSpaces().getRef(spaceEntity.getName());
        identityEntity.getManagerSpaces().getRefs().remove(ref);
      }
      spaceEntity.getMembersId().removeAll(managerIdsToDelete);
    }

    List<String> pendingIdsToDelete = spaceEntity.getPendingMembersId();
    if (pendingIdsToDelete != null) {
      pendingIdsToDelete.removeAll(Arrays.asList(pendingIds));
      for (String id : pendingIdsToDelete) {
        IdentityEntity identityEntity = _findById(IdentityEntity.class, id);
        SpaceRef ref = identityEntity.getPendingSpaces().getRef(spaceEntity.getName());
        identityEntity.getPendingSpaces().getRefs().remove(ref);
      }
      spaceEntity.getMembersId().removeAll(pendingIdsToDelete);
    }

    List<String> invitedIdsToDelete = spaceEntity.getInvitedMembersId();
    if (invitedIdsToDelete != null) {
      invitedIdsToDelete.removeAll(Arrays.asList(invitedIds));
      for (String id : invitedIdsToDelete) {
        IdentityEntity identityEntity = _findById(IdentityEntity.class, id);
        SpaceRef ref = identityEntity.getInvitedSpaces().getRef(spaceEntity.getName());
        _removeById(SpaceRef.class, ref.getId());
      }
      spaceEntity.getMembersId().removeAll(invitedIdsToDelete);
    }

    if (membersIds != null && membersIds.length > 0) {
      for (String memberId : membersIds) {

        // Spaces references
        IdentityEntity identityEntity = _findById(IdentityEntity.class, memberId);
        String name = spaceEntity.getName();

        // Move ref if pending or invited
        SpaceRef gotRef = identityEntity.getInvitedSpaces().getRefs().get(name);
        if (gotRef == null) {
          gotRef = identityEntity.getPendingSpaces().getRefs().get(name);
        }
        if (gotRef == null) {
          SpaceRef spaceRef = identityEntity.getSpaces().getRefs().get(name);
        }

        if (gotRef != null) {
          // Move ref
          identityEntity.getSpaces().getRefs().put(name, gotRef);
        }
        else {
          // Create ref
          SpaceRef spaceRef = identityEntity.getSpaces().getRef(spaceEntity.getName());
          spaceRef.setSpaceRef(spaceEntity);
        }

      }

      spaceEntity.setMembersId(Arrays.asList(membersIds));

    }
    else {
      spaceEntity.setMembersId(null);
    }

    if (managerIds != null && managerIds.length > 0) {
      for (String managerId : managerIds) {

        // Spaces references
        IdentityEntity identityEntity = _findById(IdentityEntity.class, managerId);
        SpaceRef spaceRef = identityEntity.getManagerSpaces().getRef(spaceEntity.getName());
        spaceRef.setSpaceRef(spaceEntity);

      }

      spaceEntity.setManagerMembersId(Arrays.asList(managerIds));

    }
    else {
      spaceEntity.setManagerMembersId(null);
    }

    if (pendingIds != null && pendingIds.length > 0) {
      for (String pendingId : pendingIds) {

        // Spaces references
        IdentityEntity identityEntity = _findById(IdentityEntity.class, pendingId);
        SpaceRef spaceRef = identityEntity.getPendingSpaces().getRef(spaceEntity.getName());
        spaceRef.setSpaceRef(spaceEntity);

      }

      spaceEntity.setPendingMembersId(Arrays.asList(pendingIds));

    }
    else {
      spaceEntity.setPendingMembersId(null);
    }

    if (invitedIds != null && invitedIds.length > 0) {
      for (String invitedId : invitedIds) {

        // Spaces references
        IdentityEntity identityEntity = _findById(IdentityEntity.class, invitedId);
        SpaceRef spaceRef = identityEntity.getInvitedSpaces().getRef(spaceEntity.getName());
        spaceRef.setSpaceRef(spaceEntity);

      }

      spaceEntity.setInvitedMembersId(Arrays.asList(invitedIds));
      
    }
    else {
      spaceEntity.setInvitedMembersId(null);
    }

  }

  private boolean _validateFilter(SpaceFilter filter) {

    if (filter.getSpaceNameSearchCondition() != null &&
        filter.getSpaceNameSearchCondition().length() != 0) {
      if (isValidInput(filter.getSpaceNameSearchCondition())) {
        return true;
      }
      return false;
    }
    else if (!Character.isDigit(filter.getFirstCharacterOfSpaceName())) {
      return true;
    }
    return false;
  }

  public SpaceEntity _createSpace(Space space) throws SpaceStorageException {
    SpaceRootEntity spaceRootEntity = getSpaceRoot();
    SpaceEntity spaceEntity = spaceRootEntity.getSpace(space.getPrettyName());
    space.setId(spaceEntity.getId());

    return spaceEntity;

  }

  public SpaceEntity _saveSpace(Space space) throws NodeNotFoundException {

    return _findById(SpaceEntity.class, space.getId());

  }

  /*
    Filter query
   */
  
  private Query<SpaceEntity> _getSpacesByFilterQuery(String userId, SpaceFilter spaceFilter) {

    // TODO : JCR query

    QueryBuilder<SpaceEntity> builder = getSession().createQueryBuilder(SpaceEntity.class);
    whereExpression.clear();

    _applyFilter(whereExpression, spaceFilter);

    if (userId != null) {
      whereExpression
          .and()
          .equals(SpaceEntity.membersId, userId);
    }

    if (whereExpression.toString().length() == 0) {
      return builder.get();
    }
    else {
      return builder.where(whereExpression.toString()).get();
    }

  }

  private Query<SpaceEntity> _getAccessibleSpacesByFilterQuery(String userId, SpaceFilter spaceFilter) {

    // TODO : JCR query

    QueryBuilder<SpaceEntity> builder = getSession().createQueryBuilder(SpaceEntity.class);
    whereExpression.clear();

    if (spaceFilter != null) {
      _applyFilter(whereExpression, spaceFilter);
    }

    // TODO : remove this crap

    if (spaceFilter != null && userId != null) {
      whereExpression.and();
      whereExpression.startGroup();
    }
    
    if (userId != null) {
      whereExpression
          .equals(SpaceEntity.membersId, userId)
          .or()
          .equals(SpaceEntity.managerMembersId, userId);
    }

    if (spaceFilter != null && userId != null) {
      whereExpression.endGroup();
    }

    return builder.where(whereExpression.toString()).get();

  }

  private Query<SpaceEntity> _getPublicSpacesQuery(String userId, SpaceFilter spaceFilter) {

    // TODO : JCR query

    QueryBuilder<SpaceEntity> builder = getSession().createQueryBuilder(SpaceEntity.class);
    whereExpression.clear();

    if (spaceFilter != null) {
      _applyFilter(whereExpression, spaceFilter);
      whereExpression.and();
    }

    builder.where(whereExpression
        .not().equals(SpaceEntity.membersId, userId)
        .and().not().equals(SpaceEntity.managerMembersId, userId)
        .and().not().equals(SpaceEntity.invitedMembersId, userId)
        .and().not().equals(SpaceEntity.pendingMembersId, userId)
        .toString()
    );

    return builder.where(whereExpression.toString()).get();

  }

  private Query<SpaceEntity> _getPendingSpacesFilterQuery(String userId, SpaceFilter spaceFilter) {

    // TODO : JCR query

    QueryBuilder<SpaceEntity> builder = getSession().createQueryBuilder(SpaceEntity.class);
    whereExpression.clear();

    if (spaceFilter != null) {
      _applyFilter(whereExpression, spaceFilter);
      whereExpression.and();
    }

    builder.where(whereExpression
        .equals(SpaceEntity.pendingMembersId, userId)
        .toString()
    );

    return builder.where(whereExpression.toString()).get();

  }

  private Query<SpaceEntity> _getInvitedSpacesFilterQuery(String userId, SpaceFilter spaceFilter) {

    // TODO : JCR query

    QueryBuilder<SpaceEntity> builder = getSession().createQueryBuilder(SpaceEntity.class);
    whereExpression.clear();

    if (spaceFilter != null) {
      _applyFilter(whereExpression, spaceFilter);
      whereExpression.and();
    }

    builder.where(whereExpression
        .equals(SpaceEntity.invitedMembersId, userId)
        .toString()
    );

    return builder.where(whereExpression.toString()).get();

  }

  private Query<SpaceEntity> _getEditableSpacesFilterQuery(String userId, SpaceFilter spaceFilter) {

    // TODO : JCR query

    QueryBuilder<SpaceEntity> builder = getSession().createQueryBuilder(SpaceEntity.class);
    whereExpression.clear();

    if (spaceFilter != null) {
      _applyFilter(whereExpression, spaceFilter);
      whereExpression.and();
    }

    builder.where(whereExpression
        .equals(SpaceEntity.managerMembersId, userId)
        .toString()
    );

    return builder.where(whereExpression.toString()).get();

  }

  private void _applyFilter(WhereExpression whereExpression, SpaceFilter spaceFilter) {

    String spaceNameSearchCondition = spaceFilter.getSpaceNameSearchCondition();
    char firstCharacterOfName = spaceFilter.getFirstCharacterOfSpaceName();

    if (spaceNameSearchCondition != null && spaceNameSearchCondition.length() != 0) {
      if (this.isValidInput(spaceNameSearchCondition)) {

        spaceNameSearchCondition = this.processSearchCondition(spaceNameSearchCondition);

        if (spaceNameSearchCondition.indexOf(PERCENT_STR) >= 0) {
          whereExpression.startGroup();
          whereExpression
              .like(SpaceEntity.name, spaceNameSearchCondition)
              .or()
              .like(SpaceEntity.description, spaceNameSearchCondition);
          whereExpression.endGroup();
        }
        else {
          whereExpression.startGroup();
          whereExpression
              .contains(SpaceEntity.name, spaceNameSearchCondition)
              .or()
              .contains(SpaceEntity.description, spaceNameSearchCondition);
          whereExpression.endGroup();
        }
      }
    }
    else if (!Character.isDigit(firstCharacterOfName)) {
      String firstCharacterOfNameString = Character.toString(firstCharacterOfName);
      String firstCharacterOfNameLowerCase = firstCharacterOfNameString.toLowerCase() + PERCENT_STR;
      whereExpression
          .like(whereExpression.callFunction(QueryFunction.LOWER, SpaceEntity.name), firstCharacterOfNameLowerCase);
    }
  }

  private boolean isValidInput(String input) {
    if (input == null) {
      return false;
    }
    String cleanString = input.replaceAll("\\*", "");
    cleanString = cleanString.replaceAll("\\%", "");
    if (cleanString.length() > 0 && Character.isDigit(cleanString.charAt(0))) {
       return false;
    } else if (cleanString.length() == 0) {
      return false;
    }
    return true;
  }

  private String processSearchCondition(String searchCondition) {
    StringBuffer searchConditionBuffer = new StringBuffer();
    if (searchCondition.indexOf(ASTERISK_STR) < 0 && searchCondition.indexOf(PERCENT_STR) < 0) {
      if (searchCondition.charAt(0) != ASTERISK_CHAR) {
        searchConditionBuffer.append(ASTERISK_STR).append(searchCondition);
      }
      if (searchCondition.charAt(searchCondition.length() - 1) != ASTERISK_CHAR) {
        searchConditionBuffer.append(ASTERISK_STR);
      }
    } else {
      searchCondition = searchCondition.replace(ASTERISK_STR, PERCENT_STR);
      searchConditionBuffer.append(PERCENT_STR).append(searchCondition).append(PERCENT_STR);
    }
    return searchConditionBuffer.toString();
  }

  public Space getSpaceByDisplayName(String spaceDisplayName) throws SpaceStorageException {
    Space space = null;

    QueryBuilder<SpaceEntity> builder = getSession().createQueryBuilder(SpaceEntity.class);
    whereExpression.clear();

    whereExpression.equals(SpaceEntity.displayName, spaceDisplayName);

    QueryResult<SpaceEntity> result = builder.where(whereExpression.toString()).get().objects();
    
    if (result.hasNext()) {
      space = new Space();
      _fillSpaceFormEntity(result.next(),  space);
    }

    return space;
  }

  public void saveSpace(Space space, boolean isNew) throws SpaceStorageException {

    SpaceEntity entity;

    try {
      
      if (isNew) {
        entity = _createSpace(space);
      }
      else {
        entity = _saveSpace(space);
      }

      //
      _fillEntityFormSpace(space, entity);
      _createRefs(entity, space);

      //
      getSession().save();

    }
    catch (NodeNotFoundException e) {
      throw new SpaceStorageException(SpaceStorageException.Type.FAILED_TO_SAVE_SPACE, e.getMessage(), e);
    }

  }

  public void deleteSpace(String id) throws SpaceStorageException {

    //
    _removeById(SpaceEntity.class, id);

    //
    getSession().save();
    
  }

  /*
    Member spaces
   */

  public List<Space> getMemberSpaces(String userId, long offset, long limit) throws SpaceStorageException {

    List<Space> spaces = new ArrayList<Space>();

    try {

      int i = 0;
      Collection<SpaceRef> spaceEntities = _findById(IdentityEntity.class, userId).getSpaces().getRefs().values();

      if (spaceEntities != null) {
        for (SpaceRef spaceRef : spaceEntities) {

          Space space = new Space();
          _fillSpaceFormEntity(spaceRef.getSpaceRef(), space);
          spaces.add(space);

          if (++i >= limit) {
            return spaces;
          }
        }
      }

    }
    catch (NodeNotFoundException e) {
      return spaces;
    }

    return spaces;
  }

  public List<Space> getMemberSpaces(String userId) throws SpaceStorageException {

    try {

      IdentityEntity identityEntity = _findById(IdentityEntity.class, userId);

      List<Space> spaces = new ArrayList<Space>();
      for (SpaceRef space : identityEntity.getSpaces().getRefs().values()) {

        Space newSpace = new Space();
        _fillSpaceFormEntity(space.getSpaceRef(), newSpace);
        spaces.add(newSpace);
      }

      return spaces;

    }
    catch (NodeNotFoundException e) {
      throw new SpaceStorageException(SpaceStorageException.Type.FAILED_TO_GET_MEMBER_SPACES, e.getMessage(), e);
    }
  }

  public int getMemberSpacesByFilterCount(String userId, SpaceFilter spaceFilter) {
    return _getSpacesByFilterQuery(userId, spaceFilter).objects().size();
  }

  public int getMemberSpacesCount(String userId) throws SpaceStorageException {
    return 0;
  }

  public List<Space> getMemberSpacesByFilter(String userId, SpaceFilter spaceFilter, long offset, long limit) {

    List<Space> spaces = new ArrayList<Space>();

    //
    QueryResult<SpaceEntity> results = _getSpacesByFilterQuery(userId, spaceFilter).objects(offset, limit);

    while (results.hasNext()) {
      SpaceEntity currentSpace = results.next();
      Space space = new Space();
      _fillSpaceFormEntity(currentSpace, space);
      spaces.add(space);
    }

    return spaces;

  }

  /*
    Pending spaces
   */

  public List<Space> getPendingSpaces(String userId, long offset, long limit) throws SpaceStorageException {

    List<Space> spaces = new ArrayList<Space>();

    try {

      int i = 0;
      Collection<SpaceRef> spaceEntities = _findById(IdentityEntity.class, userId).getPendingSpaces().getRefs().values();

      if (spaceEntities != null) {
        for (SpaceRef spaceRef : spaceEntities) {

          Space space = new Space();
          _fillSpaceFormEntity(spaceRef.getSpaceRef(), space);
          spaces.add(space);

          if (++i >= limit) {
            return spaces;
          }
        }
      }

    }
    catch (NodeNotFoundException e) {
      // TODO : manage
    }

    return spaces;
  }

  public List<Space> getPendingSpaces(String userId) throws SpaceStorageException {

    List<Space> spaces = new ArrayList<Space>();

    try {
      for (SpaceRef ref : _findById(IdentityEntity.class, userId).getPendingSpaces().getRefs().values()) {

        Space space = new Space();
        _fillEntityFormSpace(space, ref.getSpaceRef());
        spaces.add(space);
      }
    }
    catch (NodeNotFoundException e) {
      // TODO : manage
    }

    return spaces;
  }

  public int getPendingSpacesByFilterCount(String userId, SpaceFilter spaceFilter) {
    return _getPendingSpacesFilterQuery(userId, spaceFilter).objects().size();
  }

  public int getPendingSpacesCount(String userId) throws SpaceStorageException {
    try {
      return _findById(IdentityEntity.class, userId).getPendingSpaces().getRefs().size();
    }
    catch (NodeNotFoundException e) {
      return 0;
    }
  }

  public List<Space> getPendingSpacesByFilter(String userId, SpaceFilter spaceFilter, long offset, long limit) {

    List<Space> spaces = new ArrayList<Space>();

    //
    QueryResult<SpaceEntity> results = _getPendingSpacesFilterQuery(userId, spaceFilter).objects(offset, limit);

    while (results.hasNext()) {
      SpaceEntity currentSpace = results.next();
      Space space = new Space();
      _fillSpaceFormEntity(currentSpace, space);
      spaces.add(space);
    }

    return spaces;
  }

  /*
    Invited spaces
   */

  public List<Space> getInvitedSpaces(String userId) throws SpaceStorageException {

    List<Space> spaces = new ArrayList<Space>();

    try {
      for (SpaceRef ref : _findById(IdentityEntity.class, userId).getInvitedSpaces().getRefs().values()) {

        Space space = new Space();
        _fillEntityFormSpace(space, ref.getSpaceRef());
        spaces.add(space);
      }
    }
    catch (NodeNotFoundException e) {
      // TODO : manage
    }

    return spaces;
  }

  public List<Space> getInvitedSpaces(String userId, long offset, long limit) throws SpaceStorageException {
    List<Space> spaces = new ArrayList<Space>();

    try {

      int i = 0;
      Collection<SpaceRef> spaceEntities = _findById(IdentityEntity.class, userId).getInvitedSpaces().getRefs().values();

      if (spaceEntities != null) {
        for (SpaceRef spaceRef : spaceEntities) {

          Space space = new Space();
          _fillSpaceFormEntity(spaceRef.getSpaceRef(), space);
          spaces.add(space);

          if (++i >= limit) {
            return spaces;
          }
        }
      }

    }
    catch (NodeNotFoundException e) {
      // TODO : manage
    }

    return spaces;
  }

  public int getInvitedSpacesCount(String userId) throws SpaceStorageException {

    try {
      return _findById(IdentityEntity.class, userId).getInvitedSpaces().getRefs().size();
    }
    catch (NodeNotFoundException e) {
      return 0;
    }
    
  }

  public int getInvitedSpacesByFilterCount(String userId, SpaceFilter spaceFilter) {
    return _getInvitedSpacesFilterQuery(userId, spaceFilter).objects().size();
  }

  public List<Space> getInvitedSpacesByFilter(String userId, SpaceFilter spaceFilter, long offset, long limit) {

    List<Space> spaces = new ArrayList<Space>();

    //
    QueryResult<SpaceEntity> results = _getInvitedSpacesFilterQuery(userId, spaceFilter).objects(offset, limit);

    while (results.hasNext()) {
      SpaceEntity currentSpace = results.next();
      Space space = new Space();
      _fillSpaceFormEntity(currentSpace, space);
      spaces.add(space);
    }

    return spaces;
  }

  /*
    Public spaces
   */
  
  public List<Space> getPublicSpacesByFilter(String userId, SpaceFilter spaceFilter, long offset, long limit) {

    try {
      _findById(IdentityEntity.class, userId);
    }
    catch (NodeNotFoundException e) {
      userId = null;
    }

    List<Space> spaces = new ArrayList<Space>();

    //
    QueryResult<SpaceEntity> results = _getSpacesByFilterQuery(userId, spaceFilter).objects(offset, limit);

    while (results.hasNext()) {
      SpaceEntity currentSpace = results.next();
      Space space = new Space();
      _fillSpaceFormEntity(currentSpace, space);
      spaces.add(space);
    }

    return spaces;
  }

  public int getPublicSpacesCount(String userId) throws SpaceStorageException {
    return _getPublicSpaces(userId).objects().size();
  }

  public int getPublicSpacesByFilterCount(String userId, SpaceFilter spaceFilter) {
    return _getPublicSpacesQuery(userId, spaceFilter).objects().size();
  }

  public List<Space> getPublicSpaces(String userId) throws SpaceStorageException {
    List<Space> spaces = new ArrayList<Space>();

    //
    QueryResult<SpaceEntity> results = _getPublicSpaces(userId).objects();

    while (results.hasNext()) {
      SpaceEntity currentSpace = results.next();
      Space space = new Space();
      _fillSpaceFormEntity(currentSpace, space);
      spaces.add(space);
    }

    return spaces;
  }

  public List<Space> getPublicSpaces(String userId, long offset, long limit) throws SpaceStorageException {

    List<Space> spaces = new ArrayList<Space>();

    //
    QueryResult<SpaceEntity> results = _getPublicSpaces(userId).objects(offset, limit);

    while (results.hasNext()) {
      SpaceEntity currentSpace = results.next();
      Space space = new Space();
      _fillSpaceFormEntity(currentSpace, space);
      spaces.add(space);
    }

    return spaces;
  }

  private Query<SpaceEntity> _getPublicSpaces(String userId) {
    return _getPublicSpacesQuery(userId, null);
  }

  /*
    Accessible spaces
   */

  public List<Space> getAccessibleSpaces(String userId) throws SpaceStorageException {

    List<Space> spaces = new ArrayList<Space>();

    //
    QueryResult<SpaceEntity> results = _getAccessibleSpacesByFilterQuery(userId, null).objects();

    while (results.hasNext()) {
      SpaceEntity currentSpace = results.next();
      Space space = new Space();
      _fillSpaceFormEntity(currentSpace, space);
      spaces.add(space);
    }

    return spaces;
  }

  public List<Space> getAccessibleSpaces(String userId, long offset, long limit) throws SpaceStorageException {
    List<Space> spaces = new ArrayList<Space>();

    //
    QueryResult<SpaceEntity> results = _getAccessibleSpacesByFilterQuery(userId, null).objects(offset, limit);

    while (results.hasNext()) {
      SpaceEntity currentSpace = results.next();
      Space space = new Space();
      _fillSpaceFormEntity(currentSpace, space);
      spaces.add(space);
    }

    return spaces;
  }

  public int getAccessibleSpacesCount(String userId) throws SpaceStorageException {
    return _getAccessibleSpacesByFilterQuery(userId, null).objects().size();
  }

  public int getAccessibleSpacesByFilterCount(String userId, SpaceFilter spaceFilter) {
    return _getAccessibleSpacesByFilterQuery(userId, spaceFilter).objects().size();
  }

  public List<Space> getAccessibleSpacesByFilter(String userId, SpaceFilter spaceFilter, long offset, long limit) {

    try {
      _findById(IdentityEntity.class, userId);
    }
    catch (NodeNotFoundException e) {
      return new ArrayList<Space>();
    }

    List<Space> spaces = new ArrayList<Space>();

    //
    QueryResult<SpaceEntity> results = _getAccessibleSpacesByFilterQuery(userId, spaceFilter).objects(offset, limit);

    while (results.hasNext()) {
      SpaceEntity currentSpace = results.next();
      Space space = new Space();
      _fillSpaceFormEntity(currentSpace, space);
      spaces.add(space);
    }

    return spaces;
  }

  /*
    Editable spaces    
   */

  public int getEditableSpacesCount(String userId) throws SpaceStorageException {
    try {
      return _findById(IdentityEntity.class, userId).getManagerSpaces().getRefs().size();
    }
    catch (NodeNotFoundException e) {
      return 0;
    }
  }

  public int getEditableSpacesByFilterCount(String userId, SpaceFilter spaceFilter) {
    return _getEditableSpacesFilterQuery(userId, spaceFilter).objects().size();
  }

  public List<Space> getEditableSpacesByFilter(String userId, SpaceFilter spaceFilter, long offset, long limit) {

    try {
      _findById(IdentityEntity.class, userId);
    }
    catch (NodeNotFoundException e) {
      userId = null;
    }

    List<Space> spaces = new ArrayList<Space>();

    //
    QueryResult<SpaceEntity> results = _getEditableSpacesFilterQuery(userId, spaceFilter).objects(offset, limit);

    while (results.hasNext()) {
      SpaceEntity currentSpace = results.next();
      Space space = new Space();
      _fillSpaceFormEntity(currentSpace, space);
      spaces.add(space);
    }

    return spaces;
  }

  public List<Space> getEditableSpaces(String userId, long offset, long limit) throws SpaceStorageException {

    List<Space> spaces = new ArrayList<Space>();

    try {

      int i = 0;
      Collection<SpaceRef> spaceEntities = _findById(IdentityEntity.class, userId).getManagerSpaces().getRefs().values();

      if (spaceEntities != null) {
        for (SpaceRef spaceRef : spaceEntities) {

          Space space = new Space();
          _fillSpaceFormEntity(spaceRef.getSpaceRef(), space);
          spaces.add(space);

          if (++i >= limit) {
            return spaces;
          }
        }
      }

    }
    catch (NodeNotFoundException e) {
      // TODO : manage
    }

    return spaces;
  }

  public List<Space> getEditableSpaces(String userId) throws SpaceStorageException {

    List<Space> spaces = new ArrayList<Space>();

    try {

      Collection<SpaceRef> spaceEntities = _findById(IdentityEntity.class, userId).getManagerSpaces().getRefs().values();

      if (spaceEntities != null) {
        for (SpaceRef spaceRef : spaceEntities) {

          Space space = new Space();
          _fillSpaceFormEntity(spaceRef.getSpaceRef(), space);
          spaces.add(space);
        }
      }

    }
    catch (NodeNotFoundException e) {
      // TODO : manage
    }

    return spaces;

  }

  /*
    All spaces
   */
  
  public int getAllSpacesByFilterCount(SpaceFilter spaceFilter) {

    if (_validateFilter(spaceFilter)) {
      return _getSpacesByFilterQuery(spaceFilter).objects().size();
    }
    else {
      return 0;
    }

  }

  public int getAllSpacesCount() throws SpaceStorageException {

    return getSpaceRoot().getSpaces().size(); // TODO : use property

  }

  public List<Space> getAllSpaces() throws SpaceStorageException {

    List<Space> spaces = new ArrayList<Space>();

    for (SpaceEntity spaceEntity : getSpaceRoot().getSpaces().values()) {
      Space space = new Space();
      _fillSpaceFormEntity(spaceEntity, space);
      spaces.add(space);
    }

    return spaces;

  }


  /*
    Get spaces
   */

  public List<Space> getSpacesByFilter(SpaceFilter spaceFilter, long offset, long limit) {

    List<Space> spaces = new ArrayList<Space>();

    if (!_validateFilter(spaceFilter)) {
      return spaces;
    }

    //
    QueryResult<SpaceEntity> results = _getSpacesByFilterQuery(spaceFilter).objects(offset, limit);

    while (results.hasNext()) {
      SpaceEntity currentSpace = results.next();
      Space space = new Space();
      _fillSpaceFormEntity(currentSpace, space);
      spaces.add(space);
    }

    return spaces;
  }

  public List<Space> getSpaces(long offset, long limit) throws SpaceStorageException {

    // TODO : manage offset

    List<Space> spaces = new ArrayList<Space>();

    int i = 0;
    for (SpaceEntity spaceEntity : getSpaceRoot().getSpaces().values()) {

      Space space = new Space();
      _fillSpaceFormEntity(spaceEntity, space);
      spaces.add(space);

      if (++i >= limit) {
        break;
      }

    }

    return spaces;

  }

  public Space getSpaceById(String id) throws SpaceStorageException {

    try {

      SpaceEntity spaceEntity = _findById(SpaceEntity.class, id);

      Space space = new Space();

      _fillSpaceFormEntity(spaceEntity, space);

      return space;

    }
    catch (NodeNotFoundException e) {
      throw new SpaceStorageException(SpaceStorageException.Type.FAILED_TO_GET_SPACE_BY_ID, e.getMessage(), e);
    }

  }

  public Space getSpaceByPrettyName(String spacePrettyName) throws SpaceStorageException {

    try {

      SpaceEntity entity = _findByPath(SpaceEntity.class, String.format("/production/soc:spaces/soc:%s", spacePrettyName));

      Space space = new Space();
      _fillSpaceFormEntity(entity, space);

      return space;

    }
    catch (NodeNotFoundException e) {
      return null;
    }
  }

  public Space getSpaceByGroupId(String groupId) throws SpaceStorageException {

    // TODO : JCR query

    QueryBuilder<SpaceEntity> builder = getSession().createQueryBuilder(SpaceEntity.class);
    whereExpression.clear();

    builder.where(whereExpression.equals(SpaceEntity.groupId, groupId).toString());

    QueryResult<SpaceEntity> result = builder.get().objects();

    if (result.hasNext()) {
      SpaceEntity entity =  result.next();
      Space space = new Space();

      _fillSpaceFormEntity(entity, space);

      return space;
    }
    else {
      return null;
    }

  }

  public Space getSpaceByUrl(String url) throws SpaceStorageException {
    // TODO : JCR query

    QueryBuilder<SpaceEntity> builder = getSession().createQueryBuilder(SpaceEntity.class);
    builder.where("soc:url = '" + url + "'");

    SpaceEntity entity =  builder.get().objects().next();
    Space space = new Space();

    _fillSpaceFormEntity(entity, space);

    return space;

  }

  private Query<SpaceEntity> _getSpacesByFilterQuery(SpaceFilter spaceFilter) {
    return _getSpacesByFilterQuery(null, spaceFilter);
  }

}