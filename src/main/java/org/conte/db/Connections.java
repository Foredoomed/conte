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

package org.conte.db;

import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Connections {

	private static final Logger logger = LoggerFactory.getLogger(Connections.class);

	private static final ThreadLocal<Connection> conn = new ThreadLocal<Connection>();
		
	/**
	 * Link the database connection to the current thread
	 * 
	 * @param dbName database name. e.g. mysql
	 * @param connection database connection
	 */
//	public static void link(String dbName, Connection connection){
//		if(pond.get() == null){
//			pond.set(new HashMap<String,Connection>());
//		}
//		pond.get().put(dbName, connection);
//		logger.debug("Linked the database: " + dbName + " and its connection: " + connection + " to the current thread.");
//	}
	
	/**
	 * Unlink the database connection to the current thread
	 * 
	 * @param dbName
	 */
//	public static void unLink(String dbName){
//		pond.get().remove(dbName);
//		logger.debug("Unlinked the database: " + dbName + "to the current thread.");
//	}
	
	/**
	 * Return a connection
	 * 
	 * @param dbName
	 * @return
	 */
	public static Connection getConnection(){
		return (Connection)conn.get();
	}
	
	/**
	 * Attach the connection to the current thread
	 * 
	 * @param connection
	 */
	public static void attach(Connection connection){
		conn.set(connection);
	}
	
}
