import './brandPanel.css'

export const BrandPanel = () => {
  const features = [
    { icon: '📊', title: '数据可视化', desc: '多维度数据分析' },
    { icon: '⚡', title: '流程自动化', desc: '智能审批流转' },
    { icon: '🔐', title: '安全可靠', desc: '企业级数据安全' },
    { icon: '🤝', title: '高效协作', desc: '团队无缝沟通' },
  ]

  return (
    <div className="brand-panel">
      <div className="decoration-circle circle-1"></div>
      <div className="decoration-circle circle-2"></div>
      <div className="decoration-circle circle-3"></div>
      <div className="decoration-shape shape-1"></div>
      <div className="decoration-shape shape-2"></div>
      <div className="decoration-dots dots-1"></div>
      <div className="decoration-dots dots-2"></div>

      <div className="brand-content">
        <div className="brand-logo">
          <div className="logo-icon">
            <span className="logo-text">企</span>
            <div className="logo-glow"></div>
          </div>
          <div className="brand-title-group">
            <h1 className="brand-title">企业OA</h1>
            <p className="brand-subtitle">办公自动化系统</p>
          </div>
        </div>

        <div className="brand-slogan-section">
          <p className="brand-slogan">
            让办公更高效
            <span className="slogan-highlight"> · </span>
            让管理更智慧
          </p>
          <p className="brand-desc">
            一站式企业数字化办公解决方案，助力企业提升运营效率
          </p>
        </div>

        <div className="brand-features">
          {features.map((feature, index) => (
            <div key={index} className="feature-card">
              <div className="feature-icon-wrapper">
                <span className="feature-icon">{feature.icon}</span>
              </div>
              <div className="feature-info">
                <h3 className="feature-title">{feature.title}</h3>
                <p className="feature-desc">{feature.desc}</p>
              </div>
            </div>
          ))}
        </div>
      </div>

      <div className="brand-footer">
        <div className="footer-line"></div>
        <p className="copyright-text">© 2026 企业OA系统 版权所有</p>
      </div>
    </div>
  )
}
