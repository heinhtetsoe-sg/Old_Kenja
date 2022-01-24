<?php

require_once('for_php7.php');

class knjl014rQuery extends Query
{

    //名称マスタよりデータを取得
    public function get_name_cd($year, $cd, $cd2 = "")
    {
        $query  = " SELECT ";
        $query .= "     NAMECD2 || ':' || NAME1 AS LABEL, ";
        $query .= "     NAMECD2 AS VALUE ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$year."' ";
        $query .= "     AND NAMECD1 = '".$cd."' ";
        if ($cd2) {
            $query .= "     AND NAMECD2 = '".$cd2."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //データ一覧取得
    public function getMainQuery($model, $cmb = "")
    {

        $query  = " SELECT DISTINCT ";
        if ($cmb) {
            $query .= "     BEF.BEFORE_PAGE AS VALUE, ";
            $query .= "     BEF.BEFORE_PAGE AS LABEL ";
        } else {
            $query .= "     BEF.BEFORE_PAGE, ";
            $query .= "     BEF.BEFORE_SEQ, ";
            $query .= "     BASE.FS_CD, ";
            $query .= "     FIN.FINSCHOOL_NAME, ";
            $query .= "     BASE.EXAMNO, ";
            $query .= "     DET2.REMARK1 || '-' || DET2.REMARK2 AS JIZEN, ";
            $query .= "     BASE.JUDGEMENT, ";
            $query .= "     BASE.NAME, ";
            $query .= "     BASE.NAME_KANA, ";
            $query .= "     BEF.SENBATU1_SCHOOL, ";
            $query .= "     BEF.SENBATU1_MAJOR ";
        }
        $query .= " FROM ";
        $query .= "     ENTEXAM_APPLICANT_BEFORE_DAT BEF ";
        $query .= "     INNER JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT DET2 ON BEF.ENTEXAMYEAR = DET2.ENTEXAMYEAR ";
        $query .= "          AND DET2.APPLICANTDIV = BEF.APPLICANTDIV ";
        $query .= "          AND DET2.SEQ = '002' ";
        $query .= "          AND DET2.REMARK1 = BEF.BEFORE_PAGE ";
        $query .= "          AND DET2.REMARK2 = BEF.BEFORE_SEQ ";
        $query .= "     INNER JOIN ENTEXAM_APPLICANTBASE_DAT BASE ON BEF.ENTEXAMYEAR = BASE.ENTEXAMYEAR ";
        $query .= "          AND DET2.APPLICANTDIV = BASE.APPLICANTDIV ";
        $query .= "          AND DET2.ENTEXAMYEAR = BASE.ENTEXAMYEAR ";
        $query .= "          AND DET2.EXAMNO = BASE.EXAMNO ";
        $query .= "          AND BEF.APPLICANTDIV = BASE.APPLICANTDIV ";
        $query .= "          AND BEF.TESTDIV = BASE.TESTDIV ";
        $query .= "     LEFT JOIN FINSCHOOL_MST FIN ON BEF.FS_CD = FIN.FINSCHOOLCD ";
        $query .= " WHERE ";
        $query .= "     BEF.ENTEXAMYEAR = '".$model->year."' ";
        $query .= "     AND BEF.APPLICANTDIV = '".$model->field["APPLICANTDIV"]."' ";
        $query .= "     AND BEF.TESTDIV = '".$model->field["TESTDIV"]."' ";
        $query .= "     AND BEF.SENBATU1_SCHOOL IS NOT NULL ";
        if ($cmb) {
            $query .= " ORDER BY ";
            $query .= "     VALUE ";
        } else {
            if ($model->field["BEFORE_PAGE"]) {
                $query .= "     AND BEF.BEFORE_PAGE = '".$model->field["BEFORE_PAGE"]."' ";
            }
            $query .= " ORDER BY ";
            $query .= "     BEF.BEFORE_PAGE, ";
            $query .= "     BEF.BEFORE_SEQ, ";
            $query .= "     BASE.FS_CD, ";
            $query .= "     BASE.EXAMNO ";
        }

        return $query;
    }

    //得点データありか
    public function getScoreDat($model)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_SCORE_DAT T1 ";
        $query .= " WHERE ";
        $query .= "     T1.ENTEXAMYEAR = '".$model->year."' ";
        $query .= "     AND T1.APPLICANTDIV = '".$model->field["APPLICANTDIV"]."' ";
        $query .= "     AND T1.TESTDIV = '".$model->field["TESTDIV"]."' ";

        return $query;
    }

    //DELETE
    public function DeleteDetailDataQuery($db, $model, $examno)
    {
        $query  = " DELETE FROM ";
        $query .= "     ENTEXAM_APPLICANTBASE_DETAIL_DAT ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR  = '".$model->year."' ";
        $query .= " AND APPLICANTDIV = '".$model->field["APPLICANTDIV"]."' ";
        $query .= " AND EXAMNO       = '".$examno."' ";
        $query .= " AND SEQ          = '003' ";

        $db->query($query);
        return;
    }

    //UPDATE
    public function &getUpdateQuery($db, $model, $examno, $mijuken)
    {

        $db->autoCommit(false);

        $data = array();
        $data["JUDGEMENT"][TEXT]        = $mijuken ? "4" : "";
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][NUMBER]        = "sysdate()";

        $where  = " WHERE ";
        $where .= "     ENTEXAMYEAR  = '".$model->year."' ";
        $where .= " AND APPLICANTDIV = '".$model->field["APPLICANTDIV"]."' ";
        $where .= " AND EXAMNO       = '".$examno."' ";

        $query = Query::updateSQL($data, "ENTEXAM_APPLICANTBASE_DAT", $where);
        $db->query($query);

        knjl014rQuery::DeleteDetailDataQuery($db, $model, $examno);

        //ENTEXAM_APPLICANTBASE_DETAIL_DATの追加
        if ($mijuken) {
            $data2 = array();
            $data2["ENTEXAMYEAR"][TEXT]     = $model->year;
            $data2["APPLICANTDIV"][TEXT]    = $model->field["APPLICANTDIV"];
            $data2["EXAMNO"][TEXT]          = $examno;
            $data2["SEQ"][TEXT]             = "003";
            $data2["REMARK1"][TEXT]         = "1";
            $data2["REGISTERCD"][TEXT]      = STAFFCD;
            $data2["UPDATED"][NUMBER]       = "sysdate()";

            $query = Query::insertSQL($data2, "ENTEXAM_APPLICANTBASE_DETAIL_DAT");
            $db->query($query);
        }

        $db->commit();
        return;
    }
}
