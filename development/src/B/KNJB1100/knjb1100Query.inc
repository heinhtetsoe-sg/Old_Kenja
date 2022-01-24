<?php

require_once('for_php7.php');

class knjb1100Query extends Query {

    //学期取得
    function getSemester($year) {
        $query  = " SELECT ";
        $query .= "     SEMESTER AS VALUE, ";
        $query .= "     SEMESTERNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SEMESTER_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$year}' ";
        $query .= "     AND SEMESTER <> '9' ";
        $query .= " ORDER BY ";
        $query .= "     SEMESTER ";
        return $query;
    }
    //合併先科目一覧：科目合併設定データより取得。単位加算のみ。
    function getList($model, $year) {
        $query  = "";
        $query .= " SELECT DISTINCT ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     T1.COMBINED_CLASSCD || '-' || T1.COMBINED_SCHOOL_KIND || '-' || T1.COMBINED_CURRICULUM_CD || '-' || T1.COMBINED_SUBCLASSCD AS COMBINED_SUBCLASSCD, ";
        } else {
            $query .= "     T1.COMBINED_SUBCLASSCD, ";
        }
        $query .= "     T2.SUBCLASSNAME ";
        $query .= " FROM ";
        $query .= "     SUBCLASS_REPLACE_COMBINED_DAT T1 ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     LEFT JOIN SUBCLASS_MST T2 ON T2.CLASSCD         = T1.COMBINED_CLASSCD ";
            $query .= "                              AND T2.SCHOOL_KIND     = T1.COMBINED_SCHOOL_KIND ";
            $query .= "                              AND T2.CURRICULUM_CD   = T1.COMBINED_CURRICULUM_CD ";
            $query .= "                              AND T2.SUBCLASSCD      = T1.COMBINED_SUBCLASSCD ";
        } else {
            $query .= "     LEFT JOIN SUBCLASS_MST T2 ON T2.SUBCLASSCD=T1.COMBINED_SUBCLASSCD ";
        }
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '{$year}' AND ";
        $query .= "     T1.CALCULATE_CREDIT_FLG = '2' ";//単位加算
        $query .= " ORDER BY ";
        $query .= "     COMBINED_SUBCLASSCD ";
        return $query;
    }
    //講座・群を取得
    function getChairDat($model, $year, $semester, $subclasscd) {
        $query  = "";

        $query .= " WITH CHAIR_STD AS ( ";
        $query .= "     SELECT ";
        $query .= "         CHAIRCD, ";
        $query .= "         COUNT(*) AS CNT ";
        $query .= "     FROM ";
        $query .= "         CHAIR_STD_DAT ";
        $query .= "     WHERE ";
        $query .= "         YEAR = '{$year}' AND ";
        $query .= "         SEMESTER = '{$semester}' ";
        $query .= "     GROUP BY ";
        $query .= "         CHAIRCD ";
        $query .= "     ) ";

        $query .= " SELECT ";
        $query .= "     T1.CHAIRCD, ";
        $query .= "     T1.CHAIRNAME, ";
        $query .= "     T1.GROUPCD, ";
        $query .= "     L1.GROUPNAME, ";
        $query .= "     case when 0 < value(L2.CNT, 0) then '作成済み' else '未作成' end AS CNT ";
        $query .= " FROM ";
        $query .= "     CHAIR_DAT T1 ";
        $query .= "     LEFT JOIN V_ELECTCLASS_MST L1 ON L1.GROUPCD = T1.GROUPCD ";
        $query .= "                                  AND L1.YEAR = T1.YEAR ";
        $query .= "     LEFT JOIN CHAIR_STD L2 ON L2.CHAIRCD = T1.CHAIRCD ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '{$year}' AND ";
        $query .= "     T1.SEMESTER = '{$semester}' AND ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD = '{$subclasscd}' ";
        } else {
            $query .= "     T1.SUBCLASSCD = '{$subclasscd}' ";
        }
        $query .= " ORDER BY ";
        $query .= "     T1.GROUPCD, ";
        $query .= "     T1.CHAIRCD ";
        return $query;
    }
    //名簿作成状況を取得
    function getExistsChairStdDat($year, $semester, $chaircd) {
        $query  = "";
        $query .= " SELECT ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     CHAIR_STD_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$year}' AND ";
        $query .= "     SEMESTER = '{$semester}' AND ";
        $query .= "     CHAIRCD = '{$chaircd}' ";
        return $query;
    }
    //合併元科目の講座名簿の存在チェック
    function getExistsMoto($model, $year, $semester, $subclasscd) {
        $query  = "";
        $query .= " SELECT DISTINCT ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     SUBCLASS_REPLACE_COMBINED_DAT T1, ";
        $query .= "     CHAIR_DAT T2, ";
        $query .= "     CHAIR_STD_DAT T3 ";
        $query .= "     INNER JOIN SCHREG_REGD_DAT L1 ON L1.SCHREGNO = T3.SCHREGNO ";
        $query .= "                                  AND L1.YEAR = T3.YEAR ";
        $query .= "                                  AND L1.SEMESTER = T3.SEMESTER ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '{$year}' AND ";
        $query .= "     T2.YEAR = '{$year}' AND ";
        $query .= "     T2.SEMESTER = '{$semester}' AND ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     T1.COMBINED_CLASSCD || '-' || T1.COMBINED_SCHOOL_KIND || '-' || T1.COMBINED_CURRICULUM_CD || '-' || T1.COMBINED_SUBCLASSCD = '{$subclasscd}' AND ";
            $query .= "     T2.CLASSCD          = T1.ATTEND_CLASSCD AND ";
            $query .= "     T2.SCHOOL_KIND      = T1.ATTEND_SCHOOL_KIND AND ";
            $query .= "     T2.CURRICULUM_CD    = T1.ATTEND_CURRICULUM_CD AND ";
            $query .= "     T2.SUBCLASSCD       = T1.ATTEND_SUBCLASSCD AND ";
        } else {
            $query .= "     T1.COMBINED_SUBCLASSCD = '{$subclasscd}' AND ";
            $query .= "     T2.SUBCLASSCD = T1.ATTEND_SUBCLASSCD AND ";
        }
        $query .= "     T3.YEAR = '{$year}' AND ";
        $query .= "     T3.SEMESTER = '{$semester}' AND ";
        $query .= "     T3.CHAIRCD = T2.CHAIRCD ";
        return $query;
    }
    //名簿開始日・終了日（学期開始日・終了日）取得
    function getAppdate($year, $semester) {
        $query  = " SELECT ";
        $query .= "     SDATE, ";
        $query .= "     EDATE ";
        $query .= " FROM ";
        $query .= "     SEMESTER_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$year}' ";
        $query .= "     AND SEMESTER = '{$semester}' ";
        return $query;
    }
    //合併元科目の名簿（生徒）を取得
    function getUpdateSchno($model, $year, $semester, $subclasscd) {
        $query  = "";
        $query .= " SELECT DISTINCT ";
        $query .= "     T3.SCHREGNO ";
        $query .= " FROM ";
        $query .= "     SUBCLASS_REPLACE_COMBINED_DAT T1, ";
        $query .= "     CHAIR_DAT T2, ";
        $query .= "     CHAIR_STD_DAT T3 ";
        $query .= "     INNER JOIN SCHREG_REGD_DAT L1 ON L1.SCHREGNO = T3.SCHREGNO ";
        $query .= "                                  AND L1.YEAR = T3.YEAR ";
        $query .= "                                  AND L1.SEMESTER = T3.SEMESTER ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '{$year}' AND ";
        $query .= "     T2.YEAR = '{$year}' AND ";
        $query .= "     T2.SEMESTER = '{$semester}' AND ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     T1.COMBINED_CLASSCD || '-' || T1.COMBINED_SCHOOL_KIND || '-' || T1.COMBINED_CURRICULUM_CD || '-' || T1.COMBINED_SUBCLASSCD = '{$subclasscd}' AND ";
            $query .= "     T2.CLASSCD          = T1.ATTEND_CLASSCD AND ";
            $query .= "     T2.SCHOOL_KIND      = T1.ATTEND_SCHOOL_KIND AND ";
            $query .= "     T2.CURRICULUM_CD    = T1.ATTEND_CURRICULUM_CD AND ";
            $query .= "     T2.SUBCLASSCD       = T1.ATTEND_SUBCLASSCD AND ";
        } else {
            $query .= "     T1.COMBINED_SUBCLASSCD = '{$subclasscd}' AND ";
            $query .= "     T2.SUBCLASSCD = T1.ATTEND_SUBCLASSCD AND ";
        }
        $query .= "     T3.YEAR = '{$year}' AND ";
        $query .= "     T3.SEMESTER = '{$semester}' AND ";
        $query .= "     T3.CHAIRCD = T2.CHAIRCD ";
        $query .= " ORDER BY ";
        $query .= "     T3.SCHREGNO ";
        return $query;
    }
    //削除
    function getDeleteChairStdDat($year, $semester, $chaircd) {
        $query  = "";
        $query .= " DELETE FROM CHAIR_STD_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$year}' AND ";
        $query .= "     SEMESTER = '{$semester}' AND ";
        $query .= "     CHAIRCD = '{$chaircd}' ";
        return $query;
    }
    //追加
    function getInsertChairStdDat($year, $semester, $chaircd, $schno, $appdate, $appenddate) {
        $data = array();

        $data["YEAR"][TEXT]         = $year;
        $data["SEMESTER"][TEXT]     = $semester;
        $data["CHAIRCD"][TEXT]      = $chaircd;
        $data["SCHREGNO"][TEXT]     = $schno;
        $data["APPDATE"][DATE]      = $appdate;
        $data["APPENDDATE"][DATE]   = $appenddate;
        $data["REGISTERCD"][TEXT]   = STAFFCD;
        $data["UPDATED"][FUNC]      = "SYSDATE()";

        $query = Query::insertSQL($data, "CHAIR_STD_DAT");
        return $query;
    }
    //生成された名簿より受講クラスを取得
    function getUpdateClass($year, $semester, $chaircd) {
        $query  = "";
        $query .= " SELECT DISTINCT ";
        $query .= "     L1.GRADE, ";
        $query .= "     L1.HR_CLASS ";
        $query .= " FROM ";
        $query .= "     CHAIR_STD_DAT T3 ";
        $query .= "     INNER JOIN SCHREG_REGD_DAT L1 ON L1.SCHREGNO = T3.SCHREGNO ";
        $query .= "                                  AND L1.YEAR = T3.YEAR ";
        $query .= "                                  AND L1.SEMESTER = T3.SEMESTER ";
        $query .= " WHERE ";
        $query .= "     T3.YEAR = '{$year}' AND ";
        $query .= "     T3.SEMESTER = '{$semester}' AND ";
        $query .= "     T3.CHAIRCD = '{$chaircd}' ";
        $query .= " ORDER BY ";
        $query .= "     L1.GRADE, ";
        $query .= "     L1.HR_CLASS ";
        return $query;
    }
    //削除
    function getDeleteChairClsDat($year, $semester, $chaircd, $groupcd) {
        $query  = "";
        $query .= " DELETE FROM CHAIR_CLS_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$year}' AND ";
        $query .= "     SEMESTER = '{$semester}' AND ";
        $query .= "     CHAIRCD = '{$chaircd}' AND ";
        $query .= "     GROUPCD = '{$groupcd}' ";
        return $query;
    }
    //追加
    function getInsertChairClsDat($year, $semester, $chaircd, $groupcd, $trgtgrade, $trgtclass) {
        $data = array();

        $data["YEAR"][TEXT]         = $year;
        $data["SEMESTER"][TEXT]     = $semester;
        $data["CHAIRCD"][TEXT]      = $chaircd;
        $data["GROUPCD"][TEXT]      = $groupcd;
        $data["TRGTGRADE"][TEXT]    = $trgtgrade;
        $data["TRGTCLASS"][TEXT]    = $trgtclass;
        $data["REGISTERCD"][TEXT]   = STAFFCD;
        $data["UPDATED"][FUNC]      = "SYSDATE()";

        $query = Query::insertSQL($data, "CHAIR_CLS_DAT");
        return $query;
    }
}
?>