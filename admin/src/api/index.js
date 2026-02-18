import request from '../utils/request'

// 用户登录
export const login = (data) => request.post('/user/login', data)

// 管理员 - 用户管理
export const getUserList = (params) => request.get('/admin/user/list', { params })
export const updateUserStatus = (data) => request.put('/admin/user/status', data)

// 管理员 - 运动项目管理
export const getSportList = (params) => request.get('/admin/sport/list', { params })
export const addSport = (data) => request.post('/admin/sport/add', data)
export const updateSport = (data) => request.put('/admin/sport/update', data)
export const deleteSport = (id) => request.delete(`/admin/sport/delete/${id}`)

// 管理员 - 打卡记录管理
export const getCheckinList = (params) => request.get('/admin/checkin/list', { params })
export const deleteCheckin = (id) => request.delete(`/admin/checkin/delete/${id}`)

// 管理员 - 积分记录管理
export const getPointsList = (params) => request.get('/admin/points/list', { params })

// 管理员 - 统计
export const getStatsOverview = () => request.get('/admin/stats/overview')
export const getStatsTrend = (params) => request.get('/admin/stats/trend', { params })
