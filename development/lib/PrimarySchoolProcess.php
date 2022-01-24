<?php

require_once('for_php7.php');

# kanji=漢字
# $Id: PrimarySchoolProcess.php,v 1.1 2011/11/08 07:40:59 m-yama Exp $

//共通関数
class PrimarySchoolProcess{

    function delInsUnitSchChrRankDat(
            $db,
            $year
    ) {
        $query = "DELETE FROM UNIT_SCH_CHR_RANK_DAT WHERE YEAR = '{$year}' ";
        $db->query($query);

        $query  = " INSERT INTO UNIT_SCH_CHR_RANK_DAT ( ";
        $query .= "     SELECT ";
        $query .= "          T1.EXECUTEDATE, ";
        $query .= "          T1.PERIODCD, ";
        $query .= "          T1.CHAIRCD, ";
        $query .= "          T1.YEAR, ";
        $query .= "          T1.SEMESTER, ";
        $query .= "          RANK() OVER(PARTITION BY T1.CHAIRCD ORDER BY T1.EXECUTEDATE, T1.PERIODCD) AS RANK, ";
        $query .= "          REGISTERCD, ";
        $query .= "          sysdate() ";
        $query .= "      FROM ";
        $query .= "          SCH_CHR_DAT T1 ";
        $query .= "      WHERE ";
        $query .= "          YEAR = '{$year}' ";
        $query .= " ) ";
        $db->query($query);
    }

}
?>
