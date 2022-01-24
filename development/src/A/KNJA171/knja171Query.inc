<?php

require_once('for_php7.php');

class knja171Query extends Query
{

    //学期取得
    public function getSemester()
    {
        $query  = "SELECT ";
        $query .= "    SEMESTER AS VALUE, ";
        $query .= "    SEMESTERNAME AS LABEL ";
        $query .= "FROM ";
        $query .= "    SEMESTER_MST ";
        $query .= "WHERE ";
        $query .= "    YEAR = '".CTRL_YEAR."' ";
        $query .= "    AND SEMESTER < '9' ";
        $query .= "ORDER BY ";
        $query .= "    VALUE ";

        return $query;
    }

    //クラス取得
    public function getAuthClass($model)
    {
        $query = "";
        if ($model->isGakunenKongou) { //クラス選択/法定クラス
            $query  = " WITH COMBOBASE_T AS ( ";
            $query .= " SELECT DISTINCT ";
            $query .= "     N1.NAMECD2 || T1.SCHOOL_KIND || '-' || T1.HR_CLASS AS VALUE, ";
            $query .= "     N1.ABBV1 || '-' || T1.HR_CLASS || ':' || T1.HR_CLASS_NAME1 AS LABEL ";
            $query .= " FROM V_STAFF_HR_DAT T1 ";
            $query .= "      LEFT JOIN V_NAME_MST N1 ON N1.YEAR = T1.YEAR AND N1.NAMECD1 = 'A023' AND N1.NAME1 = T1.SCHOOL_KIND ";
            $query .= " WHERE ";
            $query .= "     T1.YEAR = '".CTRL_YEAR."' ";
            $query .= "     AND T1.SEMESTER = '".$model->field["OUTPUT"] ."' ";
            if ($model->Properties["use_prg_schoolkind"] == "1") {
                if ($model->selectSchoolKind) {
                    $query .= "     AND T1.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind), "','")."') ";
                }
            } elseif ($model->Properties["useSchool_KindField"] == "1") {
                $query .= "     AND T1.SCHOOL_KIND = '".SCHOOLKIND."' ";
            }
            //参照・更新可（制限付き）
            if (AUTHORITY == DEF_REFER_RESTRICT || AUTHORITY == DEF_UPDATE_RESTRICT) {
                $query .= "     AND T1.STAFFCD = '".STAFFCD."' ";
            }
            $query .= "     AND T1.HR_CLASS_NAME1 IN (  SELECT ";
            $query .= "                                    MAX(M1.HR_CLASS_NAME1) AS MAX_NAME  ";
            $query .= "                                 FROM ";
            $query .= "                                     V_STAFF_HR_DAT M1 ";
            $query .= "                                 WHERE ";
            $query .= "                                     T1.YEAR     = M1.YEAR AND ";
            $query .= "                                     T1.SEMESTER = M1.SEMESTER AND ";
            $query .= "                                     T1.HR_CLASS = M1.HR_CLASS AND ";
            $query .= "                                     T1.SCHOOL_KIND = M1.SCHOOL_KIND AND ";
            $query .= "                                     T1.STAFFCD  = M1.STAFFCD ";
            $query .= "                                 ) ";
            $query .= " ), NUMBERING_T AS ( ";
            $query .= " SELECT ";
            $query .= "     ROW_NUMBER() OVER(PARTITION BY VALUE ORDER BY VALUE) AS PRIORI, ";
            $query .= "     VALUE,LABEL ";
            $query .= " FROM ";
            $query .= "   COMBOBASE_T ";
            $query .= " ) ";
            $query .= " SELECT * FROM NUMBERING_T WHERE PRIORI = 1 ORDER BY VALUE, LABEL ";
        } elseif ($model->field["CHOICE"] == "3") {  //実クラス
            $query  = " SELECT DISTINCT ";
            if ($model->isGhr) {
                $query .= "     GHR_CD AS VALUE, ";
                $query .= "     GHR_NAME AS LABEL ";
            } else {
                $query .= "     GRADE || HR_CLASS AS VALUE, ";
                $query .= "     HR_NAME AS LABEL ";
            }
            $query .= " FROM ";
            if ($model->Properties["useFi_Hrclass"] == '1') {
                $query .= "     SCHREG_REGD_FI_HDAT ";
            } elseif ($model->Properties["useSpecial_Support_Hrclass"] == '1') {
                $query .= "     SCHREG_REGD_GHR_HDAT ";
            }
            $query .= " WHERE ";
            $query .= "     YEAR         = '".CTRL_YEAR."' ";
            $query .= "     AND SEMESTER = '".$model->field["OUTPUT"] ."' ";
            if ($model->Properties["useFi_Hrclass"] == '1' && $model->Properties["use_prg_schoolkind"] == "1") {
                if ($model->selectSchoolKind) {
                    $query .= "     AND GRADE IN (SELECT ";
                    $query .= "                       REGD_G.GRADE ";
                    $query .= "                   FROM ";
                    $query .= "                       SCHREG_REGD_GDAT REGD_G ";
                    $query .= "                   WHERE ";
                    $query .= "                       REGD_G.YEAR = '".CTRL_YEAR."' ";
                    $query .= "                       AND REGD_G.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind), "','")."')) ";
                }
            } elseif ($model->Properties["useFi_Hrclass"] == '1' && $model->Properties["useSchool_KindField"] == "1") {
                $query .= "     AND GRADE IN (SELECT ";
                $query .= "                       REGD_G.GRADE ";
                $query .= "                   FROM ";
                $query .= "                       SCHREG_REGD_GDAT REGD_G ";
                $query .= "                   WHERE ";
                $query .= "                       REGD_G.YEAR = '".CTRL_YEAR."' ";
                $query .= "                       AND REGD_G.SCHOOL_KIND = '" .SCHOOLKIND ."') ";
            }
            //参照・更新可（制限付き）
            if (AUTHORITY == DEF_REFER_RESTRICT || AUTHORITY == DEF_UPDATE_RESTRICT) {
                $query .= "     AND (TR_CD1 = '".STAFFCD."' OR ";
                $query .= "          TR_CD2 = '".STAFFCD."' OR ";
                $query .= "          TR_CD3 = '".STAFFCD."' OR ";
                $query .= "          SUBTR_CD1 = '".STAFFCD."' OR ";
                $query .= "          SUBTR_CD2 = '".STAFFCD."' OR ";
                $query .= "          SUBTR_CD3 = '".STAFFCD."') ";
            }
            $query .= " ORDER BY ";
            $query .= "     VALUE ";
        } else {  //年組選択
            $query  = "SELECT ";
            $query .= "    T1.GRADE || HR_CLASS AS VALUE, ";
            $query .= "    HR_NAME AS LABEL ";
            $query .= "FROM ";
            $query .= "    SCHREG_REGD_HDAT T1 ";
            if ($model->Properties["use_prg_schoolkind"] == "1") {
                if ($model->selectSchoolKind) {
                    $query .= "INNER JOIN SCHREG_REGD_GDAT T2 ON T2.YEAR = T1.YEAR AND T2.GRADE = T1.GRADE ";
                    $query .= " AND T2.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind), "','")."') ";
                }
            } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
                $query .= "INNER JOIN SCHREG_REGD_GDAT T2 ON T2.YEAR = T1.YEAR AND T2.GRADE = T1.GRADE ";
                $query .= " AND T2.SCHOOL_KIND = '".SCHOOLKIND."' ";
            }
            $query .= "WHERE ";
            $query .= "    T1.YEAR = '".CTRL_YEAR."' ";
            $query .= "    AND SEMESTER = '".$model->field["OUTPUT"] ."' ";
            //参照・更新可（制限付き）
            if (AUTHORITY == DEF_REFER_RESTRICT || AUTHORITY == DEF_UPDATE_RESTRICT) {
                $query .= "    AND (TR_CD1 = '" .STAFFCD ."' OR ";
                $query .= "         TR_CD2 = '" .STAFFCD ."' OR ";
                $query .= "         TR_CD3 = '" .STAFFCD ."') ";
            }
            $query .= "ORDER BY";
            $query .= "    VALUE ";
        }

        return $query;
    }

    //職員情報取得
    public function getStaffInfo()
    {
        $query = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     STAFF_DETAIL_SEQ_MST ";
        $query .= " WHERE ";
        $query .= "     STAFFCD = '".STAFFCD."' AND ";
        $query .= "     STAFF_SEQ = '001' ";
        return $query;
    }

    //生徒取得
    public function getAuthStudent($model)
    {
        $query  = "SELECT ";
        $query .= "    T1.SCHREGNO AS VALUE, ";
        $query .= "    T1.SCHREGNO || '　' || T1.ATTENDNO ||  '番　' || L1.NAME_SHOW AS LABEL ";
        $query .= "FROM ";
        $query .= "    SCHREG_REGD_DAT T1 ";
        $query .= "    INNER JOIN SCHREG_BASE_MST L1 ON L1.SCHREGNO = T1.SCHREGNO ";
        $query .= "WHERE ";
        $query .= "    T1.YEAR = '".CTRL_YEAR."' AND ";
        $query .= "    T1.SEMESTER = '".$model->field["OUTPUT"] ."' AND ";
        $query .= "    T1.GRADE || T1.HR_CLASS = '".$model->field["GRADE_HR_CLASS"] ."' ";
        $query .= "ORDER BY ";
        $query .= "    T1.ATTENDNO ";

        return $query;
    }

    //CSV出力データ取得
    public function getCsvQuery($model)
    {
        $sdate = str_replace("/", "-", $model->control["学期開始日付"][$model->field["OUTPUT"]]);
        $edate = str_replace("/", "-", $model->control["学期終了日付"][$model->field["OUTPUT"]]);

        $query  = " SELECT DISTINCT ";
        $query .= "     T1.YEAR, ";
        $query .= "     T1.SEMESTER, ";
        if ($model->field["CHOICE"] == "3" && ($model->isGhr)) {
            $query .= "  TF1.GHR_CD, ";
            $query .= "  TG3.GHR_NAME AS GHR_CLASSNAME, ";
        } elseif ($model->isGakunenKongou) {
            $query .= "  TG1.SCHOOL_KIND AS GK_SCHKIND, ";
            $query .= "  TG1.HR_CLASS AS GK_HR_CLASS, ";
            $query .= "  TG1.HR_CLASS_NAME1 AS GK_CLASSNAME, ";
            $query .= "  DB20.COURSENAME, ";
        }
        $query .= "     T1.GRADE, ";
        $query .= "     T1.HR_CLASS, ";
        if ($model->field["CHOICE"] == "3" && ($model->isGhr)) {
            $query .= "     TG3.GHR_NAME AS HR_NAME, ";
        } else {
            $query .= "     T3.HR_NAME, ";
        }
        $query .= "     L5.STAFFNAME, ";
        $query .= "     T1.SCHREGNO, ";
        if ($model->field["CHOICE"] == "3" && ($model->isGhr)) {
            $query .= "   TF1.GHR_ATTENDNO AS ATTENDNO, ";
        } else {
            $query .= "   T1.ATTENDNO, ";
        }
        $query .= "     T1.ATTENDNO, ";
        $query .= "     T2.NAME, ";
        $query .= "     T2.NAME_ENG, ";
        $query .= "     VALUE(T2.NAME_KANA,'') AS NAME_KANA, ";
        $query .= "     VALUE(CHAR(T2.BIRTHDAY),'') AS BIRTHDAY, ";
        $query .= "     VALUE(L4.ZIPCD,'') AS ZIPCD, ";
        if ($model->z010Name1 == 'sundaikoufu' || $model->z010Name1 == 'mieken') {
            $query .= "     VALUE(L4.ADDR1,'') || VALUE(L4.ADDR2,'') AS ADDR1, ";
        } elseif ($model->z010Name1 == 'bunkyo') {
            $query .= "     VALUE(L4.ADDR1,'') AS ADDR1, ";
            $query .= "     VALUE(L4.ADDR2,'') AS ADDR2, ";
        } else {
            $query .= "     VALUE(L4.ADDR1,'') AS ADDR1, ";
        }
        $query .= "     VALUE(L4.TELNO,'') AS TEL, ";
        $query .= "     VALUE(L1.GUARD_NAME,'') AS GUARD_NAME, ";
        $query .= "     VALUE(T2.EMERGENCYTELNO,'') AS E_TEL, ";
        $query .= "     CASE WHEN GDAT.SCHOOL_KIND = 'P' THEN VALUE(DET002.BASE_REMARK1, L2.FINSCHOOL_NAME,'') ELSE VALUE(L2.FINSCHOOL_NAME,'') END AS FINSCHOOL_NAME, ";
        if ($model->field["FORM"] == "3" && $model->field["FORM3_CLUB_CHECK"] == "1") {
            $query .= "     VALUE(CLUB_T.CLUBNAME,'') AS CLUBNAME, ";
        } else {
            $query .= "   '' AS CLUBNAME,";
        }
        $query .= "     VALUE(L3.ABBV1,'') AS SEX, ";
        $query .= "     VALUE(L6.NAME1,'') AS ENT_DIV_NAME ";
        if ($model->ismeikei && $model->field["FORM3_DORMITORY_CHECK"] == "1") {
            $query .= "     ,CASE WHEN DB9.DOMI_ENTDAY IS NOT NULL THEN '〇' ELSE '' END AS DORMITORYFLG ";
        }
        $query .= " FROM ";
        if ($model->field["CHOICE"] == "3") {
            if (!$model->isGhr) {
                $query .= " SCHREG_REGD_FI_DAT TF1 ";
                $query .= "     LEFT JOIN SCHREG_REGD_DAT T1 ON T1.SCHREGNO = TF1.SCHREGNO ";
                $query .= "     AND T1.YEAR = TF1.YEAR ";
                $query .= "     AND T1.SEMESTER = TF1.SEMESTER ";
                $query .= "     INNER JOIN SCHREG_REGD_HDAT T3 ON T1.YEAR = T3.YEAR ";
                $query .= "     AND T1.SEMESTER = T3.SEMESTER ";
                $query .= "     AND T1.GRADE = T3.GRADE ";
                $query .= "     AND T1.HR_CLASS = T3.HR_CLASS ";
            } else {
                $query .= " SCHREG_REGD_GHR_DAT TF1 ";
                $query .= "     LEFT JOIN SCHREG_REGD_DAT T1 ON T1.SCHREGNO = TF1.SCHREGNO ";
                $query .= "     AND T1.YEAR = TF1.YEAR ";
                $query .= "     AND T1.SEMESTER = TF1.SEMESTER ";
                $query .= "     INNER JOIN SCHREG_REGD_GHR_HDAT TG3 ON TF1.YEAR = TG3.YEAR ";
                $query .= "     AND TF1.SEMESTER = TG3.SEMESTER ";
                $query .= "     AND TF1.GHR_CD = TG3.GHR_CD ";
                $query .= "     INNER JOIN SCHREG_REGD_HDAT T3 ON T1.YEAR = T3.YEAR ";
                $query .= "     AND T1.SEMESTER = T3.SEMESTER ";
                $query .= "     AND T1.GRADE = T3.GRADE ";
                $query .= "     AND T1.HR_CLASS = T3.HR_CLASS ";
            }
        } elseif ($model->isGakunenKongou) {
            $query .= "   V_STAFF_HR_DAT TG1 ";
            $query .= "   INNER JOIN SCHREG_REGD_HDAT T3 ON T3.YEAR = TG1.YEAR ";
            $query .= "    AND T3.SEMESTER = TG1.SEMESTER ";
            $query .= "    AND T3.GRADE = TG1.GRADE ";
            $query .= "    AND T3.HR_CLASS = TG1.HR_CLASS ";
            $query .= "   INNER JOIN SCHREG_REGD_DAT T1 ON T1.YEAR = TG1.YEAR ";
            $query .= "    AND T1.SEMESTER = TG1.SEMESTER ";
            $query .= "    AND T1.GRADE = T3.GRADE ";
            $query .= "    AND T1.HR_CLASS = T3.HR_CLASS ";
        } else {
            $query .= "   SCHREG_REGD_DAT T1 ";
            $query .= "   INNER JOIN SCHREG_REGD_HDAT T3 ";
            $query .= "     ON T1.YEAR = T3.YEAR ";
            $query .= "    AND T1.SEMESTER = T3.SEMESTER ";
            $query .= "    AND T1.GRADE = T3.GRADE ";
            $query .= "    AND T1.HR_CLASS = T3.HR_CLASS ";
        }
        $query .= "     INNER JOIN SCHREG_BASE_MST T2 ON T1.SCHREGNO    = T2.SCHREGNO ";
        $query .= "     LEFT JOIN SCHREG_REGD_GDAT GDAT ON T1.YEAR       = GDAT.YEAR ";
        $query .= "                                   AND T1.GRADE      = GDAT.GRADE ";
        $query .= "     LEFT JOIN GUARDIAN_DAT L1 ON T2.SCHREGNO = L1.SCHREGNO ";
        $query .= "     LEFT JOIN FINSCHOOL_MST L2 ON T2.FINSCHOOLCD = L2.FINSCHOOLCD ";
        $query .= "     LEFT JOIN NAME_MST L3 ON L3.NAMECD1 = 'Z002' AND L3.NAMECD2 = T2.SEX ";
        $query .= "     LEFT JOIN ( SELECT ";
        $query .= "                     SCHREGNO, ";
        $query .= "                     ZIPCD, ";
        $query .= "                     TELNO, ";
        $query .= "                     ADDR1, ";
        $query .= "                     ADDR2 ";
        $query .= "                 FROM ";
        $query .= "                     SCHREG_ADDRESS_DAT W1 ";
        $query .= "                 WHERE ";
        $query .= "                     (W1.SCHREGNO,W1.ISSUEDATE) IN (SELECT ";
        $query .= "                                                         SCHREGNO, ";
        $query .= "                                                         MAX(ISSUEDATE) ";
        $query .= "                                                     FROM ";
        $query .= "                                                         SCHREG_ADDRESS_DAT W2 ";
        $query .= "                                                     WHERE ";
        $query .= "                                                         W2.ISSUEDATE <= '".$edate."' AND ";
        $query .= "                                                         (W2.EXPIREDATE IS NULL OR W2.EXPIREDATE >= '".$sdate."') ";
        $query .= "                                                     GROUP BY ";
        $query .= "                                                         SCHREGNO ";
        $query .= "                                                     ) ";
        $query .= "                 ) L4 ON L4.SCHREGNO = T1.SCHREGNO ";
        $query .= "     LEFT JOIN STAFF_MST L5 ON T3.TR_CD1 = L5.STAFFCD ";
        $query .= "     LEFT JOIN NAME_MST L6 ON L6.NAMECD1 = 'A002' AND L6.NAMECD2 = T2.ENT_DIV ";
        $query .= "     LEFT JOIN SCHREG_BASE_DETAIL_MST DET002 ON DET002.SCHREGNO = T1.SCHREGNO ";
        $query .= "                                            AND DET002.BASE_SEQ ='002' ";
        if ($model->field["FORM"] == "3" && $model->field["FORM3_CLUB_CHECK"] == "1") {
            $query .= "     LEFT JOIN ( SELECT ";
            $query .= "                     SCHW.SCHREGNO, ";
            //校種利用、または校種対応プロパティが立っている場合は校種を取得する。
            if ($model->Properties["useClubMultiSchoolKind"] == "1") {
                //校種対応プロパティでは校種をLOGIN校種で絞る。
                $query .= " SCHW.SCHOOL_KIND, ";
            } elseif ($model->Properties["use_prg_schoolkind"] == "1" || $model->Properties["useSchool_KindField"] == "1") {
                //校種を利用するプロパティ設定であれば、校種を取得する。
                $query .= " SCHW.SCHOOL_KIND, ";
            }
            $query .= "                     SCHW.CLUBCD, ";
            $query .= "                     CLUB_M.CLUBNAME ";
            $query .= "                 FROM ";
            $query .= "                     SCHREG_CLUB_HIST_DAT SCHW ";
            $query .= "                     INNER JOIN CLUB_MST CLUB_M ON SCHW.CLUBCD = CLUB_M.CLUBCD ";
            if ($model->Properties["useClubMultiSchoolKind"] == "1") {
                //校種対応プロパティでは校種をLOGIN校種で絞る。
                $query .= "                        AND SCHW.SCHOOL_KIND = CLUB_M.SCHOOL_KIND ";
            } elseif ($model->Properties["use_prg_schoolkind"] == "1" || $model->Properties["useSchool_KindField"] == "1") {
                //校種を利用するプロパティ設定であれば、学年指定しているのでGDATの校種で絞る。
                $query .= "                        AND SCHW.SCHOOL_KIND = CLUB_M.SCHOOL_KIND ";
            }
            $query .= "                 WHERE ";
            $query .= "                     '".CTRL_DATE."' BETWEEN SCHW.SDATE AND VALUE(SCHW.EDATE, '9999-12-31') ";
            $query .= "                 ORDER BY ";
            $query .= "                     SCHW.SCHREGNO, ";
            $query .= "                     SCHW.CLUBCD ";
            $query .= "                 ) CLUB_T ON CLUB_T.SCHREGNO = T1.SCHREGNO ";
            //校種利用、または校種対応プロパティが立っている場合は校種を絞る。
            if ($model->Properties["useClubMultiSchoolKind"] == "1") {
                //校種対応プロパティでは校種をLOGIN校種で絞る。
                $query .= " AND CLUB_T.SCHOOL_KIND = '".SCHOOLKIND."' ";
            } elseif ($model->Properties["use_prg_schoolkind"] == "1" || $model->Properties["useSchool_KindField"] == "1") {
                //校種を利用するプロパティ設定であれば、学年指定しているのでGDATの校種で絞る。
                $query .= " AND CLUB_T.SCHOOL_KIND = GDAT.SCHOOL_KIND ";
            }
        }
        if ($model->ismeikei && $model->field["FORM3_DORMITORY_CHECK"] == "1") {
            $query .= "   LEFT JOIN SCHREG_DOMITORY_HIST_DAT DB9 ON ";
            $query .= "     DB9.SCHREGNO = T1.SCHREGNO ";
            $query .= "     AND DB9.DOMI_ENTDAY <= '".CTRL_DATE."' ";
            $query .= "     AND ( DB9.DOMI_OUTDAY IS NULL OR '".CTRL_DATE."' < DB9.DOMI_OUTDAY) ";
        }
        $query .= "   LEFT JOIN COURSE_MST DB20 ON DB20.COURSECD = T1.COURSECD ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR     = '".CTRL_YEAR."' AND ";
        $query .= "     T1.SEMESTER = '".$model->field["OUTPUT"]."' AND ";
        if ($model->field["CHOICE"] == "3" && ($model->isGhr)) {
            $query .= "   TF1.GHR_CD IN ('".implode(explode(',', $model->selectdata), "','")."') ";
            $query .= " ORDER BY T1.GRADE, T1.HR_CLASS, TF1.GHR_ATTENDNO ";
        } elseif ($model->isGakunenKongou) {
            $query .= "   TG1.SCHOOL_KIND || '-' || TG1.HR_CLASS IN ('".implode(explode(',', substr($model->selectdata, 1)), "','")."') ";
            $query .= " ORDER BY TG1.SCHOOL_KIND, TG1.HR_CLASS, T1.GRADE,T1.HR_CLASS,T1.ATTENDNO ";
        } else {
            if ($model->field["CHOICE"] == "1") {
                $query .= "     T1.GRADE || T1.HR_CLASS IN ('".implode(explode(',', $model->selectdata), "','")."') ";
            } else {
                $query .= "     T1.SCHREGNO IN ('".implode(explode(',', $model->selectdata), "','")."') ";
            }
            $query .= " ORDER BY ";
            $query .= "     T1.GRADE, ";
            $query .= "     T1.HR_CLASS, ";
            $query .= "     T1.ATTENDNO ";
        }

        return $query;
    }

    //テーブル存在チェック
    public function checkTableExist()
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     SYSIBM.SYSTABLES ";
        $query .= " WHERE ";
        $query .= "     NAME = 'SETTING_DAT' ";

        return $query;
    }

    //生徒項目名取得
    public function getSchName($model)
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     REMARK1, ";
        $query .= "     SCHOOLCD ";
        $query .= " FROM ";
        $query .= "     SETTING_DAT ";
        $query .= " WHERE ";
        $query .= "     SEQ = '001' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            $query .= " AND SCHOOL_KIND IN (SELECT ";
            $query .= "                         SCHOOL_KIND ";
            $query .= "                     FROM ";
            $query .= "                         SCHREG_REGD_GDAT ";
            $query .= "                     WHERE ";
            $query .= "                         YEAR    = '".CTRL_YEAR."' AND ";
            $query .= "                         GRADE   = '". substr($model->field["GRADE_HR_CLASS"], 0, 2)."' ";
            $query .= "                     ) ";
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND SCHOOLCD    = '".sprintf("%012d", SCHOOLCD)."' ";
            $query .= " AND SCHOOL_KIND = '".SCHOOLKIND."' ";
        } else {
            $query .= " AND SCHOOL_KIND IN (SELECT ";
            $query .= "                         SCHOOL_KIND ";
            $query .= "                     FROM ";
            $query .= "                         SCHREG_REGD_GDAT ";
            $query .= "                     WHERE ";
            $query .= "                         YEAR    = '".CTRL_YEAR."' AND ";
            $query .= "                         GRADE   = '". substr($model->field["GRADE_HR_CLASS"], 0, 2)."' ";
            $query .= "                     ) ";
        }
        $query .= " ORDER BY ";
        $query .= "     SCHOOLCD ";

        return $query;
    }

    public function getZ010Name1()
    {
        $query  = " SELECT ";
        $query .= "     NAME1 ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = 'Z010' ";
        $query .= "     AND NAMECD2 = '00' ";

        return $query;
    }
}
