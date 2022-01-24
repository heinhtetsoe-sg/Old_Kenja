<?php

require_once('for_php7.php');

class knjx110query extends Query
{
    public function getSecurityHigh()
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     MENU_HIGH_SECURITY_MST ";
        $query .= " WHERE ";
        $query .= "     PROGRAMID = 'KNJX110' ";
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
    public function getSelectFieldSQL($model)
    {
        $query  = "select distinct T1.executedate ";
        $query .= "from sch_fac_dat T1 ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= "      INNER JOIN CHAIR_DAT CHAIR ON T1.YEAR = CHAIR.YEAR ";
                $query .= "           AND T1.SEMESTER = CHAIR.SEMESTER ";
                $query .= "           AND T1.CHAIRCD = CHAIR.CHAIRCD ";
                $query .= "           AND CHAIR.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind), "','")."') ";
            }
        } elseif ($model->Properties["useSchool_KindField"] == "1") {
            $query .= "      INNER JOIN CHAIR_DAT CHAIR ON T1.YEAR = CHAIR.YEAR ";
            $query .= "           AND T1.SEMESTER = CHAIR.SEMESTER ";
            $query .= "           AND T1.CHAIRCD = CHAIR.CHAIRCD ";
            $query .= "           AND CHAIR.SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= "order by T1.executedate";

        return $query;
    }

    //講座一覧
    public function getSelectFieldSQL2($model)
    {
        $query  = "SELECT t1.chaircd, t2.chairname ";
        $query .= "FROM   (SELECT DISTINCT T1.chaircd FROM sch_fac_dat T1  ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= "      INNER JOIN CHAIR_DAT CHAIR ON T1.YEAR = CHAIR.YEAR ";
                $query .= "           AND T1.SEMESTER = CHAIR.SEMESTER ";
                $query .= "           AND T1.CHAIRCD = CHAIR.CHAIRCD ";
                $query .= "           AND CHAIR.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind), "','")."') ";
            }
        } elseif ($model->Properties["useSchool_KindField"] == "1") {
            $query .= "      INNER JOIN CHAIR_DAT CHAIR ON T1.YEAR = CHAIR.YEAR ";
            $query .= "           AND T1.SEMESTER = CHAIR.SEMESTER ";
            $query .= "           AND T1.CHAIRCD = CHAIR.CHAIRCD ";
            $query .= "           AND CHAIR.SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        if ($model->field["EXECUTEDATE"]!="") {
            $query .= "where T1.executedate = '".$model->field["EXECUTEDATE"]."' ";
        }
        $query .= "       ) t1  ";
        $query .= "left join (SELECT DISTINCT chaircd, chairname FROM chair_dat) t2 on t1.chaircd = t2.chaircd ";
        $query .= "ORDER BY t1.chaircd ";

        return $query;
    }

    //施設一覧
    public function getSelectFieldSQL3($model)
    {
        $query  = "select distinct t1.faccd,t2.facilityname ";
        $query .= "from sch_fac_dat t1 ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= "      INNER JOIN CHAIR_DAT CHAIR ON t1.YEAR = CHAIR.YEAR ";
                $query .= "           AND t1.SEMESTER = CHAIR.SEMESTER ";
                $query .= "           AND t1.CHAIRCD = CHAIR.CHAIRCD ";
                $query .= "           AND CHAIR.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind), "','")."') ";
            }
        } elseif ($model->Properties["useSchool_KindField"] == "1") {
            $query .= "      INNER JOIN CHAIR_DAT CHAIR ON t1.YEAR = CHAIR.YEAR ";
            $query .= "           AND t1.SEMESTER = CHAIR.SEMESTER ";
            $query .= "           AND t1.CHAIRCD = CHAIR.CHAIRCD ";
            $query .= "           AND CHAIR.SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= " , facility_mst t2 ";
        $query .= "where t1.faccd = t2.faccd ";
        if ($model->field["EXECUTEDATE"]!="") {
            $query .= "and t1.executedate = '".$model->field["EXECUTEDATE"]."' ";
        }
        $query .= "order by t1.faccd";

        return $query;
    }

    //データ出力
    public function getselectDataSQL($model)
    {
        $query  = "select ";
        $query .= "     t1.executedate, ";
        $query .= "     t1.periodcd, ";
        $query .= "     t1.chaircd, ";
        $query .= "     t1.faccd ";
        $query .= "from sch_fac_dat t1 ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= "      INNER JOIN CHAIR_DAT CHAIR ON t1.YEAR = CHAIR.YEAR ";
                $query .= "           AND t1.SEMESTER = CHAIR.SEMESTER ";
                $query .= "           AND t1.CHAIRCD = CHAIR.CHAIRCD ";
                $query .= "           AND CHAIR.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind), "','")."') ";
            }
        } elseif ($model->Properties["useSchool_KindField"] == "1") {
            $query .= "      INNER JOIN CHAIR_DAT CHAIR ON t1.YEAR = CHAIR.YEAR ";
            $query .= "           AND t1.SEMESTER = CHAIR.SEMESTER ";
            $query .= "           AND t1.CHAIRCD = CHAIR.CHAIRCD ";
            $query .= "           AND CHAIR.SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= "where t1.PERIODCD IS NOT NULL ";
        if ($model->field["EXECUTEDATE"]!="") {
            $query .= "and t1.EXECUTEDATE = '".$model->field["EXECUTEDATE"]."' ";
        }
        if ($model->field["CHAIRCD"]!="") {
            $query .= "and t1.CHAIRCD = '".$model->field["CHAIRCD"]."' ";
        }
        if ($model->field["FACCD"]!="") {
            $query .= "and t1.FACCD = '".$model->field["FACCD"]."' ";
        }
        $query .= "order by t1.executedate,t1.periodcd,t1.chaircd,t1.faccd";

        return $query;
    }

    //マスタ(存在チェック用)
    public function getMasterCheck($model, $data, $chk_no)
    {
        $flg    = true;
        $query  = "";
        //条件選択
        switch ($chk_no) {
            case "1":
                $where  = "chair_dat where YEAR = Fiscalyear(date('".$data["EXECUTEDATE"]."')) ";
                $where .= "and SEMESTER = Y2t(date('".$data["EXECUTEDATE"]."'),'N') ";
                $where .= "and CHAIRCD = '".$data["CHAIRCD"]."' ";
                if ($model->Properties["use_prg_schoolkind"] == "1") {
                    if ($model->selectSchoolKind) {
                        $where .= "AND SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind), "','")."') ";
                    }
                } elseif ($model->Properties["useSchool_KindField"] == "1") {
                    $where .= "AND SCHOOL_KIND = '".SCHOOLKIND."' ";
                }
                break;
            case "2":
                $where  = "sch_chr_dat where EXECUTEDATE = '".$data["EXECUTEDATE"]."' ";
                $where .= "and PERIODCD = '".$data["PERIODCD"]."' ";
                $where .= "and CHAIRCD = '".$data["CHAIRCD"]."' ";
                break;
            case "3":
                $where  = "facility_mst where FACCD = '".$data["FACCD"]."' ";
                break;
            case "4":
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
        $query  = "select 'X' from sch_fac_dat ";
        $query .= "where EXECUTEDATE = '".$data["EXECUTEDATE"]."' ";
        $query .= "and PERIODCD = '".$data["PERIODCD"]."' ";
        $query .= "and CHAIRCD = '".$data["CHAIRCD"]."' ";
        $query .= "and FACCD = '".$data["FACCD"]."' ";

        return $query;
    }


    //マスタデータの削除
    public function deleteQueryCsv(&$db, $data)
    {
        $query  = "DELETE FROM sch_fac_dat ";
        $query .= "where EXECUTEDATE = '".$data["EXECUTEDATE"]."' ";
        $query .= "and PERIODCD = '".$data["PERIODCD"]."' ";
        $query .= "and CHAIRCD = '".$data["CHAIRCD"]."' ";
        $query .= "and FACCD = '".$data["FACCD"]."' ";

        $result = $db->query($query);
    }


    //マスタの追加（ＣＳＶデータより読込）
    public function insertQueryCsv(&$db, $data, $registercd)
    {
        $data1["EXECUTEDATE"][TEXT]         = $data["EXECUTEDATE"];
        $data1["PERIODCD"][TEXT]            = $data["PERIODCD"];
        $data1["CHAIRCD"][TEXT]             = $data["CHAIRCD"];
        $data1["FACCD"][TEXT]               = $data["FACCD"];
        $data1["YEAR"][TEXT]                = common::DateConv1(str_replace("-", "/", $data["EXECUTEDATE"]), 12);
        $data1["SEMESTER"][TEXT]            = $data["SEMESTER"];

        $data1["REGISTERCD"][TEXT]          = $registercd;
        $data1["UPDATED"][NUMBER]           = "sysdate()";

        $query = Query::insertSQL($data1, "sch_fac_dat");

        $result = $db->query($query);
    }


    //マスタの更新（ＣＳＶデータより読込）
    public function updateQueryCsv(&$db, $data, $registercd)
    {
        $data1["EXECUTEDATE"][TEXT]         = $data["EXECUTEDATE"];
        $data1["PERIODCD"][TEXT]            = $data["PERIODCD"];
        $data1["CHAIRCD"][TEXT]             = $data["CHAIRCD"];
        $data1["FACCD"][TEXT]               = $data["FACCD"];
        $data1["YEAR"][TEXT]                = common::DateConv1(str_replace("-", "/", $data["EXECUTEDATE"]), 12);
        $data1["SEMESTER"][TEXT]            = $data["SEMESTER"];

        $data1["REGISTERCD"][TEXT]          = $registercd;
        $data1["UPDATED"][NUMBER]           = "sysdate()";

        $where  = "where EXECUTEDATE = '".$data["EXECUTEDATE"]."' ";
        $where .= "and PERIODCD = '".$data["PERIODCD"]."' ";
        $where .= "and CHAIRCD = '".$data["CHAIRCD"]."' ";
        $where .= "and FACCD = '".$data["FACCD"]."' ";

        $query = Query::updateSQL($data1, "sch_fac_dat", $where);

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
