package com.familyfood.dish.service;

import com.familyfood.dish.dto.DishRequest;
import com.familyfood.dish.dto.DishResponse;
import com.familyfood.dish.dto.TagRequest;
import com.familyfood.dish.entity.DishCategory;
import com.familyfood.dish.entity.DishTag;
import java.util.List;

public interface DishApplicationService {
    List<DishResponse> list(String keyword, Long categoryId, Long tagId, String status);

    DishResponse get(Long id);

    DishResponse create(DishRequest request);

    DishResponse update(Long id, DishRequest request);

    DishResponse updateStatus(Long id, String status);

    void delete(Long id);

    List<DishCategory> categories();

    DishCategory createCategory(String name);

    List<DishTag> tags();

    DishTag createTag(TagRequest request);
}
