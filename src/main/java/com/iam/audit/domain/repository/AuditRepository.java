package com.iam.audit.domain.repository;

import com.iam.audit.domain.model.AdminActivity;
import com.iam.audit.domain.model.DataAccessLog;
import java.util.List;

/** Append-only audit log persistence port. */
public interface AuditRepository {

  /**
   * Persists a management event.
   *
   * @param event event to store
   * @return stored event
   */
  AdminActivity saveAdminActivity(AdminActivity event);

  /**
   * Persists an authorization decision log entry.
   *
   * @param log decision to store
   * @return stored decision
   */
  DataAccessLog saveAccessDecision(DataAccessLog log);

  /**
   * Returns recent management events.
   *
   * @param limit maximum events to return
   * @return events ordered by occurrence time descending
   */
  List<AdminActivity> findAdminActivities(int limit);

  /**
   * Returns recent authorization decisions.
   *
   * @param limit maximum decisions to return
   * @return decisions ordered by occurrence time descending
   */
  List<DataAccessLog> findAccessDecisions(int limit);
}
