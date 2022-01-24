<?php

require_once('for_php7.php');

require_once("PrimarySchoolProcess.php");

class knjs030Query extends Query {

    /* 校時取得 */
    function getPeriod() {
        $query  = " SELECT ";
        $query .= "     NAMECD2 AS VALUE, ";
        $query .= "     NAME1 AS PERIOD_NAME ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND NAMECD1 = 'B001' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    /* 学級名称取得 */
    function selectHrClass($model)
    {
        $query  = " SELECT ";
        $query .= "     REGD_H.GRADE || '-' || REGD_H.HR_CLASS AS VALUE, ";
        $query .= "     REGD_H.HR_NAME AS LABEL";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_HDAT REGD_H ";
        $query .= " WHERE ";
        $query .= "     REGD_H.YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND REGD_H.SEMESTER = '".CTRL_SEMESTER."' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= " AND REGD_H.GRADE IN (SELECT ";
                $query .= "                         REGD_G.GRADE ";
                $query .= "                      FROM ";
                $query .= "                         SCHREG_REGD_GDAT REGD_G ";
                $query .= "                      WHERE ";
                $query .= "                         REGD_G.YEAR = '".CTRL_YEAR."' ";
                $query .= "                         AND REGD_G.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."')) ";
            }
        } elseif ($model->Properties["useSchool_KindField"] == "1") {
            $query .= "     AND REGD_H.GRADE IN (SELECT ";
            $query .= "                             REGD_G.GRADE ";
            $query .= "                          FROM ";
            $query .= "                             SCHREG_REGD_GDAT REGD_G ";
            $query .= "                          WHERE ";
            $query .= "                             REGD_G.YEAR = '".CTRL_YEAR."' ";
            $query .= "                             AND REGD_G.SCHOOL_KIND = '" .SCHOOLKIND ."') ";
        }
        if(AUTHORITY != DEF_UPDATABLE){
            $query .= "     AND (REGD_H.TR_CD1    = '".STAFFCD."' OR ";
            $query .= "          REGD_H.TR_CD2    = '".STAFFCD."' OR ";
            $query .= "          REGD_H.TR_CD3    = '".STAFFCD."' OR ";
            $query .= "          REGD_H.SUBTR_CD1 = '".STAFFCD."' OR ";
            $query .= "          REGD_H.SUBTR_CD2 = '".STAFFCD."' OR ";
            $query .= "          REGD_H.SUBTR_CD3 = '".STAFFCD."') ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    /* 学校行事取得 */
    function getEventQuery($executeDate, $model) {
        $query  = " SELECT ";
        $query .= "     EXECUTEDATE, ";
        $query .= "     HOLIDAY_FLG, ";
        $query .= "     REMARK1, ";
        $query .= "     REMARK2 ";
        $query .= " FROM ";
        if ($model->field["HR_OR_STAFF"] == "1") {
            $query .= "     EVENT_DAT ";
        } else {
            $query .= "     EVENT_MST ";
        }
        $query .= " WHERE ";
        if ($model->field["HR_OR_STAFF"] == "1") {
            $query .= "     EXECUTEDATE = '{$executeDate}' ";
            $query .= "     AND GRADE || '-' || HR_CLASS = '{$model->field["HR_CLASS"]}' ";
        } else {
            $query .= "     DATA_DIV = '1' ";
            $query .= "     AND GRADE = '00' ";
            $query .= "     AND EXECUTEDATE = '{$executeDate}' ";
        }
        $query .= "     AND HR_CLASS_DIV = '{$model->field["HR_CLASS_DIV"]}' ";

        return $query;
    }

    /* 学期取得 */
    function getThisSeme($day) {
        list($lyear, $lmonth, $lday) = preg_split("/-/", $day);
        $setYear = $lmonth > 3 ? $lyear : $lyear - 1;
        $query  = " SELECT ";
        $query .= "     SEMESTER ";
        $query .= " FROM ";
        $query .= "     SEMESTER_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$setYear}' ";
        $query .= "     AND SEMESTER < '9' ";
        $query .= "     AND '{$day}' BETWEEN SDATE AND EDATE ";

        return $query;
    }

    function getHrUnitDat($model, $chairCd = "", $div = "") {
        list($grade, $hrClass) = preg_split("/-/", $model->field["HR_CLASS"]);

        $query  = " WITH MAIN_T AS ( ";
        if ($div == "") {
            $query .= " SELECT DISTINCT ";
            $query .= "     SCH_CDAT.SEMESTER, ";
            $query .= "     SCH_CDAT.EXECUTEDATE, ";
            $query .= "     SCH_CDAT.PERIODCD, ";
            $query .= "     SCH_CDAT.CHAIRCD, ";
            $query .= "     SCH_CDAT.RANK, ";
            $query .= "     CHAIR.CHAIRNAME, ";
            $query .= "     CHAIR.CHAIRABBV, ";
            //教育課程対応
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= " CHAIR.SCHOOL_KIND, ";
                $query .= " CHAIR.CURRICULUM_CD, ";
            }
            $query .= "     CHAIR.SUBCLASSCD, ";
            $query .= "     SUBD.SUBCLASSNAME, ";
            $query .= "     SUBD.SUBCLASSABBV, ";
            $query .= "     CLASSD.CLASSCD, ";
            $query .= "     CLASSD.CLASSNAME, ";
            $query .= "     CLASSD.CLASSABBV, ";
            $query .= "     UNITD.DIV, ";
            $query .= "     UNITD.L_TITOL, ";
            if ($model->field["UNIT_SHOW"] == "1") {
                $query .= "     CASE WHEN UNITD.UNIT_S_NAME IS NOT NULL ";
                $query .= "          THEN UNITD.UNIT_S_NAME ";
                $query .= "          ELSE CASE WHEN UNITD.UNIT_M_NAME IS NOT NULL ";
                $query .= "                    THEN UNITD.UNIT_M_NAME ";
                $query .= "                    ELSE UNITD.UNIT_L_NAME ";
                $query .= "               END ";
                $query .= "     END AS UNIT_NAME, ";
            } else {
                $query .= "     UNITD.UNIT_L_NAME AS UNIT_NAME, ";
            }
            $query .= "     UNITD.UNIT_M_NAME, ";
            $query .= "     UNITD.UNIT_S_NAME, ";
            $query .= "     UNITD.UNIT_DATA, ";
            $query .= "     CHAIR_STF.STAFFCD, ";
            $query .= "     STF.STAFFNAME, ";
            if ($model->field["REMARK_SHOW"] == "1") {
                $query .= "     UNITD.REMARK1 AS REMARK, ";
            } else if ($model->field["REMARK_SHOW"] == "2") {
                $query .= "     UNITD.REMARK2 AS REMARK, ";
            } else {
                $query .= "     UNITD.REMARK3 AS REMARK, ";
            }
            $query .= "     UNITD.REMARK1, ";
            $query .= "     UNITD.REMARK2, ";
            $query .= "     UNITD.REMARK3, ";
            $query .= "     UNITD.REMARK4, ";
            $query .= "     ISSUEM.ISSUECOMPANYNAME ";
        } else {
            $query .= " SELECT DISTINCT ";
            $query .= "     SCH_CDAT.EXECUTEDATE, ";
            $query .= "     SCH_CDAT.PERIODCD, ";
            $query .= "     SCH_CDAT.CHAIRCD, ";
            //教育課程対応
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= " SUBD.CLASSCD, ";
                $query .= " SUBD.SCHOOL_KIND, ";
                $query .= " SUBD.CURRICULUM_CD, ";
            }
            $query .= "     SUBD.SUBCLASSCD ";
        }
        $query .= " FROM ";
        $query .= "     UNIT_SCH_CHR_RANK_DAT SCH_CDAT ";
        $query .= "     INNER JOIN CHAIR_DAT CHAIR ON CHAIR.YEAR = SCH_CDAT.YEAR ";
        $query .= "                AND CHAIR.SEMESTER = SCH_CDAT.SEMESTER ";
        $query .= "                AND CHAIR.CHAIRCD = SCH_CDAT.CHAIRCD ";
        $query .= "     LEFT JOIN CHAIR_STF_DAT CHAIR_STF ON CHAIR_STF.YEAR = CHAIR.YEAR ";
        $query .= "          AND CHAIR_STF.SEMESTER = CHAIR.SEMESTER ";
        $query .= "          AND CHAIR_STF.CHAIRCD = CHAIR.CHAIRCD ";
        $query .= "     LEFT JOIN STAFF_MST STF ON STF.STAFFCD = CHAIR_STF.STAFFCD ";
        $query .= "     INNER JOIN SUBCLASS_MST SUBD ON SUBD.SUBCLASSCD = CHAIR.SUBCLASSCD ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "                             AND SUBD.CLASSCD = CHAIR.CLASSCD ";
            $query .= "                             AND SUBD.SCHOOL_KIND = CHAIR.SCHOOL_KIND ";
            $query .= "                             AND SUBD.CURRICULUM_CD = CHAIR.CURRICULUM_CD ";
        }
        $query .= "     INNER JOIN CLASS_MST CLASSD ON CLASSD.CLASSCD = SUBSTR(SUBD.SUBCLASSCD, 1, 2) ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "                            AND CLASSD.SCHOOL_KIND = SUBD.SCHOOL_KIND ";
        }
        $query .= "     INNER JOIN UNIT_STUDY_TEXT_BOOK_DAT UNIT_TEXT ON UNIT_TEXT.YEAR = CHAIR.YEAR ";
        $query .= "                AND UNIT_TEXT.GRADE = '{$grade}' ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "            AND UNIT_TEXT.CLASSCD = CHAIR.CLASSCD ";
            $query .= "            AND UNIT_TEXT.SCHOOL_KIND = CHAIR.SCHOOL_KIND ";
            $query .= "            AND UNIT_TEXT.CURRICULUM_CD = CHAIR.CURRICULUM_CD ";
        }
        $query .= "                AND UNIT_TEXT.SUBCLASSCD = CHAIR.SUBCLASSCD ";
        $query .= "     LEFT JOIN UNIT_DAT UNITD ON UNITD.YEAR = SCH_CDAT.YEAR ";
        $query .= "          AND UNITD.DATA_DIV = '2' ";
        $query .= "          AND UNITD.GRADE || '-' || UNITD.HR_CLASS = '{$model->field["HR_CLASS"]}' ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "      AND UNITD.CLASSCD = CHAIR.CLASSCD ";
            $query .= "      AND UNITD.SCHOOL_KIND = CHAIR.SCHOOL_KIND ";
            $query .= "      AND UNITD.CURRICULUM_CD = CHAIR.CURRICULUM_CD ";
        }
        $query .= "          AND UNITD.SUBCLASSCD = CHAIR.SUBCLASSCD ";
        $query .= "          AND UNITD.SEQ = SCH_CDAT.RANK ";
        $query .= "          AND UNITD.ISSUECOMPANYCD = UNIT_TEXT.ISSUECOMPANYCD ";
        $query .= "     LEFT JOIN ISSUECOMPANY_MST ISSUEM ON ISSUEM.ISSUECOMPANYCD = UNITD.ISSUECOMPANYCD ";
        $query .= " WHERE ";
        if ($div != "TOTAL_YEAR") {
            $query .= "     SCH_CDAT.EXECUTEDATE BETWEEN '{$model->updDateArray[6]["DAY"]}' AND '{$model->updDateArray[5]["DAY"]}' AND ";
        } else {
            list($fYear, $fMonth, $fDay) = preg_split("/-/", $model->updDateArray[6]["DAY"]);
            $setUpdDate = $fMonth > 3 ? $fYear."-04-01" : ($fYear - 1)."-04-01";
            $query .= "     SCH_CDAT.EXECUTEDATE BETWEEN '{$setUpdDate}' AND '{$model->updDateArray[5]["DAY"]}' AND ";
        }
        $query .= "         SCH_CDAT.CHAIRCD IN ( ";
        $query .= "         SELECT DISTINCT ";
        $query .= "             CSTD.CHAIRCD ";
        $query .= "         FROM ";
        $query .= "             SCHREG_REGD_DAT REGD, ";
        $query .= "             CHAIR_STD_DAT CSTD ";
        $query .= "         WHERE ";
        $query .= "             REGD.YEAR = '".CTRL_YEAR."' ";
        $query .= "             AND REGD.SEMESTER IN ( ";
        $query .= "                 SELECT ";
        $query .= "                     II1.SEMESTER ";
        $query .= "                 FROM ";
        $query .= "                     SEMESTER_MST II1 ";
        $query .= "                 WHERE ";
        $query .= "                     II1.YEAR = '".CTRL_YEAR."' ";
        $query .= "                     AND II1.SEMESTER < '9' ";
        if ($div != "TOTAL_YEAR") {
            $query .= "                     AND ( ";
            $setOr = "";
            foreach ($model->updDateArray as $key => $val) {
                $query .= "                     ".$setOr." '{$val["DAY"]}' BETWEEN II1.SDATE AND II1.EDATE ";
                $setOr = " OR ";
            }
            $query .= "                     ) ";
        }
        $query .= "             ) ";
        $query .= "             AND REGD.GRADE || '-' || REGD.HR_CLASS = '{$model->field["HR_CLASS"]}' ";
        $query .= "             AND REGD.YEAR = CSTD.YEAR ";
        $query .= "             AND REGD.SEMESTER = CSTD.SEMESTER ";
        $query .= "             AND REGD.SCHREGNO = CSTD.SCHREGNO ";
        if ($div != "TOTAL_YEAR") {
            $query .= "             AND ( ";
            $setOr = "";
            foreach ($model->updDateArray as $key => $val) {
                $query .= "                     ".$setOr." '{$val["DAY"]}' BETWEEN CSTD.APPDATE AND CSTD.APPENDDATE ";
                $setOr = " OR ";
            }
            $query .= "             ) ";
        }
        $query .= "     ) ";
        $query .= " ) ";

        if ($div == "") {
            $query .= " SELECT ";
            $query .= "     * ";
            $query .= " FROM ";
            $query .= "     MAIN_T ";
        } else {
            $query .= " , CNT_T AS ( ";
            $query .= " SELECT ";
            $query .= "     COUNT(*) * 45 AS CNT ";
            $query .= " FROM ";
            $query .= "     MAIN_T ";
            $query .= " WHERE ";
            if ($model->field["JISU_SHOW"] == "2") {
                $query .= "     SUBSTR(SUBCLASSCD, 1, 2) ";
                //教育課程対応
                if ($model->Properties["useCurriculumcd"] == '1') {
                    $query .= " || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD ";
                }
                $query .= "             = '{$chairCd}' ";
            } else {
                $query .= "     CHAIRCD = '".$chairCd."' ";
            }
            $query .= "     AND NOT EXISTS(SELECT ";
            $query .= "                        'x' ";
            $query .= "                    FROM ";
            $query .= "                        UNIT_SCH_CHR_BUNKATU_DAT NE1 ";
            $query .= "                    WHERE ";
            $query .= "                        MAIN_T.EXECUTEDATE = NE1.EXECUTEDATE ";
            $query .= "                        AND MAIN_T.PERIODCD = NE1.PERIODCD ";
            $query .= "                        AND MAIN_T.CHAIRCD = NE1.MAIN_CHAIRCD ";
            $query .= "     ) ";

            $query .= " UNION ALL ";
            $query .= " SELECT ";
            $query .= "     SUM(UB.MINUTE) AS CNT ";
            $query .= " FROM ";
            $query .= "     UNIT_SCH_CHR_BUNKATU_DAT AS UB ";
            $query .= "     INNER JOIN CHAIR_DAT CHAIR ON CHAIR.YEAR = UB.YEAR ";
            $query .= "                AND CHAIR.SEMESTER = UB.SEMESTER ";
            $query .= "                AND CHAIR.CHAIRCD = UB.CHAIRCD ";
            $query .= "     INNER JOIN SUBCLASS_MST SUBD ON SUBD.SUBCLASSCD = CHAIR.SUBCLASSCD ";
            //教育課程対応
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "                             AND SUBD.CLASSCD = CHAIR.CLASSCD ";
                $query .= "                             AND SUBD.SCHOOL_KIND = CHAIR.SCHOOL_KIND ";
                $query .= "                             AND SUBD.CURRICULUM_CD = CHAIR.CURRICULUM_CD ";
            }
            $query .= " WHERE ";
            if ($div != "TOTAL_YEAR") {
                $query .= "     UB.EXECUTEDATE BETWEEN '{$model->updDateArray[6]["DAY"]}' AND '{$model->updDateArray[5]["DAY"]}' AND ";
            } else {
                list($fYear, $fMonth, $fDay) = preg_split("/-/", $model->updDateArray[6]["DAY"]);
                $setUpdDate = $fMonth > 3 ? $fYear."-04-01" : ($fYear - 1)."-04-01";
                $query .= "     UB.EXECUTEDATE BETWEEN '{$setUpdDate}' AND '{$model->updDateArray[5]["DAY"]}' AND ";
            }
            if ($model->field["JISU_SHOW"] == "2") {
                $query .= "     SUBSTR(SUBD.SUBCLASSCD, 1, 2) ";
                //教育課程対応
                if ($model->Properties["useCurriculumcd"] == '1') {
                    $query .= " || '-' || SUBD.SCHOOL_KIND || '-' || SUBD.CURRICULUM_CD ";
                }
                $query .= "                = '{$chairCd}' ";
            } else {
                $query .= "     UB.CHAIRCD = '".$chairCd."' ";
            }
            $query .= " ) ";
            $query .= " SELECT ";
            $query .= "     SUM(CNT) / 45 AS SYOU, ";
            $query .= "     MOD(SUM(CNT), 45) / 5 AS BUNSI ";
            $query .= " FROM ";
            $query .= "     CNT_T ";
        }

        return $query;
    }

    function getStaffUnitDat($model, $chairCd = "", $div = "") {

        $query  = " WITH MAIN_T AS ( ";
        if ($div == "") {
            $query .= " SELECT DISTINCT ";
            $query .= "     SCH_CDAT.SEMESTER, ";
            $query .= "     SCH_CDAT.EXECUTEDATE, ";
            $query .= "     SCH_CDAT.PERIODCD, ";
            $query .= "     SCH_CDAT.CHAIRCD, ";
            $query .= "     SCH_CDAT.RANK, ";
            $query .= "     CHAIR.CHAIRNAME, ";
            $query .= "     CHAIR.CHAIRABBV, ";
            //教育課程対応
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= " CHAIR.SCHOOL_KIND, ";
                $query .= " CHAIR.CURRICULUM_CD, ";
            }
            $query .= "     CHAIR.SUBCLASSCD, ";
            $query .= "     SUBD.SUBCLASSNAME, ";
            $query .= "     SUBD.SUBCLASSABBV, ";
            $query .= "     CLASSD.CLASSCD, ";
            $query .= "     CLASSD.CLASSNAME, ";
            $query .= "     CLASSD.CLASSABBV, ";
            $query .= "     UNITD.DIV, ";
            $query .= "     UNITD.L_TITOL, ";
            if ($model->field["UNIT_SHOW"] == "1") {
                $query .= "     CASE WHEN UNITD.UNIT_S_NAME IS NOT NULL ";
                $query .= "          THEN UNITD.UNIT_S_NAME ";
                $query .= "          ELSE CASE WHEN UNITD.UNIT_M_NAME IS NOT NULL ";
                $query .= "                    THEN UNITD.UNIT_M_NAME ";
                $query .= "                    ELSE UNITD.UNIT_L_NAME ";
                $query .= "               END ";
                $query .= "     END AS UNIT_NAME, ";
            } else {
                $query .= "     UNITD.UNIT_L_NAME AS UNIT_NAME, ";
            }
            $query .= "     UNITD.UNIT_M_NAME, ";
            $query .= "     UNITD.UNIT_S_NAME, ";
            $query .= "     UNITD.UNIT_DATA, ";
            $query .= "     REGD_H.HR_NAME, ";
            if ($model->field["REMARK_SHOW"] == "1") {
                $query .= "     UNITD.REMARK1 AS REMARK, ";
            } else if ($model->field["REMARK_SHOW"] == "2") {
                $query .= "     UNITD.REMARK2 AS REMARK, ";
            } else {
                $query .= "     UNITD.REMARK3 AS REMARK, ";
            }
            $query .= "     UNITD.REMARK1, ";
            $query .= "     UNITD.REMARK2, ";
            $query .= "     UNITD.REMARK3, ";
            $query .= "     UNITD.REMARK4, ";
            $query .= "     ISSUEM.ISSUECOMPANYNAME ";
        } else {
            $query .= " SELECT DISTINCT ";
            $query .= "     SCH_CDAT.EXECUTEDATE, ";
            $query .= "     SCH_CDAT.PERIODCD, ";
            $query .= "     SCH_CDAT.CHAIRCD, ";
            //教育課程対応
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= " SUBD.CLASSCD, ";
                $query .= " SUBD.SCHOOL_KIND, ";
                $query .= " SUBD.CURRICULUM_CD, ";
            }
            $query .= "     SUBD.SUBCLASSCD ";
        }
        $query .= " FROM ";
        $query .= "     UNIT_SCH_CHR_RANK_DAT SCH_CDAT ";
        $query .= "     INNER JOIN CHAIR_DAT CHAIR ON CHAIR.YEAR = SCH_CDAT.YEAR ";
        $query .= "                AND CHAIR.SEMESTER = SCH_CDAT.SEMESTER ";
        $query .= "                AND CHAIR.CHAIRCD = SCH_CDAT.CHAIRCD ";
        $query .= "     INNER JOIN SUBCLASS_MST SUBD ON SUBD.SUBCLASSCD = CHAIR.SUBCLASSCD ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "                             AND SUBD.CLASSCD = CHAIR.CLASSCD ";
            $query .= "                             AND SUBD.SCHOOL_KIND = CHAIR.SCHOOL_KIND ";
            $query .= "                             AND SUBD.CURRICULUM_CD = CHAIR.CURRICULUM_CD ";
        }
        $query .= "     INNER JOIN CLASS_MST CLASSD ON CLASSD.CLASSCD = SUBSTR(SUBD.SUBCLASSCD, 1, 2) ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "                            AND CLASSD.SCHOOL_KIND = SUBD.SCHOOL_KIND ";
        }
        $query .= "     INNER JOIN CHAIR_STF_DAT CHAIR_STF ON CHAIR_STF.YEAR = CHAIR.YEAR ";
        $query .= "           AND CHAIR_STF.SEMESTER = CHAIR.SEMESTER ";
        $query .= "           AND CHAIR_STF.CHAIRCD = CHAIR.CHAIRCD ";
        $query .= "           AND CHAIR_STF.STAFFCD = '".$model->field["STAFF"]."' ";
        $query .= "     LEFT JOIN STAFF_MST STF ON STF.STAFFCD = CHAIR_STF.STAFFCD ";
        $query .= "     LEFT JOIN ( ";
        $query .= "              SELECT ";
        $query .= "                  CSTD.YEAR, ";
        $query .= "                  CSTD.SEMESTER, ";
        $query .= "                  CSTD.CHAIRCD, ";
        $query .= "                  MAX(REGD.GRADE || '-' || REGD.HR_CLASS) AS MAX_CLASS ";
        $query .= "              FROM ";
        $query .= "                  SCHREG_REGD_DAT REGD, ";
        $query .= "                  CHAIR_STD_DAT CSTD ";
        $query .= "              WHERE ";
        $query .= "                  REGD.YEAR = '".CTRL_YEAR."' ";
        $query .= "                  AND REGD.SEMESTER IN ( ";
        $query .= "                      SELECT ";
        $query .= "                          II1.SEMESTER ";
        $query .= "                      FROM ";
        $query .= "                          SEMESTER_MST II1 ";
        $query .= "                      WHERE ";
        $query .= "                          II1.YEAR = '".CTRL_YEAR."' ";
        $query .= "                          AND II1.SEMESTER < '9' ";
        if ($div != "TOTAL_YEAR") {
            $query .= "                          AND ( ";
            $setOr = "";
            foreach ($model->updDateArray as $key => $val) {
                $query .= "                          ".$setOr." '{$val["DAY"]}' BETWEEN II1.SDATE AND II1.EDATE ";
                $setOr = " OR ";
            }
            $query .= "                          ) ";
        }
        $query .= "                  ) ";
        $query .= "                  AND REGD.YEAR = CSTD.YEAR ";
        $query .= "                  AND REGD.SEMESTER = CSTD.SEMESTER ";
        $query .= "                  AND REGD.SCHREGNO = CSTD.SCHREGNO ";
        if ($div != "TOTAL_YEAR") {
            $query .= "                  AND ( ";
            $setOr = "";
            foreach ($model->updDateArray as $key => $val) {
                $query .= "                          ".$setOr." '{$val["DAY"]}' BETWEEN CSTD.APPDATE AND CSTD.APPENDDATE ";
                $setOr = " OR ";
            }
            $query .= "                  ) ";
        }
        $query .= "              GROUP BY ";
        $query .= "                  CSTD.YEAR, ";
        $query .= "                  CSTD.SEMESTER, ";
        $query .= "                  CSTD.CHAIRCD ";
        $query .= "          ) MAX_REGD ON MAX_REGD.YEAR = CHAIR_STF.YEAR ";
        $query .= "          AND MAX_REGD.SEMESTER = CHAIR_STF.SEMESTER ";
        $query .= "          AND MAX_REGD.CHAIRCD = CHAIR_STF.CHAIRCD ";
        $query .= "     LEFT JOIN SCHREG_REGD_HDAT REGD_H ON REGD_H.YEAR = MAX_REGD.YEAR ";
        $query .= "          AND REGD_H.SEMESTER = MAX_REGD.SEMESTER ";
        $query .= "          AND REGD_H.GRADE || '-' || REGD_H.HR_CLASS = MAX_REGD.MAX_CLASS ";
        $query .= "     INNER JOIN UNIT_STUDY_TEXT_BOOK_DAT UNIT_TEXT ON UNIT_TEXT.YEAR = CHAIR.YEAR ";
        $query .= "                AND UNIT_TEXT.GRADE = REGD_H.GRADE ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "            AND UNIT_TEXT.CLASSCD = CHAIR.CLASSCD ";
            $query .= "            AND UNIT_TEXT.SCHOOL_KIND = CHAIR.SCHOOL_KIND ";
            $query .= "            AND UNIT_TEXT.CURRICULUM_CD = CHAIR.CURRICULUM_CD ";
        }
        $query .= "                AND UNIT_TEXT.SUBCLASSCD = CHAIR.SUBCLASSCD ";
        $query .= "     LEFT JOIN UNIT_DAT UNITD ON UNITD.YEAR = SCH_CDAT.YEAR ";
        $query .= "          AND UNITD.DATA_DIV = '2' ";
        $query .= "          AND UNITD.GRADE || '-' || UNITD.HR_CLASS = MAX_REGD.MAX_CLASS ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "      AND UNITD.CLASSCD = CHAIR.CLASSCD ";
            $query .= "      AND UNITD.SCHOOL_KIND = CHAIR.SCHOOL_KIND ";
            $query .= "      AND UNITD.CURRICULUM_CD = CHAIR.CURRICULUM_CD ";
        }
        $query .= "          AND UNITD.SUBCLASSCD = CHAIR.SUBCLASSCD ";
        $query .= "          AND UNITD.SEQ = SCH_CDAT.RANK ";
        $query .= "          AND UNITD.ISSUECOMPANYCD = UNIT_TEXT.ISSUECOMPANYCD ";
        $query .= "     LEFT JOIN ISSUECOMPANY_MST ISSUEM ON ISSUEM.ISSUECOMPANYCD = UNITD.ISSUECOMPANYCD ";
        if ($div != "TOTAL_YEAR") {
            $query .= " WHERE ";
            $query .= "     SCH_CDAT.EXECUTEDATE BETWEEN '{$model->updDateArray[6]["DAY"]}' AND '{$model->updDateArray[5]["DAY"]}' ";
        } else {
            $query .= " WHERE ";
            list($fYear, $fMonth, $fDay) = preg_split("/-/", $model->updDateArray[6]["DAY"]);
            $setUpdDate = $fMonth > 3 ? $fYear."-04-01" : ($fYear - 1)."-04-01";
            $query .= "     SCH_CDAT.EXECUTEDATE BETWEEN '{$setUpdDate}' AND '{$model->updDateArray[5]["DAY"]}' ";
        }
        $query .= " ) ";

        $query .= " SELECT ";
        if ($div == "") {
            $query .= "     * ";
        } else {
            $query .= "     COUNT(*) AS CNT ";
        }
        $query .= " FROM ";
        $query .= "     MAIN_T ";
        if ($div != "") {
            $query .= " WHERE ";
            if ($model->field["JISU_SHOW"] == "2") {
                $query .= "     SUBSTR(SUBCLASSCD, 1, 2) ";
                //教育課程対応
                if ($model->Properties["useCurriculumcd"] == '1') {
                    $query .= " || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD ";
                }
                $query .= "             = '{$chairCd}' ";
            } else {
                $query .= "     CHAIRCD = '".$chairCd."' ";
            }
        }

        return $query;
    }

    /* 分割データ取得 */
    function getBunkatu($model, $executeDate, $periodCd, $chairCd, $div = "") {
        $query  = " SELECT ";
        if ($div == "COUNT") {
            $query .= "     COUNT(*) AS CNT ";
        } else {
            $query .= "     * ";
        }
        $query .= " FROM ";
        $query .= "     UNIT_SCH_CHR_BUNKATU_DAT ";
        $query .= " WHERE ";
        $query .= "     EXECUTEDATE = '{$executeDate}' ";
        $query .= "     AND PERIODCD = '{$periodCd}' ";
        $query .= "     AND MAIN_CHAIRCD = '{$chairCd}' ";
        if ($div != "COUNT") {
            $query .= " ORDER BY ";
            $query .= "     SEQ ";
        }

        return $query;
    }

    /* 講座取得 */
    function getChair($model) {
        $query  = " SELECT DISTINCT ";
        $query .= "     CSTD.SEMESTER, ";
        $query .= "     CSTD.CHAIRCD AS VALUE, ";
        $query .= "     CHAIR.CHAIRNAME, ";
        $query .= "     CHAIR.CHAIRABBV, ";
        $query .= "     SUBD.SUBCLASSNAME, ";
        $query .= "     SUBD.SUBCLASSABBV, ";
        $query .= "     substr(CHAIR.SUBCLASSCD, 1, 2) AS CLASSCD, ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= " CHAIR.SCHOOL_KIND, ";
            $query .= " CHAIR.CURRICULUM_CD, ";
        }
        $query .= "     CLASS_M.CLASSNAME, ";
        $query .= "     CLASS_M.CLASSABBV, ";
        $query .= "     CHAIR_STF.STAFFCD, ";
        $query .= "     STF.STAFFNAME ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT REGD, ";
        $query .= "     CHAIR_STD_DAT CSTD ";
        $query .= "     LEFT JOIN CHAIR_DAT CHAIR ON CHAIR.YEAR = CSTD.YEAR ";
        $query .= "          AND CHAIR.SEMESTER = CSTD.SEMESTER ";
        $query .= "          AND CHAIR.CHAIRCD = CSTD.CHAIRCD ";
        $query .= "     LEFT JOIN CHAIR_STF_DAT CHAIR_STF ON CHAIR_STF.YEAR = CHAIR.YEAR ";
        $query .= "          AND CHAIR_STF.SEMESTER = CHAIR.SEMESTER ";
        $query .= "          AND CHAIR_STF.CHAIRCD = CHAIR.CHAIRCD ";
        $query .= "     LEFT JOIN STAFF_MST STF ON STF.STAFFCD = CHAIR_STF.STAFFCD ";
        $query .= "     LEFT JOIN SUBCLASS_MST SUBD ON SUBD.SUBCLASSCD = CHAIR.SUBCLASSCD ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "                            AND SUBD.CLASSCD = CHAIR.CLASSCD ";
            $query .= "                            AND SUBD.SCHOOL_KIND = CHAIR.SCHOOL_KIND ";
            $query .= "                            AND SUBD.CURRICULUM_CD = CHAIR.CURRICULUM_CD ";
        }
        $query .= "     LEFT JOIN CLASS_MST CLASS_M ON substr(CHAIR.SUBCLASSCD, 1, 2) = CLASS_M.CLASSCD ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "                            AND CHAIR.SCHOOL_KIND = CLASS_M.SCHOOL_KIND ";
        }
        $query .= " WHERE ";
        $query .= "     REGD.YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND REGD.SEMESTER IN ( ";
        $query .= "         SELECT ";
        $query .= "             II1.SEMESTER ";
        $query .= "         FROM ";
        $query .= "             SEMESTER_MST II1 ";
        $query .= "         WHERE ";
        $query .= "             II1.YEAR = '".CTRL_YEAR."' ";
        $query .= "             AND II1.SEMESTER < '9' ";
        $query .= "             AND ( ";
        $setOr = "";
        foreach ($model->updDateArray as $key => $val) {
            $query .= "             ".$setOr." '{$val["DAY"]}' BETWEEN II1.SDATE AND II1.EDATE ";
            $setOr = " OR ";
        }
        $query .= "         ) ";
        $query .= "     ) ";
        $query .= "     AND REGD.GRADE || '-' || REGD.HR_CLASS = '{$model->field["HR_CLASS"]}' ";
        $query .= "     AND REGD.YEAR = CSTD.YEAR ";
        $query .= "     AND REGD.SEMESTER = CSTD.SEMESTER ";
        $query .= "     AND REGD.SCHREGNO = CSTD.SCHREGNO ";
        $query .= "     AND EXISTS (SELECT ";
        $query .= "                     'x' ";
        $query .= "                 FROM ";
        $query .= "                     UNIT_STUDY_TEXT_BOOK_DAT UNIT_TEXT ";
        $query .= "                 WHERE ";
        $query .= "                     UNIT_TEXT.YEAR = CHAIR.YEAR ";
        $query .= "                     AND UNIT_TEXT.GRADE = REGD.GRADE ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "                 AND UNIT_TEXT.CLASSCD = CHAIR.CLASSCD ";
            $query .= "                 AND UNIT_TEXT.SCHOOL_KIND = CHAIR.SCHOOL_KIND ";
            $query .= "                 AND UNIT_TEXT.CURRICULUM_CD = CHAIR.CURRICULUM_CD ";
        }
        $query .= "                     AND UNIT_TEXT.SUBCLASSCD = CHAIR.SUBCLASSCD ";
        $query .= "     ) ";
        $query .= " ORDER BY ";
        $query .= "     CSTD.SEMESTER, ";
        $query .= "     CSTD.CHAIRCD ";
        return $query;
    }

    /* 担任しているか */
    function getTrClass($model)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_HDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND SEMESTER = '".CTRL_SEMESTER."' ";
        if(AUTHORITY != DEF_UPDATABLE){
            $query .= "     AND (TR_CD1 = '".STAFFCD."' ";
            $query .= "          OR TR_CD2 = '".STAFFCD."' ";
            $query .= "          OR TR_CD3 = '".STAFFCD."' ";
            $query .= "          OR SUBTR_CD1 = '".STAFFCD."' ";
            $query .= "          OR SUBTR_CD2 = '".STAFFCD."' ";
            $query .= "          OR SUBTR_CD3 = '".STAFFCD."') ";
        }

        return $query;
    }

    /* 担当データの取得 */
    function selectStaff($model)
    {
        $query  = " SELECT ";
        $query .= "     T1.STAFFCD AS VALUE, ";
        $query .= "     T1.STAFFCD || ':' || T1.STAFFNAME AS LABEL";
        $query .= " FROM ";
        $query .= "     V_STAFF_MST T1 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' ";
        if(AUTHORITY != DEF_UPDATABLE){
            $query .= "     AND T1.STAFFCD = '".STAFFCD."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     T1.STAFFCD ";

        return $query;
    }

    //月取得
    function getMonth()
    {
        $query  = " WITH MONTH_T(LABEL, VALUE, SORTNO) AS ( ";
        $query .= " VALUES('04', '04', '01') ";
        $query .= " UNION ";           
        $query .= " VALUES('05', '05', '02') ";
        $query .= " UNION ";           
        $query .= " VALUES('06', '06', '03') ";
        $query .= " UNION ";           
        $query .= " VALUES('07', '07', '04') ";
        $query .= " UNION ";           
        $query .= " VALUES('08', '08', '05') ";
        $query .= " UNION ";           
        $query .= " VALUES('09', '09', '06') ";
        $query .= " UNION ";           
        $query .= " VALUES('10', '10', '07') ";
        $query .= " UNION ";           
        $query .= " VALUES('11', '11', '08') ";
        $query .= " UNION ";           
        $query .= " VALUES('12', '12', '09') ";
        $query .= " UNION ";           
        $query .= " VALUES('01', '01', '10') ";
        $query .= " UNION ";           
        $query .= " VALUES('02', '02', '11') ";
        $query .= " UNION ";           
        $query .= " VALUES('03', '03', '12') ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     * ";
        $query .= " from ";
        $query .= "     MONTH_T ";
        $query .= " ORDER BY ";
        $query .= "     SORTNO ";

        return $query;
    }

    //標準時数
    function getTotalJisu($model, $chairCd) {
        list($grade, $hrClass) = preg_split("/-/", $model->field["HR_CLASS"]);

        $query  = " SELECT ";
        $query .= "     SUM(INT(T1.STANDARD_TIME)) AS STANDARD_TIME ";
        $query .= " FROM ";
        $query .= "     UNIT_CLASS_LESSON_SCHOOL_DAT T1 ";
        $query .= " WHERE ";
        $query .= "     T1.SCHOOL_KIND IN (SELECT ";
        $query .= "                         I1.SCHOOL_KIND ";
        $query .= "                     FROM ";
        $query .= "                         SCHREG_REGD_GDAT I1 ";
        $query .= "                     WHERE ";
        $query .= "                         I1.YEAR = '".CTRL_YEAR."' ";
        $query .= "                         AND I1.GRADE = '".$grade."' ";
        $query .= "                     ) ";
        $query .= "     AND T1.YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND T1.CURRICULUM_CD = '1' ";
        $query .= "     AND ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     T1.CLASSCD || '-' || ";
            $query .= "     T1.SCHOOL_KIND || '-' || ";
            $query .= "     T1.CURRICULUM_CD || '-' || ";
        }
        $query .= "         T1.SUBCLASSCD IN (SELECT DISTINCT ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "                     I2.CLASSCD || '-' || ";
            $query .= "                     I2.SCHOOL_KIND || '-' || ";
            $query .= "                     I2.CURRICULUM_CD || '-' || ";
        }
        $query .= "                         I2.SUBCLASSCD ";
        $query .= "                     FROM ";
        $query .= "                         CHAIR_DAT I2 ";
        $query .= "                     WHERE ";
        $query .= "                         I2.YEAR = '".CTRL_YEAR."' ";
        if ($model->field["JISU_SHOW"] == "2") {
            $query .= "                         AND SUBSTR(I2.SUBCLASSCD, 1, 2) ";
            //教育課程対応
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "                         || '-' || I2.SCHOOL_KIND || '-' || I2.CURRICULUM_CD ";
            }
            $query .= "                                        = '{$chairCd}' ";
        } else {
            $query .= "                         AND I2.CHAIRCD = '{$chairCd}' ";
        }
        $query .= "                     ) ";
        $query .= "     AND T1.SEMESTER = '9' ";
        $query .= "     AND T1.GRADE = '".$grade."' ";
        $query .= "     AND T1.TIME_DIV = '1' ";

        return $query;
    }

    /* イベントデータの更新 */
    function getUpdEvent($db, $model)
    {
        foreach ($model->updDateArray as $key => $val) {
            if (strlen($model->updEvent["HOLIDAY_FLG_".$val["DAY"]]) == 0 && strlen($model->updEvent["EVENT_".$val["DAY"]]) == 0) {
                $query = "DELETE FROM EVENT_DAT WHERE EXECUTEDATE = '{$val["DAY"]}' AND GRADE || '-' || HR_CLASS = '{$model->field["HR_CLASS"]}' AND HR_CLASS_DIV = '{$model->field["HR_CLASS_DIV"]}' ";
                $db->query($query);
            } else {
                /* 更新対象データが存在するかをチェック */
                $query  = " SELECT ";
                $query .= "     COUNT(*) AS CNT ";
                $query .= " FROM ";
                $query .= "     EVENT_DAT ";
                $query .= " WHERE ";
                $query .= "     EXECUTEDATE = '{$val["DAY"]}' ";
                $query .= "     AND GRADE || '-' || HR_CLASS = '{$model->field["HR_CLASS"]}' ";
                $query .= "     AND HR_CLASS_DIV = '{$model->field["HR_CLASS_DIV"]}' ";

                $flag = $db->getOne($query);
                $data = array();

                if ($flag) {
                    $where  = " WHERE ";
                    $where .= "     EXECUTEDATE = '{$val["DAY"]}' ";
                    $where .= "     AND GRADE || '-' || HR_CLASS = '{$model->field["HR_CLASS"]}' ";
                    $where .= "     AND HR_CLASS_DIV = '{$model->field["HR_CLASS_DIV"]}' ";
                } else {
                    /* 更新対象データがない時 - INSERT */
                    list($grade, $hrClass) = preg_split("/-/", $model->field["HR_CLASS"]);
                    $data["EXECUTEDATE"][DATE]  = $val["DAY"];
                    $data["GRADE"][TEXT]        = $grade;
                    $data["HR_CLASS"][TEXT]     = $hrClass;
                    $data["HR_CLASS_DIV"][TEXT] = $model->field["HR_CLASS_DIV"];
                }
                /* 更新データをセット */
                $data["HOLIDAY_FLG"][TEXT]  = $model->updEvent["HOLIDAY_FLG_".$val["DAY"]];
                $data["REMARK1"][TEXT]      = $model->updEvent["EVENT_".$val["DAY"]];
                $data["REGISTERCD"][TEXT]   = STAFFCD ;
                $data["UPDATED"][FUNC]      = "sysdate()";

                if ($flag) {
                    $query = Query::updateSQL($data, "EVENT_DAT", $where);
                } else {
                    $query = Query::insertSQL($data, "EVENT_DAT");
                }
                $db->query($query);
            }
        }

        return true;
    }

    function getUpdateSchChr($db, $model)
    {
        foreach ($model->updSchChr as $key => $val) {

            $delChair = $model->delChair[$val["DAY"]."_".$val["PERIODCD"]];

            //休日フラグがONの場合無条件削除
            if ($model->updEvent["HOLIDAY_FLG_".$val["DAY"]] == "1") {
                $query = "DELETE FROM SCH_CHR_DAT WHERE EXECUTEDATE = '{$val["DAY"]}' AND PERIODCD = '{$val["PERIODCD"]}' AND CHAIRCD = '{$delChair}'";
                $db->query($query);

                $query = "DELETE FROM SCH_CHR_DAT WHERE EXECUTEDATE = '{$val["DAY"]}' AND PERIODCD = '{$val["PERIODCD"]}' AND CHAIRCD = '{$val["CHAIRCD"]}'";
                $db->query($query);

                continue;
            }

            $query = "DELETE FROM SCH_CHR_DAT WHERE EXECUTEDATE = '{$val["DAY"]}' AND PERIODCD = '{$val["PERIODCD"]}' AND CHAIRCD = '{$delChair}'";
            $db->query($query);

            $query = "DELETE FROM SCH_CHR_DAT WHERE EXECUTEDATE = '{$val["DAY"]}' AND PERIODCD = '{$val["PERIODCD"]}' AND CHAIRCD = '{$val["CHAIRCD"]}'";
            $db->query($query);

            if (strlen($val["CHAIRCD"]) > 0) {
                $data["EXECUTEDATE"][DATE]  = $val["DAY"];
                $data["PERIODCD"][TEXT]     = $val["PERIODCD"];
                $data["CHAIRCD"][TEXT]      = $val["CHAIRCD"];
                $data["EXECUTED"][TEXT]     = "0";
                $data["DATADIV"][TEXT]      = "1";
                $data["YEAR"][TEXT]         = CTRL_YEAR;
                $data["SEMESTER"][TEXT]     = $val["SEME"];
                $data["REGISTERCD"][TEXT]   = STAFFCD ;
                $data["UPDATED"][FUNC]      = "sysdate()";

                $query = Query::insertSQL($data, "SCH_CHR_DAT");
                $db->query($query);
            }

        }

        PrimarySchoolProcess::delInsUnitSchChrRankDat($db, CTRL_YEAR);

        return true;
    }

    function getUpdateUnitDat($db, $model)
    {
        list($grade, $hrClass) = preg_split("/-/", $model->field["HR_CLASS"]);
        $changeUnit = preg_split("/:/", $model->changeVal);

        foreach ($model->updSchChr as $key => $val) {
            if (strlen(array_search($val["DAY"]."_".$val["PERIODCD"], $changeUnit)) > 0) {
                $query  = " SELECT ";
                $query .= "      UNITD.* ";
                $query .= " FROM ";
                $query .= "     UNIT_SCH_CHR_RANK_DAT SCH_CDAT ";
                $query .= "     INNER JOIN CHAIR_DAT CHAIR ON CHAIR.YEAR = SCH_CDAT.YEAR ";
                $query .= "                AND CHAIR.SEMESTER = SCH_CDAT.SEMESTER ";
                $query .= "                AND CHAIR.CHAIRCD = SCH_CDAT.CHAIRCD ";
                $query .= "     INNER JOIN UNIT_STUDY_TEXT_BOOK_DAT UNIT_TEXT ON UNIT_TEXT.YEAR = CHAIR.YEAR ";
                $query .= "                AND UNIT_TEXT.GRADE = '{$grade}' ";
                //教育課程対応
                if ($model->Properties["useCurriculumcd"] == '1') {
                    $query .= "            AND UNIT_TEXT.CLASSCD = CHAIR.CLASSCD ";
                    $query .= "            AND UNIT_TEXT.SCHOOL_KIND = CHAIR.SCHOOL_KIND ";
                    $query .= "            AND UNIT_TEXT.CURRICULUM_CD = CHAIR.CURRICULUM_CD ";
                }
                $query .= "                AND UNIT_TEXT.SUBCLASSCD = CHAIR.SUBCLASSCD, ";
                $query .= "     UNIT_DAT UNITD ";
                $query .= " WHERE ";
                $query .= "     SCH_CDAT.EXECUTEDATE = '{$val["DAY"]}' ";
                $query .= "     AND SCH_CDAT.PERIODCD = '{$val["PERIODCD"]}' ";
                $query .= "     AND SCH_CDAT.CHAIRCD = '{$val["CHAIRCD"]}' ";
                $query .= "     AND UNITD.YEAR = SCH_CDAT.YEAR ";
                $query .= "     AND UNITD.DATA_DIV = '2' ";
                $query .= "     AND UNITD.GRADE || '-' || UNITD.HR_CLASS = '{$model->field["HR_CLASS"]}' ";
                //教育課程対応
                if ($model->Properties["useCurriculumcd"] == '1') {
                    $query .= " AND UNITD.CLASSCD = CHAIR.CLASSCD ";
                    $query .= " AND UNITD.SCHOOL_KIND = CHAIR.SCHOOL_KIND ";
                    $query .= " AND UNITD.CURRICULUM_CD = CHAIR.CURRICULUM_CD ";
                }
                $query .= "     AND UNITD.SUBCLASSCD = CHAIR.SUBCLASSCD ";
                $query .= "     AND UNITD.SEQ = SCH_CDAT.RANK ";
                $query .= "     AND UNITD.ISSUECOMPANYCD = UNIT_TEXT.ISSUECOMPANYCD ";

                $unitKey = $db->getRow($query, DB_FETCHMODE_ASSOC);

                if (is_array($unitKey)) {
                    /* 更新データをセット */
                    $data["REMARK".$model->field["REMARK_SHOW"]][TEXT] = $val["REMARK"];
                    $data["REGISTERCD"][TEXT]   = STAFFCD ;
                    $data["UPDATED"][FUNC]      = "sysdate()";

                    $where  = " WHERE ";
                    $where .= "     YEAR = '".CTRL_YEAR."' ";
                    $where .= "     AND DATA_DIV = '2' ";
                    $where .= "     AND GRADE || '-' || HR_CLASS = '{$model->field["HR_CLASS"]}' ";
                    //教育課程対応
                    if ($model->Properties["useCurriculumcd"] == '1') {
                        $where .= " AND CLASSCD = '{$unitKey["CLASSCD"]}' ";
                        $where .= " AND SCHOOL_KIND = '{$unitKey["SCHOOL_KIND"]}' ";
                        $where .= " AND CURRICULUM_CD = '{$unitKey["CURRICULUM_CD"]}' ";
                    }
                    $where .= "     AND SUBCLASSCD = '{$unitKey["SUBCLASSCD"]}' ";
                    $where .= "     AND ISSUECOMPANYCD = '{$unitKey["ISSUECOMPANYCD"]}' ";
                    $where .= "     AND SEQ = {$unitKey["SEQ"]} ";

                    $query = Query::updateSQL($data, "UNIT_DAT", $where);

                    $db->query($query);
                }

            }
        }

        return true;
    }

    /* 分割更新 */
    function setBunkatuUpdate($db, $model)
    {
        $query = " FROM  WHERE EXECUTEDATE = '{}'";
        $query  = " DELETE FROM ";
        $query .= "     UNIT_SCH_CHR_BUNKATU_DAT ";
        $query .= " WHERE ";
        $query .= "     EXECUTEDATE = '{$model->bunKatuField["BUNKATU_DATE"]}' ";
        $query .= "     AND PERIODCD = '{$model->bunKatuField["BUNKATU_PERIOD"]}' ";
        $query .= "     AND MAIN_CHAIRCD = '{$model->bunKatuField["BUNKATU_CHAIRCD"]}' ";

        for ($i = 1; $i <= $model->bunkatuSu; $i++) {
            if ($model->bunKatuField["B_CHAIRCD".$i]) {
                $data["EXECUTEDATE"][DATE]  = $model->bunKatuField["BUNKATU_DATE"];
                $data["PERIODCD"][TEXT]     = $model->bunKatuField["BUNKATU_PERIOD"];
                $data["MAIN_CHAIRCD"][TEXT] = $model->bunKatuField["BUNKATU_CHAIRCD"];
                $data["SEQ"][NUMBER]        = $i;
                $data["CHAIRCD"][TEXT]      = $model->bunKatuField["B_CHAIRCD".$i];
                $data["MINUTE"][NUMBER]     = $model->bunKatuField["B_MINUTE".$i];
                $data["YEAR"][TEXT]         = CTRL_YEAR;
                $data["SEMESTER"][TEXT]     = $model->bunKatuField["BUNKATU_SEME"];
                $data["REGISTERCD"][TEXT]   = STAFFCD ;
                $data["UPDATED"][FUNC]      = "sysdate()";
                $query = Query::insertSQL($data, "UNIT_SCH_CHR_BUNKATU_DAT");
                $db->query($query);
            }
        }

        return true;
    }
}
?>
