<?php

require_once('for_php7.php');

class knjl540aQuery extends Query {
    //名称マスタより取得
    function getNameMst($year, $namecd1) {
        $query  = " SELECT ";
        $query .= "     NAMECD2 AS VALUE,";
        $query .= "     NAMECD2 || ':' || NAME1 AS LABEL, ";
        $query .= "     NAMESPARE2 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR    = '".$year."' AND ";
        $query .= "     NAMECD1 = '".$namecd1."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //志望区分コンボ
    function getHopeCourseCd($model) {
        $query .= " SELECT DISTINCT ";
        $query .= "   T1.HOPE_COURSECODE || ':' || T2.HOPE_NAME AS LABEL, ";  // 志望区分コード
        $query .= "   T1.HOPE_COURSECODE AS VALUE ";                          // 志望区分名称
        $query .= " FROM ";
        $query .= "   ENTEXAM_HOPE_COURSE_YDAT T1 ";
        $query .= "   LEFT JOIN ENTEXAM_HOPE_COURSE_MST T2 ";
        $query .= "     ON T2.HOPE_COURSECODE = T1.HOPE_COURSECODE ";
        $query .= " WHERE ";
        $query .= "   ENTEXAMYEAR = '{$model->ObjYear}' ";
        $query .= " ORDER BY ";
        $query .= "  T1.HOPE_COURSECODE ";

        return $query;
    }

    //一覧表示
    function SelectQuery($model, $flg="") {
        $query  = " SELECT ";
        if ($flg == "FISCH_ONLY") {
            $query .= " DISTINCT ";
            $query .= "   BASE.FS_CD AS VALUE, ";
            $query .= "   FINS.FINSCHOOL_NAME_ABBV AS LABEL ";
        } else {
            $query .= "     RCPT.EXAMNO, ";
            $query .= "     RCPT.RECEPTNO, ";
            $query .= "     BASE.NAME, ";
            $query .= "     BASE.NAME_KANA, ";
            $query .= "     RCPT.JUDGEDIV, ";
            $query .= "     FINS.FINSCHOOL_NAME_ABBV AS FINSCHOOL_NAME ";
        }
        $query .= " FROM ";
        $query .= "     ENTEXAM_RECEPT_DAT RCPT ";
        $query .= "     LEFT JOIN V_ENTEXAM_APPLICANTBASE_DAT BASE ON BASE.ENTEXAMYEAR = RCPT.ENTEXAMYEAR ";
        $query .= "                                         AND BASE.APPLICANTDIV = RCPT.APPLICANTDIV ";
        $query .= "                                         AND BASE.TESTDIV      = RCPT.TESTDIV ";
        $query .= "                                         AND BASE.EXAMNO       = RCPT.EXAMNO ";
        $query .= "     LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT DET ON BASE.ENTEXAMYEAR = DET.ENTEXAMYEAR ";
        $query .= "                                         AND BASE.APPLICANTDIV = DET.APPLICANTDIV ";
        $query .= "                                         AND BASE.EXAMNO       = DET.EXAMNO ";
        $query .= "                                         AND DET.SEQ           = '001' ";
        $query .= "     LEFT JOIN ENTEXAM_SCHOOL_MST ENTS ON BASE.FS_CD = ENTS.ENTEXAM_SCHOOLCD ";
        $query .= "                                     AND BASE.ENTEXAMYEAR = ENTS.ENTEXAMYEAR ";
        $query .= "     LEFT JOIN FINSCHOOL_MST FINS ON ENTS.FINSCHOOLCD = FINS.FINSCHOOLCD ";
        $query .= " WHERE ";
        $query .= "         RCPT.ENTEXAMYEAR  = '".$model->ObjYear."'  ";
        $query .= "     AND RCPT.APPLICANTDIV = '".$model->applicantdiv."'  ";
        $query .= "     AND RCPT.TESTDIV      = '".$model->testdiv."' ";
        $query .= "     AND RCPT.EXAM_TYPE    = '".$model->examType."' ";
        $query .= "     AND VALUE(JUDGEDIV, '4') = '4' "; //欠席またはNULL
        if ($model->hopeCourseCode != $model->CONST_SELALL) {
            $query .= "     AND DET.REMARK10 = '".$model->hopeCourseCode."'  ";
        }
        if ($model->searchExamNo) {
            $query .= "    AND RCPT.EXAMNO = '".$model->searchExamNo."' ";
        }
        if ($flg != "FISCH_ONLY" && $model->finSchoolCd != "" && $model->finSchoolCd != $model->CONST_SELALL) {
            $query .= "  AND BASE.FS_CD = '".$model->finSchoolCd."'";
        }
        $query .= " ORDER BY ";
        if ($flg == "FISCH_ONLY") {
            $query .= "     BASE.FS_CD ";
        } else {
            $query .= "     RCPT.RECEPTNO ";
        }

        return $query;
    }

/***** 更新処理 *****/
    //更新
    function getUpdateQuery(&$model) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        foreach ($model->setReceptNo as $key => $ReceptNo) {
            //更新(ENTEXAM_RECEPT_DAT)
            $data = array();
            if ($model->arrTesAb[$ReceptNo]) {
                $data["JUDGEDIV"][TEXT]   = $model->arrTesAb[$ReceptNo];
            } else {
                $data["JUDGEDIV"][TEXT]   = "";
            }
            $data["REGISTERCD"][TEXT]      = STAFFCD;
            $data["UPDATED"][FUNC]         = "SYSDATE()";

            $where  = " WHERE ENTEXAMYEAR  = '".$model->ObjYear."' ";
            $where .= "   AND APPLICANTDIV = '".$model->applicantdiv."' ";
            $where .= "   AND TESTDIV      = '".$model->testdiv."' ";
            $where .= "   AND RECEPTNO     = '".$ReceptNo."' ";
            $where .= "   AND EXAM_TYPE    = '".$model->examType."' ";
            $where .= "   AND VALUE(JUDGEDIV, '4') = '4' "; //欠席またはNULL

            $query = Query::updateSQL($data, "ENTEXAM_RECEPT_DAT", $where);
            $db->query($query);

        }

        $db->commit();
        Query::dbCheckIn($db);
        return true;
    }
}
?>