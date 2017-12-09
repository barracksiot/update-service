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

package io.barracks.updateservice.utils;

import io.barracks.updateservice.model.UpdateRevision;
import io.barracks.updateservice.model.UpdateStatus;

import java.util.Date;
import java.util.UUID;

import static org.hamcrest.Matchers.*;

public class UpdateRevisionUtils {

    public static UpdateRevision.UpdateRevisionBuilder getNewUpdateRevisionBuilder(String userId) {
        return UpdateRevision.builder()
                .userId(userId)
                .name("Name")
                .description("description")
                .packageId(UUID.randomUUID().toString())
                .segmentId(UUID.randomUUID().toString());
    }

    public static UpdateRevision.UpdateRevisionBuilder getCreatedUpdateRevisionBuilder(String userId) {
        return getNewUpdateRevisionBuilder(userId)
                .uuid(UUID.randomUUID())
                .revisionId(1)
                .creationDate(new Date(11234567890L))
                .status(UpdateStatus.DRAFT);
    }

    public static UpdateRevision getMatcherThatIgnoreUpdateUuidAndCreationDate(UpdateRevision update) {
        return org.mockito.Matchers.argThat(allOf(
                org.hamcrest.Matchers.<UpdateRevision>hasProperty("revisionId", equalTo(update.getRevisionId())),
                org.hamcrest.Matchers.<UpdateRevision>hasProperty("status", equalTo(update.getStatus())),
                org.hamcrest.Matchers.<UpdateRevision>hasProperty("name", equalTo(update.getName())),
                org.hamcrest.Matchers.<UpdateRevision>hasProperty("description", equalTo(update.getDescription())),
                org.hamcrest.Matchers.<UpdateRevision>hasProperty("packageId", equalTo(update.getPackageId())),
                org.hamcrest.Matchers.<UpdateRevision>hasProperty("segmentId", equalTo(update.getSegmentId()))
        ));
    }

    public static UpdateRevision getMatcherThatIgnoreUpdateUuid(UpdateRevision update) {
        return org.mockito.Matchers.argThat(allOf(
                org.hamcrest.Matchers.<UpdateRevision>hasProperty("revisionId", equalTo(update.getRevisionId())),
                org.hamcrest.Matchers.<UpdateRevision>hasProperty("status", equalTo(update.getStatus())),
                org.hamcrest.Matchers.<UpdateRevision>hasProperty("name", equalTo(update.getName())),
                org.hamcrest.Matchers.<UpdateRevision>hasProperty("description", equalTo(update.getDescription())),
                org.hamcrest.Matchers.<UpdateRevision>hasProperty("packageId", equalTo(update.getPackageId())),
                org.hamcrest.Matchers.<UpdateRevision>hasProperty("segmentId", equalTo(update.getSegmentId())),
                org.hamcrest.Matchers.<UpdateRevision>hasProperty("creationDate", equalTo(update.getCreationDate()))
        ));
    }

    public static UpdateRevision getMatcherToVerifyUpdateUuidDifferent(UpdateRevision update) {
        return org.mockito.Matchers.argThat(allOf(
                org.hamcrest.Matchers.<UpdateRevision>hasProperty("uuid", not(update.getUuid())),
                org.hamcrest.Matchers.<UpdateRevision>hasProperty("revisionId", equalTo(update.getRevisionId())),
                org.hamcrest.Matchers.<UpdateRevision>hasProperty("status", equalTo(update.getStatus())),
                org.hamcrest.Matchers.<UpdateRevision>hasProperty("name", equalTo(update.getName())),
                org.hamcrest.Matchers.<UpdateRevision>hasProperty("description", equalTo(update.getDescription())),
                org.hamcrest.Matchers.<UpdateRevision>hasProperty("packageId", equalTo(update.getPackageId())),
                org.hamcrest.Matchers.<UpdateRevision>hasProperty("segmentId", equalTo(update.getSegmentId())),
                org.hamcrest.Matchers.<UpdateRevision>hasProperty("creationDate", equalTo(update.getCreationDate()))
        ));
    }

    public static UpdateRevision getMatcherThatIgnoreCreationDate(UpdateRevision update) {
        return org.mockito.Matchers.argThat(allOf(
                org.hamcrest.Matchers.<UpdateRevision>hasProperty("uuid", equalTo(update.getUuid())),
                org.hamcrest.Matchers.<UpdateRevision>hasProperty("revisionId", equalTo(update.getRevisionId())),
                org.hamcrest.Matchers.<UpdateRevision>hasProperty("status", equalTo(update.getStatus())),
                org.hamcrest.Matchers.<UpdateRevision>hasProperty("name", equalTo(update.getName())),
                org.hamcrest.Matchers.<UpdateRevision>hasProperty("description", equalTo(update.getDescription())),
                org.hamcrest.Matchers.<UpdateRevision>hasProperty("packageId", equalTo(update.getPackageId())),
                org.hamcrest.Matchers.<UpdateRevision>hasProperty("segmentId", equalTo(update.getSegmentId()))
        ));
    }

    public static UpdateRevision getMatcherToVerifyCreationDateDifferent(UpdateRevision update) {
        return org.mockito.Matchers.argThat(allOf(
                org.hamcrest.Matchers.<UpdateRevision>hasProperty("creationDate", not(update.getCreationDate())),
                org.hamcrest.Matchers.<UpdateRevision>hasProperty("uuid", equalTo(update.getUuid())),
                org.hamcrest.Matchers.<UpdateRevision>hasProperty("revisionId", equalTo(update.getRevisionId())),
                org.hamcrest.Matchers.<UpdateRevision>hasProperty("status", equalTo(update.getStatus())),
                org.hamcrest.Matchers.<UpdateRevision>hasProperty("name", equalTo(update.getName())),
                org.hamcrest.Matchers.<UpdateRevision>hasProperty("description", equalTo(update.getDescription())),
                org.hamcrest.Matchers.<UpdateRevision>hasProperty("packageId", equalTo(update.getPackageId())),
                org.hamcrest.Matchers.<UpdateRevision>hasProperty("segmentId", equalTo(update.getSegmentId()))
        ));
    }
}
