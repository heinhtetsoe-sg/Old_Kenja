<?php

require_once('for_php7.php');

class knjz352cquery extends Query {

    //学校種別の取得
    function getNameMstA023($model) {
        $query  = " SELECT ";
        $query .= "     ABBV1 AS LABEL, ";
        $query .= "     NAME1 AS VALUE ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND NAMECD1 = 'A023' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= "     AND NAME1 IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
            }
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";
        return $query;
    }

    //課程学科取得
    function getCourseMajor($model) {
        $query  = " SELECT DISTINCT ";
        $query .= "     T1.COURSECD || T1.MAJORCD || ':' || S1.COURSENAME || S1.MAJORNAME AS LABEL, ";
        $query .= "     T1.COURSECD || '-' || T1.MAJORCD AS VALUE ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1 ";
        $query .= "     INNER JOIN V_COURSE_MAJOR_MST S1 ";
        $query .= "          ON T1.YEAR         = S1.YEAR ";
        $query .= "         AND T1.COURSECD     = S1.COURSECD ";
        $query .= "         AND T1.MAJORCD      = S1.MAJORCD ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            $query .= "     INNER JOIN SCHREG_REGD_GDAT S2 ";
            $query .= "          ON T1.YEAR         = S2.YEAR ";
            $query .= "         AND T1.GRADE        = S2.GRADE ";
            $query .= "         AND S2.SCHOOL_KIND  = '".$model->school_kind."' ";
        } else if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= "     INNER JOIN SCHREG_REGD_GDAT S2 ";
            $query .= "          ON T1.YEAR         = S2.YEAR ";
            $query .= "         AND T1.GRADE        = S2.GRADE ";
            $query .= "         AND S2.SCHOOL_KIND  = '".SCHOOLKIND."' ";
        }
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //出欠項目名一覧取得
    function getAttendNameList($model) {
        $query  = " SELECT ";
        $query .= "     NAMECD1, ";
        $query .= "     NAMECD2, ";
        $query .= "     NAME1 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR    = '".CTRL_YEAR."' AND ";
        $query .= "     NAMECD1 = 'A004' ";
        $query .= " UNION  ";
        $query .= " SELECT ";
        $query .= "     NAMECD1, ";
        $query .= "     NAMECD2, ";
        $query .= "     NAME1 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR    = '".CTRL_YEAR."' AND ";
        $query .= "     NAMECD1 = 'C001' ";
        $query .= " UNION  ";
        $query .= " SELECT ";
        $query .= "     NAMECD1, ";
        $query .= "     NAMECD2, ";
        $query .= "     NAME1 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR    = '".CTRL_YEAR."' AND ";
        $query .= "     NAMECD1 = 'C002' ";

        return $query;
    }

    //出欠項目名一覧取得
    function getAdminControlAttendItemnameDat($model, $attend_div) {
        $query  = " SELECT ";
        $query .= "     ATTEND_ITEM, ";
        $query .= "     ATTEND_ITEMNAME ";
        $query .= " FROM ";
        $query .= "     ADMIN_CONTROL_ATTEND_ITEMNAME_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR        = '".CTRL_YEAR."' AND ";
        $query .= "     SCHOOL_KIND = '".$model->school_kind."' AND ";
        $query .= "     ATTEND_DIV  = '".$attend_div."' AND ";
        $query .= "     GRADE       = '".$model->field["GRADE"]."' AND ";
        $query .= "     COURSECD || '-' || MAJORCD = '".$model->field["COURSE_MAJOR"]."' ";

        return $query;
    }

    //更新処理
    function updateQuery($model) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //削除
        $query  = " DELETE FROM ";
        $query .= "     ADMIN_CONTROL_ATTEND_ITEMNAME_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR        = '".CTRL_YEAR."' AND ";
        $query .= "     SCHOOL_KIND = '".$model->school_kind."' AND ";
        $query .= "     ATTEND_DIV  = '".$model->field["ATTEND_DIV"]."' AND ";
        $query .= "     GRADE       = '".$model->field["GRADE"]."' AND ";
        $query .= "     COURSECD || '-' || MAJORCD = '".$model->field["COURSE_MAJOR"]."' ";

        $db->query($query);

        list ($coursecd, $majorcd) = explode('-', $model->field["COURSE_MAJOR"]);
        foreach ($model->item_array[$model->field["ATTEND_DIV"]] as $key => $val) {
            //入力された項目のみ追加
            if (strlen($model->fields["ATTEND_ITEMNAME"][$val["value"]])) {
                $data = array();
                $data["YEAR"][TEXT]             = CTRL_YEAR;
                $data["SCHOOL_KIND"][TEXT]      = $model->school_kind;
                $data["ATTEND_DIV"][TEXT]       = $model->field["ATTEND_DIV"];
                $data["GRADE"][TEXT]            = $model->field["GRADE"];
                $data["COURSECD"][TEXT]         = $coursecd;
                $data["MAJORCD"][TEXT]          = $majorcd;
                $data["ATTEND_ITEM"][TEXT]      = $val["value"];
                $data["ATTEND_ITEMNAME"][TEXT]  = $model->fields["ATTEND_ITEMNAME"][$val["value"]];
                $data["REGISTERCD"][TEXT]       = STAFFCD;
                $data["UPDATED"][FUNC]          = "sysdate()";

                $db->query(Query::insertSQL($data, "ADMIN_CONTROL_ATTEND_ITEMNAME_DAT"));
            }
        }

        $db->commit();
        Query::dbCheckIn($db);
    }

    //前年度データ取得
    function getCopyData($year, $flg) {
        $query  =  " SELECT ";
        if ($flg == "cnt") {
            $query .=  "    COUNT(*) ";
        } else {
            $query .=  "    * ";
        }
        $query .=  " FROM ";
        $query .=  "    ADMIN_CONTROL_ATTEND_ITEMNAME_DAT ";
        $query .=  " WHERE ";
        $query .=  "    YEAR = '".$year."' ";

        return $query;
    }

    //前年度コピー処理
    function &getCopyQuery($pre_year) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //削除
        $query  =  " DELETE FROM ";
        $query .=  "    ADMIN_CONTROL_ATTEND_ITEMNAME_DAT ";
        $query .=  " WHERE ";
        $query .=  "    YEAR = '".CTRL_YEAR."' ";
        $db->query($query);

        //前年度データ取得
        $query = knjz352cQuery::getCopyData($pre_year, "list");
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            //追加
            $data = array();
            $data["YEAR"][TEXT]             = CTRL_YEAR;
            $data["SCHOOL_KIND"][TEXT]      = $row["SCHOOL_KIND"];
            $data["ATTEND_DIV"][TEXT]       = $row["ATTEND_DIV"];
            $data["GRADE"][TEXT]            = $row["GRADE"];
            $data["COURSECD"][TEXT]         = $row["COURSECD"];
            $data["MAJORCD"][TEXT]          = $row["MAJORCD"];
            $data["ATTEND_ITEM"][TEXT]      = $row["ATTEND_ITEM"];
            $data["ATTEND_ITEMNAME"][TEXT]  = $row["ATTEND_ITEMNAME"];
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][FUNC]          = "sysdate()";

            $query = Query::insertSQL($data, "ADMIN_CONTROL_ATTEND_ITEMNAME_DAT");
            $db->query($query);
        }

        $db->commit();
        Query::dbCheckIn($db);
    }
    
}
?>
