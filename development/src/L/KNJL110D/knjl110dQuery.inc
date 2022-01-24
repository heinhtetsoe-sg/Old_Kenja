<?php

require_once('for_php7.php');

class knjl110dQuery extends Query {
    //名称マスタよりデータを取得
    function get_name_cd($year, $namecd1, $sitei = "") {
        $query  = " SELECT ";
        $query .= "     NAMECD2 || ':' || NAME1 AS LABEL, ";
        $query .= "     NAMECD2 AS VALUE, ";
        $query .= "     ABBV1, ";
        $query .= "     NAMESPARE2 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "         YEAR    = '".$year."' ";
        $query .= "     AND NAMECD1 = '".$namecd1."' ";
        if ($sitei) {
            $query .= "     AND NAMECD2 = '".$sitei."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //名称マスタより和暦の元号を取得
    function get_calendarno($year, $namecd2 = "") {
        $query  = " SELECT ";
        $query .= "     NAMECD2, ";
        $query .= "     NAME1, ";
        $query .= "     NAMESPARE1, ";
        $query .= "     NAMESPARE2, ";
        $query .= "     NAMESPARE3 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "         YEAR    = '".$year."' ";
        $query .= "     AND NAMECD1 = 'L007' ";
        if (strlen($namecd2)) {
            $query .= "     AND NAMECD2 = '".$namecd2."' ";
        }

        return $query;
    }

    //メインデータ取得
    function get_edit_data($model) {
        $db = Query::dbCheckOut();

        $query  = "";
        $query .= " SELECT ";
        $query .= "     VSIT.* ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_VISIT_DAT VSIT ";
        $query .= "     LEFT JOIN NAME_MST L007 ON L007.NAMECD1 = 'L007' ";
        $query .= "                            AND L007.NAMECD2 = VSIT.FS_ERACD ";
        $query .= " WHERE ";
        $query .= "         VSIT.ENTEXAMYEAR  = '".$model->year."' ";

        if ($model->cmd == "back" || $model->cmd == "back1") {
            $query.= "     AND VSIT.VISIT_NO < '".$model->visitNo."'";
            $query.= " ORDER BY ";
            $query.= "     VSIT.VISIT_NO DESC ";
        } elseif ($model->cmd == "next" || $model->cmd == "next1") {
            $query.= "     AND VSIT.VISIT_NO > '".$model->visitNo."'";
            $query.= " ORDER BY ";
            $query.= "     VSIT.VISIT_NO ASC ";
        } else {
            $query.= "     AND VSIT.VISIT_NO = '".$model->visitNo."'";
        }

        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);

        return $row;
    }

    //出身学校マスタよりデータを取得
    function getFinschoolName($cd) {
        $query  = " SELECT  T1.FINSCHOOL_NAME ";
        $query .= "   FROM  FINSCHOOL_MST T1 ";
        $query .= "  WHERE  T1.FINSCHOOLCD = '".$cd."' ";
        return $query;
    }

/***** 追加更新処理 *****/
    //存在チェック
    function getExistdataCnt($model) {
        $query  = " SELECT ";
        $query .= "     COUNT(VISIT_NO) ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_VISIT_DAT ";
        $query .= " WHERE ";
        $query .= "         ENTEXAMYEAR = '".$model->year."' ";
        $query .= "     AND VISIT_NO    = '".$model->visitNo."' ";
        return $query;
    }

    //追加・更新
    function &getUpdateQuery($model) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //ENTEXAM_VISIT_DAT
        $data = array();
        $data["ENTEXAMYEAR"][TEXT]      = $model->year;
        $data["VISIT_NO"][TEXT]         = sprintf("%03d", $model->visitNo);
        $data["VISIT_DATE"][DATE]       = str_replace("/", "-", $model->field["VISIT_DATE"]);
        $data["NAME"][TEXT]             = $model->field["NAME"];
        $data["NAME_KANA"][TEXT]        = $model->field["NAME_KANA"];
        $data["ERACD"][TEXT]            = $model->field["ERACD"];
        $data["BIRTH_Y"][TEXT]          = sprintf("%02d", $model->field["BIRTH_Y"]);
        $data["BIRTH_M"][TEXT]          = sprintf("%02d", $model->field["BIRTH_M"]);
        $data["BIRTH_D"][TEXT]          = sprintf("%02d", $model->field["BIRTH_D"]);
        $data["SEX"][TEXT]              = $model->field["SEX"];
        $data["ZIPCD"][TEXT]            = $model->field["ZIPCD"];
        $data["ADDRESS1"][TEXT]         = $model->field["ADDRESS1"];
        $data["ADDRESS2"][TEXT]         = $model->field["ADDRESS2"];
        $data["TELNO"][TEXT]            = $model->field["TELNO"];
        $data["FS_CD"][TEXT]            = (strlen($model->field["FS_CD"])) ? sprintf("%07s",$model->field["FS_CD"]) : "";
        $data["FS_ERACD"][TEXT]         = $model->field["FS_ERACD"];
        $data["FS_Y"][TEXT]             = sprintf("%02d", $model->field["FS_Y"]);
        $data["FS_M"][TEXT]             = sprintf("%02d", $model->field["FS_M"]);
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][FUNC]          = "sysdate()";
        if ($model->cmd == "add") {
            $query = Query::insertSQL($data, "ENTEXAM_VISIT_DAT");
            $db->query($query);
        } else {
            $where  = " WHERE ENTEXAMYEAR  = '".$model->year."' ";
            $where .= "   AND VISIT_NO     = '".$model->visitNo."' ";
            $query = Query::updateSQL($data, "ENTEXAM_VISIT_DAT", $where);
            $db->query($query);
        }

        $db->commit();
        Query::dbCheckIn($db);
        return;
    }

    //--- DELETE
    function &getDeleteQuery($model) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //ENTEXAM_APPLICANTBASE_DAT
        $query  = " DELETE FROM ENTEXAM_VISIT_DAT ";
        $query .= " WHERE ENTEXAMYEAR  = '".$model->year."' ";
        $query .= "   AND VISIT_NO     = '".$model->visitNo."' ";
        $db->query($query);

        $db->commit();
        Query::dbCheckIn($db);
        return;
    }
}
?>