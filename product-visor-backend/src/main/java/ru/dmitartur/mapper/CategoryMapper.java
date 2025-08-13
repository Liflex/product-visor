package ru.dmitartur.mapper;
import ru.dmitartur.dto.CategoryDto;
import ru.dmitartur.entity.Category;
import org.mapstruct.Mapper;
import org.springframework.stereotype.Component;

@Mapper(componentModel = "spring", uses = {AttributeMapper.class})
@Component
public interface CategoryMapper {

    CategoryDto categoryToCategoryDto(Category category);

    Category categoryDtoToCategory(CategoryDto categoryDto);
}
