/*
 * Copyright (C) 2003-2010 eXo Platform SAS.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Affero General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see<http://www.gnu.org/licenses/>.
 */
package org.exoplatform.social.core.manager;

import org.exoplatform.services.cache.CacheService;
import org.exoplatform.services.log.ExoLogger;
import org.exoplatform.services.log.Log;
import org.exoplatform.social.core.storage.api.ActivityStorage;

/**
 * Class CachingActivityManager extends ActivityManagerImpl with caching.
 *
 * @author <a href="mailto:vien_levan@exoplatform.com">vien_levan</a>
 * @since Nov 24, 2010
 * @version 1.2.0-GA
 */
public class CachingActivityManager extends ActivityManagerImpl {
  /** Logger */
  private static final Log LOG = ExoLogger.getLogger(CachingActivityManager.class);

  /**
   * Instantiates a new caching activity manager.
   *
   * @param activityStorage
   * @param identityManager
   * @param cacheService
   */
  public CachingActivityManager(ActivityStorage activityStorage,
                                IdentityManager identityManager,
                                CacheService cacheService) {
    super(activityStorage, identityManager);
  }

}
