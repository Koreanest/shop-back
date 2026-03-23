package com.hbk.controller;

import com.hbk.dto.SpotItemCreateReq;
import com.hbk.dto.SpotItemRes;
import com.hbk.service.SpotItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/spot-items")
public class SpotItemController {

    private final SpotItemService spotItemService;

    /** ✅ 목록 */
    @GetMapping
    public List<SpotItemRes> list() {
        return spotItemService.list();
    }

    /** ✅ 생성 (multipart/form-data) */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public SpotItemRes create(@ModelAttribute SpotItemCreateReq req) {
        return spotItemService.create(req);
    }

    /** ✅ 삭제 */
    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        spotItemService.delete(id);
    }
}