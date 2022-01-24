<?php

require_once('for_php7.php');

class knjh440Query extends Query {

    //学年
    function getGrade($model) {
        $query  = " SELECT ";
        $query .= "     GRADE AS VALUE, ";
        $query .= "     GRADE_NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_GDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$model->mockyear}' ";
        $query .= "     AND SCHOOL_KIND = 'H' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //模試リスト
    function getListMock($model) {
        $query  = "";
        $query .= " WITH LEFT_LIST AS ( ";
        $query .= "     SELECT DISTINCT ";
        $query .= "         MOCKCD ";
        $query .= "     FROM ";
        $query .= "         RECORD_MOCK_ORDER_DAT ";
        $query .= "     WHERE ";
        $query .= "             YEAR    = '{$model->mockyear}' ";
        $query .= "         AND GRADE   = '{$model->field["GRADE"]}' ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     T1.MOCKCD AS VALUE, ";
        $query .= "     T1.MOCKCD || ':' || T1.MOCKNAME1 AS LABEL, ";
        $query .= "     L1.MOCKCD AS LEFT_CD ";
        $query .= " FROM ";
        $query .= "     MOCK_MST T1 ";
        $query .= "     LEFT JOIN LEFT_LIST L1 ON L1.MOCKCD = T1.MOCKCD ";
        $query .= " WHERE ";
        $query .= "     SUBSTR(T1.MOCKCD,2,4) = '{$model->mockyear}' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //科目リスト
    function getListSub($model) {
        $query  = "";
        $query .= " WITH LEFT_LIST AS ( ";
        $query .= "     SELECT DISTINCT ";
        $query .= "         CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD AS SUBCLASSCD ";
        $query .= "     FROM ";
        $query .= "         RECORD_MOCK_ORDER_SUB_DAT ";
        $query .= "     WHERE ";
        $query .= "             YEAR    = '{$model->mockyear}' ";
        $query .= "         AND GRADE   = '{$model->field["GRADE"]}' ";
        $query .= " ) , MOCK_LIST AS ( ";
        $query .= "     SELECT DISTINCT ";
        $query .= "         MOCKCD ";
        $query .= "     FROM ";
        $query .= "         RECORD_MOCK_ORDER_DAT ";
        $query .= "     WHERE ";
        $query .= "             YEAR    = '{$model->mockyear}' ";
        $query .= "         AND GRADE   = '{$model->field["GRADE"]}' ";
        $query .= " ) , MAIN_LIST AS ( ";
        $query .= "     SELECT DISTINCT ";
        $query .= "         L3.CLASSCD || '-' || L3.SCHOOL_KIND || '-' || L3.CURRICULUM_CD || '-' || L3.SUBCLASSCD AS SUBCLASSCD, ";
        $query .= "         L3.SUBCLASSNAME ";
        $query .= "     FROM ";
        $query .= "         MOCK_CSV_SUBCLASS_REPLACE_DAT T1 ";
        $query .= "         INNER JOIN MOCK_LIST L1 ON L1.MOCKCD = T1.MOCKCD ";
        $query .= "         INNER JOIN MOCK_SUBCLASS_MST L2 ON L2.MOCK_SUBCLASS_CD = T1.MOCK_SUBCLASS_CD ";
        $query .= "         INNER JOIN SUBCLASS_MST L3 ";
        $query .= "             ON L3.CLASSCD || '-' || L3.SCHOOL_KIND || '-' || L3.CURRICULUM_CD || '-' || L3.SUBCLASSCD ";
        $query .= "              = L2.CLASSCD || '-' || L2.SCHOOL_KIND || '-' || L2.CURRICULUM_CD || '-' || L2.SUBCLASSCD ";
        $query .= "     WHERE ";
        $query .= "             T1.YEAR  = '{$model->mockyear}' ";
        $query .= "         AND T1.GRADE = '{$model->field["GRADE"]}' ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     T1.SUBCLASSCD AS VALUE, ";
        $query .= "     T1.SUBCLASSCD || ':' || T1.SUBCLASSNAME AS LABEL, ";
        $query .= "     L1.SUBCLASSCD AS LEFT_CD ";
        $query .= " FROM ";
        $query .= "     MAIN_LIST T1 ";
        $query .= "     LEFT JOIN LEFT_LIST L1 ON L1.SUBCLASSCD = T1.SUBCLASSCD ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //UPDATE(模試)
    function &getUpdateMock(&$model) {
        //配列
        $codeArray = array();
        $codeArray = explode (",", $model->selectdataMock);

        //DB接続
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //削除
        $query  = " DELETE FROM RECORD_MOCK_ORDER_DAT ";
        $query .= "  WHERE YEAR     = '{$model->mockyear}' ";
        $query .= "    AND GRADE    = '{$model->field["GRADE"]}' ";
        $db->query($query);

        if ($codeArray[0] != "") {
            for ($i = 0; $i < get_count($codeArray); $i++) {
                $data = array();
                $data["YEAR"][TEXT]         = $model->mockyear;
                $data["GRADE"][TEXT]        = $model->field["GRADE"];
                $data["SEQ"][NUMBER]        = $i + 1;
                $data["TEST_DIV"][TEXT]     = "3";
                $data["MOCKCD"][TEXT]       = $codeArray[$i];
                $data["REGISTERCD"][TEXT]   = STAFFCD;
                $data["UPDATED"][FUNC]      = "sysdate()";
                //追加
                $query = Query::insertSQL($data, "RECORD_MOCK_ORDER_DAT");
                $db->query($query);
            }
        }

        //削除(科目)
        $query  = " DELETE FROM RECORD_MOCK_ORDER_SUB_DAT ";
        $query .= "  WHERE YEAR     = '{$model->mockyear}' ";
        $query .= "    AND GRADE    = '{$model->field["GRADE"]}' ";
        $db->query($query);

        $db->commit(); // トランザクションをコミットする。
        //DB切断
        Query::dbCheckIn($db);
        return;
    }

    //UPDATE(科目)
    function &getUpdateSub(&$model) {
        //配列
        $codeArray = array();
        $codeArray = explode (",", $model->selectdataSub);

        //DB接続
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //削除
        $query  = " DELETE FROM RECORD_MOCK_ORDER_SUB_DAT ";
        $query .= "  WHERE YEAR     = '{$model->mockyear}' ";
        $query .= "    AND GRADE    = '{$model->field["GRADE"]}' ";
        $db->query($query);

        if ($codeArray[0] != "") {
            for ($i = 0; $i < get_count($codeArray); $i++) {
                $data = array();
                $data["YEAR"][TEXT]         = $model->mockyear;
                $data["GRADE"][TEXT]        = $model->field["GRADE"];
                $data["SEQ"][NUMBER]        = $i + 1;
                $subclass_array = array();
                $subclass_array = explode("-", $codeArray[$i]);
                $data["CLASSCD"][TEXT]      = $subclass_array[0];
                $data["SCHOOL_KIND"][TEXT]  = $subclass_array[1];
                $data["CURRICULUM_CD"][TEXT]= $subclass_array[2];
                $data["SUBCLASSCD"][TEXT]   = $subclass_array[3];
                $data["REGISTERCD"][TEXT]   = STAFFCD;
                $data["UPDATED"][FUNC]      = "sysdate()";
                //追加
                $query = Query::insertSQL($data, "RECORD_MOCK_ORDER_SUB_DAT");
                $db->query($query);
            }
        }

        $db->commit(); // トランザクションをコミットする。
        //DB切断
        Query::dbCheckIn($db);
        return;
    }

    //コピーチェック
    function getCopyCheck($model) {
        $query  = " SELECT COUNT(*) FROM RECORD_MOCK_ORDER_DAT ";
        $query .= " WHERE YEAR = '".$model->mockyear."' ";
        $query .= "   AND SUBSTR(MOCKCD,2,4) = '".$model->mockyear."'";

        return $query;
    }
    //コピーチェック2
    function getCopyCheck2($model) {
        $query  = " SELECT COUNT(*) FROM RECORD_MOCK_ORDER_SUB_DAT ";
        $query .= " WHERE YEAR = '".$model->mockyear."' ";

        return $query;
    }

    //前年度からのコピー
    function &getCopyQuery($model) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $copyyear = $model->mockyear - 1;

        //RECORD_MOCK_ORDER_DAT
        $query  = "";
        $query .= "INSERT INTO RECORD_MOCK_ORDER_DAT ";
        $query .= "( ";
        $query .= "    YEAR, ";
        $query .= "    GRADE, ";
        $query .= "    SEQ, ";
        $query .= "    TEST_DIV, ";
        $query .= "    MOCKCD, ";
        $query .= "    REGISTERCD, ";
        $query .= "    UPDATED ";
        $query .= ") ";
        $query .= "( ";
        $query .= "SELECT ";
        $query .= "    '".$model->mockyear."' AS YEAR, ";
        $query .= "    GRADE, ";
        $query .= "    SEQ, ";
        $query .= "    TEST_DIV, ";
        $query .= "    SUBSTR(MOCKCD,1,1) || '".$model->mockyear."' || SUBSTR(MOCKCD,6,4) AS MOCKCD, ";
        $query .= "    '".STAFFCD."' AS REGISTERCD, ";
        $query .= "    SYSDATE() AS UPDATED ";
        $query .= "FROM ";
        $query .= "    RECORD_MOCK_ORDER_DAT T0 ";
        $query .= "WHERE ";
        $query .= "    YEAR = '".$copyyear."' ";
        $query .= "    AND SUBSTR(MOCKCD,2,4) = '".$copyyear."' ";
        $query .= "    AND SUBSTR(MOCKCD,1,1) || '".$model->mockyear."' || SUBSTR(MOCKCD,6,4) IN (SELECT MOCKCD FROM MOCK_MST) ";
        $query .= ") ";

        $db->query($query);

        //RECORD_MOCK_ORDER_SUB_DAT
        $query  = "";
        $query .= "INSERT INTO RECORD_MOCK_ORDER_SUB_DAT ";
        $query .= "( ";
        $query .= "    YEAR, ";
        $query .= "    GRADE, ";
        $query .= "    SEQ, ";
        $query .= "    CLASSCD, ";
        $query .= "    SCHOOL_KIND, ";
        $query .= "    CURRICULUM_CD, ";
        $query .= "    SUBCLASSCD, ";
        $query .= "    REGISTERCD, ";
        $query .= "    UPDATED ";
        $query .= ") ";
        $query .= "( ";
        $query .= "SELECT ";
        $query .= "    '".$model->mockyear."' AS YEAR, ";
        $query .= "    GRADE, ";
        $query .= "    SEQ, ";
        $query .= "    CLASSCD, ";
        $query .= "    SCHOOL_KIND, ";
        $query .= "    CURRICULUM_CD, ";
        $query .= "    SUBCLASSCD, ";
        $query .= "    '".STAFFCD."' AS REGISTERCD, ";
        $query .= "    SYSDATE() AS UPDATED ";
        $query .= "FROM ";
        $query .= "    RECORD_MOCK_ORDER_SUB_DAT T0 ";
        $query .= "WHERE ";
        $query .= "    YEAR = '".$copyyear."' ";
        $query .= "    AND NOT EXISTS ( ";
        $query .= "             SELECT 'X' FROM RECORD_MOCK_ORDER_SUB_DAT T1 ";
        $query .= "              WHERE T1.YEAR  = '".$model->mockyear."' ";
        $query .= "                AND T1.GRADE = T0.GRADE ";
        $query .= "                AND T1.SEQ   = T0.SEQ ";
        $query .= "                   ) ";
        $query .= ") ";

        $db->query($query);

        $db->commit();
        Query::dbCheckIn($db);
        return true;
   }
}
?>
