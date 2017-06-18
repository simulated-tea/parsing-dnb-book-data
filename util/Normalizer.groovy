package util

import groovy.json.JsonOutput as jo

class Normalizer {
    def config

    def transform = [
        find_year_in_string: { value ->
            def matcher = value =~ /.*(\d{4}).*/
            if (matcher) return matcher[0][1]
            null
        },
        strip_brackets: { it - '[' - ']' },
    ]

    def process(data) {
        data.collect{ entry ->
            def result = entry.clone()

            config.actions.each{ action ->
                try{
                    def value = entry[action.value]
                    if (value instanceof List) {
                        result[action.value] = value.collect{ transform[action.transformation] it }
                    } else {
                        result[action.value] = transform[action.transformation] value
                    }
                } catch (AssertionError e) {
                    println "WARNING -- Transformation malfunctioned on: $action.value -- dropping"
                    println jo.prettyPrint(jo.toJson(entry))
                } catch (Exception e) {
                    println "ERROR   -- Unexpected error in transform on: $action.value"
                    println jo.prettyPrint(jo.toJson(entry))
                    throw e
                }
            }

            result
        }
    }
}
