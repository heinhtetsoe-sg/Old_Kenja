<?php

require_once('for_php7.php');

class knjb060Query extends Query
{
    //Z010の取得
    public function getSchoolName()
    {
        $query  = " SELECT ";
        $query .= "     NAME1 ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "       NAMECD1 = 'Z010' ";
        $query .= "   AND NAMECD2 = '00' ";
        return $query;
    }

    //年度を取得
    public function getYear()
    {
        $query  = " SELECT ";
        $query .= "     '" .CTRL_YEAR ."' AS LABEL, ";
        $query .= "     '" .CTRL_YEAR ."' AS VALUE ";
        $query .= " FROM ";
        $query .= "     SYSIBM.SYSDUMMY1 ";
        $query .= " UNION ";
        $query .= " SELECT ";
        $query .= "     '" .(CTRL_YEAR + 1)."' AS LABEL, ";
        $query .= "     '" .(CTRL_YEAR + 1)."' AS VALUE ";
        $query .= " FROM ";
        $query .= "     SYSIBM.SYSDUMMY1 ";
        $query .= " ORDER BY ";
        $query .= "     VALUE DESC ";

        return $query;
    }

    //学校区分取得(単位制の場合、学級別は出力不可とする)---2005.05.19
    public function getSchooldiv($model)
    {
        $db = Query::dbCheckOut();
        $query = "SELECT SCHOOLDIV FROM SCHOOL_MST WHERE YEAR='".$model->year."'";
        $ret_val = $db->getOne($query);
        Query::dbCheckIn($db);
        return $ret_val;
    }
    //時間割パターンヘッダクエリ
    public function getBscHdQuery($model)
    {
        $query  ="SELECT ";
        $query .="    t1.year, ";
        $query .="    t1.semester, ";
        $query .="    t2.semestername, ";
        $query .="    t1.bscseq, ";
        $query .="    t1.title ";
        $query .="FROM ";
        $query .="    sch_ptrn_hdat t1, ";
        $query .="    semester_mst t2 ";
        $query .="WHERE ";
        $query .="    t1.year='" .$model->year ."' AND ";
        $query .="    t1.year=t2.year AND ";
        $query .="    t1.semester=t2.semester ";
        $query .="ORDER BY ";
        $query .="    t1.semester, ";
        $query .="    t1.bscseq ";

        $db = Query::dbCheckOut();
        $row1[]= array('label' => "", 'value' => "");
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $row1[]= array('label' => $row["SEMESTERNAME"]." Seq".$row["BSCSEQ"].":".$row["TITLE"],
                           'value' => $row["YEAR"].",".$row["BSCSEQ"].",".$row["SEMESTER"]);
        }
        $result->free();
        Query::dbCheckIn($db);

        return $row1;
    }

    //所属クエリ
    public function getSectQuery($year)
    {
        $query  = "SELECT ";
        $query .= " T1.SECTIONCD AS VALUE, ";
        $query .= " T1.SECTIONCD || '　' || T1.SECTIONNAME AS LABEL ";
        $query .= "FROM ";
        $query .= " SECTION_MST T1 ";
        $query .= " INNER JOIN  SECTION_YDAT T2 ON T1.SECTIONCD = T2.SECTIONCD ";
        $query .= "WHERE T2.YEAR = '" .$year ."' ";
        $query .= "ORDER BY T1.SECTIONCD";

        $db = Query::dbCheckOut();
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $row1[]= array('label' => $row["LABEL"],
                            'value' => $row["VALUE"]);
        }
        $result->free();
        Query::dbCheckIn($db);

        return $row1;
    }
    //年組取得
    public function getHrclass($YEAR, $SEMESTER, $model)
    {
        $query = "SELECT REGD_H.GRADE || REGD_H.HR_CLASS AS VALUE,REGD_H.HR_NAME AS LABEL ".
                    "FROM SCHREG_REGD_HDAT REGD_H ".
                    "WHERE REGD_H.YEAR='" .$YEAR ."' AND REGD_H.SEMESTER= '" .$SEMESTER ."'";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= "     AND REGD_H.GRADE IN (SELECT ";
                $query .= "                             REGD_G.GRADE ";
                $query .= "                          FROM ";
                $query .= "                             SCHREG_REGD_GDAT REGD_G ";
                $query .= "                          WHERE ";
                $query .= "                             REGD_G.YEAR = '" .$YEAR ."' ";
                $query .= "                             AND REGD_G.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind), "','")."')) ";
            }
        } elseif ($model->Properties["useSchool_KindField"] == "1") {
            $query .= "     AND REGD_H.GRADE IN (SELECT ";
            $query .= "                             REGD_G.GRADE ";
            $query .= "                          FROM ";
            $query .= "                             SCHREG_REGD_GDAT REGD_G ";
            $query .= "                          WHERE ";
            $query .= "                             REGD_G.YEAR = '" .$YEAR ."' ";
            $query .= "                             AND REGD_G.SCHOOL_KIND = '" .SCHOOLKIND ."') ";
        }
        return $query;
    }

    //課程学科コースを取得
    public function getCourse($model, $course = "")
    {
        $query  = "";
        $query .= " SELECT DISTINCT ";
        $query .= "     T1.COURSECD || T1.MAJORCD || T1.COURSECODE AS VALUE, ";
        $query .= "     T1.COURSECD || T1.MAJORCD || T1.COURSECODE || '　' || C1.COURSENAME || C2.MAJORNAME || C3.COURSECODENAME AS LABEL, ";
        $query .= "     C1.COURSENAME || C2.MAJORNAME || C3.COURSECODENAME AS CSV_LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1 ";
        $query .= "     LEFT JOIN COURSE_MST C1 ON C1.COURSECD = T1.COURSECD ";
        $query .= "     LEFT JOIN MAJOR_MST C2 ON C2.COURSECD = T1.COURSECD AND C2.MAJORCD = T1.MAJORCD ";
        $query .= "     LEFT JOIN COURSECODE_MST C3 ON C3.COURSECODE = T1.COURSECODE ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND T1.SEMESTER = '".CTRL_SEMESTER."' ";
        $query .= "     AND T1.GRADE || T1.HR_CLASS BETWEEN '".$model->field["GRADE_HR_CLASS1"]."' AND '".$model->field["GRADE_HR_CLASS2"]."' ";
        if (strlen($course)) {
            $query .= "     AND T1.COURSECD || T1.MAJORCD || T1.COURSECODE = '".$course."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";
        //echo $query;
        return $query;
    }

    //施設を取得
    public function getFacility()
    {
        $query  = "SELECT ";
        $query .= "    faccd AS VALUE, ";
        $query .= "    faccd || '　' || facilityname AS LABEL ";
        $query .= "FROM ";
        $query .= "    v_facility_mst ";
        $query .= "WHERE ";
        $query .= "    year='".CTRL_YEAR."' ";
        $query .= "ORDER BY ";
        $query .= "    faccd ";

        $db = Query::dbCheckOut();
        $row4 = array();
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $row4[]= array('label' => $row["LABEL"],
                           'value' => $row["VALUE"]);
        }
        $result->free();
        Query::dbCheckIn($db);

        return $row4;
    }

    /********** ここからＣＳＶ関連 **********/

    //通常時間割の時、指定日の学期情報を取得
    public function getSemesterInfo($date, $year)
    {
        $year = CTRL_YEAR;

        $query  = "";
        $query .= " SELECT ";
        $query .= "     YEAR, ";
        $query .= "     SEMESTER, ";
        $query .= "     SDATE, ";
        $query .= "     EDATE ";
        $query .= " FROM ";
        $query .= "     SEMESTER_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$year}' ";
        $query .= "     AND SEMESTER <> '9' ";
        $query .= "     AND date('{$date}') BETWEEN SDATE AND EDATE ";
        return $query;
    }
    //基本時間割タイトル取得
    public function getSubTitle($model)
    {
        $query  = " SELECT ";
        $query .= "     TITLE ";
        $query .= " FROM ";
        $query .= "     SCH_PTRN_HDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$model->year}' ";
        $query .= "     AND SEMESTER = '{$model->semester}' ";
        $query .= "     AND BSCSEQ = {$model->bscseq} ";
        return $query;
    }

    //配列定義に使用(名称マスタより校時略称などを取得
    public function psNameMstCommon($model)
    {
        $query  = "";
        $query .= " SELECT f_period(NAMECD2) AS PERIODCD,ABBV1 ";
        $query .= " FROM   V_NAME_MST ";
        $query .= " WHERE  YEAR='{$model->year}' AND ";
        $query .= "        NAMECD1='B001' AND ";
        $query .= "        NAMESPARE2 IS NULL ";
        $query .= " ORDER BY 1 ";
        return $query;
    }

    //配列定義に使用
    public function psCommon($model)
    {
        $query  = "";
        //職員別
        if ($model->field["KUBUN"] == 1) {
            $query .= " SELECT COUNT(*) AS MAX_CNT ";
            $query .= " FROM   V_SECTION_MST W1,V_STAFF_MST W2 ";
            $query .= " WHERE  W1.YEAR='{$model->year}' AND ";
            $query .= "        W1.SECTIONCD BETWEEN '{$model->field["SECTION_CD_NAME1"]}' AND '{$model->field["SECTION_CD_NAME2"]}' AND ";
            $query .= "        W2.YEAR=W1.YEAR AND ";
            $query .= "        W2.SECTIONCD=W1.SECTIONCD ";
        }
        //学級別
        if ($model->field["KUBUN"] == 2) {
            $query .= " SELECT COUNT(*) AS MAX_CNT ";
            $query .= " FROM   SCHREG_REGD_HDAT ";
            $query .= " WHERE  YEAR='{$model->year}' AND ";
            $query .= "        SEMESTER='{$model->semester}' AND ";
            $query .= "        GRADE||HR_CLASS BETWEEN '{$model->field["GRADE_HR_CLASS1"]}' AND '{$model->field["GRADE_HR_CLASS2"]}' ";
        }
        //生徒別
        if ($model->field["KUBUN"] == 3) {
            $query .= " SELECT COUNT(*) AS MAX_CNT ";
            $query .= " FROM   SCHREG_REGD_DAT ";
            $query .= " WHERE  YEAR='{$model->year}' AND ";
            $query .= "        SEMESTER='{$model->semester}' AND ";
            $query .= "        GRADE||HR_CLASS BETWEEN '{$model->field["GRADE_HR_CLASS3"]}' AND '{$model->field["GRADE_HR_CLASS4"]}' ";
        }
        //施設別
        if ($model->field["KUBUN"] == 4) {
            $query .= " SELECT COUNT(*) AS MAX_CNT ";
            $query .= " FROM   V_FACILITY_MST W1 ";
            $query .= " WHERE  W1.YEAR='{$model->year}' AND ";
            $query .= "        W1.FACCD BETWEEN '{$model->field["FACCD_NAME1"]}' AND '{$model->field["FACCD_NAME2"]}' ";
        }
        return $query;
    }

    //(共通)時間割
    public function getJikanwari($model)
    {
        $query  = "";
        //名称マスタ（校時：ＳＨＲを取得）
        $query .= " WITH PERIOD AS ( ";
        $query .= "     SELECT NAMECD2 AS PERIODCD ";
        $query .= "     FROM   V_NAME_MST ";
        $query .= "     WHERE  YEAR='{$model->year}' AND ";
        $query .= "            NAMECD1='B001' AND ";
        $query .= "            NAMESPARE2 IS NOT NULL ) ";
        //生徒別
        if ($model->field["KUBUN"] == 3) {
            //講座職員
            $query .= " ,CHAIR_STF AS ( ";
            //(生徒別のみ) on:職員は正担任(MAX職員番号)のみ出力, null:通常出力
            if ($model->field["STAFF_CHECK"] == "on") {
                $query .= "     SELECT ";
                $query .= "         YEAR, ";
                $query .= "         SEMESTER, ";
                $query .= "         CHAIRCD, ";
                $query .= "         max(STAFFCD) AS STAFFCD ";
                $query .= "     FROM ";
                $query .= "         CHAIR_STF_DAT ";
                $query .= "     WHERE ";
                $query .= "         YEAR = '{$model->year}' ";
                $query .= "         AND CHARGEDIV = 1 ";
                $query .= "     GROUP BY ";
                $query .= "         YEAR, ";
                $query .= "         SEMESTER, ";
                $query .= "         CHAIRCD ";
            } else {
                $query .= "     SELECT ";
                $query .= "         YEAR, ";
                $query .= "         SEMESTER, ";
                $query .= "         CHAIRCD, ";
                $query .= "         STAFFCD ";
                $query .= "     FROM ";
                $query .= "         CHAIR_STF_DAT ";
                $query .= "     WHERE ";
                $query .= "         YEAR = '{$model->year}' ";
            }
            $query .= "     ) ";
        }
        //講座時間割
        $query .= " ,SCH_DAT AS ( ";
        if ($model->field["RADIO"] == "1") { //基本
            $query .= " SELECT DISTINCT W1.DAYCD,W1.PERIODCD,W1.CHAIRCD ";
            $query .= "        ,W2.FACCD,W4.SUBCLASSCD,W4.GROUPCD ";
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "        ,W4.CLASSCD ";
                $query .= "        ,W4.SCHOOL_KIND ";
                $query .= "        ,W4.CURRICULUM_CD ";
            }
            $query .= "        ,CASE WHEN W5.STAFFCD IS NOT NULL THEN W5.STAFFCD ELSE W3.STAFFCD END AS STAFFCD ";//ダンスの先生
            $query .= "        ,CASE WHEN W1.CHAIRCD IS NOT NULL THEN null ELSE W1.CHAIRCD END AS TESTCD ";//基本はnull
            $query .= "        ,CASE WHEN W1.CHAIRCD IS NOT NULL THEN null ELSE W1.CHAIRCD END AS DATADIV ";
            $query .= "        ,W4.CHAIRNAME ";
            $query .= "        ,W4.CHAIRABBV ";
            $query .= " FROM   SCH_PTRN_DAT W1 ";
            //ダンスの先生
            $query .= "        LEFT JOIN SCH_PTRN_STF_DAT W5 ON (W5.YEAR = W1.YEAR AND ";
            $query .= "                                          W5.SEMESTER = W1.SEMESTER AND ";
            $query .= "                                          W5.BSCSEQ = W1.BSCSEQ AND ";
            $query .= "                                          W5.DAYCD = W1.DAYCD AND ";
            $query .= "                                          W5.PERIODCD = W1.PERIODCD AND ";
            $query .= "                                          W5.CHAIRCD = W1.CHAIRCD) ";
        } else { //通常
            $query .= " SELECT DISTINCT W1.EXECUTEDATE,DAYOFWEEK(W1.EXECUTEDATE) AS DAYCD ";
            $query .= "        ,W1.PERIODCD,W1.CHAIRCD,W4.SUBCLASSCD,W4.GROUPCD ";
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "        ,W4.CLASSCD ";
                $query .= "        ,W4.SCHOOL_KIND ";
                $query .= "        ,W4.CURRICULUM_CD ";
            }
            $query .= "        ,CASE WHEN W5.STAFFCD IS NOT NULL THEN W5.STAFFCD ELSE W3.STAFFCD END AS STAFFCD ";//ダンスの先生
            $query .= "        ,W6.TESTKINDCD||W6.TESTITEMCD AS TESTCD ";
            $query .= "        ,W1.DATADIV ";
            //施設別
            if ($model->field["KUBUN"] == 4) {
                $query .= "    ,W2.FACCD ";
            } else { //通常
                $query .= "    ,CASE WHEN W6.TESTKINDCD IS NULL THEN W2.FACCD END AS FACCD ";
            }
            $query .= "        ,W4.CHAIRNAME ";
            $query .= "        ,W4.CHAIRABBV ";
            $query .= " FROM   SCH_CHR_DAT W1 ";
            //ダンスの先生
            $query .= "        LEFT JOIN SCH_STF_DAT W5 ON (W5.EXECUTEDATE = W1.EXECUTEDATE AND ";
            $query .= "                                     W5.PERIODCD = W1.PERIODCD AND ";
            $query .= "                                     W5.CHAIRCD = W1.CHAIRCD) ";
            //テスト
            $query .= "        LEFT JOIN SCH_CHR_TEST W6 ON (W6.EXECUTEDATE = W1.EXECUTEDATE AND ";
            $query .= "                                      W6.PERIODCD = W1.PERIODCD AND ";
            $query .= "                                      W6.CHAIRCD = W1.CHAIRCD) ";
        }
        $query .= "            LEFT JOIN CHAIR_FAC_DAT W2 ON (W2.YEAR = W1.YEAR AND ";
        $query .= "                                           W2.SEMESTER = W1.SEMESTER AND ";
        $query .= "                                           W2.CHAIRCD = W1.CHAIRCD) ";
        $tableChairStf = ($model->field["KUBUN"] == 3) ? "CHAIR_STF" : "CHAIR_STF_DAT";
        $query .= "            LEFT JOIN {$tableChairStf} W3 ON (W3.YEAR = W1.YEAR AND ";
        $query .= "                                           W3.SEMESTER = W1.SEMESTER AND ";
        $query .= "                                           W3.CHAIRCD = W1.CHAIRCD) ";
        $query .= "            LEFT JOIN CHAIR_DAT W4 ON (W4.YEAR = W1.YEAR AND ";
        $query .= "                                       W4.SEMESTER = W1.SEMESTER AND ";
        $query .= "                                       W4.CHAIRCD = W1.CHAIRCD) ";
        if ($model->field["RADIO"] == "1") { //基本
            $query .= " WHERE  W1.YEAR='{$model->year}' AND  ";
            $query .= "        W1.SEMESTER='{$model->semester}' AND  ";
            $query .= "        W1.BSCSEQ = {$model->bscseq} AND  ";
            $query .= "        W1.DAYCD BETWEEN '2' AND '7' AND  ";
        } else { //通常
            $query .= " WHERE  W1.EXECUTEDATE BETWEEN '{$model->sdate}' AND '{$model->edate}' AND  ";
            $query .= "        DAYOFWEEK(W1.EXECUTEDATE) BETWEEN 2 AND 7 AND  ";
            //テストのみ出力
            if ($model->field["TEST_CHECK"] == "on") {
                if ($model->Properties["useProficiency"] == '1') {
                    $query .= "    W1.DATADIV IN ('2','3') AND  ";
                } else {
                    $query .= "    W1.DATADIV IN ('2') AND  ";
                }
            }
        }
        $query .= "            W1.PERIODCD NOT IN (SELECT PERIODCD FROM PERIOD) ) ";
        return $query;
    }

    //職員・施設別csv
    public function getTeacherFacilityCsv($model)
    {
        $query  = "";

        //時間割
        $query .= knjb060Query::getJikanwari($model);

        //講座クラス
        $query .= " ,CHAIR_CLS AS ( ";
        $query .= "     SELECT W2.CHAIRCD,MIN(W1.TRGTGRADE||W1.TRGTCLASS) AS TRGTGRCL ";
        $query .= "     FROM   CHAIR_CLS_DAT W1 ";
        $query .= "            LEFT JOIN CHAIR_DAT W2 ON W2.YEAR=W1.YEAR AND  ";
        $query .= "                                      W2.SEMESTER=W1.SEMESTER AND  ";
        $query .= "                                      W1.GROUPCD=W2.GROUPCD AND  ";
        $query .= "                                     (W1.CHAIRCD='0000000' OR W1.CHAIRCD=W2.CHAIRCD) ";
        $query .= "     WHERE  W1.YEAR = '{$model->year}' AND ";
        $query .= "            W1.SEMESTER  = '{$model->semester}' ";
        $query .= "     GROUP BY W2.CHAIRCD ) ";

        //職員別
        if ($model->field["KUBUN"] == 1) {
            //所属
            $query .= " ,SECTION AS ( ";
            $query .= "     SELECT W2.SECTIONCD,W2.STAFFCD,W2.STAFFNAME,W1.SECTIONNAME ";
            $query .= "     FROM   V_SECTION_MST W1,V_STAFF_MST W2 ";
            $query .= "     WHERE  W1.YEAR='{$model->year}' AND ";
            $query .= "            W1.SECTIONCD BETWEEN '{$model->field["SECTION_CD_NAME1"]}' AND '{$model->field["SECTION_CD_NAME2"]}' AND ";
            $query .= "            W2.YEAR=W1.YEAR AND ";
            $query .= "            W2.SECTIONCD=W1.SECTIONCD ";
            $query .= "             ) ";
        }
        //施設別
        if ($model->field["KUBUN"] == 4) {
            //施設
            $query .= " ,FACILITY AS ( ";
            $query .= "     SELECT W1.FACCD, W1.FACILITYNAME ";
            $query .= "     FROM   V_FACILITY_MST W1 ";
            $query .= "     WHERE  W1.YEAR='{$model->year}' AND ";
            $query .= "            W1.FACCD BETWEEN '{$model->field["FACCD_NAME1"]}' AND '{$model->field["FACCD_NAME2"]}' ";
            $query .= "             ) ";
        }

        //必履修記号
        $query .= knjb060Query::getRequireMark($model);

        //メイン
        $query .= " SELECT T1.DAYCD ";
        $query .= "        ,f_period(T1.PERIODCD) as PERIODCD ";
        $query .= "        ,T1.CHAIRCD ";
        $query .= "        ,T1.FACCD ";
        $query .= "        ,T1.STAFFCD ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "        ,T1.CLASSCD || T1.SCHOOL_KIND || T1.CURRICULUM_CD || T1.SUBCLASSCD AS SUBCLASSCD ";
        } else {
            $query .= "        ,T1.SUBCLASSCD ";
        }
        $query .= "        ,T1.GROUPCD ";
        $query .= "        ,L3.GRADE ";
        $query .= "        ,L3.HR_CLASS ";
        $query .= "        ,CASE WHEN T1.GROUPCD > '0000' THEN L3.HR_NAME||'*' ";
        $query .= "              ELSE L3.HR_NAME END AS TARGETCLASS ";
        //職員別
        if ($model->field["KUBUN"] == 1) {
            $query .= "    ,T3.SECTIONCD,T3.SECTIONNAME ";
            if ($model->field["STAFF_ABBV_CHECK"] == "on") {
                $query .= "    ,VALUE(L4.STAFFNAME_SHOW,'') AS STAFFNAME ";
            } else {
                $query .= "    ,VALUE(L4.STAFFNAME,'') AS STAFFNAME ";
            }
            $query .= "    ,CASE WHEN T1.TESTCD IS NOT NULL AND T1.DATADIV = '2' THEN VALUE(L9.TESTITEMNAME,'　') ";
            if ($model->Properties["useProficiency"] == '1') {
                $query .= "      WHEN T1.TESTCD IS NOT NULL AND T1.DATADIV = '3' THEN VALUE(P9.PROFICIENCYNAME1,'　') ";
            }
            $query .= "          ELSE VALUE(L5.FACILITYNAME,'　') END AS FACILITYNAME ";
        }
        //施設別
        if ($model->field["KUBUN"] == 4) {
            $query .= "    ,VALUE(L5.FACILITYNAME,'') AS FACILITYNAME ";
            $query .= "    ,CASE WHEN T1.TESTCD IS NOT NULL AND T1.DATADIV = '2' THEN VALUE(L9.TESTITEMNAME,'　') ";
            if ($model->Properties["useProficiency"] == '1') {
                $query .= "      WHEN T1.TESTCD IS NOT NULL AND T1.DATADIV = '3' THEN VALUE(P9.PROFICIENCYNAME1,'　') ";
            }
            if ($model->field["STAFF_ABBV_CHECK"] == "on") {
                $query .= "          ELSE VALUE(L4.STAFFNAME_SHOW,'　') END AS STAFFNAME ";
            } else {
                $query .= "          ELSE VALUE(L4.STAFFNAME,'　') END AS STAFFNAME ";
            }
        }
        if ($model->field["SUBCLASS_ABBV_CHECK"] == "on") {
            if ($model->field["NO_REQUIRE_FLG_CHECK"] == "on") {
                if ($model->field["SUBCLASS_CHAIR_DIV"] == "1") {
                    $query .= "        ,VALUE(L6.SUBCLASSABBV,'') AS SUBCLASSNAME ";
                } else {
                    $query .= "        ,VALUE(T1.CHAIRABBV,'') AS SUBCLASSNAME ";
                }
            } else {
                if ($model->field["SUBCLASS_CHAIR_DIV"] == "1") {
                    $query .= "        ,VALUE(R1.NAMESPARE1,'') || VALUE(L6.SUBCLASSABBV,'') AS SUBCLASSNAME ";
                } else {
                    $query .= "        ,VALUE(R1.NAMESPARE1,'') || VALUE(T1.CHAIRABBV,'') AS SUBCLASSNAME ";
                }
            }
        } else {
            if ($model->field["NO_REQUIRE_FLG_CHECK"] == "on") {
                if ($model->field["SUBCLASS_CHAIR_DIV"] == "1") {
                    $query .= "        ,VALUE(L6.SUBCLASSNAME,'') AS SUBCLASSNAME ";
                } else {
                    $query .= "        ,VALUE(T1.CHAIRNAME,'') AS SUBCLASSNAME ";
                }
            } else {
                if ($model->field["SUBCLASS_CHAIR_DIV"] == "1") {
                    $query .= "        ,VALUE(R1.NAMESPARE1,'') || VALUE(L6.SUBCLASSNAME,'') AS SUBCLASSNAME ";
                } else {
                    $query .= "        ,VALUE(R1.NAMESPARE1,'') || VALUE(T1.CHAIRNAME,'') AS SUBCLASSNAME ";
                }
            }
        }
        $query .= "        ,L7.ABBV1 AS PERIOD_NAME ";
        if ($model->field["NO_REQUIRE_FLG_CHECK"] == "on") {
            $query .= "        ,VALUE(L8.GROUPNAME,'') AS GROUPNAME ";
        } else {
            $query .= "        ,VALUE(R1.NAMESPARE1,'') || VALUE(L8.GROUPNAME,'') AS GROUPNAME ";
        }
        $query .= " FROM   SCH_DAT T1 ";
        $query .= "        LEFT JOIN STAFF_MST L4 ON L4.STAFFCD = T1.STAFFCD ";
        $query .= "        LEFT JOIN FACILITY_MST L5 ON L5.FACCD = T1.FACCD ";
        $query .= "        LEFT JOIN SUBCLASS_MST L6 ON L6.SUBCLASSCD = T1.SUBCLASSCD ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "             AND L6.CLASSCD = T1.CLASSCD ";
            $query .= "             AND L6.SCHOOL_KIND = T1.SCHOOL_KIND ";
            $query .= "             AND L6.CURRICULUM_CD = T1.CURRICULUM_CD ";
        }
        $query .= "        LEFT JOIN NAME_MST L7 ON L7.NAMECD1='B001' AND L7.NAMECD2 = T1.PERIODCD ";
        $query .= "        LEFT JOIN V_ELECTCLASS_MST L8 ON L8.YEAR='{$model->year}' AND L8.GROUPCD = T1.GROUPCD ";
        if ($model->Properties["useTestCountflg"] == "TESTITEM_MST_COUNTFLG_NEW_SDIV") {
            $query .= "                      LEFT JOIN TESTITEM_MST_COUNTFLG_NEW_SDIV L9 ON L9.YEAR='{$model->year}' ";
            $query .= "                          AND L9.SEMESTER='{$model->semester}'";
            $query .= "                          AND L9.TESTKINDCD||L9.TESTITEMCD =T1.TESTCD ";
            $query .= "                          AND L9.SCORE_DIV = '01' ";//固定
        } elseif ($model->Properties["useTestCountflg"] == "TESTITEM_MST_COUNTFLG_NEW") {
            $query .= "                      LEFT JOIN TESTITEM_MST_COUNTFLG_NEW L9 ON L9.YEAR='{$model->year}' ";
            $query .= "                          AND L9.SEMESTER='{$model->semester}'";
            $query .= "                          AND L9.TESTKINDCD||L9.TESTITEMCD =T1.TESTCD ";
        } else {
            $query .= "                      LEFT JOIN TESTITEM_MST_COUNTFLG L9 ON L9.YEAR='{$model->year}' ";
            $query .= "                          AND L9.TESTKINDCD||L9.TESTITEMCD =T1.TESTCD ";
        }
        if ($model->Properties["useProficiency"] == '1') {
            $query .= "        LEFT JOIN PROFICIENCY_MST P9 ON P9.PROFICIENCYDIV = '02' ";//固定
            $query .= "                                    AND P9.PROFICIENCYCD  = T1.TESTCD ";
        }
        $query .= "        LEFT JOIN REQUIRE_MARK R1 ON R1.CHAIRCD=T1.CHAIRCD ";
        $query .= "        ,CHAIR_CLS T2 ";
        $query .= "        LEFT JOIN SCHREG_REGD_HDAT L3 ON L3.YEAR='{$model->year}' AND  ";
        $query .= "                                         L3.SEMESTER='{$model->semester}' AND  ";
        $query .= "                                         L3.GRADE||L3.HR_CLASS=T2.TRGTGRCL  ";
        //職員別
        if ($model->field["KUBUN"] == 1) {
            $query .= "    ,SECTION T3 ";
            $query .= " WHERE  T1.CHAIRCD=T2.CHAIRCD AND ";
            $query .= "        T1.STAFFCD=T3.STAFFCD ";
            $query .= " ORDER BY T3.SECTIONCD,T1.STAFFCD, ";
            $query .= "          2,T1.DAYCD, ";
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "      T1.CLASSCD || T1.SCHOOL_KIND || T1.CURRICULUM_CD || T1.SUBCLASSCD, ";
            } else {
                $query .= "      T1.SUBCLASSCD, ";
            }
            $query .= "          T1.FACCD ";
        }
        //施設別
        if ($model->field["KUBUN"] == 4) {
            $query .= "    ,FACILITY T3 ";
            $query .= " WHERE  T1.CHAIRCD=T2.CHAIRCD AND ";
            $query .= "        T1.FACCD=T3.FACCD ";
            $query .= " ORDER BY T1.FACCD, ";
            $query .= "          2,T1.DAYCD, ";
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "      T1.CLASSCD || T1.SCHOOL_KIND || T1.CURRICULUM_CD || T1.SUBCLASSCD, ";
            } else {
                $query .= "      T1.SUBCLASSCD, ";
            }
            $query .= "          T1.STAFFCD ";
        }

        return $query;
    }

    //学級・生徒別csv
    public function getClassStudentCsv($model, $flg, $keycdFlg2 = "")
    {
        $query  = "";

        //時間割
        $query .= knjb060Query::getJikanwari($model);

        //学級別
        if ($model->field["KUBUN"] == 2) {
            //在籍
            $query .= " ,SCHNO AS ( ";
            $query .= "     SELECT SCHREGNO,YEAR,SEMESTER,GRADE,HR_CLASS ";
            $query .= "     FROM   SCHREG_REGD_DAT ";
            $query .= "     WHERE  YEAR='{$model->year}' AND ";
            $query .= "            SEMESTER='{$model->semester}' AND ";
            $query .= "            GRADE||HR_CLASS BETWEEN '{$model->field["GRADE_HR_CLASS1"]}' AND '{$model->field["GRADE_HR_CLASS2"]}' ";
            if (strlen($model->field["COURSE"])) {
                $query .= "            AND COURSECD || MAJORCD || COURSECODE = '{$model->field["COURSE"]}' ";
            }
            $query .= "             ) ";
            //講座生徒
            $query .= " ,CHAIR_STD AS ( ";
            $query .= "     SELECT DISTINCT CHAIRCD,GRADE,HR_CLASS ";
            $query .= "     FROM   CHAIR_STD_DAT W1,SCHNO W2 ";
            $query .= "     WHERE  W1.YEAR=W2.YEAR AND  ";
            $query .= "            W1.SEMESTER=W2.SEMESTER AND  ";
            $query .= "            W1.SCHREGNO=W2.SCHREGNO ) ";
            //講座クラス
            $query .= " ,CHAIR_CLS AS ( ";
            $query .= "     SELECT W2.CHAIRCD,W1.TRGTGRADE,W1.TRGTCLASS,W1.YEAR,W1.SEMESTER ";
            $query .= "     FROM   CHAIR_CLS_DAT W1 ";
            $query .= "            LEFT JOIN CHAIR_DAT W2 ON W2.YEAR=W1.YEAR AND  ";
            $query .= "                                      W2.SEMESTER=W1.SEMESTER AND  ";
            $query .= "                                      W1.GROUPCD=W2.GROUPCD AND  ";
            $query .= "                                     (W1.CHAIRCD='0000000' OR W1.CHAIRCD=W2.CHAIRCD) ";
            $query .= "     WHERE  W1.YEAR = '{$model->year}' AND ";
            $query .= "            W1.SEMESTER  = '{$model->semester}' AND ";
            if ($flg == 1) { //学級別時間割表
                $query .= "        W1.TRGTGRADE||W1.TRGTCLASS BETWEEN '{$model->field["GRADE_HR_CLASS1"]}' AND '{$model->field["GRADE_HR_CLASS2"]}' ) ";
            }
            if ($flg == 2) { //同時展開担当者一覧
                $query .= "        W1.TRGTGRADE||W1.TRGTCLASS = '{$keycdFlg2}' ) ";
            }
        }
        //生徒別
        if ($model->field["KUBUN"] == 3) {
            //在籍
            $query .= " ,SCHNO AS ( ";
            $query .= "     SELECT SCHREGNO,YEAR,SEMESTER,GRADE,HR_CLASS,ATTENDNO ";
            $query .= "     FROM   SCHREG_REGD_DAT ";
            $query .= "     WHERE  YEAR='{$model->year}' AND ";
            $query .= "            SEMESTER='{$model->semester}' AND ";
            if ($flg == 1) { //生徒別時間割表
                $query .= "            GRADE||HR_CLASS BETWEEN '{$model->field["GRADE_HR_CLASS3"]}' AND '{$model->field["GRADE_HR_CLASS4"]}' ";
            }
            if ($flg == 2) { //同時展開担当者一覧
                $query .= "            SCHREGNO = '{$keycdFlg2}' ";
            }
            $query .= "             ) ";
            //講座生徒
            $query .= " ,CHAIR_STD AS ( ";
            $query .= "     SELECT W1.CHAIRCD, W1.SCHREGNO, W1.APPDATE, W1.APPENDDATE ";
            $query .= "     FROM   CHAIR_STD_DAT W1,SCHNO W2 ";
            $query .= "     WHERE  W1.YEAR=W2.YEAR AND  ";
            $query .= "            W1.SEMESTER=W2.SEMESTER AND  ";
            $query .= "            W1.SCHREGNO=W2.SCHREGNO ) ";
            $query .= " ,MAX_STD AS ( ";
            $query .= "     SELECT CHAIRCD, SCHREGNO, MAX(APPDATE) AS APPDATE ";
            $query .= "     FROM   CHAIR_STD ";
            $query .= "     GROUP BY CHAIRCD, SCHREGNO ) ";
        }

        //必履修記号
        $query .= knjb060Query::getRequireMark($model);

        //メイン
        $query .= " SELECT T1.DAYCD ";
        $query .= "        ,f_period(T1.PERIODCD) as PERIODCD ";
        $query .= "        ,T1.CHAIRCD ";
        $query .= "        ,T1.FACCD ";
        $query .= "        ,T1.STAFFCD ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "        ,T1.CLASSCD || T1.SCHOOL_KIND || T1.CURRICULUM_CD || T1.SUBCLASSCD AS SUBCLASSCD ";
        } else {
            $query .= "        ,T1.SUBCLASSCD ";
        }
        $query .= "        ,T1.GROUPCD ";
        //学級別
        if ($model->field["KUBUN"] == 2) {
            $query .= "    ,L3.GRADE ";
            $query .= "    ,L3.HR_CLASS ";
        }
        //生徒別
        if ($model->field["KUBUN"] == 3) {
            $query .= "    ,T2.SCHREGNO ";
            $query .= "    ,L1.GRADE ";
            $query .= "    ,L1.HR_CLASS ";
            $query .= "    ,L1.ATTENDNO ";
            $query .= "    ,L2.NAME ";
        }
        $query .= "        ,L3.HR_NAME AS TARGETCLASS ";
        if ($model->field["STAFF_ABBV_CHECK"] == "on") {
            $query .= "        ,VALUE(L4.STAFFNAME_SHOW,'') AS STAFFNAME ";
        } else {
            $query .= "        ,VALUE(L4.STAFFNAME,'') AS STAFFNAME ";
        }
        $query .= "        ,CASE WHEN T1.TESTCD IS NOT NULL AND T1.DATADIV = '2' THEN VALUE(L9.TESTITEMNAME,'　') ";
        if ($model->Properties["useProficiency"] == '1') {
            $query .= "          WHEN T1.TESTCD IS NOT NULL AND T1.DATADIV = '3' THEN VALUE(P9.PROFICIENCYNAME1,'　') ";
        }
        $query .= "              ELSE VALUE(L5.FACILITYNAME,'　') END AS FACILITYNAME ";
        if ($model->field["SUBCLASS_ABBV_CHECK"] == "on") {
            if ($model->field["NO_REQUIRE_FLG_CHECK"] == "on") {
                if ($model->field["SUBCLASS_CHAIR_DIV"] == "1") {
                    $query .= "    ,VALUE(L6.SUBCLASSABBV,'') AS SUBCLASSNAME ";
                } else {
                    $query .= "    ,VALUE(T1.CHAIRABBV,'') AS SUBCLASSNAME ";
                }
            } else {
                if ($model->field["SUBCLASS_CHAIR_DIV"] == "1") {
                    $query .= "    ,VALUE(R1.NAMESPARE1,'') || VALUE(L6.SUBCLASSABBV,'') AS SUBCLASSNAME ";
                } else {
                    $query .= "    ,VALUE(R1.NAMESPARE1,'') || VALUE(T1.CHAIRABBV,'') AS SUBCLASSNAME ";
                }
            }
        } else {
            if ($model->field["NO_REQUIRE_FLG_CHECK"] == "on") {
                if ($model->field["SUBCLASS_CHAIR_DIV"] == "1") {
                    $query .= "    ,VALUE(L6.SUBCLASSNAME,'') AS SUBCLASSNAME ";
                } else {
                    $query .= "    ,VALUE(T1.CHAIRNAME,'') AS SUBCLASSNAME ";
                }
            } else {
                if ($model->field["SUBCLASS_CHAIR_DIV"] == "1") {
                    $query .= "    ,VALUE(R1.NAMESPARE1,'') || VALUE(L6.SUBCLASSNAME,'') AS SUBCLASSNAME ";
                } else {
                    $query .= "    ,VALUE(R1.NAMESPARE1,'') || VALUE(T1.CHAIRNAME,'') AS SUBCLASSNAME ";
                }
            }
        }
        $query .= "        ,L7.ABBV1 AS PERIOD_NAME ";
        $query .= "        ,L8.GROUPNAME ";
        $query .= " FROM   SCH_DAT T1 ";
        $query .= "        LEFT JOIN STAFF_MST L4 ON L4.STAFFCD = T1.STAFFCD ";
        $query .= "        LEFT JOIN FACILITY_MST L5 ON L5.FACCD = T1.FACCD ";
        $query .= "        LEFT JOIN SUBCLASS_MST L6 ON L6.SUBCLASSCD = T1.SUBCLASSCD ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "             AND L6.CLASSCD = T1.CLASSCD ";
            $query .= "             AND L6.SCHOOL_KIND = T1.SCHOOL_KIND ";
            $query .= "             AND L6.CURRICULUM_CD = T1.CURRICULUM_CD ";
        }
        $query .= "        LEFT JOIN NAME_MST L7 ON L7.NAMECD1='B001' AND L7.NAMECD2 = T1.PERIODCD ";
        $query .= "        LEFT JOIN V_ELECTCLASS_MST L8 ON L8.YEAR='{$model->year}' AND L8.GROUPCD = T1.GROUPCD ";
        if ($model->Properties["useTestCountflg"] == "TESTITEM_MST_COUNTFLG_NEW_SDIV") {
            $query .= "                      LEFT JOIN TESTITEM_MST_COUNTFLG_NEW_SDIV L9 ON L9.YEAR='{$model->year}' ";
            $query .= "                          AND L9.SEMESTER='{$model->semester}'";
            $query .= "                          AND L9.TESTKINDCD||L9.TESTITEMCD =T1.TESTCD ";
            $query .= "                          AND L9.SCORE_DIV = '01' ";//固定
        } elseif ($model->Properties["useTestCountflg"] == "TESTITEM_MST_COUNTFLG_NEW") {
            $query .= "                      LEFT JOIN TESTITEM_MST_COUNTFLG_NEW L9 ON L9.YEAR='{$model->year}' ";
            $query .= "                          AND L9.SEMESTER='{$model->semester}'";
            $query .= "                          AND L9.TESTKINDCD||L9.TESTITEMCD =T1.TESTCD ";
        } else {
            $query .= "                      LEFT JOIN TESTITEM_MST_COUNTFLG L9 ON L9.YEAR='{$model->year}' ";
            $query .= "                          AND L9.TESTKINDCD||L9.TESTITEMCD =T1.TESTCD ";
        }
        if ($model->Properties["useProficiency"] == '1') {
            $query .= "        LEFT JOIN PROFICIENCY_MST P9 ON P9.PROFICIENCYDIV = '02' ";//固定
            $query .= "                                    AND P9.PROFICIENCYCD  = T1.TESTCD ";
        }
        //学級別
        if ($model->field["KUBUN"] == 2) {
            $query .= "        ,CHAIR_CLS T2 ";
            $query .= "        LEFT JOIN SCHREG_REGD_HDAT L3 ON L3.YEAR='{$model->year}' AND  ";
            $query .= "                                         L3.SEMESTER='{$model->semester}' AND  ";
            $query .= "                                         L3.GRADE=T2.TRGTGRADE AND ";
            $query .= "                                         L3.HR_CLASS=T2.TRGTCLASS  ";
            $query .= "        LEFT JOIN REQUIRE_MARK R1 ON R1.CHAIRCD=T2.CHAIRCD AND ";
            $query .= "                                     R1.GRADE=T2.TRGTGRADE AND ";
            $query .= "                                     R1.HR_CLASS=T2.TRGTCLASS  ";
            $query .= " WHERE  T1.CHAIRCD=T2.CHAIRCD ";
            $query .= "        AND (T2.CHAIRCD,T2.TRGTGRADE,T2.TRGTCLASS) IN (SELECT CHAIRCD,GRADE,HR_CLASS FROM CHAIR_STD) ";
            //同時展開担当者一覧
            if ($flg == 2) {
                $query .= "    AND T1.GROUPCD > '0000' ";
            }
            //学級別時間割表
            if ($flg == 1) {
                $query .= " ORDER BY L3.GRADE,L3.HR_CLASS, ";
                $query .= "          2,T1.DAYCD, ";
                $query .= "          T1.GROUPCD, ";
                if ($model->Properties["useCurriculumcd"] == '1') {
                    $query .= "      T1.CLASSCD || T1.SCHOOL_KIND || T1.CURRICULUM_CD || T1.SUBCLASSCD, ";
                } else {
                    $query .= "      T1.SUBCLASSCD, ";
                }
                $query .= "          T1.STAFFCD, ";
                $query .= "          T1.FACCD ";
            }
            //同時展開担当者一覧
            if ($flg == 2) {
                $query .= " ORDER BY T1.DAYCD,2, ";
                $query .= "          T1.GROUPCD, ";
                if ($model->Properties["useCurriculumcd"] == '1') {
                    $query .= "      T1.CLASSCD || T1.SCHOOL_KIND || T1.CURRICULUM_CD || T1.SUBCLASSCD, ";
                } else {
                    $query .= "      T1.SUBCLASSCD, ";
                }
                $query .= "          T1.STAFFCD ";
            }
        }
        //生徒別
        if ($model->field["KUBUN"] == 3) {
            if ($model->field["RADIO"] == "1") { //基本
                $query .= "    ,MAX_STD T2 ";
            } else { //通常
                $query .= "    ,CHAIR_STD T2 ";
            }
            $query .= "        LEFT JOIN SCHREG_BASE_MST L2 ON L2.SCHREGNO=T2.SCHREGNO ";
            $query .= "        LEFT JOIN SCHNO L1 ON L1.SCHREGNO=T2.SCHREGNO ";
            $query .= "        LEFT JOIN SCHREG_REGD_HDAT L3 ON L3.YEAR=L1.YEAR AND  ";
            $query .= "                                         L3.SEMESTER=L1.SEMESTER AND  ";
            $query .= "                                         L3.GRADE=L1.GRADE AND  ";
            $query .= "                                         L3.HR_CLASS=L1.HR_CLASS ";
            $query .= "        LEFT JOIN REQUIRE_MARK R1 ON R1.CHAIRCD=T2.CHAIRCD AND  ";
            $query .= "                                     R1.SCHREGNO=T2.SCHREGNO ";
            $query .= " WHERE  T1.CHAIRCD=T2.CHAIRCD ";
            //同時展開担当者一覧
            if ($flg == 2) {
                $query .= "    AND T1.GROUPCD > '0000' ";
            }
            //通常
            if ($model->field["RADIO"] == "2") {
                $query .= "    AND T1.EXECUTEDATE BETWEEN T2.APPDATE AND T2.APPENDDATE ";
            }
            //生徒別時間割表
            if ($flg == 1) {
                $query .= " ORDER BY L1.GRADE,L1.HR_CLASS,L1.ATTENDNO, ";
                $query .= "          2,T1.DAYCD, ";
                $query .= "          T1.GROUPCD, ";
                if ($model->Properties["useCurriculumcd"] == '1') {
                    $query .= "      T1.CLASSCD || T1.SCHOOL_KIND || T1.CURRICULUM_CD || T1.SUBCLASSCD, ";
                } else {
                    $query .= "      T1.SUBCLASSCD, ";
                }
                $query .= "          T1.STAFFCD, ";
                $query .= "          T1.FACCD ";
            }
            //同時展開担当者一覧
            if ($flg == 2) {
                $query .= " ORDER BY T1.DAYCD,2, ";
                $query .= "          T1.GROUPCD, ";
                if ($model->Properties["useCurriculumcd"] == '1') {
                    $query .= "      T1.CLASSCD || T1.SCHOOL_KIND || T1.CURRICULUM_CD || T1.SUBCLASSCD, ";
                } else {
                    $query .= "      T1.SUBCLASSCD, ";
                }
                $query .= "          T1.STAFFCD ";
            }
        }

        return $query;
    }

    //必履修記号csv共通
    public function getRequireMark($model)
    {
        $query  = "";
        //単位マスタ
        $query .= " , CREDIT AS ( ";
        $query .= "     SELECT ";
        $query .= "         T1.COURSECD||T1.MAJORCD||T1.COURSECODE AS COURSE, ";
        $query .= "         T1.GRADE, ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "        T1.CLASSCD, ";
            $query .= "        T1.SCHOOL_KIND, ";
            $query .= "        T1.CURRICULUM_CD, ";
        }
        $query .= "         T1.SUBCLASSCD, ";
        $query .= "         T1.REQUIRE_FLG, ";
        $query .= "         N1.NAMESPARE1 ";
        $query .= "     FROM ";
        $query .= "         CREDIT_MST T1, ";
        $query .= "         NAME_MST N1 ";
        $query .= "     WHERE ";
        $query .= "         T1.YEAR='{$model->year}' AND ";
        $query .= "         N1.NAMECD1='Z011' AND ";
        $query .= "         N1.NAMECD2=T1.REQUIRE_FLG ";
        $query .= "     ) ";
        //講座を履修している生徒の「学年・組・出席番号」の一番若い生徒を対象
        $query .= " , MIN_SCHNO AS ( ";
        $query .= "     SELECT ";
        $query .= "         T1.CHAIRCD, ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "        T3.CLASSCD, ";
            $query .= "        T3.SCHOOL_KIND, ";
            $query .= "        T3.CURRICULUM_CD, ";
        }
        $query .= "         T3.SUBCLASSCD, ";
        if ($model->field["KUBUN"] == 2) {//学級別
            $query .= "     T2.GRADE,T2.HR_CLASS, ";
        }
        if ($model->field["KUBUN"] == 3) {//生徒別
            $query .= "     T2.SCHREGNO, ";
        }
        $query .= "         MIN(T2.GRADE||T2.HR_CLASS||T2.ATTENDNO) AS MIN_NO ";
        $query .= "     FROM ";
        $query .= "         CHAIR_STD_DAT T1, ";
        $query .= "         SCHREG_REGD_DAT T2, ";
        $query .= "         CHAIR_DAT T3 ";
        $query .= "     WHERE ";
        $query .= "         T1.YEAR='{$model->year}' AND ";
        $query .= "         T1.SEMESTER='{$model->semester}' AND ";
        if ($model->field["KUBUN"] == 2) {//学級別
            $query .= "     T2.GRADE||T2.HR_CLASS BETWEEN '{$model->field["GRADE_HR_CLASS1"]}' AND '{$model->field["GRADE_HR_CLASS2"]}' AND ";
            if (strlen($model->field["COURSE"])) {
                $query .= "     T2.COURSECD || T2.MAJORCD || T2.COURSECODE = '{$model->field["COURSE"]}' AND ";
            }
        }
        if ($model->field["KUBUN"] == 3) {//生徒別
            $query .= "     T2.GRADE||T2.HR_CLASS BETWEEN '{$model->field["GRADE_HR_CLASS3"]}' AND '{$model->field["GRADE_HR_CLASS4"]}' AND ";
        }
        $query .= "         T2.YEAR=T1.YEAR AND ";
        $query .= "         T2.SEMESTER=T1.SEMESTER AND ";
        $query .= "         T2.SCHREGNO=T1.SCHREGNO AND ";
        $query .= "         T3.YEAR=T1.YEAR AND ";
        $query .= "         T3.SEMESTER=T1.SEMESTER AND ";
        $query .= "         T3.CHAIRCD=T1.CHAIRCD AND ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "         T3.CLASSCD || T3.SCHOOL_KIND || T3.CURRICULUM_CD || T3.SUBCLASSCD IN (SELECT W1.CLASSCD || W1.SCHOOL_KIND || W1.CURRICULUM_CD || W1.SUBCLASSCD FROM CREDIT W1 GROUP BY W1.CLASSCD || W1.SCHOOL_KIND || W1.CURRICULUM_CD || W1.SUBCLASSCD) ";
        } else {
            $query .= "         T3.SUBCLASSCD IN (SELECT W1.SUBCLASSCD FROM CREDIT W1 GROUP BY W1.SUBCLASSCD) ";
        }
        $query .= "     GROUP BY T1.CHAIRCD,T3.SUBCLASSCD ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "        ,T3.CLASSCD ";
            $query .= "        ,T3.SCHOOL_KIND ";
            $query .= "        ,T3.CURRICULUM_CD ";
        }
        if ($model->field["KUBUN"] == 2) {//学級別
            $query .= "     ,T2.GRADE,T2.HR_CLASS ";
        }
        if ($model->field["KUBUN"] == 3) {//生徒別
            $query .= "     ,T2.SCHREGNO ";
        }
        $query .= "     ) ";

        //サブメイン
        $query .= " , REQUIRE_MARK AS ( ";
        $query .= "     SELECT ";
        $query .= "         T1.CHAIRCD, ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "        T1.CLASSCD, ";
            $query .= "        T1.SCHOOL_KIND, ";
            $query .= "        T1.CURRICULUM_CD, ";
        }
        $query .= "         T1.SUBCLASSCD, ";
        $query .= "         T2.SCHREGNO, ";
        $query .= "         T2.GRADE, ";
        $query .= "         T2.HR_CLASS, ";
        $query .= "         T2.COURSECD||T2.MAJORCD||T2.COURSECODE AS COURSE, ";
        $query .= "         T3.REQUIRE_FLG, ";
        $query .= "         T3.NAMESPARE1 ";
        $query .= "     FROM ";
        $query .= "         MIN_SCHNO T1, ";
        $query .= "         SCHREG_REGD_DAT T2, ";
        $query .= "         CREDIT T3 ";
        $query .= "     WHERE ";
        $query .= "         T2.YEAR='{$model->year}' AND ";
        $query .= "         T2.SEMESTER='{$model->semester}' AND ";
        $query .= "         T2.GRADE||T2.HR_CLASS||T2.ATTENDNO = T1.MIN_NO AND ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "         T3.CLASSCD=T1.CLASSCD AND ";
            $query .= "         T3.SCHOOL_KIND=T1.SCHOOL_KIND AND ";
            $query .= "         T3.CURRICULUM_CD=T1.CURRICULUM_CD AND ";
        }
        $query .= "         T3.SUBCLASSCD=T1.SUBCLASSCD AND ";
        $query .= "         T3.GRADE=T2.GRADE AND ";
        $query .= "         T3.COURSE = T2.COURSECD||T2.MAJORCD||T2.COURSECODE ";
        $query .= "     ) ";

        return $query;
    }
}
