package af.gov.anar.lib.jasper.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import org.springframework.data.jpa.repository.Query;
import af.gov.anar.lib.jasper.repository.ReportRepository;
import af.gov.anar.lib.jasper.entity.Report;

@Repository
public interface ReportRepository extends JpaRepository<Report, Long> {
    public Report findById(String id);
}