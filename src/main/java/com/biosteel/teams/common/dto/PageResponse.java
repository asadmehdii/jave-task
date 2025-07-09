package com.biosteel.teams.common.dto;

import java.util.List;

import org.springframework.data.domain.Page;

import lombok.Data;

@Data
public class PageResponse<T> {
    private List<T> content;
    private PageMetadata metadata;

    @Data
    public static class PageMetadata {
        private int page;
        private int size;
        private int totalPages;
        private long totalElements;
        private boolean first;
        private boolean last;

        public static PageMetadata from(Page<?> page) {
            PageMetadata metadata = new PageMetadata();
            metadata.setPage(page.getNumber());
            metadata.setSize(page.getSize());
            metadata.setTotalPages(page.getTotalPages());
            metadata.setTotalElements(page.getTotalElements());
            metadata.setFirst(page.isFirst());
            metadata.setLast(page.isLast());
            return metadata;
        }
    }

    public static <T> PageResponse<T> from(Page<T> page) {
        PageResponse<T> response = new PageResponse<>();
        response.setContent(page.getContent());
        response.setMetadata(PageMetadata.from(page));
        return response;
    }
}