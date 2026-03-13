package com.hbk.legacy;

import com.hbk.service.FileStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor //final 필드를 주입받는 생성자를 자동 생성
public class LegacyTextBannerService {

    private final TextBannerRepository textBannerRepository;
    private final FileStorageService fileStorageService;

    @Transactional(readOnly = true)//DB “조회 전용” 트랜잭션으로 목록 조회 시작.
    public List<LegacyTextBannerRes> list(){
return textBannerRepository.findAll().stream()
        .sorted(Comparator.comparing(e -> e.getSortOrder() == null ? 0 : e.getSortOrder()))
        //sortOrder 기준 오름차순 정렬 sortOrder가 null이면 0으로 간주해서 정렬.
        .map(LegacyTextBannerRes::from).toList();//엔티티(TextBannerEntity)를 응답 DTO(TextBannerRes)로 변환.
        //stream 결과를 List로 만들어서 반환.
    }

    @Transactional
    public LegacyTextBannerRes create(LegacyTextBannerCreateReq req) {
String title = req.getTitle() == null ? "" : req.getTitle().trim();
String desc = req.getDesc() == null ? "" : req.getDesc().trim();
//title/desc가 null이면 빈 문자열로 처리. null이 아니면 앞뒤 공백 제거(trim).
if(title.isBlank()) throw new IllegalArgumentException("title is required");
if(desc.isBlank()) throw new IllegalArgumentException("desc is required");
//title/desc가 비어있으면(공백 포함) 예외 발생.

String visibleYn = (req.getVisibleYn() == null || req.getVisibleYn().isBlank()) ? "Y"
: req.getVisibleYn().trim().toUpperCase();
//visibleYn이 없거나 비어있으면 기본값 "Y". 있으면 trim 후 대문자로 변환 (y → Y).
if (!visibleYn.equals("Y") && !visibleYn.equals("N")) {
    throw new IllegalArgumentException("visibleYn must be Y or N");
}//"Y" 또는 "N"만 허용. 아니면 예외.

        Integer sortOrder = req.getSortOrder();
//요청에서 sortOrder를 받음.
        if (sortOrder == null) {//sortOrder가 없으면 DB에서 현재 최대값을 가져와서 +1로 자동 지정.
            int max = textBannerRepository.findMaxSortOrder();
            sortOrder = max + 1;
        }

        String imageUrl = null;

        try{ //요청에 이미지가 있으면 저장하고, 저장된 접근 URL을 받음.
imageUrl = fileStorageService.saveTextBannerImage(req.getImage());
        }catch(Exception e) {
  throw new RuntimeException("image upload failed", e);
  //업로드 중 에러면 RuntimeException으로 감싸서 던짐.
        }
        LegacyTextBanner saved = textBannerRepository.save(//엔티티를 만들어서 DB 저장.
LegacyTextBanner.builder()
.title(title).desc(desc).imageUrl(imageUrl)
//linkUrl은 null이면 null 그대로, 있으면 trim 처리.
.linkUrl(req.getLinkUrl() == null ? null : req.getLinkUrl().trim()).sortOrder(sortOrder)
.visibleYn(visibleYn).build()
        );
return  LegacyTextBannerRes.from(saved); //저장된 엔티티를 응답 DTO로 변환해서 반환.
    }

    @Transactional
    public void delete(long id){
LegacyTextBanner entity = textBannerRepository.findById(id)//id로 배너 조회.
.orElseThrow(() -> new IllegalArgumentException("text banner not found: " + id));
//DB 삭제 전에(선택적으로) 연결된 이미지 파일을 먼저 삭제 시도.
fileStorageService.deleteByRelativeUrl(entity.getImageUrl());
textBannerRepository.delete(entity);
    }

}
