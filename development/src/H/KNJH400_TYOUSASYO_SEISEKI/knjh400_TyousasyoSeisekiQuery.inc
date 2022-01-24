<?php

require_once('for_php7.php');

class knjh400_TyousasyoSeisekiQuery extends Query
{

    //生徒名取得
    public function getName($model)
    {
        $query .= " SELECT ";
        $query .= "     NAME_SHOW ";
        $query .= " FROM ";
        $query .= "     SCHREG_BASE_MST ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO = '{$model->schregno}' ";

        return $query;
    }
    //ABC評定科目
    public function getABCHyoutei($fields)
    {
        $henkanSub = $fields["CURRICULUM_CD"]."-".$fields["CLASSCD"].$fields["SUBCLASSCD"];
        list($curri, $classCd, $kind, $subclassCd) = preg_split("/-/", $henkanSub);
        $setSub = $classCd."-".$kind."-".$curri."-".$subclassCd;

        $query  = " SELECT ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = 'D065' ";
        $query .= "     AND NAME1 = '{$setSub}' ";


        return $query;
    }

    //評定
    public function getD001($hyoutei)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = 'D001' ";
        $query .= "     AND NAMECD2 = '{$hyoutei}' ";

        return $query;
    }

    public function getCurriculum()
    {
        $query  = " SELECT ";
        $query .= "   NAMECD2 AS VALUE, ";
        $query .= "   NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "   NAME_MST ";
        $query .= " WHERE ";
        $query .= "   NAMECD1 = 'Z018' ";
        $query .= " ORDER BY ";
        $query .= "   VALUE ";
        return $query;
    }

    public function getGenkyuu()
    {
        $query  = " SELECT ";
        $query .= "   VALUE(REMARK6, '0') AS REMARK6 ";
        $query .= " FROM ";
        $query .= "   CERTIF_SCHOOL_DAT ";
        $query .= " WHERE ";
        $query .= "   YEAR = '".CTRL_YEAR."' ";
        $query .= "   AND CERTIF_KINDCD = '107' ";
        return $query;
    }

    public function getSchoolMst()
    {
        $query  = " SELECT ";
        $query .= "   VALUE(SCHOOLDIV, '0') AS SCHOOLDIV ";
        $query .= " FROM ";
        $query .= "   SCHOOL_MST ";
        $query .= " WHERE ";
        $query .= "   YEAR = '".CTRL_YEAR."' ";
        return $query;
    }

    //指導要録データよりデータを取得
    public function selectQuery($model, $sortDiv = "", $checkColorDiv = "")
    {
        $query  = " WITH REGD AS ( ";
        $query .= " SELECT ";
        $query .= "   YEAR, ";
        $query .= "   SCHREGNO, ";
        $query .= "   MAX(GRADE) AS GRADE";
        $query .= " FROM ";
        $query .= "   SCHREG_REGD_DAT ";
        $query .= " WHERE ";
        $query .= "   SCHREGNO = '" .$model->schregno ."' ";
        $query .= " GROUP BY ";
        $query .= "   YEAR, ";
        $query .= "   SCHREGNO ";
        $query .= " ), NOT_RYUNEN AS ( ";
        $query .= "   SELECT ";
        $query .= "     GRADE, ";
        $query .= "     SCHREGNO, ";
        $query .= "     MAX(YEAR) AS YEAR";
        $query .= "   FROM ";
        $query .= "     (SELECT DISTINCT SCHREGNO, YEAR, GRADE FROM SCHREG_REGD_DAT ";
        $query .= "      WHERE ";
        $query .= "      SCHREGNO = '" .$model->schregno ."' ";
        $query .= "     ) T1 ";
        $query .= "   GROUP BY ";
        $query .= "     GRADE, ";
        $query .= "     SCHREGNO ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "   T1.*, ";
        $query .= "   REGD.GRADE, ";
        $query .= "   CASE WHEN NOT_RYUNEN.YEAR IS NULL THEN '1' END AS RYUNEN_FLG, ";
        $query .= "   DETAIL.REMARK1 AS YUUKOU_FLG, ";
        $query .= "   T2.CLASSNAME, ";
        $query .= "   T3.SUBCLASSCD as CHECKSUBCD,";
        $query .= "   T3.SUBCLASSNAME AS SUBCLASSNAME_M,";
        $query .= "   T3.SUBCLASSABBV AS SUBCLASSABBV_M,";
        $query .= "   T3.SUBCLASSNAME_ENG AS SUBCLASSNAME_ENG_M,";
        $query .= "   T3.SUBCLASSABBV_ENG AS SUBCLASSABBV_ENG_M,";
        if ($model->Properties["useProvFlg"] == '1') {
            $query .= "   T5.PROV_FLG, ";
        }
        $query .= "   T4.REMARK ";
        $query .= " FROM ";
        $query .= "   CLASS_MST T2, ";
        $query .= "   SCHREG_STUDYREC_DAT T1 ";
        $query .= "   LEFT JOIN REGD ON T1.YEAR = REGD.YEAR ";
        $query .= "        AND T1.SCHREGNO = REGD.SCHREGNO ";
        $query .= "   LEFT JOIN NOT_RYUNEN ON REGD.GRADE = NOT_RYUNEN.GRADE ";
        $query .= "        AND T1.SCHREGNO = NOT_RYUNEN.SCHREGNO ";
        $query .= "        AND T1.YEAR = NOT_RYUNEN.YEAR ";
        $query .= "   LEFT JOIN SCHREG_STUDYREC_DETAIL_DAT DETAIL ON T1.SCHOOLCD = DETAIL.SCHOOLCD ";
        $query .= "                   AND T1.YEAR = DETAIL.YEAR ";
        $query .= "                   AND T1.SCHREGNO = DETAIL.SCHREGNO ";
        $query .= "                   AND T1.CLASSCD = DETAIL.CLASSCD ";
        $query .= "                   AND T1.SCHOOL_KIND = DETAIL.SCHOOL_KIND ";
        $query .= "                   AND T1.CURRICULUM_CD = DETAIL.CURRICULUM_CD ";
        $query .= "                   AND T1.SUBCLASSCD = DETAIL.SUBCLASSCD ";
        $query .= "                   AND DETAIL.SEQ = '002' ";
        $query .= "   LEFT OUTER JOIN STUDYRECREMARK_DAT T4 ON T1.YEAR = T4.YEAR ";
        $query .= "                   AND T1.SCHREGNO = T4.SCHREGNO ";
        $query .= "                   AND T1.CLASSCD = T4.CLASSCD ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "               AND T1.SCHOOL_KIND = T4.SCHOOL_KIND ";
            $query .= "               AND T1.CURRICULUM_CD = T4.CURRICULUM_CD ";
        }
        $query .= "                   AND T1.SUBCLASSCD = T4.SUBCLASSCD ";
        $query .= "   LEFT JOIN SUBCLASS_MST T3 ON T1.SUBCLASSCD = T3.SUBCLASSCD ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "               AND T1.CLASSCD = T3.CLASSCD ";
            $query .= "               AND T1.SCHOOL_KIND = T3.SCHOOL_KIND ";
            $query .= "               AND T1.CURRICULUM_CD = T3.CURRICULUM_CD ";
        }
        if ($model->Properties["useProvFlg"] == '1') {
            $query .= "   LEFT JOIN STUDYREC_PROV_FLG_DAT T5 ON T1.SCHOOLCD = T5.SCHOOLCD ";
            $query .= "                                     AND T1.YEAR     = T5.YEAR ";
            $query .= "                                     AND T1.SCHREGNO  = T5.SCHREGNO ";
            $query .= "                                     AND T1.SUBCLASSCD  = T5.SUBCLASSCD ";
            //教育課程対応
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "               AND T1.CLASSCD = T5.CLASSCD ";
                $query .= "               AND T1.SCHOOL_KIND = T5.SCHOOL_KIND ";
                $query .= "               AND T1.CURRICULUM_CD = T5.CURRICULUM_CD ";
            }
        }
        $query .= " WHERE ";
        if ($model->cmd == "edit") {
            $query .= "   T1.YEAR   = '" .$model->editYear ."' ";
            $query .= "   AND T1.ANNUAL   = '" .$model->annual ."' ";
            $query .= "   AND ";
            //教育課程対応
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "   T1.CLASSCD || '-' || ";
                $query .= "   T1.SCHOOL_KIND || '-' || ";
                $query .= "   T1.CURRICULUM_CD || '-' || ";
            }
            $query .= "       T1.SUBCLASSCD   = '" .$model->subclasscd ."' ";
            $query .= "   AND ";
        }
        $query .= "   T1.SCHREGNO = '" .$model->schregno ."' ";
        $query .= "   AND T1.CLASSCD = T2.CLASSCD ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "   AND T1.SCHOOL_KIND = T2.SCHOOL_KIND ";
        }
        if ($checkColorDiv == "CHECK_COLOR") {
            $query .= "   AND T1.SCHOOLCD <> '1' ";
        }
        if ($sortDiv == "HENSYU") {
            $query .= " ORDER BY ";
            $query .= "     T1.CLASSCD, ";
            $query .= "     T1.SCHOOL_KIND, ";
            $query .= "     T1.CURRICULUM_CD, ";
            $query .= "     T1.SUBCLASSCD, ";
            $query .= "     T1.YEAR ";
        } elseif (is_array($model->sort)) {
            $query .= " ORDER BY ";
            foreach ($model->sort as $field => $order) {
                switch ($field) {
                    case "YEAR":    //年度順
                        $query .= " T1.YEAR";
                        $query .= ($order > 0)? " ASC, " : " DESC, ";
                        //教育課程対応
                        if ($model->Properties["useCurriculumcd"] == '1') {
                            $query .= "   T1.CLASSCD || '-' || ";
                            $query .= "   T1.SCHOOL_KIND || '-' || ";
                            $query .= "   T1.CURRICULUM_CD || '-' || ";
                        }
                        $query .= " T1.SUBCLASSCD";
                        break 2;
                    case "SUBCLASSCD":    //科目順
                        $query .= " T1.YEAR, ";
                        //教育課程対応
                        if ($model->Properties["useCurriculumcd"] == '1') {
                            $query .= "   T1.CLASSCD || '-' || ";
                            $query .= "   T1.SCHOOL_KIND || '-' || ";
                            $query .= "   T1.CURRICULUM_CD || '-' || ";
                        }
                        $query .= " T1.SUBCLASSCD";
                        $query .= ($order > 0)? " ASC " : " DESC ";
                        break 2;
                }
            }
        } else {
            $query .= "   T1.YEAR, ";
            //教育課程対応
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "   T1.CLASSCD || '-' || ";
                $query .= "   T1.SCHOOL_KIND || '-' || ";
                $query .= "   T1.CURRICULUM_CD || '-' || ";
            }
            $query .= "   T1.SUBCLASSCD ";
        }

        return $query;
    }

    public function getMaxRegdYear($model)
    {
        $query  = " WITH REGD AS ( ";
        $query .= " SELECT ";
        $query .= "   SCHREGNO, ";
        $query .= "   MAX(GRADE) AS GRADE";
        $query .= " FROM ";
        $query .= "   SCHREG_REGD_DAT ";
        $query .= " WHERE ";
        $query .= "   YEAR = '" .$model->editYear ."' ";
        $query .= "   AND SCHREGNO = '" .$model->schregno ."' ";
        $query .= " GROUP BY ";
        $query .= "   YEAR, ";
        $query .= "   SCHREGNO ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "   MAX(YEAR) AS YEAR";
        $query .= " FROM ";
        $query .= "   SCHREG_REGD_DAT T1, ";
        $query .= "   REGD ";
        $query .= " WHERE ";
        $query .= "   T1.SCHREGNO = REGD.SCHREGNO ";
        $query .= "   AND T1.GRADE = REGD.GRADE ";
        return $query;
    }

    //対応者を取得
    public function selectEnt($schregno)
    {
        $query = " SELECT ";
        $query .= "   NAME1 ";
        $query .= " FROM ";
        $query .= "   SCHREG_BASE_MST ";
        $query .= "   LEFT JOIN NAME_MST ON NAMECD1 = 'A002' AND NAMECD2 = ENT_DIV";
        $query .= " WHERE ";
        $query .= "   SCHREGNO = '" .$schregno."' ";
        $query .= "   AND ENT_DIV IN ('4','5') ";
        return $query;
    }

    //対応者を取得・・・未使用？
    public function selectSubclassQuery($year)
    {
        $query = " SELECT ";
        $query .= "   * ";
        $query .= " FROM ";
        $query .= "   V_SUBCLASS_MST ";
        $query .= " WHERE ";
        $query .= "   YEAR = '" .$year."' ";
        $query .= " ORDER BY ";
        $query .= "   SUBCLASSCD ";
        return $query;
    }

    //学校マスタ取得
    public function selectSchoolMstQuery($year)
    {
        $query  = " SELECT ";
        $query .= "   * ";
        $query .= " FROM ";
        $query .= "   SCHOOL_MST ";
        $query .= " WHERE ";
        $query .= "   YEAR = '".$year."' ";

        return $query;
    }

    public function getStudyRecCnt($row, $schoolcd, $model)
    {
        $query  = " SELECT ";
        $query .= "   COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "   SCHREG_STUDYREC_DAT ";
        $query .= " WHERE ";
        $query .= "   SCHOOLCD = '{$schoolcd}' ";
        $query .= "   AND YEAR = '{$row["YEAR"]}' ";
        $query .= "   AND SCHREGNO = '{$row["SCHREGNO"]}' ";
        $query .= "   AND ANNUAL = '{$row["ANNUAL"]}' ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $subclassCd = $row["CLASSCD"].$row["SUBCLASSCD"];
            $query .= "   AND CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD = '{$subclassCd}' ";
        } else {
            $query .= "   AND CLASSCD = '{$row["CLASSCD"]}' ";
            $subclassCd = $row["CLASSCD"].$row["SUBCLASSCD"];
            $query .= "   AND SUBCLASSCD = '{$subclassCd}' ";
        }
        return $query;
    }

    //履歴一覧・・・参考SQLはselectQuery()
    public function getListRireki($model)
    {
        $query  = " WITH REGD AS ( ";
        $query .= " SELECT ";
        $query .= "   YEAR, ";
        $query .= "   SCHREGNO, ";
        $query .= "   MAX(GRADE) AS GRADE";
        $query .= " FROM ";
        $query .= "   SCHREG_REGD_DAT ";
        $query .= " WHERE ";
        $query .= "   SCHREGNO = '" .$model->schregno ."' ";
        $query .= " GROUP BY ";
        $query .= "   YEAR, ";
        $query .= "   SCHREGNO ";
        $query .= " ), NOT_RYUNEN AS ( ";
        $query .= "   SELECT ";
        $query .= "     GRADE, ";
        $query .= "     SCHREGNO, ";
        $query .= "     MAX(YEAR) AS YEAR";
        $query .= "   FROM ";
        $query .= "     (SELECT DISTINCT SCHREGNO, YEAR, GRADE FROM SCHREG_REGD_DAT ";
        $query .= "      WHERE ";
        $query .= "      SCHREGNO = '" .$model->schregno ."' ";
        $query .= "     ) T1 ";
        $query .= "   GROUP BY ";
        $query .= "     GRADE, ";
        $query .= "     SCHREGNO ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "   T1.*, ";
        $query .= "   L1.STAFFNAME, ";
        $query .= "   REGD.GRADE, ";
        $query .= "   CASE WHEN NOT_RYUNEN.YEAR IS NULL THEN '1' END AS RYUNEN_FLG, ";
        $query .= "   DETAIL.REMARK1 AS YUUKOU_FLG, ";
        $query .= "   T2.CLASSNAME, ";
        $query .= "   T3.SUBCLASSCD as CHECKSUBCD,";
        $query .= "   T3.SUBCLASSNAME AS SUBCLASSNAME_M,";
        $query .= "   T3.SUBCLASSABBV AS SUBCLASSABBV_M,";
        $query .= "   T3.SUBCLASSNAME_ENG AS SUBCLASSNAME_ENG_M,";
        $query .= "   T3.SUBCLASSABBV_ENG AS SUBCLASSABBV_ENG_M,";
        if ($model->Properties["useProvFlg"] == '1') {
            $query .= "   T5.PROV_FLG, ";
        }
        $query .= "   RM1.REMARK AS REMARK1, ";
        $query .= "   RM2.REMARK AS REMARK2 ";
        $query .= " FROM ";
        $query .= "   CLASS_MST T2, ";
        $query .= "   SCHREG_STUDYREC_EXEC_HIST_DAT T1 ";
        $query .= "   LEFT JOIN STAFF_MST L1 ON L1.STAFFCD = T1.REGISTERCD ";
        $query .= "   LEFT JOIN REGD ON T1.YEAR = REGD.YEAR ";
        $query .= "        AND T1.SCHREGNO = REGD.SCHREGNO ";
        $query .= "   LEFT JOIN NOT_RYUNEN ON REGD.GRADE = NOT_RYUNEN.GRADE ";
        $query .= "        AND T1.SCHREGNO = NOT_RYUNEN.SCHREGNO ";
        $query .= "        AND T1.YEAR = NOT_RYUNEN.YEAR ";
        $query .= "   LEFT JOIN SCHREG_STUDYREC_DETAIL_EXEC_HIST_DAT DETAIL ";
        $query .= "                    ON T1.CALC_DATE = DETAIL.CALC_DATE ";
        $query .= "                   AND T1.CALC_TIME = DETAIL.CALC_TIME ";
        $query .= "                   AND T1.BEF_AFT_DIV = DETAIL.BEF_AFT_DIV ";
        $query .= "                   AND T1.SCHOOLCD = DETAIL.SCHOOLCD ";
        $query .= "                   AND T1.YEAR = DETAIL.YEAR ";
        $query .= "                   AND T1.SCHREGNO = DETAIL.SCHREGNO ";
        $query .= "                   AND T1.CLASSCD = DETAIL.CLASSCD ";
        $query .= "                   AND T1.SCHOOL_KIND = DETAIL.SCHOOL_KIND ";
        $query .= "                   AND T1.CURRICULUM_CD = DETAIL.CURRICULUM_CD ";
        $query .= "                   AND T1.SUBCLASSCD = DETAIL.SUBCLASSCD ";
        $query .= "                   AND DETAIL.SEQ = '002' ";
        $query .= "   LEFT JOIN STUDYRECREMARK_EXEC_HIST_DAT RM1 ";
        $query .= "                    ON T1.CALC_DATE = RM1.CALC_DATE ";
        $query .= "                   AND T1.CALC_TIME = RM1.CALC_TIME ";
        $query .= "                   AND RM1.BEF_AFT_DIV = '1' ";
        $query .= "                   AND T1.YEAR = RM1.YEAR ";
        $query .= "                   AND T1.SCHREGNO = RM1.SCHREGNO ";
        $query .= "                   AND T1.CLASSCD = RM1.CLASSCD ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "               AND T1.SCHOOL_KIND = RM1.SCHOOL_KIND ";
            $query .= "               AND T1.CURRICULUM_CD = RM1.CURRICULUM_CD ";
        }
        $query .= "                   AND T1.SUBCLASSCD = RM1.SUBCLASSCD ";
        $query .= "   LEFT JOIN STUDYRECREMARK_EXEC_HIST_DAT RM2 ";
        $query .= "                    ON T1.CALC_DATE = RM2.CALC_DATE ";
        $query .= "                   AND T1.CALC_TIME = RM2.CALC_TIME ";
        $query .= "                   AND RM2.BEF_AFT_DIV = '2' ";
        $query .= "                   AND T1.YEAR = RM2.YEAR ";
        $query .= "                   AND T1.SCHREGNO = RM2.SCHREGNO ";
        $query .= "                   AND T1.CLASSCD = RM2.CLASSCD ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "               AND T1.SCHOOL_KIND = RM2.SCHOOL_KIND ";
            $query .= "               AND T1.CURRICULUM_CD = RM2.CURRICULUM_CD ";
        }
        $query .= "                   AND T1.SUBCLASSCD = RM2.SUBCLASSCD ";
        $query .= "   LEFT JOIN SUBCLASS_MST T3 ON T1.SUBCLASSCD = T3.SUBCLASSCD ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "               AND T1.CLASSCD = T3.CLASSCD ";
            $query .= "               AND T1.SCHOOL_KIND = T3.SCHOOL_KIND ";
            $query .= "               AND T1.CURRICULUM_CD = T3.CURRICULUM_CD ";
        }
        if ($model->Properties["useProvFlg"] == '1') {
            $query .= "   LEFT JOIN STUDYREC_PROV_FLG_EXEC_HIST_DAT T5 ";
            $query .= "                    ON T1.CALC_DATE = T5.CALC_DATE ";
            $query .= "                   AND T1.CALC_TIME = T5.CALC_TIME ";
            $query .= "                   AND T1.BEF_AFT_DIV = T5.BEF_AFT_DIV ";
            $query .= "                   AND T1.SCHOOLCD = T5.SCHOOLCD ";
            $query .= "                   AND T1.YEAR = T5.YEAR ";
            $query .= "                   AND T1.SCHREGNO = T5.SCHREGNO ";
            $query .= "                   AND T1.SUBCLASSCD = T5.SUBCLASSCD ";
            //教育課程対応
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "               AND T1.CLASSCD = T5.CLASSCD ";
                $query .= "               AND T1.SCHOOL_KIND = T5.SCHOOL_KIND ";
                $query .= "               AND T1.CURRICULUM_CD = T5.CURRICULUM_CD ";
            }
        }
        $query .= " WHERE ";
        $query .= "   T1.SCHREGNO = '" .$model->schregno ."' ";
        $query .= "   AND T1.CLASSCD = T2.CLASSCD ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "   AND T1.SCHOOL_KIND = T2.SCHOOL_KIND ";
        }
        $query .= " ORDER BY ";
        $query .= "   T1.CALC_DATE DESC, ";
        $query .= "   T1.CALC_TIME DESC, ";
        $query .= "   T1.YEAR, ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "   T1.CLASSCD || '-' || ";
            $query .= "   T1.SCHOOL_KIND || '-' || ";
            $query .= "   T1.CURRICULUM_CD || '-' || ";
        }
        $query .= "   T1.SUBCLASSCD, ";
        $query .= "   T1.SCHOOLCD "; ////データ種別

//        echo $query;
        return $query;
    }
    //(実行履歴)実行日付・時間を取得
    public function getCalcDateTime()
    {
        $query  = " with t_date_time (CALC_DATE,CALC_TIME) as ( ";
        $query .= " values( ";
        $query .= "     date(sysdate()), ";
        $query .= "     time(sysdate()) ";
        $query .= " )) ";
        $query .= "  ";
        $query .= " select * from t_date_time ";
        return $query;
    }

    //年度取得
    public function &selectQueryYear($model)
    {
        $query = " SELECT DISTINCT ";
        $query .= "   YEAR ";
        $query .= " FROM ";
        $query .= "   SCHREG_STUDYREC_DAT ";
        $query .= " WHERE ";
        $query .= "   SCHREGNO = '".$model->schregno."' ";
        return $query;
    }

    //年度重複エラーチェック(同じ年度に複数の年次を登録しない）
    public function selectAnnualQuery($model)
    {
        $query = "SELECT * FROM schreg_studyrec_dat
                   WHERE year       = '".$model->field["YEAR"]."'
                     AND schregno   = '".$model->schregno."'
                     AND annual    <> '".$model->field["ANNUAL"]."'";
        return $query;
    }
    //年度重複エラーチェック(同じ年度に複数の年次を登録しない）
    public function selectAnnualQuery2($model)
    {
        $query = "SELECT * FROM schreg_studyrec_dat
                   WHERE year      <> '".$model->field["YEAR"]."'
                     AND schregno   = '".$model->schregno."'
                     AND annual     = '".$model->field["ANNUAL"]."'";
        return $query;
    }

    //レコードチェック
    public function recordCheckQuery($model)
    {
        $query = "SELECT * FROM schreg_studyrec_dat ";
        $query .= "  WHERE ";
        $query .= "        YEAR       = '".$model->field["YEAR"]."'";
        $query .= "    AND SCHREGNO   = '".$model->schregno."'";
        $query .= "    AND ANNUAL     = '".$model->field["ANNUAL"]."'";
        if ($model->Properties["useCurriculumcd"] == "1") {
            $query .= "    AND CURRICULUM_CD = '".$model->field["CURRICULUM_CD"]."'";
            $query .= "    AND CLASSCD || '-' || SCHOOL_KIND || '-' || SUBCLASSCD = '".$model->field["CLASSCD"].$model->field["SUBCLASSCD"]."'";
        } else {
            $query .= "    AND CLASSCD    = '".$model->field["CLASSCD"]."'";
            $query .= "    AND SUBCLASSCD = '".$model->field["CLASSCD"].$model->field["SUBCLASSCD"]."'";
        }
        return $query;
    }

    //署名チェック
    public function getOpinionsWk($year, $schregno)
    {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     ATTEST_OPINIONS_WK ";
        $query .= " WHERE ";
        $query .= "     YEAR     = '{$year}' AND ";
        $query .= "     SCHREGNO = '{$schregno}' ";
        return $query;
    }
}
