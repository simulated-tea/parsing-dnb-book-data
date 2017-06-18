package util

import groovy.sql.Sql
@GrabConfig(systemClassLoader=true)
@Grab('org.mariadb.jdbc:mariadb-java-client:1.5.7')
import org.mariadb.jdbc.MariaDbDataSource

// yeah, yeah, I know it's an anti-pattern. This is just some dirty ad-hoc scripting, ok?

class DuplicateSafeTableCacher {
    
    def _sql
    def datasource
    def log_debug = false

    def schema
    def tableCache = [:]

    DuplicateSafeTableCacher(config) {
        datasource          = new MariaDbDataSource(config.database.url)
        datasource.user     = config.database.user
        datasource.password = config.database.password
        schema = config.database.schema_config
        clear()
    }

    def clear() {
        // Performance optimization: convert to hashmap by hash over column values
        // (horrible, horrible idea, doing this via lists)
        schema*.table.each{ tableCache[it] = [] }
    }

    def testConnection() {
        def result = 0;
        try {
            result = sql.rows('select 1 + 2 as result from dual')[0].result
        } catch (e) {}
        3 == result ? 'OK' : 'Database connection could not be verified'
    }

    def withTransaction(closure) {
        sql.withTransaction{
            closure()
        }
        // Don't we need cache-update (deletions) on rollbacks?
    }

    def readFromDb(table, whereColumns = [], whereValues = []) {
        def whereClause = ''
        if (whereColumns) {
            whereClause = 'where '
            whereClause += (0..whereColumns.size()-1).collect{ i ->
                 whereColumns[i] + (null == whereValues[i] ? " is NULL" : " = ?")
            }.join(' and ')
        }
        sql.rows("select * from $table $whereClause", whereValues)
    }

    def findInCache(table, columns, data) {
        tableCache[table].find{ entry ->
            (/* all given data is the same as in entry */
                (0..columns.size()-1).every{ i -> entry[columns[i]] == data[i] }
            ) ? entry : null
        }
    }

    def findInDb(table, columns, data) {
        def foundEntries = readFromDb(table, columns, data)
        if ([] == foundEntries) { return null }
        assert 1 == foundEntries.size()
        def foundEntry = foundEntries[0]
        tableCache[table] << foundEntry
        return foundEntry
    }

    def insert(table, columns, data) {
        def dbEntry = findInCache(table, columns, data)
        if (null == dbEntry) {
            dbEntry = findInDb(table, columns, data)
        }
        if (null == dbEntry) {
            insertToDb table, columns, data
            dbEntry = findInDb(table, columns, data) 
            assert dbEntry, 'entry finally in DB'
        }
        return dbEntry
    }

    def insertToDb(table, columns, data) {
        def n = columns.size()
        assert n == data.size()
        def query = "insert into $table (${columns.join(', ')}) values (${(['?']*n).join(', ')})"
        if (log_debug) { println "Executing insert query: $query\nParameters: $data" }
        sql.execute query, data
    }

    def getSql() {
        _sql ?:( _sql = new Sql(datasource) )
    }
}
