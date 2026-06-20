import { useState } from 'react'
import { Layout, Menu, Avatar, Dropdown, Button } from 'antd'
import {
  DashboardOutlined,
  UserOutlined,
  SettingOutlined,
  LogoutOutlined,
  MenuFoldOutlined,
  MenuUnfoldOutlined,
  TeamOutlined,
  UserSwitchOutlined,
  SafetyOutlined,
  AppstoreOutlined,
} from '@ant-design/icons'
import { Outlet, useNavigate, useLocation } from 'react-router-dom'
import { useUserStore } from '@/store/userStore'
import './MainLayout.css'

const { Header, Sider, Content } = Layout

const MainLayout = () => {
  const navigate = useNavigate()
  const location = useLocation()
  const { userInfo, clearAuth } = useUserStore()
  const [collapsed, setCollapsed] = useState(false)

  const handleLogout = () => {
    clearAuth()
    navigate('/login')
  }

  const userMenuItems = [
    {
      key: 'profile',
      icon: <UserOutlined />,
      label: '个人中心',
    },
    {
      key: 'settings',
      icon: <SettingOutlined />,
      label: '个人设置',
    },
    {
      type: 'divider' as const,
    },
    {
      key: 'logout',
      icon: <LogoutOutlined />,
      label: '退出登录',
      onClick: handleLogout,
    },
  ]

  const menuItems = [
    {
      key: '/dashboard',
      icon: <DashboardOutlined />,
      label: '工作台',
    },
    {
      key: 'system',
      icon: <SettingOutlined />,
      label: '系统管理',
      children: [
        {
          key: '/system/user',
          icon: <UserOutlined />,
          label: '用户管理',
        },
        {
          key: '/system/dept',
          icon: <TeamOutlined />,
          label: '部门管理',
        },
        {
          key: '/system/post',
          icon: <UserSwitchOutlined />,
          label: '岗位管理',
        },
        {
          key: '/system/role',
          icon: <SafetyOutlined />,
          label: '角色管理',
        },
        {
          key: '/system/dict',
          icon: <AppstoreOutlined />,
          label: '字典管理',
        },
      ],
    },
  ]

  const handleMenuClick = ({ key }: { key: string }) => {
    if (key.startsWith('/')) {
      navigate(key)
    }
  }

  const getSelectedKeys = () => {
    const pathname = location.pathname
    if (pathname.startsWith('/system')) {
      return [pathname]
    }
    return [pathname]
  }

  const getOpenKeys = () => {
    const pathname = location.pathname
    if (pathname.startsWith('/system')) {
      return ['system']
    }
    return []
  }

  return (
    <Layout className="main-layout">
      <Sider
        trigger={null}
        collapsible
        collapsed={collapsed}
        className="layout-sider"
      >
        <div className="logo">
          <div className="logo-icon">企</div>
          {!collapsed && <span className="logo-text">企业OA</span>}
        </div>
        <Menu
          theme="dark"
          mode="inline"
          selectedKeys={getSelectedKeys()}
          defaultOpenKeys={getOpenKeys()}
          items={menuItems}
          onClick={handleMenuClick}
        />
      </Sider>
      <Layout>
        <Header className="layout-header">
          <Button
            type="text"
            icon={collapsed ? <MenuUnfoldOutlined /> : <MenuFoldOutlined />}
            onClick={() => setCollapsed(!collapsed)}
            className="collapse-btn"
          />
          <div className="header-right">
            <Dropdown menu={{ items: userMenuItems }} placement="bottomRight">
              <div className="user-info">
                <Avatar size="small" src={userInfo?.avatar} icon={<UserOutlined />} />
                <span className="user-name">{userInfo?.realName || '用户'}</span>
              </div>
            </Dropdown>
          </div>
        </Header>
        <Content className="layout-content">
          <Outlet />
        </Content>
      </Layout>
    </Layout>
  )
}

export default MainLayout
