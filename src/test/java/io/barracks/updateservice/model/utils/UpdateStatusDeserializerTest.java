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

package io.barracks.updateservice.model.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.barracks.updateservice.exception.UnknownUpdateStatusException;
import io.barracks.updateservice.model.UpdateStatus;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class UpdateStatusDeserializerTest {
    private ObjectMapper mapper;

    @Before
    public void setUp() {
        mapper = new ObjectMapper();
    }

    @Test
    public void testDeserializeArchivedStatus() throws IOException {
        // Given
        final String serializedStatus = "\"archived\"";

        // When
        UpdateStatus status = mapper.readValue(serializedStatus, UpdateStatus.class);

        // Then
        assertNotNull(status);
        assertEquals(UpdateStatus.ARCHIVED, status);
    }

    @Test
    public void testDeserializeDraftStatus() throws IOException {
        final String serializedStatus = "\"draft\"";

        UpdateStatus status = mapper.readValue(serializedStatus, UpdateStatus.class);

        assertNotNull(status);
        assertEquals(UpdateStatus.DRAFT, status);
    }

    @Test
    public void testDeserializePublishedStatus() throws IOException {
        final String serializedStatus = "\"published\"";

        UpdateStatus status = mapper.readValue(serializedStatus, UpdateStatus.class);

        assertNotNull(status);
        assertEquals(UpdateStatus.PUBLISHED, status);
    }

    @Test
    public void testDeserializeScheduledStatus() throws IOException {
        final String serializedStatus = "\"scheduled\"";

        UpdateStatus status = mapper.readValue(serializedStatus, UpdateStatus.class);

        assertNotNull(status);
        assertEquals(UpdateStatus.SCHEDULED, status);
    }

    @Test(expected = UnknownUpdateStatusException.class)
    public void testDeserializeInvalidStatus() throws IOException {
        final String serializedStatus = "\"youpla\"";

        mapper.readValue(serializedStatus, UpdateStatus.class);
    }

}
