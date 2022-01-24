<?php

require_once('for_php7.php');

class knjz405Query extends Query {
    //一覧取得（左画面）
    function getList($point_l_cd="") {
        $query  = " SELECT ";
        $query .= "     T1.POINT_L_CD, ";
        $query .= "     VALUE(T1.REMARK_L,'　') AS REMARK_L, ";
        $query .= "     T2.POINT_M_CD, ";
        $query .= "     VALUE(T2.REMARK_M,'　') AS REMARK_M ";
        $query .= " FROM ";
        $query .= "     JVIEW_POINT_L_MST T1 ";
        $query .= "     LEFT JOIN JVIEW_POINT_M_MST T2 ";
        $query .= "              ON T1.POINT_DIV    = T2.POINT_DIV ";
        $query .= "             AND T1.POINT_L_CD   = T2.POINT_L_CD ";
        $query .= " WHERE ";
        $query .= "     T1.POINT_DIV = '01' ";
        if ($point_l_cd) {
            $query .= " AND T1.POINT_L_CD = '".$point_l_cd."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     T1.POINT_L_CD, ";
        $query .= "     T2.POINT_M_CD ";

        return $query;
    }
        
    //１レコード取得 (右画面)
    function getRow($point_l_cd, $point_m_cd) {
        $query  = " SELECT ";
        $query .= "     T1.POINT_L_CD, ";
        $query .= "     T1.REMARK_L, ";
        $query .= "     T2.POINT_M_CD, ";
        $query .= "     T2.REMARK_M ";
        $query .= " FROM ";
        $query .= "     JVIEW_POINT_L_MST T1 ";
        $query .= "     LEFT JOIN JVIEW_POINT_M_MST T2 ";
        $query .= "              ON T1.POINT_DIV    = T2.POINT_DIV ";
        $query .= "             AND T1.POINT_L_CD   = T2.POINT_L_CD ";
        $query .= " WHERE ";
        $query .= "     T1.POINT_DIV    = '01' AND ";
        $query .= "     T1.POINT_L_CD   = '".$point_l_cd."' AND ";
        $query .= "     T2.POINT_M_CD   = '".$point_m_cd."' ";

        return $query;
    }

    //追加処理
    function &getInsertQuery($fields) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //存在チェック
        $checkL = $db->getOne(knjz405Query::checkJviewPointLMst($fields["POINT_L_CD"]));

        if ($checkL == 0) {
            //追加 -- JVIEW_POINT_L_MST
            $data = array();
            $data["POINT_DIV"][TEXT]    = '01';
            $data["POINT_L_CD"][TEXT]   = $fields["POINT_L_CD"];
            $data["REMARK_L"][TEXT]     = $fields["REMARK_L"];
            $data["REGISTERCD"][TEXT]   = STAFFCD;
            $data["UPDATED"][NUMBER]    = "sysdate()";

            $query = Query::insertSQL($data, "JVIEW_POINT_L_MST");
            $db->query($query);
        } else {
            //更新 -- JVIEW_POINT_L_MST
            $data = array();
            $data["REMARK_L"][TEXT]     = $fields["REMARK_L"];
            $data["REGISTERCD"][TEXT]   = STAFFCD;
            $data["UPDATED"][NUMBER]    = "sysdate()";

            $where  = " WHERE ";
            $where .= "  POINT_DIV      = '01' AND ";
            $where .= "  POINT_L_CD     = '".$fields["POINT_L_CD"]."' ";

            $query = Query::updateSQL($data, "JVIEW_POINT_L_MST", $where);
            $db->query($query);
        }

        //追加 -- JVIEW_POINT_M_MST
        $data = array();
        $data["POINT_DIV"][TEXT]    = '01';
        $data["POINT_L_CD"][TEXT]   = $fields["POINT_L_CD"];
        $data["POINT_M_CD"][TEXT]   = $fields["POINT_M_CD"];
        $data["REMARK_M"][TEXT]     = $fields["REMARK_M"];
        $data["REGISTERCD"][TEXT]   = STAFFCD;
        $data["UPDATED"][NUMBER]    = "sysdate()";

        $query = Query::insertSQL($data, "JVIEW_POINT_M_MST");
        $db->query($query);

        $db->commit();
        Query::dbCheckIn($db);
        return;
    }

    //更新処理
    function &getUpdateQuery($fields) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //更新 -- JVIEW_POINT_L_MST
        $data = array();
        $data["REMARK_L"][TEXT]     = $fields["REMARK_L"];
        $data["REGISTERCD"][TEXT]   = STAFFCD;
        $data["UPDATED"][NUMBER]    = "sysdate()";

        $where  = " WHERE ";
        $where .= "  POINT_DIV      = '01' AND ";
        $where .= "  POINT_L_CD     = '".$fields["POINT_L_CD"]."' ";

        $query = Query::updateSQL($data, "JVIEW_POINT_L_MST", $where);
        $db->query($query);

        //更新 -- JVIEW_POINT_M_MST
        $data = array();
        $data["REMARK_M"][TEXT]     = $fields["REMARK_M"];
        $data["REGISTERCD"][TEXT]   = STAFFCD;
        $data["UPDATED"][NUMBER]    = "sysdate()";

        $where  = " WHERE ";
        $where .= "  POINT_DIV      = '01' AND ";
        $where .= "  POINT_L_CD     = '".$fields["POINT_L_CD"]."' AND ";
        $where .= "  POINT_M_CD     = '".$fields["POINT_M_CD"]."' ";

        $query = Query::updateSQL($data, "JVIEW_POINT_M_MST", $where);
        $db->query($query);

        $db->commit();
        Query::dbCheckIn($db);
        return;
    }

    //削除処理
    function &getDeleteQuery($fields) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //存在チェック
        $checkM = $db->getOne(knjz405Query::checkJviewPointMMst($fields["POINT_L_CD"], $fields["POINT_M_CD"]));

        if ($checkM == 0) {
            //削除 -- JVIEW_POINT_L_MST
            $query  = " DELETE FROM ";
            $query .= "     JVIEW_POINT_L_MST ";
            $query .= " WHERE ";
            $query .= "     POINT_DIV   = '01' AND ";
            $query .= "     POINT_L_CD  = '".$fields["POINT_L_CD"]."' ";
            $db->query($query);
        }

        //削除 -- JVIEW_POINT_M_MST
        $query  = " DELETE FROM ";
        $query .= "     JVIEW_POINT_M_MST ";
        $query .= " WHERE ";
        $query .= "     POINT_DIV   = '01' AND ";
        $query .= "     POINT_L_CD  = '".$fields["POINT_L_CD"]."' AND ";
        $query .= "     POINT_M_CD  = '".$fields["POINT_M_CD"]."' ";
        $db->query($query);

        $db->commit();
        Query::dbCheckIn($db);

        return;
    }

    //存在チェック -- JVIEW_POINT_L_MST
    function checkJviewPointLMst($point_l_cd) {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     JVIEW_POINT_L_MST ";
        $query .= " WHERE ";
        $query .= "     POINT_DIV   = '01' AND ";
        $query .= "     POINT_L_CD  = '".$point_l_cd."' ";

        return $query;
    }

    //存在チェック -- JVIEW_POINT_M_MST
    function checkJviewPointMMst($point_l_cd, $point_m_cd) {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     JVIEW_POINT_M_MST ";
        $query .= " WHERE ";
        $query .= "     POINT_DIV   = '01' AND ";
        $query .= "     POINT_L_CD  = '".$point_l_cd."' AND ";
        $query .= "     POINT_M_CD != '".$point_m_cd."' ";

        return $query;
    }
}
?>
