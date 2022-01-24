<?php

require_once('for_php7.php');

class knja063aQuery extends Query {

    //年度・学期コンボ
    function getTerm($model) {
        $year = CTRL_YEAR;
        $nextyear = CTRL_YEAR + 1;

        $query  = " SELECT ";
        $query .= "     YEAR || '年度　' || SEMESTERNAME AS LABEL, ";
        $query .= "     YEAR || '-' || SEMESTER AS VALUE ";
        $query .= " FROM ";
        $query .= "     SEMESTER_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$year}' ";
        $query .= "     AND SEMESTER < '9' ";
        //次年度の1学期を追加表示
        if (CTRL_SEMESTER == $model->control["学期数"]) { //最終学期のとき
            $query .= " UNION ALL ";
            $query .= " SELECT ";
            $query .= "     YEAR || '年度　' || SEMESTERNAME AS LABEL, ";
            $query .= "     YEAR || '-' || SEMESTER AS VALUE ";
            $query .= " FROM ";
            $query .= "     SEMESTER_MST ";
            $query .= " WHERE ";
            $query .= "     YEAR = '{$nextyear}' ";
            $query .= "     AND SEMESTER = '1' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //年度・学期取得
    function getYearSemester($model) {
        $query  = " SELECT ";
        $query .= "     YEAR || '-' || SEMESTER AS VALUE, ";
        $query .= "     YEAR || '年度' || SEMESTERNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SEMESTER_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR IN ('".CTRL_YEAR."', '".(CTRL_YEAR+1)."') AND ";
        $query .= "     SEMESTER <> '9' AND ";
        $query .= "     YEAR || SEMESTER >= '".CTRL_YEAR.CTRL_SEMESTER."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";
        $query .= " FETCH FIRST 2 ROWS ONLY ";

        return $query;
    }

    //左クラスコンボ
    function getGhrCd($model, $term, $flg="") {
        list($year, $semester) = explode('-', $term);
        list($grade, $hrClass) = preg_split("/-/", $model->ghr_cd);

        $query  = " SELECT ";
        $query .= "     HR_NAME AS LABEL, ";
        $query .= "     T1.GRADE || '-' || HR_CLASS AS VALUE ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_FI_HDAT T1 ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= "INNER JOIN SCHREG_REGD_GDAT T2 ON T2.YEAR = T1.YEAR AND T2.GRADE = T1.GRADE ";
                $query .= " AND T2.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
            }
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= "INNER JOIN SCHREG_REGD_GDAT T2 ON T2.YEAR = T1.YEAR AND T2.GRADE = T1.GRADE ";
            $query .= " AND T2.SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '{$year}' ";
        $query .= "     AND SEMESTER = '{$semester}' ";
        if ($flg) {
            $query .= "     AND T1.GRADE = '{$grade}' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";
        return $query;
    }

    //右クラスコンボ
    function getHrClass($model) {
        list($grade, $hrClass) = preg_split("/-/", $model->ghr_cd);
        $query  = " SELECT ";
        $query .= "     HR_NAME AS LABEL, ";
        $query .= "     GRADE || '-' || HR_CLASS AS VALUE ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_HDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$model->year}' ";
        $query .= "     AND SEMESTER = '{$model->semester}' ";
        $query .= "     AND GRADE = '{$grade}' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";
        return $query;
    }

    //左生徒リスト
    function getGhrStudents($model, $flg="") {
        $query  = " SELECT ";
        if ($flg == "COUNT") {
            $query .= "     COUNT(*) AS CNT ";
        } else {
            $query .= "     T1.SCHREGNO, ";
            $query .= "     T1.GRADE, ";
            $query .= "     T1.HR_CLASS, ";
            $query .= "     T1.ATTENDNO, ";
            $query .= "     T5.HR_NAME, ";
            $query .= "     T2.HR_NAME AS HOUTEI_HR_NAME, ";
            $query .= "     L1.GRADE AS HOUTEI_GRADE, ";
            $query .= "     L1.HR_CLASS AS HOUTEI_HR_CLASS, ";
            $query .= "     L1.ATTENDNO AS HOUTEI_ATTENDNO, ";
            $query .= "     T3.NAME ";
            if ($flg == "right") {
                $query .= "    ,CASE WHEN L2.SCHREGNO IS NULL THEN 1 ELSE 0 END AS NOT_REGD ";
            }
        }
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_FI_DAT T1 ";
        $query .= "     INNER JOIN SCHREG_REGD_DAT L1 ON L1.YEAR = T1.YEAR AND L1.SEMESTER = T1.SEMESTER AND L1.SCHREGNO = T1.SCHREGNO ";
        $query .= "     INNER JOIN SCHREG_REGD_HDAT T2 ON T2.YEAR = T1.YEAR AND T2.SEMESTER = T1.SEMESTER AND T2.GRADE = L1.GRADE AND T2.HR_CLASS = L1.HR_CLASS ";
        $query .= "     INNER JOIN SCHREG_BASE_MST T3 ON T3.SCHREGNO = T1.SCHREGNO ";
        $query .= "     INNER JOIN SCHREG_REGD_FI_HDAT T5 ON T5.YEAR = T1.YEAR ";
        $query .= "           AND T5.SEMESTER = T1.SEMESTER ";
        $query .= "           AND T5.GRADE = T1.GRADE ";
        $query .= "           AND T5.HR_CLASS = T1.HR_CLASS ";
        if ($flg == "right") {
            $query .= "     LEFT JOIN SCHREG_REGD_DAT L2 ";
            $query .= "              ON L2.YEAR || '-' || L2.SEMESTER = '{$model->yearsem["L"]["value"]}' ";
            $query .= "             AND L2.SCHREGNO = T1.SCHREGNO ";
        }
        $query .= " WHERE ";
        if ($flg == "right") {
            $query .= "     T1.YEAR || '-' || T1.SEMESTER = '{$model->yearsem["R"]["value"]}' ";
            $query .= "     AND T1.GRADE || '-' || T1.HR_CLASS = '{$model->hr_class}' ";
            $query .= "     AND T1.SCHREGNO NOT IN ('" .str_replace("," ,"','" ,$model->selectdata). "') ";
            $query .= "     AND T1.SCHREGNO NOT IN ( ";
            $query .= "         SELECT ";
            $query .= "             SCHREGNO ";
            $query .= "         FROM ";
            $query .= "             SCHREG_REGD_FI_DAT ";
            $query .= "         WHERE ";
            $query .= "             YEAR || '-' || SEMESTER = '{$model->yearsem["L"]["value"]}' ";
            $query .= "     ) ";
        } else {
            if ($model->hr_kirikae == "2") {
                $query .= "     T1.YEAR || '-' || T1.SEMESTER = '{$model->yearsem["L"]["value"]}' ";
            } else {
                $query .= "     T1.YEAR = '{$model->year}' ";
                $query .= "     AND T1.SEMESTER = '{$model->semester}' ";
            }
            $query .= "     AND T1.GRADE || '-' || T1.HR_CLASS = '{$model->ghr_cd}' ";
        }
        if ($flg != "COUNT") {
            $query .= " ORDER BY ";
            $query .= "     T1.GRADE, ";
            $query .= "     T1.HR_CLASS, ";
            $query .= "     T1.ATTENDNO, ";
            $query .= "     HOUTEI_GRADE, ";
            $query .= "     HOUTEI_HR_CLASS, ";
            $query .= "     HOUTEI_ATTENDNO ";
        }
        return $query;
    }

    //右生徒リスト
    function getHrStudents($model) {
        $query  = " SELECT ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     T1.GRADE, ";
        $query .= "     T1.HR_CLASS, ";
        $query .= "     T1.ATTENDNO, ";
        $query .= "     T2.HR_NAME, ";
        $query .= "     T3.NAME ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1 ";
        $query .= "     INNER JOIN SCHREG_REGD_HDAT T2 ON T2.YEAR = T1.YEAR AND T2.SEMESTER = T1.SEMESTER AND T2.GRADE = T1.GRADE AND T2.HR_CLASS = T1.HR_CLASS ";
        $query .= "     INNER JOIN SCHREG_BASE_MST T3 ON T3.SCHREGNO = T1.SCHREGNO ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '{$model->year}' ";
        $query .= "     AND T1.SEMESTER = '{$model->semester}' ";
        $query .= "     AND T1.GRADE || '-' || T1.HR_CLASS = '{$model->hr_class}' ";
        //但し、左側リストにいる人(右側から左側へ移動しただけの生徒も含む)と
        //SCHREG_REGD_GHR_DATに登録されている人(GHR割振り済みの生徒)は除く
        $query .= "     AND T1.SCHREGNO NOT IN ('" .str_replace("," ,"','" ,$model->selectdata). "') ";
        $query .= "     AND T1.SCHREGNO NOT IN ( ";
        $query .= "         SELECT ";
        $query .= "             T4.SCHREGNO ";
        $query .= "         FROM ";
        $query .= "             SCHREG_REGD_FI_DAT T4 ";
        $query .= "         WHERE ";
        $query .= "             T4.YEAR = '{$model->year}' ";
        $query .= "             AND T4.SEMESTER = '{$model->semester}' ";
        $query .= "     ) ";
        $query .= " ORDER BY ";
        $query .= "     T1.GRADE, ";
        $query .= "     T1.HR_CLASS, ";
        $query .= "     T1.ATTENDNO ";
        return $query;
    }

    function &getUpdateQuery($model) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        if ($model->hr_kirikae == "2") {
            list ($year, $semester) = preg_split("/-/", $model->yearsem["L"]["value"]);
        } else {
            $year = $model->year;
            $semester = $model->semester;
        }

        //①左のリストの生徒のうち、既に登録済み生徒は何もしない
        //（つまり、最初から左にいる生徒は何もしない）

        //②既に登録済み生徒のうち、左のリストにいない生徒を削除する
        //（つまり、左から右へ移動した生徒を削除する）
        $query  = " DELETE FROM ";
        $query .= "     SCHREG_REGD_FI_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$year}' ";
        $query .= "     AND SEMESTER = '{$semester}' ";
        $query .= "     AND GRADE || '-' || HR_CLASS = '{$model->ghr_cd}' ";
        $query .= "     AND SCHREGNO NOT IN ('" .str_replace("," ,"','" ,$model->selectdata). "') ";
        $db->query($query);

        list($grade, $hrClass) = preg_split("/-/", $model->ghr_cd);

        //③左のリストの生徒のうち、未登録生徒を追加する
        //（つまり、右から左へ移動した生徒を追加する）
        $query  = " INSERT INTO SCHREG_REGD_FI_DAT ( ";
        $query .= " SCHREGNO, ";
        $query .= " YEAR, ";
        $query .= " SEMESTER, ";
        $query .= " GRADE, ";
        $query .= " HR_CLASS, ";
        $query .= " ANNUAL, ";
        $query .= " SEAT_ROW, ";
        $query .= " SEAT_COL, ";
        $query .= " COURSECD, ";
        $query .= " MAJORCD, ";
        $query .= " COURSECODE, ";
        $query .= " REGISTERCD, ";
        $query .= " UPDATED ";
        $query .= " ) ";
        $query .= "   (SELECT ";
        $query .= "     SCHREGNO, ";
        $query .= "     YEAR, ";
        $query .= "     SEMESTER, ";
        $query .= "     '{$grade}', ";
        $query .= "     '{$hrClass}', ";
        $query .= "     ANNUAL, ";
        $query .= "     SEAT_ROW, ";
        $query .= "     SEAT_COL, ";
        $query .= "     COURSECD, ";
        $query .= "     MAJORCD, ";
        $query .= "     COURSECODE, ";
        $query .= "     '".STAFFCD."', ";
        $query .= "     SYSDATE() ";
        $query .= "   FROM ";
        $query .= "     SCHREG_REGD_DAT ";
        $query .= "   WHERE ";
        $query .= "     SCHREGNO IN ('" .str_replace("," ,"','" ,$model->selectdata). "') ";
        $query .= "     AND YEAR = '{$year}' ";
        $query .= "     AND SEMESTER = '{$semester}' ";
        //登録済み生徒は除く
        $query .= "     AND SCHREGNO NOT IN ( ";
        $query .= "         SELECT ";
        $query .= "             SCHREGNO ";
        $query .= "         FROM ";
        $query .= "             SCHREG_REGD_FI_DAT ";
        $query .= "         WHERE ";
        $query .= "             YEAR = '{$year}' ";
        $query .= "             AND SEMESTER = '{$semester}' ";
        $query .= "             AND GRADE || '-' || HR_CLASS = '{$model->ghr_cd}' ";
        $query .= "     ) ";
        $query .= "   )  ";
        $db->query($query);

        $db->commit();
        Query::dbCheckIn($db);
        return;
    }

    //テーブル存在チェック
    function checkTableExist() {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     SYSIBM.SYSTABLES ";
        $query .= " WHERE ";
        $query .= "     NAME = 'SETTING_DAT' ";

        return $query;
    }

    //生徒項目名取得
    function getSchName($model) {
        $query  = " SELECT DISTINCT ";
        $query .= "     REMARK1, ";
        $query .= "     SCHOOLCD ";
        $query .= " FROM ";
        $query .= "     SETTING_DAT ";
        $query .= " WHERE ";
        $query .= "     SEQ = '001' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= " AND SCHOOLCD    = '".sprintf("%012d", SCHOOLCD)."' ";
                $query .= " AND SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
            }
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND SCHOOLCD    = '".sprintf("%012d", SCHOOLCD)."' ";
            $query .= " AND SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     SCHOOLCD ";

        return $query;
    }
}
?>
