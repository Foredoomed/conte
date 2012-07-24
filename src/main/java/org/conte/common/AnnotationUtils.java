/* 
 *	Copyright 2012 Foredoomed
 *	Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * 	Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License. 
 */

package org.conte.common;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import org.conte.annotation.BelongsTo;
import org.conte.annotation.HasMany;
import org.conte.annotation.HasOne;
import org.conte.annotation.Id;

public final class AnnotationUtils {

	private AnnotationUtils() {

	}

	public static Map<String, Object> getBelongsTo(Object obj) throws Exception {
		Map<String, Object> attrs = new HashMap<String, Object>();
		for (Field field : obj.getClass().getDeclaredFields()) {
			Class<?> fieldType = field.getType();
			String fieldName = field.getName();
			Annotation[] annotations = field.getDeclaredAnnotations();
			if (annotations.length > 0
					&& BelongsTo.class.equals(annotations[0].annotationType())) {
				String foreignKey = ((BelongsTo) annotations[0]).FK();
				attrs.put("column", foreignKey);
				attrs.put("fieldType", fieldType);
				attrs.put("fieldName", fieldName);
				String getterName = BeanUtils.getter(fieldName);
				Method getter = obj.getClass().getMethod(getterName);
				Object target = getter.invoke(obj);
				if (target == null) {
					break;
				}
				Map<String, Object> primaryKey = getPrimaryKey(target);
				attrs.put("value", primaryKey.get("value"));
				break;
			}

		}
		return attrs;
	}

	public static Map<String, Object> getHasOne(Object obj) throws Exception {
		Map<String, Object> attrs = new HashMap<String, Object>();
		for (Field field : obj.getClass().getDeclaredFields()) {
			Class<?> fieldType = field.getType();
			String fieldName = field.getName();
			Annotation[] annotations = field.getDeclaredAnnotations();
			if (annotations.length > 0
					&& HasOne.class.equals(annotations[0].annotationType())) {
				String foreignKey = ((HasOne) annotations[0]).FK();
				attrs.put("column", foreignKey);
				attrs.put("fieldType", fieldType);
				String getterName = BeanUtils.getter(fieldName);
				Method getter = obj.getClass().getMethod(getterName);
				Object target = getter.invoke(obj);
				attrs.put("value", target);
				attrs.put("fieldName", fieldName);
				break;
			}

		}
		return attrs;
	}

	public static Map<String, Object> getHasMany(Object obj) throws Exception {
		Map<String, Object> attrs = new HashMap<String, Object>();
		for (Field field : obj.getClass().getDeclaredFields()) {
			Class<?> fieldType = field.getType();
			String fieldName = field.getName();
			Annotation[] annotations = field.getDeclaredAnnotations();
			if (annotations.length > 0
					&& HasMany.class.equals(annotations[0].annotationType())) {
				String foreignKey = ((HasMany) annotations[0]).FK();
				attrs.put("column", foreignKey);
				attrs.put("fieldType", fieldType);
				String getterName = BeanUtils.getter(fieldName);
				Method getter = obj.getClass().getMethod(getterName);
				Object target = getter.invoke(obj);
				attrs.put("value", target);
				attrs.put("fieldName", fieldName);
				break;
			}

		}
		return attrs;
	}
	

	public static Map<String, Object> getPrimaryKey(Object obj)
			throws Exception {
		Map<String, Object> attrs = new HashMap<String, Object>();
		String pkName = null;
		for (Field field : obj.getClass().getDeclaredFields()) {
			Annotation[] annotations = field.getDeclaredAnnotations();
			if (annotations.length > 0
					&& Id.class.equals(annotations[0].annotationType())) {
				pkName = field.getName();
				break;
			}
		}
		attrs.put("column", pkName);
		String getterName = BeanUtils.getter(pkName);
		Method getter = obj.getClass().getMethod(getterName);
		attrs.put("value", getter.invoke(obj));
		return attrs;
	}

}
