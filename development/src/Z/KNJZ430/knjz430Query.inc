<?php

require_once('for_php7.php');

class knjz430Query extends Query
{

    //対応者を取得
    public function getkekka($year, $year_add, $table, $chkyear = "")
    {
        $query  = " WITH NEXTYEAR_TBL AS ( ";
        $query .= " SELECT ";
        $query .= "     'tbljoin' AS JOINCD, ";
        $query .= "     CASE WHEN COUNT(*) > 0 THEN '次年度データあり' ELSE '' END AS KEKKA ";
        $query .= " FROM ";
        $query .= "     $table ";
        $query .= " WHERE ";
        if ($chkyear) {
            $query .= "     ".$chkyear." = '".$year_add."' ";
        } else {
            $query .= "     YEAR = '".$year_add."' ";
        }
        $query .= " ), YEAR_TBL AS (SELECT ";
        $query .= "     'tbljoin' AS JOINCD, ";
        $query .= "     CASE WHEN COUNT(*) = 0 THEN '今年度データなし' ELSE '' END AS KEKKA ";
        $query .= " FROM ";
        $query .= "     $table ";
        $query .= " WHERE ";
        if ($chkyear) {
            $query .= "     ".$chkyear." = '".$year."' ";
        } else {
            $query .= "     YEAR = '".$year."' ";
        }
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     CASE WHEN T1.KEKKA = '' AND T2.KEKKA = '' ";
        $query .= "          THEN '' ";
        $query .= "          ELSE CASE WHEN T1.KEKKA != '' ";
        $query .= "                    THEN T1.KEKKA ";
        $query .= "                    ELSE T2.KEKKA END ";
        $query .= "     END AS KEKKA ";
        $query .= " FROM ";
        $query .= "     YEAR_TBL T1 ";
        $query .= "     LEFT JOIN NEXTYEAR_TBL T2 ON T1.JOINCD = T2.JOINCD ";

        return $query;
    }

    public function getDeleteQuery($table, $year_add)
    {
        $query  = " DELETE FROM $table WHERE YEAR = '".$year_add."' ";
        return $query;
    }

    public function getFieldQuery($table, $year, $chkyear = "")
    {
        $query  = " SELECT * FROM $table WHERE ";
        if ($chkyear) {
            $query  .= " ".$chkyear." = '".$year."' ";
        } else {
            $query  .= " YEAR = '".$year."' ";
        }
        return $query;
    }

    public function getInsertQuery($model, $year, $year_add, $table, $field, $chkyear = "")
    {
        $query  = " INSERT INTO $table ";
        $query .= " SELECT ";
        $query .= "     $field ";
        $query .= " FROM ";
        $query .= "     $table ";
        $query .= " WHERE ";
        if ($chkyear) {
            $query .= $chkyear;
        } else {
            $query .= "     YEAR = '".$year."' ";
        }

        // 単位マスタ更新
        if ('CREDIT_MST' == $table) {
            // コースコードマスタ存在判定
            $query .= "     AND EXISTS ( ";
            $query .= "             SELECT 'X' ";
            $query .= "             FROM V_COURSECODE_MST M1 ";
            $query .= "             WHERE M1.COURSECODE = ".$table.".COURSECODE ";
            $query .= "         ) ";
            // 科目マスタ存在判定
            $query .= "     AND EXISTS ( ";
            $query .= "             SELECT 'X' ";
            $query .= "             FROM V_SUBCLASS_MST M2 ";
            $query .= "             WHERE M2.YEAR = '".$year_add."' ";
            $query .= "             AND M2.SUBCLASSCD = ".$table.".SUBCLASSCD ";
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "                 AND M2.CLASSCD = ".$table.".CLASSCD ";
                $query .= "                 AND M2.SCHOOL_KIND = ".$table.".SCHOOL_KIND ";
                $query .= "                 AND M2.CURRICULUM_CD = ".$table.".CURRICULUM_CD ";
                if ($model->Properties["use_prg_schoolkind"] == "1") {
                    if ($model->selectSchoolKind) {
                        $query .= "             AND M2.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind), "','")."') ";
                    }
                } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
                    $query .= "                 AND M2.SCHOOL_KIND = '".SCHOOLKIND."' ";
                }
            }
            $query .= "         ) ";
        }

        return $query;
    }

    public function getInsertSemesterMstQuery($tablename, $year, $row, $db)
    {
        $data["YEAR"][TEXT]         = $year;
        $data["SEMESTER"][TEXT]     = $row["SEMESTER"];
        $data["SEMESTERNAME"][TEXT] = $row["SEMESTERNAME"];
        $data["SDATE"][TEXT]        = $row["SDATE"];
        $data["EDATE"][TEXT]        = $row["EDATE"];
        $data["REGISTERCD"][TEXT]   = STAFFCD ;
        $data["UPDATED"][FUNC]      = "sysdate()";
        if ('SEMESTER_DETAIL_MST' == $tablename) {
            $data["SEMESTER_DETAIL"][TEXT]     = $row["SEMESTER_DETAIL"];
        }

        $query = Query::insertSQL($data, $tablename);
        $db->query($query);

        return;
    }

    public function cntTable($table_name)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     SYSIBM.SYSCOLUMNS ";
        $query .= " WHERE ";
        $query .= "     TBNAME = '{$table_name}' ";

        return $query;
    }

    public function getCount($year_add, $table)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     $table ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$year_add}' ";

        return $query;
    }

    public function getZ010Name1()
    {
        $query  = " SELECT ";
        $query .= "     NAME1 ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = 'Z010' ";
        $query .= "     AND NAMECD2 = '00' ";

        return $query;
    }
}
