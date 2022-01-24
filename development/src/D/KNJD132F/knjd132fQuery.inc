<?php

require_once('for_php7.php');

class knjd132fQuery extends Query {
    //学期取得
    function getSemester($frg="") {
        $query  = " SELECT ";
        if ($frg == "CNT") {
            $query .= "     COUNT(*) AS CNT ";
        } else {
            $query .= "     SEMESTER, ";
            $query .= "     SEMESTERNAME ";
        }
        $query .= " FROM ";
        $query .= "     SEMESTER_MST ";
        $query .= " WHERE ";
        $query .= "         YEAR      = '".CTRL_YEAR."' ";
        $query .= "     AND SEMESTER <> '9' ";
        if (!$frg) {
            $query .= " ORDER BY ";
            $query .= "     SEMESTER ";
        }

        return $query;
    }

    //校種取得
    function getSchoolkind($grade) {
        $query  = " SELECT ";
        $query .= "     SCHOOL_KIND ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_GDAT ";
        $query .= " WHERE ";
        $query .= "         YEAR  = '".CTRL_YEAR."' ";
        $query .= "     AND GRADE = '{$grade}' ";

        return $query;
    }

    //１レコード取得
    function getRow($model, $semester="", $frg="") {
        $query  = " SELECT ";
        if ($frg) {
            $query .= "     REMARK2 AS MORAL, ";
            $query .= "     COMMUNICATION ";
        } else {
            $query .= "     SPECIALACTREMARK ";
        }
        $query .= " FROM ";
        $query .= "     HREPORTREMARK_DAT ";
        $query .= " WHERE ";
        $query .= "         YEAR     = '".CTRL_YEAR."' ";
        $query .= "     AND SEMESTER = '".$semester."' ";
        $query .= "     AND SCHREGNO = '".$model->schregno."' ";
        $query .= " ORDER BY ";
        $query .= "     SEMESTER ";

        return $query;
    }

    //存在チェック
    function getDataCheck($model, $semester) {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     HREPORTREMARK_DAT ";
        $query .= " WHERE ";
        $query .= "         YEAR     = '".CTRL_YEAR."' ";
        $query .= "     AND SEMESTER = '".$semester."' ";
        $query .= "     AND SCHREGNO = '".$model->schregno."' ";

        return $query;
    }

    //INSERT
    function &getInsertQuery($fields, $schregno, $model) {
        $db = Query::dbCheckOut();

        for ($seme = 1; $seme <= $model->semeCnt; $seme++) {
            $dataCheck = $db->getOne(knjd132fQuery::getDataCheck($this, $seme));

            if ($seme == CTRL_SEMESTER) {
                $data = array();
                $data["YEAR"][TEXT]                = CTRL_YEAR;
                $data["SEMESTER"][TEXT]            = $seme;
                $data["SCHREGNO"][TEXT]            = $schregno;
                $data["SPECIALACTREMARK"][TEXT]    = $fields["SPECIALACTREMARK".$seme];
                $data["REGISTERCD"][TEXT]          = STAFFCD;
                $data["UPDATED"][NUMBER]           = "sysdate()";

                $where  = " WHERE ";
                $where .= "         YEAR = '".CTRL_YEAR."' ";
                $where .= " AND SEMESTER = '".$seme."' ";
                $where .= " AND SCHREGNO = '".$schregno."' ";
                if ($dataCheck) {
                    $query = Query::updateSQL($data, "HREPORTREMARK_DAT", $where);
                } else {
                    $query = Query::insertSQL($data, "HREPORTREMARK_DAT");
                }
                $db->query($query);
            }
        }

        $dataCheck9 = $db->getOne(knjd132fQuery::getDataCheck($this, "9"));

        $data = array();
        $data["YEAR"][TEXT]                = CTRL_YEAR;
        $data["SEMESTER"][TEXT]            = 9;
        $data["SCHREGNO"][TEXT]            = $schregno;
        $data["COMMUNICATION"][TEXT]       = $fields["COMMUNICATION"];
        $data["REMARK2"][TEXT]             = $fields["MORAL"];
        $data["REGISTERCD"][TEXT]          = STAFFCD;
        $data["UPDATED"][NUMBER]           = "sysdate()";

        $where  = " WHERE ";
        $where .= "         YEAR = '".CTRL_YEAR."' ";
        $where .= " AND SEMESTER = '9' ";
        $where .= " AND SCHREGNO = '".$schregno."' ";
        if ($dataCheck9) {
            $query = Query::updateSQL($data, "HREPORTREMARK_DAT", $where);
        } else {
            $query = Query::insertSQL($data, "HREPORTREMARK_DAT");
        }
        $db->query($query);

        Query::dbCheckIn($db);
        return ;
    }

    //部活動取得
    function getClub($model) {
        $sdate = CTRL_YEAR.'-04-01';
        $edate = (CTRL_YEAR + 1).'-03-31';

        $query  = " SELECT ";
        $query .= "     T1.CLUBCD, ";
        $query .= "     L1.CLUBNAME AS CLUB_SHOW, ";
        $query .= "     T1.SDATE, ";
        $query .= "     T1.EDATE, ";
        $query .= "     L2.NAME1 AS EXECUTIVE_SHOW, ";
        $query .= "     T1.REMARK ";
        $query .= " FROM ";
        $query .= "     SCHREG_CLUB_HIST_DAT T1 ";
        $query .= "     INNER JOIN SCHREG_REGD_DAT T2 ";
        $query .= "          ON T2.SCHREGNO     = T1.SCHREGNO ";
        $query .= "         AND T2.YEAR         = '".CTRL_YEAR."' ";
        $query .= "         AND T2.SEMESTER     = '".CTRL_SEMESTER."' ";
        if ($model->Properties["useSchool_KindField"] == "1") {
            $query .= "     INNER JOIN SCHREG_REGD_GDAT G1 ";
            $query .= "          ON G1.YEAR         = T2.YEAR ";
            $query .= "         AND G1.GRADE        = T2.GRADE ";
            $query .= "         AND G1.SCHOOL_KIND  = T1.SCHOOL_KIND ";
        }
        $query .= "     LEFT JOIN CLUB_MST L1 ";
        $query .= "          ON T1.CLUBCD       = L1.CLUBCD ";
        if ($model->Properties["useSchool_KindField"] == "1") {
            $query .= "         AND T1.SCHOOLCD     = L1.SCHOOLCD ";
            $query .= "         AND T1.SCHOOL_KIND  = L1.SCHOOL_KIND ";
        }
        $query .= "     LEFT JOIN NAME_MST L2 ";
        $query .= "          ON L2.NAMECD1      = 'J001' ";
        $query .= "         AND T1.EXECUTIVECD  = L2.NAMECD2 ";
        $query .= " WHERE ";
        $query .= "     T1.SCHREGNO = '".$model->schregno."' AND ";
        $query .= "     ((T1.SDATE                      <= '{$sdate}' AND VALUE(T1.EDATE, '9999-12-31') >= '{$edate}') OR ";
        $query .= "      (T1.SDATE                      >= '{$sdate}' AND T1.SDATE                      <= '{$edate}') OR ";
        $query .= "      (VALUE(T1.EDATE, '9999-12-31') >= '{$sdate}' AND VALUE(T1.EDATE, '9999-12-31') <= '{$edate}') ";
        $query .= "     ) ";
        if ($model->Properties["useSchool_KindField"] == "1") {
            $query .= " AND T1.SCHOOLCD    = '".sprintf("%012d", SCHOOLCD)."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     T1.SDATE, ";
        $query .= "     T1.CLUBCD ";

        return $query;
    }

    //委員会取得
    function getCommittee($model) {
        $query  = " SELECT ";
        $query .= "     T1.GRADE, ";
        $query .= "     G1.GRADE_NAME1, ";
        $query .= "     T1.SEMESTER, ";
        $query .= "     L3.NAME1 AS SEMESTERNAME, ";
        $query .= "     T1.SEQ, ";
        $query .= "     L1.COMMITTEENAME AS COMMITTEE_SHOW, ";
        $query .= "     T1.CHARGENAME AS CHARGE_SHOW, ";
        $query .= "     L2.NAME1 AS EXECUTIVE_SHOW ";
        $query .= " FROM ";
        $query .= "     SCHREG_COMMITTEE_HIST_DAT T1 ";
        $query .= "     INNER JOIN SCHREG_REGD_DAT T2 ";
        $query .= "          ON T2.SCHREGNO     = T1.SCHREGNO ";
        $query .= "         AND T2.YEAR         = T1.YEAR ";
        $query .= "         AND T2.SEMESTER     = '".CTRL_SEMESTER."' ";
        $query .= "     INNER JOIN SCHREG_REGD_GDAT G1 ";
        $query .= "          ON G1.YEAR         = T2.YEAR ";
        $query .= "         AND G1.GRADE        = T2.GRADE ";
        if ($model->Properties["useSchool_KindField"] == "1") {
            $query .= "         AND G1.SCHOOL_KIND  = T1.SCHOOL_KIND ";
        }
        $query .= "     LEFT JOIN COMMITTEE_MST L1 ";
        $query .= "          ON T1.COMMITTEE_FLG    = L1.COMMITTEE_FLG ";
        $query .= "         AND T1.COMMITTEECD      = L1.COMMITTEECD ";
        if ($model->Properties["useSchool_KindField"] == "1") {
            $query .= "         AND T1.SCHOOLCD         = L1.SCHOOLCD ";
            $query .= "         AND T1.SCHOOL_KIND      = L1.SCHOOL_KIND ";
        }
        $query .= "     LEFT JOIN NAME_MST L2 ";
        $query .= "          ON L2.NAMECD1      = 'J002' ";
        $query .= "         AND T1.EXECUTIVECD  = L2.NAMECD2 ";
        $query .= "     LEFT JOIN NAME_MST L3 ";
        $query .= "          ON L3.NAMECD1      = 'J004' ";
        $query .= "         AND T1.SEMESTER     = L3.NAMECD2 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR     = '" .CTRL_YEAR."' AND ";
        $query .= "     T1.SCHREGNO = '" .$model->schregno ."' ";
        if ($model->Properties["useSchool_KindField"] == "1") {
            $query .= " AND T1.SCHOOLCD = '".sprintf("%012d", SCHOOLCD)."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     T1.GRADE, ";
        $query .= "     T1.SEMESTER, ";
        $query .= "     T1.SEQ ";

        return $query;
    }

    //大会記録備考取得
    function getSchregClubHdetailDat($model) {
        $query  = " SELECT ";
        $query .= "     T1.CLUBCD, ";
        $query .= "     L1.CLUBNAME AS CLUB_SHOW, ";
        $query .= "     T1.HOSTCD, ";
        $query .= "     L2.HOSTNAME, ";
        $query .= "     T1.MEET_NAME, ";
        $query .= "     T1.DETAIL_DATE, ";
        $query .= "     T1.DETAIL_SEQ, ";
        $query .= "     CASE T1.DIV WHEN '1' THEN '個人' WHEN '2' THEN '団体' ELSE '' END AS DIV_NAME, ";
        $query .= "     T1.RECORDCD, ";
        $query .= "     L3.RECORDNAME, ";
        $query .= "     T1.DOCUMENT, ";
        $query .= "     T1.DETAIL_REMARK ";
        $query .= " FROM ";
        $query .= "     SCHREG_CLUB_HDETAIL_DAT T1 ";
        $query .= "     INNER JOIN SCHREG_REGD_DAT T2 ";
        $query .= "          ON T2.SCHREGNO     = T1.SCHREGNO ";
        $query .= "         AND T2.YEAR         = '".CTRL_YEAR."' ";
        $query .= "         AND T2.SEMESTER     = '".CTRL_SEMESTER."' ";
        if ($model->Properties["useSchool_KindField"] == "1") {
            $query .= "     INNER JOIN SCHREG_REGD_GDAT G1 ";
            $query .= "          ON G1.YEAR         = T2.YEAR ";
            $query .= "         AND G1.GRADE        = T2.GRADE ";
            $query .= "         AND G1.SCHOOL_KIND  = T1.SCHOOL_KIND ";
        }
        $query .= "     LEFT JOIN CLUB_MST L1 ON T1.CLUBCD = L1.CLUBCD ";
        if ($model->Properties["useSchool_KindField"] == "1") {
            $query .= "             AND T1.SCHOOLCD     = L1.SCHOOLCD ";
            $query .= "             AND T1.SCHOOL_KIND  = L1.SCHOOL_KIND ";
        }
        $query .= "     LEFT JOIN CLUB_HOST_MST L2 ON T1.HOSTCD = L2.HOSTCD ";
        if ($model->Properties["useSchool_KindField"] == "1") {
            $query .= "             AND T1.SCHOOLCD     = L2.SCHOOLCD ";
            $query .= "             AND T1.SCHOOL_KIND  = L2.SCHOOL_KIND ";
        }
        $query .= "     LEFT JOIN CLUB_RECORD_MST L3 ON T1.RECORDCD = L3.RECORDCD ";
        if ($model->Properties["useSchool_KindField"] == "1") {
            $query .= "             AND T1.SCHOOLCD     = L3.SCHOOLCD ";
            $query .= "             AND T1.SCHOOL_KIND  = L3.SCHOOL_KIND ";
        }
        $query .= " WHERE ";
        $query .= "     T1.SCHREGNO = '".$model->schregno."' ";
        if ($model->Properties["useSchool_KindField"] == "1") {
            $query .= " AND T1.SCHOOLCD = '".sprintf("%012d", SCHOOLCD)."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     T1.HOSTCD, ";
        $query .= "     T1.DETAIL_DATE DESC, ";
        $query .= "     T1.DETAIL_SEQ ";

        return $query;
    }

    //学籍資格データよりデータを取得
    function getAward($model) {
        if ($model->Properties["useQualifiedMst"] == '1') {
            $query  = " SELECT ";
            $query .= "     T1.YEAR, ";
            $query .= "     T1.SCHREGNO, ";
            $query .= "     T1.SEQ, ";
            $query .= "     T1.REGDDATE, ";
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "     T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD AS SUBCLASSCD, ";
            } else {
                $query .= "     T1.SUBCLASSCD, ";
            }
            $query .= "     CASE L1.CONDITION_DIV ";
            $query .= "          WHEN '1' ";
            $query .= "          THEN '国家資格' ";
            $query .= "          WHEN '2' ";
            $query .= "          THEN '公的資格' ";
            $query .= "          WHEN '3' ";
            $query .= "          THEN '民間資格' ";
            $query .= "          ELSE 'その他' ";
            $query .= "     END AS CONDITION_DIV, ";
            $query .= "     T1.QUALIFIED_CD, ";
            $query .= "     L1.QUALIFIED_NAME AS QUALIFIED_SHOW, ";
            $query .= "     L1.QUALIFIED_ABBV, ";
            $query .= "     L1.PROMOTER, ";
            $query .= "     T1.CONTENTS, ";
            $query .= "     L2.NAME1 AS RANK, ";
            $query .= "     T1.REMARK, ";
            $query .= "     T1.CREDITS ";
            $query .= " FROM ";
            $query .= "     SCHREG_QUALIFIED_HOBBY_DAT T1 ";
            $query .= "     LEFT JOIN QUALIFIED_MST L1 ";
            $query .= "          ON L1.QUALIFIED_CD = T1.QUALIFIED_CD ";
            $query .= "     LEFT JOIN NAME_MST L2 ";
            $query .= "          ON L2.NAMECD2  = T1.RANK ";
            $query .= "         AND L2.NAMECD1  = 'H312' ";
            $query .= " WHERE ";
            $query .= "     T1.YEAR     = '".CTRL_YEAR."' AND ";
            $query .= "     T1.SCHREGNO = '{$model->schregno}' ";
            if ($model->Properties["useSchregQualified"] == 'SUBCLASS_QUALIFIED_TEST_DAT') {
                $query .= " UNION ";
                $query .= " SELECT ";
                $query .= "     T1.YEAR, ";
                $query .= "     T1.SCHREGNO, ";
                $query .= "     0 AS SEQ, ";
                $query .= "     T1.TEST_DATE AS REGDDATE, ";
                $query .= "     '00-00-00-000000' AS SUBCLASSCD, ";
                $query .= "     CASE L1.CONDITION_DIV ";
                $query .= "          WHEN '1' ";
                $query .= "          THEN '国家資格' ";
                $query .= "          WHEN '2' ";
                $query .= "          THEN '公的資格' ";
                $query .= "          WHEN '3' ";
                $query .= "          THEN '民間資格' ";
                $query .= "          ELSE 'その他' ";
                $query .= "     END AS CONDITION_DIV, ";
                $query .= "     T1.QUALIFIED_CD, ";
                $query .= "     L1.QUALIFIED_NAME AS QUALIFIED_SHOW, ";
                $query .= "     L1.QUALIFIED_ABBV, ";
                $query .= "     L1.PROMOTER, ";
                $query .= "     '' AS CONTENTS, ";
                $query .= "     L2.TEST_NAME AS RANK, ";
                $query .= "     N1.NAME1 AS REMARK, ";
                $query .= "     0 AS CREDITS ";
                $query .= " FROM ";
                $query .= "     SCHREG_QUALIFIED_TEST_DAT T1 ";
                $query .= "     LEFT JOIN QUALIFIED_MST L1 ON L1.QUALIFIED_CD = T1.QUALIFIED_CD ";
                $query .= "     LEFT JOIN QUALIFIED_TEST_MST L2 ";
                $query .= "          ON L2.YEAR         = T1.YEAR ";
                $query .= "         AND L2.QUALIFIED_CD = T1.QUALIFIED_CD ";
                $query .= "         AND L2.TEST_CD      = T1.TEST_CD ";
                $query .= "     LEFT JOIN NAME_MST N1 ";
                $query .= "          ON N1.NAMECD1      = 'Z050' ";
                $query .= "         AND SUBSTR(T1.RESULT_CD, 4) = N1.NAMECD2 ";
                $query .= " WHERE ";
                $query .= "     T1.YEAR     = '".CTRL_YEAR."' AND ";
                $query .= "     T1.SCHREGNO = '{$model->schregno}' AND ";
                $query .= "     T1.RESULT_CD NOT IN ('8888', '9999') "; //欠席・不合格を除く
            }
            $query .= " ORDER BY ";
            $query .= "     REGDDATE, ";
            $query .= "     SUBCLASSCD, ";
            $query .= "     SEQ ";
        } else {
            $query  = " SELECT ";
            $query .= "     T1.YEAR, ";
            $query .= "     T1.REGDDATE, ";
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "     T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD AS SUBCLASSCD, ";
            } else {
                $query .= "     T1.SUBCLASSCD, ";
            }
            $query .= "     CASE WHEN T1.CONDITION_DIV = '1' ";
            $query .= "          THEN T1.CONDITION_DIV || ':資格' ";
            $query .= "          ELSE T1.CONDITION_DIV || ':その他' ";
            $query .= "          END AS CONDITION_DIV, ";
            $query .= "     T1.SEQ, ";
            $query .= "     T1.CONTENTS, ";
            $query .= "     T1.REMARK, ";
            $query .= "     T1.CREDITS, ";
            $query .= "     T5.CREDITS AS CREDIT ";
            $query .= " FROM ";
            $query .= "     SCHREG_QUALIFIED_HOBBY_DAT T1 ";
            $query .= "     LEFT JOIN SUBCLASS_MST T2 ";
            $query .= "          ON T1.SUBCLASSCD       = T2.SUBCLASSCD ";
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "         AND T1.CLASSCD          = T2.CLASSCD ";
                $query .= "         AND T1.SCHOOL_KIND      = T2.SCHOOL_KIND ";
                $query .= "         AND T1.CURRICULUM_CD    = T2.CURRICULUM_CD ";
            }
            $query .= "     LEFT JOIN SCHREG_REGD_DAT T4 ";
            $query .= "          ON T4.SCHREGNO = T1.SCHREGNO ";
            $query .= "         AND T4.YEAR     = T1.YEAR ";
            $query .= "         AND T4.SEMESTER = '".CTRL_SEMESTER."' ";
            $query .= "     LEFT JOIN CREDIT_MST T5 ";
            $query .= "          ON T5.YEAR         = T1.YEAR ";
            $query .= "         AND T5.COURSECD     = T4.COURSECD ";
            $query .= "         AND T5.MAJORCD      = T4.MAJORCD ";
            $query .= "         AND T5.GRADE        = T4.GRADE ";
            $query .= "         AND T5.COURSECODE   = T4.COURSECODE ";
            $query .= "         AND T5.CLASSCD      = SUBSTR(T1.SUBCLASSCD,1,2) ";
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "         AND T5.SCHOOL_KIND      = T1.SCHOOL_KIND ";
                $query .= "         AND T5.CURRICULUM_CD    = T1.CURRICULUM_CD ";
            }
            $query .= "                              AND T5.SUBCLASSCD = T1.SUBCLASSCD ";
            $query .= " WHERE ";
            $query .= "     T1.YEAR     = '".CTRL_YEAR."' AND ";
            $query .= "     T1.SCHREGNO = '{$model->schregno}' ";
            $query .= " ORDER BY ";
            $query .= "     T1.REGDDATE, ";
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "     T1.CLASSCD, ";
                $query .= "     T1.SCHOOL_KIND, ";
                $query .= "     T1.CURRICULUM_CD, ";
            }
            $query .= "     T1.SUBCLASSCD, ";
            $query .= "     T1.SEQ ";
        }

        return $query;
    }
}
?>
