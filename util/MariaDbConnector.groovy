package util

import groovy.sql.Sql
@GrabConfig(systemClassLoader=true)
@Grab('org.mariadb.jdbc:mariadb-java-client:1.5.7')
import org.mariadb.jdbc.MariaDbDataSource

class MariaDbConnector {
    def _sql
    def db_config
    def datasource
    def schema_config
    def log_debug = false

    def recentlyInsertedCache = [:]

    MariaDbConnector(config) {
        config              = config instanceof Map ? config : (new ConfigSlurper()).parse(new URL('file:' + config))
        datasource          = new MariaDbDataSource(config.database.url)
        datasource.user     = config.database.user
        datasource.password = config.database.password
        schema_config       = config.database.schema_config
        schema_config*.table.each{ recentlyInsertedCache[it] = [] }
    }

    def testConnection() {
        def result = 0;
        try {
            result = sql.rows('select 1 + 2 as result from dual')[0].result
        } catch (e) {}
        3 == result ? 'OK' : 'Database connection could not be verified'
    }

    def read(table, whereColumns = [], whereValues = []) {
        def whereClause = ''
        if (whereColumns) {
            whereClause = 'where ' + whereColumns.collect{ "$it = ?" }.join(' and ')
        }
        sql.rows("select * from $table $whereClause", whereValues)
    }

    def importBookData(data) {
        data.each{ bookData ->
            if (log_debug) { println "Processing entry $bookData" }
            def tableIds = [:]
            schema_config.each{ table ->
                if (log_debug) { println "Collecting data for $table" }
                // TODO: [test pending] Check if overlapping entry is already present
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
                    persistSingleEntry table.table, columnNames, row, tableIds
                }

                if (log_debug) { println "Table done. Entry cache state: $tableIds" }
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

    def persistSingleEntry(tableName, columnNames, columnData, tableIds) {
        insertData tableName, columnNames, columnData
        def resultingDbEntries = read(tableName, columnNames, columnData)
        assert 1 == resultingDbEntries.size()
        def resultEntry = resultingDbEntries[0]
        if (log_debug) { println "Found inserted entry: $resultEntry" }
        rememberEntryIdForFutureReference tableName, resultEntry, tableIds
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

    def scalarOrListCast(item, clazz) {
        (item instanceof List) ? item.collect{ it.asType(clazz) } : item.asType(clazz)
    }

    def insertData(table, columns, data) {
        def n = columns.size()
        assert n == data.size()
        def query = "insert into $table (${columns.join(', ')}) values (${(['?']*n).join(', ')})"
        if (log_debug) { println "Executing insert query: $query\nParameters: $data" }
        sql.execute query, data
    }

    def rememberEntryIdForFutureReference(tableName, resultEntry, tableIds) {
        def entryId
        if (resultEntry.containsKey('id')) {
            entryId = resultEntry.id
        }
        if (resultEntry.containsKey('ID')) {
            entryId = resultEntry.ID
        }
        if (entryId) {
            tableIds << [(tableName): entryId]
            recentlyInsertedCache[tableName] << resultEntry // FUTURE: limit max cache size
        }
    }

    def getSql() {
        _sql ?:( _sql = new Sql(datasource) )
    }
}
