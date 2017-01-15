package util

class Marc21XmlParser {
    def config

    Marc21XmlParser(config) {
        this.config = config
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
