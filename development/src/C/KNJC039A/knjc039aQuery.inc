<?php

require_once('for_php7.php');

class knjc039aQuery extends Query {

    //校種取得
    function getSchoolKind($model) {
        $query  = " SELECT ";
        $query .= "     NAME1 AS VALUE, ";
        $query .= "     ABBV1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR    = '".CTRL_YEAR."' AND ";
        $query .= "     NAMECD1 = 'A023' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= " AND NAME1 IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
            }
        } else if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND NAME1 = '".SCHOOLKIND."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE DESC ";

        return $query;
    }

    //前年度からのコピー
    function &getCopyQuery($model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $query = "";
        $query .= " INSERT INTO ATTEND_REASON_COLLECTION_MST( ";
        $query .= "     YEAR, ";
        $query .= "     SCHOOL_KIND, ";
        $query .= "     COLLECTION_CD, ";
        $query .= "     COLLECTION_NAME, ";
        $query .= "     FROM_DATE, ";
        $query .= "     TO_DATE, ";
        $query .= "     SEMESTER, ";
        $query .= "     REGISTERCD, ";
        $query .= "     UPDATED ";
        $query .= " ) ";
        $query .= " ( SELECT ";
        $query .= "      '".CTRL_YEAR."', ";
        $query .= "      SCHOOL_KIND, ";
        $query .= "      COLLECTION_CD, ";
        $query .= "      COLLECTION_NAME, ";
        $query .= "      FROM_DATE + 1 YEAR, ";
        $query .= "      TO_DATE + 1 YEAR, ";
        $query .= "      CASE ";
        $query .= "         WHEN SEMESTER IN ( ";
        $query .= "             SELECT ";
        $query .= "                 SEMESTER ";
        $query .= "             FROM ";
        $query .= "                 SEMESTER_MST ";
        $query .= "             WHERE ";
        $query .= "                 YEAR = '".CTRL_YEAR."' ";
        $query .= "         ) ";
        $query .= "         THEN SEMESTER ";
        $query .= "         ELSE  NULL";
        $query .= "      END ,";
        $query .= "      '".STAFFCD."', ";
        $query .= "      SYSDATE() ";
        $query .= "   FROM ";
        $query .= "       ATTEND_REASON_COLLECTION_MST A0 ";
        $query .= "   WHERE YEAR = '".(CTRL_YEAR-1)."' ";
        $query .= " ) ";

        $db->query($query);
        $db->commit();
        Query::dbCheckIn($db);
        return true;
   }

    //存在チェック
    function getExists($model)
    {
        $db = Query::dbCheckOut();

        $query .= " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     ATTEND_REASON_COLLECTION_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR              = '".CTRL_YEAR."' AND ";
        $query .= "     SCHOOL_KIND       = '".$model->field["SCHOOL_KIND"]."' AND ";
        $query .= "     COLLECTION_CD     = '".$model->field["COLLECTION_CD"]."' ";

        $ret_val = $db->getOne($query);
        Query::dbCheckIn($db);

        return $ret_val;
    }

    //前年度にデータがあるかチェック
    function getExistsPreYear($model)
    {
        $db = Query::dbCheckOut();

        $query .= " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     ATTEND_REASON_COLLECTION_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR    = '".(CTRL_YEAR-1)."' ";

        $ret_val = $db->getOne($query);
        Query::dbCheckIn($db);

        return $ret_val;
    }

    //今年にデータがあるかチェック
    function getExistsThisYear($model)
    {
        $db = Query::dbCheckOut();

        $query = "";
        $query .= " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     ATTEND_REASON_COLLECTION_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR    = '".CTRL_YEAR."' ";

        $ret_val = $db->getOne($query);
        Query::dbCheckIn($db);

        return $ret_val;
    }

    //出欠備考が存在しているかチェック
    function getExistsAttendRemark($model)
    {
        $db = Query::dbCheckOut();

        $query = "";
        $query .= " SELECT ";
        $query .= "     COUNT(SCHREGNO) ";
        $query .= " FROM ";
        $query .= "     ATTEND_REASON_COLLECTION_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR               = '".CTRL_YEAR."' AND";
        $query .= "     SCHOOL_KIND        = '".$model->field["SCHOOL_KIND"]."' AND ";
        $query .= "     COLLECTION_CD      = '".$model->field["COLLECTION_CD"]."' AND ";
        $query .= "     ATTEND_REMARK IS NOT NULL ";

        $ret_val = $db->getOne($query);
        Query::dbCheckIn($db);

        return $ret_val;
    }

    function getList($model){
        $query  = " SELECT ";
        $query .= " *";
        $query .= " FROM ";
        $query .= "     ATTEND_REASON_COLLECTION_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR   = '".CTRL_YEAR."' ";
        
        return $query;
    }

    //学期を取得
    function getSemester() {
        $query  = " SELECT ";
        $query .= " SEMESTER AS VALUE, ";
        $query .= " SEMESTERNAME AS LABEL ";
        $query .= " FROM SEMESTER_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR  = '".CTRL_YEAR."' ";
        
        return $query;
    }
    
    //１レコード取得
    function getRow($model, $db) {
        $query  = " SELECT * FROM ATTEND_REASON_COLLECTION_MST ";
        $query .= "  WHERE YEAR                 = '".$model->year."' ";
        $query .= "   AND  SCHOOL_KIND          = '".$model->school_kind."' ";
        $query .= "   AND  COLLECTION_CD        = '".$model->collection_cd."' ";
        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        return $row;
    }

    // INSERT&UPDATE 一度DELETEしてINSERTする。
    function &getInsertUpdateQuery($model)
    {
        $db = Query::dbCheckOut();

        $db->autoCommit(false);
        $query  = " DELETE FROM ATTEND_REASON_COLLECTION_MST ";
        $query .= " WHERE YEAR              = '".CTRL_YEAR."' ";
        $query .= " AND SCHOOL_KIND         = '".$model->field["SCHOOL_KIND"]."' ";
        $query .= " AND COLLECTION_CD       = '".$model->field["COLLECTION_CD"]."' ";
        $db->query($query);

        $query = "";

        $data["YEAR"][TEXT]                                 = CTRL_YEAR;
        $data["SCHOOL_KIND"][TEXT]                          = $model->field["SCHOOL_KIND"];
        $data["COLLECTION_CD"][TEXT]                        = $model->field["COLLECTION_CD"];
        $data["COLLECTION_NAME"][TEXT]                      = $model->field["COLLECTION_NAME"];
        $data["FROM_DATE"][TEXT]                            = str_replace("/", "-", $model->field["FROM_DATE"]);
        $data["TO_DATE"][TEXT]                              = str_replace("/", "-", $model->field["TO_DATE"]);
        $data["SEMESTER"][TEXT]                             = $model->field["SEMESTER"];
        $data["REGISTERCD"][TEXT]                           = STAFFCD;
        $data["UPDATED"][FUNC]                              = "sysdate()";
        $query = Query::insertSQL($data, "ATTEND_REASON_COLLECTION_MST");
        $db->query($query);

        $db->commit(); // トランザクションをコミットする。
        Query::dbCheckIn($db);
        return;
    }

    //DELETE
    function &getDeleteQuery($model) {
        $db = Query::dbCheckOut();

        $query  = "DELETE FROM ATTEND_REASON_COLLECTION_MST ";
        $query .= "     WHERE YEAR             = '".CTRL_YEAR."' ";
        $query .= "       AND SCHOOL_KIND      = '".$this->field["SCHOOL_KIND"]."' ";
        $query .= "       AND COLLECTION_CD    = '".$this->field["COLLECTION_CD"]."' ";

        $db->query($query);
        Query::dbCheckIn($db);

        return $result;
    }

    //DELETE(ATTEND_REASON_COLLECTION_DAT)
    function &getDeleteAttendRemarkQuery($model) {
        $db = Query::dbCheckOut();

        $query  = "DELETE FROM ATTEND_REASON_COLLECTION_DAT ";
        $query .= "     WHERE YEAR             = '".CTRL_YEAR."' ";
        $query .= "       AND SCHOOL_KIND      = '".$this->field["SCHOOL_KIND"]."' ";
        $query .= "       AND COLLECTION_CD    = '".$this->field["COLLECTION_CD"]."' ";

        $db->query($query);
        Query::dbCheckIn($db);

        return $result;
    }

}
?>
