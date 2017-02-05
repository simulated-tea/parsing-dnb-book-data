
config = (new ConfigSlurper()).parse(new URL('file:config.groovy'))

keys_in_marc_file = config.marc21.dataExtractionPlan*.name
keys_in_db_config = config.database.schema_config*.columns*.source.flatten().findAll()

keys_in_db_config.each{ key ->
    assert keys_in_marc_file.contains(key)
}

println "Test successful!"
