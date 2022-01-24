<?php

require_once('for_php7.php');

class knjz291Query extends Query {

    //教育委員会用学校コード取得
    function getEdboardSchoolcd() {
        $query  = " SELECT ";
        $query .= "     KYOUIKU_IINKAI_SCHOOLCD ";
        $query .= " FROM ";
        $query .= "     V_SCHOOL_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";

        return $query;
    }

    //年度取得
    function getStaffYear() {
        $query  = " SELECT DISTINCT ";
        $query .= "     YEAR AS VALUE, ";
        $query .= "     YEAR || '年度' AS LABEL ";
        $query .= " FROM ";
        $query .= "     STAFF_YDAT ";
        $query .= " ORDER BY ";
        $query .= "     VALUE DESC ";

        return $query;
    }

    //Z010
    function getKyouikuIinkai() {

        $query  = " SELECT ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     NAME_MST T1 ";
        $query .= " WHERE ";
        $query .= "     T1.NAMECD1 = 'Z010' ";
        $query .= "     AND T1.ABBV1 = '1' ";

        return $query;
    }

    //学年取得
    function getGrade($model, $positioncdManager="") {
        $detailYear = $model->year == "ALL" ? CTRL_YEAR : $model->year;
        $detailYear = $detailYear ? $detailYear : CTRL_YEAR;

        $query  = " SELECT ";
        if ($positioncdManager) {
            $query .= "     GRADE_NAME1 AS LABEL ";
        } else {
            $query .= "     GRADE AS VALUE, ";
            $query .= "     GRADE_NAME1 AS LABEL ";
        }
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_GDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$detailYear."' ";
        if ($positioncdManager) {
            $query .= " AND GRADE = '".$positioncdManager."' ";
        } else {
            $query .= " ORDER BY ";
            $query .= "     VALUE ";
        }

        return $query;
    }

    //教科取得
    function getClass($model, $positioncdManager="") {
        $detailYear = $model->year == "ALL" ? CTRL_YEAR : $model->year;
        $detailYear = $detailYear ? $detailYear : CTRL_YEAR;

        $query  = " SELECT ";
        if ($positioncdManager) {
            //教育課程対応
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "     CLASSCD || '-' || SCHOOL_KIND || ' ' || CLASSNAME AS LABEL ";
            } else {
                $query .= "     CLASSCD || ' ' || CLASSNAME AS LABEL ";
            }
        } else {
            //教育課程対応
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "     CLASSCD || '-' || SCHOOL_KIND AS VALUE, ";
                $query .= "     CLASSCD || '-' || SCHOOL_KIND || ' ' || CLASSNAME AS LABEL ";
            } else {
                $query .= "     CLASSCD AS VALUE, ";
                $query .= "     CLASSCD || ' ' || CLASSNAME AS LABEL ";
            }
        }
        $query .= " FROM ";
        $query .= "     V_CLASS_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$detailYear."' ";
        if ($positioncdManager) {
            //教育課程対応
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= " AND CLASSCD || '-' || SCHOOL_KIND = '".$positioncdManager."' ";
            } else {
                $query .= " AND CLASSCD = '".$positioncdManager."' ";
            }
        } else {
            $query .= " ORDER BY ";
            $query .= "     VALUE ";
        }
        return $query;
    }

    //リスト取得
    function getList($model) {
        $detailYear = $model->year == "ALL" ? CTRL_YEAR : $model->year;
        $detailYear = $detailYear ? $detailYear : CTRL_YEAR;

        $query  = " SELECT ";
        $query .= "     T1.STAFFCD, ";
        $query .= "     T1.STAFFNAME, ";
        $query .= "     T1.STAFFNAME_SHOW, ";
        $query .= "     T1.STAFFNAME_KANA, ";
        $query .= "     T1.STAFFNAME_ENG, ";
        $query .= "     T1.STAFFNAME_REAL, ";
        $query .= "     T1.STAFFNAME_KANA_REAL, ";
        $query .= "     L1.FIELD1 AS JOBCD, ";
        $query .= "     L2.FIELD1 AS SECTIONCD, ";
        $query .= "     L3.FIELD1 AS DUTYSHARECD, ";
        $query .= "     L4.FIELD1 AS CHARGECLASSCD, ";
        $query .= "     L5.FIELD1 AS POSITIONCD1, ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     CASE WHEN L5.FIELD1 = '0200' THEN L5.FIELD2 WHEN L5.FIELD1 = '1050' THEN L5.FIELD2 || '-' || L5.FIELD3 ELSE NULL END AS POSITIONCD1_MANAGER, ";
        } else {
            $query .= "     CASE WHEN L5.FIELD1 = '0200' THEN L5.FIELD2 WHEN L5.FIELD1 = '1050' THEN L5.FIELD2 ELSE NULL END AS POSITIONCD1_MANAGER, ";
        }
        
        $query .= "     L6.FIELD1 AS POSITIONCD2, ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     CASE WHEN L6.FIELD1 = '0200' THEN L6.FIELD2 WHEN L6.FIELD1 = '1050' THEN L6.FIELD2 || '-' || L6.FIELD3 ELSE NULL END AS POSITIONCD2_MANAGER, ";
        } else {
            $query .= "     CASE WHEN L6.FIELD1 = '0200' THEN L6.FIELD2 WHEN L6.FIELD1 = '1050' THEN L6.FIELD2 ELSE NULL END AS POSITIONCD2_MANAGER, ";
        }
        
        $query .= "     L7.FIELD1 AS POSITIONCD3, ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     CASE WHEN L7.FIELD1 = '0200' THEN L7.FIELD2 WHEN L6.FIELD1 = '1050' THEN L7.FIELD2 || '-' || L7.FIELD3 ELSE NULL END AS POSITIONCD3_MANAGER, ";
        } else {
            $query .= "     CASE WHEN L7.FIELD1 = '0200' THEN L7.FIELD2 WHEN L6.FIELD1 = '1050' THEN L7.FIELD2 ELSE NULL END AS POSITIONCD3_MANAGER, ";
        }
        $query .= "     T2.JOBNAME, ";
        $query .= "     T3.SECTIONABBV, ";
        $query .= "     T4.SHARENAME, ";
        $query .= "     T6.POSITIONNAME AS POSITIONNAME1, ";
        $query .= "     T7.POSITIONNAME AS POSITIONNAME2, ";
        $query .= "     T8.POSITIONNAME AS POSITIONNAME3, ";
        $query .= "     T1.STAFFSEX, ";
        $query .= "     T5.NAME2, ";
        $query .= "     T1.STAFFBIRTHDAY, ";
        $query .= "     T1.STAFFZIPCD, ";
        $query .= "     T1.STAFFADDR1, ";
        $query .= "     T1.STAFFADDR2, ";
        $query .= "     T1.STAFFTELNO, ";
        $query .= "     T1.STAFFFAXNO, ";
        $query .= "     T1.STAFFE_MAIL, ";
        $query .= "     T1.UPDATED ";
        $query .= " FROM ";
        $query .= "     STAFF_MST T1";
        $query .= "     LEFT JOIN STAFF_DETAIL_MST L1 ON L1.YEAR = '{$detailYear}' ";
        $query .= "          AND T1.STAFFCD = L1.STAFFCD ";
        $query .= "          AND L1.STAFF_SEQ = '001' ";
        $query .= "     LEFT JOIN STAFF_DETAIL_MST L2 ON L2.YEAR = '{$detailYear}' ";
        $query .= "          AND T1.STAFFCD = L2.STAFFCD ";
        $query .= "          AND L2.STAFF_SEQ = '002' ";
        $query .= "     LEFT JOIN STAFF_DETAIL_MST L3 ON L3.YEAR = '{$detailYear}' ";
        $query .= "          AND T1.STAFFCD = L3.STAFFCD ";
        $query .= "          AND L3.STAFF_SEQ = '003' ";
        $query .= "     LEFT JOIN STAFF_DETAIL_MST L4 ON L4.YEAR = '{$detailYear}' ";
        $query .= "          AND T1.STAFFCD = L4.STAFFCD ";
        $query .= "          AND L4.STAFF_SEQ = '004' ";
        $query .= "     LEFT JOIN STAFF_DETAIL_MST L5 ON L5.YEAR = '{$detailYear}' ";
        $query .= "          AND T1.STAFFCD = L5.STAFFCD ";
        $query .= "          AND L5.STAFF_SEQ = '005' ";
        $query .= "     LEFT JOIN STAFF_DETAIL_MST L6 ON L6.YEAR = '{$detailYear}' ";
        $query .= "          AND T1.STAFFCD = L6.STAFFCD ";
        $query .= "          AND L6.STAFF_SEQ = '006' ";
        $query .= "     LEFT JOIN STAFF_DETAIL_MST L7 ON L7.YEAR = '{$detailYear}' ";
        $query .= "          AND T1.STAFFCD = L7.STAFFCD ";
        $query .= "          AND L7.STAFF_SEQ = '007' ";
        $query .= "     LEFT JOIN JOB_MST T2 ON L1.FIELD1 = T2.JOBCD ";
        $query .= "     LEFT JOIN SECTION_MST T3 ON L2.FIELD1 = T3.SECTIONCD ";
        $query .= "     LEFT JOIN DUTYSHARE_MST T4 ON L3.FIELD1 = T4.DUTYSHARECD ";
        $query .= "     LEFT JOIN NAME_MST T5 ON T5.NAMECD1 = 'Z002' AND T1.STAFFSEX = T5.NAMECD2 ";
        $query .= "     LEFT JOIN POSITION_MST T6 ON L5.FIELD1 = T6.POSITIONCD ";
        $query .= "     LEFT JOIN POSITION_MST T7 ON L6.FIELD1 = T7.POSITIONCD ";
        $query .= "     LEFT JOIN POSITION_MST T8 ON L7.FIELD1 = T8.POSITIONCD ";
        if ($model->year != "ALL") {
            $query .= " WHERE ";
            $query .= "     T1.STAFFCD IN (SELECT STAFFCD FROM STAFF_YDAT WHERE YEAR = '".$detailYear."') ";
        }
        $query .= " ORDER BY ";
        $query .= "     T1.STAFFCD";

        return $query;
    }

    //１レコード取得
    function getRow($model, $staffcd) {

        $detailYear = $model->year == "ALL" ? CTRL_YEAR : $model->year;
        $detailYear = $detailYear ? $detailYear : CTRL_YEAR;

        $query .= " SELECT ";
        $query .= "     T1.STAFFCD, ";
        $query .= "     T1.STAFFNAME, ";
        $query .= "     T1.STAFFNAME_SHOW, ";
        $query .= "     T1.STAFFNAME_KANA, ";
        $query .= "     T1.STAFFNAME_ENG, ";
        $query .= "     T1.STAFFNAME_REAL, ";
        $query .= "     T1.STAFFNAME_KANA_REAL, ";
        $query .= "     L1.FIELD1 AS JOBCD, ";
        $query .= "     L2.FIELD1 AS SECTIONCD, ";
        $query .= "     L3.FIELD1 AS DUTYSHARECD, ";
        $query .= "     L3.FIELD2 AS DUTYSHARECD2, ";
        $query .= "     L4.FIELD1 AS CHARGECLASSCD, ";
        $query .= "     L5.FIELD1 AS POSITIONCD1, ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     CASE WHEN L5.FIELD1 = '0200' THEN L5.FIELD2 WHEN L5.FIELD1 = '1050' THEN L5.FIELD2 || '-' || L5.FIELD3 ELSE NULL END AS POSITIONCD1_MANAGER, ";
        } else {
            $query .= "     CASE WHEN L5.FIELD1 = '0200' THEN L5.FIELD2 WHEN L5.FIELD1 = '1050' THEN L5.FIELD2 ELSE NULL END AS POSITIONCD1_MANAGER, ";
        }
        
        $query .= "     L6.FIELD1 AS POSITIONCD2, ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     CASE WHEN L6.FIELD1 = '0200' THEN L6.FIELD2 WHEN L6.FIELD1 = '1050' THEN L6.FIELD2 || '-' || L6.FIELD3 ELSE NULL END AS POSITIONCD2_MANAGER, ";
        } else {
            $query .= "     CASE WHEN L6.FIELD1 = '0200' THEN L6.FIELD2 WHEN L6.FIELD1 = '1050' THEN L6.FIELD2 ELSE NULL END AS POSITIONCD2_MANAGER, ";
        }
        
        $query .= "     L7.FIELD1 AS POSITIONCD3, ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     CASE WHEN L7.FIELD1 = '0200' THEN L7.FIELD2 WHEN L7.FIELD1 = '1050' THEN L7.FIELD2 || '-' || L7.FIELD3 ELSE NULL END AS POSITIONCD3_MANAGER, ";
        } else {
            $query .= "     CASE WHEN L7.FIELD1 = '0200' THEN L7.FIELD2 WHEN L7.FIELD1 = '1050' THEN L7.FIELD2 ELSE NULL END AS POSITIONCD3_MANAGER, ";
        }
        $query .= "     L8.FIELD1 AS CHARGENURSEOFF, ";
        $query .= "     T1.STAFFSEX, ";
        $query .= "     T1.STAFFBIRTHDAY, ";
        $query .= "     T1.STAFFZIPCD, ";
        $query .= "     T1.STAFFADDR1, ";
        $query .= "     T1.STAFFADDR2, ";
        $query .= "     T1.STAFFTELNO, ";
        $query .= "     T1.STAFFFAXNO, ";
        $query .= "     T1.STAFFE_MAIL, ";
        $query .= "     T1.EDBOARD_STAFFCD, ";
        $query .= "     T1.EDBOARD_TORIKOMI_FLG, ";
        $query .= "     T1.UPDATED ";
        $query .= " FROM ";
        $query .= "     STAFF_MST T1";
        $query .= "     LEFT JOIN STAFF_DETAIL_MST L1 ON L1.YEAR = '{$detailYear}' ";
        $query .= "          AND T1.STAFFCD = L1.STAFFCD ";
        $query .= "          AND L1.STAFF_SEQ = '001' ";
        $query .= "     LEFT JOIN STAFF_DETAIL_MST L2 ON L2.YEAR = '{$detailYear}' ";
        $query .= "          AND T1.STAFFCD = L2.STAFFCD ";
        $query .= "          AND L2.STAFF_SEQ = '002' ";
        $query .= "     LEFT JOIN STAFF_DETAIL_MST L3 ON L3.YEAR = '{$detailYear}' ";
        $query .= "          AND T1.STAFFCD = L3.STAFFCD ";
        $query .= "          AND L3.STAFF_SEQ = '003' ";
        $query .= "     LEFT JOIN STAFF_DETAIL_MST L4 ON L4.YEAR = '{$detailYear}' ";
        $query .= "          AND T1.STAFFCD = L4.STAFFCD ";
        $query .= "          AND L4.STAFF_SEQ = '004' ";
        $query .= "     LEFT JOIN STAFF_DETAIL_MST L5 ON L5.YEAR = '{$detailYear}' ";
        $query .= "          AND T1.STAFFCD = L5.STAFFCD ";
        $query .= "          AND L5.STAFF_SEQ = '005' ";
        $query .= "     LEFT JOIN STAFF_DETAIL_MST L6 ON L6.YEAR = '{$detailYear}' ";
        $query .= "          AND T1.STAFFCD = L6.STAFFCD ";
        $query .= "          AND L6.STAFF_SEQ = '006' ";
        $query .= "     LEFT JOIN STAFF_DETAIL_MST L7 ON L7.YEAR = '{$detailYear}' ";
        $query .= "          AND T1.STAFFCD = L7.STAFFCD ";
        $query .= "          AND L7.STAFF_SEQ = '007' ";
        $query .= "     LEFT JOIN STAFF_DETAIL_MST L8 ON L8.YEAR = '{$detailYear}' ";
        $query .= "          AND T1.STAFFCD = L8.STAFFCD ";
        $query .= "          AND L8.STAFF_SEQ = '008' ";
        $query .= " WHERE ";
        $query .= "     T1.STAFFCD = '{$staffcd}' ";

        return $query;
    }

    //職員履歴
    function getEdWork($model, $staffcd) {
        $query .= " SELECT ";
        $query .= "     MAIN.* ";
        $query .= " FROM ";
        $query .= "     EDBOARD_STAFF_WORK_HIST_DAT MAIN, ";
        $query .= "     ( ";
        $query .= "     SELECT ";
        $query .= "         STAFFCD, ";
        $query .= "         MAX(FROM_DATE) AS FROM_DATE ";
        $query .= "     FROM ";
        $query .= "         EDBOARD_STAFF_WORK_HIST_DAT ";
        $query .= "     WHERE ";
        $query .= "         STAFFCD = '{$staffcd}' ";
        $query .= "     GROUP BY ";
        $query .= "         STAFFCD ";
        $query .= "     ) T2 ";
        $query .= " WHERE ";
        $query .= "     MAIN.STAFFCD = T2.STAFFCD ";
        $query .= "     AND MAIN.FROM_DATE = T2.FROM_DATE ";

        return $query;
    }

    //職員区分取得（学校別教職員外テーブル）
    function getStaffDiv($edboard_schoolcd, $staffcd) {
        $query  = " SELECT "; 
        $query .= "     STAFF_DIV "; 
        $query .= " FROM "; 
        $query .= "     EDBOARD_STAFF_DAT "; 
        $query .= " WHERE "; 
        $query .= "     EDBOARD_SCHOOLCD = '".$edboard_schoolcd."' "; 
        $query .= " AND STAFFCD = '".$staffcd."'  "; 

        return $query;
    }

    //職名取得
    function getJOBNAME() {
        $query  = " SELECT DISTINCT ";
        $query .= "     JOBCD AS VALUE, ";
        $query .= "     JOBCD || '  ' || JOBNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     JOB_MST ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //所属取得
    function getSECTION() {
        $query  = " SELECT DISTINCT ";
        $query .= "     SECTIONCD AS VALUE, ";
        $query .= "     SECTIONCD || '  ' || SECTIONABBV AS LABEL ";
        $query .= " FROM ";
        $query .= "     SECTION_MST ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //校務分掌部取得
    function getDUTYSHARE() {
        $query  = " SELECT DISTINCT ";
        $query .= "     DUTYSHARECD AS VALUE, ";
        $query .= "     DUTYSHARECD || '  ' || SHARENAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     DUTYSHARE_MST ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //性別取得
    function getSTAFFSEX() {
        $query  = " SELECT DISTINCT ";
        $query .= "     NAMECD2 AS VALUE, ";
        $query .= "     NAMECD2 || '  ' || NAME2 AS LABEL ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = 'Z002' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //資格教科
    function getStaffClass($model) {
        $query  = " SELECT ";
        $query .= "     T2.CLASSNAME, ";
        $query .= "     T1.CLASSCD ";
        $query .= " FROM ";
        $query .= "     STAFF_CLASS_MST T1 ";
        $query .= "     LEFT JOIN CLASS_MST T2 ON T1.CLASSCD = T2.CLASSCD ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "                       AND T1.SCHOOL_KIND   = T2.SCHOOL_KIND ";
        }
        $query .= " WHERE ";
        $query .= "     T1.STAFFCD = '".$model->staffcd."' ";
        $query .= " ORDER BY ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "      T1.SCHOOL_KIND, ";
            $query .= "      T1.CLASSCD ";
        } else {
            $query .= "      T1.CLASSCD ";
        }
        return $query;
    }

    //届け取得
    function getStaffRequestform($model) {
        $sdate = CTRL_YEAR.'-04-01';
        $edate = (CTRL_YEAR+1).'-03-31';

        $query  = " SELECT ";
        $query .= "     T2.NAME1 || '(' || REPLACE(CHAR(T1.SDATE),'-','/') || '～' || REPLACE(CHAR(T1.EDATE),'-','/') || ')' ";
        $query .= " FROM ";
        $query .= "     STAFF_REQUESTFORM_DAT T1 ";
        $query .= "     LEFT JOIN NAME_MST T2 ON T1.WORK_DIV = T2.NAMECD2 AND T2.NAMECD1 = 'Z028' ";
        $query .= " WHERE ";
        $query .= "     STAFFCD = '".$model->staffcd."' AND ";
        $query .= "     DATE('".$edate."') >= SDATE AND ";
        $query .= "     DATE('".$sdate."') <= EDATE ";
        $query .= " ORDER BY ";
        $query .= "     T1.SDATE ";

        return $query;
    }

    //免許・資格取得
    function getStaffQualified($model) {
        $query  = " SELECT ";
        $query .= "     T2.NAME1 AS QUALIFIED_CD, ";
        $query .= "     T1.QUALIFIED_NAME ";
        $query .= " FROM ";
        $query .= "     STAFF_QUALIFIED_DAT T1 ";
        $query .= "     LEFT JOIN NAME_MST T2 ON T1.QUALIFIED_CD = T2.NAMECD2 AND T2.NAMECD1 = 'Z031' ";
        $query .= " WHERE ";
        $query .= "     T1.STAFFCD = '".$model->staffcd."' ";
        $query .= " ORDER BY ";
        $query .= "     T1.SEQ ";

        return $query;
    }

    //肩書き
    function getPositionCd($model)
    {
        $detailYear = $model->year == "ALL" ? CTRL_YEAR : $model->year;
        $detailYear = $detailYear ? $detailYear : CTRL_YEAR;

        $query .= " SELECT ";
        $query .= "     T1.POSITIONCD || ' ' || L1.POSITIONNAME AS LABEL, ";
        $query .= "     T1.POSITIONCD AS VALUE ";
        $query .= " FROM ";
        $query .= "     POSITION_YDAT T1 ";
        $query .= "     LEFT JOIN POSITION_MST L1 ON L1.POSITIONCD = T1.POSITIONCD ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".$detailYear."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";
        return $query;
    }

    //担当教科
    function getStaffExt($model, $staffSeq)
    {
        $detailYear = $model->year == "ALL" ? CTRL_YEAR : $model->year;
        $detailYear = $detailYear ? $detailYear : CTRL_YEAR;

        $query .= " SELECT ";
        $query .= "     EXT_SEQ, ";
        $query .= "     FIELD2 || '-' || FIELD3 AS VALUE ";
        $query .= " FROM ";
        $query .= "     STAFF_DETAIL_EXT_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$detailYear}' ";
        $query .= "     AND STAFFCD = '{$model->staffcd}' ";
        $query .= "     AND STAFF_SEQ = '{$staffSeq}' ";
        $query .= " ORDER BY ";
        $query .= "     EXT_SEQ ";

        return $query;
    }

    //学校取得
    function getEdSchool($model, $kyouikuIinkai) {

        $query  = " SELECT ";
        $query .= "     T1.EDBOARD_SCHOOLCD AS VALUE, ";
        $query .= "     T1.EDBOARD_SCHOOLCD || ':' || T1.EDBOARD_SCHOOLNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     EDBOARD_SCHOOL_MST T1 ";
        if ($kyouikuIinkai == 0) {
            $query .= " WHERE ";
            $query .= "     T1.EDBOARD_SCHOOLCD = '{$model->edboard_schoolcd}' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //名称マスタ取得
    function getNameMst($namecd1) {

        $query  = " SELECT ";
        $query .= "     T1.NAMECD2 AS VALUE, ";
        $query .= "     T1.NAMECD2 || ':' || T1.NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     NAME_MST T1 ";
        $query .= " WHERE ";
        $query .= "     T1.NAMECD1 = '{$namecd1}' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //V名称マスタ取得
    function getVNameMst($namecd1, $model) {

        $query  = " SELECT ";
        $query .= "     NAMECD2 AS VALUE, ";
        $query .= "     NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "         YEAR    = '".CTRL_YEAR."' ";
        $query .= "     AND NAMECD1 = '{$namecd1}' ";
        if ($namecd1 == "Z043" && $model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= "     AND NAMESPARE2 IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
            }
        } elseif ($namecd1 == "Z043" && $model->Properties["useSchool_KindField"] == "1") {
            $query .= "     AND NAMESPARE2 = '".SCHOOLKIND."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //課程取得
    function getCouser() {
        $query  = " SELECT ";
        $query .= "     COURSECD AS VALUE, ";
        $query .= "     COURSECD || ':' || COURSENAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     COURSE_MST ";

        return $query;
    }

    //年度データの確認
    function IsExisting($staffcd) {
        $query  = " SELECT "; 
        $query .= "     * "; 
        $query .= " FROM "; 
        $query .= "     STAFF_YDAT "; 
        $query .= " WHERE "; 
        $query .= "     STAFFCD = '".$staffcd."' "; 

        return $query;
    }

    //学校別教職員外テーブル存在チェック
    function chkStaffAnotherDat($staffcd) {
        $query  = " SELECT "; 
        $query .= "     * "; 
        $query .= " FROM "; 
        $query .= "     EDBOARD_STAFF_DAT "; 
        $query .= " WHERE "; 
        $query .= "     STAFFCD = '".$staffcd."' "; 

        return $query;
    }

    //県職員登録コード存在チェック
    function chkEdboardStaffcd($staffcd, $check="") {
        $query  = " SELECT "; 
        $query .= "     COUNT(*) AS CNT "; 
        $query .= " FROM "; 
        $query .= "     STAFF_MST "; 
        $query .= " WHERE "; 
        if ($check) {
            $query .= "     EDBOARD_STAFFCD = '".$staffcd."' ";
            if ($check != "GAKKOU") {
                $query .= " AND STAFFCD = '".$check."' ";
            }
        } else {
            $query .= "     STAFFCD = '".$staffcd."' ";
        }

        return $query;
    }

    //INSERT
    function &getInsertQuery($model, $fields) {
        $db  = Query::dbCheckOut();

        $data["STAFFCD"][TEXT]              = $fields["STAFFCD"];
        $data["STAFFNAME"][TEXT]            = $fields["STAFFNAME"];
        $data["STAFFNAME_SHOW"][TEXT]       = $fields["STAFFNAME_SHOW"];
        $data["STAFFNAME_KANA"][TEXT]       = $fields["STAFFNAME_KANA"];
        $data["STAFFNAME_ENG"][TEXT]        = $fields["STAFFNAME_ENG"];
        $data["STAFFNAME_REAL"][TEXT]       = $fields["STAFFNAME_REAL"];
        $data["STAFFNAME_KANA_REAL"][TEXT]  = $fields["STAFFNAME_KANA_REAL"];
        $data["JOBCD"][TEXT]                = $fields["JOBCD"];
        $data["SECTIONCD"][TEXT]            = $fields["SECTIONCD"];
        $data["DUTYSHARECD"][TEXT]          = $fields["DUTYSHARECD"];
        $data["CHARGECLASSCD"][TEXT]        = $fields["CHARGECLASSCD"];
        $data["STAFFSEX"][TEXT]             = $fields["STAFFSEX"];
        $data["STAFFBIRTHDAY"][TEXT]        = str_replace("/","-",$fields["STAFFBIRTHDAY"]);
        $data["STAFFZIPCD"][TEXT]           = $fields["STAFFZIPCD"];
        $data["STAFFADDR1"][TEXT]           = $fields["STAFFADDR1"];
        $data["STAFFADDR2"][TEXT]           = $fields["STAFFADDR2"];
        $data["STAFFTELNO"][TEXT]           = $fields["STAFFTELNO"];
        $data["STAFFFAXNO"][TEXT]           = $fields["STAFFFAXNO"];
        $data["STAFFE_MAIL"][TEXT]          = $fields["STAFFE_MAIL"];
        if ($model->sendSubmit === '1') {
            $data["EDBOARD_STAFFCD"][TEXT]      = $fields["EDBOARD_STAFFCD"];//学校側はテキストを更新
        } else {
            $data["EDBOARD_STAFFCD"][TEXT]      = $fields["STAFFCD"];        //県教育委員会側は同じSTAFFCDを更新
        }
        $data["REGISTERCD"][TEXT]           = STAFFCD;
        $data["UPDATED"][FUNC]              = "sysdate()";

        $query = Query::insertSQL($data, "STAFF_MST");
        $db->query($query);

        knjz291Query::setStaffDetail($db, $model, $fields);

        Query::dbCheckIn($db);
        return;
    }

    //INSERT
    function &insertStaffAnotherDat($model, $fields) {
        $db2 = Query::dbCheckOut2();

        $data["EDBOARD_SCHOOLCD"][TEXT]     = $model->edboard_schoolcd;
        $data["STAFFCD"][TEXT]              = $fields["STAFFCD"];
        $data["STAFF_DIV"][TEXT]            = ($model->iinkai) ? 1 : 2;
        $data["REGISTERCD"][TEXT]           = STAFFCD;
        $data["UPDATED"][FUNC]              = "sysdate()";

        $query = Query::insertSQL($data, "EDBOARD_STAFF_DAT");
        $db2->query($query);

        Query::dbCheckIn($db2);

        return;
    }

    //INSERT
    function &insertRirekiData($fields) {
        $db  = Query::dbCheckOut();
        $db->autoCommit(false);

        //STAFF_NAME_HIST_DAT
        $data = array();
        $data["STAFFCD"][TEXT]              = $fields["STAFFCD"];
        $data["SDATE"][TEXT]                = str_replace("/","-",$fields["NAME_SDATE"]);
        $data["STAFFNAME"][TEXT]            = $fields["STAFFNAME"];
        $data["STAFFNAME_SHOW"][TEXT]       = $fields["STAFFNAME_SHOW"];
        $data["STAFFNAME_KANA"][TEXT]       = $fields["STAFFNAME_KANA"];
        $data["STAFFNAME_ENG"][TEXT]        = $fields["STAFFNAME_ENG"];
        $data["STAFFNAME_REAL"][TEXT]       = $fields["STAFFNAME_REAL"];
        $data["STAFFNAME_KANA_REAL"][TEXT]  = $fields["STAFFNAME_KANA_REAL"];
        $data["REGISTERCD"][TEXT]           = STAFFCD;
        $data["UPDATED"][FUNC]              = "sysdate()";

        $query = Query::insertSQL($data, "STAFF_NAME_HIST_DAT");
        $db->query($query);

        //STAFF_ADDRESS_DAT
        $data = array();
        $data["STAFFCD"][TEXT]              = $fields["STAFFCD"];
        $data["SDATE"][TEXT]                = str_replace("/","-",$fields["ADDRESS_SDATE"]);
        $data["STAFFZIPCD"][TEXT]           = $fields["STAFFZIPCD"];
        $data["STAFFADDR1"][TEXT]           = $fields["STAFFADDR1"];
        $data["STAFFADDR2"][TEXT]           = $fields["STAFFADDR2"];
        $data["STAFFTELNO"][TEXT]           = $fields["STAFFTELNO"];
        $data["STAFFFAXNO"][TEXT]           = $fields["STAFFFAXNO"];
        $data["STAFFE_MAIL"][TEXT]          = $fields["STAFFE_MAIL"];
        $data["REGISTERCD"][TEXT]           = STAFFCD;
        $data["UPDATED"][FUNC]              = "sysdate()";

        $query = Query::insertSQL($data, "STAFF_ADDRESS_DAT");
        $db->query($query);

        $db->commit();  // トランザクションをコミットする。
        Query::dbCheckIn($db);
        return;
    }

    //UPDATE
    function &getUpdateQuery($model, $fields) {
        $db  = Query::dbCheckOut();
        $db2 = Query::dbCheckOut2();

        if ($model->sendSubmit != "") {
            $data["JOBCD"][TEXT]                = $fields["JOBCD"];
            $data["SECTIONCD"][TEXT]            = $fields["SECTIONCD"];
            $data["DUTYSHARECD"][TEXT]          = $fields["DUTYSHARECD"];
            $data["CHARGECLASSCD"][TEXT]        = $fields["CHARGECLASSCD"];
            $data["EDBOARD_STAFFCD"][TEXT]      = $fields["EDBOARD_STAFFCD"];//学校側はテキストを更新
        }
        $data["STAFFSEX"][TEXT]             = $fields["STAFFSEX"];
        $data["STAFFBIRTHDAY"][TEXT]        = str_replace("/","-",$fields["STAFFBIRTHDAY"]);
        $data["REGISTERCD"][TEXT]           = STAFFCD;
        $data["UPDATED"][FUNC]              = "sysdate()";

        $where = " WHERE STAFFCD            = '".$model->staffcd."' ";
        $query = Query::updateSQL($data, "STAFF_MST", $where);
        $db->query($query);

        knjz291Query::setStaffDetail($db, $model, $fields);

        Query::dbCheckIn($db);
        Query::dbCheckIn($db2);

        return;
    }

    function setStaffDetail($db, $model, $fields) {
        $detailYear = $model->year == "ALL" ? CTRL_YEAR : $model->year;
        $detailYear = $detailYear ? $detailYear : CTRL_YEAR;
        //職員詳細マスタの削除
        $query  = " DELETE FROM STAFF_DETAIL_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$detailYear}' ";
        $query .= "     AND STAFFCD = '{$fields["STAFFCD"]}' ";
        $db->query($query);

        $data = array();
        $data["YEAR"][TEXT]                 = $detailYear;
        $data["STAFFCD"][TEXT]              = $fields["STAFFCD"];
        $data["STAFF_SEQ"][TEXT]            = "001";
        $data["FIELD1"][TEXT]               = $fields["JOBCD"];
        $data["REGISTERCD"][TEXT]           = STAFFCD;
        $data["UPDATED"][FUNC]              = "sysdate()";
        $query = Query::insertSQL($data, "STAFF_DETAIL_MST");
        $db->query($query);

        $data = array();
        $data["YEAR"][TEXT]                 = $detailYear;
        $data["STAFFCD"][TEXT]              = $fields["STAFFCD"];
        $data["STAFF_SEQ"][TEXT]            = "002";
        $data["FIELD1"][TEXT]               = $fields["SECTIONCD"];
        $data["REGISTERCD"][TEXT]           = STAFFCD;
        $data["UPDATED"][FUNC]              = "sysdate()";
        $query = Query::insertSQL($data, "STAFF_DETAIL_MST");
        $db->query($query);

        $data = array();
        $data["YEAR"][TEXT]                 = $detailYear;
        $data["STAFFCD"][TEXT]              = $fields["STAFFCD"];
        $data["STAFF_SEQ"][TEXT]            = "003";
        $data["FIELD1"][TEXT]               = $fields["DUTYSHARECD"];
        $data["FIELD2"][TEXT]               = $fields["DUTYSHARECD2"];
        $data["REGISTERCD"][TEXT]           = STAFFCD;
        $data["UPDATED"][FUNC]              = "sysdate()";
        $query = Query::insertSQL($data, "STAFF_DETAIL_MST");
        $db->query($query);

        $data = array();
        $data["YEAR"][TEXT]                 = $detailYear;
        $data["STAFFCD"][TEXT]              = $fields["STAFFCD"];
        $data["STAFF_SEQ"][TEXT]            = "004";
        $data["FIELD1"][TEXT]               = $fields["CHARGECLASSCD"];
        $data["REGISTERCD"][TEXT]           = STAFFCD;
        $data["UPDATED"][FUNC]              = "sysdate()";
        $query = Query::insertSQL($data, "STAFF_DETAIL_MST");
        $db->query($query);

        knjz291Query::insDetailClassData($db, $model, $detailYear, $fields, "005", 1);
        knjz291Query::insDetailClassData($db, $model, $detailYear, $fields, "006", 2);
        knjz291Query::insDetailClassData($db, $model, $detailYear, $fields, "007", 3);
        if ($model->Properties["use_staff_detail_ext_mst"] == "1") {
            //職員詳細マスタの削除
            $query  = " DELETE FROM STAFF_DETAIL_EXT_MST ";
            $query .= " WHERE ";
            $query .= "     YEAR = '{$detailYear}' ";
            $query .= "     AND STAFFCD = '{$fields["STAFFCD"]}' ";
            $db->query($query);

            knjz291Query::insDetailClassExtData($db, $model, $detailYear, $fields, "005", 1);
            knjz291Query::insDetailClassExtData($db, $model, $detailYear, $fields, "006", 2);
            knjz291Query::insDetailClassExtData($db, $model, $detailYear, $fields, "007", 3);
        }

        $data = array();
        $data["YEAR"][TEXT]                 = $detailYear;
        $data["STAFFCD"][TEXT]              = $fields["STAFFCD"];
        $data["STAFF_SEQ"][TEXT]            = "008";
        $data["FIELD1"][TEXT]               = $fields["CHARGENURSEOFF"];
        $data["REGISTERCD"][TEXT]           = STAFFCD;
        $data["UPDATED"][FUNC]              = "sysdate()";
        $query = Query::insertSQL($data, "STAFF_DETAIL_MST");
        $db->query($query);
    }

    //教科登録
    function insDetailClassData($db, $model, $detailYear, $fields, $staffSeq, $positionNum) {
        $data = array();
        $data["YEAR"][TEXT]                 = $detailYear;
        $data["STAFFCD"][TEXT]              = $fields["STAFFCD"];
        $data["STAFF_SEQ"][TEXT]            = $staffSeq;
        $data["FIELD1"][TEXT]               = $fields["POSITIONCD{$positionNum}"];
        if ($fields["POSITIONCD{$positionNum}"] === '0200') {
            $data["FIELD2"][TEXT]               = $fields["POSITIONCD{$positionNum}_MANAGER"];
            $data["FIELD3"][TEXT]               = "";
        } else if ($fields["POSITIONCD{$positionNum}"] === '1050') {
            //教育課程対応
            if ($model->Properties["useCurriculumcd"] == '1') {
                list($classcd, $school_kind) = explode("-", $fields["POSITIONCD{$positionNum}_MANAGER"]);
                $data["FIELD2"][TEXT]               = $classcd;
                $data["FIELD3"][TEXT]               = $school_kind;
            } else {
                $data["FIELD2"][TEXT]               = $fields["POSITIONCD{$positionNum}_MANAGER"];
            }
        }
        $data["REGISTERCD"][TEXT]           = STAFFCD;
        $data["UPDATED"][FUNC]              = "sysdate()";
        $query = Query::insertSQL($data, "STAFF_DETAIL_MST");
        $db->query($query);
    }

    //教科登録
    function insDetailClassExtData($db, $model, $detailYear, $fields, $staffSeq, $positionNum) {
        $setExtSeq = 1;
        foreach ($this->positionArray[$positionNum] as $posiKey => $posiVal) {
            $data = array();
            $data["YEAR"][TEXT]                 = $detailYear;
            $data["STAFFCD"][TEXT]              = $fields["STAFFCD"];
            $data["STAFF_SEQ"][TEXT]            = $staffSeq;
            $data["EXT_SEQ"][NUMBER]            = $setExtSeq;
            $data["FIELD1"][TEXT]               = $fields["POSITIONCD{$positionNum}"];
            if ($fields["POSITIONCD{$positionNum}"] === '0200') {
                $data["FIELD2"][TEXT]               = $fields["POSITIONCD{$positionNum}_MANAGER"];
                $data["FIELD3"][TEXT]               = "";
            } else if ($fields["POSITIONCD{$positionNum}"] === '1050') {
                //教育課程対応
                if ($model->Properties["useCurriculumcd"] == '1') {
                    list($classcd, $school_kind) = explode("-", $posiVal);
                    $data["FIELD2"][TEXT]               = $classcd;
                    $data["FIELD3"][TEXT]               = $school_kind;
                } else {
                    $data["FIELD2"][TEXT]               = $posiVal;
                }
            }
            $data["REGISTERCD"][TEXT]           = STAFFCD;
            $data["UPDATED"][FUNC]              = "sysdate()";
            $query = Query::insertSQL($data, "STAFF_DETAIL_EXT_MST");
            $db->query($query);
            $setExtSeq++;
        }
    }

    //DELETE
    function &getDeleteQuery($model, $staffcd) {
        $db = Query::dbCheckOut();
        $db2 = Query::dbCheckOut2();

        $db->autoCommit(false);

        //STAFF_MST
        $query  = " DELETE FROM STAFF_MST ";
        $query .= " WHERE STAFFCD = '".$staffcd."' ";
        $db->query($query);

        //STAFF_NAME_HIST_DAT
        $query  = " DELETE FROM STAFF_NAME_HIST_DAT ";
        $query .= " WHERE STAFFCD = '".$staffcd."' ";
        $db->query($query);

        //STAFF_ADDRESS_DAT
        $query  = " DELETE FROM STAFF_ADDRESS_DAT ";
        $query .= " WHERE STAFFCD = '".$staffcd."' ";
        $db->query($query);

        //職員詳細マスタの削除
        $query  = " DELETE FROM STAFF_DETAIL_MST ";
        $query .= " WHERE STAFFCD = '".$staffcd."' ";
        $db->query($query);

        if ($model->Properties["use_staff_detail_ext_mst"] == "1") {
            //職員詳細マスタの削除
            $query  = " DELETE FROM STAFF_DETAIL_EXT_MST ";
            $query .= " WHERE STAFFCD = '".$staffcd."' ";
            $db->query($query);
        }

        //利用者マスタの削除
        $query  = " DELETE FROM USER_MST ";
        $query .= " WHERE STAFFCD = '".$staffcd."' ";
        $db->query($query);

        //利用者パスワード履歴マスタの削除
        $query  = " DELETE FROM USER_PWD_HIST_MST ";
        $query .= " WHERE STAFFCD = '".$staffcd."' ";
        $db->query($query);

        if ($model->sendSubmit == "") {

            //STAFF_REQUESTFORM_DAT
            $query  = " DELETE FROM STAFF_REQUESTFORM_DAT ";
            $query .= " WHERE STAFFCD = '".$staffcd."' ";
            $db->query($query);

            //STAFF_QUALIFIED_DAT
            $query  = " DELETE FROM STAFF_QUALIFIED_DAT ";
            $query .= " WHERE STAFFCD = '".$staffcd."' ";
            $db->query($query);

        } else {

            //STAFF_CLASS_MST
            $query  = " DELETE FROM STAFF_CLASS_MST ";
            $query .= " WHERE STAFFCD = '".$staffcd."' ";
            $db->query($query);

            //EDBOARD_STAFF_DAT
            $query  = " DELETE FROM EDBOARD_STAFF_DAT ";
            $query .= " WHERE EDBOARD_SCHOOLCD = '".$model->edboard_schoolcd."' ";
            $query .= "   AND STAFFCD = '".$staffcd."' ";
            $db2->query($query);
        }

        $db->commit();  // トランザクションをコミットする。

        Query::dbCheckIn($db);
        Query::dbCheckIn($db2);

        return true;
    }

    //--- 詳細データの最新でUPDATE 
    function &newDataUpdate($model, $fields)
    {        
        $db = Query::dbCheckOut();

        $query  = " WITH MAX_T AS ( ";
        $query .= " SELECT ";
        $query .= "     STAFFCD, ";
        $query .= "     MAX(YEAR) AS YEAR ";
        $query .= " FROM ";
        $query .= "     STAFF_DETAIL_MST ";
        $query .= " WHERE ";
        $query .= "     STAFFCD = '".$fields["STAFFCD"]."' ";
        $query .= " GROUP BY ";
        $query .= "     STAFFCD ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     T1.* ";
        $query .= " FROM ";
        $query .= "     STAFF_DETAIL_MST T1 ";
        $query .= " WHERE ";
        $query .= "     EXISTS (SELECT ";
        $query .= "                 'x' ";
        $query .= "             FROM ";
        $query .= "                 MAX_T E1 ";
        $query .= "             WHERE ";
        $query .= "                 T1.YEAR = E1.YEAR ";
        $query .= "                 AND T1.STAFFCD = E1.STAFFCD ";
        $query .= "            ) ";

        $result = $db->query($query);
        $data = array();
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            if ($row["STAFF_SEQ"] == "001") {
                $data["JOBCD"][TEXT] = $row["FIELD1"];
            } else if ($row["STAFF_SEQ"] == "002") {
                $data["SECTIONCD"][TEXT] = $row["FIELD1"];
            } else if ($row["STAFF_SEQ"] == "003") {
                $data["DUTYSHARECD"][TEXT] = $row["FIELD1"];
            } else if ($row["STAFF_SEQ"] == "004") {
                $data["CHARGECLASSCD"][TEXT] = $row["FIELD1"];
            }
        }
        $result->free();

        $data["REGISTERCD"][TEXT]   = STAFFCD;
        $data["UPDATED"][FUNC]      = "sysdate()";

        $where = "WHERE STAFFCD = '" .$fields["STAFFCD"] ."'";

        $query = Query::updateSQL($data, "STAFF_MST", $where);
        $db->query($query);

        Query::dbCheckIn($db);
        return;
    }

    /**********************/
    /*  資格教科登録画面  */
    /**********************/

    //１レコード取得
    function getRow2($staffcd, $classcd, $sdate, $model) {

        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     STAFF_CLASS_MST ";
        $query .= " WHERE ";
        $query .= "     STAFFCD     = '".$staffcd."' ";
        $query .= " AND CLASSCD     = '".$classcd."'";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= " AND SCHOOL_KIND = '" .$model->school_kind."' ";
        }
        $query .= " AND SDATE       = '".str_replace("/", "-", $sdate)."' ";
        $query .= " ORDER BY ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "      SCHOOL_KIND, ";
            $query .= "      CLASSCD ";
        } else {
            $query .= "      CLASSCD ";
        }

        return $query;
    }

    //職員情報取得
    function getStfInfo2($staffcd) {
        $query  = " SELECT ";
        $query .= "     T1.STAFFCD, ";
        $query .= "     T1.STAFFNAME, ";
        $query .= "     T2.CLASSCD, ";
        $query .= "     T2.SDATE, ";
        $query .= "     T2.EDATE ";
        $query .= " FROM ";
        $query .= "     STAFF_MST T1 ";
        $query .= "     LEFT JOIN STAFF_CLASS_MST T2 ON T1.STAFFCD = T2.STAFFCD ";
        $query .= " WHERE ";
        $query .= "     T1.STAFFCD = '".$staffcd."' ";
        $query .= " ORDER BY ";
        $query .= "     T1.STAFFCD, ";
        $query .= "     T2.CLASSCD ";

        return $query;
    }

    //教科データの取得
    function getClassSdateEdate($staffcd, $model) {
        $query  = " SELECT ";
        $query .= "     T2.CLASSNAME, ";
        $query .= "     T1.CLASSCD, ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     T1.SCHOOL_KIND, ";
        }
        $query .= "     T1.SDATE, ";
        $query .= "     T1.EDATE ";
        $query .= " FROM ";
        $query .= "     STAFF_CLASS_MST T1 ";
        $query .= "     LEFT JOIN CLASS_MST T2 ON T1.CLASSCD = T2.CLASSCD ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "                       AND T1.SCHOOL_KIND   = T2.SCHOOL_KIND ";
        }
        $query .= " WHERE ";
        $query .= "     T1.STAFFCD = '".$staffcd."' ";
        $query .= " ORDER BY ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "      T1.SCHOOL_KIND, ";
            $query .= "      T1.CLASSCD ";
        } else {
            $query .= "      T1.CLASSCD ";
        }

        return $query;
    }

    //教科名取得
    function getName($model) {
        $query  = "  SELECT ";
        $query .= "      T1.CLASSCD, ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     T1.SCHOOL_KIND, ";
        }
        $query .= "      T1.CLASSNAME ";
        $query .= "  FROM ";
        $query .= "      CLASS_MST T1 ";
        $query .= "  ORDER BY ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "      T1.SCHOOL_KIND, ";
            $query .= "      T1.CLASSCD ";
        } else {
            $query .= "      T1.CLASSCD ";
        }

        return $query;
    }

    //マスタメンテの重複確認用関数
    function get_cnt_Classcd($staffcd, $classcd, $model) {
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $class_array = array();
            $class_array = explode("-", $classcd);
        }
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     STAFF_CLASS_MST ";
        $query .= " WHERE ";
        $query .= "     STAFFCD = '".$staffcd."' AND ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     CLASSCD          = '".$class_array[0]."' AND ";
            $query .= "     SCHOOL_KIND      = '".$class_array[1]."' ";
        } else {
            $query .= "     CLASSCD  = '".$classcd."' ";
        }

        return $query;
    }

    //教育課程重複エラーチェック１レコード取得
    function getRow2check($staffcd, $classcd, $sdate, $model) {
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $class_array = array();
            $class_array = explode("-", $classcd);
        }

        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     STAFF_CLASS_MST ";
        $query .= " WHERE ";
        $query .= "     STAFFCD     = '".$staffcd."' ";
        $query .= " AND CLASSCD     = '".$class_array[0]."' ";
        $query .= " AND SCHOOL_KIND = '".$class_array[1]."' ";
        $query .= " AND SDATE       = '".str_replace("/", "-", $sdate)."' ";

        return $query;
    }

    //STAFF_CLASS_MST追加
    function &getInsert2Query($fields, $model) {
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $class_array = array();
            $class_array = explode("-", $fields["CLASSCD"]);
        }
        $db = Query::dbCheckOut();

        $data["STAFFCD"][TEXT]              = $fields["STAFFCD"];
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $data["CLASSCD"][TEXT]        = $class_array[0];
            $data["SCHOOL_KIND"][TEXT]    = $class_array[1];
        } else {
            $data["CLASSCD"][TEXT]              = $fields["CLASSCD"];
        }
        $data["SDATE"][TEXT]                = str_replace("/","-",$fields["SDATE"]);
        $data["EDATE"][TEXT]                = str_replace("/","-",$fields["EDATE"]);
        $data["REGISTERCD"][TEXT]           = STAFFCD;
        $data["UPDATED"][FUNC]              = "sysdate()";

        $query = Query::insertSQL($data, "STAFF_CLASS_MST");
        $db->query($query);

        Query::dbCheckIn($db);
        return;
    }

    //STAFF_CLASS_MST更新
    function &getUpdate2Query($model) {
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $class_array = array();
            $class_array = explode("-", $model->subField2["CLASSCD"]);
        }
        $db = Query::dbCheckOut();

        $data["EDATE"][TEXT]            = str_replace("/","-",$model->subField2["EDATE"]);
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][FUNC]          = "sysdate()";

        $where  = " WHERE ";
        $where .= "     STAFFCD         = '".$model->staffcd."' ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $where .= " AND CLASSCD         = '".$class_array[0]."' ";
            $where .= " AND SCHOOL_KIND     = '".$class_array[1]."' ";
        } else {
            $where .= " AND CLASSCD         = '".$model->subField2["CLASSCD"]."' ";
        }
        $where .= " AND SDATE           = '".str_replace("/","-",$model->subField2["SDATE"])."' ";

        $query = Query::updateSQL($data, "STAFF_CLASS_MST", $where);
        $db->query($query);

        Query::dbCheckIn($db);
        return;
    }

    //STAFF_CLASS_MST削除
    function &getDelete2Query($model) {
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $class_array = array();
            $class_array = explode("-", $model->subField2["CLASSCD"]);
        }
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $query  = " DELETE FROM ";
        $query .= "     STAFF_CLASS_MST ";
        $query .= " WHERE ";
        $query .= "     STAFFCD     = '".$model->staffcd."' ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= " AND CLASSCD     = '".$class_array[0]."' ";
            $query .= " AND SCHOOL_KIND = '".$class_array[1]."' ";
        } else {
            $query .= " AND CLASSCD     = '".$model->subField2["CLASSCD"]."' ";
        }
        $query .= " AND SDATE       = '".str_replace("/","-",$model->subField2["SDATE"])."' ";
        $query .= " AND EDATE       = '".str_replace("/","-",$model->subField2["EDATE"])."' ";
        $db->query($query);

        $db->commit(); // トランザクションをコミットする。
        Query::dbCheckIn($db);
        return true;
    }

    /**
     * 前年度コピー(職員詳細データ)処理
    */
    function &getCopyDetail($model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        for ($i = 1; $i <= 8; $i++) {
            //対象のSTAFF_SEQをセット
            $setSeq = sprintf ("%03d", $i);
            //対象の前年度データを取得
            $result = $db->query(knjz291Query::getCopyList($model, $setSeq));
            while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $checkCountdata = $db->getOne(knjz291Query::getCheckCountData($model, $setSeq, $row["STAFFCD"]));
                if ($checkCountdata > 0) {
                    $checkUpdata = $db->getOne(knjz291Query::getCheckCountData($model, $setSeq, $row["STAFFCD"], "UPDATE"));
                //Insert処理
                } else {
                    knjz291Query::getInsertCopyQuery($db, $row, $model);
                }
                //Update処理
                if ($checkUpdata > 0 && $checkUpdata == "1") {
                    knjz291Query::getUpdateCopyQuery($db, $row, $model);
                }
            }
            $result->free();
        }

        //STAFF_DETAIL_EXT_MST
        if ($model->Properties["use_staff_detail_ext_mst"] == "1") {
            for ($i = 5; $i <= 7; $i++) {
                //対象のSTAFF_SEQをセット
                $setSeq = sprintf ("%03d", $i);

                //対象の前年度データを取得
                $result = $db->query(knjz291Query::getCopyListExt($model, $setSeq));
                while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                    //今年度データ有無
                    $thisCnt = $db->getOne(knjz291Query::getCheckCountDataExt($model, $setSeq, $row["STAFFCD"]));
                    if ($thisCnt > 0) {
                        continue;
                    }
                    //対象の前年度データを取得
                    knjz291Query::getInsertCopyQueryExt($db, $row, $model);
                }
                $result->free();
            }
        }

        $db->commit(); // トランザクションをコミットする。
        Query::dbCheckIn($db);
        return true;
    }

    //コピー元対象データ取得
    function getCopyList($model, $setSeq) {
        $setOldYear = $model->year - 1;
        $query  = " SELECT ";
        $query .= "     '".$model->year."', ";
        $query .= "     T1.STAFFCD, ";
        $query .= "     T1.STAFF_SEQ, ";
        $query .= "     T1.FIELD1, ";
        $query .= "     T1.FIELD2, ";
        $query .= "     T1.FIELD3, ";
        $query .= "     T1.FIELD4, ";
        $query .= "     T1.FIELD5 ";
        $query .= " FROM ";
        $query .= "     STAFF_DETAIL_MST T1 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".$setOldYear."' ";
        $query .= " AND T1.STAFF_SEQ = '".$setSeq."' ";
        $query .= " AND (T1.FIELD1 IS NOT NULL ";
        $query .= " OR   T1.FIELD2 IS NOT NULL ";
        $query .= " OR   T1.FIELD3 IS NOT NULL ";
        $query .= " OR   T1.FIELD4 IS NOT NULL ";
        $query .= " OR   T1.FIELD5 IS NOT NULL) ";
        //該当の先生が昨年度/当年度共に有効か、チェック。
        $query .= " AND  T1.STAFFCD IN (SELECT STAFFCD FROM STAFF_YDAT WHERE YEAR = '".$setOldYear."') ";
        $query .= " AND  T1.STAFFCD IN (SELECT STAFFCD FROM STAFF_YDAT WHERE YEAR = '".$model->year."') ";
        return $query;
    }

    //今年度データをカウント
    function getCheckCountData($model, $setSeq, $staffcd, $upflg="") {
        $query  = " SELECT ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     STAFF_DETAIL_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$model->year."' ";
        $query .= " AND STAFF_SEQ = '".$setSeq."' ";
        $query .= " AND STAFFCD = '".$staffcd."' ";
        if ($upflg != "") {
            $query .= " AND FIELD1 IS NULL ";
            $query .= " AND FIELD2 IS NULL ";
            $query .= " AND FIELD3 IS NULL ";
            $query .= " AND FIELD4 IS NULL ";
            $query .= " AND FIELD5 IS NULL ";
        }

        return $query;
    }

    //詳細データ Insert(コピー処理)
    function &getInsertCopyQuery($db, $row, $model)
    {
        $data = array();
        $data["YEAR"][TEXT]          = $model->year;
        $data["STAFFCD"][TEXT]       = $row["STAFFCD"];
        $data["STAFF_SEQ"][TEXT]     = $row["STAFF_SEQ"];
        $data["FIELD1"][TEXT]        = $row["FIELD1"];
        $data["FIELD2"][TEXT]        = $row["FIELD2"];
        $data["FIELD3"][TEXT]        = $row["FIELD3"];
        $data["FIELD4"][TEXT]        = $row["FIELD4"];
        $data["FIELD5"][TEXT]        = $row["FIELD5"];
        $data["REGISTERCD"][TEXT]    = STAFFCD;
        $data["UPDATED"][FUNC]       = "sysdate()";
        $query = Query::insertSQL($data, "STAFF_DETAIL_MST");
        $db->query($query);

        return;
    }

    //詳細データ Update(コピー処理)
    function &getUpdateCopyQuery($db, $row, $model)
    {
        $data = array();
        $data["FIELD1"][TEXT]        = $row["FIELD1"];
        $data["FIELD2"][TEXT]        = $row["FIELD2"];
        $data["FIELD3"][TEXT]        = $row["FIELD3"];
        $data["FIELD4"][TEXT]        = $row["FIELD4"];
        $data["FIELD5"][TEXT]        = $row["FIELD5"];
        $data["REGISTERCD"][TEXT]    = STAFFCD;
        $data["UPDATED"][FUNC]       = "sysdate()";
        
        $where  = " WHERE ";
        $where .= "     YEAR = '".$model->year."' ";
        $where .= " AND STAFF_SEQ = '".$row["STAFF_SEQ"]."' ";
        $where .= " AND STAFFCD = '".$row["STAFFCD"]."' ";
        
        $query = Query::updateSQL($data, "STAFF_DETAIL_MST", $where);
        
        $db->query($query);

        return;
    }

    //データをカウント
    function getCopyListExt($model, $setSeq) {
        $setOldYear = $model->year - 1;
        $query  = " SELECT ";
        $query .= "     T1.STAFFCD, ";
        $query .= "     T1.STAFF_SEQ, ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     STAFF_DETAIL_EXT_MST T1 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".$setOldYear."' ";
        $query .= " AND T1.STAFF_SEQ = '".$setSeq."' ";
        $query .= " AND (T1.FIELD1 IS NOT NULL ";
        $query .= " OR   T1.FIELD2 IS NOT NULL ";
        $query .= " OR   T1.FIELD3 IS NOT NULL ";
        $query .= " OR   T1.FIELD4 IS NOT NULL ";
        $query .= " OR   T1.FIELD5 IS NOT NULL) ";
        //該当の先生が昨年度/当年度共に有効か、チェック。
        $query .= " AND  T1.STAFFCD IN (SELECT STAFFCD FROM STAFF_YDAT WHERE YEAR = '".$setOldYear."') ";
        $query .= " AND  T1.STAFFCD IN (SELECT STAFFCD FROM STAFF_YDAT WHERE YEAR = '".$model->year."') ";
        $query .= " GROUP BY ";
        $query .= "     T1.STAFFCD, ";
        $query .= "     T1.STAFF_SEQ ";
        $query .= " HAVING ";
        $query .= "     COUNT(*) > 0 ";
        return $query;
    }

    //コピー先カウント
    function getCheckCountDataExt($model, $setSeq, $staffcd) {
        $query  = " SELECT ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     STAFF_DETAIL_EXT_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$model->year."' ";
        $query .= " AND STAFF_SEQ = '".$setSeq."' ";
        $query .= " AND STAFFCD = '".$staffcd."' ";
        $query .= " AND (FIELD1 IS NOT NULL ";
        $query .= " OR   FIELD2 IS NOT NULL ";
        $query .= " OR   FIELD3 IS NOT NULL ";
        $query .= " OR   FIELD4 IS NOT NULL ";
        $query .= " OR   FIELD5 IS NOT NULL) ";

        return $query;
    }

    //詳細データ Insert(コピー処理)
    function &getInsertCopyQueryExt($db, $row, $model)
    {
        $query = knjz291Query::getCopyDataExt($model, $row);
        $resultCopy = $db->query($query);
        while ($rowCopy = $resultCopy->fetchRow(DB_FETCHMODE_ASSOC)) {
            $data = array();
            $data["YEAR"][TEXT]          = $model->year;
            $data["STAFFCD"][TEXT]       = $rowCopy["STAFFCD"];
            $data["STAFF_SEQ"][TEXT]     = $rowCopy["STAFF_SEQ"];
            $data["EXT_SEQ"][NUMBER]     = $rowCopy["EXT_SEQ"];
            $data["FIELD1"][TEXT]        = $rowCopy["FIELD1"];
            $data["FIELD2"][TEXT]        = $rowCopy["FIELD2"];
            $data["FIELD3"][TEXT]        = $rowCopy["FIELD3"];
            $data["FIELD4"][TEXT]        = $rowCopy["FIELD4"];
            $data["FIELD5"][TEXT]        = $rowCopy["FIELD5"];
            $data["REGISTERCD"][TEXT]    = STAFFCD;
            $data["UPDATED"][FUNC]       = "sysdate()";

            $query = Query::insertSQL($data, "STAFF_DETAIL_EXT_MST");
            $db->query($query);
        }
        $resultCopy->free();

        return;
    }

    //コピー元データ取得
    function getCopyDataExt($model, $row) {
        $setOldYear = $model->year - 1;
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     STAFF_DETAIL_EXT_MST T1 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".$setOldYear."' ";
        $query .= "     AND T1.STAFFCD = '".$row["STAFFCD"]."' ";
        $query .= "     AND T1.STAFF_SEQ = '".$row["STAFF_SEQ"]."' ";
        return $query;
    }

    //職員履歴
    function getEdWorkCheck($fields) {
        $query .= " SELECT ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     EDBOARD_STAFF_WORK_HIST_DAT ";
        $query .= " WHERE ";
        $query .= "     STAFFCD = '{$fields["STAFFCD"]}' ";
        $query .= "     AND FROM_DATE = '".str_replace("/","-",$fields["FROM_DATE"])."' ";

        return $query;
    }

    //INS OR UPD
    function edWorkUpd($model, $fields) {
        $db2 = Query::dbCheckOut2();
        $db2->autoCommit(false);

        $query = knjz291Query::getEdWorkCheck($fields);
        $dataCnt = $db2->getOne($query);

        if ($dataCnt > 0) {
            $query  = " DELETE FROM ";
            $query .= "     EDBOARD_STAFF_WORK_HIST_DAT ";
            $query .= " WHERE ";
            $query .= "     STAFFCD     = '{$fields["STAFFCD"]}' ";
            $query .= "     AND FROM_DATE = '".str_replace("/","-",$fields["FROM_DATE"])."' ";

            $db2->query($query);
        }

        $data = array();
        $data["STAFFCD"][TEXT]          = $fields["STAFFCD"];
        $data["FROM_DATE"][DATE]        = $fields["FROM_DATE"];
        $data["WORK_DIV"][TEXT]         = "0";
        $data["FROM_DIV"][TEXT]         = $fields["FROM_DIV"];
        $data["FROM_SCHOOLCD"][TEXT]    = $fields["FROM_SCHOOLCD"];
        $data["FROM_COURSECD"][TEXT]    = $fields["FROM_COURSECD"];
        $data["TO_DATE"][DATE]          = $fields["TO_DATE"];
        $data["TO_DIV"][TEXT]           = $fields["TO_DIV"];
        $data["TO_SCHOOLCD"][TEXT]      = $fields["TO_SCHOOLCD"];
        $data["TO_COURSECD"][TEXT]      = $fields["TO_COURSECD"];
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][FUNC]          = "sysdate()";
        $query = Query::insertSQL($data, "EDBOARD_STAFF_WORK_HIST_DAT");
        $db2->query($query);

        $db2->commit();
        Query::dbCheckIn($db2);

        return;
    }

    //DEL
    function edWorkDel($model, $fields) {
        $db2 = Query::dbCheckOut2();
        $db2->autoCommit(false);

        $query  = " DELETE FROM ";
        $query .= "     EDBOARD_STAFF_WORK_HIST_DAT ";
        $query .= " WHERE ";
        $query .= "     STAFFCD     = '{$fields["STAFFCD"]}' ";

        $db2->query($query);

        $db2->commit();
        Query::dbCheckIn($db2);

        return;
    }
}
?>
