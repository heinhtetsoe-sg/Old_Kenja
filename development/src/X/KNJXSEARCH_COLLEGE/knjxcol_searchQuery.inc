<?php

require_once('for_php7.php');

class knjxcol_searchQuery extends Query {
    //学校系列
    function getSchoolGroup() {
        $query  = " SELECT ";
        $query .= "     NAMECD2 || ' ' || NAME1 AS LABEL, ";
        $query .= "     NAMECD2 AS VALUE ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = 'E012' ";
        $query .= " ORDER BY ";
        $query .= "     NAMECD2 ";

        return $query;
    }

    //もっとも文字数の多い名前を取得
    function getLongName() {
        $query  = " SELECT ";
        $query .= "     SCHOOL_NAME ";
        $query .= " FROM ";
        $query .= "     COLLEGE_MST ";
        $query .= " WHERE ";
        $query .= "         SCHOOL_CD   IS NOT NULL ";
        $query .= "     AND SCHOOL_NAME IS NOT NULL ";
        if ($model->field["SCHOOL_NAME"] != '') {
            $query .= "     AND SCHOOL_NAME LIKE '%{$model->field["SCHOOL_NAME"]}%' ";
        }
        if ($model->field["SCHOOL_GROUP"] != '') {
            $query .= "     AND SCHOOL_GROUP = '{$model->field["SCHOOL_GROUP"]}' ";
        }
        $query .= " ORDER BY ";
        $query .= "     LENGTH(SCHOOL_NAME) DESC ";
        $query .= " FETCH FIRST 1 ROWS ONLY ";

        return $query;
    }

    //検索結果
    function getSchoolList($model){
        //文字列の文頭、文末に含まれる全角or半角スペースを消去
        $trimSchoolname = preg_replace('/^[ 　]+/u', '', $model->field["SCHOOL_NAME"]);
        $trimSchoolname = preg_replace('/[ 　]+$/u', '', $trimSchoolname);

        $query  = " SELECT ";
        $query .= "     T1.SCHOOL_CD, ";
        $query .= "     SCHOOL_NAME, ";
        $query .= "     BUNAME, ";
        $query .= "     KANAME, ";
        $query .= "     SCHOOL_SORT, ";
        $query .= "     BUNYA, ";
        $query .= "     AREA_NAME, ";
        $query .= "     L2.ZIPCD, ";
        $query .= "     L2.ADDR1, ";
        $query .= "     L2.ADDR2, ";
        $query .= "     L2.TELNO, ";
        $query .= "     GREDES, ";
        $query .= "     SCHOOL_NAME_SHOW1, ";
        $query .= "     SCHOOL_NAME_SHOW2, ";
        $query .= "     SCHOOL_GROUP, ";
        $query .= "     L1.NAME1 AS SCHOOL_GROUP_NAME ";
        $query .= " FROM ";
        $query .= "     COLLEGE_MST T1 ";
        $query .= " LEFT JOIN ";
        $query .= "     NAME_MST L1 ON  L1.NAMECD2 = T1.SCHOOL_GROUP ";
        $query .= "                 AND L1.NAMECD1 = 'E012' ";
        $query .= " LEFT JOIN ";
        $query .= "     COLLEGE_CAMPUS_ADDR_DAT L2 ON  L2.SCHOOL_CD      = T1.SCHOOL_CD ";
        $query .= "                                AND L2.CAMPUS_ADDR_CD = T1.CAMPUS_ADDR_CD ";
        $query .= " WHERE ";
        $query .= "         T1.SCHOOL_CD IS NOT NULL ";
        if ($model->field["SCHOOL_NAME"] != '') {
            $query .= "     AND SCHOOL_NAME LIKE '%{$trimSchoolname}%' ";
        }
        if ($model->field["SCHOOL_GROUP"] != '') {
            $query .= "     AND SCHOOL_GROUP = '{$model->field["SCHOOL_GROUP"]}' ";
        }
        if ($model->field["AREA_NAME"] != '') {
            $query .= "     AND AREA_NAME LIKE '%{$model->field["AREA_NAME"]}%' ";
        }
        $query .= " ORDER BY ";
        $query .= "     T1.SCHOOL_CD ";

        return $query;
    }

    //部名
    function getCollegeFacultyMst($model) {
        $school_cd = explode('|', $model->field["SCHOOL_LIST"]);

        $query  = " SELECT ";
        $query .= "     L1.FACULTYCD || ' ' || L1.FACULTYNAME AS LABEL, ";
        $query .= "     L1.FACULTYCD || '|' || L1.FACULTYNAME || '|' || VALUE(L2.ZIPCD, '') || '|' || VALUE(L2.ADDR1, '') || '|' || VALUE(L2.ADDR2, '') || '|' || VALUE(L2.TELNO, '') AS VALUE ";
        $query .= " FROM ";
        $query .= "     COLLEGE_MST T1 ";
        $query .= " LEFT JOIN COLLEGE_FACULTY_MST L1 ON  L1.SCHOOL_CD = T1.SCHOOL_CD ";
        $query .= " LEFT JOIN COLLEGE_CAMPUS_ADDR_DAT L2 ON  L2.SCHOOL_CD = T1.SCHOOL_CD ";
        $query .= "                                      AND L2.CAMPUS_ADDR_CD = L1.CAMPUS_ADDR_CD ";
        $query .= " WHERE ";
        $query .= "     T1.SCHOOL_CD = '{$school_cd[0]}' ";
        $query .= " ORDER BY ";
        if($model->field["SORT"] == 'on'){
            $query .= "     L1.FACULTYCD DESC";
        } else {
            $query .= "     L1.FACULTYCD ASC";
        }

        return $query;
    }

    //科名
    function getCollegeDepartmentMst($model) {
        $school_cd = explode('|', $model->field["SCHOOL_LIST"]);
        $facultycd = explode('|', $model->field["FACULTY"]);

        $query .= " SELECT ";
        $query .= "     L2.DEPARTMENTCD || ' ' || L2.DEPARTMENTNAME AS LABEL, ";
        $query .= "     L2.DEPARTMENTCD || '|' || L2.DEPARTMENTNAME AS VALUE ";
        $query .= " FROM ";
        $query .= "     COLLEGE_MST T1 ";
        $query .= " LEFT JOIN COLLEGE_FACULTY_MST L1 ON L1.SCHOOL_CD = T1.SCHOOL_CD ";
        $query .= " LEFT JOIN COLLEGE_DEPARTMENT_MST L2 ON  L2.SCHOOL_CD = T1.SCHOOL_CD ";
        $query .= "                             AND L2.FACULTYCD = L1.FACULTYCD ";
        $query .= " WHERE ";
        $query .= "         T1.SCHOOL_CD = '{$school_cd[0]}' ";
        $query .= "     AND L1.FACULTYCD = '{$facultycd[0]}' ";
        $query .= " GROUP BY ";
        $query .= "     L2.DEPARTMENTCD, ";
        $query .= "     L2.DEPARTMENTNAME ";
        $query .= " ORDER BY ";
        if($model->field["SORT"] == 'on'){
            $query .= "     L2.DEPARTMENTCD DESC";
        } else {
            $query .= "     L2.DEPARTMENTCD ASC";
        }

        return $query;
    }

    //学校名取得
    function getSchoolName() {
        $query  = " SELECT ";
        $query .= "     NAME1";
        $query .= " FROM ";
        $query .= "     NAME_MST";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = 'Z010' AND NAMECD2 = '00'";

        return $query;
    }
    
}
?>
