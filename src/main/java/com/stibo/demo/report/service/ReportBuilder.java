package com.stibo.demo.report.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.util.CollectionUtils;

import com.stibo.demo.report.model.Attribute;
import com.stibo.demo.report.model.AttributeGroup;
import com.stibo.demo.report.model.AttributeLink;
import com.stibo.demo.report.model.Category;
import com.stibo.demo.report.model.Datastandard;

public class ReportBuilder {
    private final Datastandard datastandard;

    private final Map<String, AttributeGroup> attributeGroupMap;

    private final Map<String, Category> categoryIdMap;

    private final Map<String, Attribute> attributeIdMap;

    private final Map<String, AttributeGroup> groupIdMap;

    private final ReportFormatter rf;

    public ReportBuilder(Datastandard datastandard, ReportFormatter reportFormatter) {
        this.datastandard = datastandard;
        this.rf = reportFormatter;
        this.categoryIdMap = datastandard.getCategories().stream().collect(Collectors.toMap(Category::getId, Function.identity()));
        this.attributeIdMap = datastandard.getAttributes().stream().collect(Collectors.toMap(Attribute::getId, Function.identity()));
        this.attributeGroupMap = datastandard.getAttributeGroups().stream().collect(Collectors.toMap(AttributeGroup::getId, Function.identity()));
        this.groupIdMap = datastandard.getAttributeGroups().stream().collect(Collectors.toMap(AttributeGroup::getId, Function.identity()));
    }

    public Stream<Stream<String>> build(String categoryId) {
        Collection<Category> categoryHierarchy = collectCategoryHierarchy(categoryId);
        Stream<Stream<String>> report = categoryHierarchy.stream()
                .flatMap(category -> category.getAttributeLinks()
                        .stream().map(attributeLink -> buildRow(category, attributeLink)));
        return Stream.concat(getHeader(),report);
    }

    private Stream<Stream<String>> getHeader() {
        return Stream.of(Stream.of("Category Name", "Attribute Name", "Description", "Type", "Groups"));
    }

    private Stream<String> buildRow(Category category, AttributeLink attributeLink) {
        Attribute attribute = attributeIdMap.get(attributeLink.getId());
        return Stream.of(
                buildCategory(category),
                buildAttributeName(attribute, attributeLink),
                buildAttributeDesc(attribute),
                buildType(attribute),
                buildGroup(attribute));
    }

    private String buildCategory(Category category) {
        return rf.print(category.getName()).end();
    }

    private String buildAttributeName(Attribute attribute, AttributeLink attributeLink) {
        return rf.printWithMandatory(attribute.getName(), !attributeLink.getOptional()).end();
    }

    private String buildAttributeDesc(Attribute attribute) {
        return rf.print(attribute.getDescription()).end();
    }

    private String buildGroup(Attribute attribute) {
        return rf.printList(attribute.getGroupIds().stream().map(groupId -> groupIdMap.get(groupId).getName())).end();
    }

    private String buildType(Attribute attribute) {
        rf.print(attribute.getType().getId());
        if ( false == CollectionUtils.isEmpty(attribute.getAttributeLinks()) ) {
            buildNestedType(attribute.getAttributeLinks());
        }
        return rf.multivalue(attribute.getType().getMultiValue()).end();
    }

    private void buildNestedType(List<AttributeLink> attributeLinkList) {
        rf.startSubsection();
        attributeLinkList.forEach(attributeLink -> {
            Attribute nestedAttribute = attributeIdMap.get(attributeLink.getId());
            rf.printWithMandatory(nestedAttribute.getName(), !attributeLink.getOptional()).printType(nestedAttribute.getType().getId());
            if ( false == CollectionUtils.isEmpty(nestedAttribute.getAttributeLinks()) ) {
                buildNestedType(nestedAttribute.getAttributeLinks());
            }
            rf.multivalue(nestedAttribute.getType().getMultiValue());
        });
        rf.endSubsection();
    }

    private Collection<Category> collectCategoryHierarchy(String categoryId) {
        Category category;
        List<Category> categoryHierarchy = new ArrayList<>();
        do {
            category = categoryIdMap.get(categoryId);
            categoryHierarchy.add(category);
        } while (null != (categoryId = category.getParentId()));
        Collections.reverse(categoryHierarchy);
        return categoryHierarchy;
    }
}
