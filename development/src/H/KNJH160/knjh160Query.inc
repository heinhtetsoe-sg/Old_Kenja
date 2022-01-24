<?php

require_once('for_php7.php');
class knjh160Query extends Query {

    //起動チェック
    function ChecktoStart($db)
    {
        foreach (array("schreg_regd_hdat","v_course_mst","v_major_mst","v_finschool_mst") as $val)
        {
            $query = "SELECT COUNT(*) FROM ".$val." WHERE year = '".CTRL_YEAR."'";
            if ($val == "schreg_regd_hdat") $query .= " AND semester = '".CTRL_SEMESTER."'";

            if ($db->getOne($query) == "0") return false;
        }
        foreach (array("A001","A002","A003","Z002") as $val)
        {
            $query = "SELECT COUNT(*) FROM v_name_mst WHERE year = '".CTRL_YEAR."' AND namecd1 = '".$val."'";
            if ($db->getOne($query) == "0") return false;
        }
        return true;
    }

    //置換画面用の生徒一覧
    function GetStudent($GradeClass)
    {
        $arr = explode(",",$GradeClass);

        $query  = "";
        $query .= "  SELECT";
        $query .= "      T2.name_show ,";
        $query .= "      T1.schregno ,";
        $query .= "      T1.attendno";
        $query .= "  FROM";
        $query .= "      schreg_regd_dat T1";
        $query .= "      LEFT OUTER JOIN schreg_base_mst T2 ON T1.schregno = T2.schregno";
        $query .= "  WHERE";
        $query .= "      T1.year     = '".CTRL_YEAR."' AND";
        $query .= "      T1.semester = '".CTRL_SEMESTER."' AND";
        $query .= "      T1.grade    = '".$arr[0]."' AND";
        $query .= "      T1.hr_class = '".$arr[1]."'";
        $query .= "  ORDER BY";
        $query .= "      T1.attendno";

        return $query;
    }

    //各生徒情報
    function getStudent_data($db, $schregno)
    {

        $query  = "";
        $query .= "  WITH SCHADDTABLE AS ( ";
        $query .= "  SELECT";
        $query .= "      S1.SCHREGNO,S1.ISSUEDATE,S1.EXPIREDATE,S1.ZIPCD,S1.AREACD,N1.NAME1,";
        $query .= "      S1.ADDR1,S1.ADDR2,S1.ADDR1_ENG,S1.ADDR2_ENG,S1.TELNO,S1.FAXNO,EMAIL";
        $query .= "  FROM";
        $query .= "      SCHREG_ADDRESS_DAT S1 ";
        $query .= "      LEFT JOIN V_NAME_MST N1 ON NAMECD1 ='A020' AND NAMECD2 = S1.AREACD AND YEAR = '".CTRL_YEAR."',";
        $query .= "      (SELECT";
        $query .= "          SCHREGNO,MAX(ISSUEDATE) AS ISSUEDATE";
        $query .= "       FROM";
        $query .= "           SCHREG_ADDRESS_DAT ";
        $query .= "       WHERE";
        $query .= "           SCHREGNO = '". $schregno. "' ";
        $query .= "       GROUP BY";
        $query .= "           SCHREGNO ) S2 ";
        $query .= "  WHERE";
        $query .= "      S1.SCHREGNO = '". $schregno. "' ";
        $query .= "      AND  S1.ISSUEDATE = S2.ISSUEDATE";
        $query .= "  ), ";
        $query .= "  AWARDTABLE AS ( ";
        $query .= "  SELECT";
        $query .= "      S1.SCHREGNO,S1.DETAIL_SDATE,S1.BICYCLE_CD,S1.BICYCLE_NO ";
        $query .= "  FROM";
        $query .= "      SCHREG_DETAILHIST_DAT S1, ";
        $query .= "      (SELECT";
        $query .= "          SCHREGNO,MAX(DETAIL_SDATE) AS DETAIL_SDATE ";
        $query .= "       FROM";
        $query .= "           SCHREG_DETAILHIST_DAT ";
        $query .= "       WHERE";
        $query .= "           YEAR = '".CTRL_YEAR."' ";
        $query .= "           AND SCHREGNO = '". $schregno. "' ";
        $query .= "           AND DETAIL_DIV = '3' ";
        $query .= "       GROUP BY";
        $query .= "           SCHREGNO ) S2 ";
        $query .= "  WHERE";
        $query .= "      S1.YEAR = '".CTRL_YEAR."' ";
        $query .= "      AND S1.SCHREGNO = '". $schregno. "' ";
        $query .= "      AND S1.DETAIL_DIV = '3' ";
        $query .= "      AND S1.DETAIL_SDATE = S2.DETAIL_SDATE ";
        $query .= "  )";
        $query .= "  SELECT";
        $query .= "      T1.SCHREGNO, T2.ANNUAL, T1.INOUTCD,N1.NAME1 AS INOUTNM,";
        $query .= "      T1.NAME, T1.NAME_SHOW, T1.NAME_KANA, T1.NAME_ENG,";
        $query .= "      T1.BIRTHDAY, N2.NAME1 AS SEX, T1.BLOODTYPE, T1.BLOOD_RH,";
        $query .= "      T1.ENT_DATE, T1.ENT_DIV, T1.ENT_SCHOOL, T1.ENT_ADDR,";
        $query .= "      T1.GRD_DATE, T1.GRD_DIV, T1.GRD_SCHOOL, T1.GRD_ADDR, T1.GRD_NO,";
        $query .= "      T1.REMARK1, T1.REMARK2, T1.REMARK3,T1.FINISH_DATE,";
        $query .= "      T2.COURSECODE,";
        $query .= "      T1.UPDATED AS UPDATED1,";
        $query .= "      T2.ATTENDNO,";
        $query .= "      T2.COURSECD ||','|| T2.MAJORCD AS COURSEMAJORCD,";
        $query .= "      T2.UPDATED AS UPDATED2,";
        $query .= "      T2.GRADE,";
        $query .= "      T2.GRADE || ',' || T2.HR_CLASS AS GRCL,";
        $query .= "      T4.HR_NAME,";
        $query .= "      T3.ISSUEDATE,T3.EXPIREDATE,T3.ZIPCD,T3.AREACD,T3.NAME1 AS AREANAME,";
        $query .= "      T3.ADDR1,T3.ADDR2,T3.ADDR1_ENG,T3.ADDR2_ENG,T3.TELNO,T3.FAXNO,T3.EMAIL,";
        $query .= "      T1.EMERGENCYCALL,T1.EMERGENCYTELNO,T1.EMERGENCYNAME,T1.EMERGENCYRELA_NAME,";
        $query .= "      T1.EMERGENCYCALL2,T1.EMERGENCYTELNO2,T1.EMERGENCYNAME2,T1.EMERGENCYRELA_NAME2,";
        $query .= "      V1.COURSENAME || V1.MAJORABBV AS COURSE_SUBJECT,";
        $query .= "      V2.COURSECODENAME,";
        $query .= "      V3.FINSCHOOL_NAME, ";
        $query .= "      V4.PRISCHOOL_NAME, ";
        $query .= "      V5.BICYCLE_CD, ";
        $query .= "      V5.BICYCLE_NO ";
        $query .= "  FROM";
        $query .= "      SCHREG_BASE_MST T1 ";
        $query .= "      LEFT JOIN SCHREG_REGD_DAT T2 ON T1.SCHREGNO = T2.SCHREGNO";
        $query .= "      LEFT JOIN SCHADDTABLE T3 ON T1.SCHREGNO = T3.SCHREGNO";
        $query .= "      LEFT JOIN V_NAME_MST N1 ON T2.YEAR = N1.YEAR";
        $query .= "      AND N1.NAMECD1 = 'A001' AND T1.INOUTCD = N1.NAMECD2 ";
        $query .= "      LEFT JOIN V_NAME_MST N2 ON T2.YEAR = N2.YEAR";
        $query .= "      AND N2.NAMECD1 = 'Z002' AND T1.SEX = N2.NAMECD2 ";
        $query .= "      LEFT JOIN SCHREG_REGD_HDAT T4 ON T2.YEAR = T4.YEAR";
        $query .= "      AND T2.SEMESTER = T4.SEMESTER AND T2.GRADE || T2.HR_CLASS = T4.GRADE || T4.HR_CLASS ";
        $query .= "      LEFT JOIN V_COURSE_MAJOR_MST V1 ON T2.YEAR = V1.YEAR";
        $query .= "      AND T2.COURSECD = V1.COURSECD AND T2.MAJORCD = V1.MAJORCD ";
        $query .= "      LEFT JOIN V_COURSECODE_MST V2 ON T2.YEAR = V2.YEAR";
        $query .= "      AND T2.COURSECODE = V2.COURSECODE ";
        $query .= "      LEFT JOIN V_FINSCHOOL_MST V3 ON T2.YEAR = V3.YEAR";
        $query .= "      AND T1.FINSCHOOLCD = V3.FINSCHOOLCD ";
        $query .= "      LEFT JOIN V_PRISCHOOL_MST V4 ON T2.YEAR = V4.YEAR";
        $query .= "      AND T1.PRISCHOOLCD = V4.PRISCHOOLCD ";
        $query .= "      LEFT JOIN AWARDTABLE V5 ON T1.SCHREGNO = V5.SCHREGNO";
        $query .= "  WHERE";
        $query .= "      T1.SCHREGNO = '". $schregno. "' AND";
        $query .= "      T2.YEAR     = '". CTRL_YEAR. "' AND";
        $query .= "      T2.SEMESTER = '". CTRL_SEMESTER. "'";

        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);

        return $row;
    }

    function getOldRegdDat($schregno) {

        $query  = " WITH INSTATE_T AS ( ";
        $query .= " SELECT ";
        $query .= "     T1.YEAR, ";
        $query .= "     MAX(T1.SEMESTER) AS SEMESTER, ";
        $query .= "     T1.SCHREGNO ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR < '". CTRL_YEAR. "' ";
        $query .= "     AND T1.SCHREGNO = '". $schregno. "' ";
        $query .= " GROUP BY ";
        $query .= "     T1.YEAR, ";
        $query .= "     T1.SCHREGNO ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     T1.YEAR, ";
        $query .= "     T1.GRADE, ";
        $query .= "     T1.HR_CLASS, ";
        $query .= "     R_HDAT.HR_NAME, ";
        $query .= "     T1.ATTENDNO, ";
        $query .= "     L1.STAFFNAME AS STAFF1, ";
        $query .= "     L2.STAFFNAME AS STAFF2 ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1 ";
        $query .= "     LEFT JOIN SCHREG_REGD_HDAT R_HDAT ON T1.YEAR = R_HDAT.YEAR ";
        $query .= "          AND T1.SEMESTER = R_HDAT.SEMESTER ";
        $query .= "          AND T1.GRADE = R_HDAT.GRADE ";
        $query .= "          AND T1.HR_CLASS = R_HDAT.HR_CLASS ";
        $query .= "     LEFT JOIN STAFF_MST L1 ON R_HDAT.TR_CD1 = L1.STAFFCD ";
        $query .= "     LEFT JOIN STAFF_MST L2 ON R_HDAT.SUBTR_CD1 = L2.STAFFCD ";
        $query .= " WHERE ";
        $query .= "     EXISTS (SELECT ";
        $query .= "                 T2.SCHREGNO ";
        $query .= "             FROM ";
        $query .= "                 INSTATE_T T2 ";
        $query .= "             WHERE ";
        $query .= "                 T1.YEAR || T1.SEMESTER || T1.SCHREGNO = T2.YEAR || T2.SEMESTER || T2.SCHREGNO  ";
        $query .= "            ) ";
        $query .= " ORDER BY ";
        $query .= "     T1.YEAR, ";
        $query .= "     T1.GRADE, ";
        $query .= "     T1.HR_CLASS, ";
        $query .= "     T1.ATTENDNO ";

        return $query;
    }

}
?>
