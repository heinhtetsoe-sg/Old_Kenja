<?php

require_once('for_php7.php');


class knjz235Query extends Query {

    //学年取得（権限チェック）
    function getGrade($model)
    {
        //参照・更新可
        $query  = " SELECT DISTINCT ";
        $query .= "     GRADE AS VALUE, ";
        $query .= "     GRADE AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_HDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND SEMESTER = '".CTRL_SEMESTER."' ";
        //参照・更新可（制限付き）
        if (AUTHORITY == DEF_REFER_RESTRICT || AUTHORITY == DEF_UPDATE_RESTRICT) {
            $query .= "     AND (   TR_CD1 = '".STAFFCD."' ";
            $query .= "          OR TR_CD2 = '".STAFFCD."' ";
            $query .= "          OR TR_CD3 = '".STAFFCD."') ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //テスト一覧取得
    function getTestList($model)
    {

        $query  = " WITH MAIN AS ( ";
        $query .= "     SELECT ";
        $query .= "         '1:' || SEMESTER || ':' || TESTKINDCD || ':' || TESTITEMCD AS VALUE, ";
        $query .= "         '考査：' || TESTITEMNAME AS LABEL ";
        $query .= "     FROM ";
        $query .= "         TESTITEM_MST_COUNTFLG_NEW ";
        $query .= "     WHERE ";
        $query .= "         YEAR = '".CTRL_YEAR."' ";

        for ($i = 1; $i < 3; $i++){
            $query .= "     UNION ";
            $query .= "     SELECT ";
            $query .= "         '2:' || MOCKCD AS VALUE, ";
            if($i == 1){
                $query .= "     '模試：' || MOCKNAME1 AS LABEL ";
            } else {
                $query .= "     '実力：' || MOCKNAME1 AS LABEL ";
            }
            $query .= "     FROM ";
            $query .= "         MOCK_MST ";
            $query .= "     WHERE ";
            $query .= "         SUBSTR(MOCKCD,1,1) = '".$i."' ";
        }

        $query .= " ), SELECTED AS ( ";
        $query .= "     SELECT ";
        $query .= "         TEST_DIV || ':' || SEMESTER || ':' || TESTKINDCD || ':' || TESTITEMCD AS VALUE ";
        $query .= "     FROM ";
        $query .= "         RECORD_MOCK_ORDER_DAT ";
        $query .= "     WHERE ";
        $query .= "         YEAR = '".CTRL_YEAR."' AND ";
        $query .= "         GRADE = '".$model->field["GRADE"]."' AND ";
        $query .= "         TEST_DIV = '1' ";
        $query .= "     UNION ";
        $query .= "     SELECT ";
        $query .= "         TEST_DIV || ':' || MOCKCD AS VALUE ";
        $query .= "     FROM ";
        $query .= "         RECORD_MOCK_ORDER_DAT ";
        $query .= "     WHERE ";
        $query .= "         YEAR = '".CTRL_YEAR."' AND ";
        $query .= "         GRADE = '".$model->field["GRADE"]."' AND ";
        $query .= "         TEST_DIV = '2' ";
        $query .= " ) ";

        $query .= " SELECT VALUE,LABEL";
        $query .= " FROM MAIN";
        $query .= " WHERE VALUE NOT IN (SELECT * FROM SELECTED) ";
        $query .= " ORDER BY VALUE ";

        return $query;
    }

    //選択テスト取得
    function getSelectTest($model)
    {

        $query  = " SELECT ";
        $query .= "     T1.SEQ, ";
        $query .= "     T1.TEST_DIV || ':' || T1.SEMESTER || ':' || T1.TESTKINDCD || ':' || T1.TESTITEMCD AS VALUE, ";
        $query .= "     '考査：' || T2.TESTITEMNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     RECORD_MOCK_ORDER_DAT T1, ";
        $query .= "     TESTITEM_MST_COUNTFLG_NEW T2 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = T2.YEAR AND ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' AND ";
        $query .= "     T1.GRADE = '".$model->field["GRADE"]."' AND ";
        $query .= "     T1.TEST_DIV = '1' AND ";
        $query .= "     T1.SEMESTER = T2.SEMESTER AND ";
        $query .= "     T1.TESTKINDCD = T2.TESTKINDCD AND ";
        $query .= "     T1.TESTITEMCD = T2.TESTITEMCD ";

        for ($i = 1; $i < 3; $i++){
            $query .= " UNION ";
            $query .= " SELECT ";
            $query .= "     T1.SEQ, ";
            $query .= "     T1.TEST_DIV || ':' || T1.MOCKCD AS VALUE, ";
            if($i == 1){
                $query .= "     '模試：' || T2.MOCKNAME1 AS LABEL ";
            } else {
                $query .= "     '実力：' || T2.MOCKNAME1 AS LABEL ";
            }
            $query .= " FROM ";
            $query .= "     RECORD_MOCK_ORDER_DAT T1, ";
            $query .= "     MOCK_MST T2 ";
            $query .= " WHERE ";
            $query .= "     T1.YEAR = '".CTRL_YEAR."' AND ";
            $query .= "     T1.GRADE = '".$model->field["GRADE"]."' AND ";
            $query .= "     T1.TEST_DIV = '2' AND ";
            $query .= "     T1.MOCKCD = T2.MOCKCD AND ";
            $query .= "     SUBSTR(T1.MOCKCD,1,1) = '".$i."' ";
        }

        $query .= " ORDER BY ";
        $query .= "     SEQ ";

        return $query;
    }

    //更新
    function &getUpdateQuery($model)
    {        
        //配列に格納
        $array = explode (",", $model->selectdata);
        
        $db = Query::dbCheckOut();
        $db->autoCommit(false);
        $updated = date("Y-m-d H:i:s", time());     //更新日付
        
        $query = " DELETE FROM RECORD_MOCK_ORDER_DAT ";
        $query.= " WHERE YEAR = '" .CTRL_YEAR."' AND ";
        $query.= "       GRADE = '".$model->field["GRADE"]."' ";
        $db->query($query);

        //考査順の登録
        if ($array[0] !="") {
            for ($i = 0; $i < get_count($array); $i++){
                $testdiv = $semester = $testkindcd = $testitemcd = $mockcd= "";
                if(substr($array[$i],0,1) == 1){
                    list($testdiv, $semester, $testkindcd, $testitemcd) = explode(":", $array[$i]);
                } else {
                    list($testdiv, $mockcd) = explode(":", $array[$i]);
                }

                $data["YEAR"][TEXT] 	    = CTRL_YEAR;
                $data["GRADE"][TEXT] 		= $model->field["GRADE"];
                $data["SEQ"][NUMBER] 		= $i+1;
                $data["TEST_DIV"][TEXT] 	= $testdiv;
                $data["SEMESTER"][TEXT] 	= ($semester) ? $semester : "";
                $data["TESTKINDCD"][TEXT] 	= ($testkindcd) ? $testkindcd : "";
                $data["TESTITEMCD"][TEXT] 	= ($testitemcd) ? $testitemcd : "";
                $data["MOCKCD"][TEXT] 		= ($mockcd) ? $mockcd : "";
                $data["REGISTERCD"][TEXT]   = STAFFCD;
                $data["UPDATED"][NUMBER] 	= "sysdate()";

                $query = Query::insertSQL($data, "RECORD_MOCK_ORDER_DAT");
                $db->query($query);
            }
        }
        $db->commit();      // トランザクションをコミットする。
        Query::dbCheckIn($db);
        return;
    }

    //コピー前チェック
    function cntSelectTest()
    {
        $db = Query::dbCheckOut();

        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     RECORD_MOCK_ORDER_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";

        $cntselected = $db->getOne($query);
        Query::dbCheckIn($db);
        return $cntselected;
    }

    //前年度のデータチェック
    function cntLastOrder()
    {
        $db = Query::dbCheckOut();

        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     RECORD_MOCK_ORDER_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".(CTRL_YEAR-1)."' ";

        $cntlastorder = $db->getOne($query);
        Query::dbCheckIn($db);
        return $cntlastorder;
    }

    //前年度からコピー
    function &getCopyQuery()
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $query  = " INSERT INTO RECORD_MOCK_ORDER_DAT ";
        $query .= "           ( YEAR, ";
        $query .= "             GRADE, ";
        $query .= "             SEQ, ";
        $query .= "             TEST_DIV, ";
        $query .= "             SEMESTER, ";
        $query .= "             TESTKINDCD, ";
        $query .= "             TESTITEMCD, ";
        $query .= "             MOCKCD, ";
        $query .= "             REGISTERCD, ";
        $query .= "             UPDATED ) ";
        $query .= "   (SELECT ";
        $query .= "         '".CTRL_YEAR."', ";
        $query .= "         GRADE, ";
        $query .= "         SEQ, ";
        $query .= "         TEST_DIV, ";
        $query .= "         SEMESTER, ";
        $query .= "         TESTKINDCD, ";
        $query .= "         TESTITEMCD, ";
        $query .= "         MOCKCD, ";
        $query .= "         '".STAFFCD."', ";
        $query .= "         sysdate() ";
        $query .= "     FROM ";
        $query .= "         RECORD_MOCK_ORDER_DAT ";
        $query .= "     WHERE ";
        $query .= "         YEAR = '".(CTRL_YEAR-1)."' AND ";
        $query .= "         SEMESTER || TESTKINDCD || TESTITEMCD IN( ";
        $query .= "                         SELECT ";
        $query .= "                             SEMESTER || TESTKINDCD || TESTITEMCD ";
        $query .= "                         FROM ";
        $query .= "                             TESTITEM_MST_COUNTFLG_NEW ";
        $query .= "                         WHERE ";
        $query .= "                             YEAR = '".CTRL_YEAR."' ";
        $query .= "                         ) ";
        $query .= "     ) ";

        $db->query($query);
        $db->commit();
        Query::dbCheckIn($db);
       return true;
    }

}
?>
