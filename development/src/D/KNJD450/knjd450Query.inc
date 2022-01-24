<?php

require_once('for_php7.php');

class knjd450Query extends Query {

    //対象データ取得
    function getCountData($model, $setDiv="")
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     EDUCATION_GUIDANCE_SCHREG_REMARK_DAT T1 ";
        $query .= " WHERE ";
        $query .= "     T1.SCHREGNO = '".$model->schregno."' ";
        $query .= " AND T1.YEAR = '".$model->exp_year."' ";
        $query .= " AND T1.DIV = '".$setDiv."' ";
        $query .= " AND T1.CLASSCD = '00' ";
        $query .= " AND T1.SCHOOL_KIND = '00' ";
        $query .= " AND T1.CURRICULUM_CD = '00' ";
        $query .= " AND T1.SUBCLASSCD = '000000' ";

        return $query;
    }

    //対象データ一覧を取得する
    function getRecordList($model, $div2Cnt, $div3Cnt, $div4Cnt)
    {
    
        $query  = " WITH SUBCLASS_DATA AS ( ";
        $query .= " SELECT DISTINCT ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     T1.YEAR, ";
        $query .= "     '1' AS DIV, ";
        $query .= "     L1.CLASSCD, ";
        $query .= "     L1.SCHOOL_KIND, ";
        $query .= "     L1.CURRICULUM_CD, ";
        $query .= "     L1.SUBCLASSCD, ";
        $query .= "     L1.SUBCLASSNAME ";
        $query .= " FROM ";
        $query .= "     EDUCATION_GUIDANCE_SCHREG_SUBCLASS_DAT T1 ";
        $query .= "     INNER JOIN V_SUBCLASS_MST L1 ON L1.YEAR = T1.YEAR ";
        $query .= "                                 AND L1.CLASSCD = T1.CLASSCD ";
        $query .= "                                 AND L1.SCHOOL_KIND = T1.SCHOOL_KIND ";
        $query .= "                                 AND L1.CURRICULUM_CD = T1.CURRICULUM_CD ";
        $query .= "                                 AND L1.SUBCLASSCD = T1.SUBCLASSCD ";
        $query .= " WHERE ";
        $query .= "     T1.SCHREGNO = '".$model->schregno."' ";
        $query .= " AND T1.YEAR = '".$model->exp_year."' ";
        $query .= " ), REMARK_DAT AS ( ";
        $query .= "     SELECT ";
        $query .= "         T1.* ";
        $query .= "     FROM ";
        $query .= "         EDUCATION_GUIDANCE_SCHREG_REMARK_DAT T1, ";
        $query .= "         (SELECT ";
        $query .= "             CLASSCD, ";
        $query .= "             SCHOOL_KIND, ";
        $query .= "             CURRICULUM_CD, ";
        $query .= "             SUBCLASSCD, ";
        $query .= "             MAX(RTRIM(CHAR(WRITING_DATE)) || '-' || SEMESTER) AS DATE_SEME ";
        $query .= "         FROM ";
        $query .= "             EDUCATION_GUIDANCE_SCHREG_REMARK_DAT ";
        $query .= "         WHERE ";
        $query .= "             SCHREGNO    = '".$model->schregno."' AND ";
        $query .= "             YEAR        = '".$model->exp_year."' AND ";
        $query .= "             DIV         = '1' ";
        $query .= "         GROUP BY ";
        $query .= "             CLASSCD, ";
        $query .= "             SCHOOL_KIND, ";
        $query .= "             CURRICULUM_CD, ";
        $query .= "             SUBCLASSCD ";
        $query .= "         ) T2 ";
        $query .= "     WHERE ";
        $query .= "         T1.SCHREGNO         = '".$model->schregno."' AND ";
        $query .= "         T1.YEAR             = '".$model->exp_year."' AND ";
        $query .= "         T1.DIV              = '1' AND ";
        $query .= "         RTRIM(CHAR(T1.WRITING_DATE)) || '-' || T1.SEMESTER  = T2.DATE_SEME AND ";
        $query .= "         T1.CLASSCD          = T2.CLASSCD AND ";
        $query .= "         T1.SCHOOL_KIND      = T2.SCHOOL_KIND AND ";
        $query .= "         T1.CURRICULUM_CD    = T2.CURRICULUM_CD AND ";
        $query .= "         T1.SUBCLASSCD       = T2.SUBCLASSCD ";
        $query .= " ) ";

        $query .= " SELECT ";
        $query .= "     L1.SCHREGNO, ";
        $query .= "     L1.YEAR, ";
        $query .= "     T1.SEMESTER, ";
        $query .= "     L1.DIV, ";
        $query .= "     L1.CLASSCD, ";
        $query .= "     L1.SCHOOL_KIND, ";
        $query .= "     L1.CURRICULUM_CD, ";
        $query .= "     L1.SUBCLASSCD, ";
        $query .= "     L1.CLASSCD || '-' || L1.SCHOOL_KIND || '-' || L1.CURRICULUM_CD || '-' || L1.SUBCLASSCD || ':' || L1.SUBCLASSNAME AS SUBCLASSNAME, ";
        $query .= "     T1.WRITING_DATE, ";
        $query .= "     T1.REMARK1, ";
        $query .= "     T1.REMARK2, ";
        $query .= "     T1.REMARK3, ";
        $query .= "     T1.REMARK4 ";
        $query .= " FROM ";
        $query .= "     SUBCLASS_DATA L1 ";
        $query .= "     LEFT JOIN REMARK_DAT T1  ON L1.SCHREGNO     = T1.SCHREGNO ";
        $query .= "                             AND L1.YEAR         = T1.YEAR ";
        $query .= "                             AND L1.DIV          = T1.DIV ";
        $query .= "                             AND L1.CLASSCD      = T1.CLASSCD ";
        $query .= "                             AND L1.SCHOOL_KIND  = T1.SCHOOL_KIND ";
        $query .= "                             AND L1.CURRICULUM_CD = T1.CURRICULUM_CD ";
        $query .= "                             AND L1.SUBCLASSCD   = T1.SUBCLASSCD ";
        $query .= "  ";
        $query .= " UNION ";
        if ($div2Cnt == 0) {
            $query .= " SELECT ";
            $query .= "     '".$model->schregno."', ";
            $query .= "     '".$model->exp_year."', ";
            $query .= "     '' AS SEMESTER, ";
            $query .= "     '2' AS DIV, ";
            $query .= "     '00' AS CLASSCD, ";
            $query .= "     '00' AS SCHOOL_KIND, ";
            $query .= "     '00' AS CURRICULUM_CD, ";
            $query .= "     '000000' AS SUBCLASSCD, ";
            $query .= "     '対人関係・社会性' AS SUBCLASSNAME, ";
            $query .= "     CAST(NULL AS DATE) AS WRITING_DATE, ";
            $query .= "     '' AS REMARK1, ";
            $query .= "     '' AS REMARK2, ";
            $query .= "     '' AS REMARK3, ";
            $query .= "     '' AS REMARK4     ";
            $query .= " FROM ";
            $query .= "     SYSIBM.SYSDUMMY1 ";
        } else {
            $query .= " SELECT ";
            $query .= "     T1.SCHREGNO, ";
            $query .= "     T1.YEAR, ";
            $query .= "     T1.SEMESTER, ";
            $query .= "     T1.DIV, ";
            $query .= "     '00' AS CLASSCD, ";
            $query .= "     '00' AS SCHOOL_KIND, ";
            $query .= "     '00' AS CURRICULUM_CD, ";
            $query .= "     '000000' AS SUBCLASSCD, ";
            $query .= "     '対人関係・社会性' AS SUBCLASSNAME, ";
            $query .= "     T1.WRITING_DATE, ";
            $query .= "     T1.REMARK1, ";
            $query .= "     T1.REMARK2, ";
            $query .= "     T1.REMARK3, ";
            $query .= "     T1.REMARK4 ";
            $query .= " FROM ";
            $query .= "     EDUCATION_GUIDANCE_SCHREG_REMARK_DAT T1, ";
            $query .= "     (SELECT ";
            $query .= "         CLASSCD, ";
            $query .= "         SCHOOL_KIND, ";
            $query .= "         CURRICULUM_CD, ";
            $query .= "         SUBCLASSCD, ";
            $query .= "         MAX(RTRIM(CHAR(WRITING_DATE)) || '-' || SEMESTER) AS DATE_SEME ";
            $query .= "     FROM ";
            $query .= "         EDUCATION_GUIDANCE_SCHREG_REMARK_DAT ";
            $query .= "     WHERE ";
            $query .= "         SCHREGNO = '".$model->schregno."' AND ";
            $query .= "         YEAR     = '".$model->exp_year."' AND ";
            $query .= "         DIV      = '2' ";
            $query .= "     GROUP BY ";
            $query .= "         CLASSCD, ";
            $query .= "         SCHOOL_KIND, ";
            $query .= "         CURRICULUM_CD, ";
            $query .= "         SUBCLASSCD ";
            $query .= "     ) T2 ";
            $query .= " WHERE ";
            $query .= "     T1.SCHREGNO     = '".$model->schregno."' AND ";
            $query .= "     T1.YEAR         = '".$model->exp_year."' AND ";
            $query .= "     T1.DIV          = '2' AND ";
            $query .= "     RTRIM(CHAR(T1.WRITING_DATE)) || '-' || T1.SEMESTER  = T2.DATE_SEME AND ";
            $query .= "     T1.CLASSCD      = T2.CLASSCD AND ";
            $query .= "     T1.SCHOOL_KIND  = T2.SCHOOL_KIND AND ";
            $query .= "     T1.CURRICULUM_CD = T2.CURRICULUM_CD AND ";
            $query .= "     T1.SUBCLASSCD   = T2.SUBCLASSCD ";
        }
        $query .= " UNION ";
        if ($div3Cnt == 0) {
            $query .= " SELECT ";
            $query .= "     '".$model->schregno."', ";
            $query .= "     '".$model->exp_year."', ";
            $query .= "     '' AS SEMESTER, ";
            $query .= "     '3' AS DIV, ";
            $query .= "     '00' AS CLASSCD, ";
            $query .= "     '00' AS SCHOOL_KIND, ";
            $query .= "     '00' AS CURRICULUM_CD, ";
            $query .= "     '000000' AS SUBCLASSCD, ";
            $query .= "     'コミュニケーション' AS SUBCLASSNAME, ";
            $query .= "     CAST(NULL AS DATE) AS WRITING_DATE, ";
            $query .= "     '' AS REMARK1, ";
            $query .= "     '' AS REMARK2, ";
            $query .= "     '' AS REMARK3, ";
            $query .= "     '' AS REMARK4     ";
            $query .= " FROM ";
            $query .= "     SYSIBM.SYSDUMMY1 ";
        } else {
            $query .= " SELECT ";
            $query .= "     T1.SCHREGNO, ";
            $query .= "     T1.YEAR, ";
            $query .= "     T1.SEMESTER, ";
            $query .= "     T1.DIV, ";
            $query .= "     '00' AS CLASSCD, ";
            $query .= "     '00' AS SCHOOL_KIND, ";
            $query .= "     '00' AS CURRICULUM_CD, ";
            $query .= "     '000000' AS SUBCLASSCD, ";
            $query .= "     'コミュニケーション' AS SUBCLASSNAME, ";
            $query .= "     T1.WRITING_DATE, ";
            $query .= "     T1.REMARK1, ";
            $query .= "     T1.REMARK2, ";
            $query .= "     T1.REMARK3, ";
            $query .= "     T1.REMARK4     ";
            $query .= " FROM ";
            $query .= "     EDUCATION_GUIDANCE_SCHREG_REMARK_DAT T1, ";
            $query .= "     (SELECT ";
            $query .= "         CLASSCD, ";
            $query .= "         SCHOOL_KIND, ";
            $query .= "         CURRICULUM_CD, ";
            $query .= "         SUBCLASSCD, ";
            $query .= "         MAX(RTRIM(CHAR(WRITING_DATE)) || '-' || SEMESTER) AS DATE_SEME ";
            $query .= "     FROM ";
            $query .= "         EDUCATION_GUIDANCE_SCHREG_REMARK_DAT ";
            $query .= "     WHERE ";
            $query .= "         SCHREGNO = '".$model->schregno."' AND ";
            $query .= "         YEAR     = '".$model->exp_year."' AND ";
            $query .= "         DIV      = '3' ";
            $query .= "     GROUP BY ";
            $query .= "         CLASSCD, ";
            $query .= "         SCHOOL_KIND, ";
            $query .= "         CURRICULUM_CD, ";
            $query .= "         SUBCLASSCD ";
            $query .= "     ) T2 ";
            $query .= " WHERE ";
            $query .= "     T1.SCHREGNO     = '".$model->schregno."' AND ";
            $query .= "     T1.YEAR         = '".$model->exp_year."' AND ";
            $query .= "     T1.DIV          = '3' AND ";
            $query .= "     RTRIM(CHAR(T1.WRITING_DATE)) || '-' || T1.SEMESTER  = T2.DATE_SEME AND ";
            $query .= "     T1.CLASSCD      = T2.CLASSCD AND ";
            $query .= "     T1.SCHOOL_KIND  = T2.SCHOOL_KIND AND ";
            $query .= "     T1.CURRICULUM_CD = T2.CURRICULUM_CD AND ";
            $query .= "     T1.SUBCLASSCD   = T2.SUBCLASSCD ";
        }
        $query .= " UNION ";
        if ($div4Cnt == 0) {
            $query .= " SELECT ";
            $query .= "     '".$model->schregno."', ";
            $query .= "     '".$model->exp_year."', ";
            $query .= "     '' AS SEMESTER, ";
            $query .= "     '4' AS DIV, ";
            $query .= "     '00' AS CLASSCD, ";
            $query .= "     '00' AS SCHOOL_KIND, ";
            $query .= "     '00' AS CURRICULUM_CD, ";
            $query .= "     '000000' AS SUBCLASSCD, ";
            $query .= "     'その他' AS SUBCLASSNAME, ";
            $query .= "     CAST(NULL AS DATE) AS WRITING_DATE, ";
            $query .= "     '' AS REMARK1, ";
            $query .= "     '' AS REMARK2, ";
            $query .= "     '' AS REMARK3, ";
            $query .= "     '' AS REMARK4     ";
            $query .= " FROM ";
            $query .= "     SYSIBM.SYSDUMMY1 ";
        } else {
            $query .= " SELECT ";
            $query .= "     T1.SCHREGNO, ";
            $query .= "     T1.YEAR, ";
            $query .= "     T1.SEMESTER, ";
            $query .= "     T1.DIV, ";
            $query .= "     '00' AS CLASSCD, ";
            $query .= "     '00' AS SCHOOL_KIND, ";
            $query .= "     '00' AS CURRICULUM_CD, ";
            $query .= "     '000000' AS SUBCLASSCD, ";
            $query .= "     'その他' AS SUBCLASSNAME, ";
            $query .= "     T1.WRITING_DATE, ";
            $query .= "     T1.REMARK1, ";
            $query .= "     T1.REMARK2, ";
            $query .= "     T1.REMARK3, ";
            $query .= "     T1.REMARK4     ";
            $query .= " FROM ";
            $query .= "     EDUCATION_GUIDANCE_SCHREG_REMARK_DAT T1, ";
            $query .= "     (SELECT ";
            $query .= "         CLASSCD, ";
            $query .= "         SCHOOL_KIND, ";
            $query .= "         CURRICULUM_CD, ";
            $query .= "         SUBCLASSCD, ";
            $query .= "         MAX(RTRIM(CHAR(WRITING_DATE)) || '-' || SEMESTER) AS DATE_SEME ";
            $query .= "     FROM ";
            $query .= "         EDUCATION_GUIDANCE_SCHREG_REMARK_DAT ";
            $query .= "     WHERE ";
            $query .= "         SCHREGNO = '".$model->schregno."' AND ";
            $query .= "         YEAR     = '".$model->exp_year."' AND ";
            $query .= "         DIV      = '4' ";
            $query .= "     GROUP BY ";
            $query .= "         CLASSCD, ";
            $query .= "         SCHOOL_KIND, ";
            $query .= "         CURRICULUM_CD, ";
            $query .= "         SUBCLASSCD ";
            $query .= "     ) T2 ";
            $query .= " WHERE ";
            $query .= "     T1.SCHREGNO     = '".$model->schregno."' AND ";
            $query .= "     T1.YEAR         = '".$model->exp_year."' AND ";
            $query .= "     T1.DIV          = '4' AND ";
            $query .= "     RTRIM(CHAR(T1.WRITING_DATE)) || '-' || T1.SEMESTER  = T2.DATE_SEME AND ";
            $query .= "     T1.CLASSCD      = T2.CLASSCD AND ";
            $query .= "     T1.SCHOOL_KIND  = T2.SCHOOL_KIND AND ";
            $query .= "     T1.CURRICULUM_CD = T2.CURRICULUM_CD AND ";
            $query .= "     T1.SUBCLASSCD   = T2.SUBCLASSCD ";
        }
        $query .= " ORDER BY ";
        $query .= "     DIV, ";
        $query .= "     SCHOOL_KIND, ";
        $query .= "     CLASSCD, ";
        $query .= "     CURRICULUM_CD, ";
        $query .= "     SUBCLASSCD ";
        return $query;
    }

    //作成年月日取得
    function getWritingDate($model, $date="") {
        $query  = " SELECT DISTINCT ";
        $query .= "     T1.WRITING_DATE AS VALUE, ";
        $query .= "     REPLACE(RTRIM(CHAR(T1.WRITING_DATE)), '-', '/') AS LABEL, ";
        $query .= "     T1.SEMESTER ";
        $query .= " FROM ";
        $query .= "     EDUCATION_GUIDANCE_SCHREG_REMARK_DAT T1, ";
        $query .= "     (SELECT DISTINCT ";
        $query .= "         WRITING_DATE, ";
        $query .= "         MAX(SEMESTER) AS SEMESTER ";
        $query .= "     FROM ";
        $query .= "         EDUCATION_GUIDANCE_SCHREG_REMARK_DAT ";
        $query .= "     WHERE ";
        $query .= "         SCHREGNO        = '".$model->schregno."' AND ";
        $query .= "         YEAR            = '".$model->exp_year."' AND ";
        $query .= "         CLASSCD         = '".$model->getClasscd."' AND ";
        $query .= "         SCHOOL_KIND     = '".$model->getSchoolkind."' AND ";
        $query .= "         CURRICULUM_CD   = '".$model->getCurriculumcd."' AND ";
        $query .= "         SUBCLASSCD      = '".$model->getSubclasscd."' AND ";
        $query .= "         DIV             = '".$model->getDiv."' ";
        $query .= "     GROUP BY ";
        $query .= "         WRITING_DATE ";
        $query .= "     ) T2 ";
        $query .= " WHERE ";
        $query .= "     T1.SCHREGNO         = '".$model->schregno."' AND ";
        $query .= "     T1.YEAR             = '".$model->exp_year."' AND ";
        $query .= "     T1.CLASSCD          = '".$model->getClasscd."' AND ";
        $query .= "     T1.SCHOOL_KIND      = '".$model->getSchoolkind."' AND ";
        $query .= "     T1.CURRICULUM_CD    = '".$model->getCurriculumcd."' AND ";
        $query .= "     T1.SUBCLASSCD       = '".$model->getSubclasscd."' AND ";
        $query .= "     T1.DIV              = '".$model->getDiv."' AND ";
        $query .= "     T1.SEMESTER         = T2.SEMESTER AND ";
        $query .= "     T1.WRITING_DATE     = T2.WRITING_DATE ";
        if ($date) {
            $query .= " AND T1.WRITING_DATE     = '".$date."' ";
        } else {
            $query .= " ORDER BY ";
            $query .= "     VALUE DESC ";
        }

        return $query;
    }

    //対象データ取得
    function getSelectData($model, $semester)
    {

        $query  = " SELECT ";
        $query .= "     T1.REMARK1, ";
        $query .= "     T1.REMARK2, ";
        $query .= "     T1.REMARK3, ";
        $query .= "     T1.REMARK4 ";
        $query .= " FROM ";
        $query .= "     EDUCATION_GUIDANCE_SCHREG_REMARK_DAT T1 ";
        $query .= " WHERE ";
        $query .= "     T1.SCHREGNO     = '".$model->schregno."' ";
        $query .= " AND T1.YEAR         = '".$model->exp_year."' ";
        $query .= " AND T1.SEMESTER     = '".$semester."' ";
        $query .= " AND T1.DIV          = '".$model->getDiv."' ";
        $query .= " AND T1.WRITING_DATE = '".$model->field["WRITING_DATE"]."' ";
        $query .= " AND T1.CLASSCD      = '".$model->getClasscd."' ";
        $query .= " AND T1.SCHOOL_KIND  = '".$model->getSchoolkind."' ";
        $query .= " AND T1.CURRICULUM_CD = '".$model->getCurriculumcd."' ";
        $query .= " AND T1.SUBCLASSCD   = '".$model->getSubclasscd."' ";

        return $query;
    }

    //INSERT
    function &getUpdateQuery($model)
    {
        $db = Query::dbCheckOut();
        $date = str_replace("/", "-", $model->field["WRT_DATE"]);

        $data = array();
        $data["REMARK1"][TEXT]      = $model->field["REMARK1"];
        $data["REMARK2"][TEXT]      = $model->field["REMARK2"];
        $data["REMARK3"][TEXT]      = $model->field["REMARK3"];
        $data["REMARK4"][TEXT]      = $model->field["REMARK4"];
        $data["REGISTERCD"][TEXT]          = STAFFCD;
        $data["UPDATED"][NUMBER]           = "sysdate()";
        if ($model->cmd == "add") {
            $data["YEAR"][TEXT]            = $model->exp_year;
            $data["SEMESTER"][TEXT]        = '9';
            $data["SCHREGNO"][TEXT]        = $model->schregno;
            $data["DIV"][TEXT]             = $model->getDiv;
            $data["WRITING_DATE"][TEXT]    = $date;
            $data["CLASSCD"][TEXT]         = $model->getClasscd;
            $data["SCHOOL_KIND"][TEXT]     = $model->getSchoolkind;
            $data["CURRICULUM_CD"][TEXT]   = $model->getCurriculumcd;
            $data["SUBCLASSCD"][TEXT]      = $model->getSubclasscd;
            
            $query = Query::insertSQL($data, "EDUCATION_GUIDANCE_SCHREG_REMARK_DAT");
            $db->query($query);
        } else {
            //学期取得
            $date_sem = $db->getRow(knjd450Query::getWritingDate($model, $date), DB_FETCHMODE_ASSOC);

            $where  = " WHERE ";
            $where .= "     SCHREGNO        = '".$model->schregno."' ";
            $where .= " AND YEAR            = '".$model->exp_year."' ";
            $where .= " AND SEMESTER        = '".$date_sem["SEMESTER"]."' ";
            $where .= " AND DIV             = '".$model->getDiv."' ";
            $where .= " AND WRITING_DATE    = '".$date."' ";
            $where .= " AND CLASSCD         = '".$model->getClasscd."' ";
            $where .= " AND SCHOOL_KIND     = '".$model->getSchoolkind."' ";
            $where .= " AND CURRICULUM_CD   = '".$model->getCurriculumcd."' ";
            $where .= " AND SUBCLASSCD      = '".$model->getSubclasscd."' ";
        
            $query = Query::updateSQL($data, "EDUCATION_GUIDANCE_SCHREG_REMARK_DAT", $where);
            $db->query($query);
        }

        Query::dbCheckIn($db);
        return ;
    }
}
?>
