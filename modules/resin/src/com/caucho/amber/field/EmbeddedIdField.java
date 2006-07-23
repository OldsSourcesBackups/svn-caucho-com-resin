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
 *   Free Software Foundation, Inc.
 *   59 Temple Place, Suite 330
 *   Boston, MA 02111-1307  USA
 *
 * @author Scott Ferguson
 */

package com.caucho.amber.field;

import java.lang.reflect.Method;

import java.io.IOException;

import java.util.ArrayList;
import java.util.HashSet;

import java.util.logging.Logger;

import com.caucho.bytecode.JClassWrapper;

import com.caucho.util.L10N;

import com.caucho.log.Log;

import com.caucho.config.ConfigException;

import com.caucho.java.JavaWriter;

import com.caucho.amber.type.Type;
import com.caucho.amber.type.EntityType;

import com.caucho.amber.table.Column;
import com.caucho.amber.table.Table;

import com.caucho.amber.query.AmberExpr;
import com.caucho.amber.query.PathExpr;
import com.caucho.amber.query.KeyColumnExpr;
import com.caucho.amber.query.QueryParser;

/**
 * Configuration for a bean's field
 */
public class EmbeddedIdField extends EntityEmbeddedField implements IdField {
  private static final L10N L = new L10N(EmbeddedIdField.class);
  protected static final Logger log = Log.open(EmbeddedIdField.class);

  boolean _isKeyField;

  public EmbeddedIdField(EntityType tableType)
  {
    super(tableType);
    setEmbeddedId(true);
  }

  public EmbeddedIdField(EntityType tableType,
                         String name)
    throws ConfigException
  {
    super(tableType, name);
    setEmbeddedId(true);
  }

  /**
   * Returns the columns
   */
  public ArrayList<Column> getColumns()
  {
    return null;
  }

  /**
   * Returns type
   */
  public Type getType()
  {
    return null;
  }

  /**
   * Set true if key fields are accessed through fields.
   */
  public void setKeyField(boolean isKeyField)
  {
    _isKeyField = isKeyField;
  }

  /**
   * Returns the component count.
   */
  public int getComponentCount()
  {
    return 1;
  }

  /**
   * Returns the foreign type.
   */
  public String getForeignTypeName()
  {
    return null;
  }

  /**
   * Returns the generator.
   */
  public String getGenerator()
  {
    return null;
  }

  /**
   * Generates the setter for a key property
   */
  public String generateSetKeyProperty(String key, String value)
    throws IOException
  {
    return null;
  }

  /**
   * Generates the getter for a key property
   */
  public String generateGetKeyProperty(String key)
    throws IOException
  {
    return null;
  }

  /**
   * Generates the property getter for an EJB proxy
   *
   * @param value the non-null value
   */
  public String generateGetProxyProperty(String value)
  {
    return null;
  }

  /**
   * Generates any prologue.
   */
  public void generatePrologue(JavaWriter out, HashSet<Object> completedSet)
    throws IOException
  {
    super.generatePrologue(out, completedSet);

    if (isAbstract()) {
      out.println();

      out.println();
      out.println("public " + getJavaTypeName() + " " + getGetterName() + "()");
      out.println("{");
      out.println("  return " + getFieldName() + ";");
      out.println("}");

      out.println();
      out.println("public void " + getSetterName() + "(" + getJavaTypeName() + " v)");
      out.println("{");
      out.println("  " + getFieldName() + " = v;");
      out.println("}");
    }
  }

  /**
   * Generates the set clause.
   */
  public void generateSetGeneratedKeys(JavaWriter out, String pstmt)
    throws IOException
  {
  }

  /**
   * Returns the actual data.
   */
  public String generateSuperGetter()
  {
    if (isAbstract() || getGetterMethod() == null)
      return getFieldName();
    else
      return getGetterMethod().getName() + "()";
  }

  /**
   * Sets the actual data.
   */
  public String generateSuperSetter(String value)
  {
    if (isAbstract() || getGetterMethod() == null || getSetterMethod() == null)
      return(getFieldName() + " = " + value + ";");
    else
      return getSetterMethod().getName() + "(" + value + ")";
  }

  /**
   * Returns the where code
   */
  public String generateMatchArgWhere(String id)
  {
    return generateWhere(id);
  }

  /**
   * Returns the where code
   */
  public String generateRawWhere(String id)
  {
    return id + "." + getName() + "=?";
  }

  /**
   * Generates loading code
   */
  public int generateLoad(JavaWriter out, String rs,
                          String indexVar, int index)
    throws IOException
  {
    return index;
  }

  /**
   * Returns the foreign type.
   */
  public int generateLoadForeign(JavaWriter out, String rs,
                                 String indexVar, int index)
    throws IOException
  {
    return 0;
  }

  /**
   * Generates loading cache
   */
  public void generateLoadFromObject(JavaWriter out, String obj)
    throws IOException
  {
    out.println(generateSuperSetter(generateGet(obj)) + ";");
  }

  /**
   * Generates the select clause.
   */
  public String generateLoadSelect(Table table, String id)
  {
    return null;
  }

  /**
   * Generates loading cache
   */
  public String generateSetNull(String obj)
    throws IOException
  {
    return null;
  }

  /**
   * Returns a test for null.
   */
  public String generateIsNull(String value)
  {
    return  null;
  }

  /**
   * Returns the foreign type.
   */
  public int generateLoadForeign(JavaWriter out, String rs,
                                 String indexVar, int index,
                                 String name)
    throws IOException
  {
    // XXX: 0 == null
    return 0;
  }

  /**
   * Generates the set clause.
   */
  public void generateSet(JavaWriter out, String pstmt,
                          String index, String value)
    throws IOException
  {
    super.generateSet(out, pstmt, index, value);
  }

  /**
   * Generates code for a match.
   */
  public void generateMatch(JavaWriter out, String key)
    throws IOException
  {
  }

  /**
   * Generates code to test the equals.
   */
  public String generateEquals(String left, String right)
  {
    return null;
  }

  /**
   * Generates the set clause.
   */
  public void generateSetInsert(JavaWriter out, String pstmt, String index)
    throws IOException
  {
    generateSet(out, pstmt, index);
  }

  /**
   * Generates the set clause.
   */
  public void generateCheckCreateKey(JavaWriter out)
    throws IOException
  {
  }

  /**
   * Creates the expression for the field.
   */
  public AmberExpr createExpr(QueryParser parser, PathExpr parent)
  {
    return null;
  }

  /**
   * Converts to an object.
   */
  public String toObject(String value)
  {
    return null;
  }

  /**
   * Converts from an object.
   */
  public String toValue(String value)
  {
    return null;
  }
}
