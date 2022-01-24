<?php

require_once('for_php7.php');

class knjl075qQuery extends Query {
    //名称マスタより取得
    function getNameMst($year, $namecd1, $nameOnly="") {
        $query  = " SELECT ";
        $query .= "     NAMECD2 AS VALUE,";
        if ($nameOnly) {
            $query .= "     NAME1 AS LABEL, ";
        } else {
            $query .= "     NAMECD2 || ':' || NAME1 AS LABEL, ";
        }
        $query .= "     NAMESPARE2 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR    = '".$year."' AND ";
        $query .= "     NAMECD1 = '".$namecd1."' ";
        if ($namecd1 == "L003") {
            $query .= "     AND NAMESPARE3 = '".SCHOOLKIND."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //一覧表示
    function SelectQuery($model) {
        $query  = " SELECT ";
        $query .= "     DTAL_D.EXAMNO, ";
        $query .= "     BASE_D.NAME, ";
        $query .= "     CASE WHEN DTAL_D.REMARK1 = '1' THEN '特別'  ";
        $query .= "          WHEN DTAL_D.REMARK1 = '2' THEN '一般' END AS SKIBOU, ";
        $query .= "     DTAL_D.REMARK3 AS STOUKYU, ";
        $query .= "     DTAL_D.REMARK4 AS HTOUKYU ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_APPLICANTBASE_DETAIL_DAT DTAL_D ";
        $query .= " INNER JOIN ENTEXAM_APPLICANTBASE_DAT BASE_D ";
        $query .= "                  ON DTAL_D.ENTEXAMYEAR  = BASE_D.ENTEXAMYEAR ";
        $query .= "                 AND DTAL_D.APPLICANTDIV = BASE_D.APPLICANTDIV ";
        $query .= "                 AND DTAL_D.EXAMNO       = BASE_D.EXAMNO ";
        $query .= " WHERE ";
        $query .= "         DTAL_D.ENTEXAMYEAR  = '{$model->ObjYear}' ";
        $query .= "     AND DTAL_D.APPLICANTDIV = '{$model->applicantdiv}' ";
        $query .= "     AND BASE_D.TESTDIV      = '{$model->testdiv}' ";
        $query .= "     AND DTAL_D.SEQ          = '023' ";
        $query .= "     AND DTAL_D.REMARK2      = '1' ";//スカラー採用
        $query .= " ORDER BY ";
        $query .= "     DTAL_D.EXAMNO ";

        return $query;
    }


    //更新
    function getUpdateQuery(&$model) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        foreach ($model->arr_examno as $key => $examno) {
            //受付データ
            $data = array();
            $data["REMARK3"][TEXT]          = $model->stoukyu[$examno];
            if ($model->testdiv == "1" || $model->testdiv == "2") {
                $data["REMARK4"][TEXT]          = $model->htoukyu[$examno] ;
            }
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][FUNC]          = "sysdate()";
            //条件
            $where  = " WHERE ENTEXAMYEAR   = '{$model->ObjYear}' ";
            $where .= "   AND APPLICANTDIV  = '{$model->applicantdiv}' ";
            $where .= "   AND EXAMNO        = '".$examno."' ";
            $where .= "   AND SEQ           = '023' ";
            $query = Query::updateSQL($data, "ENTEXAM_APPLICANTBASE_DETAIL_DAT", $where);
            $db->query($query);

        }

        $db->commit();
        Query::dbCheckIn($db);

        return true;
    }
}
?>