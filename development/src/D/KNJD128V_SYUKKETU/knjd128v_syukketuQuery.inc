<?php

require_once('for_php7.php');

class knjd128v_syukketuQuery extends Query {

    function getSecurityHigh() {
        $query  = " SELECT ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     MENU_HIGH_SECURITY_MST ";
        $query .= " WHERE ";
        $query .= "     PROGRAMID = 'KNJD128V_SYUKKETU' ";
        $query .= "     AND INVALID_FLG = '0' ";

        return $query;
    }

    function getSemester($model) {
        $query  = " SELECT ";
        $query .= "     SEMESTER, ";
        $query .= "     SEMESTERNAME ";
        $query .= " FROM ";
        $query .= "     SEMESTER_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND '{$model->sendAppdate}' BETWEEN SDATE AND EDATE ";
        $query .= " ORDER BY ";
        $query .= "     SEMESTER ";

        return $query;
    }

    //科目取得
    function getSubclass($model) {
        $query  = " SELECT ";
        $query .= "     SUBCLASSABBV ";
        $query .= " FROM ";
        $query .= "     SUBCLASS_MST ";
        $query .= " WHERE ";
        if ($model->Properties["useCurriculumcd"] == "1") {
            $query .= "     CLASSCD || '-' || SCHOOL_KIND || '-' ||  CURRICULUM_CD || '-' || SUBCLASSCD = '".$model->sendSubclass."' ";
        } else {
            $query .= "     SUBCLASSCD = '".$model->sendSubclass."' ";
        }

        return $query;
    }

    //講座取得
    function getChair($model) {
        $query  = " SELECT ";
        $query .= "     CHAIRNAME ";
        $query .= " FROM ";
        $query .= "     CHAIR_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR     = '".CTRL_YEAR."' AND ";
        $query .= "     SEMESTER = '".CTRL_SEMESTER."' AND ";
        $query .= "     CHAIRCD = '{$model->sendChair}' ";

        return $query;
    }

    //メインデータ作成
    function selectSemesAll($seme="") {
        $query  = " SELECT ";
        $query .= "     SEMESTER, ";
        $query .= "     SEMESTERNAME, ";
        if (!$seme) {
            $query .= "     CASE WHEN MONTH(SDATE) < 4 ";
            $query .= "          THEN MONTH(SDATE) + 12 ";
            $query .= "          ELSE MONTH(SDATE) END AS S_MONTH, ";
            $query .= "     CASE WHEN MONTH(EDATE) < 4 ";
            $query .= "          THEN MONTH(EDATE) + 12 ";
            $query .= "          ELSE MONTH(EDATE) END AS E_MONTH, ";
            $query .= "     CASE WHEN MONTH(SDATE) < 4 ";
            $query .= "          THEN MONTH(SDATE) + 12 ";
            $query .= "          ELSE MONTH(SDATE) END AS CALC_S_MONTH, ";
            $query .= "     CASE WHEN MONTH(EDATE) < 4 ";
            $query .= "          THEN MONTH(EDATE) + 12 ";
            $query .= "          ELSE MONTH(EDATE) END AS CALC_E_MONTH ";
        } else {
            $query .= "     SEMESTERNAME, ";
            $query .= "     MONTH(SDATE) AS S_MONTH, ";
            $query .= "     DAY(SDATE) AS S_DAY, ";
            $query .= "     MONTH(EDATE) AS E_MONTH, ";
            $query .= "     DAY(EDATE) AS E_DAY, ";
            $query .= "     CASE WHEN MONTH(SDATE) < 4 ";
            $query .= "          THEN MONTH(SDATE) + 12 ";
            $query .= "          ELSE MONTH(SDATE) END AS CALC_S_MONTH, ";
            $query .= "     CASE WHEN MONTH(EDATE) < 4 ";
            $query .= "          THEN MONTH(EDATE) + 12 ";
            $query .= "          ELSE MONTH(EDATE) END AS CALC_E_MONTH ";
        }
        $query .= " FROM ";
        $query .= "     SEMESTER_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        if (!$seme) {
            $query .= "     AND SEMESTER <> '9' ";
        } else {
            $query .= "     AND SEMESTER = '".$seme."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     SEMESTER ";

        return $query;
    }

    //対象月データの取得
    function selectMonthQuery($month, $model) {
        $setNameCd = "Z005";
        if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $setNameCd = "Z".SCHOOLKIND."05";
        }
        $query  = " SELECT DISTINCT ";
        $query .= "     NAMECD2, ";
        $query .= "     NAME1, ";
        $query .= "     NAMESPARE1 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' AND ";
        $query .= "     NAMECD1 = '{$setNameCd}' AND ";
        $query .= "     NAMECD2 IN (SELECT ";
        $query .= "                     CONTROL_CODE ";
        $query .= "                 FROM ";
        $query .= "                     ADMIN_CONTROL_DAT ";
        $query .= "                 WHERE ";
        $query .= "                     YEAR = '".CTRL_YEAR."' AND ";
        if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= "                     SCHOOL_KIND = '".SCHOOLKIND."' AND ";
        }
        $query .= "                     CONTROL_FLG = '2') AND ";
        $query .= "     NAMECD2 = '".sprintf('%02d', $month)."' ";
        $query .= " ORDER BY ";
        $query .= "     NAMESPARE1 ";

        return $query;
    }

    //MAX(LESSON)取得
    function getInputMaxLesson($model) {
        $query  = " WITH SCH_T AS ( ";
        $query .= "     SELECT DISTINCT ";
        $query .= "         T1.SCHREGNO ";
        $query .= "     FROM ";
        $query .= "         CHAIR_STD_DAT T1, ";
        $query .= "         SCHREG_REGD_DAT T2 ";
        $query .= "     WHERE ";
        $query .= "         T1.YEAR     = T2.YEAR AND ";
        $query .= "         T1.YEAR     = '".CTRL_YEAR."' AND ";
        $query .= "         T1.SEMESTER = T2.SEMESTER AND ";
        $query .= "         T1.SEMESTER = '".$model->field["SEMESTER"]."' AND ";
        $query .= "         T1.SCHREGNO = T2.SCHREGNO AND ";
        $query .= "         T1.CHAIRCD  = '".$model->field["CHAIRCD"]."' AND ";
        $query .= "         '{$model->sendAppdate}' BETWEEN T1.APPDATE AND T1.APPENDDATE ";
        $query .= " ) ";

        $query .= " SELECT ";
        $query .= "     MAX(LESSON) AS LESSON ";
        $query .= " FROM ";
        $query .= "     ATTEND_SUBCLASS_DAT ";
        $query .= " WHERE ";
        $query .= "     COPYCD      = '0' AND ";
        $query .= "     YEAR        = '".CTRL_YEAR."' AND ";
        $query .= "     MONTH       = '".$model->field["MONTH"]."' AND ";
        $query .= "     SEMESTER    = '".$model->field["SEMESTER"]."' AND ";
        if ($model->Properties["useCurriculumcd"] == "1") {
            $query .= "     CLASSCD || '-' || SCHOOL_KIND || '-' ||  CURRICULUM_CD || '-' || SUBCLASSCD = '".$model->field["SUBCLASSCD"]."' AND ";
        } else {
            $query .= "     CLASSCD = '".substr($model->field["SUBCLASSCD"], 0, 2)."' AND ";
            $query .= "     SUBCLASSCD = '".$model->field["SUBCLASSCD"]."' AND ";
        }
        $query .= "     SCHREGNO IN (SELECT SCHREGNO FROM SCH_T) ";

        return $query;
    }

    //学期の最終月判定
    function getMaxSemeMonthCnt($model) {
        $query  = " SELECT ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     SEMESTER_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' AND ";
        $query .= "     SEMESTER = '".$model->field["SEMESTER"]."' AND ";
        $query .= "     MONTH(EDATE) = ".intval($model->field["MONTH"])." ";

        return $query;
    }

    //学期の週数取得
    function getSyusu($seme) {
        $query  = " SELECT ";
        $query .= "     HOUTEI_SYUSU_SEMESTER".$seme." ";
        $query .= " FROM ";
        $query .= "     V_SCHOOL_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' AND ";
        $query .= "     JUGYOU_JISU_FLG = '1' ";

        return $query;
    }

    //単位数取得
    function getCredit($model) {
        $query  = " WITH MIN_SCH AS ( ";
        $query .= "     SELECT DISTINCT ";
        $query .= "         MIN(T2.GRADE || T2.HR_CLASS || T2.ATTENDNO) AS SCH ";
        $query .= "     FROM ";
        $query .= "         CHAIR_STD_DAT T1, ";
        $query .= "         SCHREG_REGD_DAT T2 ";
        $query .= "     WHERE ";
        $query .= "         T1.YEAR     = T2.YEAR AND ";
        $query .= "         T1.YEAR     = '".CTRL_YEAR."' AND ";
        $query .= "         T1.SEMESTER = T2.SEMESTER AND ";
        $query .= "         T1.SEMESTER = '".$model->field["SEMESTER"]."' AND ";
        $query .= "         T1.SCHREGNO = T2.SCHREGNO AND ";
        $query .= "         T1.CHAIRCD  = '".$model->field["CHAIRCD"]."' AND ";
        $query .= "         '{$model->sendAppdate}' BETWEEN T1.APPDATE AND T1.APPENDDATE ";
        $query .= " ), SCH_INFO AS ( ";
        $query .= "     SELECT ";
        $query .= "         YEAR, ";
        $query .= "         COURSECD, ";
        $query .= "         MAJORCD, ";
        $query .= "         GRADE, ";
        $query .= "         COURSECODE ";
        $query .= "     FROM ";
        $query .= "         SCHREG_REGD_DAT ";
        $query .= "     WHERE ";
        $query .= "         YEAR     = '".CTRL_YEAR."' AND ";
        $query .= "         SEMESTER = '".$model->field["SEMESTER"]."' AND ";
        $query .= "         GRADE || HR_CLASS || ATTENDNO IN (SELECT SCH FROM MIN_SCH) ";
        $query .= " ) ";

        $query .= " SELECT ";
        $query .= "     CREDITS ";
        $query .= " FROM ";
        $query .= "     CREDIT_MST T1, ";
        $query .= "     SCH_INFO T2 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR         = T2.YEAR AND ";
        $query .= "     T1.COURSECD     = T2.COURSECD AND ";
        $query .= "     T1.MAJORCD      = T2.MAJORCD AND ";
        $query .= "     T1.GRADE        = T2.GRADE AND ";
        $query .= "     T1.COURSECODE   = T2.COURSECODE AND ";
        if ($model->Properties["useCurriculumcd"] == "1") {
            $query .= "     CLASSCD || '-' || SCHOOL_KIND || '-' ||  CURRICULUM_CD || '-' || SUBCLASSCD = '".$model->field["SUBCLASSCD"]."' ";
        } else {
            $query .= "     SUBCLASSCD = '".$model->field["SUBCLASSCD"]."' ";
        }

        return $query;
    }

    //MAX(SUM(LESSON))取得
    function getMaxSumLesson($model) {
        $query  = " WITH SCH_T AS ( ";
        $query .= "     SELECT DISTINCT ";
        $query .= "         T1.SCHREGNO ";
        $query .= "     FROM ";
        $query .= "         CHAIR_STD_DAT T1, ";
        $query .= "         SCHREG_REGD_DAT T2 ";
        $query .= "     WHERE ";
        $query .= "         T1.YEAR     = T2.YEAR AND ";
        $query .= "         T1.YEAR     = '".CTRL_YEAR."' AND ";
        $query .= "         T1.SEMESTER = T2.SEMESTER AND ";
        $query .= "         T1.SEMESTER = '".$model->field["SEMESTER"]."' AND ";
        $query .= "         T1.SCHREGNO = T2.SCHREGNO AND ";
        $query .= "         T1.CHAIRCD  = '".$model->field["CHAIRCD"]."' AND ";
        $query .= "         '{$model->sendAppdate}' BETWEEN T1.APPDATE AND T1.APPENDDATE ";
        $query .= " ), SUM_LESSON AS ( ";
        $query .= "     SELECT ";
        $query .= "         SCHREGNO, ";
        $query .= "         SUM(LESSON) AS LESSON ";
        $query .= "     FROM ";
        $query .= "         ATTEND_SUBCLASS_DAT ";
        $query .= "     WHERE ";
        $query .= "         COPYCD      = '0' AND ";
        $query .= "         YEAR        = '".CTRL_YEAR."' AND ";
        $query .= "         SEMESTER    = '".$model->field["SEMESTER"]."' AND ";
        if ($model->Properties["useCurriculumcd"] == "1") {
            $query .= "         CLASSCD || '-' || SCHOOL_KIND || '-' ||  CURRICULUM_CD || '-' || SUBCLASSCD = '".$model->field["SUBCLASSCD"]."' AND ";
        } else {
            $query .= "         CLASSCD = '".substr($model->field["SUBCLASSCD"], 0, 2)."' AND ";
            $query .= "         SUBCLASSCD = '".$model->field["SUBCLASSCD"]."' AND ";
        }
        $query .= "         SCHREGNO IN (SELECT SCHREGNO FROM SCH_T) ";
        $query .= "     GROUP BY ";
        $query .= "         SCHREGNO ";
        $query .= " ) ";

        $query .= " SELECT ";
        $query .= "     MAX(LESSON) AS LESSON ";
        $query .= " FROM ";
        $query .= "     SUM_LESSON ";

        return $query;
    }

    //締め日の取得
    function getAppointedDay($model) {
        $query  = " SELECT ";
        $query .= "     APPOINTED_DAY ";
        $query .= " FROM ";
        $query .= "     APPOINTED_DAY_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR        = '". CTRL_YEAR ."' AND ";
        if ($model->Properties["useSchool_KindField"] == "1") {
            $query .= "     SCHOOL_KIND = '". $model->school_kind ."' AND ";
        }
        $query .= "     MONTH       = '".$model->field["MONTH"]."' AND ";
        $query .= "     SEMESTER    = '".$model->field["SEMESTER"]."' ";

        return $query;
    }

    //C001
    function getC001() {
        $query  = " SELECT ";
        $query .= "     NAMECD2 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR    = '".CTRL_YEAR."' AND ";
        $query .= "     NAMECD1 = 'C001' ";
        $query .= " ORDER BY ";
        $query .= "     NAMECD2 ";

        return $query;
    }

    //A004
    function getA004() {
        $query  = " SELECT ";
        $query .= "     NAMECD2 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR    = '".CTRL_YEAR."' AND ";
        $query .= "     NAMECD1 = 'A004' ";
        $query .= " ORDER BY ";
        $query .= "     NAMECD2 ";

        return $query;
    }

    //欠課種別取得
    function getSickDiv() {
        $query  = " SELECT ";
        $query .= "     NAMECD2 AS VALUE, ";
        $query .= "     NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR    = '".CTRL_YEAR."' AND ";
        $query .= "     NAMECD1 = 'C001' AND ";
        $query .= "     NAMECD2 IN ('4', '5', '6') ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //明細
    function selectMeisaiQuery($model) {
        $month = (sprintf('%2d', $model->field["MONTH"]) < 4 ? sprintf('%2d', $model->field["MONTH"]) + 12 : sprintf('%2d', $model->field["MONTH"]));

        $query  = " WITH SCH_INFO AS ( ";
        $query .= "     SELECT DISTINCT ";
        $query .= "         T1.SCHREGNO, ";
        $query .= "         T1.GRADE, ";
        $query .= "         T1.HR_CLASS, ";
        $query .= "         T1.ATTENDNO, ";
        $query .= "         T3.HR_NAMEABBV || '-' || T1.ATTENDNO AS HR_ATTENDNO, ";
        $query .= "         T2.NAME ";
        $query .= "     FROM ";
        $query .= "         SCHREG_REGD_DAT T1, ";
        $query .= "         SCHREG_BASE_MST T2, ";
        $query .= "         SCHREG_REGD_HDAT T3, ";
        $query .= "         CHAIR_STD_DAT T4 ";
        $query .= "     WHERE ";
        $query .= "         T1.YEAR     = T3.YEAR AND ";
        $query .= "         T1.YEAR     = T4.YEAR AND ";
        $query .= "         T1.YEAR     = '".CTRL_YEAR."' AND ";
        $query .= "         T1.SEMESTER = T3.SEMESTER AND ";
        $query .= "         T1.SEMESTER = T4.SEMESTER AND ";
        $query .= "         T1.SEMESTER = '".$model->field["SEMESTER"]."' AND ";
        $query .= "         T1.SCHREGNO = T2.SCHREGNO AND ";
        $query .= "         T1.SCHREGNO = T4.SCHREGNO AND ";
        $query .= "         T1.GRADE    = T3.GRADE AND ";
        $query .= "         T1.HR_CLASS = T3.HR_CLASS AND ";
        $query .= "         T4.CHAIRCD  = '".$model->field["CHAIRCD"]."' AND ";
        $query .= "         ".$month." BETWEEN (CASE WHEN MONTH(T4.APPDATE) < 4  THEN MONTH(T4.APPDATE) + 12 ELSE MONTH(T4.APPDATE) END) ";
        $query .= "                        AND (CASE WHEN MONTH(T4.APPENDDATE) < 4 THEN MONTH(T4.APPENDDATE) + 12 ELSE MONTH(T4.APPENDDATE) END) ";
        $query .= " ) ";

        $query .= " SELECT ";
        $query .= "     T1.*, ";
        $query .= "     T2.APPOINTED_DAY, ";
        $query .= "     T2.LESSON ";
        foreach ($model->a004 as $key => $val) {
            if ($val) {
                $query .= "     ,T2.{$val} ";
            }
        }
        foreach ($model->c001 as $key => $val) {
            if ($val) {
                $query .= "     ,T2.{$val} ";
            }
        }
        $query .= "    ,T2.SCHREGNO AS SUBCL_SCHREGNO ";

        $query .= " FROM ";
        $query .= "     SCH_INFO T1 ";
        $query .= "     LEFT JOIN ATTEND_SUBCLASS_DAT T2 ON T1.SCHREGNO     = T2.SCHREGNO AND ";
        $query .= "                                         T2.YEAR         = '".CTRL_YEAR."' AND  ";
        $query .= "                                         T2.SEMESTER     = '".$model->field["SEMESTER"]."' AND ";
        $query .= "                                         T2.MONTH        = '".$model->field["MONTH"]."' AND ";
        if ($model->Properties["useCurriculumcd"] == "1") {
            $query .= "                                         T2.CLASSCD || '-' || T2.SCHOOL_KIND || '-' ||  T2.CURRICULUM_CD || '-' || T2.SUBCLASSCD   = '".$model->field["SUBCLASSCD"]."' ";
        } else {
            $query .= "                                         T2.CLASSCD      = '".substr($model->field["SUBCLASSCD"], 0, 2)."' AND ";
            $query .= "                                         T2.SUBCLASSCD   = '".$model->field["SUBCLASSCD"]."' ";
        }
        $query .= " ORDER BY ";
        $query .= "         T1.GRADE, ";
        $query .= "         T1.HR_CLASS, ";
        $query .= "         T1.ATTENDNO, ";
        $query .= "         T1.SCHREGNO ";

        return $query;
    }

    //異動データ（退学・転学・卒業）取得
    function getIdouData($schregno, $date) {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     SCHREG_BASE_MST ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO = '".$schregno."' AND ";
        $query .= "     GRD_DIV IN ('1', '2', '3', '6') AND ";
        $query .= "     GRD_DATE < '".$date."' ";

        return $query;
    }

    //異動データ（留学・休学）取得
    function getTransferData($schregno, $date) {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     SCHREG_TRANSFER_DAT ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO = '".$schregno."' AND ";
        $query .= "     TRANSFERCD IN ('1', '2') AND ";
        $query .= "     '".$date."' BETWEEN TRANSFER_SDATE AND CASE WHEN TRANSFER_EDATE IS NULL THEN '".(CTRL_YEAR+1)."' || '-03-31' ELSE TRANSFER_EDATE END ";

        return $query;
    }

    //出欠月別累積データの更新
    function getUpdateQuery($model) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        foreach($model->field["SCHREGNO"] as $key => $schregno) {
            //更新対象データが存在するかをチェック
            $query  = " SELECT ";
            $query .= "     COUNT(*) ";
            $query .= " FROM ";
            $query .= "     ATTEND_SUBCLASS_DAT ";
            $query .= " WHERE ";
            $query .= "     COPYCD      = '0' AND ";
            $query .= "     YEAR        = '".CTRL_YEAR."' AND ";
            $query .= "     SEMESTER    = '".$model->field["SEMESTER"]."' AND ";
            $query .= "     MONTH       = '".$model->field["MONTH"]."' AND ";
            $query .= "     SCHREGNO    = '".$schregno."' AND ";
            if ($model->Properties["useCurriculumcd"] == "1") {
                $query .= "     CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD = '".$model->field["SUBCLASSCD"]."' ";
            } else {
                $query .= "     CLASSCD     = '".substr($model->field["SUBCLASSCD"], 0, 2)."' AND ";
                $query .= "     SUBCLASSCD  = '".$model->field["SUBCLASSCD"]."' ";
            }

            $flag = $db->getOne($query);
            $data = array();

            if ($flag) {
                $where  = " WHERE ";
                $where .= "     COPYCD      = '0' AND ";
                $where .= "     YEAR        = '".CTRL_YEAR."' AND ";
                $where .= "     SEMESTER    = '".$model->field["SEMESTER"]."' AND ";
                $where .= "     MONTH       = '".$model->field["MONTH"]."' AND ";
                $where .= "     SCHREGNO    = '".$schregno."' AND ";
                if ($model->Properties["useCurriculumcd"] == "1") {
                    $where .= "     CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD = '".$model->field["SUBCLASSCD"]."' ";
                } else {
                    $where .= "     CLASSCD     = '".substr($model->field["SUBCLASSCD"], 0, 2)."' AND ";
                    $where .= "     SUBCLASSCD  = '".$model->field["SUBCLASSCD"]."' ";
                }
            } else {
                //更新対象データがない時 - INSERT
                $data["COPYCD"][TEXT]     = "0";
                $data["YEAR"][TEXT]       = CTRL_YEAR;
                $data["MONTH"][TEXT]      = $model->field["MONTH"];
                $data["SEMESTER"][TEXT]   = $model->field["SEMESTER"];
                $data["SCHREGNO"][TEXT]   = $schregno;
                if ($model->Properties["useCurriculumcd"] == "1") {
                    list($classCd, $schoolKind, $curriculumCd, $subclassCD) = preg_split("/-/", $model->field["SUBCLASSCD"]);
                    $data["CLASSCD"][TEXT]       = $classCd;
                    $data["SCHOOL_KIND"][TEXT]   = $schoolKind;
                    $data["CURRICULUM_CD"][TEXT] = $curriculumCd;
                    $data["SUBCLASSCD"][TEXT]    = $subclassCD;
                } else {
                    $data["CLASSCD"][TEXT]    = substr($model->field["SUBCLASSCD"], 0, 2);
                    $data["SUBCLASSCD"][TEXT] = $model->field["SUBCLASSCD"];
                }
            }
            //更新データをセット
            $data["APPOINTED_DAY"][TEXT] = $model->field["APPOINTED_DAY"];     //締め日
            $data["LESSON"][NUMBER]      = ($model->field["LESSON"][$key]) ?   $model->field["LESSON"][$key] : $model->field["LESSON_SET"];    //授業日数

            foreach ($model->a004 as $keyA004 => $val) {
                if ($val) {
                    $data[$val][NUMBER]     = ($model->field[$val][$key]) ?  $model->field[$val][$key] : 0;   //休学日数
                }
            }
            foreach ($model->c001 as $keyC001 => $val) {
                if ($val) {
                    $data[$val][NUMBER]     = ($model->field[$val][$key]) ?  $model->field[$val][$key] : 0;   //休学日数
                }
            }
            $data["REGISTERCD"][TEXT]    = STAFFCD ;                                 //登録者コード
            $data["UPDATED"][FUNC]       = "sysdate()";                              //更新日付

            //ATTEND_SEMES_DATチェック
            $query  = " SELECT ";
            $query .= "     COUNT(*) ";
            $query .= " FROM ";
            $query .= "     ATTEND_SEMES_DAT ";
            $query .= " WHERE ";
            $query .= "     COPYCD      = '0' AND ";
            $query .= "     YEAR        = '".CTRL_YEAR."' AND ";
            $query .= "     MONTH       = '".$model->field["MONTH"]."' AND ";
            $query .= "     SEMESTER    = '".$model->field["SEMESTER"]."' AND ";
            $query .= "     SCHREGNO    = '".$schregno."' ";

            $flg2 = $db->getOne($query);
            $data2 = array();

            //ATTEND_SEMES_DATセット
            $data2["COPYCD"][TEXT]          = "0";
            $data2["YEAR"][TEXT]            = CTRL_YEAR;
            $data2["MONTH"][TEXT]           = $model->field["MONTH"];
            $data2["SEMESTER"][TEXT]        = $model->field["SEMESTER"];
            $data2["SCHREGNO"][TEXT]        = $schregno;
            $data2["APPOINTED_DAY"][TEXT]   = $model->field["APPOINTED_DAY"];
            $data2["LESSON"][NUMBER]        = 0;
            $data2["OFFDAYS"][NUMBER]       = 0;
            $data2["ABSENT"][NUMBER]        = 0;
            $data2["SUSPEND"][NUMBER]       = 0;
            if ($model->Properties["useKekkaJisu"] == "true") {
                $data["KEKKA_JISU"][NUMBER] = 0; //使用しない
            }
            if ($model->Properties["useKekka"] == "true") {
                $data["KEKKA"][NUMBER]      = 0; //使用しない
            }
            if ($model->Properties["useLatedetail"] == "true") {
                $data["LATEDETAIL"][NUMBER] = 0; //使用しない
            }
            if ($model->c001["25"]) {
                $data2["KOUDOME"][NUMBER]   = 0;
            }
            if ($model->c001["19"]) {
                $data2["VIRUS"][NUMBER]     = 0;
            }
            $data2["MOURNING"][NUMBER]      = 0;
            $data2["ABROAD"][NUMBER]        = 0;
            $data2["SICK"][NUMBER]          = 0;
            $data2["NOTICE"][NUMBER]        = 0;
            $data2["NONOTICE"][NUMBER]      = 0;
            $data2["LATE"][NUMBER]          = 0;
            $data2["EARLY"][NUMBER]         = 0;
            $data2["REGISTERCD"][TEXT]      = STAFFCD;
            $data2["UPDATED"][FUNC]         = "SYSDATE()";

            if ("" != $model->field["APPOINTED_DAY"]) {
                //更新対象データがある時 - UPDATE
                if($flag) {
                    $query = Query::updateSQL($data, "ATTEND_SUBCLASS_DAT", $where);
                }
                //更新対象データがない時 - INSERT
                else {
                    $query = Query::insertSQL($data, "ATTEND_SUBCLASS_DAT");
                }

                //ATTEND_SEMES_DAT追加
                if(!$flg2) {
                    $query2 = Query::insertSQL($data2, "ATTEND_SEMES_DAT");
                    $db->query($query2);
                }
            } else {
                if ($flag) {
                    $query = "DELETE FROM ATTEND_SUBCLASS_DAT ".$where;
                }
            }
            $db->query($query);
        }

        $db->commit();
        Query::dbCheckIn($db);
        return true;
    }
}
?>
