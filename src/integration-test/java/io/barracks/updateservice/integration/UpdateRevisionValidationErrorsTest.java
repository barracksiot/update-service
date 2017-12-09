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

public class UpdateRevisionValidationErrorsTest extends UpdateRevisionBaseTest {
    @Test
    public void createUpdate_whenRequestIsMissingUserId_shouldReturn400() throws Exception {
        // Given
        final JSONObject request = getBaseUpdateRevisionRequest();
        request.remove(USER_ID_KEY);

        // When
        final ResponseEntity<JSONObject> responseEntity = createUpdate(request);

        // Then
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    public void createUpdate_whenRequestUserIdIsBlank_shouldReturn400() throws Exception {
        // Given
        final JSONObject request = getBaseUpdateRevisionRequest();
        request.replace(USER_ID_KEY, "");

        // When
        final ResponseEntity<JSONObject> responseEntity = createUpdate(request);

        // Then
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    public void createUpdate_whenRequestIsMissingName_shouldReturn400() throws Exception {
        // Given
        final JSONObject request = getBaseUpdateRevisionRequest();
        request.remove(NAME_KEY);

        // When
        final ResponseEntity<JSONObject> responseEntity = createUpdate(request);

        // Then
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    public void createUpdate_whenRequestNameIsBlank_shouldReturn400() throws Exception {
        // Given
        final JSONObject request = getBaseUpdateRevisionRequest();
        request.replace(NAME_KEY, "");

        // When
        final ResponseEntity<JSONObject> responseEntity = createUpdate(request);

        // Then
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    public void createUpdate_whenRequestIsMissingPackageId_shouldReturn400() throws Exception {
        // Given
        final JSONObject request = getBaseUpdateRevisionRequest();
        request.remove(PACKAGE_ID_KEY);

        // When
        final ResponseEntity<JSONObject> responseEntity = createUpdate(request);

        // Then
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    public void createUpdate_whenRequestPackageIdIsBlank_shouldReturn400() throws Exception {
        // Given
        final JSONObject request = getBaseUpdateRevisionRequest();
        request.replace(PACKAGE_ID_KEY, "");

        // When
        final ResponseEntity<JSONObject> responseEntity = createUpdate(request);

        // Then
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    public void reviseUpdate_whenScheduledWithoutDate_shouldReturn400() throws Exception {
        // Given
        final String userId = UUID.randomUUID().toString();
        final JSONObject request = getBaseUpdateRevisionRequest();
        final JSONObject created = createUpdate(request).getBody();
        request.replace(STATUS_KEY, "scheduled");
        request.remove(SCHEDULED_DATE_KEY);

        // When
        final ResponseEntity<JSONObject> responseEntity = reviseUpdate(userId, created.getAsString(UUID_KEY), request);

        // Then
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

}
