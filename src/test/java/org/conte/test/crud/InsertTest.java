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

package org.conte.test.crud;

import java.util.ArrayList;
import java.util.List;

import org.conte.db.DB;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

public class InsertTest {
	@BeforeTest
	public void open() {
		DB db = new DB();
		db.open("com.mysql.jdbc.Driver", "jdbc:mysql://localhost/test", "root",
				null);
	}
	
	/**
	 * Single table insertion
	 */
	@Test
	public void insert(){
		Conte conte = new Conte();
		conte.setId(7);
		conte.setAddress("d");
		conte.setCity("d");
		conte.setName("d");
		conte.save();
	}
	
	/**
	 * Multiple tables insertion
	 */
	@Test
	public void insertHasOne(){
		Conte conte = new Conte();
		conte.setAddress("e");
		conte.setCity("e");
		conte.setName("e");
		
		ConteOne co = new ConteOne();
//		co.setConte(conte);
//		co.setId(1);
//		co.setName("a");
//		co.save();
		co.setId(2);
		co.setName("b");
		//conte.setConteOne(co);
		conte.save();
	}
	
	@Test
	public void insertHasMany(){
		Conte conte = new Conte();
		conte.setAddress("f");
		conte.setCity("f");
		conte.setName("f");
		
		List list = new ArrayList();
		ConteOne co1 = new ConteOne();
		co1.setName("f");
		list.add(co1);
		
		ConteOne co2 = new ConteOne();
		co2.setName("f");
		list.add(co2);
		conte.setList(list);
		conte.save();
	}
}
