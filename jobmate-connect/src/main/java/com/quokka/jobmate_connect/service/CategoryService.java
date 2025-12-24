package com.quokka.jobmate_connect.service;

import com.quokka.jobmate_connect.dto.request.category.CategoryBulkRequest;
import com.quokka.jobmate_connect.dto.request.category.CategoryRequest;
import com.quokka.jobmate_connect.dto.response.category.CategoryResponse;
import com.quokka.jobmate_connect.entity.Category;
import com.quokka.jobmate_connect.exception.AppException;
import com.quokka.jobmate_connect.exception.ErrorCode;
import com.quokka.jobmate_connect.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = lombok.AccessLevel.PRIVATE, makeFinal = true)
public class CategoryService {

    CategoryRepository categoryRepository;

    public CategoryResponse createCategory(CategoryRequest request) {
        if (categoryRepository.existsByNameIgnoreCase(request.getName())) {
            throw new AppException(ErrorCode.ALREADY_EXISTS);
        }

        Category category = Category.builder()
                .name(request.getName().trim())
                .build();

        return toResponse(categoryRepository.save(category));
    }

    public List<CategoryResponse> createCategories(CategoryBulkRequest request) {
        List<String> names = request.getNames();
        if (names == null || names.isEmpty()) {
            throw new AppException(ErrorCode.BAD_REQUEST);
        }

        HashSet<String> normalizedSet = new HashSet<>();
        List<Category> categoriesToSave = new ArrayList<>();

        for (String rawName : names) {
            if (rawName == null || rawName.trim().isEmpty()) {
                throw new AppException(ErrorCode.BAD_REQUEST);
            }

            String normalized = rawName.trim();
            String key = normalized.toLowerCase();

            if (!normalizedSet.add(key)) {
                throw new AppException(ErrorCode.ALREADY_EXISTS);
            }

            if (categoryRepository.existsByNameIgnoreCase(normalized)) {
                throw new AppException(ErrorCode.ALREADY_EXISTS);
            }

            categoriesToSave.add(Category.builder().name(normalized).build());
        }

        return categoryRepository.saveAll(categoriesToSave).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public List<CategoryResponse> getAllCategories() {
        return categoryRepository.findAll()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public CategoryResponse updateCategory(UUID id, CategoryRequest request) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.CATEGORY_NOT_FOUND));

        if (!category.getName().equalsIgnoreCase(request.getName())
                && categoryRepository.existsByNameIgnoreCase(request.getName())) {
            throw new AppException(ErrorCode.ALREADY_EXISTS);
        }

        category.setName(request.getName().trim());
        return toResponse(categoryRepository.save(category));
    }

    public void deleteCategory(UUID id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.CATEGORY_NOT_FOUND));

        categoryRepository.delete(category);
    }

    public Category getCategory(UUID id) {
        if (id == null) {
            return null;
        }
        return categoryRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.CATEGORY_NOT_FOUND));
    }

    /**
     * Khởi tạo categories mặc định (chỉ tạo những category chưa tồn tại).
     * Method này được sử dụng trong initialization, không throw exception nếu
     * category đã tồn tại.
     * 
     * @return Số lượng categories mới được tạo
     */
    public int initDefaultCategories(List<String> categoryNames) {
        if (categoryNames == null || categoryNames.isEmpty()) {
            return 0;
        }

        List<Category> categoriesToSave = new ArrayList<>();

        for (String rawName : categoryNames) {
            if (rawName == null || rawName.trim().isEmpty()) {
                continue; // Bỏ qua các tên rỗng
            }

            String normalized = rawName.trim();

            // Chỉ tạo category nếu chưa tồn tại
            if (!categoryRepository.existsByNameIgnoreCase(normalized)) {
                categoriesToSave.add(Category.builder().name(normalized).build());
            }
        }

        if (!categoriesToSave.isEmpty()) {
            categoryRepository.saveAll(categoriesToSave);
        }

        return categoriesToSave.size();
    }

    private CategoryResponse toResponse(Category category) {
        return CategoryResponse.builder()
                .id(category.getId())
                .name(category.getName())
                .build();
    }
}
