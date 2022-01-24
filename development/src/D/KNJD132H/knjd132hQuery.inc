<?php

require_once('for_php7.php');

class knjd132hQuery extends Query {

    //学期取得
    function getSemester($model) {
        $query  = " SELECT ";
        $query .= "     SEMESTER AS VALUE, ";
        $query .= "     SEMESTERNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SEMESTER_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND SEMESTER < '9' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //校種取得
    function getSchkind($grade) {
        $query  = " SELECT ";
        $query .= "    SCHOOL_KIND ";
        $query .= " FROM ";
        $query .= "    SCHREG_REGD_GDAT ";
        $query .= " WHERE ";
        $query .= "        YEAR  = '".CTRL_YEAR."' ";
        $query .= "    AND GRADE = '".$grade."' ";

        return $query;
    }

    //１レコード取得
    function getHreportRemarkRow($model) {
        $query  = " SELECT ";
        $query .= "     D01_01.REMARK1 AS REMARK1_01_01, ";
        $query .= "     D01_02.REMARK1 AS REMARK1_01_02, ";
        $query .= "     D02_01.REMARK1 AS REMARK1_02_01, ";
        $query .= "     HREP.COMMUNICATION ";
        $query .= " FROM ";
        $query .= "     HREPORTREMARK_DAT HREP ";
        $query .= "     LEFT JOIN HREPORTREMARK_DETAIL_DAT D01_01 ON D01_01.YEAR     = HREP.YEAR ";
        $query .= "                                              AND D01_01.SEMESTER = HREP.SEMESTER ";
        $query .= "                                              AND D01_01.SCHREGNO = HREP.SCHREGNO ";
        $query .= "                                              AND D01_01.DIV      = '01' ";
        $query .= "                                              AND D01_01.CODE     = '01' ";
        $query .= "     LEFT JOIN HREPORTREMARK_DETAIL_DAT D01_02 ON D01_02.YEAR     = HREP.YEAR ";
        $query .= "                                              AND D01_02.SEMESTER = HREP.SEMESTER ";
        $query .= "                                              AND D01_02.SCHREGNO = HREP.SCHREGNO ";
        $query .= "                                              AND D01_02.DIV      = '01' ";
        $query .= "                                              AND D01_02.CODE     = '02' ";
        $query .= "     LEFT JOIN HREPORTREMARK_DETAIL_DAT D02_01 ON D02_01.YEAR     = HREP.YEAR ";
        $query .= "                                              AND D02_01.SEMESTER = HREP.SEMESTER ";
        $query .= "                                              AND D02_01.SCHREGNO = HREP.SCHREGNO ";
        $query .= "                                              AND D02_01.DIV      = '02' ";
        $query .= "                                              AND D02_01.CODE     = '01' ";
        $query .= " WHERE ";
        $query .= "         HREP.YEAR     = '".$model->exp_year."' ";
        $query .= "     AND HREP.SEMESTER = '".$model->field["SEMESTER"]."' ";
        $query .= "     AND HREP.SCHREGNO = '".$model->schregno."' ";

        return $query;
    }

    //存在チェック
    function getHreportRemarkCnt($model) {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     HREPORTREMARK_DAT ";
        $query .= " WHERE ";
        $query .= "         YEAR     = '".$model->exp_year."' ";
        $query .= "     AND SEMESTER = '".$model->field["SEMESTER"]."' ";
        $query .= "     AND SCHREGNO = '".$model->schregno."' ";

        return $query;
    }

    //INSERT
    function &getInsertQuery($model) {
        $db = Query::dbCheckOut();

        //HREPORTREMARK_DAT
        //存在チェック
        $check = $db->getCol(knjd132hQuery::getHreportRemarkCnt($model));

        if(get_count($check) == 0) {
            $data["YEAR"][TEXT]                 = $model->exp_year;
            $data["SEMESTER"][TEXT]             = $model->field["SEMESTER"];
            $data["SCHREGNO"][TEXT]             = $model->schregno;
            $data["COMMUNICATION"][TEXT]        = $model->field["COMMUNICATION"];
            $data["REGISTERCD"][TEXT]           = STAFFCD;
            $data["UPDATED"][FUNC]              = "sysdate()";

            $db->query(Query::insertSQL($data, "HREPORTREMARK_DAT"));
        } else {
            $data["COMMUNICATION"][TEXT]        = $model->field["COMMUNICATION"];
            $data["REGISTERCD"][TEXT]           = STAFFCD;
            $data["UPDATED"][FUNC]              = "sysdate()";

            $where  = " WHERE ";
            $where .= "         YEAR     = '".$model->exp_year."' ";
            $where .= "     AND SEMESTER = '".$model->field["SEMESTER"]."' ";
            $where .= "     AND SCHREGNO = '".$model->schregno."' ";

            $db->query(Query::updateSQL($data, "HREPORTREMARK_DAT", $where));
        }

        //HREPORTREMARK_DETAIL_DAT
        for ($div = 1; $div <= 2; $div++) {
            $setDiv = sprintf("%02d", $div);
            for ($code = 1; $code <= 2; $code++) {
                $setCode = sprintf("%02d", $code);
                if ($div == "2" && $model->schKind == "J") break;
                if ($div == "2" && $code == "2") break;
                //項目
                $data = array();
                $data["YEAR"][TEXT]         = CTRL_YEAR;
                $data["SEMESTER"][TEXT]     = $model->field["SEMESTER"];
                $data["SCHREGNO"][TEXT]     = $model->schregno;
                $data["DIV"][TEXT]          = $setDiv;
                $data["CODE"][TEXT]         = $setCode;
                if ($div == "1" && $code == "1") {
                    $setRemark1 = $model->field["REMARK1_01_01"];//委員会・係・部活動の成果
                } else if ($div == "1" && $code == "2") {
                    $setRemark1 = $model->field["REMARK1_01_02"];//資格取得・検定・表彰など
                } else {
                    $setRemark1 = $model->field["REMARK1_02_01"];//研究活動テーマ
                }
                $data["REMARK1"][TEXT]      = $setRemark1;
                $data["REGISTERCD"][TEXT]   = STAFFCD ;
                $data["UPDATED"][FUNC]      = "sysdate()";

                //存在チェック
                $query  = " SELECT COUNT(*) FROM HREPORTREMARK_DETAIL_DAT";
                $query .= " WHERE ";
                $query .= "         YEAR     = '".CTRL_YEAR."' ";
                $query .= "     AND SEMESTER = '".$model->field["SEMESTER"]."' ";
                $query .= "     AND SCHREGNO = '".$model->schregno."' ";
                $query .= "     AND DIV      = '".$setDiv."' ";
                $query .= "     AND CODE     = '".$setCode."' ";

                if (0 < $db->getOne($query)) {
                    //更新
                    $where  = " WHERE ";
                    $where .= "         YEAR     = '".CTRL_YEAR."' ";
                    $where .= "     AND SEMESTER = '".$model->field["SEMESTER"]."' ";
                    $where .= "     AND SCHREGNO = '".$model->schregno."' ";
                    $where .= "     AND DIV      = '".$setDiv."' ";
                    $where .= "     AND CODE     = '".$setCode."' ";
                    $query = Query::updateSQL($data, "HREPORTREMARK_DETAIL_DAT", $where);
                } else {
                    //追加
                    $query = Query::insertSQL($data, "HREPORTREMARK_DETAIL_DAT");
                }
                $db->query($query);
            }
        }

        Query::dbCheckIn($db);
        return ;
    }

    //部活動参照
    function getClub($model) {
        $query  = " SELECT ";
        $query .= "     T1.CLUBCD, ";
        $query .= "     T2.CLUBNAME AS CLUB_SHOW, ";
        $query .= "     T1.SDATE, ";
        $query .= "     T1.EDATE, ";
        $query .= "     T3.NAME1 AS EXECUTIVE_SHOW, ";
        $query .= "     T1.REMARK ";
        $query .= " FROM ";
        $query .= "     SCHREG_CLUB_HIST_DAT T1 ";
        $query .= "     LEFT JOIN CLUB_MST T2 ";
        $query .= "          ON T1.CLUBCD = T2.CLUBCD ";
        if ($model->Properties["useSchool_KindField"] == "1") {
            $query .= "     AND T1.SCHOOLCD     = T2.SCHOOLCD ";
            $query .= "     AND T1.SCHOOL_KIND  = T2.SCHOOL_KIND ";
        }
        $query .= "     LEFT JOIN NAME_MST T3 ";
        $query .= "          ON T3.NAMECD1      = 'J001' ";
        $query .= "         AND T1.EXECUTIVECD  = T3.NAMECD2 ";
        $query .= " WHERE ";
        $query .= "     T1.SCHREGNO = '".$model->schregno."' AND ";
        $query .= "     FISCALYEAR(T1.SDATE) <= '".CTRL_YEAR."' AND ";
        $query .= "     value(FISCALYEAR(T1.EDATE), '9999') >= '".CTRL_YEAR."' ";
        if ($model->Properties["useSchool_KindField"] == "1") {
            $query .= " AND T1.SCHOOLCD    = '".sprintf("%012d", SCHOOLCD)."' ";
            $query .= " AND T1.SCHOOL_KIND = '".$model->schKind."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     T1.SDATE ";

        return $query;
    }

    //委員会活動参照
    function getCommittee($model) {
        $query  = " SELECT ";
        $query .= "     T1.GRADE, ";
        $query .= "     G1.GRADE_NAME1, ";
        $query .= "     T1.SEMESTER, ";
        $query .= "     T4.NAME1 AS SEMESTERNAME, ";
        $query .= "     T1.SEQ, ";
        $query .= "     T2.COMMITTEENAME AS COMMITTEE_SHOW, ";
        $query .= "     T1.CHARGENAME AS CHARGE_SHOW, ";
        $query .= "     T3.NAME1 AS EXECUTIVE_SHOW ";
        $query .= " FROM ";
        $query .= "     SCHREG_COMMITTEE_HIST_DAT T1 ";
        $query .= "     LEFT JOIN COMMITTEE_MST T2 ";
        $query .= "          ON T1.COMMITTEE_FLG    = T2.COMMITTEE_FLG ";
        $query .= "         AND T1.COMMITTEECD      = T2.COMMITTEECD ";
        if ($model->Properties["useSchool_KindField"] == "1") {
            $query .= "     AND T1.SCHOOLCD         = T2.SCHOOLCD ";
            $query .= "     AND T1.SCHOOL_KIND      = T2.SCHOOL_KIND ";
        }
        $query .= "     LEFT JOIN NAME_MST T3 ";
        $query .= "          ON T3.NAMECD1      = 'J002' ";
        $query .= "         AND T1.EXECUTIVECD  = T3.NAMECD2 ";
        $query .= "     LEFT JOIN NAME_MST T4 ";
        $query .= "          ON T4.NAMECD1      = 'J004' ";
        $query .= "         AND T1.SEMESTER     = T4.NAMECD2 ";
        $query .= "     LEFT JOIN SCHREG_REGD_GDAT G1 ";
        $query .= "          ON T1.YEAR     = G1.YEAR ";
        $query .= "         AND T1.GRADE    = G1.GRADE ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR     = '".CTRL_YEAR."' AND ";
        $query .= "     T1.SCHREGNO = '".$model->schregno."' ";
        if ($model->Properties["useSchool_KindField"] == "1") {
            $query .= " AND T1.SCHOOLCD    = '".sprintf("%012d", SCHOOLCD)."' ";
            $query .= " AND T1.SCHOOL_KIND = '".$model->schKind."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     T1.GRADE, ";
        $query .= "     T1.SEMESTER, ";
        $query .= "     T1.SEQ ";

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
            $query .= "          ON L2.NAMECD2 = T1.RANK ";
            $query .= "         AND L2.NAMECD1 = 'H312' ";
            $query .= " WHERE ";
            $query .= "     T1.SCHREGNO = '{$model->schregno}' AND ";
            $query .= "     T1.YEAR     = '".CTRL_YEAR."' ";
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
                $query .= "     T1.SCHREGNO = '".$model->schregno."' AND ";
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
            $query .= "     T1.SUBCLASSCD, ";
            $query .= "     CASE WHEN T1.CONDITION_DIV = '1' ";
            $query .= "          THEN '資格' ";
            $query .= "          ELSE 'その他' ";
            $query .= "     END AS CONDITION_DIV, ";
            $query .= "     T1.SEQ, ";
            $query .= "     T1.CONTENTS, ";
            $query .= "     T1.REMARK, ";
            $query .= "     T1.CREDITS, ";
            $query .= "     T5.CREDITS AS CREDIT ";
            $query .= " FROM ";
            $query .= "     SCHREG_QUALIFIED_HOBBY_DAT T1 ";
            $query .= "     LEFT JOIN SUBCLASS_MST T2 ";
            $query .= "          ON T1.SUBCLASSCD        = T2.SUBCLASSCD ";
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "         AND T1.CLASSCD          = T2.CLASSCD ";
                $query .= "         AND T1.SCHOOL_KIND      = T2.SCHOOL_KIND ";
                $query .= "         AND T1.CURRICULUM_CD    = T2.CURRICULUM_CD ";
            }
            $query .= "     LEFT JOIN SCHREG_REGD_DAT T4 ";
            $query .= "          ON T4.SCHREGNO     = T1.SCHREGNO ";
            $query .= "         AND T4.YEAR         = T1.YEAR ";
            $query .= "         AND T4.SEMESTER     = '".CTRL_SEMESTER."' ";
            $query .= "     LEFT JOIN CREDIT_MST T5 ";
            $query .= "          ON T5.YEAR         = T1.YEAR ";
            $query .= "         AND T5.COURSECD     = T4.COURSECD ";
            $query .= "         AND T5.MAJORCD      = T4.MAJORCD ";
            $query .= "         AND T5.GRADE        = T4.GRADE ";
            $query .= "         AND T5.COURSECODE   = T4.COURSECODE ";
            $query .= "         AND T5.CLASSCD      = SUBSTR(T1.SUBCLASSCD,1,2) ";
            $query .= "         AND T5.SUBCLASSCD   = T1.SUBCLASSCD ";
            //教育課程対応
            if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "         AND T5.SCHOOL_KIND      = T1.SCHOOL_KIND ";
            $query .= "         AND T5.CURRICULUM_CD    = T1.CURRICULUM_CD ";
            }
            $query .= " WHERE ";
            $query .= "     T1.SCHREGNO = '{$model->schregno}' AND ";
            $query .= "     T1.YEAR     = '".CTRL_YEAR."' ";
            $query .= " ORDER BY  ";
            $query .= "     T1.REGDDATE, ";
            //教育課程対応
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= " T1.CLASSCD, ";
                $query .= " T1.SCHOOL_KIND, ";
                $query .= " T1.CURRICULUM_CD, ";
            }
            $query .= "     T1.SUBCLASSCD, ";
            $query .= "     T1.SEQ ";
        }

        return $query;
    }

    //記録備考参照
    function getSchregClubHdetailDat($model) {
        $query  = " SELECT DISTINCT ";
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
        $query .= "     T1.SCHREGNO = '".$model->schregno."' AND ";
        $query .= "     FISCALYEAR(T1.DETAIL_DATE) = '".CTRL_YEAR."' ";
        if ($model->Properties["useSchool_KindField"] == "1") {
            $query .= " AND T1.SCHOOLCD    = '".sprintf("%012d", SCHOOLCD)."' ";
            $query .= " AND T1.SCHOOL_KIND = '".$model->schKind."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     T1.HOSTCD, ";
        $query .= "     T1.DETAIL_DATE DESC, ";
        $query .= "     T1.DETAIL_SEQ ";

        return $query;
    }
}
?>
