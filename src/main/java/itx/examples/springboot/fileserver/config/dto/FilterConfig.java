package itx.examples.springboot.fileserver.config.dto;

import java.util.Arrays;
import java.util.List;

public class FilterConfig {

    private String path;
    private String access;
    private List<String> roles;

    public FilterConfig() {
    }

    public FilterConfig(String path, String access, String ... roles) {
        this.path = path;
        this.access = access;
        this.roles = Arrays.asList(roles);
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getAccess() {
        return access;
    }

    public void setAccess(String access) {
        this.access = access;
    }

    public List<String> getRoles() {
        return roles;
    }

    public void setRoles(List<String> roles) {
        this.roles = roles;
    }
}
