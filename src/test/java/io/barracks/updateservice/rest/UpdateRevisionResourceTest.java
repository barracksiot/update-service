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

import com.fasterxml.jackson.databind.ObjectMapper;
import io.barracks.commons.test.ServiceClientTest;
import io.barracks.updateservice.config.ExceptionConfig;
import io.barracks.updateservice.exception.InvalidUpdateOperationException;
import io.barracks.updateservice.exception.NoSuchUpdateException;
import io.barracks.updateservice.exception.UpdateNotFoundException;
import io.barracks.updateservice.manager.UpdateRevisionManager;
import io.barracks.updateservice.model.UpdateEntity;
import io.barracks.updateservice.model.UpdateRevision;
import io.barracks.updateservice.model.UpdateStatus;
import io.barracks.updateservice.utils.UpdateEntityUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.HateoasPageableHandlerMethodArgumentResolver;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.MediaType;
import org.springframework.restdocs.RestDocumentation;
import org.springframework.restdocs.mockmvc.RestDocumentationResultHandler;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.*;

import static io.barracks.updateservice.utils.UpdateRevisionUtils.getCreatedUpdateRevisionBuilder;
import static io.barracks.updateservice.utils.UpdateRevisionUtils.getMatcherThatIgnoreUpdateUuidAndCreationDate;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.*;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@RunWith(MockitoJUnitRunner.class)
public class UpdateRevisionResourceTest extends ServiceClientTest {

    private static final String UPDATES_ENDPOINT = "/updates";
    private static final String LATEST_UPDATES_ENDPOINT = UPDATES_ENDPOINT + "/latest";

    @Rule
    public final RestDocumentation restDocumentation = new RestDocumentation("build/generated-snippets");

    private final HateoasPageableHandlerMethodArgumentResolver argumentResolver = new HateoasPageableHandlerMethodArgumentResolver();

    private MockMvc mvc;

    @Mock
    private UpdateRevisionManager updateRevisionManager;

    @Mock
    private PagedResourcesAssembler<UpdateRevision> assembler;

    @InjectMocks
    private UpdateRevisionResource updateRevisionResource;

    @Before
    public void setUp() throws Exception {
        RestDocumentationResultHandler document = document("{method-name}", preprocessRequest(prettyPrint()), preprocessResponse(prettyPrint()));
        updateRevisionResource = new UpdateRevisionResource(updateRevisionManager, new PagedResourcesAssembler<>(argumentResolver, null));
        this.mvc = MockMvcBuilders
                .standaloneSetup(updateRevisionResource)
                .setCustomArgumentResolvers(argumentResolver)
                .setHandlerExceptionResolvers(new ExceptionConfig().restExceptionResolver().build())
                .apply(documentationConfiguration(restDocumentation))
                .alwaysDo(document)
                .build();
    }

    @Test
    public void createUpdate_whenUserIdIsMissing_shouldReturn400BadRequest() throws Exception {
        // Given
        final String userId = UUID.randomUUID().toString();
        final UpdateEntity requestBody = UpdateEntityUtils.getEntityBuilder(userId)
                .userId(null)
                .build();
        final ObjectMapper mapper = new ObjectMapper();
        final String jsonRequestBody = mapper.writeValueAsString(requestBody);

        // When
        final ResultActions result = mvc.perform(
                MockMvcRequestBuilders.post(UPDATES_ENDPOINT)
                        .content(jsonRequestBody)
                        .contentType(MediaType.APPLICATION_JSON_UTF8)
                        .accept(MediaType.APPLICATION_JSON_UTF8)
        );

        // Then
        result.andExpect(status().isBadRequest());
    }

    @Test
    public void createUpdate_whenNameIsMissing_shouldReturn400BadRequest() throws Exception {
        // Given
        final String userId = UUID.randomUUID().toString();
        final UpdateEntity requestBody = UpdateEntityUtils.getEntityBuilder(userId)
                .name(null)
                .build();
        final ObjectMapper mapper = new ObjectMapper();
        final String jsonRequestBody = mapper.writeValueAsString(requestBody);

        // When
        final ResultActions result = mvc.perform(
                MockMvcRequestBuilders.post(UPDATES_ENDPOINT)
                        .content(jsonRequestBody)
                        .contentType(MediaType.APPLICATION_JSON_UTF8)
                        .accept(MediaType.APPLICATION_JSON_UTF8)
        );

        // Then
        result.andExpect(status().isBadRequest());
    }

    @Test
    public void createUpdate_whenPackageIdIsMissing_shouldReturn400BadRequest() throws Exception {
        // Given
        final String userId = UUID.randomUUID().toString();
        final UpdateEntity requestBody = UpdateEntityUtils.getEntityBuilder(userId)
                .packageId(null)
                .build();
        final ObjectMapper mapper = new ObjectMapper();
        final String jsonRequestBody = mapper.writeValueAsString(requestBody);

        // When
        final ResultActions result = mvc.perform(
                MockMvcRequestBuilders.post(UPDATES_ENDPOINT)
                        .content(jsonRequestBody)
                        .contentType(MediaType.APPLICATION_JSON_UTF8)
                        .accept(MediaType.APPLICATION_JSON_UTF8)
        );

        // Then
        result.andExpect(status().isBadRequest());
    }


    @Test
    public void createUpdate_whenSegmentIdIsMissing_shouldReturn200ok() throws Exception {
        // Given
        final String userId = UUID.randomUUID().toString();
        final UpdateEntity requestBody = UpdateEntityUtils.getEntityBuilder(userId)
                .segmentId(null)
                .build();
        final ObjectMapper mapper = new ObjectMapper();
        final String jsonRequestBody = mapper.writeValueAsString(requestBody);
        final UpdateRevision revisionSaved = requestBody.toUpdateRevision().toBuilder()
                .revisionId(1)
                .status(UpdateStatus.DRAFT)
                .build();
        when(updateRevisionManager.createUpdate(getMatcherThatIgnoreUpdateUuidAndCreationDate(requestBody.toUpdateRevision()))).thenReturn(revisionSaved);

        // When
        final ResultActions result = mvc.perform(
                MockMvcRequestBuilders.post(UPDATES_ENDPOINT)
                        .content(jsonRequestBody)
                        .contentType(MediaType.APPLICATION_JSON_UTF8)
                        .accept(MediaType.APPLICATION_JSON_UTF8)
        );

        // Then
        verify(updateRevisionManager).createUpdate(getMatcherThatIgnoreUpdateUuidAndCreationDate(requestBody.toUpdateRevision()));
        result.andExpect(status().isCreated());
        compareUpdateRevisionWithResult(result, revisionSaved, "");
    }

    @Test
    public void createUpdate_whenValidPayload_shouldReturnTheUpdateRevisionAnd200Ok() throws Exception {
        // Given
        final String userId = UUID.randomUUID().toString();
        final UpdateEntity requestBody = UpdateEntityUtils.getEntityBuilder(userId).build();
        final ObjectMapper mapper = new ObjectMapper();
        final String jsonRequestBody = mapper.writeValueAsString(requestBody);

        final UpdateRevision revisionSaved = requestBody.toUpdateRevision().toBuilder()
                .revisionId(1)
                .status(UpdateStatus.DRAFT)
                .build();

        when(updateRevisionManager.createUpdate(getMatcherThatIgnoreUpdateUuidAndCreationDate(requestBody.toUpdateRevision()))).thenReturn(revisionSaved);

        // When
        final ResultActions result = mvc.perform(
                MockMvcRequestBuilders.post(UPDATES_ENDPOINT)
                        .content(jsonRequestBody)
                        .contentType(MediaType.APPLICATION_JSON_UTF8)
                        .accept(MediaType.APPLICATION_JSON_UTF8)
        );

        // Then
        verify(updateRevisionManager).createUpdate(getMatcherThatIgnoreUpdateUuidAndCreationDate(requestBody.toUpdateRevision()));
        result.andExpect(status().isCreated());
        compareUpdateRevisionWithResult(result, revisionSaved, "");
    }

    @Test
    public void reviseUpdate_whenNoUserIdGiven_shouldReturn400BadRequest() throws Exception {
        // Given
        final String userId = UUID.randomUUID().toString();
        final UUID updateUuid = UUID.randomUUID();
        final UpdateEntity requestBody = UpdateEntityUtils.getEntityBuilder(userId)
                .build();
        final ObjectMapper mapper = new ObjectMapper();
        final String jsonRequestBody = mapper.writeValueAsString(requestBody);

        // When
        final ResultActions result = mvc.perform(
                MockMvcRequestBuilders.put(UPDATES_ENDPOINT + "/" + updateUuid.toString())
                        .content(jsonRequestBody)
                        .contentType(MediaType.APPLICATION_JSON_UTF8)
                        .accept(MediaType.APPLICATION_JSON_UTF8)
        );

        // Then
        result.andExpect(status().isBadRequest());
    }

    @Test
    public void reviseUpdate_whenManagerThrowUpdateNotFoundException_shouldReturn404NotFound() throws Exception {
        // Given
        final String userId = UUID.randomUUID().toString();
        final UUID updateUuid = UUID.randomUUID();
        final UpdateEntity requestBody = UpdateEntityUtils.getEntityBuilder(userId)
                .userId(userId)
                .build();
        final ObjectMapper mapper = new ObjectMapper();
        final String jsonRequestBody = mapper.writeValueAsString(requestBody);

        doThrow(UpdateNotFoundException.class)
                .when(updateRevisionManager).reviseUpdate(getMatcherThatIgnoreUpdateUuidAndCreationDate(requestBody.toUpdateRevision()));

        // When
        final ResultActions result = mvc.perform(
                MockMvcRequestBuilders.put(UPDATES_ENDPOINT + "/" + updateUuid.toString())
                        .param("userId", userId)
                        .content(jsonRequestBody)
                        .contentType(MediaType.APPLICATION_JSON_UTF8)
                        .accept(MediaType.APPLICATION_JSON_UTF8)
        );

        // Then
        verify(updateRevisionManager).reviseUpdate(getMatcherThatIgnoreUpdateUuidAndCreationDate(requestBody.toUpdateRevision()));
        result.andExpect(status().isNotFound());
    }

    @Test
    public void reviseUpdate_whenManagerThrowInvalidUpdateOperationException_shouldReturn400BadRequest() throws Exception {
        // Given
        final String userId = UUID.randomUUID().toString();
        final UUID updateUuid = UUID.randomUUID();
        final UpdateEntity requestBody = UpdateEntityUtils.getEntityBuilder(userId)
                .userId(userId)
                .build();
        final ObjectMapper mapper = new ObjectMapper();
        final String jsonRequestBody = mapper.writeValueAsString(requestBody);

        doThrow(new InvalidUpdateOperationException(""))
                .when(updateRevisionManager).reviseUpdate(getMatcherThatIgnoreUpdateUuidAndCreationDate(requestBody.toUpdateRevision()));

        // When
        final ResultActions result = mvc.perform(
                MockMvcRequestBuilders.put(UPDATES_ENDPOINT + "/" + updateUuid.toString())
                        .param("userId", userId)
                        .content(jsonRequestBody)
                        .contentType(MediaType.APPLICATION_JSON_UTF8)
                        .accept(MediaType.APPLICATION_JSON_UTF8)
        );

        // Then
        verify(updateRevisionManager).reviseUpdate(getMatcherThatIgnoreUpdateUuidAndCreationDate(requestBody.toUpdateRevision()));
        result.andExpect(status().isBadRequest());
    }

    @Test
    public void reviseUpdate_whenNoNameGiven_shouldReturn400BadRequest() throws Exception {
        // Given
        final String userId = UUID.randomUUID().toString();
        final UUID updateUuid = UUID.randomUUID();
        final UpdateEntity requestBody = UpdateEntityUtils.getEntityBuilder(userId)
                .name(null)
                .build(); // status field is missing in the update revision sent
        final ObjectMapper mapper = new ObjectMapper();
        final String jsonRequestBody = mapper.writeValueAsString(requestBody);

        // When
        final ResultActions result = mvc.perform(
                MockMvcRequestBuilders.put(UPDATES_ENDPOINT + "/" + updateUuid.toString())
                        .param("userId", userId)
                        .content(jsonRequestBody)
                        .contentType(MediaType.APPLICATION_JSON_UTF8)
                        .accept(MediaType.APPLICATION_JSON_UTF8)
        );

        // Then
        result.andExpect(status().isBadRequest());
    }

    @Test
    public void reviseUpdate_whenScheduledDateIsReceivedButStatusIsNotScheduled_shouldSendNoScheduledDateToTheManager() throws Exception {
        // Given
        final String userId = UUID.randomUUID().toString();
        final String uuid = UUID.randomUUID().toString();
        final UpdateEntity requestBody = UpdateEntityUtils.getEntityBuilder(userId)
                .status(UpdateStatus.PUBLISHED)
                .scheduledDate(new Date())
                .build();
        final ObjectMapper mapper = new ObjectMapper();
        final String jsonRequestBody = mapper.writeValueAsString(requestBody);
        final UpdateRevision updateRevision = requestBody.toUpdateRevision().toBuilder().uuid(UUID.fromString(uuid)).scheduledDate(null).build();
        when(updateRevisionManager.reviseUpdate(updateRevision)).thenReturn(updateRevision);

        // When
        final ResultActions result = mvc.perform(
                MockMvcRequestBuilders.put(UPDATES_ENDPOINT + "/" + uuid)
                        .param("userId", userId)
                        .content(jsonRequestBody)
                        .contentType(MediaType.APPLICATION_JSON_UTF8)
                        .accept(MediaType.APPLICATION_JSON_UTF8)
        );

        // Then
        verify(updateRevisionManager).reviseUpdate(updateRevision);
        result.andExpect(status().isOk());
    }

    @Test
    public void reviseUpdate_whenNoPackageIdGiven_shouldReturn400BadRequest() throws Exception {
        // Given
        final String userId = UUID.randomUUID().toString();
        final UUID updateUuid = UUID.randomUUID();
        final UpdateEntity requestBody = UpdateEntityUtils.getEntityBuilder(userId)
                .packageId(null)
                .build();
        final ObjectMapper mapper = new ObjectMapper();
        final String jsonRequestBody = mapper.writeValueAsString(requestBody);

        // When
        final ResultActions result = mvc.perform(
                MockMvcRequestBuilders.put(UPDATES_ENDPOINT + "/" + updateUuid.toString())
                        .param("userId", userId)
                        .content(jsonRequestBody)
                        .contentType(MediaType.APPLICATION_JSON_UTF8)
                        .accept(MediaType.APPLICATION_JSON_UTF8)
        );

        // Then
        result.andExpect(status().isBadRequest());
    }

    @Test
    public void reviseUpdate_whenNoSegmentIdGiven_shouldReturn200Ok() throws Exception {

        // Given
        final String userId = UUID.randomUUID().toString();
        final UUID updateUuid = UUID.randomUUID();
        final UpdateEntity requestBody = UpdateEntityUtils.getEntityBuilder(userId)
                .segmentId(null)
                .build();
        final ObjectMapper mapper = new ObjectMapper();
        final String jsonRequestBody = mapper.writeValueAsString(requestBody);
        final UpdateRevision revisionSaved = requestBody.toUpdateRevision().toBuilder()
                .revisionId(1)
                .status(UpdateStatus.DRAFT)
                .build();
        when(updateRevisionManager.reviseUpdate(getMatcherThatIgnoreUpdateUuidAndCreationDate(requestBody.toUpdateRevision()))).thenReturn(revisionSaved);

        // When
        final ResultActions result = mvc.perform(
                MockMvcRequestBuilders.put(UPDATES_ENDPOINT + "/" + updateUuid.toString())
                        .param("userId", userId)
                        .content(jsonRequestBody)
                        .contentType(MediaType.APPLICATION_JSON_UTF8)
                        .accept(MediaType.APPLICATION_JSON_UTF8)
        );

        // Then
        verify(updateRevisionManager).reviseUpdate(getMatcherThatIgnoreUpdateUuidAndCreationDate(requestBody.toUpdateRevision()));
        result.andExpect(status().isOk());
        compareUpdateRevisionWithResult(result, revisionSaved, "");
    }

    @Test
    public void reviseUpdate_whenValidPayloadGiven_shouldReturnTheUpdateRevisionAnd200Ok() throws Exception {
        // Given
        final String userId = UUID.randomUUID().toString();
        final UUID updateUuid = UUID.randomUUID();
        final UpdateEntity requestBody = UpdateEntityUtils.getEntityBuilder(userId)
                .status(UpdateStatus.DRAFT)
                .build();
        final ObjectMapper mapper = new ObjectMapper();
        final String jsonRequestBody = mapper.writeValueAsString(requestBody);
        final UpdateRevision revisedUpdate = requestBody.toUpdateRevision().toBuilder().revisionId(2).build();

        doReturn(revisedUpdate).when(updateRevisionManager).reviseUpdate(getMatcherThatIgnoreUpdateUuidAndCreationDate(requestBody.toUpdateRevision()));

        // When
        final ResultActions result = mvc.perform(
                MockMvcRequestBuilders.put(UPDATES_ENDPOINT + "/" + updateUuid.toString())
                        .param("userId", userId)
                        .content(jsonRequestBody)
                        .contentType(MediaType.APPLICATION_JSON_UTF8)
                        .accept(MediaType.APPLICATION_JSON_UTF8)
        );

        // Then
        verify(updateRevisionManager).reviseUpdate(getMatcherThatIgnoreUpdateUuidAndCreationDate(requestBody.toUpdateRevision()));
        result.andExpect(status().isOk());
        compareUpdateRevisionWithResult(result, revisedUpdate, "");
    }

    @Test
    public void getAllUpdates_whenNoUserIdGiven_shouldReturn400BadRequest() throws Exception {
        // Given
        final Pageable pageable = new PageRequest(0, 10);

        // When
        final ResultActions result = mvc.perform(
                MockMvcRequestBuilders.get(UPDATES_ENDPOINT)
                        .params(queryFrom(pageable))
                        .accept(MediaType.APPLICATION_JSON_UTF8)
        );

        // Then
        result.andExpect(status().isBadRequest());
    }

    @Test
    public void getAllUpdates_whenNoStatusFilterGiven_shouldGiveEmptyListFilterToTheManager() throws Exception {
        // Given
        final Pageable pageable = new PageRequest(0, 10);
        final String userId = UUID.randomUUID().toString();

        doReturn(new PageImpl<UpdateRevision>(Collections.emptyList())).when(updateRevisionManager).getAllUpdates(pageable, userId, Collections.emptyList(), Collections.emptyList());

        // When
        final ResultActions result = mvc.perform(
                MockMvcRequestBuilders.get(UPDATES_ENDPOINT)
                        .param("userId", userId)
                        .params(queryFrom(pageable))
                        .accept(MediaType.APPLICATION_JSON_UTF8)
        );

        // Then
        verify(updateRevisionManager).getAllUpdates(pageable, userId, Collections.emptyList(), Collections.emptyList());
        result.andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$.content", hasSize(0)));
    }

    @Test
    public void getAllUpdates_whenEmptyStatusFilterGiven_shouldGiveThoseFilterToTheManager() throws Exception {
        // Given
        final Pageable pageable = new PageRequest(0, 10);
        final String userId = UUID.randomUUID().toString();
        final List<String> statuses = Collections.emptyList();

        doReturn(new PageImpl<UpdateRevision>(Collections.emptyList())).when(updateRevisionManager).getAllUpdates(pageable, userId, statuses, Collections.emptyList());

        // When
        final ResultActions result = mvc.perform(
                MockMvcRequestBuilders.get(UPDATES_ENDPOINT)
                        .param("userId", userId)
                        .param("status", "")
                        .params(queryFrom(pageable))
                        .accept(MediaType.APPLICATION_JSON_UTF8)
        );

        // Then
        verify(updateRevisionManager).getAllUpdates(pageable, userId, statuses, Collections.emptyList());
        result.andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$.content", hasSize(0)));
    }

    @Test
    public void getAllUpdates_whenStatusFilterGiven_shouldGiveThoseFilterToTheManager() throws Exception {
        // Given
        final Pageable pageable = new PageRequest(0, 10);
        final String userId = UUID.randomUUID().toString();
        final List<String> statuses = Arrays.asList("aStatus", "anotherStatus");

        doReturn(new PageImpl<UpdateRevision>(Collections.emptyList())).when(updateRevisionManager).getAllUpdates(pageable, userId, statuses, Collections.emptyList());

        // When
        final ResultActions result = mvc.perform(
                MockMvcRequestBuilders.get(UPDATES_ENDPOINT)
                        .param("userId", userId)
                        .param("status", statuses.toArray(new String[0]))
                        .params(queryFrom(pageable))
                        .accept(MediaType.APPLICATION_JSON_UTF8)
        );

        // Then
        verify(updateRevisionManager).getAllUpdates(pageable, userId, statuses, Collections.emptyList());
        result.andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$.content", hasSize(0)));
    }

    @Test
    public void getAllUpdates_whenValidPayloadGivenAndSomeUpdatesExists_shouldReturnUpdateListAnd200Ok() throws Exception {
        // Given
        final Pageable pageable = new PageRequest(0, 10);
        final String userId = UUID.randomUUID().toString();
        final List<UpdateRevision> updates = new ArrayList<>();
        final UpdateRevision update1 = getCreatedUpdateRevisionBuilder(userId).build();
        final UpdateRevision update2 = getCreatedUpdateRevisionBuilder(userId).build();
        updates.add(update1);
        updates.add(update2);

        doReturn(new PageImpl<>(updates)).when(updateRevisionManager).getAllUpdates(pageable, userId, Collections.emptyList(), Collections.emptyList());

        // When
        final ResultActions result = mvc.perform(
                MockMvcRequestBuilders.get(UPDATES_ENDPOINT)
                        .param("userId", userId)
                        .params(queryFrom(pageable))
                        .accept(MediaType.APPLICATION_JSON_UTF8)
        );

        // Then
        verify(updateRevisionManager).getAllUpdates(pageable, userId, Collections.emptyList(), Collections.emptyList());
        result.andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$.content", hasSize(2)));
        compareUpdateRevisionWithResult(result, update1, "content[0]");
        compareUpdateRevisionWithResult(result, update2, "content[1]");
    }

    @Test
    public void getUpdateByUuid_whenNoUserIdGiven_shouldReturn400BadRequest() throws Exception {
        // Given
        final String updateUuid = UUID.randomUUID().toString();

        // When
        final ResultActions result = mvc.perform(
                MockMvcRequestBuilders.get(UPDATES_ENDPOINT + "/" + updateUuid)
                        .accept(MediaType.APPLICATION_JSON_UTF8)
        );

        // Then
        result.andExpect(status().isBadRequest());
    }

    @Test
    public void getUpdateByUuid_whenUpdateIdInvalid_shouldReturn404notFound() throws Exception {
        // Given
        final UUID updateUuid = UUID.randomUUID();
        final String userId = UUID.randomUUID().toString();

        doThrow(UpdateNotFoundException.class)
                .when(updateRevisionManager).getUpdateByUuid(updateUuid, userId);

        // When
        final ResultActions result = mvc.perform(
                MockMvcRequestBuilders.get(UPDATES_ENDPOINT + "/" + updateUuid + "?userId=" + userId)
                        .accept(MediaType.APPLICATION_JSON_UTF8)
        );

        // Then
        result.andExpect(status().isNotFound());
        verify(updateRevisionManager).getUpdateByUuid(updateUuid, userId);
    }

    @Test
    public void getUpdateByUuid_whenUpdateExists_shouldReturnThatUpdateAnd200Ok() throws Exception {
        // Given
        final UUID updateUuid = UUID.randomUUID();
        final String userId = UUID.randomUUID().toString();
        final UpdateRevision update = getCreatedUpdateRevisionBuilder(userId)
                .uuid(updateUuid)
                .build();

        when(updateRevisionManager.getUpdateByUuid(updateUuid, userId))
                .thenReturn(update);

        // When
        final ResultActions result = mvc.perform(
                MockMvcRequestBuilders.get(UPDATES_ENDPOINT + "/" + updateUuid + "?userId=" + userId)
                        .contentType(MediaTypes.HAL_JSON)
                        .accept(MediaType.APPLICATION_JSON_UTF8)
        );

        // Then
        result.andExpect(status().isOk());
        compareUpdateRevisionWithResult(result, update, "");
        verify(updateRevisionManager).getUpdateByUuid(updateUuid, userId);
    }

    @Test
    public void getLatestPublishedUpdate_whenNoUserIdGiven_shouldReturn400BadRequest() throws Exception {
        // When
        final String segmentId = UUID.randomUUID().toString();
        final ResultActions result = mvc.perform(
                MockMvcRequestBuilders.get(LATEST_UPDATES_ENDPOINT + "?segmentId=" + segmentId)
                        .accept(MediaType.APPLICATION_JSON_UTF8)
        );

        // Then
        result.andExpect(status().isBadRequest());
    }

    @Test
    public void getLatestPublishedUpdate_whenNoSegmentIdGiven_shouldCheckForOtherSegment() throws Exception {
        // Given
        final String userId = UUID.randomUUID().toString();
        final UpdateRevision update = getCreatedUpdateRevisionBuilder(userId)
                .status(UpdateStatus.PUBLISHED)
                .build();

        when(updateRevisionManager.getLatestPublishedUpdateForOtherSegment(userId))
                .thenReturn(update);

        // When
        final ResultActions result = mvc.perform(
                MockMvcRequestBuilders.get(LATEST_UPDATES_ENDPOINT + "?userId=" + userId)
                        .accept(MediaType.APPLICATION_JSON_UTF8)
        );

        // Then
        result.andExpect(status().isOk());
        verify(updateRevisionManager).getLatestPublishedUpdateForOtherSegment(userId);
    }

    @Test
    public void getLatestPublishedUpdate_whenSegmentIdEmpty_shouldCheckForOtherSegment() throws Exception {
        // Given
        final String userId = UUID.randomUUID().toString();
        final UpdateRevision update = getCreatedUpdateRevisionBuilder(userId)
                .status(UpdateStatus.PUBLISHED)
                .build();

        when(updateRevisionManager.getLatestPublishedUpdateForOtherSegment(userId))
                .thenReturn(update);

        // When
        final ResultActions result = mvc.perform(
                MockMvcRequestBuilders.get(LATEST_UPDATES_ENDPOINT + "?userId=" + userId +"&segmentId=")
                        .accept(MediaType.APPLICATION_JSON_UTF8)
        );

        // Then
        result.andExpect(status().isOk());
        verify(updateRevisionManager).getLatestPublishedUpdateForOtherSegment(userId);
    }

    @Test
    public void getLatestPublishedUpdate_whenManagerThrowNoSuchUpdateException_shouldReturn204NoContent() throws Exception {
        // Given
        final String userId = UUID.randomUUID().toString();
        final String segmentId = UUID.randomUUID().toString();
        when(updateRevisionManager.getLatestPublishedUpdateForSegment(userId, segmentId))
                .thenThrow(new NoSuchUpdateException(""));

        // When
        final ResultActions result = mvc.perform(
                MockMvcRequestBuilders.get(LATEST_UPDATES_ENDPOINT + "?userId=" + userId + "&segmentId=" + segmentId)
                        .accept(MediaType.APPLICATION_JSON_UTF8)
        );

        // Then
        result.andExpect(status().isNoContent());
        verify(updateRevisionManager).getLatestPublishedUpdateForSegment(userId, segmentId);
    }

    @Test
    public void getLatestPublishedUpdate_whenAtLeastOnePublishedUpdateExists_shouldReturnThatUpdateAnd200Ok() throws Exception {
        // Given
        final String userId = UUID.randomUUID().toString();
        final String segmentId = UUID.randomUUID().toString();
        final UpdateRevision update = getCreatedUpdateRevisionBuilder(userId)
                .status(UpdateStatus.PUBLISHED)
                .build();

        when(updateRevisionManager.getLatestPublishedUpdateForSegment(userId, segmentId))
                .thenReturn(update);

        // When
        final ResultActions result = mvc.perform(
                MockMvcRequestBuilders.get(LATEST_UPDATES_ENDPOINT + "?userId=" + userId + "&segmentId=" + segmentId)
                        .accept(MediaType.APPLICATION_JSON_UTF8)
        );

        // Then
        result.andExpect(status().isOk());
        compareUpdateRevisionWithResult(result, update, "");
        verify(updateRevisionManager).getLatestPublishedUpdateForSegment(userId, segmentId);
    }

    private void compareUpdateRevisionWithResult(ResultActions result, UpdateRevision update, String pathToUpdate) throws Exception {
        result.andExpect(jsonPath("$." + pathToUpdate + ".uuid").value(update.getUuid().toString()))
                .andExpect(jsonPath("$." + pathToUpdate + ".userId").value(update.getUserId()))
                .andExpect(jsonPath("$." + pathToUpdate + ".name").value(update.getName()))
                .andExpect(jsonPath("$." + pathToUpdate + ".description").value(update.getDescription()))
                .andExpect(jsonPath("$." + pathToUpdate + ".packageId").value(update.getPackageId()))
                .andExpect(jsonPath("$." + pathToUpdate + ".status").value(update.getStatus().getName()))
                .andExpect(jsonPath("$." + pathToUpdate + ".revisionId").value(update.getRevisionId()));
        if (update.getSegmentId() != null) {
            result.andExpect(jsonPath("$." + pathToUpdate + ".segmentId").value(update.getSegmentId()));
        } else {
            result.andExpect(jsonPath("$." + pathToUpdate + ".segmentId").doesNotExist());
        }
    }
}
