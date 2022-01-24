<?php

require_once('for_php7.php');

class knjh342Query extends Query {

    //年組一覧
    function getGrade($model)
    {
        $query  = "SELECT DISTINCT ";
        $query .= "    GRADE AS VALUE, ";
        $query .= "    GRADE_NAME1 AS LABEL ";
        $query .= "FROM ";
        $query .= "    SCHREG_REGD_GDAT ";
        $query .= "WHERE ";
        $query .= "    YEAR = '".CTRL_YEAR."' ";
        $query .= "ORDER BY ";
        $query .= "    VALUE ";

        return $query;
    }

    //模試種別
    function getMockDiv()
    {
        $query  = " SELECT ";
        $query .= "     NAMECD2 AS VALUE, ";
        $query .= "     NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND NAMECD1 = 'H308' ";
        $query .= "     AND NAMESPARE1 = '1' ";
        $query .= " ORDER BY ";
        $query .= "     NAMECD2 ";

        return $query;
    }

    //模試、実力テスト
    function getMockMst($target)
    {
        $query  = "SELECT ";
        $query .= "    MOCKCD AS VALUE, ";
        $query .= "    MOCKCD || ':' || MOCKNAME1 AS LABEL ";
        $query .= "FROM ";
        $query .= "    MOCK_MST ";
        $query .= "WHERE ";
        $query .= "    substr(MOCKCD, 1, 1) = '".$target."' ";
        $query .= "ORDER BY ";
        $query .= "    VALUE ";

        return $query;
    }

    /* 出欠月別累積データ・出欠累積データ・出欠科目別累積データ・学籍異動データ */
    function selectMainData($model)
    {

        $query  = " SELECT ";
        $query .= "     T1.PREF_SUBCLASSCD, ";
        $query .= "     T1.SUBCLASS_NAME, ";
        $query .= "     L1.AVG ";
        $query .= " FROM ";
        $query .= "     MOCK_PREF_SUBCLASS_MST T1 ";
        $query .= "     LEFT JOIN MOCK_PREF_AVG_DAT L1 ON L1.YEAR = '".CTRL_YEAR."' ";
        $query .= "          AND L1.MOCKCD = '".$model->field["MOCK_TARGET"]."' ";
        $query .= "          AND L1.GRADE = '".$model->field["GRADE"]."' ";
        $query .= "          AND L1.PREF_SUBCLASSCD = T1.PREF_SUBCLASSCD ";
        $query .= " ORDER BY ";
        $query .= "     T1.PREF_SUBCLASSCD ";

        return $query;
    }

    /* 模試データの更新 */
    function getUpdateQuery($model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $query = "DELETE FROM MOCK_PREF_AVG_DAT WHERE YEAR = '".CTRL_YEAR."' AND MOCKCD = '".$model->field["MOCK_TARGET"]."' AND GRADE = '".$model->field["GRADE"]."' ";
        $db->query($query);

        foreach($model->setData as $subclassCd => $val){
            if (strlen($val["AVG"]) > 0) {
                $data = array();
                $data["YEAR"][TEXT]             = CTRL_YEAR;
                $data["MOCKCD"][TEXT]           = $model->field["MOCK_TARGET"];
                $data["GRADE"][TEXT]            = $model->field["GRADE"];
                $data["PREF_SUBCLASSCD"][TEXT]  = $subclassCd;
                $data["AVG"][NUMBER]            = $val["AVG"];
                $data["REGISTERCD"][TEXT] = STAFFCD ;
                $data["UPDATED"][FUNC]    = "sysdate()";

                $query = Query::insertSQL($data, "MOCK_PREF_AVG_DAT");

                $db->query($query);
            }
        }
        $db->commit();
        Query::dbCheckIn($db);
        return true;
    }

}
?>
