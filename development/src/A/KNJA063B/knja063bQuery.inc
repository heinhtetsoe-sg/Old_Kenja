<?php

require_once('for_php7.php');

class knja063bQuery extends Query {

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
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //左クラスコンボ
    function getGhrCd($model) {
        list($grade, $hrClass) = preg_split("/-/", $model->ghr_cd);

        $query  = " SELECT ";
        $query .= "     LCH.LC_NAME AS LABEL, ";
        $query .= "     LCH.GRADE || '-' || LC_CLASS AS VALUE ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_LC_HDAT LCH ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= "INNER JOIN SCHREG_REGD_GDAT GDAT ON GDAT.YEAR = LCH.YEAR AND GDAT.GRADE = LCH.GRADE ";
                $query .= " AND GDAT.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
            }
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= "INNER JOIN SCHREG_REGD_GDAT GDAT ON GDAT.YEAR = LCH.YEAR AND GDAT.GRADE = LCH.GRADE ";
            $query .= " AND GDAT.SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= " WHERE ";
        $query .= "     LCH.YEAR = '{$model->year}' ";
        $query .= "     AND LCH.SEMESTER = '{$model->semester}' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";
        return $query;
    }

    //右クラスコンボ
    function getHrClass($model) {
        $query  = " SELECT ";
        $query .= "     HR_NAME AS LABEL, ";
        $query .= "     GRADE || '-' || HR_CLASS AS VALUE ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_HDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$model->year}' ";
        $query .= "     AND SEMESTER = '{$model->semester}' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";
        return $query;
    }

    //左生徒リスト
    function getLcStudents($model, $flg="") {
        $query  = " SELECT ";
        if ($flg == "COUNT") {
            $query .= "     COUNT(*) AS CNT ";
        } else {
            $query .= "     LCD.SCHREGNO, ";
            $query .= "     LCD.GRADE, ";
            $query .= "     LCD.LC_CLASS, ";
            $query .= "     LCD.ATTENDNO, ";
            $query .= "     LCH.LC_NAME, ";
            $query .= "     REGDH.HR_NAME AS HOUTEI_HR_NAME, ";
            $query .= "     REGD.GRADE AS HOUTEI_GRADE, ";
            $query .= "     REGD.HR_CLASS AS HOUTEI_HR_CLASS, ";
            $query .= "     REGD.ATTENDNO AS HOUTEI_ATTENDNO, ";
            $query .= "     BASE.NAME ";
        }
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_LC_DAT LCD ";
        $query .= "     INNER JOIN SCHREG_REGD_DAT REGD ON REGD.YEAR = LCD.YEAR AND REGD.SEMESTER = LCD.SEMESTER AND REGD.SCHREGNO = LCD.SCHREGNO ";
        $query .= "     INNER JOIN SCHREG_REGD_HDAT REGDH ON REGDH.YEAR = LCD.YEAR AND REGDH.SEMESTER = LCD.SEMESTER AND REGDH.GRADE = REGD.GRADE AND REGDH.HR_CLASS = REGD.HR_CLASS ";
        $query .= "     INNER JOIN SCHREG_BASE_MST BASE ON BASE.SCHREGNO = LCD.SCHREGNO ";
        $query .= "     INNER JOIN SCHREG_REGD_LC_HDAT LCH ON LCH.YEAR = LCD.YEAR ";
        $query .= "           AND LCH.SEMESTER = LCD.SEMESTER ";
        $query .= "           AND LCH.GRADE = LCD.GRADE ";
        $query .= "           AND LCH.LC_CLASS = LCD.LC_CLASS ";
        $query .= " WHERE ";
        $query .= "     LCD.YEAR = '{$model->year}' ";
        $query .= "     AND LCD.SEMESTER = '{$model->semester}' ";
        $query .= "     AND LCD.GRADE || '-' || LCD.LC_CLASS = '{$model->ghr_cd}' ";
        if ($flg != "COUNT") {
            $query .= " ORDER BY ";
            $query .= "     LCD.GRADE, ";
            $query .= "     LCD.LC_CLASS, ";
            $query .= "     LCD.ATTENDNO, ";
            $query .= "     HOUTEI_GRADE, ";
            $query .= "     HOUTEI_HR_CLASS, ";
            $query .= "     HOUTEI_ATTENDNO ";
        }

        return $query;
    }

    //右生徒リスト
    function getHrStudents($model) {
        $query  = " SELECT ";
        $query .= "     REGD.SCHREGNO, ";
        $query .= "     REGD.GRADE, ";
        $query .= "     REGD.HR_CLASS, ";
        $query .= "     REGD.ATTENDNO, ";
        $query .= "     REGDH.HR_NAME, ";
        $query .= "     BASE.NAME ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT REGD ";
        $query .= "     INNER JOIN SCHREG_REGD_HDAT REGDH ON REGDH.YEAR = REGD.YEAR AND REGDH.SEMESTER = REGD.SEMESTER AND REGDH.GRADE = REGD.GRADE AND REGDH.HR_CLASS = REGD.HR_CLASS ";
        $query .= "     INNER JOIN SCHREG_BASE_MST BASE ON BASE.SCHREGNO = REGD.SCHREGNO ";
        $query .= " WHERE ";
        $query .= "     REGD.YEAR = '{$model->year}' ";
        $query .= "     AND REGD.SEMESTER = '{$model->semester}' ";
        $query .= "     AND REGD.GRADE || '-' || REGD.HR_CLASS = '{$model->hr_class}' ";
        $query .= "     AND REGD.SCHREGNO NOT IN ('" .str_replace("," ,"','" ,$model->selectdata). "') ";
        $query .= "     AND REGD.SCHREGNO NOT IN ( ";
        $query .= "         SELECT ";
        $query .= "             LCD.SCHREGNO ";
        $query .= "         FROM ";
        $query .= "             SCHREG_REGD_LC_DAT LCD ";
        $query .= "         WHERE ";
        $query .= "             LCD.YEAR = '{$model->year}' ";
        $query .= "             AND LCD.SEMESTER = '{$model->semester}' ";
        $query .= "     ) ";
        $query .= " ORDER BY ";
        $query .= "     REGD.GRADE, ";
        $query .= "     REGD.HR_CLASS, ";
        $query .= "     REGD.ATTENDNO ";
        return $query;
    }

    function &getUpdateQuery($model) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $query  = " DELETE FROM ";
        $query .= "     SCHREG_REGD_LC_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$model->year}' ";
        $query .= "     AND SEMESTER = '{$model->semester}' ";
        $query .= "     AND GRADE || '-' || LC_CLASS = '{$model->ghr_cd}' ";
        $query .= "     AND SCHREGNO NOT IN ('" .str_replace("," ,"','" ,$model->selectdata). "') ";
        $db->query($query);

        list($grade, $hrClass) = preg_split("/-/", $model->ghr_cd);

        //③左のリストの生徒のうち、未登録生徒を追加する
        //（つまり、右から左へ移動した生徒を追加する）
        $query  = " INSERT INTO SCHREG_REGD_LC_DAT ( ";
        $query .= " SCHREGNO, ";
        $query .= " YEAR, ";
        $query .= " SEMESTER, ";
        $query .= " GRADE, ";
        $query .= " LC_CLASS, ";
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
        $query .= "     AND YEAR = '{$model->year}' ";
        $query .= "     AND SEMESTER = '{$model->semester}' ";
        //登録済み生徒は除く
        $query .= "     AND SCHREGNO NOT IN ( ";
        $query .= "         SELECT ";
        $query .= "             SCHREGNO ";
        $query .= "         FROM ";
        $query .= "             SCHREG_REGD_LC_DAT ";
        $query .= "         WHERE ";
        $query .= "             YEAR = '{$model->year}' ";
        $query .= "             AND SEMESTER = '{$model->semester}' ";
        $query .= "             AND GRADE || '-' || LC_CLASS = '{$model->ghr_cd}' ";
        $query .= "     ) ";
        $query .= "   )  ";
        $db->query($query);
        $db->commit();
        Query::dbCheckIn($db);
        return;
    }

}
?>
