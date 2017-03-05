import util.Marc21XmlParser
import util.MariaDbConnector
import groovy.sql.Sql

def inputFileName

if (args.size() >= 1) {
    inputFileName = args[0]
}

if (null == inputFileName) {
    println "Please provide a file to be read!"
    System.exit 1
}

def inputFile = new File(inputFileName)

config = (new ConfigSlurper()).parse(new URL('file:config.groovy'))

def parser = new Marc21XmlParser(config.marc21.dataExtractionPlan)
def bookData = parser.parseText(inputFile.text)

def dbConnector = new MariaDbConnector(config)
dbConnector.log_debug = true
dbConnector.importBookData(bookData)

System.exit 0
