<?php

require_once('for_php7.php');

class knjl321bQuery extends Query
{

    //名称マスタより取得
    public function getNameMst($year, $namecd1)
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     NAMECD2 || ':' || NAME1 AS LABEL, ";
        $query .= "     NAMECD2 AS VALUE,";
        $query .= "     NAMESPARE2 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$year."' AND ";
        $query .= "     NAMECD1 = '".$namecd1."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //名称マスタより取得
    public function getNameMstTestSubclass($model)
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     NAMECD2 AS TESTSUBCLASSCD,";
        $query .= "     NAME1 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$model->ObjYear."' AND ";
        $query .= "     NAMECD1 = 'L009' ";
        if ($model->field["TESTDIV"] == '1') {
            $query .= "     AND NAMESPARE2 = '1' ";
        } elseif ($model->field["TESTDIV"] == '2') {
            $query .= "     AND NAMESPARE3 = '1' ";
        }
        $query .= " ORDER BY ";
        $query .= "     TESTSUBCLASSCD ";

        return $query;
    }

    public function getCsvQuery($model)
    {
        $query  = " SELECT ";
        $query .= "     CASE WHEN RDET1.REMARK4 IN ('1', '2') THEN ";
        $query .= "       CASE WHEN RDET2.REMARK4 = '1' THEN CRSJG2.JUDGMENT_COURSE_ABBV ";
        $query .= "            WHEN RDET1.REMARK4 = '1' THEN CRSJG1.JUDGMENT_COURSE_ABBV ";
        $query .= "       ELSE NML013.NAME1 END ";
        $query .= "     ELSE ";
        $query .= "         NML013_2.NAME1 ";
        $query .= "     END AS JUDGEDIV_NAME, ";
        $query .= "     RECEPT.TOTAL_RANK4, ";
        //希望コース
        $query .= "     NML033.ABBV2 AS SELECT_SUBCLASS_DIV_ABBV2, ";
        $query .= "     BASE.EXAMNO, ";
        $query .= "     BASE.NAME, ";
        $query .= "     NMZ002.NAME2 AS SEX_NAME, ";
        $query .= "     FIN.FINSCHOOL_NAME_ABBV, ";
        $query .= "     RECEPT.TOTAL3, ";
        $query .= "     RECEPT.TOTAL1, ";
        $query .= "     RECEPT.AVARAGE3, ";
        $query .= "     RECEPT.AVARAGE1, ";
        $query .= "     RECEPT.AVARAGE4, ";
        $query .= "     INTV.INTERVIEW_A, ";
        $query .= "     INTV.INTERVIEW_B, ";
        $query .= "     INTV.INTERVIEW_C, ";
        $query .= "     BASE.REMARK1, ";
        $query .= "     CASE WHEN CONFRPT.EXAMNO IS NOT NULL THEN 1 ELSE 0 END AS HAS_CONFRPT_DAT, ";
        $query .= "     CONFRPT.AVERAGE_ALL, ";
        $query .= "     CONFRPT.CONFIDENTIAL_RPT01, ";
        $query .= "     CONFRPT.CONFIDENTIAL_RPT02, ";
        $query .= "     CONFRPT.CONFIDENTIAL_RPT03, ";
        $query .= "     CONFRPT.CONFIDENTIAL_RPT04, ";
        $query .= "     CONFRPT.CONFIDENTIAL_RPT05, ";
        $query .= "     CONFRPT.CONFIDENTIAL_RPT06, ";
        $query .= "     CONFRPT.CONFIDENTIAL_RPT07, ";
        $query .= "     CONFRPT.CONFIDENTIAL_RPT08, ";
        $query .= "     CONFRPT.CONFIDENTIAL_RPT09, ";
        $query .= "     CONFRPT.CONFIDENTIAL_RPT10, ";
        $query .= "     CONFRPT.CONFIDENTIAL_RPT11, ";
        $query .= "     CONFRPT.CONFIDENTIAL_RPT12, ";
        $query .= "     CONFRPT.BASE_FLG AS F1, ";
        $query .= "     CONFRPT.HEALTH_FLG AS F2, ";
        $query .= "     CONFRPT.ACTIVE_FLG AS F3, ";
        $query .= "     CONFRPT.RESPONSIBLE_FLG AS F4, ";
        $query .= "     CONFRPT.ORIGINAL_FLG AS F5, ";
        $query .= "     CONFRPT.MIND_FLG AS F6, ";
        $query .= "     CONFRPT.NATURE_FLG AS F7, ";
        $query .= "     CONFRPT.WORK_FLG AS F8, ";
        $query .= "     CONFRPT.JUSTICE_FLG AS F9, ";
        $query .= "     CONFRPT.PUBLIC_FLG AS F10, ";
        $query .= "     CASE WHEN VALUE(CONFRPT.BASE_FLG, '') <> '1' THEN '1' END AS BASE_FLG_SHOW, ";
        $query .= "     CONFRPT.ABSENCE_DAYS, ";
        $query .= "     CONFRPT.ABSENCE_DAYS2, ";
        $query .= "     CONFRPT.ABSENCE_DAYS3, ";
        $query .= "     CONFRPT.ABSENCE_REMARK, ";
        $query .= "     CONFRPT.ABSENCE_REMARK2, ";
        $query .= "     CONFRPT.ABSENCE_REMARK3, ";
        $query .= "     BDETAIL4.REMARK3 AS BASE_DETAIL4_REMARK3, ";
        $query .= "     BDETAIL4.REMARK4 AS BASE_DETAIL4_REMARK4, ";
        $query .= "     CRS.PROMISE_COURSE_ABBV, ";
        $query .= "     BDETAIL4.REMARK1 AS BASE_DETAIL4_REMARK1, ";
        $query .= "     BDETAIL4.REMARK2 AS BASE_DETAIL4_REMARK2, ";
        $query .= "     CONFRPT.REMARK1 AS CONFRPT_REMARK1, ";
        $query .= "     NML009.NAMECD2 AS TESTSUBCLASSCD, ";
        $query .= "     TSCORE.SCORE ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_APPLICANTBASE_DAT BASE ";
        $query .= "       LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT BDETAIL4 ON BDETAIL4.ENTEXAMYEAR = BASE.ENTEXAMYEAR ";
        $query .= "           AND BDETAIL4.EXAMNO = BASE.EXAMNO ";
        $query .= "           AND BDETAIL4.SEQ = '004'  ";
        $query .= "      LEFT JOIN ENTEXAM_RECEPT_DAT RECEPT ON RECEPT.ENTEXAMYEAR = BASE.ENTEXAMYEAR ";
        $query .= "           AND RECEPT.APPLICANTDIV = BASE.APPLICANTDIV ";
        $query .= "           AND RECEPT.TESTDIV = BASE.TESTDIV ";
        $query .= "           AND RECEPT.EXAM_TYPE = '1' ";
        $query .= "           AND RECEPT.EXAMNO = BASE.EXAMNO  ";
        $query .= "      LEFT JOIN ENTEXAM_RECEPT_DETAIL_DAT RDET1 ON RDET1.ENTEXAMYEAR = BASE.ENTEXAMYEAR ";
        $query .= "           AND RDET1.APPLICANTDIV = BASE.APPLICANTDIV ";
        $query .= "           AND RDET1.TESTDIV = BASE.TESTDIV ";
        $query .= "           AND RDET1.EXAM_TYPE = '1' ";
        $query .= "           AND RDET1.RECEPTNO = RECEPT.RECEPTNO ";
        $query .= "           AND RDET1.SEQ = '001' ";
        $query .= "      LEFT JOIN ENTEXAM_COURSE_JUDGMENT_MST CRSJG1 ON CRSJG1.ENTEXAMYEAR = BASE.ENTEXAMYEAR ";
        $query .= "           AND (BASE.TESTDIV = '1' AND CRSJG1.TAKE_RECOMMEND_TEST_FLG = '1'";
        $query .= "             OR BASE.TESTDIV = '2' AND CRSJG1.TAKE_GENERAL_TEST_FLG = '1') ";
        $query .= "           AND CRSJG1.NORMAL_PASSCOURSECD = RDET1.REMARK1 ";
        $query .= "           AND CRSJG1.NORMAL_PASSMAJORCD = RDET1.REMARK2 ";
        $query .= "           AND CRSJG1.NORMAL_PASSEXAMCOURSECD = RDET1.REMARK3 ";
        $query .= "           AND CRSJG1.EARLY_PASSCOURSECD IS NULL ";
        $query .= "           AND CRSJG1.EARLY_PASSMAJORCD IS NULL ";
        $query .= "           AND CRSJG1.EARLY_PASSEXAMCOURSECD IS NULL ";
        $query .= "      LEFT JOIN ENTEXAM_RECEPT_DETAIL_DAT RDET2 ON RDET2.ENTEXAMYEAR = BASE.ENTEXAMYEAR ";
        $query .= "           AND RDET2.APPLICANTDIV = BASE.APPLICANTDIV ";
        $query .= "           AND RDET2.TESTDIV = BASE.TESTDIV ";
        $query .= "           AND RDET2.EXAM_TYPE = '1' ";
        $query .= "           AND RDET2.RECEPTNO = RECEPT.RECEPTNO ";
        $query .= "           AND RDET2.SEQ = '002' ";
        $query .= "      LEFT JOIN ENTEXAM_COURSE_JUDGMENT_MST CRSJG2 ON CRSJG2.ENTEXAMYEAR = BASE.ENTEXAMYEAR ";
        $query .= "           AND BASE.TESTDIV = '2' "; // 一般入試のみ
        $query .= "           AND CRSJG2.CHANGE_SINGLE_TEST_FLG = '1' ";
        $query .= "           AND CRSJG2.EARLY_PASSCOURSECD = RDET2.REMARK1 ";
        $query .= "           AND CRSJG2.EARLY_PASSMAJORCD = RDET2.REMARK2 ";
        $query .= "           AND CRSJG2.EARLY_PASSEXAMCOURSECD = RDET2.REMARK3 ";
        $query .= "      LEFT JOIN NAME_MST NML009 ON NML009.NAMECD1 = 'L009' ";
        $query .= "           AND ( ";
        $query .= "                (BASE.TESTDIV = '1' AND NML009.NAMESPARE2 = '1') OR ";
        $query .= "                (BASE.TESTDIV = '2' AND NML009.NAMESPARE3 = '1') ";
        $query .= "           )  ";
        $query .= "      LEFT JOIN ENTEXAM_SCORE_DAT TSCORE ON TSCORE.ENTEXAMYEAR = BASE.ENTEXAMYEAR ";
        $query .= "           AND TSCORE.APPLICANTDIV = BASE.APPLICANTDIV ";
        $query .= "           AND TSCORE.TESTDIV = BASE.TESTDIV ";
        $query .= "           AND TSCORE.EXAM_TYPE = RECEPT.EXAM_TYPE ";
        $query .= "           AND TSCORE.RECEPTNO = RECEPT.RECEPTNO ";
        $query .= "           AND TSCORE.TESTSUBCLASSCD = NML009.NAMECD2  ";
        $query .= "      LEFT JOIN FINSCHOOL_MST FIN ON FIN.FINSCHOOLCD = BASE.FS_CD ";
        $query .= "      LEFT JOIN ENTEXAM_INTERVIEW_DAT INTV ON INTV.ENTEXAMYEAR = BASE.ENTEXAMYEAR ";
        $query .= "           AND INTV.APPLICANTDIV = BASE.APPLICANTDIV ";
        $query .= "           AND INTV.TESTDIV = BASE.TESTDIV ";
        $query .= "           AND INTV.EXAMNO = BASE.EXAMNO  ";
        $query .= "      LEFT JOIN ENTEXAM_APPLICANTCONFRPT_DAT CONFRPT ON CONFRPT.ENTEXAMYEAR = BASE.ENTEXAMYEAR ";
        $query .= "           AND CONFRPT.EXAMNO = BASE.EXAMNO  ";
        $query .= "      LEFT JOIN ENTEXAM_COURSE_JUDGMENT_MST CRS ON CRS.ENTEXAMYEAR = BASE.ENTEXAMYEAR ";
        $query .= "           AND CRS.JUDGMENT_DIV = BDETAIL4.REMARK8  ";
        $query .= "      LEFT JOIN NAME_MST NML013 ON NML013.NAMECD1 = 'L013' ";
        $query .= "           AND NML013.NAMECD2 = RDET1.REMARK4  ";
        $query .= "      LEFT JOIN NAME_MST NMZ002 ON NMZ002.NAMECD1 = 'Z002' ";
        $query .= "           AND NMZ002.NAMECD2 = BASE.SEX ";
        $query .= "      LEFT JOIN NAME_MST NML013_2 ON NML013_2.NAMECD1 = 'L013' ";
        $query .= "           AND NML013_2.NAMECD2 = RECEPT.JUDGEDIV ";
        $query .= "     LEFT JOIN NAME_MST NML033 ON NML033.NAMECD1 = 'L033' ";
        $query .= "          AND BASE.SELECT_SUBCLASS_DIV = NML033.NAMECD2 ";
        $query .= " WHERE ";
        $query .= "     BASE.ENTEXAMYEAR = '".$model->ObjYear."'      AND ";
        $query .= "     BASE.APPLICANTDIV = '".$model->field["APPLICANTDIV"]."'      AND ";
        $query .= "     BASE.TESTDIV = '".$model->field["TESTDIV"]."' ";
        if ($model->field["SPECIAL_REASON_DIV"]) {
            $query .= "     AND BASE.SPECIAL_REASON_DIV IS NOT NULL ";
        }
        $query .= " ORDER BY ";
        if ($model->field["SORT"] == '2') {
            $query .= "     RECEPT.TOTAL_RANK4, ";
        }
        $query .= "     BASE.EXAMNO ";
        return $query;
    }

    //名称マスタより取得
    public function getNameMstL008($year)
    {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR       = '".$year."' ";
        $query .= " AND NAMECD1    = 'L008' ";
        $query .= " AND NAMESPARE1 = '1' "; //5教科のみ取得
        $query .= " ORDER BY ";
        $query .= "     NAMECD2 ";
        return $query;
    }
}
