#cf-sample_iot

##빌드
###gradle이 설치되어 있지 않다면: 
`gradlew assemble`

###gradle이 설치되어 있다면: 
`gradle assemble`

###output: 
 build/libs/app-1.0.jar


##실행
###eclipse :
+ com.loe.ChromeAppApplication.java 에 대해서 Debug As > Java Application
+ Debug Configurations > Java Application > ChromeAppApplication 
   [Arguments] > Program arguments:
   `--IOT_MGMT_URL=http://localhost/ --IOT_OID=abcd --IOT_DKEY=abcd`

###command line :
 `java -jar build/libs/app-1.0.jar --IOT_MGMT_URL=http://localhost/ --IOT_OID=abcd --IOT_DKEY=abcd`


##필요 환경 변수
+ IOT_MGMT_URL
+ IOT_OID
+ IOT_DKEY

