<?php

require_once('for_php7.php');

class knjx100oquery extends Query {

    function getSecurityHigh() {
        $query  = " SELECT ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     MENU_HIGH_SECURITY_MST ";
        $query .= " WHERE ";
        $query .= "     PROGRAMID = 'KNJX100O' ";
        $query .= "     AND INVALID_FLG = '0' ";

        return $query;
    }

    function getSchoolCd() {
        $query  = " SELECT ";
        $query .= "     NAME2 ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = 'Z010' ";
        $query .= "     AND NAMECD2 = '00' ";

        return $query;
    }

    //実施日付より学期を取得
    function getSemesterSQL($data)
    {
        $query  = "select semester ";
        $query .= "from semester_mst ";
        $query .= "where year = Fiscalyear(date('".$data["EXECUTEDATE"]."')) ";
        $query .= "and semester = Y2t(date('".$data["EXECUTEDATE"]."'),'N') ";

        return $query;  
    }

    //年度＆学期
    function getYearSemesterCombo() {
        $query  = "";
        $query .= " SELECT DISTINCT ";
        $query .= "     T1.YEAR, ";
        $query .= "     T1.SEMESTER, ";
        $query .= "     L1.SEMESTERNAME ";
        $query .= " FROM ";
        $query .= "     SCH_CHR_DAT T1 ";
        $query .= "     INNER JOIN SEMESTER_MST L1 ON L1.YEAR = T1.YEAR AND L1.SEMESTER = T1.SEMESTER ";
        $query .= " ORDER BY ";
        $query .= "     T1.YEAR, ";
        $query .= "     T1.SEMESTER ";
        return $query;
    }
    //実施日付
    function getExecutedateCombo($model) {
        //変数
        $yearSemester = $model->field["YEAR_SEME"];
        //SQL
        $query  = "";
        $query .= " SELECT DISTINCT ";
        $query .= "     T1.EXECUTEDATE ";
        $query .= " FROM ";
        $query .= "     SCH_CHR_DAT T1 ";
        $query .= "     INNER JOIN SEMESTER_MST L1 ON L1.YEAR = T1.YEAR AND L1.SEMESTER = T1.SEMESTER ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR || '-' || T1.SEMESTER = '{$yearSemester}' ";
        $query .= " ORDER BY ";
        $query .= "     T1.EXECUTEDATE ";
        return $query;
    }
    //講座
    function getChaircdCombo($model) {
        //変数
        $yearSemester = $model->field["YEAR_SEME"];
        $executedate = $model->field["EXECUTEDATE"];
        //SQL
        $query  = "";
        $query .= " SELECT DISTINCT ";
        $query .= "     T1.CHAIRCD, ";
        $query .= "     L2.CHAIRNAME ";
        $query .= " FROM ";
        $query .= "     SCH_CHR_DAT T1 ";
        $query .= "     INNER JOIN SEMESTER_MST L1 ON L1.YEAR = T1.YEAR AND L1.SEMESTER = T1.SEMESTER ";
        $query .= "     INNER JOIN CHAIR_DAT L2 ON L2.YEAR = T1.YEAR AND L2.SEMESTER = T1.SEMESTER AND L2.CHAIRCD = T1.CHAIRCD ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR || '-' || T1.SEMESTER = '{$yearSemester}' ";
        if (strlen($executedate)) {
            $query .= "     AND T1.EXECUTEDATE = '{$executedate}' ";
        }
        $query .= " ORDER BY ";
        $query .= "     T1.CHAIRCD ";
        return $query;
    }
    //データ出力
    function getselectDataSQL($model) {
        //変数
        $yearSemester = $model->field["YEAR_SEME"];
        $executedate = $model->field["EXECUTEDATE"];
        $chaircd = $model->field["CHAIRCD"];
        //SQL
        $query  = "";
        $query .= " SELECT ";
        $query .= "     T1.EXECUTEDATE, ";
        $query .= "     T1.PERIODCD, ";
        $query .= "     T1.CHAIRCD, ";
        $query .= "     T1.ATTESTOR ";
        $query .= " FROM ";
        $query .= "     SCH_CHR_DAT T1 ";
        $query .= "     INNER JOIN SEMESTER_MST L1 ON L1.YEAR = T1.YEAR AND L1.SEMESTER = T1.SEMESTER ";
        $query .= "     INNER JOIN CHAIR_DAT L2 ON L2.YEAR = T1.YEAR AND L2.SEMESTER = T1.SEMESTER AND L2.CHAIRCD = T1.CHAIRCD ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR || '-' || T1.SEMESTER = '{$yearSemester}' ";
        if (strlen($executedate)) {
            $query .= "     AND T1.EXECUTEDATE = '{$executedate}' ";
        }
        if (strlen($chaircd)) {
            $query .= "     AND T1.CHAIRCD = '{$chaircd}' ";
        }
        $query .= " ORDER BY ";
        $query .= "     T1.EXECUTEDATE, ";
        $query .= "     T1.PERIODCD, ";
        $query .= "     T1.CHAIRCD ";
        return $query;
    }

    //マスタ(存在チェック用)
    function getMasterCheck($data, $chk_no)
    {
        $flg    = true;
        $query  = "";
        //条件選択
        switch ($chk_no) 
        {
            case "1":
                $where  = "chair_dat where YEAR = Fiscalyear(date('".$data["EXECUTEDATE"]."')) ";
                $where .= "and SEMESTER = Y2t(date('".$data["EXECUTEDATE"]."'),'N') ";
                $where .= "and CHAIRCD = '".$data["CHAIRCD"]."' ";
                break;
            case "2":
                $where  = "staff_mst where STAFFCD = '".$data["ATTESTOR"]."' ";
                break;
            case "3":
                $where  = "name_mst where NAMECD1='B001' AND NAMECD2 = '".$data["PERIODCD"]."' ";
                break;
            default:
                $flg = false;
                break;
        }
        if( flg ) $query = "SELECT 'X' FROM " . $where;

        return $query;
    }


    //マスタ(追加・更新・削除用)
    function getExist_Check($data)
    {
        $query  = "select * from sch_chr_dat ";
        $query .= "where EXECUTEDATE = '".$data["EXECUTEDATE"]."' ";
        $query .= "and PERIODCD = '".$data["PERIODCD"]."' ";
        $query .= "and CHAIRCD = '".$data["CHAIRCD"]."' ";

        return $query;  
    }


    //マスタデータの削除
    function deleteQueryCsv(&$db, $data)
    {
        $query  = "DELETE FROM sch_chr_dat ";
        $query .= "where EXECUTEDATE = '".$data["EXECUTEDATE"]."' ";
        $query .= "and PERIODCD = '".$data["PERIODCD"]."' ";
        $query .= "and CHAIRCD = '".$data["CHAIRCD"]."' ";

        $result = $db->query($query);
    }


    //マスタの追加（ＣＳＶデータより読込）
    function insertQueryCsv(&$db, $data, $registercd)
    {

            $data1["EXECUTEDATE"][TEXT]         = $data["EXECUTEDATE"];
            $data1["PERIODCD"][TEXT]            = $data["PERIODCD"];
            $data1["CHAIRCD"][TEXT]             = $data["CHAIRCD"];
            $data1["EXECUTED"][TEXT]            = $data["EXECUTED"];
            $data1["DATADIV"][TEXT]             = $data["DATADIV"];
            $data1["YEAR"][TEXT]                = $data["YEAR"];
            $data1["SEMESTER"][TEXT]            = $data["SEMESTER"];
            $data1["ATTESTOR"][TEXT]            = $data["ATTESTOR"];

            $data1["REGISTERCD"][TEXT]          = $registercd;
            $data1["UPDATED"][NUMBER]           = "sysdate()";

            $query = Query::insertSQL($data1, "sch_chr_dat");

            $result = $db->query($query);

    }


    //マスタの更新（ＣＳＶデータより読込）
    function updateQueryCsv(&$db, $data, $registercd)
    {

            $data1["EXECUTEDATE"][TEXT]         = $data["EXECUTEDATE"];
            $data1["PERIODCD"][TEXT]            = $data["PERIODCD"];
            $data1["CHAIRCD"][TEXT]             = $data["CHAIRCD"];
            $data1["EXECUTED"][TEXT]            = $data["EXECUTED"];
            $data1["DATADIV"][TEXT]             = $data["DATADIV"];
            $data1["YEAR"][TEXT]                = $data["YEAR"];
            $data1["SEMESTER"][TEXT]            = $data["SEMESTER"];
            $data1["ATTESTOR"][TEXT]            = $data["ATTESTOR"];

            $data1["REGISTERCD"][TEXT]          = $registercd;
            $data1["UPDATED"][NUMBER]           = "sysdate()";

            $where  = "where EXECUTEDATE = '".$data["EXECUTEDATE"]."' ";
            $where .= "and PERIODCD = '".$data["PERIODCD"]."' ";
            $where .= "and CHAIRCD = '".$data["CHAIRCD"]."' ";

            $query = Query::updateSQL($data1, "sch_chr_dat", $where);

            $result = $db->query($query);

    }


    //エラーデータの削除
    function deleteQueryErr()
    {
        $query  = "DELETE FROM w_csvmsg_dat ";
        return $query;
    }


    //エラーＤＢへの追加
    function insertQueryErr(&$db, $record_no, $check_error)
    {

            $data1["MSGROW"][NUMBER]        = $record_no;
            $data1["MSGREMARK"][TEXT]       = $check_error;

            $query = Query::insertSQL($data1, "w_csvmsg_dat");

            $result = $db->query($query);

    }


    //CVS作成用のQUERY
    function selectCsvQuery()
    {
        $query  = "select * from w_csvmsg_dat";

        return $query;
    }


    // SCH_CHR_COUNTFLGの作成
    function createSchChrCountflg(&$db, $data, $registercd, $shori_mei) {
        // SCH_CHR_COUNTFLGを作成する条件
        // 1.講座データ（CHAIR_DAT.COUNTFLG）が「集計しない（0）」「集計する（1）」
        // 2.講座データ（CHAIR_DAT.GROUPCD）が「群でない（0000）」「群（XXXX）」
        $query  = "SELECT ";
        $query .= "    GROUPCD, COUNTFLG ";
        $query .= "FROM ";
        $query .= "    CHAIR_DAT ";
        $query .= "WHERE ";
        $query .= "    YEAR='".$data["YEAR"]."' AND ";
        $query .= "    SEMESTER='".$data["SEMESTER"]."' AND ";
        $query .= "    CHAIRCD='".$data["CHAIRCD"]."' ";
        $rtnRow = $db->getRow($query,DB_FETCHMODE_ASSOC);
        if (is_array($rtnRow)) {
            // SCH_CHR_COUNTFLGを一旦削除する
            $query  = "DELETE FROM SCH_CHR_COUNTFLG ";
            $query .= "WHERE ";
            $query .= "    EXECUTEDATE='".$data["EXECUTEDATE"]."' AND ";
            $query .= "    PERIODCD='".$data["PERIODCD"]."' AND ";
            $query .= "    CHAIRCD='".$data["CHAIRCD"]."' ";
            $result = $db->query($query);
            // SCH_CHR_COUNTFLGを追加する・・・・・「更新」が実行された場合
            // 講座受講クラスデータ（CHAIR_CLS_DAT）を参照して
            // その年組数分のSCH_CHR_COUNTFLGを作成する。・・・「集計しない（0）」「集計する（1）」なSCH_CHR_COUNTFLGを作成する
            if ($shori_mei == "1") {
                $query  = "INSERT INTO SCH_CHR_COUNTFLG ";
                $query .= "SELECT ";
                $query .= "    date('".$data["EXECUTEDATE"]."') AS EXECUTEDATE, ";
                $query .= "    '".$data["PERIODCD"]."' AS PERIODCD, ";
                $query .= "    '".$data["CHAIRCD"]."' AS CHAIRCD, ";
                $query .= "    TRGTGRADE AS GRADE, ";
                $query .= "    TRGTCLASS AS HR_CLASS, ";
                $query .= "    '".$rtnRow["COUNTFLG"]."' AS COUNTFLG, ";
                $query .= "    '".$registercd."' AS REGISTERCD, ";
                $query .= "    sysdate() AS UPDATED ";
                $query .= "FROM ";
                $query .= "    CHAIR_CLS_DAT ";
                $query .= "WHERE ";
                $query .= "    YEAR='".$data["YEAR"]."' AND ";
                $query .= "    SEMESTER='".$data["SEMESTER"]."' AND ";
                // 「群（XXXX）」の場合、「CHAIR_CLS_DAT.CHAIRCD = '0000000'」
                $chaircd = ($rtnRow["GROUPCD"] != "0000") ? "0000000" : $data["CHAIRCD"];
                $query .= "    CHAIRCD='".$chaircd."' AND ";
                $query .= "    GROUPCD='".$rtnRow["GROUPCD"]."' ";
                $result = $db->query($query);
            }
        }
    }
}
?>
