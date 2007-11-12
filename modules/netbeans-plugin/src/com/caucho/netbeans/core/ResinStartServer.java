/*
 * Copyright (c) 1998-2007 Caucho Technology -- all rights reserved
 *
 * This file is part of Resin(R) Open Source
 *
 * Each copy or derived work must preserve the copyright notice and this
 * notice unmodified.
 *
 * Resin Open Source is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * Resin Open Source is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE, or any warranty
 * of NON-INFRINGEMENT.  See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Resin Open Source; if not, write to the
 *
 *   Free Software Foundation, Inc.
 *   59 Temple Place, Suite 330
 *   Boston, MA 02111-1307  USA
 *
 * @author Sam
 */


package com.caucho.netbeans.core;

import com.caucho.netbeans.PluginL10N;
import com.caucho.netbeans.PluginLogger;
import com.caucho.netbeans.util.ProgressEventSupport;
import com.caucho.netbeans.util.ResinProperties;
import com.caucho.netbeans.util.Status;

import org.netbeans.modules.j2ee.deployment.plugins.api.ServerDebugInfo;
import org.netbeans.modules.j2ee.deployment.plugins.api.StartServer;
import org.openide.util.RequestProcessor;

import javax.enterprise.deploy.shared.CommandType;
import javax.enterprise.deploy.shared.StateType;
import javax.enterprise.deploy.spi.DeploymentManager;
import javax.enterprise.deploy.spi.Target;
import javax.enterprise.deploy.spi.TargetModuleID;
import javax.enterprise.deploy.spi.exceptions.OperationUnsupportedException;
import javax.enterprise.deploy.spi.status.ClientConfiguration;
import javax.enterprise.deploy.spi.status.DeploymentStatus;
import javax.enterprise.deploy.spi.status.ProgressListener;
import javax.enterprise.deploy.spi.status.ProgressObject;
import java.util.logging.Level;

public final class ResinStartServer
  extends StartServer
{
  private static final PluginL10N L = new PluginL10N(ResinStartServer.class);
  private static final PluginLogger log = new PluginLogger(ResinStartServer.class);

  private enum Mode { RUN, DEBUG }

  private final ResinDeploymentManager _manager;

  public ResinStartServer(DeploymentManager manager)
  {
    _manager = (ResinDeploymentManager) manager;
  }

  public boolean isAlsoTargetServer(Target target)
  {
    return true;
  }

  public boolean supportsStartDeploymentManager()
  {
    return true;
  }

  public boolean supportsStartDebugging(Target target)
  {
    return true;
  }

  public ProgressObject startDeploymentManager()
  {
    StartRunnable start = new StartRunnable(Mode.RUN, CommandType.START);
    RequestProcessor.getDefault().post(start);
    return start;
  }

  public ProgressObject stopDeploymentManager()
  {
    StartRunnable start = new StartRunnable(Mode.RUN, CommandType.STOP);
    RequestProcessor.getDefault().post(start);
    return start;
  }

  public boolean needsStartForConfigure()
  {
    return false;
  }

  public boolean needsStartForTargetList()
  {
    return true;
  }

  public boolean needsStartForAdminConfig()
  {
    return false;
  }

  public boolean isRunning()
  {
    return _manager.getResinProcess().isActive()
           && _manager.getResinProcess().isResponding();
  }

  public boolean isDebuggable(Target target)
  {
    return _manager.getResinProcess().isActive()
           && _manager.getResinProcess().isDebug();
  }

  public ProgressObject startDebugging(Target target)
  {
    StartRunnable start = new StartRunnable(Mode.DEBUG, CommandType.START);
    RequestProcessor.getDefault().post(start);

    return start;
  }

  public ServerDebugInfo getDebugInfo(Target target)
  {
    ResinProperties props = _manager.getProperties();

    return new ServerDebugInfo(props.getHost(), props.getDebugPort());
  }

  private class StartRunnable
    implements Runnable, ProgressObject
  {
    private final Mode _mode;
    private final CommandType _command;
    private final ProgressEventSupport _eventSupport;
    private Process _process;

    public StartRunnable(Mode mode, CommandType command)
    {
      _mode = mode;
      _command = command;
      _eventSupport = new ProgressEventSupport(this);

      // must be in constructor to stop netbeans nullpointer
      if (_command == CommandType.START)
        fireProgressEvent(StateType.RUNNING, L.l("Waiting for Resin to start..."));
      else if (_command == CommandType.STOP)
        fireProgressEvent(StateType.RUNNING, L.l("Waiting for Resin to stop..."));
      else
        throw new AssertionError(_command.toString());
    }

    private void fireProgressEvent(StateType state, String msg)
    {
      log.log(state == StateType.FAILED ? Level.WARNING : Level.INFO, msg);

      Status status = new Status(_command, msg, state);

      _eventSupport.fireProgressEvent(null, status);
    }

    public void run()
    {
      if (_command == CommandType.START) {
        ResinProcess resinProcess = _manager.getResinProcess();

        try {
          if (_mode == Mode.DEBUG)
            resinProcess.startDebug();
          else
            resinProcess.start();

          fireProgressEvent(StateType.COMPLETED, L.l("Resin start completed"));
        }
        catch (IllegalStateException ex) {
          fireProgressEvent(StateType.FAILED, ex.getLocalizedMessage());
          log.log(Level.FINE, ex);
        }
        catch (Exception ex) {
          fireProgressEvent(StateType.FAILED, ex.toString());
          log.log(Level.WARNING, ex);
        }
      }
      else if (_command == CommandType.STOP) {
        ResinProcess resinProcess = _manager.getResinProcess();

        if (! resinProcess.isActive()) {
          fireProgressEvent(StateType.COMPLETED,
                            L.l("Resin stop completed, but there was no process to stop."));
        }
        else {
          try {
            resinProcess.stop();
            fireProgressEvent(StateType.COMPLETED, L.l("Resin stop completed."));
          }
          catch (Exception ex) {
            log.log(Level.WARNING, ex);

            fireProgressEvent(StateType.COMPLETED,
                              L.l("Error stopping Resin: {0}", ex.toString()));
          }
        }
      }
      else
        throw new AssertionError(_command.toString());
    }


    public DeploymentStatus getDeploymentStatus()
    {
      return _eventSupport.getDeploymentStatus();
    }

    public TargetModuleID[] getResultTargetModuleIDs()
    {
      return null;
    }

    public ClientConfiguration getClientConfiguration(TargetModuleID targetModuleID)
    {
      return null;
    }

    public boolean isCancelSupported()
    {
      return false;
    }

    public void cancel()
      throws OperationUnsupportedException
    {
      throw new OperationUnsupportedException("Cancel is not supported");
    }

    public boolean isStopSupported()
    {
      return false;
    }

    public void stop()
      throws OperationUnsupportedException
    {
      throw new OperationUnsupportedException("Stop is not supported");
    }

    public void addProgressListener(ProgressListener progressListener)
    {
      _eventSupport.addProgressListener(progressListener);
    }

    public void removeProgressListener(ProgressListener progressListener)
    {
      _eventSupport.removeProgressListener(progressListener);
    }
  }

}
