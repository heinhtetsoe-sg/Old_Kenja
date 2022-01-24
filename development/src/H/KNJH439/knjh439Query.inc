<?php

require_once('for_php7.php');

class knjh439Query extends Query {

    //年度
    function getYear($div = "MOCKYEAR") {
        if ($div == "MOCKYEAR") {
            $query .= " SELECT DISTINCT ";
            $query .= "     SUBSTR(MOCKCD,2,4) AS LABEL, ";
            $query .= "     SUBSTR(MOCKCD,2,4) AS VALUE ";
            $query .= " FROM ";
            $query .= "     MOCK_MST ";
            $query .= " ORDER BY ";
            $query .= "     VALUE ";
        } else {
            $query .= " SELECT DISTINCT ";
            $query .= "     YEAR AS LABEL, ";
            $query .= "     YEAR AS VALUE ";
            $query .= " FROM ";
            $query .= "     MOCK_CSV_HOPE_FIELD_DAT ";
            $query .= " WHERE ";
            $query .= "     YEAR = SUBSTR(MOCKCD,2,4) ";
            $query .= " ORDER BY ";
            $query .= "     VALUE ";
        }

        return $query;
    }

    //模試データ
    function getMockcd($model) {
        $query  = " SELECT ";
        $query .= "     MOCKCD AS VALUE, ";
        $query .= "     MOCKCD || ':' || MOCKNAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     MOCK_MST ";
        $query .= " WHERE ";
        $query .= "     SUBSTR(MOCKCD,2,4) = '".$model->mockyear."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }


    //リスト
    function getList($model) {
        $query  = " SELECT ";
        $query .= "     T1.MOCKCD, ";
        $query .= "     T1.GRADE, ";
        $query .= "     T1.FIELD_CNT as H_FIELD_CNT,";
        $query .= "     T2.FIELD_CNT as K_FIELD_CNT,";
        $query .= "     L1.MOCKNAME1, ";
        //駿台甲府学園用
        if($model->CHECK_SCHOOL == 'sundaikoufu'){
            $query .= "     L2.GRADE_NAME2 AS GRADE_NAME1";
        } else {
            $query .= "     L2.GRADE_NAME1 ";
        }
        $query .= " FROM ";
        $query .= "     MOCK_CSV_HOPE_FIELD_DAT T1 ";
        $query .= "     LEFT JOIN MOCK_MST L1 ON L1.MOCKCD = T1.MOCKCD ";
        //駿台甲府学園用
        if($model->CHECK_SCHOOL == 'sundaikoufu'){
            $query .= "     LEFT JOIN SCHREG_REGD_GDAT L2 ON L2.YEAR = T1.YEAR AND L2.GRADE_CD = T1.GRADE ";
        } else {
            $query .= "     LEFT JOIN SCHREG_REGD_GDAT L2 ON L2.YEAR = T1.YEAR AND L2.GRADE = T1.GRADE ";
        }
        $query .= "     LEFT JOIN MOCK_CSV_SCORE_FIELD_DAT T2 ON T1.MOCKCD = T2.MOCKCD AND T1.GRADE = T2.GRADE ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".$model->mockyear."' ";
        $query .= "     AND SUBSTR(T1.MOCKCD,2,4) = '".$model->mockyear."'";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= " AND SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
            }
        } else if($model->Properties["useSchool_KindField"] == "1"){
            $query .= " AND ";
            $query .= "     SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     T1.MOCKCD, ";
        $query .= "     T1.GRADE ";

        return $query;
    }

    //１レコード取得
    function getRow($year, $mockcd, $grade) {
        $db = Query::dbCheckOut();

        $query  = " SELECT ";
        $query .= "    t1.YEAR, ";
        $query .= "    t1.MOCKCD, ";
        $query .= "    t1.GRADE, ";
        $query .= "    t1.FIELD_CNT as H_FIELD_CNT, ";
        $query .= "    t2.FIELD_CNT as K_FIELD_CNT ";
        $query .= " FROM ";
        $query .= "    MOCK_CSV_HOPE_FIELD_DAT t1 ";
        $query .= "    left join MOCK_CSV_SCORE_FIELD_DAT t2 on t1.YEAR = t2.YEAR and t1.MOCKCD = t2.MOCKCD and t1.GRADE = t2.GRADE ";
        $query .= " WHERE ";
        $query .= "    t1.YEAR = '".$year."' AND ";
        $query .= "    t1.MOCKCD = '".$mockcd."' AND ";
        $query .= "    t1.GRADE = '".$grade."' ";

        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);
        return $row;
    }

    //--- INSERT
    function &getInsertQuery($model) {
        $db = Query::dbCheckOut();

        $data["YEAR"][TEXT]         = $model->mockyear;
        $data["MOCKCD"][TEXT]       = $model->field["MOCKCD"];
        $data["GRADE"][TEXT]        = $model->field["GRADE"];
        $data["FIELD_CNT"][NUMBER]  = $model->field["H_FIELD_CNT"];
        $data["REGISTERCD"][TEXT]   = STAFFCD;
        $data["UPDATED"][NUMBER]    = "sysdate()";

        $query = Query::insertSQL($data, "MOCK_CSV_HOPE_FIELD_DAT");
        $db->query($query);
        
        $data["YEAR"][TEXT]         = $model->mockyear;
        $data["MOCKCD"][TEXT]       = $model->field["MOCKCD"];
        $data["GRADE"][TEXT]        = $model->field["GRADE"];
        $data["FIELD_CNT"][NUMBER]  = $model->field["K_FIELD_CNT"];
        $data["REGISTERCD"][TEXT]   = STAFFCD;
        $data["UPDATED"][NUMBER]    = "sysdate()";

        $query = Query::insertSQL($data, "MOCK_CSV_SCORE_FIELD_DAT");
        $db->query($query);

        Query::dbCheckIn($db);
        return;
    }

    //--- UPDATE 
    function &getUpdateQuery($model) {
        $db = Query::dbCheckOut();

        $data["YEAR"][TEXT]         = $model->mockyear;
        $data["MOCKCD"][TEXT]       = $model->field["MOCKCD"];
        $data["GRADE"][TEXT]        = $model->field["GRADE"];
        $data["FIELD_CNT"][NUMBER]  = $model->field["H_FIELD_CNT"];
        $data["REGISTERCD"][TEXT]   = STAFFCD;
        $data["UPDATED"][NUMBER]    = "sysdate()";

        $where  = " WHERE YEAR      = '".$model->mockyear."' ";
        $where .= "   AND MOCKCD    = '".$model->field["MOCKCD"]."' ";
        $where .= "   AND GRADE     = '".$model->field["GRADE"]."' ";

        $query = Query::updateSQL($data, "MOCK_CSV_HOPE_FIELD_DAT", $where);
        $db->query($query);

        $data["YEAR"][TEXT]         = $model->mockyear;
        $data["MOCKCD"][TEXT]       = $model->field["MOCKCD"];
        $data["GRADE"][TEXT]        = $model->field["GRADE"];
        $data["FIELD_CNT"][NUMBER]  = $model->field["K_FIELD_CNT"];
        $data["REGISTERCD"][TEXT]   = STAFFCD;
        $data["UPDATED"][NUMBER]    = "sysdate()";

        $where  = " WHERE YEAR      = '".$model->mockyear."' ";
        $where .= "   AND MOCKCD    = '".$model->field["MOCKCD"]."' ";
        $where .= "   AND GRADE     = '".$model->field["GRADE"]."' ";

        $query = Query::updateSQL($data, "MOCK_CSV_SCORE_FIELD_DAT", $where);
        $db->query($query);
        Query::dbCheckIn($db);
        return;
    }

    //DELETE
    function &getDeleteQuery($model) {
        $db = Query::dbCheckOut();

        $query  = "DELETE FROM MOCK_CSV_HOPE_FIELD_DAT ";
        $query .= " WHERE YEAR      = '".$model->mockyear."' ";
        $query .= "   AND MOCKCD    = '".$model->field["MOCKCD"]."' ";
        $query .= "   AND GRADE     = '".$model->field["GRADE"]."' ";

        $db->query($query);

        $query  = "DELETE FROM MOCK_CSV_SCORE_FIELD_DAT ";
        $query .= " WHERE YEAR      = '".$model->mockyear."' ";
        $query .= "   AND MOCKCD    = '".$model->field["MOCKCD"]."' ";
        $query .= "   AND GRADE     = '".$model->field["GRADE"]."' ";

        $db->query($query);
        Query::dbCheckIn($db);

        return $result;
    }

    //コピーチェック
    function getCopyCheck($model) {
        $query  = " SELECT COUNT(*) FROM MOCK_CSV_HOPE_FIELD_DAT ";
        $query .= " WHERE YEAR = '".$model->mockyear."' ";
        $query .= "   AND SUBSTR(MOCKCD,2,4) = '".$model->mockyear."'";
        return $query;

    }

    //コピー処理
    function getCopyQuery($model) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $query  = "INSERT INTO MOCK_CSV_HOPE_FIELD_DAT ";
        $query .= "( ";
        $query .= "    YEAR, ";
        $query .= "    MOCKCD, ";
        $query .= "    GRADE, ";
        $query .= "    FIELD_CNT, ";
        $query .= "    REGISTERCD, ";
        $query .= "    UPDATED ";
        $query .= ") ";
        $query .= "( ";
        $query .= "SELECT ";
        $query .= "    '".$model->mockyear."' AS YEAR, ";
        $query .= "    SUBSTR(MOCKCD,1,1) || '".$model->mockyear."' || SUBSTR(MOCKCD,6,4) AS MOCKCD, ";
        $query .= "    GRADE, ";
        $query .= "    FIELD_CNT, ";
        $query .= "    '".STAFFCD."' AS REGISTERCD, ";
        $query .= "    SYSDATE() AS UPDATED ";
        $query .= "FROM ";
        $query .= "    MOCK_CSV_HOPE_FIELD_DAT ";
        $query .= "WHERE ";
        $query .= "    YEAR = '".$model->copyyear."' ";
        $query .= "    AND SUBSTR(MOCKCD,2,4) = '".$model->copyyear."' ";
        $query .= "    AND SUBSTR(MOCKCD,1,1) || '".$model->mockyear."' || SUBSTR(MOCKCD,6,4) IN (SELECT MOCKCD FROM MOCK_MST) ";
        $query .= ") ";

        $db->query($query);

        $query  = "INSERT INTO MOCK_CSV_SCORE_FIELD_DAT ";
        $query .= "( ";
        $query .= "    YEAR, ";
        $query .= "    MOCKCD, ";
        $query .= "    GRADE, ";
        $query .= "    FIELD_CNT, ";
        $query .= "    REGISTERCD, ";
        $query .= "    UPDATED ";
        $query .= ") ";
        $query .= "( ";
        $query .= "SELECT ";
        $query .= "    '".$model->mockyear."' AS YEAR, ";
        $query .= "    SUBSTR(MOCKCD,1,1) || '".$model->mockyear."' || SUBSTR(MOCKCD,6,4) AS MOCKCD, ";
        $query .= "    GRADE, ";
        $query .= "    FIELD_CNT, ";
        $query .= "    '".STAFFCD."' AS REGISTERCD, ";
        $query .= "    SYSDATE() AS UPDATED ";
        $query .= "FROM ";
        $query .= "    MOCK_CSV_SCORE_FIELD_DAT ";
        $query .= "WHERE ";
        $query .= "    YEAR = '".$model->copyyear."' ";
        $query .= "    AND SUBSTR(MOCKCD,2,4) = '".$model->copyyear."' ";
        $query .= "    AND SUBSTR(MOCKCD,1,1) || '".$model->mockyear."' || SUBSTR(MOCKCD,6,4) IN (SELECT MOCKCD FROM MOCK_MST) ";
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
