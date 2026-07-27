-- Streak 프로젝트 더미(테스트) 데이터
-- application.properties의 spring.sql.init.mode=always 설정에 의해
-- 앱 실행 시 schema.sql 다음에 자동으로 실행된다. (수동 실행: mysql -u ureca -p streak < src/main/resources/data.sql)
--
-- 회원 40명 / 스터디 20개 / 세션 33개 / 과제 30개 / 출석 136건 / 제출 123건 / 참여자 52건 / 가입신청 46건
--
-- ※ 세션/과제 마감일은 전부 CURDATE() 기준 "이번 주 수요일"(@this_wed)을 기준으로 계산한다.
--   과거로 -n주, 미래로 +n주 식으로만 계산해서(0주차는 쓰지 않음) 서버를 언제 실행하든
--   "이미 지난 회차 / 아직 안 지난 회차" 구분이 항상 자연스럽게 맞도록 했다.
--   즉 이 파일은 실행 시점이 언제든 항상 최신 상태처럼 보이도록 설계되어 있다.
--
-- 로그인 가능 계정(비밀번호 원문 1234qwer): test1@gmail.com, test2@gmail.com
-- 그 외 member3~40@test.com은 비밀번호 해시가 더미값이라 로그인 불가 (조회/연관관계 테스트 전용)
--
-- 스터디 구성
--   [진행 중인 스터디 6개 - 세션/과제/출석/제출 데이터 있음]
--   1. 코딩테스트 대비반         (ALGORITHM,   CLOSED)     - 8주 과정 중 5주차 진행, 정원 마감
--   2. 토익 900+ 스피드 스터디   (ENGLISH,     CLOSED)     - 6주 과정 중 4주차 진행, 정원 마감
--   3. 정보처리기사 실기 벼락치기 (CERTIFICATE, RECRUITING) - 2주차 진행, 아직 정원 여유 있음
--   4. 사이드 프로젝트: 스터디 매칭 앱 (ETC, ENDED)         - 6주 과정 완주 후 종료
--   5. CS 전공지식 스터디        (ETC,         RECRUITING) - 이제 막 1주차 시작
--   6. 리트코드 위클리 챌린지    (ALGORITHM,   CLOSED)     - 8주 과정 중 7주차 진행, 정원 마감
--   [막 개설되어 세션이 아직 없는 모집중 스터디 13개] - id 7~19
--   [소프트 삭제된 스터디 1개 - 목록 미노출 테스트용] - id 20

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;
TRUNCATE TABLE submissions;
TRUNCATE TABLE attendances;
TRUNCATE TABLE assignments;
TRUNCATE TABLE study_sessions;
TRUNCATE TABLE applications;
TRUNCATE TABLE participants;
TRUNCATE TABLE studies;
TRUNCATE TABLE members;
SET FOREIGN_KEY_CHECKS = 1;

-- =========================================================
-- 날짜 계산용 세션 변수
-- =========================================================
-- 이번 주(월~일) 안에 있는 수요일 (WEEKDAY: 월=0 ... 일=6, 수=2)
SET @this_wed = DATE_ADD(CURDATE(), INTERVAL (2 - WEEKDAY(CURDATE())) DAY);

-- 스터디1(코딩테스트 대비반) - 매주 수요일 19:30, 5주차 진행 중(과거 5회 + 미래 3회)
SET @s1_1 = ADDTIME(DATE_ADD(@this_wed, INTERVAL -5 WEEK), '19:30:00');
SET @s1_2 = ADDTIME(DATE_ADD(@this_wed, INTERVAL -4 WEEK), '19:30:00');
SET @s1_3 = ADDTIME(DATE_ADD(@this_wed, INTERVAL -3 WEEK), '19:30:00');
SET @s1_4 = ADDTIME(DATE_ADD(@this_wed, INTERVAL -2 WEEK), '19:30:00');
SET @s1_5 = ADDTIME(DATE_ADD(@this_wed, INTERVAL -1 WEEK), '19:30:00');
SET @s1_6 = ADDTIME(DATE_ADD(@this_wed, INTERVAL  1 WEEK), '19:30:00');
SET @s1_7 = ADDTIME(DATE_ADD(@this_wed, INTERVAL  2 WEEK), '19:30:00');
SET @s1_8 = ADDTIME(DATE_ADD(@this_wed, INTERVAL  3 WEEK), '19:30:00');

-- 스터디2(토익 900+) - 매주 수요일 20:00, 4주차 진행 중(과거 4회 + 미래 2회)
SET @s2_1 = ADDTIME(DATE_ADD(@this_wed, INTERVAL -4 WEEK), '20:00:00');
SET @s2_2 = ADDTIME(DATE_ADD(@this_wed, INTERVAL -3 WEEK), '20:00:00');
SET @s2_3 = ADDTIME(DATE_ADD(@this_wed, INTERVAL -2 WEEK), '20:00:00');
SET @s2_4 = ADDTIME(DATE_ADD(@this_wed, INTERVAL -1 WEEK), '20:00:00');
SET @s2_5 = ADDTIME(DATE_ADD(@this_wed, INTERVAL  1 WEEK), '20:00:00');
SET @s2_6 = ADDTIME(DATE_ADD(@this_wed, INTERVAL  2 WEEK), '20:00:00');

-- 스터디3(정보처리기사) - 매주 수요일 21:00, 2주차 진행 중(과거 2회 + 미래 1회)
SET @s3_1 = ADDTIME(DATE_ADD(@this_wed, INTERVAL -2 WEEK), '21:00:00');
SET @s3_2 = ADDTIME(DATE_ADD(@this_wed, INTERVAL -1 WEEK), '21:00:00');
SET @s3_3 = ADDTIME(DATE_ADD(@this_wed, INTERVAL  1 WEEK), '21:00:00');

-- 스터디4(사이드 프로젝트) - 매주 수요일 19:00, 6주 전부 과거(종료됨)
SET @s4_1 = ADDTIME(DATE_ADD(@this_wed, INTERVAL -6 WEEK), '19:00:00');
SET @s4_2 = ADDTIME(DATE_ADD(@this_wed, INTERVAL -5 WEEK), '19:00:00');
SET @s4_3 = ADDTIME(DATE_ADD(@this_wed, INTERVAL -4 WEEK), '19:00:00');
SET @s4_4 = ADDTIME(DATE_ADD(@this_wed, INTERVAL -3 WEEK), '19:00:00');
SET @s4_5 = ADDTIME(DATE_ADD(@this_wed, INTERVAL -2 WEEK), '19:00:00');
SET @s4_6 = ADDTIME(DATE_ADD(@this_wed, INTERVAL -1 WEEK), '19:00:00');

-- 스터디5(CS 전공지식) - 매주 수요일 20:30, 1주차 진행 중(과거 1회 + 미래 1회)
SET @s5_1 = ADDTIME(DATE_ADD(@this_wed, INTERVAL -1 WEEK), '20:30:00');
SET @s5_2 = ADDTIME(DATE_ADD(@this_wed, INTERVAL  1 WEEK), '20:30:00');

-- 스터디6(리트코드 위클리) - 매주 수요일 21:30, 7주차 진행 중(과거 7회 + 미래 1회)
SET @s6_1 = ADDTIME(DATE_ADD(@this_wed, INTERVAL -7 WEEK), '21:30:00');
SET @s6_2 = ADDTIME(DATE_ADD(@this_wed, INTERVAL -6 WEEK), '21:30:00');
SET @s6_3 = ADDTIME(DATE_ADD(@this_wed, INTERVAL -5 WEEK), '21:30:00');
SET @s6_4 = ADDTIME(DATE_ADD(@this_wed, INTERVAL -4 WEEK), '21:30:00');
SET @s6_5 = ADDTIME(DATE_ADD(@this_wed, INTERVAL -3 WEEK), '21:30:00');
SET @s6_6 = ADDTIME(DATE_ADD(@this_wed, INTERVAL -2 WEEK), '21:30:00');
SET @s6_7 = ADDTIME(DATE_ADD(@this_wed, INTERVAL -1 WEEK), '21:30:00');
SET @s6_8 = ADDTIME(DATE_ADD(@this_wed, INTERVAL  1 WEEK), '21:30:00');

-- =========================================================
-- 회원 40명 (비밀번호 원문: test1/test2 = 1234qwer, 나머지는 더미 해시라 로그인 불가)
-- =========================================================
INSERT INTO members (id, email, password, name, phone, status, created_at, updated_at) VALUES
(1,'test1@gmail.com','$2a$10$N9KNK2LfyhMsb8x8odJfpeD/B4mLlOGn7YFh5RfVsJUvyJbigxqyS','김도윤','010-1000-0001','ACTIVE',NOW(),NOW()),
(2,'test2@gmail.com','$2a$10$N9KNK2LfyhMsb8x8odJfpeD/B4mLlOGn7YFh5RfVsJUvyJbigxqyS','이서연','010-1000-0002','ACTIVE',NOW(),NOW()),
(3,'member3@test.com','$2a$10$dummyhash','박서준','010-1000-0003','ACTIVE',NOW(),NOW()),
(4,'member4@test.com','$2a$10$dummyhash','최지우','010-1000-0004','ACTIVE',NOW(),NOW()),
(5,'member5@test.com','$2a$10$dummyhash','정하윤','010-1000-0005','ACTIVE',NOW(),NOW()),
(6,'member6@test.com','$2a$10$dummyhash','강민준','010-1000-0006','ACTIVE',NOW(),NOW()),
(7,'member7@test.com','$2a$10$dummyhash','조유진','010-1000-0007','ACTIVE',NOW(),NOW()),
(8,'member8@test.com','$2a$10$dummyhash','윤지호','010-1000-0008','ACTIVE',NOW(),NOW()),
(9,'member9@test.com','$2a$10$dummyhash','장서윤','010-1000-0009','ACTIVE',NOW(),NOW()),
(10,'member10@test.com','$2a$10$dummyhash','임도현','010-1000-0010','ACTIVE',NOW(),NOW()),
(11,'member11@test.com','$2a$10$dummyhash','한소율','010-1000-0011','ACTIVE',NOW(),NOW()),
(12,'member12@test.com','$2a$10$dummyhash','오은우','010-1000-0012','ACTIVE',NOW(),NOW()),
(13,'member13@test.com','$2a$10$dummyhash','서지안','010-1000-0013','ACTIVE',NOW(),NOW()),
(14,'member14@test.com','$2a$10$dummyhash','신예준','010-1000-0014','ACTIVE',NOW(),NOW()),
(15,'member15@test.com','$2a$10$dummyhash','권나윤','010-1000-0015','ACTIVE',NOW(),NOW()),
(16,'member16@test.com','$2a$10$dummyhash','황시우','010-1000-0016','ACTIVE',NOW(),NOW()),
(17,'member17@test.com','$2a$10$dummyhash','안수아','010-1000-0017','ACTIVE',NOW(),NOW()),
(18,'member18@test.com','$2a$10$dummyhash','송민재','010-1000-0018','ACTIVE',NOW(),NOW()),
(19,'member19@test.com','$2a$10$dummyhash','전하람','010-1000-0019','ACTIVE',NOW(),NOW()),
(20,'member20@test.com','$2a$10$dummyhash','홍유나','010-1000-0020','ACTIVE',NOW(),NOW()),
(21,'member21@test.com','$2a$10$dummyhash','배준서','010-1000-0021','ACTIVE',NOW(),NOW()),
(22,'member22@test.com','$2a$10$dummyhash','노윤서','010-1000-0022','ACTIVE',NOW(),NOW()),
(23,'member23@test.com','$2a$10$dummyhash','유하준','010-1000-0023','ACTIVE',NOW(),NOW()),
(24,'member24@test.com','$2a$10$dummyhash','문서아','010-1000-0024','ACTIVE',NOW(),NOW()),
(25,'member25@test.com','$2a$10$dummyhash','양지훈','010-1000-0025','ACTIVE',NOW(),NOW()),
(26,'member26@test.com','$2a$10$dummyhash','손예은','010-1000-0026','ACTIVE',NOW(),NOW()),
(27,'member27@test.com','$2a$10$dummyhash','백승우','010-1000-0027','ACTIVE',NOW(),NOW()),
(28,'member28@test.com','$2a$10$dummyhash','심가은','010-1000-0028','ACTIVE',NOW(),NOW()),
(29,'member29@test.com','$2a$10$dummyhash','구민서','010-1000-0029','ACTIVE',NOW(),NOW()),
(30,'member30@test.com','$2a$10$dummyhash','곽태윤','010-1000-0030','ACTIVE',NOW(),NOW()),
(31,'member31@test.com','$2a$10$dummyhash','나은채','010-1000-0031','ACTIVE',NOW(),NOW()),
(32,'member32@test.com','$2a$10$dummyhash','마준영','010-1000-0032','ACTIVE',NOW(),NOW()),
(33,'member33@test.com','$2a$10$dummyhash','반지민','010-1000-0033','ACTIVE',NOW(),NOW()),
(34,'member34@test.com','$2a$10$dummyhash','방소윤','010-1000-0034','ACTIVE',NOW(),NOW()),
(35,'member35@test.com','$2a$10$dummyhash','표동현','010-1000-0035','ACTIVE',NOW(),NOW()),
(36,'member36@test.com','$2a$10$dummyhash','여준혁','010-1000-0036','ACTIVE',NOW(),NOW()),
(37,'member37@test.com','$2a$10$dummyhash','은서율','010-1000-0037','ACTIVE',NOW(),NOW()),
(38,'member38@test.com','$2a$10$dummyhash','감민지','010-1000-0038','ACTIVE',NOW(),NOW()),
(39,'member39@test.com','$2a$10$dummyhash','국지호','010-1000-0039','DISABLED',NOW(),NOW()),
(40,'member40@test.com','$2a$10$dummyhash','기서현','010-1000-0040','ACTIVE',NOW(),NOW());

-- =========================================================
-- 스터디 20개
-- =========================================================
INSERT INTO studies (id, title, description, capacity, category, status, is_deleted, created_at, updated_at) VALUES
(1,'코딩테스트 대비반','매주 수요일 저녁 7시 30분, 온라인 Zoom으로 진행하는 코딩테스트 대비 스터디입니다.\n\n[목표] 프로그래머스/백준 실버~골드 난이도 문제를 주 3문제 이상 풀고 매 회차 서로의 풀이를 리뷰합니다.\n\n[출석 규칙] 무단 결석이 2회 누적되면 스터디장과 상의 후 다음 기수부터 참여가 제한될 수 있습니다. 부득이한 경우 최소 하루 전에는 미리 공지해주세요.\n\n[모집 현황] 현재 정원이 마감되어 추가 모집은 받지 않습니다.',6,'ALGORITHM','CLOSED',false,DATE_SUB(@s1_1, INTERVAL 5 DAY),NOW()),
(2,'토익 900+ 스피드 스터디','토익 900점 이상을 목표로 매주 수요일 저녁 8시에 온라인으로 모이는 스터디입니다.\n\n[진행 방식] 매 회차 LC/RC 모의고사 1회분을 풀고 오답노트를 공유합니다.\n\n[과제 규칙] 단어 암기 테스트 과제를 2회 이상 미제출하면 스터디비 환급 대상에서 제외됩니다.\n\n[모집 현황] 현재 정원이 다 차서 모집을 종료했습니다.',5,'ENGLISH','CLOSED',false,DATE_SUB(@s2_1, INTERVAL 4 DAY),NOW()),
(3,'정보처리기사 실기 벼락치기','정보처리기사 실기 시험을 함께 준비하는 스터디로, 매주 수요일 저녁 9시 온라인(디스코드)으로 진행합니다.\n\n[진행 방식] 회차마다 기출문제 20문제를 풀고 오답 위주로 해설합니다.\n\n[출석 규칙] 출석률이 70% 미만이면 다음 회차부터 참여가 제한될 수 있습니다.\n\n[모집 현황] 아직 정원에 여유가 있어 추가 모집 중입니다.',8,'CERTIFICATE','RECRUITING',false,DATE_SUB(@s3_1, INTERVAL 4 DAY),NOW()),
(4,'사이드 프로젝트: 스터디 매칭 앱','우리 동네 스터디 매칭 앱을 함께 만든 사이드 프로젝트 스터디였습니다.\n\n[진행 방식] 매주 수요일 저녁 7시 온라인으로 모여 기획-디자인-개발 진행 상황을 공유했습니다.\n\n[운영 규칙] 무단 결석이 잦으면 다음 스프린트에서 역할을 조정하는 방식으로 운영했습니다.\n\n[진행 현황] 6주간의 프로젝트를 마치고 최종 데모까지 마무리하여 현재는 종료된 스터디입니다.',5,'ETC','ENDED',false,DATE_SUB(@s4_1, INTERVAL 5 DAY),NOW()),
(5,'CS 전공지식 스터디','운영체제·네트워크·데이터베이스 등 CS 전공지식을 정리하는 스터디입니다.\n\n[진행 방식] 매주 수요일 저녁 8시 30분 온라인으로 진행하며, 각자 정리한 내용을 발표하고 질문을 주고받습니다.\n\n[출석 규칙] 무단 결석 2회 시 운영진과 별도 면담을 진행합니다.\n\n[모집 현황] 이제 막 시작한 스터디라 아직 자리가 남아있습니다.',7,'ETC','RECRUITING',false,DATE_SUB(@s5_1, INTERVAL 3 DAY),NOW()),
(6,'리트코드 위클리 챌린지','리트코드 문제를 매주 수요일 밤 9시 30분에 온라인으로 함께 풀어보는 위클리 챌린지 스터디입니다.\n\n[진행 방식] 회차마다 Medium 난이도 이상 문제 2개를 풀이하고 코드 리뷰를 진행합니다.\n\n[출석 규칙] 결석 2회 이상 시 다음 기수 참여 우선순위에서 제외됩니다.\n\n[모집 현황] 현재 정원이 마감되어 모집을 종료했습니다.',6,'ALGORITHM','CLOSED',false,DATE_SUB(@s6_1, INTERVAL 6 DAY),NOW()),
(7,'매일 기록 챌린지: 회고 스터디','매일 하루를 짧게 회고하고 인증하는 스터디입니다.\n\n[일정] 정기 모임은 매주 화요일 저녁 9시 온라인으로 진행할 예정입니다.\n\n[출석 규칙] 3일 이상 무단 미인증 시 스터디에서 제외될 수 있습니다.\n\n[모집 현황] 이제 막 개설된 스터디로 함께할 팀원을 모집 중입니다.',10,'ETC','RECRUITING',false,DATE_SUB(NOW(), INTERVAL 13 DAY),NOW()),
(8,'오픽 IH 목표 스피킹 클럽','오픽 IH 등급 취득을 목표로 하는 스피킹 스터디입니다.\n\n[일정] 매주 목요일 저녁 8시 온라인 화상으로 모여 롤플레이와 모의시험을 진행할 예정입니다.\n\n[출석 규칙] 출석률 80% 미만이면 다음 기수부터 참여가 제한됩니다.\n\n[모집 현황] 아직 스터디장 혼자라 팀원을 기다리고 있습니다.',6,'ENGLISH','RECRUITING',false,DATE_SUB(NOW(), INTERVAL 12 DAY),NOW()),
(9,'SQLD 3주 완성반','SQLD 자격증을 3주 안에 취득하는 것을 목표로 하는 벼락치기 스터디입니다.\n\n[일정] 매주 월/수요일 저녁 8시 온라인으로 모의고사를 풀고 오답을 리뷰합니다.\n\n[과제 규칙] 과제 미제출이 누적되면 스터디장이 개별 안내를 드립니다.',8,'CERTIFICATE','RECRUITING',false,DATE_SUB(NOW(), INTERVAL 11 DAY),NOW()),
(10,'취준생 포트폴리오 스터디','백엔드 취업 준비생을 위한 포트폴리오 완성 스터디입니다.\n\n[일정] 매주 금요일 저녁 7시 온라인으로 모여 서로의 프로젝트를 리뷰합니다.\n\n[출석 규칙] 무단 결석 2회 시 참여가 제한될 수 있습니다.\n\n[모집 현황] 개설한 지 얼마 안 되어 팀원을 모집 중입니다.',5,'ETC','RECRUITING',false,DATE_SUB(NOW(), INTERVAL 10 DAY),NOW()),
(11,'자바 코딩테스트 입문반','자바로 코딩테스트 기초를 다지는 입문자용 스터디입니다.\n\n[일정] 매주 수요일 저녁 7시 온라인으로 진행하며, 매회 기초 문제 5개를 함께 풀이합니다.\n\n[규칙] 결석 시 다음 회차 전까지 밀린 문제를 반드시 풀어와야 합니다.',6,'ALGORITHM','RECRUITING',false,DATE_SUB(NOW(), INTERVAL 9 DAY),NOW()),
(12,'토익스피킹 레벨7 목표반','토익스피킹 레벨7 취득을 목표로 하는 스터디입니다.\n\n[일정] 매주 화요일 저녁 9시 온라인 진행 예정입니다.\n\n[출석 규칙] 출석 미달(70% 미만) 시 다음 기수 참여가 제한됩니다.\n\n[모집 현황] 스터디장 1인 개설 단계로 팀원을 기다리는 중입니다.',6,'ENGLISH','RECRUITING',false,DATE_SUB(NOW(), INTERVAL 8 DAY),NOW()),
(13,'빅데이터분석기사 준비반','빅데이터분석기사 필기·실기를 함께 준비하는 스터디입니다.\n\n[일정] 매주 목요일 저녁 8시 온라인으로 모여 기출문제를 풀이합니다.\n\n[출석 규칙] 무단 결석 2회 누적 시 스터디장과 면담 후 계속 여부를 결정합니다.',7,'CERTIFICATE','RECRUITING',false,DATE_SUB(NOW(), INTERVAL 7 DAY),NOW()),
(14,'사이드 프로젝트 팀원모집(기획+디자인)','개발자 위주로 모인 사이드 프로젝트에 기획/디자인 팀원을 모집합니다.\n\n[일정] 매주 월요일 저녁 8시 온라인 미팅 예정입니다.\n\n[규칙] 2주 이상 연락두절 시 팀에서 제외될 수 있습니다.',6,'ETC','RECRUITING',false,DATE_SUB(NOW(), INTERVAL 6 DAY),NOW()),
(15,'파이썬 알고리즘 스터디(초급)','파이썬으로 알고리즘 기초를 다지는 초급자 스터디입니다.\n\n[일정] 매주 수요일 저녁 7시 온라인으로 모여 기초 문제를 함께 풉니다.\n\n[출석 규칙] 결석 2회 이상 시 스터디장과 상의가 필요합니다.',8,'ALGORITHM','RECRUITING',false,DATE_SUB(NOW(), INTERVAL 5 DAY),NOW()),
(16,'새벽 영어 회화 스터디','출근 전 새벽 6시 30분, 온라인으로 짧게 영어 회화를 연습하는 스터디입니다.\n\n[일정] 매주 월/수/금 진행 예정입니다.\n\n[출석 규칙] 주 2회 이상 무단 결석 시 스터디에서 제외됩니다.',5,'ENGLISH','RECRUITING',false,DATE_SUB(NOW(), INTERVAL 4 DAY),NOW()),
(17,'컴활1급 실기 스터디','컴퓨터활용능력 1급 실기 시험을 준비하는 스터디입니다.\n\n[일정] 매주 토요일 오후 2시 온라인으로 실습 문제를 함께 풀이합니다.\n\n[과제 규칙] 과제 미제출 2회 시 개별 안내 드립니다.',6,'CERTIFICATE','RECRUITING',false,DATE_SUB(NOW(), INTERVAL 3 DAY),NOW()),
(18,'1인 개발 프로젝트 인증반','각자 진행 중인 1인 개발 프로젝트의 진행 상황을 매주 인증하는 스터디입니다.\n\n[일정] 매주 일요일 저녁 9시 온라인으로 모여 짧게 발표합니다.\n\n[규칙] 2주 연속 미인증 시 스터디에서 제외됩니다.',10,'ETC','RECRUITING',false,DATE_SUB(NOW(), INTERVAL 2 DAY),NOW()),
(19,'코딩테스트 스터디(파이썬, 심화)','파이썬으로 코딩테스트 심화 문제를 푸는 스터디입니다.\n\n[일정] 매주 수요일 밤 10시 온라인으로 진행 예정입니다.\n\n[출석 규칙] 무단 결석 2회 시 다음 기수 참여가 제한됩니다.',6,'ALGORITHM','RECRUITING',false,DATE_SUB(NOW(), INTERVAL 1 DAY),NOW()),
(20,'번역 스터디','원서 번역을 함께 연습하던 스터디였으나 참여 인원 부족으로 스터디장이 스터디를 삭제했습니다.\n(목록 미노출 테스트용 더미 데이터)',5,'ETC','RECRUITING',true,DATE_SUB(NOW(), INTERVAL 20 DAY),NOW());

-- =========================================================
-- 참여자 52건 (리더/멤버) - 매 스터디마다 참여자 수가 capacity를 넘지 않도록 구성
-- =========================================================
INSERT INTO participants (id, study_id, member_id, role, created_at, updated_at) VALUES
-- 스터디1 (6/6, 마감)
(1,1,1,'LEADER',NOW(),NOW()),
(2,1,3,'MEMBER',NOW(),NOW()),
(3,1,4,'MEMBER',NOW(),NOW()),
(4,1,5,'MEMBER',NOW(),NOW()),
(5,1,6,'MEMBER',NOW(),NOW()),
(6,1,7,'MEMBER',NOW(),NOW()),
-- 스터디2 (5/5, 마감)
(7,2,2,'LEADER',NOW(),NOW()),
(8,2,1,'MEMBER',NOW(),NOW()),
(9,2,8,'MEMBER',NOW(),NOW()),
(10,2,9,'MEMBER',NOW(),NOW()),
(11,2,10,'MEMBER',NOW(),NOW()),
-- 스터디3 (5/8, 모집중)
(12,3,11,'LEADER',NOW(),NOW()),
(13,3,1,'MEMBER',NOW(),NOW()),
(14,3,2,'MEMBER',NOW(),NOW()),
(15,3,12,'MEMBER',NOW(),NOW()),
(16,3,13,'MEMBER',NOW(),NOW()),
-- 스터디4 (5/5, 종료)
(17,4,14,'LEADER',NOW(),NOW()),
(18,4,15,'MEMBER',NOW(),NOW()),
(19,4,16,'MEMBER',NOW(),NOW()),
(20,4,17,'MEMBER',NOW(),NOW()),
(21,4,18,'MEMBER',NOW(),NOW()),
-- 스터디5 (4/7, 모집중)
(22,5,1,'LEADER',NOW(),NOW()),
(23,5,19,'MEMBER',NOW(),NOW()),
(24,5,20,'MEMBER',NOW(),NOW()),
(25,5,21,'MEMBER',NOW(),NOW()),
-- 스터디6 (6/6, 마감)
(26,6,22,'LEADER',NOW(),NOW()),
(27,6,2,'MEMBER',NOW(),NOW()),
(28,6,23,'MEMBER',NOW(),NOW()),
(29,6,24,'MEMBER',NOW(),NOW()),
(30,6,25,'MEMBER',NOW(),NOW()),
(31,6,26,'MEMBER',NOW(),NOW()),
-- 스터디7~20 (막 개설되었거나 소프트 삭제된 스터디)
(32,7,27,'LEADER',NOW(),NOW()),
(33,7,28,'MEMBER',NOW(),NOW()),
(34,8,29,'LEADER',NOW(),NOW()),
(35,9,30,'LEADER',NOW(),NOW()),
(36,9,31,'MEMBER',NOW(),NOW()),
(37,9,32,'MEMBER',NOW(),NOW()),
(38,10,33,'LEADER',NOW(),NOW()),
(39,11,34,'LEADER',NOW(),NOW()),
(40,11,35,'MEMBER',NOW(),NOW()),
(41,12,36,'LEADER',NOW(),NOW()),
(42,13,37,'LEADER',NOW(),NOW()),
(43,13,38,'MEMBER',NOW(),NOW()),
(44,14,40,'LEADER',NOW(),NOW()),
(45,15,6,'LEADER',NOW(),NOW()),
(46,15,9,'MEMBER',NOW(),NOW()),
(47,16,21,'LEADER',NOW(),NOW()),
(48,17,24,'LEADER',NOW(),NOW()),
(49,17,3,'MEMBER',NOW(),NOW()),
(50,18,17,'LEADER',NOW(),NOW()),
(51,19,13,'LEADER',NOW(),NOW()),
(52,20,1,'LEADER',NOW(),NOW());

-- =========================================================
-- 가입 신청 46건
-- 원칙: 현재 참여 중인 멤버(리더 제외)는 전부 APPROVED 이력이 남아있고,
--       그 외에 PENDING/REJECTED 신청 몇 건을 추가로 섞어 실사용처럼 보이게 구성
-- =========================================================
INSERT INTO applications (id, member_id, study_id, content, status, created_at, updated_at) VALUES
-- 스터디1 멤버들의 승인 이력
(1,3,1,'알고리즘 스터디 지원합니다. 매일 1문제씩은 꾸준히 풀고 있어요!','APPROVED',NOW(),NOW()),
(2,4,1,'코딩테스트 준비 중인데 혼자 하니 늘어져서 지원합니다.','APPROVED',NOW(),NOW()),
(3,5,1,'실버 문제 위주로 감 잡고 싶어서 지원합니다.','APPROVED',NOW(),NOW()),
(4,6,1,'스터디원들과 코드 리뷰 해보고 싶어 지원합니다.','APPROVED',NOW(),NOW()),
(5,7,1,'취업 준비 중이라 코테 감각을 유지하고 싶습니다.','APPROVED',NOW(),NOW()),
-- 스터디2 멤버들의 승인 이력
(6,1,2,'토익 900 목표로 같이 공부할 사람을 찾다가 지원합니다.','APPROVED',NOW(),NOW()),
(7,8,2,'LC 파트가 약해서 스터디로 보완하고 싶습니다.','APPROVED',NOW(),NOW()),
(8,9,2,'졸업 요건 때문에 토익 점수가 급합니다.','APPROVED',NOW(),NOW()),
(9,10,2,'매일 단어 암기는 하고 있는데 실전 감각이 부족해서요.','APPROVED',NOW(),NOW()),
-- 스터디3 멤버들의 승인 이력
(10,1,3,'정보처리기사 실기 처음이라 같이 준비하고 싶습니다.','APPROVED',NOW(),NOW()),
(11,2,3,'필기는 붙었고 실기만 남았습니다. 잘 부탁드려요.','APPROVED',NOW(),NOW()),
(12,12,3,'실기 기출 위주로 스터디하고 싶어 지원합니다.','APPROVED',NOW(),NOW()),
(13,13,3,'혼자 준비하다 막혀서 지원하게 됐습니다.','APPROVED',NOW(),NOW()),
-- 스터디4 멤버들의 승인 이력
(14,15,4,'사이드 프로젝트 경험을 쌓고 싶어 지원합니다.','APPROVED',NOW(),NOW()),
(15,16,4,'프론트엔드 담당 가능합니다. 잘 부탁드려요.','APPROVED',NOW(),NOW()),
(16,17,4,'기획 쪽으로 참여하고 싶습니다.','APPROVED',NOW(),NOW()),
(17,18,4,'백엔드 개발 참여 희망합니다.','APPROVED',NOW(),NOW()),
-- 스터디5 멤버들의 승인 이력
(18,19,5,'CS 지식이 부족해서 정리해보고 싶어 지원합니다.','APPROVED',NOW(),NOW()),
(19,20,5,'전공자인데 복습 겸 참여하고 싶습니다.','APPROVED',NOW(),NOW()),
(20,21,5,'네트워크 파트 발표 자신 있습니다!','APPROVED',NOW(),NOW()),
-- 스터디6 멤버들의 승인 이력
(21,2,6,'리트코드 꾸준히 풀고 싶어서 지원합니다.','APPROVED',NOW(),NOW()),
(22,23,6,'해외 취업 준비 중이라 영어 문제풀이도 병행하고 싶습니다.','APPROVED',NOW(),NOW()),
(23,24,6,'Medium 난이도 위주로 실력을 늘리고 싶습니다.','APPROVED',NOW(),NOW()),
(24,25,6,'코드 리뷰 받아보고 싶어 지원합니다.','APPROVED',NOW(),NOW()),
(25,26,6,'꾸준함이 부족해서 스터디 힘을 빌리고 싶습니다.','APPROVED',NOW(),NOW()),
-- 라이트 스터디 멤버들의 승인 이력
(26,28,7,'매일 기록하는 습관을 만들고 싶어 지원합니다.','APPROVED',NOW(),NOW()),
(27,31,9,'SQLD 3주 안에 따고 싶어서 지원합니다.','APPROVED',NOW(),NOW()),
(28,32,9,'실무에 SQL을 써야 해서 급하게 준비 중입니다.','APPROVED',NOW(),NOW()),
(29,35,11,'자바 입문자인데 같이 기초 다지고 싶습니다.','APPROVED',NOW(),NOW()),
(30,38,13,'빅데이터분석기사 필기 준비 중입니다.','APPROVED',NOW(),NOW()),
(31,9,15,'파이썬 알고리즘 처음 시작합니다. 잘 부탁드려요.','APPROVED',NOW(),NOW()),
(32,3,17,'컴활 1급 실기 같이 준비하고 싶어 지원합니다.','APPROVED',NOW(),NOW()),
-- 대기/거절 등 추가 신청 (실제 서비스처럼 상태가 섞여 보이도록)
(33,16,3,'정보처리기사 실기도 같이 준비하고 싶어 지원합니다.','PENDING',NOW(),NOW()),
(34,25,3,'시간대가 맞을지 모르겠지만 지원해봅니다.','REJECTED',NOW(),NOW()),
(35,10,5,'CS 지식 복습하고 싶어서 지원합니다.','PENDING',NOW(),NOW()),
(36,18,5,'운영체제 파트 자신 있습니다.','PENDING',NOW(),NOW()),
(37,6,5,'시간이 맞지 않아 거절되었습니다.','REJECTED',NOW(),NOW()),
(38,5,7,'회고 습관을 만들어보고 싶어 지원합니다.','PENDING',NOW(),NOW()),
(39,7,9,'SQLD 준비 같이 하고 싶어 지원합니다.','PENDING',NOW(),NOW()),
(40,12,9,'일정이 맞지 않아 거절되었습니다.','REJECTED',NOW(),NOW()),
(41,14,11,'자바 코테 기초부터 다시 시작하고 싶습니다.','PENDING',NOW(),NOW()),
(42,20,13,'빅데이터분석기사 같이 준비하고 싶어 지원합니다.','PENDING',NOW(),NOW()),
(43,19,17,'컴활 자격증 준비 중인데 거절되었습니다.','REJECTED',NOW(),NOW()),
(44,2,19,'파이썬 심화 문제도 풀어보고 싶어 지원합니다.','PENDING',NOW(),NOW()),
(45,1,16,'새벽형 인간이라 아침 스터디에 지원합니다.','PENDING',NOW(),NOW()),
(46,1,8,'스피킹 연습하고 싶어 지원했는데 아쉽게 거절되었습니다.','REJECTED',NOW(),NOW());

-- =========================================================
-- 스터디 세션(회차) 33개
-- =========================================================
INSERT INTO study_sessions (id, study_id, session_number, title, content, starts_at, created_at, updated_at) VALUES
-- 스터디1
(1,1,1,'1회차 - OT 및 스터디 규칙 안내','스터디 진행 방식과 규칙을 안내하고 서로 소개하는 시간을 가졌습니다.',@s1_1,NOW(),NOW()),
(2,1,2,'2회차 - 그리디 알고리즘','그리디 알고리즘 문제 풀이 및 코드 리뷰.',@s1_2,NOW(),NOW()),
(3,1,3,'3회차 - 구현/시뮬레이션','구현 유형 문제 풀이 및 코드 리뷰.',@s1_3,NOW(),NOW()),
(4,1,4,'4회차 - 이분탐색','이분탐색 문제 풀이 및 코드 리뷰.',@s1_4,NOW(),NOW()),
(5,1,5,'5회차 - DFS/BFS','DFS/BFS 문제 풀이 및 코드 리뷰.',@s1_5,NOW(),NOW()),
(6,1,6,'6회차 - 다이나믹 프로그래밍','DP 문제 풀이 예정.',@s1_6,NOW(),NOW()),
(7,1,7,'7회차 - 우선순위 큐/힙','힙 자료구조 문제 풀이 예정.',@s1_7,NOW(),NOW()),
(8,1,8,'8회차 - 모의 코딩테스트 및 마무리','전체 마무리 모의고사 진행 예정.',@s1_8,NOW(),NOW()),
-- 스터디2
(9,2,1,'1회차 - OT 및 레벨테스트','현재 실력 확인용 미니 모의고사 진행.',@s2_1,NOW(),NOW()),
(10,2,2,'2회차 - LC 모의고사','LC 모의고사 1회분 진행 및 오답 리뷰.',@s2_2,NOW(),NOW()),
(11,2,3,'3회차 - RC 모의고사','RC 모의고사 1회분 진행 및 오답 리뷰.',@s2_3,NOW(),NOW()),
(12,2,4,'4회차 - 파트별 취약점 보완','개인별 취약 파트 집중 보완.',@s2_4,NOW(),NOW()),
(13,2,5,'5회차 - 통합 모의고사','LC/RC 통합 모의고사 진행 예정.',@s2_5,NOW(),NOW()),
(14,2,6,'6회차 - 최종 점검 및 마무리','실전 감각 최종 점검 예정.',@s2_6,NOW(),NOW()),
-- 스터디3
(15,3,1,'1회차 - OT 및 기출 유형 소개','실기 기출 문제 유형을 소개하고 학습 계획을 세웠습니다.',@s3_1,NOW(),NOW()),
(16,3,2,'2회차 - 기출문제 풀이','기출문제 20문제 풀이 및 해설.',@s3_2,NOW(),NOW()),
(17,3,3,'3회차 - 오답 위주 복습','오답 노트 기반 복습 예정.',@s3_3,NOW(),NOW()),
-- 스터디4
(18,4,1,'1회차 - 기획 킥오프','서비스 아이디어 확정 및 역할 분담.',@s4_1,NOW(),NOW()),
(19,4,2,'2회차 - 와이어프레임 리뷰','초안 와이어프레임 공유 및 피드백.',@s4_2,NOW(),NOW()),
(20,4,3,'3회차 - API 설계 리뷰','API 명세서 공유 및 리뷰.',@s4_3,NOW(),NOW()),
(21,4,4,'4회차 - 중간 개발 점검','개발 진행 상황 공유.',@s4_4,NOW(),NOW()),
(22,4,5,'5회차 - 통합 테스트','프론트-백엔드 통합 테스트 진행.',@s4_5,NOW(),NOW()),
(23,4,6,'6회차 - 최종 데모 및 회고','최종 데모 발표 및 프로젝트 회고.',@s4_6,NOW(),NOW()),
-- 스터디5
(24,5,1,'1회차 - OT 및 커리큘럼 확정','스터디 목표와 발표 순서를 정했습니다.',@s5_1,NOW(),NOW()),
(25,5,2,'2회차 - 운영체제/네트워크 발표','프로세스, TCP/UDP 관련 발표 예정.',@s5_2,NOW(),NOW()),
-- 스터디6
(26,6,1,'1회차 - OT 및 문제풀이 방식 안내','문제풀이 및 리뷰 방식을 정했습니다.',@s6_1,NOW(),NOW()),
(27,6,2,'2회차 - Medium 문제풀이','Medium 난이도 문제 2개 풀이.',@s6_2,NOW(),NOW()),
(28,6,3,'3회차 - Hard 문제 도전','Hard 난이도 1문제 도전.',@s6_3,NOW(),NOW()),
(29,6,4,'4회차 - Medium 문제풀이','Medium 난이도 문제 2개 풀이.',@s6_4,NOW(),NOW()),
(30,6,5,'5회차 - Medium 문제풀이','Medium 난이도 문제 2개 풀이.',@s6_5,NOW(),NOW()),
(31,6,6,'6회차 - Hard 문제 도전','Hard 난이도 1문제 도전.',@s6_6,NOW(),NOW()),
(32,6,7,'7회차 - Medium 문제풀이','Medium 난이도 문제 2개 풀이.',@s6_7,NOW(),NOW()),
(33,6,8,'8회차 - 마무리 및 회고','8주차 마무리 및 회고 예정.',@s6_8,NOW(),NOW());

-- =========================================================
-- 과제 30개 (이미 지난 회차는 마감일도 지남 -> 제출 데이터 존재 / 다음 회차 과제는 아직 마감 전)
-- =========================================================
INSERT INTO assignments (id, session_id, title, description, due_at, created_at, updated_at) VALUES
-- 스터디1
(1,1,'1주차 과제 - 배열/문자열 3문제','프로그래머스 Lv1~2 배열/문자열 문제 3개를 풀고 코드와 풀이 링크를 제출해주세요.',ADDTIME(DATE_ADD(DATE(@s1_1), INTERVAL 4 DAY),'23:59:59'),NOW(),NOW()),
(2,2,'2주차 과제 - 그리디 3문제','그리디 유형 문제 3개를 풀고 풀이 링크를 제출해주세요.',ADDTIME(DATE_ADD(DATE(@s1_2), INTERVAL 4 DAY),'23:59:59'),NOW(),NOW()),
(3,3,'3주차 과제 - 구현 3문제','구현 유형 문제 3개를 풀고 풀이 링크를 제출해주세요.',ADDTIME(DATE_ADD(DATE(@s1_3), INTERVAL 4 DAY),'23:59:59'),NOW(),NOW()),
(4,4,'4주차 과제 - 이분탐색 3문제','이분탐색 유형 문제 3개를 풀고 풀이 링크를 제출해주세요.',ADDTIME(DATE_ADD(DATE(@s1_4), INTERVAL 4 DAY),'23:59:59'),NOW(),NOW()),
(5,5,'5주차 과제 - DFS/BFS 3문제','DFS/BFS 유형 문제 3개를 풀고 풀이 링크를 제출해주세요.',ADDTIME(DATE_ADD(DATE(@s1_5), INTERVAL 4 DAY),'23:59:59'),NOW(),NOW()),
(6,6,'6주차 과제 - 다이나믹 프로그래밍 3문제','DP 유형 문제 3개를 풀고 풀이 링크를 제출해주세요.',ADDTIME(DATE_ADD(DATE(@s1_6), INTERVAL -1 DAY),'23:59:59'),NOW(),NOW()),
-- 스터디2
(7,9,'1주차 과제 - LC 모의고사 1회분','LC 모의고사 1회분을 풀고 채점 결과를 제출해주세요.',ADDTIME(DATE_ADD(DATE(@s2_1), INTERVAL 4 DAY),'23:59:59'),NOW(),NOW()),
(8,10,'2주차 과제 - RC 모의고사 1회분','RC 모의고사 1회분을 풀고 채점 결과를 제출해주세요.',ADDTIME(DATE_ADD(DATE(@s2_2), INTERVAL 4 DAY),'23:59:59'),NOW(),NOW()),
(9,11,'3주차 과제 - 오답노트 정리','이번 주 틀린 문제 오답노트를 정리해서 제출해주세요.',ADDTIME(DATE_ADD(DATE(@s2_3), INTERVAL 4 DAY),'23:59:59'),NOW(),NOW()),
(10,12,'4주차 과제 - 단어 100개 암기 테스트','단어 100개 암기 테스트 결과를 제출해주세요.',ADDTIME(DATE_ADD(DATE(@s2_4), INTERVAL 4 DAY),'23:59:59'),NOW(),NOW()),
(11,13,'5주차 과제 - LC/RC 통합 모의고사','통합 모의고사를 풀고 채점 결과를 제출해주세요.',ADDTIME(DATE_ADD(DATE(@s2_5), INTERVAL -1 DAY),'23:59:59'),NOW(),NOW()),
-- 스터디3
(12,15,'1주차 과제 - 기출문제 20제','실기 기출문제 20문제를 풀고 답안을 제출해주세요.',ADDTIME(DATE_ADD(DATE(@s3_1), INTERVAL 4 DAY),'23:59:59'),NOW(),NOW()),
(13,16,'2주차 과제 - 기출문제 20제(오답 위주)','지난 회차 오답 위주로 기출문제 20문제를 풀고 제출해주세요.',ADDTIME(DATE_ADD(DATE(@s3_2), INTERVAL 4 DAY),'23:59:59'),NOW(),NOW()),
(14,17,'3주차 과제 - 실기 기출 모의고사','실기 기출 모의고사 1회분을 풀고 제출해주세요.',ADDTIME(DATE_ADD(DATE(@s3_3), INTERVAL -1 DAY),'23:59:59'),NOW(),NOW()),
-- 스터디4
(15,18,'1주차 과제 - 기획서 초안 제출','서비스 기획서 초안을 작성해서 제출해주세요.',ADDTIME(DATE_ADD(DATE(@s4_1), INTERVAL 4 DAY),'23:59:59'),NOW(),NOW()),
(16,19,'2주차 과제 - 와이어프레임 제출','주요 화면 와이어프레임을 제출해주세요.',ADDTIME(DATE_ADD(DATE(@s4_2), INTERVAL 4 DAY),'23:59:59'),NOW(),NOW()),
(17,20,'3주차 과제 - API 명세서 초안','담당 파트 API 명세서 초안을 제출해주세요.',ADDTIME(DATE_ADD(DATE(@s4_3), INTERVAL 4 DAY),'23:59:59'),NOW(),NOW()),
(18,21,'4주차 과제 - 개발 진행상황 공유','이번 주 개발 진행상황을 정리해서 제출해주세요.',ADDTIME(DATE_ADD(DATE(@s4_4), INTERVAL 4 DAY),'23:59:59'),NOW(),NOW()),
(19,22,'5주차 과제 - 통합 테스트 결과 공유','통합 테스트 결과와 발견된 이슈를 제출해주세요.',ADDTIME(DATE_ADD(DATE(@s4_5), INTERVAL 4 DAY),'23:59:59'),NOW(),NOW()),
(20,23,'6주차 과제 - 최종 발표자료 제출','최종 데모 발표자료를 제출해주세요.',ADDTIME(DATE_ADD(DATE(@s4_6), INTERVAL 4 DAY),'23:59:59'),NOW(),NOW()),
-- 스터디5
(21,24,'1주차 과제 - 운영체제 프로세스/스레드 정리','프로세스와 스레드 개념을 정리해서 제출해주세요.',ADDTIME(DATE_ADD(DATE(@s5_1), INTERVAL 4 DAY),'23:59:59'),NOW(),NOW()),
(22,25,'2주차 과제 - 네트워크 TCP/UDP 정리','TCP/UDP 개념을 정리해서 제출해주세요.',ADDTIME(DATE_ADD(DATE(@s5_2), INTERVAL -1 DAY),'23:59:59'),NOW(),NOW()),
-- 스터디6
(23,26,'1주차 과제 - Medium 2문제','Medium 난이도 문제 2개를 풀고 제출해주세요.',ADDTIME(DATE_ADD(DATE(@s6_1), INTERVAL 4 DAY),'23:59:59'),NOW(),NOW()),
(24,27,'2주차 과제 - Medium 2문제','Medium 난이도 문제 2개를 풀고 제출해주세요.',ADDTIME(DATE_ADD(DATE(@s6_2), INTERVAL 4 DAY),'23:59:59'),NOW(),NOW()),
(25,28,'3주차 과제 - Hard 1문제 + Medium 1문제','Hard 1문제, Medium 1문제를 풀고 제출해주세요.',ADDTIME(DATE_ADD(DATE(@s6_3), INTERVAL 4 DAY),'23:59:59'),NOW(),NOW()),
(26,29,'4주차 과제 - Medium 2문제','Medium 난이도 문제 2개를 풀고 제출해주세요.',ADDTIME(DATE_ADD(DATE(@s6_4), INTERVAL 4 DAY),'23:59:59'),NOW(),NOW()),
(27,30,'5주차 과제 - Medium 2문제','Medium 난이도 문제 2개를 풀고 제출해주세요.',ADDTIME(DATE_ADD(DATE(@s6_5), INTERVAL 4 DAY),'23:59:59'),NOW(),NOW()),
(28,31,'6주차 과제 - Hard 1문제 + Medium 1문제','Hard 1문제, Medium 1문제를 풀고 제출해주세요.',ADDTIME(DATE_ADD(DATE(@s6_6), INTERVAL 4 DAY),'23:59:59'),NOW(),NOW()),
(29,32,'7주차 과제 - Medium 2문제','Medium 난이도 문제 2개를 풀고 제출해주세요.',ADDTIME(DATE_ADD(DATE(@s6_7), INTERVAL 4 DAY),'23:59:59'),NOW(),NOW()),
(30,33,'8주차 과제 - Medium 2문제','Medium 난이도 문제 2개를 풀고 제출해주세요.',ADDTIME(DATE_ADD(DATE(@s6_8), INTERVAL -1 DAY),'23:59:59'),NOW(),NOW());

-- =========================================================
-- 출석 136건 (이미 지난 회차만 기록, 앞으로 있을 회차는 아직 UNMARKED라 레코드 자체가 없음)
-- =========================================================
INSERT INTO attendances (id, session_id, member_id, status, created_at, updated_at) VALUES
-- 스터디1 (1~6,3,4,5,7 참여자: 1,3,4,5,6,7) / 세션 1~5
(1,1,1,'PRESENT',NOW(),NOW()),(2,1,3,'PRESENT',NOW(),NOW()),(3,1,4,'PRESENT',NOW(),NOW()),(4,1,5,'PRESENT',NOW(),NOW()),(5,1,6,'PRESENT',NOW(),NOW()),(6,1,7,'PRESENT',NOW(),NOW()),
(7,2,1,'PRESENT',NOW(),NOW()),(8,2,3,'PRESENT',NOW(),NOW()),(9,2,4,'PRESENT',NOW(),NOW()),(10,2,5,'ABSENT',NOW(),NOW()),(11,2,6,'PRESENT',NOW(),NOW()),(12,2,7,'PRESENT',NOW(),NOW()),
(13,3,1,'PRESENT',NOW(),NOW()),(14,3,3,'PRESENT',NOW(),NOW()),(15,3,4,'ABSENT',NOW(),NOW()),(16,3,5,'PRESENT',NOW(),NOW()),(17,3,6,'PRESENT',NOW(),NOW()),(18,3,7,'PRESENT',NOW(),NOW()),
(19,4,1,'PRESENT',NOW(),NOW()),(20,4,3,'PRESENT',NOW(),NOW()),(21,4,4,'PRESENT',NOW(),NOW()),(22,4,5,'PRESENT',NOW(),NOW()),(23,4,6,'PRESENT',NOW(),NOW()),(24,4,7,'ABSENT',NOW(),NOW()),
(25,5,1,'PRESENT',NOW(),NOW()),(26,5,3,'PRESENT',NOW(),NOW()),(27,5,4,'PRESENT',NOW(),NOW()),(28,5,5,'PRESENT',NOW(),NOW()),(29,5,6,'PRESENT',NOW(),NOW()),(30,5,7,'ABSENT',NOW(),NOW()),
-- 스터디2 (참여자: 2,1,8,9,10) / 세션 9~12
(31,9,2,'PRESENT',NOW(),NOW()),(32,9,1,'PRESENT',NOW(),NOW()),(33,9,8,'PRESENT',NOW(),NOW()),(34,9,9,'PRESENT',NOW(),NOW()),(35,9,10,'PRESENT',NOW(),NOW()),
(36,10,2,'PRESENT',NOW(),NOW()),(37,10,1,'PRESENT',NOW(),NOW()),(38,10,8,'ABSENT',NOW(),NOW()),(39,10,9,'PRESENT',NOW(),NOW()),(40,10,10,'PRESENT',NOW(),NOW()),
(41,11,2,'PRESENT',NOW(),NOW()),(42,11,1,'PRESENT',NOW(),NOW()),(43,11,8,'PRESENT',NOW(),NOW()),(44,11,9,'PRESENT',NOW(),NOW()),(45,11,10,'ABSENT',NOW(),NOW()),
(46,12,2,'PRESENT',NOW(),NOW()),(47,12,1,'ABSENT',NOW(),NOW()),(48,12,8,'PRESENT',NOW(),NOW()),(49,12,9,'PRESENT',NOW(),NOW()),(50,12,10,'PRESENT',NOW(),NOW()),
-- 스터디3 (참여자: 11,1,2,12,13) / 세션 15~16
(51,15,11,'PRESENT',NOW(),NOW()),(52,15,1,'PRESENT',NOW(),NOW()),(53,15,2,'PRESENT',NOW(),NOW()),(54,15,12,'PRESENT',NOW(),NOW()),(55,15,13,'PRESENT',NOW(),NOW()),
(56,16,11,'PRESENT',NOW(),NOW()),(57,16,1,'PRESENT',NOW(),NOW()),(58,16,2,'ABSENT',NOW(),NOW()),(59,16,12,'PRESENT',NOW(),NOW()),(60,16,13,'PRESENT',NOW(),NOW()),
-- 스터디4 (참여자: 14,15,16,17,18) / 세션 18~23
(61,18,14,'PRESENT',NOW(),NOW()),(62,18,15,'PRESENT',NOW(),NOW()),(63,18,16,'PRESENT',NOW(),NOW()),(64,18,17,'PRESENT',NOW(),NOW()),(65,18,18,'PRESENT',NOW(),NOW()),
(66,19,14,'PRESENT',NOW(),NOW()),(67,19,15,'PRESENT',NOW(),NOW()),(68,19,16,'PRESENT',NOW(),NOW()),(69,19,17,'PRESENT',NOW(),NOW()),(70,19,18,'PRESENT',NOW(),NOW()),
(71,20,14,'PRESENT',NOW(),NOW()),(72,20,15,'PRESENT',NOW(),NOW()),(73,20,16,'PRESENT',NOW(),NOW()),(74,20,17,'PRESENT',NOW(),NOW()),(75,20,18,'ABSENT',NOW(),NOW()),
(76,21,14,'PRESENT',NOW(),NOW()),(77,21,15,'PRESENT',NOW(),NOW()),(78,21,16,'ABSENT',NOW(),NOW()),(79,21,17,'PRESENT',NOW(),NOW()),(80,21,18,'ABSENT',NOW(),NOW()),
(81,22,14,'PRESENT',NOW(),NOW()),(82,22,15,'PRESENT',NOW(),NOW()),(83,22,16,'PRESENT',NOW(),NOW()),(84,22,17,'PRESENT',NOW(),NOW()),(85,22,18,'ABSENT',NOW(),NOW()),
(86,23,14,'PRESENT',NOW(),NOW()),(87,23,15,'PRESENT',NOW(),NOW()),(88,23,16,'PRESENT',NOW(),NOW()),(89,23,17,'PRESENT',NOW(),NOW()),(90,23,18,'ABSENT',NOW(),NOW()),
-- 스터디5 (참여자: 1,19,20,21) / 세션 24
(91,24,1,'PRESENT',NOW(),NOW()),(92,24,19,'PRESENT',NOW(),NOW()),(93,24,20,'PRESENT',NOW(),NOW()),(94,24,21,'ABSENT',NOW(),NOW()),
-- 스터디6 (참여자: 22,2,23,24,25,26) / 세션 26~32
(95,26,22,'PRESENT',NOW(),NOW()),(96,26,2,'PRESENT',NOW(),NOW()),(97,26,23,'PRESENT',NOW(),NOW()),(98,26,24,'PRESENT',NOW(),NOW()),(99,26,25,'PRESENT',NOW(),NOW()),(100,26,26,'PRESENT',NOW(),NOW()),
(101,27,22,'PRESENT',NOW(),NOW()),(102,27,2,'PRESENT',NOW(),NOW()),(103,27,23,'PRESENT',NOW(),NOW()),(104,27,24,'PRESENT',NOW(),NOW()),(105,27,25,'PRESENT',NOW(),NOW()),(106,27,26,'PRESENT',NOW(),NOW()),
(107,28,22,'PRESENT',NOW(),NOW()),(108,28,2,'PRESENT',NOW(),NOW()),(109,28,23,'PRESENT',NOW(),NOW()),(110,28,24,'ABSENT',NOW(),NOW()),(111,28,25,'PRESENT',NOW(),NOW()),(112,28,26,'PRESENT',NOW(),NOW()),
(113,29,22,'PRESENT',NOW(),NOW()),(114,29,2,'ABSENT',NOW(),NOW()),(115,29,23,'PRESENT',NOW(),NOW()),(116,29,24,'PRESENT',NOW(),NOW()),(117,29,25,'PRESENT',NOW(),NOW()),(118,29,26,'PRESENT',NOW(),NOW()),
(119,30,22,'PRESENT',NOW(),NOW()),(120,30,2,'PRESENT',NOW(),NOW()),(121,30,23,'PRESENT',NOW(),NOW()),(122,30,24,'PRESENT',NOW(),NOW()),(123,30,25,'PRESENT',NOW(),NOW()),(124,30,26,'ABSENT',NOW(),NOW()),
(125,31,22,'PRESENT',NOW(),NOW()),(126,31,2,'PRESENT',NOW(),NOW()),(127,31,23,'PRESENT',NOW(),NOW()),(128,31,24,'PRESENT',NOW(),NOW()),(129,31,25,'PRESENT',NOW(),NOW()),(130,31,26,'ABSENT',NOW(),NOW()),
(131,32,22,'PRESENT',NOW(),NOW()),(132,32,2,'PRESENT',NOW(),NOW()),(133,32,23,'PRESENT',NOW(),NOW()),(134,32,24,'PRESENT',NOW(),NOW()),(135,32,25,'PRESENT',NOW(),NOW()),(136,32,26,'PRESENT',NOW(),NOW());

-- =========================================================
-- 과제 제출 123건 (마감이 지난 과제 위주로 제출, 일부는 결석/미제출로 남겨둠)
-- =========================================================
INSERT INTO submissions (id, assignment_id, member_id, content, created_at, updated_at) VALUES
-- 스터디1 과제1~5 (과제6은 아직 마감 전이라 제출 없음)
(1,1,1,'https://github.com/kimdoyun/algo-study/tree/main/week1 제출합니다!',NOW(),NOW()),(2,1,3,'프로그래머스 3문제 다 풀었습니다. 링크 남깁니다.',NOW(),NOW()),(3,1,4,'배열 문제는 익숙한데 문자열 파트가 헷갈리네요. 제출합니다.',NOW(),NOW()),(4,1,5,'제출합니다. 리뷰 부탁드려요!',NOW(),NOW()),(5,1,6,'다 풀긴 했는데 2번 문제 시간복잡도가 애매합니다.',NOW(),NOW()),(6,1,7,'제출합니다.',NOW(),NOW()),
(7,2,1,'그리디 3문제 제출합니다.',NOW(),NOW()),(8,2,3,'2번 문제에서 반례 찾느라 오래 걸렸어요. 제출합니다.',NOW(),NOW()),(9,2,4,'제출합니다!',NOW(),NOW()),(10,2,5,'풀이 링크 공유드립니다.',NOW(),NOW()),(11,2,6,'제출합니다. 코드 리뷰 부탁드려요.',NOW(),NOW()),(12,2,7,'제출합니다.',NOW(),NOW()),
(13,3,1,'구현 문제 3개 제출합니다.',NOW(),NOW()),(14,3,3,'시뮬레이션 문제가 좀 어려웠습니다. 제출합니다.',NOW(),NOW()),(15,3,4,'제출합니다.',NOW(),NOW()),(16,3,5,'제출합니다!',NOW(),NOW()),(17,3,6,'제출 완료했습니다.',NOW(),NOW()),(18,3,7,'제출합니다.',NOW(),NOW()),
(19,4,1,'이분탐색 3문제 제출합니다.',NOW(),NOW()),(20,4,3,'파라메트릭 서치까지 응용해봤습니다. 제출합니다.',NOW(),NOW()),(21,4,4,'제출합니다.',NOW(),NOW()),(22,4,5,'제출합니다!',NOW(),NOW()),(23,4,6,'제출 완료했습니다.',NOW(),NOW()),
(24,5,1,'DFS/BFS 3문제 제출합니다.',NOW(),NOW()),(25,5,3,'BFS로 최단거리 구하는 문제 재밌었습니다. 제출합니다.',NOW(),NOW()),(26,5,4,'제출합니다.',NOW(),NOW()),(27,5,5,'제출합니다!',NOW(),NOW()),(28,5,6,'제출 완료했습니다.',NOW(),NOW()),
-- 스터디2 과제7~10 (과제11은 아직 마감 전)
(29,7,2,'LC 모의고사 채점 결과 92점 제출합니다.',NOW(),NOW()),(30,7,1,'LC 85점 나왔습니다. 오답 정리해서 같이 올릴게요.',NOW(),NOW()),(31,7,8,'채점표 캡처해서 제출합니다.',NOW(),NOW()),(32,7,9,'제출합니다.',NOW(),NOW()),(33,7,10,'제출합니다!',NOW(),NOW()),
(34,8,2,'RC 모의고사 결과 제출합니다.',NOW(),NOW()),(35,8,1,'RC 파트3에서 많이 틀렸네요. 제출합니다.',NOW(),NOW()),(36,8,8,'제출합니다.',NOW(),NOW()),(37,8,9,'제출합니다!',NOW(),NOW()),(38,8,10,'채점 결과 제출합니다.',NOW(),NOW()),
(39,9,2,'오답노트 정리해서 제출합니다.',NOW(),NOW()),(40,9,1,'이번 주 오답 12개 정리했습니다.',NOW(),NOW()),(41,9,8,'제출합니다.',NOW(),NOW()),(42,9,9,'제출합니다!',NOW(),NOW()),
(43,10,2,'단어 100개 암기 테스트 98점 제출합니다.',NOW(),NOW()),(44,10,8,'테스트 결과 제출합니다.',NOW(),NOW()),(45,10,9,'제출합니다!',NOW(),NOW()),(46,10,10,'제출합니다.',NOW(),NOW()),
-- 스터디3 과제12~13 (과제14는 아직 마감 전)
(47,12,11,'기출 20문제 답안 제출합니다.',NOW(),NOW()),(48,12,1,'제출합니다. 3, 7, 15번이 어려웠어요.',NOW(),NOW()),(49,12,2,'제출합니다.',NOW(),NOW()),(50,12,12,'제출합니다!',NOW(),NOW()),(51,12,13,'제출 완료했습니다.',NOW(),NOW()),
(52,13,11,'오답 위주로 다시 풀어서 제출합니다.',NOW(),NOW()),(53,13,1,'제출합니다.',NOW(),NOW()),(54,13,12,'제출합니다!',NOW(),NOW()),(55,13,13,'제출 완료했습니다.',NOW(),NOW()),
-- 스터디4 과제15~20
(56,15,14,'기획서 초안 노션 링크 제출합니다.',NOW(),NOW()),(57,15,15,'제출합니다.',NOW(),NOW()),(58,15,16,'제출합니다!',NOW(),NOW()),(59,15,17,'기획서 검토 부탁드립니다.',NOW(),NOW()),(60,15,18,'제출합니다.',NOW(),NOW()),
(61,16,14,'와이어프레임 피그마 링크 제출합니다.',NOW(),NOW()),(62,16,15,'제출합니다.',NOW(),NOW()),(63,16,16,'제출합니다!',NOW(),NOW()),(64,16,17,'제출합니다.',NOW(),NOW()),(65,16,18,'제출합니다.',NOW(),NOW()),
(66,17,14,'API 명세서 초안 제출합니다.',NOW(),NOW()),(67,17,15,'제출합니다.',NOW(),NOW()),(68,17,16,'제출합니다!',NOW(),NOW()),(69,17,17,'제출합니다.',NOW(),NOW()),
(70,18,14,'이번 주 개발 진행상황 공유드립니다.',NOW(),NOW()),(71,18,15,'제출합니다.',NOW(),NOW()),(72,18,17,'제출합니다.',NOW(),NOW()),
(73,19,14,'통합 테스트 결과 공유드립니다. 이슈 3건 발견했습니다.',NOW(),NOW()),(74,19,15,'제출합니다.',NOW(),NOW()),(75,19,16,'제출합니다!',NOW(),NOW()),(76,19,17,'제출합니다.',NOW(),NOW()),
(77,20,14,'최종 발표자료 제출합니다. 다들 고생하셨습니다!',NOW(),NOW()),(78,20,15,'제출합니다.',NOW(),NOW()),(79,20,16,'제출합니다!',NOW(),NOW()),(80,20,17,'제출합니다.',NOW(),NOW()),
-- 스터디5 과제21 (과제22는 아직 마감 전)
(81,21,1,'프로세스/스레드 정리 노션 링크 제출합니다.',NOW(),NOW()),(82,21,19,'제출합니다.',NOW(),NOW()),(83,21,20,'제출합니다!',NOW(),NOW()),
-- 스터디6 과제23~29 (과제30은 아직 마감 전)
(84,23,22,'Medium 2문제 제출합니다.',NOW(),NOW()),(85,23,2,'제출합니다.',NOW(),NOW()),(86,23,23,'제출합니다!',NOW(),NOW()),(87,23,24,'제출합니다.',NOW(),NOW()),(88,23,25,'제출합니다.',NOW(),NOW()),(89,23,26,'제출합니다!',NOW(),NOW()),
(90,24,22,'제출합니다.',NOW(),NOW()),(91,24,2,'제출합니다.',NOW(),NOW()),(92,24,23,'제출합니다!',NOW(),NOW()),(93,24,24,'제출합니다.',NOW(),NOW()),(94,24,25,'제출합니다.',NOW(),NOW()),(95,24,26,'제출합니다!',NOW(),NOW()),
(96,25,22,'Hard 문제 정말 어려웠습니다. 제출합니다.',NOW(),NOW()),(97,25,2,'제출합니다.',NOW(),NOW()),(98,25,23,'제출합니다!',NOW(),NOW()),(99,25,25,'제출합니다.',NOW(),NOW()),(100,25,26,'제출합니다!',NOW(),NOW()),
(101,26,22,'제출합니다.',NOW(),NOW()),(102,26,23,'제출합니다!',NOW(),NOW()),(103,26,24,'제출합니다.',NOW(),NOW()),(104,26,25,'제출합니다.',NOW(),NOW()),(105,26,26,'제출합니다!',NOW(),NOW()),
(106,27,22,'제출합니다.',NOW(),NOW()),(107,27,2,'제출합니다.',NOW(),NOW()),(108,27,23,'제출합니다!',NOW(),NOW()),(109,27,24,'제출합니다.',NOW(),NOW()),(110,27,25,'제출합니다.',NOW(),NOW()),
(111,28,22,'Hard 문제 제출합니다.',NOW(),NOW()),(112,28,2,'제출합니다.',NOW(),NOW()),(113,28,23,'제출합니다!',NOW(),NOW()),(114,28,24,'제출합니다.',NOW(),NOW()),(115,28,25,'제출합니다.',NOW(),NOW()),
(116,29,22,'제출합니다.',NOW(),NOW()),(117,29,2,'제출합니다.',NOW(),NOW()),(118,29,23,'제출합니다!',NOW(),NOW()),(119,29,24,'제출합니다.',NOW(),NOW()),(120,29,25,'제출합니다.',NOW(),NOW()),(121,29,26,'제출합니다!',NOW(),NOW()),
-- 다음 회차 과제 일부는 부지런한 스터디장이 미리 제출
(122,6,1,'다음 주 과제 미리 풀어봤습니다. 제출합니다.',NOW(),NOW()),
(123,30,22,'다음 주 과제 미리 제출합니다.',NOW(),NOW());

-- 다음 회원가입/신규 데이터가 기존 id와 충돌하지 않도록 AUTO_INCREMENT를 맞춰준다.
ALTER TABLE members AUTO_INCREMENT = 1000;
ALTER TABLE studies AUTO_INCREMENT = 1000;
ALTER TABLE participants AUTO_INCREMENT = 1000;
ALTER TABLE applications AUTO_INCREMENT = 1000;
ALTER TABLE study_sessions AUTO_INCREMENT = 1000;
ALTER TABLE assignments AUTO_INCREMENT = 1000;
ALTER TABLE attendances AUTO_INCREMENT = 1000;
ALTER TABLE submissions AUTO_INCREMENT = 1000;
