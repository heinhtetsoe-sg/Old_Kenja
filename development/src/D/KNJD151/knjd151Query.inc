<?php

require_once('for_php7.php');

class knjd151Query extends Query
{

    //対象外の生徒取得
    public function getSchnoIdou($model, $semester)
    {
        $query  = "WITH SCHNO AS (  ";
        $query .= "    SELECT SCHREGNO FROM SCHREG_REGD_DAT  ";
        $query .= "    WHERE YEAR='".CTRL_YEAR."' AND SEMESTER='".$semester."' AND ";
        $query .= "          GRADE || HR_CLASS = '".$model->field["GRADE_HR_CLASS"]."' ),  ";
        $query .= "BASE AS (  ";
        $query .= "    SELECT SCHREGNO FROM SCHREG_BASE_MST  ";
        $query .= "    WHERE SCHREGNO IN (SELECT SCHREGNO FROM SCHNO) AND  ";
        $query .= "        GRD_DIV IN ('2','3') AND GRD_DATE <= '".str_replace("/", "-", $model->field["DATE"])."'  ";
        $query .= "    GROUP BY SCHREGNO ), ";
        $query .= "TRANS AS (  ";
        $query .= "    SELECT SCHREGNO FROM SCHREG_TRANSFER_DAT  ";
        $query .= "    WHERE SCHREGNO IN (SELECT SCHREGNO FROM SCHNO) AND  ";
        $query .= "        ((TRANSFERCD IN ('1','2','3') AND  ";
        $query .= "          '".str_replace("/", "-", $model->field["DATE"])."' BETWEEN TRANSFER_SDATE AND TRANSFER_EDATE) OR  ";
        $query .= "         (TRANSFERCD IN ('4') AND '".str_replace("/", "-", $model->field["DATE"])."' < TRANSFER_SDATE) )  ";
        $query .= "    GROUP BY SCHREGNO ) ";

        $query .= "SELECT SCHREGNO FROM BASE  ";
        $query .= "UNION SELECT SCHREGNO FROM TRANS  ";

        return $query;
    }


    //年組取得（権限チェック）
    public function getAuth($year, $semester)
    {
        //参照・更新可
        if (AUTHORITY == DEF_REFERABLE || AUTHORITY == DEF_UPDATABLE) {
            $query  = "SELECT GRADE || HR_CLASS AS VALUE,HR_NAME AS LABEL ";
            $query .= "FROM SCHREG_REGD_HDAT ";
            $query .= "WHERE YEAR='" .$year ."'";
            $query .= "AND SEMESTER='".$semester ."'";
        }
        //参照・更新可（制限付き）
        if (AUTHORITY == DEF_REFER_RESTRICT || AUTHORITY == DEF_UPDATE_RESTRICT) {
            $query  = "SELECT GRADE || HR_CLASS AS VALUE,HR_NAME AS LABEL ";
            $query .= "FROM SCHREG_REGD_HDAT ";
            $query .= "WHERE YEAR='" .$year ."' ";
            $query .= "AND SEMESTER='".$semester ."' ";
            $query .= " AND (TR_CD1 = '" .STAFFCD ."' ";
            $query .= " OR TR_CD2 = '" .STAFFCD ."' ";
            $query .= " OR TR_CD3 = '" .STAFFCD ."') ";
        }

        return $query;
    }


    //テスト項目取得 NO001
    public function getTestkind($year, $semester, $model)
    {
        $query  = " SELECT ";
        $query .= "     TESTKINDCD || TESTITEMCD AS VALUE, ";
        $query .= "     TESTITEMNAME AS LABEL ";
        $query .= " FROM ";
        $query .=       $model->testTable;
        $query .= " WHERE ";
        $query .= "     YEAR = '" .$year. "' AND ";
        $query .= "     TESTKINDCD IN ('01','02') ";
        if ($model->testTable == "TESTITEM_MST_COUNTFLG_NEW") {
            $query .= "     AND SEMESTER = '".$semester ."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }


    // getRow共通
    public function sqlGetRow($query)
    {
        $db = Query::dbCheckOut();
        $ret_arr = array();
        $ret_arr = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);
        return $ret_arr;
    }

    //学期名称およびテスト項目
    public function sqlTestItem($year, $semester, $testitem, $model)
    {
        $query  = " SELECT ";
        $query .= "     TESTITEMNAME, ";
        $query .= "     SEMESTERNAME ";
        $query .= " FROM ";
        $query .= "     ".$model->testTable." T1, ";
        $query .= "     SEMESTER_MST T2";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = T2.YEAR AND ";
        $query .= "     T1.YEAR = '$year' AND ";
        $query .= "     T1.TESTKINDCD || T1.TESTITEMCD = '$testitem' AND ";
        $query .= "     T2.SEMESTER = '$semester'" ;
        if ($model->testTable == "TESTITEM_MST_COUNTFLG_NEW") {
            $query .= "     AND T1.SEMESTER = T2.SEMESTER ";
        }
        return $query;
    }

    //ＨＲ担任１・２・３
    public function sqlTrName($year, $semester, $grade_hr_class)
    {
        $query  = "SELECT  (SELECT STAFFNAME FROM STAFF_MST T2 WHERE T2.STAFFCD = T1.TR_CD1) AS TR_NAME1 ";
        $query .= "       ,(SELECT STAFFNAME FROM STAFF_MST T2 WHERE T2.STAFFCD = T1.TR_CD2) AS TR_NAME2 ";
        $query .= "       ,(SELECT STAFFNAME FROM STAFF_MST T2 WHERE T2.STAFFCD = T1.TR_CD3) AS TR_NAME3 ";
        $query .= "FROM    SCHREG_REGD_HDAT T1 ";
        $query .= "WHERE   T1.YEAR = '$year' ";
        $query .= "    AND T1.SEMESTER = '$semester' ";
        $query .= "    AND T1.GRADE||T1.HR_CLASS = '$grade_hr_class' ";
        return $query;
    }

    //テスト名取得
    public function getTestName($model)
    {
        $query  = " SELECT ";
        $query .= "     T2.TESTITEMNAME, ";
        $query .= "     T1.SEMESTER || T2.TESTKINDCD || T2.TESTITEMCD AS VALUE ";
        $query .= " FROM ";
        $query .= "     SEMESTER_MST T1, ";
        $query .= "     ".$model->testTable." T2 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = T2.YEAR AND ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' ";
        if ($model->testTable == "TESTITEM_MST_COUNTFLG_NEW") {
            $query .= "     AND T1.SEMESTER = T2.SEMESTER ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }


    //講座内生徒数・平均の配列を取得
    public function getChairAverage($model)
    {
        $db = Query::dbCheckOut();
        $ret_arr = array();
        $query  = knjd151Query::sqlChairAverage($model);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $key = $row["SUBCLASSCD"] ."-" .$row["CHAIRCD"];
            $val = $row["SCHNUM"] ."-" .$row["AVERAGE"] ."-" .$row["CHAIRNAME"] ."-" .$row["HENSA"];
            $ret_arr[$key] = $val;
        }
        $result->free();
        Query::dbCheckIn($db);
        return $ret_arr;
    }

    //講座内生徒数・平均を取得
    public function sqlChairAverage($model)
    {
        //パラメータ
        $selectdata = array();
        $selectdata = explode(",", $model->selectdata);
        $year       = $model->field["YEAR"];
        $semester   = $model->field["GAKKI"];
        $score_field = $this->testfield["SCORE_FIELD"];
        $chair_field = $this->testfield["CHAIR_FIELD"];

        $query  = "WITH AVERAGE_DATA AS( ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "    SELECT CLASSCD ";
            $query .= "          ,SCHOOL_KIND ";
            $query .= "          ,CURRICULUM_CD ";
            $query .= "          ,SUBCLASSCD ";
        } else {
            $query .= "    SELECT SUBCLASSCD ";
        }
        $query .= "          ,".$chair_field." AS CHAIRCD ";
        $query .= "          ,DECIMAL(ROUND(AVG(FLOAT(".$score_field."))*10,0)/10,5,1) AS AVERAGE ";
        $query .= "          ,DECIMAL(ROUND(STDDEV(FLOAT(".$score_field."))*10,0)/10,5,1) AS HENSA ";
        $query .= "          ,SUM(CASE WHEN ".$score_field." IS NOT NULL THEN 1 ELSE NULL END) AS SCHNUM ";
        $query .= "    FROM RECORD_DAT W1 ";
        $query .= "    WHERE YEAR = '$year' ";
        $query .= "      AND ".$score_field." IS NOT NULL ";
        $query .= "      AND EXISTS (SELECT 'X' FROM RECORD_DAT W2 ";
        $query .= "                  WHERE YEAR = '$year' ";
        $query .= "                    AND SCHREGNO IN ('" .implode($selectdata, "','") ."') ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "                    AND W2.CLASSCD       = W1.CLASSCD ";
            $query .= "                    AND W2.SCHOOL_KIND   = W1.SCHOOL_KIND ";
            $query .= "                    AND W2.CURRICULUM_CD = W1.CURRICULUM_CD ";
            $query .= "                    AND W2.SUBCLASSCD    = W1.SUBCLASSCD ";
            $query .= "                    AND W2.".$chair_field." = W1.".$chair_field.") ";
            $query .= "    GROUP BY CLASSCD,SCHOOL_KIND,CURRICULUM_CD,SUBCLASSCD,".$chair_field." ";
            $query .= "    ) ";

            $query .= "SELECT T1.CLASSCD || ':' || T1.SCHOOL_KIND || ':' || T1.CURRICULUM_CD || ':' || T1.SUBCLASSCD AS SUBCLASSCD, ";
            $query .= "       T1.CHAIRCD,T1.AVERAGE,T1.HENSA,T1.SCHNUM,T2.CHAIRNAME,T3.SUBCLASSABBV AS SUBCLASSNAME ";
            $query .= "FROM AVERAGE_DATA T1 ";
            $query .= "LEFT JOIN CHAIR_DAT T2 ON T2.YEAR = '$year' AND T2.SEMESTER = '$semester' AND T2.CHAIRCD = T1.CHAIRCD ";
            $query .= "LEFT JOIN SUBCLASS_MST T3 ON T3.CLASSCD = T1.CLASSCD AND T3.SCHOOL_KIND = T1.SCHOOL_KIND AND T3.CURRICULUM_CD = T1.CURRICULUM_CD AND T3.SUBCLASSCD = T1.SUBCLASSCD ";
        } else {
            $query .= "                    AND W2.SUBCLASSCD = W1.SUBCLASSCD ";
            $query .= "                    AND W2.".$chair_field." = W1.".$chair_field.") ";
            $query .= "    GROUP BY SUBCLASSCD,".$chair_field." ";
            $query .= "    ) ";

            $query .= "SELECT T1.SUBCLASSCD,T1.CHAIRCD,T1.AVERAGE,T1.HENSA,T1.SCHNUM,T2.CHAIRNAME,T3.SUBCLASSABBV AS SUBCLASSNAME ";
            $query .= "FROM AVERAGE_DATA T1 ";
            $query .= "LEFT JOIN CHAIR_DAT T2 ON T2.YEAR = '$year' AND T2.SEMESTER = '$semester' AND T2.CHAIRCD = T1.CHAIRCD ";
            $query .= "LEFT JOIN SUBCLASS_MST T3 ON T3.SUBCLASSCD = T1.SUBCLASSCD ";
        }
        return $query;
    }

    //講座内成績順位の配列を取得
    public function getChairRank($model, $chair_average)
    {
        $db = Query::dbCheckOut();
        $ret_arr = array();
        foreach ($chair_average as $key => $val) {
            $keydata = array();
            $keydata = explode("-", $key);
            $valdata = array();
            $valdata = explode("-", $val);
            $query  = knjd151Query::sqlChairRank($model, $keydata[0], $keydata[1], $valdata[1], $valdata[3]);
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $key1 = $row["SUBCLASSCD"] ."-" .$row["CHAIRCD"];
                $key2 = $row["SCORE"];
                $val2 = $row["RANK"] ."-" .$row["HENSACHI"];
                $ret_arr[$key1][$key2] = $val2;
            }
            $result->free();
        }
        Query::dbCheckIn($db);
        return $ret_arr;
    }

    //講座内成績順位を取得
    public function sqlChairRank($model, $subclass, $chair, $average, $hensa)
    {
        //パラメータ
        $selectdata = array();
        $selectdata = explode(",", $model->selectdata);
        $year       = $model->field["YEAR"];
        $score_field = $this->testfield["SCORE_FIELD"];
        $chair_field = $this->testfield["CHAIR_FIELD"];

        $query  = "SELECT SCHREGNO ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "      ,CLASSCD || ':' || SCHOOL_KIND || ':' || CURRICULUM_CD || ':' || SUBCLASSCD AS SUBCLASSCD ";
        } else {
            $query .= "      ,SUBCLASSCD ";
        }
        $query .= "      ,".$chair_field." AS CHAIRCD ";
        $query .= "      ,".$score_field." AS SCORE ";
        if (0 < $hensa) {
            $query .= "  ,DECIMAL(ROUND((".$score_field."-$average)/$hensa*100,0)/10+50,5,1) AS HENSACHI ";
        }
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "      ,RANK() OVER(PARTITION BY CLASSCD, SCHOOL_KIND, CURRICULUM_CD, SUBCLASSCD, ".$chair_field." ORDER BY ".$score_field." DESC) AS RANK ";
        } else {
            $query .= "      ,RANK() OVER(PARTITION BY SUBCLASSCD, ".$chair_field." ORDER BY ".$score_field." DESC) AS RANK ";
        }
        $query .= "FROM RECORD_DAT W1 ";
        $query .= "WHERE YEAR = '$year' ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "  AND CLASSCD || ':' || SCHOOL_KIND || ':' || CURRICULUM_CD || ':' || SUBCLASSCD = '$subclass' ";
        } else {
            $query .= "  AND SUBCLASSCD = '$subclass' ";
        }
        $query .= "  AND ".$chair_field." = '$chair' ";
        $query .= "  AND ".$score_field." IS NOT NULL ";
        $query .= "  AND EXISTS (SELECT 'X' FROM RECORD_DAT W2 ";
        $query .= "              WHERE YEAR = '$year' ";
        $query .= "                AND SCHREGNO IN ('" .implode($selectdata, "','") ."') ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "                AND W2.CLASSCD       = W1.CLASSCD ";
            $query .= "                AND W2.SCHOOL_KIND   = W1.SCHOOL_KIND ";
            $query .= "                AND W2.CURRICULUM_CD = W1.CURRICULUM_CD ";
        }
        $query .= "                AND W2.SUBCLASSCD = W1.SUBCLASSCD ";
        $query .= "                AND W2.".$chair_field." = W1.".$chair_field.") ";
        $query .= "GROUP BY SCHREGNO ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "      ,CLASSCD ";
            $query .= "      ,SCHOOL_KIND ";
            $query .= "      ,CURRICULUM_CD ";
        }
        $query .= "      ,SUBCLASSCD ";
        $query .= "      ,".$chair_field." ";
        $query .= "      ,".$score_field." ";
        if (0 < $hensa) {
            $query .= "  ,DECIMAL(ROUND((".$score_field."-$average)/$hensa*100,0)/10+50,5,1) ";
        }
        return $query;
    }

    // 遅刻何回で欠課とするかの指数取得
    public function sqlSchoolAbsentCov($year)
    {
        $query = "SELECT absent_cov,absent_cov_late FROM school_mst WHERE year = '$year'";
        return $query;
    }

    //出欠集計開始日付などを取得
    public function sqlAttendDate($year, $attenddate)
    {
        $query  = "SELECT W2.SEMESTER ";
        $query .= "      ,MAX(CASE WHEN W2.MONTH BETWEEN '01' AND '03' THEN RTRIM(CHAR(INT(W2.YEAR)+1)) ELSE W2.YEAR END) AS MAX_YEAR ";
        $query .= "      ,W2.MONTH ";
        $query .= "      ,MAX(W2.APPOINTED_DAY) AS MAX_APP ";
        $query .= "FROM   SEMESTER_MST W1 ";
        $query .= "      ,ATTEND_SEMES_DAT W2 ";
        $query .= "WHERE  W1.YEAR='$year' AND ";
        $query .= "       W2.YEAR=W1.YEAR AND W2.SEMESTER=W1.SEMESTER ";
        $query .= "GROUP BY W2.SEMESTER,W2.MONTH ";
        $query .= "HAVING MAX(CASE WHEN W2.MONTH BETWEEN '01' AND '03' THEN RTRIM(CHAR(INT(W2.YEAR)+1)) ELSE W2.YEAR END) ";
        $query .= "       || '-' || ";
        $query .= "       W2.MONTH ";
        $query .= "       || '-' || ";
        $query .= "       MAX(W2.APPOINTED_DAY) ";
        $query .= "       <= '$attenddate' ";
        $query .= "ORDER BY 2,3,4 ";
        return $query;
    }

    //成績データなどを取得
    public function sqlRecordAttend($model, $absent_cov, $absent_cov_late, $attend_seme_month, $attend_sdate)
    {
        //パラメータ
        $selectdata = array();
        $selectdata     = explode(",", $model->selectdata);
        $year           = $model->field["YEAR"];
        $semester       = $model->field["GAKKI"];
        $grade_hr_class = $model->field["GRADE_HR_CLASS"];
        $attenddate     = str_replace("/", "-", $model->field["DATE"]);

        //学籍の表
        $query  = "WITH SCHNO AS( ";
        $query .= "    SELECT  T2.SCHREGNO,T3.NAME,T4.HR_NAME,T2.GRADE,T2.HR_CLASS,T2.ATTENDNO,T2.COURSECD,T2.MAJORCD,T2.COURSECODE ";
        $query .= "    FROM    SCHREG_REGD_DAT T2 ";
        $query .= "           ,SCHREG_BASE_MST T3 ";
        $query .= "           ,SCHREG_REGD_HDAT T4 ";
        $query .= "    WHERE   T2.YEAR = '$year' ";
        $query .= "        AND T2.GRADE||T2.HR_CLASS = '$grade_hr_class' ";
        $query .= "        AND T2.SCHREGNO IN ('" .implode($selectdata, "','") ."') ";
        $query .= "        AND T2.SEMESTER = '$semester' ";
        $query .= "        AND T2.SCHREGNO = T3.SCHREGNO ";
        $query .= "        AND T4.YEAR = '$year' ";
        $query .= "        AND T4.SEMESTER = T2.SEMESTER ";
        $query .= "        AND T4.GRADE = T2.GRADE ";
        $query .= "        AND T4.HR_CLASS = T2.HR_CLASS ";
        $query .= "    ) ";

        //講座の表
        $query .= ",CHAIR_S AS( ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "    SELECT  S1.SCHREGNO,S1.SEMESTER,S1.CHAIRCD,S2.CLASSCD,S2.SCHOOL_KIND,S2.CURRICULUM_CD,S2.SUBCLASSCD ";
        } else {
            $query .= "    SELECT  S1.SCHREGNO,S1.SEMESTER,S1.CHAIRCD,S2.SUBCLASSCD ";
        }
        $query .= "    FROM    CHAIR_STD_DAT S1 ";
        $query .= "           ,CHAIR_DAT S2 ";
        $query .= "    WHERE   S1.YEAR = '$year' ";
        $query .= "        AND S2.YEAR = S1.YEAR ";
        $query .= "        AND S2.SEMESTER = S1.SEMESTER ";
        $query .= "        AND S2.CHAIRCD = S1.CHAIRCD ";
        $query .= "        AND (SUBSTR(SUBCLASSCD,1,2) <= '89' OR SUBSTR(SUBCLASSCD,1,2)  = '90') ";
        $query .= "        AND EXISTS(SELECT 'X' FROM SCHNO S3 WHERE S3.SCHREGNO = S1.SCHREGNO GROUP BY SCHREGNO) ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "    GROUP BY S1.SCHREGNO,S1.SEMESTER,S1.CHAIRCD,S2.CLASSCD,S2.SCHOOL_KIND,S2.CURRICULUM_CD,S2.SUBCLASSCD ";
        } else {
            $query .= "    GROUP BY S1.SCHREGNO,S1.SEMESTER,S1.CHAIRCD,S2.SUBCLASSCD ";
        }
        $query .= "    ) ";

        //出欠集計データの表
        $query .= ",ATTEND_CALC AS( ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "    SELECT  T1.SCHREGNO,T1.SEMESTER,T1.CLASSCD,T1.SCHOOL_KIND,T1.CURRICULUM_CD,T1.SUBCLASSCD ";
        } else {
            $query .= "    SELECT  T1.SCHREGNO,T1.SEMESTER,T1.SUBCLASSCD ";
        }
        $query .= "           ,SUM(VALUE(SICK,0) + VALUE(NOTICE,0) + VALUE(NONOTICE,0) + VALUE(NURSEOFF,0)) AS ABSENT1 ";
        $query .= "           ,SUM(VALUE(LATE,0) + VALUE(EARLY,0)) AS LATE_EARLY ";
        $query .= "    FROM    ATTEND_SUBCLASS_DAT T1 ";
        $query .= "    WHERE   YEAR = '$year' ";
        $query .= "        AND SEMESTER || MONTH IN ('" .implode($attend_seme_month, "','") ."') ";
        $query .= "        AND EXISTS(SELECT 'X'  FROM SCHNO T2  WHERE T2.SCHREGNO = T1.SCHREGNO  GROUP BY SCHREGNO)  ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "    GROUP BY T1.SCHREGNO,T1.SEMESTER,T1.CLASSCD,T1.SCHOOL_KIND,T1.CURRICULUM_CD,T1.SUBCLASSCD ";
        } else {
            $query .= "    GROUP BY T1.SCHREGNO,T1.SEMESTER,T1.SUBCLASSCD ";
        }
        $query .= "    )  ";

        //テスト項目マスタの集計フラグの表
        $query .= " , TEST_COUNTFLG AS ( ";
        $query .= "     SELECT ";
        $query .= "         T1.EXECUTEDATE, ";
        $query .= "         T1.PERIODCD, ";
        $query .= "         T1.CHAIRCD, ";
        $query .= "         '2' AS DATADIV ";
        $query .= "     FROM ";
        $query .= "         SCH_CHR_TEST T1, ";
        $query .= "         TESTITEM_MST_COUNTFLG_NEW T2 ";
        $query .= "     WHERE ";
        $query .= "             T2.YEAR       = T1.YEAR ";
        $query .= "         AND T2.SEMESTER   = T1.SEMESTER ";
        $query .= "         AND T2.TESTKINDCD = T1.TESTKINDCD ";
        $query .= "         AND T2.TESTITEMCD = T1.TESTITEMCD ";
        $query .= "         AND T2.COUNTFLG   = '0' "; //0：集計しない 0以外：集計する
        $query .= "     ) ";

        //出欠集計データの表
        $query .= ",ATTEND_A AS( ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "    SELECT  S1.SCHREGNO,S1.CLASSCD,S1.SCHOOL_KIND,S1.CURRICULUM_CD,S1.SUBCLASSCD,S1.SEMESTER ";
        } else {
            $query .= "    SELECT  S1.SCHREGNO,SUBCLASSCD,S1.SEMESTER ";
        }
        $query .= "           ,SUM(CASE WHEN (CASE WHEN ATT_DI.ATSUB_REPL_DI_CD IS NOT NULL THEN ATT_DI.ATSUB_REPL_DI_CD ELSE ATT_DI.REP_DI_CD END) IN('4','5','6','14','11','12','13') THEN 1 ELSE 0 END)AS ABSENT1 ";
        $query .= "           ,SUM(CASE WHEN (CASE WHEN ATT_DI.ATSUB_REPL_DI_CD IS NOT NULL THEN ATT_DI.ATSUB_REPL_DI_CD ELSE ATT_DI.REP_DI_CD END) IN('15','16','23','24') THEN SMALLINT(VALUE(ATT_DI.MULTIPLY, '1')) ELSE 0 END) AS LATE_EARLY ";
        $query .= "    FROM( ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "        SELECT  T2.SCHREGNO,EXECUTEDATE,PERIODCD,T3.CLASSCD,T3.SCHOOL_KIND,T3.CURRICULUM_CD,T3.SUBCLASSCD,T3.SEMESTER ";
        } else {
            $query .= "        SELECT  T2.SCHREGNO,EXECUTEDATE,PERIODCD,SUBCLASSCD,T3.SEMESTER ";
        }
        $query .= "        FROM    CHAIR_S T3 ";
        $query .= "               ,SCH_CHR_DAT T1 ";
        $query .= "               ,CHAIR_STD_DAT T2 ";
        $query .= "        WHERE   T1.YEAR = '$year' ";
        $query .= "            AND T1.SEMESTER = T3.SEMESTER ";
        $query .= "            AND T1.CHAIRCD = T3.CHAIRCD ";
        $query .= "            AND T2.YEAR = '$year' ";
        $query .= "            AND T2.CHAIRCD = T3.CHAIRCD ";
        $query .= "            AND T2.SCHREGNO = T3.SCHREGNO ";
        $query .= "            AND T2.SEMESTER = T3.SEMESTER ";
        $query .= "            AND T1.EXECUTEDATE BETWEEN '$attend_sdate' AND '$attenddate' ";
        $query .= "            AND T1.EXECUTEDATE BETWEEN T2.APPDATE AND T2.APPENDDATE  ";
        $query .= "            AND NOT EXISTS(SELECT  'X' FROM SCH_CHR_COUNTFLG T4 ";
        $query .= "                           WHERE   T4.EXECUTEDATE = T1.EXECUTEDATE ";
        $query .= "                               AND T4.PERIODCD = T1.PERIODCD ";
        $query .= "                               AND T4.CHAIRCD = T1.CHAIRCD ";
        $query .= "                               AND T4.GRADE||T4.HR_CLASS = '$grade_hr_class' ";
        $query .= "                               AND T1.DATADIV IN ('0','1') "; //テスト(DATADIV=2)以外
        $query .= "                               AND T4.COUNTFLG = '0') ";
        $query .= "            AND NOT EXISTS(SELECT 'X' FROM TEST_COUNTFLG TEST ";
        $query .= "                            WHERE TEST.EXECUTEDATE = T1.EXECUTEDATE ";
        $query .= "                              AND TEST.PERIODCD    = T1.PERIODCD ";
        $query .= "                              AND TEST.CHAIRCD     = T1.CHAIRCD ";
        $query .= "                              AND TEST.DATADIV     = T1.DATADIV) "; //テスト(DATADIV=2)
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "        GROUP BY T2.SCHREGNO,EXECUTEDATE,PERIODCD,T3.CLASSCD,T3.SCHOOL_KIND,T3.CURRICULUM_CD,T3.SUBCLASSCD,T3.SEMESTER ";
        } else {
            $query .= "        GROUP BY T2.SCHREGNO,EXECUTEDATE,PERIODCD,SUBCLASSCD,T3.SEMESTER ";
        }
        $query .= "    )S1 ";
        $query .= "    INNER JOIN ATTEND_DAT S2 ON S2.YEAR = '$year' ";
        $query .= "                            AND S2.ATTENDDATE = S1.EXECUTEDATE ";
        $query .= "                            AND S2.PERIODCD = S1.PERIODCD ";
        $query .= "                            AND S1.SCHREGNO = S2.SCHREGNO ";
        $query .= "    INNER JOIN ATTEND_DI_CD_DAT ATT_DI ON ATT_DI.YEAR    = S2.YEAR ";
        $query .= "                                      AND ATT_DI.DI_CD   = S2.DI_CD ";
        $query .= "                                      AND (CASE WHEN ATT_DI.ATSUB_REPL_DI_CD IS NOT NULL THEN ATT_DI.ATSUB_REPL_DI_CD ELSE ATT_DI.REP_DI_CD END) IN('4','5','6','11','12','13','14','15','16','23','24') ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "    GROUP BY S1.SCHREGNO,S1.CLASSCD,S1.SCHOOL_KIND,S1.CURRICULUM_CD,S1.SUBCLASSCD,S1.SEMESTER ";
            $query .= "    UNION ALL  ";
            $query .= "    SELECT  T1.SCHREGNO,T1.CLASSCD,T1.SCHOOL_KIND,T1.CURRICULUM_CD,T1.SUBCLASSCD,T1.SEMESTER ";
        } else {
            $query .= "    GROUP BY S1.SCHREGNO,S1.SUBCLASSCD,S1.SEMESTER ";
            $query .= "    UNION ALL  ";
            $query .= "    SELECT  T1.SCHREGNO,T1.SUBCLASSCD,T1.SEMESTER ";
        }
        $query .= "           ,SUM(ABSENT1) AS ABSENT1 ";
        $query .= "           ,SUM(LATE_EARLY) AS LATE_EARLY ";
        $query .= "    FROM    ATTEND_CALC T1 ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "    GROUP BY T1.SCHREGNO,T1.CLASSCD,T1.SCHOOL_KIND,T1.CURRICULUM_CD,T1.SUBCLASSCD,T1.SEMESTER ";
        } else {
            $query .= "    GROUP BY T1.SCHREGNO,T1.SUBCLASSCD,T1.SEMESTER ";
        }
        $query .= "    ) ";

        //出欠集計データの表
        $query .= ",ATTEND_B AS( ";
        if (($absent_cov == "1" || $absent_cov == "3") && (is_numeric($absent_cov_late) && (int)$absent_cov_late != 0)) {
            //学期でペナルティ欠課を算出する場合
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "SELECT  T1.SCHREGNO,T1.CLASSCD,T1.SCHOOL_KIND,T1.CURRICULUM_CD,T1.SUBCLASSCD ";
            } else {
                $query .= "SELECT  T1.SCHREGNO,T1.SUBCLASSCD ";
            }
            $query .= "       ,VALUE(SUM(ABSENT),0) AS ABSENT ";
            $query .= "       ,VALUE(SUM(ABSENT2),0) AS ABSENT2 ";
            $query .= "       ,VALUE(SUM(LATE_EARLY),0) AS LATE_EARLY ";
            $query .= "FROM ( ";
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "    SELECT  T1.SCHREGNO,T1.CLASSCD,T1.SCHOOL_KIND,T1.CURRICULUM_CD,T1.SUBCLASSCD,T1.SEMESTER ";
            } else {
                $query .= "    SELECT  T1.SCHREGNO,T1.SUBCLASSCD,T1.SEMESTER ";
            }
            $query .= "           ,VALUE(SUM(ABSENT1),0) AS ABSENT ";
            if ($absent_cov == "1") {
                $query .= "       ,VALUE(SUM(ABSENT1),0) + VALUE(SUM(LATE_EARLY),0) / ".$absent_cov_late." AS ABSENT2 ";
            } else {
                $query .= "       ,FLOAT(DECIMAL(VALUE(SUM(ABSENT1),0) + VALUE(SUM(FLOAT(LATE_EARLY)),0) / ".$absent_cov_late.",5,1)) AS ABSENT2 ";
            }
            $query .= "           ,VALUE(SUM(LATE_EARLY),0) - VALUE(SUM(LATE_EARLY),0) / ".$absent_cov_late." * ".$absent_cov_late." AS LATE_EARLY ";
            $query .= "    FROM    ATTEND_A T1 ";
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "    GROUP BY T1.SCHREGNO,T1.CLASSCD,T1.SCHOOL_KIND,T1.CURRICULUM_CD,T1.SUBCLASSCD,T1.SEMESTER ";
                $query .= ") T1 ";
                $query .= "GROUP BY T1.SCHREGNO,T1.CLASSCD,T1.SCHOOL_KIND,T1.CURRICULUM_CD,T1.SUBCLASSCD ";
            } else {
                $query .= "    GROUP BY T1.SCHREGNO,T1.SUBCLASSCD,T1.SEMESTER ";
                $query .= ") T1 ";
                $query .= "GROUP BY T1.SCHREGNO,T1.SUBCLASSCD ";
            }
        } elseif (($absent_cov == "2" || $absent_cov == "4") && (is_numeric($absent_cov_late) && (int)$absent_cov_late != 0)) {
            //通年でペナルティ欠課を算出する場合
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "SELECT  T1.SCHREGNO,T1.CLASSCD,T1.SCHOOL_KIND,T1.CURRICULUM_CD,T1.SUBCLASSCD ";
            } else {
                $query .= "SELECT  T1.SCHREGNO,T1.SUBCLASSCD ";
            }
            $query .= "       ,VALUE(SUM(ABSENT1),0) AS ABSENT ";
            if ($absent_cov == "2") {
                $query .= "   ,VALUE(SUM(ABSENT1),0) + VALUE(SUM(LATE_EARLY),0) / ".$absent_cov_late." AS ABSENT2 ";
            } else {
                $query .= "   ,FLOAT(DECIMAL(VALUE(SUM(ABSENT1),0) + VALUE(SUM(FLOAT(LATE_EARLY)),0) / ".$absent_cov_late.",5,1)) AS ABSENT2 ";
            }
            $query .= "       ,VALUE(SUM(LATE_EARLY),0) - VALUE(SUM(LATE_EARLY),0) / ".$absent_cov_late." * ".$absent_cov_late." AS LATE_EARLY ";
            $query .= "FROM    ATTEND_A T1 ";
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "GROUP BY T1.SCHREGNO,T1.CLASSCD,T1.SCHOOL_KIND,T1.CURRICULUM_CD,T1.SUBCLASSCD ";
            } else {
                $query .= "GROUP BY T1.SCHREGNO,T1.SUBCLASSCD ";
            }
        } else {
            //ペナルティ欠課なしの場合
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "SELECT  T1.SCHREGNO,T1.CLASSCD,T1.SCHOOL_KIND,T1.CURRICULUM_CD,T1.SUBCLASSCD ";
            } else {
                $query .= "SELECT  T1.SCHREGNO,T1.SUBCLASSCD ";
            }
            $query .= "       ,VALUE(SUM(ABSENT1),0) AS ABSENT ";
            $query .= "       ,VALUE(SUM(ABSENT1),0) AS ABSENT2 ";
            $query .= "       ,VALUE(SUM(LATE_EARLY),0) AS LATE_EARLY ";
            $query .= "FROM    ATTEND_A T1 ";
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "GROUP BY T1.SCHREGNO,T1.CLASSCD,T1.SCHOOL_KIND,T1.CURRICULUM_CD,T1.SUBCLASSCD ";
            } else {
                $query .= "GROUP BY T1.SCHREGNO,T1.SUBCLASSCD ";
            }
        }
        $query .= "    ) ";

        //メイン表
        $query .= "SELECT T2.HR_NAME,T2.ATTENDNO,T2.NAME ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "      ,T5.CLASSCD || ':' || T5.SCHOOL_KIND || ':' ||T5.CURRICULUM_CD || ':' ||T5.SUBCLASSCD AS SUBCLASSCD,T4.SUBCLASSNAME ";
        } else {
            $query .= "      ,T5.SUBCLASSCD,T4.SUBCLASSNAME ";
        }
        $query .= "      ,T6.CREDITS AS CREDITS ";

        $intr_term = array("_INTR", "_TERM");
        for ($i = 1; $i <= $semester; $i++) {
            for ($j = 0; $j < get_count($intr_term); $j++) {
                $t_name = $i .$intr_term[$j];
                $query .= "  ,SEM".$t_name."_CHAIRCD ";
                $query .= "  ,SEM".$t_name."_SCORE ";
                $query .= "  ,CASE WHEN SEM".$t_name."_VALUE IS NOT NULL THEN RTRIM(CHAR(SEM".$t_name."_VALUE)) ";
                $query .= "        ELSE SEM".$t_name."_VALUE_DI END AS SEM".$t_name."_VALUE ";
            }
            $query .= "  ,CASE WHEN SEM".$i."_VALUE IS NOT NULL THEN RTRIM(CHAR(SEM".$i."_VALUE)) ";
            $query .= "        ELSE SEM".$i."_VALUE_DI END AS SEM".$i."_VALUE ";
        }

        $query .= "      ,CASE WHEN GRAD_VALUE IS NOT NULL THEN RTRIM(CHAR(GRAD_VALUE)) ";
        $query .= "            ELSE GRAD_VALUE_DI END AS GRAD_VALUE ";
        $query .= "      ,T3.ABSENT,T3.ABSENT2,T3.LATE_EARLY,'".$model->lastColumn."' AS ".$model->lastColumn." ";
        $query .= "FROM( ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     SELECT  SCHREGNO,CLASSCD,SCHOOL_KIND,CURRICULUM_CD,SUBCLASSCD ";
            $query .= "     FROM    CHAIR_S ";
            $query .= "     WHERE   SEMESTER <= '$semester' ";
            $query .= "     GROUP BY SCHREGNO,CLASSCD,SCHOOL_KIND,CURRICULUM_CD,SUBCLASSCD ";
        } else {
            $query .= "     SELECT  SCHREGNO,SUBCLASSCD ";
            $query .= "     FROM    CHAIR_S ";
            $query .= "     WHERE   SEMESTER <= '$semester' ";
            $query .= "     GROUP BY SCHREGNO,SUBCLASSCD ";
        }
        $query .= "    ) T5 ";
        $query .= "    LEFT JOIN RECORD_DAT T1 ON T1.YEAR = '$year' ";
        $query .= "                           AND T1.SCHREGNO = T5.SCHREGNO ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "                           AND T1.CLASSCD = T5.CLASSCD ";
            $query .= "                           AND T1.SCHOOL_KIND = T5.SCHOOL_KIND ";
            $query .= "                           AND T1.CURRICULUM_CD = T5.CURRICULUM_CD ";
        }
        $query .= "                           AND T1.SUBCLASSCD = T5.SUBCLASSCD ";
        $query .= "    LEFT JOIN SCHNO T2 ON T2.SCHREGNO = T5.SCHREGNO ";
        $query .= "    LEFT JOIN ATTEND_B T3 ON T3.SCHREGNO = T5.SCHREGNO ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "                         AND T3.CLASSCD = T5.CLASSCD ";
            $query .= "                         AND T3.SCHOOL_KIND = T5.SCHOOL_KIND ";
            $query .= "                         AND T3.CURRICULUM_CD = T5.CURRICULUM_CD ";
        }
        $query .= "                         AND T3.SUBCLASSCD = T5.SUBCLASSCD ";
        $query .= "    LEFT JOIN CREDIT_MST T6 ON T6.YEAR = '$year' ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "                           AND T6.CLASSCD = T5.CLASSCD ";
            $query .= "                           AND T6.SCHOOL_KIND = T5.SCHOOL_KIND ";
            $query .= "                           AND T6.CURRICULUM_CD = T5.CURRICULUM_CD ";
        }
        $query .= "                           AND T6.SUBCLASSCD = T5.SUBCLASSCD ";
        $query .= "                           AND T6.GRADE = T2.GRADE ";
        $query .= "                           AND T6.COURSECD = T2.COURSECD ";
        $query .= "                           AND T6.MAJORCD = T2.MAJORCD ";
        $query .= "                           AND T6.COURSECODE = T2.COURSECODE ";
        $query .= "    LEFT JOIN SUBCLASS_MST T4 ON T4.SUBCLASSCD = T5.SUBCLASSCD ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "                             AND T4.CLASSCD = T5.CLASSCD ";
            $query .= "                             AND T4.SCHOOL_KIND = T5.SCHOOL_KIND ";
            $query .= "                             AND T4.CURRICULUM_CD = T5.CURRICULUM_CD ";
            $query .= "ORDER BY T2.ATTENDNO,T5.CLASSCD,T5.SCHOOL_KIND,T5.CURRICULUM_CD,T5.SUBCLASSCD ";
        } else {
            $query .= "ORDER BY T2.ATTENDNO,T5.SUBCLASSCD ";
        }
        return $query;
    }
}
