package com.changgou.util;

import com.changgou.file.FastDFSFile;
import org.csource.common.NameValuePair;
import org.csource.fastdfs.*;
import org.springframework.core.io.ClassPathResource;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

/*****
 * @Author: www.itheima.com
 * @Description: com.changgou.util
 * 功能：
 *      1.文件上传
 *      2.文件下载
 *      3.文件删除
 *      4.文件信息获取
 *      5.Tracker信息获取
 *      6.Storage信息获取
 ****/
public class FastDFSClient {

    /****
     * 初始化Tracker信息
     * Tracker->IP
     * Tracker->Port
     */
    static {
        try {
            //1.读取fdfs_client.conf文件   BeanFactory  ApplicationContext
            String path = new ClassPathResource("fdfs_client.conf").getPath();
            //String path = "D:\\project\\workspace68\\changgou\\changgou-parent\\changgou-service\\changgou-service-file\\src\\main\\resources\\fdfs_client.conf";

            //2.初始化加载到指定对象中->FastDFS的对象有关
            ClientGlobal.init(path);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /****
     * 上传文件
     * @param fastdfsFile  :  包含了文件所有信息
     */
    public static String[] upload(FastDFSFile fastdfsFile) throws Exception{
        //自定义属性
        NameValuePair[] meta_list = new NameValuePair[1];
        meta_list[0] = new NameValuePair("设备","华为P30 Pro");

        //获取StorageClient
        StorageClient storageClient = getStorageClient();

        /****
         * 因为Storage信息存储到了Storage的客户端对象中，所以可以通过Storage客户端实现对Storage的访问，例如文件上传
         * 1）要上传的文件内容提供
         * 2）文件的扩展名自
         * 3）自定义属性信息
         * 4：返回内容
         *      1)文件存储的Storage组名
         *      2)文件的详细存储路径
         */
        String[] uploads = storageClient.upload_file(fastdfsFile.getContent(), fastdfsFile.getExt(), meta_list);

        for (String upload : uploads) {
            System.out.println(upload);
        }
        return uploads;
    }


    /****
     * 文件信息获取
     * @param groupName        :  文件的组名
     * @param remoteFileName  :  文件的存储详细名字信息
     */
    public static FileInfo getFile(String groupName, String remoteFileName) throws Exception{
        //获取StorageClient
        StorageClient storageClient = getStorageClient();

        //获取文件信息
        return storageClient.get_file_info(groupName,remoteFileName);
    }

    /****
     * 下载文件信息
     * @param groupName        :  文件的组名
     * @param remoteFileName  :  文件的存储详细名字信息
     */
    public static InputStream downFile(String groupName, String remoteFileName) throws Exception{
        //获取StorageClient
        StorageClient storageClient = getStorageClient();

        //下载文件
        byte[] buffer = storageClient.download_file(groupName, remoteFileName);
        return new ByteArrayInputStream(buffer);
    }


    /****
     * 删除文件信息
     * @param groupName        :  文件的组名
     * @param remoteFileName  :  文件的存储详细名字信息
     */
    public static int deleteFile(String groupName, String remoteFileName) throws Exception{
        //获取StorageClient
        StorageClient storageClient = getStorageClient();

        /***
         * 删除文件
         * 0:success
         * !0:error
         */
        return storageClient.delete_file(groupName,remoteFileName);
    }

    /***
     * 获取Storage组信息
     * @throws Exception
     */
    public static StorageServer getStorage() throws Exception{
        //TrackerClient:Tracker客户端
        TrackerClient trackerClient = new TrackerClient();
        //通过TrackerClient客户端获取连接->会携带Storage信息->Connection
        TrackerServer trackerServer = trackerClient.getConnection();
        return trackerClient.getStoreStorage(trackerServer);
    }


    /****
     * 获取Storage服务信息
     * @throws Exception
     */
    public static ServerInfo[] getServerInfo(String groupName, String remoteFileName) throws Exception{
        //TrackerClient:Tracker客户端
        TrackerClient trackerClient = new TrackerClient();

        //通过TrackerClient客户端获取连接->会携带Storage信息->Connection
        TrackerServer trackerServer = trackerClient.getConnection();

        return trackerClient.getFetchStorages(trackerServer,groupName,remoteFileName);
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


    public static void main(String[] args) throws Exception {
        String groupName = "group1";
        String remoteFileName="M00/00/00/wKjThF1kq7qAQc29AAnAAJuzIB4929.jpg";

        //获取文件信息
        //FileInfo fileInfo = getFile(groupName, remoteFileName);
        //System.out.println("文件大小:"+fileInfo.getFileSize());
        //System.out.println("文件IP:"+fileInfo.getSourceIpAddr());

        //文件下载操作
        //InputStream is = downFile(groupName, remoteFileName);
        //缓冲区
        //byte[] buffer = new byte[1024];
        //写入到D盘1.jpg文件中
        //FileOutputStream os = new FileOutputStream("D:/1.jpg");
        //读文件
        //while (is.read(buffer)!=-1){
        //    os.write(buffer);
        //}
        //关闭资源
        //os.flush();
        //os.close();
        //is.close();


        //文件删除
        //int count = deleteFile(groupName, remoteFileName);
        //System.out.println("受影响行数："+count);

        //Storeage信息获取
        //StorageServer storage = getStorage();
        //System.out.println(storage.getStorePathIndex());

        //获取Storage服务信息
        //ServerInfo[] serverInfos = getServerInfo(groupName, remoteFileName);
        //for (ServerInfo serverInfo : serverInfos) {
        //    System.out.println(serverInfo.getIpAddr()+":"+serverInfo.getPort());
        //}

        //获取Tracker信息
        //TrackerServer trackerServer = getTrackerServer();
        //System.out.println(trackerServer.getInetSocketAddress().getHostString() + ":" + trackerServer.getInetSocketAddress().getPort());
        //System.out.println(ClientGlobal.getG_tracker_http_port());
    }



}
