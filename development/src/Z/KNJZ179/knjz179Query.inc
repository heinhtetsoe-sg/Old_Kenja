<?php

require_once('for_php7.php');

class knjz179Query extends Query {
    //参照年度
    function getGdatYear() {
        $query  = " SELECT DISTINCT ";
        $query .= "     YEAR || '年度' AS LABEL, ";
        $query .= "     YEAR AS VALUE ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_GDAT ";
        $query .= " ORDER BY ";
        $query .= "     YEAR ";

        return $query;
    }

    //対象年度
    function getYear() {
        $query  = " SELECT DISTINCT ";
        $query .= "     YEAR || '年度' AS LABEL, ";
        $query .= "     YEAR AS VALUE ";
        $query .= " FROM ";
        $query .= "     SCHOOL_MST ";
        $query .= " ORDER BY ";
        $query .= "     YEAR ";

        return $query;
    }

    //SCHREG_REGD_GDAT から１レコード取得
    function getSchregRegdGdat($term, $grade) {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_GDAT ";
        $query .= " WHERE ";
        $query .= "         YEAR = '{$term}' ";
        $query .= "     AND GRADE = '{$grade}' ";

        return $query;
    }

    //一覧
    function SelectList($term, $model) {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_GDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$term}' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= " AND SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
            }
        }else if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     YEAR, ";
        $query .= "     GRADE ";

        return $query;
    }

    //校種
    function getSchoolKind($model)
    {
        $query  = " SELECT ";
        $query .= "     NAME1 AS VALUE, ";
        $query .= "     NAME1 || ':' || ABBV1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = 'A023' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= " AND NAME1 IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
            }
        }else if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND NAME1 = '".SCHOOLKIND."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     NAMECD2 ";

        return $query;
    }

    //--- INSERT
    function &getInsertQuery($model) {
        $db = Query::dbCheckOut();

        $data["YEAR"][TEXT]           = $model->term;
        $data["GRADE"][TEXT]          = $model->fields["GRADE"];
        $data["SCHOOL_KIND"][TEXT]    = $model->fields["SCHOOL_KIND"];
        $data["GRADE_CD"][TEXT]       = $model->fields["GRADE_CD"];
        $data["GRADE_NAME1"][TEXT]    = $model->fields["GRADE_NAME1"];
        $data["GRADE_NAME2"][TEXT]    = $model->fields["GRADE_NAME2"];
        $data["GRADE_NAME3"][TEXT]    = $model->fields["GRADE_NAME3"];
        $data["REGISTERCD"][TEXT]     = STAFFCD;
        $data["UPDATED"][NUMBER]      = "sysdate()";

        $query = Query::insertSQL($data, "schreg_regd_gdat");
        $db->query($query);
        Query::dbCheckIn($db);
        return;
    }

    //--- UPDATE
    function &getUpdateQuery($model) {
        $db = Query::dbCheckOut();

        $data["YEAR"][TEXT]           = $model->term;
        $data["GRADE"][TEXT]          = $model->fields["GRADE"];
        $data["SCHOOL_KIND"][TEXT]    = $model->fields["SCHOOL_KIND"];
        $data["GRADE_CD"][TEXT]       = $model->fields["GRADE_CD"];
        $data["GRADE_NAME1"][TEXT]    = $model->fields["GRADE_NAME1"];
        $data["GRADE_NAME2"][TEXT]    = $model->fields["GRADE_NAME2"];
        $data["GRADE_NAME3"][TEXT]    = $model->fields["GRADE_NAME3"];
        $data["REGISTERCD"][TEXT]     = STAFFCD;
        $data["UPDATED"][NUMBER]      = "sysdate()";
        $where  = " WHERE   YEAR     = '" .SUBSTR($model->term,0,4)."'";
        $where .= "     AND GRADE    = '".$model->grade."'";

        $query = Query::updateSQL($data, "schreg_regd_gdat", $where);

        $db->query($query);
        Query::dbCheckIn($db);
        return;
    }

    //DELETE
    function &getDeleteQuery($model) {
        $db = Query::dbCheckOut();

        $query  = " DELETE FROM ";
        $query .= "     SCHREG_REGD_GDAT ";
        $query .= " WHERE   YEAR  = '{$model->term}' ";
        $query .= "     AND GRADE = '{$model->grade}' ";

        $db->query($query);
        Query::dbCheckIn($db);
    }

    //「コピーボタン」押し時に、既に登録されているデータがあるか
    function getCount($term, $model) {
        $db = Query::dbCheckOut();

        $query .= " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_GDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$term}' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= " AND SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
            }
        }else if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND SCHOOL_KIND = '".SCHOOLKIND."' ";
        }

        $count = $db->getOne($query);
        Query::dbCheckIn($db);
        return $count;
    }

    // 「コピーボタン」を押したときに参照年度にデータがあるのチェック
    function cntSchregRegdGdatTerm2($term2, $model) {
        $db = Query::dbCheckOut();

        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_GDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$term2}' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= " AND SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
            }
        }else if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND SCHOOL_KIND = '".SCHOOLKIND."' ";
        }

        $count = $db->getOne($query);
        Query::dbCheckIn($db);
        return $count;
    }

    //データを削除
    function Delete_Gdat($term, $model) {
        $query  = " DELETE FROM ";
        $query .= "     SCHREG_REGD_GDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$term}' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= " AND SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
            }
        }else if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND SCHOOL_KIND = '".SCHOOLKIND."' ";
        }

        return $query;
    }

    //学籍在籍ヘッダデータの参照学年のデータを対象学年にインサート
    function Insert_Gdat($term2, $term, $model) {
        $query  = " INSERT INTO SCHREG_REGD_GDAT ";
        $query .= " SELECT ";
        $query .= "      '{$term}', ";
        $query .= "      GRADE, ";
        $query .= "      SCHOOL_KIND, ";
        $query .= "      GRADE_CD, ";
        $query .= "      GRADE_NAME1, ";
        $query .= "      GRADE_NAME2, ";
        $query .= "      GRADE_NAME3, ";
        $query .= "      registercd, ";
        $query .= "      SYSDATE()";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_GDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$term2}' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= " AND SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
            }
        }else if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND SCHOOL_KIND = '".SCHOOLKIND."' ";
        }

        return $query;
    }

    //「コピーボタン」押し時の処理
    function getInsertCopyQuery($term2, $term, $model) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $db->query(knjz179Query::Delete_Gdat($term, $model));
        $db->query(knjz179Query::Insert_Gdat($term2, $term, $model));
        $db->commit();
        Query::dbCheckIn($db);
    }
}
?>
