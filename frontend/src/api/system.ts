import { get, post, put, del } from './request'
import type {
  UserVO,
  UserQueryDTO,
  CreateUserDTO,
  UpdateUserDTO,
  ResetUserPasswordDTO,
  PageResult,
  SysDept,
  SysPost,
  SysRole,
} from '@/types/system'

export const getUserPage = (params: UserQueryDTO): Promise<PageResult<UserVO>> => {
  return get<PageResult<UserVO>>('/system/user/page', { params })
}

export const getUserById = (id: number): Promise<UserVO> => {
  return get<UserVO>(`/system/user/${id}`)
}

export const checkUsernameUnique = (username: string): Promise<boolean> => {
  return get<boolean>('/system/user/check-username', { params: { username } })
}

export const checkPhoneUnique = (phone: string, userId?: number): Promise<boolean> => {
  return get<boolean>('/system/user/check-phone', { params: { phone, userId } })
}

export const createUser = (data: CreateUserDTO): Promise<void> => {
  return post<void>('/system/user', data)
}

export const updateUser = (data: UpdateUserDTO): Promise<void> => {
  return put<void>('/system/user', data)
}

export const enableUser = (id: number): Promise<void> => {
  return put<void>(`/system/user/enable/${id}`)
}

export const disableUser = (id: number): Promise<void> => {
  return put<void>(`/system/user/disable/${id}`)
}

export const batchEnableUser = (userIds: number[]): Promise<void> => {
  return put<void>('/system/user/batch-enable', userIds)
}

export const batchDisableUser = (userIds: number[]): Promise<void> => {
  return put<void>('/system/user/batch-disable', userIds)
}

export const resetUserPassword = (data: ResetUserPasswordDTO): Promise<string> => {
  return put<string>('/system/user/reset-password', data)
}

export const deleteUser = (id: number): Promise<void> => {
  return del<void>(`/system/user/${id}`)
}

export const batchDeleteUsers = (userIds: number[]): Promise<void> => {
  return del<void>('/system/user/batch', { data: userIds })
}

export const exportUsers = (params: UserQueryDTO): Promise<UserVO[]> => {
  return get<UserVO[]>('/system/user/export', { params })
}

export const getDeptTree = (): Promise<SysDept[]> => {
  return get<SysDept[]>('/system/user/dept/tree')
}

export const getPostsByDeptId = (deptId: number): Promise<SysPost[]> => {
  return get<SysPost[]>('/system/user/post/list', { params: { deptId } })
}

export const getAllRoles = (): Promise<SysRole[]> => {
  return get<SysRole[]>('/system/user/role/list')
}
