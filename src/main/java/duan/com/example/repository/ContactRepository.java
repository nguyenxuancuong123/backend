package duan.com.example.repository;

import duan.com.example.entity.Contact;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface ContactRepository extends JpaRepository<Contact, Long> {

    List<Contact> findBySenderOrReceiverOrderByTimestampAsc(String sender, String receiver);
    List<Contact> findAllByOrderByTimestampAsc();

    @Modifying
    @Transactional
    @Query("UPDATE Contact l SET l.sender = :newSender WHERE l.sender = :oldSender")
    int updateSender(@Param("oldSender") String oldSender, @Param("newSender") String newSender);

    @Modifying
    @Transactional
    @Query("UPDATE Contact l SET l.receiver = :newReceiver WHERE l.receiver = :oldReceiver")
    int updateReceiver(@Param("oldReceiver") String oldReceiver, @Param("newReceiver") String newReceiver);
}