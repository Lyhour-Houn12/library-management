    package com.library.application.payload.response;


    import lombok.AllArgsConstructor;
    import lombok.Data;
    import lombok.NoArgsConstructor;
    import org.springframework.data.domain.Page;

    import java.util.List;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public class PageResponse<T> {
        private List<T> content;
        private Integer pageNumber;
        private Integer pageSize;
        private Long totalElements;
        private Integer totalPages;
        private Boolean firstPage;
        private Boolean lastPage;
        private Boolean empty;

        public static <T> PageResponse<T> from(Page<T> page){
            return new PageResponse<>(
                    page.getContent(),
                    page.getNumber() + 1,
                    page.getSize(),
                    page.getTotalElements(),
                    page.getTotalPages(),
                    page.isFirst(),
                    page.isLast(),
                    page.isEmpty()
            );
        }
    }
