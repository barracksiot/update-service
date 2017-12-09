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
import org.junit.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

public class UpdateRevisionRepositoryByStatusesTest extends UpdateInfoRepositoryTest {

    @Test
    public void getAllUpdatesByStatuses_whenNoStatuses_shouldReturnAllUpdates() throws Exception {
        // Given
        final String userId = UUID.randomUUID().toString();
        List<UpdateRevision> updates = setupAllStatusUpdates(userId);

        // When
        Page<UpdateRevision> result = updateInfoRepository.getAllLatestUpdateInfo(
                new PageRequest(0, 10),
                Optional.of(userId),
                Collections.emptyList(),
                Collections.emptyList()
        );

        // Then
        assertThat(result.getTotalElements()).isEqualTo(updates.size());
        assertThat(result.getContent()).containsExactlyInAnyOrder(updates.toArray(new UpdateRevision[0]));
    }

    @Test
    public void getAllUpdatesByStatuses_whenStatusDraft_shouldReturnDraftUpdates() throws Exception {
        getAllUpdatedByStatusesHelper_whenOneStatusGiven_shouldReturnUpdatesOfThatStatus(UpdateStatus.DRAFT);
    }

    @Test
    public void getAllUpdatesByStatuses_whenStatusPublished_shouldReturnPublishedUpdates() throws Exception {
        getAllUpdatedByStatusesHelper_whenOneStatusGiven_shouldReturnUpdatesOfThatStatus(UpdateStatus.PUBLISHED);
    }

    @Test
    public void getAllUpdatesByStatuses_whenStatusScheduled_shouldReturnScheduledUpdates() throws Exception {
        getAllUpdatedByStatusesHelper_whenOneStatusGiven_shouldReturnUpdatesOfThatStatus(UpdateStatus.SCHEDULED);
    }

    @Test
    public void getAllUpdatesByStatuses_whenStatusArchived_shouldReturnArchivedUpdates() throws Exception {
        getAllUpdatedByStatusesHelper_whenOneStatusGiven_shouldReturnUpdatesOfThatStatus(UpdateStatus.ARCHIVED);
    }

    private void getAllUpdatedByStatusesHelper_whenOneStatusGiven_shouldReturnUpdatesOfThatStatus(UpdateStatus statusFilter) throws Exception {
        // Given
        final String userId = UUID.randomUUID().toString();
        List<UpdateRevision> updates = setupAllStatusUpdates(userId).stream().filter(updateInfo -> updateInfo.getStatus() == statusFilter).collect(Collectors.toList());

        // When
        Page<UpdateRevision> result = updateInfoRepository.getAllLatestUpdateInfo(
                new PageRequest(0, 10),
                Optional.of(userId),
                Collections.singletonList(statusFilter.getName()),
                Collections.emptyList()
        );

        // Then
        assertThat(result.getTotalElements()).isEqualTo(updates.size());
        assertThat(result.getContent()).containsExactlyInAnyOrder(updates.toArray(new UpdateRevision[0]));
    }

    private void getAllUpdatedByStatusesHelper_whenMultipleStatusGiven_shouldReturnUpdatesOfThoseStatuses(List<UpdateStatus> statusFilters) throws Exception {
        // Given
        final String userId = UUID.randomUUID().toString();
        List<UpdateRevision> updates = setupAllStatusUpdates(userId).stream().filter(updateInfo -> statusFilters.contains(updateInfo.getStatus())).collect(Collectors.toList());

        // When
        Page<UpdateRevision> result = updateInfoRepository.getAllLatestUpdateInfo(
                new PageRequest(0, 10),
                Optional.of(userId),
                statusFilters.stream().map(UpdateStatus::getName).collect(Collectors.toList()),
                Collections.emptyList()
        );

        // Then
        assertThat(result.getTotalElements()).isEqualTo(updates.size());
        assertThat(result.getContent()).containsExactlyInAnyOrder(updates.toArray(new UpdateRevision[0]));
    }

    @Test
    public void getAllUpdatesByStatuses_whenStatusesDraftAndPublished_shouldReturnDraftOrPublishedUpdates() throws Exception {
        getAllUpdatedByStatusesHelper_whenMultipleStatusGiven_shouldReturnUpdatesOfThoseStatuses(Arrays.asList(
                UpdateStatus.DRAFT, UpdateStatus.PUBLISHED
        ));
    }

    @Test
    public void getAllUpdatesByStatuses_whenStatusesDraftAndArchived_shouldReturnDraftOrArchivedUpdates() throws Exception {
        getAllUpdatedByStatusesHelper_whenMultipleStatusGiven_shouldReturnUpdatesOfThoseStatuses(Arrays.asList(
                UpdateStatus.DRAFT, UpdateStatus.ARCHIVED
        ));
    }

    @Test
    public void getAllUpdatesByStatuses_whenStatusesDraftAndScheduled_shouldReturnDraftOrArchivedUpdates() throws Exception {
        getAllUpdatedByStatusesHelper_whenMultipleStatusGiven_shouldReturnUpdatesOfThoseStatuses(Arrays.asList(
                UpdateStatus.DRAFT, UpdateStatus.SCHEDULED
        ));
    }

    @Test
    public void getAllUpdatesByStatuses_whenStatusesPublishedAndArchived_shouldReturnDraftOrPublishedUpdates() throws Exception {
        getAllUpdatedByStatusesHelper_whenMultipleStatusGiven_shouldReturnUpdatesOfThoseStatuses(Arrays.asList(
                UpdateStatus.PUBLISHED, UpdateStatus.ARCHIVED
        ));
    }

    @Test
    public void getAllUpdatesByStatuses_whenStatusesPublishedAndScheduled_shouldReturnDraftOrPublishedUpdates() throws Exception {
        getAllUpdatedByStatusesHelper_whenMultipleStatusGiven_shouldReturnUpdatesOfThoseStatuses(Arrays.asList(
                UpdateStatus.PUBLISHED, UpdateStatus.SCHEDULED
        ));
    }

    @Test
    public void getAllUpdatesByStatuses_whenStatusesArchivedAndScheduled_shouldReturnDraftOrPublishedUpdates() throws Exception {
        getAllUpdatedByStatusesHelper_whenMultipleStatusGiven_shouldReturnUpdatesOfThoseStatuses(Arrays.asList(
                UpdateStatus.ARCHIVED, UpdateStatus.SCHEDULED
        ));
    }

    @Test
    public void getAllUpdatesByStatuses_whenStatusesDraftAndScheduledAndPublished_shouldReturnDraftOrPublishedUpdates() throws Exception {
        getAllUpdatedByStatusesHelper_whenMultipleStatusGiven_shouldReturnUpdatesOfThoseStatuses(Arrays.asList(
                UpdateStatus.DRAFT, UpdateStatus.SCHEDULED, UpdateStatus.PUBLISHED
        ));
    }

    @Test
    public void getAllUpdatesByStatuses_whenStatusesDraftAndScheduledAndArchived_shouldReturnDraftOrPublishedUpdates() throws Exception {
        getAllUpdatedByStatusesHelper_whenMultipleStatusGiven_shouldReturnUpdatesOfThoseStatuses(Arrays.asList(
                UpdateStatus.DRAFT, UpdateStatus.SCHEDULED, UpdateStatus.ARCHIVED
        ));
    }

    @Test
    public void getAllUpdatesByStatuses_whenStatusesDraftAndPublishedAndArchived_shouldReturnDraftOrPublishedUpdates() throws Exception {
        getAllUpdatedByStatusesHelper_whenMultipleStatusGiven_shouldReturnUpdatesOfThoseStatuses(Arrays.asList(
                UpdateStatus.DRAFT, UpdateStatus.PUBLISHED, UpdateStatus.ARCHIVED
        ));
    }

    @Test
    public void getAllUpdatesByStatuses_whenStatusesScheduledAndPublishedAndArchived_shouldReturnDraftOrPublishedUpdates() throws Exception {
        getAllUpdatedByStatusesHelper_whenMultipleStatusGiven_shouldReturnUpdatesOfThoseStatuses(Arrays.asList(
                UpdateStatus.SCHEDULED, UpdateStatus.PUBLISHED, UpdateStatus.ARCHIVED
        ));
    }

    @Test
    public void getAllUpdatesByStatuses_ifAllStatuses_shouldReturnAllStatuses() throws Exception {
        getAllUpdatedByStatusesHelper_whenMultipleStatusGiven_shouldReturnUpdatesOfThoseStatuses(
                Arrays.asList(UpdateStatus.DRAFT, UpdateStatus.SCHEDULED, UpdateStatus.PUBLISHED, UpdateStatus.ARCHIVED)
        );
    }

    private List<UpdateRevision> setupAllStatusUpdates(String userId) throws IOException {
        List<UpdateRevision> updates = new ArrayList<>();
        List<UpdateRevision> latest = new ArrayList<>();
        // Draft Only
        UpdateRevision info = UpdateRevisionUtils.getCreatedUpdateRevisionBuilder(userId)
                .status(UpdateStatus.DRAFT)
                .name("draft only")
                .build();
        updates.add(info);
        latest.add(info);

        // Draft, scheduled
        info = UpdateRevisionUtils.getCreatedUpdateRevisionBuilder(userId)
                .status(UpdateStatus.DRAFT)
                .name("draft, then scheduled")
                .build();
        updates.add(info);
        info = info.toBuilder()
                .creationDate(new Date(123456789111L))
                .status(UpdateStatus.SCHEDULED)
                .scheduledDate(new Date(99999999222L))
                .revisionId(2)
                .build();
        updates.add(info);
        latest.add(info);

        // Draft, published
        info = UpdateRevisionUtils.getCreatedUpdateRevisionBuilder(userId)
                .status(UpdateStatus.DRAFT)
                .name("draft, then scheduled")
                .build();
        updates.add(info);
        info = info.toBuilder()
                .creationDate(new Date(123456789111L))
                .status(UpdateStatus.PUBLISHED)
                .revisionId(2)
                .build();
        updates.add(info);
        latest.add(info);

        // Draft, scheduled, published
        info = UpdateRevisionUtils.getCreatedUpdateRevisionBuilder(userId)
                .status(UpdateStatus.DRAFT)
                .name("draft, scheduled and then published")
                .build();
        updates.add(info);
        info = info.toBuilder()
                .creationDate(new Date(123456789111L))
                .status(UpdateStatus.SCHEDULED)
                .scheduledDate(new Date(123456789222L))
                .revisionId(2)
                .build();
        updates.add(info);
        info = info.toBuilder()
                .creationDate(new Date(123456789222L))
                .status(UpdateStatus.PUBLISHED)
                .scheduledDate(null)
                .revisionId(3)
                .build();
        updates.add(info);
        latest.add(info);

        // Draft, published, archived
        info = UpdateRevisionUtils.getCreatedUpdateRevisionBuilder(userId)
                .status(UpdateStatus.DRAFT)
                .name("draft, published and then archived")
                .build();
        updates.add(info);
        info = info.toBuilder()
                .creationDate(new Date(123456789111L))
                .status(UpdateStatus.PUBLISHED)
                .revisionId(2)
                .build();
        updates.add(info);
        info = info.toBuilder()
                .creationDate(new Date(123456789222L))
                .status(UpdateStatus.ARCHIVED)
                .revisionId(3)
                .build();
        updates.add(info);
        latest.add(info);

        // Draft, scheduled, published, archived
        info = UpdateRevisionUtils.getCreatedUpdateRevisionBuilder(userId)
                .status(UpdateStatus.DRAFT)
                .name("draft, scheduled, published and then archived")
                .build();
        updates.add(info);
        info = info.toBuilder()
                .creationDate(new Date(123456789111L))
                .status(UpdateStatus.SCHEDULED)
                .scheduledDate(new Date(123456789222L))
                .revisionId(2)
                .build();
        updates.add(info);
        info = info.toBuilder()
                .creationDate(new Date(123456789222L))
                .status(UpdateStatus.PUBLISHED)
                .scheduledDate(null)
                .revisionId(3)
                .build();
        updates.add(info);
        info = info.toBuilder()
                .creationDate(new Date(123456789333L))
                .status(UpdateStatus.ARCHIVED)
                .revisionId(4)
                .build();
        updates.add(info);
        latest.add(info);

        // Draft, published, archived, published
        info = UpdateRevisionUtils.getCreatedUpdateRevisionBuilder(userId)
                .status(UpdateStatus.DRAFT)
                .name("draft, published, archived and then published")
                .build();
        updates.add(info);
        info = info.toBuilder()
                .creationDate(new Date(123456789111L))
                .status(UpdateStatus.PUBLISHED)
                .revisionId(2)
                .build();
        updates.add(info);
        info = info.toBuilder()
                .creationDate(new Date(123456789222L))
                .status(UpdateStatus.ARCHIVED)
                .revisionId(3)
                .build();
        updates.add(info);
        info = info.toBuilder()
                .creationDate(new Date(123456789333L))
                .status(UpdateStatus.PUBLISHED)
                .revisionId(4)
                .build();
        updates.add(info);
        latest.add(info);

        insertUpdateInfoInDb(updates.toArray(new UpdateRevision[0]));
        return latest;
    }

}
