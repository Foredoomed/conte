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

import java.io.Serializable;

public class ActiveRecord implements Serializable {

	private static final long serialVersionUID = -8855942524079484263L;
	private static ThreadLocal<Query> query = new ThreadLocal<Query>();
	private static ThreadLocal<State> state = new ThreadLocal<State>();

	static {
		query.set(new Query());
		state.set(State.Transient);
	}

	/**
	 * Save the object into the database
	 */
	public void save() {
		query.get().save(this);
		state.set(State.Persistent);
	}

	/**
	 * Update the corresponding record in the database
	 */
	public void update() {
		query.get().update(this);
		state.set(State.Persistent);
	}

	/**
	 * Delete the corresponding record in the database
	 */
	public void delete() {
		query.get().delete(this);
		state.set(State.Transient);
	}

	/**
	 * Save the object into the database when it is new or update it when it is
	 * already exists in the database.
	 */
	public void saveOrUpdate() {
		if (State.Transient.equals(state.get())) {
			save();
		} else if (State.Persistent.equals(state.get())) {
			update();
		}
	}

	/**
	 * Retrieve the object corresponding to the specified primary key that
	 * matches any supplied options
	 * 
	 * @param clazz
	 * @param id
	 * @return
	 */
	public static <T extends ActiveRecord> T find(Class<T> clazz, int id) {
		state.set(State.Persistent);
		return query.get().find(clazz, id);	
	}

	/**
	 * This method returns all of the matching records for the supplied primary
	 * keys.
	 * 
	 * @param low
	 * @param high
	 * @param clazz
	 * @return
	 */
	public static <T extends ActiveRecord> Query find(int[] ids, Class<T> clazz) {
		StringBuilder sb = new StringBuilder();
		sb.append("id IN (");
		for (int i = 0; i < ids.length; i++) {
			sb.append(ids[i]);
			if (i != ids.length - 1) {
				sb.append(",");
			}
		}
		sb.append(")");
		return query.get().where(clazz, sb.toString());

	}

	/**
	 * Finds the first record.
	 * 
	 * @param clazz
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static <T extends ActiveRecord> T first(Class<T> clazz) {
		Query lazyResults = query.get().limit(1).where(clazz, null);
		return (T) (lazyResults.results.size() > 0 ? lazyResults.results.get(0)
				: null);
	}

	/**
	 * Finds the last record matched by the supplied options.
	 * 
	 * @param clazz
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static <T extends ActiveRecord> T last(Class<T> clazz) {
		Query lazyResults = query.get().limit(1).order("id DESC")
				.where(clazz, null);
		return (T) (lazyResults.results.size() > 0 ? lazyResults.results.get(0)
				: null);

	}

	/**
	 * This method retrieves a batch of records and then yields each record to
	 * the Callback interface individually as a model
	 * 
	 * @param rows
	 * @param callback
	 */
	public static <T extends ActiveRecord> void findEach(int rows,
			ResultCallback<T> callback) {

	}

	public static <T extends ActiveRecord> void findInBatches(int row,
			ResultCallback<T> callback) {

	}

	/**
	 * Specify conditions to limit the records returned, representing the
	 * WHERE-part of the SQL statement.
	 * 
	 * @param clazz
	 * @param condition
	 * @param params
	 * @return
	 */
	public static <T extends ActiveRecord> Query where(Class<T> clazz,
			String condition, Object... params) {
		return new Query().where(clazz, condition, params);
	}

	/**
	 * To retrieve records from the database in a specific order. For instance:
	 * order("name") / order("name DESC") / order("created_at ASC") /
	 * order("name ASC, age DESC")
	 * 
	 * @param column
	 * @param order
	 * @return
	 */
	public static <T extends ActiveRecord> Query order(String order) {
		return new Query().order(order);
	}

	/**
	 * To select only a subset of fields from the result set, you can specify
	 * the subset via the select method. Notice: If the select method is used,
	 * all the returning objects will be read only.
	 * 
	 * @param columns
	 * @return
	 */
	public static <T extends ActiveRecord> Query select(Class<T> clazz,
			String... columns) {

		return query.get().select(clazz, columns);
	}

	/**
	 * Get a single record per unique value in a certain field.
	 * 
	 * @return
	 */
	public static <T extends ActiveRecord> Query unique() {
		return query.get().unique();
	}

	/**
	 * Specify the number of records to be retrieved.
	 * 
	 * @param rows
	 * @return
	 */
	public static <T extends ActiveRecord> Query limit(int limit) {
		return query.get().limit(limit);
	}

	/**
	 * Specify the number of records to skip before starting to return the
	 * records.
	 * 
	 * @param offset
	 * @return
	 */
	public static <T extends ActiveRecord> Query offset(int offset) {
		return query.get().offset(offset);
	}

	/**
	 * 
	 * 
	 * @param column
	 * @return
	 */
	public static <T extends ActiveRecord> Query group(String column) {
		return query.get().group(column);
	}

	/*
	 * Disallow modification or deletion of any of the returned object. Any
	 * attempt to alter or destroy a readonly record will not succeed, raising a
	 * ReadOnlyRecordException.
	 */
	public static <T extends ActiveRecord> Query readOnly() {
		return query.get().readOnly();
	}

	public static <T extends ActiveRecord> Query joins(String tables) {
		return null;
	}

	// n+1
	public static <T extends ActiveRecord> Query includes() {
		return null;
	}

	/**
	 * This method let you use your own SQL to find records in a table. It will
	 * return a list of objects even if the underlying query returns just a
	 * single record.
	 * 
	 * @param sql
	 * @return
	 */
	public static <T extends ActiveRecord> Query findBySql(String sql,
			Class<T> clazz) {
		return where(clazz, null);
	}

	// like find_by_sql but will not instantiate them. Instead, you will get an
	// array of hashes where each hash indicates a record.
	public static <T extends ActiveRecord> Query selectAll(String sql) {
		return null;
	}

	/**
	 * Check for the existence of the object,it will return true if any one of
	 * those records exists.
	 * 
	 * @param ids
	 * @param clazz
	 * @return
	 */
	public static <T extends ActiveRecord> boolean existsOrNot(Class<T> clazz,
			int[] ids) {

		for (int id : ids) {
			T t = find(clazz, id);
			if (t != null) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Get the count of records in one of the tables
	 * 
	 * @param column
	 */
	public static void count(String... column) {
		query.get().count(column);
	}

	/**
	 * Get the average of a certain number in one of the tables
	 * 
	 * @param column
	 */
	public static void average(String column) {
		query.get().average(column);
	}

	/**
	 * Find the minimum value of a column
	 * 
	 * @param column
	 */
	public static void minimum(String... column) {
		query.get().minimum(column);
	}

	/**
	 * Find the maximum value of a column
	 * 
	 * @param column
	 */
	public static void maximum(String... column) {
		query.get().maximum(column);
	}

	/**
	 * Find the sum of a column for all records
	 * 
	 * @param column
	 */
	public static void sum(String... column) {
		query.get().sum(column);
	}
}
