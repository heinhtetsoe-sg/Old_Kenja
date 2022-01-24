<?php

require_once('for_php7.php');

class knjh020_2aQuery extends Query {
    function getGrdCls() {
        $query  = "SELECT DISTINCT ";
        $query .= "       GRADE || ',' || HR_CLASS AS GC, GRADE, HR_CLASS, ";
        $query .= "       HR_NAME AS GC_J ";
        $query .= " FROM SCHREG_REGD_HDAT ";
        $query .= " WHERE YEAR = '".$this->year."' ";
        $query .= " ORDER BY GC";

        return $query;
    }

    //NAME_MSTから情報を取得
    function getNameMst_data($cd) {
        $query  = "SELECT * FROM V_NAME_MST ";
        $query .= "WHERE NAMECD1 = '".$cd."' AND ";
        $query .= "      YEAR = '".CTRL_YEAR."'";

        return $query;
    }

    //本人のデータ取得
    function getRow_himself($model) {
        $db = Query::dbCheckOut();

        $query  = " SELECT NAME_SHOW,BIRTHDAY FROM  SCHREG_BASE_MST ";
        $query .= " WHERE SCHREGNO ='";
        $query .= $model->schregno;
        $query .= "' ";
        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);

        return $row;
    }

    //連番の最大値を取得
    function getRela_Maxno($model) {
        $db = Query::dbCheckOut();

        $query  = " SELECT MAX(INT(RELANO)) AS MAX FROM SCHREG_RELA_DAT ";
        $query .= " WHERE SCHREGNO ='";
        $query .= $model->schregno;
        $query .= "' ";
        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);

        return $row["MAX"];
    }

    //親族のデータ一覧を取得
    function getRow_relative_list($model)
    {
        $query  = " SELECT * FROM  SCHREG_RELA_DAT ";
        $query .= " WHERE SCHREGNO ='".$model->schregno."' ORDER BY RELANO";

        return $query;
    }

    //卒業した兄弟姉妹取得
    function getGrdBrother($model) {
        $query  = " SELECT ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     T1.GRD_DIV, ";
        $query .= "     L1.NAME1 ";
        $query .= " FROM ";
        $query .= "     SCHREG_BASE_MST T1 ";
        $query .= "     LEFT JOIN NAME_MST L1 ON L1.NAMECD1 = 'A003' AND L1.NAMECD2 = T1.GRD_DIV ";
        $query .= " WHERE ";
        $query .= "     GRD_DIV IS NOT NULL AND ";
        $query .= "     GRD_DIV != '4' AND ";
        $query .= "     SCHREGNO IN (SELECT ";
        $query .= "                     RELA_SCHREGNO ";
        $query .= "                 FROM ";
        $query .= "                     SCHREG_RELA_DAT ";
        $query .= "                 WHERE ";
        $query .= "                     SCHREGNO = '".$model->schregno."' ";
        $query .= "                 ) ";

        return $query;
    }

    //学年一覧取得
    function getSchregRegdGdat($model) {
        $query  = " SELECT ";
        $query .= "     GRADE AS VALUE, ";
        $query .= "     GRADE_NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_GDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //親族のデータを取得
    function getRow_relative($model) {
        $db = Query::dbCheckOut();

        $query  = " SELECT * FROM  SCHREG_RELA_DAT ";
        $query .= " WHERE SCHREGNO ='".$model->schregno."'";
        $query .= "   AND RELANO ='".$model->relano."' ";
        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);

        return $row;
    }

    //親族情報INSERT
    function &getInsertQuery($model,$relano) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $data["SCHREGNO"][TEXT]      = $model->schregno;
        $data["RELANO"][TEXT]        = $relano;
        $data["RELANAME"][TEXT]      = $model->field["RELANAME"];
        $data["RELAKANA"][TEXT]      = $model->field["RELAKANA"];
        $data["RELASEX"][TEXT]       = $model->field["RELASEX"];
        $data["RELABIRTHDAY"][TEXT]  = str_replace("/","-",$model->field["RELABIRTHDAY"]);
        $data["RELATIONSHIP"][TEXT]  = $model->field["RELATIONSHIP"];
        $data["RELA_SCHREGNO"][TEXT] = $model->field["RELA_SCHREGNO"];
        $data["REGD_GRD_FLG"][TEXT]  = $model->field["REGD_GRD_FLG"];
        $data["RELA_GRADE"][TEXT]    = $model->field["RELA_GRADE"];
        $data["OCCUPATION"][TEXT]    = $model->field["OCCUPATION"];
        $data["REGIDENTIALCD"][TEXT] = $model->field["REGIDENTIALCD"];
        $data["REMARK"][TEXT]        = $model->field["REMARK"];
        $data["REGISTERCD"][TEXT]    = STAFFCD;
        $data["UPDATED"][FUNC]       = "sysdate()";

        $query = Query::insertSQL($data, "SCHREG_RELA_DAT");
        $db->query($query);

        $db->commit();
        Query::dbCheckIn($db);
        $model->relano = $relano;

        return true;
    }

    //親族情報UPDATE
    function &getUpdateQuery($model) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $data["RELANAME"][TEXT]      = $model->field["RELANAME"];
        $data["RELAKANA"][TEXT]      = $model->field["RELAKANA"];
        $data["RELASEX"][TEXT]       = $model->field["RELASEX"];
        $data["RELABIRTHDAY"][TEXT]  = str_replace("/","-",$model->field["RELABIRTHDAY"]);
        $data["RELATIONSHIP"][TEXT]  = $model->field["RELATIONSHIP"];
        $data["RELA_SCHREGNO"][TEXT] = $model->field["RELA_SCHREGNO"];
        $data["REGD_GRD_FLG"][TEXT]  = $model->field["REGD_GRD_FLG"];
        $data["RELA_GRADE"][TEXT]    = $model->field["RELA_GRADE"];
        $data["OCCUPATION"][TEXT]    = $model->field["OCCUPATION"];
        $data["REGIDENTIALCD"][TEXT] = $model->field["REGIDENTIALCD"];
        $data["REMARK"][TEXT]        = $model->field["REMARK"];
        $data["REGISTERCD"][TEXT]    = STAFFCD;
        $data["UPDATED"][FUNC]       = "sysdate()";

        $where  = " WHERE SCHREGNO = '".$model->schregno."' ";
        $where .= " AND RELANO = '".$model->relano."' ";

        $query = Query::updateSQL($data, "SCHREG_RELA_DAT", $where);
        $db->query($query);

        $db->commit(); //コミットする。
        Query::dbCheckIn($db);

        return true;
    }

    //delete
    function &getDeleteQuery($model) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $query  = " DELETE FROM SCHREG_RELA_DAT ";
        $query .= " WHERE SCHREGNO = '".$model->schregno."' ";
        $query .= " AND RELANO = '".$model->relano."' ";

        $db->query($query);
        $db->commit(); //コミットする。
        Query::dbCheckIn($db);

        return true;
    }
    //親族データを挿入
    function add_Schreg_Rela_Dat($model) {

        $db = Query::dbCheckOut();

        $query = "SELECT RELANO FROM SCHREG_RELA_DAT WHERE SCHREGNO = '".$model->schregno. "' ORDER BY RELANO DESC";
        $max = $db->getRow($query, DB_FETCHMODE_ASSOC);

        $db->autoCommit(false);
        $query  = "SELECT * FROM SCHREG_RELA_DAT WHERE SCHREGNO = '".$model->stucd."' ";
        $result = $db->query($query);

        $ret = false;
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $max["RELANO"]++;
            $data["SCHREGNO"][TEXT]         = $model->schregno;
            $data["RELANO"][TEXT]           = $max["RELANO"];
            $data["RELANAME"][TEXT]         = $row["RELANAME"];
            $data["RELAKANA"][TEXT]         = $row["RELAKANA"];
            $data["RELASEX"][TEXT]          = $row["RELASEX"];
            $data["RELABIRTHDAY"][TEXT]     = $row["RELABIRTHDAY"];
            $data["RELATIONSHIP"][TEXT]     = $row["RELATIONSHIP"];
            $data["RELA_SCHREGNO"][TEXT]    = $model->stucd;
            $data["REGD_GRD_FLG"][TEXT]     = $row["REGD_GRD_FLG"];
            $data["RELA_GRADE"][TEXT]       = $row["RELA_GRADE"];
            $data["OCCUPATION"][TEXT]       = $row["OCCUPATION"];
            $data["REGIDENTIALCD"][TEXT]    = $row["REGIDENTIALCD"];
            $data["REMARK"][TEXT]           = $row["REMARK"];
            $data["REGISTERCD"][TEXT]       = $row["REGISTERCD"];
            $data["UPDATED"][FUNC]          = "sysdate()";

            $query = Query::insertSQL($data, "SCHREG_RELA_DAT");
            $db->query($query);
            $ret = true;
        }
        $db->commit();
        Query::dbCheckIn($db);

        return $ret;
    }

    //本人続柄・長子のデータを取得
    function getRowBaseD($model) {
        $db = Query::dbCheckOut();

        $query  = " SELECT ";
        $query .= "     BASE_REMARK1 AS BASE_RELATIONSHIP, ";
        $query .= "     BASE_REMARK2 AS BASE_FIRST_CHILD, ";
        $query .= "     BASE_REMARK3 AS BASE_PREF ";
        $query .= " FROM ";
        $query .= "     SCHREG_BASE_DETAIL_MST ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO = '".$model->schregno."' ";
        $query .= " AND BASE_SEQ = '007' ";

        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);

        return $row;
    }

    //都道府県取得
    function getPrefMst() {
        $query  = " SELECT ";
        $query .= "     PREF_CD AS VALUE, ";
        $query .= "     PREF_NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     PREF_MST ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //本人続柄・長子・都道府県INSERT_UPDATE
    function &getInsUpdBaseD($model) {
        $db = Query::dbCheckOut();

        $data["BASE_REMARK1"][TEXT]  = $model->field["BASE_RELATIONSHIP"];
        $data["BASE_REMARK2"][TEXT]  = $model->field["BASE_FIRST_CHILD"];
        $data["BASE_REMARK3"][TEXT]  = $model->field["BASE_PREF"];
        $data["REGISTERCD"][TEXT]    = STAFFCD;
        $data["UPDATED"][FUNC]       = "sysdate()";

        $query  = " SELECT COUNT(*) FROM SCHREG_BASE_DETAIL_MST ";
        $query .= " WHERE SCHREGNO   = '".$model->schregno."' ";
        $query .= "   AND BASE_SEQ   = '007' ";
        $cnt = $db->getOne($query);

        if ($cnt == 0) {
            $data["SCHREGNO"][TEXT]      = $model->schregno;
            $data["BASE_SEQ"][TEXT]      = "007";
            $query = Query::insertSQL($data, "SCHREG_BASE_DETAIL_MST");
        } else {
            $where  = " WHERE SCHREGNO   = '".$model->schregno."' ";
            $where .= "   AND BASE_SEQ   = '007' ";
            $query = Query::updateSQL($data, "SCHREG_BASE_DETAIL_MST", $where);
        }
        $db->query($query);

        Query::dbCheckIn($db);

        return true;
    }

    //本人続柄・長子DELETE
    function &getDelBaseD($model) {
        $db = Query::dbCheckOut();

        $query  = " SELECT COUNT(*) FROM SCHREG_RELA_DAT ";
        $query .= " WHERE SCHREGNO = '".$model->schregno."' ";
        $cnt = $db->getOne($query);

        if ($cnt == 0) {
            $query  = " DELETE FROM SCHREG_BASE_DETAIL_MST ";
            $query .= " WHERE SCHREGNO   = '".$model->schregno."' ";
            $query .= "   AND BASE_SEQ   = '007' ";
            $db->query($query);
        }

        Query::dbCheckIn($db);

        return true;
    }
}
?>
