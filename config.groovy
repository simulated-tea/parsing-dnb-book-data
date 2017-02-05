marc21 {
    dataExtractionPlan = [
       [name: "ISBN",                 findby: [tag: "020", code: "9"],  resolve_multiple: "collect"],
       [name: "Einband und Preis",    findby: [tag: "020", code: "c"]],
       [name: "Autor",                findby: [tag: "100", code: "a"]],
       [name: "Autor Lebensdaten",    findby: [tag: "100", code: "d"]],
       [name: "Autor 2",              findby: [tag: "700", code: "a", where: [code: "4", value: "aut"]]],
       [name: "Übersetzer",           findby: [tag: "700", code: "a", where: [code: "4", value: "trl"]]],
       [name: "Publisher",            findby: [tag: "710", code: "a"],  resolve_multiple: "collect"],
       [name: "Titel",                findby: [tag: "245", code: "a"]],
       [name: "Untertitel",           findby: [tag: "245", code: "b"]],
       [name: "Verfasser auf Titel",  findby: [tag: "245", code: "c"]],
       [name: "Ausgabe",              findby: [tag: "250", code: "a"]],
       [name: "Umfang",               findby: [tag: "300", code: "a"],  resolve_multiple: "ignore"],
       [name: "Format",               findby: [tag: "300", code: "c"],  resolve_multiple: "ignore"],
       [name: "Medium",               findby: [tag: "338", code: "a"]],
       [name: "Sachgruppe",           findby: [tag: "082", code: "a"],  resolve_multiple: "collect"],
       [name: "Verlag Ort",           findby: [tag: "264", code: "a"]],
       [name: "Verlag",               findby: [tag: "264", code: "b"]],
       [name: "Erscheinungsjahr",     findby: [tag: "264", code: "c"]],
       [name: "Schlagworte 1",        findby: [tag: "648", code: "a"],  resolve_multiple: "collect"],
       [name: "Schlagworte 2",        findby: [tag: "650", code: "a"],  resolve_multiple: "collect"],
       [name: "Schlagworte 3",        findby: [tag: "651", code: "a"],  resolve_multiple: "collect"],
       [name: "Schlagworte exotisch", findby: [tag: "653", code: "a"],  resolve_multiple: "collect"],
       [name: "Schlagworte obskur",   findby: [tag: "926", code: "x"],  resolve_multiple: "collect"],
       [name: "Gattung",              findby: [tag: "655", code: "a",   where: [code: "2", value: "gatbeg"]]],
       [name: "Inhalt",               findby: [tag: "655", code: "a",   where: [code: "2", value: "gnd-content"]]],
       [name: "Inhaltstext Link",     findby: [tag: "856", code: "u"],  resolve_multiple: "collect"],
       [name: "Inhaltstext Link Typ", findby: [tag: "856", code: "3"],  resolve_multiple: "collect"],
    ]
}

database {
    url      = 'jdbc:mariadb://localhost:3306/books'
    user     = 'root'
    password = 'root'

    schema_config = [
        [
            table: "buch",
            columns: [
                [name: "Titel",            type: "varchar(255)", source: "Titel"],
                [name: "Inhaltstext_Link", type: "varchar(255)", source: "Inhaltstext Link"],
            ],
        ], [
            table: "autor",
            columns: [
                [name: "Name", type: "varchar(255)", source: 'Autor'],
            ],
        ], [
            table: "buch_autor",
            columns: [
                [name: "ID_Buch",  type: "int(10)"],
                [name: "ID_Autor", type: "int(10)"],
            ],
        ], [
            table: "gattung",
            columns: [
                [name: "Gattung", type: "varchar(255)", source: "Gattung"],
            ],
        ], [
            table: "buch_gattung",
            columns: [
                [name: "ID_Buch",    type: "int(10)"],
                [name: "ID_Gattung", type: "int(10)"],
            ],
        ], [
            table: "inhalt",
            columns: [
                [name: "Inhalt", type: "varchar(255)", source: "Inhalt"],
            ],
        ], [
            table: "buch_inhalt",
            columns: [
                [name: "ID_Buch",   type: "int(10)"],
                [name: "ID_Inhalt", type: "int(10)"],
            ],
        ], [
            table: "sachgruppe",
            columns: [
                [name: "Sachgruppe", type: "varchar(255)", source: "Sachgruppe"],
            ],
        ], [
            table: "buch_sachgruppe",
            columns: [
                [name: "ID_Buch",       type: "int(10)"],
                [name: "ID_Sachgruppe", type: "int(10)"],
            ],
        ], [
            table: "schlagwort",
            columns: [
                [name: "Schlagwort", type: "varchar(255)", source: ["Schlagworte 1", "Schlagworte 2", "Schlagworte 3"]],
            ],
        ], [
            table: "buch_schlagwort",
            columns: [
                [name: "ID_Buch",       type: "int(10)"],
                [name: "ID_Schlagwort", type: "bigint(20)"],
            ],
        ], [
            table: "medium",
            columns: [
                [name: "ID_Buch",          type: "int(10)"],
                [name: "ISBN13",           type: "varchar(17)",  source: "ISBN"],
                [name: "Typ",              type: "varchar(20)",  source: "Medium"],
                [name: "Verlag",           type: "varchar(255)", source: "Verlag"],
                [name: "Verlag_Ort",       type: "varchar(255)", source: "Verlag Ort"],
                [name: "Erscheinungsjahr", type: "int(10)",      source: "Erscheinungsjahr"],
            ],
        ],
    ]
}
