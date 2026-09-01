package com.example.demo.service;


// ========== Model ==========
import com.example.demo.model.Outfit;
import com.example.demo.util.SelectPartOfData;

// ========== Java ==========
import java.util.List;


public interface OutfitService {
    
//==================== 儲存並命名一套穿搭 ====================
/*
建立一套新的 Outfit，使用者完成搭配後，可替這套穿搭命名並儲存
@Param userId 使用者 ID
@Param name 穿搭名稱
@return 建立完成的 Outfit
*/
Outfit createOutfit(Long userId,String name);


//==================== 查詢某一套穿搭 ====================
/*
根據outfitId查詢單一穿搭 
@param outfitId 穿搭 ID
@return 查詢到的outfit
*/
Outfit findById(Long outfitId);

//==================== 我的穿搭 ====================
// 根據userId查此使用者所儲存的所有穿搭 (固定每頁10筆,page 從 0 開始)
SelectPartOfData.Result<Outfit> findByUserId(Long userId, int page);

//==================== 修改穿搭名稱 ====================
/*
修改穿搭名稱
@param outfitId 穿搭 ID
@param name 新的穿搭名稱
@return 修改完成的 Outfit
*/
Outfit updateOutfitName(Long outfitId,String name);

//==================== 刪除整套穿搭 ====================
/*
根據 outfitId 刪除整套穿搭
param outfitId 要刪除的穿搭 ID
*/
void deleteOutfit(Long outfitId);

}
