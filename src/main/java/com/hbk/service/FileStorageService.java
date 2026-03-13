package com.hbk.service;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class FileStorageService { //👉 파일 저장/삭제 담당.

    //업로드 경로 설정 기본 업로드 폴더는 프로젝트 루트의 uploads 폴더.
    @Value("${app.upload.root:./uploads}")
    private  String uploadRoot;

    //파일 저장 메서드 👉 MultipartFile을 받아서 저장 후 접근 가능한 URL 문자열을 반환
    public String saveTextBannerImage(MultipartFile file) throws IOException {

        if (file == null || file.isEmpty()) return null;
        //👉 파일이 없으면 그냥 null 반환 (저장 안함)

        String original = StringUtils.cleanPath(
file.getOriginalFilename() == null ? "image" : file.getOriginalFilename()
//👉 업로드된 원본 파일명을 가져옴 null이면 "image"로 대체
        );
        //👉 파일 확장자 추출
String ext = "";
int dot = original.lastIndexOf('.');
if(dot >= 0) ext = original.substring(dot);
//
String filename = UUID.randomUUID() + ext;//파일명을 랜덤 UUID로 변경
//업로드 경로 생성
        Path dir = Paths.get(uploadRoot, "text-banners");
Files.createDirectories(dir);
//저장할 최종 경로 생성
Path target = dir.resolve(filename);
//파일 실제 저장 이미 존재하면 덮어쓰기
Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);

return "/uploads/text-banners/" + filename;
    }

    //파일 삭제 메서드
    public void deleteByRelativeUrl(String relativeUrl) {
        //값이 없으면 종료
        if(relativeUrl == null || relativeUrl.isBlank()) return;

String normalized = relativeUrl.startsWith("/") ? relativeUrl.substring(1)
        : relativeUrl; //앞에 / 제거
        try{
            Files.deleteIfExists(Paths.get(normalized));//해당 파일이 존재하면 삭제
        }catch (Exception ignore){
//👉 삭제 실패해도 프로그램 죽지 않게 무시
        }

    }


}
