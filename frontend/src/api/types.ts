// 백엔드 DTO/enum과 1:1로 대응하는 타입 정의 (com.example.* 패키지 기준)

export type StudyCategory = "ALGORITHM" | "ENGLISH" | "CERTIFICATE" | "ETC";
export type StudyStatus = "RECRUITING" | "CLOSED" | "ENDED";
export type StudyRole = "LEADER" | "MEMBER";
export type AttendanceStatus = "UNMARKED" | "PRESENT" | "ABSENT";
export type ApplicationStatus = "PENDING" | "APPROVED" | "REJECTED";
export type MyStudyApplyStatus = "NONE" | "PENDING" | "ACCEPTED" | "REJECTED";

export interface AuthResDto {
  memberId: number;
  email: string;
  name: string;
  phone: string | null;
}

export interface CsrfResDto {
  token: string;
  headerName: string;
}

export interface MemberResDto {
  memberId: number;
  email: string;
  name: string;
  phone: string | null;
}

export interface UpdateMemberResDto {
  member: MemberResDto;
  reLoginRequired: boolean;
}

// ---- study ----
export interface StudyInfoResDto {
  title: string;
  description: string;
  capacity: number;
  category: StudyCategory;
  studyStatus: StudyStatus;
}

export interface StudySummaryResDto {
  studyId: number;
  title: string;
  category: StudyCategory;
  nextSessionInfo: string | null;
  isLeader: boolean;
  attendanceRate: number | null;
}

export interface StudySummaryListResDto {
  currentPageNum: number;
  pageSize: number;
  totalPages: number;
  totalElements: number;
  hasNext: boolean;
  studyList: StudySummaryResDto[];
}

export interface SessionSummaryResDto {
  sessionId: number;
  sessionNumber: number;
  startsAt: string;
  title: string;
}

export interface NextSessionAssignmentDto {
  assignmentId: number;
  title: string;
  dueAt: string;
  daysUntilDue: number;
  submitted: boolean;
}

export interface AttendanceRateComparisonDto {
  totalAverage: number | null;
  myAverage: number | null;
}

export interface AssignmentRateComparisonDto {
  totalRate: number | null;
  myRate: number | null;
}

export interface MergedSessionDashboardDataDto {
  sessionId: number;
  sessionNumber: number;
  title: string;
  startsAt: string;
  myAttendanceStatus: AttendanceStatus;
  teamAttendanceRate: number | null;
  teamAssignmentSubmissionRate: number | null;
  hasAssignments: boolean;
}

export interface SessionDashboardDataListResDto {
  currentPage: number;
  pageSize: number;
  totalPages: number;
  totalElements: number;
  hasNext: boolean;
  sessionList: MergedSessionDashboardDataDto[];
}

export interface StudyDashboardResDto {
  studyId: number;
  title: string;
  description: string;
  category: StudyCategory;
  status: StudyStatus;
  isLeader: boolean;
  currentApplicationCnt: number;
  currentParticipantCount: number;
  capacity: number;
  nextSession: SessionSummaryResDto | null;
  nextSessionAssignments: NextSessionAssignmentDto[];
  attendanceComparison: AttendanceRateComparisonDto;
  assignmentComparison: AssignmentRateComparisonDto;
  sessions: SessionDashboardDataListResDto;
}

export interface UpdateStudyLeaderResDto {
  studyId: number;
  newLeaderId: number;
  newLeaderName: string;
}

// ---- study apply (스터디 찾기) ----
export interface StudyApplySummaryResDto {
  studyId: number;
  title: string;
  leaderName: string;
  category: StudyCategory;
  status: StudyStatus;
  myStatus: MyStudyApplyStatus;
  currentParticipantCnt: number;
  capacity: number;
  createdAt: string;
}

export interface StudyApplySummaryListResDto {
  currentPageNum: number;
  pageSize: number;
  totalPages: number;
  totalElements: number;
  hasNext: boolean;
  studyList: StudyApplySummaryResDto[];
}

export interface StudyApplyDetailResDto {
  studyId: number;
  title: string;
  leaderId: number;
  leaderName: string;
  description: string;
  category: StudyCategory;
  status: StudyStatus;
  myStatus: MyStudyApplyStatus;
  currentParticipantCnt: number;
  capacity: number;
  isLeader: boolean;
  createdAt: string;
}

// ---- session ----
export interface SessionResDto {
  sessionId: number;
  sessionNumber: number;
  title: string;
  content: string;
  startsAt: string;
}

export interface SessionAssignmentResDto {
  assignmentId: number;
  title: string;
  dueAt: string;
  isSubmitted: boolean;
}

export interface SessionAttendanceResDto {
  attendanceId: number;
  memberId: number;
  memberName: string;
  status: AttendanceStatus;
}

export interface SessionInfoResDto {
  sessionId: number;
  sessionNumber: number;
  title: string;
  content: string;
  startsAt: string;
  assignments: SessionAssignmentResDto[];
  attendances: SessionAttendanceResDto[];
  attendanceRate: number;
  assignmentRate: number;
}

// ---- assignment ----
export interface AssignmentInfoResDto {
  assignmentId: number;
  sessionId: number;
  sessionNumber: number;
  title: string;
  description: string;
  dueAt: string;
}

export interface AssignmentSummaryResDto {
  assignmentId: number;
  sessionNumber: number;
  title: string;
  dueAt: string;
}

export interface AssignmentListResDto {
  sessionId: number;
  assignments: AssignmentSummaryResDto[];
}

// ---- attendance ----
export interface AttendanceParticipantResDto {
  memberId: number;
  name: string;
  status: AttendanceStatus;
}

export interface AttendanceSessionResDto {
  sessionId: number;
  participants: AttendanceParticipantResDto[];
}

// ---- application ----
export interface ApplicationResDto {
  applicationId: number;
  studyId: number;
  applicantId: number;
  applicantName: string;
  content: string;
  status: ApplicationStatus;
  appliedAt: string;
}

// ---- submission ----
export interface SubmissionSummaryResDto {
  submissionId: number;
  memberId: number;
  content: string;
}

export interface SubmissionInfoResDto {
  submissionId: number | null;
  memberId: number;
  memberName: string;
  content: string | null;
  createdAt: string | null;
  updatedAt: string | null;
  isSubmitted: boolean;
}

export interface SubmissionListResDto {
  assignmentId: number;
  submissions: SubmissionInfoResDto[];
}

// ---- mypage ----
export interface MyStudyResDto {
  studyId: number;
  title: string;
  category: StudyCategory;
  status: StudyStatus;
  role: StudyRole;
}

export interface MyAttendanceRateResDto {
  totalCount: number;
  presentCount: number;
  attendanceRate: number;
}

export interface MyStreakResDto {
  longestStreak: number;
}

export interface MyAssignmentResDto {
  assignmentId: number;
  studyId: number;
  studyTitle: string;
  title: string;
  dueAt: string;
}

export interface MyApplicationResDto {
  applicationId: number;
  studyId: number;
  studyTitle: string;
  status: ApplicationStatus;
  appliedAt: string;
}

export interface MyTodaySessionResDto {
  sessionId: number;
  studyId: number;
  studyTitle: string;
  title: string;
  startsAt: string;
}

export interface MyPageDashboardResDto {
  studies: MyStudyResDto[];
  attendanceRate: MyAttendanceRateResDto;
  streak: MyStreakResDto;
  deadlineAssignments: MyAssignmentResDto[];
  todaySessions: MyTodaySessionResDto[];
  applications: MyApplicationResDto[];
}

// ---- calendar ----
export interface CalendarItemResDto {
  type: "SESSION" | "ASSIGNMENT";
  id: number;
  studyId: number;
  studyTitle: string;
  title: string;
  date: string;
}

export interface CalendarMonthResDto {
  year: number;
  month: number;
  schedules: CalendarItemResDto[];
}
