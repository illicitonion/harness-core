/*
 * Copyright 2022 Harness Inc. All rights reserved.
 * Use of this source code is governed by the PolyForm Free Trial 1.0.0 license
 * that can be found in the licenses directory at the root of this repository, also available at
 * https://polyformproject.org/wp-content/uploads/2020/05/PolyForm-Free-Trial-1.0.0.txt.
 */

package io.harness.template.mappers;

import io.harness.EntityType;

import java.util.HashMap;
import java.util.Map;

public class TemplateChildEntityTypeToEntityTypeMapper {

    //TODO: modify EntityTpe yamlName itself ideally. THis is temporary fix
    private Map<String, EntityType> map;

    private static TemplateChildEntityTypeToEntityTypeMapper instance;

    private TemplateChildEntityTypeToEntityTypeMapper(){
        map = new HashMap<>();
        map.put("Deployment", EntityType.DEPLOYMENT_STAGE);
    }

    public static TemplateChildEntityTypeToEntityTypeMapper getInstance(){
        if(instance == null){
            synchronized (TemplateChildEntityTypeToEntityTypeMapper.class) {
                if(instance == null){
                    instance = new TemplateChildEntityTypeToEntityTypeMapper();
                }
            }
        }
        return instance;
    }

    public EntityType getEntityType(String templateChildType){
        if(map.containsKey(templateChildType)){
            return map.get(templateChildType);
        }else{
            EntityType entityType = getEntityTypeFromTemplateChildType(templateChildType);
            map.put(templateChildType, entityType);
            return entityType;
        }
    }

    private static EntityType getEntityTypeFromTemplateChildType(String templateChildType) {
        if(isValidEnum(EntityType.class, templateChildType)){
            return EntityType.valueOf(templateChildType);
        }else{
            return null;
        }

    }

    private static boolean isValidEnum(Class enumClass, String enumValue){
        if (enumValue == null) {
            return false;
        }
        try {
            Enum.valueOf(enumClass, enumValue);
            return true;
        } catch (IllegalArgumentException ex) {
            return false;
        }
    }
}
