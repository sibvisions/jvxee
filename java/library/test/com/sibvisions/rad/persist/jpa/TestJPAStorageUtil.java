/*
 * Copyright 2015 SIB Visions GmbH
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 *
 *
 * History
 *
 * 15.09.2015 - [RZ] - creation
 */
package com.sibvisions.rad.persist.jpa;

import java.math.BigDecimal;
import java.util.Date;

import javax.rad.model.datatype.BigDecimalDataType;
import javax.rad.model.datatype.BinaryDataType;
import javax.rad.model.datatype.BooleanDataType;
import javax.rad.model.datatype.ObjectDataType;
import javax.rad.model.datatype.StringDataType;
import javax.rad.model.datatype.TimestampDataType;

import org.junit.Assert;
import org.junit.Test;

/**
 * Tests the {@link JPAStorageUtil}.
 * 
 * @author Robert Zenz
 */
public class TestJPAStorageUtil
{
	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	// Test methods
	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	
	/**
	 * Tests the {@link JPAStorageUtil#getDataTypeIdentifierForJavaType(Class)}
	 * method.
	 */
	@Test
	public void testGetDataTypeIdentifierForJavaType()
	{
		// JPAStorageUtil.getDataTypeIdentifierForJavaType(null);
		
		Assert.assertSame(BooleanDataType.class, JPAStorageUtil.getDataTypeIdentifierForJavaType(boolean.class).getClass());
		Assert.assertSame(BooleanDataType.class, JPAStorageUtil.getDataTypeIdentifierForJavaType(Boolean.class).getClass());
		
		Assert.assertSame(BinaryDataType.class, JPAStorageUtil.getDataTypeIdentifierForJavaType(byte[].class).getClass());
		Assert.assertSame(BinaryDataType.class, JPAStorageUtil.getDataTypeIdentifierForJavaType(Byte[].class).getClass());
		Assert.assertSame(BinaryDataType.class, JPAStorageUtil.getDataTypeIdentifierForJavaType(char[].class).getClass());
		Assert.assertSame(BinaryDataType.class, JPAStorageUtil.getDataTypeIdentifierForJavaType(Character[].class).getClass());
		
		Assert.assertSame(StringDataType.class, JPAStorageUtil.getDataTypeIdentifierForJavaType(char.class).getClass());
		Assert.assertSame(StringDataType.class, JPAStorageUtil.getDataTypeIdentifierForJavaType(Character.class).getClass());
		Assert.assertSame(StringDataType.class, JPAStorageUtil.getDataTypeIdentifierForJavaType(String.class).getClass());
		
		Assert.assertSame(BigDecimalDataType.class, JPAStorageUtil.getDataTypeIdentifierForJavaType(byte.class).getClass());
		Assert.assertSame(BigDecimalDataType.class, JPAStorageUtil.getDataTypeIdentifierForJavaType(short.class).getClass());
		Assert.assertSame(BigDecimalDataType.class, JPAStorageUtil.getDataTypeIdentifierForJavaType(int.class).getClass());
		Assert.assertSame(BigDecimalDataType.class, JPAStorageUtil.getDataTypeIdentifierForJavaType(long.class).getClass());
		Assert.assertSame(BigDecimalDataType.class, JPAStorageUtil.getDataTypeIdentifierForJavaType(float.class).getClass());
		Assert.assertSame(BigDecimalDataType.class, JPAStorageUtil.getDataTypeIdentifierForJavaType(double.class).getClass());
		Assert.assertSame(BigDecimalDataType.class, JPAStorageUtil.getDataTypeIdentifierForJavaType(Short.class).getClass());
		Assert.assertSame(BigDecimalDataType.class, JPAStorageUtil.getDataTypeIdentifierForJavaType(Integer.class).getClass());
		Assert.assertSame(BigDecimalDataType.class, JPAStorageUtil.getDataTypeIdentifierForJavaType(Long.class).getClass());
		Assert.assertSame(BigDecimalDataType.class, JPAStorageUtil.getDataTypeIdentifierForJavaType(Float.class).getClass());
		Assert.assertSame(BigDecimalDataType.class, JPAStorageUtil.getDataTypeIdentifierForJavaType(Double.class).getClass());
		Assert.assertSame(BigDecimalDataType.class, JPAStorageUtil.getDataTypeIdentifierForJavaType(BigDecimal.class).getClass());
		
		Assert.assertSame(TimestampDataType.class, JPAStorageUtil.getDataTypeIdentifierForJavaType(Date.class).getClass());
		
		Assert.assertSame(ObjectDataType.class, JPAStorageUtil.getDataTypeIdentifierForJavaType(Object.class).getClass());
		Assert.assertSame(ObjectDataType.class, JPAStorageUtil.getDataTypeIdentifierForJavaType(Object[].class).getClass());
	}
	
	/**
	 * Tests the {@link JPAStorageUtil#isPrimitiveOrWrapped(Class)} method.
	 */
	@Test
	public void testIsPrimitiveOrWrapped()
	{
		// JPAStorageUtil.isPrimitiveOrWrapped(null);
		
		Assert.assertTrue(JPAStorageUtil.isPrimitiveOrWrapped(boolean.class));
		Assert.assertTrue(JPAStorageUtil.isPrimitiveOrWrapped(int.class));
		Assert.assertTrue(JPAStorageUtil.isPrimitiveOrWrapped(double.class));
		
		Assert.assertTrue(JPAStorageUtil.isPrimitiveOrWrapped(Boolean.class));
		Assert.assertTrue(JPAStorageUtil.isPrimitiveOrWrapped(Integer.class));
		Assert.assertTrue(JPAStorageUtil.isPrimitiveOrWrapped(Double.class));
		
		Assert.assertFalse(JPAStorageUtil.isPrimitiveOrWrapped(Object.class));
		Assert.assertFalse(JPAStorageUtil.isPrimitiveOrWrapped(Object[].class));
		Assert.assertFalse(JPAStorageUtil.isPrimitiveOrWrapped(Date.class));
	}
	
}	// TestJPAStorageUtil
