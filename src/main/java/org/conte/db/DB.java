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
import java.sql.DriverManager;

import org.conte.exception.InitException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DB {
	
	private static final Logger logger = LoggerFactory.getLogger(DB.class);
	
	
	/**
	 * Open a new connection
	 * 
	 * @param driver database driver class name
	 * @param url database url
	 * @param username user name
	 * @param password password 
	 */
	public void open(String driver, String url, String username, String password) {
		
		try {
			Class.forName(driver);
			Connection connection = DriverManager.getConnection(url,username,password);
			Connections.attach(connection);
		} catch (Exception e) {
			throw new InitException(e);
		}
		
	}
	
	/**
	 * Return a specified connection
	 * 
	 * @return
	 */
	public static Connection getConnection(){
		return Connections.getConnection();
	}
	
}
