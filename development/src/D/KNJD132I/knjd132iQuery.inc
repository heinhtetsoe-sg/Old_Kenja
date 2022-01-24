<?php

require_once('for_php7.php');

class knjd132iQuery extends Query {

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
    function getHreportremarkDat($schregno, $semester) {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     HREPORTREMARK_DAT ";
        $query .= " WHERE ";
        $query .= "         YEAR     = '".CTRL_YEAR."' ";
        $query .= "     AND SEMESTER = '".$semester."' ";
        $query .= "     AND SCHREGNO = '".$schregno."'";

        return $query;
    }

    //PROGRAMPATH取得
    function getProgrampath() {
        $query  = " WITH MENU AS ( ";
        $query .= "     SELECT ";
        $query .= "         MIN(MENUID) AS MENUID ";
        $query .= "     FROM ";
        $query .= "         MENU_HIGH_SECURITY_MST ";
        $query .= "     WHERE ";
        $query .= "         PROGRAMID ='KNJD132I' ";
        $query .= " ) ";

        $query .= " SELECT ";
        $query .= "     PROGRAMPATH ";
        $query .= " FROM ";
        $query .= "     MENU_HIGH_SECURITY_MST T1, ";
        $query .= "     MENU T2 ";
        $query .= " WHERE ";
        $query .= "     T1.MENUID = T2.MENUID AND ";
        $query .= "     T1.PROGRAMID ='KNJD132I' ";

        return $query;
    }

    //部活動参照
    function getClub($model) {
        $sdate = $model->exp_year . '-04-01';
        $edate = ($model->exp_year + 1) . '-03-31';

        $query  = " SELECT ";
        $query .= "     T2.CLUBNAME, ";
        $query .= "     T1.SDATE, ";
        $query .= "     T1.EDATE, ";
        $query .= "     T3.NAME1 ";
        $query .= " FROM ";
        $query .= "     SCHREG_CLUB_HIST_DAT T1 ";
        $query .= " LEFT OUTER JOIN ";
        $query .= "     CLUB_MST T2 ON  T1.CLUBCD = T2.CLUBCD ";
        $query .= " LEFT OUTER JOIN ";
        $query .= "     NAME_MST T3 ON  T3.NAMECD1     = 'J001' ";
        $query .= "                 AND T1.EXECUTIVECD = T3.NAMECD2 ";
        $query .= " WHERE ";
        $query .= "     T1.SCHREGNO = '" .$model->schregno ."' AND ";
        $query .= "     ( ";
        $query .= "         (T1.SDATE                      <= '{$sdate}' AND VALUE(T1.EDATE, '9999-12-31') >= '{$edate}') OR ";
        $query .= "         (T1.SDATE                      >= '{$sdate}' AND T1.SDATE                      <= '{$edate}') OR ";
        $query .= "         (VALUE(T1.EDATE, '9999-12-31') >= '{$sdate}' AND VALUE(T1.EDATE, '9999-12-31') <= '{$edate}') ";
        $query .= "     ) ";
        $query .= " ORDER BY ";
        $query .= "     T1.SDATE, ";
        $query .= "     T1.CLUBCD ";

        return $query;
    }

    //委員会活動参照
    function getCommittee($model) {
        $query  = " SELECT ";
        $query .= "     T1.GRADE, ";
        $query .= "     T1.SEMESTER, ";
        $query .= "     T4.NAME1 AS SEMESTERNAME, ";
        $query .= "     T1.SEQ, ";
        $query .= "     T2.COMMITTEENAME, ";
        $query .= "     T1.CHARGENAME, ";
        $query .= "     T3.NAME1 ";
        $query .= " FROM ";
        $query .= "     SCHREG_COMMITTEE_HIST_DAT T1 ";
        $query .= " LEFT OUTER JOIN ";
        $query .= "     COMMITTEE_MST T2 ON  T1.COMMITTEE_FLG = T2.COMMITTEE_FLG ";
        $query .= "                      AND T1.COMMITTEECD   = T2.COMMITTEECD ";
        $query .= " LEFT OUTER JOIN ";
        $query .= "     NAME_MST T3 ON  T3.NAMECD1     = 'J002' ";
        $query .= "                 AND T1.EXECUTIVECD = T3.NAMECD2 ";
        $query .= " LEFT OUTER JOIN ";
        $query .= "     NAME_MST T4 ON  T4.NAMECD1      = 'J004' ";
        $query .= "                 AND T1.SEMESTER     = T4.NAMECD2 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR     = '" .$model->exp_year."' AND ";
        $query .= "     T1.SCHREGNO = '" .$model->schregno ."' ";
        $query .= " ORDER BY ";
        $query .= "     T1.GRADE, ";
        $query .= "     T1.SEMESTER, ";
        $query .= "     T1.SEQ ";

        return $query;
    }

    //記録備考参照
    function getSchregClubHdetailDat($model) {
        $query  = " SELECT ";
        $query .= "     T1.CLUBCD, ";
        $query .= "     L1.CLUBNAME, ";
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
        $query .= "     LEFT JOIN CLUB_HOST_MST L2 ON T1.HOSTCD = L2.HOSTCD ";
        $query .= "     LEFT JOIN CLUB_RECORD_MST L3 ON T1.RECORDCD = L3.RECORDCD ";
        $query .= " WHERE ";
        $query .= "     T1.SCHREGNO = '".$model->schregno."' ";
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
    
    /********/
    /* 更新 */
    /********/
    function &getInsertUpdateQuery($model, $schregno, $semester) {
        $db = Query::dbCheckOut();

        //存在チェック
        $check = $db->getCol(knjd132iQuery::getHreportremarkDat($schregno, $semester));

        if(get_count($check) == 0) {
            $data["YEAR"][TEXT]                 = CTRL_YEAR;
            $data["SEMESTER"][TEXT]             = $semester;
            $data["SCHREGNO"][TEXT]             = $schregno;

            $data["SPECIALACTREMARK"][TEXT]     = $model->field["SPECIALACTREMARK"];
            $data["COMMUNICATION"][TEXT]        = $model->field["COMMUNICATION"];

            $data["REGISTERCD"][TEXT]           = STAFFCD;
            $data["UPDATED"][NUMBER]            = "sysdate()";

            $db->query(Query::insertSQL($data, "HREPORTREMARK_DAT"));
        } else {
            $data["SPECIALACTREMARK"][TEXT]     = $model->field["SPECIALACTREMARK"];
            $data["COMMUNICATION"][TEXT]        = $model->field["COMMUNICATION"];
            $data["REGISTERCD"][TEXT]           = STAFFCD;
            $data["UPDATED"][FUNC]              = "sysdate()";

            $where  = " WHERE ";
            $where .= "     YEAR     = '".CTRL_YEAR."' AND ";
            $where .= "     SEMESTER = '".$semester."' AND ";
            $where .= "     SCHREGNO = '".$schregno."' ";

            $db->query(Query::updateSQL($data, "HREPORTREMARK_DAT", $where));
        }

        Query::dbCheckIn($db);
        return ;
    }
}
?>
