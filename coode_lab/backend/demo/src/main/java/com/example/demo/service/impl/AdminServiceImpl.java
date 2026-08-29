package com.example.demo.service.impl;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.model.Admin;
import com.example.demo.repository.AdminRepository;
import com.example.demo.service.AdminService;

@Service
public class AdminServiceImpl implements AdminService{

    private final AdminRepository adminRepository;

    //建構子注入
    public AdminServiceImpl(AdminRepository adminRepository){
        this.adminRepository=adminRepository;
    }


    //管理員登入
    @Override
    public Admin login(String email, String password) {
        /*
        1、adminRepository.findByEmail(email) => 回傳Optional<Admin>
        若查到:則Admin物件存進admin變數
        若查不到:orElseThrow()會拋出IllegalArgumentException("顯示管理員帳號不存在") 
        */
        Admin admin=adminRepository.findByEmail(email)
        .orElseThrow(()->
        new IllegalArgumentException("管理員帳號不存在")); 
        /*
        2、.admin.gerPassword() => 取得資料庫中此管理員密碼
        ! => NOT(不相等)
        .equals(password)=>比較兩個字串內容是否相同
        if=> 若輸入的密碼和資料庫不同則拋出("密碼錯誤")
        */
        if(!admin.getPassword().equals(password)){
        throw new IllegalArgumentException("密碼錯誤");
        }

        return admin;
    } 

    //新增管理員
    @Override
    public Admin createAdmin(Admin admin) {
        /*
        1、檢查此管理員Email是否存在
        admin.geteEmail() => 取得要新增的管理員Email
        existsByEmail(...) => 到資料庫檢查是否已經有相同Email
        最後回傳 true 代表已存在，false 代表不存在
        */
        if(adminRepository.existsByEmail(admin.getEmail())){

            /*
            2、如果Email已存在，就不允許新增
            post man:500 "Internal Server Error"
            */
            throw new IllegalArgumentException("Email 已存在");
        }
        /*
        3、如果Email不存在，就把Admin物件存進mysql
        save(admin)會執行新增資料的動作
        回傳儲存後的Admin物件
        */
        return adminRepository.save(admin);
    }


    //查詢單一管理員
    @Override
    public Admin findById(Long adminId) {
        /*
        1、根據管理員的id到mysql查詢管理員資料
        findById(adminId)是JpaReostory提供的方法，會回傳Optional<Admin>
        */      
        return adminRepository.findById(adminId)
        /*
        2、如果找不到adminId對應的管理員資料，就拋出IllegalArgumentException("找不到管理員資料")
        */
        .orElseThrow(()->
        new IllegalArgumentException("找不到管理員資料"));
    }


    //查詢全部管理員
    @Override
    public List<Admin> findAll() {
        /*
        1、呼叫AdminRepository的findAll()方式
        finAll()是JpaRepository內建的方法，會回傳List<Admin>
        這個方法會到admin資料表查詢所有管理員資料
        
        2、回傳List<Admin>給Controller
        */
        return adminRepository.findAll();   
    }


    //修改管理員資料
    @Override
    @Transactional
    public Admin updateAdmin(Admin admin) {
        /*
        1、先取得傳進來的 Admin 物件的id，admin.getId() => 取得要修改的管理員id
        再使用 JpaRepository 內建的 findById() 到資料庫查詢這個id的管理員是否存在
        findById() 回傳 Optional<Admin>
        若有找到:則原資料庫的Admin物件存進 existingAdmin 變數
        */
       Admin existingAdmin=adminRepository.findById(admin.getAdminId())

            /*
            2、如果資料庫找不到這個管理員ID
            就拋出 IllgalArgumentEception("找不到管理員資料")
            並停止修改動作
            */
            .orElseThrow(()->
            new IllegalArgumentException("找不到管理員資料"));

            // → 判斷原本的 Email 和修改後的 Email 是否不同         // → 到資料庫檢查修改後的 Email 是否已經存在
            if(!existingAdmin.getEmail().equals(admin.getEmail()) && adminRepository.existsByEmail(admin.getEmail())){
               
            /*如果「管理員有修改 Email」而且「新的 Email 已經被其他帳號使用」
            就不允許修改，並拋出例外*/
                throw new IllegalArgumentException("Email 已存在");
            }
            /*
            3、只修改 '允許修改' 的欄位
            目前Admin物件只有email可以修改，其他欄位不允許修改
            */
            existingAdmin.setEmail(admin.getEmail());


            return adminRepository.save(existingAdmin);
    }

    //修改管理員密碼
    @Override
    @Transactional 
    /*
    @Transactional2代表整個修改流程放在同一個交易處理中
    若中間發生錯誤，Spring會幫忙Rollback(回滾資料)
    避免資料只改一半
    */
    public void changePassword(Long adminId, String newPassword) {
        /*
        1、根據adminId到資料庫查詢管理員
        findById(adminId)是JpaRepository內建的方法
        會回傳Optional<Admin> 有可能查的到，也可能查不到
        */
        Admin admin=adminRepository.findById(adminId)

        /*
        2、如果找不到adminId對應的管理員資料，就拋出IllegalArgumentException
        並停止後繼續修改密碼的流程
        */
        .orElseThrow(()->
        new IllegalArgumentException("找不到管理員資料"));

        /*
        3、把查到Admin物件中的password
        修改成使用者傳進來的新password
        */
        admin.setPassword(newPassword);

        /*
        4、將修改後的Admin物件存回資料庫
        save(admin)會執行Update的動作
        */
        adminRepository.save(admin);
    }

    //刪除管理管理員帳號
    @Override
    @Transactional
    public void deleteAdmin(Long adminId) {
        /*
        1、檢查adminId是否存在
        existsById(adminId)是JpaRepository內建的方法
        回傳boolean: true -> 存在，false -> 不存在
        if !adminRepository.existsById(adminId) 意思是: " 如果這個adminId不存在 "
        */
       if(!adminRepository.existsById(adminId)){

        /*
        2、不存在就拋出IllegalArgumentException
        */
        throw new IllegalArgumentException("找不到管理員資料");
       }
       /*
       3、如果adminId存在就刪除這個管理員帳號
         deleteById(adminId)是JpaRepository內建的方法
       */
       adminRepository.deleteById(adminId);
    }
    
}
