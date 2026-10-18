package com.iam.identity.infra.security;

import com.iam.common.security.IamPrincipals;
import com.iam.identity.domain.model.User;
import com.iam.identity.domain.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.Optional;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Ends a signed-in session once its IAM user is deleted or disabled, for example by SCIM, so the
 * session cannot authorize new tokens or call management APIs.
 */
public class ActiveUserSessionFilter extends OncePerRequestFilter {

  private final UserRepository userRepository;

  /**
   * Creates the filter.
   *
   * @param userRepository IAM users
   */
  public ActiveUserSessionFilter(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain chain)
      throws ServletException, IOException {
    Optional<String> userId =
        IamPrincipals.userId(SecurityContextHolder.getContext().getAuthentication());
    if (userId.isPresent()
        && userRepository.findById(userId.get()).filter(User::isEnabled).isEmpty()) {
      SecurityContextHolder.clearContext();
      HttpSession session = request.getSession(false);
      if (session != null) {
        session.invalidate();
      }
    }
    chain.doFilter(request, response);
  }
}
