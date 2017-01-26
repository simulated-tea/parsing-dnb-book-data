import util.MariaDbConnector

schema_config = [
   [name: "ISBN",  type: 'INTEGER'],
   [name: "Titel", type: 'VARCHAR'],
]

config_file = './database_config.groovy'
connector = new MariaDbConnector(config_file, schema_config)

assert connector.datasource != null
assert connector.datasource instanceof javax.sql.DataSource
assert connector.sql != null
assert connector.sql instanceof groovy.sql.Sql

executeWithPresentTestTable{
    assert connector.read('test') == []
    assert connector.write('test', [
            [isbn: 17, titel: 'something wicked'],
            [isbn: 18, titel: 'noone will ever know'],
        ]) == 2
    assert connector.read('test') == [
            [isbn: 17, titel: 'something wicked'],
            [isbn: 18, titel: 'noone will ever know'],
        ]
    
    //assert connector.write('test', [[id: "17"]]) == 0 -- howto error?

}

println "Test successful!"






private executeWithPresentTestTable(Closure code) {
    try {
        connector.sql.execute '''
            CREATE TABLE test (
                isbn  INTEGER,
                titel VARCHAR(255)
            )
        '''
    } catch (e) {
        println "Could not execute tests due to error!"
        println e
        System.exit 1
    }
    try {
        code()
    } finally {
        try {
            connector.sql.execute 'DROP TABLE test'
        } catch (e) {
            println "Something went horribly wrong. Check the database for leftover test data!"
            System.exit 1
        }
    }
}
