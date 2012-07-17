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

package org.conte.model;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.SQLXML;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ResultHandler {

	public static final Map<Class<?>, Object> primitives = new HashMap<Class<?>, Object>();

	static {
		primitives.put(Byte.class, Byte.valueOf((byte) 0));
		primitives.put(Short.class, Short.valueOf((short) 0));
		primitives.put(Integer.class, Integer.valueOf(0));
		primitives.put(Float.class, Float.valueOf(0f));
		primitives.put(Double.class, new Double(0d));
		primitives.put(Long.class, Long.valueOf(0L));
		primitives.put(Boolean.class, Boolean.FALSE);
		primitives.put(Character.class, Character.valueOf((char) 0));
	}

	public <T extends ActiveRecord> T toObject(ResultSet rs, Class<T> type) throws SQLException {

		PropertyDescriptor[] props = getPropertyDescriptors(type);
		ResultSetMetaData rsmd = rs.getMetaData();
		int[] columnToProperty = mapColumnsToProperties(rsmd, props);

		return createObject(rs, type, props, columnToProperty);

	}

	public <T extends ActiveRecord> ResultList toObjectList(ResultSet rs, Class<T> clazz)
			throws SQLException {
		ResultList results = new ResultList();

		if (!rs.next()) {
			return results;
		}

		PropertyDescriptor[] props = getPropertyDescriptors(clazz);
		ResultSetMetaData rsmd = rs.getMetaData();
		int[] columnToProperty = mapColumnsToProperties(rsmd, props);

		do {
			results.add(createObject(rs, clazz, props, columnToProperty));
		} while (rs.next());

		return results;
	}

	public PropertyDescriptor[] getPropertyDescriptors(Class<?> clazz)
			throws SQLException {

		BeanInfo beanInfo = null;
		try {
			beanInfo = Introspector.getBeanInfo(clazz);
		} catch (IntrospectionException e) {
			throw new SQLException("Bean introspection failed: "
					+ e.getMessage());
		}
		return beanInfo.getPropertyDescriptors();
	}

	public int[] mapColumnsToProperties(ResultSetMetaData rsmd,
			PropertyDescriptor[] props) throws SQLException {
		int cols = rsmd.getColumnCount();
		int[] columnToProperty = new int[cols + 1];
		Arrays.fill(columnToProperty, -1);

		for (int col = 1; col <= cols; col++) {
			// Try to get user specified column name first
			String columnName = rsmd.getColumnLabel(col);
			if (columnName == null || columnName.length() == 0) {
				// Get the real column name
				columnName = rsmd.getColumnName(col);
			}
			for (int i = 0; i < props.length; i++) {
				if (columnName.equalsIgnoreCase(props[i].getName())) {
					columnToProperty[col] = i;
					break;
				}
			}
		}
		return columnToProperty;
	}

	public <T extends ActiveRecord> T createObject(ResultSet rs, Class<T> clazz,
			PropertyDescriptor[] props, int[] columnToProperty)
			throws SQLException {

		T object = newInstance(clazz);

		for (int i = 1; i < columnToProperty.length; i++) {
			if (columnToProperty[i] == -1) {
				continue;
			}
			PropertyDescriptor prop = props[columnToProperty[i]];
			Class<?> propertyType = prop.getPropertyType();
			Object value = processColumn(rs, i, propertyType);

			if (propertyType != null && propertyType.isPrimitive()
					&& value == null) {
				value = primitives.get(propertyType);
			}

			invokeSetter(object, prop, value);
		}
		return object;
	}

	public <T> T newInstance(Class<T> clazz) throws SQLException {
		try {
			return clazz.newInstance();

		} catch (InstantiationException e) {
			throw new SQLException("Cannot create " + clazz.getName() + ": "
					+ e.getMessage());

		} catch (IllegalAccessException e) {
			throw new SQLException("Cannot create " + clazz.getName() + ": "
					+ e.getMessage());
		}
	}

	public Object processColumn(ResultSet rs, int columnIndex,
			Class<?> propertyType) throws SQLException {
		// If the column type is Object
		if (!propertyType.isPrimitive() && rs.getObject(columnIndex) == null) {
			return null;
		}

		if (String.class.equals(propertyType)) {
			return rs.getString(columnIndex);

		} else if (propertyType.equals(Integer.TYPE)
				|| propertyType.equals(Integer.class)) {
			return Integer.valueOf(rs.getInt(columnIndex));

		} else if (propertyType.equals(Boolean.TYPE)
				|| propertyType.equals(Boolean.class)) {
			return Boolean.valueOf(rs.getBoolean(columnIndex));

		} else if (propertyType.equals(Long.TYPE)
				|| propertyType.equals(Long.class)) {
			return Long.valueOf(rs.getLong(columnIndex));

		} else if (propertyType.equals(Double.TYPE)
				|| propertyType.equals(Double.class)) {
			return Double.valueOf(rs.getDouble(columnIndex));
		} else if (propertyType.equals(Float.TYPE)
				|| propertyType.equals(Float.class)) {
			return Float.valueOf(rs.getFloat(columnIndex));

		} else if (propertyType.equals(Short.TYPE)
				|| propertyType.equals(Short.class)) {
			return Short.valueOf(rs.getShort(columnIndex));

		} else if (propertyType.equals(Byte.TYPE)
				|| propertyType.equals(Byte.class)) {
			return Byte.valueOf(rs.getByte(columnIndex));

		} else if (propertyType.equals(Timestamp.class)) {
			return rs.getTimestamp(columnIndex);

		} else if (propertyType.equals(SQLXML.class)) {
			return rs.getSQLXML(columnIndex);

		} else {
			return rs.getObject(columnIndex);
		}
	}

	public void invokeSetter(Object target, PropertyDescriptor prop,
			Object value) throws SQLException {

		Method setter = prop.getWriteMethod();
		if (setter == null) {
			return;
		}

		Class<?>[] params = setter.getParameterTypes();
		try {
			if (value instanceof java.util.Date) {
				final String targetType = params[0].getName();
				if ("java.sql.Date".equals(targetType)) {
					value = new java.sql.Date(
							((java.util.Date) value).getTime());
				} else if ("java.sql.Time".equals(targetType)) {
					value = new java.sql.Time(
							((java.util.Date) value).getTime());
				} else if ("java.sql.Timestamp".equals(targetType)) {
					value = new java.sql.Timestamp(
							((java.util.Date) value).getTime());
				}
			}

			if (isCompatibleType(value, params[0])) {
				setter.invoke(target, value);
			} else {
				throw new SQLException("Cannot set " + prop.getName()
						+ ": incompatible types, cannot convert "
						+ value.getClass().getName() + " to "
						+ params[0].getName());

			}
		} catch (IllegalArgumentException e) {
			throw new SQLException("Cannot set " + prop.getName() + ": "
					+ e.getMessage());

		} catch (IllegalAccessException e) {
			throw new SQLException("Cannot set " + prop.getName() + ": "
					+ e.getMessage());

		} catch (InvocationTargetException e) {
			throw new SQLException("Cannot set " + prop.getName() + ": "
					+ e.getMessage());
		}
	}

	public boolean isCompatibleType(Object value, Class<?> paramType) {

		if (value == null || paramType.isInstance(value)) {
			return true;
		} else if (paramType.equals(Integer.TYPE)
				&& Integer.class.isInstance(value)) {
			return true;

		} else if (paramType.equals(Long.TYPE) && Long.class.isInstance(value)) {
			return true;

		} else if (paramType.equals(Double.TYPE)
				&& Double.class.isInstance(value)) {
			return true;

		} else if (paramType.equals(Float.TYPE)
				&& Float.class.isInstance(value)) {
			return true;

		} else if (paramType.equals(Short.TYPE)
				&& Short.class.isInstance(value)) {
			return true;

		} else if (paramType.equals(Byte.TYPE) && Byte.class.isInstance(value)) {
			return true;

		} else if (paramType.equals(Character.TYPE)
				&& Character.class.isInstance(value)) {
			return true;

		} else if (paramType.equals(Boolean.TYPE)
				&& Boolean.class.isInstance(value)) {
			return true;

		}
		return false;
	}
}
