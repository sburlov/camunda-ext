package org.camunda.latera.bss.connectors.hid

import groovy.net.xmlrpc.*
import org.camunda.bpm.engine.delegate.DelegateExecution
import org.camunda.latera.bss.logging.SimpleLogger
import org.camunda.latera.bss.connectors.HID
import org.camunda.latera.bss.connectors.hid.hydra.Ref
import org.camunda.latera.bss.connectors.hid.hydra.Good
import org.camunda.latera.bss.connectors.hid.hydra.Document
import org.camunda.latera.bss.connectors.hid.hydra.Contract
import org.camunda.latera.bss.connectors.hid.hydra.PriceOrder
import org.camunda.latera.bss.connectors.hid.hydra.Invoice
import org.camunda.latera.bss.connectors.hid.hydra.Subject
import org.camunda.latera.bss.connectors.hid.hydra.Company
import org.camunda.latera.bss.connectors.hid.hydra.Person
import org.camunda.latera.bss.connectors.hid.hydra.Reseller
import org.camunda.latera.bss.connectors.hid.hydra.Group
import org.camunda.latera.bss.connectors.hid.hydra.Customer
import org.camunda.latera.bss.connectors.hid.hydra.Account
import org.camunda.latera.bss.connectors.hid.hydra.Subscription
import org.camunda.latera.bss.connectors.hid.hydra.Equipment
import org.camunda.latera.bss.connectors.hid.hydra.Region
import org.camunda.latera.bss.connectors.hid.hydra.Address

class Hydra implements Ref, Good, Document, Contract, PriceOrder, Invoice, Subject, Company, Person, Reseller, Group, Customer, Account, Subscription, Equipment, Region, Address {
  private static Integer DEFAULT_FIRM = 100
  HID hid
  def firmId
  def resellerId
  DelegateExecution execution
  SimpleLogger logger

  Hydra(DelegateExecution execution) {
    this.execution = execution
    this.logger = new SimpleLogger(this.execution)
    this.hid = new HID(execution)

    def user        = execution.getVariable('hydraUser') ?: 'hydra'
    def password    = execution.getVariable('hydraPassword')
    this.firmId     = execution.getVariable('hydraFirmId') ?: (execution.getVariable('homsOrderDataFirmId') ?: getDefaultFirmId())
    this.resellerId = execution.getVariable('hydraResellerId') ?: execution.getVariable('homsOrderDataResellerId')

    this.hid.execute('MAIN.INIT', [
      vch_VC_IP       : '127.0.0.1',
      vch_VC_USER     : user,
      vch_VC_PASS     : password,
      vch_VC_APP_CODE : 'NETSERV_HID',
      vch_VC_CLN_APPID: 'HydraOMS'
    ])

    this.hid.execute('MAIN.SET_ACTIVE_FIRM', [
      num_N_FIRM_ID: getFirmId()
    ])
  }

  LinkedHashMap mergeParams(
    LinkedHashMap initial,
    LinkedHashMap input
  ) {
    LinkedHashMap params = initial + input

    //If it is set opf instead of opfId, get proper reference ids from Hydra
    LinkedHashMap result = [:]
    List keysToExclude = []
    params.each{ name, value ->
      def group = (name =~ /^(.*)Id$/)
      if (group.size() > 0) {
        String noIdName = group[0][1]
        if (params.containsKey(noIdName)) {
          result[name] = getRefIdByCode(params[noIdName])
          keysToExclude.add(name)
          keysToExclude.add(noIdName)
        }
      }
    }
    //And then remove non-id key if id was set above
    params.each{ name, value ->
      if (!keysToExclude.contains(name)) {
        result[name] = value
      }
    }
    return result
  }

  def getDefaultFirmId() {
    return DEFAULT_FIRM
  }

  def getFirmId() {
    return firmId
  }

  def getResellerId() {
    return resellerId
  }

  //Other methods are imported from traits
}