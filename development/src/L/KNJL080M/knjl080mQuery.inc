<?php

require_once('for_php7.php');

class knjl080mQuery extends Query {

    //名称
    function GetName($namecd1,$ObjYear) {
        $query = "SELECT namecd2, name1, name2, namespare1, namespare2 FROM v_name_mst
                   WHERE year    = '".$ObjYear."'
                     AND namecd1 = '".$namecd1."'
                   ORDER BY namecd2";
        return $query;
    }

    //対象者
    function GetLeftList($model) {
        /* ------------- 手続者 ------------- */
            $query  = "  SELECT";
            $query .= "      T1.examno, ";
            $query .= "      T1.name, ";
            $query .= "      T1.proceduredate ";
            $query .= "  FROM";
            $query .= "      entexam_applicantbase_dat T1";
            $query .= "  LEFT JOIN ";
            $query .= "      NAME_MST L1 ON  L1.NAMECD2 = T1.judgement ";
            $query .= "                  AND L1.NAMECD1 = 'L013' ";
            $query .= "  WHERE";
            $query .= "      T1.entexamyear     = '".$model->ObjYear."' AND";
            $query .= "      T1.applicantdiv    = '".$model->applicantdiv."' AND";
            $query .= "      T1.procedurediv    = '1' AND"; //手続済み
            $query .= "      T1.entdiv          = '1' AND"; //入学済み
            $query .= "      L1.NAMESPARE1      = '1'";  //合格
                             //辞退者を除く
            $query .= "      AND NOT EXISTS (";
            $query .= "          SELECT";
            $query .= "              'X' ";
            $query .= "          FROM";
            $query .= "              entexam_applicantbase_dat T0";
            $query .= "          WHERE";
            $query .= "              T0.entexamyear     = '".$model->ObjYear."' AND";
            $query .= "              T0.applicantdiv    = '".$model->applicantdiv."' AND";
            $query .= "             (T0.procedurediv    = '2' OR";      //手続無し
            $query .= "              T0.entdiv          = '2' ) AND";   //入学辞退
            $query .= "              T0.examno          = T1.examno )";
            $query .= "  ORDER BY";
            $query .= "      T1.examno";
        return $query;
    }

    //合格者
    function GetRightList($model) {
        /* ------------- 合否判定での合格者 ------------- */
            $query  = "  SELECT";
            $query .= "      T1.examno ,";
            $query .= "      T1.name ";
            $query .= "  FROM";
            $query .= "      entexam_applicantbase_dat T1";
            $query .= "  LEFT JOIN ";
            $query .= "      NAME_MST L1 ON  L1.NAMECD2 = T1.judgement ";
            $query .= "                  AND L1.NAMECD1 = 'L013' ";
            $query .= "  WHERE";
            $query .= "      T1.entexamyear     = '".$model->ObjYear."' AND";
            $query .= "      T1.applicantdiv    = '".$model->applicantdiv."' AND";
            $query .= "      L1.NAMESPARE1      = '1'";  //合格
                             //辞退者を除く
            $query .= "      AND NOT EXISTS (";
            $query .= "          SELECT";
            $query .= "              'X' ";
            $query .= "          FROM";
            $query .= "              entexam_applicantbase_dat T0";
            $query .= "          WHERE";
            $query .= "              T0.entexamyear     = '".$model->ObjYear."' AND";
            $query .= "              T0.applicantdiv    = '".$model->applicantdiv."' AND";
            $query .= "             (T0.procedurediv    = '2' OR";      //手続無し
            $query .= "              T0.entdiv          = '2' ) AND";   //入学辞退
            $query .= "              T0.examno          = T1.examno )";
            $query .= "  ORDER BY";
            $query .= "      T1.examno";
        return $query;
    }

    //UPDATE
    function &getUpdateQuery($model) {
        //対象者(左のリスト）
        $array  = (strlen($model->selectdata) ? explode(",", $model->selectdata) : array());
        //合格者(右のリスト)
        $array2 = (strlen($model->selectdata2) ? explode(",", $model->selectdata2) : array());
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        switch ($model->appli_type) {
            case "1":  //手続者
                foreach ($array as $examno) {
                    $query  = "UPDATE entexam_applicantbase_dat ";
                    $query .= "   SET procedurediv  = '1'";      //手続区分 1:済み/2:無し
                    $query .= "      ,entdiv        = '1'";      //入学区分 1:済み/2:辞退
                    $query .= "      ,registercd = '".STAFFCD."'";
                    $query .= "      ,updated    = SYSDATE()";
                    $query .= " WHERE entexamyear  = '".$model->ObjYear."'";
                    $query .= "   AND applicantdiv = '".$model->applicantdiv."'";
                    $query .= "   AND examno       = '".$examno."'";
                    $db->query($query);

                    $query  = "UPDATE entexam_applicantbase_dat ";
                    $query .= "   SET proceduredate = '". str_replace("/", "-", $model->proceduredate) ."'"; //手続き日付
                    $query .= "      ,registercd    = '".STAFFCD."'";
                    $query .= "      ,updated       = SYSDATE()";
                    $query .= " WHERE entexamyear   = '".$model->ObjYear."'";
                    $query .= "   AND applicantdiv  = '".$model->applicantdiv."'";
                    $query .= "   AND examno        = '".$examno."'";
                    $query .= "   AND proceduredate is null ";
                    $db->query($query);
                }

                foreach ($array2 as $examno) {
                    $query  = "UPDATE entexam_applicantbase_dat ";
                    $query .= "   SET procedurediv  = NULL";
                    $query .= "      ,entdiv        = NULL";
                    $query .= "      ,proceduredate = NULL";
                    $query .= "      ,registercd = '".STAFFCD."'";
                    $query .= "      ,updated    = SYSDATE()";
                    $query .= " WHERE entexamyear  = '".$model->ObjYear."'";
                    $query .= "   AND applicantdiv = '".$model->applicantdiv."'";
                    $query .= "   AND examno       = '".$examno."'";
                    $db->query($query);
                }
            break;
        }

        $db->commit();

        Query::dbCheckIn($db);
    }
}
?>
