/*
* Copyright (c) 1998-2006 Caucho Technology -- all rights reserved
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
* @author Scott Ferguson
*/

package javax.xml.ws;
import javax.xml.ws.handler.*;
import java.util.*;

/**
 * The Binding interface is the base interface for JAX-WS protocol bindings.
 * Since: JAX-WS 2.0
 */
public interface Binding {

  /**
   * Gets a copy of the handler chain for a protocol binding instance. If the
   * returned chain is modified a call to setHandlerChain is required to
   * configure the binding instance with the new chain.
   */
  abstract List<Handler> getHandlerChain();


  /**
   * Sets the handler chain for the protocol binding instance.
   */
  abstract void setHandlerChain(List<Handler> handler);

}

