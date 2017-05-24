Hdfs-over-ftp是基于Apache ftpserver进行二次开发的ftp，后端对接HDFS文件系统。

代码目录：
core
distribution
examples
ftplet-api
hdfs-over-ftp
pom.xml


主要修改的项目：
distribution：用于打包程序的tar.gz部署包

hdfs-over-ftp:主要业务，ftp对接hdfs、监听上传文件并将文件信息写入kafka消息系统等。


其余项目：

1.接口：org.apache.ftpserver.ftplet.FtpRequest增加2个方法：
    void setAttribute(String key ,Object value);
    Object getAttribute(String key);
2.默认实现org.apache.ftpserver.impl.DefaultFtpRequest：
    复写上述两个方法。


3.实现访问kerberized hadoop 方式


