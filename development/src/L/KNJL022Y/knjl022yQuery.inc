<?php
class knjl022yQuery extends Query {

    //名称取得
    function GetName($namecd1, $ObjYear)
    {
        $query  = " SELECT ";
        $query .= "     NAMECD2, ";
        $query .= "     NAME1, ";
        $query .= "     NAME2, ";
        $query .= "     NAMESPARE1, ";
        $query .= "     NAMESPARE2, ";
        $query .= "     NAMESPARE3 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR    = '".$ObjYear."' AND ";
        $query .= "     NAMECD1 = '".$namecd1."' ";

        return $query;
    }

    //最小受験番号
    function getMinExam($model)
    {
        $query  = " SELECT ";
        $query .= "     MIN(T1.EXAMNO) AS EXAMNO ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_APPLICANTBASE_DAT T1 ";
        $query .= " WHERE ";
        $query .= "     T1.ENTEXAMYEAR = '".$model->ObjYear."' AND ";
        $query .= "     T1.APPLICANTDIV = '".$model->applicantdiv."' ";

        return $query;
    }

    //一覧表示
    function SelectQuery($model)
    {
        $query  = " SELECT ";
        $query .= "     T1.EXAMNO, ";
        $query .= "     T1.NAME, ";
        $query .= "     T1.NAME_KANA, ";
        $query .= "     T1.SEX AS SEXCD,  ";
        $query .= "     N1.NAME2 AS SEX, ";
        $query .= "     T2.ACTIVITY, ";
        $query .= "     T2.SECTION, ";
        $query .= "     T2.RESULTS ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_APPLICANTBASE_DAT T1 ";
        $query .= "     LEFT JOIN ENTEXAM_APPLICANTACTIVITY_DAT T2 ON ";
        $query .= "             T1.ENTEXAMYEAR = T2.ENTEXAMYEAR AND ";
        $query .= "             T1.EXAMNO      = T2.EXAMNO ";
        $query .= "     LEFT JOIN NAME_MST N1 ON ";
        $query .= "             N1.NAMECD1 = 'Z002' AND ";
        $query .= "             N1.NAMECD2 = T1.SEX ";
        $query .= "     LEFT JOIN ENTEXAM_APPLICANTDESIRE_DAT W1 ";
        $query .= "         ON  W1.ENTEXAMYEAR  = T1.ENTEXAMYEAR ";
        $query .= "         AND W1.APPLICANTDIV = T1.APPLICANTDIV ";
        $query .= "         AND W1.TESTDIV      = T1.TESTDIV ";
        $query .= "         AND W1.EXAMNO       = T1.EXAMNO ";
        $query .= " WHERE ";
        $query .= "     T1.ENTEXAMYEAR = '".$model->ObjYear."' AND ";
        $query .= "     T1.APPLICANTDIV = '".$model->applicantdiv."' AND ";
        $query .= "     W1.TESTDIV = '".$model->testdiv."' ";
        //スポーツ等優秀者希望者 及び スポーツ推薦受験者
        $query .= "     AND (T1.SPORTS_FLG = '1' OR W1.RECOM_KIND = '2') ";
        $query .= " ORDER BY T1.EXAMNO";

        return $query;
    }

    //更新(一度すべて削除してから追加）
    function getUpdateQuery(&$model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //削除
        $query  = " DELETE FROM ENTEXAM_APPLICANTACTIVITY_DAT";
        $query .= " WHERE ENTEXAMYEAR  = '".$model->ObjYear."'";
        $query .= "   AND EXAMNO IN ('".implode("','",array_keys($model->score))."')";
        $db->query($query);

        foreach ($model->score as $examno => $val)
        {
            $query  = "INSERT INTO ENTEXAM_APPLICANTACTIVITY_DAT";
            $query .= " VALUES ('".$model->ObjYear."'";
            $query .= "        ,'".$examno."'";
            $query .= "        ,'".$val["ACTIVITY"]."'";    //活動
            $query .= "        ,'".$val["RESULTS"]."'";     //主な実績
            $query .= "        ,'".$val["SECTION"]."'";     //所属
            $query .= "        ,'".STAFFCD."'";
            $query .= "        ,SYSDATE()";
            $query .= ")";
            $db->query($query);
        }

        $db->commit();
        Query::dbCheckIn($db);
        return true;
    }
}
?>