<?php

require_once('for_php7.php');

class knjx040query extends Query
{
    public function getSecurityHigh()
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     MENU_HIGH_SECURITY_MST ";
        $query .= " WHERE ";
        $query .= "     PROGRAMID = 'KNJX040' ";
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

    //マスタ(存在チェック用)
    public function getMasterCheck($table, $where)
    {
        $query = "SELECT 'X' FROM {$table} {$where}";

        return $query;
    }


    //マスタ取得SQL
    public function getMasterSQL($table, $where)
    {
        $query = "SELECT * FROM " . $table . " " . $where;

        return $query;
    }


    //年度一覧取得SQL
    public function getSelectFieldSQL()
    {
        $query  = "select distinct t1.year,t1.semester,t2.semestername ";
        $query .= "from schreg_regd_hdat t1 ";
        $query .= "     inner join semester_mst t2 on t1.year=t2.year and t1.semester=t2.semester ";
        $query .= "order by t1.year,t1.semester";

        return $query;
    }

    //データ出力
    public function getselectDataSQL($model)
    {
        $query  = " SELECT ";
        $query .= "     T1.YEAR, ";
        $query .= "     T1.SEMESTER, ";
        $query .= "     T1.GRADE, ";
        $query .= "     T1.HR_CLASS, ";
        $query .= "     T1.HR_NAME, ";
        $query .= "     T1.HR_NAMEABBV, ";
        if ($model->useSchregRegdHdat == '1') { //新しいschreg_regd_hdatのテーブルであるならば
            $query .= " T1.GRADE_NAME, ";
            $query .= " T1.HR_CLASS_NAME1, ";
            $query .= " T1.HR_CLASS_NAME2, ";
        }
        $query .= "     T1.HR_FACCD, ";
        $query .= "     T1.TR_CD1, ";
        $query .= "     T1.TR_CD2, ";
        $query .= "     T1.TR_CD3, ";
        $query .= "     T1.SUBTR_CD1, ";
        $query .= "     T1.SUBTR_CD2, ";
        $query .= "     T1.SUBTR_CD3 ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_HDAT T1, ";
        $query .= "     SCHREG_REGD_GDAT T2 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR || T1.SEMESTER = '{$model->field["YEAR"]}' AND ";
        $query .= "     T1.YEAR     = T2.YEAR AND ";
        $query .= "     T1.GRADE    = T2.GRADE ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= " AND T2.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind), "','")."') ";
            }
        } elseif ($model->Properties["useSchool_KindField"] == "1") {
            $query .= " AND T2.SCHOOL_KIND = '" .SCHOOLKIND ."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     T1.GRADE,T1.HR_CLASS";

        return $query;
    }


    //マスタデータの削除
    public function deleteQueryCsv(&$db, $data)
    {
        $query  = " DELETE ";
        $query .= "     FROM SCHREG_REGD_HDAT ";
        $query .= " WHERE ";
        $query .= "         YEAR     = '{$data["YEAR"]}'     ";
        $query .= "     AND SEMESTER = '{$data["SEMESTER"]}' ";
        $query .= "     AND GRADE    = '{$data["GRADE"]}'    ";
        $query .= "     AND HR_CLASS = '{$data["HR_CLASS"]}' ";

        $result = $db->query($query);
    }

    //職員クラス履歴データ削除
    public function deleteStaffClassHistDat(&$db, $data)
    {
        $query  = " DELETE ";
        $query .= "     FROM STAFF_CLASS_HIST_DAT ";
        $query .= " WHERE ";
        $query .= "         YEAR     = '{$data["YEAR"]}'     ";
        $query .= "     AND SEMESTER = '{$data["SEMESTER"]}' ";
        $query .= "     AND GRADE    = '{$data["GRADE"]}'    ";
        $query .= "     AND HR_CLASS = '{$data["HR_CLASS"]}' ";

        $result = $db->query($query);
    }

    //マスタの追加（ＣＳＶデータより読込）
    public function insertQueryCsv(&$db, $data, $registercd, $useSchregRegdHdat)
    {
        $data1["YEAR"][TEXT]         = $data["YEAR"];
        $data1["SEMESTER"][TEXT]     = $data["SEMESTER"];
        $data1["GRADE"][TEXT]        = $data["GRADE"];
        $data1["HR_CLASS"][TEXT]     = $data["HR_CLASS"];
        $data1["HR_NAME"][TEXT]      = $data["HR_NAME"];
        $data1["HR_NAMEABBV"][TEXT]  = $data["HR_NAMEABBV"];
        if ($useSchregRegdHdat == '1') { //新しいschreg_regd_hdatのテーブルであるならば3フィールド追加
            $data1["GRADE_NAME"][TEXT]     = $data["GRADE_NAME"];
            $data1["HR_CLASS_NAME1"][TEXT] = $data["HR_CLASS_NAME1"];
            $data1["HR_CLASS_NAME2"][TEXT] = $data["HR_CLASS_NAME2"];
        }
        $data1["HR_FACCD"][TEXT]     = $data["HR_FACCD"];
        $data1["TR_CD1"][TEXT]       = $data["TR_CD1"];
        $data1["TR_CD2"][TEXT]       = $data["TR_CD2"];
        $data1["TR_CD3"][TEXT]       = $data["TR_CD3"];
        $data1["SUBTR_CD1"][TEXT]    = $data["SUBTR_CD1"];
        $data1["SUBTR_CD2"][TEXT]    = $data["SUBTR_CD2"];
        $data1["SUBTR_CD3"][TEXT]    = $data["SUBTR_CD3"];

        $data1["REGISTERCD"][TEXT]   = $registercd;
        $data1["UPDATED"][NUMBER]    = "sysdate()";

        $query = Query::insertSQL($data1, "SCHREG_REGD_HDAT");

        $result = $db->query($query);
    }


    //マスタの更新（ＣＳＶデータより読込）
    public function updateQueryCsv(&$db, $data, $registercd, $useSchregRegdHdat)
    {
        $data1["YEAR"][TEXT]          = $data["YEAR"];
        $data1["SEMESTER"][TEXT]      = $data["SEMESTER"];
        $data1["GRADE"][TEXT]         = $data["GRADE"];
        $data1["HR_CLASS"][TEXT]      = $data["HR_CLASS"];
        $data1["HR_NAME"][TEXT]       = $data["HR_NAME"];
        $data1["HR_NAMEABBV"][TEXT]   = $data["HR_NAMEABBV"];
        if ($useSchregRegdHdat == '1') { //新しいschreg_regd_hdatのテーブルであるならば3フィールド追加
            $data1["GRADE_NAME"][TEXT]     = $data["GRADE_NAME"];
            $data1["HR_CLASS_NAME1"][TEXT] = $data["HR_CLASS_NAME1"];
            $data1["HR_CLASS_NAME2"][TEXT] = $data["HR_CLASS_NAME2"];
        }
        $data1["HR_FACCD"][TEXT]      = $data["HR_FACCD"];
        $data1["TR_CD1"][TEXT]        = $data["TR_CD1"];
        $data1["TR_CD2"][TEXT]        = $data["TR_CD2"];
        $data1["TR_CD3"][TEXT]        = $data["TR_CD3"];
        $data1["SUBTR_CD1"][TEXT]     = $data["SUBTR_CD1"];
        $data1["SUBTR_CD2"][TEXT]     = $data["SUBTR_CD2"];
        $data1["SUBTR_CD3"][TEXT]     = $data["SUBTR_CD3"];

        $data1["REGISTERCD"][TEXT]    = $registercd;
        $data1["UPDATED"][NUMBER]     = "sysdate()";

        $where  = " WHERE ";
        $where .= "         YEAR      = '{$data["YEAR"]}'     ";
        $where .= "     AND SEMESTER  = '{$data["SEMESTER"]}' ";
        $where .= "     AND GRADE     = '{$data["GRADE"]}'    ";
        $where .= "     AND HR_CLASS  = '{$data["HR_CLASS"]}' ";
        $query = Query::updateSQL($data1, "SCHREG_REGD_HDAT", $where);

        $result = $db->query($query);
    }

    //職員クラス履歴データ追加
    public function insertStaffClassHistDat(&$db, $data, $tr_div, $staffcd)
    {
        $data1["YEAR"][TEXT]        = $data["YEAR"];
        $data1["SEMESTER"][TEXT]    = $data["SEMESTER"];
        $data1["GRADE"][TEXT]       = $data["GRADE"];
        $data1["HR_CLASS"][TEXT]    = $data["HR_CLASS"];
        $data1["TR_DIV"][TEXT]      = $tr_div;
        $data1["FROM_DATE"][TEXT]   = $data["YEAR"].'-04-01';
        $data1["TO_DATE"][TEXT]     = ((int)$data["YEAR"]+1).'-03-31';
        $data1["STAFFCD"][TEXT]     = $staffcd;
        $data1["REGISTERCD"][TEXT]  = STAFFCD;
        $data1["UPDATED"][NUMBER]   = "sysdate()";

        $query = Query::insertSQL($data1, "STAFF_CLASS_HIST_DAT");
        $result = $db->query($query);
    }

    //担任職員コード取得
    public function getTrStaffcd($data, $field)
    {
        $query  = " SELECT ";
        $query .= "     ".$field." ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_HDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR        = '".$data["YEAR"]."' AND ";
        $query .= "     SEMESTER    = '".$data["SEMESTER"]."' AND ";
        $query .= "     GRADE       = '".$data["GRADE"]."' AND ";
        $query .= "     HR_CLASS    = '".$data["HR_CLASS"]."' ";

        return $query;
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
