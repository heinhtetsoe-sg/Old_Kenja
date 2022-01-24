<?php

require_once('for_php7.php');


class knja139aQuery extends Query {

    //年組取得（権限チェック）
    function getAuth($model)
    {
        //参照・更新可
        if (AUTHORITY == DEF_REFERABLE || AUTHORITY == DEF_UPDATABLE) {
            $query  = "SELECT GRADE || HR_CLASS AS VALUE,HR_NAME AS LABEL ";
            $query .= "FROM SCHREG_REGD_HDAT ";
            $query .= "WHERE YEAR='" .CTRL_YEAR ."'";
            $query .= "AND SEMESTER='".CTRL_SEMESTER ."'";
        }
        //参照・更新可（制限付き）
        if (AUTHORITY == DEF_REFER_RESTRICT || AUTHORITY == DEF_UPDATE_RESTRICT) {
            $query  = "SELECT GRADE || HR_CLASS AS VALUE,HR_NAME AS LABEL ";
            $query .= "FROM SCHREG_REGD_HDAT ";
            $query .= "WHERE YEAR='" .CTRL_YEAR ."' ";
            $query .= "AND SEMESTER='".CTRL_SEMESTER ."' ";
            $query .= " AND (TR_CD1 = '" .STAFFCD ."' ";
            $query .= " OR TR_CD2 = '" .STAFFCD ."' ";
            $query .= " OR TR_CD3 = '" .STAFFCD ."') ";
        }

        return $query;
    }

    //学年取得（権限チェック）
    function getAuth2($model)
    {
        //参照・更新可
            $query  = "SELECT DISTINCT GRADE AS VALUE,cast(cast(GRADE as int) as char) || '学年' AS LABEL ";
            $query .= "FROM SCHREG_REGD_HDAT ";
            $query .= "WHERE YEAR='" .CTRL_YEAR ."'";
            $query .= "AND SEMESTER='".CTRL_SEMESTER ."'";

        return $query;
    }

    //名称マスタ
    function getNameMst($namecd1, $namecd2)
    {
        $query  = "SELECT * ";
        $query .= "FROM V_NAME_MST ";
        $query .= "WHERE YEAR='" .CTRL_YEAR ."'";
        $query .= "AND NAMECD1='".$namecd1 ."'";
        $query .= "AND NAMECD2='".$namecd2 ."'";
        return $query;
    }

    function getCsvQuery($select_data)
    {
        $c = "";
        $selectSql = "(";
        foreach ($select_data as $s) {
            $selectSql .= $c."'".$s."'";
            $c = ",";
        }
        $selectSql .= ")";

        $query .= "  WITH SCHREGNOS AS (";
        $query .= "   SELECT T1.SCHREGNO, T2.SCHOOL_KIND ";
        $query .= "   FROM SCHREG_REGD_DAT T1 ";
        $query .= "   INNER JOIN SCHREG_REGD_GDAT T2 ON T2.YEAR = T1.YEAR ";
        $query .= "     AND T2.GRADE = T1.GRADE ";
        $query .= "   WHERE T1.YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND T1.SEMESTER = '".CTRL_SEMESTER."' ";
        if ("2" == $this->field["OUTPUT"]) {
            $query .= " AND T1.GRADE || T1.HR_CLASS IN ".$selectSql." ";
        } else {
            $query .= " AND T1.SCHREGNO IN ".$selectSql." ";
        }
        $query .= "  ), ENT_GRD_YEARS AS (";
        $query .= "       SELECT '0' AS SCHOOLDIV, T4.SCHREGNO, T4.YEAR, T4.GRADE, T4_2.GRADE_CD ";
        $query .= "       FROM SCHOOL_MST T2 ";
        $query .= "       LEFT JOIN ( ";
        $query .= "           SELECT SCHREGNO, YEAR, GRADE FROM V_REGDYEAR_GRADE_DAT ";
        $query .= "       ) T4 ON T4.YEAR = T2.YEAR ";
        $query .= "       LEFT JOIN SCHREG_REGD_GDAT T4_2 ON T4_2.YEAR = T4.YEAR AND T4_2.GRADE = T4.GRADE ";
        $query .= "       WHERE T2.SCHOOLDIV = '0' ";
        $query .= "       UNION ALL ";
        $query .= "       SELECT '1' AS SCHOOLDIV, T5.SCHREGNO, T5.YEAR, T5.GRADE, T5_2.GRADE_CD ";
        $query .= "       FROM SCHOOL_MST T2 ";
        $query .= "       LEFT JOIN ( ";
        $query .= "           SELECT SCHREGNO, YEAR, GRADE FROM V_REGDYEAR_UNIT_DAT ";
        $query .= "       ) T5 ON T5.YEAR = T2.YEAR  ";
        $query .= "       LEFT JOIN SCHREG_REGD_GDAT T5_2 ON T5_2.YEAR = T5.YEAR AND T5_2.GRADE = T5.GRADE ";
        $query .= "       WHERE T2.SCHOOLDIV = '1' ";
        $query .= "  ) ";
        $query .= "  , STATE_MAIN AS (SELECT ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     FISCALYEAR(ENT_DATE) AS ENT_YEAR, ";
        $query .= "     ENT_DATE, ";
        $query .= "     ENT_REASON, ";
        $query .= "     ENT_SCHOOL, ";
        $query .= "     ENT_ADDR, ";
        if ("1" == $this->Properties["useAddrField2"]) {
            $query .= "     ENT_ADDR2,";
        } else {
            $query .= "     CAST(NULL AS VARCHAR(1)) AS ENT_ADDR2,";
        }
        $query .= "     ENT_DIV, ";
        $query .= "     T3.NAME1 AS ENT_DIV_NAME, ";
        $query .= "     FISCALYEAR(GRD_DATE) AS GRD_YEAR, ";
        $query .= "     GRD_DATE, ";
        $query .= "     GRD_REASON, ";
        $query .= "     GRD_SCHOOL, ";
        $query .= "     GRD_ADDR, ";
        if ("1" == $this->Properties["useAddrField2"]) {
            $query .= "     GRD_ADDR2,";
        } else {
            $query .= "     CAST(NULL AS VARCHAR(1)) AS GRD_ADDR2,";
        }
        $query .= "     GRD_NO, ";
        $query .= "     GRD_DIV, ";
        $query .= "     T4.NAME1 AS GRD_DIV_NAME, ";
        $query .= "     T1.CURRICULUM_YEAR, ";
        $query .= "     T1.TENGAKU_SAKI_ZENJITU, ";
        $query .= "     T1.NYUGAKUMAE_SYUSSIN_JOUHOU ";
        $query .= "  FROM ";
        $query .= "     SCHREG_ENT_GRD_HIST_DAT T1 ";
        $query .= "     INNER JOIN SCHREGNOS T2 ON T2.SCHREGNO = T1.SCHREGNO AND T2.SCHOOL_KIND = T1.SCHOOL_KIND ";
        $query .= "     LEFT JOIN NAME_MST T3 ON T3.NAMECD1='A002' AND T3.NAMECD2 = T1.ENT_DIV ";
        $query .= "     LEFT JOIN NAME_MST T4 ON T4.NAMECD1='A003' AND T4.NAMECD2 = T1.GRD_DIV ";
        $query .= "  ), STATE AS ( ";
        $query .= "  SELECT ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     T1.ENT_YEAR, ";
        $query .= "     T1.ENT_DATE, ";
        $query .= "     T1.ENT_REASON, ";
        $query .= "     T1.ENT_SCHOOL, ";
        $query .= "     T1.ENT_ADDR, ";
        $query .= "     T1.ENT_ADDR2,";
        $query .= "     T1.ENT_DIV, ";
        $query .= "     T1.ENT_DIV_NAME, ";
        $query .= "     T1.GRD_YEAR, ";
        $query .= "     T1.GRD_DATE, ";
        $query .= "     T1.GRD_REASON, ";
        $query .= "     T1.GRD_SCHOOL, ";
        $query .= "     T1.GRD_ADDR, ";
        $query .= "     T1.GRD_ADDR2,";
        $query .= "     T1.GRD_NO, ";
        $query .= "     T1.GRD_DIV, ";
        $query .= "     T1.GRD_DIV_NAME, ";
        $query .= "     T1.CURRICULUM_YEAR, ";
        $query .= "     T1.TENGAKU_SAKI_ZENJITU, ";
        $query .= "     T1.NYUGAKUMAE_SYUSSIN_JOUHOU, ";
        $query .= "     YE.GRADE AS ENT_YEAR_GRADE, ";
        $query .= "     YE.GRADE_CD AS ENT_YEAR_GRADE_CD, ";
        $query .= "     YG.GRADE AS GRD_YEAR_GRADE, ";
        $query .= "     YG.GRADE_CD AS GRD_YEAR_GRADE_CD ";
        $query .= "  FROM STATE_MAIN T1 ";
        $query .= "     LEFT JOIN ENT_GRD_YEARS YE ON YE.YEAR = T1.ENT_YEAR AND YE.SCHREGNO = T1.SCHREGNO ";
        $query .= "     LEFT JOIN ENT_GRD_YEARS YG ON YG.YEAR = T1.GRD_YEAR AND YG.SCHREGNO = T1.SCHREGNO ";
        $query .= "  ) ";
        $query .= " , TA as( select ";
        $query .= "          T1.SCHREGNO, ";
        $query .= "          '0' as SCH_SENKOU_KIND, ";
        $query .= "          MAX(case when SENKOU_KIND = '0' then YEAR else '-1' end) as SCH_YEAR, ";
        $query .= "          '1' as COMP_SENKOU_KIND, ";
        $query .= "          MAX(case when SENKOU_KIND = '1' then YEAR else '-1' end) as COMP_YEAR ";
        $query .= "  from ";
        $query .= "          AFT_GRAD_COURSE_DAT T1 ";
        $query .= "          INNER JOIN SCHREGNOS T2 ON T2.SCHREGNO = T1.SCHREGNO ";
        $query .= "  where ";
        $query .= "          PLANSTAT = '1'";
        $query .= "  group by ";
        $query .= "          T1.SCHREGNO ";
        $query .= " ), TA2 as( select ";
        $query .= "      (case when TA.SCH_YEAR >= TA.COMP_YEAR then TA.SCH_YEAR else TA.COMP_YEAR end) as YEAR, ";
        $query .= "      T1.SCHREGNO, ";
        $query .= "      T1.SENKOU_KIND, ";
        $query .= "      MAX(T1.SEQ) AS SEQ ";
        $query .= "  from ";
        $query .= "      AFT_GRAD_COURSE_DAT T1 ";
        $query .= "  inner join TA on ";
        $query .= "      T1.SCHREGNO = TA.SCHREGNO ";
        $query .= "      and T1.YEAR = (case when TA.SCH_YEAR >= TA.COMP_YEAR then TA.SCH_YEAR else TA.COMP_YEAR end) ";
        $query .= "      and T1.SENKOU_KIND = (case when TA.SCH_YEAR >= TA.COMP_YEAR then TA.SCH_SENKOU_KIND else TA.COMP_SENKOU_KIND end) ";
        $query .= "  where ";
        $query .= "      T1.PLANSTAT = '1'";
        $query .= "  group by ";
        $query .= "      (case when TA.SCH_YEAR >= TA.COMP_YEAR then TA.SCH_YEAR else TA.COMP_YEAR end), ";
        $query .= "      T1.SCHREGNO, ";
        $query .= "      T1.SENKOU_KIND ";
        $query .= " ), AFT_MAIN AS ( ";
        $query .= " select  ";
        $query .= "       T1.SCHREGNO ";
        $query .= "      ,T1.SENKOU_KIND ";
        $query .= "      ,T1.STAT_CD ";
        $query .= "      ,T1.THINKEXAM ";
        $query .= "      ,T1.JOB_THINK ";
        $query .= "      ,L1.NAME1 as E017NAME1 ";
        $query .= "      ,L2.NAME1 as E018NAME1 ";
        $query .= "      ,L3.SCHOOL_NAME ";
        $query .= "      ,T1.FACULTYCD ";
        $query .= "      ,L5.FACULTYNAME ";
        $query .= "      ,T1.DEPARTMENTCD ";
        $query .= "      ,L6.DEPARTMENTNAME ";
        $query .= "      ,L7.ADDR1 AS CAMPUSADDR1 ";
        $query .= "      ,L8.ADDR1 AS CAMPUSFACULTYADDR1 ";
        $query .= "      ,L4.COMPANY_NAME ";
        $query .= "      ,L4.ADDR1 AS COMPANYADDR1 ";
        $query .= "      ,L4.ADDR2 AS COMPANYADDR2 ";
        $query .= "      ,ROW_NUMBER() OVER(ORDER BY T1.YEAR) AS ROW_NUM ";
        $query .= " from ";
        $query .= "      AFT_GRAD_COURSE_DAT T1 ";
        $query .= " inner join TA2 on ";
        $query .= "      T1.YEAR = TA2.YEAR ";
        $query .= "      and T1.SCHREGNO = TA2.SCHREGNO ";
        $query .= "      and T1.SENKOU_KIND = TA2.SENKOU_KIND ";
        $query .= "      and T1.SEQ = TA2.SEQ ";
        $query .= " left join NAME_MST L1 on L1.NAMECD1 = 'E017' and L1.NAME1 = T1.STAT_CD ";
        $query .= " left join NAME_MST L2 on L2.NAMECD1 = 'E018' and L2.NAME1 = T1.STAT_CD ";
        $query .= " left join COLLEGE_MST L3 on L3.SCHOOL_CD = T1.STAT_CD ";
        $query .= " left join COLLEGE_FACULTY_MST L5 on L5.SCHOOL_CD = L3.SCHOOL_CD ";
        $query .= "      and L5.FACULTYCD = T1.FACULTYCD ";
        $query .= " left join COLLEGE_DEPARTMENT_MST L6 on L6.SCHOOL_CD = L3.SCHOOL_CD ";
        $query .= "      and L6.FACULTYCD = T1.FACULTYCD ";
        $query .= "      and L6.DEPARTMENTCD = T1.DEPARTMENTCD ";
        $query .= " left join COLLEGE_CAMPUS_ADDR_DAT L7 on L7.SCHOOL_CD = L3.SCHOOL_CD ";
        $query .= "      and L7.CAMPUS_ADDR_CD = L3.CAMPUS_ADDR_CD ";
        $query .= " left join COLLEGE_CAMPUS_ADDR_DAT L8 on L8.SCHOOL_CD = L5.SCHOOL_CD ";
        $query .= "      and L8.CAMPUS_ADDR_CD = L5.CAMPUS_ADDR_CD ";
        $query .= " left join COMPANY_MST L4 on L4.COMPANY_CD = T1.STAT_CD ";
        $query .= " where ";
        $query .= "      T1.PLANSTAT = '1' ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= " T2.NAME,";
        $query .= " T2.REAL_NAME,";
        $query .= " ENTGRD.GRD_DATE, ";
        $query .= " T2.NAME_ENG,";
        $query .= " T2.NAME_KANA,T2.REAL_NAME_KANA,T2.BIRTHDAY,T7.ABBV1 AS SEX,";
        $query .= " T21.BIRTHDAY_FLG,";
        $query .= " T7.ABBV2 AS SEX_ENG,";
        $query .= " T1.GRADE,T1.ATTENDNO,T1.ANNUAL,T6.HR_NAME,";
        $query .= " T3.COURSENAME,";
        $query .= " T4.MAJORNAME,";
        $query .= " T5.COURSECODENAME,T3.COURSEABBV,T4.MAJORABBV,";
        $query .= " T3.COURSEENG,T4.MAJORENG,";
        $query .= " ENTGRD.FINISH_DATE,";
        $query .= " FIN_S.FINSCHOOL_ZIPCD AS J_ZIPCD,";
        $query .= " FIN_S.FINSCHOOL_NAME AS J_NAME,";
        $query .= " NM_MST.NAME1 AS INSTALLATION_DIV,";
        $query .= " VALUE(NML019.NAME1, '') AS FINSCHOOL_TYPE_NAME,";
        $query .= " SCHADD.ADDR1, SCHADD.ADDR2, SCHADD.ZIPCD, SCHADD.TELNO, SCHADD.EMAIL, ";
        $query .= " T12.GUARD_NAME, ";
        $query .= " T12.GUARD_KANA,";
        $query .= " T12.GUARD_ADDR1, T12.GUARD_ADDR2, T12.GUARD_ZIPCD, T12.GUARD_TELNO,";
        $query .= " T1.SCHREGNO, ";
        $query .= "     STT.ENT_YEAR, ";
        $query .= "     STT.ENT_DATE, ";
        $query .= "     STT.ENT_REASON, ";
        $query .= "     STT.ENT_SCHOOL, ";
        $query .= "     STT.ENT_ADDR, ";
        $query .= "     STT.ENT_ADDR2,";
        $query .= "     STT.ENT_DIV, ";
        $query .= "     STT.ENT_DIV_NAME, ";
        $query .= "     STT.GRD_YEAR, ";
        $query .= "     STT.GRD_DATE, ";
        $query .= "     STT.GRD_REASON, ";
        $query .= "     STT.GRD_SCHOOL, ";
        $query .= "     STT.GRD_ADDR, ";
        $query .= "     STT.GRD_ADDR2,";
        $query .= "     STT.GRD_NO, ";
        $query .= "     STT.GRD_DIV, ";
        $query .= "     STT.GRD_DIV_NAME, ";
        $query .= "     STT.CURRICULUM_YEAR, ";
        $query .= "     STT.TENGAKU_SAKI_ZENJITU, ";
        $query .= "     STT.NYUGAKUMAE_SYUSSIN_JOUHOU, ";
        $query .= "     STT.ENT_YEAR_GRADE, ";
        $query .= "     STT.ENT_YEAR_GRADE_CD, ";
        $query .= "     STT.GRD_YEAR_GRADE, ";
        $query .= "     STT.GRD_YEAR_GRADE_CD ";
        $query .= "      ,AFT.SENKOU_KIND ";
        $query .= "      ,AFT.STAT_CD ";
        $query .= "      ,AFT.THINKEXAM ";
        $query .= "      ,AFT.JOB_THINK ";
        $query .= "      ,AFT.E017NAME1 ";
        $query .= "      ,AFT.E018NAME1 ";
        $query .= "      ,AFT.SCHOOL_NAME ";
        $query .= "      ,AFT.FACULTYCD ";
        $query .= "      ,AFT.FACULTYNAME ";
        $query .= "      ,AFT.DEPARTMENTCD ";
        $query .= "      ,AFT.DEPARTMENTNAME ";
        $query .= "      ,AFT.CAMPUSADDR1 ";
        $query .= "      ,AFT.CAMPUSFACULTYADDR1 ";
        $query .= "      ,AFT.COMPANY_NAME ";
        $query .= "      ,AFT.COMPANYADDR1 ";
        $query .= "      ,AFT.COMPANYADDR2 ";
        $query .= "      ,AFT.ROW_NUM ";
        $query .= "      ,DET.BASE_REMARK1 ";
        $query .= " FROM SCHREG_REGD_DAT T1 ";
        $query .= " INNER JOIN SCHREGNOS T0 ON T0.SCHREGNO = T1.SCHREGNO ";
        $query .= " INNER JOIN SCHREG_REGD_HDAT T6 ON T6.YEAR = T1.YEAR AND T6.SEMESTER = T1.SEMESTER ";
        $query .= "     AND T6.GRADE = T1.GRADE AND T6.HR_CLASS = T1.HR_CLASS ";
        $query .= " INNER JOIN SCHOOL_MST T10 ON T10.YEAR = T1.YEAR ";
        $query .= " INNER JOIN SCHREG_BASE_MST T2 ON T2.SCHREGNO = T1.SCHREGNO ";
        $query .= " LEFT JOIN SCHREG_REGD_GDAT REGDG ON REGDG.YEAR = T1.YEAR ";
        $query .= "     AND REGDG.GRADE = T1.GRADE ";
        $query .= " LEFT JOIN SCHREG_ENT_GRD_HIST_DAT ENTGRD ON ENTGRD.SCHREGNO = T1.SCHREGNO ";
        $query .= "     AND ENTGRD.SCHOOL_KIND= REGDG.SCHOOL_KIND ";
        $query .= " LEFT JOIN NAME_MST T7 ON T7.NAMECD1='Z002' AND T7.NAMECD2=T2.SEX ";
        $query .= " LEFT JOIN FINSCHOOL_MST FIN_S ON FIN_S.FINSCHOOLCD = ENTGRD.FINSCHOOLCD ";
        $query .= " LEFT JOIN NAME_MST NM_MST ON NM_MST.NAMECD1 = 'L001' AND NM_MST.NAMECD2 = FIN_S.FINSCHOOL_DISTCD ";
        $query .= " LEFT JOIN NAME_MST NML019 ON NML019.NAMECD1 = 'L019' AND NML019.NAMECD2 = FIN_S.FINSCHOOL_TYPE ";
        $query .= " LEFT JOIN COURSE_MST T3 ON T3.COURSECD = T1.COURSECD ";
        $query .= " LEFT JOIN MAJOR_MST T4 ON T4.COURSECD = T1.COURSECD AND T4.MAJORCD = T1.MAJORCD ";
        $query .= " LEFT JOIN V_COURSECODE_MST T5 ON T5.YEAR = T1.YEAR ";
        $query .= "     AND VALUE(T5.COURSECODE,'0000') = VALUE(T1.COURSECODE,'0000')";
        $query .= " LEFT JOIN GUARDIAN_DAT T12 ON T12.SCHREGNO = T2.SCHREGNO ";
        $query .= " LEFT JOIN KIN_GRD_LEDGER_SETUP_DAT T21 ON T21.SCHREGNO = T2.SCHREGNO AND T21.BIRTHDAY_FLG = '1' ";
        $query .= " LEFT JOIN (SELECT SCHREGNO, MAX(ISSUEDATE) AS ISSUEDATE FROM SCHREG_ADDRESS_DAT GROUP BY SCHREGNO) SCHADD0 ON SCHADD0.SCHREGNO = T1.SCHREGNO ";
        $query .= " LEFT JOIN SCHREG_ADDRESS_DAT SCHADD ON SCHADD.SCHREGNO = SCHADD0.SCHREGNO AND  SCHADD.ISSUEDATE = SCHADD0.ISSUEDATE ";
        $query .= " LEFT JOIN STATE STT ON STT.SCHREGNO = T1.SCHREGNO ";
        $query .= " LEFT JOIN AFT_MAIN AFT ON AFT.SCHREGNO = T1.SCHREGNO AND AFT.ROW_NUM = 1 ";
        $query .= " LEFT JOIN SCHREG_BASE_DETAIL_MST DET ON DET.SCHREGNO = T1.SCHREGNO AND DET.BASE_SEQ = '003' ";
        $query .= "  WHERE T1.YEAR= '".CTRL_YEAR."' ";
        $query .= "    AND T1.SEMESTER= '".CTRL_SEMESTER."' ";
        $query .= "  ORDER BY T1.GRADE, T1.HR_CLASS, T1.ATTENDNO ";

        return $query;
    }

}

?>
