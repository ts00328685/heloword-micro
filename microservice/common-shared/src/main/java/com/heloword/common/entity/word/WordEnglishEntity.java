package com.heloword.common.entity.word;

import javax.persistence.Entity;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import com.heloword.common.base.entity.BaseWordEntity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@NoArgsConstructor
@SuperBuilder
@Data
@Entity
@SequenceGenerator(initialValue = 1, name = "id_generator", sequenceName = "word_english_seq")
@Table(name = "WORD_ENGLISH")
public class WordEnglishEntity extends BaseWordEntity {

}
