<?php

require_once('for_php7.php');

class knje015Query extends Query
{
    /**************/
    /* メイン画面 */
    /**************/
    //学年コンボ
    public function getGrade($year, $schregno)
    {
        $query   = " WITH REGD_DATA AS ( ";
        $query  .= "   SELECT ";
        $query  .= "     T1.YEAR, ";
        $query  .= "     T1.SCHREGNO, ";
        $query  .= "     T2.GRADE ";
        $query  .= "   FROM ";
        $query  .= "     SCHREG_REGD_DAT T1 ";
        $query  .= "     LEFT JOIN SCHREG_REGD_GDAT T2 ";
        $query  .= "       ON T1.YEAR = T2.YEAR ";
        $query  .= "       AND T1.GRADE = T2.GRADE ";
        $query  .= "   WHERE ";
        $query  .= "     T1.SCHREGNO = '{$schregno}' ";
        $query  .= "     AND T2.SCHOOL_KIND = ( ";
        $query  .= "       SELECT DISTINCT ";
        $query  .= "         TWK.SCHOOL_KIND ";
        $query  .= "       FROM ";
        $query  .= "         SCHREG_REGD_DAT TW1, ";
        $query  .= "         SCHREG_REGD_GDAT TWK ";
        $query  .= "       WHERE ";
        $query  .= "         TW1.YEAR = '{$year}' ";
        $query  .= "         AND TW1.SCHREGNO = '{$schregno}' ";
        $query  .= "         AND TWK.YEAR = TW1.YEAR ";
        $query  .= "         AND TWK.GRADE = TW1.GRADE ";
        $query  .= "     ) ";
        $query  .= " ),  HEXAM AS (";
        $query  .= "   SELECT ";
        $query  .= "     T1.YEAR, ";
        $query  .= "     T1.SCHREGNO, ";
        $query  .= "     T2.GRADE ";
        $query  .= "   FROM ";
        $query  .= "     ( ";
        $query  .= "       SELECT ";
        $query  .= "         YEAR, ";
        $query  .= "         SCHREGNO ";
        $query  .= "       FROM ";
        $query  .= "         HEXAM_ENTREMARK_DAT ENTREMARK ";
        $query  .= "       WHERE ";
        $query  .= "         SCHREGNO = '{$schregno}' ";
        $query  .= "       UNION ";
        $query  .= "       SELECT ";
        $query  .= "         YEAR, ";
        $query  .= "         SCHREGNO ";
        $query  .= "       FROM ";
        $query  .= "         HEXAM_ENTREMARK_DAT ENTREMARK_TRAINREF ";
        $query  .= "       WHERE ";
        $query  .= "         SCHREGNO = '{$schregno}' ";
        $query  .= "     ) T1 ";
        $query  .= "     LEFT JOIN SCHREG_REGD_DAT T2 ";
        $query  .= "       ON T1.YEAR = T2.YEAR ";
        $query  .= "       AND T1.SCHREGNO = T2.SCHREGNO ";
        $query  .= " ) ";
        $query  .= " SELECT ";
        $query  .= "   REGHEX.YEAR || '年度 ' || CASE ";
        $query  .= "     WHEN GDAT.GRADE_CD IS NOT NULL ";
        $query  .= "     THEN CAST(INT (GDAT.GRADE_CD) AS CHAR (1)) || '学年 ' ";
        $query  .= "     ELSE '' ";
        $query  .= "     END AS LABEL, ";
        $query  .= "     REGHEX.YEAR AS VALUE ";
        $query  .= " FROM ";
        $query  .= "   ( ";
        $query  .= "     SELECT ";
        $query  .= "       REGD.YEAR, ";
        $query  .= "       REGD.SCHREGNO, ";
        $query  .= "       REGD.GRADE ";
        $query  .= "     FROM ";
        $query  .= "       REGD_DATA REGD ";
        $query  .= "     UNION ";
        $query  .= "     SELECT ";
        $query  .= "       HEXAM.YEAR, ";
        $query  .= "       HEXAM.SCHREGNO, ";
        $query  .= "       HEXAM.GRADE ";
        $query  .= "     FROM ";
        $query  .= "       HEXAM ";
        $query  .= "   ) REGHEX ";
        $query  .= "   LEFT JOIN SCHREG_REGD_GDAT GDAT ";
        $query  .= "     ON REGHEX.YEAR = GDAT.YEAR ";
        $query  .= "     AND REGHEX.GRADE = GDAT.GRADE ";
        $query  .= " GROUP BY ";
        $query  .= "   REGHEX.YEAR, ";
        $query  .= "   GDAT.GRADE_CD ";
        $query  .= " ORDER BY ";
        $query  .= "   VALUE ";

        return $query;
    }

    //対象年度における生徒の担任一覧取得
    public function getTaninCnt($model)
    {
        $query  = "  SELECT ";
        $query .= "     COUNT(*) ";
        $query .= "  FROM ";
        $query .= "    SCHREG_REGD_DAT T1 ";
        $query .= "    INNER JOIN SCHREG_REGD_HDAT T2 ";
        $query .= "      ON T1.YEAR = T2.YEAR ";
        $query .= "      AND T1.SEMESTER = T2.SEMESTER ";
        $query .= "      AND T1.GRADE = T2.GRADE ";
        $query .= "      AND T1.HR_CLASS = T2.HR_CLASS ";
        $query .= "  WHERE ";
        $query .= "    T1.SCHREGNO = '".$model->schregno."' ";
        $query .= "    AND T1.YEAR = '".$model->gradeYear."' ";
        if ($model->auth == DEF_REFER_RESTRICT || $model->auth == DEF_UPDATE_RESTRICT) {
            $query .= "  AND ( ";
            $query .= "       '".STAFFCD."' IN (TR_CD1, TR_CD2, TR_CD3, SUBTR_CD1, SUBTR_CD2, SUBTR_CD3) ";
            $query .= "       OR ";
            $query .= "       T1.GRADE IN ";
            $query .= "           (SELECT ";
            $query .= "                FIELD2 ";
            $query .= "            FROM ";
            $query .= "                STAFF_DETAIL_MST ST ";
            $query .= "            WHERE ";
            $query .= "                ST.YEAR = T1.YEAR ";
            $query .= "                AND ST.STAFFCD = '".STAFFCD."' ";
            $query .= "                AND ST.STAFF_SEQ IN ('005', '006', '007') ";
            $query .= "                AND ST.FIELD1 = '0200') ";
            $query .= "      ) ";
        } else {
            $query .= "    AND '".STAFFCD."' IN (TR_CD1, TR_CD2, TR_CD3, SUBTR_CD1, SUBTR_CD2, SUBTR_CD3) ";
        }
        return $query;
    }

    //総合所見の使用不可(2020年度以降は使用しない)
    public function getTotalRemarkDisable($model)
    {
        $query  = " WITH GRADE_2018 AS ( ";
        $query .= "     SELECT ";
        $query .= "         * ";
        $query .= "     FROM ";
        $query .= "         SCHREG_REGD_GDAT ";
        $query .= "     WHERE ";
        $query .= "         YEAR = '2018' ";
        $query .= "         AND SCHOOL_KIND = 'H' ";
        $query .= "     ORDER BY ";
        $query .= "         GRADE ";
        $query .= "      FETCH FIRST 1 ROWS ONLY ";
        $query .= " ) ";
        $query .= " , GRADE_2019 AS ( ";
        $query .= "     SELECT ";
        $query .= "         * ";
        $query .= "     FROM ";
        $query .= "         SCHREG_REGD_GDAT ";
        $query .= "     WHERE ";
        $query .= "         YEAR = '2019' ";
        $query .= "         AND SCHOOL_KIND = 'H' ";
        $query .= "     ORDER BY ";
        $query .= "         GRADE ";
        $query .= "         FETCH FIRST 2 ROWS ONLY ";
        $query .= " ) ";
        $query .= " SELECT * FROM GRADE_2018 ";
        $query .= " UNION ";
        $query .= " SELECT * FROM GRADE_2019 ";
        $query .= " ORDER BY ";
        $query .= "     YEAR ";
        $query .= "     , GRADE ";

        return $query;
    }

    //メインデータ
    public function getMainData($model)
    {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     HEXAM_ENTREMARK_TRAINREF_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$model->gradeYear}' ";
        $query .= "     AND SCHREGNO = '{$model->schregno}' ";
        
        return $query;
    }

    //メインデータ(総合所見)
    public function getMainDataTotalremark($model)
    {
        $query  = " SELECT ";
        $query .= "     TOTALREMARK ";
        $query .= " FROM ";
        $query .= "     HTRAINREMARK_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$model->gradeYear}' ";
        $query .= "     AND SCHREGNO = '{$model->schregno}' ";

        return $query;
    }

    //メインデータ(学びの記録)
    public function getMainDataManabi($model)
    {
        $query  = " SELECT ";
        $query .= "     REMARK ";
        $query .= " FROM ";
        $query .= "     HEXAM_ENTREMARK_LEARNING_DAT ";
        $query .= " WHERE ";
        $query .= "         YEAR     = '{$model->gradeYear}' ";
        $query .= "     AND SCHREGNO = '{$model->schregno}' ";

        return $query;
    }

    //更新
    public function update(&$db, $model)
    {
        for ($i=1; $i<=6; $i++) {
            $data = array();
            $data["REMARK"][TEXT]         = $model->field['SEQ00'.$i];
            $data["REGISTERCD"][TEXT]     = STAFFCD;
            $data["UPDATED"][NUMBER]      = "SYSDATE()";

            $query  = " SELECT ";
            $query .= "     COUNT(*) ";
            $query .= " FROM ";
            $query .= "     HEXAM_ENTREMARK_TRAINREF_DAT ";
            $query .= " WHERE ";
            $query .= "     YEAR = '{$model->gradeYear}' ";
            $query .= "     AND SCHREGNO = '{$model->schregno}' ";
            $query .= "     AND TRAIN_SEQ = '00{$i}' ";

            if ($db->getOne($query)>0) {
                $where  = "WHERE ";
                $where .= "     YEAR = '{$model->gradeYear}' ";
                $where .= "     AND SCHREGNO = '{$model->schregno}' ";
                $where .= "     AND TRAIN_SEQ = '00{$i}' ";

                $query = Query::updateSQL($data, "HEXAM_ENTREMARK_TRAINREF_DAT", $where);
            } else {
                $data["YEAR"][TEXT]           = $model->gradeYear;
                $data["SCHREGNO"][TEXT]       = $model->schregno;
                $data["TRAIN_SEQ"][TEXT]      = '00'.$i;
                $query = Query::insertSQL($data, 'HEXAM_ENTREMARK_TRAINREF_DAT');
            }
            $db->query($query);
        }

        if ($model->Properties["useSeitoSidoYorokuShomeiKinou"] != "1" && ($model->Properties["useSeitoSidoYorokuSougouShoken"] == "1" || $model->Properties["useSeitoSidoYorokuSougouShoken"] == "2")) {
            //データセット(総合所見)
            $data = array();
            $data["TOTALREMARK"][TEXT]      = $model->field['TOTALREMARK'];
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][NUMBER]        = "SYSDATE()";

            $query  = " SELECT ";
            $query .= "     COUNT(*) ";
            $query .= " FROM ";
            $query .= "     HTRAINREMARK_DAT ";
            $query .= " WHERE ";
            $query .= "         YEAR     = '{$model->gradeYear}' ";
            $query .= "     AND SCHREGNO = '{$model->schregno}' ";

            if ($db->getOne($query) > 0) {
                $where  = "WHERE ";
                $where .= "         YEAR     = '{$model->gradeYear}' ";
                $where .= "     AND SCHREGNO = '{$model->schregno}' ";

                $query = Query::updateSQL($data, "HTRAINREMARK_DAT", $where);
            } else {
                $query  = " SELECT ";
                $query .= "     ANNUAL ";
                $query .= " FROM ";
                $query .= "     SCHREG_REGD_DAT ";
                $query .= " WHERE ";
                $query .= "     YEAR     = '{$model->gradeYear}' AND ";
                $query .= "     SCHREGNO = '{$model->schregno}' ";
                $query .= " ORDER BY ";
                $query .= "     YEAR || SEMESTER DESC  ";
                $annual = $db->getOne($query);

                $data["YEAR"][TEXT]         = $model->gradeYear;
                $data["SCHREGNO"][TEXT]     = $model->schregno;
                $data["ANNUAL"][TEXT]       = $annual;
                $query = Query::insertSQL($data, 'HTRAINREMARK_DAT');
            }
            $db->query($query);
        }

        //データセット(まなびの記録)
        $data = array();
        $data["REMARK"][TEXT]         = $model->field['REMARK'];
        $data["REGISTERCD"][TEXT]     = STAFFCD;
        $data["UPDATED"][NUMBER]      = "SYSDATE()";

        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     HEXAM_ENTREMARK_LEARNING_DAT ";
        $query .= " WHERE ";
        $query .= "         YEAR     = '{$model->gradeYear}' ";
        $query .= "     AND SCHREGNO = '{$model->schregno}' ";

        if ($db->getOne($query)>0) {
            $where  = "WHERE ";
            $where .= "         YEAR     = '{$model->gradeYear}' ";
            $where .= "     AND SCHREGNO = '{$model->schregno}' ";

            $query = Query::updateSQL($data, "HEXAM_ENTREMARK_LEARNING_DAT", $where);
        } else {
            $data["YEAR"][TEXT]           = $model->gradeYear;
            $data["SCHREGNO"][TEXT]       = $model->schregno;
            $query = Query::insertSQL($data, 'HEXAM_ENTREMARK_LEARNING_DAT');
        }
        $db->query($query);
    }

    /**********************/
    /* 通知表所見参照画面 */
    /**********************/
    //年度コンボ
    public function getYear($schregno)
    {
        $query  = " SELECT ";
        $query .= "     YEAR AS LABEL, ";
        $query .= "     YEAR AS VALUE ";
        $query .= " FROM ";
        $query .= "     HREPORTREMARK_DAT ";
        $query .= " WHERE ";
        $query .= "       SCHREGNO = '{$schregno}' ";
        $query .= " GROUP BY ";
        $query .= "     YEAR ";
        $query .= " ORDER BY ";
        $query .= "     YEAR ";

        return $query;
    }

    //通知票所見
    public function getRemark($schregno, $year)
    {
        $query  = " SELECT ";
        $query .= "     SEMESTER, ";
        $query .= "     TOTALSTUDYTIME, ";
        $query .= "     SPECIALACTREMARK, ";
        $query .= "     COMMUNICATION, ";
        $query .= "     REMARK1, ";
        $query .= "     REMARK2, ";
        $query .= "     REMARK3 ";
        $query .= " FROM ";
        $query .= "     HREPORTREMARK_DAT ";
        $query .= " WHERE ";
        $query .= "       YEAR     = '{$year}' ";
        $query .= "   AND SCHREGNO = '{$schregno}' ";

        return $query;
    }

    //学期名称取得
    public function getSemester($year, $semester)
    {
        $query  = " SELECT ";
        $query .= "     SEMESTERNAME ";
        $query .= " FROM ";
        $query .= "     SEMESTER_MST T1 ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$year."' AND ";
        $query .= "     SEMESTER = '".$semester."' ";

        return $query;
    }

    /******************/
    /* 部活動参照画面 */
    /******************/
    //部活動参照
    public function getClub($model)
    {
        $sdate = $model->gradeYear . '-04-01';
        $edate = ((int)$model->gradeYear + 1) . '-03-31';

        $query  = " SELECT ";
        $query .= "     T2.CLUBNAME, ";
        $query .= "     T1.SDATE, ";
        $query .= "     T1.EDATE, ";
        $query .= "     T3.NAME1 ";
        $query .= " FROM ";
        $query .= "     SCHREG_CLUB_HIST_DAT T1 ";
        $query .= " LEFT OUTER JOIN ";
        $query .= "     CLUB_MST T2 ON  T1.CLUBCD = T2.CLUBCD ";
        $query .= " LEFT OUTER JOIN ";
        $query .= "     NAME_MST T3 ON  T3.NAMECD1     = 'J001' ";
        $query .= "                 AND T1.EXECUTIVECD = T3.NAMECD2 ";
        $query .= " WHERE ";
        $query .= "     T1.SCHREGNO = '{$model->schregno}' AND ";
        $query .= "     ( ";
        $query .= "         (T1.SDATE                      <= '{$sdate}' AND VALUE(T1.EDATE, '9999-12-31') >= '{$edate}') OR ";
        $query .= "         (T1.SDATE                      >= '{$sdate}' AND T1.SDATE                      <= '{$edate}') OR ";
        $query .= "         (VALUE(T1.EDATE, '9999-12-31') >= '{$sdate}' AND VALUE(T1.EDATE, '9999-12-31') <= '{$edate}') ";
        $query .= "     ) ";
        $query .= " ORDER BY ";
        $query .= "     T1.SDATE ";

        return $query;
    }

    /****************/
    /* 資格参照画面 */
    /****************/
    //学籍資格データよりデータを取得
    public function getAward($model)
    {
        $sdate = $model->gradeYear . '-04-01';
        $edate = ((int)$model->gradeYear + 1) . '-03-31';

        if ($model->Properties["useQualifiedMst"] == '1') {
            $query  = " SELECT ";
            $query .= "     T1.YEAR, ";
            $query .= "     T1.SCHREGNO, ";
            $query .= "     T1.SEQ, ";
            $query .= "     T1.REGDDATE, ";
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "     T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD AS SUBCLASSCD, ";
            } else {
                $query .= "     T1.SUBCLASSCD, ";
            }
            $query .= "     CASE L1.CONDITION_DIV ";
            $query .= "          WHEN '1' ";
            $query .= "          THEN '国家資格' ";
            $query .= "          WHEN '2' ";
            $query .= "          THEN '公的資格' ";
            $query .= "          WHEN '3' ";
            $query .= "          THEN '民間資格' ";
            $query .= "          ELSE 'その他' ";
            $query .= "     END AS CONDITION_DIV, ";
            $query .= "     T1.QUALIFIED_CD, ";
            $query .= "     L1.QUALIFIED_NAME, ";
            $query .= "     L1.PROMOTER, ";
            $query .= "     T1.CONTENTS, ";
            $query .= "     L2.NAME1 AS RANK, ";
            $query .= "     T1.REMARK, ";
            $query .= "     T1.CREDITS ";
            $query .= " FROM ";
            $query .= "     SCHREG_QUALIFIED_HOBBY_DAT T1 ";
            $query .= " LEFT JOIN ";
            $query .= "     QUALIFIED_MST L1 ON L1.QUALIFIED_CD = T1.QUALIFIED_CD ";
            $query .= " LEFT JOIN ";
            $query .= "     NAME_MST L2 ON  L2.NAMECD2 = T1.RANK ";
            $query .= "                 AND L2.NAMECD1 = 'H312' ";
            $query .= " WHERE ";
            $query .= "     T1.SCHREGNO = '{$model->schregno}' AND ";
            $query .= "     T1.REGDDATE BETWEEN '{$sdate}' AND '{$edate}' ";
            $query .= " ORDER BY ";
            $query .= "     T1.REGDDATE, ";
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "     T1.CLASSCD, ";
                $query .= "     T1.SCHOOL_KIND, ";
                $query .= "     T1.CURRICULUM_CD, ";
            }
            $query .= "     T1.SUBCLASSCD, ";
            $query .= "     T1.SEQ ";
        } else {
            $query  = " SELECT ";
            $query .= "     T1.year, ";
            $query .= "     T1.regddate, ";
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "     T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD AS SUBCLASSCD, ";
            } else {
                $query .= "     T1.SUBCLASSCD, ";
            }
            $query .= "     CASE WHEN T1.CONDITION_DIV = '1' ";
            $query .= "               THEN '資格' ";
            $query .= "               ELSE 'その他' ";
            $query .= "          END AS CONDITION_DIV, ";
            $query .= "     T1.SEQ, ";
            $query .= "     T1.contents, ";
            $query .= "     T1.remark, ";
            $query .= "     T1.credits, ";
            $query .= "     T5.CREDITS AS CREDIT ";
            $query .= " FROM ";
            $query .= "     schreg_qualified_hobby_dat T1 ";
            $query .= "     LEFT OUTER JOIN subclass_mst T2 ON T1.subclasscd = T2.subclasscd ";
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "                                AND T1.CLASSCD       = T2.CLASSCD ";
                $query .= "                                AND T1.SCHOOL_KIND   = T2.SCHOOL_KIND ";
                $query .= "                                AND T1.CURRICULUM_CD = T2.CURRICULUM_CD ";
            }
            $query .= "     LEFT JOIN SCHREG_REGD_DAT T4 ON T4.SCHREGNO = T1.SCHREGNO ";
            $query .= "                                 AND T4.YEAR     = T1.year ";
            $query .= "                                 AND T4.SEMESTER = '".CTRL_SEMESTER."' ";
            $query .= "     LEFT JOIN CREDIT_MST T5 ON T5.YEAR       = T1.year ";
            $query .= "                            AND T5.COURSECD   = T4.COURSECD ";
            $query .= "                            AND T5.MAJORCD    = T4.MAJORCD ";
            $query .= "                            AND T5.GRADE      = T4.GRADE ";
            $query .= "                            AND T5.COURSECODE = T4.COURSECODE ";
            $query .= "                            AND T5.CLASSCD    = SUBSTR(T1.SUBCLASSCD,1,2) ";
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "                        AND T5.SCHOOL_KIND   = T1.SCHOOL_KIND ";
                $query .= "                        AND T5.CURRICULUM_CD = T1.CURRICULUM_CD ";
            }
            $query .= "                            AND T5.SUBCLASSCD = T1.SUBCLASSCD ";
            $query .= " WHERE ";
            $query .= "     T1.SCHREGNO = '{$model->schregno}' AND ";
            $query .= "     T1.REGDDATE BETWEEN '{$sdate}' AND '{$edate}' ";
            $query .= " ORDER BY  ";
            $query .= "     T1.regddate, ";
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "     T1.CLASSCD, ";
                $query .= "     T1.SCHOOL_KIND, ";
                $query .= "     T1.CURRICULUM_CD, ";
            }
            $query .= "     T1.subclasscd, ";
            $query .= "     T1.SEQ ";
        }

        return $query;
    }
}
