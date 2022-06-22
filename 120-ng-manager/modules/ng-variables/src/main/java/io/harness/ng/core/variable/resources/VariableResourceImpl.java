/*
 * Copyright 2022 Harness Inc. All rights reserved.
 * Use of this source code is governed by the PolyForm Free Trial 1.0.0 license
 * that can be found in the licenses directory at the root of this repository, also available at
 * https://polyformproject.org/wp-content/uploads/2020/05/PolyForm-Free-Trial-1.0.0.txt.
 */

package io.harness.ng.core.variable.resources;

import static io.harness.ng.core.variable.VariablePermissions.VARIABLE_DELETE_PERMISSION;
import static io.harness.ng.core.variable.VariablePermissions.VARIABLE_EDIT_PERMISSION;
import static io.harness.ng.core.variable.VariablePermissions.VARIABLE_RESOURCE_TYPE;
import static io.harness.ng.core.variable.VariablePermissions.VARIABLE_VIEW_PERMISSION;

import io.harness.accesscontrol.NGAccessControlCheck;
import io.harness.accesscontrol.acl.api.Resource;
import io.harness.accesscontrol.acl.api.ResourceScope;
import io.harness.accesscontrol.clients.AccessControlClient;
import io.harness.annotations.dev.HarnessTeam;
import io.harness.annotations.dev.OwnedBy;
import io.harness.ng.beans.PageResponse;
import io.harness.ng.core.dto.ResponseDTO;
import io.harness.ng.core.variable.dto.VariableRequestDTO;
import io.harness.ng.core.variable.dto.VariableResponseDTO;
import io.harness.ng.core.variable.entity.Variable;
import io.harness.ng.core.variable.mappers.VariableMapper;
import io.harness.ng.core.variable.services.VariableService;

import com.google.inject.Inject;
import java.util.List;
import java.util.Optional;
import javax.ws.rs.NotFoundException;
import lombok.AllArgsConstructor;

@OwnedBy(HarnessTeam.PL)
@AllArgsConstructor(onConstructor = @__({ @Inject }))
public class VariableResourceImpl implements VariableResource {
  private final VariableService variableService;
  private final VariableMapper variableMapper;
  private final AccessControlClient accessControlClient;

  @Override
  @NGAccessControlCheck(resourceType = VARIABLE_RESOURCE_TYPE, permission = VARIABLE_VIEW_PERMISSION)
  public ResponseDTO<VariableResponseDTO> get(
      String identifier, String accountIdentifier, String orgIdentifier, String projectIdentifier) {
    Optional<VariableResponseDTO> variable =
        variableService.get(accountIdentifier, orgIdentifier, projectIdentifier, identifier);
    if (!variable.isPresent()) {
      throw new NotFoundException(String.format("Variable with identifier [%s] in project [%s] and org [%s] not found",
          identifier, projectIdentifier, orgIdentifier));
    }
    return ResponseDTO.newResponse(variable.get());
  }

  @Override
  public ResponseDTO<VariableResponseDTO> create(String accountIdentifier, VariableRequestDTO variableRequestDTO) {
    accessControlClient.checkForAccessOrThrow(
        ResourceScope.of(accountIdentifier, variableRequestDTO.getVariable().getOrgIdentifier(),
            variableRequestDTO.getVariable().getProjectIdentifier()),
        Resource.of(VARIABLE_RESOURCE_TYPE, null), VARIABLE_EDIT_PERMISSION);

    Variable createdVariable = variableService.create(accountIdentifier, variableRequestDTO.getVariable());
    return ResponseDTO.newResponse(variableMapper.toResponseWrapper(createdVariable));
  }

  @Override
  @NGAccessControlCheck(resourceType = VARIABLE_RESOURCE_TYPE, permission = VARIABLE_VIEW_PERMISSION)
  public ResponseDTO<PageResponse<VariableResponseDTO>> list(String accountIdentifier, String orgIdentifier,
      String projectIdentifier, int page, int size, String searchTerm, boolean includeVariablesFromEverySubScope) {
    return ResponseDTO.newResponse(variableService.list(accountIdentifier, orgIdentifier, projectIdentifier, page, size,
        searchTerm, includeVariablesFromEverySubScope));
  }

  @Override
  public ResponseDTO<VariableResponseDTO> update(String accountIdentifier, VariableRequestDTO variableRequestDTO) {
    accessControlClient.checkForAccessOrThrow(
        ResourceScope.of(accountIdentifier, variableRequestDTO.getVariable().getOrgIdentifier(),
            variableRequestDTO.getVariable().getProjectIdentifier()),
        Resource.of(VARIABLE_RESOURCE_TYPE, null), VARIABLE_EDIT_PERMISSION);
    Variable updatedVariable = variableService.update(accountIdentifier, variableRequestDTO.getVariable());
    return ResponseDTO.newResponse(variableMapper.toResponseWrapper(updatedVariable));
  }

  @Override
  @NGAccessControlCheck(resourceType = VARIABLE_RESOURCE_TYPE, permission = VARIABLE_DELETE_PERMISSION)
  public ResponseDTO<Boolean> delete(
      String accountIdentifier, String orgIdentifier, String projectIdentifier, String variableIdentifier) {
    boolean deleted = variableService.delete(accountIdentifier, orgIdentifier, projectIdentifier, variableIdentifier);
    return ResponseDTO.newResponse(deleted);
  }

  @Override
  public ResponseDTO<List<String>> expressions(
      String accountIdentifier, String orgIdentifier, String projectIdentifier) {
    return ResponseDTO.newResponse(variableService.getExpressions(accountIdentifier, orgIdentifier, projectIdentifier));
  }
}
