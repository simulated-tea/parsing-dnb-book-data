import util.Marc21XmlParser

config = [
   [name: "ISBN",              findby: [tag: "020", code: "9"],  resolve_multiple: "collect"],
   [name: "Einband und Preis", findby: [tag: "020", code: "c"]],
   [name: "Titel",             findby: [tag: "245", code: "a"],  resolve_multiple: "ignore"],
   [name: "Gattung",           findby: [tag: "655", code: "a", where: [code: "2", value: "gatbeg"]]],
   [name: "Inhalt",            findby: [tag: "655", code: "a", where: [code: "2", value: "gnd-content"]]],
]

def file = new File("./testdata.groovy")

def parser = new Marc21XmlParser(config)
def result = parser.parseText(file.getText("UTF-8"))

assert result instanceof List
assert result.size() == 1
assert result[0].ISBN == ["978-3-462-04897-1", "3-462-04897-X"]
assert result[0]."Einband und Preis" == ["Festeinband : EUR 18.50 (AT), EUR 17.99 (DE)"]

//assert result[0].Titel == ["Kind aller Länder"] // alternate encoding ... :(
assert result[0].Titel.size() == 1
assert result[0].Titel[0] ==~ "Kind aller L.?.?nder"

assert result[0].Gattung.size() == 1
assert result[0].Gattung[0] ==~ "Erz.?.?hlende Literatur"
assert result[0].Inhalt == ["Fiktionale Darstellung"] 

println "Test successful!"
