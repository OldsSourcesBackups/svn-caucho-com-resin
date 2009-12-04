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
 *
 *   Free Software Foundation, Inc.
 *   59 Temple Place, Suite 330
 *   Boston, MA 02111-1307  USA
 *
 * @author Rodrigo Westrupp
 */

package com.caucho.amber.cfg;

import javax.persistence.JoinColumn;


/**
 * <join-column> tag in the orm.xml
 */
public class JoinColumnConfig extends AbstractColumnConfig {

  // attributes
  private String _referencedColumnName;
  
  public JoinColumnConfig()
  {
  }
  
  public JoinColumnConfig(JoinColumn joinColumn)
  {
    setName(joinColumn.name());
    setReferencedColumnName(joinColumn.referencedColumnName());
    setUnique(joinColumn.unique());
    setNullable(joinColumn.nullable());
    setInsertable(joinColumn.insertable());
    setUpdatable(joinColumn.updatable());
    setColumnDefinition(joinColumn.columnDefinition());
    setTable(joinColumn.table());
  }

  /**
   * Returns the referenced column name.
   */
  public String getReferencedColumnName()
  {
    return _referencedColumnName;
  }

  /**
   * Sets the referenced column name.
   */
  public void setReferencedColumnName(String referencedColumnName)
  {
    _referencedColumnName = referencedColumnName;
  }
}
