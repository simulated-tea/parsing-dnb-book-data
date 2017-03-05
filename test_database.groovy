import util.MariaDbConnector

exception = null
getException = { test -> try { test() } catch (RuntimeException e) { exception = e } }
schema_config = [
    [
        table: 'book',
        columns: [
            [name: "title", type: "varchar(255)", source: "title"],
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
        table: 'tag', // Bad example, since usually tag would rather be done via a join table
                      // like for author above.
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
connector.log_debug = false
assert 'OK' == connector.testConnection()

executeWithPresentTestTables{ // can insert book data
    assert connector.read('book') == []
    assert connector.read('author') == []
    assert connector.read('book_author') == []
    assert connector.read('tag') == []

    connector.importBookData([
        [
            title: "the book",
            isbn: 1234567890,
            author: "the prophet",
            tags: ["clever", "lies", "fantasy"]
        ]
    ])

    assert connector.read('book') == [[id: 1, title: "the book", isbn10: 1234567890]]
    assert connector.read('author') == [[ID: 1, name: "the prophet"]]
    assert connector.read('book_author') == [[id_book: 1, id_author: 1]]
    assert connector.read('tag') == [
        [ID: 1, id_book: 1, name: "clever"],
        [ID: 2, id_book: 1, name: "lies"],
        [ID: 3, id_book: 1, name: "fantasy"],
    ]
}
// TODO: Test with reusing existing author from db!

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
            dropTestTables()
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
            title VARCHAR(255),
            isbn10 INTEGER
        )
    '''
    connector.sql.execute '''
        CREATE TABLE author (
            ID INTEGER NOT NULL AUTO_INCREMENT KEY,
            name VARCHAR(255)
        )
    '''
    // TODO: add foreign keys
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

private dropTestTables() {
    connector.sql.execute 'DROP TABLE book'
    connector.sql.execute 'DROP TABLE author'
    connector.sql.execute 'DROP TABLE book_author'
    connector.sql.execute 'DROP TABLE tag'
}
