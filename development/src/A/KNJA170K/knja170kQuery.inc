<?php

require_once('for_php7.php');

class knja170kQuery extends Query
{

    //年組取得（権限チェック）
    public function getAuth($model, $semester)
    {
        $query = "";
        if ($model->field["CHANGE"] == "1") {
            if ($model->Properties["useSpecial_Support_Hrclass"] == '1' && $model->field["GAKUNEN_KONGOU"] == "1") {
                $query  = " WITH COMBOBASE_T AS ( ";
                $query .= " SELECT DISTINCT ";
                $query .= "     N1.NAMECD2 || T1.SCHOOL_KIND || '-' || T1.HR_CLASS AS VALUE, ";
                $query .= "     N1.ABBV1 || '-' || T1.HR_CLASS || ':' || T1.HR_CLASS_NAME1 AS LABEL ";
                $query .= " FROM V_STAFF_HR_DAT T1 ";
                $query .= "      LEFT JOIN V_NAME_MST N1 ON N1.YEAR = T1.YEAR AND N1.NAMECD1 = 'A023' AND N1.NAME1 = T1.SCHOOL_KIND ";
                $query .= " WHERE ";
                $query .= "     T1.YEAR = '".CTRL_YEAR."' ";
                $query .= "     AND T1.SEMESTER = '".$semester ."' ";
                if ($model->Properties["use_prg_schoolkind"] == "1") {
                    if ($model->selectSchoolKind) {
                        $query .= "     AND T1.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind), "','")."') ";
                    }
                } elseif ($model->Properties["useSchool_KindField"] == "1") {
                    $query .= "     AND T1.SCHOOL_KIND = '".SCHOOLKIND."' ";
                }
                //参照・更新可（制限付き）
                if (AUTHORITY == DEF_REFER_RESTRICT || AUTHORITY == DEF_UPDATE_RESTRICT) {
                    $query .= "     AND T1.STAFFCD = '".STAFFCD."' ";
                }
                $query .= "     AND T1.HR_CLASS_NAME1 IN (SELECT ";
                $query .= "                                  MAX(M1.HR_CLASS_NAME1) AS MAX_NAME  ";
                $query .= "                               FROM ";
                $query .= "                                   V_STAFF_HR_DAT M1 ";
                $query .= "                               WHERE ";
                $query .= "                                   T1.YEAR     = M1.YEAR AND ";
                $query .= "                                   T1.SEMESTER = M1.SEMESTER AND ";
                $query .= "                                   T1.HR_CLASS = M1.HR_CLASS AND ";
                $query .= "                                   T1.SCHOOL_KIND = M1.SCHOOL_KIND AND ";
                $query .= "                                   T1.STAFFCD  = M1.STAFFCD ";
                $query .= "                              ) ";
                $query .= " ), NUMBERING_T AS ( ";
                $query .= " SELECT ";
                $query .= "     ROW_NUMBER() OVER(PARTITION BY VALUE ORDER BY VALUE) AS PRIORI, ";
                $query .= "     VALUE,LABEL ";
                $query .= " FROM ";
                $query .= "   COMBOBASE_T ";
                $query .= " ) ";
                $query .= " SELECT * FROM NUMBERING_T WHERE PRIORI = 1 ORDER BY VALUE, LABEL ";
            } else {
                $query  = " SELECT DISTINCT ";
                $query .= "     REGD_H.GRADE || REGD_H.HR_CLASS AS VALUE, ";
                $query .= "     REGD_H.HR_NAME AS LABEL ";
                $query .= " FROM ";
                $query .= "     SCHREG_REGD_HDAT REGD_H ";
                $query .= " WHERE ";
                $query .= "     REGD_H.YEAR           = '".CTRL_YEAR."' ";
                $query .= "     AND REGD_H.SEMESTER   = '".$semester ."' ";
                if ($model->Properties["use_prg_schoolkind"] == "1") {
                    if ($model->selectSchoolKind) {
                        $query .= "     AND REGD_H.GRADE IN (SELECT ";
                        $query .= "                              REGD_G.GRADE ";
                        $query .= "                          FROM ";
                        $query .= "                              SCHREG_REGD_GDAT REGD_G ";
                        $query .= "                          WHERE ";
                        $query .= "                              REGD_G.YEAR = '".CTRL_YEAR."' ";
                        $query .= "                              AND REGD_G.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind), "','")."')) ";
                    }
                } elseif ($model->Properties["useSchool_KindField"] == "1") {
                    $query .= "     AND REGD_H.GRADE IN (SELECT ";
                    $query .= "                              REGD_G.GRADE ";
                    $query .= "                          FROM ";
                    $query .= "                              SCHREG_REGD_GDAT REGD_G ";
                    $query .= "                          WHERE ";
                    $query .= "                              REGD_G.YEAR = '".CTRL_YEAR."' ";
                    $query .= "                              AND REGD_G.SCHOOL_KIND = '" .SCHOOLKIND ."') ";
                }
                //参照・更新可（制限付き）
                if (AUTHORITY == DEF_REFER_RESTRICT || AUTHORITY == DEF_UPDATE_RESTRICT) {
                    $query .= "     AND (TR_CD1 = '".STAFFCD."' OR ";
                    $query .= "          TR_CD2 = '".STAFFCD."' OR ";
                    $query .= "          TR_CD3 = '".STAFFCD."' OR ";
                    $query .= "          SUBTR_CD1 = '".STAFFCD."' OR ";
                    $query .= "          SUBTR_CD2 = '".STAFFCD."' OR ";
                    $query .= "          SUBTR_CD3 = '".STAFFCD."') ";
                }
                $query .= " ORDER BY ";
                $query .= "     VALUE ";
            }
        } elseif ($model->field["CHANGE"] == "3") {
            $query  = " SELECT DISTINCT ";
            if ($model->Properties["useFi_Hrclass"] != '1' && $model->Properties["useSpecial_Support_Hrclass"] == '1') {
                $query .= "     GHR_CD AS VALUE, ";
                $query .= "     GHR_NAME AS LABEL ";
            } else {
                $query .= "     GRADE || HR_CLASS AS VALUE, ";
                $query .= "     HR_NAME AS LABEL ";
            }
            $query .= " FROM ";
            if ($model->Properties["useFi_Hrclass"] == '1') {
                $query .= "     SCHREG_REGD_FI_HDAT ";
            } elseif ($model->Properties["useSpecial_Support_Hrclass"] == '1') {
                $query .= "     SCHREG_REGD_GHR_HDAT ";
            }
            $query .= " WHERE ";
            $query .= "     YEAR         = '".CTRL_YEAR."' ";
            $query .= "     AND SEMESTER = '".$semester ."' ";
            if ($model->Properties["useFi_Hrclass"] == '1' && $model->Properties["use_prg_schoolkind"] == "1") {
                if ($model->selectSchoolKind) {
                    $query .= "     AND GRADE IN (SELECT ";
                    $query .= "                       REGD_G.GRADE ";
                    $query .= "                   FROM ";
                    $query .= "                       SCHREG_REGD_GDAT REGD_G ";
                    $query .= "                   WHERE ";
                    $query .= "                       REGD_G.YEAR = '".CTRL_YEAR."' ";
                    $query .= "                       AND REGD_G.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind), "','")."')) ";
                }
            } elseif ($model->Properties["useFi_Hrclass"] == '1' && $model->Properties["useSchool_KindField"] == "1") {
                $query .= "     AND GRADE IN (SELECT ";
                $query .= "                       REGD_G.GRADE ";
                $query .= "                   FROM ";
                $query .= "                       SCHREG_REGD_GDAT REGD_G ";
                $query .= "                   WHERE ";
                $query .= "                       REGD_G.YEAR = '".CTRL_YEAR."' ";
                $query .= "                       AND REGD_G.SCHOOL_KIND = '" .SCHOOLKIND ."') ";
            }
            //参照・更新可（制限付き）
            if (AUTHORITY == DEF_REFER_RESTRICT || AUTHORITY == DEF_UPDATE_RESTRICT) {
                $query .= "     AND (TR_CD1 = '".STAFFCD."' OR ";
                $query .= "          TR_CD2 = '".STAFFCD."' OR ";
                $query .= "          TR_CD3 = '".STAFFCD."' OR ";
                $query .= "          SUBTR_CD1 = '".STAFFCD."' OR ";
                $query .= "          SUBTR_CD2 = '".STAFFCD."' OR ";
                $query .= "          SUBTR_CD3 = '".STAFFCD."') ";
            }
            $query .= " ORDER BY ";
            $query .= "     VALUE ";
        } else {
            $query  = " SELECT ";
            $query .= "     T1.GRADE || HR_CLASS AS VALUE, ";
            $query .= "     HR_NAME AS LABEL ";
            $query .= " FROM ";
            $query .= "     SCHREG_REGD_HDAT T1 ";
            if ($model->Properties["use_prg_schoolkind"] == "1") {
                if ($model->selectSchoolKind) {
                    $query .= "INNER JOIN SCHREG_REGD_GDAT T2 ON T2.YEAR = T1.YEAR AND T2.GRADE = T1.GRADE ";
                    $query .= " AND T2.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind), "','")."') ";
                }
            } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
                $query .= "INNER JOIN SCHREG_REGD_GDAT T2 ON T2.YEAR = T1.YEAR AND T2.GRADE = T1.GRADE ";
                $query .= " AND T2.SCHOOL_KIND = '".SCHOOLKIND."' ";
            }
            $query .= " WHERE ";
            $query .= "     T1.YEAR     = '".CTRL_YEAR."' ";
            $query .= " AND SEMESTER = '".$semester ."' ";
            //参照・更新可（制限付き）
            if (AUTHORITY == DEF_REFER_RESTRICT || AUTHORITY == DEF_UPDATE_RESTRICT) {
                $query .= " AND (   TR_CD1      = '".STAFFCD."' ";
                $query .= "      OR TR_CD2      = '".STAFFCD."' ";
                $query .= "      OR TR_CD3      = '".STAFFCD."' ";
                $query .= "      OR SUBTR_CD1   = '".STAFFCD."' ";
                $query .= "      OR SUBTR_CD2   = '".STAFFCD."' ";
                $query .= "      OR SUBTR_CD3   = '".STAFFCD."') ";
            }
            $query .= " ORDER BY ";
            $query .= "     VALUE ";
        }
        return $query;
    }

    //生徒データ取得
    public function getStudent($model)
    {
        $query  = " SELECT ";
        $query .= "     T1.SCHREGNO AS VALUE, ";
        $query .= "     T1.ATTENDNO || '番　' || T2.NAME_SHOW AS LABEL, ";
        $query .= "     T1.ATTENDNO ";
        $query .= " FROM ";
        $query .= "     SCHREG_BASE_MST T2, ";
        $query .= "     SCHREG_REGD_DAT T1 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR     = '".CTRL_YEAR."' ";
        $query .= " AND T1.SEMESTER = '".$model->field["OUTPUT"]."' ";
        $query .= " AND T1.GRADE || T1.HR_CLASS = '".$model->field["GRADE_HR_CLASS"]."' ";
        $query .= " AND T2.SCHREGNO = T1.SCHREGNO ";
        $query .= " ORDER BY ";
        $query .= "     T1.ATTENDNO, ";
        $query .= "     VALUE ";

        return $query;
    }
}
