package org.camunda.latera.bss.connectors.hoper.hydra

import static org.camunda.latera.bss.utils.Constants.DOC_STATE_Actual
import static org.camunda.latera.bss.utils.Constants.DOC_STATE_Executed
import static org.camunda.latera.bss.utils.Constants.DOC_STATE_Draft
import static org.camunda.latera.bss.utils.Constants.DOC_STATE_Canceled
import static org.camunda.latera.bss.utils.Constants.DOC_STATE_Closed
import static org.camunda.latera.bss.utils.Constants.DOC_STATE_Dissolved
import static org.camunda.latera.bss.utils.Constants.DOC_STATE_Processing
import static org.camunda.latera.bss.utils.Constants.DOC_STATE_Prepared
import static org.camunda.latera.bss.utils.Constants.SUBJ_ROLE_Provider
import static org.camunda.latera.bss.utils.Constants.SUBJ_ROLE_Recipient

trait Document {
  private static LinkedHashMap DOCUMENT_ENTITY_TYPE = [
    one    : 'document',
    plural : 'documents'
  ]

  Map getDocumentEntityType(def id = null) {
    return DOCUMENT_ENTITY_TYPE + withId(id)
  }

  Integer getDocumentStateActualId() {
    return DOC_STATE_Actual
  }

  Integer getDocumentStateExecutedId() {
    return DOC_STATE_Executed
  }

  Integer getDocumentStateDraftId() {
    return DOC_STATE_Draft
  }

  Integer getDocumentStateCanceledId() {
    return DOC_STATE_Canceled
  }

  Integer getDocumentStateClosedId() {
    return DOC_STATE_Closed
  }

  Integer getDocumentStateDissolvedId() {
    return DOC_STATE_Dissolved
  }

  Integer getDocumentStateProcessingId() {
    return DOC_STATE_Processing
  }

  Integer getDocumentStatePreparedId() {
    return DOC_STATE_Prepared
  }

  Integer getProviderRoleId() {
    return SUBJ_ROLE_Provider
  }

  Integer getRecipientRoleId() {
    return SUBJ_ROLE_Recipient
  }
}