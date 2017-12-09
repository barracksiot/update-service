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

import io.barracks.updateservice.utils.UpdateEntityUtils;
import org.junit.Test;

import java.util.Date;
import java.util.HashMap;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertFalse;

public class UpdateRevisionTest {

    @Test
    public void toString_whenHavingAUpdateInfoObject_shouldHaveACustomToStringMethod() {
        assertFalse(new UpdateRevision("", UUID.randomUUID(), "Name", "toto", "Description", "OBJECTID", "segmentId", 1, new HashMap<>(), new Date(), UpdateStatus.ARCHIVED, null).toString().contains("@"));
    }

    @Test
    public void getScheduledDate_whenScheduledDateIsNull_shouldReturnNull() {
        final UpdateEntity entity = UpdateEntityUtils.getEntityBuilder("coucou").scheduledDate(null).build();
        assertThat(entity.getScheduledDate()).isNull();
    }

    @Test
    public void getScheduledDate_whenScheduledDateIsNotNull_shouldReturnACopyOfTheCreationDate() {
        final Date scheduledDate = new Date(987654321L);
        final UpdateEntity entity = UpdateEntityUtils.getEntityBuilder("coucou").status(UpdateStatus.SCHEDULED).scheduledDate(scheduledDate).build();
        assertThat(entity.getScheduledDate()).hasSameTimeAs(scheduledDate);
        assertThat(entity.getScheduledDate()).isNotSameAs(scheduledDate);
    }
}
