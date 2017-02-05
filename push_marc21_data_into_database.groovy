import util.Marc21XmlParser
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


System.exit 0
