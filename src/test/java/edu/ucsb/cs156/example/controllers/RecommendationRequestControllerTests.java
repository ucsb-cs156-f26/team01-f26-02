package edu.ucsb.cs156.example.controllers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import edu.ucsb.cs156.example.ControllerTestCase;
import edu.ucsb.cs156.example.entities.RecommendationRequest;
import edu.ucsb.cs156.example.repositories.RecommendationRequestRepository;
import edu.ucsb.cs156.example.repositories.UserRepository;
import edu.ucsb.cs156.example.testconfig.TestConfig;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MvcResult;

@WebMvcTest(controllers = RecommendationRequestController.class)
@Import(TestConfig.class)
public class RecommendationRequestControllerTests extends ControllerTestCase {
  @MockitoBean RecommendationRequestRepository recommendationRequestRepository;
  @MockitoBean UserRepository userRepository;

  // Authorization tests for /api/ucsbdates/admin/all

  @Test
  public void logged_out_users_cannot_get_all() throws Exception {
    mockMvc
        .perform(get("/api/RecommendationRequest/all"))
        .andExpect(status().is(403)); // logged out users can't get all
  }

  @WithMockUser(roles = {"USER"})
  @Test
  public void logged_in_users_can_get_all() throws Exception {
    mockMvc.perform(get("/api/RecommendationRequest/all")).andExpect(status().is(200)); // logged
  }

  @Test
  public void logged_out_users_cannot_post() throws Exception {
    mockMvc
        .perform(
            post("/api/RecommendationRequest/post")
                .param("requesterEmail", "student@example.com")
                .param("professorEmail", "professor@example.com")
                .param("explanation", "Practice request")
                .param("dateRequested", "2026-10-21T12:00:00")
                .param("dateNeeded", "2026-11-01T12:00:00")
                .param("done", "false")
                .with(csrf()))
        .andExpect(status().is(403));
  }

  @WithMockUser(roles = {"USER"})
  @Test
  public void logged_in_regular_users_cannot_post() throws Exception {
    mockMvc
        .perform(
            post("/api/RecommendationRequest/post")
                .param("requesterEmail", "student@example.com")
                .param("professorEmail", "professor@example.com")
                .param("explanation", "Practice request")
                .param("dateRequested", "2026-10-21T12:00:00")
                .param("dateNeeded", "2026-11-01T12:00:00")
                .param("done", "false")
                .with(csrf()))
        .andExpect(status().is(403)); // only admins can post
  }

  @WithMockUser(roles = {"USER"})
  @Test
  public void logged_in_user_can_get_all_ucsbdates() throws Exception {

    // arrange
    LocalDateTime requested = LocalDateTime.parse("2026-10-21T12:00:00");
    LocalDateTime needed = LocalDateTime.parse("2026-11-01T12:00:00");

    RecommendationRequest request1 =
        RecommendationRequest.builder()
            .id(1L)
            .requesterEmail("student1@example.com")
            .professorEmail("professor1@example.com")
            .explanation("Graduate school")
            .dateRequested(requested)
            .dateNeeded(needed)
            .done(false)
            .build();

    RecommendationRequest request2 =
        RecommendationRequest.builder()
            .id(2L)
            .requesterEmail("student2@example.com")
            .professorEmail("professor2@example.com")
            .explanation("Scholarship")
            .dateRequested(requested)
            .dateNeeded(needed)
            .done(true)
            .build();

    ArrayList<RecommendationRequest> expectedRequests = new ArrayList<>();
    expectedRequests.addAll(Arrays.asList(request1, request2));

    when(recommendationRequestRepository.findAll()).thenReturn(expectedRequests);
    // act
    MvcResult response =
        mockMvc
            .perform(get("/api/RecommendationRequest/all"))
            .andExpect(status().isOk())
            .andReturn();

    // assert

    verify(recommendationRequestRepository, times(1)).findAll();
    String expectedJson = mapper.writeValueAsString(expectedRequests);
    String responseString = response.getResponse().getContentAsString();
    assertEquals(expectedJson, responseString);
  }

  @WithMockUser(roles = {"ADMIN", "USER"})
  @Test
  public void an_admin_user_can_post_a_new_recommendation_request() throws Exception {
    // arrange

    // arrange
    LocalDateTime requested = LocalDateTime.parse("2026-10-21T12:00:00");
    LocalDateTime needed = LocalDateTime.parse("2026-11-01T12:00:00");

    RecommendationRequest request =
        RecommendationRequest.builder()
            .requesterEmail("student@example.com")
            .professorEmail("professor@example.com")
            .explanation("Graduate school")
            .dateRequested(requested)
            .dateNeeded(needed)
            .done(true)
            .build();

    when(recommendationRequestRepository.save(eq(request))).thenReturn(request);

    // act
    MvcResult response =
        mockMvc
            .perform(
                post("/api/RecommendationRequest/post")
                    .param("requesterEmail", "student@example.com")
                    .param("professorEmail", "professor@example.com")
                    .param("explanation", "Graduate school")
                    .param("dateRequested", "2026-10-21T12:00:00")
                    .param("dateNeeded", "2026-11-01T12:00:00")
                    .param("done", "true")
                    .with(csrf()))
            .andExpect(status().isOk())
            .andReturn();

    // assert
    verify(recommendationRequestRepository, times(1)).save(request);
    String expectedJson = mapper.writeValueAsString(request);
    String responseString = response.getResponse().getContentAsString();
    assertEquals(expectedJson, responseString);
  }
}
