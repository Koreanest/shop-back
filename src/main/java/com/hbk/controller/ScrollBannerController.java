package com.hbk.controller;

import com.hbk.dto.ScrollBannerCreateReq;
import com.hbk.dto.ScrollBannerRes;
import com.hbk.service.ScrollBannerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/scroll-banners")
public class ScrollBannerController {

    private final ScrollBannerService scrollBannerService;

    /** ✅ 목록 */
    @GetMapping
    public List<ScrollBannerRes> list() {
        return scrollBannerService.list();
    }

    /** ✅ 생성 (multipart/form-data) */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ScrollBannerRes create(@ModelAttribute ScrollBannerCreateReq req) {
        return scrollBannerService.create(req);
    }

    /** ✅ 삭제 */
    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        scrollBannerService.delete(id);
    }
}