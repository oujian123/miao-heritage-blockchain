package com.miaoheritage.api.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.miaoheritage.entity.Category;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CategoryDTO {
    
    private Long id;
    private String name;
    private String description;
    private String icon;
    private Integer sortOrder;
    private Long parentId;
    private String parentName;
    private List<CategoryDTO> children;
    private boolean isActive;
    
    /**
     * 从实体转换为DTO
     */
    public static CategoryDTO fromEntity(Category category) {
        if (category == null) {
            return null;
        }
        
        CategoryDTO dto = new CategoryDTO();
        dto.setId(category.getId());
        dto.setName(category.getName());
        dto.setDescription(category.getDescription());
        dto.setIcon(category.getIcon());
        dto.setSortOrder(category.getSortOrder());
        dto.setActive(category.isActive());
        
        if (category.getParent() != null) {
            dto.setParentId(category.getParent().getId());
            dto.setParentName(category.getParent().getName());
        }
        
        if (category.getChildren() != null && !category.getChildren().isEmpty()) {
            dto.setChildren(category.getChildren().stream()
                    .map(CategoryDTO::fromEntity)
                    .collect(Collectors.toList()));
        }
        
        return dto;
    }
    
    /**
     * 从实体列表转换为DTO列表
     */
    public static List<CategoryDTO> fromEntities(List<Category> categories) {
        if (categories == null) {
            return new ArrayList<>();
        }
        
        return categories.stream()
                .map(CategoryDTO::fromEntity)
                .collect(Collectors.toList());
    }
} 