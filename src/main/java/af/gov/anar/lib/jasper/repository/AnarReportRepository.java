package af.gov.anar.lib.jasper.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import org.springframework.data.jpa.repository.Query;
import af.gov.anar.lib.jasper.repository.AnarReportRepository;
import af.gov.anar.lib.jasper.entity.AnarReport;

@Repository
public interface AnarReportRepository extends JpaRepository<AnarReport, Long> {
    public AnarReport findById(String id);
}