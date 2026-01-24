mvn clean verify sonar:sonar \
  -Dmaven.test.failure.ignore=true \
  -Dsonar.projectKey=thingifier \
  -Dsonar.projectName=Thingifier \
  -Dsonar.host.url=http://localhost:9000 \
  -Dsonar.login=<LOGIN_TOKEN>
