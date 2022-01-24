<?php

require_once('for_php7.php');

class knjd135pquery extends Query
{

    //校種取得
    public function getSchoolKind($grade)
    {
        $query  = " SELECT ";
        $query .= "     SCHOOL_KIND ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_GDAT ";
        $query .= " WHERE ";
        $query .= "         YEAR  = '".CTRL_YEAR."' ";
        $query .= "     AND GRADE = '{$grade}' ";

        return $query;
    }

    //学校名取得
    public function getNameMstZ010($model)
    {
        $query  = " SELECT ";
        $query .= "     NAME1 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND NAMECD1 = 'Z010' ";
        $query .= "     AND NAMECD2 = '00' ";

        return $query;
    }

    //学期一覧取得
    public function getSemesterList($model)
    {
        $query  = " SELECT ";
        $query .= "     SEMESTER AS VALUE, ";
        $query .= "     SEMESTERNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SEMESTER_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        if ($model->schoolName == 'miyagiken' || $model->schoolName == 'rakunan' || $model->schoolName == 'seijyo' || $model->Properties["knjd135pSemes9NotHyoji"] == "1") {
            $query .= "     AND SEMESTER <> '9' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //年組一覧取得
    public function getGradeHrclass($model, $semester)
    {
        $query  = " SELECT ";
        $query .= "     T1.GRADE || T1.HR_CLASS AS VALUE, ";
        $query .= "     T1.HR_NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_HDAT T1 ";
        $query .= " LEFT JOIN SCHREG_REGD_GDAT T2 ON T2.YEAR  = T1.YEAR";
        $query .= "                              AND T2.GRADE = T1.GRADE";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' AND ";
        $query .= "     T1.SEMESTER = '".$semester."' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= " AND T2.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind), "','")."') ";
            }
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND T2.SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        if (AUTHORITY == DEF_UPDATE_RESTRICT || $AUTHORITY == DEF_REFER_RESTRICT) {
            $query .= "     AND (TR_CD1     = '".STAFFCD."' OR ";
            $query .= "          TR_CD2     = '".STAFFCD."' OR ";
            $query .= "          TR_CD3     = '".STAFFCD."' OR ";
            $query .= "          SUBTR_CD1  = '".STAFFCD."' OR ";
            $query .= "          SUBTR_CD2  = '".STAFFCD."' OR ";
            $query .= "          SUBTR_CD3  = '".STAFFCD."') ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //全体のデータの取得
    public function selectQuery($model, $semester)
    {
        $query  = " WITH COMMUNICATION AS ( ";
        $query .= "     SELECT ";
        $query .= "         SCHREGNO, ";
        $query .= "         COMMUNICATION, ";
        $query .= "         ATTENDREC_REMARK ";
        $query .= "     FROM ";
        $query .= "         HREPORTREMARK_DAT ";
        $query .= "     WHERE ";
        $query .= "         YEAR = '".CTRL_YEAR."' AND ";
        $query .= "         SEMESTER = '".$model->field["SEMESTER"]."' ";
        $query .= " ), SCHINFO AS ( ";
        $query .= "     SELECT ";
        $query .= "         T1.SCHREGNO, ";
        $query .= "         T1.ATTENDNO, ";
        $query .= "         T2.NAME ";
        $query .= "     FROM ";
        $query .= "         SCHREG_REGD_DAT T1, ";
        $query .= "         SCHREG_BASE_MST T2 ";
        $query .= "     WHERE ";
        $query .= "         T1.YEAR = '".CTRL_YEAR."' AND ";
        $query .= "         T1.SEMESTER = '".$semester."' AND ";
        $query .= "         T1.GRADE || T1.HR_CLASS = '".$model->field["GRADE_HR_CLASS"]."' AND ";
        $query .= "         T1.SCHREGNO = T2.SCHREGNO ";
        $query .= " ) ";

        $query .= " SELECT ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     T1.ATTENDNO, ";
        $query .= "     T1.NAME, ";
        $query .= "     T2.COMMUNICATION, ";
        $query .= "     T2.ATTENDREC_REMARK ";
        $query .= " FROM ";
        $query .= "     SCHINFO T1 ";
        $query .= "     LEFT JOIN COMMUNICATION T2 ON T1.SCHREGNO = T2.SCHREGNO ";
        $query .= " ORDER BY ";
        $query .= "     T1.ATTENDNO ";

        return $query;
    }

    /* データ更新処理 */
    public function update($model)
    {
        $db = Query::dbCheckOut();

        for ($i = 0; $i < count($model->data["SCHREGNO"]); $i++) {
            $schregno = $model->data["SCHREGNO"][$i];

            //項目
            $data = array();
            $data["YEAR"][TEXT]             = CTRL_YEAR;
            $data["SEMESTER"][TEXT]         = $model->field["SEMESTER"];
            $data["SCHREGNO"][TEXT]         = $schregno;
            $data["COMMUNICATION"][TEXT]    = $model->fields["COMMUNICATION"][$i];
            $data["ATTENDREC_REMARK"][TEXT] = $model->fields["ATTENDREC_REMARK"][$i];
            $data["REGISTERCD"][TEXT]       = STAFFCD ;
            $data["UPDATED"][FUNC]          = "sysdate()";

            //存在チェック
            $query  = " SELECT COUNT(*) FROM HREPORTREMARK_DAT";
            $query .= " WHERE ";
            $query .= "     YEAR = '".CTRL_YEAR."' AND ";
            $query .= "     SEMESTER = '".$model->field["SEMESTER"]."' AND ";
            $query .= "     SCHREGNO = '".$schregno."' ";

            if (0 < $db->getOne($query)) {
                //更新
                $where  = " WHERE ";
                $where .= "     YEAR = '".CTRL_YEAR."' AND ";
                $where .= "     SEMESTER = '".$model->field["SEMESTER"]."' AND ";
                $where .= "     SCHREGNO = '".$schregno."' ";
                $query = Query::updateSQL($data, "HREPORTREMARK_DAT", $where);
            } else {
                //追加
                $query = Query::insertSQL($data, "HREPORTREMARK_DAT");
            }
            $db->query($query);
        }
        Query::dbCheckIn($db);
    }

    /*===================================定型文選択===================================*/

    //日付範囲取得
    public function getClubDate($model, $schregno)
    {
        $query  = " SELECT ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     CASE WHEN L1.ENT_DATE IS NOT NULL THEN L1.ENT_DATE ELSE T2.ENT_DATE END AS ENT_DATE, ";
        $query .= "     CASE WHEN L1.ENT_DATE IS NOT NULL THEN VALUE(L1.GRD_DATE,'9999-12-31') ELSE VALUE(T2.GRD_DATE,'9999-12-31') END AS GRD_DATE ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1 ";
        $query .= "     INNER JOIN SCHREG_BASE_MST T2 ON T1.SCHREGNO = T2.SCHREGNO ";
        $query .= "     INNER JOIN SCHREG_REGD_GDAT T3 ON T1.YEAR = T3.YEAR AND T1.GRADE = T3.GRADE ";
        $query .= "     LEFT JOIN SCHREG_ENT_GRD_HIST_DAT L1 ON T1.SCHREGNO = L1.SCHREGNO AND T3.SCHOOL_KIND = L1.SCHOOL_KIND ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR     = '".CTRL_YEAR."' AND ";
        $query .= "     T1.SEMESTER = '".$model->field["SEMESTER"]."' AND ";
        $query .= "     T1.SCHREGNO = '".$schregno."' ";

        return $query;
    }

    //部活動取得
    public function getClub($model, $schregno, $sdate, $edate)
    {
        $query  = " SELECT ";
        $query .= "     T1.CLUBCD, ";
        $query .= "     T2.CLUBNAME AS CLUB_SHOW, ";  //*
        $query .= "     T1.SDATE, ";
        $query .= "     T1.EDATE, ";
        $query .= "     T3.NAME1 AS EXECUTIVE_SHOW, ";  //*
        $query .= "     T1.REMARK ";
        $query .= " FROM ";
        $query .= "     SCHREG_CLUB_HIST_DAT T1 ";
        $query .= " LEFT OUTER JOIN ";
        $query .= "     CLUB_MST T2 ON  T1.CLUBCD = T2.CLUBCD ";
        if ($model->Properties["useSchool_KindField"] == "1") {
            $query .= "             AND T1.SCHOOLCD     = T2.SCHOOLCD ";
            $query .= "             AND T1.SCHOOL_KIND  = T2.SCHOOL_KIND ";
        }
        $query .= " LEFT OUTER JOIN ";
        $query .= "     NAME_MST T3 ON  T3.NAMECD1     = 'J001' ";
        $query .= "                 AND T1.EXECUTIVECD = T3.NAMECD2 ";
        $query .= " WHERE ";
        $query .= "     T1.SCHREGNO = '" .$schregno ."' AND ";
        $query .= "     ( ";
        $query .= "         (T1.SDATE                      <= '{$sdate}' AND VALUE(T1.EDATE, '9999-12-31') >= '{$edate}') OR ";
        $query .= "         (T1.SDATE                      >= '{$sdate}' AND T1.SDATE                      <= '{$edate}') OR ";
        $query .= "         (VALUE(T1.EDATE, '9999-12-31') >= '{$sdate}' AND VALUE(T1.EDATE, '9999-12-31') <= '{$edate}') ";
        $query .= "     ) ";
        if ($model->Properties["useClubMultiSchoolKind"] == "1") {
            $query .= " AND T1.SCHOOLCD    = '".sprintf("%012d", SCHOOLCD)."' ";
            $query .= " AND T1.SCHOOL_KIND = '".SCHOOLKIND."' ";
        } elseif ($model->Properties["useSchool_KindField"] == "1") {
            $query .= " AND T1.SCHOOLCD    = '".sprintf("%012d", SCHOOLCD)."' ";
            $query .= " AND T1.SCHOOL_KIND IN ( SELECT ";
            $query .= "                             S2.SCHOOL_KIND ";
            $query .= "                         FROM ";
            $query .= "                             SCHREG_REGD_DAT S1, ";
            $query .= "                             SCHREG_REGD_GDAT S2 ";
            $query .= "                         WHERE ";
            $query .= "                             S1.YEAR     = S2.YEAR AND ";
            $query .= "                             S1.YEAR     = '".CTRL_YEAR."' AND ";
            $query .= "                             S1.SEMESTER = '".$model->field["SEMESTER"]."' AND ";
            $query .= "                             S1.SCHREGNO = T1.SCHREGNO AND ";
            $query .= "                             S1.GRADE    = S2.GRADE ) ";
        }
        $query .= " ORDER BY ";
        $query .= "     T1.SDATE, ";
        $query .= "     T1.CLUBCD ";

        return $query;
    }

    //記録備考参照
    public function getSchregClubHdetailDat($model, $schregno)
    {
        ////distinctしているので、項目を削るとレコード件数が変わってしまうので、項目は削れない事に注意。 "*"が利用項目。
        $query  = "";
        $query .= " SELECT DISTINCT ";
        $query .= "     T1.CLUBCD, ";
        $query .= "     L1.CLUBNAME AS CLUB_SHOW, ";  //*
        $query .= "     T1.HOSTCD, ";
        $query .= "     L2.HOSTNAME, ";
        $query .= "     T1.MEET_NAME, ";    //*
        $query .= "     T1.DETAIL_DATE, ";  //*
        $query .= "     T1.DETAIL_SEQ, ";
        $query .= "     CASE T1.DIV WHEN '1' THEN '個人' WHEN '2' THEN '団体' ELSE '' END AS DIV_NAME, ";  //*
        $query .= "     T1.RECORDCD, ";
        $query .= "     L3.RECORDNAME, ";  //*
        $query .= "     T1.ITEMCD, ";
        $query .= "     T1.KINDCD, ";
        $query .= "     L4.KINDNAME, ";  //*
        $query .= "     T1.DOCUMENT, ";  //*
        $query .= "     T1.DETAIL_REMARK ";  //*
        $query .= " FROM ";
        $query .= "     SCHREG_CLUB_HDETAIL_DAT T1 ";
        $query .= "     LEFT JOIN CLUB_MST L1 ";
        $query .= "          ON T1.CLUBCD       = L1.CLUBCD ";
        if ($model->Properties["useSchool_KindField"] == "1") {
            $query .= "         AND T1.SCHOOLCD     = L1.SCHOOLCD ";
            $query .= "         AND T1.SCHOOL_KIND  = L1.SCHOOL_KIND ";
        }
        $query .= "     LEFT JOIN CLUB_HOST_MST L2 ";
        $query .= "          ON T1.HOSTCD       = L2.HOSTCD ";
        if ($model->Properties["useClubMultiSchoolKind"] == "1") {
            $query .= "         AND T1.SCHOOLCD             = L2.SCHOOLCD ";
            $query .= "         AND T1.DETAIL_SCHOOL_KIND   = L2.SCHOOL_KIND ";
        } elseif ($model->Properties["useSchool_KindField"] == "1") {
            $query .= "         AND T1.SCHOOLCD     = L2.SCHOOLCD ";
            $query .= "         AND T1.SCHOOL_KIND  = L2.SCHOOL_KIND ";
        }
        $query .= "     LEFT JOIN CLUB_RECORD_MST L3 ";
        $query .= "          ON T1.RECORDCD     = L3.RECORDCD ";
        if ($model->Properties["useClubMultiSchoolKind"] == "1") {
            $query .= "         AND T1.SCHOOLCD             = L3.SCHOOLCD ";
            $query .= "         AND T1.DETAIL_SCHOOL_KIND   = L3.SCHOOL_KIND ";
        } elseif ($model->Properties["useSchool_KindField"] == "1") {
            $query .= "         AND T1.SCHOOLCD     = L3.SCHOOLCD ";
            $query .= "         AND T1.SCHOOL_KIND  = L3.SCHOOL_KIND ";
        }
        $query .= "     LEFT JOIN CLUB_ITEM_KIND_MST L4 ";
        $query .= "          ON T1.ITEMCD       = L4.ITEMCD ";
        $query .= "         AND T1.KINDCD       = L4.KINDCD ";
        if ($model->Properties["useClubMultiSchoolKind"] == "1") {
            $query .= "         AND T1.SCHOOLCD             = L4.SCHOOLCD ";
            $query .= "         AND T1.DETAIL_SCHOOL_KIND   = L4.SCHOOL_KIND ";
        } elseif ($model->Properties["useSchool_KindField"] == "1") {
            $query .= "         AND T1.SCHOOLCD     = L4.SCHOOLCD ";
            $query .= "         AND T1.SCHOOL_KIND  = L4.SCHOOL_KIND ";
        }
        $query .= " WHERE ";
        $query .= "     T1.SCHREGNO = '".$schregno."' ";
        $query .= " AND FISCALYEAR(T1.DETAIL_DATE) = '".CTRL_YEAR."' ";
        if ($model->Properties["useClubMultiSchoolKind"] == "1") {
            $query .= " AND T1.SCHOOLCD     = '".sprintf("%012d", SCHOOLCD)."' ";
            $query .= " AND T1.SCHOOL_KIND  = '".SCHOOLKIND."' ";
        } elseif ($model->Properties["useSchool_KindField"] == "1") {
            $query .= " AND T1.SCHOOLCD     = '".sprintf("%012d", SCHOOLCD)."' ";
            $query .= " AND T1.SCHOOL_KIND IN ( SELECT ";
            $query .= "                             S2.SCHOOL_KIND ";
            $query .= "                         FROM ";
            $query .= "                             SCHREG_REGD_DAT S1, ";
            $query .= "                             SCHREG_REGD_GDAT S2 ";
            $query .= "                         WHERE ";
            $query .= "                             S1.YEAR     = S2.YEAR AND ";
            $query .= "                             S1.YEAR     = '".CTRL_YEAR."' AND ";
            $query .= "                             S1.SEMESTER = '".$model->field["SEMESTER"]."' AND ";
            $query .= "                             S1.GRADE    = S2.GRADE AND ";
            $query .= "                             S1.SCHREGNO = '".$schregno."') ";
        }
        $query .= " ORDER BY ";
        $query .= "     T1.HOSTCD, ";
        $query .= "     T1.DETAIL_DATE DESC, ";
        $query .= "     T1.DETAIL_SEQ ";

        return $query;
    }

    //対象年度取得
    public function getCommitteeRegdYear($model, $schregno)
    {
        $query  = " WITH MAIN AS ( ";
        $query .= "     SELECT DISTINCT ";
        $query .= "         T1.YEAR, ";
        $query .= "         T2.SCHOOL_KIND ";
        $query .= "     FROM ";
        $query .= "         SCHREG_REGD_DAT T1 ";
        $query .= "         INNER JOIN SCHREG_REGD_GDAT T2 ON T1.YEAR = T2.YEAR AND T1.GRADE = T2.GRADE ";
        $query .= "     WHERE ";
        $query .= "         T1.SCHREGNO = '".$schregno."' ";
        $query .= " ) ";

        $query .= " SELECT ";
        $query .= "     YEAR ";
        $query .= " FROM ";
        $query .= "     MAIN ";
        $query .= " WHERE ";
        $query .= "     SCHOOL_KIND IN (SELECT SCHOOL_KIND FROM MAIN WHERE YEAR = '".CTRL_YEAR."') ";

        return $query;
    }

    //委員会活動参照
    public function getCommittee($model, $year, $schregno)
    {
        $query  = " SELECT ";
        $query .= "     T1.GRADE, ";
        $query .= "     G1.GRADE_NAME1, ";
        $query .= "     T1.SEMESTER, ";
        $query .= "     L3.NAME1 AS SEMESTER_SHOW, ";  //*
        $query .= "     T1.SEQ, ";
        $query .= "     L1.COMMITTEENAME AS COMMITTEE_SHOW, ";  //*
        $query .= "     T1.CHARGENAME AS CHARGE_SHOW, ";  //*
        $query .= "     L2.NAME1 AS EXECUTIVE_SHOW ";  //*
        $query .= " FROM ";
        $query .= "     SCHREG_COMMITTEE_HIST_DAT T1 ";
        $query .= "     INNER JOIN SCHREG_REGD_GDAT G1 ";
        $query .= "          ON T1.YEAR         = G1.YEAR ";
        $query .= "         AND T1.GRADE        = G1.GRADE ";
        if ($model->Properties["useSchool_KindField"] == "1") {
            $query .= "     AND T1.SCHOOL_KIND  = G1.SCHOOL_KIND ";
        }
        $query .= "     LEFT JOIN COMMITTEE_MST L1 ";
        $query .= "          ON T1.COMMITTEE_FLG    = L1.COMMITTEE_FLG ";
        $query .= "         AND T1.COMMITTEECD      = L1.COMMITTEECD ";
        if ($model->Properties["useSchool_KindField"] == "1") {
            $query .= "     AND T1.SCHOOLCD         = L1.SCHOOLCD ";
            $query .= "     AND T1.SCHOOL_KIND      = L1.SCHOOL_KIND ";
        }
        $query .= "     LEFT JOIN NAME_MST L2 ";
        $query .= "          ON L2.NAMECD1      = 'J002' ";
        $query .= "         AND T1.EXECUTIVECD  = L2.NAMECD2 ";
        $query .= "     LEFT JOIN NAME_MST L3 ";
        $query .= "          ON L3.NAMECD1      = 'J004' ";
        $query .= "         AND T1.SEMESTER     = L3.NAMECD2 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".$year."' AND ";
        $query .= "     T1.SCHREGNO     = '".$schregno."' ";
        if ($model->Properties["useSchool_KindField"] == "1") {
            $query .= " AND T1.SCHOOLCD     = '".sprintf("%012d", SCHOOLCD)."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     T1.GRADE, ";
        $query .= "     T1.SEMESTER, ";
        $query .= "     T1.SEQ ";

        return $query;
    }

    //学籍資格データよりデータを取得
    public function getQualified($model, $db, $schregno)
    {
        $query  = "";
        if ($model->Properties["useQualifiedMst"] == '1') {
            $query .= " SELECT ";
            $query .= "     T1.YEAR, ";
            $query .= "     T1.SCHREGNO, ";
            $query .= "     T1.SEQ, ";
            $query .= "     T1.REGDDATE, ";  //*
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
            $query .= "     END AS CONDITION_DIV, ";  //*
            $query .= "     T1.QUALIFIED_CD, ";
            $query .= "     L1.QUALIFIED_NAME AS QUALIFIED_SHOW, ";  //*
            $query .= "     L1.QUALIFIED_ABBV, ";
            $query .= "     L1.PROMOTER, ";  //*
            $query .= "     T1.CONTENTS, ";
            $query .= "     L2.NAME1 AS RANK, ";  //*
            $query .= "     T1.SCORE, ";    //*
            $query .= "     T1.REMARK, ";   //*
            $query .= "     T1.CREDITS ";
            $query .= " FROM ";
            $query .= "     SCHREG_QUALIFIED_HOBBY_DAT T1 ";
            $query .= " LEFT JOIN ";
            $query .= "     QUALIFIED_MST L1 ON L1.QUALIFIED_CD = T1.QUALIFIED_CD ";
            $query .= " LEFT JOIN ";
            $query .= "     NAME_MST L2 ON  L2.NAMECD2 = T1.RANK ";
            $query .= "                 AND L2.NAMECD1 = 'H312' ";
            if (0 < $db->getOne(knjd135pquery::getAwardSyscatColumns("QUALIFIED_RANK_DAT"))) {
                $query .= " INNER JOIN ";
                $query .= "     QUALIFIED_RANK_DAT L3 ON L3.QUALIFIED_CD = T1.QUALIFIED_CD ";
                $query .= "                          AND L3.RANK = T1.RANK ";
                $query .= "                          AND L3.NOT_PRINT IS NULL ";
            }
            $query .= " WHERE ";
            $query .= "     T1.YEAR     = '".CTRL_YEAR."' AND ";
            $query .= "     T1.SCHREGNO = '".$schregno."' ";
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
                $query .= "     NULL AS SCORE, ";
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
                if (0 < $db->getOne(knjd135pquery::getAwardSyscatColumns("QUALIFIED_RESULT_MST"))) {
                    $query .= " INNER JOIN ";
                    $query .= "     QUALIFIED_RESULT_MST L3 ON L3.YEAR = T1.YEAR ";
                    $query .= "                            AND L3.QUALIFIED_CD = T1.QUALIFIED_CD ";
                    $query .= "                            AND L3.RESULT_CD = T1.RESULT_CD ";
                    $query .= "                            AND L3.NOT_PRINT IS NULL ";
                }
                $query .= " WHERE ";
                $query .= "     T1.YEAR     = '".CTRL_YEAR."' AND ";
                $query .= "     T1.SCHREGNO = '".$schregno."' AND ";
                $query .= "     T1.RESULT_CD NOT IN ('8888', '9999') "; //欠席・不合格を除く
            }
            $query .= " ORDER BY ";
            $query .= "     REGDDATE, ";
            $query .= "     SUBCLASSCD, ";
            $query .= "     SEQ ";
        } else {
            $query .= " SELECT ";
            $query .= "     T1.YEAR, ";
            $query .= "     T1.REGDDATE, ";  //*
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "     T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD AS SUBCLASSCD, ";
            } else {
                $query .= "     T1.SUBCLASSCD, ";
            }
            $query .= "     CASE WHEN T1.CONDITION_DIV = '1' ";
            $query .= "          THEN T1.CONDITION_DIV || ':資格' ";
            $query .= "          ELSE T1.CONDITION_DIV || ':その他' ";
            $query .= "          END AS CONDITION_DIV, ";  //*
            $query .= "     T1.SEQ, ";
            $query .= "     T1.CONTENTS, ";  //*
            $query .= "     T1.SCORE, ";
            $query .= "     T1.REMARK, ";    //*
            $query .= "     T1.CREDITS, ";
            $query .= "     T5.CREDITS AS CREDIT ";
            $query .= " FROM ";
            $query .= "     SCHREG_QUALIFIED_HOBBY_DAT T1 ";
            $query .= "     LEFT JOIN SUBCLASS_MST T2 ON T1.SUBCLASSCD = T2.SUBCLASSCD ";
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "                                  AND T1.CLASSCD         = T2.CLASSCD ";
                $query .= "                                  AND T1.SCHOOL_KIND     = T2.SCHOOL_KIND ";
                $query .= "                                  AND T1.CURRICULUM_CD   = T2.CURRICULUM_CD ";
            }
            $query .= "       LEFT JOIN SCHREG_REGD_DAT T4 ON T4.SCHREGNO = T1.SCHREGNO ";
            $query .= "                                   AND T4.YEAR     = T1.YEAR ";
            $query .= "                                   AND T4.SEMESTER = '".$model->field["SEMESTER"]."' ";
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
            if (0 < $db->getOne(knjd135pquery::getAwardSyscatColumns("QUALIFIED_RANK_DAT"))) {
                $query .= " INNER JOIN ";
                $query .= "     QUALIFIED_RANK_DAT L3 ON L3.QUALIFIED_CD = T1.QUALIFIED_CD ";
                $query .= "                          AND L3.RANK = T1.RANK ";
                $query .= "                          AND L3.NOT_PRINT IS NULL ";
            }
            $query .= " WHERE ";
            $query .= "     T1.YEAR     = '".CTRL_YEAR."' AND ";
            $query .= "     T1.SCHREGNO = '".$schregno."' ";
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

    //NOT_PRINTがテーブルにあるかどうか取得
    public function getAwardSyscatColumns($tableName)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(COLNAME) ";
        $query .= " FROM ";
        $query .= "     SYSCAT.COLUMNS ";
        $query .= " WHERE ";
        $query .= "     TABNAME = '".$tableName."' ";
        $query .= "     AND COLNAME = 'NOT_PRINT' ";

        return $query;
    }

    //賞データ取得
    public function getHyosyo($model, $schregno)
    {
        $query  = "";
        if ($model->send_prgid == 'KNJE020') {
            $query .= " WITH T_YEAR AS ( ";
            $query .= "     SELECT DISTINCT ";
            $query .= "         R1.YEAR ";
            $query .= "     FROM ";
            $query .= "         SCHREG_REGD_DAT T1 ";
            $query .= "         INNER JOIN NAME_MST T2 ON T2.NAMECD1 = 'A023' AND T1.GRADE BETWEEN T2.NAME2 AND T2.NAME3, ";
            $query .= "         SCHREG_REGD_DAT R1 ";
            $query .= "     WHERE ";
            $query .= "         T1.YEAR     = '".CTRL_YEAR."' AND ";
            $query .= "         T1.SEMESTER = '".$model->field["SEMESTER"]."' AND ";
            $query .= "         T1.SCHREGNO = '".$schregno."' AND ";
            $query .= "         T1.SCHREGNO = R1.SCHREGNO AND ";
            $query .= "         R1.GRADE BETWEEN T2.NAME2 AND T2.NAME3 ";
            $query .= " ) ";
        }
        $query .= " SELECT ";
        $query .= "     DETAIL_SDATE, ";  //*
        $query .= "     DETAILCD, ";
        $query .= "     L1.NAME1 AS DETAILCDNAME, ";  //*
        $query .= "     CONTENT AS CONTENT, ";  //*
        $query .= "     REMARK ";  //*
        $query .= " FROM ";
        $query .= "     SCHREG_DETAILHIST_DAT T1 ";
        $query .= "     LEFT JOIN NAME_MST L1 ON L1.NAMECD1 = 'H303' AND L1.NAMECD2 = T1.DETAILCD ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO     = '".$schregno."' AND ";
        $query .= "     DETAIL_DIV   = '1' AND ";
        $query .= "     FISCALYEAR(DETAIL_SDATE) = '".CTRL_YEAR."' ";
        $query .= " ORDER BY ";
        $query .= "     DETAIL_SDATE ";

        return $query;
    }
}
