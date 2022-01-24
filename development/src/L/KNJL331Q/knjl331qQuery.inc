<?php

require_once('for_php7.php');
class knjl331qQuery extends Query
{
    //名称マスタより取得
    public function getNameMst($year, $namecd1, $namecd2 = "")
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     NAME1, ";
        $query .= "     NAMECD2 || ':' || NAME1 AS LABEL, ";
        $query .= "     NAMECD2 AS VALUE,";
        $query .= "     NAMESPARE2 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "         YEAR    = '".$year."' ";
        $query .= "     AND NAMECD1 = '".$namecd1."' ";
        if ($namecd2) {
            $query .= "     AND NAMECD2    = '".$namecd2."' ";
        }
        if ($namecd1 == "L003") {
            $query .= "     AND NAMESPARE3 = '".SCHOOLKIND."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //名称マスタより取得L004
    public function getNameMstL004($year)
    {
        $query  = " SELECT ";
        $query .= "     T1.NAMECD2 AS VALUE, ";
        $query .= "     T1.NAMECD2 || ':' || T1.NAME1 AS LABEL, ";
        $query .= "     T3.NAMESPARE2 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST T1 ";
        $query .= "     INNER JOIN ( ";
        $query .= "         SELECT ";
        $query .= "             ABBV3 AS NAMECD2 ";
        $query .= "         FROM ";
        $query .= "             V_NAME_MST ";
        $query .= "         WHERE ";
        $query .= "             YEAR = '".$year."' ";
        $query .= "             AND NAMECD1 = 'L004' ";
        $query .= "         GROUP BY ";
        $query .= "             ABBV3 ";
        $query .= "     ) T2 ON T2.NAMECD2 = T1.NAMECD2 ";
        $query .= "     LEFT JOIN ( ";
        $query .= "         SELECT ";
        $query .= "             ABBV3 AS NAMECD2, ";
        $query .= "             '1' AS NAMESPARE2 ";
        $query .= "         FROM ";
        $query .= "             V_NAME_MST ";
        $query .= "         WHERE ";
        $query .= "             YEAR = '".$year."' ";
        $query .= "             AND NAMECD1 = 'L004' ";
        $query .= "             AND NAMESPARE2 = '1' ";
        $query .= "         GROUP BY ";
        $query .= "             ABBV3 ";
        $query .= "     ) T3 ON T3.NAMECD2 = T1.NAMECD2 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".$year."' ";
        $query .= "     AND T1.NAMECD1 = 'L045' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //受験科目
    public function getTestSubclasscd($model, $testdiv)
    {
        $query  = " SELECT ";
        $query .= "     NAMECD2 AS VALUE, ";
        $query .= "     NAME1   AS LABEL ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR        = '{$model->ObjYear}' AND ";
        $query .= "     NAMECD1     = 'L009' AND ";
        $query .= "     NAME1 IS NOT NULL AND ";
        //満点マスタの設定が前提
        $query .= "     NAMECD2 IN (SELECT ";
        $query .= "                     TESTSUBCLASSCD ";
        $query .= "                 FROM ";
        $query .= "                     ENTEXAM_PERFECT_MST ";
        $query .= "                 WHERE ";
        $query .= "                     ENTEXAMYEAR     = '{$model->ObjYear}' AND ";
        $query .= "                     APPLICANTDIV    = '".$model->field["APPLICANTDIV"]."' AND ";
        $query .= "                     TESTDIV         = '{$testdiv}' ";
        $query .= "                 ) ";
        if ($testdiv == "3") {
            $query .= "    AND NAMECD2 <> '6' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //CSVデータ
    public function getCsvData($model)
    {
        $query  = " WITH MARK_DATA AS ( ";
        $query .= " SELECT DISTINCT ";
        $query .= "     T1.EXAMNO, ";
        $query .= "     T3.EXAMCOURSE_MARK ";
        $query .= " FROM ";
        $query .= "     V_ENTEXAM_APPLICANTBASE_DAT T1 ";
        $query .= "     LEFT JOIN ENTEXAM_NO_DAT T2  ON T2.ENTEXAMYEAR  = T2.ENTEXAMYEAR ";
        $query .= "                                 AND T2.APPLICANTDIV = T2.APPLICANTDIV ";
        $query .= "     LEFT JOIN ENTEXAM_COURSE_MST T3 ";
        $query .= "                          ON T2.ENTEXAMYEAR  = T3.ENTEXAMYEAR ";
        $query .= "                         AND T2.APPLICANTDIV = T3.APPLICANTDIV ";
        $query .= "                         AND T2.TESTDIV      = T3.TESTDIV ";
        $query .= "                         AND T2.COURSECD     = T3.COURSECD ";
        $query .= "                         AND T2.MAJORCD      = T3.MAJORCD ";
        $query .= "                         AND T2.EXAMCOURSECD = T3.EXAMCOURSECD ";
        $query .= " WHERE ";
        $query .= "         T1.ENTEXAMYEAR  = '".$model->ObjYear."' ";
        $query .= "     AND T1.APPLICANTDIV = '".$model->field["APPLICANTDIV"]."' ";
        if ($model->field["TESTDIV"] != "99") {
            $query .= "     AND T1.TESTDIV0    = '".$model->field["TESTDIV"]."' ";
        }
        $query .= "     AND T1.EXAMNO BETWEEN T2.EXAMNO_FROM AND T2.EXAMNO_TO ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     BASE.EXAMNO, ";
        $query .= "     L004.ABBV1 AS TESTDIV, ";
        $query .= "     MARK.EXAMCOURSE_MARK, ";
        $query .= "     BASE.NAME, ";
        $query .= "     BASE.NAME_KANA, ";
        $query .= "     Z002.NAME1 AS SEX, ";
        $query .= "     CASE ";
        $query .= "         WHEN BASE.JUDGEMENT = '4' THEN '0' ";
        $query .= "         ELSE '1' ";
        $query .= "     END AS SYUKKETU_KUBUN, ";
        $query .= "     L013.NAME1 AS JUDGEMENT, ";
        $query .= "     L012.NAME1 AS ENTDIV, ";
        $query .= "     L013.NAME1 AS JUDGEMENT2, ";
        $query .= "     L012.NAME1 AS ENTDIV2, ";
        $query .= "     BASE.BIRTHDAY, ";
        $query .= "     BASE.FS_CD, ";
        $query .= "     FSCH.FINSCHOOL_NAME AS FS_NAME, ";
        $query .= "     BASE.FS_DAY, ";
        $query .= "     ADDR.ZIPCD, ";
        $query .= "     ADDR.ADDRESS1, ";
        $query .= "     ADDR.ADDRESS2, ";
        $query .= "     ADDR.TELNO, ";
        $query .= "     ADDR.GNAME, ";
        $query .= "     ADDR.GZIPCD, ";
        $query .= "     ADDR.GADDRESS1, ";
        $query .= "     ADDR.GADDRESS2, ";
        $query .= "     ADDR.GTELNO, ";
        $query .= "     H201.NAME1 AS RELATIONSHIP, ";
        $query .= "     CASE ";
        $query .= "         WHEN BASE.SCHOLAR_KIBOU = '1' THEN '特別' ";
        $query .= "         WHEN BASE.SCHOLAR_KIBOU = '2' THEN '一般' ";
        $query .= "         ELSE '無' ";
        $query .= "     END SCHOLAR_KIBOU, ";
        $query .= "     CASE ";
        $query .= "         WHEN BASE.SCHOLAR_KIBOU IS NOT NULL AND BASE.SCHOLAR_SAIYOU = '1' THEN '採用' ";
        $query .= "         WHEN BASE.SCHOLAR_KIBOU IS NOT NULL AND BASE.SCHOLAR_SAIYOU IS NULL THEN '不採用' ";
        $query .= "     END SCHOLAR_SAIYOU, ";
        $query .= "     BASE.SCHOLAR_TOUKYU_SENGAN, ";
        $query .= "     CASE ";
        $query .= "         WHEN BASE.DORMITORY_FLG = '1' THEN '有' ";
        $query .= "         ELSE '無' ";
        $query .= "     END AS DORMITORY_FLG, ";
        $query .= "     SH_SCH1.FINSCHOOL_NAME AS SH_SCHOOLCD1, ";
        $query .= "     SH_SCH2.FINSCHOOL_NAME AS SH_SCHOOLCD2, ";
        $query .= "     SH_SCH3.FINSCHOOL_NAME AS SH_SCHOOLCD3, ";
        $query .= "     SH_SCH4.FINSCHOOL_NAME AS SH_SCHOOLCD4, ";
        $query .= "     CASE WHEN PUB.KAKUYAKU_FLG = '1' THEN 'y' END AS KAKUYAKU_FLG, ";
        $query .= "     '', ";
        $query .= "     '', ";
        $query .= "     DCONF001.REMARK11 AS CONF1_AVG5, ";
        $query .= "     DCONF001.REMARK12 AS CONF1_AVG9, ";
        $query .= "     DCONF002.REMARK11 AS CONF2_AVG5, ";
        $query .= "     DCONF002.REMARK12 AS CONF2_AVG9, ";
        $query .= "     CONF.AVERAGE5     AS CONF3_AVG5, ";
        $query .= "     CONF.AVERAGE_ALL  AS CONF3_AVG9, ";
        $query .= "     PUB.SCORE1 AS PUBLIC_SCORE1, ";
        $query .= "     PUB.SCORE2 AS PUBLIC_SCORE2, ";
        $query .= "     CONF.ABSENCE_DAYS, ";
        $query .= "     CONF.ABSENCE_DAYS2, ";
        $query .= "     CONF.ABSENCE_DAYS3, ";
        $query .= "     CONF.ABSENCE_REMARK, ";
        $query .= "     CONF.ABSENCE_REMARK2, ";
        $query .= "     CONF.ABSENCE_REMARK3, ";
        $query .= "     CONF.REMARK1 AS TOKKIJIKOU, ";
        $query .= "     DCONF003.REMARK9 AS KATSUDOU_TOKKI, ";
        foreach ($model->ippanTestSubCd as $key => $testName) {
            $query .= "     I_SCORE{$key}.SCORE AS I_{$key}, ";
        }
        $query .= "     '' AS I_TOTAL, ";
        //一般面接
        $query .= "     CASE WHEN BASE.TESTDIV0 != '3' THEN '' ELSE L027.NAME1 END AS I_INTERVIEW_VALUE, ";
        $query .= "     S1.SAT_NO, ";
        $query .= "     S3.SCORE_TOTAL AS SAT_TOTAL, ";
        $query .= "     S4.NAME1 AS SAT_JUDGE, ";
        foreach ($model->suisenTestSubCd as $key => $testName) {
            $query .= "     S_SCORE{$key}.SCORE AS S_{$key}, ";
        }
        $query .= "     '' AS S_TOTAL, ";
        //推薦面接
        $query .= "     CASE WHEN BASE.TESTDIV0 != '2' THEN '' ELSE L027.NAME1 END AS S_INTERVIEW_VALUE, ";
        $query .= "     L047.NAME1 AS S_COMPOSITION_VALUE, ";
        $query .= "     '".$model->lastColumn."' AS ".$model->lastColumn."  ";
        $query .= " FROM ";
        $query .= "     V_ENTEXAM_APPLICANTBASE_DAT BASE ";
        $query .= "     LEFT JOIN MARK_DATA MARK ";
        $query .= "                          ON BASE.EXAMNO  = MARK.EXAMNO ";
        $query .= "     LEFT JOIN ENTEXAM_APPLICANTADDR_DAT ADDR ";
        $query .= "                          ON BASE.ENTEXAMYEAR  = ADDR.ENTEXAMYEAR ";
        $query .= "                         AND BASE.APPLICANTDIV = ADDR.APPLICANTDIV ";
        $query .= "                         AND BASE.EXAMNO       = ADDR.EXAMNO ";
        $query .= "     LEFT JOIN FINSCHOOL_MST FSCH ";
        $query .= "                          ON BASE.FS_CD        = FSCH.FINSCHOOLCD ";
        $query .= "     LEFT JOIN FINSCHOOL_MST SH_SCH1 ";
        $query .= "                          ON BASE.SH_SCHOOLCD1 = SH_SCH1.FINSCHOOLCD ";
        $query .= "     LEFT JOIN FINSCHOOL_MST SH_SCH2 ";
        $query .= "                          ON BASE.SH_SCHOOLCD2 = SH_SCH2.FINSCHOOLCD ";
        $query .= "     LEFT JOIN FINSCHOOL_MST SH_SCH3 ";
        $query .= "                          ON BASE.SH_SCHOOLCD3 = SH_SCH3.FINSCHOOLCD ";
        $query .= "     LEFT JOIN FINSCHOOL_MST SH_SCH4 ";
        $query .= "                          ON BASE.SH_SCHOOLCD4 = SH_SCH4.FINSCHOOLCD ";
        $query .= "     LEFT JOIN ENTEXAM_PUBLIC_TEST_DAT PUB ";
        $query .= "                          ON BASE.ENTEXAMYEAR  = PUB.ENTEXAMYEAR ";
        $query .= "                         AND BASE.APPLICANTDIV = PUB.APPLICANTDIV ";
        $query .= "                         AND BASE.EXAMNO       = PUB.EXAMNO ";
        $query .= "     LEFT JOIN ENTEXAM_APPLICANTCONFRPT_DETAIL_DAT DCONF001 ";
        $query .= "                          ON BASE.ENTEXAMYEAR  = DCONF001.ENTEXAMYEAR ";
        $query .= "                         AND BASE.APPLICANTDIV = DCONF001.APPLICANTDIV ";
        $query .= "                         AND BASE.EXAMNO       = DCONF001.EXAMNO ";
        $query .= "                         AND DCONF001.SEQ      = '001' ";
        $query .= "     LEFT JOIN ENTEXAM_APPLICANTCONFRPT_DETAIL_DAT DCONF002 ";
        $query .= "                          ON BASE.ENTEXAMYEAR  = DCONF002.ENTEXAMYEAR ";
        $query .= "                         AND BASE.APPLICANTDIV = DCONF002.APPLICANTDIV ";
        $query .= "                         AND BASE.EXAMNO       = DCONF002.EXAMNO ";
        $query .= "                         AND DCONF002.SEQ      = '002' ";
        $query .= "     LEFT JOIN ENTEXAM_APPLICANTCONFRPT_DETAIL_DAT DCONF003 ";
        $query .= "                          ON BASE.ENTEXAMYEAR  = DCONF003.ENTEXAMYEAR ";
        $query .= "                         AND BASE.APPLICANTDIV = DCONF003.APPLICANTDIV ";
        $query .= "                         AND BASE.EXAMNO       = DCONF003.EXAMNO ";
        $query .= "                         AND DCONF003.SEQ      = '003' ";
        $query .= "     LEFT JOIN ENTEXAM_APPLICANTCONFRPT_DAT CONF ";
        $query .= "                          ON BASE.ENTEXAMYEAR  = CONF.ENTEXAMYEAR ";
        $query .= "                         AND BASE.APPLICANTDIV = CONF.APPLICANTDIV ";
        $query .= "                         AND BASE.EXAMNO       = CONF.EXAMNO ";
        $query .= "     LEFT JOIN SAT_APP_FORM_MST S1 ON S1.YEAR = '".CTRL_YEAR."' AND S1.SAT_NO = BASE.JIZEN_BANGOU ";
        $query .= "     LEFT JOIN SAT_EXAM_DAT S3 ON S3.YEAR = '".CTRL_YEAR."' AND S3.SAT_NO = BASE.JIZEN_BANGOU ";
        $query .= "     LEFT JOIN V_NAME_MST S4 ON S4.YEAR = S3.YEAR AND S4.NAMECD1 = 'L200' AND S4.NAMECD2 = S3.JUDGE_SAT ";
        $query .= "     LEFT JOIN ENTEXAM_RECEPT_DAT REC_I ";
        $query .= "                          ON BASE.ENTEXAMYEAR             = REC_I.ENTEXAMYEAR ";
        $query .= "                         AND BASE.APPLICANTDIV            = REC_I.APPLICANTDIV ";
        $query .= "                         AND REC_I.TESTDIV                = '5' ";//一般
        $query .= "                         AND REC_I.EXAM_TYPE              = '1' ";
        $query .= "                         AND BASE.EXAMNO                  = REC_I.EXAMNO ";
        foreach ($model->ippanTestSubCd as $key => $testName) {
            $query .= "     LEFT JOIN ENTEXAM_SCORE_DAT I_SCORE{$key} ";
            $query .= "                          ON REC_I.ENTEXAMYEAR            = I_SCORE{$key}.ENTEXAMYEAR ";
            $query .= "                         AND REC_I.APPLICANTDIV           = I_SCORE{$key}.APPLICANTDIV ";
            $query .= "                         AND REC_I.TESTDIV                = I_SCORE{$key}.TESTDIV ";
            $query .= "                         AND REC_I.EXAM_TYPE              = I_SCORE{$key}.EXAM_TYPE ";
            $query .= "                         AND REC_I.RECEPTNO               = I_SCORE{$key}.RECEPTNO ";
            $query .= "                         AND I_SCORE{$key}.TESTSUBCLASSCD = '{$key}' ";
        }
        $query .= "     LEFT JOIN ENTEXAM_RECEPT_DAT REC_S ";
        $query .= "                          ON BASE.ENTEXAMYEAR             = REC_S.ENTEXAMYEAR ";
        $query .= "                         AND BASE.APPLICANTDIV            = REC_S.APPLICANTDIV ";
        $query .= "                         AND REC_S.TESTDIV               IN ('3', '4') "; //3:校長推薦、4:自己推薦
        $query .= "                         AND REC_S.EXAM_TYPE              = '1' ";
        $query .= "                         AND BASE.EXAMNO                  = REC_S.EXAMNO ";
        foreach ($model->suisenTestSubCd as $key => $testName) {
            $query .= "     LEFT JOIN ENTEXAM_SCORE_DAT S_SCORE{$key} ";
            $query .= "                          ON REC_S.ENTEXAMYEAR            = S_SCORE{$key}.ENTEXAMYEAR ";
            $query .= "                         AND REC_S.APPLICANTDIV           = S_SCORE{$key}.APPLICANTDIV ";
            $query .= "                         AND REC_S.TESTDIV                = S_SCORE{$key}.TESTDIV ";
            $query .= "                         AND REC_S.EXAM_TYPE              = S_SCORE{$key}.EXAM_TYPE ";
            $query .= "                         AND REC_S.RECEPTNO               = S_SCORE{$key}.RECEPTNO ";
            $query .= "                         AND S_SCORE{$key}.TESTSUBCLASSCD = '{$key}' ";
        }
        $query .= "     LEFT JOIN ENTEXAM_SCORE_DAT SYORON ";
        $query .= "                          ON BASE.ENTEXAMYEAR      = SYORON.ENTEXAMYEAR ";
        $query .= "                         AND BASE.APPLICANTDIV     = SYORON.APPLICANTDIV ";
        $query .= "                         AND BASE.TESTDIV          = '3' ";
        $query .= "                         AND SYORON.EXAM_TYPE      = '1' ";
        $query .= "                         AND BASE.EXAMNO           = SYORON.RECEPTNO ";
        $query .= "                         AND SYORON.TESTSUBCLASSCD = '6' ";
        $query .= "     LEFT JOIN V_NAME_MST L004  ";
        $query .= "                          ON BASE.ENTEXAMYEAR  = L004.YEAR  ";
        $query .= "                         AND L004.NAMECD1      = 'L004'  ";
        $query .= "                         AND BASE.TESTDIV      = L004.NAMECD2 ";
        $query .= "     LEFT JOIN V_NAME_MST L012  ";
        $query .= "                          ON BASE.ENTEXAMYEAR  = L012.YEAR  ";
        $query .= "                         AND L012.NAMECD1      = 'L012'  ";
        $query .= "                         AND BASE.ENTDIV       = L012.NAMECD2 ";
        $query .= "     LEFT JOIN V_NAME_MST L013  ";
        $query .= "                          ON BASE.ENTEXAMYEAR  = L013.YEAR  ";
        $query .= "                         AND L013.NAMECD1      = 'L013'  ";
        $query .= "                         AND BASE.JUDGEMENT    = L013.NAMECD2 ";
        $query .= "     LEFT JOIN V_NAME_MST Z002  ";
        $query .= "                          ON BASE.ENTEXAMYEAR  = Z002.YEAR  ";
        $query .= "                         AND Z002.NAMECD1      = 'Z002'  ";
        $query .= "                         AND BASE.SEX          = Z002.NAMECD2 ";
        $query .= "     LEFT JOIN V_NAME_MST H201  ";
        $query .= "                          ON BASE.ENTEXAMYEAR  = H201.YEAR  ";
        $query .= "                         AND H201.NAMECD1      = 'H201'  ";
        $query .= "                         AND ADDR.RELATIONSHIP = H201.NAMECD2 ";
        $query .= "     LEFT JOIN ENTEXAM_INTERVIEW_DAT INTERVIEW ";
        $query .= "                          ON INTERVIEW.ENTEXAMYEAR   = BASE.ENTEXAMYEAR ";
        $query .= "                         AND INTERVIEW.APPLICANTDIV  = BASE.APPLICANTDIV ";
        $query .= "                         AND INTERVIEW.TESTDIV       = BASE.TESTDIV ";
        $query .= "                         AND INTERVIEW.EXAMNO        = BASE.EXAMNO ";
        $query .= "     LEFT JOIN V_NAME_MST L027 ON L027.YEAR = BASE.ENTEXAMYEAR AND L027.NAMECD1 = 'L027' AND L027.NAMECD2 = INTERVIEW.INTERVIEW_VALUE ";
        $query .= "     LEFT JOIN V_NAME_MST L047 ON L047.YEAR = BASE.ENTEXAMYEAR AND L047.NAMECD1 = 'L047' AND L047.NAMECD2 = INTERVIEW.COMPOSITION_VALUE ";
        $query .= " WHERE ";
        $query .= "         BASE.ENTEXAMYEAR  = '".$model->ObjYear."' ";
        $query .= "     AND BASE.APPLICANTDIV = '".$model->field["APPLICANTDIV"]."' ";
        if ($model->field["TESTDIV"] != "99") {
            $query .= "     AND BASE.TESTDIV0    = '".$model->field["TESTDIV"]."' ";
        }
        if ($model->field["ABSENT"] == "1") {
            $query .= "     AND VALUE(BASE.JUDGEMENT,'') <> '4' ";
        }
        $query .= " ORDER BY ";
        $query .= "     BASE.TESTDIV, ";
        $query .= "     BASE.EXAMNO ";

        return $query;
    }
}
