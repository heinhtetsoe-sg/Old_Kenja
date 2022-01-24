<?php

require_once('for_php7.php');

class knjz022Query extends Query {
    //学校名取得
    function getSchoolName2($model) {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     V_SCHOOL_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$model->year."' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            $query .= "     AND SCHOOLCD    = '".sprintf("%012d", SCHOOLCD)."' ";
            $query .= "     AND SCHOOL_KIND = '".$model->sendSchkind."' ";
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= "     AND SCHOOLCD    = '".sprintf("%012d", SCHOOLCD)."' ";
            $query .= "     AND SCHOOL_KIND = '".SCHOOLKIND."' ";
        }

        return $query;
    }

    //学校側DB　教育委員会、学校サーバ情報
    function getDataBaseInfo() {
        $query  = " SELECT ";
        $query .= "     ABBV1 "; //1:教育委員会サーバ　2:教育委員会とリンクする学校　null:教育委員会とリンクしない学校
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = 'Z010' AND ";
        $query .= "     NAMECD2 = '00' ";

        return $query;
    }

    //学校側DB　教育委員会統計用学校番号取得
    function getSchoolDetailDat002($model) {
        $query  = " SELECT ";
        $query .= "     SCHOOL_REMARK3 "; //EDBOARD_SCHOOLCD
        $query .= " FROM ";
        $query .= "     SCHOOL_DETAIL_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR        = '".$model->year."' AND ";
        $query .= "     SCHOOL_SEQ  = '002' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            $query .= " AND SCHOOLCD    = '".sprintf("%012d", SCHOOLCD)."' ";
            $query .= " AND SCHOOL_KIND = '".$model->sendSchkind."' ";
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND SCHOOLCD    = '".sprintf("%012d", SCHOOLCD)."' ";
            $query .= " AND SCHOOL_KIND = '".SCHOOLKIND."' ";
        }

        return $query;
    }

    //教育委員側DB　教育委員会設定学校マスタを取得
    function getEdboardSchoolMst($edboard_schoolcd) {
        $query .= " SELECT ";
        $query .= "     EDBOARD_SCHOOLCD, ";
        $query .= "     EDBOARD_SCHOOLNAME, ";  //FINSCHOOL_NAME
        $query .= "     GROUPWARE_SCHOOLCD ";   //FINSCHOOLCD
        $query .= " FROM ";
        $query .= "     EDBOARD_SCHOOL_MST ";
        $query .= " WHERE ";
        $query .= "     EDBOARD_SCHOOLCD = '".$edboard_schoolcd."' ";
        
        return $query;
    }

    //データ取得
    function getSchoolDetailDat($model) {
        $query  = " SELECT ";
        $query .= "     SCHOOL_REMARK1 AS FINSCHOOLCD, ";
        $query .= "     SCHOOL_REMARK2 AS SCHOOL_TYPE, ";
        $query .= "     SCHOOL_REMARK3 AS COURSE_CD, ";
        $query .= "     SCHOOL_REMARK4 AS ATTENDANCE_FLAG, ";
        $query .= "     SCHOOL_REMARK5 AS KENJA_URL ";
        $query .= " FROM ";
        $query .= "     SCHOOL_DETAIL_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR        = '".$model->year."' AND ";
        $query .= "     SCHOOL_SEQ  = '008' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            $query .= " AND SCHOOLCD    = '".sprintf("%012d", SCHOOLCD)."' ";
            $query .= " AND SCHOOL_KIND = '".$model->sendSchkind."' ";
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND SCHOOLCD    = '".sprintf("%012d", SCHOOLCD)."' ";
            $query .= " AND SCHOOL_KIND = '".SCHOOLKIND."' ";
        }

        return $query;
    }

    //出身学校マスタ取得
    function getFinschoolMst($finschoolcd) {
        $query  = " SELECT ";
        $query .= "     FINSCHOOLCD, ";
        $query .= "     FINSCHOOL_NAME, ";
        $query .= "     FINSCHOOL_KANA ";
        $query .= " FROM ";
        $query .= "     FINSCHOOL_MST ";
        $query .= " WHERE ";
        $query .= "     FINSCHOOLCD = '".$finschoolcd."' ";

        return $query;
    }

    //更新処理
    function updateSchoolDetailDat($model) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //削除
        $query  = " DELETE FROM ";
        $query .= "     SCHOOL_DETAIL_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR        = '".$model->year."' AND ";
        $query .= "     SCHOOL_SEQ  = '008' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            $query .= " AND SCHOOLCD    = '".sprintf("%012d", SCHOOLCD)."' ";
            $query .= " AND SCHOOL_KIND = '".$model->sendSchkind."' ";
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND SCHOOLCD    = '".sprintf("%012d", SCHOOLCD)."' ";
            $query .= " AND SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $db->query($query);

        //追加
        $data = array();
        $data["YEAR"][TEXT]            = $model->year;
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            $data["SCHOOLCD"][TEXT]        = sprintf("%012d", SCHOOLCD);
            $data["SCHOOL_KIND"][TEXT]     = $model->sendSchkind;
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $data["SCHOOLCD"][TEXT]        = sprintf("%012d", SCHOOLCD);
            $data["SCHOOL_KIND"][TEXT]     = SCHOOLKIND;
        }
        $data["SCHOOL_SEQ"][TEXT]      = "008";
        $data["SCHOOL_REMARK1"][TEXT]  = $model->field["FINSCHOOLCD"];
        $data["SCHOOL_REMARK2"][TEXT]  = $model->field["SCHOOL_TYPE"];
        $data["SCHOOL_REMARK3"][TEXT]  = $model->field["COURSE_CD"];
        $data["SCHOOL_REMARK4"][TEXT]  = $model->field["ATTENDANCE_FLAG"];
        $data["SCHOOL_REMARK5"][TEXT]  = $model->field["KENJA_URL"];
        $data["REGISTERCD"][TEXT]      = STAFFCD;
        $data["UPDATED"][FUNC]         = "sysdate()";

        $query = Query::insertSQL($data, "SCHOOL_DETAIL_DAT");
        $db->query($query);

        $db->commit();
        Query::dbCheckIn($db);
        return true;
    }
}
?>
