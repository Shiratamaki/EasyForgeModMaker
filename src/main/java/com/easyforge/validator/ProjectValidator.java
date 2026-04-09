package com.easyforge.validator;

import com.easyforge.model.*;
import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ProjectValidator {
    public static List<ValidationError> validate(ModProject project) {
        List<ValidationError> errors = new ArrayList<>();
        // 检查物品ID唯一性
        Set<String> itemIds = new HashSet<>();
        for (ItemData item : project.getItems()) {
            if (!itemIds.add(item.getId())) {
                errors.add(new ValidationError(ValidationError.Severity.ERROR, "物品",
                        "物品ID重复: " + item.getId(), "请修改为唯一ID"));
            }
            if (!item.getId().matches("[a-z0-9_]+")) {
                errors.add(new ValidationError(ValidationError.Severity.ERROR, "物品",
                        "物品ID格式错误: " + item.getId(), "只能包含小写字母、数字和下划线"));
            }
            if (item.getDisplayName() == null || item.getDisplayName().isEmpty()) {
                errors.add(new ValidationError(ValidationError.Severity.ERROR, "物品",
                        "物品显示名称为空: " + item.getId(), "请填写显示名称"));
            }
        }
        // 检查方块ID唯一性
        Set<String> blockIds = new HashSet<>();
        for (BlockData block : project.getBlocks()) {
            if (!blockIds.add(block.getId())) {
                errors.add(new ValidationError(ValidationError.Severity.ERROR, "方块",
                        "方块ID重复: " + block.getId(), "请修改为唯一ID"));
            }
            if (!block.getId().matches("[a-z0-9_]+")) {
                errors.add(new ValidationError(ValidationError.Severity.ERROR, "方块",
                        "方块ID格式错误: " + block.getId(), "只能包含小写字母、数字和下划线"));
            }
            if (block.getDisplayName() == null || block.getDisplayName().isEmpty()) {
                errors.add(new ValidationError(ValidationError.Severity.ERROR, "方块",
                        "方块显示名称为空: " + block.getId(), "请填写显示名称"));
            }
        }
        // 检查配方输出物品是否存在（仅警告）
        for (RecipeData recipe : project.getRecipes()) {
            String output = recipe.getOutputItem();
            if (output.contains(":")) {
                String[] parts = output.split(":");
                if (!project.getModId().equals(parts[0])) {
                    // 跨模组依赖，不检查
                    continue;
                }
                String id = parts[1];
                boolean exists = project.getItems().stream().anyMatch(i -> i.getId().equals(id)) ||
                        project.getBlocks().stream().anyMatch(b -> b.getId().equals(id));
                if (!exists) {
                    errors.add(new ValidationError(ValidationError.Severity.WARNING, "配方",
                            "配方输出物品不存在: " + output, "请确保物品或方块已注册"));
                }
            }
        }
        return errors;
    }
}