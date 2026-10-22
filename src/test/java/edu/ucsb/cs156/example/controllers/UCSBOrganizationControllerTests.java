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
import edu.ucsb.cs156.example.entities.UCSBOrganization;
import edu.ucsb.cs156.example.repositories.UCSBOrganizationRepository;
import edu.ucsb.cs156.example.repositories.UserRepository;
import edu.ucsb.cs156.example.testconfig.TestConfig;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MvcResult;

@WebMvcTest(controllers = UCSBOrganizationController.class)
@Import(TestConfig.class)
public class UCSBOrganizationControllerTests extends ControllerTestCase {

  @MockitoBean UCSBOrganizationRepository ucsbOrganizationRepository;
  @MockitoBean UserRepository userRepository;

  private UCSBOrganization sampleOrganization() {
    return UCSBOrganization.builder()
        .orgCode("TEST")
        .orgTranslationShort("Test Org")
        .orgTranslation("Test Organization")
        .inactive(false)
        .build();
  }

  // GET ALL: authorization

  @Test
  public void logged_out_users_cannot_get_all() throws Exception {
    mockMvc.perform(get("/api/ucsborganization/all")).andExpect(status().isForbidden());
  }

  @WithMockUser(roles = {"USER"})
  @Test
  public void logged_in_users_can_get_all() throws Exception {
    UCSBOrganization org = sampleOrganization();

    when(ucsbOrganizationRepository.findAll()).thenReturn(Arrays.asList(org));

    MvcResult response =
        mockMvc.perform(get("/api/ucsborganization/all")).andExpect(status().isOk()).andReturn();

    verify(ucsbOrganizationRepository, times(1)).findAll();
    assertEquals(
        mapper.writeValueAsString(Arrays.asList(org)), response.getResponse().getContentAsString());
  }

  // GET BY ID

  @Test
  public void logged_out_users_cannot_get_by_id() throws Exception {
    mockMvc
        .perform(get("/api/ucsborganization").param("orgCode", "TEST"))
        .andExpect(status().isForbidden());
  }

  @WithMockUser(roles = {"USER"})
  @Test
  public void logged_in_user_can_get_existing_organization() throws Exception {
    UCSBOrganization org = sampleOrganization();

    when(ucsbOrganizationRepository.findById(eq("TEST"))).thenReturn(Optional.of(org));

    MvcResult response =
        mockMvc
            .perform(get("/api/ucsborganization").param("orgCode", "TEST"))
            .andExpect(status().isOk())
            .andReturn();

    verify(ucsbOrganizationRepository).findById("TEST");
    assertEquals(mapper.writeValueAsString(org), response.getResponse().getContentAsString());
  }

  @WithMockUser(roles = {"USER"})
  @Test
  public void get_nonexistent_organization_returns_404() throws Exception {
    when(ucsbOrganizationRepository.findById(eq("MISSING"))).thenReturn(Optional.empty());

    MvcResult response =
        mockMvc
            .perform(get("/api/ucsborganization").param("orgCode", "MISSING"))
            .andExpect(status().isNotFound())
            .andReturn();

    verify(ucsbOrganizationRepository).findById("MISSING");

    Map<String, Object> json = responseToJson(response);
    assertEquals("EntityNotFoundException", json.get("type"));
    assertEquals("UCSBOrganization with id MISSING not found", json.get("message"));
  }

  // POST

  @Test
  public void logged_out_users_cannot_post() throws Exception {
    mockMvc
        .perform(
            post("/api/ucsborganization/post")
                .param("orgCode", "TEST")
                .param("orgTranslationShort", "Test Org")
                .param("orgTranslation", "Test Organization")
                .param("inactive", "false")
                .with(csrf()))
        .andExpect(status().isForbidden());
  }

  @WithMockUser(roles = {"USER"})
  @Test
  public void regular_users_cannot_post() throws Exception {
    mockMvc
        .perform(
            post("/api/ucsborganization/post")
                .param("orgCode", "TEST")
                .param("orgTranslationShort", "Test Org")
                .param("orgTranslation", "Test Organization")
                .param("inactive", "false")
                .with(csrf()))
        .andExpect(status().isForbidden());
  }

  @WithMockUser(roles = {"ADMIN", "USER"})
  @Test
  public void admin_can_post_organization() throws Exception {
    UCSBOrganization org = sampleOrganization();
    org.setInactive(true);

    when(ucsbOrganizationRepository.save(eq(org))).thenReturn(org);

    MvcResult response =
        mockMvc
            .perform(
                post("/api/ucsborganization/post")
                    .param("orgCode", "TEST")
                    .param("orgTranslationShort", "Test Org")
                    .param("orgTranslation", "Test Organization")
                    .param("inactive", "true")
                    .with(csrf()))
            .andExpect(status().isOk())
            .andReturn();

    verify(ucsbOrganizationRepository).save(org);
    assertEquals(mapper.writeValueAsString(org), response.getResponse().getContentAsString());
  }

  // DELETE

  @Test
  public void logged_out_users_cannot_delete() throws Exception {
    mockMvc
        .perform(delete("/api/ucsborganization").param("orgCode", "TEST").with(csrf()))
        .andExpect(status().isForbidden());
  }

  @WithMockUser(roles = {"USER"})
  @Test
  public void regular_users_cannot_delete() throws Exception {
    mockMvc
        .perform(delete("/api/ucsborganization").param("orgCode", "TEST").with(csrf()))
        .andExpect(status().isForbidden());
  }

  @WithMockUser(roles = {"ADMIN", "USER"})
  @Test
  public void admin_can_delete_organization() throws Exception {
    UCSBOrganization org = sampleOrganization();

    when(ucsbOrganizationRepository.findById(eq("TEST"))).thenReturn(Optional.of(org));

    MvcResult response =
        mockMvc
            .perform(delete("/api/ucsborganization").param("orgCode", "TEST").with(csrf()))
            .andExpect(status().isOk())
            .andReturn();

    verify(ucsbOrganizationRepository).findById("TEST");
    verify(ucsbOrganizationRepository).delete(org);

    assertEquals("record TEST deleted", response.getResponse().getContentAsString());
  }

  @WithMockUser(roles = {"ADMIN", "USER"})
  @Test
  public void deleting_nonexistent_organization_returns_404() throws Exception {

    when(ucsbOrganizationRepository.findById("MISSING")).thenReturn(Optional.empty());

    MvcResult response =
        mockMvc
            .perform(delete("/api/ucsborganization").param("orgCode", "MISSING").with(csrf()))
            .andExpect(status().isNotFound())
            .andReturn();

    verify(ucsbOrganizationRepository).findById("MISSING");

    Map<String, Object> json = responseToJson(response);
    assertEquals("UCSBOrganization with id MISSING not found", json.get("message"));
  }

  // PUT

  @Test
  public void logged_out_users_cannot_put() throws Exception {
    UCSBOrganization org = sampleOrganization();

    mockMvc
        .perform(
            put("/api/ucsborganization")
                .param("orgCode", "TEST")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(org))
                .with(csrf()))
        .andExpect(status().isForbidden());
  }

  @WithMockUser(roles = {"USER"})
  @Test
  public void regular_users_cannot_put() throws Exception {
    UCSBOrganization org = sampleOrganization();

    mockMvc
        .perform(
            put("/api/ucsborganization")
                .param("orgCode", "TEST")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(org))
                .with(csrf()))
        .andExpect(status().isForbidden());
  }

  @WithMockUser(roles = {"ADMIN", "USER"})
  @Test
  public void admin_can_update_organization() throws Exception {
    UCSBOrganization original = sampleOrganization();

    UCSBOrganization updated =
        UCSBOrganization.builder()
            .orgCode("TEST")
            .orgTranslationShort("Updated")
            .orgTranslation("Updated Organization")
            .inactive(true)
            .build();

    when(ucsbOrganizationRepository.findById("TEST")).thenReturn(Optional.of(original));

    MvcResult response =
        mockMvc
            .perform(
                put("/api/ucsborganization")
                    .param("orgCode", "TEST")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(mapper.writeValueAsString(updated))
                    .with(csrf()))
            .andExpect(status().isOk())
            .andReturn();

    verify(ucsbOrganizationRepository).findById("TEST");
    verify(ucsbOrganizationRepository).save(updated);

    assertEquals(mapper.writeValueAsString(updated), response.getResponse().getContentAsString());
  }

  @WithMockUser(roles = {"ADMIN", "USER"})
  @Test
  public void updating_nonexistent_organization_returns_404() throws Exception {

    UCSBOrganization org = sampleOrganization();

    when(ucsbOrganizationRepository.findById("MISSING")).thenReturn(Optional.empty());

    MvcResult response =
        mockMvc
            .perform(
                put("/api/ucsborganization")
                    .param("orgCode", "MISSING")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(mapper.writeValueAsString(org))
                    .with(csrf()))
            .andExpect(status().isNotFound())
            .andReturn();

    verify(ucsbOrganizationRepository).findById("MISSING");

    Map<String, Object> json = responseToJson(response);
    assertEquals("UCSBOrganization with id MISSING not found", json.get("message"));
  }
}
