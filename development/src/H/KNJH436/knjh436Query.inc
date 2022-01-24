<?php

require_once('for_php7.php');

class knjh436Query extends Query {

    function getMockCompany($model) {
        $query .= " SELECT ";
        $query .= "     NAMESPARE1 AS VALUE, ";
        $query .= "     NAMESPARE1 || ':' || NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$model->field["YEAR"]}' ";
        $query .= "     AND NAMECD1 = 'H313' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    function getCsvDiv() {
        $query .= " WITH MAIN (LABEL, VALUE) AS ( ";
        $query .= "     VALUES('得点データ', '1') ";
        $query .= "     UNION ";
        $query .= "     VALUES('志望データ', '2') ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     MAIN ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    function getMockMst($model) {
        $query  = " SELECT ";
        $query .= "     T1.MOCKCD AS VALUE, ";
        $query .= "     T1.MOCKCD || ':' || T1.MOCKNAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     MOCK_MST T1, ";
        if ($model->field["COMPANY"] == "00000001") {
            $query .= "     MOCK_CSV_SCORE_FIELD_DAT T2, ";
            $query .= "     MOCK_CSV_HOPE_FIELD_DAT T3 ";
        } else if ($model->field["CSV_DIV"] == "1") {
            $query .= "     MOCK_CSV_SCORE_FIELD_DAT T2 ";
        } else if ($model->field["CSV_DIV"] == "2") {
            $query .= "     MOCK_CSV_HOPE_FIELD_DAT T2 ";
        } else {
            $query .= "     MOCK_CSV_SCORE_FIELD_DAT T2, ";
            $query .= "     MOCK_CSV_HOPE_FIELD_DAT T3 ";
        }
        $query .= " WHERE ";
        $query .= "     T1.COMPANYCD = '{$model->field["COMPANY"]}' ";
        $query .= "     AND T2.YEAR = '{$model->field["YEAR"]}' ";
        $query .= "     AND T2.GRADE = '{$model->field["GRADE"]}' ";
        $query .= "     AND T1.MOCKCD = T2.MOCKCD ";
        if ($model->field["COMPANY"] == "00000001" || $model->field["COMPANY"] == "00000003" || $model->field["COMPANY"] == "00000004") {
            $query .= "     AND T2.YEAR = T3.YEAR ";
            $query .= "     AND T2.GRADE = T3.GRADE ";
            $query .= "     AND T2.MOCKCD = T3.MOCKCD ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    function getSecurityHigh() {
        $query  = " SELECT ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     MENU_HIGH_SECURITY_MST ";
        $query .= " WHERE ";
        $query .= "     PROGRAMID = 'KNJH436' ";
        $query .= "     AND INVALID_FLG = '0' ";

        return $query;
    }

    //年度一覧
    function getYear($properties)
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     YEAR AS VALUE, ";
        $query .= "     YEAR || '年度' AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHOOL_MST ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= " WHERE ";
                $query .= "     SCHOOLCD = '".sprintf("%012d", SCHOOLCD)."' ";
                $query .= " AND SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
            }
        } else if($properties["useSchool_KindField"] == "1"){
            $query .= " WHERE ";
            $query .= "     SCHOOLCD = '".sprintf("%012d", SCHOOLCD)."' ";
            $query .= " AND ";
            $query .= "     SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //学年
    function getGrade($model)
    {
        $query  = " SELECT ";
        //駿台甲府学園用
        if($model->CHECK_SCHOOL == 'sundaikoufu'){
            $query .= "     GRADE_CD AS VALUE, ";
            $query .= "     GRADE_NAME2 AS LABEL ";
        } else {
            $query .= "     GRADE AS VALUE, ";
            $query .= "     GRADE_NAME1 AS LABEL ";
        }
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_GDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$model->field["YEAR"]}' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= " AND SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
            }
        } else if($model->Properties["useSchool_KindField"] == "1"){
            $query .= " AND ";
            $query .= "     SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //MOCK_RANK_RANGE_DAT
    function getMockRankRangeDat($model)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     MOCK_RANK_RANGE_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$model->field["YEAR"]}' ";
        $query .= "     AND MOCKCD = '{$model->field["MOCKCD"]}' ";
        $query .= "     AND SCHREGNO IN ( ";
        $query .= "                     SELECT ";
        $query .= "                         SCHREGNO ";
        $query .= "                     FROM ";
        $query .= "                         SCHREG_REGD_DAT T1 ";
        $query .= "                     WHERE ";
        $query .= "                         T1.YEAR = '{$model->field["YEAR"]}' ";
        $query .= "                         AND T1.GRADE = '{$model->field["GRADE"]}' ";
        $query .= "                     ) ";

        return $query;
    }

    //名称マスタ存在チェックSQL
    function checkMstData($namecd1, $namecd2)
    {
        $query  = "SELECT COUNT(*)";
        $query .= " FROM NAME_MST";
        $query .= " WHERE NAMECD1 = '".$namecd1."' ";
        $query .= "       AND NAMECD2 = '".$namecd2."' ";

        return $query;
    }

    function getHopeField($model) {
        $query  = " SELECT ";
        $query .= "     T1.FIELD_CNT ";
        $query .= " FROM ";
        $query .= "     MOCK_CSV_HOPE_FIELD_DAT T1 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '{$model->field["YEAR"]}' ";
        $query .= "     AND T1.MOCKCD = '{$model->field["MOCKCD"]}' ";
        $query .= "     AND T1.GRADE = '{$model->field["GRADE"]}' ";

        return $query;
    }

    function getScoreField($model) {
        $query  = " SELECT ";
        $query .= "     T1.FIELD_CNT ";
        $query .= " FROM ";
        $query .= "     MOCK_CSV_SCORE_FIELD_DAT T1 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '{$model->field["YEAR"]}' ";
        $query .= "     AND T1.MOCKCD = '{$model->field["MOCKCD"]}' ";
        $query .= "     AND T1.GRADE = '{$model->field["GRADE"]}' ";

        return $query;
    }

    function getSubclassCd($model) {
        $query  = " SELECT ";
        $query .= "     T1.FIELD_CNT, ";
        $query .= "     T1.MOCK_SUBCLASS_CD ";
        $query .= " FROM ";
        $query .= "     MOCK_CSV_SUBCLASS_REPLACE_DAT T1 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '{$model->field["YEAR"]}' ";
        $query .= "     AND T1.MOCKCD = '{$model->field["MOCKCD"]}' ";
        $query .= "     AND T1.GRADE = '{$model->field["GRADE"]}' ";
        $query .= " ORDER BY ";
        $query .= "     T1.FIELD_CNT ";

        return $query;
    }

    function getSchregNo($model, $hrClass, $attendNo) {
        $hrClass = sprintf("%03d", $hrClass);
        $attendNo = sprintf("%03d", $attendNo);
        $query  = " SELECT ";
        $query .= "     MAX(t1.SCHREGNO) as SCHREGNO ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT t1  ";
        //駿台甲府学園用
        if($model->CHECK_SCHOOL == 'sundaikoufu'){
            $query .= "     left join SCHREG_REGD_GDAT t2 on t1.YEAR = t2.YEAR and t1.GRADE = t2.GRADE ";
        }
        $query .= " WHERE ";
        $query .= "     t1.YEAR = '{$model->field["YEAR"]}' AND ";
        //駿台甲府学園用
        if($model->CHECK_SCHOOL == 'sundaikoufu'){
            $query .= "     t2.GRADE_CD = '{$model->field["GRADE"]}' AND ";
        } else {
            $query .= "     t1.GRADE = '{$model->field["GRADE"]}' AND ";
        }
        $query .= "     t1.HR_CLASS = '{$hrClass}' AND ";
        $query .= "     t1.ATTENDNO = '{$attendNo}' ";

        return $query;
    }

    //マスタのタイトル追加（ＣＳＶデータより読込）
    function insertQueryCsvHead($model, $data_arr)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        if ($model->field["COMPANY"] == "00000001") {

            $query  = " DELETE FROM MOCK_CSV_SUNDAI_HEAD_HDAT ";
            $query .= " WHERE ";
            $query .= "     YEAR = '{$data_arr["KYEAR"]}' ";
            $query .= "     AND MOSI_CD = '{$data_arr["KMOSI_CD"]}' ";
            $db->query($query);

            $hdata = array();
            $hdata["YEAR"][TEXT]             = $data_arr["KYEAR"];
            $hdata["MOSI_CD"][TEXT]          = $data_arr["KMOSI_CD"];
            $hdata["MOCKCD"][TEXT]           = $model->field["MOCKCD"];
            $hdata["TYEAR"][TEXT]            = $data_arr["YEAR"];
            $hdata["TMOSI_CD"][TEXT]         = $data_arr["MOSI_CD"];
            $hdata["TMOSI_NAME"][TEXT]       = $data_arr["MOSI_NAME"];
            $hdata["TKANA"][TEXT]            = $data_arr["KANA"];
            $hdata["THR_CLASS"][TEXT]        = $data_arr["HR_CLASS"];
            $hdata["TATTENDNO"][TEXT]        = $data_arr["ATTENDNO"];
            $hdata["TEXAMNO"][TEXT]          = $data_arr["EXAMNO"];
            $hdata["TSCHOOL_CD"][TEXT]       = $data_arr["SCHOOL_CD"];
            $hdata["TSCHOOL_EDA"][TEXT]      = $data_arr["SCHOOL_EDA"];
            $hdata["TSCHOOL_NAME"][TEXT]     = $data_arr["SCHOOL_NAME"];
            $hdata["TBUNRI_DIV"][TEXT]       = $data_arr["BUNRI_DIV"];
            $hdata["TSEX"][TEXT]             = $data_arr["SEX"];
            $hdata["TGRADE"][TEXT]           = $data_arr["GRADE"];
            $hdata["TGEN_SOTU"][TEXT]        = $data_arr["GEN_SOTU"];
            $hdata["TKAMOKU_EIGO"][TEXT]     = $data_arr["KAMOKU_EIGO"];
            $hdata["TKAMOKU_SUUGAKU"][TEXT]  = $data_arr["KAMOKU_SUUGAKU"];
            $hdata["TKAMOKU_KOKUGO"][TEXT]   = $data_arr["KAMOKU_KOKUGO"];
            $hdata["TKAMOKU_RI1"][TEXT]      = $data_arr["KAMOKU_RI1"];
            $hdata["TKAMOKU_RI2"][TEXT]      = $data_arr["KAMOKU_RI2"];
            $hdata["TKAMOKU_RI3"][TEXT]      = $data_arr["KAMOKU_RI3"];
            $hdata["TKAMOKU_REKIKOU1"][TEXT] = $data_arr["KAMOKU_REKIKOU1"];
            $hdata["TKAMOKU_REKIKOU2"][TEXT] = $data_arr["KAMOKU_REKIKOU2"];
            $hdata["TRIKA_FIRST"][TEXT]      = $data_arr["RIKA_FIRST"];
            $hdata["TREKIKOU_FIRST"][TEXT]   = $data_arr["REKIKOU_FIRST"];
            $hdata["TDEVIATION"][TEXT]       = $data_arr["DEVIATION"];
            $hdata["TRANK"][TEXT]            = $data_arr["RANK"];
            $hdata["TCNT"][TEXT]             = $data_arr["CNT"];
            $hdata["REGISTERCD"][TEXT]       = STAFFCD;
            $hdata["UPDATED"][NUMBER]        = "SYSDATE()";

            $query = Query::insertSQL($hdata, "MOCK_CSV_SUNDAI_HEAD_HDAT");
            $db->query($query);

            $query  = " DELETE FROM MOCK_CSV_SUNDAI_HOPE_HEAD_DAT ";
            $query .= " WHERE ";
            $query .= "     YEAR = '{$data_arr["KYEAR"]}' ";
            $query .= "     AND MOSI_CD = '{$data_arr["KMOSI_CD"]}' ";
            $db->query($query);

            $query  = " DELETE FROM MOCK_CSV_SUNDAI_SCORE_HEAD_DAT ";
            $query .= " WHERE ";
            $query .= "     YEAR = '{$data_arr["KYEAR"]}' ";
            $query .= "     AND MOSI_CD = '{$data_arr["KMOSI_CD"]}' ";
            $db->query($query);

            for ($hCnt = 1; $hCnt <= $model->hopeCnt; $hCnt++) {
                $hopeData = array();
                $hopeData["YEAR"][TEXT]              = $data_arr["KYEAR"];
                $hopeData["MOSI_CD"][TEXT]           = $data_arr["KMOSI_CD"];
                $hopeData["SEQ"][TEXT]               = sprintf("%03d", $data_arr["SEQ{$hCnt}"]);
                $hopeData["TYEAR"][TEXT]             = $data_arr["YEAR"];
                $hopeData["TMOSI_CD"][TEXT]          = $data_arr["MOSI_CD"];
                $hopeData["TSCHOOL_CD"][TEXT]        = $data_arr["SCHOOL_CD{$hCnt}"];
                $hopeData["TSCHOOL_NAME"][TEXT]      = $data_arr["SCHOOL_NAME{$hCnt}"];
                $hopeData["TNITTEI"][TEXT]           = $data_arr["NITTEI{$hCnt}"];
                $hopeData["TRANK"][TEXT]             = $data_arr["TRANK{$hCnt}"];
                $hopeData["TCNT"][TEXT]              = $data_arr["CNT{$hCnt}"];
                $hopeData["TJUDGE_HYOUKA"][TEXT]     = $data_arr["JUDGE_HYOUKA{$hCnt}"];
                $hopeData["TJUDGE_SUUTI"][TEXT]      = $data_arr["JUDGE_SUUTI{$hCnt}"];
                $hopeData["REGISTERCD"][TEXT]        = STAFFCD;
                $hopeData["UPDATED"][NUMBER]         = "SYSDATE()";

                $query = Query::insertSQL($hopeData, "MOCK_CSV_SUNDAI_HOPE_HEAD_DAT");
                $db->query($query);
            }
            for ($sCnt = 1; $sCnt <= $model->scoreCnt; $sCnt++) {
                $scoreData = array();
                $scoreData["YEAR"][TEXT]              = $data_arr["KYEAR"];
                $scoreData["MOSI_CD"][TEXT]           = $data_arr["KMOSI_CD"];
                $scoreData["SEQ"][TEXT]               = $data_arr["SEQ{$sCnt}"];
                $scoreData["TYEAR"][TEXT]             = $data_arr["YEAR"];
                $scoreData["TMOSI_CD"][TEXT]          = $data_arr["MOSI_CD"];
                $scoreData["TSCORE"][TEXT]            = $data_arr["SCORE{$sCnt}"];
                $scoreData["TDEVIATION"][TEXT]        = $data_arr["DEVIATION{$sCnt}"];
                $scoreData["TRANK"][TEXT]             = $data_arr["RANK{$sCnt}"];
                $scoreData["REGISTERCD"][TEXT]        = STAFFCD;
                $scoreData["UPDATED"][NUMBER]         = "SYSDATE()";

                $query = Query::insertSQL($scoreData, "MOCK_CSV_SUNDAI_SCORE_HEAD_DAT");
                $db->query($query);
            }
        } else if ($model->field["COMPANY"] == "00000002" && $model->field["CSV_DIV"] == "1") {

            $query  = " DELETE FROM MOCK_CSV_BENE_SCORE_HEAD_HDAT ";
            $query .= " WHERE ";
            $query .= "     YEAR = '{$data_arr["KYEAR"]}' ";
            $query .= "     AND KYOUZAICD = '{$data_arr["KKYOUZAICD"]}' ";
            $db->query($query);

            $hdata = array();
            $hdata["YEAR"][TEXT]                = $data_arr["KYEAR"];
            $hdata["KYOUZAICD"][TEXT]           = $data_arr["KKYOUZAICD"];
            $hdata["MOCKCD"][TEXT]              = $data_arr["MOCKCD"];
            $hdata["TYEAR"][TEXT]               = $data_arr["YEAR"];
            $hdata["TKYOUZAI"][TEXT]            = $data_arr["KYOUZAICD"];
            $hdata["TTYPE"][TEXT]               = $data_arr["TYPE"];
            $hdata["TKYOUZAINAME"][TEXT]        = $data_arr["KYOUZAINAME"];
            $hdata["TGAKKACD"][TEXT]            = $data_arr["GAKKACD"];
            $hdata["TGAKKANAME"][TEXT]          = $data_arr["GAKKANAME"];
            $hdata["TBENEID"][TEXT]             = $data_arr["BENEID"];
            $hdata["THR_CLASS"][TEXT]           = $data_arr["HR_CLASS"];
            $hdata["TATTENDNO"][TEXT]           = $data_arr["ATTENDNO"];
            $hdata["TNAME"][TEXT]               = $data_arr["NAME"];
            $hdata["TBUNRI_DIV"][TEXT]          = $data_arr["BUNRI_DIV"];
            $hdata["TBIRTHDAY"][TEXT]           = $data_arr["BIRTHDAY"];
            $hdata["TSEX"][TEXT]                = $data_arr["SEX"];
            $hdata["TDEVIATION"][TEXT]          = $data_arr["DEVIATION"];
            $hdata["TRANK"][TEXT]               = $data_arr["RANK"];
            $hdata["TCNT"][TEXT]                = $data_arr["CNT"];
            $hdata["REGISTERCD"][TEXT]          = STAFFCD;
            $hdata["UPDATED"][NUMBER]           = "SYSDATE()";

            $query = Query::insertSQL($hdata, "MOCK_CSV_BENE_SCORE_HEAD_HDAT");
            $db->query($query);

            $query  = " DELETE FROM MOCK_CSV_BENE_SCORE_HEAD_DAT ";
            $query .= " WHERE ";
            $query .= "     YEAR = '{$data_arr["KYEAR"]}' ";
            $query .= "     AND KYOUZAICD = '{$data_arr["KKYOUZAICD"]}' ";
            $db->query($query);

            for ($sCnt = 1; $sCnt <= $model->scoreCnt; $sCnt++) {
                $scoreData = array();
                $scoreData["YEAR"][TEXT]                 = $data_arr["KYEAR"];
                $scoreData["KYOUZAICD"][TEXT]            = $data_arr["KKYOUZAICD"];
                $scoreData["SEQ"][TEXT]                  = $data_arr["SEQ{$sCnt}"];
                $scoreData["TYEAR"][TEXT]                = $data_arr["YEAR"];
                $scoreData["TKYOUZAI"][TEXT]             = $data_arr["KYOUZAICD"];
                $scoreData["TSCORE"][TEXT]               = $data_arr["SCORE{$sCnt}"];
                $scoreData["TGTZ"][TEXT]                 = $data_arr["GTZ{$sCnt}"];
                $scoreData["TALL_DEV"][TEXT]             = $data_arr["ALL_DEV{$sCnt}"];
                $scoreData["TSCHOOL_DEV"][TEXT]          = $data_arr["SCHOOL_DEV{$sCnt}"];
                $scoreData["TALL_RANK"][TEXT]            = $data_arr["ALL_RANK{$sCnt}"];
                $scoreData["TSCHOOL_RANK"][TEXT]         = $data_arr["SCHOOL_RANK{$sCnt}"];
                $scoreData["REGISTERCD"][TEXT]           = STAFFCD;
                $scoreData["UPDATED"][NUMBER]            = "SYSDATE()";

                $query = Query::insertSQL($scoreData, "MOCK_CSV_BENE_SCORE_HEAD_DAT");
                $db->query($query);

            }
        } else if ($model->field["COMPANY"] == "00000002" && $model->field["CSV_DIV"] == "2") {

            $query  = " DELETE FROM MOCK_CSV_BENE_HOPE_HEAD_HDAT ";
            $query .= " WHERE ";
            $query .= "     YEAR = '{$data_arr["KYEAR"]}' ";
            $query .= "     AND KYOUZAICD = '{$data_arr["KKYOUZAICD"]}' ";
            $db->query($query);

            $hdata = array();
            $hdata["YEAR"][TEXT]               = $data_arr["KYEAR"];
            $hdata["KYOUZAICD"][TEXT]          = $data_arr["KKYOUZAICD"];
            $hdata["MOCKCD"][TEXT]             = $data_arr["MOCKCD"];
            $hdata["TYEAR"][TEXT]              = $data_arr["YEAR"];
            $hdata["TKYOUZAI"][TEXT]           = $data_arr["KYOUZAICD"];
            $hdata["TTYPE"][TEXT]              = $data_arr["TYPE"];
            $hdata["TKYOUZAINAME"][TEXT]       = $data_arr["KYOUZAINAME"];
            $hdata["TGAKKACD"][TEXT]           = $data_arr["GAKKACD"];
            $hdata["TGAKKANAME"][TEXT]         = $data_arr["GAKKANAME"];
            $hdata["TBENEID"][TEXT]            = $data_arr["BENEID"];
            $hdata["THR_CLASS"][TEXT]          = $data_arr["HR_CLASS"];
            $hdata["TATTENDNO"][TEXT]          = $data_arr["ATTENDNO"];
            $hdata["TNAME"][TEXT]              = $data_arr["NAME"];
            $hdata["TBUNRI_DIV"][TEXT]         = $data_arr["BUNRI_DIV"];
            $hdata["TBIRTHDAY"][TEXT]          = $data_arr["BIRTHDAY"];
            $hdata["TSEX"][TEXT]               = $data_arr["SEX"];
            $hdata["REGISTERCD"][TEXT]         = STAFFCD;
            $hdata["UPDATED"][NUMBER]          = "SYSDATE()";

            $query = Query::insertSQL($hdata, "MOCK_CSV_BENE_HOPE_HEAD_HDAT");
            $db->query($query);

            $query  = " DELETE FROM MOCK_CSV_BENE_HOPE_HEAD_DAT ";
            $query .= " WHERE ";
            $query .= "     YEAR = '{$data_arr["KYEAR"]}' ";
            $query .= "     AND KYOUZAICD = '{$data_arr["KKYOUZAICD"]}' ";
            $db->query($query);

            for ($hCnt = 1; $hCnt <= $model->hopeCnt; $hCnt++) {
                $hopeData = array();
                $hopeData["YEAR"][TEXT]              = $data_arr["KYEAR"];
                $hopeData["KYOUZAICD"][TEXT]         = $data_arr["KKYOUZAICD"];
                $hopeData["SEQ"][TEXT]               = $data_arr["SEQ{$hCnt}"];
                $hopeData["TYEAR"][TEXT]             = $data_arr["YEAR"];
                $hopeData["TKYOUZAI"][TEXT]          = $data_arr["KYOUZAICD"];
                $hopeData["TSCHOOL_CD"][TEXT]        = $data_arr["SCHOOL_CD{$hCnt}"];
                $hopeData["TBOSYUTANNI"][TEXT]       = $data_arr["BOSYUTANNI{$hCnt}"];
                $hopeData["TSCHOOL_NAME"][TEXT]      = $data_arr["SCHOOL_NAME{$hCnt}"];
                $hopeData["TGAKUBU_NAME"][TEXT]      = $data_arr["GAKUBU_NAME{$hCnt}"];
                $hopeData["TGAKKA_NAME"][TEXT]       = $data_arr["GAKKA_NAME{$hCnt}"];
                $hopeData["TNITTEI"][TEXT]           = $data_arr["NITTEI{$hCnt}"];
                $hopeData["THOUSIKI"][TEXT]          = $data_arr["HOUSIKI{$hCnt}"];
                $hopeData["TALL_JUDGE"][TEXT]        = $data_arr["ALL_JUDGE{$hCnt}"];
                $hopeData["TJUDGE1"][TEXT]           = $data_arr["JUDGE1{$hCnt}"];
                $hopeData["TJUDGE2"][TEXT]           = $data_arr["JUDGE2{$hCnt}"];
                $hopeData["TALL_JUDGE_SS"][TEXT]     = $data_arr["ALL_JUDGE_SS{$hCnt}"];
                $hopeData["TJUDGE1_SS"][TEXT]        = $data_arr["JUDGE1_SS{$hCnt}"];
                $hopeData["TJUDGE2_SS"][TEXT]        = $data_arr["JUDGE2_SS{$hCnt}"];
                $hopeData["REGISTERCD"][TEXT]        = STAFFCD;
                $hopeData["UPDATED"][NUMBER]         = "SYSDATE()";

                $query = Query::insertSQL($hopeData, "MOCK_CSV_BENE_HOPE_HEAD_DAT");
                $db->query($query);
            }
        } else if ($model->field["COMPANY"] == "00000003") {

            $query  = " DELETE FROM MOCK_CSV_ZKAI_HEAD_HDAT ";
            $query .= " WHERE ";
            $query .= "     YEAR = '{$data_arr["KYEAR"]}' ";
            $query .= "     AND MOSI_CD = '{$data_arr["KMOSI_CD"]}' ";
            $db->query($query);

            $hdata = array();
            $hdata["YEAR"][TEXT]            = $data_arr["KYEAR"];
            $hdata["MOSI_CD"][TEXT]         = $data_arr["KMOSI_CD"];
            $hdata["MOCKCD"][TEXT]          = $data_arr["MOCKCD"];
            $hdata["TYEAR"][TEXT]           = $data_arr["YEAR"];
            $hdata["TMOSI_CD"][TEXT]        = $data_arr["MOSI_CD"];
            $hdata["TMOSI_NAME"][TEXT]      = $data_arr["MOSI_NAME"];
            $hdata["THR_CLASS"][TEXT]       = $data_arr["HR_CLASS"];
            $hdata["TATTENDNO"][TEXT]       = $data_arr["ATTENDNO"];
            $hdata["TKANA"][TEXT]           = $data_arr["KANA"];
            $hdata["TDEVIATION"][TEXT]      = $data_arr["DEVIATION"];
            $hdata["TRANK"][TEXT]           = $data_arr["RANK"];
            $hdata["TCNT"][TEXT]            = $data_arr["CNT"];
            $hdata["REGISTERCD"][TEXT]      = STAFFCD;
            $hdata["UPDATED"][NUMBER]       = "SYSDATE()";

            $query = Query::insertSQL($hdata, "MOCK_CSV_ZKAI_HEAD_HDAT");
            $db->query($query);

            $query  = " DELETE FROM MOCK_CSV_ZKAI_SCORE_HEAD_DAT ";
            $query .= " WHERE ";
            $query .= "     YEAR = '{$data_arr["KYEAR"]}' ";
            $query .= "     AND MOSI_CD = '{$data_arr["KMOSI_CD"]}' ";
            $db->query($query);

            for ($sCnt = 1; $sCnt <= $model->scoreCnt; $sCnt++) {
                $scoreData = array();
                $scoreData["YEAR"][TEXT]              = $data_arr["KYEAR"];
                $scoreData["MOSI_CD"][TEXT]           = $data_arr["KMOSI_CD"];
                $scoreData["SEQ"][TEXT]               = $data_arr["SEQ{$sCnt}"];
                $scoreData["TYEAR"][TEXT]             = $data_arr["YEAR"];
                $scoreData["TMOSI_CD"][TEXT]          = $data_arr["MOSI_CD"];
                $scoreData["TSCORE"][TEXT]            = $data_arr["SCORE{$sCnt}"];
                $scoreData["TDEVIATION"][TEXT]        = $data_arr["DEVIATION{$sCnt}"];
                $scoreData["REGISTERCD"][TEXT]        = STAFFCD;
                $scoreData["UPDATED"][NUMBER]         = "SYSDATE()";

                $query = Query::insertSQL($scoreData, "MOCK_CSV_ZKAI_SCORE_HEAD_DAT");
                $db->query($query);
            }

            $query  = " DELETE FROM MOCK_CSV_ZKAI_HOPE_HEAD_DAT ";
            $query .= " WHERE ";
            $query .= "         YEAR    = '{$data_arr["KYEAR"]}' ";
            $query .= "     AND MOSI_CD = '{$data_arr["KMOSI_CD"]}' ";
            $db->query($query);

            for ($hCnt = 1; $hCnt <= $model->hopeCnt; $hCnt++) {
                $hopeData = array();
                $hopeData["YEAR"][TEXT]              = $data_arr["KYEAR"];
                $hopeData["MOSI_CD"][TEXT]           = $data_arr["KMOSI_CD"];
                $hopeData["SEQ"][TEXT]               = sprintf("%03d", $data_arr["SEQ{$hCnt}"]);
                $hopeData["TYEAR"][TEXT]             = $data_arr["YEAR"];
                $hopeData["TMOSI_CD"][TEXT]          = $data_arr["MOSI_CD"];
                $hopeData["TSCHOOL_NAME"][TEXT]      = $data_arr["SCHOOL_NAME{$hCnt}"];
                $hopeData["TSCHOOL_CD"][TEXT]        = $data_arr["SCHOOL_CD{$hCnt}"];
                $hopeData["TJUDGE_HYOUKA"][TEXT]     = $data_arr["JUDGE_HYOUKA{$hCnt}"];
                $hopeData["TRANK"][TEXT]             = $data_arr["TRANK{$hCnt}"];
                $hopeData["TJUDGE_SUUTI"][TEXT]      = $data_arr["JUDGE_SUUTI{$hCnt}"];
                $hopeData["REGISTERCD"][TEXT]        = STAFFCD;
                $hopeData["UPDATED"][NUMBER]         = "SYSDATE()";

                $query = Query::insertSQL($hopeData, "MOCK_CSV_ZKAI_HOPE_HEAD_DAT");
                $db->query($query);
            }
        } else if ($model->field["COMPANY"] == "00000004") {

            $query  = " DELETE FROM MOCK_CSV_KAWAI_HEAD_HDAT ";
            $query .= " WHERE ";
            $query .= "     YEAR = '{$data_arr["KYEAR"]}' ";
            $query .= "     AND MOSI_CD = '{$data_arr["KMOSI_CD"]}' ";
            $db->query($query);

            $hdata = array();
            $hdata["YEAR"][TEXT]            = $data_arr["KYEAR"];
            $hdata["MOSI_CD"][TEXT]         = $data_arr["KMOSI_CD"];
            $hdata["MOCKCD"][TEXT]          = $model->field["MOCKCD"];
            $hdata["TYOUSHI_NO"][TEXT]      = $data_arr["YOUSHI_NO"];
            $hdata["TYEAR"][TEXT]           = $data_arr["YEAR"];
            $hdata["TMOSI_CD"][TEXT]        = $data_arr["MOSI_CD"];
            $hdata["TSCHOOL_CD"][TEXT]      = $data_arr["SCHOOL_CD"];
            $hdata["TGRADE"][TEXT]          = $data_arr["GRADE"];
            $hdata["THR_CLASS"][TEXT]       = $data_arr["HR_CLASS"];
            $hdata["TATTENDNO"][TEXT]       = $data_arr["ATTENDNO"];
            $hdata["TKANA"][TEXT]           = $data_arr["KANA"];
            $hdata["TEXAM_TYPE"][TEXT]      = $data_arr["EXAM_TYPE"];
            $hdata["TBUN_RI_CD"][TEXT]      = $data_arr["BUN_RI_CD"];
            $hdata["TREMARK_A"][TEXT]       = $data_arr["REMARK_A"];
            $hdata["TREMARK_B"][TEXT]       = $data_arr["REMARK_B"];
            $hdata["TREMARK_C"][TEXT]       = $data_arr["REMARK_C"];
            $hdata["TREMARK_D"][TEXT]       = $data_arr["REMARK_D"];
            $hdata["TREMARK_E"][TEXT]       = $data_arr["REMARK_E"];
            $hdata["TREMARK_F"][TEXT]       = $data_arr["REMARK_F"];
            $hdata["TREMARK_G"][TEXT]       = $data_arr["REMARK_G"];
            $hdata["TREMARK_H"][TEXT]       = $data_arr["REMARK_H"];
            $hdata["TREMARK_I"][TEXT]       = $data_arr["REMARK_I"];
            $hdata["REGISTERCD"][TEXT]      = STAFFCD;
            $hdata["UPDATED"][NUMBER]       = "SYSDATE()";

            $query = Query::insertSQL($hdata, "MOCK_CSV_KAWAI_HEAD_HDAT");
            $db->query($query);

            $query  = " DELETE FROM MOCK_CSV_KAWAI_SCORE_HEAD_DAT ";
            $query .= " WHERE ";
            $query .= "     YEAR = '{$data_arr["KYEAR"]}' ";
            $query .= "     AND MOSI_CD = '{$data_arr["KMOSI_CD"]}' ";
            $db->query($query);

            for ($sCnt = 1; $sCnt <= $model->scoreCnt; $sCnt++) {
                $scoreData = array();
                $scoreData["YEAR"][TEXT]              = $data_arr["KYEAR"];
                $scoreData["MOSI_CD"][TEXT]           = $data_arr["KMOSI_CD"];
                $scoreData["SEQ"][TEXT]               = sprintf("%03d", $data_arr["SEQ{$sCnt}"]);
                $scoreData["TYEAR"][TEXT]             = $data_arr["YEAR"];
                $scoreData["TMOSI_CD"][TEXT]          = $data_arr["MOSI_CD"];
                $scoreData["TKAMOKU_CD"][TEXT]        = $data_arr["TKAMOKU_CD{$sCnt}"];
                $scoreData["TKAMOKU_NAME"][TEXT]      = $data_arr["TKAMOKU_NAME{$sCnt}"];
                $scoreData["THAITEN"][TEXT]           = $data_arr["THAITEN{$sCnt}"];
                $scoreData["TSCORE"][TEXT]            = $data_arr["TSCORE{$sCnt}"];
                $scoreData["TDEVIATION"][TEXT]        = $data_arr["TDEVIATION{$sCnt}"];
                $scoreData["TLEVEL"][TEXT]            = $data_arr["TLEVEL{$sCnt}"];
                $scoreData["REGISTERCD"][TEXT]        = STAFFCD;
                $scoreData["UPDATED"][NUMBER]         = "SYSDATE()";

                $query = Query::insertSQL($scoreData, "MOCK_CSV_KAWAI_SCORE_HEAD_DAT");
                $db->query($query);
            }

            $query  = " DELETE FROM MOCK_CSV_KAWAI_HOPE_HEAD_DAT ";
            $query .= " WHERE ";
            $query .= "         YEAR    = '{$data_arr["KYEAR"]}' ";
            $query .= "     AND MOSI_CD = '{$data_arr["KMOSI_CD"]}' ";
            $db->query($query);

            for ($hCnt = 1; $hCnt <= $model->hopeCnt; $hCnt++) {
                $hopeData = array();
                $hopeData["YEAR"][TEXT]              = $data_arr["KYEAR"];
                $hopeData["MOSI_CD"][TEXT]           = $data_arr["KMOSI_CD"];
                $hopeData["SEQ"][TEXT]               = sprintf("%03d", $data_arr["SEQ{$hCnt}"]);
                $hopeData["TYEAR"][TEXT]             = $data_arr["YEAR"];
                $hopeData["TMOSI_CD"][TEXT]          = $data_arr["MOSI_CD"];
                $hopeData["TSCHOOL_CD_5"][TEXT]      = $data_arr["TSCHOOL_CD_5{$hCnt}"];
                $hopeData["TSCHOOL_CD_10"][TEXT]     = $data_arr["TSCHOOL_CD_10{$hCnt}"];
                $hopeData["TSCHOOL_NAME"][TEXT]      = $data_arr["TSCHOOL_NAME{$hCnt}"];
                $hopeData["THYOUKA_SEISEKI"][TEXT]   = $data_arr["THYOUKA_SEISEKI{$hCnt}"];
                $hopeData["TSE_HYOUKA"][TEXT]        = $data_arr["TSE_HYOUKA{$hCnt}"];
                $hopeData["TNI_HYOUKA"][TEXT]        = $data_arr["TNI_HYOUKA{$hCnt}"];
                $hopeData["TSOUHYOU_PO"][TEXT]       = $data_arr["TSOUHYOU_PO{$hCnt}"];
                $hopeData["TSOU_HYOUKA"][TEXT]       = $data_arr["TSOU_HYOUKA{$hCnt}"];
                $hopeData["REGISTERCD"][TEXT]        = STAFFCD;
                $hopeData["UPDATED"][NUMBER]         = "SYSDATE()";

                $query = Query::insertSQL($hopeData, "MOCK_CSV_KAWAI_HOPE_HEAD_DAT");
                $db->query($query);
            }
        }

        $db->commit();
        Query::dbCheckIn($db);
    }

    //マスタの追加（ＣＳＶデータより読込）
    function insertQueryCsv($model, $data_arr)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $query  = " DELETE FROM MOCK_RANK_RANGE_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$model->field["YEAR"]}' ";
        $query .= "     AND MOCKCD = '{$model->field["MOCKCD"]}' ";
        $query .= "     AND SCHREGNO IN ( ";
        $query .= "                     SELECT ";
        $query .= "                         SCHREGNO ";
        $query .= "                     FROM ";
        $query .= "                         SCHREG_REGD_DAT T1 ";
        $query .= "                     WHERE ";
        $query .= "                         T1.YEAR = '{$model->field["YEAR"]}' ";
        $query .= "                         AND T1.GRADE = '{$model->field["GRADE"]}' ";
        $query .= "                     ) ";

        $db->query($query);

        $cnt = 0;   //処理件数
        for ($i = 0; $i < get_count($data_arr); $i++) {
            if ($model->field["COMPANY"] == "00000001") {

                $query  = " DELETE FROM MOCK_CSV_SUNDAI_HDAT ";
                $query .= " WHERE ";
                $query .= "     YEAR = '{$data_arr[$i]["YEAR"]}' ";
                $query .= "     AND MOSI_CD = '{$data_arr[$i]["MOSI_CD"]}' ";
                $query .= "     AND EXAMNO = '{$data_arr[$i]["EXAMNO"]}' ";
                $db->query($query);

                $hdata = array();
                $hdata["YEAR"][TEXT]            = $data_arr[$i]["YEAR"];
                $hdata["MOSI_CD"][TEXT]         = $data_arr[$i]["MOSI_CD"];
                $hdata["MOCKCD"][TEXT]          = $data_arr[$i]["MOCKCD"];
                $hdata["MOSI_NAME"][TEXT]       = $data_arr[$i]["MOSI_NAME"];
                $hdata["HR_CLASS"][TEXT]        = $data_arr[$i]["HR_CLASS"];
                $hdata["ATTENDNO"][TEXT]        = $data_arr[$i]["ATTENDNO"];
                $hdata["EXAMNO"][TEXT]          = $data_arr[$i]["EXAMNO"];
                $hdata["SCHREGNO"][TEXT]        = $data_arr[$i]["SCHREGNO"];
                $hdata["SCHOOL_CD"][TEXT]       = $data_arr[$i]["SCHOOL_CD"];
                $hdata["SCHOOL_EDA"][TEXT]      = $data_arr[$i]["SCHOOL_EDA"];
                $hdata["SCHOOL_NAME"][TEXT]     = $data_arr[$i]["SCHOOL_NAME"];
                $hdata["BUNRI_DIV"][TEXT]       = $data_arr[$i]["BUNRI_DIV"];
                $hdata["SEX"][TEXT]             = $data_arr[$i]["SEX"];
                $hdata["GRADE"][TEXT]           = $data_arr[$i]["GRADE"];
                $hdata["GEN_SOTU"][TEXT]        = $data_arr[$i]["GEN_SOTU"];
                $hdata["KAMOKU_EIGO"][TEXT]     = $data_arr[$i]["KAMOKU_EIGO"];
                $hdata["KAMOKU_SUUGAKU"][TEXT]  = $data_arr[$i]["KAMOKU_SUUGAKU"];
                $hdata["KAMOKU_KOKUGO"][TEXT]   = $data_arr[$i]["KAMOKU_KOKUGO"];
                $hdata["KAMOKU_RI1"][TEXT]      = $data_arr[$i]["KAMOKU_RI1"];
                $hdata["KAMOKU_RI2"][TEXT]      = $data_arr[$i]["KAMOKU_RI2"];
                $hdata["KAMOKU_RI3"][TEXT]      = $data_arr[$i]["KAMOKU_RI3"];
                $hdata["KAMOKU_REKIKOU1"][TEXT] = $data_arr[$i]["KAMOKU_REKIKOU1"];
                $hdata["KAMOKU_REKIKOU2"][TEXT] = $data_arr[$i]["KAMOKU_REKIKOU2"];
                $hdata["RIKA_FIRST"][TEXT]      = $data_arr[$i]["RIKA_FIRST"];
                $hdata["REKIKOU_FIRST"][TEXT]   = $data_arr[$i]["REKIKOU_FIRST"];
                $hdata["DEVIATION"][NUMBER]     = $data_arr[$i]["DEVIATION"];
                $hdata["RANK"][NUMBER]          = $data_arr[$i]["RANK"];
                $hdata["CNT"][NUMBER]           = $data_arr[$i]["CNT"];
                $hdata["REGISTERCD"][TEXT]      = STAFFCD;
                $hdata["UPDATED"][NUMBER]       = "SYSDATE()";

                $query = Query::insertSQL($hdata, "MOCK_CSV_SUNDAI_HDAT");
                $db->query($query);

                $query  = " DELETE FROM MOCK_CSV_SUNDAI_HOPE_DAT ";
                $query .= " WHERE ";
                $query .= "     YEAR = '{$data_arr[$i]["YEAR"]}' ";
                $query .= "     AND MOSI_CD = '{$data_arr[$i]["MOSI_CD"]}' ";
                $query .= "     AND EXAMNO = '{$data_arr[$i]["EXAMNO"]}' ";
                $db->query($query);

                $query  = " DELETE FROM MOCK_CSV_SUNDAI_SCORE_DAT ";
                $query .= " WHERE ";
                $query .= "     YEAR = '{$data_arr[$i]["YEAR"]}' ";
                $query .= "     AND MOSI_CD = '{$data_arr[$i]["MOSI_CD"]}' ";
                $query .= "     AND EXAMNO = '{$data_arr[$i]["EXAMNO"]}' ";
                $db->query($query);

                for ($hCnt = 1; $hCnt <= $model->hopeCnt; $hCnt++) {
                    $hopeData = array();
                    $hopeData["YEAR"][TEXT]             = $data_arr[$i]["YEAR"];
                    $hopeData["MOSI_CD"][TEXT]          = $data_arr[$i]["MOSI_CD"];
                    $hopeData["EXAMNO"][TEXT]           = $data_arr[$i]["EXAMNO"];
                    $hopeData["SEQ"][TEXT]              = sprintf("%03d", $data_arr[$i]["SEQ{$hCnt}"]);
                    $hopeData["SCHOOL_CD"][TEXT]        = $data_arr[$i]["SCHOOL_CD{$hCnt}"];
                    $hopeData["SCHOOL_NAME"][TEXT]      = $data_arr[$i]["SCHOOL_NAME{$hCnt}"];
                    $hopeData["NITTEI"][TEXT]           = $data_arr[$i]["NITTEI{$hCnt}"];
                    $hopeData["RANK"][NUMBER]           = $data_arr[$i]["RANK{$hCnt}"];
                    $hopeData["CNT"][NUMBER]            = $data_arr[$i]["CNT{$hCnt}"];
                    $hopeData["JUDGE_HYOUKA"][TEXT]     = $data_arr[$i]["JUDGE_HYOUKA{$hCnt}"];
                    $hopeData["JUDGE_SUUTI"][NUMBER]    = $data_arr[$i]["JUDGE_SUUTI{$hCnt}"];
                    $hopeData["REGISTERCD"][TEXT]       = STAFFCD;
                    $hopeData["UPDATED"][NUMBER]        = "SYSDATE()";

                    $query = Query::insertSQL($hopeData, "MOCK_CSV_SUNDAI_HOPE_DAT");
                    $db->query($query);
                }
                for ($sCnt = 1; $sCnt <= $model->scoreCnt; $sCnt++) {
                    $scoreData = array();
                    $scoreData["YEAR"][TEXT]             = $data_arr[$i]["YEAR"];
                    $scoreData["MOSI_CD"][TEXT]          = $data_arr[$i]["MOSI_CD"];
                    $scoreData["EXAMNO"][TEXT]           = $data_arr[$i]["EXAMNO"];
                    $scoreData["SEQ"][TEXT]              = $data_arr[$i]["SEQ{$sCnt}"];
                    $scoreData["SCORE"][NUMBER]          = $data_arr[$i]["SCORE{$sCnt}"];
                    $scoreData["DEVIATION"][NUMBER]      = $data_arr[$i]["DEVIATION{$sCnt}"];
                    $scoreData["RANK"][NUMBER]           = $data_arr[$i]["KRANK{$sCnt}"];
                    $scoreData["REGISTERCD"][TEXT]       = STAFFCD;
                    $scoreData["UPDATED"][NUMBER]        = "SYSDATE()";

                    $query = Query::insertSQL($scoreData, "MOCK_CSV_SUNDAI_SCORE_DAT");
                    $db->query($query);
                }
            } else if ($model->field["COMPANY"] == "00000002" && $model->field["CSV_DIV"] == "1") {

                $query  = " DELETE FROM MOCK_CSV_BENE_SCORE_HDAT ";
                $query .= " WHERE ";
                $query .= "     YEAR = '{$data_arr[$i]["YEAR"]}' ";
                $query .= "     AND KYOUZAICD = '{$data_arr[$i]["KYOUZAICD"]}' ";
                $query .= "     AND BENEID = '{$data_arr[$i]["BENEID"]}' ";
                $db->query($query);

                $hdata = array();
                $hdata["YEAR"][TEXT]               = $data_arr[$i]["YEAR"];
                $hdata["KYOUZAICD"][TEXT]          = $data_arr[$i]["KYOUZAICD"];
                $hdata["MOCKCD"][TEXT]             = $data_arr[$i]["MOCKCD"];
                $hdata["TYPE"][TEXT]               = $data_arr[$i]["TYPE"];
                $hdata["KYOUZAINAME"][TEXT]        = $data_arr[$i]["KYOUZAINAME"];
                $hdata["GAKKACD"][TEXT]            = $data_arr[$i]["GAKKACD"];
                $hdata["GAKKANAME"][TEXT]          = $data_arr[$i]["GAKKANAME"];
                $hdata["BENEID"][TEXT]             = $data_arr[$i]["BENEID"];
                $hdata["HR_CLASS"][TEXT]           = $data_arr[$i]["HR_CLASS"];
                $hdata["ATTENDNO"][TEXT]           = $data_arr[$i]["ATTENDNO"];
                $hdata["NAME"][TEXT]               = $data_arr[$i]["NAME"];
                $hdata["BUNRI_DIV"][TEXT]          = $data_arr[$i]["BUNRI_DIV"];
                $hdata["BIRTHDAY"][DATE]           = $data_arr[$i]["BIRTHDAY"];
                $hdata["SEX"][TEXT]                = $data_arr[$i]["SEX"];
                $hdata["DEVIATION"][NUMBER]        = $data_arr[$i]["DEVIATION"];
                $hdata["RANK"][NUMBER]             = $data_arr[$i]["RANK"];
                $hdata["CNT"][NUMBER]              = $data_arr[$i]["CNT"];
                $hdata["REGISTERCD"][TEXT]         = STAFFCD;
                $hdata["UPDATED"][NUMBER]          = "SYSDATE()";

                $query = Query::insertSQL($hdata, "MOCK_CSV_BENE_SCORE_HDAT");
                $db->query($query);

                $query  = " DELETE FROM MOCK_CSV_BENE_SCORE_DAT ";
                $query .= " WHERE ";
                $query .= "     YEAR = '{$data_arr[$i]["YEAR"]}' ";
                $query .= "     AND KYOUZAICD = '{$data_arr[$i]["KYOUZAICD"]}' ";
                $query .= "     AND BENEID = '{$data_arr[$i]["BENEID"]}' ";
                $db->query($query);

                for ($sCnt = 1; $sCnt <= $model->scoreCnt; $sCnt++) {
                    $scoreData = array();
                    $scoreData["YEAR"][TEXT]                = $data_arr[$i]["YEAR"];
                    $scoreData["KYOUZAICD"][TEXT]           = $data_arr[$i]["KYOUZAICD"];
                    $scoreData["BENEID"][TEXT]              = $data_arr[$i]["BENEID"];
                    $scoreData["SEQ"][TEXT]                 = $data_arr[$i]["SEQ{$sCnt}"];
                    $scoreData["SCORE"][NUMBER]             = $data_arr[$i]["SCORE{$sCnt}"];
                    $scoreData["GTZ"][TEXT]                 = $data_arr[$i]["GTZ{$sCnt}"];
                    $scoreData["ALL_DEV"][NUMBER]           = $data_arr[$i]["ALL_DEV{$sCnt}"];
                    $scoreData["SCHOOL_DEV"][NUMBER]        = $data_arr[$i]["SCHOOL_DEV{$sCnt}"];
                    $scoreData["ALL_RANK"][NUMBER]          = $data_arr[$i]["ALL_RANK{$sCnt}"];
                    $scoreData["SCHOOL_RANK"][NUMBER]       = $data_arr[$i]["SCHOOL_RANK{$sCnt}"];
                    $scoreData["REGISTERCD"][TEXT]          = STAFFCD;
                    $scoreData["UPDATED"][NUMBER]           = "SYSDATE()";

                    $query = Query::insertSQL($scoreData, "MOCK_CSV_BENE_SCORE_DAT");
                    $db->query($query);

                }
            } else if ($model->field["COMPANY"] == "00000002" && $model->field["CSV_DIV"] == "2") {

                $query  = " DELETE FROM MOCK_CSV_BENE_HOPE_HDAT ";
                $query .= " WHERE ";
                $query .= "     YEAR = '{$data_arr[$i]["YEAR"]}' ";
                $query .= "     AND KYOUZAICD = '{$data_arr[$i]["KYOUZAICD"]}' ";
                $query .= "     AND BENEID = '{$data_arr[$i]["BENEID"]}' ";
                $db->query($query);

                $hdata = array();
                $hdata["YEAR"][TEXT]               = $data_arr[$i]["YEAR"];
                $hdata["KYOUZAICD"][TEXT]          = $data_arr[$i]["KYOUZAICD"];
                $hdata["MOCKCD"][TEXT]             = $data_arr[$i]["MOCKCD"];
                $hdata["TYPE"][TEXT]               = $data_arr[$i]["TYPE"];
                $hdata["KYOUZAINAME"][TEXT]        = $data_arr[$i]["KYOUZAINAME"];
                $hdata["GAKKACD"][TEXT]            = $data_arr[$i]["GAKKACD"];
                $hdata["GAKKANAME"][TEXT]          = $data_arr[$i]["GAKKANAME"];
                $hdata["BENEID"][TEXT]             = $data_arr[$i]["BENEID"];
                $hdata["SCHREGNO"][TEXT]           = $data_arr[$i]["SCHREGNO"];
                $hdata["HR_CLASS"][TEXT]           = $data_arr[$i]["HR_CLASS"];
                $hdata["ATTENDNO"][TEXT]           = $data_arr[$i]["ATTENDNO"];
                $hdata["NAME"][TEXT]               = $data_arr[$i]["NAME"];
                $hdata["BUNRI_DIV"][TEXT]          = $data_arr[$i]["BUNRI_DIV"];
                $hdata["BIRTHDAY"][DATE]           = $data_arr[$i]["BIRTHDAY"];
                $hdata["SEX"][TEXT]                = $data_arr[$i]["SEX"];
                $hdata["REGISTERCD"][TEXT]         = STAFFCD;
                $hdata["UPDATED"][NUMBER]          = "SYSDATE()";

                $query = Query::insertSQL($hdata, "MOCK_CSV_BENE_HOPE_HDAT");
                $db->query($query);

                $query  = " DELETE FROM MOCK_CSV_BENE_HOPE_DAT ";
                $query .= " WHERE ";
                $query .= "     YEAR = '{$data_arr[$i]["YEAR"]}' ";
                $query .= "     AND KYOUZAICD = '{$data_arr[$i]["KYOUZAICD"]}' ";
                $query .= "     AND BENEID = '{$data_arr[$i]["BENEID"]}' ";
                $db->query($query);

                for ($hCnt = 1; $hCnt <= $model->hopeCnt; $hCnt++) {
                    $hopeData = array();
                    $hopeData["YEAR"][TEXT]             = $data_arr[$i]["YEAR"];
                    $hopeData["KYOUZAICD"][TEXT]        = $data_arr[$i]["KYOUZAICD"];
                    $hopeData["BENEID"][TEXT]           = $data_arr[$i]["BENEID"];
                    $hopeData["SEQ"][TEXT]              = $data_arr[$i]["SEQ{$hCnt}"];
                    $hopeData["SCHOOL_CD"][TEXT]        = $data_arr[$i]["SCHOOL_CD{$hCnt}"];
                    $hopeData["BOSYUTANNI"][TEXT]       = $data_arr[$i]["BOSYUTANNI{$hCnt}"];
                    $hopeData["SCHOOL_NAME"][TEXT]      = $data_arr[$i]["SCHOOL_NAME{$hCnt}"];
                    $hopeData["GAKUBU_NAME"][TEXT]      = $data_arr[$i]["GAKUBU_NAME{$hCnt}"];
                    $hopeData["GAKKA_NAME"][TEXT]       = $data_arr[$i]["GAKKA_NAME{$hCnt}"];
                    $hopeData["NITTEI"][TEXT]           = $data_arr[$i]["NITTEI{$hCnt}"];
                    $hopeData["HOUSIKI"][TEXT]          = $data_arr[$i]["HOUSIKI{$hCnt}"];
                    $hopeData["ALL_JUDGE"][TEXT]        = $data_arr[$i]["ALL_JUDGE{$hCnt}"];
                    $hopeData["JUDGE1"][TEXT]           = $data_arr[$i]["JUDGE1{$hCnt}"];
                    $hopeData["JUDGE2"][TEXT]           = $data_arr[$i]["JUDGE2{$hCnt}"];
                    $hopeData["ALL_JUDGE_SS"][TEXT]     = $data_arr[$i]["ALL_JUDGE_SS{$hCnt}"];
                    $hopeData["JUDGE1_SS"][TEXT]        = $data_arr[$i]["JUDGE1_SS{$hCnt}"];
                    $hopeData["JUDGE2_SS"][TEXT]        = $data_arr[$i]["JUDGE2_SS{$hCnt}"];
                    $hopeData["REGISTERCD"][TEXT]       = STAFFCD;
                    $hopeData["UPDATED"][NUMBER]        = "SYSDATE()";

                    if($hopeData["SCHOOL_CD"][TEXT] != ""){
                        $query = Query::insertSQL($hopeData, "MOCK_CSV_BENE_HOPE_DAT");
                        $db->query($query);
                    }
                }
            } else if ($model->field["COMPANY"] == "00000003") {

                $query  = " DELETE FROM MOCK_CSV_ZKAI_HDAT ";
                $query .= " WHERE ";
                $query .= "     YEAR = '{$data_arr[$i]["YEAR"]}' ";
                $query .= "     AND MOSI_CD = '{$data_arr[$i]["MOSI_CD"]}' ";
                $query .= "     AND HR_CLASS = '".sprintf("%03d", $data_arr[$i]["HR_CLASS"])."' ";
                $query .= "     AND ATTENDNO = '".sprintf("%03d", $data_arr[$i]["ATTENDNO"])."' ";
                $db->query($query);

                $hdata = array();
                $hdata["YEAR"][TEXT]            = $data_arr[$i]["YEAR"];
                $hdata["MOSI_CD"][TEXT]         = $data_arr[$i]["MOSI_CD"];
                $hdata["MOCKCD"][TEXT]          = $data_arr[$i]["MOCKCD"];
                $hdata["MOSI_NAME"][TEXT]       = $data_arr[$i]["MOSI_NAME"];
                $hdata["HR_CLASS"][TEXT]        = sprintf("%03d", $data_arr[$i]["HR_CLASS"]);
                $hdata["ATTENDNO"][TEXT]        = sprintf("%03d", $data_arr[$i]["ATTENDNO"]);
                $hdata["KANA"][TEXT]            = $data_arr[$i]["KANA"];
                $hdata["DEVIATION"][NUMBER]     = $data_arr[$i]["DEVIATION"];
                $hdata["RANK"][NUMBER]          = $data_arr[$i]["RANK"];
                $hdata["CNT"][NUMBER]           = $data_arr[$i]["CNT"];
                $hdata["REGISTERCD"][TEXT]      = STAFFCD;
                $hdata["UPDATED"][NUMBER]       = "SYSDATE()";

                $query = Query::insertSQL($hdata, "MOCK_CSV_ZKAI_HDAT");
                $db->query($query);

                $query  = " DELETE FROM MOCK_CSV_ZKAI_SCORE_DAT ";
                $query .= " WHERE ";
                $query .= "     YEAR = '{$data_arr[$i]["YEAR"]}' ";
                $query .= "     AND MOSI_CD = '{$data_arr[$i]["MOSI_CD"]}' ";
                $query .= "     AND HR_CLASS = '".sprintf("%03d", $data_arr[$i]["HR_CLASS"])."' ";
                $query .= "     AND ATTENDNO = '".sprintf("%03d", $data_arr[$i]["ATTENDNO"])."' ";
                $db->query($query);

                for ($sCnt = 1; $sCnt <= $model->scoreCnt; $sCnt++) {
                    $scoreData = array();
                    $scoreData["YEAR"][TEXT]             = $data_arr[$i]["YEAR"];
                    $scoreData["MOSI_CD"][TEXT]          = $data_arr[$i]["MOSI_CD"];
                    $scoreData["HR_CLASS"][TEXT]         = sprintf("%03d", $data_arr[$i]["HR_CLASS"]);
                    $scoreData["ATTENDNO"][TEXT]         = sprintf("%03d", $data_arr[$i]["ATTENDNO"]);
                    $scoreData["SEQ"][TEXT]              = $data_arr[$i]["SEQ{$sCnt}"];
                    $scoreData["SCORE"][NUMBER]          = $data_arr[$i]["SCORE{$sCnt}"];
                    $scoreData["DEVIATION"][NUMBER]      = $data_arr[$i]["DEVIATION{$sCnt}"];
                    $scoreData["REGISTERCD"][TEXT]       = STAFFCD;
                    $scoreData["UPDATED"][NUMBER]        = "SYSDATE()";

                    $query = Query::insertSQL($scoreData, "MOCK_CSV_ZKAI_SCORE_DAT");
                    $db->query($query);
                }

                $query  = " DELETE FROM MOCK_CSV_ZKAI_HOPE_DAT ";
                $query .= " WHERE ";
                $query .= "         YEAR     = '{$data_arr[$i]["YEAR"]}' ";
                $query .= "     AND MOSI_CD  = '{$data_arr[$i]["MOSI_CD"]}' ";
                $query .= "     AND HR_CLASS = '".sprintf("%03d", $data_arr[$i]["HR_CLASS"])."' ";
                $query .= "     AND ATTENDNO = '".sprintf("%03d", $data_arr[$i]["ATTENDNO"])."' ";
                $db->query($query);

                for ($hCnt = 1; $hCnt <= $model->hopeCnt; $hCnt++) {
                    $hopeData = array();
                    $hopeData["YEAR"][TEXT]             = $data_arr[$i]["YEAR"];
                    $hopeData["MOSI_CD"][TEXT]          = $data_arr[$i]["MOSI_CD"];
                    $hopeData["HR_CLASS"][TEXT]         = sprintf("%03d", $data_arr[$i]["HR_CLASS"]);
                    $hopeData["ATTENDNO"][TEXT]         = sprintf("%03d", $data_arr[$i]["ATTENDNO"]);
                    $hopeData["SEQ"][TEXT]              = sprintf("%03d", $data_arr[$i]["SEQ{$hCnt}"]);
                    $hopeData["SCHOOL_NAME"][TEXT]      = $data_arr[$i]["SCHOOL_NAME{$hCnt}"];
                    $hopeData["SCHOOL_CD"][TEXT]        = $data_arr[$i]["SCHOOL_CD{$hCnt}"];
                    $hopeData["JUDGE_HYOUKA"][TEXT]     = $data_arr[$i]["JUDGE_HYOUKA{$hCnt}"];
                    $hopeData["RANK"][NUMBER]           = $data_arr[$i]["RANK{$hCnt}"];
                    $hopeData["JUDGE_SUUTI"][NUMBER]    = $data_arr[$i]["JUDGE_SUUTI{$hCnt}"];
                    $hopeData["REGISTERCD"][TEXT]       = STAFFCD;
                    $hopeData["UPDATED"][NUMBER]        = "SYSDATE()";

                    $query = Query::insertSQL($hopeData, "MOCK_CSV_ZKAI_HOPE_DAT");
                    $db->query($query);
                }
            } else if ($model->field["COMPANY"] == "00000004") {

                $query  = " DELETE FROM MOCK_CSV_KAWAI_HDAT ";
                $query .= " WHERE ";
                $query .= "     YEAR = '{$data_arr[$i]["YEAR"]}' ";
                $query .= "     AND MOSI_CD = '{$data_arr[$i]["MOSI_CD"]}' ";
                $query .= "     AND HR_CLASS = '".sprintf("%03d", $data_arr[$i]["HR_CLASS"])."' ";
                $query .= "     AND ATTENDNO = '".sprintf("%03d", $data_arr[$i]["ATTENDNO"])."' ";
                $db->query($query);

                $hdata = array();
                $hdata["YOUSHI_NO"][TEXT]       = $data_arr[$i]["YOUSHI_NO"];
                $hdata["YEAR"][TEXT]            = $data_arr[$i]["YEAR"];
                $hdata["MOSI_CD"][TEXT]         = $data_arr[$i]["MOSI_CD"];
                $hdata["MOCKCD"][TEXT]          = $data_arr[$i]["MOCKCD"];
                $hdata["SCHOOL_CD"][TEXT]       = $data_arr[$i]["SCHOOL_CD"];
                $hdata["GRADE"][TEXT]           = $data_arr[$i]["GRADE"];
                $hdata["HR_CLASS"][TEXT]        = sprintf("%03d", $data_arr[$i]["HR_CLASS"]);
                $hdata["ATTENDNO"][TEXT]        = sprintf("%03d", $data_arr[$i]["ATTENDNO"]);
                $hdata["SCHREGNO"][TEXT]        = $data_arr[$i]["SCHREGNO"];
                $hdata["KANA"][TEXT]            = $data_arr[$i]["KANA"];
                $hdata["EXAM_TYPE"][TEXT]       = $data_arr[$i]["EXAM_TYPE"];
                $hdata["BUN_RI_CD"][TEXT]       = $data_arr[$i]["BUN_RI_CD"];
                $hdata["REMARK_A"][TEXT]        = $data_arr[$i]["REMARK_A"];
                $hdata["REMARK_B"][TEXT]        = $data_arr[$i]["REMARK_B"];
                $hdata["REMARK_C"][TEXT]        = $data_arr[$i]["REMARK_C"];
                $hdata["REMARK_D"][TEXT]        = $data_arr[$i]["REMARK_D"];
                $hdata["REMARK_E"][TEXT]        = $data_arr[$i]["REMARK_E"];
                $hdata["REMARK_F"][TEXT]        = $data_arr[$i]["REMARK_F"];
                $hdata["REMARK_G"][TEXT]        = $data_arr[$i]["REMARK_G"];
                $hdata["REMARK_H"][TEXT]        = $data_arr[$i]["REMARK_H"];
                $hdata["REMARK_I"][TEXT]        = $data_arr[$i]["REMARK_I"];
                $hdata["REGISTERCD"][TEXT]      = STAFFCD;
                $hdata["UPDATED"][NUMBER]       = "SYSDATE()";

                $query = Query::insertSQL($hdata, "MOCK_CSV_KAWAI_HDAT");
                $db->query($query);

                $query  = " DELETE FROM MOCK_CSV_KAWAI_SCORE_DAT ";
                $query .= " WHERE ";
                $query .= "     YEAR = '{$data_arr[$i]["YEAR"]}' ";
                $query .= "     AND MOSI_CD = '{$data_arr[$i]["MOSI_CD"]}' ";
                $query .= "     AND HR_CLASS = '".sprintf("%03d", $data_arr[$i]["HR_CLASS"])."' ";
                $query .= "     AND ATTENDNO = '".sprintf("%03d", $data_arr[$i]["ATTENDNO"])."' ";
                $db->query($query);

                for ($sCnt = 1; $sCnt <= $model->scoreCnt; $sCnt++) {
                    $scoreData = array();
                    $scoreData["YEAR"][TEXT]             = $data_arr[$i]["YEAR"];
                    $scoreData["MOSI_CD"][TEXT]          = $data_arr[$i]["MOSI_CD"];
                    $scoreData["GRADE"][TEXT]            = $data_arr[$i]["GRADE"];
                    $scoreData["HR_CLASS"][TEXT]         = sprintf("%03d", $data_arr[$i]["HR_CLASS"]);
                    $scoreData["ATTENDNO"][TEXT]         = sprintf("%03d", $data_arr[$i]["ATTENDNO"]);
                    $scoreData["SEQ"][TEXT]              = $data_arr[$i]["SEQ{$sCnt}"];
                    $scoreData["MOCK_SUBCLASS_CD"][TEXT] = $data_arr[$i]["MOCK_SUBCLASS_CD{$sCnt}"];
                    $scoreData["KAMOKU_CD"][TEXT]        = $data_arr[$i]["KAMOKU_CD{$sCnt}"];
                    $scoreData["KAMOKU_NAME"][TEXT]      = $data_arr[$i]["KAMOKU_NAME{$sCnt}"];
                    $scoreData["HAITEN"][TEXT]           = $data_arr[$i]["HAITEN{$sCnt}"];
                    $scoreData["SCORE"][NUMBER]          = $data_arr[$i]["SCORE{$sCnt}"];
                    $scoreData["DEVIATION"][NUMBER]      = $data_arr[$i]["DEVIATION{$sCnt}"];
                    $scoreData["LEVEL"][TEXT]            = $data_arr[$i]["LEVEL{$sCnt}"];
                    $scoreData["REGISTERCD"][TEXT]       = STAFFCD;
                    $scoreData["UPDATED"][NUMBER]        = "SYSDATE()";

                    $query = Query::insertSQL($scoreData, "MOCK_CSV_KAWAI_SCORE_DAT");
                    $db->query($query);
                }

                $query  = " DELETE FROM MOCK_CSV_KAWAI_HOPE_DAT ";
                $query .= " WHERE ";
                $query .= "         YEAR     = '{$data_arr[$i]["YEAR"]}' ";
                $query .= "     AND MOSI_CD  = '{$data_arr[$i]["MOSI_CD"]}' ";
                $query .= "     AND HR_CLASS = '".sprintf("%03d", $data_arr[$i]["HR_CLASS"])."' ";
                $query .= "     AND ATTENDNO = '".sprintf("%03d", $data_arr[$i]["ATTENDNO"])."' ";
                $db->query($query);

                for ($hCnt = 1; $hCnt <= $model->hopeCnt; $hCnt++) {
                    $hopeData = array();
                    $hopeData["YEAR"][TEXT]             = $data_arr[$i]["YEAR"];
                    $hopeData["MOSI_CD"][TEXT]          = $data_arr[$i]["MOSI_CD"];
                    $hopeData["GRADE"][TEXT]            = $data_arr[$i]["GRADE"];
                    $hopeData["HR_CLASS"][TEXT]         = sprintf("%03d", $data_arr[$i]["HR_CLASS"]);
                    $hopeData["ATTENDNO"][TEXT]         = sprintf("%03d", $data_arr[$i]["ATTENDNO"]);
                    $hopeData["SEQ"][TEXT]              = sprintf("%03d", $data_arr[$i]["SEQ{$hCnt}"]);
                    $hopeData["SCHOOL_CD_5"][TEXT]      = $data_arr[$i]["SCHOOL_CD_5{$hCnt}"];
                    $hopeData["SCHOOL_CD_10"][TEXT]     = $data_arr[$i]["SCHOOL_CD_10{$hCnt}"];
                    $hopeData["SCHOOL_NAME"][TEXT]      = $data_arr[$i]["SCHOOL_NAME{$hCnt}"];
                    $hopeData["HYOUKA_SEISEKI"][TEXT]   = $data_arr[$i]["HYOUKA_SEISEKI{$hCnt}"];
                    $hopeData["SE_HYOUKA"][TEXT]        = $data_arr[$i]["SE_HYOUKA{$hCnt}"];
                    $hopeData["NI_HYOUKA"][TEXT]        = $data_arr[$i]["NI_HYOUKA{$hCnt}"];
                    $hopeData["SOUHYOU_PO"][TEXT]       = $data_arr[$i]["SOUHYOU_PO{$hCnt}"];
                    $hopeData["SOU_HYOUKA"][TEXT]       = $data_arr[$i]["SOU_HYOUKA{$hCnt}"];
                    $hopeData["REGISTERCD"][TEXT]       = STAFFCD;
                    $hopeData["UPDATED"][NUMBER]        = "SYSDATE()";

                    $query = Query::insertSQL($hopeData, "MOCK_CSV_KAWAI_HOPE_DAT");
                    $db->query($query);
                }
            }
            $cnt++;
        }
        $db->commit();
        Query::dbCheckIn($db);

        return $cnt;

    }

    //削除（ＣＳＶデータより読込）
    function DeleteQueryCsv($model, &$data_arr)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $cnt = 0;   //処理件数
        for ($i = 0; $i < get_count($data_arr); $i++) {
            if ($model->field["COMPANY"] == "00000001") {

                $query  = " DELETE FROM MOCK_CSV_SUNDAI_HDAT ";
                $query .= " WHERE ";
                $query .= "     YEAR = '{$data_arr[$i]["YEAR"]}' ";
                $query .= "     AND MOSI_CD = '{$data_arr[$i]["MOSI_CD"]}' ";
                $query .= "     AND EXAMNO = '{$data_arr[$i]["EXAMNO"]}' ";
                $db->query($query);

                $query  = " DELETE FROM MOCK_CSV_SUNDAI_HOPE_DAT ";
                $query .= " WHERE ";
                $query .= "     YEAR = '{$data_arr[$i]["YEAR"]}' ";
                $query .= "     AND MOSI_CD = '{$data_arr[$i]["MOSI_CD"]}' ";
                $query .= "     AND EXAMNO = '{$data_arr[$i]["EXAMNO"]}' ";
                $db->query($query);

                $query  = " DELETE FROM MOCK_CSV_SUNDAI_SCORE_DAT ";
                $query .= " WHERE ";
                $query .= "     YEAR = '{$data_arr[$i]["YEAR"]}' ";
                $query .= "     AND MOSI_CD = '{$data_arr[$i]["MOSI_CD"]}' ";
                $query .= "     AND EXAMNO = '{$data_arr[$i]["EXAMNO"]}' ";
                $db->query($query);

            } else if ($model->field["COMPANY"] == "00000002" && $model->field["CSV_DIV"] == "1") {

                $query  = " DELETE FROM MOCK_CSV_BENE_SCORE_HDAT ";
                $query .= " WHERE ";
                $query .= "     YEAR = '{$data_arr[$i]["YEAR"]}' ";
                $query .= "     AND KYOUZAICD = '{$data_arr[$i]["KYOUZAICD"]}' ";
                $query .= "     AND BENEID = '{$data_arr[$i]["BENEID"]}' ";
                $db->query($query);

                $query  = " DELETE FROM MOCK_CSV_BENE_SCORE_DAT ";
                $query .= " WHERE ";
                $query .= "     YEAR = '{$data_arr[$i]["YEAR"]}' ";
                $query .= "     AND KYOUZAICD = '{$data_arr[$i]["KYOUZAICD"]}' ";
                $query .= "     AND BENEID = '{$data_arr[$i]["BENEID"]}' ";
                $db->query($query);

            } else if ($model->field["COMPANY"] == "00000002" && $model->field["CSV_DIV"] == "2") {

                $query  = " DELETE FROM MOCK_CSV_BENE_HOPE_HDAT ";
                $query .= " WHERE ";
                $query .= "     YEAR = '{$data_arr[$i]["YEAR"]}' ";
                $query .= "     AND KYOUZAICD = '{$data_arr[$i]["KYOUZAICD"]}' ";
                $query .= "     AND BENEID = '{$data_arr[$i]["BENEID"]}' ";
                $db->query($query);

                $query  = " DELETE FROM MOCK_CSV_BENE_HOPE_DAT ";
                $query .= " WHERE ";
                $query .= "     YEAR = '{$data_arr[$i]["YEAR"]}' ";
                $query .= "     AND KYOUZAICD = '{$data_arr[$i]["KYOUZAICD"]}' ";
                $query .= "     AND BENEID = '{$data_arr[$i]["BENEID"]}' ";
                $db->query($query);
            } else if ($model->field["COMPANY"] == "00000003") {

                $query  = " DELETE FROM MOCK_CSV_ZKAI_HDAT ";
                $query .= " WHERE ";
                $query .= "     YEAR = '{$data_arr[$i]["YEAR"]}' ";
                $query .= "     AND MOSI_CD = '{$data_arr[$i]["MOSI_CD"]}' ";
                $query .= "     AND HR_CLASS = '".sprintf("%03d", $data_arr[$i]["HR_CLASS"])."' ";
                $query .= "     AND ATTENDNO = '".sprintf("%03d", $data_arr[$i]["ATTENDNO"])."' ";
                $db->query($query);

                $query  = " DELETE FROM MOCK_CSV_ZKAI_SCORE_DAT ";
                $query .= " WHERE ";
                $query .= "     YEAR = '{$data_arr[$i]["YEAR"]}' ";
                $query .= "     AND MOSI_CD = '{$data_arr[$i]["MOSI_CD"]}' ";
                $query .= "     AND HR_CLASS = '".sprintf("%03d", $data_arr[$i]["HR_CLASS"])."' ";
                $query .= "     AND ATTENDNO = '".sprintf("%03d", $data_arr[$i]["ATTENDNO"])."' ";
                $db->query($query);

                $query  = " DELETE FROM MOCK_CSV_ZKAI_HOPE_DAT ";
                $query .= " WHERE ";
                $query .= "         YEAR     = '{$data_arr[$i]["YEAR"]}' ";
                $query .= "     AND MOSI_CD  = '{$data_arr[$i]["MOSI_CD"]}' ";
                $query .= "     AND HR_CLASS = '".sprintf("%03d", $data_arr[$i]["HR_CLASS"])."' ";
                $query .= "     AND ATTENDNO = '".sprintf("%03d", $data_arr[$i]["ATTENDNO"])."' ";
                $db->query($query);
            } else if ($model->field["COMPANY"] == "00000004") {

                $query  = " DELETE FROM MOCK_CSV_KAWAI_HDAT ";
                $query .= " WHERE ";
                $query .= "     YEAR = '{$data_arr[$i]["YEAR"]}' ";
                $query .= "     AND MOSI_CD = '{$data_arr[$i]["MOSI_CD"]}' ";
                $query .= "     AND HR_CLASS = '".sprintf("%03d", $data_arr[$i]["HR_CLASS"])."' ";
                $query .= "     AND ATTENDNO = '".sprintf("%03d", $data_arr[$i]["ATTENDNO"])."' ";
                $db->query($query);

                $query  = " DELETE FROM MOCK_CSV_KAWAI_SCORE_DAT ";
                $query .= " WHERE ";
                $query .= "     YEAR = '{$data_arr[$i]["YEAR"]}' ";
                $query .= "     AND MOSI_CD = '{$data_arr[$i]["MOSI_CD"]}' ";
                $query .= "     AND HR_CLASS = '".sprintf("%03d", $data_arr[$i]["HR_CLASS"])."' ";
                $query .= "     AND ATTENDNO = '".sprintf("%03d", $data_arr[$i]["ATTENDNO"])."' ";
                $db->query($query);

                $query  = " DELETE FROM MOCK_CSV_KAWAI_HOPE_DAT ";
                $query .= " WHERE ";
                $query .= "         YEAR     = '{$data_arr[$i]["YEAR"]}' ";
                $query .= "     AND MOSI_CD  = '{$data_arr[$i]["MOSI_CD"]}' ";
                $query .= "     AND HR_CLASS = '".sprintf("%03d", $data_arr[$i]["HR_CLASS"])."' ";
                $query .= "     AND ATTENDNO = '".sprintf("%03d", $data_arr[$i]["ATTENDNO"])."' ";
                $db->query($query);
            }

            $cnt++;
        }
        $db->commit();
        Query::dbCheckIn($db);

        return $cnt;
    }

    //エラーＤＢへの追加
    function insertQueryErr(&$db, $record_no, $check_error)
    {
        $data1["PROGRAMID"][TEXT] = PROGRAMID;
        $data1["MSGROW"][NUMBER]  = $record_no;
        $data1["MSGREMARK"][TEXT] = $check_error;

        $query = Query::insertSQL($data1, "W_CSVMSG_PRG_DAT");

        $result = $db->query($query);

    }

    //CVS
    function selectCsvQuery($model)
    {
        if ($model->field["COMPANY"] == "00000001") {
            $query  = " SELECT ";
            $query .= "     T1.YEAR, ";
            $query .= "     T1.MOSI_CD, ";
            $query .= "     T1.MOSI_NAME, ";
            $query .= "     T1.KANA, ";
            $query .= "     T1.HR_CLASS, ";
            $query .= "     T1.ATTENDNO, ";
            $query .= "     T1.EXAMNO, ";
            $query .= "     T1.SCHOOL_CD, ";
            $query .= "     T1.SCHOOL_EDA, ";
            $query .= "     T1.SCHOOL_NAME, ";
            $query .= "     T1.BUNRI_DIV, ";
            $query .= "     T1.SEX, ";
            $query .= "     T1.GRADE, ";
            $query .= "     T1.GEN_SOTU, ";
            $query .= "     T1.KAMOKU_EIGO, ";
            $query .= "     T1.KAMOKU_SUUGAKU, ";
            $query .= "     T1.KAMOKU_KOKUGO, ";
            $query .= "     T1.KAMOKU_RI1, ";
            $query .= "     T1.KAMOKU_RI2, ";
            $query .= "     T1.KAMOKU_REKIKOU1, ";
            $query .= "     T1.KAMOKU_REKIKOU2, ";
            for ($hCnt = 1; $hCnt <= $model->hopeCnt; $hCnt++) {
                $query .= "     H{$hCnt}.SCHOOL_CD AS SCHOOL_CD{$hCnt}, ";
                $query .= "     H{$hCnt}.SCHOOL_NAME AS SCHOOL_NAME{$hCnt}, ";
                $query .= "     H{$hCnt}.NITTEI AS NITTEI{$hCnt}, ";
                $query .= "     H{$hCnt}.RANK AS RANK{$hCnt}, ";
                $query .= "     H{$hCnt}.CNT AS CNT{$hCnt}, ";
                $query .= "     H{$hCnt}.JUDGE_HYOUKA AS JUDGE_HYOUKA{$hCnt}, ";
                $query .= "     H{$hCnt}.JUDGE_SUUTI AS JUDGE_SUUTI{$hCnt}, ";
            }
            for ($sCnt = 1; $sCnt <= $model->scoreCnt; $sCnt++) {
                $query .= "     S{$sCnt}.SCORE AS SCORE{$sCnt}, ";
                $query .= "     S{$sCnt}.DEVIATION AS DEVIATION{$sCnt}, ";
                $query .= "     S{$sCnt}.RANK AS RANK{$sCnt}, ";
            }
            $query .= "     T1.DEVIATION, ";
            $query .= "     T1.RANK, ";
            $query .= "     T1.CNT ";
            $query .= " FROM ";
            $query .= "     MOCK_CSV_SUNDAI_HDAT T1 ";
            for ($hCnt = 1; $hCnt <= $model->hopeCnt; $hCnt++) {
                $setHCnt = sprintf("%03d", $hCnt);
                $query .= "     LEFT JOIN MOCK_CSV_SUNDAI_HOPE_DAT H{$hCnt} ON T1.YEAR = H{$hCnt}.YEAR ";
                $query .= "          AND T1.MOSI_CD = H{$hCnt}.MOSI_CD ";
                $query .= "          AND T1.EXAMNO = H{$hCnt}.EXAMNO ";
                $query .= "          AND H{$hCnt}.SEQ = '{$setHCnt}' ";
            }
            for ($sCnt = 1; $sCnt <= $model->scoreCnt; $sCnt++) {
                $query .= "     LEFT JOIN MOCK_CSV_SUNDAI_SCORE_DAT S{$sCnt} ON T1.YEAR = S{$sCnt}.YEAR ";
                $query .= "          AND T1.MOSI_CD = S{$sCnt}.MOSI_CD ";
                $query .= "          AND T1.EXAMNO = S{$sCnt}.EXAMNO ";
                $query .= "          AND S{$sCnt}.SEQ = '{$sCnt}' ";
            }
            $query .= " WHERE ";
            $query .= "     T1.YEAR = '{$model->field["YEAR"]}' ";
            $query .= "     AND T1.MOCKCD = '{$model->field["MOCKCD"]}' ";
            $query .= " ORDER BY ";
            $query .= "     T1.YEAR, ";
            $query .= "     T1.HR_CLASS, ";
            $query .= "     T1.ATTENDNO, ";
            $query .= "     T1.EXAMNO ";
        } else if ($model->field["COMPANY"] == "00000002" && $model->field["CSV_DIV"] == "1") {
            $query  = " SELECT ";
            $query .= "     T1.YEAR, ";
            $query .= "     T1.KYOUZAICD, ";
            $query .= "     T1.TYPE, ";
            $query .= "     T1.KYOUZAINAME, ";
            $query .= "     T1.GAKKACD, ";
            $query .= "     T1.GAKKANAME, ";
            $query .= "     T1.BENEID, ";
            $query .= "     T1.HR_CLASS, ";
            $query .= "     T1.ATTENDNO, ";
            $query .= "     T1.NAME, ";
            $query .= "     T1.BUNRI_DIV, ";
            $query .= "     T1.BIRTHDAY, ";
            $query .= "     T1.SEX, ";
            for ($sCnt = 1; $sCnt <= $model->scoreCnt; $sCnt++) {
                $query .= "     S{$sCnt}.SCORE AS SCORE{$sCnt}, ";
                $query .= "     S{$sCnt}.GTZ AS GTZ{$sCnt}, ";
                $query .= "     S{$sCnt}.ALL_DEV AS ALL_DEV{$sCnt}, ";
                $query .= "     S{$sCnt}.SCHOOL_DEV AS SCHOOL_DEV{$sCnt}, ";
                $query .= "     S{$sCnt}.ALL_RANK AS ALL_RANK{$sCnt}, ";
                $query .= "     S{$sCnt}.SCHOOL_RANK AS SCHOOL_RANK{$sCnt}, ";
            }
            $query .= "     T1.DEVIATION, ";
            $query .= "     T1.RANK, ";
            $query .= "     T1.CNT ";
            $query .= " FROM ";
            $query .= "     MOCK_CSV_BENE_SCORE_HDAT T1 ";
            for ($sCnt = 1; $sCnt <= $model->scoreCnt; $sCnt++) {
                $query .= "     LEFT JOIN MOCK_CSV_BENE_SCORE_DAT S{$sCnt} ON T1.YEAR = S{$sCnt}.YEAR ";
                $query .= "          AND T1.KYOUZAICD = S{$sCnt}.KYOUZAICD ";
                $query .= "          AND T1.BENEID = S{$sCnt}.BENEID ";
                $query .= "          AND S{$sCnt}.SEQ = '{$sCnt}' ";
            }
            $query .= " WHERE ";
            $query .= "     T1.YEAR = '{$model->field["YEAR"]}' ";
            $query .= "     AND T1.MOCKCD = '{$model->field["MOCKCD"]}' ";
            $query .= " ORDER BY ";
            $query .= "     T1.YEAR, ";
            $query .= "     T1.HR_CLASS, ";
            $query .= "     T1.ATTENDNO, ";
            $query .= "     T1.BENEID ";
        } else if ($model->field["COMPANY"] == "00000002" && $model->field["CSV_DIV"] == "2") {
            $query  = " SELECT ";
            $query .= "     T1.YEAR, ";
            $query .= "     T1.KYOUZAICD, ";
            $query .= "     T1.TYPE, ";
            $query .= "     T1.KYOUZAINAME, ";
            $query .= "     T1.GAKKACD, ";
            $query .= "     T1.GAKKANAME, ";
            $query .= "     T1.BENEID, ";
            $query .= "     T1.HR_CLASS, ";
            $query .= "     T1.ATTENDNO, ";
            $query .= "     T1.NAME, ";
            $query .= "     T1.BUNRI_DIV, ";
            $query .= "     T1.BIRTHDAY, ";
            $query .= "     T1.SEX ";
            for ($hCnt = 1; $hCnt <= $model->hopeCnt; $hCnt++) {
                $query .= "     ,H{$hCnt}.SCHOOL_CD AS SCHOOL_CD{$hCnt} ";
                $query .= "     ,H{$hCnt}.BOSYUTANNI AS BOSYUTANNI{$hCnt} ";
                $query .= "     ,H{$hCnt}.SCHOOL_NAME AS SCHOOL_NAME{$hCnt} ";
                $query .= "     ,H{$hCnt}.GAKUBU_NAME AS GAKUBU_NAME{$hCnt} ";
                $query .= "     ,H{$hCnt}.GAKKA_NAME AS GAKKA_NAME{$hCnt} ";
                $query .= "     ,H{$hCnt}.NITTEI AS NITTEI{$hCnt} ";
                $query .= "     ,H{$hCnt}.HOUSIKI AS HOUSIKI{$hCnt} ";
                $query .= "     ,H{$hCnt}.ALL_JUDGE AS ALL_JUDGE{$hCnt} ";
                $query .= "     ,H{$hCnt}.JUDGE1 AS JUDGE1{$hCnt} ";
                $query .= "     ,H{$hCnt}.JUDGE2 AS JUDGE2{$hCnt} ";
                $query .= "     ,H{$hCnt}.ALL_JUDGE_SS AS ALL_JUDGE_SS{$hCnt} ";
                $query .= "     ,H{$hCnt}.JUDGE1_SS AS JUDGE1_SS{$hCnt} ";
                $query .= "     ,H{$hCnt}.JUDGE2_SS AS JUDGE2_SS{$hCnt} ";
            }
            $query .= " FROM ";
            $query .= "     MOCK_CSV_BENE_HOPE_HDAT T1 ";
            for ($hCnt = 1; $hCnt <= $model->hopeCnt; $hCnt++) {
                $query .= "     LEFT JOIN MOCK_CSV_BENE_HOPE_DAT H{$hCnt} ON T1.YEAR = H{$hCnt}.YEAR ";
                $query .= "          AND T1.KYOUZAICD = H{$hCnt}.KYOUZAICD ";
                $query .= "          AND T1.BENEID = H{$hCnt}.BENEID ";
                $query .= "          AND H{$hCnt}.SEQ = '{$hCnt}' ";
            }
            $query .= " WHERE ";
            $query .= "     T1.YEAR = '{$model->field["YEAR"]}' ";
            $query .= "     AND T1.MOCKCD = '{$model->field["MOCKCD"]}' ";
            $query .= " ORDER BY ";
            $query .= "     T1.YEAR, ";
            $query .= "     T1.HR_CLASS, ";
            $query .= "     T1.ATTENDNO, ";
            $query .= "     T1.BENEID ";
        } else if ($model->field["COMPANY"] == "00000003") {
            $query  = " SELECT ";
            $query .= "     T1.YEAR, ";
            $query .= "     T1.MOSI_CD, ";
            $query .= "     T1.MOSI_NAME, ";
            $query .= "     T1.HR_CLASS, ";
            $query .= "     T1.ATTENDNO, ";
            $query .= "     T1.KANA ";
            for ($sCnt = 1; $sCnt <= $model->scoreCnt; $sCnt++) {
                $query .= "     ,S{$sCnt}.SCORE AS SCORE{$sCnt} ";
                $query .= "     ,S{$sCnt}.DEVIATION AS DEVIATION{$sCnt} ";
            }
            for ($hCnt = 1; $hCnt <= $model->hopeCnt; $hCnt++) {
                $query .= "     ,H{$hCnt}.SCHOOL_NAME  AS SCHOOL_NAME{$hCnt} ";
                $query .= "     ,H{$hCnt}.SCHOOL_CD    AS SCHOOL_CD{$hCnt} ";
                $query .= "     ,H{$hCnt}.JUDGE_HYOUKA AS JUDGE_HYOUKA{$hCnt} ";
                $query .= "     ,H{$hCnt}.RANK         AS RANK{$hCnt} ";
                $query .= "     ,H{$hCnt}.JUDGE_SUUTI  AS JUDGE_SUUTI{$hCnt} ";
            }
            $query .= " FROM ";
            $query .= "     MOCK_CSV_ZKAI_HDAT T1 ";
            for ($sCnt = 1; $sCnt <= $model->scoreCnt; $sCnt++) {
                $query .= "     LEFT JOIN MOCK_CSV_ZKAI_SCORE_DAT S{$sCnt} ON T1.YEAR = S{$sCnt}.YEAR ";
                $query .= "          AND T1.MOSI_CD = S{$sCnt}.MOSI_CD ";
                $query .= "          AND T1.HR_CLASS = S{$sCnt}.HR_CLASS ";
                $query .= "          AND T1.ATTENDNO = S{$sCnt}.ATTENDNO ";
                $query .= "          AND S{$sCnt}.SEQ = '{$sCnt}' ";
            }
            for ($hCnt = 1; $hCnt <= $model->hopeCnt; $hCnt++) {
                $setHCnt = sprintf("%03d", $hCnt);
                $query .= "     LEFT JOIN MOCK_CSV_ZKAI_HOPE_DAT H{$hCnt} ON T1.YEAR      = H{$hCnt}.YEAR ";
                $query .= "                                              AND T1.MOSI_CD   = H{$hCnt}.MOSI_CD ";
                $query .= "                                              AND T1.HR_CLASS  = H{$hCnt}.HR_CLASS ";
                $query .= "                                              AND T1.ATTENDNO  = H{$hCnt}.ATTENDNO ";
                $query .= "                                              AND H{$hCnt}.SEQ = '{$setHCnt}' ";
            }
            $query .= " WHERE ";
            $query .= "     T1.YEAR = '{$model->field["YEAR"]}' ";
            $query .= "     AND T1.MOCKCD = '{$model->field["MOCKCD"]}' ";
            $query .= " ORDER BY ";
            $query .= "     T1.YEAR, ";
            $query .= "     T1.HR_CLASS, ";
            $query .= "     T1.ATTENDNO ";

        } else if ($model->field["COMPANY"] == "00000004") {
            $query  = " SELECT ";
            $query .= "     T1.YEAR, ";
            $query .= "     T1.MOSI_CD, ";
            $query .= "     T1.MOSI_NAME, ";
            $query .= "     T1.HR_CLASS, ";
            $query .= "     T1.ATTENDNO, ";
            $query .= "     T1.KANA ";
            for ($sCnt = 1; $sCnt <= $model->scoreCnt; $sCnt++) {
                $query .= "     ,S{$sCnt}.SCORE AS SCORE{$sCnt} ";
                $query .= "     ,S{$sCnt}.DEVIATION AS DEVIATION{$sCnt} ";
            }
            for ($hCnt = 1; $hCnt <= $model->hopeCnt; $hCnt++) {
                $query .= "     ,H{$hCnt}.SCHOOL_NAME  AS SCHOOL_NAME{$hCnt} ";
                $query .= "     ,H{$hCnt}.SCHOOL_CD    AS SCHOOL_CD{$hCnt} ";
                $query .= "     ,H{$hCnt}.JUDGE_HYOUKA AS JUDGE_HYOUKA{$hCnt} ";
                $query .= "     ,H{$hCnt}.RANK         AS RANK{$hCnt} ";
                $query .= "     ,H{$hCnt}.JUDGE_SUUTI  AS JUDGE_SUUTI{$hCnt} ";
            }
            $query .= " FROM ";
            $query .= "     MOCK_CSV_KAWAI_HDAT T1 ";
            for ($sCnt = 1; $sCnt <= $model->scoreCnt; $sCnt++) {
                $query .= "     LEFT JOIN MOCK_CSV_KAWAI_SCORE_DAT S{$sCnt} ON T1.YEAR = S{$sCnt}.YEAR ";
                $query .= "          AND T1.MOSI_CD = S{$sCnt}.MOSI_CD ";
                $query .= "          AND T1.HR_CLASS = S{$sCnt}.HR_CLASS ";
                $query .= "          AND T1.ATTENDNO = S{$sCnt}.ATTENDNO ";
                $query .= "          AND S{$sCnt}.SEQ = '{$sCnt}' ";
            }
            for ($hCnt = 1; $hCnt <= $model->hopeCnt; $hCnt++) {
                $setHCnt = sprintf("%03d", $hCnt);
                $query .= "     LEFT JOIN MOCK_CSV_KAWAI_HOPE_DAT H{$hCnt} ON T1.YEAR      = H{$hCnt}.YEAR ";
                $query .= "                                              AND T1.MOSI_CD   = H{$hCnt}.MOSI_CD ";
                $query .= "                                              AND T1.HR_CLASS  = H{$hCnt}.HR_CLASS ";
                $query .= "                                              AND T1.ATTENDNO  = H{$hCnt}.ATTENDNO ";
                $query .= "                                              AND H{$hCnt}.SEQ = '{$setHCnt}' ";
            }
            $query .= " WHERE ";
            $query .= "     T1.YEAR = '{$model->field["YEAR"]}' ";
            $query .= "     AND T1.MOCKCD = '{$model->field["MOCKCD"]}' ";
            $query .= " ORDER BY ";
            $query .= "     T1.YEAR, ";
            $query .= "     T1.HR_CLASS, ";
            $query .= "     T1.ATTENDNO ";

        }
        return $query;
    }

    function getTitleMockCompany($model) {
        $query .= " SELECT ";
        $query .= "     NAME1 ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = 'H313' ";
        $query .= "     AND NAMESPARE1 = '{$model->field["COMPANY"]}' ";

        return $query;
    }

    function getTitleCsvDiv($model) {
        $query .= " WITH MAIN (LABEL, VALUE) AS ( ";
        $query .= "     VALUES('得点データ', '1') ";
        $query .= "     UNION ";
        $query .= "     VALUES('希望データ', '2') ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     MAIN ";
        $query .= " WHERE ";
        $query .= "     VALUE = '{$model->field["CSV_DIV"]}' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    function getTitleMockMst($model) {
        $query  = " SELECT ";
        $query .= "     T1.MOCKNAME1 ";
        $query .= " FROM ";
        $query .= "     MOCK_MST T1 ";
        $query .= " WHERE ";
        $query .= "     T1.MOCKCD = '{$model->field["MOCKCD"]}' ";

        return $query;
    }

    //学年
    function getTitleGrade($model)
    {
        $query  = " SELECT ";
        $query .= "     GRADE_NAME2 ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_GDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$model->field["YEAR"]}' ";
        $query .= "     AND GRADE_CD = '{$model->field["GRADE"]}' ";

        return $query;
    }

    //CSV_HEAD_TITLE
    function getCsvTitle($model, $tableName)
    {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     {$tableName} ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$model->field["YEAR"]}' ";
        $query .= "     AND MOCKCD = '{$model->field["MOCKCD"]}' ";
        return $query;
    }

    //CSV_HEAD_TITLE駿台HOPE
    function getCsvSundaiTitle($model, $tableName, $mosiCd)
    {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     {$tableName} ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$model->field["YEAR"]}' ";
        $query .= "     AND MOSI_CD = '{$mosiCd}' ";
        $query .= " ORDER BY ";
        $query .= "     SEQ ";
        return $query;
    }

    //CSV_HEAD_TITLE真剣HOPE
    function getCsvBeneTitle($model, $tableName, $kyouzaiCd)
    {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     {$tableName} ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$model->field["YEAR"]}' ";
        $query .= "     AND KYOUZAICD = '{$kyouzaiCd}' ";
        $query .= " ORDER BY ";
        $query .= "     SEQ ";
        return $query;
    }

    //模試科目数、志望校数取得
    function getCsvTitleDefault($model, $tableName) {
        $query  = " SELECT ";
        $query .= "     FIELD_CNT ";
        $query .= " FROM ";
        $query .= "     {$tableName} ";
        $query .= " WHERE ";
        $query .= "         YEAR   = '{$model->field["YEAR"]}' ";
        $query .= "     AND MOCKCD = '{$model->field["MOCKCD"]}' ";
        $query .= "     AND GRADE  = '{$model->field["GRADE"]}' ";

        return $query;
    }

    //学校名取得
    function getCheck_School()
    {
        $query  = " SELECT ";
        $query .= "     NAME1 ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = 'Z010' ";
        $query .= "     AND NAMECD2 = '00' ";
        return $query;

    }


    //模試のファイル名を取得
    function getMock_File_Name($mockcd)
    {
        $query  = " SELECT ";
        $query .= "     FILE_NAME ";
        $query .= " FROM ";
        $query .= "     MOCK_MST ";
        $query .= " WHERE ";
        $query .= "     MOCKCD = '".$mockcd."' ";
        
        return $query;

    }


}
?>
