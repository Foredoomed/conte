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
import org.conte.annotation.Id;

public final class AnnotationUtils {

	private AnnotationUtils() {

	}

	public static AnnotationAttributes getBelongsTo(Class<?> clazz) {
		AnnotationAttributes attrs = new AnnotationAttributes();
		for (Field field : clazz.getDeclaredFields()) {
			Class<?> fieldType = field.getType();
			Annotation[] annotations = field.getDeclaredAnnotations();
			for (Annotation ann : annotations) {
				if (BelongsTo.class.equals(ann.annotationType())) {
					attrs.put("column", ((BelongsTo) ann).value());
					attrs.put("fieldType", fieldType);
				}
			}
		}
		return attrs;
	}

	public static Map<String, Object> getPrimarykeyPair(Object obj)
			throws Exception {
		Map<String, Object> pair = new HashMap<String, Object>();
		String pkName = null;
		for (Field field : obj.getClass().getDeclaredFields()) {
			Annotation[] annotation = field.getDeclaredAnnotations();
			if (Id.class.equals(annotation[0].annotationType())) {
				pkName = field.getName();
				break;
			}
		}
		pair.put("name", pkName);
		String getterName = "get" + pkName.substring(0, 1).toUpperCase()
				+ pkName.substring(1);
		Method getter = obj.getClass().getMethod(getterName);
		pair.put("value", getter.invoke(obj));
		return pair;
	}

	public static Object getPrimarykeyValue(Object obj) throws Exception {
		String primaryKey = null;
		for (Field field : obj.getClass().getDeclaredFields()) {
			Annotation[] annotation = field.getDeclaredAnnotations();
			if (Id.class.equals(annotation[0].annotationType())) {
				primaryKey = field.getName();
				break;
			}
		}
		String getterName = "get" + primaryKey.substring(0, 1).toUpperCase()
				+ primaryKey.substring(1);
		Method getter = obj.getClass().getMethod(getterName);
		return getter.invoke(obj);
	}
}
