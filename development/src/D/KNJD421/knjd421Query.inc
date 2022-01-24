<?php

require_once('for_php7.php');

class knjd421Query extends Query {

    //学期を取得
    function getSemester() {
        $query  = " SELECT ";
        $query .= "     SEMESTER AS VALUE, ";
        $query .= "     SEMESTERNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SEMESTER_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= " AND SEMESTER <> '9' ";
        $query .= " ORDER BY ";
        $query .= "     SEMESTER ";

        return $query;
    }

    //所見学期取得
    function getD078($namecd1, $countFlg = '')
    {
        $query  = " SELECT ";
        if ($countFlg) {
            $query .= "     COUNT(*) AS COUNT ";
        } else {
            $query .= "     NAME1 AS LABEL, ";
            $query .= "     NAMECD2 AS VALUE, ";
            $query .= "     CASE WHEN ABBV1 = '".CTRL_SEMESTER."' THEN 1 END AS DEF_VALUE_FLG ";
        }
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= " AND NAMECD1 = '{$namecd1}' ";
        $query .= " ORDER BY ";
        $query .= "     NAMECD2 ";

        return $query;
    }

    //年組取得
    function getGradeHrClass($model) {
        $query  = " SELECT DISTINCT ";
        $query .= "     T1.HR_NAME AS LABEL, ";
        $query .= "     T1.GRADE || '-' || T1.HR_CLASS AS VALUE ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_HDAT T1 ";
        $query .= "     INNER JOIN SCHREG_REGD_GDAT G1 ";
        $query .= "          ON G1.YEAR         = T1.YEAR ";
        $query .= "         AND G1.GRADE        = T1.GRADE ";
        $query .= "         AND G1.SCHOOL_KIND  = '".$model->field["SCHOOL_KIND"]."' ";
        $query .= "     LEFT JOIN SCHREG_REGD_DAT T3 ";
        $query .= "          ON T3.YEAR     = T1.YEAR ";
        $query .= "         AND T3.SEMESTER = T1.SEMESTER ";
        $query .= "         AND T3.GRADE    = T1.GRADE ";
        $query .= "         AND T3.HR_CLASS = T1.HR_CLASS ";
        $query .= "     LEFT JOIN SCHREG_BASE_MST L1 ";
        $query .= "          ON L1.SCHREGNO = T3.SCHREGNO ";
        $query .= "     LEFT JOIN SCHREG_BASE_YEAR_DETAIL_MST L2 ";
        $query .= "          ON L2.SCHREGNO = T3.SCHREGNO ";
        $query .= "         AND L2.YEAR     = T1.YEAR ";
        $query .= "         AND L2.BASE_SEQ = '004' ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR     = '".CTRL_YEAR."' ";
        if ($model->field["SEMESTER"] == '9') {
            $query .= " AND T1.SEMESTER = '". CTRL_SEMESTER ."' ";
        } else {
            $query .= " AND T1.SEMESTER = '".$model->field["SEMESTER"]."' ";
        }
        //更新・参照可（制限付き）
        if (AUTHORITY == DEF_UPDATE_RESTRICT || AUTHORITY == DEF_REFER_RESTRICT) {
            $query .= " AND (T1.TR_CD1      = '". STAFFCD ."' OR ";
            $query .= "      T1.TR_CD2      = '". STAFFCD ."' OR ";
            $query .= "      T1.TR_CD3      = '". STAFFCD ."' OR ";
            $query .= "      T1.SUBTR_CD1   = '". STAFFCD ."' OR ";
            $query .= "      T1.SUBTR_CD2   = '". STAFFCD ."' OR ";
            $query .= "      T1.SUBTR_CD3   = '". STAFFCD ."') ";
        }
        $query .= "     AND CASE WHEN L2.BASE_REMARK1 IS NULL THEN L1.HANDICAP ELSE L2.BASE_REMARK1 END ";
        $query .= "         IN (SELECT NAMECD2 FROM V_NAME_MST N1 WHERE N1.YEAR = T1.YEAR AND N1.NAMECD1 = 'A025' AND N1.NAMESPARE3 = '1') ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //年組取得（学年混合）
    function getStaffHr($model) {
        $query  = " SELECT ";
        $query .= "     T1.SCHOOL_KIND || '-' || T1.HR_CLASS  || ':' || T1.HR_CLASS_NAME1 AS LABEL, ";
        $query .= "     T1.HR_CLASS AS VALUE ";
        $query .= " FROM ";
        $query .= "     V_STAFF_HR_DAT T1 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR         = '".CTRL_YEAR."' ";
        if ($model->field["SEMESTER"] == '9') {
            $query .= " AND T1.SEMESTER = '". CTRL_SEMESTER ."' ";
        } else {
            $query .= " AND T1.SEMESTER = '".$model->field["SEMESTER"]."' ";
        }
        $query .= "     AND T1.SCHOOL_KIND = '".$model->field["SCHOOL_KIND"]."' ";
        //更新・参照可（制限付き）
        if (AUTHORITY == DEF_UPDATE_RESTRICT || AUTHORITY == DEF_REFER_RESTRICT) {
            $query .= " AND T1.STAFFCD  = '".STAFFCD."' ";
        }
        $query .= "     AND T1.HR_CLASS_NAME1 IN (  SELECT ";
        $query .= "                                     MAX(M1.HR_CLASS_NAME1) AS MAX_NAME  ";
        $query .= "                                 FROM ";
        $query .= "                                     V_STAFF_HR_DAT M1 ";
        $query .= "                                 WHERE ";
        $query .= "                                     T1.YEAR         = M1.YEAR AND ";
        $query .= "                                     T1.SEMESTER     = M1.SEMESTER AND ";
        $query .= "                                     T1.HR_CLASS     = M1.HR_CLASS AND ";
        $query .= "                                     T1.SCHOOL_KIND  = M1.SCHOOL_KIND AND ";
        $query .= "                                     T1.STAFFCD      = M1.STAFFCD ";
        $query .= "                                 ) ";
        $query .= " GROUP BY ";
        $query .= "     T1.HR_CLASS, ";
        $query .= "     T1.HR_CLASS_NAME1, ";
        $query .= "     T1.SCHOOL_KIND ";
        $query .= " ORDER BY ";
        $query .= "     T1.SCHOOL_KIND, ";
        $query .= "     VALUE ";

        return $query;
    }

    //実クラス取得
    function getGhr($model, $ghrCd = '') {
        $query  = " SELECT DISTINCT ";
        $query .= "     T1.GHR_NAME AS LABEL, ";
        $query .= "     T1.GHR_CD AS VALUE, ";
        $query .= "     MAX(T4.SCHOOL_KIND) AS SCHOOL_KIND, ";
        $query .= "     CASE WHEN MIN(T4.SCHOOL_KIND) = MAX(T4.SCHOOL_KIND) THEN 1 END AS SCHOOL_KIND_FLG ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_GHR_HDAT T1 ";
        $query .= "     INNER JOIN SCHREG_REGD_GHR_DAT T2 ";
        $query .= "          ON T2.YEAR     = T1.YEAR ";
        $query .= "         AND T2.SEMESTER = T1.SEMESTER ";
        $query .= "         AND T2.GHR_CD   = T1.GHR_CD ";
        $query .= "     LEFT JOIN SCHREG_REGD_DAT T3 ";
        $query .= "          ON T3.YEAR     = T1.YEAR ";
        $query .= "         AND T3.SEMESTER = T1.SEMESTER ";
        $query .= "         AND T3.SCHREGNO = T2.SCHREGNO ";
        $query .= "     LEFT JOIN SCHREG_REGD_GDAT T4 ";
        $query .= "          ON T4.YEAR     = T3.YEAR ";
        $query .= "         AND T4.GRADE    = T3.GRADE ";
        $query .= "     LEFT JOIN SCHREG_BASE_MST L1 ";
        $query .= "          ON L1.SCHREGNO = T2.SCHREGNO ";
        $query .= "     LEFT JOIN SCHREG_BASE_YEAR_DETAIL_MST L2 ";
        $query .= "          ON L2.SCHREGNO = L1.SCHREGNO ";
        $query .= "         AND L2.YEAR     = T1.YEAR ";
        $query .= "         AND L2.BASE_SEQ = '004' ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR     = '".CTRL_YEAR."' ";
        if ($model->field["SEMESTER"] == '9') {
            $query .= " AND T1.SEMESTER = '". CTRL_SEMESTER ."' ";
        } else {
            $query .= " AND T1.SEMESTER = '".$model->field["SEMESTER"]."' ";
        }
        if ($ghrCd) {
            $query .= " AND T1.GHR_CD = '". $ghrCd."' ";
        }
        //更新・参照可（制限付き）
        if (AUTHORITY == DEF_UPDATE_RESTRICT || AUTHORITY == DEF_REFER_RESTRICT) {
            $query .= " AND (T1.TR_CD1    = '". STAFFCD ."' OR ";
            $query .= "      T1.TR_CD2    = '". STAFFCD ."' OR ";
            $query .= "      T1.TR_CD3    = '". STAFFCD ."' OR ";
            $query .= "      T1.SUBTR_CD1 = '". STAFFCD ."' OR ";
            $query .= "      T1.SUBTR_CD2 = '". STAFFCD ."' OR ";
            $query .= "      T1.SUBTR_CD3 = '". STAFFCD ."') ";
        }
        $query .= "     AND CASE WHEN L2.BASE_REMARK1 IS NULL THEN L1.HANDICAP ELSE L2.BASE_REMARK1 END ";
        $query .= "         IN (SELECT NAMECD2 FROM V_NAME_MST N1 WHERE N1.YEAR = T1.YEAR AND N1.NAMECD1 = 'A025' AND N1.NAMESPARE3 = '1') ";
        $query .= " GROUP BY ";
        $query .= "     T1.GHR_CD, T1.GHR_NAME ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //学校名取得の取得(テストコンボで使う)
    function getSchoolName() {
        $query  = " SELECT ";
        $query .= "     NAME1, NAME2 ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = 'Z010' AND ";
        $query .= "     NAMECD2 = '00' ";

        return $query;
    }

    //校種
    function getSchoolKind($model) {
        $query  = " SELECT DISTINCT ";
        $query .= "     NMA023.NAMECD2, ";
        $query .= "     NMA023.ABBV1 AS LABEL, ";
        $query .= "     NMA023.NAME1 AS VALUE ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST NMA023 ";
        $query .= "     INNER JOIN SCHREG_REGD_GDAT T2 ON T2.YEAR = NMA023.YEAR ";
        $query .= "       AND T2.SCHOOL_KIND = NMA023.NAME1 ";
        $query .= "     INNER JOIN SCHREG_REGD_DAT T3 ON T3.YEAR = T2.YEAR ";
        $query .= "       AND T3.GRADE = T2.GRADE ";
        $query .= " WHERE ";
        $query .= "     NMA023.YEAR                 = '".CTRL_YEAR."' ";
        $query .= " AND NMA023.NAMECD1              = 'A023' ";
        if ($model->field["SEMESTER"] == '9') {
            $query .= "     AND T3.SEMESTER         = '". CTRL_SEMESTER ."' ";
        } else {
            $query .= "     AND T3.SEMESTER         = '".$model->field["SEMESTER"]."' ";
        }
        if ($model->Properties["useSpecial_Support_Hrclass"] == '1') {
            $query .= " UNION ";
            $query .= " SELECT DISTINCT ";
            $query .= "     NMA023.NAMECD2, ";
            $query .= "     NMA023.ABBV1 AS LABEL, ";
            $query .= "     NMA023.NAME1 AS VALUE ";
            $query .= " FROM ";
            $query .= "     V_NAME_MST NMA023 ";
            $query .= "     INNER JOIN SCHREG_REGD_GDAT T2 ON T2.YEAR = NMA023.YEAR ";
            $query .= "       AND T2.SCHOOL_KIND = NMA023.NAME1 ";
            $query .= "     INNER JOIN SCHREG_REGD_DAT T3 ON T3.YEAR = T2.YEAR ";
            $query .= "       AND T3.GRADE = T2.GRADE ";
            $query .= "     INNER JOIN SCHREG_REGD_GHR_DAT T4 ON T4.SCHREGNO = T3.SCHREGNO ";
            $query .= "       AND T4.YEAR = T3.YEAR ";
            $query .= "       AND T4.SEMESTER = T3.SEMESTER ";
            $query .= " WHERE ";
            $query .= "     NMA023.YEAR                 = '".CTRL_YEAR."' ";
            $query .= " AND NMA023.NAMECD1              = 'A023' ";
            if ($model->field["SEMESTER"] == '9') {
                $query .= "     AND T3.SEMESTER         = '". CTRL_SEMESTER ."' ";
            } else {
                $query .= "     AND T3.SEMESTER         = '".$model->field["SEMESTER"]."' ";
            }
        }
        $query .= " ORDER BY ";
        $query .= "     NAMECD2 ";

        return $query;
    }

    //グループ
    function getGradeKindGroup($model) {
        if ($model->field["HUKUSIKI_RADIO"] == "2") {
            //実クラス
            $query  = " SELECT DISTINCT ";
            $query .= "     T1.CONDITION || T1.GROUPCD  || '　' || NMA033.NAME1 || '　' || T1.GROUPNAME AS LABEL, ";
            $query .= "     T1.GHR_CD || T1.GRADE || T1.HR_CLASS || T1.CONDITION || T1.GROUPCD AS VALUE ";
            $query .= " FROM ";
            $query .= "     GRADE_KIND_COMP_GROUP_YMST T1 ";
            $query .= "     INNER JOIN GRADE_KIND_SCHREG_GROUP_DAT T5 ";
            $query .= "          ON T5.YEAR         = T1.YEAR ";
            $query .= "         AND T5.SEMESTER     = T1.SEMESTER ";
            $query .= "         AND T5.GAKUBU_SCHOOL_KIND = T1.GAKUBU_SCHOOL_KIND ";
            $query .= "         AND T5.GHR_CD       = T1.GHR_CD ";
            $query .= "         AND T5.GRADE        = T1.GRADE ";
            $query .= "         AND T5.HR_CLASS     = T1.HR_CLASS ";
            $query .= "         AND T5.CONDITION    = T1.CONDITION ";
            $query .= "         AND T5.GROUPCD      = T1.GROUPCD ";
            $query .= "     INNER JOIN SCHREG_REGD_GHR_DAT T4 ";
            $query .= "          ON T4.SCHREGNO     = T5.SCHREGNO ";
            $query .= "         AND T4.YEAR         = T5.YEAR ";
            if ($model->field["SEMESTER"] == '9') {
                $query .= "        AND T4.SEMESTER = '". CTRL_SEMESTER ."' ";
            } else {
                $query .= "        AND T4.SEMESTER = '".$model->field["SEMESTER"]."' ";
            }
            $query .= "     LEFT JOIN V_NAME_MST NMA033 ";
            $query .= "          ON T1.YEAR         = NMA033.YEAR ";
            $query .= "         AND 'A033'          = NMA033.NAMECD1 ";
            $query .= "         AND T1.CONDITION    = NMA033.NAMECD2 ";
            $query .= " WHERE ";
            $query .= "     T1.YEAR         = '".CTRL_YEAR."' ";
            if ($model->Properties["useGradeKindCompGroupSemester"] == "1") {
                if ($model->field["SEMESTER"] == '9') {
                    $query .= " AND T1.SEMESTER = '". CTRL_SEMESTER ."' ";
                } else {
                    $query .= " AND T1.SEMESTER = '".$model->field["SEMESTER"]."' ";
                }
            } else {
                $query .= " AND T1.SEMESTER = '9' ";
            }
            $query .= "     AND T1.GAKUBU_SCHOOL_KIND = '". $model->field["SCHOOL_KIND"] ."' ";
            $query .= "     AND T1.GHR_CD   = '".$model->field["GHR_CD"]."' ";
        } else {
            //法定クラス
            $query  = " SELECT DISTINCT ";
            $query .= "     T1.CONDITION || T1.GROUPCD  || '　' || NMA033.NAME1 || '　' || T1.GROUPNAME AS LABEL, ";
            $query .= "     T1.GHR_CD || T1.GRADE || T1.HR_CLASS || T1.CONDITION || T1.GROUPCD AS VALUE ";
            $query .= " FROM ";
            $query .= "     GRADE_KIND_COMP_GROUP_YMST T1 ";
            $query .= "     INNER JOIN SCHREG_REGD_GDAT T2 ";
            $query .= "          ON T2.YEAR         = T1.YEAR ";
            $query .= "         AND T2.SCHOOL_KIND  = T1.GAKUBU_SCHOOL_KIND ";
            $query .= "     INNER JOIN SCHREG_REGD_DAT T3 ";
            $query .= "          ON T3.YEAR         = T1.YEAR ";
            if ($model->field["SEMESTER"] == '9') {
                $query .= "     AND T3.SEMESTER = '". CTRL_SEMESTER ."' ";
            } else {
                $query .= "     AND T3.SEMESTER = '".$model->field["SEMESTER"]."' ";
            }
            $query .= "         AND T3.GRADE        = T2.GRADE ";
            $query .= "     INNER JOIN GRADE_KIND_SCHREG_GROUP_DAT T5 ";
            $query .= "          ON T5.YEAR         = T1.YEAR ";
            $query .= "         AND T5.SEMESTER     = T1.SEMESTER ";
            $query .= "         AND T5.GAKUBU_SCHOOL_KIND = T1.GAKUBU_SCHOOL_KIND ";
            $query .= "         AND T5.GHR_CD       = T1.GHR_CD ";
            $query .= "         AND T5.GRADE        = T1.GRADE ";
            $query .= "         AND T5.HR_CLASS     = T1.HR_CLASS ";
            $query .= "         AND T5.CONDITION    = T1.CONDITION ";
            $query .= "         AND T5.GROUPCD      = T1.GROUPCD ";
            $query .= "         AND T5.SCHREGNO     = T3.SCHREGNO ";
            $query .= "     LEFT JOIN V_NAME_MST NMA033 ";
            $query .= "          ON T1.YEAR         = NMA033.YEAR ";
            $query .= "         AND 'A033'          = NMA033.NAMECD1 ";
            $query .= "         AND T1.CONDITION    = NMA033.NAMECD2 ";
            $query .= " WHERE ";
            $query .= "     T1.YEAR         = '".CTRL_YEAR."' ";
            if ($model->Properties["useGradeKindCompGroupSemester"] == "1") {
                if ($model->field["SEMESTER"] == '9') {
                    $query .= " AND T1.SEMESTER = '". CTRL_SEMESTER ."' ";
                } else {
                    $query .= " AND T1.SEMESTER = '".$model->field["SEMESTER"]."' ";
                }
            } else {
                $query .= " AND T1.SEMESTER = '9' ";
            }
            if ($model->Properties["useSpecial_Support_Hrclass"] == '1') {
                $query .= " AND NOT EXISTS (SELECT ";
                $query .= "                     'X' ";
                $query .= "                 FROM ";
                $query .= "                     V_NAME_MST N1 ";
                $query .= "                 WHERE ";
                $query .= "                     N1.YEAR     = '".CTRL_YEAR."' ";
                $query .= "                 AND N1.NAMECD1  = 'A032' ";
                $query .= "                 AND N1.NAMECD2  = T3.GRADE ";
                $query .= "                 AND N1.NAME1    = T3.HR_CLASS ";
                $query .= "                 ) ";
            }
            $query .= "     AND T1.GAKUBU_SCHOOL_KIND = '".$model->field["SCHOOL_KIND"] ."' ";
            //学年混合
            if ($model->field["GRADE_MIX"] == "1") {
                $query .= " AND T1.HR_CLASS = '".$model->field["GHR_CD"]."' ";
            //年組
            } else {
                $query .= " AND T1.GRADE || '-' || T1.HR_CLASS = '".$model->field["GHR_CD"]."' ";
            }
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //教科区分生徒取得
    function getClassDivOrder($model) {
        $query = "";
        $query .= " WITH CLASS_DIV(VALUE, LABEL) AS ( ";
        $query .= "     VALUES('0', '教科') ";
        $query .= "    UNION ALL ";
        $query .= "     VALUES('1', '各教科等を合わせた指導') ";
        $query .= "    UNION ALL ";
        $query .= "     VALUES('2', '領域') ";
        $query .= "    UNION ALL ";
        $query .= "     VALUES('3', '総学') ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     CLASS_DIV T1 ";
        $query .= " ORDER BY VALUE ";
        return $query;
    }

    //生徒取得
    function getStudent($model) {
        if ($model->field["HUKUSIKI_RADIO"] == "2") {
            //実クラス
            $query  = " SELECT DISTINCT ";
            $query .= "     T4.GHR_NAME || T1.GHR_ATTENDNO || '番　' || T1.SCHREGNO || '　' || L1.NAME_SHOW AS LABEL, ";
            $query .= "     '1-' || T1.GHR_CD || T1.GHR_ATTENDNO || '-' || T1.SCHREGNO AS VALUE ";
            $query .= " FROM ";
            $query .= "     SCHREG_REGD_GHR_DAT T1 ";
            $query .= "     INNER JOIN SCHREG_BASE_MST L1 ";
            $query .= "          ON L1.SCHREGNO     = T1.SCHREGNO ";
            $query .= "     INNER JOIN SCHREG_REGD_DAT T2 ";
            $query .= "          ON T2.SCHREGNO     = T1.SCHREGNO ";
            $query .= "         AND T2.YEAR         = T1.YEAR ";
            $query .= "         AND T2.SEMESTER     = T1.SEMESTER ";
            $query .= "     INNER JOIN SCHREG_REGD_GDAT T3 ";
            $query .= "          ON T3.YEAR         = T2.YEAR ";
            $query .= "         AND T3.GRADE        = T2.GRADE ";
            $query .= "         AND T3.SCHOOL_KIND  = '{$model->field["SCHOOL_KIND"]}' ";
            $query .= "     INNER JOIN SCHREG_REGD_GHR_HDAT T4 ";
            $query .= "          ON T4.YEAR         = T1.YEAR ";
            $query .= "         AND T4.SEMESTER     = T1.SEMESTER ";
            $query .= "         AND T4.GHR_CD       = T1.GHR_CD ";
            $query .= "     LEFT JOIN SCHREG_BASE_YEAR_DETAIL_MST L2 ";
            $query .= "          ON L2.SCHREGNO = T1.SCHREGNO ";
            $query .= "         AND L2.YEAR     = T1.YEAR ";
            $query .= "         AND L2.BASE_SEQ = '004' ";
            $query .= " WHERE ";
            $query .= "     T1.YEAR         = '".CTRL_YEAR."' ";
            if ($model->field["SEMESTER"] == '9') {
                $query .= " AND T1.SEMESTER = '". CTRL_SEMESTER ."' ";
            } else {
                $query .= " AND T1.SEMESTER = '".$model->field["SEMESTER"]."' ";
            }
            if ($model->field["GHR_CD"] == '' && $model->field["GRADE_KIND_GROUP"] == '') {
                    $query .= " AND T1.SCHREGNO = '--------' ";
            } else {
                if ($model->field["GHR_CD"]) {
                    $query .= " AND T1.GHR_CD = '".$model->field["GHR_CD"]."' ";
                }
                if ($model->field["GRADE_KIND_GROUP"]) {
                    $query .= " AND EXISTS (SELECT ";
                    $query .= "                 'X' ";
                    $query .= "             FROM ";
                    $query .= "                 GRADE_KIND_SCHREG_GROUP_DAT T5 ";
                    $query .= "             WHERE ";
                    $query .= "                 T5.YEAR     = T1.YEAR ";
                    if ($model->Properties["useGradeKindCompGroupSemester"] == "1") {
                        $query .= "             AND T5.SEMESTER = T1.SEMESTER ";
                    } else {
                        $query .= "             AND T5.SEMESTER = '9' ";
                    }
                    $query .= "             AND T5.GAKUBU_SCHOOL_KIND = '".$model->field["SCHOOL_KIND"]."' ";
                    $query .= "             AND T5.GHR_CD || T5.GRADE || T5.HR_CLASS || T5.CONDITION || T5.GROUPCD = '".$model->field["GRADE_KIND_GROUP"]."' ";
                    $query .= "             AND T5.SCHREGNO = T1.SCHREGNO ";
                    $query .= "             ) ";
                }
            }
        } else {
            //法定クラス
            $query  = " SELECT DISTINCT ";
            $query .= "     T4.HR_NAME || T1.ATTENDNO || '番　' || T1.SCHREGNO || '　' || L1.NAME_SHOW AS LABEL, ";
            $query .= "     '2-' || T1.GRADE || T1.HR_CLASS || T1.ATTENDNO || '-' || T1.SCHREGNO AS VALUE ";
            $query .= " FROM ";
            $query .= "     SCHREG_REGD_DAT T1 ";
            $query .= "     INNER JOIN SCHREG_BASE_MST L1 ";
            $query .= "          ON L1.SCHREGNO     = T1.SCHREGNO ";
            $query .= "     INNER JOIN SCHREG_REGD_GDAT T3 ";
            $query .= "          ON T3.YEAR         = T1.YEAR ";
            $query .= "         AND T3.GRADE        = T1.GRADE ";
            $query .= "         AND T3.SCHOOL_KIND  = '{$model->field["SCHOOL_KIND"]}' ";
            $query .= "     INNER JOIN SCHREG_REGD_HDAT T4 ";
            $query .= "          ON T4.YEAR         = T1.YEAR ";
            $query .= "         AND T4.SEMESTER     = T1.SEMESTER ";
            $query .= "         AND T4.GRADE        = T1.GRADE ";
            $query .= "         AND T4.HR_CLASS     = T1.HR_CLASS ";
            $query .= "     LEFT JOIN SCHREG_BASE_YEAR_DETAIL_MST L2 ";
            $query .= "          ON L2.SCHREGNO = T1.SCHREGNO ";
            $query .= "         AND L2.YEAR     = T1.YEAR ";
            $query .= "         AND L2.BASE_SEQ = '004' ";
            $query .= " WHERE ";
            $query .= "     T1.YEAR         = '".CTRL_YEAR."' ";
            if ($model->field["SEMESTER"] == '9') {
                $query .= " AND T1.SEMESTER = '". CTRL_SEMESTER ."' ";
            } else {
                $query .= " AND T1.SEMESTER = '".$model->field["SEMESTER"]."' ";
            }
            if ($model->field["GHR_CD"] == '' && $model->field["GRADE_KIND_GROUP"] == '') {
                $query .= "    AND T1.SCHREGNO = '--------' ";
            } else {
                if ($model->field["GHR_CD"]) {
                    //学年混合
                    if ($model->field["GRADE_MIX"] == "1") {
                        $query .= " AND T1.HR_CLASS = '".$model->field["GHR_CD"]."' ";
                    //年組
                    } else {
                        $query .= " AND T1.GRADE || '-' || T1.HR_CLASS = '".$model->field["GHR_CD"]."' ";
                    }
                }
                if ($model->field["GRADE_KIND_GROUP"]) {
                    $query .= " AND EXISTS (SELECT ";
                    $query .= "                 'X' ";
                    $query .= "             FROM ";
                    $query .= "                 GRADE_KIND_SCHREG_GROUP_DAT T5 ";
                    $query .= "             WHERE ";
                    $query .= "                 T5.YEAR     = T1.YEAR ";
                    if ($model->Properties["useGradeKindCompGroupSemester"] == "1") {
                        $query .= "             AND T5.SEMESTER = T1.SEMESTER ";
                    } else {
                        $query .= "             AND T5.SEMESTER = '9' ";
                    }
                    $query .= "             AND T5.GAKUBU_SCHOOL_KIND = '".$model->field["SCHOOL_KIND"]."' ";
                    $query .= "             AND T5.GHR_CD || T5.GRADE || T5.HR_CLASS || T5.CONDITION || T5.GROUPCD = '".$model->field["GRADE_KIND_GROUP"]."' ";
                    $query .= "             AND T5.SCHREGNO = T1.SCHREGNO ";
                    $query .= "             ) ";
                }
            }
        }
        $query .= "     AND CASE WHEN L2.BASE_REMARK1 IS NULL THEN L1.HANDICAP ELSE L2.BASE_REMARK1 END ";
        $query .= "         IN (SELECT NAMECD2 FROM V_NAME_MST N1 WHERE N1.YEAR = T1.YEAR AND N1.NAMECD1 = 'A025' AND N1.NAMESPARE3 = '1') ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }
}
?>
