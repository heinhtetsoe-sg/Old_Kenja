<?php

require_once('for_php7.php');

class knjc161bQuery extends Query
{
    public $tmp_semester;   //UDFのTERM_GETの値を一時代入
    public $tmp_month;      //選択月
    public $tmp_sdate;      //データ抽出範囲の開始日付の値を一時代入
    public $tmp_edate;      //データ抽出範囲の終了日付の値を一時代入

    //起動チェック
    public function checkToStart($db)
    {
        return $db->getOne("SELECT COUNT(*) FROM SEMESTER_MST");
    }

    //学期の間の月を取得
    public function getSemesterMonth($year)
    {
        return "SELECT * FROM semester_mst WHERE year = '".$year."' AND semester <> '9'";
    }

    public function getSelectClass($model)
    {
        $query  = "SELECT T1.GRADE || T1.HR_CLASS AS VALUE, T1.HR_NAME AS LABEL ";
        $query .= "FROM SCHREG_REGD_HDAT T1 ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= "INNER JOIN SCHREG_REGD_GDAT T2 ON T2.YEAR = T1.YEAR AND T2.GRADE = T1.GRADE ";
                $query .= " AND T2.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind), "','")."') ";
            }
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= "INNER JOIN SCHREG_REGD_GDAT T2 ON T2.YEAR = T1.YEAR AND T2.GRADE = T1.GRADE ";
            $query .= " AND T2.SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= "WHERE T1.YEAR	 = '" .CTRL_YEAR ."' ";
        $query .= "  AND T1.SEMESTER = '" .substr($model->month, 0, 1)."' ";

        return $query;
    }

    //対象生徒
    public function getStudent($model)
    {
        $query  = " SELECT ";
        $query .= "     T2.SCHREGNO, ";
        $query .= "     T2.SCHREGNO AS SCHREGNO_SHOW, ";
        $query .= "     T2.ATTENDNO, ";
        $query .= "     T1.NAME_SHOW ";
        $query .= " FROM ";
        $query .= "     SCHREG_BASE_MST T1, ";
        $query .= "     SCHREG_REGD_DAT T2 ";
        $query .= "     LEFT JOIN ";
        $query .= "         SCHREG_SEND_ADDRESS_DAT S1 ON S1.SCHREGNO = T2.SCHREGNO AND S1.DIV = '1' ";
        $query .= " WHERE ";
        $query .= "     T2.YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND T2.SEMESTER = '".substr($model->month, 0, 1)."' ";
        $query .= "     AND T2.GRADE || T2.HR_CLASS = '".$model->selGrHrCls."' ";
        $query .= "     AND T2.SCHREGNO = T1.SCHREGNO ";
        $query .= " ORDER BY ";
        $query .= "     T2.ATTENDNO ";

        return $query;
    }

    //対象外の生徒取得
    public function getSchnoIdou($model)
    {
        //指定月の翌月初日を取得する。
        $mth = intval(substr($model->month, 2, 2));
        $yVal = intval(CTRL_YEAR) + ($mth < 4 ? 1 : 0);
        $fstdate = date('Y-m-d', strtotime('first day of '.$yVal."-".$mth));
        $lstdate = date('Y-m-d', strtotime('last day of '.$yVal."-".$mth));

        $query  = " SELECT T1.SCHREGNO, T1.ATTENDNO ";
        $query .= " FROM   SCHREG_REGD_DAT T1, V_SEMESTER_GRADE_MST T2 ";
        $query .= " WHERE  T1.YEAR = '".CTRL_YEAR."' AND ";
        $query .= "        T1.SEMESTER = '".substr($model->month, 0, 1)."' AND ";
        $query .= "        T1.YEAR = T2.YEAR AND ";
        $query .= "        T1.SEMESTER = T2.SEMESTER AND ";
        $query .= "        T2.GRADE = '".substr($grade_hr_class, 0, 2)."' AND ";
        $query .= "        T1.GRADE||T1.HR_CLASS = '".$model->selGrHrCls."' ";
        //転学(2)・退学(3)者 但し異動日が当月初日より小さい場合
        //転入(4)・編入(5)者 但し異動日が当月終日より大きい場合
        $query .= "        AND ( EXISTS(SELECT 'X' FROM SCHREG_BASE_MST S1 ";
        $query .= "                     WHERE S1.SCHREGNO = T1.SCHREGNO AND ";
        $query .= "                           ((S1.GRD_DIV IN('2','3') AND S1.GRD_DATE < '".$fstdate."' ) ";
        $query .= "                           OR (S1.ENT_DIV IN('4','5') AND S1.ENT_DATE > '".$lstdate."' )) ";
        $query .= "                    ) ";
        $query .= "              OR ";
        //留学(1)・休学(2)者
        $query .= "              EXISTS(SELECT 'X' FROM SCHREG_TRANSFER_DAT S1 ";
        $query .= "                     WHERE S1.SCHREGNO = T1.SCHREGNO AND ";
        $query .= "                           S1.TRANSFERCD IN ('1','2') AND ";
        $query .= "                           '".$fstdate."' > S1.TRANSFER_SDATE AND ";
        $query .= "                           '".$lstdate."' < S1.TRANSFER_EDATE ";
        $query .= "                    ) ";
        $query .= "        ) ";
        $query .= " ORDER BY T1.ATTENDNO ";

        return $query;
    }
}
