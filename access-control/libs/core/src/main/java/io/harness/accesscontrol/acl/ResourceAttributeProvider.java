package io.harness.accesscontrol.acl;

import io.harness.accesscontrol.scopes.core.Scope;

import java.util.Map;

public interface ResourceAttributeProvider {
  Map<String, String> getAttributes(Scope resourceScope, String resourceType, String resourceIdentifier);
}
