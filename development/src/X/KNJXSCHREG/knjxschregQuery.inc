<?php

require_once('for_php7.php');

class knjxschregQuery extends Query {
    //名前
    function getName($schregno) {
        $query  = " SELECT ";
        $query .= "     NAME_SHOW ";
        $query .= " FROM ";
        $query .= "     SCHREG_BASE_MST ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO = '{$schregno}' ";

        return $query;
    }
    
    //復学あり
    function getComeBackT()
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     SYSCAT.COLUMNS ";
        $query .= " WHERE ";
        $query .= "     TABNAME = 'SCHREG_ENT_GRD_HIST_COMEBACK_DAT' ";

        return $query;
    }

    //復学日
    function getCB_entDate($model) {
        $query  = " WITH MIN_T AS ( ";
        $query .= " SELECT ";
        $query .= "     SCHREGNO, ";
        $query .= "     MIN(COMEBACK_DATE) AS COMEBACK_DATE ";
        $query .= " FROM ";
        $query .= "     SCHREG_ENT_GRD_HIST_COMEBACK_DAT ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO = '".$model->schregno."' ";
        $query .= "     AND SCHOOL_KIND = '". $model->schoolKind. "' ";
        $query .= " GROUP BY ";
        $query .= "     SCHREGNO ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     ENT_DATE ";
        $query .= " FROM ";
        $query .= "     SCHREG_ENT_GRD_HIST_COMEBACK_DAT T1, ";
        $query .= "     MIN_T ";
        $query .= " WHERE ";
        $query .= "     T1.SCHREGNO = MIN_T.SCHREGNO ";
        $query .= "     AND T1.SCHOOL_KIND = '". $model->schoolKind. "' ";
        $query .= "     AND T1.COMEBACK_DATE = MIN_T.COMEBACK_DATE ";

        return $query;
    }

    //年組,先生名
    function getGrd_ClasQuery($model)
    {
        $query  = "";
        $query .= "  SELECT DISTINCT";
        $query .= "      T1.grade || ',' || T1.hr_class AS gc,";
        $query .= "      T1.hr_name,";
        $query .= "      T2.staffname_show AS name_show,";
        $query .= "      T2.staffcd";
        $query .= "  FROM";
        $query .= "      schreg_regd_hdat T1 ";
        $query .= "      LEFT OUTER JOIN staff_mst T2 ON T1.tr_cd1 = T2.staffcd";
        $query .= "  WHERE";
        $query .= "      T1.year     = '".CTRL_YEAR."' AND";
        $query .= "      T1.semester = '".CTRL_SEMESTER."'";
        if (AUTHORITY == DEF_UPDATE_RESTRICT) {
            $query .= "AND (T1.tr_cd1    = '".STAFFCD."' OR T1.tr_cd2    = '".STAFFCD."' OR T1.tr_cd3    = '".STAFFCD."' OR ";
            $query .= "     T1.subtr_cd1 = '".STAFFCD."' OR T1.subtr_cd2 = '".STAFFCD."' OR T1.subtr_cd3 = '".STAFFCD."')";
        }
        $query .= "  ORDER BY";
        $query .= "      gc";
        return $query;
    }
    
    //各生徒情報
    function getStudent_data($schregno, $model)
    {
        $db = Query::dbCheckOut();

        $query  = "  SELECT";
        $query .= "      T1.SCHREGNO,";
        $query .= "      T2.ANNUAL,";
        $query .= "      T1.INOUTCD,";
        $query .= "      T1.NAME,";
        $query .= "      T1.NAME_SHOW,";
        $query .= "      T1.NAME_KANA,";
        $query .= "      T1.NAME_ENG,";
        $query .= "      T1.REAL_NAME,";
        $query .= "      T1.REAL_NAME_KANA,";
        $query .= "      T1.BIRTHDAY,";
        $query .= "      T1.SEX,";
        $query .= "      T1.BLOODTYPE,";
        $query .= "      T1.BLOOD_RH,";
        $query .= "      T1.HANDICAP,";
        $query .= "      T1.NATIONALITY,";
        $query .= "      CASE WHEN T3.SCHREGNO IS NOT NULL ";
        $query .= "           THEN T3.FINSCHOOLCD ";
        $query .= "           ELSE T1.FINSCHOOLCD ";
        $query .= "      END AS FINSCHOOLCD, ";
        $query .= "      CASE WHEN T3.SCHREGNO IS NOT NULL ";
        $query .= "           THEN T3.FINISH_DATE ";
        $query .= "           ELSE T1.FINISH_DATE ";
        $query .= "      END AS FINISH_DATE, ";
        $query .= "      T1.PRISCHOOLCD,";
        $query .= "      CASE WHEN T3.SCHREGNO IS NOT NULL ";
        $query .= "           THEN T3.ENT_DATE ";
        $query .= "           ELSE T1.ENT_DATE ";
        $query .= "      END AS ENT_DATE, ";
        $query .= "      T3.CURRICULUM_YEAR, ";
        $query .= "      CASE WHEN T3.SCHREGNO IS NOT NULL ";
        $query .= "           THEN T3.ENT_DIV ";
        $query .= "           ELSE T1.ENT_DIV ";
        $query .= "      END AS ENT_DIV, ";
        $query .= "      CASE WHEN T3.SCHREGNO IS NOT NULL ";
        $query .= "           THEN T3.ENT_REASON ";
        $query .= "           ELSE T1.ENT_REASON ";
        $query .= "      END AS ENT_REASON, ";
        $query .= "      CASE WHEN T3.SCHREGNO IS NOT NULL ";
        $query .= "           THEN T3.ENT_SCHOOL ";
        $query .= "           ELSE T1.ENT_SCHOOL ";
        $query .= "      END AS ENT_SCHOOL, ";
        $query .= "      CASE WHEN T3.SCHREGNO IS NOT NULL ";
        $query .= "           THEN T3.ENT_ADDR ";
        $query .= "           ELSE T1.ENT_ADDR ";
        $query .= "      END AS ENT_ADDR, ";
        if ($model->Properties["useAddrField2"] == "1" || $model->schoolKind == "H") {
            $query .= "      CASE WHEN T3.SCHREGNO IS NOT NULL ";
            $query .= "           THEN T3.ENT_ADDR2 ";
            $query .= "           ELSE T1.ENT_ADDR2 ";
            $query .= "      END AS ENT_ADDR2, ";
        }
        $query .= "      CASE WHEN T3.SCHREGNO IS NOT NULL ";
        $query .= "           THEN T3.GRD_DATE ";
        $query .= "           ELSE T1.GRD_DATE ";
        $query .= "      END AS GRD_DATE, ";
        $query .= "      CASE WHEN T3.SCHREGNO IS NOT NULL ";
        $query .= "           THEN T3.GRD_DIV ";
        $query .= "           ELSE T1.GRD_DIV ";
        $query .= "      END AS GRD_DIV, ";
        $query .= "      CASE WHEN T3.SCHREGNO IS NOT NULL ";
        $query .= "           THEN T3.GRD_REASON ";
        $query .= "           ELSE T1.GRD_REASON ";
        $query .= "      END AS GRD_REASON, ";
        $query .= "      CASE WHEN T3.SCHREGNO IS NOT NULL ";
        $query .= "           THEN T3.GRD_SCHOOL ";
        $query .= "           ELSE T1.GRD_SCHOOL ";
        $query .= "      END AS GRD_SCHOOL, ";
        $query .= "      CASE WHEN T3.SCHREGNO IS NOT NULL ";
        $query .= "           THEN T3.GRD_ADDR ";
        $query .= "           ELSE T1.GRD_ADDR ";
        $query .= "      END AS GRD_ADDR, ";
        if ($model->Properties["useAddrField2"] == "1" || $model->schoolKind == "H") {
            $query .= "      CASE WHEN T3.SCHREGNO IS NOT NULL ";
            $query .= "           THEN T3.GRD_ADDR2 ";
            $query .= "           ELSE T1.GRD_ADDR2 ";
            $query .= "      END AS GRD_ADDR2, ";
        }
        $query .= "      CASE WHEN T3.SCHREGNO IS NOT NULL ";
        $query .= "           THEN T3.GRD_NO ";
        $query .= "           ELSE T1.GRD_NO ";
        $query .= "      END AS GRD_NO, ";
        $query .= "      T1.REMARK1,";
        $query .= "      T1.REMARK2,";
        $query .= "      T1.REMARK3,";
        $query .= "      T1.TENGAKU_SAKI_ZENJITU,";
        $query .= "      T1.TENGAKU_SAKI_GRADE, ";
        $query .= "      T1.NYUGAKUMAE_SYUSSIN_JOUHOU,";
        $query .= "      T1.EXAMNO, ";
        $query .= "      T2.COURSECODE,";
        $query .= "      T1.UPDATED AS UPDATED1,";
        $query .= "      T2.ATTENDNO,";
        $query .= "      T2.COURSECD ||','|| T2.MAJORCD AS COURSEMAJORCD,";
        $query .= "      T2.UPDATED AS UPDATED2,";
        $query .= "      T2.GRADE,";
        $query .= "      T2.GRADE || ',' || T2.HR_CLASS AS GRCL";
        $query .= "  FROM";
        $query .= "      V_SCHREG_BASE_MST T1 ";
        $query .= "      LEFT JOIN SCHREG_REGD_DAT T2 ON T1.SCHREGNO = T2.SCHREGNO";
        $query .= "      LEFT JOIN SCHREG_ENT_GRD_HIST_DAT T3 ON T1.SCHREGNO = T3.SCHREGNO";
        $query .= "           AND T3.SCHOOL_KIND = '". $model->schoolKind. "' ";
        $query .= "  WHERE";
        $query .= "      T1.SCHREGNO = '". $schregno. "' AND";
        $query .= "      T2.YEAR     = '". CTRL_YEAR. "' AND";
        $query .= "      T2.SEMESTER = '". CTRL_SEMESTER. "'";

        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);

        return $row;
    }

    //更新するときに履歴に入れるデータを取ってくる
    function getStudent_data_before($model, $schregno) {
        $query  = " SELECT ";
        $query .= "     T2.YEAR, ";
        $query .= "     T2.SEMESTER, ";
        $query .= "     T2.GRADE, ";
        $query .= "     T2.HR_CLASS, ";
        $query .= "     T2.ATTENDNO, ";
        $query .= "     T2.ANNUAL, ";
        $query .= "     T2.COURSECD, ";
        $query .= "     T2.MAJORCD, ";
        $query .= "     T2.COURSECODE, ";
        $query .= "     T2.SCHREGNO, ";
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
        $query .= "     T1.FINSCHOOLCD, ";
        $query .= "     T1.FINISH_DATE, ";
        $query .= "     T1.ENT_DATE, ";
        $query .= "     T1.ENT_DIV, ";
        $query .= "     T1.ENT_REASON, ";
        $query .= "     T1.ENT_SCHOOL, ";
        $query .= "     T1.ENT_ADDR, ";
        if ($model->Properties["useAddrField2"] == "1" || $model->schoolKind == "H") {
            $query .= "     T1.ENT_ADDR2, ";
        }
        $query .= "     T1.GRD_DATE, ";
        $query .= "     T1.GRD_DIV, ";
        $query .= "     T1.GRD_REASON, ";
        $query .= "     T1.GRD_SCHOOL, ";
        $query .= "     T1.GRD_ADDR, ";
        if ($model->Properties["useAddrField2"] == "1" || $model->schoolKind == "H") {
            $query .= "     T1.GRD_ADDR2, ";
        }
        $query .= "     T1.GRD_NO, ";
        $query .= "     T1.GRD_TERM, ";
        $query .= "     T1.PRISCHOOLCD, ";
        $query .= "     T1.REMARK1, ";
        $query .= "     T1.REMARK2, ";
        $query .= "     T1.REMARK3 ";
        $query .= " FROM ";
        $query .= "     SCHREG_BASE_MST T1 ";
        $query .= " LEFT OUTER JOIN ";
        $query .= "     SCHREG_REGD_DAT T2 ON T1.SCHREGNO = T2.SCHREGNO ";
        $query .= " WHERE ";
        $query .= "     T1.SCHREGNO = '{$schregno}' AND ";
        $query .= "     T2.YEAR = '".CTRL_YEAR."' AND ";
        $query .= "     T2.SEMESTER = '".CTRL_SEMESTER."' ";

        return $query;
    }

    //履歴の最終終了日付取得
    function get_last_hist_expiredate($schregno) {
        $query  = " SELECT ";
        $query .= "     EXPIREDATE ";
        $query .= " FROM ";
        $query .= "     SCHREG_BASE_HIST_DAT ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO = '{$schregno}' ";
        $query .= " ORDER BY ";
        $query .= "     ISSUEDATE DESC ";
        $query .= " FETCH FIRST ROW ONLY ";

        return $query;
    }

    //最終履歴がなかったら入学日付を取得する
    function get_ent_date($schregno) {
        $query  = " SELECT ";
        $query .= "     ENT_DATE ";
        $query .= " FROM ";
        $query .= "     SCHREG_BASE_MST ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO = '{$schregno}' ";

        return $query;
    }


    //入学事由取得
    function getEnt_reason($schregno)
    {
        return "SELECT ent_reason FROM schreg_base_mst WHERE schregno = '".$schregno."'";
    }

    //卒業事由取得
    function getGrd_reason($schregno)
    {
        return "SELECT grd_reason FROM schreg_base_mst WHERE schregno = '".$schregno."'";
    }

    //課程学科コンボ
    function getCourse_Subject()
    {
        $query  = "";
        $query .= "  SELECT DISTINCT";
        $query .= "      coursecd || ',' || majorcd AS coursemajorcd,";
        $query .= "      coursename || majorabbv AS course_subject";
        $query .= "  FROM";
        $query .= "      v_course_major_mst";
        $query .= "  WHERE";
        $query .= "      year = '".CTRL_YEAR."'";
        $query .= "  ORDER BY";
        $query .= "      coursemajorcd";
        return $query;
    }

    //出身中学校コンボ
    function getFinschoolName($cd)
    {
        $query .= " SELECT ";
        $query .= "     FINSCHOOL_NAME ";
        $query .= " FROM ";
        $query .= "     FINSCHOOL_MST ";
        $query .= " WHERE ";
        $query .= "     FINSCHOOLCD = '{$cd}' ";

        return $query;
    }

    //出身塾コンボ
    function getPrischoolName()
    {
        $query  = " SELECT prischoolcd, prischool_name ";
        $query .= " FROM v_prischool_mst ";
        $query .= " WHERE year = '".CTRL_YEAR. "'";
        $query .= " ORDER BY prischoolcd";
        return $query;
    }

    //SCHREG_BASE_MSTにデータの有無チェック
    function getExist_base_mst($schregno)
    {
        $db = Query::dbCheckOut();

        $query  = " SELECT COUNT(*) FROM schreg_base_mst ";
        $query .= " WHERE schregno ='". $schregno. "' ";

        $row = $db->getOne($query);
        Query::dbCheckIn($db);
        return (($row == 0)? false : true);
    }

    //年組名取得
    function getHR_Name($model)
    {
        if ($model->field["GRCL"]) {
            $tmp = explode(",", $model->field["GRCL"]);
        } else {
            $tmp = explode(",", $model->GradeClass);
        }
        $query  = "SELECT hr_name FROM schreg_regd_hdat";
        $query .= " WHERE grade    = '".$tmp[0]."'";
        $query .= "   AND hr_class = '".$tmp[1]."'";
        $query .= "   AND year     = '".CTRL_YEAR."'";
        $query .= "   AND semester = '".CTRL_SEMESTER."'";
        return $query;
    }

    //出席番号重複チェック
    function IsExistAttendno($model)
    {
        $db = Query::dbCheckOut();
        $arr = explode(",",$model->field["GRCL"]);

        $query  = " SELECT COUNT(*) FROM schreg_regd_dat ";
        $query .= " WHERE grade    = '" .$arr[0]. "'";
        $query .= "   AND hr_class = '" .$arr[1]. "'";
        $query .= "   AND year     = '" .CTRL_YEAR. "'";
        $query .= "   AND semester = '" .CTRL_SEMESTER. "'";
        $query .= "   AND attendno = '" .sprintf("%03d",$model->field["ATTENDNO"]). "'";

        if ($model->cmd == "update" || $model->cmd == "pre_up" || $model->cmd == "next_up") {
            $query .= "   AND schregno <> '" . $model->schregno. "'";
        }

        $row = $db->getOne($query);
        Query::dbCheckIn($db);
        return (($row == 0)? false : true);
    }

    //SCHREG_REGD_DATにデータの有無チェック
    function getExist_regd_dat($model)
    {
        $db = Query::dbCheckOut();

        $query  = " SELECT COUNT(*) FROM schreg_regd_dat ";
        $query .= " WHERE schregno = '". $model->field["SCHREGNO"]. "' ";
        $query .= "   AND semester = '". CTRL_SEMESTER. "' ";
        $query .= "   AND     year = '". CTRL_YEAR. "' ";
        $row = $db->getOne($query);

        Query::dbCheckIn($db);
        return (($row == 0)? false : true);
    }

    //コースコード取得
    function getCourseCode()
    {
        $query  = " SELECT coursecode, coursecodename ";
        $query .= "   FROM v_coursecode_mst ";
        $query .= " WHERE year     = '". CTRL_YEAR ."'";
        $query .= " ORDER BY coursecode";
        return $query;
    }

    //V_NAME_MSTから情報を取得
    function getNameMst_data($cd)
    {
        $query  = " SELECT * FROM v_name_mst ";
        $query .= " WHERE namecd1 ='" .$cd. "' AND year='".CTRL_YEAR."'";
        $query .= " ORDER BY namecd1, namecd2";
        return $query;
    }

    //学校種別取得
    function getSubSchoolKind()
    {
        $query  = " SELECT ";
        $query .= "     NAME1 AS VALUE, ";
        $query .= "     VALUE(NAME1, '') || ':' || VALUE(ABBV1, '') AS LABEL ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND NAMECD1 = 'A023' ";
        $query .= " ORDER BY ";
        $query .= "     NAMECD2 ";

        return $query;
    }

    //内容が変更されているかのチェック(一括更新)履歴にとるか取らないかの判断
    function changeCheckIkkatu() {
        
    }

    //更新の際内容が変更されていれば、
    //履歴テーブルを更新するので、日付を取得する。
    function getEdate($schregno) {
        $query  = " SELECT ";
        $query .= "     EXPIREDATE ";
        $query .= " FROM ";
        $query .= "     SCHREG_BASE_HIST_DAT ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO = '{$schregno}' ";
        $query .= " ORDER BY ";
        $query .= "     ISSUEDATE DESC";
        $query .= " FETCH FIRST 1 ROWS ONLY ";

        return $query;
    }

}
?>
