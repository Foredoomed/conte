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

import java.beans.PropertyDescriptor;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.conte.annotation.Id;
import org.conte.common.AnnotationUtils;
import org.conte.common.DbUtils;
import org.conte.db.DB;
import org.conte.exception.ExcuteQueryException;

import com.mysql.jdbc.Statement;

public class Query {

	private ResultHandler handler = new ResultHandler();
	ResultList results = new ResultList();

	private int limit;
	private int offset;
	private boolean readOnly;
	private boolean unique;
	private String order;
	private String groupByColumn;
	private static Set<String> sums = new HashSet<String>();
	private static Set<String> maximums = new HashSet<String>();
	private static Set<String> minimums = new HashSet<String>();
	private static Set<String> averages = new HashSet<String>();
	private static Set<String> counts = new HashSet<String>();

	private String query;

	public void save(Object obj) {

		PreparedStatement stmt = null;
		Connection connection = null;

		try {
			PropertyDescriptor[] pd = handler.getPropertyDescriptors(obj
					.getClass());
			StringBuilder sb = new StringBuilder();
			StringBuilder values = new StringBuilder();
			sb.append("insert into " + obj.getClass().getSimpleName() + "(");
			values.append("values(");
			Map<String, Object> belongsTo = AnnotationUtils.getBelongsTo(obj
					.getClass());
			Map<String, Object> primaryKey = AnnotationUtils.getPrimaryKey(obj);
			Map<String, Object> hasOne = AnnotationUtils.getHasOne(obj);
			for (int i = 0; i < pd.length; i++) {
				if ("class".equals(pd[i].getName())) {
					continue;
				}

				if (pd[i].getPropertyType().equals(belongsTo.get("fieldType"))) {
					sb.append(belongsTo.get("column")).append(",");
					values.append("'").append(primaryKey.get("value"))
							.append("'").append(",");
					continue;
				}
				if (pd[i].getPropertyType().equals(hasOne.get("fieldType"))) {

					continue;
				}
				Method getter = pd[i].getReadMethod();
				Object value = getter.invoke(obj);
				sb.append(pd[i].getName());
				values.append("'").append(value).append("'");

				if (i != pd.length - 1) {
					sb.append(",");
					values.append(",");
				}
			}

			sb.append(") ");
			values.append(")");

			connection = DB.getConnection();
			connection.setAutoCommit(false);
			if ((Integer) primaryKey.get("value") == 0) {
				stmt = connection.prepareStatement(
						sb.toString() + values.toString(),
						Statement.RETURN_GENERATED_KEYS);
			} else {
				stmt = connection.prepareStatement(sb.toString()
						+ values.toString());
			}
			stmt.executeUpdate();
			if (hasOne.get("column") != null) {
				if ((Integer) primaryKey.get("value") == 0) {
					ResultSet generatedKey = stmt.getGeneratedKeys();
					if (generatedKey.next()) {
						primaryKey.put("value", generatedKey.getInt(1));
					}
				}
				saveHasOne(hasOne, primaryKey);
			}
			connection.commit();
		} catch (Exception e) {
			throw new ExcuteQueryException(e);
		} finally {
			DbUtils.closeQuietly(stmt);
		}

	}

	public void saveHasOne(Map<String, Object> hasOne,
			Map<String, Object> primaryKey) {
		PreparedStatement stmt = null;
		Connection connection = null;

		try {
			Object obj = hasOne.get("value");
			PropertyDescriptor[] pd = handler.getPropertyDescriptors(obj
					.getClass());
			StringBuilder sb = new StringBuilder();
			StringBuilder values = new StringBuilder();
			sb.append("insert into " + obj.getClass().getSimpleName() + "(");
			values.append("values(");
			Map<String, Object> belongsTo = AnnotationUtils.getBelongsTo(obj);
			for (int i = 0; i < pd.length; i++) {
				if ("class".equals(pd[i].getName())) {
					continue;
				}
				if (belongsTo.get("fieldType") != null
						&& pd[i].getPropertyType().equals(
								belongsTo.get("fieldType"))) {
					continue;
				}
				Method getter = pd[i].getReadMethod();
				Object value = getter.invoke(obj);

				sb.append(pd[i].getName());
				values.append("'").append(value).append("'");

				if (i != pd.length - 1) {
					sb.append(",");
					values.append(",");
				}
			}
			if (obj != null) {
				sb.append(",").append(hasOne.get("column")).append(") ");
				values.append(",'").append(primaryKey.get("value"))
						.append("')");
			}

			connection = DB.getConnection();
			stmt = connection.prepareStatement(sb.toString()
					+ values.toString());

			stmt.executeUpdate();

		} catch (Exception e) {
			throw new ExcuteQueryException(e);
		} finally {
			DbUtils.closeQuietly(stmt);
		}

	}

	public void update(Object obj) {
		StringBuilder sb = new StringBuilder();
		sb.append("update " + obj.getClass().getSimpleName() + " set ");
		PreparedStatement stmt = null;
		Connection connection = null;
		try {
			PropertyDescriptor[] pd = handler.getPropertyDescriptors(obj
					.getClass());
			Map<String, Object> primaryKey = AnnotationUtils.getPrimaryKey(obj);
			Map<String, Object> foreignKey = AnnotationUtils.getBelongsTo(obj);
			Map<String, Object> hasOne = AnnotationUtils.getHasOne(obj);
			for (int i = 0; i < pd.length; i++) {
				if ("class".equals(pd[i].getName())
						|| primaryKey.get("column").equals(pd[i].getName())) {
					continue;
				}
				if (pd[i].getPropertyType().equals(foreignKey.get("fieldType"))) {
					sb.append(foreignKey.get("column")).append(" = ")
							.append("'").append(foreignKey.get("value"))
							.append("',");
					continue;
				}
				if (pd[i].getPropertyType().equals(hasOne.get("fieldType"))) {
					continue;
				}

				Method getter = pd[i].getReadMethod();
				Object value = getter.invoke(obj);
				sb.append(pd[i].getName()).append(" = ").append("'")
						.append(value).append("'");

				if (i != pd.length - 1) {
					sb.append(",");
				}
			}

			sb.append(" where ").append(primaryKey.get("column")).append("='")
					.append(primaryKey.get("value")).append("'");
			connection = DB.getConnection();
			connection.setAutoCommit(false);
			stmt = connection.prepareStatement(sb.toString());
			stmt.executeUpdate();

			if (hasOne.get("column") != null) {
				if ((Integer) primaryKey.get("value") == 0) {
					throw new ExcuteQueryException();
				}
				updateHasOne(hasOne, primaryKey);
			}
			connection.commit();
		} catch (Exception e) {
			throw new ExcuteQueryException(e);
		} finally {
			DbUtils.closeQuietly(stmt);
		}
	}

	private void updateHasOne(Map<String, Object> hasOne,
			Map<String, Object> foreignKey) {
		Object obj = hasOne.get("value");
		StringBuilder sb = new StringBuilder();
		sb.append("update " + obj.getClass().getSimpleName() + " set ");
		PreparedStatement stmt = null;
		Connection connection = null;
		try {
			PropertyDescriptor[] pd = handler.getPropertyDescriptors(obj
					.getClass());
			Map<String, Object> belongsTo = AnnotationUtils.getBelongsTo(obj);
			Map<String, Object> primaryKey = AnnotationUtils.getPrimaryKey(obj);
			for (int i = 0; i < pd.length; i++) {
				if ("class".equals(pd[i].getName())
						|| foreignKey.get("column").equals(pd[i].getName())) {
					continue;
				}
				if (pd[i].getPropertyType().equals(belongsTo.get("fieldType"))) {
					continue;
				}

				Method getter = pd[i].getReadMethod();
				Object value = getter.invoke(obj);
				sb.append(pd[i].getName()).append("=").append("'")
						.append(value).append("'");

				if (i != pd.length - 1) {
					sb.append(",");
				}
			}
			if (obj != null) {
				sb.append(",").append(hasOne.get("column")).append("=")
						.append("'").append(foreignKey.get("value"))
						.append("'");
			}

			sb.append(" where ").append(primaryKey.get("column")).append("='")
					.append(primaryKey.get("value")).append("'");
			connection = DB.getConnection();
			stmt = connection.prepareStatement(sb.toString());
			stmt.executeUpdate();
		} catch (Exception e) {
			throw new ExcuteQueryException(e);
		} finally {
			DbUtils.closeQuietly(stmt);
		}
	}

	public void delete(Object obj) {
		StringBuilder sb = new StringBuilder();
		sb.append("delete from " + obj.getClass().getSimpleName() + " where ");

		PreparedStatement stmt = null;
		Connection connection = null;
		try {
			connection = DB.getConnection();
			String primaryKey = null;
			for (Field field : obj.getClass().getDeclaredFields()) {
				Annotation[] annotation = field.getDeclaredAnnotations();
				if (Id.class.equals(annotation[0].annotationType())) {
					primaryKey = field.getName();
					break;
				}
			}
			sb.append(primaryKey).append(" = ?");
			String getterName = "get"
					+ primaryKey.substring(0, 1).toUpperCase()
					+ primaryKey.substring(1);
			Method getter = obj.getClass().getMethod(getterName);
			Object value = getter.invoke(obj);
			stmt = connection.prepareStatement(sb.toString());
			stmt.setInt(1, (Integer) value);
			stmt.executeUpdate();
		} catch (Exception e) {
			throw new ExcuteQueryException(e);
		} finally {
			DbUtils.closeQuietly(stmt);
		}

	}

	public <T extends ActiveRecord> T find(Class<T> clazz, int id) {

		if (id < 0) {
			throw new IllegalArgumentException(
					"The parameter id can not be negative.");
		}

		String query = "select * from " + clazz.getSimpleName()
				+ " where id = ?";

		PreparedStatement stmt = null;
		ResultSet rs = null;
		Connection connection = null;
		try {
			connection = DB.getConnection();
			if (connection == null) {
				throw new ExcuteQueryException();
			}
			stmt = connection.prepareStatement(query);
			stmt.setInt(1, id);
			rs = stmt.executeQuery();
			if (rs.next()) {
				return (T) handler.toObject(rs, clazz);
			}
			return null;
		} catch (Exception e) {
			throw new ExcuteQueryException(e);
		} finally {
			DbUtils.closeQuietly(rs);
			DbUtils.closeQuietly(stmt);
		}
	}

	public <T extends ActiveRecord> Query where(Class<T> clazz,
			String condition, Object... params) {
		return this;
	}

	public <T extends ActiveRecord> Query select(Class<T> clazz,
			String... columns) {

		if (columns.length == 0) {
			throw new IllegalArgumentException(
					"Columns must be defined for the select method.");
		}
		StringBuilder sb = new StringBuilder();
		sb.append("select ");
		for (int i = 0; i < columns.length; i++) {
			sb.append(columns[i]);
			if (i != columns.length - 1) {
				sb.append(",");
			}
		}

		PreparedStatement stmt = null;
		ResultSet rs = null;
		Connection connection = null;
		try {
			connection = DB.getConnection();
			if (connection == null) {
				throw new ExcuteQueryException();
			}
			stmt = connection.prepareStatement(sb.toString());
			rs = stmt.executeQuery();
			if (rs.next()) {
				results = handler.toObjectList(rs, clazz);
			}
			return null;
		} catch (Exception e) {
			throw new ExcuteQueryException(e);
		} finally {
			DbUtils.closeQuietly(rs);
			DbUtils.closeQuietly(stmt);
		}
	}

	public Query limit(int limit) {

		if (limit < 0) {
			throw new IllegalArgumentException("limit can not be negative");
		}

		if (query == null || query.length() == 0) {
			throw new ExcuteQueryException("wrong use of limit");
		}

		this.limit = limit;
		return this;
	}

	public Query order(String order) {

		if (order == null || order.length() == 0) {
			throw new IllegalArgumentException(
					"ORDERBY column can not be null or empty");
		}

		this.order = order;
		return this;
	}

	public Query sum(String... column) {
		if (column == null) {
			throw new IllegalArgumentException(
					"SUM column can not be null or empty");
		}
		for (String c : column) {
			sums.add(c);
		}
		return this;
	}

	public Query maximum(String... column) {
		if (column == null) {
			throw new IllegalArgumentException(
					"SUM column can not be null or empty");
		}
		for (String c : column) {
			maximums.add(c);
		}
		return this;
	}

	public Query minimum(String... column) {
		if (column == null) {
			throw new IllegalArgumentException(
					"SUM column can not be null or empty");
		}
		for (String c : column) {
			minimums.add(c);
		}
		return this;
	}

	public Query average(String... column) {
		if (column == null) {
			throw new IllegalArgumentException(
					"SUM column can not be null or empty");
		}
		for (String c : column) {
			averages.add(c);
		}
		return this;
	}

	public Query count(String... column) {
		if (column == null) {
			throw new IllegalArgumentException(
					"SUM column can not be null or empty");
		}
		for (String c : column) {
			counts.add(c);
		}
		return this;
	}

	public Query unique() {
		this.unique = true;
		return this;
	}

	public Query offset(int offset) {
		this.offset = offset;
		return this;
	}

	public Query readOnly() {
		this.readOnly = true;
		return this;
	}

	public Query group(String column) {
		this.groupByColumn = column;
		return this;
	}

}
