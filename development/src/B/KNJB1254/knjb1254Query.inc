<?php

require_once('for_php7.php');
class knjb1254Query extends Query
{

    //学期を取得
    public function getSemester()
    {
        $query .= " SELECT ";
        $query .= "     YEAR || ':' || SEMESTER AS VALUE, ";
        $query .= "     YEAR || '年度 ' || SEMESTERNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SEMESTER_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR IN ('".CTRL_YEAR."', '".(CTRL_YEAR + 1)."') ";
        $query .= "     AND SEMESTER <> '9' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE DESC ";

        return $query;
    }

    //履修履歴
    public function getRirekiCode($model)
    {
        $query  = " SELECT ";
        $query .= "     RIREKI_CODE AS VALUE, ";
        $query .= "     SELECT_NAME || ' ' || CHAR(REPLACE(CHAR(SELECT_DATE), '-', '/')) AS LABEL ";
        $query .= " FROM ";
        $query .= "     STUDY_SELECT_DATE_YMST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //講座名簿数取得
    public function getChairStdDatCount($model)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     CHAIR_STD_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR || ':' || SEMESTER = '".$model->field["YEAR_SEMESTER"]."' ";

        return $query;
    }

    //出欠済み時間割チェック
    public function getSchChrDatExecuted($model)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     SCH_CHR_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR || ':' || SEMESTER = '".$model->field["YEAR_SEMESTER"]."' AND ";
        $query .= "     EXECUTED = '1' ";

        return $query;
    }

    //生成名簿取得
    public function getList($model)
    {
        $query  = " WITH CHAIR AS ( ";
        $query .= "     SELECT DISTINCT ";
        $query .= "         T1.YEAR, ";
        $query .= "         T1.SEMESTER, ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "         T1.CLASSCD, ";
            $query .= "         T1.SCHOOL_KIND, ";
            $query .= "         T1.CURRICULUM_CD, ";
        }
        $query .= "         T1.SUBCLASSCD, ";
        $query .= "         T1.CHAIRCD, ";
        $query .= "         T2.TRGTGRADE AS GRADE, ";
        $query .= "         T2.TRGTCLASS AS HR_CLASS ";
        $query .= "     FROM ";
        $query .= "         CHAIR_DAT T1, ";
        $query .= "         CHAIR_CLS_DAT T2 ";
        $query .= "     WHERE ";
        $query .= "         T1.YEAR     = T2.YEAR AND ";
        $query .= "         T1.YEAR || ':' || T1.SEMESTER = '".$model->field["YEAR_SEMESTER"]."'  AND ";
        $query .= "         T1.SEMESTER = T2.SEMESTER AND ";
        $query .= "         (T1.CHAIRCD = T2.CHAIRCD OR T2.CHAIRCD = '0000000') AND ";
        $query .= "         T1.GROUPCD  = T2.GROUPCD ";
        $query .= " ), CHAIR_LIST AS ( ";
        $query .= "     SELECT DISTINCT ";
        $query .= "         T1.YEAR, ";
        $query .= "         T1.SEMESTER, ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "         T1.CLASSCD, ";
            $query .= "         T1.SCHOOL_KIND, ";
            $query .= "         T1.CURRICULUM_CD, ";
        }
        $query .= "         T1.SUBCLASSCD, ";
        $query .= "         T1.CHAIRCD, ";
        $query .= "         T2.SCHREGNO ";
        $query .= "     FROM ";
        $query .= "         CHAIR T1, ";
        $query .= "         SCHREG_REGD_DAT T2 ";
        $query .= "     WHERE ";
        $query .= "         T1.YEAR     = T2.YEAR AND ";
        $query .= "         T1.SEMESTER = T2.SEMESTER AND ";
        $query .= "         T1.GRADE    = T2.GRADE AND ";
        $query .= "         T1.HR_CLASS = T2.HR_CLASS ";
        $query .= " ), MAIN AS ( ";
        $query .= "     SELECT DISTINCT ";
        $query .= "         T1.YEAR, ";
        $query .= "         T1.SEMESTER, ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "         T1.CLASSCD, ";
            $query .= "         T1.SCHOOL_KIND, ";
            $query .= "         T1.CURRICULUM_CD, ";
        }
        $query .= "         T1.SUBCLASSCD, ";
        $query .= "         T2.CHAIRCD, ";
        $query .= "         T1.SCHREGNO, ";
        $query .= "         T3.SDATE AS APPDATE, ";
        $query .= "         T3.EDATE AS APPENDDATE ";
        $query .= "     FROM ";
        $query .= "         SUBCLASS_STD_SELECT_RIREKI_DAT T1, ";
        $query .= "         CHAIR_LIST T2, ";
        $query .= "         SEMESTER_MST T3 ";
        $query .= "     WHERE ";
        $query .= "         T1.YEAR         = T2.YEAR AND ";
        $query .= "         T1.YEAR         = T3.YEAR AND ";
        $query .= "         T1.SEMESTER     = T2.SEMESTER AND ";
        $query .= "         T1.SEMESTER     = T3.SEMESTER AND ";
        $query .= "         T1.RIREKI_CODE = '".$model->field["RIREKI_CODE"]."' AND ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "         T1.CLASSCD          = T2.CLASSCD AND ";
            $query .= "         T1.SCHOOL_KIND      = T2.SCHOOL_KIND AND ";
            $query .= "         T1.CURRICULUM_CD    = T2.CURRICULUM_CD AND ";
        }
        $query .= "         T1.SUBCLASSCD   = T2.SUBCLASSCD AND ";
        $query .= "         T1.SCHREGNO     = T2.SCHREGNO ";
        $query .= " ), W_CNT AS ( ";
        $query .= "     SELECT ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "         CLASSCD, ";
            $query .= "         SCHOOL_KIND, ";
            $query .= "         CURRICULUM_CD, ";
        }
        $query .= "         SUBCLASSCD, ";
        $query .= "         SCHREGNO, ";
        $query .= "         COUNT(SCHREGNO) AS CNT ";
        $query .= "     FROM ";
        $query .= "         MAIN ";
        $query .= "     GROUP BY ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "         CLASSCD, ";
            $query .= "         SCHOOL_KIND, ";
            $query .= "         CURRICULUM_CD, ";
        }
        $query .= "         SUBCLASSCD, ";
        $query .= "         SCHREGNO ";
        $query .= " ) ";

        $query .= " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     MAIN T1 ";
        $query .= " WHERE ";
        $query .= "     EXISTS (SELECT ";
        $query .= "                 'X' ";
        $query .= "             FROM ";
        $query .= "                 W_CNT T2 ";
        $query .= "             WHERE ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "             T1.CLASSCD          = T2.CLASSCD AND ";
            $query .= "             T1.SCHOOL_KIND      = T2.SCHOOL_KIND AND ";
            $query .= "             T1.CURRICULUM_CD    = T2.CURRICULUM_CD AND ";
        }
        $query .= "                 T1.SUBCLASSCD = T2.SUBCLASSCD AND ";
        $query .= "                 T1.SCHREGNO =T2.SCHREGNO AND ";
        $query .= "                 T2.CNT = 1 ";
        $query .= "             ) ";

        return $query;
    }

    //名簿生成（更新）
    public function &getCreateQuery($model)
    {
        $db = Query::dbCheckOut();

        $result = $db->query(knjb1254Query::getList($model, "LIST"));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $data = array();
            $data["YEAR"][TEXT]         = $row["YEAR"];
            $data["SEMESTER"][TEXT]     = $row["SEMESTER"];
            $data["CHAIRCD"][TEXT]      = $row["CHAIRCD"];
            $data["SCHREGNO"][TEXT]     = $row["SCHREGNO"];
            $data["APPDATE"][TEXT]      = $row["APPDATE"];
            $data["APPENDDATE"][TEXT]   = $row["APPENDDATE"];
            $data["REGISTERCD"][TEXT]   = STAFFCD;
            $data["UPDATED"][NUMBER]    = "sysdate()";

            $query = Query::insertSQL($data, "CHAIR_STD_DAT");
            $db->query($query);
        }
        $result->free();

        Query::dbCheckIn($db);
        return;
    }

    //名簿削除
    public function &getDeleteQuery($model)
    {
        $db = Query::dbCheckOut();

        $query  = " DELETE FROM ";
        $query .= "     CHAIR_STD_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR || ':' || SEMESTER    = '".$model->field["YEAR_SEMESTER"]."' ";

        $db->query($query);

        Query::dbCheckIn($db);
        return $result;
    }

    //ＣＳＶ出力
    public function selectCsvQuery($model)
    {
        $query .= " WITH CHAIR_LIST AS ( ";
        $query .= "     SELECT DISTINCT ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "         T2.CLASSCD, ";
            $query .= "         T2.SCHOOL_KIND, ";
            $query .= "         T2.CURRICULUM_CD, ";
        }
        $query .= "         T2.SUBCLASSCD, ";
        $query .= "         T1.SCHREGNO ";
        $query .= "     FROM ";
        $query .= "         CHAIR_STD_DAT T1, ";
        $query .= "         CHAIR_DAT T2 ";
        $query .= "     WHERE ";
        $query .= "         T1.YEAR     = T2.YEAR AND ";
        $query .= "         T1.YEAR || ':' || T1.SEMESTER = '".$model->field["YEAR_SEMESTER"]."' AND ";
        $query .= "         T1.SEMESTER = T2.SEMESTER AND ";
        $query .= "         T1.CHAIRCD  = T2.CHAIRCD ";
        $query .= " ), MAIN AS ( ";
        $query .= "     SELECT ";
        $query .= "         * ";
        $query .= "     FROM ";
        $query .= "         SUBCLASS_STD_SELECT_RIREKI_DAT T1 ";
        $query .= "     WHERE ";
        $query .= "         YEAR || ':' || SEMESTER = '".$model->field["YEAR_SEMESTER"]."' AND ";
        $query .= "         RIREKI_CODE = '".$model->field["RIREKI_CODE"]."' AND ";
        $query .= "         NOT EXISTS (SELECT ";
        $query .= "                         'X' ";
        $query .= "                     FROM ";
        $query .= "                         CHAIR_LIST T2 ";
        $query .= "                     WHERE ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "                     T1.CLASSCD          = T2.CLASSCD AND ";
            $query .= "                     T1.SCHOOL_KIND      = T2.SCHOOL_KIND AND ";
            $query .= "                     T1.CURRICULUM_CD    = T2.CURRICULUM_CD AND ";
        }
        $query .= "                         T1.SUBCLASSCD = T2.SUBCLASSCD AND ";
        $query .= "                         T1.SCHREGNO = T2.SCHREGNO ";
        $query .= "                     ) ";
        $query .= " ) ";

        $query .= " SELECT ";
        $query .= "     T1.YEAR, ";
        $query .= "     T1.SEMESTER, ";
        $query .= "     CHAR(REPLACE(CHAR(T4.SELECT_DATE), '-', '/')) AS SELECT_DATE, ";
        $query .= "     T1.GROUPCD, ";
        $query .= "     T1.CLASSCD, ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "         T1.SCHOOL_KIND, ";
        }
        $query .= "     T1.CURRICULUM_CD, ";
        $query .= "     T1.SUBCLASSCD, ";
        $query .= "     T2.GRADE, ";
        $query .= "     T2.HR_CLASS, ";
        $query .= "     T2.ATTENDNO, ";
        $query .= "     T3.NAME, ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     '".$model->lastColumn."' ";
        $query .= " FROM ";
        $query .= "     MAIN T1 ";
        $query .= "     LEFT JOIN SCHREG_REGD_DAT T2 ON T1.YEAR = T2.YEAR ";
        $query .= "                                 AND T1.SEMESTER = T2.SEMESTER ";
        $query .= "                                 AND T1.SCHREGNO = T2.SCHREGNO ";
        $query .= "     LEFT JOIN SCHREG_BASE_MST T3 ON T1.SCHREGNO = T3.SCHREGNO ";
        $query .= "     LEFT JOIN STUDY_SELECT_DATE_YMST T4 ON T1.YEAR = T4.YEAR ";
        $query .= "                                        AND T1.RIREKI_CODE = T4.RIREKI_CODE ";
        $query .= " ORDER BY ";
        $query .= "     T2.GRADE, ";
        $query .= "     T2.HR_CLASS, ";
        $query .= "     T2.ATTENDNO, ";
        $query .= "     T1.SCHREGNO, ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "         T1.CLASSCD, ";
            $query .= "         T1.SCHOOL_KIND, ";
            $query .= "         T1.CURRICULUM_CD, ";
        }
        $query .= "     T1.SUBCLASSCD ";

        return $query;
    }
}
