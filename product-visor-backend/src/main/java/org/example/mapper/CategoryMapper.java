package org.example.mapper;
import org.example.dto.CategoryDto;
import org.example.entity.Category;
import org.mapstruct.Mapper;
import org.springframework.stereotype.Component;

@Mapper(componentModel = "spring", uses = {AttributeMapper.class})
@Component
public interface CategoryMapper {

    CategoryDto categoryToCategoryDto(Category category);

    Category categoryDtoToCategory(CategoryDto categoryDto);
}
