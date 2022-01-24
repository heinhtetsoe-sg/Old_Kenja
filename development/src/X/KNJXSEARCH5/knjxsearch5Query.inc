<?php

require_once('for_php7.php');

class knjxsearch5Query extends Query {
   //学年と組
   function GetHr_Class($model)
   {
        $query = "";
        $query .= "SELECT ";
        $query .= "  VALUE(T1.GRADE,'') || '-' || VALUE(T1.HR_CLASS,'') AS VALUE, ";
        $query .= "  T1.HR_NAME AS LABEL, ";
        $query .= "  T1.HR_NAMEABBV, ";
        $query .= "  T2.STAFFCD, ";
        $query .= "  T2.STAFFNAME_SHOW ";
        $query .= "FROM ";
        $query .= "  SCHREG_REGD_HDAT T1,";
        $query .= "  STAFF_MST T2 ";
        $query .= "WHERE ";
        $query .= "  T1.TR_CD1 = T2.STAFFCD AND ";
        $query .= "  T1.YEAR = '" .CTRL_YEAR ."' AND ";
        $query .= "  T1.SEMESTER = '" .CTRL_SEMESTER ."' ";

        //中等教育校フラグ
        if ($model->ses_flg[$model->programid] == 1 && $model->chiyoda == "chiyoda"){
            $query .= "  AND T1.GRADE <= '03' ";
        }
        if ($model->ses_flg[$model->programid] == 2 && $model->chiyoda == "chiyoda"){
            $query .= "  AND T1.GRADE > '03' ";
        }

        //参照・更新可（制限付き）
        if ($model->usr_auth == DEF_REFER_RESTRICT || $model->usr_auth == DEF_UPDATE_RESTRICT) {
            $query .= "  AND (T1.TR_CD1 = '" .STAFFCD ."' ";
            $query .= "    OR T1.TR_CD2 = '" .STAFFCD ."' ";
            $query .= "    OR T1.TR_CD3 = '" .STAFFCD ."' ";
            $query .= "    OR T1.SUBTR_CD1 = '" .STAFFCD ."' ";
            $query .= "    OR T1.SUBTR_CD2 = '" .STAFFCD ."' ";
            $query .= "    OR T1.SUBTR_CD3 = '" .STAFFCD ."') ";
        }
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= " AND T1.GRADE IN (SELECT ";
                $query .= "                     G1.GRADE ";
                $query .= "                 FROM ";
                $query .= "                     SCHREG_REGD_GDAT G1 ";
                $query .= "                 WHERE ";
                $query .= "                     G1.YEAR         = T1.YEAR AND ";
                $query .= "                     G1.SCHOOL_KIND  IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
                $query .= "                 ) ";
            }
        } else if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND T1.GRADE IN (SELECT ";
            $query .= "                     G1.GRADE ";
            $query .= "                 FROM ";
            $query .= "                     SCHREG_REGD_GDAT G1 ";
            $query .= "                 WHERE ";
            $query .= "                     G1.YEAR         = T1.YEAR AND ";
            $query .= "                     G1.SCHOOL_KIND  = '".SCHOOLKIND."' ";
            $query .= "                 ) ";
        }
        $query .= " ORDER BY ";
        $query .= "  T1.GRADE,T1.HR_CLASS ";

        return $query;
    }

    //卒業年組
    function GetGrdHrClass($model)
    {
        $query  = " SELECT ";
        $query .= "     GREGDH.GRADE || GREGDH.HR_CLASS AS VALUE, ";
        $query .= "     MAX(GREGDH.HR_NAME) AS LABEL ";
        $query .= " FROM ";
        $query .= "     GRD_REGD_HDAT GREGDH ";
        $query .= "     INNER JOIN GRD_BASE_MST GBASE ON GREGDH.SEMESTER = GBASE.GRD_SEMESTER ";
        $query .= "           AND GREGDH.GRADE = GBASE.GRD_GRADE ";
        $query .= "           AND GREGDH.HR_CLASS = GBASE.GRD_HR_CLASS ";
        $query .= " WHERE ";
        $query .= "         GREGDH.YEAR = CAST(FISCALYEAR(GBASE.GRD_DATE) AS VARCHAR(4)) ";
        $query .= "     AND GREGDH.YEAR = '{$model->search["GRD_YEAR"]}' ";
        $query .= " GROUP BY ";
        $query .= "     GREGDH.GRADE, ";
        $query .= "     GREGDH.HR_CLASS ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //コース取得
    function GetCourseCode()
    {
        $query = " SELECT ";
        $query .= "   VALUE(COURSECODE,'') || '　' || VALUE(COURSECODENAME,'') AS LABEL, ";
        $query .= "   COURSECODE AS VALUE ";
        $query .= " FROM ";
        $query .= "   COURSECODE_MST ";

        return $query;
    }

    //クラブ取得
    function GetClub($model)
    {
        $query = " SELECT ";
        $query .= "   VALUE(T1.CLUBCD,'') || '　' || VALUE(T2.CLUBNAME,'') AS LABEL, ";
        $query .= "   T1.CLUBCD AS VALUE ";
        $query .= " FROM ";
        $query .= "   CLUB_YDAT T1 ";
        $query .= "   LEFT JOIN CLUB_MST T2 ON T2.CLUBCD = T1.CLUBCD ";
        $query .= " WHERE ";
        $query .= "   T1.YEAR = '".CTRL_YEAR."' ";
        if ($model->Properties["useClubMultiSchoolKind"] == "1") {
            $query .= " AND T1.SCHOOLCD     = T2.SCHOOLCD ";
            $query .= " AND T1.SCHOOL_KIND  = T2.SCHOOL_KIND ";
            $query .= " AND T1.SCHOOLCD     = '".sprintf("%012d", SCHOOLCD)."' ";
            $query .= " AND T1.SCHOOL_KIND  = '".SCHOOLKIND."' ";
        } else if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= " AND T1.SCHOOLCD     = T2.SCHOOLCD ";
                $query .= " AND T1.SCHOOL_KIND  = T2.SCHOOL_KIND ";
                $query .= " AND T1.SCHOOLCD     = '".sprintf("%012d", SCHOOLCD)."' ";
                $query .= " AND T1.SCHOOL_KIND  IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
            }
        } else if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND T1.SCHOOLCD     = T2.SCHOOLCD ";
            $query .= " AND T1.SCHOOL_KIND  = T2.SCHOOL_KIND ";
            $query .= " AND T1.SCHOOLCD     = '".sprintf("%012d", SCHOOLCD)."' ";
            $query .= " AND T1.SCHOOL_KIND  = '".SCHOOLKIND."' ";
        }
        $query .= " ORDER BY ";
        $query .= "   T1.CLUBCD ";

        return $query;
    }

    //講座取得
    function GetChair($model)
    {
        $query = " SELECT ";
        $query .= "   VALUE(CHAIRCD,'') || '　' || VALUE(CHAIRNAME,'') AS LABEL, ";
        $query .= "   CHAIRCD AS VALUE ";
        $query .= " FROM ";
        $query .= "   CHAIR_DAT ";
        $query .= " WHERE ";
        $query .= "   YEAR = '".CTRL_YEAR."' ";
        $query .= "   AND SEMESTER = '" .CTRL_SEMESTER. "' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= " AND SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
            }
        } else if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= " ORDER BY ";
        $query .= "   CHAIRCD ";

        return $query;
    }
    
    //卒業年度
    function GetGrdYear()
    {
        $query = " SELECT DISTINCT ";
        $query .= "   FISCALYEAR(GRD_DATE) AS LABEL, ";
        $query .= "   FISCALYEAR(GRD_DATE) AS VALUE ";
        $query .= " FROM ";
        $query .= "   GRD_BASE_MST ";
        $query .= " ORDER BY ";
        $query .= "   VALUE DESC ";

        return $query;
    }
    
    //左リスト
    function GetStudents($model)
    {
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
        $query .= "   FISCALYEAR(T2.GRD_DATE) AS GRD_YEAR ";
        if ($model->mode == "grd"){   //卒業
            $query .= "   ,T2.GRD_SEMESTER, ";
            $query .= "   T2.GRD_GRADE, ";
            $query .= "   T2.GRD_HR_CLASS, ";
            $query .= "   T2.GRD_ATTENDNO ";
        }
        $query .= " FROM ";
        if ($model->mode == "grd"){   //卒業
            $query .= "   GRD_REGD_DAT T1, ";
            $query .= "   GRD_BASE_MST T2, ";
            $query .= "   GRD_REGD_HDAT T3 ";
        }else if($model->mode == "ungrd"){ //在籍
            $query .= "   SCHREG_REGD_DAT T1, ";
            $query .= "   SCHREG_BASE_MST T2,";
            $query .= "   SCHREG_REGD_HDAT T3 ";
        }
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= "     INNER JOIN SCHREG_REGD_GDAT G1 ";
                $query .= "          ON G1.YEAR         = T3.YEAR ";
                $query .= "         AND G1.GRADE        = T3.GRADE ";
                $query .= "         AND G1.SCHOOL_KIND  IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
            }
        } else if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= "     INNER JOIN SCHREG_REGD_GDAT G1 ";
            $query .= "          ON G1.YEAR         = T3.YEAR ";
            $query .= "         AND G1.GRADE        = T3.GRADE ";
            $query .= "         AND G1.SCHOOL_KIND  = '".SCHOOLKIND."' ";
        }
        $query .= " WHERE ";
        if (isset($model->left_grade) && $model->mode == "ungrd" ){
            $query .= "   T1.GRADE || '-' || T1.HR_CLASS = '" .$model->left_grade."' AND ";
        }
        if ($model->mode == "grd" && $model->search["HR_CLASS"]){
            $query .= "   T2.GRD_GRADE || T2.GRD_HR_CLASS = '" .sprintf("%03s", $model->search["HR_CLASS"]). "' AND ";
        }
        if($model->mode == "ungrd"){  //在籍生
            $query .= "   T1.YEAR = '" .CTRL_YEAR. "' AND ";
            $query .= "   T1.SEMESTER = '" .CTRL_SEMESTER. "' AND ";
        }else if($model->mode == "grd" && $model->search["GRD_YEAR"]){  //卒業生
            $query .= "   FISCALYEAR(T2.GRD_DATE) = '" .$model->search["GRD_YEAR"]. "' AND ";
        }
        if ($model->search["COURSECODE"]) {
            $query .= "   T1.COURSECODE = '" .$model->search["COURSECODE"]. "' AND ";
        }
        if ($model->search["CLUBCD"]) {
            $query .= "   T1.SCHREGNO IN ( ";
            $query .= "                   SELECT DISTINCT I1.SCHREGNO ";
            $query .= "                   FROM SCHREG_CLUB_HIST_DAT I1 ";
            $query .= "                   WHERE I1.CLUBCD = '" .$model->search["CLUBCD"]. "') AND ";
        }
        if($model->mode == "ungrd"){  //在籍生
            if ($model->search["CHAIRCD"]) {
                $query .= "   T1.SCHREGNO IN ( ";
                $query .= "                   SELECT DISTINCT ";
                $query .= "                       I2.SCHREGNO ";
                $query .= "                   FROM ";
                $query .= "                       CHAIR_STD_DAT I2 ";
                $query .= "                   WHERE ";
                $query .= "                       I2.YEAR = '" .CTRL_YEAR. "' ";
                $query .= "                       AND I2.SEMESTER = '" .CTRL_SEMESTER. "' ";
                $query .= "                       AND I2.CHAIRCD = '" .$model->search["CHAIRCD"]. "') AND ";
            }
        }
        if ($model->search["NAME"]) {
            $query .= "   T2.NAME LIKE '%" .str_replace("'","''",$model->search["NAME"]). "%' AND ";
        }
        if ($model->search["NAME_SHOW"]) {
            $query .= "   T2.NAME_SHOW LIKE '%" .str_replace("'","''",$model->search["NAME_SHOW"]). "%' AND ";
        }
        if ($model->search["NAME_KANA"]) {
            $query .= "   T2.NAME_KANA LIKE '%" .str_replace("'","''",$model->search["NAME_KANA"]). "%' AND ";
        }
        if ($model->search["NAME_ENG"]) {
            $query .= "   T2.NAME_ENG LIKE '%" .str_replace("'","''",$model->search["NAME_ENG"]). "%' AND ";
        }
        if ($model->search["KEYWORD"] || $model->search["ACTIONDATE"]) {
            $query .= "   T1.SCHREGNO IN ( ";
            $query .= "                   SELECT DISTINCT ";
            $query .= "                       I3.SCHREGNO ";
            $query .= "                   FROM ";
            $query .= "                       ACTION_DOCUMENT_DAT I3 ";
            $query .= "                   WHERE ";
            if ($model->search["KEYWORD"] && $model->search["ACTIONDATE"]) {
                $query .= "                       I3.ACTIONDATE >= '" .str_replace("/","-",$model->search["ACTIONDATE"]). "' ";
                $query .= "                       AND I3.TEXT LIKE '%" .$model->search["KEYWORD"]. "%' ";
            } elseif ($model->search["ACTIONDATE"]) {
                $query .= "                       I3.ACTIONDATE >= '" .str_replace("/","-",$model->search["ACTIONDATE"]). "' ";
            } elseif ($model->search["KEYWORD"]) {
                $query .= "                       I3.TEXT LIKE '%" .$model->search["KEYWORD"]. "%' ";
            }
            $query .= "                   ) AND ";
        }
        $query .= "   T1.YEAR     = T3.YEAR AND ";
        $query .= "   T1.SEMESTER = T3.SEMESTER AND ";
        $query .= "   T1.GRADE    = T3.GRADE AND ";
        $query .= "   T1.HR_CLASS = T3.HR_CLASS AND ";
        $query .= "   T1.SCHREGNO = T2.SCHREGNO ";

        if($model->mode == "grd"){  //卒業生
            $query .= "   AND T1.YEAR     = FISCALYEAR(T2.GRD_DATE) ";
            $query .= "   AND T1.SEMESTER = T2.GRD_SEMESTER ";
            $query .= "   AND T1.GRADE    = T2.GRD_GRADE ";
            $query .= "   AND T1.HR_CLASS = T2.GRD_HR_CLASS ";
        }

        //中等教育校フラグ
        if ($model->ses_flg[$model->programid] == 1 && $model->chiyoda == "chiyoda"){
            $query .= "  AND T1.GRADE <= '03' ";
        }
        if ($model->ses_flg[$model->programid] == 2 && $model->chiyoda == "chiyoda"){
            $query .= "  AND T1.GRADE > '03' ";
        }

        //参照・更新可（制限付き）
        if($model->mode == "ungrd"){  //在籍生
            if ($model->usr_auth == DEF_REFER_RESTRICT || $model->usr_auth == DEF_UPDATE_RESTRICT) {
                $query .= "  AND (T3.TR_CD1 = '" .STAFFCD ."' ";
                $query .= "    OR T3.TR_CD2 = '" .STAFFCD ."' ";
                $query .= "    OR T3.TR_CD3 = '" .STAFFCD ."' ";
                $query .= "    OR T3.SUBTR_CD1 = '" .STAFFCD ."' ";
                $query .= "    OR T3.SUBTR_CD2 = '" .STAFFCD ."' ";
                $query .= "    OR T3.SUBTR_CD3 = '" .STAFFCD ."') ";
            }
        }
        $query .= " ORDER BY ";
        $query .= "   T1.GRADE,";
        $query .= "   T1.HR_CLASS,";
        $query .= "   T1.ATTENDNO ";

        return $query;
   }
    //学校区分名称
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
