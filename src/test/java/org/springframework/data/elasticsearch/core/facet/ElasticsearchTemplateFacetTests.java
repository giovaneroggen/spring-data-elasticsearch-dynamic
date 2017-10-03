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

package org.springframework.data.elasticsearch.core.facet;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.FacetedPage;
import org.springframework.data.elasticsearch.core.aggregation.AggregatedPage;
import org.springframework.data.elasticsearch.core.facet.request.HistogramFacetRequestBuilder;
import org.springframework.data.elasticsearch.core.facet.request.NativeFacetRequest;
import org.springframework.data.elasticsearch.core.facet.request.RangeFacetRequestBuilder;
import org.springframework.data.elasticsearch.core.facet.request.StatisticalFacetRequestBuilder;
import org.springframework.data.elasticsearch.core.facet.request.TermFacetRequestBuilder;
import org.springframework.data.elasticsearch.core.facet.result.HistogramResult;
import org.springframework.data.elasticsearch.core.facet.result.IntervalUnit;
import org.springframework.data.elasticsearch.core.facet.result.Range;
import org.springframework.data.elasticsearch.core.facet.result.RangeResult;
import org.springframework.data.elasticsearch.core.facet.result.StatisticalResult;
import org.springframework.data.elasticsearch.core.facet.result.Term;
import org.springframework.data.elasticsearch.core.facet.result.TermResult;
import org.springframework.data.elasticsearch.core.query.IndexQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.data.elasticsearch.core.query.SearchQuery;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import static org.elasticsearch.index.query.QueryBuilders.*;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

/**
 * @author Rizwan Idrees
 * @author Mohsin Husen
 * @author Jonathan Yan
 * @author Artur Konczak
 */

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:elasticsearch-template-test.xml")
public class ElasticsearchTemplateFacetTests {

	private static final String RIZWAN_IDREES = "Rizwan Idrees";
	private static final String MOHSIN_HUSEN = "Mohsin Husen";
	private static final String JONATHAN_YAN = "Jonathan Yan";
	private static final String ARTUR_KONCZAK = "Artur Konczak";
	private static final int YEAR_2002 = 2002;
	private static final int YEAR_2001 = 2001;
	private static final int YEAR_2000 = 2000;
	private static final String PUBLISHED_YEARS = "publishedYears";
	@Autowired
	private ElasticsearchTemplate elasticsearchTemplate;

	@Before
	public void before() {
		elasticsearchTemplate.deleteIndex(ArticleEntity.class);
		elasticsearchTemplate.createIndex(ArticleEntity.class);
		elasticsearchTemplate.putMapping(ArticleEntity.class);
		elasticsearchTemplate.refresh(ArticleEntity.class);

		IndexQuery article1 = new ArticleEntityBuilder("1").title("article four").addAuthor(RIZWAN_IDREES).addAuthor(ARTUR_KONCZAK).addAuthor(MOHSIN_HUSEN).addAuthor(JONATHAN_YAN).score(10).buildIndex();
		IndexQuery article2 = new ArticleEntityBuilder("2").title("article three").addAuthor(RIZWAN_IDREES).addAuthor(ARTUR_KONCZAK).addAuthor(MOHSIN_HUSEN).addPublishedYear(YEAR_2000).score(20).buildIndex();
		IndexQuery article3 = new ArticleEntityBuilder("3").title("article two").addAuthor(RIZWAN_IDREES).addAuthor(ARTUR_KONCZAK).addPublishedYear(YEAR_2001).addPublishedYear(YEAR_2000).score(30).buildIndex();
		IndexQuery article4 = new ArticleEntityBuilder("4").title("article one").addAuthor(RIZWAN_IDREES).addPublishedYear(YEAR_2002).addPublishedYear(YEAR_2001).addPublishedYear(YEAR_2000).score(40).buildIndex();

		elasticsearchTemplate.index(article1);
		elasticsearchTemplate.index(article2);
		elasticsearchTemplate.index(article3);
		elasticsearchTemplate.index(article4);
		elasticsearchTemplate.refresh(ArticleEntity.class);
	}

	@Test
	public void shouldReturnFacetedAuthorsForGivenQueryWithDefaultOrder() {

		// given
		String facetName = "fauthors";
		SearchQuery searchQuery = new NativeSearchQueryBuilder().withQuery(matchAllQuery()).withFacet(new TermFacetRequestBuilder(facetName).fields("authors.untouched").build()).build();
		// when
		FacetedPage<ArticleEntity> result = elasticsearchTemplate.queryForPage(searchQuery, ArticleEntity.class);
		// then
		assertThat(result.getNumberOfElements(), is(equalTo(4)));

		TermResult facet = (TermResult) result.getFacet(facetName);
		Term term = facet.getTerms().get(0);
		assertThat(term.getTerm(), is(RIZWAN_IDREES));
		assertThat(term.getCount(), is(4l));

		term = facet.getTerms().get(1);
		assertThat(term.getTerm(), is(ARTUR_KONCZAK));
		assertThat(term.getCount(), is(3l));

		term = facet.getTerms().get(2);
		assertThat(term.getTerm(), is(MOHSIN_HUSEN));
		assertThat(term.getCount(), is(2l));

		term = facet.getTerms().get(3);
		assertThat(term.getTerm(), is(JONATHAN_YAN));
		assertThat(term.getCount(), is(1l));
		
		assertThat(facet.getTotal(), is(4l));
		assertThat(facet.getOther(), is(0l));
		assertThat(facet.getMissing(), is(0l));
	}

	@Test
	public void shouldReturnFacetedAuthorsForGivenFilteredQuery() {

		// given
		String facetName = "fauthors";
		SearchQuery searchQuery = new NativeSearchQueryBuilder().withQuery(matchAllQuery())
				.withFacet(new TermFacetRequestBuilder(facetName).applyQueryFilter().fields("authors.untouched").build()).build();
		// when
		FacetedPage<ArticleEntity> result = elasticsearchTemplate.queryForPage(searchQuery, ArticleEntity.class);
		// then
		assertThat(result.getNumberOfElements(), is(equalTo(4)));

		TermResult facet = (TermResult) result.getFacet(facetName);
		Term term = facet.getTerms().get(0);
		assertThat(term.getTerm(), is(RIZWAN_IDREES));
		assertThat(term.getCount(), is(4l));

		term = facet.getTerms().get(1);
		assertThat(term.getTerm(), is(ARTUR_KONCZAK));
		assertThat(term.getCount(), is(3l));

		term = facet.getTerms().get(2);
		assertThat(term.getTerm(), is(MOHSIN_HUSEN));
		assertThat(term.getCount(), is(2l));
		
		assertThat(facet.getTotal(), is(4l));
		assertThat(facet.getOther(), is(0l));
		assertThat(facet.getMissing(), is(0l));
	}

	@Test
	public void shouldExcludeTermsFromFacetedAuthorsForGivenQuery() {
		// given
		String facetName = "fauthors";
		SearchQuery searchQuery = new NativeSearchQueryBuilder().withQuery(matchAllQuery())
				.withFacet(new TermFacetRequestBuilder(facetName).applyQueryFilter().fields("authors.untouched").excludeTerms(RIZWAN_IDREES, ARTUR_KONCZAK).build()).build();
		// when
		FacetedPage<ArticleEntity> result = elasticsearchTemplate.queryForPage(searchQuery, ArticleEntity.class);
		// then
		assertThat(result.getNumberOfElements(), is(equalTo(4)));

		TermResult facet = (TermResult) result.getFacet(facetName);

		assertThat(facet.getTerms().size(), is(2));

		Term term = facet.getTerms().get(0);
		assertThat(term.getTerm(), is(MOHSIN_HUSEN));
		assertThat(term.getCount(), is(2l));

		Term term1 = facet.getTerms().get(1);
		assertThat(term1.getTerm(), is(JONATHAN_YAN));
		assertThat(term1.getCount(), is(1l));
		
		assertThat(facet.getTotal(), is(2l));
		assertThat(facet.getOther(), is(0l));
		assertThat(facet.getMissing(), is(0l));
	}

	@Test
	public void shouldReturnFacetedAuthorsForGivenQueryOrderedByTerm() {

		// given
		String facetName = "fauthors";
		SearchQuery searchQuery = new NativeSearchQueryBuilder().withQuery(matchAllQuery())
				.withFacet(new TermFacetRequestBuilder(facetName).fields("authors.untouched").ascTerm().build()).build();
		// when
		FacetedPage<ArticleEntity> result = elasticsearchTemplate.queryForPage(searchQuery, ArticleEntity.class);
		// then
		assertThat(result.getNumberOfElements(), is(equalTo(4)));

		TermResult facet = (TermResult) result.getFacet(facetName);
		Term term = facet.getTerms().get(0);
		assertThat(term.getTerm(), is(ARTUR_KONCZAK));
		assertThat(term.getCount(), is(3l));

		term = facet.getTerms().get(1);
		assertThat(term.getTerm(), is(JONATHAN_YAN));
		assertThat(term.getCount(), is(1l));

		term = facet.getTerms().get(2);
		assertThat(term.getTerm(), is(MOHSIN_HUSEN));
		assertThat(term.getCount(), is(2l));

		term = facet.getTerms().get(3);
		assertThat(term.getTerm(), is(RIZWAN_IDREES));
		assertThat(term.getCount(), is(4l));

		assertThat(facet.getTotal(), is(4l));
		assertThat(facet.getOther(), is(0l));
		assertThat(facet.getMissing(), is(0l));
	}

	@Test
	public void shouldReturnFacetedAuthorsForGivenQueryOrderedByCountAsc() {

		// given
		String facetName = "fauthors";
		SearchQuery searchQuery = new NativeSearchQueryBuilder().withQuery(matchAllQuery())
				.withFacet(new TermFacetRequestBuilder(facetName).fields("authors.untouched").ascCount().build()).build();
		// when
		FacetedPage<ArticleEntity> result = elasticsearchTemplate.queryForPage(searchQuery, ArticleEntity.class);
		// then
		assertThat(result.getNumberOfElements(), is(equalTo(4)));

		TermResult facet = (TermResult) result.getFacet(facetName);
		Term term = facet.getTerms().get(0);
		assertThat(term.getTerm(), is(JONATHAN_YAN));
		assertThat(term.getCount(), is(1l));

		term = facet.getTerms().get(1);
		assertThat(term.getTerm(), is(MOHSIN_HUSEN));
		assertThat(term.getCount(), is(2l));

		term = facet.getTerms().get(2);
		assertThat(term.getTerm(), is(ARTUR_KONCZAK));
		assertThat(term.getCount(), is(3l));

		term = facet.getTerms().get(3);
		assertThat(term.getTerm(), is(RIZWAN_IDREES));
		assertThat(term.getCount(), is(4l));
		
		assertThat(facet.getTotal(), is(4l));
		assertThat(facet.getOther(), is(0l));
		assertThat(facet.getMissing(), is(0l));
	}

	@Test
	public void shouldReturnFacetedYearsForGivenQuery() {

		// given
		String facetName = "fyears";
		SearchQuery searchQuery = new NativeSearchQueryBuilder().withQuery(matchAllQuery())
				.withFacet(new TermFacetRequestBuilder(facetName).fields("publishedYears").descCount().build()).build();
		// when
		FacetedPage<ArticleEntity> result = elasticsearchTemplate.queryForPage(searchQuery, ArticleEntity.class);
		// then
		assertThat(result.getNumberOfElements(), is(equalTo(4)));

		TermResult facet = (TermResult) result.getFacet(facetName);
		assertThat(facet.getTerms().size(), is(equalTo(3)));

		Term term = facet.getTerms().get(0);
		assertThat(term.getTerm(), is(Long.toString(YEAR_2000)));
		assertThat(term.getCount(), is(3l));

		term = facet.getTerms().get(1);
		assertThat(term.getTerm(), is(Long.toString(YEAR_2001)));
		assertThat(term.getCount(), is(2l));

		term = facet.getTerms().get(2);
		assertThat(term.getTerm(), is(Long.toString(YEAR_2002)));
		assertThat(term.getCount(), is(1l));
		
		assertThat(facet.getTotal(), is(3l));
		assertThat(facet.getOther(), is(0l));
		assertThat(facet.getMissing(), is(0l));
	}

	@Test
	public void shouldReturnExistingFacetedYearsForGivenQuery() {

		// given
		String facetName = "fyears";
		SearchQuery searchQuery = new NativeSearchQueryBuilder().withQuery(matchAllQuery())
				.withFacet(new TermFacetRequestBuilder(facetName).applyQueryFilter().fields("publishedYears").descCount().build()).build();
		// when
		FacetedPage<ArticleEntity> result = elasticsearchTemplate.queryForPage(searchQuery, ArticleEntity.class);
		// then
		assertThat(result.getNumberOfElements(), is(equalTo(4)));

		TermResult facet = (TermResult) result.getFacet(facetName);
		assertThat(facet.getTerms().size(), is(equalTo(3)));

		Term term = facet.getTerms().get(0);
		assertThat(term.getTerm(), is(Long.toString(YEAR_2000)));
		assertThat(term.getCount(), is(3l));

		term = facet.getTerms().get(1);
		assertThat(term.getTerm(), is(Long.toString(YEAR_2001)));
		assertThat(term.getCount(), is(2l));

		term = facet.getTerms().get(2);
		assertThat(term.getTerm(), is(Long.toString(YEAR_2002)));
		assertThat(term.getCount(), is(1l));
		
		assertThat(facet.getTotal(), is(3l));
		assertThat(facet.getOther(), is(0l));
		assertThat(facet.getMissing(), is(0l));
	}


	@Test(expected = IllegalArgumentException.class)
	public void shouldThrowExeptionsForMultiFieldFacet() {

		// given
		String facetName = "fyears";
		SearchQuery searchQuery = new NativeSearchQueryBuilder().withQuery(matchAllQuery())
				.withFacet(new TermFacetRequestBuilder(facetName).fields("publishedYears", "authors.untouched").ascTerm().build()).build();
		// when
		FacetedPage<ArticleEntity> result = elasticsearchTemplate.queryForPage(searchQuery, ArticleEntity.class);
		// then
		assertThat(result.getNumberOfElements(), is(equalTo(4)));

		TermResult facet = (TermResult) result.getFacet(facetName);
		assertThat(facet.getTerms().size(), is(equalTo(7)));

		Term term = facet.getTerms().get(0);
		assertThat(term.getTerm(), is(Long.toString(YEAR_2000)));
		assertThat(term.getCount(), is(3l));

		term = facet.getTerms().get(1);
		assertThat(term.getTerm(), is(Long.toString(YEAR_2001)));
		assertThat(term.getCount(), is(2l));

		term = facet.getTerms().get(2);
		assertThat(term.getTerm(), is(Long.toString(YEAR_2002)));
		assertThat(term.getCount(), is(1l));

		term = facet.getTerms().get(3);
		assertThat(term.getTerm(), is(ARTUR_KONCZAK));
		assertThat(term.getCount(), is(3l));

		term = facet.getTerms().get(4);
		assertThat(term.getTerm(), is(JONATHAN_YAN));
		assertThat(term.getCount(), is(1l));

		term = facet.getTerms().get(5);
		assertThat(term.getTerm(), is(MOHSIN_HUSEN));
		assertThat(term.getCount(), is(2l));

		term = facet.getTerms().get(6);
		assertThat(term.getTerm(), is(RIZWAN_IDREES));
		assertThat(term.getCount(), is(4l));
		
		assertThat(facet.getTotal(), is(16l));
		assertThat(facet.getOther(), is(0l));
		assertThat(facet.getMissing(), is(1l));
	}

	@Test
	public void shouldReturnFacetedYearsAndFacetedAuthorsForGivenQuery() {

		// given
		String numberFacetName = "fAuthors";
		String stringFacetName = "fyears";
		SearchQuery searchQuery = new NativeSearchQueryBuilder().withQuery(matchAllQuery())
				.withFacet(new TermFacetRequestBuilder(numberFacetName).fields("publishedYears").ascTerm().build())
				.withFacet(new TermFacetRequestBuilder(stringFacetName).fields("authors.untouched").ascTerm().build())
				.build();
		// when
		FacetedPage<ArticleEntity> result = elasticsearchTemplate.queryForPage(searchQuery, ArticleEntity.class);
		// then
		assertThat(result.getNumberOfElements(), is(equalTo(4)));

		TermResult numberFacet = (TermResult) result.getFacet(numberFacetName);
		assertThat(numberFacet.getTerms().size(), is(equalTo(3)));

		Term numberTerm = numberFacet.getTerms().get(0);
		assertThat(numberTerm.getTerm(), is(Long.toString(YEAR_2000)));
		assertThat(numberTerm.getCount(), is(3l));

		numberTerm = numberFacet.getTerms().get(1);
		assertThat(numberTerm.getTerm(), is(Long.toString(YEAR_2001)));
		assertThat(numberTerm.getCount(), is(2l));

		numberTerm = numberFacet.getTerms().get(2);
		assertThat(numberTerm.getTerm(), is(Long.toString(YEAR_2002)));
		assertThat(numberTerm.getCount(), is(1l));

		TermResult stringFacet = (TermResult) result.getFacet(stringFacetName);
		Term stringTerm = stringFacet.getTerms().get(0);
		assertThat(stringTerm.getTerm(), is(ARTUR_KONCZAK));
		assertThat(stringTerm.getCount(), is(3l));

		stringTerm = stringFacet.getTerms().get(1);
		assertThat(stringTerm.getTerm(), is(JONATHAN_YAN));
		assertThat(stringTerm.getCount(), is(1l));

		stringTerm = stringFacet.getTerms().get(2);
		assertThat(stringTerm.getTerm(), is(MOHSIN_HUSEN));
		assertThat(stringTerm.getCount(), is(2l));

		stringTerm = stringFacet.getTerms().get(3);
		assertThat(stringTerm.getTerm(), is(RIZWAN_IDREES));
		assertThat(stringTerm.getCount(), is(4l));
		
		assertThat(stringFacet.getTotal(), is(4l));
		assertThat(stringFacet.getOther(), is(0l));
		assertThat(stringFacet.getMissing(), is(0l));
	}


	@Test(expected = UnsupportedOperationException.class)
	public void shouldThrowExceptionForNativeFacets() {

		// given
		String facetName = "fyears";
		SearchQuery searchQuery = new NativeSearchQueryBuilder().withQuery(matchAllQuery())
				.withFacet(new NativeFacetRequest()).build();
		// when
		FacetedPage<ArticleEntity> result = elasticsearchTemplate.queryForPage(searchQuery, ArticleEntity.class);
		// then
		assertThat(result.getNumberOfElements(), is(equalTo(4)));

		TermResult facet = (TermResult) result.getFacet(facetName);
		assertThat(facet.getTerms().size(), is(equalTo(3)));

		Term term = facet.getTerms().get(0);
		assertThat(term.getTerm(), is(Long.toString(YEAR_2000)));
		assertThat(term.getCount(), is(3l));

		term = facet.getTerms().get(1);
		assertThat(term.getTerm(), is(Long.toString(YEAR_2001)));
		assertThat(term.getCount(), is(2l));

		term = facet.getTerms().get(2);
		assertThat(term.getTerm(), is(Long.toString(YEAR_2002)));
		assertThat(term.getCount(), is(1l));
		
		assertThat(facet.getTotal(), is(6l));
		assertThat(facet.getOther(), is(0l));
		assertThat(facet.getMissing(), is(1l));
	}

	@Test
	public void shouldFilterResultByRegexForGivenQuery() {
		// given
		String facetName = "regex_authors";
		SearchQuery searchQuery = new NativeSearchQueryBuilder().withQuery(matchAllQuery())
				.withFacet(new TermFacetRequestBuilder(facetName).applyQueryFilter().fields("authors.untouched").regex("Art.*").build()).build();
		// when
		FacetedPage<ArticleEntity> result = elasticsearchTemplate.queryForPage(searchQuery, ArticleEntity.class);
		// then
		assertThat(result.getNumberOfElements(), is(equalTo(4)));

		TermResult facet = (TermResult) result.getFacet(facetName);

		assertThat(facet.getTerms().size(), is(1));

		Term term = facet.getTerms().get(0);
		assertThat(term.getTerm(), is(ARTUR_KONCZAK));
		assertThat(term.getCount(), is(3l));

		assertThat(facet.getTotal(), is(1l));
		assertThat(facet.getOther(), is(0l));
		assertThat(facet.getMissing(), is(0l));
	}

	@Test
	public void shouldReturnAllTermsForGivenQuery() {
		// given

		String facetName = "all_authors";
		SearchQuery searchQuery = new NativeSearchQueryBuilder().withQuery(matchAllQuery())
				.withFacet(new TermFacetRequestBuilder(facetName).applyQueryFilter().fields("authors.untouched").allTerms().build()).build();
		// when
		FacetedPage<ArticleEntity> result = elasticsearchTemplate.queryForPage(searchQuery, ArticleEntity.class);
		// then
		assertThat(result.getNumberOfElements(), is(equalTo(4)));

		TermResult facet = (TermResult) result.getFacet(facetName);

		assertThat(facet.getTerms().size(), is(4));
		
		assertThat(facet.getTotal(), is(4l));
		assertThat(facet.getOther(), is(0l));
		assertThat(facet.getMissing(), is(0l));
	}


	@Test
	public void shouldReturnRangeFacetForGivenQuery() {
		// given
		String facetName = "rangeYears";
		SearchQuery searchQuery = new NativeSearchQueryBuilder().withQuery(matchAllQuery())
				.withFacet(
						new RangeFacetRequestBuilder(facetName).field(PUBLISHED_YEARS)
								.to(YEAR_2000).range(YEAR_2000, YEAR_2002).from(YEAR_2002).build()
				).build();
		// when
		FacetedPage<ArticleEntity> result = elasticsearchTemplate.queryForPage(searchQuery, ArticleEntity.class);
		// then
		assertThat(result.getNumberOfElements(), is(equalTo(4)));

		RangeResult facet = (RangeResult) result.getFacet(facetName);
		assertThat(facet.getRanges().size(), is(equalTo(3)));

		Range range = facet.getRanges().get(0);
		assertThat(range.getFrom(), is(Double.NEGATIVE_INFINITY));
		assertThat(range.getTo(), is((double) YEAR_2000));
		assertThat(range.getCount(), is(0L));
		assertThat(range.getTotal(), is(0.0));

		range = facet.getRanges().get(1);
		assertThat(range.getFrom(), is((double) YEAR_2000));
		assertThat(range.getTo(), is((double) YEAR_2002));
		assertThat(range.getCount(), is(3L));
		assertThat(range.getTotal(), is(12004.0));

		range = facet.getRanges().get(2);
		assertThat(range.getFrom(), is((double) YEAR_2002));
		assertThat(range.getTo(), is(Double.POSITIVE_INFINITY));
		assertThat(range.getCount(), is(1L));
		assertThat(range.getTotal(), is(6003.0));
	}

	@Test
	public void shouldReturnKeyValueRangeFacetForStringValuesInGivenQuery() {
		// given
		String facetName = "rangeScoreOverYears";
		SearchQuery searchQuery = new NativeSearchQueryBuilder().withQuery(matchAllQuery())
				.withFacet(
						new RangeFacetRequestBuilder(facetName).fields(PUBLISHED_YEARS, "score")
								.to(YEAR_2000).range(YEAR_2000, YEAR_2002).from(YEAR_2002).build()
				).build();
		// when
		FacetedPage<ArticleEntity> result = elasticsearchTemplate.queryForPage(searchQuery, ArticleEntity.class);
		// then
		assertThat(result.getNumberOfElements(), is(equalTo(4)));

		RangeResult facet = (RangeResult) result.getFacet(facetName);
		assertThat(facet.getRanges().size(), is(equalTo(3)));

		Range range = facet.getRanges().get(0);
		assertThat(range.getFrom(), is(Double.NEGATIVE_INFINITY));
		assertThat(range.getTo(), is((double) YEAR_2000));
		assertThat(range.getCount(), is(0L));
		assertThat(range.getTotal(), is(0.0));

		range = facet.getRanges().get(1);
		assertThat(range.getFrom(), is((double) YEAR_2000));
		assertThat(range.getTo(), is((double) YEAR_2002));
		assertThat(range.getCount(), is(3L));
		assertThat(range.getTotal(), is(90.0));

		range = facet.getRanges().get(2);
		assertThat(range.getFrom(), is((double) YEAR_2002));
		assertThat(range.getTo(), is(Double.POSITIVE_INFINITY));
		assertThat(range.getCount(), is(1L));
		assertThat(range.getTotal(), is(40.0));
	}

	@Test
	public void shouldReturnStatisticalFacetForGivenQuery() {
		// given
		String facetName = "statPublishedYear";
		SearchQuery searchQuery = new NativeSearchQueryBuilder().withQuery(matchAllQuery())
				.withFacet(new StatisticalFacetRequestBuilder(facetName).field(PUBLISHED_YEARS).build()
				).build();
		// when
		FacetedPage<ArticleEntity> result = elasticsearchTemplate.queryForPage(searchQuery, ArticleEntity.class);
		// then
		assertThat(result.getNumberOfElements(), is(equalTo(4)));

		StatisticalResult facet = (StatisticalResult) result.getFacet(facetName);
		assertThat(facet.getCount(), is(equalTo(6L)));
		assertThat(facet.getMax(), is(equalTo(2002.0)));
		assertThat(facet.getMin(), is(equalTo(2000.0)));
	}

	@Test
	public void shouldReturnHistogramFacetForGivenQuery() {
		// given
		String facetName = "numberPublicationPerYear";
		SearchQuery searchQuery = new NativeSearchQueryBuilder().withQuery(matchAllQuery())
				.withFacet(new HistogramFacetRequestBuilder(facetName).field(PUBLISHED_YEARS).interval(1).build()
				).build();
		// when
		FacetedPage<ArticleEntity> result = elasticsearchTemplate.queryForPage(searchQuery, ArticleEntity.class);
		// then
		assertThat(result.getNumberOfElements(), is(equalTo(4)));

		HistogramResult facet = (HistogramResult) result.getFacet(facetName);
		assertThat(facet.getIntervalUnit().size(), is(equalTo(3)));

		IntervalUnit unit = facet.getIntervalUnit().get(0);
		assertThat(unit.getKey(), is(Long.valueOf(YEAR_2000)));
		assertThat(unit.getCount(), is(3L));

		unit = facet.getIntervalUnit().get(1);
		assertThat(unit.getKey(), is(Long.valueOf(YEAR_2001)));
		assertThat(unit.getCount(), is(2L));

		unit = facet.getIntervalUnit().get(2);
		assertThat(unit.getKey(), is(Long.valueOf(YEAR_2002)));
		assertThat(unit.getCount(), is(1L));
	}

	@Test
	public void shouldNotThrowExceptionForNoFacets()
	{
		SearchQuery searchQuery = new NativeSearchQueryBuilder().withQuery(matchAllQuery()).build();
		AggregatedPage<ArticleEntity> result = elasticsearchTemplate.queryForPage(searchQuery, ArticleEntity.class);

		assertThat(result.hasFacets(), is(false));
	}
}

