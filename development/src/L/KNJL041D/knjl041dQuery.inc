<?php

require_once('for_php7.php');

class knjl041dQuery extends Query {
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

    //会場コンボ
    function getExamHallCd($model) {
        $query  = " SELECT ";
        $query .= "     HALL.EXAMHALLCD AS VALUE, ";
        $query .= "     HALL.EXAMHALL_NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_HALL_YDAT HALL ";
        $query .= " WHERE ";
        $query .= "         ENTEXAMYEAR  = '".$model->ObjYear."' ";
        $query .= "     AND APPLICANTDIV = '".$model->applicantdiv."' ";
        $query .= "     AND TESTDIV      = '".$model->testdiv."' ";
        $query .= "     AND EXAM_TYPE    = '".$model->examType."' ";
        $query .= " ORDER BY ";
        $query .= "     EXAMHALLCD ";

        return $query;
    }

    //一覧表示
    function SelectQuery($model) {
        $query  = " SELECT ";
        $query .= "     BASE.EXAMNO, ";
        $query .= "     BASE.NAME, ";
        $query .= "     Z002.ABBV1 AS SEX, ";
        $query .= "     FINS.FINSCHOOL_NAME_ABBV, ";
        $query .= "     BASE.JUDGEMENT, ";
        $query .= "     INTV.INTERVIEW_VALUE, ";
        $query .= "     INTV.ATTEND_FLG ";
        $query .= " FROM ";
        $query .= "     V_ENTEXAM_APPLICANTBASE_DAT BASE  ";
        $query .= "     LEFT JOIN V_NAME_MST Z002 ON Z002.YEAR    = BASE.ENTEXAMYEAR ";
        $query .= "                              AND Z002.NAMECD1 = 'Z002' ";
        $query .= "                              AND Z002.NAMECD2 = BASE.SEX ";
        $query .= "     LEFT JOIN FINSCHOOL_MST FINS ON BASE.FS_CD = FINS.FINSCHOOLCD ";
        $query .= "     LEFT JOIN ENTEXAM_INTERVIEW_DAT INTV ON BASE.ENTEXAMYEAR  = INTV.ENTEXAMYEAR ";
        $query .= "                                         AND BASE.APPLICANTDIV = INTV.APPLICANTDIV ";
        $query .= "                                         AND BASE.TESTDIV      = INTV.TESTDIV ";
        $query .= "                                         AND BASE.EXAMNO       = INTV.EXAMNO ";
        $query .= " WHERE ";
        $query .= "         BASE.ENTEXAMYEAR  = '".$model->ObjYear."'  ";
        $query .= "     AND BASE.APPLICANTDIV = '".$model->applicantdiv."'  ";
        $query .= "     AND BASE.TESTDIV      = '".$model->testdiv."' ";
        if ($model->examhallCd != "9999") {
            $query .= "     AND BASE.EXAMNO IN (SELECT ";
            $query .= "                             BASE.EXAMNO ";
            $query .= "                         FROM ";
            $query .= "                             ENTEXAM_HALL_YDAT HALL ";
            $query .= "                         WHERE ";
            $query .= "                                 BASE.ENTEXAMYEAR  = HALL.ENTEXAMYEAR ";
            $query .= "                             AND BASE.APPLICANTDIV = HALL.APPLICANTDIV ";
            $query .= "                             AND BASE.TESTDIV      = HALL.TESTDIV ";
            $query .= "                             AND HALL.EXAM_TYPE    = '".$model->examType."' ";
            $query .= "                             AND HALL.EXAMHALLCD   = '".$model->examhallCd."' ";
            $query .= "                             AND BASE.EXAMNO BETWEEN HALL.S_RECEPTNO AND HALL.E_RECEPTNO ";
            $query .= "                         ) ";
        }
        $query .= " ORDER BY ";
        $query .= "     BASE.EXAMNO ";

        return $query;
    }

/***** 更新処理 *****/
    //データ取得(ENTEXAM_INTERVIEW_DAT)
    function getCountInterviewDat($model, $examNo) {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_INTERVIEW_DAT ";
        $query .= " WHERE ";
        $query .= "         ENTEXAMYEAR  = '".$model->ObjYear."' ";
        $query .= "     AND APPLICANTDIV = '".$model->applicantdiv."' ";
        $query .= "     AND TESTDIV      = '".$model->testdiv."' ";
        $query .= "     AND EXAMNO       = '".$examNo."' ";

        return $query;
    }

    //更新
    function getUpdateQuery(&$model) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        foreach ($model->setExamNo as $key => $examno) {
            //更新(ENTEXAM_APPLICANTBASE_DAT)
            $data = array();
            if ($model->arrTesAb[$examno]) {
                $data["JUDGEMENT"][TEXT]        = $model->arrTesAb[$examno];
            } else {
                if ($model->arrIntAb[$examno]) {
                    $data["JUDGEMENT"][TEXT]        = "0";
                } else {
                    $data["JUDGEMENT"][TEXT]        = "";
                }
            }
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][FUNC]          = "SYSDATE()";

            $where  = " WHERE ENTEXAMYEAR  = '".$model->ObjYear."' ";
            $where .= "   AND APPLICANTDIV = '".$model->applicantdiv."' ";
            $where .= "   AND EXAMNO       = '".$examno."' ";
            $where .= "   AND (VALUE(JUDGEMENT, '4') = '4' OR JUDGEMENT = '0') "; //欠席またはNULLまたは0

            $query = Query::updateSQL($data, "ENTEXAM_APPLICANTBASE_DAT", $where);
            $db->query($query);

            //更新(ENTEXAM_INTERVIEW_DAT)
            $data = array();
            $data["ENTEXAMYEAR"][TEXT]      = $model->ObjYear;
            $data["APPLICANTDIV"][TEXT]     = $model->applicantdiv;
            $data["TESTDIV"][TEXT]          = $model->testdiv;
            $data["EXAMNO"][TEXT]           = $examno;
            $data["ATTEND_FLG"][TEXT]       = ($model->arrIntAb[$examno]) ? $model->arrIntAb[$examno]: "";
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][FUNC]          = "SYSDATE()";

            $cnt = $db->getOne(knjl041dQuery::getCountInterviewDat($model, $examno));
            if ($cnt == 0) {
                $query = Query::insertSQL($data, "ENTEXAM_INTERVIEW_DAT");
            } else {
                $where  = " WHERE ENTEXAMYEAR  = '".$model->ObjYear."' ";
                $where .= "   AND APPLICANTDIV = '".$model->applicantdiv."' ";
                $where .= "   AND TESTDIV      = '".$model->testdiv."' ";
                $where .= "   AND EXAMNO       = '".$examno."' ";
                $query = Query::updateSQL($data, "ENTEXAM_INTERVIEW_DAT", $where);
            }
            $db->query($query);
        }

        $db->commit();
        Query::dbCheckIn($db);
        return true;
    }
}
?>