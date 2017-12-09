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

package io.barracks.updateservice.rest;

import io.barracks.updateservice.manager.UpdateStatusManager;
import io.barracks.updateservice.model.UpdateStatus;
import io.barracks.updateservice.model.UpdateStatusCompatibility;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;

@RunWith(MockitoJUnitRunner.class)
public class UpdateStatusResourceTest {
    private UpdateStatusResource updateStatusResource;
    @Mock
    private UpdateStatusManager updateStatusManager;

    @Before
    public void setup() {
        updateStatusResource = new UpdateStatusResource(updateStatusManager);
    }

    @Test
    public void getAllStatusCompatibilities_shouldCallManager_andReturnResult() {
        // Given
        final UpdateStatusCompatibility expected = UpdateStatusCompatibility.builder().status(UpdateStatus.ARCHIVED).build();
        doReturn(Collections.singletonList(expected))
                .when(updateStatusManager).getAllCompatibilities();

        // When
        final List<UpdateStatusCompatibility> compatibilities = updateStatusResource.getAllStatusCompatibilities();

        // Then
        assertThat(compatibilities).containsExactlyInAnyOrder(expected);
    }

    @Test
    public void getStatusCompatibilities_shouldCallManager_andReturnResult() {
        // Given
        final UpdateStatus status = UpdateStatus.ARCHIVED;
        final UpdateStatusCompatibility expected = UpdateStatusCompatibility.builder().status(UpdateStatus.PUBLISHED).build();
        doReturn(expected).when(updateStatusManager).getCompatibilityFor(status);

        // When
        final UpdateStatusCompatibility result = updateStatusResource.getStatusCompatibilities(status.getName());

        // Then
        assertThat(result).isEqualTo(expected);
    }
}
