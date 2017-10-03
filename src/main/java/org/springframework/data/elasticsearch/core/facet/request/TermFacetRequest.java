/*
 * Copyright 2014 the original author or authors.
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

package org.springframework.data.elasticsearch.core.facet.request;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.lucene.util.automaton.RegExp;
import org.elasticsearch.search.aggregations.AbstractAggregationBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.terms.support.IncludeExclude;
import org.springframework.data.elasticsearch.core.facet.AbstractFacetRequest;
import org.springframework.util.Assert;


/**
 * Term facet
 *
 * @author Artur Konczak
 */
@Deprecated
public class TermFacetRequest extends AbstractFacetRequest {

	private String[] fields;
	private String[] excludeTerms;
	private int size = 10;
	private TermFacetOrder order = TermFacetOrder.descCount;
	private boolean allTerms = false;
	private String regex = null;

	public TermFacetRequest(String name) {
		super(name);
	}

	public void setFields(String... fields) {
		Assert.isTrue(ArrayUtils.isNotEmpty(fields), "Term agg need one field only");
		Assert.isTrue(ArrayUtils.getLength(fields) == 1, "Term agg need one field only");
		this.fields = fields;
	}

	public void setSize(int size) {
		Assert.isTrue(size >= 0, "Size should be bigger then zero !!!");
		this.size = size;
	}

	public void setOrder(TermFacetOrder order) {
		this.order = order;
	}

	public void setExcludeTerms(String... excludeTerms) {
		this.excludeTerms = excludeTerms;
	}

	public void setAllTerms(boolean allTerms) {
		this.allTerms = allTerms;
	}

	public void setRegex(String regex) {
		this.regex = regex;
	}

	@Override
	public AbstractAggregationBuilder getFacet() {
		Assert.notEmpty(fields, "Please select at last one field !!!");
		final TermsAggregationBuilder termsBuilder = AggregationBuilders.terms(getName()).field(fields[0]).size(this.size);

		switch (order) {
			case descTerm:
				termsBuilder.order(Terms.Order.term(false));
				break;
			case ascTerm:
				termsBuilder.order(Terms.Order.term(true));
				break;
			case descCount:
				termsBuilder.order(Terms.Order.count(false));
				break;
			default:
				termsBuilder.order(Terms.Order.count(true));
		}
		if (ArrayUtils.isNotEmpty(excludeTerms)) {
			termsBuilder.includeExclude(new IncludeExclude(null,excludeTerms));
		}

		if (allTerms) {
			termsBuilder.size(Integer.MAX_VALUE);
		}

		if (StringUtils.isNotBlank(regex)) {
			termsBuilder.includeExclude(new IncludeExclude(new RegExp(regex),null));
		}

		return termsBuilder;
	}
}