<?php

require_once('for_php7.php');

class knjl031cQuery extends Query {


    //入試制度取得
    function get_apct_div($namecd,$year)
    {
        $query  = "SELECT DISTINCT ";
        $query .= "    NAME1   AS LABEL, ";
        $query .= "    NAMECD2 AS VALUE ";
        $query .= "FROM ";
        $query .= "    V_NAME_MST ";
        $query .= "WHERE ";
        $query .= "    YEAR    = '{$year}' AND ";
        $query .= "    NAMECD1 = '{$namecd}' ";
        $query .= "ORDER BY ";
        $query .= "    NAMECD2 ";

        return $query;
    }

    //入試区分取得
    function get_testdiv_div($namecd, $year) {
        $query  = " SELECT ";
        $query .= "     NAME1 AS LABEL, ";
        $query .= "     NAMECD2 AS VALUE ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR    = '{$year}'   AND ";
        $query .= "     NAMECD1 = '{$namecd}' ";

        return $query;
    }

    //試験科目取得
    function select_subclass_div($namecd,$model)
    {
        $query  = "SELECT DISTINCT ";
        $query .= "    T1.TESTSUBCLASSCD || ' ' || T2.NAME1  AS LABEL, ";
        $query .= "    T1.TESTSUBCLASSCD AS VALUE, ";
        $query .= "    T1.SHOWORDER ";
        $query .= "FROM ";
        $query .= "    ENTEXAM_TESTSUBCLASSCD_DAT T1 ";
        $query .= "LEFT OUTER JOIN V_NAME_MST T2 ";
        $query .= "        ON  T1.ENTEXAMYEAR    = T2.YEAR ";
        $query .= "        AND T1.TESTSUBCLASSCD = T2.NAMECD2 ";
        $query .= "        AND T2.NAMECD1        = '{$namecd}' ";
        $query .= "WHERE ";
        $query .= "    T1.ENTEXAMYEAR  = '{$model->ObjYear}' AND ";
        $query .= "    T1.APPLICANTDIV = '{$model->field["APDIV"]}' AND ";
        $query .= "    T1.TESTDIV      = '{$model->field["TESTDIV"]}' ";
        $query .= "ORDER BY ";
        $query .= "    T1.SHOWORDER ";

        return $query;
    }

    //試験科目一覧取得
    function get_subclasslist_div($namecd,$model)
    {
        $query  = "SELECT DISTINCT ";
        $query .= "    NAMECD2 || ' ' || NAME1 AS LABEL, ";
        $query .= "    NAMECD2 AS VALUE ";
        $query .= "FROM ";
        $query .= "    V_NAME_MST ";
        $query .= "WHERE ";
        $query .= "    YEAR    = '{$model->ObjYear}' AND ";
        $query .= "    NAMECD1 = '{$namecd}' AND ";
        $query .= "    NAMECD2 NOT IN(SELECT DISTINCT ";
        $query .= "                        TESTSUBCLASSCD ";
        $query .= "                    FROM ";
        $query .= "                        ENTEXAM_TESTSUBCLASSCD_DAT ";
        $query .= "                    WHERE ";
        $query .= "                        ENTEXAMYEAR  = '{$model->ObjYear}' AND ";
        $query .= "                        APPLICANTDIV = '{$model->field["APDIV"]}' AND ";
        $query .= "                        TESTDIV      = '{$model->field["TESTDIV"]}' ";
        $query .= "                    ) ";
        $query .= "ORDER BY ";
        $query .= "    NAMECD2 ";

        return $query;
    }

    /**
     * UPDATE
     */
    function &getUpdateQuery($model)
    {
        $stuffcd = STAFFCD;

        //配列に格納
        $array = explode (",", $model->selectdata);

        $db = Query::dbCheckOut();
        $db->autoCommit(false);
        $updated = date("Y-m-d H:i:s", time());     //更新日付

        $query  = "DELETE from ENTEXAM_TESTSUBCLASSCD_DAT ";
        $query .= "where APPLICANTDIV = '{$model->field["APDIV"]}'";
        $query .= "  and ENTEXAMYEAR  = '{$model->ObjYear}'";
        $query .= "  and TESTDIV      = '{$model->field["TESTDIV"]}'";
        $db->query($query);

        //試験科目の登録
        if ($array[0] !="") {
            $query  = "INSERT INTO ENTEXAM_TESTSUBCLASSCD_DAT( ";
            $query .= "    ENTEXAMYEAR, ";
            $query .= "    APPLICANTDIV, ";
            $query .= "    TESTDIV, ";
            $query .= "    EXAM_TYPE, ";
            $query .= "    TESTSUBCLASSCD, ";
            $query .= "    SHOWORDER, ";
            $query .= "    REGISTERCD, ";
            $query .= "    UPDATED ";
            $query .= ") VALUES";
            for ($i = 0; $i < get_count($array); $i++){
                $sp = ",";
                if ($i == get_count($array)-1){
                    $sp = "";
                }
                $j = $i + 1;
                $query .= "('{$model->ObjYear}'";           //入試年度
                $query .= ",'{$model->field["APDIV"]}'";    //入試制度
                $query .= ",'{$model->field["TESTDIV"]}'";  //入試区分：法政は未使用のため固定値をセット
                $query .= ",'1'";                           //受験型　：法政は未使用のため固定値をセット
                $query .= ",'{$array[$i]}'";                //試験科目
                $query .= ",{$j}";                          //表示順
                $query .= ",'{$stuffcd}'";
                $query .= ",'{$updated}'){$sp}";            //更新日付

            }
            $db->query($query);
        }
        $db->commit();      // トランザクションをコミットする。

        Query::dbCheckIn($db);
        return; 
    }
}
?>
