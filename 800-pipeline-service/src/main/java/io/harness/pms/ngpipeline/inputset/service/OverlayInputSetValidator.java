package io.harness.pms.ngpipeline.inputset.service;

import static io.harness.annotations.dev.HarnessTeam.PIPELINE;

import io.harness.annotations.dev.OwnedBy;
import io.harness.data.structure.EmptyPredicate;
import io.harness.exception.InvalidRequestException;
import io.harness.gitsync.helpers.GitContextHelper;
import io.harness.gitsync.interceptor.GitEntityInfo;
import io.harness.gitsync.interceptor.GitSyncBranchContext;
import io.harness.pms.gitsync.PmsGitSyncBranchContextGuard;
import io.harness.pms.inputset.OverlayInputSetErrorWrapperDTOPMS;
import io.harness.pms.merger.helpers.InputSetYamlHelper;
import io.harness.pms.ngpipeline.inputset.beans.entity.InputSetEntity;
import io.harness.pms.ngpipeline.inputset.exceptions.InvalidOverlayInputSetException;
import io.harness.pms.ngpipeline.inputset.helpers.InputSetErrorsHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import lombok.experimental.UtilityClass;

@OwnedBy(PIPELINE)
@UtilityClass
public class OverlayInputSetValidator {
  public void validateOverlayInputSet(PMSInputSetService inputSetService, String accountId, String orgIdentifier,
      String projectIdentifier, String pipelineIdentifier, String yaml) {
    String identifier = InputSetYamlHelper.getStringField(yaml, "identifier", "overlayInputSet");
    if (EmptyPredicate.isEmpty(identifier)) {
      throw new InvalidRequestException("Identifier cannot be empty");
    }
    if (identifier.length() > 63) {
      throw new InvalidRequestException("Overlay Input Set identifier length cannot be more that 63 characters.");
    }
    List<String> inputSetReferences = InputSetYamlHelper.getReferencesFromOverlayInputSetYaml(yaml);
    if (inputSetReferences.isEmpty()) {
      throw new InvalidRequestException("Input Set References can't be empty");
    }

    InputSetYamlHelper.confirmPipelineIdentifierInOverlayInputSet(yaml, pipelineIdentifier);
    InputSetYamlHelper.confirmOrgAndProjectIdentifier(yaml, "overlayInputSet", orgIdentifier, projectIdentifier);

    List<Optional<InputSetEntity>> inputSets;
    if (GitContextHelper.isUpdateToNewBranch()) {
      String baseBranch = Objects.requireNonNull(GitContextHelper.getGitEntityInfo()).getBaseBranch();
      String repoIdentifier = GitContextHelper.getGitEntityInfo().getYamlGitConfigId();
      GitSyncBranchContext branchContext =
          GitSyncBranchContext.builder()
              .gitBranchInfo(GitEntityInfo.builder().branch(baseBranch).yamlGitConfigId(repoIdentifier).build())
              .build();
      try (PmsGitSyncBranchContextGuard ignored = new PmsGitSyncBranchContextGuard(branchContext, true)) {
        inputSets = findAllReferredInputSets(
            inputSetService, inputSetReferences, accountId, orgIdentifier, projectIdentifier, pipelineIdentifier);
      }
    } else {
      inputSets = findAllReferredInputSets(
          inputSetService, inputSetReferences, accountId, orgIdentifier, projectIdentifier, pipelineIdentifier);
    }
    Map<String, String> invalidReferences =
        InputSetErrorsHelper.getInvalidInputSetReferences(inputSets, inputSetReferences);
    if (!invalidReferences.isEmpty()) {
      OverlayInputSetErrorWrapperDTOPMS overlayInputSetErrorWrapperDTOPMS =
          OverlayInputSetErrorWrapperDTOPMS.builder().invalidReferences(invalidReferences).build();
      throw new InvalidOverlayInputSetException(
          "Exception in creating the Overlay Input Set", overlayInputSetErrorWrapperDTOPMS);
    }
  }

  private List<Optional<InputSetEntity>> findAllReferredInputSets(PMSInputSetService inputSetService,
      List<String> referencesInOverlay, String accountId, String orgIdentifier, String projectIdentifier,
      String pipelineIdentifier) {
    List<Optional<InputSetEntity>> inputSets = new ArrayList<>();
    referencesInOverlay.forEach(identifier -> {
      if (EmptyPredicate.isEmpty(identifier)) {
        throw new InvalidRequestException("Empty Input Set Identifier not allowed in Input Set References");
      }
      inputSets.add(
          inputSetService.get(accountId, orgIdentifier, projectIdentifier, pipelineIdentifier, identifier, false));
    });
    return inputSets;
  }
}
