<?php

require_once('for_php7.php');
class knjl140wQuery extends Query
{
    //権限チェック（入試管理者）
    public function getAdminFlg()
    {
        $query  = " SELECT ";
        $query .= "     FIELD1 ";
        $query .= " FROM ";
        $query .= "     STAFF_DETAIL_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND STAFFCD = '".STAFFCD."' ";
        $query .= "     AND STAFF_SEQ = '009' ";

        $db = Query::dbCheckOut();
        $rtnFlg = $db->getOne($query);
        Query::dbCheckIn($db);

        return $rtnFlg;
    }

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

    //入学コースコンボ
    public function getGoukaku($model)
    {
        $query  = " SELECT ";
        $query .= "     L0.REMARK1    || ";
        $query .= "     L0.REMARK2    || ";
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
        $query .= "     ENTEXAM_APPLICANTBASE_DETAIL_DAT L0 ON L0.ENTEXAMYEAR = T1.ENTEXAMYEAR ";
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
        $query .= " AND T1.PROCEDUREDIV = '1' ";
        $query .= " AND T1.ENTDIV       = '1' "; //入学有り
        $query .= " AND L1.NAMESPARE1 IS NOT NULL ";
        $query .= " AND L2.EXAMCOURSE_NAME IS NOT NULL ";
        $query .= " GROUP BY ";
        $query .= "     L0.REMARK1, ";
        $query .= "     L0.REMARK2, ";
        $query .= "     L0.REMARK3, ";
        $query .= "     L3.COURSENAME, ";
        $query .= "     L4.MAJORNAME, ";
        $query .= "     L5.COURSECODENAME ";
        $query .= " ORDER BY ";
        $query .= "     L0.REMARK1, ";
        $query .= "     L0.REMARK2, ";
        $query .= "     L0.REMARK3 ";

        return $query;
    }

    //入学者のCSV
    public function selectCvsStudentQuery($model)
    {
        $query  = " SELECT ";
        $query .= "     T1.ENTEXAMYEAR, ";
        $query .= "     T1.EXAMNO AS SCHREGNO, ";
        $query .= "     '1' AS ENTDIV, ";
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
        $query .= "     T1.ENTEXAMYEAR AS KATEI_ENTEXAMYEAR, ";
        $query .= "     '' AS TIKUCD, ";
        $query .= "     '' AS EXAMNO, ";
        $query .= "     '".$model->lastColumn."' AS ".$model->lastColumn." ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_APPLICANTBASE_DAT T1 ";
        $query .= " LEFT JOIN ";
        $query .= "     ENTEXAM_APPLICANTBASE_DETAIL_DAT L0 ON L0.ENTEXAMYEAR = T1.ENTEXAMYEAR ";
        $query .= "                                        AND L0.EXAMNO = T1.EXAMNO ";
        $query .= "                                        AND L0.SEQ = '007' ";
        $query .= " LEFT JOIN ";
        $query .= "     ENTEXAM_APPLICANTADDR_DAT L1 ON  L1.ENTEXAMYEAR = T1.ENTEXAMYEAR ";
        $query .= "                                  AND L1.EXAMNO = T1.EXAMNO ";
        $query .= " LEFT JOIN ";
        $query .= "     NAME_MST L2 ON  L2.NAMECD2 = T1.JUDGEMENT "; //合格
        $query .= "                 AND L2.NAMECD1 = 'L013' ";
        $query .= " WHERE ";
        $query .= "       T1.ENTEXAMYEAR  = '{$model->examyear}' ";
        $query .= "   AND T1.PROCEDUREDIV = '1' "; //手続済み
        $query .= "   AND T1.ENTDIV       = '1' "; //入学有り
        if ($model->goukaku == 'ALL_J') {
            $query .= "   AND T1.APPLICANTDIV = '1' ";
        } elseif ($model->goukaku == 'ALL_H') {
            $query .= "   AND T1.APPLICANTDIV = '2' ";
        } else {
            $query .= "   AND L0.REMARK1  = '{$model->suc_coursecd}' ";
            $query .= "   AND L0.REMARK2  = '{$model->suc_majorcd}' ";
            $query .= "   AND L0.REMARK3  = '{$model->suc_coursecode}' ";
        }
        $query .= "   AND L2.NAMESPARE1 IS NOT NULL ";
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
        $query .= "     T1.NAME, ";
        $query .= "     T1.EXAMNO AS SCHREGNO, ";
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
        $query .= "     ENTEXAM_APPLICANTBASE_DETAIL_DAT L0 ON L0.ENTEXAMYEAR = T1.ENTEXAMYEAR ";
        $query .= "                                        AND L0.EXAMNO = T1.EXAMNO ";
        $query .= "                                        AND L0.SEQ = '007' ";
        $query .= " LEFT JOIN ";
        $query .= "     ENTEXAM_APPLICANTADDR_DAT L1 ON  L1.ENTEXAMYEAR = T1.ENTEXAMYEAR ";
        $query .= "                                  AND L1.EXAMNO = T1.EXAMNO ";
        $query .= " LEFT JOIN ";
        $query .= "     NAME_MST L2 ON  L2.NAMECD2 = T1.JUDGEMENT "; //合格
        $query .= "                 AND L2.NAMECD1 = 'L013' ";
        $query .= " WHERE ";
        $query .= "       T1.ENTEXAMYEAR  = '{$model->examyear}' ";
        $query .= "   AND T1.PROCEDUREDIV = '1' "; //手続済み
        $query .= "   AND T1.ENTDIV       = '1' "; //入学有り
        if ($model->goukaku == 'ALL_J') {
            $query .= "   AND T1.APPLICANTDIV = '1' ";
        } elseif ($model->goukaku == 'ALL_H') {
            $query .= "   AND T1.APPLICANTDIV = '2' ";
        } else {
            $query .= "   AND L0.REMARK1  = '{$model->suc_coursecd}' ";
            $query .= "   AND L0.REMARK2  = '{$model->suc_majorcd}' ";
            $query .= "   AND L0.REMARK3  = '{$model->suc_coursecode}' ";
        }
        $query .= "   AND L2.NAMESPARE1 IS NOT NULL ";

        $query .= " ORDER BY ";
        $query .= $this->selected_data;

        return $query;
    }

    //通学手段情報のCSV
    public function selectCsvTugakuQuery($model)
    {
        $query  = " SELECT ";
        $query .= "     '3' AS C1, ";
        $query .= "     '' AS C2, ";
        $query .= "     '' AS C3, ";
        $query .= "     '' AS C4, ";
        $query .= "     '' AS C5, ";
        $query .= "     T1.EXAMNO AS SCHREGNO, ";
        $query .= "     T1.NAME AS NAME, ";
        $query .= "     '' AS C8, ";
        $query .= "     '' AS C9, ";
        $query .= "     '' AS C10, ";
        $query .= "     '' AS C11, ";
        $query .= "     '' AS C12, ";
        $query .= "     '' AS C13, ";
        $query .= "     '' AS C14, ";
        $query .= "     '' AS C15, ";
        $query .= "     '' AS C16, ";
        $query .= "     '' AS C17, ";
        $query .= "     '' AS C18, ";
        $query .= "     '' AS C19, ";
        $query .= "     '' AS C20, ";
        $query .= "     '' AS C21, ";
        $query .= "     '' AS C22, ";
        $query .= "     '' AS C23, ";
        $query .= "     '' AS C24, ";
        $query .= "     '' AS C25, ";
        $query .= "     '' AS C26, ";
        $query .= "     '' AS C27, ";
        $query .= "     '' AS C28, ";
        $query .= "     '' AS C29, ";
        $query .= "     '' AS C30, ";
        $query .= "     '' AS C31, ";
        $query .= "     '' AS C32, ";
        $query .= "     '' AS C33, ";
        $query .= "     '' AS C34, ";
        $query .= "     '' AS C35, ";
        $query .= "     '' AS C36, ";
        $query .= "     '' AS C37, ";
        $query .= "     '' AS C38, ";
        $query .= "     '' AS C39, ";
        $query .= "     '' AS C30, ";
        $query .= "     '' AS C41, ";
        $query .= "     '' AS C42, ";
        $query .= "     '' AS C43, ";
        $query .= "     '' AS C44, ";
        $query .= "     '' AS C45, ";
        $query .= "     '' AS C46, ";
        $query .= "     '' AS C47, ";
        $query .= "     '' AS C48, ";
        $query .= "     '' AS C49, ";
        $query .= "     '' AS C50, ";
        $query .= "     '' AS C51, ";
        $query .= "     '' AS C52, ";
        $query .= "     '' AS C53, ";
        $query .= "     '' AS C54, ";
        $query .= "     '' AS C55, ";
        $query .= "     '' AS C56, ";
        $query .= "     '' AS C57, ";
        $query .= "     '' AS C58, ";
        $query .= "     '' AS C59, ";
        $query .= "     '' AS C60, ";
        $query .= "     '' AS C61, ";
        $query .= "     '".$model->lastColumn."' AS ".$model->lastColumn." ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_APPLICANTBASE_DAT T1 ";
        $query .= " LEFT JOIN ";
        $query .= "     ENTEXAM_APPLICANTBASE_DETAIL_DAT L0 ON L0.ENTEXAMYEAR = T1.ENTEXAMYEAR ";
        $query .= "                                        AND L0.EXAMNO = T1.EXAMNO ";
        $query .= "                                        AND L0.SEQ = '007' ";
        $query .= " LEFT JOIN ";
        $query .= "     NAME_MST L2 ON  L2.NAMECD2 = T1.JUDGEMENT "; //合格
        $query .= "                 AND L2.NAMECD1 = 'L013' ";
        $query .= " WHERE ";
        $query .= "       T1.ENTEXAMYEAR  = '{$model->examyear}' ";
        $query .= "   AND T1.PROCEDUREDIV = '1' "; //手続済み
        if ($model->schoolName != "bunkyo") {
            $query .= "   AND T1.ENTDIV       = '1' "; //入学有り
        } else {
            $query .= "   AND VALUE(T1.ENTDIV, '0') <> '2' "; //辞退者は対象外
        }
        if ($model->goukaku == 'ALL_J') {
            $query .= "   AND T1.APPLICANTDIV = '1' ";
        } elseif ($model->goukaku == 'ALL_H') {
            $query .= "   AND T1.APPLICANTDIV = '2' ";
        } else {
            $query .= "   AND L0.REMARK1  = '{$model->suc_coursecd}' ";
            $query .= "   AND L0.REMARK2  = '{$model->suc_majorcd}' ";
            $query .= "   AND L0.REMARK3  = '{$model->suc_coursecode}' ";
        }
        $query .= "   AND L2.NAMESPARE1 IS NOT NULL ";

        $query .= " ORDER BY ";
        $query .= $this->selected_data;

        return $query;
    }
}
