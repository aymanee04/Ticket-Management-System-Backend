package ma.bank.ticketmanagementsystembackend;

import ma.bank.ticketmanagementsystembackend.dtos.PagedResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

public class PaginationUtils {

    private PaginationUtils() {}

    public static <T> PagedResponse<T> toPagedResponse(Page<T> p) {
        return new PagedResponse<>(
                p.getContent(), p.getNumber(), p.getSize(),
                p.getTotalElements(), p.getTotalPages(),
                p.isLast(), p.isFirst()
        );
    }

    public static Pageable buildPageable(int page, int size, String sortBy, String direction) {
        Sort.Direction dir = direction.equalsIgnoreCase("asc")
                ? Sort.Direction.ASC
                : Sort.Direction.DESC;

        return PageRequest.of(page, size, Sort.by(dir, sortBy));
    }
}
