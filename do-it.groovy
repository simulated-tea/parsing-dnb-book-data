

def inputFileName

if (args.size() >= 1) {
    inputFileName = args[0]
}

if (null == inputFileName) {
    println "Please provide a file to be read!"
    System.exit 1
}

def inputFile = new File(inputFileName, )

def data = (new XmlSlurper()).parseText(inputFile.text)

def dataExtractionPlan = [
   [name: "ISBN",                           findby: [tag: "020", code: "9"],  resolve_multiple: "collect"],
   [name: "Einband und Preis",              findby: [tag: "020", code: "c"]],
   [name: "Verfasser",                      findby: [tag: "100", code: "a"]],
   [name: "Verfasser Lebensdaten",          findby: [tag: "100", code: "d"]],
   [name: "Übesetzer",                      findby: [tag: "700", code: "a"],  resolve_multiple: "collect"],
   [name: "Publisher",                      findby: [tag: "710", code: "a"],  resolve_multiple: "collect"],
   [name: "Titel",                          findby: [tag: "245", code: "a"]],
   [name: "Untertitel",                     findby: [tag: "245", code: "b"]],
   [name: "Verfasser auf Titel",            findby: [tag: "245", code: "c"]],
   [name: "Ausgabe",                        findby: [tag: "250", code: "a"]],
   [name: "Umfang",                         findby: [tag: "300", code: "a"],  resolve_multiple: "ignore"],
   [name: "Format",                         findby: [tag: "300", code: "c"],  resolve_multiple: "ignore"],
   [name: "Verlag Stadt",                   findby: [tag: "264", code: "a"]],
   [name: "Verlag Name",                    findby: [tag: "264", code: "b"]],
   [name: "Verlag Erscheinungsdatum",       findby: [tag: "264", code: "c"]],
   [name: "Schlagworte 1",                  findby: [tag: "648", code: "a"],  resolve_multiple: "collect"],
   [name: "Schlagworte 2",                  findby: [tag: "650", code: "a"],  resolve_multiple: "collect"],
   [name: "Schlagworte 3",                  findby: [tag: "651", code: "a"],  resolve_multiple: "collect"],
   [name: "Schlagworte exotisch",           findby: [tag: "653", code: "a"],  resolve_multiple: "collect"],
   [name: "Schlagworte obskur",             findby: [tag: "926", code: "x"],  resolve_multiple: "collect"],
   [name: "Bibliographisch Einordnung",     findby: [tag: "655", code: "a"],  resolve_multiple: "collect"],
   [name: "Bibliographisch Einordnung Typ", findby: [tag: "655", code: "2"],  resolve_multiple: "collect"],
   [name: "Abstract Link",                  findby: [tag: "856", code: "u"],  resolve_multiple: "collect"],
   [name: "Abstract Link Typ",              findby: [tag: "856", code: "3"],  resolve_multiple: "collect"],
]

System.exit 0
