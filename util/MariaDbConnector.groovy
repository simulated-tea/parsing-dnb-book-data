package util

import groovy.sql.Sql
@GrabConfig(systemClassLoader=true)
@Grab('org.mariadb.jdbc:mariadb-java-client:1.5.7')
import org.mariadb.jdbc.MariaDbDataSource

class MariaDbConnector {
    def _sql
    def db_config
    def datasource
    def schema_config = [
        [table: "buch", columns: [name: "Titel", type: "varchar(255)"], [name: "Inhaltstext_Link", type: "varchar(255)"]],
        [table: "autor", columns: [name: "Name", type: "varchar(255)"]],
        [table: "buch_autor", columns: [name: "ID_Buch", type: "int(10)"], [name: "ID_Autor", type: "int(10)"]],
        [table: "gattung", columns: [name: "Name", type: "varchar(255)"]],
        [table: "buch_gattung", columns: [name: "ID_Buch", type: "int(10)"], [name: "ID_Gattung", type: "int(10)"]],
        [table: "inhalt", columns: [name: "Name", type: "varchar(255)"]],
        [table: "buch_inhalt", columns: [name: "ID_Buch", type: "int(10)"], [name: "ID_Inhalt", type: "int(10)"]],
        [table: "sachgruppe", columns: [name: "Name", type: "varchar(255)"]],
        [table: "buch_sachgruppe", columns: [name: "ID_Buch", type: "int(10)"], [name: "ID_Sachgruppe", type: "int(10)"]],
        [table: "schlagwort", columns: [name: "Name", type: "varchar(255)"]],
        [table: "buch_schlagwort", columns: [name: "ID_Buch", type: "int(10)"], [name: "ID_Schlagwort", type: "bigint(20)"]],
        [table: "medium", columns: [name: "ID_Buch", type: "int(10)"], [name: "ISBN13", type: "varchar(17)"], [name: "Typ", type: "varchar(20)"], [name: "Verlag", type: "varchar(255)"], [name: "Verlag_Ort", type: "varchar(255)"], [name: "Erscheinungsjahr", type: "int(10)"]]
    ]

    MariaDbConnector(config_file) {
        db_config           = (new ConfigSlurper()).parse(new URL('file:' + config_file))
        datasource          = new MariaDbDataSource(db_config.database.url)
        datasource.user     = db_config.database.user
        datasource.password = db_config.database.password
    }

    def setSchemaConfig(config) {
        validateSchemaConfig(config)
        schema_config = config
    }

    def read(table) {
        sql.rows('select * from ' + table)
    }

    def write(table, data) {
        assert schema_config && schema_config.size >= 1, '[Fundamental]: Target schema known and non-empty.' +
            'Please configure schema before writing.'
        schema_config.each

    }

    def typeMap = [
        'int': 'INTEGER',
        'text': 'VARCHAR(255)',
    ]

    def getSql() {
        _sql ?:( _sql = new Sql(datasource) )
    }

    def validateSchemaConfig(config) {
        def config_size = config.size
        def number_of_type_options = config*.type.findAll().size
        if (number_of_type_options != config_size) {
            throw new IncompleteConfigurationException("Config entries found: $config_size" +
                " -- ones found with type config: $number_of_type_options -- which am I missing?")
        }
        def unknown_types = config*.type.findAll{ ! typeMap.keySet().contains(it) }
        if ([] != unknown_types) {
            throw new IncompleteConfigurationException("Unknown configured data types: $unknown_types" +
                " -- I only know of ${typeMap.keySet()} -- which one were you looking for?")
        }
    }
}

class IncompleteConfigurationException extends RuntimeException {
    IncompleteConfigurationException(message) { super(message) }
}
