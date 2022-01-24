<?php

require_once('for_php7.php');
class knja080Query extends Query {

    function getA023SchoolKind($model, $name1) {
        if (CTRL_SEMESTER == $model->control["学期数"]){ //最終学期のとき
            $year = CTRL_YEAR + 1;
        } else {
            $year = CTRL_YEAR;
        }
        $query  = "";
        $query .= " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$year}' ";
        $query .= "     AND NAMECD1 = 'A023' ";
        $query .= "     AND NAME1 = '{$name1}' ";
        $query .= " ORDER BY ";
        $query .= "     ABBV2 ";
        return $query;
    }

    function getA023($model) {
        if (CTRL_SEMESTER == $model->control["学期数"]){ //最終学期のとき
            $year = CTRL_YEAR + 1;
        } else {
            $year = CTRL_YEAR;
        }

        $query  = "";
        $query .= " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$year}' ";
        $query .= "     AND NAMECD1 = 'A023' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= " AND NAME1 IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
            }
        } else if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND NAME1 = '".SCHOOLKIND."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     ABBV2 ";
        return $query;
    }

    //ヘッダのクラス取得
    function getHr_Class($model)
    {
        if (CTRL_SEMESTER == $model->control["学期数"]){ //最終学期のとき
            $year = CTRL_YEAR+1;
            $semes = $model->min_semester;    //最小学期
        }else{
            $year = CTRL_YEAR;
            $semes = CTRL_SEMESTER+1;
        }
        //新クラス取得
        $query = " SELECT DISTINCT ";
        $query .= "   'NEW' AS YEAR, ";
        $query .= "   T1.HR_NAME, ";
        $query .= "   T1.GRADE, ";
        $query .= "   T1.HR_CLASS, ";
        $query .= "   T1.TR_CD1, ";
        $query .= "   T2.STAFFNAME_SHOW AS NAME ";
        $query .= " FROM ";
        $query .= "   SCHREG_REGD_HDAT T1 LEFT OUTER JOIN STAFF_MST T2 ON T1.TR_CD1 = T2.STAFFCD ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= "INNER JOIN SCHREG_REGD_GDAT T3 ON T3.YEAR = T1.YEAR AND T3.GRADE = T1.GRADE ";
                $query .= " AND T3.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
            }
        } else if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= "INNER JOIN SCHREG_REGD_GDAT T3 ON T3.YEAR = T1.YEAR AND T3.GRADE = T1.GRADE ";
            $query .= " AND T3.SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= " WHERE ";
        $query .= "   T1.YEAR = '" .$year ."' AND ";
        $query .= "   T1.SEMESTER = '" .$semes ."' ";
        //旧クラス取得

        if (CTRL_SEMESTER != $model->control["学期数"]){ //最終学期じゃないとき
                $query .= " UNION       ";
                $query .= " SELECT DISTINCT ";
                $query .= "   'OLD' AS YEAR, ";
                $query .= "   T1.HR_NAME, ";
                $query .= "   T1.GRADE, ";
                $query .= "   T1.HR_CLASS, ";
                $query .= "   T1.TR_CD1, ";
                $query .= "   T2.STAFFNAME_SHOW AS NAME ";
                $query .= " FROM ";
                $query .= "   SCHREG_REGD_HDAT T1 LEFT OUTER JOIN STAFF_MST T2 ON T1.TR_CD1 = T2.STAFFCD ";
                if ($model->Properties["use_prg_schoolkind"] == "1") {
                    if ($model->selectSchoolKind) {
                        $query .= "INNER JOIN SCHREG_REGD_GDAT T3 ON T3.YEAR = T1.YEAR AND T3.GRADE = T1.GRADE ";
                        $query .= " AND T3.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
                    }
                } else if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
                    $query .= "INNER JOIN SCHREG_REGD_GDAT T3 ON T3.YEAR = T1.YEAR AND T3.GRADE = T1.GRADE ";
                    $query .= " AND T3.SCHOOL_KIND = '".SCHOOLKIND."' ";
                }
                $query .= " WHERE ";
                $query .= "   T1.YEAR     = '" .CTRL_YEAR ."' AND ";
                $query .= "   T1.SEMESTER = '" .CTRL_SEMESTER ."' ";
        }else{ //最終学期のとき
                if ($model->control["学校区分"] == '0'){ //学年制
                        if ($model->Properties["use_prg_schoolkind"] == "1" || $model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
                            $query .= " UNION   ";
                            $query .= " SELECT DISTINCT ";
                            $query .= "   'OLD' AS YEAR, ";
                            $query .= "   T1.HR_NAME, ";
                            $query .= "   T1.GRADE, ";
                            $query .= "   T1.HR_CLASS, ";
                            $query .= "   T1.TR_CD1, ";
                            $query .= "   T2.STAFFNAME_SHOW AS NAME ";
                            $query .= " FROM ";
                            $query .= "   SCHREG_REGD_HDAT T1 LEFT OUTER JOIN STAFF_MST T2 ON T1.TR_CD1 = T2.STAFFCD ";
                            $query .= "   INNER JOIN SCHREG_REGD_GDAT T3 ON ";
                            $query .= "       T3.YEAR     = T1.YEAR AND ";
                            $query .= "       T3.GRADE = T1.GRADE ";
                            $query .= "   INNER JOIN NAME_MST A023 ON ";
                            $query .= "       A023.NAMECD1 = 'A023' AND ";
                            $query .= "       A023.NAME1 = T3.SCHOOL_KIND AND ";
                            $query .= "       A023.NAME3 = T3.GRADE ";
                            $query .= "   INNER JOIN NAME_MST A023_AFT ON ";
                            $query .= "       A023_AFT.NAMECD1 = 'A023' AND ";
                            $query .= "       (INT(A023_AFT.NAME2) = INT(A023.NAME3) + 1 ";
                            $query .= "       OR INT(A023_AFT.ABBV2) = INT(A023.ABBV2) + 1) ";
                            $query .= " WHERE ";
                            $query .= "   T1.YEAR     = '" .CTRL_YEAR ."' AND ";
                            $query .= "   T1.SEMESTER = '" .CTRL_SEMESTER ."' ";
                            if ($model->Properties["use_prg_schoolkind"] == "1") {
                                if ($model->selectSchoolKind) {
                                    $query .= " AND A023_AFT.NAME1 IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
                                }
                            } else if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
                                $query .= " AND A023_AFT.NAME1 = '".SCHOOLKIND."' ";
                            }
                        }
                        $query .= " UNION   ";
                        $query .= " SELECT DISTINCT ";
                        $query .= "   'OLD' AS YEAR, ";
                        $query .= "   T1.HR_NAME, ";
                        $query .= "   T1.GRADE, ";
                        $query .= "   T1.HR_CLASS, ";
                        $query .= "   T1.TR_CD1, ";
                        $query .= "   T3.STAFFNAME_SHOW AS NAME ";
                        $query .= " FROM ";
                        $query .= "   SCHREG_REGD_HDAT T1 LEFT OUTER JOIN STAFF_MST T3 ON T1.TR_CD1 = T3.STAFFCD ";
                        if ($model->Properties["use_prg_schoolkind"] == "1") {
                            if ($model->selectSchoolKind) {
                                $query .= "INNER JOIN SCHREG_REGD_GDAT T4 ON T4.YEAR = T1.YEAR AND T4.GRADE = T1.GRADE ";
                                $query .= " AND T4.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
                            }
                        } else if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
                            $query .= "INNER JOIN SCHREG_REGD_GDAT T4 ON T4.YEAR = T1.YEAR AND T4.GRADE = T1.GRADE ";
                            $query .= " AND T4.SCHOOL_KIND = '".SCHOOLKIND."' ";
                        }
                        $query .= "   ,CLASS_FORMATION_DAT T2 ";
                        $query .= " WHERE ";
                        $query .= "   T1.YEAR           = '" .CTRL_YEAR."' AND ";
                        $query .= "   T1.SEMESTER       = '" .CTRL_SEMESTER."' AND ";
                        $query .= "   INTEGER(T2.YEAR)  = INTEGER(T1.YEAR) + 1 AND ";
                        $query .= "   T2.SEMESTER       = '" .$model->min_semester ."' AND ";
                        $query .= "   T1.GRADE          = T2.OLD_GRADE AND ";
                        $query .= "   T1.HR_CLASS       = T2.OLD_HR_CLASS ";
                        if ($model->isIkkanUseSchKind) {
                            $query .= " UNION ";
                            $query .= " SELECT ";
                            $query .= "     'OLD' AS YEAR, ";
                            $query .= "     T1.HR_NAME, ";
                            $query .= "     T1.GRADE, ";
                            $query .= "     T1.HR_CLASS, ";
                            $query .= "     T1.TR_CD1, ";
                            $query .= "     T2.STAFFNAME_SHOW AS NAME ";
                            $query .= " FROM ";
                            $query .= "     SCHREG_REGD_HDAT T1 ";
                            $query .= "     LEFT JOIN STAFF_MST T2 ON T1.TR_CD1 = T2.STAFFCD, ";
                            $query .= "     NAME_MST N1 ";
                            $query .= "     INNER JOIN (SELECT ";
                            $query .= "                     * ";
                            $query .= "                 FROM ";
                            $query .= "                     NAME_MST ";
                            $query .= "                 WHERE ";
                            $query .= "                     NAMECD1 = 'A023' AND ";
                            $query .= "                     NAME1   = '".SCHOOLKIND."' ";
                            $query .= "                 ) N2 ON INTEGER(N1.ABBV2) = INTEGER(N2.ABBV2)-1 ";
                            $query .= " WHERE ";
                            $query .= "     T1.YEAR     = '".CTRL_YEAR."' AND ";
                            $query .= "     T1.SEMESTER = '".CTRL_SEMESTER."' AND ";
                            $query .= "     N1.NAMECD1  = 'A023' AND ";
                            $query .= "     T1.GRADE BETWEEN N1.NAMESPARE2 AND N1.NAMESPARE3 ";
                        }
                }else{//単位制
                        $query .= " UNION   ";
                        $query .= " SELECT DISTINCT ";
                        $query .= "   'OLD' AS YEAR, ";
                        $query .= "   T1.HR_NAME, ";
                        $query .= "   T1.GRADE, ";
                        $query .= "   T1.HR_CLASS, ";
                        $query .= "   T1.TR_CD1, ";
                        $query .= "   T3.STAFFNAME_SHOW AS NAME ";
                        $query .= " FROM ";
                        $query .= "   SCHREG_REGD_HDAT T1 LEFT OUTER JOIN STAFF_MST T3 ON T1.TR_CD1 = T3.STAFFCD ";
                        if ($model->Properties["use_prg_schoolkind"] == "1") {
                            if ($model->selectSchoolKind) {
                                $query .= "INNER JOIN SCHREG_REGD_GDAT T4 ON T4.YEAR = T1.YEAR AND T4.GRADE = T1.GRADE ";
                                $query .= " AND T4.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
                            }
                        } else if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
                            $query .= "INNER JOIN SCHREG_REGD_GDAT T4 ON T4.YEAR = T1.YEAR AND T4.GRADE = T1.GRADE ";
                            $query .= " AND T4.SCHOOL_KIND = '".SCHOOLKIND."' ";
                        }
                        $query .= "   ,CLASS_FORMATION_DAT T2 ";
                        $query .= " WHERE ";
                        $query .= "   T1.YEAR           = '" .CTRL_YEAR ."' AND ";
                        $query .= "   T1.SEMESTER       = '" .$model->control["学期数"] ."' AND ";
                        $query .= "   INTEGER(T2.YEAR)  = INTEGER(T1.YEAR) + 1 AND ";
                        $query .= "   T2.SEMESTER       = '" .$model->min_semester ."' AND ";
                        $query .= "   T1.GRADE          = T2.OLD_GRADE AND ";
                        $query .= "   T1.HR_CLASS       = T2.OLD_HR_CLASS ";
                }
        }

        $query .= " ORDER BY ";
        $query .= "   YEAR, ";
        $query .= "   GRADE, ";
        $query .= "   HR_CLASS ";

        return $query;
    }
    //編成済み生徒名の取得（左のリスト）
    function GetNewStudent($model)
    {
        list($grade, $hr_class) = explode("-", $model->newclass);
        if (CTRL_SEMESTER == $model->control["学期数"]){ //最終学期のとき
            $year = CTRL_YEAR+1;
            $semes = $model->min_semester;
        }else{
            $year = CTRL_YEAR;
            $semes = CTRL_SEMESTER+1;
        }
        $query = " SELECT ";
        $query .= "     T3.HR_NAME, ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     T1.OLD_GRADE, ";
        $query .= "     T1.OLD_HR_CLASS, ";
        $query .= "     T1.OLD_ATTENDNO, ";
        $query .= "     T1.REMAINGRADE_FLG, ";
        if ((CTRL_SEMESTER == $model->control["学期数"])) {
            $query .= "     CASE WHEN T1.OLD_GRADE IS NOT NULL THEN L1.NAME_SHOW ELSE L2.NAME END AS NAME ";
        } else {
            $query .= "   L1.NAME_SHOW AS NAME ";
        }
        $query .= " FROM ";
        $query .= "     CLASS_FORMATION_DAT T1 ";
        $query .= "     LEFT JOIN SCHREG_REGD_HDAT T3 ON ";
        $query .= "         T3.YEAR = '" .CTRL_YEAR ."' AND ";
        $query .= "         T3.SEMESTER = '" .CTRL_SEMESTER ."' AND ";
        $query .= "         T1.OLD_GRADE = T3.GRADE AND ";
        $query .= "         T1.OLD_HR_CLASS = T3.HR_CLASS ";
        $query .= "     LEFT JOIN SCHREG_BASE_MST L1 ON T1.SCHREGNO = L1.SCHREGNO ";
        $query .= "     LEFT JOIN FRESHMAN_DAT L2 ON T1.YEAR = L2.ENTERYEAR AND T1.SCHREGNO = L2.SCHREGNO ";
        $query .= " WHERE ";
        $query .= "   T1.YEAR = '" .$year ."' AND ";
        $query .= "   T1.SEMESTER = '" .$semes ."' AND ";
        $query .= "   T1.GRADE = '" .$grade ."' AND ";
        $query .= "   T1.HR_CLASS = '" .$hr_class ."' ";
        $query .= " ORDER BY ";
        $query .= "   T1.OLD_GRADE, ";
        $query .= "   T1.OLD_HR_CLASS, ";
        $query .= "   T1.OLD_ATTENDNO ";

        return $query;
    }
    
    //編成される生徒名の取得（右のリスト）
    function GetOldStudent($model)
    {
        list($grade, $hr_class) = explode("-", $model->oldclass);

        if (CTRL_SEMESTER == $model->control["学期数"]){ //最終学期のとき
            //新入生読込
            list($checkGrade, $checkHr) = explode("-", $model->oldclass);
            if ($checkGrade == "00") {
                $query = " SELECT ";
                $query .= "   T3.HR_NAME, ";
                $query .= "   T1.SCHREGNO, ";
                $query .= "   T1.OLD_GRADE, ";
                $query .= "   T1.OLD_HR_CLASS, ";
                $query .= "   T1.OLD_ATTENDNO AS ATTENDNO, ";
                $query .= "   T1.REMAINGRADE_FLG, ";
                $query .= "   case when T1.OLD_GRADE is not null then B2.NAME_SHOW else T2.NAME end as NAME ";
                $query .= " FROM ";
                $query .= "   CLASS_FORMATION_DAT T1 LEFT OUTER JOIN SCHREG_REGD_HDAT T3  ";
                $query .= "   ON T3.YEAR = '" .CTRL_YEAR ."' AND  ";
                $query .= "   T3.SEMESTER = '" .CTRL_SEMESTER ."' AND  ";
                $query .= "   T1.OLD_GRADE = T3.GRADE AND  ";
                $query .= "   T1.OLD_HR_CLASS = T3.HR_CLASS LEFT OUTER JOIN FRESHMAN_DAT T2  ";
                $query .= "   ON T1.SCHREGNO = T2.SCHREGNO AND  ";
                $query .= "   T1.YEAR = T2.ENTERYEAR ";
                $query .= "   LEFT OUTER JOIN SCHREG_BASE_MST B2 ON T1.SCHREGNO = B2.SCHREGNO ";
                $query .= " WHERE ";
                $query .= "   T1.GRADE = '{$model->A023GradeHr[$model->oldclass]["NAME2"]}' AND ";
                $query .= "   T1.REMAINGRADE_FLG = '0' AND ";
                $query .= "   T1.OLD_GRADE IS NULL AND ";
                $query .= "   T1.YEAR = '" .(CTRL_YEAR+1) ."' AND ";
                $query .= "   T1.SEMESTER = '" .$model->min_semester ."' AND ";
                $query .= "   (T1.HR_CLASS IS NULL OR T1.HR_CLASS = '') AND ";
                $query .= "   T1.SCHREGNO NOT IN ('" .str_replace("," ,"','" ,$model->selectdata). "') ";
                $query .= " ORDER BY ";
                $query .= "   OLD_GRADE, ";
                $query .= "   OLD_HR_CLASS, ";
                $query .= "   OLD_ATTENDNO ";
            } else {
                $query = " SELECT ";
                $query .= "   T3.HR_NAME, ";
                $query .= "   T1.SCHREGNO, ";
                $query .= "   T1.OLD_GRADE, ";
                $query .= "   T1.OLD_HR_CLASS, ";
                $query .= "   T1.OLD_ATTENDNO AS ATTENDNO, ";
                $query .= "   T1.REMAINGRADE_FLG, ";
                $query .= "   T2.NAME_SHOW AS NAME ";
                $query .= " FROM ";
                $query .= "   CLASS_FORMATION_DAT T1 ";
                $query .= " LEFT OUTER JOIN SCHREG_REGD_HDAT T3  ";
                $query .= "   ON T3.YEAR   = '" .CTRL_YEAR ."' AND  ";
                $query .= "   T3.SEMESTER  = '" .CTRL_SEMESTER ."' AND  ";
                $query .= "   T1.OLD_GRADE = T3.GRADE AND  ";
                $query .= "   T1.OLD_HR_CLASS = T3.HR_CLASS ";
                $query .= " LEFT OUTER JOIN SCHREG_BASE_MST T2  ";
                $query .= "   ON T1.SCHREGNO = T2.SCHREGNO ";
                $query .= " WHERE ";
                $query .= "   T1.YEAR         = '" .(CTRL_YEAR+1) ."' AND ";
                $query .= "   T1.SEMESTER     = '" .$model->min_semester ."'  AND ";
                $query .= "   T1.OLD_GRADE    = '" .$grade ."' AND ";
                $query .= "   T1.OLD_HR_CLASS = '" .$hr_class ."' AND ";
                $query .= "   ( T1.HR_CLASS IS NULL OR T1.HR_CLASS = '') AND ";
                $query .= "   T1.SCHREGNO NOT IN ('" .str_replace("," ,"','" ,$model->selectdata). "') ";
                $query .= " ORDER BY ";
                $query .= "   OLD_ATTENDNO ";
            }
        }else{
            $query = " SELECT ";
            $query .= "   ST3.HR_NAME, ";
            $query .= "   ST1.SCHREGNO, ";
            $query .= "   ST1.GRADE, ";
            $query .= "   ST1.HR_CLASS, ";
            $query .= "   ST1.ATTENDNO, ";
            $query .= "   ST2.NAME_SHOW AS NAME, ";
            $query .= "   ST4.REMAINGRADE_FLG ";
            $query .= " FROM ";
            $query .= "   SCHREG_REGD_DAT ST1 LEFT OUTER JOIN CLASS_FORMATION_DAT ST4  ";
            $query .= "   ON ST1.SCHREGNO = ST4.SCHREGNO AND ST4.YEAR = '" .CTRL_YEAR ."' AND ST4.SEMESTER = '" .(CTRL_SEMESTER+1) ."', ";
            $query .= "   SCHREG_BASE_MST ST2, ";
            $query .= "   SCHREG_REGD_HDAT ST3 ";
            $query .= " WHERE ";
            $query .= "   ST1.SCHREGNO = ST2.SCHREGNO AND ";
            //HR割振り済みの生徒は省く
            $query .= "   ST1.SCHREGNO NOT IN ( SELECT SCHREGNO FROM CLASS_FORMATION_DAT ";
            $query .= "                          WHERE YEAR =  '" .CTRL_YEAR ."'";
            $query .= "                            AND SEMESTER = '" .(CTRL_SEMESTER+1) ."'";
            $query .= "                            AND (HR_CLASS IS NOT NULL OR HR_CLASS <> '')) AND ";

            $query .= "   ST1.SCHREGNO NOT IN ('" .implode($model->schregno ,"','"). "') AND ";

            $query .= "   ST1.YEAR = '".CTRL_YEAR ."' AND ";
            $query .= "   ST1.YEAR = ST3.YEAR AND ";
            $query .= "   ST1.SEMESTER = '".CTRL_SEMESTER ."' AND ";
            $query .= "   ST1.SEMESTER = ST3.SEMESTER AND ";
            $query .= "   ST1.GRADE = '".$grade ."' AND ";
            $query .= "   ST1.GRADE = ST3.GRADE AND ";
            $query .= "   ST1.HR_CLASS = '".$hr_class ."' AND ";
            $query .= "   ST1.HR_CLASS = ST3.HR_CLASS ";
            $query .= " ORDER BY ";
            $query .= "   GRADE, ";
            $query .= "   HR_CLASS, ";
            $query .= "   ATTENDNO ";

        }
        return $query;
    }

    function &getUpdateQuery($model)
    {
        //近代フラグ取得
        $kindai_flg = knja080Query::getNameMstCount();
    
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        

        list($grade, $hr_class) = explode("-", $model->newclass);
        if (CTRL_SEMESTER == $model->control["学期数"]){ //最終学期のとき
            
            $query = " UPDATE ";
            $query .= "   CLASS_FORMATION_DAT ";
            $query .= " SET ";
            $query .= "   GRADE     = '" .$grade ."', ";
            $query .= "   HR_CLASS  = '" .$hr_class ."' ";
            //近代フラグをチェック
            if ($kindai_flg != 0) {
                //HRクラスコース設定からデータ取得＆設定
                $kindaiRow = $db->getRow(knja080Query::getSchRegdDitailHdat(CTRL_YEAR+1, $model->min_semester, $grade, $hr_class), DB_FETCHMODE_ASSOC);
                $query .= "   ,COURSECD   = '" .$kindaiRow["COURSECD"] ."' ";
                $query .= "   ,MAJORCD    = '" .$kindaiRow["MAJORCD"] ."' ";
                $query .= "   ,COURSECODE = '" .$kindaiRow["COURSECODE"] ."' ";
            }
            $query .= " WHERE ";
            $query .= "   YEAR      = '" .(CTRL_YEAR+1) ."' AND ";
            $query .= "   SEMESTER  = '" .$model->min_semester ."' AND ";
            $query .= "   SCHREGNO IN ('" .str_replace("," ,"','" ,$model->selectdata) ."') ";

            $db->query($query);
            $array = explode(",", $model->selectdata);
            //移動前の生徒と移動後の生徒を求める
            $array = array_diff($model->schregno, array_intersect($model->schregno, $array));

            //右リストの組をNULLにする
            $query = " UPDATE ";
            $query .= "   CLASS_FORMATION_DAT ";
            $query .= " SET ";
            $query .= "   HR_CLASS  = NULL ";
            $query .= " WHERE ";
            $query .= "   YEAR      = '" .(CTRL_YEAR+1) ."' AND ";
            $query .= "   SEMESTER  = '" .$model->min_semester ."' AND ";
            $query .= "   SCHREGNO IN ('" .implode($array, "','") ."') ";

            $db->query($query);
        }else{
            //現在処理学期が１、２学期の場合の処理
            $query = " DELETE FROM ";
            $query .= "   CLASS_FORMATION_DAT ";
            $query .= " WHERE ";
            $query .= "   YEAR      = '". CTRL_YEAR ."' AND ";
            $query .= "   SEMESTER  = '" .(CTRL_SEMESTER+1) ."' AND ";
            $query .= "   SCHREGNO IN ('" .str_replace("," ,"','" ,$model->selectdata). "') ";

            $db->query($query);

            //現在の年組の生徒を削除する
            $query = " DELETE FROM ";
            $query .= "   CLASS_FORMATION_DAT ";
            $query .= " WHERE ";
            $query .= "   YEAR = '". CTRL_YEAR ."' AND ";
            $query .= "   SEMESTER = '" .(CTRL_SEMESTER+1) ."' AND ";
            $query .= "   GRADE    = '" .$grade ."' AND ";
            $query .= "   HR_CLASS = '" .$hr_class ."' ";

            $db->query($query);

            $query = " INSERT INTO CLASS_FORMATION_DAT ( ";
            $query .= " SCHREGNO, ";
            $query .= " YEAR, ";
            $query .= " SEMESTER, ";
            $query .= " GRADE, ";
            $query .= " HR_CLASS, ";
            $query .= " OLD_GRADE, ";
            $query .= " OLD_HR_CLASS, ";
            $query .= " OLD_ATTENDNO, ";
            $query .= " COURSECD, ";
            $query .= " MAJORCD, ";
            $query .= " COURSECODE, ";
            $query .= " UPDATED ";
            $query .= " ) ";
            $query .= "   (SELECT ";
            $query .= "     SCHREGNO, ";
            $query .= "     '". CTRL_YEAR. "' , ";
            $query .= "     '". (CTRL_SEMESTER+1) ."' , ";
            $query .= "     '" .$grade ."' , ";
            $query .= "     '" .$hr_class ."' , ";
            $query .= "     GRADE , ";
            $query .= "     HR_CLASS , ";
            $query .= "     ATTENDNO , ";
            //近代フラグをチェック
            if ($kindai_flg != 0) {
                //HRクラスコース設定からデータ取得＆設定
                $kindaiRow = $db->getRow(knja080Query::getSchRegdDitailHdat(CTRL_YEAR, CTRL_SEMESTER+1, $grade, $hr_class), DB_FETCHMODE_ASSOC);
                $query .= "   '" .$kindaiRow["COURSECD"] ."', ";
                $query .= "   '" .$kindaiRow["MAJORCD"] ."', ";
                $query .= "   '" .$kindaiRow["COURSECODE"] ."', ";
            } else {
                $query .= "     COURSECD , ";
                $query .= "     MAJORCD , ";
                $query .= "     COURSECODE , ";
            }
            $query .= "     SYSDATE() ";
            $query .= "   FROM ";
            $query .= "     SCHREG_REGD_DAT ";
            $query .= "   WHERE ";
            $query .= "     SCHREGNO IN ('" .str_replace("," ,"','" ,$model->selectdata). "') AND ";
            $query .= "     YEAR = '". CTRL_YEAR. "' AND ";
            $query .= "     SEMESTER = '". CTRL_SEMESTER ."' ";
            $query .= "   )  ";

            $db->query($query);
        }
        $db->commit();
        Query::dbCheckIn($db);
        return;
    }

    //ヘッダのクラス取得
    function GetClass($year, $semester)
    {
        $query  = "SELECT T1.grade, INTEGER(T1.hr_class) AS hr_class, T1.tr_cd1, T2.STAFFNAME_SHOW AS name";
        $query .= "  FROM schreg_regd_hdat T1";
        $query .= "       LEFT OUTER JOIN staff_mst T2 ON T1.tr_cd1   = T2.staffcd";
        $query .= " WHERE T1.year     = '".$year."'";
        $query .= "   AND T1.semester = '".$semester."'";
        $query .= " ORDER BY grade, hr_class";

        return $query;
    }

    //次年度の最小の学期を求める
    function getNextYearMinSemes($model){
        $query = " SELECT ";
        $query .= "   MIN(SEMESTER) AS SEMESTER ";
        $query .= " FROM ";
        $query .= "   SEMESTER_MST ";
        $query .= " WHERE ";
        $query .= "   YEAR = '".(CTRL_YEAR+1) ."' ";

        return $query;
    }

    function getNameMst() {
        $query  = "";
        $query .= "SELECT ";
        $query .= "    NAMECD1, ";
        $query .= "    NAMECD2, ";
        $query .= "    NAME1, ";
        $query .= "    NAMESPARE1, ";
        $query .= "    NAMESPARE2 ";
        $query .= "FROM ";
        $query .= "    NAME_MST ";
        $query .= "WHERE ";
        $query .= "    NAMECD1='Z010' AND ";
        $query .= "    NAMECD2='00' ";
        $db = Query::dbCheckOut();
        $rtnRow = array();
        $rtnRow = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);
        return $rtnRow;
    }
    
    function getNameMstCount() {
        $query  = "";
        $query .= "SELECT ";
        $query .= "    COUNT(*) ";
        $query .= "FROM ";
        $query .= "    NAME_MST ";
        $query .= "WHERE ";
        $query .= "    NAMECD1='Z013' AND ";
        $query .= "    NAMECD2='00' AND ";
        $query .= "    NAME1='1' ";
       
        $db = Query::dbCheckOut();
        $cnt = $db->getOne($query);
        Query::dbCheckIn($db);
        return $cnt;
    }
    
    function getSchRegdDitailHdat($year, $semester, $grade, $hr_class) {
        
        $query  = " SELECT ";
        $query .= "      COURSECD";
        $query .= "     ,MAJORCD";
        $query .= "     ,COURSECODE";
        $query .= " FROM schreg_regd_diteil_hdat ";
        $query .= " WHERE year   = '".$year."'";
        $query .= " AND semester = '".$semester."'";
        $query .= " AND grade    = '".$grade."'";
        $query .= " AND hr_class = '".$hr_class."'"; 
        
        return $query;
    }

    //学校区分取得
    function getSchooldiv($model) {
        $query  = " SELECT ";
        $query .= "      SCHOOLDIV ";
        $query .= " FROM ";
        $query .= "     SCHOOL_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND SCHOOLCD    = '".sprintf("%012d", SCHOOLCD)."' ";
            $query .= " AND SCHOOL_KIND = '".SCHOOLKIND."' ";
        }

        return $query;
    }
}
?>
