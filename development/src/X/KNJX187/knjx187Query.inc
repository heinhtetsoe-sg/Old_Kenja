<?php

require_once('for_php7.php');
class knjx187query extends Query
{
    /******************/
    /**  画面で使用  **/
    /******************/
    public function getSecurityHigh()
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     MENU_HIGH_SECURITY_MST ";
        $query .= " WHERE ";
        $query .= "     PROGRAMID = 'KNJX187' ";
        $query .= "     AND INVALID_FLG = '0' ";

        return $query;
    }

    //年度一覧
    public function getYearSeme()
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     T1.YEAR || T1.SEMESTER  AS VALUE, ";
        $query .= "     T1.YEAR || '年度 ' || T2.SEMESTERNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_HDAT T1 ";
        $query .= "     INNER JOIN SEMESTER_MST T2 ON T1.YEAR     = T2.YEAR ";
        $query .= "                               AND T1.SEMESTER = T2.SEMESTER ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //年組一覧
    public function getGradeHrClass($model)
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     HDAT.GRADE || HDAT.HR_CLASS AS VALUE, ";
        $query .= "     HDAT.HR_NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_HDAT HDAT ";
        $query .= " WHERE ";
        $query .= "         HDAT.YEAR || HDAT.SEMESTER = '".$model->field["YEAR"]."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    /**********************/
    /**  存在チェック用  **/
    /**********************/
    //学籍番号
    public function schregNoCntSql($schregno)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT ";
        $query .= " WHERE ";
        $query .= "         YEAR     = '".CTRL_YEAR."' ";
        $query .= "     AND SEMESTER = '".CTRL_SEMESTER."' ";
        $query .= "     AND SCHREGNO = '{$schregno}' ";

        return $query;
    }

    /************************/
    /**  ＣＳＶ取込で使用  **/
    /************************/
    //マスタの追加（ＣＳＶデータより読込）
    public function insertQueryCsv($model, &$data_arr)
    {
        $data = array();

        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $cnt = 0;   //処理件数
        for ($i = 0; $i < get_count($data_arr); $i++) {
            //データセット
            $data["SCHREGNO"][TEXT]             = $data_arr[$i]["SCHREGNO"];
            $data["YEAR"][TEXT]                 = $data_arr[$i]["YEAR"];
            $data["BASE_SEQ"][TEXT]             = "004";
            $data["BASE_REMARK2"][TEXT]         = $data_arr[$i]["BASE_REMARK2"];
            $data["REGISTERCD"][TEXT]           = STAFFCD;
            $data["UPDATED"][NUMBER]            = "SYSDATE()";
            if (1 > $db->getOne("SELECT COUNT(*) FROM SCHREG_BASE_YEAR_DETAIL_MST WHERE SCHREGNO = '".$data_arr[$i]["SCHREGNO"]."' AND YEAR = '".$data_arr[$i]["YEAR"]."' AND BASE_SEQ = '004'")) {
                $query = Query::insertSQL($data, "SCHREG_BASE_YEAR_DETAIL_MST");
            } else {
                $where  = " WHERE SCHREGNO = '".$data_arr[$i]["SCHREGNO"]."' ";
                $where .= "   AND YEAR     = '".$data_arr[$i]["YEAR"]."' ";
                $where .= "   AND BASE_SEQ = '004' ";

                $query = Query::updateSQL($data, "SCHREG_BASE_YEAR_DETAIL_MST", $where);
            }
            $db->query($query);
            $cnt++;
        }
        $db->commit();
        Query::dbCheckIn($db);

        return $cnt;
    }

    /************************/
    /**  ＣＳＶ削除で使用  **/
    /************************/
    //削除（ＣＳＶデータより読込）
    public function deleteQueryCsv($model, &$data_arr)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $cnt = 0;   //処理件数
        for ($i = 0; $i < get_count($data_arr); $i++) {
            if (1 > $db->getOne("SELECT COUNT(*) FROM SCHREG_BASE_YEAR_DETAIL_MST WHERE SCHREGNO = '".$data_arr[$i]["SCHREGNO"]."' AND YEAR = '".$data_arr[$i]["YEAR"]."' AND BASE_SEQ = '004'")) {
            } else {
                $data = array();
                $data["BASE_REMARK2"][TEXT]         = "";
                $data["REGISTERCD"][TEXT]           = STAFFCD;
                $data["UPDATED"][NUMBER]            = "SYSDATE()";
                $where  = " WHERE SCHREGNO = '".$data_arr[$i]["SCHREGNO"]."' ";
                $where .= "   AND YEAR     = '".$data_arr[$i]["YEAR"]."' ";
                $where .= "   AND BASE_SEQ = '004' ";

                $query = Query::updateSQL($data, "SCHREG_BASE_YEAR_DETAIL_MST", $where);
                $db->query($query);
            }
            $cnt++;
        }
        $db->commit();
        Query::dbCheckIn($db);

        return $cnt;
    }

    /********************/
    /**  エラー処理用  **/
    /********************/
    //エラーＤＢへの追加
    public function insertQueryErr(&$db, $record_no, $check_error)
    {
        $data1 = array();
        $data1["PROGRAMID"][TEXT]   = PROGRAMID;
        $data1["MSGROW"][NUMBER]    = $record_no;
        $data1["MSGREMARK"][TEXT]   = $check_error;

        $query = Query::insertSQL($data1, "W_CSVMSG_PRG_DAT");
        $db->query($query);
    }

    /************************/
    /**  ＣＳＶ出力で使用  **/
    /************************/
    //名称マスタ取得
    public function getNameMst($namecd1)
    {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "         YEAR    = '".CTRL_YEAR."' ";
        $query .= "     AND NAMECD1 = '".$namecd1."' ";
        $query .= " ORDER BY ";
        $query .= "     NAMECD2 ";

        return $query;
    }

    //CSVデータ出力
    public function selectMainQuery($model)
    {
        $query  = " SELECT ";
        $query .= "     REGD.GRADE, ";
        $query .= "     REGD.HR_CLASS, ";
        $query .= "     REGD.ATTENDNO, ";
        $query .= "     REGD.SCHREGNO, ";
        $query .= "     REGD.YEAR, ";
        $query .= "     SEQ004.BASE_REMARK2, ";
        $query .= "     '".$model->lastColumn."' AS ".$model->lastColumn." ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT REGD ";
        $query .= "     LEFT JOIN SCHREG_BASE_YEAR_DETAIL_MST SEQ004 ON REGD.SCHREGNO   = SEQ004.SCHREGNO ";
        $query .= "                                                 AND REGD.YEAR       = SEQ004.YEAR ";
        $query .= "                                                 AND SEQ004.BASE_SEQ = '004' ";
        $query .= " WHERE ";
        $query .= "     REGD.YEAR || REGD.SEMESTER = '".$model->field["YEAR"]."' ";
        if ($model->field["GRADE_HR_CLASS"] != "") {
            $query .= "     AND REGD.GRADE || REGD.HR_CLASS = '".$model->field["GRADE_HR_CLASS"]."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     REGD.GRADE || REGD.HR_CLASS || REGD.ATTENDNO ";

        return $query;
    }
}
