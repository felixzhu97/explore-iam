package com.iam.identity.controller;

import com.iam.identity.domain.model.Group;
import com.iam.identity.service.GroupService;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * AIP Identity Group API.
 *
 * @see <a href="https://google.aip.dev/135">AIP-135 Delete</a>
 * @see <a href="https://google.aip.dev/136">AIP-136 Custom methods</a>
 */
@RestController
@RequestMapping("/api/v1/groups")
public class GroupController {

  private final GroupService groupService;

  /**
   * Creates the group API controller.
   *
   * @param groupService group service
   */
  public GroupController(GroupService groupService) {
    this.groupService = groupService;
  }

  /**
   * Lists all IAM groups.
   *
   * @return group summaries
   */
  @GetMapping
  @PreAuthorize("hasAnyRole('IAM_ADMIN', 'IAM_AUDITOR')")
  public ListGroupsResponse list() {
    return new ListGroupsResponse(
        groupService.findAll().stream().map(GroupResponse::from).toList());
  }

  /**
   * Creates a new IAM group.
   *
   * @param request group creation payload
   * @return created group
   */
  @PostMapping
  @PreAuthorize("hasRole('IAM_ADMIN')")
  public ResponseEntity<GroupResponse> create(@RequestBody CreateGroupRequest request) {
    String displayName =
        request.displayName() != null ? request.displayName() : request.name();
    Group group = groupService.create(displayName);
    return ResponseEntity.status(HttpStatus.CREATED).body(GroupResponse.from(group));
  }

  /**
   * Adds a user to a group (AIP-136).
   *
   * @param group group id
   * @param request member payload
   * @return empty response
   */
  @PostMapping("/{group}:addMember")
  @PreAuthorize("hasRole('IAM_ADMIN')")
  public ResponseEntity<Void> addMember(
      @PathVariable String group, @RequestBody MemberRequest request) {
    groupService.addMember(group, request.userId());
    return ResponseEntity.noContent().build();
  }

  /**
   * Removes a user from a group (AIP-136).
   *
   * @param group group id
   * @param request member payload
   * @return empty response
   */
  @PostMapping("/{group}:removeMember")
  @PreAuthorize("hasRole('IAM_ADMIN')")
  public ResponseEntity<Void> removeMember(
      @PathVariable String group, @RequestBody MemberRequest request) {
    groupService.removeMember(group, request.userId());
    return ResponseEntity.noContent().build();
  }

  /**
   * Deletes an empty group.
   *
   * @param group group id
   * @return empty response
   */
  @DeleteMapping("/{group}")
  @PreAuthorize("hasRole('IAM_ADMIN')")
  public ResponseEntity<Void> delete(@PathVariable String group) {
    groupService.delete(group);
    return ResponseEntity.noContent().build();
  }

  /** Request body for creating a group. */
  public record CreateGroupRequest(String name, String displayName) {}

  /** AIP list response. */
  public record ListGroupsResponse(List<GroupResponse> groups) {}

  /** IAM group summary. */
  public record GroupResponse(String name, String id, String displayName, List<String> memberIds) {
    static GroupResponse from(Group group) {
      return new GroupResponse(
          "groups/" + group.getId(), group.getId(), group.getName(), group.memberUserIds());
    }
  }
}
