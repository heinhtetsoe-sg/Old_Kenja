<?php
class knji100c_03Query extends Query {

/*
        性別区分 .........Z002

        使用テーブル
        SCHREG_BASE_MST......基礎情報取得用
        SCHREG_REGD_DAT......学籍情報取得用
        ATTEND_SUBCLASS_DAT..出欠情報（科目別）取得用
        SUBCLASS_MST.........科目名取得用
*/

    //ＣＳＶ書き出し
    function selectQuery($model) {
        $query  = " WITH BASE_MST AS ( ";
        $query .= "     SELECT ";
        $query .= "         * ";
        $query .= "     FROM ";
        $query .= "         SCHREG_BASE_MST ";
        $query .= "     WHERE ";
        $query .= "         SCHREGNO IN ('" .str_replace(",", "','", $model->selectdata_l) ."') ";
        $query .= " ), ATTEND_SUBCLASS AS ( ";
        $query .= "     SELECT DISTINCT ";
        $query .= "         SCHREGNO, ";
        $query .= "         MONTH, ";
        $query .= "         SUBCLASSCD, ";
        $query .= "         SICK, ";
        $query .= "         NOTICE, ";
        $query .= "         NONOTICE, ";
        $query .= "         NURSEOFF, ";
        $query .= "         LATE, ";
        $query .= "         EARLY ";
        $query .= "     FROM ";
        $query .= "         ATTEND_SUBCLASS_DAT ";
        $query .= "     WHERE ";
        $query .= "         YEAR = '".CTRL_YEAR."' AND ";
        $query .= "         SCHREGNO IN ('" .str_replace(",", "','", $model->selectdata_l) ."') AND ";
        $query .= "         MONTH || '-' ||  SEMESTER  <= '".$model->month."' ";
        $query .= " ), SUBCLASS_DATA AS ( ";
        $query .= "     SELECT DISTINCT ";
        $query .= "         SCHREGNO ";
        for($i=0; $i<count($model->opt_subclass); $i++){
            $query .= "     ,SUM(CASE SUBCLASSCD WHEN '".$model->opt_subclass[$i]."' THEN INT(SUBCLASSCD) ELSE NULL END) AS SUBCLASSCD" .$i ." ";
            $query .= "     ,SUM(CASE SUBCLASSCD WHEN '".$model->opt_subclass[$i]."' THEN (SICK + NOTICE + NONOTICE + NURSEOFF) ELSE NULL END) AS KETSUJI" .$i ." ";
            $query .= "     ,SUM(CASE SUBCLASSCD WHEN '".$model->opt_subclass[$i]."' THEN (LATE + EARLY) ELSE NULL END) AS LATE_EARLY" .$i ." ";
            $query .= "     ,SUM(CASE SUBCLASSCD WHEN '".$model->opt_subclass[$i]."' THEN 0 ELSE NULL END) AS KEKKA" .$i ." ";
        }
        $query .= "     FROM ";
        $query .= "         ATTEND_SUBCLASS ";
        $query .= "     GROUP BY ";
        $query .= "         SCHREGNO ";
        $query .= " ) ";

        $query .= " SELECT ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     T1.NAME, ";
        $query .= "     T2.GRADE, ";
        $query .= "     T2.HR_CLASS, ";
        $query .= "     T2.ATTENDNO, ";
        if ($model->output == OUT_CODE_NAME){           //コード＋名称
            $query .= "     VALUE(T1.SEX,'') || ',' || ";
            $query .= "     VALUE(N1.NAME1,'') SEX ";
        } else if ($model->output == OUT_CODE_ONLY){    //コードのみ
            $query .= "     T1.SEX SEX ";
        } else if ($model->output == OUT_NAME_ONLY){    //名称のみ
            $query .= "     N1.NAME1 SEX ";
        }
        for($i=0; $i<count($model->opt_subclass); $i++){
            $query .= "     ,CASE WHEN L1.SUBCLASSCD" .$i ." IS NOT NULL THEN '".$model->opt_subclass[$i] ."' ELSE NULL END AS SUBCLASSCD" .$i ." ";
            $query .= "     ,L1.KETSUJI" .$i ." ";
            $query .= "     ,L1.LATE_EARLY" .$i ." ";
            $query .= "     ,L1.KEKKA" .$i ." ";
        }
        $query .= " FROM ";
        $query .= "     BASE_MST T1 ";
        $query .= "     LEFT JOIN NAME_MST N1 ON N1.NAMECD2 = T1.SEX AND N1.NAMECD1='Z002' ";
        $query .= "     LEFT JOIN SUBCLASS_DATA L1 ON L1.SCHREGNO = T1.SCHREGNO ";
        $query .= "     INNER JOIN SCHREG_REGD_DAT T2 ON  T2.SCHREGNO = T1.SCHREGNO ";
        $query .= "                                   AND T2.YEAR     = '".CTRL_YEAR."' ";
        $query .= "                                   AND T2.SEMESTER = '".CTRL_SEMESTER."' ";
        $query .= " ORDER BY ";
        $query .= "     T2.GRADE, ";
        $query .= "     T2.HR_CLASS, ";
        $query .= "     T2.ATTENDNO ";

        return $query;
    }

    //科目取得
    function getSubclassList($model) {
        $query  = "     SELECT DISTINCT ";
        $query .= "         T1.SUBCLASSCD, ";
        $query .= "         T2.SUBCLASSNAME ";
        $query .= "     FROM ";
        $query .= "         ATTEND_SUBCLASS_DAT T1 ";
        $query .= "         LEFT JOIN V_SUBCLASS_MST T2 ON T1.YEAR = T2.YEAR AND T1.SUBCLASSCD = T2.SUBCLASSCD ";
        $query .= "     WHERE ";
        $query .= "         T1.YEAR = '".CTRL_YEAR."' AND ";
        $query .= "         T1.SCHREGNO IN ('" .str_replace(",", "','", $model->selectdata_l) ."') AND ";
        $query .= "         T1.MONTH || '-' ||  T1.SEMESTER  <= '".$model->month."' ";
        $query .= "     ORDER BY ";
        $query .= "         T1.SUBCLASSCD ";

        return $query;
    }




    //年組取得（権限チェック）
    function getAuth() {
        //参照・更新可
        if (AUTHORITY == DEF_REFERABLE || AUTHORITY == DEF_UPDATABLE){
            $query  = "SELECT ";
            $query .= "    GRADE || '_' ||  HR_CLASS AS VALUE, ";
            $query .= "    HR_NAME AS LABEL ";
            $query .= "FROM ";
            $query .= "    SCHREG_REGD_HDAT ";
            $query .= "WHERE ";
            $query .= "    YEAR='".CTRL_YEAR."' AND ";
            $query .= "    SEMESTER='". CTRL_SEMESTER ."' ";
            $query .= " ORDER BY ";
            $query .= "     VALUE ";
        }
        //参照・更新可（制限付き）
        if (AUTHORITY == DEF_REFER_RESTRICT || AUTHORITY == DEF_UPDATE_RESTRICT){
            $query  = "SELECT ";
            $query .= "    GRADE || '_' ||  HR_CLASS AS VALUE, ";
            $query .= "    HR_NAME AS LABEL ";
            $query .= "FROM ";
            $query .= "    SCHREG_REGD_HDAT ";
            $query .= "WHERE ";
            $query .= "    YEAR='".CTRL_YEAR."' AND ";
            $query .= "    SEMESTER='". CTRL_SEMESTER ."' AND ";
            $query .= "       (TR_CD1 = '" .STAFFCD ."' ";
            $query .= "     OR TR_CD2 = '" .STAFFCD ."' ";
            $query .= "     OR TR_CD3 = '" .STAFFCD ."') ";
            $query .= " ORDER BY ";
            $query .= "     VALUE ";
        }

        return $query;
    }

    //対象外の生徒取得
    function getSchnoIdou($model)
    {
        $date = str_replace("/","-",CTRL_DATE);

        $query  = "";
        $query .= " SELECT T1.SCHREGNO, T1.ATTENDNO ";
        $query .= " FROM   SCHREG_REGD_DAT T1, SEMESTER_MST T2 ";
        $query .= " WHERE  T1.YEAR = '".CTRL_YEAR."' AND ";
        $query .= "        T1.SEMESTER = '".CTRL_SEMESTER."' AND ";
        $query .= "        T1.YEAR = T2.YEAR AND ";
        $query .= "        T1.SEMESTER = T2.SEMESTER AND ";
                           //卒業(1)・転学(2)・退学(3)者 但し異動日が学期終了日または異動基準日より小さい場合
        $query .= "        EXISTS(SELECT 'X' FROM SCHREG_BASE_MST S1 ";
        $query .= "                WHERE S1.SCHREGNO = T1.SCHREGNO AND ";
        $query .= "                    (S1.GRD_DIV IN('1','2','3') AND S1.GRD_DATE < ";
        $query .= "                         CASE WHEN T2.EDATE < '".$date."' ";
        $query .= "                              THEN T2.EDATE ELSE '".$date."' END)) ";
        $query .= " ORDER BY T1.ATTENDNO ";

        return $query;
    }

    //生徒リストtoリスト右
    function getStudent_right($model) {
        $query .= " SELECT ";
        $query .= "     T2.HR_NAME, ";
        $query .= "     T1.ATTENDNO, ";
        $query .= "     L1.NAME_SHOW, ";
        $query .= "     T1.GRADE || T1.HR_CLASS || T1.ATTENDNO || '-' || T1.SCHREGNO AS VALUE ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1 ";
        $query .= " LEFT JOIN SCHREG_REGD_HDAT T2 ";
        $query .= "        ON T2.YEAR = T1.YEAR ";
        $query .= "       AND T2.SEMESTER = T1.SEMESTER ";
        $query .= "       AND T2.GRADE = T1.GRADE ";
        $query .= "       AND T2.HR_CLASS = T1.HR_CLASS ";
        $query .= " LEFT JOIN SCHREG_BASE_MST L1 ON L1.SCHREGNO = T1.SCHREGNO ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' AND ";
        $query .= "     T1.SEMESTER = '". CTRL_SEMESTER ."' AND ";
        $query .= "     T1.GRADE || '_' || T1.HR_CLASS = '{$model->grade_hr_class}' ";
        $query .= " ORDER BY ";
        $query .= "     T1.ATTENDNO ";

        return $query;
    }

    //生徒リストtoリスト左
    function getStudent_left($model) {
        $query .= " SELECT ";
        $query .= "     T2.HR_NAME, ";
        $query .= "     T1.ATTENDNO, ";
        $query .= "     L1.NAME_SHOW, ";
        $query .= "     T1.GRADE || T1.HR_CLASS || T1.ATTENDNO || '-' || T1.SCHREGNO AS VALUE  ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1 ";
        $query .= " LEFT JOIN SCHREG_REGD_HDAT T2 ";
        $query .= "        ON T2.YEAR = T1.YEAR ";
        $query .= "       AND T2.SEMESTER = T1.SEMESTER ";
        $query .= "       AND T2.GRADE = T1.GRADE ";
        $query .= "       AND T2.HR_CLASS = T1.HR_CLASS ";
        $query .= " LEFT JOIN SCHREG_BASE_MST L1 ON L1.SCHREGNO = T1.SCHREGNO ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' AND ";
        $query .= "     T1.SEMESTER = '". CTRL_SEMESTER ."' AND ";
        $query .= "     T1.SCHREGNO IN ('" .str_replace(",", "','", $model->selectdata_l) ."') ";
        $query .= " ORDER BY ";
        $query .= "     T1.GRADE, ";
        $query .= "     T1.HR_CLASS, ";
        $query .= "     T1.ATTENDNO ";

        return $query;
    }

    //対象月取得
    function getSemeMonth()
    {
        $query  = " SELECT ";
        $query .= "     SEMESTER, ";
        $query .= "     SEMESTERNAME, ";
        $query .= "     CASE WHEN MONTH(SDATE) < 4 ";
        $query .= "          THEN MONTH(SDATE) + 12 ";
        $query .= "          ELSE MONTH(SDATE) END AS S_MONTH, ";
        $query .= "     CASE WHEN MONTH(EDATE) < 4 ";
        $query .= "          THEN MONTH(EDATE) + 12 ";
        $query .= "          ELSE MONTH(EDATE) END AS E_MONTH ";
        $query .= " FROM ";
        $query .= "     SEMESTER_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND SEMESTER <> '9' ";
        $query .= " ORDER BY ";
        $query .= "     SEMESTER ";

        return $query;
    }

    //対象月名称取得
    function getMonthName($month)
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     NAMECD2, ";
        $query .= "     NAME1, ";
        $query .= "     NAMESPARE1 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR  = '".CTRL_YEAR."' ";
        $query .= "     AND NAMECD1 = 'Z005' ";
        $query .= "     AND NAMECD2 = '".sprintf('%02d',$month)."' ";
        $query .= " ORDER BY ";
        $query .= "     NAMESPARE1 ";

        return $query;
    }

    //出欠名称取得
    function getAttendName($namecd2)
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     NAME1 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR  = '".CTRL_YEAR."' ";
        $query .= "     AND NAMECD1 = 'C001' ";
        $query .= "     AND NAMECD2 = '".$namecd2."' ";

        return $query;
    }

    //サブシステム取得
    function getSubSystem() {
        $query  = " SELECT ";
        $query .= "     NAMECD2 AS VALUE, ";
        $query .= "     NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' AND ";
        $query .= "     NAMECD1 = 'I001' ";

        return $query;
    }
}
?>