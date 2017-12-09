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

import io.barracks.commons.test.MongoRepositoryTest;
import io.barracks.updateservice.model.UpdateRevision;
import org.junit.Before;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.IOException;
import java.util.Arrays;

import static org.junit.Assert.assertEquals;

abstract class UpdateInfoRepositoryTest extends MongoRepositoryTest {
    UpdateRevisionRepositoryImpl updateInfoRepository;
    MongoTemplate mongoTemplate;

    UpdateInfoRepositoryTest() {
        super(UpdateRevision.class.getDeclaredAnnotation(Document.class).collection());
    }

    @Before
    public void setUp() throws Exception {
        super.setUp();
        mongoTemplate = new MongoTemplate(getMongo(), getDatabaseName());
        updateInfoRepository = new UpdateRevisionRepositoryImpl(mongoTemplate);
    }

    void compareUpdateInfo(UpdateRevision expected, UpdateRevision actual) {
        assertEquals(expected.getUuid(), actual.getUuid());
        assertEquals(expected.getRevisionId(), actual.getRevisionId());
        assertEquals(expected.getName(), actual.getName());
        assertEquals(expected.getDescription(), actual.getDescription());
        assertEquals(expected.getPackageId(), actual.getPackageId());
        assertEquals(expected.getSegmentId(), actual.getSegmentId());
        assertEquals(expected.getCreationDate(), actual.getCreationDate());
        assertEquals(expected.getAdditionalProperties(), actual.getAdditionalProperties());
        assertEquals(expected.getUserId(), actual.getUserId());
        assertEquals(expected.getStatus(), actual.getStatus());
    }

    void insertUpdateInfoInDb(UpdateRevision... updateRevision) throws IOException {
        mongoTemplate.insertAll(Arrays.asList(updateRevision));
    }
}