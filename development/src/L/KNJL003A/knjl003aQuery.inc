<?php
class knjl003aQuery extends Query
{

    //入試年度一覧取得
    public function selectYearQuery()
    {
        $query  = " WITH T_ALL (VALUE, LABEL) AS ( ";
        $query .= "      VALUES('".(CTRL_YEAR + 1)."', '".(CTRL_YEAR + 1)."') ";
        $query .= "      ) ";
        $query .= " SELECT ";
        $query .= "      VALUE, ";
        $query .= "      LABEL ";
        $query .= " FROM ";
        $query .= "      T_ALL ";
        $query .= " UNION ";
        $query .= " SELECT DISTINCT ";
        $query .= "     ENTEXAMYEAR AS VALUE, ";
        $query .= "     ENTEXAMYEAR AS LABEL ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_HONORDIV_MST ";
        $query .= " ORDER BY ";
        $query .= "     VALUE DESC ";

        return $query;
    }

    //名称マスタより取得
    public function getNameMst($model, $namecd1, $nameCd2 = "")
    {
        $query  = " SELECT ";
        if ($nameCd2) {
            $query .= "     NAME1 ";
        } else {
            $query .= "     NAMECD2 AS VALUE, ";
            $query .= "     NAMECD2 || ':' || NAME1 AS LABEL, ";
            $query .= "     NAMESPARE2 ";
        }
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "         YEAR    = '".$model->year."' ";
        $query .= "     AND NAMECD1 = '".$namecd1."' ";
        if ($nameCd2) {
            $query .= "     AND NAMECD2 = '".$nameCd2."' ";
        } else {
            $query .= " ORDER BY ";
            $query .= "     VALUE ";
        }

        return $query;
    }

    //通知書用区分1取得
    public function getNoticeClass()
    {
        $query  = " WITH T_ALL (CD, NAME) AS ( ";
        $query .= "     VALUES('1', '第1種') ";
        $query .= "     UNION ALL ";
        $query .= "     VALUES('2', '第2種') ";
        $query .= "     UNION ALL ";
        $query .= "     VALUES('3', '第3種') ";
        $query .= "     UNION ALL ";
        $query .= "     VALUES('4', '第4種') ";
        $query .= "     ) ";
        $query .= " SELECT ";
        $query .= "     CD AS VALUE, ";
        $query .= "     CD || ':' || NAME AS LABEL, ";
        $query .= "     NAME ";
        $query .= " FROM ";
        $query .= "     T_ALL ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //通知書用区分2取得
    public function getNoticeKind()
    {
        $query  = " WITH T_ALL (CD, NAME) AS ( ";
        $query .= "     VALUES('1', '学力') ";
        $query .= "     UNION ALL ";
        $query .= "     VALUES('2', 'ファミリー') ";
        $query .= "     UNION ALL ";
        $query .= "     VALUES('3', '英検') ";
        $query .= "     UNION ALL ";
        $query .= "     VALUES('4', '資格') ";
        $query .= "     UNION ALL ";
        $query .= "     VALUES('5', 'クラブ') ";
        $query .= "     ) ";
        $query .= " SELECT ";
        $query .= "     CD AS VALUE, ";
        $query .= "     CD || ':' || NAME AS LABEL, ";
        $query .= "     NAME ";
        $query .= " FROM ";
        $query .= "     T_ALL ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //リスト取得
    public function selectQuery($model)
    {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_HONORDIV_MST TEST ";
        $query .= " WHERE ";
        $query .= "         TEST.ENTEXAMYEAR  = '".$model->year."' ";
        $query .= "     AND TEST.APPLICANTDIV = '".$model->applicantdiv."' ";
        $query .= " ORDER BY ";
        $query .= "     TEST.HONORDIV ";

        return $query;
    }

    //１レコード取得
    public function getRow($year, $applicantdiv, $honordiv)
    {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_HONORDIV_MST ";
        $query .= " WHERE ";
        $query .= "         ENTEXAMYEAR  = '".$year."' ";
        $query .= "     AND APPLICANTDIV = '".$applicantdiv."' ";
        $query .= "     AND HONORDIV     = '".$honordiv."' ";

        return $query;
    }

    public function chkUniqPriority($fields)
    {
        $db = Query::dbCheckOut();
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_HONORDIV_MST ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR      = '{$fields["ENTEXAMYEAR"]}' ";
        $query .= "     AND APPLICANTDIV = '{$fields["APPLICANTDIV"]}' ";
        $query .= "     AND HONORDIV     <> '{$fields["HONORDIV"]}' ";
        $query .= "     AND PRIORITY     = '{$fields["PRIORITY"]}' ";

        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);
        return $row;
    }

    /************/
    /* 更新処理 */
    /************/
    //INSERT
    public function &getInsertQuery($fields)
    {
        $db = Query::dbCheckOut();

        $data["ENTEXAMYEAR"][TEXT]      = $fields["ENTEXAMYEAR"];
        $data["APPLICANTDIV"][TEXT]     = $fields["APPLICANTDIV"];
        $data["HONORDIV"][TEXT]         = $fields["HONORDIV"];
        $data["HONORDIV_NAME"][TEXT]    = $fields["HONORDIV_NAME"];
        $data["HONORDIV_ABBV"][TEXT]    = $fields["HONORDIV_ABBV"];
        $data["CLUB_FLG"][TEXT]         = $fields["CLUB_FLG"];
        $data["NOTICE_CLASS"][TEXT]     = $fields["NOTICE_CLASS"];
        $data["NOTICE_KIND"][TEXT]      = $fields["NOTICE_KIND"];
        $data["ENROLL_FEES"][TEXT]      = $fields["ENROLL_FEES"];
        $data["SCHOOL_FEES"][TEXT]      = $fields["SCHOOL_FEES"];
        $data["SCHOLARSHIP1"][TEXT]     = $fields["SCHOLARSHIP1"];
        $data["SCHOLARSHIP2"][TEXT]     = $fields["SCHOLARSHIP2"];
        $data["HONOR_TYPE"][TEXT]       = $fields["HONOR_TYPE"];
        $data["PRIORITY"][TEXT]         = $fields["PRIORITY"];
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][FUNC]          = "sysdate()";

        $query = Query::insertSQL($data, "ENTEXAM_HONORDIV_MST");

        $db->query($query);
        Query::dbCheckIn($db);
        return;
    }

    //UPDATE
    public function &getUpdateQuery($fields)
    {
        $db = Query::dbCheckOut();

        $data["HONORDIV_NAME"][TEXT]    = $fields["HONORDIV_NAME"];
        $data["HONORDIV_ABBV"][TEXT]    = $fields["HONORDIV_ABBV"];
        $data["CLUB_FLG"][TEXT]         = $fields["CLUB_FLG"];
        $data["NOTICE_CLASS"][TEXT]     = $fields["NOTICE_CLASS"];
        $data["NOTICE_KIND"][TEXT]      = $fields["NOTICE_KIND"];
        $data["ENROLL_FEES"][TEXT]      = $fields["ENROLL_FEES"];
        $data["SCHOOL_FEES"][TEXT]      = $fields["SCHOOL_FEES"];
        $data["SCHOLARSHIP1"][TEXT]     = $fields["SCHOLARSHIP1"];
        $data["SCHOLARSHIP2"][TEXT]     = $fields["SCHOLARSHIP2"];
        $data["HONOR_TYPE"][TEXT]       = $fields["HONOR_TYPE"];
        $data["PRIORITY"][TEXT]         = $fields["PRIORITY"];
        $data["ENROLL_FEES2"][TEXT]     = $fields["ENROLL_FEES2"];
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][FUNC]          = "sysdate()";

        $where  = " WHERE ENTEXAMYEAR       = '".$fields["ENTEXAMYEAR"]."'";
        $where .= "   AND APPLICANTDIV      = '".$fields["APPLICANTDIV"]."'";
        $where .= "   AND HONORDIV          = '".$fields["HONORDIV"]."' ";

        $query = Query::updateSQL($data, "ENTEXAM_HONORDIV_MST", $where);

        $db->query($query);
        Query::dbCheckIn($db);
        return;
    }

    //DELETE
    public function &getDeleteQuery($fields)
    {
        $db = Query::dbCheckOut();

        $query  = " DELETE FROM ENTEXAM_HONORDIV_MST ";
        $query .= "       WHERE ENTEXAMYEAR     = '".$fields["ENTEXAMYEAR"]."'";
        $query .= "         AND APPLICANTDIV    = '".$fields["APPLICANTDIV"]."'";
        $query .= "         AND HONORDIV        = '".$fields["HONORDIV"]."'";

        $db->query($query);
        Query::dbCheckIn($db);

        return $result;
    }

    /********************/
    /* 前年度コピー処理 */
    /********************/
    //ある年度のデータが存在するかチェック
    public function checkData($year)
    {
        $db = Query::dbCheckOut();

        $query  = " SELECT ";
        $query .= "     'X' ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_HONORDIV_MST ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR = '".$year."'";
        $row = $db->getOne($query);

        Query::dbCheckIn($db);
        return $row;
    }
    //ある年度にデータが存在するのかチェック
    public function getCheckPerfectMstSql($year)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_HONORDIV_MST ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR = '".$year."' ";

        return $query;
    }

    //COPY
    public function getCopyQuery($thisYear, $nextYear)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $query  = " INSERT INTO ";
        $query .= "     ENTEXAM_HONORDIV_MST ";
        $query .= " ( ";
        $query .= "     ENTEXAMYEAR, ";
        $query .= "     APPLICANTDIV, ";
        $query .= "     HONORDIV, ";
        $query .= "     HONORDIV_NAME, ";
        $query .= "     HONORDIV_ABBV, ";
        $query .= "     CLUB_FLG, ";
        $query .= "     NOTICE_CLASS, ";
        $query .= "     NOTICE_KIND, ";
        $query .= "     ENROLL_FEES, ";
        $query .= "     SCHOOL_FEES, ";
        $query .= "     SCHOLARSHIP1, ";
        $query .= "     SCHOLARSHIP2, ";
        $query .= "     HONOR_TYPE, ";
        $query .= "     PRIORITY, ";
        $query .= "     ENROLL_FEES2, ";
        $query .= "     REGISTERCD, ";
        $query .= "     UPDATED ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     '".$nextYear."', ";
        $query .= "     APPLICANTDIV, ";
        $query .= "     HONORDIV, ";
        $query .= "     HONORDIV_NAME, ";
        $query .= "     HONORDIV_ABBV, ";
        $query .= "     CLUB_FLG, ";
        $query .= "     NOTICE_CLASS, ";
        $query .= "     NOTICE_KIND, ";
        $query .= "     ENROLL_FEES, ";
        $query .= "     SCHOOL_FEES, ";
        $query .= "     SCHOLARSHIP1, ";
        $query .= "     SCHOLARSHIP2, ";
        $query .= "     HONOR_TYPE, ";
        $query .= "     PRIORITY, ";
        $query .= "     ENROLL_FEES2, ";
        $query .= "     '".STAFFCD."', ";
        $query .= "     sysdate() ";
        $query .= " FROM";
        $query .= "     ENTEXAM_HONORDIV_MST ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR = '".$thisYear."' ";

        $db->query($query);

        $db->commit();
        Query::dbCheckIn($db);
        return;
    }
}
