export interface AjaxResult<T = any> {
  code: number
  msg: string
  data: T
}

export interface PageResult<T = any> {
  rows: T[]
  total: number
}
