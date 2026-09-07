package com.example.demo.repository;

// ========== Spring Data JPA ==========
import org.springframework.data.jpa.repository.JpaRepository;

// ========== Model ==========
import com.example.demo.model.Outfit;
import com.example.demo.model.User;

// ========== Java ==========
import java.util.List;

/*
Outfit：代表這個 Repository 要操作的 Entity
Long:代表Outfit PK outfitId的資料類別
*/
public interface OutfitRepository extends JpaRepository<Outfit,Long>{


// ================ 查詢使用者的所有穿搭 ================
        /*
        根據User查詢此user建立的所有Outfit
        @param user要查詢使用者
        @return此使用者的所有穿搭
        */
        List<Outfit> findByUser(User user);



 //目前功能尚未使用，所以先註解保留    
/* ==================查「這個使用者」＋「名稱包含Keyword」的穿搭============
List<Outfit> findByUserAndNameContaining(User user,String name);*/
    
    }

    
