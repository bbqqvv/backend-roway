package org.bbqqvv.backendecommerce.util;

import java.text.Normalizer;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Utility class for generating URL-friendly slugs from strings.
 * Optimized for performance and broad Unicode (especially Vietnamese) support.
 */
public class SlugUtils {

    // Pre-compiled patterns for performance
    private static final Pattern DIACRITICAL_MARKS = Pattern.compile("\\p{M}");
    private static final Pattern NON_ALPHANUMERIC = Pattern.compile("[^a-z0-9\\s-]");
    private static final Pattern WHITESPACE = Pattern.compile("\\s+");
    private static final Pattern MULTIPLE_DASHES = Pattern.compile("-+");
    private static final Pattern TRIM_DASHES = Pattern.compile("^-|-$");

    /**
     * Converts a string into a clean, URL-friendly slug.
     * Handles Vietnamese characters and strips accents using Java Normalizer.
     *
     * @param input The raw string to convert.
     * @return A sanitized slug string.
     */
    public static String toSlug(String input) {
        if (input == null || input.isEmpty()) {
            return "";
        }

        // 1. Manually handle special Vietnamese character 'đ/Đ'
        // Normalizer doesn't decompose 'đ' to 'd' automatically
        String normalized = input.replace("đ", "d").replace("Đ", "d");

        // 2. Normalize to NFD form (decompose accents from base characters)
        normalized = Normalizer.normalize(normalized, Normalizer.Form.NFD);

        // 3. Remove all diacritical marks (accents)
        normalized = DIACRITICAL_MARKS.matcher(normalized).replaceAll("");

        // 4. Lowercase for consistency
        normalized = normalized.toLowerCase(Locale.ENGLISH);

        // 5. Replace spaces with dashes
        normalized = WHITESPACE.matcher(normalized).replaceAll("-");

        // 6. Remove non-alphanumeric characters (except dashes)
        normalized = NON_ALPHANUMERIC.matcher(normalized).replaceAll("");

        // 7. Collapse multiple dashes and trim edge dashes
        normalized = MULTIPLE_DASHES.matcher(normalized).replaceAll("-");
        normalized = TRIM_DASHES.matcher(normalized).replaceAll("");

        return normalized;
    }

    public static void main(String[] args) {
        String input = "Đảm bảo rằng mọi ký tự đều được chuyển về chữ thường chuẩn SEO!";
        System.out.println("Input: " + input);
        System.out.println("Slug : " + toSlug(input));
    }
}
