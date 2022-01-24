<?php

require_once('for_php7.php');

class knjl321nQuery extends Query
{

    //入試制度、入試区分、専併区分取得
    public function getNameMst($model, $setcd, $namecd2 = "")
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     NAMECD2 || ':' || NAME1 AS LABEL, ";
        $query .= "     NAMECD2 AS VALUE, ";
        $query .= "     NAMESPARE2, ";
        $query .= "     NAME1 AS CSV_NAME ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$model->ObjYear}' ";
        $query .= " AND NAMECD1 = '{$setcd}' ";
        if (strlen($namecd2)) {
            $query .= " AND NAMECD2 = '{$namecd2}' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //CSVデータ
    public function selectMainQuery($model, $testSubclass)
    {
        $query  = "";
        $query .= " SELECT  ";
        $query .= "      BDETAIL1.REMARK8 || BDETAIL1.REMARK9 || BDETAIL1.REMARK10 AS EXAMCOURSE,  ";
        $query .= "      CRS1.EXAMCOURSE_NAME, ";
        $query .= "      CASE WHEN BDETAIL9.REMARK9 = '1' THEN 1 "; //○
        $query .= "           WHEN BDETAIL9.REMARK9 = '3' THEN 2 "; //△
        $query .= "           WHEN BDETAIL9.REMARK9 = '4' THEN 3 "; //▲
        $query .= "           WHEN BDETAIL9.REMARK9 = '2' THEN 4 "; //×
        $query .= "           ELSE 5 ";
        $query .= "      END AS KYOIKU_SORT, ";
        $query .= "      CASE WHEN BDETAIL9.REMARK9 = '4' THEN 2 ";
        $query .= "           WHEN BDETAIL9.REMARK9 = '2' THEN 3 ";
        $query .= "           ELSE 1 ";
        $query .= "      END AS KYOIKU_DIV, ";
        $query .= "      BDETAIL9.REMARK9 AS KYOIKU,  ";
        $query .= "      NML026.NAME1 AS KYOIKU_NAME, ";
        $query .= "      CRS2.EXAMCOURSE_NAME AS SUC_EXAMCOURSE_NAME, ";
        $query .= "      BDETAIL1.REMARK10 AS EXAMCOURSECD,  ";
        $query .= "      BASE.SHDIV, ";
        $query .= "      NML006.NAME1 AS SHDIV_NAME, ";
        $query .= "      BASE.EXAMNO,  ";
        $query .= "      BASE.NAME,  ";
        $query .= "      FIN.FINSCHOOL_NAME_ABBV AS FINSCHOOL_NAME,  ";
        $query .= "      NML013.NAME1 AS KESSEKI_JITAI_NAME, ";
        $query .= "      RECEPT.TOTAL4,  ";
        $query .= "      RECEPT.DIV_RANK4,  ";
        $query .= "      CRS17.EXAMCOURSE_NAME AS EXAMCOURSE_NAME2, ";
        $query .= "      BDETAIL9.REMARK7,  ";
        $query .= "      NML025.NAME1 AS REMARK7_NAME, ";
        $query .= "      BDETAIL9.REMARK1,  ";
        $query .= "      BDETAIL9.REMARK2,  ";
        $query .= "      BDETAIL9.REMARK3,  ";
        $query .= "      BDETAIL9.REMARK4,  ";
        $query .= "      BDETAIL9.REMARK8,  ";
        $query .= "      NML025_2.NAME1 AS REMARK8_NAME, ";
        $query .= "      BDETAIL9.REMARK5,  ";
        $query .= "      BDETAIL9.REMARK6,  ";
        $query .= "      BDETAIL4.REMARK1 AS CLUB_NAME, ";
        $query .= "      CONFRPT.TOTAL_ALL,  ";
        $query .= "      CONFRPT.TOTAL5,  ";
        $query .= "      CONFRPT.KASANTEN_ALL,  ";
        foreach ($testSubclass as $val => $label) {
            $query .= "      TSCORE{$val}.ATTEND_FLG AS ATTEND_FLG{$val},  ";
            $query .= "      TSCORE{$val}.SCORE AS SCORE{$val},  ";
        }
        $query .= "      'DUMMY' AS DUMMY  ";
        $query .= "  FROM  ";
        $query .= "      ENTEXAM_APPLICANTBASE_DAT BASE  ";
        $query .= "      LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT BDETAIL1 ON BDETAIL1.ENTEXAMYEAR = BASE.ENTEXAMYEAR  ";
        $query .= "          AND BDETAIL1.EXAMNO = BASE.EXAMNO  ";
        $query .= "          AND BDETAIL1.SEQ = '001'  ";
        $query .= "      LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT BDETAIL4 ON BDETAIL4.ENTEXAMYEAR = BASE.ENTEXAMYEAR  ";
        $query .= "          AND BDETAIL4.EXAMNO = BASE.EXAMNO  ";
        $query .= "          AND BDETAIL4.SEQ = '004'  ";
        $query .= "      LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT BDETAIL9 ON BDETAIL9.ENTEXAMYEAR = BASE.ENTEXAMYEAR  ";
        $query .= "          AND BDETAIL9.EXAMNO = BASE.EXAMNO  ";
        $query .= "          AND BDETAIL9.SEQ = '009'  ";
        $query .= "      LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT BDETAIL17 ON BDETAIL17.ENTEXAMYEAR = BASE.ENTEXAMYEAR ";
        $query .= "          AND BDETAIL17.EXAMNO = BASE.EXAMNO ";
        $query .= "          AND BDETAIL17.SEQ = '017' ";
        $query .= "      LEFT JOIN ENTEXAM_COURSE_MST CRS1 ON CRS1.ENTEXAMYEAR = BASE.ENTEXAMYEAR  ";
        $query .= "          AND CRS1.APPLICANTDIV = BASE.APPLICANTDIV  ";
        $query .= "          AND CRS1.TESTDIV = BASE.TESTDIV  ";
        $query .= "          AND CRS1.COURSECD = BDETAIL1.REMARK8  ";
        $query .= "          AND CRS1.MAJORCD = BDETAIL1.REMARK9  ";
        $query .= "          AND CRS1.EXAMCOURSECD = BDETAIL1.REMARK10 ";
        $query .= "      LEFT JOIN ENTEXAM_COURSE_MST CRS2 ON CRS2.ENTEXAMYEAR = BASE.ENTEXAMYEAR  ";
        $query .= "          AND CRS2.APPLICANTDIV = BASE.APPLICANTDIV  ";
        $query .= "          AND CRS2.TESTDIV = BASE.TESTDIV  ";
        $query .= "          AND CRS2.COURSECD = BASE.SUC_COURSECD ";
        $query .= "          AND CRS2.MAJORCD = BASE.SUC_MAJORCD  ";
        $query .= "          AND CRS2.EXAMCOURSECD = BASE.SUC_COURSECODE ";
        $query .= "      LEFT JOIN ENTEXAM_COURSE_MST CRS17 ON CRS17.ENTEXAMYEAR = BASE.ENTEXAMYEAR  ";
        $query .= "          AND CRS17.APPLICANTDIV = BASE.APPLICANTDIV  ";
        $query .= "          AND CRS17.TESTDIV      = BASE.TESTDIV  ";
        $query .= "          AND CRS17.COURSECD     = BDETAIL17.REMARK1  ";
        $query .= "          AND CRS17.MAJORCD      = BDETAIL17.REMARK2  ";
        $query .= "          AND CRS17.EXAMCOURSECD = BDETAIL17.REMARK3 ";
        $query .= "      LEFT JOIN ENTEXAM_RECEPT_DAT RECEPT ON RECEPT.ENTEXAMYEAR = BASE.ENTEXAMYEAR  ";
        $query .= "          AND RECEPT.APPLICANTDIV = BASE.APPLICANTDIV  ";
        $query .= "          AND RECEPT.TESTDIV = BASE.TESTDIV  ";
        $query .= "          AND RECEPT.EXAM_TYPE = '1'  ";
        $query .= "          AND RECEPT.EXAMNO = BASE.EXAMNO  ";
        foreach ($testSubclass as $val => $label) {
            $query .= "      LEFT JOIN ENTEXAM_SCORE_DAT TSCORE{$val} ON TSCORE{$val}.ENTEXAMYEAR = RECEPT.ENTEXAMYEAR  ";
            $query .= "          AND TSCORE{$val}.APPLICANTDIV = RECEPT.APPLICANTDIV  ";
            $query .= "          AND TSCORE{$val}.TESTDIV = RECEPT.TESTDIV  ";
            $query .= "          AND TSCORE{$val}.EXAM_TYPE = RECEPT.EXAM_TYPE  ";
            $query .= "          AND TSCORE{$val}.RECEPTNO = RECEPT.RECEPTNO  ";
            $query .= "          AND TSCORE{$val}.TESTSUBCLASSCD = '{$val}'  ";
        }
        $query .= "      LEFT JOIN FINSCHOOL_MST FIN ON FIN.FINSCHOOLCD = BASE.FS_CD  ";
        $query .= "      LEFT JOIN ENTEXAM_APPLICANTCONFRPT_DAT CONFRPT ON CONFRPT.ENTEXAMYEAR = BASE.ENTEXAMYEAR  ";
        $query .= "          AND CONFRPT.EXAMNO = BASE.EXAMNO  ";
        $query .= "      LEFT JOIN NAME_MST NML013 ON NML013.NAMECD1 = 'L013'  ";
        $query .= "          AND NML013.NAMECD2 = BASE.JUDGEMENT ";
        $query .= "          AND BASE.JUDGEMENT IN ('4', '5') ";
        $query .= "      LEFT JOIN NAME_MST NML006 ON NML006.NAMECD1 = 'L006'  ";
        $query .= "          AND NML006.NAMECD2 = BASE.SHDIV ";
        $query .= "      LEFT JOIN NAME_MST NML026 ON NML026.NAMECD1 = 'L026'  ";
        $query .= "          AND NML026.NAMECD2 = BDETAIL9.REMARK9 ";
        $query .= "      LEFT JOIN NAME_MST NML025 ON NML025.NAMECD1 = 'L025'  ";
        $query .= "          AND NML025.NAMECD2 = BDETAIL9.REMARK7 ";
        $query .= "      LEFT JOIN NAME_MST NML025_2 ON NML025_2.NAMECD1 = 'L025'  ";
        $query .= "          AND NML025_2.NAMECD2 = BDETAIL9.REMARK8 ";
        $query .= "  WHERE  ";
        $query .= "      BASE.ENTEXAMYEAR = '" . $model->ObjYear . "'  ";
        $query .= "      AND BASE.APPLICANTDIV = '" . $model->field["APPLICANTDIV"] . "'  ";
        $query .= "      AND BASE.TESTDIV = '" . $model->field["TESTDIV"] . "'  ";
        if (strlen($model->field["SCORE_TOTAL"])) {
            $query .= "      AND VALUE(RECEPT.TOTAL4, -1) <= " . $model->field["SCORE_TOTAL"] . "  ";
            $query .= "      AND VALUE(BDETAIL9.REMARK9, '') <> '2' "; // 教育相談×は表示しない
        }
        $query .= "  ORDER BY  ";
        //成績順
        if ($model->field["ORDER"] == "1") {
            $query .= "      BASE.SHDIV,  ";        //1.専併区分
            $query .= "      KYOIKU_SORT, ";        //2.教育相談
            $query .= "      BDETAIL1.REMARK10,  "; //3.第一志望コース
            $query .= "      VALUE(RECEPT.TOTAL4, -1) DESC,  ";
            $query .= "      BASE.EXAMNO  ";
        //受験番号順
        } else {
            $query .= "      BASE.SHDIV,  ";
            $query .= "      BASE.EXAMNO  ";
        }

        return $query;
    }

    //科目取得
    public function getNameMstL009($model)
    {
        $query  = " SELECT ";
        $query .= "     NAME1 AS LABEL, ";
        $query .= "     NAMECD2 AS VALUE ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$model->ObjYear}' ";
        $query .= " AND NAMECD1 = 'L009' ";
        $query .= " AND NAMECD2 in ('1','2','3','4','5') ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }
}
