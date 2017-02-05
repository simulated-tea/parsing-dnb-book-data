import util.MariaDbConnector

exception = null
getException = { test -> try { test() } catch (RuntimeException e) { exception = e } }
schema_config = [
    [
        table: 'book',
        columns: [
            [name: "titel", type: "varchar(255)", source: "titel"],
            [name: "isbn10", type: "int(10)", source: "isbn"],
        ],
    ], [
        table: 'author',
        columns: [
            [name: "name", type: "varchar(255)", source: "author"],
        ],
    ], [
        table: 'book_author',
        columns: [
            [name: "id_book", type: "int(10)"],
            [name: "id_author", type: "int(10)"],
        ],
    ], [
        table: 'tag',
        columns: [
            [name: "id_book", type: "int(10)"],
            [name: "name", type: "varchar(255)", source: "tags"],
        ],
    ]
]

config = (new ConfigSlurper()).parse(new URL('file:config.groovy'))
config.database.url = config.database.url.replace('/books', '/test')
config.database.schema_config = schema_config
connector = new MariaDbConnector(config)
assert 'OK' == connector.testConnection()

executeWithPresentTestTables{
    assert connector.read('book') == []
    assert connector.read('author') == []
    assert connector.read('book_author') == []
    assert connector.read('tag') == []

    connector.insertData([
        [
            title: "the book",
            isbn: 1234567890,
            author: "the prophet",
            tags: ["clever", "lies", "fantasy"]
        ]
    ])
            
    assert connector.read('book') == [[id: 1, title: "the book", isbn10: 1234567890]]
    assert connector.read('author') == [[id: 1, name: "the prophet"]]
    assert connector.read('book_author') == [[id_book: 1, id_author: 1]]
    assert connector.read('tag') == [
        [id: 1, id_book: 1, name: "clever"],
        [id: 2, id_book: 1, name: "lies"],
        [id: 3, id_book: 1, name: "fantasy"],
    ]
}

println "Test successful!"




private executeWithPresentTestTables(Closure code) {
    try {
        createTestTables()
    } catch (e) {
        println "Could not execute tests due to error!"
        println e
        System.exit 1
    }
    try {
        code()
    } finally {
        try {
            connector.sql.execute 'DROP TABLE book'
            connector.sql.execute 'DROP TABLE author'
            connector.sql.execute 'DROP TABLE book_author'
            connector.sql.execute 'DROP TABLE tag'
        } catch (e) {
            println "Something went horribly wrong. Check the database for leftover test data!"
            System.exit 1
        }
    }
}

private createTestTables() {
    connector.sql.execute '''
        CREATE TABLE book (
            id INTEGER NOT NULL AUTO_INCREMENT KEY,
            titel VARCHAR(255),
            isbn10 INTEGER
        )
    '''
    connector.sql.execute '''
        CREATE TABLE author (
            ID INTEGER NOT NULL AUTO_INCREMENT KEY,
            name VARCHAR(255)
        )
    '''
    connector.sql.execute '''
        CREATE TABLE book_author (
            id_book INTEGER,
            id_author INTEGER
        )
    '''
    connector.sql.execute '''
        CREATE TABLE tag (
            ID INTEGER NOT NULL AUTO_INCREMENT KEY,
            id_book INTEGER,
            name VARCHAR(255)
        )
    '''
}
