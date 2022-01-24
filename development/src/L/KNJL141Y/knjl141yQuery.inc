<?php

require_once('for_php7.php');
class knjl141yquery extends Query
{
    //学校名取得
    public function getSchoolName()
    {
        $query  = " SELECT ";
        $query .= "     NAME1 ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = 'Z010' AND ";
        $query .= "     NAMECD2 = '00' ";

        return $query;
    }

    //科目数取得
    public function getKamokuSuu()
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = 'L009' ";
        $query .= " AND NAMESPARE1 = '1' ";
        $query .= " AND NAMECD2 not in ('9') ";
        
        return $query;
    }

    //科目名取得
    public function getKmaokuMei()
    {
        $query  = " SELECT ";
        //中学校のとき
        if ($model->applicantdiv == '1') {
            $query .= "     NAME1 AS NAME ";
        //高校のとき
        } else {
            $query .= "     NAME2 AS NAME ";
        }
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = 'L009' ";
        $query .= " AND NAMESPARE1 = '1' ";
        $query .= " AND NAMECD2 not in ('9') ";
        $query .= " ORDER BY ";
        $query .= "     NAMECD2 ";
        
        return $query;
    }


    //合格コースコンボ
    public function getGoukaku($model)
    {
        $query  = " WITH MAIN_T AS ( ";
        $query .= " SELECT ";
        $query .= "     T1.SUC_COURSECD, ";
        $query .= "     T1.SUC_MAJORCD, ";
        $query .= "     T1.SUC_COURSECODE, ";
        $query .= "     T1.APPLICANTDIV ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_APPLICANTBASE_DAT T1 ";
        $query .= " LEFT JOIN ";
        $query .= "     NAME_MST L1 ON  L1.NAMECD2 = T1.JUDGEMENT ";
        $query .= "                 AND L1.NAMECD1 = 'L013' ";
        $query .= " WHERE ";
        $query .= "     T1.ENTEXAMYEAR = '{$model->examyear}' ";
        $query .= "     AND T1.PROCEDUREDIV = '1' ";
        $query .= "     AND T1.ENTDIV = '1' ";
        $query .= "     AND L1.NAMESPARE1 IS NOT NULL ";
        $query .= " GROUP BY ";
        $query .= "     T1.SUC_COURSECD, ";
        $query .= "     T1.SUC_MAJORCD, ";
        $query .= "     T1.SUC_COURSECODE, ";
        $query .= "     T1.APPLICANTDIV ";
        $query .= " ), MIN_COURSE AS ( ";
        $query .= " SELECT ";
        $query .= "     COURSECD, ";
        $query .= "     MAJORCD, ";
        $query .= "     EXAMCOURSECD, ";
        $query .= "     APPLICANTDIV, ";
        $query .= "     MIN(TESTDIV) AS TESTDIV ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_COURSE_MST ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR = '{$model->examyear}' ";
        $query .= " GROUP BY ";
        $query .= "     COURSECD, ";
        $query .= "     MAJORCD, ";
        $query .= "     EXAMCOURSECD, ";
        $query .= "     APPLICANTDIV ";
        $query .= " ) ";

        $query .= " SELECT ";
        $query .= "     VALUE(T1.SUC_COURSECD, '')   ||  ";
        $query .= "     VALUE(T1.SUC_MAJORCD, '')    || ";
        $query .= "     ':'               || ";
        $query .= "     VALUE(L2.COURSENAME, '')     || ";
        $query .= "     VALUE(L3.MAJORNAME, '')      || ";
        $query .= "     ' '               || ";
        $query .= "     VALUE(L1.EXAMCOURSE_NAME, '') AS LABEL, ";
        $query .= "     VALUE(T1.SUC_COURSECD, '')   || ':' || ";
        $query .= "     VALUE(T1.SUC_MAJORCD, '')    || ':' || ";
        $query .= "     VALUE(T1.SUC_COURSECODE, '') || ':' || ";
        $query .= "     VALUE(T1.APPLICANTDIV, '') AS VALUE ";
        $query .= " FROM ";
        $query .= "     MAIN_T T1 ";
        $query .= "     LEFT JOIN (SELECT ";
        $query .= "                    * ";
        $query .= "                FROM ";
        $query .= "                     ENTEXAM_COURSE_MST LL1 ";
        $query .= "                 WHERE ";
        $query .= "                     LL1.ENTEXAMYEAR = '{$model->examyear}' ";
        $query .= "                     AND EXISTS(SELECT ";
        $query .= "                                 'x' ";
        $query .= "                             FROM ";
        $query .= "                                 MIN_COURSE E1 ";
        $query .= "                             WHERE ";
        $query .= "                                 E1.COURSECD = LL1.COURSECD ";
        $query .= "                                 AND E1.MAJORCD = LL1.MAJORCD ";
        $query .= "                                 AND E1.EXAMCOURSECD = LL1.EXAMCOURSECD ";
        $query .= "                                 AND E1.TESTDIV = LL1.TESTDIV ";
        $query .= "                            ) ";
        $query .= "          ) L1 ON L1.COURSECD = T1.SUC_COURSECD ";
        $query .= "              AND L1.MAJORCD = T1.SUC_MAJORCD ";
        $query .= "              AND L1.EXAMCOURSECD = T1.SUC_COURSECODE ";
        $query .= "              AND L1.APPLICANTDIV = T1.APPLICANTDIV ";
        $query .= "     LEFT JOIN COURSE_MST L2 ON L2.COURSECD = T1.SUC_COURSECD ";
        $query .= "     LEFT JOIN MAJOR_MST L3 ON  L3.MAJORCD = T1.SUC_MAJORCD ";
        $query .= " ORDER BY ";
        $query .= "     T1.APPLICANTDIV, ";
        $query .= "     T1.SUC_COURSECD, ";
        $query .= "     T1.SUC_MAJORCD, ";
        $query .= "     T1.SUC_COURSECODE ";

        return $query;
    }

    public function selectCvsQuery($model, $kamokuSuu)
    {
        $query  = " SELECT ";
        $query .= "     T1.ENTEXAMYEAR, ";
        $query .= "     T1.EXAMNO AS SCHREGNO, ";
        $query .= "     T1.NAME, ";
        $query .= "     T1.NAME_KANA, ";
        $query .= "     SEX, ";
        $query .= "     T1.SUC_COURSECD AS COURSECD, ";
        $query .= "     T1.SUC_MAJORCD AS MAJORCD, ";
        $query .= "     T1.SUC_COURSECODE AS COURSECODE, ";
        for ($i = 1; $i <= $kamokuSuu; $i++) { //科目の数だけループ
            $j = $i + 1;
            $query .= "     M{$j}.SCORE AS SCORE_M{$i}, ";
        }
        //中学校のとき
        if ($model->applicantdiv == '1') {
            $query .= "         M1.TOTAL2 AS SHOUKEI, ";   //小計
        //高校のとき
        } else {
            //条件文 小計点
            $query .= "     CASE WHEN T1.TESTDIV = '3' ";
            $query .= "          THEN  M1.TOTAL1 ";   //小計
            $query .= "          ELSE  M1.TOTAL2 ";   //小計
            $query .= "     END, ";
        }
        $query .= "     S1.SCORE, ";                 //加算点
        $query .= "     ROUND(K1.AVERAGE_ALL, 1), "; //内申点
        $query .= "     '".$model->lastColumn."' AS ".$model->lastColumn." ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_APPLICANTBASE_DAT T1 ";
        $query .= " LEFT JOIN ";
        $query .= "     ENTEXAM_RECEPT_DAT L1 ON  L1.ENTEXAMYEAR   = T1.ENTEXAMYEAR ";
        $query .= "                           AND L1.APPLICANTDIV  = T1.APPLICANTDIV ";
        $query .= "                           AND L1.TESTDIV       = T1.TESTDIV ";
        $query .= "                           AND L1.EXAM_TYPE     = '1' ";
        $query .= "                           AND L1.EXAMNO        = T1.EXAMNO ";
        $query .= " LEFT JOIN ";
        $query .= "     ENTEXAM_RECEPT_DAT M1 ON  M1.ENTEXAMYEAR   = T1.ENTEXAMYEAR ";
        $query .= "                           AND M1.APPLICANTDIV  = T1.APPLICANTDIV ";
        $query .= "                           AND M1.TESTDIV       = T1.TESTDIV ";
        $query .= "                           AND M1.EXAM_TYPE     = '1' ";
        $query .= "                           AND M1.EXAMNO        = T1.EXAMNO ";
        for ($i = 1; $i <= $kamokuSuu; $i++) { //科目の数だけループ
            $j = $i + 1;
            $query .= " LEFT JOIN ";
            $query .= "     ENTEXAM_SCORE_DAT M{$j} ON  M{$j}.ENTEXAMYEAR    = T1.ENTEXAMYEAR ";
            $query .= "                             AND M{$j}.APPLICANTDIV   = T1.APPLICANTDIV ";
            $query .= "                             AND M{$j}.TESTDIV        = T1.TESTDIV ";
            $query .= "                             AND M{$j}.EXAM_TYPE      = M1.EXAM_TYPE ";
            $query .= "                             AND M{$j}.RECEPTNO       = M1.RECEPTNO ";
            $query .= "                             AND M{$j}.TESTSUBCLASSCD = '{$i}' ";
        }
        $query .= " LEFT JOIN ";
        $query .= "     ENTEXAM_APPLICANTADDR_DAT N3 ON  N3.ENTEXAMYEAR = T1.ENTEXAMYEAR ";
        $query .= "                                  AND N3.EXAMNO      = T1.EXAMNO ";
        $query .= " LEFT JOIN ";
        $query .= "     NAME_MST N4 ON  N4.NAMECD2 = T1.JUDGEMENT ";
        $query .= "                 AND N4.NAMECD1 = 'L013' ";
        //加算点を出す
        $query .= " LEFT JOIN ";
        $query .= "     ENTEXAM_SCORE_DAT S1 ON  S1.ENTEXAMYEAR  = T1.ENTEXAMYEAR ";
        $query .= "                          AND S1.APPLICANTDIV = T1.APPLICANTDIV ";
        $query .= "                          AND S1.TESTDIV      = T1.TESTDIV ";
        $query .= "                          AND S1.EXAM_TYPE    = M1.EXAM_TYPE ";
        $query .= "                          AND S1.RECEPTNO     = M1.RECEPTNO ";
        $query .= "                          AND S1.TESTSUBCLASSCD = '9' ";
        //内申点を出す
        $query .= " LEFT JOIN ";
        $query .= "     ENTEXAM_APPLICANTCONFRPT_DAT K1 ON  K1.ENTEXAMYEAR = T1.ENTEXAMYEAR ";
        $query .= "                                     AND K1.EXAMNO      = T1.EXAMNO ";
        //ここまで
        $query .= " WHERE ";
        $query .= "       T1.ENTEXAMYEAR  = '{$model->examyear}' ";
        $query .= "   AND T1.APPLICANTDIV  = '{$model->applicantdiv}' ";
        $query .= "   AND T1.PROCEDUREDIV = '1' "; //手続済み
        $query .= "   AND T1.ENTDIV       = '1' "; //入学有り
        $query .= "   AND T1.SUC_COURSECD = '{$model->suc_coursecd}' ";
        $query .= "   AND T1.SUC_MAJORCD  = '{$model->suc_majorcd}' ";
        $query .= "   AND N4.NAMESPARE1 IS NOT NULL ";

        if ($model->suc_coursecode != $model->lastColumn) {
            $query .= "   AND T1.SUC_COURSECODE = '{$model->suc_coursecode}' ";
        }

        $query .= " ORDER BY ";
        $query .= $this->selected_data;

        return $query;
    }
}
