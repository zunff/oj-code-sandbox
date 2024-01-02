import java.io.FileDescriptor;
import java.security.Permission;

/**
 * 自定义安全管理器
 */
public class MySecurityManager extends SecurityManager{

    //检查读权限
    @Override
    public void checkRead(String file) {
        //这里先不限制读权限，因为会把读取Java API的操作也限制，需要一个白名单来过滤
    }

    //检查写权限
    @Override
    public void checkWrite(String file) {
        throw new SecurityException("您没有 Write 权限！！请检查代码有无违规操作");
    }

    //检查执行权限
    @Override
    public void checkExec(String cmd) {
        throw new SecurityException("您没有 Exec 权限！！请检查代码有无违规操作");
    }

    //检查连接网络权限
    @Override
    public void checkConnect(String host, int port) {
        throw new SecurityException("您没有 Connect 权限！！请检查代码有无违规操作");
    }
}
