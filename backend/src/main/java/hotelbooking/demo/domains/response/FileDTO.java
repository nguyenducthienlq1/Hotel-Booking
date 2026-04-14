package hotelbooking.demo.domains.response;

import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FileDTO {

    private String fileName;
    private String url;

}
