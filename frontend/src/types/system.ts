export interface UserVO {
  id: number
  username: string
  realName: string
  phone: string
  email: string
  deptId: number
  deptName: string
  postId: number
  postName: string
  roleIds: number[]
  roleNames: string
  status: number
  remark: string
  createTime: string
}

export interface UserQueryDTO {
  username?: string
  realName?: string
  phone?: string
  email?: string
  deptId?: number
  postId?: number
  status?: number
  startTime?: string
  endTime?: string
  pageNum?: number
  pageSize?: number
  orderByColumn?: string
  isAsc?: string
  showDeleted?: boolean
}

export interface CreateUserDTO {
  username: string
  realName: string
  phone: string
  email?: string
  deptId: number
  postId: number
  roleIds: number[]
  password?: string
  remark?: string
}

export interface UpdateUserDTO {
  id: number
  realName: string
  phone: string
  email?: string
  deptId: number
  postId: number
  roleIds: number[]
  remark?: string
}

export interface ResetUserPasswordDTO {
  userId: number
  newPassword?: string
}

export interface PageResult<T> {
  list: T[]
  total: number
  pageNum: number
  pageSize: number
}

export interface SysDept {
  id: number
  parentId: number
  deptName: string
  orderNum: number
  leader: string
  phone: string
  email: string
  status: number
  createTime: string
}

export interface SysPost {
  id: number
  postCode: string
  postName: string
  deptId: number
  orderNum: number
  description: string
  status: number
  createTime: string
}

export interface SysRole {
  id: number
  roleName: string
  roleCode: string
  description: string
  status: number
  createTime: string
}
