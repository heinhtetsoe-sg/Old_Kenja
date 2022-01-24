<?php

require_once('for_php7.php');

class knjxclub_detailQuery extends Query {

    //リスト取得
    function getList($model)
    {
        $sdate = CTRL_YEAR.'-04-01';
        $edate = (CTRL_YEAR+1).'-03-31';

        $query  = " WITH CLUB AS ( ";
        $query .= "     SELECT ";
        $query .= "         T1.* ";
        $query .= "     FROM ";
        $query .= "         CLUB_MST T1, ";
        $query .= "         CLUB_YDAT T2 ";
        $query .= "     WHERE ";
        $query .= "         T1.CLUBCD = T2.CLUBCD AND ";
        $query .= "         T2.YEAR = '".CTRL_YEAR."' ";
        if ($model->Properties["useClubMultiSchoolKind"] == "1") {
            $query .= " AND T1.SCHOOLCD    = '".SCHOOLCD."' ";
            $query .= " AND T1.SCHOOL_KIND = '".SCHOOLKIND."' ";
            $query .= " AND T2.SCHOOLCD    = '".SCHOOLCD."' ";
            $query .= " AND T2.SCHOOL_KIND = '".SCHOOLKIND."' ";
        } else if ($model->Properties["use_prg_schoolkind"] == "1") {
            $query .= " AND T1.SCHOOLCD    = '".SCHOOLCD."' ";
            $query .= " AND T1.SCHOOL_KIND = '".$model->schKind."' ";
            $query .= " AND T2.SCHOOLCD    = '".SCHOOLCD."' ";
            $query .= " AND T2.SCHOOL_KIND = '".$model->schKind."' ";
        } else if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND T1.SCHOOLCD    = '".SCHOOLCD."' ";
            $query .= " AND T1.SCHOOL_KIND = '".SCHOOLKIND."' ";
            $query .= " AND T2.SCHOOLCD    = '".SCHOOLCD."' ";
            $query .= " AND T2.SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= " ), HIST AS ( ";
        $query .= "     SELECT ";
        $query .= "         CLUBCD, ";
        $query .= "         SDATE, ";
        $query .= "         CASE WHEN EDATE IS NULL THEN '9999-12-31' ELSE EDATE END AS EDATE ";
        $query .= "     FROM ";
        $query .= "         SCHREG_CLUB_HIST_DAT ";
        $query .= "     WHERE ";
        $query .= "         SCHREGNO = '".$model->schregno."' ";
        if ($model->Properties["useClubMultiSchoolKind"] == "1") {
            $query .= "    AND SCHOOLCD    = '".SCHOOLCD."' ";
            $query .= "    AND SCHOOL_KIND = '".SCHOOLKIND."' ";
        } else if ($model->Properties["use_prg_schoolkind"] == "1") {
            $query .= "    AND SCHOOLCD    = '".SCHOOLCD."' ";
            $query .= "    AND SCHOOL_KIND = '".$model->schKind."' ";
        } else if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= "    AND SCHOOLCD    = '".SCHOOLCD."' ";
            $query .= "    AND SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= " ), SCH_HIST AS ( ";
        $query .= "     SELECT ";
        $query .= "         T1.CLUBCD, ";
        $query .= "         T3.CLUBNAME, ";
        $query .= "         T1.SDATE, ";
        $query .= "         T1.EDATE ";
        $query .= "     FROM ";
        $query .= "         HIST T1 ";
        $query .= "         LEFT JOIN CLUB T3 ON T1.CLUBCD = T3.CLUBCD ";
        $query .= "     WHERE ";
        $query .= "         '".$sdate."' < T1.EDATE AND ";
        $query .= "         '".$edate."' > T1.SDATE ";
        $query .= " ) ";
        $query .= " SELECT DISTINCT ";
        $query .= "     T1.*, ";
        $query .= "     T2.CLUBNAME, ";
        $query .= "     T3.HOSTCD || ':' || T3.HOSTNAME AS HOSTCD, ";
        $query .= "     T4.ITEMCD || ':' || T4.ITEMNAME AS ITEMCD, ";
        $query .= "     T5.KINDCD || ':' || T5.KINDNAME AS KINDCD, ";
        $query .= "     T6.RECORDCD || ':' || T6.RECORDNAME AS RECORDCD ";
        $query .= " FROM ";
        $query .= "     SCHREG_CLUB_HDETAIL_DAT T1 ";
        $query .= "     LEFT JOIN SCH_HIST T2 ON T1.CLUBCD = T2.CLUBCD ";
        $query .= "     LEFT JOIN CLUB_HOST_MST T3 ON T1.HOSTCD = T3.HOSTCD ";
        if ($model->Properties["useClubMultiSchoolKind"] == "1") {
            $query .= "    AND T3.SCHOOLCD    = '".SCHOOLCD."' ";
            //(登録時の)生徒のSCHOOL_KINDを利用
            $query .= "    AND T3.SCHOOL_KIND = T1.DETAIL_SCHOOL_KIND ";
        } else if ($model->Properties["use_prg_schoolkind"] == "1") {
            $query .= "    AND T3.SCHOOLCD    = '".SCHOOLCD."' ";
            $query .= "    AND T3.SCHOOL_KIND = '".$model->schKind."' ";
        } else if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= "    AND T3.SCHOOLCD    = '".SCHOOLCD."' ";
            $query .= "    AND T3.SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= "     LEFT JOIN CLUB_ITEM_MST T4 ON T1.ITEMCD = T4.ITEMCD ";
        if ($model->Properties["useClubMultiSchoolKind"] == "1") {
            $query .= "    AND T4.SCHOOLCD    = '".SCHOOLCD."' ";
            //(登録時の)生徒のSCHOOL_KINDを利用
            $query .= "    AND T4.SCHOOL_KIND = T1.DETAIL_SCHOOL_KIND ";
        } else if ($model->Properties["use_prg_schoolkind"] == "1") {
            $query .= "    AND T4.SCHOOLCD    = '".SCHOOLCD."' ";
            $query .= "    AND T4.SCHOOL_KIND = '".$model->schKind."' ";
        } else if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= "    AND T4.SCHOOLCD    = '".SCHOOLCD."' ";
            $query .= "    AND T4.SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= "     LEFT JOIN CLUB_ITEM_KIND_MST T5 ON T1.ITEMCD = T5.ITEMCD AND T1.KINDCD = T5.KINDCD";
        if ($model->Properties["useClubMultiSchoolKind"] == "1") {
            $query .= "    AND T5.SCHOOLCD    = '".SCHOOLCD."' ";
            //(登録時の)生徒のSCHOOL_KINDを利用
            $query .= "    AND T5.SCHOOL_KIND = T1.DETAIL_SCHOOL_KIND ";
        } else if ($model->Properties["use_prg_schoolkind"] == "1") {
            $query .= "    AND T5.SCHOOLCD    = '".SCHOOLCD."' ";
            $query .= "    AND T5.SCHOOL_KIND = '".$model->schKind."' ";
        } else if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= "    AND T5.SCHOOLCD    = '".SCHOOLCD."' ";
            $query .= "    AND T5.SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= "     LEFT JOIN CLUB_RECORD_MST T6 ON T1.RECORDCD = T6.RECORDCD ";
        if ($model->Properties["useClubMultiSchoolKind"] == "1") {
            $query .= "    AND T6.SCHOOLCD    = '".SCHOOLCD."' ";
            //(登録時の)生徒のSCHOOL_KINDを利用
            $query .= "    AND T6.SCHOOL_KIND = T1.DETAIL_SCHOOL_KIND ";
        } else if ($model->Properties["use_prg_schoolkind"] == "1") {
            $query .= "    AND T6.SCHOOLCD    = '".SCHOOLCD."' ";
            $query .= "    AND T6.SCHOOL_KIND = '".$model->schKind."' ";
        } else if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= "    AND T6.SCHOOLCD    = '".SCHOOLCD."' ";
            $query .= "    AND T6.SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= " WHERE ";
        $query .= "     T1.SCHREGNO = '".$model->schregno."' AND ";
        $query .= "     '".$sdate."' <= T1.DETAIL_DATE AND ";
        $query .= "     '".$edate."' >= T1.DETAIL_DATE AND ";
        $query .= "     T1.DETAIL_DATE BETWEEN T2.SDATE AND T2.EDATE ";
        if($model->programid == "KNJJ040"){
            $query .= "     AND T1.CLUBCD = '".$model->g_clubcd."' ";
        }
        if ($model->Properties["useClubMultiSchoolKind"] == "1") {
            $query .= "    AND T1.SCHOOLCD    = '".SCHOOLCD."' ";
            $query .= "    AND T1.SCHOOL_KIND = '".SCHOOLKIND."' ";
        } else if ($model->Properties["use_prg_schoolkind"] == "1") {
            $query .= "    AND T1.SCHOOLCD    = '".SCHOOLCD."' ";
            $query .= "    AND T1.SCHOOL_KIND = '".$model->schKind."' ";
        } else if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= "    AND T1.SCHOOLCD    = '".SCHOOLCD."' ";
            $query .= "    AND T1.SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     T1.CLUBCD, ";
        $query .= "     T1.DETAIL_DATE, ";
        $query .= "     T1.DETAIL_SEQ ";

        return $query;
    }

    //生徒情報取得
    function getSchinfo($model)
    {
        $query  = " SELECT ";
        $query .= "     T3.HR_NAME || ' ' || T1.ATTENDNO || '番　　' || T2.NAME_SHOW ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1 ";
        $query .= "     LEFT JOIN SCHREG_BASE_MST T2 ON ";
        $query .= "         T1.SCHREGNO = T2.SCHREGNO ";
        $query .= "     LEFT JOIN SCHREG_REGD_HDAT T3 ON ";
        $query .= "         T1.YEAR = T3.YEAR AND ";
        $query .= "         T1.SEMESTER = T3.SEMESTER AND ";
        $query .= "         T1.GRADE = T3.GRADE AND ";
        $query .= "         T1.HR_CLASS = T3.HR_CLASS ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' AND ";
        $query .= "     T1.SEMESTER = '".CTRL_SEMESTER."' AND ";
        $query .= "     T1.SCHREGNO = '".$model->schregno."' ";

        return $query;
    }

    //選択した生徒の校種を取得
    function getSchKind($model) {
        $query .= " SELECT DISTINCT ";
        $query .= "     REGDG.SCHOOL_KIND ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT REGD ";
        $query .= "     INNER JOIN SCHREG_REGD_GDAT REGDG ON REGD.YEAR = REGDG.YEAR ";
        $query .= "           AND REGD.GRADE = REGDG.GRADE ";
        $query .= "     LEFT JOIN SEMESTER_MST SM ON REGD.YEAR = SM.YEAR ";
        $query .= "          AND SM.SEMESTER = '9' ";
        $query .= " WHERE ";
        $query .= "     REGD.SCHREGNO = '".$model->schregno."' ";
        $query .= "     AND REGD.YEAR = '".CTRL_YEAR."' ";

        return $query;
    }

    //登録データの校種を取得
    function getDetailSchKind($model) {
        $date = str_replace("/", "-", $model->date);
        $query .= " SELECT  ";
        $query .= "     DETAIL_SCHOOL_KIND ";
        $query .= " FROM ";
        $query .= "     SCHREG_CLUB_HDETAIL_DAT ";
        $query .= " WHERE ";
        $query .= "   SCHOOLCD = '".SCHOOLCD."' ";
        $query .= "   AND SCHOOL_KIND = '".SCHOOLKIND."' ";
        $query .= "   AND SCHREGNO = '".$model->schregno."' ";
        $query .= "   AND CLUBCD = '".$model->clubcd."' ";
        $query .= "   AND DETAIL_DATE = '".$date."' ";
        $query .= "   AND DETAIL_SEQ = ".$model->seq." ";

        return $query;
    }

    //１レコード取得
    function getRow($model)
    {
        $date = str_replace("/", "-", $model->date);

        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     SCHREG_CLUB_HDETAIL_DAT ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO = '".$model->schregno."' AND ";
        $query .= "     CLUBCD = '".$model->clubcd."' AND ";
        $query .= "     DETAIL_DATE = '".$date."' AND ";
        $query .= "     DETAIL_SEQ = ".$model->seq." ";
        if ($model->Properties["useClubMultiSchoolKind"] == "1") {
            $query .= "    AND SCHOOLCD    = '".SCHOOLCD."' ";
            $query .= "    AND SCHOOL_KIND = '".SCHOOLKIND."' ";
        } else if ($model->Properties["use_prg_schoolkind"] == "1") {
            $query .= "    AND SCHOOLCD    = '".SCHOOLCD."' ";
            $query .= "    AND SCHOOL_KIND = '".$model->schKind."' ";
        } else if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= "    AND SCHOOLCD    = '".SCHOOLCD."' ";
            $query .= "    AND SCHOOL_KIND = '".SCHOOLKIND."' ";
        }

        return $query;
    }

    //部クラブ取得
    function getClubName($model)
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     T1.CLUBCD || ' ' || T3.CLUBNAME AS LABEL, ";
        $query .= "     T1.CLUBCD AS VALUE ";
        $query .= " FROM ";
        $query .= "     SCHREG_CLUB_HIST_DAT T1, ";
        $query .= "     CLUB_YDAT T2 ";
        $query .= "     LEFT JOIN CLUB_MST T3 ON T2.CLUBCD = T3.CLUBCD ";
        if ($model->Properties["useClubMultiSchoolKind"] == "1") {
            $query .= "    AND T3.SCHOOLCD    = '".SCHOOLCD."' ";
            $query .= "    AND T3.SCHOOL_KIND = '".SCHOOLKIND."' ";
        } else if ($model->Properties["use_prg_schoolkind"] == "1") {
            $query .= "    AND T3.SCHOOLCD    = '".SCHOOLCD."' ";
            $query .= "    AND T3.SCHOOL_KIND = '".$model->schKind."' ";
        } else if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= "    AND T3.SCHOOLCD    = '".SCHOOLCD."' ";
            $query .= "    AND T3.SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= " WHERE ";
        $query .= "     T1.SCHREGNO = '".$model->schregno."' AND ";
        $query .= "     T2.YEAR = '".CTRL_YEAR."' AND ";
        $query .= "     T1.CLUBCD = T2.CLUBCD ";
        if($model->programid == "KNJJ040"){
            $query .= "     AND T1.CLUBCD = '".$model->g_clubcd."' ";
        }
        if ($model->Properties["useClubMultiSchoolKind"] == "1") {
            $query .= "    AND T1.SCHOOLCD    = '".SCHOOLCD."' ";
            $query .= "    AND T1.SCHOOL_KIND = '".SCHOOLKIND."' ";
            $query .= "    AND T2.SCHOOLCD    = '".SCHOOLCD."' ";
            $query .= "    AND T2.SCHOOL_KIND = '".SCHOOLKIND."' ";
        } else if ($model->Properties["use_prg_schoolkind"] == "1") {
            $query .= "    AND T1.SCHOOLCD    = '".SCHOOLCD."' ";
            $query .= "    AND T1.SCHOOL_KIND = '".$model->schKind."' ";
            $query .= "    AND T2.SCHOOLCD    = '".SCHOOLCD."' ";
            $query .= "    AND T2.SCHOOL_KIND = '".$model->schKind."' ";
        } else if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= "    AND T1.SCHOOLCD    = '".SCHOOLCD."' ";
            $query .= "    AND T1.SCHOOL_KIND = '".SCHOOLKIND."' ";
            $query .= "    AND T2.SCHOOLCD    = '".SCHOOLCD."' ";
            $query .= "    AND T2.SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //大会一覧取得
    function getMeetList($model) {
        $query  = " SELECT DISTINCT ";
        $query .= "     MEET_NAME AS VALUE, ";
        $query .= "     MEET_NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "    (SELECT DISTINCT ";
        $query .= "         MEET_NAME ";
        $query .= "     FROM ";
        $query .= "         SCHREG_CLUB_HDETAIL_DAT ";
        $query .= "     WHERE ";
        $query .= "         REPLACE(MEET_NAME, '　', '') <> '' ";
        if ($model->Properties["useClubMultiSchoolKind"] == "1") {
            $query .= " AND SCHOOLCD    = '".SCHOOLCD."' ";
            $query .= " AND SCHOOL_KIND = '".SCHOOLKIND."' ";
            $query .= " AND DETAIL_SCHOOL_KIND = '".$model->schreg_schkind."' ";
        } else if ($model->Properties["use_prg_schoolkind"] == "1") {
            $query .= " AND SCHOOLCD    = '".SCHOOLCD."' ";
            $query .= " AND SCHOOL_KIND = '".$model->schKind."' ";
        } else if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND SCHOOLCD    = '".SCHOOLCD."' ";
            $query .= " AND SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= "     GROUP BY ";
        $query .= "         MEET_NAME ";
        $query .= "     UNION ";
        $query .= "     SELECT DISTINCT ";
        $query .= "         MEET_NAME ";
        $query .= "     FROM ";
        $query .= "         GROUP_CLUB_HDETAIL_DAT ";
        $query .= "     WHERE ";
        $query .= "         REPLACE(MEET_NAME, '　', '') <> '' ";
        if ($model->Properties["useClubMultiSchoolKind"] == "1") {
            $query .= " AND SCHOOLCD    = '".SCHOOLCD."' ";
            $query .= " AND SCHOOL_KIND = '".SCHOOLKIND."' ";
            $query .= " AND DETAIL_SCHOOL_KIND = '".$model->schreg_schkind."' ";
        } else if ($model->Properties["use_prg_schoolkind"] == "1") {
            $query .= " AND SCHOOLCD    = '".SCHOOLCD."' ";
            $query .= " AND SCHOOL_KIND = '".$model->schkind."' ";
        } else if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND SCHOOLCD    = '".SCHOOLCD."' ";
            $query .= " AND SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= "     ) T1 ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //開催地域取得
    function getClubHost($model)
    {
        $query  = " SELECT ";
        $query .= "     HOSTCD || ' ' || HOSTNAME AS LABEL, ";
        $query .= "     HOSTCD AS VALUE ";
        $query .= " FROM ";
        $query .= "     CLUB_HOST_MST ";
        if ($model->Properties["useClubMultiSchoolKind"] == "1") {
            $query .= " WHERE SCHOOLCD = '".SCHOOLCD."' ";
            //初期表示、クラブ選択時それぞれに応じた校種の情報を取得する
            $query .= " AND SCHOOL_KIND = '".$model->schreg_schkind."' ";
        } else if ($model->Properties["use_prg_schoolkind"] == "1") {
            $query .= " WHERE SCHOOLCD = '".SCHOOLCD."' ";
            $query .= " AND SCHOOL_KIND = '".$model->schKind."' ";
        } else if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " WHERE SCHOOLCD = '".SCHOOLCD."' ";
            $query .= " AND SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //種目取得
    function getClubItem($model, $clubcd)
    {
        $query  = " SELECT ";
        $query .= "     T1.ITEMCD || ' ' || T2.ITEMNAME AS LABEL, ";
        $query .= "     T1.ITEMCD AS VALUE ";
        $query .= " FROM ";
        $query .= "     CLUB_ITEM_DAT T1 ";
        $query .= "     LEFT JOIN CLUB_ITEM_MST T2 ON T1.ITEMCD = T2.ITEMCD ";
        if ($model->Properties["useClubMultiSchoolKind"] == "1") {
            $query .= "    AND T2.SCHOOLCD    = T1.SCHOOLCD ";
            $query .= "    AND T2.SCHOOL_KIND = T1.SCHOOL_KIND ";
        } else if ($model->Properties["use_prg_schoolkind"] == "1") {
            $query .= "    AND T2.SCHOOLCD    = '".SCHOOLCD."' ";
            $query .= "    AND T2.SCHOOL_KIND = '".$model->schKind."' ";
        } else if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= "    AND T2.SCHOOLCD    = '".SCHOOLCD."' ";
            $query .= "    AND T2.SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= " WHERE ";
        $query .= "     T1.CLUBCD = '".$clubcd."' ";
        if ($model->Properties["useClubMultiSchoolKind"] == "1") {
            $query .= "    AND T1.SCHOOLCD    = '".SCHOOLCD."' ";
            //初期表示、クラブ選択時それぞれに応じた校種の情報を取得する
            $query .= "    AND T1.SCHOOL_KIND = '".$model->schreg_schkind."' ";
        } else if ($model->Properties["use_prg_schoolkind"] == "1") {
            $query .= "    AND T1.SCHOOLCD    = '".SCHOOLCD."' ";
            $query .= "    AND T1.SCHOOL_KIND = '".$model->schKind."' ";
        } else if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= "    AND T1.SCHOOLCD    = '".SCHOOLCD."' ";
            $query .= "    AND T1.SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //種類取得
    function getClubItemKind($model, $itemcd)
    {
        $query  = " SELECT ";
        $query .= "     KINDCD || ' ' || KINDNAME AS LABEL, ";
        $query .= "     KINDCD AS VALUE ";
        $query .= " FROM ";
        $query .= "     CLUB_ITEM_KIND_MST ";
        $query .= " WHERE ";
        $query .= "     ITEMCD = '".$itemcd."' ";
        if ($model->Properties["useClubMultiSchoolKind"] == "1") {
            $query .= "    AND SCHOOLCD    = '".SCHOOLCD."' ";
            //初期表示、クラブ選択時それぞれに応じた校種を指定
            $query .= " AND SCHOOL_KIND = '".$model->schreg_schkind."' ";
        } else if ($model->Properties["use_prg_schoolkind"] == "1") {
            $query .= "    AND SCHOOLCD    = '".SCHOOLCD."' ";
            $query .= "    AND SCHOOL_KIND = '".$model->schKind."' ";
        } else if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= "    AND SCHOOLCD    = '".SCHOOLCD."' ";
            $query .= "    AND SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //成績取得
    function getClubRecord($model)
    {
        $query  = " SELECT ";
        $query .= "     RECORDCD || ' ' || RECORDNAME AS LABEL, ";
        $query .= "     RECORDCD AS VALUE ";
        $query .= " FROM ";
        $query .= "     CLUB_RECORD_MST ";
        if ($model->Properties["useClubMultiSchoolKind"] == "1") {
            $query .= " WHERE SCHOOLCD = '".SCHOOLCD."' ";
            //初期表示、クラブ選択時それぞれに応じた校種を指定
            $query .= " AND SCHOOL_KIND = '".$model->schreg_schkind."' ";
        } else if ($model->Properties["use_prg_schoolkind"] == "1") {
            $query .= " WHERE SCHOOLCD = '".SCHOOLCD."' ";
            $query .= " AND SCHOOL_KIND = '".$model->schKind."' ";
        } else if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " WHERE SCHOOLCD = '".SCHOOLCD."' ";
            $query .= " AND SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //在籍チェック
    function checkClubExist($model)
    {
        $date = str_replace("/", "-", $model->field["DETAIL_DATE"]);

        $query  = " WITH CLUB_HIST AS( ";
        $query .= "     SELECT ";
        $query .= "         SDATE, ";
        $query .= "         CASE WHEN EDATE IS NULL THEN '9999-12-31' ELSE EDATE END AS EDATE ";
        $query .= "     FROM ";
        $query .= "         SCHREG_CLUB_HIST_DAT ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO = '".$model->schregno."' AND ";
        if ($model->Properties["useClubMultiSchoolKind"] == "1") {
            $query .= "    SCHOOLCD    = '".SCHOOLCD."' AND ";
            $query .= "    SCHOOL_KIND = '".SCHOOLKIND."' AND ";
        } else if ($model->Properties["use_prg_schoolkind"] == "1") {
            $query .= "    SCHOOLCD    = '".SCHOOLCD."' AND ";
            $query .= "    SCHOOL_KIND = '".$model->schKind."' AND ";
        } else if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= "    SCHOOLCD    = '".SCHOOLCD."' AND ";
            $query .= "    SCHOOL_KIND = '".SCHOOLKIND."' AND ";
        }
        $query .= "     CLUBCD = '".$model->field["CLUBCD"]."' ) ";

        $query .= " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     CLUB_HIST ";
        $query .= " WHERE ";
        $query .= " '".$date."' BETWEEN SDATE AND EDATE ";

        return $query;
    }

    //SEQ取得
    function getSeq($model)
    {
        $date = str_replace("/", "-", $model->field["DETAIL_DATE"]);

        $query  = " SELECT ";
        $query .= "     MAX(DETAIL_SEQ) AS DETAIL_SEQ ";
        $query .= " FROM ";
        $query .= "     SCHREG_CLUB_HDETAIL_DAT ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO    = '".$model->schregno."' AND ";
        $query .= "     CLUBCD      = '".$model->field["CLUBCD"]."' AND ";
        $query .= "     DETAIL_DATE = '".$date."' ";
        if ($model->Properties["useClubMultiSchoolKind"] == "1") {
            $query .= "    AND SCHOOLCD     = '".SCHOOLCD."' ";
            $query .= "    AND SCHOOL_KIND  = '".SCHOOLKIND."' ";
        } else if ($model->Properties["use_prg_schoolkind"] == "1") {
            $query .= "    AND SCHOOLCD     = '".SCHOOLCD."' ";
            $query .= "    AND SCHOOL_KIND  = '".$model->schKind."' ";
        } else if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= "    AND SCHOOLCD     = '".SCHOOLCD."' ";
            $query .= "    AND SCHOOL_KIND  = '".SCHOOLKIND."' ";
        }

        return $query;
    }

    //GROUPCD取得
    function getGroupcd($model) {
        $date = str_replace("/", "-", $model->field["DETAIL_DATE"]);

        $query  = " SELECT ";
        $query .= "     MAX(GROUPCD) AS GROUPCD ";
        $query .= " FROM ";
        $query .= "     GROUP_CLUB_HDETAIL_DAT ";
        $query .= " WHERE ";
        $query .= "     CLUBCD      = '".$model->field["CLUBCD"]."' AND ";
        $query .= "     DETAIL_DATE = '".$date."' ";
        if ($model->Properties["useClubMultiSchoolKind"] == "1") {
            $query .= " AND SCHOOLCD    = '".SCHOOLCD."' ";
            $query .= " AND SCHOOL_KIND = '".SCHOOLKIND."' ";
        } else if ($model->Properties["use_prg_schoolkind"] == "1") {
            $query .= " AND SCHOOLCD    = '".SCHOOLCD."' ";
            $query .= " AND SCHOOL_KIND = '".$model->schkind."' ";
        } else if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND SCHOOLCD    = '".SCHOOLCD."' ";
            $query .= " AND SCHOOL_KIND = '".SCHOOLKIND."' ";
        }

        return $query;
    }

    //INSERT
    function &getInsertQuery($model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        if ($model->cmd == "update") {
            //GROUPCD
            $groupcd = $model->groupcd;
            //同じグループの学籍番号取得
            $schArray = $db->getCol(knjxclub_detailQuery::getGroupCnt($model, 'schno'));
        } else {
            $seq = $db->getOne(knjxclub_detailQuery::getSeq($model));
            $model->field["DETAIL_SEQ"] = ($seq) ? $seq+1 : 1;

            $groupcd = "";
            if ($model->field["DIV"] == "2") {
                //GROUPCD取得
                $groupcd = $db->getOne(knjxclub_detailQuery::getGroupcd($model));
                $groupcd = ($groupcd) ? $groupcd+1 : 1;
            }
            //学籍番号セット
            $schArray = array($model->schregno);
        }


        foreach ($schArray as $schregno) {
            // SCHREG_CLUB_HDETAIL_DAT
            $data = array();
            if ($model->cmd == "add") {
                if ($model->Properties["useClubMultiSchoolKind"] == "1") {
                    $data["SCHOOLCD"][TEXT]         = SCHOOLCD;
                    $data["SCHOOL_KIND"][TEXT]      = SCHOOLKIND;
                    $data["DETAIL_SCHOOL_KIND"][TEXT] = $model->schreg_schkind;
                } else if ($model->Properties["use_prg_schoolkind"] == "1") {
                    $data["SCHOOLCD"][TEXT]         = SCHOOLCD;
                    $data["SCHOOL_KIND"][TEXT]      = $model->schKind;
                } else if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
                    $data["SCHOOLCD"][TEXT]         = SCHOOLCD;
                    $data["SCHOOL_KIND"][TEXT]      = SCHOOLKIND;
                }
                $data["SCHREGNO"][TEXT]         = $model->schregno;
                $data["CLUBCD"][TEXT]           = $model->field["CLUBCD"];
                $data["DETAIL_DATE"][TEXT]      = str_replace("/", "-", $model->field["DETAIL_DATE"]);
                $data["DETAIL_SEQ"][NUMBER]     = $model->field["DETAIL_SEQ"];
            }
            $data["MEET_NAME"][TEXT]        = $model->field["MEET_NAME"];
            $data["DIV"][TEXT]              = $model->field["DIV"];
            $data["GROUPCD"][TEXT]          = $groupcd;
            $data["HOSTCD"][TEXT]           = $model->field["HOSTCD"];
            $data["ITEMCD"][TEXT]           = $model->field["ITEMCD"];
            $data["KINDCD"][TEXT]           = $model->field["KINDCD"];
            $data["RECORDCD"][TEXT]         = $model->field["RECORDCD"];
            $data["DOCUMENT"][TEXT]         = $model->field["DOCUMENT"];
            $data["DETAIL_REMARK"][TEXT]    = $model->field["DETAIL_REMARK"];
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][FUNC]          = "sysdate()";

            if ($model->cmd == "update") {
                $where  = " WHERE ";
                $where .= "     SCHREGNO    = '".$schregno."' ";
                $where .= " AND CLUBCD      = '".$model->field["CLUBCD"]."' ";
                $where .= " AND DETAIL_DATE = '".str_replace("/", "-", $model->field["DETAIL_DATE"])."' ";
                $where .= " AND GROUPCD     = '".$groupcd."' ";
                if ($model->Properties["useClubMultiSchoolKind"] == "1") {
                    $where .= " AND SCHOOLCD    = '".SCHOOLCD."'";
                    $where .= " AND SCHOOL_KIND = '".SCHOOLKIND."'";
                } else if ($model->Properties["use_prg_schoolkind"] == "1") {
                    $where .= " AND SCHOOLCD    = '".SCHOOLCD."'";
                    $where .= " AND SCHOOL_KIND = '".$model->schKind."'";
                } else if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
                    $where .= " AND SCHOOLCD    = '".SCHOOLCD."'";
                    $where .= " AND SCHOOL_KIND = '".SCHOOLKIND."'";
                }
                $query = Query::updateSQL($data, "SCHREG_CLUB_HDETAIL_DAT", $where);
            } else {
                $query = Query::insertSQL($data, "SCHREG_CLUB_HDETAIL_DAT");
            }
            $db->query($query);
        }

        if ($model->field["DIV"] == "2") {
            // GROUP_CLUB_HDETAIL_DAT
            $data = array();
            if ($model->cmd == "add") {
                if ($model->Properties["useClubMultiSchoolKind"] == "1") {
                    $data["SCHOOLCD"][TEXT]         = SCHOOLCD;
                    $data["SCHOOL_KIND"][TEXT]      = SCHOOLKIND;
                    $data["DETAIL_SCHOOL_KIND"][TEXT] = $model->schreg_schkind;
                } else if ($model->Properties["use_prg_schoolkind"] == "1") {
                    $data["SCHOOLCD"][TEXT]         = SCHOOLCD;
                    $data["SCHOOL_KIND"][TEXT]      = $model->schKind;
                } else if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
                    $data["SCHOOLCD"][TEXT]         = SCHOOLCD;
                    $data["SCHOOL_KIND"][TEXT]      = SCHOOLKIND;
                }
                $data["CLUBCD"][TEXT]           = $model->field["CLUBCD"];
                $data["DETAIL_DATE"][TEXT]      = str_replace("/", "-", $model->field["DETAIL_DATE"]);
                $data["GROUPCD"][TEXT]          = $groupcd;
            }
            $data["MEET_NAME"][TEXT]        = $model->field["MEET_NAME"];
            $data["HOSTCD"][TEXT]           = $model->field["HOSTCD"];
            $data["ITEMCD"][TEXT]           = $model->field["ITEMCD"];
            $data["KINDCD"][TEXT]           = $model->field["KINDCD"];
            $data["RECORDCD"][TEXT]         = $model->field["RECORDCD"];
            $data["DOCUMENT"][TEXT]         = $model->field["DOCUMENT"];
            $data["DETAIL_REMARK"][TEXT]    = $model->field["DETAIL_REMARK"];
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][FUNC]          = "sysdate()";

            if ($model->cmd == "update") {
                $where  = " WHERE ";
                $where .= "     CLUBCD      = '".$model->field["CLUBCD"]."' ";
                $where .= " AND DETAIL_DATE = '".str_replace("/", "-", $model->field["DETAIL_DATE"])."' ";
                $where .= " AND GROUPCD     = '".$groupcd."' ";
                if ($model->Properties["useClubMultiSchoolKind"] == "1") {
                    $where .= " AND SCHOOLCD    = '".SCHOOLCD."'";
                    $where .= " AND SCHOOL_KIND = '".SCHOOLKIND."'";
                } else if ($model->Properties["use_prg_schoolkind"] == "1") {
                    $where .= " AND SCHOOLCD    = '".SCHOOLCD."'";
                    $where .= " AND SCHOOL_KIND = '".$model->schKind."'";
                } else if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
                    $where .= " AND SCHOOLCD    = '".SCHOOLCD."'";
                    $where .= " AND SCHOOL_KIND = '".SCHOOLKIND."'";
                }
                $query = Query::updateSQL($data, "GROUP_CLUB_HDETAIL_DAT", $where);
            } else {
                $query = Query::insertSQL($data, "GROUP_CLUB_HDETAIL_DAT");
            }
            $db->query($query);
        }

        $db->commit();
        Query::dbCheckIn($db);
        return $model->field["DETAIL_SEQ"];
    }

    //DELETE
    function &getDeleteQuery($model)
    {
        $db = Query::dbCheckOut();

        $query  = " DELETE FROM ";
        $query .= "     SCHREG_CLUB_HDETAIL_DAT ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO = '".$model->schregno."' AND ";
        $query .= "     CLUBCD = '".$model->field["CLUBCD"]."' AND ";
        $query .= "     DETAIL_DATE = '".str_replace("/", "-", $model->field["DETAIL_DATE"])."' AND ";
        $query .= "     DETAIL_SEQ = ".$model->field["DETAIL_SEQ"]." ";
        if ($model->Properties["useClubMultiSchoolKind"] == "1") {
            $query .= "    AND SCHOOLCD    = '".SCHOOLCD."' ";
            $query .= "    AND SCHOOL_KIND = '".SCHOOLKIND."' ";
        } else if ($model->Properties["use_prg_schoolkind"] == "1") {
            $query .= "    AND SCHOOLCD    = '".SCHOOLCD."' ";
            $query .= "    AND SCHOOL_KIND = '".$model->schKind."' ";
        } else if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= "    AND SCHOOLCD    = '".SCHOOLCD."' ";
            $query .= "    AND SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $db->query($query);

        //同じグループの人数チェック
        $cnt = $db->getOne(knjxclub_detailQuery::getGroupCnt($model, 'cnt'));

        if ($cnt == 0) {
            //GROUP_CLUB_HDETAIL_DAT
            $query  = " DELETE FROM ";
            $query .= "     GROUP_CLUB_HDETAIL_DAT ";
            $query .= " WHERE ";
            $query .= "     CLUBCD      = '".$model->field["CLUBCD"]."' AND ";
            $query .= "     DETAIL_DATE = '".str_replace("/", "-", $model->field["DETAIL_DATE"])."' AND ";
            $query .= "     GROUPCD     = '".$model->groupcd."' ";
            if ($model->Properties["useClubMultiSchoolKind"] == "1") {
                $query .= " AND SCHOOLCD    = '".SCHOOLCD."' ";
                $query .= " AND SCHOOL_KIND = '".SCHOOLKIND."' ";
            } else if ($model->Properties["use_prg_schoolkind"] == "1") {
                $query .= " AND SCHOOLCD    = '".SCHOOLCD."' ";
                $query .= " AND SCHOOL_KIND = '".$model->schkind."' ";
            } else if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
                $query .= " AND SCHOOLCD    = '".SCHOOLCD."' ";
                $query .= " AND SCHOOL_KIND = '".SCHOOLKIND."' ";
            }
            $db->query($query);
        }

        Query::dbCheckIn($db);
        return;
    }

    //グループの人数チェック
    function getGroupCnt($model, $flg) {
        $query  = " SELECT ";
        if ($flg == 'cnt') {
            $query .= "     COUNT(SCHREGNO) ";
        } else {
            $query .= "     SCHREGNO ";
        }
        $query .= " FROM ";
        $query .= "     SCHREG_CLUB_HDETAIL_DAT ";
        $query .= " WHERE ";
        $query .= "     CLUBCD      = '".$model->field["CLUBCD"]."' AND ";
        $query .= "     DETAIL_DATE = '".str_replace("/", "-", $model->field["DETAIL_DATE"])."' AND ";
        $query .= "     GROUPCD     = '".$model->groupcd."' ";
        if ($model->Properties["useClubMultiSchoolKind"] == "1") {
            $query .= "    AND SCHOOLCD    = '".SCHOOLCD."' ";
            $query .= "    AND SCHOOL_KIND = '".SCHOOLKIND."' ";
        } else if ($model->Properties["use_prg_schoolkind"] == "1") {
            $query .= "    AND SCHOOLCD    = '".SCHOOLCD."' ";
            $query .= "    AND SCHOOL_KIND = '".$model->schKind."' ";
        } else if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= "    AND SCHOOLCD    = '".SCHOOLCD."' ";
            $query .= "    AND SCHOOL_KIND = '".SCHOOLKIND."' ";
        }

        return $query;
    }
}
?>
