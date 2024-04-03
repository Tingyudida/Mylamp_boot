package top.tangyh.lamp.file.controller;

import cn.hutool.core.util.StrUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import top.tangyh.basic.annotation.log.WebLog;
import top.tangyh.basic.base.R;
import top.tangyh.basic.utils.ArgumentAssert;
import top.tangyh.lamp.file.properties.FileServerProperties;
import top.tangyh.lamp.file.service.FileService;
import top.tangyh.lamp.file.vo.param.FileUploadVO;
import top.tangyh.lamp.file.vo.result.FileResultVO;

import java.util.List;
import java.util.Map;

import static top.tangyh.basic.exception.code.ExceptionCode.BASE_VALID_PARAM;
import static top.tangyh.lamp.common.constant.SwaggerConstants.DATA_TYPE_MULTIPART_FILE;

/**
 * <p>
 * 前端控制器
 * 增量文件上传日志
 * </p>
 *
 * @author tangyh
 * @date 2021-06-30
 * @create [2021-06-30] [tangyh] [初始创建]
 */
@Slf4j
@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/file/anyone")
@Tag(name = "文件上传")
public class FileAnyoneController {
    private final FileService fileService;
    private final FileServerProperties fileServerProperties;


    /**
     * 上传文件
     *
     * @param file         文件
     * @param fileUploadVO 附件信息
     */
    @Operation(summary = "上传文件", description = "上传文件")
    @Parameters({
            @Parameter(name = "file", description = "附件", schema = @Schema(type = DATA_TYPE_MULTIPART_FILE), in = ParameterIn.QUERY, required = true),
    })
    @PostMapping(value = "/upload")
    @WebLog("上传小文件到租户库")
    public R<FileResultVO> upload(@RequestParam(value = "file") MultipartFile file,
                                  @Validated FileUploadVO fileUploadVO) {
        // 忽略路径字段,只处理文件类型
        if (file.isEmpty()) {
            return R.validFail(BASE_VALID_PARAM.build("请上传有效文件"));
        }

        if (!fileServerProperties.validSuffix(file.getOriginalFilename())) {
            return R.validFail(BASE_VALID_PARAM.build("文件后缀不支持"));
        }
        if (StrUtil.containsAny(file.getOriginalFilename(), "../", "./")) {
            return R.validFail(BASE_VALID_PARAM.build("文件名不能含有特殊字符"));
        }

//        if (ContextUtil.isEmptyTenantId()) {
//            return R.validFail(BASE_VALID_PARAM.build("请携带租户信息"));
//        }
        return R.success(fileService.upload(file, fileUploadVO));
    }

    /**
     * 根据文件id，获取访问路径
     *
     * @param ids 文件id
     */
    @Operation(summary = "根据文件id查询文件的临时访问路径", description = "根据文件id查询文件的临时访问路径")
    @PostMapping(value = "/findUrlById")
    @WebLog("根据文件id，获取文件临时的访问路径")
    public R<Map<Long, String>> findUrlById(@RequestBody List<Long> ids) {
        return R.success(fileService.findUrlById(ids));
    }

    /**
     * 下载一个文件或多个文件打包下载
     *
     * @param ids 文件id
     */
    @Operation(summary = "根据文件id打包下载文件", description = "根据文件id打包下载文件")
    @GetMapping(value = "/download", produces = "application/octet-stream")
    @WebLog("批量下载附件")
    public void download(@RequestParam List<Long> ids, HttpServletRequest request, HttpServletResponse response) throws Exception {
        ArgumentAssert.notEmpty(ids, "请选择至少一个附件");
        fileService.download(request, response, ids);
    }

    /**
     * 根据文件id下载文件
     *
     * @param id 文件id
     */
    @Operation(summary = "根据文件id下载文件", description = "根据文件id下载文件")
    @GetMapping(value = "/down", produces = "application/octet-stream")
    @WebLog("下载附件")
    public void download(@RequestParam Long id, HttpServletRequest request, HttpServletResponse response) throws Exception {
        ArgumentAssert.notNull(id, "请选择至少一个附件");
        fileService.download(request, response, id);
    }
}
