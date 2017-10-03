/*
 * Copyright 2013-2017 the original author or authors.
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
package org.springframework.data.elasticsearch.core.query;

import static java.util.Collections.addAll;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.elasticsearch.action.search.SearchType;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.util.Assert;

/**
 * AbstractQuery
 *
 * @author Rizwan Idrees
 * @author Mohsin Husen
 * @author Mark Paluch
 */
abstract class AbstractQuery implements Query {

	protected Pageable pageable = DEFAULT_PAGE;
	protected Sort sort;
	protected List<String> indices = new ArrayList<>();
	protected List<String> types = new ArrayList<>();
	protected List<String> fields = new ArrayList<>();
	protected SourceFilter sourceFilter;
	protected float minScore;
	protected Collection<String> ids;
	protected String route;
	protected SearchType searchType = SearchType.DFS_QUERY_THEN_FETCH;

	@Override
	public Sort getSort() {
		return this.sort;
	}

	@Override
	public Pageable getPageable() {
		return this.pageable;
	}

	@Override
	public final <T extends Query> T setPageable(Pageable pageable) {
		
		Assert.notNull(pageable, "Pageable must not be null!");
		
		this.pageable = pageable;
		return (T) this.addSort(pageable.getSort());
	}

	@Override
	public void addFields(String... fields) {
		addAll(this.fields, fields);
	}

	@Override
	public List<String> getFields() {
		return fields;
	}

	@Override
	public List<String> getIndices() {
		return indices;
	}

	@Override
	public void addIndices(String... indices) {
		addAll(this.indices, indices);
	}

	@Override
	public void addTypes(String... types) {
		addAll(this.types, types);
	}

	@Override
	public List<String> getTypes() {
		return types;
	}

	@Override
	public void addSourceFilter(SourceFilter sourceFilter) {
		this.sourceFilter = sourceFilter;
	}

	@Override
	public SourceFilter getSourceFilter() {
		return sourceFilter;
	}

	@SuppressWarnings("unchecked")
	public final <T extends Query> T addSort(Sort sort) {
		if (sort == null) {
			return (T) this;
		}

		if (this.sort == null) {
			this.sort = sort;
		} else {
			this.sort = this.sort.and(sort);
		}

		return (T) this;
	}

	public float getMinScore() {
		return minScore;
	}

	public void setMinScore(float minScore) {
		this.minScore = minScore;
	}

	public Collection<String> getIds() {
		return ids;
	}

	public void setIds(Collection<String> ids) {
		this.ids = ids;
	}

	public String getRoute() {
		return route;
	}

	public void setRoute(String route) {
		this.route = route;
	}

	public void setSearchType(SearchType searchType) {
		this.searchType = searchType;
	}

	public SearchType getSearchType() {
		return searchType;
	}
}
