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

package io.barracks.updateservice.repository;

import io.barracks.updateservice.model.UpdateRevision;
import io.barracks.updateservice.model.UpdateStatus;
import io.barracks.updateservice.utils.UpdateRevisionUtils;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

public class UpdateRevisionRepositoryBySegmentAndStatusTest extends UpdateInfoRepositoryTest {

    @Test
    public void getLatestUpdateInfoPublishedBySegment_whenNoUpdateInfo_shouldReturnNoUpdate() throws IOException {
        // Given
        final String userId = UUID.randomUUID().toString();
        final String segmentId = UUID.randomUUID().toString();
        final UpdateRevision updateRevision1 = UpdateRevisionUtils.getCreatedUpdateRevisionBuilder(userId)
                .segmentId(segmentId)
                .status(UpdateStatus.ARCHIVED)
                .build();
        final UpdateRevision updateRevision2 = UpdateRevisionUtils.getCreatedUpdateRevisionBuilder(userId)
                .segmentId(segmentId)
                .status(UpdateStatus.DRAFT)
                .build();
        insertUpdateInfoInDb(updateRevision1);
        insertUpdateInfoInDb(updateRevision2);

        // When
        Optional<UpdateRevision> update = updateInfoRepository.getLatestPublishedUpdateInfoBySegment(userId, segmentId);

        // Then
        assertThat(update).isNotPresent();
    }

    @Test
    public void getLatestUpdateInfoPublishedBySegment_whenADraftUpdateInfoExists_shouldReturnThatUpdateInfo() throws IOException {
        // Given
        final String userId = UUID.randomUUID().toString();
        final String segmentId = UUID.randomUUID().toString();
        final UpdateRevision updateRevision1 = UpdateRevisionUtils.getCreatedUpdateRevisionBuilder(userId)
                .segmentId(segmentId)
                .status(UpdateStatus.ARCHIVED)
                .build();
        final UpdateRevision updateRevision2 = UpdateRevisionUtils.getCreatedUpdateRevisionBuilder(userId)
                .segmentId(segmentId)
                .status(UpdateStatus.DRAFT)
                .build();
        final UpdateRevision updateRevision3 = UpdateRevisionUtils.getCreatedUpdateRevisionBuilder(userId)
                .segmentId(segmentId)
                .status(UpdateStatus.PUBLISHED)
                .build();
        final UpdateRevision updateRevisionBadUser = UpdateRevisionUtils.getCreatedUpdateRevisionBuilder("AnotherUserId")
                .uuid(updateRevision3.getUuid())
                .status(UpdateStatus.PUBLISHED)
                .build();
        insertUpdateInfoInDb(updateRevision1);
        insertUpdateInfoInDb(updateRevision2);
        insertUpdateInfoInDb(updateRevision3);
        insertUpdateInfoInDb(updateRevisionBadUser);

        // When
        Optional<UpdateRevision> update = updateInfoRepository.getLatestPublishedUpdateInfoBySegment(userId, segmentId);

        // Then
        assertThat(update).isPresent();
        compareUpdateInfo(updateRevision3, update.get());
    }

    @Test
    public void getLatestUpdateInfoPublishedBySegment_whenUpdateInfoWasDraftThenPublished_shouldThatUpdateInfo() throws IOException {
        //Given
        final String userId = UUID.randomUUID().toString();
        final String segmentId = UUID.randomUUID().toString();
        final UUID uuid = UUID.randomUUID();
        final UpdateRevision updateRevision1 = UpdateRevisionUtils.getCreatedUpdateRevisionBuilder(userId)
                .uuid(uuid)
                .segmentId(segmentId)
                .creationDate(new Date(11234567890L))
                .build();
        final UpdateRevision updateRevision2 = UpdateRevisionUtils.getCreatedUpdateRevisionBuilder(userId)
                .uuid(uuid)
                .segmentId(segmentId)
                .creationDate(new Date(11234567893L))
                .status(UpdateStatus.PUBLISHED)
                .revisionId(2)
                .build();
        final UpdateRevision updateRevisionBadUser = UpdateRevisionUtils.getCreatedUpdateRevisionBuilder("anotherUserId")
                .uuid(uuid)
                .creationDate(new Date(11234567893L))
                .status(UpdateStatus.PUBLISHED)
                .build();
        insertUpdateInfoInDb(updateRevision1);
        insertUpdateInfoInDb(updateRevision2);
        insertUpdateInfoInDb(updateRevisionBadUser);

        // When
        Optional<UpdateRevision> update = updateInfoRepository.getLatestPublishedUpdateInfoBySegment(userId, segmentId);

        // Then
        assertThat(update).isPresent();
        compareUpdateInfo(updateRevision2, update.get());
    }

    @Test
    public void getLatestUpdateInfoPublishedBySegment_whenUpdateInfoWasDraftThenPublishedThenArchived_shouldReturnNoUpdate() throws IOException {
        // Given
        final String userId = UUID.randomUUID().toString();
        final String segmentId = UUID.randomUUID().toString();
        final UUID uuid = UUID.randomUUID();
        final UpdateRevision updateRevision1 = UpdateRevisionUtils.getCreatedUpdateRevisionBuilder(userId)
                .uuid(uuid)
                .segmentId(segmentId)
                .creationDate(new Date(11234567890L))
                .build();
        final UpdateRevision updateRevision2 = UpdateRevisionUtils.getCreatedUpdateRevisionBuilder(userId)
                .uuid(uuid)
                .segmentId(segmentId)
                .creationDate(new Date(11234567893L))
                .status(UpdateStatus.PUBLISHED)
                .revisionId(2)
                .build();
        final UpdateRevision updateRevision3 = UpdateRevisionUtils.getCreatedUpdateRevisionBuilder(userId)
                .uuid(uuid)
                .segmentId(segmentId)
                .creationDate(new Date(11234567896L))
                .status(UpdateStatus.ARCHIVED)
                .revisionId(3)
                .build();
        final UpdateRevision updateRevisionBadUser = UpdateRevisionUtils.getCreatedUpdateRevisionBuilder("anotherUserId")
                .uuid(uuid)
                .creationDate(new Date(11234567893L))
                .status(UpdateStatus.PUBLISHED)
                .build();
        insertUpdateInfoInDb(updateRevision1);
        insertUpdateInfoInDb(updateRevision2);
        insertUpdateInfoInDb(updateRevision3);
        insertUpdateInfoInDb(updateRevisionBadUser);

        // When
        Optional<UpdateRevision> update = updateInfoRepository.getLatestPublishedUpdateInfoBySegment(userId, segmentId);

        // Then
        assertThat(update).isNotPresent();
    }

    @Test
    public void getLatestUpdateInfoPublishedBySegment_whenUpdateInfoWasDraftThenPublishedThenArchivedThenPublished_shouldReturnThatUpdateInfo() throws IOException {
        // Given
        final String userId = UUID.randomUUID().toString();
        final String segmentId = UUID.randomUUID().toString();
        final UUID uuid = UUID.randomUUID();
        final UpdateRevision updateRevision1 = UpdateRevisionUtils.getCreatedUpdateRevisionBuilder(userId)
                .uuid(uuid)
                .segmentId(segmentId)
                .creationDate(new Date(11234567890L))
                .build();
        final UpdateRevision updateRevision2 = UpdateRevisionUtils.getCreatedUpdateRevisionBuilder(userId)
                .uuid(uuid)
                .segmentId(segmentId)
                .creationDate(new Date(11234567893L))
                .status(UpdateStatus.PUBLISHED)
                .revisionId(2)
                .build();
        final UpdateRevision updateRevision3 = UpdateRevisionUtils.getCreatedUpdateRevisionBuilder(userId)
                .uuid(uuid)
                .segmentId(segmentId)
                .creationDate(new Date(11234567896L))
                .status(UpdateStatus.ARCHIVED)
                .revisionId(3)
                .build();
        final UpdateRevision updateRevision4 = UpdateRevisionUtils.getCreatedUpdateRevisionBuilder(userId)
                .uuid(uuid)
                .segmentId(segmentId)
                .creationDate(new Date(11234567899L))
                .status(UpdateStatus.PUBLISHED)
                .revisionId(4)
                .build();
        final UpdateRevision updateRevisionBadUser = UpdateRevisionUtils.getCreatedUpdateRevisionBuilder("anotherUserId")
                .uuid(uuid)
                .creationDate(new Date(11234567893L))
                .status(UpdateStatus.PUBLISHED)
                .build();
        insertUpdateInfoInDb(updateRevision1);
        insertUpdateInfoInDb(updateRevision2);
        insertUpdateInfoInDb(updateRevision3);
        insertUpdateInfoInDb(updateRevision4);
        insertUpdateInfoInDb(updateRevisionBadUser);

        // When
        Optional<UpdateRevision> update = updateInfoRepository.getLatestPublishedUpdateInfoBySegment(userId, segmentId);

        // Then
        assertThat(update).isPresent();
        compareUpdateInfo(updateRevision4, update.get());
    }

    @Test
    public void getLatestPublishedUpdateInfo_whenNoPublishedUpdatePresent_shouldReturnNoUpdate() throws IOException {
        // Given
        final String userId = UUID.randomUUID().toString();
        final String segmentId = UUID.randomUUID().toString();
        final UUID uuid = UUID.randomUUID();
        final UpdateRevision updateRevision1 = UpdateRevisionUtils.getCreatedUpdateRevisionBuilder(userId)
                .uuid(uuid)
                .segmentId(segmentId)
                .creationDate(new Date(11234567890L))
                .build();
        final UpdateRevision updateRevision2 = UpdateRevisionUtils.getCreatedUpdateRevisionBuilder(userId)
                .uuid(uuid)
                .segmentId(segmentId)
                .creationDate(new Date(11234567893L))
                .status(UpdateStatus.ARCHIVED)
                .revisionId(2)
                .build();
        insertUpdateInfoInDb(updateRevision1);
        insertUpdateInfoInDb(updateRevision2);

        // When
        Optional<UpdateRevision> result = updateInfoRepository.getLatestPublishedUpdateInfoBySegment(userId, segmentId);

        // Then
        Assert.assertFalse(result.isPresent());
    }

    @Test
    public void getLatestPublishedUpdateInfo_whenOnlyOnePublishedUpdatePresent_shouldReturnThatUpdateInfo() throws IOException {
        // Given
        final String userId = UUID.randomUUID().toString();
        final String segmentId = UUID.randomUUID().toString();
        final UUID uuid = UUID.randomUUID();
        final UpdateRevision updateRevision1 = UpdateRevisionUtils.getCreatedUpdateRevisionBuilder(userId)
                .uuid(uuid)
                .segmentId(segmentId)
                .status(UpdateStatus.PUBLISHED)
                .build();
        final UpdateRevision updateRevisionBadUser = UpdateRevisionUtils.getCreatedUpdateRevisionBuilder("anotherUserId")
                .uuid(uuid)
                .status(UpdateStatus.PUBLISHED)
                .build();
        insertUpdateInfoInDb(updateRevision1);
        insertUpdateInfoInDb(updateRevisionBadUser);

        // When
        Optional<UpdateRevision> result = updateInfoRepository.getLatestPublishedUpdateInfoBySegment(userId, segmentId);

        // Then
        Assert.assertTrue(result.isPresent());
        compareUpdateInfo(updateRevision1, result.get());
    }

    @Test
    public void getLatestPublishedUpdateInfo_whenTwoPublishedUpdatePresent_shouldTheLatestUpdateInfo() throws IOException {
        // Given
        final String userId = UUID.randomUUID().toString();
        final String segmentId = UUID.randomUUID().toString();
        final UUID uuid = UUID.randomUUID();

        final UpdateRevision updateRevision1 = UpdateRevisionUtils.getCreatedUpdateRevisionBuilder(userId)
                .segmentId(segmentId)
                .creationDate(new Date(11234567000L))
                .status(UpdateStatus.PUBLISHED)
                .build();
        final UpdateRevision updateRevision2 = UpdateRevisionUtils.getCreatedUpdateRevisionBuilder(userId)
                .uuid(uuid)
                .segmentId(segmentId)
                .creationDate(new Date(11234567999L))
                .status(UpdateStatus.PUBLISHED)
                .build();
        final UpdateRevision updateRevisionBadUser = UpdateRevisionUtils.getCreatedUpdateRevisionBuilder("anotherUserId")
                .uuid(uuid)
                .creationDate(new Date(11234569999L))
                .status(UpdateStatus.PUBLISHED)
                .build();
        insertUpdateInfoInDb(updateRevision1);
        insertUpdateInfoInDb(updateRevision2);
        insertUpdateInfoInDb(updateRevisionBadUser);

        // When
        Optional<UpdateRevision> result = updateInfoRepository.getLatestPublishedUpdateInfoBySegment(userId, segmentId);

        // Then
        Assert.assertTrue(result.isPresent());
        compareUpdateInfo(updateRevision2, result.get());
    }

    @Test
    public void getLatestPublishedUpdateInfo_whenMoreThanTwoPublishedUpdatePresent_shouldTheMostRecentUpdateInfo() throws IOException {
        // Given
        final String userId = UUID.randomUUID().toString();
        final String segmentId = UUID.randomUUID().toString();
        final UpdateRevision updateRevision1 = UpdateRevisionUtils.getCreatedUpdateRevisionBuilder(userId)
                .segmentId(segmentId)
                .creationDate(new Date(11234567000L))
                .status(UpdateStatus.PUBLISHED)
                .build();
        final UpdateRevision updateRevision2 = UpdateRevisionUtils.getCreatedUpdateRevisionBuilder(userId)
                .segmentId(segmentId)
                .creationDate(new Date(11234567111L))
                .status(UpdateStatus.PUBLISHED)
                .build();
        final UpdateRevision updateRevision3 = UpdateRevisionUtils.getCreatedUpdateRevisionBuilder(userId)
                .segmentId(segmentId)
                .creationDate(new Date(11234567222L))
                .status(UpdateStatus.PUBLISHED)
                .build();
        final UpdateRevision updateRevision4 = UpdateRevisionUtils.getCreatedUpdateRevisionBuilder(userId)
                .segmentId(segmentId)
                .creationDate(new Date(11234567333L))
                .status(UpdateStatus.PUBLISHED)
                .build();
        final UpdateRevision updateRevision5 = UpdateRevisionUtils.getCreatedUpdateRevisionBuilder(userId)
                .segmentId(segmentId)
                .creationDate(new Date(11234567444L))
                .status(UpdateStatus.PUBLISHED)
                .build();
        final UpdateRevision updateRevision6 = UpdateRevisionUtils.getCreatedUpdateRevisionBuilder(userId)
                .segmentId(segmentId)
                .creationDate(new Date(11234567555L))
                .status(UpdateStatus.PUBLISHED)
                .build();
        final UpdateRevision updateRevisionBadUser = UpdateRevisionUtils.getCreatedUpdateRevisionBuilder("anotherUserId")
                .segmentId(segmentId)
                .creationDate(new Date(11234567666L))
                .status(UpdateStatus.PUBLISHED)
                .build();
        insertUpdateInfoInDb(updateRevision1);
        insertUpdateInfoInDb(updateRevision2);
        insertUpdateInfoInDb(updateRevision3);
        insertUpdateInfoInDb(updateRevision4);
        insertUpdateInfoInDb(updateRevision5);
        insertUpdateInfoInDb(updateRevision6);
        insertUpdateInfoInDb(updateRevisionBadUser);

        // When
        Optional<UpdateRevision> result = updateInfoRepository.getLatestPublishedUpdateInfoBySegment(userId, segmentId);

        // Then
        assertThat(result).isPresent();
        compareUpdateInfo(updateRevision6, result.get());
    }
}
