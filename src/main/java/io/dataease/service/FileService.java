package io.dataease.service;

import io.dataease.base.domain.*;
import io.dataease.base.mapper.FileContentMapper;
import io.dataease.base.mapper.FileMetadataMapper;
import io.dataease.commons.constants.FileType;
import io.dataease.commons.exception.DEException;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.UUID;


@Service
public class FileService {
    @Resource //类似@Autowired
    private FileMetadataMapper fileMetadataMapper;
    @Resource
    private FileContentMapper fileContentMapper;
    public byte[] loadFileAsBytes(String id) {
        FileContent fileContent = fileContentMapper.selectByPrimaryKey(id);

        return fileContent.getFile();
    }

    public FileContent getFileContent(String fileId) {
        return fileContentMapper.selectByPrimaryKey(fileId);
    }

    public void deleteFileByIds(List<String> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            return;
        }
        FileMetadataExample example = new FileMetadataExample();
        example.createCriteria().andIdIn(ids);
        fileMetadataMapper.deleteByExample(example);

        FileContentExample example2 = new FileContentExample();
        example2.createCriteria().andFileIdIn(ids);
        fileContentMapper.deleteByExample(example2);

       /* LoadTestFileExample example3 = new LoadTestFileExample();
        example3.createCriteria().andFileIdIn(ids);
        loadTestFileMapper.deleteByExample(example3);*/
    }

    public void deleteFileRelatedByIds(List<String> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            return;
        }
        FileMetadataExample example = new FileMetadataExample();
        example.createCriteria().andIdIn(ids);
        fileMetadataMapper.deleteByExample(example);

        FileContentExample example2 = new FileContentExample();
        example2.createCriteria().andFileIdIn(ids);
        fileContentMapper.deleteByExample(example2);
    }

    public FileMetadata saveFile(MultipartFile file) {
        return saveFile(file,file.getOriginalFilename());
    }

    public FileMetadata saveFile(MultipartFile file,String fileName) {
        final FileMetadata fileMetadata = new FileMetadata();
        fileMetadata.setId(UUID.randomUUID().toString());
        fileMetadata.setName(fileName);
        fileMetadata.setSize(file.getSize());
        fileMetadata.setCreateTime(System.currentTimeMillis());
        fileMetadata.setUpdateTime(System.currentTimeMillis());
        FileType fileType = getFileType(fileMetadata.getName());
        fileMetadata.setType(fileType.name());
        fileMetadataMapper.insert(fileMetadata);

        FileContent fileContent = new FileContent();
        fileContent.setFileId(fileMetadata.getId());
        try {
            fileContent.setFile(file.getBytes());
        } catch (IOException e) {
            DEException.throwException(e);
        }
        fileContentMapper.insert(fileContent);

        return fileMetadata;
    }

    public FileMetadata saveFile(byte[] fileByte,String fileName,Long fileSize) {
        final FileMetadata fileMetadata = new FileMetadata();
        fileMetadata.setId(UUID.randomUUID().toString());
        fileMetadata.setName(fileName);
        fileMetadata.setSize(fileSize);
        fileMetadata.setCreateTime(System.currentTimeMillis());
        fileMetadata.setUpdateTime(System.currentTimeMillis());
        FileType fileType = getFileType(fileMetadata.getName());
        fileMetadata.setType(fileType.name());
        fileMetadataMapper.insert(fileMetadata);

        FileContent fileContent = new FileContent();
        fileContent.setFileId(fileMetadata.getId());
        fileContent.setFile(fileByte);
        fileContentMapper.insert(fileContent);

        return fileMetadata;
    }

    public FileMetadata copyFile(String fileId) {
        FileMetadata fileMetadata = fileMetadataMapper.selectByPrimaryKey(fileId);
        FileContent fileContent = getFileContent(fileId);
        if (fileMetadata != null && fileContent != null) {
            fileMetadata.setId(UUID.randomUUID().toString());
            fileMetadata.setCreateTime(System.currentTimeMillis());
            fileMetadata.setUpdateTime(System.currentTimeMillis());
            fileMetadataMapper.insert(fileMetadata);

            fileContent.setFileId(fileMetadata.getId());
            fileContentMapper.insert(fileContent);
        }
        return fileMetadata;
    }

    private FileType getFileType(String filename) {
        int s = filename.lastIndexOf(".") + 1;
        String type = filename.substring(s);
        return FileType.valueOf(type.toUpperCase());
    }

    public void deleteFileById(String fileId) {
        deleteFileByIds(Collections.singletonList(fileId));
    }

    //根据id获取文件信息
    public FileMetadata getFileMetadataById(String fileId) {
        return fileMetadataMapper.selectByPrimaryKey(fileId);
    }
}
