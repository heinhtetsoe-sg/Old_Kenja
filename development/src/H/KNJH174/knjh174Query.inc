<?php

require_once('for_php7.php');
class knjh174query extends Query {


    /**************************/
    /**  前年度コピーで使用  **/
    /**************************/

    //年度
    function getYear() {
        $query  = " WITH THIS_YEAR(VALUE, LABEL) AS ( ";
        $query .= " VALUES('".CTRL_YEAR."', '".CTRL_YEAR."') ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     YEAR AS VALUE, ";
        $query .= "     YEAR AS LABEL ";
        $query .= " FROM ";
        $query .= "     HOUSE_GROUP_DAT ";
        $query .= " GROUP BY ";
        $query .= "     YEAR ";
        $query .= " UNION ";
        $query .= " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     THIS_YEAR ";
        $query .= " ORDER BY ";
        $query .= "     VALUE DESC ";
        return $query;
    }

    //その年度のデータが存在するかチェック
    function CheckData($model, $year) {
        $db = Query::dbCheckOut();

        $query  = " SELECT 'X' FROM HOUSE_GROUP_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$year}'";
        $row = $db->getOne($query);

        Query::dbCheckIn($db);
        return $row;
    }

    function getCopyQuery($model) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $query  ="  INSERT INTO HOUSE_GROUP_DAT( ";
        $query .="    YEAR, ";
        $query .="    HOUSE_GROUP_CD, ";
        $query .="    SCHREGNO, ";
        $query .="    REGISTERCD, ";
        $query .="    UPDATED ";
        $query .="  ) ( ";
        $query .="    SELECT ";
        $query .="      '".$model->field["TRGTYEAR"]."', ";
        $query .="       T1.HOUSE_GROUP_CD, ";
        $query .="       T1.SCHREGNO, ";
        $query .="       '".STAFFCD."', ";
        $query .="       SYSDATE() ";
        $query .="    FROM ";
        $query .="      HOUSE_GROUP_DAT T1 ";
        $query .="    WHERE ";
        $query .="      T1.YEAR = '".($model->field["TRGTYEAR"] - 1)."' ";
        $query .="      AND EXISTS ( ";                             //指定年度に在籍データがある生徒だけコピー
        $query .="        SELECT ";
        $query .="          'X' FROM SCHREG_REGD_DAT T2 ";
        $query .="        WHERE ";
        $query .="              T1.SCHREGNO = T2.SCHREGNO ";
        $query .="          AND T2.YEAR = ".$model->field["TRGTYEAR"]."";
        $query .="          AND T2.SEMESTER = '".CTRL_SEMESTER."' ";
        $query .="      ) ";
        $query .="  ) ";

        $db->query($query);

        $db->commit();
        Query::dbCheckIn($db);
        return;
    }


    /******************/
    /**  画面で使用  **/
    /******************/

    //名称マスタより取得
    function getNameMst($year, $namecd1, $namecd2="") {
        $query  = " SELECT DISTINCT ";
        $query .= "     NAME1, ";
        $query .= "     NAMECD2 || ':' || NAME1 AS LABEL, ";
        $query .= "     NAMECD2 AS VALUE, ";
        $query .= "     NAMESPARE2 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR    = '".$year."' AND ";
        $query .= "     NAMECD1 = '".$namecd1."' ";
        if ($namecd2) {
            $query .= " AND NAMECD2 = '".$namecd2."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //名称マスタより取得(ハウス)
    function gethouse($model) {
        $query  = " SELECT ";
        $query .= "     NAMECD2 AS VALUE, ";
        $query .= "     NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$model->field["TRGTYEAR"]."' ";
        $query .= "     AND NAMECD1 = 'H050' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //名称マスタより取得(ファミリー)
    function getfamily($model) {
        $query  = " SELECT ";
        $query .= "     NAMECD2 AS VALUE, ";
        $query .= "     NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$model->field["TRGTYEAR"]."' ";
        $query .= "     AND NAMECD1 = 'H051' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }


    /************************/
    /**  ＣＳＶ取込で使用  **/
    /************************/

    //追加（ＣＳＶデータより読込）
    function updateQueryCsv($model, &$data_arr) {
        $data = array();
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $cnt = 0;   //処理件数
        for ($i = 0; $i < get_count($data_arr); $i++) {

            $houseGroupCd = $data_arr[$i]["HOUSE_GROUP_CD"];
            $schregno = $data_arr[$i]["SCHREGNO"];
            if ($schregno != "") {
                //更新 -- HOUSE_GROUP_DAT
                //DELETE
                $query  = "  DELETE ";
                $query .= "  FROM ";
                $query .= "    HOUSE_GROUP_DAT ";
                $query .= "  WHERE ";
                $query .= "    YEAR = '".$model->field["TRGTYEAR"]."' AND ";
                $query .= "    SCHREGNO = '{$schregno}' ";  //学籍番号のみをチェックする。あればHOUSE_GROUP_CDを変更したと解釈し削除
                $db->query($query);

                //INSERT
                $data = array(); 
                $data["YEAR"][TEXT]             = $model->field["TRGTYEAR"];
                $data["SCHREGNO"][TEXT]         = $schregno;
                $data["HOUSE_GROUP_CD"][TEXT]   = $houseGroupCd;
                $data["REGISTERCD"][TEXT]       = STAFFCD;
                $data["UPDATED"][NUMBER]        = "SYSDATE()";

                $query = Query::insertSQL($data, "HOUSE_GROUP_DAT");
                $db->query($query);

                //UPDATE(SCHREG_BASE_MST)
                $namemst = $db->getRow(knjh174Query::getNameMst($model->field["TRGTYEAR"], "H050", SUBSTR($houseGroupCd, 0, 1)));
                $detaildata = $namemst[0];
                $namemst = $db->getRow(knjh174Query::getNameMst($model->field["TRGTYEAR"], "H051", SUBSTR($houseGroupCd, 1, 1)));
                $detaildata .= $namemst[0];
                $namemst = $db->getRow(knjh174Query::getNameMst($model->field["TRGTYEAR"], "H052", SUBSTR($houseGroupCd, 2, 1)));
                $detaildata .= $namemst[0];

                $data = array();
                $data["REMARK3"][TEXT]     = $detaildata;
                $data["REGISTERCD"][TEXT]  = STAFFCD;
                $data["UPDATED"][FUNC]     = "SYSDATE()";
                $where  = " WHERE ";
                $where .= "     SCHREGNO = '{$schregno}' ";

                $query = Query::updateSQL($data, "SCHREG_BASE_MST", $where);
                $db->query($query);

            }
            $cnt++;
        }
        $db->commit();
        Query::dbCheckIn($db);

        return $cnt;
    }

    //削除（ＣＳＶデータより読込）
    function deleteQueryCsv($model, &$data_arr) {
        $data = array();
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $cnt = 0;   //処理件数
        for ($i = 0; $i < get_count($data_arr); $i++) {
            $houseGroupCd = $data_arr[$i]["HOUSE_GROUP_CD"];
            $schregno = $data_arr[$i]["SCHREGNO{$grpNo}"];
            if ($schregno != "") {
                //削除 -- HOUSE_GROUP_DAT
                //DELETE
                $query  = "  DELETE ";
                $query .= "  FROM ";
                $query .= "    HOUSE_GROUP_DAT ";
                $query .= "  WHERE ";
                $query .= "    YEAR             = '".$model->field["TRGTYEAR"]."' AND ";
                $query .= "    SCHREGNO         = '{$schregno}' ";

                $db->query($query);

                //更新 -- SCHREG_BASE_MST
                $data = array();
                $data["REMARK3"][TEXT]     = null;
                $data["REGISTERCD"][TEXT]  = STAFFCD;
                $data["UPDATED"][FUNC]     = "SYSDATE()";
                $where  = " WHERE ";
                $where .= "     SCHREGNO = '{$schregno}' ";
                $query = Query::updateSQL($data, "SCHREG_BASE_MST", $where);

                $db->query($query);
            }
            $cnt++;
        }
        $db->commit();
        Query::dbCheckIn($db);
        return $cnt;
    }


    /************************/
    /**  エラーチェック用  **/
    /************************/

    //存在チェック(在校生)
    function getSchregExists($model,$schregno) {
        $query  ="    SELECT ";
        $query .="      COUNT(*) ";
        $query .="    FROM ";
        $query .="      SCHREG_REGD_DAT ";
        $query .="    WHERE ";
        $query .="      YEAR = '".$model->field["TRGTYEAR"]."' AND ";
        $query .="      SEMESTER = '".CTRL_SEMESTER."' AND ";
        $query .="      SCHREGNO = '{$schregno}' ";

        return $query;
    }

    //存在チェック(名称マスタ)
    function getHouseCdErrorChk($model) {
        $query  = "    SELECT ";
        $query .= "      NAMECD1, ";
        $query .= "      NAMECD2 ";
        $query .= "    FROM ";
        $query .= "      V_NAME_MST ";
        $query .= "    WHERE ";
        $query .= "      YEAR = '".$model->field["TRGTYEAR"]."' ";
        $query .= "      AND NAMECD1 IN ('H050', 'H051', 'H052') ";
        $query .= "    ORDER BY ";
        $query .= "      NAMECD1, ";
        $query .= "      NAMECD2 ";

        return $query;
    }

    //エラーＤＢへの追加
    function insertQueryErr(&$db, $record_no, $check_error) {
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

    //CSVデータ出力
    function selectMainQuery($model) {

        $query  ="    WITH MAX_SEMES AS ( ";
        $query .="      SELECT ";
        $query .="        YEAR, ";
        $query .="        MAX(SEMESTER) AS SEMESTER, ";
        $query .="        SCHREGNO ";
        $query .="      FROM ";
        $query .="        SCHREG_REGD_DAT ";
        $query .="      WHERE ";
        $query .="        YEAR = '".$model->field["TRGTYEAR"]."' ";
        $query .="      GROUP BY ";
        $query .="        YEAR, ";
        $query .="        SCHREGNO ";
        $query .="    ), REGD_DATA AS ( ";
        $query .="      SELECT ";
        $query .="        T1.YEAR, ";
        $query .="        T1.SCHREGNO, ";
        $query .="        T5.NAME, ";
        $query .="        T5.NAME_KANA, ";
        $query .="        T4.GRADE_NAME1, ";
        $query .="        T3.HR_NAME, ";
        $query .="        T1.ATTENDNO, ";
        $query .="        T6.ABBV1, ";
        $query .="        T1.GRADE || T1.HR_CLASS || T1.ATTENDNO AS SORT_NENKUMIBAN ";
        $query .="      FROM ";
        $query .="        SCHREG_REGD_DAT T1 ";
        $query .="        INNER JOIN MAX_SEMES T2 ";
        $query .="          ON T1.YEAR = T2.YEAR ";
        $query .="          AND T1.SEMESTER = T2.SEMESTER ";
        $query .="    	  AND T1.SCHREGNO = T2.SCHREGNO ";
        $query .="        LEFT JOIN SCHREG_REGD_HDAT T3 ";
        $query .="          ON T1.YEAR = T3.YEAR ";
        $query .="          AND T1.SEMESTER = T3.SEMESTER ";
        $query .="          AND T1.GRADE = T3.GRADE ";
        $query .="          AND T1.HR_CLASS = T3.HR_CLASS ";
        $query .="        LEFT JOIN SCHREG_REGD_GDAT T4 ";
        $query .="          ON T1.YEAR = T4.YEAR ";
        $query .="          AND T1.GRADE = T4.GRADE ";
        $query .="        LEFT JOIN SCHREG_BASE_MST T5 ";
        $query .="          ON T1.SCHREGNO = T5.SCHREGNO ";
        $query .="        LEFT JOIN NAME_MST T6 ";
        $query .="          ON T6.NAMECD1 = 'Z002' ";
        $query .="          AND T6.NAMECD2 = T5.SEX ";
        $query .="      WHERE ";
        $query .="        T1.YEAR = '".$model->field["TRGTYEAR"]."' ";
        $query .="    ) ";
        $query .="    SELECT ";
        $query .="      REGD.GRADE_NAME1, ";
        $query .="      REGD.HR_NAME, ";
        $query .="      REGD.ATTENDNO, ";
        $query .="      HGD.HOUSE_GROUP_CD, ";
        $query .="      T1.NAME1, ";
        $query .="      T2.NAME1 AS NAME2, ";
        $query .="      T3.NAME1 AS NAME3, ";
        $query .="      REGD.SCHREGNO, ";
        $query .="      REGD.NAME, ";
        $query .="      REGD.NAME_KANA, ";
        $query .="      REGD.ABBV1 ";
        $query .="    FROM ";
        $query .="      HOUSE_GROUP_DAT HGD ";
        $query .="      INNER JOIN REGD_DATA REGD ";
        $query .="        ON HGD.YEAR = REGD.YEAR ";
        $query .="        AND HGD.SCHREGNO = REGD.SCHREGNO ";
        $query .="      LEFT JOIN V_NAME_MST T1 ";
        $query .="        ON T1.YEAR = '".$model->field["TRGTYEAR"]."' ";
        $query .="        AND T1.NAMECD1 = 'H050' ";
        $query .="        AND T1.NAMECD2 = SUBSTR(HGD.HOUSE_GROUP_CD, 1, 1) ";
        $query .="      LEFT JOIN V_NAME_MST T2 ";
        $query .="        ON T2.YEAR = '".$model->field["TRGTYEAR"]."' ";
        $query .="        AND T2.NAMECD1 = 'H051' ";
        $query .="        AND T2.NAMECD2 = SUBSTR(HGD.HOUSE_GROUP_CD, 2, 1) ";
        $query .="      LEFT JOIN V_NAME_MST T3 ";
        $query .="        ON T3.YEAR = '".$model->field["TRGTYEAR"]."' ";
        $query .="        AND T3.NAMECD1 = 'H052' ";
        $query .="        AND T3.NAMECD2 = SUBSTR(HGD.HOUSE_GROUP_CD, 3, 1) ";
        if($model->field["house"] != 'ALL' && $model->field["family"] != 'ALL'){
            $query .="      WHERE ";
            $query .="        SUBSTR(HGD.HOUSE_GROUP_CD, 1, 1) = '".$model->field["house"]."' ";               
            $query .="       AND SUBSTR(HGD.HOUSE_GROUP_CD, 2, 1) = '".$model->field["family"]."' ";               
        }
        if($model->field["house"] != 'ALL' && $model->field["family"] == 'ALL'){
            $query .="      WHERE ";
            $query .="        SUBSTR(HGD.HOUSE_GROUP_CD, 1, 1) = '".$model->field["house"]."' ";
        }
        if($model->field["family"] != 'ALL' && $model->field["house"] == 'ALL'){
            $query .="      WHERE ";
            $query .="        SUBSTR(HGD.HOUSE_GROUP_CD, 2, 1) = '".$model->field["family"]."' ";
        }
        $query .="    ORDER BY ";
        $query .="      HGD.HOUSE_GROUP_CD, ";
        $query .="      REGD.SORT_NENKUMIBAN ";

        return $query;
    }
}
?>
