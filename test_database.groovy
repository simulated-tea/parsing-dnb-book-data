import util.MariaDbConnector

exception = null
getException = { test -> try { test() } catch (RuntimeException e) { exception = e } }
schema_config = [
    [
        table: 'book',
        columns: [
            [name: "title", type: "varchar(255)", source: "title"],
            [name: "isbn10", type: "int(10)", source: "isbn", optional: true],
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
tables = connector.tableCacher
sql = connector.tableCacher.sql
connector.log_debug = false
assert 'OK' == connector.testConnection()

executeWithPresentTestTables{
    println "Test: Can insert a full set of data"
    assert tables.readFromDb('book') == []
    assert tables.readFromDb('author') == []
    assert tables.readFromDb('book_author') == []
    assert tables.readFromDb('tag') == []

    def statistics = connector.importBookData([[
            title: "the book",
            isbn: 1234567890,
            author: "the prophet",
            tags: ["clever", "lies", "fantasy"],
    ]])

    assert tables.readFromDb('book') == [[id: 1, title: "the book", isbn10: 1234567890]]
    assert tables.readFromDb('author') == [[ID: 1, name: "the prophet"]]
    assert tables.readFromDb('book_author') == [[id_book: 1, id_author: 1]]
    assert tables.readFromDb('tag') == [
        [ID: 1, id_book: 1, name: "clever"],
        [ID: 2, id_book: 1, name: "lies"],
        [ID: 3, id_book: 1, name: "fantasy"],
    ]
    assert statistics == [
        items: 1,
        successful_imported: 1,
        failures: 0,
    ]
}
executeWithPresentTestTables{
    println "Test: Only complete records are imported."
    def originalOut = System.out
    def originalErr = System.err
    System.out = new PrintStream(new OutputStream() {void write(int b) { /* noop */ } })
    System.err = new PrintStream(new OutputStream() {void write(int b) { /* noop */ } })
    def statistics = connector.importBookData([[
            title: "the book",
            isbn: 1234567890,
            // author missing
            tags: [],
    ]])
    System.out = originalOut
    System.err = originalErr

    assert tables.readFromDb('book') == []
    assert tables.readFromDb('author') == []
    assert tables.readFromDb('book_author') == []
    assert tables.readFromDb('tag') == []
    assert statistics == [items: 1, successful_imported: 0, failures: 1]
}
executeWithPresentTestTables{
    println "Test: Optional data may be missing"
    connector.importBookData([[
            title: "the book",
            // isbn missing
            author: "the prophet",
            tags: [],
    ]])

    assert tables.readFromDb('book') == [[id: 1, title: "the book", isbn10: null]]
}
executeWithPresentTestTables{
    println "Test: No content in multi-value fields is admissible"
    connector.importBookData([[
            title: "the book",
            isbn: 1234567890,
            author: "the prophet",
            tags: [],        // no tags
    ]])

    assert tables.readFromDb('book') == [[id: 1, title: "the book", isbn10: 1234567890]]
    assert tables.readFromDb('tag') == []
}
executeWithPresentTestTables{
    println "Test: Deduplicates the author entry (or any table entry)"
    connector.importBookData([[
            title: "the book",
            isbn: 1234567890,
            author: "the prophet",
            tags: [],
        ],[
            title: "the new book",
            isbn: 1234567891,
            author: "the prophet",
            tags: [],
    ]])

    assert tables.readFromDb('book') == [
        [id: 1, title: "the book", isbn10: 1234567890],
        [id: 2, title: "the new book", isbn10: 1234567891],
    ]
    assert tables.readFromDb('author') == [[ID: 1, name: "the prophet"]]
    assert tables.readFromDb('book_author') == [
        [id_book: 1, id_author: 1],
        [id_book: 2, id_author: 1],
    ]
}
executeWithPresentTestTables{
    println "Test: Deduplicates if entry already exists in database"
    sql.execute 'INSERT INTO author (ID, name) VALUES (7, "the prophet")'

    connector.importBookData([[
            title: "the book",
            isbn: 1234567890,
            author: "the prophet",
            tags: [],
    ]])

    assert tables.readFromDb('author') == [[ID: 7, name: "the prophet"]]
    assert tables.readFromDb('book_author') == [[id_book: 1, id_author: 7]]
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
    tables.clear() // clear cache
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
    sql.execute '''
        CREATE TABLE book (
            id INTEGER NOT NULL AUTO_INCREMENT KEY,
            title VARCHAR(255),
            isbn10 INTEGER DEFAULT NULL
        )
    '''
    sql.execute '''
        CREATE TABLE author (
            ID INTEGER NOT NULL AUTO_INCREMENT KEY,
            name VARCHAR(255)
        )
    '''
    // maybe add foreign keys -- or maybe not: assertion errors are nicer after all
    sql.execute '''
        CREATE TABLE book_author (
            id_book INTEGER,
            id_author INTEGER
        )
    '''
    sql.execute '''
        CREATE TABLE tag (
            ID INTEGER NOT NULL AUTO_INCREMENT KEY,
            id_book INTEGER,
            name VARCHAR(255)
        )
    '''
}

private dropTestTables() {
    sql.execute 'DROP TABLE book'
    sql.execute 'DROP TABLE author'
    sql.execute 'DROP TABLE book_author'
    sql.execute 'DROP TABLE tag'
}
