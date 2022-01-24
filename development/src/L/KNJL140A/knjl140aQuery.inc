<?php

require_once('for_php7.php');
class knjl140aquery extends Query
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
        $query  = " SELECT ";
        $query .= "     L0.REMARK1    || ";
        $query .= "     L0.REMARK2    || ";
        $query .= "     ':'               || ";
        $query .= "     L3.COURSENAME     || ";
        $query .= "     L4.MAJORNAME      || ";
        $query .= "     ' '               || ";
        $query .= "     L5.COURSECODENAME AS LABEL, ";
        $query .= "     L0.REMARK1 || ':' || ";
        $query .= "     L0.REMARK2 || ':' || ";
        $query .= "     L0.REMARK3 || ':' || ";
        $query .= "     T1.TESTDIV AS VALUE ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_APPLICANTBASE_DAT T1 ";
        $query .= " LEFT JOIN ";
        $query .= "     ENTEXAM_APPLICANTBASE_DETAIL_DAT L0 ON L0.ENTEXAMYEAR  = T1.ENTEXAMYEAR ";
        $query .= "                                        AND L0.APPLICANTDIV = T1.APPLICANTDIV ";
        $query .= "                                        AND L0.EXAMNO       = T1.EXAMNO ";
        $query .= "                                        AND L0.SEQ          = '007' ";
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
        $query .= " AND T1.PROCEDUREDIV = '1' ";
        $query .= " AND T1.ENTDIV = '1' ";
        $query .= " AND L1.NAMESPARE1 IS NOT NULL ";
        $query .= " AND L2.EXAMCOURSE_NAME IS NOT NULL ";
        if ($model->schoolName == 'hirogaku') {
            $query .= "   AND T1.APPLICANTDIV = '{$model->fixApplicantDiv}' ";
        }
        $query .= " GROUP BY ";
        $query .= "     L0.REMARK1, ";
        $query .= "     L0.REMARK2, ";
        $query .= "     L0.REMARK3, ";
        $query .= "     T1.TESTDIV, ";
        $query .= "     L3.COURSENAME, ";
        $query .= "     L4.MAJORNAME, ";
        $query .= "     L5.COURSECODENAME ";
        $query .= " ORDER BY ";
        $query .= "     L0.REMARK1, ";
        $query .= "     L0.REMARK2, ";
        $query .= "     L0.REMARK3, ";
        $query .= "     T1.TESTDIV ";
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
        $query .= "     '' AS ENTDIV, ";
        $query .= "     '' AS GRADE, ";
        $query .= "     '' AS HR_CLASS, ";
        $query .= "     '' AS ATTENDNO, ";
        $query .= "     '0' AS INOUTCD, ";
        $query .= "     L0.REMARK1 AS COURSECD, ";
        $query .= "     L0.REMARK2 AS MAJORCD, ";
        $query .= "     L0.REMARK3 AS COURSECODE, ";
        $query .= "     T1.NAME, ";
        $query .= "     T1.NAME_KANA, ";
        $query .= "     CAST(REPLACE(CAST(T1.BIRTHDAY AS CHAR(10)), '-', '/') AS CHAR(10)) AS BIRTHDAY, ";
        $query .= "     T1.SEX, "; //コードで出力する
        $query .= "     T1.FS_CD, ";
        $query .= "     CASE WHEN VALUE(T1.FS_GRDYEAR, '') != '' ";
        $query .= "          THEN FS_GRDYEAR || '/3/31' ";
        $query .= "          ELSE '' ";
        $query .= "     END AS FINSCHOOLGRADDATE, ";
        $query .= "     L1.ZIPCD, ";
        $query .= "     L1.ADDRESS1, ";
        $query .= "     L1.ADDRESS2, ";
        $query .= "     L1.TELNO, ";
        $query .= "     '' AS FAXNO, ";
        $query .= "     '' AS EMAIL, ";
        $query .= "     L1.EMERGENCYCALL, ";
        $query .= "     L1.EMERGENCYTELNO, ";
        $query .= "     '' AS SCALASHIPDIV, ";
        $query .= "     '".$model->lastColumn."' AS ".$model->lastColumn." ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_APPLICANTBASE_DAT T1 ";
        $query .= " LEFT JOIN ";
        $query .= "     ENTEXAM_APPLICANTBASE_DETAIL_DAT L0 ON L0.ENTEXAMYEAR  = T1.ENTEXAMYEAR ";
        $query .= "                                        AND L0.APPLICANTDIV = T1.APPLICANTDIV ";
        $query .= "                                        AND L0.EXAMNO       = T1.EXAMNO ";
        $query .= "                                        AND L0.SEQ          = '007' ";
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
        $query .= "     NAME_MST L2 ON  L2.NAMECD2 = T1.JUDGEMENT "; //合格
        $query .= "                 AND L2.NAMECD1 = 'L013' ";
        $query .= " WHERE ";
        $query .= "       T1.ENTEXAMYEAR  = '{$model->examyear}' ";
        $query .= "   AND T1.PROCEDUREDIV = '1' "; //手続済み
        $query .= "   AND T1.ENTDIV       = '1' "; //入学有り
        $query .= "   AND L2.NAMESPARE1 IS NOT NULL ";
        if ($model->schoolName == 'hirogaku') {
            $query .= "   AND T1.APPLICANTDIV = '{$model->fixApplicantDiv}' ";
        }
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
        $query .= " ORDER BY ";
        $query .= $this->selected_data;

        return $query;
    }

    //保護者のCSV
    public function selectCvsGuardQuery($model)
    {
        $query  = " SELECT ";
        $query .= "     '' AS GRADE, ";
        $query .= "     '' AS HR_CLASS, ";
        $query .= "     '' AS ATTENDNO, ";
        $query .= "     '' AS NAME, ";
        if ($model->schoolName == 'hirogaku' && SCHOOLKIND == 'J') {
            $query .= "     BD030.REMARK2 AS SCHREGNO, ";
        } else {
            $query .= "     T1.EXAMNO AS SCHREGNO, ";
        }
        $query .= "     value(L1.RELATIONSHIP, '01') AS RELATIONSHIP, ";
        $query .= "     L1.GNAME, ";
        $query .= "     L1.GKANA, ";
        $query .= "     '' AS SEITO_SEX, ";
        $query .= "     '' AS BIRTHDAY, ";
        $query .= "     L1.GZIPCD, ";
        $query .= "     L1.GADDRESS1, ";
        $query .= "     L1.GADDRESS2, ";
        $query .= "     L1.GTELNO, ";
        $query .= "     L1.GFAXNO, ";
        $query .= "     '' AS EMAIL, ";
        $query .= "     '' AS GUARD_JOBCD, ";
        $query .= "     '' AS GUARD_WORK_NAME, ";
        $query .= "     '' AS GUARD_WORK_TELNO, ";
        $query .= "     value(L1.RELATIONSHIP, '01') AS GUARANTOR_RELATIONSHIP, ";
        $query .= "     L1.GNAME AS GUARANTOR_NAME, ";
        $query .= "     L1.GKANA AS GUARANTOR_KANA, ";
        $query .= "     '' AS GUARANTOR_SEX, ";
        $query .= "     L1.GZIPCD AS GUARANTOR_ZIPCD, ";
        $query .= "     L1.GADDRESS1 AS GUARANTOR_ADDR1, ";
        $query .= "     L1.GADDRESS2 AS GUARANTOR_ADDR2, ";
        $query .= "     L1.GTELNO AS GUARANTOR_TELNO, ";
        $query .= "     '' AS GUARANTOR_JOBCD, ";
        $query .= "     '' AS PUBLIC_OFFICE, ";
        $query .= "     '".$model->lastColumn."' AS ".$model->lastColumn." ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_APPLICANTBASE_DAT T1 ";
        $query .= " LEFT JOIN ";
        $query .= "     ENTEXAM_APPLICANTBASE_DETAIL_DAT L0 ON L0.ENTEXAMYEAR  = T1.ENTEXAMYEAR ";
        $query .= "                                        AND L0.APPLICANTDIV = T1.APPLICANTDIV ";
        $query .= "                                        AND L0.EXAMNO       = T1.EXAMNO ";
        $query .= "                                        AND L0.SEQ          = '007' ";
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
        $query .= "     NAME_MST L2 ON  L2.NAMECD2 = T1.JUDGEMENT "; //合格
        $query .= "                 AND L2.NAMECD1 = 'L013' ";
        $query .= " WHERE ";
        $query .= "       T1.ENTEXAMYEAR  = '{$model->examyear}' ";
        $query .= "   AND T1.PROCEDUREDIV = '1' "; //手続済み
        $query .= "   AND T1.ENTDIV       = '1' "; //入学有り
        if ($model->schoolName == 'hirogaku') {
            $query .= "   AND T1.APPLICANTDIV = '{$model->fixApplicantDiv}' ";
        }
        $query .= "   AND L2.NAMESPARE1 IS NOT NULL ";
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
        $query .= " ORDER BY ";
        $query .= $this->selected_data;

        return $query;
    }
}
