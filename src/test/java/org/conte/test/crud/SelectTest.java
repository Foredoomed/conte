/* 
 	Copyright 2012 Foredoomed
 	Licensed under the Apache License, Version 2.0 (the "License");
   	you may not use this file except in compliance with the License.
   	You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

  	Unless required by applicable law or agreed to in writing, software
   	distributed under the License is distributed on an "AS IS" BASIS,
   	WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   	See the License for the specific language governing permissions and
   	limitations under the License. 
 */

package org.conte.test.crud;

import org.conte.db.DB;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

public class SelectTest {

	@BeforeTest
	public void open() {
		DB db = new DB();
		db.open("com.mysql.jdbc.Driver", "jdbc:mysql://localhost/test", "root",
				null);
	}

	@Test
	public void findById() {

		Conte conte = Conte.find(Conte.class, 1);
		System.out.print(conte);
	}

}
