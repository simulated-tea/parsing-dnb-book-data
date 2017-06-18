package util

import groovy.json.JsonOutput as jo

class MariaDbConnector {
    final String MISSING_DATA_ERROR = 'Mandatory source data missing in input'

    def db_config
    def schema_config
    def log_debug = false

    def tableCacher

    MariaDbConnector(config) {
        config        = config instanceof Map ? config : (new ConfigSlurper()).parse(new URL('file:' + config))
        schema_config = config.database.schema_config
        tableCacher   = new DuplicateSafeTableCacher(config)
    }

    def testConnection() { tableCacher.testConnection() }

    def importBookData(data) {
        def importStatistics = [items: 0, successfully_imported: 0, failures: 0]
        data.each{ bookData ->
            importStatistics.items += 1
            try {
                tableCacher.withTransaction{
                    importSingleBook(bookData)
                }
                importStatistics.successfully_imported += 1
            } catch (AssertionError e) {
                importStatistics.failures += 1
                if (e.message ==~ /.*$MISSING_DATA_ERROR.*/) {
                    println "WARNING -- Faulty data entry encountered:\n $bookData\nNot written."
                    // TODO: clean out cache!! :((
                } else {
                    throw e
                }
            } catch (Exception e) {
                println "ERROR   -- Unexpected error on data:"
                println jo.prettyPrint(jo.toJson(bookData))
                throw e
            }
        }
        println "Book import results:"
        println importStatistics.collect{ k, v -> "  $k  --> $v" }.join("\n")
        importStatistics
    }

    def importSingleBook(bookData) {
        if (log_debug) { println "Processing entry $bookData" }
        def tableIds = [:]
        schema_config.each{ table ->
            if (log_debug) { println "Collecting data for $table" }
            def columnNames = []
            def columnData = []
            table.columns.each{ columnSpec ->
                def name = columnSpec.name
                def datum
                if (name.toLowerCase().startsWith('id_')) {
                    def referencedTable = name - 'id_' - 'ID_'
                    datum = tableIds[referencedTable.toLowerCase()]
                } else {
                    datum = grabPayloadData(columnSpec, bookData)
                }
                columnNames << name
                columnData << datum
            }

            def rowData = columnsToRows columnNames, columnData
            rowData.each{ row ->
                def resultEntry = tableCacher.insert table.table, columnNames, row
                rememberEntryIdForForeignKeys table.table, resultEntry, tableIds
                if (log_debug) { println "Confirming inserted entry: $resultEntry" }
            }

            if (log_debug) { println "Table done. Entry cache state: $tableIds" }
        }
    }

    def grabPayloadData(columnSpec, data) { // /!\ Bad design choices ahead
        assert null != columnSpec.type
        assert null != columnSpec.source
        def sourceItems = []
        if (columnSpec.source instanceof List) {
            sourceItems += columnSpec.source.collect{ data[it] }.flatten()
        } else {
            sourceItems += data[columnSpec.source]
        }
        def sourceData = sourceItems.findAll()
        if (columnSpec.optional && [] == sourceData) { return null }
        assert [] != sourceData, MISSING_DATA_ERROR

        def castData
        switch(columnSpec.type.toLowerCase()) {
            case ~/^int.*/:     castData = sourceData.collect{ it as Integer }
            case ~/^bigint.*/:  castData = sourceData.collect{ it as BigInteger }
            case ~/^varchar.*/: castData = sourceData.collect{ it as String }
        }
        1 == castData.size() ? castData[0] : castData
    }

    def columnsToRows(columnNames, columnData) {
        if (columnData.every{ datum -> !( datum instanceof List ) }) {
            return [columnData]
        }
        if ( !onlyIdColumnsAreNotLists(columnNames, columnData)
            || !allListsAreSameSize(columnData)) {
            throw new UnsupportedOperationException("Malconfigured table data: $columnNames $columnData")
        }
        def rowsNeeded = columnData.find{ it instanceof List }.size()
        if (0 == rowsNeeded) { return [] }
        (0..rowsNeeded-1).collect{ i ->
            def collected = []
            columnNames.eachWithIndex{ name, j ->
                collected << (name.toLowerCase().startsWith('id_') ? columnData[j] : columnData[j][i])
            }
            collected
        }
    }

    def onlyIdColumnsAreNotLists(columnNames, columnData) {
        def i = -1
        columnNames.every{ name ->
                i++
                name.toLowerCase().startsWith('id_') || columnData[i] instanceof List
        }
    }

    def allListsAreSameSize(columnData) {
        1 == columnData.findAll{ it instanceof List }.collect{ it.size() }.unique().size()
    }

    def rememberEntryIdForForeignKeys(tableName, resultEntry, tableIds) {
        def entryId
        if (resultEntry.containsKey('id')) {
            entryId = resultEntry.id
        }
        if (resultEntry.containsKey('ID')) {
            entryId = resultEntry.ID
        }
        if (entryId) {
            tableIds << [(tableName.toLowerCase()): entryId]
        }
    }

    def setLog_debug(value) {
        this.log_debug = value
        tableCacher.log_debug = value
    }
}
