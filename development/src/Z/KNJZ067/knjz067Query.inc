<?php

require_once('for_php7.php');

class knjz067Query extends Query {
    //年度一覧取得
    function getIBYear() {
        $query  = " WITH THIS_YEAR(LABEL, VALUE) AS (VALUES('".CTRL_YEAR."', '".CTRL_YEAR."')) ";
        $query .= " SELECT ";
        $query .= "     LABEL, ";
        $query .= "     VALUE ";
        $query .= " FROM ";
        $query .= "     THIS_YEAR ";
        $query .= " UNION ";
        $query .= " SELECT DISTINCT ";
        $query .= "     IBYEAR AS LABEL, ";
        $query .= "     IBYEAR AS VALUE ";
        $query .= " FROM ";
        $query .= "     IBSUBCLASS_SELECT_DAT ";
        $query .= " ORDER BY ";
        $query .= "     VALUE DESC ";

        return $query;
    }

    //左フレームのリスト
    function getList($model) {
        $query  = " SELECT ";
        $query .= "      T1.*, ";
        $query .= "      L1.ABBV1, ";
        $query .= "      L2.IBSUBCLASSNAME_ENG, ";
        $query .= "      L3.GRADE_NAME1 ";
        $query .= " FROM ";
        $query .= "     IBSUBCLASS_SELECT_DAT T1 ";
        $query .= "     LEFT JOIN NAME_MST L1 ON L1.NAMECD1 = 'A034' ";
        $query .= "                          AND L1.NAME1 = T1.IBPRG_COURSE ";
        $query .= "     LEFT JOIN IBSUBCLASS_MST L2 ON L2.IBCLASSCD         = T1.IBCLASSCD ";
        $query .= "                                AND L2.IBPRG_COURSE      = T1.IBPRG_COURSE ";
        $query .= "                                AND L2.IBCURRICULUM_CD   = T1.IBCURRICULUM_CD ";
        $query .= "                                AND L2.IBSUBCLASSCD      = T1.IBSUBCLASSCD ";
        $query .= "     LEFT JOIN SCHREG_REGD_GDAT L3 ON L3.YEAR    = T1.IBYEAR ";
        $query .= "                                  AND L3.GRADE   = T1.IBGRADE ";
        $query .= " WHERE ";
        $query .= "     T1.IBYEAR       = '".$model->ibyear."' AND ";
        $query .= "     T1.IBNOT_CALC   = '1' ";
        $query .= " ORDER BY ";
        $query .= "     L1.NAMECD2, ";
        $query .= "     T1.IBPRG_COURSE, ";
        $query .= "     T1.IBCURRICULUM_CD, ";
        $query .= "     T1.IBCLASSCD, ";
        $query .= "     T1.IBSUBCLASSCD ";

        return $query;
    }

    //IBSUBCLASS_SELECT_DATから１レコード取得
    function getIBSubclassSelectDat($ibyear, $ibgrade, $ibclasscd, $ibprg_course, $ibcurriculum_cd, $ibsubclasscd) {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     IBSUBCLASS_SELECT_DAT ";
        $query .= " WHERE ";
        $query .= "     IBYEAR          = '".$ibyear."' AND ";
        $query .= "     IBGRADE         = '".$ibgrade."' AND ";
        $query .= "     IBCLASSCD       = '".$ibclasscd."' AND ";
        $query .= "     IBPRG_COURSE    = '".$ibprg_course."' AND ";
        $query .= "     IBCURRICULUM_CD = '".$ibcurriculum_cd."' AND ";
        $query .= "     IBSUBCLASSCD    = '".$ibsubclasscd."' AND ";
        $query .= "     IBNOT_CALC      = '1' ";

        return $query;
    }

    //学年一覧取得
    function getIBGrade($model, $flg) {
        $query  = " SELECT ";
        if ($flg == "check") {
            $query .= "     COUNT(*) ";
        } else {
            $query .= " DISTINCT ";
            $query .= "     T1.GRADE AS VALUE, ";
            $query .= "     T1.GRADE_NAME1 AS LABEL ";
        }
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_GDAT T1 ";
        $query .= "     LEFT JOIN V_NAME_MST L1 ON L1.YEAR = T1.YEAR ";
        $query .= "         AND L1.NAMECD1 = 'A034'  ";
        $query .= "         AND T1.GRADE BETWEEN L1.NAME2 AND L1.NAME3 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".$model->ibyear."' ";
        $query .= "     AND (L1.YEAR IS NULL OR L1.YEAR IS NOT NULL AND L1.NAME1 <> 'M') ";
        if ($flg == "list") {
            $query .= " ORDER BY ";
            $query .= "     VALUE ";
        }

        return $query;
    }

    //IBコース一覧取得
    function getIBPrgCourse($model, $flg) {
        if ($model->cmd === 'edit' && $model->ibgrade != "") {
            $set_grade = $model->ibgrade;
        } else {
            $set_grade = $model->field["IBGRADE"];
        }
    
        $query  = " SELECT ";
        if ($flg == "check") {
            $query .= "     COUNT(*) ";
        } else {
            $query .= "     NAME1 AS VALUE, ";
            $query .= "     ABBV1 AS LABEL ";
        }
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR    = '".$model->ibyear."' ";
        $query .= " AND NAMECD1 = 'A034' ";
        $query .= " AND '".$set_grade."' BETWEEN NAME2 AND NAME3 ";
        if ($flg == "list") {
            $query .= " ORDER BY ";
            $query .= "     NAMECD2 ";
        }

        return $query;
    }

    //科目一覧取得
    function getIBSubclasscd($model, $ibprg_course, $flg) {
        $query  = " SELECT ";
        if ($flg == "check") {
            $query .= "     COUNT(*) ";
        } else {
            $query .= "     T1.IBCLASSCD || '-' || T1.IBPRG_COURSE || '-' || T1.IBCURRICULUM_CD || '-' || T1.IBSUBCLASSCD AS VALUE, ";
            $query .= "     T1.IBCLASSCD || '-' || T1.IBPRG_COURSE || '-' || T1.IBCURRICULUM_CD || '-' || T1.IBSUBCLASSCD || ' ' || T1.IBSUBCLASSNAME_ENG AS LABEL ";
        }
        $query .= " FROM ";
        $query .= "     IBSUBCLASS_MST T1, ";
        $query .= "     IBSUBCLASS_YDAT T2 ";
        $query .= " WHERE ";
        $query .= "     T1.IBCLASSCD        = T2.IBCLASSCD AND ";
        $query .= "     T1.IBPRG_COURSE     = T2.IBPRG_COURSE AND ";
        $query .= "     T1.IBCURRICULUM_CD  = T2.IBCURRICULUM_CD AND ";
        $query .= "     T1.IBSUBCLASSCD     = T2.IBSUBCLASSCD AND ";
        $query .= "     T2.IBYEAR           = '".$model->ibyear."' ";
        if ($flg == "list") {
            $query .= "     AND ";
            $query .= "     T1.IBPRG_COURSE     = '".$ibprg_course."' ";
            $query .= " ORDER BY ";
            $query .= "     T1.IBCURRICULUM_CD, ";
            $query .= "     T1.IBCLASSCD, ";
            $query .= "     T1.IBSUBCLASSCD ";
        }

        return $query;
    }

    /**********/
    /* INSERT */
    /**********/
    function &getInsertQuery($model) {
        $db = Query::dbCheckOut();

        //削除
        $query  = " DELETE FROM ";
        $query .= "     IBSUBCLASS_SELECT_DAT ";
        $query .= " WHERE ";
        $query .= "     IBYEAR          = '".$model->ibyear."' AND ";
        $query .= "     IBGRADE         = '".$model->field["IBGRADE"]."' AND ";
        $query .= "     IBCLASSCD       = '".$model->field["IBCLASSCD"]."' AND ";
        $query .= "     IBPRG_COURSE    = '".$model->field["IBPRG_COURSE"]."' AND ";
        $query .= "     IBCURRICULUM_CD = '".$model->field["IBCURRICULUM_CD"]."' AND ";
        $query .= "     IBSUBCLASSCD    = '".$model->field["IBSUBCLASSCD"]."' ";

        $db->query($query);

        //追加
        $data = array();
        $data["IBYEAR"][TEXT]           = $model->ibyear;
        $data["IBGRADE"][TEXT]          = $model->field["IBGRADE"];
        $data["IBCLASSCD"][TEXT]        = $model->field["IBCLASSCD"];
        $data["IBPRG_COURSE"][TEXT]     = $model->field["IBPRG_COURSE"];
        $data["IBCURRICULUM_CD"][TEXT]  = $model->field["IBCURRICULUM_CD"];
        $data["IBSUBCLASSCD"][TEXT]     = $model->field["IBSUBCLASSCD"];
        $data["IBNOT_CALC"][TEXT]       = "1";
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][NUMBER]        = "sysdate()";

        $query = Query::insertSQL($data, "IBSUBCLASS_SELECT_DAT");
        $db->query($query);
        Query::dbCheckIn($db);
    }

    /**********/
    /* DELETE */
    /**********/
    function &getDeleteQuery($model) {
        $db = Query::dbCheckOut();

        $query  = " DELETE FROM ";
        $query .= "     IBSUBCLASS_SELECT_DAT ";
        $query .= " WHERE ";
        $query .= "     IBYEAR          = '".$model->ibyear."' AND ";
        $query .= "     IBGRADE         = '".$model->field["IBGRADE"]."' AND ";
        $query .= "     IBCLASSCD       = '".$model->field["IBCLASSCD"]."' AND ";
        $query .= "     IBPRG_COURSE    = '".$model->field["IBPRG_COURSE"]."' AND ";
        $query .= "     IBCURRICULUM_CD = '".$model->field["IBCURRICULUM_CD"]."' AND ";
        $query .= "     IBSUBCLASSCD    = '".$model->field["IBSUBCLASSCD"]."' ";

        $db->query($query);
        Query::dbCheckIn($db);
    }

    //IBSUBCLASS_SELECT_DAT存在チェック
    function checkIBSubclassSelectDat($ibyear) {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     IBSUBCLASS_SELECT_DAT ";
        $query .= " WHERE ";
        $query .= "     IBYEAR      = '".$ibyear."' AND ";
        $query .= "     IBNOT_CALC  = '1' ";

        return $query;
    }

    //前年度データ取得
    function getPreYearData($model) {
        $query  = " SELECT DISTINCT ";
        $query .= "     T1.* ";
        $query .= " FROM ";
        $query .= "     IBSUBCLASS_SELECT_DAT T1, ";
        $query .= "     SCHREG_REGD_GDAT T2, ";
        $query .= "     V_NAME_MST T3, ";
        $query .= "     IBSUBCLASS_YDAT T4 ";
        $query .= " WHERE ";
        $query .= "     T1.IBYEAR           = '".($model->ibyear-1)."' AND ";
        $query .= "     T1.IBNOT_CALC       = '1' AND ";
        $query .= "     T2.YEAR             = '".$model->ibyear."' AND ";
        $query .= "     T1.IBGRADE          = T2.GRADE AND ";
        $query .= "     T3.NAMECD1          = 'A034' AND ";
        $query .= "     T3.YEAR             = '".$model->ibyear."' AND ";
        $query .= "     T1.IBPRG_COURSE     = T3.NAME1 AND ";
        $query .= "     T4.IBYEAR           = '".$model->ibyear."' AND ";
        $query .= "     T1.IBCLASSCD        = T4.IBCLASSCD AND ";
        $query .= "     T1.IBPRG_COURSE     = T4.IBPRG_COURSE AND ";
        $query .= "     T1.IBCURRICULUM_CD  = T4.IBCURRICULUM_CD AND ";
        $query .= "     T1.IBSUBCLASSCD     = T4.IBSUBCLASSCD ";

        return $query;
    }

    /********/
    /* COPY */
    /********/
    function &getCopyQuery($model) {
        $db = Query::dbCheckOut();

        //削除
        $query  = " DELETE FROM ";
        $query .= "     IBSUBCLASS_SELECT_DAT ";
        $query .= " WHERE ";
        $query .= "     IBYEAR = '".$model->ibyear."' ";
        $db->query($query);

        //前年度データ
        $query = knjz067Query::getPreYearData($model);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            //追加
            $data = array();
            $data["IBYEAR"][TEXT]           = $model->ibyear;
            $data["IBGRADE"][TEXT]          = $row["IBGRADE"];
            $data["IBCLASSCD"][TEXT]        = $row["IBCLASSCD"];
            $data["IBPRG_COURSE"][TEXT]     = $row["IBPRG_COURSE"];
            $data["IBCURRICULUM_CD"][TEXT]  = $row["IBCURRICULUM_CD"];
            $data["IBSUBCLASSCD"][TEXT]     = $row["IBSUBCLASSCD"];
            $data["IBNOT_CALC"][TEXT]       = "1";
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][NUMBER]        = "sysdate()";

            $query = Query::insertSQL($data, "IBSUBCLASS_SELECT_DAT");
            $db->query($query);
        }
        Query::dbCheckIn($db);
    }
}
?>
