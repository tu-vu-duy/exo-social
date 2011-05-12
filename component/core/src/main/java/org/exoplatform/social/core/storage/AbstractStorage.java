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

import org.chromattic.api.Chromattic;
import org.chromattic.api.ChromatticSession;
import org.exoplatform.commons.chromattic.ChromatticLifeCycle;
import org.exoplatform.commons.chromattic.ChromatticManager;
import org.exoplatform.container.PortalContainer;
import org.exoplatform.social.core.chromattic.entity.ProviderRootEntity;
import org.exoplatform.social.core.chromattic.lifecycle.SocialChromatticLifeCycle;
import org.exoplatform.social.core.storage.exception.NodeNotFoundException;
import org.exoplatform.social.core.storage.query.WhereExpression;

/**
 * @author <a href="mailto:alain.defrance@exoplatform.com">Alain Defrance</a>
 * @version $Revision$
 */
public abstract class AbstractStorage {

  protected final PortalContainer container;
  protected final ChromatticManager manager;
  protected final ChromatticLifeCycle lifeCycle;
  protected final Chromattic chromattic;
  protected final WhereExpression whereExpression;

  //
  protected static final String NS_JCR = "jcr:";

  //
  protected static final String NODETYPE_PROVIDERS = "soc:providers";

  //
  protected static final String ASTERISK_STR = "*";
  protected static final String PERCENT_STR = "%";
  protected static final char   ASTERISK_CHAR = '*';
  protected static final String SPACE_STR = " ";
  protected static final String EMPTY_STR = "";
  protected static final String SLASH_STR = "/";

  protected AbstractStorage() {

    this.container = PortalContainer.getInstance();
    this.manager = (ChromatticManager) container.getComponentInstanceOfType(ChromatticManager.class);
    this.lifeCycle = manager.getLifeCycle("soc");
    this.chromattic = lifeCycle.getChromattic();
    this.whereExpression = new WhereExpression();

  }

  protected ChromatticSession getSession() {
    if (SocialChromatticLifeCycle.getSession() != null) {
      return SocialChromatticLifeCycle.getSession();
    }
    return chromattic.openSession();
  }

  private <T> T getRoot(String nodetypeName, Class<T> t) {
    T got = getSession().findByPath(t, nodetypeName);
    if (got == null) {
      got = getSession().insert(t, nodetypeName);
    }
    return got;
  }

  protected ProviderRootEntity getProviderRoot() {
    return getRoot(NODETYPE_PROVIDERS, ProviderRootEntity.class);
  }



  protected <T> T _findById(final Class<T> clazz, final String nodeId) throws NodeNotFoundException {

    if (nodeId == null) {
      throw new NodeNotFoundException("null id cannot be found");
    }

    //
    T got = getSession().findById(clazz, nodeId);

    //
    if (got == null) {
      throw new NodeNotFoundException(nodeId + " doesn't exists");
    }

    return got;
  }

  protected <T> T _findByPath(final Class<T> clazz, final String nodePath) throws NodeNotFoundException {
    if (nodePath == null) {
      throw new NodeNotFoundException("null nodePath cannot be found");
    }

    //
    T got = getSession().findByPath(clazz, nodePath, true);

    //
    if (got == null) {
      throw new NodeNotFoundException(nodePath + " doesn't exists");
    }

    return got;
  }

  protected boolean isJcrProperty(String name) {
    return !name.startsWith(NS_JCR);
  }
}
