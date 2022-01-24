<?php

require_once('for_php7.php');

class knjz350cQuery extends Query
{

    //学校種別の取得
    public function getNameMstA023($model)
    {
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
                $query .= "     AND NAME1 IN ('".implode(explode(':', $model->selectSchoolKind), "','")."') ";
            }
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";
        return $query;
    }

    //対象プログラム一覧取得
    public function getProgramIdList($model)
    {
        $setNameCd = "C043";
        if ($model->Properties["use_prg_schoolkind"] == "1" || $model->Properties["useSchool_KindField"] == "1") {
            $setNameCd = "C".$model->school_kind."43";
        }

        $query  = " SELECT ";
        $query .= "     T1.PROGRAMID AS VALUE, ";
        $query .= "     T1.PROGRAMID || '：' || T2.MENUNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     (SELECT ";
        if ($model->Properties["useSchool_KindField"] == "1") {
            $query .= "         SCHOOLCD, ";
            $query .= "         SCHOOL_KIND, ";
        }
        $query .= "         PROGRAMID, ";
        $query .= "         MIN(MENUID) AS MENUID ";
        $query .= "     FROM ";
        $query .= "         MENU_MST ";
        $query .= "     WHERE ";
        $query .= "         PROGRAMID IN ('".implode("','", array_keys($model->prgid))."') AND ";
        $query .= "         MENUNAME IS NOT NULL ";
        $query .= "     GROUP BY ";
        if ($model->Properties["useSchool_KindField"] == "1") {
            $query .= "         SCHOOLCD, ";
            $query .= "         SCHOOL_KIND, ";
        }
        $query .= "         PROGRAMID ";
        $query .= "     ) T1, ";
        $query .= "     MENU_MST T2 ";
        $query .= " WHERE ";
        $query .= "     T1.MENUID       = T2.MENUID AND ";
        $query .= "     T1.PROGRAMID IN (SELECT NAME1 FROM NAME_MST WHERE NAMECD1 = '".$setNameCd."') ";
        if ($model->Properties["useSchool_KindField"] == "1") {
            $query .= "     AND T1.SCHOOLCD     = T2.SCHOOLCD ";
            $query .= "     AND T1.SCHOOL_KIND  = T2.SCHOOL_KIND ";
        }
        if ($model->Properties["use_prg_schoolkind"] == "1" || $model->Properties["useSchool_KindField"] == "1") {
            $query .= " AND T1.SCHOOLCD     = '".sprintf("%012d", SCHOOLCD)."' ";
            $query .= " AND T1.SCHOOL_KIND  = '".SCHOOLKIND."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //権限グループ取得
    public function getUserGroup($model)
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     T1.GROUPCD AS VALUE, ";
        $query .= "     T1.GROUPCD || '：' || T1.GROUPNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     USERGROUP_MST T1, ";
        $query .= "     USERGROUP_DAT T2 ";
        $query .= " WHERE ";
        $query .= "     T2.YEAR         = '".CTRL_YEAR."' AND ";
        $query .= "     T1.GROUPCD      = T2.GROUPCD ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            $query .= " AND T1.SCHOOLCD     = T2.SCHOOLCD ";
            $query .= " AND T1.SCHOOLCD     = '".sprintf("%012d", SCHOOLCD)."' ";
            $query .= " AND T1.SCHOOL_KIND  = T2.SCHOOL_KIND ";
            $query .= " AND T1.SCHOOL_KIND  = '".SCHOOLKIND."' ";
        } elseif ($model->Properties["useSchool_KindField"] == "1") {
            $query .= " AND T1.SCHOOLCD     = T2.SCHOOLCD ";
            $query .= " AND T1.SCHOOLCD     = '".sprintf("%012d", SCHOOLCD)."' ";
            $query .= " AND T1.SCHOOL_KIND  = T2.SCHOOL_KIND ";
            $query .= " AND T1.SCHOOL_KIND  = '".SCHOOLKIND."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //課程学科取得
    public function getCourseMajor($model)
    {
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
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
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
    public function getAttendNameList($model)
    {
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
    public function getAdminControlAttendItemnameDat($model, $attend_div)
    {
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

    //出欠表示項目取得
    public function getAdminFieldList($model, $control_div, $attend_div, $input_flg = "")
    {
        $query  = " SELECT ";
        $query .= "     ATTEND_ITEM, ";
        $query .= "     INPUT_FLG, ";
        $query .= "     SHOWORDER ";
        $query .= " FROM ";
        $query .= "     ADMIN_CONTROL_ATTEND_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR        = '".CTRL_YEAR."' AND ";
        $query .= "     SCHOOL_KIND = '".$model->school_kind."' AND ";
        $query .= "     CONTROL_DIV = '".$control_div."' AND ";
        $query .= "     ATTEND_DIV  = '".$attend_div."' AND ";
        $query .= "     PROGRAMID   = '".$model->field["PRG_ID"]."' AND ";
        $query .= "     GROUPCD     = '".$model->field["GROUPCD"]."' AND ";
        $query .= "     GRADE       = '".$model->field["GRADE"]."' AND ";
        $query .= "     COURSECD || '-' || MAJORCD = '".$model->field["COURSE_MAJOR"]."' ";
        if ($input_flg) {
            $query .= " AND INPUT_FLG   = '1' ";
        }
        $query .= " ORDER BY ";
        $query .= "     INT(SHOWORDER) ";

        return $query;
    }

    //UPDATE
    public function &getUpdateQuery(&$model, $selectdata, $flg)
    {
        //配列に格納
        $array = explode(",", $selectdata);

        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $attend_div = $model->prgid[$model->field["PRG_ID"]];

        if ($flg == "1") {
            //入力可項目取得
            $query = knjz350cQuery::getAdminFieldList($model, "1", $attend_div, "1");
            $input_array = $db->getCol($query);

            //削除
            $query  = " DELETE FROM ";
            $query .= "     ADMIN_CONTROL_ATTEND_DAT ";
            $query .= " WHERE ";
            $query .= "     YEAR        = '".CTRL_YEAR."' AND ";
            $query .= "     SCHOOL_KIND = '".$model->school_kind."' AND ";
            $query .= "     CONTROL_DIV = '1' AND ";
            $query .= "     ATTEND_DIV  = '".$attend_div."' AND ";
            $query .= "     PROGRAMID   = '".$model->field["PRG_ID"]."' AND ";
            $query .= "     GROUPCD     = '".$model->field["GROUPCD"]."' AND ";
            $query .= "     GRADE       = '".$model->field["GRADE"]."' AND ";
            $query .= "     COURSECD || '-' || MAJORCD = '".$model->field["COURSE_MAJOR"]."' ";

            $db->query($query);

            //追加
            if ($array[0] != "") {
                for ($i = 0; $i < get_count($array); $i++) {
                    list($no, $attend_item) = explode('-', $array[$i]);
                    list($coursecd, $majorcd) = explode('-', $model->field["COURSE_MAJOR"]);

                    $data = array();
                    $data["YEAR"][TEXT]         = CTRL_YEAR;
                    $data["SCHOOL_KIND"][TEXT]  = $model->school_kind;
                    $data["CONTROL_DIV"][TEXT]  = "1";
                    $data["ATTEND_DIV"][TEXT]   = $attend_div;
                    $data["PROGRAMID"][TEXT]    = $model->field["PRG_ID"];
                    $data["GROUPCD"][TEXT]      = $model->field["GROUPCD"];
                    $data["GRADE"][TEXT]        = $model->field["GRADE"];
                    $data["COURSECD"][TEXT]     = $coursecd;
                    $data["MAJORCD"][TEXT]      = $majorcd;
                    $data["ATTEND_ITEM"][TEXT]  = $attend_item;
                    $data["SHOWORDER"][TEXT]    = $i + 1;
                    $data["INPUT_FLG"][TEXT]    = (in_array($attend_item, $input_array)) ? "1" : "";
                    $data["REGISTERCD"][TEXT]   = STAFFCD;
                    $data["UPDATED"][FUNC]      = "sysdate()";

                    $db->query(Query::insertSQL($data, "ADMIN_CONTROL_ATTEND_DAT"));
                }
            }
        } elseif ($flg == "2") {
            //入力可フラグをカット
            $data = array();
            $data["INPUT_FLG"][TEXT]    = "";
            $data["REGISTERCD"][TEXT]   = STAFFCD;
            $data["UPDATED"][FUNC]      = "sysdate()";

            $where  = " WHERE ";
            $where .= "     YEAR            = '".CTRL_YEAR."' AND ";
            $where .= "     SCHOOL_KIND     = '".$model->school_kind."' AND ";
            $where .= "     CONTROL_DIV     = '1' AND ";
            $where .= "     ATTEND_DIV      = '".$attend_div."' AND ";
            $where .= "     PROGRAMID       = '".$model->field["PRG_ID"]."' AND ";
            $where .= "     GROUPCD         = '".$model->field["GROUPCD"]."' AND ";
            $where .= "     GRADE           = '".$model->field["GRADE"]."' AND ";
            $where .= "     COURSECD || '-' || MAJORCD = '".$model->field["COURSE_MAJOR"]."' AND ";
            $where .= "     INPUT_FLG       = '1' ";

            $db->query(Query::updateSQL($data, "ADMIN_CONTROL_ATTEND_DAT", $where));

            //更新
            if ($array[0] != "") {
                for ($i = 0; $i < get_count($array); $i++) {
                    list($no, $attend_item) = explode('-', $array[$i]);

                    $data = array();
                    $data["INPUT_FLG"][TEXT]    = "1";
                    $data["REGISTERCD"][TEXT]   = STAFFCD;
                    $data["UPDATED"][FUNC]      = "sysdate()";

                    $where  = " WHERE ";
                    $where .= "     YEAR            = '".CTRL_YEAR."' AND ";
                    $where .= "     SCHOOL_KIND     = '".$model->school_kind."' AND ";
                    $where .= "     CONTROL_DIV     = '1' AND ";
                    $where .= "     ATTEND_DIV      = '".$attend_div."' AND ";
                    $where .= "     PROGRAMID       = '".$model->field["PRG_ID"]."' AND ";
                    $where .= "     GROUPCD         = '".$model->field["GROUPCD"]."' AND ";
                    $where .= "     GRADE           = '".$model->field["GRADE"]."' AND ";
                    $where .= "     COURSECD || '-' || MAJORCD = '".$model->field["COURSE_MAJOR"]."' AND ";
                    $where .= "     ATTEND_ITEM     = '".$attend_item."' ";

                    $db->query(Query::updateSQL($data, "ADMIN_CONTROL_ATTEND_DAT", $where));
                }
            }
        } elseif ($flg == "3") {
            //削除
            $query  = " DELETE FROM ";
            $query .= "     ADMIN_CONTROL_ATTEND_DAT ";
            $query .= " WHERE ";
            $query .= "     YEAR        = '".CTRL_YEAR."' AND ";
            $query .= "     SCHOOL_KIND = '".$model->school_kind."' AND ";
            $query .= "     CONTROL_DIV = '2' AND ";
            $query .= "     ATTEND_DIV  = '".$attend_div."' AND ";
            $query .= "     PROGRAMID   = '".$model->field["PRG_ID"]."' AND ";
            $query .= "     GROUPCD     = '".$model->field["GROUPCD"]."' AND ";
            $query .= "     GRADE       = '".$model->field["GRADE"]."' AND ";
            $query .= "     COURSECD || '-' || MAJORCD = '".$model->field["COURSE_MAJOR"]."' ";

            $db->query($query);

            //追加
            if ($array[0] != "") {
                for ($i = 0; $i < get_count($array); $i++) {
                    list($no, $attend_item) = explode('-', $array[$i]);
                    list($coursecd, $majorcd) = explode('-', $model->field["COURSE_MAJOR"]);

                    $data = array();
                    $data["YEAR"][TEXT]         = CTRL_YEAR;
                    $data["SCHOOL_KIND"][TEXT]  = $model->school_kind;
                    $data["CONTROL_DIV"][TEXT]  = "2";
                    $data["ATTEND_DIV"][TEXT]   = $attend_div;
                    $data["PROGRAMID"][TEXT]    = $model->field["PRG_ID"];
                    $data["GROUPCD"][TEXT]      = $model->field["GROUPCD"];
                    $data["GRADE"][TEXT]        = $model->field["GRADE"];
                    $data["COURSECD"][TEXT]     = $coursecd;
                    $data["MAJORCD"][TEXT]      = $majorcd;
                    $data["ATTEND_ITEM"][TEXT]  = $attend_item;
                    $data["SHOWORDER"][TEXT]    = $i + 1;
                    $data["REGISTERCD"][TEXT]   = STAFFCD;
                    $data["UPDATED"][FUNC]      = "sysdate()";

                    $db->query(Query::insertSQL($data, "ADMIN_CONTROL_ATTEND_DAT"));
                }
            }
        }

        $db->autoCommit(true);
        Query::dbCheckIn($db);
        return;
    }

    //前年度からのコピー
    public function &getCopyQuery($model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $this_year  = CTRL_YEAR;
        $pre_year   = CTRL_YEAR - 1;

        $query  = " INSERT INTO ADMIN_CONTROL_ATTEND_DAT ";
        $query .= " SELECT ";
        $query .= "     '".$this_year."', ";
        $query .= "     T1.SCHOOL_KIND, ";
        $query .= "     T1.CONTROL_DIV, ";
        $query .= "     T1.ATTEND_DIV, ";
        $query .= "     T1.PROGRAMID, ";
        $query .= "     T1.GROUPCD, ";
        $query .= "     T1.GRADE, ";
        $query .= "     T1.COURSECD, ";
        $query .= "     T1.MAJORCD, ";
        $query .= "     T1.ATTEND_ITEM, ";
        $query .= "     RTRIM(CHAR(INT(VALUE(L1.SHOWORDER,'0')) + ROW_NUMBER() OVER (PARTITION BY T1.CONTROL_DIV,T1.ATTEND_DIV,T1.PROGRAMID,T1.GROUPCD ORDER BY T1.CONTROL_DIV,T1.ATTEND_DIV,T1.PROGRAMID,T1.GROUPCD,INT(T1.SHOWORDER)))), ";
        $query .= "     T1.INPUT_FLG, ";
        $query .= "     '".STAFFCD."', ";
        $query .= "     SYSDATE() ";
        $query .= " FROM ";
        $query .= "     ADMIN_CONTROL_ATTEND_DAT T1 ";
        $query .= "     LEFT JOIN (SELECT ";
        $query .= "                     CONTROL_DIV, ";
        $query .= "                     ATTEND_DIV, ";
        $query .= "                     PROGRAMID, ";
        $query .= "                     GROUPCD, ";
        $query .= "                     GRADE, ";
        $query .= "                     COURSECD, ";
        $query .= "                     MAJORCD, ";
        $query .= "                     MAX(SHOWORDER) AS SHOWORDER ";
        $query .= "                 FROM ";
        $query .= "                     ADMIN_CONTROL_ATTEND_DAT ";
        $query .= "                 WHERE ";
        $query .= "                     YEAR        = '".$this_year."' AND ";
        $query .= "                     SCHOOL_KIND = '".$model->school_kind."' ";
        $query .= "                 GROUP BY ";
        $query .= "                     CONTROL_DIV, ";
        $query .= "                     ATTEND_DIV, ";
        $query .= "                     PROGRAMID, ";
        $query .= "                     GROUPCD, ";
        $query .= "                     GRADE, ";
        $query .= "                     COURSECD, ";
        $query .= "                     MAJORCD ";
        $query .= "             ) L1 ON T1.CONTROL_DIV  = L1.CONTROL_DIV ";
        $query .= "                 AND T1.ATTEND_DIV   = L1.ATTEND_DIV ";
        $query .= "                 AND T1.PROGRAMID    = L1.PROGRAMID ";
        $query .= "                 AND T1.GROUPCD      = L1.GROUPCD ";
        $query .= "                 AND T1.GRADE        = L1.GRADE ";
        $query .= "                 AND T1.COURSECD     = L1.COURSECD ";
        $query .= "                 AND T1.MAJORCD      = L1.MAJORCD ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR        = '".$pre_year."' AND ";
        $query .= "     T1.SCHOOL_KIND = '".$model->school_kind."' AND ";
        $query .= "     NOT EXISTS (SELECT ";
        $query .= "                     'X' ";
        $query .= "                 FROM ";
        $query .= "                     ADMIN_CONTROL_ATTEND_DAT T2 ";
        $query .= "                 WHERE ";
        $query .= "                     T2.YEAR         = '".$this_year."' AND ";
        $query .= "                     T1.SCHOOL_KIND  = T2.SCHOOL_KIND AND ";
        $query .= "                     T1.CONTROL_DIV  = T2.CONTROL_DIV AND ";
        $query .= "                     T1.ATTEND_DIV   = T2.ATTEND_DIV AND ";
        $query .= "                     T1.PROGRAMID    = T2.PROGRAMID AND ";
        $query .= "                     T1.GROUPCD      = T2.GROUPCD AND ";
        $query .= "                     T1.GRADE        = T2.GRADE AND ";
        $query .= "                     T1.COURSECD     = T2.COURSECD AND ";
        $query .= "                     T1.MAJORCD      = T2.MAJORCD AND ";
        $query .= "                     T1.ATTEND_ITEM  = T2.ATTEND_ITEM ";
        $query .= "                 ) AND ";
        $query .= "     T1.ATTEND_ITEM IN ('".implode(explode(',', $model->itemList), "','")."') ";
        $query .= " ORDER BY ";
        $query .= "     T1.CONTROL_DIV, ";
        $query .= "     T1.ATTEND_DIV, ";
        $query .= "     T1.PROGRAMID, ";
        $query .= "     T1.GROUPCD, ";
        $query .= "     T1.GRADE, ";
        $query .= "     T1.COURSECD, ";
        $query .= "     T1.MAJORCD, ";
        $query .= "     INT(T1.SHOWORDER) ";

        $db->query($query);

        $db->commit();
        Query::dbCheckIn($db);
        return;
    }
}
