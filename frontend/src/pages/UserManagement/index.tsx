import { useState, useEffect, useCallback } from 'react'
import {
  Table,
  Button,
  Space,
  Input,
  Select,
  DatePicker,
  Form,
  Tag,
  message,
  Popconfirm,
  Tooltip,
  TreeSelect,
  Checkbox,
} from 'antd'
import {
  PlusOutlined,
  SearchOutlined,
  ReloadOutlined,
  EditOutlined,
  DeleteOutlined,
  LockOutlined,
  UnlockOutlined,
  KeyOutlined,
  ExportOutlined,
  SettingOutlined,
} from '@ant-design/icons'
import type { ColumnsType, TableRowSelection } from 'antd/es/table/interface'
import dayjs from 'dayjs'
import {
  getUserPage,
  deleteUser,
  batchDeleteUsers,
  enableUser,
  disableUser,
  batchEnableUser,
  batchDisableUser,
  getDeptTree,
  getPostsByDeptId,
  exportUsers,
} from '@/api/system'
import type { UserVO, UserQueryDTO, SysDept, SysPost } from '@/types/system'
import UserFormModal from './components/UserFormModal'
import ResetPasswordModal from './components/ResetPasswordModal'
import './style.css'

const { RangePicker } = DatePicker
const { Option } = Select

const UserManagementPage = () => {
  const [form] = Form.useForm()
  const [loading, setLoading] = useState(false)
  const [userList, setUserList] = useState<UserVO[]>([])
  const [total, setTotal] = useState(0)
  const [pageNum, setPageNum] = useState(1)
  const [pageSize, setPageSize] = useState(10)
  const [selectedRowKeys, setSelectedRowKeys] = useState<React.Key[]>([])
  const [deptTree, setDeptTree] = useState<any[]>([])
  const [postList, setPostList] = useState<SysPost[]>([])
  const [showDeleted, setShowDeleted] = useState(false)

  const [addModalVisible, setAddModalVisible] = useState(false)
  const [editModalVisible, setEditModalVisible] = useState(false)
  const [resetPwdModalVisible, setResetPwdModalVisible] = useState(false)
  const [currentUser, setCurrentUser] = useState<UserVO | null>(null)

  const fetchUserList = useCallback(async () => {
    setLoading(true)
    try {
      const values = form.getFieldsValue()
      const params: UserQueryDTO = {
        ...values,
        pageNum,
        pageSize,
        showDeleted,
      }
      if (values.createTime && values.createTime.length === 2) {
        params.startTime = values.createTime[0].format('YYYY-MM-DD HH:mm:ss')
        params.endTime = values.createTime[1].format('YYYY-MM-DD HH:mm:ss')
      }
      delete (params as any).createTime

      const res = await getUserPage(params)
      setUserList(res.list)
      setTotal(res.total)
    } catch (error) {
      console.error('获取用户列表失败', error)
    } finally {
      setLoading(false)
    }
  }, [pageNum, pageSize, showDeleted, form])

  const fetchDeptTree = async () => {
    try {
      const depts = await getDeptTree()
      const treeData = buildTree(depts, 0)
      setDeptTree(treeData)
    } catch (error) {
      console.error('获取部门树失败', error)
    }
  }

  const buildTree = (depts: SysDept[], parentId: number): any[] => {
    return depts
      .filter((dept) => dept.parentId === parentId)
      .map((dept) => ({
        title: dept.deptName,
        value: dept.id,
        children: buildTree(depts, dept.id),
      }))
  }

  const handleDeptChange = async (deptId: number) => {
    if (deptId) {
      try {
        const posts = await getPostsByDeptId(deptId)
        setPostList(posts)
      } catch (error) {
        console.error('获取岗位列表失败', error)
      }
    } else {
      setPostList([])
    }
  }

  useEffect(() => {
    fetchDeptTree()
  }, [])

  useEffect(() => {
    fetchUserList()
  }, [fetchUserList])

  const handleSearch = () => {
    setPageNum(1)
    fetchUserList()
  }

  const handleReset = () => {
    form.resetFields()
    setPostList([])
    setShowDeleted(false)
    setPageNum(1)
    fetchUserList()
  }

  const handleAdd = () => {
    setCurrentUser(null)
    setAddModalVisible(true)
  }

  const handleEdit = (record: UserVO) => {
    setCurrentUser(record)
    setEditModalVisible(true)
  }

  const handleDelete = async (record: UserVO) => {
    try {
      await deleteUser(record.id)
      message.success('删除成功')
      fetchUserList()
    } catch (error) {
      console.error('删除用户失败', error)
    }
  }

  const handleBatchDelete = async () => {
    if (selectedRowKeys.length === 0) {
      message.warning('请选择要删除的用户')
      return
    }
    try {
      await batchDeleteUsers(selectedRowKeys.map(Number))
      message.success(`成功删除 ${selectedRowKeys.length} 个用户`)
      setSelectedRowKeys([])
      fetchUserList()
    } catch (error) {
      console.error('批量删除用户失败', error)
    }
  }

  const handleEnable = async (record: UserVO) => {
    try {
      await enableUser(record.id)
      message.success('启用成功')
      fetchUserList()
    } catch (error) {
      console.error('启用用户失败', error)
    }
  }

  const handleDisable = async (record: UserVO) => {
    try {
      await disableUser(record.id)
      message.success('禁用成功')
      fetchUserList()
    } catch (error) {
      console.error('禁用用户失败', error)
    }
  }

  const handleBatchEnable = async () => {
    if (selectedRowKeys.length === 0) {
      message.warning('请选择要启用的用户')
      return
    }
    try {
      await batchEnableUser(selectedRowKeys.map(Number))
      message.success(`成功启用 ${selectedRowKeys.length} 个用户`)
      setSelectedRowKeys([])
      fetchUserList()
    } catch (error) {
      console.error('批量启用用户失败', error)
    }
  }

  const handleBatchDisable = async () => {
    if (selectedRowKeys.length === 0) {
      message.warning('请选择要禁用的用户')
      return
    }
    try {
      await batchDisableUser(selectedRowKeys.map(Number))
      message.success(`成功禁用 ${selectedRowKeys.length} 个用户`)
      setSelectedRowKeys([])
      fetchUserList()
    } catch (error) {
      console.error('批量禁用用户失败', error)
    }
  }

  const handleResetPassword = (record: UserVO) => {
    setCurrentUser(record)
    setResetPwdModalVisible(true)
  }

  const handleExport = async () => {
    try {
      const values = form.getFieldsValue()
      const params: UserQueryDTO = {
        ...values,
        showDeleted,
      }
      if (values.createTime && values.createTime.length === 2) {
        params.startTime = values.createTime[0].format('YYYY-MM-DD HH:mm:ss')
        params.endTime = values.createTime[1].format('YYYY-MM-DD HH:mm:ss')
      }
      delete (params as any).createTime

      const data = await exportUsers(params)
      message.success(`成功导出 ${data.length} 条数据`)
      console.log('导出数据:', data)
    } catch (error) {
      console.error('导出用户失败', error)
    }
  }

  const columns: ColumnsType<UserVO> = [
    {
      title: '序号',
      key: 'index',
      width: 60,
      render: (_text, _record, index) => (pageNum - 1) * pageSize + index + 1,
    },
    {
      title: '工号',
      dataIndex: 'username',
      key: 'username',
      width: 120,
      sorter: true,
    },
    {
      title: '姓名',
      dataIndex: 'realName',
      key: 'realName',
      width: 100,
    },
    {
      title: '手机号',
      dataIndex: 'phone',
      key: 'phone',
      width: 130,
    },
    {
      title: '邮箱',
      dataIndex: 'email',
      key: 'email',
      width: 200,
    },
    {
      title: '所属部门',
      dataIndex: 'deptName',
      key: 'deptName',
      width: 120,
    },
    {
      title: '岗位',
      dataIndex: 'postName',
      key: 'postName',
      width: 120,
    },
    {
      title: '绑定角色',
      dataIndex: 'roleNames',
      key: 'roleNames',
      width: 200,
    },
    {
      title: '账号状态',
      dataIndex: 'status',
      key: 'status',
      width: 100,
      render: (status: number) =>
        status === 1 ? <Tag color="green">启用</Tag> : <Tag color="red">禁用</Tag>,
    },
    {
      title: '创建时间',
      dataIndex: 'createTime',
      key: 'createTime',
      width: 180,
      sorter: true,
      render: (time: string) => dayjs(time).format('YYYY-MM-DD HH:mm:ss'),
    },
    {
      title: '操作',
      key: 'action',
      width: 240,
      fixed: 'right',
      render: (_text, record) => (
        <Space size="small">
          <Tooltip title="编辑">
            <Button
              type="link"
              size="small"
              icon={<EditOutlined />}
              onClick={() => handleEdit(record)}
            >
              编辑
            </Button>
          </Tooltip>
          <Tooltip title="重置密码">
            <Button
              type="link"
              size="small"
              icon={<KeyOutlined />}
              onClick={() => handleResetPassword(record)}
            >
              重置密码
            </Button>
          </Tooltip>
          {record.status === 1 ? (
            <Popconfirm
              title="确认禁用该账号？"
              description="禁用后用户无法登录系统"
              onConfirm={() => handleDisable(record)}
              okText="确认"
              cancelText="取消"
            >
              <Button type="link" size="small" danger icon={<LockOutlined />}>
                禁用
              </Button>
            </Popconfirm>
          ) : (
            <Popconfirm
              title="确认启用该账号？"
              onConfirm={() => handleEnable(record)}
              okText="确认"
              cancelText="取消"
            >
              <Button type="link" size="small" icon={<UnlockOutlined />}>
                启用
              </Button>
            </Popconfirm>
          )}
          <Popconfirm
            title="确认删除该用户？"
            description="删除后数据不可恢复"
            onConfirm={() => handleDelete(record)}
            okText="确认"
            cancelText="取消"
            okButtonProps={{ danger: true }}
          >
            <Button type="link" size="small" danger icon={<DeleteOutlined />}>
              删除
            </Button>
          </Popconfirm>
        </Space>
      ),
    },
  ]

  const rowSelection: TableRowSelection<UserVO> = {
    selectedRowKeys,
    onChange: (newSelectedRowKeys) => {
      setSelectedRowKeys(newSelectedRowKeys)
    },
  }

  const handleTableChange = (pagination: any, _filters: any, sorter: any) => {
    setPageNum(pagination.current)
    setPageSize(pagination.pageSize)
    if (sorter.field && sorter.order) {
      const orderByColumn = sorter.field === 'username' ? 'u.username' : 'u.create_time'
      const isAsc = sorter.order === 'ascend' ? 'asc' : 'desc'
      form.setFieldsValue({ orderByColumn, isAsc })
    }
  }

  return (
    <div className="user-management-page">
      <div className="page-header">
        <h2 className="page-title">用户管理</h2>
      </div>

      <div className="search-section">
        <Form form={form} layout="inline">
          <Form.Item name="realName" label="姓名">
            <Input placeholder="请输入姓名" allowClear style={{ width: 150 }} />
          </Form.Item>
          <Form.Item name="username" label="工号">
            <Input placeholder="请输入工号" allowClear style={{ width: 150 }} />
          </Form.Item>
          <Form.Item name="phone" label="手机号">
            <Input placeholder="请输入手机号" allowClear style={{ width: 150 }} />
          </Form.Item>
          <Form.Item name="email" label="邮箱">
            <Input placeholder="请输入邮箱" allowClear style={{ width: 180 }} />
          </Form.Item>
          <Form.Item name="deptId" label="部门">
            <TreeSelect
              placeholder="请选择部门"
              treeData={deptTree}
              allowClear
              style={{ width: 180 }}
              treeDefaultExpandAll
              onChange={handleDeptChange}
            />
          </Form.Item>
          <Form.Item name="postId" label="岗位">
            <Select placeholder="请选择岗位" allowClear style={{ width: 150 }}>
              {postList.map((post) => (
                <Option key={post.id} value={post.id}>
                  {post.postName}
                </Option>
              ))}
            </Select>
          </Form.Item>
          <Form.Item name="status" label="账号状态">
            <Select placeholder="请选择状态" allowClear style={{ width: 120 }}>
              <Option value={1}>启用</Option>
              <Option value={0}>禁用</Option>
            </Select>
          </Form.Item>
          <Form.Item name="createTime" label="创建时间">
            <RangePicker style={{ width: 280 }} />
          </Form.Item>
          <Form.Item>
            <Space>
              <Button type="primary" icon={<SearchOutlined />} onClick={handleSearch}>
                查询
              </Button>
              <Button icon={<ReloadOutlined />} onClick={handleReset}>
                重置
              </Button>
            </Space>
          </Form.Item>
        </Form>
      </div>

      <div className="toolbar">
        <Space>
          <Button type="primary" icon={<PlusOutlined />} onClick={handleAdd}>
            新增用户
          </Button>
          <Popconfirm
            title="确认批量启用选中的用户？"
            onConfirm={handleBatchEnable}
            okText="确认"
            cancelText="取消"
            disabled={selectedRowKeys.length === 0}
          >
            <Button
              icon={<UnlockOutlined />}
              disabled={selectedRowKeys.length === 0}
            >
              批量启用
            </Button>
          </Popconfirm>
          <Popconfirm
            title="确认批量禁用选中的用户？"
            description="禁用后用户无法登录系统"
            onConfirm={handleBatchDisable}
            okText="确认"
            cancelText="取消"
            disabled={selectedRowKeys.length === 0}
          >
            <Button
              danger
              icon={<LockOutlined />}
              disabled={selectedRowKeys.length === 0}
            >
              批量禁用
            </Button>
          </Popconfirm>
          <Popconfirm
            title="确认批量删除选中的用户？"
            description="删除后数据不可恢复"
            onConfirm={handleBatchDelete}
            okText="确认"
            cancelText="取消"
            okButtonProps={{ danger: true }}
            disabled={selectedRowKeys.length === 0}
          >
            <Button
              danger
              icon={<DeleteOutlined />}
              disabled={selectedRowKeys.length === 0}
            >
              批量删除
            </Button>
          </Popconfirm>
          <Button icon={<ExportOutlined />} onClick={handleExport}>
            导出用户花名册
          </Button>
        </Space>
        <Space>
          <Checkbox checked={showDeleted} onChange={(e) => setShowDeleted(e.target.checked)}>
            显示已删除用户
          </Checkbox>
          <Tooltip title="列设置">
            <Button icon={<SettingOutlined />} />
          </Tooltip>
        </Space>
      </div>

      <div className="table-section">
        <Table
          rowKey="id"
          columns={columns}
          dataSource={userList}
          loading={loading}
          rowSelection={rowSelection}
          pagination={{
            current: pageNum,
            pageSize,
            total,
            showSizeChanger: true,
            pageSizeOptions: ['10', '20', '50'],
            showQuickJumper: true,
            showTotal: (total) => `共 ${total} 条记录`,
          }}
          onChange={handleTableChange}
          scroll={{ x: 1400 }}
        />
      </div>

      <UserFormModal
        visible={addModalVisible}
        mode="add"
        onCancel={() => setAddModalVisible(false)}
        onSuccess={() => {
          setAddModalVisible(false)
          fetchUserList()
        }}
      />

      <UserFormModal
        visible={editModalVisible}
        mode="edit"
        userData={currentUser}
        onCancel={() => setEditModalVisible(false)}
        onSuccess={() => {
          setEditModalVisible(false)
          fetchUserList()
        }}
      />

      <ResetPasswordModal
        visible={resetPwdModalVisible}
        userData={currentUser}
        onCancel={() => setResetPwdModalVisible(false)}
        onSuccess={() => {
          setResetPwdModalVisible(false)
        }}
      />
    </div>
  )
}

export default UserManagementPage
