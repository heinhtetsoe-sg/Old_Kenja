<?php

require_once('for_php7.php');
class knjl140mQuery extends Query
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

    //学校名取得
    public function getNameMst($namecd1)
    {
        $query  = " SELECT ";
        $query .= "     NAMECD2, ";
        $query .= "     NAME1, ";
        $query .= "     ABBV1 ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = '".$namecd1."' ";
        $query .= " ORDER BY ";
        $query .= "     NAMECD2 ";

        return $query;
    }

    //校種取得(入学コース取得用)
    public function getVNameMst($model, $namecd1)
    {
        $query  = " SELECT ";
        $query .= "     NAMECD2, ";
        $query .= "     NAME1, ";
        $query .= "     ABBV1 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$model->examyear}' ";
        $query .= "     AND NAMECD1 = '{$namecd1}' ";
        $query .= " ORDER BY ";
        $query .= "     NAMECD2 ";

        return $query;
    }

    //入試区分リスト取得
    public function getTestDivList($year)
    {
        $query  = " SELECT ";
        $query .= "     NAME1, ";
        $query .= "     NAMECD2 AS VALUE,";
        $query .= "     NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR    = '".$year."' AND ";
        $query .= "     NAMECD1 = 'L004' ";
        $query .= " ORDER BY ";
        $query .= "     int(VALUE) ";

        return $query;
    }

    //入学者のCSV
    public function selectCvsStudentQuery($model)
    {
        $query  = " SELECT ";
        $query .= "     T1.ENTEXAMYEAR, ";
        $query .= "     T1.EXAMNO AS SCHREGNO, ";
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
        $query .= "     FIN_M.FINSCHOOL_NAME, ";
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
        $query .= "     T1.EXAMNO AS EXAMNO, ";
        if ($model->Properties["UseMusyouKaisu"] == "1") {
            $query .= " '' AS MUSYOUKAISUU, ";
        }
        $query .= "     T1.TESTDIV, ";
        $query .= "     '".$model->lastColumn."' AS ".$model->lastColumn." ";
        if ($model->field["OUTPUTSCORE"] == 1) {
            if ($model->Properties["useScoreMongonDeviation"] == "1") {
                $query .= "    , LR.LINK_JUDGE_DEVIATION AS DEVIATION ";
            } else {
                $query .= "    , LR.TOTAL4 AS SCORE ";
            }
        }
        $query .= " FROM ";
        $query .= "     ENTEXAM_APPLICANTBASE_DAT T1 ";
        $query .= " LEFT JOIN ";
        $query .= "     ENTEXAM_APPLICANTBASE_DETAIL_DAT L0 ON L0.ENTEXAMYEAR  = T1.ENTEXAMYEAR ";
        $query .= "                                        AND L0.EXAMNO       = T1.EXAMNO ";
        $query .= "                                        AND L0.SEQ          = '007' ";
        $query .= " LEFT JOIN ";
        $query .= "     ENTEXAM_APPLICANTADDR_DAT L1 ON  L1.ENTEXAMYEAR  = T1.ENTEXAMYEAR ";
        $query .= "                                  AND L1.EXAMNO       = T1.EXAMNO ";
        $query .= " LEFT JOIN ";
        $query .= "     NAME_MST L2 ON  L2.NAMECD2 = T1.JUDGEMENT "; //合格
        $query .= "                 AND L2.NAMECD1 = 'L013' ";
        if ($model->field["OUTPUTSCORE"] == 1) {
            $query .= "     LEFT JOIN ENTEXAM_RECEPT_DAT LR ";
            $query .= "        ON LR.ENTEXAMYEAR  = T1.ENTEXAMYEAR ";
            $query .= "       AND LR.APPLICANTDIV = T1.APPLICANTDIV ";
            $query .= "       AND LR.TESTDIV      = T1.TESTDIV ";
            $query .= "       AND LR.RECEPTNO     = T1.EXAMNO ";
        }
        $query .= " LEFT JOIN FINSCHOOL_MST FIN_M";
        $query .= "       ON FIN_M.FINSCHOOLCD = T1.FS_CD ";
        $query .= " WHERE ";
        $query .= "       T1.ENTEXAMYEAR  = '{$model->examyear}' ";
        $query .= "   AND T1.PROCEDUREDATE IS NOT NULL "; //振込日
        $query .= "   AND T1.ENTDIV       = '1' "; //入学有り
        $query .= "   AND VALUE(T1.ENTDIV, '0') <> '2' "; //辞退者は対象外
        $query .= "   AND L2.NAMESPARE1 IS NOT NULL ";
        // APPLICANTDIVは条件に加えない
        // $query .= "   AND T1.APPLICANTDIV = '{$model->applicantdiv}' ";
        $query .= "   AND T1.TESTDIV = '{$model->testdiv}' ";
        $query .= " ORDER BY ";
        $query .= $model->selected_data;

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
        $query .= "     ENTEXAM_APPLICANTADDR_DAT L1 ON  L1.ENTEXAMYEAR  = T1.ENTEXAMYEAR ";
        $query .= "                                  AND L1.EXAMNO       = T1.EXAMNO ";
        $query .= " LEFT JOIN ";
        $query .= "     NAME_MST L2 ON  L2.NAMECD2 = T1.JUDGEMENT "; //合格
        $query .= "                 AND L2.NAMECD1 = 'L013' ";
        $query .= " WHERE ";
        $query .= "       T1.ENTEXAMYEAR  = '{$model->examyear}' ";
        $query .= "   AND T1.PROCEDUREDATE IS NOT NULL "; //振込日
        $query .= "   AND T1.ENTDIV       = '1' "; //入学有り
        $query .= "   AND VALUE(T1.ENTDIV, '0') <> '2' "; //辞退者は対象外
        $query .= "   AND L2.NAMESPARE1 IS NOT NULL ";
        // APPLICANTDIVは条件に加えない
        // $query .= "   AND T1.APPLICANTDIV = '{$model->applicantdiv}' ";
        $query .= "   AND T1.TESTDIV = '{$model->testdiv}' ";

        $query .= " ORDER BY ";
        $query .= $model->selected_data;

        return $query;
    }
}
