<?php

require_once('for_php7.php');

class knjl502jQuery extends Query {

    //名称マスタより取得
    function getNameMst($model, $namecd1, $nameCd2="") {
        $query  = " SELECT ";
        if ($nameCd2) {
            $query .= "     NAME1 ";
        } else {
            $query .= "     NAMECD2 AS VALUE, ";
            $query .= "     NAMECD2 || ':' || NAME1 AS LABEL, ";
            $query .= "     NAMECD2, ";
            $query .= "     NAME1, ";
            $query .= "     NAMESPARE2 ";
        }
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "         YEAR    = '".$model->year."' ";
        $query .= "     AND NAMECD1 = '".$namecd1."' ";
        if ($nameCd2) {
            $query .= "     AND NAMECD2 = '".$nameCd2."' ";
        } else {
            $query .= " ORDER BY ";
            $query .= "     VALUE ";
        }

        return $query;
    }

    //リスト取得
    function selectQuery($model, $examType="") {
        $query  = " SELECT ";
        if ($examType !== "") {
            $query .= "     COUNT(*) ";
        } else {
            $query .= "     MAIN.ENTEXAMYEAR, ";
            $query .= "     MAIN.APPLICANTDIV, ";
            $query .= "     MAIN.EXAM_TYPE, ";
            $query .= "     MAIN.EXAMTYPE_NAME, ";
            $query .= "     MAIN.EXAMTYPE_NAME_ABBV, ";
            $query .= "     L009.NAME1 AS SUBCLASS ";
        }
        $query .= " FROM ";
        $query .= "     ENTEXAM_EXAMTYPE_MST MAIN ";
        $query .= "     LEFT JOIN ENTEXAM_EXAMTYPE_SUBCLASS_MST SUBM ON MAIN.ENTEXAMYEAR  = SUBM.ENTEXAMYEAR ";
        $query .= "                                                 AND MAIN.APPLICANTDIV = SUBM.APPLICANTDIV ";
        $query .= "                                                 AND MAIN.EXAM_TYPE    = SUBM.EXAM_TYPE ";
        $query .= "     LEFT JOIN V_NAME_MST L009 ON L009.YEAR    = MAIN.ENTEXAMYEAR ";
        $query .= "                              AND L009.NAMECD2 = SUBM.SUBCLASSCD ";
        $query .= "                              AND L009.NAMECD1 = 'L009' ";
        $query .= " WHERE ";
        $query .= "         MAIN.ENTEXAMYEAR  = '".$model->year."' ";
        $query .= "     AND MAIN.APPLICANTDIV = '".$model->applicantdiv."' ";
        if ($examType !== "" && $examType != "ALL") {
            $query .= "     AND MAIN.EXAM_TYPE = '".$examType."' ";
        } else if ($examType == "") {
            $query .= " ORDER BY ";
            $query .= "     MAIN.EXAM_TYPE, ";
            $query .= "     SUBM.SUBCLASSCD ";
        }

        return $query;
    }

    //１レコード取得
    function getRow($year, $applicantdiv, $examType) {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_EXAMTYPE_MST ";
        $query .= " WHERE ";
        $query .= "         ENTEXAMYEAR  = '".$year."' ";
        $query .= "     AND APPLICANTDIV = '".$applicantdiv."' ";
        $query .= "     AND EXAM_TYPE    = '".$examType."' ";

        return $query;
    }

    //科目取得
    function getRow2($year, $applicantdiv, $examType) {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_EXAMTYPE_SUBCLASS_MST ";
        $query .= " WHERE ";
        $query .= "         ENTEXAMYEAR  = '".$year."' ";
        $query .= "     AND APPLICANTDIV = '".$applicantdiv."' ";
        $query .= "     AND EXAM_TYPE    = '".$examType."' ";

        return $query;
    }

    /************/
    /* 更新処理 */
    /************/
    //INSERT
    function &getInsertQuery($model, $fields) {
        $db = Query::dbCheckOut();

        $data["ENTEXAMYEAR"][TEXT]          = $fields["ENTEXAMYEAR"];
        $data["APPLICANTDIV"][TEXT]         = $fields["APPLICANTDIV"];
        $data["EXAM_TYPE"][TEXT]            = $fields["EXAM_TYPE"];
        $data["EXAMTYPE_NAME"][TEXT]        = $fields["EXAMTYPE_NAME"];
        $data["EXAMTYPE_NAME_ABBV"][TEXT]   = $fields["EXAMTYPE_NAME_ABBV"];
        $data["REGISTERCD"][TEXT]           = STAFFCD;
        $data["UPDATED"][FUNC]              = "sysdate()";

        $query = Query::insertSQL($data, "ENTEXAM_EXAMTYPE_MST");
        $db->query($query);

        // ENTEXAM_EXAMTYPE_SUBCLASS_MST
        knjl502jQuery::getDelInsEntexamExamtypeSubclassMst($db, $fields, $model);

        Query::dbCheckIn($db);
        return;
    }

    //UPDATE
    function &getUpdateQuery($model, $fields) {
        $db = Query::dbCheckOut();

        $data["EXAMTYPE_NAME"][TEXT]        = $fields["EXAMTYPE_NAME"];
        $data["EXAMTYPE_NAME_ABBV"][TEXT]   = $fields["EXAMTYPE_NAME_ABBV"];
        $data["REGISTERCD"][TEXT]           = STAFFCD;
        $data["UPDATED"][FUNC]              = "sysdate()";

        $where  = " WHERE ENTEXAMYEAR       = '".$fields["ENTEXAMYEAR"]."'";
        $where .= "   AND APPLICANTDIV      = '".$fields["APPLICANTDIV"]."'";
        $where .= "   AND EXAM_TYPE         = '".$fields["EXAM_TYPE"]."' ";

        $query = Query::updateSQL($data, "ENTEXAM_EXAMTYPE_MST", $where);
        $db->query($query);

        // ENTEXAM_EXAMTYPE_SUBCLASS_MST
        knjl502jQuery::getDelInsEntexamExamtypeSubclassMst($db, $fields, $model);

        Query::dbCheckIn($db);
        return;
    }

    //DELETE
    function &getDeleteQuery($fields) {
        $db = Query::dbCheckOut();

        $query  = " DELETE FROM ENTEXAM_EXAMTYPE_MST ";
        $query .= "       WHERE ENTEXAMYEAR     = '".$fields["ENTEXAMYEAR"]."'";
        $query .= "         AND APPLICANTDIV    = '".$fields["APPLICANTDIV"]."'";
        $query .= "         AND EXAM_TYPE       = '".$fields["EXAM_TYPE"]."'";
        $db->query($query);

        $query  = " DELETE FROM ENTEXAM_EXAMTYPE_SUBCLASS_MST ";
        $query .= "       WHERE ENTEXAMYEAR     = '".$fields["ENTEXAMYEAR"]."'";
        $query .= "         AND APPLICANTDIV    = '".$fields["APPLICANTDIV"]."'";
        $query .= "         AND EXAM_TYPE       = '".$fields["EXAM_TYPE"]."'";
        $db->query($query);

        Query::dbCheckIn($db);

        return $result;
    }

    //ENTEXAM_EXAMTYPE_SUBCLASS_MST
    function &getDelInsEntexamExamtypeSubclassMst($db, $fields, $model) {
        //削除
        $query  = " DELETE FROM ENTEXAM_EXAMTYPE_SUBCLASS_MST ";
        $query .= "       WHERE ENTEXAMYEAR     = '".$fields["ENTEXAMYEAR"]."'";
        $query .= "         AND APPLICANTDIV    = '".$fields["APPLICANTDIV"]."'";
        $query .= "         AND EXAM_TYPE       = '".$fields["EXAM_TYPE"]."'";
        $db->query($query);

        //登録
        foreach ($model->subClassArr as $key => $name1) {
            //選択されたサブクラスのみ登録
            if ($fields["SUBCLASS_SELECT_".$key] == "") continue;

            $data["ENTEXAMYEAR"][TEXT]          = $fields["ENTEXAMYEAR"];
            $data["APPLICANTDIV"][TEXT]         = $fields["APPLICANTDIV"];
            $data["EXAM_TYPE"][TEXT]            = $fields["EXAM_TYPE"];
            $data["SUBCLASSCD"][TEXT]           = $fields["SUBCLASSCD_".$key];
            $data["JUDGE_SUMMARY"][TEXT]        = $fields["JUDGE_SUMMARY_".$key];
            $data["REGISTERCD"][TEXT]           = STAFFCD;
            $data["UPDATED"][FUNC]              = "sysdate()";

            $query = Query::insertSQL($data, "ENTEXAM_EXAMTYPE_SUBCLASS_MST");
            $db->query($query);
        }
        return;
    }
}
?>
