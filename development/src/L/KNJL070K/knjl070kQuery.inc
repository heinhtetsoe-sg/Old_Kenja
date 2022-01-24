<?php

require_once('for_php7.php');

class knjl070kQuery extends Query {

    //名称
    function GetName($year, $namecd1)
    {
        $query = "SELECT namecd2, name1 FROM v_name_mst
                   WHERE year    = '".$year."'
                     AND namecd1 = '".$namecd1."'";
        return $query;
    }
    
    //試験会場設定データ
    function selectQuery(&$model)
    {
        $query = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_HALL_DAT ";
        $query .= " WHERE ";
        if ($model->cmd == "edit"){
            $query .= "     EXAMHALLCD = '" .$model->examhallcd ."' AND ";
        }
        $query .= "     TESTDIV = '" .$model->testdiv ."' ";
        $query .= " ORDER BY ";
        $query .= "     EXAMHALLCD ";
        return $query;
    }
    function update(&$model)
    {
        $db = Query::dbCheckOut();

        $query = " UPDATE ";
        $query .= "     ENTEXAM_HALL_DAT ";
        $query .= " SET ";
        $query .= "     EXAMHALL_NAME = '" .Query::addquote($model->field["EXAMHALL_NAME"]) ."', ";
        $query .= "     CAPA_CNT = " .$model->field["CAPA_CNT"] .", ";
        if (is_numeric($model->field["S_EXAMNO"])){
            $query .= "     S_EXAMNO = '" .sprintf("%04d", (int) $model->field["S_EXAMNO"]) ."', ";
        }
        $query .= "     E_EXAMNO = null, ";
        $query .= "     REGISTERCD = '".STAFFCD ."', ";
        $query .= "     UPDATED  = SYSDATE() ";
        $query .= " WHERE ";
        $query .= "     TESTDIV = '" .$model->testdiv ."' AND ";
        $query .= "     EXAMHALLCD = '" .$model->examhallcd ."' ";

        $db->query($query);
        Query::dbCheckIn($db);
    }
    //割り振り実行
    function exec(&$model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //試験会場割振り
        $query = " UPDATE ";
        $query .= "     ENTEXAM_APPLICANTBASE_DAT T1 ";
        $query .= " SET ";
        $query .= "     (T1.EXAMHALLNO,UPDATED) = (";
        $query .= "         NULL, ";
        $query .= "         SYSDATE() ";
        $query .= "     ) ";
        $query .= " WHERE ";
        $query .= "     T1.ENTEXAMYEAR = '" .$model->year ."' AND ";
        $query .= "     T1.SPECIAL_REASON_DIV IN (SELECT NAMECD2 FROM NAME_MST WHERE NAMECD1 = 'L017' AND NAMESPARE1 = '1') AND ";
        $query .= "     T1.TESTDIV = '" .$model->testdiv ."' ";

        $db->query($query);

        $sum_capa_cnt = 0;
        foreach($model->data as $v)
        {
            if (is_numeric($v["S_EXAMNO"])){
                $s_examno = (int) $v["S_EXAMNO"];
            }else{
                $s_examno = (int) $e_examno + 1;
            }
            $query = " SELECT ";
            $query .= "     SUBSTR(DIGITS(INTEGER(COALESCE(MIN(ST1.examno),'0'))),7,4) AS S_EXAMNO, ";
            $query .= "     SUBSTR(DIGITS(INTEGER(COALESCE(MAX(ST1.examno),'0'))),7,4) AS E_EXAMNO ";
            $query .= " FROM ";
            $query .= "     (SELECT ";
            $query .= "         row_number() over(ORDER BY EXAMNO) AS ke, ";
            $query .= "         examno ";
            $query .= "     FROM ";
            $query .= "         ENTEXAM_APPLICANTBASE_DAT ";
            $query .= "     WHERE ";
            $query .= "         ENTEXAMYEAR = '".$model->year ."' AND ";
            $query .= "         SPECIAL_REASON_DIV IN (SELECT NAMECD2 FROM NAME_MST WHERE NAMECD1 = 'L017' AND NAMESPARE1 = '1') AND ";
            $query .= "         INTEGER(EXAMNO) >= " .$s_examno ." AND ";
            $query .= "         TESTDIV = '" .$model->testdiv ."' AND ";
            $query .= "         VALUE(APPLICANTDIV,'0') NOT IN ('2') ";			//2006.01.25 alp m-yama
            $query .= "     ) AS ST1 ";
            $query .= " WHERE ";
            $query .= "     ST1.ke <= " .$v["CAPA_CNT"];

            $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
            if (is_array($row)){
                if ((int) $row["S_EXAMNO"] == 0 || (int) $row["E_EXAMNO"] == 0 ) break;
                $query = " UPDATE ";
                $query .= "     ENTEXAM_HALL_DAT T1 ";
                $query .= " SET ";
                $query .= "     S_EXAMNO = '" .$row["S_EXAMNO"] ."',";
                $query .= "     E_EXAMNO = '" .$row["E_EXAMNO"] ."'";
                $query .= " WHERE ";
                $query .= "     T1.TESTDIV = '" .$model->testdiv ."' AND ";
                $query .= "     T1.EXAMHALLCD = '" .$v["EXAMHALLCD"] ."' ";

                $db->query($query);

                //試験会場割振り
                $query = " UPDATE ";
                $query .= "     ENTEXAM_APPLICANTBASE_DAT T1 ";
                $query .= " SET ";
                $query .= "     EXAMHALLNO = '" .$v["EXAMHALLCD"] ."',";
                $query .= "     UPDATED = SYSDATE() ";
                $query .= " WHERE ";
                $query .= "     T1.ENTEXAMYEAR = '" .$model->year ."' AND ";
                $query .= "     T1.SPECIAL_REASON_DIV IN (SELECT NAMECD2 FROM NAME_MST WHERE NAMECD1 = 'L017' AND NAMESPARE1 = '1') AND ";
                $query .= "     T1.TESTDIV = '" .$model->testdiv ."' AND ";
	            $query .= "     VALUE(T1.APPLICANTDIV,'0') NOT IN ('2') AND ";			//2006.01.25 alp m-yama
                $query .= "     INTEGER(T1.EXAMNO) BETWEEN " .(int) $row["S_EXAMNO"] ." AND ";
                $query .= (int) $row["E_EXAMNO"];

                $db->query($query);
            }
            $e_examno = $row["E_EXAMNO"];
        }

        $db->commit(); 
        Query::dbCheckIn($db);
    }
    //受験番号クリア
    function clear(&$model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //試験会場割振り
        $query = " UPDATE ";
        $query .= "     ENTEXAM_APPLICANTBASE_DAT T1 ";
        $query .= " SET ";
        $query .= "     (T1.EXAMHALLNO,UPDATED) = (";
        $query .= "         NULL, ";
        $query .= "         SYSDATE() ";
        $query .= "     ) ";
        $query .= " WHERE ";
        $query .= "     T1.ENTEXAMYEAR = '" .$model->year ."' AND ";
        $query .= "     T1.SPECIAL_REASON_DIV IN (SELECT NAMECD2 FROM NAME_MST WHERE NAMECD1 = 'L017' AND NAMESPARE1 = '1') AND ";
        $query .= "     T1.TESTDIV = '" .$model->testdiv ."' ";

        $db->query($query);

        $query = " UPDATE ";
        $query .= "     ENTEXAM_HALL_DAT T1 ";
        $query .= " SET ";
        $query .= "     (T1.S_EXAMNO,T1.E_EXAMNO,UPDATED) = (";
        $query .= "         NULL, ";
        $query .= "         NULL, ";
        $query .= "         SYSDATE() ";
        $query .= "     ) ";
        $query .= " WHERE ";
        $query .= "     T1.TESTDIV = '" .$model->testdiv ."' ";

        $db->query($query);

        $db->commit();
        Query::dbCheckIn($db);

    }
    function insert(&$model)
    {
        $db = Query::dbCheckOut();

        $query = " SELECT ";
        $query .= "     MAX(EXAMHALLCD) AS EXAMHALLCD ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_HALL_DAT ";
        $query .= " WHERE ";
        $query .= "     TESTDIV = '" .$model->testdiv ."' ";
        $query .= " ORDER BY ";
        $query .= "     EXAMHALLCD DESC ";

        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);

        $examhallcd = 1;
        if (is_numeric($row["EXAMHALLCD"])){
            $examhallcd = (int) $row["EXAMHALLCD"] + 1;
        }
        $query = " INSERT INTO ENTEXAM_HALL_DAT( ";
        $query .= " TESTDIV, ";
        $query .= " EXAMHALLCD, ";
        $query .= " EXAMHALL_NAME, ";
        $query .= " CAPA_CNT, ";
        if (is_numeric($model->field["S_EXAMNO"])){
            $query .= " S_EXAMNO, ";
        }
        $query .= " REGISTERCD ";
        $query .= " ) ";
        $query .= " VALUES ";
        $query .= " ( ";
        $query .= " '" .$model->testdiv ."', ";
        $query .= " '".sprintf("%04d", $examhallcd). "', ";
        $query .= " '" .Query::addquote($model->field["EXAMHALL_NAME"]) ."', ";
        $query .= " " .$model->field["CAPA_CNT"] .", ";
        if (is_numeric($model->field["S_EXAMNO"])){
            $query .= " '" .sprintf("%04d", (int) $model->field["S_EXAMNO"]) ."', ";
        }
        $query .= " '".STAFFCD ."' ";
        $query .= " ) ";

        $db->query($query);

        Query::dbCheckIn($db);
    }
    function delete(&$model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //試験会場割振り
        $query = " UPDATE ";
        $query .= "     ENTEXAM_APPLICANTBASE_DAT T1 ";
        $query .= " SET ";
        $query .= "     (T1.EXAMHALLNO,UPDATED) = (";
        $query .= "         NULL, ";
        $query .= "         SYSDATE() ";
        $query .= "     ) ";
        $query .= " WHERE ";
        $query .= "     T1.ENTEXAMYEAR = '" .$model->year ."' AND ";
        $query .= "     T1.SPECIAL_REASON_DIV IN (SELECT NAMECD2 FROM NAME_MST WHERE NAMECD1 = 'L017' AND NAMESPARE1 = '1') AND ";
        $query .= "     T1.TESTDIV = '" .$model->testdiv ."' ";

        $db->query($query);

        $query = " DELETE ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_HALL_DAT ";
        $query .= " WHERE ";
        $query .= "     TESTDIV = '" .$model->testdiv ."' AND ";
        $query .= "     EXAMHALLCD IN ('" .implode($model->field["CHECKED"],"','") ."') ";

        $result = $db->query($query);

        $db->query($query);
        $db->commit(); 
        Query::dbCheckIn($db);
    }
    //処理、未処理件数取得
    function selectQueryCount(&$model)
    {
        $query = " SELECT ";
        $query .= "     COUNT(CASE WHEN EXAMHALLNO IS NOT NULL THEN 1 END) AS EXAMHALLNO1, ";
        $query .= "     COUNT(CASE WHEN EXAMHALLNO IS NULL THEN 1 END) AS EXAMHALLNO2 ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_APPLICANTBASE_DAT ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR = '" .$model->year ."' AND ";
        $query .= "     SPECIAL_REASON_DIV IN (SELECT NAMECD2 FROM NAME_MST WHERE NAMECD1 = 'L017' AND NAMESPARE1 = '1') AND ";
        $query .= "     TESTDIV = '" .$model->testdiv ."' AND ";
        $query .= "     VALUE(APPLICANTDIV,'0') NOT IN ('2') ";

        return $query;

    }
}
?>