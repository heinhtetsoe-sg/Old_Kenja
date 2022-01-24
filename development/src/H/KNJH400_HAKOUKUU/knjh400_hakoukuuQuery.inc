<?php

require_once('for_php7.php');

class knjh400_hakoukuuQuery extends Query
{
    //生徒名取得
    public function getName($model)
    {
        $query .= " SELECT ";
        $query .= "     NAME_SHOW ";
        $query .= " FROM ";
        $query .= "     SCHREG_BASE_MST ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO = '{$model->schregno}' ";

        return $query;
    }
    //最小学年（校種毎）
    public function getMinGrade($model)
    {
        $query  = " SELECT ";
        $query .= "     NAME2 AS MIN_GRADE ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "         YEAR    = '".CTRL_YEAR."' ";
        $query .= "     AND NAMECD1 = 'A023' ";
        $query .= "     AND NAME1   = '{$model->getSchKind}' ";

        return $query;
    }

    //生徒健康診断ヘッダデータ取得
    public function getMedexamHdat($model, $year = '')
    {
        $year = ($year == '') ? $model->year : $year;
        $db = Query::dbCheckOut();

        $query  = "SELECT * FROM MEDEXAM_HDAT ";
        $query .= "WHERE SCHREGNO = '".$model->schregno."' AND ";
        $query .= "      YEAR = '".$year."'";

        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);

        Query::dbCheckIn($db);
        return $row;
    }
    //MEDEXAM_HDATにレコードがあるかチェック
    public function checkMedexamHdat($year, $schregno)
    {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     MEDEXAM_HDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR     = '{$year}' AND ";
        $query .= "     SCHREGNO = '{$schregno}' ";

        return $query;
    }
    //生徒健康診断歯口腔データ取得
    public function getMedexamToothDat($model, $year = '')
    {
        $year = ($year == '') ? $model->year : $year;
        $db = Query::dbCheckOut();

        $query  = "SELECT ";
        $query .= "    T1.* ";
        $query .= "    , DT002.TOOTH_REMARK3 AS OTHERDISEASE_REMARK1 ";
        $query .= "    , DT002.TOOTH_REMARK4 AS OTHERDISEASE_REMARK2 ";
        $query .= "    , DT002.TOOTH_REMARK5 AS OTHERDISEASE_REMARK3 ";
        $query .= "    , DT002.TOOTH_REMARK6 AS OTHERDISEASE_REMARK4 ";
        $query .= "    , DT003.TOOTH_REMARK3 AS OTHERDISEASECD3 ";
        $query .= "    , DT003.TOOTH_REMARK4 AS OTHERDISEASECD4 ";
        $query .= "    , DT003.TOOTH_REMARK5 AS OTHERDISEASE3 ";
        $query .= "    , DT003.TOOTH_REMARK6 AS OTHERDISEASE4 ";
        $query .= "    , DT005.TOOTH_REMARK1 AS DENTISTREMARK_REMARK1 ";
        $query .= "    , DT005.TOOTH_REMARK2 AS DENTISTREMARK_REMARK2 ";
        $query .= "    , DT005.TOOTH_REMARK3 AS DENTISTREMARK_REMARK3 ";
        $query .= "    , DT005.TOOTH_REMARK4 AS DENTISTREMARK_REMARK4 ";
        $query .= "FROM ";
        $query .= "    V_MEDEXAM_TOOTH_DAT T1 ";
        $query .= "    LEFT JOIN MEDEXAM_TOOTH_DETAIL_DAT DT002 ";
        $query .= "           ON DT002.YEAR      = T1.YEAR ";
        $query .= "          AND DT002.SCHREGNO  = T1.SCHREGNO ";
        $query .= "          AND DT002.TOOTH_SEQ = '002' ";
        $query .= "    LEFT JOIN MEDEXAM_TOOTH_DETAIL_DAT DT003 ";
        $query .= "           ON DT003.YEAR      = T1.YEAR ";
        $query .= "          AND DT003.SCHREGNO  = T1.SCHREGNO ";
        $query .= "          AND DT003.TOOTH_SEQ = '003' ";
        $query .= "    LEFT JOIN MEDEXAM_TOOTH_DETAIL_DAT DT005 ";
        $query .= "           ON DT005.YEAR      = T1.YEAR ";
        $query .= "          AND DT005.SCHREGNO  = T1.SCHREGNO ";
        $query .= "          AND DT005.TOOTH_SEQ = '005' ";
        $query .= "WHERE T1.SCHREGNO = '".$model->schregno."' AND ";
        $query .= "      T1.YEAR = '".$year."'";

        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);
        return $row;
    }
    //生徒名前取得（学籍基礎マスタ）
    public function getSchregBaseMstData($model, $toothDate)
    {
        $query  = " SELECT ";
        $query .= "     BIRTHDAY, ";
        if($toothDate){
            $query .= "     CASE WHEN BIRTHDAY IS NOT NULL THEN YEAR('{$toothDate}' - BIRTHDAY) END AS AGE, ";
        }
        $query .= "     FISCALYEAR(ENT_DATE) AS ENT_YEAR ";
        $query .= " FROM ";
        $query .= "     SCHREG_BASE_MST ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO = '" .$model->schregno."'";
        return $query;
    }
    //生徒学年クラス取得
    public function getSchregRegdDatData($model)
    {
        $query  = " SELECT t1.GRADE,t1.HR_CLASS,t1.ATTENDNO, ";
        $query .= "        t2.HR_NAME ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT t1 ";
        $query .= "     LEFT JOIN SCHREG_REGD_HDAT t2 ON t2.YEAR = t1.YEAR ";
        $query .= "     AND t2.SEMESTER = t1.SEMESTER ";
        $query .= "     AND t2.GRADE = t1.GRADE ";
        $query .= "     AND t2.HR_CLASS = t1.HR_CLASS ";
        $query .= " WHERE t1.SCHREGNO = '" .$model->schregno."' AND ";
        $query .= "       t1.YEAR = '".$model->year."' AND ";
        $query .= "       t1.SEMESTER = '".$model->semester."' ";

        return $query;
    }

    //生徒健康診断歯口腔データ取得
    public function getToothData($model)
    {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     MEDEXAM_TOOTH_DAT ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO = '".$model->schregno."' ";
        $query .= "     AND YEAR = '".$model->year."'";

        return $query;
    }

    //名称マスタ
    public function getNameMst($model, $namecd1)
    {
        $query  = " SELECT ";
        $query .= "     ABBV1 AS SHOW, ";
        $query .= "     NAMECD2 AS VALUE, ";
        if ($model->z010 == "musashinohigashi" && ($namecd1 == "F511" || $namecd1 == "F510" || $namecd1 == "F520" || $namecd1 == "F513")) {
            $query .= "     NAME2 AS LABEL, ";
        } else {
            $query .= "     NAMECD2 || '  ' || NAME1 AS LABEL, ";
        }
        $query .= "     CASE WHEN ABBV1 IS NULL ";
        $query .= "          THEN NAMECD2 || '  ' || NAME1 ";
        $query .= "          ELSE NAMECD2 || '  ' || ABBV1 || '  ' || NAME1 ";
        $query .= "     END AS LABEL2 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR    = '{$model->year}' AND ";
        $query .= "     NAMECD1 = '".$namecd1."'";
        return $query;
    }

    //名称マスタ(予備２取得)
    public function getNameMstSpare2($model, $setInNamecd1)
    {
        $query  = " SELECT ";
        $query .= "     NAMECD1, ";
        $query .= "     NAMECD2 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "         YEAR    = '{$model->year}' ";
        $query .= "     AND NAMECD1 IN ({$setInNamecd1}) ";
        $query .= "     AND NAMESPARE2 = '1' ";
        return $query;
    }

    //名称マスタ（Z010）
    public function getZ010($model)
    {
        $query  = " SELECT ";
        $query .= "     NAME1 ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = 'Z010' AND ";
        $query .= "     NAMECD2 = '00'";
        return $query;
    }

    //更新対象のレコードが存在するのかチェック
    public function checkHdat($model)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     MEDEXAM_HDAT ";
        $query .= " WHERE ";
        $query .= "         YEAR = '{$model->year}' ";
        $query .= "     AND SCHREGNO = '{$model->schregno}' ";

        return $query;
    }
    //MEDEXAM_DET_DAT(健康診断詳細データを取得)
    public function getMedexamDetDat($model)
    {
        $db = Query::dbCheckOut();

        $query  = "SELECT * FROM MEDEXAM_DET_DAT ";
        $query .= "WHERE SCHREGNO = '".$model->schregno."' AND ";
        $query .= "      YEAR = '".$model->year."'";

        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);
        return $row;
    }

    //一括更新処理画面用の生徒一覧
    public function getStudent($model)
    {
        $arr = explode("-", $model->GradeClass);
        $query  = "SELECT a.NAME_SHOW";
        $query .= "      ,b.SCHREGNO";
        $query .= "      ,b.ATTENDNO";
        $query .= "      ,FISCALYEAR(ENT_DATE) AS ENT_YEAR ";
        $query .= "  FROM SCHREG_BASE_MST a RIGHT OUTER JOIN ";
        $query .= "       SCHREG_REGD_DAT b";
        $query .= "    ON a.SCHREGNO = b.SCHREGNO";
        $query .= " WHERE b.YEAR     = '".$model->year."'";
        $query .= "   AND b.SEMESTER = '".$model->semester."'";
        $query .= "   AND b.GRADE    = '".$arr[0]."'";
        $query .= "   AND b.HR_CLASS = '".$arr[1]."'";
        $query .= " ORDER BY ATTENDNO";
        return $query;
    }

    public function getDentistremarkDate($year, $schregno)
    {
        $query  = " SELECT ";
        $query .= "     DENTISTREMARKDATE ";
        $query .= " FROM ";
        $query .= "     MEDEXAM_TOOTH_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$year}' AND ";
        $query .= "     SCHREGNO = '{$schregno}' ";

        return $query;
    }

    //テーブル存在チェック
    public function checkTableExist()
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     SYSIBM.SYSTABLES ";
        $query .= " WHERE ";
        $query .= "     NAME = 'SETTING_DAT' ";

        return $query;
    }

    //校種取得
    public function getSchoolKind($grade)
    {
        $query  = " SELECT ";
        $query .= "    SCHOOL_KIND ";
        $query .= " FROM ";
        $query .= "    SCHREG_REGD_GDAT ";
        $query .= " WHERE ";
        $query .= "        YEAR  = '".CTRL_YEAR."' ";
        $query .= "    AND GRADE = '".$grade."' ";

        return $query;
    }

    //生徒項目名取得
    public function getSchName($model)
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     REMARK1, ";
        $query .= "     SCHOOLCD ";
        $query .= " FROM ";
        $query .= "     SETTING_DAT ";
        $query .= " WHERE ";
        $query .= "     SEQ = '001' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= " AND SCHOOLCD    = '".sprintf("%012d", SCHOOLCD)."' ";
                $query .= " AND SCHOOL_KIND = '".$model->getSchKind."' ";
            }
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND SCHOOLCD    = '".sprintf("%012d", SCHOOLCD)."' ";
            $query .= " AND SCHOOL_KIND = '".SCHOOLKIND."' ";
        } else {
            $query .= " AND SCHOOL_KIND IN (SELECT ";
            $query .= "                         S2.SCHOOL_KIND ";
            $query .= "                     FROM ";
            $query .= "                         SCHREG_REGD_DAT S1, ";
            $query .= "                         SCHREG_REGD_GDAT S2 ";
            $query .= "                     WHERE ";
            $query .= "                         S1.YEAR     = S2.YEAR AND ";
            $query .= "                         S1.YEAR     = '".$model->year."' AND ";
            $query .= "                         S1.SEMESTER = '".$model->semester."' AND ";
            $query .= "                         S1.GRADE    = S2.GRADE AND ";
            $query .= "                         S1.SCHREGNO = '".$model->schregno."' ";
            $query .= "                     ) ";
        }
        $query .= " ORDER BY ";
        $query .= "     SCHOOLCD ";

        return $query;
    }
}
