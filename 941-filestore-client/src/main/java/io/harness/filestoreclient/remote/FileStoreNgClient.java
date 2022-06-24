package io.harness.filestoreclient.remote;

import io.harness.annotations.dev.OwnedBy;
import io.harness.ng.core.dto.ResponseDTO;
import io.harness.filestore.dto.node.FileNodeDTO;
import io.harness.filestore.dto.node.FolderNodeDTO;
import org.hibernate.validator.constraints.NotBlank;
import org.hibernate.validator.constraints.NotEmpty;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

import static io.harness.NGCommonEntityConstants.ACCOUNT_KEY;
import static io.harness.NGCommonEntityConstants.IDENTIFIER_KEY;
import static io.harness.NGCommonEntityConstants.ORG_KEY;
import static io.harness.NGCommonEntityConstants.PROJECT_KEY;
import static io.harness.annotations.dev.HarnessTeam.CDP;

@OwnedBy(CDP)
public interface FileStoreNgClient {
    String FILE_STORE_NG_API = "file-store";

    @GET(FILE_STORE_NG_API + "/files/{identifier}")
    Call<ResponseDTO<FileNodeDTO>> getFileNg(
            @Path(IDENTIFIER_KEY) @NotBlank String identifier,
            @NotEmpty @Query(value = ACCOUNT_KEY) String accountIdentifier,
            @Query(value = ORG_KEY) String orgIdentifier,
            @Query(value = PROJECT_KEY) String projectIdentifier,
            @Query(value = "includeContent") Boolean includeContent);

    @GET(FILE_STORE_NG_API + "/folders/{identifier}")
    Call<ResponseDTO<FolderNodeDTO>> getFolderNg(
            @Path(IDENTIFIER_KEY) @NotBlank String identifier,
            @NotEmpty @Query(value = ACCOUNT_KEY) String accountIdentifier,
            @Query(value = ORG_KEY) String orgIdentifier,
            @Query(value = PROJECT_KEY) String projectIdentifier,
            @Query(value = "includeContent") Boolean includeContent);
}
