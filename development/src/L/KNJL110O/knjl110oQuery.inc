<?php

require_once('for_php7.php');

class knjl110oQuery extends Query 
{
    //名称マスタよりデータを取得
    function GetName($ObjYear, $namecd1, $namecd2 = "")
    {
        $query  = " SELECT ";
        $query .= "     NAMECD2 || '：' || NAME1 AS LABEL, ";
        $query .= "     NAMECD2 AS VALUE, ";
        $query .= "     NAME1 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR    = '{$ObjYear}' ";
        $query .= "     AND NAMECD1 = '{$namecd1}' ";
        if ($namecd2) {
            $query .= "     AND NAMECD2 = '{$namecd2}' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";
        return $query;
    }

    //入試区分マスタ
    function getTestdivMst($ObjYear)
    {
        $query  = " SELECT ";
        $query .= "     SHOWORDER, ";
        $query .= "     TESTDIV || '：' || NAME AS LABEL, ";
        $query .= "     TESTDIV AS VALUE ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_TESTDIV_MST ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR = '{$ObjYear}' ";
        $query .= "     AND SHOWDIV = '1' ";
        $query .= " ORDER BY ";
        $query .= "     SHOWORDER, ";
        $query .= "     VALUE ";
        return $query;
    }

    //入試区分マスタ
    function getClassDivName($ObjYear, $model)
    {
        $query  = " SELECT ";
        $query .= "     CLASSDIV ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_TESTDIV_MST ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR = '{$ObjYear}' ";
        $query .= "     AND TESTDIV = '{$model->field["TESTDIV"]}' ";
        return $query;
    }

    //データ取得
    function getJudgeAvg($model)
    {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_JUDGE_AVARAGE_DAT ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR = '{$model->year}' ";
        $query .= "     AND APPLICANTDIV = '{$model->field["APPLICANTDIV"]}' ";
        $query .= "     AND TESTDIV = '{$model->field["TESTDIV"]}' ";
        $query .= "     AND EXAM_TYPE = '{$model->field["EXAM_TYPE"]}' ";
        $query .= " ORDER BY ";
        $query .= "     TESTSUBCLASSCD ";
        return $query;
    }

    //出願コース取得
    function selectQueryCourse($model) {
        $query  = " SELECT DISTINCT ";
        $query .= "     t3.coursecd || t3.majorcd || t3.examcoursecd ";
        $query .= " FROM ";
        $query .= "     (SELECT DISTINCT ";
        $query .= "         t1.entexamyear, ";
        $query .= "         t1.desirediv ";
        $query .= "     FROM ";
        $query .= "         entexam_applicantbase_dat t1 ";
        $query .= "     WHERE ";
        $query .= "         examno IN (SELECT examno ";
        $query .= "                      FROM entexam_recept_dat ";
        $query .= "                     WHERE ";
        $query .= "                           entexamyear  = '".$model->year."' AND ";
        $query .= "                           applicantdiv = '".$model->field["APPLICANTDIV"]."' AND ";
        $query .= "                           exam_type    = '".$model->field["EXAM_TYPE"]."' AND ";
        $query .= "                           testdiv      = '".$model->field["TESTDIV"]."'  ";
        $query .= "                   ) ";
        $query .= "     ) t1, ";
        $query .= "     entexam_wishdiv_mst t2, ";
        $query .= "     entexam_course_mst  t3 ";
        $query .= " WHERE ";
        $query .= "     t2.entexamyear  = t1.entexamyear AND ";
        $query .= "     t2.desirediv    = t1.desirediv AND ";
        $query .= "     t3.entexamyear  = t2.entexamyear AND ";
        $query .= "     t3.coursecd     = t2.coursecd AND ";
        $query .= "     t3.majorcd      = t2.majorcd AND ";
        $query .= "     t3.examcoursecd = t2.examcoursecd and";
        $query .= "     t2.wishno       = '1'";
        $query .= " ORDER BY ";
        $query .= "     t3.coursecd || t3.majorcd || t3.examcoursecd ";

        return $query;
    }

    //再計算
    function getSaikeisanSql($model, $setClassDiv) {

        $setJudgeclass = "6";
        if ($setClassDiv == "1") {
            $setJudgeclass = "3";
        } else if ($setClassDiv == "2") {
            $setJudgeclass = "4";
        }
        $query  = " WITH STD_T AS ( ";
        $query .= " SELECT ";
        $query .= "     T1.RECEPTNO, ";
        $query .= "     L1.SEX, ";
        $query .= "     T1.ENTEXAMYEAR, ";
        $query .= "     T1.APPLICANTDIV, ";
        $query .= "     T1.TESTDIV, ";
        $query .= "     T1.EXAM_TYPE, ";
        $query .= "     T1.HONORDIV, ";
        $query .= "     T1.JUDGECLASS, ";
        $query .= "     T1.TOTAL4 ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_RECEPT_DAT T1 ";
        $query .= "     LEFT JOIN ENTEXAM_APPLICANTBASE_DAT L1 ON T1.ENTEXAMYEAR = L1.ENTEXAMYEAR ";
        $query .= "          AND T1.EXAMNO = L1.EXAMNO ";
        $query .= " WHERE ";
        $query .= "     T1.ENTEXAMYEAR = '{$model->year}' ";
        $query .= "     AND T1.APPLICANTDIV = '{$model->field["APPLICANTDIV"]}' ";
        $query .= "     AND T1.TESTDIV = '{$model->field["TESTDIV"]}' ";
        $query .= "     AND T1.EXAM_TYPE = '{$model->field["EXAM_TYPE"]}' ";
        $query .= "     AND T1.JUDGEDIV = '1' ";
        $query .= " GROUP BY ";
        $query .= "     T1.RECEPTNO, ";
        $query .= "     L1.SEX, ";
        $query .= "     T1.ENTEXAMYEAR, ";
        $query .= "     T1.APPLICANTDIV, ";
        $query .= "     T1.TESTDIV, ";
        $query .= "     T1.EXAM_TYPE, ";
        $query .= "     T1.HONORDIV, ";
        $query .= "     T1.JUDGECLASS, ";
        $query .= "     T1.TOTAL4 ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     T1.TESTSUBCLASSCD, ";
        $query .= "     '9' AS SEX, ";
        $query .= "     MAX(T1.SCORE1) AS MAX_SCORE, ";
        $query .= "     MIN(T1.SCORE1) AS MIN_SCORE, ";
        $query .= "     ROUND(AVG(DECIMAL(T1.SCORE1)),1) AS AVG1 ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_SCORE_SPARE_DAT T1, ";
        $query .= "     STD_T T2 ";
        $query .= " WHERE ";
        $query .= "     T1.ENTEXAMYEAR = T2.ENTEXAMYEAR ";
        $query .= "     AND T1.RECEPTNO = T2.RECEPTNO ";
        $query .= "     AND T1.APPLICANTDIV = T2.APPLICANTDIV ";
        $query .= "     AND T1.TESTDIV = T2.TESTDIV ";
        $query .= "     AND T1.EXAM_TYPE = T2.EXAM_TYPE ";
        $query .= "     AND T1.SEQ = '001' ";
        $query .= "     AND T2.JUDGECLASS NOT IN ('4','6') "; //4:スライド合格 6:非正規合格 を除く
        $query .= " GROUP BY ";
        $query .= "     T1.TESTSUBCLASSCD ";
        $query .= " UNION ALL ";
        $query .= " SELECT ";
        $query .= "     T1.TESTSUBCLASSCD, ";
        $query .= "     T2.SEX, ";
        $query .= "     MAX(T1.SCORE1) AS MAX_SCORE, ";
        $query .= "     MIN(T1.SCORE1) AS MIN_SCORE, ";
        $query .= "     ROUND(AVG(DECIMAL(T1.SCORE1)),1) AS AVG1 ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_SCORE_SPARE_DAT T1, ";
        $query .= "     STD_T T2 ";
        $query .= " WHERE ";
        $query .= "     T1.ENTEXAMYEAR = T2.ENTEXAMYEAR ";
        $query .= "     AND T1.RECEPTNO = T2.RECEPTNO ";
        $query .= "     AND T1.APPLICANTDIV = T2.APPLICANTDIV ";
        $query .= "     AND T1.TESTDIV = T2.TESTDIV ";
        $query .= "     AND T1.EXAM_TYPE = T2.EXAM_TYPE ";
        $query .= "     AND T1.SEQ = '001' ";
        $query .= "     AND T2.JUDGECLASS NOT IN ('4','6') "; //4:スライド合格 6:非正規合格 を除く
        $query .= " GROUP BY ";
        $query .= "     T1.TESTSUBCLASSCD, ";
        $query .= "     T2.SEX ";
        $query .= " UNION ALL ";
        $query .= " SELECT ";
        $query .= "     'S' AS TESTSUBCLASSCD, ";
        $query .= "     '9' AS SEX, ";
        $query .= "     MAX(T1.SCORE1) AS MAX_SCORE, ";
        $query .= "     MIN(T1.SCORE1) AS MIN_SCORE, ";
        $query .= "     ROUND(AVG(DECIMAL(T1.SCORE1)),1) AS AVG1 ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_SCORE_SPARE_DAT T1, ";
        $query .= "     STD_T T2 ";
        $query .= " WHERE ";
        $query .= "     T2.JUDGECLASS = '{$setJudgeclass}' ";
        $query .= "     AND T1.ENTEXAMYEAR = T2.ENTEXAMYEAR ";
        $query .= "     AND T1.RECEPTNO = T2.RECEPTNO ";
        $query .= "     AND T1.APPLICANTDIV = T2.APPLICANTDIV ";
        $query .= "     AND T1.TESTDIV = T2.TESTDIV ";
        $query .= "     AND T1.EXAM_TYPE = T2.EXAM_TYPE ";
        $query .= "     AND T1.SEQ = '001' ";
        $query .= "     AND T1.TESTSUBCLASSCD = 'A' ";
        $query .= " UNION ALL ";
        $query .= " SELECT ";
        $query .= "     'T' AS TESTSUBCLASSCD, ";
        $query .= "     '9' AS SEX, ";
        $query .= "     MAX(T1.SCORE1) AS MAX_SCORE, ";
        $query .= "     MIN(T1.SCORE1) AS MIN_SCORE, ";
        $query .= "     ROUND(AVG(DECIMAL(T1.SCORE1)),1) AS AVG1 ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_SCORE_SPARE_DAT T1, ";
        $query .= "     STD_T T2 ";
        $query .= " WHERE ";
        $query .= "     T2.HONORDIV = '1' ";
        $query .= "     AND T1.ENTEXAMYEAR = T2.ENTEXAMYEAR ";
        $query .= "     AND T1.RECEPTNO = T2.RECEPTNO ";
        $query .= "     AND T1.APPLICANTDIV = T2.APPLICANTDIV ";
        $query .= "     AND T1.TESTDIV = T2.TESTDIV ";
        $query .= "     AND T1.EXAM_TYPE = T2.EXAM_TYPE ";
        $query .= "     AND T1.SEQ = '001' ";
        $query .= "     AND T1.TESTSUBCLASSCD = 'A' ";
        //ライン種別
        foreach ($model->lineCntArr as $key => $val) {
            $query .= " UNION ALL ";
            $query .= " SELECT ";
            $query .= "     '{$val}' AS TESTSUBCLASSCD, ";
            $query .= "     '9' AS SEX, ";
            $query .= "     0 AS MAX_SCORE, ";
            $query .= "     BORDER_SCORE AS MIN_SCORE, ";
            $query .= "     0 AS AVG1 ";
            $query .= " FROM ";
            $query .= "     ENTEXAM_PASSINGMARK_MST ";
            $query .= " WHERE ";
            $query .= "         ENTEXAMYEAR  = '".$model->year."' ";
            $query .= "     AND APPLICANTDIV = '".$model->field["APPLICANTDIV"]."' ";
            $query .= "     AND TESTDIV      = '".$model->field["TESTDIV"]."' ";
            $query .= "     AND EXAM_TYPE    = '".$model->field["EXAM_TYPE"]."' ";
            $query .= "     AND SHDIV        = '".$key."' ";
            $query .= "     AND COURSECD || MAJORCD || EXAMCOURSECD = '{$model->course}' ";
        }
        $query .= " ORDER BY ";
        $query .= "     TESTSUBCLASSCD, ";
        $query .= "     SEX ";

        return $query;

    }

    function getUpdateQuery($model) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $query  = " DELETE FROM ";
        $query .= "     ENTEXAM_JUDGE_AVARAGE_DAT ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR = '{$model->year}' ";
        $query .= "     AND APPLICANTDIV = '{$model->field["APPLICANTDIV"]}' ";
        $query .= "     AND TESTDIV = '{$model->field["TESTDIV"]}' ";
        $query .= "     AND EXAM_TYPE = '{$model->field["EXAM_TYPE"]}' ";

        $db->query($query);

        foreach ($model->kamoku as $key => $val) {
            $data = array();
            $data["ENTEXAMYEAR"][TEXT]      = $model->year;
            $data["APPLICANTDIV"][TEXT]     = $model->field["APPLICANTDIV"];
            $data["TESTDIV"][TEXT]          = $model->field["TESTDIV"];
            $data["EXAM_TYPE"][TEXT]        = $model->field["EXAM_TYPE"];
            $data["TESTSUBCLASSCD"][TEXT]   = $key;
            $data["AVARAGE_MEN"][NUMBER]    = $model->meisaiField["M_AVG".$key];
            $data["AVARAGE_WOMEN"][NUMBER]  = $model->meisaiField["W_AVG".$key];
            $data["AVARAGE_TOTAL"][NUMBER]  = $model->meisaiField["T_AVG".$key];
            $data["MAX_SCORE"][NUMBER]      = $model->meisaiField["MAX".$key];
            $data["MIN_SCORE"][NUMBER]      = $model->meisaiField["MIN".$key];
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][FUNC]          = "sysdate()";

            $query = Query::insertSQL($data, "ENTEXAM_JUDGE_AVARAGE_DAT");
            $db->query($query);
        }
        $db->commit();
        Query::dbCheckIn($db);

    }
}
?>