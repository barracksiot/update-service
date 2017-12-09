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

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import io.barracks.updateservice.model.UpdateStatus;
import org.junit.Test;

import java.io.IOException;
import java.io.StringWriter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class UpdateStatusSerializerTest {

    @Test
    public void testSerializeArchivedStatus() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonGenerator generator = new JsonFactory().createGenerator(stringWriter);
        UpdateStatusSerializer serializer = new UpdateStatusSerializer();

        serializer.serialize(UpdateStatus.ARCHIVED, generator, null);

        generator.flush();
        StringBuilder builder = new StringBuilder();
        String serializedStatus = stringWriter.toString();
        builder.append(serializedStatus);
        final String jsonString = builder.toString();

        assertNotNull(jsonString);
        assertEquals("\"archived\"", jsonString);
    }

    @Test
    public void testSerializeDraftStatus() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonGenerator generator = new JsonFactory().createGenerator(stringWriter);
        UpdateStatusSerializer serializer = new UpdateStatusSerializer();

        serializer.serialize(UpdateStatus.DRAFT, generator, null);

        generator.flush();
        StringBuilder builder = new StringBuilder();
        String serializedStatus = stringWriter.toString();
        builder.append(serializedStatus);
        final String jsonString = builder.toString();

        assertNotNull(jsonString);
        assertEquals("\"draft\"", jsonString);
    }

    @Test
    public void testSerializePublishedStatus() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonGenerator generator = new JsonFactory().createGenerator(stringWriter);
        UpdateStatusSerializer serializer = new UpdateStatusSerializer();

        serializer.serialize(UpdateStatus.PUBLISHED, generator, null);

        generator.flush();
        StringBuilder builder = new StringBuilder();
        String serializedStatus = stringWriter.toString();
        builder.append(serializedStatus);
        final String jsonString = builder.toString();

        assertNotNull(jsonString);
        assertEquals("\"published\"", jsonString);
    }

    @Test
    public void testSerializeScheduledStatus() throws IOException {
        StringWriter stringWriter = new StringWriter();
        JsonGenerator generator = new JsonFactory().createGenerator(stringWriter);
        UpdateStatusSerializer serializer = new UpdateStatusSerializer();

        serializer.serialize(UpdateStatus.SCHEDULED, generator, null);

        generator.flush();
        StringBuilder builder = new StringBuilder();
        String serializedStatus = stringWriter.toString();
        builder.append(serializedStatus);
        final String jsonString = builder.toString();

        assertNotNull(jsonString);
        assertEquals("\"scheduled\"", jsonString);
    }

}
