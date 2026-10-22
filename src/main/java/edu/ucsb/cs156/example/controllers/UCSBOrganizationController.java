package edu.ucsb.cs156.example.controllers;

import edu.ucsb.cs156.example.entities.UCSBOrganization;
import edu.ucsb.cs156.example.errors.EntityNotFoundException;
import edu.ucsb.cs156.example.repositories.UCSBOrganizationRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Tag(name = "UCSBOrganization")
@RequestMapping("/api/ucsborganization")
@RestController
@Slf4j
public class UCSBOrganizationController extends ApiController {

  @Autowired UCSBOrganizationRepository ucsbOrganizationRepository;

  @Operation(summary = "List all UCSB organizations")
  @PreAuthorize("hasRole('ROLE_USER')")
  @GetMapping("/all")
  public Iterable<UCSBOrganization> allOrganizations() {
    return ucsbOrganizationRepository.findAll();
  }

  @Operation(
      summary = "Get a single UCSB organization by orgCode",
      description =
          "Returns the organization matching the supplied orgCode. Returns 404 if no organization exists with that code.")
  @PreAuthorize("hasRole('ROLE_USER')")
  @GetMapping("")
  public UCSBOrganization getById(@Parameter(name = "orgCode") @RequestParam String orgCode) {

    return ucsbOrganizationRepository
        .findById(orgCode)
        .orElseThrow(() -> new EntityNotFoundException(UCSBOrganization.class, orgCode));
  }

  @Operation(summary = "Create a new UCSB organization")
  @PreAuthorize("hasRole('ROLE_ADMIN')")
  @PostMapping("/post")
  public UCSBOrganization postOrganization(
      @Parameter(name = "orgCode") @RequestParam String orgCode,
      @Parameter(name = "orgTranslationShort") @RequestParam String orgTranslationShort,
      @Parameter(name = "orgTranslation") @RequestParam String orgTranslation,
      @Parameter(name = "inactive") @RequestParam boolean inactive) {

    UCSBOrganization organization = new UCSBOrganization();

    organization.setOrgCode(orgCode);
    organization.setOrgTranslationShort(orgTranslationShort);
    organization.setOrgTranslation(orgTranslation);
    organization.setInactive(inactive);

    return ucsbOrganizationRepository.save(organization);
  }

  @Operation(
      summary = "Delete a UCSB organization by orgCode",
      description =
          "Deletes the organization matching the supplied orgCode. Returns 404 if no organization exists with that code.")
  @PreAuthorize("hasRole('ROLE_ADMIN')")
  @DeleteMapping("")
  public String deleteOrganization(@Parameter(name = "orgCode") @RequestParam String orgCode) {

    UCSBOrganization organization =
        ucsbOrganizationRepository
            .findById(orgCode)
            .orElseThrow(() -> new EntityNotFoundException(UCSBOrganization.class, orgCode));

    ucsbOrganizationRepository.delete(organization);

    return "record " + orgCode + " deleted";
  }

  @Operation(
      summary = "Update a UCSB organization by orgCode",
      description =
          "Updates the orgTranslationShort, orgTranslation, and inactive fields of an existing organization. "
              + "The organization is identified by orgCode, which cannot be changed. "
              + "Returns 404 if the organization does not exist.")
  @PreAuthorize("hasRole('ROLE_ADMIN')")
  @PutMapping("")
  public UCSBOrganization updateOrganization(
      @Parameter(name = "orgCode") @RequestParam String orgCode,
      @RequestBody @Valid UCSBOrganization incoming) {

    UCSBOrganization organization =
        ucsbOrganizationRepository
            .findById(orgCode)
            .orElseThrow(() -> new EntityNotFoundException(UCSBOrganization.class, orgCode));

    organization.setOrgTranslationShort(incoming.getOrgTranslationShort());
    organization.setOrgTranslation(incoming.getOrgTranslation());
    organization.setInactive(incoming.getInactive());

    ucsbOrganizationRepository.save(organization);

    return organization;
  }
}
