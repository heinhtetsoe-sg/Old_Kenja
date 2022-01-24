<?php

require_once('for_php7.php');
class knji100c_06Query extends Query {

/*
        性別区分 .........Z002

        使用テーブル
        SCHREG_BASE_MST......基礎情報取得用
        SCHREG_REGD_DAT......学籍情報取得用
        AFT_GRAD_COURSE_DAT.......進路情報取得用
*/

    //進学
    function getPrintSingaku($model, $item)
    {
        $year       = CTRL_YEAR;
        $semester   = CTRL_SEMESTER;

        $query  = "";
        $query .= " SELECT ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     I1.NAME, ";
        $query .= "     I2.GRADE, ";
        $query .= "     I2.HR_CLASS, ";
        $query .= "     I2.ATTENDNO, ";
        if ($model->output == OUT_CODE_NAME){           //コード＋名称
            $query .= "     VALUE(N1.NAMECD2,'') || ',' || VALUE(N1.NAME1,'') as SEX, ";
            $query .= "     VALUE(E012.NAMECD2,'') || ',' || VALUE(E012.NAME1,'') as SCHOOL_GROUP_NAME, ";
            $query .= "     VALUE(L4.PREF_CD,'') || ',' || VALUE(L4.PREF_NAME,'') as PREF_NAME, ";
            $query .= "     VALUE(T1.STAT_CD,'') || ',' || VALUE(L1.SCHOOL_NAME,'') as STAT_NAME, ";
            $query .= "     VALUE(L2.FACULTYCD,'') || ',' || VALUE(L2.FACULTYNAME,'') as FACULTYNAME, ";
            $query .= "     VALUE(L3.DEPARTMENTCD,'') || ',' || VALUE(L3.DEPARTMENTNAME,'') as DEPARTMENTNAME, ";
            $query .= "     VALUE(E002.NAMECD2,'') || ',' || VALUE(E002.NAME1,'') as HOWTOEXAM_NAME, ";
            $query .= "     VALUE(E005.NAMECD2,'') || ',' || VALUE(E005.NAME1,'') as DECISION_NAME, ";
            $query .= "     VALUE(E006.NAMECD2,'') || ',' || VALUE(E006.NAME1,'') as PLANSTAT_NAME ";
        } else if ($model->output == OUT_CODE_ONLY){    //コードのみ
            $query .= "     N1.NAMECD2 as SEX, ";
            $query .= "     E012.NAMECD2 as SCHOOL_GROUP_NAME, ";
            $query .= "     L4.PREF_CD as PREF_NAME, ";
            $query .= "     T1.STAT_CD as STAT_NAME, ";
            $query .= "     L2.FACULTYCD as FACULTYNAME, ";
            $query .= "     L3.DEPARTMENTCD as DEPARTMENTNAME, ";
            $query .= "     E002.NAMECD2 as HOWTOEXAM_NAME, ";
            $query .= "     E005.NAMECD2 as DECISION_NAME, ";
            $query .= "     E006.NAMECD2 as PLANSTAT_NAME ";
        } else if ($model->output == OUT_NAME_ONLY){    //名称のみ
            $query .= "     N1.NAME1 as SEX, ";
            $query .= "     E012.NAME1 as SCHOOL_GROUP_NAME, ";
            $query .= "     L4.PREF_NAME as PREF_NAME, ";
            $query .= "     L1.SCHOOL_NAME as STAT_NAME, ";
            $query .= "     L2.FACULTYNAME as FACULTYNAME, ";
            $query .= "     L3.DEPARTMENTNAME as DEPARTMENTNAME, ";
            $query .= "     E002.NAME1 as HOWTOEXAM_NAME, ";
            $query .= "     E005.NAME1 as DECISION_NAME, ";
            $query .= "     E006.NAME1 as PLANSTAT_NAME ";
        }
        $query .= " FROM ";
        $query .= "     AFT_GRAD_COURSE_DAT T1 ";
        $query .= "     INNER JOIN SCHREG_BASE_MST I1 ON I1.SCHREGNO = T1.SCHREGNO ";
        $query .= "     INNER JOIN SCHREG_REGD_DAT I2 ON I2.SCHREGNO = T1.SCHREGNO ";
        $query .= "                                  AND I2.YEAR     = '{$year}' ";
        $query .= "                                  AND I2.SEMESTER = '{$semester}' ";
        $query .= "     LEFT JOIN COLLEGE_MST L1 ON L1.SCHOOL_CD = T1.STAT_CD ";
        $query .= "     LEFT JOIN NAME_MST E012 ON E012.NAMECD1 = 'E012' AND E012.NAMECD2 = L1.SCHOOL_GROUP ";
        $query .= "     LEFT JOIN COLLEGE_FACULTY_MST L2 ON L2.SCHOOL_CD = T1.STAT_CD ";
        $query .= "                                     AND L2.FACULTYCD = T1.FACULTYCD ";
        $query .= "     LEFT JOIN COLLEGE_DEPARTMENT_MST L3 ON L3.SCHOOL_CD    = T1.STAT_CD ";
        $query .= "                                        AND L3.FACULTYCD    = T1.FACULTYCD ";
        $query .= "                                        AND L3.DEPARTMENTCD = T1.DEPARTMENTCD ";
        $query .= "     LEFT JOIN PREF_MST L4 ON L4.PREF_CD = T1.PREF_CD ";
        $query .= "     LEFT JOIN NAME_MST E002 ON E002.NAMECD1 = 'E002' AND E002.NAMECD2 = T1.HOWTOEXAM ";
        $query .= "     LEFT JOIN NAME_MST E005 ON E005.NAMECD1 = 'E005' AND E005.NAMECD2 = T1.DECISION ";
        $query .= "     LEFT JOIN NAME_MST E006 ON E006.NAMECD1 = 'E006' AND E006.NAMECD2 = T1.PLANSTAT ";
        $query .= "     LEFT JOIN NAME_MST N1 ON N1.NAMECD1 = 'Z002' AND N1.NAMECD2 = I1.SEX ";
        $query .= " WHERE ";
        $query .= "         T1.YEAR         = '{$year}' ";
        $query .= "     AND T1.SENKOU_KIND  = '0' ";
        if ($item == "SINGAKU_PASS") {
            $query .= " AND T1.DECISION     = '1' ";
        }
        if ($item == "SINGAKU_LAST") {
            $query .= " AND T1.PLANSTAT     = '1' ";
        }
        $query .= "     AND T1.SCHREGNO IN ('" .str_replace(",", "','", $model->selectdata_l) ."') ";
        $query .= " ORDER BY ";
        $query .= "     I2.GRADE, ";
        $query .= "     I2.HR_CLASS, ";
        $query .= "     I2.ATTENDNO, ";
        $query .= "     T1.SEQ ";
        return $query;
    }

    //就職
    function getPrintShushoku($model, $item)
    {
        $year       = CTRL_YEAR;
        $semester   = CTRL_SEMESTER;

        $query  = "";
        $query .= " SELECT ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     I1.NAME, ";
        $query .= "     I2.GRADE, ";
        $query .= "     I2.HR_CLASS, ";
        $query .= "     I2.ATTENDNO, ";
        if ($model->output == OUT_CODE_NAME){           //コード＋名称
            $query .= "     VALUE(N1.NAMECD2,'') || ',' || VALUE(N1.NAME1,'') as SEX, ";
            $query .= "     VALUE(L1.COMPANY_CD,'') || ',' || VALUE(L1.COMPANY_NAME,'') as STAT_NAME, ";
            $query .= "     VALUE(L2.INDUSTRY_LCD,'') || ',' || VALUE(L2.INDUSTRY_LNAME,'') as INDUSTRY_LNAME, ";
            $query .= "     VALUE(L4.PREF_CD,'') || ',' || VALUE(L4.PREF_NAME,'') as PREF_NAME, ";
            $query .= "     VALUE(E002.NAMECD2,'') || ',' || VALUE(E002.NAME1,'') as HOWTOEXAM_NAME, ";
            $query .= "     VALUE(E005.NAMECD2,'') || ',' || VALUE(E005.NAME1,'') as DECISION_NAME, ";
            $query .= "     VALUE(E006.NAMECD2,'') || ',' || VALUE(E006.NAME1,'') as PLANSTAT_NAME ";
        } else if ($model->output == OUT_CODE_ONLY){    //コードのみ
            $query .= "     N1.NAMECD2 as SEX, ";
            $query .= "     L1.COMPANY_CD as STAT_NAME, ";
            $query .= "     L2.INDUSTRY_LCD as INDUSTRY_LNAME, ";
            $query .= "     L4.PREF_CD as PREF_NAME, ";
            $query .= "     E002.NAMECD2 as HOWTOEXAM_NAME, ";
            $query .= "     E005.NAMECD2 as DECISION_NAME, ";
            $query .= "     E006.NAMECD2 as PLANSTAT_NAME ";
        } else if ($model->output == OUT_NAME_ONLY){    //名称のみ
            $query .= "     N1.NAME1 as SEX, ";
            $query .= "     L1.COMPANY_NAME as STAT_NAME, ";
            $query .= "     L2.INDUSTRY_LNAME as INDUSTRY_LNAME, ";
            $query .= "     L4.PREF_NAME as PREF_NAME, ";
            $query .= "     E002.NAME1 as HOWTOEXAM_NAME, ";
            $query .= "     E005.NAME1 as DECISION_NAME, ";
            $query .= "     E006.NAME1 as PLANSTAT_NAME ";
        }
        $query .= " FROM ";
        $query .= "     AFT_GRAD_COURSE_DAT T1 ";
        $query .= "     INNER JOIN SCHREG_BASE_MST I1 ON I1.SCHREGNO = T1.SCHREGNO ";
        $query .= "     INNER JOIN SCHREG_REGD_DAT I2 ON I2.SCHREGNO = T1.SCHREGNO ";
        $query .= "                                  AND I2.YEAR     = '{$year}' ";
        $query .= "                                  AND I2.SEMESTER = '{$semester}' ";
        $query .= "     LEFT JOIN COMPANY_MST L1 ON L1.COMPANY_CD = T1.STAT_CD ";
        $query .= "     LEFT JOIN INDUSTRY_L_MST L2 ON L2.INDUSTRY_LCD = L1.INDUSTRY_LCD ";
        $query .= "     LEFT JOIN PREF_MST L4 ON L4.PREF_CD = T1.PREF_CD ";
        $query .= "     LEFT JOIN NAME_MST E002 ON E002.NAMECD1 = 'E002' AND E002.NAMECD2 = T1.HOWTOEXAM ";
        $query .= "     LEFT JOIN NAME_MST E005 ON E005.NAMECD1 = 'E005' AND E005.NAMECD2 = T1.DECISION ";
        $query .= "     LEFT JOIN NAME_MST E006 ON E006.NAMECD1 = 'E006' AND E006.NAMECD2 = T1.PLANSTAT ";
        $query .= "     LEFT JOIN NAME_MST N1 ON N1.NAMECD1 = 'Z002' AND N1.NAMECD2 = I1.SEX ";
        $query .= " WHERE ";
        $query .= "         T1.YEAR         = '{$year}' ";
        $query .= "     AND T1.SENKOU_KIND  = '1' ";
        if ($item == "SHUSHOKU_PASS") {
            $query .= " AND T1.DECISION     = '1' ";
        }
        $query .= "     AND T1.SCHREGNO IN ('" .str_replace(",", "','", $model->selectdata_l) ."') ";
        $query .= " ORDER BY ";
        $query .= "     I2.GRADE, ";
        $query .= "     I2.HR_CLASS, ";
        $query .= "     I2.ATTENDNO, ";
        $query .= "     T1.SEQ ";
        return $query;
    }

    //課程学科取得
    function getCourseMajor($model) {
        $query  = " SELECT ";
        $query .= "     COURSECD || MAJORCD AS VALUE, ";
        $query .= "     COURSENAME || MAJORNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     V_COURSE_MAJOR_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //学年取得
    function getGrade($model) {
        $query  = "SELECT ";
        $query .= "    GRADE AS VALUE, ";
        $query .= "    GRADE_NAME1 AS LABEL ";
        $query .= "FROM ";
        $query .= "    SCHREG_REGD_GDAT ";
        $query .= "WHERE ";
        $query .= "    YEAR = '".CTRL_YEAR."' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= "     AND GRADE IN (SELECT ";
                $query .= "                      REGD_G.GRADE ";
                $query .= "                   FROM ";
                $query .= "                      SCHREG_REGD_GDAT REGD_G ";
                $query .= "                   WHERE ";
                $query .= "                      REGD_G.YEAR = '".CTRL_YEAR."' ";
                $query .= "                      AND REGD_G.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."')) ";
            }
        } elseif ($model->Properties["useSchool_KindField"] == "1") {
            $query .= "     AND GRADE IN (SELECT ";
            $query .= "                      REGD_G.GRADE ";
            $query .= "                   FROM ";
            $query .= "                      SCHREG_REGD_GDAT REGD_G ";
            $query .= "                   WHERE ";
            $query .= "                      REGD_G.YEAR = '".CTRL_YEAR."' ";
            $query .= "                      AND REGD_G.SCHOOL_KIND = '" .SCHOOLKIND ."') ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //性別取得
    function getSex($model) {
        $query  = " SELECT ";
        $query .= "     NAMECD2 AS VALUE, ";
        $query .= "     ABBV1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR    = '".CTRL_YEAR."' AND ";
        $query .= "     NAMECD1 = 'Z002' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //年組取得（権限チェック）
    function getAuth($model) {
        $query  = " SELECT ";
        $query .= "     GRADE || '_' ||  HR_CLASS AS VALUE, ";
        $query .= "     HR_NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_HDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR        = '".CTRL_YEAR."' AND ";
        $query .= "     SEMESTER    = '".CTRL_SEMESTER."' AND ";
        $query .= "     GRADE       = '".$model->grade."' ";
        //参照・更新可（制限付き）
        if (AUTHORITY == DEF_REFER_RESTRICT || AUTHORITY == DEF_UPDATE_RESTRICT){
            $query .= " AND '".STAFFCD."' IN (TR_CD1, TR_CD2, TR_CD3) ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

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
        $query .= "     T1.SEMESTER = '". CTRL_SEMESTER ."' ";
        if ($model->coursemajor) {
            $query .= " AND T1.COURSECD || T1.MAJORCD = '{$model->coursemajor}' ";
        }
        if ($model->grade) {
            $query .= " AND T1.GRADE = '{$model->grade}' ";
        }
        if ($model->sex) {
            $query .= " AND L1.SEX = '{$model->sex}' ";
        }
        if ($model->grade_hr_class) {
            $query .= " AND T1.GRADE || '_' || T1.HR_CLASS = '{$model->grade_hr_class}' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

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

    //サブシステム名取得
    function getSubSystem() {
        $query  = " SELECT ";
        $query .= "     NAMECD2 AS VALUE, ";
        $query .= "     NAMECD2 || '：' || NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' AND ";
        $query .= "     NAMECD1 = 'I001' ";
        $query .= " ORDER BY ";
        $query .= "     NAMECD2 ";

        return $query;
    }

    //選択フィールド取得
    function getFieldSql($model) {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     KNJI100C_KAKIDASHI_LIST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND DATA_DIV = '06' ";
        $query .= " ORDER BY ";
        $query .= "     SEQ ";

        return $query;
    }

    //選択フィールド削除
    function getDelFieldSql($model) {
        $query  = " DELETE FROM ";
        $query .= "     KNJI100C_KAKIDASHI_LIST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND DATA_DIV = '06' ";

        return $query;
    }

    //選択フィールド保存
    function getFieldInsSql($model, $setSeq, $val) {

        //入金予定項目データ
        $data = array();
        $data["YEAR"][TEXT]            = CTRL_YEAR;
        $data["DATA_DIV"][TEXT]        = "06";
        $data["SEQ"][NUMBER]           = $setSeq;
        $data["FIELD_NAME"][TEXT]      = $val;
        $data["REGISTERCD"][TEXT]      = STAFFCD;
        $data["UPDATED"][FUNC]         = "sysdate()";

        $query = Query::insertSQL($data, "KNJI100C_KAKIDASHI_LIST");
        return $query;
    }
}
?>