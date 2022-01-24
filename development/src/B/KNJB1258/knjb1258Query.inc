<?php

require_once('for_php7.php');

class knjb1258query extends Query {

    //学期を取得
    function getSemester($model) {
        $query .= " SELECT ";
        $query .= "     YEAR || ':' || SEMESTER AS VALUE, ";
        $query .= "     YEAR || '年度 ' || SEMESTERNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SEMESTER_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR IN ('".CTRL_YEAR."', '".(CTRL_YEAR + 1)."') ";
        $query .= "     AND SEMESTER <> '9' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE DESC ";

        return $query;
    }

    function getSecurityHigh() {
        $query  = " SELECT ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     MENU_HIGH_SECURITY_MST ";
        $query .= " WHERE ";
        $query .= "     PROGRAMID = 'KNJB1258' ";
        $query .= "     AND INVALID_FLG = '0' ";

        return $query;
    }

    // 受講クラスの自動生成:select
    function selectAutoChairCls($year, $semester, $flg)
    {
        $query  = "";

        // HRな講座
        // -- CHAIRCD 単位で処理する
        if ($flg == 0) {
            // -- GROUPCD = '0000'
            // -- 講座にぶら下がる受講クラスがゼロ件であること
            $query .= " WITH GET_CHAIRCD AS ( ";
            $query .= "     select w1.CHAIRCD ";
            $query .= "       from CHAIR_DAT w1 ";
            $query .= "      where w1.YEAR = '$year' ";
            $query .= "        and w1.SEMESTER = '$semester' ";
            $query .= "        and w1.GROUPCD = '0000' ";
            $query .= "        and not exists (select 'X' ";
            $query .= "                          from CHAIR_CLS_DAT w2 ";
            $query .= "                         where w2.YEAR = w1.YEAR ";
            $query .= "                           and w2.SEMESTER = w1.SEMESTER ";
            $query .= "                           and w2.CHAIRCD = w1.CHAIRCD ";
            $query .= "                           and w2.GROUPCD = w1.GROUPCD ) ";
            $query .= "     ) ";
            // -- 名簿を取り込み、受講クラスを算出する
            $query .= " select distinct w1.CHAIRCD, w3.GRADE, w3.HR_CLASS ";
            $query .= "   from CHAIR_STD_DAT w1, ";
            $query .= "        GET_CHAIRCD w2, ";
            $query .= "        SCHREG_REGD_DAT w3 ";
            $query .= "  where w1.YEAR = '$year' ";
            $query .= "    and w1.SEMESTER = '$semester' ";
            $query .= "    and w2.CHAIRCD = w1.CHAIRCD ";
            $query .= "    and w3.SCHREGNO = w1.SCHREGNO ";
            $query .= "    and w3.YEAR = w1.YEAR ";
            $query .= "    and w3.SEMESTER = w1.SEMESTER ";
            $query .= "    and w3.GRADE is not null ";
            $query .= "    and w3.HR_CLASS is not null ";
            $query .= " order by w1.CHAIRCD, w3.GRADE, w3.HR_CLASS ";

        // HRでない講座(群)
        // -- GROUPCD 単位で処理する
        } else {
            // -- GROUPCD <> '0000'
            // -- groupcd に一致する受講クラスがゼロ件であること
            $query .= " WITH GET_CHAIRCD AS ( ";
            $query .= "     select w1.CHAIRCD, w1.GROUPCD ";
            $query .= "       from CHAIR_DAT w1 ";
            $query .= "      where w1.YEAR = '$year' ";
            $query .= "        and w1.SEMESTER = '$semester' ";
            $query .= "        and w1.GROUPCD <> '0000' ";
            $query .= "        and not exists (select 'X' ";
            $query .= "                          from CHAIR_CLS_DAT w2 ";
            $query .= "                         where w2.YEAR = w1.YEAR ";
            $query .= "                           and w2.SEMESTER = w1.SEMESTER ";
            $query .= "                           and w2.CHAIRCD = '0000000' ";
            $query .= "                           and w2.GROUPCD = w1.GROUPCD ) ";
            $query .= "     ) ";
            // -- 上記の全講座の全名簿を取り込み、受講クラスを算出する
            $query .= " select distinct w2.GROUPCD, w3.GRADE, w3.HR_CLASS ";
            $query .= "   from CHAIR_STD_DAT w1, ";
            $query .= "        GET_CHAIRCD w2, ";
            $query .= "        SCHREG_REGD_DAT w3 ";
            $query .= "  where w1.YEAR = '$year' ";
            $query .= "    and w1.SEMESTER = '$semester' ";
            $query .= "    and w2.CHAIRCD = w1.CHAIRCD ";
            $query .= "    and w3.SCHREGNO = w1.SCHREGNO ";
            $query .= "    and w3.YEAR = w1.YEAR ";
            $query .= "    and w3.SEMESTER = w1.SEMESTER ";
            $query .= "    and w3.GRADE is not null ";
            $query .= "    and w3.HR_CLASS is not null ";
            $query .= " order by w2.GROUPCD, w3.GRADE, w3.HR_CLASS ";
        }

        return $query;
    }


    // 受講クラスの自動生成:insert
    function insertAutoChairCls($data, $year, $semester, $registercd, $flg)
    {
        $auto_data = array();
        $auto_data["YEAR"][TEXT]        = $year;
        $auto_data["SEMESTER"][TEXT]    = $semester;
        $auto_data["CHAIRCD"][TEXT]     = ($flg == 0) ? $data["CHAIRCD"] : "0000000";
        $auto_data["GROUPCD"][TEXT]     = ($flg == 0) ? "0000" : $data["GROUPCD"];
        $auto_data["TRGTGRADE"][TEXT]   = $data["GRADE"];
        $auto_data["TRGTCLASS"][TEXT]   = $data["HR_CLASS"];
        $auto_data["REGISTERCD"][TEXT]  = $registercd;
        $auto_data["UPDATED"][NUMBER]   = "sysdate()";

        $query = Query::insertSQL($auto_data, "chair_cls_dat");
        return $query;
    }
}
?>
