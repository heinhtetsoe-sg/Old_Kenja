<?php

class knjd301bQuery extends Query
{
    //学期取得
    public function getSemester($model, $semester = "")
    {
        $query  = " SELECT ";
        $query .= "     SEMESTERNAME AS LABEL, ";
        $query .= "     SEMESTER AS VALUE, ";
        $query .= "     SDATE, ";
        $query .= "     EDATE ";
        $query .= " FROM ";
        $query .= "     SEMESTER_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$model->year."' ";
        if ($semester != "") {
            $query .= " AND SEMESTER = '".$semester."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //テスト種別取得
    public function getTestKind($model)
    {
        $query  = " SELECT ";
        $query .= "     TESTKINDCD || TESTITEMCD || SCORE_DIV AS VALUE, ";
        $query .= "     TESTKINDCD || TESTITEMCD || SCORE_DIV || '：' || TESTITEMNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     TESTITEM_MST_COUNTFLG_NEW_SDIV ";
        $query .= " WHERE ";
        $query .= "     YEAR      = '{$model->year}' AND ";
        $query .= "     SEMESTER  = '{$model->semester}' ";
        if ($model->reportKind == "3") {
            // 3:成績一覧表が選択された場合は、素点のみ表示する
            $query .= " AND SCORE_DIV = '".$model->scoreDivRawScore."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //指定年度におけるクラブ一覧を取得
    public function getClubs($model, $selectedClubs = null)
    {
        $query   = " SELECT ";
        $query  .= "     CLBM.SCHOOLCD    AS SCHOOLCD, ";
        $query  .= "     CLBM.SCHOOL_KIND AS SCHOOL_KIND , ";
        $query  .= "     CLBM.CLUBCD      AS CLUBCD , ";
        $query  .= "     CLUBNAME , ";
        $query  .= "     SDATE , ";
        $query  .= "     ACTIVITY_PLACE , ";
        $query  .= "     CLUBROOM_ASSIGN ";
        $query  .= " FROM ";
        $query  .= "     CLUB_MST CLBM ";
        $query  .= " INNER JOIN  ";
        $query  .= "     CLUB_YDAT CLBY ";
        $query  .= " ON ";
        $query  .= "     CLBY.SCHOOLCD    = CLBM.SCHOOLCD ";
        $query  .= " AND CLBY.YEAR        = '{$model->year}' ";
        $query  .= " AND CLBY.CLUBCD      = CLBM.CLUBCD ";
        $query  .= " AND CLBY.SCHOOL_KIND = CLBM.SCHOOL_KIND ";
        $query  .= " WHERE ";
        $query  .= "     CLBM.SCHOOL_KIND = '{$model->schoolKind}' ";

        //クラブを限定する場合
        if (isset($selectedClubs) && is_array($selectedClubs)) {
            $query  .= " AND CLBM.CLUBCD IN ('".implode($selectedClubs, "','")."')";
        }

        $query  .= " ORDER BY CLUBCD ASC  ";

        return $query;
    }

    public function getGradeRange($model)
    {
        $query  = " SELECT ";
        $query .= "     GRADE ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_GDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$model->year."' AND ";
        $query .= "     SCHOOL_KIND = '".$model->schoolKind."' ";
        $query .= " ORDER BY ";
        $query .= "     GRADE ";
        
        return $query;
    }

    //学年別平均点を取得
    public function getGakunenbetsuHeikintenQuery($model, $gradeRangeList)
    {
        $query  = " WITH CLUB_RESULTS AS ( ";
        $query .= " SELECT ";
        $query .= "     CLUB.CLUBCD, ";
        $query .= "     CLUB.SCHOOLCD, ";
        $query .= "     CLUB.SCHOOL_KIND, ";
        $query .= "     CASE DAT.GRADE WHEN '".$gradeRangeList[1]."' THEN FLOAT(RANK.SCORE) ELSE NULL END AS G01, ";
        $query .= "     CASE DAT.GRADE WHEN '".$gradeRangeList[2]."' THEN FLOAT(RANK.SCORE) ELSE NULL END AS G02, ";
        $query .= "     CASE DAT.GRADE WHEN '".$gradeRangeList[3]."' THEN FLOAT(RANK.SCORE) ELSE NULL END AS G03, ";
        $query .= "     FLOAT(RANK.SCORE) AS AVG ";

        $query .= " FROM ";
        $query .= "     CLUB_MST CLUB ";

        $query .= "     INNER JOIN NAME_MST A023 ON ";
        $query .= "                NAMECD1 = 'A023' ";
        $query .= "            AND NAME1   = CLUB.SCHOOL_KIND ";

        $query .= "     INNER JOIN SCHREG_CLUB_HIST_DAT HIST ON ";
        $query .= "                HIST.SCHOOLCD    = CLUB.SCHOOLCD ";
        $query .= "            AND HIST.SCHOOL_KIND = CLUB.SCHOOL_KIND ";
        $query .= "            AND HIST.CLUBCD      = CLUB.CLUBCD ";
        $query .= "            AND (HIST.EDATE IS NULL OR (DATE('".CTRL_DATE."') BETWEEN HIST.SDATE AND HIST.EDATE)) ";

        $query .= "     INNER JOIN SCHREG_REGD_DAT DAT ON ";
        $query .= "                DAT.YEAR     = '".$model->year."' ";
        if ($model->semester == "9") {
            $query .= "            AND DAT.SEMESTER = '".$model->maxSemester."' ";
        } else {
            $query .= "            AND DAT.SEMESTER = '".$model->semester."' ";
        }
        $query .= "            AND DAT.SCHREGNO = HIST.SCHREGNO ";
        $query .= "            AND (DAT.GRADE BETWEEN A023.NAME2 AND A023.NAME3) ";

        $query .= "     INNER JOIN SCHREG_REGD_HDAT HDAT ON ";
        $query .= "                HDAT.YEAR     = DAT.YEAR ";
        $query .= "            AND HDAT.SEMESTER = DAT.SEMESTER ";
        $query .= "            AND HDAT.GRADE    = DAT.GRADE ";
        $query .= "            AND HDAT.HR_CLASS = DAT.HR_CLASS ";

        $query .= "     INNER JOIN SCHREG_BASE_MST BASE ON ";
        $query .= "                BASE.SCHREGNO = DAT.SCHREGNO ";

        $query .= "     INNER JOIN TESTITEM_MST_COUNTFLG_NEW_SDIV TESTITEM ON ";
        $query .= "                TESTITEM.YEAR       = DAT.YEAR ";
        $query .= "            AND TESTITEM.SEMESTER   = '".$model->semester."' ";
        $query .= "            AND TESTITEM.TESTKINDCD = '".substr($model->testKindCd, 0, 2)."' ";
        $query .= "            AND TESTITEM.TESTITEMCD = '".substr($model->testKindCd, 2, 2)."' ";
        $query .= "            AND TESTITEM.SCORE_DIV  = '".substr($model->testKindCd, 4, 2)."' ";

        $query .= "     INNER JOIN RECORD_RANK_SDIV_DAT RANK ON ";
        $query .= "                RANK.YEAR           = TESTITEM.YEAR ";
        $query .= "            AND RANK.SEMESTER       = TESTITEM.SEMESTER ";
        $query .= "            AND RANK.TESTKINDCD     = TESTITEM.TESTKINDCD ";
        $query .= "            AND RANK.TESTITEMCD     = TESTITEM.TESTITEMCD ";
        $query .= "            AND RANK.SCORE_DIV      = TESTITEM.SCORE_DIV ";
        $query .= "            AND RANK.SCHOOL_KIND    = CLUB.SCHOOL_KIND ";
        $query .= "            AND RANK.SCHREGNO       = DAT.SCHREGNO ";
        $query .= "            AND RANK.CLASSCD       <= '90' ";
        $query .= "            AND RANK.CURRICULUM_CD <> '99' ";

        $query .= " WHERE ";
        $query .= "     CLUB.SCHOOLCD    = '".$model->schoolcd."' AND ";
        $query .= "     CLUB.SCHOOL_KIND = '".$model->schoolKind."' AND ";
        $query .= "     CLUB.CLUBCD      IN ('".implode("','", $model->selectedClubs)."') ";

        $query .= " ORDER BY ";
        $query .= "     CLUB.CLUBCD ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     CLUB.CLUBCD, ";
        $query .= "     CLUB.CLUBNAME AS NAME, ";
        $query .= "     DECIMAL(INT(AVG(G01) * 100 + 0.5) / 100.0, 5, 2) AS G01, ";
        $query .= "     DECIMAL(INT(AVG(G02) * 100 + 0.5) / 100.0, 5, 2) AS G02, ";
        $query .= "     DECIMAL(INT(AVG(G03) * 100 + 0.5) / 100.0, 5, 2) AS G03, ";
        $query .= "     DECIMAL(INT(AVG(AVG) * 100 + 0.5) / 100.0, 5, 2) AS AVG ";

        $query .= " FROM ";
        $query .= "     CLUB_MST CLUB ";

        $query .= "     LEFT JOIN CLUB_RESULTS ON ";
        $query .= "               CLUB_RESULTS.CLUBCD      = CLUB.CLUBCD ";
        $query .= "           AND CLUB_RESULTS.SCHOOLCD    = CLUB.SCHOOLCD ";
        $query .= "           AND CLUB_RESULTS.SCHOOL_KIND = CLUB.SCHOOL_KIND ";

        $query .= " WHERE ";
        $query .= "     CLUB.SCHOOLCD    = '".$model->schoolcd."' AND ";
        $query .= "     CLUB.SCHOOL_KIND = '".$model->schoolKind."' AND ";
        $query .= "     CLUB.CLUBCD      IN ('".implode("','", $model->selectedClubs)."') ";

        $query .= " GROUP BY ";
        $query .= "     CLUB.CLUBCD, ";
        $query .= "     CLUB.CLUBNAME ";

        $query .= " UNION ALL ";

        $query .= " (SELECT ";
        $query .= "     '統　計' AS CLUBCD, ";
        $query .= "     '' AS NAME, ";
        $query .= "     DECIMAL(INT(AVG(G01) * 100 + 0.5) / 100.0, 5, 2) AS G01, ";
        $query .= "     DECIMAL(INT(AVG(G02) * 100 + 0.5) / 100.0, 5, 2) AS G02, ";
        $query .= "     DECIMAL(INT(AVG(G03) * 100 + 0.5) / 100.0, 5, 2) AS G03, ";
        $query .= "     DECIMAL(INT(AVG(AVG) * 100 + 0.5) / 100.0, 5, 2) AS AVG ";
        $query .= " FROM ";
        $query .= "     CLUB_RESULTS) ";

        $query .= " ORDER BY ";
        $query .= "     CLUBCD ";

        return $query;
    }

    //度数分布票を取得
    public function getDosuBunpuhyoQuery($model)
    {
        $query  = " SELECT ";
        $query .= "     DAT.GRADE, ";
        $query .= "     CLUB.CLUBCD, ";
        $query .= "     SCORE.CLASSCD || '-' || SCORE.SCHOOL_KIND || '-' || SCORE.CURRICULUM_CD || '-' || SCORE.SUBCLASSCD AS SUBCLASSCD, ";
        $query .= "     RANK.SCHREGNO, ";
        $query .= "     CASE SCORE.VALUE_DI WHEN '*' THEN 1 ELSE 0 END AS VALUE_DI, ";
        $query .= "     RANK.SCORE ";
        $query .= " FROM ";
        $query .= "     CLUB_MST CLUB ";

        $query .= "     INNER JOIN NAME_MST A023 ON ";
        $query .= "                NAMECD1 = 'A023' ";
        $query .= "            AND NAME1   = CLUB.SCHOOL_KIND ";

        $query .= "     INNER JOIN SCHREG_CLUB_HIST_DAT HIST ON ";
        $query .= "                HIST.SCHOOLCD    = CLUB.SCHOOLCD ";
        $query .= "            AND HIST.SCHOOL_KIND = CLUB.SCHOOL_KIND ";
        $query .= "            AND HIST.CLUBCD      = CLUB.CLUBCD ";
        $query .= "            AND (HIST.EDATE IS NULL OR (DATE('".CTRL_DATE."') BETWEEN HIST.SDATE AND HIST.EDATE)) ";

        $query .= "     INNER JOIN SCHREG_REGD_DAT DAT ON ";
        $query .= "                DAT.YEAR     = '".$model->year."' ";
        if ($model->semester == "9") {
            $query .= "            AND DAT.SEMESTER = '".$model->maxSemester."' ";
        } else {
            $query .= "            AND DAT.SEMESTER = '".$model->semester."' ";
        }
        $query .= "            AND DAT.SCHREGNO = HIST.SCHREGNO ";
        $query .= "            AND (DAT.GRADE BETWEEN A023.NAME2 AND A023.NAME3) ";

        $query .= "     INNER JOIN TESTITEM_MST_COUNTFLG_NEW_SDIV TESTITEM ON ";
        $query .= "                TESTITEM.YEAR       = DAT.YEAR ";
        $query .= "            AND TESTITEM.SEMESTER   = '".$model->semester."' ";
        $query .= "            AND TESTITEM.TESTKINDCD = '".substr($model->testKindCd, 0, 2)."' ";
        $query .= "            AND TESTITEM.TESTITEMCD = '".substr($model->testKindCd, 2, 2)."' ";
        $query .= "            AND TESTITEM.SCORE_DIV  = '".substr($model->testKindCd, 4, 2)."' ";

        $query .= "     INNER JOIN RECORD_SCORE_DAT SCORE ON ";
        $query .= "                SCORE.YEAR          = TESTITEM.YEAR ";
        $query .= "            AND SCORE.SEMESTER      = TESTITEM.SEMESTER ";
        $query .= "            AND SCORE.TESTKINDCD    = TESTITEM.TESTKINDCD ";
        $query .= "            AND SCORE.TESTITEMCD    = TESTITEM.TESTITEMCD ";
        $query .= "            AND SCORE.SCORE_DIV     = TESTITEM.SCORE_DIV ";
        $query .= "            AND SCORE.SCHOOL_KIND   = CLUB.SCHOOL_KIND ";
        $query .= "            AND SCORE.SCHREGNO      = DAT.SCHREGNO ";

        $query .= "     LEFT JOIN RECORD_RANK_SDIV_DAT RANK ON ";
        $query .= "                RANK.YEAR          = SCORE.YEAR ";
        $query .= "            AND RANK.SEMESTER      = SCORE.SEMESTER ";
        $query .= "            AND RANK.TESTKINDCD    = SCORE.TESTKINDCD ";
        $query .= "            AND RANK.TESTITEMCD    = SCORE.TESTITEMCD ";
        $query .= "            AND RANK.SCORE_DIV     = SCORE.SCORE_DIV ";
        $query .= "            AND RANK.SCHOOL_KIND   = SCORE.SCHOOL_KIND ";
        $query .= "            AND RANK.CURRICULUM_CD = SCORE.CURRICULUM_CD ";
        $query .= "            AND RANK.SUBCLASSCD    = SCORE.SUBCLASSCD ";
        $query .= "            AND RANK.SCHREGNO      = SCORE.SCHREGNO ";

        $query .= " WHERE ";
        $query .= "     CLUB.SCHOOLCD    = '".$model->schoolcd."' AND ";
        $query .= "     CLUB.SCHOOL_KIND = '".$model->schoolKind."' AND ";
        $query .= "     CLUB.CLUBCD      IN ('".implode("','", $model->selectedClubs)."') ";

        $query .= " ORDER BY ";
        $query .= "     DAT.GRADE, ";
        $query .= "     CLUB.CLUBCD, ";
        $query .= "     SCORE.CLASSCD, ";
        $query .= "     SCORE.SCHOOL_KIND, ";
        $query .= "     SCORE.CURRICULUM_CD, ";
        $query .= "     SCORE.SUBCLASSCD ";

        return $query;
    }

    //度数分布表の刻み幅欄を取得
    public function getDosuBunpuhyoTickWidthQuery($model)
    {
        $query  = " WITH TICK_WIDTH AS ( ";
        $query .= knjd301bQuery::getDosuBunpuhyoQuery($model);
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     GRADE, ";
        $query .= "     CLUBCD, ";
        $query .= "     SUBCLASSCD ";

        if ($model->scoreDivRating == substr($model->testKindCd, 4, 2)) {
            for ($idx = 5, $id = 1; 0 < $idx; $idx--, $id++) {
                $query .= "     ,COUNT(CASE SCORE WHEN ".$idx." THEN SCHREGNO ELSE NULL END) AS COUNT".$id." ";
            }
        } else {
            for ($idx = 100, $id = 1; 0 < $idx; $idx -= 5, $id++) {
                $low = $id == 1 ? $idx - 5 : $idx - 4;
                $query .= "     ,COUNT(CASE WHEN ".$low." <= SCORE AND SCORE <=".$idx." THEN SCHREGNO ELSE NULL END) AS COUNT".$id." ";
                if ($id == 1) {
                    $idx--;
                }
            }
        }

        $query .= " FROM ";
        $query .= "     TICK_WIDTH ";

        $query .= " GROUP BY ";
        $query .= "     GRADE, ";
        $query .= "     CLUBCD, ";
        $query .= "     SUBCLASSCD ";

        $query .= " ORDER BY ";
        $query .= "     GRADE, ";
        $query .= "     CLUBCD, ";
        $query .= "     SUBCLASSCD ";

        return $query;
    }

    //度数分布表の集計欄（未受験者数、受験者数～最低点）を取得
    public function getDosuBunpuhyoStatisticsQuery($model)
    {
        $query  = " WITH STATISTICS AS ( ";
        $query .= knjd301bQuery::getDosuBunpuhyoQuery($model);
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     GRADE, ";
        $query .= "     CLUBCD, ";
        $query .= "     SUBCLASSCD, ";
        $query .= "     COUNT(SCHREGNO) AS COUNT, ";
        $query .= "     SUM(VALUE_DI) AS VALUE_DI_COUNT, ";
        $query .= "     SUM(SCORE) AS SCORE, ";
        $query .= "     DECIMAL(INT(AVG(FLOAT(SCORE)) * 100 + 0.5) / 100.0, 5, 2) AS AVG, ";
        $query .= "     DECIMAL(INT(STDDEV(FLOAT(SCORE)) * 100 + 0.5) / 100.0, 5, 2) AS STDDEV, ";
        $query .= "     MAX(SCORE) AS MAX, ";
        $query .= "     MIN(SCORE) AS MIN ";

        $query .= " FROM ";
        $query .= "     STATISTICS ";

        $query .= " GROUP BY ";
        $query .= "     GRADE, ";
        $query .= "     CLUBCD, ";
        $query .= "     SUBCLASSCD ";

        $query .= " ORDER BY ";
        $query .= "     GRADE, ";
        $query .= "     CLUBCD, ";
        $query .= "     SUBCLASSCD ";
        
        return $query;
    }

    //成績一覧表を取得
    public function getBukatsudoubetsuSeisekiIchiranhyoQuery($model)
    {
        $query  = " SELECT ";
        $query .= "     DAT.GRADE, ";
        $query .= "     CLUB.CLUBCD, ";
        $query .= "     RANK.CLASSCD || '-' || RANK.SCHOOL_KIND || '-' || RANK.CURRICULUM_CD || '-' || RANK.SUBCLASSCD AS SUBCLASSCD, ";
        $query .= "     HDAT.HR_NAMEABBV, ";
        $query .= "     HIST.SCHREGNO, ";
        $query .= "     BASE.NAME, ";
        $query .= "     SUM(CASE TESTITEM.SCORE_DIV WHEN '".$model->scoreDivRawScore."' THEN RANK.SCORE ELSE NULL END) AS RAW_SCORE, ";
        $query .= "     SUM(CASE TESTITEM.SCORE_DIV WHEN '".$model->scoreDivResults."' THEN RANK.SCORE ELSE NULL END) AS RESULTS, ";
        $query .= "     SUM(CASE TESTITEM.SCORE_DIV WHEN '".$model->scoreDivRating."' THEN RANK.SCORE ELSE NULL END) AS RATING ";
        $query .= " FROM ";
        $query .= "     CLUB_MST CLUB ";

        $query .= "     INNER JOIN NAME_MST A023 ON ";
        $query .= "                NAMECD1 = 'A023' ";
        $query .= "            AND NAME1   = CLUB.SCHOOL_KIND ";

        $query .= "     INNER JOIN SCHREG_CLUB_HIST_DAT HIST ON ";
        $query .= "                HIST.SCHOOLCD    = CLUB.SCHOOLCD ";
        $query .= "            AND HIST.SCHOOL_KIND = CLUB.SCHOOL_KIND ";
        $query .= "            AND HIST.CLUBCD      = CLUB.CLUBCD ";
        $query .= "            AND (HIST.EDATE IS NULL OR (DATE('".CTRL_DATE."') BETWEEN HIST.SDATE AND HIST.EDATE)) ";

        $query .= "     INNER JOIN SCHREG_REGD_DAT DAT ON ";
        $query .= "                DAT.YEAR     = '".$model->year."' ";
        if ($model->semester == $model->semeAll) {
            $query .= "            AND DAT.SEMESTER = '".$model->maxSemester."' ";
        } else {
            $query .= "            AND DAT.SEMESTER = '".$model->semester."' ";
        }
        $query .= "            AND DAT.SCHREGNO = HIST.SCHREGNO ";
        $query .= "            AND (DAT.GRADE BETWEEN A023.NAME2 AND A023.NAME3) ";

        $query .= "     INNER JOIN SCHREG_REGD_HDAT HDAT ON ";
        $query .= "                HDAT.YEAR     = DAT.YEAR ";
        $query .= "            AND HDAT.SEMESTER = DAT.SEMESTER ";
        $query .= "            AND HDAT.GRADE    = DAT.GRADE ";
        $query .= "            AND HDAT.HR_CLASS = DAT.HR_CLASS ";

        $query .= "     INNER JOIN SCHREG_BASE_MST BASE ON ";
        $query .= "                BASE.SCHREGNO = DAT.SCHREGNO ";

        $query .= "     INNER JOIN TESTITEM_MST_COUNTFLG_NEW_SDIV TESTITEM ON ";
        $query .= "                TESTITEM.YEAR       = DAT.YEAR ";
        $query .= "            AND TESTITEM.SEMESTER   = '".$model->semester."' ";

        if ($model->printPattern == $model->printPatternSpecify) {
            $query .= "            AND TESTITEM.TESTKINDCD || TESTITEM.TESTITEMCD || TESTITEM.SCORE_DIV = '".$model->testKindCd."' ";
        } elseif ($model->printPattern == $model->printPatternAll) {
            $query .= "            AND ( ";
            $query .= "                     TESTITEM.TESTKINDCD || TESTITEM.TESTITEMCD || TESTITEM.SCORE_DIV = '".$model->testKindCd."' ";
            $query .= "                  OR TESTITEM.TESTKINDCD || TESTITEM.TESTITEMCD || TESTITEM.SCORE_DIV = '".$model->scoreDivKimatsuResults."' ";
            $query .= "                  OR TESTITEM.TESTKINDCD || TESTITEM.TESTITEMCD || TESTITEM.SCORE_DIV = '".$model->scoreDivKimatsuRating."' ";
            $query .= "                ) ";
        } elseif ($model->printPattern == $model->printPatternKimatuOnly) {
            $query .= "            AND ( ";
            $query .= "                     TESTITEM.TESTKINDCD || TESTITEM.TESTITEMCD || TESTITEM.SCORE_DIV = '".$model->scoreDivKimatsuResults."' ";
            $query .= "                  OR TESTITEM.TESTKINDCD || TESTITEM.TESTITEMCD || TESTITEM.SCORE_DIV = '".$model->scoreDivKimatsuRating."' ";
            $query .= "                ) ";
        }

        $query .= "     INNER JOIN RECORD_RANK_SDIV_DAT RANK ON ";
        $query .= "                RANK.YEAR           = TESTITEM.YEAR ";
        $query .= "            AND RANK.SEMESTER       = TESTITEM.SEMESTER ";
        $query .= "            AND RANK.TESTKINDCD     = TESTITEM.TESTKINDCD ";
        $query .= "            AND RANK.TESTITEMCD     = TESTITEM.TESTITEMCD ";
        $query .= "            AND RANK.SCORE_DIV      = TESTITEM.SCORE_DIV ";
        $query .= "            AND RANK.SCHOOL_KIND    = CLUB.SCHOOL_KIND ";
        $query .= "            AND RANK.SCHREGNO       = DAT.SCHREGNO ";
        $query .= "            AND RANK.CLASSCD       <= '90' ";
        $query .= "            AND RANK.CURRICULUM_CD <> '99' ";

        $query .= " WHERE ";
        $query .= "     CLUB.SCHOOLCD    = '".$model->schoolcd."' AND ";
        $query .= "     CLUB.SCHOOL_KIND = '".$model->schoolKind."' AND ";
        $query .= "     CLUB.CLUBCD      IN ('".implode("','", $model->selectedClubs)."') ";

        $query .= " GROUP BY ";
        $query .= "     DAT.GRADE, ";
        $query .= "     CLUB.CLUBCD, ";
        $query .= "     RANK.CLASSCD, ";
        $query .= "     RANK.SCHOOL_KIND, ";
        $query .= "     RANK.CURRICULUM_CD, ";
        $query .= "     RANK.SUBCLASSCD, ";
        $query .= "     HDAT.HR_CLASS, ";
        $query .= "     HDAT.HR_NAMEABBV, ";
        $query .= "     HIST.SCHREGNO, ";
        $query .= "     BASE.NAME ";

        $query .= " ORDER BY ";
        $query .= "     DAT.GRADE, ";
        $query .= "     CLUB.CLUBCD, ";
        $query .= "     RANK.CLASSCD, ";
        $query .= "     RANK.SCHOOL_KIND, ";
        $query .= "     RANK.CURRICULUM_CD, ";
        $query .= "     RANK.SUBCLASSCD, ";
        $query .= "     HDAT.HR_CLASS, ";
        $query .= "     HIST.SCHREGNO ";

        return $query;
    }

    public function getGradeQuery($model)
    {
        $query  = " SELECT ";
        $query .= "     GRADE, ";
        $query .= "     GRADE_CD ";

        $query .= " FROM ";
        $query .= "     SCHREG_REGD_GDAT ";

        $query .= " WHERE ";
        $query .= "     YEAR = '".$model->year."' AND ";
        $query .= "     SCHOOL_KIND = '".$model->schoolKind."' ";

        $query .= " ORDER BY ";
        $query .= "     GRADE ";

        return $query;
    }

    public function getClubQuery($model)
    {
        $query  = " SELECT ";
        $query .= "     CLUBCD, ";
        $query .= "     CLUBNAME ";

        $query .= " FROM ";
        $query .= "     CLUB_MST ";

        $query .= " WHERE ";
        $query .= "     SCHOOLCD    = '".$model->schoolcd."' AND ";
        $query .= "     SCHOOL_KIND = '".$model->schoolKind."' AND ";
        $query .= "     CLUBCD      IN ('".implode("','", $model->selectedClubs)."') ";

        $query .= " ORDER BY ";
        $query .= "     CLUBCD ";

        return $query;
    }

    /**
     * 成績（RECORD_RANK_SDIV_DAT）がある科目のみ取得する
     */
    public function getSubclassQuery($model)
    {
        $query  = " SELECT ";
        $query .= "     SUB.CLASSCD || '-' || SUB.SCHOOL_KIND || '-' || SUB.CURRICULUM_CD || '-' || SUB.SUBCLASSCD AS SUBCLASSCD, ";
        $query .= "     SUB.SUBCLASSNAME, ";
        $query .= "     SUB.SUBCLASSABBV ";
        $query .= " FROM ";
        $query .= "     CLUB_MST CLUB ";

        $query .= "     INNER JOIN NAME_MST A023 ON ";
        $query .= "                NAMECD1 = 'A023' ";
        $query .= "            AND NAME1   = CLUB.SCHOOL_KIND ";

        $query .= "     INNER JOIN SCHREG_CLUB_HIST_DAT HIST ON ";
        $query .= "                HIST.SCHOOLCD    = CLUB.SCHOOLCD ";
        $query .= "            AND HIST.SCHOOL_KIND = CLUB.SCHOOL_KIND ";
        $query .= "            AND HIST.CLUBCD      = CLUB.CLUBCD ";
        $query .= "            AND (HIST.EDATE IS NULL OR (DATE('".CTRL_DATE."') BETWEEN HIST.SDATE AND HIST.EDATE)) ";

        $query .= "     INNER JOIN SCHREG_REGD_DAT DAT ON ";
        $query .= "                DAT.YEAR     = '".$model->year."' ";
        if ($model->semester == $model->semeAll) {
            $query .= "            AND DAT.SEMESTER = '".$model->maxSemester."' ";
        } else {
            $query .= "            AND DAT.SEMESTER = '".$model->semester."' ";
        }
        $query .= "            AND DAT.SCHREGNO = HIST.SCHREGNO ";
        $query .= "            AND (DAT.GRADE BETWEEN A023.NAME2 AND A023.NAME3) ";

        $query .= "     INNER JOIN SCHREG_REGD_HDAT HDAT ON ";
        $query .= "                HDAT.YEAR     = DAT.YEAR ";
        $query .= "            AND HDAT.SEMESTER = DAT.SEMESTER ";
        $query .= "            AND HDAT.GRADE    = DAT.GRADE ";
        $query .= "            AND HDAT.HR_CLASS = DAT.HR_CLASS ";

        $query .= "     INNER JOIN SCHREG_BASE_MST BASE ON ";
        $query .= "                BASE.SCHREGNO = DAT.SCHREGNO ";

        $query .= "     INNER JOIN TESTITEM_MST_COUNTFLG_NEW_SDIV TESTITEM ON ";
        $query .= "                TESTITEM.YEAR       = DAT.YEAR ";
        $query .= "            AND TESTITEM.SEMESTER   = '".$model->semester."' ";
        if ($model->printPattern == $model->printPatternSpecify) {
            $query .= "            AND TESTITEM.TESTKINDCD || TESTITEM.TESTITEMCD || TESTITEM.SCORE_DIV = '".$model->testKindCd."' ";
        } elseif ($model->printPattern == $model->printPatternAll) {
            $query .= "            AND ( ";
            $query .= "                     TESTITEM.TESTKINDCD || TESTITEM.TESTITEMCD || TESTITEM.SCORE_DIV = '".$model->testKindCd."' ";
            $query .= "                  OR TESTITEM.TESTKINDCD || TESTITEM.TESTITEMCD || TESTITEM.SCORE_DIV = '".$model->scoreDivKimatsuResults."' ";
            $query .= "                  OR TESTITEM.TESTKINDCD || TESTITEM.TESTITEMCD || TESTITEM.SCORE_DIV = '".$model->scoreDivKimatsuRating."' ";
            $query .= "                ) ";
        } elseif ($model->printPattern == $model->printPatternKimatuOnly) {
            $query .= "            AND ( ";
            $query .= "                     TESTITEM.TESTKINDCD || TESTITEM.TESTITEMCD || TESTITEM.SCORE_DIV = '".$model->scoreDivKimatsuResults."' ";
            $query .= "                  OR TESTITEM.TESTKINDCD || TESTITEM.TESTITEMCD || TESTITEM.SCORE_DIV = '".$model->scoreDivKimatsuRating."' ";
            $query .= "                ) ";
        }

        $query .= "     INNER JOIN RECORD_RANK_SDIV_DAT RANK ON ";
        $query .= "                RANK.YEAR           = TESTITEM.YEAR ";
        $query .= "            AND RANK.SEMESTER       = TESTITEM.SEMESTER ";
        $query .= "            AND RANK.TESTKINDCD     = TESTITEM.TESTKINDCD ";
        $query .= "            AND RANK.TESTITEMCD     = TESTITEM.TESTITEMCD ";
        $query .= "            AND RANK.SCORE_DIV      = TESTITEM.SCORE_DIV ";
        $query .= "            AND RANK.SCHOOL_KIND    = CLUB.SCHOOL_KIND ";
        $query .= "            AND RANK.SCHREGNO       = DAT.SCHREGNO ";
        $query .= "            AND RANK.CLASSCD       <= '90' ";
        $query .= "            AND RANK.CURRICULUM_CD <> '99' ";

        $query .= "     INNER JOIN SUBCLASS_MST SUB ON ";
        $query .= "                SUB.CLASSCD       = RANK.CLASSCD ";
        $query .= "            AND SUB.SCHOOL_KIND   = RANK.SCHOOL_KIND ";
        $query .= "            AND SUB.CURRICULUM_CD = RANK.CURRICULUM_CD ";
        $query .= "            AND SUB.SUBCLASSCD    = RANK.SUBCLASSCD ";

        $query .= " WHERE ";
        $query .= "     CLUB.SCHOOLCD    = '".$model->schoolcd."' AND ";
        $query .= "     CLUB.SCHOOL_KIND = '".$model->schoolKind."' AND ";
        $query .= "     CLUB.CLUBCD      IN ('".implode("','", $model->selectedClubs)."') ";

        $query .= " GROUP BY ";
        $query .= "     SUB.CLASSCD, ";
        $query .= "     SUB.SCHOOL_KIND, ";
        $query .= "     SUB.CURRICULUM_CD, ";
        $query .= "     SUB.SUBCLASSCD, ";
        $query .= "     SUB.SUBCLASSNAME, ";
        $query .= "     SUB.SUBCLASSABBV ";

        $query .= " ORDER BY ";
        $query .= "     SUB.CLASSCD, ";
        $query .= "     SUB.SCHOOL_KIND, ";
        $query .= "     SUB.CURRICULUM_CD, ";
        $query .= "     SUB.SUBCLASSCD ";

        return $query;
    }

    public function getMaxSemesterQuery($model)
    {
        $query  = " SELECT ";
        $query .= "     MAX(SEMESTER) AS SEMESTER ";
        $query .= " FROM ";
        $query .= "     SEMESTER_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$model->year."' ";
        $query .= " AND SEMESTER <> '9' ";
        $query .= " GROUP BY ";
        $query .= "     YEAR ";

        return $query;
    }
}
