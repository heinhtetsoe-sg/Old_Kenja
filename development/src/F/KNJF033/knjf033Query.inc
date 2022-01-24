<?php

require_once('for_php7.php');

class knjf033Query extends Query {

    //学年取得
    function getGrade($model)
    {
        $query  = " SELECT ";
        $query .= "     GRADE AS VALUE, ";
        $query .= "     GRADE_NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_GDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= " AND SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
            }
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //年組取得（権限チェック）
    function getHrClass($model)
    {
        //参照・更新可
        $query  = " SELECT ";
        $query .= "     GRADE || HR_CLASS AS VALUE, ";
        $query .= "     HR_NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_HDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR        = '".CTRL_YEAR."' AND ";
        $query .= "     SEMESTER    = '".CTRL_SEMESTER."' AND ";
        //参照・更新可（制限付き）
        if (AUTHORITY == DEF_REFER_RESTRICT || AUTHORITY == DEF_UPDATE_RESTRICT) {
            $query .= "     (TR_CD1     = '".STAFFCD."' OR ";
            $query .= "      TR_CD2     = '".STAFFCD."' OR ";
            $query .= "      TR_CD3     = '".STAFFCD."' OR ";
            $query .= "      SUBTR_CD1  = '".STAFFCD."' OR ";
            $query .= "      SUBTR_CD2  = '".STAFFCD."' OR ";
            $query .= "      SUBTR_CD3  = '".STAFFCD."') AND ";
        }
        $query .= "     GRADE   = '".$model->field["GRADE"]."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //名称マスタ取得
    function getNameMst($namecd1, $namecd2)
    {
        $query  = " SELECT ";
        $query .= "     NAME1 ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = '".$namecd1."' AND ";
        $query .= "     NAMECD2 = '".$namecd2."' ";

        return $query;
    }

    //検診データ取得
    function getSelectCsv($model)
    {
        $query  = " WITH SCHINFO AS ( ";
        $query .= "     SELECT ";
        $query .= "         T1.SCHREGNO, ";
        $query .= "         T1.GRADE, ";
        $query .= "         T1.HR_CLASS, ";
        $query .= "         T1.ATTENDNO, ";
        $query .= "         T2.NAME, ";
        $query .= "         T2.SEX ";
        $query .= "     FROM ";
        $query .= "         SCHREG_REGD_DAT T1 ";
        $query .= "     LEFT JOIN ";
        $query .= "         SCHREG_BASE_MST T2 ON T1.SCHREGNO = T2.SCHREGNO ";
        $query .= "     WHERE ";
        $query .= "         T1.YEAR = '".CTRL_YEAR."' AND ";
        $query .= "         T1.SEMESTER = '".CTRL_SEMESTER."' AND ";
        $query .= "         T1.GRADE || T1.HR_CLASS IN ('".$model->selectdata."') ";
        $query .= " ) ";

        $query .= " SELECT ";
        $query .= "     T2.GRADE, ";
        $query .= "     T2.HR_CLASS, ";
        $query .= "     T2.ATTENDNO, ";
        $query .= "     T2.NAME, ";
        $query .= "     T2.SEX, ";
        $query .= "     T1.HEIGHT, ";
        $query .= "     T1.WEIGHT, ";
        $query .= "     T1.SITHEIGHT, ";
        $query .= "     '' AS SHINTAIHI, ";
        $query .= "     T1.NUTRITIONCD, ";
        $query .= "     T1.R_BAREVISION_MARK, ";
        $query .= "     T1.L_BAREVISION_MARK, ";
        $query .= "     T1.R_VISION_MARK, ";
        $query .= "     T1.L_VISION_MARK, ";
        $query .= "     T1.EYEDISEASECD, ";
        $query .= "     T1.R_EAR, ";
        $query .= "     T1.L_EAR, ";
        $query .= "     T1.TB_REMARKCD, ";
        $query .= "     T1.TB_NAMECD, ";
        $query .= "     T1.HEART_MEDEXAM, ";
        $query .= "     T1.HEARTDISEASECD, ";
        $query .= "     T1.ALBUMINURIA1CD, ";
        $query .= "     T1.URICSUGAR1CD, ";
        $query .= "     T1.URICBLEED1CD, ";
        $query .= "     T1.ALBUMINURIA2CD, ";
        $query .= "     T1.URICSUGAR2CD, ";
        $query .= "     T1.URICBLEED2CD, ";
        $query .= "     '' AS HINKETU, ";
        $query .= "     '' AS HINKETU_REMARK, ";
        $query .= "     T1.DOC_CD, ";
        $query .= "     T1.OTHERDISEASECD ";
        $query .= " FROM ";
        $query .= "     MEDEXAM_DET_DAT T1 ";
        $query .= " INNER JOIN ";
        $query .= "     SCHINFO T2 ON T1.SCHREGNO = T2.SCHREGNO ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' ";
        $query .= " ORDER BY ";
        $query .= "     T2.GRADE, ";
        $query .= "     T2.HR_CLASS, ";
        $query .= "     T2.ATTENDNO ";

        return $query;
    }
}
?>
