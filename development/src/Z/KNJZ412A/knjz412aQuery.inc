<?php

require_once('for_php7.php');

class knjz412aQuery extends Query
{
/******************************************************************************************/
/******************************************************************************************/
/*******                  *****************************************************************/
/******* 左フレームで使う *****************************************************************/
/*******                  *****************************************************************/
/******************************************************************************************/
/******************************************************************************************/
    public function getFacultycd($model)
    {
        $query .= " SELECT ";
        $query .= "     FACULTYCD || ' ' || FACULTYNAME AS LABEL, ";
        $query .= "     FACULTYCD AS VALUE ";
        $query .= " FROM ";
        $query .= "     COLLEGE_FACULTY_MST ";
        $query .= " WHERE ";
        $query .= "     SCHOOL_CD = '{$model->school_cd}' ";

        return $query;
    }

    //学科一覧取得
    public function getList($model)
    {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     COLLEGE_DEPARTMENT_MST ";
        $query .= " WHERE ";
        $query .= "     SCHOOL_CD = '{$model->school_cd}' AND ";
        $query .= "     FACULTYCD = '{$model->facultycd}' ";

        return $query;
    }

    //学校名と学校コードを使うためのクエリ
    public function getCollegeMst($model)
    {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     COLLEGE_MST ";
        $query .= " WHERE ";
        $query .= "     SCHOOL_CD = '{$model->school_cd}' ";

        return $query;
    }

/******************************************************************************************/
/******************************************************************************************/
/*******                  *****************************************************************/
/******* 右フレームで使う *****************************************************************/
/*******                  *****************************************************************/
/******************************************************************************************/
/******************************************************************************************/
    //学科マスタから取得
    public function getCollegeDepartmentMst($model)
    {
        $query .= " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     COLLEGE_DEPARTMENT_MST ";
        $query .= " WHERE ";
        $query .= "     SCHOOL_CD = '{$model->school_cd}' AND ";
        $query .= "     FACULTYCD = '{$model->facultycd}' AND ";
        $query .= "     DEPARTMENTCD = '{$model->departmentcd}' ";
        
        return $query;
    }

    //住所
    public function getCollegeAddrCd($Row)
    {
        $query  = " SELECT ";
        $query .= "     CAMPUS_ADDR_CD AS LABEL, ";
        $query .= "     CAMPUS_ADDR_CD AS VALUE ";
        $query .= " FROM ";
        $query .= "     COLLEGE_CAMPUS_ADDR_DAT ";
        $query .= " WHERE ";
        $query .= "     SCHOOL_CD = '{$Row["SCHOOL_CD"]}' ";

        return $query;
    }

/******************************************************************************************/
/******************************************************************************************/
/*******                *******************************************************************/
/******* 検索画面で使う *******************************************************************/
/*******                *******************************************************************/
/******************************************************************************************/
/******************************************************************************************/
    //学校系列
    public function getSchoolGroup()
    {
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

    //検索結果
    public function getSchoolList($model)
    {
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
            $query .= "     AND SCHOOL_NAME LIKE '%{$model->field["SCHOOL_NAME"]}%' ";
        }
        if ($model->field["SCHOOL_GROUP"] != '') {
            $query .= "     AND SCHOOL_GROUP = '{$model->field["SCHOOL_GROUP"]}' ";
        }
        $query .= " ORDER BY ";
        $query .= "     T1.SCHOOL_CD ";

        return $query;
    }






/******************************************************************************************/
/******************************************************************************************/
/*******              *********************************************************************/
/******* 更新時に使う *********************************************************************/
/*******              *********************************************************************/
/******************************************************************************************/
/******************************************************************************************/
    //学部マスタから取得
    public function getCollegeDepartmentMst_update($school_cd, $facultycd, $departmentcd)
    {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     COLLEGE_DEPARTMENT_MST ";
        $query .= " WHERE ";
        $query .= "     SCHOOL_CD = '{$school_cd}' AND ";
        $query .= "     FACULTYCD = '{$facultycd}' AND ";
        $query .= "     DEPARTMENTCD = '{$departmentcd}' ";

        return $query;
    }

    //INSERT
    public function &getInsertQuery($model)
    {
        $db = Query::dbCheckOut();

        $data = array();
        $data["SCHOOL_CD"][TEXT]            = $model->school_cd;
        $data["FACULTYCD"][TEXT]            = $model->facultycd;
        $data["DEPARTMENTCD"][TEXT]         = $model->field["DEPARTMENTCD"];
        $data["DEPARTMENTNAME"][TEXT]       = $model->field["DEPARTMENTNAME"];
        $data["DEPARTMENTNAME_SHOW1"][TEXT] = $model->field["DEPARTMENTNAME_SHOW1"];
        $data["DEPARTMENTNAME_SHOW2"][TEXT] = $model->field["DEPARTMENTNAME_SHOW2"];
        if ($model->Properties["Internal_Recommendation"] == '1') {
            $data["CAMPUS_DEPARTMENTCD"][TEXT] = $model->field["CAMPUS_DEPARTMENTCD"];
        }

        $data["REGISTERCD"][TEXT]        = STAFFCD;
        $data["UPDATED"][NUMBER]         = "sysdate()";

        $query = Query::insertSQL($data, "COLLEGE_DEPARTMENT_MST");

        $db->query($query);

        Query::dbCheckIn($db);
        return;
    }

    //UPDATE
    public function &getUpdateQuery($model)
    {
        $db = Query::dbCheckOut();

        $data = array();
        $data["DEPARTMENTNAME"][TEXT]       = $model->field["DEPARTMENTNAME"];
        $data["DEPARTMENTNAME_SHOW1"][TEXT] = $model->field["DEPARTMENTNAME_SHOW1"];
        $data["DEPARTMENTNAME_SHOW2"][TEXT] = $model->field["DEPARTMENTNAME_SHOW2"];
        if ($model->Properties["Internal_Recommendation"] == '1') {
            $data["CAMPUS_DEPARTMENTCD"][TEXT] = $model->field["CAMPUS_DEPARTMENTCD"];
        }

        $data["REGISTERCD"][TEXT]           = STAFFCD;
        $data["UPDATED"][NUMBER]            = "sysdate()";

        $where  = " WHERE ";
        $where .= "         SCHOOL_CD       = '{$model->field["tmp_SCHOOL_CD"]}' ";
        $where .= "     AND FACULTYCD       = '{$model->field["tmp_FACULTYCD"]}' ";
        $where .= "     AND DEPARTMENTCD    = '{$model->field["DEPARTMENTCD"]}' ";

        $query = Query::updateSQL($data, "COLLEGE_DEPARTMENT_MST", $where);

        $db->query($query);
        Query::dbCheckIn($db);
        return;
    }

    //DELETE
    public function &getDeleteQuery($model)
    {
        $db = Query::dbCheckOut();

        $query  = " DELETE FROM ";
        $query .= "     COLLEGE_DEPARTMENT_MST ";
        $query .= " WHERE ";
        $query .= "         SCHOOL_CD       = '{$model->field["tmp_SCHOOL_CD"]}' ";
        $query .= "     AND FACULTYCD       = '{$model->field["tmp_FACULTYCD"]}' ";
        $query .= "     AND DEPARTMENTCD    = '{$model->field["DEPARTMENTCD"]}' ";

        $db->query($query);
        Query::dbCheckIn($db);

        return $result;
    }
}
