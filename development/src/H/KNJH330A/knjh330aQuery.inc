<?php

require_once('for_php7.php');

class knjh330aQuery extends Query {

    //年度
    function getYear($div = "MOCKYEAR") {
        $query  = " WITH CTRL_T (LABEL, VALUE) AS ( ";
        $query .= "     VALUES('".CTRL_YEAR."', '".CTRL_YEAR."') ";
        $query .= " ) ";

        $query .= " SELECT DISTINCT ";
        $query .= "     SUBSTR(MOCKCD,2,4) AS LABEL, ";
        $query .= "     SUBSTR(MOCKCD,2,4) AS VALUE ";
        $query .= " FROM ";
        $query .= "     MOCK_MST ";
        if ($div == "MOCKYEAR") {
            $query .= " UNION ";
            $query .= " SELECT ";
            $query .= "     * ";
            $query .= " FROM ";
            $query .= "     CTRL_T ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //業者コード
    function getCompanycd($model) {
        $query  = " SELECT ";
        $query .= "     NAMESPARE1 AS VALUE, ";
        $query .= "     NAMESPARE1 || ':' || NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$model->mockyear."' ";
        $query .= "     AND NAMECD1 = 'H313' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //学年
    function getGrade($model) {
        
        $query  = " SELECT ";
        //駿台甲府学園用
        if($model->CHECK_SCHOOL == 'sundaikoufu'){
            $query .= "     GRADE_CD AS VALUE, ";
            $query .= "     GRADE_NAME2 AS LABEL ";
        } else {
            $query .= "     GRADE AS VALUE, ";
            $query .= "     GRADE_NAME1 AS LABEL ";
        }
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_GDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$model->mockyear."' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= " AND SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
            }
        } else if($model->Properties["useSchool_KindField"] == "1"){
            $query .= " AND ";
            $query .= "     SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //試験種別
    function getMosiDiv($model) {
        $query  = " SELECT ";
        $query .= "     NAMECD2 AS VALUE, ";
        $query .= "     NAMECD2 || ':' || NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$model->mockyear."' ";
        $query .= "     AND NAMECD1 = 'H314' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //模試種別
    function getMockDiv($model)
    {
        $query  = " SELECT ";
        $query .= "     NAMECD2 AS VALUE, ";
        $query .= "     NAMECD2 || ':' || NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$model->mockyear."' ";
        $query .= "     AND NAMECD1 = 'H308' ";
        $query .= "     AND NAMESPARE2 = '1' ";
        $query .= " ORDER BY ";
        $query .= "     NAMECD2 ";

        return $query;
    }

    //リスト
    function getList($model)
    {
        $query  = " SELECT * FROM MOCK_MST ";
        $query .= " WHERE SUBSTR(MOCKCD,2,4) = '".$model->mockyear."'";
        $query .= " ORDER BY MOCKCD ";
        return $query;

    }

    //１レコード取得
    function getRow($cd)
    {
        $db = Query::dbCheckOut();

        $query = "SELECT * FROM MOCK_MST ".
                 " WHERE MOCKCD = '" .$cd ."'";

        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);
        return $row;
    }

    //--- INSERT
    function &getInsertQuery($model, $fields)
    {
        $db = Query::dbCheckOut();

        $data["MOCKCD"][TEXT]         = $fields["MOCK_DIV"].$model->mockyear.substr($fields["COMPANYCD"],-1).substr($fields["GRADE"],-1).$fields["MOCKCD"];
        $data["MOCKNAME1"][TEXT]      = $fields["MOCKNAME1"];
        $data["MOCKNAME2"][TEXT]      = $fields["MOCKNAME2"];
        $data["MOCKNAME3"][TEXT]      = $fields["MOCKNAME3"];
        $data["COMPANYCD"][TEXT]            = $fields["COMPANYCD"];
        $data["COMPANYMOSI_CD"][TEXT]       = $fields["COMPANYMOSI_CD"];
        $data["TUUCHIHYOU_MOSI_NAME"][TEXT] = $fields["TUUCHIHYOU_MOSI_NAME"];
        $data["SINROSIDOU_MOSI_NAME"][TEXT] = $fields["SINROSIDOU_MOSI_NAME"];
        $data["MOSI_DIV"][TEXT]             = $fields["MOSI_DIV"];
        $data["MOSI_DATE"][TEXT]            = $fields["MOSI_DATE"];
        $data["FILE_NAME"][TEXT]            = $fields["FILE_NAME"];
        $data["REGISTERCD"][TEXT]     = STAFFCD;
        $data["UPDATED"][NUMBER]	  = "sysdate()";

        $query = Query::insertSQL($data, "MOCK_MST");
        $db->query($query);

        Query::dbCheckIn($db);
        return;
    }

    //--- UPDATE 
    function &getUpdateQuery($model, $fields)
    {
        $db = Query::dbCheckOut();

        $data["MOCKCD"][TEXT]         = $fields["MOCK_DIV"].$model->mockyear.substr($fields["COMPANYCD"],-1).substr($fields["GRADE"],-1).$fields["MOCKCD"];
        $data["MOCKNAME1"][TEXT]      = $fields["MOCKNAME1"];
        $data["MOCKNAME2"][TEXT]      = $fields["MOCKNAME2"];
        $data["MOCKNAME3"][TEXT]      = $fields["MOCKNAME3"];
        $data["COMPANYCD"][TEXT]            = $fields["COMPANYCD"];
        $data["COMPANYMOSI_CD"][TEXT]       = $fields["COMPANYMOSI_CD"];
        $data["TUUCHIHYOU_MOSI_NAME"][TEXT] = $fields["TUUCHIHYOU_MOSI_NAME"];
        $data["SINROSIDOU_MOSI_NAME"][TEXT] = $fields["SINROSIDOU_MOSI_NAME"];
        $data["MOSI_DIV"][TEXT]             = $fields["MOSI_DIV"];
        $data["MOSI_DATE"][TEXT]            = $fields["MOSI_DATE"];
        $data["FILE_NAME"][TEXT]            = $fields["FILE_NAME"];
        $data["REGISTERCD"][TEXT]     = STAFFCD;
        $data["UPDATED"][NUMBER]      = "sysdate()";

        $where = " WHERE MOCKCD = '" .$fields["MOCK_DIV"].$model->mockyear.$model->company.$model->grade.$fields["MOCKCD"]."'";

        $query = Query::updateSQL($data, "MOCK_MST", $where);
        $db->query($query);
        Query::dbCheckIn($db);
        return;
    }

    //DELETE
    function &getDeleteQuery($model, $fields)
    {
        $db = Query::dbCheckOut();

        $query = "DELETE FROM MOCK_MST "
                ." WHERE MOCKCD  = '" .$fields["MOCK_DIV"].$model->mockyear.$model->company.$model->grade.$fields["MOCKCD"]."'";

        $db->query($query);
        Query::dbCheckIn($db);

        return $result;
    }

    //コピーチェック
    function getCopyCheck($model) {
        $query  = " SELECT COUNT(*) FROM MOCK_MST ";
        $query .= " WHERE SUBSTR(MOCKCD,2,4) = '".$model->mockyear."'";
        return $query;

    }

    //コピー処理
    function getCopyQuery($model) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $query  = "INSERT INTO MOCK_MST ";
        $query .= "( ";
        $query .= "    MOCKCD, ";
        $query .= "    MOCKNAME1, ";
        $query .= "    MOCKNAME2, ";
        $query .= "    MOCKNAME3, ";
        $query .= "    COMPANYCD, ";
        $query .= "    COMPANYMOSI_CD, ";
        $query .= "    TUUCHIHYOU_MOSI_NAME, ";
        $query .= "    SINROSIDOU_MOSI_NAME, ";
        $query .= "    MOSI_DIV, ";
        $query .= "    MOSI_DATE, ";
        $query .= "    FILE_NAME, ";
        $query .= "    REGISTERCD, ";
        $query .= "    UPDATED ";
        $query .= ") ";
        $query .= "( ";
        $query .= "SELECT ";
        $query .= "    SUBSTR(MOCKCD,1,1) || '".$model->mockyear."' || SUBSTR(MOCKCD,6,4) AS MOCKCD, ";
        $query .= "    MOCKNAME1, ";
        $query .= "    MOCKNAME2, ";
        $query .= "    MOCKNAME3, ";
        $query .= "    COMPANYCD, ";
        $query .= "    COMPANYMOSI_CD, ";
        $query .= "    TUUCHIHYOU_MOSI_NAME, ";
        $query .= "    SINROSIDOU_MOSI_NAME, ";
        $query .= "    MOSI_DIV, ";
        $query .= "    MOSI_DATE, ";
        $query .= "    FILE_NAME, ";
        $query .= "    '".STAFFCD."' AS REGISTERCD, ";
        $query .= "    SYSDATE() AS UPDATED ";
        $query .= "FROM ";
        $query .= "    MOCK_MST ";
        $query .= "WHERE ";
        $query .= "    SUBSTR(MOCKCD,2,4) = '".$model->copyyear."' ";
        $query .= ") ";

        $db->query($query);
        $db->commit();
        Query::dbCheckIn($db);

        return;
    }


    //学校名取得
    function getCheck_School()
    {
        $query  = " SELECT ";
        $query .= "     NAME1 ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = 'Z010' ";
        $query .= "     AND NAMECD2 = '00' ";
        return $query;

    }



}
?>
