<?php

require_once('for_php7.php');
class knjx070aquery extends Query
{
    public function getSecurityHigh()
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     MENU_HIGH_SECURITY_MST ";
        $query .= " WHERE ";
        $query .= "     PROGRAMID = 'KNJX070A' ";
        $query .= "     AND INVALID_FLG = '0' ";

        return $query;
    }

    public function getSchoolCd()
    {
        $query  = " SELECT ";
        $query .= "     NAME2 ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = 'Z010' ";
        $query .= "     AND NAMECD2 = '00' ";

        return $query;
    }

    //年度一覧
    public function getSelectFieldSQL()
    {
        $query  = "select distinct t1.year,t1.semester,t2.semestername ";
        $query .= "from chair_stf_dat t1 ";
        $query .= "     inner join semester_mst t2 on t1.year=t2.year and t1.semester=t2.semester ";
        $query .= "order by t1.year,t1.semester";

        return $query;
    }

    //講座一覧
    public function getSelectFieldSQL2($model)
    {
        $query  = "select distinct t1.chaircd,t2.chairname ";
        $query .= "from chair_stf_dat t1,chair_dat t2 ";
        $query .= "where t1.YEAR || t1.SEMESTER = '".$model->field["YEAR"]."' ";
        $query .= "and t1.year = t2.year ";
        $query .= "and t1.semester = t2.semester ";
        $query .= "and t1.chaircd = t2.chaircd ";
        $query .= "order by t1.chaircd";

        return $query;
    }

    //職員一覧
    public function getSelectFieldSQL3($model)
    {
        $query  = "select distinct t1.staffcd,t2.staffname ";
        $query .= "from chair_stf_dat t1,staff_mst t2 ";
        $query .= "where t1.YEAR || t1.SEMESTER = '".$model->field["YEAR"]."' ";
        $query .= "and t1.staffcd = t2.staffcd ";
        $query .= "order by t1.staffcd";

        return $query;
    }

    //データ出力
    public function getselectDataSQL($model)
    {
        $query  = "select ";
        $query .= " T1.YEAR, ";
        $query .= " T1.SEMESTER, ";
        $query .= " T1.CHAIRCD, ";
        $query .= " L1.CHAIRNAME, ";
        $query .= " T1.STAFFCD, ";
        $query .= " L2.STAFFNAME, ";
        $query .= " T1.CHARGEDIV, ";
        $query .= " '".$model->lastColumn."' AS ".$model->lastColumn." ";
        $query .= "from ";
        $query .= " chair_stf_dat T1 ";
        $query .= " LEFT JOIN CHAIR_DAT L1 ON L1.YEAR = T1.YEAR AND L1.SEMESTER = T1.SEMESTER AND L1.CHAIRCD = T1.CHAIRCD ";
        $query .= " LEFT JOIN STAFF_MST L2 ON L2.STAFFCD = T1.STAFFCD ";
        $query .= "where T1.YEAR || T1.SEMESTER = '".$model->field["YEAR"]."' ";
        if ($model->field["CHAIRCD"]!="") {
            $query .= "and T1.CHAIRCD = '".$model->field["CHAIRCD"]."' ";
        }
        if ($model->field["STAFFCD"]!="") {
            $query .= "and T1.STAFFCD = '".$model->field["STAFFCD"]."' ";
        }
        $query .= "order by T1.chaircd,T1.staffcd";

        return $query;
    }

    //マスタ(存在チェック用)
    public function getMasterCheck($data, $chk_no)
    {
        $flg    = true;
        $query  = "";
        //条件選択
        switch ($chk_no) {
            case "1":
                $where  = "chair_dat where YEAR = '".$data["YEAR"]."' ";
                $where .= "and SEMESTER = '".$data["SEMESTER"]."' ";
                $where .= "and CHAIRCD = '".$data["CHAIRCD"]."' ";
                break;
            case "2":
                $where = "staff_mst where STAFFCD = '".$data["STAFFCD"]."' ";
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
        $query  = "select 'X' from chair_stf_dat ";
        $query .= "where YEAR = '".$data["YEAR"]."' ";
        $query .= "and SEMESTER = '".$data["SEMESTER"]."' ";
        $query .= "and CHAIRCD = '".$data["CHAIRCD"]."' ";
        $query .= "and STAFFCD = '".$data["STAFFCD"]."' ";

        return $query;
    }


    //マスタデータの削除
    public function deleteQueryCsv(&$db, $data)
    {
        $query  = "DELETE FROM chair_stf_dat ";
        $query .= "where YEAR = '".$data["YEAR"]."' ";
        $query .= "and SEMESTER = '".$data["SEMESTER"]."' ";
        $query .= "and CHAIRCD = '".$data["CHAIRCD"]."' ";
        $query .= "and STAFFCD = '".$data["STAFFCD"]."' ";

        $result = $db->query($query);
    }


    //マスタの追加（ＣＳＶデータより読込）
    public function insertQueryCsv(&$db, $data, $registercd)
    {
        $data1["YEAR"][TEXT]                = $data["YEAR"];
        $data1["SEMESTER"][TEXT]            = $data["SEMESTER"];
        $data1["CHAIRCD"][TEXT]             = $data["CHAIRCD"];
        $data1["STAFFCD"][TEXT]             = $data["STAFFCD"];
        $data1["CHARGEDIV"][NUMBER]         = $data["CHARGEDIV"];

        $data1["REGISTERCD"][TEXT]          = $registercd;
        $data1["UPDATED"][NUMBER]           = "sysdate()";

        $query = Query::insertSQL($data1, "chair_stf_dat");

        $result = $db->query($query);
    }


    //マスタの更新（ＣＳＶデータより読込）
    public function updateQueryCsv(&$db, $data, $registercd)
    {
        $data1["YEAR"][TEXT]                = $data["YEAR"];
        $data1["SEMESTER"][TEXT]            = $data["SEMESTER"];
        $data1["CHAIRCD"][TEXT]             = $data["CHAIRCD"];
        $data1["STAFFCD"][TEXT]             = $data["STAFFCD"];
        $data1["CHARGEDIV"][NUMBER]         = $data["CHARGEDIV"];

        $data1["REGISTERCD"][TEXT]          = $registercd;
        $data1["UPDATED"][NUMBER]           = "sysdate()";

        $where  = "where YEAR = '".$data["YEAR"]."' ";
        $where .= "and SEMESTER = '".$data["SEMESTER"]."' ";
        $where .= "and CHAIRCD = '".$data["CHAIRCD"]."' ";
        $where .= "and STAFFCD = '".$data["STAFFCD"]."' ";

        $query = Query::updateSQL($data1, "chair_stf_dat", $where);

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
