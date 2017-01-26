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

    MariaDbConnector(config_file, schema_config) {
        db_config           = (new ConfigSlurper()).parse(new URL('file:' + config_file))
        datasource          = new MariaDbDataSource(db_config.database.url)
        datasource.user     = db_config.database.user
        datasource.password = db_config.database.password
        this.schema_config  = schema_config
    }

    def read(table) {
        []
    }

    def write(table, data) {
        assert schema_config, '[Fundamental]: Target schema known'
    }

    def getSql() {
        _sql ?:( _sql = new Sql(datasource) )
    }
}
