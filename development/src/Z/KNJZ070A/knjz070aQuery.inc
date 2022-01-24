<?php

require_once('for_php7.php');

class knjz070aQuery extends Query {
    //年度一覧取得
    function getYearQuery() {
        $query  = " SELECT DISTINCT ";
        $query .= "     IBYEAR AS VALUE, ";
        $query .= "     IBYEAR AS LABEL ";
        $query .= " FROM ";
        $query .= "     IBSUBCLASS_YDAT ";
        $query .= " ORDER BY ";
        $query .= "     VALUE DESC ";

        return $query;
    }

    //年度IB科目一覧取得
    function getYearSubclassQuery($model) {
        $query  = " SELECT ";
        $query .= "     T1.IBCLASSCD || '-' || T1.IBPRG_COURSE || '-' || T1.IBCURRICULUM_CD || '-' || T1.IBSUBCLASSCD || ' ' || T2.IBSUBCLASSNAME_ENG AS LABEL, ";
        $query .= "     T1.IBCLASSCD || '-' || T1.IBPRG_COURSE || '-' || T1.IBCURRICULUM_CD || '-' || T1.IBSUBCLASSCD || '-' || VALUE(L1.NAMECD2,'') AS VALUE ";
        $query .= " FROM ";
        $query .= "     IBSUBCLASS_YDAT T1, ";
        $query .= "     IBSUBCLASS_MST T2 ";
        $query .= "     LEFT JOIN NAME_MST L1 ON L1.NAMECD1 = 'A034' ";
        $query .= "                          AND L1.NAME1   = T2.IBPRG_COURSE ";
        $query .= " WHERE ";
        $query .= "     T1.IBYEAR           = '".$model->year."' AND ";
        $query .= "     T1.IBCLASSCD        = T2.IBCLASSCD AND ";
        $query .= "     T1.IBPRG_COURSE     = T2.IBPRG_COURSE AND ";
        $query .= "     T1.IBCURRICULUM_CD  = T2.IBCURRICULUM_CD AND ";
        $query .= "     T1.IBSUBCLASSCD     = T2.IBSUBCLASSCD ";
        $query .= " ORDER BY ";
        $query .= "     L1.NAMECD2, ";
        $query .= "     T1.IBPRG_COURSE, ";
        $query .= "     T1.IBCURRICULUM_CD, ";
        $query .= "     T1.IBCLASSCD, ";
        $query .= "     T1.IBSUBCLASSCD ";

        return $query;
    }

    //IB科目一覧取得
    function getSubclassQuery($model) {
        $query  = " SELECT ";
        $query .= "     T1.IBCLASSCD || '-' || T1.IBPRG_COURSE || '-' || T1.IBCURRICULUM_CD || '-' || T1.IBSUBCLASSCD || ' ' || T1.IBSUBCLASSNAME_ENG AS LABEL, ";
        $query .= "     T1.IBCLASSCD || '-' || T1.IBPRG_COURSE || '-' || T1.IBCURRICULUM_CD || '-' || T1.IBSUBCLASSCD || '-' || VALUE(L1.NAMECD2,'') AS VALUE ";
        $query .= " FROM ";
        $query .= "     IBSUBCLASS_MST T1 ";
        $query .= "     LEFT JOIN NAME_MST L1 ON L1.NAMECD1 = 'A034' ";
        $query .= "                          AND L1.NAME1   = T1.IBPRG_COURSE ";
        $query .= " WHERE ";
        $query .= "     NOT EXISTS (SELECT ";
        $query .= "                     'X' ";
        $query .= "                 FROM ";
        $query .= "                     IBSUBCLASS_YDAT S1 ";
        $query .= "                 WHERE ";
        $query .= "                     S1.IBYEAR           = '".$model->year."' AND ";
        $query .= "                     S1.IBCLASSCD        = T1.IBCLASSCD AND ";
        $query .= "                     S1.IBPRG_COURSE     = T1.IBPRG_COURSE AND ";
        $query .= "                     S1.IBCURRICULUM_CD  = T1.IBCURRICULUM_CD AND ";
        $query .= "                     S1.IBSUBCLASSCD     = T1.IBSUBCLASSCD) ";
        $query .= " ORDER BY ";
        $query .= "     L1.NAMECD2, ";
        $query .= "     T1.IBPRG_COURSE, ";
        $query .= "     T1.IBCURRICULUM_CD, ";
        $query .= "     T1.IBCLASSCD, ";
        $query .= "     T1.IBSUBCLASSCD ";

        return $query;
    }

    //UPDATE
    function &getUpdateQuery(&$model) {
        $array = explode (",", $model->selectdata);

        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //削除
        $query  = " DELETE ";
        $query .= " FROM ";
        $query .= "      IBSUBCLASS_YDAT ";
        $query .= " WHERE ";
        $query .= "      IBYEAR = '".$model->year."' ";
        $db->query($query);

        //追加
        if ($array[0] != "") {
            for ($i = 0; $i < get_count($array); $i++) {
                list ($ibclasscd, $ibprg_course, $ibcurriculum_cd, $ibsubclasscd, $sort) = explode('-', $array[$i]);

                $data = array();
                $data["IBYEAR"][TEXT]           = $model->year;
                $data["IBCLASSCD"][TEXT]        = $ibclasscd;
                $data["IBPRG_COURSE"][TEXT]     = $ibprg_course;
                $data["IBCURRICULUM_CD"][TEXT]  = $ibcurriculum_cd;
                $data["IBSUBCLASSCD"][TEXT]     = $ibsubclasscd;
                $data["REGISTERCD"][TEXT]       = STAFFCD;
                $data["UPDATED"][NUMBER]        = "sysdate()";

                $query = Query::insertSQL($data, "IBSUBCLASS_YDAT");
                $db->query($query);
            }
        } else {
            $model->year = "";
        }
        $db->commit();

        Query::dbCheckIn($db);
        return;
    }
}
?>
