<?php

require_once('for_php7.php');
class knjl141rquery extends Query
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

    //合格コースコンボ
    public function getGoukaku($model)
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     L0.REMARK1    || '-' || ";
        $query .= "     L0.REMARK2    || '-' || ";
        $query .= "     L0.REMARK3    || ";
        $query .= "     ':'               || ";
        $query .= "     L3.COURSENAME     || ";
        $query .= "     L4.MAJORNAME      || ";
        $query .= "     ' '               || ";
        $query .= "     L5.COURSECODENAME AS LABEL, ";
        $query .= "     L0.REMARK1 || ':' || ";
        $query .= "     L0.REMARK2 || ':' || ";
        $query .= "     L0.REMARK3 AS VALUE ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_APPLICANTBASE_DAT T1 ";
        $query .= " LEFT JOIN ";
        $query .= "     ENTEXAM_APPLICANTBASE_DETAIL_DAT L0 ON L0.ENTEXAMYEAR  = T1.ENTEXAMYEAR ";
        $query .= "                                        AND L0.APPLICANTDIV = T1.APPLICANTDIV ";
        $query .= "                                        AND L0.EXAMNO = T1.EXAMNO ";
        $query .= "                                        AND L0.SEQ = '007' ";
        $query .= " LEFT JOIN ";
        $query .= "     NAME_MST L1 ON  L1.NAMECD2 = T1.JUDGEMENT ";
        $query .= "                 AND L1.NAMECD1 = 'L013' ";
        $query .= " LEFT JOIN ";
        $query .= "     ENTEXAM_COURSE_MST L2 ON  L2.ENTEXAMYEAR        = T1.ENTEXAMYEAR ";
        $query .= "                           AND L2.TESTDIV            = T1.TESTDIV ";
        $query .= "                           AND L2.ENTER_COURSECD     = L0.REMARK1 ";
        $query .= "                           AND L2.ENTER_MAJORCD      = L0.REMARK2 ";
        $query .= "                           AND L2.ENTER_COURSECODE   = L0.REMARK3 ";
        $query .= " LEFT JOIN ";
        $query .= "     COURSE_MST L3 ON L3.COURSECD =  L0.REMARK1 ";
        $query .= " LEFT JOIN ";
        $query .= "     MAJOR_MST L4 ON  L4.COURSECD =  L0.REMARK1 ";
        $query .= "                  AND L4.MAJORCD  =  L0.REMARK2 ";
        $query .= " LEFT JOIN ";
        $query .= "     COURSECODE_MST L5 ON L5.COURSECODE =  L0.REMARK3 ";
        $query .= " WHERE ";
        $query .= "     T1.ENTEXAMYEAR = '{$model->examyear}' ";
        $query .= " AND T1.APPLICANTDIV = '{$model->fixApplicantDiv}' ";
        $query .= " AND T1.PROCEDUREDIV = '1' ";
        $query .= " AND T1.ENTDIV = '1' ";
        $query .= " AND L1.NAMESPARE1 IS NOT NULL ";
        $query .= " AND L2.EXAMCOURSE_NAME IS NOT NULL ";
        $query .= " GROUP BY ";
        $query .= "     L0.REMARK1, ";
        $query .= "     L0.REMARK2, ";
        $query .= "     L0.REMARK3, ";
        $query .= "     T1.TESTDIV, ";
        $query .= "     L3.COURSENAME, ";
        $query .= "     L4.MAJORNAME, ";
        $query .= "     L5.COURSECODENAME ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //入学者のCSV
    public function selectCvsStudentQuery($model)
    {
        $query  = " SELECT ";
        $query .= "     T1.ENTEXAMYEAR, ";
        if ($model->schoolName == 'hirogaku' && SCHOOLKIND == 'J') {
            $query .= "     BD030.REMARK2 AS SCHREGNO, ";
        } else {
            $query .= "     T1.EXAMNO AS SCHREGNO, ";
        }
        $query .= "     T1.NAME, ";
        $query .= "     T1.NAME_KANA, ";
        $query .= "     T1.SEX, ";
        $query .= "     CAST(REPLACE(CAST(T1.BIRTHDAY AS CHAR(10)), '-', '/') AS CHAR(10)) AS BIRTHDAY, ";
        $query .= "     T1.FS_CD, ";
        $query .= "     L3.FINSCHOOL_NAME, ";
        $query .= "     L1.ZIPCD, ";
        $query .= "     L1.ADDRESS1, ";
        $query .= "     L1.ADDRESS2, ";
        $query .= "     L1.TELNO, ";
        $query .= "     L1.GNAME, ";
        $query .= "     L1.GKANA, ";
        $query .= "     L4.NAME1 AS RELATIONSHIP, ";
        $query .= "     L5.NAME1 AS TESTDIV, ";
        $query .= "     CASE WHEN B1.BEFORE_EXAMCOURSECD IS NOT NULL THEN L6.ABBV3 ELSE '' END AS SUB, ";
        $query .= "     L7.NAME1 AS SHOUGAKUSEI, ";
        $query .= "     T1.SUC_COURSECD, ";
        $query .= "     T1.SUC_MAJORCD, ";
        $query .= "     T1.SUC_COURSECODE, ";
        $query .= "     C0.EXAMCOURSE_NAME, ";
        $query .= "     L0.REMARK1, ";
        $query .= "     L0.REMARK2, ";
        $query .= "     L0.REMARK3, ";
        $query .= "     G1.COURSENAME || G2.MAJORNAME || ' ' || G3.COURSECODENAME AS ENTERCOURSE_NAME, ";
        $query .= "     C1.EXAMCOURSE_MARK AS KIBOU1, ";
        $query .= "     C2.EXAMCOURSE_MARK AS KIBOU2, ";
        $query .= "     C3.EXAMCOURSE_MARK AS KIBOU3, ";
        //得点
        $query .= "     S1.SCORE AS SCORE1, ";
        $query .= "     S2.SCORE AS SCORE2, ";
        $query .= "     S3.SCORE AS SCORE3, ";
        $query .= "     S4.SCORE AS SCORE4, ";
        $query .= "     S5.SCORE AS SCORE5, ";
        //内申点90点
        $query .= "     R1.KASANTEN_ALL, ";
        //合計
        $query .= "     RE1.TOTAL4, ";
        //面接
        $query .= "     I1.INTERVIEW_VALUE, ";
        //欠席日数
        $query .= "     R1.ABSENCE_DAYS3, ";
        $query .= "     '".$model->lastColumn."' AS ".$model->lastColumn." ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_APPLICANTBASE_DAT T1 ";
        $query .= " LEFT JOIN ";
        $query .= "     ENTEXAM_APPLICANTBASE_DETAIL_DAT L0 ON L0.ENTEXAMYEAR  = T1.ENTEXAMYEAR ";
        $query .= "                                        AND L0.APPLICANTDIV = T1.APPLICANTDIV ";
        $query .= "                                        AND L0.EXAMNO       = T1.EXAMNO ";
        $query .= "                                        AND L0.SEQ          = '007' ";
        $query .= " LEFT JOIN ";
        $query .= "     ENTEXAM_APPLICANTBASE_DETAIL_DAT L002 ON L002.ENTEXAMYEAR = T1.ENTEXAMYEAR ";
        $query .= "                                        AND L002.APPLICANTDIV  = T1.APPLICANTDIV ";
        $query .= "                                        AND L002.EXAMNO        = T1.EXAMNO ";
        $query .= "                                        AND L002.SEQ           = '002' ";
        $query .= " LEFT JOIN ";
        $query .= "     ENTEXAM_APPLICANTBASE_DETAIL_DAT BD030 ON BD030.ENTEXAMYEAR  = T1.ENTEXAMYEAR ";
        $query .= "                                           AND BD030.APPLICANTDIV = T1.APPLICANTDIV ";
        $query .= "                                           AND BD030.EXAMNO       = T1.EXAMNO ";
        $query .= "                                           AND BD030.SEQ          = '030' ";
        $query .= " LEFT JOIN ";
        $query .= "     ENTEXAM_APPLICANTADDR_DAT L1 ON  L1.ENTEXAMYEAR  = T1.ENTEXAMYEAR ";
        $query .= "                                  AND L1.APPLICANTDIV = T1.APPLICANTDIV ";
        $query .= "                                  AND L1.EXAMNO       = T1.EXAMNO ";
        $query .= " LEFT JOIN ";
        $query .= "     NAME_MST L2 ON  L2.NAMECD2 = T1.JUDGEMENT ";
        $query .= "                 AND L2.NAMECD1 = 'L013' ";
        $query .= " LEFT JOIN ";
        $query .= "     V_FINSCHOOL_MST L3 ON L3.YEAR = T1.ENTEXAMYEAR ";
        $query .= "                       AND L3.FINSCHOOLCD = T1.FS_CD ";
        $query .= " LEFT JOIN ";
        $query .= "     NAME_MST L4 ON  L4.NAMECD2 = L1.RELATIONSHIP ";
        $query .= "                 AND L4.NAMECD1 = 'H201' ";
        $query .= " LEFT JOIN ";
        $query .= "     NAME_MST L5 ON  L5.NAMECD2 = T1.TESTDIV ";
        $query .= "                 AND L5.NAMECD1 = 'L004' ";
        $query .= " LEFT JOIN ";
        $query .= "     ENTEXAM_APPLICANT_BEFORE_DAT B1 ON B1.ENTEXAMYEAR = T1.ENTEXAMYEAR ";
        $query .= "                                    AND B1.APPLICANTDIV = T1.APPLICANTDIV ";
        $query .= "                                    AND B1.TESTDIV = T1.TESTDIV ";
        $query .= "                                    AND B1.BEFORE_PAGE = L002.REMARK1 ";
        $query .= "                                    AND B1.BEFORE_SEQ = L002.REMARK2 ";
        $query .= " LEFT JOIN ";
        $query .= "     NAME_MST L6 ON  L6.NAMECD2 = B1.TESTDIV ";
        $query .= "                 AND L6.NAMECD1 = 'L004' ";
        $query .= " LEFT JOIN ";
        $query .= "     NAME_MST L7 ON  L7.NAMECD2 = B1.SCHOLARSHIP ";
        $query .= "                 AND L7.NAMECD1 = 'L031' ";
        $query .= " LEFT JOIN ";
        $query .= "     ENTEXAM_COURSE_MST C0 ON C0.ENTEXAMYEAR = T1.ENTEXAMYEAR ";
        $query .= "                          AND C0.APPLICANTDIV = T1.APPLICANTDIV ";
        $query .= "                          AND C0.TESTDIV = T1.TESTDIV ";
        $query .= "                          AND C0.COURSECD = T1.SUC_COURSECD ";
        $query .= "                          AND C0.MAJORCD = T1.SUC_MAJORCD ";
        $query .= "                          AND C0.EXAMCOURSECD = T1.SUC_COURSECODE ";
        $query .= " LEFT JOIN ";
        $query .= "     COURSE_MST G1 ON G1.COURSECD =  L0.REMARK1 ";
        $query .= " LEFT JOIN ";
        $query .= "     MAJOR_MST G2 ON  G2.COURSECD =  L0.REMARK1 ";
        $query .= "                  AND G2.MAJORCD  =  L0.REMARK2 ";
        $query .= " LEFT JOIN ";
        $query .= "     COURSECODE_MST G3 ON G3.COURSECODE = L0.REMARK3 ";
        //志望区分
        $query .= " LEFT JOIN ";
        $query .= "     ENTEXAM_WISHDIV_MST W1 ON W1.ENTEXAMYEAR = T1.ENTEXAMYEAR ";
        $query .= "                           AND W1.APPLICANTDIV = T1.APPLICANTDIV ";
        $query .= "                           AND W1.TESTDIV = T1.TESTDIV ";
        $query .= "                           AND W1.DESIREDIV = T1.DESIREDIV ";
        $query .= "                           AND W1.WISHNO = '1' ";
        $query .= " LEFT JOIN ";
        $query .= "     ENTEXAM_WISHDIV_MST W2 ON W2.ENTEXAMYEAR = T1.ENTEXAMYEAR ";
        $query .= "                           AND W2.APPLICANTDIV = T1.APPLICANTDIV ";
        $query .= "                           AND W2.TESTDIV = T1.TESTDIV ";
        $query .= "                           AND W2.DESIREDIV = T1.DESIREDIV ";
        $query .= "                           AND W2.WISHNO = '2' ";
        $query .= " LEFT JOIN ";
        $query .= "     ENTEXAM_WISHDIV_MST W3 ON W3.ENTEXAMYEAR = T1.ENTEXAMYEAR ";
        $query .= "                           AND W3.APPLICANTDIV = T1.APPLICANTDIV ";
        $query .= "                           AND W3.TESTDIV = T1.TESTDIV ";
        $query .= "                           AND W3.DESIREDIV = T1.DESIREDIV ";
        $query .= "                           AND W3.WISHNO = '3' ";
        $query .= " LEFT JOIN ";
        $query .= "     ENTEXAM_COURSE_MST C1  ON C1.ENTEXAMYEAR = W1.ENTEXAMYEAR ";
        $query .= "                           AND C1.APPLICANTDIV = W1.APPLICANTDIV ";
        $query .= "                           AND C1.TESTDIV = W1.TESTDIV ";
        $query .= "                           AND C1.COURSECD = W1.COURSECD ";
        $query .= "                           AND C1.MAJORCD = W1.MAJORCD ";
        $query .= "                           AND C1.EXAMCOURSECD = W1.EXAMCOURSECD ";
        $query .= " LEFT JOIN ";
        $query .= "     ENTEXAM_COURSE_MST C2  ON C2.ENTEXAMYEAR = W2.ENTEXAMYEAR ";
        $query .= "                           AND C2.APPLICANTDIV = W2.APPLICANTDIV ";
        $query .= "                           AND C2.TESTDIV = W2.TESTDIV ";
        $query .= "                           AND C2.COURSECD = W2.COURSECD ";
        $query .= "                           AND C2.MAJORCD = W2.MAJORCD ";
        $query .= "                           AND C2.EXAMCOURSECD = W2.EXAMCOURSECD ";
        $query .= " LEFT JOIN ";
        $query .= "     ENTEXAM_COURSE_MST C3  ON C3.ENTEXAMYEAR = W3.ENTEXAMYEAR ";
        $query .= "                           AND C3.APPLICANTDIV = W3.APPLICANTDIV ";
        $query .= "                           AND C3.TESTDIV = W3.TESTDIV ";
        $query .= "                           AND C3.COURSECD = W3.COURSECD ";
        $query .= "                           AND C3.MAJORCD = W3.MAJORCD ";
        $query .= "                           AND C3.EXAMCOURSECD = W3.EXAMCOURSECD ";
        //得点データ
        $query .= " LEFT JOIN ";
        $query .= "     ENTEXAM_SCORE_DAT S1   ON S1.ENTEXAMYEAR = T1.ENTEXAMYEAR ";
        $query .= "                           AND S1.APPLICANTDIV = T1.APPLICANTDIV ";
        $query .= "                           AND S1.TESTDIV = T1.TESTDIV ";
        $query .= "                           AND S1.EXAM_TYPE = '1' ";
        $query .= "                           AND S1.RECEPTNO = T1.EXAMNO ";
        $query .= "                           AND S1.TESTSUBCLASSCD = '1' ";
        $query .= " LEFT JOIN ";
        $query .= "     ENTEXAM_SCORE_DAT S2   ON S2.ENTEXAMYEAR = T1.ENTEXAMYEAR ";
        $query .= "                           AND S2.APPLICANTDIV = T1.APPLICANTDIV ";
        $query .= "                           AND S2.TESTDIV = T1.TESTDIV ";
        $query .= "                           AND S2.EXAM_TYPE = '1' ";
        $query .= "                           AND S2.RECEPTNO = T1.EXAMNO ";
        $query .= "                           AND S2.TESTSUBCLASSCD = '2' ";
        $query .= " LEFT JOIN ";
        $query .= "     ENTEXAM_SCORE_DAT S3   ON S3.ENTEXAMYEAR = T1.ENTEXAMYEAR ";
        $query .= "                           AND S3.APPLICANTDIV = T1.APPLICANTDIV ";
        $query .= "                           AND S3.TESTDIV = T1.TESTDIV ";
        $query .= "                           AND S3.EXAM_TYPE = '1' ";
        $query .= "                           AND S3.RECEPTNO = T1.EXAMNO ";
        $query .= "                           AND S3.TESTSUBCLASSCD = '3' ";
        $query .= " LEFT JOIN ";
        $query .= "     ENTEXAM_SCORE_DAT S4   ON S4.ENTEXAMYEAR = T1.ENTEXAMYEAR ";
        $query .= "                           AND S4.APPLICANTDIV = T1.APPLICANTDIV ";
        $query .= "                           AND S4.TESTDIV = T1.TESTDIV ";
        $query .= "                           AND S4.EXAM_TYPE = '1' ";
        $query .= "                           AND S4.RECEPTNO = T1.EXAMNO ";
        $query .= "                           AND S4.TESTSUBCLASSCD = '4' ";
        $query .= " LEFT JOIN ";
        $query .= "     ENTEXAM_SCORE_DAT S5   ON S5.ENTEXAMYEAR = T1.ENTEXAMYEAR ";
        $query .= "                           AND S5.APPLICANTDIV = T1.APPLICANTDIV ";
        $query .= "                           AND S5.TESTDIV = T1.TESTDIV ";
        $query .= "                           AND S5.EXAM_TYPE = '1' ";
        $query .= "                           AND S5.RECEPTNO = T1.EXAMNO ";
        $query .= "                           AND S5.TESTSUBCLASSCD = '5' ";
        //内申点データ
        $query .= " LEFT JOIN ";
        $query .= "     ENTEXAM_APPLICANTCONFRPT_DAT R1  ON R1.ENTEXAMYEAR  = T1.ENTEXAMYEAR ";
        $query .= "                                     AND R1.APPLICANTDIV = T1.APPLICANTDIV ";
        $query .= "                                     AND R1.EXAMNO       = T1.EXAMNO ";
        //合計
        $query .= " LEFT JOIN ";
        $query .= "     ENTEXAM_RECEPT_DAT RE1    ON RE1.ENTEXAMYEAR = T1.ENTEXAMYEAR ";
        $query .= "                              AND RE1.APPLICANTDIV = T1.APPLICANTDIV ";
        $query .= "                              AND RE1.TESTDIV = T1.TESTDIV ";
        $query .= "                              AND RE1.EXAM_TYPE = '1' ";
        $query .= "                              AND RE1.RECEPTNO = T1.EXAMNO ";
        //面接データ
        $query .= " LEFT JOIN ";
        $query .= "     ENTEXAM_INTERVIEW_DAT I1  ON I1.ENTEXAMYEAR = T1.ENTEXAMYEAR ";
        $query .= "                              AND I1.APPLICANTDIV = T1.APPLICANTDIV ";
        $query .= "                              AND I1.TESTDIV = T1.TESTDIV ";
        $query .= "                              AND I1.EXAMNO = T1.EXAMNO ";
        $query .= " WHERE ";
        $query .= "       T1.ENTEXAMYEAR  = '{$model->examyear}' ";
        $query .= "   AND T1.APPLICANTDIV = '{$model->fixApplicantDiv}' ";
        $query .= "   AND T1.PROCEDUREDIV = '1' "; //手続済み
        $query .= "   AND T1.ENTDIV       = '1' "; //入学有り
        if ($model->suc_coursecd !== '9' && $model->suc_majorcd !== '999' && $model->suc_coursecode !== '9999') {
            $query .= "   AND L0.REMARK1      = '{$model->suc_coursecd}' ";
            $query .= "   AND L0.REMARK2      = '{$model->suc_majorcd}' ";
            $query .= "   AND L0.REMARK3      = '{$model->suc_coursecode}' ";
        //全てを選択
        } else {
            $query .= "   AND L0.REMARK1 IS NOT NULL ";
            $query .= "   AND L0.REMARK2 IS NOT NULL ";
            $query .= "   AND L0.REMARK3 IS NOT NULL ";
        }
        $query .= "   AND L2.NAMESPARE1 IS NOT NULL ";
        $query .= " ORDER BY ";
        $query .= $this->selected_data;

        return $query;
    }

    //科目名を取得
    public function getKamokuQuery()
    {
        $query  = " SELECT ";
        $query .= "     NAME1 ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = 'L009' ";
        $query .= " ORDER BY ";
        $query .= "     NAMECD2 ";

        return $query;
    }
}
