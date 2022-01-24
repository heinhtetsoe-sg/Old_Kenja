<?php

require_once('for_php7.php');

class knjl771hquery extends Query
{
    //名称マスタ取得
    public function getNameMst($year, $nameCd1, $nameCd2 = "")
    {
        $query  = "";
        $query .= " SELECT ";
        $query .= "     NAMECD2 VALUE ";
        $query .= "   , NAMECD2 || ':' || NAME1 LABEL ";
        $query .= "   , NAMESPARE2 ";
        $query .= " FROM V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$year}' ";
        if ($nameCd1 != "") {
            $query .= "     AND NAMECD1 = '{$nameCd1}' ";
        }
        if ($nameCd2 != "") {
            $query .= "     AND NAMECD2 = '{$nameCd2}' ";
        }

        return $query;
    }

    //入試区分取得
    public function getEntexamTestDivMst($model)
    {
        $query  = "";
        $query .= " SELECT ";
        $query .= "     TESTDIV VALUE ";
        $query .= "   , TESTDIV || ':' || TESTDIV_NAME LABEL ";
        $query .= " FROM ENTEXAM_TESTDIV_MST ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR      = '{$model->examyear}' ";
        $query .= "     AND APPLICANTDIV = '{$model->applicantdiv}' ";

        return $query;
    }

    //繰上候補者を合格点の高い順に順位付け
    public function &getSelectReceptRankQuery($model)
    {
        $query  = " SELECT ";
        $query .= "     R1.RECEPTNO, ";
        $query .= "     RANK() OVER(ORDER BY FLOAT(RD009.REMARK6) DESC) AS TOTAL_RANK, ";
        $query .= "     B1.EXAMNO ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_RECEPT_DAT R1 ";
        $query .= "     INNER JOIN ENTEXAM_APPLICANTBASE_DAT B1 ";
        $query .= "          ON B1.ENTEXAMYEAR = R1.ENTEXAMYEAR ";
        $query .= "         AND B1.APPLICANTDIV = R1.APPLICANTDIV ";
        $query .= "         AND B1.EXAMNO = R1.EXAMNO ";
        $query .= "     INNER JOIN ENTEXAM_RECEPT_DETAIL_DAT RD009 ";
        $query .= "         ON RD009.ENTEXAMYEAR = R1.ENTEXAMYEAR ";
        $query .= "         AND RD009.APPLICANTDIV = R1.APPLICANTDIV ";
        $query .= "         AND RD009.TESTDIV = R1.TESTDIV ";
        $query .= "         AND RD009.EXAM_TYPE = R1.EXAM_TYPE ";
        $query .= "         AND RD009.RECEPTNO = R1.RECEPTNO ";
        $query .= "         AND RD009.SEQ = '009' ";
        $query .= " WHERE ";
        $query .= "     R1.ENTEXAMYEAR      = '{$model->examyear}' ";
        $query .= "     AND R1.APPLICANTDIV = '{$model->applicantdiv}' ";
        $query .= "     AND R1.TESTDIV      = '{$model->testdiv}' ";
        $query .= "     AND R1.EXAM_TYPE    = '1' ";
        $query .= "     AND R1.JUDGEDIV     = '4' ";

        return $query;
    }

    //データカウント(ENTEXAM_RECEPT_DETAIL_DAT)
    public function getCountReceptDetaildata($model, $receptNo, $seq)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_RECEPT_DETAIL_DAT ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR        = '{$model->examyear}' ";
        $query .= "     AND APPLICANTDIV   = '{$model->applicantdiv}' ";
        $query .= "     AND TESTDIV        = '{$model->testdiv}' ";
        $query .= "     AND EXAM_TYPE      = '1' ";
        $query .= "     AND RECEPTNO       = '{$receptNo}' ";
        $query .= "     AND SEQ            = '{$seq}' ";

        return $query;
    }

    //更新 -- ENTEXAM_RECEPT_DETAIL_DAT
    public function &getUpdateReceptDetailDatQuery($db, $model, $rankData)
    {
        $seq = "015";
        //cnt
        $getCount = $db->getOne(knjl771hQuery::getCountReceptDetaildata($model, $rankData["RECEPTNO"], $seq));

        $data = array();
        $data["REMARK3"][TEXT]      = $rankData["TOTAL_RANK"];
        $data["REGISTERCD"][TEXT]   = STAFFCD;
        $data["UPDATED"][FUNC]      = "sysdate()";
        if ($getCount == 0) {
            $data["ENTEXAMYEAR"][TEXT]  = $model->examyear;
            $data["APPLICANTDIV"][TEXT] = $model->applicantdiv;
            $data["TESTDIV"][TEXT]      = $model->testdiv;
            $data["EXAM_TYPE"][TEXT]    = '1';
            $data["RECEPTNO"][TEXT]     = $rankData["RECEPTNO"];
            $data["SEQ"][TEXT]          = $seq;
            $query = Query::insertSQL($data, "ENTEXAM_RECEPT_DETAIL_DAT");
        } else {
            $where  = " WHERE ";
            $where .= "     ENTEXAMYEAR        = '{$model->examyear}' ";
            $where .= "     AND APPLICANTDIV   = '{$model->applicantdiv}' ";
            $where .= "     AND TESTDIV        = '{$model->testdiv}' ";
            $where .= "     AND EXAM_TYPE      = '1' ";
            $where .= "     AND RECEPTNO       = '{$rankData["RECEPTNO"]}' ";
            $where .= "     AND SEQ            = '{$seq}' ";
            $query = Query::updateSQL($data, "ENTEXAM_RECEPT_DETAIL_DAT", $where);
        }

        return $query;
    }

    //更新 -- ENTEXAM_APPLICANTBASE_DAT
    public function &getUpdateBaseDatQuery($model, $rankData)
    {
        $data = array();
        $data["SUB_ORDER"][TEXT]    = $rankData["TOTAL_RANK"];
        $data["REGISTERCD"][TEXT]   = STAFFCD;
        $data["UPDATED"][FUNC]      = "sysdate()";
        $where  = " WHERE ";
        $where .= "     ENTEXAMYEAR      = '{$model->examyear}' ";
        $where .= "     AND APPLICANTDIV = '{$model->applicantdiv}' ";
        $where .= "     AND EXAMNO       = '{$rankData["EXAMNO"]}' ";
        $query = Query::updateSQL($data, "ENTEXAM_APPLICANTBASE_DAT", $where);

        return $query;
    }
}
