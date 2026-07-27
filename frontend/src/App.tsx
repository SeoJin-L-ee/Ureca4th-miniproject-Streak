import { HashRouter, Navigate, Route, Routes } from "react-router-dom";
import AppLayout from "./layouts/AppLayout";
import { AuthProvider } from "./context/AuthContext";
import Login from "./pages/Login";
import Signup from "./pages/Signup";
import MyPage from "./pages/MyPage";
import MyStudyList from "./pages/MyStudyList";
import StudyDetail from "./pages/StudyDetail";
import ApplicantManage from "./pages/ApplicantManage";
import RoundList from "./pages/RoundList";
import RoundDetail from "./pages/RoundDetail";
import AssignmentCreate from "./pages/AssignmentCreate";
import AssignmentDetail from "./pages/AssignmentDetail";
import StudyExplore from "./pages/StudyExplore";
import CalendarPage from "./pages/CalendarPage";

function App() {
  return (
    <HashRouter>
      <AuthProvider>
        <Routes>
          <Route path="login" element={<Login />} />
          <Route path="signup" element={<Signup />} />
          <Route element={<AppLayout />}>
            <Route index element={<Navigate to="/studies" replace />} />
            <Route path="studies" element={<MyStudyList />} />
            <Route path="studies/:studyId" element={<StudyDetail />} />
            <Route path="studies/:studyId/applications" element={<ApplicantManage />} />
            <Route path="studies/:studyId/sessions" element={<RoundList />} />
            <Route path="studies/:studyId/sessions/:roundId" element={<RoundDetail />} />
            <Route path="studies/:studyId/sessions/:roundId/assignments/new" element={<AssignmentCreate />} />
            <Route path="studies/:studyId/sessions/:roundId/assignments/:assignmentId" element={<AssignmentDetail />} />
            <Route path="explore" element={<StudyExplore />} />
            <Route path="members/me/calendar" element={<CalendarPage />} />
            <Route path="members/me" element={<MyPage />} />
            <Route path="*" element={<Navigate to="/studies" replace />} />
          </Route>
        </Routes>
      </AuthProvider>
    </HashRouter>
  );
}

export default App;
