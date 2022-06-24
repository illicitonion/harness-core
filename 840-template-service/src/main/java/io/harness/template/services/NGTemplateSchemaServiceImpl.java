/*
 * Copyright 2022 Harness Inc. All rights reserved.
 * Use of this source code is governed by the PolyForm Free Trial 1.0.0 license
 * that can be found in the licenses directory at the root of this repository, also available at
 * https://polyformproject.org/wp-content/uploads/2020/05/PolyForm-Free-Trial-1.0.0.txt.
 */

package io.harness.template.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import io.harness.EntityType;
import io.harness.account.AccountClient;
import io.harness.annotations.dev.HarnessTeam;
import io.harness.annotations.dev.OwnedBy;
import io.harness.beans.FeatureName;
import io.harness.encryption.Scope;
import io.harness.exception.InvalidRequestException;
import io.harness.exception.JsonSchemaException;
import io.harness.exception.ngexception.beans.yamlschema.YamlSchemaErrorDTO;
import io.harness.exception.ngexception.beans.yamlschema.YamlSchemaErrorWrapperDTO;
import io.harness.ng.core.template.TemplateEntityType;
import io.harness.pipeline.yamlschema.YamlSchemaServiceClient;
import io.harness.pms.yaml.YamlUtils;
import io.harness.remote.client.NGRestUtils;
import io.harness.template.entity.TemplateEntity;
import io.harness.template.helpers.YamlSchemaMergeHelper;
import io.harness.template.mappers.TemplateChildEntityTypeToEntityTypeMapper;
import io.harness.yaml.schema.YamlSchemaProvider;
import io.harness.yaml.utils.JsonPipelineUtils;
import io.harness.yaml.validator.YamlSchemaValidator;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.Collections;
import java.util.NoSuchElementException;

import static io.harness.annotations.dev.HarnessTeam.CDC;

@Singleton
@Slf4j
@OwnedBy(CDC)
public class NGTemplateSchemaServiceImpl implements NGTemplateSchemaService {
    private YamlSchemaServiceClient yamlSchemaServiceClient;
    private YamlSchemaProvider yamlSchemaProvider;
    private YamlSchemaValidator yamlSchemaValidator;
    private AccountClient accountClient;
    Integer allowedParallelStages;

    @Inject
    public NGTemplateSchemaServiceImpl(YamlSchemaServiceClient yamlSchemaServiceClient, YamlSchemaProvider yamlSchemaProvider,
                                       YamlSchemaValidator yamlSchemaValidator, AccountClient accountClient, @Named("allowedParallelStages") Integer allowedParallelStages){
        this.yamlSchemaServiceClient = yamlSchemaServiceClient;
        this.yamlSchemaProvider = yamlSchemaProvider;
        this .yamlSchemaValidator = yamlSchemaValidator;
        this.accountClient = accountClient;
        this.allowedParallelStages = allowedParallelStages;

    }

    @Override
    public JsonNode getTemplateSchema(String accountIdentifier, String projectIdentifier, String orgIdentifier, Scope scope, EntityType entityType, TemplateEntityType templateEntityType) {
        try {
            return getTemplateYamlSchemaInternal(accountIdentifier, projectIdentifier, orgIdentifier, scope, entityType, templateEntityType);
        } catch (Exception e) {
            log.error("[Template] Failed to get pipeline yaml schema", e);
            throw new JsonSchemaException(e.getMessage());
        }
    }

    private JsonNode getTemplateYamlSchemaInternal(String accountIdentifier, String projectIdentifier, String orgIdentifier, Scope scope, EntityType entityType, TemplateEntityType templateEntityType) {

        if(!schemaValidationSupported(templateEntityType)){
            return null;
        }

        JsonNode templateSchema =
                yamlSchemaProvider.getYamlSchema(EntityType.TEMPLATE, orgIdentifier, projectIdentifier, scope);

        String yamlGroup = getYamlGroup(templateEntityType);
        //TODO: add a handler here to fetch for schemas that we can't get from pipeline as discussed. and refactor
        JsonNode specSchema = NGRestUtils
                .getResponse(yamlSchemaServiceClient.getYamlSchema(accountIdentifier, orgIdentifier, projectIdentifier, yamlGroup, entityType, scope)).getSchema();
        //TODO: owner of TemplateEntityGroup pipeline we can map yamlGroup

        YamlSchemaMergeHelper.mergeYamlSchema(templateSchema, specSchema, entityType, templateEntityType);
        return templateSchema;
    }

    private String getYamlGroup(TemplateEntityType templateEntityType) {
        if(HarnessTeam.PIPELINE.equals(templateEntityType.getOwnerTeam())){
            switch (templateEntityType){
                case PIPELINE_TEMPLATE:
                    return "PIPELINE";
                case STAGE_TEMPLATE:
                    return "STAGE";
                case STEP_TEMPLATE:
                    return "STEP";
                default:
                    return null;
            }
        }
        return null;
    }

    private boolean schemaValidationSupported(TemplateEntityType templateEntityType){
        switch (templateEntityType){
            case PIPELINE_TEMPLATE:
            case STEP_TEMPLATE:
            case STAGE_TEMPLATE:
                return true;
            default:
                return false;
        }
    }

    public void validateYamlSchemaInternal(String accountIdentifier, String projectIdentifier, String orgIdentifier, Scope scope, TemplateEntity templateEntity, String templateYaml) {
        long start = System.currentTimeMillis();
        try {
            EntityType entityType = TemplateChildEntityTypeToEntityTypeMapper.getInstance().getEntityType(templateEntity.getChildType());
            if(entityType == null){
                throw new UnsupportedOperationException("TemplateEntityChildType " + templateEntity.getChildType() + " not supported");
            }
            if(scope == null){
                scope = projectIdentifier != null ? Scope.PROJECT : orgIdentifier != null ? Scope.ORG : Scope.ACCOUNT;
            }
            JsonNode schema = getTemplateSchema(accountIdentifier, projectIdentifier, orgIdentifier, scope, entityType, templateEntity.getTemplateEntityType());
            String schemaString = JsonPipelineUtils.writeJsonString(schema);
            yamlSchemaValidator.validate(templateYaml, schemaString,
                    YamlSchemaMergeHelper.isFeatureFlagEnabled(FeatureName.DONT_RESTRICT_PARALLEL_STAGE_COUNT, accountIdentifier, accountClient),
                    allowedParallelStages);
        } catch (io.harness.yaml.validator.InvalidYamlException e) {
            log.info("[PMS_SCHEMA] Schema validation took total time {}ms", System.currentTimeMillis() - start);
            throw e;
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
            YamlSchemaErrorWrapperDTO errorWrapperDTO =
                    YamlSchemaErrorWrapperDTO.builder()
                            .schemaErrors(Collections.singletonList(
                                    YamlSchemaErrorDTO.builder().message(ex.getMessage()).fqn("$.pipeline").build()))
                            .build();
            throw new io.harness.yaml.validator.InvalidYamlException(ex.getMessage(), ex, errorWrapperDTO);
        }
    }
}
