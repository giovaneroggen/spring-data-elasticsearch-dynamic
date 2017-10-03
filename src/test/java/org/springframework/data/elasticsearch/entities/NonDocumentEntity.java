/*
 * Copyright 2013 the original author or authors.
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
package org.springframework.data.elasticsearch.entities;

import org.springframework.data.annotation.Id;

/**
 * @author Rizwan Idrees
 * @author Mohsin Husen
 */
public class NonDocumentEntity {

	@Id
	private String someId;
	private String someField1;
	private String someField2;

	public String getSomeField1() {
		return someField1;
	}

	public void setSomeField1(String someField1) {
		this.someField1 = someField1;
	}

	public String getSomeField2() {
		return someField2;
	}

	public void setSomeField2(String someField2) {
		this.someField2 = someField2;
	}
}
