package com.iam.identity.controller;

import com.iam.identity.domain.model.Group;
import com.iam.identity.service.GroupService;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** IAM group management API. */
@RestController
@RequestMapping("/api/identity/groups")
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
  public List<GroupResponse> list() {
    return groupService.findAll().stream().map(GroupResponse::from).toList();
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
    Group group = groupService.create(request.name());
    return ResponseEntity.status(HttpStatus.CREATED).body(GroupResponse.from(group));
  }

  /**
   * Adds a user to a group.
   *
   * @param groupId group id
   * @param request member payload
   * @return empty response
   */
  @PostMapping("/{groupId}/members")
  @PreAuthorize("hasRole('IAM_ADMIN')")
  public ResponseEntity<Void> addMember(
      @PathVariable String groupId, @RequestBody MemberRequest request) {
    groupService.addMember(groupId, request.userId());
    return ResponseEntity.noContent().build();
  }

  /** Request body for creating a group. */
  public record CreateGroupRequest(String name) {}

  /** IAM group summary. */
  public record GroupResponse(String id, String name) {
    static GroupResponse from(Group group) {
      return new GroupResponse(group.getId(), group.getName());
    }
  }
}
