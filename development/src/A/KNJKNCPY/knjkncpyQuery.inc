<?php

require_once('for_php7.php');

class knjkncpyQuery extends Query {

    //１レコード取得
    function getAttestWk($div)
    {
        $query  = " SELECT ";
        $query .= "     YEAR, ";
        $query .= "     SCHREGNO, ";
        if ($div == "CHAGE") {
            $query .= "     CHAGE_OPI_SEQ AS SEQ ";
        } else {
            $query .= "     LAST_OPI_SEQ AS SEQ ";
        }
        $query .= " FROM ";
        $query .= "     ATTEST_OPINIONS_WK ";
        $query .= " WHERE ";
        if ($div == "CHAGE") {
            $query .= "     CHAGE_OPI_SEQ IS NOT NULL ";
        } else {
            $query .= "     LAST_OPI_SEQ IS NOT NULL ";
        }

        return $query;
    }

    //１レコード取得
    function getTrainRow($setData)
    {
        $query  = " SELECT ";
        $query .= "     TOTALSTUDYACT, ";
        $query .= "     TOTALSTUDYVAL, ";
        $query .= "     SPECIALACTREMARK, ";
        $query .= "     TOTALREMARK, ";
        $query .= "     ATTENDREC_REMARK ";
        $query .= " FROM ";
        $query .= "     HTRAINREMARK_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$setData["YEAR"]."'";
        $query .= "     AND SCHREGNO = '".$setData["SCHREGNO"]."'";

        return $query;
    }

    //学習記録データ取得
    function getStudyRec($setData) {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     SCHREG_STUDYREC_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR     = '".$setData["YEAR"]."' AND ";
        $query .= "     SCHREGNO = '".$setData["SCHREGNO"]."' ";

        return $query;
    }

    //出欠記録データ取得
    function getAttendRec($setData) {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     SCHREG_ATTENDREC_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR     = '".$setData["YEAR"]."' AND ";
        $query .= "     SCHREGNO = '".$setData["SCHREGNO"]."' ";

        return $query;
    }

    //更新処理
    function &getUpdSql($setData, $opinion) {

        $data["OPINION"][TEXT]     = $opinion;   //ハッシュ値
        $data["REGISTERCD"][TEXT]  = STAFFCD;
        $data["UPDATED"][FUNC]     = "sysdate()";

        $where  = " WHERE ";
        $where .= "     YEAR = '".$setData["YEAR"]."' ";
        $where .= "     AND SEQ = ".$setData["SEQ"]." ";
        $query = Query::updateSQL($data, "ATTEST_OPINIONS_DAT", $where);

        return $query;
    }

}
?>
