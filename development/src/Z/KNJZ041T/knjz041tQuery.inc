<?php

require_once('for_php7.php');

class knjz041tQuery extends Query {
    //現在の年度のデータが無くなった時に次年度以降の値を取得
    function DeleteAtExist($model)
    {
        $query  = " SELECT MIN(ENTEXAMYEAR) ";
        $query .= " FROM ENTEXAM_COURSE_MST ";
        $query .= " WHERE ENTEXAMYEAR >= '" .$model->year. "'";
        return $query;
    }

    //データ取得
    function selectQuery() {
        $query  = " SELECT ";
        $query .= "     T1.MAJORLCD, ";
        $query .= "     T1.MAJORLNAME, ";
        $query .= "     T1.MAJORLABBV, ";
        $query .= "     L1.MAJORSCD, ";
        $query .= "     L1.MAJORSNAME, ";
        $query .= "     L1.MAJORSABBV, ";
        $query .= "     L1.MAIN_MAJORCD ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_MAJORL_MST T1 ";
        $query .= " LEFT JOIN ";
        $query .= "     ENTEXAM_MAJORS_MST L1 ON L1.MAJORLCD = T1.MAJORLCD ";
        $query .= " ORDER BY ";
        $query .= "     T1.MAJORLCD, ";
        $query .= "     L1.MAJORSCD ";

        return $query;
    }

    //入試制度
    function selectApplicantdiv($year) {
        $query  = "  SELECT";
        $query .= "      NAMECD2, ";
        $query .= "      NAME1 ";
        $query .= "  FROM";
        $query .= "      NAME_MST ";
        $query .= "  WHERE ";
        $query .= "      NAMECD1 = 'L003' ";
        $query .= "  ORDER BY ";
        $query .= "      NAMECD2 ";
        return $query;
    }

    //課程学科
    function getMajorcd() {
        $query  = " SELECT ";
        $query .= "     MAJORCD || ' ' || MAJORNAME AS LABEL, ";
        $query .= "     MAJORCD AS VALUE ";
        $query .= " FROM ";
        $query .= "     MAJOR_MST ";

        return $query;
    }

    //１レコード取得
    function getRow($majorlcd,$majorscd) {
        $query .= " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     V_ENTEXAM_MAJOR_MST ";
        $query .= " WHERE ";
        $query .= "     MAJORCD = '{$majorlcd}{$majorscd}' ";

        return $query;
    }

    function getRow_S($majorlcd, $majorscd) {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_MAJORS_MST ";
        $query .= " WHERE ";
        $query .= "     MAJORLCD = '{$majorlcd}' AND ";
        $query .= "     MAJORSCD = '{$majorscd}' ";

        return $query;
    }

    function getRow_L($majorlcd) {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_MAJORL_MST ";
        $query .= " WHERE ";
        $query .= "     MAJORLCD = '{$majorlcd}' ";

        return $query;
    }

    //INSERT
    function &getInsertQuery_L($fields) {
        $db = Query::dbCheckOut();

        //ENTEXAM_MAJORL_MST
        $data["MAJORLCD"][TEXT]     = $fields["MAJORLCD"];
        $data["MAJORLNAME"][TEXT]   = $fields["MAJORLNAME"];
        $data["MAJORLABBV"][TEXT]   = $fields["MAJORLABBV"];
        $data["REGISTERCD"][TEXT]   = STAFFCD;
        $data["UPDATED"][FUNC]      = "sysdate()";

        $query = Query::insertSQL($data, "ENTEXAM_MAJORL_MST");

        $db->query($query);
        Query::dbCheckIn($db);
        return;
    }
    //INSERT
    function &getInsertQuery_S($fields) {
        $db = Query::dbCheckOut();

        //ENTEXAM_MAJORS_MST
        $data["MAJORLCD"][TEXT]     = $fields["MAJORLCD"];
        $data["MAJORSCD"][TEXT]     = $fields["MAJORSCD"];
        $data["MAJORSNAME"][TEXT]   = $fields["MAJORSNAME"];
        $data["MAJORSABBV"][TEXT]   = $fields["MAJORSABBV"];
        $data["MAIN_MAJORCD"][TEXT] = $fields["MAIN_MAJORCD"];
        $data["REGISTERCD"][TEXT]   = STAFFCD;
        $data["UPDATED"][FUNC]      = "sysdate()";

        $query = Query::insertSQL($data, "ENTEXAM_MAJORS_MST");

        $db->query($query);

        Query::dbCheckIn($db);
        return;
    }

    //UPDATE
    function &getUpdateQuery_L($fields) {
        $db = Query::dbCheckOut();

        //ENTEXAM_MAJORL_MST
        $data["MAJORLCD"][TEXT]     = $fields["MAJORLCD"];
        $data["MAJORLNAME"][TEXT]   = $fields["MAJORLNAME"];
        $data["MAJORLABBV"][TEXT]   = $fields["MAJORLABBV"];
        $data["REGISTERCD"][TEXT]   = STAFFCD;
        $data["UPDATED"][FUNC]      = "sysdate()";

        $where  = " WHERE MAJORLCD  = '{$fields["MAJORLCD"]}'";

        $query = Query::updateSQL($data, "ENTEXAM_MAJORL_MST", $where);

        $db->query($query);
        Query::dbCheckIn($db);
        return;
    }


    //UPDATE
    function &getUpdateQuery_S($fields) {
        $db = Query::dbCheckOut();

        //ENTEXAM_MAJORS_MST
        $data["MAJORLCD"][TEXT]     = $fields["MAJORLCD"];
        $data["MAJORSCD"][TEXT]     = $fields["MAJORSCD"];
        $data["MAJORSNAME"][TEXT]   = $fields["MAJORSNAME"];
        $data["MAJORSABBV"][TEXT]   = $fields["MAJORSABBV"];
        $data["MAIN_MAJORCD"][TEXT] = $fields["MAIN_MAJORCD"];
        $data["REGISTERCD"][TEXT]   = STAFFCD;
        $data["UPDATED"][FUNC]      = "sysdate()";

        $where  = " WHERE MAJORLCD   = '{$fields["MAJORLCD"]}'";
        $where .= "   AND MAJORSCD   = '{$fields["MAJORSCD"]}'";

        $query = Query::updateSQL($data, "ENTEXAM_MAJORS_MST", $where);

        $db->query($query);
        Query::dbCheckIn($db);
        return;
    }

    //DELETE
    function &getDeleteQuery($fields) {
        $db = Query::dbCheckOut();

        $query  = " DELETE FROM ENTEXAM_MAJORS_MST ";
        $query .= "       WHERE MAJORLCD   = '{$fields["MAJORLCD"]}'";
        $query .= "         AND MAJORSCD   = '{$fields["MAJORSCD"]}'";

        $db->query($query);

        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_MAJORS_MST ";
        $query .= " WHERE ";
        $query .= "     MAJORLCD = '{$fields["MAJORLCD"]}' ";

        $majors_cnt = $db->getOne($query);

        if ($majors_cnt == 0) {
            $query  = " DELETE FROM ENTEXAM_MAJORL_MST ";
            $query .= "       WHERE MAJORLCD  = '{$fields["MAJORLCD"]}'";

            $db->query($query);
        }

        Query::dbCheckIn($db);

        return $result;
    }
}
?>
