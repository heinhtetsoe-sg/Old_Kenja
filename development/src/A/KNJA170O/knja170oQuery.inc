<?php

require_once('for_php7.php');

class knja170oQuery extends Query {

    function getSchoolCd() {
        $query  = " SELECT ";
        $query .= "     NAME2 ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = 'Z010' ";
        $query .= "     AND NAMECD2 = '00' ";

        return $query;
    }

    function getSecurityHigh() {
        $query  = " SELECT ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     MENU_HIGH_SECURITY_MST ";
        $query .= " WHERE ";
        $query .= "     PROGRAMID = 'KNJA170O' ";
        $query .= "     AND INVALID_FLG = '0' ";

        return $query;
    }

    //年組取得（権限チェック）
    function getAuth($ga, $model)
    {
        //参照・更新可
        if ($model->auth == DEF_REFERABLE || $model->auth == DEF_UPDATABLE){
            $query  = "SELECT GRADE || HR_CLASS AS VALUE,HR_NAME AS LABEL ";
            $query .= "FROM SCHREG_REGD_HDAT ";
            $query .= "WHERE YEAR='" .CTRL_YEAR ."'";
            $query .= "AND SEMESTER='".$ga ."'";
        }
        //参照・更新可（制限付き）
        if ($model->auth == DEF_REFER_RESTRICT || $model->auth == DEF_UPDATE_RESTRICT){
            $query  = "SELECT GRADE || HR_CLASS AS VALUE,HR_NAME AS LABEL ";
            $query .= "FROM SCHREG_REGD_HDAT ";
            $query .= "WHERE YEAR='" .CTRL_YEAR ."' ";
            $query .= "AND SEMESTER='".$ga ."' ";
            $query .= " AND (TR_CD1 = '" .STAFFCD ."' ";
            $query .= " OR TR_CD2 = '" .STAFFCD ."' ";
            $query .= " OR TR_CD3 = '" .STAFFCD ."') ";
        }

        return $query;
    }


    //１レコード取得
    function getRow($cd)
    {
        $db = Query::dbCheckOut();

        $query = "SELECT * FROM subclass_mst ".
                 " WHERE SUBCLASSCD = '" .$cd ."'";

        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);
        return $row;
    }
    //CSV取得
    function get_class_csv($model)
    {
        $query  = " SELECT HR_NAME ";
        $query .= " FROM SCHREG_REGD_HDAT ";
        $query .= " WHERE YEAR='" .CTRL_YEAR ."'";
        $query .= " AND SEMESTER='".$model->field["OUTPUT"] ."'";
        $query .= " AND GRADE || HR_CLASS = '".$model->field["GRADE_HR_CLASS"] ."'";

        return $query;
    }
    //CSV取得
    function get_teach_csv($model)
    {
        $query  = " SELECT ";
        $query .= "     STAFFNAME_SHOW ";
        $query .= " FROM ";
        $query .= "     STAFF_MST ";
        $query .= " WHERE ";
        $query .= "     STAFFCD = (SELECT ";
        $query .= "                     TR_CD1 ";
        $query .= "                 FROM ";
        $query .= "                     SCHREG_REGD_HDAT ";
        $query .= "                 WHERE ";
        $query .= "                     YEAR='" .CTRL_YEAR ."'AND ";
        $query .= "                     SEMESTER='".$model->field["OUTPUT"] ."'AND ";
        $query .= "                     GRADE || HR_CLASS='".$model->field["GRADE_HR_CLASS"] ."' ";
        $query .= "                 ) ";

        return $query;
    }
    //CSV取得
    function getcsvdata($model,$schregno,$sday,$eday)
    {
        $query  = " SELECT DISTINCT ";
        $query .= " DB1.YEAR, ";
        $query .= " DB1.GRADE, ";
        $query .= " DB1.HR_CLASS, ";
        $query .= " DB1.SEMESTER, ";
        $query .= " DB1.SCHREGNO, ";
        $query .= " DB1.ATTENDNO, ";
        $query .= " DB2.NAME AS SEITO_KANJI, ";
        $query .= " VALUE(DB2.NAME_KANA,'') AS SEITO_KANA, ";
        $query .= " VALUE(CHAR(DB2.BIRTHDAY),'') AS BIRTHDAY, ";
        $query .= " VALUE(DB3.ZIPCD,'') AS ZIPCD1, ";
        $query .= " VALUE(DB3.ADDR1,'') AS ADDRESS1, ";
        $query .= " value(DB3.ADDR2,'') AS ADDRESS2, ";
        $query .= " VALUE(DB3.TELNO,'') AS TELNO1, ";
        $query .= " VALUE(DB4.GUARD_NAME,'') AS GUARD_NAME, ";
        $query .= " VALUE(DB3.EMERGENCYTELNO,'') AS TELNO2, ";
        $query .= " VALUE(DB6.FINSCHOOL_NAME,'')    AS J_NAME ";
        $query .= " FROM  ";
        $query .= " SCHREG_REGD_DAT DB1 ";
        $query .= "     INNER JOIN SCHREG_BASE_MST      DB2 ON DB1.SCHREGNO = DB2.SCHREGNO ";
        $query .= "     INNER JOIN SCHREG_REGD_HDAT     DB7 ON DB1.YEAR = DB7.YEAR ";
        $query .= "                                         AND DB1.SEMESTER = DB7.SEMESTER ";
        $query .= "                                         AND DB1.GRADE = DB7.GRADE ";
        $query .= "                                         AND DB1.HR_CLASS = DB7.HR_CLASS ";
        $query .= "     LEFT  JOIN GUARDIAN_DAT         DB4 ON DB2.SCHREGNO = DB4.SCHREGNO ";
        $query .= "     LEFT  JOIN FINSCHOOL_MST     DB6 ON DB2.FINSCHOOLCD = DB6.FINSCHOOLCD ";
        $query .= "     LEFT JOIN ( ";
        $query .= " SELECT ";
        $query .= "     W1.SCHREGNO, ";
        $query .= "     ZIPCD, ";
        $query .= "     TELNO, ";
        $query .= "     EMERGENCYTELNO, ";
        $query .= "     ADDR1, ";
        $query .= "     ADDR2 ";
        $query .= " FROM ";
        $query .= "     SCHREG_ADDRESS_DAT W1 ";
        $query .= "     LEFT JOIN SCHREG_BASE_MST L1 ON W1.SCHREGNO = L1.SCHREGNO ";    //2006.01.12 m-yama
        $query .= " WHERE ";
        $query .= "     (W1.SCHREGNO,W1.ISSUEDATE) ";
        $query .= " IN ( ";
        $query .= " SELECT ";
        $query .= "     SCHREGNO, ";
        $query .= "     MAX(ISSUEDATE) ";
        $query .= " FROM ";
        $query .= "     SCHREG_ADDRESS_DAT W2 ";
        $query .= " WHERE ";
        $query .= "     W2.ISSUEDATE <= '" .$sday. "' AND ";
        $query .= "     (W2.EXPIREDATE IS NULL OR W2.EXPIREDATE >= '" .$eday. "') AND ";
        $query .= "     W2.SCHREGNO = '" .$schregno. "' ";
        $query .= " GROUP BY ";
        $query .= "     SCHREGNO )  )DB3 ON DB3.SCHREGNO = DB1.SCHREGNO ";
        $query .= " WHERE  ";
        $query .= "     DB1.YEAR = '" .$model->control["年度"]. "' ";
        $query .= "     AND DB1.SEMESTER = '" .$model->field["OUTPUT"]. "' AND  ";
        $query .= "     DB1.GRADE || DB1.HR_CLASS = '" .$model->field["GRADE_HR_CLASS"]. "' ";
        $query .= "     AND DB1.SCHREGNO = '" .$schregno. "' ";
        $query .= "     ORDER BY DB1.ATTENDNO ";

        return $query;
    }
}
?>
