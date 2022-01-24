<?php

require_once('for_php7.php');

class knjl213yquery extends Query {

    //名称マスタより取得
    function getNameMst($namecd1, $year) {
        $query  = " SELECT ";
        $query .= "     NAMECD2 AS VALUE,";
        $query .= "     NAMECD2 || ':' || NAME1 AS LABEL, ";
        $query .= "     NAMESPARE2 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$year."' AND ";
        $query .= "     NAMECD1 = '".$namecd1."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //生成対象の受験番号
    function getExamno($model, $flg = "") {
        $query  = "";
        if ($flg != "CNT") {
            $query .= "SELECT T1.EXAMNO ";
        } else {
            $query .= "SELECT COUNT(T1.EXAMNO) ";
        }
        $query .= "  FROM ENTEXAM_APPLICANTBASE_DAT T1 ";
        $query .= " WHERE T1.ENTEXAMYEAR    = '".$model->ObjYear."' ";
        $query .= "   AND T1.APPLICANTDIV   = '".$model->field["APPLICANTDIV"]."' ";
        $query .= "   AND (T1.TESTDIV1      = '".$model->field["TESTDIV"]."' ";
        $query .= "     OR T1.TESTDIV2      = '".$model->field["TESTDIV"]."' ";
        $query .= "     OR T1.TESTDIV3      = '".$model->field["TESTDIV"]."') ";
        if ($flg != "CNT") {
            $query .= " ORDER BY T1.EXAMNO ";
        }
        return $query;
    }

    function ExecuteQuery($model) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //削除処理（一旦すべてを削除する）
        $query  = "DELETE FROM entexam_recept_dat ";
        $query .= " WHERE entexamyear  = '".$model->ObjYear."'";
        $query .= "   AND applicantdiv = '".$model->field["APPLICANTDIV"]."'";
        $query .= "   AND testdiv      = '".$model->field["TESTDIV"]."'";
        $db->query($query);

        $query  = "DELETE FROM entexam_score_dat ";
        $query .= " WHERE entexamyear  = '".$model->ObjYear."'";
        $query .= "   AND applicantdiv = '".$model->field["APPLICANTDIV"]."'";
        $query .= "   AND testdiv      = '".$model->field["TESTDIV"]."'";
        $db->query($query);

        //追加処理
        $examnoArray = array();
        $query = knjl213yQuery::getExamno($model);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $examnoArray[] = $row["EXAMNO"];
        }
        $result->free();

        if (0 < get_count($examnoArray)) {
            foreach ($examnoArray as $key => $examno) {
                //受付番号の値
                if ($model->recept_div == "1") {
                    //受験番号をコピー
                    $receptno = $examno;
                } else {
                    //連番(生年月日、受験番号順)
                    $receptno = $key + 1;
                    $receptno = sprintf("%05d",$receptno);
                }
//echo "receptno=" .$receptno .", examno=" .$examno ."<BR>";
                $query  = "INSERT INTO entexam_recept_dat";
                $query .= " (entexamyear, applicantdiv, testdiv, exam_type, receptno, examno,registercd, updated)";
                $query .= " VALUES ('{$model->ObjYear}'";
                $query .= "        ,'{$model->field["APPLICANTDIV"]}'";
                $query .= "        ,'{$model->field["TESTDIV"]}'";
                $query .= "        ,'1'";
                $query .= "        ,'{$receptno}'";
                $query .= "        ,'{$examno}'";
                $query .= "        ,'".STAFFCD."'";
                $query .= "        ,SYSDATE()";
                $query .= ")";
                $db->query($query);
            }
        }

        $db->commit();
        Query::dbCheckIn($db);
    }
}
?>
