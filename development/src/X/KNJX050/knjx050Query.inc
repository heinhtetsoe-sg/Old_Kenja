<?php

require_once('for_php7.php');

class knjx050query extends Query
{
    public function getSecurityHigh()
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     MENU_HIGH_SECURITY_MST ";
        $query .= " WHERE ";
        $query .= "     PROGRAMID = 'KNJX050' ";
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
    public function getSelectFieldSQL($model)
    {
        $query  = "select distinct t1.year,t1.semester,t2.semestername ";
        $query .= "from schreg_regd_hdat t1 ";
        $query .= "     inner join semester_mst t2 on t1.year=t2.year and t1.semester=t2.semester ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= " INNER JOIN SCHREG_REGD_GDAT GDAT ON GDAT.YEAR = T1.YEAR AND GDAT.GRADE = T1.GRADE ";
                $query .= " AND GDAT.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind), "','")."') ";
            }
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " INNER JOIN SCHREG_REGD_GDAT GDAT ON GDAT.YEAR = T1.YEAR AND GDAT.GRADE = T1.GRADE ";
            $query .= " AND GDAT.SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= "order by t1.year,t1.semester";

        return $query;
    }

    //年組一覧
    public function getSelectFieldSQL2($model)
    {
        $query  = "select distinct t1.grade,hr_class,hr_name ";
        $query .= "from schreg_regd_hdat t1 ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= " INNER JOIN SCHREG_REGD_GDAT GDAT ON GDAT.YEAR = T1.YEAR AND GDAT.GRADE = T1.GRADE ";
                $query .= " AND GDAT.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind), "','")."') ";
            }
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " INNER JOIN SCHREG_REGD_GDAT GDAT ON GDAT.YEAR = T1.YEAR AND GDAT.GRADE = T1.GRADE ";
            $query .= " AND GDAT.SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= "where t1.YEAR || SEMESTER = '".$model->field["YEAR"]."' ";
        $query .= "order by t1.grade,hr_class";

        return $query;
    }

    //データ出力
    public function getselectDataSQL($model)
    {
        $query  = "select t1.* from schreg_regd_dat t1 ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= " INNER JOIN SCHREG_REGD_GDAT GDAT ON GDAT.YEAR = T1.YEAR AND GDAT.GRADE = T1.GRADE ";
                $query .= " AND GDAT.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind), "','")."') ";
            }
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " INNER JOIN SCHREG_REGD_GDAT GDAT ON GDAT.YEAR = T1.YEAR AND GDAT.GRADE = T1.GRADE ";
            $query .= " AND GDAT.SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= "where t1.YEAR || SEMESTER = '".$model->field["YEAR"]."' ";
        if ($model->field["GRADE_HR_CLASS"]!="") {
            $query .= "and t1.GRADE || HR_CLASS = '".$model->field["GRADE_HR_CLASS"]."' ";
        }
        $query .= "order by t1.grade,hr_class,attendno";

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
                $where = "schreg_base_mst where SCHREGNO = '".$data["SCHREGNO"]."' ";
                break;
            case "2":
                $where  = "schreg_regd_hdat ";
                $where .= "where    YEAR        = '".$data["YEAR"]."' ";
                $where .= "     and SEMESTER    = '".$data["SEMESTER"]."' ";
                $where .= "     and GRADE       = '".$data["GRADE"]."' ";
                $where .= "     and HR_CLASS    = '".$data["HR_CLASS"]."' ";
                break;
            case "3":
                $where = "course_mst where COURSECD = '".$data["COURSECD"]."' ";
                break;
            case "4":
                $where = "major_mst where COURSECD = '".$data["COURSECD"]."' and MAJORCD = '".$data["MAJORCD"]."' ";
                break;
            case "5":
                $where = "coursecode_mst where COURSECODE = '".$data["COURSECODE"]."' ";
                break;
            case "6":
                $where  = "semester_mst ";
                $where .= "where    YEAR        = '".$data["YEAR"]."' ";
                $where .= "     and SEMESTER    = '".$data["SEMESTER"]."' ";
                $where .= "     and SEMESTER   <> '9' ";
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
        $query  = "select 'X' from schreg_regd_dat ";
        $query .= "where SCHREGNO = '".$data["SCHREGNO"]."' ";
        $query .= "and YEAR = '".$data["YEAR"]."' ";
        $query .= "and SEMESTER = '".$data["SEMESTER"]."' ";

        return $query;
    }


    //マスタデータの削除
    public function deleteQueryCsv(&$db, $data)
    {
        $query  = "DELETE FROM schreg_regd_dat ";
        $query .= "where SCHREGNO = '".$data["SCHREGNO"]."' ";
        $query .= "and YEAR = '".$data["YEAR"]."' ";
        $query .= "and SEMESTER = '".$data["SEMESTER"]."' ";

        $result = $db->query($query);
    }


    //マスタの追加（ＣＳＶデータより読込）
    public function insertQueryCsv(&$db, $data, $registercd)
    {
        $data1["SCHREGNO"][TEXT]            = $data["SCHREGNO"];
        $data1["YEAR"][TEXT]                = $data["YEAR"];
        $data1["SEMESTER"][TEXT]            = $data["SEMESTER"];
        $data1["GRADE"][TEXT]               = $data["GRADE"];
        $data1["HR_CLASS"][TEXT]            = $data["HR_CLASS"];
        $data1["ATTENDNO"][TEXT]            = $data["ATTENDNO"];
        $data1["ANNUAL"][TEXT]              = $data["ANNUAL"];
        $data1["SEAT_ROW"][TEXT]            = $data["SEAT_ROW"];
        $data1["SEAT_COL"][TEXT]            = $data["SEAT_COL"];
        $data1["COURSECD"][TEXT]            = $data["COURSECD"];
        $data1["MAJORCD"][TEXT]             = $data["MAJORCD"];
        $data1["COURSECODE"][TEXT]          = $data["COURSECODE"];

        $data1["REGISTERCD"][TEXT]          = $registercd;
        $data1["UPDATED"][NUMBER]           = "sysdate()";

        $query = Query::insertSQL($data1, "schreg_regd_dat");

        $result = $db->query($query);
    }


    //マスタの更新（ＣＳＶデータより読込）
    public function updateQueryCsv(&$db, $data, $registercd)
    {
        $data1["SCHREGNO"][TEXT]            = $data["SCHREGNO"];
        $data1["YEAR"][TEXT]                = $data["YEAR"];
        $data1["SEMESTER"][TEXT]            = $data["SEMESTER"];
        $data1["GRADE"][TEXT]               = $data["GRADE"];
        $data1["HR_CLASS"][TEXT]            = $data["HR_CLASS"];
        $data1["ATTENDNO"][TEXT]            = $data["ATTENDNO"];
        $data1["ANNUAL"][TEXT]              = $data["ANNUAL"];
        $data1["SEAT_ROW"][TEXT]            = $data["SEAT_ROW"];
        $data1["SEAT_COL"][TEXT]            = $data["SEAT_COL"];
        $data1["COURSECD"][TEXT]            = $data["COURSECD"];
        $data1["MAJORCD"][TEXT]             = $data["MAJORCD"];
        $data1["COURSECODE"][TEXT]          = $data["COURSECODE"];

        $data1["REGISTERCD"][TEXT]          = $registercd;
        $data1["UPDATED"][NUMBER]           = "sysdate()";

        $where  = "where SCHREGNO = '".$data["SCHREGNO"]."' ";
        $where .= "and YEAR = '".$data["YEAR"]."' ";
        $where .= "and SEMESTER = '".$data["SEMESTER"]."' ";
        $query = Query::updateSQL($data1, "schreg_regd_dat", $where);

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
