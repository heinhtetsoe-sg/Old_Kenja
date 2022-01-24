<?php

require_once('for_php7.php');

class knjh400_nyuusiQuery extends Query
{
    //生徒名取得
    public function getName($model)
    {
        $query .= " SELECT ";
        $query .= "     NAME_SHOW ";
        $query .= " FROM ";
        $query .= "     SCHREG_BASE_MST ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO = '{$model->schregno}' ";

        return $query;
    }
    //名称マスタ
    public function getNameMst($model, $namecd1, $namecd2 = "")
    {
        $query  = " SELECT ";
        $query .= "     NAMECD2 AS VALUE, ";
        $query .= "     NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR    = '".$model->year."' ";
        $query .= " AND NAMECD1 = '".$namecd1."' ";
        if ($namecd2) {
            $query .= " AND NAMECD2 = '".$namecd2."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";
        return $query;
    }

    public function selectQuery($model)
    {
        $query  = " SELECT ";
        $query .= "     SB_DTL003.BASE_REMARK1, ";
        $query .= "     BASE.NAME, ";
        $query .= "     PRI.PRISCHOOL_NAME, ";
        $query .= "     SCORE1.SCORE AS SCORE1, ";
        $query .= "     SCORE2.SCORE AS SCORE2, ";
        $query .= "     SCORE3.SCORE AS SCORE3, ";
        $query .= "     SCORE4.SCORE AS SCORE4, ";
        $query .= "     SCORE5.SCORE AS SCORE5, ";
        $query .= "     RECEPT.TOTAL4, ";
        $query .= "     RECEPT.TOTAL_RANK1, ";
        $query .= "     RECEPT.TOTAL_RANK3 ";
        $query .= " FROM ";
        $query .= "     SCHREG_BASE_MST BASE ";
        $query .= "     LEFT JOIN SCHREG_BASE_DETAIL_MST SB_DTL003 ";
        $query .= "            ON BASE.SCHREGNO      = SB_DTL003.SCHREGNO ";
        $query .= "           AND SB_DTL003.BASE_SEQ = '003' ";
        $query .= "     LEFT JOIN ENTEXAM_APPLICANTBASE_DAT EA_BASE ";
        $query .= "            ON EA_BASE.ENTEXAMYEAR = FISCALYEAR(BASE.ENT_DATE) ";
        $query .= "           AND EA_BASE.EXAMNO      = SB_DTL003.BASE_REMARK1 ";
        $query .= "     LEFT JOIN PRISCHOOL_MST PRI ";
        $query .= "            ON EA_BASE.PRISCHOOLCD = PRI.PRISCHOOLCD ";
        $query .= "     LEFT JOIN ENTEXAM_RECEPT_DAT RECEPT ";
        $query .= "            ON EA_BASE.ENTEXAMYEAR   = RECEPT.ENTEXAMYEAR ";
        $query .= "           AND EA_BASE.APPLICANTDIV  = RECEPT.APPLICANTDIV ";
        $query .= "           AND EA_BASE.EXAMNO        = RECEPT.RECEPTNO ";
        $query .= "     LEFT JOIN ENTEXAM_SCORE_DAT SCORE1 ";
        $query .= "            ON EA_BASE.ENTEXAMYEAR   = SCORE1.ENTEXAMYEAR ";
        $query .= "           AND EA_BASE.APPLICANTDIV  = SCORE1.APPLICANTDIV ";
        $query .= "           AND EA_BASE.EXAMNO        = SCORE1.RECEPTNO ";
        $query .= "           AND SCORE1.TESTSUBCLASSCD = '1' ";
        $query .= "     LEFT JOIN ENTEXAM_SCORE_DAT SCORE2 ";
        $query .= "            ON EA_BASE.ENTEXAMYEAR   = SCORE2.ENTEXAMYEAR ";
        $query .= "           AND EA_BASE.APPLICANTDIV  = SCORE2.APPLICANTDIV ";
        $query .= "           AND EA_BASE.EXAMNO        = SCORE2.RECEPTNO ";
        $query .= "           AND SCORE2.TESTSUBCLASSCD = '2' ";
        $query .= "     LEFT JOIN ENTEXAM_SCORE_DAT SCORE3 ";
        $query .= "            ON EA_BASE.ENTEXAMYEAR   = SCORE3.ENTEXAMYEAR ";
        $query .= "           AND EA_BASE.APPLICANTDIV  = SCORE3.APPLICANTDIV ";
        $query .= "           AND EA_BASE.EXAMNO        = SCORE3.RECEPTNO ";
        $query .= "           AND SCORE3.TESTSUBCLASSCD = '3' ";
        $query .= "     LEFT JOIN ENTEXAM_SCORE_DAT SCORE4 ";
        $query .= "            ON EA_BASE.ENTEXAMYEAR   = SCORE4.ENTEXAMYEAR ";
        $query .= "           AND EA_BASE.APPLICANTDIV  = SCORE4.APPLICANTDIV ";
        $query .= "           AND EA_BASE.EXAMNO        = SCORE4.RECEPTNO ";
        $query .= "           AND SCORE4.TESTSUBCLASSCD = '4' ";
        $query .= "     LEFT JOIN ENTEXAM_SCORE_DAT SCORE5 ";
        $query .= "            ON EA_BASE.ENTEXAMYEAR   = SCORE5.ENTEXAMYEAR ";
        $query .= "           AND EA_BASE.APPLICANTDIV  = SCORE5.APPLICANTDIV ";
        $query .= "           AND EA_BASE.EXAMNO        = SCORE5.RECEPTNO ";
        $query .= "           AND SCORE5.TESTSUBCLASSCD = '5' ";
        $query .= " WHERE ";
        $query .= "     BASE.SCHREGNO = '{$model->schregno}' ";

        return $query;
    }

    //１レコード取得
    public function selectQuery2($model, $examno)
    {
        $query  = " SELECT ";
        $query .= "     B1.TESTDIV0, ";
        $query .= "     B1.TESTDIV, ";
        $query .= "     B1.EXAMNO, ";
        $query .= "     B1.NAME, ";
        $query .= "     B1.NAME_KANA, ";
        $query .= "     B1.BIRTHDAY, ";
        $query .= "     B1.SEX, ";
        $query .= "     BD031.REMARK5 AS SEQ031_REMARK5, ";
        $query .= "     B1.FS_CD AS FINSCHOOLCD, ";
        $query .= "     B1.FS_GRDYEAR, ";
        $query .= "     B1.FS_ERACD, ";
        $query .= "     B1.FS_Y, ";
        $query .= "     B1.FS_M, ";
        $query .= "     B1.FS_GRDDIV, ";
        $query .= "     B1.FS_DAY, ";
        $query .= "     ADDR1.GNAME, ";
        $query .= "     ADDR1.GKANA, ";
        $query .= "     ADDR1.GZIPCD, ";
        $query .= "     ADDR1.GADDRESS1, ";
        $query .= "     ADDR1.GADDRESS2, ";
        $query .= "     ADDR1.GTELNO, ";
        $query .= "     BD031.REMARK1 AS HOPE_COURSE1, ";
        $query .= "     BD031.REMARK2 AS HOPE_COURSE2, ";
        $query .= "     BD031.REMARK3 AS HOPE_COURSE3, ";
        $query .= "     BD031.REMARK4 AS HOPE_COURSE4, ";
        $query .= "     B1.DORMITORY_FLG, ";
        $query .= "     BD031.REMARK6 AS SEQ031_REMARK6, ";
        $query .= "     BD012.REMARK1 AS ALT_EXAMNO1, ";
        $query .= "     BD012.REMARK2 AS ALT_EXAMNO2, ";
        $query .= "     BD012.REMARK3 AS ALT_EXAMNO3, ";
        $query .= "     RECEPT_D015.REMARK4 AS HONORDIV, ";
        $query .= "     RECEPT_D015.REMARK5 AS HONOR_REASONDIV ";
        $query .= " FROM  ";
        $query .= "     ENTEXAM_APPLICANTBASE_DAT B1 ";
        $query .= "     LEFT JOIN ENTEXAM_APPLICANTADDR_DAT ADDR1 ";
        $query .= "            ON ADDR1.ENTEXAMYEAR = B1.ENTEXAMYEAR ";
        $query .= "           AND ADDR1.EXAMNO      = B1.EXAMNO ";
        $query .= "     LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT BD031 ";
        $query .= "            ON BD031.ENTEXAMYEAR  = B1.ENTEXAMYEAR ";
        $query .= "           AND BD031.APPLICANTDIV = B1.APPLICANTDIV ";
        $query .= "           AND BD031.EXAMNO       = B1.EXAMNO ";
        $query .= "           AND BD031.SEQ          = '031' ";
        $query .= "     LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT BD012 ";
        $query .= "            ON BD012.ENTEXAMYEAR  = B1.ENTEXAMYEAR ";
        $query .= "           AND BD012.APPLICANTDIV = B1.APPLICANTDIV ";
        $query .= "           AND BD012.EXAMNO       = B1.EXAMNO ";
        $query .= "           AND BD012.SEQ          = '012' ";
        $query .= "     LEFT JOIN ENTEXAM_RECEPT_DETAIL_DAT AS RECEPT_D015 ON ";
        $query .= "               RECEPT_D015.ENTEXAMYEAR  = B1.ENTEXAMYEAR ";
        $query .= "           AND RECEPT_D015.APPLICANTDIV = B1.APPLICANTDIV ";
        $query .= "           AND RECEPT_D015.TESTDIV      = B1.TESTDIV ";
        $query .= "           AND RECEPT_D015.RECEPTNO     = B1.EXAMNO ";
        $query .= "           AND RECEPT_D015.SEQ          = '015' ";
        $query .= " WHERE  ";
        $query .= "     B1.ENTEXAMYEAR  = '{$model->year}' ";
        $query .= " AND B1.APPLICANTDIV = '{$model->applicantdiv}' ";
        if ($model->cmd == "back1") {
            $query .= " AND B1.EXAMNO < '".$examno."' ";
            $query.= " ORDER BY ";
            $query.= "     B1.EXAMNO DESC ";
        } elseif ($model->cmd == "next1") {
            $query .= " AND B1.EXAMNO > '".$examno."' ";
            $query.= " ORDER BY ";
            $query.= "     B1.EXAMNO ASC ";
        } else {
            $query .= " AND B1.EXAMNO = '".$examno."' ";
        }

        return $query;
    }
    //ENTEXAM_GENERAL_MSTよりデータを取得
    public function getGeneralMst($model, $generalDiv, $generalCd = "")
    {
        $query  = " SELECT ";
        $query .= "   *, ";
        $query .= "   VALUE(GENERAL_NAME, '') AS LABEL,  ";
        $query .= "   GENERAL_CD AS VALUE ";
        $query .= "   ";
        $query .= " FROM ";
        $query .= "   ENTEXAM_GENERAL_MST T1 ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR  = '".$model->year."' ";
        $query .= " AND APPLICANTDIV = '".$model->applicantdiv."' ";
        $query .= " AND TESTDIV      = '0' ";
        $query .= " AND GENERAL_DIV  = '".$generalDiv."' ";
        if ($generalCd) {
            $query .= " AND GENERAL_CD = '".$generalCd."' ";
        }
        $query .= " ORDER BY ";
        $query .= "   GENERAL_CD ";

        return $query;
    }

    //特待コード一覧取得
    public function getHonordivQuery($model)
    {
        $query  = " SELECT ";
        $query .= "     GENERAL_NAME AS LABEL, ";
        $query .= "     GENERAL_CD AS VALUE ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_GENERAL_MST ";
        $query .= " WHERE ";
        $query .= "         ENTEXAMYEAR  = '{$model->year}' ";
        $query .= "     AND APPLICANTDIV = '{$model->applicantdiv}' ";
        $query .= "     AND TESTDIV      = '0' ";  // '0' 固定
        $query .= "     AND GENERAL_DIV  = '04' "; // '04'：特待コード 固定
        $query .= " ORDER BY ";
        $query .= "     VALUE(GENERAL_CD, '') ";

        return $query;
    }

    //特待理由コード一覧取得
    public function getHonorReasondivQuery($model)
    {
        $query  = " SELECT ";
        $query .= "     GENERAL_NAME AS LABEL, ";
        $query .= "     GENERAL_CD AS VALUE ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_GENERAL_MST ";
        $query .= " WHERE ";
        $query .= "         ENTEXAMYEAR  = '{$model->year}' ";
        $query .= "     AND APPLICANTDIV = '{$model->applicantdiv}' ";
        $query .= "     AND TESTDIV      = '0' ";  // '0' 固定
        $query .= "     AND GENERAL_DIV  = '05' "; // '05'：特待理由コード 固定
        $query .= " ORDER BY ";
        $query .= "     VALUE(GENERAL_CD, '') ";

        return $query;
    }

    //設定マスタより取得
    public function getSettingMst($model, $settingCd, $seq = "")
    {
        $query  = " SELECT ";
        $query .= "     SEQ, ";
        $query .= "     NAME1 ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_SETTING_MST ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR  = '{$model->year}' ";
        $query .= "     AND APPLICANTDIV = '{$model->applicantdiv}' ";
        $query .= "     AND SETTING_CD   = '{$settingCd}' ";
        if ($seq) {
            $query .= "     AND SEQ          = '{$seq}' ";
        }
        $query .= " ORDER BY ";
        $query .= "     SEQ ";

        return $query;
    }
    public function selectQuery3($model, $examno)
    {
        $query  = "";
        $query .= " SELECT ";
        $query .= "     B1.EXAMNO, ";
        $query .= "     B1.NAME, ";
        $query .= "     F1.FINSCHOOL_NAME, ";
        $query .= "     CONFD001.REMARK1 AS KYOKA1_01, ";
        $query .= "     CONFD001.REMARK2 AS KYOKA1_02, ";
        $query .= "     CONFD001.REMARK3 AS KYOKA1_03, ";
        $query .= "     CONFD001.REMARK4 AS KYOKA1_04, ";
        $query .= "     CONFD001.REMARK5 AS KYOKA1_05, ";
        $query .= "     CONFD001.REMARK6 AS KYOKA1_06, ";
        $query .= "     CONFD001.REMARK7 AS KYOKA1_07, ";
        $query .= "     CONFD001.REMARK8 AS KYOKA1_08, ";
        $query .= "     CONFD001.REMARK9 AS KYOKA1_09, ";
        $query .= "     CONFD001.REMARK10 AS TOTAL_ALL1, ";
        $query .= "     CONFD001.REMARK11 AS AVERAGE_ALL1, ";
        $query .= "     CONFD002.REMARK1 AS KYOKA2_01, ";
        $query .= "     CONFD002.REMARK2 AS KYOKA2_02, ";
        $query .= "     CONFD002.REMARK3 AS KYOKA2_03, ";
        $query .= "     CONFD002.REMARK4 AS KYOKA2_04, ";
        $query .= "     CONFD002.REMARK5 AS KYOKA2_05, ";
        $query .= "     CONFD002.REMARK6 AS KYOKA2_06, ";
        $query .= "     CONFD002.REMARK7 AS KYOKA2_07, ";
        $query .= "     CONFD002.REMARK8 AS KYOKA2_08, ";
        $query .= "     CONFD002.REMARK9 AS KYOKA2_09, ";
        $query .= "     CONFD002.REMARK10 AS TOTAL_ALL2, ";
        $query .= "     CONFD002.REMARK11 AS AVERAGE_ALL2, ";
        $query .= "     CONF1.CONFIDENTIAL_RPT01 AS KYOKA3_01, ";
        $query .= "     CONF1.CONFIDENTIAL_RPT02 AS KYOKA3_02, ";
        $query .= "     CONF1.CONFIDENTIAL_RPT03 AS KYOKA3_03, ";
        $query .= "     CONF1.CONFIDENTIAL_RPT04 AS KYOKA3_04, ";
        $query .= "     CONF1.CONFIDENTIAL_RPT05 AS KYOKA3_05, ";
        $query .= "     CONF1.CONFIDENTIAL_RPT06 AS KYOKA3_06, ";
        $query .= "     CONF1.CONFIDENTIAL_RPT07 AS KYOKA3_07, ";
        $query .= "     CONF1.CONFIDENTIAL_RPT08 AS KYOKA3_08, ";
        $query .= "     CONF1.CONFIDENTIAL_RPT09 AS KYOKA3_09, ";
        $query .= "     CONF1.TOTAL_ALL AS TOTAL_ALL3, ";
        $query .= "     CONF1.AVERAGE_ALL AS AVERAGE_ALL3, ";
        $query .= "     CONF1.ABSENCE_DAYS AS KESSEKI1, ";
        $query .= "     CONF1.ABSENCE_DAYS2 AS KESSEKI2, ";
        $query .= "     CONF1.ABSENCE_DAYS3 AS KESSEKI3 ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_APPLICANTBASE_DAT B1 ";
        $query .= "     LEFT JOIN ENTEXAM_APPLICANTCONFRPT_DAT CONF1 ";
        $query .= "         ON CONF1.ENTEXAMYEAR  = B1.ENTEXAMYEAR ";
        $query .= "        AND CONF1.APPLICANTDIV = B1.APPLICANTDIV ";
        $query .= "        AND CONF1.EXAMNO       = B1.EXAMNO ";
        $query .= "     LEFT JOIN ENTEXAM_APPLICANTCONFRPT_DETAIL_DAT CONFD001 ";
        $query .= "         ON CONFD001.ENTEXAMYEAR  = B1.ENTEXAMYEAR ";
        $query .= "        AND CONFD001.APPLICANTDIV = B1.APPLICANTDIV ";
        $query .= "        AND CONFD001.EXAMNO       = B1.EXAMNO ";
        $query .= "        AND CONFD001.SEQ          = '001' ";
        $query .= "     LEFT JOIN ENTEXAM_APPLICANTCONFRPT_DETAIL_DAT CONFD002 ";
        $query .= "         ON CONFD002.ENTEXAMYEAR  = B1.ENTEXAMYEAR ";
        $query .= "        AND CONFD002.APPLICANTDIV = B1.APPLICANTDIV ";
        $query .= "        AND CONFD002.EXAMNO       = B1.EXAMNO ";
        $query .= "        AND CONFD002.SEQ          = '002' ";
        $query .= "     LEFT JOIN FINSCHOOL_MST F1 ";
        $query .= "         ON F1.FINSCHOOLCD  = B1.FS_CD ";
        $query .= " WHERE ";
        $query .= "     B1.ENTEXAMYEAR      = '{$model->year}' ";
        $query .= "     AND B1.APPLICANTDIV = '{$model->applicantdiv}' ";
        $query .= "     AND B1.EXAMNO       = '{$examno}' ";
        $query .= " ORDER BY ";
        $query .= "     B1.EXAMNO ";

        return $query;
    }
    //出身学校マスタよりデータを取得
    public function getFinschoolName($cd)
    {
        $query  = " SELECT  T1.FINSCHOOL_NAME, VALUE(L3.NAME1, '') AS FINSCHOOL_DISTCD_NAME ";
        $query .= "   FROM  FINSCHOOL_MST T1 ";
        $query .= "         LEFT JOIN NAME_MST L3 ON  L3.NAMECD1 = 'L001' ";
        $query .= "                               AND L3.NAMECD2 = T1.FINSCHOOL_DISTCD ";
        $query .= "  WHERE  T1.FINSCHOOLCD = '".$cd."' ";
        return $query;
    }
}
