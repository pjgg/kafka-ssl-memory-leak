#!/bin/bash
echo "Bash version ${BASH_VERSION}..."
for i in {0..1000}
  do 
     curl -v -X POST http://localhost:8080/kafka/ssl?key=keyTest&value=v3
 done
