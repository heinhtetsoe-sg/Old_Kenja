<?php

require_once('for_php7.php');

class knjg080Query extends Query {
    //校種取得
    function getSchkind($model) {
        $query  = " SELECT ";
        $query .= "     NAME1 AS VALUE, ";
        $query .= "     ABBV1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = 'A023' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= "     AND NAME1 IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
            }
        }
        $query .= " ORDER BY ";
        $query .= "     NAME1 ";

        return $query;
    }

    //年組取得（権限チェック）
    function getAuth($model) {
        //参照・更新可
        if (AUTHORITY == DEF_REFERABLE || AUTHORITY == DEF_UPDATABLE){
            $query  = "SELECT ";
            $query .= "    GRADE || HR_CLASS AS VALUE, ";
            $query .= "    HR_NAME AS LABEL ";
            $query .= "FROM ";
            $query .= "    SCHREG_REGD_HDAT ";
            $query .= "WHERE ";
            $query .= "    YEAR='".CTRL_YEAR."' ";
            $query .= "    AND SEMESTER='".CTRL_SEMESTER."' ";
            if ($model->Properties["use_prg_schoolkind"] == "1") {
                $query .= " AND GRADE IN (  SELECT ";
                $query .= "                     GRADE ";
                $query .= "                 FROM ";
                $query .= "                     SCHREG_REGD_GDAT ";
                $query .= "                 WHERE ";
                $query .= "                     YEAR        = '".CTRL_YEAR."' AND ";
                $query .= "                     SCHOOL_KIND = '".$model->field["SCHKIND"]."' ";
                $query .= "             ) ";
            } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
                $query .= " AND GRADE IN (  SELECT ";
                $query .= "                     GRADE ";
                $query .= "                 FROM ";
                $query .= "                     SCHREG_REGD_GDAT ";
                $query .= "                 WHERE ";
                $query .= "                     YEAR        = '".CTRL_YEAR."' AND ";
                $query .= "                     SCHOOL_KIND = '".SCHOOLKIND."' ";
                $query .= "             ) ";
            }
        }
        //参照・更新可（制限付き）
        if (AUTHORITY == DEF_REFER_RESTRICT || AUTHORITY == DEF_UPDATE_RESTRICT) {
            $query  = "SELECT ";
            $query .= "    GRADE || HR_CLASS AS VALUE, ";
            $query .= "    HR_NAME AS LABEL ";
            $query .= "FROM ";
            $query .= "    SCHREG_REGD_HDAT ";
            $query .= "WHERE ";
            $query .= "    YEAR='".CTRL_YEAR."' ";
            $query .= "    AND SEMESTER='".CTRL_SEMESTER."' ";
            $query .= "    AND (   TR_CD1    = '" .STAFFCD ."' ";
            $query .= "         OR TR_CD2    = '" .STAFFCD ."' ";
            $query .= "         OR TR_CD3    = '" .STAFFCD ."' ";
            $query .= "         OR SUBTR_CD1 = '" .STAFFCD ."' ";
            $query .= "         OR SUBTR_CD2 = '" .STAFFCD ."' ";
            $query .= "         OR SUBTR_CD3 = '" .STAFFCD ."') ";
            if ($model->Properties["use_prg_schoolkind"] == "1") {
                $query .= " AND GRADE IN (  SELECT ";
                $query .= "                     GRADE ";
                $query .= "                 FROM ";
                $query .= "                     SCHREG_REGD_GDAT ";
                $query .= "                 WHERE ";
                $query .= "                     YEAR        = '".CTRL_YEAR."' AND ";
                $query .= "                     SCHOOL_KIND = '".$model->field["SCHKIND"]."' ";
                $query .= "             ) ";
            } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
                $query .= " AND GRADE IN (  SELECT ";
                $query .= "                     GRADE ";
                $query .= "                 FROM ";
                $query .= "                     SCHREG_REGD_GDAT ";
                $query .= "                 WHERE ";
                $query .= "                     YEAR        = '".CTRL_YEAR."' AND ";
                $query .= "                     SCHOOL_KIND = '".SCHOOLKIND."' ";
                $query .= "             ) ";
            }
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }
    
    //年度一覧取得
    function selectYearQuery($model) {
        $query  = "SELECT DISTINCT ";
        $query .= "    YEAR  AS VALUE, ";
        $query .= "    YEAR  AS LABEL ";
        $query .= "FROM ";
        $query .= "    SCHOOL_MST ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            $query .= " WHERE ";
            $query .= "     SCHOOLCD    = '".sprintf("%012d", SCHOOLCD)."' AND ";
            $query .= "     SCHOOL_KIND = '".$model->field["SCHKIND"]."' ";
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " WHERE ";
            $query .= "     SCHOOLCD    = '".sprintf("%012d", SCHOOLCD)."' AND ";
            $query .= "     SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= "GROUP BY ";
        $query .= "    YEAR ";
        $query .= "ORDER BY ";
        $query .= "    YEAR DESC ";

        return $query;
    }

    function get_name_setup_div($div="") {
        $query  = " SELECT ";
        $query .= "     NAMECD2 || ':' || NAME1 AS LABEL, ";
        $query .= "     NAMECD2 AS VALUE ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = 'D023' ";
        if($div){
            $query .= "     AND NAMECD2 = '".$div."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     NAMECD2 ";
        
        return $query;
    }



/************************************
****生徒用***************************
************************************/

    //生徒取得(左側に表示する生徒)
    function getStudent_left($model) {
        $schregno_array = explode(',', $model->selectdata);
        $schregno_target = implode("','", $schregno_array);

        //参照・更新可
        if (AUTHORITY == DEF_REFERABLE || AUTHORITY == DEF_UPDATABLE){
            $query  = " SELECT ";
            $query .= "     T1.SCHREGNO AS VALUE, ";
            $query .= "     H1.HR_NAME || ' ' || T1.ATTENDNO || '番' || CASE L2.NAME_OUTPUT_FLG WHEN '1' THEN '[通]' ELSE '　　' END || L1.NAME_SHOW AS LABEL, ";
            $query .= "     L2.NAME_OUTPUT_FLG ";
            $query .= " FROM ";
            $query .= "     SCHREG_REGD_DAT T1 ";
            $query .= " LEFT JOIN ";
            $query .= "     SCHREG_BASE_MST L1 ON L1.SCHREGNO = T1.SCHREGNO ";
            $query .= " LEFT JOIN ";
            $query .= "     SCHREG_NAME_SETUP_DAT L2 ON  L2.SCHREGNO = T1.SCHREGNO ";
            $query .= "                              AND L2.DIV      = '{$model->div}' ";
            $query .= " LEFT JOIN ";
            $query .= "     SCHREG_REGD_HDAT H1 ON H1.YEAR     = T1.YEAR ";
            $query .= "                        AND H1.SEMESTER = T1.SEMESTER ";
            $query .= "                        AND H1.GRADE    = T1.GRADE ";
            $query .= "                        AND H1.HR_CLASS = T1.HR_CLASS ";
            $query .= " WHERE ";
            $query .= "     T1.YEAR = '".CTRL_YEAR."' AND ";
            $query .= "     T1.SEMESTER = '".CTRL_SEMESTER."' AND ";
            if ($model->cmd == '' || $model->cmd == 'div' || $model->cmd == 'update' || $model->cmd == 'clear' || $model->cmd == 'back' || $model->cmd == 'knjg080') {
                $query .= "     L2.DIV = '{$model->div}' ";
            } else {
                $query .= "     T1.SCHREGNO IN ('{$schregno_target}') ";
            }
            if ($model->Properties["use_prg_schoolkind"] == "1") {
                $query .= " AND T1.GRADE IN (SELECT ";
                $query .= "                      GRADE ";
                $query .= "                  FROM ";
                $query .= "                      SCHREG_REGD_GDAT ";
                $query .= "                  WHERE ";
                $query .= "                      YEAR        = '".CTRL_YEAR."' AND ";
                $query .= "                      SCHOOL_KIND = '".$model->field["SCHKIND"]."' ";
                $query .= "                 ) ";
            } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
                $query .= " AND T1.GRADE IN (SELECT ";
                $query .= "                      GRADE ";
                $query .= "                  FROM ";
                $query .= "                      SCHREG_REGD_GDAT ";
                $query .= "                  WHERE ";
                $query .= "                      YEAR        = '".CTRL_YEAR."' AND ";
                $query .= "                      SCHOOL_KIND = '".SCHOOLKIND."' ";
                $query .= "                 ) ";
            }
            $query .= " ORDER BY ";
            $query .= "     LABEL ";
        }
        //参照・更新可（制限付き）
        if (AUTHORITY == DEF_REFER_RESTRICT || AUTHORITY == DEF_UPDATE_RESTRICT) {
            $query  = " SELECT ";
            $query .= "     T1.SCHREGNO AS VALUE, ";
            $query .= "     L3.HR_NAME || ' ' || T1.ATTENDNO || '番　' || L1.NAME_SHOW AS LABEL ";
            $query .= " FROM ";
            $query .= "     SCHREG_REGD_DAT T1 ";
            $query .= " LEFT JOIN ";
            $query .= "     SCHREG_BASE_MST L1 ON L1.SCHREGNO = T1.SCHREGNO ";
            $query .= " LEFT JOIN ";
            $query .= "     SCHREG_NAME_SETUP_DAT L2 ON  L2.SCHREGNO = T1.SCHREGNO ";
            $query .= "                              AND L2.DIV      = '{$model->div}' ";
            $query .= " LEFT JOIN ";
            $query .= "     SCHREG_REGD_HDAT L3 ON  L3.YEAR     = T1.YEAR ";
            $query .= "                         AND L3.SEMESTER = T1.SEMESTER ";
            $query .= "                         AND L3.GRADE    = T1.GRADE ";
            $query .= "                         AND L3.HR_CLASS = T1.HR_CLASS ";
            $query .= " WHERE ";
            $query .= "     T1.YEAR = '".CTRL_YEAR."' AND ";
            $query .= "     T1.SEMESTER = '".CTRL_SEMESTER."' AND ";
            $query .= "     (   L3.TR_CD1    = '" .STAFFCD ."' ";
            $query .= "      OR L3.TR_CD2    = '" .STAFFCD ."' ";
            $query .= "      OR L3.TR_CD3    = '" .STAFFCD ."' ";
            $query .= "      OR L3.SUBTR_CD1 = '" .STAFFCD ."' ";
            $query .= "      OR L3.SUBTR_CD2 = '" .STAFFCD ."' ";
            $query .= "      OR L3.SUBTR_CD3 = '" .STAFFCD ."') AND ";
            if ($model->cmd == '' || $model->cmd == 'div' || $model->cmd == 'update' || $model->cmd == 'clear' || $model->cmd == 'back' || $model->cmd == 'knjg080') {
                $query .= "     L2.DIV = '{$model->div}' ";
            } else {
                $query .= "     T1.SCHREGNO IN ('{$schregno_target}') ";
            }
            if ($model->Properties["use_prg_schoolkind"] == "1") {
                $query .= " AND T1.GRADE IN (SELECT ";
                $query .= "                     GRADE ";
                $query .= "                 FROM ";
                $query .= "                     SCHREG_REGD_GDAT ";
                $query .= "                 WHERE ";
                $query .= "                     YEAR        = '".CTRL_YEAR."' AND ";
                $query .= "                     SCHOOL_KIND = '".$model->field["SCHKIND"]."' ";
                $query .= "             ) ";
            } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
                $query .= " AND T1.GRADE IN (SELECT ";
                $query .= "                     GRADE ";
                $query .= "                 FROM ";
                $query .= "                     SCHREG_REGD_GDAT ";
                $query .= "                 WHERE ";
                $query .= "                     YEAR        = '".CTRL_YEAR."' AND ";
                $query .= "                     SCHOOL_KIND = '".SCHOOLKIND."' ";
                $query .= "             ) ";
            }
            $query .= " ORDER BY ";
            $query .= "     LABEL ";
        }

        return $query;
    }

    //生徒取得(右側に表示する生徒)
    function getStudent($model, $left_schregno) {
        $schregno_target = implode("','", (array)$left_schregno);

        $query  = " SELECT ";
        $query .= "     H1.HR_NAME || ' ' || T1.ATTENDNO || '番　' || L1.NAME_SHOW AS LABEL, ";
        $query .= "     T1.SCHREGNO AS VALUE ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1 ";
        $query .= " LEFT JOIN ";
        $query .= "     SCHREG_BASE_MST L1 ON L1.SCHREGNO = T1.SCHREGNO ";
        $query .= " LEFT JOIN ";
        $query .= "     SCHREG_NAME_SETUP_DAT L2 ON  L2.SCHREGNO = T1.SCHREGNO ";
        $query .= "                              AND L2.DIV      = '{$model->div}' ";
        $query .= " LEFT JOIN ";
        $query .= "     SCHREG_REGD_HDAT H1 ON H1.YEAR     = T1.YEAR ";
        $query .= "                        AND H1.SEMESTER = T1.SEMESTER ";
        $query .= "                        AND H1.GRADE    = T1.GRADE ";
        $query .= "                        AND H1.HR_CLASS = T1.HR_CLASS ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' AND ";
        $query .= "     T1.SEMESTER = '".CTRL_SEMESTER."' AND ";
        $query .= "     T1.GRADE || T1.HR_CLASS = '{$model->hrClass}' AND ";
        $query .= "     T1.SCHREGNO NOT IN ('{$schregno_target}') ";
        $query .= " ORDER BY ";
        $query .= "     T1.GRADE, ";
        $query .= "     T1.HR_CLASS, ";
        $query .= "     T1.ATTENDNO ";

        return $query;
    }

    //生徒更新
    function &getUpdateQuery($db, $model, $schregno, $flg) {

        $db->autoCommit(false);

        $query  = " DELETE FROM ";
        $query .= "     SCHREG_NAME_SETUP_DAT ";
        $query .= " WHERE ";
        $query .= "     DIV = '".$model->div."' AND ";
        $query .= "     SCHREGNO = '".$schregno."' ";

        $db->query($query);

        $data = array();
        $data["SCHREGNO"][TEXT]         = $schregno;
        $data["DIV"][TEXT]              = $model->div;
        $data["NAME_OUTPUT_FLG"][TEXT]  = $flg;
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][NUMBER]        = "sysdate()";

        $query = Query::insertSQL($data, "SCHREG_NAME_SETUP_DAT");
        $db->query($query);

        $db->commit(); 
    }

    //生徒削除
    function &getDeleteQuery($db, $model, $flg="") {

        $db->autoCommit(false);

        $query  = " DELETE FROM ";
        $query .= "     SCHREG_NAME_SETUP_DAT ";
        $query .= " WHERE ";
        $query .= "     DIV = '".$model->div."' AND ";
        $query .= "     SCHREGNO IN ";
        $query .= " ( ";
        $query .= " SELECT ";
        $query .= "     T1.SCHREGNO ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1 ";
        $query .= " LEFT JOIN ";
        $query .= "     SCHREG_NAME_SETUP_DAT L2 ON  L2.SCHREGNO = T1.SCHREGNO ";
        $query .= "                              AND L2.DIV      = '".$model->div."' ";
        $query .= " LEFT JOIN ";
        $query .= "     SCHREG_REGD_HDAT L3 ON  L3.YEAR     = T1.YEAR ";
        $query .= "                         AND L3.SEMESTER = T1.SEMESTER ";
        $query .= "                         AND L3.GRADE    = T1.GRADE ";
        $query .= "                         AND L3.HR_CLASS = T1.HR_CLASS ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            $query .= " INNER JOIN ";
            $query .= "     SCHREG_REGD_GDAT G1 ON  G1.YEAR     = T1.YEAR ";
            $query .= "                         AND G1.GRADE    = T1.GRADE ";
            $query .= "                         AND G1.SCHOOL_KIND = '".$model->field["SCHKIND"]."' ";
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " INNER JOIN ";
            $query .= "     SCHREG_REGD_GDAT G1 ON  G1.YEAR     = T1.YEAR ";
            $query .= "                         AND G1.GRADE    = T1.GRADE ";
            $query .= "                         AND G1.SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' AND ";
        $query .= "     T1.SEMESTER = '".CTRL_SEMESTER."' AND ";
        if (AUTHORITY == DEF_REFER_RESTRICT || AUTHORITY == DEF_UPDATE_RESTRICT) {
            $query .= "     (   L3.TR_CD1    = '" .STAFFCD ."' ";
            $query .= "      OR L3.TR_CD2    = '" .STAFFCD ."' ";
            $query .= "      OR L3.TR_CD3    = '" .STAFFCD ."' ";
            $query .= "      OR L3.SUBTR_CD1 = '" .STAFFCD ."' ";
            $query .= "      OR L3.SUBTR_CD2 = '" .STAFFCD ."' ";
            $query .= "      OR L3.SUBTR_CD3 = '" .STAFFCD ."') AND ";
        }
        $query .= "     L2.DIV = '".$model->div."' ";
        $query .= " ) ";
        if($flg == "1"){
            $query .= "     AND SCHREGNO NOT IN ('".implode(explode(',', $model->selectdata),"','")."')";
        }

        $db->query($query);
        $db->commit(); 
    }

    //通称名フラグ取得
    function getNameOutputFlg($model, $schregno) {

            $query  = " SELECT ";
            $query .= "     NAME_OUTPUT_FLG ";
            $query .= " FROM ";
            $query .= "     SCHREG_NAME_SETUP_DAT ";
            $query .= " WHERE ";
            $query .= "     SCHREGNO = '".$schregno."' AND ";
            $query .= "     DIV = '".$model->div."' ";

        return $query;
    }

    //生徒取得
    function getStudentList($model) {

        //参照・更新可
        if (AUTHORITY == DEF_REFERABLE || AUTHORITY == DEF_UPDATABLE){
            $query  = " SELECT ";
            $query .= "     H1.HR_NAME || ' ' || T1.ATTENDNO || '番' AS GRADE_HR_CLASS_NO, ";
            $query .= "     L1.NAME_SHOW AS NAME, ";
            $query .= "     T1.SCHREGNO, ";
            $query .= "     T2.NAME_OUTPUT_FLG ";
            $query .= " FROM ";
            $query .= "     SCHREG_REGD_DAT T1 ";
            $query .= " LEFT JOIN ";
            $query .= "     SCHREG_BASE_MST L1 ON L1.SCHREGNO = T1.SCHREGNO ";
            $query .= " LEFT JOIN ";
            $query .= "     SCHREG_REGD_HDAT H1 ON H1.YEAR     = T1.YEAR ";
            $query .= "                        AND H1.SEMESTER = T1.SEMESTER ";
            $query .= "                        AND H1.GRADE    = T1.GRADE ";
            $query .= "                        AND H1.HR_CLASS = T1.HR_CLASS, ";
            $query .= "     SCHREG_NAME_SETUP_DAT T2 ";
            $query .= " WHERE ";
            $query .= "         T1.YEAR     = '".CTRL_YEAR."' ";
            $query .= "     AND T1.SEMESTER = '".CTRL_SEMESTER."' ";
            $query .= "     AND T1.SCHREGNO = T2.SCHREGNO ";
            $query .= "     AND T2.DIV      = '".$model->div."' ";
            if ($model->Properties["use_prg_schoolkind"] == "1") {
                $query .= " AND T1.GRADE IN (SELECT ";
                $query .= "                     GRADE ";
                $query .= "                 FROM ";
                $query .= "                     SCHREG_REGD_GDAT ";
                $query .= "                 WHERE ";
                $query .= "                     YEAR        = '".CTRL_YEAR."' AND ";
                $query .= "                     SCHOOL_KIND = '".$model->field["SCHKIND"]."' ";
                $query .= "             ) ";
            } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
                $query .= " AND T1.GRADE IN (SELECT ";
                $query .= "                     GRADE ";
                $query .= "                 FROM ";
                $query .= "                     SCHREG_REGD_GDAT ";
                $query .= "                 WHERE ";
                $query .= "                     YEAR        = '".CTRL_YEAR."' AND ";
                $query .= "                     SCHOOL_KIND = '".SCHOOLKIND."' ";
                $query .= "             ) ";
            }
            $query .= " ORDER BY ";
            $query .= "     T1.GRADE, ";
            $query .= "     T1.HR_CLASS, ";
            $query .= "     T1.ATTENDNO ";
        }
        //参照・更新可（制限付き）
        if (AUTHORITY == DEF_REFER_RESTRICT || AUTHORITY == DEF_UPDATE_RESTRICT) {
            $query  = " SELECT ";
            $query .= "     L2.HR_NAME || ' ' || T1.ATTENDNO || '番' AS GRADE_HR_CLASS_NO, ";
            $query .= "     L1.NAME_SHOW AS NAME, ";
            $query .= "     T1.SCHREGNO, ";
            $query .= "     T2.NAME_OUTPUT_FLG ";
            $query .= " FROM ";
            $query .= "     SCHREG_REGD_DAT T1, ";
            $query .= " LEFT JOIN ";
            $query .= "     SCHREG_BASE_MST L1 ON L1.SCHREGNO = T1.SCHREGNO ";
            $query .= " LEFT JOIN ";
            $query .= "     SCHREG_REGD_HDAT L2 ON  L2.YEAR     = T1.YEAR ";
            $query .= "                         AND L2.SEMESTER = T1.SEMESTER ";
            $query .= "                         AND L2.GRADE    = T1.GRADE ";
            $query .= "                         AND L2.HR_CLASS = T1.HR_CLASS, ";
            $query .= "     SCHREG_NAME_SETUP_DAT T2 ";
            $query .= " WHERE ";
            $query .= "     T1.YEAR = '".CTRL_YEAR."' AND ";
            $query .= "     T1.SEMESTER = '".CTRL_SEMESTER."' AND ";
            $query .= "     (   L2.TR_CD1    = '" .STAFFCD ."' ";
            $query .= "      OR L2.TR_CD2    = '" .STAFFCD ."' ";
            $query .= "      OR L2.TR_CD3    = '" .STAFFCD ."' ";
            $query .= "      OR L2.SUBTR_CD1 = '" .STAFFCD ."' ";
            $query .= "      OR L2.SUBTR_CD2 = '" .STAFFCD ."' ";
            $query .= "      OR L2.SUBTR_CD3 = '" .STAFFCD ."') AND ";
            $query .= "     T1.SCHREGNO = T2.SCHREGNO AND ";
            $query .= "     T2.DIV = '".$model->div."' ";
            if ($model->Properties["use_prg_schoolkind"] == "1") {
                $query .= " AND T1.GRADE IN (SELECT ";
                $query .= "                     GRADE ";
                $query .= "                 FROM ";
                $query .= "                     SCHREG_REGD_GDAT ";
                $query .= "                 WHERE ";
                $query .= "                     YEAR        = '".CTRL_YEAR."' AND ";
                $query .= "                     SCHOOL_KIND = '".$model->field["SCHKIND"]."' ";
                $query .= "             ) ";
            } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
                $query .= " AND T1.GRADE IN (SELECT ";
                $query .= "                     GRADE ";
                $query .= "                 FROM ";
                $query .= "                     SCHREG_REGD_GDAT ";
                $query .= "                 WHERE ";
                $query .= "                     YEAR        = '".CTRL_YEAR."' AND ";
                $query .= "                     SCHOOL_KIND = '".SCHOOLKIND."' ";
                $query .= "             ) ";
            }
            $query .= " ORDER BY ";
            $query .= "     T1.GRADE, ";
            $query .= "     T1.HR_CLASS, ";
            $query .= "     T1.ATTENDNO ";
        }
        return $query;
    }

    //更新（詳細設定）
    function &getUpdateQuery2($model) {

        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $schregno = explode(',', $model->selectdata);
        for ($i = 0; $i < get_count($schregno); $i++){

            $check = ($model->checked) ? in_array($schregno[$i], $model->checked) : "";

            $data["NAME_OUTPUT_FLG"][TEXT]      = ($check) ? "1" : "";
            $data["REGISTERCD"][TEXT]           = STAFFCD;
            $data["UPDATED"][NUMBER]            = "sysdate()";

            $where  = " WHERE SCHREGNO = '" .$schregno[$i]."' AND ";
            $where .= "       DIV      = '" .$model->div."' ";

            $query = Query::updateSQL($data, "SCHREG_NAME_SETUP_DAT", $where);
            $db->query($query);
        }

        $db->commit(); 
        Query::dbCheckIn($db);
    }


/************************************
****職員用************************
************************************/

    //職員取得(左側に表示する職員)
    function getStaff_left($model) {
        $staffcd_array = explode(',', $model->selectstaffdata);
        $staffcd_target = implode("','", $staffcd_array);

        $query  = " SELECT ";
        $query .= "     T1.STAFFCD AS VALUE, ";
        $query .= "     T1.STAFFCD  || '　' || CASE L2.NAME_OUTPUT_FLG WHEN '1' THEN '[通]' ELSE '　　' END || L1.STAFFNAME AS LABEL, ";
        $query .= "     L2.NAME_OUTPUT_FLG ";
        $query .= " FROM ";
        $query .= "     STAFF_YDAT T1 ";
        $query .= " LEFT JOIN ";
        $query .= "     STAFF_MST L1 ON L1.STAFFCD = T1.STAFFCD ";
        $query .= " LEFT JOIN ";
        $query .= "     STAFF_NAME_SETUP_DAT L2 ON  L2.STAFFCD = T1.STAFFCD ";
        $query .= "                             AND L2.DIV     = '{$model->div}' ";
        $query .= "                             AND L2.YEAR    = T1.YEAR ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".$model->year."' AND ";
        if ($model->cmd == '' || $model->cmd == 'div' || $model->cmd == 'update_staff' || $model->cmd == 'clear' || $model->cmd == 'back' || $model->cmd == 'knjg080' || $model->cmd == 'back_change') {
            $query .= "     L2.DIV = '{$model->div}' ";
        } else {
            $query .= "     T1.STAFFCD IN ('{$staffcd_target}') ";
        }
        $query .= " ORDER BY ";
        $query .= "     LABEL ";

        return $query;
    }

    //職員取得(右側に表示する職員)
    function getStaff($model, $left_staffcd) {
        $staffcd_target = implode("','", (array)$left_staffcd);

        $query  = " SELECT ";
        $query .= "     T1.STAFFCD  || '　' || L1.STAFFNAME AS LABEL, ";
        $query .= "     T1.STAFFCD AS VALUE ";
        $query .= " FROM ";
        $query .= "     STAFF_YDAT T1 ";
        $query .= " LEFT JOIN ";
        $query .= "     STAFF_MST L1 ON L1.STAFFCD = T1.STAFFCD ";
        $query .= " LEFT JOIN ";
        $query .= "     STAFF_NAME_SETUP_DAT L2 ON  L2.STAFFCD = T1.STAFFCD ";
        $query .= "                             AND L2.DIV     = '{$model->div}' ";
        $query .= "                             AND L2.YEAR    = T1.YEAR ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".$model->year."' AND ";
        $query .= "     T1.STAFFCD NOT IN ('{$staffcd_target}') ";
        $query .= " ORDER BY ";
        $query .= "     LABEL ";

        return $query;
    }

    //職員更新
    function &getUpdateQuery_staff($db, $model, $staffcd, $flg) {

        $db->autoCommit(false);

        $query  = " DELETE FROM ";
        $query .= "     STAFF_NAME_SETUP_DAT ";
        $query .= " WHERE ";
        $query .= "     DIV     = '".$model->div."' ";
        $query .= " AND STAFFCD = '".$staffcd."' ";
        $query .= " AND YEAR    = '".$model->year."' ";

        $db->query($query);

        $data = array();
        $data["YEAR"][TEXT]             = $model->year;
        $data["STAFFCD"][TEXT]          = $staffcd;
        $data["DIV"][TEXT]              = $model->div;
        $data["NAME_OUTPUT_FLG"][TEXT]  = $flg;
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][NUMBER]        = "sysdate()";

        $query = Query::insertSQL($data, "STAFF_NAME_SETUP_DAT");
        $db->query($query);

        $db->commit(); 
    }

    //職員削除
    function &getDeleteQuery_staff($db, $model, $flg="") {

        $db->autoCommit(false);

        $query  = " DELETE FROM ";
        $query .= "     STAFF_NAME_SETUP_DAT ";
        $query .= " WHERE ";
        $query .= "     DIV  = '".$model->div."' AND ";
        $query .= "     YEAR = '".$model->year."' AND ";
        $query .= "     STAFFCD IN ";
        $query .= " ( ";
        $query .= " SELECT ";
        $query .= "     T1.STAFFCD ";
        $query .= " FROM ";
        $query .= "     STAFF_YDAT T1 ";
        $query .= " LEFT JOIN ";
        $query .= "     STAFF_NAME_SETUP_DAT L2 ON  L2.STAFFCD = T1.STAFFCD ";
        $query .= "                             AND L2.DIV      = '".$model->div."' ";
        $query .= "                             AND L2.YEAR    = T1.YEAR ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".$model->year."' AND ";
        $query .= "     L2.DIV = '".$model->div."' ";
        $query .= " ) ";
        if($flg == "1"){
            $query .= "     AND STAFFCD NOT IN ('".implode(explode(',', $model->selectstaffdata),"','")."')";
        }
        $db->query($query);
        $db->commit(); 
    }

    //職員通称名フラグ取得
    function getNameOutputFlg_staff($model, $staffcd) {

            $query  = " SELECT ";
            $query .= "     NAME_OUTPUT_FLG ";
            $query .= " FROM ";
            $query .= "     STAFF_NAME_SETUP_DAT ";
            $query .= " WHERE ";
            $query .= "     STAFFCD = '".$staffcd."' ";
            $query .= " AND     DIV = '".$model->div."' ";
            $query .= " AND    YEAR = '".$model->year."' ";

        return $query;
    }

    //職員取得
    function getStaffList($model) {

        $query  = " SELECT ";
        $query .= "     T1.STAFFCD, ";
        $query .= "     L1.STAFFNAME, ";
        $query .= "     L2.NAME_OUTPUT_FLG ";
        $query .= " FROM ";
        $query .= "     STAFF_YDAT T1 ";
        $query .= " LEFT JOIN ";
        $query .= "     STAFF_MST L1 ON L1.STAFFCD = T1.STAFFCD ";
        $query .= " LEFT JOIN ";
        $query .= "     STAFF_NAME_SETUP_DAT L2 ON  L2.STAFFCD = T1.STAFFCD ";
        $query .= "                             AND L2.DIV      = '".$model->div."' ";
        $query .= "                             AND L2.YEAR    = T1.YEAR ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".$model->year."' AND ";
        $query .= "     T1.STAFFCD = L2.STAFFCD AND ";
        $query .= "     L2.DIV = '".$model->div."' ";
        $query .= " ORDER BY ";
        $query .= "     T1.STAFFCD ";

        return $query;
    }


    //職員更新（詳細設定）
    function &getUpdateQuery2_staff($model) {

        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $staffcd = explode(',', $model->selectstaffdata);
        for ($i = 0; $i < get_count($staffcd); $i++){

            $check = ($model->checked) ? in_array($staffcd[$i], $model->checked) : "";

            $data["YEAR"][TEXT]                 = $model->year;
            $data["NAME_OUTPUT_FLG"][TEXT]      = ($check) ? "1" : "";
            $data["REGISTERCD"][TEXT]           = STAFFCD;
            $data["UPDATED"][NUMBER]            = "sysdate()";

            $where  = " WHERE STAFFCD  = '" .$staffcd[$i]."'";
            $where .= "     AND   DIV  = '" .$model->div."' ";
            $where .= "     AND   YEAR = '" .$model->year."' ";

            $query = Query::updateSQL($data, "STAFF_NAME_SETUP_DAT", $where);
            $db->query($query);
        }
        $db->commit(); 
        Query::dbCheckIn($db);
    }
    
    
/************************************
****保護者用***************************
************************************/

    //生徒取得(左側に表示する生徒)（保護者）
    function getGuardian_left($model) {
        $guardian_array = explode(',', $model->selectguardiandata);
        $guardian_target = implode("','", $guardian_array);

        //参照・更新可
        if (AUTHORITY == DEF_REFERABLE || AUTHORITY == DEF_UPDATABLE){
            $query  = " SELECT ";
            $query .= "     T1.SCHREGNO AS VALUE, ";
            $query .= "     H1.HR_NAME || ' ' || T1.ATTENDNO || '番' || CASE L2.GUARD_NAME_OUTPUT_FLG WHEN '1' THEN '[通]' ELSE '　　' END || L1.NAME_SHOW AS LABEL, ";
            $query .= "     L2.GUARD_NAME_OUTPUT_FLG ";
            $query .= " FROM ";
            $query .= "     SCHREG_REGD_DAT T1 ";
            $query .= " LEFT JOIN ";
            $query .= "     SCHREG_BASE_MST L1 ON L1.SCHREGNO = T1.SCHREGNO ";
            $query .= " LEFT JOIN ";
            $query .= "     GUARDIAN_NAME_SETUP_DAT L2 ON  L2.SCHREGNO = T1.SCHREGNO ";
            $query .= "                                AND L2.DIV      = '{$model->div}' ";
            $query .= " LEFT JOIN ";
            $query .= "     SCHREG_REGD_HDAT H1 ON H1.YEAR     = T1.YEAR ";
            $query .= "                        AND H1.SEMESTER = T1.SEMESTER ";
            $query .= "                        AND H1.GRADE    = T1.GRADE ";
            $query .= "                        AND H1.HR_CLASS = T1.HR_CLASS ";
            $query .= " WHERE ";
            $query .= "     T1.YEAR = '".CTRL_YEAR."' AND ";
            $query .= "     T1.SEMESTER = '".CTRL_SEMESTER."' AND ";
            if ($model->cmd == '' || $model->cmd == 'div' || $model->cmd == 'update_guardian' || $model->cmd == 'clear' || $model->cmd == 'back' || $model->cmd == 'knjg080' || $model->cmd == 'back_change2') {
                $query .= "     L2.DIV = '{$model->div}' ";
            } else {
                $query .= "     T1.SCHREGNO IN ('{$guardian_target}') ";
            }
            if ($model->Properties["use_prg_schoolkind"] == "1") {
                $query .= " AND T1.GRADE IN (SELECT ";
                $query .= "                     GRADE ";
                $query .= "                 FROM ";
                $query .= "                     SCHREG_REGD_GDAT ";
                $query .= "                 WHERE ";
                $query .= "                     YEAR        = '".CTRL_YEAR."' AND ";
                $query .= "                     SCHOOL_KIND = '".$model->field["SCHKIND"]."' ";
                $query .= "             ) ";
            } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
                $query .= " AND T1.GRADE IN (SELECT ";
                $query .= "                     GRADE ";
                $query .= "                 FROM ";
                $query .= "                     SCHREG_REGD_GDAT ";
                $query .= "                 WHERE ";
                $query .= "                     YEAR        = '".CTRL_YEAR."' AND ";
                $query .= "                     SCHOOL_KIND = '".SCHOOLKIND."' ";
                $query .= "             ) ";
            }
            $query .= " ORDER BY ";
            $query .= "     LABEL ";
        }
        //参照・更新可（制限付き）
        if (AUTHORITY == DEF_REFER_RESTRICT || AUTHORITY == DEF_UPDATE_RESTRICT) {
            $query  = " SELECT ";
            $query .= "     T1.SCHREGNO AS VALUE, ";
            $query .= "     L3.HR_NAME || ' ' || T1.ATTENDNO || '番　' || L1.NAME_SHOW AS LABEL ";
            $query .= " FROM ";
            $query .= "     SCHREG_REGD_DAT T1 ";
            $query .= " LEFT JOIN ";
            $query .= "     SCHREG_BASE_MST L1 ON L1.SCHREGNO = T1.SCHREGNO ";
            $query .= " LEFT JOIN ";
            $query .= "     GUARDIAN_NAME_SETUP_DAT L2 ON  L2.SCHREGNO = T1.SCHREGNO ";
            $query .= "                              AND L2.DIV      = '{$model->div}' ";
            $query .= " LEFT JOIN ";
            $query .= "     SCHREG_REGD_HDAT L3 ON  L3.YEAR     = T1.YEAR ";
            $query .= "                         AND L3.SEMESTER = T1.SEMESTER ";
            $query .= "                         AND L3.GRADE    = T1.GRADE ";
            $query .= "                         AND L3.HR_CLASS = T1.HR_CLASS ";
            $query .= " WHERE ";
            $query .= "     T1.YEAR = '".CTRL_YEAR."' AND ";
            $query .= "     T1.SEMESTER = '".CTRL_SEMESTER."' AND ";
            $query .= "     (   L3.TR_CD1    = '" .STAFFCD ."' ";
            $query .= "      OR L3.TR_CD2    = '" .STAFFCD ."' ";
            $query .= "      OR L3.TR_CD3    = '" .STAFFCD ."' ";
            $query .= "      OR L3.SUBTR_CD1 = '" .STAFFCD ."' ";
            $query .= "      OR L3.SUBTR_CD2 = '" .STAFFCD ."' ";
            $query .= "      OR L3.SUBTR_CD3 = '" .STAFFCD ."') AND ";
            if ($model->cmd == '' || $model->cmd == 'div' || $model->cmd == 'update_guardian' || $model->cmd == 'clear' || $model->cmd == 'back' || $model->cmd == 'knjg080' || $model->cmd == 'back_change2') {
                $query .= "     L2.DIV = '{$model->div}' ";
            } else {
                $query .= "     T1.SCHREGNO IN ('{$guardian_target}') ";
            }
            if ($model->Properties["use_prg_schoolkind"] == "1") {
                $query .= " AND T1.GRADE IN (SELECT ";
                $query .= "                     GRADE ";
                $query .= "                 FROM ";
                $query .= "                     SCHREG_REGD_GDAT ";
                $query .= "                 WHERE ";
                $query .= "                     YEAR        = '".CTRL_YEAR."' AND ";
                $query .= "                     SCHOOL_KIND = '".$model->field["SCHKIND"]."' ";
                $query .= "             ) ";
            } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
                $query .= " AND T1.GRADE IN (SELECT ";
                $query .= "                     GRADE ";
                $query .= "                 FROM ";
                $query .= "                     SCHREG_REGD_GDAT ";
                $query .= "                 WHERE ";
                $query .= "                     YEAR        = '".CTRL_YEAR."' AND ";
                $query .= "                     SCHOOL_KIND = '".SCHOOLKIND."' ";
                $query .= "             ) ";
            }
            $query .= " ORDER BY ";
            $query .= "     LABEL ";
        }
        return $query;
    }

    //生徒取得(右側に表示する生徒)（保護者）
    function getGuardian($model, $left_guardian) {
        $guardian_target = implode("','", (array)$left_guardian);

        $query  = " SELECT ";
        $query .= "     H1.HR_NAME || ' ' || T1.ATTENDNO || '番　' || L1.NAME_SHOW AS LABEL, ";
        $query .= "     T1.SCHREGNO AS VALUE ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1 ";
        $query .= " LEFT JOIN ";
        $query .= "     SCHREG_BASE_MST L1 ON L1.SCHREGNO = T1.SCHREGNO ";
        $query .= " LEFT JOIN ";
        $query .= "     GUARDIAN_NAME_SETUP_DAT L2 ON  L2.SCHREGNO = T1.SCHREGNO ";
        $query .= "                                AND L2.DIV      = '{$model->div}' ";
        $query .= " LEFT JOIN ";
        $query .= "     SCHREG_REGD_HDAT H1 ON H1.YEAR     = T1.YEAR ";
        $query .= "                        AND H1.SEMESTER = T1.SEMESTER ";
        $query .= "                        AND H1.GRADE    = T1.GRADE ";
        $query .= "                        AND H1.HR_CLASS = T1.HR_CLASS ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' AND ";
        $query .= "     T1.SEMESTER = '".CTRL_SEMESTER."' AND ";
        $query .= "     T1.GRADE || T1.HR_CLASS = '{$model->hrClass}' AND ";
        $query .= "     T1.SCHREGNO NOT IN ('{$guardian_target}') ";
        $query .= " ORDER BY ";
        $query .= "     T1.GRADE, ";
        $query .= "     T1.HR_CLASS, ";
        $query .= "     T1.ATTENDNO ";
        return $query;
    }

    //生徒更新（保護者）
    function &getUpdateQuery_guardian($db, $model, $schregno, $flg) {

        $db->autoCommit(false);

        $query  = " DELETE FROM ";
        $query .= "     GUARDIAN_NAME_SETUP_DAT ";
        $query .= " WHERE ";
        $query .= "     DIV = '".$model->div."' AND ";
        $query .= "     SCHREGNO = '".$schregno."' ";

        $db->query($query);

        $data = array();
        $data["SCHREGNO"][TEXT]             = $schregno;
        $data["DIV"][TEXT]                  = $model->div;
        $data["GUARD_NAME_OUTPUT_FLG"][TEXT]= $flg;
        $data["REGISTERCD"][TEXT]           = STAFFCD;
        $data["UPDATED"][NUMBER]            = "sysdate()";

        $query = Query::insertSQL($data, "GUARDIAN_NAME_SETUP_DAT");
        $db->query($query);
        $db->commit(); 
    }

    //生徒削除（保護者）
    function &getDeleteQuery_guardian($db, $model, $flg="") {

        $db->autoCommit(false);

        $query  = " DELETE FROM ";
        $query .= "     GUARDIAN_NAME_SETUP_DAT ";
        $query .= " WHERE ";
        $query .= "     DIV = '".$model->div."' AND ";
        $query .= "     SCHREGNO IN ";
        $query .= " ( ";
        $query .= " SELECT ";
        $query .= "     T1.SCHREGNO ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1 ";
        $query .= " LEFT JOIN ";
        $query .= "     GUARDIAN_NAME_SETUP_DAT L2 ON  L2.SCHREGNO = T1.SCHREGNO ";
        $query .= "                                AND L2.DIV      = '".$model->div."' ";
        $query .= " LEFT JOIN ";
        $query .= "     SCHREG_REGD_HDAT L3 ON  L3.YEAR     = T1.YEAR ";
        $query .= "                         AND L3.SEMESTER = T1.SEMESTER ";
        $query .= "                         AND L3.GRADE    = T1.GRADE ";
        $query .= "                         AND L3.HR_CLASS = T1.HR_CLASS ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            $query .= " INNER JOIN ";
            $query .= "     SCHREG_REGD_GDAT G1 ON  G1.YEAR     = T1.YEAR ";
            $query .= "                         AND G1.GRADE    = T1.GRADE ";
            $query .= "                         AND G1.SCHOOL_KIND = '".$model->field["SCHKIND"]."' ";
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " INNER JOIN ";
            $query .= "     SCHREG_REGD_GDAT G1 ON  G1.YEAR     = T1.YEAR ";
            $query .= "                         AND G1.GRADE    = T1.GRADE ";
            $query .= "                         AND G1.SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' AND ";
        $query .= "     T1.SEMESTER = '".CTRL_SEMESTER."' AND ";
        if (AUTHORITY == DEF_REFER_RESTRICT || AUTHORITY == DEF_UPDATE_RESTRICT) {
            $query .= "     (   L3.TR_CD1    = '" .STAFFCD ."' ";
            $query .= "      OR L3.TR_CD2    = '" .STAFFCD ."' ";
            $query .= "      OR L3.TR_CD3    = '" .STAFFCD ."' ";
            $query .= "      OR L3.SUBTR_CD1 = '" .STAFFCD ."' ";
            $query .= "      OR L3.SUBTR_CD2 = '" .STAFFCD ."' ";
            $query .= "      OR L3.SUBTR_CD3 = '" .STAFFCD ."') AND ";
        }
        $query .= "     L2.DIV = '".$model->div."' ";
        $query .= " ) ";
        if($flg == "1"){
            $query .= "     AND SCHREGNO NOT IN ('".implode(explode(',', $model->selectguardiandata),"','")."')";
        }
        $db->query($query);
        $db->commit(); 
    }

    //保護者の通称名フラグ取得
    function getGuardNameOutputFlg($model, $schregno) {

            $query  = " SELECT ";
            $query .= "     GUARD_NAME_OUTPUT_FLG ";
            $query .= " FROM ";
            $query .= "     GUARDIAN_NAME_SETUP_DAT ";
            $query .= " WHERE ";
            $query .= "     SCHREGNO = '".$schregno."' AND ";
            $query .= "     DIV = '".$model->div."' ";
        return $query;
    }

    //生徒取得（保護者）
    function getGuardianList($model) {

        //参照・更新可
        if (AUTHORITY == DEF_REFERABLE || AUTHORITY == DEF_UPDATABLE){
            $query  = " SELECT ";
            $query .= "     H1.HR_NAME || ' ' || T1.ATTENDNO || '番' AS GRADE_HR_CLASS_NO, ";
            $query .= "     L1.NAME_SHOW AS NAME, ";
            $query .= "     T1.SCHREGNO, ";
            $query .= "     T2.GUARD_NAME_OUTPUT_FLG, ";
            $query .= "     L3.GUARD_NAME ";
            $query .= " FROM ";
            $query .= "     SCHREG_REGD_DAT T1 ";
            $query .= " LEFT JOIN ";
            $query .= "     SCHREG_BASE_MST L1 ON L1.SCHREGNO = T1.SCHREGNO ";
            $query .= " LEFT JOIN ";
            $query .= "     GUARDIAN_DAT L3 ON L3.SCHREGNO = T1.SCHREGNO ";
            $query .= " LEFT JOIN ";
            $query .= "     SCHREG_REGD_HDAT H1 ON H1.YEAR     = T1.YEAR ";
            $query .= "                        AND H1.SEMESTER = T1.SEMESTER ";
            $query .= "                        AND H1.GRADE    = T1.GRADE ";
            $query .= "                        AND H1.HR_CLASS = T1.HR_CLASS, ";
            $query .= "     GUARDIAN_NAME_SETUP_DAT T2 ";
            $query .= " WHERE ";
            $query .= "     T1.YEAR = '".CTRL_YEAR."' AND ";
            $query .= "     T1.SEMESTER = '".CTRL_SEMESTER."' AND ";
            $query .= "     T1.SCHREGNO = T2.SCHREGNO AND ";
            $query .= "     T2.DIV = '".$model->div."' ";
            if ($model->Properties["use_prg_schoolkind"] == "1") {
                $query .= " AND T1.GRADE IN (SELECT ";
                $query .= "                     GRADE ";
                $query .= "                 FROM ";
                $query .= "                     SCHREG_REGD_GDAT ";
                $query .= "                 WHERE ";
                $query .= "                     YEAR        = '".CTRL_YEAR."' AND ";
                $query .= "                     SCHOOL_KIND = '".$model->field["SCHKIND"]."' ";
                $query .= "             ) ";
            } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
                $query .= " AND T1.GRADE IN (SELECT ";
                $query .= "                     GRADE ";
                $query .= "                 FROM ";
                $query .= "                     SCHREG_REGD_GDAT ";
                $query .= "                 WHERE ";
                $query .= "                     YEAR        = '".CTRL_YEAR."' AND ";
                $query .= "                     SCHOOL_KIND = '".SCHOOLKIND."' ";
                $query .= "             ) ";
            }
            $query .= " ORDER BY ";
            $query .= "     T1.GRADE, ";
            $query .= "     T1.HR_CLASS, ";
            $query .= "     T1.ATTENDNO ";
        }
        //参照・更新可（制限付き）
        if (AUTHORITY == DEF_REFER_RESTRICT || AUTHORITY == DEF_UPDATE_RESTRICT) {
            $query  = " SELECT ";
            $query .= "     L2.HR_NAME || ' ' || T1.ATTENDNO || '番' AS GRADE_HR_CLASS_NO, ";
            $query .= "     L1.NAME_SHOW AS NAME, ";
            $query .= "     T1.SCHREGNO, ";
            $query .= "     T2.GUARD_NAME_OUTPUT_FLG, ";
            $query .= "     L3.GUARD_NAME ";
            $query .= " FROM ";
            $query .= "     SCHREG_REGD_DAT T1, ";
            $query .= " LEFT JOIN ";
            $query .= "     SCHREG_BASE_MST L1 ON L1.SCHREGNO = T1.SCHREGNO ";
            $query .= " LEFT JOIN ";
            $query .= "     GUARDIAN_DAT L3 ON L3.SCHREGNO = T1.SCHREGNO ";
            $query .= " LEFT JOIN ";
            $query .= "     SCHREG_REGD_HDAT L2 ON  L2.YEAR     = T1.YEAR ";
            $query .= "                         AND L2.SEMESTER = T1.SEMESTER ";
            $query .= "                         AND L2.GRADE    = T1.GRADE ";
            $query .= "                         AND L2.HR_CLASS = T1.HR_CLASS, ";
            $query .= "     GUARDIAN_NAME_SETUP_DAT T2 ";
            $query .= " WHERE ";
            $query .= "     T1.YEAR = '".CTRL_YEAR."' AND ";
            $query .= "     T1.SEMESTER = '".CTRL_SEMESTER."' AND ";
            $query .= "     (   L2.TR_CD1    = '" .STAFFCD ."' ";
            $query .= "      OR L2.TR_CD2    = '" .STAFFCD ."' ";
            $query .= "      OR L2.TR_CD3    = '" .STAFFCD ."' ";
            $query .= "      OR L2.SUBTR_CD1 = '" .STAFFCD ."' ";
            $query .= "      OR L2.SUBTR_CD2 = '" .STAFFCD ."' ";
            $query .= "      OR L2.SUBTR_CD3 = '" .STAFFCD ."') AND ";
            $query .= "     T1.SCHREGNO = T2.SCHREGNO AND ";
            $query .= "     T2.DIV = '".$model->div."' ";
            if ($model->Properties["use_prg_schoolkind"] == "1") {
                $query .= " AND T1.GRADE IN (SELECT ";
                $query .= "                     GRADE ";
                $query .= "                 FROM ";
                $query .= "                     SCHREG_REGD_GDAT ";
                $query .= "                 WHERE ";
                $query .= "                     YEAR        = '".CTRL_YEAR."' AND ";
                $query .= "                     SCHOOL_KIND = '".$model->field["SCHKIND"]."' ";
                $query .= "             ) ";
            } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
                $query .= " AND T1.GRADE IN (SELECT ";
                $query .= "                     GRADE ";
                $query .= "                 FROM ";
                $query .= "                     SCHREG_REGD_GDAT ";
                $query .= "                 WHERE ";
                $query .= "                     YEAR        = '".CTRL_YEAR."' AND ";
                $query .= "                     SCHOOL_KIND = '".SCHOOLKIND."' ";
                $query .= "             ) ";
            }
            $query .= " ORDER BY ";
            $query .= "     T1.GRADE, ";
            $query .= "     T1.HR_CLASS, ";
            $query .= "     T1.ATTENDNO ";
        }
        return $query;
    }

    //更新（詳細設定）
    function &getUpdateQuery2_guardian($model) {

        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $schregno = explode(',', $model->selectguardiandata);
        for ($i = 0; $i < get_count($schregno); $i++){

            $check = ($model->checked) ? in_array($schregno[$i], $model->checked) : "";

            $data["GUARD_NAME_OUTPUT_FLG"][TEXT]= ($check) ? "1" : "";
            $data["REGISTERCD"][TEXT]           = STAFFCD;
            $data["UPDATED"][NUMBER]            = "sysdate()";

            $where  = " WHERE SCHREGNO = '" .$schregno[$i]."' AND ";
            $where .= "       DIV      = '" .$model->div."' ";
            $query = Query::updateSQL($data, "GUARDIAN_NAME_SETUP_DAT", $where);
            $db->query($query);
        }

        $db->commit(); 
        Query::dbCheckIn($db);
    }
}
?>
