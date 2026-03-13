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
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository repo;
    private final BrandRepository brandRepository;
    private final NavMenuRepository navMenuRepo;
    private final FileStorage fileStorage;
    private final ObjectMapper objectMapper;

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
                if (s.getSize() == null || s.getSize().isBlank())  {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "사이즈 값이 올바르지 않습니다.");
                }
                if (s.getStock() == null || s.getStock() < 0) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "재고는 0 이상이어야 합니다.");
                }
            }
            return sizes;
        } catch (ResponseStatusException e) {
            throw e;
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "사이즈 파싱 실패");
        }
    }

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

    @Transactional(readOnly = true)
    public List<ProductResponseDTO> list() {
        return repo.findAll().stream()
                .map(ProductResponseDTO::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public ProductResponseDTO getById(Long id) {
        Product product = repo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "상품이 없습니다. id=" + id));
        return ProductResponseDTO.from(product);
    }

    @Transactional(readOnly = true)
    public ProductResponseDTO getBySlug(String slug) {
        Product product = repo.findBySlug(slug)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "상품이 없습니다. slug=" + slug));
        return ProductResponseDTO.from(product);
    }

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

        product.clearSizes();
        for (ProductSizeDTO s : sizes) {
            Sku sku = Sku.builder()
                    .gripSize(s.getSize())
                    .isActive(true)
                    .price(price)
                    .skuCode(slug + "-" + s.getSize().toLowerCase())
                    .build();

            Inventory inventory = new Inventory(sku, s.getStock(), 0);
            sku.setInventory(inventory);

            product.addSize(sku);
        }

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

        if (title != null && !title.isBlank()) {
            product.setTitle(title);
            product.setSlug(generateSlug(title));
        }

        if (description != null) {
            product.setDescription(description);
        }

        if (price != null && price > 0) {
            product.setPrice(price);
        }

        if (brandId != null) {
            Brand brand = brandRepository.findById(brandId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "브랜드가 존재하지 않습니다. id=" + brandId));
            product.setBrand(brand);
        }

        if (categoryId != null) {
            NavMenu category = navMenuRepo.findById(categoryId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "카테고리가 존재하지 않습니다. id=" + categoryId));
            product.setCategory(category);
        }

        if (image != null && !image.isEmpty()) {
            fileStorage.deleteByPath(product.getImagePath());
            var stored = fileStorage.save(image);
            product.setImageUrl(stored.url());
            product.setImagePath(stored.filePath());
        }

        List<ProductSizeDTO> sizes = parseSizes(sizesJson);
        ProductSpecDTO specDto = parseSpec(specJson);

        product.clearSizes();
        for (ProductSizeDTO s : sizes) {
            Sku sku = Sku.builder()
                    .gripSize(s.getSize())
                    .isActive(true)
                    .price(price)
                    .skuCode(product.getSlug() + "-" + s.getSize().toLowerCase())
                    .build();

            Inventory inventory = new Inventory(sku, s.getStock(), 0);
            sku.setInventory(inventory);

            product.addSize(sku);
        }

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

        Product saved = repo.save(product);
        return ProductResponseDTO.from(saved);
    }

    @Transactional
    public void delete(Long id) {
        Product product = repo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "상품이 없습니다. id=" + id));

        fileStorage.deleteByPath(product.getImagePath());
        repo.delete(product);
    }
}