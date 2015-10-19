#/bin/sh
curl http://localhost:8080/workflow-engine/api/processbpmn -X POST -i -F 'file=@./test.bpmn20.xml;type=text/xml' -H "Content-Type: multipart/form-data"