<?php

require_once('for_php7.php');
class knjlz001Query extends Query
{

    //年度取得
    public function getYear()
    {
        $query  = " WITH ENTEXAMYEAR_T(VALUE, LABEL) AS ( ";
        $query .= "   VALUES('".(CTRL_YEAR + 1)."', '".(CTRL_YEAR + 1)."') ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "   * ";
        $query .= " FROM ";
        $query .= "   ENTEXAMYEAR_T ";
        $query .= " UNION ";
        $query .= "   SELECT DISTINCT ";
        $query .= "     ENTEXAMYEAR AS LABEL, ";
        $query .= "     ENTEXAMYEAR AS VALUE ";
        $query .= " FROM ";
        $query .= "   ENTEXAM_SETTING_MST ";
        $query .= " ORDER BY ";
        $query .= "   VALUE DESC ";

        return $query;
    }

    //名称マスタよりデータを取得
    public function getNameMst($year, $cd, $cd2 = "")
    {
        $query  = " SELECT ";
        $query .= "   NAMECD2 || ':' || NAME1 AS LABEL, ";
        $query .= "   NAMECD2 AS VALUE ";
        $query .= " FROM ";
        $query .= "   V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "   YEAR = '{$year}' ";
        $query .= "   AND NAMECD1 = '{$cd}' ";
        if ($cd2) {
            $query .= "   AND NAMECD2 = '{$cd2}' ";
        }
        $query .= " ORDER BY ";
        $query .= "   VALUE ";

        return $query;
    }

    public function check($model)
    {
        $db = Query::dbCheckOut();
        $query  = " SELECT ";
        $query .= "   MODIFY_FLG ";
        $query .= " FROM ";
        $query .= "   ENTEXAM_SETTINGCDDESC_MST ";
        $query .= " WHERE ";
        $query .= "   SETTING_CD = '{$model->field["SETTING_CD"]}' ";
        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);
        return $row;
    }

    public function getCombo($model)
    {
        $query  = " SELECT DISTINCT ";
        $query .= "   COALESCE(T1.SETTING_CD, T2.SETTING_CD) AS SETTING_CD, ";
        $query .= "   T1.CDMEMO ";
        $query .= " FROM ";
        $query .= "   ENTEXAM_SETTINGCDDESC_MST T1 FULL OUTER JOIN ENTEXAM_SETTING_MST T2 ON T1.SETTING_CD = T2.SETTING_CD ";
        $query .= " WHERE ";
        $query .= "       T2.ENTEXAMYEAR  = '{$model->field["ENTEXAMYEAR"]}' ";
        $query .= "   AND T2.APPLICANTDIV = '{$model->field["APPLICANTDIV"]}' ";
        $query .= " ORDER BY ";
        $query .= "   1 ";

        return $query;
    }

    public function getList($model)
    {
        $query  = " SELECT ";
        $query .= "   * ";
        $query .= " FROM ";
        $query .= "   ENTEXAM_SETTING_MST ";
        $query .= " WHERE ";
        $query .= "       ENTEXAMYEAR  = '{$model->field["ENTEXAMYEAR"]}' ";
        $query .= "   AND APPLICANTDIV = '{$model->field["APPLICANTDIV"]}' ";
        $query .= "   AND SETTING_CD   = '{$model->field["SETTING_CD"]}' ";
        $query .= " ORDER BY SETTING_CD";

        return $query;
    }

    public function getRow($model)
    {
        $db = Query::dbCheckOut();

        $query  = " SELECT ";
        $query .= "   T1.*, ";
        $query .= "   T2.CDMEMO, ";
        $query .= "   T2.MODIFY_FLG ";
        $query .= " FROM ";
        $query .= "   ENTEXAM_SETTING_MST T1 LEFT OUTER JOIN ENTEXAM_SETTINGCDDESC_MST T2 ";
        $query .= "   ON T1.SETTING_CD = T2.SETTING_CD ";
        $query .= " WHERE ";
        $query .= "   T1.ENTEXAMYEAR  = '{$model->field["ENTEXAMYEAR"]}' AND ";
        $query .= "   T1.APPLICANTDIV = '{$model->field["APPLICANTDIV"]}' AND ";
        $query .= "   T1.SETTING_CD   = '{$model->field["SETTING_CD"]}' AND ";
        $query .= "   T1.SEQ          = '{$model->field["SEQ"]}' ";

        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);
        return $row;
    }
    
    //INSERT
    public function &getInsertQuery($model)
    {
        $row = knjlz001Query::check($model);

        $db = Query::dbCheckOut();
        $db->autoCommit(false);
        if (get_count($row) != "0") {
            $data["SETTING_CD"][TEXT]   = $model->field["SETTING_CD"];
            $data["CDMEMO"][TEXT]       = $model->field["CDMEMO"];
            $data["MODIFY_FLG"][TEXT]   = $model->field["MODIFY_FLG"];
            $data["REGISTERCD"][TEXT]   = STAFFCD;
            $data["UPDATED"][NUMBER]    = "sysdate()";
            $where = " WHERE SETTING_CD = '{$model->field["SETTING_CD"]}' ";
            $query = Query::updateSQL($data, "ENTEXAM_SETTINGCDDESC_MST", $where);
            $db->query($query);
        } else {
            $query  = " DELETE FROM ";
            $query .= "   ENTEXAM_SETTINGCDDESC_MST ";
            $query .= " WHERE ";
            $query .= "   SETTING_CD = '{$model->field["SETTING_CD"]}' ";

            $db->query($query);

            $data = array();

            $data["SETTING_CD"][TEXT]   = $model->field["SETTING_CD"];
            $data["CDMEMO"][TEXT]       = $model->field["CDMEMO"];
            $data["MODIFY_FLG"][TEXT]   = $model->field["MODIFY_FLG"];
            $data["REGISTERCD"][TEXT]   = STAFFCD;
            $data["UPDATED"][NUMBER]    = "sysdate()";
            $query = Query::insertSQL($data, "ENTEXAM_SETTINGCDDESC_MST");

            $db->query($query);
        }

        $data = array();
        $data["ENTEXAMYEAR"][TEXT]    = $model->field["ENTEXAMYEAR"];
        $data["APPLICANTDIV"][TEXT]   = $model->field["APPLICANTDIV"];
        $data["SETTING_CD"][TEXT]     = $model->field["SETTING_CD"];
        $data["SEQ"][TEXT]            = $model->field["SEQ"];
        $data["NAME1"][TEXT]          = $model->field["NAME1"];
        $data["NAME2"][TEXT]          = $model->field["NAME2"];
        $data["NAME3"][TEXT]          = $model->field["NAME3"];
        $data["NAME1MEMO"][TEXT]      = $model->field["NAME1MEMO"];
        $data["NAME2MEMO"][TEXT]      = $model->field["NAME2MEMO"];
        $data["NAME3MEMO"][TEXT]      = $model->field["NAME3MEMO"];
        $data["ABBV1"][TEXT]          = $model->field["ABBV1"];
        $data["ABBV2"][TEXT]          = $model->field["ABBV2"];
        $data["ABBV3"][TEXT]          = $model->field["ABBV3"];
        $data["ABBV1MEMO"][TEXT]      = $model->field["ABBV1MEMO"];
        $data["ABBV2MEMO"][TEXT]      = $model->field["ABBV2MEMO"];
        $data["ABBV3MEMO"][TEXT]      = $model->field["ABBV3MEMO"];
        $data["NAMESPARE1"][TEXT]     = $model->field["NAMESPARE1"];
        $data["NAMESPARE2"][TEXT]     = $model->field["NAMESPARE2"];
        $data["NAMESPARE3"][TEXT]     = $model->field["NAMESPARE3"];
        $data["NAMESPARE1MEMO"][TEXT] = $model->field["NAMESPARE1MEMO"];
        $data["NAMESPARE2MEMO"][TEXT] = $model->field["NAMESPARE2MEMO"];
        $data["NAMESPARE3MEMO"][TEXT] = $model->field["NAMESPARE3MEMO"];
        $data["REGISTERCD"][TEXT]     = STAFFCD;
        $data["UPDATED"][NUMBER]      = "sysdate()";

        $query = Query::insertSQL($data, "ENTEXAM_SETTING_MST");
        
        $db->query($query);
      
        $db->commit();
        Query::dbCheckIn($db);
        return;
    }

    //UPDATE
    public function &getUpdateQuery($model)
    {
        $row = knjlz001Query::check($model);
         
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        if (get_count($row) != "0") {
            $data["SETTING_CD"][TEXT]   = $model->field["SETTING_CD"];
            $data["CDMEMO"][TEXT]       = $model->field["CDMEMO"];
            $data["MODIFY_FLG"][TEXT]   = $model->field["MODIFY_FLG"];
            $data["REGISTERCD"][TEXT]   = STAFFCD;
            $data["UPDATED"][NUMBER]    = "sysdate()";
            $where = " WHERE SETTING_CD = '{$model->field["SETTING_CD"]}' ";
            $query = Query::updateSQL($data, "ENTEXAM_SETTINGCDDESC_MST", $where);
            $db->query($query);
        } else {
            $query  = " DELETE FROM ";
            $query .= "   ENTEXAM_SETTINGCDDESC_MST ";
            $query .= " WHERE ";
            $query .= "   SETTING_CD = '{$model->field["SETTING_CD"]}' ";

            $db->query($query);

            $data = array();

            $data["SETTING_CD"][TEXT]   = $model->field["SETTING_CD"];
            $data["CDMEMO"][TEXT]       = $model->field["CDMEMO"];
            $data["MODIFY_FLG"][TEXT]   = $model->field["MODIFY_FLG"];
            $data["REGISTERCD"][TEXT]   = STAFFCD;
            $data["UPDATED"][NUMBER]    = "sysdate()";
            $query = Query::insertSQL($data, "ENTEXAM_SETTINGCDDESC_MST");

            $db->query($query);
        }

        $data = array();
        $data["ENTEXAMYEAR"][TEXT]    = $model->field["ENTEXAMYEAR"];
        $data["APPLICANTDIV"][TEXT]   = $model->field["APPLICANTDIV"];
        $data["SETTING_CD"][TEXT]     = $model->field["SETTING_CD"];
        $data["SEQ"][TEXT]            = $model->field["SEQ"];
        $data["NAME1"][TEXT]          = $model->field["NAME1"];
        $data["NAME2"][TEXT]          = $model->field["NAME2"];
        $data["NAME3"][TEXT]          = $model->field["NAME3"];
        $data["NAME1MEMO"][TEXT]      = $model->field["NAME1MEMO"];
        $data["NAME2MEMO"][TEXT]      = $model->field["NAME2MEMO"];
        $data["NAME3MEMO"][TEXT]      = $model->field["NAME3MEMO"];
        $data["ABBV1"][TEXT]          = $model->field["ABBV1"];
        $data["ABBV2"][TEXT]          = $model->field["ABBV2"];
        $data["ABBV3"][TEXT]          = $model->field["ABBV3"];
        $data["ABBV1MEMO"][TEXT]      = $model->field["ABBV1MEMO"];
        $data["ABBV2MEMO"][TEXT]      = $model->field["ABBV2MEMO"];
        $data["ABBV3MEMO"][TEXT]      = $model->field["ABBV3MEMO"];
        $data["NAMESPARE1"][TEXT]     = $model->field["NAMESPARE1"];
        $data["NAMESPARE2"][TEXT]     = $model->field["NAMESPARE2"];
        $data["NAMESPARE3"][TEXT]     = $model->field["NAMESPARE3"];
        $data["NAMESPARE1MEMO"][TEXT] = $model->field["NAMESPARE1MEMO"];
        $data["NAMESPARE2MEMO"][TEXT] = $model->field["NAMESPARE2MEMO"];
        $data["NAMESPARE3MEMO"][TEXT] = $model->field["NAMESPARE3MEMO"];
        $data["REGISTERCD"][TEXT]     = STAFFCD;
        $data["UPDATED"][NUMBER]      = "sysdate()";
        
        $where  = " WHERE ENTEXAMYEAR  = '{$model->field["ENTEXAMYEAR"]}' ";
        $where .= "   AND APPLICANTDIV = '{$model->field["APPLICANTDIV"]}' ";
        $where .= "   AND SETTING_CD   = '{$model->field["SETTING_CD"]}' ";
        $where .= "   AND SEQ          = '{$model->field["SEQ"]}' ";
        $query = Query::updateSQL($data, "ENTEXAM_SETTING_MST", $where);
        
        $db->query($query);

        $db->commit();
        Query::dbCheckIn($db);
        return;
    }

    //DELETE
    public function &getDeleteQuery($model)
    {
        $db = Query::dbCheckOut();
    
        $db->autoCommit(false);
        $query  = " DELETE FROM ENTEXAM_SETTING_MST ";
        $query .= " WHERE ENTEXAMYEAR  = '{$model->field["ENTEXAMYEAR"]}' ";
        $query .= "   AND APPLICANTDIV = '{$model->field["APPLICANTDIV"]}' ";
        $query .= "   AND SETTING_CD   = '{$model->field["SETTING_CD"]}' ";
        $query .= "   AND SEQ          = '{$model->field["SEQ"]}' ";

        $db->query($query);
    
        $db->commit();

        $query = knjlz001Query::getList($model);
        $getList = $db->getOne($query);

        Query::dbCheckIn($db);
        $result = strlen($getList) > 0;

        return $result;
    }

    //データ件数取得
    public function getCopyCnt($year, $div)
    {
        $query  = " SELECT ";
        if ($div == "cnt") {
            $query .= "     COUNT(*) ";
        } else {
            $query .= "     * ";
        }
        $query .= " FROM ";
        $query .= "     ENTEXAM_SETTING_MST ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR = '{$year}' ";

        return $query;
    }

    //コピー
    public function &getCopyQuery($model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $preYear = $model->field["ENTEXAMYEAR"] - 1;
        $result = $db->query(knjlz001Query::getCopyCnt($preYear, "data"));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $data = array();
            $data["ENTEXAMYEAR"][TEXT]      = $model->field["ENTEXAMYEAR"];
            $data["APPLICANTDIV"][TEXT]     = $row["APPLICANTDIV"];
            $data["SETTING_CD"][TEXT]       = $row["SETTING_CD"];
            $data["SEQ"][TEXT]              = $row["SEQ"];
            $data["NAME1"][TEXT]            = $row["NAME1"];
            $data["NAME2"][TEXT]            = $row["NAME2"];
            $data["NAME3"][TEXT]            = $row["NAME3"];
            $data["ABBV1"][TEXT]            = $row["ABBV1"];
            $data["ABBV2"][TEXT]            = $row["ABBV2"];
            $data["ABBV3"][TEXT]            = $row["ABBV3"];
            $data["NAMESPARE1"][TEXT]       = $row["NAMESPARE1"];
            $data["NAMESPARE2"][TEXT]       = $row["NAMESPARE2"];
            $data["NAMESPARE3"][TEXT]       = $row["NAMESPARE3"];
            $data["NAME1MEMO"][TEXT]        = $row["NAME1MEMO"];
            $data["NAME2MEMO"][TEXT]        = $row["NAME2MEMO"];
            $data["NAME3MEMO"][TEXT]        = $row["NAME3MEMO"];
            $data["ABBV1MEMO"][TEXT]        = $row["ABBV1MEMO"];
            $data["ABBV2MEMO"][TEXT]        = $row["ABBV2MEMO"];
            $data["ABBV3MEMO"][TEXT]        = $row["ABBV3MEMO"];
            $data["NAMESPARE1MEMO"][TEXT]   = $row["NAMESPARE1MEMO"];
            $data["NAMESPARE2MEMO"][TEXT]   = $row["NAMESPARE2MEMO"];
            $data["NAMESPARE3MEMO"][TEXT]   = $row["NAMESPARE3MEMO"];
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][NUMBER]        = "sysdate()";

            $query = Query::insertSQL($data, "ENTEXAM_SETTING_MST");
            $db->query($query);
        }

        $db->commit();
        Query::dbCheckIn($db);
        return;
    }
}
