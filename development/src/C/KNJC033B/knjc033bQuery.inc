<?php

require_once('for_php7.php');

class knjc033bQuery extends Query {
    //出欠コード取得
    function getNameMst() {
        $query  = " SELECT ";
        $query .= "     NAMECD2, ";
        $query .= "     NAME1 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR    = '".CTRL_YEAR."' AND ";
        $query .= "     NAMECD1 = 'C001' AND ";
        $query .= "     NAMECD2 IN ('4', '5', '6') ";
        $query .= " ORDER BY ";
        $query .= "     NAMECD2 ";

        return $query;
    }

    //学期情報取得
    function selectSemesAll($seme="") {
        $query  = " SELECT ";
        $query .= "     SEMESTER, ";
        if (!$seme) {
            $query .= "     SEMESTERNAME, ";
            $query .= "     CASE WHEN MONTH(SDATE) < 4 ";
            $query .= "          THEN MONTH(SDATE) + 12 ";
            $query .= "          ELSE MONTH(SDATE) END AS S_MONTH, ";
            $query .= "     CASE WHEN MONTH(EDATE) < 4 ";
            $query .= "          THEN MONTH(EDATE) + 12 ";
            $query .= "          ELSE MONTH(EDATE) END AS E_MONTH ";
        } else {
            $query .= "     SDATE, ";
            $query .= "     EDATE ";
        }
        $query .= " FROM ";
        $query .= "     SEMESTER_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        if (!$seme) {
            $query .= "     AND SEMESTER <> '9' ";
            $query .= " ORDER BY ";
            $query .= "     SEMESTER ";
        } else {
            $query .= "     AND SEMESTER = '".$seme."' ";
        }

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

    //校種取得
    function getSchoolKind($model) {
        $db = Query::dbCheckOut();
        list($grade, $hr_class) = explode("-", $model->field["GRADE_HR_CLASS"]);
        $query .= "  SELECT ";
        $query .= "    SCHOOL_KIND ";
        $query .= "  FROM ";
        $query .= "    SCHREG_REGD_HDAT SHD ";
        $query .= "    INNER JOIN ";
        $query .= "    SCHREG_REGD_GDAT SGD";
        $query .= "      ON SHD.YEAR     = SGD.YEAR ";
        $query .= "      AND SHD.GRADE   = SGD.GRADE ";
        $query .= "  WHERE ";
        $query .= "    SHD.YEAR      = '". CTRL_YEAR ."' ";
        $query .= "    AND SHD.SEMESTER  = '". CTRL_SEMESTER ."' ";
        $query .= "    AND SHD.GRADE     = '". $grade ."' ";
        $query .= "    AND SHD.HR_CLASS  = '". $hr_class ."' ";

        $school_kind = $db->getOne($query);
        Query::dbCheckIn($db);

        return $school_kind;
    }

    //クラス取得
    function getHrClass($model) {
        $query  = " SELECT ";
        $query .= "     GRADE || '-' || HR_CLASS AS VALUE, ";
        $query .= "     HR_NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_HDAT T1 ";
        $query .= " WHERE ";
        $query .= "     YEAR        = '".CTRL_YEAR."' AND ";
        $query .= "     SEMESTER    = '".$model->field["SEMESTER"]."' ";
        //参照・更新可（制限付き）
        if (AUTHORITY == DEF_REFER_RESTRICT || AUTHORITY == DEF_UPDATE_RESTRICT) {
            $query .= " AND '".STAFFCD."' IN (TR_CD1, TR_CD2, TR_CD3, SUBTR_CD1, SUBTR_CD2, SUBTR_CD3) ";
        }

        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= " AND GRADE IN (  SELECT ";
                $query .= "                     GRADE ";
                $query .= "                 FROM ";
                $query .= "                     SCHREG_REGD_GDAT S1 ";
                $query .= "                 WHERE ";
                $query .= "                     S1.YEAR         = T1.YEAR AND ";
                $query .= "                     S1.SCHOOL_KIND  IN ('".implode("','", explode(":", $model->selectSchoolKind))."') ";
                $query .= "                 ) ";
            }

        } else if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND GRADE IN (  SELECT ";
            $query .= "                     GRADE ";
            $query .= "                 FROM ";
            $query .= "                     SCHREG_REGD_GDAT S1 ";
            $query .= "                 WHERE ";
            $query .= "                     S1.YEAR         = T1.YEAR AND ";
            $query .= "                     S1.SCHOOL_KIND  = '".SCHOOLKIND."' ";
            $query .= "                 ) ";
        }

        $query .= " ORDER BY ";
        $query .= "     VALUE ";

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
            $query .= "     SCHOOL_KIND = '".$model->school_kind."' AND ";
        }
        $query .= "     MONTH       = '".$model->field["MONTH"]."' AND ";
        $query .= "     SEMESTER    = '".$model->field["SEMESTER"]."' ";

        return $query;
    }

    //異動データ（退学・転学・卒業）取得
    function getIdouData($model, $date) {
        $query  = " SELECT ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1, ";
        $query .= "     SCHREG_BASE_MST T2 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR     = '".CTRL_YEAR."' AND ";
        $query .= "     T1.SEMESTER = '".$model->field["SEMESTER"]."' AND ";
        $query .= "     T1.GRADE || '-' || T1.HR_CLASS = '".$model->field["GRADE_HR_CLASS"]."' AND ";
        $query .= "     T1.SCHREGNO = T2.SCHREGNO AND ";
        $query .= "     T2.GRD_DIV IN ('1', '2', '3', '6') AND ";
        $query .= "     T2.GRD_DATE < '".$date."' ";
        $query .= " GROUP BY ";
        $query .= "     T1.SCHREGNO ";

        return $query;
    }

    //異動データ（留学・休学）取得
    function getTransferData($model, $date) {
        $query  = " SELECT ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1, ";
        $query .= "     SCHREG_TRANSFER_DAT T2 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR     = '".CTRL_YEAR."' AND ";
        $query .= "     T1.SEMESTER = '".$model->field["SEMESTER"]."' AND ";
        $query .= "     T1.GRADE || '-' || T1.HR_CLASS = '".$model->field["GRADE_HR_CLASS"]."' AND ";
        $query .= "     T1.SCHREGNO = T2.SCHREGNO AND ";
        $query .= "     T2.TRANSFERCD IN ('1', '2') AND ";
        $query .= "     '".$date."' BETWEEN T2.TRANSFER_SDATE AND ";
        $query .= "     CASE WHEN T2.TRANSFER_EDATE IS NULL THEN '".(CTRL_YEAR+1)."' || '-03-31' ELSE T2.TRANSFER_EDATE END ";
        $query .= " GROUP BY ";
        $query .= "     T1.SCHREGNO ";

        return $query;
    }

    //科目一覧取得
    function getSubclassList($model, $sdate, $edate) {
        $query  = " SELECT DISTINCT ";
        if ($model->Properties["useCurriculumcd"] == "1") {
            $query .= "     T3.CLASSCD || '-' || T3.SCHOOL_KIND || '-' || T3.CURRICULUM_CD || '-' || T3.SUBCLASSCD AS SUBCLASS, ";
        } else {
            $query .= "     T3.SUBCLASSCD AS SUBCLASS, ";
        }
        $query .= "     L1.SUBCLASSABBV ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1, ";
        $query .= "     CHAIR_STD_DAT T2, ";
        $query .= "     CHAIR_DAT T3 ";
        $query .= "     LEFT JOIN SUBCLASS_MST L1 ";
        $query .= "              ON T3.SUBCLASSCD    = L1.SUBCLASSCD ";
        if ($model->Properties["useCurriculumcd"] == "1") {
            $query .= "             AND T3.CLASSCD       = L1.CLASSCD ";
            $query .= "             AND T3.SCHOOL_KIND   = L1.SCHOOL_KIND ";
            $query .= "             AND T3.CURRICULUM_CD = L1.CURRICULUM_CD ";
        }
        $query .= " WHERE ";
        $query .= "     T1.YEAR     = T2.YEAR AND ";
        $query .= "     T1.YEAR     = T3.YEAR AND ";
        $query .= "     T1.YEAR     = '".CTRL_YEAR."' AND ";
        $query .= "     T1.SEMESTER = T2.SEMESTER AND ";
        $query .= "     T1.SEMESTER = T3.SEMESTER AND ";
        $query .= "     T1.SEMESTER = '".$model->field["SEMESTER"]."' AND ";
        $query .= "     T1.GRADE || '-' || T1.HR_CLASS = '".$model->field["GRADE_HR_CLASS"]."' AND ";
        $query .= "     T1.SCHREGNO = T2.SCHREGNO AND ";
        $query .= "     T2.CHAIRCD  = T3.CHAIRCD AND ";
        $query .= "     (T2.APPDATE    BETWEEN '".$sdate."' AND '".$edate."' OR ";
        $query .= "      T2.APPENDDATE BETWEEN '".$sdate."' AND '".$edate."' OR ";
        $query .= "      '".$sdate."'  BETWEEN T2.APPDATE   AND T2.APPENDDATE OR ";
        $query .= "      '".$edate."'  BETWEEN T2.APPDATE   AND T2.APPENDDATE) ";
        //合併先科目は対象外
        $query .= "     AND NOT EXISTS (SELECT 'X' FROM ";
        $query .= "                         SUBCLASS_REPLACE_COMBINED_DAT COMB ";
        $query .= "                     WHERE ";
        $query .= "                         COMB.REPLACECD              = '1' ";
        $query .= "                     AND COMB.YEAR                   = T3.YEAR ";
        if ($model->Properties["useCurriculumcd"] == "1") {
            $query .= "                     AND COMB.COMBINED_CLASSCD       = T3.CLASSCD ";
            $query .= "                     AND COMB.COMBINED_SCHOOL_KIND   = T3.SCHOOL_KIND ";
            $query .= "                     AND COMB.COMBINED_CURRICULUM_CD = T3.CURRICULUM_CD ";
        }
        $query .= "                     AND COMB.COMBINED_SUBCLASSCD    = T3.SUBCLASSCD) ";
        $query .= " ORDER BY ";
        $query .= "     SUBCLASS ";

        return $query;
    }

    //生徒一覧取得
    function getSchList($model) {
        $query  = " SELECT ";
        $query .= "     T1.YEAR, ";
        $query .= "     T1.SEMESTER, ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     T1.ATTENDNO, ";
        $query .= "     L1.NAME_SHOW ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1 ";
        $query .= "     LEFT JOIN SCHREG_BASE_MST L1 ON T1.SCHREGNO = L1.SCHREGNO ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR     = '".CTRL_YEAR."' AND ";
        $query .= "     T1.SEMESTER = '".$model->field["SEMESTER"]."' AND ";
        $query .= "     T1.GRADE || '-' || T1.HR_CLASS = '".$model->field["GRADE_HR_CLASS"]."' ";
        $query .= " ORDER BY ";
        $query .= "     T1.ATTENDNO ";

        return $query;
    }

    //出欠データ取得
    function getMainQuery($model, $sdate, $edate, $subclass) {
        $query  = " WITH REDG_DAT AS ( ";
        $query .= "     SELECT ";
        $query .= "         * ";
        $query .= "     FROM ";
        $query .= "         SCHREG_REGD_DAT ";
        $query .= "     WHERE ";
        $query .= "         YEAR        = '".CTRL_YEAR."' AND ";
        $query .= "         SEMESTER    = '".$model->field["SEMESTER"]."' AND ";
        $query .= "         GRADE || '-' || HR_CLASS = '".$model->field["GRADE_HR_CLASS"]."' ";
        $query .= " ), CHR_SUBCLASS AS ( ";
        $query .= "     SELECT DISTINCT ";
        $query .= "         T1.SCHREGNO, ";
        if ($model->Properties["useCurriculumcd"] == "1") {
            $query .= "         T3.CLASSCD || '-' || T3.SCHOOL_KIND || '-' || T3.CURRICULUM_CD || '-' || T3.SUBCLASSCD AS SUBCLASS, ";
        } else {
            $query .= "         T3.SUBCLASSCD AS SUBCLASS, ";
        }
        $query .= "         '1' AS FLG ";
        $query .= "     FROM ";
        $query .= "         REDG_DAT T1, ";
        $query .= "         CHAIR_STD_DAT T2, ";
        $query .= "         CHAIR_DAT T3 ";
        $query .= "     WHERE ";
        $query .= "         T1.YEAR     = T2.YEAR AND ";
        $query .= "         T1.YEAR     = T3.YEAR AND ";
        $query .= "         T1.SEMESTER = T2.SEMESTER AND ";
        $query .= "         T1.SEMESTER = T3.SEMESTER AND ";
        $query .= "         T1.SCHREGNO = T2.SCHREGNO AND ";
        $query .= "         T2.CHAIRCD  = T3.CHAIRCD AND ";
        $query .= "         (T2.APPDATE    BETWEEN '".$sdate."' AND '".$edate."' OR ";
        $query .= "          T2.APPENDDATE BETWEEN '".$sdate."' AND '".$edate."' OR ";
        $query .= "          '".$sdate."'  BETWEEN T2.APPDATE   AND T2.APPENDDATE OR ";
        $query .= "          '".$edate."'  BETWEEN T2.APPDATE   AND T2.APPENDDATE) ";
        $query .= " ), ATTEND AS ( ";
        $query .= "     SELECT ";
        $query .= "         T1.SCHREGNO, ";
        if ($model->Properties["useCurriculumcd"] == "1") {
            $query .= "         T2.CLASSCD || '-' || T2.SCHOOL_KIND || '-' || T2.CURRICULUM_CD || '-' || T2.SUBCLASSCD AS SUBCLASS, ";
        } else {
            $query .= "         T2.SUBCLASSCD AS SUBCLASS, ";
        }
        $query .= "         T2.SICK, ";
        $query .= "         T2.NOTICE, ";
        $query .= "         T2.NONOTICE ";
        $query .= "     FROM ";
        $query .= "         REDG_DAT T1, ";
        $query .= "         ATTEND_SUBCLASS_DAT T2 ";
        $query .= "     WHERE ";
        $query .= "         T1.YEAR     = T2.YEAR AND ";
        $query .= "         T1.SEMESTER = T2.SEMESTER AND ";
        $query .= "         T1.SCHREGNO = T2.SCHREGNO AND ";
        $query .= "         T2.COPYCD   = '0' AND ";
        $query .= "         T2.MONTH    = '".$model->field["MONTH"]."' ";
        $query .= " ), MAIN AS ( ";
        $query .= "     SELECT ";
        $query .= "         SCHREGNO, ";
        if ($model->Properties["useCurriculumcd"] == "1") {
            $query .= "         CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD AS SUBCLASS ";
        } else {
            $query .= "         SUBCLASSCD AS SUBCLASS ";
        }
        $query .= "     FROM ";
        $query .= "         REDG_DAT T1, ";
        $query .= "         V_SUBCLASS_MST T2 ";
        $query .= "     WHERE ";
        $query .= "         T1.YEAR = T2.YEAR AND ";
        if ($model->Properties["useCurriculumcd"] == "1") {
            $query .= "         CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD IN ('".implode("','", explode(',', $subclass))."') ";
        } else {
            $query .= "         SUBCLASSCD IN ('".implode("','", explode(',', $subclass))."') ";
        }
        $query .= " ) ";

        $query .= " SELECT ";
        $query .= "     T1.*, ";
        $query .= "     L1.FLG, ";
        $query .= "     L2.SICK, ";
        $query .= "     L2.NOTICE, ";
        $query .= "     L2.NONOTICE ";
        $query .= " FROM ";
        $query .= "     MAIN T1 ";
        $query .= "     LEFT JOIN CHR_SUBCLASS L1 ON T1.SCHREGNO = L1.SCHREGNO AND T1.SUBCLASS = L1.SUBCLASS ";
        $query .= "     LEFT JOIN ATTEND L2 ON T1.SCHREGNO = L2.SCHREGNO AND T1.SUBCLASS = L2.SUBCLASS ";

        return $query;
    }

    //フィールド一覧取得
    function getFieldList($table) {
        $query  = " WITH MAIN AS ( ";
        $query .= "     SELECT ";
        $query .= "         COLNO, ";
        $query .= "         NAME ";
        $query .= "     FROM ";
        $query .= "         SYSIBM.SYSCOLUMNS ";
        $query .= "     WHERE ";
        $query .= "         TBNAME  = '".$table."' AND ";
        $query .= "         NULLS   = 'Y' AND ";
        $query .= "         NAME NOT IN ('APPOINTED_DAY', 'LESSON', 'REGISTERCD', 'UPDATED') ";
        $query .= "     ORDER BY ";
        $query .= "         COLNO ";
        $query .= " ) ";

        $query .= " SELECT ";
        $query .= "     NAME ";
        $query .= " FROM ";
        $query .= "     MAIN ";
        $query .= " ORDER BY ";
        $query .= "     COLNO ";

        return $query;
    }

    //学期の最終月判定
    function getMaxSemeMonthCnt($model) {
        $query  = " SELECT ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     SEMESTER_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR         = '".CTRL_YEAR."' AND ";
        $query .= "     SEMESTER     = '".$model->field["SEMESTER"]."' AND ";
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
        $query  = " WITH SCH_INFO AS ( ";
        $query .= "     SELECT ";
        $query .= "         SCHREGNO, ";
        $query .= "         GRADE, ";
        $query .= "         COURSECD, ";
        $query .= "         MAJORCD, ";
        $query .= "         COURSECODE ";
        $query .= "     FROM ";
        $query .= "         SCHREG_REGD_DAT ";
        $query .= "     WHERE ";
        $query .= "         YEAR = '".CTRL_YEAR."' AND ";
        $query .= "         SEMESTER = '".$model->field["SEMESTER"]."' AND ";
        $query .= "         GRADE || '-' || HR_CLASS = '".$model->field["GRADE_HR_CLASS"]."' ";
        $query .= " ) ";

        $query .= " SELECT DISTINCT ";
        $query .= "     T1.SCHREGNO, ";
        if ($model->Properties["useCurriculumcd"] == "1") {
            $query .= "     T2.CLASSCD || '-' || T2.SCHOOL_KIND || '-' || T2.CURRICULUM_CD || '-' || T2.SUBCLASSCD AS SUBCLASS, ";
        } else {
            $query .= "     T2.SUBCLASSCD AS SUBCLASS, ";
        }
        $query .= "     T2.CREDITS ";
        $query .= " FROM ";
        $query .= "     SCH_INFO T1, ";
        $query .= "     CREDIT_MST T2 ";
        $query .= " WHERE ";
        $query .= "     T1.GRADE    = T2.GRADE AND ";
        $query .= "     T1.COURSECD = T2.COURSECD AND ";
        $query .= "     T1.MAJORCD  = T2.MAJORCD AND ";
        $query .= "     T1.COURSECODE = T2.COURSECODE AND ";
        $query .= "     T2.YEAR     = '".CTRL_YEAR."' ";
        $query .= " ORDER BY ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     SUBCLASS ";

        return $query;
    }

    //出欠月別累積データの更新
    function getUpdateQuery($model) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //フィールド一覧取得（ATTEND_SEMES_DAT）
        $semes_field = array();
        $query = knjc033bQuery::getFieldList("ATTEND_SEMES_DAT");
        $semes_field = $db->getCol($query);

        //学期の最終月判定
        $query = knjc033bQuery::getMaxSemeMonthCnt($model);
        $maxMonth = $db->getOne($query);

        //学期の週数取得
        $query = knjc033bQuery::getSyusu($model->field["SEMESTER"]);
        $syusu = $db->getOne($query);

        //単位数取得
        $credit = array();
        $query = knjc033bQuery::getCredit($model);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $credit[$row["SCHREGNO"]][$row["SUBCLASS"]] = $row["CREDITS"];
        }

        foreach ($model->field["SCHREGNO"] as $key => $schregno) {
            //更新対象科目がなければスキップ
            if (!$model->field["ATTEND_FIELD"][$key]) continue;

            foreach (explode(',', $model->field["SUBCLASS"]) as $subclass) {
                //更新対象科目でなければスキップ
                if (!in_array($subclass, explode(',', $model->field["ATTEND_FIELD"][$key]))) continue;

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
                    $query .= "     CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD = '".$subclass."' ";
                } else {
                    $query .= "     CLASSCD     = '".substr($subclass, 0, 2)."' AND ";
                    $query .= "     SUBCLASSCD  = '".$subclass."' ";
                }
                $flag = $db->getOne($query);

                //授業時数取得
                $lesson = 0;
                if ($maxMonth == 1 && $syusu > 0 && $credit[$schregno][$subclass] && $model->Properties["hibiNyuuryokuNasi"] == "1" && $model->Properties["useJugyoujisuuSanshutsu"] == "1") {
                    $lesson = $syusu * $credit[$schregno][$subclass];
                }

                $data = array();
                if ($flag) {
                    $where  = " WHERE ";
                    $where .= "     COPYCD      = '0' AND ";
                    $where .= "     YEAR        = '".CTRL_YEAR."' AND ";
                    $where .= "     SEMESTER    = '".$model->field["SEMESTER"]."' AND ";
                    $where .= "     MONTH       = '".$model->field["MONTH"]."' AND ";
                    $where .= "     SCHREGNO    = '".$schregno."' AND ";
                    if ($model->Properties["useCurriculumcd"] == "1") {
                        $where .= "     CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD = '".$subclass."' ";
                    } else {
                        $where .= "     CLASSCD     = '".substr($subclass, 0, 2)."' AND ";
                        $where .= "     SUBCLASSCD  = '".$subclass."' ";
                    }
                } else {
                    //更新対象データがない時 - INSERT
                    $data["COPYCD"][TEXT]     = "0";
                    $data["YEAR"][TEXT]       = CTRL_YEAR;
                    $data["MONTH"][TEXT]      = $model->field["MONTH"];
                    $data["SEMESTER"][TEXT]   = $model->field["SEMESTER"];
                    $data["SCHREGNO"][TEXT]   = $schregno;
                    if ($model->Properties["useCurriculumcd"] == "1") {
                        list($classcd, $school_kind, $curriculum_cd, $subclasscd) = preg_split("/-/", $subclass);
                        $data["CLASSCD"][TEXT]       = $classcd;
                        $data["SCHOOL_KIND"][TEXT]   = $school_kind;
                        $data["CURRICULUM_CD"][TEXT] = $curriculum_cd;
                        $data["SUBCLASSCD"][TEXT]    = $subclasscd;
                    } else {
                        $data["CLASSCD"][TEXT]    = substr($subclass, 0, 2);
                        $data["SUBCLASSCD"][TEXT] = $subclass;
                    }
                    $data["APPOINTED_DAY"][TEXT] = $model->appointed_day;
                    $data["LESSON"][NUMBER]      = $lesson;
                }

                //更新データをセット
                foreach (explode(',', $model->field["UPD_FIELD"]) as $field) {
                    if ($model->Properties["use_Attend_zero_hyoji"] == "1") {
                        $data[$field][NUMBER]   = $model->field[$field][$subclass][$key];
                    } else {
                        $data[$field][NUMBER]   = ($model->field[$field][$subclass][$key]) ? $model->field[$field][$subclass][$key] : 0;
                    }
                }
                $data["REGISTERCD"][TEXT]    = STAFFCD;
                $data["UPDATED"][FUNC]       = "sysdate()";

                if ($flag) {
                    $query = Query::updateSQL($data, "ATTEND_SUBCLASS_DAT", $where);
                } else {
                    $query = Query::insertSQL($data, "ATTEND_SUBCLASS_DAT");
                }
                $db->query($query);
            }

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

            //ATTEND_SEMES_DATセット
            $data = array();
            $data["COPYCD"][TEXT]           = "0";
            $data["YEAR"][TEXT]             = CTRL_YEAR;
            $data["MONTH"][TEXT]            = $model->field["MONTH"];
            $data["SEMESTER"][TEXT]         = $model->field["SEMESTER"];
            $data["SCHREGNO"][TEXT]         = $schregno;
            $data["APPOINTED_DAY"][TEXT]    = $model->appointed_day;
            $data["LESSON"][NUMBER]         = 0;
            foreach ($semes_field as $semfld) {
                if ($model->Properties["use_Attend_zero_hyoji"] != "1") {
                    $data[$semfld][NUMBER]      = 0;
                }
            }
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][FUNC]          = "SYSDATE()";

            //ATTEND_SEMES_DAT追加
            if (!$flg2) {
                $query = Query::insertSQL($data, "ATTEND_SEMES_DAT");
                $db->query($query);
            }
        }

        $db->commit();
        Query::dbCheckIn($db);
        return;
    }
}
?>
