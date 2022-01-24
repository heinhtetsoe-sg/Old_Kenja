<?php

require_once('for_php7.php');
class knjx186query extends Query
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
        $query .= "     PROGRAMID = 'KNJX186' ";
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

    //複式クラス
    public function getGhrCd($model)
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     HDAT.GHR_CD AS VALUE, ";
        $query .= "     HDAT.GHR_NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_GHR_HDAT HDAT ";
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
        $query .= "     SCHREG_BASE_MST ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO = '{$schregno}' ";

        return $query;
    }

    //学期
    public function semesterCntSql($year, $semester)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     SEMESTER_MST ";
        $query .= " WHERE ";
        $query .= "         YEAR     = '{$year}' ";
        $query .= "     AND SEMESTER = '{$semester}' ";
        $query .= "     AND SEMESTER <> '9' ";

        return $query;
    }

    //GHR_CD
    public function ghrCdCntSql($year, $semester, $ghrCd)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_GHR_HDAT ";
        $query .= " WHERE ";
        $query .= "         YEAR     = '{$year}' ";
        $query .= "     AND SEMESTER = '{$semester}' ";
        $query .= "     AND GHR_CD   = '{$ghrCd}' ";

        return $query;
    }

    /************************/
    /**  ＣＳＶ取込で使用  **/
    /************************/
    //データの追加（ＣＳＶデータより読込）
    public function insertQueryCsv($model, &$data_arr)
    {
        $data = array();

        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $cnt = 0;   //処理件数
        for ($i = 0; $i < get_count($data_arr); $i++) {
            //データセット
            $data["SCHREGNO"][TEXT]         = $data_arr[$i]["SCHREGNO"];
            $data["YEAR"][TEXT]             = $data_arr[$i]["YEAR"];
            $data["SEMESTER"][TEXT]         = $data_arr[$i]["SEMESTER"];
            $data["GHR_CD"][TEXT]           = sprintf("%02d", $data_arr[$i]["GHR_CD"]);
            $data["GHR_ATTENDNO"][TEXT]     = sprintf("%03d", $data_arr[$i]["GHR_ATTENDNO"]);
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][NUMBER]        = "SYSDATE()";
            if (1 > $db->getOne("SELECT COUNT(*) FROM SCHREG_REGD_GHR_DAT WHERE SCHREGNO = '".$data_arr[$i]["SCHREGNO"]."' AND YEAR = '".$data_arr[$i]["YEAR"]."' AND SEMESTER = '".$data_arr[$i]["SEMESTER"]."'")) {
                $query = Query::insertSQL($data, "SCHREG_REGD_GHR_DAT");
            } else {
                $where  = " WHERE SCHREGNO  = '".$data_arr[$i]["SCHREGNO"]."' ";
                $where .= "   AND YEAR      = '".$data_arr[$i]["YEAR"]."' ";
                $where .= "   AND SEMESTER  = '".$data_arr[$i]["SEMESTER"]."' ";

                $query = Query::updateSQL($data, "SCHREG_REGD_GHR_DAT", $where);
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
            $query  = " DELETE FROM SCHREG_REGD_GHR_DAT ";
            $query .= " WHERE SCHREGNO = '".$data_arr[$i]["SCHREGNO"]."' ";
            $query .= "   AND YEAR     = '".$data_arr[$i]["YEAR"]."' ";
            $query .= "   AND SEMESTER = '".$data_arr[$i]["SEMESTER"]."' ";
            $db->query($query);
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
        $query .= "     GHRD.SCHREGNO, ";
        $query .= "     GHRD.YEAR, ";
        $query .= "     GHRD.SEMESTER, ";
        $query .= "     GHRD.GHR_CD, ";
        $query .= "     GHRD.GHR_ATTENDNO, ";
        $query .= "     '".$model->lastColumn."' AS ".$model->lastColumn." ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_GHR_DAT GHRD ";
        $query .= " WHERE ";
        $query .= "         GHRD.YEAR || GHRD.SEMESTER = '".$model->field["YEAR"]."' ";
        if ($model->field["GHR_CD"] != "") {
            $query .= "     AND GHRD.GHR_CD = '".$model->field["GHR_CD"]."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     GHRD.GHR_CD || GHRD.GHR_ATTENDNO ";

        return $query;
    }
}
