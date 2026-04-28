/**
 * 解析扫码结果，提取纪念页编码
 * QR code URL 格式: https://domain/memorial/{code} 或直接为 code 字符串
 */
export function parseQrCode(scanResult: string): string | null {
  if (!scanResult) return null

  // 如果是 URL，提取最后一段路径
  if (scanResult.startsWith('http')) {
    const matches = scanResult.match(/\/([a-zA-Z0-9_-]+)\/?$/)
    return matches ? matches[1] : null
  }

  // 如果包含 qrcodeCode 参数
  if (scanResult.includes('code=')) {
    const matches = scanResult.match(/code=([a-zA-Z0-9_-]+)/)
    return matches ? matches[1] : null
  }

  // 直接就是 code
  if (/^[a-zA-Z0-9_-]+$/.test(scanResult)) {
    return scanResult
  }

  return null
}
