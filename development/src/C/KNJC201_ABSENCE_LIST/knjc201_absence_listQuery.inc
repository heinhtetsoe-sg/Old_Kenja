<?php

require_once('for_php7.php');

class knjc201_absence_listQuery extends Query
{

    //学期取得
    public function getTerm($year, $day)
    {
        return " VALUES Y2T(DATE('".$day."'),'N') ";
    }

    //学年データの取得
    public function gradeCombo($model)
    {
        $query  =" SELECT ";
        $query .="     HDAT.GRADE AS VALUE, ";
        $query .="     GDAT.GRADE_NAME1 AS LABEL ";
        $query .=" FROM ";
        $query .="     SCHREG_REGD_HDAT HDAT ";
        $query .="     INNER JOIN SCHREG_REGD_GDAT GDAT ON HDAT.YEAR = GDAT.YEAR ";
        $query .="           AND HDAT.GRADE = GDAT.GRADE ";
        $query .=" WHERE ";
        $query .="     HDAT.YEAR ='".CTRL_YEAR."'  ";
        if (AUTHORITY == DEF_UPDATE_RESTRICT) {
            $query .= " AND  (    HDAT.TR_CD1    = '".STAFFCD."' 
                                OR HDAT.TR_CD2    = '".STAFFCD."' 
                                OR HDAT.TR_CD3    = '".STAFFCD."' 
                                OR HDAT.SUBTR_CD1 = '".STAFFCD."' 
                                OR HDAT.SUBTR_CD2 = '".STAFFCD."' 
                                OR HDAT.SUBTR_CD3 = '".STAFFCD."'
                                OR '".STAFFCD."' IN (SELECT STAFFCD FROM STAFF_DETAIL_MST WHERE YEAR = '".CTRL_YEAR."' AND STAFF_SEQ IN ('005', '006', '007') AND FIELD1 = '0200' AND FIELD2 = HDAT.GRADE)
                                ) ";
        }
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= "   AND GDAT.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind), "','")."') ";
            }
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= "   AND GDAT.SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .=" GROUP BY ";
        $query .="     HDAT.GRADE, ";
        $query .="     GDAT.GRADE_NAME1 ";
        $query .=" ORDER BY ";
        $query .="     VALUE ";
        return $query;
    }

    //クラス取得
    public function classCombo($model)
    {
        $query  = " SELECT ";
        $query .= "     GRADE || '-' || HR_CLASS AS VALUE, ";
        $query .= "     HR_NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_HDAT T1 ";
        $query .= " WHERE ";
        $query .= "     YEAR        = '".CTRL_YEAR."' ";
        $query .= " AND SEMESTER    = '".CTRL_SEMESTER."' ";
        //参照・更新可（制限付き）
        if (AUTHORITY == DEF_REFER_RESTRICT || AUTHORITY == DEF_UPDATE_RESTRICT) {
            $query .= " AND '".STAFFCD."' IN (TR_CD1, TR_CD2, TR_CD3, SUBTR_CD1, SUBTR_CD2, SUBTR_CD3) ";
        }
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= " AND GRADE IN (  SELECT ";
                $query .= "                     GRADE ";
                $query .= "                 FROM ";
                $query .= "                     SCHREG_REGD_GDAT S1 ";
                $query .= "                 WHERE ";
                $query .= "                     S1.YEAR         = T1.YEAR ";
                $query .= "                 AND S1.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind), "','")."') ";
                $query .= "                 ) ";
            }
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND GRADE IN (  SELECT ";
            $query .= "                     GRADE ";
            $query .= "                 FROM ";
            $query .= "                     SCHREG_REGD_GDAT S1 ";
            $query .= "                 WHERE ";
            $query .= "                     S1.YEAR         = T1.YEAR ";
            $query .= "                 AND S1.SCHOOL_KIND  = '".SCHOOLKIND."' ";
            $query .= "                 ) ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //表示用データを取得
    public function readQuery(&$model)
    {
        $query .= "SELECT";
        $query .= "    s1.SCHREGNO, ";
        $query .= "    r.HR_NAMEABBV, ";
        $query .= "    INT(s1.ATTENDNO) AS ATTENDNO, ";
        $query .= "    s2.NAME, ";
        $query .= "    n1.NAME1 AS PERIODCD, ";
        $query .= "    a2.DI_CD, ";
        $query .= "    a2.DI_NAME1, ";
        $query .= "    s3.STAFFNAME, ";
        $query .= "    to_char(a1.UPDATED, 'YYYY/MM/DD HH24:MI') AS UPDATED ";
        $query .= "FROM ";
        $query .= "    SCHREG_REGD_DAT s1  ";
        $query .= "  INNER JOIN SCHREG_REGD_HDAT r     ON s1.YEAR       = r.YEAR ";
        $query .= "                                   AND s1.SEMESTER   = r.SEMESTER";
        $query .= "                                   AND s1.GRADE      = r.GRADE";
        $query .= "                                   AND s1.HR_CLASS   = r.HR_CLASS";
        $query .= "  INNER JOIN SCHREG_BASE_MST s2     ON s1.SCHREGNO   = s2.SCHREGNO ";
        $query .= "  INNER JOIN ATTEND_PETITION_DAT a1 ON s1.SCHREGNO   = a1.SCHREGNO ";
        $query .= "                                   AND s1.YEAR       = a1.YEAR";
        $query .= "  INNER JOIN NAME_MST n1            ON a1.PERIODCD   = n1.NAMECD2 ";
        $query .= "                                   AND n1.NAMECD1    = 'B001'";
        $query .= "  INNER JOIN ATTEND_DI_CD_DAT a2    ON a1.DI_CD      = a2.DI_CD ";
        $query .= "                                   AND s1.YEAR       = a2.YEAR";
        $query .= "  LEFT JOIN STAFF_MST s3            ON a1.REGISTERCD = s3.STAFFCD ";
        $query .= "WHERE s1.YEAR     = '".CTRL_YEAR."' ";
        $query .= "  AND s1.SEMESTER = '".$model->termIs."' ";
        $query .= "  AND to_char(a1.ATTENDDATE, 'YYYY-MM-DD') = '".$model->cntl_dt_key."' ";
        if ($model->GRADE != "") {
            $query .= "   AND s1.GRADE = '".$model->GRADE."' ";
        }
        if ($model->CLASS != "") {
            $query .= "   AND s1.GRADE || '-' || s1.HR_CLASS = '".$model->CLASS."' ";
        }
        $query .= "ORDER BY";
        $query .= "    s1.SCHREGNO, ";
        $query .= "    n1.NAMECD2, ";
        $query .= "    to_char(a1.UPDATED, 'YYYY/MM/DD HH24:MI') ";

        return $query;
    }

    //表示項目の取得
    public function getDispCol()
    {
        $query  = " SELECT COALESCE(MAX(NAMECD2),'1') AS DISP_COL  ";
        $query .= "   FROM V_NAME_MST ";
        $query .= "  WHERE YEAR    = '".CTRL_YEAR."' ";
        $query .= "    AND NAMECD1 = 'C000' ";
        return $query;
    }

    //授業数の取得
    public function getPeriodNum()
    {
        $query  = " SELECT COUNT(*) AS COUNT";
        $query .= "   FROM V_NAME_MST ";
        $query .= "  WHERE YEAR    = '".CTRL_YEAR."' ";
        $query .= "    AND NAMECD1 = 'B001' ";
        return $query;
    }
}
