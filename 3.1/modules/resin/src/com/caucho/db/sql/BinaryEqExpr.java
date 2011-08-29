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

package com.caucho.db.sql;

import com.caucho.log.Log;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.logging.Logger;

class BinaryEqExpr extends Expr {
  private static final Logger log = Log.open(BinaryEqExpr.class);

  private ColumnExpr _column;
  private byte []_matchBuffer;

  BinaryEqExpr(ColumnExpr left, StringExpr right)
  {
    _column = left;

    try {
      _matchBuffer = right.getValue().getBytes("UTF8");
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  protected Expr bind(Query query)
    throws SQLException
  {
    throw new UnsupportedOperationException();
  }

  /**
   * Returns the type of the expression.
   */
  public Class getType()
  {
    return boolean.class;
  }

  /**
   * Returns the cost based on the given FromList.
   */
  public long subCost(ArrayList<FromItem> fromList)
  {
    return _column.subCost(fromList);
  }

  /**
   * Evaluates the expression as a boolean.
   */
  public int evalBoolean(QueryContext context)
    throws SQLException
  {
    if (_column.isNull(context))
      return UNKNOWN;
    else
      return _column.evalEqual(context, _matchBuffer) ? TRUE : FALSE;
  }

  public String evalString(QueryContext context)
    throws SQLException
  {
    throw new SQLException("can't convert string to boolean");
  }

  public String toString()
  {
    return "(" + _column + " = byte[])";
  }
}
