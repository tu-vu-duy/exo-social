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
import org.exoplatform.social.core.chromattic.entity.*;
import org.exoplatform.social.core.identity.provider.OrganizationIdentityProvider;
import org.exoplatform.social.core.space.SpaceFilter;
import org.exoplatform.social.core.space.model.Space;
import org.exoplatform.social.core.storage.exception.NodeNotFoundException;
import org.exoplatform.social.core.storage.query.QueryFunction;
import org.exoplatform.social.core.storage.query.WhereExpression;

import java.util.*;

/**
 * @author <a href="mailto:alain.defrance@exoplatform.com">Alain Defrance</a>
 * @version $Revision$
 */
public class SpaceStorage extends AbstractStorage {

   private final IdentityStorage identityStorage;

   public SpaceStorage(IdentityStorage identityStorage) {
     this.identityStorage = identityStorage;
   }

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
    space.setMembers(entity.getMembersId());
    space.setManagers(entity.getManagerMembersId());
    space.setPendingUsers(entity.getPendingMembersId());
    space.setInvitedUsers(entity.getInvitedMembersId());

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
    entity.setMembersId(space.getMembers());
    entity.setManagerMembersId(space.getManagers());
    entity.setPendingMembersId(space.getPendingUsers());
    entity.setInvitedMembersId(space.getInvitedUsers());

  }

  private enum RefType {
    MEMBER() {
      @Override
      public SpaceListEntity refsOf(IdentityEntity identityEntity) {
        return identityEntity.getSpaces();
      }},
    MANAGER() {
      @Override
      public SpaceListEntity refsOf(IdentityEntity identityEntity) {
        return identityEntity.getManagerSpaces();
      }},
    PENDING() {
      @Override
      public SpaceListEntity refsOf(IdentityEntity identityEntity) {
        return identityEntity.getPendingSpaces();
      }},
    INVITED() {
      @Override
      public SpaceListEntity refsOf(IdentityEntity identityEntity) {
        return identityEntity.getInvitedSpaces();
      }};

    public abstract SpaceListEntity refsOf(IdentityEntity identityEntity);
  }

  private class UpdateContext {
    private String[] added;
    private String[] removed;

    private UpdateContext(String[] added, String[] removed) {
      this.added = added;
      this.removed = removed;
    }

    public String[] getAdded() {
      return added;
    }

    public String[] getRemoved() {
      return removed;
    }
  }

  private String[] _sub(String[] l1, String[] l2) {

    if (l1 == null) {
      return new String[]{};
    }

    if (l2 == null) {
      return l1;
    }

    List<String> l = new ArrayList(Arrays.asList(l1));
    l.removeAll(Arrays.asList(l2));
    return l.toArray(new String[]{});
  }

  private void _createRefs(SpaceEntity spaceEntity, Space space) throws NodeNotFoundException {

    String[] removedMembers = _sub(spaceEntity.getMembersId(), space.getMembers());
    String[] removedManagers = _sub(spaceEntity.getManagerMembersId(), space.getManagers());
    String[] removedInvited = _sub(spaceEntity.getInvitedMembersId(), space.getInvitedUsers());
    String[] removedPending = _sub(spaceEntity.getPendingMembersId(), space.getPendingUsers());

    String[] addedMembers = _sub(space.getMembers(), spaceEntity.getMembersId());
    String[] addedManagers = _sub(space.getManagers(), spaceEntity.getManagerMembersId());
    String[] addedInvited = _sub(space.getInvitedUsers(), spaceEntity.getInvitedMembersId());
    String[] addedPending = _sub(space.getPendingUsers(), spaceEntity.getPendingMembersId());

    _manageRefList(new UpdateContext(addedMembers, removedMembers), spaceEntity, RefType.MEMBER);
    _manageRefList(new UpdateContext(addedManagers, removedManagers), spaceEntity, RefType.MANAGER);
    _manageRefList(new UpdateContext(addedInvited, removedInvited), spaceEntity, RefType.INVITED);
    _manageRefList(new UpdateContext(addedPending, removedPending), spaceEntity, RefType.PENDING);

  }

  private void _manageRefList(UpdateContext context, SpaceEntity spaceEntity, RefType type) {

    if (context.getAdded() != null) {
      for (String userName : context.getAdded()) {
        try {
          IdentityEntity identityEntity = identityStorage._findIdentityEntity(OrganizationIdentityProvider.NAME, userName);
          SpaceListEntity listRef = type.refsOf(identityEntity);
          SpaceRef ref = listRef.getRef(spaceEntity.getName());
          ref.setSpaceRef(spaceEntity);
        }
        catch (NodeNotFoundException e) {
          // TODO : manage
        }
      }

      for (String userName : context.getRemoved()) {
        try {
          IdentityEntity identityEntity = identityStorage._findIdentityEntity(OrganizationIdentityProvider.NAME, userName);
          SpaceListEntity listRef = type.refsOf(identityEntity);
          SpaceRef ref = listRef.getRef(spaceEntity.getName());
          getSession().remove(ref);
        }
        catch (NodeNotFoundException e) {
          // TODO : manage
        }
      }
    }
  }

  private boolean _validateFilter(SpaceFilter filter) {

    if (filter == null) {
      return false;
    }

    // TODO : do it better
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

    QueryBuilder<SpaceEntity> builder = getSession().createQueryBuilder(SpaceEntity.class);
    WhereExpression whereExpression = new WhereExpression();

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

    QueryBuilder<SpaceEntity> builder = getSession().createQueryBuilder(SpaceEntity.class);
    WhereExpression whereExpression = new WhereExpression();

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

    QueryBuilder<SpaceEntity> builder = getSession().createQueryBuilder(SpaceEntity.class);
    WhereExpression whereExpression = new WhereExpression();

    if (_validateFilter(spaceFilter)) {
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

    QueryBuilder<SpaceEntity> builder = getSession().createQueryBuilder(SpaceEntity.class);
    WhereExpression whereExpression = new WhereExpression();

    if (_validateFilter(spaceFilter)) {
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

    QueryBuilder<SpaceEntity> builder = getSession().createQueryBuilder(SpaceEntity.class);
    WhereExpression whereExpression = new WhereExpression();

    if (_validateFilter(spaceFilter)) {
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

    QueryBuilder<SpaceEntity> builder = getSession().createQueryBuilder(SpaceEntity.class);
    WhereExpression whereExpression = new WhereExpression();

    if (_validateFilter(spaceFilter)) {
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
    WhereExpression whereExpression = new WhereExpression();

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
      _createRefs(entity, space);
      _fillEntityFormSpace(space, entity);

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

    // TODO : manage offset

    List<Space> spaces = new ArrayList<Space>();

    try {

      int i = 0;
      IdentityEntity identityEntity = identityStorage._findIdentityEntity(OrganizationIdentityProvider.NAME, userId);
      Collection<SpaceRef> spaceEntities = identityEntity.getSpaces().getRefs().values();

      if (spaceEntities != null) {

        Iterator<SpaceRef> it = spaceEntities.iterator();
        _skip(it, offset);

        while (it.hasNext()) {

          SpaceRef spaceRef = it.next();

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

      IdentityEntity identityEntity = identityStorage._findIdentityEntity(OrganizationIdentityProvider.NAME, userId);

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
    try {
       return identityStorage._findIdentityEntity(OrganizationIdentityProvider.NAME, userId).getSpaces().getRefs().size();
    }
    catch (NodeNotFoundException e){
       return 0;
    }
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

      IdentityEntity identityEntity = identityStorage._findIdentityEntity(OrganizationIdentityProvider.NAME, userId);
      Collection<SpaceRef> spaceEntities = identityEntity.getPendingSpaces().getRefs().values();

      if (spaceEntities != null) {

        Iterator<SpaceRef> it = spaceEntities.iterator();
        _skip(it, offset);

        while (it.hasNext()) {

          SpaceRef spaceRef = it.next();

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
      IdentityEntity identityEntity = identityStorage._findIdentityEntity(OrganizationIdentityProvider.NAME, userId);
      Collection<SpaceRef> spaceEntities = identityEntity.getPendingSpaces().getRefs().values();

      for (SpaceRef ref : spaceEntities) {

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
      IdentityEntity identityEntity = identityStorage._findIdentityEntity(OrganizationIdentityProvider.NAME, userId);
      Collection<SpaceRef> spaceEntities = identityEntity.getPendingSpaces().getRefs().values();
      return spaceEntities.size();
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
      IdentityEntity identityEntity = identityStorage._findIdentityEntity(OrganizationIdentityProvider.NAME, userId);
      Collection<SpaceRef> spaceEntities = identityEntity.getInvitedSpaces().getRefs().values();

      for (SpaceRef ref : spaceEntities) {

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
      IdentityEntity identityEntity = identityStorage._findIdentityEntity(OrganizationIdentityProvider.NAME, userId);
      Collection<SpaceRef> spaceEntities = identityEntity.getInvitedSpaces().getRefs().values();

      if (spaceEntities != null) {

        Iterator<SpaceRef> it = spaceEntities.iterator();
        _skip(it, offset);

        while (it.hasNext()) {

          SpaceRef spaceRef = it.next();

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
      IdentityEntity identityEntity = identityStorage._findIdentityEntity(OrganizationIdentityProvider.NAME, userId);
      Collection<SpaceRef> spaceEntities = identityEntity.getInvitedSpaces().getRefs().values();
      return spaceEntities.size();
    }
    catch (NodeNotFoundException e) {
      return 0;
    }
    
  }

  public int getInvitedSpacesByFilterCount(String userId, SpaceFilter spaceFilter) {

    if (_validateFilter(spaceFilter)) {
      return _getInvitedSpacesFilterQuery(userId, spaceFilter).objects().size();
    }
    else {
      return 0;
    }
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
      identityStorage._findIdentityEntity(OrganizationIdentityProvider.NAME, userId);
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
    if (_validateFilter(spaceFilter)) {
      return _getPublicSpacesQuery(userId, spaceFilter).objects().size();
    }
    else {
      return 0;
    }
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
      IdentityEntity identityEntity = identityStorage._findIdentityEntity(OrganizationIdentityProvider.NAME, userId);
      return identityEntity.getManagerSpaces().getRefs().size();
    }
    catch (NodeNotFoundException e) {
      return 0;
    }
  }

  public int getEditableSpacesByFilterCount(String userId, SpaceFilter spaceFilter) {
    return _getEditableSpacesFilterQuery(userId, spaceFilter).objects().size();
  }

  public List<Space> getEditableSpacesByFilter(String userId, SpaceFilter spaceFilter, long offset, long limit) {

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

    // TODO : manage offset
    List<Space> spaces = new ArrayList<Space>();

    try {

      int i = 0;
      IdentityEntity identityEntity = identityStorage._findIdentityEntity(OrganizationIdentityProvider.NAME, userId);
      Collection<SpaceRef> spaceEntities = identityEntity.getManagerSpaces().getRefs().values();

      if (spaceEntities != null) {

        Iterator<SpaceRef> it = spaceEntities.iterator();
        _skip(it, offset);

        while (it.hasNext()) {

          SpaceRef spaceRef = it.next();

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

      IdentityEntity identityEntity = identityStorage._findIdentityEntity(OrganizationIdentityProvider.NAME, userId);
      Collection<SpaceRef> spaceEntities = identityEntity.getManagerSpaces().getRefs().values();

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

    List<Space> spaces = new ArrayList<Space>();

    int i = 0;

    Iterator<SpaceEntity> it = getSpaceRoot().getSpaces().values().iterator();
    _skip(it, offset);
    
    while (it.hasNext()) {

      SpaceEntity spaceEntity = it.next();

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
      return null;
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

    // TODO : avoid JCR query ?

    QueryBuilder<SpaceEntity> builder = getSession().createQueryBuilder(SpaceEntity.class);
    WhereExpression whereExpression = new WhereExpression();

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

    // TODO : avoid JCR query ?

    QueryBuilder<SpaceEntity> builder = getSession().createQueryBuilder(SpaceEntity.class);

    if (url != null) {
      WhereExpression whereExpression = new WhereExpression();
      whereExpression.equals(SpaceEntity.url, url);
      builder.where(whereExpression.toString());
    }

    QueryResult<SpaceEntity> result = builder.get().objects();

    if (result.hasNext()) {

      Space space = new Space();
      SpaceEntity entity =  builder.get().objects().next();

      _fillSpaceFormEntity(entity, space);

      return space;

    }
    else {
      return null;
    }

  }

  private Query<SpaceEntity> _getSpacesByFilterQuery(SpaceFilter spaceFilter) {
    return _getSpacesByFilterQuery(null, spaceFilter);
  }

}