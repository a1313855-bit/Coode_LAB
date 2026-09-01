package com.example.demo.service.impl;

// ========== Java ==========
import java.util.List;

// ========== Spring ==========
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// ========== Model ==========
import com.example.demo.model.Outfit;
import com.example.demo.model.User;

// ========== Repository ==========
import com.example.demo.repository.OutfitRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.OutfitService;
import com.example.demo.util.SelectPartOfData;

/**
 * OutfitService 的實作類別。
 *
 * 負責處理使用者穿搭的建立、查詢、重新命名與刪除等商業邏輯。
 */
@Service
public class OutfitServiceImpl implements OutfitService {

    // ╔════════════╗
    // ║ Repository ║
    // ╚════════════╝

    private final OutfitRepository outfitRepository;
    private final UserRepository userRepository;

    // ╔═════════╗
    // ║ Service ║
    // ╚═════════╝

    // private final UserService userService;

    // ╔═════════════╗
    // ║ Constructor ║
    // ╚═════════════╝

    /**
     * 建構 OutfitServiceImpl。
     *
     * @param outfitRepository Outfit 資料存取元件
     * @param userService      User 業務邏輯元件
     */
    public OutfitServiceImpl(
            OutfitRepository outfitRepository,
            UserRepository userRepository)
    /* ,UserService userService */ {

        this.outfitRepository = outfitRepository;
        this.userRepository = userRepository;
        // this.userService = userService;
    }

    // ╔════════════════╗
    // ║ Create Outfit ║
    // ╚════════════════╝

    /**
     * 建立新的穿搭。
     *
     * 建立前會確認：
     * 1、userId 不得為空。
     * 2、使用者必須存在。
     * 3、穿搭名稱不得為空白。
     * 4、穿搭名稱不得超過資料庫欄位限制。
     *
     * @param userId 建立穿搭的使用者 ID
     * @param name   穿搭名稱
     * @return 建立完成的 Outfit
     * @throws IllegalArgumentException 使用者不存在或穿搭名稱不符合規則時
     */
    @Override
    @Transactional
    public Outfit createOutfit(Long userId, String name) {

        // 1、確認使用者存在
        User user = getUser(userId);

        // 2、檢查並整理穿搭名稱
        String normalizedName = validateAndNormalizeName(name);

        // 3、建立新的 Outfit
        Outfit outfit = new Outfit();

        // 4、設定穿搭名稱
        outfit.setName(normalizedName);

        // 5、設定此穿搭所屬的使用者
        outfit.setUser(user);

        // 6、儲存至資料庫並回傳
        return outfitRepository.save(outfit);
    }

    // ╔═══════════════╗
    // ║ Find Outfit ║
    // ╚═══════════════╝

    /**
     * 根據穿搭 ID 查詢單一穿搭。
     *
     * @param outfitId 穿搭 ID
     * @return 查詢到的 Outfit
     * @throws IllegalArgumentException outfitId 為空或找不到穿搭時
     */
    @Override
    @Transactional(readOnly = true)
    public Outfit findById(Long outfitId) {

        return getOutfit(outfitId);
    }

    /**
     * 查詢指定使用者建立的所有穿搭。
     *
     * 查詢前會先確認使用者是否存在，
     * 避免將不存在的 userId 當成沒有穿搭資料。
     *
     * @param userId 使用者 ID
     * @return 該使用者建立的 Outfit 清單
     * @throws IllegalArgumentException userId 為空或找不到使用者時
     */
    @Override
    @Transactional(readOnly = true)
    public SelectPartOfData.Result<Outfit> findByUserId(Long userId, int page) {

        // 1、確認使用者存在
        User user = getUser(userId);

        // 2、查詢此使用者建立的所有穿搭
        List<Outfit> all = outfitRepository.findByUser(user);
        return SelectPartOfData.pageOf10(all, page);
    }

    // ╔════════════════╗
    // ║ Update Outfit ║
    // ╚════════════════╝

    /**
     * 修改指定穿搭的名稱。
     *
     * 修改前會確認：
     * 1、outfitId 不得為空。
     * 2、穿搭必須存在。
     * 3、新名稱不得為空白。
     * 4、新名稱不得超過資料庫欄位限制。
     *
     * @param outfitId 穿搭 ID
     * @param name     新的穿搭名稱
     * @return 修改完成的 Outfit
     * @throws IllegalArgumentException 找不到穿搭或名稱不符合規則時
     */
    @Override
    @Transactional
    public Outfit updateOutfitName(
            Long outfitId,
            String name) {

        // 1、確認穿搭存在
        Outfit existingOutfit = getOutfit(outfitId);

        // 2、檢查並整理新的穿搭名稱
        String normalizedName = validateAndNormalizeName(name);

        // 3、修改穿搭名稱
        existingOutfit.setName(normalizedName);

        /*
         * existingOutfit 是目前 Transaction 管理中的 Entity，
         * 理論上 Transaction 結束時 JPA 會自動進行 dirty checking。
         *
         * 此處仍保留 save()，
         * 讓目前專案的 Service 寫法保持一致並明確表達儲存行為。
         */
        return outfitRepository.save(existingOutfit);
    }

    // ╔════════════════╗
    // ║ Delete Outfit ║
    // ╚════════════════╝

    /**
     * 刪除指定穿搭。
     *
     * @param outfitId 要刪除的穿搭 ID
     * @throws IllegalArgumentException outfitId 為空或找不到穿搭時
     */
    @Override
    @Transactional
    public void deleteOutfit(Long outfitId) {

        // 1、確認穿搭存在
        Outfit outfit = getOutfit(outfitId);

        // 2、刪除穿搭
        outfitRepository.delete(outfit);
    }

    // ╔══════════════════════╗
    // ║ Private - Get User ║
    // ╚══════════════════════╝

    /**
     * 根據 userId 取得使用者資料。
     *
     * User 資料由 UserService 負責，
     * OutfitService 不直接存取 UserRepository。
     *
     * @param userId 使用者 ID
     * @return 查詢到的 User
     * @throws IllegalArgumentException userId 為空或找不到使用者時
     */
    private User getUser(Long userId) {

        if (userId == null) {
            throw new IllegalArgumentException(
                    "使用者 ID 不得為空");
        }

        return userRepository
                .findById(userId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "找不到使用者資料"));
    }

    // ╔════════════════════════╗
    // ║ Private - Get Outfit ║
    // ╚════════════════════════╝

    /**
     * 根據 outfitId 取得穿搭資料。
     *
     * @param outfitId 穿搭 ID
     * @return 查詢到的 Outfit
     * @throws IllegalArgumentException outfitId 為空或找不到穿搭時
     */
    private Outfit getOutfit(Long outfitId) {

        // 1、檢查 outfitId
        if (outfitId == null) {
            throw new IllegalArgumentException(
                    "穿搭 ID 不得為空");
        }

        // 2、根據 outfitId 查詢 Outfit
        return outfitRepository
                .findById(outfitId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "找不到穿搭資料"));
    }

    // ╔══════════════════════════════╗
    // ║ Private - Validate Name ║
    // ╚══════════════════════════════╝

    /**
     * 驗證並整理穿搭名稱。
     *
     * outfits.name 在資料庫設定為 VARCHAR(100)，
     * 因此 Service 層同步限制最大長度為 100 個字元。
     *
     * @param name 穿搭名稱
     * @return 去除前後空白後的穿搭名稱
     * @throws IllegalArgumentException 名稱為空或超過長度限制時
     */
    private String validateAndNormalizeName(String name) {

        // 1、不可為 null 或空白
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException(
                    "請給你的穿搭一個風格名稱");
        }

        // 2、移除名稱前後多餘空白
        String normalizedName = name.trim();

        // 3、檢查資料庫 VARCHAR(100) 長度限制
        if (normalizedName.length() > 100) {
            throw new IllegalArgumentException(
                    "穿搭名稱不可超過 100 個字元");
        }

        return normalizedName;
    }
}
