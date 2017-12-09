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

import io.barracks.updateservice.manager.UpdateRevisionManager;
import io.barracks.updateservice.model.UpdateEntity;
import io.barracks.updateservice.model.UpdateRevision;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.PagedResources;
import org.springframework.hateoas.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/updates")
@SuppressWarnings("unused")
public class UpdateRevisionResource {

    private final UpdateRevisionManager manager;
    private final PagedResourcesAssembler<UpdateRevision> assembler;

    @Autowired
    public UpdateRevisionResource(UpdateRevisionManager updateRevisionManager, PagedResourcesAssembler<UpdateRevision> assembler) {
        this.manager = updateRevisionManager;
        this.assembler = assembler;
    }

    @ResponseBody
    @ResponseStatus(HttpStatus.CREATED)
    @RequestMapping(method = RequestMethod.POST)
    public UpdateRevision createUpdate(@Validated() @RequestBody UpdateEntity entity) {
        return manager.createUpdate(entity.toUpdateRevision());
    }

    @ResponseBody
    @ResponseStatus(HttpStatus.OK)
    @RequestMapping(method = RequestMethod.PUT, value = "/{updateUuid}")
    public UpdateRevision reviseUpdate(
            @Validated() @RequestBody UpdateEntity entity,
            @PathVariable("updateUuid") String updateUuid,
            @RequestParam(value = "userId") String userId
    ) {
        return manager.reviseUpdate(
                entity.toUpdateRevision()
                        .toBuilder()
                        .uuid(UUID.fromString(updateUuid))
                        .userId(userId)
                        .build()
        );
    }

    @ResponseBody
    @ResponseStatus(HttpStatus.OK)
    @RequestMapping(method = RequestMethod.GET, value = "/{updateUuid}")
    public UpdateRevision getUpdateByUuid(
            @PathVariable("updateUuid") String updateUuid,
            @RequestParam(value = "userId") String userId
    ) {
        return manager.getUpdateByUuid(UUID.fromString(updateUuid), userId);
    }

    @ResponseBody
    @ResponseStatus(HttpStatus.OK)
    @RequestMapping(method = RequestMethod.GET)
    public PagedResources<Resource<UpdateRevision>> getAllUpdates(
            Pageable pageable,
            @RequestParam(value = "userId") String userId,
            @RequestParam(value = "status", required = false, defaultValue = "") List<String> statuses,
            @RequestParam(value = "segmentId", required = false, defaultValue = "") List<String> segmentIds
    ) {
        Page<UpdateRevision> page = manager.getAllUpdates(pageable, userId, statuses, segmentIds);
        return assembler.toResource(page);
    }

    @ResponseBody
    @ResponseStatus(HttpStatus.OK)
    @RequestMapping(method = RequestMethod.GET, value = "/latest")
    public UpdateRevision getLatestPublishedUpdateForSegment(
            @RequestParam(value = "userId") String userId,
            @RequestParam(value = "segmentId") Optional<String> segmentId
    ) {
        if (segmentId.isPresent() && !StringUtils.isEmpty(segmentId.get())) {
            return manager.getLatestPublishedUpdateForSegment(userId, segmentId.get());
        } else {
            return manager.getLatestPublishedUpdateForOtherSegment(userId);
        }
    }
}
