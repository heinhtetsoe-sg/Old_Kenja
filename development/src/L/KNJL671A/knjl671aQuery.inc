<?php

require_once('for_php7.php');

class knjl671aQuery extends Query {

    //名称マスタより取得
    function getNameMst($year, $namecd1) {
        $query  = " SELECT DISTINCT ";
        $query .= "     VALUE(NAME1, '') AS LABEL ";
        $query .= "   , NAMECD2 AS VALUE ";
        $query .= "   , NAMESPARE2 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$year."' ";
        $query .= "     AND NAMECD1 = '".$namecd1."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //合格コース
    function getEntexamCourseMst($year, $applicantDiv, $testdiv) {
        $query  = " SELECT DISTINCT ";
        $query .= "     COURSECD || '-' || MAJORCD || '-' || EXAMCOURSECD VALUE ";
        $query .= "   , EXAMCOURSECD || ':' || EXAMCOURSE_NAME LABEL ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_COURSE_MST ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR = '{$year}' ";
        $query .= "     AND APPLICANTDIV = '{$applicantDiv}' ";
        $query .= "     AND TESTDIV = '{$testdiv}' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //特別奨学生マスタ取得
    function getEntexamSpScholarshipMst($model) {
        // コースコード変換
        list($courseCd, $majorCd, $examCourseCd) = explode('-', $model->field["EXAMCOURSECD"]);

        $query  = "";
        $query .= " SELECT DISTINCT ";
        $query .= "     T1.ENTEXAMYEAR ";
        $query .= "   , T1.APPLICANTDIV ";
        $query .= "   , T1.TESTDIV ";
        $query .= "   , T1.SP_SCHOLAR_CD ";
        $query .= "   , T1.SP_SCHOLAR_NAME ";
        
        $query .= " FROM ENTEXAM_SPECIAL_SCHOLARSHIP_MST T1 ";
        $query .= " INNER JOIN ENTEXAM_SPECIAL_SCHOLARSHIP_DAT T2 ";
        $query .= "    ON T1.ENTEXAMYEAR = T2.ENTEXAMYEAR ";
        $query .= "   AND T1.APPLICANTDIV = T2.APPLICANTDIV ";
        $query .= "   AND T1.TESTDIV = T2.TESTDIV ";
        $query .= "   AND T1.SP_SCHOLAR_CD = T2.SP_SCHOLAR_CD ";
        
        $query .= " WHERE T1.ENTEXAMYEAR = '{$model->objYear}' ";
        $query .= "   AND T1.APPLICANTDIV = '{$model->field["APPLICANTDIV"]}' ";
        $query .= "   AND T1.TESTDIV = '{$model->field["TESTDIV"]}' ";
        $query .= "   AND T2.COURSECD = '{$courseCd}' ";
        $query .= "   AND T2.MAJORCD = '{$majorCd}' ";
        $query .= "   AND T2.EXAMCOURSECD = '{$examCourseCd}' ";

        $query .= " ORDER BY ";
        $query .= "     T1.SP_SCHOLAR_CD ";

        return $query;
    }


    //データ選択
    function selectQuery($model, $testDivList) {

        $query  = "";

        $query .= " WITH MIN_TESTDIV AS ( ";
        $query .= "   SELECT ";
        $query .= "       R1.ENTEXAMYEAR ";
        $query .= "     , R1.APPLICANTDIV ";
        $query .= "     , MIN(R1.TESTDIV) TESTDIV ";
        $query .= "     , R1.EXAMNO ";
        $query .= "   FROM ENTEXAM_RECEPT_DAT R1 ";
        $query .= "        INNER JOIN ENTEXAM_RECEPT_DETAIL_DAT RD_007 ";
        $query .= "           ON RD_007.ENTEXAMYEAR = R1.ENTEXAMYEAR ";
        $query .= "          AND RD_007.APPLICANTDIV = R1.APPLICANTDIV ";
        $query .= "          AND RD_007.TESTDIV = R1.TESTDIV ";
        $query .= "          AND RD_007.EXAM_TYPE = R1.EXAM_TYPE ";
        $query .= "          AND RD_007.RECEPTNO = R1.RECEPTNO ";
        $query .= "          AND RD_007.SEQ = '007' ";
        $query .= "   WHERE R1.ENTEXAMYEAR = '{$model->objYear}' ";
        $query .= "     AND R1.APPLICANTDIV = '{$model->field["APPLICANTDIV"]}' ";
        $query .= "     AND (RD_007.REMARK1 IS NOT NULL AND RD_007.REMARK7 = '1' ";
        $query .= "      OR  RD_007.REMARK2 IS NOT NULL AND RD_007.REMARK8 = '1') ";
        $query .= "   GROUP BY ";
        $query .= "       R1.ENTEXAMYEAR ";
        $query .= "     , R1.APPLICANTDIV ";
        $query .= "     , R1.EXAMNO ";
        $query .= " ), MIN_TESTDIV_RECEPT AS ( ";
        $query .= "   SELECT ";
        $query .= "       MIN_TESTDIV.ENTEXAMYEAR ";
        $query .= "     , MIN_TESTDIV.APPLICANTDIV ";
        $query .= "     , MIN_TESTDIV.TESTDIV ";
        $query .= "     , MIN_TESTDIV.EXAMNO ";
        $query .= "     , R1.EXAM_TYPE ";
        $query .= "     , R1.RECEPTNO ";
        $query .= "   FROM MIN_TESTDIV ";
        $query .= "        INNER JOIN ENTEXAM_RECEPT_DAT R1 ON MIN_TESTDIV.ENTEXAMYEAR = R1.ENTEXAMYEAR ";
        $query .= "                                        AND MIN_TESTDIV.APPLICANTDIV = R1.APPLICANTDIV ";
        $query .= "                                        AND MIN_TESTDIV.TESTDIV = R1.TESTDIV ";
        $query .= "                                        AND MIN_TESTDIV.EXAMNO = R1.EXAMNO ";
        $query .= " ) ";


        $query .= " SELECT ";
        $query .= "   T1.ENTEXAMYEAR ";
        $query .= "   , R1.RECEPTNO ";
        $query .= "   , T1.EXAMNO ";
        $query .= "   , T1.NAME ";
        $query .= "   , T1.APPLICANTDIV ";
        // 合格した入試区分
        $query .= "   , D1.TESTDIV ";

        $query .= "   , T1.SUC_COURSECD ";
        $query .= "   , T1.SUC_MAJORCD ";
        $query .= "   , T1.SUC_COURSECODE ";

        // 入試区分項目の作成
        for ($i=0; $i < count($testDivList); $i++) { 
            $testDiv = $testDivList[$i];
            $query .= ", (SELECT TOTAL4 FROM ENTEXAM_RECEPT_DAT ";
            $query .= "   WHERE ENTEXAMYEAR = T1.ENTEXAMYEAR ";
            $query .= "     AND APPLICANTDIV = T1.APPLICANTDIV ";
            $query .= "     AND EXAMNO = T1.EXAMNO ";
            $query .= "     AND TESTDIV = '{$testDiv['VALUE']}' ";
            $query .= "  ) {$testDiv['TESTDIV_COLUMN_NAME']} ";
        }

        $query .= "   , RD_015.REMARK1 SP_SCHOLAR_CD ";
        $query .= "   , T3.SP_SCHOLAR_NAME ";
        $query .= " FROM ";
        $query .= "   ENTEXAM_RECEPT_DAT R1 ";
        $query .= "   INNER JOIN ENTEXAM_APPLICANTBASE_DAT T1 ";
        $query .= "     ON T1.ENTEXAMYEAR = R1.ENTEXAMYEAR ";
        $query .= "     AND T1.APPLICANTDIV = R1.APPLICANTDIV ";
        $query .= "     AND T1.EXAMNO = R1.EXAMNO ";
        $query .= "   INNER JOIN ENTEXAM_RECEPT_DETAIL_DAT RD_007 ";
        $query .= "      ON RD_007.ENTEXAMYEAR = R1.ENTEXAMYEAR ";
        $query .= "     AND RD_007.APPLICANTDIV = R1.APPLICANTDIV ";
        $query .= "     AND RD_007.TESTDIV = R1.TESTDIV ";
        $query .= "     AND RD_007.EXAM_TYPE = R1.EXAM_TYPE ";
        $query .= "     AND RD_007.RECEPTNO = R1.RECEPTNO ";
        $query .= "     AND RD_007.SEQ = '007' ";
        $query .= "   LEFT JOIN ENTEXAM_RECEPT_DETAIL_DAT RD_015 ";
        $query .= "      ON RD_015.ENTEXAMYEAR = R1.ENTEXAMYEAR ";
        $query .= "     AND RD_015.APPLICANTDIV = R1.APPLICANTDIV ";
        $query .= "     AND RD_015.TESTDIV = R1.TESTDIV ";
        $query .= "     AND RD_015.EXAM_TYPE = R1.EXAM_TYPE ";
        $query .= "     AND RD_015.RECEPTNO = R1.RECEPTNO ";
        $query .= "     AND RD_015.SEQ = '015' ";

        $query .= "   LEFT JOIN MIN_TESTDIV_RECEPT D1 ";
        $query .= "     ON T1.ENTEXAMYEAR = D1.ENTEXAMYEAR ";
        $query .= "     AND T1.APPLICANTDIV = D1.APPLICANTDIV ";
        $query .= "     AND T1.EXAMNO = D1.EXAMNO ";
        $query .= "   LEFT JOIN ENTEXAM_RECEPT_DETAIL_DAT MIN_RD_015 ";
        $query .= "      ON MIN_RD_015.ENTEXAMYEAR = D1.ENTEXAMYEAR ";
        $query .= "     AND MIN_RD_015.APPLICANTDIV = D1.APPLICANTDIV ";
        $query .= "     AND MIN_RD_015.TESTDIV = D1.TESTDIV ";
        $query .= "     AND MIN_RD_015.EXAM_TYPE = D1.EXAM_TYPE ";
        $query .= "     AND MIN_RD_015.RECEPTNO = D1.RECEPTNO ";
        $query .= "     AND MIN_RD_015.SEQ = '015' ";
        $query .= "   LEFT JOIN ENTEXAM_SPECIAL_SCHOLARSHIP_MST T3 ";
        $query .= "     ON MIN_RD_015.ENTEXAMYEAR = T3.ENTEXAMYEAR ";
        $query .= "     AND MIN_RD_015.APPLICANTDIV = T3.APPLICANTDIV ";
        $query .= "     AND MIN_RD_015.TESTDIV = T3.TESTDIV ";
        $query .= "     AND MIN_RD_015.REMARK1 = T3.SP_SCHOLAR_CD ";

        // コースコード変換
        list($courseCd, $majorCd, $examCourseCd) = explode('-', $model->field["EXAMCOURSECD"]);

        $query .= " WHERE ";
        $query .= "   R1.ENTEXAMYEAR = '{$model->objYear}' ";
        $query .= "   AND R1.APPLICANTDIV = '{$model->field["APPLICANTDIV"]}' ";
        $query .= "   AND R1.TESTDIV = '{$model->field["TESTDIV"]}' ";
        $query .= "   AND (RD_007.REMARK1 = '{$examCourseCd}' AND RD_007.REMARK7 = '1' ";
        $query .= "    OR  RD_007.REMARK2 = '{$examCourseCd}' AND RD_007.REMARK8 = '1') ";

        $query .= " ORDER BY ";
        $sort = $model->sortOrder == "1" ? " DESC " : "";
        if ($model->sortColumn == "0") {
            $query .= "     EXAMNO ".$sort;
        } else {
            for ($i=0; $i < count($testDivList); $i++) { 
                $testDiv = $testDivList[$i];
                if ($model->sortColumn == $testDiv['VALUE']) {
                    $query .= " ".$testDiv['TESTDIV_COLUMN_NAME'].$sort;
                }
            }
        }
        return $query;
    }

    //データ更新
    function getUpdateQuery($model) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //追加(座席番号)
        for ($i=0; $i < count($model->line["EXAMNO"]); $i++) { 
            $receptNo  = $model->line["RECEPTNO"][$i];
            $examNo    = $model->line["EXAMNO"][$i];
            $scholarCd = $model->line["SP_SCHOLAR_CD"][$i];

            $data = array();
            $data["REMARK1"][TEXT]          = $scholarCd;
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][FUNC]          = "SYSDATE()";

            $detailCnt = $db->getOne(knjl671aQuery::getReceptDetailCnt($model, 'COUNT(*)', $receptNo, '015'));
            if ($detailCnt > 0) {
                $where  = " WHERE ";
                $where .= "   ENTEXAMYEAR = '{$model->objYear}' ";
                $where .= "   AND APPLICANTDIV = '{$model->field["APPLICANTDIV"]}' ";
                $where .= "   AND TESTDIV = '{$model->field["TESTDIV"]}' ";
                $where .= "   AND EXAM_TYPE = '1' ";
                $where .= "   AND RECEPTNO = '{$receptNo}' ";
                $where .= "   AND SEQ = '015' ";
                $query = Query::updateSQL($data, "ENTEXAM_RECEPT_DETAIL_DAT", $where);
            } else {
                $data["ENTEXAMYEAR"][TEXT]  = $model->objYear;
                $data["APPLICANTDIV"][TEXT] = $model->field["APPLICANTDIV"];
                $data["TESTDIV"][TEXT]      = $model->field["TESTDIV"];
                $data["EXAM_TYPE"][TEXT]    = "1";
                $data["RECEPTNO"][TEXT]     = $receptNo;
                $data["SEQ"][TEXT]          = "015";
                $query = Query::insertSQL($data, "ENTEXAM_RECEPT_DETAIL_DAT");
            }
            $db->query($query);
        }

        $db->commit();
        Query::dbCheckIn($db);
        return true;
    }

    // 志願者基礎詳細データ(登録件数取得)
    function getReceptDetailCnt($model, $field, $receptNo, $seq) {
        $query  = "";
        $query .= " SELECT {$field} ";
        $query .= " FROM ENTEXAM_RECEPT_DETAIL_DAT ";
        $query .= " WHERE ";
        $query .= "   ENTEXAMYEAR = '{$model->objYear}' ";
        $query .= "   AND APPLICANTDIV = '{$model->field["APPLICANTDIV"]}' ";
        $query .= "   AND TESTDIV = '{$model->field["TESTDIV"]}' ";
        $query .= "   AND EXAM_TYPE = '1' ";
        $query .= "   AND RECEPTNO = '{$receptNo}' ";
        $query .= "   AND SEQ = '{$seq}' ";

        return $query;
    }

}
?>
