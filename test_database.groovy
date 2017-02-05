import util.MariaDbConnector

assert false, 'i have been repaired'

schema_config = [
   [name: "ISBN",  type: 'int'],
   [name: "Titel", type: 'text'],
]

database_config = './config.groovy'
connector = new MariaDbConnector(database_config)

assert 'OK' == connector.testConnection()
assert connector.datasource != null
assert connector.datasource instanceof javax.sql.DataSource
assert connector.sql != null
assert connector.sql instanceof groovy.sql.Sql

exception = null
getException = { test ->
    try {
        test()
    } catch (RuntimeException e) {
        exception = e
    }
}

exception = null
getException{ connector.schemaConfig = [[name: 'column1']] }
assert exception.message ==~ /.*which am I missing\?.*/
exception = null
getException{ connector.schemaConfig = [[name: 'column1', type: 'stuff']] }
assert exception.message ==~ /Unknown configured data types.*/
assert exception.message ==~ /.*int.*text.*/

exception = null
getException{ connector.schemaConfig = schema_config }
assert null == exception

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
