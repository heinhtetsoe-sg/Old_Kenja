<?php

require_once('for_php7.php');

class knjd128rQuery extends Query {
    //学期情報取得
    function getSemester($seme="") {
        $query  = " SELECT ";
        $query .= "     SEMESTER AS VALUE, ";
        $query .= "     SEMESTERNAME AS LABEL, ";
        $query .= "     SDATE, ";
        $query .= "     EDATE ";
        $query .= " FROM ";
        $query .= "     SEMESTER_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        if ($seme) {
            $query .= "     AND SEMESTER = '".$seme."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //クラス取得
    function getHrClass($model) {
        $query  = " SELECT ";
        $query .= "     GRADE || '-' || HR_CLASS AS VALUE, ";
        $query .= "     HR_NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_HDAT T1 ";
        $query .= " WHERE ";
        $query .= "     YEAR        = '".CTRL_YEAR."' AND ";
        $query .= "     SEMESTER    = '".$model->field["SCH_SEMESTER"]."' ";
        //参照・更新可（制限付き）
        if (AUTHORITY == DEF_REFER_RESTRICT || AUTHORITY == DEF_UPDATE_RESTRICT) {
            $query .= " AND '".STAFFCD."' IN (TR_CD1, TR_CD2, TR_CD3, SUBTR_CD1, SUBTR_CD2, SUBTR_CD3) ";
        }
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= " AND GRADE IN (  SELECT ";
                $query .= "                     GRADE ";
                $query .= "                 FROM ";
                $query .= "                     SCHREG_REGD_GDAT S1 ";
                $query .= "                 WHERE ";
                $query .= "                     S1.YEAR         = T1.YEAR AND ";
                $query .= "                     S1.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
                $query .= "                 ) ";
            }
        } else if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND GRADE IN (  SELECT ";
            $query .= "                     GRADE ";
            $query .= "                 FROM ";
            $query .= "                     SCHREG_REGD_GDAT S1 ";
            $query .= "                 WHERE ";
            $query .= "                     S1.YEAR         = T1.YEAR AND ";
            $query .= "                     S1.SCHOOL_KIND  = '".SCHOOLKIND."' ";
            $query .= "                 ) ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //テスト取得
    function getTest($model) {
        $gradeHrClass = array();
        $gradeHrClass = explode("-", $model->field["GRADE_HR_CLASS"]);

        $query  = " SELECT ";
        $query .= "     T1.YEAR, ";
        $query .= "     T1.SEMESTER, ";
        $query .= "     T1.TESTKINDCD || '-' || T1.TESTITEMCD || '-' || T1.SCORE_DIV AS VALUE, ";
        $query .= "     T1.TESTKINDCD || T1.TESTITEMCD || T1.SCORE_DIV || ' ' || L1.TESTITEMNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     ADMIN_CONTROL_SDIV_DAT T1 ";
        $query .= "     LEFT JOIN TESTITEM_MST_COUNTFLG_NEW_SDIV L1 ON T1.YEAR = L1.YEAR ";
        $query .= "                                                AND T1.SEMESTER = L1.SEMESTER ";
        $query .= "                                                AND T1.TESTKINDCD = L1.TESTKINDCD ";
        $query .= "                                                AND T1.TESTITEMCD = L1.TESTITEMCD ";
        $query .= "                                                AND T1.SCORE_DIV = L1.SCORE_DIV ";
        $query .= "     INNER JOIN SCHREG_REGD_GDAT L2 ON L2.YEAR   = T1.YEAR ";
        $query .= "                                   AND L2.GRADE  = '".$gradeHrClass[0]."' ";
        $query .= "                                   AND L2.SCHOOL_KIND = T1.SCHOOL_KIND ";
        $query .= " WHERE ";
        $query .= "         T1.YEAR         = '".CTRL_YEAR."' ";
        $query .= "     AND T1.SEMESTER     = '".$model->field["SEMESTER"]."' ";
        $query .= "     AND T1.CLASSCD      = '00' ";
        $query .= "     AND T1.CURRICULUM_CD= '00' ";
        $query .= "     AND T1.SUBCLASSCD   = '000000' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //異動データ（退学・転学・卒業）取得
    function getIdouData($model, $date) {
        $query  = " SELECT ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1, ";
        $query .= "     SCHREG_BASE_MST T2 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR     = '".CTRL_YEAR."' AND ";
        $query .= "     T1.SEMESTER = '".$model->field["SCH_SEMESTER"]."' AND ";
        $query .= "     T1.GRADE || '-' || T1.HR_CLASS = '".$model->field["GRADE_HR_CLASS"]."' AND ";
        $query .= "     T1.SCHREGNO = T2.SCHREGNO AND ";
        $query .= "     T2.GRD_DIV IN ('1', '2', '3', '6') AND ";
        $query .= "     T2.GRD_DATE < '".$date."' ";
        $query .= " GROUP BY ";
        $query .= "     T1.SCHREGNO ";

        return $query;
    }

    //異動データ（留学・休学）取得
    function getTransferData($model, $date) {
        $query  = " SELECT ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1, ";
        $query .= "     SCHREG_TRANSFER_DAT T2 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR     = '".CTRL_YEAR."' AND ";
        $query .= "     T1.SEMESTER = '".$model->field["SCH_SEMESTER"]."' AND ";
        $query .= "     T1.GRADE || '-' || T1.HR_CLASS = '".$model->field["GRADE_HR_CLASS"]."' AND ";
        $query .= "     T1.SCHREGNO = T2.SCHREGNO AND ";
        $query .= "     T2.TRANSFERCD IN ('1', '2') AND ";
        $query .= "     '".$date."' BETWEEN T2.TRANSFER_SDATE AND ";
        $query .= "     CASE WHEN T2.TRANSFER_EDATE IS NULL THEN '".(CTRL_YEAR+1)."' || '-03-31' ELSE T2.TRANSFER_EDATE END ";
        $query .= " GROUP BY ";
        $query .= "     T1.SCHREGNO ";

        return $query;
    }

    //科目一覧取得
    function getSubclassList($model) {
        $query  = " SELECT DISTINCT ";
        $query .= "     T3.CLASSCD || '-' || T3.SCHOOL_KIND || '-' || T3.CURRICULUM_CD || '-' || T3.SUBCLASSCD AS SUBCLASS, ";
        $query .= "     L1.SUBCLASSABBV ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1, ";
        $query .= "     CHAIR_STD_DAT T2, ";
        $query .= "     CHAIR_DAT T3 ";
        $query .= "     LEFT JOIN SUBCLASS_MST L1 ";
        $query .= "              ON T3.CLASSCD       = L1.CLASSCD ";
        $query .= "             AND T3.SCHOOL_KIND   = L1.SCHOOL_KIND ";
        $query .= "             AND T3.CURRICULUM_CD = L1.CURRICULUM_CD ";
        $query .= "             AND T3.SUBCLASSCD    = L1.SUBCLASSCD ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR     = T2.YEAR AND ";
        $query .= "     T1.YEAR     = T3.YEAR AND ";
        $query .= "     T1.YEAR     = '".CTRL_YEAR."' AND ";
        $query .= "     T1.SEMESTER = T2.SEMESTER AND ";
        $query .= "     T1.SEMESTER = T3.SEMESTER AND ";
        $query .= "     T1.SEMESTER = '".$model->field["SCH_SEMESTER"]."' AND ";
        $query .= "     T1.GRADE || '-' || T1.HR_CLASS = '".$model->field["GRADE_HR_CLASS"]."' AND ";
        $query .= "     T1.SCHREGNO = T2.SCHREGNO AND ";
        $query .= "     T2.CHAIRCD  = T3.CHAIRCD AND ";
        $query .= "     T3.SUBCLASSCD NOT LIKE '9%' ";
        $query .= " ORDER BY ";
        $query .= "     SUBCLASS ";

        return $query;
    }

    //生徒一覧取得
    function getSchList($model) {
        $query  = " SELECT ";
        $query .= "     T1.YEAR, ";
        $query .= "     T1.SEMESTER, ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     T1.ATTENDNO, ";
        $query .= "     L1.NAME_SHOW ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1 ";
        $query .= "     LEFT JOIN SCHREG_BASE_MST L1 ON T1.SCHREGNO = L1.SCHREGNO ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR     = '".CTRL_YEAR."' AND ";
        $query .= "     T1.SEMESTER = '".$model->field["SCH_SEMESTER"]."' AND ";
        $query .= "     T1.GRADE || '-' || T1.HR_CLASS = '".$model->field["GRADE_HR_CLASS"]."' ";
        $query .= " ORDER BY ";
        $query .= "     T1.ATTENDNO ";

        return $query;
    }

    //成績データ取得
    function getMainQuery($model, $sdate, $edate, $subclass) {
        $query  = " WITH REDG_DAT AS ( ";
        $query .= "     SELECT ";
        $query .= "         * ";
        $query .= "     FROM ";
        $query .= "         SCHREG_REGD_DAT ";
        $query .= "     WHERE ";
        $query .= "         YEAR        = '".CTRL_YEAR."' AND ";
        $query .= "         SEMESTER    = '".$model->field["SCH_SEMESTER"]."' AND ";
        $query .= "         GRADE || '-' || HR_CLASS = '".$model->field["GRADE_HR_CLASS"]."' ";
        $query .= " ), CHR_SUBCLASS AS ( ";
        $query .= "     SELECT DISTINCT ";
        $query .= "         T1.SCHREGNO, ";
        $query .= "         T3.CLASSCD || '-' || T3.SCHOOL_KIND || '-' || T3.CURRICULUM_CD || '-' || T3.SUBCLASSCD AS SUBCLASS, ";
        $query .= "         '1' AS FLG ";
        $query .= "     FROM ";
        $query .= "         REDG_DAT T1, ";
        $query .= "         CHAIR_STD_DAT T2, ";
        $query .= "         CHAIR_DAT T3 ";
        $query .= "     WHERE ";
        $query .= "         T1.YEAR     = T2.YEAR AND ";
        $query .= "         T1.YEAR     = T3.YEAR AND ";
        $query .= "         T1.SEMESTER = T2.SEMESTER AND ";
        $query .= "         T1.SEMESTER = T3.SEMESTER AND ";
        $query .= "         T1.SCHREGNO = T2.SCHREGNO AND ";
        $query .= "         T2.CHAIRCD  = T3.CHAIRCD AND ";
        $query .= "         T3.SUBCLASSCD NOT LIKE '9%' ";
        $query .= " ), RECORD_SCORE AS ( ";
        $query .= "     SELECT ";
        $query .= "         T1.SCHREGNO, ";
        $query .= "         T2.CLASSCD || '-' || T2.SCHOOL_KIND || '-' || T2.CURRICULUM_CD || '-' || T2.SUBCLASSCD AS SUBCLASS, ";
        $query .= "         T2.SCORE, ";
        $query .= "         T2.VALUE_DI "; //欠試'*'
        $query .= "     FROM ";
        $query .= "         REDG_DAT T1, ";
        $query .= "         RECORD_SCORE_DAT T2 ";
        $query .= "     WHERE ";
        $query .= "         T1.YEAR     = T2.YEAR AND ";
        $query .= "         T1.SCHREGNO = T2.SCHREGNO AND ";
        $query .= "         T2.SEMESTER = '".$model->field["SEMESTER"]."' AND ";
        $query .= "         T2.TESTKINDCD || '-' || T2.TESTITEMCD || '-' || T2.SCORE_DIV = '".$model->field["TESTKINDCD"]."' ";
        $query .= " ), MAIN AS ( ";
        $query .= "     SELECT ";
        $query .= "         T1.SCHREGNO, ";
        $query .= "         T2.CLASSCD || '-' || T2.SCHOOL_KIND || '-' || T2.CURRICULUM_CD || '-' || T2.SUBCLASSCD AS SUBCLASS ";
        $query .= "     FROM ";
        $query .= "         REDG_DAT T1, ";
        $query .= "         V_SUBCLASS_MST T2 ";
        $query .= "     WHERE ";
        $query .= "         T1.YEAR = T2.YEAR AND ";
        $query .= "         T2.CLASSCD || '-' || T2.SCHOOL_KIND || '-' || T2.CURRICULUM_CD || '-' || T2.SUBCLASSCD IN ('".implode("','", explode(',', $subclass))."') ";
        $query .= " ) ";

        $query .= " SELECT ";
        $query .= "     T1.*, ";
        $query .= "     L1.FLG, ";
        $query .= "     L2.SCORE, ";
        $query .= "     L2.VALUE_DI "; //欠試'*'
        $query .= " FROM ";
        $query .= "     MAIN T1 ";
        $query .= "     LEFT JOIN CHR_SUBCLASS L1 ON T1.SCHREGNO = L1.SCHREGNO AND T1.SUBCLASS = L1.SUBCLASS ";
        $query .= "     LEFT JOIN RECORD_SCORE L2 ON T1.SCHREGNO = L2.SCHREGNO AND T1.SUBCLASS = L2.SUBCLASS ";

        return $query;
    }

    //成績データの更新
    function getUpdateQuery($model) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $testcdArray = array();
        $testcdArray = explode("-", $model->field["TESTKINDCD"]);

        foreach ($model->field["SCHREGNO"] as $key => $schregno) {
            //更新対象科目がなければスキップ
            if (!$model->field["RECORD_FIELD"][$key]) continue;

            foreach (explode(',', $model->field["SUBCLASS"]) as $subclass) {
                //更新対象科目でなければスキップ
                if (!in_array($subclass, explode(',', $model->field["RECORD_FIELD"][$key]))) continue;

                //更新対象データが存在するかをチェック
                $query  = " SELECT ";
                $query .= "     COUNT(*) ";
                $query .= " FROM ";
                $query .= "     RECORD_SCORE_DAT ";
                $query .= " WHERE ";
                $query .= "     YEAR        = '".CTRL_YEAR."' AND ";
                $query .= "     SEMESTER    = '".$model->field["SEMESTER"]."' AND ";
                $query .= "     TESTKINDCD || '-' || TESTITEMCD || '-' || SCORE_DIV = '".$model->field["TESTKINDCD"]."' AND ";
                $query .= "     CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD = '".$subclass."' AND ";
                $query .= "     SCHREGNO    = '".$schregno."' ";
                $flag = $db->getOne($query);

                $data = array();
                if ($flag) {
                    $where  = " WHERE ";
                    $where .= "     YEAR        = '".CTRL_YEAR."' AND ";
                    $where .= "     SEMESTER    = '".$model->field["SEMESTER"]."' AND ";
                    $where .= "     TESTKINDCD || '-' || TESTITEMCD || '-' || SCORE_DIV = '".$model->field["TESTKINDCD"]."' AND ";
                    $where .= "     CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD = '".$subclass."' AND ";
                    $where .= "     SCHREGNO    = '".$schregno."' ";
                } else {
                    //更新対象データがない時 - INSERT
                    $data["YEAR"][TEXT]         = CTRL_YEAR;
                    $data["SEMESTER"][TEXT]     = $model->field["SEMESTER"];
                    $data["TESTKINDCD"][TEXT]   = $testcdArray[0];
                    $data["TESTITEMCD"][TEXT]   = $testcdArray[1];
                    $data["SCORE_DIV"][TEXT]    = $testcdArray[2];
                    list($classcd, $school_kind, $curriculum_cd, $subclasscd) = preg_split("/-/", $subclass);
                    $data["CLASSCD"][TEXT]      = $classcd;
                    $data["SCHOOL_KIND"][TEXT]  = $school_kind;
                    $data["CURRICULUM_CD"][TEXT]= $curriculum_cd;
                    $data["SUBCLASSCD"][TEXT]   = $subclasscd;
                    $data["SCHREGNO"][TEXT]     = $schregno;
                }

                //更新データをセット
                $score = $model->field["SCORE"][$subclass][$key];
                $data["SCORE"][NUMBER]      = $score == "*" ? "" : $score;
                $data["VALUE_DI"][TEXT]     = $score == "*" ? $score : "";
                $data["REGISTERCD"][TEXT]   = STAFFCD;
                $data["UPDATED"][FUNC]      = "sysdate()";

                if ($flag) {
                    $query = Query::updateSQL($data, "RECORD_SCORE_DAT", $where);
                } else {
                    $query = Query::insertSQL($data, "RECORD_SCORE_DAT");
                }
                $db->query($query);
            }
        }

        $db->commit();
        Query::dbCheckIn($db);
        return;
    }
}
?>
