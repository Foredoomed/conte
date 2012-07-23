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

public final class BeanUtils {

	private BeanUtils() {

	}

	public static String getter(String fieldName) {
		int strLen;
		if (fieldName == null || (strLen = fieldName.length()) == 0) {
			throw new RuntimeException(
					"Field name can not be null or empty for the getter method");
		}
		return new StringBuilder(strLen).append("get")
				.append(StringUtils.capitalize(fieldName)).toString();
	}

}
