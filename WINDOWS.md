# Docker 없이 Windows에서 바로 실행하기

이 앱은 Spring Boot 실행 가능 jar(fat jar) 하나에 프론트엔드(빌드된 정적 파일)까지 다 들어있습니다.
그래서 다른 Windows 컴퓨터에는 **Java와 MySQL만 설치**하면 되고, Node나 Docker는 필요 없습니다.

## 1. 준비물 (다른 컴퓨터에 설치)

- **Java 21** (JDK 21 또는 JRE 21) — https://adoptium.net 에서 설치
- **MySQL 8.0** (MySQL Installer로 설치, MySQL Server만 있으면 됨)

## 2. MySQL에 데이터베이스 만들기

MySQL Workbench나 `mysql` 커맨드라인으로 접속해서 아래 3줄만 실행하면 됩니다.
(테이블/더미데이터는 앱이 뜰 때 자동으로 채워지므로 빈 데이터베이스만 있으면 됩니다.)

```sql
CREATE DATABASE streak CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER 'ureca'@'%' IDENTIFIED BY 'ureca';
GRANT ALL PRIVILEGES ON streak.* TO 'ureca'@'%';
FLUSH PRIVILEGES;
```

## 3. 앱 파일 옮기기

`build/libs/Streak-0.0.1-SNAPSHOT.jar` 이 파일 하나만 다른 컴퓨터로 옮기면 됩니다
(USB, 클라우드 드라이브, 사내 메신저 등 아무 방법이나 괜찮습니다. 약 58MB).

이 jar는 이번 세션에서 수정한 프론트엔드 내용까지 전부 포함해서 빌드된 최신 버전입니다.
소스가 바뀔 때마다 다시 빌드하려면 이 저장소에서:

```bash
cd frontend && npm run build && cd ..
./gradlew bootJar
```

## 4. 실행

옮긴 jar 파일이 있는 폴더에서 (MySQL이 `localhost:3306`에서 돌고 있다는 가정):

```bash
java -jar Streak-0.0.1-SNAPSHOT.jar
```

MySQL이 다른 컴퓨터/포트에 있다면 접속 정보를 옵션으로 넘기면 됩니다:

```bash
java -jar Streak-0.0.1-SNAPSHOT.jar ^
  --spring.datasource.url=jdbc:mysql://<MySQL주소>:3306/streak ^
  --spring.datasource.username=ureca ^
  --spring.datasource.password=ureca
```

## 5. 접속

브라우저에서 `http://localhost:8080` (또는 그 컴퓨터의 IP:8080).
테스트 계정: `leader1@test.com` / `password123!`

## 참고

- 앱을 재시작할 때마다 `schema.sql`/`data.sql`이 다시 실행되어 테이블과 더미 데이터가 초기화됩니다.
- 8080 포트가 이미 사용 중이면 `--server.port=8081`처럼 포트를 바꿔서 실행하세요.
