<?php

require_once('for_php7.php');

class knjb1216query extends Query {

    //履修履歴
    function getRirekiCode($model)
    {
        $query  = " SELECT ";
        $query .= "     RIREKI_CODE AS VALUE, ";
        $query .= "     SELECT_NAME || ' ' || CHAR(REPLACE(CHAR(SELECT_DATE), '-', '/')) AS LABEL ";
        $query .= " FROM ";
        $query .= "     STUDY_SELECT_DATE_YMST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$model->year}' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //生徒情報取得
    function getStudentInfoData($model) {

        if ($model->search_div == "1") {
            $query  = " SELECT ";
            $query .= "     T1.SCHREGNO, ";
            $query .= "     T1.ENTERYEAR AS YEAR, ";
            $query .= "     T1.ENTERYEAR AS CURRICULUM_YEAR, ";
            $query .= "     T1.COURSECD, ";
            $query .= "     T1.MAJORCD, ";
            $query .= "     T1.COURSECODE, ";
            $query .= "     T2.COURSENAME, ";
            $query .= "     T3.MAJORNAME ";
            $query .= " FROM ";
            $query .= "     FRESHMAN_DAT T1 ";
            $query .= "     LEFT JOIN COURSE_MST T2 ON T1.COURSECD  = T2.COURSECD ";
            $query .= "     LEFT JOIN MAJOR_MST  T3 ON T1.COURSECD  = T3.COURSECD ";
            $query .= "                            AND T1.MAJORCD   = T3.MAJORCD ";
            $query .= " WHERE ";
            $query .= "     T1.ENTERYEAR    = '".(CTRL_YEAR+1)."' AND ";
            $query .= "     T1.SCHREGNO     = '".$model->schregno."' ";
        } else {
            $query  = " SELECT ";
            $query .= "     T1.SCHREGNO, ";
            $query .= "     T1.YEAR, ";
            $query .= "     T1.ANNUAL, ";
            $query .= "     T2.CURRICULUM_YEAR, ";
            $query .= "     T1.COURSECD, ";
            $query .= "     T1.MAJORCD, ";
            $query .= "     T1.COURSECODE, ";
            $query .= "     T3.COURSENAME, ";
            $query .= "     T4.MAJORNAME, ";
            $query .= "     T6.STAFFNAME ";
            $query .= " FROM ";
            $query .= "     SCHREG_REGD_DAT T1 ";
            $query .= "     LEFT JOIN SCHREG_ENT_GRD_HIST_DAT T2 ON T1.SCHREGNO     = T2.SCHREGNO ";
            $query .= "                                         AND T2.SCHOOL_KIND  = 'H' ";
            $query .= "     LEFT JOIN COURSE_MST T3 ON T1.COURSECD  = T3.COURSECD ";
            $query .= "     LEFT JOIN MAJOR_MST  T4 ON T1.COURSECD  = T4.COURSECD ";
            $query .= "                            AND T1.MAJORCD   = T4.MAJORCD ";
            $query .= "     LEFT JOIN SCHREG_REGD_HDAT T5 ON T1.YEAR        = T5.YEAR ";
            $query .= "                                  AND T1.SEMESTER    = T5.SEMESTER ";
            $query .= "                                  AND T1.GRADE       = T5.GRADE ";
            $query .= "                                  AND T1.HR_CLASS    = T5.HR_CLASS ";
            $query .= "     LEFT JOIN STAFF_MST T6 ON T5.TR_CD1 = T6.STAFFCD ";
            $query .= " WHERE ";
            $query .= "     T1.YEAR     = '".CTRL_YEAR."' AND ";
            $query .= "     T1.SEMESTER = '".CTRL_SEMESTER."' AND ";
            $query .= "     T1.SCHREGNO = '".$model->schregno."' ";
        }

        return $query;
    }

    //明細一覧取得
    function getMeisaiQuery($model) {

        $query  = " WITH CHAIR_INFO AS ( ";
        $query .= "     SELECT ";
        $query .= "         YEAR, ";
        $query .= "         SCHREGNO, ";
        $query .= "         CLASSCD, ";
        $query .= "         SCHOOL_KIND, ";
        $query .= "         CURRICULUM_CD, ";
        $query .= "         SUBCLASSCD ";
        $query .= "     FROM ";
        $query .= "         SUBCLASS_STD_SELECT_RIREKI_DAT ";
        $query .= "     WHERE ";
        $query .= "         YEAR     = '".$model->year."' AND ";
        if ($model->year == CTRL_YEAR) {
            $query .= "         SEMESTER = '".CTRL_SEMESTER."' AND ";
        } else {
            $query .= "         SEMESTER = '1' AND ";
        }
        $query .= "         RIREKI_CODE = '{$model->rirekiCode}' AND ";
        $query .= "         SCHREGNO = '".$model->schregno."' AND ";
        $query .= "         CLASSCD  < '90' ";
        $query .= " ) ";

        $query .= " SELECT DISTINCT ";
        $query .= "     T1.CLASSCD, ";
        $query .= "     T1.SCHOOL_KIND, ";
        $query .= "     T1.CURRICULUM_CD, ";
        $query .= "     T1.SUBCLASSCD, ";
        $query .= "     L1.SUBCLASSNAME, ";
        $query .= "     L2.TEXTBOOKCD, ";
        $query .= "     L3.TEXTBOOKNAME, ";
        $query .= "     L4.ISSUECOMPANYNAME, ";
        $query .= "     L3.TEXTBOOKUNITPRICE, ";
        $query .= "     L2.NOT_DEFAULT, ";
        $query .= "     L5.NAME1 AS DIV_NAME, ";
        $query .= "     L5.NAMESPARE1 AS DIV ";
        $query .= " FROM ";
        $query .= "     CHAIR_INFO T1 ";
        $query .= "     LEFT JOIN SUBCLASS_MST L1 ON T1.CLASSCD         = L1.CLASSCD ";
        $query .= "                              AND T1.SCHOOL_KIND     = L1.SCHOOL_KIND ";
        $query .= "                              AND T1.CURRICULUM_CD   = L1.CURRICULUM_CD ";
        $query .= "                              AND T1.SUBCLASSCD      = L1.SUBCLASSCD ";
        $query .= "     LEFT JOIN SUBCLASS_TEXTBOOK_COURSE_DAT L2 ON T1.YEAR           = L2.YEAR ";
        $query .= "                                       AND T1.CLASSCD        = L2.CLASSCD ";
        $query .= "                                       AND T1.SCHOOL_KIND    = L2.SCHOOL_KIND ";
        $query .= "                                       AND T1.CURRICULUM_CD  = L2.CURRICULUM_CD ";
        $query .= "                                       AND T1.SUBCLASSCD     = L2.SUBCLASSCD ";
        $query .= "                                       AND L2.COURSECD       = '{$model->cmcArray["COURSECD"]}' ";
        $query .= "                                       AND L2.MAJORCD        = '{$model->cmcArray["MAJORCD"]}' ";
        $query .= "                                       AND L2.COURSECODE     = '{$model->cmcArray["COURSECODE"]}' ";
        $query .= "     LEFT JOIN TEXTBOOK_MST L3 ON L2.TEXTBOOKCD = L3.TEXTBOOKCD ";
        $query .= "     LEFT JOIN ISSUECOMPANY_MST L4 ON L3.ISSUECOMPANYCD = L4.ISSUECOMPANYCD ";
        $query .= "     LEFT JOIN V_NAME_MST L5 ON T1.YEAR = L5.YEAR ";
        $query .= "                            AND L5.NAMECD1 = 'M004' ";
        $query .= "                            AND L3.TEXTBOOKDIV = L5.NAMECD2 ";
        $query .= " ORDER BY ";
        $query .= "     T1.CLASSCD, ";
        $query .= "     T1.SCHOOL_KIND, ";
        $query .= "     T1.CURRICULUM_CD, ";
        $query .= "     T1.SUBCLASSCD, ";
        $query .= "     L2.TEXTBOOKCD ";

        return $query;
    }

    //学籍教科書購入データの件数取得
    function getSchregTextbookSubclassDatCnt($model, $row, $year_div="", $sonzai="") {

        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     SCHREG_TEXTBOOK_SUBCLASS_DAT ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO        = '".$model->schregno."' ";
        if ($sonzai) {
            $query .= "     AND YEAR        = '".$model->year."' ";
        } else {
            $query .= "     AND ";
            if ($year_div == "this") {
                $query .= "     YEAR            = '".$model->year."' AND ";
                $query .= "     CLASSCD         = '".$row["CLASSCD"]."' AND ";
                $query .= "     SCHOOL_KIND     = '".$row["SCHOOL_KIND"]."' AND ";
                $query .= "     CURRICULUM_CD   = '".$row["CURRICULUM_CD"]."' AND ";
                $query .= "     SUBCLASSCD      = '".$row["SUBCLASSCD"]."' AND ";
            } else if ($year_div == "past") {
                $query .= "     YEAR        < '".$model->year."' AND ";
            }
            $query .= "     TEXTBOOKCD      = '".$row["TEXTBOOKCD"]."' ";
        }

        return $query;
    }

    //教科書発注済み
    function getSchregTextbookFinDat($model, $row) {

        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     SCHREG_TEXTBOOK_CHKFIN_DAT ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO = '".$model->schregno."' ";
        $query .= "     AND YEAR = '".$model->year."' ";
        $query .= "     AND VALUE(EXECUTED, '0') = '1' ";

        return $query;
    }

    //科目別教科書データの件数取得
    function getSubclassTextbookDatCnt($model, $row, $flg="") {

        $query  = " WITH CHAIR_INFO AS ( ";
        if ($model->year == CTRL_YEAR) {
            $query .= "     SELECT ";
            $query .= "         T1.YEAR, ";
            $query .= "         T1.SCHREGNO, ";
            $query .= "         T2.CLASSCD, ";
            $query .= "         T2.SCHOOL_KIND, ";
            $query .= "         T2.CURRICULUM_CD, ";
            $query .= "         T2.SUBCLASSCD ";
            $query .= "     FROM ";
            $query .= "         CHAIR_STD_DAT T1, ";
            $query .= "         CHAIR_DAT T2 ";
            $query .= "     WHERE ";
            $query .= "         T1.YEAR     = T2.YEAR AND ";
            $query .= "         T1.YEAR     = '".$model->year."' AND ";
            $query .= "         T1.SEMESTER = T2.SEMESTER AND ";
            $query .= "         T1.SEMESTER = '".CTRL_SEMESTER."' AND ";
            $query .= "         T1.SCHREGNO = '".$model->schregno."' AND ";
            $query .= "         T1.CHAIRCD  = T2.CHAIRCD AND ";
            $query .= "         T2.CLASSCD  < '90' ";
        } else {
            $query .= "     SELECT ";
            $query .= "         YEAR, ";
            $query .= "         SCHREGNO, ";
            $query .= "         CLASSCD, ";
            $query .= "         SCHOOL_KIND, ";
            $query .= "         CURRICULUM_CD, ";
            $query .= "         SUBCLASSCD ";
            $query .= "     FROM ";
            $query .= "         SUBCLASS_STD_SELECT_RIREKI_DAT ";
            $query .= "     WHERE ";
            $query .= "         YEAR     = '".$model->year."' AND ";
            $query .= "         SEMESTER = '".CTRL_SEMESTER."' AND ";
            $query .= "         RIREKI_CODE = '{$model->rirekiCode}' AND ";
            $query .= "         SCHREGNO = '".$model->schregno."' AND ";
            $query .= "         CLASSCD  < '90' ";
        }
        $query .= " ) ";

        $query .= " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     CHAIR_INFO T1, ";
        $query .= "     SUBCLASS_TEXTBOOK_COURSE_DAT T2 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR                 = T2.YEAR ";
        $query .= "     AND T1.CLASSCD          = T2.CLASSCD ";
        $query .= "     AND T1.SCHOOL_KIND      = T2.SCHOOL_KIND ";
        $query .= "     AND T1.CURRICULUM_CD    = T2.CURRICULUM_CD ";
        $query .= "     AND T1.SUBCLASSCD       = T2.SUBCLASSCD ";
        $query .= "     AND T2.TEXTBOOKCD       = '".$row["TEXTBOOKCD"]."' ";
        $query .= "     AND T1.CLASSCD || T1.SCHOOL_KIND || T1.CURRICULUM_CD || T1.SUBCLASSCD != '".$row["CLASSCD"].$row["SCHOOL_KIND"].$row["CURRICULUM_CD"].$row["SUBCLASSCD"]."' ";
        $query .= "     AND T2.COURSECD         = '{$model->cmcArray["COURSECD"]}' ";
        $query .= "     AND T2.MAJORCD          = '{$model->cmcArray["MAJORCD"]}' ";
        $query .= "     AND T2.COURSECODE       = '{$model->cmcArray["COURSECODE"]}' ";
        if ($flg) {
            $query .= "     AND EXISTS (SELECT ";
            $query .= "                 'X' ";
            $query .= "             FROM ";
            $query .= "                 SCHREG_TEXTBOOK_SUBCLASS_DAT S1 ";
            $query .= "             WHERE ";
            $query .= "                 T1.SCHREGNO         = S1.SCHREGNO AND ";
            $query .= "                 T1.YEAR             = S1.YEAR AND ";
            $query .= "                 T1.CLASSCD          = S1.CLASSCD AND ";
            $query .= "                 T1.SCHOOL_KIND      = S1.SCHOOL_KIND AND ";
            $query .= "                 T1.CURRICULUM_CD    = S1.CURRICULUM_CD AND ";
            $query .= "                 T1.SUBCLASSCD       = S1.SUBCLASSCD AND ";
            $query .= "                 T2.TEXTBOOKCD       = S1.TEXTBOOKCD ";
            $query .= "             ) ";
        }

        return $query;
    }

    //SCHREG_TEXTBOOK_SUBCLASS_DAT更新
    function &getUpdateQuery($db, $model) {

        $array = (is_array($model->checked)) ? explode(",", implode(',',$model->checked)) : "";

        //削除
        $query  = " DELETE FROM ";
        $query .= "     SCHREG_TEXTBOOK_SUBCLASS_DAT ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO = '".$model->schregno."' AND ";
        $query .= "     YEAR     = '".$model->year."' ";

        $db->query($query);
        $db->commit();

        //追加
        if ($array) {
            for ($i = 0; $i < get_count($array); $i++){
                list($classcd, $school_kind, $curriculum_cd, $subclasscd, $textbookcd) = explode('-', $array[$i]);

                $data["SCHREGNO"][TEXT]         = $model->schregno;
                $data["YEAR"][TEXT]             = $model->year;
                $data["CLASSCD"][TEXT]          = $classcd;
                $data["SCHOOL_KIND"][TEXT]      = $school_kind;
                $data["CURRICULUM_CD"][TEXT]    = $curriculum_cd;
                $data["SUBCLASSCD"][TEXT]       = $subclasscd;
                $data["TEXTBOOKCD"][TEXT]       = $textbookcd;

                $data["REGISTERCD"][TEXT]       = STAFFCD;
                $data["UPDATED"][FUNC]          = "sysdate()";

                $query = Query::insertSQL($data, "SCHREG_TEXTBOOK_SUBCLASS_DAT");
                $db->query($query);
            }
            $db->commit();
        }

        return;
    }

    //SCHREG_TEXTBOOK_SUBCLASS_DAT更新
    function &updateTextChkFin($db, $model) {

        //削除
        $query  = " DELETE FROM ";
        $query .= "     SCHREG_TEXTBOOK_CHKFIN_DAT ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO = '".$model->schregno."' AND ";
        $query .= "     YEAR     = '".$model->year."' ";

        $db->query($query);
        $db->commit();

        $data["SCHREGNO"][TEXT]         = $model->schregno;
        $data["YEAR"][TEXT]             = $model->year;
        $data["EXECUTED"][TEXT]         = "1";

        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][FUNC]          = "sysdate()";

        $query = Query::insertSQL($data, "SCHREG_TEXTBOOK_CHKFIN_DAT");
        $db->query($query);

        $db->commit();

        return;
    }

    //学籍番号取得
    function getSchregNo($model) {
        $query  = " SELECT ";
        if ($this->cmd == "updateNext") {
            $query .= "     MIN(SCHREGNO) AS SCHREGNO ";
        } else if ($this->cmd == "updatePrev") {
            $query .= "     MAX(SCHREGNO) AS SCHREGNO ";
        } else {
            $query .= "     SCHREGNO ";
        }
        $query .= " FROM ";
        $query .= "     SCHREG_BASE_MST ";
        $query .= " WHERE ";
        if ($this->cmd == "updateNext") {
            $query .= "     SCHREGNO > '{$model->schregno}' ";
        } else if ($this->cmd == "updatePrev") {
            $query .= "     SCHREGNO < '{$model->schregno}' ";
        } else {
            $query .= "     SCHREGNO = '{$model->schregno}' ";
        }

        return $query;
    }
}
?>
