<?php

require_once('for_php7.php');

class knjb1218query extends Query {
    //生徒情報取得
    function getStudentInfoData($model) {
        $query  = " SELECT ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     T1.YEAR, ";
        $query .= "     T1.ANNUAL, ";
        $query .= "     L1.COURSENAME, ";
        $query .= "     L2.MAJORNAME, ";
        $query .= "     T6.STAFFNAME, ";
        $query .= "     CASE WHEN B2.BASE_REMARK1 IS NULL THEN B1.HANDICAP ELSE B2.BASE_REMARK1 END AS HANDICAP, ";
        $query .= "     T1.GRADE, ";
        $query .= "     G1.SCHOOL_KIND ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1 ";
        $query .= "     LEFT JOIN COURSE_MST L1 ON T1.COURSECD = L1.COURSECD ";
        $query .= "     LEFT JOIN MAJOR_MST L2 ";
        $query .= "          ON T1.COURSECD     = L2.COURSECD ";
        $query .= "         AND T1.MAJORCD      = L2.MAJORCD ";
        $query .= "     LEFT JOIN SCHREG_REGD_HDAT H1 ";
        $query .= "          ON T1.YEAR         = H1.YEAR ";
        $query .= "         AND T1.SEMESTER     = H1.SEMESTER ";
        $query .= "         AND T1.GRADE        = H1.GRADE ";
        $query .= "         AND T1.HR_CLASS     = H1.HR_CLASS ";
        $query .= "     LEFT JOIN STAFF_MST T6 ON H1.TR_CD1 = T6.STAFFCD ";
        $query .= "     LEFT JOIN SCHREG_BASE_MST B1 ON B1.SCHREGNO = T1.SCHREGNO ";
        $query .= "     LEFT JOIN SCHREG_BASE_YEAR_DETAIL_MST B2 ";
        $query .= "          ON B2.SCHREGNO     = T1.SCHREGNO ";
        $query .= "         AND B2.YEAR         = T1.YEAR ";
        $query .= "         AND B2.BASE_SEQ     = '004' ";
        $query .= "     LEFT JOIN SCHREG_REGD_GDAT G1 ";
        $query .= "          ON T1.YEAR         = G1.YEAR ";
        $query .= "         AND T1.GRADE        = G1.GRADE ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR     = '".CTRL_YEAR."' AND ";
        $query .= "     T1.SEMESTER = '".CTRL_SEMESTER."' AND ";
        $query .= "     T1.SCHREGNO = '".$model->schregno."' ";

        return $query;
    }

    //明細一覧取得
    function getMeisaiQuery($model, $schData) {
        list($grade, $school_kind, $handicap) = $schData;

        $query  = " WITH SCH_SUBCLASS AS ( ";
        if ($handicap != '003') {
            if ($school_kind == 'H') {
                $query .= " SELECT ";
                $query .= "     T1.YEAR, ";
                $query .= "     T1.SCHREGNO, ";
                $query .= "     T2.CLASSCD, ";
                $query .= "     T2.SCHOOL_KIND, ";
                $query .= "     T2.CURRICULUM_CD, ";
                $query .= "     T2.SUBCLASSCD ";
                $query .= " FROM ";
                $query .= "     SCHREG_REGD_DAT T1, ";
                $query .= "     CREDIT_MST T2 ";
                $query .= " WHERE ";
                $query .= "     T1.YEAR     = T2.YEAR AND ";
                $query .= "     T1.YEAR     = '".CTRL_YEAR."' AND ";
                $query .= "     T1.SEMESTER = '".CTRL_SEMESTER."' AND ";
                $query .= "     T1.SCHREGNO = '".$model->schregno."' AND ";
                $query .= "     T1.COURSECD = T2.COURSECD AND ";
                $query .= "     T1.MAJORCD  = T2.MAJORCD AND ";
                $query .= "     T1.GRADE    = T2.GRADE AND ";
                $query .= "     T1.COURSECODE = T2.COURSECODE AND ";
                $query .= "     T2.CLASSCD  < '90' ";
            } else {
                $query .= " SELECT DISTINCT ";
                $query .= "     T1.YEAR, ";
                $query .= "     T1.SCHREGNO, ";
                $query .= "     T2.CLASSCD, ";
                $query .= "     T2.SCHOOL_KIND, ";
                $query .= "     T2.CURRICULUM_CD, ";
                $query .= "     T2.SUBCLASSCD ";
                $query .= " FROM  ";
                $query .= "     SCHREG_REGD_DAT T1, ";
                $query .= "     JVIEWNAME_GRADE_MST T2 ";
                $query .= " WHERE ";
                $query .= "     T1.YEAR     = '".CTRL_YEAR."' AND ";
                $query .= "     T1.SEMESTER = '".CTRL_SEMESTER."' AND ";
                $query .= "     T1.SCHREGNO = '".$model->schregno."' AND ";
                $query .= "     T1.GRADE    = T2.GRADE AND ";
                $query .= "     T2.CLASSCD  < '90' ";
            }
        } else {
            $query .= " SELECT DISTINCT ";
            $query .= "     T1.YEAR, ";
            $query .= "     T1.SCHREGNO, ";
            $query .= "     T2.CLASSCD, ";
            $query .= "     T2.SCHOOL_KIND, ";
            $query .= "     T2.CURRICULUM_CD, ";
            $query .= "     T2.SUBCLASSCD ";
            $query .= " FROM ";
            $query .= "     SCHREG_REGD_DAT T1, ";
            $query .= "     V_SUBCLASS_MST T2 ";
            if ($model->Properties["useSpecial_Support_Hrclass"] == '1') {
                $query .= "     LEFT JOIN CLASS_DETAIL_MST L1 ";
                $query .= "          ON L1.CLASSCD      = T2.CLASSCD ";
                $query .= "         AND L1.SCHOOL_KIND  = T2.SCHOOL_KIND ";
                $query .= "         AND L1.CLASS_SEQ    = '001' ";
            }
            $query .= " WHERE ";
            $query .= "     T1.YEAR     = T2.YEAR AND ";
            $query .= "     T1.YEAR     = '".CTRL_YEAR."' AND ";
            $query .= "     T1.SEMESTER = '".CTRL_SEMESTER."' AND ";
            $query .= "     T1.SCHREGNO = '".$model->schregno."' AND ";
            $query .= "     T2.CLASSCD < '90' ";
            if ($model->Properties["useSpecial_Support_Hrclass"] == '1') {
                $query .= " AND VALUE(L1.CLASS_REMARK1,'') != '1' ";
            }
            if ($model->Properties["use_prg_schoolkind"] == "1") {
                if ($model->selectSchoolKind) {
                    $query .= " AND T2.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
                }
            } else if ($model->Properties["useSchool_KindField"] == "1") {
                $query .= " AND T2.SCHOOL_KIND = '".SCHOOLKIND."' ";
            }
        }
        $query .= " ) ";

        $query .= " SELECT DISTINCT ";
        $query .= "     T1.CLASSCD, ";
        $query .= "     T1.SCHOOL_KIND, ";
        $query .= "     T1.CURRICULUM_CD, ";
        $query .= "     T1.SUBCLASSCD, ";
        $query .= "     L1.SUBCLASSNAME, ";
        $query .= "     L2.TEXTBOOKCD, ";
        $query .= "     L3.TEXTBOOKNAME, ";
        $query .= "     L4.ISSUECOMPANYNAME, ";
        $query .= "     L3.TEXTBOOKUNITPRICE, ";
        $query .= "     L2.NOT_DEFAULT, ";
        $query .= "     L5.NAMESPARE1 AS DIV ";
        $query .= " FROM ";
        $query .= "     SCH_SUBCLASS T1 ";
        $query .= "     LEFT JOIN SUBCLASS_MST L1 ";
        $query .= "          ON T1.CLASSCD          = L1.CLASSCD ";
        $query .= "         AND T1.SCHOOL_KIND      = L1.SCHOOL_KIND ";
        $query .= "         AND T1.CURRICULUM_CD    = L1.CURRICULUM_CD ";
        $query .= "         AND T1.SUBCLASSCD       = L1.SUBCLASSCD ";
        $query .= "     LEFT JOIN SUBCLASS_TEXTBOOK_DAT L2 ";
        $query .= "          ON T1.YEAR             = L2.YEAR ";
        $query .= "         AND T1.CLASSCD          = L2.CLASSCD ";
        $query .= "         AND T1.SCHOOL_KIND      = L2.SCHOOL_KIND ";
        $query .= "         AND T1.CURRICULUM_CD    = L2.CURRICULUM_CD ";
        $query .= "         AND T1.SUBCLASSCD       = L2.SUBCLASSCD ";
        $query .= "     LEFT JOIN TEXTBOOK_MST L3 ON L2.TEXTBOOKCD = L3.TEXTBOOKCD ";
        $query .= "     LEFT JOIN ISSUECOMPANY_MST L4 ON L3.ISSUECOMPANYCD = L4.ISSUECOMPANYCD ";
        $query .= "     LEFT JOIN V_NAME_MST L5 ";
        $query .= "          ON T1.YEAR             = L5.YEAR ";
        $query .= "         AND L5.NAMECD1          = 'M004' ";
        $query .= "         AND L3.TEXTBOOKDIV      = L5.NAMECD2 ";
        $query .= " ORDER BY ";
        $query .= "     T1.CLASSCD, ";
        $query .= "     T1.SCHOOL_KIND, ";
        $query .= "     T1.CURRICULUM_CD, ";
        $query .= "     T1.SUBCLASSCD, ";
        $query .= "     L2.TEXTBOOKCD ";

        return $query;
    }

    //学籍教科書購入データ取得
    function getSchregTextbookSubclassDatCnt($model) {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     SCHREG_TEXTBOOK_SUBCLASS_DAT ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO = '".$model->schregno."' ";

        return $query;
    }

    //教科書発注済み
    function getSchregTextbookFinDat($model) {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     SCHREG_TEXTBOOK_CHKFIN_DAT ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO    = '".$model->schregno."' AND ";
        $query .= "     YEAR        = '".CTRL_YEAR."' AND ";
        $query .= "     VALUE(EXECUTED, '0') = '1' ";

        return $query;
    }

    //科目別教科書データの件数取得
    function getSubclassTextbookDatCnt($model, $row, $schData) {
        list($grade, $school_kind, $handicap) = $schData;

        $query  = " WITH SCH_SUBCLASS AS ( ";
        if ($handicap != '003') {
            if ($school_kind == 'H') {
                $query .= " SELECT ";
                $query .= "     T1.YEAR, ";
                $query .= "     T1.SCHREGNO, ";
                $query .= "     T2.CLASSCD, ";
                $query .= "     T2.SCHOOL_KIND, ";
                $query .= "     T2.CURRICULUM_CD, ";
                $query .= "     T2.SUBCLASSCD ";
                $query .= " FROM ";
                $query .= "     SCHREG_REGD_DAT T1, ";
                $query .= "     CREDIT_MST T2 ";
                $query .= " WHERE ";
                $query .= "     T1.YEAR     = T2.YEAR AND ";
                $query .= "     T1.YEAR     = '".CTRL_YEAR."' AND ";
                $query .= "     T1.SEMESTER = '".CTRL_SEMESTER."' AND ";
                $query .= "     T1.SCHREGNO = '".$model->schregno."' AND ";
                $query .= "     T1.COURSECD = T2.COURSECD AND ";
                $query .= "     T1.MAJORCD  = T2.MAJORCD AND ";
                $query .= "     T1.GRADE    = T2.GRADE AND ";
                $query .= "     T1.COURSECODE = T2.COURSECODE AND ";
                $query .= "     T2.CLASSCD  < '90' ";
            } else {
                $query .= " SELECT DISTINCT ";
                $query .= "     T1.YEAR, ";
                $query .= "     T1.SCHREGNO, ";
                $query .= "     T2.CLASSCD, ";
                $query .= "     T2.SCHOOL_KIND, ";
                $query .= "     T2.CURRICULUM_CD, ";
                $query .= "     T2.SUBCLASSCD ";
                $query .= " FROM  ";
                $query .= "     SCHREG_REGD_DAT T1, ";
                $query .= "     JVIEWNAME_GRADE_MST T2 ";
                $query .= " WHERE ";
                $query .= "     T1.YEAR     = '".CTRL_YEAR."' AND ";
                $query .= "     T1.SEMESTER = '".CTRL_SEMESTER."' AND ";
                $query .= "     T1.SCHREGNO = '".$model->schregno."' AND ";
                $query .= "     T1.GRADE    = T2.GRADE AND ";
                $query .= "     T2.CLASSCD  < '90' ";
            }
        } else {
            $query .= " SELECT DISTINCT ";
            $query .= "     T1.YEAR, ";
            $query .= "     T1.SCHREGNO, ";
            $query .= "     T2.CLASSCD, ";
            $query .= "     T2.SCHOOL_KIND, ";
            $query .= "     T2.CURRICULUM_CD, ";
            $query .= "     T2.SUBCLASSCD ";
            $query .= " FROM ";
            $query .= "     SCHREG_REGD_DAT T1, ";
            $query .= "     V_SUBCLASS_MST T2 ";
            if ($model->Properties["useSpecial_Support_Hrclass"] == '1') {
                $query .= "     LEFT JOIN CLASS_DETAIL_MST L1 ";
                $query .= "          ON L1.CLASSCD     = T2.CLASSCD ";
                $query .= "         AND L1.SCHOOL_KIND = T2.SCHOOL_KIND ";
                $query .= "         AND L1.CLASS_SEQ   = '001' ";
            }
            $query .= " WHERE ";
            $query .= "     T1.YEAR     = T2.YEAR AND ";
            $query .= "     T1.YEAR     = '".CTRL_YEAR."' AND ";
            $query .= "     T1.SEMESTER = '".CTRL_SEMESTER."' AND ";
            $query .= "     T1.SCHREGNO = '".$model->schregno."' AND ";
            $query .= "     T2.CLASSCD < '90' ";
            if ($model->Properties["useSpecial_Support_Hrclass"] == '1') {
                $query .= " AND VALUE(L1.CLASS_REMARK1,'') != '1' ";
            }
            if ($model->Properties["use_prg_schoolkind"] == "1") {
                if ($model->selectSchoolKind) {
                    $query .= " AND T2.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
                }
            } else if ($model->Properties["useSchool_KindField"] == "1") {
                $query .= " AND T2.SCHOOL_KIND = '".SCHOOLKIND."' ";
            }
        }
        $query .= " ) ";

        $query .= " SELECT ";
        $query .= "     T1.CLASSCD, ";
        $query .= "     T1.SCHOOL_KIND, ";
        $query .= "     T1.CURRICULUM_CD, ";
        $query .= "     T1.SUBCLASSCD, ";
        $query .= "     T2.TEXTBOOKCD, ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     SCH_SUBCLASS T1, ";
        $query .= "     SUBCLASS_TEXTBOOK_DAT T2 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR             = T2.YEAR AND ";
        $query .= "     T1.CLASSCD          = T2.CLASSCD AND ";
        $query .= "     T1.SCHOOL_KIND      = T2.SCHOOL_KIND AND ";
        $query .= "     T1.CURRICULUM_CD    = T2.CURRICULUM_CD AND ";
        $query .= "     T1.SUBCLASSCD       = T2.SUBCLASSCD ";
        $query .= " GROUP BY ";
        $query .= "     T1.CLASSCD, ";
        $query .= "     T1.SCHOOL_KIND, ";
        $query .= "     T1.CURRICULUM_CD, ";
        $query .= "     T1.SUBCLASSCD, ";
        $query .= "     T2.TEXTBOOKCD ";

        return $query;
    }

    //SCHREG_TEXTBOOK_SUBCLASS_DAT更新
    function &getUpdateQuery($db, $model) {
        $array = (is_array($model->checked)) ? explode(",", implode(',',$model->checked)) : "";

        //削除
        $query  = " DELETE FROM ";
        $query .= "     SCHREG_TEXTBOOK_SUBCLASS_DAT ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO = '".$model->schregno."' AND ";
        $query .= "     YEAR     = '".CTRL_YEAR."' ";

        $db->query($query);
        $db->commit();

        //追加
        if ($array) {
            for ($i = 0; $i < get_count($array); $i++){
                list($classcd, $school_kind, $curriculum_cd, $subclasscd, $textbookcd) = explode('-', $array[$i]);

                $data = array();
                $data["SCHREGNO"][TEXT]         = $model->schregno;
                $data["YEAR"][TEXT]             = CTRL_YEAR;
                $data["CLASSCD"][TEXT]          = $classcd;
                $data["SCHOOL_KIND"][TEXT]      = $school_kind;
                $data["CURRICULUM_CD"][TEXT]    = $curriculum_cd;
                $data["SUBCLASSCD"][TEXT]       = $subclasscd;
                $data["TEXTBOOKCD"][TEXT]       = $textbookcd;
                $data["REGISTERCD"][TEXT]       = STAFFCD;
                $data["UPDATED"][FUNC]          = "sysdate()";

                $query = Query::insertSQL($data, "SCHREG_TEXTBOOK_SUBCLASS_DAT");
                $db->query($query);
            }
            $db->commit();
        }

        return;
    }

    //SCHREG_TEXTBOOK_SUBCLASS_DAT更新
    function &updateTextChkFin($db, $model) {

        //削除
        $query  = " DELETE FROM ";
        $query .= "     SCHREG_TEXTBOOK_CHKFIN_DAT ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO = '".$model->schregno."' AND ";
        $query .= "     YEAR     = '".CTRL_YEAR."' ";

        $db->query($query);
        $db->commit();

        $data = array();
        $data["SCHREGNO"][TEXT]         = $model->schregno;
        $data["YEAR"][TEXT]             = CTRL_YEAR;
        $data["EXECUTED"][TEXT]         = "1";
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][FUNC]          = "sysdate()";

        $query = Query::insertSQL($data, "SCHREG_TEXTBOOK_CHKFIN_DAT");
        $db->query($query);

        $db->commit();

        return;
    }
}
?>
