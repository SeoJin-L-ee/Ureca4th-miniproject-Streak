import type {
  Application,
  Assignment,
  Attendance,
  Member,
  Round,
  Study,
  Submission,
  User,
} from "../types";

export const CURRENT_USER: User = {
  id: "u1",
  name: "김민준",
  email: "minjun@example.com",
  phone: "010-1234-5678",
  avatarColor: "#6C5CE7",
};

export const CURRENT_MEMBER_ID = "m1"; // 김민준 in 알고리즘 스터디 3기

export const studies: Study[] = [
  {
    id: "s1",
    title: "알고리즘 스터디 3기",
    category: "알고리즘",
    tag: "리더",
    description: "코딩테스트 대비 그래프/DP 알고리즘 주 2회 스터디",
    introLong:
      "매주 금요일 저녁, 백준 골드~플래티넘 난이도 문제를 함께 풀고 코드 리뷰를 진행합니다. 발표 후 QnA 시간을 가지며, 매 회차 과제로 복습합니다.",
    schedule: "매주 금 19:00",
    location: "온라인 (Google Meet)",
    capacity: 8,
    creatorId: "u1",
    status: "진행중",
    totalRounds: 12,
    doneRounds: 6,
    createdAt: "2026-05-01",
  },
  {
    id: "s2",
    title: "토익 900+ 스터디",
    category: "어학",
    tag: "참여자",
    description: "토익 900점 목표, 주말 오전 스터디",
    introLong:
      "토익 RC/LC 실전 모의고사를 매주 풀고 오답노트를 공유합니다. 스터디 종료 후 단체 채점 및 해설 진행.",
    schedule: "매주 일 10:00",
    location: "스터디카페 강남점",
    capacity: 12,
    creatorId: "u4",
    status: "진행중",
    totalRounds: 16,
    doneRounds: 9,
    createdAt: "2026-04-10",
  },
  {
    id: "s3",
    title: "사이드 프로젝트 기획반",
    category: "프로젝트",
    tag: "참여자",
    description: "아이디어 발제부터 MVP 출시까지",
    introLong:
      "매주 서로의 사이드 프로젝트 진행상황을 공유하고 피드백을 주고받습니다. 기획/디자인/개발 배경이 섞인 스터디입니다.",
    schedule: "매주 일 20:00",
    location: "온라인 (Discord)",
    capacity: 6,
    creatorId: "u5",
    status: "진행중",
    totalRounds: 20,
    doneRounds: 5,
    createdAt: "2026-06-01",
  },
  {
    id: "s4",
    title: "정보처리기사 스터디",
    category: "자격증",
    tag: "참여자",
    description: "정보처리기사 필기+실기 대비",
    introLong: "기출문제 회독과 실기 예제 풀이를 함께 진행했던 스터디입니다.",
    schedule: "매주 수 19:00",
    location: "온라인 (Zoom)",
    capacity: 10,
    creatorId: "u6",
    status: "종료",
    totalRounds: 10,
    doneRounds: 10,
    createdAt: "2026-02-01",
  },
];

// 탐색(가입 전) 전용 스터디
export const exploreStudies: Study[] = [
  {
    id: "s5",
    title: "React 딥다이브 스터디",
    category: "웹개발",
    tag: "모집중",
    description: "React 18 최신 기능과 렌더링 원리를 파헤칩니다",
    introLong:
      "React 공식 문서와 소스코드를 함께 읽고, 매주 발표를 통해 깊이 있는 이해를 목표로 합니다. 프론트엔드 실무 경력 무관 환영.",
    schedule: "매주 수 21:00",
    location: "온라인 (Google Meet)",
    capacity: 8,
    creatorId: "u7",
    status: "모집중",
    totalRounds: 10,
    doneRounds: 0,
    createdAt: "2026-07-15",
  },
  {
    id: "s6",
    title: "매일 아침 러닝 크루",
    category: "루틴",
    tag: "모집중",
    description: "매일 아침 6시, 인증샷으로 시작하는 하루",
    introLong:
      "느슨하지만 꾸준하게. 매일 아침 러닝 인증을 남기고 주 1회 오프라인 러닝 모임을 갖습니다.",
    schedule: "매일 06:00 (인증)",
    location: "온라인 (인증) + 주 1회 오프라인",
    capacity: 20,
    creatorId: "u8",
    status: "모집중",
    totalRounds: 30,
    doneRounds: 0,
    createdAt: "2026-07-10",
  },
  {
    id: "s7",
    title: "CS 기초 다지기 스터디",
    category: "CS",
    tag: "모집중",
    description: "운영체제/네트워크/데이터베이스 핵심 개념 정리",
    introLong:
      "기술 면접 대비를 목표로 CS 핵심 개념을 매주 한 파트씩 정리하고 서로 질문합니다.",
    schedule: "매주 토 14:00",
    location: "온라인 (Google Meet)",
    capacity: 8,
    creatorId: "u9",
    status: "모집중",
    totalRounds: 12,
    doneRounds: 0,
    createdAt: "2026-07-18",
  },
];

export const members: Member[] = [
  // s1 알고리즘 스터디 3기
  { id: "m1", studyId: "s1", userId: "u1", name: "김민준", role: "leader", joinedAt: "2026-05-01", attendanceRate: 100, submissionRate: 100 },
  { id: "m2", studyId: "s1", userId: "u2", name: "박다은", role: "member", joinedAt: "2026-05-01", attendanceRate: 100, submissionRate: 83 },
  { id: "m3", studyId: "s1", userId: "u3", name: "최준", role: "member", joinedAt: "2026-05-01", attendanceRate: 67, submissionRate: 50, atRisk: true },
  { id: "m4", studyId: "s1", userId: "u10", name: "이서연", role: "member", joinedAt: "2026-05-01", attendanceRate: 100, submissionRate: 100 },
  { id: "m5", studyId: "s1", userId: "u11", name: "오하준", role: "member", joinedAt: "2026-05-08", attendanceRate: 83, submissionRate: 67 },
  { id: "m6", studyId: "s1", userId: "u12", name: "임채원", role: "member", joinedAt: "2026-05-08", attendanceRate: 100, submissionRate: 83 },
  { id: "m7", studyId: "s1", userId: "u13", name: "정도윤", role: "member", joinedAt: "2026-05-08", attendanceRate: 50, submissionRate: 33, atRisk: true },

  // s2 토익 900+ 스터디
  { id: "m8", studyId: "s2", userId: "u4", name: "한지민", role: "leader", joinedAt: "2026-04-10", attendanceRate: 100, submissionRate: 100 },
  { id: "m9", studyId: "s2", userId: "u1", name: "김민준", role: "member", joinedAt: "2026-04-12", attendanceRate: 100, submissionRate: 89 },

  // s3 사이드 프로젝트 기획반
  { id: "m10", studyId: "s3", userId: "u5", name: "서지호", role: "leader", joinedAt: "2026-06-01", attendanceRate: 100, submissionRate: 100 },
  { id: "m11", studyId: "s3", userId: "u1", name: "김민준", role: "member", joinedAt: "2026-06-01", attendanceRate: 75, submissionRate: 75 },

  // s4 정보처리기사 스터디 (종료)
  { id: "m12", studyId: "s4", userId: "u6", name: "윤소율", role: "leader", joinedAt: "2026-02-01", attendanceRate: 100, submissionRate: 100 },
  { id: "m13", studyId: "s4", userId: "u1", name: "김민준", role: "member", joinedAt: "2026-02-01", attendanceRate: 100, submissionRate: 100 },
];

function toLocalDateStr(date: Date) {
  const y = date.getFullYear();
  const m = String(date.getMonth() + 1).padStart(2, "0");
  const d = String(date.getDate()).padStart(2, "0");
  return `${y}-${m}-${d}`;
}

const ROUND_TOPICS: Record<string, string[]> = {
  s1: [
    "OT / 그래프 탐색 알고리즘",
    "BFS / DFS 심화",
    "최단 경로 (다익스트라)",
    "유니온 파인드",
    "최소 신장 트리",
    "위상 정렬",
    "DP 기초",
    "DP 심화",
    "이분 탐색",
    "그리디 알고리즘",
    "백트래킹",
    "총정리 / 모의 코딩테스트",
  ],
  s2: [
    "OT / 레벨테스트",
    "Part5 문법 특강",
    "실전 모의고사 1회",
    "오답노트 리뷰",
    "실전 모의고사 2회",
    "Part7 독해 전략",
    "실전 모의고사 3회",
    "오답노트 리뷰",
    "실전 모의고사 4회",
    "Part3&4 청해 특강",
    "실전 모의고사 5회",
    "오답노트 리뷰",
    "실전 모의고사 6회",
    "최종 점검",
    "실전 모의고사 7회",
    "총정리",
  ],
  s3: [
    "OT / 아이디어 발제",
    "린캔버스 작성",
    "타깃 유저 인터뷰",
    "와이어프레임 리뷰",
    "MVP 기능 정의",
    "디자인 시안 공유",
    "개발 착수 킥오프",
    "스프린트 1 회고",
    "스프린트 2 회고",
    "중간 데모 데이",
    "스프린트 3 회고",
    "스프린트 4 회고",
    "베타 테스트 준비",
    "베타 피드백 리뷰",
    "런칭 준비",
    "런칭 회고",
    "성장 지표 점검",
    "다음 스프린트 기획",
    "회고 및 피드백",
    "최종 발표",
  ],
  s4: [
    "OT / 과목 개요",
    "소프트웨어 설계",
    "소프트웨어 개발",
    "데이터베이스 구축",
    "프로그래밍 언어 활용",
    "정보시스템 구축관리",
    "필기 모의고사",
    "실기 - 알고리즘",
    "실기 - SQL",
    "실기 총정리",
  ],
};

// anchorDateStr = 날짜 기준점이 되는 회차(done < total이면 다음 예정 회차, done === total이면 마지막 회차)의 날짜
function generateRounds(
  studyId: string,
  total: number,
  done: number,
  anchorDateStr: string,
  time: string,
  location: string
): Round[] {
  const anchor = new Date(`${anchorDateStr}T00:00:00`);
  const anchorIndex = done < total ? done + 1 : total; // 1-indexed
  const topics = ROUND_TOPICS[studyId] ?? [];
  return Array.from({ length: total }, (_, i) => {
    const index = i + 1;
    const d = new Date(anchor);
    d.setDate(anchor.getDate() + (index - anchorIndex) * 7);
    const dateStr = toLocalDateStr(d);
    return {
      id: `${studyId}-r${index}`,
      studyId,
      index,
      title: topics[index - 1] ?? `${index}회차 정기 모임`,
      date: dateStr,
      time,
      location,
      status: index <= done ? "완료" : "예정",
      attendanceRate: index <= done ? [86, 92, 78, 100, 92, 86][(index - 1) % 6] : undefined,
    } as Round;
  });
}

export const rounds: Round[] = [
  ...generateRounds("s1", 12, 6, "2026-07-24", "19:00", "온라인 (Google Meet)"), // 다음 회차: 오늘(금)
  ...generateRounds("s2", 16, 9, "2026-07-26", "10:00", "스터디카페 강남점"), // 다음 회차: 일요일
  ...generateRounds("s3", 20, 5, "2026-08-02", "20:00", "온라인 (Discord)"), // 다음 회차: 다음주 일요일
  ...generateRounds("s4", 10, 10, "2026-04-22", "19:00", "온라인 (Zoom)"), // 종료된 스터디, 마지막 회차 기준
];

const s1MemberIds = members.filter((m) => m.studyId === "s1").map((m) => m.id);

export const attendances: Attendance[] = rounds
  .filter((r) => r.studyId === "s1" && r.status === "완료")
  .flatMap((r) =>
    s1MemberIds.map((memberId, idx) => {
      const patterns: Record<string, ("출석" | "지각" | "결석")[]> = {
        m1: ["출석", "출석", "출석", "출석", "출석", "출석"],
        m2: ["출석", "출석", "출석", "출석", "출석", "출석"],
        m3: ["출석", "결석", "출석", "지각", "결석", "출석"],
        m4: ["출석", "출석", "출석", "출석", "출석", "출석"],
        m5: ["출석", "출석", "지각", "출석", "결석", "출석"],
        m6: ["출석", "출석", "출석", "출석", "출석", "출석"],
        m7: ["결석", "출석", "결석", "지각", "결석", "출석"],
      };
      const memberKey = Object.keys(patterns)[idx] ?? "m1";
      return {
        id: `att-${r.id}-${memberId}`,
        roundId: r.id,
        memberId,
        status: patterns[memberKey][r.index - 1] ?? "출석",
      };
    })
  );

export const assignments: Assignment[] = [
  {
    id: "a1",
    roundId: "s1-r5",
    studyId: "s1",
    title: "최소 신장 트리 구현",
    description: "크루스칼/프림 알고리즘을 각각 구현하고, 시간복잡도를 비교 분석해 제출하세요.",
    deadline: "2026-07-13T23:59:00",
    createdAt: "2026-07-06",
  },
  {
    id: "a2",
    roundId: "s1-r6",
    studyId: "s1",
    title: "위상 정렬 실습 보고서",
    description: "위상 정렬 알고리즘의 개념을 정리하고, 이를 활용한 백준 문제 1개를 풀어 제출하세요.",
    deadline: "2026-07-27T23:59:00",
    createdAt: "2026-07-17",
  },
  {
    id: "a3",
    roundId: "s1-r7",
    studyId: "s1",
    title: "DP 기초 문제풀이",
    description: "다이나믹 프로그래밍 기초 문제 3개를 풀고 풀이 과정을 정리해 제출하세요.",
    deadline: "2026-07-30T23:59:00",
    createdAt: "2026-07-21",
  },
  {
    id: "a4",
    roundId: "s2-r8",
    studyId: "s2",
    title: "토익 900+ 오답노트",
    description: "이번 주 모의고사 오답노트를 정리해 제출하세요.",
    deadline: "2026-07-26T19:00:00",
    createdAt: "2026-07-20",
  },
];

export const submissions: Submission[] = [
  { id: "sub1", assignmentId: "a1", memberId: "m1", content: "https://github.com/minjun/algo/mst", submittedAt: "2026-07-12T20:00:00", status: "제출완료" },
  { id: "sub2", assignmentId: "a1", memberId: "m2", content: "https://github.com/daeun/mst", submittedAt: "2026-07-13T10:00:00", status: "제출완료" },
  { id: "sub3", assignmentId: "a1", memberId: "m3", content: "", submittedAt: null, status: "미제출" },
  { id: "sub4", assignmentId: "a1", memberId: "m4", content: "https://github.com/seoyeon/mst", submittedAt: "2026-07-11T18:00:00", status: "제출완료" },
  { id: "sub5", assignmentId: "a2", memberId: "m1", content: "", submittedAt: null, status: "미제출" },
  { id: "sub6", assignmentId: "a2", memberId: "m2", content: "https://github.com/daeun/bfs-dfs", submittedAt: "2026-07-23T11:00:00", status: "제출완료" },
];

export const applications: Application[] = [
  {
    id: "ap1",
    studyId: "s1",
    applicantName: "정수빈",
    message: "알고리즘 스터디 처음이지만 열심히 참여하겠습니다! 백준 실버 3 정도 풀고 있어요.",
    appliedAt: "2026-07-20",
    status: "대기",
  },
  {
    id: "ap2",
    studyId: "s1",
    applicantName: "이하늘",
    message: "코딩테스트 준비 중입니다. 그래프 알고리즘이 약해서 함께 공부하고 싶어요.",
    appliedAt: "2026-07-21",
    status: "대기",
  },
  {
    id: "ap3",
    studyId: "s1",
    applicantName: "강도현",
    message: "매주 참석 가능하며, DP 문제를 집중적으로 풀고 싶습니다.",
    appliedAt: "2026-07-10",
    status: "승인",
  },
  {
    id: "ap4",
    studyId: "s1",
    applicantName: "배유진",
    message: "시간대가 맞지 않을 수도 있지만 지원해봅니다.",
    appliedAt: "2026-07-08",
    status: "거절",
  },
];

// 마이페이지 - 나의 지원 현황 (내가 지원자인 경우)
export const myApplications: Application[] = [
  { id: "mya1", studyId: "s5", applicantName: "김민준", message: "알고리즘 다음엔 프론트엔드도 깊게 파보고 싶어 지원합니다.", appliedAt: "2026-07-22", status: "대기" },
  { id: "mya2", studyId: "s2", applicantName: "김민준", message: "토익 900+ 목표로 지원합니다.", appliedAt: "2026-04-11", status: "승인" },
  { id: "mya3", studyId: "s7", applicantName: "김민준", message: "CS 기초를 다지고 싶어 지원합니다.", appliedAt: "2026-07-05", status: "거절" },
];
