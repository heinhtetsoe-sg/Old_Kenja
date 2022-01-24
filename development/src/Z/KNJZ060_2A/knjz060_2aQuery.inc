<?php

require_once('for_php7.php');

class knjz060_2aQuery extends Query {
    //IBCLASS_MST取得
    function getListData() {
        $query  = " SELECT ";
        $query .= "      T1.*, ";
        $query .= "      L1.ABBV1 ";
        $query .= " FROM ";
        $query .= "     IBCLASS_MST T1 ";
        $query .= "     LEFT JOIN NAME_MST L1 ON L1.NAMECD1 = 'A034' ";
        $query .= "                          AND L1.NAME1 = T1.IBPRG_COURSE ";
        $query .= " ORDER BY ";
        $query .= "     L1.NAMECD2, ";
        $query .= "     T1.IBPRG_COURSE, ";
        $query .= "     T1.IBCLASSCD ";

        return $query;
    }

    //１レコード取得
    function getRow($model, $ibclasscd, $ibprg_course) {
        $db = Query::dbCheckOut();

        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     IBCLASS_MST ";
        $query .= " WHERE ";
        $query .= "     IBCLASSCD       = '".$ibclasscd."' AND ";
        $query .= "     IBPRG_COURSE    = '".$ibprg_course."' ";

        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);
        return $row;
    }

    //学校種別取得
    function getIBSchoolKind() {
        $query  = " SELECT ";
        $query .= "      NAME1 AS VALUE, ";
        $query .= "      ABBV1 AS LABEL ";
        $query .= " FROM ";
        $query .= "      NAME_MST ";
        $query .= " WHERE ";
        $query .= "      NAMECD1 = 'A034' ";
        $query .= " ORDER BY ";
        $query .= "      NAMECD2 ";

        return $query;
    }

    //年度データの確認
    function IsExistingYdat($model) {
        $db = Query::dbCheckOut();

        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     IBCLASS_YDAT ";
        $query .= " WHERE ";
        $query .= "     IBCLASSCD       = '".$model->ibclasscd."' AND ";
        $query .= "     IBPRG_COURSE    = '".$model->ibprg_course."' ";

        $row = $db->getRow($query);
        Query::dbCheckIn($db);

        $flg = (is_array($row)) ? true : false;
        return $flg;
    }

    //科目マスタの確認
    function IsExistingSubclass($model) {
        $db = Query::dbCheckOut();

        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     IBSUBCLASS_MST ";
        $query .= " WHERE ";
        $query .= "     IBCLASSCD       = '".$model->ibclasscd."' AND ";
        $query .= "     IBPRG_COURSE    = '".$model->ibprg_course."' ";

        $row = $db->getRow($query);
        Query::dbCheckIn($db);

        $flg = (is_array($row)) ? true : false;
        return $flg;
    }

    //--- UPDATE
    function &getUpdateQuery($model, $fields) {
        $db = Query::dbCheckOut();

        $data = array();
        $data["IBCLASSNAME"][TEXT]          = $fields["IBCLASSNAME"];
        $data["IBCLASSABBV"][TEXT]          = $fields["IBCLASSABBV"];
        $data["IBCLASSNAME_ENG"][TEXT]      = $fields["IBCLASSNAME_ENG"];
        $data["IBCLASSABBV_ENG"][TEXT]      = $fields["IBCLASSABBV_ENG"];
        $data["IBCLASSORDERNAME1"][TEXT]    = $fields["IBCLASSORDERNAME1"];
        $data["IBCLASSORDERNAME2"][TEXT]    = $fields["IBCLASSORDERNAME2"];
        $data["IBCLASSORDERNAME3"][TEXT]    = $fields["IBCLASSORDERNAME3"];
        $data["IBSUBCLASSES"][NUMBER]       = $fields["IBSUBCLASSES"];
        $data["IBSHOWORDER"][NUMBER]        = $fields["IBSHOWORDER"];
        $data["IBSHOWORDER2"][NUMBER]       = $fields["IBSHOWORDER2"];
        $data["IBSHOWORDER3"][NUMBER]       = $fields["IBSHOWORDER3"];
        $data["IBSHOWORDER4"][NUMBER]       = $fields["IBSHOWORDER4"];
        $data["IBELECTDIV"][TEXT]           = ($fields["IBELECTDIV"] == 1) ? 1 : 0;
        $data["IBSPECIALDIV"][TEXT]         = $fields["IBSPECIALDIV"];
        $data["REGISTERCD"][TEXT]           = STAFFCD;
        $data["UPDATED"][NUMBER]            = "sysdate()";

        //追加
        if ($this->cmd == "add") {
            $data["IBCLASSCD"][TEXT]            = $fields["IBCLASSCD"];
            $data["IBPRG_COURSE"][TEXT]         = $fields["IBPRG_COURSE"];

            $query = Query::insertSQL($data, "IBCLASS_MST");
            $db->query($query);

        //更新
        } else {
            $where  = " WHERE ";
            $where .= "     IBCLASSCD       = '".$fields["IBCLASSCD"]."' AND ";
            $where .= "     IBPRG_COURSE    = '".$fields["IBPRG_COURSE"]."' ";

            $query = Query::updateSQL($data, "IBCLASS_MST", $where);
            $db->query($query);
        }

        Query::dbCheckIn($db);
        return;
    }

    //--- DELETE
    function &getDeleteQuery($model, $fields) {
        $db = Query::dbCheckOut();

        $query  = " DELETE ";
        $query .= " FROM ";
        $query .= "     IBCLASS_MST ";
        $query .= " WHERE ";
        $query .= "     IBCLASSCD       = '".$fields["IBCLASSCD"]."' AND ";
        $query .= "     IBPRG_COURSE    = '".$fields["IBPRG_COURSE"]."' ";

        $db->query($query);
        Query::dbCheckIn($db);

        return;
    }
}
?>
