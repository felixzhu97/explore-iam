package com.iam.federation.web;

import com.iam.federation.application.usecase.ListOidcClientsUseCase;
import com.iam.federation.application.usecase.RegisterOidcClientUseCase;
import com.iam.federation.application.usecase.RegisterOidcClientUseCase.RegisterOidcClientCommand;
import com.iam.federation.application.usecase.RegisterOidcClientUseCase.RegisteredOidcClientResult;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/clients")
public class ClientController {

    private final RegisterOidcClientUseCase registerOidcClientUseCase;
    private final ListOidcClientsUseCase listOidcClientsUseCase;

    public ClientController(
            RegisterOidcClientUseCase registerOidcClientUseCase,
            ListOidcClientsUseCase listOidcClientsUseCase) {
        this.registerOidcClientUseCase = registerOidcClientUseCase;
        this.listOidcClientsUseCase = listOidcClientsUseCase;
    }

    @PostMapping
    public ResponseEntity<ClientResponse> register(@RequestBody RegisterClientRequest request) {
        RegisteredOidcClientResult result = this.registerOidcClientUseCase.execute(
                new RegisterOidcClientCommand(
                        request.clientName(),
                        request.redirectUris(),
                        request.postLogoutRedirectUris(),
                        request.scopes(),
                        request.responseTypes(),
                        request.authorizationGrantTypes(),
                        request.clientAuthenticationMethods(),
                        request.clientUri()));
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(result));
    }

    @GetMapping
    public List<ClientResponse> list() {
        return this.listOidcClientsUseCase.list().stream()
                .map(ClientController::toResponse)
                .toList();
    }

    @GetMapping("/{clientId}")
    public ResponseEntity<ClientResponse> get(@PathVariable String clientId) {
        return this.listOidcClientsUseCase
                .findByClientId(clientId)
                .map(view -> ResponseEntity.ok(toResponse(view)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    private static ClientResponse toResponse(RegisteredOidcClientResult result) {
        return new ClientResponse(
                result.id(),
                result.clientId(),
                result.clientName(),
                result.clientSecret(),
                result.clientUri(),
                result.redirectUris(),
                result.postLogoutRedirectUris(),
                result.scopes(),
                result.responseTypes(),
                result.authorizationGrantTypes(),
                result.clientAuthenticationMethods());
    }

    private static ClientResponse toResponse(ListOidcClientsUseCase.OidcClientView view) {
        return new ClientResponse(
                view.id(),
                view.clientId(),
                view.clientName(),
                null,
                view.clientUri(),
                view.redirectUris(),
                view.postLogoutRedirectUris(),
                view.scopes(),
                view.responseTypes(),
                view.authorizationGrantTypes(),
                view.clientAuthenticationMethods());
    }
}
