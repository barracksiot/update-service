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

import java.util.Collections;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertFalse;

public class UpdateRevisionCreateUpdateListTest extends UpdateRevisionBaseTest {

    @Test
    public void getUpdates_whenTheCollectionIsEmpty_shouldReturnNoUpdates() throws Exception {
        // Given
        final String userId = UUID.randomUUID().toString();

        // When
        final ResponseEntity<JSONObject> responseEntity = getLatestUpdatesForUser(userId);

        // Then
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertFalse(responseEntity.getBody().containsKey("_embedded"));
    }

    @Test
    public void getUpdateByUuid_whenUpdateDoesNotExist_shouldReturnNotFound() {
        // Given
        final String userId = UUID.randomUUID().toString();
        final String uuid = UUID.randomUUID().toString();

        // When
        final ResponseEntity<JSONObject> responseEntity = getUpdateByUuid(userId, uuid);

        // Then
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    public void createUpdate_withValidUpdate_shouldReturnThatUpdate() throws Exception {
        // Given
        final JSONObject request = getBaseUpdateRevisionRequest();
        final JSONObject expected = duplicate(request);

        // When
        final ResponseEntity<JSONObject> responseEntity = createUpdate(request);
        final JSONObject result = responseEntity.getBody();
        copyUpdateExpectedFields(expected, result);

        // Then
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(result).isEqualTo(expected);
        assertThat(result.getAsNumber(REVISION_ID_KEY)).isEqualTo(1);
        assertThat(result.getAsString(CREATION_DATE_KEY)).isNotNull();
    }

    @Test
    public void createUpdate_withNoSegmentId_shouldReturnThatUpdate() throws Exception {
        // Given
        final String userId = UUID.randomUUID().toString();
        final JSONObject request = getBaseUpdateRevisionRequest();
        request.replace(USER_ID_KEY, userId);
        request.remove(SEGMENT_ID_KEY);
        final JSONObject expected = duplicate(request);

        // When
        final ResponseEntity<JSONObject> responseEntity = createUpdate(request);
        final JSONObject result = responseEntity.getBody();
        copyUpdateExpectedFields(expected, result);

        // Then
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(result).isEqualTo(expected);
    }

    @Test
    public void createUpdate_withStatusNotDraft_shouldReturnThatUpdateAsDraft() throws Exception {
        // Given
        final String userId = UUID.randomUUID().toString();
        final JSONObject request = getBaseUpdateRevisionRequest();
        request.replace(USER_ID_KEY, userId);
        request.replace(STATUS_KEY, "archived");
        final JSONObject expected = duplicate(request);
        expected.replace(STATUS_KEY, "draft");

        // When
        final ResponseEntity<JSONObject> responseEntity = createUpdate(request);
        final JSONObject result = responseEntity.getBody();
        copyUpdateExpectedFields(expected, result);

        // Then
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(result).isEqualTo(expected);
        assertThat(result.getAsNumber(REVISION_ID_KEY)).isEqualTo(1);
    }

    @Test
    public void getByUuid_whenUpdateExists_shouldReturnIt() throws Exception {
        // Given
        final String userId = UUID.randomUUID().toString();
        final JSONObject request = getBaseUpdateRevisionRequest();
        request.replace(USER_ID_KEY, userId);
        final JSONObject expected = duplicate(request);
        final JSONObject result = createUpdate(request).getBody();
        copyUpdateExpectedFields(expected, result);

        // When
        final ResponseEntity<JSONObject> responseEntity = getUpdateByUuid(userId, request.getAsString(UUID_KEY));

        // Then
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result).isEqualTo(expected);
    }

    @Test
    public void getUpdates_whenTheCollectionContainsOneUpdate_shouldReturnThatUpdate() throws Exception {
        // Given
        final String userId = UUID.randomUUID().toString();
        final JSONObject request = getBaseUpdateRevisionRequest();
        request.replace(USER_ID_KEY, userId);
        final JSONObject created = createUpdate(request).getBody();

        // When
        final ResponseEntity<JSONObject> responseEntity = getLatestUpdatesForUser(userId);

        // Then
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(getEmbeddedUpdateInfos(responseEntity.getBody())).containsExactly(created);
    }

    @Test
    public void reviseUpdate_withValidRevision_shouldReturnRevisedUpdate() throws Exception {
        // Given
        final String userId = UUID.randomUUID().toString();
        final JSONObject request = getBaseUpdateRevisionRequest();
        request.replace(USER_ID_KEY, userId);
        final JSONObject created = createUpdate(request).getBody();
        request.replace(NAME_KEY, UUID.randomUUID().toString());
        final JSONObject expected = duplicate(request);
        final ResponseEntity<JSONObject> revised = reviseUpdate(userId, created.getAsString(UUID_KEY), request);

        // When
        final JSONObject result = revised.getBody();
        copyUpdateExpectedFields(expected, result);

        // Then
        assertThat(revised.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result).isEqualTo(expected);
        assertThat(result.getAsNumber(REVISION_ID_KEY)).isEqualTo(2);
        assertThat(result.getAsString(CREATION_DATE_KEY)).isNotNull();
    }

    @Test
    public void getUpdates_whenTheCollectionContainsOneUpdateAndMultipleVersions_shouldReturnLatest() throws Exception {
        // Given
        final String userId = UUID.randomUUID().toString();
        final JSONObject request = getBaseUpdateRevisionRequest();
        request.replace(USER_ID_KEY, userId);
        final JSONObject created = createUpdate(request).getBody();
        request.replace(NAME_KEY, UUID.randomUUID().toString());
        final JSONObject revised = reviseUpdate(userId, created.getAsString(UUID_KEY), request).getBody();

        // When
        ResponseEntity<JSONObject> responseEntity = getLatestUpdatesForUser(userId);

        // Then
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(getEmbeddedUpdateInfos(responseEntity.getBody())).containsExactly(revised);
    }

    @Test
    public void getUpdates_whenMultipleUpdatesAndMultipleRevisions_shouldReturnLatestSortedByDate() throws Exception {
        // Given
        final String userId = UUID.randomUUID().toString();
        final JSONObject request = getBaseUpdateRevisionRequest();
        request.replace(USER_ID_KEY, userId);
        final JSONObject created1 = createUpdate(request).getBody();
        request.replace(STATUS_KEY, "published");
        reviseUpdate(userId, created1.getAsString(UUID_KEY), request);
        request.replace(STATUS_KEY, "archived");
        final JSONObject expected1 = reviseUpdate(userId, created1.getAsString(UUID_KEY), request).getBody();
        request.replace(STATUS_KEY, "draft");
        final JSONObject created2 = createUpdate(request).getBody();
        request.replace(STATUS_KEY, "published");
        final JSONObject expected2 = reviseUpdate(userId, created2.getAsString(UUID_KEY), request).getBody();

        // When
        ResponseEntity<JSONObject> responseEntity = getLatestUpdatesForUser(userId);

        // Then
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(getEmbeddedUpdateInfos(responseEntity.getBody())).containsExactly(expected2, expected1);
    }

    @Test
    public void getUpdates_whenMultipleUpdatesAndMultipleRevisionsSortedByUnknownKey_shouldReturnLatestSortedByDate() throws Exception {
        // Given
        final String userId = UUID.randomUUID().toString();
        final JSONObject request = getBaseUpdateRevisionRequest();
        request.replace(USER_ID_KEY, userId);
        final JSONObject created1 = createUpdate(request).getBody();
        request.replace(STATUS_KEY, "published");
        reviseUpdate(userId, created1.getAsString(UUID_KEY), request);
        request.replace(STATUS_KEY, "archived");
        final JSONObject expected1 = reviseUpdate(userId, created1.getAsString(UUID_KEY), request).getBody();
        request.replace(STATUS_KEY, "draft");
        final JSONObject created2 = createUpdate(request).getBody();
        request.replace(STATUS_KEY, "published");
        final JSONObject expected2 = reviseUpdate(userId, created2.getAsString(UUID_KEY), request).getBody();

        // When
        ResponseEntity<JSONObject> responseEntity = getLatestUpdatesForUserWithParameters(userId, Collections.singletonList("sort"), "whatever,asc");

        // Then
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(getEmbeddedUpdateInfos(responseEntity.getBody())).containsExactly(expected2, expected1);
    }

    @Test
    public void getUpdates_whenMultipleUpdatesAndMultipleRevisionsSortedByRevisionId_shouldReturnLatestSortedByDate() throws Exception {
        // Given
        final String userId = UUID.randomUUID().toString();
        final JSONObject request = getBaseUpdateRevisionRequest();
        request.replace(USER_ID_KEY, userId);
        final JSONObject created1 = createUpdate(request).getBody();
        request.replace(STATUS_KEY, "published");
        reviseUpdate(userId, created1.getAsString(UUID_KEY), request);
        request.replace(STATUS_KEY, "archived");
        final JSONObject expected1 = reviseUpdate(userId, created1.getAsString(UUID_KEY), request).getBody();
        request.replace(STATUS_KEY, "draft");
        final JSONObject created2 = createUpdate(request).getBody();
        request.replace(STATUS_KEY, "published");
        final JSONObject expected2 = reviseUpdate(userId, created2.getAsString(UUID_KEY), request).getBody();

        // When
        ResponseEntity<JSONObject> responseEntity = getLatestUpdatesForUserWithParameters(userId, Collections.singletonList("sort"), "revisionId,desc");

        // Then
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(getEmbeddedUpdateInfos(responseEntity.getBody())).containsExactly(expected1, expected2);
    }

    @Test
    public void reviseUpdate_withValidScheduling_shouldReturnScheduledUpdate() throws Exception {
        // Given
        final String userId = UUID.randomUUID().toString();
        final JSONObject request = getBaseUpdateRevisionRequest();
        request.replace(USER_ID_KEY, userId);
        final JSONObject created = createUpdate(request).getBody();
        request.replace(STATUS_KEY, "scheduled");
        request.put(SCHEDULED_DATE_KEY, "1986-02-27T10:00:05.666Z");
        final JSONObject expected = duplicate(request);
        final ResponseEntity<JSONObject> revised = reviseUpdate(userId, created.getAsString(UUID_KEY), request);

        // When
        final JSONObject result = revised.getBody();
        copyUpdateExpectedFields(expected, result);

        // Then
        assertThat(revised.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result).isEqualTo(expected);
        assertThat(result.getAsNumber(REVISION_ID_KEY)).isEqualTo(2);
    }

    @Test
    public void reviseUpdate_withStatusDraftValidScheduledDate_shouldIgnoreScheduledUpdate() throws Exception {
        // Given
        final String userId = UUID.randomUUID().toString();
        final JSONObject request = getBaseUpdateRevisionRequest();
        request.replace(USER_ID_KEY, userId);
        final JSONObject created = createUpdate(request).getBody();
        request.replace(STATUS_KEY, "draft");
        request.put(SCHEDULED_DATE_KEY, "1986-02-27T10:00:05.666Z");
        final JSONObject expected = duplicate(request);
        expected.remove(SCHEDULED_DATE_KEY);
        final ResponseEntity<JSONObject> revised = reviseUpdate(userId, created.getAsString(UUID_KEY), request);

        // When
        final JSONObject result = revised.getBody();
        copyUpdateExpectedFields(expected, result);

        // Then
        assertThat(revised.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result).isEqualTo(expected);
        assertFalse(result.containsKey(SCHEDULED_DATE_KEY));
    }

    @Test
    public void reviseUpdate_withStatusPublishedValidScheduledDate_shouldIgnoreScheduledUpdate() throws Exception {
        // Given
        final String userId = UUID.randomUUID().toString();
        final JSONObject request = getBaseUpdateRevisionRequest();
        request.replace(USER_ID_KEY, userId);
        final JSONObject created = createUpdate(request).getBody();
        request.replace(STATUS_KEY, "published");
        request.put(SCHEDULED_DATE_KEY, "1986-02-27T10:00:05.666Z");
        final JSONObject expected = duplicate(request);
        expected.remove(SCHEDULED_DATE_KEY);
        final ResponseEntity<JSONObject> revised = reviseUpdate(userId, created.getAsString(UUID_KEY), request);

        // When
        final JSONObject result = revised.getBody();
        copyUpdateExpectedFields(expected, result);

        // Then
        assertThat(revised.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result).isEqualTo(expected);
        assertFalse(result.containsKey(SCHEDULED_DATE_KEY));
    }

    @Test
    public void reviseUpdate_withStatusArchivedValidScheduledDate_shouldIgnoreScheduledUpdate() throws Exception {
        // Given
        final String userId = UUID.randomUUID().toString();
        final JSONObject request = getBaseUpdateRevisionRequest();
        request.replace(USER_ID_KEY, userId);
        final JSONObject created = createUpdate(request).getBody();
        request.replace(STATUS_KEY, "published");
        reviseUpdate(userId, created.getAsString(UUID_KEY), request);
        request.replace(STATUS_KEY, "archived");
        request.put(SCHEDULED_DATE_KEY, "1986-02-27T10:00:05.666Z");
        final JSONObject expected = duplicate(request);
        expected.remove(SCHEDULED_DATE_KEY);
        final ResponseEntity<JSONObject> revised = reviseUpdate(userId, created.getAsString(UUID_KEY), request);

        // When
        final JSONObject result = revised.getBody();
        copyUpdateExpectedFields(expected, result);

        // Then
        assertThat(revised.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result).isEqualTo(expected);
        assertFalse(result.containsKey(SCHEDULED_DATE_KEY));
    }

    @Test
    public void getByUuid_whenUpdateWasRevised_shouldReturnLatest() throws Exception {
        // Given
        final String userId = UUID.randomUUID().toString();
        final JSONObject request = getBaseUpdateRevisionRequest();
        request.replace(USER_ID_KEY, userId);
        final JSONObject created = createUpdate(request).getBody();
        final String updateUuid = created.getAsString(UUID_KEY);
        request.replace(STATUS_KEY, "published");
        final JSONObject expected = reviseUpdate(userId, updateUuid, request).getBody();

        // When
        final ResponseEntity<JSONObject> responseEntity = getUpdateByUuid(userId, updateUuid);

        // Then
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(responseEntity.getBody()).isEqualTo(expected);
    }
}