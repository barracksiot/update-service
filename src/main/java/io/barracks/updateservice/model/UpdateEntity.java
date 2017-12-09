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

import com.fasterxml.jackson.annotation.*;
import cz.jirutka.validator.spring.SpELAssert;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import org.hibernate.validator.constraints.NotBlank;
import org.mongodb.morphia.annotations.Embedded;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Getter
@Builder(toBuilder = true)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@ToString
@EqualsAndHashCode
@SpELAssert(value = "validate()", message = "Invalid couple status / scheduled date")
public class UpdateEntity {
    public static final String DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSSX";

    @NotBlank
    private final String userId;
    @NotBlank
    private final String name;
    private final String description;
    @NotBlank
    private final String packageId;
    private final String segmentId;
    @NotNull
    private final Map<String, Object> additionalProperties;
    @Embedded
    private final UpdateStatus status;
    @JsonFormat(pattern = DATE_FORMAT)
    private final Date scheduledDate;

    private UpdateEntity(
            String userId,
            String name,
            String description,
            String packageId,
            @Nullable String segmentId,
            Map<String, Object> additionalProperties,
            UpdateStatus status,
            Date scheduledDate
    ) {
        this.userId = userId;
        this.name = name;
        this.description = description;
        this.packageId = packageId;
        this.segmentId = segmentId;
        this.additionalProperties = additionalProperties == null ? new HashMap<>() : new HashMap<>(additionalProperties);
        this.status = status;
        this.scheduledDate = (scheduledDate == null || status != UpdateStatus.SCHEDULED ? null : new Date(scheduledDate.getTime()));
    }

    @JsonCreator
    public static UpdateEntity fromJson(
            @JsonProperty("userId") String userId,
            @JsonProperty("name") String name,
            @JsonProperty("description") String description,
            @JsonProperty("packageId") String packageId,
            @JsonProperty("segmentId") @Nullable String segmentId,
            @JsonProperty("additionalProperties") Map<String, Object> additionalProperties,
            @JsonProperty("status") UpdateStatus status,
            @JsonProperty("scheduledDate") Date scheduledDate
    ) {
        return new UpdateEntity(userId, name, description, packageId, segmentId, additionalProperties, status, scheduledDate);
    }


    @JsonProperty("scheduledDate")
    @JsonFormat(pattern = DATE_FORMAT)
    public Date getScheduledDate() {
        if (this.scheduledDate == null) {
            return null;
        } else {
            return new Date(scheduledDate.getTime());
        }
    }

    public Map<String, Object> getAdditionalProperties() {
        return new HashMap<>(additionalProperties);
    }

    public boolean validate() {
        if (UpdateStatus.SCHEDULED.equals(status)) {
            return scheduledDate != null;
        }
        return true;
    }

    // TODO Find a way to check if there is a missing parameter
    public UpdateRevision toUpdateRevision() {
        return UpdateRevision.builder()
                .userId(this.getUserId())
                .name(this.getName())
                .description(this.getDescription())
                .packageId(this.getPackageId())
                .segmentId(this.getSegmentId())
                .additionalProperties(this.getAdditionalProperties())
                .status(this.getStatus())
                .scheduledDate(this.getScheduledDate())
                .build();
    }
}
