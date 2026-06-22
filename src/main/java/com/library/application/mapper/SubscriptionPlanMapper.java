package com.library.application.mapper;

import com.library.application.entity.SubscriptionPlan;
import com.library.application.payload.dto.SubscriptionPlanDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface SubscriptionPlanMapper {

    SubscriptionPlan toEntity(SubscriptionPlanDTO subscriptionPlanDTO);

    SubscriptionPlanDTO toDto(SubscriptionPlan subscriptionPlan);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "planCode", ignore = true)
    void updateEntity(SubscriptionPlanDTO subscriptionPlanDTO, @MappingTarget SubscriptionPlan subscriptionPlan);
}
