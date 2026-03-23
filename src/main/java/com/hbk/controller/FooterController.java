package com.hbk.controller;

import com.hbk.dto.*;
import com.hbk.service.FooterService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/footer")
public class FooterController {

    private final FooterService footerService;

    // 1차 카테고리
    @GetMapping("/categories")
    public List<FooterCategoryResponse> categories() {
        return footerService.categories();
    }

    @PostMapping("/categories")
    public FooterCategoryResponse createCategory(@RequestBody FooterCategoryCreateRequest req) {
        return footerService.createCategory(req);
    }

    @DeleteMapping("/categories/{id}")
    public void deleteCategory(@PathVariable Long id) {
        footerService.deleteCategory(id);
    }

    // 2차 링크
    @GetMapping("/categories/{categoryId}/links")
    public List<FooterLinkResponse> links(@PathVariable Long categoryId) {
        return footerService.linksByCategory(categoryId);
    }

    @PostMapping("/links")
    public FooterLinkResponse createLink(@RequestBody FooterLinkCreateRequest req) {
        return footerService.createLink(req);
    }

    @DeleteMapping("/links/{id}")
    public void deleteLink(@PathVariable Long id) {
        footerService.deleteLink(id);
    }

    // 하단 문구(문단 2개)
    @GetMapping("/text")
    public FooterTextResponse getText() {
        return footerService.getText();
    }

    @PutMapping("/text")
    public FooterTextResponse upsertText(@RequestBody FooterTextUpsertRequest req) {
        return footerService.upsertText(req);
    }
}