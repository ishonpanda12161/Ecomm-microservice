package com.ecommerce.app.config.HttpClient;

import com.ecommerce.app.config.HttpService.KeycloakTokenResponse;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;
import org.springframework.web.service.annotation.PutExchange;

import java.util.List;
import java.util.Map;

//@HttpExchange
public interface KeycloakServiceClient {

//
//    @PostExchange(
//            value = "/realms/{realm}/protocol/openid-connect/token",
//            contentType = MediaType.APPLICATION_FORM_URLENCODED_VALUE
//    )
//    KeycloakTokenResponse getAccessToken(
//            @PathVariable String realm,
//            @RequestParam MultiValueMap<String,String> params
//    );
//
//    @PostExchange(
//            value = "/admin/realms/{realm}/users",
//            contentType = MediaType.APPLICATION_JSON_VALUE
//    )
//    ResponseEntity<String> createNewUser(
//            @RequestHeader("Authorization") String token,
//            @PathVariable String realm,
//            @RequestBody Map<String,Object> userPayload
//    );
//
//    @GetExchange(
//            value = "/admin/realms/{realm}/clients/{clientUid}/roles/{roleName}"
//    )
//    ResponseEntity<Map<String,Object>> getRoleRepresentation(
//            @RequestHeader("Authorization") String token,
//            @PathVariable String realm,
//            @PathVariable String clientUid,
//            @PathVariable String roleName
//    );
//
//    @PostExchange(
//            value = "/admin/realms/{realm}/users/{userId}/role-mappings/clients/{clientUid}",
//            contentType = MediaType.APPLICATION_JSON_VALUE
//    )
//    ResponseEntity<Void> assignClientRoleToUser(
//            @RequestHeader("Authorization") String token,
//            @RequestBody List<Map<String,Object>> roleRepresentation,
//            @PathVariable String realm,
//            @PathVariable String userId,
//            @PathVariable String clientUid
//    );

}
