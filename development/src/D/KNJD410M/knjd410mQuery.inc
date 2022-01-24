<?php

require_once('for_php7.php');

class knjd410mQuery extends Query
{

    /****************/
    /*    左画面    */
    /****************/

    //校種取得
    public function getSchKind($model, $val = "")
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     ABBV1 AS LABEL, ";
        $query .= "     NAME1 AS VALUE ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "         YEAR    = '".$model->year."' ";
        $query .= "     AND NAMECD1 = 'A023' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= "     AND NAME1 IN ('".implode(explode(':', $model->selectSchoolKind), "','")."') ";
            }
        } elseif ($model->Properties["useSchool_KindField"] == "1") {
            $query .= " AND NAME1 = '".SCHOOLKIND."' ";
        }
        if ($val) {
            $query .= " AND NAME1 = '".$val."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //クラスコンボ
    public function getHrClass($model)
    {
        if ($model->hukusiki_radio == "2") {
            $query  = " SELECT DISTINCT ";
            $query .= "     L0.GHR_CD AS VALUE, ";
            $query .= "     L0.GHR_NAME AS LABEL ";
            $query .= " FROM ";
            $query .= "     SCHREG_REGD_GHR_DAT T0 ";
            $query .= "     INNER JOIN SCHREG_REGD_GHR_HDAT L0 ON L0.YEAR       = T0.YEAR  ";
            $query .= "                                       AND L0.SEMESTER   = T0.SEMESTER ";
            $query .= "                                       AND L0.GHR_CD     = T0.GHR_CD, ";
            $query .= "     SCHREG_REGD_DAT T1 ";
            $query .= "     INNER JOIN SCHREG_REGD_HDAT T2 ON T2.YEAR       = T1.YEAR  ";
            $query .= "                                   AND T2.SEMESTER   = T1.SEMESTER  ";
            $query .= "                                   AND T2.GRADE      = T1.GRADE  ";
            $query .= "                                   AND T2.HR_CLASS   = T1.HR_CLASS ";
            $query .= "     LEFT JOIN SCHREG_BASE_MST L1 ON L1.SCHREGNO = T1.SCHREGNO ";
            $query .= "     LEFT JOIN SCHREG_BASE_YEAR_DETAIL_MST L2 ";
            $query .= "          ON L2.SCHREGNO     = T1.SCHREGNO ";
            $query .= "         AND L2.YEAR         = T1.YEAR ";
            $query .= "         AND L2.BASE_SEQ     = '004', ";
            $query .= "     SCHREG_REGD_GDAT T3 ";
            $query .= " WHERE ";
            $query .= "     T0.YEAR         = '{$model->year}' ";
            $query .= " AND T0.SEMESTER     = '{$model->regdSemester}' ";
            $query .= " AND T0.YEAR         = T1.YEAR ";
            $query .= " AND T0.SEMESTER     = T1.SEMESTER ";
            $query .= " AND T0.SCHREGNO     = T1.SCHREGNO ";
            $query .= " AND T1.YEAR         = T3.YEAR ";
            $query .= " AND T1.GRADE        = T3.GRADE ";
            $query .= " AND T3.SCHOOL_KIND  = '{$model->gakubu_school_kind}' ";
            $query .= " AND T2.GRADE || T2.HR_CLASS NOT IN (SELECT ";
            $query .= "                                         NAMECD2 || NAME1 AS HR_CLASS ";
            $query .= "                                     FROM ";
            $query .= "                                         V_NAME_MST ";
            $query .= "                                     WHERE ";
            $query .= "                                         NAMECD1 = 'A032' ";
            $query .= "                                     AND YEAR = '{$model->year}') ";
            if (AUTHORITY == DEF_REFER_RESTRICT || AUTHORITY == DEF_UPDATE_RESTRICT) {
                $query .= " AND (   L0.TR_CD1       = '".STAFFCD."' ";
                $query .= "      OR L0.TR_CD2       = '".STAFFCD."' ";
                $query .= "      OR L0.TR_CD3       = '".STAFFCD."' ";
                $query .= "      OR L0.SUBTR_CD1    = '".STAFFCD."' ";
                $query .= "      OR L0.SUBTR_CD2    = '".STAFFCD."' ";
                $query .= "      OR L0.SUBTR_CD3    = '".STAFFCD."') ";
            }
        } else {
            $query  = " SELECT DISTINCT ";
            $query .= "     T1.GRADE || '-' || T1.HR_CLASS AS VALUE, ";
            $query .= "     T1.HR_NAME AS LABEL ";
            $query .= " FROM ";
            $query .= "     SCHREG_REGD_HDAT T1 ";
            $query .= "     INNER JOIN SCHREG_REGD_DAT L0 ";
            $query .= "          ON L0.YEAR         = T1.YEAR  ";
            $query .= "         AND L0.SEMESTER     = T1.SEMESTER  ";
            $query .= "         AND L0.GRADE        = T1.GRADE  ";
            $query .= "         AND L0.HR_CLASS     = T1.HR_CLASS ";
            $query .= "     LEFT JOIN SCHREG_BASE_MST L1 ON L1.SCHREGNO = L0.SCHREGNO ";
            $query .= "     LEFT JOIN SCHREG_BASE_YEAR_DETAIL_MST L2 ";
            $query .= "          ON L2.SCHREGNO     = L0.SCHREGNO ";
            $query .= "         AND L2.YEAR         = L0.YEAR ";
            $query .= "         AND L2.BASE_SEQ     = '004', ";
            $query .= "     SCHREG_REGD_GDAT T2 ";
            $query .= " WHERE ";
            $query .= "     T1.YEAR         = '{$model->year}' ";
            $query .= " AND T1.SEMESTER     = '{$model->regdSemester}' ";
            $query .= " AND T1.YEAR         = T2.YEAR ";
            $query .= " AND T1.GRADE        = T2.GRADE ";
            $query .= " AND T2.SCHOOL_KIND  = '{$model->gakubu_school_kind}' ";
            $query .= " AND T1.GRADE || T1.HR_CLASS NOT IN (SELECT ";
            $query .= "                                         NAMECD2 || NAME1 AS HR_CLASS ";
            $query .= "                                     FROM ";
            $query .= "                                         V_NAME_MST ";
            $query .= "                                     WHERE ";
            $query .= "                                         NAMECD1 = 'A032' ";
            $query .= "                                     AND YEAR = '{$model->year}') ";
            if (AUTHORITY == DEF_REFER_RESTRICT || AUTHORITY == DEF_UPDATE_RESTRICT) {
                $query .= " AND (   T1.TR_CD1       = '".STAFFCD."' ";
                $query .= "      OR T1.TR_CD2       = '".STAFFCD."' ";
                $query .= "      OR T1.TR_CD3       = '".STAFFCD."' ";
                $query .= "      OR T1.SUBTR_CD1    = '".STAFFCD."' ";
                $query .= "      OR T1.SUBTR_CD2    = '".STAFFCD."' ";
                $query .= "      OR T1.SUBTR_CD3    = '".STAFFCD."') ";
            }
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //クラスコンボ
    public function getHrClass2($model)
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     T1.GRADE || '-' || T1.HR_CLASS AS VALUE, ";
        $query .= "     L1.HR_NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_GHR_DAT T0 ";
        $query .= "     INNER JOIN SCHREG_REGD_GHR_HDAT L0 ON L0.YEAR       = T0.YEAR  ";
        $query .= "                                       AND L0.SEMESTER   = T0.SEMESTER ";
        $query .= "                                       AND L0.GHR_CD     = T0.GHR_CD, ";
        $query .= "     SCHREG_REGD_DAT T1 ";
        $query .= "     INNER JOIN SCHREG_REGD_HDAT L1 ON L1.YEAR       = T1.YEAR  ";
        $query .= "                                   AND L1.SEMESTER   = T1.SEMESTER  ";
        $query .= "                                   AND L1.GRADE      = T1.GRADE  ";
        $query .= "                                   AND L1.HR_CLASS   = T1.HR_CLASS ";
        $query .= "     INNER JOIN SCHREG_REGD_GDAT L2 ON L2.YEAR       = T1.YEAR  ";
        $query .= "                                   AND L2.GRADE      = T1.GRADE  ";
        $query .= "                                   AND L2.SCHOOL_KIND  = '{$model->gakubu_school_kind}' ";
        $query .= "     LEFT JOIN SCHREG_BASE_MST B1 ON B1.SCHREGNO = T1.SCHREGNO ";
        $query .= "     LEFT JOIN SCHREG_BASE_YEAR_DETAIL_MST B2 ";
        $query .= "          ON B2.SCHREGNO     = T1.SCHREGNO ";
        $query .= "         AND B2.YEAR         = T1.YEAR ";
        $query .= "         AND B2.BASE_SEQ     = '004' ";
        $query .= " WHERE ";
        $query .= "     T0.YEAR         = T1.YEAR ";
        $query .= " AND T0.YEAR         = '{$model->year}' ";
        $query .= " AND T0.SEMESTER     = T1.SEMESTER ";
        $query .= " AND T0.SEMESTER     = '{$model->regdSemester}' ";
        $query .= " AND T0.GHR_CD       = '{$model->hr_class}' ";
        $query .= " AND T0.SCHREGNO     = T1.SCHREGNO ";
        $query .= " AND T1.GRADE || T1.HR_CLASS NOT IN (SELECT ";
        $query .= "                                         NAMECD2 || NAME1 AS HR_CLASS ";
        $query .= "                                     FROM ";
        $query .= "                                         V_NAME_MST ";
        $query .= "                                     WHERE ";
        $query .= "                                         NAMECD1 = 'A032' ";
        $query .= "                                     AND YEAR    = '{$model->year}') ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //一覧取得
    public function getList($model, $condition = "")
    {
        $query  = " SELECT ";
        if ($condition) {
            $query .= "     COUNT(*) AS CNT ";
        } else {
            $query .= "     T1.GROUPCD ";
            $query .= "     , T1.GROUPNAME ";
            $query .= "     , T2.SCHREGNO ";
            if ($model->hukusiki_radio == "1") {
                $query .= "     , S1.ATTENDNO ";
                $query .= "     , S2.HR_NAME ";
            } else {
                $query .= "     , S5.GHR_ATTENDNO AS ATTENDNO ";
                $query .= "     , S6.GHR_NAME AS HR_NAME ";
            }
        }
        $query .= "     , S3.NAME_SHOW ";
        $query .= " FROM ";
        $query .= "     GRADE_KIND_COMP_GROUP_YMST T1 ";
        $query .= "     INNER JOIN GRADE_KIND_SCHREG_GROUP_DAT T2 ";
        $query .= "         ON T2.YEAR = T1.YEAR ";
        $query .= "         AND T2.SEMESTER = T1.SEMESTER ";
        $query .= "         AND T2.GAKUBU_SCHOOL_KIND = T1.GAKUBU_SCHOOL_KIND ";
        $query .= "         AND T2.GHR_CD = T1.GHR_CD ";
        $query .= "         AND T2.GRADE = T1.GRADE ";
        $query .= "         AND T2.HR_CLASS = T1.HR_CLASS ";
        $query .= "         AND T2.CONDITION = T1.CONDITION ";
        $query .= "         AND T2.GROUPCD = T1.GROUPCD ";
        $query .= "     INNER JOIN SCHREG_REGD_DAT S1 ";
        $query .= "         ON S1.SCHREGNO = T2.SCHREGNO ";
        $query .= "         AND S1.YEAR = T1.YEAR ";
        $query .= "         AND S1.SEMESTER = '{$model->regdSemester}' ";
        $query .= "     INNER JOIN SCHREG_REGD_HDAT S2 ";
        $query .= "         ON S2.YEAR = T1.YEAR ";
        $query .= "         AND S2.SEMESTER = S1.SEMESTER ";
        $query .= "         AND S2.GRADE = S1.GRADE ";
        $query .= "         AND S2.HR_CLASS = S1.HR_CLASS ";
        $query .= "     INNER JOIN SCHREG_BASE_MST S3 ";
        $query .= "         ON S3.SCHREGNO = T2.SCHREGNO ";
        if ($model->hukusiki_radio == "2") {
            $query .= "     INNER JOIN SCHREG_REGD_GHR_DAT S5 ";
            $query .= "         ON S5.SCHREGNO = T2.SCHREGNO ";
            $query .= "         AND S5.YEAR = T1.YEAR ";
            $query .= "         AND S5.SEMESTER = S1.SEMESTER ";
            $query .= "     INNER JOIN SCHREG_REGD_GHR_HDAT S6 ";
            $query .= "         ON S6.YEAR = T1.YEAR ";
            $query .= "         AND S6.SEMESTER = S1.SEMESTER ";
            $query .= "         AND S6.GHR_CD = S5.GHR_CD ";
        }
        $query .= " WHERE ";
        $query .= "     T1.YEAR     = '{$model->year}' ";
        $query .= " AND T1.SEMESTER = '{$model->semester}' ";
        $query .= " AND T1.GAKUBU_SCHOOL_KIND = '{$model->gakubu_school_kind}' ";
        $query .= " AND T1.GHR_CD = '00' ";
        $query .= " AND T1.GRADE = '00' ";
        $query .= " AND T1.HR_CLASS = '000' ";
        if ($model->hukusiki_radio == "1") {
            $query .= " AND S1.GRADE || '-' || S1.HR_CLASS IN ('".implode("','", $model->hr_class_list)."') ";
        } else {
            $query .= " AND S5.GHR_CD IN ('".implode("','", $model->hr_class_list)."') ";
            if ($model->hr_class) {
                $query .= " AND S1.GRADE || '-' || S1.HR_CLASS IN ('".implode("','", $model->hr_class2_list)."') ";
            }
        }
        if ($model->hr_class) {
            if ($model->hukusiki_radio == "1") {
                $query .= " AND S1.GRADE || '-' || S1.HR_CLASS = '{$model->hr_class}' ";
            } else {
                $query .= " AND S5.GHR_CD = '{$model->hr_class}' ";
                if ($model->hr_class2) {
                    $query .= " AND S1.GRADE || '-' || S1.HR_CLASS = '{$model->hr_class2}' ";
                }
            }
        }
        if ($condition) {
            $query .= " AND T1.CONDITION = '{$condition}' ";
        }
        if ($condition == "") {
            $query .= " ORDER BY ";
            if ($model->list_sort == "groupcd") {
                $query .= "     T1.GROUPCD, ";
            }
            if ($model->hukusiki_radio == "1") {
                $query .= "     S1.GRADE, ";
                $query .= "     S1.HR_CLASS, ";
                $query .= "     S1.ATTENDNO ";
            } else {
                $query .= "     S6.GHR_CD ";
                $query .= "     , S5.GHR_ATTENDNO ";
            }
        }

        return $query;
    }


    /****************/
    /*    右画面    */
    /****************/

    //生徒一覧取得
    public function getSchList($model)
    {
        $query  = " SELECT ";
        if ($model->hukusiki_radio == "1") {
            $query .= "     T2.HR_NAME || '　' || T1.ATTENDNO || '　' || T4.NAME_SHOW AS LABEL ";
            $query .= "     , T1.SCHREGNO AS VALUE ";
        } else {
            $query .= "     T6.GHR_NAME || '　' || T5.GHR_ATTENDNO || '　' || T4.NAME_SHOW AS LABEL ";
            $query .= "     , T1.SCHREGNO AS VALUE ";
        }
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1 ";
        $query .= "     INNER JOIN SCHREG_REGD_HDAT T2 ";
        $query .= "         ON T2.YEAR = T1.YEAR ";
        $query .= "         AND T2.SEMESTER = T1.SEMESTER ";
        $query .= "         AND T2.GRADE = T1.GRADE ";
        $query .= "         AND T2.HR_CLASS = T1.HR_CLASS ";
        $query .= "     INNER JOIN SCHREG_REGD_GDAT T3 ";
        $query .= "         ON T3.YEAR = T1.YEAR ";
        $query .= "         AND T3.GRADE = T1.GRADE ";
        $query .= "         AND T3.SCHOOL_KIND = '{$model->gakubu_school_kind}' ";
        $query .= "     INNER JOIN SCHREG_BASE_MST T4 ";
        $query .= "         ON T4.SCHREGNO = T1.SCHREGNO ";
        if ($model->hukusiki_radio == "2") {
            $query .= "     INNER JOIN SCHREG_REGD_GHR_DAT T5 ";
            $query .= "         ON T5.SCHREGNO = T1.SCHREGNO ";
            $query .= "         AND T5.YEAR = T1.YEAR ";
            $query .= "         AND T5.SEMESTER = T1.SEMESTER ";
            $query .= "     INNER JOIN SCHREG_REGD_GHR_HDAT T6 ";
            $query .= "         ON T6.YEAR = T1.YEAR ";
            $query .= "         AND T6.SEMESTER = T1.SEMESTER ";
            $query .= "         AND T6.GHR_CD = T5.GHR_CD ";
        }
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '{$model->year}' ";
        $query .= "     AND T1.SEMESTER = '{$model->regdSemester}' ";
        if ($model->hukusiki_radio == "1") {
            $query .= " AND T1.GRADE || '-' || T1.HR_CLASS IN ('".implode("','", $model->hr_class_list)."') ";
        } else {
            $query .= " AND T5.GHR_CD IN ('".implode("','", $model->hr_class_list)."') ";
            if ($model->hr_class) {
                $query .= " AND T1.GRADE || '-' || T1.HR_CLASS IN ('".implode("','", $model->hr_class2_list)."') ";
            }
        }
        if ($model->hr_class) {
            if ($model->hukusiki_radio == "1") {
                $query .= " AND T1.GRADE || '-' || T1.HR_CLASS = '{$model->hr_class}' ";
            } else {
                $query .= " AND T5.GHR_CD = '{$model->hr_class}' ";
                if ($model->hr_class2) {
                    $query .= " AND T1.GRADE || '-' || T1.HR_CLASS = '{$model->hr_class2}' ";
                }
            }
        }
        $query .= "     AND NOT EXISTS ( ";
        $query .= "         SELECT ";
        $query .= "             'X' ";
        $query .= "         FROM ";
        $query .= "             GRADE_KIND_SCHREG_GROUP_DAT T7 ";
        $query .= "         WHERE ";
        $query .= "             T7.YEAR = T1.YEAR ";
        $query .= "             AND T7.SEMESTER = '{$model->semester}' ";
        $query .= "             AND T7.GAKUBU_SCHOOL_KIND = T3.SCHOOL_KIND ";
        $query .= "             AND T7.SCHREGNO = T1.SCHREGNO ";
        $query .= "             AND T7.GHR_CD = '00' ";
        $query .= "             AND T7.GRADE = '00' ";
        $query .= "             AND T7.HR_CLASS = '000' ";
        $query .= "     ) ";
        $query .= " ORDER BY ";
        if ($model->hukusiki_radio == "1") {
            $query .= "     T1.GRADE ";
            $query .= "     , T1.HR_CLASS ";
            $query .= "     , T1.ATTENDNO ";
        } else {
            $query .= "     T5.GHR_CD ";
            $query .= "     , T5.GHR_ATTENDNO ";
        }

        return $query;
    }

    //科目グループコンボ取得
    public function getGroupCombo($model)
    {
        $query  = " SELECT ";
        $query .= "     GROUPCD || '　' || GROUPNAME AS LABEL ";
        $query .= "     , CONDITION || '-' || GROUPCD AS VALUE ";
        $query .= " FROM ";
        $query .= "     GRADE_KIND_COMP_GROUP_YMST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$model->year}' ";
        $query .= "     AND SEMESTER = '{$model->semester}' ";
        $query .= "     AND GAKUBU_SCHOOL_KIND = '{$model->gakubu_school_kind}' ";
        $query .= "     AND GHR_CD = '00' ";
        $query .= "     AND GRADE = '00' ";
        $query .= "     AND HR_CLASS = '000' ";
        $query .= " ORDER BY ";
        $query .= "     CONDITION ";
        $query .= "     , GROUPCD ";

        return $query;
    }

    //削除処理
    public function &getDeleteQuery($model)
    {
        $db = Query::dbCheckOut();

        //対象テーブル
        if (get_count($model->checked) > 0) {
            foreach ($model->checked as $schregno) {
                $query  = " DELETE FROM";
                $query .= "     GRADE_KIND_SCHREG_GROUP_DAT ";
                $query .= " WHERE ";
                $query .= "     YEAR = '{$model->year}' ";
                $query .= "     AND SEMESTER = '{$model->semester}' ";
                $query .= "     AND GAKUBU_SCHOOL_KIND = '{$model->gakubu_school_kind}' ";
                $query .= "     AND GHR_CD = '00' ";
                $query .= "     AND GRADE = '00' ";
                $query .= "     AND HR_CLASS = '000' ";
                $query .= "     AND SCHREGNO = '{$schregno}' ";

                $db->query($query);
            }
        }

        Query::dbCheckIn($db);

        return true;
    }

    //更新処理
    public function &getUpdateQuery($model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        list($condition, $groupcd) = explode('-', $model->field["CONDITION_GROUPCD"]);

        $query  = " INSERT INTO GRADE_KIND_SCHREG_GROUP_DAT ( ";
        $query .= "     YEAR, ";
        $query .= "     SEMESTER, ";
        $query .= "     GAKUBU_SCHOOL_KIND, ";
        $query .= "     GHR_CD, ";
        $query .= "     GRADE, ";
        $query .= "     HR_CLASS, ";
        $query .= "     CONDITION, ";
        $query .= "     GROUPCD, ";
        $query .= "     SCHREGNO, ";
        $query .= "     REGISTERCD, ";
        $query .= "     UPDATED ";
        $query .= " ) ";
        $query .= "   (SELECT ";
        $query .= "     YEAR, ";
        $query .= "     '{$model->semester}', ";
        $query .= "     '{$model->gakubu_school_kind}', ";
        $query .= "     '00', ";
        $query .= "     '00', ";
        $query .= "     '000', ";
        $query .= "     '{$condition}', ";
        $query .= "     '{$groupcd}', ";
        $query .= "     SCHREGNO, ";
        $query .= "     '".STAFFCD."', ";
        $query .= "     SYSDATE() ";
        $query .= "   FROM ";
        $query .= "     SCHREG_REGD_DAT ";
        $query .= "   WHERE ";
        $query .= "     SCHREGNO IN ('" .str_replace(",", "','", $model->selectdata). "') ";
        $query .= "     AND YEAR = '{$model->year}' ";
        $query .= "     AND SEMESTER = '{$model->regdSemester}' ";
        $query .= "   )  ";

        $db->query($query);

        $db->commit();      // トランザクションをコミットする。
        Query::dbCheckIn($db);

        return true;
    }
}
