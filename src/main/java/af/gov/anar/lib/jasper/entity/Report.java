/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package af.gov.anar.lib.jasper.entity;

import java.time.LocalDateTime;

import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.CreationTimestamp;
import com.fasterxml.jackson.annotation.JsonIgnore;
import org.hibernate.envers.Audited;
import lombok.*;
import java.util.Date;
import java.time.LocalDate;
import java.time.ZoneId;

@Entity
@Table(name = "report")
@Getter
@Setter
@AllArgsConstructor
@Builder
@ToString
@Audited
public class Report{

    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "oid", unique = true)
    private long oid;

    private String month;
    private String year;

    private Boolean deleted = false;

    @Id
    public String id;
    
    private Date deletedAt;
    private String deletedBy;

    @Column(name = "name", unique = true)
    private String name;

    @Column(name = "description")
    private String description;
  
    @Column(name = "xml_content")
    @Type(type = "text")
    private String xmlContent;

    @Column(name = "parameters")
    @Type(type = "text")
    private String parameters;

    @Column(name = "organization")
    @Type(type = "text")
    private String organization;

    @Column(name = "organization_code")
    @Type(type = "text")
    private String organizationCode;

    @Column(name = "created_at")
    @CreationTimestamp
    private LocalDateTime createdAt;

    public Report(String name, String description) {
            this.name = name;
            this.description = description;
    }

    @Override
	public String toString() {
        return "Report [id=" + id + ", name=" + name + ", description=" + description + ", created_at=" + createdAt  + "]";
	}

        
    public Report() {
        if (id == null) {
            this.id = String.format("DABS%s%s", String.valueOf(System.nanoTime() / 100000000),
                    String.valueOf(System.currentTimeMillis() / 100000000));
        }

        if (oid == 0) {
            this.oid = System.nanoTime();
        }

        Date date = new Date();
        LocalDate localDate = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        this.month = localDate.getMonthValue() + "";
        this.year = localDate.getYear() + "";
    }

}