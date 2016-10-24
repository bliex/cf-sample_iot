#cf-sample_iot

##빌드
###mvn 이 설치되어 있다면: 
`mvn clean package`

###mvn 이 설치되어 있지 않다면: 
`mvnw cmd clean package`

###output: 
 target/demo-0.0.1-SNAPSHOT.jar


##실행
###eclipse :
+ com.example.DemoApplication.java 에 대해서 Debug As > Java Application
+ Debug Configurations > Java Application > DemoApplication 
   [Arguments] > Program arguments:
   `--IOT_MGMT_URL=http://localhost/ --IOT_OID=abcd --IOT_DKEY=abcd`

###command line :
 `java -jar target/demo-0.0.1-SNAPSHOT.jar --IOT_MGMT_URL=http://localhost/ --IOT_OID=abcd --IOT_DKEY=abcd`


##필요 환경 변수
+ IOT_MGMT_URL
+ IOT_OID
+ IOT_DKEY

