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

package io.barracks.updateservice.model;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.barracks.updateservice.exception.UnknownUpdateStatusException;
import io.barracks.updateservice.model.utils.UpdateStatusDeserializer;
import io.barracks.updateservice.model.utils.UpdateStatusSerializer;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Arrays;

@Getter
@AllArgsConstructor(access = AccessLevel.PACKAGE)
@JsonSerialize(using = UpdateStatusSerializer.class)
@JsonDeserialize(using = UpdateStatusDeserializer.class)
public enum UpdateStatus {

    DRAFT("draft"),
    PUBLISHED("published"),
    ARCHIVED("archived"),
    SCHEDULED("scheduled");

    private String name;

    public static UpdateStatus fromName(String statusName) {
        return Arrays.stream(UpdateStatus.values())
                .filter(status -> statusName.equals(status.getName()))
                .findFirst()
                .orElseThrow(() -> new UnknownUpdateStatusException(statusName));
    }
}
