<?php

require_once('for_php7.php');

class knjz403jQuery extends Query {
    //一覧取得（左画面）
    function getList($model) {
        $query  = " SELECT ";
        $query .= "     T1.SCHOOL_KIND, ";
        $query .= "     L1.ABBV1 AS SCHOOL_KIND_NAME, ";
        $query .= "     T1.STUDYREC_CODE, ";
        $query .= "     T1.STUDYREC_CODENAME ";
        $query .= " FROM ";
        $query .= "     BEHAVIOR_SUB_MST T1";
        $query .= "     LEFT JOIN NAME_MST L1 ON L1.NAMECD1 = 'A023' ";
        $query .= "                          AND L1.NAME1   = T1.SCHOOL_KIND ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= " WHERE ";
                $query .= "     T1.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
            }
        } elseif ($model->Properties["useSchool_KindField"] == "1") {
            $query .= " WHERE ";
            $query .= "     T1.SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     T1.SCHOOL_KIND, ";
        $query .= "     T1.STUDYREC_CODE ";

        return $query;
    }

    //１レコード取得 (右画面)
    function getRow($school_kind, $studyrec_code) {
        $db = Query::dbCheckOut();

        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     BEHAVIOR_SUB_MST ";
        $query .= " WHERE ";
        $query .= "     SCHOOL_KIND     = '".$school_kind."' AND ";
        $query .= "     STUDYREC_CODE   = '".$studyrec_code."' ";

        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);

        return $row;
    }

    //校種取得
    function getSchoolKind($model, $schoolkind="") {
        $query .= " SELECT ";
        $query .= "     ABBV1 AS LABEL, ";
        $query .= "     NAME1 AS VALUE ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = 'A023' ";
        if ($schoolkind) {
            $query .= " AND NAME1 = '".$schoolkind."' ";
        } else {
            if ($model->Properties["use_prg_schoolkind"] == "1") {
                if ($model->selectSchoolKind) {
                    $query .= " AND NAME1 IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
                }
            } elseif ($model->Properties["useSchool_KindField"] == "1") {
                $query .= " AND NAME1 = '".SCHOOLKIND."' ";
            }
            $query .= " ORDER BY ";
            $query .= "     VALUE ";
        }

        return $query;
    }

    //更新 -- BEHAVIOR_SUB_MST
    function &getUpdateQuery($cmd, $fields) {
        $db = Query::dbCheckOut();

        $data = array();
        if ($cmd == "add") {
            $data["SCHOOL_KIND"][TEXT]          = $fields["SCHOOL_KIND"];
            $data["STUDYREC_CODE"][TEXT]        = $fields["STUDYREC_CODE"];
        }
        $data["STUDYREC_CODENAME"][TEXT]    = $fields["STUDYREC_CODENAME"];
        $data["REGISTERCD"][TEXT]           = STAFFCD;
        $data["UPDATED"][NUMBER]            = "sysdate()";

        if ($cmd == "update") {
            $where  = " WHERE ";
            $where .= "     SCHOOL_KIND     = '" .$fields["SCHOOL_KIND"]."' AND ";
            $where .= "     STUDYREC_CODE   = '" .$fields["STUDYREC_CODE"]."' ";

            $query = Query::updateSQL($data, "BEHAVIOR_SUB_MST", $where);
        } else {
            $query = Query::insertSQL($data, "BEHAVIOR_SUB_MST");
        }
        $db->query($query);

        Query::dbCheckIn($db);
        return;
    }

    //削除 -- BEHAVIOR_SUB_MST
    function &getDeleteQuery($fields) {
        $db = Query::dbCheckOut();

        $query  = " DELETE FROM ";
        $query .= "     BEHAVIOR_SUB_MST ";
        $query .= " WHERE ";
        $query .= "     SCHOOL_KIND     = '".$fields["SCHOOL_KIND"]."' AND ";
        $query .= "     STUDYREC_CODE   = '".$fields["STUDYREC_CODE"]."' ";

        $db->query($query);
        Query::dbCheckIn($db);
        return $result;
    }
}
?>
