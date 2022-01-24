<?php

require_once('for_php7.php');


class knjm432kquery extends Query
{

    //課程学科コンボ取得
    public function getCourseMajor($model)
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     VCM.COURSECD || VCM.MAJORCD || ':' || VCM.COURSENAME || VCM.MAJORNAME AS LABEL, ";
        $query .= "     VCM.COURSECD || '-' || VCM.MAJORCD AS VALUE ";
        $query .= " FROM ";
        $query .= "     TESTITEM_MST_COUNTFLG_NEW_GCM_SDIV TEST_GCM ";
        $query .= "     INNER JOIN V_COURSE_MAJOR_MST VCM ON TEST_GCM.YEAR = VCM.YEAR ";
        $query .= "           AND TEST_GCM.COURSECD = VCM.COURSECD ";
        $query .= "           AND TEST_GCM.MAJORCD = VCM.MAJORCD ";
        $query .= " WHERE ";
        $query .= "     TEST_GCM.YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND TEST_GCM.SCHOOLCD = '".SCHOOLCD."' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= "     AND TEST_GCM.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind), "','")."') ";
            }
        } elseif ($model->Properties["useSchool_KindField"] == "1") {
            $query .= "     AND TEST_GCM.SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //科目名の取得
    public function getSubclassName($model)
    {
        $subclass_array = array();
        $subclass_array = explode("-", $model->field["SUBCLASSCD"]);
        $query  = " SELECT ";
        $query .= "     SUBCLASSNAME ";
        $query .= " FROM ";
        $query .= "     SUBCLASS_MST ";
        $query .= " WHERE ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     CLASSCD          = '".$subclass_array[0]."' AND ";
            $query .= "     SCHOOL_KIND      = '".$subclass_array[1]."' AND ";
            $query .= "     CURRICULUM_CD    = '".$subclass_array[2]."' AND ";
            $query .= "     SUBCLASSCD       = '".$subclass_array[3]."' ";
        } else {
            $query .= "     SUBCLASSCD       = '".$model->field["SUBCLASSCD"]."' ";
        }
        return $query;
    }
    //講座名の取得
    public function getChairName($model)
    {
        $query  = " SELECT ";
        $query .= "     CHAIRNAME ";
        $query .= " FROM ";
        $query .= "     CHAIR_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND SEMESTER = '".CTRL_SEMESTER."' ";
        $query .= "     AND CHAIRCD = '".$model->field["CHAIRCD"]."' ";
        return $query;
    }

    //「評価にリンクを付けるか？」のフラグを取得
    public function getNameMstD048()
    {
        $query  = " SELECT ";
        $query .= "     NAMESPARE1 ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = 'D048' AND ";
        $query .= "     NAMECD2 = '01' ";

        $db = Query::dbCheckOut();
        $rtnRow = array();
        $rtnRow = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);

        return $rtnRow;
    }

    //D065登録科目か
    public function getD065Sub($model)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND NAMECD1 = 'D065' ";
        $query .= "     AND NAME1 = '".$model->field["SUBCLASSCD"]."' ";

        return $query;
    }

    //D065登録科目の入力値チェック
    public function getD001List()
    {
        $query  = " SELECT ";
        $query .= "     NAMECD2 AS VALUE, ";
        $query .= "     NAME1 AS MARK ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND NAMECD1 = 'D001' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //名称マスタA023に登録されているの学校校種を取得
    public function getSchoolkindQuery()
    {
        $query  = " SELECT ";
        $query .= "     MIN(NAME1) AS SCHOOL_KIND ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = 'A023' ";

        return $query;
    }

    //学期取得
    public function getSemester()
    {
        $query  = "SELECT ";
        $query .= "    SEMESTERNAME, ";
        $query .= "    SEMESTER ";
        $query .= "FROM ";
        $query .= "    SEMESTER_MST ";
        $query .= "WHERE ";
        $query .= "    YEAR = '".CTRL_YEAR."' ";
        $query .= "    AND SEMESTER <> '9' ";
        $query .= "ORDER BY ";
        $query .= "    SEMESTER ";

        return $query;
    }

    //処理対象科目
    public function getHankiCnt($sem)
    {
        $query  = "";
        $query .= " SELECT ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     SUBCLASS_DETAIL_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND SUBCLASS_SEQ = '012' ";
        $query .= "     AND SUBCLASS_REMARK{$sem} = '1' ";
        return $query;
    }

    //科目データの取得
    public function getSubclassMst($model, $cmCnt)
    {
        $query  = "";
        $query .= " WITH REPLACE AS ( ";
        $query .= "     SELECT DISTINCT ";
        $query .= "         '1' AS DIV, ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     COMBINED_CLASSCD, COMBINED_SCHOOL_KIND, COMBINED_CURRICULUM_CD, ";
        }
        $query .= "         COMBINED_SUBCLASSCD ";
        $query .= "     FROM ";
        $query .= "         SUBCLASS_REPLACE_COMBINED_DAT ";
        $query .= "     WHERE ";
        $query .= "         YEAR = '".CTRL_YEAR."' ";
        $query .= " ) ";
        $query .= " SELECT ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     T3.CLASSCD || '-' || T3.SCHOOL_KIND || '-' || T3.CURRICULUM_CD || '-' || T3.SUBCLASSCD AS VALUE, ";
            $query .= "     CASE WHEN L1.COMBINED_SUBCLASSCD IS NOT NULL THEN '●' ELSE '　' END || T3.CLASSCD || '-' || T3.SCHOOL_KIND || '-' || T3.CURRICULUM_CD || '-' || T3.SUBCLASSCD || '：' || T3.SUBCLASSNAME AS LABEL ";
        } else {
            $query .= "     T3.SUBCLASSCD AS VALUE, ";
            $query .= "     CASE WHEN L1.COMBINED_SUBCLASSCD IS NOT NULL THEN '●' ELSE '　' END || T3.SUBCLASSCD || '：' || T3.SUBCLASSNAME AS LABEL ";
        }
        $query .= " FROM ";
        $query .= "     CHAIR_DAT T1 ";
        $query .= "     INNER JOIN CHAIR_STD_DAT T2 ";
        $query .= "          ON T2.YEAR       = T1.YEAR ";
        $query .= "         AND T2.SEMESTER   = T1.SEMESTER ";
        $query .= "         AND T2.CHAIRCD    = T1.CHAIRCD ";
        $query .= "     INNER JOIN CHAIR_STF_DAT T4 ";
        $query .= "          ON T4.YEAR       = T1.YEAR ";
        $query .= "         AND T4.SEMESTER   = T1.SEMESTER ";
        $query .= "         AND T4.CHAIRCD    = T1.CHAIRCD ";
        //更新可能(制限付)
        if (AUTHORITY != DEF_UPDATABLE) {
            $query .= "     AND T4.STAFFCD = '".STAFFCD."' ";
        }
        $query .= "     INNER JOIN SUBCLASS_MST T3 ON ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     T3.CLASSCD = T1.CLASSCD AND ";
            $query .= "     T3.SCHOOL_KIND = T1.SCHOOL_KIND AND ";
            $query .= "     T3.CURRICULUM_CD = T1.CURRICULUM_CD AND ";
        }
        $query .= "         T3.SUBCLASSCD = T1.SUBCLASSCD ";
        $query .= "     LEFT JOIN REPLACE L1 ON L1.DIV = '1' ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     AND T3.CLASSCD = L1.COMBINED_CLASSCD ";
            $query .= "     AND T3.SCHOOL_KIND = L1.COMBINED_SCHOOL_KIND ";
            $query .= "     AND T3.CURRICULUM_CD = L1.COMBINED_CURRICULUM_CD ";
        }
        $query .= "         AND T3.SUBCLASSCD = L1.COMBINED_SUBCLASSCD ";
        //指定された課程学科の生徒
        if ($model->Properties["use_school_detail_gcm_dat"] == '1' && 1 < $cmCnt) {
            $query .= "     INNER JOIN SCHREG_REGD_DAT W2 ON W2.SCHREGNO = T2.SCHREGNO ";
            $query .= "       AND W2.YEAR = T2.YEAR ";
            $query .= "       AND W2.SEMESTER = T2.SEMESTER ";
            $query .= "       AND W2.COURSECD || '-' || W2.MAJORCD = '{$model->field["COURSE_MAJOR"]}' ";
            $query .= "     INNER JOIN SCHREG_REGD_GDAT W3 ON W3.YEAR = W2.YEAR ";
            $query .= "       AND W3.GRADE = W2.GRADE ";
            if ($model->Properties["use_prg_schoolkind"] == "1") {
                if ($model->selectSchoolKind) {
                    $query .= "   AND W3.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind), "','")."') ";
                }
            } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
                $query .= "   AND W3.SCHOOL_KIND = '".SCHOOLKIND."' ";
            }
        }
        $query .= " WHERE ";
        $query .= "         T1.YEAR       = '".CTRL_YEAR."' ";
        $query .= "     AND T1.SEMESTER   = '".CTRL_SEMESTER."' ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            if ($model->Properties["use_prg_schoolkind"] == "1") {
                if ($model->selectSchoolKind) {
                    $query .= " AND T3.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind), "','")."') ";
                }
            } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
                $query .= " AND T3.SCHOOL_KIND = '".SCHOOLKIND."' ";
            }
        }
        $query .= "     AND (T3.SUBCLASSCD NOT LIKE '9%' OR T3.SUBCLASSCD LIKE '90%' ) ";
        //科目ラジオ・・・科目コンボのリスト条件
        //半期認定科目
        //1:1学期(前期)　2:2学期(後期)　3:3学期　4:通年
        if ($model->field["SUBCLASS_DIV"] == '1' || $model->field["SUBCLASS_DIV"] == '2' || $model->field["SUBCLASS_DIV"] == '3' || $model->field["SUBCLASS_DIV"] == '4') {
            if ($model->field["SUBCLASS_DIV"] == '4') {
                $in = "NOT IN";
                $subRem = "W2.SUBCLASS_REMARK1 = '1' OR W2.SUBCLASS_REMARK2 = '1' OR W2.SUBCLASS_REMARK3 = '1'";
            } else {
                $in = "IN";
                $subRem = "W2.SUBCLASS_REMARK{$model->field["SUBCLASS_DIV"]} = '1'";
            }
            //sql
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= " AND T3.CLASSCD || T3.SCHOOL_KIND || T3.CURRICULUM_CD || T3.SUBCLASSCD {$in} (SELECT W2.CLASSCD || W2.SCHOOL_KIND || W2.CURRICULUM_CD || W2.SUBCLASSCD FROM SUBCLASS_DETAIL_DAT W2 WHERE W2.YEAR = '".CTRL_YEAR."' AND W2.SUBCLASS_SEQ = '012' AND ({$subRem})) ";
            } else {
                $query .= " AND T3.SUBCLASSCD {$in} (SELECT W2.SUBCLASSCD FROM SUBCLASS_DETAIL_DAT W2 WHERE W2.YEAR = '".CTRL_YEAR."' AND W2.SUBCLASS_SEQ = '012' AND ({$subRem})) ";
            }
        }
        //5:全て・・・初期値
        //6:合併先
        if ($model->field["SUBCLASS_DIV"] == '6') {
            $query .= "     AND L1.COMBINED_SUBCLASSCD IS NOT NULL ";
        }
        $query .= " GROUP BY ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     T3.CLASSCD, T3.SCHOOL_KIND, T3.CURRICULUM_CD, ";
        }
        $query .= "     T3.SUBCLASSCD, ";
        $query .= "     L1.COMBINED_SUBCLASSCD, ";
        $query .= "     T3.SUBCLASSNAME ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";
        return $query;
    }

    //講座データの取得
    public function selectChairQuery($model, $cmCnt)
    {
        $subclass_array = array();
        $subclass_array = explode("-", $model->field["SUBCLASSCD"]);

        $query  = " SELECT ";
        $query .= "     T1.CHAIRCD AS VALUE, ";
        $query .= "     T1.CHAIRCD || '：' || T1.CHAIRNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     CHAIR_DAT T1 ";
        $query .= "     INNER JOIN CHAIR_STD_DAT T2 ";
        $query .= "          ON T2.YEAR       = T1.YEAR ";
        $query .= "         AND T2.SEMESTER   = T1.SEMESTER ";
        $query .= "         AND T2.CHAIRCD    = T1.CHAIRCD ";
        $query .= "     INNER JOIN CHAIR_STF_DAT T4 ";
        $query .= "          ON T4.YEAR       = T1.YEAR ";
        $query .= "         AND T4.SEMESTER   = T1.SEMESTER ";
        $query .= "         AND T4.CHAIRCD    = T1.CHAIRCD ";
        //更新可能(制限付)
        if (AUTHORITY != DEF_UPDATABLE) {
            $query .= "     AND T4.STAFFCD = '".STAFFCD."' ";
        }
        //指定された課程学科の生徒
        if ($model->Properties["use_school_detail_gcm_dat"] == '1' && 1 < $cmCnt) {
            $query .= "     INNER JOIN SCHREG_REGD_DAT W2 ON W2.SCHREGNO = T2.SCHREGNO ";
            $query .= "       AND W2.YEAR = T2.YEAR ";
            $query .= "       AND W2.SEMESTER = T2.SEMESTER ";
            $query .= "       AND W2.COURSECD || '-' || W2.MAJORCD = '{$model->field["COURSE_MAJOR"]}' ";
            $query .= "     INNER JOIN SCHREG_REGD_GDAT W3 ON W3.YEAR = W2.YEAR ";
            $query .= "       AND W3.GRADE = W2.GRADE ";
            if ($model->Properties["use_prg_schoolkind"] == "1") {
                if ($model->selectSchoolKind) {
                    $query .= "   AND W3.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind), "','")."') ";
                }
            } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
                $query .= "   AND W3.SCHOOL_KIND = '".SCHOOLKIND."' ";
            }
        }
        $query .= " WHERE ";
        $query .= "     T1.year       = '".CTRL_YEAR."' AND ";
        $query .= "     T1.semester   = '".CTRL_SEMESTER."' AND ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     T1.CLASSCD          = '".$subclass_array[0]."' AND ";
            $query .= "     T1.SCHOOL_KIND      = '".$subclass_array[1]."' AND ";
            $query .= "     T1.CURRICULUM_CD    = '".$subclass_array[2]."' AND ";
            $query .= "     T1.SUBCLASSCD       = '".$subclass_array[3]."' ";
        } else {
            $query .= "     T1.subclasscd = '".$model->field["SUBCLASSCD"]."' ";
        }
        $query .= " GROUP BY ";
        $query .= "     T1.CHAIRCD, ";
        $query .= "     T1.CHAIRNAME ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";
        return $query;
    }

    //V学校マスタ
    public function getVSchoolMst()
    {
        $query  = "";
        $query .= " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     V_SCHOOL_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";

        return $query;
    }

    //累積最大日取得
    public function getMax($subclasscd, $model)
    {
        $subclass_array = array();
        $subclass_array = explode("-", $model->field["SUBCLASSCD"]);
        $query  = "SELECT DISTINCT year ";
        $query .= "              ,month ";
        $query .= "              ,CASE MONTH WHEN '01' THEN CHAR(SMALLINT(month) + 12)  ";
        $query .= "                          WHEN '02' THEN CHAR(SMALLINT(month) + 12)  ";
        $query .= "                          WHEN '03' THEN CHAR(SMALLINT(month) + 12) ELSE month END AS tmp_month ";
        $query .= "              ,semester ";
        $query .= "              ,appointed_day ";
        $query .= "         FROM attend_subclass_dat ";
        $query .= "        WHERE COPYCD     = '0' ";
        $query .= "          AND year       = '".CTRL_YEAR."' ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "          AND CLASSCD        = '".$subclass_array[0]."' ";
            $query .= "          AND SCHOOL_KIND    = '".$subclass_array[1]."' ";
            $query .= "          AND CURRICULUM_CD  = '".$subclass_array[2]."' ";
            $query .= "          AND SUBCLASSCD     = '".$subclass_array[3]."' ";
        } else {
            $query .= "          AND subclasscd = '".$model->field["SUBCLASSCD"]."' ";
        }
        $query .= "          AND semester   = (SELECT MAX(semester) FROM attend_subclass_dat ";
        $query .= "                             WHERE COPYCD     = '0' ";
        $query .= "                               AND year       = '".CTRL_YEAR."' ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "                               AND CLASSCD        = '".$subclass_array[0]."' ";
            $query .= "                               AND SCHOOL_KIND    = '".$subclass_array[1]."' ";
            $query .= "                               AND CURRICULUM_CD  = '".$subclass_array[2]."' ";
            $query .= "                               AND SUBCLASSCD     = '".$subclass_array[3]."') ";
        } else {
            $query .= "                               AND subclasscd = '".$model->field["SUBCLASSCD"]."') ";
        }
        $query .= "ORDER BY tmp_month DESC ";
        return $query;
    }

    //全体のデータの取得
    public function selectQuery($model, $execute_date, $cmCnt)
    {
        $subclass_array = array();
        $subclass_array = explode("-", $model->field["SUBCLASSCD"]);

        $query  = "";
        $query .= " SELECT W1.schregno";
        $query .= "       ,W2.grade, W2.hr_class, W2.hr_name, W2.attendno, W2.inoutcd, W2.name_show, W2.grd_date, W2.COURSE";
        $query .= "       ,W3.transfer_sdate, W3.transfer_edate ";
        $query .= "       ,W4.PROV_FLG ";
        $query .= "   FROM ";
        /* 生徒一覧 */
        $query .= "    (SELECT schregno ";
        $query .= "       FROM chair_std_dat ";
        $query .= "      WHERE year = '".CTRL_YEAR."'  ";
        $query .= "        AND chaircd = '".$model->field["CHAIRCD"]."'  ";
        $query .= "        AND '".$execute_date."' between appdate AND appenddate ";
        $query .= "    ) W1 ";
        /* 基礎情報 */
        $query .= " INNER JOIN ";
        $query .= "    (SELECT T1.year,T1.grade,T0.hr_name, T1.hr_class,T1.attendno,T2.inoutcd,T2.name_show,T2.schregno, T2.grd_date, T1.COURSECD || T1.MAJORCD || T1.COURSECODE as COURSE ";
        $query .= "       FROM schreg_regd_hdat T0, schreg_regd_dat T1, schreg_base_mst T2 ";
        $query .= "      WHERE T0.year     = T1.year";
        $query .= "        AND T0.semester = T1.semester";
        $query .= "        AND T0.grade    = T1.grade";
        $query .= "        AND T0.hr_class = T1.hr_class";
        $query .= "        AND T1.year     = '".CTRL_YEAR."' ";
        $query .= "        AND T1.semester = '".CTRL_SEMESTER."' ";
        //指定された課程学科の生徒
        if ($model->Properties["use_school_detail_gcm_dat"] == '1' && 1 < $cmCnt) {
            $query .= "        AND T1.COURSECD || '-' || T1.MAJORCD = '{$model->field["COURSE_MAJOR"]}' ";
        }
        $query .= "        AND T1.schregno = T2.schregno ) W2 ";
        $query .= "   ON W2.schregno = W1.schregno ";
        /* 異動情報 */
        $query .= " LEFT OUTER JOIN ";
        $query .= "    (SELECT T1.schregno, MIN(T1.transfer_sdate) AS transfer_sdate, MAX(T1.transfer_edate) AS transfer_edate";
        $query .= "       FROM schreg_transfer_dat T1 ";
        $query .= "      WHERE FISCALYEAR(T1.transfer_sdate) = '".CTRL_YEAR."'";
        $query .= "      GROUP BY T1.schregno ) W3 ";
        $query .= "   ON W3.schregno = W1.schregno ";
        /* 仮評定情報 */
        $query .= " LEFT JOIN ";
        $query .= "    (SELECT  SCHREGNO, PROV_FLG ";
        $query .= "     FROM   RECORD_PROV_FLG_DAT ";
        $query .= "     WHERE  YEAR = '".CTRL_YEAR."' ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "       AND  CLASSCD          = '".$subclass_array[0]."' ";
            $query .= "       AND  SCHOOL_KIND      = '".$subclass_array[1]."' ";
            $query .= "       AND  CURRICULUM_CD    = '".$subclass_array[2]."' ";
            $query .= "       AND  SUBCLASSCD       = '".$subclass_array[3]."' ";
        } else {
            $query .= "       AND  SUBCLASSCD = '".$model->field["SUBCLASSCD"]."' ";
        }
        $query .= "    ) W4 ";
        $query .= "   ON W4.SCHREGNO = W1.SCHREGNO ";
        $query .= " ORDER BY W2.grade, W2.hr_class,W2.attendno ";
        return $query;
    }

    //成績データ
    public function getScore($model, $execute_date, $cmCnt)
    {
        $subclass_array = array();
        $subclass_array = explode("-", $model->field["SUBCLASSCD"]);

        $query  = " WITH SELECT_T AS ( ".knjm432kQuery::selectQuery($model, $execute_date, $cmCnt).") ";
        $query .= " SELECT ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     T1.SEMESTER || T1.TESTKINDCD || T1.TESTITEMCD || T1.SCORE_DIV AS TESTCD, ";
        $query .= "     T1.SCORE, ";
        $query .= "     T1.COMP_CREDIT, ";
        $query .= "     T1.GET_CREDIT, ";
        $query .= "     T1.VALUE_DI "; //欠試'*'
        $query .= " FROM ";
        $query .= "     RECORD_SCORE_HIST_DAT T1 ";
        $query .= " WHERE ";
        $query .= "         T1.YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND T1.CLASSCD       = '{$subclass_array[0]}' ";
        $query .= "     AND T1.SCHOOL_KIND   = '{$subclass_array[1]}' ";
        $query .= "     AND T1.CURRICULUM_CD = '{$subclass_array[2]}' ";
        $query .= "     AND T1.SUBCLASSCD    = '{$subclass_array[3]}' ";
        $query .= "     AND T1.SCHREGNO IN (SELECT I1.SCHREGNO FROM SELECT_T I1) ";
        $query .= "     AND T1.SEQ = 1 "; //1回目のみ
        return $query;
    }

    //追指導データ
    public function getRecordSlump($year, $testcd, $schregno, $grade, $model)
    {
        $subclass_array = array();
        $subclass_array = explode("-", $model->field["SUBCLASSCD"]);
        $query  = "";
        //相対評定マスタ
        $query .= "WITH RELA_CNT AS ( ";
        $query .= "    SELECT ";
        $query .= "        'CNT' AS KEY, ";
        $query .= "        COUNT(*) AS CNT ";
        $query .= "    FROM ";
        $query .= "        RELATIVEASSESS_MST RELA ";
        $query .= "    WHERE ";
        $query .= "        RELA.ASSESSCD = '3' ";
        $query .= "        AND RELA.GRADE = '{$grade}' ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "    AND RELA.CLASSCD        = '".$subclass_array[0]."' ";
            $query .= "    AND RELA.SCHOOL_KIND    = '".$subclass_array[1]."' ";
            $query .= "    AND RELA.CURRICULUM_CD  = '".$subclass_array[2]."' ";
            $query .= "    AND RELA.SUBCLASSCD     = '".$subclass_array[3]."' ";
        } else {
            $query .= "    AND RELA.SUBCLASSCD     = '{$model->field["SUBCLASSCD"]}' ";
        }
        $query .= "    ) ";
        //メイン
        $query .= "SELECT ";
        $query .= "    CASE WHEN 0 < RELA_CNT.CNT THEN RELA.ASSESSLEVEL ELSE ASSESS.ASSESSLEVEL END AS ASSESSLEVEL, ";
        $query .= "    SLUMP.SCORE, ";
        $query .= "    SLUMP.MARK ";
        $query .= "FROM   RECORD_SLUMP_SDIV_DAT SLUMP ";
        $query .= "       LEFT JOIN ASSESS_MST ASSESS ";
        $query .= "             ON  ASSESS.ASSESSCD = '3' ";
        $query .= "             AND SLUMP.SCORE BETWEEN ASSESS.ASSESSLOW AND ASSESS.ASSESSHIGH ";
        $query .= "       LEFT JOIN RELA_CNT RELA_CNT ";
        $query .= "             ON  RELA_CNT.KEY = 'CNT' ";
        $query .= "       LEFT JOIN RELATIVEASSESS_MST RELA ";
        $query .= "             ON  RELA.ASSESSCD = '3' ";
        $query .= "             AND SLUMP.SCORE BETWEEN RELA.ASSESSLOW AND RELA.ASSESSHIGH ";
        $query .= "             AND RELA.GRADE = '{$grade}' ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "         AND RELA.CLASSCD        = '".$subclass_array[0]."' ";
            $query .= "         AND RELA.SCHOOL_KIND    = '".$subclass_array[1]."' ";
            $query .= "         AND RELA.CURRICULUM_CD  = '".$subclass_array[2]."' ";
            $query .= "         AND RELA.SUBCLASSCD     = '".$subclass_array[3]."' ";
        } else {
            $query .= "         AND RELA.SUBCLASSCD     = '{$model->field["SUBCLASSCD"]}' ";
        }
        $query .= "WHERE  SLUMP.YEAR          = '{$year}' ";
        $query .= "  AND  SLUMP.SEMESTER || SLUMP.TESTKINDCD || SLUMP.TESTITEMCD || SLUMP.SCORE_DIV = '{$testcd}' ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "  AND SLUMP.CLASSCD        = '".$subclass_array[0]."' ";
            $query .= "  AND SLUMP.SCHOOL_KIND    = '".$subclass_array[1]."' ";
            $query .= "  AND SLUMP.CURRICULUM_CD  = '".$subclass_array[2]."' ";
            $query .= "  AND SLUMP.SUBCLASSCD     = '".$subclass_array[3]."' ";
        } else {
            $query .= "  AND SLUMP.SUBCLASSCD     = '{$model->field["SUBCLASSCD"]}' ";
        }
        $query .= "  AND  SLUMP.SCHREGNO      = '{$schregno}' ";
        return $query;
    }

    //(年間ごとに集計）する場合
    public function getAttendData($schno, $absent_cov, $absent_cov_late, $schoolRow, $model)
    {
        $subclass_array = array();
        $subclass_array = explode("-", $model->field["SUBCLASSCD"]);

        $notice = "SUM(value(notice,0)) + SUM(value(nonotice,0)) + SUM(value(sick,0)) + SUM(value(nurseoff,0))";
        //学校マスタの各フラグを参照し「休学・公欠・出廷・忌引・出廷（伝染病）」を欠課に含める処理。
        if ($schoolRow["SUB_OFFDAYS"]  == "1") {
            $notice .= " + SUM(value(OFFDAYS,0))";
        }
        if ($schoolRow["SUB_ABSENT"]   == "1") {
            $notice .= " + SUM(value(ABSENT,0))";
        }
        if ($schoolRow["SUB_SUSPEND"]  == "1") {
            $notice .= " + SUM(value(SUSPEND,0))";
        }
        if ($schoolRow["SUB_MOURNING"] == "1") {
            $notice .= " + SUM(value(MOURNING,0))";
        }
        if ($schoolRow["SUB_VIRUS"]    == "1") {
            $notice .= " + SUM(value(VIRUS,0))";
        }
        if ($schoolRow["SUB_KOUDOME"]  == "1") {
            $notice .= " + SUM(value(KOUDOME,0))";
        }
        $late_early = "SUM(value(late,0)) + SUM(value(early,0))";
        /* 出欠累積情報 */
        $query  = "SELECT schregno ";
        $query .= "      ,SUM(VALUE(LESSON,0)) - SUM(VALUE(OFFDAYS,0)) - SUM(VALUE(ABROAD,0))";
        if ($schoolRow["SUB_OFFDAYS"]  == "1") {
            $query .= " + SUM(VALUE(OFFDAYS,0))";
        }
        $query .= "       AS LESSON "; //授業時数
        $query .= "      ,SUM(VALUE(LESSON,0)) - SUM(VALUE(OFFDAYS,0)) - SUM(VALUE(ABROAD,0)) - SUM(VALUE(SUSPEND,0)) - SUM(VALUE(MOURNING,0))";
        if ($model->Properties["useVirus"] == 'true') {
            $query .= " - SUM(VALUE(VIRUS,0))";
        }
        if ($model->Properties["useKoudome"] == 'true') {
            $query .= " - SUM(VALUE(KOUDOME,0))";
        }
        if ($schoolRow["SUB_OFFDAYS"]  == "1") {
            $query .= " + SUM(VALUE(OFFDAYS,0))";
        }
        if ($schoolRow["SUB_SUSPEND"]  == "1") {
            $query .= " + SUM(VALUE(SUSPEND,0))";
        }
        if ($schoolRow["SUB_MOURNING"] == "1") {
            $query .= " + SUM(VALUE(MOURNING,0))";
        }
        if ($schoolRow["SUB_VIRUS"]    == "1") {
            $query .= " + SUM(VALUE(VIRUS,0))";
        }
        if ($schoolRow["SUB_KOUDOME"]  == "1") {
            $query .= " + SUM(VALUE(KOUDOME,0))";
        }
        $query .= "       AS MLESSON "; //出席すべき時数
        $query .= "       ,".$notice."  AS T_NOTICE ";
        //小数点ありAdd
        if ($absent_cov == "4" && (is_numeric($absent_cov_late) && (int)$absent_cov_late != 0)) {
            $query .= "   ,MOD(".$late_early.",".$absent_cov_late.") AS T_LATEEARLY ";
            $query .= "   ,DECIMAL((FLOAT(".$late_early.") / ".$absent_cov_late.") + (".$notice."),4,1) AS NOTICE_LATE ";
        } elseif ($absent_cov == "5" && (is_numeric($absent_cov_late) && (int)$absent_cov_late != 0)) {
            $query .= "   ,CASE WHEN MOD({$late_early} , {$absent_cov_late}) >= {$schoolRow["AMARI_KURIAGE"]} ";
            $query .= "         THEN 0 ";
            $query .= "         ELSE MOD({$late_early},{$absent_cov_late}) ";
            $query .= "    END AS T_LATEEARLY ";
            $query .= "   ,(({$late_early}) / {$absent_cov_late}) + ({$notice}) + (CASE WHEN MOD({$late_early} , {$absent_cov_late}) >= {$schoolRow["AMARI_KURIAGE"]} ";
            $query .= "                                                  THEN 1 ";
            $query .= "                                                  ELSE 0 ";
            $query .= "                                             END) AS NOTICE_LATE ";
        } elseif ($absent_cov == "2" && (is_numeric($absent_cov_late) && (int)$absent_cov_late != 0)) {
            $query .= "   ,MOD(".$late_early.",".$absent_cov_late.") AS T_LATEEARLY ";
            $query .= "   ,((".$late_early.") / ".$absent_cov_late.") + (".$notice.") AS NOTICE_LATE ";
        } else {
            $query .= "   ,".$late_early."     AS T_LATEEARLY ";
            $query .= "   ,".$notice."  AS NOTICE_LATE ";
        }
        $query .= "       ,".$late_early."     AS LATE_EARLY ";
        $query .= "  FROM attend_subclass_dat  ";
        $query .= " WHERE COPYCD     = '0' ";
        $query .= "   AND year       = '".CTRL_YEAR."' ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "   AND CLASSCD       = '".$subclass_array[0]."' ";
            $query .= "   AND SCHOOL_KIND   = '".$subclass_array[1]."' ";
            $query .= "   AND CURRICULUM_CD = '".$subclass_array[2]."' ";
            $query .= "   AND SUBCLASSCD    = '".$subclass_array[3]."' ";
        } else {
            $query .= "   AND SUBCLASSCD    = '{$model->field["SUBCLASSCD"]}' ";
        }
        $query .= "   AND schregno  = '{$schno}' ";
        $query .= " GROUP BY schregno";

        return $query;
    }
    //(学期ごとに集計）する場合
    public function getAttendData2($schno, $absent_cov, $absent_cov_late, $schoolRow, $model)
    {
        $subclass_array = array();
        $subclass_array = explode("-", $model->field["SUBCLASSCD"]);

        $notice = "SUM(value(notice,0)) + SUM(value(nonotice,0)) + SUM(value(sick,0)) + SUM(value(nurseoff,0))";
        //学校マスタの各フラグを参照し「休学・公欠・出廷・忌引・出廷（伝染病）」を欠課に含める処理。
        if ($schoolRow["SUB_OFFDAYS"]  == "1") {
            $notice .= " + SUM(value(OFFDAYS,0))";
        }
        if ($schoolRow["SUB_ABSENT"]   == "1") {
            $notice .= " + SUM(value(ABSENT,0))";
        }
        if ($schoolRow["SUB_SUSPEND"]  == "1") {
            $notice .= " + SUM(value(SUSPEND,0))";
        }
        if ($schoolRow["SUB_MOURNING"] == "1") {
            $notice .= " + SUM(value(MOURNING,0))";
        }
        if ($schoolRow["SUB_VIRUS"]    == "1") {
            $notice .= " + SUM(value(VIRUS,0))";
        }
        if ($schoolRow["SUB_KOUDOME"]  == "1") {
            $notice .= " + SUM(value(KOUDOME,0))";
        }
        $late_early = "SUM(value(late,0)) + SUM(value(early,0))";
        $query  = "SELECT schregno ";
        $query .= "       ,SUM(LESSON)      AS LESSON";
        $query .= "       ,SUM(MLESSON)     AS MLESSON";
        $query .= "       ,SUM(T_NOTICE)    AS T_NOTICE";
        $query .= "       ,SUM(T_LATEEARLY) AS T_LATEEARLY";
        $query .= "       ,SUM(NOTICE_LATE) AS NOTICE_LATE";
        $query .= "       ,SUM(LATE_EARLY)  AS LATE_EARLY";
        $query .= "  FROM ";
        $query .= "       (SELECT  T1.schregno ";
        $query .= "               ,T1.semester ";

        $query .= "               ,SUM(VALUE(LESSON,0)) - SUM(VALUE(OFFDAYS,0)) - SUM(VALUE(ABROAD,0))";
        if ($schoolRow["SUB_OFFDAYS"]  == "1") {
            $query .= " + SUM(VALUE(OFFDAYS,0))";
        }
        $query .= "                AS LESSON "; //授業時数

        $query .= "               ,SUM(VALUE(LESSON,0)) - SUM(VALUE(OFFDAYS,0)) - SUM(VALUE(ABROAD,0)) - SUM(VALUE(SUSPEND,0)) - SUM(VALUE(MOURNING,0))";
        if ($model->Properties["useVirus"] == 'true') {
            $query .= " - SUM(VALUE(VIRUS,0))";
        }
        if ($model->Properties["useKoudome"] == 'true') {
            $query .= " - SUM(VALUE(KOUDOME,0))";
        }
        if ($schoolRow["SUB_OFFDAYS"]  == "1") {
            $query .= " + SUM(VALUE(OFFDAYS,0))";
        }
        if ($schoolRow["SUB_SUSPEND"]  == "1") {
            $query .= " + SUM(VALUE(SUSPEND,0))";
        }
        if ($schoolRow["SUB_MOURNING"] == "1") {
            $query .= " + SUM(VALUE(MOURNING,0))";
        }
        if ($schoolRow["SUB_VIRUS"]    == "1") {
            $query .= " + SUM(VALUE(VIRUS,0))";
        }
        if ($schoolRow["SUB_KOUDOME"]  == "1") {
            $query .= " + SUM(VALUE(KOUDOME,0))";
        }
        $query .= "                AS MLESSON "; //出席すべき時数

        $query .= "               ,".$notice." AS T_NOTICE ";
        //小数点ありAdd
        if ($absent_cov == "3" && (is_numeric($absent_cov_late) && (int)$absent_cov_late != 0)) {
            $query .= "           ,MOD(".$late_early.",".$absent_cov_late.") AS T_LATEEARLY ";
            $query .= "           ,DECIMAL((FLOAT(".$late_early.") / ".$absent_cov_late.") + (".$notice."),4,1) AS NOTICE_LATE ";
        } elseif ($absent_cov == "1" && (is_numeric($absent_cov_late) && (int)$absent_cov_late != 0)) {
            $query .= "           ,MOD(".$late_early.",".$absent_cov_late.") AS T_LATEEARLY ";
            $query .= "           ,((".$late_early.") / ".$absent_cov_late.") + (".$notice.") AS NOTICE_LATE ";
        } else {
            $query .= "           ,".$late_early."    AS T_LATEEARLY ";
            $query .= "           ,".$notice." AS NOTICE_LATE ";
        }
        $query .= "               ,".$late_early."    AS LATE_EARLY ";
        $query .= "          FROM attend_subclass_dat T1   ";
        $query .= "         WHERE COPYCD        = '0' ";
        $query .= "           AND T1.year       = '".CTRL_YEAR."' ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "           AND T1.CLASSCD        = '".$subclass_array[0]."' ";
            $query .= "           AND T1.SCHOOL_KIND    = '".$subclass_array[1]."' ";
            $query .= "           AND T1.CURRICULUM_CD  = '".$subclass_array[2]."' ";
            $query .= "           AND T1.SUBCLASSCD     = '".$subclass_array[3]."' ";
        } else {
            $query .= "           AND T1.SUBCLASSCD     = '{$model->field["SUBCLASSCD"]}' ";
        }
        $query .= "           AND T1.schregno   = '{$schno}' ";
        $query .= "         GROUP BY T1.schregno, T1.semester) T1 ";
        $query .= " GROUP BY schregno ";

        return $query;
    }

    /* 成績データ更新処理 */
    public function update($model)
    {
        //DB接続
        $db = Query::dbCheckOut();

        $subclass_array = array();
        $subclass_array = explode("-", $model->field["SUBCLASSCD"]);

        //年度
        $year = CTRL_YEAR;

        //合併先科目か
        $query = knjm432kQuery::getCombined($model, $year);
        $rowCombined = $db->getRow($query, DB_FETCHMODE_ASSOC);

        for ($i = 0; $i < $model->field["COUNT"]; $i++) {
            $schno = $model->fields["SCHREGNO"][$i];
            $testcd = "9990009";

            $data = array();
            $data["YEAR"][TEXT]         = $year;
            $data["SEMESTER"][TEXT]     = substr($testcd, 0, 1);
            $data["TESTKINDCD"][TEXT]   = substr($testcd, 1, 2);
            $data["TESTITEMCD"][TEXT]   = substr($testcd, 3, 2);
            $data["SCORE_DIV"][TEXT]    = substr($testcd, 5, 2);
            //教育課程対応
            if ($model->Properties["useCurriculumcd"] == '1') {
                $data["CLASSCD"][TEXT]          = $subclass_array[0];
                $data["SCHOOL_KIND"][TEXT]      = $subclass_array[1];
                $data["CURRICULUM_CD"][TEXT]    = $subclass_array[2];
                $data["SUBCLASSCD"][TEXT]       = $subclass_array[3];
            } else {
                $data["SUBCLASSCD"][TEXT]       = $model->field["SUBCLASSCD"];
            }
            $data["SCHREGNO"][TEXT]     = $schno;
            $data["SEQ"][NUMBER]        = 1; //1回目のみ
            $data["TEST_DATE"][TEXT]    = CTRL_DATE ;
            $data["CHAIRCD"][TEXT]      = $model->field["CHAIRCD"];

            $score = $model->fields["SCORE"][$i];
            $data["SCORE"][NUMBER]      = $score == "*" ? "" : $score;
            $data["VALUE_DI"][TEXT]     = $score == "*" ? $score : "";
            //履修単位・修得単位
            //単位の自動計算
            //履修単位／修得単位が入力可で、学年評定が入力されていて、履修単位／修得単位がNULLの時
            //仕様追加。プロパティnotOpenCreditが'1'の時は、履修単位／修得単位の窓をあけない。また、単位の自動計算は、常に上書きする。
            if ((strlen($score) && $score != "*") && (!strlen($model->fields["COMP_CREDIT"][$i]) && !strlen($model->fields["GET_CREDIT"][$i]) || $model->Properties["notOpenCredit"] == '1')) {
                //合併先科目か
                if ($rowCombined["FLG"] == "1" || $rowCombined["FLG"] == "2") {
                    $query = knjm432kQuery::getCreditCombined($model, $year, $schno, $score, $testcd, $rowCombined["FLG"]);
                } else {
                    $query = knjm432kQuery::getCredit($model, $year, $schno, $score);
                }
                $rowCredit = $db->getRow($query, DB_FETCHMODE_ASSOC);
                $data["COMP_CREDIT"][NUMBER]    = $rowCredit["COMP_CREDIT"];
                $data["GET_CREDIT"][NUMBER]     = $rowCredit["GET_CREDIT"];
            } else {
                $data["COMP_CREDIT"][NUMBER]    = ($model->Properties["notOpenCredit"] == '1') ? "" : $model->fields["COMP_CREDIT"][$i];
                $data["GET_CREDIT"][NUMBER]     = ($model->Properties["notOpenCredit"] == '1') ? "" : $model->fields["GET_CREDIT"][$i];
            }

            $data["REGISTERCD"][TEXT]   = STAFFCD;
            $data["UPDATED"][FUNC]      = "sysdate()";

            //削除
            $query = knjm432kQuery::deleteRecordScore($year, $testcd, $schno, $model);
            $db->query($query);
            //成績が1件も入力されていない場合、RECORD_SCORE_HIST_DATのレコードは作成しない（考査ごと）
            //testInsArray：成績が１つでも入力された考査コードを配列に保管
            if (in_array($testcd, $model->testInsArray)) {
                //追加
                $query = Query::insertSQL($data, "RECORD_SCORE_HIST_DAT");
                $db->query($query);
            }

            //評定フラグ・・・仮評定(1)、本評定(NULL)
            //学年評定を更新する場合、評定フラグは本評定(NULL)とする・・・レコードを削除またはNULLで更新(どちらでもよい)
            $query = knjm432kQuery::delRecordProvFlgDat($year, $model->field["SUBCLASSCD"], $schno, $model);
            $db->query($query);
            if ($model->fields["PROV_FLG"][$i] == '1') {
                $query = knjm432kQuery::insRecordProvFlgDat($year, $model->field["SUBCLASSCD"], $schno, $model);
                $db->query($query);
            }
        }

        //DB切断
        Query::dbCheckIn($db);
    }

    //単位マスタから履修単位・修得単位を取得
    public function getCredit($model, $year, $schno, $score)
    {
        $subclass_array = array();
        $subclass_array = explode("-", $model->field["SUBCLASSCD"]);

        $query  = "";
        $query .= " SELECT ";
        $query .= "     T1.SCHREGNO, ";
        //D065登録科目
        if ($model->subD065 == "1") {
            $query .= "     case when 0 <= {$score} then T4.CREDITS ";
            $query .= "          else NULL end AS COMP_CREDIT, ";
            $query .= "     case when 0 <= {$score} then T4.CREDITS ";
            $query .= "          else NULL end AS GET_CREDIT ";
        } else {
            $query .= "     case when 1 <= {$score} then T4.CREDITS ";
            $query .= "          when 0 = {$score} then 0 ";
            $query .= "          else NULL end AS COMP_CREDIT, ";
            $query .= "     case when 1 < {$score} then T4.CREDITS ";
            $query .= "          when 1 = {$score} then 0 ";
            $query .= "          when 0 = {$score} then 0 ";
            $query .= "          else NULL end AS GET_CREDIT ";
        }
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1 ";
        $query .= "     LEFT JOIN CREDIT_MST T4 ON T4.YEAR = '{$year}' ";
        $query .= "                            AND T4.COURSECD = T1.COURSECD ";
        $query .= "                            AND T4.MAJORCD = T1.MAJORCD ";
        $query .= "                            AND T4.GRADE = T1.GRADE ";
        $query .= "                            AND T4.COURSECODE = T1.COURSECODE ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "                        AND T4.CLASSCD        = '".$subclass_array[0]."' ";
            $query .= "                        AND T4.SCHOOL_KIND    = '".$subclass_array[1]."' ";
            $query .= "                        AND T4.CURRICULUM_CD  = '".$subclass_array[2]."' ";
            $query .= "                        AND T4.SUBCLASSCD     = '".$subclass_array[3]."' ";
        } else {
            $query .= "                        AND T4.SUBCLASSCD     = '{$model->field["SUBCLASSCD"]}' ";
        }
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '{$year}' AND ";
        $query .= "     T1.SEMESTER = '".CTRL_SEMESTER."' AND ";
        $query .= "     T1.SCHREGNO = '{$schno}' ";
        return $query;
    }

    //合併先科目か
    public function getCombined($model, $year)
    {
        $subclass_array = array();
        $subclass_array = explode("-", $model->field["SUBCLASSCD"]);

        $query  = "";
        $query .= " SELECT ";
        $query .= "     CALCULATE_CREDIT_FLG AS FLG ";
        $query .= " FROM ";
        $query .= "     SUBCLASS_REPLACE_COMBINED_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$year}' ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= " AND COMBINED_CLASSCD        = '".$subclass_array[0]."' ";
            $query .= " AND COMBINED_SCHOOL_KIND    = '".$subclass_array[1]."' ";
            $query .= " AND COMBINED_CURRICULUM_CD  = '".$subclass_array[2]."' ";
            $query .= " AND COMBINED_SUBCLASSCD     = '".$subclass_array[3]."' ";
        } else {
            $query .= " AND COMBINED_SUBCLASSCD     = '{$model->field["SUBCLASSCD"]}' ";
        }
        return $query;
    }

    //合併先科目の履修単位・修得単位を取得
    //(固定)合併先科目の単位マスタから
    //(加算)合併元科目の成績データから
    public function getCreditCombined($model, $year, $schno, $score, $testcd, $flg)
    {
        $subclass_array = array();
        $subclass_array = explode("-", $model->field["SUBCLASSCD"]);

        $query  = "";
        $query .= " SELECT ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     case when 1 <= {$score} and '{$flg}' = '1' then T4.CREDITS ";
        $query .= "          when 1 <= {$score} and '{$flg}' = '2' then T5.COMP_CREDIT ";
        $query .= "          when 0 = {$score} then 0 ";
        $query .= "          else NULL end AS COMP_CREDIT, ";
        $query .= "     case when 1 < {$score} and '{$flg}' = '1' then T4.CREDITS ";
        $query .= "          when 1 < {$score} and '{$flg}' = '2' and D015.NAMESPARE1 = 'Y' then T5.GET_CREDIT_Y ";
        $query .= "          when 1 < {$score} and '{$flg}' = '2' then T5.GET_CREDIT ";
        $query .= "          when 1 = {$score} then 0 ";
        $query .= "          when 0 = {$score} then 0 ";
        $query .= "          else NULL end AS GET_CREDIT ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1 ";
        //(固定)合併先科目の単位マスタから
        $query .= "     LEFT JOIN CREDIT_MST T4 ON T4.YEAR = '{$year}' ";
        $query .= "                            AND T4.COURSECD = T1.COURSECD ";
        $query .= "                            AND T4.MAJORCD = T1.MAJORCD ";
        $query .= "                            AND T4.GRADE = T1.GRADE ";
        $query .= "                            AND T4.COURSECODE = T1.COURSECODE ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "                        AND T4.CLASSCD        = '".$subclass_array[0]."' ";
            $query .= "                        AND T4.SCHOOL_KIND    = '".$subclass_array[1]."' ";
            $query .= "                        AND T4.CURRICULUM_CD  = '".$subclass_array[2]."' ";
            $query .= "                        AND T4.SUBCLASSCD     = '".$subclass_array[3]."' ";
        } else {
            $query .= "                        AND T4.SUBCLASSCD     = '{$model->field["SUBCLASSCD"]}' ";
        }
        //(加算)合併元科目の成績データから
        $query .= "     LEFT JOIN ( ";
        $query .= "         SELECT ";
        $query .= "             T2.SCHREGNO, ";
        $query .= "             SUM(T2.COMP_CREDIT) AS COMP_CREDIT, ";
        $query .= "             SUM(T2.GET_CREDIT) AS GET_CREDIT, ";
        $query .= "             SUM(case when 1 < T2.SCORE then T2.GET_CREDIT ";
        $query .= "                      when 1 = T2.SCORE then T2.COMP_CREDIT end) AS GET_CREDIT_Y ";
        $query .= "         FROM ";
        $query .= "             SUBCLASS_REPLACE_COMBINED_DAT T1, ";
        $query .= "             RECORD_SCORE_HIST_DAT T2 ";
        $query .= "         WHERE ";
        $query .= "             T1.YEAR = '{$year}' ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "         AND T1.COMBINED_CLASSCD        = '".$subclass_array[0]."' ";
            $query .= "         AND T1.COMBINED_SCHOOL_KIND    = '".$subclass_array[1]."' ";
            $query .= "         AND T1.COMBINED_CURRICULUM_CD  = '".$subclass_array[2]."' ";
            $query .= "         AND T1.COMBINED_SUBCLASSCD     = '".$subclass_array[3]."' ";
        } else {
            $query .= "         AND T1.COMBINED_SUBCLASSCD     = '{$model->field["SUBCLASSCD"]}' ";
        }
        $query .= "             AND T2.YEAR = '{$year}' ";
        $query .= "             AND T2.SEMESTER || T2.TESTKINDCD || T2.TESTITEMCD || T2.SCORE_DIV = '{$testcd}' ";
        $query .= "             AND T2.SCHREGNO = '{$schno}' ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "         AND T2.CLASSCD = T1.ATTEND_CLASSCD ";
            $query .= "         AND T2.SCHOOL_KIND = T1.ATTEND_SCHOOL_KIND ";
            $query .= "         AND T2.CURRICULUM_CD = T1.ATTEND_CURRICULUM_CD ";
        }
        $query .= "             AND T2.SUBCLASSCD = T1.ATTEND_SUBCLASSCD ";
        $query .= "             AND T2.SEQ = 1 "; //1回目のみ
        $query .= "         GROUP BY ";
        $query .= "             T2.SCHREGNO ";
        $query .= "     ) T5 ON T5.SCHREGNO = T1.SCHREGNO ";
        //元科目が評定１の場合、履修単位を修得単位に加算・・・但し、名称マスタの予備１がYの時
        $query .= "     LEFT JOIN V_NAME_MST D015 ON D015.YEAR = T1.YEAR ";
        $query .= "                              AND D015.NAMECD1 = 'D015' ";
        $query .= "                              AND D015.NAMECD2 = '01' ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '{$year}' AND ";
        $query .= "     T1.SEMESTER = '".CTRL_SEMESTER."' AND ";
        $query .= "     T1.SCHREGNO = '{$schno}' ";
        return $query;
    }

    //更新
    public function delInsRecordScore($year, $testcd, $model, $schno, $score, $db)
    {
        $subclass_array = array();
        $subclass_array = explode("-", $model->field["SUBCLASSCD"]);

        $data = array();
        $data["YEAR"][TEXT]         = $year;
        $data["SEMESTER"][TEXT]     = substr($testcd, 0, 1);
        $data["TESTKINDCD"][TEXT]   = substr($testcd, 1, 2);
        $data["TESTITEMCD"][TEXT]   = substr($testcd, 3, 2);
        $data["SCORE_DIV"][TEXT]    = substr($testcd, 5, 2);
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $data["CLASSCD"][TEXT]          = $subclass_array[0];
            $data["SCHOOL_KIND"][TEXT]      = $subclass_array[1];
            $data["CURRICULUM_CD"][TEXT]    = $subclass_array[2];
            $data["SUBCLASSCD"][TEXT]       = $subclass_array[3];
        } else {
            $data["SUBCLASSCD"][TEXT]       = $model->field["SUBCLASSCD"];
        }
        $data["SCHREGNO"][TEXT]     = $schno;
        $data["SEQ"][NUMBER]        = 1; //1回目のみ
        $data["TEST_DATE"][TEXT]    = CTRL_DATE ;
        $data["SCORE"][NUMBER]      = $score == "*" ? "" : $score;
        $data["VALUE_DI"][TEXT]     = $score == "*" ? $score : "";
        $data["CHAIRCD"][TEXT]      = $model->field["CHAIRCD"];
        $data["REGISTERCD"][TEXT]   = STAFFCD;
        $data["UPDATED"][FUNC]      = "sysdate()";

        //削除
        $query = knjm432kQuery::deleteRecordScore($year, $testcd, $schno, $model);
        $db->query($query);
        //追加
        $query = Query::insertSQL($data, "RECORD_SCORE_HIST_DAT");
        $db->query($query);
    }

    //テーブルRECORD_SCORE_HIST_DATのレコードを削除
    public function deleteRecordScore($year, $testcd, $schno, $model)
    {
        $subclass_array = array();
        $subclass_array = explode("-", $model->field["SUBCLASSCD"]);

        $query  = "";
        $query .= "DELETE ";
        $query .= "FROM   RECORD_SCORE_HIST_DAT ";
        $query .= "WHERE  YEAR          = '{$year}' ";
        $query .= "  AND  SEMESTER || TESTKINDCD || TESTITEMCD || SCORE_DIV = '{$testcd}' ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "  AND CLASSCD        = '".$subclass_array[0]."' ";
            $query .= "  AND SCHOOL_KIND    = '".$subclass_array[1]."' ";
            $query .= "  AND CURRICULUM_CD  = '".$subclass_array[2]."' ";
            $query .= "  AND SUBCLASSCD     = '".$subclass_array[3]."' ";
        } else {
            $query .= "  AND SUBCLASSCD     = '{$model->field["SUBCLASSCD"]}' ";
        }
        $query .= "  AND  SCHREGNO      = '{$schno}' ";
        $query .= "  AND  SEQ = 1 "; //1回目のみ
        return $query;
    }

    //テーブルRECORD_PROV_FLG_DATのレコードを削除
    public function delRecordProvFlgDat($year, $subclass, $schno, $model)
    {
        $subclass_array = array();
        $subclass_array = explode("-", $subclass);

        $query  = "";
        $query .= "DELETE ";
        $query .= "FROM   RECORD_PROV_FLG_DAT ";
        $query .= "WHERE  YEAR          = '{$year}' ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "  AND  CLASSCD        = '".$subclass_array[0]."' ";
            $query .= "  AND  SCHOOL_KIND    = '".$subclass_array[1]."' ";
            $query .= "  AND  CURRICULUM_CD  = '".$subclass_array[2]."' ";
            $query .= "  AND  SUBCLASSCD     = '".$subclass_array[3]."' ";
        } else {
            $query .= "  AND  SUBCLASSCD     = '{$subclass}' ";
        }
        $query .= "  AND  SCHREGNO      = '{$schno}' ";
        return $query;
    }

    //RECORD_PROV_FLG_DATのレコードを追加
    public function insRecordProvFlgDat($year, $subclass, $schno, $model)
    {
        $subclass_array = array();
        $subclass_array = explode("-", $subclass);

        $data = array();
        $data["YEAR"][TEXT]         = $year;
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $data["CLASSCD"][TEXT]       = $subclass_array[0];
            $data["SCHOOL_KIND"][TEXT]   = $subclass_array[1];
            $data["CURRICULUM_CD"][TEXT] = $subclass_array[2];
            $data["SUBCLASSCD"][TEXT]    = $subclass_array[3];
        } else {
            $data["SUBCLASSCD"][TEXT]    = $subclass;
        }
        $data["SCHREGNO"][TEXT]     = $schno;
        //評定フラグ・・・仮評定(1)、本評定(NULL)
        $data["PROV_FLG"][TEXT]     = "1";
        $data["REGISTERCD"][TEXT]   = STAFFCD;
        $data["UPDATED"][FUNC]      = "SYSDATE()";

        $query = Query::insertSQL($data, "RECORD_PROV_FLG_DAT");
        return $query;
    }
}
