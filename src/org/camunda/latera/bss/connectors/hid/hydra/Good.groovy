package org.camunda.latera.bss.connectors.hid.hydra

trait Good {
  private static String GOODS_TABLE = 'SR_V_GOODS'

  def getGoodsTable() {
    return GOODS_TABLE
  }

  LinkedHashMap getGood(def goodId) {
    LinkedHashMap where = [
      n_good_id: goodId
    ]
    return hid.getTableData(getGoodsTable(), where: where)
  }

  LinkedHashMap getGoodByCode(String code) {
    LinkedHashMap where = [
      vc_code: code
    ]
    return hid.getTableData(getGoodsTable(), where: where)
  }

  LinkedHashMap getGoodByName(String name) {
    LinkedHashMap where = [
      vc_name: name
    ]
    return hid.getTableData(getGoodsTable(), where: where)
  }

  LinkedHashMap getGoodUnitId(def goodId) {
    LinkedHashMap where = [
      n_good_id: goodId
    ]
    return getGood(goodId).n_unit_id
  }

  def getGoodValueTypeIdByCode(String code) {
    return hid.queryFirst("""
      SELECT SR_GOODS_PKG_S.GET_GOOD_VALUE_TYPE_ID('${code}') FROM DUAL
    """)?.getAt(0)
  }
}