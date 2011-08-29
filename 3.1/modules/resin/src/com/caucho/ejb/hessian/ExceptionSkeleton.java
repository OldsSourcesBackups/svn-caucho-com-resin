/*
 * Copyright (c) 1998-2008 Caucho Technology -- all rights reserved
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
 *   Free SoftwareFoundation, Inc.
 *   59 Temple Place, Suite 330
 *   Boston, MA 02111-1307  USA
 *
 * @author Scott Ferguson
 */

package com.caucho.ejb.hessian;

import com.caucho.hessian.io.HessianInput;
import com.caucho.hessian.io.HessianOutput;
import com.caucho.ejb.protocol.Skeleton;
import com.caucho.log.Log;

import javax.jms.JMSException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Base class for any bean skeleton capable of handling an HESSIAN-RPC request.
 *
 * <p/>Once selected, the calling servlet will dispatch the request through
 * the <code>_service</code> call.  After parsing the request headers,
 * <code>_service</code> calls the generated entry <code>_execute</code>
 * to execute the request.
 */
public class ExceptionSkeleton extends Skeleton {
  protected static Logger log = Log.open(ExceptionSkeleton.class);

  ExceptionSkeleton()
    throws JMSException
  {
  }

  /**
   * Services the request.
   *
   * @param rawIs the raw input stream from the servlet request
   * @param rawOs the raw output stream to the servlet response
   */
  public void _service(InputStream rawIs, OutputStream rawOs)
    throws Exception
  {
    throw new UnsupportedOperationException();
  }

  /**
   * Services the request.
   *
   * @param rawIs the raw input stream from the servlet request
   * @param rawOs the raw output stream to the servlet response
   */
  public void _service(InputStream rawIs, OutputStream rawOs,
		       Throwable e)
    throws Exception
  {
    HessianInput in = new HessianInput(rawIs);
    HessianOutput out = new HessianWriter(rawOs);

    try {
      in.startCall();
      
      String method = in.getMethod();

      in.completeCall();

      out.startReply();
      out.writeFault("SystemFault", String.valueOf(e), e);
      out.completeReply();
    } catch (Exception e1) {
      log.log(Level.WARNING, e1.toString(), e1);

      out.startReply();
      out.writeFault("SystemFault", String.valueOf(e1), e1);
      out.completeReply();
    }
  }
}


