/*
 * Copyright 2021 Harness Inc. All rights reserved.
 * Use of this source code is governed by the PolyForm Shield 1.0.0 license
 * that can be found in the licenses directory at the root of this repository, also available at
 * https://polyformproject.org/wp-content/uploads/2020/06/PolyForm-Shield-1.0.0.txt.
 */

/*
 * This file is generated by jOOQ.
 */
package io.harness.timescaledb.tables.records;

import io.harness.timescaledb.tables.BillingData;

import java.time.OffsetDateTime;
import org.jooq.impl.TableRecordImpl;

/**
 * This class is generated by jOOQ.
 */
@SuppressWarnings({"all", "unchecked", "rawtypes"})
public class BillingDataRecord extends TableRecordImpl<BillingDataRecord> {
  private static final long serialVersionUID = 1L;

  /**
   * Setter for <code>public.billing_data.starttime</code>.
   */
  public BillingDataRecord setStarttime(OffsetDateTime value) {
    set(0, value);
    return this;
  }

  /**
   * Getter for <code>public.billing_data.starttime</code>.
   */
  public OffsetDateTime getStarttime() {
    return (OffsetDateTime) get(0);
  }

  /**
   * Setter for <code>public.billing_data.endtime</code>.
   */
  public BillingDataRecord setEndtime(OffsetDateTime value) {
    set(1, value);
    return this;
  }

  /**
   * Getter for <code>public.billing_data.endtime</code>.
   */
  public OffsetDateTime getEndtime() {
    return (OffsetDateTime) get(1);
  }

  /**
   * Setter for <code>public.billing_data.accountid</code>.
   */
  public BillingDataRecord setAccountid(String value) {
    set(2, value);
    return this;
  }

  /**
   * Getter for <code>public.billing_data.accountid</code>.
   */
  public String getAccountid() {
    return (String) get(2);
  }

  /**
   * Setter for <code>public.billing_data.settingid</code>.
   */
  public BillingDataRecord setSettingid(String value) {
    set(3, value);
    return this;
  }

  /**
   * Getter for <code>public.billing_data.settingid</code>.
   */
  public String getSettingid() {
    return (String) get(3);
  }

  /**
   * Setter for <code>public.billing_data.instanceid</code>.
   */
  public BillingDataRecord setInstanceid(String value) {
    set(4, value);
    return this;
  }

  /**
   * Getter for <code>public.billing_data.instanceid</code>.
   */
  public String getInstanceid() {
    return (String) get(4);
  }

  /**
   * Setter for <code>public.billing_data.instancetype</code>.
   */
  public BillingDataRecord setInstancetype(String value) {
    set(5, value);
    return this;
  }

  /**
   * Getter for <code>public.billing_data.instancetype</code>.
   */
  public String getInstancetype() {
    return (String) get(5);
  }

  /**
   * Setter for <code>public.billing_data.billingaccountid</code>.
   */
  public BillingDataRecord setBillingaccountid(String value) {
    set(6, value);
    return this;
  }

  /**
   * Getter for <code>public.billing_data.billingaccountid</code>.
   */
  public String getBillingaccountid() {
    return (String) get(6);
  }

  /**
   * Setter for <code>public.billing_data.clusterid</code>.
   */
  public BillingDataRecord setClusterid(String value) {
    set(7, value);
    return this;
  }

  /**
   * Getter for <code>public.billing_data.clusterid</code>.
   */
  public String getClusterid() {
    return (String) get(7);
  }

  /**
   * Setter for <code>public.billing_data.clustername</code>.
   */
  public BillingDataRecord setClustername(String value) {
    set(8, value);
    return this;
  }

  /**
   * Getter for <code>public.billing_data.clustername</code>.
   */
  public String getClustername() {
    return (String) get(8);
  }

  /**
   * Setter for <code>public.billing_data.appid</code>.
   */
  public BillingDataRecord setAppid(String value) {
    set(9, value);
    return this;
  }

  /**
   * Getter for <code>public.billing_data.appid</code>.
   */
  public String getAppid() {
    return (String) get(9);
  }

  /**
   * Setter for <code>public.billing_data.serviceid</code>.
   */
  public BillingDataRecord setServiceid(String value) {
    set(10, value);
    return this;
  }

  /**
   * Getter for <code>public.billing_data.serviceid</code>.
   */
  public String getServiceid() {
    return (String) get(10);
  }

  /**
   * Setter for <code>public.billing_data.envid</code>.
   */
  public BillingDataRecord setEnvid(String value) {
    set(11, value);
    return this;
  }

  /**
   * Getter for <code>public.billing_data.envid</code>.
   */
  public String getEnvid() {
    return (String) get(11);
  }

  /**
   * Setter for <code>public.billing_data.cloudproviderid</code>.
   */
  public BillingDataRecord setCloudproviderid(String value) {
    set(12, value);
    return this;
  }

  /**
   * Getter for <code>public.billing_data.cloudproviderid</code>.
   */
  public String getCloudproviderid() {
    return (String) get(12);
  }

  /**
   * Setter for <code>public.billing_data.parentinstanceid</code>.
   */
  public BillingDataRecord setParentinstanceid(String value) {
    set(13, value);
    return this;
  }

  /**
   * Getter for <code>public.billing_data.parentinstanceid</code>.
   */
  public String getParentinstanceid() {
    return (String) get(13);
  }

  /**
   * Setter for <code>public.billing_data.region</code>.
   */
  public BillingDataRecord setRegion(String value) {
    set(14, value);
    return this;
  }

  /**
   * Getter for <code>public.billing_data.region</code>.
   */
  public String getRegion() {
    return (String) get(14);
  }

  /**
   * Setter for <code>public.billing_data.launchtype</code>.
   */
  public BillingDataRecord setLaunchtype(String value) {
    set(15, value);
    return this;
  }

  /**
   * Getter for <code>public.billing_data.launchtype</code>.
   */
  public String getLaunchtype() {
    return (String) get(15);
  }

  /**
   * Setter for <code>public.billing_data.clustertype</code>.
   */
  public BillingDataRecord setClustertype(String value) {
    set(16, value);
    return this;
  }

  /**
   * Getter for <code>public.billing_data.clustertype</code>.
   */
  public String getClustertype() {
    return (String) get(16);
  }

  /**
   * Setter for <code>public.billing_data.workloadname</code>.
   */
  public BillingDataRecord setWorkloadname(String value) {
    set(17, value);
    return this;
  }

  /**
   * Getter for <code>public.billing_data.workloadname</code>.
   */
  public String getWorkloadname() {
    return (String) get(17);
  }

  /**
   * Setter for <code>public.billing_data.workloadtype</code>.
   */
  public BillingDataRecord setWorkloadtype(String value) {
    set(18, value);
    return this;
  }

  /**
   * Getter for <code>public.billing_data.workloadtype</code>.
   */
  public String getWorkloadtype() {
    return (String) get(18);
  }

  /**
   * Setter for <code>public.billing_data.namespace</code>.
   */
  public BillingDataRecord setNamespace(String value) {
    set(19, value);
    return this;
  }

  /**
   * Getter for <code>public.billing_data.namespace</code>.
   */
  public String getNamespace() {
    return (String) get(19);
  }

  /**
   * Setter for <code>public.billing_data.cloudservicename</code>.
   */
  public BillingDataRecord setCloudservicename(String value) {
    set(20, value);
    return this;
  }

  /**
   * Getter for <code>public.billing_data.cloudservicename</code>.
   */
  public String getCloudservicename() {
    return (String) get(20);
  }

  /**
   * Setter for <code>public.billing_data.taskid</code>.
   */
  public BillingDataRecord setTaskid(String value) {
    set(21, value);
    return this;
  }

  /**
   * Getter for <code>public.billing_data.taskid</code>.
   */
  public String getTaskid() {
    return (String) get(21);
  }

  /**
   * Setter for <code>public.billing_data.cloudprovider</code>.
   */
  public BillingDataRecord setCloudprovider(String value) {
    set(22, value);
    return this;
  }

  /**
   * Getter for <code>public.billing_data.cloudprovider</code>.
   */
  public String getCloudprovider() {
    return (String) get(22);
  }

  /**
   * Setter for <code>public.billing_data.billingamount</code>.
   */
  public BillingDataRecord setBillingamount(Double value) {
    set(23, value);
    return this;
  }

  /**
   * Getter for <code>public.billing_data.billingamount</code>.
   */
  public Double getBillingamount() {
    return (Double) get(23);
  }

  /**
   * Setter for <code>public.billing_data.cpubillingamount</code>.
   */
  public BillingDataRecord setCpubillingamount(Double value) {
    set(24, value);
    return this;
  }

  /**
   * Getter for <code>public.billing_data.cpubillingamount</code>.
   */
  public Double getCpubillingamount() {
    return (Double) get(24);
  }

  /**
   * Setter for <code>public.billing_data.memorybillingamount</code>.
   */
  public BillingDataRecord setMemorybillingamount(Double value) {
    set(25, value);
    return this;
  }

  /**
   * Getter for <code>public.billing_data.memorybillingamount</code>.
   */
  public Double getMemorybillingamount() {
    return (Double) get(25);
  }

  /**
   * Setter for <code>public.billing_data.idlecost</code>.
   */
  public BillingDataRecord setIdlecost(Double value) {
    set(26, value);
    return this;
  }

  /**
   * Getter for <code>public.billing_data.idlecost</code>.
   */
  public Double getIdlecost() {
    return (Double) get(26);
  }

  /**
   * Setter for <code>public.billing_data.cpuidlecost</code>.
   */
  public BillingDataRecord setCpuidlecost(Double value) {
    set(27, value);
    return this;
  }

  /**
   * Getter for <code>public.billing_data.cpuidlecost</code>.
   */
  public Double getCpuidlecost() {
    return (Double) get(27);
  }

  /**
   * Setter for <code>public.billing_data.memoryidlecost</code>.
   */
  public BillingDataRecord setMemoryidlecost(Double value) {
    set(28, value);
    return this;
  }

  /**
   * Getter for <code>public.billing_data.memoryidlecost</code>.
   */
  public Double getMemoryidlecost() {
    return (Double) get(28);
  }

  /**
   * Setter for <code>public.billing_data.usagedurationseconds</code>.
   */
  public BillingDataRecord setUsagedurationseconds(Double value) {
    set(29, value);
    return this;
  }

  /**
   * Getter for <code>public.billing_data.usagedurationseconds</code>.
   */
  public Double getUsagedurationseconds() {
    return (Double) get(29);
  }

  /**
   * Setter for <code>public.billing_data.cpuunitseconds</code>.
   */
  public BillingDataRecord setCpuunitseconds(Double value) {
    set(30, value);
    return this;
  }

  /**
   * Getter for <code>public.billing_data.cpuunitseconds</code>.
   */
  public Double getCpuunitseconds() {
    return (Double) get(30);
  }

  /**
   * Setter for <code>public.billing_data.memorymbseconds</code>.
   */
  public BillingDataRecord setMemorymbseconds(Double value) {
    set(31, value);
    return this;
  }

  /**
   * Getter for <code>public.billing_data.memorymbseconds</code>.
   */
  public Double getMemorymbseconds() {
    return (Double) get(31);
  }

  /**
   * Setter for <code>public.billing_data.maxcpuutilization</code>.
   */
  public BillingDataRecord setMaxcpuutilization(Double value) {
    set(32, value);
    return this;
  }

  /**
   * Getter for <code>public.billing_data.maxcpuutilization</code>.
   */
  public Double getMaxcpuutilization() {
    return (Double) get(32);
  }

  /**
   * Setter for <code>public.billing_data.maxmemoryutilization</code>.
   */
  public BillingDataRecord setMaxmemoryutilization(Double value) {
    set(33, value);
    return this;
  }

  /**
   * Getter for <code>public.billing_data.maxmemoryutilization</code>.
   */
  public Double getMaxmemoryutilization() {
    return (Double) get(33);
  }

  /**
   * Setter for <code>public.billing_data.avgcpuutilization</code>.
   */
  public BillingDataRecord setAvgcpuutilization(Double value) {
    set(34, value);
    return this;
  }

  /**
   * Getter for <code>public.billing_data.avgcpuutilization</code>.
   */
  public Double getAvgcpuutilization() {
    return (Double) get(34);
  }

  /**
   * Setter for <code>public.billing_data.avgmemoryutilization</code>.
   */
  public BillingDataRecord setAvgmemoryutilization(Double value) {
    set(35, value);
    return this;
  }

  /**
   * Getter for <code>public.billing_data.avgmemoryutilization</code>.
   */
  public Double getAvgmemoryutilization() {
    return (Double) get(35);
  }

  /**
   * Setter for <code>public.billing_data.systemcost</code>.
   */
  public BillingDataRecord setSystemcost(Double value) {
    set(36, value);
    return this;
  }

  /**
   * Getter for <code>public.billing_data.systemcost</code>.
   */
  public Double getSystemcost() {
    return (Double) get(36);
  }

  /**
   * Setter for <code>public.billing_data.cpusystemcost</code>.
   */
  public BillingDataRecord setCpusystemcost(Double value) {
    set(37, value);
    return this;
  }

  /**
   * Getter for <code>public.billing_data.cpusystemcost</code>.
   */
  public Double getCpusystemcost() {
    return (Double) get(37);
  }

  /**
   * Setter for <code>public.billing_data.memorysystemcost</code>.
   */
  public BillingDataRecord setMemorysystemcost(Double value) {
    set(38, value);
    return this;
  }

  /**
   * Getter for <code>public.billing_data.memorysystemcost</code>.
   */
  public Double getMemorysystemcost() {
    return (Double) get(38);
  }

  /**
   * Setter for <code>public.billing_data.actualidlecost</code>.
   */
  public BillingDataRecord setActualidlecost(Double value) {
    set(39, value);
    return this;
  }

  /**
   * Getter for <code>public.billing_data.actualidlecost</code>.
   */
  public Double getActualidlecost() {
    return (Double) get(39);
  }

  /**
   * Setter for <code>public.billing_data.cpuactualidlecost</code>.
   */
  public BillingDataRecord setCpuactualidlecost(Double value) {
    set(40, value);
    return this;
  }

  /**
   * Getter for <code>public.billing_data.cpuactualidlecost</code>.
   */
  public Double getCpuactualidlecost() {
    return (Double) get(40);
  }

  /**
   * Setter for <code>public.billing_data.memoryactualidlecost</code>.
   */
  public BillingDataRecord setMemoryactualidlecost(Double value) {
    set(41, value);
    return this;
  }

  /**
   * Getter for <code>public.billing_data.memoryactualidlecost</code>.
   */
  public Double getMemoryactualidlecost() {
    return (Double) get(41);
  }

  /**
   * Setter for <code>public.billing_data.unallocatedcost</code>.
   */
  public BillingDataRecord setUnallocatedcost(Double value) {
    set(42, value);
    return this;
  }

  /**
   * Getter for <code>public.billing_data.unallocatedcost</code>.
   */
  public Double getUnallocatedcost() {
    return (Double) get(42);
  }

  /**
   * Setter for <code>public.billing_data.cpuunallocatedcost</code>.
   */
  public BillingDataRecord setCpuunallocatedcost(Double value) {
    set(43, value);
    return this;
  }

  /**
   * Getter for <code>public.billing_data.cpuunallocatedcost</code>.
   */
  public Double getCpuunallocatedcost() {
    return (Double) get(43);
  }

  /**
   * Setter for <code>public.billing_data.memoryunallocatedcost</code>.
   */
  public BillingDataRecord setMemoryunallocatedcost(Double value) {
    set(44, value);
    return this;
  }

  /**
   * Getter for <code>public.billing_data.memoryunallocatedcost</code>.
   */
  public Double getMemoryunallocatedcost() {
    return (Double) get(44);
  }

  /**
   * Setter for <code>public.billing_data.instancename</code>.
   */
  public BillingDataRecord setInstancename(String value) {
    set(45, value);
    return this;
  }

  /**
   * Getter for <code>public.billing_data.instancename</code>.
   */
  public String getInstancename() {
    return (String) get(45);
  }

  /**
   * Setter for <code>public.billing_data.cpurequest</code>.
   */
  public BillingDataRecord setCpurequest(Double value) {
    set(46, value);
    return this;
  }

  /**
   * Getter for <code>public.billing_data.cpurequest</code>.
   */
  public Double getCpurequest() {
    return (Double) get(46);
  }

  /**
   * Setter for <code>public.billing_data.memoryrequest</code>.
   */
  public BillingDataRecord setMemoryrequest(Double value) {
    set(47, value);
    return this;
  }

  /**
   * Getter for <code>public.billing_data.memoryrequest</code>.
   */
  public Double getMemoryrequest() {
    return (Double) get(47);
  }

  /**
   * Setter for <code>public.billing_data.cpulimit</code>.
   */
  public BillingDataRecord setCpulimit(Double value) {
    set(48, value);
    return this;
  }

  /**
   * Getter for <code>public.billing_data.cpulimit</code>.
   */
  public Double getCpulimit() {
    return (Double) get(48);
  }

  /**
   * Setter for <code>public.billing_data.memorylimit</code>.
   */
  public BillingDataRecord setMemorylimit(Double value) {
    set(49, value);
    return this;
  }

  /**
   * Getter for <code>public.billing_data.memorylimit</code>.
   */
  public Double getMemorylimit() {
    return (Double) get(49);
  }

  /**
   * Setter for <code>public.billing_data.maxcpuutilizationvalue</code>.
   */
  public BillingDataRecord setMaxcpuutilizationvalue(Double value) {
    set(50, value);
    return this;
  }

  /**
   * Getter for <code>public.billing_data.maxcpuutilizationvalue</code>.
   */
  public Double getMaxcpuutilizationvalue() {
    return (Double) get(50);
  }

  /**
   * Setter for <code>public.billing_data.maxmemoryutilizationvalue</code>.
   */
  public BillingDataRecord setMaxmemoryutilizationvalue(Double value) {
    set(51, value);
    return this;
  }

  /**
   * Getter for <code>public.billing_data.maxmemoryutilizationvalue</code>.
   */
  public Double getMaxmemoryutilizationvalue() {
    return (Double) get(51);
  }

  /**
   * Setter for <code>public.billing_data.avgcpuutilizationvalue</code>.
   */
  public BillingDataRecord setAvgcpuutilizationvalue(Double value) {
    set(52, value);
    return this;
  }

  /**
   * Getter for <code>public.billing_data.avgcpuutilizationvalue</code>.
   */
  public Double getAvgcpuutilizationvalue() {
    return (Double) get(52);
  }

  /**
   * Setter for <code>public.billing_data.avgmemoryutilizationvalue</code>.
   */
  public BillingDataRecord setAvgmemoryutilizationvalue(Double value) {
    set(53, value);
    return this;
  }

  /**
   * Getter for <code>public.billing_data.avgmemoryutilizationvalue</code>.
   */
  public Double getAvgmemoryutilizationvalue() {
    return (Double) get(53);
  }

  /**
   * Setter for <code>public.billing_data.networkcost</code>.
   */
  public BillingDataRecord setNetworkcost(Double value) {
    set(54, value);
    return this;
  }

  /**
   * Getter for <code>public.billing_data.networkcost</code>.
   */
  public Double getNetworkcost() {
    return (Double) get(54);
  }

  /**
   * Setter for <code>public.billing_data.pricingsource</code>.
   */
  public BillingDataRecord setPricingsource(String value) {
    set(55, value);
    return this;
  }

  /**
   * Getter for <code>public.billing_data.pricingsource</code>.
   */
  public String getPricingsource() {
    return (String) get(55);
  }

  /**
   * Setter for <code>public.billing_data.storageactualidlecost</code>.
   */
  public BillingDataRecord setStorageactualidlecost(Double value) {
    set(56, value);
    return this;
  }

  /**
   * Getter for <code>public.billing_data.storageactualidlecost</code>.
   */
  public Double getStorageactualidlecost() {
    return (Double) get(56);
  }

  /**
   * Setter for <code>public.billing_data.storageunallocatedcost</code>.
   */
  public BillingDataRecord setStorageunallocatedcost(Double value) {
    set(57, value);
    return this;
  }

  /**
   * Getter for <code>public.billing_data.storageunallocatedcost</code>.
   */
  public Double getStorageunallocatedcost() {
    return (Double) get(57);
  }

  /**
   * Setter for <code>public.billing_data.storageutilizationvalue</code>.
   */
  public BillingDataRecord setStorageutilizationvalue(Double value) {
    set(58, value);
    return this;
  }

  /**
   * Getter for <code>public.billing_data.storageutilizationvalue</code>.
   */
  public Double getStorageutilizationvalue() {
    return (Double) get(58);
  }

  /**
   * Setter for <code>public.billing_data.storagerequest</code>.
   */
  public BillingDataRecord setStoragerequest(Double value) {
    set(59, value);
    return this;
  }

  /**
   * Getter for <code>public.billing_data.storagerequest</code>.
   */
  public Double getStoragerequest() {
    return (Double) get(59);
  }

  /**
   * Setter for <code>public.billing_data.storagembseconds</code>.
   */
  public BillingDataRecord setStoragembseconds(Double value) {
    set(60, value);
    return this;
  }

  /**
   * Getter for <code>public.billing_data.storagembseconds</code>.
   */
  public Double getStoragembseconds() {
    return (Double) get(60);
  }

  /**
   * Setter for <code>public.billing_data.storagecost</code>.
   */
  public BillingDataRecord setStoragecost(Double value) {
    set(61, value);
    return this;
  }

  /**
   * Getter for <code>public.billing_data.storagecost</code>.
   */
  public Double getStoragecost() {
    return (Double) get(61);
  }

  /**
   * Setter for <code>public.billing_data.maxstoragerequest</code>.
   */
  public BillingDataRecord setMaxstoragerequest(Double value) {
    set(62, value);
    return this;
  }

  /**
   * Getter for <code>public.billing_data.maxstoragerequest</code>.
   */
  public Double getMaxstoragerequest() {
    return (Double) get(62);
  }

  /**
   * Setter for <code>public.billing_data.maxstorageutilizationvalue</code>.
   */
  public BillingDataRecord setMaxstorageutilizationvalue(Double value) {
    set(63, value);
    return this;
  }

  /**
   * Getter for <code>public.billing_data.maxstorageutilizationvalue</code>.
   */
  public Double getMaxstorageutilizationvalue() {
    return (Double) get(63);
  }

  // -------------------------------------------------------------------------
  // Constructors
  // -------------------------------------------------------------------------

  /**
   * Create a detached BillingDataRecord
   */
  public BillingDataRecord() {
    super(BillingData.BILLING_DATA);
  }

  /**
   * Create a detached, initialised BillingDataRecord
   */
  public BillingDataRecord(OffsetDateTime starttime, OffsetDateTime endtime, String accountid, String settingid,
      String instanceid, String instancetype, String billingaccountid, String clusterid, String clustername,
      String appid, String serviceid, String envid, String cloudproviderid, String parentinstanceid, String region,
      String launchtype, String clustertype, String workloadname, String workloadtype, String namespace,
      String cloudservicename, String taskid, String cloudprovider, Double billingamount, Double cpubillingamount,
      Double memorybillingamount, Double idlecost, Double cpuidlecost, Double memoryidlecost,
      Double usagedurationseconds, Double cpuunitseconds, Double memorymbseconds, Double maxcpuutilization,
      Double maxmemoryutilization, Double avgcpuutilization, Double avgmemoryutilization, Double systemcost,
      Double cpusystemcost, Double memorysystemcost, Double actualidlecost, Double cpuactualidlecost,
      Double memoryactualidlecost, Double unallocatedcost, Double cpuunallocatedcost, Double memoryunallocatedcost,
      String instancename, Double cpurequest, Double memoryrequest, Double cpulimit, Double memorylimit,
      Double maxcpuutilizationvalue, Double maxmemoryutilizationvalue, Double avgcpuutilizationvalue,
      Double avgmemoryutilizationvalue, Double networkcost, String pricingsource, Double storageactualidlecost,
      Double storageunallocatedcost, Double storageutilizationvalue, Double storagerequest, Double storagembseconds,
      Double storagecost, Double maxstoragerequest, Double maxstorageutilizationvalue) {
    super(BillingData.BILLING_DATA);

    setStarttime(starttime);
    setEndtime(endtime);
    setAccountid(accountid);
    setSettingid(settingid);
    setInstanceid(instanceid);
    setInstancetype(instancetype);
    setBillingaccountid(billingaccountid);
    setClusterid(clusterid);
    setClustername(clustername);
    setAppid(appid);
    setServiceid(serviceid);
    setEnvid(envid);
    setCloudproviderid(cloudproviderid);
    setParentinstanceid(parentinstanceid);
    setRegion(region);
    setLaunchtype(launchtype);
    setClustertype(clustertype);
    setWorkloadname(workloadname);
    setWorkloadtype(workloadtype);
    setNamespace(namespace);
    setCloudservicename(cloudservicename);
    setTaskid(taskid);
    setCloudprovider(cloudprovider);
    setBillingamount(billingamount);
    setCpubillingamount(cpubillingamount);
    setMemorybillingamount(memorybillingamount);
    setIdlecost(idlecost);
    setCpuidlecost(cpuidlecost);
    setMemoryidlecost(memoryidlecost);
    setUsagedurationseconds(usagedurationseconds);
    setCpuunitseconds(cpuunitseconds);
    setMemorymbseconds(memorymbseconds);
    setMaxcpuutilization(maxcpuutilization);
    setMaxmemoryutilization(maxmemoryutilization);
    setAvgcpuutilization(avgcpuutilization);
    setAvgmemoryutilization(avgmemoryutilization);
    setSystemcost(systemcost);
    setCpusystemcost(cpusystemcost);
    setMemorysystemcost(memorysystemcost);
    setActualidlecost(actualidlecost);
    setCpuactualidlecost(cpuactualidlecost);
    setMemoryactualidlecost(memoryactualidlecost);
    setUnallocatedcost(unallocatedcost);
    setCpuunallocatedcost(cpuunallocatedcost);
    setMemoryunallocatedcost(memoryunallocatedcost);
    setInstancename(instancename);
    setCpurequest(cpurequest);
    setMemoryrequest(memoryrequest);
    setCpulimit(cpulimit);
    setMemorylimit(memorylimit);
    setMaxcpuutilizationvalue(maxcpuutilizationvalue);
    setMaxmemoryutilizationvalue(maxmemoryutilizationvalue);
    setAvgcpuutilizationvalue(avgcpuutilizationvalue);
    setAvgmemoryutilizationvalue(avgmemoryutilizationvalue);
    setNetworkcost(networkcost);
    setPricingsource(pricingsource);
    setStorageactualidlecost(storageactualidlecost);
    setStorageunallocatedcost(storageunallocatedcost);
    setStorageutilizationvalue(storageutilizationvalue);
    setStoragerequest(storagerequest);
    setStoragembseconds(storagembseconds);
    setStoragecost(storagecost);
    setMaxstoragerequest(maxstoragerequest);
    setMaxstorageutilizationvalue(maxstorageutilizationvalue);
  }
}
