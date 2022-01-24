<?php

require_once('for_php7.php');

class knjx130query extends Query
{


    //実施日付より学期を取得
    public function getSemesterSQL($data)
    {
        $query  = "select semester ";
        $query .= "from semester_mst ";
        $query .= "where year = Fiscalyear(date('".$data["EXECUTEDATE"]."')) ";
        $query .= "and semester = Y2t(date('".$data["EXECUTEDATE"]."'),'N') ";

        return $query;
    }

    //実施日付一覧
    public function getSelectFieldSQL()
    {
        $query  = "select distinct executedate ";
        $query .= "from SCH_CHR_T_DAT ";
        $query .= "order by executedate";

        return $query;
    }

    //講座一覧
    public function getSelectFieldSQL2($model)
    {
        $query  = "SELECT t1.chaircd, t2.chairname ";
        $query .= "FROM   (SELECT DISTINCT chaircd FROM SCH_CHR_T_DAT  ";
        if ($model->field["EXECUTEDATE"]!="") {
            $query .= "where executedate = '".$model->field["EXECUTEDATE"]."' ";
        }
        $query .= "		  ) t1  ";
        $query .= "left join (SELECT DISTINCT chaircd, chairname FROM chair_dat) t2 on t1.chaircd = t2.chaircd ";
        $query .= "ORDER BY t1.chaircd ";

        return $query;
    }

    //データ出力
    public function getselectDataSQL($model)
    {
        $query  = "select ";
        $query .= "		executedate, ";
        $query .= "		periodcd, ";
        $query .= "		chaircd, ";
        $query .= "		schooling_seq, ";
        $query .= "		attestor, ";
        $query .= "		remark ";
        $query .= "from SCH_CHR_T_DAT ";
        $query .= "where PERIODCD IS NOT NULL ";
        if ($model->field["EXECUTEDATE"]!="") {
            $query .= "and EXECUTEDATE = '".$model->field["EXECUTEDATE"]."' ";
        }
        if ($model->field["CHAIRCD"]!="") {
            $query .= "and CHAIRCD = '".$model->field["CHAIRCD"]."' ";
        }
        $query .= "and schooling_seq IS NOT NULL ";
        $query .= "order by executedate,periodcd,chaircd,schooling_seq";

        return $query;
    }

    //マスタ(存在チェック用)
    public function getMasterCheck($data, $chk_no)
    {
        $flg = true;
        $query = "";
        //条件選択
        switch ($chk_no) {
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
        if ($flg) {
            $query = "SELECT 'X' FROM " . $where;
        }

        return $query;
    }


    //マスタ(追加・更新・削除用)
    public function getExistCheck($data)
    {
        $query  = "select * from SCH_CHR_T_DAT ";
        $query .= "where EXECUTEDATE = '".$data["EXECUTEDATE"]."' ";
        $query .= "and PERIODCD = '".$data["PERIODCD"]."' ";
        $query .= "and CHAIRCD = '".$data["CHAIRCD"]."' ";
        $query .= "and SCHOOLING_SEQ = ".$data["SCHOOLING_SEQ"]." ";

        return $query;
    }


    //マスタデータの削除
    public function deleteQueryCsv(&$db, $data)
    {
        $query  = "DELETE FROM SCH_CHR_T_DAT ";
        $query .= "where EXECUTEDATE = '".$data["EXECUTEDATE"]."' ";
        $query .= "and PERIODCD = '".$data["PERIODCD"]."' ";
        $query .= "and CHAIRCD = '".$data["CHAIRCD"]."' ";
        $query .= "and SCHOOLING_SEQ = ".$data["SCHOOLING_SEQ"]." ";

        $result = $db->query($query);
    }


    //マスタの追加（ＣＳＶデータより読込）
    public function insertQueryCsv(&$db, $data, $registercd)
    {
        $data1["EXECUTEDATE"][TEXT]     = $data["EXECUTEDATE"];
        $data1["PERIODCD"][TEXT]        = $data["PERIODCD"];
        $data1["CHAIRCD"][TEXT]         = $data["CHAIRCD"];
        $data1["SCHOOLING_SEQ"][NUMBER] = $data["SCHOOLING_SEQ"];
        $data1["EXECUTED"][TEXT]        = $data["EXECUTED"];
        $data1["DATADIV"][TEXT]         = $data["DATADIV"];
        $data1["YEAR"][TEXT]            = common::DateConv1(str_replace("-", "/", $data["EXECUTEDATE"]), 12);
        $data1["SEMESTER"][TEXT]        = $data["SEMESTER"];
        $data1["ATTESTOR"][TEXT]        = $data["ATTESTOR"];
        $data1["REMARK"][TEXT]          = $data["REMARK"];

        $data1["REGISTERCD"][TEXT]      = $registercd;
        $data1["UPDATED"][NUMBER]       = "sysdate()";

        $query = Query::insertSQL($data1, "SCH_CHR_T_DAT");

        $result = $db->query($query);
    }


    //マスタの更新（ＣＳＶデータより読込）
    public function updateQueryCsv(&$db, $data, $registercd)
    {
        $data1["EXECUTEDATE"][TEXT]     = $data["EXECUTEDATE"];
        $data1["PERIODCD"][TEXT]        = $data["PERIODCD"];
        $data1["CHAIRCD"][TEXT]         = $data["CHAIRCD"];
        $data1["SCHOOLING_SEQ"][NUMBER] = $data["SCHOOLING_SEQ"];
        $data1["EXECUTED"][TEXT]        = $data["EXECUTED"];
        $data1["DATADIV"][TEXT]         = $data["DATADIV"];
        $data1["YEAR"][TEXT]            = common::DateConv1(str_replace("-", "/", $data["EXECUTEDATE"]), 12);
        $data1["SEMESTER"][TEXT]        = $data["SEMESTER"];
        $data1["ATTESTOR"][TEXT]        = $data["ATTESTOR"];
        $data1["REMARK"][TEXT]          = $data["REMARK"];

        $data1["REGISTERCD"][TEXT]      = $registercd;
        $data1["UPDATED"][NUMBER]       = "sysdate()";

        $where  = "where EXECUTEDATE = '".$data["EXECUTEDATE"]."' ";
        $where .= "and PERIODCD = '".$data["PERIODCD"]."' ";
        $where .= "and CHAIRCD = '".$data["CHAIRCD"]."' ";
        $where .= "and SCHOOLING_SEQ = ".$data["SCHOOLING_SEQ"]." ";

        $query = Query::updateSQL($data1, "SCH_CHR_T_DAT", $where);

        $result = $db->query($query);
    }

    //エラーＤＢへの追加
    public function insertQueryErr(&$db, $record_no, $check_error)
    {
        $data1["PROGRAMID"][TEXT] = PROGRAMID;
        $data1["MSGROW"][NUMBER]  = $record_no;
        $data1["MSGREMARK"][TEXT] = $check_error;

        $query = Query::insertSQL($data1, "W_CSVMSG_PRG_DAT");

        $result = $db->query($query);
    }
}
