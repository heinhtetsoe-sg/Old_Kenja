<?php

require_once('for_php7.php');

class knjl426query extends Query {

    // 今年度生成:insert
    function insertRecruitDat() {
        $query  = " INSERT INTO RECRUIT_PS_DAT ";
        $query .= " SELECT ";
        $query .= "     '".(CTRL_YEAR+1)."', ";
        $query .= "     RECRUIT_NO, ";
        $query .= "     NAME, ";
        $query .= "     NAME_KANA, ";
        $query .= "     PRISCHOOLCD, ";
        $query .= "     PRISCHOOL_CLASS_CD, ";
        $query .= "     PRISCHOOL_TEACHER, ";
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
        $query .= "     RECRUIT_PS_DAT LAST_D ";
        $query .= " WHERE ";
        $query .= "         YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND NOT EXISTS( ";
        $query .= "             SELECT ";
        $query .= "                 'x' ";
        $query .= "             FROM ";
        $query .= "                 RECRUIT_PS_DAT COPY_D ";
        $query .= "             WHERE ";
        $query .= "                     COPY_D.YEAR       = '".(CTRL_YEAR+1)."' ";
        $query .= "                 AND COPY_D.RECRUIT_NO = LAST_D.RECRUIT_NO ";
        $query .= "     ) ";

        return $query;
    }

    // 今年度生成:insert
    function insertPrischoolClassDetailYdat() {
        $query  = " INSERT INTO PRISCHOOL_CLASS_DETAIL_YDAT ";
        $query .= " SELECT ";
        $query .= "     '".(CTRL_YEAR+1)."', ";
        $query .= "     PRISCHOOLCD, ";
        $query .= "     PRISCHOOL_CLASS_CD, ";
        $query .= "     SEQ, ";
        $query .= "     REMARK1, ";
        $query .= "     REMARK2, ";
        $query .= "     REMARK3, ";
        $query .= "     REMARK4, ";
        $query .= "     REMARK5, ";
        $query .= "     REMARK6, ";
        $query .= "     REMARK7, ";
        $query .= "     REMARK8, ";
        $query .= "     REMARK9, ";
        $query .= "     REMARK10, ";
        $query .= "     '".STAFFCD."', ";
        $query .= "     sysdate() ";
        $query .= " FROM ";
        $query .= "     PRISCHOOL_CLASS_DETAIL_YDAT LAST_D ";
        $query .= " WHERE ";
        $query .= "         YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND NOT EXISTS( ";
        $query .= "             SELECT ";
        $query .= "                 'x' ";
        $query .= "             FROM ";
        $query .= "                 PRISCHOOL_CLASS_DETAIL_YDAT COPY_D ";
        $query .= "             WHERE ";
        $query .= "                     COPY_D.YEAR       = '".(CTRL_YEAR+1)."' ";
        $query .= "                 AND COPY_D.PRISCHOOLCD = LAST_D.PRISCHOOLCD ";
        $query .= "                 AND COPY_D.PRISCHOOL_CLASS_CD = LAST_D.PRISCHOOL_CLASS_CD ";
        $query .= "                 AND COPY_D.SEQ = LAST_D.SEQ ";
        $query .= "     ) ";

        return $query;
    }
}
?>
