package org.bbqqvv.backendecommerce.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "blog_posts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BlogPost extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, unique = true)
    private String slug;

    @Column(columnDefinition = "TEXT")
    private String summary;

    @Column(columnDefinition = "TEXT")
    private String content;

    private String author;

    private String date;

    private String imageUrl;

    private String readingTime;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private BlogCategory category;

    @Builder.Default
    @ElementCollection
    @CollectionTable(name = "blog_post_tags", joinColumns = @JoinColumn(name = "post_id"))
    @Column(name = "tag")
    private List<String> tags = new ArrayList<>();

    @Builder.Default
    @ElementCollection
    @CollectionTable(name = "blog_post_gallery", joinColumns = @JoinColumn(name = "post_id"))
    @Column(name = "image_url")
    private List<String> gallery = new ArrayList<>();

    @Builder.Default
    @ManyToMany
    @JoinTable(
        name = "blog_post_related_products",
        joinColumns = @JoinColumn(name = "post_id"),
        inverseJoinColumns = @JoinColumn(name = "product_id")
    )
    private List<Product> relatedProducts = new ArrayList<>();
}
