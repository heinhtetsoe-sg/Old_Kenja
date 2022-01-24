<?php

require_once('for_php7.php');

class knjh113cQuery extends Query {
    //名称マスタより取得
    function getNameMst($namecd1) {
        $query  = "";
        $query .= " SELECT ";
        $query .= "     NAMECD2 AS VALUE,";
        $query .= "     NAMECD2 || ':' || NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = '".$namecd1."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //学期名取得
    function getSemesterName() {
        $query  = " SELECT ";
        $query .= "     SEMESTERNAME ";
        $query .= " FROM ";
        $query .= "     SEMESTER_MST ";
        $query .= " WHERE ";
        $query .= "         YEAR     = '".CTRL_YEAR."' ";
        $query .= "     AND SEMESTER = '".CTRL_SEMESTER."' ";

        return $query;
    }

    //資格コンボ
    function getQualifiedMst() {
        $query  = " SELECT DISTINCT ";
        $query .= "     QUALIFIED_CD AS VALUE, ";
        $query .= "     QUALIFIED_CD || ':' || QUALIFIED_NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     QUALIFIED_MST ";
        $query .= " WHERE ";
        $query .= "     MANAGEMENT_FLG = '1' ";
        $query .= " ORDER BY ";
        $query .= "     QUALIFIED_CD ";

        return $query;
    }

    //年組取得（権限チェック）
    function getAuth($model) {
        //参照・更新可
        if (AUTHORITY == DEF_REFERABLE || AUTHORITY == DEF_UPDATABLE){
            $query  = " SELECT ";
            $query .= "     GRADE || HR_CLASS AS VALUE, ";
            $query .= "     HR_NAME AS LABEL ";
            $query .= " FROM ";
            $query .= "     SCHREG_REGD_HDAT ";
            $query .= " WHERE ";
            $query .= "         YEAR     = '".CTRL_YEAR."' ";
            $query .= "     AND SEMESTER = '".CTRL_SEMESTER."' ";
            if ($model->Properties["use_prg_schoolkind"] == "1") {
                if ($model->selectSchoolKind) {
                    $query .= " AND GRADE IN (  SELECT ";
                    $query .= "                     GRADE ";
                    $query .= "                 FROM ";
                    $query .= "                     SCHREG_REGD_GDAT ";
                    $query .= "                 WHERE ";
                    $query .= "                     YEAR        = '".CTRL_YEAR."' AND ";
                    $query .= "                     SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
                    $query .= "             ) ";
                }
            } else if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
                $query .= " AND GRADE IN (  SELECT ";
                $query .= "                     GRADE ";
                $query .= "                 FROM ";
                $query .= "                     SCHREG_REGD_GDAT ";
                $query .= "                 WHERE ";
                $query .= "                     YEAR        = '".CTRL_YEAR."' AND ";
                $query .= "                     SCHOOL_KIND = '".SCHOOLKIND."' ";
                $query .= "             ) ";
            }
        }
        //参照・更新可（制限付き）
        if (AUTHORITY == DEF_REFER_RESTRICT || AUTHORITY == DEF_UPDATE_RESTRICT) {
            $query  = " SELECT ";
            $query .= "     GRADE || HR_CLASS AS VALUE, ";
            $query .= "     HR_NAME AS LABEL ";
            $query .= " FROM ";
            $query .= "     SCHREG_REGD_HDAT ";
            $query .= " WHERE ";
            $query .= "         YEAR     = '".CTRL_YEAR."' ";
            $query .= "     AND SEMESTER = '".CTRL_SEMESTER."' ";
            $query .= "     AND (   TR_CD1    = '" .STAFFCD ."' ";
            $query .= "          OR TR_CD2    = '" .STAFFCD ."' ";
            $query .= "          OR TR_CD3    = '" .STAFFCD ."' ";
            $query .= "          OR SUBTR_CD1 = '" .STAFFCD ."' ";
            $query .= "          OR SUBTR_CD2 = '" .STAFFCD ."' ";
            $query .= "          OR SUBTR_CD3 = '" .STAFFCD ."') ";
            if ($model->Properties["use_prg_schoolkind"] == "1") {
                if ($model->selectSchoolKind) {
                    $query .= " AND GRADE IN (  SELECT ";
                    $query .= "                     GRADE ";
                    $query .= "                 FROM ";
                    $query .= "                     SCHREG_REGD_GDAT ";
                    $query .= "                 WHERE ";
                    $query .= "                     YEAR        = '".CTRL_YEAR."' AND ";
                    $query .= "                     SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
                    $query .= "             ) ";
                }
            } else if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
                $query .= " AND GRADE IN (  SELECT ";
                $query .= "                     GRADE ";
                $query .= "                 FROM ";
                $query .= "                     SCHREG_REGD_GDAT ";
                $query .= "                 WHERE ";
                $query .= "                     YEAR        = '".CTRL_YEAR."' AND ";
                $query .= "                     SCHOOL_KIND = '".SCHOOLKIND."' ";
                $query .= "             ) ";
            }
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //学期開始、終了日取得
    function getSEdate($semesName) {
        $query  = " SELECT DISTINCT ";
        if ($semesName == "SDATE") {
            $query .= "     SDATE ";
        } else {
            $query .= "     EDATE ";
        }
        $query .= " FROM ";
        $query .= "     SEMESTER_MST ";
        $query .= " WHERE ";
        $query .= "         YEAR     = '".CTRL_YEAR ."' ";
        $query .= "     AND SEMESTER = '9' ";

        return $query;
    }

    //試験日コンボ
    function getTestDate($model) {
        $query  = " SELECT DISTINCT ";
        $query .= "     TEST_DATE AS VALUE, ";
        $query .= "     TEST_DATE AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_QUALIFIED_TEST_DAT ";
        $query .= " WHERE ";
        $query .= "         YEAR         = '".CTRL_YEAR ."' ";
        $query .= "     AND QUALIFIED_CD = '".$model->sikakuCd."' ";
        $query .= "     AND TEST_DATE BETWEEN '".$model->sDate."' AND '".$model->eDate."' ";
        $query .= "     AND SCHREGNO     IN ( ";
        $query .= "                          SELECT ";
        $query .= "                              SCHREGNO ";
        $query .= "                          FROM ";
        $query .= "                              SCHREG_REGD_DAT ";
        $query .= "                          WHERE ";
        $query .= "                              YEAR              = '".CTRL_YEAR."' ";
        $query .= "                          AND SEMESTER          = '".CTRL_SEMESTER."' ";
        $query .= "                          AND GRADE || HR_CLASS = '".$model->gradeHrClass."' ";
        $query .= "                         ) ";
        $query .= " GROUP BY ";
        $query .= "     TEST_DATE ";
        $query .= " ORDER BY ";
        $query .= "     TEST_DATE ";

        return $query;
    }

    //リスト
    function getList($model) {
        $query  = " SELECT ";
        $query .= "     REGD.SCHREGNO, ";
        $query .= "     TEST.TEST_CD, ";
        $query .= "     HDAT.HR_NAME || '-' || REGD.ATTENDNO || '番' AS G_HR_NO, ";
        $query .= "     BASE.NAME, ";
        $query .= "     TS_M.TEST_NAME_ABBV, ";
        $query .= "     Z050.NAMECD2 AS PASS_CHECK, ";
        $query .= "     Z050.NAMECD2 || ':' || Z050.NAME1 AS RESULT, ";
        $query .= "     TEST.LIMITED_DATE, ";
        $query .= "     VALUE(GET_D.RESULT_CD || ':' || RS_M.RESULT_NAME_ABBV, 'なし') AS SYUTOKU_KYU ";
        $query .= " FROM ";
        $query .= "     SCHREG_QUALIFIED_TEST_DAT TEST ";
        $query .= "     LEFT JOIN SCHREG_BASE_MST BASE ON TEST.SCHREGNO = BASE.SCHREGNO ";
        $query .= "     LEFT JOIN SCHREG_REGD_DAT REGD ON TEST.SCHREGNO = REGD.SCHREGNO ";
        $query .= "                                   AND TEST.YEAR     = REGD.YEAR ";
        $query .= "                                   AND REGD.SEMESTER = '".CTRL_SEMESTER."' ";
        $query .= "     LEFT JOIN SCHREG_REGD_HDAT HDAT ON TEST.YEAR     = HDAT.YEAR ";
        $query .= "                                    AND REGD.SEMESTER = HDAT.SEMESTER ";
        $query .= "                                    AND REGD.GRADE    = HDAT.GRADE ";
        $query .= "                                    AND REGD.HR_CLASS = HDAT.HR_CLASS ";
        $query .= "     LEFT JOIN (SELECT ";//持っている最上の資格級を取得
        $query .= "                    T1.SCHREGNO, ";
        $query .= "                    MIN(T1.RESULT_CD) AS RESULT_CD ";
        $query .= "                FROM ";
        $query .= "                    SCHREG_QUALIFIED_TEST_DAT T1 ";
        $query .= "                    LEFT JOIN QUALIFIED_RESULT_MST T2 ON T1.YEAR         = T2.YEAR ";
        $query .= "                                                     AND T1.QUALIFIED_CD = T2.QUALIFIED_CD ";
        $query .= "                                                     AND T1.RESULT_CD    = T2.RESULT_CD ";
        $query .= "                WHERE ";
        $query .= "                        T1.RESULT_CD NOT IN ('8888', '9999') ";//欠席と、不合格は対象外
        $query .= "                    AND T1.RESULT_CD IS NOT NULL ";
        $query .= "                    AND T1.QUALIFIED_CD = '".$model->sikakuCd."' ";
        $query .= "                    AND T2.CERT_FLG     = 'T' ";//資格フラグが立っているもの
        $query .= "                GROUP BY ";
        $query .= "                    T1.SCHREGNO ";
        $query .= "               ) GET_D ON REGD.SCHREGNO = GET_D.SCHREGNO ";
        $query .= "     LEFT JOIN QUALIFIED_TEST_MST TS_M ON TEST.YEAR         = TS_M.YEAR ";
        $query .= "                                      AND TEST.QUALIFIED_CD = TS_M.QUALIFIED_CD ";
        $query .= "                                      AND TEST.TEST_CD      = TS_M.TEST_CD ";
        $query .= "     LEFT JOIN QUALIFIED_RESULT_MST RS_M ON TEST.YEAR         = RS_M.YEAR ";
        $query .= "                                        AND TEST.QUALIFIED_CD = RS_M.QUALIFIED_CD ";
        $query .= "                                        AND RS_M.RESULT_CD    = GET_D.RESULT_CD ";
        $query .= "     LEFT JOIN NAME_MST Z050 ON Z050.NAMECD1  = 'Z050' ";
        $query .= "                            AND SUBSTR(TEST.RESULT_CD, 4) = Z050.NAMECD2 ";
        $query .= " WHERE ";
        $query .= "         TEST.YEAR         = '".CTRL_YEAR."' ";
        if ($model->testDate != "") {
            $query .= "     AND TEST.TEST_DATE    = '".str_replace('/', '-', $model->testDate)."' ";
        }
        $query .= "     AND TEST.QUALIFIED_CD = '".$model->sikakuCd."' ";
        $query .= "     AND HDAT.GRADE || HDAT.HR_CLASS = '".$model->gradeHrClass."' ";
        $query .= " ORDER BY ";
        $query .= "     REGD.ATTENDNO ";

        return $query;
    }


/******** 更新処理  ********/
    //資格期限取得
    function getLimitDate($model, $resultCd) {
        $query  = " SELECT ";
        $query .= "     DATE('".str_replace('/', '-', $model->testDate)."') + LIMITED_PERIOD MONTHS ";
        $query .= " FROM ";
        $query .= "     QUALIFIED_RESULT_MST ";
        $query .= " WHERE ";
        $query .= "         YEAR         = '".CTRL_YEAR."' ";
        $query .= "     AND QUALIFIED_CD = '".$model->sikakuCd."' ";
        $query .= "     AND RESULT_CD    = '".$resultCd."' ";

        return $query;
    }

    //更新
    function getUpdateQuery(&$model) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $keyList = array();
        $keyList = explode(",", $model->hid_schregNo);
        foreach ($keyList as $key => $val) {
            $tmp = explode("-", $val);
            $schregNo  = $tmp[0];
            $testCd    = $tmp[1];
            if ($model->judgeDiv == "9") {
                $resultCd  = "9999";
            } else if ($model->judgeDiv == "8") {
                $resultCd  = "8888";
            } else {
                $resultCd  = substr($testCd, 0, 3).$model->judgeDiv;
            }
            $limitDate = $db->getOne(knjh113cQuery::getLimitDate($model, $resultCd));

            //チェックありの人を処理
            if ($model->field["CHK_DATA"][$schregNo.$testCd] != "on") continue;

            //受付データ
            $data = array();
            $data["RESULT_CD"][TEXT]        = $resultCd;
            $data["LIMITED_DATE"][DATE]     = $limitDate;
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][FUNC]          = "sysdate()";
            //条件
            $where  = " WHERE YEAR         = '".CTRL_YEAR."' ";
            $where .= "   AND SCHREGNO     = '".$schregNo."' ";
            $where .= "   AND TEST_DATE    = '".str_replace('/', '-', $model->testDate)."' ";
            $where .= "   AND QUALIFIED_CD = '".$model->sikakuCd."' ";
            $where .= "   AND TEST_CD      = '".$testCd."' ";
            $query = Query::updateSQL($data, "SCHREG_QUALIFIED_TEST_DAT", $where);
            $db->query($query);
        }

        $db->commit();
        Query::dbCheckIn($db);

        return true;
    }
}
?>