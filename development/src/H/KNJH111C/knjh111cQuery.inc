<?php

require_once('for_php7.php');

class knjh111cQuery extends Query {
/************ 左画面 ***************/
    //学年コンボ
    function getGrade($model) {
        $query  = " SELECT DISTINCT ";
        $query .= "     GRADE AS VALUE, ";
        $query .= "     GRADE_NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_GDAT ";
        $query .= " WHERE ";
        $query .= "         YEAR        = '".CTRL_YEAR."' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= "     AND SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
            }
        } else if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= "     AND SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     GRADE ";

        return $query;
    }

   //組コンボ
   function GetHr_Class($model) {
        $query  = " SELECT ";
        $query .= "     T1.HR_CLASS AS VALUE, ";
        $query .= "     T1.HR_NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_HDAT T1 ";
        $query .= " WHERE ";
        $query .= "         T1.YEAR     = '".CTRL_YEAR ."' ";
        $query .= "     AND T1.SEMESTER = '".CTRL_SEMESTER."' ";
        $query .= "     AND T1.GRADE    = '".$model->grade."' ";
        //参照・更新可（制限付き）
        if (AUTHORITY == DEF_REFER_RESTRICT || AUTHORITY == DEF_UPDATE_RESTRICT) {
            $query .= "     AND (T1.TR_CD1 = '" .STAFFCD ."' ";
            $query .= "       OR T1.TR_CD2 = '" .STAFFCD ."' ";
            $query .= "       OR T1.TR_CD3 = '" .STAFFCD ."' ";
            $query .= "       OR T1.SUBTR_CD1 = '" .STAFFCD ."' ";
            $query .= "       OR T1.SUBTR_CD2 = '" .STAFFCD ."' ";
            $query .= "       OR T1.SUBTR_CD3 = '" .STAFFCD ."') ";
        }
        $query .= " ORDER BY ";
        $query .= "     T1.GRADE, ";
        $query .= "     T1.HR_CLASS ";

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
        $query .= "                              YEAR     = '".CTRL_YEAR."' ";
        $query .= "                          AND SEMESTER = '".CTRL_SEMESTER."' ";
        $query .= "                          AND GRADE    = '".$model->grade."' ";
        $query .= "                          AND HR_CLASS = '".$model->hrClass."' ";
        $query .= "                         ) ";
        $query .= " GROUP BY ";
        $query .= "     TEST_DATE ";
        $query .= " ORDER BY ";
        $query .= "     TEST_DATE ";

        return $query;
    }

    //受験級コンボ
    function getTestCdLeft($model) {
        $query  = " SELECT DISTINCT ";
        $query .= "     TEST_M.TEST_CD AS VALUE, ";
        $query .= "     TEST_M.TEST_CD || ':' || TEST_M.TEST_NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_QUALIFIED_TEST_DAT TEST_D ";
        $query .= "     LEFT JOIN QUALIFIED_TEST_MST TEST_M ON TEST_D.YEAR         = TEST_M.YEAR ";
        $query .= "                                        AND TEST_D.QUALIFIED_CD = TEST_M.QUALIFIED_CD ";
        $query .= "                                        AND TEST_D.TEST_CD      = TEST_M.TEST_CD ";
        $query .= " WHERE ";
        $query .= "         TEST_D.YEAR         = '".CTRL_YEAR ."' ";
        $query .= "     AND TEST_D.QUALIFIED_CD = '".$model->sikakuCd."' ";
        if ($model->topTestDate != "") {
            $query .= "     AND TEST_D.TEST_DATE    = '".$model->topTestDate."' ";
        }
        $query .= "     AND TEST_D.SCHREGNO IN ( ";
        $query .= "                             SELECT ";
        $query .= "                                 SCHREGNO ";
        $query .= "                             FROM ";
        $query .= "                                 SCHREG_REGD_DAT ";
        $query .= "                             WHERE ";
        $query .= "                                 YEAR     = '".CTRL_YEAR."' ";
        $query .= "                             AND SEMESTER = '".CTRL_SEMESTER."' ";
        $query .= "                             AND GRADE    = '".$model->grade."' ";
        $query .= "                             AND HR_CLASS = '".$model->hrClass."' ";
        $query .= "                            ) ";
        $query .= " ORDER BY ";
        $query .= "     TEST_M.TEST_CD ";

        return $query;
    }

    //リスト
    function getList($model) {
        $query  = " WITH MAX_TEST_DATE AS ( ";
        $query .= "     SELECT ";
        $query .= "         T1.SCHREGNO, ";
        $query .= "         T2.BEF_TEST_DATE ";
        $query .= "     FROM ";
        $query .= "         SCHREG_QUALIFIED_TEST_DAT T1, ";
        $query .= "         (SELECT ";
        $query .= "              SCHREGNO, ";
        $query .= "              MAX(TEST_DATE) AS BEF_TEST_DATE ";
        $query .= "          FROM ";
        $query .= "              SCHREG_QUALIFIED_TEST_DAT ";
        $query .= "          WHERE ";
        $query .= "              QUALIFIED_CD = '".$model->sikakuCd."' ";
        if ($model->topTestDate != "") {
            $query .= "          AND TEST_DATE < '".$model->topTestDate."' ";
        }
        $query .= "          GROUP BY ";
        $query .= "              SCHREGNO ) T2 ";
        $query .= "     WHERE ";
        $query .= "             T1.SCHREGNO     = T2.SCHREGNO ";
        $query .= "         AND T1.TEST_DATE    = T2.BEF_TEST_DATE ";
        $query .= "         AND T1.QUALIFIED_CD = '".$model->sikakuCd."' ";
        $query .= " ), BEF_TEST_DATA AS ( ";
        $query .= "     SELECT DISTINCT ";
        $query .= "         TEST.SCHREGNO, ";
        $query .= "         MAX_D.BEF_TEST_DATE, ";
        $query .= "         TS_M.TEST_NAME, ";
        $query .= "         CASE ";
        $query .= "             WHEN TEST.RESULT_CD = '9999' THEN '不合格' ";
        $query .= "             WHEN TEST.RESULT_CD = '8888' THEN '欠席' ";
        $query .= "             ELSE RS_M.RESULT_NAME_ABBV ";
        $query .= "         END AS RESULT, ";
        $query .= "         VALUE(RS_M2.RESULT_NAME_ABBV, '') AS GET_TEST_NAME ";
        $query .= "     FROM ";
        $query .= "         SCHREG_QUALIFIED_TEST_DAT TEST ";
        $query .= "         INNER JOIN MAX_TEST_DATE MAX_D ON TEST.SCHREGNO  = MAX_D.SCHREGNO ";
        $query .= "                                       AND TEST.TEST_DATE = MAX_D.BEF_TEST_DATE ";
        $query .= "         LEFT JOIN QUALIFIED_TEST_MST TS_M ON TEST.YEAR         = TS_M.YEAR ";
        $query .= "                                          AND TEST.QUALIFIED_CD = TS_M.QUALIFIED_CD ";
        $query .= "                                          AND TEST.TEST_CD      = TS_M.TEST_CD ";
        $query .= "         LEFT JOIN QUALIFIED_RESULT_MST RS_M ON TEST.YEAR         = RS_M.YEAR ";
        $query .= "                                            AND TEST.QUALIFIED_CD = RS_M.QUALIFIED_CD ";
        $query .= "                                            AND TEST.RESULT_CD    = RS_M.RESULT_CD ";
        $query .= "         LEFT JOIN (SELECT ";
        $query .= "                        SCHREGNO, ";
        $query .= "                        MIN(RESULT_CD) AS RESULT_CD ";
        $query .= "                    FROM ";
        $query .= "                        SCHREG_QUALIFIED_TEST_DAT ";
        $query .= "                    WHERE ";
        $query .= "                            QUALIFIED_CD = '".$model->sikakuCd."' ";
        $query .= "                        AND RESULT_CD IS NOT NULL ";
        $query .= "                    GROUP BY ";
        $query .= "                        SCHREGNO ";
        $query .= "                   ) GET_D ON TEST.SCHREGNO = GET_D.SCHREGNO ";
        $query .= "         LEFT JOIN QUALIFIED_RESULT_MST RS_M2 ON TEST.YEAR         = RS_M2.YEAR ";
        $query .= "                                             AND TEST.QUALIFIED_CD = RS_M2.QUALIFIED_CD ";
        $query .= "                                             AND GET_D.RESULT_CD   = RS_M2.RESULT_CD ";
        $query .= "     WHERE ";
        $query .= "         TEST.QUALIFIED_CD = '".$model->sikakuCd."' ";
        $query .= " ) ";
        //メイン
        $query .= " SELECT ";
        $query .= "     HDAT.HR_NAME || '-' || REGD.ATTENDNO || '番　' || BASE.NAME AS NAME, ";
        $query .= "     TEST.TEST_DATE, ";
        $query .= "     TEST.TEST_CD, ";
        $query .= "     TS_M.TEST_NAME_ABBV, ";
        $query .= "     BEF_D.BEF_TEST_DATE, ";
        $query .= "     BEF_D.TEST_NAME AS BEF_TEST_NAME, ";
        $query .= "     BEF_D.RESULT, ";
        $query .= "     BEF_D.GET_TEST_NAME ";
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
        $query .= "     LEFT JOIN QUALIFIED_TEST_MST TS_M ON TEST.YEAR         = TS_M.YEAR ";
        $query .= "                                      AND TEST.QUALIFIED_CD = TS_M.QUALIFIED_CD ";
        $query .= "                                      AND TEST.TEST_CD      = TS_M.TEST_CD ";
        $query .= "     LEFT JOIN BEF_TEST_DATA BEF_D ON TEST.SCHREGNO  = BEF_D.SCHREGNO ";
        $query .= " WHERE ";
        $query .= "         TEST.YEAR         = '".CTRL_YEAR."' ";
        if ($model->topTestDate != "") {
            $query .= "     AND TEST.TEST_DATE    = '".$model->topTestDate."' ";
        }
        $query .= "     AND TEST.QUALIFIED_CD = '".$model->sikakuCd."' ";
        $query .= "     AND TEST.TEST_CD      = '".$model->topTestCd."' ";
        $query .= "     AND HDAT.GRADE        = '".$model->grade."' ";
        $query .= "     AND HDAT.HR_CLASS     = '".$model->hrClass."' ";
        $query .= " ORDER BY ";
        $query .= "     REGD.ATTENDNO ";

        return $query;
    }

/************ 右画面 ***************/
    //HR_NAME取得
    function getHrName($model) {
        $query  = " SELECT ";
        $query .= "     HR_NAME ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_HDAT ";
        $query .= " WHERE ";
        $query .= "         YEAR     = '".CTRL_YEAR."' ";
        $query .= "     AND SEMESTER = '".CTRL_SEMESTER."' ";
        $query .= "     AND GRADE    = '".$model->grade."' ";
        $query .= "     AND HR_CLASS = '".$model->hrClass."' ";

        return $query;
    }

    //生徒一覧取得(左リスト)
    function getSchregLeftList($model) {
        $query  = " SELECT ";
        $query .= "     REGD.SCHREGNO || ':' || REGD.ATTENDNO || '-' || VALUE(GET_D.RESULT_CD, '9999') AS VALUE, ";
        $query .= "     REGD.ATTENDNO || '番　' || VALUE(RS_M2.RESULT_NAME_ABBV, '　　　　') || '　' || BASE.NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT REGD ";
        $query .= "     LEFT JOIN SCHREG_BASE_MST BASE ON REGD.SCHREGNO = BASE.SCHREGNO ";
        $query .= "     LEFT JOIN (SELECT ";
        $query .= "                    SCHREGNO, ";
        $query .= "                    MIN(RESULT_CD) AS RESULT_CD ";
        $query .= "                FROM ";
        $query .= "                    SCHREG_QUALIFIED_TEST_DAT ";
        $query .= "                WHERE ";
        $query .= "                        RESULT_CD NOT IN ('8888', '9999') ";//欠席と、不合格は対象外
        $query .= "                    AND RESULT_CD IS NOT NULL ";
        $query .= "                    AND QUALIFIED_CD = '".$model->sikakuCd."' ";
        $query .= "                GROUP BY ";
        $query .= "                    SCHREGNO ";
        $query .= "               ) GET_D ON REGD.SCHREGNO = GET_D.SCHREGNO ";
        $query .= "     LEFT JOIN QUALIFIED_RESULT_MST RS_M2 ON REGD.YEAR          = RS_M2.YEAR ";
        $query .= "                                         AND RS_M2.QUALIFIED_CD = '".$model->sikakuCd."' ";
        $query .= "                                         AND GET_D.RESULT_CD    = RS_M2.RESULT_CD ";
        $query .= "     INNER JOIN SCHREG_QUALIFIED_TEST_DAT TEST ON TEST.YEAR         = REGD.YEAR ";
        $query .= "                                                AND TEST.SCHREGNO     = REGD.SCHREGNO ";
        if ($model->field["RIGHT_TEST_DATE"] != "") {
            $query .= "                                                AND TEST.TEST_DATE    = '".str_replace('/', '-', $model->field["RIGHT_TEST_DATE"])."' ";
        }
        $query .= "                                                AND TEST.QUALIFIED_CD = '".$model->sikakuCd."' ";
        $query .= "                                                AND TEST.TEST_CD      = '".$model->field["RIGHT_TEST_CD"]."' ";
        $query .= " WHERE ";
        $query .= "         REGD.YEAR     = '".CTRL_YEAR."' ";
        $query .= "     AND REGD.SEMESTER = '".CTRL_SEMESTER."' ";
        $query .= "     AND REGD.GRADE || REGD.HR_CLASS = '".$model->grade."".$model->hrClass."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE(GET_D.RESULT_CD, '9999'), ";
        $query .= "     REGD.ATTENDNO ";

        return $query;
    }

    //生徒一覧取得(右リスト)
    function getSchregRightList($model, $left_schregno) {
        $schregno_target = implode("','", (array)$left_schregno);

        $query  = " SELECT ";
        $query .= "     REGD.SCHREGNO || ':' || REGD.ATTENDNO || '-' || VALUE(GET_D.RESULT_CD, '9999')  AS VALUE, ";
        $query .= "     REGD.ATTENDNO || '番　' || VALUE(RS_M2.RESULT_NAME_ABBV, '　　　　') || '　' || BASE.NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT REGD ";
        $query .= "     LEFT JOIN SCHREG_BASE_MST BASE ON REGD.SCHREGNO = BASE.SCHREGNO ";
        $query .= "     LEFT JOIN (SELECT ";
        $query .= "                    SCHREGNO, ";
        $query .= "                    MIN(RESULT_CD) AS RESULT_CD ";//個人が持っている最高級
        $query .= "                FROM ";
        $query .= "                    SCHREG_QUALIFIED_TEST_DAT ";
        $query .= "                WHERE ";
        $query .= "                        RESULT_CD NOT IN ('8888', '9999') ";//欠席と、不合格は対象外
        $query .= "                    AND RESULT_CD IS NOT NULL ";
        $query .= "                    AND QUALIFIED_CD = '".$model->sikakuCd."' ";
        $query .= "                GROUP BY ";
        $query .= "                    SCHREGNO ";
        $query .= "               ) GET_D ON REGD.SCHREGNO = GET_D.SCHREGNO ";
        $query .= "     LEFT JOIN QUALIFIED_RESULT_MST RS_M2 ON REGD.YEAR          = RS_M2.YEAR ";
        $query .= "                                         AND RS_M2.QUALIFIED_CD = '".$model->sikakuCd."' ";
        $query .= "                                         AND GET_D.RESULT_CD    = RS_M2.RESULT_CD ";
        $query .= "     LEFT JOIN SCHREG_QUALIFIED_TEST_DAT TES2 ON REGD.YEAR          = TES2.YEAR  ";
        $query .= "                                               AND GET_D.SCHREGNO     = TES2.SCHREGNO  ";
        $query .= "                                               AND GET_D.RESULT_CD    = TES2.RESULT_CD ";
        $query .= "                                               AND TES2.QUALIFIED_CD  = '".$model->sikakuCd."' ";
        $query .= " WHERE ";
        $query .= "         REGD.YEAR     = '".CTRL_YEAR."' ";
        $query .= "     AND REGD.SEMESTER = '".CTRL_SEMESTER."' ";
        $query .= "     AND REGD.GRADE || REGD.HR_CLASS = '".$model->grade."".$model->hrClass."' ";
        $query .= "     AND REGD.SCHREGNO NOT IN ('{$schregno_target}') ";
        if (substr($model->field["RIGHT_TEST_CD"], -1) != "0") {
            $query .= "     AND GET_D.RESULT_CD = '".$model->field["RIGHT_TEST_CD"]."' ";
            $query .= "     AND TES2.LIMITED_DATE > '".CTRL_DATE."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE(GET_D.RESULT_CD, '9999'), ";
        $query .= "     REGD.ATTENDNO ";

        return $query;
    }

    //受験級コンボ
    function getTestCd($model) {
        $query  = " SELECT DISTINCT ";
        $query .= "     TEST_CD AS VALUE, ";
        $query .= "     TEST_CD || ':' || TEST_NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     QUALIFIED_TEST_MST ";
        $query .= " WHERE ";
        $query .= "         YEAR         = '".CTRL_YEAR ."' ";
        $query .= "     AND QUALIFIED_CD = '".$model->sikakuCd."' ";
        $query .= " ORDER BY ";
        $query .= "     TEST_CD ";

        return $query;
    }

/******** 追加更新処理  ********/
    //--- INSERT
    function &getDeleteInsertQuery($model, $fields) {
        $schregNo = explode(",", $model->selectdata);

        $db = Query::dbCheckOut();

        $query  = " DELETE FROM ";
        $query .= "     SCHREG_QUALIFIED_TEST_DAT ";
        $query .= " WHERE ";
        $query .= "         YEAR         = '".CTRL_YEAR."' ";
        $query .= "     AND TEST_DATE    = '".str_replace('/', '-', $fields["RIGHT_TEST_DATE"])."' ";
        $query .= "     AND QUALIFIED_CD = '".$model->sikakuCd."' ";
        $query .= "     AND TEST_CD      = '".$fields["RIGHT_TEST_CD"]."' ";
        $query .= "     AND SCHREGNO IN ";
        $query .= " ( ";
        $query .= " SELECT ";
        $query .= "     SCHREGNO ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT ";
        $query .= " WHERE ";
        $query .= "         YEAR     = '".CTRL_YEAR."' ";
        $query .= "     AND SEMESTER = '".CTRL_SEMESTER."' ";
        $query .= "     AND GRADE    = '".$model->grade."' ";
        $query .= "     AND HR_CLASS = '".$model->hrClass."' ";
        $query .= " ) ";
        $db->query($query);

        for ($i=0; $i < get_count($schregNo); $i++) {
            $data = array();
            $data["YEAR"][TEXT]             = CTRL_YEAR;
            $data["SCHREGNO"][TEXT]         = $schregNo[$i];
            $data["TEST_DATE"][DATE]        = str_replace('/', '-', $fields["RIGHT_TEST_DATE"]);
            $data["QUALIFIED_CD"][TEXT]     = $model->sikakuCd;
            $data["TEST_CD"][TEXT]          = $fields["RIGHT_TEST_CD"];
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][NUMBER]        = "sysdate()";

            $query = Query::insertSQL($data, "SCHREG_QUALIFIED_TEST_DAT");
            $db->query($query);
        }

        Query::dbCheckIn($db);
        return;
    }

    //生徒削除
    function &getDeleteQuery($model) {
        $db = Query::dbCheckOut();

        $query  = " DELETE FROM ";
        $query .= "     SCHREG_QUALIFIED_TEST_DAT ";
        $query .= " WHERE ";
        $query .= "         YEAR         = '".CTRL_YEAR."' ";
        $query .= "     AND TEST_DATE    = '".str_replace('/', '-', $model->field["RIGHT_TEST_DATE"])."' ";
        $query .= "     AND QUALIFIED_CD = '".$model->sikakuCd."' ";
        $query .= "     AND TEST_CD      = '".$model->field["RIGHT_TEST_CD"]."' ";
        $query .= "     AND SCHREGNO IN ";
        $query .= " ( ";
        $query .= " SELECT ";
        $query .= "     SCHREGNO ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT ";
        $query .= " WHERE ";
        $query .= "         YEAR     = '".CTRL_YEAR."' ";
        $query .= "     AND SEMESTER = '".CTRL_SEMESTER."' ";
        $query .= "     AND GRADE    = '".$model->grade."' ";
        $query .= "     AND HR_CLASS = '".$model->hrClass."' ";
        $query .= " ) ";

        $db->query($query);
        Query::dbCheckIn($db);
        return;
    }
}
?>
