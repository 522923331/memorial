package com.ruoyi.memorial.utils;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import com.ruoyi.common.constant.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Component
public class QrCodeUtil {

    private static final Logger log = LoggerFactory.getLogger(QrCodeUtil.class);

    @Value("${ruoyi.profile}")
    private String profilePath;

    /**
     * H5 前端基础地址，用于拼装扫码后跳转的完整 URL。
     * 配置示例：http://localhost:5173 或 https://memorial.example.com
     * 留空时回退为仅编码 qrcodeCode 本身（仅限本地调试）。
     */
    @Value("${memorial.qrcode.h5-base-url:}")
    private String h5BaseUrl;

    public byte[] generateQrCode(String content) throws WriterException, IOException {
        int width = 300;
        int height = 300;
        Map<EncodeHintType, Object> hints = new HashMap<>();
        hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
        hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);
        hints.put(EncodeHintType.MARGIN, 1);

        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        BitMatrix bitMatrix = qrCodeWriter.encode(content, BarcodeFormat.QR_CODE, width, height, hints);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        MatrixToImageWriter.writeToStream(bitMatrix, "PNG", outputStream);
        return outputStream.toByteArray();
    }

    /**
     * 生成二维码图片并落盘，返回可访问的 URL 和扫码内容。
     * URL 形如 /profile/memorial/qrcode/<code>.png，由 Spring 资源处理器映射到 profilePath 目录。
     */
    public QrCodeResult generateForCode(String qrcodeCode) {
        String content = buildContent(qrcodeCode);
        String url;
        try {
            byte[] qrBytes = generateQrCode(content);
            String fileName = qrcodeCode + ".png";
            String savePath = profilePath + "/memorial/qrcode";
            File dir = new File(savePath);
            if (!dir.exists() && !dir.mkdirs()) {
                throw new IOException("创建二维码目录失败: " + savePath);
            }
            File qrFile = new File(savePath, fileName);
            try (FileOutputStream fos = new FileOutputStream(qrFile)) {
                fos.write(qrBytes);
            }
            url = Constants.RESOURCE_PREFIX + "/memorial/qrcode/" + fileName;
        } catch (Exception e) {
            log.error("生成二维码失败", e);
            url = "";
        }
        return new QrCodeResult(url, content);
    }

    private String buildContent(String qrcodeCode) {
        if (h5BaseUrl == null || h5BaseUrl.isBlank()) {
            return qrcodeCode;
        }
        String base = h5BaseUrl.replaceAll("/+$", "");
        return base + "/#/pages/memorial/detail?code=" + qrcodeCode;
    }

    public record QrCodeResult(String url, String content) {
    }
}
