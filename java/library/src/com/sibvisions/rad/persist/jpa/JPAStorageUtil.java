/*
 * Copyright 2012 SIB Visions GmbH
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
 * 09.05.2012 - [SW] - creation
 */
package com.sibvisions.rad.persist.jpa;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.EmbeddedId;
import javax.persistence.Id;
import javax.persistence.metamodel.Attribute;
import javax.rad.model.datatype.BigDecimalDataType;
import javax.rad.model.datatype.BinaryDataType;
import javax.rad.model.datatype.BooleanDataType;
import javax.rad.model.datatype.IDataType;
import javax.rad.model.datatype.ObjectDataType;
import javax.rad.model.datatype.StringDataType;
import javax.rad.model.datatype.TimestampDataType;

import com.sibvisions.util.type.StringUtil;

/**
 * The {@link JPAStorageUtil} provides various methods for working with the
 * {@link JPAStorage}.
 * 
 * @author Stefan Wurm
 * 		
 */
public final class JPAStorageUtil
{
	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	// Class members
	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	
	/**
	 * The {@link Set} of {@link Class}es that represent wrappers of primitive
	 * types.
	 **/
	private static final Set<Class> WRAPPER_TYPES = new HashSet();
	
	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	// Initialization
	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	
	static
	{
		WRAPPER_TYPES.add(Boolean.class);
		WRAPPER_TYPES.add(Character.class);
		WRAPPER_TYPES.add(Byte.class);
		WRAPPER_TYPES.add(Short.class);
		WRAPPER_TYPES.add(Integer.class);
		WRAPPER_TYPES.add(Long.class);
		WRAPPER_TYPES.add(Float.class);
		WRAPPER_TYPES.add(Double.class);
		WRAPPER_TYPES.add(String.class);
	}
	
	/**
	 * No instance needed.
	 */
	private JPAStorageUtil()
	{
		// No instance needed.
	}
	
	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	// User-defined methods
	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	
	/**
	 * Returns all {@link Annotation}s for the given {@link Attribute}.
	 * 
	 * @param pAttribute the {@link Attribute} from which to get the
	 *            {@link Annotation}s.
	 * @return the {@link Annotation}s of the given {@link Attribute}.
	 *         {@code null} if the {@link Annotation}s could not be got.
	 */
	public static Annotation[] getAnnotationsForAttribute(Attribute pAttribute)
	{
		Member member = pAttribute.getJavaMember();
		
		if (member instanceof Field)
		{
			return ((Field)member).getAnnotations();
		}
		else if (member instanceof Method)
		{
			return ((Method)member).getAnnotations();
		}
		
		return null;
	}
	
	/**
	 * Returns the {@link IDataType} for the given {@link Class java type}.
	 * 
	 * @param pJavaType the {@link Class java type}.
	 * @return the {@link IDataType}.
	 */
	public static IDataType getDataTypeIdentifierForJavaType(Class pJavaType)
	{
		if (pJavaType.isArray())
		{
			if (pJavaType == Byte[].class || pJavaType == byte[].class)
			{
				return new BinaryDataType();
			}
			else if (pJavaType == Character[].class || pJavaType == char[].class)
			{
				return new BinaryDataType();
			}
		}
		
		if (pJavaType == String.class
				|| pJavaType == Character.class
				|| pJavaType == char.class)
		{
			return new StringDataType();
		}
		else if (Number.class.isAssignableFrom(pJavaType)
				|| pJavaType == byte.class
				|| pJavaType == short.class
				|| pJavaType == int.class
				|| pJavaType == long.class
				|| pJavaType == float.class
				|| pJavaType == double.class)
		{
			return new BigDecimalDataType();
			
		}
		
		if (pJavaType == Boolean.class || pJavaType == boolean.class)
		{
			return new BooleanDataType();
		}
		else if (Date.class.isAssignableFrom(pJavaType))
		{
			return new TimestampDataType();
		}
		
		return new ObjectDataType();
	}
	
	/**
	 * Returns the default-value for the attribute.
	 * 
	 * String country = "AT"
	 * 
	 * "AT" is the default-value
	 * 
	 * @param pAttribute the attribute
	 * @param pEntityClass the class of the entity
	 * @return the default-value
	 * @throws Exception if getting the default value failed.
	 */
	public static Object getDefaultValueForAttribute(Attribute pAttribute, Class pEntityClass) throws Exception
	{
		return pEntityClass.getMethod(getGetterMethodNameForAttribute(pAttribute)).invoke(pEntityClass.newInstance());
	}
	
	/**
	 * Returns the name of the getter-method.
	 * 
	 * @param pAttribute the attribute
	 * @return the name of the getter-method
	 */
	public static String getGetterMethodNameForAttribute(Attribute pAttribute)
	{
		String methodName = StringUtil.formatMethodName("get", pAttribute.getName());
		
		if (pAttribute.getJavaType() == Boolean.class || pAttribute.getJavaType() == boolean.class)
		{
			try
			{
				pAttribute.getDeclaringType().getJavaType().getMethod(methodName);
			}
			catch (NoSuchMethodException e)
			{
				methodName = StringUtil.formatMethodName("is", pAttribute.getName());
			}
		}
		
		return methodName;
	}
	
	/**
	 * Returns the label for the attribute.
	 * 
	 * @param pAttribute the attribute
	 * @return the label of the attribute
	 */
	public static String getLabelForAttribute(Attribute pAttribute)
	{
		return StringUtil.formatInitCap(pAttribute.getName());
	}
	
	/**
	 * Returns the name for the {@link Attribute}.
	 * 
	 * @param pAttribute the {@link Attribute}.
	 * @return the name for the {@link Attribute}.
	 */
	public static String getNameForAttribute(Attribute pAttribute)
	{
		return getNameForAttribute(pAttribute, getAnnotationsForAttribute(pAttribute));
	}
	
	/**
	 * Returns the name of the setter-method.
	 * 
	 * @param pAttribute the attribute
	 * @return the name of the setter-method
	 */
	public static String getSetterMethodNameForAttribute(Attribute pAttribute)
	{
		return StringUtil.formatMethodName("set", pAttribute.getName());
	}
	
	/**
	 * Gets the {@link Class Type-Class} for the given {@link Attribute}.
	 * 
	 * @param pAttribute the {@link Attribute}.
	 * @return the {@link Class Type-Class}.
	 * @throws Exception if getting the type class failed.
	 */
	public static Class getTypeClassForAttribute(Attribute pAttribute) throws Exception
	{
		Field stringListField = pAttribute.getDeclaringType().getJavaType().getDeclaredField(pAttribute.getName());
		
		ParameterizedType stringListType = (ParameterizedType)stringListField.getGenericType();
		
		Class<?> typeClass = (Class<?>)stringListType.getActualTypeArguments()[0];
		
		return typeClass;
	}
	
	/**
	 * Checks if the given {@link Attribute} is part of the primary key of the
	 * given class.
	 * 
	 * @param pAttribute the {@link Attribute}.
	 * @return {@code true} if given {@link Attribute} is part of the primary
	 *         key.
	 */
	public static boolean isPrimaryKeyAttribute(Attribute pAttribute)
	{
		for (Annotation annotation : getAnnotationsForAttribute(pAttribute))
		{
			if (annotation.annotationType() == Id.class || annotation.annotationType() == EmbeddedId.class)
			{
				return true;
			}
		}
		
		return false;
	}
	
	/**
	 * Checks if the given {@link Class} is a primitive or a wrapper.
	 * 
	 * @param pClazz the {@link Class}.
	 * @return {@code true} if the given {@link Class} is a primitive or a wrapper.
	 */
	public static boolean isPrimitiveOrWrapped(Class pClazz)
	{
		return pClazz.isPrimitive() || WRAPPER_TYPES.contains(pClazz);
	}
	
	/**
	 * Returns the name for the {@link Attribute}.
	 * 
	 * @param pAttribute the attribute
	 * @param pAnnotations the attribute annotations
	 * @return the name for the attribute
	 */
	static String getNameForAttribute(Attribute pAttribute, Annotation[] pAnnotations)
	{
		if (pAnnotations != null)
		{
			//try to detect the name from Column annotation
			try
			{
				for (Annotation annotation : pAnnotations)
				{
					if (annotation.annotationType() == javax.persistence.Column.class)
					{
						String sName = (String)annotation.getClass().getMethod("name").invoke(annotation);
						
						if (sName != null && sName.length() > 0)
						{
							return sName;
						}
					}
				}
			}
			catch (Exception e)
			{
				//nothing to be done
			}
		}
		
		return pAttribute.getName().toUpperCase();
	}
	
}	// JPAStorageUtil
