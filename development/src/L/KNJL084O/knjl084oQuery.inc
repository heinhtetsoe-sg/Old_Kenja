<?php

require_once('for_php7.php');

class knjl084oQuery extends Query {

    //名称
    function GetName($namecd1, $ObjYear, $namecd2 = "")
    {
        $query  = " SELECT ";
        $query .= "     NAMECD2 || '：' || NAME1 AS LABEL, ";
        $query .= "     NAMECD2 AS VALUE, ";
        $query .= "     NAMECD2, ";
        $query .= "     NAME1, ";
        $query .= "     NAME2, ";
        $query .= "     NAMESPARE1, ";
        $query .= "     NAMESPARE2, ";
        $query .= "     NAMESPARE3 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$ObjYear."' ";
        $query .= "     AND NAMECD1 = '".$namecd1."' ";
        if ($namecd2) {
            $query .= "     AND NAMECD2 = '".$namecd2."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //入試区分マスタ
    function getTestdivMst($ObjYear, $testdiv = "")
    {
        $query  = " SELECT ";
        $query .= "     TESTDIV || '：' || NAME AS LABEL, ";
        $query .= "     TESTDIV AS VALUE, ";
        $query .= "     NAME AS NAME1, ";
        $query .= "     CLASSDIV AS NAMESPARE3 ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_TESTDIV_MST ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR = '".$ObjYear."' ";
        if ($testdiv) {
            $query .= "     AND TESTDIV = '".$testdiv."' ";
        }
        $query .= "     AND SHOWDIV = '1' ";
        $query .= " ORDER BY ";
        $query .= "     SHOWORDER, ";
        $query .= "     TESTDIV ";
        return $query;
    }

    //処理対象
    function getAppliType($model)
    {
        $query  = " WITH APPLI_TYPE (LABEL, VALUE) AS ( ";
        $query .= "     VALUES('通常合格', '1'), ('アップ合格', '2'), ('スライド合格', '3'), ('非正規合格', '6') ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     APPLI_TYPE ";
        $query .= " WHERE ";
        if ($model->senbatuDiv == "2") {
            $query .= "     VALUE IN ('1', '3') ";
        } else if ($model->senbatuDiv == "1") {
            $query .= "     VALUE IN ('1', '2') ";
        } else {
            $query .= "     VALUE IN ('6') ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //候補者
    function GetRightList($model)
    {
        $query  = " WITH RECEPS AS ( ";
        $query .= " SELECT ";
        $query .= "     T1.EXAMNO, ";
        $query .= "     '複' AS RECEPS, ";
        $query .= "     '1' AS ORDERDIV ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_RECEPT_DAT T1 ";
        $query .= " WHERE ";
        $query .= "     T1.ENTEXAMYEAR = '".$model->ObjYear."' ";
        $query .= "     AND T1.APPLICANTDIV = '".$model->applicantdiv."' ";
        $query .= " GROUP BY ";
        $query .= "     T1.EXAMNO ";
        $query .= " HAVING ";
        $query .= "     COUNT(T1.EXAMNO) > 1 ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     T1.EXAMNO || ";
        $query .= "     '：(' || ";
        $query .= "     CAST(RIGHT('   ' || RTRIM(CAST(VALUE(T1.TOTAL4, 0) AS CHAR(3))), 3) AS CHAR(3)) || ";
        $query .= "     '点)' || ";
        $query .= "     '　' || ";
        $query .= "     '(' || L2.NAME2 || ')' || ";
        $query .= "     '　' || VALUE(L3.RECEPS, '　') || '　' || ";
        $query .= "     L1.NAME AS LABEL, ";
        $query .= "     CAST(RIGHT('0000' || RTRIM(CAST(VALUE(T1.TOTAL_RANK4, 9999) AS CHAR(4))), 4) AS CHAR(4)) || ':' || ";
        $query .= "     VALUE(L3.ORDERDIV, '9') || ':' || ";
        $query .= "     T1.EXAMNO || ':' || ";
        $query .= "     '0' AS VALUE, ";
        $query .= "     T1.EXAMNO, ";
        $query .= "     L1.NAME ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_RECEPT_DAT T1 ";
        $query .= "     LEFT JOIN ENTEXAM_APPLICANTBASE_DAT L1 ON T1.ENTEXAMYEAR = L1.ENTEXAMYEAR ";
        $query .= "          AND T1.EXAMNO = L1.EXAMNO ";
        $query .= "     LEFT JOIN NAME_MST L2 ON L2.NAMECD1 = 'Z002' ";
        $query .= "          AND L1.SEX = L2.NAMECD2 ";
        $query .= "     LEFT JOIN RECEPS L3 ON T1.EXAMNO = L3.EXAMNO ";
        $query .= " WHERE ";
        $query .= "     T1.ENTEXAMYEAR = '".$model->ObjYear."' ";
        $query .= "     AND T1.APPLICANTDIV = '".$model->applicantdiv."' ";
        $query .= "     AND T1.TESTDIV = '".$model->testdiv."' ";
        $query .= "     AND T1.TOTAL4 IS NOT NULL ";
        if ($model->appli_type == "1") {
            $query .= "     AND T1.JUDGELINE = '2' ";
            $query .= "     AND T1.JUDGECLASS IS NULL ";
        }
        if ($model->appli_type == "2") {
            $query .= "     AND T1.JUDGELINE = '1' ";
            $query .= "     AND T1.JUDGECLASS = '1' ";
        }
        if ($model->appli_type == "3") {
            $query .= "     AND T1.JUDGELINE = '2' ";
            $query .= "     AND T1.JUDGECLASS IS NULL ";
        }
        if ($model->appli_type == "6") {
            $query .= "     AND T1.JUDGELINE = '2' ";
            $query .= "     AND T1.JUDGECLASS IS NULL ";
        }
        $query .= " ORDER BY ";
        $query .= "     T1.TOTAL4 DESC, ";
        $query .= "     L3.ORDERDIV, ";
        $query .= "     T1.EXAMNO ";

        return $query;
    }

    //対象者
    function GetLeftList($model)
    {
        $query  = " WITH RECEPS AS ( ";
        $query .= " SELECT ";
        $query .= "     T1.EXAMNO, ";
        $query .= "     '複' AS RECEPS, ";
        $query .= "     '1' AS ORDERDIV ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_RECEPT_DAT T1 ";
        $query .= " WHERE ";
        $query .= "     T1.ENTEXAMYEAR = '".$model->ObjYear."' ";
        $query .= "     AND T1.APPLICANTDIV = '".$model->applicantdiv."' ";
        $query .= " GROUP BY ";
        $query .= "     T1.EXAMNO ";
        $query .= " HAVING ";
        $query .= "     COUNT(T1.EXAMNO) > 1 ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     T1.EXAMNO || ";
        $query .= "     '：(' || ";
        $query .= "     CAST(RIGHT('   ' || RTRIM(CAST(VALUE(T1.TOTAL4, 0) AS CHAR(3))), 3) AS CHAR(3)) || ";
        $query .= "     '点)' || ";
        $query .= "     '　' || ";
        $query .= "     '(' || L2.NAME2 || ')' || ";
        $query .= "     '　' || VALUE(L3.RECEPS, '　') || '　' || ";
        $query .= "     L1.NAME AS LABEL, ";
        $query .= "     CAST(RIGHT('0000' || RTRIM(CAST(VALUE(T1.TOTAL_RANK4, 9999) AS CHAR(4))), 4) AS CHAR(4)) || ':' || ";
        $query .= "     VALUE(L3.ORDERDIV, '9') || ':' || ";
        $query .= "     T1.EXAMNO || ':' || ";
        $query .= "     '1' AS VALUE, ";
        $query .= "     T1.EXAMNO, ";
        $query .= "     L1.NAME ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_RECEPT_DAT T1 ";
        $query .= "     LEFT JOIN ENTEXAM_APPLICANTBASE_DAT L1 ON T1.ENTEXAMYEAR = L1.ENTEXAMYEAR ";
        $query .= "          AND T1.EXAMNO = L1.EXAMNO ";
        $query .= "     LEFT JOIN NAME_MST L2 ON L2.NAMECD1 = 'Z002' ";
        $query .= "          AND L1.SEX = L2.NAMECD2 ";
        $query .= "     LEFT JOIN RECEPS L3 ON T1.EXAMNO = L3.EXAMNO ";
        $query .= " WHERE ";
        $query .= "     T1.ENTEXAMYEAR = '".$model->ObjYear."' ";
        $query .= "     AND T1.APPLICANTDIV = '".$model->applicantdiv."' ";
        $query .= "     AND T1.TESTDIV = '".$model->testdiv."' ";
        $query .= "     AND T1.TOTAL4 IS NOT NULL ";
        if ($model->appli_type == "1") {
            $query .= "     AND T1.JUDGELINE = '2' ";
            $query .= "     AND T1.JUDGECLASS IN ('1', '2') ";
        }
        if ($model->appli_type == "2") {
            $query .= "     AND T1.JUDGELINE = '1' ";
            $query .= "     AND T1.JUDGECLASS = '3' ";
        }
        if ($model->appli_type == "3") {
            $query .= "     AND T1.JUDGELINE = '2' ";
            $query .= "     AND T1.JUDGECLASS = '4' ";
        }
        if ($model->appli_type == "6") {
            $query .= "     AND T1.JUDGELINE = '2' ";
            $query .= "     AND T1.JUDGECLASS = '6' "; //6:非正規合格
        }
        $query .= " ORDER BY ";
        $query .= "     T1.TOTAL4 DESC, ";
        $query .= "     L3.ORDERDIV, ";
        $query .= "     T1.EXAMNO ";

        return $query;
    }

    function getCourseMajor($ObjYear, $examno) {
        $query  = " SELECT ";
        $query .= "     L1.COURSECD, ";
        $query .= "     L1.MAJORCD, ";
        $query .= "     L1.EXAMCOURSECD ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_APPLICANTBASE_DAT T1 ";
        $query .= "     LEFT JOIN ENTEXAM_WISHDIV_MST L1 ON T1.ENTEXAMYEAR = L1.ENTEXAMYEAR ";
        $query .= "          AND L1.WISHNO = '1' ";
        $query .= "          AND T1.DESIREDIV = L1.DESIREDIV ";
        $query .= " WHERE ";
        $query .= "     T1.ENTEXAMYEAR = '".$ObjYear."' ";
        $query .= "     AND T1.EXAMNO = '".$examno."' ";

        return $query;
    }

    //UPDATE
    function &getUpdateQuery($model)
    {
        //対象者(左のリスト）
        $array  = (strlen($model->selectdata) ? explode(",", $model->selectdata) : array());
        //合格者(右のリスト)
        $array2 = (strlen($model->selectdata2) ? explode(",", $model->selectdata2) : array());
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        switch ($model->appli_type)
        {
            case "1":  //通常合格
                foreach ($array as $examno) {
                    $query  = " UPDATE ENTEXAM_RECEPT_DAT ";
                    $query .= "   SET ";
                    $query .= "      JUDGEDIV   = '1', ";
                    $query .= "      JUDGECLASS   = '".$model->senbatuDiv."', ";
                    $query .= "      REGISTERCD = '".STAFFCD."', ";
                    $query .= "      UPDATED    = SYSDATE() "; 
                    $query .= " WHERE ENTEXAMYEAR  = '".$model->ObjYear."'";
                    $query .= "   AND APPLICANTDIV = '".$model->applicantdiv."'";
                    $query .= "   AND TESTDIV      = '".$model->testdiv."'";
                    $query .= "   AND EXAMNO       = '".$examno."'"; 
                    $db->query($query);

                    $course = $db->getRow(knjl084oQuery::getCourseMajor($model->ObjYear, $examno), DB_FETCHMODE_ASSOC);

                    $query  = "UPDATE ENTEXAM_APPLICANTBASE_DAT ";
                    $query .= "   SET ";
                    $query .= "      JUDGEMENT   = '1', ";
                    $query .= "      ENTCLASS   = '".$model->senbatuDiv."', ";
                    $query .= "      SUC_COURSECD   = '".$course["COURSECD"]."', ";
                    $query .= "      SUC_MAJORCD   = '".$course["MAJORCD"]."', ";
                    $query .= "      SUC_COURSECODE   = '".$course["EXAMCOURSECD"]."', ";
                    $query .= "      REGISTERCD = '".STAFFCD."', ";
                    $query .= "      UPDATED    = SYSDATE() "; 
                    $query .= " WHERE ENTEXAMYEAR  = '".$model->ObjYear."'";
                    $query .= "   AND EXAMNO       = '".$examno."'"; 
                    $query .= "   AND value(ENTCLASS,'0') NOT IN ('2')"; //入学クラスが”2:選抜クラス”の場合は更新しない。
                    $db->query($query);
                }

            break;

            case "2":  //アップ合格
                foreach ($array as $examno) {
                    $query  = " UPDATE ENTEXAM_RECEPT_DAT ";
                    $query .= "   SET ";
                    $query .= "      JUDGEDIV   = '1', ";
                    $query .= "      JUDGECLASS   = '3', ";
                    $query .= "      REGISTERCD = '".STAFFCD."', ";
                    $query .= "      UPDATED    = SYSDATE() "; 
                    $query .= " WHERE ENTEXAMYEAR  = '".$model->ObjYear."'";
                    $query .= "   AND APPLICANTDIV = '".$model->applicantdiv."'";
                    $query .= "   AND TESTDIV      = '".$model->testdiv."'";
                    $query .= "   AND EXAMNO       = '".$examno."'"; 
                    $db->query($query);

                    $course = $db->getRow(knjl084oQuery::getCourseMajor($model->ObjYear, $examno), DB_FETCHMODE_ASSOC);

                    $query  = "UPDATE ENTEXAM_APPLICANTBASE_DAT ";
                    $query .= "   SET ";
                    $query .= "      JUDGEMENT   = '1', ";
                    $query .= "      ENTCLASS   = '2', ";
                    $query .= "      SUC_COURSECD   = '".$course["COURSECD"]."', ";
                    $query .= "      SUC_MAJORCD   = '".$course["MAJORCD"]."', ";
                    $query .= "      SUC_COURSECODE   = '".$course["EXAMCOURSECD"]."', ";
                    $query .= "      REGISTERCD = '".STAFFCD."', ";
                    $query .= "      UPDATED    = SYSDATE() "; 
                    $query .= " WHERE ENTEXAMYEAR  = '".$model->ObjYear."'";
                    $query .= "   AND EXAMNO       = '".$examno."'"; 
                    $query .= "   AND value(ENTCLASS,'0') NOT IN ('2')"; //入学クラスが”2:選抜クラス”の場合は更新しない。
                    $db->query($query);
                }

            break;

            case "3":  //スライド合格
                foreach ($array as $examno) {
                    $query  = " UPDATE ENTEXAM_RECEPT_DAT ";
                    $query .= "   SET ";
                    $query .= "      JUDGEDIV   = '1', ";
                    $query .= "      JUDGECLASS   = '4', ";
                    $query .= "      REGISTERCD = '".STAFFCD."', ";
                    $query .= "      UPDATED    = SYSDATE() "; 
                    $query .= " WHERE ENTEXAMYEAR  = '".$model->ObjYear."'";
                    $query .= "   AND APPLICANTDIV = '".$model->applicantdiv."'";
                    $query .= "   AND TESTDIV      = '".$model->testdiv."'";
                    $query .= "   AND EXAMNO       = '".$examno."'"; 
                    $db->query($query);

                    $course = $db->getRow(knjl084oQuery::getCourseMajor($model->ObjYear, $examno), DB_FETCHMODE_ASSOC);

                    $query  = "UPDATE ENTEXAM_APPLICANTBASE_DAT ";
                    $query .= "   SET ";
                    $query .= "      JUDGEMENT   = '1', ";
                    $query .= "      ENTCLASS   = '1', ";
                    $query .= "      SUC_COURSECD   = '".$course["COURSECD"]."', ";
                    $query .= "      SUC_MAJORCD   = '".$course["MAJORCD"]."', ";
                    $query .= "      SUC_COURSECODE   = '".$course["EXAMCOURSECD"]."', ";
                    $query .= "      REGISTERCD = '".STAFFCD."', ";
                    $query .= "      UPDATED    = SYSDATE() "; 
                    $query .= " WHERE ENTEXAMYEAR  = '".$model->ObjYear."'";
                    $query .= "   AND EXAMNO       = '".$examno."'"; 
                    $query .= "   AND value(ENTCLASS,'0') NOT IN ('2')"; //入学クラスが”2:選抜クラス”の場合は更新しない。
                    $db->query($query);
                }

            break;

            case "6":  //非正規合格
                foreach ($array as $examno) {
                    $query  = " UPDATE ENTEXAM_RECEPT_DAT ";
                    $query .= "   SET ";
                    $query .= "      JUDGEDIV       = '1', ";
                    $query .= "      JUDGECLASS     = '6', "; //6:非正規合格
                    $query .= "      REGISTERCD     = '".STAFFCD."', ";
                    $query .= "      UPDATED        = SYSDATE() "; 
                    $query .= " WHERE ENTEXAMYEAR  = '".$model->ObjYear."'";
                    $query .= "   AND APPLICANTDIV = '".$model->applicantdiv."'";
                    $query .= "   AND TESTDIV      = '".$model->testdiv."'";
                    $query .= "   AND EXAMNO       = '".$examno."'"; 
                    $db->query($query);

                    $course = $db->getRow(knjl084oQuery::getCourseMajor($model->ObjYear, $examno), DB_FETCHMODE_ASSOC);

                    $query  = "UPDATE ENTEXAM_APPLICANTBASE_DAT ";
                    $query .= "   SET ";
                    $query .= "      JUDGEMENT      = '1', ";
                    $query .= "      ENTCLASS       = '1', "; //1:特別進学クラス
                    $query .= "      SUC_COURSECD   = '".$course["COURSECD"]."', ";
                    $query .= "      SUC_MAJORCD    = '".$course["MAJORCD"]."', ";
                    $query .= "      SUC_COURSECODE = '".$course["EXAMCOURSECD"]."', ";
                    $query .= "      REGISTERCD     = '".STAFFCD."', ";
                    $query .= "      UPDATED        = SYSDATE() "; 
                    $query .= " WHERE ENTEXAMYEAR  = '".$model->ObjYear."'";
                    $query .= "   AND EXAMNO       = '".$examno."'"; 
                    $query .= "   AND value(ENTCLASS,'0') NOT IN ('2')"; //入学クラスが”2:選抜クラス”の場合は更新しない。
                    $db->query($query);
                }

            break;
        }

        $db->commit(); 

        Query::dbCheckIn($db);
    }
}
?>
