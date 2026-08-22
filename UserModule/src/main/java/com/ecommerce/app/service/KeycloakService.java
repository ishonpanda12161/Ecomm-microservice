package com.ecommerce.app.service;

import com.ecommerce.app.exception.APIException;
import com.ecommerce.app.payload.UserRequestDTO;
import jakarta.ws.rs.core.Response;
import lombok.RequiredArgsConstructor;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class KeycloakService {

    @Value("${keycloak.realm}")
    private String realm;
    @Value("${keycloak.client-uid}")
    private String clientUUID;

    private final Keycloak keycloak;


    public String createKeycloakUser(UserRequestDTO userRequestDTO)
    {
        UserRepresentation user = new UserRepresentation();
        user.setUsername(userRequestDTO.getUsername());
        user.setEmail(userRequestDTO.getEmail());
        user.setFirstName(userRequestDTO.getFirstName());
        user.setLastName(userRequestDTO.getLastName());
        user.setEnabled(true);

        CredentialRepresentation credential = new CredentialRepresentation();
        credential.setType(CredentialRepresentation.PASSWORD);
        credential.setValue(userRequestDTO.getPassword());
        credential.setTemporary(false);
        user.setCredentials(List.of(credential));

        Response response = keycloak.realm(realm).users().create(user);
        if(response.getStatus()!=Response.Status.CREATED.getStatusCode())
        {
            throw new APIException("Failed to create User","KEYCLOAK", LocalDateTime.now());
        }

        String location = response.getHeaderString("Location");
        return location.substring(location.lastIndexOf("/")+1);
    }

    public void assignRole(String keycloakId,String roleName)
    {
        try{
            RoleRepresentation role = keycloak
                    .realm(realm)
                    .clients()
                    .get(clientUUID)
                    .roles()
                    .get(roleName)
                    .toRepresentation();

            keycloak.realm(realm).users().get(keycloakId).roles().clientLevel(clientUUID).add(List.of(role));
        }catch (Exception e)
        {
            throw new APIException("Failed to assign Role to User","KEYCLOAK", LocalDateTime.now());
        }
    }


    public void updateKeycloakUser(String keycloakId,UserRequestDTO userRequestDTO)
    {
        try {
            UserResource userResource = keycloak.realm(realm).users().get(keycloakId);
            UserRepresentation user = userResource.toRepresentation();

            user.setUsername(userRequestDTO.getUsername());
            user.setEmail(userRequestDTO.getEmail());
            user.setFirstName(userRequestDTO.getFirstName());
            user.setLastName(userRequestDTO.getLastName());

            CredentialRepresentation credential = new CredentialRepresentation();
            credential.setType(CredentialRepresentation.PASSWORD);
            credential.setValue(userRequestDTO.getPassword());
            credential.setTemporary(false);

            userResource.update(user);
            userResource.resetPassword(credential);
        }catch (Exception e)
        {
            throw new APIException("Failed to update User","KEYCLOAK", LocalDateTime.now());
        }
    }

    public void deleteKeycloakUser(String keycloakId)
    {
        try{
            keycloak.realm(realm).users().get(keycloakId).remove();
        }catch (Exception e)
        {
            throw new APIException("Failed to delete User","KEYCLOAK", LocalDateTime.now());
        }
    }

}
