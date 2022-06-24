package io.harness.ngsettings.settings;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.google.common.collect.Sets;
import com.google.common.io.Resources;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import io.harness.annotations.dev.HarnessTeam;
import io.harness.annotations.dev.OwnedBy;
import io.harness.exception.InvalidRequestException;
import io.harness.ngsettings.api.SettingsService;
import io.harness.ngsettings.entities.Setting;
import io.harness.ngsettings.entities.SettingConfiguration;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

@OwnedBy(HarnessTeam.PL)
@Slf4j
@Singleton
public class SettingsCreationJob {
    private final SettingsConfig settingsConfig;
    private final SettingsService settingsService;
    private static final String SETTINGS_YAML_PATH = "io/harness/ngsettings/settings/settings.yml";

    @Inject
    public SettingsCreationJob(SettingsService settingsService) {
        ObjectMapper om = new ObjectMapper(new YAMLFactory());
        URL url = getClass().getClassLoader().getResource(SETTINGS_YAML_PATH);
            try {
            byte[] bytes = Resources.toByteArray(url);
            this.settingsConfig = om.readValue(bytes, SettingsConfig.class);
        } catch (
        IOException e) {
            throw new InvalidRequestException("Permissions file path is invalid or the syntax is incorrect", e);
        }
        this.settingsService = settingsService;
    }

    public void run() {
        log.info("Updating settings in the database");
        Set<SettingConfiguration>  latestSettings = settingsConfig.getSettings();
        Set<SettingConfiguration>  currentSettings = new HashSet<>(settingsService.listDefaultSettings());
        Set<SettingConfiguration> upsertSettings = Sets.difference(latestSettings, currentSettings);
    }
}
