package util

/**
 * Parser to read bibliographic data as downloaded from
 * http://www.dnb.de/EN/Home/home_node.html
 */
class Marc21XmlParser {
    def config

    Marc21XmlParser(config) {
        if (config) {
            this.config = config
        }
    }

    def datafieldsWithValueInTaggedSubfield(dataList, code, value) {
        dataList.findAll{ xmlData ->
            (xmlData.subfield.find{ it.@code == code } as String) == value
        }
    }

    def parseText(text) {
        def inputData = (new XmlSlurper()).parseText(text)
        def allBooks = inputData.record
        def result = []

        allBooks.each{ bookData ->
            def newBookEntry = [:]
            config.each{ datumToExtract ->

                def foundDatafields = bookData\
                        .datafield.findAll{ it.@tag == datumToExtract.findby.tag }
                
                if (datumToExtract.findby.where) {
                    def where = datumToExtract.findby.where
                    foundDatafields = datafieldsWithValueInTaggedSubfield(foundDatafields, where.code, where.value)
                }

                def foundData = foundDatafields\
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
