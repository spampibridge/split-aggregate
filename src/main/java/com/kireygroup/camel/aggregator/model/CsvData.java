package com.kireygroup.camel.aggregator.model;


import org.apache.camel.dataformat.bindy.annotation.CsvRecord;
import org.apache.camel.dataformat.bindy.annotation.DataField;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@CsvRecord(separator = ";")
@ToString
@Entity
@Table(name = "CSV_DATA", uniqueConstraints = { @UniqueConstraint(columnNames = { "designation", "speed" }) })
public class CsvData {
	
	@Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
	
	@DataField(pos = 1, required = true)
    private String designation;
	
	@DataField(pos = 2, required = true)
    private Long speed;

}
