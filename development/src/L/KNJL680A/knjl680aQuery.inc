<?php

require_once('for_php7.php');
class knjl680aQuery extends Query
{

    //入試年度取得
    public function getEntExamYear()
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     ENTEXAMYEAR ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_APPLICANTBASE_DAT ";
        $query .= " ORDER BY ";
        $query .= "     ENTEXAMYEAR DESC ";

        return $query;
    }

    //現在の年度のデータが無くなった時に次年度以降の値を取得
    public function deleteAtExist($model)
    {
        $query  = " SELECT ";
        $query .= "     MIN(ENTEXAMYEAR) ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_APPLICANTBASE_DAT ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR >= '".$model->examyear."' ";

        return $query;
    }

    //入試制度取得(入試区分一覧取得用)
    public function getVNameMst($model, $namecd1, $namecd2 = "")
    {
        $query  = " SELECT ";
        $query .= "     NAME1, ";
        $query .= "     NAMECD2, ";
        $query .= "     ABBV1 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$model->examyear}' ";
        $query .= "     AND NAMECD1 = '{$namecd1}' ";
        if ($namecd2) {
            $query .= " AND NAMECD2 = '".$namecd2."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     NAMECD2 ";

        return $query;
    }

    //名称マスタよりNAMESPARE2取得(初期値を取得できていない場合)
    public function getNameSpare2($model, $namecd1)
    {
        $query  = " SELECT ";
        $query .= "     NAMESPARE2 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "         YEAR       = '".$model->examyear."' ";
        $query .= "     AND NAMECD1    = '".$namecd1."' ";
        $query .= "     AND NAMESPARE2 = '1' ";

        return $query;
    }

    //名称マスタより取得(初期値を取得できていない場合)
    public function getNameMstSet($model, $namecd1, $namespare2)
    {
        $query  = " SELECT ";
        $query .= "     NAMECD2 AS VALUE ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR    = '".$model->examyear."' ";
        $query .= " AND NAMECD1 = '".$namecd1."' ";
        if ($namespare2) {
            $query .= " AND NAMESPARE2 = '".$namespare2."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     int(VALUE) ";

        return $query;
    }

    //テスト科目取得
    public function getTestSubclass($model)
    {
        $useName = ($model->applicantdiv == "1") ? "NAME2" : "NAME1";

        $query  = "    SELECT ";
        $query .= "      NAMECD2 ";
        $query .= "    FROM ";
        $query .= "      V_NAME_MST ";
        $query .= "    WHERE ";
        $query .= "      YEAR = '".$model->examyear."' AND ";
        $query .= "      NAMECD1 = 'L009' AND ";
        $query .= "      {$useName} IS NOT NULL ";

        return $query;
    }

    //CSVデータ出力
    public function selectMainQuery($model)
    {
        $useName = ($model->applicantdiv == "2") ? "NAME1" : "NAME2" ;
        $l004namecd1 = ($model->applicantdiv == "1") ? "L024" : "L004";

        //合格した入試区分の中で最小(合格回)を取得
        $query .= " WITH MIN_TESTDIV AS ( ";
        $query .= "   SELECT ";
        $query .= "       ENTEXAMYEAR ";
        $query .= "     , APPLICANTDIV ";
        $query .= "     , MIN(TESTDIV) TESTDIV ";
        $query .= "     , EXAMNO ";
        $query .= "   FROM ENTEXAM_RECEPT_DAT ";
        $query .= "   WHERE ";
        $query .= "     ENTEXAMYEAR = '{$model->examyear}' ";
        $query .= "     AND APPLICANTDIV = '{$model->applicantdiv}' ";
        $query .= "     AND EXAM_TYPE = '1' ";
        $query .= "     AND JUDGEDIV = '1' ";
        $query .= "   GROUP BY ";
        $query .= "       ENTEXAMYEAR ";
        $query .= "     , APPLICANTDIV ";
        $query .= "     , EXAMNO ";
        $query .= " ) ";

        $query .= "    SELECT ";
        //1人に対して指定した試験区分の数だけレコードを作る
        if ($model->field["OUTPUTDIV"] == "2") {
            $query .= "      NMST.NAMECD2 AS TESTDIV2, ";
            $query .= "      CASE WHEN RECEPT.TESTDIV IS NULL ";  //実際にそのTESTDIVのレコードが存在するかのフラグ
            $query .= "       THEN '1' ";
            $query .= "       ELSE '0' ";
            $query .= "      END AS NOT_TAKEN, ";
        }
        $query .= "      BASE.APPLICANTDIV, ";
        $query .= "      NAMEL003.NAME1 AS APPLICANTDIV_NAME, ";
        $query .= "      RECEPT.TESTDIV, ";
        $query .= "      NAMEL004.NAME1 AS TESTDIV_NAME, ";
        $query .= "      BASE.EXAMNO, ";
        $query .= "      BASE.NAME, ";
        $query .= "      BASE.NAME_KANA, ";
        $query .= "      BASE.SEX, ";
        $query .= "      ADDR.ZIPCD, ";
        $query .= "      ADDR.ADDRESS1, ";
        $query .= "      ADDR.ADDRESS2, ";
        $query .= "      BASE.FS_CD, ";
        $query .= "      FINS.FINSCHOOL_NAME, ";
        $query .= "      FINS.FINSCHOOL_PREF_CD, ";
        $query .= "      PREF.PREF_NAME AS FINSCHOOL_PREF_NAME, ";
        $query .= "      RDET010.REMARK1 || RDET010.REMARK2 AS SEAT_NO, ";
        if ($model->applicantdiv == "2") {
            $query .= "      BDET005.REMARK4 AS ENNOU_FLG, ";
            $query .= "      CASE WHEN BDET005.REMARK4 = '1' THEN '延納希望' ELSE '' END AS ENNOU_FLG_NAME, ";
        }
        $query .= "      RDET007.REMARK1 AS SIBOU1_EXAMCOURSECD, ";
        $query .= "      CMST1.EXAMCOURSE_NAME AS SIBOU1_EXAMCOURSE_NAME, ";
        $query .= "      RDET007.REMARK2 AS SIBOU2_EXAMCOURSECD, ";
        $query .= "      CMST2.EXAMCOURSE_NAME AS SIBOU2_EXAMCOURSE_NAME, ";
        if ($model->applicantdiv == "2") {
            $query .= "      BDET005.REMARK1 AS SP_ACT_FLG, ";
        }
        $query .= "      BDET005.REMARK2 AS KIKOKU_FLG, ";
        $query .= "      CASE WHEN BDET005.REMARK2 = '1' THEN '帰国子女' ELSE '' END AS KIKOKU_FLG_NAME, ";
        $query .= "      BDET005.REMARK3 AS QUALIFIED_ENG_CD, ";
        $query .= "      NAMEL055.NAME1 AS QUALIFIED_ENG_NAME, ";
        if ($model->applicantdiv == "2") {
            $query .= "      RDET007.REMARK5 AS RDET007RMK5, ";
            $query .= "      CASE RDET007.REMARK5 ";
            $query .= "         WHEN '1' THEN '寮' ";
            $query .= "         WHEN '2' THEN '通学' ";
            $query .= "         ELSE NULL ";
            $query .= "      END AS TUGAKU_DIV_NAME, ";
        }
        $query .= "      ADDR.GNAME, ";
        $query .= "      ADDR.GKANA, ";
        $query .= "      ADDR.GTELNO, ";
        $query .= "      ADDR.EMERGENCYTELNO, ";
        $query .= "      ADDR.RELATIONSHIP, ";
        $query .= "      NAMEH201.NAME1 AS RELATIONSHIP_NAME, ";
        $query .= "      BDET005.REMARK5 AS SIBOU1_SCHOOL_CD, ";
        $query .= "      SIBOU1.FINSCHOOL_NAME SIBOU1_SCHOOL_NAME, ";
        $query .= "      BDET005.REMARK6 AS SIBOU2_SCHOOL_CD, ";
        $query .= "      SIBOU2.FINSCHOOL_NAME SIBOU2_SCHOOL_NAME, ";
        $query .= "      BDET005.REMARK7 AS SIBOU3_SCHOOL_CD, ";
        $query .= "      SIBOU3.FINSCHOOL_NAME SIBOU3_SCHOOL_NAME, ";
        if ($model->applicantdiv == "2") {
            $query .= "      CONFRPT.TOTAL5, ";
            $query .= "      CONFRPT.ABSENCE_DAYS3, ";
            $query .= "      NAMEL067.NAME1 AS ABSENCE_DAYS3_NAME, ";
            $query .= "      CONFRPT.SPECIALACTREC, ";
            $query .= "      NAMEL068.NAME1 AS SPECIALACTREC_NAME, ";
            $query .= "      CONFRPT.TOTALSTUDYTIME, ";
            $query .= "      NAMEL069.NAME1 AS TOTALSTUDYTIME_NAME, ";
        }
        foreach ($model->testCdArr as $key => $val) {
            $query .= "      TESTSUB{$key}.{$useName} AS SUBNAME{$key}, ";
            $query .= "      TESTSUB{$key}.SCORE AS SUBSCORE{$key}, ";
            $query .= "      TESTSUB{$key}.SCORE2 AS SUBSCORE{$key}_2, ";
        }
        $sep = "";
        foreach ($model->testCdArr as $key => $val) {
            $query .= $sep;
            $query .= "      VALUE(TESTSUB{$key}.SCORE, 0) ";
            $sep = "+";
        }
        $query .= "      AS TOTAL_SCORE, ";
        $query .= "      RECEPT.TOTAL4, ";
        $query .= "      RECEPT.TOTAL_RANK4, ";
        if ($model->applicantdiv == "1") {
            $query .= "      CASE WHEN RDET007.REMARK7 = '1' OR RDET007.REMARK8 = '1' ";
            $query .= "           THEN '1' ";
            $query .= "           ELSE CASE WHEN RDET007.REMARK7 = '2' OR RDET007.REMARK8 = '2' ";
            $query .= "                     THEN '2' ";
            $query .= "                     ELSE CASE WHEN RDET007.REMARK7 = '9' OR RDET007.REMARK8 = '9' ";
            $query .= "                               THEN '9' ";
            $query .= "                               ELSE '' ";
            $query .= "                          END ";
            $query .= "                END ";
            $query .= "      END AS JUDGEDIV, ";
            $query .= "      CASE WHEN RDET007.REMARK7 = '1' OR RDET007.REMARK8 = '1' ";
            $query .= "           THEN '合格' ";
            $query .= "           ELSE CASE WHEN RDET007.REMARK7 = '2' OR RDET007.REMARK8 = '2' ";
            $query .= "                     THEN '不合格' ";
            $query .= "                     ELSE CASE WHEN RDET007.REMARK7 = '9' OR RDET007.REMARK8 = '9' ";
            $query .= "                               THEN '欠席' ";
            $query .= "                               ELSE '' ";
            $query .= "                          END ";
            $query .= "                END ";
            $query .= "      END AS JUDGEDIV_NAME, ";
            $query .= "      CASE WHEN RDET007.REMARK7 = '1' THEN RDET007.REMARK1 ";
            $query .= "           WHEN RDET007.REMARK8 = '1' THEN RDET007.REMARK2 ";
            $query .= "           ELSE '' ";
            $query .= "      END AS PASS_COURSE_CD, ";
            $query .= "      CASE WHEN RDET007.REMARK7 = '1' THEN CMST1.EXAMCOURSE_NAME ";
            $query .= "           WHEN RDET007.REMARK8 = '1' THEN CMST2.EXAMCOURSE_NAME ";
            $query .= "           ELSE '' ";
            $query .= "      END AS PASS_COURSE_NAME, ";
        } else {
            $query .= "      RECEPT.JUDGEDIV, ";
            $query .= "      NAMEL013.NAME1 AS JUDGEDIV_NAME, ";
            $query .= "      CASE WHEN RECEPT.JUDGEDIV = '1' THEN BASE.SUC_COURSECODE ELSE '' END AS PASS_COURSE_CD, ";
            $query .= "      CASE WHEN RECEPT.JUDGEDIV = '1' THEN CMST_PASS.EXAMCOURSE_NAME ELSE '' END AS PASS_COURSE_NAME, ";
        }
        $query .= "      RDET015.REMARK1 AS SP_SCHOLAR_DIV, ";
        $query .= "      SCHOLAR.SP_SCHOLAR_NAME, ";
        if ($model->applicantdiv == "2") {
            $query .= "      RDET007.REMARK6 AS APPLICANT_METHOD, ";
            $query .= "      NAMEL071.NAME1 AS APPLICANT_METHOD_NAME, ";
            $query .= "      BASE.FS_GRDDIV, ";
            $query .= "      NAMEL016.NAME1 AS FS_GRDDIV_NAME, ";
        }
        $query .= "      BDET008.REMARK1 AS PRISCHOOLCD, ";
        $query .= "      PRISCHOOL.PRISCHOOL_NAME, ";
        $query .= "      BDET009.REMARK1 AS BDET009RMK1, ";
        $query .= "      BDET009.REMARK2 AS BDET009RMK2, ";
        $query .= "      BDET009.REMARK3 AS BDET009RMK3, ";
        $query .= "      BDET009.REMARK4 AS BDET009RMK4, ";
        $query .= "      BDET009.REMARK5 AS BDET009RMK5, ";
        $query .= "      RDET009.REMARK1 AS RDET009RMK1, ";
        $query .= "      RDET009.REMARK2 AS RDET009RMK2, ";
        $query .= "      RDET009.REMARK3 AS RDET009RMK3, ";
        $query .= "      RDET009.REMARK4 AS RDET009RMK4, ";
        $query .= "      RDET009.REMARK5 AS RDET009RMK5, ";
        $query .= "      BASE.UPDATED ";
        $query .= "    FROM ";
        $query .= "      ENTEXAM_APPLICANTBASE_DAT BASE ";
        $query .= "      LEFT JOIN V_NAME_MST NAMEL003 ";
        $query .= "        ON NAMEL003.YEAR       = BASE.ENTEXAMYEAR ";
        $query .= "        AND NAMEL003.NAMECD1   = 'L003' ";
        $query .= "        AND NAMEL003.NAMECD2   = BASE.APPLICANTDIV ";
        if ($model->field["OUTPUTDIV"] == "2") {
            $query .= "      LEFT JOIN V_NAME_MST NMST ";
            $query .= "          ON NMST.YEAR = BASE.ENTEXAMYEAR ";
            $query .= "         AND NMST.NAMECD1 = '".$l004namecd1."' ";
            $query .= "         AND NMST.NAMECD2 IN ('".implode("','", $model->selectedTestDiv)."') ";
        }
        $query .= "      LEFT JOIN FINSCHOOL_MST FINS ";
        $query .= "        ON BASE.FS_CD = FINS.FINSCHOOLCD ";
        $query .= "      LEFT JOIN PREF_MST PREF ";
        $query .= "        ON PREF.PREF_CD = FINS.FINSCHOOL_PREF_CD ";
        $query .= "      LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT BDET005 ";
        $query .= "        ON BASE.ENTEXAMYEAR = BDET005.ENTEXAMYEAR ";
        $query .= "        AND BASE.APPLICANTDIV = BDET005.APPLICANTDIV ";
        $query .= "        AND BASE.EXAMNO = BDET005.EXAMNO ";
        $query .= "        AND BDET005.SEQ = '005' ";
        $query .= "      LEFT JOIN V_NAME_MST NAMEL055 ";
        $query .= "        ON NAMEL055.YEAR       = BDET005.ENTEXAMYEAR ";
        $query .= "        AND NAMEL055.NAMECD1   = 'L055' ";
        $query .= "        AND NAMEL055.NAMECD2   = BDET005.REMARK3 ";
        $query .= "      LEFT JOIN FINSCHOOL_MST SIBOU1 ";
        $query .= "        ON SIBOU1.FINSCHOOLCD = BDET005.REMARK5 ";
        $query .= "      LEFT JOIN FINSCHOOL_MST SIBOU2 ";
        $query .= "        ON SIBOU2.FINSCHOOLCD = BDET005.REMARK6 ";
        $query .= "      LEFT JOIN FINSCHOOL_MST SIBOU3 ";
        $query .= "        ON SIBOU3.FINSCHOOLCD = BDET005.REMARK7 ";
        $query .= "      LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT BDET006 ";
        $query .= "        ON BASE.ENTEXAMYEAR = BDET006.ENTEXAMYEAR ";
        $query .= "        AND BASE.APPLICANTDIV = BDET006.APPLICANTDIV ";
        $query .= "        AND BASE.EXAMNO = BDET006.EXAMNO ";
        $query .= "        AND BDET006.SEQ = '006' ";
        $query .= "      LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT BDET007 ";
        $query .= "        ON BASE.ENTEXAMYEAR = BDET007.ENTEXAMYEAR ";
        $query .= "        AND BASE.APPLICANTDIV = BDET007.APPLICANTDIV ";
        $query .= "        AND BASE.EXAMNO = BDET007.EXAMNO ";
        $query .= "        AND BDET007.SEQ = '007' ";
        $query .= "      LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT BDET008 ";
        $query .= "        ON BASE.ENTEXAMYEAR = BDET008.ENTEXAMYEAR ";
        $query .= "        AND BASE.APPLICANTDIV = BDET008.APPLICANTDIV ";
        $query .= "        AND BASE.EXAMNO = BDET008.EXAMNO ";
        $query .= "        AND BDET008.SEQ = '008' ";
        $query .= "      LEFT JOIN PRISCHOOL_MST PRISCHOOL ";
        $query .= "        ON PRISCHOOL.PRISCHOOLCD = BDET008.REMARK1 ";
        $query .= "      LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT BDET009 ";
        $query .= "        ON BASE.ENTEXAMYEAR = BDET009.ENTEXAMYEAR ";
        $query .= "        AND BASE.APPLICANTDIV = BDET009.APPLICANTDIV ";
        $query .= "        AND BASE.EXAMNO = BDET009.EXAMNO ";
        $query .= "        AND BDET009.SEQ = '009' ";
        $query .= "      LEFT JOIN MIN_TESTDIV MIN_T ";
        $query .= "        ON MIN_T.ENTEXAMYEAR   = BASE.ENTEXAMYEAR ";
        $query .= "        AND MIN_T.APPLICANTDIV = BASE.APPLICANTDIV ";
        $query .= "        AND MIN_T.TESTDIV      = BASE.TESTDIV ";
        $query .= "        AND MIN_T.EXAMNO       = BASE.EXAMNO ";
        $query .= "      LEFT JOIN ENTEXAM_APPLICANTADDR_DAT ADDR ";
        $query .= "        ON BASE.ENTEXAMYEAR = ADDR.ENTEXAMYEAR ";
        $query .= "        AND BASE.APPLICANTDIV = ADDR.APPLICANTDIV ";
        $query .= "        AND BASE.EXAMNO = ADDR.EXAMNO ";
        $query .= "      LEFT JOIN V_NAME_MST NAMEH201 ";
        $query .= "        ON NAMEH201.YEAR       = ADDR.ENTEXAMYEAR ";
        $query .= "        AND NAMEH201.NAMECD1   = 'H201' ";
        $query .= "        AND NAMEH201.NAMECD2   = ADDR.RELATIONSHIP ";
        if ($model->applicantdiv == "2") {
            $query .= "      LEFT JOIN ENTEXAM_APPLICANTCONFRPT_DAT CONFRPT ";
            $query .= "        ON BASE.ENTEXAMYEAR = CONFRPT.ENTEXAMYEAR ";
            $query .= "        AND BASE.APPLICANTDIV = CONFRPT.APPLICANTDIV ";
            $query .= "        AND BASE.EXAMNO = CONFRPT.EXAMNO ";
            $query .= "      LEFT JOIN V_NAME_MST NAMEL067 ";
            $query .= "        ON NAMEL067.YEAR       = CONFRPT.ENTEXAMYEAR ";
            $query .= "        AND NAMEL067.NAMECD1   = 'L067' ";
            $query .= "        AND NAMEL067.NAMECD2   = CONFRPT.ABSENCE_DAYS3 ";
            $query .= "      LEFT JOIN V_NAME_MST NAMEL068 ";
            $query .= "        ON NAMEL068.YEAR       = CONFRPT.ENTEXAMYEAR ";
            $query .= "        AND NAMEL068.NAMECD1   = 'L068' ";
            $query .= "        AND NAMEL068.NAMECD2   = CONFRPT.SPECIALACTREC ";
            $query .= "      LEFT JOIN V_NAME_MST NAMEL069 ";
            $query .= "        ON NAMEL069.YEAR       = CONFRPT.ENTEXAMYEAR ";
            $query .= "        AND NAMEL069.NAMECD1   = 'L069' ";
            $query .= "        AND NAMEL069.NAMECD2   = CONFRPT.TOTALSTUDYTIME ";
        }
        $query .= "      LEFT JOIN ENTEXAM_RECEPT_DAT RECEPT ";
        $query .= "        ON BASE.ENTEXAMYEAR = RECEPT.ENTEXAMYEAR ";
        $query .= "        AND BASE.APPLICANTDIV = RECEPT.APPLICANTDIV ";
        $query .= "        AND BASE.EXAMNO = RECEPT.EXAMNO ";
        if ($model->field["OUTPUTDIV"] == "2") {
            $query .= "        AND NMST.NAMECD2 = RECEPT.TESTDIV ";
        }
        $query .= "      LEFT JOIN V_NAME_MST NAMEL004 ";
        $query .= "        ON NAMEL004.YEAR       = RECEPT.ENTEXAMYEAR ";
        $query .= "        AND NAMEL004.NAMECD1   = '".$l004namecd1."' ";
        $query .= "        AND NAMEL004.NAMECD2   = RECEPT.TESTDIV ";
        $query .= "      LEFT JOIN V_NAME_MST NAMEL013 ";
        $query .= "        ON NAMEL013.YEAR       = RECEPT.ENTEXAMYEAR ";
        $query .= "        AND NAMEL013.NAMECD1   = 'L013' ";
        $query .= "        AND NAMEL013.NAMECD2   = RECEPT.JUDGEDIV ";
        $query .= "      LEFT JOIN ENTEXAM_RECEPT_DETAIL_DAT RDET007 ";
        $query .= "        ON RECEPT.ENTEXAMYEAR = RDET007.ENTEXAMYEAR ";
        $query .= "        AND RECEPT.APPLICANTDIV = RDET007.APPLICANTDIV ";
        $query .= "        AND RECEPT.TESTDIV = RDET007.TESTDIV ";
        $query .= "        AND RECEPT.EXAM_TYPE = RDET007.EXAM_TYPE ";
        $query .= "        AND RECEPT.RECEPTNO = RDET007.RECEPTNO ";
        $query .= "        AND RDET007.SEQ = '007' ";
        $query .= "      LEFT JOIN ENTEXAM_RECEPT_DETAIL_DAT RDET009 ";
        $query .= "        ON RECEPT.ENTEXAMYEAR = RDET009.ENTEXAMYEAR ";
        $query .= "        AND RECEPT.APPLICANTDIV = RDET009.APPLICANTDIV ";
        $query .= "        AND RECEPT.TESTDIV = RDET009.TESTDIV ";
        $query .= "        AND RECEPT.EXAM_TYPE = RDET009.EXAM_TYPE ";
        $query .= "        AND RECEPT.RECEPTNO = RDET009.RECEPTNO ";
        $query .= "        AND RDET009.SEQ = '009' ";
        $query .= "      LEFT JOIN ENTEXAM_RECEPT_DETAIL_DAT RDET010 ";
        $query .= "        ON RECEPT.ENTEXAMYEAR = RDET010.ENTEXAMYEAR ";
        $query .= "        AND RECEPT.APPLICANTDIV = RDET010.APPLICANTDIV ";
        $query .= "        AND RECEPT.TESTDIV = RDET010.TESTDIV ";
        $query .= "        AND RECEPT.EXAM_TYPE = RDET010.EXAM_TYPE ";
        $query .= "        AND RECEPT.RECEPTNO = RDET010.RECEPTNO ";
        $query .= "        AND RDET010.SEQ = '010' ";
        $query .= "      LEFT JOIN ENTEXAM_RECEPT_DETAIL_DAT RDET015 ";
        $query .= "        ON RECEPT.ENTEXAMYEAR  = RDET015.ENTEXAMYEAR ";
        $query .= "        AND RECEPT.APPLICANTDIV = RDET015.APPLICANTDIV ";
        $query .= "        AND RECEPT.TESTDIV = RDET015.TESTDIV ";
        $query .= "        AND RECEPT.EXAM_TYPE = RDET015.EXAM_TYPE ";
        $query .= "        AND RECEPT.RECEPTNO = RDET015.RECEPTNO ";
        $query .= "        AND RDET015.SEQ = '015' ";
        $query .= "      LEFT JOIN ENTEXAM_SPECIAL_SCHOLARSHIP_MST SCHOLAR ";
        $query .= "        ON SCHOLAR.ENTEXAMYEAR    = RDET015.ENTEXAMYEAR ";
        $query .= "        AND SCHOLAR.APPLICANTDIV  = RDET015.APPLICANTDIV ";
        $query .= "        AND SCHOLAR.TESTDIV       = RDET015.TESTDIV ";
        $query .= "        AND SCHOLAR.SP_SCHOLAR_CD = RDET015.REMARK1 ";
        $query .= "      LEFT JOIN ENTEXAM_COURSE_MST CMST_PASS ";
        $query .= "        ON CMST_PASS.ENTEXAMYEAR   = RECEPT.ENTEXAMYEAR ";
        $query .= "        AND CMST_PASS.APPLICANTDIV = RECEPT.APPLICANTDIV ";
        $query .= "        AND CMST_PASS.TESTDIV      = RECEPT.TESTDIV ";
        $query .= "        AND CMST_PASS.COURSECD     = '0' ";
        $query .= "        AND CMST_PASS.MAJORCD      = '000' ";
        $query .= "        AND CMST_PASS.EXAMCOURSECD = BASE.SUC_COURSECODE ";
        $query .= "      LEFT JOIN ENTEXAM_COURSE_MST CMST1 ";
        $query .= "        ON CMST1.ENTEXAMYEAR   = RDET007.ENTEXAMYEAR ";
        $query .= "        AND CMST1.APPLICANTDIV = RDET007.APPLICANTDIV ";
        $query .= "        AND CMST1.TESTDIV      = RDET007.TESTDIV ";
        $query .= "        AND CMST1.COURSECD     = '0' ";
        $query .= "        AND CMST1.MAJORCD      = '000' ";
        $query .= "        AND CMST1.EXAMCOURSECD = RDET007.REMARK1 ";
        $query .= "      LEFT JOIN ENTEXAM_COURSE_MST CMST2 ";
        $query .= "        ON CMST2.ENTEXAMYEAR   = RDET007.ENTEXAMYEAR ";
        $query .= "        AND CMST2.APPLICANTDIV = RDET007.APPLICANTDIV ";
        $query .= "        AND CMST2.TESTDIV      = RDET007.TESTDIV ";
        $query .= "        AND CMST2.COURSECD     = '0' ";
        $query .= "        AND CMST2.MAJORCD      = '000' ";
        $query .= "        AND CMST2.EXAMCOURSECD = RDET007.REMARK2 ";
        if ($model->applicantdiv == "2") {
            $query .= "      LEFT JOIN V_NAME_MST NAMEL071 ";
            $query .= "        ON NAMEL071.YEAR       = RDET007.ENTEXAMYEAR ";
            $query .= "        AND NAMEL071.NAMECD1   = 'L071' ";
            $query .= "        AND NAMEL071.NAMECD2   = RDET007.REMARK6 ";
        }
        $query .= "      LEFT JOIN V_NAME_MST NAMEL016 ";
        $query .= "        ON NAMEL016.YEAR       = BASE.ENTEXAMYEAR ";
        $query .= "        AND NAMEL016.NAMECD1   = 'L016' ";
        $query .= "        AND NAMEL016.NAMECD2   = BASE.FS_GRDDIV ";
        //テスト得点
        foreach ($model->testCdArr as $key => $val) {
            $query .= "      LEFT JOIN ( ";
            $query .= "        SELECT ";
            $query .= "          * ";
            $query .= "        FROM ";
            $query .= "          ENTEXAM_SCORE_DAT T1 ";
            $query .= "          INNER JOIN V_NAME_MST T2 ";
            $query .= "            ON T1.ENTEXAMYEAR = T2.YEAR ";
            $query .= "            AND T1.TESTSUBCLASSCD = T2.NAMECD2 ";
            $query .= "            AND T2.NAMECD1 = 'L009' ";
            $query .= "            AND T2.NAMECD2 = '{$val}' ";
            $query .= "      ) TESTSUB{$key} ";
            $query .= "        ON RECEPT.ENTEXAMYEAR = TESTSUB{$key}.ENTEXAMYEAR ";
            $query .= "        AND RECEPT.APPLICANTDIV = TESTSUB{$key}.APPLICANTDIV ";
            $query .= "        AND RECEPT.TESTDIV = TESTSUB{$key}.TESTDIV ";
            $query .= "        AND RECEPT.EXAM_TYPE = TESTSUB{$key}.EXAM_TYPE ";
            $query .= "        AND RECEPT.RECEPTNO = TESTSUB{$key}.RECEPTNO ";
        }
        $query .= "        WHERE ";
        $query .= "         BASE.ENTEXAMYEAR    = '".$model->examyear."' AND ";
        $query .= "         BASE.APPLICANTDIV   = '".$model->applicantdiv."' ";
        if ($model->field["OUTPUTDIV"] != "2") {
            $query .= "         AND RECEPT.TESTDIV        IN ('".implode("','", $model->selectedTestDiv)."') ";
        }
        $query .= "        ORDER BY ";
        $query .= "         BASE.ENTEXAMYEAR, ";
        $query .= "         BASE.APPLICANTDIV, ";
        if ($model->field["OUTPUTDIV"] == "2") {
            $query .= "         BASE.EXAMNO, ";
            $query .= "         TESTDIV2 ";
        } else {
            $query .= "         RECEPT.TESTDIV, ";
            $query .= "         BASE.EXAMNO ";
        }

        return $query;
    }

    //合格、合格コース、合格コース名称を取得する。
    public function selectJudgement($model)
    {
        $query  = " WITH G_RECEPT AS ( ";
        $query .= "     SELECT ";
        $query .= "         ENTEXAMYEAR, ";
        $query .= "         APPLICANTDIV, ";
        $query .= "         EXAM_TYPE, ";
        $query .= "         EXAMNO ";
        $query .= "     FROM ";
        $query .= "         ENTEXAM_RECEPT_DAT ";
        $query .= "     WHERE ";
        $query .= "             ENTEXAMYEAR  = '".$model->examyear."' ";
        $query .= "         AND APPLICANTDIV = '".$model->applicantdiv."' ";
        $query .= "         AND EXAM_TYPE    = '1' ";
        $query .= "     GROUP BY ";
        $query .= "         ENTEXAMYEAR, ";
        $query .= "         APPLICANTDIV, ";
        $query .= "         EXAM_TYPE, ";
        $query .= "         EXAMNO ";
        $query .= "     HAVING ";
        $query .= "         COUNT(EXAMNO) > 1 ";
        $query .= " ), ";
        $query .= " DUP_RECEPT AS ( ";
        $query .= "     SELECT ";
        $query .= "         RE.ENTEXAMYEAR, ";
        $query .= "         RE.APPLICANTDIV, ";
        $query .= "         RE.TESTDIV, ";
        $query .= "         RE.EXAM_TYPE, ";
        $query .= "         RE.RECEPTNO, ";
        $query .= "         RE.EXAMNO ";
        $query .= "     FROM ";
        $query .= "         ENTEXAM_RECEPT_DAT RE ";
        $query .= "         INNER JOIN G_RECEPT ON ";
        $query .= "             G_RECEPT.ENTEXAMYEAR  = RE.ENTEXAMYEAR ";
        $query .= "         AND G_RECEPT.APPLICANTDIV = RE.APPLICANTDIV ";
        $query .= "         AND G_RECEPT.EXAM_TYPE    = RE.EXAM_TYPE ";
        $query .= "         AND G_RECEPT.EXAMNO       = RE.EXAMNO ";
        $query .= " ), ";
        $query .= " RDIV1 AS ( ";
        $query .= "     SELECT ";
        $query .= "         RE.ENTEXAMYEAR, ";
        $query .= "         RE.APPLICANTDIV, ";
        $query .= "         RE.EXAM_TYPE, ";
        $query .= "         RE.EXAMNO, ";
        $query .= "         BASE.SUC_COURSECODE, ";
        $query .= "         CMST_PASS.EXAMCOURSE_NAME ";
        $query .= "     FROM ";
        $query .= "         ENTEXAM_RECEPT_DAT RE ";
        $query .= "         LEFT JOIN ENTEXAM_APPLICANTBASE_DAT BASE ON ";
        $query .= "             BASE.ENTEXAMYEAR  = RE.ENTEXAMYEAR ";
        $query .= "         AND BASE.APPLICANTDIV = RE.APPLICANTDIV ";
        $query .= "         AND BASE.EXAMNO       = RE.EXAMNO ";
        $query .= "         LEFT JOIN ENTEXAM_COURSE_MST CMST_PASS ON ";
        $query .= "             CMST_PASS.ENTEXAMYEAR  = RE.ENTEXAMYEAR ";
        $query .= "         AND CMST_PASS.APPLICANTDIV = RE.APPLICANTDIV ";
        $query .= "         AND CMST_PASS.TESTDIV      = RE.TESTDIV ";
        $query .= "         AND CMST_PASS.COURSECD     = '0' ";
        $query .= "         AND CMST_PASS.MAJORCD      = '000' ";
        $query .= "         AND CMST_PASS.EXAMCOURSECD = BASE.SUC_COURSECODE ";
        $query .= "     WHERE ";
        $query .= "             RE.TESTDIV   = '1' ";
        $query .= "         AND RE.JUDGEDIV  = '1' ";
        $query .= " ), ";
        $query .= " RDIV2 AS ( ";
        $query .= "     SELECT ";
        $query .= "         RE.ENTEXAMYEAR, ";
        $query .= "         RE.APPLICANTDIV, ";
        $query .= "         RE.EXAM_TYPE, ";
        $query .= "         RE.EXAMNO, ";
        $query .= "         BASE.SUC_COURSECODE, ";
        $query .= "         CMST_PASS.EXAMCOURSE_NAME ";
        $query .= "     FROM ";
        $query .= "         ENTEXAM_RECEPT_DAT RE ";
        $query .= "         LEFT JOIN ENTEXAM_APPLICANTBASE_DAT BASE ON ";
        $query .= "             BASE.ENTEXAMYEAR  = RE.ENTEXAMYEAR ";
        $query .= "         AND BASE.APPLICANTDIV = RE.APPLICANTDIV ";
        $query .= "         AND BASE.EXAMNO       = RE.EXAMNO ";
        $query .= "         LEFT JOIN ENTEXAM_COURSE_MST CMST_PASS ON ";
        $query .= "             CMST_PASS.ENTEXAMYEAR  = RE.ENTEXAMYEAR ";
        $query .= "         AND CMST_PASS.APPLICANTDIV = RE.APPLICANTDIV ";
        $query .= "         AND CMST_PASS.TESTDIV      = RE.TESTDIV ";
        $query .= "         AND CMST_PASS.COURSECD     = '0' ";
        $query .= "         AND CMST_PASS.MAJORCD      = '000' ";
        $query .= "         AND CMST_PASS.EXAMCOURSECD = BASE.SUC_COURSECODE ";
        $query .= "     WHERE ";
        $query .= "             RE.TESTDIV   = '2' ";
        $query .= "         AND RE.JUDGEDIV  = '1' ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     DUP_RECEPT.EXAMNO, ";
        $query .= "     DUP_RECEPT.TESTDIV, ";
        $query .= "     DUP_RECEPT.RECEPTNO, ";
        $query .= "     RDIV1.SUC_COURSECODE, ";
        $query .= "     RDIV1.EXAMCOURSE_NAME, ";
        $query .= "     RDIV2.SUC_COURSECODE, ";
        $query .= "     RDIV2.EXAMCOURSE_NAME, ";
        $query .= "     CASE WHEN DUP_RECEPT.TESTDIV = '1' THEN ";
        $query .= "               CASE WHEN  RDIV1.SUC_COURSECODE IS NULL THEN ";
        $query .= "                          RDIV2.SUC_COURSECODE ";
        $query .= "                    ELSE ";
        $query .= "                          RDIV1.SUC_COURSECODE ";
        $query .= "               END ";
        $query .= "          ELSE ";
        $query .= "               CASE WHEN  RDIV2.SUC_COURSECODE IS NULL THEN ";
        $query .= "                          RDIV1.SUC_COURSECODE ";
        $query .= "                    ELSE ";
        $query .= "                          RDIV2.SUC_COURSECODE ";
        $query .= "               END ";
        $query .= "     END AS PASS_COURSE_CD, ";
        $query .= "     CASE WHEN DUP_RECEPT.TESTDIV = '1' THEN ";
        $query .= "               CASE WHEN  RDIV1.SUC_COURSECODE IS NULL THEN ";
        $query .= "                          RDIV2.EXAMCOURSE_NAME ";
        $query .= "                    ELSE ";
        $query .= "                          RDIV1.EXAMCOURSE_NAME ";
        $query .= "               END ";
        $query .= "          ELSE ";
        $query .= "               CASE WHEN  RDIV2.SUC_COURSECODE IS NULL THEN ";
        $query .= "                          RDIV1.EXAMCOURSE_NAME ";
        $query .= "                    ELSE ";
        $query .= "                          RDIV2.EXAMCOURSE_NAME ";
        $query .= "               END ";
        $query .= "     END AS PASS_COURSE_NAME ";
        $query .= " FROM ";
        $query .= "     DUP_RECEPT  ";
        $query .= "     LEFT JOIN RDIV1 ON ";
        $query .= "         RDIV1.ENTEXAMYEAR  = DUP_RECEPT.ENTEXAMYEAR ";
        $query .= "     AND RDIV1.APPLICANTDIV = DUP_RECEPT.APPLICANTDIV ";
        $query .= "     AND RDIV1.EXAM_TYPE    = DUP_RECEPT.EXAM_TYPE ";
        $query .= "     AND RDIV1.EXAMNO       = DUP_RECEPT.EXAMNO ";
        $query .= "     LEFT JOIN RDIV2 ON ";
        $query .= "         RDIV2.ENTEXAMYEAR  = DUP_RECEPT.ENTEXAMYEAR ";
        $query .= "     AND RDIV2.APPLICANTDIV = DUP_RECEPT.APPLICANTDIV ";
        $query .= "     AND RDIV2.EXAM_TYPE    = DUP_RECEPT.EXAM_TYPE ";
        $query .= "     AND RDIV2.EXAMNO       = DUP_RECEPT.EXAMNO ";
        $query .= " ORDER BY ";
        $query .= "     DUP_RECEPT.EXAMNO ";
        
        return $query;
    }
}
