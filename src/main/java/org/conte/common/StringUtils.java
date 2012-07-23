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

public final class StringUtils {

	private StringUtils() {

	}

	/**
	 * Changes the first character of a string to upper case.
	 * 
	 * @param source
	 * @return
	 */
	public static String capitalize(String source) {
		int strLen;
		if (source == null || (strLen = source.length()) == 0) {
			return source;
		}
		return new StringBuilder(strLen)
				.append(Character.toTitleCase(source.charAt(0)))
				.append(source.substring(1)).toString();
	}
}
