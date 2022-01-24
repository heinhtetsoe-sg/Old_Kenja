<?php

require_once('for_php7.php');

class knjh344Query extends Query {

    //校種取得
    function getSchkind($model) {
        $query  = " SELECT ";
        $query .= "     NAME1 AS VALUE, ";
        $query .= "     ABBV1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = 'A023' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= "     AND NAME1 IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
            }
        }
        $query .= " ORDER BY ";
        $query .= "     NAME1 ";

        return $query;
    }

    //学年取得
    function getGrade($model) {
        $query  = "SELECT ";
        $query .= "    GRADE AS VALUE, ";
        $query .= "    GRADE_NAME1 AS LABEL ";
        $query .= "FROM ";
        $query .= "    SCHREG_REGD_GDAT ";
        $query .= "WHERE ";
        $query .= "    YEAR = '".CTRL_YEAR."' ";
        //if ($model->Properties["use_prg_schoolkind"] == "1") {
        //    if ($model->selectSchoolKind) {
        //        $query .= "     AND GRADE IN (SELECT ";
        //        $query .= "                      REGD_G.GRADE ";
        //        $query .= "                   FROM ";
        //        $query .= "                      SCHREG_REGD_GDAT REGD_G ";
        //        $query .= "                   WHERE ";
        //        $query .= "                      REGD_G.YEAR = '".CTRL_YEAR."' ";
        //        if ($model->field["SCHKIND"] == "") {
        //            $query .= "                      AND REGD_G.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."')) ";
        //        } else {
        //            $query .= "                      AND REGD_G.SCHOOL_KIND = '".$model->field["SCHKIND"]."') ";
        //        }
        //    }
        //} elseif ($model->Properties["useSchool_KindField"] == "1") {
        //    $query .= "     AND GRADE IN (SELECT ";
        //    $query .= "                      REGD_G.GRADE ";
        //    $query .= "                   FROM ";
        //    $query .= "                      SCHREG_REGD_GDAT REGD_G ";
        //    $query .= "                   WHERE ";
        //    $query .= "                      REGD_G.YEAR = '".CTRL_YEAR."' ";
        //    $query .= "                      AND REGD_G.SCHOOL_KIND = '" .SCHOOLKIND ."') ";
        //} else {
            $query .= " AND SCHOOL_KIND = '".$model->field["SCHKIND"]."' ";
        //}
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //クラス取得
    function getAuthClass($model)
    {
        $query  = "SELECT DISTINCT";
        $query .= "    T1.GRADE || T1.HR_CLASS AS VALUE, ";
        $query .= "    T1.HR_NAME AS LABEL ";
        $query .= "FROM ";
        $query .= "    SCHREG_REGD_HDAT T1 ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= "INNER JOIN SCHREG_REGD_GDAT T2 ON T2.YEAR = T1.YEAR AND T2.GRADE = T1.GRADE ";
                $query .= " AND T2.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
            }
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= "INNER JOIN SCHREG_REGD_GDAT T2 ON T2.YEAR = T1.YEAR AND T2.GRADE = T1.GRADE ";
            $query .= " AND T2.SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= "WHERE ";
        $query .= "    T1.YEAR = '".CTRL_YEAR."' ";
        $query .= "    AND T1.GRADE = '".$model->field["GRADE"] ."' ";
        //参照・更新可（制限付き）
        if (AUTHORITY == DEF_REFER_RESTRICT || AUTHORITY == DEF_UPDATE_RESTRICT){
            $query .= "    AND (TR_CD1 = '" .STAFFCD ."' OR ";
            $query .= "         TR_CD2 = '" .STAFFCD ."' OR ";
            $query .= "         TR_CD3 = '" .STAFFCD ."') ";
        }
        $query .= "ORDER BY";
        $query .= "    VALUE ";

        return $query;
    }

    //模試情報取得
    function getTrialList() {
        $query .= " SELECT ";
        $query .= "   MOCKCD AS VALUE, ";
        $query .= "   MOCKCD || ':' || MOCKNAME1 AS LABEL ";
        $query .= " FROM MOCK_MST ";
        $query .= " WHERE MOCKCD Like '1".CTRL_YEAR."%' ";
        $query .= " ORDER BY ";
        $query .= "   COMPANYCD ASC, ";
        $query .= "   VALUE ASC ";
        return $query;
    }

    //模試情報取得
    function getTrialInfoQuery($mockcd) {
        $query = "SELECT * FROM MOCK_MST WHERE MOCKCD IN (".$mockcd.") ORDER BY MOCKCD ";
        return $query;
    }

    //CSV出力データ取得
    function getCsvQuery($model, $company) {
        $g_hrclassarry = explode (",", $model->selectdata);
        $g_grclass = "('".implode("','", $g_hrclassarry)."')";
        //処理概要
        //1.ベースとなる年度～学籍番号までの情報
        //2.各会社のテーブルのSCORE_DATに対して、模試選択1用,模試選択2用としてフィルタを用意。
        //3.(CSV出力指定ときのみ)次年度選択科目の項目を用意(SUBJECTINFO)。
        //4.教科別評定平均を算出(SCOREINFO)。
        //5.1.、～4.を利用して、データを作成する。
        $query  = " WITH BASEDAT AS ( ";
        $query .= "  SELECT ";
        $query .= "   T1.YEAR, ";
        $query .= "   T1.SEMESTER, ";
        $query .= "   T1.COURSECD, ";
        $query .= "   T1.MAJORCD, ";
        $query .= "   T1.COURSECODE, ";
        $query .= "   T1.GRADE, ";
        $query .= "   T1.HR_CLASS, ";
        $query .= "   T1.ATTENDNO, ";
        $query .= "   T1.SCHREGNO, ";
        $query .= "   T2.NAME ";
        $query .= "  FROM ";
        $query .= "   SCHREG_REGD_DAT T1 ";
        $query .= "   LEFT JOIN SCHREG_BASE_MST T2 ON ";
        $query .= "     T2.SCHREGNO = T1.SCHREGNO ";
        $query .= "  GROUP BY ";
        $query .= "   T1.YEAR, ";
        $query .= "   T1.SEMESTER, ";
        $query .= "   T1.COURSECD, ";
        $query .= "   T1.MAJORCD, ";
        $query .= "   T1.COURSECODE, ";
        $query .= "   T1.GRADE, ";
        $query .= "   T1.HR_CLASS, ";
        $query .= "   T1.ATTENDNO, ";
        $query .= "   T1.SCHREGNO, ";
        $query .= "   T2.NAME ";
        $query .= " ), FILTER_MOCK_CSV_SUNDAI_SCORE_DAT_01 AS (";
        $query .= "   SELECT ";
        $query .= "       T2.* ";
        $query .= "   FROM MOCK_CSV_SUNDAI_HDAT T1 ";
        $query .= "       LEFT JOIN MOCK_CSV_SUNDAI_SCORE_DAT T2 ON T2.YEAR = T1.YEAR ";
        $query .= "       AND T2.MOSI_CD = T1.MOSI_CD ";
        $query .= "       AND T2.EXAMNO = T1.EXAMNO ";
        $query .= "   WHERE ";
        $query .= "       T1.MOCKCD = '".$model->field["TRIALTEST1_NAME"]."' ";
        $query .= " ), FILTER_MOCK_CSV_BENE_SCORE_DAT_01 AS ( ";
        $query .= "   SELECT ";
        $query .= "       T2.* ";
        $query .= "   FROM MOCK_CSV_BENE_SCORE_HDAT T1 ";
        $query .= "       LEFT JOIN MOCK_CSV_BENE_SCORE_DAT T2 ON T2.YEAR = T1.YEAR ";
        $query .= "       AND T2.KYOUZAICD = T1.KYOUZAICD ";
        $query .= "       AND T2.BENEID = T1.BENEID ";
        $query .= "   WHERE ";
        $query .= "       T1.MOCKCD = '".$model->field["TRIALTEST1_NAME"]."' ";
        $query .= " ), FILTER_MOCK_CSV_ZKAI_SCORE_DAT_01 AS (";
        $query .= "   SELECT ";
        $query .= "       T2.* ";
        $query .= "   FROM MOCK_CSV_ZKAI_HDAT T1 ";
        $query .= "       LEFT JOIN MOCK_CSV_ZKAI_SCORE_DAT T2 ON T2.YEAR = T1.YEAR ";
        $query .= "       AND T2.MOSI_CD = T1.MOSI_CD ";
        $query .= "       AND T2.HR_CLASS = T1.HR_CLASS ";
        $query .= "       AND T2.ATTENDNO = T1.ATTENDNO ";
        $query .= "   WHERE ";
        $query .= "       T1.MOCKCD = '".$model->field["TRIALTEST1_NAME"]."' ";
        $query .= " ), FILTER_MOCK_CSV_SUNDAI_SCORE_DAT_02 AS (";
        $query .= "   SELECT ";
        $query .= "       T2.* ";
        $query .= "   FROM MOCK_CSV_SUNDAI_HDAT T1 ";
        $query .= "       LEFT JOIN MOCK_CSV_SUNDAI_SCORE_DAT T2 ON T2.YEAR = T1.YEAR ";
        $query .= "       AND T2.MOSI_CD = T1.MOSI_CD ";
        $query .= "       AND T2.EXAMNO = T1.EXAMNO ";
        $query .= "   WHERE ";
        $query .= "       T1.MOCKCD = '".$model->field["TRIALTEST2_NAME"]."' ";
        $query .= " ), FILTER_MOCK_CSV_BENE_SCORE_DAT_02 AS (";
        $query .= "   SELECT ";
        $query .= "       T2.* ";
        $query .= "   FROM MOCK_CSV_BENE_SCORE_HDAT T1 ";
        $query .= "       LEFT JOIN MOCK_CSV_BENE_SCORE_DAT T2 ON T2.YEAR = T1.YEAR ";
        $query .= "       AND T2.KYOUZAICD = T1.KYOUZAICD ";
        $query .= "       AND T2.BENEID = T1.BENEID ";
        $query .= "   WHERE ";
        $query .= "       T1.MOCKCD = '".$model->field["TRIALTEST2_NAME"]."' ";
        $query .= " ), FILTER_MOCK_CSV_ZKAI_SCORE_DAT_02 AS (";
        $query .= "   SELECT ";
        $query .= "       T2.* ";
        $query .= "   FROM MOCK_CSV_ZKAI_HDAT T1 ";
        $query .= "       LEFT JOIN MOCK_CSV_ZKAI_SCORE_DAT T2 ON T2.YEAR = T1.YEAR ";
        $query .= "       AND T2.MOSI_CD = T1.MOSI_CD ";
        $query .= "       AND T2.HR_CLASS = T1.HR_CLASS ";
        $query .= "       AND T2.ATTENDNO = T1.ATTENDNO ";
        $query .= "   WHERE ";
        $query .= "       T1.MOCKCD = '".$model->field["TRIALTEST2_NAME"]."' ";
        $query .= " ) ";
        if (get_count($company > 0)) {
            //次年度選択科目の取得
            //生徒関係なくグループ別の教科データの列を作成するために、基礎データを抽出
            $query .= " , SUBJECTBASEELM1 AS ( ";
            $query .= "   SELECT ";
            $query .= "       TW.YEAR, ";
            $query .= "       TW.SEMESTER, ";
            $query .= "       TW.SCHOOL_KIND, ";
            $query .= "       TW.CLASSCD, ";
            $query .= "       TW.CURRICULUM_CD, ";
            $query .= "       TW.SUBCLASSCD, ";
            $query .= "       TW.GROUPCD, ";
            $query .= "       TW.SCHREGNO ";
            $query .= "   FROM ";
            $query .= "       SUBCLASS_STD_SELECT_RIREKI_DAT TW ";
            $query .= "   WHERE ";
            $query .= "       TW.YEAR = '".($model->field["YEAR"]+1)."' ";
            $query .= "       AND TW.SEMESTER = '1' ";
            $query .= "       AND TW.SCHOOL_KIND = '".$model->field["SCHKIND"]."' ";
            $query .= " ) ";
            //基礎データから、生徒一覧を作成
            $query .= " , SUBJECTBASEELM2 AS ( ";
            $query .= "   SELECT ";
            $query .= "       TW2.SCHREGNO ";
            $query .= "   FROM ";
            $query .= "       SUBJECTBASEELM1 TW2 ";
            $query .= "   GROUP BY ";
            $query .= "       TW2.SCHREGNO ";
            $query .= " ) ";
            //基礎データから、(生徒を無視した)グループ別の教科データの列を作成を作成。ここで列番号を振る。
            $query .= " , SUBJECTBASEELM3 AS ( ";
            $query .= "   SELECT ";
            $query .= "       T1.CLASSCD, ";
            $query .= "       T1.CURRICULUM_CD, ";
            $query .= "       T1.SUBCLASSCD, ";
            $query .= "       T1.GROUPCD, ";
            $query .= "       row_number() over () as seq ";
            $query .= "   FROM ";
            $query .= "       SUBJECTBASEELM1 T1 ";
            $query .= "   GROUP BY ";
            $query .= "       T1.CLASSCD, ";
            $query .= "       T1.CURRICULUM_CD, ";
            $query .= "       T1.SUBCLASSCD, ";
            $query .= "       T1.GROUPCD ";
            $query .= "   ORDER BY ";
            $query .= "       T1.GROUPCD, ";
            $query .= "       T1.CLASSCD, ";
            $query .= "       T1.CURRICULUM_CD, ";
            $query .= "       T1.SUBCLASSCD ";
            $query .= " ) ";
            //SUBJECTBASEELM2とSUBJECTBASEELM3を掛け合わせて、はめ込むベースとなる表を作成。
            $query .= " , SUBJECTBASE AS ( ";
            $query .= "   SELECT ";
            $query .= "       M3.CLASSCD, ";
            $query .= "       M3.CURRICULUM_CD, ";
            $query .= "       M3.SUBCLASSCD, ";
            $query .= "       M3.GROUPCD, ";
            $query .= "       M3.seq, ";
            $query .= "       M2.SCHREGNO ";
            $query .= "   FROM ";
            $query .= "       SUBJECTBASEELM2 M2, ";
            $query .= "       SUBJECTBASEELM3 M3 ";
            $query .= " ) ";
            //上記作成したSUBJECTBASEの表に基礎データを紐づけた上で名称を引っぱる
            $query .= " , SUBJECTINFO AS ( ";
            $query .= " select ";
            $query .= "  T1.SCHREGNO, ";
            $query .= "  max(case when seq = 1 then T2.SUBCLASSNAME else null END) as nameA, ";
            $query .= "  max(case when seq = 2 then T2.SUBCLASSNAME else null END) as nameB, ";
            $query .= "  max(case when seq = 3 then T2.SUBCLASSNAME else null END) as nameC, ";
            $query .= "  max(case when seq = 4 then T2.SUBCLASSNAME else null END) as nameD, ";
            $query .= "  max(case when seq = 5 then T2.SUBCLASSNAME else null END) as nameE, ";
            $query .= "  max(case when seq = 6 then T2.SUBCLASSNAME else null END) as nameF, ";
            $query .= "  max(case when seq = 7 then T2.SUBCLASSNAME else null END) as nameG, ";
            $query .= "  max(case when seq = 8 then T2.SUBCLASSNAME else null END) as nameH, ";
            $query .= "  max(case when seq = 9 then T2.SUBCLASSNAME else null END) as nameI, ";
            $query .= "  max(case when seq = 10 then T2.SUBCLASSNAME else null END) as nameJ, ";
            $query .= "  max(case when seq = 11 then T2.SUBCLASSNAME else null END) as nameK, ";
            $query .= "  max(case when seq = 12 then T2.SUBCLASSNAME else null END) as nameL  ";
            $query .= " from ";
            $query .= "  SUBJECTBASE T1 ";
            $query .= "  LEFT JOIN SUBJECTBASEELM1 ORGDAT ";
            $query .= "     ON ORGDAT.CLASSCD = T1.CLASSCD ";
            $query .= "    AND ORGDAT.CURRICULUM_CD = T1.CURRICULUM_CD ";
            $query .= "    AND ORGDAT.SUBCLASSCD = T1.SUBCLASSCD ";
            $query .= "    AND ORGDAT.GROUPCD = T1.GROUPCD ";
            $query .= "    AND ORGDAT.SCHREGNO = T1.SCHREGNO ";
            $query .= "  LEFT JOIN SUBCLASS_MST T2 ";
            $query .= "    ON T2.CLASSCD = ORGDAT.CLASSCD ";
            $query .= "   AND T2.CURRICULUM_CD = ORGDAT.CURRICULUM_CD ";
            $query .= " WHERE ";
            $query .= "   T2.SCHOOL_KIND = '".$model->field["SCHKIND"]."' ";
            $query .= " group by ";
            $query .= "  T1.SCHREGNO ";
            
            //成績データ
            $query .= " ), SCOREBASE AS ( ";
            $query .= " SELECT  ";
            $query .= "   SSDAT.SCHOOL_KIND, ";
            $query .= "   SSDAT.CLASSCD, ";
            $query .= "   SSDAT.CURRICULUM_CD, ";
            $query .= "   SSDAT.SUBCLASSCD, ";
            $query .= "   SSDAT.SCHREGNO, ";
            $query .= "   '1' AS SUBJECTCNT, ";
            $query .= "   SSDAT.VALUATION as score ";
            $query .= " FROM ";
            $query .= "   SCHREG_STUDYREC_DAT SSDAT ";
            $query .= " WHERE ";
            $query .= "   SSDAT.YEAR < '2006' ";
            $query .= " UNION ";
            $query .= " SELECT  ";
            $query .= "   RRDAT.SCHOOL_KIND, ";
            $query .= "   RRDAT.CLASSCD, ";
            $query .= "   RRDAT.CURRICULUM_CD, ";
            $query .= "   RRDAT.SUBCLASSCD, ";
            $query .= "   RRDAT.SCHREGNO, ";
            $query .= "   '1' AS SUBJECTCNT, ";
            $query .= "   RRDAT.SCORE ";
            $query .= " FROM ";
            $query .= "   RECORD_RANK_DAT RRDAT ";
            $query .= " WHERE ";
            $query .= "   RRDAT.YEAR = '".$model->field["YEAR"]."' ";
            $query .= "   AND RRDAT.SEMESTER = '9' ";
            $query .= "   AND RRDAT.TESTKINDCD = '99' ";
            $query .= "   AND RRDAT.TESTITEMCD = '00' ";
            $query .= "   AND RRDAT.CLASSCD = '09' ";
            $query .= " ), SCORECALWK AS ( ";
            $query .= " SELECT  ";
            $query .= "   SB1.SCHOOL_KIND, ";
            $query .= "   SB1.CLASSCD, ";
            $query .= "   SB1.SCHREGNO, ";
            $query .= "   row_number() over (partition by SB1.SCHREGNO) as seq, ";
            $query .= "   COUNT(SB1.SUBJECTCNT) AS SUBJECT_CNT, ";
            $query .= "   SUM(SB1.score) AS SCORE_CNT, ";
            $query .= "   DOUBLE(SUM(SB1.score))/double(COUNT(SB1.SUBJECTCNT)) AS JDG_AVG ";
            $query .= " FROM ";
            $query .= "   SCOREBASE SB1 ";
            $query .= " GROUP BY ";
            $query .= "   SB1.SCHOOL_KIND, ";
            $query .= "   SB1.CLASSCD, ";
            $query .= "   SB1.SCHREGNO ";
            $query .= " ORDER BY ";
            $query .= "   SB1.SCHOOL_KIND, ";
            $query .= "   SB1.CLASSCD, ";
            $query .= "   SB1.SCHREGNO ";
            $query .= " ), SCOREINFO AS ( ";
            $query .= " SELECT ";
            $query .= "   SSW1.SCHOOL_KIND, ";
            $query .= "   SSW1.SCHREGNO, ";
            $query .= "   max(case when seq = 1 then CM2.CLASSNAME else null END) as SBJNAME_1, ";
            $query .= "   max(case when seq = 1 then SSW1.JDG_AVG else null END) as SBJAVG_1, ";
            $query .= "   max(case when seq = 2 then CM2.CLASSNAME else null END) as SBJNAME_2, ";
            $query .= "   max(case when seq = 2 then SSW1.JDG_AVG else null END) as SBJAVG_2, ";
            $query .= "   max(case when seq = 3 then CM2.CLASSNAME else null END) as SBJNAME_3, ";
            $query .= "   max(case when seq = 3 then SSW1.JDG_AVG else null END) as SBJAVG_3, ";
            $query .= "   max(case when seq = 4 then CM2.CLASSNAME else null END) as SBJNAME_4, ";
            $query .= "   max(case when seq = 4 then SSW1.JDG_AVG else null END) as SBJAVG_4, ";
            $query .= "   max(case when seq = 5 then CM2.CLASSNAME else null END) as SBJNAME_5, ";
            $query .= "   max(case when seq = 5 then SSW1.JDG_AVG else null END) as SBJAVG_5, ";
            $query .= "   max(case when seq = 6 then CM2.CLASSNAME else null END) as SBJNAME_6, ";
            $query .= "   max(case when seq = 6 then SSW1.JDG_AVG else null END) as SBJAVG_6, ";
            $query .= "   max(case when seq = 7 then CM2.CLASSNAME else null END) as SBJNAME_7, ";
            $query .= "   max(case when seq = 7 then SSW1.JDG_AVG else null END) as SBJAVG_7, ";
            $query .= "   max(case when seq = 8 then CM2.CLASSNAME else null END) as SBJNAME_8, ";
            $query .= "   max(case when seq = 8 then SSW1.JDG_AVG else null END) as SBJAVG_8, ";
            $query .= "   max(case when seq = 9 then CM2.CLASSNAME else null END) as SBJNAME_9, ";
            $query .= "   max(case when seq = 9 then SSW1.JDG_AVG else null END) as SBJAVG_9 ";
            $query .= " FROM ";
            $query .= "   SCORECALWK SSW1 ";
            $query .= "   LEFT JOIN CLASS_MST CM2 ";
            $query .= "     ON CM2.CLASSCD = SSW1.CLASSCD ";
            $query .= "     AND CM2.SCHOOL_KIND = SSW1.SCHOOL_KIND ";
            $query .= " WHERE ";
            $query .= "    SSW1.SCHOOL_KIND = '".$model->field["SCHKIND"]."' ";
            $query .= " GROUP BY ";
            $query .= "   SSW1.SCHOOL_KIND, ";
            $query .= "   SSW1.SCHREGNO ";
            
            
            $query .= " ) ";
        }
        $query .= " SELECT ";
        $query .= "  D1.YEAR, ";
        $query .= "  D1.SEMESTER, ";
        $query .= "  M3.COURSENAME AS COURSENAME, ";
        $query .= "  M4.MAJORNAME AS MAJORNAME, ";
        $query .= "  M5.COURSECODENAME AS COURSECODENAME, ";
        $query .= "  G1.GRADE_NAME1 AS GRADENAME, ";
        $query .= "  H1.HR_NAME AS HR_CLASSNAME, ";
        $query .= "  D1.ATTENDNO, ";
        $query .= "  D1.SCHREGNO, ";
        $query .= "  D1.NAME ";
        if (get_count($company) > 0) {
            $query .= " , SBJINF.nameA AS SBJ_A, ";
            $query .= " SBJINF.nameB AS SBJ_B, ";
            $query .= " SBJINF.nameC AS SBJ_C, ";
            $query .= " SBJINF.nameD AS SBJ_D, ";
            $query .= " SBJINF.nameE AS SBJ_E, ";
            $query .= " SBJINF.nameF AS SBJ_F, ";
            $query .= " SBJINF.nameG AS SBJ_G, ";
            $query .= " SBJINF.nameH AS SBJ_H, ";
            $query .= " SBJINF.nameI AS SBJ_I, ";
            $query .= " SBJINF.nameJ AS SBJ_J, ";
            $query .= " SBJINF.nameK AS SBJ_K, ";
            $query .= " SBJINF.nameL AS SBJ_L, ";
            $query .= " MOCK_MST_1.MOCKNAME1 AS MOSI1NAME, ";
            $query .= " MOCK_MST_2.MOCKNAME1 AS MOSI2NAME, ";
            $query .= " SCI.SBJNAME_1, ";
            $query .= " SCI.SBJAVG_1, ";
            $query .= " SCI.SBJNAME_2, ";
            $query .= " SCI.SBJAVG_2, ";
            $query .= " SCI.SBJNAME_3, ";
            $query .= " SCI.SBJAVG_3, ";
            $query .= " SCI.SBJNAME_4, ";
            $query .= " SCI.SBJAVG_4, ";
            $query .= " SCI.SBJNAME_5, ";
            $query .= " SCI.SBJAVG_5, ";
            $query .= " SCI.SBJNAME_6, ";
            $query .= " SCI.SBJAVG_6, ";
            $query .= " SCI.SBJNAME_7, ";
            $query .= " SCI.SBJAVG_7, ";
            $query .= " SCI.SBJNAME_8, ";
            $query .= " SCI.SBJAVG_8, ";
            $query .= " SCI.SBJNAME_9, ";
            $query .= " SCI.SBJAVG_9 ";
            $mockidx = 0;
            //第1、第2の2回分回す。
            foreach($company as $valtmp) {
                if ($valtmp == "00000001") { //駿台
                    $query .= ", ";
                    $query .= " T1S.MOSI_CD ";
                    for ($hCnt = 1; $hCnt <= $model->hopeCnt[$mockidx]; $hCnt++) {
                        $query .= ", ";
                        $query .= " T4S_HOPE".$mockidx."_SEQ".$hCnt.".SCHOOL_NAME AS HOPESCHNAME".($mockidx+1)."_".$hCnt.", ";
                        $query .= " T4S_HOPE".$mockidx."_SEQ".$hCnt.".JUDGE_HYOUKA AS HOPEJUDGE".($mockidx+1)."_".$hCnt;
                    }
                    for ($sCnt = 1; $sCnt <= $model->scoreCnt[$mockidx]; $sCnt++) {
                        $sCntWk = sprintf("%02d", $sCnt);
                        $query .= ", ";
                        $query .= " T2S_SCORE".$mockidx."_SEQLABEL".$sCntWk.".TDEVIATION AS TDEVIATION".($mockidx+1)."_".$sCntWk.", ";
                        $query .= " T2S_SCORE".$mockidx."_SEQDAT".$sCntWk.".DEVIATION AS DEVIATION".($mockidx+1)."_".$sCntWk;
                    }
                }
                if ($valtmp == "00000002") { //ベネッセ
                    $query .= ", ";
                    $query .= "  T1B.BENEID ";
                    for ($hCnt = 1; $hCnt <= $model->hopeCnt[$mockidx]; $hCnt++) {
                        $query .= ", ";
                        $query .= " T4B_HOPE".$mockidx."_SEQ".$hCnt.".SCHOOL_NAME AS HOPESCHNAME".($mockidx+1)."_".$hCnt.", ";
                        $query .= " T4B_HOPE".$mockidx."_SEQ".$hCnt.".ALL_JUDGE AS HOPEJUDGE".($mockidx+1)."_".$hCnt;
                    }
                    for ($sCnt = 1; $sCnt <= $model->scoreCnt[$mockidx]; $sCnt++) {
                        $sCntWk = sprintf("%02d", $sCnt);
                        $query .= ", ";
                        $query .= "  T2B_SCORE".$mockidx."_SEQLABEL".$sCntWk.".TALL_DEV AS TDEVIATION".($mockidx+1)."_".$sCntWk.", ";
                        $query .= "  T2B_SCORE".$mockidx."_SEQDAT".$sCntWk.".ALL_DEV AS DEVIATION".($mockidx+1)."_".$sCntWk;
                    }
                }
                if ($valtmp == "00000003") { //Z会
                    $query .= ", ";
                    $query .= "  T1Z.MOSI_CD ";
                    for ($hCnt = 1; $hCnt <= $model->hopeCnt[$mockidx]; $hCnt++) {
                        $query .= ", ";
                        $query .= " T4Z_HOPE".$mockidx."_SEQ".$hCnt.".SCHOOL_NAME AS HOPESCHNAME".($mockidx+1)."_".$hCnt.", ";
                        $query .= " T4Z_HOPE".$mockidx."_SEQ".$hCnt.".JUDGE_HYOUKA AS HOPEJUDGE".($mockidx+1)."_".$hCnt;
                    }
                    for ($sCnt = 1; $sCnt <= $model->scoreCnt[$mockidx]; $sCnt++) {
                        $sCntWk = sprintf("%02d", $sCnt);
                        $query .= ", ";
                        $query .= "  T2Z_SCORE".$mockidx."_SEQLABEL".$sCntWk.".TDEVIATION AS TDEVIATION".($mockidx+1)."_".$sCntWk.", ";
                        $query .= "  T2Z_SCORE".$mockidx."_SEQDAT".$sCntWk.".DEVIATION AS DEVIATION".($mockidx+1)."_".$sCntWk;
                    }
                }
                $mockidx++;
            }
        }
        $query .= " FROM ";
        $query .= "   BASEDAT D1 ";
        $query .= "     LEFT JOIN COURSE_MST M3 ON M3.COURSECD = D1.COURSECD ";
        $query .= "     LEFT JOIN MAJOR_MST M4 ON M4.COURSECD = D1.COURSECD AND M4.MAJORCD  = D1.MAJORCD ";
        $query .= "     LEFT JOIN COURSECODE_MST M5 ON M5.COURSECODE = D1.COURSECODE ";
        $query .= "     LEFT JOIN SCHREG_REGD_GDAT G1 ON G1.YEAR = D1.YEAR AND G1.GRADE = D1.GRADE ";
        $query .= "     LEFT JOIN SCHREG_REGD_HDAT H1 ON H1.YEAR = D1.YEAR AND H1.SEMESTER = D1.SEMESTER AND H1.GRADE = D1.GRADE AND H1.HR_CLASS = D1.HR_CLASS ";
        if (get_count($company) > 0) {
            $query .= " LEFT JOIN SCOREINFO SCI ON SCI.SCHREGNO = D1.SCHREGNO ";
            $query .= " LEFT JOIN MOCK_MST MOCK_MST_1 ON MOCK_MST_1.MOCKCD = '".$model->field["TRIALTEST1_NAME"]."' ";
            $query .= " LEFT JOIN MOCK_MST MOCK_MST_2 ON MOCK_MST_2.MOCKCD = '".$model->field["TRIALTEST2_NAME"]."' ";
            $query .= " LEFT JOIN SCOREINFO SCOREINF ON SCOREINF.SCHREGNO = D1.SCHREGNO ";
            $query .= " LEFT JOIN SUBJECTINFO AS SBJINF ";
            $query .= "   ON SBJINF.SCHREGNO = D1.SCHREGNO ";
            if (in_array("00000001", $company)) { //駿台
                $query .= " LEFT JOIN MOCK_CSV_SUNDAI_HDAT T1S ON ";
                $query .= "      D1.YEAR = T1S.YEAR ";
                $query .= "      AND D1.GRADE = '".$model->field["GRADE"]."' ";
                $query .= "      AND D1.HR_CLASS = T1S.HR_CLASS ";
                $query .= "      AND D1.ATTENDNO = T1S.ATTENDNO ";
                $query .= "      AND D1.SEMESTER = '".CTRL_SEMESTER."' ";
            }
            if (in_array("00000002", $company)) { //ベネッセ
                $query .= "  LEFT JOIN MOCK_CSV_BENE_SCORE_HDAT T1B ON ";
                $query .= "      D1.YEAR = T1B.YEAR ";
                $query .= "      AND D1.GRADE = '".$model->field["GRADE"]."' ";
                $query .= "      AND D1.HR_CLASS = T1B.HR_CLASS ";
                $query .= "      AND D1.ATTENDNO = T1B.ATTENDNO ";
                $query .= "      AND D1.SEMESTER = '".CTRL_SEMESTER."' ";
            }
            if (in_array("00000003", $company)) { //Z会
                $query .= "  LEFT JOIN MOCK_CSV_ZKAI_HDAT T1Z ON ";
                $query .= "      D1.YEAR = T1Z.YEAR ";
                $query .= "      AND D1.GRADE = '".$model->field["GRADE"]."' ";
                $query .= "      AND D1.HR_CLASS = T1Z.HR_CLASS ";
                $query .= "      AND D1.ATTENDNO = T1Z.ATTENDNO ";
                $query .= "      AND D1.SEMESTER = '".CTRL_SEMESTER."' ";
            }
            $mockidx = 0;
            $mockTBLidx = sprintf("%02d", $mockidx+1);
            //第1、第2の2回分回す。
            foreach($company as $valtmp) {
                if ($valtmp == "00000001") { //駿台
                    for ($hCnt = 1; $hCnt <= $model->hopeCnt[$mockidx]; $hCnt++) {
                        $query .= " LEFT JOIN MOCK_CSV_SUNDAI_HOPE_DAT T4S_HOPE".$mockidx."_SEQ".$hCnt." ON ";
                        $query .= "   T4S_HOPE".$mockidx."_SEQ".$hCnt.".YEAR = T1S.YEAR ";
                        $query .= "   AND T4S_HOPE".$mockidx."_SEQ".$hCnt.".MOSI_CD = T1S.MOSI_CD ";
                        $query .= "   AND T4S_HOPE".$mockidx."_SEQ".$hCnt.".EXAMNO = T1S.EXAMNO ";
                        $query .= "   AND T4S_HOPE".$mockidx."_SEQ".$hCnt.".SEQ = '".$hCnt."' ";
                    }
                    for ($sCnt = 1; $sCnt <= $model->scoreCnt[$mockidx]; $sCnt++) {
                        $sCntWk = sprintf("%02d", $sCnt);
                        $query .= " LEFT JOIN FILTER_MOCK_CSV_SUNDAI_SCORE_DAT_".$mockTBLidx." T2S_SCORE".$mockidx."_SEQDAT".$sCntWk." ON ";
                        $query .= "   T2S_SCORE".$mockidx."_SEQDAT".$sCntWk.".YEAR = T1S.YEAR ";
                        $query .= "   AND T2S_SCORE".$mockidx."_SEQDAT".$sCntWk.".EXAMNO = T1S.EXAMNO ";
                        $query .= "   AND T2S_SCORE".$mockidx."_SEQDAT".$sCntWk.".MOSI_CD = T1S.MOSI_CD ";
                        $query .= "   AND T2S_SCORE".$mockidx."_SEQDAT".$sCntWk.".SEQ = '".$sCnt."' ";
                        $query .= " LEFT JOIN MOCK_CSV_SUNDAI_SCORE_HEAD_DAT T2S_SCORE".$mockidx."_SEQLABEL".$sCntWk." ON ";
                        $query .= "   T2S_SCORE".$mockidx."_SEQLABEL".$sCntWk.".YEAR = T2S_SCORE".$mockidx."_SEQDAT".$sCntWk.".YEAR ";
                        $query .= "   AND T2S_SCORE".$mockidx."_SEQLABEL".$sCntWk.".MOSI_CD = T2S_SCORE".$mockidx."_SEQDAT".$sCntWk.".MOSI_CD ";
                        $query .= "   AND T2S_SCORE".$mockidx."_SEQLABEL".$sCntWk.".SEQ = T2S_SCORE".$mockidx."_SEQDAT".$sCntWk.".SEQ ";
                    }
                }
                if ($valtmp == "00000002") { //ベネッセ
                    for ($hCnt = 1; $hCnt <= $model->hopeCnt[$mockidx]; $hCnt++) {
                        $query .= " LEFT JOIN MOCK_CSV_BENE_HOPE_DAT T4B_HOPE".$mockidx."_SEQ".$hCnt." ON ";
                        $query .= "   T4B_HOPE".$mockidx."_SEQ".$hCnt.".YEAR = T1B.YEAR ";
                        $query .= "   AND T4B_HOPE".$mockidx."_SEQ".$hCnt.".KYOUZAICD = T1B.KYOUZAICD ";
                        $query .= "   AND T4B_HOPE".$mockidx."_SEQ".$hCnt.".BENEID = T1B.BENEID ";
                        $query .= "   AND T4B_HOPE".$mockidx."_SEQ".$hCnt.".SEQ = '".$hCnt."' ";
                    }
                    for ($sCnt = 1; $sCnt <= $model->scoreCnt[$mockidx]; $sCnt++) {
                        $sCntWk = sprintf("%02d", $sCnt);
                        $query .= "  LEFT JOIN FILTER_MOCK_CSV_BENE_SCORE_DAT_".$mockTBLidx." T2B_SCORE".$mockidx."_SEQDAT".$sCntWk." ON ";
                        $query .= "    T2B_SCORE".$mockidx."_SEQDAT".$sCntWk.".YEAR = T1B.YEAR ";
                        $query .= "    AND T2B_SCORE".$mockidx."_SEQDAT".$sCntWk.".KYOUZAICD = T1B.KYOUZAICD ";
                        $query .= "    AND T2B_SCORE".$mockidx."_SEQDAT".$sCntWk.".BENEID = T1B.BENEID ";
                        $query .= "    AND T2B_SCORE".$mockidx."_SEQDAT".$sCntWk.".SEQ = '".$sCnt."' ";
                        $query .= "  LEFT JOIN MOCK_CSV_BENE_SCORE_HEAD_DAT T2B_SCORE".$mockidx."_SEQLABEL".$sCntWk." ON ";
                        $query .= "    T2B_SCORE".$mockidx."_SEQLABEL".$sCntWk.".YEAR = T2B_SCORE".$mockidx."_SEQDAT".$sCntWk.".YEAR ";
                        $query .= "    AND T2B_SCORE".$mockidx."_SEQLABEL".$sCntWk.".KYOUZAICD = T2B_SCORE".$mockidx."_SEQDAT".$sCntWk.".KYOUZAICD ";
                        $query .= "    AND T2B_SCORE".$mockidx."_SEQLABEL".$sCntWk.".SEQ = T2B_SCORE".$mockidx."_SEQDAT".$sCntWk.".SEQ ";
                    }
                }
                if ($valtmp == "00000003") { //Z会
                    for ($hCnt = 1; $hCnt <= $model->hopeCnt[$mockidx]; $hCnt++) {
                        $query .= " LEFT JOIN MOCK_CSV_ZKAI_HOPE_DAT T4Z_HOPE".$mockidx."_SEQ".$hCnt." ON ";
                        $query .= "   T4Z_HOPE".$mockidx."_SEQ".$hCnt.".YEAR = T1Z.YEAR ";
                        $query .= "   AND T4Z_HOPE".$mockidx."_SEQ".$hCnt.".MOSI_CD = T1Z.MOSI_CD ";
                        $query .= "   AND T4Z_HOPE".$mockidx."_SEQ".$hCnt.".HR_CLASS = T1Z.HR_CLASS ";
                        $query .= "   AND T4Z_HOPE".$mockidx."_SEQ".$hCnt.".ATTENDNO = T1Z.ATTENDNO ";
                        $query .= "   AND T4Z_HOPE".$mockidx."_SEQ".$hCnt.".SEQ = '".$hCnt."' ";
                    }
                    for ($sCnt = 1; $sCnt <= $model->scoreCnt[$mockidx]; $sCnt++) {
                        $sCntWk = sprintf("%02d", $sCnt);
                        $query .= "  LEFT JOIN FILTER_MOCK_CSV_ZKAI_SCORE_DAT_".$mockTBLidx." T2Z_SCORE".$mockidx."_SEQDAT".$sCntWk." ON ";
                        $query .= "    T2Z_SCORE".$mockidx."_SEQDAT".$sCntWk.".YEAR = T1Z.YEAR ";
                        $query .= "    AND T2Z_SCORE".$mockidx."_SEQDAT".$sCntWk.".MOSI_CD = T1Z.MOSI_CD ";
                        $query .= "    AND T2Z_SCORE".$mockidx."_SEQDAT".$sCntWk.".HR_CLASS = T1Z.HR_CLASS ";
                        $query .= "    AND T2Z_SCORE".$mockidx."_SEQDAT".$sCntWk.".ATTENDNO = T1Z.ATTENDNO ";
                        $query .= "    AND T2Z_SCORE".$mockidx."_SEQDAT".$sCntWk.".SEQ = '".$sCnt."' ";
                        $query .= "  LEFT JOIN MOCK_CSV_ZKAI_SCORE_HEAD_DAT T2Z_SCORE".$mockidx."_SEQLABEL".$sCntWk." ON ";
                        $query .= "    T2Z_SCORE".$mockidx."_SEQLABEL".$sCntWk.".YEAR = T2Z_SCORE".$mockidx."_SEQDAT".$sCntWk.".YEAR ";
                        $query .= "    AND T2Z_SCORE".$mockidx."_SEQLABEL".$sCntWk.".MOSI_CD = T2Z_SCORE".$mockidx."_SEQDAT".$sCntWk.".MOSI_CD ";
                        $query .= "    AND T2Z_SCORE".$mockidx."_SEQLABEL".$sCntWk.".SEQ = T2Z_SCORE".$mockidx."_SEQDAT".$sCntWk.".SEQ ";
                    }
                }
                $mockidx++;
            }
        }
        $query .= "  WHERE ";
        $query .= "    D1.YEAR = '".$model->field["YEAR"]."' ";
        $query .= "    AND D1.SEMESTER = '".CTRL_SEMESTER."' ";
        $query .= "    AND D1.GRADE || D1.HR_CLASS IN ".$g_grclass;
        $query .= " ORDER BY ";
        $query .= "  G1.GRADE_NAME1, ";
        $query .= "  H1.HR_NAME, ";
        $query .= "  D1.ATTENDNO ";

        return $query;
    }

    //次年度選択科目
    function getNextSelectSubject() {
        $query .= " select distinct ";
        $query .= "  T1.CLASSCD ||  T1.CURRICULUM_CD ||  T1.SUBCLASSCD AS VALUE, ";
        $query .= "  T2.SUBCLASSNAME AS LABEL ";
        $query .= " from ";
        $query .= "  SUBCLASS_STD_SELECT_RIREKI_DAT T1 ";
        $query .= "  LEFT JOIN SUBCLASS_MST T2 ";
        $query .= "    ON T2.CLASSCD = T1.CLASSCD ";
        $query .= "    AND T2.SCHOOL_KIND = T1.SCHOOL_KIND ";
        $query .= "    AND T2.CURRICULUM_CD = T1.CURRICULUM_CD ";
        $query .= "    AND T2.SUBCLASSCD = T1.SUBCLASSCD ";
        $query .= " WHERE ";
        $query .= "  T1.YEAR = '".(CTRL_YEAR+1)."' ";
        $query .= "  AND T1.SEMESTER = '1' ";
        $query .= " ORDER BY ";
        $query .= "  VALUE ";
        return $query;
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
            $query .= " AND SCHOOL_KIND IN (SELECT ";
            $query .= "                         SCHOOL_KIND ";
            $query .= "                     FROM ";
            $query .= "                         SCHREG_REGD_GDAT ";
            $query .= "                     WHERE ";
            $query .= "                         YEAR    = '".CTRL_YEAR."' AND ";
            $query .= "                         GRADE   = '". substr($model->field["GRADE_HR_CLASS"],0,2)."' ";
            $query .= "                     ) ";
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND SCHOOLCD    = '".sprintf("%012d", SCHOOLCD)."' ";
            $query .= " AND SCHOOL_KIND = '".SCHOOLKIND."' ";
        } else {
            $query .= " AND SCHOOL_KIND IN (SELECT ";
            $query .= "                         SCHOOL_KIND ";
            $query .= "                     FROM ";
            $query .= "                         SCHREG_REGD_GDAT ";
            $query .= "                     WHERE ";
            $query .= "                         YEAR    = '".CTRL_YEAR."' AND ";
            $query .= "                         GRADE   = '". substr($model->field["GRADE_HR_CLASS"],0,2)."' ";
            $query .= "                     ) ";
        }
        $query .= " ORDER BY ";
        $query .= "     SCHOOLCD ";

        return $query;
    }

    //登録学校名取得
    function getZ010Name1() {
        $query  = " SELECT ";
        $query .= "     NAME1 ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = 'Z010' ";
        $query .= "     AND NAMECD2 = '00' ";

        return $query;
    }

    //志望テーブル登録件数取得
    function getHopeField($model, $mockcd) {
        $query  = " SELECT ";
        $query .= "     T1.FIELD_CNT ";
        $query .= " FROM ";
        $query .= "     MOCK_CSV_HOPE_FIELD_DAT T1 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '{$model->field["YEAR"]}' ";
        $query .= "     AND T1.MOCKCD = '{$mockcd}' ";
        $query .= "     AND T1.GRADE = '{$model->field["GRADE"]}' ";

        return $query;
    }

    //成績登録件数取得
    function getScoreField($model, $mockcd) {
        $query  = " SELECT ";
        $query .= "     T1.FIELD_CNT ";
        $query .= " FROM ";
        $query .= "     MOCK_CSV_SCORE_FIELD_DAT T1 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '{$model->field["YEAR"]}' ";
        $query .= "     AND T1.MOCKCD = '{$mockcd}' ";
        $query .= "     AND T1.GRADE = '{$model->field["GRADE"]}' ";

        return $query;
    }

    //ヘッダ出力科目グループ名称取得
    function getHeadGrpSbjName($model) {
        $query  = " WITH SUBJECTBASEELM1 AS (SELECT ";
        $query .= "     TW.YEAR, ";
        $query .= "     TW.SEMESTER, ";
        $query .= "     TW.SCHOOL_KIND, ";
        $query .= "     TW.CLASSCD, ";
        $query .= "     TW.CURRICULUM_CD, ";
        $query .= "     TW.SUBCLASSCD, ";
        $query .= "     TW.GROUPCD, ";
        $query .= "     TW.SCHREGNO ";
        $query .= " FROM ";
        $query .= "     SUBCLASS_STD_SELECT_RIREKI_DAT TW ";
        $query .= " WHERE ";
        $query .= "     TW.YEAR = '".($model->field["YEAR"]+1)."' AND ";
        $query .= "     TW.SEMESTER = '1' AND ";
        $query .= "     TW.SCHOOL_KIND = '".$model->field["SCHKIND"]."' ";
        $query .= " ) , SUBJECTBASEELM2 AS ( ";
        $query .= " SELECT ";
        $query .= "     TW2.SCHREGNO ";
        $query .= " FROM ";
        $query .= "     SUBJECTBASEELM1 TW2 ";
        $query .= " GROUP BY ";
        $query .= "     TW2.SCHREGNO ";
        $query .= " ), SUBJECTBASEELM3 AS ( ";
        $query .= "  SELECT ";
        $query .= "     T1.CLASSCD, ";
        $query .= "     T1.CURRICULUM_CD, ";
        $query .= "     T1.SUBCLASSCD, ";
        $query .= "     T1.GROUPCD, ";
        $query .= "     MAX(T1.SCHREGNO) AS MSCHREGNO, ";
        $query .= "     row_number() over () as seq ";
        $query .= " FROM ";
        $query .= "     SUBJECTBASEELM1 T1 ";
        $query .= " GROUP BY ";
        $query .= "     T1.CLASSCD, ";
        $query .= "     T1.CURRICULUM_CD, ";
        $query .= "     T1.SUBCLASSCD, ";
        $query .= "     T1.GROUPCD ";
        $query .= " ORDER BY ";
        $query .= "     T1.GROUPCD, ";
        $query .= "     T1.CLASSCD, ";
        $query .= "     T1.CURRICULUM_CD, ";
        $query .= "     T1.SUBCLASSCD ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "   SBM3.seq, ";
        $query .= "   T2.ABBV ";
        $query .= " FROM ";
        $query .= "   SUBJECTBASEELM3 SBM3 ";
        $query .= "     LEFT JOIN SCHREG_REGD_DAT NSRD ";
        $query .= "        ON NSRD.YEAR = '".($model->field["YEAR"]+1)."' ";
        $query .= "       AND NSRD.SEMESTER = '1' ";
        $query .= "       AND NSRD.SCHREGNO = SBM3.MSCHREGNO ";
        $query .= "     LEFT JOIN SUBCLASS_COMP_SELECT_MST T2 ";
        $query .= "        ON T2.YEAR = NSRD.YEAR ";
        $query .= "       AND T2.GRADE = NSRD.GRADE ";
        $query .= "       AND T2.COURSECD = NSRD.COURSECD ";
        $query .= "       AND T2.MAJORCD = NSRD.MAJORCD ";
        $query .= "       AND T2.COURSECODE = NSRD.COURSECODE ";
        $query .= "       AND T2.GROUPCD = SBM3.GROUPCD ";
        $query .= " ORDER BY ";
        $query .= "   SBM3.seq ";
        
        return $query;
    }

}
?>
