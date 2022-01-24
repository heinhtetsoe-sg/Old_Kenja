<?php

require_once('for_php7.php');

class knjz410aQuery extends Query {
    //１レコード取得(右フレームでしよう)
    function getCollegeMst($model) {
        $db = Query::dbCheckOut();

        $query  = " SELECT ";
        $query .= "     T1.SCHOOL_CD, ";
        $query .= "     T1.SCHOOL_NAME, ";
        $query .= "     T1.SCHOOL_NAME_SHOW1, ";
        $query .= "     T1.SCHOOL_NAME_SHOW2, ";
        $query .= "     T1.SCHOOL_GROUP, ";
        $query .= "     T1.BUNAME, ";
        $query .= "     T1.KANAME, ";
        $query .= "     T1.SCHOOL_SORT, ";
        $query .= "     T1.BUNYA, ";
        $query .= "     T1.AREA_NAME, ";
        $query .= "     T1.CAMPUS_ADDR_CD, ";
        $query .= "     L1.PREF_CD, ";
        $query .= "     L1.ZIPCD, ";
        $query .= "     L1.ADDR1, ";
        $query .= "     L1.ADDR2, ";
        $query .= "     L1.TELNO, ";
        $query .= "     T1.GREDES, ";
        $query .= "     T1.BASE_SCORE, ";
        $query .= "     T1.NECESSARY_SCORE ";
        $query .= " FROM ";
        $query .= "     COLLEGE_MST T1 ";
        $query .= " LEFT JOIN ";
        $query .= "     COLLEGE_CAMPUS_ADDR_DAT L1 ON  L1.SCHOOL_CD = T1.SCHOOL_CD ";
        $query .= "                            AND L1.CAMPUS_ADDR_CD = T1.CAMPUS_ADDR_CD ";
        $query .= " WHERE ";
        $query .= "     T1.SCHOOL_CD = '{$model->school_cd}' ";

        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);
        return $row;
    }

    //１レコード取得(更新時のチェック)
    function getCollegeMst_update($cd) {
        $db = Query::dbCheckOut();

        $query = "SELECT * FROM COLLEGE_MST ".
                 " WHERE SCHOOL_CD = '" .$cd ."'";

        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);
        return $row;
    }

    //大学一覧取得
    function getList() {
        $query .= " SELECT ";
        $query .= "     SCHOOL_CD, ";
        $query .= "     SCHOOL_NAME, ";
        $query .= "     SCHOOL_GROUP || ':' || T4.NAME1 AS SCHOOL_GROUP, ";
        $query .= "     BUNAME, ";
        $query .= "     KANAME, ";
        $query .= "     SCHOOL_SORT || ':' || T2.NAME1 AS SCHOOL_SORT, ";
        $query .= "     BUNYA || ':' || T3.NAME1 AS BUNYA, ";
        $query .= "     AREA_NAME, ";
        $query .= "     ZIPCD, ";
        $query .= "     ADDR1, ";
        $query .= "     ADDR2, ";
        $query .= "     TELNO, ";
        $query .= "     GREDES, ";
        $query .= "     SCHOOL_NAME_SHOW1,  ";
        $query .= "     SCHOOL_NAME_SHOW2, ";
        $query .= "     BASE_SCORE, ";
        $query .= "     NECESSARY_SCORE ";
        $query .= " FROM ";
        $query .= "     COLLEGE_MST  ";
        $query .= "     LEFT JOIN V_NAME_MST T2 ON T2.YEAR = '".CTRL_YEAR."' ";
        $query .= "          AND T2.NAMECD1 = 'E001' ";
        $query .= "          AND T2.NAMECD2 = SCHOOL_SORT  ";
        $query .= "     LEFT JOIN V_NAME_MST T3 ON T3.YEAR = '".CTRL_YEAR."' ";
        $query .= "          AND T3.NAMECD1 = CASE WHEN T2.NAMESPARE2 IS NOT NULL  ";
        $query .= "                                THEN T2.NAMESPARE2 ";
        $query .= "                                ELSE 'E009' ";
        $query .= "                           END ";
        $query .= "          AND T3.NAMECD2 = BUNYA ";
        $query .= "     LEFT JOIN V_NAME_MST T4 ON T4.YEAR = '".CTRL_YEAR."' ";
        $query .= "          AND T4.NAMECD1 = 'E012' ";
        $query .= "          AND T4.NAMECD2 = SCHOOL_GROUP ";
        $query .= " ORDER BY ";
        $query .= "     SCHOOL_CD ";

        return $query;
    }

    //学校系列の取得
    function getSchool_group() {
        $query  = " SELECT NAMECD2 AS VALUE,NAMECD2 || ':' || NAME1 AS LABEL ";
        $query .= " FROM V_NAME_MST ";
        $query .= " WHERE YEAR='".CTRL_YEAR."' ";
        $query .= "       AND NAMECD1='E012' ";
        $query .= " ORDER BY NAMECD2";

        return $query;
    }

    //学校の取得
    function getSchoolcd(){

        $query  = " SELECT ";
        $query .= "     NAMECD2 AS VALUE, ";
        $query .= "     NAMECD2 || ':' || NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND NAMECD1 = 'E001' ";
        $query .= "     AND NAMECD2 in ('01','02','03','04') ";
        $query .= " UNION ";
        $query .= " SELECT ";
        $query .= "     NAMECD2 AS VALUE, ";
        $query .= "     NAMECD2 || ':' || NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND NAMECD1 = 'E001' ";
        $query .= "     AND NAMESPARE1 IS NOT NULL ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //分野用NAMECD2取得
    function getBunyaNamecd2($nameCd2) {

        $query  = " SELECT ";
        $query .= "     NAMESPARE2 ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = 'E001' ";
        $query .= "     AND NAMECD2 = '{$nameCd2}' ";

        return $query;
    }

    //分野の取得
    function getBunya($nameCd1){
        $query  = " SELECT ";
        $query .= "     NAMECD2 AS VALUE, ";
        $query .= "     NAMECD2 || ':' || NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND NAMECD1 = '{$nameCd1}' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //住所
    function getCollegeAddrCd($Row) {
        $query  = " SELECT ";
        $query .= "     CAMPUS_ADDR_CD AS LABEL, ";
        $query .= "     CAMPUS_ADDR_CD AS VALUE ";
        $query .= " FROM ";
        $query .= "     COLLEGE_CAMPUS_ADDR_DAT ";
        $query .= " WHERE ";
        $query .= "     SCHOOL_CD = '{$Row["SCHOOL_CD"]}' ";

        return $query;
    }

    function getCollegeCampusAddrDat($school_cd, $campus_addr_cd) {
        $query  = " SELECT ";
        $query .= "     T1.*, ";
        $query .= "     L1.PREF_NAME ";
        $query .= " FROM ";
        $query .= "     COLLEGE_CAMPUS_ADDR_DAT T1 ";
        $query .= " LEFT JOIN ";
        $query .= "     PREF_MST L1 ON L1.PREF_CD = T1.PREF_CD ";
        $query .= " WHERE ";
        $query .= "     T1.SCHOOL_CD = '{$school_cd}' AND ";
        $query .= "     T1.CAMPUS_ADDR_CD = '{$campus_addr_cd}' ";

        return $query;
    }

    //INSERT
    function &getInsertQuery($fields, $collegeCampusAddrDat) {
        $db = Query::dbCheckOut();

        $data["SCHOOL_CD"][TEXT]         = $fields["SCHOOL_CD"];
        $data["SCHOOL_NAME"][TEXT]       = $fields["SCHOOL_NAME"];
        $data["SCHOOL_GROUP"][TEXT]      = $fields["SCHOOL_GROUP"];
        $data["SCHOOL_SORT"][TEXT]       = $fields["SCHOOL_SORT"];
        $data["BUNYA"][TEXT]             = $fields["BUNYA"];
        $data["AREA_NAME"][TEXT]         = $fields["AREA_NAME"];
        $data["CAMPUS_ADDR_CD"][TEXT]    = $fields["CAMPUS_ADDR_CD"];
        $data["ZIPCD"][TEXT]             = $collegeCampusAddrDat["ZIPCD"];
        $data["ADDR1"][TEXT]             = $collegeCampusAddrDat["ADDR1"];
        $data["ADDR2"][TEXT]             = $collegeCampusAddrDat["ADDR2"];
        $data["TELNO"][TEXT]             = $collegeCampusAddrDat["TELNO"];
        $data["GREDES"][TEXT]            = $fields["GREDES"];
        $data["SCHOOL_NAME_SHOW1"][TEXT] = $fields["SCHOOL_NAME_SHOW1"];
        $data["SCHOOL_NAME_SHOW2"][TEXT] = $fields["SCHOOL_NAME_SHOW2"];
        $data["BASE_SCORE"][TEXT]        = $fields["BASE_SCORE"];
        $data["NECESSARY_SCORE"][TEXT]   = $fields["NECESSARY_SCORE"];

        $data["REGISTERCD"][TEXT]  = STAFFCD;
        $data["UPDATED"][NUMBER]   = "sysdate()";

        $query = Query::insertSQL($data, "COLLEGE_MST");

        $db->query($query);

        Query::dbCheckIn($db);
        return;
    }

    //UPDATE
    function &getUpdateQuery($fields, $collegeCampusAddrDat)
    {
        $db = Query::dbCheckOut();

        $data["SCHOOL_CD"][TEXT]         = $fields["SCHOOL_CD"];
        $data["SCHOOL_NAME"][TEXT]       = $fields["SCHOOL_NAME"];
        $data["SCHOOL_GROUP"][TEXT]      = $fields["SCHOOL_GROUP"];
        $data["SCHOOL_SORT"][TEXT]       = $fields["SCHOOL_SORT"];
        $data["BUNYA"][TEXT]             = $fields["BUNYA"];
        $data["AREA_NAME"][TEXT]         = $fields["AREA_NAME"];
        $data["CAMPUS_ADDR_CD"][TEXT]    = $fields["CAMPUS_ADDR_CD"];
        $data["ZIPCD"][TEXT]             = $collegeCampusAddrDat["ZIPCD"];
        $data["ADDR1"][TEXT]             = $collegeCampusAddrDat["ADDR1"];
        $data["ADDR2"][TEXT]             = $collegeCampusAddrDat["ADDR2"];
        $data["TELNO"][TEXT]             = $collegeCampusAddrDat["TELNO"];
        $data["GREDES"][TEXT]            = $fields["GREDES"];
        $data["SCHOOL_NAME_SHOW1"][TEXT] = $fields["SCHOOL_NAME_SHOW1"];
        $data["SCHOOL_NAME_SHOW2"][TEXT] = $fields["SCHOOL_NAME_SHOW2"];
        $data["BASE_SCORE"][TEXT]        = $fields["BASE_SCORE"];
        $data["NECESSARY_SCORE"][TEXT]   = $fields["NECESSARY_SCORE"];
        $data["REGISTERCD"][TEXT]  = STAFFCD;
        $data["UPDATED"][NUMBER]   = "sysdate()";
        $where = " WHERE SCHOOL_CD = '" .$fields["SCHOOL_CD"]."'";

        $query = Query::updateSQL($data, "COLLEGE_MST", $where);

        $db->query($query);
        Query::dbCheckIn($db);
        return;
    }

    //DELETE
    function &getDeleteQuery($fields)
    {
        $db = Query::dbCheckOut();

        $query = "DELETE FROM COLLEGE_MST WHERE SCHOOL_CD  = '" .$fields["SCHOOL_CD"] ."'";

        $db->query($query);
        Query::dbCheckIn($db);

        return $result;
    }


/*****************************************************************************************************/
/*****************************************************************************************************/
/*********************************************          **********************************************/
/********************************************* 住所関係 **********************************************/
/*********************************************          **********************************************/
/*****************************************************************************************************/
/*****************************************************************************************************/
    //住所一覧
    function getListAddr($school_cd) {
        $query .= " SELECT ";
        $query .= "     T1.SCHOOL_CD, ";
        $query .= "     L1.SCHOOL_NAME, ";
        $query .= "     T1.CAMPUS_ADDR_CD, ";
        $query .= "     T1.PREF_CD, ";
        $query .= "     L2.PREF_NAME, ";
        $query .= "     T1.ZIPCD, ";
        $query .= "     T1.ADDR1, ";
        $query .= "     T1.ADDR2, ";
        $query .= "     T1.TELNO ";
        $query .= " FROM ";
        $query .= "     COLLEGE_CAMPUS_ADDR_DAT T1 ";
        $query .= " LEFT JOIN ";
        $query .= "     COLLEGE_MST L1 ON L1.SCHOOL_CD = T1.SCHOOL_CD ";
        $query .= " LEFT JOIN ";
        $query .= "     PREF_MST L2 ON L2.PREF_CD = T1.PREF_CD ";
        $query .= " WHERE ";
        $query .= "     T1.SCHOOL_CD = '{$school_cd}' ";
        $query .= " ORDER BY ";
        $query .= "     T1.SCHOOL_CD, ";
        $query .= "     T1.CAMPUS_ADDR_CD ";

        return $query;
    }


    //都道府県
    function getPrefMst() {
        $query .= " SELECT ";
        $query .= "     PREF_CD || ' ' || PREF_NAME AS LABEL, ";
        $query .= "     PREF_CD AS VALUE ";
        $query .= " FROM ";
        $query .= "     PREF_MST ";

        return $query;
    }

    //DELETE
    function getDeleteCampusQuery($model) {
        $db = Query::dbCheckOut();

        $query .= " DELETE FROM ";
        $query .= "     COLLEGE_CAMPUS_ADDR_DAT ";
        $query .= " WHERE ";
        $query .= "     SCHOOL_CD = '{$model->school_cd}' AND ";
        $query .= "     CAMPUS_ADDR_CD = '{$model->field["CAMPUS_ADDR_CD"]}' ";

        $db->query($query);
        Query::dbCheckIn($db);

        return $result;
    }

    function getInsertCampusQuery($model) {
        $db = Query::dbCheckOut();

        $data["SCHOOL_CD"][TEXT]      = $model->school_cd;
        $data["CAMPUS_ADDR_CD"][TEXT] = $model->field["CAMPUS_ADDR_CD"];
        $data["PREF_CD"][TEXT]        = $model->field["PREF_CD"];
        $data["ZIPCD"][TEXT]          = $model->field["ZIPCD"];
        $data["ADDR1"][TEXT]          = $model->field["ADDR1"];
        $data["ADDR2"][TEXT]          = $model->field["ADDR2"];
        $data["TELNO"][TEXT]          = $model->field["TELNO"];
        $data["REGISTERCD"][TEXT]     = STAFFCD;
        $data["UPDATED"][NUMBER]      = "sysdate()";

        $query = Query::insertSQL($data, "COLLEGE_CAMPUS_ADDR_DAT");

        $db->query($query);

        Query::dbCheckIn($db);
        return;
    }

    function getUpdateCampusQuery($model) {
        $db = Query::dbCheckOut();

        $data["SCHOOL_CD"][TEXT]      = $model->school_cd;
        $data["CAMPUS_ADDR_CD"][TEXT] = $model->field["CAMPUS_ADDR_CD"];
        $data["PREF_CD"][TEXT]        = $model->field["PREF_CD"];
        $data["ZIPCD"][TEXT]          = $model->field["ZIPCD"];
        $data["ADDR1"][TEXT]          = $model->field["ADDR1"];
        $data["ADDR2"][TEXT]          = $model->field["ADDR2"];
        $data["TELNO"][TEXT]          = $model->field["TELNO"];
        $data["REGISTERCD"][TEXT]     = STAFFCD;
        $data["UPDATED"][NUMBER]      = "sysdate()";

        $where = " WHERE SCHOOL_CD = '{$model->school_cd}' AND CAMPUS_ADDR_CD = '{$model->field["CAMPUS_ADDR_CD"]}' ";

        $query = Query::updateSQL($data, "COLLEGE_CAMPUS_ADDR_DAT", $where);

        $db->query($query);
        Query::dbCheckIn($db);
        return;
    }
}
?>
