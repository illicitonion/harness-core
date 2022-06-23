/*
 * Copyright 2021 Harness Inc. All rights reserved.
 * Use of this source code is governed by the PolyForm Shield 1.0.0 license
 * that can be found in the licenses directory at the root of this repository, also available at
 * https://polyformproject.org/wp-content/uploads/2020/06/PolyForm-Shield-1.0.0.txt.
 */

package io.harness.delegate.task.ci;

import static io.harness.data.structure.EmptyPredicate.isNotEmpty;
import static io.harness.delegate.beans.connector.scm.gitlab.GitlabApiAccessType.OAUTH;
import static io.harness.delegate.beans.connector.scm.gitlab.GitlabApiAccessType.TOKEN;

import static java.lang.String.format;

import io.harness.cistatus.service.GithubAppConfig;
import io.harness.cistatus.service.GithubService;
import io.harness.cistatus.service.azurerepo.AzureRepoConfig;
import io.harness.cistatus.service.azurerepo.AzureRepoContext;
import io.harness.cistatus.service.azurerepo.AzureRepoService;
import io.harness.cistatus.service.bitbucket.BitbucketConfig;
import io.harness.cistatus.service.bitbucket.BitbucketService;
import io.harness.cistatus.service.gitlab.GitlabConfig;
import io.harness.cistatus.service.gitlab.GitlabService;
import io.harness.cistatus.service.gitlab.GitlabServiceImpl;
import io.harness.data.structure.EmptyPredicate;
import io.harness.delegate.beans.DelegateResponseData;
import io.harness.delegate.beans.DelegateTaskPackage;
import io.harness.delegate.beans.DelegateTaskResponse;
import io.harness.delegate.beans.ci.pod.ConnectorDetails;
import io.harness.delegate.beans.ci.status.BuildStatusPushResponse;
import io.harness.delegate.beans.ci.status.BuildStatusPushResponse.Status;
import io.harness.delegate.beans.connector.scm.GitAuthType;
import io.harness.delegate.beans.connector.scm.GitConnectionType;
import io.harness.delegate.beans.connector.scm.azurerepo.AzureRepoApiAccessType;
import io.harness.delegate.beans.connector.scm.azurerepo.AzureRepoConnectorDTO;
import io.harness.delegate.beans.connector.scm.azurerepo.AzureRepoTokenSpecDTO;
import io.harness.delegate.beans.connector.scm.bitbucket.BitbucketApiAccessType;
import io.harness.delegate.beans.connector.scm.bitbucket.BitbucketConnectorDTO;
import io.harness.delegate.beans.connector.scm.bitbucket.BitbucketUsernameTokenApiAccessDTO;
import io.harness.delegate.beans.connector.scm.github.GithubApiAccessDTO;
import io.harness.delegate.beans.connector.scm.github.GithubApiAccessType;
import io.harness.delegate.beans.connector.scm.github.GithubAppSpecDTO;
import io.harness.delegate.beans.connector.scm.github.GithubConnectorDTO;
import io.harness.delegate.beans.connector.scm.github.GithubTokenSpecDTO;
import io.harness.delegate.beans.connector.scm.gitlab.GitlabConnectorDTO;
import io.harness.delegate.beans.connector.scm.gitlab.GitlabOauthDTO;
import io.harness.delegate.beans.connector.scm.gitlab.GitlabTokenSpecDTO;
import io.harness.delegate.beans.logstreaming.ILogStreamingTaskClient;
import io.harness.delegate.task.AbstractDelegateRunnableTask;
import io.harness.delegate.task.TaskParameters;
import io.harness.delegate.task.ci.CIBuildPushParameters.CIBuildPushTaskType;
import io.harness.exception.ngexception.CIStageExecutionException;
import io.harness.git.GitClientHelper;
import io.harness.security.encryption.SecretDecryptionService;

import com.google.inject.Inject;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.NotImplementedException;
import org.apache.commons.lang3.StringUtils;

@Slf4j
public class CIBuildStatusPushTask extends AbstractDelegateRunnableTask {
  public static final String TARGET_URL = "target_url";
  @Inject private GithubService githubService;
  @Inject private BitbucketService bitbucketService;
  @Inject private GitlabService gitlabService;
  @Inject private AzureRepoService azureRepoService;
  @Inject private SecretDecryptionService secretDecryptionService;

  private static final String DESC = "description";
  private static final String STATE = "state";
  private static final String URL = "url";
  private static final String CONTEXT = "context";
  private static final String DETAILS_URL = "details_url";

  private static final String BITBUCKET_KEY = "key";
  private static final String GITHUB_API_URL = "https://api.github.com/";
  private static final String BITBUCKET_API_URL = "https://api.bitbucket.org/";
  private static final String GITLAB_API_URL = "https://gitlab.com/api/";
  private static final String AZURE_REPO_API_URL = "https://dev.azure.com/";
  private static final String AZURE_REPO_GENRE = "HarnessCI";
  private static final String APP_URL = "https://app.harness.io";
  private static final String PATH_SEPARATOR = "/";

  public CIBuildStatusPushTask(DelegateTaskPackage delegateTaskPackage, ILogStreamingTaskClient logStreamingTaskClient,
      Consumer<DelegateTaskResponse> consumer, BooleanSupplier preExecute) {
    super(delegateTaskPackage, logStreamingTaskClient, consumer, preExecute);
  }

  @Override
  public DelegateResponseData run(TaskParameters parameters) {
    if (((CIBuildPushParameters) parameters).commandType == CIBuildPushTaskType.STATUS) {
      String sha = "";
      try {
        CIBuildStatusPushParameters ciBuildStatusPushParameters = (CIBuildStatusPushParameters) parameters;
        sha = ciBuildStatusPushParameters.getSha();
        boolean statusSent = false;
        if (ciBuildStatusPushParameters.getGitSCMType() == GitSCMType.GITHUB) {
          statusSent = sendBuildStatusToGitHub(ciBuildStatusPushParameters);
        } else if (ciBuildStatusPushParameters.getGitSCMType() == GitSCMType.BITBUCKET) {
          statusSent = sendBuildStatusToBitbucket(ciBuildStatusPushParameters);
        } else if (ciBuildStatusPushParameters.getGitSCMType() == GitSCMType.GITLAB) {
          statusSent = sendBuildStatusToGitLab(ciBuildStatusPushParameters);
        } else if (ciBuildStatusPushParameters.getGitSCMType() == GitSCMType.AZURE_REPO) {
          statusSent = sendBuildStatusToAzureRepo(ciBuildStatusPushParameters);
        } else {
          throw new UnsupportedOperationException("Not supported");
        }

        if (statusSent) {
          log.info("Successfully sent the git status for sha {}, stage identifier {}",
              ciBuildStatusPushParameters.getSha(), ciBuildStatusPushParameters.getIdentifier());
          return BuildStatusPushResponse.builder().status(Status.SUCCESS).build();
        } else {
          return BuildStatusPushResponse.builder().status(Status.ERROR).build();
        }
      } catch (Exception ex) {
        log.error(String.format("failed to send status for sha %s", sha), ex);
        return BuildStatusPushResponse.builder().status(Status.ERROR).build();
      }
    }
    return BuildStatusPushResponse.builder().status(Status.ERROR).build();
  }

  private GithubAppSpecDTO retrieveGithubAppSpecDTO(
      GithubConnectorDTO gitConfigDTO, ConnectorDetails connectorDetails) {
    GithubApiAccessDTO githubApiAccessDTO = gitConfigDTO.getApiAccess();
    if (githubApiAccessDTO.getType() == GithubApiAccessType.GITHUB_APP) {
      GithubAppSpecDTO githubAppSpecDTO = (GithubAppSpecDTO) githubApiAccessDTO.getSpec();
      secretDecryptionService.decrypt(githubAppSpecDTO, connectorDetails.getEncryptedDataDetails());
      return githubAppSpecDTO;
    } else {
      throw new CIStageExecutionException(
          format("Unsupported access type %s for github status", githubApiAccessDTO.getType()));
    }
  }

  private boolean sendBuildStatusToGitHub(CIBuildStatusPushParameters ciBuildStatusPushParameters) {
    GithubConnectorDTO gitConfigDTO =
        (GithubConnectorDTO) ciBuildStatusPushParameters.getConnectorDetails().getConnectorConfig();

    GithubApiAccessDTO githubApiAccessDTO = gitConfigDTO.getApiAccess();

    if (githubApiAccessDTO == null) {
      log.warn("Not sending status because api access is not enabled for sha {}", ciBuildStatusPushParameters.getSha());
      return false;
    }

    String token = null;
    if (githubApiAccessDTO.getType() == GithubApiAccessType.GITHUB_APP) {
      GithubAppSpecDTO githubAppSpecDTO =
          retrieveGithubAppSpecDTO(gitConfigDTO, ciBuildStatusPushParameters.getConnectorDetails());

      GithubAppConfig githubAppConfig =
          GithubAppConfig.builder()
              .installationId(githubAppSpecDTO.getInstallationId())
              .appId(githubAppSpecDTO.getApplicationId())
              .privateKey(new String(githubAppSpecDTO.getPrivateKeyRef().getDecryptedValue()))
              .githubUrl(getGitApiURL(gitConfigDTO.getUrl()))
              .build();
      token = githubService.getToken(githubAppConfig);
      if (EmptyPredicate.isEmpty(token)) {
        log.error("Not sending status because token is empty for appId {}, installationId {}, sha {}",
            githubAppSpecDTO.getApplicationId(), githubAppSpecDTO.getInstallationId(),
            ciBuildStatusPushParameters.getSha());
        return false;
      }
    } else if (githubApiAccessDTO.getType() == GithubApiAccessType.TOKEN) {
      GithubTokenSpecDTO githubTokenSpecDTO = (GithubTokenSpecDTO) githubApiAccessDTO.getSpec();
      secretDecryptionService.decrypt(
          githubTokenSpecDTO, ciBuildStatusPushParameters.getConnectorDetails().getEncryptedDataDetails());
      token = new String(githubTokenSpecDTO.getTokenRef().getDecryptedValue());

      if (EmptyPredicate.isEmpty(token)) {
        log.error("Not sending status because token is empty for sha {}", ciBuildStatusPushParameters.getSha());
        return false;
      }
    } else {
      throw new CIStageExecutionException(
          format("Unsupported access type %s for github status", githubApiAccessDTO.getType()));
    }

    if (isNotEmpty(token)) {
      Map<String, Object> bodyObjectMap = new HashMap<>();
      bodyObjectMap.put(DESC, ciBuildStatusPushParameters.getDesc());
      bodyObjectMap.put(CONTEXT, ciBuildStatusPushParameters.getIdentifier());
      bodyObjectMap.put(STATE, ciBuildStatusPushParameters.getState());
      bodyObjectMap.put(TARGET_URL, ciBuildStatusPushParameters.getDetailsUrl());
      // TODO Sending Just URL will require refactoring in sendStatus method, Will be done POST CI GA
      GithubAppConfig githubAppConfig =
          GithubAppConfig.builder().githubUrl(getGitApiURL(gitConfigDTO.getUrl())).build();

      return githubService.sendStatus(githubAppConfig, token, ciBuildStatusPushParameters.getSha(),
          ciBuildStatusPushParameters.getOwner(), ciBuildStatusPushParameters.getRepo(), bodyObjectMap);
    } else {
      log.error("Not sending status because token is empty for sha {}", ciBuildStatusPushParameters.getSha());
      return false;
    }
  }

  private String getGitApiURL(String url) {
    if (GitClientHelper.isGithubSAAS(url)) {
      return GITHUB_API_URL;
    } else {
      String domain = GitClientHelper.getGitSCM(url);
      return "https://" + domain + "/api/v3/";
    }
  }
  private String getBitBucketApiURL(String url) {
    if (url.contains("bitbucket.org/")) {
      return BITBUCKET_API_URL;
    } else {
      String domain = GitClientHelper.getGitSCM(url);
      return "https://" + domain + "/";
    }
  }

  private String getGitlabApiURL(String url) {
    if (url.contains("gitlab.com")) {
      return GITLAB_API_URL;
    } else {
      String domain = GitClientHelper.getGitSCM(url);
      return "https://" + domain + "/api/";
    }
  }

  private String getAzureRepoApiURL(String url) {
    if (url.contains("azure.com")) {
      return AZURE_REPO_API_URL;
    } else {
      String domain = GitClientHelper.getGitSCM(url);
      return "https://" + domain + PATH_SEPARATOR;
    }
  }

  private boolean sendBuildStatusToBitbucket(CIBuildStatusPushParameters ciBuildStatusPushParameters) {
    Map<String, Object> bodyObjectMap = new HashMap<>();
    bodyObjectMap.put(DESC, ciBuildStatusPushParameters.getDesc());
    bodyObjectMap.put(BITBUCKET_KEY, ciBuildStatusPushParameters.getIdentifier());
    bodyObjectMap.put(STATE, ciBuildStatusPushParameters.getState());
    bodyObjectMap.put(URL, ciBuildStatusPushParameters.getDetailsUrl());

    String token = retrieveAuthToken(
        ciBuildStatusPushParameters.getGitSCMType(), ciBuildStatusPushParameters.getConnectorDetails());

    BitbucketConnectorDTO gitConfigDTO =
        (BitbucketConnectorDTO) ciBuildStatusPushParameters.getConnectorDetails().getConnectorConfig();

    if (isNotEmpty(token)) {
      return bitbucketService.sendStatus(
          BitbucketConfig.builder().bitbucketUrl(getBitBucketApiURL(gitConfigDTO.getUrl())).build(),
          ciBuildStatusPushParameters.getUserName(), token, null, ciBuildStatusPushParameters.getSha(),
          ciBuildStatusPushParameters.getOwner(), ciBuildStatusPushParameters.getRepo(), bodyObjectMap);
    } else {
      log.error("Not sending status because token is empty sha {}", ciBuildStatusPushParameters.getSha());
      return false;
    }
  }

  private boolean sendBuildStatusToGitLab(CIBuildStatusPushParameters ciBuildStatusPushParameters) {
    Map<String, Object> bodyObjectMap = new HashMap<>();
    bodyObjectMap.put(GitlabServiceImpl.DESC, ciBuildStatusPushParameters.getDesc());
    bodyObjectMap.put(GitlabServiceImpl.CONTEXT, ciBuildStatusPushParameters.getIdentifier());
    bodyObjectMap.put(GitlabServiceImpl.STATE, ciBuildStatusPushParameters.getState());
    bodyObjectMap.put(GitlabServiceImpl.TARGET_URL, ciBuildStatusPushParameters.getDetailsUrl());

    String token = retrieveAuthToken(
        ciBuildStatusPushParameters.getGitSCMType(), ciBuildStatusPushParameters.getConnectorDetails());

    GitlabConnectorDTO gitConfigDTO =
        (GitlabConnectorDTO) ciBuildStatusPushParameters.getConnectorDetails().getConnectorConfig();

    if (isNotEmpty(token)) {
      return gitlabService.sendStatus(GitlabConfig.builder().gitlabUrl(getGitlabApiURL(gitConfigDTO.getUrl())).build(),
          ciBuildStatusPushParameters.getUserName(), token, null, ciBuildStatusPushParameters.getSha(),
          ciBuildStatusPushParameters.getOwner(), ciBuildStatusPushParameters.getRepo(), bodyObjectMap);
    } else {
      log.error("Not sending status because token is empty sha {}", ciBuildStatusPushParameters.getSha());
      return false;
    }
  }

  private boolean sendBuildStatusToAzureRepo(CIBuildStatusPushParameters ciBuildStatusPushParameters) {
    Map<String, Object> bodyObjectMap = new HashMap<>();
    bodyObjectMap.put(GitlabServiceImpl.DESC, ciBuildStatusPushParameters.getDesc());
    bodyObjectMap.put(GitlabServiceImpl.CONTEXT,
        AzureRepoContext.builder().genre(AZURE_REPO_GENRE).name(ciBuildStatusPushParameters.getIdentifier()).build());
    bodyObjectMap.put(GitlabServiceImpl.STATE, ciBuildStatusPushParameters.getState());
    bodyObjectMap.put(GitlabServiceImpl.TARGET_URL, ciBuildStatusPushParameters.getDetailsUrl());

    String token = retrieveAuthToken(
        ciBuildStatusPushParameters.getGitSCMType(), ciBuildStatusPushParameters.getConnectorDetails());

    AzureRepoConnectorDTO gitConfigDTO =
        (AzureRepoConnectorDTO) ciBuildStatusPushParameters.getConnectorDetails().getConnectorConfig();

    if (isNotEmpty(token)) {
      String completeUrl = gitConfigDTO.getUrl();

      if (gitConfigDTO.getConnectionType() == GitConnectionType.ACCOUNT) {
        completeUrl = StringUtils.join(
            StringUtils.stripEnd(completeUrl, PATH_SEPARATOR), PATH_SEPARATOR, ciBuildStatusPushParameters.getRepo());
      }

      String orgAndProject;

      if (gitConfigDTO.getAuthentication().getAuthType() == GitAuthType.HTTP) {
        orgAndProject = GitClientHelper.getAzureRepoOrgAndProjectHTTP(completeUrl);
      } else {
        orgAndProject = GitClientHelper.getAzureRepoOrgAndProjectSSH(completeUrl);
      }

      String project = GitClientHelper.getAzureRepoProject(orgAndProject);
      String repo = StringUtils.substringAfterLast(completeUrl, PATH_SEPARATOR);

      return azureRepoService.sendStatus(
          AzureRepoConfig.builder().azureRepoUrl(getAzureRepoApiURL(gitConfigDTO.getUrl())).build(),
          ciBuildStatusPushParameters.getUserName(), token, ciBuildStatusPushParameters.getSha(),
          ciBuildStatusPushParameters.getOwner(), project, repo, bodyObjectMap);
    } else {
      log.error("Not sending status because token is empty sha {}", ciBuildStatusPushParameters.getSha());
      return false;
    }
  }

  private String retrieveAuthToken(GitSCMType gitSCMType, ConnectorDetails gitConnector) {
    switch (gitSCMType) {
      case GITHUB:
        return ""; // It does not require token because auth occurs via github app
      case GITLAB:
        GitlabConnectorDTO gitConfigDTO = (GitlabConnectorDTO) gitConnector.getConnectorConfig();
        if (gitConfigDTO.getApiAccess() == null) {
          throw new CIStageExecutionException(
              format("Failed to retrieve token info for gitlab connector: ", gitConnector.getIdentifier()));
        }
        if (gitConfigDTO.getApiAccess().getType() == TOKEN) {
          GitlabTokenSpecDTO gitlabTokenSpecDTO = (GitlabTokenSpecDTO) gitConfigDTO.getApiAccess().getSpec();
          secretDecryptionService.decrypt(gitlabTokenSpecDTO, gitConnector.getEncryptedDataDetails());
          return new String(gitlabTokenSpecDTO.getTokenRef().getDecryptedValue());
        } else if (gitConfigDTO.getApiAccess().getType() == OAUTH) {
          GitlabOauthDTO gitlabOauthDTO = (GitlabOauthDTO) gitConfigDTO.getApiAccess().getSpec();
          secretDecryptionService.decrypt(gitlabOauthDTO, gitConnector.getEncryptedDataDetails());
          return new String(gitlabOauthDTO.getTokenRef().getDecryptedValue());
        } else {
          throw new CIStageExecutionException(
              format("Unsupported access type %s for gitlab status", gitConfigDTO.getApiAccess().getType()));
        }
      case BITBUCKET:
        BitbucketConnectorDTO bitbucketConnectorDTO = (BitbucketConnectorDTO) gitConnector.getConnectorConfig();
        if (bitbucketConnectorDTO.getApiAccess() == null) {
          throw new CIStageExecutionException(
              format("Failed to retrieve token info for Bitbucket connector: %s", gitConnector.getIdentifier()));
        }
        if (bitbucketConnectorDTO.getApiAccess().getType() == BitbucketApiAccessType.USERNAME_AND_TOKEN) {
          BitbucketUsernameTokenApiAccessDTO bitbucketTokenSpecDTO =
              (BitbucketUsernameTokenApiAccessDTO) bitbucketConnectorDTO.getApiAccess().getSpec();
          secretDecryptionService.decrypt(bitbucketTokenSpecDTO, gitConnector.getEncryptedDataDetails());
          return new String(bitbucketTokenSpecDTO.getTokenRef().getDecryptedValue());
        } else {
          throw new CIStageExecutionException(
              format("Unsupported access type %s for gitlab status", bitbucketConnectorDTO.getApiAccess().getType()));
        }
      case AZURE_REPO:
        AzureRepoConnectorDTO azureRepoConnectorDTO = (AzureRepoConnectorDTO) gitConnector.getConnectorConfig();
        if (azureRepoConnectorDTO.getApiAccess() == null) {
          throw new CIStageExecutionException(
              format("Failed to retrieve token info for Azure repo connector: %s", gitConnector.getIdentifier()));
        }
        if (azureRepoConnectorDTO.getApiAccess().getType() == AzureRepoApiAccessType.TOKEN) {
          AzureRepoTokenSpecDTO azureRepoTokenSpecDTO =
              (AzureRepoTokenSpecDTO) azureRepoConnectorDTO.getApiAccess().getSpec();
          secretDecryptionService.decrypt(azureRepoTokenSpecDTO, gitConnector.getEncryptedDataDetails());
          return new String(azureRepoTokenSpecDTO.getTokenRef().getDecryptedValue());
        } else {
          throw new CIStageExecutionException(format(
              "Unsupported access type %s for Azure repo status", azureRepoConnectorDTO.getApiAccess().getType()));
        }
      default:
        throw new CIStageExecutionException(format("Unsupported scm type %s for git status", gitSCMType));
    }
  }

  @Override
  public DelegateResponseData run(Object[] parameters) {
    throw new NotImplementedException("not implemented");
  }
}
