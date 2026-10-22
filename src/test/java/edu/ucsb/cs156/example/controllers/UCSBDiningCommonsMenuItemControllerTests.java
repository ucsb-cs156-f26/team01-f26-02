package edu.ucsb.cs156.example.controllers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import edu.ucsb.cs156.example.ControllerTestCase;
import edu.ucsb.cs156.example.entities.UCSBDiningCommonsMenuItem;
import edu.ucsb.cs156.example.repositories.UCSBDiningCommonsMenuItemRepository;
import edu.ucsb.cs156.example.repositories.UserRepository;
import edu.ucsb.cs156.example.testconfig.TestConfig;
import java.util.ArrayList;
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

@WebMvcTest(controllers = UCSBDiningCommonsMenuItemController.class)
@Import(TestConfig.class)
public class UCSBDiningCommonsMenuItemControllerTests extends ControllerTestCase {

  @MockitoBean UCSBDiningCommonsMenuItemRepository ucsbDiningCommonsMenuItemRepository;

  @MockitoBean UserRepository userRepository;

  // Tests for GET /api/UCSBDiningCommonsMenuItem/all

  @Test
  public void logged_out_users_cannot_get_all() throws Exception {
    mockMvc
        .perform(get("/api/UCSBDiningCommonsMenuItem/all"))
        .andExpect(status().is(403)); // logged out users can't get all
  }

  @WithMockUser(roles = {"USER"})
  @Test
  public void logged_in_users_can_get_all() throws Exception {
    mockMvc
        .perform(get("/api/UCSBDiningCommonsMenuItem/all"))
        .andExpect(status().is(200)); // logged
  }

  @WithMockUser(roles = {"USER"})
  @Test
  public void logged_in_user_can_get_all_ucsb_dining_commons_menu_items() throws Exception {

    // arrange
    UCSBDiningCommonsMenuItem ucsbDiningCommonsMenuItem1 =
        UCSBDiningCommonsMenuItem.builder()
            .name("Baked Pesto Pasta with Chicken")
            .diningCommonsCode("ortega")
            .station("Entree Specials")
            .build();

    UCSBDiningCommonsMenuItem ucsbDiningCommonsMenuItem2 =
        UCSBDiningCommonsMenuItem.builder()
            .name("Tofu Banh Mi Sandwich (v)")
            .diningCommonsCode("ortega")
            .station("Entree Specials")
            .build();

    ArrayList<UCSBDiningCommonsMenuItem> expectedMenuItems = new ArrayList<>();
    expectedMenuItems.addAll(Arrays.asList(ucsbDiningCommonsMenuItem1, ucsbDiningCommonsMenuItem2));

    when(ucsbDiningCommonsMenuItemRepository.findAll()).thenReturn(expectedMenuItems);

    // act
    MvcResult response =
        mockMvc
            .perform(get("/api/UCSBDiningCommonsMenuItem/all"))
            .andExpect(status().isOk())
            .andReturn();

    // assert

    verify(ucsbDiningCommonsMenuItemRepository, times(1)).findAll();
    String expectedJson = mapper.writeValueAsString(expectedMenuItems);
    String responseString = response.getResponse().getContentAsString();
    assertEquals(expectedJson, responseString);
  }

  // Tests for GET /api/UCSBDiningCommonsMenuItem?id={id}

  @Test
  public void logged_out_users_cannot_get_by_id() throws Exception {
    mockMvc
        .perform(get("/api/UCSBDiningCommonsMenuItem").param("id", "7"))
        .andExpect(status().is(403)); // logged out users can't get by id
  }

  @WithMockUser(roles = {"USER"})
  @Test
  public void test_that_logged_in_user_can_get_by_id_when_the_id_exists() throws Exception {

    // arrange
    UCSBDiningCommonsMenuItem ucsbDiningCommonsMenuItem =
        UCSBDiningCommonsMenuItem.builder()
            .name("Baked Pesto Pasta with Chicken")
            .diningCommonsCode("ortega")
            .station("Entree Specials")
            .build();

    when(ucsbDiningCommonsMenuItemRepository.findById(eq(7L)))
        .thenReturn(Optional.of(ucsbDiningCommonsMenuItem));

    // act
    MvcResult response =
        mockMvc
            .perform(get("/api/UCSBDiningCommonsMenuItem").param("id", "7"))
            .andExpect(status().isOk())
            .andReturn();

    // assert

    verify(ucsbDiningCommonsMenuItemRepository, times(1)).findById(eq(7L));
    String expectedJson = mapper.writeValueAsString(ucsbDiningCommonsMenuItem);
    String responseString = response.getResponse().getContentAsString();
    assertEquals(expectedJson, responseString);
  }

  @WithMockUser(roles = {"USER"})
  @Test
  public void test_that_logged_in_user_can_get_by_id_when_the_id_does_not_exist() throws Exception {

    // arrange

    when(ucsbDiningCommonsMenuItemRepository.findById(eq(7L))).thenReturn(Optional.empty());

    // act
    MvcResult response =
        mockMvc
            .perform(get("/api/UCSBDiningCommonsMenuItem").param("id", "7"))
            .andExpect(status().isNotFound())
            .andReturn();

    // assert

    verify(ucsbDiningCommonsMenuItemRepository, times(1)).findById(eq(7L));
    Map<String, Object> json = responseToJson(response);
    assertEquals("id 7 not found", json.get("message"));
  }

  // Tests for POST /api/UCSBDiningCommonsMenuItem/post

  @Test
  public void logged_out_users_cannot_post() throws Exception {
    mockMvc
        .perform(
            post("/api/UCSBDiningCommonsMenuItem/post")
                .param("name", "Baked Pesto Pasta with Chicken")
                .param("diningCommonsCode", "ortega")
                .param("station", "Entree Specials")
                .with(csrf()))
        .andExpect(status().is(403));
  }

  @WithMockUser(roles = {"USER"})
  @Test
  public void logged_in_regular_users_cannot_post() throws Exception {
    mockMvc
        .perform(
            post("/api/UCSBDiningCommonsMenuItem/post")
                .param("name", "Baked Pesto Pasta with Chicken")
                .param("diningCommonsCode", "ortega")
                .param("station", "Entree Specials")
                .with(csrf()))
        .andExpect(status().is(403)); // only admins can post
  }

  @WithMockUser(roles = {"ADMIN", "USER"})
  @Test
  public void an_admin_user_can_post_a_new_ucsb_dining_commons_menu_item() throws Exception {
    // arrange

    UCSBDiningCommonsMenuItem ucsbDiningCommonsMenuItem1 =
        UCSBDiningCommonsMenuItem.builder()
            .name("Baked Pesto Pasta with Chicken")
            .diningCommonsCode("ortega")
            .station("Entree Specials")
            .build();

    when(ucsbDiningCommonsMenuItemRepository.save(eq(ucsbDiningCommonsMenuItem1)))
        .thenReturn(ucsbDiningCommonsMenuItem1);

    // act
    MvcResult response =
        mockMvc
            .perform(
                post("/api/UCSBDiningCommonsMenuItem/post")
                    .param("name", "Baked Pesto Pasta with Chicken")
                    .param("diningCommonsCode", "ortega")
                    .param("station", "Entree Specials")
                    .with(csrf()))
            .andExpect(status().isOk())
            .andReturn();

    // assert
    verify(ucsbDiningCommonsMenuItemRepository, times(1)).save(ucsbDiningCommonsMenuItem1);
    String expectedJson = mapper.writeValueAsString(ucsbDiningCommonsMenuItem1);
    String responseString = response.getResponse().getContentAsString();
    assertEquals(expectedJson, responseString);
  }

  // Tests for DELETE /api/UCSBDiningCommonsMenuItem?id={id}

  @WithMockUser(roles = {"ADMIN", "USER"})
  @Test
  public void admin_can_delete_a_menu_item() throws Exception {
    // arrange

    UCSBDiningCommonsMenuItem ucsbDiningCommonsMenuItem1 =
        UCSBDiningCommonsMenuItem.builder()
            .name("Baked Pesto Pasta with Chicken")
            .diningCommonsCode("ortega")
            .station("Entree Specials")
            .build();

    when(ucsbDiningCommonsMenuItemRepository.findById(eq(15L)))
        .thenReturn(Optional.of(ucsbDiningCommonsMenuItem1));

    // act
    MvcResult response =
        mockMvc
            .perform(delete("/api/UCSBDiningCommonsMenuItem").param("id", "15").with(csrf()))
            .andExpect(status().isOk())
            .andReturn();

    // assert
    verify(ucsbDiningCommonsMenuItemRepository, times(1)).findById(15L);
    verify(ucsbDiningCommonsMenuItemRepository, times(1)).delete(any());

    Map<String, Object> json = responseToJson(response);
    assertEquals("UCSBDiningCommonsMenuItem with id 15 deleted", json.get("message"));
  }

  @WithMockUser(roles = {"ADMIN", "USER"})
  @Test
  public void
      admin_tries_to_delete_non_existant_ucsb_dining_commons_menu_item_and_gets_right_error_message()
          throws Exception {
    // arrange

    when(ucsbDiningCommonsMenuItemRepository.findById(eq(15L))).thenReturn(Optional.empty());

    // act
    MvcResult response =
        mockMvc
            .perform(delete("/api/UCSBDiningCommonsMenuItem").param("id", "15").with(csrf()))
            .andExpect(status().isNotFound())
            .andReturn();

    // assert
    verify(ucsbDiningCommonsMenuItemRepository, times(1)).findById(15L);
    Map<String, Object> json = responseToJson(response);
    assertEquals("UCSBDiningCommonsMenuItem with id 15 not found", json.get("message"));
  }

  // Tests for PUT /api/UCSBDiningCommonsMenuItem?id={id}

  @WithMockUser(roles = {"ADMIN", "USER"})
  @Test
  public void admin_can_edit_an_existing_ucsb_dining_commons_menu_item() throws Exception {
    // arrange

    UCSBDiningCommonsMenuItem ucsbDiningCommonsMenuItemOrig =
        UCSBDiningCommonsMenuItem.builder()
            .name("Baked Pesto Pasta with Chicken")
            .diningCommonsCode("ortega")
            .station("Entree Specials")
            .build();

    UCSBDiningCommonsMenuItem ucsbDiningCommonsMenuItemEdited =
        UCSBDiningCommonsMenuItem.builder()
            .name("Tofu Banh Mi Sandwich (v)")
            .diningCommonsCode("carrillo")
            .station("Deli")
            .build();

    String requestBody = mapper.writeValueAsString(ucsbDiningCommonsMenuItemEdited);

    when(ucsbDiningCommonsMenuItemRepository.findById(eq(67L)))
        .thenReturn(Optional.of(ucsbDiningCommonsMenuItemOrig));

    // act
    MvcResult response =
        mockMvc
            .perform(
                put("/api/UCSBDiningCommonsMenuItem")
                    .param("id", "67")
                    .contentType(MediaType.APPLICATION_JSON)
                    .characterEncoding("utf-8")
                    .content(requestBody)
                    .with(csrf()))
            .andExpect(status().isOk())
            .andReturn();

    // assert
    verify(ucsbDiningCommonsMenuItemRepository, times(1)).findById(67L);
    verify(ucsbDiningCommonsMenuItemRepository, times(1)).save(ucsbDiningCommonsMenuItemEdited);
    String responseString = response.getResponse().getContentAsString();
    assertEquals(requestBody, responseString);
  }

  @WithMockUser(roles = {"ADMIN", "USER"})
  @Test
  public void admin_cannot_edit_ucsb_dining_commons_menu_item_that_does_not_exist()
      throws Exception {
    // arrange

    UCSBDiningCommonsMenuItem ucsbEditedDiningCommonsMenuItem =
        UCSBDiningCommonsMenuItem.builder()
            .name("Baked Pesto Pasta with Chicken")
            .diningCommonsCode("ortega")
            .station("Entree Specials")
            .build();

    String requestBody = mapper.writeValueAsString(ucsbEditedDiningCommonsMenuItem);

    when(ucsbDiningCommonsMenuItemRepository.findById(eq(67L))).thenReturn(Optional.empty());

    // act
    MvcResult response =
        mockMvc
            .perform(
                put("/api/UCSBDiningCommonsMenuItem")
                    .param("id", "67")
                    .contentType(MediaType.APPLICATION_JSON)
                    .characterEncoding("utf-8")
                    .content(requestBody)
                    .with(csrf()))
            .andExpect(status().isNotFound())
            .andReturn();

    // assert
    verify(ucsbDiningCommonsMenuItemRepository, times(1)).findById(67L);
    Map<String, Object> json = responseToJson(response);
    assertEquals("UCSBDiningCommonsMenuItem with id 67 not found", json.get("message"));
  }
}
