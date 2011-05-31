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

package org.exoplatform.social.common.lifecycle;

import org.exoplatform.container.xml.InitParams;
import org.exoplatform.container.xml.ValueParam;

import java.util.concurrent.Callable;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * @author <a href="mailto:alain.defrance@exoplatform.com">Alain Defrance</a>
 * @version $Revision$
 */
public class LifeCycleCompletionService {

  /* . */
  private Executor executor;

  /* . */
  private ExecutorCompletionService ecs;

  /* . */
  private final int DEFAULT_THREAD_NUMBER = 1;

  /* . */
  private final boolean DEFAULT_ASYNC_EXECUTION = true;

  /* . */
  private int configThreadNumber;

  /* . */
  private boolean configAsyncExecution;

  public LifeCycleCompletionService(InitParams params) {

    //
    ValueParam threadNumber = params.getValueParam("thread-number");
    ValueParam asyncExecution = params.getValueParam("async-execution");

    //
    try {
      this.configThreadNumber = Integer.valueOf(threadNumber.getValue());
    }
    catch (Exception e) {
      this.configThreadNumber = DEFAULT_THREAD_NUMBER;
    }

    //
    try {
      this.configAsyncExecution = Boolean.valueOf(asyncExecution.getValue());
    }
    catch (Exception e) {
      this.configAsyncExecution = DEFAULT_ASYNC_EXECUTION;
    }


    //
    if (configAsyncExecution) {
      this.executor = Executors.newFixedThreadPool(this.configThreadNumber);
    }
    else {
      this.executor = new DirectExecutor();
    }

    //
    this.ecs = new ExecutorCompletionService(executor);

  }

  public void addTask(Callable callable) {
    ecs.submit(callable);
  }

  public void waitCompletionFinished() {
    try {
      if (executor instanceof ExecutorService) {
        ((ExecutorService) executor).awaitTermination(1, TimeUnit.SECONDS);
      }
    }
    catch (InterruptedException e) {
      throw new RuntimeException(e);
    }
  }

  private class DirectExecutor implements Executor {

    public void execute(final Runnable runnable) {
      if (Thread.interrupted()) throw new RuntimeException();

      runnable.run();
    }
  }
}
