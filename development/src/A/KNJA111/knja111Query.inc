<?php

require_once('for_php7.php');

class knja111Query extends Query {
    //生徒情報取得
    function getStudent_data($schregno, $model) {
        $db = Query::dbCheckOut();

        $query  = " SELECT ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     T2.GRADE, ";
        $query .= "     T2.HR_CLASS, ";
        $query .= "     L1.HR_NAME, ";
        $query .= "     T2.ATTENDNO, ";
        $query .= "     T2.ANNUAL, ";
        $query .= "     T2.COURSECD, ";
        $query .= "     T2.MAJORCD, ";
        $query .= "     T2.COURSECD || T2.MAJORCD || '  ' || L2.COURSENAME || L2.MAJORABBV AS COURSE_MAJORNAME, ";
        $query .= "     T2.COURSECODE, ";
        $query .= "     T2.COURSECODE || '  ' || L3.COURSECODENAME AS COURSECODENAME, ";
        $query .= "     T1.INOUTCD, ";
        $query .= "     T1.NAME, ";
        $query .= "     T1.NAME_SHOW, ";
        $query .= "     T1.NAME_KANA, ";
        $query .= "     T1.NAME_ENG, ";
        $query .= "     T1.REAL_NAME, ";
        $query .= "     T1.REAL_NAME_KANA, ";
        $query .= "     T1.BIRTHDAY, ";
        $query .= "     T1.SEX, ";
        $query .= "     T1.BLOODTYPE, ";
        $query .= "     T1.BLOOD_RH, ";
        $query .= "     T1.HANDICAP, ";
        $query .= "     T1.NATIONALITY, ";
        $query .= "     CASE WHEN T3.SCHREGNO IS NOT NULL ";
        $query .= "          THEN T3.FINSCHOOLCD ";
        $query .= "          ELSE T1.FINSCHOOLCD ";
        $query .= "          END AS FINSCHOOLCD, ";
        $query .= "     L4.FINSCHOOL_NAME, ";
        $query .= "     CASE WHEN T3.SCHREGNO IS NOT NULL ";
        $query .= "          THEN T3.FINISH_DATE ";
        $query .= "          ELSE T1.FINISH_DATE ";
        $query .= "          END AS FINISH_DATE, ";
        $query .= "     T1.NYUGAKUMAE_SYUSSIN_JOUHOU, ";
        $query .= "     CASE WHEN T3.SCHREGNO IS NOT NULL ";
        $query .= "          THEN T3.ENT_DATE ";
        $query .= "          ELSE T1.ENT_DATE ";
        $query .= "          END AS ENT_DATE, ";
        $query .= "     T3.CURRICULUM_YEAR, ";
        $query .= "     CASE WHEN T3.SCHREGNO IS NOT NULL ";
        $query .= "          THEN T3.ENT_DIV ";
        $query .= "          ELSE T1.ENT_DIV ";
        $query .= "          END AS ENT_DIV, ";
        $query .= "     T4.BASE_REMARK1 AS EXAMNO, ";
        $query .= "     CASE WHEN T3.SCHREGNO IS NOT NULL ";
        $query .= "          THEN T3.ENT_REASON ";
        $query .= "          ELSE T1.ENT_REASON ";
        $query .= "          END AS ENT_REASON, ";
        $query .= "     CASE WHEN T3.SCHREGNO IS NOT NULL ";
        $query .= "          THEN T3.ENT_SCHOOL ";
        $query .= "          ELSE T1.ENT_SCHOOL ";
        $query .= "          END AS ENT_SCHOOL, ";
        $query .= "     CASE WHEN T3.SCHREGNO IS NOT NULL ";
        $query .= "          THEN T3.ENT_ADDR ";
        $query .= "          ELSE T1.ENT_ADDR ";
        $query .= "          END AS ENT_ADDR, ";
        if ($model->Properties["useAddrField2"] == "1") {
            $query .= "     CASE WHEN T3.SCHREGNO IS NOT NULL ";
            $query .= "          THEN T3.ENT_ADDR2 ";
            $query .= "          ELSE T1.ENT_ADDR2 ";
            $query .= "          END AS ENT_ADDR2, ";
        }
        $query .= "     CASE WHEN T3.SCHREGNO IS NOT NULL ";
        $query .= "          THEN T3.GRD_DATE ";
        $query .= "          ELSE T1.GRD_DATE ";
        $query .= "          END AS GRD_DATE, ";
        $query .= "     CASE WHEN T3.SCHREGNO IS NOT NULL ";
        $query .= "          THEN T3.GRD_DIV ";
        $query .= "          ELSE T1.GRD_DIV ";
        $query .= "          END AS GRD_DIV, ";
        $query .= "     T1.TENGAKU_SAKI_ZENJITU, ";
        $query .= "     CASE WHEN T3.SCHREGNO IS NOT NULL ";
        $query .= "          THEN T3.GRD_REASON ";
        $query .= "          ELSE T1.GRD_REASON ";
        $query .= "          END AS GRD_REASON, ";
        $query .= "     CASE WHEN T3.SCHREGNO IS NOT NULL ";
        $query .= "          THEN T3.GRD_SCHOOL ";
        $query .= "          ELSE T1.GRD_SCHOOL ";
        $query .= "          END AS GRD_SCHOOL, ";
        $query .= "     CASE WHEN T3.SCHREGNO IS NOT NULL ";
        $query .= "          THEN T3.GRD_ADDR ";
        $query .= "          ELSE T1.GRD_ADDR ";
        $query .= "          END AS GRD_ADDR, ";
        if ($model->Properties["useAddrField2"] == "1") {
            $query .= "     CASE WHEN T3.SCHREGNO IS NOT NULL ";
            $query .= "          THEN T3.GRD_ADDR2 ";
            $query .= "          ELSE T1.GRD_ADDR2 ";
            $query .= "          END AS GRD_ADDR2, ";
        }
        $query .= "     T1.PRISCHOOLCD || '  ' || L5.PRISCHOOL_NAME AS PRISCHOOL_NAME, ";
        $query .= "     T1.REMARK1, ";
        $query .= "     T1.REMARK2, ";
        $query .= "     T1.REMARK3 ";
        $query .= " FROM ";
        $query .= "     V_SCHREG_BASE_MST T1 ";
        $query .= "     LEFT JOIN SCHREG_REGD_DAT T2 ON T1.SCHREGNO = T2.SCHREGNO ";
        $query .= "     LEFT JOIN SCHREG_ENT_GRD_HIST_DAT T3 ON T1.SCHREGNO = T3.SCHREGNO ";
        $query .= "             AND T3.SCHOOL_KIND = '". $model->schoolKind. "' ";
        $query .= "     LEFT JOIN SCHREG_BASE_DETAIL_MST T4 ON T4.SCHREGNO = T1.SCHREGNO ";
        $query .= "             AND T4.BASE_SEQ = '003' ";
        $query .= "     LEFT JOIN SCHREG_REGD_HDAT L1 ON L1.YEAR = T2.YEAR ";
        $query .= "             AND L1.SEMESTER     = T2.SEMESTER ";
        $query .= "             AND L1.GRADE        = T2.GRADE ";
        $query .= "             AND L1.HR_CLASS     = T2.HR_CLASS ";
        $query .= "     LEFT JOIN V_COURSE_MAJOR_MST L2 ON L2.YEAR = T2.YEAR ";
        $query .= "             AND L2.COURSECD     = T2.COURSECD ";
        $query .= "             AND L2.MAJORCD      = T2.MAJORCD ";
        $query .= "     LEFT JOIN V_COURSECODE_MST L3 ON L3.YEAR = T2.YEAR ";
        $query .= "             AND L3.COURSECODE   = T2.COURSECODE ";
        $query .= "     LEFT JOIN FINSCHOOL_MST L4 ON L4.FINSCHOOLCD = T1.FINSCHOOLCD ";
        $query .= "     LEFT JOIN V_PRISCHOOL_MST L5 ON L5.YEAR = T2.YEAR ";
        $query .= "             AND L5.PRISCHOOLCD   = T1.PRISCHOOLCD ";
        $query .= " WHERE ";
        $query .= "     T1.SCHREGNO = '".$schregno."' AND ";
        $query .= "     T2.YEAR     = '".CTRL_YEAR."' AND ";
        $query .= "     T2.SEMESTER = '".CTRL_SEMESTER."' ";

        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);

        return $row;
    }

    //V_NAME_MST取得
    function getNameMst($namecd1, $namecd2) {
        $query  = " SELECT ";
        $query .= "     NAMECD2 || '  ' || VALUE(NAME1, '') ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR    = '".CTRL_YEAR."' AND ";
        $query .= "     NAMECD1 = '".$namecd1."' AND ";
        $query .= "     NAMECD2 = '".$namecd2."' ";

        return $query;
    }

    //復学あり
    function getComeBackT() {
        $query  = " SELECT ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     SYSCAT.COLUMNS ";
        $query .= " WHERE ";
        $query .= "     TABNAME = 'SCHREG_ENT_GRD_HIST_COMEBACK_DAT' ";

        return $query;
    }

    //復学日取得
    function getCB_entDate($model) {
        $query  = " WITH MIN_T AS ( ";
        $query .= "     SELECT ";
        $query .= "         SCHREGNO, ";
        $query .= "         MIN(COMEBACK_DATE) AS COMEBACK_DATE ";
        $query .= "     FROM ";
        $query .= "         SCHREG_ENT_GRD_HIST_COMEBACK_DAT ";
        $query .= "     WHERE ";
        $query .= "         SCHREGNO    = '".$model->schregno."' AND ";
        $query .= "         SCHOOL_KIND = '".$model->schoolKind."' ";
        $query .= "     GROUP BY ";
        $query .= "         SCHREGNO ";
        $query .= " ) ";

        $query .= " SELECT ";
        $query .= "     ENT_DATE ";
        $query .= " FROM ";
        $query .= "     SCHREG_ENT_GRD_HIST_COMEBACK_DAT T1, ";
        $query .= "     MIN_T ";
        $query .= " WHERE ";
        $query .= "     T1.SCHREGNO      = MIN_T.SCHREGNO AND ";
        $query .= "     T1.SCHOOL_KIND   = '".$model->schoolKind."' AND ";
        $query .= "     T1.COMEBACK_DATE = MIN_T.COMEBACK_DATE ";

        return $query;
    }

    //学校種別取得
    function getSchoolKind($model) {
        $arr = explode(",", $model->GradeClass);

        $query  = " SELECT ";
        $query .= "     SCHOOL_KIND ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_GDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR    = '".CTRL_YEAR."' AND ";
        $query .= "     GRADE   = '".$arr[0]."' ";

        return $query;
    }

    /**************/
    /*  削除処理  */
    /**************/
    function &getDeleteQuery($model) {
        //削除対象テーブル
        $tableArray = array();
        $tableArray = array("SCHREG_BASE_MST",
                            "SCHREG_BASE_DETAIL_MST",
                            "SCHREG_REGD_DAT",
                            "SCHREG_ENT_GRD_HIST_DAT",
                            "SCHREG_TRANSFER_DAT",
                            "SCHREG_ADDRESS_DAT",
                            "ATTEND_DAT",
                            "ATTEND_SEMES_DAT",
                            "ATTEND_SUBCLASS_DAT",
                            "CERTIF_ISSUE_DAT",
                            "CHAIR_STD_DAT",
                            "CLASS_FORMATION_DAT",
                            "FRESHMAN_DAT",
                            "GUARDIAN_DAT",
                            "HEXAM_EMPREMARK_DAT",
                            "HEXAM_ENTREMARK_DAT",
                            "HEXAM_ENTREMARK_HDAT",
                            "HREPORTREMARK_DAT",
                            "HTRAINREMARK_DAT",
                            "MEDEXAM_DET_DAT",
                            "MEDEXAM_HDAT",
                            "MEDEXAM_TOOTH_DAT",
                            "SCHREG_ATTENDREC_DAT",
                            "SCHREG_DETAILHIST_DAT",
                            "SCHREG_ENVIR_DAT",
                            "SCHREG_QUALIFIED_DAT",
                            "SCHREG_RELA_DAT",
                            "SCHREG_STUDYREC_DAT",
                            "SCHREG_TRAINHIST_DAT",
                            "SCHREG_TRAIN_DAT",
                            "STUDYCLASSREMARK_DAT",
                            "STUDYRECREMARK_DAT");

        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //対象データを削除
        foreach ($tableArray as $tableName) {
            $query = " DELETE FROM {$tableName} WHERE SCHREGNO = '".$model->schregno."' ";
            $db->query($query);
        }

        $db->commit();
        Query::dbCheckIn($db);

        return true;
    }
}
?>
