package com.test.java;

import com.sun.net.httpserver.HttpContext;
import com.test.java.kindeditor.NameComparator;
import com.test.java.kindeditor.SizeComparator;
import com.test.java.kindeditor.TypeComparator;
import org.apache.tomcat.util.http.fileupload.FileItem;
import org.apache.tomcat.util.http.fileupload.FileItemFactory;
import org.apache.tomcat.util.http.fileupload.FileUploadException;
import org.apache.tomcat.util.http.fileupload.disk.DiskFileItemFactory;
import org.apache.tomcat.util.http.fileupload.servlet.ServletFileUpload;
import org.apache.tomcat.util.http.fileupload.servlet.ServletRequestContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.*;

@Controller
@RequestMapping("home")
public class HomeController {

    @Autowired
    private JdbcTemplate jdbc;

    @RequestMapping("/index")
    public String index(HttpServletRequest request) {

//        String sql = "select * from mn_category";
//        List<Map<String, Object>> list = this.jdbc.queryForList(sql);
        return "index";
    }

    @RequestMapping("/upload_json")
    public void upload_json(HttpServletRequest request, HttpServletResponse response) throws FileUploadException {
        //文件保存目录路径
        String savePath = "D:/kindeditor-upload/" + "attached/";

//文件保存目录URL
        String saveUrl = "D:/kindeditor-upload/" + "attached/";

//定义允许上传的文件扩展名
        HashMap<String, String> extMap = new HashMap<String, String>();
        extMap.put("image", "gif,jpg,jpeg,png,bmp");
        extMap.put("flash", "swf,flv");
        extMap.put("media", "swf,flv,mp3,wav,wma,wmv,mid,avi,mpg,asf,rm,rmvb");
        extMap.put("file", "doc,docx,xls,xlsx,ppt,htm,html,txt,zip,rar,gz,bz2");

//最大文件大小
        long maxSize = 1000000;

        response.setContentType("text/html; charset=UTF-8");

        if (!ServletFileUpload.isMultipartContent(request)) {
//            out.println(getError("请选择文件。"));
//            return;
        }
//检查目录
        File uploadDir = new File(savePath);
        if (!uploadDir.isDirectory()) {
//            out.println(getError("上传目录不存在。"));
//            return;
        }
//检查目录写权限
        if (!uploadDir.canWrite()) {
//            out.println(getError("上传目录没有写权限。"));
//            return;
        }

        String dirName = request.getParameter("dir");
        if (dirName == null) {
            dirName = "image";
        }
        if (!extMap.containsKey(dirName)) {
//            out.println(getError("目录名不正确。"));
//            return;
        }
//创建文件夹
        savePath += dirName + "/";
        saveUrl += dirName + "/";
        File saveDirFile = new File(savePath);
        if (!saveDirFile.exists()) {
            saveDirFile.mkdirs();
        }
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
        String ymd = sdf.format(new Date());
        savePath += ymd + "/";
        saveUrl += ymd + "/";
        File dirFile = new File(savePath);
        if (!dirFile.exists()) {
            dirFile.mkdirs();
        }

        FileItemFactory factory = new DiskFileItemFactory();
        ServletFileUpload upload = new ServletFileUpload(factory);
        upload.setHeaderEncoding("UTF-8");
        List items = upload.parseRequest(new ServletRequestContext(request));
        Iterator itr = items.iterator();
        while (itr.hasNext()) {
            FileItem item = (FileItem) itr.next();
            String fileName = item.getName();
            long fileSize = item.getSize();
            if (!item.isFormField()) {
                //检查文件大小
                if (item.getSize() > maxSize) {
//                    out.println(getError("上传文件大小超过限制。"));
                    return;
                }
                //检查扩展名
                String fileExt = fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
                if (!Arrays.<String>asList(extMap.get(dirName).split(",")).contains(fileExt)) {
//                    out.println(getError("上传文件扩展名是不允许的扩展名。\n只允许" + extMap.get(dirName) + "格式。"));
                    return;
                }

                SimpleDateFormat df = new SimpleDateFormat("yyyyMMddHHmmss");
                String newFileName = df.format(new Date()) + "_" + new Random().nextInt(1000) + "." + fileExt;
                try {
                    File uploadedFile = new File(savePath, newFileName);
                    item.write(uploadedFile);
                } catch (Exception e) {
//                    out.println(getError("上传文件失败。"));
                    return;
                }

//                JSONObject obj = new JSONObject();
//                obj.put("error", 0);
//                obj.put("url", saveUrl + newFileName);
//                out.println(obj.toJSONString());
            }
        }
    }

    @RequestMapping("file_manager_json")
    public void file_manager_json(HttpServletRequest request, HttpServletResponse response){
        //根目录路径，可以指定绝对路径，比如 /var/www/attached/
        String rootPath = "D:/kindeditor-upload/"  + "attached/";
//根目录URL，可以指定绝对路径，比如 http://www.yoursite.com/attached/
        String rootUrl  = "D:/kindeditor-upload/" + "/attached/";
//图片扩展名
        String[] fileTypes = new String[]{"gif", "jpg", "jpeg", "png", "bmp"};

        String dirName = request.getParameter("dir");
        if (dirName != null) {
            if(!Arrays.<String>asList(new String[]{"image", "flash", "media", "file"}).contains(dirName)){
//                out.println("Invalid Directory name.");
                return;
            }
            rootPath += dirName + "/";
            rootUrl += dirName + "/";
            File saveDirFile = new File(rootPath);
            if (!saveDirFile.exists()) {
                saveDirFile.mkdirs();
            }
        }
//根据path参数，设置各路径和URL
        String path = request.getParameter("path") != null ? request.getParameter("path") : "";
        String currentPath = rootPath + path;
        String currentUrl = rootUrl + path;
        String currentDirPath = path;
        String moveupDirPath = "";
        if (!"".equals(path)) {
            String str = currentDirPath.substring(0, currentDirPath.length() - 1);
            moveupDirPath = str.lastIndexOf("/") >= 0 ? str.substring(0, str.lastIndexOf("/") + 1) : "";
        }

//排序形式，name or size or type
        String order = request.getParameter("order") != null ? request.getParameter("order").toLowerCase() : "name";

//不允许使用..移动到上一级目录
        if (path.indexOf("..") >= 0) {
//            out.println("Access is not allowed.");
            return;
        }
//最后一个字符不是/
        if (!"".equals(path) && !path.endsWith("/")) {
//            out.println("Parameter is not valid.");
            return;
        }
//目录不存在或不是目录
        File currentPathFile = new File(currentPath);
        if(!currentPathFile.isDirectory()){
//            out.println("Directory does not exist.");
            return;
        }

//遍历目录取的文件信息
        List<Hashtable> fileList = new ArrayList<Hashtable>();
        if(currentPathFile.listFiles() != null) {
            for (File file : currentPathFile.listFiles()) {
                Hashtable<String, Object> hash = new Hashtable<String, Object>();
                String fileName = file.getName();
                if(file.isDirectory()) {
                    hash.put("is_dir", true);
                    hash.put("has_file", (file.listFiles() != null));
                    hash.put("filesize", 0L);
                    hash.put("is_photo", false);
                    hash.put("filetype", "");
                } else if(file.isFile()){
                    String fileExt = fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
                    hash.put("is_dir", false);
                    hash.put("has_file", false);
                    hash.put("filesize", file.length());
                    hash.put("is_photo", Arrays.<String>asList(fileTypes).contains(fileExt));
                    hash.put("filetype", fileExt);
                }
                hash.put("filename", fileName);
                hash.put("datetime", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(file.lastModified()));
                fileList.add(hash);
            }
        }

        if ("size".equals(order)) {
            Collections.sort(fileList, new SizeComparator());
        } else if ("type".equals(order)) {
            Collections.sort(fileList, new TypeComparator());
        } else {
            Collections.sort(fileList, new NameComparator());
        }
//        JSONObject result = new JSONObject();
//        result.put("moveup_dir_path", moveupDirPath);
//        result.put("current_dir_path", currentDirPath);
//        result.put("current_url", currentUrl);
//        result.put("total_count", fileList.size());
//        result.put("file_list", fileList);
//
//        response.setContentType("application/json; charset=UTF-8");
//        out.println(result.toJSONString());
    }
}
