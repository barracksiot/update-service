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
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import org.hibernate.validator.constraints.NotBlank;
import org.mongodb.morphia.annotations.Embedded;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.PersistenceConstructor;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Builder(toBuilder = true)
@Getter
@Document(collection = "updates")
@CompoundIndex(name = "userId_revisionId_uuid_idx", def = "{'userId' : 1, 'revisionId' : 1, 'uuid' : 1}", unique = true)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@ToString
@EqualsAndHashCode
public class UpdateRevision {

    public static final String DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSSX";

    @Id
    @JsonIgnore
    private final String id;
    @NotNull
    private final UUID uuid;
    @NotBlank
    private final String userId;
    @NotBlank
    private final String name;
    private final String description;
    @NotBlank
    private final String packageId;
    private final String segmentId;
    @JsonIgnore
    private final Integer revisionId;
    @NotNull
    private final Map<String, Object> additionalProperties;
    @JsonIgnore
    @JsonFormat(pattern = DATE_FORMAT)
    @CreatedDate
    private final Date creationDate;
    @Embedded
    @NotNull
    private final UpdateStatus status;
    @JsonFormat(pattern = DATE_FORMAT)
    private final Date scheduledDate;

    @PersistenceConstructor
    public UpdateRevision(
            String id, UUID uuid, String userId, String name, String description, String packageId, @Nullable String segmentId,
            Integer revisionId, Map<String, Object> additionalProperties, Date creationDate, UpdateStatus status, Date scheduledDate
    ) {
        this.id = id;
        this.uuid = uuid == null ? UUID.randomUUID() : uuid;
        this.userId = userId;
        this.name = name;
        this.description = description;
        this.packageId = packageId;
        this.segmentId = segmentId;
        this.revisionId = revisionId;
        this.additionalProperties = additionalProperties == null ? new HashMap<>() : new HashMap<>(additionalProperties);
        this.creationDate = (creationDate == null ? null : new Date(creationDate.getTime()));
        this.status = status;
        this.scheduledDate = (scheduledDate == null ? null : new Date(scheduledDate.getTime()));
    }

    @JsonProperty("revisionId")
    public Integer getRevisionId() {
        return revisionId;
    }

    @JsonProperty("creationDate")
    @JsonFormat(pattern = DATE_FORMAT)
    public Date getCreationDate() {
        if (this.creationDate == null) {
            return null;
        }
        return new Date(creationDate.getTime());
    }

    @JsonProperty("scheduledDate")
    @JsonFormat(pattern = DATE_FORMAT)
    public Date getScheduledDate() {
        if (this.scheduledDate == null) {
            return null;
        }
        return new Date(scheduledDate.getTime());
    }

    public Map<String, Object> getAdditionalProperties() {
        return new HashMap<>(additionalProperties);
    }

    @JsonGetter("uuid")
    public String getUuidAsString() {
        return uuid.toString();
    }
}