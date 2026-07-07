package com.ruoyi.memorial.service;

import com.aliyun.oss.OSS;
import com.aliyun.oss.model.ObjectMetadata;
import com.ruoyi.memorial.config.OssProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.UUID;

/**
 * 阿里云 OSS 上传服务。
 *
 * 对象 key 规则：<directory>/<UUID>.<扩展名>
 * directory 由调用方传入完整路径，建议含 userId：memorial/<userId>/<type>
 * 返回的 URL 为完整 https URL，直接写入数据库。
 */
@Service
public class OssService {

    private static final Logger log = LoggerFactory.getLogger(OssService.class);

    @Autowired
    private OSS ossClient;

    @Autowired
    private OssProperties props;

    /**
     * 上传 MultipartFile 到 OSS。
     *
     * @param directory OSS 目录前缀，建议 memorial/<userId>/<type>
     * @return 完整 OSS URL；失败抛 RuntimeException
     */
    public String upload(MultipartFile file, String directory) {
        String originalName = file.getOriginalFilename();
        String ext = extractExtension(originalName);
        String objectKey = buildObjectKey(directory, ext);
        ObjectMetadata meta = new ObjectMetadata();
        meta.setContentLength(file.getSize());
        if (file.getContentType() != null) {
            meta.setContentType(file.getContentType());
        }
        try (InputStream is = file.getInputStream()) {
            ossClient.putObject(props.getBucket(), objectKey, is, meta);
            return buildUrl(objectKey);
        } catch (Exception e) {
            log.error("OSS 上传失败 key={} ", objectKey, e);
            throw new RuntimeException("文件上传失败", e);
        }
    }

    /**
     * 上传字节数组到 OSS（用于二维码图片）。
     */
    public String uploadBytes(byte[] bytes, String directory, String fileName) {
        String objectKey = directory + "/" + fileName;
        ObjectMetadata meta = new ObjectMetadata();
        meta.setContentLength(bytes.length);
        meta.setContentType("image/png");
        try {
            ossClient.putObject(props.getBucket(), objectKey, new ByteArrayInputStream(bytes), meta);
            return buildUrl(objectKey);
        } catch (Exception e) {
            log.error("OSS 上传字节数组失败 key={} ", objectKey, e);
            throw new RuntimeException("文件上传失败", e);
        }
    }

    private String buildObjectKey(String directory, String ext) {
        String name = UUID.randomUUID().toString().replace("-", "");
        if (ext != null && !ext.isEmpty()) {
            name = name + "." + ext;
        }
        return directory + "/" + name;
    }

    private String buildUrl(String objectKey) {
        String prefix = props.getUrlPrefix();
        if (prefix == null || prefix.isEmpty()) {
            prefix = "https://" + props.getBucket() + "." + props.getEndpoint().replaceFirst("^https?://", "");
        }
        return prefix.replaceAll("/+$", "") + "/" + objectKey;
    }

    private String extractExtension(String fileName) {
        if (fileName == null) return "";
        int dot = fileName.lastIndexOf('.');
        if (dot < 0 || dot == fileName.length() - 1) return "";
        return fileName.substring(dot + 1).toLowerCase();
    }
}
