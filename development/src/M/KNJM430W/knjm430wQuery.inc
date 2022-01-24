<?php

require_once('for_php7.php');


//通信制は基本的に通年制なので、前期のみ在籍した生徒を後期にも表示できるようにとの考えで、
//データを取得するとき学期を見ていない。
//必然的にDISTINCTが多用されますがあしからず。

class knjm430wQuery extends Query
{

    //科目（講座）リスト
    public function getChrSubCd($model)
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     T1.CHAIRNAME AS LABEL, ";
        $query .= "     T1.CHAIRCD || '-' || T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD AS VALUE ";
        $query .= " FROM ";
        $query .= "     CHAIR_DAT T1 ";
        if ($model->Properties["useRepStandarddateCourseDat"] == "1") {
            $query .= "     INNER JOIN REP_STANDARDDATE_COURSE_DAT R1 ON R1.YEAR = T1.YEAR ";
        } else {
            $query .= "     INNER JOIN REP_STANDARDDATE_DAT R1 ON R1.YEAR = T1.YEAR ";
        }
        $query .= "           AND R1.CLASSCD = T1.CLASSCD ";
        $query .= "           AND R1.SCHOOL_KIND = T1.SCHOOL_KIND ";
        $query .= "           AND R1.CURRICULUM_CD = T1.CURRICULUM_CD ";
        $query .= "           AND R1.SUBCLASSCD = T1.SUBCLASSCD ";
        //参照・更新可（制限付き）
        if (AUTHORITY != DEF_UPDATABLE) {
            $query .= "     INNER JOIN CHAIR_STF_DAT S1 ON S1.YEAR = T1.YEAR ";
            $query .= "           AND S1.SEMESTER = T1.SEMESTER ";
            $query .= "           AND S1.CHAIRCD = T1.CHAIRCD ";
            $query .= "           AND S1.STAFFCD = '" .STAFFCD ."' ";
        }
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' ";
        if ($model->Properties["knjm430wSelectChaircd"] == "1") {
            $arr = explode('-', $model->subclasscd);
            $query .= "     AND T1.CLASSCD = '".$arr[0]."' ";
            $query .= "     AND T1.SCHOOL_KIND = '".$arr[1]."' ";
            $query .= "     AND T1.CURRICULUM_CD = '".$arr[2]."' ";
            $query .= "     AND T1.SUBCLASSCD = '".$arr[3]."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

//echo "AUTHORITY = " . AUTHORITY ."<BR>";
//echo "DEF_UPDATABLE = " . DEF_UPDATABLE ."<BR>";
//echo $query;
        return $query;
    }

    //科目（講座）リスト
    public function getSubclassCd($model)
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     SUBM.SUBCLASSNAME AS LABEL, ";
        $query .= "     T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD AS VALUE ";
        $query .= " FROM ";
        $query .= "     CHAIR_DAT T1 ";
        if ($model->Properties["useRepStandarddateCourseDat"] == "1") {
            $query .= "     INNER JOIN REP_STANDARDDATE_COURSE_DAT R1 ON R1.YEAR = T1.YEAR ";
        } else {
            $query .= "     INNER JOIN REP_STANDARDDATE_DAT R1 ON R1.YEAR = T1.YEAR ";
        }
        $query .= "           AND R1.CLASSCD = T1.CLASSCD ";
        $query .= "           AND R1.SCHOOL_KIND = T1.SCHOOL_KIND ";
        $query .= "           AND R1.CURRICULUM_CD = T1.CURRICULUM_CD ";
        $query .= "           AND R1.SUBCLASSCD = T1.SUBCLASSCD ";
        //参照・更新可（制限付き）
        if (AUTHORITY != DEF_UPDATABLE) {
            $query .= "     INNER JOIN CHAIR_STF_DAT S1 ON S1.YEAR = T1.YEAR ";
            $query .= "           AND S1.SEMESTER = T1.SEMESTER ";
            $query .= "           AND S1.CHAIRCD = T1.CHAIRCD ";
            $query .= "           AND S1.STAFFCD = '" .STAFFCD ."' ";
        }
        $query .= "     INNER JOIN SUBCLASS_MST SUBM ON SUBM.CLASSCD = T1.CLASSCD ";
        $query .= "           AND SUBM.SCHOOL_KIND = T1.SCHOOL_KIND ";
        $query .= "           AND SUBM.CURRICULUM_CD = T1.CURRICULUM_CD ";
        $query .= "           AND SUBM.SUBCLASSCD = T1.SUBCLASSCD ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //テスト取得
    public function getTest($model, $addTestCdSemester = "")
    {
        if ($model->Properties["knjm430wSelectChaircd"] == "1") {
            list($cls, $sck, $cur, $subcd) = explode("-", $model->subclasscd);
        } else {
            list($chrcd, $cls, $sck, $cur, $subcd) = explode("-", $model->sub);
        }

        $query  = " WITH MAIN AS ( ";
        $query .= "     SELECT DISTINCT ";
        $query .= "         N1.NAMESPARE1 AS TESTCD, ";
        $query .= "         I1.TESTITEMNAME, ";
        $query .= "         I2.SEMESTER, ";
        $query .= "         I2.SEMESTERNAME ";
        $query .= "     FROM ";
        if ($model->Properties["useRepStandarddateCourseDat"] == "1") {
            $query .= "     REP_STANDARDDATE_COURSE_DAT T1 ";
        } else {
            $query .= "     REP_STANDARDDATE_DAT T1 ";
        }
        $query .= "     INNER JOIN V_NAME_MST N1 ON N1.YEAR = T1.YEAR ";
        $query .= "           AND N1.NAMECD1 = 'M002' ";
        $query .= "           AND N1.NAMECD2 = T1.REPORTDIV ";
        $query .= "           AND N1.NAMESPARE1 IS NOT NULL ";
        $query .= "     INNER JOIN TESTITEM_MST_COUNTFLG_NEW_SDIV I1 ON T1.YEAR = I1.YEAR ";
        $query .= "           AND I1.SEMESTER || '-' || I1.TESTKINDCD || '-' || I1.TESTITEMCD || '-' || I1.SCORE_DIV = N1.NAMESPARE1 ";
        $query .= "     INNER JOIN SEMESTER_MST I2 ON I2.YEAR = I1.YEAR ";
        $query .= "           AND I2.SEMESTER = I1.SEMESTER ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND T1.CLASSCD      = '".$cls."' ";
        $query .= "     AND T1.SCHOOL_KIND  = '".$sck."' ";
        $query .= "     AND T1.CURRICULUM_CD= '".$cur."' ";
        $query .= "     AND T1.SUBCLASSCD   = '".$subcd."' ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     T1.TESTCD ";
        $query .= "   , T1.TESTITEMNAME ";
        $query .= "   , T1.SEMESTER ";
        $query .= "   , T1.SEMESTERNAME ";
        $query .= "   , 0 AS ADD_CD ";
        $query .= " FROM ";
        $query .= "     MAIN T1 ";
        if ($model->Properties["knjm430wUseGakkiHyouka"] == "1" && $addTestCdSemester) {
            $query .= " UNION ";
            $query .= " SELECT ";
            $query .= "     '".$addTestCdSemester."' || '-99-00-08' AS TESTCD ";
            $query .= "   , '学期評価' AS TESTITEMNAME ";
            $query .= "   , T1.SEMESTER ";
            $query .= "   , T1.SEMESTERNAME ";
            $query .= "   , 1 AS ADD_CD ";
            $query .= " FROM ";
            $query .= "     SEMESTER_MST T1 ";
            $query .= " WHERE ";
            $query .= "     T1.YEAR = '".CTRL_YEAR."' ";
            $query .= "     AND T1.SEMESTER = '".$addTestCdSemester."' ";
            $query .= "     AND '".$addTestCdSemester."' || '-99-00-08' NOT IN (SELECT TESTCD FROM MAIN) ";
        }
        $query .= " ORDER BY ";
        $query .= "     TESTCD ";

        return $query;
    }

    //成績データ取得
    public function GetRecordDatdata($model)
    {
        $chrsub_array = array();
        $chrsub_array = explode("-", $model->sub);
//echo "CTRL_YEAR=".CTRL_YEAR .", CTRL_SEMESTER=".CTRL_SEMESTER .", sub=".$model->sub ."<BR>";

        $query  = " WITH CHAIR_STD AS ( ";
        $query .= "     SELECT DISTINCT ";
        $query .= "         T1.YEAR, ";
        $query .= "         T1.CLASSCD, ";
        $query .= "         T1.SCHOOL_KIND, ";
        $query .= "         T1.CURRICULUM_CD, ";
        $query .= "         T1.SUBCLASSCD, ";
        $query .= "         T2.SCHREGNO ";
        $query .= "     FROM ";
        $query .= "         CHAIR_DAT T1 ";
        $query .= "         INNER JOIN CHAIR_STD_DAT T2 ON T2.YEAR = T1.YEAR ";
        $query .= "               AND T2.SEMESTER = T1.SEMESTER ";
        $query .= "               AND T2.CHAIRCD = T1.CHAIRCD ";
        $query .= "     WHERE ";
        $query .= "         T1.YEAR = '".CTRL_YEAR."' ";
        $query .= "         AND T1.CHAIRCD = '".$chrsub_array[0]."' ";
        $query .= "         AND T1.CLASSCD      = '".$chrsub_array[1]."' ";
        $query .= "         AND T1.SCHOOL_KIND  = '".$chrsub_array[2]."' ";
        $query .= "         AND T1.CURRICULUM_CD= '".$chrsub_array[3]."' ";
        $query .= "         AND T1.SUBCLASSCD   = '".$chrsub_array[4]."' ";
        //成績（1回目）
        $query .= " ), SCORE_HIST1 AS ( ";
        $query .= "     SELECT ";
        $query .= "         T1.SEMESTER || '-' || T1.TESTKINDCD || '-' || T1.TESTITEMCD || '-' || T1.SCORE_DIV AS TESTCD, ";
        $query .= "         T1.SCHREGNO, ";
        $query .= "         T1.SCORE, ";
        $query .= "         T1.VALUE ";
        $query .= "     FROM ";
        $query .= "         RECORD_SCORE_HIST_DAT T1 ";
        $query .= "     WHERE ";
        $query .= "         T1.YEAR = '".CTRL_YEAR."' ";
        $query .= "         AND T1.CLASSCD      = '".$chrsub_array[1]."' ";
        $query .= "         AND T1.SCHOOL_KIND  = '".$chrsub_array[2]."' ";
        $query .= "         AND T1.CURRICULUM_CD= '".$chrsub_array[3]."' ";
        $query .= "         AND T1.SUBCLASSCD   = '".$chrsub_array[4]."' ";
        $query .= "         AND T1.SEQ = 1 "; //1回目のみ
        //追試チェック用
        $query .= " ), SCORE_HIST_CHECK AS ( ";
        $query .= "     SELECT ";
        $query .= "         T1.SEMESTER || '-' || T1.TESTKINDCD || '-' || T1.TESTITEMCD || '-' || T1.SCORE_DIV AS TESTCD, ";
        $query .= "         T1.SCHREGNO, ";
        $query .= "         MAX(CASE WHEN T1.SEQ > 1 THEN '1' END) AS TSUISHI_FLG ";
        $query .= "     FROM ";
        $query .= "         RECORD_SCORE_HIST_DAT T1 ";
        $query .= "     WHERE ";
        $query .= "         T1.YEAR = '".CTRL_YEAR."' ";
        $query .= "         AND T1.CLASSCD      = '".$chrsub_array[1]."' ";
        $query .= "         AND T1.SCHOOL_KIND  = '".$chrsub_array[2]."' ";
        $query .= "         AND T1.CURRICULUM_CD= '".$chrsub_array[3]."' ";
        $query .= "         AND T1.SUBCLASSCD   = '".$chrsub_array[4]."' ";
        $query .= "         AND T1.SEQ > 1 "; //追試
        $query .= "     GROUP BY ";
        $query .= "         T1.SEMESTER || '-' || T1.TESTKINDCD || '-' || T1.TESTITEMCD || '-' || T1.SCORE_DIV, ";
        $query .= "         T1.SCHREGNO ";
        //受験許可フラグ
        $query .= " ), T_PASS_FLG AS ( ";
        $query .= "     SELECT ";
        $query .= "         T1.SEMESTER || '-' || T1.TESTKINDCD || '-' || T1.TESTITEMCD || '-' || T1.SCORE_DIV AS TESTCD, ";
        $query .= "         T1.SCHREGNO, ";
        $query .= "         T1.SEM_PASS_FLG ";
        $query .= "     FROM ";
        $query .= "         SUBCLASS_STD_PASS_SDIV_DAT T1 ";
        $query .= "     WHERE ";
        $query .= "         T1.YEAR = '".CTRL_YEAR."' ";
        $query .= "         AND T1.CLASSCD      = '".$chrsub_array[1]."' ";
        $query .= "         AND T1.SCHOOL_KIND  = '".$chrsub_array[2]."' ";
        $query .= "         AND T1.CURRICULUM_CD= '".$chrsub_array[3]."' ";
        $query .= "         AND T1.SUBCLASSCD   = '".$chrsub_array[4]."' ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     T3.GRADE, ";
        $query .= "     T3.HR_CLASS, ";
        $query .= "     T3.ATTENDNO, ";
        $query .= "     T2.NAME_SHOW, ";
        $query .= "     T4.HR_NAME, ";
        //テスト
        foreach ($model->testcdArray as $key => $codeArray) {
            //成績（1回目）
            if ($model->Properties["knjm430wUseGakkiHyouka"] == "1") {
                $query .= " CASE WHEN R{$key}.TESTCD like '%-99-00-08' THEN ";
                $query .= "     R{$key}.VALUE ";
                $query .= " ELSE ";
                $query .= "     R{$key}.SCORE ";
                $query .= " END AS SCORE{$key}, ";
            } else {
                $query .= " R{$key}.SCORE AS SCORE{$key}, ";
            }
            //追試チェック用
            $query .= " V{$key}.TSUISHI_FLG AS TSUISHI_FLG{$key}, ";
            //受験許可フラグ
            $query .= " F{$key}.SEM_PASS_FLG AS SEM_PASS_FLG{$key}, ";
            //考査満点マスタ(合格点)
            $query .= " P{$key}.PASS_SCORE AS PASS_SCORE{$key}, ";
        }
        $query .= "     L1.REMARK ";
        $query .= " FROM ";
        $query .= "     CHAIR_STD T1 ";
        $query .= "     INNER JOIN SCHREG_BASE_MST T2 ON T2.SCHREGNO = T1.SCHREGNO ";
        $query .= "     INNER JOIN SCHREG_REGD_DAT T3 ON T3.SCHREGNO = T1.SCHREGNO ";
        $query .= "           AND T3.YEAR = T1.YEAR ";
        $query .= "           AND T3.SEMESTER = '".CTRL_SEMESTER."' ";
        $query .= "     INNER JOIN SCHREG_REGD_HDAT T4 ON T4.YEAR = T3.YEAR ";
        $query .= "           AND T4.SEMESTER = T3.SEMESTER ";
        $query .= "           AND T4.GRADE = T3.GRADE ";
        $query .= "           AND T4.HR_CLASS = T3.HR_CLASS ";
        //テスト
        foreach ($model->testcdArray as $key => $codeArray) {
            //成績（1回目）
            $query .= "     LEFT JOIN SCORE_HIST1 R{$key} ON R{$key}.SCHREGNO = T1.SCHREGNO ";
            $query .= "          AND R{$key}.TESTCD = '".$codeArray["TESTCD"]."' ";
            //追試チェック用
            $query .= "     LEFT JOIN SCORE_HIST_CHECK V{$key} ON V{$key}.SCHREGNO = T1.SCHREGNO ";
            $query .= "          AND V{$key}.TESTCD = '".$codeArray["TESTCD"]."' ";
            //受験許可フラグ
            $query .= "     LEFT JOIN T_PASS_FLG F{$key} ON F{$key}.SCHREGNO = T1.SCHREGNO ";
            $query .= "          AND F{$key}.TESTCD = '".$codeArray["TESTCD"]."' ";
            //考査満点マスタ(合格点)
            $query .= "     LEFT JOIN PERFECT_RECORD_SDIV_DAT P{$key} ON P{$key}.YEAR = T1.YEAR ";
            $query .= "          AND P{$key}.SEMESTER || '-' || P{$key}.TESTKINDCD || '-' || P{$key}.TESTITEMCD || '-' || P{$key}.SCORE_DIV = '".$codeArray["TESTCD"]."' ";
            $query .= "          AND P{$key}.CLASSCD = T1.CLASSCD ";
            $query .= "          AND P{$key}.SCHOOL_KIND = T1.SCHOOL_KIND ";
            $query .= "          AND P{$key}.CURRICULUM_CD = T1.CURRICULUM_CD ";
            $query .= "          AND P{$key}.SUBCLASSCD = T1.SUBCLASSCD ";
            $query .= "          AND P{$key}.GRADE = CASE WHEN P{$key}.DIV = '01' THEN '00' ELSE T3.GRADE END ";
            $query .= "          AND P{$key}.COURSECD || P{$key}.MAJORCD || P{$key}.COURSECODE = CASE WHEN P{$key}.DIV IN ('01','02') THEN '00000000' ELSE T3.COURSECD || T3.MAJORCD || T3.COURSECODE END ";
        }
        //備考
        $query .= "     LEFT JOIN RECORD_REMARK_DAT L1 ON L1.YEAR = T1.YEAR ";
        $query .= "          AND L1.CLASSCD = T1.CLASSCD ";
        $query .= "          AND L1.SCHOOL_KIND = T1.SCHOOL_KIND ";
        $query .= "          AND L1.CURRICULUM_CD = T1.CURRICULUM_CD ";
        $query .= "          AND L1.SUBCLASSCD = T1.SUBCLASSCD ";
        $query .= "          AND L1.SCHREGNO = T1.SCHREGNO ";
        //クラス番号順の場合はクラスまで指定する
        if ($model->order == 2) {
            $query .= " ORDER BY ";
            $query .= "     T3.GRADE, ";
            $query .= "     T3.HR_CLASS, ";
            $query .= "     T3.ATTENDNO ";
        } else {
            $query .= " ORDER BY ";
//            $query .= "     T1.SCHREGNO ";
            $query .= "     SUBSTR(T1.SCHREGNO, 1, 4) DESC, ";
            $query .= "     SUBSTR(T1.SCHREGNO, 5, 4) ";
        }

//echo $query;
        return $query;
    }

    /* データ更新処理 */
    public function getUpdateQuery($model)
    {
        $db = Query::dbCheckOut();

        $chrsub_array = array();
        $chrsub_array = explode("-", $model->sub);

        for ($i = 1; $i <= get_count($model->setdata["SCHREGNO"]); $i++) {
            $year = CTRL_YEAR;
            $schno = $model->setdata["SCHREGNO"][$i];
            $remark_key = ""; //備考で使用
            //成績
            foreach ($model->testcdArray as $key => $codeArray) {
                $test_array = array();
                $test_array = explode("-", $codeArray["TESTCD"]);
                //データセット
                $data = array();
                $data["YEAR"][TEXT]             = $year;
                $data["SEMESTER"][TEXT]         = $test_array[0];
                $data["TESTKINDCD"][TEXT]       = $test_array[1];
                $data["TESTITEMCD"][TEXT]       = $test_array[2];
                $data["SCORE_DIV"][TEXT]        = $test_array[3];
                $data["CLASSCD"][TEXT]          = $chrsub_array[1];
                $data["SCHOOL_KIND"][TEXT]      = $chrsub_array[2];
                $data["CURRICULUM_CD"][TEXT]    = $chrsub_array[3];
                $data["SUBCLASSCD"][TEXT]       = $chrsub_array[4];
                $data["SCHREGNO"][TEXT]         = $schno;
                $data["SEQ"][NUMBER]            = 1; //1回目のみ
                $data["TEST_DATE"][TEXT]        = CTRL_DATE ;
                $data["CHAIRCD"][TEXT]          = $chrsub_array[0];
                $score = $model->setdata["SCORE".$key][$i];
                $passScore = $model->setdata["PASS_SCORE".$key][$i];
                if ($model->Properties["knjm430wUseGakkiHyouka"] == "1" && "99-00-08" == $test_array[1]."-".$test_array[2]."-".$test_array[3]) {
                    $data["VALUE"][NUMBER]          = $score;
                } else {
                    $data["SCORE"][NUMBER]          = $score;
                    $data["VALUE"][NUMBER]          = (strlen($passScore) && strlen($score) && $passScore <= $score) ? $score : ""; //合格点以上の時、SCOREをセット
                }
                $data["REGISTERCD"][TEXT]       = STAFFCD ;
                $data["UPDATED"][FUNC]          = "sysdate()";
                //条件
                $where  = " WHERE YEAR = '{$year}' ";
                $where .= "     AND SEMESTER || '-' || TESTKINDCD || '-' || TESTITEMCD || '-' || SCORE_DIV = '".$codeArray["TESTCD"]."' ";
                $where .= "     AND CLASSCD      = '".$chrsub_array[1]."' ";
                $where .= "     AND SCHOOL_KIND  = '".$chrsub_array[2]."' ";
                $where .= "     AND CURRICULUM_CD= '".$chrsub_array[3]."' ";
                $where .= "     AND SUBCLASSCD   = '".$chrsub_array[4]."' ";
                $where .= "     AND SCHREGNO = '{$schno}'";
                $where .= "     AND SEQ = 1 "; //1回目のみ
                $query = "SELECT SCORE FROM RECORD_SCORE_HIST_DAT ".$where;
                //skip

                if ($model->Properties["knjm430wCreateScoreNullRecord"] != "1" && $score == $db->getOne($query)) {
                    continue;
                }
                //削除
                $query = "DELETE FROM RECORD_SCORE_HIST_DAT ".$where;
                $db->query($query);
                //追加


                if (strlen($score) || $model->Properties["knjm430wCreateScoreNullRecord"] == "1" && $model->setdata["SEM_PASS_FLG".$key][$i] == "1") {
                    $query = Query::insertSQL($data, "RECORD_SCORE_HIST_DAT");
                    $db->query($query);
                }
/***
                //更新
                if (0 < $db->getOne($query)) {
                    $query = Query::updateSQL($data, "RECORD_SCORE_HIST_DAT", $where);
                    $db->query($query);
                //追加
                } else {
                    $query = Query::insertSQL($data, "RECORD_SCORE_HIST_DAT");
                    $db->query($query);
                }
***/
                $remark_key = "1";
            }

            //備考
            if ($remark_key != "") {
                //削除
                $query  = "DELETE FROM RECORD_REMARK_DAT";
                $query .= " WHERE YEAR = '{$year}'";
                $query .= "     AND CLASSCD      = '".$chrsub_array[1]."' ";
                $query .= "     AND SCHOOL_KIND  = '".$chrsub_array[2]."' ";
                $query .= "     AND CURRICULUM_CD= '".$chrsub_array[3]."' ";
                $query .= "     AND SUBCLASSCD   = '".$chrsub_array[4]."' ";
                $query .= "     AND SCHREGNO = '{$schno}'";
                $db->query($query);
                //追加
                $data = array();
                $data["YEAR"][TEXT]             = $year;
                $data["CLASSCD"][TEXT]          = $chrsub_array[1];
                $data["SCHOOL_KIND"][TEXT]      = $chrsub_array[2];
                $data["CURRICULUM_CD"][TEXT]    = $chrsub_array[3];
                $data["SUBCLASSCD"][TEXT]       = $chrsub_array[4];
                $data["SCHREGNO"][TEXT]         = $schno;
                $data["REMARK"][TEXT]           = $model->setdata["REMARK"][$i];
                $data["REGISTERCD"][TEXT]       = STAFFCD ;
                $data["UPDATED"][FUNC]          = "sysdate()";
                $query = Query::insertSQL($data, "RECORD_REMARK_DAT");
                $db->query($query);
            }
        }

        Query::dbCheckIn($db);
    }
}
