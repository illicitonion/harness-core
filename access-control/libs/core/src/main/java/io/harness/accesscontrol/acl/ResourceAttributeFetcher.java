package io.harness.accesscontrol.acl;

import static io.harness.data.structure.EmptyPredicate.isEmpty;

import io.harness.accesscontrol.scopes.core.Scope;
import io.harness.accesscontrol.scopes.harness.HarnessScopeParams;
import io.harness.accesscontrol.scopes.harness.ScopeMapper;
import io.harness.connector.ConnectorResourceClient;
import io.harness.environment.remote.EnvironmentResourceClient;
import io.harness.remote.client.NGRestUtils;

import com.google.inject.Inject;
import java.util.Collections;
import java.util.Map;

public class ResourceAttributeFetcher {
  @Inject EnvironmentResourceClient environmentResourceClient;
  @Inject ConnectorResourceClient connectorResourceClient;

  public Map<String, String> fetchAttributes(Scope resourceScope, String resourceType, String resourceIdentifier) {
    if (isEmpty(resourceIdentifier)) {
      return Collections.emptyMap();
    }

    HarnessScopeParams scope = ScopeMapper.toParams(resourceScope);

    switch (resourceType) {
      case "ENVIRONMENT":
        return NGRestUtils.getResponse(environmentResourceClient.getEnvironmentAttributes(
            scope.getAccountIdentifier(), scope.getOrgIdentifier(), scope.getProjectIdentifier(), resourceIdentifier));
      case "CONNECTOR":
        return NGRestUtils.getResponse(connectorResourceClient.getConnectorAttributes(
            scope.getAccountIdentifier(), scope.getOrgIdentifier(), scope.getProjectIdentifier(), resourceIdentifier));
      default:
        return Collections.emptyMap();
    }
  }
}
