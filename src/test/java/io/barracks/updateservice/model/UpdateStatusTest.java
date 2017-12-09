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

import io.barracks.updateservice.exception.UnknownUpdateStatusException;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

public class UpdateStatusTest {

    @Test
    public void testGetFromNameArchived() {
        Assert.assertEquals(UpdateStatus.ARCHIVED, UpdateStatus.fromName(UpdateStatus.ARCHIVED.getName()));
    }

    @Test
    public void testGetFromNameDraft() {
        Assert.assertEquals(UpdateStatus.DRAFT, UpdateStatus.fromName(UpdateStatus.DRAFT.getName()));
    }

    @Test
    public void testGetFromNamePublished() {
        Assert.assertEquals(UpdateStatus.PUBLISHED, UpdateStatus.fromName(UpdateStatus.PUBLISHED.getName()));
    }

    @Test
    public void testGetFromNameScheduled() {
        Assert.assertEquals(UpdateStatus.SCHEDULED, UpdateStatus.fromName(UpdateStatus.SCHEDULED.getName()));
    }

    @Test(expected = UnknownUpdateStatusException.class)
    public void testGetFromNameWithInvalidStatusName() {
        UpdateStatus.fromName("coucou");
    }

}
