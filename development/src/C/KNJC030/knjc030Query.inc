<?php

require_once('for_php7.php');

class knjc030Query extends Query {

    //校時名称の取得
    function getNamecd($year,$cd)
    {
        return " SELECT namecd2,name1 FROM v_name_mst WHERE year = '".$year."' AND namecd1 = '".$cd."' ";
    }

    //クラスの基本データを取得
    function get_class_data($model,$sem)
    {
        $query  = "";
        $query .= "  SELECT DISTINCT";
        $query .= "      GRADE || HR_CLASS AS GR_CL,";
        $query .= "      GRADE,";
        $query .= "      HR_CLASS ,";
        $query .= "      HR_NAME";
        $query .= "  FROM";
        $query .= "      SCHREG_REGD_HDAT";
        $query .= "  WHERE";
        $query .= "      YEAR ='".$model->cntl_dt_year."' AND";
        $query .= "      SEMESTER ='".$sem."'";
        $query .= "  ORDER BY";
        $query .= "      GRADE,";
        $query .= "      HR_CLASS";
        return $query;
    }


    //スタッフの基本データを取得
    function get_staff_data($cntl_dt_year)
    {

        $query  = "";
        $query .= "  SELECT";
        $query .= "      T1.STAFFCD,";
        $query .= "      T1.STAFFNAME_SHOW as SHOW_NAME,";
        $query .= "      T1.SECTIONCD,";
        $query .= "      T2.SECTIONNAME";
        $query .= "  FROM";
        $query .= "      V_STAFF_MST T1 LEFT JOIN SECTION_MST T2 ON T2.SECTIONCD = T1.SECTIONCD";
        $query .= "  WHERE";
        $query .= "      T1.YEAR            =   '".$cntl_dt_year."' AND";
        $query .= "      T1.CHARGECLASSCD   =   '1'";
        $query .= "  ORDER BY";
        $query .= "      T1.SECTIONCD";

        return $query;
    }

    //科目の基本データを取得
    function find_class()
    {
        $query = "SELECT SECTIONCD, SECTIONABBV FROM SECTION_MST ";
        return $query;
    }

    //学期取得
    function getTerm($year, $day){

        return " VALUES TERM_GET('".$year."',DATE('".$day."'),'N') ";
    }

    //校時 2004/06/14 y.arakaki
    function GetPeriod()
    {
        return "SELECT namecd2 AS periodcd, name1 AS periodname FROM name_mst WHERE namecd1 = 'B001'";
    }

    //表示用データを取得
    function readQuery($model,$semester,$useTestCountflg)
    {
        //テスト項目マスタの集計フラグの表
        $query  = " WITH TEST_COUNTFLG AS ( ";
        $query .= "     SELECT ";
        $query .= "         T1.EXECUTEDATE, ";
        $query .= "         T1.PERIODCD, ";
        $query .= "         T1.CHAIRCD, ";
        $query .= "         T2.COUNTFLG "; //0：集計しない 0以外：集計する
        $query .= "     FROM ";
        $query .= "         SCH_CHR_TEST T1, ";
        if ($useTestCountflg == "TESTITEM_MST_COUNTFLG_NEW_SDIV") {
            $query .= "         TESTITEM_MST_COUNTFLG_NEW_SDIV T2 ";
        } else if ($useTestCountflg == "TESTITEM_MST_COUNTFLG") {
            $query .= "         TESTITEM_MST_COUNTFLG T2 ";
        } else {
            $query .= "         TESTITEM_MST_COUNTFLG_NEW T2 ";
        }
        $query .= "     WHERE ";
        $query .= "             T1.EXECUTEDATE = DATE('".$model->cntl_dt_key."') ";
        $query .= "         AND T2.YEAR       = T1.YEAR ";
        if ($useTestCountflg == "TESTITEM_MST_COUNTFLG_NEW_SDIV") {
            $query .= "         AND T2.SEMESTER   = T1.SEMESTER ";
            $query .= "         AND T2.SCORE_DIV  = '01' ";
        } else if ($useTestCountflg == "TESTITEM_MST_COUNTFLG") {
        } else {
            $query .= "         AND T2.SEMESTER   = T1.SEMESTER ";
        }
        $query .= "         AND T2.TESTKINDCD = T1.TESTKINDCD ";
        $query .= "         AND T2.TESTITEMCD = T1.TESTITEMCD ";
        $query .= "     GROUP BY ";
        $query .= "         T1.EXECUTEDATE, ";
        $query .= "         T1.PERIODCD, ";
        $query .= "         T1.CHAIRCD, ";
        $query .= "         T2.COUNTFLG ";
        $query .= " ) ";
//2006.02.02 alp m-yama A1,A2を追加
        $query .=" , SUBSCHMAIN AS ( ";
        $query .= " SELECT ";
        $query .= "       T1.SEMESTER,T1.GRADE,T1.HR_CLASS,T1.GRADE || T1.HR_CLASS AS TARGETCLASS,T1.HR_NAME, T1.TR_CD1, ";
        $query .= "       T2.CHAIRCD,T3.EXECUTEDATE,T3.PERIODCD,T3.EXECUTED AS ATTENDCD,T3.DATADIV, ";
        $query .= "       A1.STAFFCD, ";
        $query .= "       A2.STAFFNAME_SHOW AS NAME_SHOW, ";
        $query .= "       A2.SECTIONCD,T6.SUBCLASSABBV,substr(T2.chairname,1,9) AS CHAIRNAME,T7.NAMESPARE1 AS SHR ";
        $query .= "   FROM ";
        $query .= "       SCHREG_REGD_HDAT T1 ";
        $query .= "       LEFT JOIN (SELECT ";
        $query .= "                    T1.YEAR, ";
        $query .= "                    T1.SEMESTER, ";
        $query .= "                    L2.GRADE, ";
        $query .= "                    L2.HR_CLASS, ";
        $query .= "                    T1.CHAIRCD, ";
        if ($model->Properties["useCurriculumcd"] == "1") {
            $query .= "      L1.CLASSCD,";
            $query .= "      L1.SCHOOL_KIND,";
            $query .= "      L1.CURRICULUM_CD,";
        }
        $query .= "                    L1.SUBCLASSCD, ";
        $query .= "                    L1.CHAIRNAME ";
        $query .= "                FROM ";
        $query .= "                    CHAIR_STD_DAT T1 ";
        $query .= "                    LEFT JOIN CHAIR_DAT L1 ON T1.YEAR = L1.YEAR ";
        $query .= "                         AND T1.SEMESTER = L1.SEMESTER ";
        $query .= "                         AND T1.CHAIRCD = L1.CHAIRCD ";
        $query .= "                    LEFT JOIN SCHREG_REGD_DAT L2 ON T1.SCHREGNO = L2.SCHREGNO ";
        $query .= "                         AND T1.YEAR = L2.YEAR ";
        $query .= "                         AND T1.SEMESTER = L2.SEMESTER ";
        $query .= "                WHERE ";
        $query .= "                    T1.YEAR = '".$model->cntl_dt_year."' ";
        $query .= "                    AND T1.SEMESTER = '".$semester."' ";
        $query .= "                    AND '".$model->cntl_dt_key."' BETWEEN T1.APPDATE AND T1.APPENDDATE ";
        $query .= "                GROUP BY ";
        $query .= "                    T1.YEAR, ";
        $query .= "                    T1.SEMESTER, ";
        $query .= "                    L2.GRADE, ";
        $query .= "                    L2.HR_CLASS, ";
        $query .= "                    T1.CHAIRCD, ";
        if ($model->Properties["useCurriculumcd"] == "1") {
            $query .= "      L1.CLASSCD,";
            $query .= "      L1.SCHOOL_KIND,";
            $query .= "      L1.CURRICULUM_CD,";
        }
        $query .= "                    L1.SUBCLASSCD, ";
        $query .= "                    L1.CHAIRNAME ";
        $query .= "       ) T2 ON T1.YEAR = T2.YEAR ";
        $query .= "            AND T1.GRADE = T2.GRADE ";
        $query .= "            AND T1.HR_CLASS = T2.HR_CLASS ";
        $query .= "            AND T1.SEMESTER = T2.SEMESTER ";
        $query .= "       LEFT JOIN SCH_CHR_DAT T3 ";
        $query .= "              ON T2.YEAR = T3.YEAR AND T2.CHAIRCD = T3.CHAIRCD ";
        $query .= "       LEFT JOIN SUBCLASS_MST T6 ";
        $query .= "              ON T2.SUBCLASSCD = T6.SUBCLASSCD ";
        if ($model->Properties["useCurriculumcd"] == "1") {
            $query .= "              AND T2.CLASSCD = T6.CLASSCD ";
            $query .= "              AND T2.SCHOOL_KIND = T6.SCHOOL_KIND ";
            $query .= "              AND T2.CURRICULUM_CD = T6.CURRICULUM_CD ";
        }
        $query .= "       LEFT JOIN V_NAME_MST T7 ";
        $query .= "              ON T3.YEAR= T7.YEAR AND T7.NAMECD1='B001' AND T7.NAMECD2=T3.PERIODCD ";
        $query .= "       LEFT JOIN SCH_STF_DAT A1 ";
        $query .= "              ON T3.PERIODCD || T3.CHAIRCD = A1.PERIODCD || A1.CHAIRCD AND A1.executedate = DATE('".$model->cntl_dt_key."') ";
        $query .= "       LEFT JOIN V_STAFF_MST A2 ";
        $query .= "              ON A2.YEAR = T1.YEAR AND A2.STAFFCD = A1.STAFFCD ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR         = '".$model->cntl_dt_year."' AND ";
        $query .= "     T1.SEMESTER     = '".$semester."' AND ";
        $query .= "     T3.EXECUTEDATE  = DATE('".$model->cntl_dt_key."') ";
        $query .= " ), SCHMAIN AS ( ";
        $query .= " SELECT * FROM SUBSCHMAIN WHERE STAFFCD IS NOT NULL ";
        $query .= " ), CHRMAIN AS ( ";
        $query .= " SELECT ";
        $query .= "     T1.SEMESTER,T1.GRADE,T1.HR_CLASS,T1.GRADE || T1.HR_CLASS AS TARGETCLASS,T1.HR_NAME, T1.TR_CD1, ";
        $query .= "     T2.CHAIRCD,T3.EXECUTEDATE,T3.PERIODCD,T3.EXECUTED AS ATTENDCD,T3.DATADIV, ";
        $query .= "     T4.STAFFCD, ";
        $query .= "     T5.STAFFNAME_SHOW AS NAME_SHOW, ";
        $query .= "     T5.SECTIONCD,T6.SUBCLASSABBV,substr(T2.chairname,1,9) AS CHAIRNAME,T7.NAMESPARE1 AS SHR ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_HDAT T1 ";
        $query .= "       LEFT JOIN (SELECT ";
        $query .= "                    T1.YEAR, ";
        $query .= "                    T1.SEMESTER, ";
        $query .= "                    L2.GRADE, ";
        $query .= "                    L2.HR_CLASS, ";
        $query .= "                    T1.CHAIRCD, ";
        if ($model->Properties["useCurriculumcd"] == "1") {
            $query .= "                    L1.CLASSCD,";
            $query .= "                    L1.SCHOOL_KIND,";
            $query .= "                    L1.CURRICULUM_CD,";
        }
        $query .= "                    L1.SUBCLASSCD, ";
        $query .= "                    L1.CHAIRNAME ";
        $query .= "                FROM ";
        $query .= "                    CHAIR_STD_DAT T1 ";
        $query .= "                    LEFT JOIN CHAIR_DAT L1 ON T1.YEAR = L1.YEAR ";
        $query .= "                         AND T1.SEMESTER = L1.SEMESTER ";
        $query .= "                         AND T1.CHAIRCD = L1.CHAIRCD ";
        $query .= "                    LEFT JOIN SCHREG_REGD_DAT L2 ON T1.SCHREGNO = L2.SCHREGNO ";
        $query .= "                         AND T1.YEAR = L2.YEAR ";
        $query .= "                         AND T1.SEMESTER = L2.SEMESTER ";
        $query .= "                WHERE ";
        $query .= "                    T1.YEAR = '".$model->cntl_dt_year."' ";
        $query .= "                    AND T1.SEMESTER = '".$semester."' ";
        $query .= "                    AND '".$model->cntl_dt_key."' BETWEEN T1.APPDATE AND T1.APPENDDATE ";
        $query .= "                GROUP BY ";
        $query .= "                    T1.YEAR, ";
        $query .= "                    T1.SEMESTER, ";
        $query .= "                    L2.GRADE, ";
        $query .= "                    L2.HR_CLASS, ";
        $query .= "                    T1.CHAIRCD, ";
        if ($model->Properties["useCurriculumcd"] == "1") {
            $query .= "                    L1.CLASSCD,";
            $query .= "                    L1.SCHOOL_KIND,";
            $query .= "                    L1.CURRICULUM_CD,";
        }
        $query .= "                    L1.SUBCLASSCD, ";
        $query .= "                    L1.CHAIRNAME ";
        $query .= "       ) T2 ON T1.YEAR = T2.YEAR ";
        $query .= "            AND T1.GRADE = T2.GRADE ";
        $query .= "            AND T1.HR_CLASS = T2.HR_CLASS ";
        $query .= "            AND T1.SEMESTER = T2.SEMESTER ";
        $query .= "       LEFT JOIN SCH_CHR_DAT T3 ";
        $query .= "              ON T2.YEAR = T3.YEAR AND T2.CHAIRCD = T3.CHAIRCD ";
        $query .= "       LEFT JOIN CHAIR_STF_DAT T4 ";
        $query .= "              ON T2.YEAR = T4.YEAR AND T2.CHAIRCD = T4.CHAIRCD AND T2.SEMESTER = T4.SEMESTER ";
        $query .= "       LEFT JOIN V_STAFF_MST T5  ";
        $query .= "              ON T4.YEAR = T5.YEAR AND T4.STAFFCD = T5.STAFFCD ";
        $query .= "       LEFT JOIN SUBCLASS_MST T6 ";
        $query .= "              ON T2.SUBCLASSCD = T6.SUBCLASSCD ";
        if ($model->Properties["useCurriculumcd"] == "1") {
            $query .= "              AND T2.CLASSCD = T6.CLASSCD ";
            $query .= "              AND T2.SCHOOL_KIND = T6.SCHOOL_KIND ";
            $query .= "              AND T2.CURRICULUM_CD = T6.CURRICULUM_CD ";
        }
        $query .= "       LEFT JOIN V_NAME_MST T7 ";
        $query .= "              ON T3.YEAR= T7.YEAR AND T7.NAMECD1='B001' AND T7.NAMECD2=T3.PERIODCD ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR         = '".$model->cntl_dt_year."' AND ";
        $query .= "     T1.SEMESTER     = '".$semester."' AND ";
        $query .= "     T3.EXECUTEDATE  = DATE('".$model->cntl_dt_key."') ";
        $query .= " ), UNIALL_T AS ( ";
        $query .= " SELECT * FROM SCHMAIN ";
        $query .= " UNION ";
        $query .= " SELECT * FROM CHRMAIN WHERE VALUE(CHAIRCD,'') || VALUE(PERIODCD,'') ";
        $query .= "                             NOT IN (SELECT VALUE(CHAIRCD,'') || VALUE(PERIODCD,'') ";
        $query .= "                                     FROM SCHMAIN) ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     T1.SEMESTER, ";
        $query .= "     T1.GRADE, ";
        $query .= "     T1.HR_CLASS, ";
        $query .= "     T1.TARGETCLASS, ";
        $query .= "     T1.HR_NAME, ";
        $query .= "     T1.TR_CD1, ";
        $query .= "     T1.CHAIRCD, ";
        $query .= "     T1.EXECUTEDATE, ";
        $query .= "     T1.PERIODCD, ";
        $query .= "     CASE WHEN VALUE(T1.ATTENDCD, '0') = '0' ";
        $query .= "          THEN VALUE(L1.EXECUTED, '0')  ";
        $query .= "          ELSE T1.ATTENDCD END AS ATTENDCD, ";
        $query .= "     T1.STAFFCD, ";
        $query .= "     T1.NAME_SHOW, ";
        $query .= "     T1.SECTIONCD, ";
        $query .= "     T1.SUBCLASSABBV, ";
        $query .= "     T1.CHAIRNAME, ";
        $query .= "     T1.SHR, ";
        $query .= "     CASE WHEN T1.DATADIV='2' THEN TEST.COUNTFLG ELSE L2.COUNTFLG END AS COUNTFLG, ";
        $query .= "     N1.NAMECD2 AS LESSON_MODE ";
        $query .= " FROM ";
        $query .= "     UNIALL_T T1 ";
        $query .= "     LEFT JOIN SCH_CHR_HRATE_DAT L1 ON L1.EXECUTEDATE = T1.EXECUTEDATE ";
        $query .= "          AND L1.PERIODCD = T1.PERIODCD ";
        $query .= "          AND L1.CHAIRCD = T1.CHAIRCD ";
        $query .= "          AND L1.GRADE = T1.GRADE ";
        $query .= "          AND L1.HR_CLASS = T1.HR_CLASS ";
        $query .= "     LEFT JOIN SCH_CHR_COUNTFLG L2 ON T1.EXECUTEDATE = L2.EXECUTEDATE ";
        $query .= "          AND T1.PERIODCD = L2.PERIODCD ";
        $query .= "          AND T1.CHAIRCD = L2.CHAIRCD ";
        $query .= "          AND T1.GRADE = L2.GRADE ";
        $query .= "          AND T1.HR_CLASS = L2.HR_CLASS ";
        $query .= "     LEFT JOIN NAME_MST N1 ON N1.NAMECD1 = 'B002' ";
        $query .= "          AND L2.LESSON_MODE = N1.NAMECD2 ";
        $query .= "     LEFT JOIN TEST_COUNTFLG TEST ";
        $query .= "         ON  TEST.EXECUTEDATE = T1.EXECUTEDATE ";
        $query .= "         AND TEST.PERIODCD    = T1.PERIODCD ";
        $query .= "         AND TEST.CHAIRCD     = T1.CHAIRCD ";
        $query .= " ORDER BY ";
        $query .= "     T1.PERIODCD ";
        return $query;
    }

    function getLessonName($lessonMode) {
        $query  = " SELECT ";
        $query .= "     ABBV1 ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = 'B002' ";
        $query .= "     AND NAMECD2 = '".$lessonMode."' ";

        return $query;
    }

    //データを移動
    function keyMoverQuery($key, $move_key = "")
    {
        switch ($move_key) {

        case "next":
            $max_min = " MIN";
            $handle = " > ";
            $handle_end = "";
            break;

        case "before":
            $max_min = " MAX";
            $handle = " < ";
            $handle_end = "";
            break;

        default:
            $max_min = " MIN";
            $handle = " BETWEEN ";
            $handle_end = " AND DATE('".(CTRL_YEAR + 1)."-3-31') ";
            break;
        }

        $db     = Query::dbCheckOut();

        $query  = "SELECT ";
        $query  .= $max_min;
        $query  .= "(EXECUTEDATE) as EXECUTEDATE  FROM ";
        $query  .= " SCH_CHR_DAT ";
        $query  .= " WHERE ";
        $query  .= " EXECUTEDATE ";
        $query  .= $handle;
        $query  .= " DATE('";
        $query  .= $key;
        $query  .= "') ";
        $query  .= $handle_end ;

        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);

        return $row["EXECUTEDATE"];
    }

    function get_myClass($cntl_dt_key,$staffcd)
    {
        $db     = Query::dbCheckOut();
        $cntl_dt_key = str_replace("/","-",$cntl_dt_key);

        $query  = " SELECT GRADE || HR_CLASS AS MYCLASS FROM SCHREG_REGD_HDAT ";
        $query  .= " WHERE ";
        $query  .= "       YEAR     = FISCALYEAR(DATE('".$cntl_dt_key."')) AND";
        $query  .= "       SEMESTER = TERM_GET(FISCALYEAR(DATE('".$cntl_dt_key."'))".",DATE('".$cntl_dt_key."'),'N') AND ";
        $query  .= "      (";
        $query  .= "       TR_CD1       = '". $staffcd ."' OR";
        $query  .= "       TR_CD2       = '". $staffcd ."' OR";
        $query  .= "       TR_CD3       = '". $staffcd ."' OR";
        $query  .= "       SUBTR_CD1    = '". $staffcd ."' OR";
        $query  .= "       SUBTR_CD2    = '". $staffcd ."' OR";
        $query  .= "       SUBTR_CD3    = '". $staffcd ."'";
        $query  .= "      )";

        $result = $db->query($query);
        $class = array();
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            $class[] = $row["MYCLASS"];
        }
        Query::dbCheckIn($db);

        return $class;
    }

    //表示項目の取得
    function getDispCol()
    {
        $query  = " SELECT COALESCE(MAX(NAMECD2),'1') AS DISP_COL  ";
        $query .= " FROM V_NAME_MST ";
        $query .= " WHERE YEAR    = '".CTRL_YEAR."' AND ";
        $query .= "       NAMECD1 = 'C000' ";
        return $query;
    }
}
?>
