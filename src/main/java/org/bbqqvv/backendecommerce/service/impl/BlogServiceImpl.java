package org.bbqqvv.backendecommerce.service.impl;

import lombok.RequiredArgsConstructor;
import org.bbqqvv.backendecommerce.dto.response.BlogCategoryResponse;
import org.bbqqvv.backendecommerce.dto.response.BlogPostResponse;
import org.bbqqvv.backendecommerce.entity.BlogPost;
import org.bbqqvv.backendecommerce.entity.Product;
import org.bbqqvv.backendecommerce.repository.BlogCategoryRepository;
import org.bbqqvv.backendecommerce.repository.BlogRepository;
import org.bbqqvv.backendecommerce.service.BlogService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BlogServiceImpl implements BlogService {

    private final BlogRepository blogRepository;
    private final BlogCategoryRepository blogCategoryRepository;

    @Override
    public List<BlogCategoryResponse> getAllCategories() {
        return blogCategoryRepository.findAll().stream()
                .map(cat -> BlogCategoryResponse.builder()
                        .id(cat.getId())
                        .name(cat.getName())
                        .slug(cat.getSlug())
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    public List<BlogPostResponse> getAllPosts() {
        return blogRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<BlogPostResponse> getPostsByCategory(String categorySlug) {
        return blogRepository.findByCategorySlug(categorySlug).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public BlogPostResponse getPostBySlug(String slug) {
        return blogRepository.findBySlug(slug)
                .map(this::mapToResponse)
                .orElseThrow(() -> new RuntimeException("Blog post not found with slug: " + slug));
    }

    private BlogPostResponse mapToResponse(BlogPost post) {
        return BlogPostResponse.builder()
                .id(post.getId())
                .title(post.getTitle())
                .slug(post.getSlug())
                .summary(post.getSummary())
                .content(post.getContent())
                .author(post.getAuthor())
                .date(post.getDate())
                .imageUrl(post.getImageUrl())
                .category(post.getCategory().getName())
                .categorySlug(post.getCategory().getSlug())
                .tags(post.getTags())
                .readingTime(post.getReadingTime())
                .gallery(post.getGallery())
                .relatedProducts(post.getRelatedProducts().stream()
                        .map(Product::getId)
                        .collect(Collectors.toList()))
                .build();
    }
}
