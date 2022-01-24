<?php

require_once('for_php7.php');

class knjxexp_ghrQuery extends Query {
    //年度コンボフラグを取得（名称マスタ）
    function getComboFlg()
    {
        $db     = Query::dbCheckOut();
        $query  = "SELECT NAMECD2, ";
        $query .= "       NAME1 as PRGID, ";
        $query .= "       NAMESPARE1 as COMBO_FLG ";
        $query .= "FROM   NAME_MST ";
        $query .= "WHERE  NAMECD1 = 'A022' ";
        $ret_val = array();
        $result = $db->query($query);
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            $ret_val[$row["PRGID"]] = $row["COMBO_FLG"];
        }
        $result->free();
        Query::dbCheckIn($db);
        return $ret_val;
    }
    //年度コンボ（学期マスタ）
    function getYearSemester($exp_year)
    {
        $pre_year = CTRL_YEAR - 1;
        $query  = " SELECT ";
        $query .= "   YEAR, ";
        $query .= "   SEMESTER, ";
        $query .= "   SEMESTERNAME ";
        $query .= " FROM ";
        $query .= "   SEMESTER_MST ";
        $query .= " WHERE ";
        $query .= "       '$pre_year' <= YEAR AND SEMESTER <> '9' ";
        if ($exp_year != "off") 
            $query .= "   AND YEAR || '-' || SEMESTER = '" .$exp_year. "' ";
        $query .= " ORDER BY ";
        $query .= "   YEAR, SEMESTER ";

        return $query;
    }
    //複式クラスコンボ
    function getGhrCd($model) {
        $query  = " SELECT DISTINCT ";
        $query .= "     T1.GHR_CD, ";
        $query .= "     T1.GHR_NAME, ";
        $query .= "     T1.GHR_NAMEABBV, ";
        $query .= "     T2.STAFFCD, ";
        $query .= "     T2.STAFFNAME_SHOW ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_GHR_HDAT T1 ";
        $query .= "     INNER JOIN STAFF_MST T2 ON T2.STAFFCD = T1.TR_CD1 ";
        $query .= "     LEFT JOIN SCHREG_REGD_GHR_DAT GHR_DAT ON GHR_DAT.YEAR = T1.YEAR ";
        $query .= "           AND GHR_DAT.SEMESTER = T1.SEMESTER ";
        $query .= "           AND GHR_DAT.GHR_CD = T1.GHR_CD ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR || '-' || T1.SEMESTER = '{$model->exp_year}' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= "  AND GHR_DAT.SCHREGNO IN (SELECT ";
                $query .= "                             I1.SCHREGNO ";
                $query .= "                         FROM ";
                $query .= "                             SCHREG_REGD_DAT I1 ";
                $query .= "                             INNER JOIN SCHREG_REGD_GDAT I2 ON I2.YEAR = I1.YEAR ";
                $query .= "                                   AND I2.GRADE = I1.GRADE ";
                $query .= "                                   AND I2.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
                $query .= "                         WHERE ";
                $query .= "                             I1.YEAR || '-' || I1.SEMESTER = '{$model->exp_year}') ";
            }
        } else if ($model->schoolKind) {
            $query .= "     AND GHR_DAT.SCHREGNO IN (SELECT ";
            $query .= "                             I1.SCHREGNO ";
            $query .= "                         FROM ";
            $query .= "                             SCHREG_REGD_DAT I1 ";
            $query .= "                             INNER JOIN SCHREG_REGD_GDAT I2 ON I2.YEAR = I1.YEAR ";
            $query .= "                                   AND I2.GRADE = I1.GRADE ";
            $query .= "                                   AND I2.SCHOOL_KIND = '".$model->schoolKind."' ";
            $query .= "                         WHERE ";
            $query .= "                             I1.YEAR || '-' || I1.SEMESTER = '{$model->exp_year}' ";
            $query .= "                         ) ";
        }
        if ($model->usr_auth == DEF_UPDATE_RESTRICT || $model->usr_auth == DEF_REFER_RESTRICT) {
            $query .= "     AND (T1.TR_CD1    = '". STAFFCD ."' OR ";
            $query .= "          T1.TR_CD2    = '". STAFFCD ."' OR ";
            $query .= "          T1.TR_CD3    = '". STAFFCD ."' OR ";
            $query .= "          T1.SUBTR_CD1 = '". STAFFCD ."' OR ";
            $query .= "          T1.SUBTR_CD2 = '". STAFFCD ."' OR ";
            $query .= "          T1.SUBTR_CD3 = '". STAFFCD ."') ";
        }
        //障害者フラグ
        if (strlen($model->handicap[$model->programid])) {
            $query .= "     AND T1.GHR_CD IN (SELECT ";
            $query .= "                             S1.GHR_CD ";
            $query .= "                         FROM ";
            $query .= "                             SCHREG_REGD_GHR_DAT S1 ";
            $query .= "                             LEFT JOIN SCHREG_BASE_MST L1 ON L1.SCHREGNO = S1.SCHREGNO ";
            $query .= "                             LEFT JOIN SCHREG_BASE_YEAR_DETAIL_MST L5 ON L5.SCHREGNO = S1.SCHREGNO AND L5.YEAR = S1.YEAR AND L5.BASE_SEQ = '004', ";
            $query .= "                             V_NAME_MST S2 ";
            $query .= "                         WHERE ";
            $query .= "                             S1.YEAR     = T1.YEAR AND ";
            $query .= "                             S2.YEAR     = T1.YEAR AND ";
            $query .= "                             S1.SEMESTER = T1.SEMESTER AND ";
            $query .= "                             S2.NAMECD1  = 'A025' AND ";
            $query .= "                             S2.NAMECD2  = (CASE WHEN L5.BASE_REMARK1 IS NULL THEN L1.HANDICAP ELSE L5.BASE_REMARK1 END) AND ";
            if (strtoupper(substr($model->handicap[$model->programid],0,1)) == "N") {
                $cutstr = substr($model->handicap[$model->programid],1);
                $query .= "                             VALUE(S2.NAMESPARE3, '') NOT IN ('".implode("','", explode(':', $cutstr))."') ";
            } else {
                $query .= "                             S2.NAMESPARE3 IN ('".implode("','", explode(':', $model->handicap[$model->programid]))."') ";
            }
            $query .= "                         ) ";
        }
        $query .= " ORDER BY ";
        $query .= "     T1.GHR_CD ";

        return $query;
    }

    //年組取得
    function getStaffHr($model) {
        $query  = " SELECT ";
        $query .= "     T1.SCHOOL_KIND || '-' || T1.HR_CLASS  || ':' || T1.HR_CLASS_NAME1 AS LABEL, ";
        $query .= "     T1.SCHOOL_KIND || '-' || T1.HR_CLASS AS VALUE ";
        $query .= " FROM ";
        $query .= "     V_STAFF_HR_DAT T1 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR     = '".CTRL_YEAR."' ";
        $query .= "     AND T1.SEMESTER = '".CTRL_SEMESTER."' ";
        if ($model->schoolKind) {
            $query .= "     AND T1.SCHOOL_KIND = '".$model->schoolKind."' ";
        }
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= "     AND T1.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
            }
        } else if ($model->Properties["useSchool_KindField"] == "1") {
            $query .= "     AND T1.SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        if ($model->usr_auth == DEF_REFER_RESTRICT || $model->usr_auth == DEF_UPDATE_RESTRICT) {
            $query .= " AND T1.STAFFCD = '".STAFFCD."' ";
        }
        $query .= "     AND T1.HR_CLASS_NAME1 IN ( SELECT ";
        $query .= "                                    MAX(M1.HR_CLASS_NAME1) AS MAX_NAME  ";
        $query .= "                                FROM ";
        $query .= "                                    V_STAFF_HR_DAT M1 ";
        $query .= "                               WHERE ";
        $query .= "                                    T1.YEAR     = M1.YEAR AND ";
        $query .= "                                    T1.SEMESTER = M1.SEMESTER AND ";
        $query .= "                                    T1.HR_CLASS = M1.HR_CLASS AND ";
        $query .= "                                    T1.SCHOOL_KIND = M1.SCHOOL_KIND AND ";
        $query .= "                                    T1.STAFFCD  = M1.STAFFCD ";
        $query .= "                             ) ";
        $query .= " GROUP BY ";
        $query .= "     T1.HR_CLASS, ";
        $query .= "     T1.HR_CLASS_NAME1, ";
        $query .= "     T1.SCHOOL_KIND ";
        $query .= " ORDER BY ";
        $query .= "     T1.SCHOOL_KIND, ";
        $query .= "     VALUE ";
        return $query;
    }

    //FI複式年組コンボ
    function getFiGradeHrclass($model) {
        $query  = " SELECT ";
        $query .= "     T1.GRADE || '-' || T1.HR_CLASS AS FI_GRADE_HR_CLASS, ";
        $query .= "     T1.HR_NAME AS FI_HR_NAME, ";
        $query .= "     T1.HR_NAMEABBV AS FI_HR_NAMEABBV, ";
        $query .= "     T2.STAFFCD, ";
        $query .= "     T2.STAFFNAME_SHOW ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_FI_HDAT T1 ";
        $query .= "     INNER JOIN STAFF_MST T2 ON T2.STAFFCD = T1.TR_CD1 ";
        if ($model->schoolKind) {
            $query .= "     INNER JOIN SCHREG_REGD_GDAT GDAT ON GDAT.YEAR = T1.YEAR ";
            $query .= "           AND GDAT.GRADE = T1.GRADE ";
            $query .= "           AND GDAT.SCHOOL_KIND = '".$model->schoolKind."' ";
        }
        $query .= " WHERE ";
        $query .= "         T1.YEAR     = '".CTRL_YEAR."' ";
        $query .= "     AND T1.SEMESTER = '".CTRL_SEMESTER."' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= "  AND T1.GRADE IN (SELECT ";
                $query .= "                     REGD_G.GRADE ";
                $query .= "                 FROM ";
                $query .= "                     SCHREG_REGD_GDAT REGD_G ";
                $query .= "                 WHERE ";
                $query .= "                     REGD_G.YEAR = '".CTRL_YEAR."' ";
                $query .= "                     AND REGD_G.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."')) ";
            }
        } else if ($model->Properties["useSchool_KindField"] == "1") {
            $query .= "     AND T1.GRADE IN (SELECT ";
            $query .= "                          REGD_G.GRADE ";
            $query .= "                      FROM ";
            $query .= "                          SCHREG_REGD_GDAT REGD_G ";
            $query .= "                      WHERE ";
            $query .= "                          REGD_G.YEAR = '".CTRL_YEAR."' ";
            $query .= "                          AND REGD_G.SCHOOL_KIND = '" .SCHOOLKIND ."') ";
        }
        if (AUTHORITY == DEF_UPDATE_RESTRICT || AUTHORITY == DEF_REFER_RESTRICT) {
            $query .= "     AND (T1.TR_CD1    = '". STAFFCD ."' OR ";
            $query .= "          T1.TR_CD2    = '". STAFFCD ."' OR ";
            $query .= "          T1.TR_CD3    = '". STAFFCD ."' OR ";
            $query .= "          T1.SUBTR_CD1 = '". STAFFCD ."' OR ";
            $query .= "          T1.SUBTR_CD2 = '". STAFFCD ."' OR ";
            $query .= "          T1.SUBTR_CD3 = '". STAFFCD ."') ";
        }
        //障害者フラグ
        if (strlen($model->handicap[$model->programid])) {
            $query .= "     AND (T1.GRADE, T1.HR_CLASS) IN (SELECT ";
            $query .= "                                         S1.GRADE, ";
            $query .= "                                         S1.HR_CLASS ";
            $query .= "                                     FROM ";
            $query .= "                                         SCHREG_REGD_FI_DAT S1 ";
            $query .= "                                         LEFT JOIN SCHREG_BASE_MST L1 ON L1.SCHREGNO = S1.SCHREGNO ";
            $query .= "                                         LEFT JOIN SCHREG_BASE_YEAR_DETAIL_MST L5 ON L5.SCHREGNO = S1.SCHREGNO AND L5.YEAR = S1.YEAR AND L5.BASE_SEQ = '004', ";
            $query .= "                                         V_NAME_MST S2 ";
            $query .= "                                     WHERE ";
            $query .= "                                         S1.YEAR     = T1.YEAR AND ";
            $query .= "                                         S2.YEAR     = T1.YEAR AND ";
            $query .= "                                         S1.SEMESTER = T1.SEMESTER AND ";
            $query .= "                                         S2.NAMECD1  = 'A025' AND ";
            $query .= "                                         S2.NAMECD2  = (CASE WHEN L5.BASE_REMARK1 IS NULL THEN L1.HANDICAP ELSE L5.BASE_REMARK1 END) AND ";
            if (strtoupper(substr($model->handicap[$model->programid],0,1)) == "N") {
                $cutstr = substr($model->handicap[$model->programid],1);
                $query .= "                                         VALUE(S2.NAMESPARE3, '') NOT IN ('".implode("','", explode(':', $cutstr))."') ";
            } else {
                $query .= "                                         S2.NAMESPARE3 IN ('".implode("','", explode(':', $model->handicap[$model->programid]))."') ";
            }
            $query .= "                                     ) ";
        }
        $query .= " ORDER BY ";
        $query .= "     FI_GRADE_HR_CLASS ";

        return $query;
    }

    //学年と組
    function GetHr_Class($model)
    {
        $query = "";
        $query .= "SELECT ";
        $query .= "  T1.GRADE, ";
        $query .= "  T1.HR_CLASS, ";
        $query .= "  T1.HR_NAME, ";
        $query .= "  T1.HR_NAMEABBV, ";
        $query .= "  T2.STAFFCD, ";
        $query .= "  T2.STAFFNAME_SHOW ";
        $query .= "FROM ";
        $query .= "  SCHREG_REGD_HDAT T1";
        if ($model->schoolKind) {
            $query .= "  INNER JOIN SCHREG_REGD_GDAT GDAT ON GDAT.YEAR = T1.YEAR ";
            $query .= "        AND GDAT.GRADE = T1.GRADE ";
            $query .= "        AND GDAT.SCHOOL_KIND = '".$model->schoolKind."' ";
        }
        $query .= "  ,STAFF_MST T2 ";
        $query .= "WHERE ";
        if ($model->usr_auth == DEF_UPDATE_RESTRICT || $model->usr_auth == DEF_REFER_RESTRICT) {
            $query .= " (T1.TR_CD1 = '". STAFFCD ."' OR";
            $query .= "  T1.TR_CD2 = '". STAFFCD ."' OR";
            $query .= "  T1.TR_CD3 = '". STAFFCD ."' OR";
            $query .= "  T1.SUBTR_CD1 = '". STAFFCD ."' OR";
            $query .= "  T1.SUBTR_CD2 = '". STAFFCD ."' OR";
            $query .= "  T1.SUBTR_CD3 = '". STAFFCD ."') AND";
        }
        $query .= "  T1.TR_CD1 = T2.STAFFCD AND ";
        $query .= "  T1.YEAR || '-' || T1.SEMESTER = '" .$model->exp_year. "' ";
        if ($model->programid == "KNJD105B") {
            $query .= "  AND GRADE NOT IN (SELECT T1.GRADE ";
            $query .= "                    FROM SCHREG_REGD_HDAT T1, ";
            $query .= "                         NAME_MST T2 ";
            $query .= "                    WHERE T1.YEAR || '-' || T1.SEMESTER = '" .$model->exp_year. "' AND ";
            $query .= "                          T2.NAMECD1 = 'Z010' AND ";
            $query .= "                          T2.NAMECD2 = '00' AND ";
            $query .= "                          T2.NAME1 = 'CHIBEN' AND ";
            $query .= "                          T1.GRADE > '10') ";
        }
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= "  AND T1.GRADE IN (SELECT ";
                $query .= "                 REGD_G.GRADE ";
                $query .= "             FROM ";
                $query .= "                 SCHREG_REGD_GDAT REGD_G ";
                $query .= "             WHERE ";
                $query .= "                 REGD_G.YEAR = '".CTRL_YEAR."' ";
                $query .= "                 AND REGD_G.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."')) ";
            }
        } else if ($model->Properties["useSchool_KindField"] == "1") {
            $query .= "     AND T1.GRADE IN (SELECT ";
            $query .= "                          REGD_G.GRADE ";
            $query .= "                      FROM ";
            $query .= "                          SCHREG_REGD_GDAT REGD_G ";
            $query .= "                      WHERE ";
            $query .= "                          REGD_G.YEAR = '".CTRL_YEAR."' ";
            $query .= "                          AND REGD_G.SCHOOL_KIND = '" .SCHOOLKIND ."') ";
        }

        //中等教育校フラグ NO001
        if ($model->ses_flg[$model->programid] == 1 && $model->chiyoda == "chiyoda"){
            $query .= "  AND T1.GRADE <= '03' ";
        }
        if ($model->ses_flg[$model->programid] == 2 && $model->chiyoda == "chiyoda"){
            $query .= "  AND T1.GRADE > '03' ";
        }
        //障害者フラグ
        if (strlen($model->handicap[$model->programid])) {
            $query .= "     AND (T1.GRADE, T1.HR_CLASS) IN (SELECT ";
            $query .= "                                         S1.GRADE, ";
            $query .= "                                         S1.HR_CLASS ";
            $query .= "                                     FROM ";
            $query .= "                                         SCHREG_REGD_DAT S1 ";
            $query .= "                                         LEFT JOIN SCHREG_BASE_MST L1 ON L1.SCHREGNO = S1.SCHREGNO ";
            $query .= "                                         LEFT JOIN SCHREG_BASE_YEAR_DETAIL_MST L5 ON L5.SCHREGNO = S1.SCHREGNO AND L5.YEAR = S1.YEAR AND L5.BASE_SEQ = '004', ";
            $query .= "                                         V_NAME_MST S2 ";
            $query .= "                                     WHERE ";
            $query .= "                                         S1.YEAR     = T1.YEAR AND ";
            $query .= "                                         S2.YEAR     = T1.YEAR AND ";
            $query .= "                                         S1.SEMESTER = T1.SEMESTER AND ";
            $query .= "                                         S2.NAMECD1  = 'A025' AND ";
            $query .= "                                         S2.NAMECD2  = (CASE WHEN L5.BASE_REMARK1 IS NULL THEN L1.HANDICAP ELSE L5.BASE_REMARK1 END) AND ";
            if (strtoupper(substr($model->handicap[$model->programid],0,1)) == "N") {
                $cutstr = substr($model->handicap[$model->programid],1);
                $query .= "                                         VALUE(S2.NAMESPARE3, '') NOT IN ('".implode("','", explode(':', $cutstr))."') ";
            } else {
                $query .= "                                         S2.NAMESPARE3 IN ('".implode("','", explode(':', $model->handicap[$model->programid]))."') ";
            }
            $query .= "                                     ) ";
        }

        $query .= " ORDER BY ";
        $query .= "  T1.GRADE,T1.HR_CLASS ";

        return $query;
    }
    //コース取得
    function GetCourseCode()
    {
        $query = " SELECT ";
        $query .= "   COURSECODE, ";
        $query .= "   COURSECODENAME ";
        $query .= " FROM ";
        $query .= "   COURSECODE_MST ";

        return $query;
    }
    //卒業年度
    function GetGrdYear()
    {
        $query = " SELECT DISTINCT ";
        $query .= "   FISCALYEAR(GRD_DATE) AS GRD_YEAR ";
        $query .= " FROM ";
        $query .= "   GRD_BASE_MST ";

        return $query;
    }
    function GetStudents($model)
    {
        if (($model->cmd == "list" || $model->cmd == "chg_hukusiki_radio") && $model->grade){
            $grade = $model->grade;
        }else if (($model->cmd == "list" || $model->cmd == "chg_hukusiki_radio") && !$model->grade){
            $grade = "";
        }else if ($model->cmd == "search" && $model->search["GRADE"]){
            $grade = $model->search["GRADE"];
        }

        $query = " SELECT DISTINCT ";
        $query .= "   T1.SCHREGNO, ";
        $query .= "   T1.GRADE, ";
        $query .= "   T1.HR_CLASS, ";
        $query .= "   T3.HR_NAME, ";
        $query .= "   T3.HR_NAMEABBV, ";
        $query .= "   T1.ATTENDNO, ";
        $query .= "   T2.SEX, ";
        $query .= "   T2.NAME_SHOW, ";
        $query .= "   T2.NAME_KANA, ";
        if ($model->mode == "grd"){   //卒業
            $query .= "   T2.GRD_SEMESTER, ";
            $query .= "   T2.GRD_GRADE, ";
            $query .= "   T2.GRD_HR_CLASS, ";
            $query .= "   T2.GRD_ATTENDNO, ";
        }
        $query .= "   FISCALYEAR(T2.ENT_DATE) AS ENT_YEAR, ";
        $query .= "   FISCALYEAR(T2.GRD_DATE) AS GRD_YEAR, ";
        //特別支援学校対応
        if ($model->Properties["useSpecial_Support_Hrclass"] == '1') {
            //複式クラス
            $query .= "     L1.GHR_CD, ";
            $query .= "     L3.GHR_NAME, ";
            $query .= "     L3.GHR_NAMEABBV, ";
            $query .= "     L1.GHR_ATTENDNO, ";
        //FI複式クラスを使うためのプロパティ
        } else if ($model->Properties["useFi_Hrclass"] == '1') {
            $query .= "     L1.GRADE || '-' || L1.HR_CLASS AS FI_GRADE_HR_CLASS, ";
            $query .= "     L3.HR_NAME AS FI_HR_NAME, ";
            $query .= "     L3.HR_NAMEABBV AS FI_HR_NAMEABBV, ";
            $query .= "     L1.ATTENDNO  AS FI_ATTENDNO, ";
        }
        $query .= "   SUBSTR(T1.SCHREGNO, 1, 4) AS DUMMY1, ";
        $query .= "   SUBSTR(T1.SCHREGNO, 5, 4) AS DUMMY2 ";

        $query .= " FROM ";
        if ($model->search["DATE"]){
            $query .= "   NURSEOFF_VISITREC_DAT T4, ";
        }
        if ($model->mode == "grd"){   //卒業
            $query .= "   GRD_BASE_MST T2, ";
            $query .= "   GRD_REGD_HDAT T3, ";
            $query .= "   GRD_REGD_DAT T1 ";
        }else if($model->mode == "ungrd"){ //在籍
            $query .= "   SCHREG_BASE_MST T2,";
            $query .= "   SCHREG_REGD_HDAT T3, ";
            $query .= "   SCHREG_REGD_DAT T1 ";
        }
        //特別支援学校対応
        if ($model->Properties["useSpecial_Support_Hrclass"] == '1') {
            //複式クラス
            $query .= "     LEFT JOIN SCHREG_REGD_GHR_DAT L1 ON L1.SCHREGNO = T1.SCHREGNO AND L1.YEAR = T1.YEAR AND L1.SEMESTER = T1.SEMESTER ";
            $query .= "     LEFT JOIN SCHREG_REGD_GHR_HDAT L3 ON L3.YEAR = L1.YEAR AND L3.SEMESTER = L1.SEMESTER AND L3.GHR_CD = L1.GHR_CD ";
        //FI複式クラスを使うためのプロパティ
        } else if ($model->Properties["useFi_Hrclass"] == '1') {
            //複式クラス
            $query .= "     LEFT JOIN SCHREG_REGD_FI_DAT L1 ON L1.SCHREGNO = T1.SCHREGNO AND L1.YEAR = T1.YEAR AND L1.SEMESTER = T1.SEMESTER ";
            $query .= "     LEFT JOIN SCHREG_REGD_FI_HDAT L3 ON L3.YEAR = L1.YEAR AND L3.SEMESTER = L1.SEMESTER AND L3.GRADE = L1.GRADE AND L3.HR_CLASS = L1.HR_CLASS ";
        }
        //障害者フラグ
        if (strlen($model->handicap[$model->programid])) {
            $query .= "     LEFT JOIN SCHREG_BASE_YEAR_DETAIL_MST L5 ON L5.SCHREGNO = T1.SCHREGNO AND L5.YEAR = T1.YEAR AND L5.BASE_SEQ = '004' ";
            if (strtoupper(substr($model->handicap[$model->programid],0,1)) == "N") {
                $cutstr = substr($model->handicap[$model->programid],1);
                $query .= "     LEFT JOIN V_NAME_MST N1 ON N1.YEAR = T1.YEAR AND N1.NAMECD1 = 'A025' AND VALUE(N1.NAMESPARE3, '') NOT IN ('".implode("','", explode(':', $cutstr))."') ";
            } else {
                $query .= "     LEFT JOIN V_NAME_MST N1 ON N1.YEAR = T1.YEAR AND N1.NAMECD1 = 'A025' AND N1.NAMESPARE3 IN ('".implode("','", explode(':', $model->handicap[$model->programid]))."') ";
            }
        }

        $query .= " WHERE ";
        //特別支援学校対応
        if ($model->Properties["useSpecial_Support_Hrclass"] == '1' && $model->hukusiki_radio == "2") {
            //複式クラス
            $query .= "   L1.YEAR || '-' || L1.SEMESTER = '" .$model->exp_year. "' AND ";
            $query .= "   L1.GHR_CD = '" .$model->ghr_cd. "' AND ";
        //FI複式クラスを使うためのプロパティ
        } else if ($model->Properties["useFi_Hrclass"] == '1' && $model->hukusiki_radio == "2") {
            //複式クラス
            $query .= "   L1.YEAR || '-' || L1.SEMESTER = '" .$model->exp_year. "' AND ";
            $query .= "   L1.GRADE || '-' || L1.HR_CLASS = '" .$model->fi_grade_hr_class. "' AND ";
        } else if (isset($grade) && $model->mode == "ungrd" ) {
            $query .= "   T1.GRADE || '-' || T1.HR_CLASS = '" .$grade."' AND ";
        }
        if ($model->mode == "grd" && $model->search["HR_CLASS"]){
            $query .= "   T2.GRD_HR_CLASS = '" .sprintf("%03s", $model->search["HR_CLASS"]). "' AND ";
        }
        if($model->mode == "ungrd"){  //在籍生
            $query .= "   T1.YEAR || '-' || T1.SEMESTER = '" .$model->exp_year. "' AND ";
        }else if($model->mode == "grd" && $model->search["GRD_YEAR"]){  //卒業生
            $query .= "   FISCALYEAR(T2.GRD_DATE) = '" .$model->search["GRD_YEAR"]. "' AND ";
        }
        if ($model->search["COURSECODE"]){
            $query .= "   T1.COURSECODE = '" .$model->search["COURSECODE"]. "' AND ";
        }
        if ($model->search["SRCH_SCHREGNO"]){
            if ($model->Properties["schregno_bubunkensaku"] == "1") {
                $query .= "   T1.SCHREGNO LIKE '%" .$model->search["SRCH_SCHREGNO"]. "%' AND ";
            } else {
                $query .= "   T1.SCHREGNO = '" .$model->search["SRCH_SCHREGNO"]. "' AND ";
            }
        }
        if ($model->search["NAME"]){
            $query .= "   T2.NAME LIKE '%" .str_replace("'","''",$model->search["NAME"]). "%' AND ";
        }
        if ($model->search["NAME_SHOW"]){
            $query .= "   T2.NAME_SHOW LIKE '%" .str_replace("'","''",$model->search["NAME_SHOW"]). "%' AND ";
        }
        if ($model->search["NAME_KANA"]){
            $query .= "   T2.NAME_KANA LIKE '%" .str_replace("'","''",$model->search["NAME_KANA"]). "%' AND ";
        }
        if ($model->search["NAME_ENG"]){
            $query .= "   T2.NAME_ENG LIKE '%" .str_replace("'","''",$model->search["NAME_ENG"]). "%' AND ";
        }
        if ($model->search["SEX"]){
            $query .= "   T2.SEX = '" .$model->search["SEX"]. "' AND ";
        }
        if ($model->search["DATE"]){
            $query .= "   T4.VISIT_DATE = '" .$model->search["DATE"]. "' AND ";
            $query .= "   T1.SCHREGNO = T4.SCHREGNO AND ";
        }
        $query .= "   T1.YEAR     = T3.YEAR AND ";
        $query .= "   T1.SEMESTER = T3.SEMESTER AND ";
        $query .= "   T1.GRADE    = T3.GRADE AND ";
        $query .= "   T1.HR_CLASS = T3.HR_CLASS AND ";

        if($model->mode == "grd"){  //卒業生
            $query .= "   T1.YEAR     = FISCALYEAR(T2.GRD_DATE) AND ";
            $query .= "   T1.SEMESTER = T2.GRD_SEMESTER AND ";
            $query .= "   T1.GRADE    = T2.GRD_GRADE AND ";
            $query .= "   T1.HR_CLASS = T2.GRD_HR_CLASS AND ";
        }

        if ($model->mode == "ungrd" && $model->programid == "KNJD105B") {
            $query .= "   T1.GRADE NOT IN (SELECT S1.GRADE ";
            $query .= "                    FROM SCHREG_REGD_HDAT S1, ";
            $query .= "                         NAME_MST S2 ";
            $query .= "                    WHERE S1.YEAR || '-' || S1.SEMESTER = '" .$model->exp_year. "' AND ";
            $query .= "                          S2.NAMECD1 = 'Z010' AND ";
            $query .= "                          S2.NAMECD2 = '00' AND ";
            $query .= "                          S2.NAME1 = 'CHIBEN' AND ";
            $query .= "                          S1.GRADE > '06') AND ";
        }

        $query .= "   T1.SCHREGNO = T2.SCHREGNO ";

        //中等教育校フラグ NO001
        if ($model->ses_flg[$model->programid] == 1 && $model->chiyoda == "chiyoda"){
            $query .= "  AND T1.GRADE <= '03' ";
        }
        if ($model->ses_flg[$model->programid] == 2 && $model->chiyoda == "chiyoda"){
            $query .= "  AND T1.GRADE > '03' ";
        }
        //障害者フラグ
        if (strlen($model->handicap[$model->programid])) {
            $query .= " AND N1.NAMECD2 = (CASE WHEN L5.BASE_REMARK1 IS NULL THEN T2.HANDICAP ELSE L5.BASE_REMARK1 END) ";
        }

        $query .= " ORDER BY ";
        //特別支援学校対応
        if ($model->Properties["useSpecial_Support_Hrclass"] == '1' && $model->hukusiki_radio == "2") {
            //複式クラス
            $query .= "   L1.GHR_CD, ";
            if ($model->Properties["KNJXEXP_GHR_SEARCH"] == "SCHREGNO") {
                $query .= "   SUBSTR(T1.SCHREGNO, 1, 4) DESC, ";
                $query .= "   SUBSTR(T1.SCHREGNO, 5, 4) ";
            } else {
                $query .= "   L1.GHR_ATTENDNO ";
            }
        //FI複式クラスを使うためのプロパティ
        } else if ($model->Properties["useFi_Hrclass"] == '1' && $model->hukusiki_radio == "2") {
            //複式クラス
            $query .= "  FI_GRADE_HR_CLASS, ";
            if ($model->Properties["KNJXEXP_GHR_SEARCH"] == "SCHREGNO") {
                $query .= "   SUBSTR(T1.SCHREGNO, 1, 4) DESC, ";
                $query .= "   SUBSTR(T1.SCHREGNO, 5, 4) ";
            } else {
                $query .= "   FI_ATTENDNO ";
            }
        } else {
            $query .= "   T1.GRADE,";
            $query .= "   T1.HR_CLASS,";
            if ($model->Properties["KNJXEXP_GHR_SEARCH"] == "SCHREGNO") {
                $query .= "   SUBSTR(T1.SCHREGNO, 1, 4) DESC, ";
                $query .= "   SUBSTR(T1.SCHREGNO, 5, 4) ";
            } else {
                $query .= "   T1.ATTENDNO ";
            }
        }

        return $query;
   }

    //生徒一覧取得
    function getSchInfo($model) {

        $query  = " SELECT DISTINCT ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     T1.GRADE, ";
        $query .= "     T1.HR_CLASS, ";
        $query .= "     L2.HR_NAME, ";
        $query .= "     L2.HR_NAMEABBV, ";
        $query .= "     T1.ATTENDNO, ";
        $query .= "     L1.SEX, ";
        $query .= "     L1.NAME_SHOW, ";
        $query .= "     L1.NAME_KANA, ";
        $query .= "     FISCALYEAR(L1.ENT_DATE) AS ENT_YEAR, ";
        $query .= "     FISCALYEAR(L1.GRD_DATE) AS GRD_YEAR, ";
        $query .= "     SUBSTR(T1.SCHREGNO, 1, 4) AS DUMMY1, ";
        $query .= "     SUBSTR(T1.SCHREGNO, 5, 4) AS DUMMY2 ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1 ";
        $query .= "     LEFT JOIN SCHREG_BASE_MST L1 ON L1.SCHREGNO = T1.SCHREGNO ";
        $query .= "     LEFT JOIN SCHREG_REGD_HDAT L2 ON L2.YEAR = T1.YEAR ";
        $query .= "          AND L2.SEMESTER = T1.SEMESTER ";
        $query .= "          AND L2.GRADE = T1.GRADE ";
        $query .= "          AND L2.HR_CLASS = T1.HR_CLASS ";
        $query .= "     ,V_STAFF_HR_DAT T3 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR     = '".CTRL_YEAR."' AND ";
        $query .= "     T1.SEMESTER = '".CTRL_SEMESTER."' AND ";
        $query .= "     T1.YEAR     = T3.YEAR AND ";
        $query .= "     T1.SEMESTER = T3.SEMESTER AND ";
        $query .= "     T1.GRADE = T3.GRADE AND ";
        $query .= "     T1.HR_CLASS = T3.HR_CLASS AND ";
        $query .= "     T3.SCHOOL_KIND || '-' || T1.HR_CLASS = '".$model->ghr_cd."' ";
        $query .= " ORDER BY ";
        $query .= "     T1.GRADE, ";
        $query .= "     T1.HR_CLASS, ";
        $query .= "     T1.ATTENDNO, ";
        $query .= "     T1.SCHREGNO ";

        return $query;
    }


    //学校区分名称 NO001
    function GetSchooldiv()
    {
        $db     = Query::dbCheckOut();
        $query = " SELECT NAME1 FROM NAME_MST WHERE NAMECD1='Z010' AND NAMECD2='00' ";
        $ret_val = $db->getOne($query);
        Query::dbCheckIn($db);

        return $ret_val;
    }
}
?>
