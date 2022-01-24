<?php

require_once('for_php7.php');

class knjd131tQuery extends Query {

    //学期取得
    function getSemester($model) {
        $query  = " SELECT ";
        $query .= "     SEMESTER AS VALUE, ";
        $query .= "     SEMESTERNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SEMESTER_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    // HREPORTREMARK_DAT取得
    function getHreportremarkDat($model) {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     HREPORTREMARK_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR        = '".$model->exp_year."' AND ";
        $query .= "     SEMESTER    = '".$model->field["SEMESTER"]."' AND ";
        $query .= "     SCHREGNO    = '".$model->schregno."' ";

        return $query;
    }

    //総学講座名取込
    function getSougakuChairName($model) {
        $query   = "  SELECT DISTINCT ";
        $query  .= "    T2.CHAIRNAME ";
        $query  .= "  FROM ";
        $query  .= "    CHAIR_STD_DAT T1 ";
        $query  .= "    INNER JOIN CHAIR_DAT T2 ";
        $query  .= "      ON T1.YEAR = T2.YEAR ";
        $query  .= "      AND T1.SEMESTER = T2.SEMESTER ";
        $query  .= "      AND T1.CHAIRCD = T2.CHAIRCD ";
        $query  .= "  WHERE ";
        $query  .= "    T1.YEAR = '".CTRL_YEAR."' ";

        // 「9:学期(学期末)」の場合は最終学期の講座名を取得
        if ($model->field["SEMESTER"] != "9") {
            $query  .= "    AND T1.SEMESTER = '".$model->field["SEMESTER"]."' ";
        } else {
            $query  .= "    AND T1.SEMESTER = (SELECT MAX(SEMESTER) FROM SEMESTER_MST WHERE YEAR = '".CTRL_YEAR."' AND SEMESTER <> '9') ";
        }
        $query  .= "    AND T1.SCHREGNO = '".$model->schregno."' ";
        $query  .= "    AND T2.CLASSCD = '90' ";

        return $query;
    }

    //部活動参照
    function getClub($model) {
        $query  = " SELECT DISTINCT ";
        $query .= "     L2.CLUBNAME, ";
        $query .= "     T1.CLUBCD, ";
        $query .= "     L1.DETAIL_DATE, ";
        $query .= "     L1.DETAIL_REMARK ";
        $query .= " FROM ";
        $query .= "     SCHREG_CLUB_HIST_DAT T1 ";
        $query .= " LEFT JOIN ";
        $query .= "     SCHREG_CLUB_HDETAIL_DAT L1 ON  L1.SCHREGNO = T1.SCHREGNO ";
        $query .= "                                AND L1.CLUBCD = T1.CLUBCD ";
        $query .= " LEFT JOIN ";
        $query .= "     CLUB_MST L2 ON L2.CLUBCD = T1.CLUBCD ";
        $query .= " WHERE ";
        $query .= "     T1.SCHREGNO = '{$model->schregno}' ";
        $query .= " ORDER BY ";
        $query .= "     T1.CLUBCD, ";
        $query .= "     L1.DETAIL_DATE ";

        return $query;
    }

    //委員会活動参照
    function getCommittee($model) {
        $query  = " SELECT ";
        $query .= "     L1.SEQ, ";
        $query .= "     L2.COMMITTEENAME, ";
        $query .= "     T1.CHARGENAME, ";
        $query .= "     L1.DETAIL_DATE, ";
        $query .= "     L1.DETAIL_REMARK ";
        $query .= " FROM ";
        $query .= "     SCHREG_COMMITTEE_HIST_DAT T1 ";
        $query .= " LEFT JOIN ";
        $query .= "     SCHREG_COMMITTEE_HDETAIL_DAT L1 ON  L1.YEAR = T1.YEAR ";
        $query .= "                                     AND L1.SEQ  = T1.SEQ ";
        $query .= " LEFT JOIN ";
        $query .= "     COMMITTEE_MST L2 ON  L2.COMMITTEE_FLG = T1.COMMITTEE_FLG ";
        $query .= "                      AND L2.COMMITTEECD   = T1.COMMITTEECD ";
        $query .= " WHERE ";
        $query .= "         T1.YEAR     = '".CTRL_YEAR."' ";
        $query .= "     AND T1.SCHREGNO = '{$model->schregno}' ";
        $query .= " ORDER BY ";
        $query .= "     T1.COMMITTEECD, ";
        $query .= "     L1.DETAIL_DATE ";

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
            $query .= " LEFT JOIN ";
            $query .= "     QUALIFIED_MST L1 ON L1.QUALIFIED_CD = T1.QUALIFIED_CD ";
            $query .= " LEFT JOIN ";
            $query .= "     NAME_MST L2 ON  L2.NAMECD2 = T1.RANK ";
            $query .= "                 AND L2.NAMECD1 = 'H312' ";
            $query .= " WHERE ";
            $query .= "     T1.YEAR     = '{$model->exp_year}' AND ";
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
                $query .= "     T1.YEAR     = '{$model->exp_year}' AND ";
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
            $query .= "       LEFT OUTER JOIN SUBCLASS_MST T2 ON T1.SUBCLASSCD = T2.SUBCLASSCD ";
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "                                  AND T1.CLASSCD         = T2.CLASSCD ";
                $query .= "                                  AND T1.SCHOOL_KIND     = T2.SCHOOL_KIND ";
                $query .= "                                  AND T1.CURRICULUM_CD   = T2.CURRICULUM_CD ";
            }
            $query .= "       LEFT JOIN SCHREG_REGD_DAT T4 ON T4.SCHREGNO = T1.SCHREGNO ";
            $query .= "                                   AND T4.YEAR     = T1.YEAR ";
            $query .= "                                   AND T4.SEMESTER = '".CTRL_SEMESTER."' ";
            $query .= "       LEFT JOIN CREDIT_MST T5 ON T5.YEAR       = T1.YEAR ";
            $query .= "                              AND T5.COURSECD   = T4.COURSECD ";
            $query .= "                              AND T5.MAJORCD    = T4.MAJORCD ";
            $query .= "                              AND T5.GRADE      = T4.GRADE ";
            $query .= "                              AND T5.COURSECODE = T4.COURSECODE ";
            $query .= "                              AND T5.CLASSCD    = SUBSTR(T1.SUBCLASSCD,1,2) ";
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "                          AND T5.SCHOOL_KIND     = T1.SCHOOL_KIND ";
                $query .= "                          AND T5.CURRICULUM_CD   = T1.CURRICULUM_CD ";
            }
            $query .= "                              AND T5.SUBCLASSCD = T1.SUBCLASSCD ";
            $query .= " WHERE ";
            $query .= "     T1.YEAR     = '{$model->exp_year}' AND ";
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

    //更新処理
    function &getUpdateQuery($model) {
        $db = Query::dbCheckOut();

        // HREPORTREMARK_DAT
        //存在チェック
        $chk = $db->getCol(knjd131tQuery::getHreportremarkDat($model));

        if (get_count($chk) == 0) {
            $data = array();
            $data["YEAR"][TEXT]                 = $model->exp_year;
            $data["SEMESTER"][TEXT]             = $model->field["SEMESTER"];
            $data["SCHREGNO"][TEXT]             = $model->schregno;
            $data["REMARK1"][TEXT]              = $model->field["REMARK1"];
            $data["REMARK2"][TEXT]              = $model->field["REMARK2"];
            $data["REMARK3"][TEXT]              = $model->field["REMARK3"];
            $data["COMMUNICATION"][TEXT]        = $model->field["COMMUNICATION"];
            $data["REGISTERCD"][TEXT]           = STAFFCD;
            $data["UPDATED"][FUNC]              = "sysdate()";

            $db->query(Query::insertSQL($data, "HREPORTREMARK_DAT"));
        } else {
            $data = array();
            $data["REMARK1"][TEXT]              = $model->field["REMARK1"];
            $data["REMARK2"][TEXT]              = $model->field["REMARK2"];
            $data["REMARK3"][TEXT]              = $model->field["REMARK3"];
            $data["COMMUNICATION"][TEXT]        = $model->field["COMMUNICATION"];
            $data["REGISTERCD"][TEXT]           = STAFFCD;
            $data["UPDATED"][FUNC]              = "sysdate()";

            $where  = " WHERE ";
            $where .= "     YEAR     = '".$model->exp_year."' AND ";
            $where .= "     SEMESTER = '".$model->field["SEMESTER"]."' AND ";
            $where .= "     SCHREGNO = '".$model->schregno."' ";

            $db->query(Query::updateSQL($data, "HREPORTREMARK_DAT", $where));
        }

        Query::dbCheckIn($db);
        return ;
    }
}
?>
