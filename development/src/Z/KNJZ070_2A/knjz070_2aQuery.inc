<?php

require_once('for_php7.php');

class knjz070_2aQuery extends Query {
    //IBSUBCLASS_MST取得
    function getListData() {
        $query  = " SELECT ";
        $query .= "      T1.* ";
        $query .= " FROM ";
        $query .= "     IBSUBCLASS_MST T1 ";
        $query .= "     LEFT JOIN NAME_MST L1 ON L1.NAMECD1 = 'A034' ";
        $query .= "                          AND L1.NAME1   = T1.IBPRG_COURSE ";
        $query .= " ORDER BY ";
        $query .= "     L1.NAMECD2, ";
        $query .= "     T1.IBPRG_COURSE, ";
        $query .= "     T1.IBCURRICULUM_CD, ";
        $query .= "     T1.IBCLASSCD, ";
        $query .= "     T1.IBSUBCLASSCD ";

        return $query;
    }

    //１レコード取得
    function getRow($model, $ibclasscd, $ibprg_course, $ibcurriculum_cd, $ibsubclasscd) {
        $db = Query::dbCheckOut();

        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     IBSUBCLASS_MST ";
        $query .= " WHERE ";
        $query .= "     IBCLASSCD       = '".$ibclasscd."' AND ";
        $query .= "     IBPRG_COURSE    = '".$ibprg_course."' AND ";
        $query .= "     IBCURRICULUM_CD = '".$ibcurriculum_cd."' AND ";
        $query .= "     IBSUBCLASSCD    = '".$ibsubclasscd."' ";

        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);
        return $row;
    }

    //教科一覧取得
    function getClassData() {
        $query  = " SELECT ";
        $query .= "     T1.IBCLASSCD || '-' || T1.IBPRG_COURSE AS VALUE, ";
        $query .= "     T1.IBCLASSCD || '-' || T1.IBPRG_COURSE || ' ' || T1.IBCLASSNAME_ENG AS LABEL ";
        $query .= " FROM ";
        $query .= "     IBCLASS_MST T1 ";
        $query .= "     LEFT JOIN NAME_MST L1 ON L1.NAMECD1 = 'A034' ";
        $query .= "                          AND L1.NAME1   = T1.IBPRG_COURSE ";
        $query .= " ORDER BY ";
        $query .= "     L1.NAMECD2, ";
        $query .= "     T1.IBPRG_COURSE, ";
        $query .= "     T1.IBCLASSCD ";

        return $query;
    }

    //教育課程取得
    function getIBCurriculumCd() {
        $query  = " SELECT ";
        $query .= "      NAMECD2 AS VALUE, ";
        $query .= "      NAMECD2 || '：' || NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "      NAME_MST ";
        $query .= " WHERE ";
        $query .= "      NAMECD1 = 'Z034' ";
        $query .= " ORDER BY ";
        $query .= "      VALUE ";

        return $query;
    }

    //年度データの確認
    function IsExistingYdat($model) {
        $db = Query::dbCheckOut();

        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     IBSUBCLASS_YDAT ";
        $query .= " WHERE ";
        $query .= "     IBCLASSCD           = '".$model->ibclasscd."' AND ";
        $query .= "     IBPRG_COURSE        = '".$model->ibprg_course."' AND ";
        $query .= "     IBCURRICULUM_CD     = '".$model->ibcurriculum_cd."' AND ";
        $query .= "     IBSUBCLASSCD        = '".$model->ibsubclasscd."' ";

        $row = $db->getRow($query);
        Query::dbCheckIn($db);

        $flg = (is_array($row)) ? true : false;
        return $flg;
    }

    //--- UPDATE
    function &getUpdateQuery($model, $fields) {
        $db = Query::dbCheckOut();

        $data = array();
        $data["IBSUBCLASSNAME"][TEXT]           = $fields["IBSUBCLASSNAME"];
        $data["IBSUBCLASSABBV"][TEXT]           = $fields["IBSUBCLASSABBV"];
        $data["IBSUBCLASSNAME_ENG"][TEXT]       = $fields["IBSUBCLASSNAME_ENG"];
        $data["IBSUBCLASSABBV_ENG"][TEXT]       = $fields["IBSUBCLASSABBV_ENG"];
        $data["IBSUBCLASSORDERNAME1"][TEXT]     = $fields["IBSUBCLASSORDERNAME1"];
        $data["IBSUBCLASSORDERNAME2"][TEXT]     = $fields["IBSUBCLASSORDERNAME2"];
        $data["IBSUBCLASSORDERNAME3"][TEXT]     = $fields["IBSUBCLASSORDERNAME3"];
        $data["IBSHOWORDER"][NUMBER]            = $fields["IBSHOWORDER"];
        $data["IBSHOWORDER2"][NUMBER]           = $fields["IBSHOWORDER2"];
        $data["IBSHOWORDER3"][NUMBER]           = $fields["IBSHOWORDER3"];
        $data["IBSUBCLASSCD2"][TEXT]            = $fields["IBSUBCLASSCD2"];
        $data["IBSUBCLASSCD3"][TEXT]            = $fields["IBSUBCLASSCD3"];
        $data["IBELECTDIV"][TEXT]               = ($fields["IBELECTDIV"] == 1) ? 1 : 0;
        $data["REGISTERCD"][TEXT]               = STAFFCD;
        $data["UPDATED"][NUMBER]                = "sysdate()";

        //追加
        if ($this->cmd == "add") {
            $data["IBCLASSCD"][TEXT]            = $fields["IBCLASSCD"];
            $data["IBPRG_COURSE"][TEXT]         = $fields["IBPRG_COURSE"];
            $data["IBCURRICULUM_CD"][TEXT]      = $fields["IBCURRICULUM_CD"];
            $data["IBSUBCLASSCD"][TEXT]         = $fields["IBSUBCLASSCD"];

            $query = Query::insertSQL($data, "IBSUBCLASS_MST");
            $db->query($query);

        //更新
        } else {
            $where  = " WHERE ";
            $where .= "     IBCLASSCD       = '".$fields["IBCLASSCD"]."' AND ";
            $where .= "     IBPRG_COURSE    = '".$fields["IBPRG_COURSE"]."' AND ";
            $where .= "     IBCURRICULUM_CD = '".$fields["IBCURRICULUM_CD"]."' AND ";
            $where .= "     IBSUBCLASSCD    = '".$fields["IBSUBCLASSCD"]."' ";

            $query = Query::updateSQL($data, "IBSUBCLASS_MST", $where);
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
        $query .= "     IBSUBCLASS_MST ";
        $query .= " WHERE ";
        $query .= "     IBCLASSCD           = '".$fields["IBCLASSCD"]."' AND ";
        $query .= "     IBPRG_COURSE        = '".$fields["IBPRG_COURSE"]."' AND ";
        $query .= "     IBCURRICULUM_CD     = '".$fields["IBCURRICULUM_CD"]."' AND ";
        $query .= "     IBSUBCLASSCD        = '".$fields["IBSUBCLASSCD"]."' ";

        $db->query($query);
        Query::dbCheckIn($db);

        return;
    }
}
?>
