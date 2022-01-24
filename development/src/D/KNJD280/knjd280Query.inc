<?php

require_once('for_php7.php');


class knjd280Query extends Query
{

    //対象外の生徒取得  /* NO002 */ /* NO003 */
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
    public function getAuth($model, $year, $semester)
    {
        //参照・更新可
        if (AUTHORITY == DEF_REFERABLE || AUTHORITY == DEF_UPDATABLE) {
            $query  = "SELECT T1.GRADE || T1.HR_CLASS AS VALUE,T1.HR_NAME AS LABEL ";
            $query .= "FROM SCHREG_REGD_HDAT T1 ";
            $query .= "LEFT JOIN SCHREG_REGD_GDAT T2 ON T2.YEAR = T1.YEAR AND T2.GRADE = T1.GRADE ";
            $query .= "WHERE T1.YEAR='" .$year ."'";
            $query .= "AND T1.SEMESTER='".$semester ."'";
            if ($model->Properties["useCurriculumcd"] == '1') {
                if ($model->Properties["use_prg_schoolkind"] == "1") {
                    if ($model->selectSchoolKind) {
                        $query .= " AND T2.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind), "','")."') ";
                    }
                } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
                    $query .= " AND T2.SCHOOL_KIND = '".SCHOOLKIND."' ";
                }
            }
        }
        //参照・更新可（制限付き）
        if (AUTHORITY == DEF_REFER_RESTRICT || AUTHORITY == DEF_UPDATE_RESTRICT) {
            $query  = "SELECT T1.GRADE || T1.HR_CLASS AS VALUE,T1.HR_NAME AS LABEL ";
            $query .= "FROM SCHREG_REGD_HDAT T1 ";
            $query .= "LEFT JOIN SCHREG_REGD_GDAT T2 ON T2.YEAR = T1.YEAR AND T2.GRADE = T1.GRADE ";
            $query .= "WHERE T1.YEAR='" .$year ."' ";
            $query .= "AND T1.SEMESTER='".$semester ."' ";
            if ($model->Properties["useCurriculumcd"] == '1') {
                if ($model->Properties["use_prg_schoolkind"] == "1") {
                    if ($model->selectSchoolKind) {
                        $query .= " AND T2.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind), "','")."') ";
                    }
                } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
                    $query .= " AND T2.SCHOOL_KIND = '".SCHOOLKIND."' ";
                }
            }
            $query .= " AND (T1.TR_CD1 = '" .STAFFCD ."' ";
            $query .= " OR T1.TR_CD2 = '" .STAFFCD ."' ";
            $query .= " OR T1.TR_CD3 = '" .STAFFCD ."') ";
        }

        return $query;
    }


    //学校長名取得
    public function getPrincipal($year)
    {
        $query = "SELECT staffname FROM v_staff_mst where year='".$year."' and jobcd='0001'";

        return $query;
    }


    //学期名取得
    public function getSemester($year)
    {
        $query = "SELECT semester,semestername FROM semester_mst where year='".$year."' and not semester='9' order by 1";

        return $query;
    }


    //生徒基本データ取得
    public function getinfo_ps1($year, $semester, $schregno)
    {

        $query  = "SELECT ";
        $query .= "     T2.NAME,";
        $query .= "     T2.NAME_KANA,";
        $query .= "     T2.BIRTHDAY,";
    //性別
        $query .= "     T7.ABBV1 AS SEX,";
        $query .= "     T1.GRADE,";
        $query .= "     T1.ATTENDNO,";
        $query .= "     T1.ANNUAL,";
        $query .= "     T6.HR_NAME,";
    //担任名 add nakamoto
        $query .= "     T13.STAFFNAME,";
    //課程・学科・コース
        $query .= "     T3.COURSENAME,";
        $query .= "     T4.MAJORNAME,";
        $query .= "     T5.COURSECODENAME,";
        $query .= "     T3.COURSEABBV,";
        $query .= "     T4.MAJORABBV,";
    //保護者情報
        $query .= "     T12.GUARD_NAME,";
        $query .= "     T12.GUARD_KANA,";
        $query .= "     VALUE(T12.GUARD_ADDR1,'') || VALUE(T12.GUARD_ADDR2,'') AS GUARD_ADDR,";
        $query .= "     T12.GUARD_ADDR1,";
        $query .= "     T12.GUARD_ADDR2,";
        $query .= "     T12.GUARD_ZIPCD,";
        $query .= "     T1.SCHREGNO ";
        $query .= "FROM ";
    //学籍情報(??? or ????)
        $query .= "     (";
        $query .= "         SELECT * ";
        $query .= "         FROM ";
        $query .= "             SCHREG_REGD_DAT T1 ";
        $query .= "         WHERE ";
        $query .= "                 T1.SCHREGNO='" .$schregno ."' ";
        $query .= "             AND T1.YEAR='" .$year ."' ";
        $query .= "             AND T1.SEMESTER='" .$semester ."' ";
        $query .= "     ) T1 ";
        $query .= "     INNER JOIN SCHREG_REGD_HDAT T6 ON T6.YEAR = T1.YEAR AND T6.SEMESTER = T1.SEMESTER ";
        $query .= "                                     AND T6.GRADE = T1.GRADE AND T6.HR_CLASS = T1.HR_CLASS ";
    //基礎情報
        $query .= "     INNER JOIN SCHREG_BASE_MST T2 ON T2.SCHREGNO = T1.SCHREGNO ";
        $query .= "     LEFT JOIN NAME_MST T7 ON NAMECD1='Z002' AND NAMECD2=T2.SEX ";
    //課程、学科、コース
        $query .= "     LEFT JOIN COURSE_MST T3 ON T3.COURSECD = T1.COURSECD ";
        $query .= "     LEFT JOIN MAJOR_MST T4 ON T4.COURSECD = T1.COURSECD AND T4.MAJORCD = T1.MAJORCD ";
        $query .= "     LEFT JOIN V_COURSECODE_MST T5 ON T5.YEAR = T1.YEAR ";
        $query .= "                             AND VALUE(T5.COURSECODE,'0000') = VALUE(T1.COURSECODE,'0000')";
    //保護者情報
        $query .= "     LEFT JOIN GUARDIAN_DAT T12 ON T12.SCHREGNO = T2.SCHREGNO ";
    //担任名情報 add nakamoto
        $query .= "     LEFT JOIN STAFF_MST T13 ON T13.STAFFCD = T6.TR_CD1 ";

        return $query;
    }


    //生徒別履修科目データ取得  //2004/04/01 nakamoto 教科コード仕様の変更に伴う修正
    public function getinfo_ps2($year, $semester, $schregno)
    {
        $subject_U = "89";          //教科コード    //2004/04/01

        $query  = "SELECT ";
        $query .= "     VALUE(T1.SUBCLASSCD,T2.SUBCLASSCD) AS SUBCLASSCD,";
        $query .= "     (SELECT CLASSNAME FROM CLASS_MST S1 WHERE ";
        $query .= "         SUBSTR(VALUE(T1.SUBCLASSCD,T2.SUBCLASSCD),1,2) = S1.CLASSCD) AS CLASSNAME,";
        $query .= "     (SELECT SUBCLASSNAME FROM SUBCLASS_MST S1 WHERE ";
        $query .= "         VALUE(T1.SUBCLASSCD,T2.SUBCLASSCD) = S1.SUBCLASSCD) AS SUBCLASSNAME ";
        $query .= "FROM ";
                    //素点データ
        $query .= "     (";
        $query .= "         SELECT ";
        $query .= "             W1.SUBCLASSCD ";
        $query .= "         FROM ";
        $query .= "             TESTSCORE_DAT W1 ";
        $query .= "         WHERE ";
        $query .= "                 W1.SCHREGNO ='" .$schregno. "' ";
        $query .= "             AND W1.YEAR = '" .$year. "' ";
        $query .= "             AND W1.SEMESTER <= '" . $semester . "' ";
        $query .= "             AND W1.TESTKINDCD IN ('01','02') ";
        $query .= "             AND W1.TESTITEMCD = '01' ";
        // 2004/02/19 $query .= "               AND W1.SUBCLASSCD BETWEEN '010000' AND '499999' ";
        // 2004/04/01 $query .= "               AND W1.SUBCLASSCD < '500000' ";
        $query .= "             AND substr(W1.SUBCLASSCD,1,2)<='".$subject_U."' ";
        $query .= "         GROUP BY ";
        $query .= "             W1.SUBCLASSCD ";
        $query .= "     )T1 ";
                    //成績期末データ・成績学年データ
        $query .= "     FULL JOIN(";
        $query .= "         SELECT ";
        $query .= "             VALUE(S1.SUBCLASSCD,S2.SUBCLASSCD) AS SUBCLASSCD ";
        $query .= "         FROM ";
        $query .= "             (";
        $query .= "                 SELECT ";
        $query .= "                     W1.GRADINGCLASSCD AS SUBCLASSCD ";
        $query .= "                 FROM ";
        $query .= "                     RECORDSEMES_DAT W1 ";
        $query .= "                 WHERE ";
        $query .= "                         W1.SCHREGNO ='" .$schregno. "' ";
        $query .= "                     AND W1.YEAR = '" .$year. "' ";
        $query .= "                     AND W1.SEMESTER <= '" . $semester . "' ";
        // 2004/02/19 $query .= "                       AND W1.GRADINGCLASSCD BETWEEN '010000' AND '499999' ";
        // 2004/04/01 $query .= "                       AND W1.GRADINGCLASSCD < '500000' ";
        $query .= "                     AND substr(W1.GRADINGCLASSCD,1,2)<='".$subject_U."' ";
        $query .= "                 GROUP BY ";
        $query .= "                     W1.GRADINGCLASSCD ";
        $query .= "             )S1 ";
        $query .= "             FULL JOIN(";
        $query .= "                 SELECT ";
        $query .= "                     W1.GRADINGCLASSCD AS SUBCLASSCD ";
        $query .= "                 FROM ";
        $query .= "                     RECORDSEMES_DAT W1 ";
        $query .= "                 WHERE ";
        $query .= "                         W1.SCHREGNO ='" .$schregno. "' ";
        $query .= "                     AND W1.YEAR = '" .$year. "' ";
        // 2004/02/19 $query .= "                       AND W1.GRADINGCLASSCD BETWEEN '010000' AND '499999' ";
        // 2004/04/01 $query .= "                       AND W1.GRADINGCLASSCD < '500000' ";
        $query .= "                     AND substr(W1.GRADINGCLASSCD,1,2)<='".$subject_U."' ";
        $query .= "                 GROUP BY ";
        $query .= "                     W1.GRADINGCLASSCD ";
        $query .= "             )S2 ON S2.SUBCLASSCD = S1.SUBCLASSCD ";
        $query .= "     )T2 ON T2.SUBCLASSCD = T1.SUBCLASSCD ";
        $query .= "ORDER BY SUBCLASSCD";

        return $query;
    }


    //生徒別科目別素点データ取得 2004/02/19 nakamoto 処理時間の短縮。
    public function getinfo_ps3($year, $semester, $schregno, $subclasscd)
    {
        $query  = "SELECT ";
        $query .= "     T1.SUBCLASSCD,";
        $query .= "     T1.SEMESTER,";
        $query .= "     T1.TESTKINDCD,";
        $query .= "     T1.SCORE,";
        $query .= "     T1.VALUE,";
        $query .= "     T1.ATTEND_FLG,";
        $query .= "     T2.AVG ";
        $query .= "FROM ";
                    //素点、中間評価
        $query .= "     (";
        $query .= "         SELECT ";
        $query .= "             W1.SUBCLASSCD,";
        $query .= "             W1.SEMESTER,";
        $query .= "             W1.TESTKINDCD,";
        $query .= "             W1.SCORE,";
        $query .= "             W1.VALUE,";
        $query .= "             W1.ATTEND_FLG ";
        $query .= "         FROM ";
        $query .= "             TESTSCORE_DAT W1 ";
        $query .= "         WHERE ";
        $query .= "                 W1.SCHREGNO ='" .$schregno. "' ";
        $query .= "             AND W1.YEAR = '" . $year . "' ";
        $query .= "             AND W1.SEMESTER <= '" . $semester . "' ";
        $query .= "             AND W1.TESTKINDCD IN ('01','02')";
        $query .= "             AND W1.TESTITEMCD = '01' ";
        $query .= "             AND W1.SUBCLASSCD ='" .$subclasscd. "' ";
        $query .= "     )T1 ";
                    //科目別コース別平均
        $query .= "     LEFT JOIN (";
        $query .= "         SELECT ";
        $query .= "             W1.SUBCLASSCD,";
        $query .= "             W1.SEMESTER,";
        $query .= "             W1.TESTKINDCD,";
        // 2004/02/19 $query .= "               DECIMAL(ROUND(DECIMAL(AVG(FLOAT(W1.SCORE)),5,2),1),5,1) AS AVG ";
        $query .= "             INT(ROUND(AVG(FLOAT(W1.SCORE))*10,0)/10) AS AVG ";
        $query .= "         FROM ";
        // 2004/02/19
        $query .= "             (";
        $query .= "                 SELECT ";
        $query .= "                     SCHREGNO,";
        $query .= "                     SUBCLASSCD,";
        $query .= "                     SEMESTER,";
        $query .= "                     TESTKINDCD,";
        $query .= "                     SCORE ";
        $query .= "                 FROM ";
        $query .= "                     TESTSCORE_DAT ";
        $query .= "                 WHERE ";
        $query .= "                         ATTEND_FLG = '1' ";
        $query .= "                     AND SUBCLASSCD ='" .$subclasscd. "' ";
        $query .= "                     AND YEAR = '" . $year . "' ";
        $query .= "                     AND SEMESTER <= '" . $semester . "' ";
        $query .= "                     AND TESTKINDCD IN ('01','02') ";
        $query .= "                     AND TESTITEMCD = '01' ";
        $query .= "             )W1 ";
        $query .= "             INNER JOIN(";
        $query .= "                 SELECT ";
        $query .= "                     SCHREGNO ";
        $query .= "                 FROM ";
        $query .= "                     SCHREG_REGD_DAT W2 ";
        $query .= "                 WHERE ";
        $query .= "                         YEAR = '" . $year . "' ";
        $query .= "                     AND SEMESTER <= '" . $semester . "' ";
        $query .= "                     AND EXISTS (SELECT 'X' ";
        $query .= "                                 FROM ";
        $query .= "                                     SCHREG_REGD_DAT W3 ";
        $query .= "                                 WHERE ";
        $query .= "                                         W3.GRADE = W2.GRADE ";
        $query .= "                                     AND W3.COURSECD = W2.COURSECD ";
        $query .= "                                     AND W3.MAJORCD = W2.MAJORCD ";
        $query .= "                                     AND VALUE(W3.COURSECODE,'0000') = VALUE(W2.COURSECODE,'0000') ";
        $query .= "                                     AND W3.SCHREGNO ='" .$schregno. "' ";
        $query .= "                                     AND W3.YEAR = '" . $year . "' ";
        $query .= "                                     AND W3.SEMESTER = '" . $semester . "')";
        $query .= "             )W2 ON W2.SCHREGNO = W1.SCHREGNO ";
/*
        $query .= "             TESTSCORE_DAT W1,";
        $query .= "             SCHREG_REGD_DAT W2 ";
        $query .= "         WHERE ";
        $query .= "                 W1.ATTEND_FLG = '1' ";
        $query .= "             AND W1.SUBCLASSCD ='" .$subclasscd. "' ";
        $query .= "             AND W1.YEAR = '" . $year . "' ";
        $query .= "             AND W1.SEMESTER <= '" . $semester . "' ";
        $query .= "             AND W1.TESTKINDCD IN ('01','02') ";
        $query .= "             AND W1.TESTITEMCD = '01' ";
        $query .= "             AND W1.YEAR = W2.YEAR ";
        $query .= "             AND W1.SEMESTER = W2.SEMESTER ";
        $query .= "             AND W1.SCHREGNO = W2.SCHREGNO ";
        $query .= "             AND EXISTS (SELECT 'X' ";
        $query .= "                         FROM ";
        $query .= "                             SCHREG_REGD_DAT W3 ";
        $query .= "                         WHERE ";
        $query .= "                                 W3.YEAR = W2.YEAR ";
        $query .= "                             AND W3.GRADE = W2.GRADE ";
        $query .= "                             AND W3.COURSECD = W2.COURSECD ";
        $query .= "                             AND W3.MAJORCD = W2.MAJORCD ";
        $query .= "                             AND VALUE(W3.COURSECODE,'0000') = VALUE(W2.COURSECODE,'0000') ";
        $query .= "                             AND W3.SCHREGNO ='" .$schregno. "' ";
        $query .= "                             AND W3.YEAR = '" .  $year . "' ";
        $query .= "                             AND W3.SEMESTER = '" .  $semester . "'";
        $query .= "                         ) ";
*/
        $query .= "         GROUP BY ";
        $query .= "             W1.SUBCLASSCD,";
        $query .= "             W1.SEMESTER,";
        $query .= "             W1.TESTKINDCD ";
        $query .= "     )T2 ON T1.SUBCLASSCD = T2.SUBCLASSCD AND T1.SEMESTER = T2.SEMESTER ";
        $query .= "                                         AND T1.TESTKINDCD = T2.TESTKINDCD ";
        $query .= "ORDER BY ";
        $query .= "     T1.SUBCLASSCD,";
        $query .= "     T1.SEMESTER,";
        $query .= "     T1.TESTKINDCD";

        return $query;
    }


    //生徒別科目別成績期末学年データ取得
    public function getinfo_ps4($year, $semester, $schregno, $subclasscd)
    {
        $query  = "SELECT ";
        $query .= "     W1.SEMESTER,";
        $query .= "     VALUATION,";
        $query .= "     0 AS GET_CREDIT ";
        $query .= "FROM ";
        $query .= "     RECORDSEMES_DAT W1 ";
        $query .= "WHERE ";
        $query .= "         W1.YEAR = '" . $year . "' ";
        $query .= "     AND W1.SEMESTER <= '" . $semester . "' ";
        $query .= "     AND W1.SCHREGNO ='" . $schregno . "' ";
        $query .= "     AND W1.GRADINGCLASSCD ='" . $subclasscd . "' ";
        $query .= "UNION ";
        $query .= "SELECT ";
        $query .= "     '9' AS SEMESTER,";
        $query .= "     VALUATION,";
        $query .= "     GET_CREDIT ";
        $query .= "FROM ";
        $query .= "     RECORDGRADE_DAT W1 ";
        $query .= "WHERE ";
        $query .= "         W1.YEAR = '" . $year . "' ";
        $query .= "     AND W1.SCHREGNO ='" . $schregno . "' ";
        $query .= "     AND W1.GRADINGCLASSCD ='" . $subclasscd . "' ";
        $query .= "ORDER BY ";
        $query .= "     SEMESTER";

        return $query;
    }


    //生徒別科目別授業時数・欠課時数データ取得
    public function getinfo_ps5($year, $semester, $schregno, $subclasscd)
    {
        $query  = "SELECT ";
        $query .= "     VALUE(W4.SEMESTER,'9') AS SEMESTER,";
        $query .= "     SUM(VALUE(W3.CREDITS,0) * VALUE(W2.CLASSWEEKS,0) - SUSPEND - MOURNING) AS JISU,";
        $query .= "     SUM(W4.ABSENT) AS ABSENT,";
        $query .= "     SUM(W3.CREDITS) AS CREDITS ";
        $query .= "FROM ";
        $query .= "     (";
        $query .= "         SELECT ";
        $query .= "             S1.SCHREGNO,";
        $query .= "             S1.SEMESTER,";
        $query .= "             S1.SUBCLASSCD,";
        $query .= "             VALUE(SUM(S1.SICK),0) + VALUE(SUM(S1.NOTICE),0) + ";
        $query .= "                 VALUE(SUM(S1.NONOTICE),0) + VALUE(SUM(S1.NURSEOFF),0) AS ABSENT,";
        $query .= "             VALUE(SUM(S1.SUSPEND),0) AS SUSPEND,";
        $query .= "             VALUE(SUM(S1.MOURNING),0) AS MOURNING ";
        $query .= "         FROM ";
        $query .= "             ATTEND_SUBCLASS_DAT S1 ";
        $query .= "         WHERE ";
        $query .= "                 S1.YEAR = '" . $year . "' ";
        $query .= "             AND S1.SEMESTER <= '" . $semester . "' ";
        $query .= "             AND S1.SCHREGNO ='" . $schregno . "' ";
        $query .= "             AND S1.SUBCLASSCD ='" . $subclasscd . "' ";
        $query .= "         GROUP BY ";
        $query .= "             S1.SCHREGNO,";
        $query .= "             S1.SEMESTER,";
        $query .= "             S1.SUBCLASSCD ";
        $query .= "     )W4 ";
        $query .= "     INNER JOIN SCHREG_REGD_DAT W1 ON W1.SCHREGNO = W4.SCHREGNO ";
        $query .= "                                 AND W1.SEMESTER = W4.SEMESTER ";
        $query .= "                                 AND W1.YEAR = '" . $year . "' ";
        $query .= "     INNER JOIN SCHREG_REGD_HDAT W2 ON W1.YEAR = W2.YEAR ";
        $query .= "                                 AND W1.SEMESTER = W2.SEMESTER ";
        $query .= "                                 AND W1.GRADE = W2.GRADE ";
        $query .= "                                 AND W1.HR_CLASS = W2.HR_CLASS ";
        $query .= "     LEFT JOIN CREDIT_MST W3 ON W1.YEAR = W3.YEAR ";
        $query .= "                                 AND W1.GRADE = W3.GRADE ";
        $query .= "                                 AND W1.COURSECD = W3.COURSECD ";
        $query .= "                                 AND W1.MAJORCD = W3.MAJORCD ";
        $query .= "                                 AND VALUE(W1.COURSECODE,'0000') = VALUE(W3.COURSECODE,'0000') ";
        $query .= "                                 AND W4.SUBCLASSCD = W3.SUBCLASSCD ";
        $query .= "GROUP BY ";
        $query .= "GROUPING SETS ";
        $query .= "     (W4.SEMESTER,()) ";
        $query .= "ORDER BY ";
        $query .= "     SEMESTER";

        return $query;
    }


    //生徒別出欠データ取得
    public function getinfo_ps6($year, $semester, $schregno, $grde_hrclass, $end_date)
    {

        $query  = "SELECT ";
        $query .= "     T1.SEMESTER,";
        $query .= "     CLASSDAYS,";
        $query .= "     SUSPEND,";
        $query .= "     MOURNING,";
        $query .= "     ABROAD_CLASSDAYS,";
        $query .= "     VALUE(CLASSDAYS,0)-VALUE(SUSPEND,0)-VALUE(MOURNING,0) AS PRESENT1,";
        $query .= "     VALUE(SICK,0)+VALUE(ACCIDENTNOTICE,0)+VALUE(NOACCIDENTNOTICE,0) AS ABSENT,";
        $query .= "     VALUE(CLASSDAYS,0)-VALUE(SUSPEND,0)-VALUE(MOURNING,0)-VALUE(SICK,0) ";
        $query .= "                 -VALUE(ACCIDENTNOTICE,0)-VALUE(NOACCIDENTNOTICE,0) AS PRESENT2,";
        $query .= "     LATE,";
        $query .= "     EARLY ";
        $query .= "FROM ";
        //  出欠データ
        $query .= "     (";
        $query .= "         SELECT ";
        $query .= "             VALUE(SEMESTER,'9') AS SEMESTER,";
        $query .= "             SUM(CLASSDAYS) AS CLASSDAYS,";
        $query .= "             SUM(SUSPEND) AS SUSPEND,";
        $query .= "             SUM(MOURNING) AS MOURNING,";
        $query .= "             SUM(SICK) AS SICK,";
        $query .= "             SUM(ACCIDENTNOTICE) AS ACCIDENTNOTICE,";
        $query .= "             SUM(NOACCIDENTNOTICE) AS NOACCIDENTNOTICE,";
        $query .= "             SUM(LATE1) AS LATE,";
        $query .= "             SUM(EARLY1) AS EARLY ";
        $query .= "         FROM ";
        $query .= "             ATTEND_SEMES_DAT ";
        $query .= "         WHERE ";
        $query .= "                 SCHREGNO = '" . $schregno . "' ";
        $query .= "             AND YEAR = '" . $year . "' ";
        $query .= "             AND SEMESTER <= '" . $semester . "' ";
        $query .= "         GROUP BY ";
        $query .= "         GROUPING SETS ";
        $query .= "             (SEMESTER,())";
        $query .= "     )T1 ";
        //  留学日数
        $query .= "     LEFT JOIN(";
        $query .= "         SELECT ";
        $query .= "             T2.SEMESTER,";
        $query .= "             COUNT(T3.EXECUTEDATE) AS ABROAD_CLASSDAYS ";
        $query .= "         FROM ";
        $query .= "             (";
        $query .= "                 SELECT ";
        $query .= "                     TRANSFER_SDATE,";
        $query .= "                     TRANSFER_EDATE ";
        $query .= "                 FROM ";
        $query .= "                     SCHREG_TRANSFER_DAT ";
        $query .= "                 WHERE ";
        $query .= "                         SCHREGNO = '" . $schregno . "' ";
        $query .= "                     AND TRANSFERCD = '2' ";
        $query .= "             )T1 ";
        $query .= "             INNER JOIN (";
        $query .= "                 SELECT DISTINCT ";
        $query .= "                     S1.EXECUTEDATE ";
        $query .= "                 FROM ";
        $query .= "                     (";
        $query .= "                         SELECT ";
        $query .= "                             W1.EXECUTEDATE,";
        $query .= "                             MAX(W1.CHAIRCD) AS CHAIRCD ";
        $query .= "                         FROM ";
        $query .= "                             SCH_CHR_DAT W1,";
        $query .= "                             CHAIR_CLS_DAT W2 ";
        $query .= "                         WHERE ";
        $query .= "                                 W1.YEAR  = '" . $year . "' ";
        $query .= "                             AND W1.YEAR = W2.YEAR ";
        $query .= "                             AND W1.CHAIRCD = W2.CHAIRCD ";
        $query .= "                             AND W2.TRGTGRADE || W2.TRGTCLASS = '" . $grde_hrclass . "' ";
        $query .= "                         GROUP BY ";
        $query .= "                             W1.EXECUTEDATE ";
        $query .= "                     )S1 ";
        $query .= "                     INNER JOIN CHAIR_DAT S2 ON S2.YEAR  = '" . $year . "' ";
        $query .= "                                                     AND S2.CHAIRCD = S1.CHAIRCD ";
        $query .= "             )T3 ON T3.EXECUTEDATE >= T1.TRANSFER_SDATE ";
        $query .= "                     AND T3.EXECUTEDATE <= VALUE(T1.TRANSFER_EDATE,DATE('" . $end_date . "'))";
        $query .= "             INNER JOIN SEMESTER_MST T2 ON T2.YEAR  = '" . $year . "' ";
        $query .= "                     AND T3.EXECUTEDATE BETWEEN T2.SDATE AND T2.EDATE ";
        $query .= "         GROUP BY ";
        $query .= "             T2.SEMESTER ";
        $query .= "     )T2 ON T1.SEMESTER = T2.SEMESTER ";
        $query .= "ORDER BY ";
        $query .= "     T1.SEMESTER";

        return $query;
    }


    //学校外における学修の単位認定データ取得
    public function getinfo_ps7($year, $schregno)
    {
        $query  = "SELECT ";
        $query .= "     W1.SUBCLASSCD,";
        $query .= "     SUBCLASSNAME,";
        $query .= "     CONTENTS,";
        $query .= "     CREDITS ";
        $query .= "FROM ";
        $query .= "     SCHREG_QUALIFIED_DAT W1 INNER JOIN SUBCLASS_MST W2 ON W2.SUBCLASSCD = W1.SUBCLASSCD ";
        $query .= "WHERE ";
        $query .= "         W1.YEAR='" .$year. "' ";
        $query .= "     AND W1.SCHREGNO ='" .$schregno. "' ";
        $query .= "ORDER BY ";
        $query .= "     W1.SUBCLASSCD";

        return $query;
    }


    //所見及び通信欄データ取得 2004/02/12 nakamoto テーブル変更により、学期をカット。
    public function getinfo_ps8($year, $semester, $schregno)
    {
        $query  = "SELECT ";
        // 2004/02/12 $query .= "       SEMESTER,";
        $query .= "     TOTALSTUDYTIME,";
        $query .= "     COMMUNICATION ";
        $query .= "FROM ";
        $query .= "     HREPORTREMARK_DAT ";
        $query .= "WHERE ";
        $query .= "         YEAR ='" .$year. "' ";
        // 2004/02/12 $query .= "       AND SEMESTER ='" .$semester. "' ";
        $query .= "     AND SCHREGNO ='" .$schregno. "'";

        return $query;
    }
}
