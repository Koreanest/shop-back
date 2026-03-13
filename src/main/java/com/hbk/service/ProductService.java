package com.hbk.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hbk.dto.ProductResponseDTO;
import com.hbk.dto.ProductSizeDTO;
import com.hbk.dto.ProductSpecDTO;
import com.hbk.entity.*;
import com.hbk.repository.BrandRepository;
import com.hbk.repository.NavMenuRepository;
import com.hbk.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.text.Normalizer;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository repo;
    private final BrandRepository brandRepository;
    private final NavMenuRepository navMenuRepo;
    private final FileStorage fileStorage;
    private final ObjectMapper objectMapper;

    /**
     * 상품 생성 시 사용할 slug 생성기
     *
     * update에서는 slug를 재생성하지 않는다.
     * 이유:
     * 1) URL 안정성 유지
     * 2) skuCode 기준 안정성 유지
     */
    private String generateSlug(String title) {
        String base = Normalizer.normalize(title, Normalizer.Form.NFD)
                .replaceAll("[^\\p{IsAlphabetic}\\p{IsDigit}가-힣 ]", "")
                .toLowerCase()
                .trim()
                .replaceAll("\\s+", "-");

        String slug = base;
        int count = 1;

        while (repo.existsBySlug(slug)) {
            slug = base + "-" + count++;
        }
        return slug;
    }

    /**
     * size 문자열 정규화
     * 예: " 2 " -> "2"
     */
    private String normalizeSize(String size) {
        return size == null ? "" : size.trim().toLowerCase();
    }

    /**
     * 요청 size 중복 방지
     * 예: "2" 와 " 2 " 는 같은 값으로 본다.
     */
    private void validateDuplicateSizes(List<ProductSizeDTO> sizes) {
        Set<String> keys = new HashSet<>();

        for (ProductSizeDTO s : sizes) {
            String key = normalizeSize(s.getSize());
            if (!keys.add(key)) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "중복된 사이즈가 있습니다. size=" + s.getSize()
                );
            }
        }
    }

    /**
     * sizes JSON 파싱 + 기본 검증
     */
    private List<ProductSizeDTO> parseSizes(String sizesJson) {
        try {
            List<ProductSizeDTO> sizes = objectMapper.readValue(
                    sizesJson,
                    objectMapper.getTypeFactory().constructCollectionType(List.class, ProductSizeDTO.class)
            );

            if (sizes == null || sizes.isEmpty()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "사이즈를 1개 이상 추가하세요.");
            }

            for (ProductSizeDTO s : sizes) {
                if (s.getSize() == null || s.getSize().isBlank()) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "사이즈 값이 올바르지 않습니다.");
                }
                if (s.getStock() == null || s.getStock() < 0) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "재고는 0 이상이어야 합니다.");
                }
            }

            validateDuplicateSizes(sizes);
            return sizes;
        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "사이즈 파싱 실패");
        }
    }

    /**
     * spec JSON 파싱
     */
    private ProductSpecDTO parseSpec(String specJson) {
        try {
            ProductSpecDTO spec = objectMapper.readValue(specJson, ProductSpecDTO.class);
            if (spec == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "상품 스펙은 필수입니다.");
            }
            return spec;
        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "상품 스펙 파싱 실패");
        }
    }

    /**
     * 목록 조회
     *
     * 기존 컨트롤러 호환을 위해 유지.
     * 목록 pagination은 별도 QueryService로 확장 가능.
     */
    @Transactional(readOnly = true)
    public List<ProductResponseDTO> list() {
        return repo.findAll().stream()
                .map(ProductResponseDTO::from)
                .toList();
    }

    /**
     * ID 기반 상세 조회
     */
    @Transactional(readOnly = true)
    public ProductResponseDTO getById(Long id) {
        Product product = repo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "상품이 없습니다. id=" + id));
        return ProductResponseDTO.from(product);
    }

    /**
     * slug 기반 상세 조회
     */
    @Transactional(readOnly = true)
    public ProductResponseDTO getBySlug(String slug) {
        Product product = repo.findBySlug(slug)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "상품이 없습니다. slug=" + slug));
        return ProductResponseDTO.from(product);
    }

    /**
     * 상품 등록
     *
     * create는 신규 상품 생성이므로 slug 생성 + SKU/재고 신규 생성 방식이 맞다.
     */
    @Transactional
    public ProductResponseDTO create(
            String title,
            String description,
            Integer price,
            Long brandId,
            Long categoryId,
            String sizesJson,
            String specJson,
            MultipartFile image
    ) throws Exception {

        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("상품명은 필수입니다.");
        }
        if (price == null || price <= 0) {
            throw new IllegalArgumentException("가격은 0보다 커야 합니다.");
        }
        if (brandId == null) {
            throw new IllegalArgumentException("브랜드는 필수입니다.");
        }
        if (categoryId == null) {
            throw new IllegalArgumentException("카테고리는 필수입니다.");
        }
        if (image == null || image.isEmpty()) {
            throw new IllegalArgumentException("이미지를 선택하세요.");
        }

        List<ProductSizeDTO> sizes = parseSizes(sizesJson);
        ProductSpecDTO specDto = parseSpec(specJson);

        Brand brand = brandRepository.findById(brandId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "브랜드가 존재하지 않습니다. id=" + brandId));

        NavMenu category = navMenuRepo.findById(categoryId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "카테고리가 존재하지 않습니다. id=" + categoryId));

        var stored = fileStorage.save(image);
        String slug = generateSlug(title);

        Product product = Product.builder()
                .brand(brand)
                .title(title)
                .description(description)
                .price(price)
                .slug(slug)
                .category(category)
                .imageUrl(stored.url())
                .imagePath(stored.filePath())
                .build();

        // 신규 SKU/재고 생성
        product.clearSizes();
        for (ProductSizeDTO s : sizes) {
            String normalizedSize = normalizeSize(s.getSize());

            Sku sku = Sku.builder()
                    .gripSize(normalizedSize)
                    .isActive(true)
                    .price(price)
                    .skuCode(slug + "-" + normalizedSize)
                    .build();

            Inventory inventory = new Inventory(sku, s.getStock(), 0);
            sku.setInventory(inventory);

            product.addSize(sku);
        }

        // 스펙 생성
        ProductSpec spec = ProductSpec.builder()
                .headSizeSqIn(specDto.getHeadSizeSqIn())
                .unstrungWeightG(specDto.getUnstrungWeightG())
                .balanceMm(specDto.getBalanceMm())
                .lengthIn(specDto.getLengthIn())
                .patternMain(specDto.getPatternMain())
                .patternCross(specDto.getPatternCross())
                .stiffnessRa(specDto.getStiffnessRa())
                .build();

        product.setSpec(spec);

        Product saved = repo.save(product);
        return ProductResponseDTO.from(saved);
    }

    /**
     * ProductSpec 동기화
     * 있으면 수정, 없으면 생성
     */
    private void syncSpec(Product product, ProductSpecDTO specDto) {
        ProductSpec spec = product.getSpec();

        if (spec == null) {
            spec = ProductSpec.builder().build();
            product.setSpec(spec);
        }

        spec.setHeadSizeSqIn(specDto.getHeadSizeSqIn());
        spec.setUnstrungWeightG(specDto.getUnstrungWeightG());
        spec.setBalanceMm(specDto.getBalanceMm());
        spec.setLengthIn(specDto.getLengthIn());
        spec.setPatternMain(specDto.getPatternMain());
        spec.setPatternCross(specDto.getPatternCross());
        spec.setStiffnessRa(specDto.getStiffnessRa());
    }

    /**
     * SKU/Inventory 동기화
     *
     * 정책:
     * - 기존에 있으면 수정
     * - 없으면 신규 생성
     * - 요청에서 빠진 기존 SKU는 삭제
     *
     * 비교 기준은 현재 프로젝트 단계에서는 size(gripSize)로 간다.
     * 이후 필요하면 skuId 기반으로 확장할 수 있다.
     */
    private void syncSizes(Product product, List<ProductSizeDTO> requestedSizes) {
        // 기존 SKU를 size 기준 map으로 정리
        Map<String, Sku> existingMap = product.getSizes().stream()
                .collect(Collectors.toMap(
                        sku -> normalizeSize(sku.getGripSize()),
                        Function.identity()
                ));

        Set<String> requestedKeys = new HashSet<>();

        // 요청 size 순회: 수정 또는 추가
        for (ProductSizeDTO dto : requestedSizes) {
            String sizeKey = normalizeSize(dto.getSize());
            requestedKeys.add(sizeKey);

            Sku existingSku = existingMap.get(sizeKey);

            if (existingSku != null) {
                // 기존 SKU 수정
                existingSku.setGripSize(sizeKey);
                existingSku.setPrice(product.getPrice());
                existingSku.setIsActive(true);

                if (existingSku.getInventory() == null) {
                    Inventory inventory = new Inventory(existingSku, dto.getStock(), 0);
                    existingSku.setInventory(inventory);
                } else {
                    existingSku.getInventory().setStockQty(dto.getStock());
                    existingSku.getInventory().setSafetyStockQty(0);
                }
            } else {
                // 신규 SKU 추가
                Sku newSku = Sku.builder()
                        .gripSize(sizeKey)
                        .price(product.getPrice())
                        .isActive(true)
                        .skuCode(product.getSlug() + "-" + sizeKey)
                        .build();

                Inventory inventory = new Inventory(newSku, dto.getStock(), 0);
                newSku.setInventory(inventory);

                product.addSize(newSku);
            }
        }

        // 요청에서 빠진 기존 SKU 삭제
        List<Sku> toRemove = product.getSizes().stream()
                .filter(sku -> !requestedKeys.contains(normalizeSize(sku.getGripSize())))
                .toList();

        for (Sku sku : toRemove) {
            product.removeSize(sku);
        }
    }

    /**
     * 상품 수정
     *
     * 업그레이드 포인트:
     * - slug 재생성 금지
     * - SKU 전량 삭제 후 재생성 대신 sync 방식 사용
     * - 기존 SKU ID 최대한 유지
     */
    @Transactional
    public ProductResponseDTO update(
            Long id,
            String title,
            String description,
            Integer price,
            Long brandId,
            Long categoryId,
            String sizesJson,
            String specJson,
            MultipartFile image
    ) throws Exception {

        Product product = repo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "상품이 없습니다. id=" + id));

        if (title == null || title.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "상품명은 필수입니다.");
        }
        if (price == null || price <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "가격은 0보다 커야 합니다.");
        }

        List<ProductSizeDTO> sizes = parseSizes(sizesJson);
        ProductSpecDTO specDto = parseSpec(specJson);

        Brand brand = (brandId != null)
                ? brandRepository.findById(brandId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "브랜드가 존재하지 않습니다. id=" + brandId))
                : product.getBrand();

        NavMenu category = (categoryId != null)
                ? navMenuRepo.findById(categoryId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "카테고리가 존재하지 않습니다. id=" + categoryId))
                : product.getCategory();

        // 기본 정보 수정
        product.setTitle(title);
        product.setDescription(description);
        product.setPrice(price);
        product.setBrand(brand);
        product.setCategory(category);

        // slug는 유지
        // product.setSlug(generateSlug(title)); // 의도적으로 하지 않음

        // 이미지 교체
        if (image != null && !image.isEmpty()) {
            fileStorage.deleteByPath(product.getImagePath());
            var stored = fileStorage.save(image);
            product.setImageUrl(stored.url());
            product.setImagePath(stored.filePath());
        }

        // 스펙 동기화
        syncSpec(product, specDto);

        // SKU/재고 동기화
        syncSizes(product, sizes);

        Product saved = repo.save(product);
        return ProductResponseDTO.from(saved);
    }

    /**
     * 상품 삭제
     */
    @Transactional
    public void delete(Long id) {
        Product product = repo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "상품이 없습니다. id=" + id));

        fileStorage.deleteByPath(product.getImagePath());
        repo.delete(product);
    }
}