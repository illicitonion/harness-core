package io.harness.delegate.task.azure.appservice.webapp.ng.request;

import static io.harness.annotations.dev.HarnessTeam.CDP;

import io.harness.annotations.dev.OwnedBy;
import io.harness.delegate.beans.logstreaming.CommandUnitsProgress;
import io.harness.delegate.task.azure.appservice.webapp.ng.AzureWebAppInfraDelegateConfig;
import io.harness.delegate.task.azure.appservice.webapp.ng.AzureWebAppRequestType;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@OwnedBy(CDP)
@EqualsAndHashCode(callSuper = true)
public class AzureWebAppSwapSlotsRequest extends AbstractWebAppTaskRequest {
  private String targetSlot;
  private Integer timeoutIntervalInMin;

  @Builder
  public AzureWebAppSwapSlotsRequest(CommandUnitsProgress commandUnitsProgress,
      AzureWebAppInfraDelegateConfig infrastructure, String targetSlot, Integer timeoutIntervalInMin) {
    super(commandUnitsProgress, infrastructure);
    this.targetSlot = targetSlot;
    this.timeoutIntervalInMin = timeoutIntervalInMin;
  }

  @Override
  public AzureWebAppRequestType getRequestType() {
    return AzureWebAppRequestType.SWAP_SLOTS;
  }
}
