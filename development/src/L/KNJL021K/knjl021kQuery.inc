<?php

require_once('for_php7.php');

class knjl021kQuery extends Query {

    //名称
    function GetName($namecd1,$ObjYear,$flg=0)
    {
        $query  = "SELECT namecd2, name1, name2 FROM v_name_mst";
        $query .= " WHERE year     = '".$ObjYear."'";
        $query .= "    AND namecd1 = '".$namecd1."'";
        return $query;
    }

    //中高判定
    function GetJorH(){
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     SCHOOL_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' AND ";
        $query .= "     SCHOOLNAME1 LIKE '%近畿%中学%' ";

        return $query;
    }

    //リストデータ
    function GetList($model, $opt_where)
    {

        $query  = "WITH MAIN_T AS ( ";
        $query .= "SELECT ";
        $query .= "    COUNT(*) AS CNT, ";
        $query .= "    ENTEXAMYEAR, ";
        $query .= "    TESTDIV, ";
        $query .= "    MIN(ACCEPTNO) AS ACCEPTNO1, ";
        $query .= "    ".$opt_where[$model->center_title]["MAIN_T_SELECT"]." ";
        $query .= "FROM ";
        $query .= "    ENTEXAM_CONSULTATION_HDAT ";
        $query .= "WHERE ";
        $query .= "    ENTEXAMYEAR = '".$model->ObjYear."' ";
        $query .= "    AND TESTDIV = '".$model->testdiv."' ";
        $query .= "GROUP BY ";
        $query .= "    ENTEXAMYEAR, ";
        $query .= "    TESTDIV, ";
        $query .= "    ".$opt_where[$model->center_title]["MAIN_T_GROUP"]." ";
        $query .= "HAVING ";
        $query .= "    COUNT(*) > 1 ";
        $query .= "), JOIN_T AS ( ";
        $query .= "SELECT ";
        $query .= "    T1.ACCEPTNO AS ACCEPTNO2, ";
        $query .= "    VALUE(T1.NAME,'') AS NAME2, ";
        $query .= "    VALUE(T1.NAME_KANA,'') AS KANA2, ";
        $query .= "    VALUE(T1.PS_ACCEPTNO,'') AS PS_ACCEPTNO2, ";
        $query .= "    VALUE(T1.FS_ACCEPTNO,'') AS FS_ACCEPTNO2, ";
        $query .= "    VALUE(T1.FS_CD,'') AS FS_CD2 ";
        $query .= "FROM ";
        $query .= "    ENTEXAM_CONSULTATION_HDAT T1 ";
        $query .= "WHERE ";
        $query .= "    T1.ENTEXAMYEAR = '".$model->ObjYear."' ";
        $query .= "    AND T1.TESTDIV = '".$model->testdiv."' ";
        $query .= "    AND T1.ACCEPTNO NOT IN (SELECT ";
        $query .= "                                T2.ACCEPTNO1 ";
        $query .= "                            FROM ";
        $query .= "                                MAIN_T T2) ";
        $query .= "), SELECT_T AS ( ";
        $query .= "SELECT ";
        $query .= "    COUNT(*) AS CNT, ";
        $query .= "    T1.ENTEXAMYEAR, ";
        $query .= "    T1.TESTDIV, ";
        $query .= "    MIN(T1.ACCEPTNO1) AS ACCEPTNO1, ";
        $query .= "    ".$opt_where[$model->center_title]["SELECT_T_SELECT"]." ";
        $query .= "FROM ";
        $query .= "    MAIN_T T1 ";
        $query .= "    LEFT JOIN ENTEXAM_CONSULTATION_HDAT L1 ON L1.ENTEXAMYEAR = T1.ENTEXAMYEAR ";
        $query .= "         AND L1.TESTDIV = T1.TESTDIV ";
        $query .= "         AND L1.ACCEPTNO = T1.ACCEPTNO1 ";
        $query .= "    LEFT JOIN JOIN_T T2 ON ".$opt_where[$model->center_title]["SELECT_JOIN"]." ";
        $query .= "WHERE ";
        $query .= "    ".$opt_where[$model->center_title]["SELECT_T_WHERE"]." ";
        $query .= "GROUP BY ";
        $query .= "    T1.ENTEXAMYEAR, ";
        $query .= "    T1.TESTDIV, ";
        $query .= "    ".$opt_where[$model->center_title]["SELECT_T_SELECT"]." ";
        $query .= ") ";
        $query .= "SELECT ";
        $query .= "    T1.CNT, ";
        $query .= "    T1.ACCEPTNO1, ";
        $query .= "    L1.NAME AS NAME1, ";
        $query .= "    L1.NAME_KANA AS KANA1, ";
        $query .= "    L1.PS_ACCEPTNO AS PS_ACCEPTNO1, ";
        $query .= "    L1.FS_ACCEPTNO AS FS_ACCEPTNO1, ";
        $query .= "    T2.ACCEPTNO2, ";
        $query .= "    T2.PS_ACCEPTNO2, ";
        $query .= "    T2.FS_ACCEPTNO2, ";
        $query .= "    T2.NAME2, ";
        $query .= "    T2.KANA2 ";
        $query .= "FROM ";
        $query .= "    SELECT_T T1 ";
        $query .= "    LEFT JOIN ENTEXAM_CONSULTATION_HDAT L1 ON L1.ENTEXAMYEAR = T1.ENTEXAMYEAR ";
        $query .= "              AND L1.TESTDIV = T1.TESTDIV ";
        $query .= "              AND L1.ACCEPTNO = T1.ACCEPTNO1 ";
        $query .= "    LEFT JOIN JOIN_T T2 ON ".$opt_where[$model->center_title]["SELECT_JOIN"]." ";
        $query .= "WHERE ";
        $query .= "    T1.CNT = 1 ";
        $query .= "    AND ".$opt_where[$model->center_title]["SELECT_T_WHERE"]." ";
        $query .= "UNION ALL ";
        $query .= "SELECT ";
        $query .= "    T1.CNT, ";
        $query .= "    T1.ACCEPTNO1, ";
        $query .= "    L1.NAME AS NAME1, ";
        $query .= "    L1.NAME_KANA AS KANA1, ";
        $query .= "    L1.PS_ACCEPTNO AS PS_ACCEPTNO1, ";
        $query .= "    L1.FS_ACCEPTNO AS FS_ACCEPTNO1, ";
        $query .= "    'ERROR' AS ACCEPTNO2, ";
        $query .= "    'ERROR' AS PS_ACCEPTNO2, ";
        $query .= "    'ERROR' AS FS_ACCEPTNO2, ";
        $query .= "    '重複データあり'AS NAME2, ";
        $query .= "    '重複データあり'AS KANA2 ";
        $query .= "FROM ";
        $query .= "    SELECT_T T1 ";
        $query .= "    LEFT JOIN ENTEXAM_CONSULTATION_HDAT L1 ON L1.ENTEXAMYEAR = T1.ENTEXAMYEAR ";
        $query .= "              AND L1.TESTDIV = T1.TESTDIV ";
        $query .= "              AND L1.ACCEPTNO = T1.ACCEPTNO1 ";
        $query .= "WHERE ";
        $query .= "    T1.CNT > 1 ";
        $query .= "ORDER BY ";
        $query .= "    ACCEPTNO1 ";

        return $query;
    }

    //UPDATEチェック
    function &getUpdateCheckQuery($model, $copyaccept, $field, $datadiv)
    {
        $retcnt = 0;
        $db = Query::dbCheckOut();

        $query  = "SELECT";
        $query .= "    SUM(SMALLINT(VALUE(JUDGEMENT,'0'))) AS JUDGEMENT,";
        $query .= "    ENTEXAMYEAR,";
        $query .= "    TESTDIV,";
        $query .= "    ACCEPTNO,";
        $query .= "    DATADIV";
        $query .= " FROM ";
        $query .= "     ENTEXAM_CONSULTATION_DAT ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR = '".$model->ObjYear."' ";
        $query .= "     AND TESTDIV = '".$model->testdiv."' ";
        $query .= "     AND ACCEPTNO = '".$copyaccept."' ";
        $query .= "     AND DATADIV = '".$datadiv."' ";
        $query .= " GROUP BY ";
        $query .= "     ENTEXAMYEAR,";
        $query .= "     TESTDIV,";
        $query .= "     ACCEPTNO,";
        $query .= "     DATADIV";
        $retcnt = $db->getOne($query);
        Query::dbCheckIn($db);
        return $retcnt;
    }

    //UPDATE:ENTEXAM_CONSULTATION_HDAT
    function &getHdatUpdateQuery($model, $baseaccept, $copyaccept, $field)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $query  = "UPDATE ";
        $query .= "    ENTEXAM_CONSULTATION_HDAT ";
        $query .= "SET ( ";
        $query .= "      ".$field."_UPDATED, ";
        $query .= "      ".$field."_ACCEPTNO, ";
        $query .= "      ".$field."_CD, ";
        $query .= "      ".$field."_ITEM1, ";
        $query .= "      ".$field."_ITEM2, ";
        $query .= "      ".$field."_ITEM3, ";
        $query .= "      ".$field."_ITEM4, ";
        $query .= "      ".$field."_ITEM5, ";
        $query .= "      registercd, ";
        $query .= "      updated ";
        $query .= "      ) = ";
        $query .= " ( ";
        $query .= "    SELECT ";
        $query .= "      ".$field."_UPDATED, ";
        $query .= "      ".$field."_ACCEPTNO, ";
        $query .= "      ".$field."_CD, ";
        $query .= "      ".$field."_ITEM1, ";
        $query .= "      ".$field."_ITEM2, ";
        $query .= "      ".$field."_ITEM3, ";
        $query .= "      ".$field."_ITEM4, ";
        $query .= "      ".$field."_ITEM5, ";
        $query .= "     '".STAFFCD ."', ";
        $query .= "       SYSDATE() ";
        $query .= "    FROM ";
        $query .= "        ENTEXAM_CONSULTATION_HDAT ";
        $query .= "    WHERE ";
        $query .= "        ENTEXAMYEAR = '".$model->ObjYear."' ";
        $query .= "        AND TESTDIV = '".$model->testdiv."' ";
        $query .= "        AND ACCEPTNO = '".$copyaccept."' ";
        $query .= " ) ";
        $query .= "WHERE ";
        $query .= "    ENTEXAMYEAR = '".$model->ObjYear."' ";
        $query .= "    AND TESTDIV = '".$model->testdiv."' ";
        $query .= "    AND ACCEPTNO = '".$baseaccept."' ";
        $db->query($query);
        $db->commit(); 
        Query::dbCheckIn($db);
    }

    //DELETE : ENTEXAM_CONSULTATION_DAT
    function &getDatDeleteQuery($model, $baseaccept, $datadiv)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $query  = " DELETE FROM ENTEXAM_CONSULTATION_DAT ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR  = '".$model->ObjYear."' ";
        $query .= "     AND TESTDIV  = '".$model->testdiv."' ";
        $query .= "     AND ACCEPTNO = '".$baseaccept."' ";
        $query .= "     AND DATADIV  = '".$datadiv."' ";

        $db->query($query);
        $db->commit(); 
        Query::dbCheckIn($db);
    }

    //INSERT : ENTEXAM_CONSULTATION_DAT
    function &getDatInsertQuery($model, $baseaccept, $copyaccept, $datadiv)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $query  = "INSERT INTO ";
        $query .= "    ENTEXAM_CONSULTATION_DAT ";
        $query .= " ( ";
        $query .= "    SELECT ";
        $query .= "        ENTEXAMYEAR, ";
        $query .= "        TESTDIV, ";
        $query .= "        '".$baseaccept."', ";
        $query .= "        DATADIV, ";
        $query .= "        WISHNO, ";
        $query .= "        ORG_SHDIV, ";
        $query .= "        ORG_MAJORCD, ";
        $query .= "        ORG_JUDGEMENT, ";
        $query .= "        SHDIV, ";
        $query .= "        COURSECD, ";
        $query .= "        MAJORCD, ";
        $query .= "        EXAMCOURSECD, ";
        $query .= "        JUDGEMENT, ";
        $query .= "        '".STAFFCD ."', ";
        $query .= "        SYSDATE() ";
        $query .= "    FROM ";
        $query .= "        ENTEXAM_CONSULTATION_DAT ";
        $query .= "    WHERE ";
        $query .= "        ENTEXAMYEAR  = '".$model->ObjYear."' ";
        $query .= "        AND TESTDIV  = '".$model->testdiv."' ";
        $query .= "        AND ACCEPTNO = '".$copyaccept."' ";
        $query .= "        AND DATADIV  = '".$datadiv."' ";
        $query .= " ) ";

        $db->query($query);
        $db->commit(); 
        Query::dbCheckIn($db);
    }
}
?>
