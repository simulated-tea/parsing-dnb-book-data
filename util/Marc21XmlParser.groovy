package util

/**
 * Parser to read bibliographic data as downloaded from
 * http://www.dnb.de/EN/Home/home_node.html
 */
class Marc21XmlParser {
    def config = [ // some default values
        [name: "ISBN",                     findby: [tag: "020", code: "9"],  resolve_multiple: "collect"],
        [name: "Einband und Preis",        findby: [tag: "020", code: "c"]],
        [name: "Verfasser",                findby: [tag: "100", code: "a"]],
        [name: "Übesetzer",                findby: [tag: "700", code: "a"],  resolve_multiple: "collect"],
        [name: "Publisher",                findby: [tag: "710", code: "a"],  resolve_multiple: "collect"],
        [name: "Titel",                    findby: [tag: "245", code: "a"]],
        [name: "Untertitel",               findby: [tag: "245", code: "b"]],
        [name: "Verfasser auf Titel",      findby: [tag: "245", code: "c"]],
        [name: "Ausgabe",                  findby: [tag: "250", code: "a"]],
        [name: "Umfang",                   findby: [tag: "300", code: "a"],  resolve_multiple: "ignore"],
        [name: "Format",                   findby: [tag: "300", code: "c"],  resolve_multiple: "ignore"],
        [name: "Verlag Stadt",             findby: [tag: "264", code: "a"]],
        [name: "Verlag Name",              findby: [tag: "264", code: "b"]],
        [name: "Verlag Erscheinungsdatum", findby: [tag: "264", code: "c"]],
        [name: "Klappentext Link",         findby: [tag: "856", code: "u"],  resolve_multiple: "collect"],
        [name: "Klappentext Link Typ",     findby: [tag: "856", code: "3"],  resolve_multiple: "collect"],
    ]

    Marc21XmlParser() {}
    Marc21XmlParser(config) {
        if (config) {
            this.config = config
        }
    }

    def parseText(text) {
        def inputData = (new XmlSlurper()).parseText(text)
        def allBooks = inputData.record
        def result = []

        allBooks.each{ bookData ->
            def newBookEntry = [:]
            config.each{ datumToExtract ->
                def foundData = bookData\
                        .datafield.findAll{ it.@tag == datumToExtract.findby.tag }
                        .subfield.findAll{ it.@code == datumToExtract.findby.code }
                        .collect{ it as String }

                def data = []
                if (foundData.size > 0) {
                    switch(datumToExtract.resolve_multiple) {
                        case "ignore":
                            data = foundData.subList(0, 1) 
                            break
                        case "collect":
                            data = foundData
                            break
                        default:
                            data = foundData
                    }
                }
                newBookEntry[datumToExtract.name] = data
            }
            result << newBookEntry
        }
        result
    }
}
