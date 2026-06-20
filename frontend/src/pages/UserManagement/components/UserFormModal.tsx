import { useState, useEffect } from 'react'
import { Modal, Form, Input, Select, TreeSelect, Checkbox, message, Space, Button } from 'antd'
import { ReloadOutlined } from '@ant-design/icons'
import {
  createUser,
  updateUser,
  getUserById,
  getDeptTree,
  getPostsByDeptId,
  getAllRoles,
  checkUsernameUnique,
  checkPhoneUnique,
} from '@/api/system'
import type { UserVO, SysDept, SysPost, SysRole, CreateUserDTO, UpdateUserDTO } from '@/types/system'

const { Option } = Select
const { TextArea } = Input

interface UserFormModalProps {
  visible: boolean
  mode: 'add' | 'edit'
  userData?: UserVO | null
  onCancel: () => void
  onSuccess: () => void
}

const UserFormModal = ({ visible, mode, userData, onCancel, onSuccess }: UserFormModalProps) => {
  const [form] = Form.useForm()
  const [loading, setLoading] = useState(false)
  const [deptTree, setDeptTree] = useState<any[]>([])
  const [postList, setPostList] = useState<SysPost[]>([])
  const [roleList, setRoleList] = useState<SysRole[]>([])
  const [autoGeneratePwd, setAutoGeneratePwd] = useState(true)

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

  const fetchRoles = async () => {
    try {
      const roles = await getAllRoles()
      setRoleList(roles)
    } catch (error) {
      console.error('获取角色列表失败', error)
    }
  }

  const fetchPosts = async (deptId: number) => {
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

  const handleDeptChange = (deptId: number) => {
    fetchPosts(deptId)
    form.setFieldsValue({ postId: undefined })
  }

  useEffect(() => {
    if (visible) {
      fetchDeptTree()
      fetchRoles()
      form.resetFields()

      if (mode === 'edit' && userData) {
        setLoading(true)
        getUserById(userData.id)
          .then((user) => {
            form.setFieldsValue({
              ...user,
            })
            fetchPosts(user.deptId)
          })
          .catch((error) => {
            console.error('获取用户详情失败', error)
            message.error('获取用户详情失败')
          })
          .finally(() => {
            setLoading(false)
          })
      } else {
        setPostList([])
        setAutoGeneratePwd(true)
      }
    }
  }, [visible, mode, userData])

  const validateUsername = async (_: any, value: string) => {
    if (!value) {
      return Promise.resolve()
    }
    if (mode === 'add') {
      const isUnique = await checkUsernameUnique(value)
      if (!isUnique) {
        return Promise.reject(new Error('工号已存在，请更换工号'))
      }
    }
    return Promise.resolve()
  }

  const validatePhone = async (_: any, value: string) => {
    if (!value) {
      return Promise.resolve()
    }
    const userId = mode === 'edit' ? userData?.id : undefined
    const isUnique = await checkPhoneUnique(value, userId)
    if (!isUnique) {
      return Promise.reject(new Error('该手机号已绑定其他账号'))
    }
    return Promise.resolve()
  }

  const generateRandomPassword = () => {
    const upperCase = 'ABCDEFGHIJKLMNOPQRSTUVWXYZ'
    const lowerCase = 'abcdefghijklmnopqrstuvwxyz'
    const number = '0123456789'
    const specialChar = '!@#$%^&*'
    const allChar = upperCase + lowerCase + number + specialChar

    let password = ''
    for (let i = 0; i < 2; i++) {
      password += upperCase.charAt(Math.floor(Math.random() * upperCase.length))
    }
    for (let i = 0; i < 2; i++) {
      password += lowerCase.charAt(Math.floor(Math.random() * lowerCase.length))
    }
    for (let i = 0; i < 2; i++) {
      password += number.charAt(Math.floor(Math.random() * number.length))
    }
    for (let i = 0; i < 2; i++) {
      password += specialChar.charAt(Math.floor(Math.random() * specialChar.length))
    }
    for (let i = 0; i < 4; i++) {
      password += allChar.charAt(Math.floor(Math.random() * allChar.length))
    }

    password = password.split('').sort(() => Math.random() - 0.5).join('')
    form.setFieldsValue({ password })
    return password
  }

  const handleSubmit = async () => {
    try {
      const values = await form.validateFields()
      setLoading(true)

      if (mode === 'add') {
        const dto: CreateUserDTO = {
          username: values.username,
          realName: values.realName,
          phone: values.phone,
          email: values.email,
          deptId: values.deptId,
          postId: values.postId,
          roleIds: values.roleIds,
          remark: values.remark,
        }
        if (!autoGeneratePwd && values.password) {
          dto.password = values.password
        }
        await createUser(dto)
        message.success('用户创建成功')
      } else {
        const dto: UpdateUserDTO = {
          id: userData!.id,
          realName: values.realName,
          phone: values.phone,
          email: values.email,
          deptId: values.deptId,
          postId: values.postId,
          roleIds: values.roleIds,
          remark: values.remark,
        }
        await updateUser(dto)
        message.success('用户更新成功')
      }

      onSuccess()
    } catch (error: any) {
      if (error.errorFields) {
        return
      }
      console.error('保存用户失败', error)
    } finally {
      setLoading(false)
    }
  }

  return (
    <Modal
      title={mode === 'add' ? '新增用户' : '编辑用户'}
      open={visible}
      onCancel={onCancel}
      onOk={handleSubmit}
      confirmLoading={loading}
      width={600}
      okText="确认"
      cancelText="取消"
      destroyOnClose
    >
      <Form
        form={form}
        layout="vertical"
        initialValues={{ status: 1 }}
      >
        <Form.Item
          name="username"
          label="工号"
          rules={[
            { required: true, message: '请输入工号' },
            { min: 2, max: 20, message: '工号长度为2-20位' },
            { pattern: /^[a-zA-Z0-9]+$/, message: '工号只能包含数字和字母' },
            { validator: validateUsername },
          ]}
        >
          <Input placeholder="请输入工号" disabled={mode === 'edit'} />
        </Form.Item>

        <Form.Item
          name="realName"
          label="姓名"
          rules={[
            { required: true, message: '请输入姓名' },
            { max: 10, message: '姓名长度不能超过10个字符' },
          ]}
        >
          <Input placeholder="请输入姓名" />
        </Form.Item>

        <Form.Item
          name="phone"
          label="手机号"
          rules={[
            { required: true, message: '请输入手机号' },
            { pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确' },
            { validator: validatePhone },
          ]}
        >
          <Input placeholder="请输入手机号" maxLength={11} />
        </Form.Item>

        <Form.Item
          name="email"
          label="邮箱"
          rules={[
            { type: 'email', message: '邮箱格式不正确' },
          ]}
        >
          <Input placeholder="请输入邮箱" />
        </Form.Item>

        <Form.Item
          name="deptId"
          label="所属部门"
          rules={[{ required: true, message: '请选择所属部门' }]}
        >
          <TreeSelect
            placeholder="请选择部门"
            treeData={deptTree}
            treeDefaultExpandAll
            onChange={handleDeptChange}
            showSearch
            treeNodeFilterProp="title"
          />
        </Form.Item>

        <Form.Item
          name="postId"
          label="岗位"
          rules={[{ required: true, message: '请选择岗位' }]}
        >
          <Select placeholder="请选择岗位" disabled={!form.getFieldValue('deptId')}>
            {postList.map((post) => (
              <Option key={post.id} value={post.id}>
                {post.postName}
              </Option>
            ))}
          </Select>
        </Form.Item>

        <Form.Item
          name="roleIds"
          label="绑定角色"
          rules={[{ required: true, message: '请至少选择一个角色' }]}
        >
          <Checkbox.Group style={{ width: '100%' }}>
            <Space wrap>
              {roleList.map((role) => (
                <Checkbox key={role.id} value={role.id}>
                  {role.roleName}
                </Checkbox>
              ))}
            </Space>
          </Checkbox.Group>
        </Form.Item>

        {mode === 'add' && (
          <>
            <Form.Item>
              <Checkbox
                checked={autoGeneratePwd}
                onChange={(e) => setAutoGeneratePwd(e.target.checked)}
              >
                系统自动生成初始密码
              </Checkbox>
            </Form.Item>

            {!autoGeneratePwd && (
              <Form.Item
                name="password"
                label="初始密码"
                rules={[
                  { required: true, message: '请输入初始密码' },
                  { min: 6, max: 20, message: '密码长度为6-20位' },
                  {
                    pattern: /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)[a-zA-Z\d!@#$%^&*()_+\-=\[\]{}|;:,.<>?]{6,20}$/,
                    message: '密码需包含大小写字母和数字',
                  },
                ]}
              >
                <Input.Password placeholder="请输入初始密码" />
              </Form.Item>
            )}

            {autoGeneratePwd && (
              <Form.Item label="初始密码">
                <Space>
                  <Button
                    icon={<ReloadOutlined />}
                    onClick={generateRandomPassword}
                  >
                    生成随机密码
                  </Button>
                  <span style={{ color: '#666' }}>
                    点击生成按钮可预览密码，系统将自动生成强密码
                  </span>
                </Space>
              </Form.Item>
            )}
          </>
        )}

        <Form.Item
          name="remark"
          label="备注"
          rules={[{ max: 200, message: '备注长度不能超过200字' }]}
        >
          <TextArea rows={3} placeholder="请输入备注信息" maxLength={200} showCount />
        </Form.Item>
      </Form>
    </Modal>
  )
}

export default UserFormModal
