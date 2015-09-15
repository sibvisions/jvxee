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
 * Util Methods for the JPA Integration.
 * 
 * @author Stefan Wurm
 *
 */
public final class JPAStorageUtil
{
	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	// Class members
	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

	/** the primitive type wrappers. **/
	private static final Set<Class>	WRAPPER_TYPES	= new HashSet();

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
	 * Invisible constructor because <code>JPAStorageUtil</code> is a utility class.
	 */
	private JPAStorageUtil()
	{
	}

	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	// User-defined methods
	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	
	/**
	 * Returns all Annoations for the given attribute.
	 * 
	 * @param pAttribute the attribute
	 * @param pEntityClass the entity class
	 * @return the Annotaitons
	 * @throws Exception 
	 */
	public static Annotation[] getAnnotationsForAttribute(Attribute pAttribute, Class pEntityClass) throws Exception
	{
		String member = pAttribute.getJavaMember().getName();
	
		if (member.startsWith("get") || member.startsWith("is"))
		{ 
			// The Annotation is defined on the getter-Methode
			return pEntityClass.getMethod(member).getAnnotations();
		}
	
		// The Annotation is defined on the field 
		return pEntityClass.getDeclaredField(member).getAnnotations(); 
	}
	
	/**
	 * Returns the IDataType for the given Class.
	 *  
	 * @param pJavaType the java Type
	 * @return the IDataType
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
	 * @throws Exception 
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
	 * Returns the name for the attribute.
	 * 
	 * @param pAttribute the attribute
	 * @return the name for the attribute
	 */
	public static String getNameForAttribute(Attribute pAttribute)
	{
		Annotation[] anons;
		
		try
		{
			anons = getAnnotationsForAttribute(pAttribute, pAttribute.getDeclaringType().getJavaType());
		}
		catch (Exception e)
		{
			anons = null;
		}
		
		return getNameForAttribute(pAttribute, anons);
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
	 * The Type-Class for the given attribute.
	 * 
	 * @param pAttribute the attribute
	 * @return the type-Class
	 * @throws Exception 
	 */
	public static Class getTypeClassForAttribute(Attribute pAttribute) throws Exception
	{
		Field stringListField = pAttribute.getDeclaringType().getJavaType().getDeclaredField(pAttribute.getName());

		ParameterizedType stringListType = (ParameterizedType)stringListField.getGenericType();

		Class<?> typeClass = (Class<?>)stringListType.getActualTypeArguments()[0];

		return typeClass;
	}

	/**
	 * Checks if the given Attribute is part of the primary key of the given class.
	 * 
	 * @param pAttribute the Attribute
	 * @param pEntityClass The class of the entity
	 * @return true if given Attribute is part of the primary key
	 * @throws Exception 
	 */
	public static boolean isPrimaryKeyAttribute(Attribute pAttribute, Class pEntityClass) throws Exception
	{
		Annotation[] annotations = getAnnotationsForAttribute(pAttribute, pEntityClass);
	
		for (Annotation annotation : annotations)
		{
			if (annotation.annotationType() == Id.class || annotation.annotationType() == EmbeddedId.class)
			{
				return true;
			}
		}
	
		return false;
	}

	/**
	 * Checks if the given class is a primitive or wrapper.
	 * 
	 * @param pClazz the class
	 * @return  true if the given class is a primitive or wrapper
	 */
	public static boolean isPrimitiveOrWrapped(Class pClazz)
	{
		return pClazz.isPrimitive() || WRAPPER_TYPES.contains(pClazz);
	}

	/**
	 * Returns the name for the attribute.
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
