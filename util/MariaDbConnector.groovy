package util

class MariaDbConnector {
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
        data.each{ bookData ->
            try {
                tableCacher.withTransaction{
                    importSingleBook(bookData)
                }
            } catch (AssertionError e) {
                if (e.message ==~ /(?s).*null != data\[source.*/) {
                    println "WARNING -- Faulty data entry encountered:\n $bookData\nNot written."
                } else {
                    throw e
                }
            }
        }
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
                    datum = tableIds[referencedTable]
                } else {
                    datum = grabPayloadData(columnSpec, bookData)
                }
                columnNames << name
                columnData << datum
            }

            def rowData = columnsToRows columnNames, columnData
            rowData.each{ row ->
                ensurePersistenceOfSingleEntry table.table, columnNames, row, tableIds
            }

            if (log_debug) { println "Table done. Entry cache state: $tableIds" }
        }
    }

    def grabPayloadData(columnSpec, data) {
        columnSpec.with{
            assert null != type
            assert null != source
            assert null != data[source]
            switch(type.toLowerCase()) {
                case ~/^int.*/:     return scalarOrListCast(data[source], Integer)
                case ~/^bigint.*/:  return scalarOrListCast(data[source], BigInteger)
                case ~/^varchar.*/: return scalarOrListCast(data[source], String)
            }
        }
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

    def ensurePersistenceOfSingleEntry(tableName, columnNames, columnData, tableIds) {
        def resultEntry = tableCacher.insert tableName, columnNames, columnData
        rememberEntryIdForForeignKeys tableName, resultEntry, tableIds
        if (log_debug) { println "Confirming inserted entry: $resultEntry" }
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
            tableIds << [(tableName): entryId]
        }
    }

    def read(table, whereColumns, whereValues) {
    }

    def scalarOrListCast(item, clazz) {
        (item instanceof List) ? item.collect{ it.asType(clazz) } : item.asType(clazz)
    }

    def setLog_debug(value) {
        this.log_debug = value
        tableCacher.log_debug = value
    }
}
