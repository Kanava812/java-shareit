package ru.practicum.shareit.booking;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;
import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {
    List<Booking> findAllByBookerIdOrderByStartDesc(Long bookerId);

    List<Booking> findAllByBookerIdAndEndBeforeOrderByStartDesc(Long bookerId, LocalDateTime end);

    List<Booking> findAllByBookerIdAndStartAfterOrderByStartDesc(Long bookerId, LocalDateTime start);

    List<Booking> findAllByBookerIdAndStatusOrderByStartDesc(Long bookerId, BookingStatus status);

    @Query("SELECT b FROM Booking b WHERE b.booker.id = :bookerId AND b.start <= :now AND b.end >= :now " +
            "ORDER BY b.start DESC")
    List<Booking> findAllByBookerIdAndCurrentTime(@Param("bookerId") Long bookerId, @Param("now") LocalDateTime now);

    List<Booking> findAllByItemOwnerIdOrderByStartDesc(Long bookerId);

    List<Booking> findAllByItemOwnerIdAndEndBeforeOrderByStartDesc(Long bookerId, LocalDateTime end);

    List<Booking> findAllByItemOwnerIdAndStartAfterOrderByStartDesc(Long bookerId, LocalDateTime start);

    List<Booking> findAllByItemOwnerIdAndStatusOrderByStartDesc(Long bookerId, BookingStatus status);

    @Query("SELECT b FROM Booking b WHERE b.item.owner.id = :ownerId AND b.start <= :now AND b.end >= :now " +
            "ORDER BY b.start DESC")
    List<Booking> findAllByOwnerIdAndCurrentTime(@Param("ownerId") Long ownerId, @Param("now") LocalDateTime now);

    @Query("SELECT b.item.id as itemId, b.start as start, b.end as end FROM Booking b WHERE b.item.owner.id = :ownerId " +
            "ORDER BY b.item.id, b.start ASC")
    List<BookingDates> findAllBookingsByOwnerId(@Param("ownerId") Long ownerId);

    @Query("SELECT CASE WHEN COUNT(b) > 0 THEN true ELSE false END FROM Booking b WHERE b.booker.id = :bookerId " +
            "AND b.item.id = :itemId AND b.status = :status AND b.end < :end")
    boolean existsByBookerIdAndItemIdAndStatusAndEndBefore(@Param("bookerId") Long bookerId, @Param("itemId") Long itemId,
                                                           @Param("status") BookingStatus status,
                                                           @Param("end") LocalDateTime end);

    List<BookingDates> findAllBookingsByItemId(Long itemId);
}