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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.barracks.updateservice.model.UpdateStatus;
import io.barracks.updateservice.model.UpdateStatusCompatibility;
import org.junit.Test;

import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static org.assertj.core.api.Assertions.assertThat;

public class UpdateStatusCompatibilitySerializerTest {
    @Test
    public void serializer_shouldSerializeAllNamesCorrectly() {
        // Given
        UpdateStatusCompatibility compatibility = UpdateStatusCompatibility.builder()
                .status(UpdateStatus.DRAFT)
                .compatibilities(Arrays.asList(UpdateStatus.PUBLISHED, UpdateStatus.ARCHIVED, UpdateStatus.SCHEDULED))
                .build();

        // When
        JsonNode result = new ObjectMapper().valueToTree(compatibility);
        assertThat(result.get("name").textValue()).isEqualTo(compatibility.getStatus().getName());
        assertThat(StreamSupport.stream(result.get("compatibleStatus").spliterator(), false)
                .map(JsonNode::textValue)
                .collect(Collectors.toList())
        ).containsExactlyElementsOf(
                compatibility.getCompatibilities().stream()
                        .map(UpdateStatus::getName)
                        .collect(Collectors.toList())
        );
    }
}
