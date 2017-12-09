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
import io.barracks.updateservice.model.UpdateStatusCompatibility;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class UpdateStatusManager {
    public UpdateStatusCompatibility getCompatibilityFor(UpdateStatus status) {
        return UpdateStatusCompatibility.builder()
                .status(status)
                .compatibilities(getCompatibleStatuses(status))
                .build();
    }

    public List<UpdateStatusCompatibility> getAllCompatibilities() {
        return Arrays.stream(UpdateStatus.values())
                .map(this::getCompatibilityFor)
                .collect(Collectors.toList());
    }

    boolean areCompatible(UpdateStatus from, UpdateStatus to) {
        return getCompatibleStatuses(from).contains(to);
    }

    /**
     * Return all the status compatible with the given status
     * Ex: if an updateInfo is a DRAFT, it cannot be ARCHIVED, it must be PUBLISHED before
     *
     * @return a list of compatible status for the specified status
     */
    List<UpdateStatus> getCompatibleStatuses(UpdateStatus status) {
        switch (status) {
            case DRAFT:
                return Arrays.asList(UpdateStatus.DRAFT, UpdateStatus.PUBLISHED, UpdateStatus.SCHEDULED);

            case PUBLISHED:
                return Collections.singletonList(UpdateStatus.ARCHIVED);

            case ARCHIVED:
                return Collections.singletonList(UpdateStatus.PUBLISHED);

            case SCHEDULED:
                return Arrays.asList(UpdateStatus.SCHEDULED, UpdateStatus.DRAFT, UpdateStatus.PUBLISHED, UpdateStatus.ARCHIVED);

            default:
                return Collections.emptyList();
        }
    }
}
