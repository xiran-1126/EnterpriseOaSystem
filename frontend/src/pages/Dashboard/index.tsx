import { useUserStore } from '@/store/userStore'
import './style.css'

const DashboardPage = () => {
  const { userInfo } = useUserStore()

  return (
    <div className="dashboard-page">
      <div className="welcome-section">
        <h2>欢迎回来，{userInfo?.realName || '用户'}！</h2>
        <p>今天是个好日子，祝您工作愉快 🎉</p>
      </div>

      <div className="stats-grid">
        <div className="stat-card">
          <div className="stat-icon blue">📋</div>
          <div className="stat-info">
            <div className="stat-value">12</div>
            <div className="stat-label">待办事项</div>
          </div>
        </div>
        <div className="stat-card">
          <div className="stat-icon green">✅</div>
          <div className="stat-info">
            <div className="stat-value">28</div>
            <div className="stat-label">已办事项</div>
          </div>
        </div>
        <div className="stat-card">
          <div className="stat-icon orange">📧</div>
          <div className="stat-info">
            <div className="stat-value">5</div>
            <div className="stat-label">未读消息</div>
          </div>
        </div>
        <div className="stat-card">
          <div className="stat-icon purple">📅</div>
          <div className="stat-info">
            <div className="stat-value">3</div>
            <div className="stat-label">今日会议</div>
          </div>
        </div>
      </div>
    </div>
  )
}

export default DashboardPage
