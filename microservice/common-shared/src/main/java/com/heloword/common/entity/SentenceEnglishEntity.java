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
@SuperBuilder
@SequenceGenerator(initialValue = 1, name = "id_generator", sequenceName = "sentence_english_seq")
@Table(name = "SENTENCE_ENGLISH")
public class SentenceEnglishEntity extends BaseSentenceEntity {

}
