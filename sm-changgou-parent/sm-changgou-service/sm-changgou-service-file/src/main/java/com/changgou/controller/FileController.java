package com.changgou.controller;

import com.changgou.file.FastDFSFile;
import com.changgou.util.FileUitl;
import com.thoughtworks.xstream.core.util.FastField;
import org.csource.common.MyException;
import org.csource.fastdfs.ClientGlobal;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * @Author：Mr.ran &Date：2019/8/27 15:53
 * <p>
 * @Description： 文件上传
 */


@RestController
public class FileController {

    @PostMapping("/upload")
    public String upload(@RequestParam(value = "file") MultipartFile multipartFile) throws IOException, MyException {

        //获取文件的 内容
        byte[] bytes = multipartFile.getBytes();

        //获取文件名
        String filename = multipartFile.getOriginalFilename();

        //System.out.println(multipartFile.getName());// 获取的不是文件名 而是

        System.out.println(multipartFile.getOriginalFilename());
        //获取后缀名 getFilenameExtension()
        String extensionName = StringUtils.getFilenameExtension(filename);

        FastDFSFile fastDFSFile = new FastDFSFile(filename, bytes, extensionName);

        //调用文件上传的方法 返回的数组中有 文件 的组名，
        String[] upload = FileUitl.upload(fastDFSFile);
        //
        String url = "http://192.168.211.132:8811/" + upload[0] + "/" + upload[1];
        return url;
    }

}
