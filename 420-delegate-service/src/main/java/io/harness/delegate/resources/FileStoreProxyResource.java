package io.harness.delegate.resources;

import com.google.inject.Inject;
import io.harness.annotations.dev.OwnedBy;
import io.harness.data.validator.EntityIdentifier;
import io.harness.filestore.dto.node.FileNodeDTO;
import io.harness.filestore.dto.node.FolderNodeDTO;
import io.harness.filestoreclient.remote.FileStoreNgClient;
import io.harness.ng.core.dto.ErrorDTO;
import io.harness.ng.core.dto.FailureDTO;
import io.harness.ng.core.dto.ResponseDTO;
import io.harness.security.annotations.DelegateAuth;
import io.harness.security.annotations.PublicApi;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.validator.constraints.NotBlank;
import software.wings.security.annotations.AuthRule;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import java.io.IOException;

import static io.harness.NGCommonEntityConstants.ACCOUNT_KEY;
import static io.harness.NGCommonEntityConstants.ACCOUNT_PARAM_MESSAGE;
import static io.harness.NGCommonEntityConstants.FILE_PARAM_MESSAGE;
import static io.harness.NGCommonEntityConstants.IDENTIFIER_KEY;
import static io.harness.NGCommonEntityConstants.ORG_KEY;
import static io.harness.NGCommonEntityConstants.ORG_PARAM_MESSAGE;
import static io.harness.NGCommonEntityConstants.PROJECT_KEY;
import static io.harness.NGCommonEntityConstants.PROJECT_PARAM_MESSAGE;
import static io.harness.annotations.dev.HarnessTeam.CDP;
import static io.harness.network.SafeHttpCall.execute;
import static software.wings.security.PermissionAttribute.PermissionType.DELEGATE;

@OwnedBy(CDP)
@Path("/file-store-proxy")
@Api("/file-store-proxy")
@Produces({"application/json", "application/yaml"})
@Consumes({"application/json", "application/yaml"})
@Tag(name = "File Store", description = "This contains APIs related to File Store in Harness")
@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Bad Request",
        content =
                {
                        @Content(mediaType = "application/json", schema = @Schema(implementation = FailureDTO.class))
                        , @Content(mediaType = "application/yaml", schema = @Schema(implementation = FailureDTO.class))
                })
@io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Internal server error",
        content =
                {
                        @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDTO.class))
                        , @Content(mediaType = "application/yaml", schema = @Schema(implementation = ErrorDTO.class))
                })
@ApiResponses(value =
        {
                @ApiResponse(code = 400, response = FailureDTO.class, message = "Bad Request")
                , @ApiResponse(code = 500, response = ErrorDTO.class, message = "Internal server error")
        })
@AllArgsConstructor(onConstructor = @__({ @Inject}))
@Slf4j
@Hidden
@DelegateAuth
@PublicApi
public class FileStoreProxyResource {

    @Inject
    FileStoreNgClient fileStoreClient;

    @GET
    @Path("files/{identifier}")
    @DelegateAuth
    @ApiOperation(value = "Get file", nickname = "getFile")
    @Operation(operationId = "getFile", summary = "Get File",
            responses =
                    {
                            @io.swagger.v3.oas.annotations.responses.
                                    ApiResponse(responseCode = "default", description = "Get file, optionally with content")
                    })
    public ResponseDTO<FileNodeDTO>
    getFile (
            @Parameter(description = FILE_PARAM_MESSAGE) @PathParam(
                    IDENTIFIER_KEY) @NotBlank @EntityIdentifier String fileIdentifier,
            @Parameter(description = ACCOUNT_PARAM_MESSAGE) @QueryParam(ACCOUNT_KEY) @NotBlank String accountIdentifier,
            @Parameter(description = ORG_PARAM_MESSAGE) @QueryParam(ORG_KEY) String orgIdentifier,
            @Parameter(description = PROJECT_PARAM_MESSAGE) @QueryParam(PROJECT_KEY) String projectIdentifier,
            @Parameter(description = "Include content") @QueryParam("includeContent") Boolean includeContent) throws IOException {

        return execute(fileStoreClient.getFileNg(fileIdentifier, accountIdentifier, orgIdentifier, projectIdentifier, includeContent));
    }

    @GET
    @Path("folders/{identifier}")
    @DelegateAuth
    @ApiOperation(value = "Get folder", nickname = "getFolder")
    @Operation(operationId = "getFolder", summary = "Get Folder",
            responses =
                    {
                            @io.swagger.v3.oas.annotations.responses.
                                    ApiResponse(responseCode = "default", description = "Get folder, optionally with content")
                    })
    public ResponseDTO<FolderNodeDTO>
    getFolder (
            @Parameter(description = FILE_PARAM_MESSAGE) @PathParam(
                    IDENTIFIER_KEY) @NotBlank @EntityIdentifier String folderIdentifier,
            @Parameter(description = ACCOUNT_PARAM_MESSAGE) @QueryParam(ACCOUNT_KEY) @NotBlank String accountIdentifier,
            @Parameter(description = ORG_PARAM_MESSAGE) @QueryParam(ORG_KEY) String orgIdentifier,
            @Parameter(description = PROJECT_PARAM_MESSAGE) @QueryParam(PROJECT_KEY) String projectIdentifier,
            @Parameter(description = "Include content") @QueryParam("includeContent") Boolean includeContent) throws IOException {

        return execute(fileStoreClient.getFolderNg(folderIdentifier, accountIdentifier, orgIdentifier, projectIdentifier, includeContent));
    }

}
