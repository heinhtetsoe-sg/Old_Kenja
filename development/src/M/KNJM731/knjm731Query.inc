<?php

require_once('for_php7.php');

class knjm731Query extends Query {

    //クラス一覧取得
    function getHrClassList() {
        $query  = " SELECT DISTINCT ";
        $query .= "     HR_NAME AS LABEL, ";
        $query .= "     GRADE || '-' || HR_CLASS AS VALUE ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_HDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR        = '".CTRL_YEAR."' AND ";
        $query .= "     SEMESTER    = '".CTRL_SEMESTER."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //データ一覧取得
    function getMainQuery($model) {

        if ($model->field["SEARCH_DIV"] == "2") {
            $query  = " SELECT ";
            $query .= "     T1.SCHREGNO, ";
            $query .= "     T2.NAME_SHOW, ";
            $query .= "     T3.HR_NAME || '-' || T1.ATTENDNO AS ATTENDNO, ";
            $query .= "     T2.MUSYOU_KAISU ";
            $query .= " FROM ";
            $query .= "     SCHREG_REGD_DAT T1 ";
            $query .= "     LEFT JOIN V_SCHREG_BASE_MST T2 ON T1.SCHREGNO   = T2.SCHREGNO ";
            $query .= "     LEFT JOIN SCHREG_REGD_HDAT T3  ON T1.YEAR       = T3.YEAR AND ";
            $query .= "                                       T1.SEMESTER   = T3.SEMESTER AND ";
            $query .= "                                       T1.GRADE      = T3.GRADE AND ";
            $query .= "                                       T1.HR_CLASS   = T3.HR_CLASS ";
            $query .= " WHERE ";
            $query .= "     T1.YEAR     = '".CTRL_YEAR."' AND ";
            $query .= "     T1.SEMESTER = '".CTRL_SEMESTER."' AND ";
            $query .= "     T1.GRADE || '-' || T1.HR_CLASS = '".$model->field["GRADE_HR_CLASS"]."' ";
            $query .= " ORDER BY ";
            $query .= "     T1.SCHREGNO ";
        } else {
            $query  = " SELECT ";
            $query .= "     T1.SCHREGNO, ";
            $query .= "     T1.NAME AS NAME_SHOW, ";
            $query .= "     T2.BASE_REMARK4 AS MUSYOU_KAISU ";
            $query .= " FROM ";
            $query .= "     FRESHMAN_DAT T1 ";
            $query .= "     LEFT JOIN SCHREG_BASE_DETAIL_MST T2 ON T1.SCHREGNO = T2.SCHREGNO AND ";
            $query .= "                                            T2.BASE_SEQ = '004' ";
            $query .= " WHERE ";
            $query .= "     T1.ENTERYEAR = '".(CTRL_YEAR+1)."' ";
            $query .= " ORDER BY ";
            $query .= "     T1.SCHREGNO ";
        }

        return $query;
    }


    //SCHREG_BASE_DETAIL_MST存在チェック
    function checkSchregBaseDetailDat($schregno) {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     SCHREG_BASE_DETAIL_MST ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO = '".$schregno."' AND ";
        $query .= "     BASE_SEQ = '004' ";

        return $query;
    }

    //UPDATE
    function &getDetailUpdateQuery($db, $schregno, $musyou_kaisu) {

        $db->autoCommit(false);

        $cnt = $db->getOne(knjm731Query::checkSchregBaseDetailDat($schregno));

        $data["BASE_REMARK4"][TEXT]     = $musyou_kaisu;
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][NUMBER]        = "sysdate()";

        if ($cnt > 0) {
            $where  = " WHERE ";
            $where .= "     SCHREGNO    = '".$schregno."' AND ";
            $where .= "     BASE_SEQ    = '004' ";

            $query = Query::updateSQL($data, "SCHREG_BASE_DETAIL_MST", $where);
            $db->query($query);
        } else if ($cnt == "0" && $musyou_kaisu != "") {
            $data["SCHREGNO"][TEXT]           = $schregno;
            $data["BASE_SEQ"][TEXT]           = '004';

            $query = Query::insertSQL($data, "SCHREG_BASE_DETAIL_MST");
            $db->query($query);
        }

        $db->commit();
        return;
    }
}
?>
