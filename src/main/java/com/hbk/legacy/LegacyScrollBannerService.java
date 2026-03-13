package com.hbk.legacy;

import com.hbk.service.FileStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class LegacyScrollBannerService {

    private final ScrollBannerRepository scrollBannerRepository;
    private final FileStorageService fileStorageService;

    /** ✅ 목록 조회 (sortOrder 오름차순) */
    @Transactional(readOnly = true)
    public List<LegacyScrollBannerRes> list() {
        return scrollBannerRepository.findAll().stream()
                .sorted(Comparator.comparingInt(e -> e.getSortOrder() == null ? 0 : e.getSortOrder()))
                .map(LegacyScrollBannerRes::from)
                .toList();
    }

    /** ✅ 생성 (multipart/form-data) */
    @Transactional
    public LegacyScrollBannerRes create(LegacyScrollBannerCreateReq req) {
        // title 필수
        String title = req.getTitle() == null ? "" : req.getTitle().trim();
        if (title.isBlank()) throw new IllegalArgumentException("title is required");

        // ✅ visibleYn 기본값
        String visibleYn = (req.getVisibleYn() == null || req.getVisibleYn().isBlank())
                ? "Y"
                : req.getVisibleYn().trim().toUpperCase();

        if (!visibleYn.equals("Y") && !visibleYn.equals("N")) {
            throw new IllegalArgumentException("visibleYn must be Y or N");
        }

        // ✅ sortOrder 기본값: max + 1
        Integer sortOrder = req.getSortOrder();
        if (sortOrder == null) {
            int max = scrollBannerRepository.findMaxSortOrder();
            sortOrder = max + 1;
        }

        // ✅ 버튼 텍스트 기본값
        String buttonText = (req.getButtonText() == null || req.getButtonText().trim().isEmpty())
                ? "구매하기"
                : req.getButtonText().trim();

        // ✅ 링크들(trim)
        String linkUrl = req.getLinkUrl() == null ? null : req.getLinkUrl().trim();
        String buttonLinkUrl = req.getButtonLinkUrl() == null ? null : req.getButtonLinkUrl().trim();

        // ✅ 이미지 업로드(선택)
        String imageUrl = null;
        try {
            imageUrl = fileStorageService.saveTextBannerImage(req.getImage());
        } catch (Exception e) {
            throw new RuntimeException("image upload failed", e);
        }

        LegacyScrollBanner saved = scrollBannerRepository.save(
                LegacyScrollBanner.builder()
                        .title(title)
                        .imageUrl(imageUrl)
                        .linkUrl(linkUrl)
                        .buttonText(buttonText)
                        .buttonLinkUrl(buttonLinkUrl)
                        .sortOrder(sortOrder)
                        .visibleYn(visibleYn)
                        .build()
        );

        return LegacyScrollBannerRes.from(saved);
    }

    /** ✅ 삭제 (이미지 파일도 같이 삭제 시도) */
    @Transactional
    public void delete(long id) {
        LegacyScrollBanner entity = scrollBannerRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("scroll banner not found: " + id));

        // 파일 삭제 먼저(선택)
        fileStorageService.deleteByRelativeUrl(entity.getImageUrl());

        scrollBannerRepository.delete(entity);
    }
}