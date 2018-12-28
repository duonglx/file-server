package itx.examples.springboot.fileserver.rest;

import itx.examples.springboot.fileserver.services.FileAccessService;
import itx.examples.springboot.fileserver.services.FileService;
import itx.examples.springboot.fileserver.services.OperationNotAllowedException;
import itx.examples.springboot.fileserver.services.SecurityService;
import itx.examples.springboot.fileserver.services.dto.FileList;
import itx.examples.springboot.fileserver.services.dto.RoleId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.Set;

@RestController
@RequestMapping(path = FileServerController.URI_PREFIX)
public class FileServerController {

    private static final Logger LOG = LoggerFactory.getLogger(FileServerController.class);

    public static final String URI_PREFIX = "/services/files";
    public static final String LIST_PREFIX = "/list/";
    public static final String DOWNLOAD_PREFIX = "/download/";
    public static final String UPLOAD_PREFIX = "/upload/";
    public static final String DELETE_PREFIX = "/delete/";
    public static final String CREATEDIR_PREFIX = "/createdir/";

    private final FileService fileService;
    private final FileAccessService fileAccessService;
    private final SecurityService securityService;

    @Autowired
    private HttpServletRequest httpServletRequest;

    @Autowired
    public FileServerController(FileService fileService,
                                FileAccessService fileAccessService, SecurityService securityService) {
        this.fileService = fileService;
        this.fileAccessService = fileAccessService;
        this.securityService = securityService;
    }

    @GetMapping(DOWNLOAD_PREFIX + "**")
    public ResponseEntity<Resource> downloadFile() {
        try {
            String contextPath = httpServletRequest.getRequestURI();
            String sessionId = httpServletRequest.getSession().getId();
            Optional<Set<RoleId>> roles = securityService.getRoles(sessionId);
            if (roles.isPresent()) {
                Path filePath = Paths.get(contextPath.substring((URI_PREFIX + DOWNLOAD_PREFIX).length()));
                LOG.info("downloadFile: {}", filePath);
                Resource resource = fileService.loadFileAsResource(roles.get(), filePath);
                String contentType = "application/octet-stream";
                return ResponseEntity.ok()
                        .contentType(MediaType.parseMediaType(contentType))
                        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                        .body(resource);
            }
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (FileNotFoundException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        } catch (OperationNotAllowedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }

    @GetMapping(LIST_PREFIX + "**")
    public ResponseEntity<FileList> getFiles() {
        try {
            String contextPath = httpServletRequest.getRequestURI();
            String sessionId = httpServletRequest.getSession().getId();
            Optional<Set<RoleId>> roles = securityService.getRoles(sessionId);
            if (roles.isPresent()) {
                Path filePath = Paths.get(contextPath.substring((URI_PREFIX + LIST_PREFIX).length()));
                LOG.info("getFiles: {}", filePath);
                FileList fileInfo = fileService.getFilesInfo(roles.get(), filePath);
                return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(fileInfo);
            }
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        } catch (OperationNotAllowedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }

    @PostMapping(UPLOAD_PREFIX + "**")
    public ResponseEntity<Resource> fileUpload(@RequestParam("file") MultipartFile file) {
        try {
            String contextPath = httpServletRequest.getRequestURI();
            String sessionId = httpServletRequest.getSession().getId();
            Optional<Set<RoleId>> roles = securityService.getRoles(sessionId);
            if (roles.isPresent()) {
                Path filePath = Paths.get(contextPath.substring((URI_PREFIX + UPLOAD_PREFIX).length()));
                LOG.info("upload: {}", filePath);
                fileService.saveFile(roles.get(), filePath, file.getInputStream());
                return ResponseEntity.ok().build();
            }
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        } catch (OperationNotAllowedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }

    @DeleteMapping(DELETE_PREFIX + "**")
    public ResponseEntity<Resource> delete() {
        try {
            String contextPath = httpServletRequest.getRequestURI();
            String sessionId = httpServletRequest.getSession().getId();
            Optional<Set<RoleId>> roles = securityService.getRoles(sessionId);
            if (roles.isPresent()) {
                Path filePath = Paths.get(contextPath.substring((URI_PREFIX + DELETE_PREFIX).length()));
                LOG.info("delete: {}", filePath);
                fileService.delete(roles.get(), filePath);
                return ResponseEntity.ok().build();
            }
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        } catch (OperationNotAllowedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }

    @PostMapping(CREATEDIR_PREFIX + "**")
    public ResponseEntity<Resource> createDirectory() {
        try {
            String contextPath = httpServletRequest.getRequestURI();
            String sessionId = httpServletRequest.getSession().getId();
            Optional<Set<RoleId>> roles = securityService.getRoles(sessionId);
            if (roles.isPresent()) {
                Path filePath = Paths.get(contextPath.substring((URI_PREFIX + CREATEDIR_PREFIX).length()));
                LOG.info("createDirectory: {}", filePath);
                fileService.createDirectory(roles.get(), filePath);
                return ResponseEntity.ok().build();
            }
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        } catch (OperationNotAllowedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }

}
