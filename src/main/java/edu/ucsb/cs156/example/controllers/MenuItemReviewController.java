package edu.ucsb.cs156.example.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import edu.ucsb.cs156.example.entities.MenuItemReview;
import edu.ucsb.cs156.example.repositories.MenuItemReviewRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.LocalDateTime;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** This is a REST controller for MenuItemReview */
@Tag(name = "MenuItemReview")
@RequestMapping("/api/menuitemreview")
@RestController
@Slf4j
public class MenuItemReviewController extends ApiController {

  @Autowired MenuItemReviewRepository menuItemReviewRepository;

  /**
   * List all menu item reviews
   *
   * @return an iterable of MenuItemReview
   */
  @Operation(summary = "List all menu item reviews")
  @PreAuthorize("hasRole('ROLE_USER')")
  @GetMapping("/all")
  public Iterable<MenuItemReview> allMenuItemReview() {
    Iterable<MenuItemReview> dates = menuItemReviewRepository.findAll();
    return dates;
  }

  /**
   * Create a new menu item review
   *
   * @param itemId the id of the menu item being reviewed
   * @param reviewerEmail the email of the reviewer
   * @param stars the number of stars the menu item got
   * @param dateReviewed the date revewed
   * @param comments the comments of the review
   * @return the saved menuitemreview
   */
  @Operation(summary = "Create a new menu item review")
  @PreAuthorize("hasRole('ROLE_ADMIN')")
  @PostMapping("/post")
  public MenuItemReview postMenuItemReview(
      @Parameter(name = "itemId") @RequestParam long itemId,
      @Parameter(name = "reviewerEmail") @RequestParam String reviewerEmail,
      @Parameter(name = "stars") @RequestParam int stars,
      @Parameter(name = "comments") @RequestParam String comments,
      @Parameter(
              name = "dateReviewed",
              description =
                  "date reviewed (in iso format, e.g. YYYY-mm-ddTHH:MM:SS; see https://en.wikipedia.org/wiki/ISO_8601)")
          @RequestParam("dateReviewed")
          @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
          LocalDateTime dateReviewed)
      throws JsonProcessingException {

    // For an explanation of @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    // See: https://www.baeldung.com/spring-date-parameters

    log.info("dateReviewed={}", dateReviewed);

    MenuItemReview menuItemReview = new MenuItemReview();
    menuItemReview.setItemId(itemId);
    menuItemReview.setReviewerEmail(reviewerEmail);
    menuItemReview.setStars(stars);
    menuItemReview.setComments(comments);
    menuItemReview.setDateReviewed(dateReviewed);

    MenuItemReview savedMenuItemReview = menuItemReviewRepository.save(menuItemReview);

    return menuItemReview;
  }
}
