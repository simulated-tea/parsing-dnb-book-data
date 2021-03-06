import util.Marc21XmlParser
import util.MariaDbConnector
import util.Normalizer
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

def normalizer = new Normalizer(config: config.normalize)
def qualityBookData = normalizer.process(bookData)

def dbConnector = new MariaDbConnector(config)
dbConnector.log_debug = true
dbConnector.importBookData(qualityBookData)

System.exit 0
