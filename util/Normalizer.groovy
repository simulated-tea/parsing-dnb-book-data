package util

import groovy.json.JsonOutput as jo

class Normalizer {
    def config

    def transform = [
        find_year_in_string: { value -> (value =~ /.*(\d{4}).*/)[0][1] },
    ]

    def process(data) {
        data.collect{ entry ->
            def result = entry.clone()

            config.actions.each{ action ->
                try{
                    result[action.value] = transform[action.transformation] entry[action.value]
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
