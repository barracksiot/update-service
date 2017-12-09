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

import io.barracks.updateservice.model.UpdateStatus;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class UpdateStatusManagerTest {
    private UpdateStatusManager updateStatusManager = spy(new UpdateStatusManager());

    @Before
    public void setup() {
        reset(updateStatusManager);
    }

    @Test
    public void getAllCompatibilities_shouldGetCompatibilitiesForAllStatuses() {
        updateStatusManager.getAllCompatibilities();
        for (UpdateStatus status : UpdateStatus.values()) {
            verify(updateStatusManager).getCompatibilityFor(status);
        }
    }

    @Test
    public void getCompatibilities_whenStatusIsDraft_shouldReturnStatuesCompatibleWithDraft() {
        checkCompatibilitiesForStatus(UpdateStatus.DRAFT, UpdateStatus.DRAFT, UpdateStatus.PUBLISHED, UpdateStatus.SCHEDULED);
    }

    @Test
    public void getCompatibilities_whenStatusIsScheduled_shouldReturnStatuesCompatibleWithScheduled() {
        checkCompatibilitiesForStatus(UpdateStatus.SCHEDULED, UpdateStatus.SCHEDULED, UpdateStatus.DRAFT, UpdateStatus.PUBLISHED, UpdateStatus.ARCHIVED);
    }

    @Test
    public void getCompatibilities_whenStatusIsPublished_shouldReturnStatuesCompatibleWithPublished() {
        checkCompatibilitiesForStatus(UpdateStatus.PUBLISHED, UpdateStatus.ARCHIVED);
    }

    @Test
    public void getCompatibilities_whenStatusIsArchived_shouldReturnStatuesCompatibleWithArchived() {
        checkCompatibilitiesForStatus(UpdateStatus.ARCHIVED, UpdateStatus.PUBLISHED);
    }

    private void checkCompatibilitiesForStatus(UpdateStatus status, UpdateStatus... expectedStatuses) {
        assertThat(updateStatusManager.getCompatibilityFor(status).getCompatibilities()).containsExactlyInAnyOrder(expectedStatuses);
    }

    @Test
    public void testArchivedIsCompatibleWithArchived() {
        Assert.assertFalse(updateStatusManager.areCompatible(UpdateStatus.ARCHIVED, UpdateStatus.ARCHIVED));
    }

    @Test
    public void testArchivedIsCompatibleWithDraft() {
        Assert.assertFalse(updateStatusManager.areCompatible(UpdateStatus.ARCHIVED, UpdateStatus.DRAFT));
    }

    @Test
    public void testArchivedIsCompatibleWithPublished() {
        Assert.assertTrue(updateStatusManager.areCompatible(UpdateStatus.ARCHIVED, UpdateStatus.PUBLISHED));
    }

    @Test
    public void testArchivedIsCompatibleWithScheduled() {
        Assert.assertFalse(updateStatusManager.areCompatible(UpdateStatus.ARCHIVED, UpdateStatus.SCHEDULED));
    }

    @Test
    public void testDraftIsCompatibleWithArchived() {
        Assert.assertFalse(updateStatusManager.areCompatible(UpdateStatus.DRAFT, UpdateStatus.ARCHIVED));
    }

    @Test
    public void testDraftIsCompatibleWithDraft() {
        Assert.assertTrue(updateStatusManager.areCompatible(UpdateStatus.DRAFT, UpdateStatus.DRAFT));
    }

    @Test
    public void testDraftIsCompatibleWithPublished() {
        Assert.assertTrue(updateStatusManager.areCompatible(UpdateStatus.DRAFT, UpdateStatus.PUBLISHED));
    }

    @Test
    public void testDraftIsCompatibleWithScheduled() {
        Assert.assertTrue(updateStatusManager.areCompatible(UpdateStatus.DRAFT, UpdateStatus.SCHEDULED));
    }

    @Test
    public void testPublishedIsCompatibleWithArchived() {
        Assert.assertTrue(updateStatusManager.areCompatible(UpdateStatus.PUBLISHED, UpdateStatus.ARCHIVED));
    }

    @Test
    public void testPublishedIsCompatibleWithDraft() {
        Assert.assertFalse(updateStatusManager.areCompatible(UpdateStatus.PUBLISHED, UpdateStatus.DRAFT));
    }

    @Test
    public void testPublishedIsCompatibleWithPublished() {
        Assert.assertFalse(updateStatusManager.areCompatible(UpdateStatus.PUBLISHED, UpdateStatus.PUBLISHED));
    }

    @Test
    public void testPublishedIsCompatibleWithScheduled() {
        Assert.assertFalse(updateStatusManager.areCompatible(UpdateStatus.PUBLISHED, UpdateStatus.SCHEDULED));
    }

    @Test
    public void testScheduledIsCompatibleWithArchived() {
        Assert.assertTrue(updateStatusManager.areCompatible(UpdateStatus.SCHEDULED, UpdateStatus.ARCHIVED));
    }

    @Test
    public void testScheduledIsCompatibleWithDraft() {
        Assert.assertTrue(updateStatusManager.areCompatible(UpdateStatus.SCHEDULED, UpdateStatus.DRAFT));
    }

    @Test
    public void testScheduledIsCompatibleWithPublished() {
        Assert.assertTrue(updateStatusManager.areCompatible(UpdateStatus.SCHEDULED, UpdateStatus.PUBLISHED));
    }

    @Test
    public void testScheduledIsCompatibleWithScheduled() {
        Assert.assertTrue(updateStatusManager.areCompatible(UpdateStatus.SCHEDULED, UpdateStatus.SCHEDULED));
    }

}
