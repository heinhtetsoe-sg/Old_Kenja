<?php

require_once('for_php7.php');

class knjl416query extends Query {

    // 今年度生成:insert
    function insertRecruitDat()
    {
        $query  = " INSERT INTO RECRUIT_DAT ";
        $query .= " SELECT ";
        $query .= "     '".(CTRL_YEAR+1)."', ";
        $query .= "     RECRUIT_NO, ";
        $query .= "     NAME, ";
        $query .= "     NAME_KANA, ";
        $query .= "     SCHOOL_KIND, ";
        $query .= "     SEX, ";
        $query .= "     BIRTHDAY, ";
        $query .= "     FINSCHOOLCD, ";
        $query .= "     '0' || CAST(INT(GRADE) + 1 AS CHAR(1)), ";
        $query .= "     SCHOOL_TEACHER, ";
        $query .= "     PRISCHOOLCD, ";
        $query .= "     PRISCHOOL_CLASS_CD, ";
        $query .= "     PRISCHOOL_TEACHER, ";
        $query .= "     GUARD_NAME, ";
        $query .= "     GUARD_KANA, ";
        $query .= "     ZIPCD, ";
        $query .= "     ADDR1, ";
        $query .= "     ADDR2, ";
        $query .= "     TELNO, ";
        $query .= "     TELNO2, ";
        $query .= "     FAXNO, ";
        $query .= "     EMAIL, ";
        $query .= "     REMARK, ";
        $query .= "     '".STAFFCD."', ";
        $query .= "     sysdate() ";
        $query .= " FROM ";
        $query .= "     RECRUIT_DAT LAST_D ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND ((SCHOOL_KIND = 'J' AND VALUE(GRADE, '06') != '06') ";
        $query .= "           OR ";
        $query .= "          (SCHOOL_KIND = 'H' AND VALUE(GRADE, '09') != '09')) ";
        $query .= "     AND NOT EXISTS( ";
        $query .= "             SELECT ";
        $query .= "                 'x' ";
        $query .= "             FROM ";
        $query .= "                 RECRUIT_DAT COPY_D ";
        $query .= "             WHERE ";
        $query .= "                 COPY_D.YEAR = '".(CTRL_YEAR+1)."' ";
        $query .= "                 AND COPY_D.RECRUIT_NO = LAST_D.RECRUIT_NO ";
        $query .= "     ) ";

        return $query;
    }
}
?>
