package org.bbqqvv.backendecommerce.service.impl;

import org.bbqqvv.backendecommerce.exception.codes.*;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.bbqqvv.backendecommerce.config.jwt.SecurityUtils;
import org.bbqqvv.backendecommerce.dto.PageResponse;
import org.bbqqvv.backendecommerce.dto.request.ChangePasswordRequest;
import org.bbqqvv.backendecommerce.dto.request.UserCreationRequest;
import org.bbqqvv.backendecommerce.dto.request.UserUpdateRequest;
import org.bbqqvv.backendecommerce.dto.response.UserResponse;
import org.bbqqvv.backendecommerce.dto.response.UserUpdateResponse;
import org.bbqqvv.backendecommerce.entity.User;
import org.bbqqvv.backendecommerce.exception.AppException;
import org.bbqqvv.backendecommerce.exception.ErrorCode;
import org.bbqqvv.backendecommerce.mapper.UserMapper;
import org.bbqqvv.backendecommerce.repository.UserRepository;
import org.bbqqvv.backendecommerce.service.UserService;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.bbqqvv.backendecommerce.service.img.CloudinaryService;

import static org.bbqqvv.backendecommerce.util.PagingUtil.toPageResponse;

@Service
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class UserServiceImpl implements UserService {
    UserRepository userRepository;
    UserMapper userMapper;
    PasswordEncoder passwordEncoder;
    CloudinaryService cloudinaryService;

    public UserServiceImpl(UserRepository userRepository, UserMapper userMapper, PasswordEncoder passwordEncoder, CloudinaryService cloudinaryService) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
        this.cloudinaryService = cloudinaryService;
    }

    @Override
    @Transactional
    public UserResponse createUser(UserCreationRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new AppException(UserErrorCode.USER_EXISTED);
        }
        
        // Map RegisterUserRequest -> User entity
        User user = userMapper.toUser(request);
        
        // Encode password before saving
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        // Lưu vào DB
        User savedUser = userRepository.save(user);

        // Map User entity -> UserResponse
        return userMapper.toUserResponse(savedUser);
    }

    @Cacheable(value = "users", key = "#id")
    @Override
    public UserResponse getUserById(Long id) {
        User user = userRepository.findById(id).orElseThrow(() ->
                new AppException(UserErrorCode.USER_NOT_FOUND)
        );
        return userMapper.toUserResponse(user);
    }
    @Override
    public User getUserByUsernameEntity(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException(UserErrorCode.USER_NOT_FOUND));
    }
    @Cacheable(value = "users", key = "'allUsers'")
    @Override
    public PageResponse<UserResponse> getAllUsers(Pageable pageable) {
        Page<User> userPage = userRepository.findAll(pageable);
        return toPageResponse(userPage, userMapper::toUserResponse);
    }
    @Override
    @Transactional
    public UserResponse updateUser(Long id, UserCreationRequest request) {
        // Kiểm tra người dùng có tồn tại không
        User user = userRepository.findById(id).orElseThrow(() ->
                new AppException(UserErrorCode.USER_NOT_FOUND)
        );

        // Cập nhật thông tin từ DTO
        user.setUsername(request.getUsername());
        if (request.getPassword() != null && !request.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }
        user.setEmail(request.getEmail());

        // Lưu lại
        User updatedUser = userRepository.save(user);

        // Map User entity -> UserResponse
        return userMapper.toUserResponse(updatedUser);
    }




    @Override
    @Transactional
    public void deleteUser(Long id) {
        // Kiểm tra người dùng có tồn tại không trước khi xóa
        User user = userRepository.findById(id).orElseThrow(() ->
                new AppException(UserErrorCode.USER_NOT_FOUND)
        );

        // Prevent deleting an ADMIN
        if (user.getAuthorities() != null && user.getAuthorities().contains(org.bbqqvv.backendecommerce.entity.Role.ROLE_ADMIN)) {
            throw new AppException(UserErrorCode.UNAUTHORIZED);
        }

        // Xóa người dùng
        userRepository.deleteById(id);
    }

    @Override
    public boolean existsByUsername(String username) {
        // Kiểm tra xem username đã tồn tại trong cơ sở dữ liệu chưa
        return userRepository.existsByUsername(username);
    }

    @Override
    @Transactional
    public UserResponse updateUserInfo(UserUpdateRequest request) {
        String username = SecurityUtils.getCurrentUserLogin()
                .orElseThrow(() -> new AppException(CommonErrorCode.UNAUTHENTICATED));

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException(UserErrorCode.USER_NOT_FOUND));

        // Cập nhật name và bio nếu có
        if (request.getName() != null) {
            user.setName(request.getName());
        }
        if (request.getBio() != null) {
            user.setBio(request.getBio());
        }
        if (request.getPhoneNumber() != null) {
            user.setPhoneNumber(request.getPhoneNumber());
        }
        if (request.getAvatar() != null && !request.getAvatar().isEmpty()) {
            org.bbqqvv.backendecommerce.dto.request.ImageMetadata metadata = cloudinaryService.uploadImage(request.getAvatar());
            user.setAvatar(metadata.getUrl());
        }

        userRepository.save(user);
        return userMapper.toUserResponse(user);
    }

    @Override
    public void changePassword(ChangePasswordRequest request) {
        String username = SecurityUtils.getCurrentUserLogin()
                .orElseThrow(() -> new AppException(CommonErrorCode.UNAUTHENTICATED));

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException(UserErrorCode.USER_NOT_FOUND));

        // Kiểm tra mật khẩu cũ
        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            throw new AppException(UserErrorCode.INVALID_OLD_PASSWORD);
        }

        // Kiểm tra mật khẩu mới và xác nhận mật khẩu có khớp nhau không
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new AppException(UserErrorCode.PASSWORDS_DO_NOT_MATCH);
        }

        // Mã hóa mật khẩu mới
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }

    @Override
    public UserResponse getCurrentUser() {
        String username = SecurityUtils.getCurrentUserLogin()
                .orElseThrow(() -> new AppException(CommonErrorCode.UNAUTHENTICATED));

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException(UserErrorCode.USER_NOT_FOUND));

        return userMapper.toUserResponse(user);
    }

    @Override
    @Transactional
    public UserResponse updateRole(Long id, org.bbqqvv.backendecommerce.dto.request.RoleUpdateRequest request) {
        User user = userRepository.findById(id).orElseThrow(() ->
                new AppException(UserErrorCode.USER_NOT_FOUND)
        );

        String roleStr = request.getRole().toUpperCase();
        if (!roleStr.startsWith("ROLE_")) {
            roleStr = "ROLE_" + roleStr;
        }

        try {
            org.bbqqvv.backendecommerce.entity.Role newRole = org.bbqqvv.backendecommerce.entity.Role.valueOf(roleStr);
            if (user.getAuthorities() == null) {
                user.setAuthorities(new java.util.HashSet<>());
            } else {
                user.getAuthorities().clear();
            }
            user.getAuthorities().add(newRole);
            User updatedUser = userRepository.save(user);
            return userMapper.toUserResponse(updatedUser);
        } catch (IllegalArgumentException e) {
            throw new AppException(CommonErrorCode.UNCATEGORIZED_EXCEPTION);
        }
    }

    @Override
    @Transactional
    public UserResponse updatePermissions(Long id, org.bbqqvv.backendecommerce.dto.request.PermissionsUpdateRequest request) {
        User user = userRepository.findById(id).orElseThrow(() ->
                new AppException(UserErrorCode.USER_NOT_FOUND)
        );

        if (user.getPermissions() == null) {
            user.setPermissions(new java.util.HashSet<>());
        } else {
            user.getPermissions().clear();
        }

        if (request.getPermissions() != null) {
            for (String perm : request.getPermissions()) {
                try {
                    user.getPermissions().add(org.bbqqvv.backendecommerce.entity.Permission.valueOf(perm.toUpperCase()));
                } catch (IllegalArgumentException ignored) {
                }
            }
        }

        User updatedUser = userRepository.save(user);
        return userMapper.toUserResponse(updatedUser);
    }

}

