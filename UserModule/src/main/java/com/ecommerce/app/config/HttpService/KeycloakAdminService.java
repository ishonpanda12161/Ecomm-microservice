package com.ecommerce.app.config.HttpService;

import com.ecommerce.app.config.HttpClient.KeycloakServiceClient;
import com.ecommerce.app.exception.APIException;
import com.ecommerce.app.payload.UserRequestDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class KeycloakAdminService {

    @Value("${keycloak.adminUsername}")
    private String adminUsername;
    @Value("${keycloak.adminPassword}")
    private String adminPassword;
    @Value("${keycloak.realm}")
    private String realm;
    @Value("${keycloak.clientId}")
    private String clientId;
    @Value("${keycloak.client-uid}")
    private String clientUid;

    private final KeycloakServiceClient keycloakServiceClient;

    public String getAccessToken()
    {
        MultiValueMap<String,String> params = new LinkedMultiValueMap<>();
        params.add("client_id",clientId);
        params.add("username",adminUsername);
        params.add("password",adminPassword);
        params.add("grant_type","password");

        KeycloakTokenResponse response = keycloakServiceClient.getAccessToken(realm,params);
        return response.getAccessToken();
    }

    public String createUser(String token, UserRequestDTO userRequestDTO)
    {
        Map<String,Object> userPayload = new HashMap<>();
        userPayload.put("username",userRequestDTO.getUsername());
        userPayload.put("email",userRequestDTO.getEmail());
        userPayload.put("enabled",true);
        userPayload.put("firstName",userRequestDTO.getFirstName());
        userPayload.put("lastName",userRequestDTO.getLastName());

        Map<String,Object> credentials = new HashMap<>();
        credentials.put("type","password");
        credentials.put("value",userRequestDTO.getPassword());
        credentials.put("temporary",false);
        userPayload.put("credentials", List.of(credentials));

        ResponseEntity<String> response = keycloakServiceClient.createNewUser("Bearer "+token,realm,userPayload);
        if(!response.getStatusCode().equals(HttpStatus.CREATED))
        {
            throw new APIException(response.getBody(),"Keycloak User", LocalDateTime.now());
        }

        URI location = response.getHeaders().getLocation();
        if(location==null)
        {
            throw new APIException(response.getBody(),"Keycloak Location Header User", LocalDateTime.now());
        }

        String path = location.getPath();
        return path.substring(path.lastIndexOf("/")+1);

    }

    private Map<String,Object> getClientRoleRepresentation(String token,String roleName)
    {
        ResponseEntity<Map<String,Object>> response = keycloakServiceClient.getRoleRepresentation("Bearer "+token,realm,clientUid,roleName);
        return response.getBody();
    }

    public void assignClientRoleToUser(String username,String roleName,String userId)
    {
        String token = getAccessToken();
        Map<String,Object> roleRep = getClientRoleRepresentation(token,roleName);

        ResponseEntity<Void> response = keycloakServiceClient.assignClientRoleToUser("Bearer "+token,List.of(roleRep),realm,userId,clientUid);

        if(!response.getStatusCode().is2xxSuccessful())
        {
            throw new APIException("Failed to Assign Role: "+roleName+" to User: "+username,"Keycloak Role",LocalDateTime.now());
        }

    }

}
