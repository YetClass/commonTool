package com.zhouzifei.tool.media.file.FileClient;


import com.alibaba.fastjson.JSON;
import com.qiniu.common.QiniuException;
import com.qiniu.common.Region;
import com.qiniu.http.Response;
import com.qiniu.storage.BucketManager;
import com.qiniu.storage.Configuration;
import com.qiniu.storage.UploadManager;
import com.qiniu.storage.model.DefaultPutRet;
import com.qiniu.util.Auth;
import com.qiniu.util.StringUtils;
import com.zhouzifei.tool.entity.VirtualFile;
import com.zhouzifei.tool.exception.QiniuApiException;
import com.zhouzifei.tool.media.file.FileUtil;

import java.io.InputStream;
import java.util.Date;

/**
 * Qiniu云操作文件的api：v1
 * @author 周子斐 (17600004572@163.com)
 * @version 1.0
 * @note https://www.zhouzifei.com
 * @remark 2019年7月16日
 * @since 1.0
 */
public class QiniuApiClient extends BaseApiClient {

    private static final String DEFAULT_PREFIX = "qiniu/";

    private String accessKey;
    private String secretKey;
    private String bucket;
    private String path;
    private String pathPrefix;

    public QiniuApiClient() {
        super("七牛云");
    }

    public QiniuApiClient init(String accessKey, String secretKey, String bucketName, String baseUrl, String uploadType) {
        this.accessKey = accessKey;
        this.secretKey = secretKey;
        this.bucket = bucketName;
        this.path = baseUrl;
        this.pathPrefix = StringUtils.isNullOrEmpty(uploadType) ? DEFAULT_PREFIX : uploadType.endsWith("/") ? uploadType : uploadType + "/";
        return this;
    }

    /**
     * 上传图片
     *
     * @param is       图片流
     * @param imageUrl 图片路径
     * @return 上传后的路径
     */
    @Override
    public VirtualFile uploadImg(InputStream is, String imageUrl) {
        this.check();

        String key = FileUtil.generateTempFileName(imageUrl);
        this.createNewFileName(key, this.pathPrefix);
        Date startTime = new Date();
        //Zone.zone0:华东
        //Zone.zone1:华北
        //Zone.zone2:华南
        //Zone.zoneNa0:北美
        Configuration cfg = new Configuration(Region.autoRegion());
        UploadManager uploadManager = new UploadManager(cfg);
        try {
            Auth auth = Auth.create(this.accessKey, this.secretKey);
            String upToken = auth.uploadToken(this.bucket);
            Response response = uploadManager.put(is, this.newFileName, upToken, null, null);

            //解析上传成功的结果
            DefaultPutRet putRet = JSON.parseObject(response.bodyString(), DefaultPutRet.class);

            return new VirtualFile()
                    .setOriginalFileName(key)
                    .setSuffix(this.suffix)
                    .setUploadStartTime(startTime)
                    .setUploadEndTime(new Date())
                    .setFilePath(putRet.key)
                    .setFileHash(putRet.hash)
                    .setFullFilePath(this.path + putRet.key);
        } catch (QiniuException ex) {
            throw new QiniuApiException("[" + this.storageType + "]文件上传失败：" + ex.error());
        }
    }

    /**
     * 删除七牛空间图片方法
     *
     * @param key 七牛空间中文件名称
     */
    @Override
    public boolean removeFile(String key) {
        this.check();

        if (StringUtils.isNullOrEmpty(key)) {
            throw new QiniuApiException("[" + this.storageType + "]删除文件失败：文件key为空");
        }
        Auth auth = Auth.create(this.accessKey, this.secretKey);
        Configuration config = new Configuration(Region.autoRegion());
        BucketManager bucketManager = new BucketManager(auth, config);
        try {
            Response re = bucketManager.delete(this.bucket, key);
            return re.isOK();
        } catch (QiniuException e) {
            Response r = e.response;
            throw new QiniuApiException("[" + this.storageType + "]删除文件发生异常：" + r.toString());
        }
    }

    @Override
    public void check() {
        if (StringUtils.isNullOrEmpty(this.accessKey) || StringUtils.isNullOrEmpty(this.secretKey) || StringUtils.isNullOrEmpty(this.bucket)) {
            throw new QiniuApiException("[" + this.storageType + "]尚未配置七牛云，文件上传功能暂时不可用！");
        }
    }

    public String getPath() {
        return this.path;
    }
}
