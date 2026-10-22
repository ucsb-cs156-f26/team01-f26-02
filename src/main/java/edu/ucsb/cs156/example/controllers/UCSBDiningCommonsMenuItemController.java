package edu.ucsb.cs156.example.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import edu.ucsb.cs156.example.entities.UCSBDiningCommonsMenuItem;
import edu.ucsb.cs156.example.errors.EntityNotFoundException;
import edu.ucsb.cs156.example.repositories.UCSBDiningCommonsMenuItemRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** This is a REST controller for UCSBDiningCommonsMenuItems */
@Tag(name = "UCSBDiningCommonsMenuItem")
@RequestMapping({"/api/UCSBDiningCommonsMenuItem"})
@RestController
@Slf4j
public class UCSBDiningCommonsMenuItemController extends ApiController {

  @Autowired UCSBDiningCommonsMenuItemRepository ucsbDiningCommonsMenuItemRepository;

  /**
   * List all UCSB dining commons menu items
   *
   * @return an iterable of UCSBDiningCommonsMenuItem
   */
  @Operation(summary = "List all ucsb dining commons menu items")
  @PreAuthorize("hasRole('ROLE_USER')")
  @GetMapping("/all")
  public Iterable<UCSBDiningCommonsMenuItem> allUCSBDiningCommonsMenuItems() {
    Iterable<UCSBDiningCommonsMenuItem> menuItems = ucsbDiningCommonsMenuItemRepository.findAll();
    return menuItems;
  }

  /**
   * Get a single dining commons menu item by id
   *
   * @param id the id of the dining commons menu item
   * @return the menu item, or a 404 response with a missing-id message
   */
  @Operation(summary = "Get a single dining commons menu item")
  @PreAuthorize("hasRole('ROLE_USER')")
  @GetMapping("")
  public ResponseEntity<?> getById(@Parameter(name = "id") @RequestParam Long id) {
    Optional<UCSBDiningCommonsMenuItem> ucsbDiningCommonsMenuItem =
        ucsbDiningCommonsMenuItemRepository.findById(id);

    if (ucsbDiningCommonsMenuItem.isEmpty()) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND)
          .body(genericMessage("id %s not found".formatted(id)));
    }

    return ResponseEntity.ok(ucsbDiningCommonsMenuItem.get());
  }

  /**
   * Create a new dining commons menu item
   *
   * @param diningCommonsCode which hall it is in
   * @param name the name of the menu item
   * @param station type of food (entree, desert, etc)
   * @return the saved UCSBDiningCommonsMenuItem
   */
  @Operation(summary = "Create a new dining commons menu item")
  @PreAuthorize("hasRole('ROLE_ADMIN')")
  @PostMapping("/post")
  public UCSBDiningCommonsMenuItem postUCSBDiningCommonsMenuItem(
      @Parameter(name = "diningCommonsCode") @RequestParam String diningCommonsCode,
      @Parameter(name = "name") @RequestParam String name,
      @Parameter(name = "station") @RequestParam String station)
      throws JsonProcessingException {

    UCSBDiningCommonsMenuItem ucsbDiningCommonsMenuItem = new UCSBDiningCommonsMenuItem();
    ucsbDiningCommonsMenuItem.setDiningCommonsCode(diningCommonsCode);
    ucsbDiningCommonsMenuItem.setName(name);
    ucsbDiningCommonsMenuItem.setStation(station);

    UCSBDiningCommonsMenuItem savedUcsbDiningCommonsMenuItem =
        ucsbDiningCommonsMenuItemRepository.save(ucsbDiningCommonsMenuItem);

    return savedUcsbDiningCommonsMenuItem;
  }

  /**
   * Delete a UCSBDiningCommonsMenuItem
   *
   * @param id the id of the menu item to delete
   * @return a message indicating the menu item was deleted
   */
  @Operation(summary = "Delete a UCSBDiningCommonsMenuItem")
  @PreAuthorize("hasRole('ROLE_ADMIN')")
  @DeleteMapping("")
  public Object deleteUCSBDiningCommonsMenuItem(@Parameter(name = "id") @RequestParam Long id) {
    UCSBDiningCommonsMenuItem ucsbDiningCommonsMenuItem =
        ucsbDiningCommonsMenuItemRepository
            .findById(id)
            .orElseThrow(() -> new EntityNotFoundException(UCSBDiningCommonsMenuItem.class, id));

    ucsbDiningCommonsMenuItemRepository.delete(ucsbDiningCommonsMenuItem);
    return genericMessage("UCSBDiningCommonsMenuItem with id %s deleted".formatted(id));
  }

  /**
   * Update a single dining commons menu item
   *
   * @param id id of the menu item to update
   * @param incoming the new menu item
   * @return the updated menu item object
   */
  @Operation(summary = "Update a single dining commons menu item")
  @PreAuthorize("hasRole('ROLE_ADMIN')")
  @PutMapping("")
  public UCSBDiningCommonsMenuItem updateUCSBDiningCommonsMenuItem(
      @Parameter(name = "id") @RequestParam Long id,
      @RequestBody @Valid UCSBDiningCommonsMenuItem incoming) {

    UCSBDiningCommonsMenuItem ucsbDiningCommonsMenuItem =
        ucsbDiningCommonsMenuItemRepository
            .findById(id)
            .orElseThrow(() -> new EntityNotFoundException(UCSBDiningCommonsMenuItem.class, id));

    ucsbDiningCommonsMenuItem.setDiningCommonsCode(incoming.getDiningCommonsCode());
    ucsbDiningCommonsMenuItem.setName(incoming.getName());
    ucsbDiningCommonsMenuItem.setStation(incoming.getStation());

    ucsbDiningCommonsMenuItemRepository.save(ucsbDiningCommonsMenuItem);

    return ucsbDiningCommonsMenuItem;
  }
}
