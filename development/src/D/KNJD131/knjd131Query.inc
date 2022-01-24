<?php

require_once('for_php7.php');

class knjd131Query extends Query {
    //１レコード取得
    function getTrainRow($model, $schregno) {
        $query  = "  WITH REMARK AS ( ";
        $query .= "  SELECT ";
        $query .= "      SCHREGNO, ";
        $query .= "      SPECIALACTREMARK ";
        $query .= "  FROM ";
        $query .= "      HREPORTREMARK_DAT ";
        $query .= "  WHERE ";
        $query .= "      YEAR = '".CTRL_YEAR."' AND ";
        $query .= "      SEMESTER = '9' AND ";
        $query .= "      SCHREGNO = '".$schregno."' ";
        $query .= "  ), COMMU AS ( ";
        $query .= "  SELECT ";
        $query .= "      SCHREGNO, ";
        $query .= "      COMMUNICATION ";
        $query .= "  FROM ";
        $query .= "      HREPORTREMARK_DAT ";
        $query .= "  WHERE ";
        $query .= "      YEAR = '".CTRL_YEAR."' AND ";
        $query .= "      SEMESTER = '".$model->field["SEMESTER"]."' AND ";
        $query .= "      SCHREGNO = '".$schregno."' ";
        $query .= "  ), SCHNO AS ( ";
        $query .= "  SELECT ";
        $query .= "      SCHREGNO ";
        $query .= "  FROM ";
        $query .= "      REMARK ";
        $query .= "  UNION ALL ";
        $query .= "  SELECT ";
        $query .= "      SCHREGNO ";
        $query .= "  FROM ";
        $query .= "      COMMU ";
        $query .= "  ) ";
        $query .= "  SELECT ";
        $query .= "      T1.SPECIALACTREMARK, ";
        $query .= "      T2.COMMUNICATION ";
        $query .= "  FROM ";
        $query .= "      SCHNO T0 ";
        $query .= "      LEFT JOIN REMARK T1 ON T1.SCHREGNO = T0.SCHREGNO ";
        $query .= "      LEFT JOIN COMMU T2 ON T2.SCHREGNO = T0.SCHREGNO ";

        return $query;
    }

    //学期取得
    function getSemester()
    {
        $query  = "SELECT ";
        $query .= "    SEMESTERNAME AS LABEL, ";
        $query .= "    SEMESTER AS VALUE ";
        $query .= "FROM ";
        $query .= "    SEMESTER_MST ";
        $query .= "WHERE ";
        $query .= "    YEAR = '".CTRL_YEAR."' ";
        $query .= "AND SEMESTER <> '9' ";
        $query .= "ORDER BY ";
        $query .= "    SEMESTER ";

        return $query;
    }

    //ログイン学期名取得（一括更新タイトル用）
    function getSemesterName()
    {
        $query  = "SELECT ";
        $query .= "    SEMESTERNAME ";
        $query .= "FROM ";
        $query .= "    SEMESTER_MST ";
        $query .= "WHERE ";
        $query .= "    YEAR = '".CTRL_YEAR."' ";
        $query .= "AND SEMESTER = '".CTRL_SEMESTER."' ";

        return $query;
    }
    
    //クラス名取得（一括更新タイトル用）
    function getHrClassName($model)
    {
        $query  = "SELECT ";
        $query .= "    HR_NAME ";
        $query .= "FROM ";
        $query .= "    SCHREG_REGD_HDAT ";
        $query .= "WHERE ";
        $query .= "    YEAR = '".CTRL_YEAR."' ";
        $query .= "AND SEMESTER = '".CTRL_SEMESTER."' ";
        $query .= "AND GRADE || '-' || HR_CLASS = '{$model->grade_hrclass}' ";
        
        return $query;
    }

    //部活動参照
    function getClub($model) {
        $query  = " SELECT DISTINCT ";
        $query .= "     L2.CLUBNAME, ";
        $query .= "     T1.CLUBCD, ";
        $query .= "     L1.DETAIL_DATE, ";
        $query .= "     L1.DETAIL_REMARK ";
        $query .= " FROM ";
        $query .= "     SCHREG_CLUB_HIST_DAT T1 ";
        $query .= " LEFT JOIN ";
        $query .= "     SCHREG_CLUB_HDETAIL_DAT L1 ON  L1.SCHREGNO = T1.SCHREGNO ";
        $query .= "                                AND L1.CLUBCD = T1.CLUBCD ";
        $query .= " LEFT JOIN ";
        $query .= "     CLUB_MST L2 ON L2.CLUBCD = T1.CLUBCD ";
        $query .= " WHERE ";
        $query .= "     T1.SCHREGNO = '{$model->schregno}' ";
        $query .= " ORDER BY ";
        $query .= "     T1.CLUBCD, ";
        $query .= "     L1.DETAIL_DATE ";

        return $query;
    }

    //委員会活動参照
    function getCommittee($model) {
        $query  = " SELECT ";
        $query .= "     L1.SEQ, ";
        $query .= "     L2.COMMITTEENAME, ";
        $query .= "     T1.CHARGENAME, ";
        $query .= "     L1.DETAIL_DATE, ";
        $query .= "     L1.DETAIL_REMARK ";
        $query .= " FROM ";
        $query .= "     SCHREG_COMMITTEE_HIST_DAT T1 ";
        $query .= " LEFT JOIN ";
        $query .= "     SCHREG_COMMITTEE_HDETAIL_DAT L1 ON  L1.YEAR = T1.YEAR ";
        $query .= "                                     AND L1.SEQ  = T1.SEQ ";
        $query .= " LEFT JOIN ";
        $query .= "     COMMITTEE_MST L2 ON  L2.COMMITTEE_FLG = T1.COMMITTEE_FLG ";
        $query .= "                      AND L2.COMMITTEECD   = T1.COMMITTEECD ";
        $query .= " WHERE ";
        $query .= "         T1.YEAR     = '".CTRL_YEAR."' ";
        $query .= "     AND T1.SCHREGNO = '{$model->schregno}' ";
        $query .= " ORDER BY ";
        $query .= "     T1.COMMITTEECD, ";
        $query .= "     L1.DETAIL_DATE ";

        return $query;
    }

    //一括更新の生徒一覧
    function getStudent($model) {
        $query  = " SELECT ";
        $query .= "     T1.ATTENDNO || ' ' || T1.SCHREGNO || ' ' || L1.NAME_SHOW AS LABEL, ";
        $query .= "     T1.SCHREGNO AS VALUE, ";
        $query .= "     T1.SCHREGNO ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1 ";
        $query .= " LEFT JOIN ";
        $query .= "     SCHREG_BASE_MST L1 ON  L1.SCHREGNO = T1.SCHREGNO ";
        $query .= " WHERE ";
        $query .= "         T1.YEAR     = '".CTRL_YEAR."' ";
        $query .= "     AND T1.SEMESTER = '".CTRL_SEMESTER."' ";
        $query .= "     AND T1.GRADE || '-' || T1.HR_CLASS = '{$model->grade_hrclass}' ";
        $query .= " ORDER BY ";
        $query .= "     ATTENDNO ";

        return $query;
    }

    function getCommunication($field, $schregno) {
        $query .= " SELECT ";
        $query .= "     COMMUNICATION ";
        $query .= " FROM ";
        $query .= "     HREPORTREMARK_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR     = '".CTRL_YEAR."' AND ";
        $query .= "     SEMESTER = '".$field["SEMESTER"]."' AND ";
        $query .= "     SCHREGNO = '{$schregno}' ";

        return $query;
    }

   //レコードがあるかのチェック(インサートにするかアップデートにするか)
   function checkExists($field, $schregno) {
        $query .= " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     HREPORTREMARK_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' AND ";
        $query .= "     SEMESTER = '".$field["SEMESTER"]."' AND ";
        $query .= "     SCHREGNO = '{$schregno}' ";

        return $query;
   }

    /********/
    /* 更新 */
    /********/
    //INSERT
    function &getInsertQuery($fields, $schregno) {
        $db = Query::dbCheckOut();

        $data["YEAR"][TEXT]                = CTRL_YEAR;
        $data["SEMESTER"][TEXT]            = 9;
        $data["SCHREGNO"][TEXT]            = $schregno;
        $data["SPECIALACTREMARK"][TEXT]    = $fields["SPECIALACTREMARK"];
        $data["REGISTERCD"][TEXT]          = STAFFCD;
        $data["UPDATED"][NUMBER]           = "sysdate()";

        $db->query(Query::insertSQL($data, "hreportremark_dat"));

        //通信欄
        $data2["YEAR"][TEXT]                = CTRL_YEAR;
        $data2["SEMESTER"][TEXT]            = $fields["SEMESTER"];
        $data2["SCHREGNO"][TEXT]            = $schregno;
        $data2["COMMUNICATION"][TEXT]       = $fields["COMMUNICATION"];
        $data2["REGISTERCD"][TEXT]          = STAFFCD;
        $data2["UPDATED"][NUMBER]           = "sysdate()";

        $db->query(Query::insertSQL($data2, "hreportremark_dat"));
        
        Query::dbCheckIn($db);
        return ;
    }

   //DELETE
   function &getDeleteQuery($field, $schregno) {
        $db = Query::dbCheckOut();

        $query  = " DELETE FROM ";
        $query .= "     HREPORTREMARK_DAT ";
        $query .= " WHERE ";
        $query .= "         YEAR     = '".CTRL_YEAR."' ";
        $query .= "     AND SEMESTER IN ('9', '".$field["SEMESTER"]."') ";
        $query .= "     AND SCHREGNO = '".$schregno."'";

        $db->query($query);

        Query::dbCheckIn($db);
        return $result;
   }

    //INSERT
    function &getInsertQuery2($field, $selectdata) {
        $db = Query::dbCheckOut();

        $schregArray = explode(",", $selectdata);
        foreach ($schregArray as $schregno) {
            $query = knjd131Query::checkExists($field, $schregno);
            $cnt = $db->getOne($query);

            $data["YEAR"][TEXT]                = CTRL_YEAR;
            $data["SEMESTER"][TEXT]            = $field["SEMESTER"];
            $data["SCHREGNO"][TEXT]            = $schregno;
            if ($field["RCHECK1"] == "on") {
                $data["COMMUNICATION"][TEXT]   = $field["COMMUNICATION"];
            }
            $data["REGISTERCD"][TEXT]          = STAFFCD;
            $data["UPDATED"][NUMBER]           = "sysdate()";

            if ($cnt > 0) {
                $where  = " WHERE YEAR     = '".CTRL_YEAR."' AND ";
                $where .= "       SEMESTER = '".$field["SEMESTER"]."' AND ";
                $where .= "       SCHREGNO = '{$schregno}' ";

                $query = Query::updateSQL($data, "HREPORTREMARK_DAT",$where);
            } else {
                $query = Query::insertSQL($data, "HREPORTREMARK_DAT");
            }
            $db->query($query);
        }

        Query::dbCheckIn($db);
        return $result;
   }
}
?>
