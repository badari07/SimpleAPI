package com.example.user.repository;

import com.example.user.entity.SocialLogin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SocialLoginRepository extends JpaRepository<SocialLogin, Long> {
    
    Optional<SocialLogin> findByProviderAndProviderId(SocialLogin.Provider provider, String providerId);
    
    List<SocialLogin> findByUser_Id(Long userId);
    
    @Query("SELECT s FROM SocialLogin s WHERE s.user.id = :userId AND s.provider = :provider")
    Optional<SocialLogin> findByUserIdAndProvider(@Param("userId") Long userId, @Param("provider") SocialLogin.Provider provider);
    
    boolean existsByProviderAndProviderId(SocialLogin.Provider provider, String providerId);
}
