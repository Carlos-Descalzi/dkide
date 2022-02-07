#!/bin/bash
pwd=$PWD
pushd src/grammars/json
java -jar $pwd/antlr4.jar JSON.g4 -o $pwd/src/main/java/io/datakitchen/ide/json/parser -visitor -Dlanguage=Java