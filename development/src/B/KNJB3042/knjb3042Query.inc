<?php

require_once('for_php7.php');

require_once("PrimarySchoolProcess.php");

class knjb3042Query extends Query
{
    //休日取得
    public function getHolidayMst($model)
    {
        $query .= " SELECT ";
        $query .= "     HOLIDAY ";
        $query .= " FROM ";
        $query .= "     HOLIDAY_MST ";
        $query .= " WHERE ";
        $query .= "     HOLIDAY BETWEEN '" . str_replace('/', '-', $model->startDate) . "' AND '" . str_replace('/', '-', $model->endDate) . "' ";

        return $query;
    }
    //校種取得
    public function getSchoolKind($model)
    {
        $query .= " SELECT ";
        $query .= "     NAME1 AS VALUE, ";
        $query .= "     ABBV1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = 'A023' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= "           AND NAME1 IN ('".implode(explode(':', $model->selectSchoolKind), "','")."') ";
            }
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= "     AND NAME1 = '".SCHOOLKIND."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     ABBV3,NAMECD2 ";
        $query .= "  ";

        return $query;
    }
    //年度学期
    public function getYearSemester()
    {
        $query .= " SELECT ";
        $query .= "     SEME.YEAR || '-' || SEME.SEMESTER AS VALUE, ";
        $query .= "     SEME.YEAR || '年度 ' || SEME.SEMESTERNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SEMESTER_MST SEME ";
        $query .= " WHERE ";
        $query .= "     SEME.YEAR IN ('".CTRL_YEAR."', '".(CTRL_YEAR + 1)."') ";
        $query .= "     AND SEME.SEMESTER < '9' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //学期
    public function getSemesterInfo($model)
    {
        $query .= " SELECT ";
        $query .= "     SEME.* ";
        $query .= " FROM ";
        $query .= "     SEMESTER_MST SEME ";
        $query .= " WHERE ";
        $query .= "     SEME.YEAR || '-' || SEME.SEMESTER = '{$model->yearSeme}' ";

        return $query;
    }

    //基本時間割
    public function getSchPtrnHdat($model)
    {
        $query  = " SELECT ";
        $query .= "     BSCSEQ, ";
        $query .= "     TITLE, ";
        $query .= "     UPDATED ";
        $query .= " FROM ";
        $query .= "     SCH_PTRN_HDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$model->year}' ";
        $query .= " AND SEMESTER = '{$model->semester}' ";
        $query .= " ORDER BY ";
        $query .= "     BSCSEQ ";

        return $query;
    }

    //校時
    public function getPeriod($model, $namecd)
    {
        $query  = " SELECT ";
        $query .= "     NAMECD2 AS VALUE, ";
        $query .= "     NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$model->year}' ";
        $query .= "     AND NAMECD1 = '{$namecd}' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //年組名取得
    public function getHrName($model)
    {
        $query .= " SELECT ";
        $query .= "     H1.HR_NAME AS LABEL, ";
        $query .= "     H1.GRADE || '-' || H1.HR_CLASS AS VALUE ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_HDAT H1 ";
        $query .= "     INNER JOIN SCHREG_REGD_GDAT REGDG ON REGDG.YEAR = H1.YEAR ";
        $query .= "         AND REGDG.GRADE = H1.GRADE ";
        $query .= " WHERE ";
        $query .= "     H1.YEAR = '{$model->year}' ";
        $query .= " AND H1.SEMESTER = '{$model->semester}' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= "           AND REGDG.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind), "','")."') ";
            }
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= "     AND REGDG.SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //群取得
    public function getGun($model)
    {
        $query .= " SELECT ";
        $query .= "     GROUPCD AS VALUE, ";
        $query .= "     GROUPNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     ELECTCLASS_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$model->year}' ";

        return $query;
    }

    //職員
    public function getStaff2($model)
    {
        $query .= " SELECT ";
        $query .= "     STF.STAFFCD AS VALUE, ";
        $query .= "     STF.STAFFNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     V_STAFF_MST STF ";
        $query .= " WHERE ";
        $query .= "     STF.YEAR = '{$model->year}' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= "           AND (STF.STAFFCD NOT IN (SELECT STAFFCD FROM USER_MST) ";
                $query .= "             OR STF.STAFFCD IN (SELECT STAFFCD FROM USER_MST WHERE SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind), "','")."')) ";
                $query .= "               ) ";
            }
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= "           AND (STF.STAFFCD NOT IN (SELECT STAFFCD FROM USER_MST) ";
            $query .= "             OR STF.STAFFCD IN (SELECT STAFFCD FROM USER_MST WHERE SCHOOL_KIND = '".SCHOOLKIND."') ";
            $query .= "               ) ";
        }
        $query .= " ORDER  BY ";
        $query .= "     STF.STAFFCD ";

        return $query;
    }

    //職員
    public function getStaff($model)
    {
        $query  = "";
        $query .= " WITH ";
        if ($model->field['SCH_DIV'] == '3') {
            $query .= " TUUJO(STAFFCD, STAFFNAME, SECTIONCD, SECTIONNAME, CHARGECLASSCD) AS ( ";
            $query .= " VALUES('**', '通常講座', '', '', '') ";
            $query .= " ), ";
        }
        $query .= " FUMEI(STAFFCD, STAFFNAME, SECTIONCD, SECTIONNAME, CHARGECLASSCD) AS ( ";
        $query .= " VALUES('--', '不明', '', '', '') ";
        $query .= " ) ";
        if ($model->field['SCH_DIV'] == '3') {
            $query .= " SELECT ";
            $query .= "     '0' AS ORDERDIV, ";
            $query .= "     STAFFCD, ";
            $query .= "     STAFFNAME, ";
            $query .= "     SECTIONCD, ";
            $query .= "     SECTIONNAME, ";
            $query .= "     CHARGECLASSCD ";
            $query .= " FROM ";
            $query .= "     TUUJO ";
            $query .= " UNION ";
        }
        $query .= " SELECT ";
        $query .= "     '0' AS ORDERDIV, ";
        $query .= "     STAFFCD, ";
        $query .= "     STAFFNAME, ";
        $query .= "     SECTIONCD, ";
        $query .= "     SECTIONNAME, ";
        $query .= "     CHARGECLASSCD ";
        $query .= " FROM ";
        $query .= "     FUMEI ";
        $query .= " UNION ";
        $query .= " SELECT ";
        $query .= "     '1' AS ORDERDIV, ";
        $query .= "     STF.STAFFCD, ";
        $query .= "     STF.STAFFNAME_SHOW AS STAFFNAME, ";
        $query .= "     STF.SECTIONCD, ";
        $query .= "     SEC.SECTIONABBV AS SECTIONNAME, ";
        $query .= "     STF.CHARGECLASSCD ";
        $query .= " FROM ";
        $query .= "     V_STAFF_MST STF ";
        $query .= "     LEFT JOIN SECTION_MST SEC ON STF.SECTIONCD = SEC.SECTIONCD ";
        $query .= " WHERE ";
        $query .= "     STF.YEAR = '{$model->year}' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= "           AND (STF.STAFFCD NOT IN (SELECT STAFFCD FROM USER_MST) ";
                $query .= "             OR STF.STAFFCD IN (SELECT STAFFCD FROM USER_MST WHERE SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind), "','")."')) ";
                $query .= "               ) ";
            }
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= "           AND (STF.STAFFCD NOT IN (SELECT STAFFCD FROM USER_MST) ";
            $query .= "             OR STF.STAFFCD IN (SELECT STAFFCD FROM USER_MST WHERE SCHOOL_KIND = '".SCHOOLKIND."') ";
            $query .= "               ) ";
        }
        $query .= " ORDER  BY ";
        $query .= "     ORDERDIV, ";
        $query .= "     STAFFCD ";

        return $query;
    }

    //制限付き権限ユーザーの左メニュー 職員
    public function getStaffLimitUser($model)
    {
        $query  = "";
        $query .= " WITH ";
        if ($model->field['SCH_DIV'] == '3') {
            $query .= " TUUJO(STAFFCD) AS ( ";
            $query .= " VALUES('**') ";
            $query .= " ), ";
        }
        $query .= " FUMEI(STAFFCD) AS ( ";
        $query .= " VALUES('--') ";
        $query .= " ) ";
        if ($model->field['SCH_DIV'] == '3') {
            $query .= " SELECT ";
            $query .= "     STAFFCD ";
            $query .= " FROM ";
            $query .= "     TUUJO ";
            $query .= " UNION ";
        }
        $query .= " SELECT DISTINCT ";
        $query .= "     STAFFCD ";
        $query .= " FROM ";
        $query .= "     CHAIR_STF_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$model->year}' ";
        $query .= "     AND SEMESTER = '{$model->semester}' ";
        $query .= "     AND CHAIRCD IN (SELECT ";
        $query .= "                         CHAIRCD ";
        $query .= "                     FROM ";
        $query .= "                         CHAIR_STF_DAT ";
        $query .= "                     WHERE ";
        $query .= "                         YEAR = '{$model->year}' ";
        $query .= "                         AND SEMESTER = '{$model->semester}' ";
        $query .= "                         AND STAFFCD = '" . STAFFCD . "' ";
        $query .= "                    ) ";

        return $query;
    }

    //職員OR講座ajax
    public function getLayoutStaffChair($model)
    {
        if ($model->leftMenu == '1') {
            $query  = " WITH FUMEI(STAFFCD, STAFFNAME) AS ( ";
            $query .= "     VALUES('--', '不明') ";
            $query .= " ), FUMEIKOMI AS ( ";
            $query .= "     SELECT ";
            $query .= "         '{$model->year}' AS YEAR, ";
            $query .= "         '0' AS ORDERDIV, ";
            $query .= "         STAFFCD, ";
            $query .= "         STAFFNAME ";
            $query .= "     FROM ";
            $query .= "         FUMEI ";
            $query .= "     UNION ";
            $query .= "     SELECT ";
            $query .= "         STF.YEAR, ";
            $query .= "         '1' AS ORDERDIV, ";
            $query .= "         STF.STAFFCD, ";
            $query .= "         STF.STAFFNAME ";
            $query .= "     FROM ";
            $query .= "         V_STAFF_MST STF ";
            $query .= "     WHERE ";
            $query .= "         STF.YEAR = '{$model->year}' ";
            if ($model->Properties["use_prg_schoolkind"] == "1") {
                if ($model->selectSchoolKind) {
                    $query .= "           AND (STF.STAFFCD NOT IN (SELECT STAFFCD FROM USER_MST) ";
                    $query .= "             OR STF.STAFFCD IN (SELECT STAFFCD FROM USER_MST WHERE SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind), "','")."')) ";
                    $query .= "               ) ";
                }
            } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
                $query .= "           AND (STF.STAFFCD NOT IN (SELECT STAFFCD FROM USER_MST) ";
                $query .= "             OR STF.STAFFCD IN (SELECT STAFFCD FROM USER_MST WHERE SCHOOL_KIND = '".SCHOOLKIND."') ";
                $query .= "               ) ";
            }
            $query .= " ) ";
            $query .= " SELECT ";
            $query .= "     STF.ORDERDIV, ";
            $query .= "     STF.STAFFCD AS VALUE, ";
            $query .= "     STF.STAFFNAME AS LABEL ";
            $query .= " FROM ";
            $query .= "     FUMEIKOMI STF ";
            $query .= " WHERE ";
            $query .= "     STF.YEAR = '{$model->year}' ";
            if ($model->ajaxParam["SCHOOL_KIND_STAFF"]) {
                $query .= "     AND STF.STAFFCD IN ( ";
                $query .= "         SELECT ";
                $query .= "             CSTF.STAFFCD ";
                $query .= "         FROM ";
                $query .= "             CHAIR_DAT CHAIRD ";
                $query .= "             INNER JOIN CHAIR_STF_DAT CSTF ON CHAIRD.YEAR = CSTF.YEAR ";
                $query .= "                   AND CHAIRD.SEMESTER = CSTF.SEMESTER ";
                $query .= "                   AND CHAIRD.CHAIRCD = CSTF.CHAIRCD ";
                $query .= "         WHERE ";
                $query .= "             CHAIRD.YEAR = '{$model->year}' ";
                $query .= "             AND CHAIRD.SEMESTER = '{$model->semester}' ";
                $query .= "             AND SCHOOL_KIND = '{$model->ajaxParam["SCHOOL_KIND_STAFF"]}' ";
                $query .= "     ) ";
            }
            if ($model->ajaxParam["GRAND_HR_CLASSCD_STAFF"]) {
                $query .= "     AND STF.STAFFCD IN ( ";
                $query .= "         SELECT ";
                $query .= "             CSTF.STAFFCD ";
                $query .= "         FROM ";
                $query .= "             CHAIR_CLS_DAT CCLS ";
                $query .= "             INNER JOIN CHAIR_STF_DAT CSTF ON CCLS.YEAR = CSTF.YEAR ";
                $query .= "                   AND CCLS.SEMESTER = CSTF.SEMESTER ";
                $query .= "                   AND CCLS.CHAIRCD = CSTF.CHAIRCD ";
                $query .= "         WHERE ";
                $query .= "             CCLS.YEAR = '{$model->year}' ";
                $query .= "             AND CCLS.SEMESTER = '{$model->semester}' ";
                $query .= "             AND CCLS.TRGTGRADE || '-' || CCLS.TRGTCLASS = '{$model->ajaxParam["GRAND_HR_CLASSCD_STAFF"]}' ";
                $query .= "             AND VALUE(CCLS.GROUPCD, '0000') = '0000' ";
                $query .= "         UNION ";
                $query .= "         SELECT ";
                $query .= "             CSTF.STAFFCD ";
                $query .= "         FROM ";
                $query .= "             CHAIR_CLS_DAT CCLS ";
                $query .= "             INNER JOIN CHAIR_DAT CHAIRD ON CCLS.YEAR = CHAIRD.YEAR ";
                $query .= "                   AND CCLS.SEMESTER = CHAIRD.SEMESTER ";
                $query .= "                   AND CCLS.GROUPCD = CHAIRD.GROUPCD ";
                $query .= "             INNER JOIN CHAIR_STF_DAT CSTF ON CHAIRD.YEAR = CSTF.YEAR ";
                $query .= "                   AND CHAIRD.SEMESTER = CSTF.SEMESTER ";
                $query .= "                   AND CHAIRD.CHAIRCD = CSTF.CHAIRCD ";
                $query .= "         WHERE ";
                $query .= "             CCLS.YEAR = '{$model->year}' ";
                $query .= "             AND CCLS.SEMESTER = '{$model->semester}' ";
                $query .= "             AND CCLS.TRGTGRADE || '-' || CCLS.TRGTCLASS = '{$model->ajaxParam["GRAND_HR_CLASSCD_STAFF"]}' ";
                $query .= "             AND VALUE(CCLS.GROUPCD, '0000') != '0000' ";
                $query .= "     ) ";
            }
            if ($model->ajaxParam["SUBCLASSCD_STAFF"]) {
                $query .= "     AND STF.STAFFCD IN ( ";
                $query .= "         SELECT ";
                $query .= "             CSTF.STAFFCD ";
                $query .= "         FROM ";
                $query .= "             CHAIR_DAT CHAIRD ";
                $query .= "             INNER JOIN CHAIR_STF_DAT CSTF ON CHAIRD.YEAR = CSTF.YEAR ";
                $query .= "                   AND CHAIRD.SEMESTER = CSTF.SEMESTER ";
                $query .= "                   AND CHAIRD.CHAIRCD = CSTF.CHAIRCD ";
                $query .= "         WHERE ";
                $query .= "             CHAIRD.YEAR = '{$model->year}' ";
                $query .= "             AND CHAIRD.SEMESTER = '{$model->semester}' ";
                $query .= "             AND CHAIRD.CLASSCD || '-' || CHAIRD.SCHOOL_KIND || '-' || CHAIRD.CURRICULUM_CD || '-' || CHAIRD.SUBCLASSCD = '{$model->ajaxParam["SUBCLASSCD_STAFF"]}' ";
                $query .= "     ) ";
            }
            if ($model->ajaxParam["GUNCD_STAFF"]) {
                $query .= "     AND STF.STAFFCD IN ( ";
                $query .= "         SELECT ";
                $query .= "             CSTF.STAFFCD ";
                $query .= "         FROM ";
                $query .= "             CHAIR_DAT CHAIRD ";
                $query .= "             INNER JOIN CHAIR_STF_DAT CSTF ON CHAIRD.YEAR = CSTF.YEAR ";
                $query .= "                   AND CHAIRD.SEMESTER = CSTF.SEMESTER ";
                $query .= "                   AND CHAIRD.CHAIRCD = CSTF.CHAIRCD ";
                $query .= "         WHERE ";
                $query .= "             CHAIRD.YEAR = '{$model->year}' ";
                $query .= "             AND CHAIRD.SEMESTER = '{$model->semester}' ";
                $query .= "             AND CHAIRD.GROUPCD = '{$model->ajaxParam["GUNCD_STAFF"]}' ";
                $query .= "     ) ";
            }
            $query .= " ORDER  BY ";
            $query .= "     ORDERDIV, ";
            $query .= "     VALUE ";
        } else {
            $query  = " SELECT ";
            $query .= "     CHAIR.CHAIRCD AS VALUE, ";
            $query .= "     CHAIR.CHAIRNAME AS LABEL ";
            $query .= " FROM ";
            $query .= "     CHAIR_DAT CHAIR ";
            $query .= " WHERE ";
            $query .= "     CHAIR.YEAR || '-' || CHAIR.SEMESTER = '{$model->yearSeme}' ";
            if ($model->ajaxParam["SCHOOL_KIND_STAFF"]) {
                $query .= "     AND CHAIR.CHAIRCD IN ( ";
                $query .= "         SELECT ";
                $query .= "             CHAIRD.CHAIRCD ";
                $query .= "         FROM ";
                $query .= "             CHAIR_DAT CHAIRD ";
                $query .= "         WHERE ";
                $query .= "             CHAIRD.YEAR = '{$model->year}' ";
                $query .= "             AND CHAIRD.SEMESTER = '{$model->semester}' ";
                $query .= "             AND SCHOOL_KIND = '{$model->ajaxParam["SCHOOL_KIND_STAFF"]}' ";
                $query .= "     ) ";
            }
            if ($model->ajaxParam["GRAND_HR_CLASSCD_STAFF"]) {
                $query .= "     AND CHAIR.CHAIRCD IN ( ";
                $query .= "         SELECT ";
                $query .= "             CCLS.CHAIRCD ";
                $query .= "         FROM ";
                $query .= "             CHAIR_CLS_DAT CCLS ";
                $query .= "         WHERE ";
                $query .= "             CCLS.YEAR = '{$model->year}' ";
                $query .= "             AND CCLS.SEMESTER = '{$model->semester}' ";
                $query .= "             AND CCLS.TRGTGRADE || '-' || CCLS.TRGTCLASS = '{$model->ajaxParam["GRAND_HR_CLASSCD_STAFF"]}' ";
                $query .= "             AND VALUE(CCLS.GROUPCD, '0000') = '0000' ";
                $query .= "         UNION ";
                $query .= "         SELECT ";
                $query .= "             CCLS.CHAIRCD ";
                $query .= "         FROM ";
                $query .= "             CHAIR_CLS_DAT CCLS ";
                $query .= "             INNER JOIN CHAIR_DAT CHAIRD ON CCLS.YEAR = CHAIRD.YEAR ";
                $query .= "                   AND CCLS.SEMESTER = CHAIRD.SEMESTER ";
                $query .= "                   AND CCLS.GROUPCD = CHAIRD.GROUPCD ";
                $query .= "         WHERE ";
                $query .= "             CCLS.YEAR = '{$model->year}' ";
                $query .= "             AND CCLS.SEMESTER = '{$model->semester}' ";
                $query .= "             AND CCLS.TRGTGRADE || '-' || CCLS.TRGTCLASS = '{$model->ajaxParam["GRAND_HR_CLASSCD_STAFF"]}' ";
                $query .= "             AND VALUE(CCLS.GROUPCD, '0000') != '0000' ";
                $query .= "     ) ";
            }
            if ($model->ajaxParam["SUBCLASSCD_STAFF"]) {
                $query .= "     AND CHAIR.CHAIRCD IN ( ";
                $query .= "         SELECT ";
                $query .= "             CHAIRD.CHAIRCD ";
                $query .= "         FROM ";
                $query .= "             CHAIR_DAT CHAIRD ";
                $query .= "         WHERE ";
                $query .= "             CHAIRD.YEAR = '{$model->year}' ";
                $query .= "             AND CHAIRD.SEMESTER = '{$model->semester}' ";
                $query .= "             AND CHAIRD.CLASSCD || '-' || CHAIRD.SCHOOL_KIND || '-' || CHAIRD.CURRICULUM_CD || '-' || CHAIRD.SUBCLASSCD = '{$model->ajaxParam["SUBCLASSCD_STAFF"]}' ";
                $query .= "     ) ";
            }
            if ($model->ajaxParam["GUNCD_STAFF"]) {
                $query .= "     AND CHAIR.CHAIRCD IN ( ";
                $query .= "         SELECT ";
                $query .= "             CHAIRD.CHAIRCD ";
                $query .= "         FROM ";
                $query .= "             CHAIR_DAT CHAIRD ";
                $query .= "         WHERE ";
                $query .= "             CHAIRD.YEAR = '{$model->year}' ";
                $query .= "             AND CHAIRD.SEMESTER = '{$model->semester}' ";
                $query .= "             AND CHAIRD.GROUPCD = '{$model->ajaxParam["GUNCD_STAFF"]}' ";
                $query .= "     ) ";
            }
            $query .= " ORDER  BY ";
            $query .= "     CHAIR.CHAIRCD ";
        }

        return $query;
    }

    //年組OR科目ajax
    public function getLayoutHrSubclass($model)
    {
        if ($model->leftMenu == '2') {
            $query  = " WITH FUMEI(VALUE, LABEL) AS ( ";
            $query .= "     VALUES('--', '不明') ";
            $query .= " ), FUMEIKOMI AS ( ";
            $query .= " SELECT ";
            $query .= "     '0' AS ORDERDIV, ";
            $query .= "     '' AS SCHOOL_KIND, ";
            $query .= "     VALUE, ";
            $query .= "     LABEL ";
            $query .= " FROM ";
            $query .= "     FUMEI ";
            $query .= " UNION ";
            $query .= " SELECT ";
            $query .= "     '1' AS ORDERDIV, ";
            $query .= "     REGDG.SCHOOL_KIND, ";
            $query .= "     REGDH.GRADE || REGDH.HR_CLASS AS VALUE, ";
            $query .= "     REGDH.HR_NAME AS LABEL ";
            $query .= " FROM ";
            $query .= "     SCHREG_REGD_HDAT REGDH ";
            $query .= "     INNER JOIN SCHREG_REGD_GDAT REGDG ON REGDH.YEAR = REGDG.YEAR ";
            $query .= "           AND REGDH.GRADE = REGDG.GRADE ";
            $query .= " WHERE ";
            $query .= "     REGDH.YEAR = '{$model->year}' ";
            $query .= "     AND REGDH.SEMESTER = '{$model->semester}' ";
            $query .= " ) ";
            $query .= " SELECT ";
            $query .= "     FUMEIKOMI.ORDERDIV, ";
            $query .= "     FUMEIKOMI.VALUE, ";
            $query .= "     FUMEIKOMI.LABEL ";
            $query .= " FROM ";
            $query .= "     FUMEIKOMI ";
            if ($model->Properties["use_prg_schoolkind"] == "1") {
                if ($model->selectSchoolKind) {
                    $query .= " WHERE ";
                    $query .= "     FUMEIKOMI.VALUE = '--' ";
                    $query .= "     OR ";
                    $query .= "           FUMEIKOMI.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind), "','")."') ";
                }
            } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
                $query .= " WHERE ";
                $query .= "     FUMEIKOMI.VALUE = '--' ";
                $query .= "     OR ";
                $query .= "     FUMEIKOMI.SCHOOL_KIND = '".SCHOOLKIND."' ";
            }
            $query .= " ORDER BY ";
            $query .= "     ORDERDIV, ";
            $query .= "     VALUE ";
        } else {
            $query  = " WITH FUMEI(VALUE, LABEL) AS ( ";
            $query .= "     VALUES('--', '不明') ";
            $query .= " ), FUMEIKOMI AS ( ";
            $query .= " SELECT ";
            $query .= "     '0' AS ORDERDIV, ";
            $query .= "     '' AS SCHOOL_KIND, ";
            $query .= "     VALUE, ";
            $query .= "     LABEL ";
            $query .= " FROM ";
            $query .= "     FUMEI ";
            $query .= " UNION ";
            $query .= " SELECT DISTINCT ";
            $query .= "     '1' AS ORDERDIV, ";
            $query .= "     CHAIR.SCHOOL_KIND, ";
            $query .= "     CHAIR.CLASSCD || '-' || CHAIR.SCHOOL_KIND || '-' || CHAIR.CURRICULUM_CD || '-' || CHAIR.SUBCLASSCD AS VALUE, ";
            $query .= "     SUBCLASS.SUBCLASSNAME AS LABEL ";
            $query .= " FROM ";
            $query .= "     CHAIR_DAT CHAIR ";
            $query .= "     INNER JOIN SUBCLASS_MST SUBCLASS ON CHAIR.CLASSCD = SUBCLASS.CLASSCD ";
            $query .= "           AND CHAIR.SCHOOL_KIND = SUBCLASS.SCHOOL_KIND ";
            $query .= "           AND CHAIR.CURRICULUM_CD = SUBCLASS.CURRICULUM_CD ";
            $query .= "           AND CHAIR.SUBCLASSCD = SUBCLASS.SUBCLASSCD ";
            $query .= " WHERE ";
            $query .= "     CHAIR.YEAR = '{$model->year}' ";
            $query .= "     AND CHAIR.SEMESTER = '{$model->semester}' ";
            $query .= " ) ";
            $query .= " SELECT DISTINCT ";
            $query .= "     '1' AS ORDERDIV, ";
            $query .= "     FUMEIKOMI.VALUE, ";
            $query .= "     FUMEIKOMI.LABEL ";
            $query .= " FROM ";
            $query .= "     FUMEIKOMI ";
            $where = "";
            if ($model->ajaxParam["SCHOOL_KIND_HRSUBCLASS"]) {
                if ($where == "") {
                    $where = " WHERE ";
                } else {
                    $where = " AND ";
                }
                $query .= $where;
                $query .= "     FUMEIKOMI.SCHOOL_KIND = '".$model->ajaxParam["SCHOOL_KIND_HRSUBCLASS"]."' ";
            }
            if ($model->Properties["use_prg_schoolkind"] == "1") {
                if ($model->selectSchoolKind) {
                    if ($where == "") {
                        $where = " WHERE ";
                    } else {
                        $where = " AND ";
                    }
                    $query .= $where;
                    $query .= "     FUMEIKOMI.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind), "','")."') ";
                }
            } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
                if ($where == "") {
                    $where = " WHERE ";
                } else {
                    $where = " AND ";
                }
                $query .= $where;
                $query .= "     FUMEIKOMI.SCHOOL_KIND = '".SCHOOLKIND."' ";
            }
            $query .= " ORDER BY ";
            $query .= "     ORDERDIV, ";
            $query .= "     VALUE ";
        }

        return $query;
    }

    //職員講座
    public function getStaffChair($model)
    {
        $query .= " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     CHAIR_STF_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$model->year}' ";
        $query .= "     AND SEMESTER = '{$model->semester}' ";
        $query .= " ORDER BY ";
        $query .= "     STAFFCD, ";
        $query .= "     CHAIRCD ";

        return $query;
    }

    //講座
    public function getChairAll($model)
    {
        $query  = " SELECT ";
        $query .= "     CHAIR.CHAIRCD ";
        $query .= "     , CHAIR.CHAIRNAME ";
        $query .= "     , CHAIR.CHAIRABBV ";
        $query .= " FROM ";
        $query .= "     CHAIR_DAT CHAIR ";
        $query .= " WHERE ";
        $query .= "     CHAIR.YEAR = '{$model->year}' ";
        $query .= "     AND CHAIR.SEMESTER = '{$model->semester}' ";

        return $query;
    }

    //年組
    public function getHrClassSql($model)
    {
        $query  = " WITH ";
        if ($model->field['SCH_DIV'] == '3') {
            $query .= " TUUJO(GRADE, HR_CLASS, HR_NAME) AS ( ";
            $query .= " VALUES('*', '*', '通常講座') ";
            $query .= " ), ";
        }
        $query .= " FUMEI(GRADE, HR_CLASS, HR_NAME) AS ( ";
        $query .= " VALUES('-', '-', '不明') ";
        $query .= " ) ";
        if ($model->field['SCH_DIV'] == '3') {
            $query .= " SELECT ";
            $query .= "     '0' AS ORDERDIV, ";
            $query .= "     GRADE, ";
            $query .= "     HR_CLASS, ";
            $query .= "     HR_NAME ";
            $query .= " FROM ";
            $query .= "     TUUJO ";
            $query .= " UNION ";
        }
        $query .= " SELECT ";
        $query .= "     '0' AS ORDERDIV, ";
        $query .= "     GRADE, ";
        $query .= "     HR_CLASS, ";
        $query .= "     HR_NAME ";
        $query .= " FROM ";
        $query .= "     FUMEI ";
        $query .= " UNION ";
        $query .= " SELECT ";
        $query .= "     '1' AS ORDERDIV, ";
        $query .= "     REGDH.GRADE, ";
        $query .= "     REGDH.HR_CLASS, ";
        $query .= "     REGDH.HR_NAME ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_HDAT REGDH ";
        $query .= "     INNER JOIN SCHREG_REGD_GDAT REGDG ON REGDH.YEAR = REGDG.YEAR ";
        $query .= "           AND REGDH.GRADE = REGDG.GRADE ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= "           AND REGDG.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind), "','")."') ";
            }
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= "     AND REGDG.SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= " WHERE ";
        $query .= "     REGDH.YEAR = '{$model->year}' ";
        $query .= "     AND REGDH.SEMESTER = '{$model->semester}' ";
        $query .= " ORDER BY ";
        $query .= "     ORDERDIV, ";
        $query .= "     GRADE, ";
        $query .= "     HR_CLASS ";

        return $query;
    }

    //制限付き権限ユーザーの左メニュー 年組
    public function getHrClassSqlLimitUser($model)
    {
        $query  = " WITH ";
        if ($model->field['SCH_DIV'] == '3') {
            $query .= " TUUJO(GRADE, HR_CLASS) AS ( ";
            $query .= " VALUES('*', '*') ";
            $query .= " ), ";
        }
        $query .= " FUMEI(GRADE, HR_CLASS) AS ( ";
        $query .= " VALUES('-', '-') ";
        $query .= " ) ";
        if ($model->field['SCH_DIV'] == '3') {
            $query .= " SELECT ";
            $query .= "     GRADE, ";
            $query .= "     HR_CLASS ";
            $query .= " FROM ";
            $query .= "     TUUJO ";
            $query .= " UNION ";
        }
        $query .= " SELECT ";
        $query .= "     GRADE, ";
        $query .= "     HR_CLASS ";
        $query .= " FROM ";
        $query .= "     FUMEI ";
        $query .= " UNION ";
        $query .= " SELECT ";
        $query .= "     GRADE, ";
        $query .= "     HR_CLASS ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_HDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR='{$model->year}' AND ";
        $query .= "     SEMESTER='{$model->semester}' ";
        $query .= "     AND  ";
        $query .= "     ( ";
        $query .= "         TR_CD1 = '" . STAFFCD . "' OR  ";
        $query .= "         TR_CD2 = '" . STAFFCD . "' OR  ";
        $query .= "         TR_CD3 = '" . STAFFCD . "' OR  ";
        $query .= "         SUBTR_CD1 = '" . STAFFCD . "' OR  ";
        $query .= "         SUBTR_CD2 = '" . STAFFCD . "' OR  ";
        $query .= "         SUBTR_CD3 = '" . STAFFCD . "' ";
        $query .= "     ) ";

        return $query;
    }

    //講座受講クラス
    public function getChairClassSql($model)
    {
        $query  = " SELECT ";
        $query .= "     CHAIRCD, ";
        $query .= "     TRGTGRADE, ";
        $query .= "     TRGTCLASS ";
        $query .= " FROM ";
        $query .= "     CHAIR_CLS_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$model->year}' ";
        $query .= "     AND SEMESTER = '{$model->semester}' ";
        $query .= "     AND GROUPCD = '0000' ";
        $query .= " UNION ";
        $query .= " SELECT ";
        $query .= "     CHAIR_D.CHAIRCD, ";
        $query .= "     CHAIR_CLS.TRGTGRADE, ";
        $query .= "     CHAIR_CLS.TRGTCLASS ";
        $query .= " FROM ";
        $query .= "     CHAIR_CLS_DAT CHAIR_CLS ";
        $query .= "     INNER JOIN CHAIR_DAT CHAIR_D ON CHAIR_CLS.YEAR = CHAIR_D.YEAR ";
        $query .= "           AND CHAIR_CLS.SEMESTER = CHAIR_D.SEMESTER ";
        $query .= "           AND CHAIR_CLS.GROUPCD = CHAIR_D.GROUPCD ";
        $query .= " WHERE ";
        $query .= "     CHAIR_CLS.YEAR = '{$model->year}' ";
        $query .= "     AND CHAIR_CLS.SEMESTER = '{$model->semester}' ";
        $query .= "     AND CHAIR_CLS.GROUPCD != '0000' ";

        return $query;
    }

    //科目-講座
    public function getChairSubclass($model)
    {
        $query .= " SELECT DISTINCT ";
        $query .= "     CHAIR.CHAIRCD, ";
        $query .= "     CHAIR.CLASSCD || '-' || CHAIR.SCHOOL_KIND || '-' || CHAIR.CURRICULUM_CD || '-' || CHAIR.SUBCLASSCD AS SUBCLASSCD ";
        $query .= " FROM ";
        $query .= "     CHAIR_DAT CHAIR ";
        $query .= " WHERE ";
        $query .= "     CHAIR.YEAR = '{$model->year}' ";
        $query .= "     AND CHAIR.SEMESTER = '{$model->semester}' ";
        $query .= " ORDER BY ";
        $query .= "     CHAIR.CHAIRCD ";

        return $query;
    }

    //登録データ取得
    public function getSchChair($model)
    {
        $setSdate = str_replace("/", "-", $model->startDate);
        $setEdate = str_replace("/", "-", $model->endDate);
        $query  = " WITH EXE_UNION AS ( ";
        $query .= " SELECT ";
        $query .= "     EXECUTEDATE, ";
        $query .= "     PERIODCD, ";
        $query .= "     CHAIRCD, ";
        $query .= "     '' AS GRADE, ";
        $query .= "     '' AS HR_CLASS, ";
        $query .= "     EXECUTED ";
        $query .= " FROM ";
        $query .= "     SCH_CHR_DAT ";
        $query .= " WHERE ";
        $query .= "     EXECUTEDATE BETWEEN '{$setSdate}' AND '{$setEdate}' ";
        $query .= " UNION ";
        $query .= " SELECT ";
        $query .= "     EXECUTEDATE, ";
        $query .= "     PERIODCD, ";
        $query .= "     CHAIRCD, ";
        $query .= "     GRADE, ";
        $query .= "     HR_CLASS, ";
        $query .= "     EXECUTED ";
        $query .= " FROM ";
        $query .= "     SCH_CHR_HRATE_DAT ";
        $query .= " WHERE ";
        $query .= "     EXECUTEDATE BETWEEN '{$setSdate}' AND '{$setEdate}' ";
        $query .= " ), EXE_T AS ( ";
        $query .= " SELECT ";
        $query .= "     EXECUTEDATE, ";
        $query .= "     PERIODCD, ";
        $query .= "     CHAIRCD, ";
        $query .= "     CASE WHEN COUNT(*) = SUM(CASE WHEN EXECUTED = '1' THEN 1 ELSE 0 END) ";
        $query .= "          THEN 'SYUKKETSU' ";
        $query .= "          ELSE CASE WHEN SUM(CASE WHEN EXECUTED = '1' THEN 1 ELSE 0 END) = 0 ";
        $query .= "                    THEN 'MI_SYUKKETSU' ";
        $query .= "                    ELSE 'ITIBU_SYUKKETSU' ";
        $query .= "               END ";
        $query .= "     END AS EXECUTED ";
        $query .= " FROM ";
        $query .= "     EXE_UNION ";
        $query .= " GROUP BY ";
        $query .= "     EXECUTEDATE, ";
        $query .= "     PERIODCD, ";
        $query .= "     CHAIRCD ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     SCH_CHR.EXECUTEDATE AS DATE_OR_WEEK, ";
        $query .= "     SCH_CHR.PERIODCD, ";
        $query .= "     SCH_CHR.CHAIRCD, ";
        $query .= "     CHAIR.CHAIRNAME, ";
        $query .= "     CHAIR.CHAIRABBV, ";
        $query .= "     SCH_CHR.DATADIV, ";
        $query .= "     TEST.TESTKINDCD, ";
        $query .= "     TEST.TESTITEMCD, ";
        $query .= "     EXE_T.EXECUTED ";
        $query .= " FROM ";
        $query .= "     SCH_CHR_DAT SCH_CHR ";
        $query .= "     LEFT JOIN CHAIR_DAT CHAIR ON SCH_CHR.YEAR = CHAIR.YEAR ";
        $query .= "          AND SCH_CHR.SEMESTER = CHAIR.SEMESTER ";
        $query .= "          AND SCH_CHR.CHAIRCD = CHAIR.CHAIRCD ";
        $query .= "     LEFT JOIN SCH_CHR_TEST TEST ON SCH_CHR.EXECUTEDATE = TEST.EXECUTEDATE ";
        $query .= "          AND SCH_CHR.PERIODCD = TEST.PERIODCD ";
        $query .= "          AND SCH_CHR.CHAIRCD = TEST.CHAIRCD ";
        $query .= "     LEFT JOIN EXE_T ON SCH_CHR.EXECUTEDATE = EXE_T.EXECUTEDATE ";
        $query .= "          AND SCH_CHR.PERIODCD = EXE_T.PERIODCD ";
        $query .= "          AND SCH_CHR.CHAIRCD = EXE_T.CHAIRCD ";
        $query .= " WHERE ";
        $query .= "     SCH_CHR.EXECUTEDATE BETWEEN '{$setSdate}' AND '{$setEdate}' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= "           AND CHAIR.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind), "','")."') ";
            }
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= "     AND CHAIR.SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     SCH_CHR.EXECUTEDATE, ";
        $query .= "     SCH_CHR.PERIODCD, ";
        $query .= "     SCH_CHR.CHAIRCD ";

        return $query;
    }

    //基本時間割
    public function getSchPtrn($model)
    {
        $query .= " SELECT ";
        $query .= "     PTRN_D.DAYCD AS DATE_OR_WEEK, ";
        $query .= "     PTRN_D.PERIODCD, ";
        $query .= "     PTRN_D.CHAIRCD, ";
        $query .= "     CHAIR.CHAIRNAME, ";
        $query .= "     CHAIR.CHAIRABBV ";
        $query .= " FROM ";
        $query .= "     SCH_PTRN_DAT PTRN_D ";
        $query .= "     LEFT JOIN CHAIR_DAT CHAIR ON PTRN_D.YEAR = CHAIR.YEAR ";
        $query .= "          AND PTRN_D.SEMESTER = CHAIR.SEMESTER ";
        $query .= "          AND PTRN_D.CHAIRCD = CHAIR.CHAIRCD ";
        $query .= " WHERE ";
        $query .= "     PTRN_D.YEAR = '{$model->year}' ";
        $query .= "     AND PTRN_D.SEMESTER = '{$model->semester}' ";
        $query .= "     AND PTRN_D.BSCSEQ = {$model->field['BSCSEQ']} ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= "           AND CHAIR.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind), "','")."') ";
            }
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= "     AND CHAIR.SCHOOL_KIND = '".SCHOOLKIND."' ";
        }

        return $query;
    }

    //講座変更データ取得
    public function getSchStaff($model)
    {
        $setSdate = str_replace("/", "-", $model->startDate);
        $setEdate = str_replace("/", "-", $model->endDate);
        $query .= " SELECT ";
        $query .= "     SCH_STF.EXECUTEDATE AS DATE_OR_WEEK, ";
        $query .= "     SCH_STF.PERIODCD, ";
        $query .= "     SCH_STF.CHAIRCD, ";
        $query .= "     SCH_STF.STAFFCD ";
        $query .= " FROM ";
        $query .= "     SCH_STF_DAT SCH_STF ";
        $query .= " WHERE ";
        $query .= "     SCH_STF.EXECUTEDATE BETWEEN '{$setSdate}' AND '{$setEdate}' ";
        $query .= " ORDER BY ";
        $query .= "     SCH_STF.EXECUTEDATE, ";
        $query .= "     SCH_STF.PERIODCD, ";
        $query .= "     SCH_STF.CHAIRCD ";

        return $query;
    }
    //講座変更データ取得
    public function getPtrnStaff($model)
    {
        $setSdate = str_replace("/", "-", $model->startDate);
        $setEdate = str_replace("/", "-", $model->endDate);
        $query .= " SELECT ";
        $query .= "     SCH_STF.DAYCD AS DATE_OR_WEEK, ";
        $query .= "     SCH_STF.PERIODCD, ";
        $query .= "     SCH_STF.CHAIRCD, ";
        $query .= "     SCH_STF.STAFFCD ";
        $query .= " FROM ";
        $query .= "     SCH_PTRN_STF_DAT SCH_STF ";
        $query .= " WHERE ";
        $query .= "     SCH_STF.YEAR = '{$model->year}' ";
        $query .= "     AND SCH_STF.SEMESTER = '{$model->semester}' ";
        $query .= "     AND SCH_STF.BSCSEQ = {$model->field['BSCSEQ']} ";
        $query .= " ORDER BY ";
        $query .= "     SCH_STF.DAYCD, ";
        $query .= "     SCH_STF.PERIODCD, ";
        $query .= "     SCH_STF.CHAIRCD ";

        return $query;
    }

    //科目
    public function getSubclass($model, $fumeiDisp)
    {
        $query  = "";
        if ($fumeiDisp) {
            $query  = " WITH ";
            if ($model->field['SCH_DIV'] == '3') {
                $query .= " TUUJO(SUBCLASSNAME, VALUE, LABEL) AS ( ";
                $query .= " VALUES('通常講座', '**', '**:通常講座') ";
                $query .= " ), ";
            }

            $query .= " FUMEI(SUBCLASSNAME, VALUE, LABEL) AS ( ";
            $query .= " VALUES('不明', '--', '--:不明') ";
            $query .= " ) ";
            if ($model->field['SCH_DIV'] == '3') {
                $query .= " SELECT ";
                $query .= "     '0' AS ORDERDIV, ";
                $query .= "     SUBCLASSNAME, ";
                $query .= "     VALUE, ";
                $query .= "     LABEL ";
                $query .= " FROM ";
                $query .= "     TUUJO ";
                $query .= " UNION ";
            }
            $query .= " SELECT ";
            $query .= "     '0' AS ORDERDIV, ";
            $query .= "     SUBCLASSNAME, ";
            $query .= "     VALUE, ";
            $query .= "     LABEL ";
            $query .= " FROM ";
            $query .= "     FUMEI ";
            $query .= " UNION ";
        }
        $query .= " SELECT DISTINCT ";
        $query .= "     '1' AS ORDERDIV, ";
        $query .= "     SUBCLASS.SUBCLASSNAME, ";
        $query .= "     CHAIR.CLASSCD || '-' || CHAIR.SCHOOL_KIND || '-' || CHAIR.CURRICULUM_CD || '-' || CHAIR.SUBCLASSCD AS VALUE, ";
        $query .= "     CHAIR.CLASSCD || '-' || CHAIR.SCHOOL_KIND || '-' || CHAIR.CURRICULUM_CD || '-' || CHAIR.SUBCLASSCD || ':' || SUBCLASS.SUBCLASSNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     CHAIR_DAT CHAIR ";
        $query .= "     INNER JOIN SUBCLASS_MST SUBCLASS ON CHAIR.CLASSCD = SUBCLASS.CLASSCD ";
        $query .= "           AND CHAIR.SCHOOL_KIND = SUBCLASS.SCHOOL_KIND ";
        $query .= "           AND CHAIR.CURRICULUM_CD = SUBCLASS.CURRICULUM_CD ";
        $query .= "           AND CHAIR.SUBCLASSCD = SUBCLASS.SUBCLASSCD ";
        $query .= " WHERE ";
        $query .= "     CHAIR.YEAR = '{$model->year}' ";
        $query .= "     AND CHAIR.SEMESTER = '{$model->semester}' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= "           AND CHAIR.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind), "','")."') ";
            }
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= "     AND CHAIR.SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     ORDERDIV, ";
        $query .= "     VALUE ";

        return $query;
    }

    //制限付き権限ユーザーの左メニュー 科目
    public function getSubclassLimitUser($model)
    {
        $query  = " WITH ";
        if ($model->field['SCH_DIV'] == '3') {
            $query .= " TUUJO(VALUE) AS ( ";
            $query .= " VALUES('**') ";
            $query .= " ), ";
        }
        $query .= " FUMEI(VALUE) AS ( ";
        $query .= " VALUES('--') ";
        $query .= " ) ";
        if ($model->field['SCH_DIV'] == '3') {
            $query .= " SELECT ";
            $query .= "     VALUE ";
            $query .= " FROM ";
            $query .= "     TUUJO ";
            $query .= " UNION ";
        }
        $query .= " SELECT ";
        $query .= "     VALUE ";
        $query .= " FROM ";
        $query .= "     FUMEI ";
        $query .= " UNION ";
        $query .= " SELECT DISTINCT ";
        $query .= "     CHAIR.CLASSCD || '-' || CHAIR.SCHOOL_KIND || '-' || CHAIR.CURRICULUM_CD || '-' || CHAIR.SUBCLASSCD AS VALUE  ";
        $query .= " FROM ";
        $query .= "     CHAIR_DAT CHAIR  ";
        $query .= "     INNER JOIN CHAIR_STF_DAT STF ";
        $query .= "         ON  CHAIR.YEAR = STF.YEAR ";
        $query .= "         AND CHAIR.SEMESTER = STF.SEMESTER ";
        $query .= "         AND CHAIR.CHAIRCD = STF.CHAIRCD ";
        $query .= " WHERE ";
        $query .= "     CHAIR.YEAR = '{$model->year}' ";
        $query .= "     AND CHAIR.SEMESTER = '{$model->semester}' ";
        $query .= "     AND STF.STAFFCD = '" . STAFFCD . "' ";

        return $query;
    }

    //講座
    public function getChair($model)
    {
        $removeIds = json_decode($model->removeIds);

        $query  = " SELECT DISTINCT ";
        $query .= "     CHAIR.CHAIRCD, ";
        $query .= "     CHAIR.CHAIRCD || ':' || RTRIM(CHAR(CASE WHEN CHAIR.FRAMECNT IS NULL THEN 1 ELSE CHAIR.FRAMECNT END))  || ':' || VALUE(CHAIR.CHAIRABBV, '') AS VALUE, ";
        $query .= "     CHAIR.CHAIRCD || ':' || CHAIR.CHAIRNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     CHAIR_DAT CHAIR ";
        $query .= " LEFT JOIN ";
        $query .= "     CHAIR_STF_DAT STF ";
        $query .= "         ON CHAIR.YEAR = STF.YEAR ";
        $query .= "         AND CHAIR.SEMESTER = STF.SEMESTER ";
        $query .= "         AND CHAIR.CHAIRCD = STF.CHAIRCD ";
        if ($model->grandHrClassCd) {
            $query .= " INNER JOIN ";
            $query .= " ( ";
            $query .= "     SELECT ";
            $query .= "         CHAIR.CHAIRCD, ";
            $query .= "         CHAIR.GROUPCD ";
            $query .= "     FROM ";
            $query .= "         CHAIR_DAT CHAIR ";
            $query .= "     INNER JOIN ";
            $query .= "         CHAIR_CLS_DAT CLS ON CHAIR.YEAR = CLS.YEAR ";
            $query .= "         AND CHAIR.SEMESTER = CLS.SEMESTER ";
            $query .= "         AND CLS.GROUPCD = '0000' ";
            $query .= "         AND CHAIR.CHAIRCD = CLS.CHAIRCD ";
            $query .= "     WHERE ";
            $query .= "         CHAIR.YEAR = '{$model->year}' ";
            $query .= "         AND CHAIR.SEMESTER = '{$model->semester}' ";
            $query .= "         AND CHAIR.GROUPCD = '0000' ";
            $query .= "         AND CLS.TRGTGRADE || '-' || CLS.TRGTCLASS = '{$model->grandHrClassCd}' ";
            $query .= "     UNION ";
            $query .= "     SELECT ";
            $query .= "         CHAIR.CHAIRCD, ";
            $query .= "         CHAIR.GROUPCD ";
            $query .= "     FROM ";
            $query .= "         CHAIR_DAT CHAIR ";
            $query .= "     INNER JOIN ";
            $query .= "         CHAIR_CLS_DAT CLS ON CHAIR.YEAR = CLS.YEAR ";
            $query .= "         AND CHAIR.SEMESTER = CLS.SEMESTER ";
            $query .= "         AND CLS.CHAIRCD = '0000000' ";
            $query .= "         AND CHAIR.GROUPCD = CLS.GROUPCD ";
            $query .= "     WHERE ";
            $query .= "         CHAIR.YEAR = '{$model->year}' ";
            $query .= "         AND CHAIR.GROUPCD != '0000' ";
            $query .= "         AND CHAIR.SEMESTER = '{$model->semester}' ";
            $query .= "         AND CLS.TRGTGRADE || '-' || CLS.TRGTCLASS = '{$model->grandHrClassCd}' ";
            $query .= " ) T1 ";
            $query .= "    ON CHAIR.CHAIRCD = T1.CHAIRCD ";
        }
        if ($model->couseCd) {
            $couseList = explode('-', $model->couseCd);

            $query .= " INNER JOIN ";
            $query .= "     CREDIT_MST CREDIT ";
            $query .= "     ON CREDIT.CLASSCD = CHAIR.CLASSCD ";
            $query .= "     AND CREDIT.SCHOOL_KIND = CHAIR.SCHOOL_KIND ";
            $query .= "     AND CREDIT.CURRICULUM_CD = CHAIR.CURRICULUM_CD ";
            $query .= "     AND CREDIT.SUBCLASSCD = CHAIR.SUBCLASSCD ";
            $query .= "     AND CREDIT.COURSECD = '{$couseList[0]}' ";
            $query .= "     AND CREDIT.MAJORCD = '{$couseList[1]}' ";
            $query .= "     AND CREDIT.GRADE = '{$couseList[2]}' ";
            $query .= "     AND CREDIT.COURSECODE = '{$couseList[3]}' ";
        }
        $query .= " WHERE ";
        $query .= "     CHAIR.YEAR = '{$model->year}' ";
        $query .= "     AND CHAIR.SEMESTER = '{$model->semester}' ";
        if ($model->subclassCd) {
            $query .= "     AND CHAIR.CLASSCD || '-' || CHAIR.SCHOOL_KIND || '-' || CHAIR.CURRICULUM_CD || '-' || CHAIR.SUBCLASSCD = '{$model->subclassCd}' ";
        }
        if ($model->gunCd) {
            $query .= "     AND CHAIR.GROUPCD  = '{$model->gunCd}' ";
        }
        if ($model->staffCd) {
            $query .= "     AND STF.STAFFCD = '{$model->staffCd}' AND STF.CHARGEDIV = 1 ";
        }
        if ($removeIds) {
            $query .= "     AND CHAIR.CHAIRCD NOT IN ('".implode("','", $removeIds)."')";
        }
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= " AND CHAIR.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind), "','")."') ";
            }
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND CHAIR.SCHOOL_KIND  = '".SCHOOLKIND."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }
    //講座
    public function getChair2($model, $tuujoDisp = false)
    {
        $query  = "";
        if ($tuujoDisp && $model->field['SCH_DIV'] == '3') {
            $query  = " WITH ";
            $query .= " TUUJO(CHAIRCD, FRAMECNT, CHAIRNAME) AS ( ";
            $query .= " VALUES('**', '1', '通常講座') ";
            $query .= " ) ";
            $query .= " SELECT ";
            $query .= "     CHAIRCD, ";
            $query .= "     FRAMECNT, ";
            $query .= "     CHAIRNAME ";
            $query .= " FROM ";
            $query .= "     TUUJO ";
            $query .= " UNION ";
        }
        $query .= " SELECT ";
        $query .= "     CHAIR.CHAIRCD, ";
        $query .= "     RTRIM(CHAR(CASE WHEN CHAIR.FRAMECNT IS NULL THEN 1 ELSE CHAIR.FRAMECNT END)) as FRAMECNT, ";
        $query .= "     CHAIR.CHAIRNAME ";
        $query .= " FROM ";
        $query .= "     CHAIR_DAT CHAIR ";
        $query .= " WHERE ";
        $query .= "     CHAIR.YEAR = '{$model->year}' ";
        $query .= "     AND CHAIR.SEMESTER = '{$model->semester}' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= " AND CHAIR.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind), "','")."') ";
            }
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND CHAIR.SCHOOL_KIND  = '".SCHOOLKIND."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     CHAIRCD ";

        return $query;
    }

    //制限付き権限ユーザーの左メニュー 講座
    public function getChairLimitUser($model)
    {
        $query  = "";
        if ($model->field['SCH_DIV'] == '3') {
            $query  = " WITH ";
            $query .= " TUUJO(CHAIRCD) AS ( ";
            $query .= " VALUES('**') ";
            $query .= " ) ";
            $query .= " SELECT ";
            $query .= "     CHAIRCD ";
            $query .= " FROM ";
            $query .= "     TUUJO ";
            $query .= " UNION ";
        }
        $query .= " SELECT ";
        $query .= "     CHAIRCD ";
        $query .= " FROM ";
        $query .= "     CHAIR_STF_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$model->year}' ";
        $query .= "     AND SEMESTER = '{$model->semester}' ";
        $query .= "     AND STAFFCD = '" . STAFFCD . "' ";

        return $query;
    }

    //テストコンボ
    public function getTestCombo($model)
    {
        if ($model->Properties["useTestCountflg"] == "TESTITEM_MST_COUNTFLG_NEW_SDIV") {
            $query  = " SELECT ";
            $query .= "     TESTKINDCD || TESTITEMCD AS VALUE, ";
            $query .= "     TESTKINDCD || TESTITEMCD || ':' || TESTITEMNAME AS LABEL, ";
            $query .= "     COUNTFLG ";
            $query .= " FROM ";
            $query .= "     TESTITEM_MST_COUNTFLG_NEW_SDIV ";
            $query .= " WHERE ";
            $query .= "     YEAR = '{$model->year}' ";
            $query .= "     AND SEMESTER = '{$model->semester}' ";
            $query .= "     AND SCORE_DIV = '01' ";
            $query .= "     AND TESTKINDCD IN ('01', '02') ";
        } elseif ($model->Properties["useTestCountflg"] == "TESTITEM_MST_COUNTFLG_NEW") {
            $query .= " SELECT ";
            $query .= "     TESTKINDCD || TESTITEMCD AS VALUE, ";
            $query .= "     TESTKINDCD || TESTITEMCD || ':' || TESTITEMNAME AS LABEL, ";
            $query .= "     COUNTFLG ";
            $query .= " FROM ";
            $query .= "     TESTITEM_MST_COUNTFLG_NEW ";
            $query .= " WHERE ";
            $query .= "     YEAR = '{$model->year}' ";
            $query .= "     AND SEMESTER = '{$model->semester}' ";
            $query .= "     AND TESTKINDCD IN ('01', '02') ";
        } else {
            $query .= " SELECT ";
            $query .= "     TESTKINDCD || TESTITEMCD AS VALUE, ";
            $query .= "     TESTKINDCD || TESTITEMCD || ':' || TESTITEMNAME AS LABEL, ";
            $query .= "     COUNTFLG ";
            $query .= " FROM ";
            $query .= "     TESTITEM_MST_COUNTFLG ";
            $query .= " WHERE ";
            $query .= "     YEAR = '{$model->year}' ";
            $query .= "     AND TESTKINDCD IN ('01', '02') ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //講座コンボ
    public function getCiChairDat($model)
    {
        $chairArray = explode(":", $model->chairInfo["SEND_KOMA_VAL"]);
        $query .= " SELECT ";
        $query .= "     CHAIR.COUNTFLG, ";
        $query .= "     CHAIR.CHAIRCD AS VALUE, ";
        $query .= "     CHAIR.CHAIRCD || ':' || CHAIR.CHAIRNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     CHAIR_DAT CHAIR ";
        $query .= " WHERE ";
        $query .= "     CHAIR.YEAR = '{$model->year}' ";
        $query .= "     AND CHAIR.SEMESTER = '{$model->semester}' ";
        $query .= "     AND CHAIR.CHAIRCD IN ('".implode("','", $chairArray)."') ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //科目情報
    public function getCiSubclass($model)
    {
        $query .= " SELECT DISTINCT ";
        $query .= "     CHAIR.CLASSCD || '-' || CHAIR.SCHOOL_KIND || '-' || CHAIR.CURRICULUM_CD || '-' || CHAIR.SUBCLASSCD || ':' || SUBCLASS.SUBCLASSNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     CHAIR_DAT CHAIR ";
        $query .= "     INNER JOIN SUBCLASS_MST SUBCLASS ON CHAIR.CLASSCD = SUBCLASS.CLASSCD ";
        $query .= "           AND CHAIR.SCHOOL_KIND = SUBCLASS.SCHOOL_KIND ";
        $query .= "           AND CHAIR.CURRICULUM_CD = SUBCLASS.CURRICULUM_CD ";
        $query .= "           AND CHAIR.SUBCLASSCD = SUBCLASS.SUBCLASSCD ";
        $query .= " WHERE ";
        $query .= "     CHAIR.YEAR = '{$model->year}' ";
        $query .= "     AND CHAIR.SEMESTER = '{$model->semester}' ";
        $query .= "     AND CHAIR.CHAIRCD = '{$model->ciField["CI_CHAIRCD"]}' ";

        return $query;
    }

    //講座情報
    public function getChairInfo($model)
    {
        $query  = " SELECT ";
        $query .= "     CHAIR.GROUPCD, ";
        $query .= "     ELC.GROUPNAME, ";
        $query .= "     CHAIR.LESSONCNT, ";
        $query .= "     CHAIR.FRAMECNT ";
        $query .= " FROM ";
        $query .= "     CHAIR_DAT CHAIR ";
        $query .= "     LEFT JOIN ELECTCLASS_DAT ELC ON ELC.YEAR = CHAIR.YEAR ";
        $query .= "          AND ELC.GROUPCD = CHAIR.GROUPCD ";
        $query .= " WHERE ";
        $query .= "     CHAIR.YEAR = '{$model->year}' ";
        $query .= "     AND CHAIR.SEMESTER = '{$model->semester}' ";
        $query .= "     AND CHAIR.CHAIRCD = '{$model->ciField["CI_CHAIRCD"]}' ";

        return $query;
    }

    //講座クラス
    public function getChairClass($model)
    {
        $query  = " SELECT ";
        $query .= "     CHAIR_CL.TRGTGRADE || '-' || CHAIR_CL.TRGTCLASS AS VALUE, ";
        $query .= "     REGD_H.HR_NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     CHAIR_DAT CHAIR ";
        $query .= "     LEFT JOIN CHAIR_CLS_DAT CHAIR_CL ON CHAIR_CL.YEAR = CHAIR.YEAR ";
        $query .= "          AND CHAIR_CL.SEMESTER = CHAIR.SEMESTER ";
        if ($model->chairInfo["GROUPCD"] > 0) {
            $query .= "          AND CHAIR_CL.GROUPCD = CHAIR.GROUPCD ";
        } else {
            $query .= "          AND CHAIR_CL.CHAIRCD = CHAIR.CHAIRCD ";
        }
        $query .= "     LEFT JOIN SCHREG_REGD_HDAT REGD_H ON REGD_H.YEAR = CHAIR_CL.YEAR ";
        $query .= "          AND REGD_H.SEMESTER = CHAIR_CL.SEMESTER ";
        $query .= "          AND REGD_H.GRADE = CHAIR_CL.TRGTGRADE ";
        $query .= "          AND REGD_H.HR_CLASS = CHAIR_CL.TRGTCLASS ";
        $query .= " WHERE ";
        $query .= "     CHAIR.YEAR = '{$model->year}' ";
        $query .= "     AND CHAIR.SEMESTER = '{$model->semester}' ";
        $query .= "     AND CHAIR.CHAIRCD = '{$model->ciField["CI_CHAIRCD"]}' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //時間割担当(通常
    public function getCiSchStaff($model)
    {
        $query .= " SELECT ";
        $query .= "     STF.STAFFNAME AS LABEL, ";
        $query .= "     SCH_STF.STAFFCD AS VALUE ";
        $query .= " FROM ";
        $query .= "     SCH_STF_DAT SCH_STF ";
        $query .= "     LEFT JOIN STAFF_MST STF ON STF.STAFFCD = SCH_STF.STAFFCD ";
        $query .= " WHERE ";
        $query .= "     SCH_STF.EXECUTEDATE = '{$model->chairInfo["SEND_DATE"]}' ";
        $query .= "     AND SCH_STF.PERIODCD = '{$model->periYomikae[$model->chairInfo["SEND_PERIOD"]]}' ";
        $query .= "     AND SCH_STF.CHAIRCD = '{$model->ciField["CI_CHAIRCD"]}' ";
        $query .= " ORDER BY ";
        $query .= "     SCH_STF.STAFFCD ";

        return $query;
    }

    //時間割担当(基本
    public function getCiPtrnStaff($model)
    {
        $query .= " SELECT ";
        $query .= "     STF.STAFFNAME AS LABEL, ";
        $query .= "     SCH_STF.STAFFCD AS VALUE ";
        $query .= " FROM ";
        $query .= "     SCH_PTRN_STF_DAT SCH_STF ";
        $query .= "     LEFT JOIN STAFF_MST STF ON STF.STAFFCD = SCH_STF.STAFFCD ";
        $query .= " WHERE ";
        $query .= "     SCH_STF.YEAR = '{$model->year}' ";
        $query .= "     AND SCH_STF.SEMESTER = '{$model->semester}' ";
        $query .= "     AND SCH_STF.BSCSEQ = {$model->field["BSCSEQ"]} ";
        $query .= "     AND SCH_STF.DAYCD = '{$model->chairInfo["SEND_DATE"]}' ";
        $query .= "     AND SCH_STF.PERIODCD = '{$model->periYomikae[$model->chairInfo["SEND_PERIOD"]]}' ";
        $query .= "     AND SCH_STF.CHAIRCD = '{$model->ciField["CI_CHAIRCD"]}' ";
        $query .= " ORDER BY ";
        $query .= "     SCH_STF.STAFFCD ";

        return $query;
    }

    //講座担当
    public function getCiChairStaff($model)
    {
        $query .= " SELECT ";
        $query .= "     STF.STAFFNAME AS LABEL, ";
        $query .= "     CHR_STF.STAFFCD AS VALUE ";
        $query .= " FROM ";
        $query .= "     CHAIR_STF_DAT CHR_STF ";
        $query .= "     LEFT JOIN STAFF_MST STF ON STF.STAFFCD = CHR_STF.STAFFCD ";
        $query .= " WHERE ";
        $query .= "     CHR_STF.YEAR = '{$model->year}' ";
        $query .= "     AND CHR_STF.SEMESTER = '{$model->semester}' ";
        $query .= "     AND CHR_STF.CHAIRCD = '{$model->ciField["CI_CHAIRCD"]}' ";
        $query .= "     AND CHR_STF.CHARGEDIV = 1 ";
        $query .= " ORDER BY ";
        $query .= "     CHR_STF.STAFFCD ";

        return $query;
    }

    //施設(通常
    public function getCiFacility($model)
    {
        $query  = " SELECT ";
        $query .= "     FAC_M.FACILITYNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCH_FAC_DAT FAC ";
        $query .= "     LEFT JOIN FACILITY_MST FAC_M ON FAC.FACCD = FAC_M.FACCD ";
        $query .= " WHERE ";
        $query .= "     FAC.EXECUTEDATE = '{$model->chairInfo["SEND_DATE"]}' ";
        $query .= "     AND FAC.PERIODCD = '{$model->periYomikae[$model->chairInfo["SEND_PERIOD"]]}' ";
        $query .= "     AND FAC.CHAIRCD = '{$model->ciField["CI_CHAIRCD"]}' ";
        $query .= " ORDER BY ";
        $query .= "     FAC.FACCD ";

        return $query;
    }

    //施設(基本
    public function getCiPtrnFacility($model)
    {
        $query  = " SELECT ";
        $query .= "     FAC_M.FACILITYNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCH_PTRN_FAC_DAT FAC ";
        $query .= "     LEFT JOIN FACILITY_MST FAC_M ON FAC.FACCD = FAC_M.FACCD ";
        $query .= " WHERE ";
        $query .= "     FAC.YEAR = '{$model->year}' ";
        $query .= "     AND FAC.SEMESTER = '{$model->semester}' ";
        $query .= "     AND FAC.BSCSEQ = {$model->field["BSCSEQ"]} ";
        $query .= "     AND FAC.DAYCD = '{$model->chairInfo["SEND_DATE"]}' ";
        $query .= "     AND FAC.PERIODCD = '{$model->periYomikae[$model->chairInfo["SEND_PERIOD"]]}' ";
        $query .= "     AND FAC.CHAIRCD = '{$model->ciField["CI_CHAIRCD"]}' ";
        $query .= " ORDER BY ";
        $query .= "     FAC.FACCD ";

        return $query;
    }

    //試験会場(通常
    public function getCiTestFacility($model)
    {
        $query  = " SELECT ";
        $query .= "     FAC_M.FACILITYNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCH_TEST_FAC_DAT FAC ";
        $query .= "     LEFT JOIN FACILITY_MST FAC_M ON FAC.FACCD = FAC_M.FACCD ";
        $query .= " WHERE ";
        $query .= "     FAC.EXECUTEDATE = '{$model->chairInfo["SEND_DATE"]}' ";
        $query .= "     AND FAC.PERIODCD = '{$model->periYomikae[$model->chairInfo["SEND_PERIOD"]]}' ";
        $query .= "     AND FAC.CHAIRCD = '{$model->ciField["CI_CHAIRCD"]}' ";
        $query .= " ORDER BY ";
        $query .= "     FAC.FACCD ";

        return $query;
    }

    //試験会場(基本
    public function getCiPtrnTestFacility($model)
    {
        $query  = " SELECT ";
        $query .= "     FAC_M.FACILITYNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCH_PTRN_TEST_FAC_DAT FAC ";
        $query .= "     LEFT JOIN FACILITY_MST FAC_M ON FAC.FACCD = FAC_M.FACCD ";
        $query .= " WHERE ";
        $query .= "     FAC.YEAR = '{$model->year}' ";
        $query .= "     AND FAC.SEMESTER = '{$model->semester}' ";
        $query .= "     AND FAC.BSCSEQ = {$model->field["BSCSEQ"]} ";
        $query .= "     AND FAC.DAYCD = '{$model->chairInfo["SEND_DATE"]}' ";
        $query .= "     AND FAC.PERIODCD = '{$model->periYomikae[$model->chairInfo["SEND_PERIOD"]]}' ";
        $query .= "     AND FAC.CHAIRCD = '{$model->ciField["CI_CHAIRCD"]}' ";
        $query .= " ORDER BY ";
        $query .= "     FAC.FACCD ";

        return $query;
    }

    //SCH_CHR_DAT取得
    public function getCiSchChair($model)
    {
        $query .= " SELECT ";
        $query .= "     SCH_CHR.DATADIV, ";
        $query .= "     SCH_CHR.EXECUTED ";
        $query .= " FROM ";
        $query .= "     SCH_CHR_DAT SCH_CHR ";
        $query .= " WHERE ";
        $query .= "     SCH_CHR.EXECUTEDATE = '{$model->chairInfo["SEND_DATE"]}' ";
        $query .= "     AND SCH_CHR.PERIODCD = '{$model->periYomikae[$model->chairInfo["SEND_PERIOD"]]}' ";
        $query .= "     AND SCH_CHR.CHAIRCD = '{$model->ciField["CI_CHAIRCD"]}' ";

        return $query;
    }

    //SCH_CHR_COUNTFLG取得
    public function getCiSchChairCount($model)
    {
        $query  = " WITH MAX_T AS ( ";
        $query .= " SELECT ";
        $query .= "     MAX(SCH_CHR_C.COUNTFLG) AS COUNTFLG, ";
        $query .= "     MAX(SCH_CHR_C.LESSON_MODE) AS LESSON_MODE ";
        $query .= " FROM ";
        $query .= "     SCH_CHR_COUNTFLG SCH_CHR_C ";
        $query .= " WHERE ";
        $query .= "     SCH_CHR_C.EXECUTEDATE = '{$model->chairInfo["SEND_DATE"]}' ";
        $query .= "     AND SCH_CHR_C.PERIODCD = '{$model->periYomikae[$model->chairInfo["SEND_PERIOD"]]}' ";
        $query .= "     AND SCH_CHR_C.CHAIRCD = '{$model->ciField["CI_CHAIRCD"]}' ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     MAX_T.COUNTFLG, ";
        $query .= "     B002.NAME1 AS LESSON_MODE ";
        $query .= " FROM ";
        $query .= "     MAX_T ";
        $query .= "     LEFT JOIN NAME_MST B002 ON B002.NAMECD1 = 'B002' ";
        $query .= "          AND MAX_T.LESSON_MODE = B002.NAMECD2 ";

        return $query;
    }

    //SCH_PTRN_COUNTFLG_DAT取得
    public function getCiPtrnChairCount($model)
    {
        $query  = " WITH MAX_T AS ( ";
        $query .= " SELECT ";
        $query .= "     MAX(SCH_CHR_C.COUNTFLG) AS COUNTFLG, ";
        $query .= "     '00' AS LESSON_MODE ";
        $query .= " FROM ";
        $query .= "     SCH_PTRN_COUNTFLG_DAT SCH_CHR_C ";
        $query .= " WHERE ";
        $query .= "     SCH_CHR_C.YEAR = '{$model->year}' ";
        $query .= "     AND SCH_CHR_C.SEMESTER = '{$model->semester}' ";
        $query .= "     AND SCH_CHR_C.BSCSEQ = {$model->field["BSCSEQ"]} ";
        $query .= "     AND SCH_CHR_C.DAYCD = '{$model->chairInfo["SEND_DATE"]}' ";
        $query .= "     AND SCH_CHR_C.PERIODCD = '{$model->periYomikae[$model->chairInfo["SEND_PERIOD"]]}' ";
        $query .= "     AND SCH_CHR_C.CHAIRCD = '{$model->ciField["CI_CHAIRCD"]}' ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     MAX_T.COUNTFLG, ";
        $query .= "     B002.NAME1 AS LESSON_MODE ";
        $query .= " FROM ";
        $query .= "     MAX_T ";
        $query .= "     LEFT JOIN NAME_MST B002 ON B002.NAMECD1 = 'B002' ";
        $query .= "          AND MAX_T.LESSON_MODE = B002.NAMECD2 ";

        return $query;
    }

    //学籍在籍データの件数取得
    public function getRegdDatCnt($grade = "")
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '". CTRL_YEAR ."' ";
        if ($grade) {
            $query .= "     AND GRADE = '". $grade ."' ";
        }

        return $query;
    }

    //コース名コンボ取得
    public function getCouseName($model, $flg, $sch_kind)
    {
        if ($flg) {
            $query  = " SELECT DISTINCT ";
            $query .= "     T2.COURSECD || '-' || T3.MAJORCD || '-' || T4.GRADE || '-' || T1.COURSECODE as VALUE, ";
            $query .= "     T4.GRADE_NAME1 || '&nbsp;&nbsp;(' || T2.COURSECD || T3.MAJORCD || ')&nbsp;' || T2.COURSENAME || T3.MAJORNAME || '&nbsp;&nbsp;(' || T1.COURSECODE || ')&nbsp;' || T1.COURSECODENAME as LABEL, ";
            $query .= "     T4.GRADE, ";
            $query .= "     T2.COURSECD, ";
            $query .= "     T3.MAJORCD, ";
            $query .= "     T1.COURSECODE ";
            $query .= " FROM ";
            $query .= "     COURSECODE_MST T1, ";
            $query .= "     COURSE_MST T2, ";
            $query .= "     MAJOR_MST T3, ";
            $query .= "     SCHREG_REGD_GDAT T4 ";
            $query .= " WHERE ";
            $query .= "     T4.YEAR         = '".$model->year."' AND ";
            $query .= "     T2.COURSECD     = T3.COURSECD ";
            if ($sch_kind != "ALL") {
                $query .= " AND T4.SCHOOL_KIND  = '".$sch_kind."' ";
            }
            if ($model->Properties["use_prg_schoolkind"] == "1") {
                if ($model->selectSchoolKind) {
                    $query .= " AND T4.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind), "','")."') ";
                }
            } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
                $query .= " AND T4.SCHOOL_KIND  = '".SCHOOLKIND."' ";
            }
            $query .= " ORDER BY ";
            $query .= "     T4.GRADE, ";
            $query .= "     T2.COURSECD, ";
            $query .= "     T3.MAJORCD, ";
            $query .= "     T1.COURSECODE ";
        } else {
            $query  = " SELECT DISTINCT ";
            $query .= "     T4.COURSECD || '-' || T4.MAJORCD || '-' || T4.GRADE || '-' || T4.COURSECODE as VALUE, ";
            $query .= "     T5.GRADE_NAME1 || '&nbsp;&nbsp;(' || T4.COURSECD || T4.MAJORCD || ')&nbsp;' || T2.COURSENAME || T3.MAJORNAME || '&nbsp;&nbsp;(' || T4.COURSECODE || ')&nbsp;' || T1.COURSECODENAME as LABEL, ";
            $query .= "     T4.GRADE, ";
            $query .= "     T4.COURSECD, ";
            $query .= "     T4.MAJORCD, ";
            $query .= "     T4.COURSECODE ";
            $query .= " FROM ";
            $query .= "     COURSECODE_MST T1, ";
            $query .= "     COURSE_MST T2, ";
            $query .= "     MAJOR_MST T3, ";
            $query .= "     SCHREG_REGD_DAT T4, ";
            $query .= "     SCHREG_REGD_GDAT T5 ";
            $query .= " WHERE ";
            $query .= "     T4.YEAR         ='".$model->year."' AND ";
            $query .= "     T1.COURSECODE   = T4.COURSECODE AND ";
            $query .= "     T2.COURSECD     = T4.COURSECD AND ";
            $query .= "     T3.COURSECD     = T4.COURSECD AND ";
            $query .= "     T4.MAJORCD      = T3.MAJORCD AND ";
            $query .= "     T4.YEAR         = T5.YEAR AND ";
            $query .= "     T4.GRADE        = T5.GRADE ";
            if ($sch_kind != "ALL") {
                $query .= " AND T5.SCHOOL_KIND  = '".$sch_kind."' ";
            }
            if ($model->Properties["use_prg_schoolkind"] == "1") {
                if ($model->selectSchoolKind) {
                    $query .= " AND T5.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind), "','")."') ";
                }
            } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
                $query .= " AND T5.SCHOOL_KIND  = '".SCHOOLKIND."' ";
            }
            $query .= " ORDER BY ";
            $query .= "     T4.GRADE, ";
            $query .= "     T4.COURSECD, ";
            $query .= "     T4.MAJORCD, ";
            $query .= "     T4.COURSECODE ";
        }

        return $query;
    }

    public function getZyukouNumMstAll($model)
    {
        $query  = " SELECT ";
        $query .= "     CHAIRCD, ";
        $query .= "     REMARK1 ";
        $query .= " FROM ";
        $query .= "     CHAIR_DETAIL_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$model->year}' ";
        $query .= "     AND SEMESTER = '{$model->semester}' ";
        $query .= "     AND SEQ = '001' ";

        return $query;
    }

    public function getZyukouNumAll($model, $day)
    {
        $query  = " WITH CHA_MST AS ( ";
        $query .= "     SELECT ";
        $query .= "         CHAIRCD ";
        $query .= "         , INT(REMARK1) MAXCNT ";
        $query .= "     FROM ";
        $query .= "         CHAIR_DETAIL_DAT ";
        $query .= "     WHERE ";
        $query .= "         YEAR = '{$model->year}' ";
        $query .= "         AND SEMESTER = '{$model->semester}' ";
        $query .= "         AND SEQ = '001' ";
        $query .= " ) ";
        $query .= " , STD_CNT AS ( ";
        $query .= "     SELECT ";
        $query .= "         CHAIRCD ";
        $query .= "         , COUNT(*) AS CNT ";
        $query .= "     FROM ";
        $query .= "         CHAIR_STD_DAT ";
        $query .= "     WHERE ";
        $query .= "         YEAR = '{$model->year}' ";
        $query .= "         AND SEMESTER = '{$model->semester}' ";
        $query .= "         AND '{$day}' BETWEEN APPDATE AND APPENDDATE ";
        $query .= "     GROUP BY ";
        $query .= "         CHAIRCD ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     STD_CNT.CHAIRCD ";
        $query .= "     , STD_CNT.CNT ";
        $query .= "     , CHA_MST.MAXCNT ";
        $query .= " FROM ";
        $query .= "     STD_CNT ";
        $query .= "     LEFT JOIN CHA_MST ";
        $query .= "         ON STD_CNT.CHAIRCD = CHA_MST.CHAIRCD ";
        $query .= " WHERE STD_CNT.CNT > CHA_MST.MAXCNT ";

        return $query;
    }

    //科目、群取得
    public function getChairDataAjax($model)
    {
        $query .= " SELECT ";
        $query .= "     SUBCLASSNAME, ";
        $query .= "     SUBCLASS.SUBCLASSCD, ";
        $query .= "     VALUE(ELEM.GROUPNAME, '') AS GROUPNAME, ";
        $query .= "     CHAIR.GROUPCD ";
        $query .= " FROM ";
        $query .= "     CHAIR_DAT CHAIR ";
        $query .= "     LEFT JOIN SUBCLASS_MST SUBCLASS ON CHAIR.CLASSCD = SUBCLASS.CLASSCD ";
        $query .= "          AND CHAIR.SCHOOL_KIND = SUBCLASS.SCHOOL_KIND ";
        $query .= "          AND CHAIR.CURRICULUM_CD = SUBCLASS.CURRICULUM_CD ";
        $query .= "          AND CHAIR.SUBCLASSCD = SUBCLASS.SUBCLASSCD ";
        $query .= "     LEFT JOIN ELECTCLASS_DAT ELEM ON CHAIR.GROUPCD = ELEM.GROUPCD ";
        $query .= "          AND CHAIR.YEAR = ELEM.YEAR ";
        $query .= " WHERE ";
        $query .= "     CHAIR.YEAR = '{$model->year}' ";
        $query .= "     AND CHAIR.SEMESTER = '{$model->semester}' ";
        $query .= "     AND CHAIR.CHAIRCD = '{$model->ajaxParam['AJAX_CHAIRCD']}' ";

        return $query;
    }

    public function getMeiboZyuuhukuPHP($model, $day, $chairList)
    {
        $query .= " WITH CHAIR_STD AS ( ";
        $query .= "   SELECT DISTINCT ";
        $query .= "     CHAIRCD ";
        $query .= "     , SCHREGNO ";
        $query .= "   FROM ";
        $query .= "     CHAIR_STD_DAT ";
        $query .= "   WHERE ";
        $query .= "     YEAR = '{$model->year}' ";
        $query .= "     AND SEMESTER = '{$model->semester}' ";
        $query .= "     AND '{$day}' BETWEEN APPDATE AND APPENDDATE ";
        $query .= "     AND CHAIRCD IN ( '".implode("','", $chairList)."')";
        $query .= " )  ";
        $query .= " , STD_CHAIRCNT AS ( ";
        $query .= "   SELECT ";
        $query .= "     SCHREGNO ";
        $query .= "     , COUNT(CHAIRCD) CHAIRCNT ";
        $query .= "   FROM ";
        $query .= "     CHAIR_STD ";
        $query .= "   GROUP BY ";
        $query .= "     SCHREGNO ";
        $query .= " ) ";
        $query .= " SELECT DISTINCT ";
        $query .= "   CHAIR_STD.CHAIRCD, ";
        $query .= "   STD_CHAIRCNT.CHAIRCNT ";
        $query .= " FROM ";
        $query .= "   CHAIR_STD ";
        $query .= "   INNER JOIN STD_CHAIRCNT ";
        $query .= "     ON CHAIR_STD.SCHREGNO = STD_CHAIRCNT.SCHREGNO ";
        $query .= " WHERE STD_CHAIRCNT.CHAIRCNT > 1 ";

        return $query;
    }

    public function getKouzaZyuuhukuPHP($model, $day, $periodCd)
    {
        $query .= " WITH CHAIR_STD AS ( ";
        $query .= "   SELECT DISTINCT ";
        $query .= "     CHAIRCD ";
        $query .= "     , SCHREGNO ";
        $query .= "   FROM ";
        $query .= "     CHAIR_STD_DAT ";
        $query .= "   WHERE ";
        $query .= "     YEAR = '{$model->year}' ";
        $query .= "     AND SEMESTER = '{$model->semester}' ";
        if ($model->field["SCH_DIV"] == "1") {
            $query .= "     AND '{$model->semesterInfo["EDATE"]}' BETWEEN APPDATE AND APPENDDATE ";
        } else {
            $query .= "     AND '{$day}' BETWEEN APPDATE AND APPENDDATE ";
        }
        $query .= "     AND CHAIRCD IN ( ";
        $query .= "       SELECT DISTINCT ";
        $query .= "         CHAIRCD  ";
        $query .= "       FROM ";
        if ($model->field["SCH_DIV"] == "1") {
            $query .= "         SCH_PTRN_DAT ";
        } else {
            $query .= "         SCH_CHR_DAT ";
        }
        $query .= "       WHERE ";
        $query .= "         YEAR = '{$model->year}'  ";
        $query .= "         AND SEMESTER = '{$model->semester}'  ";
        if ($model->field["SCH_DIV"] == "1") {
            //
            $query .= "         AND BSCSEQ = {$model->field['BSCSEQ']} ";
            $query .= "         AND DAYCD = '{$day}' ";
        } else {
            $query .= "         AND EXECUTEDATE = '{$day}' ";
        }
        $query .= "         AND PERIODCD = '{$periodCd}' ";
        $query .= "     ) ";
        $query .= " )  ";
        $query .= " , STD_CHAIRCNT AS ( ";
        $query .= "   SELECT ";
        $query .= "     SCHREGNO ";
        $query .= "     , COUNT(CHAIRCD) CHAIRCNT ";
        $query .= "   FROM ";
        $query .= "     CHAIR_STD ";
        $query .= "   GROUP BY ";
        $query .= "     SCHREGNO ";
        $query .= " ) ";
        $query .= " SELECT DISTINCT ";
        $query .= "   CHAIR_STD.CHAIRCD, ";
        $query .= "   STD_CHAIRCNT.CHAIRCNT ";
        $query .= " FROM ";
        $query .= "   CHAIR_STD ";
        $query .= "   INNER JOIN STD_CHAIRCNT ";
        $query .= "     ON CHAIR_STD.SCHREGNO = STD_CHAIRCNT.SCHREGNO ";
        $query .= " WHERE STD_CHAIRCNT.CHAIRCNT > 1 ";

        return $query;
    }

    public function getKouzaZyuuhukuPHPTest($model, $day, $periodCd, $chairList)
    {
        $query .= " WITH CHAIR_STD AS ( ";
        $query .= "   SELECT DISTINCT ";
        $query .= "     CHAIRCD ";
        $query .= "     , SCHREGNO ";
        $query .= "   FROM ";
        $query .= "     CHAIR_STD_DAT ";
        $query .= "   WHERE ";
        $query .= "     YEAR = '{$model->year}' ";
        $query .= "     AND SEMESTER = '{$model->semester}' ";
        if ($model->field["SCH_DIV"] == "1") {
            $query .= "     AND '{$model->semesterInfo["EDATE"]}' BETWEEN APPDATE AND APPENDDATE ";
        } else {
            $query .= "     AND '{$day}' BETWEEN APPDATE AND APPENDDATE ";
        }
        $query .= "     AND CHAIRCD IN ( ";
        $query .= "       SELECT DISTINCT ";
        $query .= "         CHAIRCD  ";
        $query .= "       FROM ";
        if ($model->field["SCH_DIV"] == "1") {
            $query .= "         SCH_PTRN_DAT ";
        } else {
            $query .= "         SCH_CHR_DAT ";
        }
        $query .= "       WHERE ";
        $query .= "         YEAR = '{$model->year}'  ";
        $query .= "         AND SEMESTER = '{$model->semester}'  ";
        if ($model->field["SCH_DIV"] == "1") {
            //
            $query .= "         AND BSCSEQ = {$model->field['BSCSEQ']} ";
            $query .= "         AND DAYCD = '{$day}' ";
        } else {
            $query .= "         AND EXECUTEDATE = '{$day}' ";
        }
        $query .= "         AND PERIODCD = '{$periodCd}' ";
        $query .= "         AND CHAIRCD IN ('" . join("','", $chairList) . "') ";
        $query .= "     ) ";
        $query .= " )  ";
        $query .= " , STD_CHAIRCNT AS ( ";
        $query .= "   SELECT ";
        $query .= "     SCHREGNO ";
        $query .= "     , COUNT(CHAIRCD) CHAIRCNT ";
        $query .= "   FROM ";
        $query .= "     CHAIR_STD ";
        $query .= "   GROUP BY ";
        $query .= "     SCHREGNO ";
        $query .= " ) ";
        $query .= " SELECT DISTINCT ";
        $query .= "   CHAIR_STD.CHAIRCD, ";
        $query .= "   STD_CHAIRCNT.CHAIRCNT ";
        $query .= " FROM ";
        $query .= "   CHAIR_STD ";
        $query .= "   INNER JOIN STD_CHAIRCNT ";
        $query .= "     ON CHAIR_STD.SCHREGNO = STD_CHAIRCNT.SCHREGNO ";
        $query .= " WHERE STD_CHAIRCNT.CHAIRCNT > 1 ";

        return $query;
    }

    public function getGunCodeAjax($model, $chairCd)
    {
        $query .= " SELECT ";
        $query .= "     CHAIR.CHAIRNAME, ";
        $query .= "     CHAIR.GROUPCD ";
        $query .= " FROM ";
        $query .= "     CHAIR_DAT CHAIR ";
        $query .= " WHERE ";
        $query .= "     CHAIR.YEAR = '{$model->year}' ";
        $query .= "     AND CHAIR.SEMESTER = '{$model->semester}' ";
        $query .= "     AND CHAIR.CHAIRCD = '{$chairCd}' ";

        return $query;
    }

    public function getFacilityKouzaKamokuAjax($model, $chairCd)
    {
        $query .= " SELECT ";
        $query .= "     CHAIR.CHAIRNAME, ";
        $query .= "     CHAIR.GROUPCD, ";
        $query .= "     VALUE(ELEM.GROUPNAME, '') AS GROUPNAME, ";
        $query .= "     CHAIR.CLASSCD || '-' || CHAIR.SCHOOL_KIND || '-' || CHAIR.CURRICULUM_CD || '-' || CHAIR.SUBCLASSCD AS SUBCLASSCD, ";
        $query .= "     SUBM.SUBCLASSNAME ";
        $query .= " FROM ";
        $query .= "     CHAIR_DAT CHAIR ";
        $query .= "     LEFT JOIN SUBCLASS_MST SUBM ON CHAIR.CLASSCD = SUBM.CLASSCD ";
        $query .= "          AND CHAIR.SCHOOL_KIND = SUBM.SCHOOL_KIND ";
        $query .= "          AND CHAIR.CURRICULUM_CD = SUBM.CURRICULUM_CD ";
        $query .= "          AND CHAIR.SUBCLASSCD = SUBM.SUBCLASSCD ";
        $query .= "     LEFT JOIN ELECTCLASS_DAT ELEM ON CHAIR.GROUPCD = ELEM.GROUPCD ";
        $query .= "          AND CHAIR.YEAR = ELEM.YEAR ";
        $query .= " WHERE ";
        $query .= "     CHAIR.YEAR = '{$model->year}' ";
        $query .= "     AND CHAIR.SEMESTER = '{$model->semester}' ";
        $query .= "     AND CHAIR.CHAIRCD = '{$chairCd}' ";

        return $query;
    }

    public function getSchFacDat($model)
    {
        $setSdate = str_replace("/", "-", $model->startDate);
        $setEdate = str_replace("/", "-", $model->endDate);
        $query  = " SELECT ";
        $query .= "     FACD.EXECUTEDATE AS DATE_OR_WEEK, ";
        $query .= "     FACD.PERIODCD, ";
        $query .= "     FACD.CHAIRCD, ";
        $query .= "     FACD.FACCD ";
        $query .= " FROM ";
        $query .= "     SCH_FAC_DAT FACD ";
        $query .= " WHERE ";
        $query .= "     EXECUTEDATE BETWEEN '{$setSdate}' AND '{$setEdate}' ";
        $query .= " ORDER BY ";
        $query .= "     FACD.EXECUTEDATE, ";
        $query .= "     FACD.PERIODCD, ";
        $query .= "     FACD.CHAIRCD, ";
        $query .= "     FACD.FACCD ";

        return $query;
    }

    public function getPtrnFacDat($model)
    {
        $query  = " SELECT ";
        $query .= "     FACD.DAYCD AS DATE_OR_WEEK, ";
        $query .= "     FACD.PERIODCD, ";
        $query .= "     FACD.CHAIRCD, ";
        $query .= "     FACD.FACCD ";
        $query .= " FROM ";
        $query .= "     SCH_PTRN_FAC_DAT FACD ";
        $query .= " WHERE ";
        $query .= "     FACD.YEAR = '{$model->year}' ";
        $query .= "     AND FACD.SEMESTER = '{$model->semester}' ";
        $query .= "     AND FACD.BSCSEQ = {$model->field['BSCSEQ']} ";
        $query .= " ORDER BY ";
        $query .= "     FACD.DAYCD, ";
        $query .= "     FACD.PERIODCD, ";
        $query .= "     FACD.CHAIRCD, ";
        $query .= "     FACD.FACCD ";

        return $query;
    }

    public function getSchTestFacDat($model)
    {
        $setSdate = str_replace("/", "-", $model->startDate);
        $setEdate = str_replace("/", "-", $model->endDate);
        $query  = " SELECT ";
        $query .= "     FACD.EXECUTEDATE AS DATE_OR_WEEK, ";
        $query .= "     FACD.PERIODCD, ";
        $query .= "     FACD.CHAIRCD, ";
        $query .= "     FACD.FACCD ";
        $query .= " FROM ";
        $query .= "     SCH_TEST_FAC_DAT FACD ";
        $query .= " WHERE ";
        $query .= "     EXECUTEDATE BETWEEN '{$setSdate}' AND '{$setEdate}' ";
        $query .= " ORDER BY ";
        $query .= "     FACD.EXECUTEDATE, ";
        $query .= "     FACD.PERIODCD, ";
        $query .= "     FACD.CHAIRCD, ";
        $query .= "     FACD.FACCD ";

        return $query;
    }

    public function getPtrnTestFacDat($model)
    {
        $query  = " SELECT ";
        $query .= "     FACD.DAYCD AS DATE_OR_WEEK, ";
        $query .= "     FACD.PERIODCD, ";
        $query .= "     FACD.CHAIRCD, ";
        $query .= "     FACD.FACCD ";
        $query .= " FROM ";
        $query .= "     SCH_PTRN_TEST_FAC_DAT FACD ";
        $query .= " WHERE ";
        $query .= "     FACD.YEAR = '{$model->year}' ";
        $query .= "     AND FACD.SEMESTER = '{$model->semester}' ";
        $query .= "     AND FACD.BSCSEQ = {$model->field['BSCSEQ']} ";
        $query .= " ORDER BY ";
        $query .= "     FACD.DAYCD, ";
        $query .= "     FACD.PERIODCD, ";
        $query .= "     FACD.CHAIRCD, ";
        $query .= "     FACD.FACCD ";

        return $query;
    }

    public function getSchCountFlgDat($model)
    {
        $setSdate = str_replace("/", "-", $model->startDate);
        $setEdate = str_replace("/", "-", $model->endDate);
        $query  = " WITH CHAIR_GHR AS ( ";
        $query .= knjb3042Query::getChairClassSql($model);
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     COUNTFLG.EXECUTEDATE AS DATE_OR_WEEK, ";
        $query .= "     COUNTFLG.PERIODCD, ";
        $query .= "     COUNTFLG.CHAIRCD, ";
        $query .= "     COUNTFLG.GRADE || '-' || COUNTFLG.HR_CLASS AS GRADEHR, ";
        $query .= "     VALUE(COUNTFLG.COUNTFLG, '1') AS COUNTFLG, ";
        $query .= "     VALUE(COUNTFLG.LESSON_MODE, '00') AS LESSON_MODE ";
        $query .= " FROM ";
        $query .= "     SCH_CHR_COUNTFLG COUNTFLG, ";
        $query .= "     CHAIR_GHR ";
        $query .= " WHERE ";
        $query .= "     COUNTFLG.EXECUTEDATE BETWEEN '{$setSdate}' AND '{$setEdate}' ";
        $query .= "     AND COUNTFLG.CHAIRCD = CHAIR_GHR.CHAIRCD ";
        $query .= "     AND COUNTFLG.GRADE = CHAIR_GHR.TRGTGRADE ";
        $query .= "     AND COUNTFLG.HR_CLASS = CHAIR_GHR.TRGTCLASS ";
        $query .= " ORDER BY ";
        $query .= "     COUNTFLG.EXECUTEDATE, ";
        $query .= "     COUNTFLG.PERIODCD, ";
        $query .= "     COUNTFLG.CHAIRCD, ";
        $query .= "     GRADEHR ";

        return $query;
    }

    public function getPtrnCountFlgDat($model)
    {
        $query  = " WITH CHAIR_GHR AS ( ";
        $query .= knjb3042Query::getChairClassSql($model);
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     COUNTFLG.DAYCD AS DATE_OR_WEEK, ";
        $query .= "     COUNTFLG.PERIODCD, ";
        $query .= "     COUNTFLG.CHAIRCD, ";
        $query .= "     COUNTFLG.GRADE || '-' || COUNTFLG.HR_CLASS AS GRADEHR, ";
        $query .= "     VALUE(COUNTFLG.COUNTFLG, '1') AS COUNTFLG, ";
        $query .= "     '00' AS LESSON_MODE ";
        $query .= " FROM ";
        $query .= "     SCH_PTRN_COUNTFLG_DAT COUNTFLG, ";
        $query .= "     CHAIR_GHR ";
        $query .= " WHERE ";
        $query .= "     COUNTFLG.YEAR = '{$model->year}' ";
        $query .= "     AND COUNTFLG.SEMESTER = '{$model->semester}' ";
        $query .= "     AND COUNTFLG.BSCSEQ = {$model->field['BSCSEQ']} ";
        $query .= "     AND COUNTFLG.CHAIRCD = CHAIR_GHR.CHAIRCD ";
        $query .= "     AND COUNTFLG.GRADE = CHAIR_GHR.TRGTGRADE ";
        $query .= "     AND COUNTFLG.HR_CLASS = CHAIR_GHR.TRGTCLASS ";
        $query .= " ORDER BY ";
        $query .= "     COUNTFLG.DAYCD, ";
        $query .= "     COUNTFLG.PERIODCD, ";
        $query .= "     COUNTFLG.CHAIRCD, ";
        $query .= "     GRADEHR ";

        return $query;
    }

    public function getSchCountFlgDatOne($model)
    {
        $query  = " SELECT ";
        $query .= "     COUNTFLG.GRADE || '-' || COUNTFLG.HR_CLASS AS GRADEHR, ";
        $query .= "     COUNTFLG.COUNTFLG, ";
        $query .= "     COUNTFLG.LESSON_MODE ";
        $query .= " FROM ";
        $query .= "     SCH_CHR_COUNTFLG COUNTFLG ";
        $query .= " WHERE ";
        $query .= "     COUNTFLG.EXECUTEDATE = '{$model->chairInfo["SEND_DATE"]}' ";
        $query .= "     AND COUNTFLG.PERIODCD = '{$model->periYomikae[$model->chairInfo["SEND_PERIOD"]]}' ";
        $query .= "     AND COUNTFLG.CHAIRCD = '{$model->ciField["CI_CHAIRCD"]}' ";
        $query .= "     AND COUNTFLG.GRADE || '-' || COUNTFLG.HR_CLASS IN ('".implode("','", $model->chairClass)."') ";

        return $query;
    }

    public function getPtrnCountFlgDatOne($model)
    {
        $query  = " SELECT ";
        $query .= "     COUNTFLG.GRADE || '-' || COUNTFLG.HR_CLASS AS GRADEHR, ";
        $query .= "     COUNTFLG.COUNTFLG, ";
        $query .= "     '' AS LESSON_MODE ";
        $query .= " FROM ";
        $query .= "     SCH_PTRN_COUNTFLG_DAT COUNTFLG ";
        $query .= " WHERE ";
        $query .= "     COUNTFLG.YEAR = '{$model->year}' ";
        $query .= "     AND COUNTFLG.SEMESTER = '{$model->semester}' ";
        $query .= "     AND COUNTFLG.BSCSEQ = {$model->field['BSCSEQ']} ";
        $query .= "     AND COUNTFLG.PERIODCD = '{$model->periYomikae[$model->chairInfo["SEND_PERIOD"]]}' ";
        $query .= "     AND COUNTFLG.CHAIRCD = '{$model->ciField["CI_CHAIRCD"]}' ";
        $query .= "     AND COUNTFLG.GRADE || '-' || COUNTFLG.HR_CLASS IN ('".implode("','", $model->chairClass)."') ";

        return $query;
    }

    public function getKouzaFacilityAjax($model)
    {
        $query  = " SELECT ";
        $query .= "     FACY.FACCD, ";
        $query .= "     FACM.FACILITYNAME ";
        $query .= " FROM ";
        $query .= "     FACILITY_YDAT FACY ";
        $query .= "     LEFT JOIN FACILITY_MST FACM ON FACY.FACCD = FACM.FACCD ";
        $query .= " WHERE ";
        $query .= "     FACY.YEAR = '{$model->year}' ";

        return $query;
    }

    public function getFacilityAjax($model)
    {
        $query .= " SELECT ";
        $query .= "     FACY.FACCD, ";
        $query .= "     FACM.FACILITYNAME ";
        $query .= " FROM ";
        $query .= "     FACILITY_YDAT FACY ";
        $query .= "     LEFT JOIN FACILITY_MST FACM ON FACY.FACCD = FACM.FACCD ";
        $query .= " WHERE ";
        $query .= "     FACY.YEAR = '{$model->year}' ";

        return $query;
    }

    public function getLessonModeAjax()
    {
        $query  = " SELECT ";
        $query .= "     B002.NAMECD2 AS VALUE, ";
        $query .= "     B002.NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     NAME_MST B002 ";
        $query .= " WHERE ";
        $query .= "     B002.NAMECD1 = 'B002' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    // 受講生人数オーバーの講座一覧取得
    public function getChairStdCapaOver($model, $day)
    {
        $query  = " WITH CHAIR_STD_CNT AS ( ";
        $query .= "     SELECT ";
        $query .= "         YEAR ";
        $query .= "         , SEMESTER ";
        $query .= "         , CHAIRCD ";
        $query .= "         , COUNT(*) STD_CNT ";
        $query .= "     FROM ";
        $query .= "         CHAIR_STD_DAT ";
        $query .= "     WHERE ";
        $query .= "         YEAR = '{$model->year}' ";
        $query .= "         AND SEMESTER = '{$model->semester}' ";
        $query .= "         AND '{$day}' BETWEEN APPDATE AND APPENDDATE ";
        $query .= "     GROUP BY ";
        $query .= "         YEAR ";
        $query .= "         , SEMESTER ";
        $query .= "         , CHAIRCD ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     CHAIR_STD_MAX.YEAR ";
        $query .= "     , CHAIR_STD_MAX.SEMESTER ";
        $query .= "     , CHAIR_STD_MAX.CHAIRCD ";
        $query .= "     , CHAIR_STD_MAX.REMARK1 STD_MAX ";
        $query .= "     , CHAIR_STD_CNT.STD_CNT ";
        $query .= " FROM ";
        $query .= "     CHAIR_DETAIL_DAT CHAIR_STD_MAX ";
        $query .= "     INNER JOIN CHAIR_STD_CNT ";
        $query .= "         ON CHAIR_STD_MAX.YEAR = CHAIR_STD_CNT.YEAR ";
        $query .= "         AND CHAIR_STD_MAX.SEMESTER = CHAIR_STD_CNT.SEMESTER ";
        $query .= "         AND CHAIR_STD_MAX.CHAIRCD = CHAIR_STD_CNT.CHAIRCD ";
        $query .= " WHERE ";
        $query .= "     CHAIR_STD_MAX.YEAR = '{$model->year}' ";
        $query .= "     AND CHAIR_STD_MAX.SEMESTER = '{$model->semester}' ";
        $query .= "     AND CHAIR_STD_MAX.SEQ = '001' ";
        $query .= "     AND INT(CHAIR_STD_MAX.REMARK1) < CHAIR_STD_CNT.STD_CNT ";

        return $query;
    }

    // 同一名簿の講座一覧取得
    public function getChairStdSameMeibo($model, $day)
    {
        $query  = " WITH TARGET_STD AS ( ";
        $query .= "     SELECT ";
        $query .= "         YEAR ";
        $query .= "         , SEMESTER ";
        $query .= "         , CHAIRCD ";
        $query .= "         , SCHREGNO ";
        $query .= "     FROM ";
        $query .= "         CHAIR_STD_DAT ";
        $query .= "     WHERE ";
        $query .= "         YEAR = '{$model->year}' ";
        $query .= "         AND SEMESTER = '{$model->semester}' ";
        $query .= "         AND CHAIRCD = '{$model->ajaxParam['CHAIRCD']}' ";
        $query .= "         AND '{$day}' BETWEEN APPDATE AND APPENDDATE ";
        $query .= " ) ";

        $query .= " , CHAIR_STD_CNT AS ( ";
        $query .= " SELECT ";
        $query .= "     T1.YEAR ";
        $query .= "     , T1.SEMESTER ";
        $query .= "     , T1.CHAIRCD ";
        $query .= "     , COUNT(T1.SCHREGNO) STD_CNT ";
        $query .= "     , COUNT(T2.SCHREGNO) CHK_STD_CNT ";
        $query .= "     , (SELECT COUNT(*) FROM TARGET_STD) BASE_STD_CNT ";
        $query .= " FROM ";
        $query .= "     CHAIR_STD_DAT T1 ";
        $query .= "     LEFT JOIN TARGET_STD T2 ";
        $query .= "         ON T1.SCHREGNO = T2.SCHREGNO ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '{$model->year}' ";
        $query .= "     AND T1.SEMESTER = '{$model->semester}' ";
        $query .= "     AND '{$day}' BETWEEN T1.APPDATE AND T1.APPENDDATE ";
        $query .= " GROUP BY ";
        $query .= "     T1.YEAR ";
        $query .= "     , T1.SEMESTER ";
        $query .= "     , T1.CHAIRCD ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     CHAIR_STD_CNT ";
        $query .= " WHERE ";
        $query .= "     STD_CNT = CHK_STD_CNT ";
        $query .= "     AND STD_CNT = BASE_STD_CNT ";

        return $query;
    }

    // HRクラスの名簿一覧取得
    public function getHrClassStdMeibo($model)
    {
        $query  = " SELECT ";
        $query .= "     T1.YEAR ";
        $query .= "     , T1.SEMESTER ";
        $query .= "     , T1.SCHREGNO ";
        $query .= "     , T1.GRADE ";
        $query .= "     , T1.HR_CLASS ";
        $query .= "     , T2.NAME ";
        $query .= "     , T2.NAME_SHOW ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1 ";
        $query .= "     INNER JOIN SCHREG_BASE_MST T2 ";
        $query .= "         ON T1.SCHREGNO = T2.SCHREGNO ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '{$model->year}' ";
        $query .= "     AND T1.SEMESTER = '{$model->semester}' ";
        $query .= "     AND T1.GRADE = '{$model->ajaxParam['GRADE']}' ";
        $query .= "     AND T1.HR_CLASS = '{$model->ajaxParam['HR_CLASS']}' ";

        return $query;
    }

    // 名簿の講座一覧取得
    public function getStdChair($model, $day)
    {
        $query  = " WITH CHAIR_STD AS ( ";
        $query .= "     SELECT ";
        $query .= "         YEAR ";
        $query .= "         , SEMESTER ";
        $query .= "         , CHAIRCD ";
        $query .= "         , SCHREGNO ";
        $query .= "     FROM ";
        $query .= "         CHAIR_STD_DAT ";
        $query .= "     WHERE ";
        $query .= "         YEAR = '{$model->year}' ";
        $query .= "         AND SEMESTER = '{$model->semester}' ";
        $query .= "         AND '{$day}' BETWEEN APPDATE AND APPENDDATE ";
        $query .= " ) ";
        $query .= " SELECT DISTINCT ";
        $query .= "     T1.YEAR ";
        $query .= "     , T1.SEMESTER ";
        $query .= "     , T1.SCHREGNO ";
        $query .= "     , T1.CHAIRCD ";
        $query .= " FROM ";
        $query .= "     CHAIR_STD T1 ";
        $query .= " WHERE ";
        $query .= "     T1.SCHREGNO = '{$model->ajaxParam['SCHREGNO']}' ";

        return $query;
    }

    // 収容講座数を取得
    public function getChrFacility($model, $faccd)
    {
        $query  = " SELECT ";
        $query .= "     FACCD ";
        $query .= "     , CHR_CAPACITY ";
        $query .= " FROM ";
        $query .= "     V_FACILITY_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$model->year}' ";
        $query .= "     AND FACCD IN ('".implode("','", $faccd)."') ";

        return $query;
    }


    // 収容講座数を取得
    public function getChrFacilityOverFac($model, $executedate, $periodCd, $faccd)
    {
        $query  = " SELECT ";
        $query .= "     FAC.FACCD ";
        $query .= "     , V_FAC.CHR_CAPACITY ";
        $query .= "     , FAC.EXECUTEDATE ";
        $query .= "     , FAC.PERIODCD ";
        $query .= "     , COUNT(FAC.FACCD) AS CNT ";
        $query .= " FROM ";
        $query .= "     SCH_FAC_DAT FAC ";
        $query .= "     INNER JOIN V_FACILITY_MST V_FAC ";
        $query .= "         ON V_FAC.YEAR = FAC.YEAR ";
        $query .= "         AND V_FAC.FACCD = FAC.FACCD ";
        $query .= " WHERE ";
        $query .= "     FAC.YEAR = '{$model->year}' ";
        $query .= "     AND FAC.SEMESTER = '{$model->semester}' ";
        $query .= "     AND FAC.PERIODCD = '{$periodCd}' ";
        $query .= "     AND FAC.EXECUTEDATE = '{$executedate}' ";
        $query .= "     AND FAC.FACCD IN ('".str_replace(":", "','", $faccd)."') ";
        $query .= " GROUP BY ";
        $query .= "     FAC.FACCD ";
        $query .= "     , V_FAC.CHR_CAPACITY ";
        $query .= "     , FAC.EXECUTEDATE ";
        $query .= "     , FAC.PERIODCD ";
        $query .= " HAVING ";
        $query .= "     COUNT(FAC.FACCD) > V_FAC.CHR_CAPACITY ";

        return $query;
    }

    // 収容講座数を取得(基本)
    public function getChrFacilityOverPtrn($model, $daycd, $periodCd, $faccd)
    {
        $query  = " SELECT ";
        $query .= "     FAC.FACCD ";
        $query .= "     , V_FAC.CHR_CAPACITY ";
        $query .= "     , FAC.DAYCD ";
        $query .= "     , FAC.PERIODCD ";
        $query .= "     , COUNT(FAC.FACCD) AS CNT ";
        $query .= " FROM ";
        $query .= "     SCH_PTRN_FAC_DAT FAC ";
        $query .= "     INNER JOIN V_FACILITY_MST V_FAC ";
        $query .= "         ON V_FAC.YEAR = FAC.YEAR ";
        $query .= "         AND V_FAC.FACCD = FAC.FACCD ";
        $query .= " WHERE ";
        $query .= "     FAC.YEAR = '{$model->year}' ";
        $query .= "     AND FAC.SEMESTER = '{$model->semester}' ";
        $query .= "     AND FAC.PERIODCD = '{$periodCd}' ";
        $query .= "     AND FAC.DAYCD = '{$daycd}' ";
        $query .= "     AND FAC.BSCSEQ = {$model->field["BSCSEQ"]} ";
        $query .= "     AND FAC.FACCD IN ('".str_replace(":", "','", $faccd)."') ";
        $query .= " GROUP BY ";
        $query .= "     FAC.FACCD ";
        $query .= "     , V_FAC.CHR_CAPACITY ";
        $query .= "     , FAC.DAYCD ";
        $query .= "     , FAC.PERIODCD ";
        $query .= " HAVING ";
        $query .= "     COUNT(FAC.FACCD) > V_FAC.CHR_CAPACITY ";

        return $query;
    }

    public function update($db, $model)
    {
        $yearSeme = explode('-', $model->yearSeme);

        $updateData = array('Add' => array(),
                            'AddChairCdNew' => array(),
                            'AddChairCdOld' => array(),
                            'Del' => array(),
                            'DelChairCd' => array());

        if (!empty($model->updateAddData)) {
            $updateAddDataList = explode('|', $model->updateAddData);
            foreach ($updateAddDataList as $key => $value) {
                $AddParts = explode(",", $value);
                $oldData = explode("_", $AddParts[0]);
                $newData = explode("_", $AddParts[1]);
                //校時の読替
                $oldData[1] = $model->periYomikae[$oldData[1]];
                $newData[1] = $model->periYomikae[$newData[1]];
                $updateData['Add'][] = array('Old' => $oldData, 'New'=>$newData);
                //追加した講座は、OLDの最終に'Add'と入っている。
                if ($oldData[get_count($oldData) - 1] != 'Add') {
                    //追加ではない時、OLDの講座番号をセット
                    //出欠情報の移動用。デフォルト表示のみ対象
                    $updateData['AddChairCdOld'][] = $oldData[2];
                }
                $updateData['AddChairCdNew'][] = $newData[2];
            }
        }

        if (!empty($model->updateDelData)) {
            $updateDelDataList = explode('|', $model->updateDelData);
            foreach ($updateDelDataList as $key => $value) {
                $AddParts = explode(",", $value);
                $oldData = explode("_", $AddParts[0]);
                //校時読替
                $oldData[1] = $model->periYomikae[$oldData[1]];
                $updateData['Del'][] = $oldData;
                $updateData['DelChairCd'][] = $oldData[2];
            }
        }
        $setSdate = str_replace("/", "-", $model->startDate);
        $setEdate = str_replace("/", "-", $model->endDate);

        $befAttendData = array();
        $befChrHrateData = array();
        $befSchChrData = array();
        if (!empty($updateData['AddChairCdOld'])) {
            $joinChairCd = join("','", $updateData['AddChairCdOld']);

            //更新前ATTEND_DAT
            $query  = " SELECT ";
            $query .= "     * ";
            $query .= " FROM ";
            $query .= "     ATTEND_DAT ";
            $query .= " WHERE ";
            $query .= "     ATTENDDATE BETWEEN '{$setSdate}' AND '{$setEdate}' ";
            $query .= "     AND CHAIRCD IN ('{$joinChairCd}') ";

            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $befAttendData[$row["ATTENDDATE"]][$row["PERIODCD"]][$row["CHAIRCD"]][] = $row;
            }
            $result->free();

            //更新前SCH_CHR_HRATE_DAT
            $query  = " SELECT DISTINCT ";
            $query .= "     T1.* ";
            $query .= " FROM ";
            $query .= "     SCH_CHR_HRATE_DAT T1";
            $query .= "     INNER JOIN CHAIR_STD_DAT T2 ON T2.YEAR = '{$model->year}' ";
            $query .= "           AND T2.SEMESTER = '{$model->semester}' ";
            $query .= "           AND T2.CHAIRCD = T1.CHAIRCD ";
            $query .= "           AND T1.EXECUTEDATE BETWEEN T2.APPDATE AND T2.APPENDDATE ";
            $query .= "     INNER JOIN SCHREG_REGD_DAT T3 ON T3.YEAR = T2.YEAR ";
            $query .= "           AND T3.SEMESTER = T2.SEMESTER ";
            $query .= "           AND T3.SCHREGNO = T2.SCHREGNO ";
            $query .= "           AND T3.GRADE = T1.GRADE ";
            $query .= "           AND T3.HR_CLASS = T1.HR_CLASS ";
            $query .= " WHERE ";
            $query .= "     T1.EXECUTEDATE BETWEEN '{$setSdate}' AND '{$setEdate}' ";
            $query .= "     AND T1.CHAIRCD IN ('{$joinChairCd}') ";

            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $befChrHrateData[$row["EXECUTEDATE"]][$row["PERIODCD"]][$row["CHAIRCD"]][] = $row;
            }
            $result->free();

            //更新前SCH_CHR_DAT
            $query  = " SELECT ";
            $query .= "     * ";
            $query .= " FROM ";
            $query .= "     SCH_CHR_DAT ";
            $query .= " WHERE ";
            $query .= "     EXECUTEDATE BETWEEN '{$setSdate}' AND '{$setEdate}' ";
            $query .= "     AND CHAIRCD IN ('{$joinChairCd}') ";

            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $befSchChrData[$row["EXECUTEDATE"]][$row["PERIODCD"]][$row["CHAIRCD"]][] = $row;
            }
            $result->free();
        }

        $delPeriArray = array_merge($updateData['Add'], $updateData['Del']);
        foreach ($delPeriArray as $upAddKey => $upAddVal) {
            if (isset($upAddVal['Old'])) {
                list($executeCdOld, $periodCdOld, $chairCdOld, $testCdOld, $lineCntOld, $facilityOld, $testFacilityOld, $countLesson) = $upAddVal['Old'];
            } else {
                list($executeCdOld, $periodCdOld, $chairCdOld, $testCdOld, $lineCntOld, $facilityOld, $testFacilityOld, $countLesson) = $upAddVal;
            }
            $query  = " DELETE FROM ";
            $query .= "     SCH_CHR_TEST ";
            $query .= " WHERE ";
            $query .= "     EXECUTEDATE = '{$executeCdOld}' ";
            $query .= "     AND PERIODCD = '{$periodCdOld}' ";
            $query .= "     AND CHAIRCD = '{$chairCdOld}' ";
            $db->query($query);

            $query  = " DELETE FROM ATTEND_DAT WHERE ";
            $query .= "     ATTENDDATE = '{$executeCdOld}' ";
            $query .= "     AND PERIODCD = '{$periodCdOld}' ";
            $query .= "     AND CHAIRCD = '{$chairCdOld}' ";
            $db->query($query);

            //SCH_CHR_HRATE_DAT DEL
            $query  = " DELETE FROM SCH_CHR_HRATE_DAT WHERE ";
            $query .= "     EXECUTEDATE = '{$executeCdOld}' ";
            $query .= "     AND PERIODCD = '{$periodCdOld}' ";
            $query .= "     AND CHAIRCD = '{$chairCdOld}' ";
            $db->query($query);

            //SCH_CHR_DAT DEL
            $query  = " DELETE FROM SCH_CHR_DAT WHERE ";
            $query .= "     EXECUTEDATE = '{$executeCdOld}' ";
            $query .= "     AND PERIODCD = '{$periodCdOld}' ";
            $query .= "     AND CHAIRCD = '{$chairCdOld}' ";
            $db->query($query);

            //SCH_FAC_DAT DEL
            $query  = " DELETE FROM SCH_FAC_DAT WHERE ";
            $query .= "     EXECUTEDATE = '{$executeCdOld}' ";
            $query .= "     AND PERIODCD = '{$periodCdOld}' ";
            $query .= "     AND CHAIRCD = '{$chairCdOld}' ";
            $db->query($query);

            if ($model->Properties["useTestFacility"] == "1") {
                //SCH_TEST_FAC_DAT DEL
                $query  = " DELETE FROM SCH_TEST_FAC_DAT WHERE ";
                $query .= "     EXECUTEDATE = '{$executeCdOld}' ";
                $query .= "     AND PERIODCD = '{$periodCdOld}' ";
                $query .= "     AND CHAIRCD = '{$chairCdOld}' ";
                $db->query($query);
            }

            //SCH_CHR_COUNTFLG DEL
            $query  = " DELETE FROM SCH_CHR_COUNTFLG WHERE ";
            $query .= "     EXECUTEDATE = '{$executeCdOld}' ";
            $query .= "     AND PERIODCD = '{$periodCdOld}' ";
            $query .= "     AND CHAIRCD = '{$chairCdOld}' ";
            $db->query($query);
        }


        $check = array();
        foreach ($updateData['Add'] as $key => $Row) {
            list($executeCdOld, $periodCdOld, $chairCdOld, $testCdOld, $lineCntOld, $facilityOld, $testFacilityOld, $countLessonOld) = $Row['Old'];
            list($executeCdNew, $periodCdNew, $chairCdNew, $testCdNew, $lineCntNew, $facilityNew, $testFacilityNew, $countLessonNew) = $Row['New'];
            if ($check[$executeCdNew][$periodCdNew][$chairCdNew]) {
                continue;
            } else {
                $check[$executeCdNew][$periodCdNew][$chairCdNew] = true;
            }

            //ATTEND_DAT INS
            if (isset($befAttendData[$executeCdOld][$periodCdOld][$chairCdOld])) {
                foreach ($befAttendData[$executeCdOld][$periodCdOld][$chairCdOld] as $befKey => $befVal) {
                    $data = array();
                    $data["SCHREGNO"][TEXT]       = $befVal["SCHREGNO"];
                    $data["ATTENDDATE"][DATE]     = $executeCdNew;
                    $data["PERIODCD"][TEXT]       = $periodCdNew;
                    $data["CHAIRCD"][TEXT]        = $chairCdNew;
                    $data["DI_CD"][TEXT]          = $befVal["DI_CD"];
                    $data["DI_REMARK_CD"][TEXT]   = $befVal["DI_REMARK_CD"];
                    $data["DI_REMARK"][TEXT]      = $befVal["DI_REMARK"];
                    $data["YEAR"][TEXT]           = $befVal["YEAR"];
                    $data["REGISTERCD"][TEXT]     = STAFFCD;
                    $data["UPDATED"][NUMBER]      = "sysdate()";

                    $query = Query::insertSQL($data, "ATTEND_DAT");
                    $db->query($query);
                }
            }
            //SCH_CHR_HRATE_DAT INS
            if (isset($befChrHrateData[$executeCdOld][$periodCdOld][$chairCdOld])) {
                foreach ($befChrHrateData[$executeCdOld][$periodCdOld][$chairCdOld] as $befKey => $befVal) {
                    $data = array();
                    $data["EXECUTEDATE"][DATE]    = $executeCdNew;
                    $data["PERIODCD"][TEXT]       = $periodCdNew;
                    $data["CHAIRCD"][TEXT]        = $chairCdNew;
                    $data["GRADE"][TEXT]          = $befVal["GRADE"];
                    $data["HR_CLASS"][TEXT]       = $befVal["HR_CLASS"];
                    $data["EXECUTED"][TEXT]       = $befVal["EXECUTED"];
                    $data["ATTESTOR"][TEXT]       = $befVal["ATTESTOR"];
                    $data["REGISTERCD"][TEXT]     = STAFFCD;
                    $data["UPDATED"][NUMBER]      = "sysdate()";

                    $query = Query::insertSQL($data, "SCH_CHR_HRATE_DAT");
                    $db->query($query);
                }
            }
            //SCH_CHR_DAT INS
            if (isset($befSchChrData[$executeCdOld][$periodCdOld][$chairCdOld])) {
                foreach ($befSchChrData[$executeCdOld][$periodCdOld][$chairCdOld] as $befKey => $befVal) {
                    $data = array();
                    $data["EXECUTEDATE"][DATE]    = $executeCdNew;
                    $data["PERIODCD"][TEXT]       = $periodCdNew;
                    $data["CHAIRCD"][TEXT]        = $chairCdNew;
                    $data["EXECUTED"][TEXT]       = $befVal["EXECUTED"];
                    $data["EXECUTEDIV"][TEXT]     = $befVal["EXECUTEDIV"];
                    $data["DATADIV"][TEXT]        = ($testCdNew == 0) ? 1 : 2;
                    $data["YEAR"][TEXT]           = $befVal["YEAR"];
                    $data["SEMESTER"][TEXT]       = $befVal["SEMESTER"];
                    $data["ATTESTOR"][TEXT]       = $befVal["ATTESTOR"];
                    $data["REGISTERCD"][TEXT]     = STAFFCD;
                    $data["UPDATED"][NUMBER]      = "sysdate()";

                    $query = Query::insertSQL($data, "SCH_CHR_DAT");
                    $db->query($query);
                }
            } else {
                $data = array();
                $data["EXECUTEDATE"][DATE]  = $executeCdNew;
                $data["PERIODCD"][TEXT]     = $periodCdNew;
                $data["CHAIRCD"][TEXT]      = $chairCdNew;
                $data["EXECUTED"][TEXT]     = 0;
                $data["EXECUTEDIV"][TEXT]   = 0;
                $data["DATADIV"][TEXT]      = ($testCdNew == 0) ? 1 : 2;
                $data["YEAR"][TEXT]         = $yearSeme[0];
                $data["SEMESTER"][TEXT]     = $yearSeme[1];
                $data["ATTESTOR"][TEXT]     = null;
                $data["REGISTERCD"][TEXT]   = STAFFCD;
                $data["UPDATED"][NUMBER]    = "sysdate()";

                $query = Query::insertSQL($data, "SCH_CHR_DAT");
                $db->query($query);
            }

            if ($testCdNew != '0') {
                $data = array();
                $data["EXECUTEDATE"][TEXT]     = $executeCdNew;
                $data["PERIODCD"][TEXT]        = $periodCdNew;
                $data["CHAIRCD"][TEXT]         = $chairCdNew;
                $data["TESTKINDCD"][TEXT]      = substr($testCdNew, 0, 2);
                $data["TESTITEMCD"][TEXT]      = substr($testCdNew, 2, 2);
                $data["EXECUTED"][TEXT]        = 0;
                $data["YEAR"][TEXT]            = $yearSeme[0];
                $data["SEMESTER"][TEXT]        = $yearSeme[1];
                $data["REGISTERCD"][TEXT]      = STAFFCD;
                $data["UPDATED"][NUMBER]       = "sysdate()";

                $query = Query::insertSQL($data, "SCH_CHR_TEST");
                $db->query($query);
            }

            //SCH_FAC_DAT INS
            if ($facilityNew != '0') {
                $facilityArray = explode(':', $facilityNew);
                foreach ($facilityArray as $facKey => $facVal) {
                    $data = array();
                    $data["EXECUTEDATE"][TEXT]    = $executeCdNew;
                    $data["PERIODCD"][TEXT]       = $periodCdNew;
                    $data["CHAIRCD"][TEXT]        = $chairCdNew;
                    $data["FACCD"][TEXT]          = $facVal;
                    $data["YEAR"][TEXT]           = $yearSeme[0];
                    $data["SEMESTER"][TEXT]       = $yearSeme[1];
                    $data["REGISTERCD"][TEXT]     = STAFFCD;
                    $data["UPDATED"][NUMBER]      = "sysdate()";

                    $query = Query::insertSQL($data, "SCH_FAC_DAT");
                    $db->query($query);
                }
            }

            if ($model->Properties["useTestFacility"] == "1") {
                //SCH_TEST_FAC_DAT INS
                if ($testFacilityNew != '0') {
                    $testFacilityArray = explode(':', $testFacilityNew);
                    foreach ($testFacilityArray as $facKey => $facVal) {
                        $data = array();
                        $data["EXECUTEDATE"][TEXT]    = $executeCdNew;
                        $data["PERIODCD"][TEXT]       = $periodCdNew;
                        $data["CHAIRCD"][TEXT]        = $chairCdNew;
                        $data["FACCD"][TEXT]          = $facVal;
                        $data["YEAR"][TEXT]           = $yearSeme[0];
                        $data["SEMESTER"][TEXT]       = $yearSeme[1];
                        $data["REGISTERCD"][TEXT]     = STAFFCD;
                        $data["UPDATED"][NUMBER]      = "sysdate()";

                        $query = Query::insertSQL($data, "SCH_TEST_FAC_DAT");
                        $db->query($query);
                    }
                }
            }

            //SCH_CHR_COUNTFLG INS
            if (strpos($countLessonNew, '00-000/1-00') !== false) {
                $query = knjb3042Query::getChairClassUpdSql($model, $chairCdNew);
                $result = $db->query($query);
                while ($clRow = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                    $data = array();
                    $data["EXECUTEDATE"][TEXT]  = $executeCdNew;
                    $data["PERIODCD"][TEXT]     = $periodCdNew;
                    $data["CHAIRCD"][TEXT]      = $chairCdNew;
                    $data["GRADE"][TEXT]        = $clRow["TRGTGRADE"];
                    $data["HR_CLASS"][TEXT]     = $clRow["TRGTCLASS"];
                    $data["COUNTFLG"][TEXT]     = $clRow["COUNTFLG"];
                    $data["LESSON_MODE"][TEXT]  = "00";
                    $data["REGISTERCD"][TEXT]   = STAFFCD;
                    $data["UPDATED"][NUMBER]    = "sysdate()";

                    $query = Query::insertSQL($data, "SCH_CHR_COUNTFLG");
                    $db->query($query);
                }
                $result->free();
            } else {
                $countLessonArray = explode(':', $countLessonNew);
                foreach ($countLessonArray as $clKey => $clVal) {
                    list($gradeHr, $countLesson) = explode('/', $clVal);
                    list($grade, $hrClass) = explode('-', $gradeHr);
                    list($countFlg, $lessonMode) = explode('-', $countLesson);

                    $data = array();
                    $data["EXECUTEDATE"][TEXT]  = $executeCdNew;
                    $data["PERIODCD"][TEXT]     = $periodCdNew;
                    $data["CHAIRCD"][TEXT]      = $chairCdNew;
                    $data["GRADE"][TEXT]        = $grade;
                    $data["HR_CLASS"][TEXT]     = $hrClass;
                    $data["COUNTFLG"][TEXT]     = $countFlg;
                    $data["LESSON_MODE"][TEXT]  = $lessonMode;
                    $data["REGISTERCD"][TEXT]   = STAFFCD;
                    $data["UPDATED"][NUMBER]    = "sysdate()";

                    $query = Query::insertSQL($data, "SCH_CHR_COUNTFLG");
                    $db->query($query);
                }
            }
        }
    }

    //講座受講クラス
    public function getChairClassUpdSql($model, $chairCd)
    {
        $query  = " SELECT ";
        $query .= "     CHAIR_CLS.TRGTGRADE, ";
        $query .= "     CHAIR_CLS.TRGTCLASS, ";
        $query .= "     CHAIR_D.COUNTFLG ";
        $query .= " FROM ";
        $query .= "     CHAIR_CLS_DAT CHAIR_CLS ";
        $query .= "     INNER JOIN CHAIR_DAT CHAIR_D ON CHAIR_CLS.YEAR = CHAIR_D.YEAR ";
        $query .= "           AND CHAIR_CLS.SEMESTER = CHAIR_D.SEMESTER ";
        $query .= "           AND CHAIR_CLS.CHAIRCD = CHAIR_D.CHAIRCD ";
        $query .= " WHERE ";
        $query .= "     CHAIR_CLS.YEAR || '-' || CHAIR_CLS.SEMESTER = '{$model->yearSeme}' ";
        $query .= "     AND CHAIR_CLS.GROUPCD = '0000' ";
        $query .= "     AND CHAIR_CLS.CHAIRCD = '{$chairCd}' ";
        $query .= " UNION ";
        $query .= " SELECT ";
        $query .= "     CHAIR_CLS.TRGTGRADE, ";
        $query .= "     CHAIR_CLS.TRGTCLASS, ";
        $query .= "     CHAIR_D.COUNTFLG ";
        $query .= " FROM ";
        $query .= "     CHAIR_CLS_DAT CHAIR_CLS ";
        $query .= "     INNER JOIN CHAIR_DAT CHAIR_D ON CHAIR_CLS.YEAR = CHAIR_D.YEAR ";
        $query .= "           AND CHAIR_CLS.SEMESTER = CHAIR_D.SEMESTER ";
        $query .= "           AND CHAIR_CLS.GROUPCD = CHAIR_D.GROUPCD ";
        $query .= "           AND CHAIR_D.CHAIRCD = '{$chairCd}' ";
        $query .= " WHERE ";
        $query .= "     CHAIR_CLS.YEAR || '-' || CHAIR_CLS.SEMESTER = '{$model->yearSeme}' ";
        $query .= "     AND CHAIR_CLS.GROUPCD != '0000' ";

        return $query;
    }

    //特別講師確認用
    public function getChairStffExistCnt($db, $model)
    {
        $cache = array();
        foreach ($model->chairStff as $setKey => $staff) {
            list($executeCd, $periodCd, $chairCd) = explode("_", $setKey);
            $periodCd = $model->periYomikae[$periodCd];

            $query  = " DELETE FROM ";
            $query .= "     SCH_STF_DAT ";
            $query .= " WHERE ";
            $query .= "     EXECUTEDATE = '{$executeCd}' ";
            $query .= "     AND CHAIRCD = '{$chairCd}' ";
            if (!isset($cache[$query])) {
                $db->query($query);
                $cache[$query] =true;
            }
        }
        foreach ($model->chairStff as $setKey => $staff) {
            list($executeCd, $periodCd, $chairCd) = explode("_", $setKey);
            $periodCd = $model->periYomikae[$periodCd];

            $setInstate = "('".join("','", $staff)."')";

            $query  = " SELECT ";
            $query .= "     COUNT(*) AS CNT ";
            $query .= " FROM ";
            $query .= "     CHAIR_STF_DAT ";
            $query .= " WHERE ";
            $query .= "     YEAR || '-' || SEMESTER = '{$model->yearSeme}' ";
            $query .= "     AND CHAIRCD = '{$chairCd}' ";
            $query .= "     AND STAFFCD IN {$setInstate} ";
            $query .= "     AND CHARGEDIV = 1 ";

            $checkCnt = $db->getOne($query);

            $query  = " SELECT ";
            $query .= "     COUNT(*) AS CNT ";
            $query .= " FROM ";
            $query .= "     CHAIR_STF_DAT ";
            $query .= " WHERE ";
            $query .= "     YEAR || '-' || SEMESTER = '{$model->yearSeme}' ";
            $query .= "     AND CHAIRCD = '{$chairCd}' ";
            $query .= "     AND CHARGEDIV = 1 ";

            $checkCnt2 = $db->getOne($query);
            if ($checkCnt != $checkCnt2 || get_count($staff) != $checkCnt2) {
                foreach ($staff as $stfKey => $staffCd) {
                    $data = array();
                    $data["EXECUTEDATE"][TEXT]     = $executeCd;
                    $data["PERIODCD"][TEXT]        = $periodCd;
                    $data["CHAIRCD"][TEXT]         = $chairCd;
                    $data["STAFFCD"][TEXT]         = $staffCd;
                    $data["REGISTERCD"][TEXT]      = STAFFCD;
                    $data["UPDATED"][NUMBER]       = "sysdate()";

                    $query = Query::insertSQL($data, "SCH_STF_DAT");
                    $db->query($query);
                }
            }
        }
    }

    /**
     * 変更通知の登録
     */
    public function insertOperationHistory($db, $model)
    {

        // 授業形態一覧取得
        $model->lessonMst = array();
        $query = knjb3042Query::getLessonModeAjax($model);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $model->lessonMst[$row['VALUE']] = $row;
        }

        // クラス(HR-CLASS)一覧取得
        $model->hrClassMst = array();
        $query = knjb3042Query::getHrName($model);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $model->hrClassMst[$row['VALUE']] = $row;
        }

        // 施設一覧取得
        $model->facilityMst = array();
        $query = knjb3042Query::getFacilityAjax($model);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $model->facilityMst[$row['FACCD']] = $row;
        }

        // 試験会場一覧取得
        $model->testFacilityMst = array();
        $query = knjb3042Query::getFacilityAjax($model);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $model->testFacilityMst[$row['FACCD']] = $row;
        }

        // 職員一覧取得
        $model->staffMst = array();
        $query = knjb3042Query::getStaff2($model);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $model->staffMst[$row['VALUE']] = $row;
        }
        // 講座一覧取得
        $model->chairMst = array();
        $query = knjb3042Query::getChairAll($model);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $model->chairMst[$row['CHAIRCD']] = $row;
        }

        // 校時一覧取得
        $model->periodMst = array();
        $namecd = ($model->Properties["KNJB3042_SchTestPattern"] == "1" && $model->field['SCH_DIV'] == '3') ? 'B004' : 'B001';
        $query = knjb3042Query::getPeriod($model, $namecd);
        $result = $db->query($query);
        $periCnt = 1;
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $model->periodMst[$periCnt] = $row;
            $periCnt++;
        }


        // 考査種別
        // getTestCombo
        $model->testMst = array();
        $query = knjb3042Query::getTestCombo($model);
        $result = $db->query($query);
        $periCnt = 1;
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $model->testMst[$row['VALUE']] = $row;
        }

        // 講座-担当職員の一覧を取得(テーブルへ登録する通知単位)
        $chairStaffList = array();

        $startDate = str_replace('/', '-', $model->startDate);
        $endDate = str_replace('/', '-', $model->endDate);

        $query  = "";
        $query .= "SELECT DISTINCT CHAIRCD, STAFFCD ";
        $query .= "FROM CHAIR_STF_DAT ";
        $query .= "WHERE YEAR = '{$model->year}' ";
        $query .= "  AND SEMESTER = '{$model->semester}' ";
        $query .= "  AND CHARGEDIV = 1 ";
        $query .= "UNION ";
        $query .= "SELECT DISTINCT CHAIRCD, STAFFCD ";
        $query .= "FROM SCH_STF_DAT ";
        $query .= "WHERE EXECUTEDATE BETWEEN '{$startDate}' AND '{$endDate}' ";
        $query .= "  AND STAFFCD IN (SELECT STAFFCD FROM V_STAFF_MST WHERE YEAR = '{$model->year}') ";
        $query .= "ORDER BY CHAIRCD, STAFFCD ";
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $chairStaffList[$row['CHAIRCD']][] = $row['STAFFCD'];
        }
        $result->free();

        $procDate = date("Y-m-d");
        // 登録日付の SEQ を取得
        $query = "";
        $query .= "SELECT VALUE(MAX(SEQ), 0) MAXSEQ ";
        $query .= "FROM NOTICE_MESSAGE_DAT ";
        $query .= "WHERE REGIST_DATE = '{$procDate}'";
        $seq = $db->getOne($query);
        $seq++;

        $opeHistory = $model->operationHistory;
        for ($i=0; $i < get_count($opeHistory); $i++) {
            $opeLog = $opeHistory[$i];

            // 講座
            $chairList = array();
            // 通知職員
            $noticeStaff = array();
            if (isset($opeLog['FROM_CHAIRCD'])) {
                for ($chairCnt=0; $chairCnt < get_count($opeLog['FROM_CHAIRCD']); $chairCnt++) {
                    $chairCd = $opeLog['FROM_CHAIRCD'][$chairCnt];
                    if (!in_array($chairCd, $chairList)) {
                        $chairList[] = $chairCd;
                    }

                    $staffList = $chairStaffList[$chairCd];
                    if (isset($staffList)) {
                        for ($stfCnt=0; $stfCnt < get_count($staffList); $stfCnt++) {
                            $staffCd = $staffList[$stfCnt];
                            if (!in_array($staffCd, $noticeStaff)) {
                                $noticeStaff[] = $staffCd;
                            }
                        }
                    }
                }
            }
            if (isset($opeLog['TO_CHAIRCD'])) {
                for ($chairCnt=0; $chairCnt < get_count($opeLog['TO_CHAIRCD']); $chairCnt++) {
                    $chairCd = $opeLog['TO_CHAIRCD'][$chairCnt];
                    if (!in_array($chairCd, $chairList)) {
                        $chairList[] = $chairCd;
                    }

                    $staffList = $chairStaffList[$chairCd];
                    if (isset($staffList)) {
                        for ($stfCnt=0; $stfCnt < get_count($staffList); $stfCnt++) {
                            $staffCd = $staffList[$stfCnt];
                            if (!in_array($staffCd, $noticeStaff)) {
                                $noticeStaff[] = $staffCd;
                            }
                        }
                    }
                }
            }


            for ($chairCnt=0; $chairCnt < get_count($chairList); $chairCnt++) {
                $chairCd = $chairList[$chairCnt];
                // 変更通知のメッセージを作成
                $opeMsg = knjb3042Query::createOperationHistoryMessage($model, $opeLog, $chairCd);

                for ($staffCnt=0; $staffCnt < get_count($noticeStaff); $staffCnt++) {
                    $staffCd = $noticeStaff[$staffCnt];

                    $data = array();
                    $data["REGIST_DATE"][TEXT]      = $procDate;
                    $data["SEQ"][NUMBER]            = $seq;

                    $data["STAFFCD"][TEXT]          = $staffCd;
                    $data["FROM_DATE"][TEXT]        = $opeLog['FROM_EXECUTEDATE'];
                    $data["FROM_PERIODCD"][TEXT]    = $model->periYomikae[$opeLog['FROM_PERIODCD']];
                    $data["FROM_CHAIRCD"][TEXT]     = $opeLog['FROM_CHAIRCD'] ? $chairCd : '';
                    $data["TO_DATE"][TEXT]          = $opeLog['TO_EXECUTEDATE'];
                    $data["TO_PERIODCD"][TEXT]      = $model->periYomikae[$opeLog['TO_PERIODCD']];
                    $data["TO_CHAIRCD"][TEXT]       = $opeLog['TO_CHAIRCD'] ? $chairCd : '';

                    $data["NOTICE_MESSAGE"][TEXT]   = $opeMsg;
                    $data["CANCEL_FLG"][TEXT]       = "0";

                    $data["REGISTERCD"][TEXT]      = STAFFCD;
                    $data["UPDATED"][NUMBER]       = "sysdate()";

                    $query = Query::insertSQL($data, "NOTICE_MESSAGE_DAT");
                    $db->query($query);
                    $seq++;
                }
            }
        }
        return;
    }


    public function createOperationHistoryMessage($model, $opeLog, $chairCd)
    {

        // 変更通知メッセージ
        $opeLogMessage = array(
              "ADD"         => "【講座追加】%toDate% %toPeriod%(%toChairCd%) を追加"
            , "MOVE"        => "【講座移動】%fromDate% %fromPeriod%(%fromChairCd%) → %toDate% %toPeriod%(%toChairCd%) へ移動"
            , "DEL"         => "【講座削除】%fromDate% %fromPeriod%(%fromChairCd%) を削除"
            , "STAFF"       => "【職員変更】%fromDate% %fromPeriod%(%fromChairCd%)%fromStaff% → %toDate% %toPeriod%(%toChairCd%)%toStaff% へ変更"
            , "FAC"         => "【施設変更】%fromDate% %fromPeriod%(%fromChairCd%)%fromFac% → %toDate% %toPeriod%(%toChairCd%)%toFac% へ変更"
            , "TESTFAC"     => "【施設変更】%fromDate% %fromPeriod%(%fromChairCd%)%fromTestFac% → %toDate% %toPeriod%(%toChairCd%)%toTestFac% へ変更"
            , "TEST"        => "【時間割種別変更】%fromDate% %fromPeriod%(%fromChairCd%)%fromTest% → %toDate% %toPeriod%(%toChairCd%)%toTest% へ変更"
            , "COUNTLESSON" => "【集計フラグ/授業形態変更】%fromDate% %fromPeriod%(%fromChairCd%)%fromCountLesson% → %toDate% %toPeriod%(%toChairCd%)%toCountLesson% へ変更"
        );

        // メッセージ文字列取得
        $opeMsg = $opeLogMessage[$opeLog['OPEMODE']];

        // TODO: コード値 を メッセージ用の名称へ変換する
        $messageParamList['%opeMode%'] = $opeLog['OPEMODE'];

        $messageParamList['%fromDate%'] = str_replace('-', '/', $opeLog['FROM_EXECUTEDATE']);

        // 校時
        if ($opeLog['FROM_PERIODCD']) {
            $messageParamList['%fromPeriod%'] = $model->periodMst[$opeLog['FROM_PERIODCD']]['LABEL'];
        } else {
            $messageParamList['%fromPeriod%'] = '(なし)';
        }

        // 講座
        // $messageParamList['%fromChairCd%'] = $opeLog['FROM_CHAIRCD'] ? $chairCd : '';
        if ($opeLog['FROM_CHAIRCD']) {
            $messageParamList['%fromChairCd%'] = $chairCd.':'.$model->chairMst[$chairCd]['CHAIRABBV'];
        } else {
            $messageParamList['%fromChairCd%'] = '(なし)';
        }

        $messageParamList['%toDate%'] = str_replace('-', '/', $opeLog['TO_EXECUTEDATE']);

        // 校時
        // $messageParamList['%toPeriod%'] = $opeLog['TO_PERIODCD'];
        if ($opeLog['TO_PERIODCD']) {
            $messageParamList['%toPeriod%'] = $model->periodMst[$opeLog['TO_PERIODCD']]['LABEL'];
        } else {
            $messageParamList['%toPeriod%'] = '';
        }

        // 講座
        // $messageParamList['%toChairCd%'] = $opeLog['TO_CHAIRCD'] ? $chairCd : '';
        if ($opeLog['TO_CHAIRCD']) {
            $messageParamList['%toChairCd%'] = $chairCd.':'.$model->chairMst[$chairCd]['CHAIRABBV'];
        } else {
            $messageParamList['%toChairCd%'] = '(なし)';
        }

        // 担当職員
        // $messageParamList['%fromStaff%'] = $opeLog['FROM_STAFF'];
        if ($opeLog['FROM_STAFF']) {
            $messageParamList['%fromStaff%'] = $opeLog['FROM_STAFF'].':'.$model->staffMst[$opeLog['FROM_STAFF']]['LABEL'];
        } else {
            $messageParamList['%fromStaff%'] = '(不明)';
        }

        // $messageParamList['%toStaff%'] = $opeLog['TO_STAFF'];
        if ($opeLog['TO_STAFF']) {
            $messageParamList['%toStaff%'] = $opeLog['TO_STAFF'].':'.$model->staffMst[$opeLog['TO_STAFF']]['LABEL'];
        } else {
            $messageParamList['%toStaff%'] = '(不明)';
        }

        // 施設
        // $messageParamList['%fromFac%'] = $opeLog['FROM_FAC'] ? $chairCd : '(なし)';
        if ($opeLog['FROM_FAC']) {
            $paramList = explode(':', $opeLog['FROM_FAC']);
            $nameList = array();
            for ($i=0; $i < get_count($paramList); $i++) {
                $nameList[] = $model->facilityMst[$paramList[$i]]['FACILITYNAME'];
            }
            $messageParamList['%fromFac%'] = implode(',', $nameList);
        } else {
            $messageParamList['%fromFac%'] = '(なし)';
        }
        // $messageParamList['%toFac%'] = $opeLog['TO_FAC'] ? $chairCd : '(なし)';
        if ($opeLog['TO_FAC']) {
            $paramList = explode(':', $opeLog['TO_FAC']);
            $nameList = array();
            for ($i=0; $i < get_count($paramList); $i++) {
                $nameList[] = $model->facilityMst[$paramList[$i]]['FACILITYNAME'];
            }
            $messageParamList['%toFac%'] = implode(',', $nameList);
        } else {
            $messageParamList['%toFac%'] = '(なし)';
        }

        // 試験会場
        // $messageParamList['%fromTestFac%'] = $opeLog['FROM_TESTFAC'] ? $chairCd : '(なし)';
        if ($opeLog['FROM_TESTFAC']) {
            $paramList = explode(':', $opeLog['FROM_TESTFAC']);
            $nameList = array();
            for ($i=0; $i < get_count($paramList); $i++) {
                $nameList[] = $model->testFacilityMst[$paramList[$i]]['FACILITYNAME'];
            }
            $messageParamList['%fromTestFac%'] = implode(',', $nameList);
        } else {
            $messageParamList['%fromTestFac%'] = '(なし)';
        }
        // $messageParamList['%toTestFac%'] = $opeLog['TO_TESTFAC'] ? $chairCd : '(なし)';
        if ($opeLog['TO_TESTFAC']) {
            $paramList = explode(':', $opeLog['TO_TESTFAC']);
            $nameList = array();
            for ($i=0; $i < get_count($paramList); $i++) {
                $nameList[] = $model->testFacilityMst[$paramList[$i]]['FACILITYNAME'];
            }
            $messageParamList['%toTestFac%'] = implode(',', $nameList);
        } else {
            $messageParamList['%toTestFac%'] = '(なし)';
        }

        // 考査種別
        $messageParamList['%fromTest%'] = $opeLog['FROM_TEST'];
        if ($opeLog['FROM_TEST']) {
            $messageParamList['%fromTest%'] = $model->testMst[$opeLog['FROM_TEST']]['LABEL'];
        } else {
            $messageParamList['%fromTest%'] = '通常';
        }

        $messageParamList['%toTest%'] = $opeLog['TO_TEST'];
        if ($opeLog['TO_TEST']) {
            $messageParamList['%toTest%'] = $model->testMst[$opeLog['TO_TEST']]['LABEL'];
        } else {
            $messageParamList['%toTest%'] = '通常';
        }

        // 集計フラグ-授業形態
        // $messageParamList['%fromCountLesson%'] = $opeLog['FROM_COUNTLESSON'];
        if ($opeLog['FROM_COUNTLESSON']) {
            $paramList = explode(':', $opeLog['FROM_COUNTLESSON']);
            $nameList = array();
            for ($i=0; $i < get_count($paramList); $i++) {
                list($hrClass, $param) = explode("/", $paramList[$i]);
                list($countFlg, $lesson) = explode("-", $param);
                $name = $model->hrClassMst[$hrClass]['LABEL'];
                $name .= '(';
                $name .= $countFlg == '1' ? '集計あり' : '集計なし';
                $name .= ' 授業形態:'.$model->lessonMst[$lesson]['LABEL'];
                $name .= ')';
                $nameList[] = $name;
            }
            $messageParamList['%fromCountLesson%'] = implode(',', $nameList);
        } else {
            $messageParamList['%fromCountLesson%'] = '(なし)';
        }

        // $messageParamList['%toCountLesson%'] = $opeLog['TO_COUNTLESSON'];
        if ($opeLog['TO_COUNTLESSON']) {
            $paramList = explode(':', $opeLog['TO_COUNTLESSON']);
            $nameList = array();
            for ($i=0; $i < get_count($paramList); $i++) {
                list($hrClass, $param) = explode("/", $paramList[$i]);
                list($countFlg, $lesson) = explode("-", $param);
                $name = $model->hrClassMst[$hrClass]['LABEL'];
                $name .= '(';
                $name .= $countFlg == '1' ? '集計あり' : '集計なし';
                $name .= ' 授業形態:'.$model->lessonMst[$lesson]['LABEL'];
                $name .= ')';
                $nameList[] = $name;
            }
            $messageParamList['%toCountLesson%'] = implode(',', $nameList);
        } else {
            $messageParamList['%toCountLesson%'] = '(なし)';
        }

        // メッセージの置換
        foreach ($messageParamList as $key => $value) {
            $opeMsg = str_replace($key, $value, $opeMsg);
        }
        return $opeMsg;
    }

    public function selectOpelog($model)
    {
        $procDate = date("Y-m-d");
        $startDate = str_replace('/', '-', $model->startDate);
        $endDate = str_replace('/', '-', $model->endDate);

        $query = "";
        $query .= " SELECT ";
        $query .= "    MSG.REGIST_DATE ";
        $query .= "  , MSG.SEQ ";
        $query .= "  , MSG.STAFFCD ";
        $query .= "  , STAFF.STAFFNAME_SHOW ";
        $query .= "  , MSG.NOTICE_MESSAGE ";
        $query .= "  , MSG.CANCEL_FLG ";

        $query .= " FROM NOTICE_MESSAGE_DAT MSG ";
        $query .= " INNER JOIN V_STAFF_MST STAFF ";
        $query .= " ON MSG.STAFFCD = STAFF.STAFFCD ";
        $query .= " AND STAFF.YEAR = '{$model->year}' ";

        $query .= " WHERE (REGIST_DATE = '{$procDate}' ";
        $query .= "    OR ( '{$startDate}' <= VALUE(FROM_DATE, TO_DATE) AND VALUE(TO_DATE, FROM_DATE) <= '{$endDate}' ) ) ";

        if ($model->ajaxParam) {
            if ($model->ajaxParam['CANCEL_SHOW'] == '0') {
                $query .= " AND CANCEL_FLG = '0' ";
            } else {
                // $query .= " AND CANCEL_FLG = '0' ";
            }
        } else {
            $query .= " AND CANCEL_FLG = '0' ";
        }

        $query .= " ORDER BY REGIST_DATE, SEQ ";

        return $query;
    }

    // 変更通知履歴の更新
    public function updateOperationHistory($db, $model)
    {
        if ($model->ajaxParam) {
            for ($i=0; $i < get_count($model->ajaxParam); $i++) {
                $param = $model->ajaxParam[$i];

                $procDate = str_replace('/', '-', $param['REGISTDATE']);

                $data = array();
                $data["NOTICE_MESSAGE"][TEXT] = $param['MESSAGE'];
                $data["CANCEL_FLG"][TEXT]     = $param['CANCEL'];

                $where  = " WHERE REGIST_DATE = '{$procDate}'";
                $where .= "   AND SEQ  = '{$param['SEQ']}'";

                $query = Query::updateSQL($data, "NOTICE_MESSAGE_DAT", $where);
                $db->query($query);
            }
        }
    }


    public function updatePtrn($db, $model)
    {
        $yearSeme = explode('-', $model->yearSeme);

        $updateData = array('Add' => array(),
                            'AddChairCdNew' => array(),
                            'AddChairCdOld' => array(),
                            'Del' => array(),
                            'DelChairCd' => array());

        if (!empty($model->updateAddData)) {
            $updateAddDataList = explode('|', $model->updateAddData);
            foreach ($updateAddDataList as $key => $value) {
                $AddParts = explode(",", $value);
                $oldData = explode("_", $AddParts[0]);
                $newData = explode("_", $AddParts[1]);
                //曜日の読替
                $oldData[0] = $model->weekPtrn[$oldData[0]]['CD'];
                $newData[0] = $model->weekPtrn[$newData[0]]['CD'];
                //校時の読替
                $oldData[1] = $model->periYomikae[$oldData[1]];
                $newData[1] = $model->periYomikae[$newData[1]];
                $updateData['Add'][] = array('Old' => $oldData, 'New'=>$newData);
                //追加した講座は、OLDの最終に'Add'と入っている。
                if ($oldData[get_count($oldData) - 1] != 'Add') {
                    //追加ではない時、OLDの講座番号をセット
                    //出欠情報の移動用。デフォルト表示のみ対象
                    $updateData['AddChairCdOld'][] = $oldData[2];
                }
                $updateData['AddChairCdNew'][] = $newData[2];
            }
        }

        if (!empty($model->updateDelData)) {
            $updateDelDataList = explode('|', $model->updateDelData);
            foreach ($updateDelDataList as $key => $value) {
                $AddParts = explode(",", $value);
                $oldData = explode("_", $AddParts[0]);
                //曜日の読替
                $oldData[0] = $model->weekPtrn[$oldData[0]]['CD'];
                //校時読替
                $oldData[1] = $model->periYomikae[$oldData[1]];
                $updateData['Del'][] = $oldData;
                $updateData['DelChairCd'][] = $oldData[2];
            }
        }

        $delPeriArray = array_merge($updateData['Add'], $updateData['Del']);
        foreach ($delPeriArray as $upAddKey => $upAddVal) {
            if (isset($upAddVal['Old'])) {
                list($executeCdOld, $periodCdOld, $chairCdOld, $testCdOld, $lineCntOld, $facilityOld, $testFacilityOld, $countLessonOld) = $upAddVal['Old'];
            } else {
                list($executeCdOld, $periodCdOld, $chairCdOld, $testCdOld, $lineCntOld, $facilityOld, $testFacilityOld, $countLessonOld) = $upAddVal;
            }
            //SCH_PTRN_DAT DEL
            $query  = " DELETE FROM SCH_PTRN_DAT WHERE ";
            $query .= "     YEAR = '{$model->year}' ";
            $query .= "     AND SEMESTER = '{$model->semester}' ";
            $query .= "     AND BSCSEQ = {$model->field["BSCSEQ"]} ";
            $query .= "     AND DAYCD = '{$executeCdOld}' ";
            $query .= "     AND PERIODCD = '{$periodCdOld}' ";
            $query .= "     AND CHAIRCD = '{$chairCdOld}' ";
            $db->query($query);

            //SCH_PTRN_FAC_DAT DEL
            $query  = " DELETE FROM SCH_PTRN_FAC_DAT WHERE ";
            $query .= "     YEAR = '{$model->year}' ";
            $query .= "     AND SEMESTER = '{$model->semester}' ";
            $query .= "     AND BSCSEQ = {$model->field["BSCSEQ"]} ";
            $query .= "     AND DAYCD = '{$executeCdOld}' ";
            $query .= "     AND PERIODCD = '{$periodCdOld}' ";
            $query .= "     AND CHAIRCD = '{$chairCdOld}' ";
            $db->query($query);

            if ($model->Properties["useTestFacility"] == "1") {
                //SCH_PTRN_TEST_FAC_DAT DEL
                $query  = " DELETE FROM SCH_PTRN_TEST_FAC_DAT WHERE ";
                $query .= "     YEAR = '{$model->year}' ";
                $query .= "     AND SEMESTER = '{$model->semester}' ";
                $query .= "     AND BSCSEQ = {$model->field["BSCSEQ"]} ";
                $query .= "     AND DAYCD = '{$executeCdOld}' ";
                $query .= "     AND PERIODCD = '{$periodCdOld}' ";
                $query .= "     AND CHAIRCD = '{$chairCdOld}' ";
                $db->query($query);
            }

            //SCH_PTRN_COUNTFLG_DAT DEL
            $query  = " DELETE FROM SCH_PTRN_COUNTFLG_DAT WHERE ";
            $query .= "     YEAR = '{$model->year}' ";
            $query .= "     AND SEMESTER = '{$model->semester}' ";
            $query .= "     AND BSCSEQ = {$model->field["BSCSEQ"]} ";
            $query .= "     AND DAYCD = '{$executeCdOld}' ";
            $query .= "     AND PERIODCD = '{$periodCdOld}' ";
            $query .= "     AND CHAIRCD = '{$chairCdOld}' ";
            $db->query($query);
        }


        $check = array();
        foreach ($updateData['Add'] as $key => $Row) {
            list($executeCdOld, $periodCdOld, $chairCdOld, $testCdOld, $lineCntOld, $facilityOld, $testFacilityOld, $countLessonOld) = $Row['Old'];
            list($executeCdNew, $periodCdNew, $chairCdNew, $testCdNew, $lineCntNew, $facilityNew, $testFacilityNew, $countLessonNew) = $Row['New'];
            if ($check[$executeCdNew][$periodCdNew][$chairCdNew]) {
                continue;
            } else {
                $check[$executeCdNew][$periodCdNew][$chairCdNew] = true;
            }

            $data = array();
            $data["YEAR"][TEXT]         = $model->year;
            $data["SEMESTER"][TEXT]     = $model->semester;
            $data["BSCSEQ"][NUMBER]     = $model->field["BSCSEQ"];
            $data["DAYCD"][TEXT]        = $executeCdNew;
            $data["PERIODCD"][TEXT]     = $periodCdNew;
            $data["CHAIRCD"][TEXT]      = $chairCdNew;
            $data["REGISTERCD"][TEXT]   = STAFFCD;
            $data["UPDATED"][NUMBER]    = "sysdate()";

            $query = Query::insertSQL($data, "SCH_PTRN_DAT");
            $db->query($query);

            //SCH_PTRN_FAC_DAT INS
            if ($facilityNew != '0') {
                $facilityArray = explode(':', $facilityNew);
                foreach ($facilityArray as $facKey => $facVal) {
                    $data = array();
                    $data["YEAR"][TEXT]         = $model->year;
                    $data["SEMESTER"][TEXT]     = $model->semester;
                    $data["BSCSEQ"][NUMBER]     = $model->field["BSCSEQ"];
                    $data["DAYCD"][TEXT]        = $executeCdNew;
                    $data["PERIODCD"][TEXT]     = $periodCdNew;
                    $data["CHAIRCD"][TEXT]      = $chairCdNew;
                    $data["FACCD"][TEXT]        = $facVal;
                    $data["REGISTERCD"][TEXT]   = STAFFCD;
                    $data["UPDATED"][NUMBER]    = "sysdate()";

                    $query = Query::insertSQL($data, "SCH_PTRN_FAC_DAT");
                    $db->query($query);
                }
            }

            if ($model->Properties["useTestFacility"] == "1") {
                //SCH_PTRN_TEST_FAC_DAT INS
                if ($testFacilityNew != '0') {
                    $testFacilityArray = explode(':', $testFacilityNew);
                    foreach ($testFacilityArray as $facKey => $facVal) {
                        $data = array();
                        $data["YEAR"][TEXT]         = $model->year;
                        $data["SEMESTER"][TEXT]     = $model->semester;
                        $data["BSCSEQ"][NUMBER]     = $model->field["BSCSEQ"];
                        $data["DAYCD"][TEXT]        = $executeCdNew;
                        $data["PERIODCD"][TEXT]     = $periodCdNew;
                        $data["CHAIRCD"][TEXT]      = $chairCdNew;
                        $data["FACCD"][TEXT]        = $facVal;
                        $data["REGISTERCD"][TEXT]   = STAFFCD;
                        $data["UPDATED"][NUMBER]    = "sysdate()";

                        $query = Query::insertSQL($data, "SCH_PTRN_TEST_FAC_DAT");
                        $db->query($query);
                    }
                }
            }

            //SCH_PTRN_COUNTFLG_DAT INS
            if (strpos($countLessonNew, '00-000/1-00') !== false) {
                $query = knjb3042Query::getChairClassUpdSql($model, $chairCdNew);
                $result = $db->query($query);
                while ($clRow = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                    $data = array();
                    $data["YEAR"][TEXT]         = $model->year;
                    $data["SEMESTER"][TEXT]     = $model->semester;
                    $data["BSCSEQ"][NUMBER]     = $model->field["BSCSEQ"];
                    $data["DAYCD"][TEXT]        = $executeCdNew;
                    $data["PERIODCD"][TEXT]     = $periodCdNew;
                    $data["CHAIRCD"][TEXT]      = $chairCdNew;
                    $data["GRADE"][TEXT]        = $clRow["TRGTGRADE"];
                    $data["HR_CLASS"][TEXT]     = $clRow["TRGTCLASS"];
                    $data["COUNTFLG"][TEXT]     = $clRow["COUNTFLG"];
                    $data["REGISTERCD"][TEXT]   = STAFFCD;
                    $data["UPDATED"][NUMBER]    = "sysdate()";

                    $query = Query::insertSQL($data, "SCH_PTRN_COUNTFLG_DAT");
                    $db->query($query);
                }
                $result->free();
            } else {
                $countLessonArray = explode(':', $countLessonNew);
                foreach ($countLessonArray as $clKey => $clVal) {
                    list($gradeHr, $countLesson) = explode('/', $clVal);
                    list($grade, $hrClass) = explode('-', $gradeHr);
                    list($countFlg, $lessonMode) = explode('-', $countLesson);

                    $data = array();
                    $data["YEAR"][TEXT]         = $model->year;
                    $data["SEMESTER"][TEXT]     = $model->semester;
                    $data["BSCSEQ"][NUMBER]     = $model->field["BSCSEQ"];
                    $data["DAYCD"][TEXT]        = $executeCdNew;
                    $data["PERIODCD"][TEXT]     = $periodCdNew;
                    $data["CHAIRCD"][TEXT]      = $chairCdNew;
                    $data["GRADE"][TEXT]        = $grade;
                    $data["HR_CLASS"][TEXT]     = $hrClass;
                    $data["COUNTFLG"][TEXT]     = $countFlg;
                    $data["REGISTERCD"][TEXT]   = STAFFCD;
                    $data["UPDATED"][NUMBER]    = "sysdate()";

                    $query = Query::insertSQL($data, "SCH_PTRN_COUNTFLG_DAT");
                    $db->query($query);
                }
            }
        }
    }

    //特別講師確認用
    public function getPtrnChairStffExistCnt($db, $model)
    {
        $cache = array();
        foreach ($model->chairStff as $setKey => $staff) {
            list($setDate, $periodCd, $chairCd) = explode("_", $setKey);
            //曜日の読替
            $executeCd = $model->weekPtrn[$setDate]['CD'];
            $periodCd = $model->periYomikae[$periodCd];

            $query  = " DELETE FROM ";
            $query .= "     SCH_PTRN_STF_DAT ";
            $query .= " WHERE ";
            $query .= "     YEAR = '{$model->year}' ";
            $query .= "     AND SEMESTER = '{$model->semester}' ";
            $query .= "     AND BSCSEQ = {$model->field['BSCSEQ']} ";
            $query .= "     AND DAYCD = '{$executeCd}' ";
            $query .= "     AND CHAIRCD = '{$chairCd}' ";
            if (!isset($cache[$query])) {
                $db->query($query);
                $cache[$query] =true;
            }
        }
        foreach ($model->chairStff as $setKey => $staff) {
            list($setDate, $periodCd, $chairCd) = explode("_", $setKey);
            //曜日の読替
            $executeCd = $model->weekPtrn[$setDate]['CD'];
            $periodCd = $model->periYomikae[$periodCd];

            $setInstate = "('".join("','", $staff)."')";

            $query  = " SELECT ";
            $query .= "     COUNT(*) AS CNT ";
            $query .= " FROM ";
            $query .= "     CHAIR_STF_DAT ";
            $query .= " WHERE ";
            $query .= "     YEAR || '-' || SEMESTER = '{$model->yearSeme}' ";
            $query .= "     AND CHAIRCD = '{$chairCd}' ";
            $query .= "     AND STAFFCD IN {$setInstate} ";
            $query .= "     AND CHARGEDIV = 1 ";

            $checkCnt = $db->getOne($query);

            $query  = " SELECT ";
            $query .= "     COUNT(*) AS CNT ";
            $query .= " FROM ";
            $query .= "     CHAIR_STF_DAT ";
            $query .= " WHERE ";
            $query .= "     YEAR || '-' || SEMESTER = '{$model->yearSeme}' ";
            $query .= "     AND CHAIRCD = '{$chairCd}' ";
            $query .= "     AND CHARGEDIV = 1 ";

            $checkCnt2 = $db->getOne($query);

            if ($checkCnt != $checkCnt2 || get_count($staff) != $checkCnt2) {
                foreach ($staff as $stfKey => $staffCd) {
                    $data = array();
                    $data["YEAR"][TEXT]         = $model->year;
                    $data["SEMESTER"][TEXT]     = $model->semester;
                    $data["BSCSEQ"][NUMBER]     = $model->field["BSCSEQ"];
                    $data["DAYCD"][TEXT]        = $executeCd;
                    $data["PERIODCD"][TEXT]     = $periodCd;
                    $data["CHAIRCD"][TEXT]      = $chairCd;
                    $data["STAFFCD"][TEXT]      = $staffCd;
                    $data["REGISTERCD"][TEXT]   = STAFFCD;
                    $data["UPDATED"][NUMBER]    = "sysdate()";

                    $query = Query::insertSQL($data, "SCH_PTRN_STF_DAT");
                    $db->query($query);
                }
            }
        }
    }
    public function updatePtrnTemplate($db, $model)
    {
        if ($model->field['PTRN_UPDATE_RADIO'] == 1) {
            $data = array();
            $data["TITLE"][TEXT]        = $model->field['PTRN_UPDATE_TITLE'];
            $data["REGISTERCD"][TEXT]   = STAFFCD;
            $data["UPDATED"][NUMBER]    = "sysdate()";

            $where  = " WHERE YEAR      = '{$model->year}'";
            $where .= "   AND SEMESTER  = '{$model->semester}'";
            $where .= "   AND BSCSEQ     = {$model->field['BSCSEQ']}";

            $query = Query::updateSQL($data, "SCH_PTRN_HDAT", $where);
            $db->query($query);

            $bscSeq = $model->field['BSCSEQ'];
        }
        if ($model->field['PTRN_UPDATE_RADIO'] == 2) {
            $query .= " SELECT ";
            $query .= "     MAX(BSCSEQ) ";
            $query .= " FROM ";
            $query .= "     SCH_PTRN_HDAT ";
            $query .= " WHERE ";
            $query .= "     YEAR = '{$model->year}' ";
            $query .= "     AND SEMESTER = '{$model->semester}' ";

            $bscSeq = $db->getOne($query);
            if (!isset($bscSeq)) {
                $bscSeq = 1;
            } else {
                $bscSeq++;
            }

            $data = array();
            $data["YEAR"][TEXT]         = $model->year;
            $data["SEMESTER"][TEXT]     = $model->semester;
            $data["BSCSEQ"][NUMBER]     = $bscSeq;
            $data["TITLE"][TEXT]        = $model->field['PTRN_UPDATE_TITLE'];
            $data["REGISTERCD"][TEXT]   = STAFFCD;
            $data["UPDATED"][NUMBER]    = "sysdate()";

            $query = Query::insertSQL($data, "SCH_PTRN_HDAT");
            $db->query($query);
        }
        return $bscSeq;
    }

    public function updateUnitSchChrRankDat($db, $model)
    {
        PrimarySchoolProcess::delInsUnitSchChrRankDat($db, CTRL_YEAR);

        return true;
    }

    //基本時間割 テンプレート削除
    public function deletePtrnTemplate($db, $model)
    {
        //SCH_PTRN_HDAT DEL
        $query  = " DELETE FROM SCH_PTRN_HDAT WHERE ";
        $query .= "     YEAR = '{$model->year}' ";
        $query .= "     AND SEMESTER = '{$model->semester}' ";
        $query .= "     AND BSCSEQ = {$model->field["BSCSEQ"]} ";
        $db->query($query);

        //SCH_PTRN_DAT DEL
        $query  = " DELETE FROM SCH_PTRN_DAT WHERE ";
        $query .= "     YEAR = '{$model->year}' ";
        $query .= "     AND SEMESTER = '{$model->semester}' ";
        $query .= "     AND BSCSEQ = {$model->field["BSCSEQ"]} ";
        $db->query($query);

        //SCH_PTRN_FAC_DAT DEL
        $query  = " DELETE FROM SCH_PTRN_FAC_DAT WHERE ";
        $query .= "     YEAR = '{$model->year}' ";
        $query .= "     AND SEMESTER = '{$model->semester}' ";
        $query .= "     AND BSCSEQ = {$model->field["BSCSEQ"]} ";
        $db->query($query);

        if ($model->Properties["useTestFacility"] == "1") {
            //SCH_PTRN_TEST_FAC_DAT DEL
            $query  = " DELETE FROM SCH_PTRN_TEST_FAC_DAT WHERE ";
            $query .= "     YEAR = '{$model->year}' ";
            $query .= "     AND SEMESTER = '{$model->semester}' ";
            $query .= "     AND BSCSEQ = {$model->field["BSCSEQ"]} ";
            $db->query($query);
        }

        //SCH_PTRN_COUNTFLG_DAT DEL
        $query  = " DELETE FROM SCH_PTRN_COUNTFLG_DAT WHERE ";
        $query .= "     YEAR = '{$model->year}' ";
        $query .= "     AND SEMESTER = '{$model->semester}' ";
        $query .= "     AND BSCSEQ = {$model->field["BSCSEQ"]} ";
        $db->query($query);

        //SCH_PTRN_STF_DAT DEL
        $query  = " DELETE FROM SCH_PTRN_STF_DAT WHERE ";
        $query .= "     YEAR = '{$model->year}' ";
        $query .= "     AND SEMESTER = '{$model->semester}' ";
        $query .= "     AND BSCSEQ = {$model->field['BSCSEQ']} ";
        $db->query($query);
    }


    //テスト考査時間割:講座の生徒数合計
    public function chairStdSum($model)
    {
        $query .= " SELECT ";
        $query .= "     EXECUTEDATE, ";
        $query .= "     PERIODCD, ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     SCH_CHR_DAT SCH_CHR ";
        $query .= "     INNER JOIN CHAIR_STD_DAT CHR_STD ON SCH_CHR.YEAR = CHR_STD.YEAR ";
        $query .= "          AND SCH_CHR.SEMESTER = CHR_STD.SEMESTER ";
        $query .= "          AND SCH_CHR.CHAIRCD = CHR_STD.CHAIRCD ";
        $query .= "          AND SCH_CHR.EXECUTEDATE BETWEEN CHR_STD.APPDATE AND CHR_STD.APPENDDATE ";
        $query .= "     LEFT JOIN CHAIR_DAT CHAIR ON SCH_CHR.YEAR = CHAIR.YEAR ";
        $query .= "          AND SCH_CHR.SEMESTER = CHAIR.SEMESTER ";
        $query .= "          AND SCH_CHR.CHAIRCD = CHAIR.CHAIRCD ";
        $query .= " WHERE ";
        $query .= "     SCH_CHR.EXECUTEDATE BETWEEN '".str_replace('/', '-', $model->startDate)."' AND '".str_replace('/', '-', $model->endDate)."' ";
        $query .= "     AND SCH_CHR.YEAR = '{$model->year}' ";
        $query .= "     AND SCH_CHR.SEMESTER = '{$model->semester}' ";
        $query .= "     AND SCH_CHR.DATADIV = '2' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= "           AND CHAIR.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind), "','")."') ";
            }
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= "     AND CHAIR.SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= " GROUP BY ";
        $query .= "     EXECUTEDATE, ";
        $query .= "     PERIODCD ";

        return $query;
    }

    //テスト考査時間割:講座の生徒数合計AJAX
    public function chairStdSumAJAX($model, $targetDay, $chairList)
    {
        $query .= " SELECT ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     CHAIR_STD_DAT CHR_STD ";
        $query .= " WHERE ";
        $query .= "     CHR_STD.YEAR = '{$model->year}' ";
        $query .= "     AND CHR_STD.CHAIRCD IN ('".join("','", $chairList)."') ";
        $query .= "     AND '{$targetDay}'BETWEEN CHR_STD.APPDATE AND CHR_STD.APPENDDATE ";

        return $query;
    }

    //テスト考査時間割:選択した校座の校座名簿に含まれる生徒が含まれる講座
    public function includingChairAJAX($model, $targetDay, $targetPeriod, $chairList)
    {
        $query .= " SELECT DISTINCT ";
        $query .= "     EXECUTEDATE, ";
        $query .= "     PERIODCD, ";
        $query .= "     SCH_CHR.CHAIRCD ";
        $query .= " FROM ";
        $query .= "     SCH_CHR_DAT SCH_CHR ";
        $query .= "     INNER JOIN CHAIR_STD_DAT CHR_STD ON SCH_CHR.YEAR = CHR_STD.YEAR ";
        $query .= "          AND SCH_CHR.SEMESTER = CHR_STD.SEMESTER ";
        $query .= "          AND SCH_CHR.CHAIRCD = CHR_STD.CHAIRCD ";
        $query .= "          AND SCH_CHR.EXECUTEDATE BETWEEN CHR_STD.APPDATE AND CHR_STD.APPENDDATE ";
        $query .= "          AND CHR_STD.SCHREGNO IN ( ";
        $query .= "             SELECT ";
        $query .= "                 SCHREGNO ";
        $query .= "             FROM ";
        $query .= "                 SCH_CHR_DAT SCH_CHR ";
        $query .= "                 INNER JOIN CHAIR_STD_DAT CHR_STD ON SCH_CHR.YEAR = CHR_STD.YEAR ";
        $query .= "                      AND SCH_CHR.SEMESTER = CHR_STD.SEMESTER ";
        $query .= "                      AND SCH_CHR.CHAIRCD = CHR_STD.CHAIRCD ";
        $query .= "                      AND SCH_CHR.EXECUTEDATE BETWEEN CHR_STD.APPDATE AND CHR_STD.APPENDDATE ";
        $query .= "                 LEFT JOIN CHAIR_DAT CHAIR ON SCH_CHR.YEAR = CHAIR.YEAR ";
        $query .= "                      AND SCH_CHR.SEMESTER = CHAIR.SEMESTER ";
        $query .= "                      AND SCH_CHR.CHAIRCD = CHAIR.CHAIRCD ";
        $query .= "             WHERE ";
        $query .= "                 SCH_CHR.EXECUTEDATE = '{$targetDay}' ";
        $query .= "                 AND SCH_CHR.YEAR = '{$model->year}' ";
        $query .= "                 AND SCH_CHR.SEMESTER = '{$model->semester}' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= "           AND CHAIR.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind), "','")."') ";
            }
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= "     AND CHAIR.SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= "                 AND SCH_CHR.PERIODCD = '{$targetPeriod}' ";
        $query .= "                 AND SCH_CHR.CHAIRCD IN ('".join("','", $chairList)."') ";
        $query .= "             ) ";
        $query .= "     LEFT JOIN CHAIR_DAT CHAIR ON SCH_CHR.YEAR = CHAIR.YEAR ";
        $query .= "          AND SCH_CHR.SEMESTER = CHAIR.SEMESTER ";
        $query .= "          AND SCH_CHR.CHAIRCD = CHAIR.CHAIRCD ";
        $query .= " WHERE ";
        $query .= "     SCH_CHR.EXECUTEDATE BETWEEN '".str_replace('/', '-', $model->startDate)."' AND '".str_replace('/', '-', $model->endDate)."' ";
        $query .= "     AND SCH_CHR.YEAR = '{$model->year}' ";
        $query .= "     AND SCH_CHR.SEMESTER = '{$model->semester}' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= "           AND CHAIR.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind), "','")."') ";
            }
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= "     AND CHAIR.SCHOOL_KIND = '".SCHOOLKIND."' ";
        }

        return $query;
    }

    //テスト考査時間割:選択した校座の校座名簿に含まれる生徒が含まれる講座
    public function dipliStdViewAJAX($model, $targetDay, $targetPeriod, $chairList)
    {
        $query .= " WITH CHAIR_STD AS ( ";
        $query .= "     SELECT DISTINCT ";
        $query .= "         CHR_STD.CHAIRCD, ";
        $query .= "         CHR_STD.SCHREGNO, ";
        $query .= "         T1.NAME_SHOW, ";
        $query .= "         T2.ATTENDNO, ";
        $query .= "         T3.CHAIRNAME ";
        $query .= "     FROM ";
        $query .= "         SCH_CHR_DAT SCH_CHR ";
        $query .= "     INNER JOIN CHAIR_STD_DAT CHR_STD ";
        $query .= "         ON SCH_CHR.YEAR = CHR_STD.YEAR ";
        $query .= "         AND SCH_CHR.SEMESTER = CHR_STD.SEMESTER ";
        $query .= "         AND SCH_CHR.CHAIRCD = CHR_STD.CHAIRCD ";
        $query .= "         AND SCH_CHR.EXECUTEDATE BETWEEN CHR_STD.APPDATE AND CHR_STD.APPENDDATE ";
        $query .= "     LEFT JOIN SCHREG_BASE_MST T1 ";
        $query .= "         ON T1.SCHREGNO = CHR_STD.SCHREGNO ";
        $query .= "     LEFT JOIN SCHREG_REGD_DAT T2 ";
        $query .= "         ON T2.YEAR = CHR_STD.YEAR ";
        $query .= "         AND T2.SEMESTER = CHR_STD.SEMESTER ";
        $query .= "         AND T2.SCHREGNO = CHR_STD.SCHREGNO ";
        $query .= "     LEFT JOIN CHAIR_DAT T3 ";
        $query .= "         ON T3.CHAIRCD = CHR_STD.CHAIRCD ";
        $query .= "         AND T3.YEAR = CHR_STD.YEAR ";
        $query .= "         AND T3.SEMESTER = CHR_STD.SEMESTER ";
        $query .= "     WHERE ";
        $query .= "         CHR_STD.YEAR = '{$model->year}' ";
        $query .= "         AND CHR_STD.SEMESTER = '{$model->semester}' ";
        $query .= "         AND '{$targetDay}' BETWEEN CHR_STD.APPDATE AND CHR_STD.APPENDDATE ";
        $query .= "         AND CHR_STD.CHAIRCD IN ('".join("','", $chairList)."') ";
        $query .= "         AND SCH_CHR.PERIODCD = '{$targetPeriod}' ";
        $query .= " )  ";
        $query .= " , STD_CHAIRCNT AS ( ";
        $query .= "     SELECT ";
        $query .= "         SCHREGNO, ";
        $query .= "         COUNT(CHAIRCD) CHAIRCNT ";
        $query .= "     FROM ";
        $query .= "         CHAIR_STD ";
        $query .= "     GROUP BY ";
        $query .= "         SCHREGNO ";
        $query .= " ) ";
        $query .= " SELECT DISTINCT ";
        $query .= "     CHAIR_STD.CHAIRCD, ";
        $query .= "     CHAIR_STD.SCHREGNO, ";
        $query .= "     CHAIR_STD.NAME_SHOW, ";
        $query .= "     CHAIR_STD.ATTENDNO, ";
        $query .= "     CHAIR_STD.CHAIRNAME ";
        $query .= " FROM ";
        $query .= "     CHAIR_STD ";
        $query .= " INNER JOIN STD_CHAIRCNT ";
        $query .= "     ON CHAIR_STD.SCHREGNO = STD_CHAIRCNT.SCHREGNO ";
        $query .= " WHERE ";
        $query .= "     STD_CHAIRCNT.CHAIRCNT > 1 ";
        $query .= " ORDER BY ";
        $query .= "     CHAIR_STD.SCHREGNO,CHAIR_STD.CHAIRCD ";

        return $query;
    }
}
