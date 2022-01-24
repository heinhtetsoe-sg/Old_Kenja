<?php

require_once('for_php7.php');

class knjl031eQuery extends Query {
    //名称マスタより取得
    function getNameMst($model, $namecd1) {
        $query  = " SELECT ";
        $query .= "     NAMECD2 AS VALUE,";
        $query .= "     NAMECD2 || ':' || NAME1 AS LABEL, ";
        $query .= "     T1.* ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST T1 ";
        $query .= " WHERE ";
        $query .= "     YEAR    = '".$model->entexamyear."' AND ";
        $query .= "     NAMECD1 = '".$namecd1."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //会場データ取得
    function getEntexamHallYdat($model) {
        $query  = " SELECT ";
        $query .= "     EXAMHALLCD AS VALUE,";
        $query .= "     EXAMHALLCD || ':' || EXAMHALL_NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_HALL_YDAT ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR     = '".$model->entexamyear."' AND ";
        $query .= "     APPLICANTDIV    = '".$model->applicantdiv."' AND ";
        $query .= "     TESTDIV         = '".$model->testdiv."' AND ";
        $query .= "     EXAM_TYPE       = '".$model->examhall_type."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //一覧取得
    function selectQuery($model) {
        $query  = " SELECT ";
        $query .= "     L058.NAME1 AS DESIREDIV, ";
        $query .= "     L061.ABBV1 AS HEIGAN, ";
        $query .= "     BASE.EXAMNO, ";
        $query .= "     BASE.TESTDIV1, ";
        $query .= "     L045.NAME1 AS TESTDIV1NAME, ";
        $query .= "     BASE.NAME, ";
        $query .= "     FM.FINSCHOOL_NAME_ABBV AS FS_NAME, ";
        $query .= "     GRPD.EXAMHALLCD, ";
        $query .= "     GRPD.EXAMHALLGROUPCD, ";
        $query .= "     GRPD.EXAMHALLGROUP_ORDER ";
        $query .= " FROM ";
        $query .= "     V_ENTEXAM_APPLICANTBASE_DAT BASE ";
        $query .= "     LEFT JOIN ENTEXAM_HALL_GROUP_DAT GRPD ";
        $query .= "          ON GRPD.ENTEXAMYEAR    = BASE.ENTEXAMYEAR ";
        $query .= "         AND GRPD.APPLICANTDIV   = BASE.APPLICANTDIV ";
        $query .= "         AND GRPD.TESTDIV        = BASE.TESTDIV ";
        $query .= "         AND GRPD.EXAMNO         = BASE.EXAMNO ";
        $query .= "         AND GRPD.EXAMHALL_TYPE  = '".$model->examhall_type."' ";
        $query .= "     LEFT JOIN V_NAME_MST L045 ";
        $query .= "          ON L045.YEAR       = BASE.ENTEXAMYEAR ";
        $query .= "         AND L045.NAMECD1    = 'L045' ";
        $query .= "         AND L045.NAMECD2    = BASE.TESTDIV1 ";
        $query .= "     LEFT JOIN V_NAME_MST L058 ON L058.YEAR    = BASE.ENTEXAMYEAR ";
        $query .= "                              AND L058.NAMECD1 = 'L058' ";
        $query .= "                              AND L058.NAMECD2 = BASE.DESIREDIV ";
        $query .= "     LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT BD033 ON BD033.ENTEXAMYEAR = BASE.ENTEXAMYEAR ";
        $query .= "                              AND BD033.APPLICANTDIV = BASE.APPLICANTDIV ";
        $query .= "                              AND BD033.EXAMNO = BASE.EXAMNO ";
        $query .= "                              AND BD033.SEQ = '033' ";
        $query .= "     LEFT JOIN V_NAME_MST L061 ON L061.YEAR    = BASE.ENTEXAMYEAR ";
        $query .= "                              AND L061.NAMECD1 = 'L061' ";
        $query .= "                              AND L061.NAMECD2 = BD033.REMARK3 ";
        $query .= "                              AND VALUE(L061.NAMESPARE1, '0') = '1' ";
        $query .= "     LEFT JOIN FINSCHOOL_MST FM ";
        $query .= "       ON FM.FINSCHOOLCD = BASE.FS_CD ";
        $query .= " WHERE ";
        $query .= "     BASE.ENTEXAMYEAR    = '".$model->entexamyear."' AND ";
        $query .= "     BASE.APPLICANTDIV   = '".$model->applicantdiv."' AND ";
        $query .= "     BASE.TESTDIV        = '".$model->testdiv."' ";
        $query .= " ORDER BY ";
        if ($model->testdiv == '21' || $model->testdiv == '22') {
            $query .= "     CASE WHEN L061.NAMECD1 IS NULL THEN 0 ELSE 1 END, ";
        }
        if ($model->testdiv == '10' && $model->examhall_type == '1') {
            $query .= "     BASE.TESTDIV1, ";
        }
        $query .= "     BASE.EXAMNO ";

        return $query;
    }

    //データ取得
    function getEntexamHallGroupDat($model) {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_HALL_GROUP_DAT ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR     = '".$model->entexamyear."' AND ";
        $query .= "     APPLICANTDIV    = '".$model->applicantdiv."' AND ";
        $query .= "     TESTDIV         = '".$model->testdiv."' AND ";
        $query .= "     EXAMHALL_TYPE   = '".$model->examhall_type."' ";

        return $query;
    }

    //更新処理
    function getUpdateQuery(&$model) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //データ取得
        $tmpData = array();
        $result = $db->query(knjl031eQuery::getEntexamHallGroupDat($model));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $tmpData[$row["EXAMNO"]] = $row;
        }
        $result->free();

        //更新：全生徒対象、一括設定：チェックの生徒が対象
        $examnoArray = ($model->cmd == 'update') ? explode(",", $model->hidExamNo) : $model->chkExamNo;

        //更新
        foreach ($examnoArray as $examno) {
            $data = array();
            $updFlg = true;

            if ($model->cmd == 'update') {
                $data["EXAMHALLCD"][TEXT]           = $model->updField[$examno]["examhallcd"];
                $data["EXAMHALLGROUPCD"][TEXT]      = $model->updField[$examno]["examhallgroupcd"];
                $data["EXAMHALLGROUP_ORDER"][TEXT]  = $model->updField[$examno]["examhallgroup_order"];
            } else {
                //生徒の会場コードと一括設定の会場コードが同じとき、更新しない
                if ($tmpData[$examno]["EXAMHALLCD"] == $model->examhallcd) $updFlg = false;

                $data["EXAMHALLCD"][TEXT]           = $model->examhallcd;
                $data["EXAMHALLGROUPCD"][TEXT]      = '';
                $data["EXAMHALLGROUP_ORDER"][TEXT]  = '';
            }
            $data["REGISTERCD"][TEXT]           = STAFFCD;
            $data["UPDATED"][FUNC]              = "SYSDATE()";

            if ($updFlg) {
                if (get_count($tmpData[$examno]) > 0) {
                    $where  = " WHERE ENTEXAMYEAR   = '".$model->entexamyear."' ";
                    $where .= "   AND APPLICANTDIV  = '".$model->applicantdiv."' ";
                    $where .= "   AND TESTDIV       = '".$model->testdiv."' ";
                    $where .= "   AND EXAMHALL_TYPE = '".$model->examhall_type."' ";
                    $where .= "   AND EXAMNO        = '".$examno."' ";

                    $db->query(Query::updateSQL($data, "ENTEXAM_HALL_GROUP_DAT", $where));
                } else {
                    $data["ENTEXAMYEAR"][TEXT]      = $model->entexamyear;
                    $data["APPLICANTDIV"][TEXT]     = $model->applicantdiv;
                    $data["TESTDIV"][TEXT]          = $model->testdiv;
                    $data["EXAMHALL_TYPE"][TEXT]    = $model->examhall_type;
                    $data["EXAMNO"][TEXT]           = $examno;

                    $db->query(Query::insertSQL($data, "ENTEXAM_HALL_GROUP_DAT"));
                }
            }
        }

        $db->commit();
        Query::dbCheckIn($db);
        return;
    }
}
?>
