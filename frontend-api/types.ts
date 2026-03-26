/**
 * ==========================================================
 * ENUMS
 * ==========================================================
 */
export type OrderStatus =
  | 'PENDING'
  | 'CONFIRMED'
  | 'PROCESSING'
  | 'SHIPPED'
  | 'DELIVERED'
  | 'CANCELLED'
  | 'RETURNED'
  | 'FAILED';

export type PaymentMethod = 'CASH_ON_DELIVERY' | 'VNPAY' | 'STRIPE';

export type PaymentStatus = 'PENDING' | 'COMPLETED' | 'FAILED' | 'REFUNDED';

export type DiscountType = 'FIXED_AMOUNT' | 'PERCENTAGE';

/**
 * ==========================================================
 * GENERIC API WRAPPERS
 * ==========================================================
 */
export interface ApiResponse<T> {
  code: number;
  success: boolean;
  message: string;
  data: T;
  details?: any;
}

export interface PageResponse<T> {
  items: T[];
  currentPage: number;
  pageSize: number;
  totalPages: number;
  totalElements: number;
}

/**
 * ==========================================================
 * AUTHENTICATION & USER – RESPONSES
 * ==========================================================
 */
export interface JwtResponse {
  token: string;
  refreshToken: string;
}

export interface AuthenticationResponse {
  token: string;
  refreshToken: string;
}

export interface UserResponse {
  id: number;
  username: string;
  name: string;
  email: string;
  avatar?: string;
  phoneNumber?: string;
  bio?: string;
  authorities: RoleResponse;
  createdAt: string;
  updatedAt: string;
}

export interface RoleResponse {
  role: string;
}

export interface UserUpdateResponse {
  name: string;
  bio: string;
}

export interface AddressResponse {
  id: number;
  userId: number;
  recipientName: string;
  country: string;
  province: string;
  district: string;
  commune: string;
  note: string;
  addressLine: string;
  email: string;
  phoneNumber: string;
  defaultAddress: boolean;
}

export interface OtpResponse {
  message: string;
  email: string;
}

export interface OtpVerificationResponse {
  message: string;
  success: boolean;
}

export interface ResetPasswordResponse {
  message: string;
  success: boolean;
}

/**
 * ==========================================================
 * PRODUCT & CATEGORY – RESPONSES
 * ==========================================================
 */
export interface ProductResponse {
  id: number;
  name: string;
  slug: string;
  shortDescription: string;
  description: string;
  productCode: string;
  featured: boolean;
  sale: boolean;
  active: boolean;
  reviewCount: number;
  salePercentage: number;
  tags: string[];
  categoryName: string;
  mainImageUrl: string;
  mainImagePublicId?: string;
  secondaryImageUrls: string[];
  secondaryImagePublicIds?: string[];
  descriptionImageUrls: string[];
  descriptionImagePublicIds?: string[];
  variants: ProductVariantResponse[];
  createdAt: string;
  updatedAt: string;
}

export interface ProductVariantResponse {
  id: number;
  sizes: SizeProductResponse[];
  color: string;
  imageUrl?: string;
  publicId?: string;
}

export interface SizeProductResponse {
  sizeName: string;
  stock: number;
  price: number;
  priceAfterDiscount: number;
}

export interface CategoryResponse {
  id: number;
  name: string;
  slug: string;
  image?: string;
  publicId?: string;
  sizes: SizeCategoryResponse[];
  createdAt: string;
  updatedAt: string;
}

export interface SizeCategoryResponse {
  id: number;
  name: string;
  categoryId: number;
}

export interface CategoryResponseForFilter {
  id: number;
  name: string;
  slug: string;
}

export interface FilterOptionsResponse {
  categories: string[];
  tags: string[];
  colors: string[];
  sizes: string[];
  minPrice: number;
  maxPrice: number;
}

/**
 * ==========================================================
 * CART & ORDER – RESPONSES
 * ==========================================================
 */
export interface CartResponse {
  id: number;
  userId: number;
  cartItems: CartItemResponse[];
  totalPrice: number;
}

export interface CartItemResponse {
  id: number;
  productId: number;
  productName: string;
  mainImageUrl: string;
  quantity: number;
  color: string;
  sizeName: string;
  price: number;
  subtotal: number;
  stock: number;
  inStock: boolean;
}

export interface OrderResponse {
  id: number;
  userId: number;
  name: string;
  address: string;
  phoneNumber: string;
  notes: string;
  orderCode: string;
  status: OrderStatus;
  paymentMethod: PaymentMethod;
  shippingFee: number;
  discountCode?: string;
  discountAmount: number;
  expectedDeliveryDate?: string;
  orderItems: OrderItemResponse[];
  totalAmount: number;
  paymentUrl?: string;
  createdAt: string;
  updatedAt: string;
}

export interface OrderItemResponse {
  productId: number;
  productName: string;
  mainImageUrl: string;
  color: string;
  sizeName: string;
  quantity: number;
  price: number;
  subtotal: number;
}

/**
 * ==========================================================
 * BLOG – RESPONSES
 * ==========================================================
 */
export interface BlogPostResponse {
  id: number;
  title: string;
  slug: string;
  summary: string;
  content: string;
  author: string;
  date: string;
  imageUrl: string;
  category: string;
  categorySlug?: string;
  tags?: string[];
  readingTime?: string;
  gallery?: string[];
  relatedProducts?: number[];
}

export interface BlogCategoryResponse {
  id: number;
  name: string;
  slug: string;
}

/**
 * ==========================================================
 * DISCOUNT – RESPONSES
 * ==========================================================
 */
export interface DiscountResponse {
  id: number;
  code: string;
  discountAmount: number;
  maxDiscountAmount: number;
  discountType: DiscountType;
  minOrderValue: number;
  usageLimit: number;
  timesUsed: number;
  startDate: string;
  expiryDate: string;
  active: boolean;
  applicableProductsCount: number;
  applicableUsersCount: number;
  createdAt: string;
  updatedAt: string;
}

export interface DiscountPreviewResponse {
  discountCode: string;
  discountType: DiscountType;
  originalTotalAmount: number;
  discountAmount: number;
  finalAmount: number;
  valid: boolean;
  message: string;
}

/**
 * ==========================================================
 * MISCELLANEOUS – RESPONSES
 * ==========================================================
 */
export interface FavouriteResponse {
  id: number;
  userId: number;
  nameProduct: string;
  imageUrl: string;
  productUrl: string;
  stockStatus: string;
  price: number;
}

export interface ProductReviewResponse {
  id: number;
  productId: number;
  orderItemId: number;
  productName: string;
  userId: number;
  userName: string;
  rating: number;
  reviewText: string;
  imageUrl?: string;
  createdAt: string;
}

export interface SearchHistoryResponse {
  id: number;
  searchQuery: string;
  createdAt: string;
}

export interface SupportItemResponse {
  id: number;
  img: string;
  title: string;
  hours: string;
  contact: string;
  link: string;
  bgColor: string;
}

/**
 * ==========================================================
 * AUTHENTICATION – REQUESTS
 * ==========================================================
 */
export interface AuthenticationRequest {
  username: string;
  password?: string;
}

export interface OAuth2LoginRequest {
  token: string;
}

export interface UserCreationRequest {
  username: string;
  password?: string;
  email: string;
  name?: string;
}

export interface RefreshTokenRequest {
  refreshToken: string;
}

export interface OtpRequest {
  email: string;
}

export interface OtpVerificationRequest {
  email: string;
  otp: string;
}

export interface ResetPasswordRequest {
  email: string;
  newPassword: string;
}

export interface ChangePasswordRequest {
  oldPassword: string;
  newPassword: string;
  confirmPassword: string;
}

/**
 * ==========================================================
 * USER & ADDRESS – REQUESTS
 * ==========================================================
 */
export interface UserUpdateRequest {
  name: string;
  bio: string;
}

export interface AddressRequest {
  recipientName: string;
  country: string;
  province: string;
  district: string;
  commune: string;
  addressLine: string;
  phoneNumber: string;
  email: string;
  defaultAddress: boolean;
  note?: string;
}

/**
 * ==========================================================
 * CART – REQUESTS
 * ==========================================================
 */
export interface CartItemRequest {
  productId: number;
  quantity: number;
  sizeName?: string;
  color?: string;
}

/**
 * ==========================================================
 * ORDER – REQUESTS
 * ==========================================================
 */
export interface OrderRequest {
  addressId: number;
  cartId: number;
  discountCode?: string;
  paymentMethod: PaymentMethod;
}

export interface OrderItemRequest {
  productId: number;
  quantity: number;
  color: string;
  sizeName: string;
}

/**
 * ==========================================================
 * PRODUCT & REVIEW – REQUESTS
 * ==========================================================
 */
export interface ProductReviewRequest {
  productId: number;
  rating: number;
  reviewText: string;
  imageFiles?: File[];
}

export interface FavouriteRequest {
  productId: number;
  sizeProductVariantId: number;
}

export interface ImageMetadata {
  publicId?: string;
  url?: string;
}

export interface SizeProductRequest {
  sizeName: string;
  stock: number;
  price?: number;
}

export interface ProductVariantRequest {
  image?: File;
  imageMetadata?: ImageMetadata;
  sizes: SizeProductRequest[];
  color: string;
  price?: number;
  stock?: number;
}

export interface ProductRequest {
  name: string;
  shortDescription?: string;
  description?: string;
  productCode: string;
  categoryId: number;
  salePercentage?: number;
  featured?: boolean;
  price?: number;
  tags?: string[];
  sale?: boolean;
  active?: boolean;
  isOldProduct?: boolean;
  mainImage?: File;
  mainImageMetadata?: ImageMetadata;
  secondaryImages?: File[];
  secondaryImageMetadata?: ImageMetadata[];
  descriptionImages?: File[];
  descriptionImageMetadata?: ImageMetadata[];
  variants?: ProductVariantRequest[];
}

/**
 * ==========================================================
 * DISCOUNT – REQUESTS
 * ==========================================================
 */
export interface DiscountPreviewRequest {
  cartId: number;
  discountCode: string;
}

/**
 * ==========================================================
 * SEARCH – REQUESTS
 * ==========================================================
 */
export interface SearchHistoryRequest {
  searchQuery: string;
}
