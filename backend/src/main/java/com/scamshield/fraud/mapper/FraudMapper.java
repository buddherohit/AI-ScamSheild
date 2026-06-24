package com.scamshield.fraud.mapper;

import com.scamshield.fraud.dto.FraudReportResponse;
import com.scamshield.fraud.dto.ThreatIndicatorResponse;
import com.scamshield.fraud.entity.FraudReport;
import com.scamshield.fraud.entity.ThreatIndicator;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface FraudMapper {

    @Mapping(target = "entityType", source = "entity.type")
    @Mapping(target = "entityValue", source = "entity.value")
    @Mapping(target = "categoryCode", source = "category.code")
    @Mapping(target = "categoryDisplayName", source = "category.displayName")
    @Mapping(target = "reporterEmail", source = "reporter.email")
    FraudReportResponse toReportResponse(FraudReport report);

    ThreatIndicatorResponse toThreatIndicatorResponse(ThreatIndicator indicator);
}
