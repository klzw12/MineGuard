package com.klzw.service.trip.repository;

import com.klzw.common.mongodb.repository.BaseMongoRepository;
import com.klzw.service.trip.document.TripTrackDocument;
import org.springframework.stereotype.Repository;

/**
 * 行程轨迹MongoDB Repository
 */
@Repository
public interface TripTrackMongoRepository extends BaseMongoRepository<TripTrackDocument, String> {
    
}
