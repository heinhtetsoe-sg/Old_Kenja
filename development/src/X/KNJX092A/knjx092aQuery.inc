<?php

require_once('for_php7.php');
class knjx092aquery extends Query
{
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

    public function getSecurityHigh()
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     MENU_HIGH_SECURITY_MST ";
        $query .= " WHERE ";
        $query .= "     PROGRAMID = 'KNJX092A' ";
        $query .= "     AND INVALID_FLG = '0' ";

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

    //施設一覧
    public function getSelectFieldSQL3($model)
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     T1.FACCD, ";
        $query .= "     T2.FACILITYNAME ";
        $query .= " FROM ";
        $query .= "     CHAIR_FAC_DAT T1, ";
        $query .= "     FACILITY_MST T2 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR || T1.SEMESTER = '".$model->field["YEAR"]."' AND ";
        $query .= "     T1.FACCD = T2.FACCD ";
        $query .= " ORDER BY ";
        $query .= "     T1.FACCD ";

        return $query;
    }

    //データ出力
    public function getselectDataSQL($model)
    {
        $query  = " SELECT ";
        $query .= "     T1.YEAR, ";
        $query .= "     T1.SEMESTER, ";
        $query .= "     T1.CHAIRCD, ";
        $query .= "     L1.CHAIRNAME, ";
        $query .= "     T1.FACCD, ";
        $query .= "     '".$model->lastColumn."' AS ".$model->lastColumn." ";
        $query .= " FROM ";
        $query .= "     CHAIR_FAC_DAT T1 ";
        $query .= " LEFT JOIN CHAIR_DAT L1 ON L1.YEAR = T1.YEAR AND L1.SEMESTER = T1.SEMESTER AND L1.CHAIRCD = T1.CHAIRCD ";
        $query .= "where T1.YEAR || T1.SEMESTER = '".$model->field["YEAR"]."' ";
        if ($model->field["CHAIRCD"] != "") {
            $query .= " AND T1.CHAIRCD = '".$model->field["CHAIRCD"]."' ";
        }
        if ($model->field["FACCD"] != "") {
            $query .= " AND T1.FACCD = '".$model->field["FACCD"]."' ";
        }
        $query .= " ORDER BY T1.CHAIRCD, T1.FACCD";

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
                $where  = " CHAIR_DAT WHERE YEAR = '".$data["YEAR"]."' ";
                $where .= " AND SEMESTER = '".$data["SEMESTER"]."' ";
                $where .= " AND CHAIRCD = '".$data["CHAIRCD"]."' ";
                break;
            case "2":
                $where  = " FACILITY_MST WHERE FACCD = '".$data["FACCD"]."' ";
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
        $query  = " SELECT 'X' FROM ";
        $query .= "     CHAIR_FAC_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR        = '".$data["YEAR"]."' AND ";
        $query .= "     SEMESTER    = '".$data["SEMESTER"]."' AND ";
        $query .= "     CHAIRCD     = '".$data["CHAIRCD"]."' AND ";
        $query .= "     FACCD       = '".$data["FACCD"]."' ";

        return $query;
    }

    //マスタデータの削除
    public function deleteQueryCsv(&$db, $data)
    {
        $query  = " DELETE FROM ";
        $query .= "     CHAIR_FAC_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR        = '".$data["YEAR"]."' AND ";
        $query .= "     SEMESTER    = '".$data["SEMESTER"]."' AND ";
        $query .= "     CHAIRCD     = '".$data["CHAIRCD"]."' AND ";
        $query .= "     FACCD       = '".$data["FACCD"]."' ";

        $result = $db->query($query);
    }

    //マスタの追加（ＣＳＶデータより読込）
    public function insertQueryCsv(&$db, $data)
    {
        $data1["YEAR"][TEXT]        = $data["YEAR"];
        $data1["SEMESTER"][TEXT]    = $data["SEMESTER"];
        $data1["CHAIRCD"][TEXT]     = $data["CHAIRCD"];
        $data1["FACCD"][TEXT]       = $data["FACCD"];
        $data1["REGISTERCD"][TEXT]  = STAFFCD;
        $data1["UPDATED"][NUMBER]   = "sysdate()";

        $query = Query::insertSQL($data1, "CHAIR_FAC_DAT");
        $result = $db->query($query);
    }

    //マスタの更新（ＣＳＶデータより読込）
    public function updateQueryCsv(&$db, $data)
    {
        $data1["YEAR"][TEXT]        = $data["YEAR"];
        $data1["SEMESTER"][TEXT]    = $data["SEMESTER"];
        $data1["CHAIRCD"][TEXT]     = $data["CHAIRCD"];
        $data1["FACCD"][TEXT]       = $data["FACCD"];
        $data1["REGISTERCD"][TEXT]  = STAFFCD;
        $data1["UPDATED"][NUMBER]   = "sysdate()";

        $where  = " WHERE ";
        $where .= "     YEAR        = '".$data["YEAR"]."' AND ";
        $where .= "     SEMESTER    = '".$data["SEMESTER"]."' AND ";
        $where .= "     CHAIRCD     = '".$data["CHAIRCD"]."' AND ";
        $where .= "     FACCD       = '".$data["FACCD"]."' ";

        $query = Query::updateSQL($data1, "CHAIR_FAC_DAT", $where);
        $result = $db->query($query);
    }

    //エラーＤＢへの追加
    public function insertQueryErr(&$db, $record_no, $check_error)
    {
        $data1["PROGRAMID"][TEXT]       = PROGRAMID;
        $data1["MSGROW"][NUMBER]        = $record_no;
        $data1["MSGREMARK"][TEXT]       = $check_error;

        $query = Query::insertSQL($data1, "W_CSVMSG_PRG_DAT");
        $result = $db->query($query);
    }
}
