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

package io.barracks.updateservice.manager;

import io.barracks.commons.test.ServiceClientTest;
import io.barracks.updateservice.exception.InvalidUpdateOperationException;
import io.barracks.updateservice.exception.NoSuchUpdateException;
import io.barracks.updateservice.exception.UpdateNotFoundException;
import io.barracks.updateservice.model.UpdateRevision;
import io.barracks.updateservice.model.UpdateStatus;
import io.barracks.updateservice.repository.UpdateRevisionRepository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

import static io.barracks.updateservice.utils.UpdateRevisionUtils.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class UpdateRevisionManagerTest extends ServiceClientTest {

    @Mock
    private UpdateRevisionRepository updateRevisionRepository;
    private UpdateRevisionManager manager;

    @Before
    public void setUp() throws Exception {
        manager = new UpdateRevisionManager(updateRevisionRepository, new UpdateStatusManager());
    }

    @Test
    public void createUpdate_whenUpdateGivenContainAStatus_shouldOverwriteTheStatusAndReturnTheRevisionCreated() {
        // Given
        final String userId = UUID.randomUUID().toString();
        final UpdateRevision newUpdate = getNewUpdateRevisionBuilder(userId)
                .status(UpdateStatus.PUBLISHED)
                .build();
        final UpdateRevision beforeSaveUpdate = newUpdate.toBuilder()
                .status(UpdateStatus.DRAFT)
                .revisionId(1)
                .build();
        final UpdateRevision mockResponse = beforeSaveUpdate.toBuilder()
                .uuid(UUID.randomUUID())
                .creationDate(new Date(1234567890L))
                .build();

        when(updateRevisionRepository.save(getMatcherThatIgnoreUpdateUuid(beforeSaveUpdate))).thenReturn(mockResponse);

        // When
        final UpdateRevision managerResponse = manager.createUpdate(newUpdate);

        // Then
        verify(updateRevisionRepository).save(getMatcherThatIgnoreUpdateUuid(beforeSaveUpdate));
        assertEquals(mockResponse, managerResponse);
    }

    @Test
    public void createUpdate_whenUpdateGivenContainAnUuid_shouldOverwriteTheUUidAndReturnTheRevisionCreated() {
        // Given
        final String userId = UUID.randomUUID().toString();
        final UpdateRevision newUpdate = getNewUpdateRevisionBuilder(userId)
                .uuid(UUID.randomUUID())
                .build();
        final UpdateRevision beforeSaveUpdate = newUpdate.toBuilder()
                .uuid(null)
                .revisionId(1)
                .status(UpdateStatus.DRAFT)
                .build();
        final UpdateRevision mockResponse = beforeSaveUpdate.toBuilder()
                .uuid(UUID.randomUUID())
                .creationDate(new Date(1234567890L))
                .build();

        when(updateRevisionRepository.save(getMatcherToVerifyUpdateUuidDifferent(beforeSaveUpdate))).thenReturn(mockResponse);

        // When
        final UpdateRevision managerResponse = manager.createUpdate(newUpdate);

        // Then
        verify(updateRevisionRepository).save(getMatcherToVerifyUpdateUuidDifferent(beforeSaveUpdate));
        assertEquals(mockResponse, managerResponse);
    }

    @Test
    public void createUpdate_whenUpdateGivenContainARevisionId_shouldOverwriteTheRevisionIdAndReturnTheRevisionCreated() {
        // Given
        final String userId = UUID.randomUUID().toString();
        final UpdateRevision newUpdate = getNewUpdateRevisionBuilder(userId)
                .revisionId(3)
                .build();
        final UpdateRevision beforeSaveUpdate = newUpdate.toBuilder()
                .revisionId(1)
                .status(UpdateStatus.DRAFT)
                .build();
        final UpdateRevision mockResponse = beforeSaveUpdate.toBuilder()
                .uuid(UUID.randomUUID())
                .creationDate(new Date(1234567890L))
                .build();

        when(updateRevisionRepository.save(getMatcherToVerifyUpdateUuidDifferent(beforeSaveUpdate))).thenReturn(mockResponse);

        // When
        final UpdateRevision managerResponse = manager.createUpdate(newUpdate);

        // Then
        verify(updateRevisionRepository).save(getMatcherToVerifyUpdateUuidDifferent(beforeSaveUpdate));
        assertEquals(mockResponse, managerResponse);
    }

    @Test
    public void createUpdate_whenUpdateGivenIsValid_shouldReturnTheRevisionCreated() {
        // Given
        final String userId = UUID.randomUUID().toString();
        final UpdateRevision newUpdate = getNewUpdateRevisionBuilder(userId).build();
        final UpdateRevision beforeSaveUpdate = newUpdate.toBuilder()
                .revisionId(1)
                .status(UpdateStatus.DRAFT)
                .build();
        final UpdateRevision mockResponse = beforeSaveUpdate.toBuilder()
                .uuid(UUID.randomUUID())
                .creationDate(new Date(1234567890L))
                .build();

        when(updateRevisionRepository.save(getMatcherThatIgnoreUpdateUuid(beforeSaveUpdate))).thenReturn(mockResponse);

        // When
        final UpdateRevision managerResponse = manager.createUpdate(newUpdate);

        // Then
        verify(updateRevisionRepository).save(getMatcherThatIgnoreUpdateUuid(beforeSaveUpdate));
        assertEquals(mockResponse, managerResponse);
    }

    @Test
    public void reviseUpdate_whenUpdateNotFound_shouldThrowUpdateNotFoundException() {
        // Given
        final String userId = UUID.randomUUID().toString();
        final UUID updateUuid = UUID.randomUUID();
        final UpdateRevision revision = getCreatedUpdateRevisionBuilder(userId)
                .uuid(updateUuid)
                .status(UpdateStatus.PUBLISHED)
                .build();

        when(updateRevisionRepository.findTopByUuidAndUserIdOrderByRevisionIdDesc(updateUuid, userId)).thenReturn(Optional.empty());

        // When - Then
        assertThatExceptionOfType(UpdateNotFoundException.class)
                .isThrownBy(() -> manager.reviseUpdate(revision))
                .withMessage("No update with id " + updateUuid.toString());
        verify(updateRevisionRepository).findTopByUuidAndUserIdOrderByRevisionIdDesc(updateUuid, userId);
    }

    @Test
    public void reviseUpdate_whenNewStatusIsNotCompatibleWithCurrentStatus_shouldThrowInvalidUpdateOperation() {
        // Given
        final String userId = UUID.randomUUID().toString();
        final UUID updateUuid = UUID.randomUUID();
        final UpdateRevision currentRevision = getCreatedUpdateRevisionBuilder(userId)
                .status(UpdateStatus.PUBLISHED)
                .uuid(updateUuid)
                .build();
        final UpdateRevision revision = currentRevision.toBuilder()
                .status(UpdateStatus.DRAFT)
                .build();

        when(updateRevisionRepository.findTopByUuidAndUserIdOrderByRevisionIdDesc(updateUuid, userId))
                .thenReturn(Optional.of(currentRevision));

        // When - Then
        assertThatExceptionOfType(InvalidUpdateOperationException.class)
                .isThrownBy(() -> manager.reviseUpdate(revision))
                .withMessage("Cannot change status " + UpdateStatus.PUBLISHED.getName() + " to status " + UpdateStatus.DRAFT.getName());
        verify(updateRevisionRepository).findTopByUuidAndUserIdOrderByRevisionIdDesc(updateUuid, userId);
    }

    @Test
    public void reviseUpdate_whenRevisionContainsId_shouldIgnoreTheIdAndCreateANewRevisionAndReturnIt() {
        // Given
        final String userId = UUID.randomUUID().toString();
        final UUID updateUuid = UUID.randomUUID();

        final UpdateRevision currentRevision = getCreatedUpdateRevisionBuilder(userId)
                .status(UpdateStatus.PUBLISHED)
                .id(UUID.randomUUID().toString())
                .uuid(updateUuid)
                .build();
        final UpdateRevision revision = currentRevision.toBuilder()
                .status(UpdateStatus.ARCHIVED)
                .build();
        final UpdateRevision revisionWithoutId = currentRevision.toBuilder()
                .status(UpdateStatus.ARCHIVED)
                .revisionId(currentRevision.getRevisionId() + 1)
                .id(null)
                .build();
        final UpdateRevision mockResponse = currentRevision.toBuilder()
                .status(UpdateStatus.ARCHIVED)
                .id(UUID.randomUUID().toString())
                .uuid(updateUuid)
                .build();

        when(updateRevisionRepository.findTopByUuidAndUserIdOrderByRevisionIdDesc(updateUuid, userId))
                .thenReturn(Optional.of(currentRevision));
        when(updateRevisionRepository.save(getMatcherThatIgnoreCreationDate(revisionWithoutId))).thenReturn(mockResponse);

        // When
        final UpdateRevision returnedRevision = manager.reviseUpdate(revision);

        // Then
        verify(updateRevisionRepository).findTopByUuidAndUserIdOrderByRevisionIdDesc(updateUuid, userId);
        verify(updateRevisionRepository).save(getMatcherThatIgnoreCreationDate(revisionWithoutId));
        assertEquals(mockResponse, returnedRevision);
    }

    @Test
    public void reviseUpdate_whenRevisionContainsRevisionId_shouldIgnoreTheIdAndCreateANewRevisionAndReturnIt() {
        // Given
        final String userId = UUID.randomUUID().toString();
        final UUID updateUuid = UUID.randomUUID();

        final UpdateRevision currentRevision = getCreatedUpdateRevisionBuilder(userId)
                .status(UpdateStatus.PUBLISHED)
                .revisionId(2)
                .uuid(updateUuid)
                .build();
        final UpdateRevision revision = currentRevision.toBuilder()
                .status(UpdateStatus.ARCHIVED)
                .revisionId(6)
                .build();
        final UpdateRevision revisionWithoutCorrectRevisionId = currentRevision.toBuilder()
                .status(UpdateStatus.ARCHIVED)
                .revisionId(currentRevision.getRevisionId() + 1)
                .build();
        final UpdateRevision mockResponse = currentRevision.toBuilder()
                .status(UpdateStatus.ARCHIVED)
                .build();

        when(updateRevisionRepository.findTopByUuidAndUserIdOrderByRevisionIdDesc(updateUuid, userId))
                .thenReturn(Optional.of(currentRevision));
        when(updateRevisionRepository.save(getMatcherThatIgnoreCreationDate(revisionWithoutCorrectRevisionId)))
                .thenReturn(mockResponse);

        // When
        final UpdateRevision returnedRevision = manager.reviseUpdate(revision);

        // Then
        verify(updateRevisionRepository).findTopByUuidAndUserIdOrderByRevisionIdDesc(updateUuid, userId);
        verify(updateRevisionRepository).save(getMatcherThatIgnoreCreationDate(revisionWithoutCorrectRevisionId));
        assertEquals(mockResponse, returnedRevision);
    }

    @Test
    public void reviseUpdate_whenRevisionContainsCreationDate_shouldIgnoreTheCreationDateAndCreateANewRevisionAndReturnIt() {
        // Given
        final String userId = UUID.randomUUID().toString();
        final UUID updateUuid = UUID.randomUUID();

        final UpdateRevision currentRevision = getCreatedUpdateRevisionBuilder(userId)
                .status(UpdateStatus.PUBLISHED)
                .creationDate(new Date(1122334455L))
                .uuid(updateUuid)
                .build();
        final UpdateRevision revision = currentRevision.toBuilder()
                .status(UpdateStatus.ARCHIVED)
                .creationDate(new Date(1234567890L))
                .build();
        final UpdateRevision revisionWithRevisionId = revision.toBuilder()
                .status(UpdateStatus.ARCHIVED)
                .revisionId(revision.getRevisionId() + 1)
                .build();
        final UpdateRevision mockResponse = currentRevision.toBuilder()
                .status(UpdateStatus.ARCHIVED)
                .build();

        when(updateRevisionRepository.findTopByUuidAndUserIdOrderByRevisionIdDesc(updateUuid, userId))
                .thenReturn(Optional.of(currentRevision));
        when(updateRevisionRepository.save(getMatcherToVerifyCreationDateDifferent(revisionWithRevisionId)))
                .thenReturn(mockResponse);

        // When
        final UpdateRevision returnedRevision = manager.reviseUpdate(revision);

        // Then
        verify(updateRevisionRepository).findTopByUuidAndUserIdOrderByRevisionIdDesc(updateUuid, userId);
        verify(updateRevisionRepository).save(getMatcherToVerifyCreationDateDifferent(revisionWithRevisionId));
        assertEquals(mockResponse, returnedRevision);
    }

    @Test
    public void reviseUpdate_whenGivenRevisionIsValid_shouldCreateANewRevisionAndReturnIt() {
        // Given
        final String userId = UUID.randomUUID().toString();
        final UUID updateUuid = UUID.randomUUID();

        final UpdateRevision currentRevision = getCreatedUpdateRevisionBuilder(userId)
                .status(UpdateStatus.PUBLISHED)
                .uuid(updateUuid)
                .build();
        final UpdateRevision revision = currentRevision.toBuilder()
                .status(UpdateStatus.ARCHIVED)
                .build();
        final UpdateRevision mockResponse = currentRevision.toBuilder()
                .status(UpdateStatus.ARCHIVED)
                .uuid(updateUuid)
                .revisionId(currentRevision.getRevisionId() + 1)
                .build();

        when(updateRevisionRepository.findTopByUuidAndUserIdOrderByRevisionIdDesc(updateUuid, userId))
                .thenReturn(Optional.of(currentRevision));
        when(updateRevisionRepository.save(getMatcherThatIgnoreCreationDate(mockResponse)))
                .thenReturn(mockResponse);

        // When
        final UpdateRevision returnedRevision = manager.reviseUpdate(revision);

        // Then
        verify(updateRevisionRepository).findTopByUuidAndUserIdOrderByRevisionIdDesc(updateUuid, userId);
        verify(updateRevisionRepository).save(getMatcherThatIgnoreCreationDate(mockResponse));
        assertEquals(mockResponse, returnedRevision);
    }

    @Test
    public void getAllUpdates_whenNoUpdateExists_shouldReturnEmptyList() {
        // Given
        final Pageable pageable = new PageRequest(0, 20);
        final String userId = UUID.randomUUID().toString();
        final List<String> statusFilters = Collections.emptyList();
        final Page<UpdateRevision> repositoryResponse = new PageImpl<>(Collections.emptyList(), pageable, 0);

        when(updateRevisionRepository.getAllLatestUpdateInfo(pageable, Optional.of(userId), statusFilters, Collections.emptyList()))
                .thenReturn(repositoryResponse);

        // When
        final Page<UpdateRevision> managerResponse = manager.getAllUpdates(pageable, userId, statusFilters, Collections.emptyList());

        // Then
        verify(updateRevisionRepository).getAllLatestUpdateInfo(pageable, Optional.of(userId), statusFilters, Collections.emptyList());
        assertEquals(managerResponse.getTotalElements(), 0);
        assertEquals(managerResponse.getTotalPages(), 0);
        assertEquals(managerResponse.getContent().size(), 0);
    }

    @Test
    public void getAllUpdates_whenStatusFilterGiven_shouldReturnFilteredUpdateList() {
        // Given
        final Pageable pageable = new PageRequest(0, 20);
        final String userId = UUID.randomUUID().toString();
        final String[] statuses = {UpdateStatus.DRAFT.getName(), UpdateStatus.PUBLISHED.getName()};
        final List<String> statusFilters = Arrays.asList(statuses);
        final List<UpdateRevision> filteredUpdateList = this.getAllKindsOfUpdates(userId)
                .parallelStream()
                .filter((item) -> statusFilters.contains(item.getStatus().getName()))
                .collect(Collectors.toList());
        final Page<UpdateRevision> repositoryResponse = new PageImpl<>(filteredUpdateList, pageable, filteredUpdateList.size());

        when(updateRevisionRepository.getAllLatestUpdateInfo(pageable, Optional.of(userId), statusFilters, Collections.emptyList()))
                .thenReturn(repositoryResponse);

        // When
        final Page<UpdateRevision> managerResponse = manager.getAllUpdates(pageable, userId, statusFilters, Collections.emptyList());

        // Then
        verify(updateRevisionRepository).getAllLatestUpdateInfo(pageable, Optional.of(userId), statusFilters, Collections.emptyList());
        assertEquals(managerResponse.getTotalElements(), filteredUpdateList.size());
        assertEquals(managerResponse.getContent().size(), filteredUpdateList.size());
        assertThat(managerResponse.getContent()).containsExactlyInAnyOrder(filteredUpdateList.toArray(new UpdateRevision[0]));
    }

    @Test
    public void getUpdateByUuid_whenNoUpdateFound_UpdateNotFoundException() {
        // Given
        final UUID updateUuid = UUID.randomUUID();
        final String userId = UUID.randomUUID().toString();
        when(updateRevisionRepository.findTopByUuidAndUserIdOrderByRevisionIdDesc(updateUuid, userId))
                .thenReturn(Optional.empty());

        // When
        assertThatExceptionOfType(UpdateNotFoundException.class)
                .isThrownBy(() -> manager.getUpdateByUuid(updateUuid, userId))
                .withMessage("No update with id " + updateUuid.toString());

        // Then
        verify(updateRevisionRepository).findTopByUuidAndUserIdOrderByRevisionIdDesc(updateUuid, userId);
    }

    @Test
    public void getUpdateByUuid_whenUpdateExists_shouldReturnIt() {
        // Given
        final UUID updateUuid = UUID.randomUUID();
        final String userId = UUID.randomUUID().toString();
        final UpdateRevision update = getCreatedUpdateRevisionBuilder(userId)
                .uuid(updateUuid)
                .build();
        when(updateRevisionRepository.findTopByUuidAndUserIdOrderByRevisionIdDesc(updateUuid, userId))
                .thenReturn(Optional.of(update));

        // When
        final UpdateRevision managerResponse = manager.getUpdateByUuid(updateUuid, userId);

        // Then
        verify(updateRevisionRepository).findTopByUuidAndUserIdOrderByRevisionIdDesc(updateUuid, userId);
        assertEquals(update, managerResponse);
    }

    @Test
    public void getLatestPublishedUpdateForSegment_whenNoPublishedUpdateExists_shouldReturnNoSuchUpdateException() {
        // Given
        final String userId = UUID.randomUUID().toString();
        final String segmentId = UUID.randomUUID().toString();

        when(updateRevisionRepository.getLatestPublishedUpdateInfoBySegment(userId, segmentId))
                .thenReturn(Optional.empty());

        // When
        assertThatExceptionOfType(NoSuchUpdateException.class)
                .isThrownBy(() -> manager.getLatestPublishedUpdateForSegment(userId, segmentId))
                .withMessage("No published update");

        // Then
        verify(updateRevisionRepository).getLatestPublishedUpdateInfoBySegment(userId, segmentId);
    }

    @Test
    public void getLatestPublishedUpdateForSegment_whenPublishedUpdateExists_shouldReturnIt() {
        // Given
        final String userId = UUID.randomUUID().toString();
        final String segmentId = UUID.randomUUID().toString();
        final UpdateRevision response = getCreatedUpdateRevisionBuilder(userId)
                .segmentId(segmentId)
                .build();

        when(updateRevisionRepository.getLatestPublishedUpdateInfoBySegment(userId, segmentId))
                .thenReturn(Optional.of(response));

        // When
        UpdateRevision managerResponse = manager.getLatestPublishedUpdateForSegment(userId, segmentId);

        // Then
        verify(updateRevisionRepository).getLatestPublishedUpdateInfoBySegment(userId, segmentId);
        assertEquals(response, managerResponse);
    }

    @Test
    public void getLatestPublishedUpdate_whenNoPublishedUpdateExists_shouldReturnNoSuchUpdateException() {
        // Given
        final String userId = UUID.randomUUID().toString();

        when(updateRevisionRepository.getLatestPublishedUpdateInfoWithoutSegmentId(userId))
                .thenReturn(Optional.empty());

        // When
        assertThatExceptionOfType(NoSuchUpdateException.class)
                .isThrownBy(() -> manager.getLatestPublishedUpdateForOtherSegment(userId))
                .withMessage("No published update");

        // Then
        verify(updateRevisionRepository).getLatestPublishedUpdateInfoWithoutSegmentId(userId);
    }

    @Test
    public void getLatestPublishedUpdate_whenPublishedUpdateExists_shouldReturnIt() {
        // Given
        final String userId = UUID.randomUUID().toString();
        final UpdateRevision response = getCreatedUpdateRevisionBuilder(userId)
                .segmentId(null)
                .build();

        when(updateRevisionRepository.getLatestPublishedUpdateInfoWithoutSegmentId(userId))
                .thenReturn(Optional.of(response));

        // When
        UpdateRevision managerResponse = manager.getLatestPublishedUpdateForOtherSegment(userId);

        // Then
        verify(updateRevisionRepository).getLatestPublishedUpdateInfoWithoutSegmentId(userId);
        assertEquals(response, managerResponse);
    }

    @Test
    public void publishDueScheduledUpdates_whenADueScheduledUpdateExists_shouldUpdateIt() {
        // Given
        final String userId = UUID.randomUUID().toString();
        final UpdateStatus status = UpdateStatus.SCHEDULED;
        final UpdateRevision dueUpdate = buildScheduledUpdate(userId, true);
        final UpdateRevision notDueUpdate = buildScheduledUpdate(userId, false);
        final List<UpdateRevision> scheduledUpdates = Arrays.asList(
                dueUpdate,
                notDueUpdate
        );
        final Page<UpdateRevision> scheduledUpdatesPage = new PageImpl<>(scheduledUpdates);
        final UpdateRevision publishedDueUpdate = dueUpdate.toBuilder().scheduledDate(null).status(UpdateStatus.PUBLISHED).build();
        when(updateRevisionRepository.getAllLatestUpdateInfo(any(Pageable.class), eq(status)))
                .thenReturn(scheduledUpdatesPage);
        when(updateRevisionRepository.findTopByUuidAndUserIdOrderByRevisionIdDesc(publishedDueUpdate.getUuid(), userId))
                .thenReturn(Optional.of(dueUpdate));

        // When
        manager.publishDueScheduledUpdates();

        // Then
        verify(updateRevisionRepository).getAllLatestUpdateInfo(any(Pageable.class), eq(status));
        verify(updateRevisionRepository, times(1)).save(getMatcherThatIgnoreCreationDate(
                publishedDueUpdate.toBuilder()
                        .revisionId(dueUpdate.getRevisionId() + 1)
                        .build()
        ));
    }

    @Test
    public void publishDueScheduledUpdates_whenNoDueScheduledUpdateExist_shouldUpdateNone() {
        // Given
        final String userId = UUID.randomUUID().toString();
        final UpdateStatus status = UpdateStatus.SCHEDULED;
        final UpdateRevision notDueUpdate1 = buildScheduledUpdate(userId, false);
        final UpdateRevision notDueUpdate2 = buildScheduledUpdate(userId, false);
        final List<UpdateRevision> scheduledUpdates = Arrays.asList(
                notDueUpdate1,
                notDueUpdate2
        );
        final Page<UpdateRevision> scheduledUpdatesPage = new PageImpl<>(scheduledUpdates);
        when(updateRevisionRepository.getAllLatestUpdateInfo(any(Pageable.class), eq(status)))
                .thenReturn(scheduledUpdatesPage);

        // When
        manager.publishDueScheduledUpdates();

        // Then
        verify(updateRevisionRepository).getAllLatestUpdateInfo(any(Pageable.class), eq(status));
        verify(updateRevisionRepository, times(0)).save(any(UpdateRevision.class));
    }

    @Test
    public void publishDueScheduledUpdates_whenNoScheduledUpdateExist_shouldUpdateNone() {
        // Given
        final UpdateStatus status = UpdateStatus.SCHEDULED;
        final List<UpdateRevision> scheduledUpdates = Collections.emptyList();
        final Page<UpdateRevision> scheduledUpdatesPage = new PageImpl<>(scheduledUpdates);
        when(updateRevisionRepository.getAllLatestUpdateInfo(any(Pageable.class), eq(status)))
                .thenReturn(scheduledUpdatesPage);

        // When
        manager.publishDueScheduledUpdates();

        // Then
        verify(updateRevisionRepository).getAllLatestUpdateInfo(any(Pageable.class), eq(status));
        verify(updateRevisionRepository, times(0)).save(any(UpdateRevision.class));
    }

    private UpdateRevision buildScheduledUpdate(String userId, boolean due) {
        final UpdateRevision.UpdateRevisionBuilder builder = getCreatedUpdateRevisionBuilder(userId);
        if (due) {
            builder.scheduledDate(Date.from(LocalDate.now().minusDays(1L).atStartOfDay(ZoneId.systemDefault()).toInstant()));
        } else {
            builder.scheduledDate(Date.from(LocalDate.now().plusDays(1L).atStartOfDay(ZoneId.systemDefault()).toInstant()));
        }
        return builder.build();
    }

    private List<UpdateRevision> getAllKindsOfUpdates(String userId) {
        final List<UpdateRevision> revisions = new ArrayList<>();

        revisions.add(getCreatedUpdateRevisionBuilder(userId)
                .status(UpdateStatus.DRAFT)
                .build()
        );
        revisions.add(getCreatedUpdateRevisionBuilder(userId)
                .status(UpdateStatus.SCHEDULED)
                .scheduledDate(new Date(1234567890L))
                .build()
        );
        revisions.add(getCreatedUpdateRevisionBuilder(userId)
                .status(UpdateStatus.SCHEDULED)
                .scheduledDate(new Date(9876543210L))
                .build()
        );
        revisions.add(getCreatedUpdateRevisionBuilder(userId)
                .status(UpdateStatus.PUBLISHED)
                .build()
        );
        revisions.add(getCreatedUpdateRevisionBuilder(userId)
                .status(UpdateStatus.ARCHIVED)
                .build()
        );

        return revisions;
    }
}