<?php

require_once('for_php7.php');
class knjl140query extends Query
{
    //学校名取得
    public function getSchoolName()
    {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = 'Z010' AND ";
        $query .= "     NAMECD2 = '00' ";

        return $query;
    }

    //内外区分取得
    public function getInoutcd($model)
    {
        $query  = " SELECT ";
        $query .= "     NAMECD2 || ':' || NAME1 AS LABEL, ";
        $query .= "     NAMECD2 AS VALUE ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = 'A001' AND ";
        $query .= "     NAMECD2 IN ('0','1') "; //0:内生 1:外生
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //合格コースコンボ
    public function getGoukaku($model)
    {
        if ($model->schoolName == 'CHIBEN' || $model->schoolName == 'HOUSEI' || $model->schoolName == 'jisyukan') {
            //この学校が智辯・法政・自修館だったら
            $query .= " WITH SUBMAIN AS ( ";
            $query .= " SELECT ";
            if ($model->schoolName == 'jisyukan') {
                $query .= "     T1.SUC_COURSECD   || ";
                $query .= "     T1.SUC_MAJORCD    || ";
                $query .= "     ':'               || ";
                $query .= "     L3.COURSENAME     || ";
                $query .= "     L4.MAJORNAME      || ";
                $query .= "     ' '               || ";
                $query .= "     '中学' AS LABEL, ";

                $query .= "     T1.SUC_COURSECD   || ':' || ";
                $query .= "     T1.SUC_MAJORCD    || ':' || ";
                $query .= "     '".$model->lastColumn."'      || ':' || "; //GROUP BY できれば文字は何でもいい
                $query .= "     'CHUGAKU' AS VALUE ";
            } else {
                $query .= "     CASE WHEN T1.APPLICANTDIV = '1' ";
                $query .= "          THEN     T1.SUC_COURSECD   || ";
                $query .= "                   T1.SUC_MAJORCD    || ";
                $query .= "                   ':'               || ";
                $query .= "                   L3.COURSENAME     || ";
                $query .= "                   L4.MAJORNAME      || ";
                $query .= "                   ' '               || ";
                $query .= "                   '中学' ";

                $query .= "          ELSE     T1.SUC_COURSECD   || ";
                $query .= "                   T1.SUC_MAJORCD    || ";
                $query .= "                   ':'               || ";
                $query .= "                   L3.COURSENAME     || ";
                $query .= "                   L4.MAJORNAME      || ";
                $query .= "                   ' '               || ";
                $query .= "                   '高校' ";
                $query .= "     END AS LABEL, ";

                $query .= "     CASE WHEN T1.APPLICANTDIV = '1' ";
                $query .= "          THEN     T1.SUC_COURSECD   || ':' || ";
                $query .= "                   T1.SUC_MAJORCD    || ':' || ";
                $query .= "                   '".$model->lastColumn."'      || ':' || "; //GROUP BY できれば文字は何でもいい
                $query .= "                   'CHUGAKU' ";

                $query .= "          ELSE     T1.SUC_COURSECD   || ':' || ";
                $query .= "                   T1.SUC_MAJORCD    || ':' || ";
                $query .= "                   '".$model->lastColumn."'      || ':' || "; //GROUP BY できれば文字は何でもいい
                $query .= "                   'KOKO' ";
                $query .= "     END AS VALUE ";
            }
            $query .= " FROM ";
            $query .= "     ENTEXAM_APPLICANTBASE_DAT T1 ";
            $query .= " LEFT JOIN ";
            $query .= "     NAME_MST L1 ON  L1.NAMECD2 = T1.JUDGEMENT ";
            $query .= "                 AND L1.NAMECD1 = 'L013' ";
            $query .= " LEFT JOIN ";
            $query .= "     ENTEXAM_COURSE_MST L2 ON  L2.ENTEXAMYEAR  = T1.ENTEXAMYEAR ";
            $query .= "                           AND L2.TESTDIV      = T1.TESTDIV ";
            $query .= "                           AND L2.COURSECD     = T1.SUC_COURSECD ";
            $query .= "                           AND L2.MAJORCD      = T1.SUC_MAJORCD ";
            $query .= "                           AND L2.EXAMCOURSECD = T1.SUC_COURSECODE ";
            $query .= " LEFT JOIN ";
            $query .= "     COURSE_MST L3 ON L3.COURSECD = T1.SUC_COURSECD ";
            $query .= " LEFT JOIN ";
            $query .= "     MAJOR_MST L4 ON  L4.COURSECD = T1.SUC_COURSECD ";
            $query .= "                  AND L4.MAJORCD  = T1.SUC_MAJORCD ";
            $query .= " WHERE ";
            $query .= "     T1.ENTEXAMYEAR  = '{$model->examyear}' ";
            $query .= " AND T1.PROCEDUREDIV = '1' ";
            $query .= " AND T1.ENTDIV       = '1' ";
            $query .= " AND L1.NAMESPARE1 IS NOT NULL ";
            $query .= " AND L2.EXAMCOURSE_NAME IS NOT NULL ";
            $query .= " ) ";

            //メイン
            $query .= " SELECT ";
            $query .= "     LABEL, ";
            $query .= "     VALUE ";
            $query .= " FROM ";
            $query .= "     SUBMAIN ";
            $query .= " GROUP BY ";
            $query .= "     LABEL, ";
            $query .= "     VALUE ";
            $query .= " ORDER BY ";
            $query .= "     VALUE ";
        } else { //智辯・法政・自修館以外の学校
            $query  = " SELECT ";
            $query .= "     T1.SUC_COURSECD   ||  ";
            $query .= "     T1.SUC_MAJORCD    || ";
            $query .= "     ':'               || ";
            $query .= "     L3.COURSENAME     || ";
            $query .= "     L4.MAJORNAME      || ";
            $query .= "     ' '               || ";
            $query .= "     L2.EXAMCOURSE_NAME AS LABEL, ";
            $query .= "     T1.SUC_COURSECD   || ':' || ";
            $query .= "     T1.SUC_MAJORCD    || ':' || ";
            $query .= "     T1.SUC_COURSECODE || ':' || ";
            $query .= "     T1.TESTDIV AS VALUE ";
            $query .= " FROM ";
            $query .= "     ENTEXAM_APPLICANTBASE_DAT T1 ";
            $query .= " LEFT JOIN ";
            $query .= "     NAME_MST L1 ON  L1.NAMECD2 = T1.JUDGEMENT ";
            $query .= "                 AND L1.NAMECD1 = 'L013' ";
            $query .= " LEFT JOIN ";
            $query .= "     ENTEXAM_COURSE_MST L2 ON  L2.ENTEXAMYEAR = T1.ENTEXAMYEAR ";
            $query .= "                           AND L2.TESTDIV = T1.TESTDIV ";
            $query .= "                           AND L2.COURSECD = T1.SUC_COURSECD ";
            $query .= "                           AND L2.MAJORCD = T1.SUC_MAJORCD ";
            $query .= "                           AND L2.EXAMCOURSECD = T1.SUC_COURSECODE ";
            $query .= " LEFT JOIN ";
            $query .= "     COURSE_MST L3 ON L3.COURSECD = T1.SUC_COURSECD ";
            $query .= " LEFT JOIN ";
            $query .= "     MAJOR_MST L4 ON  L4.COURSECD = T1.SUC_COURSECD ";
            $query .= "                  AND L4.MAJORCD  = T1.SUC_MAJORCD ";
            $query .= " WHERE ";
            $query .= "     T1.ENTEXAMYEAR = '{$model->examyear}' ";
            $query .= " AND T1.PROCEDUREDIV = '1' ";
            $query .= " AND T1.ENTDIV = '1' ";
            $query .= " AND L1.NAMESPARE1 IS NOT NULL ";
            $query .= " AND L2.EXAMCOURSE_NAME IS NOT NULL ";
            $query .= " GROUP BY ";
            $query .= "     T1.SUC_COURSECD, ";
            $query .= "     T1.SUC_MAJORCD, ";
            $query .= "     T1.SUC_COURSECODE, ";
            $query .= "     T1.TESTDIV, ";
            $query .= "     L2.EXAMCOURSE_NAME, ";
            $query .= "     L3.COURSENAME, ";
            $query .= "     L4.MAJORNAME ";
            $query .= " ORDER BY ";
            $query .= "     T1.SUC_COURSECD, ";
            $query .= "     T1.SUC_MAJORCD, ";
            $query .= "     T1.SUC_COURSECODE, ";
            $query .= "     T1.TESTDIV, ";
            $query .= "     L2.EXAMCOURSE_NAME ";
        }

        return $query;
    }

    //入学者のCSV
    public function selectCvsStudentQuery($model)
    {
        $query  = " SELECT ";
        if ($model->schoolName == 'CHIBEN' && $model->isWakayama) {
            $query .= "     T1.ENTEXAMYEAR, ";
            if ($model->inoutcd === '0') {
                $query .= "     '' AS SCHREGNO2, ";
                $query .= "     '' AS SNO, ";
            } else {
                //学籍番号(数字8桁)
                //1桁目　1:和歌山
                $query .= "     '1' || ";
                //23桁目　入学年度の西暦下2桁
                $query .= "     SUBSTR(T1.ENTEXAMYEAR,3) || ";
                //4桁目　0:新入学
                $query .= "     '0' || ";
                //5桁目　1:高校スポーツ 2:中学外部生 3:高校編入
                $query .= "     (CASE WHEN T1.APPLICANTDIV = '2' AND T1.TESTDIV  = '4' THEN '1' ";
                $query .= "           WHEN T1.APPLICANTDIV = '1' AND T1.TESTDIV != '6' THEN '2' ";
                $query .= "           WHEN T1.APPLICANTDIV = '2' AND T1.TESTDIV  = '3' THEN '3' ";
                $query .= "      ELSE '0' END) AS SCHREGNO2, ";
                //678桁目　通し番号
                $query .= "     RANK() OVER(PARTITION BY ";
                $query .= "                (CASE WHEN T1.APPLICANTDIV = '2' AND T1.TESTDIV  = '4' THEN '1' ";
                $query .= "                      WHEN T1.APPLICANTDIV = '1' AND T1.TESTDIV != '6' THEN '2' ";
                $query .= "                      WHEN T1.APPLICANTDIV = '2' AND T1.TESTDIV  = '3' THEN '3' ";
                $query .= "                 ELSE '0' END) ";
                $query .= "                 ORDER BY T1.SEX, TRANSLATE_KANA(T1.NAME_KANA), T1.BIRTHDAY) AS SNO, ";
            }
            $query .= "     '1' AS ENTDIV, ";
            $query .= "     '' AS GRADE, ";
            $query .= "     '' AS HR_CLASS, ";
            $query .= "     '' AS ATTENDNO, ";
            $query .= "     '{$model->inoutcd}' AS INOUTCD, ";
            $query .= "     T1.SUC_COURSECD AS COURSECD, ";
            $query .= "     T1.SUC_MAJORCD AS MAJORCD, ";
            $query .= "     T1.SUC_COURSECODE AS COURSECODE, ";
            $query .= "     T1.NAME, ";
            $query .= "     T1.NAME_KANA, ";
            $query .= "     CAST(REPLACE(CAST(T1.BIRTHDAY AS CHAR(10)), '-', '/') AS CHAR(10)) AS BIRTHDAY, ";
            $query .= "     T1.SEX, "; //コードで出力する
            $query .= "     T1.FS_CD, ";
            $query .= "     T1.ENTEXAMYEAR || '/03/31' AS FINSCHOOLGRADDATE, ";
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
            $query .= "     F1.DISTRICTCD AS TIKUCD, ";
            $query .= "     T1.EXAMNO AS SCHREGNO, ";
            $query .= "     '".$model->lastColumn."' AS ".$model->lastColumn." ";
        } else {
            $query .= "     T1.ENTEXAMYEAR, ";
            $query .= "     T1.EXAMNO AS SCHREGNO, ";
            $query .= "     '' AS ENTDIV, ";
            $query .= "     '' AS HR_CLASS, ";
            $query .= "     '' AS ATTENDNO, ";
            $query .= "     '0' AS INOUTCD, ";
            $query .= "     T1.SUC_COURSECD AS COURSECD, ";
            $query .= "     T1.SUC_MAJORCD AS MAJORCD, ";
            $query .= "     T1.SUC_COURSECODE AS COURSECODE, ";
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
        }
        $query .= " FROM ";
        $query .= "     ENTEXAM_APPLICANTBASE_DAT T1 ";
        $query .= " LEFT JOIN ";
        $query .= "     ENTEXAM_APPLICANTADDR_DAT L1 ON  L1.ENTEXAMYEAR = T1.ENTEXAMYEAR ";
        $query .= "                                  AND L1.EXAMNO = T1.EXAMNO ";
        $query .= " LEFT JOIN ";
        $query .= "     NAME_MST L2 ON  L2.NAMECD2 = T1.JUDGEMENT "; //合格
        $query .= "                 AND L2.NAMECD1 = 'L013' ";
        if ($model->schoolName == 'CHIBEN' && $model->isWakayama) {
            $query .= " LEFT JOIN FINSCHOOL_MST F1 ON F1.FINSCHOOLCD = T1.FS_CD ";
        }
        $query .= " WHERE ";
        $query .= "       T1.ENTEXAMYEAR  = '{$model->examyear}' ";
        $query .= "   AND T1.PROCEDUREDIV = '1' "; //手続済み
        $query .= "   AND T1.ENTDIV       = '1' "; //入学有り
        if ($model->schoolName != 'HOUSEI' && $model->schoolName != 'kyoaiP') { //法政、共愛小学校以外
            $query .= "   AND T1.SUC_COURSECD = '{$model->suc_coursecd}' ";
            $query .= "   AND T1.SUC_MAJORCD  = '{$model->suc_majorcd}' ";
        }
        $query .= "   AND L2.NAMESPARE1 IS NOT NULL ";

        if ($model->suc_coursecode != $model->lastColumn && $model->schoolName != 'kyoaiP') {
            $query .= "   AND T1.SUC_COURSECODE = '{$model->suc_coursecode}' ";
        }

        if ($model->schoolName == 'CHIBEN' || $model->schoolName == 'HOUSEI') {
            if ($model->testdiv == 'CHUGAKU') { //中学を選択している時
                $query .= "   AND T1.APPLICANTDIV  = '1' ";
            } else { //高校を選択しているとき
                $query .= "   AND T1.APPLICANTDIV != '1' ";
            }
        }
        if ($model->schoolName == 'hirogaku') {
            $query .= "   AND T1.APPLICANTDIV = '{$model->fixApplicantDiv}' ";
        }
        if ($model->schoolName == 'CHIBEN' && $model->isWakayama) {
            if ($model->inoutcd === '0') {
                $query .= "   AND T1.TESTDIV  = '6' ";
            } else {
                $query .= "   AND T1.TESTDIV != '6' ";
            }
        }
        $query .= " ORDER BY ";
        $query .= $this->selected_data;
        return $query;
    }

    //保護者のCSV
    public function selectCvsGuardQuery($model)
    {
        $query  = " SELECT ";
        if ($model->schoolName == 'CHIBEN' && $model->isWakayama) {
            $query .= "     '' AS GRADE, ";
            $query .= "     '' AS HR_CLASS, ";
            $query .= "     '' AS ATTENDNO, ";
            $query .= "     T1.NAME AS NAME, ";
            if ($model->inoutcd === '0') {
                $query .= "     '' AS SCHREGNO2, ";
                $query .= "     '' AS SNO, ";
            } else {
                //学籍番号(数字8桁)
                //1桁目　1:和歌山
                $query .= "     '1' || ";
                //23桁目　入学年度の西暦下2桁
                $query .= "     SUBSTR(T1.ENTEXAMYEAR,3) || ";
                //4桁目　0:新入学
                $query .= "     '0' || ";
                //5桁目　1:高校スポーツ 2:中学外部生 3:高校編入
                $query .= "     (CASE WHEN T1.APPLICANTDIV = '2' AND T1.TESTDIV  = '4' THEN '1' ";
                $query .= "           WHEN T1.APPLICANTDIV = '1' AND T1.TESTDIV != '6' THEN '2' ";
                $query .= "           WHEN T1.APPLICANTDIV = '2' AND T1.TESTDIV  = '3' THEN '3' ";
                $query .= "      ELSE '0' END) AS SCHREGNO2, ";
                //678桁目　通し番号
                $query .= "     RANK() OVER(PARTITION BY ";
                $query .= "                (CASE WHEN T1.APPLICANTDIV = '2' AND T1.TESTDIV  = '4' THEN '1' ";
                $query .= "                      WHEN T1.APPLICANTDIV = '1' AND T1.TESTDIV != '6' THEN '2' ";
                $query .= "                      WHEN T1.APPLICANTDIV = '2' AND T1.TESTDIV  = '3' THEN '3' ";
                $query .= "                 ELSE '0' END) ";
                $query .= "                 ORDER BY T1.SEX, TRANSLATE_KANA(T1.NAME_KANA), T1.BIRTHDAY) AS SNO, ";
            }
            $query .= "     value(L1.RELATIONSHIP, '01') AS RELATIONSHIP, ";
            $query .= "     L1.GNAME, ";
            $query .= "     L1.GKANA, ";
            $query .= "     CASE WHEN L1.RELATIONSHIP IN ('01','03','05','07','09') THEN '1' ";
            $query .= "          WHEN L1.RELATIONSHIP IN ('02','04','06','08','10') THEN '2' ";
            $query .= "     ELSE '' END AS SEITO_SEX, ";
            $query .= "     '' AS BIRTHDAY, ";
            $query .= "     L1.GZIPCD, ";
            $query .= "     L1.GADDRESS1, ";
            $query .= "     L1.GADDRESS2, ";
            $query .= "     L1.GTELNO, ";
            $query .= "     '' AS GFAXNO, ";
            $query .= "     '' AS EMAIL, ";
            $query .= "     '' AS GUARD_JOBCD, ";
            $query .= "     '' AS GUARD_WORK_NAME, ";
            $query .= "     '' AS GUARD_WORK_TELNO, ";
            $query .= "     '' AS GUARANTOR_RELATIONSHIP, ";
            $query .= "     '' AS GUARANTOR_NAME, ";
            $query .= "     '' AS GUARANTOR_KANA, ";
            $query .= "     '' AS GUARANTOR_SEX, ";
            $query .= "     '' AS GUARANTOR_ZIPCD, ";
            $query .= "     '' AS GUARANTOR_ADDR1, ";
            $query .= "     '' AS GUARANTOR_ADDR2, ";
            $query .= "     '' AS GUARANTOR_TELNO, ";
            $query .= "     '' AS GUARANTOR_JOBCD, ";
            $query .= "     '' AS PUBLIC_OFFICE, ";
            $query .= "     '".$model->lastColumn."' AS ".$model->lastColumn.", ";
            $query .= "     T1.EXAMNO AS SCHREGNO ";
        } else {
            $query .= "     '' AS GRADE, ";
            $query .= "     '' AS HR_CLASS, ";
            $query .= "     '' AS ATTENDNO, ";
            $query .= "     '' AS NAME, ";
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
        }
        $query .= " FROM ";
        $query .= "     ENTEXAM_APPLICANTBASE_DAT T1 ";
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
        if ($model->schoolName != 'HOUSEI' && $model->schoolName != 'kyoaiP') { //法政以外
            $query .= "   AND T1.SUC_COURSECD = '{$model->suc_coursecd}' ";
            $query .= "   AND T1.SUC_MAJORCD  = '{$model->suc_majorcd}' ";
        }
        $query .= "   AND L2.NAMESPARE1 IS NOT NULL ";

        if ($model->suc_coursecode != $model->lastColumn && $model->schoolName != 'kyoaiP') {
            $query .= "   AND T1.SUC_COURSECODE = '{$model->suc_coursecode}' ";
        }

        if ($model->schoolName == 'CHIBEN' || $model->schoolName == 'HOUSEI') {
            if ($model->testdiv == 'CHUGAKU') { //中学を選択している時
                $query .= "   AND T1.APPLICANTDIV  = '1' ";
            } else { //高校を選択しているとき
                $query .= "   AND T1.APPLICANTDIV != '1' ";
            }
        }
        if ($model->schoolName == 'hirogaku') {
            $query .= "   AND T1.APPLICANTDIV = '{$model->fixApplicantDiv}' ";
        }
        if ($model->schoolName == 'CHIBEN' && $model->isWakayama) {
            if ($model->inoutcd === '0') {
                $query .= "   AND T1.TESTDIV  = '6' ";
            } else {
                $query .= "   AND T1.TESTDIV != '6' ";
            }
        }

        $query .= " ORDER BY ";
        $query .= $this->selected_data;
        return $query;
    }
}
