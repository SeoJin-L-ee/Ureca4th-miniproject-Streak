# Docker로 실행하기

Java, Node, MySQL을 따로 설치하지 않고 Docker만 있으면 이 저장소를 그대로 실행할 수 있습니다.
프론트엔드 빌드 → 백엔드 빌드(jar) → MySQL 기동까지 `docker compose` 한 번으로 처리됩니다.

## 준비물

- Docker Desktop (Docker Engine + Docker Compose v2) 설치된 컴퓨터

## 실행 방법

저장소 루트(이 파일이 있는 위치)에서:

```bash
docker compose up --build
```

- 처음 실행할 때는 프론트엔드/백엔드 빌드 + MySQL 이미지 다운로드 때문에 몇 분 정도 걸립니다.
- 빌드가 끝나고 `Started StreakApplication`류의 로그가 보이면 준비된 것입니다.
- 브라우저에서 http://localhost:8080 접속.

## 테스트 계정

`data.sql`에 들어있는 더미 계정 중 하나로 로그인하면 됩니다.

- `leader1@test.com` / `password123!`

## 데이터 초기화

앱이 재시작될 때마다 `schema.sql` / `data.sql`이 다시 실행되어 테이블과 더미 데이터가 초기화됩니다.
즉 화면에서 직접 추가/수정한 데이터는 컨테이너를 재시작하면 사라집니다.

## 종료 / 정리

```bash
# 컨테이너만 종료 (MySQL 데이터는 볼륨에 유지됨)
docker compose down

# MySQL 데이터까지 완전히 초기화하고 싶을 때
docker compose down -v
```

## 참고

- 8080(백엔드+정적 프론트), 3306(MySQL) 포트를 사용합니다. 다른 프로세스가 이미 쓰고 있다면 `docker-compose.yml`의 `ports`를 바꿔주세요.
