package org.camunda.latera.bss.connectors.hid.hydra

import org.camunda.latera.bss.utils.DateTimeUtil
import org.camunda.latera.bss.utils.Oracle

trait Account {
  private static String ACCOUNTS_TABLE       = 'SI_V_SUBJ_ACCOUNTS'
  private static String DEFAULT_ACCOUNT_TYPE = 'ACC_TYPE_Personal'

  def getAccountsTable() {
    return ACCOUNTS_TABLE
  }

  def getDefaultAccountTypeId() {
    return getRefIdByCode(DEFAULT_ACCOUNT_TYPE)
  }

  LinkedHashMap getAccount(
    def accountId,
    def accountTypeId = getDefaultAccountTypeId()
  ) {
    LinkedHashMap where = [
      n_account_id: accountId
    ]
    if (accountTypeId) {
      where.n_account_type_id = accountTypeId
    }
    return hid.getTableFirst(getAccountsTable(), where: where)
  }

  LinkedHashMap getAccountBalance(
    def accountId,
    def operationDate = DateTimeUtil.now()
  ) {
    return hid.queryFirst("""
    SELECT
        'n_account_id',       N_ACCOUNT_ID,
        'n_sum_bal',          N_SUM_BAL,
        'n_sum_total',        N_SUM_TOTAL,
        'n_sum_reserved_cur', N_SUM_RESERVED_CUR,
        'n_sum_reserved',     N_SUM_RESERVED,
        'n_sum_overdraft',    N_SUM_OVERDRAFT,
        'n_sum_free',         N_SUM_FREE,
        'd_bal',              D_BAL,
        'd_overdraft_end',    D_OVERDRAFT_END
    FROM
      TABLE(SI_ACCOUNTS_PKG.GET_ACCOUNT_BALANCE_P(
        num_N_ACCOUNT_ID    => ${accountId},
        dt_D_OPER           => ${Oracle.encodeDateStr(operationDate)}))
  """, true)
  }

  LinkedHashMap getAccountBalanceTotal(
    def accountId,
    def operationDate = DateTimeUtil.now()
  ) {
    return getAccountBalance(accountId, operationDate)?.n_sum_total
  }

  LinkedHashMap getAccountFree(
    def accountId,
    def operationDate = DateTimeUtil.now()
  ) {
    return getAccountBalance(accountId, operationDate)?.n_sum_free
  }

  List getSubjectAccounts(
    def subjectId,
    def accountTypeId = getDefaultAccountTypeId()
  ) {
    LinkedHashMap where = [
      n_subject_id: subjectId
    ]
    if (accountTypeId) {
      where.n_account_type_id = accountTypeId
    }
    return hid.getTableData(getAccountsTable(), where: where)
  }

  List getCompanyAccounts(
    def companyId,
    def accountTypeId = getDefaultAccountTypeId()
  ) {
    return getSubjectAccounts(companyId, accountTypeId)
  }

  List getPersonAccounts(
    def personId,
    def accountTypeId = getDefaultAccountTypeId()
  ) {
    return getSubjectAccounts(personId, accountTypeId)
  }

  List getCustomerAccounts(
    def customerId,
    def accountTypeId = getDefaultAccountTypeId()
  ) {
    return getSubjectAccounts(customerId, accountTypeId)
  }

  LinkedHashMap getSubjectAccount(
    def subjectId,
    def accountTypeId = getDefaultAccountTypeId()
  ) {
    return getSubjectAccounts(subjectId, accountTypeId)?.getAt(0)
  }

  LinkedHashMap getCompanyAccount(
    def companyId,
    def accountTypeId = getDefaultAccountTypeId()
  ) {
    return getSubjectAccount(companyId, accountTypeId)
  }

  LinkedHashMap getPersonAccount(
    def personId,
    def accountTypeId = getDefaultAccountTypeId()
  ) {
    return getSubjectAccount(personId, accountTypeId)
  }

  LinkedHashMap getCustomerAccount(
    def customerId,
    def accountTypeId = getDefaultAccountTypeId()
  ) {
    return getSubjectAccount(customerId, accountTypeId)
  }

  def putCustomerAccount(LinkedHashMap input) {
    def params = mergeParams([
      accountId            : null,
      customerId           : null,
      currencyId           : getDefaultCurrencyId(),
      name                 : null,
      code                 : null,
      number               : null,
      permanentOverdraft   : null,
      temporalOverdraft    : null,
      temporalOverdraftEnd : null,
      maxOverdraft         : null,
      rem                  : null
    ], input)
    try {
      logger.info("Putting account with params ${params}")

      LinkedHashMap account = hid.execute('SI_ACCOUNTS_PKG.CUSTOMER_ACCOUNT_PUT', [
        num_N_ACCOUNT_ID          : params.accountId,
        num_N_CUSTOMER_ID         : params.customerId,
        num_N_CURRENCY_ID         : params.currencyId,
        vch_VC_NAME               : params.name,
        vch_VC_CODE               : params.code,
        vch_VC_ACCOUNT            : params.number,
        num_N_PERMANENT_OVERDRAFT : params.permanentOverdraft,
        num_N_TEMPORAL_OVERDRAFT  : params.temporalOverdraft,
        dt_D_TEMP_OVERDRAFT_END   : params.temporalOverdraftEnd,
        num_N_MAX_OVERDRAFT       : params.maxOverdraft,
        vch_VC_REM                : params.rem
      ])
      logger.info("   Account ${account.num_N_ACCOUNT_ID} was put successfully!")
      return account
    } catch (Exception e){
      logger.error("   Error while putting account!")
      logger.error_oracle(e)
    }
  }

  Boolean putAdjustment(LinkedHashMap input) {
    LinkedHashMap params = mergeParams([
      accountId     : null,
      docId         : null,
      goodId        : null,
      equipmentId   : null,
      sum           : null,
      sumWoTax      : null,
      operationDate : null,
      firmId        : getFirmId()
    ], input)
    try {
      logger.info("Putting adjustment with params ${params}")
      hid.execute('SD_BALANCE_ADJUSTMENTS_PKG.CHARGE_ADJUSTMENT', [
        num_N_ACCOUNT_ID  : params.accountId,
        num_N_CONTRACT_ID : params.docId,
        num_N_OBJECT_ID   : params.equipmentId,
        num_N_SERVICE_ID  : params.goodId,
        num_N_SUM         : params.sum,
        num_N_SUM_WO_TAX  : params.sumWoTax,
        dt_D_OPER         : params.operationDate,
        num_N_FIRM_ID     : params.firmId
      ])
      logger.info("   Adjustment was put successfully!")
      return true
    } catch (Exception e){
      logger.error("   Error while putting adjustment!")
      logger.error_oracle(e)
      return false
    }
  }

  Boolean processAccount(
    def accountId,
    def beginDate = DateTimeUtil.now(),
    def endDate   = null
  ) {
    try {
      logger.info("Processing account id ${accountId}")
      hid.execute('SD_CHARGE_LOGS_CHARGING_PKG.PROCESS_ACCOUNT', [
        num_N_ACCOUNT_ID : accountId,
        dt_D_OPER        : beginDate,
        dt_D_OPER_END    : endDate
      ])
      logger.info("   Account processed successfully!")
      return true
    } catch (Exception e){
      logger.error("   Error while processing account!")
      logger.error_oracle(e)
      return false
    }
  }

  Boolean processAccount(LinkedHashMap input) {
    LinkedHashMap params = mergeParams([
      accountId : null,
      beginDate : DateTimeUtil.now(),
      endDate   : null
    ], input)
    return processAccount(params.accountId, params.beginDate, params.endDate)
  }
}