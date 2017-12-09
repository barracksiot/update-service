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

package io.barracks.updateservice.repository;

import io.barracks.updateservice.model.UpdateRevision;
import io.barracks.updateservice.model.UpdateStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.aggregation.TypedAggregation;
import org.springframework.data.mongodb.core.query.Criteria;

import java.util.*;
import java.util.stream.Collectors;

import static org.springframework.data.mongodb.core.aggregation.Aggregation.match;
import static org.springframework.data.mongodb.core.query.Criteria.where;

public class UpdateRevisionRepositoryImpl implements UpdateRevisionRepositoryCustom {

    private static final String UUID_KEY = "uuid";
    private static final String SEGMENT_ID_KEY = "segmentId";
    private static final String DATE_KEY = "creationDate";
    private static final String USER_ID_KEY = "userId";
    private static final String UPDATE_STATUS_KEY = "status";

    private static final String OTHER_SEGMENT_KEY = "other";

    private final Logger log = LoggerFactory.getLogger(this.getClass());
    private final MongoOperations operations;

    @Autowired
    public UpdateRevisionRepositoryImpl(MongoOperations operations) {
        this.operations = operations;
    }

    @Override
    public Page<UpdateRevision> getAllLatestUpdateInfo(Pageable pageable, UpdateStatus status) {
        return getAllLatestUpdateInfo(pageable, Optional.empty(), Collections.singletonList(status.getName()), Collections.emptyList());
    }

    @Override
    public Page<UpdateRevision> getAllLatestUpdateInfo(Pageable pageable, Optional<String> userId, List<String> statuses, List<String> segmentIds) {
        // Basic operations
        final ArrayList<AggregationOperation> aggregationOperations = new ArrayList<>();
        userId.ifPresent(content -> aggregationOperations.add(match(where(USER_ID_KEY).is(content))));
        aggregationOperations.addAll(
                Arrays.asList(
                        Aggregation.sort(Sort.Direction.DESC, DATE_KEY),
                        Aggregation.group(UUID_KEY).first("$$ROOT").as("updateInfo"),
                        this.getProjectOperation()
                )
        );

        // Handle sorting
        ArrayList<AggregationOperation> sorts = new ArrayList<>();
        if (pageable.getSort() != null) {
            for (Sort.Order order : pageable.getSort()) {
                try {
                    UpdateRevision.class.getDeclaredField(order.getProperty());
                    sorts.add(Aggregation.sort(order.getDirection(), order.getProperty()));
                } catch (NoSuchFieldException e) {
                    log.warn("You can't sort updates using " + order.getProperty());
                }
            }
        }
        if (sorts.isEmpty()) {
            aggregationOperations.add(Aggregation.sort(Sort.Direction.DESC, DATE_KEY));
        } else {
            aggregationOperations.addAll(sorts);
        }

        // Handle statuses
        if (!statuses.isEmpty()) {
            final List<UpdateStatus> statusList = statuses.stream().map(UpdateStatus::fromName).collect(Collectors.toList());
            aggregationOperations.add(match(where(UPDATE_STATUS_KEY).in(statusList)));
        }

        // Handle segments
        if(!segmentIds.isEmpty()) {
            final List<Criteria> segmentMatch = new ArrayList<>(3);
            final List<String> realIds = segmentIds.stream().filter(id -> !OTHER_SEGMENT_KEY.equals(id)).collect(Collectors.toList());
            if(!realIds.isEmpty()) {
                segmentMatch.add(where(SEGMENT_ID_KEY).in(realIds));
            }
            boolean includeOther = segmentIds.stream().anyMatch(OTHER_SEGMENT_KEY::equals);
            if(includeOther) {
                segmentMatch.add(where(SEGMENT_ID_KEY).exists(false));
                segmentMatch.add(where(SEGMENT_ID_KEY).is(null));
            }
            aggregationOperations.add(match(new Criteria().orOperator(segmentMatch.toArray(new Criteria[]{}))));
        }

        // Count
        List<AggregationOperation> countAggregation = new ArrayList<>(aggregationOperations);
        countAggregation.add(Aggregation.group(new String[]{}).count().as("count"));
        Count result = operations.aggregate(Aggregation.newAggregation(UpdateRevision.class, countAggregation), Count.class).getUniqueMappedResult();
        int updateCount = result != null ? result.count : 0;

        // Skip and size
        aggregationOperations.addAll(
                Arrays.asList(
                        Aggregation.skip(pageable.getPageNumber() * pageable.getPageSize()),
                        Aggregation.limit(pageable.getPageSize()))
        );

        TypedAggregation<UpdateRevision> aggregation = Aggregation.newAggregation(
                UpdateRevision.class,
                aggregationOperations
        );
        AggregationResults<UpdateRevision> groupResult = operations.aggregate(aggregation, UpdateRevision.class);
        List<UpdateRevision> results = groupResult.getMappedResults();

        return new PageImpl<>(results, pageable, updateCount);
    }

    @Override
    public Optional<UpdateRevision> getLatestPublishedUpdateInfoBySegment(String userId, String segmentId) {
        TypedAggregation<UpdateRevision> aggregation = Aggregation.newAggregation(
                UpdateRevision.class,
                match(where(USER_ID_KEY).is(userId)),
                Aggregation.sort(Sort.Direction.DESC, DATE_KEY),
                Aggregation.group(UUID_KEY).first("$$ROOT").as("updateInfo"),
                this.getProjectOperation(),
                match(
                        new Criteria().andOperator(
                                where(SEGMENT_ID_KEY).is(segmentId),
                                where(UPDATE_STATUS_KEY).is(UpdateStatus.PUBLISHED.name())
                        )
                ),
                Aggregation.sort(Sort.Direction.DESC, DATE_KEY),
                Aggregation.limit(1)
        );

        return Optional.ofNullable(operations.aggregate(aggregation, UpdateRevision.class).getUniqueMappedResult());
    }

    @Override
    public Optional<UpdateRevision> getLatestPublishedUpdateInfoWithoutSegmentId(String userId) {
        TypedAggregation<UpdateRevision> aggregation = Aggregation.newAggregation(
                UpdateRevision.class,
                match(where(USER_ID_KEY).is(userId)),
                Aggregation.sort(Sort.Direction.DESC, DATE_KEY),
                Aggregation.group(UUID_KEY).first("$$ROOT").as("updateInfo"),
                this.getProjectOperation(),
                match(
                        new Criteria().andOperator(
                                new Criteria().orOperator(where(SEGMENT_ID_KEY).is(null), where(SEGMENT_ID_KEY).exists(false)),
                                where(UPDATE_STATUS_KEY).is(UpdateStatus.PUBLISHED.name())
                        )
                ),
                Aggregation.sort(Sort.Direction.DESC, DATE_KEY),
                Aggregation.limit(1)
        );

        return Optional.ofNullable(operations.aggregate(aggregation, UpdateRevision.class).getUniqueMappedResult());
    }

    private AggregationOperation getProjectOperation() {
        return Aggregation.project("updateInfo")
                .and("updateInfo._id").as("_id")
                .and("updateInfo.uuid").as("uuid")
                .and("updateInfo.packageId").as("packageId")
                .and("updateInfo.segmentId").as("segmentId")
                .and("updateInfo.name").as("name")
                .and("updateInfo.additionalProperties").as("additionalProperties")
                .and("updateInfo.userId").as("userId")
                .and("updateInfo.description").as("description")
                .and("updateInfo.revisionId").as("revisionId")
                .and("updateInfo.creationDate").as("creationDate")
                .and("updateInfo.scheduledDate").as("scheduledDate")
                .and("updateInfo.status").as("status");
    }

    private static class Count {
        private int count = 0;

        public Count(int count) {
            this.count = count;
        }
    }
}
