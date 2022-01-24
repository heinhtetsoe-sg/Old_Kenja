<?php

require_once('for_php7.php');

class knjh187query extends Query {
    //対象幼稚園取得
    function getKindergarten() {
        $query  = " SELECT DISTINCT ";
        $query .= "     T1.COURSECD || '-' || T1.MAJORCD || '-' || T1.COURSECODE AS VALUE, ";
        $query .= "     T1.COURSECODE || ':' || VALUE(T2.COURSECODENAME,'') AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1 ";
        $query .= "     LEFT JOIN V_COURSECODE_MST T2 ";
        $query .= "              ON T1.YEAR         = T2.YEAR ";
        $query .= "             AND T1.COURSECODE   = T2.COURSECODE ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR     = '".CTRL_YEAR."' AND ";
        $query .= "     T1.SEMESTER = '".CTRL_SEMESTER."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //年組取得（権限チェック）
    function getHrClass($model) {
        $query  = " SELECT DISTINCT ";
        $query .= "     T1.GRADE || T1.HR_CLASS AS VALUE, ";
        $query .= "     T2.HR_NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1, ";
        $query .= "     SCHREG_REGD_HDAT T2 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR     = T2.YEAR AND ";
        $query .= "     T1.YEAR     = '".CTRL_YEAR."' AND ";
        $query .= "     T1.SEMESTER = T2.SEMESTER AND ";
        $query .= "     T1.SEMESTER = '".CTRL_SEMESTER."' AND ";
        $query .= "     T1.GRADE    = T2.GRADE AND ";
        $query .= "     T1.HR_CLASS = T2.HR_CLASS ";
        if ($model->field["COURSECODE"] != "ALL") {
            $query .= " AND T1.COURSECD || '-' || T1.MAJORCD || '-' || T1.COURSECODE = '".$model->field["COURSECODE"]."' ";
        }
        if (AUTHORITY == DEF_REFER_RESTRICT || AUTHORITY == DEF_UPDATE_RESTRICT) {
            $query .= " AND '".STAFFCD."' IN (TR_CD1, TR_CD2, TR_CD3, SUBTR_CD1, SUBTR_CD2, SUBTR_CD3) ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //バスコース取得
    function getBusCourse() {
        $query  = " SELECT ";
        $query .= "     COURSE_CD AS VALUE, ";
        $query .= "     BUS_NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     CHILDCARE_BUS_YMST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //全体のデータ取得
    function selectQuery($model) {
        $query  = " WITH SCH_INFO AS ( ";
        $query .= "     SELECT DISTINCT ";
        $query .= "         T1.SCHREGNO, ";
        $query .= "         T1.GRADE, ";
        $query .= "         T1.HR_CLASS, ";
        $query .= "         T1.ATTENDNO, ";
        $query .= "         L1.HR_NAME, ";
        $query .= "         L2.NAME_SHOW ";
        $query .= "     FROM ";
        $query .= "         SCHREG_REGD_DAT T1 ";
        $query .= "         LEFT JOIN SCHREG_REGD_HDAT L1 ";
        $query .= "                  ON T1.YEAR     = L1.YEAR ";
        $query .= "                 AND T1.SEMESTER = L1.SEMESTER ";
        $query .= "                 AND T1.GRADE    = L1.GRADE ";
        $query .= "                 AND T1.HR_CLASS = L1.HR_CLASS ";
        $query .= "         LEFT JOIN SCHREG_BASE_MST L2 ON T1.SCHREGNO = L2.SCHREGNO, ";
        $query .= "         CHILDCARE_DAT T2 ";
        $query .= "     WHERE ";
        $query .= "         T1.YEAR     = T2.YEAR AND ";
        $query .= "         T1.YEAR     = '".CTRL_YEAR."' AND ";
        $query .= "         T1.SEMESTER = '".CTRL_SEMESTER."' AND ";
        $query .= "         T1.SCHREGNO = T2.SCHREGNO ";
        if (AUTHORITY == DEF_REFER_RESTRICT || AUTHORITY == DEF_UPDATE_RESTRICT) {
            $query .= " AND '".STAFFCD."' IN (L1.TR_CD1, L1.TR_CD2, L1.TR_CD3, L1.SUBTR_CD1, L1.SUBTR_CD2, L1.SUBTR_CD3) ";
        }
        if ($model->field["COURSECODE"] != "ALL") {
            $query .= " AND T1.COURSECD || '-' || T1.MAJORCD || '-' || T1.COURSECODE = '".$model->field["COURSECODE"]."' ";
        }
        if ($model->field["GRADE_HR_CLASS"] != "ALL") {
            $query .= " AND T1.GRADE || T1.HR_CLASS = '".$model->field["GRADE_HR_CLASS"]."' ";
        }
        $query .= " ) ";

        $query .= " SELECT ";
        $query .= "     T1.*, ";
        $query .= "     L1.JOSYA_2, ";
        $query .= "     L1.GESYA_2, ";
        $query .= "     L1.ROSEN_2, ";
        $query .= "     L2.BUS_NAME ";
        $query .= " FROM ";
        $query .= "     SCH_INFO T1 ";
        $query .= "     LEFT JOIN SCHREG_ENVIR_DAT L1 ON T1.SCHREGNO = L1.SCHREGNO AND L1.HOWTOCOMMUTECD = '1' ";
        $query .= "     LEFT JOIN CHILDCARE_BUS_YMST L2 ON L2.YEAR = '".CTRL_YEAR."' AND L2.COURSE_CD = L1.ROSEN_2 ";
        $query .= " ORDER BY ";
        $query .= "     T1.GRADE, ";
        $query .= "     T1.HR_CLASS, ";
        $query .= "     T1.ATTENDNO ";

        return $query;
    }

    //更新処理
    function updateQuery($model) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        for ($i = 0; $i < get_count($model->data["SCHREGNO"]); $i++) {

            //存在チェック
            $cnt = $db->getOne(knjh187Query::checkExists($model->data["SCHREGNO"][$i]));

            if ($model->fields["JOSYA_2"][$i] != "" && $model->fields["ROSEN_2"][$i] != "" && $model->fields["GESYA_2"][$i] != "") {

                $data = array();
                if (0 == $cnt) $data["SCHREGNO"][TEXT]  = $model->data["SCHREGNO"][$i];
                $data["HOWTOCOMMUTECD"][TEXT]           = 1;
                $data["JOSYA_2"][TEXT]                  = $model->fields["JOSYA_2"][$i];
                $data["ROSEN_2"][TEXT]                  = $model->fields["ROSEN_2"][$i];
                $data["GESYA_2"][TEXT]                  = $model->fields["GESYA_2"][$i];
                $data["FLG_2"][TEXT]                    = 3;
                $data["REGISTERCD"][TEXT]               = STAFFCD ;
                $data["UPDATED"][FUNC]                  = "sysdate()";

                if (0 < $cnt) {
                    //更新
                    $where  = " WHERE ";
                    $where .= "     SCHREGNO    = '".$model->data["SCHREGNO"][$i]."' ";
                    $query = Query::updateSQL($data, "SCHREG_ENVIR_DAT", $where);
                } else {
                    //追加
                    $query = Query::insertSQL($data, "SCHREG_ENVIR_DAT");
                }
                $db->query($query);

            } else if ($model->fields["JOSYA_2"][$i] == "" && $model->fields["ROSEN_2"][$i] == "" && $model->fields["GESYA_2"][$i] == "") {

                if (0 < $cnt) {
                    $data = array();
                    $data["JOSYA_2"][TEXT]          = "";
                    $data["ROSEN_2"][TEXT]          = "";
                    $data["GESYA_2"][TEXT]          = "";
                    $data["FLG_2"][TEXT]            = "";
                    $data["REGISTERCD"][TEXT]       = STAFFCD ;
                    $data["UPDATED"][FUNC]          = "sysdate()";

                    //更新
                    $where  = " WHERE ";
                    $where .= "     SCHREGNO    = '".$model->data["SCHREGNO"][$i]."' ";
                    $query = Query::updateSQL($data, "SCHREG_ENVIR_DAT", $where);
                    $db->query($query);
                }
            }
        }

        $db->commit();
        Query::dbCheckIn($db);
    }

    //存在チェック
    function checkExists($schregno) {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     SCHREG_ENVIR_DAT ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO    = '".$schregno."' ";

        return $query;
    }
}
?>
