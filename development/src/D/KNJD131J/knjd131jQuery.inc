<?php

require_once('for_php7.php');

class knjd131jQuery extends Query
{
    //学年チェック
    public function checkGrade($model)
    {
        $query  = "SELECT ";
        $query .= "    COUNT(*) AS CNT ";
        $query .= "FROM ";
        $query .= "    NAME_MST ";
        $query .= "WHERE ";
        $query .= "    NAMECD1 = 'A034' ";
        $query .= "AND NAME1 = 'M' ";
        $query .= "AND '".$model->grade."' BETWEEN NAME2 AND NAME3 ";

        return $query;
    }
        
    //１レコード取得
    public function getTrainRow($model, $schregno)
    {
        $query  = "  WITH DIV1 AS ( ";
        $query .= "  SELECT ";
        $query .= "      SCHREGNO, ";
        $query .= "      REMARK1 AS DIV1_REMARK ";
        $query .= "  FROM ";
        $query .= "      HREPORTREMARK_DETAIL_DAT ";
        $query .= "  WHERE ";
        $query .= "      YEAR = '".CTRL_YEAR."' AND ";
        $query .= "      SEMESTER = '9' AND ";
        $query .= "      DIV = '01' AND ";
        $query .= "      CODE = '01' AND ";
        $query .= "      SCHREGNO = '".$schregno."' ";
        $query .= "  ), DIV2 AS ( ";
        $query .= "  SELECT ";
        $query .= "      SCHREGNO, ";
        $query .= "      REMARK1 AS DIV2_REMARK ";
        $query .= "  FROM ";
        $query .= "      HREPORTREMARK_DETAIL_DAT ";
        $query .= "  WHERE ";
        $query .= "      YEAR = '".CTRL_YEAR."' AND ";
        $query .= "      SEMESTER = '9' AND ";
        $query .= "      DIV = '02' AND ";
        $query .= "      CODE = '01' AND ";
        $query .= "      SCHREGNO = '".$schregno."' ";
        $query .= "  ), DIV3 AS ( ";
        $query .= "  SELECT ";
        $query .= "      SCHREGNO, ";
        $query .= "      REMARK4 AS DIV3_REMARK ";
        $query .= "  FROM ";
        $query .= "      HREPORTREMARK_DETAIL_DAT ";
        $query .= "  WHERE ";
        $query .= "      YEAR = '".CTRL_YEAR."' AND ";
        $query .= "      SEMESTER = '9' AND ";
        $query .= "      DIV = '03' AND ";
        $query .= "      CODE = '01' AND ";
        $query .= "      SCHREGNO = '".$schregno."' ";
        $query .= "  ), SCHNO AS ( ";
        $query .= "  SELECT ";
        $query .= "      SCHREGNO ";
        $query .= "  FROM ";
        $query .= "      DIV1 ";
        $query .= "  UNION ALL ";
        $query .= "  SELECT ";
        $query .= "      SCHREGNO ";
        $query .= "  FROM ";
        $query .= "      DIV2 ";
        $query .= "  UNION ALL ";
        $query .= "  SELECT ";
        $query .= "      SCHREGNO ";
        $query .= "  FROM ";
        $query .= "      DIV3 ";
        $query .= "  ) ";
        $query .= "  SELECT ";
        $query .= "      T1.DIV1_REMARK, ";
        $query .= "      T2.DIV2_REMARK, ";
        $query .= "      T3.DIV3_REMARK ";
        $query .= "  FROM ";
        $query .= "      SCHNO T0 ";
        $query .= "      LEFT JOIN DIV1 T1 ON T1.SCHREGNO = T0.SCHREGNO ";
        $query .= "      LEFT JOIN DIV2 T2 ON T2.SCHREGNO = T0.SCHREGNO ";
        $query .= "      LEFT JOIN DIV3 T3 ON T3.SCHREGNO = T0.SCHREGNO ";

        return $query;
    }

    //データチェック
    public function getDataCount($semester, $schregno, $div)
    {
        $query  = "SELECT ";
        $query .= "    COUNT(*) AS CNT ";
        $query .= "FROM ";
        $query .= "    HREPORTREMARK_DETAIL_DAT ";
        $query .= "WHERE ";
        $query .= "    YEAR = '".CTRL_YEAR."' ";
        $query .= "AND SEMESTER = '".$semester."' ";
        $query .= "AND SCHREGNO = '".$schregno."' ";
        $query .= "AND DIV = '".$div."' ";
        $query .= "AND CODE = '01' ";

        return $query;
    }

    /********/
    /* 更新 */
    /********/
    //INSERT
    public function &getInsertQuery($fields, $schregno)
    {
        $db = Query::dbCheckOut();

        //総合的な学習の時間
        $div1_data = $db->getOne(knjd131jQuery::getDataCount("9", $schregno, "01"));
        $data = array();
        $data["REMARK1"][TEXT]             = $fields["DIV1_REMARK"];
        $data["REGISTERCD"][TEXT]          = STAFFCD;
        $data["UPDATED"][NUMBER]           = "sysdate()";
        if ($div1_data == 0) {
            $data["YEAR"][TEXT]                = CTRL_YEAR;
            $data["SEMESTER"][TEXT]            = 9;
            $data["SCHREGNO"][TEXT]            = $schregno;
            $data["DIV"][TEXT]                 = "01";
            $data["CODE"][TEXT]                = "01";
            
            $db->query(Query::insertSQL($data, "HREPORTREMARK_DETAIL_DAT"));
        } else {
            $where  = "WHERE ";
            $where .= "    YEAR = '".CTRL_YEAR."' ";
            $where .= "AND SEMESTER = '9' ";
            $where .= "AND SCHREGNO = '".$schregno."' ";
            $where .= "AND DIV  = '01' ";
            $where .= "AND CODE = '01' ";
            
            $db->query(Query::updateSQL($data, "HREPORTREMARK_DETAIL_DAT", $where));
        }

        //道徳
        $div2_data = $db->getOne(knjd131jQuery::getDataCount("9", $schregno, "02"));
        $data2 = array();
        $data2["REMARK1"][TEXT]             = $fields["DIV2_REMARK"];
        $data2["REGISTERCD"][TEXT]          = STAFFCD;
        $data2["UPDATED"][NUMBER]           = "sysdate()";
        if ($div2_data == 0) {
            $data2["YEAR"][TEXT]                = CTRL_YEAR;
            $data2["SEMESTER"][TEXT]            = 9;
            $data2["SCHREGNO"][TEXT]            = $schregno;
            $data2["DIV"][TEXT]                 = "02";
            $data2["CODE"][TEXT]                = "01";
            
            $db->query(Query::insertSQL($data2, "HREPORTREMARK_DETAIL_DAT"));
        } else {
            $where  = "WHERE ";
            $where .= "    YEAR = '".CTRL_YEAR."' ";
            $where .= "AND SEMESTER = '9' ";
            $where .= "AND SCHREGNO = '".$schregno."' ";
            $where .= "AND DIV  = '02' ";
            $where .= "AND CODE = '01' ";
            
            $db->query(Query::updateSQL($data2, "HREPORTREMARK_DETAIL_DAT", $where));
        }
        
        //活動と奉仕の記録
        $div3_data = $db->getOne(knjd131jQuery::getDataCount("9", $schregno, "03"));
        $data3 = array();
        $data3["REMARK4"][TEXT]             = $fields["DIV3_REMARK"];
        $data3["REGISTERCD"][TEXT]          = STAFFCD;
        $data3["UPDATED"][NUMBER]           = "sysdate()";

        if ($div3_data == 0) {
            $data3["YEAR"][TEXT]                = CTRL_YEAR;
            $data3["SEMESTER"][TEXT]            = 9;
            $data3["SCHREGNO"][TEXT]            = $schregno;
            $data3["DIV"][TEXT]                 = "03";
            $data3["CODE"][TEXT]                = "01";
            
            $db->query(Query::insertSQL($data3, "HREPORTREMARK_DETAIL_DAT"));
        } else {
            $where  = "WHERE ";
            $where .= "    YEAR = '".CTRL_YEAR."' ";
            $where .= "AND SEMESTER = '9' ";
            $where .= "AND SCHREGNO = '".$schregno."' ";
            $where .= "AND DIV  = '03' ";
            $where .= "AND CODE = '01' ";
            
            $db->query(Query::updateSQL($data3, "HREPORTREMARK_DETAIL_DAT", $where));
        }
        
        Query::dbCheckIn($db);
        return ;
    }
}
