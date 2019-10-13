package org.camunda.latera.bss.utils

import groovy.json.JsonSlurper
import groovy.json.JsonOutput
import static org.camunda.latera.bss.utils.DateTimeUtil.isDate
import static org.camunda.latera.bss.utils.DateTimeUtil.iso
import static org.camunda.latera.bss.utils.ListUtil.isList
import static org.camunda.latera.bss.utils.MapUtil.isMap

class JSON {
  static def escape(obj) {
    if (isMap(obj)) {
      LinkedHashMap newMap = [:]
      obj.each { def k, def v ->
        newMap[k] = escape(v)
      }
      return newMap
    } else if (isList(obj)) {
      List newList = []
      obj.each { def item ->
        newList << escape(item)
      }
      return newList
    } else if (isDate(obj)) {
      return iso(obj)
    } else if (obj instanceof CSV) {
      return obj.dataMap
    } else {
      return obj
    }
  }

  static String to(Object obj) {
    return JsonOutput.toJson(escape(obj))
  }

  static String pretty(CharSequence json) {
    return JsonOutput.prettyPrint(json)
  }

  static String pretty(Object obj) {
    return pretty(to(obj))
  }

  static Object from(CharSequence json) {
    return new JsonSlurper().parseText(json)
  }
}