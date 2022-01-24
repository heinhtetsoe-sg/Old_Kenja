<?php

require_once('for_php7.php');

// kanji=漢字
// $Id: knjd501kQuery.inc 57471 2017-12-13 07:53:28Z yamashiro $
class knjd501kQuery extends Query
{
    
    //処理年度より学年を取得
    function get_grade_data()
    {
        $query  = "SELECT DISTINCT GRADE, CAST(SMALLINT(GRADE) AS CHAR) || '学年' AS SHOW_GRADE";
        $query .= " FROM SCHREG_REGD_HDAT";
        $query .= " WHERE YEAR = '".CTRL_YEAR."'";
        return $query;
    }

    //処理年度より学期を取得
    function get_semester_data()
    {
        $query  = "SELECT SEMESTER, SEMESTERNAME";
        $query .= " FROM SEMESTER_MST";
        $query .= " WHERE YEAR = '".CTRL_YEAR."'";
        $query .= "       AND SEMESTER < '3'";
        return $query;
    }

    //処理年度より科目を取得
    function get_subclass_data($model)
    {
        $query  = "WITH AUTH_TABLE AS ( ";
        $query .= "SELECT ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     T3.CLASSCD, ";
            $query .= "     T3.SCHOOL_KIND, ";
            $query .= "     T3.CURRICULUM_CD, ";
        }
        $query .= "    T3.SUBCLASSCD, ";
        $query .= "    T1.YEAR, ";
        $query .= "    T2.SCHREGNO ";
        $query .= "FROM ";
        $query .= "    CHAIR_STF_DAT T1, ";
        $query .= "    CHAIR_STD_DAT T2, ";
        $query .= "    CHAIR_DAT T3 ";
        $query .= "WHERE ";
        $query .= "    T1.YEAR = '".CTRL_YEAR."' ";
        $query .= "    AND T1.SEMESTER = '".$model->semes."' ";
        $query .= "    AND T1.YEAR = T2.YEAR ";
        $query .= "    AND T1.YEAR = T3.YEAR ";
        $query .= "    AND T1.SEMESTER = T2.SEMESTER ";
        $query .= "    AND T1.SEMESTER = T3.SEMESTER ";
        $query .= "    AND T1.CHAIRCD = T2.CHAIRCD ";
        $query .= "    AND T1.CHAIRCD = T3.CHAIRCD ";
        $query .= "    AND T1.STAFFCD = '".STAFFCD."'";
        $query .= "GROUP BY ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     T3.CLASSCD, ";
            $query .= "     T3.SCHOOL_KIND, ";
            $query .= "     T3.CURRICULUM_CD, ";
        }
        $query .= "    T3.SUBCLASSCD, ";
        $query .= "    T1.YEAR, ";
        $query .= "    T2.SCHREGNO ";
        $query .= ") ";
        $query .= "SELECT DISTINCT";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD AS VALUE, ";
            $query .= "     T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD || ':' || T2.SUBCLASSABBV AS LABEL ";
        } else {
            $query .= "    T1.SUBCLASSCD AS VALUE,";
            $query .= "    T2.SUBCLASSABBV AS LABEL";
        }
        $query .= " FROM";
        $query .= "     KIN_RECORD_DAT T1";
        if(AUTHORITY == DEF_REFER_RESTRICT || AUTHORITY == DEF_UPDATE_RESTRICT){
            $query .= "     INNER JOIN AUTH_TABLE T3 ON T1.SUBCLASSCD = T3.SUBCLASSCD  ";
            //教育課程対応
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "                             AND T1.CLASSCD       = T3.CLASSCD ";
                $query .= "                             AND T1.SCHOOL_KIND   = T3.SCHOOL_KIND ";
                $query .= "                             AND T1.CURRICULUM_CD = T3.CURRICULUM_CD ";
            }
            $query .= "                             AND T1.YEAR=T3.YEAR  ";
            $query .= "                             AND T1.SCHREGNO=T3.SCHREGNO ";
        }
        $query .= "     LEFT JOIN SUBCLASS_MST T2 ON T1.SUBCLASSCD = T2.SUBCLASSCD";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "                             AND T1.CLASSCD       = T2.CLASSCD ";
            $query .= "                             AND T1.SCHOOL_KIND   = T2.SCHOOL_KIND ";
            $query .= "                             AND T1.CURRICULUM_CD = T2.CURRICULUM_CD ";
        }
        $query .= " WHERE";
        $query .= "     T1.YEAR = '".CTRL_YEAR."'";
        $query .= "     AND SUBSTR(T1.SUBCLASSCD,1,2) in ('01','02','03','04','05','06','07','08','09')";
        $query .= "     AND SUBSTR(T1.SUBCLASSCD,1,2) || T1.SCHOOL_KIND NOT IN (SELECT CLASSCD || SCHOOL_KIND FROM CLASS_MST WHERE ELECTDIV = '1')";
        $query .= " ORDER BY";
        $query .= "    VALUE ";

        return $query;
    }

    //講座テストデータを取得
    function get_testcnt($model, $seme)
    {
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $subclass_array = array();
            $subclass_array = explode("-", $model->subcd);
        }
        $query  = "WITH CHR_TEST_TABLE_SUB AS ( ";
        $query .= "SELECT ";
        $query .= "    T1.CHAIRCD, ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     T2.CLASSCD, ";
            $query .= "     T2.SCHOOL_KIND, ";
            $query .= "     T2.CURRICULUM_CD, ";
        }
        $query .= "    T2.SUBCLASSCD, ";
        $query .= "    T1.TESTKINDCD, ";
        $query .= "    T1.YEAR, ";
        $query .= "    T1.SEMESTER ";
        $query .= "FROM ";
        $query .= "    SCH_CHR_TEST T1, ";
        $query .= "    CHAIR_DAT T2 ";
        $query .= "WHERE ";
        $query .= "    T1.YEAR = '".CTRL_YEAR."' ";
        $query .= "    AND T1.SEMESTER = '".$seme."' ";
        $query .= "    AND T2.YEAR = T1.YEAR ";
        $query .= "    AND T2.SEMESTER = T1.SEMESTER ";
        $query .= "    AND T2.CHAIRCD = T1.CHAIRCD ";
        $query .= "    AND T2.CHAIRCD = T1.CHAIRCD ";
        if ($model->subcd == "0") {
            $query .= "    AND SUBSTR(T2.SUBCLASSCD,1,2) <= '09' ";
        } else {
            //教育課程対応
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "    AND T2.CLASSCD       = '".$subclass_array[0]."' ";
                $query .= "    AND T2.SCHOOL_KIND   = '".$subclass_array[1]."' ";
                $query .= "    AND T2.CURRICULUM_CD = '".$subclass_array[2]."' ";
                $query .= "    AND T2.SUBCLASSCD    = '".$subclass_array[3]."' ";
            } else {
                $query .= "    AND T2.SUBCLASSCD = '".$model->subcd."' ";
            }
        }
        $query .= "GROUP BY ";
        $query .= "    T1.CHAIRCD, ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     T2.CLASSCD, ";
            $query .= "     T2.SCHOOL_KIND, ";
            $query .= "     T2.CURRICULUM_CD, ";
        }
        $query .= "    T2.SUBCLASSCD, ";
        $query .= "    T1.TESTKINDCD, ";
        $query .= "    T1.YEAR, ";
        $query .= "    T1.SEMESTER ";
        $query .= ") ";
        $query .= "SELECT ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "    CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD AS SUBCD, ";
        } else {
            $query .= "    SUBCLASSCD AS SUBCD, ";
        }
        $query .= "    SEMESTER AS SEMES, ";
        $query .= "    MAX(SMALLINT(TESTKINDCD)) AS MAXTEST, ";
        $query .= "    MIN(SMALLINT(TESTKINDCD)) AS MINTEST ";
        $query .= "FROM ";
        $query .= "    CHR_TEST_TABLE_SUB ";
        $query .= "GROUP BY ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     CLASSCD, ";
            $query .= "     SCHOOL_KIND, ";
            $query .= "     CURRICULUM_CD, ";
        }
        $query .= "    SUBCLASSCD, ";
        $query .= "    SEMESTER ";

        return $query;
    }

    //メインデータ
    function get_mainData($model, $subchair, $whre, $name, $last)
    {
        $query  = " ".$name ;
        $query .= "SELECT ";
        $query .= "    T1.SCHREGNO, ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     T2.CLASSCD, ";
            $query .= "     T2.SCHOOL_KIND, ";
            $query .= "     T2.CURRICULUM_CD, ";
        }
        $query .= "    T2.SUBCLASSCD, ";
        $query .= "    L1.HR_NAMEABBV || '-' || T1.ATTENDNO AS HR_SHOW, ";
        $query .= "    L2.NAME_SHOW, ";
        $query .= "    L3.SUBCLASSABBV, ";
        $query .= "    CASE WHEN T2.SEM1_INTER_REC_DI IS NULL THEN CAST(T2.SEM1_INTER_REC AS CHAR(3)) ";
        $query .= "         ELSE T2.SEM1_INTER_REC_DI END AS SEM1_INTER_REC, ";
        $query .= "    CASE WHEN T2.SEM1_TERM_REC_DI IS NULL THEN CAST(T2.SEM1_TERM_REC AS CHAR(3)) ";
        $query .= "         ELSE T2.SEM1_TERM_REC_DI END AS SEM1_TERM_REC, ";
        $query .= "    T2.SEM1_REC,T2.SEM1_REC_FLG, ";
        $query .= "    CASE WHEN T2.SEM2_INTER_REC_DI IS NULL THEN CAST(T2.SEM2_INTER_REC AS CHAR(3)) ";
        $query .= "         ELSE T2.SEM2_INTER_REC_DI END AS SEM2_INTER_REC, ";
        $query .= "    CASE WHEN T2.SEM2_TERM_REC_DI IS NULL THEN CAST(T2.SEM2_TERM_REC AS CHAR(3)) ";
        $query .= "         ELSE T2.SEM2_TERM_REC_DI END AS SEM2_TERM_REC, ";
        $query .= "    T2.SEM2_REC,T2.SEM2_REC_FLG, ";
        $query .= "    CASE WHEN T2.SEM3_TERM_REC_DI IS NULL THEN CAST(T2.SEM3_TERM_REC AS CHAR(3)) ";
        $query .= "         ELSE T2.SEM3_TERM_REC_DI END AS SEM3_TERM_REC, ";
        $query .= "    T2.SEM3_REC,T2.SEM3_REC_FLG, ";
        $query .= "    T2.GRADE_RECORD,T2.GRADE_ASSESS, ";
        $query .= "    T2.GRADE3_RELAASSESS_5STEP,T2.GRADE3_RELAASSESS_10STEP ";
        $query .= "FROM ";
        $query .= "    SCHREG_REGD_DAT T1 ";
        $query .= "    LEFT JOIN SCHREG_REGD_HDAT L1 ON L1.YEAR = T1.YEAR ";
        $query .= "              AND L1.SEMESTER = T1.SEMESTER ";
        $query .= "              AND L1.GRADE = T1.GRADE ";
        $query .= "              AND L1.HR_CLASS = T1.HR_CLASS ";
        $query .= "    LEFT JOIN SCHREG_BASE_MST L2 ON L2.SCHREGNO = T1.SCHREGNO, ";
        $query .= "    KIN_RECORD_DAT T2 ";
        if(AUTHORITY == DEF_REFER_RESTRICT || AUTHORITY == DEF_UPDATE_RESTRICT){
            $query .= "     INNER JOIN ";
            $query .= " ( ";
            $query .= "SELECT ";
            //教育課程対応
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "     T3.CLASSCD, ";
                $query .= "     T3.SCHOOL_KIND, ";
                $query .= "     T3.CURRICULUM_CD, ";
            }
            $query .= "    T3.SUBCLASSCD, ";
            $query .= "    T1.YEAR, ";
            $query .= "    T2.SCHREGNO ";
            $query .= "FROM ";
            $query .= "    CHAIR_STF_DAT T1, ";
            $query .= "    CHAIR_STD_DAT T2, ";
            $query .= "    CHAIR_DAT T3 ";
            $query .= "WHERE ";
            $query .= "    T1.YEAR = '".CTRL_YEAR."' ";
            $query .= "    AND T1.SEMESTER = '".$model->semes."' ";
            $query .= "    AND T1.YEAR = T2.YEAR ";
            $query .= "    AND T1.YEAR = T3.YEAR ";
            $query .= "    AND T1.SEMESTER = T2.SEMESTER ";
            $query .= "    AND T1.SEMESTER = T3.SEMESTER ";
            $query .= "    AND T1.CHAIRCD = T2.CHAIRCD ";
            $query .= "    AND T1.CHAIRCD = T3.CHAIRCD ";
            $query .= "    AND T1.STAFFCD = '".STAFFCD."'";
            $query .= "GROUP BY ";
            //教育課程対応
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "     T3.CLASSCD, ";
                $query .= "     T3.SCHOOL_KIND, ";
                $query .= "     T3.CURRICULUM_CD, ";
            }
            $query .= "    T3.SUBCLASSCD, ";
            $query .= "    T1.YEAR, ";
            $query .= "    T2.SCHREGNO ";
            $query .= ") L4 ON T2.SUBCLASSCD = L4.SUBCLASSCD ";
            //教育課程対応
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "   AND T2.CLASSCD       = L4.CLASSCD ";
                $query .= "   AND T2.SCHOOL_KIND   = L4.SCHOOL_KIND ";
                $query .= "   AND T2.CURRICULUM_CD = L4.CURRICULUM_CD ";
            }
            $query .= "    AND T2.YEAR = L4.YEAR ";
            $query .= "    AND T2.SCHREGNO = L4.SCHREGNO ";
        }
        $query .= "    LEFT JOIN V_SUBCLASS_MST L3 ON L3.YEAR = T2.YEAR ";
        $query .= "              AND L3.SUBCLASSCD = T2.SUBCLASSCD ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "             AND L3.CLASSCD       = T2.CLASSCD ";
            $query .= "             AND L3.SCHOOL_KIND   = T2.SCHOOL_KIND ";
            $query .= "             AND L3.CURRICULUM_CD = T2.CURRICULUM_CD ";
        }
        $query .= "WHERE ";
        $query .= "    T1.YEAR = '".CTRL_YEAR."' ";
        $query .= "    AND T1.SEMESTER = '".$model->semes."' ";
        $query .= "    AND T1.GRADE = '".$model->grade."' ";
        $query .= "    AND T2.YEAR = T1.YEAR ";
        $query .= "    AND T2.SCHREGNO = T1.SCHREGNO ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "    AND T2.CLASSCD || '-' || T2.SCHOOL_KIND || '-' || T2.CURRICULUM_CD || '-' || T2.SUBCLASSCD = '".$subchair."' ";
        } else {
            $query .= "    AND T2.SUBCLASSCD = '".$subchair."' ";
        }
        $query .= "    AND SUBSTR(T2.SUBCLASSCD,1,2) || T2.SCHOOL_KIND NOT IN (SELECT CLASSCD || SCHOOL_KIND FROM CLASS_MST WHERE ELECTDIV = '1')";
//        $query .= "    AND ".$whre." ";
        $query .= $last;

        return $query;
    }

    //在籍期間の取得
    function getOnTheRegisterPeriod($schregno)
    {
        $query .= " WITH T01 AS( ";
        $query .= "     SELECT CASE WHEN t1.ENT_DATE IS NULL THEN t2.SDATE ELSE t1.ENT_DATE END AS ENT_DATE, ";
        $query .= "            CASE WHEN t1.GRD_DATE IS NULL THEN T2.EDATE ELSE t1.GRD_DATE END AS GRD_DATE ";
        $query .= "       FROM SCHREG_BASE_MST AS t1, ";
        $query .= "            SEMESTER_MST    AS t2 ";
        $query .= "      WHERE t2.YEAR = '".CTRL_YEAR."' ";
        $query .= "        AND t2.SEMESTER = '9' ";
        $query .= "        AND t1.SCHREGNO = '".$schregno."' ";
        $query .= " ) ";
        $query .= " SELECT COUNT(*) AS CNT ";
        $query .= "   FROM T01 ";
        $query .= "  WHERE NOT('".CTRL_DATE."' BETWEEN ENT_DATE AND GRD_DATE) ";
        return $query;
    }

    //異動者情報の取得
    function getTransferData($schregno)
    {
        $query  = " WITH T01 AS( ";
        $query .= "     SELECT DISTINCT  ";
        $query .= "            t2.TRANSFERCD, ";
        $query .= "            t2.TRANSFER_SDATE, ";
        $query .= "            CASE WHEN t2.TRANSFER_EDATE IS NULL THEN t3.EDATE ELSE t2.TRANSFER_EDATE END AS TRANSFER_EDATE ";
        $query .= "       FROM SCHREG_REGD_DAT t1, ";
        $query .= "            SCHREG_TRANSFER_DAT t2, ";
        $query .= "            SEMESTER_MST t3 ";
        $query .= "      WHERE t1.SCHREGNO = t2.SCHREGNO  ";
        $query .= "        AND t2.TRANSFERCD IN ('1', '2', '3')  ";
        $query .= "        AND t1.YEAR = T3.YEAR  ";
        $query .= "        AND t3.SEMESTER = '9'  ";
        $query .= "        AND t1.YEAR = '".CTRL_YEAR."' ";
        $query .= "        AND t1.SCHREGNO = '".$schregno."' ";
        $query .= " ) ";
        $query .= " SELECT COUNT(*) AS CNT";
        $query .= "   FROM T01 ";
        $query .= "  WHERE '".CTRL_DATE."' BETWEEN TRANSFER_SDATE AND TRANSFER_EDATE ";
        return $query;
    }

    //更新処理
    function getUpdateQuery($model, $fieldno)
    {
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $subclass_array = array();
            $subclass_array = explode(":", $fieldno[2]);
        }
        $data = array();
        $fieldname = "SEM".$model->semes;
        $data[$fieldname."_REC_FLG"][TEXT] = ($fieldno[3] == "") ? NULL : "1";          //遡及フラグ
        $data[$fieldname."_REC"][NUMBER]   = ($fieldno[3] == "") ? NULL : $fieldno[3];  //成績

        $where  = " WHERE YEAR            = '".CTRL_YEAR."' AND ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $where .= "       CLASSCD           = '".$subclass_array[0]."' AND ";
            $where .= "       SCHOOL_KIND       = '".$subclass_array[1]."' AND ";
            $where .= "       CURRICULUM_CD     = '".$subclass_array[2]."' AND ";
            $where .= "       SUBCLASSCD        = '".$subclass_array[3]."' AND ";
        } else {
            $where .= "       SUBCLASSCD      = '".$fieldno[2]."' AND ";
        }
        $where .= "       SCHREGNO        = '".$fieldno[1]."' ";

        $query = Query::updateSQL($data, "KIN_RECORD_DAT",$where);
        return $query;
    }

    //成績データ取得
    function getRecordDat($model, $fieldno)
    {
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $subclass_array = array();
            $subclass_array = explode(":", $fieldno[2]);
        }
        $query  = " SELECT * FROM KIN_RECORD_DAT ";
        $query .= " WHERE YEAR           = '".CTRL_YEAR."' ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "       AND CLASSCD           = '".$subclass_array[0]."' ";
            $query .= "       AND SCHOOL_KIND       = '".$subclass_array[1]."' ";
            $query .= "       AND CURRICULUM_CD     = '".$subclass_array[2]."' ";
            $query .= "       AND SUBCLASSCD        = '".$subclass_array[3]."' ";
        } else {
            $query .= "       AND SUBCLASSCD = '".$fieldno[2]."' ";
        }
        $query .= "       AND SCHREGNO   = '".$fieldno[1]."' ";
        return $query;
    }

    //更新処理
    function getGradeRecordUpQuery($db, $model, $fieldno, $record)
    {
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $subclass_array = array();
            $subclass_array = explode(":", $fieldno[2]);
        }
        $grade_record = "";
        $grade_assess = "";
        $reccnt = 0;
        for ($semcnt = 1; $semcnt <= 3; $semcnt++) {
            $reccnt += ($record["SEM".$semcnt."_REC"] >= "0") ? 1 : 0;
        }
        if ($reccnt >= 2) {
            if ($record["SEM1_REC"] >= "0" && $record["SEM2_REC"] >= "0" && $record["SEM3_REC"] >= "0") {
                $grade_record = round(($record["SEM1_REC"] + $record["SEM2_REC"] + $record["SEM3_REC"]) / 3);
            } else if ($record["SEM1_REC"] >="0" && $record["SEM2_REC"] >="0") {
                $grade_record = round(($record["SEM1_REC"] + $record["SEM2_REC"]) / 2);
            } else if ($record["SEM1_REC"] >="0" && $record["SEM3_REC"] >="0") {
                $grade_record = round(($record["SEM1_REC"] + $record["SEM3_REC"]) / 2);
            } else if ($record["SEM2_REC"] >="0" && $record["SEM3_REC"] >="0") {
                $grade_record = round(($record["SEM2_REC"] + $record["SEM3_REC"]) / 2);
            }

            if ($grade_record >= "0") {
                $query  = "     SELECT ";
                $query .= "         ASSESSLEVEL ";
                $query .= "     FROM ";
                $query .= "         RELATIVEASSESS_MST ";
                $query .= "     WHERE ";
                $query .= "         GRADE = '".$model->grade."' AND ";
                //教育課程対応
                if ($model->Properties["useCurriculumcd"] == '1') {
                    $query .= "         CLASSCD         = '".$subclass_array[0]."' AND ";
                    $query .= "         SCHOOL_KIND     = '".$subclass_array[1]."' AND ";
                    $query .= "         CURRICULUM_CD   = '".$subclass_array[2]."' AND ";
                    $query .= "         SUBCLASSCD      = '".$subclass_array[3]."' AND ";
                } else {
                    $query .= "         SUBCLASSCD = '".$fieldno[2]."' AND ";
                }
                $query .= "         ASSESSCD = '3' AND";    //ASSESSCDは３
                $query .= "         ".round($grade_record)." <= ASSESSHIGH AND ";
                $query .= "         ".round($grade_record)." >= ASSESSLOW ";
                $grade_assess = $db->getOne($query);
            }
        }

        $data = array();
        $data["GRADE_RECORD"][NUMBER] = ($grade_record == "") ? NULL : $grade_record;  //学年成績
        $data["GRADE_ASSESS"][TEXT]   = ($grade_assess == "") ? NULL : $grade_assess;  //学年評定

        $where  = " WHERE YEAR            = '".CTRL_YEAR."' AND ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $where .= "       CLASSCD           = '".$subclass_array[0]."' AND ";
            $where .= "       SCHOOL_KIND       = '".$subclass_array[1]."' AND ";
            $where .= "       CURRICULUM_CD     = '".$subclass_array[2]."' AND ";
            $where .= "       SUBCLASSCD        = '".$subclass_array[3]."' AND ";
        } else {
            $where .= "       SUBCLASSCD      = '".$fieldno[2]."' AND ";
        }
        $where .= "       SCHREGNO        = '".$fieldno[1]."' ";

        $query = Query::updateSQL($data, "KIN_RECORD_DAT",$where);
        return $query;
    }

}
?>
