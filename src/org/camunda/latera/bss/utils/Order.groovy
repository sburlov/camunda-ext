package org.camunda.latera.bss.utils

import groovy.lang.GroovyObject
import org.camunda.bpm.engine.delegate.DelegateExecution
import static org.camunda.latera.bss.utils.StringUtil.*
import static org.camunda.latera.bss.utils.DateTimeUtil.*
import org.camunda.latera.bss.utils.JSON
import org.camunda.latera.bss.connectors.Minio

class Order implements GroovyObject {
  private DelegateExecution _execution

  Order(DelegateExecution execution) {
    this._execution  = execution
  }

  static def getValue(CharSequence name, DelegateExecution execution) {
    def result = null
    if (name == 'uploadedFile' || name == 'homsOrderDataUploadedFile') {
      return null
    }
    if (name.startsWith('homsOrderData')) {
      result = execution.getVariable(name)
    } else {
      result = execution.getVariable("homsOrderData${capitalize(name)}")
    }

    def date = parseDateTimeAny(result)
    if (date) {
      return date
    }
    return result
  }

  static def getValue(DelegateExecution execution, CharSequence name) {
    return getValue(name, execution)
  }

  def getValue(CharSequence name) {
    if (name == 'data') {
      return this.getClass().getData(this._execution)
    }
    return this.getClass().getValue(name, this._execution)
  }

  def getProperty(String propertyName) { // do not change type to CharSequence, dynamic access will not work
    return getValue(propertyName)
  }

  def getAt(CharSequence name) {
    return getValue(name)
  }

  def getVariable(CharSequence name) {
    return getValue(name)
  }

  static Map getData(DelegateExecution execution) {
    LinkedHashMap data = [:]
    execution.getVariables().each { key, value ->
      if (key =~ /^homsOrderData/ && key != 'homsOrderDataUploadedFile') {
        String _key = decapitalize(key.replaceFirst(/^homsOrderData/, ''))
        data[_key] = getValue(key, execution)
      }
    }
    return data
  }

  Map getData() {
    return getData(this._execution)
  }

  def asType(Class target) {
    switch (target) {
      case Map : return getData()
      default  : return this
    }
  }

  static void setValue(CharSequence name, def value, DelegateExecution execution) {
    if (name == 'uploadedFile' || name == 'homsOrderDataUploadedFile') {
      return
    }
    if (isString(value)) {
      value = value.toString() // Convert GStringImpl to String
    }
    if (isDate(value)) {
      value = iso(value)
    }
    if (name.startsWith('homsOrderData')) {
      execution.setVariable(name, value)
    } else {
      execution.setVariable("homsOrderData${capitalize(name)}", value)
    }
  }

  static void setValue(DelegateExecution execution, CharSequence name, def value) {
    setValue(name, value, execution)
  }

  void setProperty(String propertyName, def newValue) { // do not change type to CharSequence, dynamic access will not work
    setValue(propertyName, newValue)
  }

  void putAt(CharSequence name, def value) {
    setValue(name, value)
  }

  Order plus(Map data) {
    setValue(data.name, data.value)
    return this
  }

  void setVariable(CharSequence name, def value) {
    setValue(name, value)
  }

  void setValue(CharSequence name, def value) {
    this.getClass().setValue(name, value, this._execution)
  }

  static void saveData(Map data, DelegateExecution execution) {
    data.each { key, value ->
      setValue(key, value, execution)
    }
  }

  static void saveData(DelegateExecution execution, Map data) {
    saveData(execution, data)
  }

  void saveData(Map data) {
    saveData(data, this._execution)
  }

  static void removeValue(CharSequence name, DelegateExecution execution) {
    if (name.startsWith('homsOrderData')) {
      execution.removeValue(name)
    } else {
      execution.removeValue("homsOrderData${capitalize(name)}")
    }
  }

  static void removeValue(DelegateExecution execution, CharSequence name) {
    removeValue(name, execution)
  }

  Order minus(CharSequence name) {
    removeValue(name)
    return this
  }

  void removeValue(CharSequence name) {
    this.getClass().removeValue(name, this._execution)
  }

  static List getFiles(CharSequence prefix = '', DelegateExecution execution) {
    return JSON.from(execution.getVariable("homsOrderData${prefix}FileList") ?: '[]')
  }

  List getFiles(CharSequence prefix = '') {
    return this.class.getFiles(prefix, this._execution)
  }

  static Map getFile(CharSequence name, CharSequence prefix = '', DelegateExecution execution) {
    List files = getFiles(prefix, execution)
    files.each { file ->
      if (file.origin_name == name || file.real_name == name) {
        return file
      }
    }
    return null
  }

  Map getFile(CharSequence name, CharSequence prefix = '') {
    return this.class.getFile(name, prefix, this._execution)
  }

  static List getFilesContent(CharSequence prefix = '', DelegateExecution execution) {
    List result = []
    List files  = getFiles(prefix, execution)
    if (files) {
      Minio minio  = new Minio(execution)
      files.each { file ->
        result += file + [
          name    : file.origin_name,
          content : minio.getFileContent(file.bucket, file.real_name)
        ]
      }
    }
    return result
  }

  List getFilesContent(CharSequence prefix = '') {
    return getFilesContent(prefix, this._execution)
  }

  static Map getFileContent(CharSequence name, CharSequence prefix = '', DelegateExecution execution) {
    LinkedHashMap file = getFile(name, prefix, execution)
    if (file) {
      Minio minio  = new Minio(execution)
      return file + [
        name    : file.origin_name,
        content : minio.getFileContent(file.bucket, file.real_name)
      ]
    } else {
      return null
    }
  }

  Map getFileContent(CharSequence name, CharSequence prefix = '') {
    return getFileContent(name, prefix, this._execution)
  }
}