<?php

require_once('for_php7.php');
class knjh410Query extends Query {

    //生徒情報取得
    function getStudentData($model)
    {
        $query  = "SELECT ";
        $query .= "    L2.HR_NAME, ";
        $query .= "    T1.ATTENDNO, ";
        $query .= "    T1.ANNUAL, ";
        $query .= "    T1.COURSECD || T1.MAJORCD || '：' || L4.COURSENAME || L4.MAJORABBV AS COURSEMAJOR_NAME, ";
        $query .= "    T1.COURSECODE || '：' || L3.COURSECODENAME AS COURSE_NAME, ";
        $query .= "    T1.SCHREGNO, ";
        $query .= "    L1.NAME, ";
        $query .= "    L1.SEX || '：' || N1.NAME1 AS SEX, ";
        $query .= "    L1.NAME_KANA, ";
        $query .= "    L1.NAME_ENG, ";
        $query .= "    L5.CLUBNAME, ";
        $query .= "    L6.COMMITTEENAME, ";
        $query .= "    L7.NAMESPARE2 ";
        $query .= "FROM ";
        if($model->mode != "grd"){
            $query .= "    SCHREG_REGD_DAT T1 ";
        }else{
            $query .= "    GRD_REGD_DAT T1 ";
        }
        $query .= "    LEFT JOIN SCHREG_BASE_MST L1 ON L1.SCHREGNO = T1.SCHREGNO ";
        $query .= "    LEFT JOIN SCHREG_REGD_HDAT L2 ON L2.YEAR = T1.YEAR ";
        $query .= "         AND L2.SEMESTER = T1.SEMESTER ";
        $query .= "         AND L2.GRADE = T1.GRADE ";
        $query .= "         AND L2.HR_CLASS = T1.HR_CLASS ";
        $query .= "    LEFT JOIN COURSECODE_MST L3 ON L3.COURSECODE = T1.COURSECODE ";
        $query .= "    LEFT JOIN NAME_MST N1 ON N1.NAMECD1 = 'Z002' ";
        $query .= "         AND N1.NAMECD2 = L1.SEX ";
        $query .= "    LEFT JOIN V_COURSE_MAJOR_MST L4 ON L4.YEAR = T1.YEAR ";
        $query .= "         AND L4.COURSECD = T1.COURSECD ";
        $query .= "         AND L4.MAJORCD = T1.MAJORCD ";
        $query .= "    LEFT JOIN (SELECT ";
        $query .= "                   T1.SCHREGNO, ";
        $query .= "                   T1.CLUBCD, ";
        $query .= "                   L1.CLUBNAME ";
        $query .= "               FROM ";
        $query .= "                   SCHREG_CLUB_HIST_DAT T1 ";
        $query .= "                   LEFT JOIN CLUB_MST L1 ON T1.CLUBCD = L1.CLUBCD, ";
        $query .= "                   (SELECT ";
        $query .= "                        SCHREGNO, ";
        $query .= "                        MAX(SDATE) AS SDATE ";
        $query .= "                    FROM ";
        $query .= "                        SCHREG_CLUB_HIST_DAT ";
        $query .= "                    WHERE ";
        $query .= "                        SCHREGNO = '".$model->schregno."' ";
        $query .= "                        AND EDATE IS NULL ";
        $query .= "                    GROUP BY ";
        $query .= "                        SCHREGNO ";
        $query .= "                   ) T2 ";
        $query .= "               WHERE ";
        $query .= "                   T1.SCHREGNO = T2.SCHREGNO ";
        $query .= "                   AND T1.SDATE = T2.SDATE ";
        $query .= "               GROUP BY ";
        $query .= "                   T1.SCHREGNO, ";
        $query .= "                   T1.CLUBCD, ";
        $query .= "                   L1.CLUBNAME ";
        $query .= "              ) L5 ON L5.SCHREGNO = T1.SCHREGNO ";
        $query .= "    LEFT JOIN (SELECT ";
        $query .= "                   T1.SCHREGNO, ";
        $query .= "                   T1.COMMITTEECD, ";
        $query .= "                   L1.COMMITTEENAME ";
        $query .= "               FROM ";
        $query .= "                   SCHREG_COMMITTEE_HIST_DAT T1 ";
        $query .= "                   LEFT JOIN COMMITTEE_MST L1 ON T1.COMMITTEE_FLG = L1.COMMITTEE_FLG AND T1.COMMITTEECD = L1.COMMITTEECD, ";
        $query .= "                   (SELECT ";
        $query .= "                       SCHREGNO, ";
        $query .= "                       MAX(SEQ) AS SEQ ";
        $query .= "                   FROM ";
        $query .= "                       SCHREG_COMMITTEE_HIST_DAT ";
        $query .= "                   WHERE ";
        $query .= "                       YEAR = '".$model->year."' ";
        $query .= "                       AND SCHREGNO = '".$model->schregno."' ";
        $query .= "                   GROUP BY ";
        $query .= "                       SCHREGNO ";
        $query .= "                   ) T2 ";
        $query .= "               WHERE ";
        $query .= "                   YEAR = '".$model->year."' ";
        $query .= "                   AND T1.SCHREGNO = T2.SCHREGNO ";
        $query .= "                   AND T1.SEQ = T2.SEQ ";
        $query .= "               GROUP BY ";
        $query .= "                   T1.SCHREGNO, ";
        $query .= "                   T1.COMMITTEECD, ";
        $query .= "                   L1.COMMITTEENAME ";
        $query .= "              ) L6 ON L6.SCHREGNO = T1.SCHREGNO ";
        $query .= "    LEFT JOIN NAME_MST L7 ON L1.HANDICAP = L7.NAMECD2 and L7.NAMECD1 = 'A025' ";
        $query .= "WHERE ";
        $query .= "    T1.SCHREGNO = '".$model->schregno."' ";
        $query .= "    AND T1.SEMESTER = '".$model->semester."' ";
        $query .= "    AND T1.YEAR = '".$model->year."' ";

        return $query;
    }
    
    //学年種別取得
    function getSchoolKind($model)
    {
        $query  = " SELECT ";
        $query .= "     SCHOOL_KIND ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_GDAT ";
        $query .= " WHERE ";
        $query .= "     (YEAR, GRADE) in (SELECT ";
        $query .= "                             YEAR, ";
        $query .= "                             GRADE ";
        $query .= "                         FROM ";
        $query .= "                             SCHREG_REGD_DAT ";
        $query .= "                         WHERE ";
        $query .= "                             SCHREGNO = '".$model->schregno."' AND ";
        $query .= "                             YEAR = '".$model->year."' AND ";
        $query .= "                             SEMESTER = '".$model->semester."' ";
        $query .= "                         ) ";

        return $query;
    }

    //学籍基礎情報
    function getStudent_data($schregno, $model)
    {

$query .= " SELECT ";
$query .= "     a1.*, ";
$query .= "     a2.NAME1 as SEX_NAME, ";
$query .= "     a3.NAME1 as OTHER, ";
$query .= "     a4.NAME1 as NATION_NAME, ";
$query .= "     a5.NAME1 as ENT_DIV_NAME, ";
$query .= "     a6.NAME1 as GRD_DIV_NAME ";
$query .= " FROM ";
$query .= "     ( ";
        $query .= "  SELECT";
        $query .= "      T1.SCHREGNO,";
        $query .= "      T1.BIRTHDAY,";
        $query .= "      T1.SEX,";
        $query .= "      T1.BLOODTYPE,";
        $query .= "      T1.BLOOD_RH,";
        $query .= "      T1.HANDICAP,";
        $query .= "      T1.NATIONALITY,";
        $query .= "      CASE WHEN T3.SCHREGNO IS NOT NULL ";
        $query .= "           THEN T3.FINSCHOOLCD ";
        $query .= "           ELSE T1.FINSCHOOLCD ";
        $query .= "      END AS FINSCHOOLCD, ";
        $query .= "      CASE WHEN T3.SCHREGNO IS NOT NULL ";
        $query .= "           THEN T3.FINISH_DATE ";
        $query .= "           ELSE T1.FINISH_DATE ";
        $query .= "      END AS FINISH_DATE, ";
        $query .= "      T1.PRISCHOOLCD,";
        $query .= "      CASE WHEN T3.SCHREGNO IS NOT NULL ";
        $query .= "           THEN T3.ENT_DATE ";
        $query .= "           ELSE T1.ENT_DATE ";
        $query .= "      END AS ENT_DATE, ";
        $query .= "      T3.CURRICULUM_YEAR, ";
        $query .= "      CASE WHEN T3.SCHREGNO IS NOT NULL ";
        $query .= "           THEN T3.ENT_DIV ";
        $query .= "           ELSE T1.ENT_DIV ";
        $query .= "      END AS ENT_DIV, ";
        $query .= "      CASE WHEN T3.SCHREGNO IS NOT NULL ";
        $query .= "           THEN T3.ENT_REASON ";
        $query .= "           ELSE T1.ENT_REASON ";
        $query .= "      END AS ENT_REASON, ";
        $query .= "      CASE WHEN T3.SCHREGNO IS NOT NULL ";
        $query .= "           THEN T3.ENT_SCHOOL ";
        $query .= "           ELSE T1.ENT_SCHOOL ";
        $query .= "      END AS ENT_SCHOOL, ";
        $query .= "      CASE WHEN T3.SCHREGNO IS NOT NULL ";
        $query .= "           THEN T3.ENT_ADDR ";
        $query .= "           ELSE T1.ENT_ADDR ";
        $query .= "      END AS ENT_ADDR, ";
        if ($model->Properties["useAddrField2"] == "1" || $model->schoolKind == "H") {
            $query .= "      CASE WHEN T3.SCHREGNO IS NOT NULL ";
            $query .= "           THEN T3.ENT_ADDR2 ";
            $query .= "           ELSE T1.ENT_ADDR2 ";
            $query .= "      END AS ENT_ADDR2, ";
        }
        $query .= "      CASE WHEN T3.SCHREGNO IS NOT NULL ";
        $query .= "           THEN T3.GRD_DATE ";
        $query .= "           ELSE T1.GRD_DATE ";
        $query .= "      END AS GRD_DATE, ";
        $query .= "      CASE WHEN T3.SCHREGNO IS NOT NULL ";
        $query .= "           THEN T3.GRD_DIV ";
        $query .= "           ELSE T1.GRD_DIV ";
        $query .= "      END AS GRD_DIV, ";
        $query .= "      CASE WHEN T3.SCHREGNO IS NOT NULL ";
        $query .= "           THEN T3.GRD_REASON ";
        $query .= "           ELSE T1.GRD_REASON ";
        $query .= "      END AS GRD_REASON, ";
        $query .= "      CASE WHEN T3.SCHREGNO IS NOT NULL ";
        $query .= "           THEN T3.GRD_SCHOOL ";
        $query .= "           ELSE T1.GRD_SCHOOL ";
        $query .= "      END AS GRD_SCHOOL, ";
        $query .= "      CASE WHEN T3.SCHREGNO IS NOT NULL ";
        $query .= "           THEN T3.GRD_ADDR ";
        $query .= "           ELSE T1.GRD_ADDR ";
        $query .= "      END AS GRD_ADDR, ";
        if ($model->Properties["useAddrField2"] == "1" || $model->schoolKind == "H") {
            $query .= "      CASE WHEN T3.SCHREGNO IS NOT NULL ";
            $query .= "           THEN T3.GRD_ADDR2 ";
            $query .= "           ELSE T1.GRD_ADDR2 ";
            $query .= "      END AS GRD_ADDR2, ";
        }
        $query .= "      CASE WHEN T3.SCHREGNO IS NOT NULL ";
        $query .= "           THEN T3.GRD_NO ";
        $query .= "           ELSE T1.GRD_NO ";
        $query .= "      END AS GRD_NO, ";
        $query .= "      T1.REMARK1,";
        $query .= "      T1.REMARK2,";
        $query .= "      T1.REMARK3,";
        $query .= "      T1.TENGAKU_SAKI_ZENJITU,";
        $query .= "      T1.TENGAKU_SAKI_GRADE, ";
        $query .= "      T1.NYUGAKUMAE_SYUSSIN_JOUHOU,";
        $query .= "      T1.EXAMNO, ";
        $query .= "      T2.COURSECODE,";
        $query .= "      T1.UPDATED AS UPDATED1,";
        $query .= "      T2.ATTENDNO,";
        $query .= "      T2.COURSECD ||','|| T2.MAJORCD AS COURSEMAJORCD,";
        $query .= "      T2.UPDATED AS UPDATED2,";
        $query .= "      T2.GRADE,";
        $query .= "      T2.GRADE || ',' || T2.HR_CLASS AS GRCL";
        $query .= "  FROM";
        $query .= "      V_SCHREG_BASE_MST T1 ";
        $query .= "      LEFT JOIN SCHREG_REGD_DAT T2 ON T1.SCHREGNO = T2.SCHREGNO";
        $query .= "      LEFT JOIN SCHREG_ENT_GRD_HIST_DAT T3 ON T1.SCHREGNO = T3.SCHREGNO";
        $query .= "           AND T3.SCHOOL_KIND = '". $model->schoolKind. "' ";
        $query .= "  WHERE";
        $query .= "      T1.SCHREGNO = '". $schregno. "' AND";
        $query .= "      T2.YEAR     = '". $model->year. "' AND";
        $query .= "      T2.SEMESTER = '". $model->semester. "'";
$query .= " ) a1 ";
$query .= " left join NAME_MST a2 on a1.SEX = a2.NAMECD2 and a2.NAMECD1 = 'Z002' ";
$query .= " left join NAME_MST a3 on a1.HANDICAP = a3.NAMECD2 and a3.NAMECD1 = 'A025' ";
$query .= " left join NAME_MST a4 on a1.NATIONALITY = a4.NAMECD2 and a4.NAMECD1 = 'A024' ";
$query .= " left join NAME_MST a5 on a1.ENT_DIV = a5.NAMECD2 and a5.NAMECD1 = 'A002' ";
$query .= " left join NAME_MST a6 on a1.GRD_DIV = a6.NAMECD2 and a6.NAMECD1 = 'A003' ";
        return $query;
    }
    //出身中学校コンボ
    function getFinschoolName($cd)
    {
        $query .= " SELECT ";
        $query .= "     FINSCHOOL_NAME ";
        $query .= " FROM ";
        $query .= "     FINSCHOOL_MST ";
        $query .= " WHERE ";
        $query .= "     FINSCHOOLCD = '{$cd}' ";

        return $query;
    }
    //復学あり
    function getComeBackT()
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     SYSCAT.COLUMNS ";
        $query .= " WHERE ";
        $query .= "     TABNAME = 'SCHREG_ENT_GRD_HIST_COMEBACK_DAT' ";

        return $query;
    }
    //復学日
    function getCB_entDate($model) {
        $query  = " WITH MIN_T AS ( ";
        $query .= " SELECT ";
        $query .= "     SCHREGNO, ";
        $query .= "     MIN(COMEBACK_DATE) AS COMEBACK_DATE ";
        $query .= " FROM ";
        $query .= "     SCHREG_ENT_GRD_HIST_COMEBACK_DAT ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO = '".$model->schregno."' ";
        $query .= "     AND SCHOOL_KIND = '". $model->schoolKind. "' ";
        $query .= " GROUP BY ";
        $query .= "     SCHREGNO ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     ENT_DATE ";
        $query .= " FROM ";
        $query .= "     SCHREG_ENT_GRD_HIST_COMEBACK_DAT T1, ";
        $query .= "     MIN_T ";
        $query .= " WHERE ";
        $query .= "     T1.SCHREGNO = MIN_T.SCHREGNO ";
        $query .= "     AND T1.SCHOOL_KIND = '". $model->schoolKind. "' ";
        $query .= "     AND T1.COMEBACK_DATE = MIN_T.COMEBACK_DATE ";

        return $query;
    }
    //出身塾コンボ
    function getPrischoolName($model, $prinschoolcd)
    {
        $query  = " SELECT prischoolcd, prischool_name ";
        $query .= " FROM v_prischool_mst ";
        $query .= " WHERE year = '".$model->year. "'";
        $query .= " AND prischoolcd = '".$prinschoolcd. "'";
        $query .= " ORDER BY prischoolcd";
        return $query;
    }



    //部活動参照
    function getClub($schregno) {
        $query  = " SELECT DISTINCT ";
        $query .= "     L2.CLUBNAME, ";
        $query .= "     T1.CLUBCD, ";
        $query .= "     L1.DETAIL_DATE, ";
        $query .= "     L1.DOCUMENT, ";
        $query .= "     L1.DETAIL_REMARK ";
        $query .= " FROM ";
        $query .= "     SCHREG_CLUB_HIST_DAT T1 ";
        $query .= " LEFT JOIN ";
        $query .= "     SCHREG_CLUB_HDETAIL_DAT L1 ON  L1.SCHREGNO = T1.SCHREGNO ";
        $query .= "                                AND L1.CLUBCD = T1.CLUBCD ";
        $query .= " LEFT JOIN ";
        $query .= "     CLUB_MST L2 ON L2.CLUBCD = T1.CLUBCD ";
        $query .= " WHERE ";
        $query .= "     T1.SCHREGNO = '".$schregno."' ";
        $query .= " ORDER BY ";
        $query .= "     T1.CLUBCD, ";
        $query .= "     L1.DETAIL_DATE ";

        return $query;
    }

    //委員会活動参照
    function getCommittee($model) {
        $query  = " SELECT ";
        $query .= "     L1.SEQ, ";
        $query .= "     L2.COMMITTEENAME, ";
        $query .= "     T1.CHARGENAME, ";
        $query .= "     L1.DETAIL_DATE, ";
        $query .= "     L1.DETAIL_REMARK ";
        $query .= " FROM ";
        $query .= "     SCHREG_COMMITTEE_HIST_DAT T1 ";
        $query .= " LEFT JOIN ";
        $query .= "     SCHREG_COMMITTEE_HDETAIL_DAT L1 ON  L1.YEAR = T1.YEAR ";
        $query .= "                                     AND L1.SEQ  = T1.SEQ ";
        $query .= "  	AND L1.SCHOOL_KIND = T1.SCHOOL_KIND ";
        $query .= " LEFT JOIN ";
        $query .= "     COMMITTEE_MST L2 ON  L2.COMMITTEE_FLG = T1.COMMITTEE_FLG ";
        $query .= "                      AND L2.COMMITTEECD   = T1.COMMITTEECD ";
        $query .= " WHERE ";
        $query .= "         T1.YEAR     = '".$model->year."' ";
        $query .= "     AND T1.SCHREGNO = '".$model->schregno."' ";
        $query .= "  	AND L2.SCHOOL_KIND = T1.SCHOOL_KIND ";
        $query .= " ORDER BY ";
        $query .= "     T1.COMMITTEECD, ";
        $query .= "     L1.DETAIL_DATE ";
        return $query;
    }

    //資格情報参照
    //学籍資格データよりデータを取得
    function getAward($model, $schregno) {
        $sdate = $model->year . '-04-01';
        $edate = ($model->year + 1) . '-03-31';

        if ($model->Properties["useQualifiedMst"] == '1') {
            $query  = " SELECT ";
            $query .= "     T1.YEAR, ";
            $query .= "     T1.SCHREGNO, ";
            $query .= "     T1.SEQ, ";
            $query .= "     T1.REGDDATE, ";
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "     T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD AS SUBCLASSCD, ";
            } else {
                $query .= "     T1.SUBCLASSCD, ";
            }
            $query .= "     CASE L1.CONDITION_DIV ";
            $query .= "          WHEN '1' ";
            $query .= "          THEN '国家資格' ";
            $query .= "          WHEN '2' ";
            $query .= "          THEN '公的資格' ";
            $query .= "          WHEN '3' ";
            $query .= "          THEN '民間資格' ";
            $query .= "          ELSE 'その他' ";
            $query .= "     END AS CONDITION_DIV, ";
            $query .= "     T1.QUALIFIED_CD, ";
            $query .= "     L1.QUALIFIED_NAME, ";
            $query .= "     L1.PROMOTER, ";
            $query .= "     T1.CONTENTS, ";
            $query .= "     L2.NAME1 AS RANK, ";
            $query .= "     L1.QUALIFIED_NAME || '／' || L2.NAME1 AS HYOUJI_CONTENTS, ";
            $query .= "     T1.REMARK, ";
            $query .= "     T1.CREDITS ";
            $query .= " FROM ";
            $query .= "     SCHREG_QUALIFIED_HOBBY_DAT T1 ";
            $query .= " LEFT JOIN ";
            $query .= "     QUALIFIED_MST L1 ON L1.QUALIFIED_CD = T1.QUALIFIED_CD ";
            $query .= " LEFT JOIN ";
            $query .= "     NAME_MST L2 ON  L2.NAMECD2 = T1.RANK ";
            $query .= "                 AND L2.NAMECD1 = 'H312' ";
            $query .= " WHERE ";
            $query .= "     T1.SCHREGNO = '{$schregno}' AND ";
            $query .= "     T1.REGDDATE BETWEEN '{$sdate}' AND '{$edate}' ";
            $query .= " ORDER BY ";
            $query .= "     T1.REGDDATE, ";
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "     T1.CLASSCD, ";
                $query .= "     T1.SCHOOL_KIND, ";
                $query .= "     T1.CURRICULUM_CD, ";
            }
            $query .= "     T1.SUBCLASSCD, ";
            $query .= "     T1.SEQ ";
        } else {
            $query  = " SELECT ";
            $query .= "     T1.YEAR, ";
            $query .= "     T1.REGDDATE, ";
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "     T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD AS SUBCLASSCD, ";
            } else {
                $query .= "     T1.SUBCLASSCD, ";
            }
            $query .= "     CASE WHEN T1.CONDITION_DIV = '1' ";
            $query .= "          THEN T1.CONDITION_DIV || ':資格' ";
            $query .= "          ELSE T1.CONDITION_DIV || ':その他' ";
            $query .= "          END AS CONDITION_DIV, ";
            $query .= "     T1.SEQ, ";
            $query .= "     T1.CONTENTS AS HYOUJI_CONTENTS, ";
            $query .= "     T1.REMARK, ";
            $query .= "     T1.CREDITS, ";
            $query .= "     T5.CREDITS AS CREDIT ";
            $query .= " FROM ";
            $query .= "     SCHREG_QUALIFIED_HOBBY_DAT T1 ";
            $query .= "       LEFT OUTER JOIN SUBCLASS_MST T2 ON T1.SUBCLASSCD = T2.SUBCLASSCD ";
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "                                  AND T1.CLASSCD         = T2.CLASSCD ";
                $query .= "                                  AND T1.SCHOOL_KIND     = T2.SCHOOL_KIND ";
                $query .= "                                  AND T1.CURRICULUM_CD   = T2.CURRICULUM_CD ";
            }
            $query .= "       LEFT JOIN SCHREG_REGD_DAT T4 ON T4.SCHREGNO = T1.SCHREGNO ";
            $query .= "                                   AND T4.YEAR     = T1.YEAR ";
            $query .= "                                   AND T4.SEMESTER = '".$model->semester."' ";
            $query .= "       LEFT JOIN CREDIT_MST T5 ON T5.YEAR       = T1.YEAR ";
            $query .= "                              AND T5.COURSECD   = T4.COURSECD ";
            $query .= "                              AND T5.MAJORCD    = T4.MAJORCD ";
            $query .= "                              AND T5.GRADE      = T4.GRADE ";
            $query .= "                              AND T5.COURSECODE = T4.COURSECODE ";
            $query .= "                              AND T5.CLASSCD    = SUBSTR(T1.SUBCLASSCD,1,2) ";
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "                          AND T5.SCHOOL_KIND     = T1.SCHOOL_KIND ";
                $query .= "                          AND T5.CURRICULUM_CD   = T1.CURRICULUM_CD ";
            }
            $query .= "                              AND T5.SUBCLASSCD = T1.SUBCLASSCD ";
            $query .= " WHERE ";
            $query .= "     T1.SCHREGNO = '{$schregno}' AND ";
            $query .= "     T1.REGDDATE BETWEEN '{$sdate}' AND '{$edate}' ";
            $query .= " ORDER BY ";
            $query .= "     T1.REGDDATE, ";
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "     T1.CLASSCD, ";
                $query .= "     T1.SCHOOL_KIND, ";
                $query .= "     T1.CURRICULUM_CD, ";
            }
            $query .= "     T1.SUBCLASSCD, ";
            $query .= "     T1.SEQ ";
        }

        return $query;
    }

    //指導情報
    //学籍賞罰データよりデータを取得
    function selectQuery($model)
    {
        $query = " SELECT ";
        $query .= "   T1.TRAINDATE, ";
        $query .= "   T1.SCHREGNO, ";
        $query .= "   T1.PATIENTCD, ";
        $query .= "   T1.PATIENTCD || ':' || MEISYOU_GET(T1.PATIENTCD,'H301',1) AS PATIENT, ";
        $query .= "   T1.STAFFCD, ";
        $query .= "   T1.STAFFCD || ':' || T2.STAFFNAME_SHOW AS STAFFNAME_SHOW, ";
        $query .= "   T1.HOWTOTRAINCD, ";
        $query .= "   T1.HOWTOTRAINCD || ':' || MEISYOU_GET(T1.HOWTOTRAINCD,'H302',1) AS HOWTOTRAIN, ";
        $query .= "   T1.CONTENT ";
        $query .= " FROM ";
        $query .= "   SCHREG_TRAINHIST_DAT T1 LEFT OUTER JOIN V_STAFF_MST T2  ";
        $query .= "   ON T1.YEAR = T2.YEAR AND T1.STAFFCD = T2.STAFFCD ";
        $query .= " WHERE ";
        $query .= "   T1.YEAR = '".$model->year ."' AND ";
        $query .= "   T1.SCHREGNO = '". $model->schregno ."' ";
        $query .= " ORDER BY ";
        $query .= "   T1.TRAINDATE ";

        return $query;
    }

    //賞罰情報
    //学籍賞罰データよりデータを取得
    function getDetail($model)
    {

            $query  ="SELECT ";
            $query .="    T1.DETAIL_SDATE, ";
            $query .="    T1.DETAIL_DIV, ";
            $query .="    case when T1.DETAIL_DIV = '1' then T1.DETAIL_DIV || '：' || '賞データ' ";	
            $query .="    when T1.DETAIL_DIV = '2' then T1.DETAIL_DIV || '：' || '罰データ' ";
            $query .="    else T1.DETAIL_DIV || '：' || '自転車データ' end as DETAIL_DATA, ";
            $query .="    T1.DETAILCD, ";
            $query .="    T1.content, ";
            $query .="    T1.remark, ";
            $query .="    case when T1.DETAIL_DIV = '1' then T1.DETAILCD || '：' || meisyou_get(T1.DETAILCD,'H303',1) ";
            $query .="    when T1.DETAIL_DIV = '2' then T1.DETAILCD || '：' || meisyou_get(T1.DETAILCD,'H304',1) else '' end as DETAILCD ";
            $query .="FROM  SCHREG_DETAILHIST_DAT T1 ";
            $query .="WHERE ";
            $query .="    T1.year     = '".$model->year."'";
            $query .="    AND T1.DETAIL_DIV < '3' ";
            $query .="    AND T1.schregno = '".$model->schregno."' ";
            $query .="ORDER BY T1.DETAIL_SDATE";

            return $query;
    }
    
    //出欠情報
    //欠席名称取得
    function getAttendName()
    {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = 'C001' ";
        $query .= " AND ";
        $query .= "     NAMECD2 in ('4','5','6') ";
        $query .= " ORDER BY ";
        $query .= "     NAMECD2 ";
        
        return $query;
    }
    
    //出席情報取得
    function getAttend($Properties, $schregno)
    {
        $query  = " SELECT ";
        $query .= "     t1.*, ";
        $query .= "     t2.SEMESTERNAME, ";
        $query .= "     t3.SEM_OFFDAYS, ";
        $query .= "     t4.NAMECD2 as SICK_FLG, ";
        $query .= "     t5.NAMECD2 as NOTICE_FLG, ";
        $query .= "     t7.GRADE_NAME ";
        $query .= " FROM ";
        $query .= "     (SELECT ";
        $query .= "         YEAR, ";
        $query .= "         SEMESTER, ";
        $query .= "         SCHREGNO, ";
        $query .= "         SUM(LESSON) as LESSON, ";
        $query .= "         SUM(OFFDAYS) as OFFDAYS, ";
        $query .= "         SUM(SUSPEND) as SUSPEND, ";
        $query .= "         SUM(MOURNING) as MOURNING, ";
        $query .= "         SUM(ABROAD) as ABROAD, ";
        $query .= "         SUM(SICK) as SICK, ";
        $query .= "         SUM(NOTICE) as NOTICE, ";
        $query .= "         SUM(NONOTICE) as NONOTICE ";
        if($Properties["useVirus"] == 'true'){
            $query .= "         ,SUM(VIRUS) as VIRUS ";
        }
        if($Properties["useKoudome"] == 'true'){
            $query .= "         ,SUM(KOUDOME) as KOUDOME ";
        }
        $query .= "     FROM ";
        $query .= "         ATTEND_SEMES_DAT ";
        $query .= "     WHERE ";
        $query .= "         COPYCD = '0' AND ";
        $query .= "         SCHREGNO = '".$schregno."' ";
        $query .= "     GROUP BY ";
        $query .= "         YEAR, ";
        $query .= "         SEMESTER, ";
        $query .= "         SCHREGNO ";
        $query .= "     UNION  ";
        $query .= "     SELECT ";
        $query .= "         YEAR, ";
        $query .= "         '9', ";
        $query .= "         SCHREGNO, ";
        $query .= "         SUM(LESSON) as LESSON, ";
        $query .= "         SUM(OFFDAYS) as OFFDAYS, ";
        $query .= "         SUM(SUSPEND) as SUSPEND, ";
        $query .= "         SUM(MOURNING) as MOURNING, ";
        $query .= "         SUM(ABROAD) as ABROAD, ";
        $query .= "         SUM(SICK) as SICK, ";
        $query .= "         SUM(NOTICE) as NOTICE, ";
        $query .= "         SUM(NONOTICE) as NONOTICE ";
        if($Properties["useVirus"] == 'true'){
            $query .= "         ,SUM(VIRUS) as VIRUS ";
        }
        if($Properties["useKoudome"] == 'true'){
            $query .= "         ,SUM(KOUDOME) as KOUDOME ";
        }
        $query .= "     FROM ";
        $query .= "         ATTEND_SEMES_DAT ";
        $query .= "     WHERE ";
        $query .= "         COPYCD = '0' AND ";
        $query .= "         SCHREGNO = '".$schregno."' ";
        $query .= "     GROUP BY ";
        $query .= "         YEAR, ";
        $query .= "         SCHREGNO ";
        $query .= "     ) t1  ";
        $query .= "     left join SEMESTER_MST t2 on t1.YEAR = t2.YEAR and t1.SEMESTER = t2.SEMESTER ";
        $query .= "     left join SCHOOL_MST t3 on t1.YEAR = t3.YEAR ";
        $query .= "     left join NAME_YDAT t4 on t1.YEAR = t4.YEAR and t4.NAMECD1 = 'C001' and t4.NAMECD2 = '4' ";
        $query .= "     left join NAME_YDAT t5 on t1.YEAR = t5.YEAR and t5.NAMECD1 = 'C001' and t5.NAMECD2 = '5' ";
        $query .= "     left join SCHREG_REGD_DAT t6 on t1.SCHREGNO = t6.SCHREGNO and t1.YEAR = t6.YEAR and t1.SEMESTER = t6.SEMESTER ";
        $query .= "     left join SCHREG_REGD_HDAT t7 on t1.YEAR = t7.YEAR and t1.SEMESTER = t7.SEMESTER and t6.GRADE = t7.GRADE and t6.HR_CLASS = t7.HR_CLASS ";
        $query .= " ORDER BY ";
        $query .= "     YEAR DESC, ";
        $query .= "     SEMESTER ";
        
        return $query;
    }
    
    //通学情報
    function getTuugakuData($schregno)
    {
        $query  = " SELECT ";
        $query .= "     t1.*, ";
        $query .= "     t2.GO_HOME_GROUP_NAME, ";
        $query .= "     t3.NAME1 as RESPONSIBLE, ";
        $query .= "     t4.NAME1 as COMMUTE ";
        $query .= " FROM ";
        $query .= "     SCHREG_ENVIR_DAT t1 ";
        $query .= "     left join GO_HOME_GROUP_MST t2 on t1.GO_HOME_GROUP_NO = t2.GO_HOME_GROUP_NO ";
        $query .= "     left join NAME_MST t3 on t1.RESPONSIBILITY = t3.NAMECD2 and t3.NAMECD1 = 'H109' ";
        $query .= "     left join NAME_MST t4 on t1.HOWTOCOMMUTECD = t4.NAMECD2 and t4.NAMECD1 = 'H100' ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO = '".$schregno."' ";
        
        return $query;
    }
    //駅と路線名取得
    function getStationName($rosencd, $stationcd)
    {
        $query  = " SELECT ";
        $query .= "     LINE_CD, ";
        $query .= "     STATION_CD, ";
        $query .= "     LINE_NAME, ";
        $query .= "     STATION_NAME ";
        $query .= " FROM ";
        $query .= "     STATION_NETMST ";
        $query .= " WHERE ";
        $query .= "     LINE_CD = '".$rosencd."' AND ";
        $query .= "     STATION_CD = '".$stationcd."' ";
        
        return $query;
    }
    
    //生徒情報
    function getStudentAddr($schregno)
    {
        $query  = " SELECT ";
        $query .= "     t1.SCHREGNO, ";
        $query .= "     t1.ISSUEDATE, ";
        $query .= "     t1.EXPIREDATE, ";
        $query .= "     t1.ZIPCD, ";
        $query .= "     t1.ADDR1, ";
        $query .= "     t1.ADDR2, ";
        $query .= "     t1.TELNO, ";
        $query .= "     t2.EMERGENCYCALL, ";
        $query .= "     t2.EMERGENCYNAME, ";
        $query .= "     t2.EMERGENCYTELNO ";
        $query .= " FROM ";
        $query .= "     SCHREG_ADDRESS_DAT t1 ";
        $query .= "     left join SCHREG_BASE_MST t2 on t1.SCHREGNO = t2.SCHREGNO ";
        $query .= " WHERE ";
        $query .= "     t1.SCHREGNO = '".$schregno."' ";
        $query .= " ORDER BY ";
        $query .= "     ISSUEDATE DESC ";
        
        return $query;
    }
    
    //保護者・保証人情報
    function getParentAddr($schregno, $table, $field)
    {
        $query  = " SELECT ";
        $query .= "     t1.SCHREGNO, ";
        $query .= "     t1.ISSUEDATE, ";
        $query .= "     t1.EXPIREDATE, ";
        $query .= "     t1.".$field."_ZIPCD, ";
        $query .= "     t1.".$field."_ADDR1, ";
        $query .= "     t1.".$field."_ADDR2, ";
        $query .= "     t1.".$field."_TELNO, ";
        $query .= "     case when t2.".$field."_NAME IS NOT NULL then t2.".$field."_NAME ";
        $query .= "          else t3.".$field."_NAME end as ".$field."_NAME ";
        $query .= " FROM ";
        $query .= "     ".$table."_ADDRESS_DAT t1 ";
        $query .= "     left join ".$table."_HIST_DAT t2 on t1.SCHREGNO = t2.SCHREGNO and t1.ISSUEDATE = t2.ISSUEDATE and t1.EXPIREDATE = t2.EXPIREDATE ";
        if($table != "GUARDIAN2"){
            $query .= "     left join GUARDIAN_DAT t3 on t1.SCHREGNO = t3.SCHREGNO ";
        }else{
            $query .= "     left join GUARDIAN2_DAT t3 on t1.SCHREGNO = t3.SCHREGNO ";
        }
        $query .= " WHERE ";
        $query .= "     t1.SCHREGNO = '".$schregno."' ";
        $query .= " ORDER BY ";
        $query .= "     t1.ISSUEDATE DESC ";
        
        return $query;
    }
    
    //その他情報
    function getOtherAddr($schregno)
    {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     SCHREG_SEND_ADDRESS_DAT ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO = '".$schregno."' ";
        $query .= " ORDER BY ";
        $query .= "     DIV ";
        
        return $query;
    }
    
    //家族情報
    function getFamilyData($schregno)
    {
        $query  = " SELECT ";
        $query .= "     t1.*, ";
        $query .= "     t2.NAME1 as SEX, ";
        $query .= "     t3.NAME1 as RELATION ";
        $query .= " FROM ";
        $query .= "     SCHREG_RELA_DAT t1 ";
        $query .= "     left join NAME_MST t2 on t1.RELASEX = t2.NAMECD2 and t2.NAMECD1 = 'Z002' ";
        $query .= "     left join NAME_MST t3 on t1.RELATIONSHIP = t3.NAMECD2 and t3.NAMECD1 = 'H201' ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO = '".$schregno."' ";
        $query .= " ORDER BY ";
        $query .= "     RELANO ";
        
        return $query;
    }

    //年度情報
    function getSchregAllData($schregno)
    {
        $query .= "   SELECT * FROM ";
        $query .= "   ";
        $query .= "   ( ";
        $query .= "  SELECT ";
        $query .= "    T1.SCHREGNO ";
        $query .= "    , T1.YEAR ";
        $query .= "    , T1.SEMESTER ";
        $query .= "    , T1.GRADE ";
        $query .= "    , T10.SCHOOL_KIND ";
        $query .= "    , T1.HR_CLASS ";
        $query .= "    , T1.ATTENDNO ";
        $query .= "    , T2.GRADE_NAME ";
        $query .= "    , T2.HR_CLASS_NAME1 ";
        $query .= "    , T2.TR_CD1 ";
        $query .= "    , T3.STAFFNAME_SHOW ";
        $query .= "    , T2.TR_CD2 ";
        $query .= "    , T9.STAFFNAME_SHOW AS STAFFNAME2 ";
        $query .= "    , T4.SEMESTERNAME ";
        $query .= "    , T4.SDATE ";
        $query .= "    , T4.EDATE ";
        $query .= "    , T5.CLUBCD AS CD ";
        $query .= "    , T6.CLUBNAME AS NAME ";
        $query .= "    , '1' AS CHKFLG ";
        $query .= "   ";
        $query .= "  FROM ";
        $query .= "    SCHREG_REGD_DAT T1 ";
        $query .= "    LEFT JOIN SCHREG_REGD_HDAT T2 ";
        $query .= "      ON T1.YEAR = T2.YEAR ";
        $query .= "      AND T1.SEMESTER = T2.SEMESTER ";
        $query .= "      AND T1.GRADE = T2.GRADE ";
        $query .= "      AND T1.HR_CLASS = T2.HR_CLASS ";
        $query .= "    LEFT JOIN SCHREG_REGD_GDAT T10 ";
        $query .= "      ON T1.YEAR = T10.YEAR ";
        $query .= "      AND T1.GRADE = T10.GRADE ";
        $query .= "    LEFT JOIN STAFF_MST T3 ";
        $query .= "      ON T2.TR_CD1 = T3.STAFFCD ";
        $query .= "    LEFT JOIN STAFF_MST T9 ";
        $query .= "      ON T2.TR_CD2 = T9.STAFFCD ";
        $query .= "    LEFT JOIN SEMESTER_MST T4 ";
        $query .= "      ON T1.YEAR = T4.YEAR ";
        $query .= "      AND T1.SEMESTER = T4.SEMESTER ";
        $query .= "    LEFT JOIN ( ";
        $query .= "      SELECT ";
        $query .= "        SCHOOL_KIND ";
        $query .= "        , SCHREGNO ";
        $query .= "        , CLUBCD ";
        $query .= "        , MIN(SDATE) AS SDATE ";
        $query .= "        , MAX(EDATE) AS EDATE ";
        $query .= "      FROM ";
        $query .= "        SCHREG_CLUB_HIST_DAT ";
        $query .= "      GROUP BY ";
        $query .= "        SCHOOL_KIND ";
        $query .= "        , SCHREGNO ";
        $query .= "        , CLUBCD ";
        $query .= "    ) T5 ";
        $query .= "      ON T10.SCHOOL_KIND = T5.SCHOOL_KIND ";
        $query .= "      AND T1.SCHREGNO = T5.SCHREGNO ";
        $query .= "      AND T5.SDATE < T4.EDATE ";
        $query .= "      AND (T5.EDATE IS NULL OR T5.EDATE > T4.SDATE) ";
        $query .= "    LEFT JOIN CLUB_MST T6 ";
        $query .= "      ON T10.SCHOOL_KIND = T6.SCHOOL_KIND ";
        $query .= "      AND T5.CLUBCD = T6.CLUBCD ";
        $query .= "  WHERE ";
        $query .= "    T1.SCHREGNO = '".$schregno."' ";
        $query .= "  ORDER BY ";
        $query .= "    T1.YEAR DESC ";
        $query .= "    , T1.SEMESTER DESC ";
        $query .= "    , T5.SDATE ";
        $query .= "  ) ";
        $query .= "  UNION ";
        $query .= "  ( ";
        $query .= "  SELECT ";
        $query .= "    T1.SCHREGNO ";
        $query .= "    , T1.YEAR ";
        $query .= "    , T1.SEMESTER ";
        $query .= "    , T1.GRADE ";
        $query .= "    , T10.SCHOOL_KIND ";
        $query .= "    , T1.HR_CLASS ";
        $query .= "    , T1.ATTENDNO ";
        $query .= "    , T2.GRADE_NAME ";
        $query .= "    , T2.HR_CLASS_NAME1 ";
        $query .= "    , T2.TR_CD1 ";
        $query .= "    , T3.STAFFNAME_SHOW ";
        $query .= "    , T2.TR_CD2 ";
        $query .= "    , T9.STAFFNAME_SHOW AS STAFFNAME2 ";
        $query .= "    , T4.SEMESTERNAME ";
        $query .= "    , T4.SDATE ";
        $query .= "    , T4.EDATE ";
        $query .= "    , T7.COMMITTEECD AS CD ";
        $query .= "    , T8.COMMITTEENAME AS NAME ";
        $query .= "    , '2' AS CHKFLG ";
        $query .= "  FROM ";
        $query .= "    SCHREG_REGD_DAT T1 ";
        $query .= "    LEFT JOIN SCHREG_REGD_HDAT T2 ";
        $query .= "      ON T1.YEAR = T2.YEAR ";
        $query .= "      AND T1.SEMESTER = T2.SEMESTER ";
        $query .= "      AND T1.GRADE = T2.GRADE ";
        $query .= "      AND T1.HR_CLASS = T2.HR_CLASS ";
        $query .= "    LEFT JOIN SCHREG_REGD_GDAT T10 ";
        $query .= "      ON T1.YEAR = T10.YEAR ";
        $query .= "      AND T1.GRADE = T10.GRADE ";
        $query .= "    LEFT JOIN STAFF_MST T3 ";
        $query .= "      ON T2.TR_CD1 = T3.STAFFCD ";
        $query .= "    LEFT JOIN STAFF_MST T9 ";
        $query .= "      ON T2.TR_CD2 = T9.STAFFCD ";
        $query .= "    LEFT JOIN SEMESTER_MST T4 ";
        $query .= "      ON T1.YEAR = T4.YEAR ";
        $query .= "      AND T1.SEMESTER = T4.SEMESTER ";
        $query .= "    LEFT JOIN SCHREG_COMMITTEE_HIST_DAT T7 ";
        $query .= "      ON T1.YEAR = T7.YEAR ";
        $query .= "      AND T1.SEMESTER = T7.SEMESTER ";
        $query .= "      AND T1.SCHREGNO = T7.SCHREGNO ";
        $query .= "    LEFT JOIN COMMITTEE_MST T8 ";
        $query .= "      ON T10.SCHOOL_KIND = T8.SCHOOL_KIND ";
        $query .= "      AND T7.COMMITTEECD = T8.COMMITTEECD ";
        $query .= "    LEFT JOIN SCHREG_COMMITTEE_HDETAIL_DAT SCH_COM_HDETAIL ";
        $query .= "      ON SCH_COM_HDETAIL.SCHOOLCD = T7.SCHOOLCD ";
        $query .= "      AND SCH_COM_HDETAIL.SCHOOL_KIND = T7.SCHOOL_KIND ";
        $query .= "      AND SCH_COM_HDETAIL.YEAR = T7.YEAR ";
        $query .= "      AND SCH_COM_HDETAIL.SEQ = T7.SEQ ";
        $query .= "  WHERE ";
        $query .= "    T1.SCHREGNO = '".$schregno."' ";
        $query .= "  ORDER BY ";
        $query .= "    T1.YEAR DESC ";
        $query .= "    , T1.SEMESTER DESC ";
        $query .= "    , SCH_COM_HDETAIL.DETAIL_DATE ";
        $query .= "  ) ";
        $query .= "   ";
        $query .= "  ORDER BY ";
        $query .= "  YEAR DESC ";
        $query .= "  ,SEMESTER DESC ";
        return $query;
    }



    //行動の記録取得
    function getActionDuc($model, $order)
    {
        $query  = "SELECT ";
        $query .= "    T1.STAFFCD || '　' || L1.STAFFNAME AS STAFFNAME, ";
        $query .= "    T1.ACTIONDATE, ";
        $query .= "    T1.ACTIONTIME, ";
        $query .= "    T1.STAFFCD, ";
        $query .= "    L2.NAME1 AS DIVIDENAME, ";
        $query .= "    T1.TITLE, ";
        $query .= "    T1.SCHREGNO, ";
        $query .= "    T1.ACTIONDATE, ";
        $query .= "    T1.SEQ, ";
        $query .= "    T1.PRIVATE ";
        $query .= "FROM ";
        $query .= "    ACTION_DOCUMENT_DAT T1 ";
        $query .= "    LEFT JOIN V_STAFF_MST L1 ON L1.YEAR = '".$model->year."' ";
        $query .= "         AND L1.STAFFCD = T1.STAFFCD ";
        $query .= "    LEFT JOIN V_NAME_MST L2 ON L2.YEAR = '".$model->year."' ";
        $query .= "         AND L2.NAMECD1 = 'H307' ";
        $query .= "         AND L2.NAMECD2 = T1.DIVIDECD ";
        $query .= "WHERE ";
        $query .= "    SCHREGNO = '".$model->schregno."' ";
        if (AUTHORITY == DEF_REFER_RESTRICT || AUTHORITY == DEF_UPDATE_RESTRICT) {
            $query .= "    AND T1.STAFFCD = '".STAFFCD."' ";
        }
        if ($model->narrowing) {
            $query .= " AND T1.DIVIDECD = '".$model->narrowing."' ";
        }
        $query .= "ORDER BY ";
        $query .= "    ".$order." ";
        $query .= "    T1.SEQ ";

        return $query;
    }

    //権限(受講講座)
    function getAuthChair($model)
    {
        $query  = "SELECT ";
        $query .= "    CASE WHEN COUNT(*) > 0 THEN 'ON' ELSE 'OFF' END AS CHAIRFLG ";
        $query .= "FROM ";
        $query .= "    CHAIR_STD_DAT T1 ";
        $query .= "WHERE ";
        $query .= "    T1.YEAR = '".$model->year."' ";
        $query .= "    AND T1.SEMESTER = '".$model->semester."' ";
        $query .= "    AND T1.CHAIRCD IN (SELECT ";
        $query .= "                           T2.CHAIRCD ";
        $query .= "                       FROM ";
        $query .= "                           CHAIR_STF_DAT T2 ";
        $query .= "                       WHERE ";
        $query .= "                           T2.YEAR = '".$model->year."' ";
        $query .= "                           AND T2.SEMESTER = '".$model->semester."' ";
        $query .= "                           AND STAFFCD = '".STAFFCD."' ";
        $query .= "                       ) ";
        $query .= "    AND T1.SCHREGNO = '".$model->schregno."' ";

        return $query;
    }

    //権限(クラブ)
    function getAuthClub($model)
    {
        $query  = "SELECT ";
        $query .= "    CASE WHEN COUNT(*) > 0 THEN 'ON' ELSE 'OFF' END AS CLUBFLG ";
        $query .= "FROM ";
        $query .= "    SCHREG_CLUB_HIST_DAT T1 ";
        $query .= "WHERE ";
        $query .= "    T1.CLUBCD IN (SELECT ";
        $query .= "                      CLUBCD ";
        $query .= "                  FROM ";
        $query .= "                      CLUB_YDAT ";
        $query .= "                  WHERE ";
        $query .= "                      YEAR = '".$model->year."' ";
        $query .= "                 ) ";
        $query .= "    AND T1.CLUBCD IN (SELECT ";
        $query .= "                          CLUBCD ";
        $query .= "                      FROM ";
        $query .= "                          CLUB_ADVISER_DAT ";
        $query .= "                      WHERE ";
        $query .= "                          YEAR = '".$model->year."' ";
        $query .= "                          AND ADVISER = '".STAFFCD."' ";
        $query .= "                     ) ";
        $query .= "    AND T1.SCHREGNO = '".$model->schregno."' ";

        return $query;
    }

    //権限(担任)
    function getAuthHrclass($model)
    {
        $query  = "SELECT ";
        $query .= "    CASE WHEN COUNT(*) > 0 THEN 'ON' ELSE 'OFF' END AS HRCLASSFLG ";
        $query .= "FROM ";
        $query .= "    SCHREG_REGD_DAT T1 ";
        $query .= "WHERE ";
        $query .= "    T1.SCHREGNO = '".$model->schregno."' ";
        $query .= "    AND YEAR = '".$model->year."' ";
        $query .= "    AND SEMESTER = '".$model->semester."' ";
        $query .= "    AND VALUE(T1.GRADE, '00') || VALUE(T1.HR_CLASS, '000') ";
        $query .= "        IN (SELECT ";
        $query .= "                VALUE(T2.GRADE, '00') || VALUE(T2.HR_CLASS, '000') ";
        $query .= "            FROM ";
        $query .= "                SCHREG_REGD_HDAT T2 ";
        $query .= "            WHERE ";
        $query .= "                T2.YEAR = '".$model->year."' ";
        $query .= "                AND T2.SEMESTER = '".$model->semester."' ";
        $query .= "                AND (T2.TR_CD1 = '".STAFFCD."' ";
        $query .= "                     OR T2.TR_CD2 = '".STAFFCD."' ";
        $query .= "                     OR T2.TR_CD3 = '".STAFFCD."' ";
        $query .= "                     OR T2.SUBTR_CD1 = '".STAFFCD."' ";
        $query .= "                     OR T2.SUBTR_CD2 = '".STAFFCD."' ";
        $query .= "                     OR T2.SUBTR_CD3 = '".STAFFCD."')) ";

        return $query;
    }

    //権限(進路担当)
    function getAuthCourse($model)
    {
        $query .= "SELECT ";
        $query .= "    CASE WHEN COUNT(*) > 0 THEN 'ON' ELSE 'OFF' END AS COURSEFLG ";
        $query .= "FROM ";
        $query .= "    USERGROUP_DAT ";
        $query .= "WHERE ";
        $query .= "    YEAR = '".$model->year."' ";
        $query .= "    AND GROUPCD = '0009' ";
        $query .= "    AND STAFFCD = '".STAFFCD."' ";

        return $query;
    }

    //行動の記録取得
    function getActionDucSub($model)
    {
        $query  = "SELECT ";
        $query .= "    T1.STAFFCD || '　' || L1.STAFFNAME AS STAFFNAME, ";
        $query .= "    T1.ACTIONDATE, ";
        $query .= "    T1.ACTIONTIME, ";
        $query .= "    T1.DIVIDECD, ";
        $query .= "    T1.STAFFCD, ";
        $query .= "    T1.TITLE, ";
        $query .= "    T1.TEXT, ";
        $query .= "    T1.SCHREGNO, ";
        $query .= "    T1.ACTIONDATE, ";
        $query .= "    T1.SEQ ";
        $query .= "FROM ";
        $query .= "    ACTION_DOCUMENT_DAT T1 ";
        $query .= "    LEFT JOIN V_STAFF_MST L1 ON L1.YEAR = '".$model->year."' ";
        $query .= "         AND L1.STAFFCD = T1.STAFFCD ";
        $query .= "WHERE ";
        $query .= "    SCHREGNO = '".$model->schregno."' ";
        $query .= "    AND ACTIONDATE = '".str_replace("/", "-", $model->actiondate)."' ";
        $query .= "    AND SEQ = ".$model->seq." ";
        $query .= "ORDER BY ";
        $query .= "    T1.ACTIONDATE DESC, ";
        $query .= "    T1.ACTIONTIME DESC, ";
        $query .= "    T1.SEQ ";

        return $query;
    }

    //生徒情報取得
    function getName($schregno)
    {
        $query  = "SELECT ";
        $query .= "    NAME ";
        $query .= "FROM ";
        $query .= "    SCHREG_BASE_MST ";
        $query .= "WHERE ";
        $query .= "    SCHREGNO = '".$schregno."' ";

        return $query;
    }

    //名称マスタ取得
    function getNameMst($model, $namecd1, $namecd2 = "")
    {
        $query  = "SELECT ";
        $query .= "    NAME1 AS LABEL, ";
        $query .= "    NAMECD2 AS VALUE ";
        $query .= "FROM ";
        $query .= "    V_NAME_MST ";
        $query .= "WHERE ";
        $query .= "    YEAR = '".$model->year."' ";
        $query .= "    AND NAMECD1 = '".$namecd1."' ";
        if ($namecd2) {
            $query .= "    AND NAMECD2 = '".$namecd2."' ";
        }
        $query .= "ORDER BY ";
        $query .= "    NAMECD2 ";

        return $query;
    }

    /**
     * 回数取得
     */
    function getMaxSeq($schregno)
    {
        $query  = "SELECT ";
        $query .= "    SCHREGNO, ";
        $query .= "    MAX(SEQ) + 1 AS SEQ ";
        $query .= "FROM ";
        $query .= "    ACTION_DOCUMENT_DAT ";
        $query .= "WHERE ";
        $query .= "    SCHREGNO = '".$schregno."' ";
        $query .= "GROUP BY ";
        $query .= "    SCHREGNO ";

        return $query;
    }

    /**
     * 更新
     */
    function &updateQuery($model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $data["SCHREGNO"][TEXT]               = $model->schregno;
        $data["ACTIONDATE"][TEXT]             = str_replace("/", "-", $model->actiondate);
        $data["SEQ"][NUMBER]                  = $model->seq;
        $data["ACTIONTIME"][TEXT]             = $model->actiontime;
        $data["DIVIDECD"][TEXT]               = $model->dividecd;
        $data["TITLE"][TEXT]                  = $model->title;
        $data["TEXT"][TEXT]                   = $model->text;
        $data["REGISTERCD"][TEXT]             = STAFFCD;
        $data["UPDATED"][FUNC]                = "sysdate()";

        $where  = " WHERE SCHREGNO = '".$model->schregno."'";
        $where .= "       AND ACTIONDATE = '".str_replace("/", "-", $model->actiondateUpd)."'";
        $where .= "       AND SEQ = ".$model->seq." ";

        $query = Query::updateSQL($data, "ACTION_DOCUMENT_DAT", $where);
        $db->query($query);
        
        $db->commit();
        Query::dbCheckIn($db);

        return true;
    }

    /**
     * 新規
     */
    function &insertQuery($model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $data["SCHREGNO"][TEXT]               = $model->schregno;
        $data["ACTIONDATE"][TEXT]             = str_replace("/", "-", $model->actiondate);
        $data["SEQ"][NUMBER]                  = $model->seq;
        $data["ACTIONTIME"][TEXT]             = $model->actiontime;
        $data["STAFFCD"][TEXT]                = STAFFCD;
        $data["DIVIDECD"][TEXT]               = $model->dividecd;
        $data["TITLE"][TEXT]                  = $model->title;
        $data["TEXT"][TEXT]                   = $model->text;
        $data["REGISTERCD"][TEXT]             = STAFFCD;
        $data["UPDATED"][FUNC]                = "sysdate()";

        $query = Query::insertSQL($data, "ACTION_DOCUMENT_DAT");
        $db->query($query);
        
        $db->commit();
        Query::dbCheckIn($db);

        return true;
    }

    /**
     * 削除
     */
    function &getDeleteQuery($delvalue)
    {

        $query  = "DELETE FROM ";
        $query .= "    ACTION_DOCUMENT_DAT ";
        $query .= "WHERE ";
        $query .= "    SCHREGNO = '".$delvalue[0]."' ";
        $query .= "    AND ACTIONDATE = '".str_replace("/", "-", $delvalue[1])."' ";
        $query .= "    AND SEQ = ".$delvalue[2]." ";

        return $query;
    }
}
?>
