/*
 * MIT License
 *
 * Copyright (c) 2017 Barracks Inc.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package io.barracks.updateservice.integration;

import io.barracks.updateservice.ResourceTest;
import net.minidev.json.JSONObject;
import net.minidev.json.parser.JSONParser;
import net.minidev.json.parser.ParseException;
import org.junit.Before;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.*;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.*;

public abstract class UpdateRevisionBaseTest extends ResourceTest {
    static final String USER_ID_KEY = "userId";
    static final String UUID_KEY = "uuid";
    static final String REVISION_ID_KEY = "revisionId";
    static final String SEGMENT_ID_KEY = "segmentId";
    static final String NAME_KEY = "name";
    static final String PACKAGE_ID_KEY = "packageId";
    static final String STATUS_KEY = "status";
    static final String SCHEDULED_DATE_KEY = "scheduledDate";
    static final String CREATION_DATE_KEY = "creationDate";

    private static final String LATEST_UPDATE_ENDPOINT = "/updates/latest";
    private static final String UPDATES_ENDPOINT = "/updates";
    private static final String UPDATE_BY_ID_ENDPOINT = UPDATES_ENDPOINT + "/{updateId}";
    private static final String EDIT_UPDATE_ENDPOINT = UPDATES_ENDPOINT + "/{updateId}";

    @Autowired
    private MongoTemplate mongoTemplate;

    @Override
    protected RestTemplate getHateoasRestTemplate() {
        RestTemplate template = super.getHateoasRestTemplate();
        template.setErrorHandler(new DefaultResponseErrorHandler() {
            @Override
            public void handleError(ClientHttpResponse response) throws IOException {

            }
        });
        return template;
    }

    @Override
    protected RestTemplate getRestTemplate() {
        RestTemplate template = super.getRestTemplate();
        template.setErrorHandler(new DefaultResponseErrorHandler() {
            @Override
            public void handleError(ClientHttpResponse response) throws IOException {

            }
        });
        return template;
    }

    private HttpHeaders getJsonRequestHeaders() {
        final HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        headers.add(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
        return headers;
    }

    @Before
    public void setUp() throws Exception {
        for (String collection : mongoTemplate.getCollectionNames()) {
            if (!collection.startsWith("system.")) {
                mongoTemplate.remove(new Query(), collection);
            }
        }
    }

    JSONObject getBaseUpdateRevisionRequest() throws IOException, ParseException {
        return getJsonFromResource(UpdateRevisionBaseTest.class, "createUpdate");
    }

    JSONObject duplicate(JSONObject source) throws ParseException {
        return (JSONObject) new JSONParser(JSONParser.MODE_JSON_SIMPLE).parse(source.toJSONString());
    }

    void copyUpdateExpectedFields(JSONObject request, JSONObject result) {
        request.put(UUID_KEY, result.get(UUID_KEY));
        request.put(REVISION_ID_KEY, result.get(REVISION_ID_KEY));
        request.put(CREATION_DATE_KEY, result.get(CREATION_DATE_KEY));
    }

    @SuppressWarnings("unchecked")
    List<Map<String, Object>> getEmbeddedUpdateInfos(JSONObject result) {
        return (List) ((Map) result.get("_embedded")).get("updateInfos");
    }

    ResponseEntity<JSONObject> createUpdate(JSONObject request) {
        return getRestTemplate().exchange(
                buildRequestUrl(UPDATES_ENDPOINT),
                HttpMethod.POST,
                new HttpEntity<>(request.toJSONString(), getJsonRequestHeaders()),
                JSONObject.class
        );
    }

    ResponseEntity<JSONObject> reviseUpdate(String userId, String uuid, JSONObject request) {
        return getRestTemplate().exchange(
                buildRequestUrl(EDIT_UPDATE_ENDPOINT, Collections.singletonList("userId")),
                HttpMethod.PUT,
                new HttpEntity<>(request.toJSONString(), getJsonRequestHeaders()),
                JSONObject.class,
                uuid,
                userId
        );
    }

    ResponseEntity<JSONObject> getUpdateByUuid(String userId, String uuid) {
        return getRestTemplate().exchange(
                buildRequestUrl(UPDATE_BY_ID_ENDPOINT, Collections.singletonList("userId")),
                HttpMethod.GET,
                new HttpEntity<>(getJsonRequestHeaders()),
                JSONObject.class,
                uuid,
                userId
        );
    }

    ResponseEntity<JSONObject> getUpdateBySegmentId(String userId, String segmentId) {
        return getRestTemplate().exchange(
                buildRequestUrl(LATEST_UPDATE_ENDPOINT, Arrays.asList("userId", "segmentId")),
                HttpMethod.GET,
                new HttpEntity<>(getJsonRequestHeaders()),
                JSONObject.class,
                userId,
                segmentId
        );
    }

    ResponseEntity<JSONObject> getUpdateForOtherSegment(String userId) {
        return getRestTemplate().exchange(
                buildRequestUrl(LATEST_UPDATE_ENDPOINT, Collections.singletonList("userId")),
                HttpMethod.GET,
                new HttpEntity<>(getJsonRequestHeaders()),
                JSONObject.class,
                userId
        );
    }

    ResponseEntity<JSONObject> getLatestUpdatesForUser(String userId) {
        return getLatestUpdates(Collections.singletonList("userId"), userId);
    }

    ResponseEntity<JSONObject> getLatestUpdatesForUserWithParameters(String userId, List<String> parameters, Object... values) {
        List<String> expectedParams = new ArrayList<>(parameters);
        expectedParams.add("userId");
        Object[] expectedValues = Arrays.copyOf(values, values.length + 1);
        expectedValues[values.length] = userId;

        return getLatestUpdates(expectedParams, expectedValues);
    }

    private ResponseEntity<JSONObject> getLatestUpdates(List<String> parameters, Object... uriVariables) {
        return getHateoasRestTemplate().getForEntity(
                buildRequestUrl(UPDATES_ENDPOINT, parameters),
                JSONObject.class,
                uriVariables
        );
    }
}
