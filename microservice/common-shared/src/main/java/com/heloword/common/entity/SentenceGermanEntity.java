package com.heloword.common.entity;

import javax.persistence.Entity;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import com.heloword.common.base.entity.BaseSentenceEntity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;


@NoArgsConstructor
@Data
@Entity
@Table(name = "SENTENCE_GERMAN")
@SequenceGenerator(initialValue = 1, name = "id_generator", sequenceName = "sentence_german_seq")
@SuperBuilder
public class SentenceGermanEntity extends BaseSentenceEntity {

}
