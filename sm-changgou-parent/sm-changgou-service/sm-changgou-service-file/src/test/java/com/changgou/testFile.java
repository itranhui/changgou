package com.changgou;

import com.changgou.util.FileUitl;
import org.apache.commons.io.IOUtils;
import org.csource.common.MyException;
import org.csource.fastdfs.*;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;

/**
 * @Author：Mr.ran &Date：2019/8/27 16:54
 * <p>
 * @Description：
 */

public class testFile {
    public static void main(String[] args) throws Exception {
        String groupName = "group1";
        String remote_filename = "M00/00/00/wKjThF1k672Ab5XpAA_1V-4otuA656.jpg ";
        //  //文件下载
        //  InputStream inputStream = FileUitl.downloadFile(groupName, remote_filename);

        //  OutputStream outputStream = new FileOutputStream("D:\\22.jpg");
        //  //调用工具类
        //  IOUtils.copy(inputStream, outputStream);


        //删除文件
        // int a =  FileUitl.deleteFile(groupName,remote_filename);
        // System.out.println(a);

        //获取storage信息
        //StorageServer storage = FileUitl.getStorage();
        ////所在的storage的下标
        //System.out.println(storage.getStorePathIndex());
        ////主机地址ip地址
        //System.out.println(storage.getInetSocketAddress().getAddress());
        ////端口号
        //System.out.println(storage.getInetSocketAddress().getPort());

        //System.out.println(storage.getSocket().toString());

        //获取serverInfo信息
        //        ServerInfo[] serverInfo = FileUitl.getServerInfo(groupName, remote_filename);
        //        for (ServerInfo info : serverInfo) {
        //            System.out.println(info.getIpAddr());
        //            System.out.println(info.getPort());
        //        }
        //    }
        //获取TrackerServer服务信息
        //TrackerServer trackerServer = FileUitl.getTrackerServer();
        ////取Storage客户端
        //StorageClient storageClient = FileUitl.getStorageClient();
        //FileInfo file_info = storageClient.get_file_info(groupName, remote_filename);
        //System.out.println(file_info.getCreateTimestamp());
        //System.out.println(file_info.getCrc32());
        //System.out.println(file_info.getFileSize());
        //System.out.println(file_info.getSourceIpAddr());
    }
}
