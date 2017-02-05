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

    MariaDbConnector(config) {
        config              = config instanceof Map ? config : (new ConfigSlurper()).parse(new URL('file:' + config))
        datasource          = new MariaDbDataSource(config.database.url)
        datasource.user     = config.database.user
        datasource.password = config.database.password
        schema_config       = config.database.schema_config
    }
    
    def testConnection() {
        def result = 0;
        try {
            result = sql.rows('select 1 + 2 as result from dual')[0].result
        } catch (e) {}
        3 == result ? 'OK' : 'Database connection could not be verified'
    }
    
    def read(table) {
        sql.rows('select * from ' + table)
    }

    def insertData(data) {
        //schema_config.each

    }

    def getSql() {
        _sql ?:( _sql = new Sql(datasource) )
    }
}
