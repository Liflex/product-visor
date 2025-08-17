package ru.dmitartur.client.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.dmitartur.client.entity.TelegramClient;
import ru.dmitartur.common.events.StartCommandEvent;
import ru.dmitartur.common.events.UserRegistrationSubmitEvent;

@Mapper(componentModel = "spring")
public interface TelegramClientMapper {

    @Mapping(target = "chatId", source = "chatId")
    @Mapping(target = "botId", source = "botId")
    @Mapping(target = "firstName", source = "firstName")
    @Mapping(target = "lastName", source = "lastName")
    @Mapping(target = "email", source = "email")
    @Mapping(target = "username", source = "username")
    TelegramClient toEntity(UserRegistrationSubmitEvent event);

    @Mapping(target = "chatId", source = "chatId")
    @Mapping(target = "botId", source = "botId")
    @Mapping(target = "firstName", source = "firstName")
    @Mapping(target = "lastName", source = "lastName")
    @Mapping(target = "username", source = "username")
    TelegramClient toEntity(StartCommandEvent event);
}


