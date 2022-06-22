package io.harness.telemetry;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import io.harness.annotations.dev.OwnedBy;
import io.harness.core.ci.services.CIOverviewDashboardService;
import io.harness.data.structure.EmptyPredicate;
import io.harness.iterator.PersistenceIteratorFactory;
import io.harness.logging.AccountLogContext;
import io.harness.logging.AutoLogContext;
import io.harness.mongo.iterator.MongoPersistenceIterator;
import io.harness.mongo.iterator.filter.MorphiaFilterExpander;
import io.harness.mongo.iterator.provider.MorphiaPersistenceProvider;
import io.harness.workers.background.AccountLevelEntityProcessController;
import lombok.extern.slf4j.Slf4j;
import software.wings.beans.Account;
import software.wings.beans.Account.AccountKeys;
import software.wings.service.intfc.AccountService;

import java.util.Collections;
import java.util.HashMap;

import static io.harness.annotations.dev.HarnessTeam.CI;
import static io.harness.logging.AutoLogContext.OverrideBehavior.OVERRIDE_ERROR;
import static io.harness.mongo.iterator.MongoPersistenceIterator.SchedulingType.REGULAR;
import static io.harness.telemetry.Destination.ALL;
import static java.time.Duration.ofHours;
import static java.time.Duration.ofMinutes;

@Slf4j
@Singleton
@OwnedBy(CI)
public class CiTelemetryPublisher implements MongoPersistenceIterator.Handler<Account> {
    private static final String GLOBAL_ACCOUNT_ID = "__GLOBAL_ACCOUNT_ID__";
    private static final String ACCOUNT = "Account";
    private static final String COUNT_ACTIVE_DEVELOPERS = "ci_license_developers_used";

    private final PersistenceIteratorFactory persistenceIteratorFactory;
    private final MorphiaPersistenceProvider<Account> persistenceProvider;
    private final CIOverviewDashboardService ciOverviewDashboardService;
    private final TelemetryReporter telemetryReporter;
    private final AccountService accountService;

    @Inject
    public CiTelemetryPublisher(PersistenceIteratorFactory persistenceIteratorFactory,
                                      MorphiaPersistenceProvider<Account> persistenceProvider, CIOverviewDashboardService ciOverviewDashboardService,
                                      TelemetryReporter telemetryReporter, AccountService accountService) {
        this.persistenceIteratorFactory = persistenceIteratorFactory;
        this.persistenceProvider = persistenceProvider;
        this.ciOverviewDashboardService = ciOverviewDashboardService;
        this.telemetryReporter = telemetryReporter;
        this.accountService = accountService;
    }

    public void registerIterators() {
        persistenceIteratorFactory.createPumpIteratorWithDedicatedThreadPool(
                PersistenceIteratorFactory.PumpExecutorOptions.builder()
                        .name("CiTelemetryPublisherIteration")
                        .poolSize(1)
                        .interval(ofMinutes(10))
                        .build(),
                CiTelemetryPublisher.class,
                MongoPersistenceIterator.<Account, MorphiaFilterExpander<Account>>builder()
                        .clazz(Account.class)
                        .fieldName(AccountKeys.ciTelemetryPublisherIteration)
                        .targetInterval(ofHours(24))
                        .acceptableNoAlertDelay(ofMinutes(4))
                        .acceptableExecutionTime(ofMinutes(2))
                        .handler(this)
                        .entityProcessController(new AccountLevelEntityProcessController(accountService))
                        .schedulingType(REGULAR)
                        .persistenceProvider(persistenceProvider)
                        .redistribute(true));
    }

    @Override
    public void handle(Account account) {
        try (AutoLogContext ignore0 = new AccountLogContext(account.getUuid(), OVERRIDE_ERROR)) {
            String accountId = account.getUuid();
            log.info("CiTelemetryPublisher recordTelemetry execute started for account {}.", accountId);
            try {
                if (EmptyPredicate.isNotEmpty(accountId) && !accountId.equals(GLOBAL_ACCOUNT_ID)) {
                    sendTelemetryGroupEvents(accountId);
                    log.info("Scheduled CiTelemetryPublisher event sent for account {} !", accountId);
                } else {
                    log.info("There is no Account found!. Can not send scheduled CiTelemetryPublisher event.");
                }
            } catch (Exception e) {
                log.error("CiTelemetryPublisher recordTelemetry execute failed for account {} .", accountId, e);
            } finally {
                log.info("CiTelemetryPublisher recordTelemetry execute finished for account {} .", accountId);
            }
        }
    }

    private void sendTelemetryGroupEvents(String accountId) {
        HashMap<String, Object> map = new HashMap<>();
        map.put("group_type", ACCOUNT);
        map.put("group_id", accountId);
        map.put(COUNT_ACTIVE_DEVELOPERS, ciOverviewDashboardService.getActiveCommitterCount(accountId));
        telemetryReporter.sendGroupEvent(accountId, null, map, Collections.singletonMap(ALL, true),
                TelemetryOption.builder().sendForCommunity(true).build());
    }
}
