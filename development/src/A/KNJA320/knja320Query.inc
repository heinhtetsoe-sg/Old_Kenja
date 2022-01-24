<?php

require_once('for_php7.php');

class knja320Query extends Query {

    //中学か高校かを判断---2005.07.07Add
    function getSchoolJudge()
    {
        $db = Query::dbCheckOut();
        //中学か高校かを判断
        $school = $db->getOne("SELECT COUNT(*) FROM SCHOOL_MST WHERE YEAR = '". CTRL_YEAR ."' AND SCHOOLNAME1 LIKE '%中学%'");
        Query::dbCheckIn($db);

        return $school;
    }

    //年組取得（権限チェック）
    function getGradeHrclass($model) {
        $query  = " SELECT ";
        $query .= "     T1.GRADE || HR_CLASS AS VALUE, ";
        $query .= "     HR_NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_HDAT T1 ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= " INNER JOIN SCHREG_REGD_GDAT T2 ";
                $query .= "      ON T2.YEAR         = T1.YEAR ";
                $query .= "     AND T2.GRADE        = T1.GRADE ";
                $query .= "     AND T2.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
            }
        } elseif ($model->Properties["useCurriculumcd"] == '1' && $model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " INNER JOIN SCHREG_REGD_GDAT T2 ";
            $query .= "      ON T2.YEAR         = T1.YEAR ";
            $query .= "     AND T2.GRADE        = T1.GRADE ";
            $query .= "     AND T2.SCHOOL_KIND  = '".SCHOOLKIND."' ";
        }
        $query .= " WHERE ";
        $query .= "     T1.YEAR     = '".CTRL_YEAR."' AND ";
        $query .= "     T1.SEMESTER = '".CTRL_SEMESTER."' ";
        //参照・更新可（制限付き）
        if (AUTHORITY == DEF_REFER_RESTRICT || AUTHORITY == DEF_UPDATE_RESTRICT){
            $query .= " AND (TR_CD1     = '".STAFFCD."' OR ";
            $query .= "      TR_CD2     = '".STAFFCD."' OR ";
            $query .= "      TR_CD3     = '".STAFFCD."' OR ";
            $query .= "      SUBTR_CD1  = '".STAFFCD."' OR ";
            $query .= "      SUBTR_CD2  = '".STAFFCD."' OR ";
            $query .= "      SUBTR_CD3  = '".STAFFCD."') ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //年組名称取得（ＣＳＶ用）//---2005.06.10Add
    function getHrName($model)
    {
        $query  = "SELECT HR_NAME ";
        $query .= "FROM   SCHREG_REGD_HDAT ";
        $query .= "WHERE  YEAR='" .CTRL_YEAR ."' AND ";
        $query .= "       SEMESTER='".CTRL_SEMESTER ."' AND ";
        $query .= "       GRADE || HR_CLASS='".$model->field["GRADE_HR_CLASS"] ."'";

        return $query;
    }


    //ＣＳＶ出力
    function getOutputQueryCsv($model)
    {
        $query  = "WITH SCHNO AS ( ";
        $query .= "    SELECT SCHREGNO,ATTENDNO,YEAR,SEMESTER,GRADE,HR_CLASS ";
        $query .= "    FROM   SCHREG_REGD_DAT ";
        $query .= "    WHERE  YEAR='" .CTRL_YEAR ."' AND ";
        $query .= "           SEMESTER='".CTRL_SEMESTER ."' AND ";
        $query .= "           GRADE||HR_CLASS='".$model->field["GRADE_HR_CLASS"]."' ) ";

        $query .= ",STAFF AS ( ";
        $query .= "    SELECT TR_CD1,YEAR,GRADE,HR_CLASS,HR_NAMEABBV ";
        $query .= "    FROM   SCHREG_REGD_HDAT ";
        $query .= "    WHERE  YEAR='" .CTRL_YEAR ."' AND ";
        $query .= "           SEMESTER='".CTRL_SEMESTER ."' AND ";
        $query .= "           GRADE||HR_CLASS='".$model->field["GRADE_HR_CLASS"]."' ) ";

        $query .= ",ADDRESS_DAT AS ( ";
        $query .= " SELECT ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     T1.TELNO ";
        $query .= " FROM ";
        $query .= "     SCHREG_ADDRESS_DAT T1 ";
        $query .= " INNER JOIN ( ";
        $query .= "     SELECT ";
        $query .= "         SCHREGNO, ";
        $query .= "         MAX(ISSUEDATE) AS ISSUEDATE ";
        $query .= "     FROM ";
        $query .= "         SCHREG_ADDRESS_DAT ";
        $query .= "     GROUP BY ";
        $query .= "         SCHREGNO ";
        $query .= "     ) L1 ON  L1.SCHREGNO = T1.SCHREGNO ";
        $query .= "          AND L1.ISSUEDATE = T1.ISSUEDATE ";
        $query .= " ) ";

        $query .= "SELECT 0 AS ATTENDNO ";
        $query .= "       ,T1.YEAR,T1.GRADE,T1.HR_NAMEABBV ";
        $query .= "       ,T2.STAFFNAME AS NAME ";
        $query .= "       ,T2.STAFFTELNO AS TELNO ";
        $query .= "       ,'".$model->field["STAFF_TELNO"]."' AS TELNO2 ";
        $query .= "FROM   STAFF T1 ";
        $query .= "       LEFT JOIN STAFF_MST T2 ON T2.STAFFCD=T1.TR_CD1 ";
        $query .= "UNION ALL ";
        $query .= "SELECT INT(T1.ATTENDNO) AS ATTENDNO ";
        $query .= "       ,T1.YEAR,T1.GRADE,T4.HR_NAMEABBV ";
        $query .= "       ,T2.NAME ";
        if ($model->field["TEL_NO"] == '1') {
            $query .= "       ,L1.TELNO ";
        } else {
            $query .= "       ,T2.EMERGENCYTELNO AS TELNO ";
        }
        $query .= "       ,'' AS TELNO2 ";
        $query .= "FROM   SCHNO T1 ";
        $query .= "       LEFT JOIN SCHREG_BASE_MST T2 ON T2.SCHREGNO=T1.SCHREGNO ";
        $query .= "       LEFT JOIN SCHREG_REGD_HDAT T4 ON T4.YEAR=T1.YEAR AND T4.SEMESTER=T1.SEMESTER AND ";
        $query .= "                                        T4.GRADE=T1.GRADE AND T4.HR_CLASS=T1.HR_CLASS ";
        $query .= "       LEFT JOIN ADDRESS_DAT L1 ON L1.SCHREGNO=T1.SCHREGNO ";
        $query .= "ORDER BY 1 ";

        return $query;
    }

}
?>
