<?php
class knjh410_action_documentQuery extends Query {
    //校種取得
    function getSchoolKind($model) {
        $query  = " SELECT ";
        $query .= "     T2.SCHOOL_KIND ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1 ";
        $query .= "     INNER JOIN SCHREG_REGD_GDAT T2 ON T1.YEAR = T2.YEAR AND T1.GRADE = T2.GRADE ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR     = '".$model->year."' AND ";
        $query .= "     T1.SEMESTER = '".$model->semester."' AND ";
        $query .= "     T1.SCHREGNO = '".$model->schregno."' ";

        return $query;
    }

    //行動の記録取得
    function getActionDucSub($model)
    {
        $query  = "SELECT ";
        $query .= "    T1.STAFFCD || '　' || L1.STAFFNAME AS STAFFNAME, ";
        $query .= "    T1.ACTIONDATE, ";
        $query .= "    T1.ACTIONTIME, ";
        $query .= "    T1.DIVIDECD, ";
        $query .= "    T1.STAFFCD, ";
        $query .= "    T1.TITLE, ";
        $query .= "    T1.TEXT, ";
        $query .= "    T1.SCHREGNO, ";
        $query .= "    T1.ACTIONDATE, ";
        $query .= "    T1.SEQ, ";
        $query .= "    T1.PRIVATE ";
        $query .= "FROM ";
        $query .= "    ACTION_DOCUMENT_DAT T1 ";
        $query .= "    LEFT JOIN V_STAFF_MST L1 ON L1.YEAR = '".$model->year."' ";
        $query .= "         AND L1.STAFFCD = T1.STAFFCD ";
        $query .= "WHERE ";
        $query .= "    SCHREGNO = '".$model->schregno."' ";
        $query .= "    AND ACTIONDATE = '".str_replace("/", "-", $model->actiondate)."' ";
        $query .= "    AND SEQ = ".$model->seq." ";
        $query .= "ORDER BY ";
        $query .= "    T1.ACTIONDATE DESC, ";
        $query .= "    T1.ACTIONTIME DESC, ";
        $query .= "    T1.SEQ ";

        return $query;
    }

    //生徒情報取得
    function getName($schregno)
    {
        $query  = "SELECT ";
        $query .= "    NAME ";
        $query .= "FROM ";
        $query .= "    SCHREG_BASE_MST ";
        $query .= "WHERE ";
        $query .= "    SCHREGNO = '".$schregno."' ";

        return $query;
    }

    //名称マスタ取得
    function getNameMst($model, $namecd1, $namecd2 = "")
    {
        $query  = "SELECT ";
        $query .= "    NAME1 AS LABEL, ";
        $query .= "    NAMECD2 AS VALUE ";
        $query .= "FROM ";
        $query .= "    V_NAME_MST ";
        $query .= "WHERE ";
        $query .= "    YEAR = '".$model->year."' ";
        $query .= "    AND NAMECD1 = '".$namecd1."' ";
        if ($namecd2) {
            $query .= "    AND NAMECD2 = '".$namecd2."' ";
        }
        $query .= "ORDER BY ";
        $query .= "    NAMECD2 ";

        return $query;
    }

    /**
     * 回数取得
     */
    function getMaxSeq($schregno)
    {
        $query  = "SELECT ";
        $query .= "    SCHREGNO, ";
        $query .= "    MAX(SEQ) + 1 AS SEQ ";
        $query .= "FROM ";
        $query .= "    ACTION_DOCUMENT_DAT ";
        $query .= "WHERE ";
        $query .= "    SCHREGNO = '".$schregno."' ";
        $query .= "GROUP BY ";
        $query .= "    SCHREGNO ";

        return $query;
    }

    /**
     * 更新
     */
    function &updateQuery($model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $data["SCHREGNO"][TEXT]               = $model->schregno;
        $data["ACTIONDATE"][TEXT]             = str_replace("/", "-", $model->actiondate);
        $data["SEQ"][NUMBER]                  = $model->seq;
        $data["ACTIONTIME"][TEXT]             = $model->actiontime;
        $data["DIVIDECD"][TEXT]               = $model->dividecd;
        $data["TITLE"][TEXT]                  = $model->title;
        $data["TEXT"][TEXT]                   = $model->text;
        $data["PRIVATE"][TEXT]                = $model->private;
        $data["REGISTERCD"][TEXT]             = STAFFCD;
        $data["UPDATED"][FUNC]                = "sysdate()";

        $where  = " WHERE SCHREGNO = '".$model->schregno."'";
        $where .= "       AND ACTIONDATE = '".str_replace("/", "-", $model->actiondateUpd)."'";
        $where .= "       AND SEQ = ".$model->seq." ";

        $query = Query::updateSQL($data, "ACTION_DOCUMENT_DAT", $where);
        $db->query($query);
        
        $db->commit();
        Query::dbCheckIn($db);
        return true;
    }

    /**
     * 新規
     */
    function &insertQuery($model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $data["SCHREGNO"][TEXT]               = $model->schregno;
        $data["ACTIONDATE"][TEXT]             = str_replace("/", "-", $model->actiondate);
        $data["SEQ"][NUMBER]                  = $model->seq;
        $data["ACTIONTIME"][TEXT]             = $model->actiontime;
        $data["STAFFCD"][TEXT]                = STAFFCD;
        $data["DIVIDECD"][TEXT]               = $model->dividecd;
        $data["TITLE"][TEXT]                  = $model->title;
        $data["TEXT"][TEXT]                   = $model->text;
        $data["PRIVATE"][TEXT]                = $model->private;
        $data["REGISTERCD"][TEXT]             = STAFFCD;
        $data["UPDATED"][FUNC]                = "sysdate()";

        $query = Query::insertSQL($data, "ACTION_DOCUMENT_DAT");
        $db->query($query);
        
        $db->commit();
        Query::dbCheckIn($db);
        return true;
    }

    /**
     * 削除
     */
    function &getDeleteQuery($delvalue)
    {

        $query  = "DELETE FROM ";
        $query .= "    ACTION_DOCUMENT_DAT ";
        $query .= "WHERE ";
        $query .= "    SCHREGNO = '".$delvalue[0]."' ";
        $query .= "    AND ACTIONDATE = '".str_replace("/", "-", $delvalue[1])."' ";
        $query .= "    AND SEQ = ".$delvalue[2]." ";

        return $query;
    }
}
?>
