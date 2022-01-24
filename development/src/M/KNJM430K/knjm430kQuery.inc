<?php

require_once('for_php7.php');

//通信制は基本的に通年制なので、前期のみ在籍した生徒を後期にも表示できるようにとの考えで、
//データを取得するとき学期を見ていない。
//必然的にDISTINCTが多用されますがあしからず。

class knjm430kQuery extends Query {

    //科目（講座）リスト
    function getChrSubCd($model) {
        $query  = " SELECT DISTINCT ";
        $query .= "     T1.CHAIRNAME AS LABEL, ";
        $query .= "     T1.CHAIRCD || '-' || T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD AS VALUE ";
        $query .= " FROM ";
        $query .= "     CHAIR_DAT T1 ";
        //参照・更新可（制限付き）
        if (AUTHORITY != DEF_UPDATABLE) {
            $query .= "     INNER JOIN CHAIR_STF_DAT S1 ON S1.YEAR = T1.YEAR ";
            $query .= "           AND S1.SEMESTER = T1.SEMESTER ";
            $query .= "           AND S1.CHAIRCD = T1.CHAIRCD ";
            $query .= "           AND S1.STAFFCD = '" .STAFFCD ."' ";
        }
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //学級・講座データの取得
    function selectChairQuery($model){
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $subclass_array = array();
            $subclass_array = explode("-", $model->sub);
        }
        $query  = "   SELECT DISTINCT";
        $query .= "       t1.CHAIRCD AS VALUE, ";
        $query .= "       t1.CHAIRCD || '：' || t1.CHAIRNAME AS LABEL";
        $query .= "   FROM ";
        $query .= "       CHAIR_DAT t1, ";
        $query .= "       CHAIR_STF_DAT t2 ";
        $query .= "   WHERE ";
        $query .= "       t1.YEAR = '".CTRL_YEAR."' AND ";
        $query .= "       t1.YEAR = t2.YEAR AND ";
        //$query .= "       t1.SEMESTER = '".CTRL_SEMESTER."' AND ";
        $query .= "       t1.SEMESTER = t2.SEMESTER AND ";
        $query .= "       t1.CHAIRCD = t2.CHAIRCD AND ";
        //更新可能(制限付)
        if(AUTHORITY != DEF_UPDATABLE){
          $query .= "       t2.STAFFCD = '".STAFFCD."' AND ";
        }
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     t1.CLASSCD          = '".$subclass_array[1]."' AND ";
            $query .= "     t1.SCHOOL_KIND      = '".$subclass_array[2]."' AND ";
            $query .= "     t1.CURRICULUM_CD    = '".$subclass_array[3]."' AND ";
            $query .= "     t1.SUBCLASSCD       = '".$subclass_array[4]."' ";
        } else {
            $query .= "       t1.SUBCLASSCD = '".SUBSTR($model->sub, 0, 6)."' ";
        }
        $query .= "   ORDER BY ";
        $query .= "       t1.CHAIRCD ";
        return $query;
    }

    //テスト取得
    function getTest($model) {

        $query  = " SELECT DISTINCT ";
        $query .= "     I1.SEMESTER || '-' || I1.TESTKINDCD || '-' || I1.TESTITEMCD || '-' || I1.SCORE_DIV AS TESTCD, ";
        $query .= "     I1.TESTITEMNAME, ";
        $query .= "     I2.SEMESTER, ";
        $query .= "     I2.SEMESTERNAME ";
        $query .= " FROM ";
        $query .= "     TESTITEM_MST_COUNTFLG_NEW_SDIV I1 ";
        $query .= "     INNER JOIN SEMESTER_MST I2 ON I2.YEAR = I1.YEAR ";
        $query .= "           AND I2.SEMESTER <> '9' ";
        $query .= "           AND I2.SEMESTER = I1.SEMESTER ";
        $query .= " WHERE ";
        $query .= "     I1.YEAR = '".CTRL_YEAR."' ";
        $query .= " ORDER BY ";
        $query .= "     TESTCD ";

        return $query;
    }

    //考査種別カウント取得
    function getTestCnt($model) {

        $query  = " WITH TEST_COUNT AS ( ";
        $query .= " SELECT DISTINCT ";
        $query .= "     I1.SEMESTER || '-' || I1.TESTKINDCD || '-' || I1.TESTITEMCD || '-' || I1.SCORE_DIV AS TESTCD, ";
        $query .= "     I1.TESTITEMNAME, ";
        $query .= "     I2.SEMESTER, ";
        $query .= "     I2.SEMESTERNAME ";
        $query .= " FROM ";
        $query .= "     TESTITEM_MST_COUNTFLG_NEW_SDIV I1 ";
        $query .= "     INNER JOIN SEMESTER_MST I2 ON I2.YEAR = I1.YEAR ";
        $query .= "           AND I2.SEMESTER <> '9' ";
        $query .= "           AND I2.SEMESTER = I1.SEMESTER ";
        $query .= " WHERE ";
        $query .= "     I1.YEAR = '".CTRL_YEAR."' ";
        $query .= ") ";
        $query .= "SELECT ";
        $query .= "    COUNT(*) ";
        $query .= "FROM ";
        $query .= "    TEST_COUNT  ";

        return $query;
    }

    //学期カウント取得
    function getSemeCnt($model) {

        $query  = " SELECT ";
        $query .= "   COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "   SEMESTER_MST ";
        $query .= " WHERE ";
        $query .= "   YEAR = '".CTRL_YEAR."' ";
        $query .= "   AND SEMESTER <> '9' ";

        return $query;
    }

    //受験資格取得
    function GetSemPassFlg($model, $schregno, $codeArray) {
        $chrsub_array = array();
        $chrsub_array = explode("-", $model->sub);

        $query  = "     SELECT ";
        $query .= "         SEM_PASS_FLG ";
        $query .= "     FROM ";
        $query .= "         SUBCLASS_STD_PASS_SDIV_DAT ";
        $query .= "     WHERE ";
        $query .= "         YEAR = '".CTRL_YEAR."' ";
        $query .= "         AND SEMESTER || '-' || TESTKINDCD || '-' || TESTITEMCD || '-' || SCORE_DIV = '".$codeArray."' ";
        $query .= "         AND CLASSCD       = '".$chrsub_array[1]."' ";
        $query .= "         AND SCHOOL_KIND   = '".$chrsub_array[2]."' ";
        $query .= "         AND CURRICULUM_CD = '".$chrsub_array[3]."' ";
        $query .= "         AND SUBCLASSCD    = '".$chrsub_array[4]."' ";
        $query .= "         AND SCHREGNO      = '".$schregno."' ";

        return $query;
    }

    //成績データ取得
    function GetRecordDatdata($model) {
        $chrsub_array = array();
        $chrsub_array = explode("-", $model->sub);

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
        $query .= "         AND T1.CHAIRCD = '".$model->chair."' ";
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
        $query .= "         T1.VALUE_DI ";
        $query .= "     FROM ";
        $query .= "         RECORD_SCORE_HIST_DAT T1 ";
        $query .= "     WHERE ";
        $query .= "         T1.YEAR = '".CTRL_YEAR."' ";
        $query .= "         AND T1.CLASSCD      = '".$chrsub_array[1]."' ";
        $query .= "         AND T1.SCHOOL_KIND  = '".$chrsub_array[2]."' ";
        $query .= "         AND T1.CURRICULUM_CD= '".$chrsub_array[3]."' ";
        $query .= "         AND T1.SUBCLASSCD   = '".$chrsub_array[4]."' ";
        $query .= "         AND T1.SEQ = 1 "; //1回目のみ
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
        $query .= "         AND T1.CLASSCD       = '".$chrsub_array[1]."' ";
        $query .= "         AND T1.SCHOOL_KIND   = '".$chrsub_array[2]."' ";
        $query .= "         AND T1.CURRICULUM_CD = '".$chrsub_array[3]."' ";
        $query .= "         AND T1.SUBCLASSCD    = '".$chrsub_array[4]."' ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     T3.GRADE, ";
        $query .= "     T3.HR_CLASS, ";
        $query .= "     T3.ATTENDNO, ";
        $query .= "     T2.NAME_SHOW, ";
        //テスト
        foreach ($model->testcdArray as $key => $codeArray) {
            //成績（1回目）
            $query .= " R{$key}.SCORE AS SCORE{$key}, ";
            $query .= " R{$key}.VALUE_DI AS VALUE_DI{$key}, ";
            //受験許可フラグ
            $query .= " F{$key}.SEM_PASS_FLG AS SEM_PASS_FLG{$key}, ";
            //考査満点マスタ(合格点)
            $query .= " P{$key}.PASS_SCORE AS PASS_SCORE{$key}, ";
        }
        $query .= "     T4.HR_NAME ";
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
        foreach ($model->testcdArray as $key => $codeArray) {
            //受験許可フラグ
            $query .= "     LEFT JOIN T_PASS_FLG F{$key} ON F{$key}.SCHREGNO = T1.SCHREGNO ";
            $query .= "          AND F{$key}.TESTCD = '".$codeArray["TESTCD"]."' ";
            //成績（1回目）
            $query .= "     LEFT JOIN SCORE_HIST1 R{$key} ON R{$key}.SCHREGNO = T1.SCHREGNO ";
            $query .= "          AND R{$key}.TESTCD = '".$codeArray["TESTCD"]."' ";
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
        //クラス番号順の場合はクラスまで指定する
        if ($model->order == 2) {
            $query .= " ORDER BY ";
            $query .= "     T3.GRADE, ";
            $query .= "     T3.HR_CLASS, ";
            $query .= "     T3.ATTENDNO ";
        } else {
            $query .= " ORDER BY ";
            $query .= "     SUBSTR(T1.SCHREGNO, 1, 4) DESC, ";
            $query .= "     SUBSTR(T1.SCHREGNO, 5, 4) ";
        }
        return $query;
    }

    /* データ更新処理 */
    function getUpdateQuery($model) {
        $db = Query::dbCheckOut();

        $chrsub_array = array();
        $chrsub_array = explode("-", $model->sub);

        for ($i = 1; $i <=get_count($model->setdata["SCHREGNO"]); $i++) {
            $year = CTRL_YEAR;
            $schno = $model->setdata["SCHREGNO"][$i];
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
                $data["SCORE"][NUMBER]          = $score == "*" ? "" : $score;
                $data["VALUE_DI"][TEXT]         = $score == "*" ? $score : "";
                $data["VALUE"][NUMBER]          = (strlen($passScore) && strlen($score) && $passScore <= $score) ? $score : ""; //合格点以上の時、SCOREをセット
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
                if ($score == $db->getOne($query)) {
                    continue;
                }
                //削除
                $query = "DELETE FROM RECORD_SCORE_HIST_DAT ".$where;
                $db->query($query);
                //追加
                if (strlen($score)) {
                    $query = Query::insertSQL($data, "RECORD_SCORE_HIST_DAT");
                    $db->query($query);
                }
            }
        }

        Query::dbCheckIn($db);
    }

}
?>
