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
package org.springframework.data.elasticsearch.core.geo;

import java.util.ArrayList;
import java.util.List;
import org.elasticsearch.common.geo.GeoHashUtils;
import org.elasticsearch.index.query.QueryBuilders;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.query.Criteria;
import org.springframework.data.elasticsearch.core.query.CriteriaQuery;
import org.springframework.data.elasticsearch.core.query.IndexQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.data.geo.Point;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

/**
 * @author Rizwan Idrees
 * @author Mohsin Husen
 * @author Franck Marchand
 * @author Artur Konczak
 *
 * Basic info:
 * latitude  - horizontal lines (equator = 0.0, values -90.0 to 90.0)
 * longitude - vertical lines (Greenwich = 0.0, values -180 to 180)
 * London [lat,lon] = [51.50985,-0.118082] - geohash = gcpvj3448
 * Bouding Box for London = (bbox=-0.489,51.28,0.236,51.686)
 * bbox = left,bottom,right,top
 * bbox = min Longitude , min Latitude , max Longitude , max Latitude
 *
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:elasticsearch-template-test.xml")
public class ElasticsearchTemplateGeoTests {

	@Autowired
	private ElasticsearchTemplate elasticsearchTemplate;

	private void loadClassBaseEntities() {
		elasticsearchTemplate.deleteIndex(AuthorMarkerEntity.class);
		elasticsearchTemplate.createIndex(AuthorMarkerEntity.class);
		elasticsearchTemplate.putMapping(AuthorMarkerEntity.class);
		elasticsearchTemplate.refresh(AuthorMarkerEntity.class);

		List<IndexQuery> indexQueries = new ArrayList<>();
		indexQueries.add(new AuthorMarkerEntityBuilder("1").name("Franck Marchand").location(45.7806d, 3.0875d).buildIndex());
		indexQueries.add(new AuthorMarkerEntityBuilder("2").name("Mohsin Husen").location(51.5171d, 0.1062d).buildIndex());
		indexQueries.add(new AuthorMarkerEntityBuilder("3").name("Rizwan Idrees").location(51.5171d, 0.1062d).buildIndex());
		elasticsearchTemplate.bulkIndex(indexQueries);
		elasticsearchTemplate.refresh(AuthorMarkerEntity.class);
	}

	private void loadAnnotationBaseEntities() {
		elasticsearchTemplate.deleteIndex(LocationMarkerEntity.class);
		elasticsearchTemplate.createIndex(LocationMarkerEntity.class);
		elasticsearchTemplate.putMapping(LocationMarkerEntity.class);
		elasticsearchTemplate.refresh(LocationMarkerEntity.class);

		List<IndexQuery> indexQueries = new ArrayList<>();
		double[] lonLatArray = {0.100000, 51.000000};
		String latLonString = "51.000000, 0.100000";
		String geohash = "u10j46mkfekr";
		GeoHashUtils.stringEncode(0.100000,51.000000);
		LocationMarkerEntity location1 = LocationMarkerEntity.builder()
				.id("1").name("Artur Konczak")
				.locationAsString(latLonString)
				.locationAsArray(lonLatArray)
				.locationAsGeoHash(geohash)
				.build();
		LocationMarkerEntity location2 = LocationMarkerEntity.builder()
				.id("2").name("Mohsin Husen")
				.locationAsString(geohash.substring(0, 8))
				.locationAsArray(lonLatArray)
				.locationAsGeoHash(geohash.substring(0, 8))
				.build();
		LocationMarkerEntity location3 = LocationMarkerEntity.builder()
				.id("3").name("Rizwan Idrees")
				.locationAsString(geohash)
				.locationAsArray(lonLatArray)
				.locationAsGeoHash(geohash)
				.build();
		indexQueries.add(buildIndex(location1));
		indexQueries.add(buildIndex(location2));
		indexQueries.add(buildIndex(location3));

		elasticsearchTemplate.bulkIndex(indexQueries);
		elasticsearchTemplate.refresh(LocationMarkerEntity.class);
	}

	@Test
	public void shouldPutMappingForGivenEntityWithGeoLocation() throws Exception {
		//given
		Class entity = AuthorMarkerEntity.class;
		elasticsearchTemplate.createIndex(entity);
		//when
		assertThat(elasticsearchTemplate.putMapping(entity), is(true));
	}

	@Test
	public void shouldFindAuthorMarkersInRangeForGivenCriteriaQuery() {
		//given
		loadClassBaseEntities();
		CriteriaQuery geoLocationCriteriaQuery = new CriteriaQuery(
				new Criteria("location").within(new GeoPoint(45.7806d, 3.0875d), "20km"));
		//when
		List<AuthorMarkerEntity> geoAuthorsForGeoCriteria = elasticsearchTemplate.queryForList(geoLocationCriteriaQuery, AuthorMarkerEntity.class);

		//then
		assertThat(geoAuthorsForGeoCriteria.size(), is(1));
		assertEquals("Franck Marchand", geoAuthorsForGeoCriteria.get(0).getName());
	}

	@Test
	public void shouldFindSelectedAuthorMarkerInRangeForGivenCriteriaQuery() {
		//given
		loadClassBaseEntities();
		CriteriaQuery geoLocationCriteriaQuery2 = new CriteriaQuery(
				new Criteria("name").is("Mohsin Husen").and("location").within(new GeoPoint(51.5171d, 0.1062d), "20km"));
		//when
		List<AuthorMarkerEntity> geoAuthorsForGeoCriteria2 = elasticsearchTemplate.queryForList(geoLocationCriteriaQuery2, AuthorMarkerEntity.class);

		//then
		assertThat(geoAuthorsForGeoCriteria2.size(), is(1));
		assertEquals("Mohsin Husen", geoAuthorsForGeoCriteria2.get(0).getName());
	}

	@Test
	public void shouldFindStringAnnotatedGeoMarkersInRangeForGivenCriteriaQuery() {
		//given
		loadAnnotationBaseEntities();
		CriteriaQuery geoLocationCriteriaQuery = new CriteriaQuery(
				new Criteria("locationAsString").within(new GeoPoint(51.000000, 0.100000), "1km"));
		//when
		List<LocationMarkerEntity> geoAuthorsForGeoCriteria = elasticsearchTemplate.queryForList(geoLocationCriteriaQuery, LocationMarkerEntity.class);

		//then
		assertThat(geoAuthorsForGeoCriteria.size(), is(1));
	}

	@Test
	public void shouldFindDoubleAnnotatedGeoMarkersInRangeForGivenCriteriaQuery() {
		//given
		loadAnnotationBaseEntities();
		CriteriaQuery geoLocationCriteriaQuery = new CriteriaQuery(
				new Criteria("locationAsArray").within(new GeoPoint(51.001000, 0.10100), "1km"));
		//when
		List<LocationMarkerEntity> geoAuthorsForGeoCriteria = elasticsearchTemplate.queryForList(geoLocationCriteriaQuery, LocationMarkerEntity.class);

		//then
		assertThat(geoAuthorsForGeoCriteria.size(), is(3));
	}

	@Test
	public void shouldFindAnnotatedGeoMarkersInRangeForGivenCriteriaQuery() {
		//given
		loadAnnotationBaseEntities();
		CriteriaQuery geoLocationCriteriaQuery = new CriteriaQuery(
				new Criteria("locationAsArray").within("51.001000, 0.10100", "1km"));
		//when
		List<LocationMarkerEntity> geoAuthorsForGeoCriteria = elasticsearchTemplate.queryForList(geoLocationCriteriaQuery, LocationMarkerEntity.class);

		//then
		assertThat(geoAuthorsForGeoCriteria.size(), is(3));
	}

	@Test
	public void shouldFindAnnotatedGeoMarkersInRangeForGivenCriteriaQueryUsingGeohashLocation() {
		//given
		loadAnnotationBaseEntities();
		CriteriaQuery geoLocationCriteriaQuery = new CriteriaQuery(
				new Criteria("locationAsArray").within("u1044", "3km"));
		//when
		List<LocationMarkerEntity> geoAuthorsForGeoCriteria = elasticsearchTemplate.queryForList(geoLocationCriteriaQuery, LocationMarkerEntity.class);

		//then
		assertThat(geoAuthorsForGeoCriteria.size(), is(3));
	}

	@Test
	public void shouldFindAllMarkersForNativeSearchQuery() {
		//Given
		loadAnnotationBaseEntities();
		NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder().withFilter(QueryBuilders.geoBoundingBoxQuery("locationAsArray").setCorners(52, -1, 50, 1));
		//When
		List<LocationMarkerEntity> geoAuthorsForGeoCriteria = elasticsearchTemplate.queryForList(queryBuilder.build(), LocationMarkerEntity.class);
		//Then
		assertThat(geoAuthorsForGeoCriteria.size(), is(3));
	}

	@Test
	public void shouldFindAuthorMarkersInBoxForGivenCriteriaQueryUsingGeoBox() {
		//given
		loadClassBaseEntities();
		CriteriaQuery geoLocationCriteriaQuery3 = new CriteriaQuery(
				new Criteria("location").boundedBy(
						new GeoBox(new GeoPoint(53.5171d, 0),
								new GeoPoint(49.5171d, 0.2062d))
				)
		);
		//when
		List<AuthorMarkerEntity> geoAuthorsForGeoCriteria3 = elasticsearchTemplate.queryForList(geoLocationCriteriaQuery3, AuthorMarkerEntity.class);

		//then
		assertThat(geoAuthorsForGeoCriteria3.size(), is(2));
		assertThat(geoAuthorsForGeoCriteria3, containsInAnyOrder(hasProperty("name", equalTo("Mohsin Husen")), hasProperty("name", equalTo("Rizwan Idrees"))));
	}

	@Test
	public void shouldFindAuthorMarkersInBoxForGivenCriteriaQueryUsingGeohash() {
		//given
		loadClassBaseEntities();
		CriteriaQuery geoLocationCriteriaQuery3 = new CriteriaQuery(
				new Criteria("location").boundedBy(GeoHashUtils.stringEncode(0, 53.5171d), GeoHashUtils.stringEncode(0.2062d, 49.5171d)));
		//when
		List<AuthorMarkerEntity> geoAuthorsForGeoCriteria3 = elasticsearchTemplate.queryForList(geoLocationCriteriaQuery3, AuthorMarkerEntity.class);

		//then
		assertThat(geoAuthorsForGeoCriteria3.size(), is(2));
		assertThat(geoAuthorsForGeoCriteria3, containsInAnyOrder(hasProperty("name", equalTo("Mohsin Husen")), hasProperty("name", equalTo("Rizwan Idrees"))));
	}

	@Test
	public void shouldFindAuthorMarkersInBoxForGivenCriteriaQueryUsingGeoPoints() {
		//given
		loadClassBaseEntities();
		CriteriaQuery geoLocationCriteriaQuery3 = new CriteriaQuery(
				new Criteria("location").boundedBy(
						new GeoPoint(53.5171d, 0),
						new GeoPoint(49.5171d, 0.2062d))
		);
		//when
		List<AuthorMarkerEntity> geoAuthorsForGeoCriteria3 = elasticsearchTemplate.queryForList(geoLocationCriteriaQuery3, AuthorMarkerEntity.class);

		//then
		assertThat(geoAuthorsForGeoCriteria3.size(), is(2));
		assertThat(geoAuthorsForGeoCriteria3, containsInAnyOrder(hasProperty("name", equalTo("Mohsin Husen")), hasProperty("name", equalTo("Rizwan Idrees"))));
	}

	@Test
	public void shouldFindAuthorMarkersInBoxForGivenCriteriaQueryUsingPoints() {
		//given
		loadClassBaseEntities();
		CriteriaQuery geoLocationCriteriaQuery3 = new CriteriaQuery(
				new Criteria("location").boundedBy(
						new Point(53.5171d, 0),
						new Point(49.5171d, 0.2062d ))
		);
		//when
		List<AuthorMarkerEntity> geoAuthorsForGeoCriteria3 = elasticsearchTemplate.queryForList(geoLocationCriteriaQuery3, AuthorMarkerEntity.class);

		//then
		assertThat(geoAuthorsForGeoCriteria3.size(), is(2));
		assertThat(geoAuthorsForGeoCriteria3, containsInAnyOrder(hasProperty("name", equalTo("Mohsin Husen")), hasProperty("name", equalTo("Rizwan Idrees"))));
	}

	@Test
	public void shouldFindLocationWithGeoHashPrefix() {

		//given
		loadAnnotationBaseEntities();
		NativeSearchQueryBuilder location1 = new NativeSearchQueryBuilder().withFilter(QueryBuilders.geoBoundingBoxQuery("locationAsGeoHash").setCorners("u"));
		NativeSearchQueryBuilder location2 = new NativeSearchQueryBuilder().withFilter(QueryBuilders.geoBoundingBoxQuery("locationAsGeoHash").setCorners("u1"));
		NativeSearchQueryBuilder location3 = new NativeSearchQueryBuilder().withFilter(QueryBuilders.geoBoundingBoxQuery("locationAsGeoHash").setCorners("u10"));
		NativeSearchQueryBuilder location4 = new NativeSearchQueryBuilder().withFilter(QueryBuilders.geoBoundingBoxQuery("locationAsGeoHash").setCorners("u10j"));
		NativeSearchQueryBuilder location5 = new NativeSearchQueryBuilder().withFilter(QueryBuilders.geoBoundingBoxQuery("locationAsGeoHash").setCorners("u10j4"));
		NativeSearchQueryBuilder location11 = new NativeSearchQueryBuilder().withFilter(QueryBuilders.geoBoundingBoxQuery("locationAsGeoHash").setCorners("u10j46mkfek"));

		//when
		List<LocationMarkerEntity> result1 = elasticsearchTemplate.queryForList(location1.build(), LocationMarkerEntity.class);
		List<LocationMarkerEntity> result2 = elasticsearchTemplate.queryForList(location2.build(), LocationMarkerEntity.class);
		List<LocationMarkerEntity> result3 = elasticsearchTemplate.queryForList(location3.build(), LocationMarkerEntity.class);
		List<LocationMarkerEntity> result4 = elasticsearchTemplate.queryForList(location4.build(), LocationMarkerEntity.class);
		List<LocationMarkerEntity> result5 = elasticsearchTemplate.queryForList(location5.build(), LocationMarkerEntity.class);
		List<LocationMarkerEntity> result11 = elasticsearchTemplate.queryForList(location11.build(), LocationMarkerEntity.class);

		//then
		assertThat(result1.size(), is(3));
		assertThat(result2.size(), is(3));
		assertThat(result3.size(), is(3));
		assertThat(result4.size(), is(3));
		assertThat(result5.size(), is(3));
		assertThat(result11.size(), is(2));
	}

	private IndexQuery buildIndex(LocationMarkerEntity result) {
		IndexQuery indexQuery = new IndexQuery();
		indexQuery.setId(result.getId());
		indexQuery.setObject(result);
		return indexQuery;
	}
}
