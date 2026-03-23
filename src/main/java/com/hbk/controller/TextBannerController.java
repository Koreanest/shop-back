package com.hbk.controller;

import com.hbk.dto.TextBannerCreateReq;
import com.hbk.dto.TextBannerRes;
import com.hbk.service.TextBannerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/text-banners")
public class TextBannerController {

    private final TextBannerService textBannerService;

    @GetMapping
    public List<TextBannerRes> list() {
        return textBannerService.list();
    }

    // ✅ 생성 (multipart/form-data)
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public TextBannerRes create(@ModelAttribute TextBannerCreateReq req) {
        return textBannerService.create(req);
    }

    // ✅ 삭제
    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        textBannerService.delete(id);
    }
}