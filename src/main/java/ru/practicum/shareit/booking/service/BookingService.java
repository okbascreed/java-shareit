package ru.practicum.shareit.booking.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingInfoDto;
import ru.practicum.shareit.booking.mapper.BookingMapper;
import ru.practicum.shareit.booking.model.Booking;
import ru.practicum.shareit.booking.repository.BookingRepository;
import ru.practicum.shareit.booking.validator.BookingValidator;
import ru.practicum.shareit.enums.BookingState;
import ru.practicum.shareit.enums.BookingStatus;
import ru.practicum.shareit.exception.InvalidEntityException;
import ru.practicum.shareit.exception.ObjectNotFoundException;
import ru.practicum.shareit.exception.UnknownBookingState;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.item.repository.ItemJpaRepository;
import ru.practicum.shareit.user.model.User;
import ru.practicum.shareit.user.repository.UserJpaRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class BookingService {

    private static final String USER_ERROR = "User not found.";
    private static final String ITEM_ERROR = "Item not found.";
    private static final String BOOKING_ERROR = "Booking not found.";
    private static final String BOOKING_STATE_ERROR = "Unknown booking state.";

    private final BookingRepository repository;
    private final UserJpaRepository userRepository;
    private final ItemJpaRepository itemRepository;

    @Autowired
    public BookingService(BookingRepository repository,
                          UserJpaRepository userRepository,
                          ItemJpaRepository itemRepository) {
        this.repository = repository;
        this.userRepository = userRepository;
        this.itemRepository = itemRepository;
    }

    public BookingInfoDto addBooking(Long userId, BookingDto bookingDto) {
        if (!BookingValidator.bookingValidate(bookingDto)) {
            throw new InvalidEntityException("BookingDto is not valid.");
        }

        Booking booking = BookingMapper.toBooking(bookingDto);

        Item item = itemRepository.findById(bookingDto.getItemId())
                .orElseThrow(() -> new ObjectNotFoundException(ITEM_ERROR));

        if (!item.getAvailable()) {
            throw new InvalidEntityException(ITEM_ERROR);
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ObjectNotFoundException(USER_ERROR));

        if (user.getId().equals(item.getOwner().getId())) {
            throw new ObjectNotFoundException(USER_ERROR);
        }
        booking.setBooker(user);
        booking.setItem(item);
        booking.setStatus(BookingStatus.WAITING);

        return BookingMapper.toBookingInfoDto(repository.save(booking));
    }

    public BookingInfoDto updateBookingStatus(Long userId, Long bookingId, Boolean approved) {

        Booking booking = repository.findById(bookingId)
                .orElseThrow(() ->
                        new ObjectNotFoundException(BOOKING_ERROR));

        User user = userRepository.findById(userId).orElseThrow(() ->
                new ObjectNotFoundException(USER_ERROR));

        Item item = booking.getItem();

        if (!item.getOwner().getId().equals(userId)) {
            throw new ObjectNotFoundException("Unable to approve booking. User is not owner for this item.");
        }

        BookingStatus bookingStatus = approved ? BookingStatus.APPROVED : BookingStatus.REJECTED;

        if (booking.getStatus() == bookingStatus) {
            throw new InvalidEntityException("Status already " + bookingStatus.toString());
        }

        booking.setStatus(bookingStatus);

        return BookingMapper.toBookingInfoDto(repository.save(booking));
    }

    public BookingInfoDto getCurrentBooking(Long userId, Long bookingId) {
        Booking booking = repository.findById(bookingId)
                .orElseThrow(() ->
                        new ObjectNotFoundException(BOOKING_ERROR));

        if (!userId.equals(booking.getItem().getOwner().getId()) && !userId.equals(booking.getBooker().getId())) {
            throw new ObjectNotFoundException("This user not item owner.");
        }

        return BookingMapper.toBookingInfoDto(booking);
    }

    public List<BookingInfoDto> getBooking(Long userId, String stateParam) {

        BookingState bookingState = checkState(stateParam);

        Sort sort = Sort.by(Sort.Direction.DESC, "start");
        List<Booking> bookingList = new ArrayList<>();

        User user = userRepository.findById(userId).orElseThrow(()
                -> new ObjectNotFoundException(USER_ERROR));

        switch (bookingState) {
            case ALL:
                bookingList = repository.findAllByBookerIdOrderByStartDesc(user.getId());
                break;
            case PAST:
                bookingList = repository.findAllByBookerIdAndEndIsBefore(user.getId(), LocalDateTime.now(), sort);
                break;
            case FUTURE:
                bookingList = repository.findAllByBookerIdAndStartIsAfter(user.getId(), LocalDateTime.now(), sort);
                break;
            case CURRENT:
                bookingList = repository.findAllByBookerIdAndStartIsBeforeAndEndIsAfter(user.getId(), LocalDateTime.now(), LocalDateTime.now(), sort);
                break;
            case WAITING:
                bookingList = repository.findAllByBookerIdAndStatus(user.getId(), BookingStatus.WAITING);
                break;
            case REJECTED:
                bookingList = repository.findAllByBookerIdAndStatus(user.getId(), BookingStatus.REJECTED);
                break;
            default:
                throw new UnknownBookingState(BOOKING_STATE_ERROR);
        }

        return bookingList.isEmpty() ? Collections.emptyList() : bookingList.stream()
                .map(BookingMapper::toBookingInfoDto)
                .collect(Collectors.toList());
    }

    public List<BookingInfoDto> getOwnerBooking(Long userId, String stateParam) {

        BookingState bookingState = checkState(stateParam);

        Sort sort = Sort.by(Sort.Direction.DESC, "start");
        List<Booking> bookingList = new ArrayList<>();

        User user = userRepository.findById(userId).orElseThrow(() ->
                new ObjectNotFoundException(USER_ERROR));

        switch (bookingState) {
            case ALL:
                bookingList = repository.findAllByItem_Owner_IdOrderByStartDesc(user.getId());
                break;
            case PAST:
                bookingList = repository.findAllByItem_Owner_IdAndEndIsBefore(user.getId(), LocalDateTime.now(), sort);
                break;
            case FUTURE:
                bookingList = repository.findAllByItem_Owner_IdAndStartIsAfter(user.getId(), LocalDateTime.now(), sort);
                break;
            case CURRENT:
                bookingList = repository.findAllByItem_Owner_IdAndStartIsBeforeAndEndIsAfter(user.getId(), LocalDateTime.now(), LocalDateTime.now(), sort);
                break;
            case WAITING:
                bookingList = repository.findAllByItem_Owner_IdAndStatus(user.getId(), BookingStatus.WAITING);
                break;
            case REJECTED:
                bookingList = repository.findAllByItem_Owner_IdAndStatus(user.getId(), BookingStatus.REJECTED);
                break;
            default:
                throw new UnknownBookingState(BOOKING_STATE_ERROR);
        }

        return bookingList.isEmpty() ? Collections.emptyList() : bookingList.stream()
                .map(BookingMapper::toBookingInfoDto)
                .collect(Collectors.toList());
    }

    private BookingState checkState(String state) {
        try {
            return BookingState.valueOf(state.toUpperCase());
        } catch (IllegalArgumentException exception) {
            throw new UnknownBookingState(String.format("Unknown state: %s", state));
        }
    }
}
