package com.changgou.util;

import com.changgou.file.FastDFSFile;
import org.csource.common.MyException;
import org.csource.fastdfs.*;
import org.springframework.core.io.ClassPathResource;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * @Author：Mr.ran &Date：2019/8/27 16:05
 * <p>
 * @Description：
 */

public class FileUitl {
    /***
     * 初始化tracker信息
     */
    static {
        try {
            //获取tracker的配置文件fdfs_client.conf的位置
            String filePath = new ClassPathResource("fdfs_client.conf").getPath();
            //加载tracker配置信息
            ClientGlobal.init(filePath);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /****
     * 文件上传
     * @param fastDFSFile
     */
    public static String[] upload(FastDFSFile fastDFSFile) throws IOException, MyException {
        //创建 TrackerClient 对象
        TrackerClient trackerClient = new TrackerClient();
        //通过 TrackerClient对象 获取到连接 会携带 Storage 消息  信息--->连接
        TrackerServer trackerClientConnection = trackerClient.getConnection();
        //通过TrackerServer的连接对象获取Storage服务端信息，创建一个Storage客户端对象 储存Storage服务信息
        StorageClient storageClient = new StorageClient(trackerClientConnection, null);


        //获取文件内容(字节数组)
        byte[] fileContent = fastDFSFile.getContent();

        //获取文件的后缀名
        String ext = fastDFSFile.getExt();
        /****
         * 因为Storage信息存储到了Storage的客户端对象中，所以可以通过Storage客户端实现对Storage的访问，例如文件上传
         * 1）要上传的文件内容提供
         * 2）文件的扩展名自
         * 3）自定义属性信息
         * 4：返回内容
         *      1)文件存储的Storage组名
         *      2)文件的详细存储路径
         */
        String[] uploadFile = storageClient.upload_file(fileContent, ext, null);

        for (String s : uploadFile) {
            System.out.println(s);
        }
        return uploadFile;
    }

    /****
     * 文件下载
     * @throws IOException
     */
    public static InputStream downloadFile(String groupName, String remote_filename) throws IOException, MyException {
        TrackerClient trackerClient = new TrackerClient();
        //获取到 TrackerClient 获取到  trackerServer服务
        TrackerServer trackerServer = trackerClient.getConnection();

        //创建StorageServer
        StorageClient storageClient = new StorageClient(trackerServer, null);

        //下载文件 par1:组名  ； par2: 最终文件储存的名称 返回文件字节数组
        byte[] bytes = storageClient.download_file(groupName, remote_filename);

        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);

        return byteArrayInputStream;
    }

    /****
     * 下载文件信息
     * @param groupName        :  文件的组名
     * @param remoteFileName  :  文件的存储详细名字信息
     */
    public static int deleteFile(String groupName, String remoteFileName) throws IOException, MyException {
        TrackerClient trackerClient = new TrackerClient();
        //获取到TrackerServer
        TrackerServer trackerServer = trackerClient.getConnection();

        //获取到StorageClient

        StorageClient storageClient = new StorageClient(trackerServer, null);
        //删除文件 0表示成功
        int delete_file = storageClient.delete_file(groupName, remoteFileName);

        return delete_file;
    }

    /***
     * 获取Storage组信息
     * @throws Exception
     */
    public static StorageServer getStorage() throws Exception {
        TrackerClient trackerClient = new TrackerClient();
        //通过TrackerClient客户端获取连接->会携带Storage信息->Connection
        TrackerServer trackerServer = trackerClient.getConnection();
        StorageServer storeStorage  =  trackerClient.getStoreStorage(trackerServer);
        return storeStorage;
    }


    /****
     * 获取Storage服务信息
     * @throws Exception
     */
    public static ServerInfo[] getServerInfo(String groupName, String remoteFileName) throws Exception {
        TrackerClient trackerClient = new TrackerClient();
        TrackerServer trackerServer = trackerClient.getConnection();
        return  trackerClient.getFetchStorages(trackerServer,groupName,remoteFileName);
    }

    /****
     * 获取TrackerServer服务信息
     * @throws Exception
     */
    public static TrackerServer getTrackerServer() throws Exception{
        //TrackerClient:Tracker客户端
        TrackerClient trackerClient = new TrackerClient();

        //通过TrackerClient客户端获取连接->会携带Storage信息->Connection
        TrackerServer trackerServer = trackerClient.getConnection();

        //获取Tracker
        return trackerServer;
    }

    /****
     * 获取Storage客户端
     * @throws Exception
     */
    public static StorageClient getStorageClient() throws Exception{
        //TrackerClient:Tracker客户端
        TrackerClient trackerClient = new TrackerClient();

        //通过TrackerClient客户端获取连接->会携带Storage信息->Connection
        TrackerServer trackerServer = trackerClient.getConnection();

        //通过Tracker的链接对象获取Storage服务端信息,创建一个Storage客户端对象存储Storage服务端信息
        StorageClient storageClient = new StorageClient(trackerServer, null);
        return storageClient;
    }

}




