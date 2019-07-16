# CLI_App

* Spring-Shell CLI 어플리케이션에 의해 관리의 대상이 될 서버의 샘플입니다.
* **Spring_Shell** 프로젝트와 한 쌍이 됩니다. 
* 아래와 같은 기능들을 수행할 수 있으며, 원격으로 요청될 수 있도록 각각 엔드포인트가 정의되어 있습니다.
  - 로컬 디렉토리에 위치한 특정 Shell(Bash) Script를 실행하고, 결과값과 성공/실패 여부를 반환(현재 프로젝트에서는 *hello.sh*)
  - 다른 서버에 대한 주기적인 Health check (기능 동작 여부 토글, 타겟 주소 정보 변경 가능)
  - Health check 수행 중 예외 발생 시 로그 정보는 Embedded DB(*Apache Derby*)에 저장
  - 어플리케이션 모니터링 정보(*Spring Actuator*) 및 시스템 모니터링 정보(*monitor.sh*)를 JSON 포맷으로 반환
  - 인덱스 페이지에서 시스템 모니터링 정보(CPU, Memory, Process)를 실시간으로 확인 가능(SSE를 통한 스트리밍)
