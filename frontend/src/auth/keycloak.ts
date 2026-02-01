import Keycloak from 'keycloak-js';

const keycloakConfig = {
  url: 'http://localhost:8091', // External URL for Keycloak
  realm: 'ticketing-security-realm',
  clientId: 'ticketing-client', // Assuming this is the client ID
};

const keycloak = new Keycloak(keycloakConfig);

export default keycloak;
