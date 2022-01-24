<?php

require_once('for_php7.php');

class knjc035fQuery extends Query
{
    public function getSecurityHigh()
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     MENU_HIGH_SECURITY_MST ";
        $query .= " WHERE ";
        $query .= "     PROGRAMID = 'KNJC035F' ";
        $query .= "     AND INVALID_FLG = '0' ";

        return $query;
    }

    //MIN権限グループ取得
    public function getMinGroupcd($model, $sk)
    {
        $query  = " SELECT ";
        $query .= "     SUBSTR(MIN((CASE GROUPCD WHEN '9999' THEN '0' ELSE '1' END) || GROUPCD),2) ";
        $query .= " FROM ";
        $query .= "     USERGROUP_DAT T1 ";
        $query .= " WHERE ";
        $query .= "     YEAR        = '".CTRL_YEAR."' AND ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            $query .= "     SCHOOLCD    = '".sprintf("%012d", SCHOOLCD)."' AND ";
            $query .= "     SCHOOL_KIND = '".SCHOOLKIND."' AND ";
        } elseif ($model->Properties["useSchool_KindField"] == "1") {
            $query .= "     SCHOOLCD    = '".sprintf("%012d", SCHOOLCD)."' AND ";
            $query .= "     SCHOOL_KIND = '".$sk."' AND ";
        }
        $query .= "     STAFFCD     = '".STAFFCD."' AND ";
        $query .= "     EXISTS (SELECT ";
        $query .= "                 'X' ";
        $query .= "             FROM ";
        $query .= "                 ADMIN_CONTROL_ATTEND_DAT E1 ";
        $query .= "             WHERE ";
        $query .= "                 T1.YEAR         = E1.YEAR AND ";
        if ($model->Properties["use_prg_schoolkind"] == "1" || $model->Properties["useSchool_KindField"] == "1") {
            $query .= "                 E1.SCHOOL_KIND = '".$sk."' AND ";
        } elseif ($model->Properties["use_prg_schoolkind"] == "1" || $model->Properties["useSchool_KindField"] == "1") {
            $query .= "                 T1.SCHOOL_KIND  = E1.SCHOOL_KIND AND ";
        }
        $query .= "                 E1.PROGRAMID    = '".PROGRAMID."' AND ";
        $query .= "                 T1.GROUPCD      = E1.GROUPCD ";
        $query .= "             ) ";
        return $query;
    }

    //MIN権限グループの課程学科初期値取得
    public function getMinGroupcdCourseMajor($model, $sk)
    {
        $query .= " SELECT ";
        $query .= "         COURSECD || MAJORCD AS COURSEMAJOR ";
        $query .= "     FROM ";
        $query .= "         ADMIN_CONTROL_ATTEND_DAT T1 ";
        $query .= "     WHERE ";
        $query .= "         T1.YEAR = '".CTRL_YEAR."' AND ";
        if ($model->Properties["use_prg_schoolkind"] == "1" || $model->Properties["useSchool_KindField"] == "1") {
            $query .= "     T1.SCHOOL_KIND = '".$sk."' AND ";
        }
        $query .= "         T1.PROGRAMID    = '".PROGRAMID."' AND ";
        $query .= "         T1.GROUPCD = '".$model->groupcd."' ";
        $query .= "     ORDER BY ";
        $query .= "         COURSECD || MAJORCD ";
        return $query;
    }

    //出欠項目名一覧取得
    public function getAttendNameList($model)
    {
        $query  = " SELECT ";
        $query .= "     NAMECD1, ";
        $query .= "     NAMECD2, ";
        $query .= "     NAME1 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR    = '".CTRL_YEAR."' AND ";
        $query .= "     NAMECD1 = 'A004' ";
        $query .= " UNION  ";
        $query .= " SELECT ";
        $query .= "     NAMECD1, ";
        $query .= "     NAMECD2, ";
        $query .= "     NAME1 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR    = '".CTRL_YEAR."' AND ";
        $query .= "     NAMECD1 = 'C001' ";

        return $query;
    }

    //出欠項目名一覧取得
    public function getAdminControlAttendItemnameDat($model, $sk, $cm)
    {
        //学校種別
        $school_kind = '00';
        if ($model->Properties["use_prg_schoolkind"] == "1" || $model->Properties["useSchool_KindField"] == "1") {
            $school_kind = $sk;
        }

        $query  = " SELECT ";
        $query .= "     ATTEND_ITEM, ";
        $query .= "     ATTEND_ITEMNAME ";
        $query .= " FROM ";
        $query .= "     ADMIN_CONTROL_ATTEND_ITEMNAME_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR        = '".CTRL_YEAR."' AND ";
        $query .= "     SCHOOL_KIND = '".$school_kind."' AND ";
        $query .= "     ATTEND_DIV  = '2' AND ";
        $query .= "     GRADE       = '00'  AND ";
        $query .= "     COURSECD || MAJORCD = '".$cm."' ";

        return $query;
    }

    //出欠管理者コントロール取得
    public function getAdminControlAttendDat($model, $sk, $cm)
    {
        $school_kind = '00';
        if ($model->Properties["use_prg_schoolkind"] == "1" || $model->Properties["useSchool_KindField"] == "1") {
            $school_kind = $sk;
        }

        $query  = " SELECT ";
        $query .= "     CONTROL_DIV, ";
        $query .= "     ATTEND_ITEM, ";
        $query .= "     SHOWORDER, ";
        $query .= "     INPUT_FLG ";
        $query .= " FROM ";
        $query .= "     ADMIN_CONTROL_ATTEND_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR        = '".CTRL_YEAR."' AND ";
        $query .= "     SCHOOL_KIND = '".$school_kind."' AND ";
        $query .= "     ATTEND_DIV  = '2' AND ";
        $query .= "     GRADE       = '00'  AND ";
        $query .= "     COURSECD || MAJORCD = '".$cm."' AND ";
        $query .= "     PROGRAMID   = '".PROGRAMID."' AND ";
        $query .= "     GROUPCD     = '".$model->groupcd."' ";
        $query .= " ORDER BY ";
        $query .= "     INT(SHOWORDER) ";

        return $query;
    }

    //学校種別の取得
    public function getNameMstA023($model)
    {
        $query  = " SELECT ";
        $query .= "     NAME1 AS VALUE, ";
        $query .= "     ABBV1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR    = '".CTRL_YEAR."' AND ";
        $query .= "     NAMECD1 = 'A023' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= "     AND NAME1 IN ('".implode(explode(':', $model->selectSchoolKind), "','")."') ";
            }
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //課程学科コンボ取得
    public function getCourseMajor($model, $sk)
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     T1.COURSECD || T1.MAJORCD AS VALUE, ";
        $query .= "     T1.COURSECD || T1.MAJORCD || ':' || S1.COURSENAME || S1.MAJORNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1 ";
        $query .= "     INNER JOIN V_COURSE_MAJOR_MST S1 ";
        $query .= "          ON T1.YEAR         = S1.YEAR ";
        $query .= "         AND T1.COURSECD     = S1.COURSECD ";
        $query .= "         AND T1.MAJORCD      = S1.MAJORCD ";
        if ($model->Properties["use_prg_schoolkind"] == "1" || $model->Properties["useSchool_KindField"] == "1") {
            $query .= "     INNER JOIN SCHREG_REGD_GDAT S2 ";
            $query .= "          ON T1.YEAR         = S2.YEAR ";
            $query .= "         AND T1.GRADE        = S2.GRADE ";
            $query .= "         AND S2.SCHOOL_KIND  = '".$sk."' ";
        }
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //科目取得
    public function getSubclasscd($model)
    {
        $query  = " SELECT ";
        if ($model->Properties["useCurriculumcd"] == "1") {
            $query .= "     T3.CLASSCD || '-' || T3.SCHOOL_KIND || '-' ||  T3.CURRICULUM_CD || '-' || T3.SUBCLASSCD AS VALUE, ";
            $query .= "     T3.CLASSCD || '-' || T3.SCHOOL_KIND || '-' ||  T3.CURRICULUM_CD || '-' || T3.SUBCLASSCD || VALUE(T3.SUBCLASSNAME, '') AS LABEL ";
        } else {
            $query .= "     T3.SUBCLASSCD AS VALUE, ";
            $query .= "     T3.SUBCLASSCD || VALUE(T3.SUBCLASSNAME, '') AS LABEL ";
        }
        $query .= " FROM ";
        $query .= "     CHAIR_DAT T1, ";
        $query .= "     CHAIR_STF_DAT T2, ";
        $query .= "     SUBCLASS_MST T3, ";
        $query .= "     CHAIR_STD_DAT T4, ";
        $query .= "     SCHREG_REGD_DAT T5 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR     = '".CTRL_YEAR."' AND ";
        $query .= "     T1.YEAR     = T2.YEAR AND ";
        $query .= "     T1.SEMESTER = '".CTRL_SEMESTER."' AND ";
        $query .= "     T1.SEMESTER = T2.SEMESTER AND ";
        $query .= "     T1.CHAIRCD  = T2.CHAIRCD AND ";
        $query .= "     T5.SCHREGNO = T4.SCHREGNO AND ";
        $query .= "     T5.YEAR     = T4.YEAR AND ";
        $query .= "     T5.SEMESTER = T4.SEMESTER AND ";
        //権限（制限付）
        if ($model->auth == DEF_REFER_RESTRICT || $model->auth == DEF_UPDATE_RESTRICT) {
            $query .= "     ((T2.STAFFCD = '".STAFFCD."') ";
            $query .= "         OR (T5.GRADE IN ";
            $query .= "             (SELECT ";
            $query .= "                  FIELD2 ";
            $query .= "              FROM ";
            $query .= "                  STAFF_DETAIL_MST ST ";
            $query .= "              WHERE ";
            $query .= "                  ST.YEAR = '".CTRL_YEAR."' ";
            $query .= "                  AND ST.STAFFCD = '".STAFFCD."' ";
            $query .= "                  AND ST.STAFF_SEQ IN ('005', '006', '007') ";
            $query .= "                  AND ST.FIELD1 = '0200')) ";
            $query .= "         OR (T1.CLASSCD IN ";
            $query .= "             (SELECT ";
            $query .= "                  FIELD2 ";
            $query .= "              FROM ";
            if ($model->Properties["use_staff_detail_ext_mst"] == "1") {
                $query .= "                  STAFF_DETAIL_EXT_MST ST ";
            } else {
                $query .= "                  STAFF_DETAIL_MST ST ";
            }
            $query .= "              WHERE ";
            $query .= "                  ST.YEAR = '".CTRL_YEAR."' ";
            $query .= "                  AND ST.STAFFCD = '".STAFFCD."' ";
            $query .= "                  AND ST.STAFF_SEQ IN ('005', '006', '007') ";
            $query .= "                  AND ST.FIELD1 = '1050')) ";
            $query .= "         ) AND ";
        }
        if ($model->Properties["useCurriculumcd"] == "1") {
            $query .= "     T1.CLASSCD          = T3.CLASSCD AND ";
            $query .= "     T1.SCHOOL_KIND      = T3.SCHOOL_KIND AND ";
            $query .= "     T1.CURRICULUM_CD    = T3.CURRICULUM_CD AND ";
        }
        $query .= "     T1.SUBCLASSCD = T3.SUBCLASSCD AND ";
        $query .= "     T1.YEAR     = T4.YEAR AND ";
        $query .= "     T1.SEMESTER = T4.SEMESTER AND ";
        $query .= "     T1.CHAIRCD = T4.CHAIRCD ";
        if ($model->Properties["use_school_detail_gcm_dat"] == "1") {
            $query .= " AND T5.COURSECD || T5.MAJORCD = '".$model->field["COURSE_MAJOR"]."' ";
        }
        //コールされていたら、その科目のみ使用可能
        if ($model->getPrgId) {
            if ($model->Properties["useCurriculumcd"] == "1") {
                $query .= "     AND T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' ||  T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD = '".$model->sendSubclass."' ";
            } else {
                $query .= "     AND T1.SUBCLASSCD = '".$model->sendSubclass."' ";
            }
        }
        if ($model->Properties["use_prg_schoolkind"] == "1" || $model->Properties["useSchool_KindField"] == "1") {
            $query .= " AND T5.GRADE IN (SELECT ";
            $query .= "                     GRADE ";
            $query .= "                 FROM ";
            $query .= "                     SCHREG_REGD_GDAT ";
            $query .= "                 WHERE ";
            $query .= "                     YEAR        = '".CTRL_YEAR."' AND ";
            $query .= "                     SCHOOL_KIND = '".$model->field["SCHOOL_KIND"]."' ";
            $query .= "                 ) ";
        }
        //合併先科目は対象外
        $query .= "     AND NOT EXISTS (SELECT 'X' FROM ";
        $query .= "                         SUBCLASS_REPLACE_COMBINED_DAT COMB ";
        $query .= "                     WHERE ";
        $query .= "                         COMB.REPLACECD              = '1' ";
        $query .= "                     AND COMB.YEAR                   = T1.YEAR ";
        if ($model->Properties["useCurriculumcd"] == "1") {
            $query .= "                     AND COMB.COMBINED_CLASSCD       = T1.CLASSCD ";
            $query .= "                     AND COMB.COMBINED_SCHOOL_KIND   = T1.SCHOOL_KIND ";
            $query .= "                     AND COMB.COMBINED_CURRICULUM_CD = T1.CURRICULUM_CD ";
        }
        $query .= "                     AND COMB.COMBINED_SUBCLASSCD    = T1.SUBCLASSCD) ";
        $query .= " GROUP BY ";
        if ($model->Properties["useCurriculumcd"] == "1") {
            $query .= "     T3.CLASSCD, ";
            $query .= "     T3.SCHOOL_KIND, ";
            $query .= "     T3.CURRICULUM_CD, ";
        }
        $query .= "     T3.SUBCLASSCD, ";
        $query .= "     T3.SUBCLASSNAME ";
        $query .= " ORDER BY ";
        if ($model->Properties["useCurriculumcd"] == "1") {
            $query .= "     T3.CLASSCD, ";
            $query .= "     T3.SCHOOL_KIND, ";
            $query .= "     T3.CURRICULUM_CD, ";
        }
        $query .= "     T3.SUBCLASSCD ";

        return $query;
    }

    //講座取得
    public function getChaircd(&$model)
    {
        $query  = " SELECT DISTINCT";
        $query .= "     T1.CHAIRCD AS VALUE, ";
        $query .= "     T1.CHAIRCD || VALUE(T1.CHAIRNAME, '') AS LABEL ";
        $query .= " FROM ";
        $query .= "     CHAIR_DAT T1, ";
        $query .= "     CHAIR_STF_DAT T2, ";
        $query .= "     CHAIR_STD_DAT T4, ";
        $query .= "     SCHREG_REGD_DAT T5 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR     = '".CTRL_YEAR."' AND ";
        $query .= "     T1.YEAR     = T2.YEAR AND ";
        $query .= "     T1.SEMESTER = T2.SEMESTER AND ";
        $query .= "     T1.CHAIRCD  = T2.CHAIRCD AND ";
        $query .= "     T1.YEAR     = T4.YEAR AND ";
        $query .= "     T1.SEMESTER = T4.SEMESTER AND ";
        $query .= "     T1.CHAIRCD  = T4.CHAIRCD AND ";
        $query .= "     T5.SCHREGNO = T4.SCHREGNO AND ";
        $query .= "     T5.YEAR     = T4.YEAR AND ";
        $query .= "     T5.SEMESTER = T4.SEMESTER AND ";
        //権限（制限付）
        if ($model->auth == DEF_REFER_RESTRICT || $model->auth == DEF_UPDATE_RESTRICT) {
            $query .= "     ((T2.STAFFCD = '".STAFFCD."') ";
            $query .= "         OR (T5.GRADE IN ";
            $query .= "             (SELECT ";
            $query .= "                  FIELD2 ";
            $query .= "              FROM ";
            $query .= "                  STAFF_DETAIL_MST ST ";
            $query .= "              WHERE ";
            $query .= "                  ST.YEAR = '".CTRL_YEAR."' ";
            $query .= "                  AND ST.STAFFCD = '".STAFFCD."' ";
            $query .= "                  AND ST.STAFF_SEQ IN ('005', '006', '007') ";
            $query .= "                  AND ST.FIELD1 = '0200')) ";
            $query .= "         OR (T1.CLASSCD IN ";
            $query .= "             (SELECT ";
            $query .= "                  FIELD2 ";
            $query .= "              FROM ";
            if ($model->Properties["use_staff_detail_ext_mst"] == "1") {
                $query .= "                  STAFF_DETAIL_EXT_MST ST ";
            } else {
                $query .= "                  STAFF_DETAIL_MST ST ";
            }
            $query .= "              WHERE ";
            $query .= "                  ST.YEAR = '".CTRL_YEAR."' ";
            $query .= "                  AND ST.STAFFCD = '".STAFFCD."' ";
            $query .= "                  AND ST.STAFF_SEQ IN ('005', '006', '007') ";
            $query .= "                  AND ST.FIELD1 = '1050')) ";
            $query .= "         ) AND ";
        }
        if ($model->Properties["useCurriculumcd"] == "1") {
            $query .= "     T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' ||  T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD = '".$model->field["SUBCLASSCD"]."' ";
        } else {
            $query .= "     T1.SUBCLASSCD = '".$model->field["SUBCLASSCD"]."' ";
        }
        if ($model->Properties["use_school_detail_gcm_dat"] == "1") {
            $query .= " AND T5.COURSECD || T5.MAJORCD = '".$model->field["COURSE_MAJOR"]."' ";
        }
        //コールされていたら、その講座のみ使用可能
        if ($model->getPrgId) {
            $query .= "     AND T1.CHAIRCD = '{$model->sendChair}' ";
        }
        if ($model->Properties["use_prg_schoolkind"] == "1" || $model->Properties["useSchool_KindField"] == "1") {
            $query .= " AND T5.GRADE IN (SELECT ";
            $query .= "                     GRADE ";
            $query .= "                 FROM ";
            $query .= "                     SCHREG_REGD_GDAT ";
            $query .= "                 WHERE ";
            $query .= "                     YEAR        = '".CTRL_YEAR."' AND ";
            $query .= "                     SCHOOL_KIND = '".$model->field["SCHOOL_KIND"]."' ";
            $query .= "                 ) ";
        }
        $query .= " ORDER BY ";
        $query .= "     T1.CHAIRCD ";

        return $query;
    }

    //メインデータ作成
    public function selectSemesAll($seme = "")
    {
        $query  = " SELECT ";
        $query .= "     SEMESTER, ";
        $query .= "     SEMESTERNAME, ";
        if (!$seme) {
            $query .= "     CASE WHEN MONTH(SDATE) < 4 ";
            $query .= "          THEN MONTH(SDATE) + 12 ";
            $query .= "          ELSE MONTH(SDATE) END AS S_MONTH, ";
            $query .= "     CASE WHEN MONTH(EDATE) < 4 ";
            $query .= "          THEN MONTH(EDATE) + 12 ";
            $query .= "          ELSE MONTH(EDATE) END AS E_MONTH ";
        } else {
            $query .= "     SEMESTERNAME, ";
            $query .= "     MONTH(SDATE) AS S_MONTH, ";
            $query .= "     DAY(SDATE) AS S_DAY, ";
            $query .= "     MONTH(EDATE) AS E_MONTH, ";
            $query .= "     DAY(EDATE) AS E_DAY ";
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
    public function selectMonthQuery($month, $model)
    {
        $setNameCd = "Z005";
        if ($model->Properties["use_prg_schoolkind"] == "1" || $model->Properties["useSchool_KindField"] == "1") {
            $setNameCd = "Z".$model->field["SCHOOL_KIND"]."05";
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
        if ($model->Properties["use_prg_schoolkind"] == "1" || $model->Properties["useSchool_KindField"] == "1") {
            $query .= "                     SCHOOL_KIND = '".$model->field["SCHOOL_KIND"]."' AND ";
        }
        $query .= "                     CONTROL_FLG = '2') AND ";
        $query .= "     NAMECD2 = '".sprintf('%02d', $month)."' ";
        $query .= " ORDER BY ";
        $query .= "     NAMESPARE1 ";

        return $query;
    }

    //MAX(LESSON)取得
    public function getInputMaxLesson($model)
    {
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
        $query .= "         T1.CHAIRCD  = '".$model->field["CHAIRCD"]."' ";
        if ($model->Properties["use_school_detail_gcm_dat"] == "1") {
            $query .= "     AND T2.COURSECD || T2.MAJORCD = '".$model->field["COURSE_MAJOR"]."' ";
        }
        if ($model->field["MONTH"]) {
            $query .= "         AND MONTH(T1.APPDATE) <= ".intval($model->field["MONTH"])." + CASE WHEN ".intval($model->field["MONTH"])." < 4 THEN 12 ELSE 0 END AND ";
            $query .= "         ".intval($model->field["MONTH"])." <= MONTH(T1.APPENDDATE) + CASE WHEN MONTH(T1.APPENDDATE) < 4 THEN 12 ELSE 0 END ";
            $query .= "         AND CASE WHEN ".intval($model->field["MONTH"])." < 4 THEN ".intval($model->field["MONTH"])."+12 ELSE ".intval($model->field["MONTH"])." END ";
            $query .= "         BETWEEN ";
            $query .= "             CASE WHEN MONTH(T1.APPDATE) < 4 THEN MONTH(T1.APPDATE)+12 ELSE MONTH(T1.APPDATE) END ";
            $query .= "         AND ";
            $query .= "             CASE WHEN MONTH(T1.APPENDDATE) < 4 THEN MONTH(T1.APPENDDATE)+12 ELSE MONTH(T1.APPENDDATE) END ";
        }
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

    //学校マスタ取得
    public function getSchoolMst($model)
    {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     SCHOOL_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        if ($model->Properties["use_prg_schoolkind"] == "1" || $model->Properties["useSchool_KindField"] == "1") {
            $query .= " AND SCHOOL_KIND = '".$model->field["SCHOOL_KIND"]."' ";
        }

        return $query;
    }

    public function getAttendLessonInfo($model)
    {
        $query   = "  WITH SDETAIL AS ( ";
        $query  .= "    SELECT ";
        $query  .= "      'X' AS KEY, ";
        $query  .= "      SCHOOL_REMARK2, ";
        $query  .= "      SCHOOL_REMARK3, ";
        $query  .= "      SCHOOL_REMARK7 ";
        $query  .= "    FROM ";
        $query  .= "      SCHOOL_DETAIL_DAT ";
        $query  .= "    WHERE ";
        $query  .= "      YEAR = '".CTRL_YEAR."' ";
        $query  .= "      AND SCHOOLCD = '".SCHOOLCD."' ";
        $query  .= "      AND SCHOOL_KIND = '".$model->field["SCHOOL_KIND"]."' ";
        $query  .= "      AND SCHOOL_SEQ = '001' ";
        $query  .= "  ), CREDITS AS ( ";
        $query  .= "    SELECT ";
        $query  .= "      GRADE, ";
        $query  .= "      COURSECD, ";
        $query  .= "      MAJORCD, ";
        $query  .= "      COURSECODE, ";
        $query  .= "      SUM(CREDITS) AS CREDITS, ";
        $query  .= "      'X' AS KEY ";
        $query  .= "    FROM ";
        $query  .= "      CREDIT_MST ";
        $query  .= "    WHERE ";
        $query  .= "      YEAR = '".CTRL_YEAR."' ";
        if ($model->Properties["useCurriculumcd"] == "1") {
            $query  .= "      AND CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD = '".$model->field["SUBCLASSCD"]."' ";
        } else {
            $query  .= "      AND SUBCLASSCD = '".$model->field["SUBCLASSCD"]."' ";
        }
        $query  .= "    GROUP BY  ";
        $query  .= "      GRADE, ";
        $query  .= "      COURSECD, ";
        $query  .= "      MAJORCD, ";
        $query  .= "      COURSECODE ";
        $query  .= "  ) ";
        $query  .= "  SELECT ";
        $query  .= "      GRADE, ";
        $query  .= "      COURSECD, ";
        $query  .= "      MAJORCD, ";
        $query  .= "      COURSECODE, ";
        $query  .= "    ((CREDITS * INT(SCHOOL_REMARK7)) * INT (SCHOOL_REMARK2)) / INT (value(SCHOOL_REMARK3, 1)) AS RISYUU_JYOUGEN ";
        $query  .= "  FROM ";
        $query  .= "    CREDITS T1 ";
        $query  .= "    LEFT JOIN SDETAIL T2 ";
        $query  .= "      ON T2.KEY = T1.KEY ";

        return $query;
    }

    //授業時数区分取得
    public function getJugyouJisuFlg($model, $cm = "")
    {
        $query  = " SELECT ";
        $query .= "     JUGYOU_JISU_FLG ";
        $query .= " FROM ";
        if ($cm) {
            $query .= "     V_SCHOOL_GCM_MST ";
        } else {
            $query .= "     V_SCHOOL_MST ";
        }
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        if ($model->Properties["use_prg_schoolkind"] == "1" || $model->Properties["useSchool_KindField"] == "1") {
            $query .= " AND SCHOOL_KIND = '".$model->field["SCHOOL_KIND"]."' ";
        } elseif ($cm) {
            $query .= " AND SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        if ($cm) {
            $query .= " AND COURSECD || MAJORCD = '".$cm."' ";
        }

        return $query;
    }

    //学期の最終月判定
    public function getMaxSemeMonthCnt($model)
    {
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
    public function getSyusu($model, $seme, $cm = "")
    {
        $query  = " SELECT ";
        $query .= "     HOUTEI_SYUSU_SEMESTER".$seme." ";
        $query .= " FROM ";
        if ($cm) {
            $query .= "     V_SCHOOL_GCM_MST ";
        } else {
            $query .= "     V_SCHOOL_MST ";
        }
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' AND ";
        $query .= "     JUGYOU_JISU_FLG = '1' ";
        if ($model->Properties["use_prg_schoolkind"] == "1" || $model->Properties["useSchool_KindField"] == "1") {
            $query .= " AND SCHOOL_KIND = '".$model->field["SCHOOL_KIND"]."' ";
        } elseif ($cm) {
            $query .= " AND SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        if ($cm) {
            $query .= " AND COURSECD || MAJORCD = '".$cm."' ";
        }

        return $query;
    }

    //月毎の週数取得
    public function getMonthSyusu($model, $cm = "")
    {
        $query  = " SELECT ";
        $query .= "     SYUSU ";
        $query .= " FROM ";
        $query .= "     ATTEND_SYUSU_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR        = '".CTRL_YEAR."' AND ";
        $query .= "     MONTH       = '".$model->field["MONTH"]."' AND ";
        $query .= "     SEMESTER    = '".$model->field["SEMESTER"]."' AND ";
        $query .= "     GRADE       = '00' AND ";
        if ($cm) {
            $query .= "     COURSECD || MAJORCD = '".$cm."' ";
        } else {
            $query .= "     COURSECD    = '0' AND ";
            $query .= "     MAJORCD     = '000' ";
        }

        return $query;
    }

    //単位数取得
    public function getCredit($model)
    {
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
        $query .= "         T1.CHAIRCD  = '".$model->field["CHAIRCD"]."' ";
        if ($model->Properties["use_school_detail_gcm_dat"] == "1") {
            $query .= "     AND T2.COURSECD || T2.MAJORCD = '".$model->field["COURSE_MAJOR"]."' ";
        }
        if ($model->field["MONTH"]) {
            $query .= "         AND MONTH(T1.APPDATE) <= ".intval($model->field["MONTH"])." + CASE WHEN ".intval($model->field["MONTH"])." < 4 THEN 12 ELSE 0 END AND ";
            $query .= "         ".intval($model->field["MONTH"])." <= MONTH(T1.APPENDDATE) + CASE WHEN MONTH(T1.APPENDDATE) < 4 THEN 12 ELSE 0 END ";
            $query .= "         AND CASE WHEN ".intval($model->field["MONTH"])." < 4 THEN ".intval($model->field["MONTH"])."+12 ELSE ".intval($model->field["MONTH"])." END ";
            $query .= "         BETWEEN ";
            $query .= "             CASE WHEN MONTH(T1.APPDATE) < 4 THEN MONTH(T1.APPDATE)+12 ELSE MONTH(T1.APPDATE) END ";
            $query .= "         AND ";
            $query .= "             CASE WHEN MONTH(T1.APPENDDATE) < 4 THEN MONTH(T1.APPENDDATE)+12 ELSE MONTH(T1.APPENDDATE) END ";
        }
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
    public function getMaxSumLesson($model)
    {
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
        $query .= "         T1.CHAIRCD  = '".$model->field["CHAIRCD"]."' ";
        if ($model->Properties["use_school_detail_gcm_dat"] == "1") {
            $query .= "     AND T2.COURSECD || T2.MAJORCD = '".$model->field["COURSE_MAJOR"]."' ";
        }
        if ($model->field["MONTH"]) {
            $query .= "         AND MONTH(T1.APPDATE) <= ".intval($model->field["MONTH"])." + CASE WHEN ".intval($model->field["MONTH"])." < 4 THEN 12 ELSE 0 END AND ";
            $query .= "         ".intval($model->field["MONTH"])." <= MONTH(T1.APPENDDATE) + CASE WHEN MONTH(T1.APPENDDATE) < 4 THEN 12 ELSE 0 END ";
            $query .= "         AND CASE WHEN ".intval($model->field["MONTH"])." < 4 THEN ".intval($model->field["MONTH"])."+12 ELSE ".intval($model->field["MONTH"])." END ";
            $query .= "         BETWEEN ";
            $query .= "             CASE WHEN MONTH(T1.APPDATE) < 4 THEN MONTH(T1.APPDATE)+12 ELSE MONTH(T1.APPDATE) END ";
            $query .= "         AND ";
            $query .= "             CASE WHEN MONTH(T1.APPENDDATE) < 4 THEN MONTH(T1.APPENDDATE)+12 ELSE MONTH(T1.APPENDDATE) END ";
        }
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
    public function getAppointedDay($model)
    {
        $query  = " SELECT ";
        $query .= "     APPOINTED_DAY ";
        $query .= " FROM ";
        $query .= "     APPOINTED_DAY_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR        = '". CTRL_YEAR ."' AND ";
        if ($model->Properties["useSchool_KindField"] == "1") {
            $query .= "     SCHOOL_KIND = '".$model->field["SCHOOL_KIND"]."' AND ";
        }
        $query .= "     MONTH       = '".$model->field["MONTH"]."' AND ";
        $query .= "     SEMESTER    = '".$model->field["SEMESTER"]."' ";

        return $query;
    }

    //出欠完了フラグ取得
    public function getExecuted($model)
    {
        $query  = " SELECT ";
        $query .= "     EXECUTED ";
        $query .= " FROM ";
        $query .= "     ATTEND_CHKFIN_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR        = '".CTRL_YEAR."' AND ";
        $query .= "     MONTH       = '".$model->field["MONTH"]."' AND ";
        $query .= "     SEMESTER    = '".$model->field["SEMESTER"]."' AND ";
        $query .= "     ATTEND_DIV  = '2' AND ";
        $query .= "     GRADE       = '00' AND ";
        $query .= "     HR_CLASS    = '000' AND ";
        $query .= "     CHAIRCD     = '".$model->field["CHAIRCD"]."' ";

        return $query;
    }

    //明細
    public function selectMeisaiQuery($model)
    {

        //累積期間月を配列にする。
        $range_month = array("04" => "'04'",
                             "05" => "'04','05'",
                             "06" => "'04','05','06'",
                             "07" => "'04','05','06','07'",
                             "08" => "'04','05','06','07','08'",
                             "09" => "'04','05','06','07','08','09'",
                             "10" => "'04','05','06','07','08','09','10'",
                             "11" => "'04','05','06','07','08','09','10','11'",
                             "12" => "'04','05','06','07','08','09','10','11','12'",
                             "01" => "'04','05','06','07','08','09','10','11','12','01'",
                             "02" => "'04','05','06','07','08','09','10','11','12','01','02'",
                             "03" => "'04','05','06','07','08','09','10','11','12','01','02','03'"
                             );

        $query  = " WITH SCH_INFO AS ( ";
        $query .= "     SELECT DISTINCT ";
        $query .= "         T1.SCHREGNO, ";
        $query .= "         T1.GRADE, ";
        $query .= "         T1.HR_CLASS, ";
        $query .= "         T1.ATTENDNO, ";
        $query .= "         T1.COURSECD, ";
        $query .= "         T1.MAJORCD, ";
        $query .= "         T1.COURSECODE, ";
        $query .= "         T3.HR_NAMEABBV || '-' || T1.ATTENDNO AS HR_ATTENDNO, ";
        $query .= "         T2.NAME_SHOW AS NAME ";
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
        $query .= "         T4.CHAIRCD  = '".$model->field["CHAIRCD"]."' ";
        if ($model->Properties["use_school_detail_gcm_dat"] == "1") {
            $query .= "     AND T1.COURSECD || T1.MAJORCD  = '".$model->field["COURSE_MAJOR"]."' ";
        }
        if ($model->field["MONTH"]) {
            $query .= "         AND MONTH(T4.APPDATE) <= ".intval($model->field["MONTH"])." + CASE WHEN ".intval($model->field["MONTH"])." < 4 THEN 12 ELSE 0 END AND ";
            $query .= "         ".intval($model->field["MONTH"])." <= MONTH(T4.APPENDDATE) + CASE WHEN MONTH(T4.APPENDDATE) < 4 THEN 12 ELSE 0 END ";
            $query .= "         AND CASE WHEN ".intval($model->field["MONTH"])." < 4 THEN ".intval($model->field["MONTH"])."+12 ELSE ".intval($model->field["MONTH"])." END ";
            $query .= "         BETWEEN ";
            $query .= "             CASE WHEN MONTH(T4.APPDATE) < 4 THEN MONTH(T4.APPDATE)+12 ELSE MONTH(T4.APPDATE) END ";
            $query .= "         AND ";
            $query .= "             CASE WHEN MONTH(T4.APPENDDATE) < 4 THEN MONTH(T4.APPENDDATE)+12 ELSE MONTH(T4.APPENDDATE) END ";
        }
        if ($model->Properties["useSchool_KindField"] == "1" || $model->Properties["useSchool_KindField"] == "1") {
            $query .= " AND T1.GRADE IN (SELECT ";
            $query .= "                     GRADE ";
            $query .= "                 FROM ";
            $query .= "                     SCHREG_REGD_GDAT ";
            $query .= "                 WHERE ";
            $query .= "                     YEAR        = '".CTRL_YEAR."' AND ";
            $query .= "                     SCHOOL_KIND = '".$model->field["SCHOOL_KIND"]."' ";
            $query .= "                 ) ";
        }
        $query  .= " ), SUM_ATTEND_SUBCLASS AS (  ";
        $query  .= "  SELECT ";
        $query  .= "    SCHREGNO, ";
        $query  .= "    SUM(NONOTICE) AS NONOTICE, ";
        $query  .= "    SUM(NOTICE) AS NOTICE, ";
        $query  .= "    SUM(SICK) AS SICK, ";
        $query  .= "    VALUE (SUM(SICK), 0) + VALUE (SUM(NOTICE), 0) + VALUE (SUM(NONOTICE), 0) ";
        if ($model->schoolMst["SUB_OFFDAYS"] == "1") {
            $query  .= "    + VALUE (SUM(OFFDAYS), 0) ";
        }
        if ($model->schoolMst["SUB_ABSENT"] == "1") {
            $query  .= "    + VALUE (SUM(ABSENT), 0) ";
        }
        if ($model->schoolMst["SUB_SUSPEND"] == "1") {
            $query  .= "    + VALUE (SUM(SUSPEND), 0) ";
        }
        if ($model->schoolMst["SUB_MOURNING"] == "1") {
            $query  .= "    + VALUE (SUM(MOURNING), 0) ";
        }
        if ($model->schoolMst["SUB_VIRUS"] == "1") {
            $query  .= "    + VALUE (SUM(VIRUS), 0) ";
        }
        $query  .= "    AS SUM_KETUJISU ";
        $query  .= "  FROM ";
        $query  .= "    ATTEND_SUBCLASS_DAT ";
        $query  .= "  WHERE ";
        $query  .= "    YEAR = '".CTRL_YEAR."' ";
        if ($model->field["MONTH"] != "" && $model->field["MONTH"] != null) {
            $query .= "     AND MONTH IN( ".$range_month[$model->field["MONTH"]].")";
            $query .= "     AND SEMESTER <= '".$model->field["SEMESTER"]."' ";
        }
        $query  .= "    AND CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD = '".$model->field["SUBCLASSCD"]."' ";
        $query  .= "  GROUP BY ";
        $query  .= "    SCHREGNO ";
        $query  .= " ) ";

        $query .= " , SUM_ATTEND_SEMES AS ( ";
        $query .= "     SELECT ";
        foreach ($model->itemR_array as $key => $val) {
            $query .= "    SUM({$val["item"]}) AS RUISEKI_{$val["item"]}, ";
        }
        $query .= "         SCHREGNO ";
        $query .= "     FROM ";
        $query .= "         ATTEND_SUBCLASS_DAT ";
        $query .= "     WHERE ";
        $query .= "         YEAR = '".CTRL_YEAR."' AND ";
        $query .= "     MONTH IN( ".$range_month[$model->field["MONTH"]].") AND ";
        $query .= "     SEMESTER <= '".$model->field["SEMESTER"]."' AND ";
        if ($model->Properties["useCurriculumcd"] == "1") {
            $query .= "                                         CLASSCD || '-' || SCHOOL_KIND || '-' ||  CURRICULUM_CD || '-' || SUBCLASSCD   = '".$model->field["SUBCLASSCD"]."' ";
        } else {
            $query .= "                                         CLASSCD      = '".substr($model->field["SUBCLASSCD"], 0, 2)."' AND ";
            $query .= "                                         SUBCLASSCD   = '".$model->field["SUBCLASSCD"]."' ";
        }
        $query .= "     GROUP BY ";
        $query .= "         SCHREGNO ";
        $query .= " ) ";

        $query .= " SELECT ";
        $query .= "     T1.*, ";
        $query .= "     T2.APPOINTED_DAY, ";
        foreach ($model->item_array as $key => $val) {
            $query .= "     T2.{$val["item"]}, ";
        }
        foreach ($model->itemR_array as $key => $val) {
            $query .= "     SUMAD.RUISEKI_{$val["item"]}, ";
        }
        $query .= "     T2.SCHREGNO AS SUBCL_SCHREGNO, ";
        $query .= "     T3.SUM_KETUJISU ";
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
        $query .= "     LEFT JOIN SUM_ATTEND_SUBCLASS T3 ON T1.SCHREGNO     = T3.SCHREGNO ";
        $query .= "     LEFT JOIN SUM_ATTEND_SEMES SUMAD ";
        $query .= "          ON SUMAD.SCHREGNO  = T1.SCHREGNO ";
        $query .= " ORDER BY ";
        $query .= "         T1.GRADE, ";
        $query .= "         T1.HR_CLASS, ";
        $query .= "         T1.ATTENDNO, ";
        $query .= "         T1.SCHREGNO ";

        return $query;
    }

    //異動データ（退学・転学・卒業）取得
    public function getIdouData($schregno, $date1, $date2)
    {
        $query  = " SELECT ";
        $query .= "     CASE WHEN GRD_DATE < '".$date1."' THEN 1 ELSE 0 END AS IDOU_COLOR, ";
        $query .= "     CASE WHEN GRD_DATE < '".$date2."' THEN 1 ELSE 0 END AS IDOU_TEXT ";
        $query .= " FROM ";
        $query .= "     SCHREG_BASE_MST ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO = '".$schregno."' AND ";
        $query .= "     GRD_DIV IN ('1', '2', '3', '6', '7') ";

        return $query;
    }

    //異動データ（留学・休学）取得
    public function getTransferData($schregno, $date1, $date2)
    {
        $query  = " SELECT ";
        $query .= "     SUM(CASE WHEN '".$date1."' BETWEEN TRANSFER_SDATE AND CASE WHEN TRANSFER_EDATE IS NULL THEN '".(CTRL_YEAR+1)."' || '-03-31' ELSE TRANSFER_EDATE END THEN 1 ELSE 0 END) AS IDOU_COLOR, ";
        $query .= "     SUM(CASE WHEN TRANSFER_SDATE <= '".$date2."' AND '".$date1."' <= CASE WHEN TRANSFER_EDATE IS NULL THEN '".(CTRL_YEAR+1)."' || '-03-31' ELSE TRANSFER_EDATE END THEN 1 ELSE 0 END) AS IDOU_TEXT ";
        $query .= " FROM ";
        $query .= "     SCHREG_TRANSFER_DAT ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO = '".$schregno."' AND ";
        $query .= "     TRANSFERCD IN ('1', '2') ";

        return $query;
    }

    //フィールド一覧取得
    public function getFieldList($table)
    {
        $query  = " WITH MAIN AS ( ";
        $query .= "     SELECT ";
        $query .= "         COLNO, ";
        $query .= "         NAME ";
        $query .= "     FROM ";
        $query .= "         SYSIBM.SYSCOLUMNS ";
        $query .= "     WHERE ";
        $query .= "         TBNAME  = '".$table."' AND ";
        $query .= "         NULLS   = 'Y' AND ";
        $query .= "         NAME NOT IN ('APPOINTED_DAY', 'REGISTERCD', 'UPDATED') ";
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

    //データ取得 -- ATTEND_SUBCLASS_DAT / ATTEND_SEMES_DAT
    public function getAttendData($model, $table)
    {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .=       $table;
        $query .= " WHERE ";
        $query .= "     COPYCD      = '0' AND ";
        $query .= "     YEAR        = '".CTRL_YEAR."' AND ";
        $query .= "     SEMESTER    = '".$model->field["SEMESTER"]."' AND ";
        $query .= "     MONTH       = '".$model->field["MONTH"]."' AND ";
        $query .= "     SCHREGNO IN ('".implode("','", $model->field["SCHREGNO"])."') ";
        if ($table == "ATTEND_SUBCLASS_DAT") {
            if ($model->Properties["useCurriculumcd"] == "1") {
                $query .= " AND CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD = '".$model->field["SUBCLASSCD"]."' ";
            } else {
                $query .= " AND CLASSCD     = '".substr($model->field["SUBCLASSCD"], 0, 2)."' ";
                $query .= " AND SUBCLASSCD  = '".$model->field["SUBCLASSCD"]."' ";
            }
        }
        $query .= " ORDER BY ";
        $query .= "     SCHREGNO ";

        return $query;
    }

    //出欠月別累積データの更新
    public function getUpdateQuery($model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $tableList = array("ATTEND_SUBCLASS_DAT"    => "subclass",
                           "ATTEND_SEMES_DAT"       => "semes"
                          );

        $subclass_field = $semes_field = array();
        $subclassData = $semesData = array();
        foreach ($tableList as $table => $var) {
            //フィールド一覧取得
            $fieldArray = $db->getCol(knjc035fQuery::getFieldList($table));
            $varname1   = $var."_field";
            $$varname1  = $fieldArray;

            //データ取得
            $getData = array();
            $query = knjc035fQuery::getAttendData($model, $table);
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $getData[$row["SCHREGNO"]] = $row;
            }
            $varname2   = $var."Data";
            $$varname2  = $getData;
        }

        //更新対象行
        $useLine = explode(',', $model->field["useLine"]);

        $line = 0;
        foreach ($model->field["SCHREGNO"] as $key => $schregno) {
            $data = array();
            if (strlen($subclassData[$schregno]["SCHREGNO"])) {
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
            $data["APPOINTED_DAY"][TEXT] = $model->field["APPOINTED_DAY"];      //締め日
            $upItemArray = array();
            foreach ($model->item_array as $order => $val) {
                //入力可の項目が更新対象
                if (in_array($val["item"], $subclass_field) && $val["input"] && in_array($key, $useLine)) {
                    if ($model->Properties["use_Attend_zero_hyoji"] == "1") {
                        $data[$val["item"]][NUMBER] = $model->field[$val["item"]][$line];
                    } else {
                        $data[$val["item"]][NUMBER] = ($model->field[$val["item"]][$line]) ? $model->field[$val["item"]][$line] : 0;
                    }
                    $upItemArray[] = $val["item"];
                }
            }
            if ($model->Properties["use_Attend_zero_hyoji"] != "1") {
                foreach ($subclass_field as $field) {
                    //入力可以外の項目（値なし）を更新
                    if (!(in_array($field, $upItemArray) || $subclassData[$schregno][$field] > 0)) {
                        $data[$field][NUMBER] = 0;
                    }
                }
            }
            $data["REGISTERCD"][TEXT]    = STAFFCD ;                            //登録者コード
            $data["UPDATED"][FUNC]       = "sysdate()";                         //更新日付

            if (in_array($key, $useLine)) {
                $line++;
            }

            //ATTEND_SEMES_DATセット
            $data2 = array();
            $data2["COPYCD"][TEXT]          = "0";
            $data2["YEAR"][TEXT]            = CTRL_YEAR;
            $data2["MONTH"][TEXT]           = $model->field["MONTH"];
            $data2["SEMESTER"][TEXT]        = $model->field["SEMESTER"];
            $data2["SCHREGNO"][TEXT]        = $schregno;
            $data2["APPOINTED_DAY"][TEXT]   = $model->field["APPOINTED_DAY"];
            foreach ($semes_field as $semfld) {
                if ($model->Properties["use_Attend_zero_hyoji"] != "1") {
                    $data2[$semfld][NUMBER]     = 0;
                }
            }
            $data2["REGISTERCD"][TEXT]      = STAFFCD;
            $data2["UPDATED"][FUNC]         = "SYSDATE()";

            if ("" != $model->field["APPOINTED_DAY"]) {
                //更新対象データがある時 - UPDATE
                if (strlen($subclassData[$schregno]["SCHREGNO"])) {
                    $query = Query::updateSQL($data, "ATTEND_SUBCLASS_DAT", $where);
                } else {//更新対象データがない時 - INSERT
                    $query = Query::insertSQL($data, "ATTEND_SUBCLASS_DAT");
                }

                //ATTEND_SEMES_DAT追加
                if (!strlen($semesData[$schregno]["SCHREGNO"])) {
                    $query2 = Query::insertSQL($data2, "ATTEND_SEMES_DAT");
                    $db->query($query2);
                }
            } else {
                if (strlen($subclassData[$schregno]["SCHREGNO"])) {
                    $query = "DELETE FROM ATTEND_SUBCLASS_DAT ".$where;
                }
            }
            $db->query($query);
        }

        /***********************/
        /*  ATTEND_CHKFIN_DAT  */
        /***********************/
        //削除
        $query  = " DELETE FROM ";
        $query .= "     ATTEND_CHKFIN_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR        = '".CTRL_YEAR."' AND ";
        $query .= "     MONTH       = '".$model->field["MONTH"]."' AND ";
        $query .= "     SEMESTER    = '".$model->field["SEMESTER"]."' AND ";
        $query .= "     ATTEND_DIV  = '2' AND ";
        $query .= "     CHAIRCD     = '".$model->field["CHAIRCD"]."' ";

        $db->query($query);

        //追加
        $data = array();
        $data["YEAR"][TEXT]         = CTRL_YEAR;
        $data["MONTH"][TEXT]        = $model->field["MONTH"];
        $data["SEMESTER"][TEXT]     = $model->field["SEMESTER"];
        $data["ATTEND_DIV"][TEXT]   = "2";
        $data["GRADE"][TEXT]        = "00";
        $data["HR_CLASS"][TEXT]     = "000";
        $data["CHAIRCD"][TEXT]      = $model->field["CHAIRCD"];
        $data["EXECUTED"][TEXT]     = $model->field["EXECUTED"];
        $data["REGISTERCD"][TEXT]   = STAFFCD;
        $data["UPDATED"][FUNC]      = "SYSDATE()";

        $query = Query::insertSQL($data, "ATTEND_CHKFIN_DAT");
        $db->query($query);

        $db->commit();
        Query::dbCheckIn($db);
        return true;
    }
}
