package me.zohar.runscore.gatheringcode.repo;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import me.zohar.runscore.gatheringcode.domain.GatheringCodeUsage;

public interface GatheringCodeUsageRepo
		extends JpaRepository<GatheringCodeUsage, String>, JpaSpecificationExecutor<GatheringCodeUsage> {

	List<GatheringCodeUsage> findByUserAccountId(String userAccountId);

}
