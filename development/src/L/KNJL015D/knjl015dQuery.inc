<?php

require_once('for_php7.php');

class knjl015dQuery extends Query {

    //名称マスタより取得
    function getNameMst($namecd1, $year) {
        $query  = " SELECT ";
        $query .= "     NAMECD2 AS VALUE,";
        $query .= "     NAMECD2 || ':' || NAME1 AS LABEL, ";
        $query .= "     NAMESPARE2 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "         YEAR = '".$year."' ";
        $query .= "     AND NAMECD1 = '".$namecd1."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";
        return $query;
    }

    //中学校リスト取得
    function getFinSchool($model)
    {
        $query .= " SELECT ";
        $query .= "     T1.FS_CD AS VALUE, ";
        $query .= "     T1.FS_CD || ':' || NAME_FS.FINSCHOOL_NAME_ABBV AS LABEL ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_APPLICANTBASE_DAT T1 ";
        $query .= "     LEFT JOIN FINSCHOOL_MST NAME_FS ";
        $query .= "      ON NAME_FS.FINSCHOOLCD = T1.FS_CD ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR = '{$model->ObjYear}' ";
        $query .= " AND APPLICANTDIV = '{$model->applicantdiv}' ";
        $query .= " AND TESTDIV = '{$model->testdiv}' ";
        $query .= " GROUP BY ";
        $query .= "     T1.FS_CD, NAME_FS.FINSCHOOL_NAME_ABBV ";
        $query .= " ORDER BY ";
        $query .= "     T1.FS_CD ";

        return $query;
    }

    //一覧表示
    function SelectQuery($model, $flg)
    {
        $query .= " SELECT ";
        if ($flg == "COUNT") {
            $query .= "     COUNT(*) AS CNT ";
        } else {
            $query .= "     T1.EXAMNO, ";
            $query .= "     T1.NAME, ";
            $query .= "     T1.SEX, ";
            $query .= "     NAME_SEX.NAME1 as NAME_SEX, ";
            $query .= "     TD1.REMARK1, ";
            $query .= "     TD1.REMARK2 ";

        }
        $query .= "  FROM ";
        $query .= "     ENTEXAM_APPLICANTBASE_DAT T1 ";
        $query .= "     LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT TD1 ";
        $query .= "           ON T1.ENTEXAMYEAR = TD1.ENTEXAMYEAR ";
        $query .= "           AND T1.APPLICANTDIV = TD1.APPLICANTDIV ";
        $query .= "           AND T1.EXAMNO = TD1.EXAMNO ";
        $query .= "           AND TD1.SEQ = '005' ";
        $query .= "     LEFT JOIN NAME_MST NAME_SEX ";
        $query .= "           ON NAME_SEX.NAMECD1 = 'Z002' ";
        $query .= "           AND T1.SEX = NAME_SEX.NAMECD2 ";
        $query .= " WHERE T1.ENTEXAMYEAR    = '".$model->ObjYear."'";
        $query .= "   AND T1.APPLICANTDIV   = '".$model->applicantdiv."' ";
        $query .= "   AND T1.TESTDIV        = '".$model->testdiv."' ";
        if ($model->finschool_cd !== 'ZZZZZZZ') {
            $query .= "   AND T1.FS_CD      = '".$model->finschool_cd."' ";
        }
        if ($flg != "COUNT") {
            $query .= " ORDER BY SUBSTR(CHAR(DECIMAL(T1.EXAMNO, 10, 0)),1,10) ";
        }
        return $query;
    }

    //登録チェック
    function chkDataQuery($model, $receptno_array)
    {
        $db = Query::dbCheckOut();

        $query .= " SELECT ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= "  FROM ";
        $query .= "     ENTEXAM_APPLICANTBASE_DETAIL_DAT TD1 ";
        $query .= " WHERE TD1.ENTEXAMYEAR    = '".$model->ObjYear."'";
        $query .= "   AND TD1.APPLICANTDIV   = '".$model->applicantdiv."' ";
        $query .= "   AND TD1.EXAMNO        = '".$receptno_array[0]."' ";
        $query .= "   AND TD1.SEQ      = '005' ";

        $retval =$db->getOne($query);
        Query::dbCheckIn($db);
        return $retval;
    }

    //新規挿入
    function insertQuery($model, $receptno_array)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);
        
        $data = array();
        $data["ENTEXAMYEAR"][TEXT]  = $model->ObjYear;
        $data["APPLICANTDIV"][TEXT] = $model->applicantdiv;
        $data["EXAMNO"][TEXT]       = $receptno_array[0];
        $data["SEQ"][TEXT]          = "005";
        $data["REMARK1"][TEXT]      = $model->selection_Remark[$receptno_array[1]];
        $data["REMARK2"][TEXT]      = $model->selection_Remark2[$receptno_array[1]];
        $data["REGISTERCD"][TEXT]   = STAFFCD;
        $data["UPDATED"][FUNC]      = "sysdate()";

        $query = Query::insertSQL($data, "ENTEXAM_APPLICANTBASE_DETAIL_DAT");
        $db->query($query);
        
        $db->commit();
        Query::dbCheckIn($db);
        return true;
    }

    //更新
    function UpdateQuery(&$model, $receptno_array)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);
        
        $data = array();
        $data["REMARK1"][TEXT]    = $model->selection_Remark[$receptno_array[1]];
        $data["REMARK2"][TEXT]    = $model->selection_Remark2[$receptno_array[1]];
        $data["REGISTERCD"][TEXT] = STAFFCD;
        $data["UPDATED"][FUNC]    = "sysdate()";

        $where  = " WHERE ";
        $where .= "     ENTEXAMYEAR      = '".$model->ObjYear."' ";
        $where .= "     AND APPLICANTDIV = '".$model->applicantdiv."' ";
        $where .= "     AND EXAMNO       = '".$receptno_array[0]."' ";
        $where .= "     AND SEQ          = '005' ";

        $query = Query::updateSQL($data, "ENTEXAM_APPLICANTBASE_DETAIL_DAT", $where);
        $db->query($query);

        $db->commit();
        Query::dbCheckIn($db);
        return true;
    }
}
?>