/*
 * Copyright 2013-2014 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.data.elasticsearch.core;

import java.io.IOException;

/**
 * @author Artur Konczak
 * @author Mohsin Husen
 */
public class CustomEntityMapper implements EntityMapper {

	public CustomEntityMapper() {
		//custom configuration/implementation (e.g. FasterXML/jackson)
	}

	@Override
	public String mapToString(Object object) throws IOException {
		//mapping Object to text
		return null;
	}

	@Override
	public <T> T mapToObject(String source, Class<T> clazz) throws IOException {
		//mapping text to Object
		return null;
	}
}
