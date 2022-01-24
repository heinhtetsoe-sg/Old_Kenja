<?php

require_once('for_php7.php');
class knji100c_04Query extends Query
{

/*
        性別区分 .........Z002

        使用テーブル
        SCHREG_BASE_MST......基礎情報取得用
        SCHREG_REGD_DAT......学籍情報取得用
        KIN_RECORD_DAT.......成績情報取得用
*/

    //ＣＳＶ書き出し
    public function selectQuery($model)
    {
        $query  = "";
        $query .= " WITH BASE_MST AS ( ";
        $query .= "     SELECT ";
        $query .= "         * ";
        $query .= "     FROM ";
        $query .= "         SCHREG_BASE_MST ";
        $query .= "     WHERE ";
        $query .= "         SCHREGNO IN ('" .str_replace(",", "','", $model->selectdata_l) ."') ";
        $query .= " ), RECORD_DATA AS ( ";
        $query .= "     SELECT ";
        $query .= "         * ";
        $query .= "     FROM ";
        $query .= "         KIN_RECORD_DAT ";
        $query .= "     WHERE ";
        $query .= "         YEAR = '".CTRL_YEAR."' AND ";
        $query .= "         SCHREGNO IN ('" .str_replace(",", "','", $model->selectdata_l) ."') ";
        $query .= " ) ";

        $query .= " SELECT ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     T1.NAME, ";
        $query .= "     T2.GRADE, ";
        $query .= "     T2.HR_CLASS, ";
        $query .= "     T2.ATTENDNO, ";
        //性別区分
        if ($model->output == OUT_CODE_NAME) {           //コード＋名称
            $query .= "     VALUE(T1.SEX,'') || ',' || ";
            $query .= "     VALUE(N1.NAME1,'') SEX,";
        } elseif ($model->output == OUT_CODE_ONLY) {    //コードのみ
            $query .= "     T1.SEX SEX, ";
        } elseif ($model->output == OUT_NAME_ONLY) {    //名称のみ
            $query .= "     N1.NAME1 SEX,";
        }
        //科目
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            if ($model->output == OUT_CODE_NAME) {           //コード＋名称
                $query .= "     VALUE(L1.CLASSCD || '-' || L1.SCHOOL_KIND || '-' || L1.CURRICULUM_CD || '-' || L1.SUBCLASSCD,'') || ',' || ";
                $query .= "     VALUE(L3.SUBCLASSNAME,'') AS SUBCLASSCD,";
            } elseif ($model->output == OUT_CODE_ONLY) {    //コードのみ
                $query .= "     L1.CLASSCD || '-' || L1.SCHOOL_KIND || '-' || L1.CURRICULUM_CD || '-' || L1.SUBCLASSCD AS SUBCLASSCD, ";
            } elseif ($model->output == OUT_NAME_ONLY) {    //名称のみ
                $query .= "     L3.SUBCLASSNAME AS SUBCLASSCD,";
            }
        } else {
            if ($model->output == OUT_CODE_NAME) {           //コード＋名称
                $query .= "     VALUE(L1.SUBCLASSCD,'') || ',' || ";
                $query .= "     VALUE(L3.SUBCLASSNAME,'') AS SUBCLASSCD,";
            } elseif ($model->output == OUT_CODE_ONLY) {    //コードのみ
                $query .= "     L1.SUBCLASSCD AS SUBCLASSCD, ";
            } elseif ($model->output == OUT_NAME_ONLY) {    //名称のみ
                $query .= "     L3.SUBCLASSNAME AS SUBCLASSCD,";
            }
        }
        //講座
        if ($model->output == OUT_CODE_NAME) {           //コード＋名称
            $query .= "     VALUE(L1.CHAIRCD,'') || ',' || ";
            $query .= "     VALUE(L2.CHAIRNAME,'') AS CHAIRCD,";
        } elseif ($model->output == OUT_CODE_ONLY) {    //コードのみ
            $query .= "     L1.CHAIRCD AS CHAIRCD, ";
        } elseif ($model->output == OUT_NAME_ONLY) {    //名称のみ
            $query .= "     L2.CHAIRNAME AS CHAIRCD,";
        }
        $query .= "     L1.SEM1_INTER_REC, ";
        $query .= "     L1.SEM1_TERM_REC, ";
        $query .= "     L1.SEM1_REC, ";
        $query .= "     L1.SEM2_INTER_REC, ";
        $query .= "     L1.SEM2_TERM_REC, ";
        $query .= "     L1.SEM2_REC, ";
        $query .= "     L1.SEM3_TERM_REC, ";
        $query .= "     L1.SEM3_REC, ";
        $query .= "     L1.SEM1_INTER_REC_FLG, ";
        $query .= "     L1.SEM1_TERM_REC_FLG, ";
        $query .= "     L1.SEM1_REC_FLG, ";
        $query .= "     L1.SEM2_INTER_REC_FLG, ";
        $query .= "     L1.SEM2_TERM_REC_FLG, ";
        $query .= "     L1.SEM2_REC_FLG, ";
        $query .= "     L1.SEM3_TERM_REC_FLG, ";
        $query .= "     L1.SEM3_REC_FLG, ";
        $query .= "     L1.SEM1_INTER_REC_DI, ";
        $query .= "     L1.SEM1_TERM_REC_DI, ";
        $query .= "     L1.SEM2_INTER_REC_DI, ";
        $query .= "     L1.SEM2_TERM_REC_DI, ";
        $query .= "     L1.SEM3_TERM_REC_DI, ";
        $query .= "     L1.GRADE_RECORD, ";
        $query .= "     L1.A_PATTERN_ASSESS, ";
        $query .= "     L1.B_PATTERN_ASSESS, ";
        $query .= "     L1.C_PATTERN_ASSESS, ";
        $query .= "     L1.JUDGE_PATTERN, ";
        $query .= "     L1.GRADE_ASSESS, ";
        $query .= "     L1.GRADE3_RELAASSESS_5STEP, ";
        $query .= "     L1.GRADE3_RELAASSESS_10STEP ";
        $query .= " FROM ";
        $query .= "     BASE_MST T1 ";
        $query .= "     LEFT JOIN NAME_MST N1 ON N1.NAMECD2 = T1.SEX AND N1.NAMECD1='Z002' ";
        $query .= "     LEFT JOIN RECORD_DATA L1 ON L1.SCHREGNO = T1.SCHREGNO ";
        $query .= "     LEFT JOIN SUBCLASS_MST L3 ON L3.SUBCLASSCD = L1.SUBCLASSCD ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     					 AND L3.CLASSCD = L1.CLASSCD  ";
            $query .= "     					 AND L3.SCHOOL_KIND = L1.SCHOOL_KIND  ";
            $query .= "     					 AND L3.CURRICULUM_CD = L1.CURRICULUM_CD  ";
        }
        $query .= "     LEFT JOIN CHAIR_DAT L2 ON  L2.CHAIRCD = L1.CHAIRCD ";
        $query .= "                            AND L2.YEAR     = '".CTRL_YEAR."' ";
        $query .= "                            AND L2.SEMESTER = '".CTRL_SEMESTER."' ";
        $query .= "     INNER JOIN SCHREG_REGD_DAT T2 ON  T2.SCHREGNO = T1.SCHREGNO ";
        $query .= "                                   AND T2.YEAR     = '".CTRL_YEAR."' ";
        $query .= "                                   AND T2.SEMESTER = '".CTRL_SEMESTER."' ";
        $query .= " ORDER BY ";
        $query .= "     T2.GRADE, ";
        $query .= "     T2.HR_CLASS, ";
        $query .= "     T2.ATTENDNO, ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     L1.CLASSCD, ";
            $query .= "     L1.SCHOOL_KIND, ";
            $query .= "     L1.CURRICULUM_CD, ";
        }
        $query .= "     L1.SUBCLASSCD ";

        return $query;
    }

    //ＣＳＶ書き出し
    public function selectQueryRecordScore($model, $recordItem)
    {
        $query  = " WITH SCH_DATA AS ( ";
        $query .= "     SELECT ";
        $query .= "         T1.SCHREGNO, ";
        $query .= "         T1.GRADE, ";
        $query .= "         T1.HR_CLASS, ";
        $query .= "         T1.ATTENDNO, ";
        $query .= "         T2.NAME, ";
        $query .= "         T2.SEX, ";
        $query .= "         N1.NAME1 AS SEX_NAME ";
        $query .= "     FROM ";
        $query .= "         SCHREG_REGD_DAT T1, ";
        $query .= "         SCHREG_BASE_MST T2 ";
        $query .= "         LEFT JOIN NAME_MST N1 ";
        $query .= "                  ON N1.NAMECD2 = T2.SEX ";
        $query .= "                 AND N1.NAMECD1 = 'Z002' ";
        $query .= "     WHERE ";
        $query .= "         T1.YEAR     = '".CTRL_YEAR."' AND ";
        $query .= "         T1.SEMESTER = '".CTRL_SEMESTER."' AND ";
        $query .= "         T1.SCHREGNO = T2.SCHREGNO AND ";
        $query .= "         T1.SCHREGNO IN ('" .str_replace(",", "','", $model->selectdata_l) ."') ";
        $query .= " ), SCH_SUBCLASS AS ( ";
        $tmpTestcd = $sep = "";
        foreach ($recordItem as $key => $val) {
            list($item, $testcd) = explode('_', $key);
            if ($item == "SCORE") {
                $tmpTestcd .= $sep.$testcd;
                $sep = "','";
            }
        }
        $query .= "     SELECT ";
        $query .= "         T1.SCHREGNO, ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "         CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || ";
        }
        $query .= "         SUBCLASSCD AS SUBCLASSCD ";
        $query .= "     FROM ";
        $query .= "         SCH_DATA T1, ";
        $query .= "         RECORD_SCORE_DAT T2 ";
        $query .= "     WHERE ";
        $query .= "         T1.SCHREGNO = T2.SCHREGNO AND ";
        $query .= "         T2.YEAR = '".CTRL_YEAR."' AND ";
        $query .= "         SEMESTER || TESTKINDCD || TESTITEMCD || SCORE_DIV IN ('".$tmpTestcd."') ";
        foreach ($recordItem as $key => $val) {
            list($item, $testcd) = explode('_', $key);
            $query .= " ), ";
            $query .= " RECORD_DATA".$testcd." AS ( ";
            $query .= "     SELECT DISTINCT ";
            $query .= "         SCHREGNO, ";
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "         CLASSCD || '-' || SCHOOL_KIND  || '-' || CURRICULUM_CD || '-' || ";
            }
            $query .= "         SUBCLASSCD AS SUBCLASSCD_".$testcd.", ";
            $query .= "         CHAIRCD AS CHAIRCD_".$testcd.", ";
            $query .= "         SCORE AS SCORE_".$testcd.", ";
            $query .= "         VALUE_DI AS DI_".$testcd." ";
            $query .= "     FROM ";
            $query .= "         RECORD_SCORE_DAT ";
            $query .= "     WHERE ";
            $query .= "         YEAR = '".CTRL_YEAR."' AND ";
            $query .= "         SCHREGNO IN ('" .str_replace(",", "','", $model->selectdata_l) ."') AND ";
            $query .= "         SEMESTER || TESTKINDCD || TESTITEMCD || SCORE_DIV = '".$testcd."' ";
        }
        $query .= " ), ";
        $query .= " RECORD_DATA AS ( ";
        $query .= "     SELECT DISTINCT ";
        $query .= "         T1.SCHREGNO, ";
        $query .= "         T1.SUBCLASSCD ";
        $cnt = 1;
        foreach ($recordItem as $key => $val) {
            list($item, $testcd) = explode('_', $key);
            $query .= "    , SUBCLASSCD_".$testcd." ";
            $query .= "    , S".$cnt.".SUBCLASSNAME AS SUBCLASSNAME_".$testcd." ";
            $query .= "    , CHAIRCD_".$testcd." ";
            $query .= "    , C".$cnt.".CHAIRNAME AS CHAIRNAME_".$testcd." ";
            $query .= "    , SCORE_".$testcd." ";
            $query .= "    , DI_".$testcd." ";
            $cnt++;
        }
        $query .= "     FROM ";
        $query .= "         SCH_SUBCLASS T1 ";
        $cnt = 1;
        foreach ($recordItem as $key => $val) {
            list($item, $testcd) = explode('_', $key);
            $query .= "     LEFT JOIN RECORD_DATA".$testcd." L".$cnt." ";
            $query .= "              ON L".$cnt.".SCHREGNO = T1.SCHREGNO ";
            $query .= "             AND L".$cnt.".SUBCLASSCD_".$testcd." = T1.SUBCLASSCD ";
            $query .= "     LEFT JOIN SUBCLASS_MST S".$cnt." ";
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "          ON S".$cnt.".CLASSCD || '-' || S".$cnt.".SCHOOL_KIND || '-' || S".$cnt.".CURRICULUM_CD || '-' || S".$cnt.".SUBCLASSCD = L".$cnt.".SUBCLASSCD_".$testcd." ";
            } else {
                $query .= "          ON S".$cnt.".SUBCLASSCD = L".$cnt.".SUBCLASSCD_".$testcd." ";
            }
            $query .= "     LEFT JOIN CHAIR_DAT C".$cnt." ";
            $query .= "              ON C".$cnt.".CHAIRCD   = L".$cnt.".CHAIRCD_".$testcd." ";
            $query .= "             AND C".$cnt.".YEAR      = '".CTRL_YEAR."' ";
            $query .= "             AND C".$cnt.".SEMESTER  = '".CTRL_SEMESTER."' ";
            $cnt++;
        }
        $query .= "     ORDER BY ";
        $query .= "         T1.SUBCLASSCD ";
        $query .= " ) ";

        $query .= " SELECT DISTINCT ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     T1.NAME, ";
        $query .= "     T1.GRADE, ";
        $query .= "     T1.HR_CLASS, ";
        $query .= "     T1.ATTENDNO, ";
        //性別区分
        if ($model->output == OUT_CODE_NAME) {          //コード＋名称
            $query .= "     VALUE(T1.SEX,'') || ',' || ";
            $query .= "     VALUE(T1.SEX_NAME,'') SEX, ";
        } elseif ($model->output == OUT_CODE_ONLY) {   //コードのみ
            $query .= "     T1.SEX SEX, ";
        } elseif ($model->output == OUT_NAME_ONLY) {   //名称のみ
            $query .= "     T1.SEX_NAME SEX, ";
        }
        $query .= "     L1.SUBCLASSCD ";
        foreach ($recordItem as $key => $val) {
            list($item, $testcd) = explode('_', $key);
            //科目
            if ($model->output == OUT_CODE_NAME) {          //コード＋名称
                $query .= "    ,VALUE(L1.SUBCLASSCD_".$testcd.",'') || ',' || ";
                $query .= "     VALUE(L1.SUBCLASSNAME_".$testcd.",'') AS SUBCLASSCD_".$testcd." ";
            } elseif ($model->output == OUT_CODE_ONLY) {   //コードのみ
                $query .= "    ,L1.SUBCLASSCD_".$testcd." ";
            } elseif ($model->output == OUT_NAME_ONLY) {   //名称のみ
                $query .= "    ,L1.SUBCLASSNAME_".$testcd." AS SUBCLASSCD_".$testcd." ";
            }
            //講座
            if ($model->output == OUT_CODE_NAME) {           //コード＋名称
                $query .= "    ,VALUE(L1.CHAIRCD_".$testcd.",'') || ',' || ";
                $query .= "     VALUE(L1.CHAIRNAME_".$testcd.",'') AS CHAIRCD_".$testcd." ";
            } elseif ($model->output == OUT_CODE_ONLY) {    //コードのみ
                $query .= "    ,L1.CHAIRCD_".$testcd." ";
            } elseif ($model->output == OUT_NAME_ONLY) {    //名称のみ
                $query .= "    ,L1.CHAIRNAME_".$testcd." AS CHAIRCD_".$testcd." ";
            }
            $query .= "    , L1.SCORE_".$testcd." ";
            $query .= "    , L1.DI_".$testcd." ";
        }
        $query .= " FROM ";
        $query .= "     SCH_DATA T1 ";
        $query .= "     LEFT JOIN RECORD_DATA L1 ON L1.SCHREGNO = T1.SCHREGNO ";
        $query .= " ORDER BY ";
        $query .= "     T1.GRADE, ";
        $query .= "     T1.HR_CLASS, ";
        $query .= "     T1.ATTENDNO, ";
        $query .= "     L1.SUBCLASSCD ";

        return $query;
    }

    //課程学科取得
    public function getCourseMajor($model)
    {
        $query  = " SELECT ";
        $query .= "     COURSECD || MAJORCD AS VALUE, ";
        $query .= "     COURSENAME || MAJORNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     V_COURSE_MAJOR_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //学年取得
    public function getGrade($model)
    {
        $query  = "SELECT ";
        $query .= "    GRADE AS VALUE, ";
        $query .= "    GRADE_NAME1 AS LABEL ";
        $query .= "FROM ";
        $query .= "    SCHREG_REGD_GDAT ";
        $query .= "WHERE ";
        $query .= "    YEAR = '".CTRL_YEAR."' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= "     AND GRADE IN (SELECT ";
                $query .= "                      REGD_G.GRADE ";
                $query .= "                   FROM ";
                $query .= "                      SCHREG_REGD_GDAT REGD_G ";
                $query .= "                   WHERE ";
                $query .= "                      REGD_G.YEAR = '".CTRL_YEAR."' ";
                $query .= "                      AND REGD_G.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind), "','")."')) ";
            }
        } elseif ($model->Properties["useSchool_KindField"] == "1") {
            $query .= "     AND GRADE IN (SELECT ";
            $query .= "                      REGD_G.GRADE ";
            $query .= "                   FROM ";
            $query .= "                      SCHREG_REGD_GDAT REGD_G ";
            $query .= "                   WHERE ";
            $query .= "                      REGD_G.YEAR = '".CTRL_YEAR."' ";
            $query .= "                      AND REGD_G.SCHOOL_KIND = '" .SCHOOLKIND ."') ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //性別取得
    public function getSex($model)
    {
        $query  = " SELECT ";
        $query .= "     NAMECD2 AS VALUE, ";
        $query .= "     ABBV1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR    = '".CTRL_YEAR."' AND ";
        $query .= "     NAMECD1 = 'Z002' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //年組取得（権限チェック）
    public function getAuth($model)
    {
        $query  = " SELECT ";
        $query .= "     GRADE || '_' ||  HR_CLASS AS VALUE, ";
        $query .= "     HR_NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_HDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR        = '".CTRL_YEAR."' AND ";
        $query .= "     SEMESTER    = '".CTRL_SEMESTER."' AND ";
        $query .= "     GRADE       = '".$model->grade."' ";
        //参照・更新可（制限付き）
        if (AUTHORITY == DEF_REFER_RESTRICT || AUTHORITY == DEF_UPDATE_RESTRICT) {
            $query .= " AND '".STAFFCD."' IN (TR_CD1, TR_CD2, TR_CD3) ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //対象外の生徒取得
    public function getSchnoIdou($model)
    {
        $date = str_replace("/", "-", CTRL_DATE);

        $query  = "";
        $query .= " SELECT T1.SCHREGNO, T1.ATTENDNO ";
        $query .= " FROM   SCHREG_REGD_DAT T1, SEMESTER_MST T2 ";
        $query .= " WHERE  T1.YEAR = '".CTRL_YEAR."' AND ";
        $query .= "        T1.SEMESTER = '".CTRL_SEMESTER."' AND ";
        $query .= "        T1.YEAR = T2.YEAR AND ";
        $query .= "        T1.SEMESTER = T2.SEMESTER AND ";
        //卒業(1)・転学(2)・退学(3)者 但し異動日が学期終了日または異動基準日より小さい場合
        $query .= "        EXISTS(SELECT 'X' FROM SCHREG_BASE_MST S1 ";
        $query .= "                WHERE S1.SCHREGNO = T1.SCHREGNO AND ";
        $query .= "                    (S1.GRD_DIV IN('1','2','3') AND S1.GRD_DATE < ";
        $query .= "                         CASE WHEN T2.EDATE < '".$date."' ";
        $query .= "                              THEN T2.EDATE ELSE '".$date."' END)) ";
        $query .= " ORDER BY T1.ATTENDNO ";

        return $query;
    }

    //生徒リストtoリスト右
    public function getStudent_right($model)
    {
        $query .= " SELECT ";
        $query .= "     T2.HR_NAME, ";
        $query .= "     T1.ATTENDNO, ";
        $query .= "     L1.NAME_SHOW, ";
        $query .= "     T1.GRADE || T1.HR_CLASS || T1.ATTENDNO || '-' || T1.SCHREGNO AS VALUE ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1 ";
        $query .= " LEFT JOIN SCHREG_REGD_HDAT T2 ";
        $query .= "        ON T2.YEAR = T1.YEAR ";
        $query .= "       AND T2.SEMESTER = T1.SEMESTER ";
        $query .= "       AND T2.GRADE = T1.GRADE ";
        $query .= "       AND T2.HR_CLASS = T1.HR_CLASS ";
        $query .= " LEFT JOIN SCHREG_BASE_MST L1 ON L1.SCHREGNO = T1.SCHREGNO ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' AND ";
        $query .= "     T1.SEMESTER = '". CTRL_SEMESTER ."' ";
        if ($model->coursemajor) {
            $query .= " AND T1.COURSECD || T1.MAJORCD = '{$model->coursemajor}' ";
        }
        if ($model->grade) {
            $query .= " AND T1.GRADE = '{$model->grade}' ";
        }
        if ($model->sex) {
            $query .= " AND L1.SEX = '{$model->sex}' ";
        }
        if ($model->grade_hr_class) {
            $query .= " AND T1.GRADE || '_' || T1.HR_CLASS = '{$model->grade_hr_class}' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //生徒リストtoリスト左
    public function getStudent_left($model)
    {
        $query .= " SELECT ";
        $query .= "     T2.HR_NAME, ";
        $query .= "     T1.ATTENDNO, ";
        $query .= "     L1.NAME_SHOW, ";
        $query .= "     T1.GRADE || T1.HR_CLASS || T1.ATTENDNO || '-' || T1.SCHREGNO AS VALUE  ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1 ";
        $query .= " LEFT JOIN SCHREG_REGD_HDAT T2 ";
        $query .= "        ON T2.YEAR = T1.YEAR ";
        $query .= "       AND T2.SEMESTER = T1.SEMESTER ";
        $query .= "       AND T2.GRADE = T1.GRADE ";
        $query .= "       AND T2.HR_CLASS = T1.HR_CLASS ";
        $query .= " LEFT JOIN SCHREG_BASE_MST L1 ON L1.SCHREGNO = T1.SCHREGNO ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' AND ";
        $query .= "     T1.SEMESTER = '". CTRL_SEMESTER ."' AND ";
        $query .= "     T1.SCHREGNO IN ('" .str_replace(",", "','", $model->selectdata_l) ."') ";
        $query .= " ORDER BY ";
        $query .= "     T1.GRADE, ";
        $query .= "     T1.HR_CLASS, ";
        $query .= "     T1.ATTENDNO ";

        return $query;
    }

    //サブシステム名取得
    public function getSubSystem()
    {
        $query  = " SELECT ";
        $query .= "     NAMECD2 AS VALUE, ";
        $query .= "     NAMECD2 || '：' || NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' AND ";
        $query .= "     NAMECD1 = 'I001' ";
        $query .= " ORDER BY ";
        $query .= "     NAMECD2 ";

        return $query;
    }

    //テスト種別
    public function getTestKind($model)
    {
        $query  = " SELECT ";
        $query .= "     T1.SEMESTER || T2.TESTKINDCD || T2.TESTITEMCD AS VALUE, ";
        if ($model->testTable == "TESTITEM_MST_COUNTFLG_NEW") {
            $query .= " T2.TESTITEMNAME AS LABEL, ";
        } else {
            $query .= " T1.SEMESTERNAME || T2.TESTITEMNAME AS LABEL, ";
        }
        $query .= "     T1.SEMESTER, ";
        $query .= "     T2.TESTKINDCD, ";
        $query .= "     T1.SEMESTERNAME || T2.TESTITEMNAME AS ITEMNAME ";
        $query .= " FROM ";
        $query .= "     SEMESTER_MST T1, ";
        $query .= "     ".$model->testTable." T2 ";
        $query .= " WHERE ";
        $query .= "         T1.YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND T1.SEMESTER < '9' ";
        $query .= "     AND T2.YEAR = T1.YEAR ";
        if ($model->testTable == "TESTITEM_MST_COUNTFLG_NEW") {
            $query .= " AND T2.SEMESTER = T1.SEMESTER ";
        }
        $query .= "     AND T2.TESTKINDCD || T2.TESTITEMCD NOT IN ('9900') ";
        $query .= " UNION ALL ";
        $query .= " SELECT ";
        $query .= "     T1.SEMESTER || '9900' AS VALUE, ";
        $query .= "     T1.SEMESTERNAME AS LABEL, ";
        $query .= "     T1.SEMESTER, ";
        $query .= "     '99' AS TESTKINDCD, ";
        $query .= "     T1.SEMESTERNAME AS ITEMNAME ";
        $query .= " FROM ";
        $query .= "     SEMESTER_MST T1 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //校種取得
    public function getSchoolKind($model)
    {
        list($grade, $hr_class) = explode('_', $model->grade_hr_class);
        $query  = " SELECT ";
        $query .= "     SCHOOL_KIND ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_GDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR     = '".CTRL_YEAR."' AND ";
        $query .= "     GRADE    = '".$grade."' ";

        return $query;
    }

    //テスト種別
    public function getTestKindSdiv($model)
    {
        $query  = " SELECT ";
        $query .= "     T1.SEMESTER || T1.TESTKINDCD || T1.TESTITEMCD || T1.SCORE_DIV AS VALUE, ";
        $query .= "     T1.SEMESTER || T1.TESTKINDCD || T1.TESTITEMCD || T1.SCORE_DIV || ' ' || T2.SEMESTERNAME || '-' || T1.TESTITEMNAME AS LABEL, ";
        $query .= "     T1.SEMESTER, ";
        $query .= "     T1.TESTKINDCD, ";
        $query .= "     T2.SEMESTERNAME || '-' || T1.TESTITEMNAME AS ITEMNAME ";
        $query .= " FROM ";
        $query .= "     TESTITEM_MST_COUNTFLG_NEW_SDIV T1 ";
        $query .= "     INNER JOIN SEMESTER_MST T2 ";
        $query .= "          ON T2.YEAR         = T1.YEAR ";
        $query .= "         AND T2.SEMESTER     = T1.SEMESTER ";
        $query .= "     INNER JOIN ADMIN_CONTROL_SDIV_DAT T3 ";
        $query .= "          ON T3.YEAR         = T1.YEAR ";
        $query .= "         AND T3.SEMESTER     = T1.SEMESTER ";
        $query .= "         AND T3.TESTKINDCD   = T1.TESTKINDCD ";
        $query .= "         AND T3.TESTITEMCD   = T1.TESTITEMCD ";
        $query .= "         AND T3.SCORE_DIV    =  T1.SCORE_DIV ";
        $query .= "         AND T3.CLASSCD      = '00' ";
        $query .= "         AND T3.SCHOOL_KIND  = '".$model->schoolkind."' ";
        $query .= "         AND T3.CURRICULUM_CD = '00' ";
        $query .= "         AND T3.SUBCLASSCD   = '000000' ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //選択フィールド取得
    public function getFieldSql($model)
    {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     KNJI100C_KAKIDASHI_LIST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND DATA_DIV = '04' ";
        $query .= " ORDER BY ";
        $query .= "     SEQ ";

        return $query;
    }

    //選択フィールド削除
    public function getDelFieldSql($model)
    {
        $query  = " DELETE FROM ";
        $query .= "     KNJI100C_KAKIDASHI_LIST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND DATA_DIV = '04' ";

        return $query;
    }

    //選択フィールド保存
    public function getFieldInsSql($model, $setSeq, $val)
    {

        //入金予定項目データ
        $data = array();
        $data["YEAR"][TEXT]            = CTRL_YEAR;
        $data["DATA_DIV"][TEXT]        = "04";
        $data["SEQ"][NUMBER]           = $setSeq;
        $data["FIELD_NAME"][TEXT]      = $val;
        $data["REGISTERCD"][TEXT]      = STAFFCD;
        $data["UPDATED"][FUNC]         = "sysdate()";

        $query = Query::insertSQL($data, "KNJI100C_KAKIDASHI_LIST");
        return $query;
    }
}
