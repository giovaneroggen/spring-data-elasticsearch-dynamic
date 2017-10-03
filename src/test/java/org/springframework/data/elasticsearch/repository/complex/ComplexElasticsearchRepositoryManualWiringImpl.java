/*
 * Copyright 2014 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.data.elasticsearch.repository.complex;

import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;

/**
 * @author Artur Konczak
 * @author Mohsin Husen
 */
public class ComplexElasticsearchRepositoryManualWiringImpl implements ComplexElasticsearchRepositoryCustom {

	private ElasticsearchTemplate template;

	@Override
	public String doSomethingSpecial() {
		assert (template.getElasticsearchConverter() != null);
		return "3+3=6";
	}

	public void setTemplate(ElasticsearchTemplate template) {
		this.template = template;
	}
}
