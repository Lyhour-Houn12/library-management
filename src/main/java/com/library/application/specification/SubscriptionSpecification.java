package com.library.application.specification;

import com.library.application.entity.SubscriptionPlan;
import com.library.application.payload.request.SubscriptionPlanFilter;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

public class SubscriptionSpecification {
    private SubscriptionSpecification(){

    }

    public static Specification<SubscriptionPlan> isActive(Boolean active){
        return ((root, query, cb) ->
                active == null ? null : cb.equal(root.get("isActive"), active));
    }

    public static Specification<SubscriptionPlan> isFeatured(Boolean featured){
        return ((root, query, cb) ->
                featured == null ? null : cb.equal(root.get("isFeatured"), featured));
    }
    public static Specification<SubscriptionPlan> hasCurrency(String currency){
        return ((root, query, cb) ->
                StringUtils.hasText(currency) ? cb.equal(root.get("currency"), currency) : null );
    }

    public static Specification<SubscriptionPlan> searchTerm(String searchTerm){
        return  ((root, query, cb) ->{
            if(!StringUtils.hasText(searchTerm)){
                return null;
            }
            String pattern = "%" + searchTerm.toLowerCase() + "%";
            Predicate nameLike = cb.like(cb.lower(root.get("name")), pattern);
            Predicate description  = cb.like(cb.lower(root.get("description")), pattern);
            return cb.or(nameLike, description);
        });
    }

    public static Specification<SubscriptionPlan> durationDaysBetween(Integer min, Integer max){
        return (((root, query, cb) -> {
            if(min != null && max != null) return cb.between(root.get("durationDays"), min, max);
            if(min != null) return cb.greaterThanOrEqualTo(root.get("durationDays"), min);
            if(max != null) return cb.lessThanOrEqualTo(root.get("durationDays"), max);
            return null;
        }));
    }

    public static Specification<SubscriptionPlan> priceBetween(Double min, Double max){
        return (((root, query, cb) -> {
            if(min != null && max != null) return cb.between(root.get("price"), min, max);
            if(min != null) return cb.greaterThanOrEqualTo(root.get("price"), min);
            if(max != null) return cb.lessThanOrEqualTo(root.get("price"), max);
            return null;
        }));
    }

    public static Specification<SubscriptionPlan> maxAllowedBetween(Integer min, Integer max){
        return (((root, query, cb) -> {
            if(min != null && max != null) return cb.between(root.get("maxBookAllowed"), min, max);
            if(min != null) return cb.greaterThanOrEqualTo(root.get("maxBookAllowed"), min);
            if(max != null) return cb.lessThanOrEqualTo(root.get("maxBookAllowed"), max);
            return null;
        }));
    }

    public static Specification<SubscriptionPlan> subscriptionPlanFilter(SubscriptionPlanFilter filter){
        return Specification
                .where(isActive(filter.getActive()))
                .and(isFeatured(filter.getFeatured()))
                .and(hasCurrency(filter.getCurrency()))
                .and(searchTerm(filter.getSearchTerm()))
                .and(durationDaysBetween(filter.getMinDuration(), filter.getMaxDuration()))
                .and(priceBetween(filter.getMinPrice(),  filter.getMaxPrice()))
                .and(maxAllowedBetween(filter.getMinBookAllowed(), filter.getMaxBookAllowed()));
    }



}
