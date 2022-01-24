<?php

require_once('for_php7.php');

class knjl031hQuery extends Query {


    //入試制度取得
    function get_apct_div($namecd,$year)
    {
        $query  = " SELECT DISTINCT NAME1,NAMECD2 ";
        $query .= " FROM V_NAME_MST ";
        $query .= " WHERE ";
        $query .= " YEAR = '".$year."' AND ";
        $query .= " NAMECD1 = '".$namecd."' ";
        $query .= " ORDER BY NAMECD2 ";

        return $query;
    }

    //試験科目取得
    function select_subclass_div($namecd,$model)
    {
        $query  = " SELECT DISTINCT T1.TESTSUBCLASSCD,T1.SHOWORDER,T2.NAME1 ";
        $query .= " FROM ENTEXAM_TESTSUBCLASSCD_DAT T1 ";
        $query .= " LEFT OUTER JOIN V_NAME_MST T2 ";
        $query .= "      ON T1.ENTEXAMYEAR = T2.YEAR ";
        $query .= "     AND T1.TESTSUBCLASSCD = T2.NAMECD2 ";
        $query .= "     AND T2.NAMECD1 = '".$namecd."' ";
        $query .= " WHERE ";
        $query .= " T1.ENTEXAMYEAR = '".$model->ObjYear."' AND ";
        $query .= " T1.APPLICANTDIV = '".$model->field["APDIV"]."' ";
        $query .= " ORDER BY T1.SHOWORDER,T1.TESTSUBCLASSCD ";

        return $query;
    }

    //試験科目一覧取得
    function get_subclasslist_div($namecd,$model)
    {
        $query  = " SELECT DISTINCT NAME1,NAMECD2 ";
        $query .= " FROM V_NAME_MST ";
        $query .= " WHERE ";
        $query .= " YEAR = '".$model->ObjYear."' AND ";
        $query .= " NAMECD1 = '".$namecd."' AND ";
        $query .= " NAMECD2 NOT IN( SELECT DISTINCT TESTSUBCLASSCD FROM ENTEXAM_TESTSUBCLASSCD_DAT ";
        $query .= " WHERE ENTEXAMYEAR = '".$model->ObjYear."' AND APPLICANTDIV = '".$model->field["APDIV"]."')";
        $query .= " ORDER BY NAMECD2 ";

        return $query;
    }

    /**
     * UPDATE
     */
    function &getUpdateQuery($model)
    {        
        //配列に格納
        $array = explode (",", $model->selectdata);
        
        $db = Query::dbCheckOut();
        $db->autoCommit(false);
        $updated = date("Y-m-d H:i:s", time());     //更新日付
        
        $query = "DELETE from ENTEXAM_TESTSUBCLASSCD_DAT ";
        $query.= "where APPLICANTDIV = '" .$model->field["APDIV"] ."'";
        $query.= "and ENTEXAMYEAR = '".$model->ObjYear."'";
        $db->query($query);

        //試験科目の登録
        if ($array[0] !="") {
            $query = "INSERT INTO ENTEXAM_TESTSUBCLASSCD_DAT(ENTEXAMYEAR,APPLICANTDIV,TESTDIV,EXAM_TYPE,TESTSUBCLASSCD,SHOWORDER,REGISTERCD,UPDATED)";
            $query.= "VALUES";
            for ($i = 0; $i < get_count($array); $i++){
                $sp = ",";
                if ($i == get_count($array)-1){
                    $sp = "";
                }
                $query.= "('" .$model->ObjYear ."'";                //入試年度
                $query.= ",'" .$model->field["APDIV"] ."'";         //入試制度
                $query.= ",'1'";                                    //入試区分：法政は未使用のため固定値をセット
                $query.= ",'1'";                                    //受験型　：法政は未使用のため固定値をセット
                $query.= ",'" .$array[$i] ."'";                     //試験科目
                $query.= ",$i+1";                                   //表示順
                $query.= ",'" .STAFFCD ."'";
                $query.= ",'" .$updated ."')" .$sp;                 //更新日付

            }
            $db->query($query);
        }
        $db->commit();      // トランザクションをコミットする。

        Query::dbCheckIn($db);
        return;
    }
}
?>
