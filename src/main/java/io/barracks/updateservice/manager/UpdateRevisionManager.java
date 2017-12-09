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

import io.barracks.updateservice.exception.InvalidUpdateOperationException;
import io.barracks.updateservice.exception.NoSuchUpdateException;
import io.barracks.updateservice.exception.UpdateNotFoundException;
import io.barracks.updateservice.model.UpdateRevision;
import io.barracks.updateservice.model.UpdateStatus;
import io.barracks.updateservice.repository.UpdateRevisionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class UpdateRevisionManager {

    private final UpdateRevisionRepository updateRevisionRepository;
    private final UpdateStatusManager updateStatusManager;

    @Autowired
    public UpdateRevisionManager(UpdateRevisionRepository updateRevisionRepository, UpdateStatusManager updateStatusManager) {
        this.updateRevisionRepository = updateRevisionRepository;
        this.updateStatusManager = updateStatusManager;
    }

    public UpdateRevision createUpdate(UpdateRevision updateRevision) {
        final UpdateRevision newUpdate = updateRevision.toBuilder()
                .revisionId(1)
                .status(UpdateStatus.DRAFT)
                .uuid(null)
                .build();
        return updateRevisionRepository.save(newUpdate);
    }

    public UpdateRevision reviseUpdate(UpdateRevision revision) {
        final Optional<UpdateRevision> currentRevision = updateRevisionRepository.findTopByUuidAndUserIdOrderByRevisionIdDesc(revision.getUuid(), revision.getUserId());
        final UpdateRevision update = currentRevision.orElseThrow(() ->
                new UpdateNotFoundException("No update with id " + revision.getUuid().toString())
        );

        if (!updateStatusManager.areCompatible(update.getStatus(), revision.getStatus())) {
            throw new InvalidUpdateOperationException("Cannot change status " + update.getStatus().getName() + " to status " + revision.getStatus().getName());
        }

        return updateRevisionRepository.save(
                revision.toBuilder()
                .id(null)
                .revisionId(update.getRevisionId() + 1)
                .creationDate(null)
                .build()
        );
    }

    public Page<UpdateRevision> getAllUpdates(Pageable pageable, String userId, List<String> statuses, List<String> segmentIds) {
        return updateRevisionRepository.getAllLatestUpdateInfo(pageable, Optional.of(userId), statuses, segmentIds);
    }

    public UpdateRevision getUpdateByUuid(UUID uuid, String userId) {
        final Optional<UpdateRevision> result = updateRevisionRepository.findTopByUuidAndUserIdOrderByRevisionIdDesc(uuid, userId);
        return result.orElseThrow(() -> new UpdateNotFoundException("No update with id " + uuid.toString()));
    }

    public UpdateRevision getLatestPublishedUpdateForSegment(String userId, String segmentId) {
        final Optional<UpdateRevision> latestUpdate = updateRevisionRepository.getLatestPublishedUpdateInfoBySegment(userId, segmentId);
        return latestUpdate.orElseThrow(() -> new NoSuchUpdateException("No published update"));
    }

    public UpdateRevision getLatestPublishedUpdateForOtherSegment(String userId) {
        final Optional<UpdateRevision> latestUpdate = updateRevisionRepository.getLatestPublishedUpdateInfoWithoutSegmentId(userId);
        return latestUpdate.orElseThrow(() -> new NoSuchUpdateException("No published update"));
    }

    public void publishDueScheduledUpdates() {
        final Date now = new Date();
        int pageNumber = 0;
        Page<UpdateRevision> page;
        do {
            final Pageable pageable = new PageRequest(pageNumber, 100);
            page = updateRevisionRepository.getAllLatestUpdateInfo(pageable, UpdateStatus.SCHEDULED);
            page.getContent().stream()
                    .filter(update -> now.after(update.getScheduledDate()))
                    .forEach(update -> this.reviseUpdate(
                            update.toBuilder()
                                    .status(UpdateStatus.PUBLISHED)
                                    .scheduledDate(null)
                                    .build()
                    ));
            pageNumber++;
        } while (page.hasNext());
    }

}
