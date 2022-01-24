<?php

require_once('for_php7.php');

class knjj010_3Query extends Query
{

    //クラブ年度取得
    public function getYear($model)
    {
        $query  = " WITH THIS_NEXT_YEAR(VALUE, LABEL) AS ( ";
        $query .= "     VALUES('".CTRL_YEAR."', '".CTRL_YEAR."') ";
        $query .= "     UNION ";
        $query .= "     VALUES('".(CTRL_YEAR + 1)."', '".(CTRL_YEAR + 1)."') ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     THIS_NEXT_YEAR ";
        $query .= " UNION ";
        $query .= " SELECT ";
        $query .= "     YEAR AS VALUE, ";
        $query .= "     YEAR AS LABEL ";
        $query .= " FROM ";
        $query .= "     CLUB_YDAT ";
        $query .= " WHERE ";
        $query .= "     SCHOOLCD = '".SCHOOLCD."' ";
        $query .= "     AND SCHOOL_KIND = '".SCHOOLKIND."' ";
        $query .= " UNION ";
        $query .= " SELECT ";
        $query .= "     YEAR AS VALUE, ";
        $query .= "     YEAR AS LABEL ";
        $query .= " FROM ";
        $query .= "     CLUB_DETAIL_DAT ";
        $query .= " WHERE ";
        $query .= "     SCHOOLCD = '".SCHOOLCD."' ";
        $query .= "     AND SCHOOL_KIND = '".SCHOOLKIND."' ";
        $query .= "     AND SEQ = '001' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE DESC ";

        return $query;
    }

    //コピー元年度
    public function getCopyYear($model)
    {
        $query  = " SELECT ";
        $query .= "     YEAR AS VALUE, ";
        $query .= "     YEAR AS LABEL ";
        $query .= " FROM ";
        $query .= "     CLUB_DETAIL_DAT ";
        $query .= " WHERE ";
        $query .= "     SCHOOLCD = '".SCHOOLCD."' ";
        $query .= "     AND SCHOOL_KIND = '".SCHOOLKIND."' ";
        $query .= "     AND SEQ = '001' ";
        $query .= " GROUP BY ";
        $query .= "     YEAR ";
        $query .= " ORDER BY ";
        $query .= "     VALUE DESC ";

        return $query;
    }

    //クラブ左リスト
    public function getClubMstYear($model)
    {
        $query .= " SELECT ";
        $query .= "     CM.*, ";
        $query .= "     CDETAIL.REMARK1 ";
        $query .= " FROM ";
        $query .= "     CLUB_MST CM ";
        $query .= "     INNER JOIN CLUB_YDAT CY ON CM.SCHOOLCD = CY.SCHOOLCD ";
        $query .= "          AND CM.SCHOOL_KIND = CY.SCHOOL_KIND ";
        $query .= "          AND CY.YEAR = '{$model->year}' ";
        $query .= "          AND CM.CLUBCD = CY.CLUBCD ";
        $query .= "     LEFT JOIN CLUB_DETAIL_DAT CDETAIL ON CDETAIL.YEAR = '{$model->year}' ";
        $query .= "          AND CM.SCHOOLCD = CDETAIL.SCHOOLCD ";
        $query .= "          AND CM.SCHOOL_KIND = CDETAIL.SCHOOL_KIND ";
        $query .= "          AND CM.CLUBCD = CDETAIL.CLUBCD ";
        $query .= "          AND CDETAIL.SEQ = '001' ";
        $query .= " WHERE ";
        $query .= "     CM.SCHOOLCD = '".SCHOOLCD."' ";
        $query .= "     AND CM.SCHOOL_KIND = '".SCHOOLKIND."' ";
        $query .= " ORDER BY ";
        $query .= "     CM.CLUBCD ";

        return $query;
    }

    //校種取得
    public function getSchkind()
    {
        $query  = " SELECT ";
        $query .= "     NAME1 AS VALUE, ";
        $query .= "     ABBV1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = 'A023' ";
        $query .= " ORDER BY ";
        $query .= "     NAME1 ";

        return $query;
    }

    //クラブ校種取得
    public function getClubDetail($model)
    {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     CLUB_DETAIL_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$model->year}' ";
        $query .= "     AND CLUBCD = '{$model->clubcd}' ";
        $query .= "     AND SCHOOLCD    = '".SCHOOLCD."' ";
        $query .= "     AND SCHOOL_KIND = '".SCHOOLKIND."' ";
        $query .= "     AND SEQ = '001' ";

        return $query;
    }

    //クラブ左リスト
    public function getClubMst($model)
    {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     CLUB_MST ";
        $query .= " WHERE ";
        $query .= "     SCHOOLCD = '".SCHOOLCD."'";
        $query .= "     AND SCHOOL_KIND = '".SCHOOLKIND."'";
        $query .= " ORDER BY ";
        $query .= "     CLUBCD";

        return $query;
    }

    //１レコード取得
    public function getOneRecord($model, $cd)
    {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     CLUB_MST ";
        $query .= " WHERE ";
        $query .= "     CLUBCD = '" .$cd ."'";
        $query .= "     AND SCHOOLCD    = '".SCHOOLCD."' ";
        $query .= "     AND SCHOOL_KIND = '".SCHOOLKIND."' ";

        return $query;
    }

    //--- UPDATE
    public function getUpdateQuery($model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $query = knjj010_3Query::getClubDetail($model);
        $backDetail = $db->getRow($query, DB_FETCHMODE_ASSOC);

        //削除
        $query  = " DELETE FROM CLUB_DETAIL_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$model->year}' ";
        $query .= "     AND CLUBCD = '{$model->clubcd}' ";
        $query .= "     AND SCHOOLCD    = '".SCHOOLCD."' ";
        $query .= "     AND SCHOOL_KIND = '".SCHOOLKIND."' ";
        $query .= "     AND SEQ = '001' ";
        $db->query($query);

        $setRemark1 = "";
        $remarkSep = "";
        foreach ($this->selectData as $key => $val) {
            $setRemark1 .= $remarkSep.$val;
            $remarkSep = ":";
        }

        $data["YEAR"][TEXT]         = $model->year;
        $data["SCHOOLCD"][TEXT]     = SCHOOLCD;
        $data["SCHOOL_KIND"][TEXT]  = SCHOOLKIND;
        $data["CLUBCD"][TEXT]       = $model->clubcd;
        $data["SEQ"][TEXT]          = "001";
        $data["REMARK1"][TEXT]      = $setRemark1;
        $data["REMARK2"][TEXT]      = $backDetail["REMARK2"];
        $data["REMARK3"][TEXT]      = $backDetail["REMARK3"];
        $data["REMARK4"][TEXT]      = $backDetail["REMARK4"];
        $data["REMARK5"][TEXT]      = $backDetail["REMARK5"];
        $data["REMARK6"][TEXT]      = $backDetail["REMARK6"];
        $data["REMARK7"][TEXT]      = $backDetail["REMARK7"];
        $data["REMARK8"][TEXT]      = $backDetail["REMARK8"];
        $data["REMARK9"][TEXT]      = $backDetail["REMARK9"];
        $data["REMARK10"][TEXT]     = $backDetail["REMARK10"];
        $data["REGISTERCD"][TEXT]   = STAFFCD;
        $data["UPDATED"][FUNC]      = "sysdate()";

        $query = Query::insertSQL($data, "CLUB_DETAIL_DAT");
        $db->query($query);

        $db->commit();
        Query::dbCheckIn($db);
        return;
    }

    //--- COPY
    public function getCopyQuery($model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //削除
        $query  = " DELETE FROM CLUB_DETAIL_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$model->year}' ";
        $query .= "     AND SCHOOLCD    = '".SCHOOLCD."' ";
        $query .= "     AND SCHOOL_KIND = '".SCHOOLKIND."' ";
        $query .= "     AND SEQ = '001' ";
        $db->query($query);

        $query  = " INSERT INTO CLUB_DETAIL_DAT ";
        $query .= " SELECT ";
        $query .= "     '{$model->year}' AS YEAR, ";
        $query .= "     SCHOOLCD, ";
        $query .= "     SCHOOL_KIND, ";
        $query .= "     CLUBCD, ";
        $query .= "     SEQ, ";
        $query .= "     REMARK1, ";
        $query .= "     REMARK2, ";
        $query .= "     REMARK3, ";
        $query .= "     REMARK4, ";
        $query .= "     REMARK5, ";
        $query .= "     REMARK6, ";
        $query .= "     REMARK7, ";
        $query .= "     REMARK8, ";
        $query .= "     REMARK9, ";
        $query .= "     REMARK10, ";
        $query .= "     '".STAFFCD."', ";
        $query .= "     sysdate() ";
        $query .= " FROM ";
        $query .= "     CLUB_DETAIL_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$model->copyYear}' ";
        $query .= "     AND SCHOOLCD    = '".SCHOOLCD."' ";
        $query .= "     AND SCHOOL_KIND = '".SCHOOLKIND."' ";
        $query .= "     AND SEQ = '001' ";
        $db->query($query);

        $db->commit();
        Query::dbCheckIn($db);
        return;
    }
}
