<?php

require_once('for_php7.php');

class knjz220eQuery extends Query {

    //名称マスタより校種取得
    function getVNameMstA023($model) {
        $query  = " SELECT ";
        $query .= "     NAME1 AS VALUE, ";
        $query .= "     ABBV1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$model->year."' ";
        $query .= "     AND NAMECD1 = 'A023' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= " AND NAME1 IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
            }
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND NAME1 = '".SCHOOLKIND."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     NAMECD2 ";

        return $query;
    }

    //全体のデータの取得
    function selectQuery($model) {
        $query  = " SELECT ";
        $query .= "     MKM.GRADE ";
        $query .= "     , MKM.MIKOMI_RATE ";
        $query .= "     , GDAT.GRADE_NAME1 ";
        $query .= " FROM ";
        $query .= "     MIKOMI_RATE_MST MKM ";
        $query .= "     INNER JOIN SCHREG_REGD_GDAT GDAT ";
        $query .= "         ON GDAT.YEAR = MKM.YEAR ";
        $query .= "         AND GDAT.GRADE = MKM.GRADE ";
        $query .= "         AND GDAT.SCHOOL_KIND = MKM.SCHOOL_KIND ";
        $query .= " WHERE ";
        $query .= "     MKM.YEAR = '".$model->year."' ";
        $query .= "     AND MKM.SCHOOL_KIND = '".$model->schoolKind."' ";
        $query .= " ORDER BY ";
        $query .= "     GRADE ";

        return $query;
    }

    //選択したデータの取得
    function getRow($model, $grade) {
        $query  = " SELECT ";
        $query .= "     MIKOMI_RATE ";
        $query .= " FROM ";
        $query .= "     MIKOMI_RATE_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$model->year."' ";
        $query .= "     AND SCHOOL_KIND = '".$model->schoolKind."' ";
        $query .= "     AND GRADE = '".$grade."' ";

       return $query;
    }

    //学年取得
    function getGrade ($model) {
        $query  = " SELECT ";
        $query .= "     GRADE AS VALUE ";
        $query .= "     , GRADE_NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_GDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$model->year."' ";
        $query .= "     AND SCHOOL_KIND = '".$model->schoolKind."'; ";

        return $query;
    }

    //INSERT
    function &getInsertQuery($model) {
        $db = Query::dbCheckOut();

        $data["YEAR"][TEXT]             = $model->year;
        $data["SCHOOL_KIND"][TEXT]      = $model->schoolKind;
        $data["GRADE"][TEXT]            = $model->field["GRADE"];
        $data["MIKOMI_RATE"][NUMBER]    = $model->field["MIKOMI_RATE"];
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][FUNC]          = "sysdate()";

        $query = Query::insertSQL($data, "MIKOMI_RATE_MST");
        $db->query($query);

        Query::dbCheckIn($db);
        return;
    }

    //UPDATE
    function &getUpdateQuery($model) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $query  = " DELETE FROM ";
        $query .= "     MIKOMI_RATE_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$model->year."' ";
        $query .= "     AND SCHOOL_KIND = '".$model->schoolKind."' ";
        $query .= "     AND GRADE = '".$model->field["GRADE"]."' ";

        $db->query($query);

        $data["YEAR"][TEXT]             = $model->year;
        $data["SCHOOL_KIND"][TEXT]      = $model->schoolKind;
        $data["GRADE"][TEXT]            = $model->field["GRADE"];
        $data["MIKOMI_RATE"][NUMBER]    = $model->field["MIKOMI_RATE"];
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][FUNC]          = "sysdate()";

        $query = Query::insertSQL($data, "MIKOMI_RATE_MST");
        $db->query($query);

        $db->commit();
        Query::dbCheckIn($db);
        return;
    }

    //DELETE
    function &getDeleteQuery($model) {
        $db = Query::dbCheckOut();

        $query  = " DELETE FROM ";
        $query .= "     MIKOMI_RATE_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$model->year."' ";
        $query .= "     AND SCHOOL_KIND = '".$model->schoolKind."' ";
        $query .= "     AND GRADE = '".$model->field["GRADE"]."' ";

        $db->query($query);

        Query::dbCheckIn($db);
        return ;
    }

    /* コピー処理関連 */
    //今年度のデータが存在するかチェック(MIKOMI_RATE_MST)
    function CheckData($year) {
        $db = Query::dbCheckOut();

        $query  = " SELECT ";
        $query .= "     'X' ";
        $query .= " FROM ";
        $query .= "     MIKOMI_RATE_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$year."' ";

        $row = $db->getOne($query);

        Query::dbCheckIn($db);

        return $row;
    }

    //コピー
    function &getCopyQuery() {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $query  = " INSERT INTO MIKOMI_RATE_MST ";
        $query .= " SELECT ";
        $query .= "     '".CTRL_YEAR."' ";
        $query .= "     , SCHOOL_KIND ";
        $query .= "     , GRADE ";
        $query .= "     , MIKOMI_RATE ";
        $query .= "     , '".STAFFCD."' ";
        $query .= "     , sysdate() ";
        $query .= " FROM ";
        $query .= "     MIKOMI_RATE_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".(CTRL_YEAR - 1)."' ";
        $db->query($query);

        $db->commit();
        Query::dbCheckIn($db);
        return;
    }
}
?>
