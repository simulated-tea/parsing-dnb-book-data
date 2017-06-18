import util.Normalizer


def default_config = [
    actions: [
        [value: "a", transformation: "find_year_in_string"],
    ]
]
def default_data = [[a: "März 2017"]]

transforms_are_applied: {
    def normalizer = new Normalizer(config: default_config)
    normalizer.transform = [ "find_year_in_string": { value -> "2017" } ]

    def results = normalizer.process(default_data)

    assert results.size() == 1
    def result = results[0]
    assert result.a == "2017"
}

test_year_finding: {
    def normalizer = new Normalizer(config: default_config)
    def data = [[a: "März 2017"]]

    def results = normalizer.process(data)

    assert results.size() == 1
    def result = results[0]
    assert result.a == "2017"
}


println "Test successful!"
