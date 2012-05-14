/*
 * Copyright 2009 SIB Visions GmbH
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
import java.util.HashSet;
import java.util.Set;

import javax.persistence.metamodel.Attribute;
import javax.rad.model.datatype.BigDecimalDataType;
import javax.rad.model.datatype.BinaryDataType;
import javax.rad.model.datatype.BooleanDataType;
import javax.rad.model.datatype.IDataType;
import javax.rad.model.datatype.ObjectDataType;
import javax.rad.model.datatype.StringDataType;
import javax.rad.model.datatype.TimestampDataType;

public class JPAStorageUtil {
	
	//TODO Check org.apache.commons.lang.ClassUtils: ClassUtils.wrapperToPrimitive(keyClass)
	private static final Set<Class> WRAPPER_TYPES = new HashSet();	
	
	static {
		
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
	
	public static IDataType getDataTypeIdentifierForJavaType(Class pJavaType) {
		
		if(pJavaType.isArray()) {
			
			if(pJavaType == java.lang.Byte[].class || pJavaType == byte[].class) {
				return new BinaryDataType();			
			} else if(pJavaType == java.lang.Character[].class || pJavaType == char[].class) {
				return new BinaryDataType();	
			}
			
		}
		
		if(pJavaType == java.lang.String.class || 
		   pJavaType == java.lang.Character.class || pJavaType == char.class) {

			return new StringDataType();
			
		} else if(pJavaType == java.lang.Byte.class || pJavaType == byte.class ||
				  pJavaType == java.lang.Short.class || pJavaType == short.class ||
				  pJavaType == java.lang.Integer.class || pJavaType == int.class ||
				  pJavaType == java.lang.Long.class || pJavaType == long.class ||
				  pJavaType == java.lang.Float.class || pJavaType == float.class ||
				  pJavaType == java.lang.Double.class || pJavaType == double.class ||
				  pJavaType == java.math.BigDecimal.class) {
			
			return new BigDecimalDataType();
			
		} if(pJavaType == java.lang.Boolean.class || pJavaType == boolean.class) {
			
			return new BooleanDataType();
		
		} else if(pJavaType == java.util.Date.class || pJavaType == java.sql.Date.class) {

			return new TimestampDataType();
			
		} else if(pJavaType == java.lang.Object.class) {
			
			return new ObjectDataType();
			
		}
		
		return new ObjectDataType();
	}
		
	public static boolean isPrimaryKeyAttribute(Attribute pAttribute, Class pEntityClass) throws Exception {

		Annotation[] annotations = getAnnotationsForAttribute(pAttribute, pEntityClass);
		
		for(Annotation annotation : annotations) {
			
			if(annotation.annotationType().getName().equals("javax.persistence.Id") || annotation.annotationType().getName().equals("javax.persistence.EmbeddedId")) {
				return true;				
			}

		}

		return false;
	}
	
	public static boolean isPrimitiveOrWrapped(Class clazz) {
		return clazz.isPrimitive() || WRAPPER_TYPES.contains(clazz);		
	}	
	
	public static String getSetterMethodNameForAttribute(Attribute attribute) {
		return "set"+attribute.getName().substring(0,1).toUpperCase()+attribute.getName().substring(1);
	}
	
	public static String getGetterMethodNameForAttribute(Attribute attribute) {
		
		String methodName = "get"+attribute.getName().substring(0,1).toUpperCase()+attribute.getName().substring(1);
		
		if(attribute.getJavaType() == Boolean.class || attribute.getJavaType() == boolean.class) { 

			try {
				
				attribute.getDeclaringType().getJavaType().getMethod(methodName);
				
			} catch (NoSuchMethodException e) {
				methodName = "is"+attribute.getName().substring(0,1).toUpperCase()+attribute.getName().substring(1);
			} 
			
		}
		
		return methodName;
	}	
	
	public static String getNameForAttribute(Attribute attribute) {
		return attribute.getName().toUpperCase();
	}

	public static String getLabelForAttribute(Attribute attribute) {
		return attribute.getName().substring(0,1).toUpperCase()+attribute.getName().substring(1);
	}	
	
	public static Annotation[] getAnnotationsForAttribute(Attribute attribute, Class entityClass) throws Exception {
		
		String member = attribute.getJavaMember().getName();
		
		if(member.startsWith("get") || member.startsWith("is")) { // The Annotation is defined on the getter-Methode
			return entityClass.getMethod(member).getAnnotations();
		}
		
		return entityClass.getDeclaredField(member).getAnnotations(); // The Annotation is defined on the Field
	}
	
	public static Object getDefaultValueForAttribute(Attribute attribute, Class entityClass) throws Exception {
		
//		String member = attribute.getName();
//		
//		Field privateField = entityClass.getDeclaredField(member);
//		privateField.setAccessible(true);
		
		return entityClass.getMethod(getGetterMethodNameForAttribute(attribute)).invoke(entityClass.newInstance());
	}

	public static Class getTypeClassForAttribute(Attribute attribute) throws Exception {

		Field stringListField = attribute.getDeclaringType().getJavaType().getDeclaredField(attribute.getName());
		
		ParameterizedType stringListType = (ParameterizedType) stringListField.getGenericType();
		
		Class<?> typeClass = (Class<?>) stringListType.getActualTypeArguments()[0];
		
		return typeClass;	

	}

}
