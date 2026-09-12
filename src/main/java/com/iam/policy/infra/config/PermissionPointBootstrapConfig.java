package com.iam.policy.infra.config;

import com.iam.common.domain.vo.Action;
import com.iam.common.domain.vo.Resource;
import com.iam.policy.domain.model.PermissionPoint;
import com.iam.policy.domain.repository.PermissionPointRepository;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/** Seeds the Permission Point catalog with GitHub-style OAuth scopes. */
@Component
@Order(40)
class PermissionPointBootstrapConfig implements ApplicationRunner {

  private static final Logger log = LoggerFactory.getLogger(PermissionPointBootstrapConfig.class);

  private final PermissionPointRepository permissionPointRepository;

  PermissionPointBootstrapConfig(PermissionPointRepository permissionPointRepository) {
    this.permissionPointRepository = permissionPointRepository;
  }

  @Override
  @Transactional
  public void run(ApplicationArguments args) {
    int created = 0;
    for (Seed seed : seeds()) {
      if (permissionPointRepository.existsByCode(seed.code())) {
        continue;
      }
      permissionPointRepository.save(
          PermissionPoint.create(
              seed.code(),
              seed.code(),
              seed.module(),
              new Action(seed.action()),
              new Resource(seed.resource()),
              seed.description()));
      created++;
    }
    if (created > 0) {
      log.info("Seeded {} permission points", created);
    }
  }

  private static List<Seed> seeds() {
    return List.of(
        new Seed("openid", "oidc", "oidc:OpenId", "arn:oidc:::openid", "OIDC openid"),
        new Seed("profile", "oidc", "oidc:Profile", "arn:oidc:::profile", "OIDC profile"),
        new Seed("email", "oidc", "oidc:Email", "arn:oidc:::email", "OIDC email"),
        new Seed(
            "write:ai_chat",
            "ai",
            "ai:Invoke",
            "arn:ai:::chat/*",
            "Explore AI chat text and privacy"),
        new Seed(
            "write:ai_audio", "ai", "ai:Invoke", "arn:ai:::audio/*", "Explore AI ASR and TTS"),
        new Seed("write:ai_rag", "ai", "ai:Invoke", "arn:ai:::rag/*", "Explore AI RAG"),
        new Seed(
            "write:ai_media",
            "ai",
            "ai:Invoke",
            "arn:ai:::media/*",
            "Explore AI image and vision"),
        new Seed(
            "write:ai_agent",
            "ai",
            "ai:Invoke",
            "arn:ai:::agent/*",
            "Explore AI pipelines workflows automations skills"),
        new Seed(
            "write:ai_tools",
            "ai",
            "ai:Invoke",
            "arn:ai:::tools/*",
            "Explore AI mcp tools and eval"),
        new Seed(
            "write:chat_messaging",
            "chat",
            "chat:Invoke",
            "arn:chat:::messaging/*",
            "Explore Chat messaging calls media notifications"),
        new Seed(
            "write:chat_social",
            "chat",
            "chat:Invoke",
            "arn:chat:::social/*",
            "Explore Chat social feed follow groups"),
        new Seed("admin:chat", "chat", "chat:Admin", "arn:chat:::admin/*", "Explore Chat admin"));
  }

  private record Seed(
      String code, String module, String action, String resource, String description) {}
}
