export type Role = "leader" | "member";

export type AttendanceStatus = "출석" | "지각" | "결석" | "예정";

export type ApplicationStatus = "대기" | "승인" | "거절";

export type SubmissionStatus = "제출완료" | "미제출";

export interface User {
  id: string;
  name: string;
  email: string;
  phone: string;
  avatarColor: string;
}

export interface Study {
  id: string;
  title: string;
  category: string;
  tag: string;
  description: string;
  introLong: string;
  schedule: string;
  location: string;
  capacity: number;
  creatorId: string;
  status: "모집중" | "진행중" | "종료";
  totalRounds: number;
  doneRounds: number;
  createdAt: string;
}

export interface Member {
  id: string; // member_id
  studyId: string;
  userId: string;
  name: string;
  role: Role;
  joinedAt: string;
  attendanceRate: number;
  submissionRate: number;
  atRisk?: boolean;
}

export interface Round {
  id: string;
  studyId: string;
  index: number;
  title: string;
  date: string;
  time: string;
  location: string;
  status: "예정" | "완료";
  attendanceRate?: number;
}

export interface Attendance {
  id: string;
  roundId: string;
  memberId: string;
  status: AttendanceStatus;
}

export interface Assignment {
  id: string;
  roundId: string;
  studyId: string;
  title: string;
  description: string;
  deadline: string;
  createdAt: string;
}

export interface Submission {
  id: string;
  assignmentId: string;
  memberId: string;
  content: string;
  submittedAt: string | null;
  status: SubmissionStatus;
}

export interface Application {
  id: string;
  studyId: string;
  applicantName: string;
  message: string;
  appliedAt: string;
  status: ApplicationStatus;
}
