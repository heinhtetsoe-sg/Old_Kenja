<?php

require_once('for_php7.php');

class knjl013hquery extends Query {

    //名称
    function GetName($namecd1,$ObjYear)
    {
        $query = "SELECT namecd2, name1, name2, namespare2 FROM v_name_mst
                   WHERE year    = '".$ObjYear."'
                     AND namecd1 = '".$namecd1."'
                   ORDER BY namecd2";
        return $query;
    }

    //受験番号を配列にセット（１件も無い場合、存在エラー）
    function existCheck($model)
    {
        $db = Query::dbCheckOut();

        $query  = "";
        $query .= "SELECT EXAMNO ";
        $query .= "  FROM ENTEXAM_APPLICANTBASE_DAT ";
        $query .= " WHERE ENTEXAMYEAR = '".$model->ObjYear."' ";
        $query .= "   AND APPLICANTDIV = '".$model->applicantdiv."' ";
        $query .= "   AND TESTDIV".$model->testdiv." IS NOT NULL ";
        $query .= " ORDER BY EXAMNO ";

        $examno = array();
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            $examno[] = $row["EXAMNO"];
        }

        Query::dbCheckIn($db);

        return $examno;
    }

    //重複データチェック(同じ受験番号が違う入試制度に在る場合、重複エラー）
    function existCheck2($model)
    {
        $db = Query::dbCheckOut();

        $query  = "";
        $query .= "SELECT RECEPTNO ";
        $query .= "      ,EXAMNO ";
        $query .= "      ,N1.NAME1 AS APPLICANTDIV  ";
        $query .= "      ,N2.NAME1 AS TESTDIV  ";
        $query .= "      ,N3.NAME1 AS EXAM_TYPE  ";
        $query .= "  FROM ENTEXAM_RECEPT_DAT T1  ";
        $query .= "       LEFT JOIN NAME_MST N1 ON N1.NAMECD1='L003' AND N1.NAMECD2=T1.APPLICANTDIV  ";
        $query .= "       LEFT JOIN NAME_MST N2 ON N2.NAMECD1='L004' AND N2.NAMECD2=T1.TESTDIV  ";
        $query .= "       LEFT JOIN NAME_MST N3 ON N3.NAMECD1='L005' AND N3.NAMECD2=T1.EXAM_TYPE  ";
        $query .= " WHERE ENTEXAMYEAR = '".$model->ObjYear."' ";
        $query .= "   AND APPLICANTDIV != '".$model->applicantdiv."' ";
        $query .= "   AND TESTDIV = '".$model->testdiv."' ";
        $query .= "   AND EXAMNO IN ( ";
        $query .= "            SELECT  EXAMNO ";
        $query .= "              FROM  ENTEXAM_APPLICANTBASE_DAT ";
        $query .= "             WHERE  ENTEXAMYEAR = '".$model->ObjYear."' ";
        $query .= "               AND  APPLICANTDIV = '".$model->applicantdiv."' ";
        $query .= "               AND  TESTDIV".$model->testdiv." IS NOT NULL ";
        $query .= "            ) ";
        $query .= " ORDER BY RECEPTNO ";

        $existdata = $db->getRow($query, DB_FETCHMODE_ASSOC);

        Query::dbCheckIn($db);

        return $existdata;
    }

    function ExecuteQuery($model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //削除処理（一旦すべてを削除する）
        $query  = "DELETE FROM entexam_recept_dat ";
        $query .= " WHERE entexamyear  = '".$model->ObjYear."'";
        $query .= "   AND applicantdiv = '".$model->applicantdiv."'";
        $query .= "   AND testdiv      = '".$model->testdiv."'";
        $db->query($query);

        $query  = "DELETE FROM entexam_score_dat ";
        $query .= " WHERE entexamyear  = '".$model->ObjYear."'";
        $query .= "   AND applicantdiv = '".$model->applicantdiv."'";
        $query .= "   AND testdiv      = '".$model->testdiv."'";
        $db->query($query);

        $query  = "DELETE FROM entexam_judge_tmp ";
        $query .= " WHERE entexamyear  = '".$model->ObjYear."'";
        $query .= "   AND applicantdiv = '".$model->applicantdiv."'";
        $query .= "   AND testdiv      = '".$model->testdiv."'";
        $db->query($query);

        //追加処理
        foreach ($model->examno as $key => $val)
        {
            //受付番号の値
            if ($model->recept_div == "1") {
                //受験番号をコピー
                $receptno = $val;
            } else {
                //連番(受験番号順)
                $receptno = $key + 1;
                $receptno = sprintf("%04d",$receptno);
            }

            $query  = "INSERT INTO entexam_recept_dat";
            $query .= " (entexamyear, applicantdiv, testdiv, exam_type, receptno, examno,registercd, updated)";
            $query .= " VALUES ('".$model->ObjYear."'";
            $query .= "        ,'".$model->applicantdiv."'";
            $query .= "        ,'".$model->testdiv."'";
            $query .= "        ,'1'";
            $query .= "        ,'".sprintf("%04d",$receptno)."'";
            $query .= "        ,'".$val."'";
            $query .= "        ,'".STAFFCD."'";
            $query .= "        ,SYSDATE()";
            $query .= ")";
            $db->query($query);
        }

        $db->commit();
        Query::dbCheckIn($db);
    }
}
?>
