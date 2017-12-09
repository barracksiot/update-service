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
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import org.junit.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;

import java.util.Map;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

public class UpdateStatusResourceTest extends ResourceTest {
    private static final String ALL_STATUS_COMPATIBILITIES_ENDPOINT = "/status";
    private static final String STATUS_COMPATIBILITIES_ENDPOINT = ALL_STATUS_COMPATIBILITIES_ENDPOINT + "/{statusName}";

    @Test
    public void getAllStatusesCompatibilities_shouldReturnAllStatusesWithTheirCompatibilities() throws Exception {
        // Given
        final JSONArray expected = getJsonArrayFromResource("allCompatibilities");

        // When
        final ResponseEntity<JSONArray> responseEntity = getRestTemplate().exchange(
                buildRequestUrl(ALL_STATUS_COMPATIBILITIES_ENDPOINT),
                HttpMethod.GET,
                null,
                JSONArray.class
        );

        // Then
        assertThat(responseEntity.getBody()).isEqualTo(expected);
    }

    @Test
    public void getStatusCompatibilities_forEachStatus_shouldReturnSameCompatibleStatuses() throws Exception {
        // Given
        final JSONArray allCompatibilities = getJsonArrayFromResource("allCompatibilities");
        final Map<String, Object> expected = allCompatibilities.stream().collect(Collectors.toMap(
                node -> ((JSONObject) node).getAsString("name"),
                node -> node
        ));

        // When
        for (Map.Entry<String, Object> status : expected.entrySet()) {
            final ResponseEntity<JSONObject> responseEntity = getRestTemplate().exchange(
                    buildRequestUrl(STATUS_COMPATIBILITIES_ENDPOINT),
                    HttpMethod.GET,
                    null,
                    JSONObject.class,
                    status.getKey()
            );
            assertThat(responseEntity.getBody()).isEqualTo(status.getValue());
        }
    }

    @Test
    public void getStatusCompatibilities_whenInvalidStatusGiven_shouldReturn400BadRequest() {
        // When
        assertThatThrownBy(() -> getRestTemplate().exchange(
                buildRequestUrl(STATUS_COMPATIBILITIES_ENDPOINT),
                HttpMethod.GET,
                null,
                JSONObject.class,
                "plop"
        )).isInstanceOf(HttpClientErrorException.class).hasMessageContaining(HttpStatus.BAD_REQUEST.toString());
    }
}
