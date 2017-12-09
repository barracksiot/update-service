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

import net.minidev.json.JSONObject;
import org.junit.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

public class LatestPublishedUpdateRevisionBySegmentTest extends UpdateRevisionBaseTest {
    @Test
    public void getUpdateForSegment_whenNoUpdateInSegment_shouldReturnNoContent() {
        // Given
        final String userId = UUID.randomUUID().toString();
        final String segmentId = UUID.randomUUID().toString();

        // When
        final ResponseEntity<JSONObject> responseEntity = getUpdateBySegmentId(userId, segmentId);

        // Then
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }

    @Test
    public void getUpdateForOtherSegment_whenNoUpdateInSegment_shouldReturnNoContent() {
        // Given
        final String userId = UUID.randomUUID().toString();

        // When
        final ResponseEntity<JSONObject> responseEntity = getUpdateForOtherSegment(userId);

        // Then
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }

    @Test
    public void getUpdateForSegment_whenNoPublishedUpdate_shouldReturnNoContent() throws Exception {
        // Given
        final String userId = UUID.randomUUID().toString();
        final String segmentId = UUID.randomUUID().toString();
        final JSONObject request = getBaseUpdateRevisionRequest();
        request.replace(USER_ID_KEY, userId);
        request.replace(SEGMENT_ID_KEY, segmentId);
        createUpdate(request);

        // When
        final ResponseEntity<JSONObject> responseEntity = getUpdateBySegmentId(userId, segmentId);

        // Then
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }

    @Test
    public void getUpdateForOtherSegment_whenNoPublishedUpdate_shouldReturnNoContent() throws Exception {
        // Given
        final String userId = UUID.randomUUID().toString();
        final JSONObject request = getBaseUpdateRevisionRequest();
        request.replace(USER_ID_KEY, userId);
        request.remove(SEGMENT_ID_KEY);
        createUpdate(request);

        // When
        final ResponseEntity<JSONObject> responseEntity = getUpdateForOtherSegment(userId);

        // Then
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
    }

    @Test
    public void getUpdateForSegment_whenPublishedUpdate_shouldReturnPublishedUpdate() throws Exception {
        // Given
        final String userId = UUID.randomUUID().toString();
        final String segmentId = UUID.randomUUID().toString();
        final JSONObject request = getBaseUpdateRevisionRequest();
        request.replace(USER_ID_KEY, userId);
        request.replace(SEGMENT_ID_KEY, segmentId);
        final JSONObject created = createUpdate(request).getBody();
        request.replace(STATUS_KEY, "published");
        final JSONObject expected = reviseUpdate(userId, created.getAsString(UUID_KEY), request).getBody();

        // When
        final ResponseEntity<JSONObject> responseEntity = getUpdateBySegmentId(userId, segmentId);

        // Then
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(responseEntity.getBody()).isEqualTo(expected);
    }

    @Test
    public void getUpdateForOtherSegment_whenNoPublishedUpdate_shouldReturnPublishedUpdate() throws Exception {
        // Given
        final String userId = UUID.randomUUID().toString();
        final JSONObject request = getBaseUpdateRevisionRequest();
        request.replace(USER_ID_KEY, userId);
        request.remove(SEGMENT_ID_KEY);
        final JSONObject created = createUpdate(request).getBody();
        request.replace(STATUS_KEY, "published");
        final JSONObject expected = reviseUpdate(userId, created.getAsString(UUID_KEY), request).getBody();

        // When
        final ResponseEntity<JSONObject> responseEntity = getUpdateForOtherSegment(userId);

        // Then
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(responseEntity.getBody()).isEqualTo(expected);
    }

    @Test
    public void getUpdateForSegment_whenMultiplePublishedUpdate_shouldReturnLatestPublishedUpdate() throws Exception {
        // Given
        final String userId = UUID.randomUUID().toString();
        final String segmentId = UUID.randomUUID().toString();
        final JSONObject request = getBaseUpdateRevisionRequest();
        request.replace(USER_ID_KEY, userId);
        request.replace(SEGMENT_ID_KEY, segmentId);
        JSONObject created = createUpdate(request).getBody();
        request.replace(STATUS_KEY, "published");
        reviseUpdate(userId, created.getAsString(UUID_KEY), request);

        request.replace(STATUS_KEY, "draft");
        created = createUpdate(request).getBody();
        request.replace(STATUS_KEY, "published");
        final JSONObject expected = reviseUpdate(userId, created.getAsString(UUID_KEY), request).getBody();

        // When
        final ResponseEntity<JSONObject> responseEntity = getUpdateBySegmentId(userId, segmentId);

        // Then
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(responseEntity.getBody()).isEqualTo(expected);
    }

    @Test
    public void getUpdateForOtherSegment_whenMultiplePublishedUpdate_shouldReturnLatestPublishedUpdate() throws Exception {
        // Given
        final String userId = UUID.randomUUID().toString();
        final JSONObject request = getBaseUpdateRevisionRequest();
        request.replace(USER_ID_KEY, userId);
        request.remove(SEGMENT_ID_KEY);
        JSONObject created = createUpdate(request).getBody();
        request.replace(STATUS_KEY, "published");
        reviseUpdate(userId, created.getAsString(UUID_KEY), request);

        request.replace(STATUS_KEY, "draft");
        created = createUpdate(request).getBody();
        request.replace(STATUS_KEY, "published");
        final JSONObject expected = reviseUpdate(userId, created.getAsString(UUID_KEY), request).getBody();

        // When
        final ResponseEntity<JSONObject> responseEntity = getUpdateForOtherSegment(userId);

        // Then
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(responseEntity.getBody()).isEqualTo(expected);
    }

}