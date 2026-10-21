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
import edu.ucsb.cs156.example.entities.Article;
import edu.ucsb.cs156.example.repositories.ArticleRepository;
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

@WebMvcTest(controllers = ArticlesController.class)
@Import(TestConfig.class)
public class ArticlesControllerTests extends ControllerTestCase {

  @MockitoBean ArticleRepository articleRepository;

  @MockitoBean UserRepository userRepository;

  // Authorization tests for /api/articles/admin/all

  @Test
  public void logged_out_users_cannot_get_all() throws Exception {
    mockMvc
        .perform(get("/api/articles/all"))
        .andExpect(status().is(403)); // logged out users can't get all
  }

  @WithMockUser(roles = {"USER"})
  @Test
  public void logged_in_users_can_get_all() throws Exception {
    mockMvc.perform(get("/api/articles/all")).andExpect(status().is(200)); // logged
  }

  // Authorization tests for /api/articles/post
  // (Perhaps should also have these for put and delete)

  @Test
  public void logged_out_users_cannot_post() throws Exception {
    mockMvc
        .perform(
            post("/api/articles/post")
                .param("title", "A new tool makes mapping the ocean easier")
                .param(
                    "url", "https://news.ucsb.edu/2026/022878/new-tool-makes-mapping-ocean-easier")
                .param(
                    "explanation",
                    "A new open-source software package developed by UCSB marine scientists makes it"
                        + " faster and simpler to find, organize and map data")
                .param("email", "harrisontasoff@ucsb.edu")
                .param("dateAdded", "2022-01-03T00:00:00")
                .with(csrf()))
        .andExpect(status().is(403));
  }

  @WithMockUser(roles = {"USER"})
  @Test
  public void logged_in_regular_users_cannot_post() throws Exception {
    mockMvc
        .perform(
            post("/api/articles/post")
                .param("title", "A new tool makes mapping the ocean easier")
                .param(
                    "url", "https://news.ucsb.edu/2026/022878/new-tool-makes-mapping-ocean-easier")
                .param(
                    "explanation",
                    "A new open-source software package developed by UCSB marine scientists makes it"
                        + " faster and simpler to find, organize and map data")
                .param("email", "harrisontasoff@ucsb.edu")
                .param("dateAdded", "2022-01-03T00:00:00")
                .with(csrf()))
        .andExpect(status().is(403)); // only admins can post
  }

  // // Tests with mocks for database actions

  @WithMockUser(roles = {"USER"})
  @Test
  public void logged_in_user_can_get_all_articles() throws Exception {

    // arrange
    LocalDateTime ldt1 = LocalDateTime.parse("2022-01-03T00:00:00");

    Article article1 =
        Article.builder()
            .title("A new tool makes mapping the ocean easier")
            .url("https://news.ucsb.edu/2026/022878/new-tool-makes-mapping-ocean-easier")
            .explanation(
                "A new open-source software package developed by UCSB marine scientists makes it"
                    + " faster and simpler to find, organize and map data")
            .email("harrisontasoff@ucsb.edu")
            .dateAdded(ldt1)
            .build();

    LocalDateTime ldt2 = LocalDateTime.parse("2024-01-04T02:00:00");

    Article article2 =
        Article.builder()
            .title("Cool beans")
            .url("fakeArticle.com")
            .explanation("A totally real online article not written by me")
            .email("zkwon@ucsb.edu")
            .dateAdded(ldt2)
            .build();

    ArrayList<Article> expectedArticles = new ArrayList<>();
    expectedArticles.addAll(Arrays.asList(article1, article2));

    when(articleRepository.findAll()).thenReturn(expectedArticles);

    // act
    MvcResult response =
        mockMvc.perform(get("/api/articles/all")).andExpect(status().isOk()).andReturn();

    // assert

    verify(articleRepository, times(1)).findAll();
    String expectedJson = mapper.writeValueAsString(expectedArticles);
    String responseString = response.getResponse().getContentAsString();
    assertEquals(expectedJson, responseString);
  }

  @WithMockUser(roles = {"ADMIN", "USER"})
  @Test
  public void an_admin_user_can_post_a_new_article() throws Exception {
    // arrange

    LocalDateTime ldt1 = LocalDateTime.parse("2022-01-03T00:00:00");

    Article article1 =
        Article.builder()
            .title("A new tool makes mapping the ocean easier")
            .url("https://news.ucsb.edu/2026/022878/new-tool-makes-mapping-ocean-easier")
            .explanation(
                "A new open-source software package developed by UCSB marine scientists makes it"
                    + " faster and simpler to find, organize and map data")
            .email("harrisontasoff@ucsb.edu")
            .dateAdded(ldt1)
            .build();

    when(articleRepository.save(eq(article1))).thenReturn(article1);

    // act
    MvcResult response =
        mockMvc
            .perform(
                post("/api/articles/post")
                    .param("title", "A new tool makes mapping the ocean easier")
                    .param(
                        "url",
                        "https://news.ucsb.edu/2026/022878/new-tool-makes-mapping-ocean-easier")
                    .param(
                        "explanation",
                        "A new open-source software package developed by UCSB marine scientists makes it"
                            + " faster and simpler to find, organize and map data")
                    .param("email", "harrisontasoff@ucsb.edu")
                    .param("dateAdded", "2022-01-03T00:00:00")
                    .with(csrf()))
            .andExpect(status().isOk())
            .andReturn();

    // assert
    verify(articleRepository, times(1)).save(article1);
    String expectedJson = mapper.writeValueAsString(article1);
    String responseString = response.getResponse().getContentAsString();
    assertEquals(expectedJson, responseString);
  }
}
