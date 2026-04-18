package org.bbqqvv.backendecommerce.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bbqqvv.backendecommerce.dto.PageResponse;
import org.bbqqvv.backendecommerce.dto.request.BlogCategoryRequest;
import org.bbqqvv.backendecommerce.dto.request.BlogPostRequest;
import org.bbqqvv.backendecommerce.dto.request.ImageMetadata;
import org.bbqqvv.backendecommerce.dto.response.BlogCategoryResponse;
import org.bbqqvv.backendecommerce.dto.response.BlogPostResponse;
import org.bbqqvv.backendecommerce.entity.BlogCategory;
import org.bbqqvv.backendecommerce.entity.BlogPost;
import org.bbqqvv.backendecommerce.entity.Product;
import org.bbqqvv.backendecommerce.repository.BlogCategoryRepository;
import org.bbqqvv.backendecommerce.repository.BlogRepository;
import org.bbqqvv.backendecommerce.repository.ProductRepository;
import org.bbqqvv.backendecommerce.service.BlogService;
import org.bbqqvv.backendecommerce.service.img.CloudinaryService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class BlogServiceImpl implements BlogService {

    private final BlogRepository blogRepository;
    private final BlogCategoryRepository blogCategoryRepository;
    private final ProductRepository productRepository;
    private final CloudinaryService cloudinaryService;

    @Override
    @Transactional(readOnly = true)
    public List<BlogCategoryResponse> getAllCategories() {
        return blogCategoryRepository.findAll().stream()
                .map(this::mapCategoryToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public BlogCategoryResponse createCategory(BlogCategoryRequest request) {
        log.info("Creating blog category: {}", request.getName());
        BlogCategory category = BlogCategory.builder()
                .name(request.getName())
                .slug(request.getSlug())
                .build();
        BlogCategory saved = blogCategoryRepository.save(category);
        return mapCategoryToResponse(saved);
    }

    @Override
    @Transactional
    public BlogCategoryResponse updateCategory(Long id, BlogCategoryRequest request) {
        log.info("Updating blog category ID: {}", id);
        BlogCategory category = blogCategoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Blog category not found with id: " + id));
        category.setName(request.getName());
        category.setSlug(request.getSlug());
        BlogCategory saved = blogCategoryRepository.save(category);
        return mapCategoryToResponse(saved);
    }

    @Override
    @Transactional
    public void deleteCategory(Long id) {
        log.info("Deleting blog category ID: {}", id);
        BlogCategory category = blogCategoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Blog category not found with id: " + id));
        blogCategoryRepository.delete(category);
        log.info("Successfully deleted blog category ID: {}", id);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<BlogPostResponse> getAllPosts(Pageable pageable, String search) {
        Page<BlogPostResponse> page;
        if (search != null && !search.isBlank()) {
            page = blogRepository.searchByKeyword(search.trim(), pageable).map(this::mapToResponse);
        } else {
            page = blogRepository.findAll(pageable).map(this::mapToResponse);
        }
        return new PageResponse<>(
                page.getNumber(),
                page.getTotalPages(),
                page.getSize(),
                page.getTotalElements(),
                page.getContent()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<BlogPostResponse> getPostsByCategory(String categorySlug) {
        return blogRepository.findByCategorySlug(categorySlug).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public BlogPostResponse getPostBySlug(String slug) {
        return blogRepository.findBySlug(slug)
                .map(this::mapToResponse)
                .orElseThrow(() -> new RuntimeException("Blog post not found with slug: " + slug));
    }

    @Override
    @Transactional
    public BlogPostResponse createPost(BlogPostRequest request) {
        log.info("Creating new blog post: {}", request.getTitle());

        BlogCategory category = blogCategoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Blog category not found with id: " + request.getCategoryId()));

        // Tự động gán ngày hiện tại nếu request.date null/rỗng
        String postDate = (request.getDate() != null && !request.getDate().isBlank()) 
                            ? request.getDate() 
                            : LocalDate.now().toString();

        BlogPost post = BlogPost.builder()
                .title(request.getTitle())
                .slug(request.getSlug())
                .summary(request.getSummary())
                .content(request.getContent())
                .author(request.getAuthor())
                .date(postDate)
                .readingTime(request.getReadingTime())
                .category(category)
                .tags(request.getTags() != null ? request.getTags() : List.of())
                .gallery(request.getGallery() != null ? request.getGallery() : List.of())
                .build();

        if (request.getRelatedProductIds() != null && !request.getRelatedProductIds().isEmpty()) {
            List<Product> products = productRepository.findAllById(request.getRelatedProductIds());
            post.setRelatedProducts(products);
        }

        // Handle image
        if (request.getImageUrl() != null && !request.getImageUrl().isBlank()) {
            post.setImageUrl(request.getImageUrl());
        }

        BlogPost saved = blogRepository.save(post);
        log.info("Blog post saved with ID: {}", saved.getId());
        return mapToResponse(saved);
    }

    @Override
    @Transactional
    public BlogPostResponse updatePost(Long id, BlogPostRequest request) {
        log.info("Updating blog post ID: {}", id);

        BlogPost post = blogRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Blog post not found with id: " + id));

        BlogCategory category = blogCategoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Blog category not found with id: " + request.getCategoryId()));

        // Cập nhật các trường cơ bản
        post.setTitle(request.getTitle());
        post.setSlug(request.getSlug());
        post.setSummary(request.getSummary());
        post.setContent(request.getContent());
        post.setAuthor(request.getAuthor());
        post.setReadingTime(request.getReadingTime());
        post.setCategory(category);

        if (request.getDate() != null && !request.getDate().isBlank()) {
            post.setDate(request.getDate());
        }
        if (request.getTags() != null) {
            post.setTags(request.getTags());
        }
        if (request.getGallery() != null) {
            post.setGallery(request.getGallery());
        }

        if (request.getRelatedProductIds() != null) {
            List<Product> products = productRepository.findAllById(request.getRelatedProductIds());
            post.setRelatedProducts(products);
        }

        // Cập nhật ảnh bìa mới nếu có
        if (request.getImageUrl() != null && !request.getImageUrl().isBlank()) {
            post.setImageUrl(request.getImageUrl());
        }

        BlogPost updatedPost = blogRepository.save(post);
        log.info("Successfully updated blog post ID: {}", updatedPost.getId());
        
        return mapToResponse(updatedPost);
    }

    @Override
    @Transactional
    public void deletePost(Long id) {
        log.info("Deleting blog post ID: {}", id);
        BlogPost post = blogRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Blog post not found with id: " + id));
        
        blogRepository.delete(post);
        log.info("Successfully deleted blog post ID: {}", id);
    }

    // ==========================================
    // PRIVATE HELPER METHODS
    // ==========================================

    private BlogCategoryResponse mapCategoryToResponse(BlogCategory cat) {
        return BlogCategoryResponse.builder()
                .id(cat.getId())
                .name(cat.getName())
                .slug(cat.getSlug())
                .build();
    }

    /**
     * Map Entity -> DTO Response
     */
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
                // Tránh NullPointerException bằng cách check category
                .category(post.getCategory() != null ? post.getCategory().getName() : null)
                .categorySlug(post.getCategory() != null ? post.getCategory().getSlug() : null)
                .tags(post.getTags())
                .readingTime(post.getReadingTime())
                .gallery(post.getGallery())
                .relatedProducts(post.getRelatedProducts() != null 
                        ? post.getRelatedProducts().stream().map(Product::getId).collect(Collectors.toList())
                        : List.of())
                .build();
    }

    @Override
    @Transactional
    public void seedDummyBlogs(int count) {
        String[] catNames = {"Phong Cách", "Xu Hướng", "Phối Đồ", "Tin Tức"};
        List<BlogCategory> categories = new java.util.ArrayList<>();
        for (String cName : catNames) {
            String cSlug = org.bbqqvv.backendecommerce.util.SlugUtils.toSlug(cName);
            BlogCategory c = blogCategoryRepository.findBySlug(cSlug).orElse(null);
            if (c == null) {
                c = blogCategoryRepository.save(BlogCategory.builder().name(cName).slug(cSlug).build());
            }
            categories.add(c);
        }

        String[] titles = {
            "Cách phối đồ chuẩn soái ca Hàn Quốc",
            "Xu hướng thời trang Thu Đông 2024",
            "Bí quyết chọn quần jean đúng form",
            "5 đôi giày sneaker nam không bao giờ lỗi thời",
            "Lựa chọn áo sơ mi đi làm lịch lãm",
            "Mẹo giữ màu quần áo luôn như mới",
            "Cách chọn cà vạt phù hợp với dáng người",
            "Tủ đồ con nhộng (Capsule Wardrobe) cho nam giới"
        };

        String[] tags = {"Style", "Men", "Fashion", "News", "Trend", "Tips"};

        java.util.Random random = new java.util.Random();
        List<BlogPost> postsToSave = new java.util.ArrayList<>();

        for (int i = 1; i <= count; i++) {
            BlogCategory randomCat = categories.get(random.nextInt(categories.size()));
            String title = titles[random.nextInt(titles.length)] + " - " + java.util.UUID.randomUUID().toString().substring(0, 4);
            
            BlogPost post = BlogPost.builder()
                .title(title)
                .slug(org.bbqqvv.backendecommerce.util.SlugUtils.toSlug(title))
                .summary("Một bài viết tóm tắt về " + title + ". Giúp bạn cải thiện gu thời trang và nắm bắt xu hướng mới nhất.")
                .content("<p>Đây là nội dung chi tiết của bài viết <strong>" + title + "</strong>.</p><p>Thời trang là một trong những yếu tố giúp nam giới tự tin hơn trong cuộc sống. Trong bài viết này, chúng ta sẽ cùng tìm hiểu cách làm sao để luôn xuất hiện với diện mạo hoàn hảo nhất.</p><img src=\"https://res.cloudinary.com/demo/image/upload/v1312461204/sample.jpg\" alt=\"Sample Image\" />")
                .author(random.nextBoolean() ? "Admin" : "Fashion Editor")
                .date(java.time.LocalDate.now().minusDays(random.nextInt(30)).toString())
                .readingTime(String.valueOf(2 + random.nextInt(8)))
                .imageUrl("https://images.unsplash.com/photo-1516257984-b1b4d707412e?auto=format&fit=crop&w=800&q=80")
                .category(randomCat)
                .tags(List.of(tags[random.nextInt(tags.length)], tags[random.nextInt(tags.length)]))
                .build();
            
            postsToSave.add(post);
        }

        blogRepository.saveAll(postsToSave);
        log.info("✅ Successfully seeded {} blog posts.", count);
    }
}
