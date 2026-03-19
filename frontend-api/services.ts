import apiClient from './api-client.ts';
import * as T from './types.ts';

/**
 * Authentication Service
 */
export const AuthService = {
    login: async (request: T.AuthenticationRequest): Promise<T.ApiResponse<T.JwtResponse>> => {
        const response = await apiClient.post<T.ApiResponse<T.JwtResponse>>('/auth/login', request);
        return response.data;
    },

    googleLogin: async (token: string): Promise<T.ApiResponse<T.JwtResponse>> => {
        const response = await apiClient.post<T.ApiResponse<T.JwtResponse>>('/auth/oauth2/google', { token });
        return response.data;
    },

    facebookLogin: async (token: string): Promise<T.ApiResponse<T.JwtResponse>> => {
        const response = await apiClient.post<T.ApiResponse<T.JwtResponse>>('/auth/oauth2/facebook', { token });
        return response.data;
    },

    register: async (request: T.UserCreationRequest): Promise<T.ApiResponse<T.UserResponse>> => {
        const response = await apiClient.post<T.ApiResponse<T.UserResponse>>('/auth/register', request);
        return response.data;
    }
};

/**
 * Product & Search Service
 */
export const ProductService = {
    getAll: async (page = 0, size = 10): Promise<T.ApiResponse<T.PageResponse<T.ProductResponse>>> => {
        const response = await apiClient.get<T.ApiResponse<T.PageResponse<T.ProductResponse>>>(`/products?page=${page}&size=${size}`);
        return response.data;
    },

    getById: async (id: number): Promise<T.ApiResponse<T.ProductResponse>> => {
        const response = await apiClient.get<T.ApiResponse<T.ProductResponse>>(`/products/${id}`);
        return response.data;
    },

    getBySlug: async (slug: string): Promise<T.ApiResponse<T.ProductResponse>> => {
        const response = await apiClient.get<T.ApiResponse<T.ProductResponse>>(`/products/slug/${slug}`);
        return response.data;
    },

    search: async (keyword: string, page = 0, size = 9): Promise<T.ApiResponse<T.PageResponse<T.ProductResponse>>> => {
        const response = await apiClient.get<T.ApiResponse<T.PageResponse<T.ProductResponse>>>(`/products/search?name=${keyword}&page=${page}&size=${size}`);
        return response.data;
    },

    filter: async (params: Record<string, string>, page = 0, size = 9): Promise<T.ApiResponse<T.PageResponse<T.ProductResponse>>> => {
        const query = new URLSearchParams(params).toString();
        const response = await apiClient.get<T.ApiResponse<T.PageResponse<T.ProductResponse>>>(`/filter?${query}&page=${page}&size=${size}`);
        return response.data;
    }
};

/**
 * Recently Viewed Service
 */
export const RecentlyViewedService = {
    markAsViewed: async (productId: number): Promise<T.ApiResponse<string>> => {
        const response = await apiClient.post<T.ApiResponse<string>>(`/products/${productId}/mark`);
        return response.data;
    },

    getHistory: async (page = 0, size = 10): Promise<T.ApiResponse<T.PageResponse<T.ProductResponse>>> => {
        const response = await apiClient.get<T.ApiResponse<T.PageResponse<T.ProductResponse>>>(`/products/recently-viewed?page=${page}&size=${size}`);
        return response.data;
    },

    sync: async (productIds: number[]): Promise<T.ApiResponse<string>> => {
        const response = await apiClient.post<T.ApiResponse<string>>('/products/viewed-sync', productIds);
        return response.data;
    }
};

/**
 * Blog Service
 */
export const BlogService = {
    getPosts: async (page = 0, size = 10): Promise<T.ApiResponse<T.PageResponse<T.BlogPostResponse>>> => {
        const response = await apiClient.get<T.ApiResponse<T.PageResponse<T.BlogPostResponse>>>(`/blog?page=${page}&size=${size}`);
        return response.data;
    },

    getPostBySlug: async (slug: string): Promise<T.ApiResponse<T.BlogPostResponse>> => {
        const response = await apiClient.get<T.ApiResponse<T.BlogPostResponse>>(`/blog/slug/${slug}`);
        return response.data;
    },

    getCategories: async (): Promise<T.ApiResponse<T.BlogCategoryResponse[]>> => {
        const response = await apiClient.get<T.ApiResponse<T.BlogCategoryResponse[]>>('/blog/categories');
        return response.data;
    }
};
