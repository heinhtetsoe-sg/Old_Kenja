<?php

require_once('for_php7.php');

class knjl072tQuery extends Query {
    //名称
    function GetName($namecd1,$ObjYear) {
        $query = "SELECT namecd2, name1, name2, namespare1, namespare2 FROM v_name_mst
                   WHERE year    = '".$ObjYear."'
                     AND namecd1 = '".$namecd1."'
                   ORDER BY namecd2";
        return $query;
    }

    function getMajorname($model) {
        $query  = " SELECT ";
        $query .= "     MAJORLNAME || '_' || MAJORSNAME ";
        $query .= " FROM ";
        $query .= "     V_ENTEXAM_MAJOR_MST ";
        $query .= " WHERE ";
        $query .= "     MAJORCD = '{$model->majorcd}' ";

        return $query;
    }

    //学科コンボ
    function getMajorcd() {
        $query  = " SELECT ";
        $query .= "     MAJORCD || ':' || MAJORLNAME || ' ' || MAJORSNAME AS LABEL, ";
        $query .= "     MAJORCD AS VALUE ";
        $query .= " FROM ";
        $query .= "     V_ENTEXAM_MAJOR_MST ";
        $query .= " ORDER BY ";
        $query .= "     MAJORCD ";

        return $query;
    }

    //募集枠数
    function getCapacity($model) {
        $query  = "";
        $query .= "SELECT ";
        $query .= "    APPLICANTDIV, ";
        $query .= "    MAJORCD, ";
        $query .= "    CAPACITY ";
        $query .= "FROM ";
        $query .= "    ENTEXAM_COURSE_MST ";
        $query .= "WHERE ";
        $query .= "    ENTEXAMYEAR = '".$model->ObjYear."' AND ";
        $query .= "    APPLICANTDIV = '".$model->applicantdiv."' AND ";
        $query .= "    MAJORCD = '".$model->majorcd."' ";
        return $query;
    }

    //合格者一覧
    function GetLeftList($model) {
            $query  = "  SELECT";
            $query .= "      T1.EXAMNO ,";
            $query .= "      T1.NAME ";
            $query .= "  FROM";
            $query .= "      ENTEXAM_APPLICANTBASE_DAT T1";
            $query .= "  LEFT JOIN ";
            $query .= "      NAME_MST L1 ON  L1.NAMECD2 = T1.JUDGEMENT ";
            $query .= "                  AND L1.NAMECD1 = 'L013' ";
            $query .= "  WHERE";
            $query .= "      T1.ENTEXAMYEAR     = '".$model->ObjYear."' AND";
            $query .= "      T1.APPLICANTDIV    = '".$model->applicantdiv."' AND";
            $query .= "      T1.SUC_MAJORCD     = '".$model->majorcd."' AND";
            $query .= "      L1.NAMESPARE1      = '1'";  //合格
            $query .= "  ORDER BY";
            $query .= "      T1.EXAMNO";

        return $query;
    }

    //候補者一覧
    function GetRightList($model) {
            $query  = " SELECT ";
            $query .= "     T1.EXAMNO, ";
            $query .= "     T1.NAME ";
            $query .= " FROM ";
            $query .= "     ENTEXAM_APPLICANTBASE_DAT T1 ";
            $query .= " LEFT JOIN ";
            $query .= "     NAME_MST L1 ON  L1.NAMECD2 = T1.JUDGEMENT ";
            $query .= "                 AND L1.NAMECD1 = 'L013' ";
            $query .= " LEFT JOIN ";
            $query .= "     ENTEXAM_APPLICANTWISH_DAT L2 ON  L2.ENTEXAMYEAR = T1.ENTEXAMYEAR ";
            $query .= "                                  AND L2.EXAMNO = T1.EXAMNO ";
            $query .= " WHERE ";
            $query .= "     T1.ENTEXAMYEAR     = '{$model->ObjYear}' AND";
            $query .= "     T1.APPLICANTDIV    = '{$model->applicantdiv}' AND";
            $query .= "     L2.MAJORLCD || L2.MAJORSCD     = '{$model->majorcd}' AND";
            $query .= "     L2.WISHNO          = '{$model->wishno}' ";
            if ($model->supplement == '1') {
                $query .= "     AND T1.TESTDIV2 IS NOT NULL";
            }
            $query .= "     AND value(L1.NAMESPARE1,'0') <> '1' ";  //合格は除く
            $query .= " ORDER BY ";
            $query .= "     T1.EXAMNO ";

        return $query;
    }

    //UPDATE
    function &getUpdateQuery($model) {
        //(左のリスト）
        $array  = (strlen($model->selectdata) ? explode(",", $model->selectdata) : array());
        //(右のリスト)
        $array2 = (strlen($model->selectdata2) ? explode(",", $model->selectdata2) : array());
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //(左のリスト）
        foreach ($array as $examno) {
            $query  = "UPDATE entexam_applicantbase_dat ";
            $query .= "   SET JUDGEMENT      = '1' ";
            $query .= "      ,SUC_COURSECD   = '1' ";
            $query .= "      ,SUC_MAJORCD    = '{$model->majorcd}' ";
            $query .= "      ,SUC_COURSECODE = '0000' ";

            $query .= "      ,registercd     = '".STAFFCD."'";
            $query .= "      ,updated        = SYSDATE()";
            $query .= " WHERE entexamyear  = '".$model->ObjYear."'";
            $query .= "   AND examno       = '".$examno."'";
            $db->query($query);
        }

        //(右のリスト)
        foreach ($array2 as $examno) {
            $query  = "UPDATE entexam_applicantbase_dat ";
            $query .= "   SET JUDGEMENT      = '2' ";
            $query .= "      ,SUC_COURSECD   = NULL ";
            $query .= "      ,SUC_MAJORCD    = NULL ";
            $query .= "      ,SUC_COURSECODE = NULL ";

            $query .= "      ,registercd     = '".STAFFCD."'";
            $query .= "      ,updated        = SYSDATE()";
            $query .= " WHERE entexamyear  = '".$model->ObjYear."'";
            $query .= "   AND examno       = '".$examno."'";
            $db->query($query);
        }

        $db->commit();

        Query::dbCheckIn($db);
    }
}
?>
