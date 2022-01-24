<?php

require_once('for_php7.php');

class knjd128xQuery extends Query {

    function getSecurityHigh($model) {
        $query  = " SELECT ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     MENU_HIGH_SECURITY_MST ";
        $query .= " WHERE ";
        $query .= "     PROGRAMID = '{$model->getPrgId}' ";
        $query .= "     AND INVALID_FLG = '0' ";

        return $query;
    }

    function getSchoolCd() {
        $query  = " SELECT ";
        $query .= "     NAME2 ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = 'Z010' ";
        $query .= "     AND NAMECD2 = '00' ";

        return $query;
    }

    //校種取得
    function getSchkind($model) {
        $query  = " SELECT ";
        $query .= "     NAME1 AS VALUE, ";
        $query .= "     ABBV1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "         YEAR    = '".CTRL_YEAR."' ";
        $query .= "     AND NAMECD1 = 'A023' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= "     AND NAME1 IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
            }
        }
        $query .= " ORDER BY ";
        $query .= "     NAMECD2 ";

        return $query;
    }

    //学期取得
    function selectSemesAll($model)
    {
        $query  = " SELECT ";
        $query .= "     SEMESTER, ";
        $query .= "     SEMESTERNAME, ";
        $query .= "     CASE WHEN MONTH(SDATE) < 4 ";
        $query .= "          THEN MONTH(SDATE) + 12 ";
        $query .= "          ELSE MONTH(SDATE) END AS S_MONTH, ";
        $query .= "     CASE WHEN MONTH(EDATE) < 4 ";
        $query .= "          THEN MONTH(EDATE) + 12 ";
        $query .= "          ELSE MONTH(EDATE) END AS E_MONTH ";
        $query .= " FROM ";
        $query .= "     SEMESTER_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR || '-' || SEMESTER = '".$model->field["YEAR_SEMES"]."' ";
        $query .= " ORDER BY ";
        $query .= "     SEMESTER ";

        return $query;
    }

    //テスト種別(ヘッダ出力/取込用)
    function getTestitem($db, $model) {

        $subclasscd = ($model->Properties["useCurriculumcd"] == '1') ? "00-".$model->field["SCHOOL_KIND"]."-00-000000" : "000000";  //テスト種別は管理者コントロールの基本設定を使用

        $query  = " SELECT ";
        $query .= "     T1.TESTKINDCD || T1.TESTITEMCD || T1.SCORE_DIV AS VALUE, ";
        $query .= "     T1.TESTITEMNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     TESTITEM_MST_COUNTFLG_NEW_SDIV T1 ";
        $query .= " INNER JOIN ";
        $query .= "     ADMIN_CONTROL_SDIV_DAT T2 ";
        $query .= " ON ";
        $query .= "     T1.YEAR = T2.YEAR AND ";
        $query .= "     T1.SEMESTER || T1.TESTKINDCD || T1.TESTITEMCD || T1.SCORE_DIV = T2.SEMESTER || T2.TESTKINDCD || T2.TESTITEMCD || T2.SCORE_DIV ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR || '-' || T1.SEMESTER = '".CTRL_YEAR."-".$model->field["SEMESTER"]."' ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "  AND T2.CLASSCD || '-' || T2.SCHOOL_KIND || '-' || T2.CURRICULUM_CD || '-' || T2.SUBCLASSCD = '".$subclasscd."' ";
        } else {
            $query .= "  AND T2.SUBCLASSCD = '".subclasscd."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //テスト種別(出力用)
    function getTestitem2($db, $model) {

        $subclasscd = ($model->Properties["useCurriculumcd"] == '1') ? "00-".$model->field["SCHOOL_KIND"]."-00-000000" : "000000";  //テスト種別は管理者コントロールの基本設定を使用

        $query  = " SELECT ";
        $query .= "     T1.TESTKINDCD || T1.TESTITEMCD || T1.SCORE_DIV AS VALUE, ";
        $query .= "     T1.TESTITEMNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     TESTITEM_MST_COUNTFLG_NEW_SDIV T1 ";
        $query .= " INNER JOIN ";
        $query .= "     ADMIN_CONTROL_SDIV_DAT T2 ";
        $query .= " ON ";
        $query .= "     T1.YEAR = T2.YEAR AND ";
        $query .= "     T1.SEMESTER || T1.TESTKINDCD || T1.TESTITEMCD || T1.SCORE_DIV = T2.SEMESTER || T2.TESTKINDCD || T2.TESTITEMCD || T2.SCORE_DIV ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR || '-' || T1.SEMESTER = '".$model->field["YEAR_SEMES"]."' ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "  AND T2.CLASSCD || '-' || T2.SCHOOL_KIND || '-' || T2.CURRICULUM_CD || '-' || T2.SUBCLASSCD = '".$subclasscd."' ";
        } else {
            $query .= "  AND T2.SUBCLASSCD = '".subclasscd."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //年度学期一覧
    function getSelectFieldSQL()
    {
        $query  = " SELECT ";
        $query .= "     YEAR || '-' || SEMESTER AS VALUE, ";
        $query .= "     YEAR || '年度 ' || SEMESTERNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SEMESTER_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' AND ";
        $query .= "     SEMESTERNAME IS NOT NULL ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //最終学期取得
    function getMaxSemes() {
        $db = Query::dbCheckOut();
        $query   = " SELECT ";
        $query  .= "   MAX(SEMESTER) AS MAX_SEMES";
        $query  .= " FROM ";
        $query  .= "   SEMESTER_MST ";
        $query  .= " WHERE ";
        $query  .= "   YEAR = '".CTRL_YEAR."' ";
        $query  .= "   AND SEMESTER <> '9' ";
        $maxSemes = $db->getOne($query);
        Query::dbCheckIn($db);

        return $maxSemes;
    }

    //科目一覧取得
    function getSubclassList($model){

        $query  = " SELECT ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     T2.CLASSCD || '-' || T2.SCHOOL_KIND || '-' || T2.CURRICULUM_CD || '-' || T2.SUBCLASSCD AS VALUE, ";
            $query .= "     T2.CLASSCD || '-' || T2.SCHOOL_KIND || '-' || T2.CURRICULUM_CD || '-' || T2.SUBCLASSCD || ' ' || VALUE(T2.SUBCLASSABBV, '') AS LABEL ";
        } else {
            $query .= "     T2.SUBCLASSCD AS VALUE, ";
            $query .= "     T2.SUBCLASSCD || ' ' || VALUE(T2.SUBCLASSABBV, '') AS LABEL ";
        }
        $query .= " FROM ";
        $query .= "     CHAIR_DAT T1, ";
        $query .= "     SUBCLASS_MST T2 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' AND ";
        $query .= "     T1.SEMESTER = '".$model->alterSemester."' AND ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            if ($model->Properties["use_prg_schoolkind"] == "1") {
                if ($model->field["SCHOOL_KIND"]) {
                    $query .= " T1.SCHOOL_KIND = '".$model->field["SCHOOL_KIND"]."' AND ";
                }
            } else if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
                $query .= " T1.SCHOOL_KIND   = '".SCHOOLKIND."' AND ";
            }
            $query .= "     T1.CLASSCD       = T2.CLASSCD AND ";
            $query .= "     T1.SCHOOL_KIND   = T2.SCHOOL_KIND AND ";
            $query .= "     T1.CURRICULUM_CD = T2.CURRICULUM_CD AND ";
        }
        $query .= "     T1.SUBCLASSCD = T2.SUBCLASSCD ";
        $query .= " GROUP BY ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     T2.CLASSCD, ";
            $query .= "     T2.SCHOOL_KIND, ";
            $query .= "     T2.CURRICULUM_CD, ";
        }
        $query .= "     T2.SUBCLASSCD, ";
        $query .= "     T2.SUBCLASSABBV ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //学年取得
    function getSubclassTrgtGrade($model) {

        $query   = "  WITH TRGT_G AS ( ";
        $query  .= "  SELECT DISTINCT ";
        $query  .= "    T2.TRGTGRADE ";
        $query  .= "  FROM ";
        $query  .= "    CHAIR_DAT T1 ";
        $query  .= "    INNER JOIN CHAIR_CLS_DAT T2 ";
        $query  .= "      ON T1.YEAR = T2.YEAR ";
        $query  .= "      AND T1.SEMESTER = T2.SEMESTER ";
        $query  .= "      AND T1.CHAIRCD = T2.CHAIRCD ";
        $query  .= "      AND T1.GROUPCD = T2.GROUPCD ";
        $query  .= "    INNER JOIN SCHREG_REGD_GDAT T3 ";
        $query  .= "      ON T1.YEAR = T3.YEAR ";
        $query  .= "      AND T2.TRGTGRADE = T3.GRADE ";
        $query  .= "  WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' AND ";
        $query .= "     T1.SEMESTER = '".$model->alterSemester."' ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "  AND T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD = '".$model->field["SUBCLASSCD"]."' ";
        } else {
            $query .= "  AND T1.SUBCLASSCD = '".$model->field["SUBCLASSCD"]."' ";
        }
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->field["SCHOOL_KIND"]) {
                $query .= " AND T3.SCHOOL_KIND = '".$model->field["SCHOOL_KIND"]."' ";
            }
        } else if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND T3.SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query  .= "  GROUP BY ";
        $query  .= "    T1.YEAR ";
        $query  .= "    , T1.SEMESTER ";
        $query  .= "    , T1.CHAIRCD ";
        $query  .= "    , T1.GROUPCD ";
        $query  .= "    , T2.TRGTGRADE ";
        $query  .= "  ) ";

        //参照・更新可
        $query .= " SELECT DISTINCT ";
        $query .= "     T1.GRADE AS VALUE, ";
        $query .= "     T1.GRADE_NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_GDAT T1 ";
        $query .= "     LEFT JOIN SCHREG_REGD_HDAT T2 ";
        $query .= "       ON T2.YEAR = T1.YEAR ";
        $query .= "      AND T2.SEMESTER = '".$model->alterSemester."' ";
        $query .= "      AND T2.GRADE = T1.GRADE ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."'  ";
        //参照・更新可（制限付き）
        if (AUTHORITY == DEF_REFER_RESTRICT || AUTHORITY == DEF_UPDATE_RESTRICT) {
            $query .= "  AND (    T2.TR_CD1 = '".STAFFCD."' ";
            $query .= "      OR T2.TR_CD2 = '".STAFFCD."' ";
            $query .= "      OR T2.TR_CD3 = '".STAFFCD."' ";
            $query .= "      OR T2.SUBTR_CD1 = '".STAFFCD."' ";
            $query .= "      OR T2.SUBTR_CD2 = '".STAFFCD."' ";
            $query .= "      OR T2.SUBTR_CD3 = '".STAFFCD."')  ";
        }
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= " AND T1.SCHOOL_KIND IN ('".implode("':'", explode(",", $model->selectSchoolKind))."') ";
            }
        } else if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND T1.SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= " AND T1.GRADE IN (SELECT TRGTGRADE FROM TRGT_G) ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //講座一覧取得
    function getChairList($model)
    {
        $query   = "  SELECT DISTINCT ";
        $query  .= "    T1.CHAIRCD AS VALUE ";
        $query  .= "    , T1.CHAIRCD || ' ' || VALUE (T1.CHAIRNAME, '') AS LABEL ";
        $query  .= "  FROM ";
        $query  .= "    CHAIR_DAT T1 ";
        $query  .= "    INNER JOIN ";
        $query  .= "    CHAIR_CLS_DAT T2 ";
        $query  .= "    ON ";
        $query  .= "    T1.YEAR = T2.YEAR AND ";
        $query  .= "    T1.SEMESTER = T2.SEMESTER AND ";
        $query  .= "    T1.CHAIRCD = T2.CHAIRCD AND ";
        $query  .= "    T1.GROUPCD = T2.GROUPCD ";
        $query  .= "  WHERE ";
        $query  .= "    T1.YEAR = '".CTRL_YEAR."' AND ";
        $query  .= "    T1.SEMESTER = '".$model->alterSemester."' AND ";
        $query  .= "    T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD = '".$model->field["SUBCLASSCD"]."' ";
        if ($model->field["TRGTGRADE"]) {
            $query  .= "    AND T2.TRGTGRADE = '".$model->field["TRGTGRADE"]."' ";
        }
        $query  .= "  ORDER BY ";
        $query  .= "    VALUE ";

        return $query;
    }

    //マスタの追加（ＣＳＶデータより読込）
    function insertQueryCsv($model, &$data_arr)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);


        $cnt = 0;   //処理件数
        for ($i = 0; $i < get_count($data_arr); $i++) {

            if ($model->Properties["useCurriculumcd"] == "1") {
                list($classcd, $school_kind, $curriculumcd, $subclasscd) = explode("-", $data_arr[$i]["SUBCLASSCD"]);
            } else {
                $subclasscd = $data_arr[$i]["SUBCLASSCD"];
            }
            $data = array();
            $data["YEAR"][TEXT]                 = CTRL_YEAR;
            $data["SEMESTER"][TEXT]             = $model->field["SEMESTER"];
            if ($model->Properties["useCurriculumcd"] == "1") {
                $data["CLASSCD"][TEXT]          = $classcd;
                $data["SCHOOL_KIND"][TEXT]      = $school_kind;
                $data["CURRICULUM_CD"][TEXT]    = $curriculumcd;
            }
            $data["SUBCLASSCD"][TEXT]           = $subclasscd;
            $data["SCHREGNO"][TEXT]             = $data_arr[$i]["SCHREGNO"];
            $data["CHAIRCD"][TEXT]              = $data_arr[$i]["CHAIRCD"];

            $data["REGISTERCD"][TEXT]           = STAFFCD;
            $data["UPDATED"][FUNC]              = "SYSDATE()";

            //テスト種別単位の挿入
            if (get_count($model->testKindValList) > 0) {
                for ($j = 0; $j < get_count($model->testKindValList); $j++) {

                    $testKindCd = substr($model->testKindValList[$j], 0, 2);
                    $testItemCd = substr($model->testKindValList[$j], 2, 2);
                    $scoreDiv   = substr($model->testKindValList[$j], 4, 2);
                    $score      = $data_arr[$i]["TEST_SCORE_".$model->testKindValList[$j]];

                    //テスト種別単位の挿入データ
                    $data["TESTKINDCD"][TEXT]       = $testKindCd;
                    $data["TESTITEMCD"][TEXT]       = $testItemCd;
                    $data["SCORE_DIV"][TEXT]        = $scoreDiv;
                    $data["SCORE"][NUMBER]          = $score;
                    //存在チェック
                    $check   = "  SELECT ";
                    $check  .= "    COUNT(*) ";
                    $check  .= "  FROM ";
                    $check  .= "    RECORD_SCORE_DAT ";
                    $check  .= "  WHERE ";
                    $check  .= "    YEAR          = '".CTRL_YEAR."' AND ";
                    $check  .= "    SEMESTER      = '".$model->field["SEMESTER"]."' AND ";
                    $check  .= "    TESTKINDCD    = '".$testKindCd."' AND ";
                    $check  .= "    TESTITEMCD    = '".$testItemCd."' AND ";
                    $check  .= "    SCORE_DIV     = '".$scoreDiv."' AND ";
                    if ($model->Properties["useCurriculumcd"] == "1") {
                        $check  .= "    CLASSCD       = '".$classcd."' AND ";
                        $check  .= "    SCHOOL_KIND   = '".$school_kind."' AND ";
                        $check  .= "    CURRICULUM_CD = '".$curriculumcd."' AND ";
                    }
                    $check  .= "    SUBCLASSCD    = '".$subclasscd."' AND ";
                    $check  .= "    SCHREGNO      = '".$data_arr[$i]["SCHREGNO"]."' ";

                    if (1 > $db->getOne($check)) {
                        $query = Query::insertSQL($data, "RECORD_SCORE_DAT");
                    } else {
                        $where   = "  WHERE ";
                        $where  .= "    YEAR          = '".CTRL_YEAR."' AND ";
                        $where  .= "    SEMESTER      = '".$model->field["SEMESTER"]."' AND ";
                        $where  .= "    TESTKINDCD    = '".$testKindCd."' AND ";
                        $where  .= "    TESTITEMCD    = '".$testItemCd."' AND ";
                        $where  .= "    SCORE_DIV     = '".$scoreDiv."' AND ";
                        if ($model->Properties["useCurriculumcd"] == "1") {
                            $where  .= "    CLASSCD       = '".$classcd."' AND ";
                            $where  .= "    SCHOOL_KIND   = '".$school_kind."' AND ";
                            $where  .= "    CURRICULUM_CD = '".$curriculumcd."' AND ";
                        }
                        $where  .= "    SUBCLASSCD    = '".$subclasscd."' AND ";
                        $where  .= "    SCHREGNO      = '".$data_arr[$i]["SCHREGNO"]."' ";

                        $query = Query::updateSQL($data, "RECORD_SCORE_DAT", $where);
                    }

                    $db->query($query);
                }
            }

            $cnt++;
        }
        $db->commit();
        Query::dbCheckIn($db);

        return $cnt;
    }

    //削除（ＣＳＶデータより読込）
    function DeleteQueryCsv($model, &$data_arr)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $cnt = 0;   //処理件数
        for ($i = 0; $i < get_count($data_arr); $i++) {
            if ($model->Properties["useCurriculumcd"] == "1") {
                list($classcd, $school_kind, $curriculumcd, $subclasscd) = explode("-", $data_arr[$i]["SUBCLASSCD"]);
            } else {
                $subclasscd = $data_arr[$i]["SUBCLASSCD"];
            }

            if (get_count($model->testKindValList) > 0) {
                for ($j = 0; $j < get_count($model->testKindValList); $j++) {
                    $testKindCd = substr($model->testKindValList[$j], 0, 2);
                    $testItemCd = substr($model->testKindValList[$j], 2, 2);
                    $scoreDiv   = substr($model->testKindValList[$j], 4, 2);

                    $query   = "  DELETE ";
                    $query  .= "  FROM ";
                    $query  .= "    RECORD_SCORE_DAT ";
                    $query  .= "  WHERE ";
                    $query  .= "        YEAR            = '".$data_arr[$i]["YEAR"]."' ";
                    $query  .= "    AND SEMESTER        = '".$data_arr[$i]["SEMESTER"]."' ";
                    $query  .= "    AND TESTKINDCD      = '".$testKindCd."' ";
                    $query  .= "    AND TESTITEMCD      = '".$testItemCd."' ";
                    $query  .= "    AND SCORE_DIV       = '".$scoreDiv."' ";
                    $query  .= "    AND CLASSCD         = '".$classcd."' ";
                    $query  .= "    AND SCHOOL_KIND     = '".$school_kind."' ";
                    $query  .= "    AND CURRICULUM_CD   = '".$curriculumcd."' ";
                    $query  .= "    AND SUBCLASSCD      = '".$subclasscd."' ";
                    $query  .= "    AND SCHREGNO        = '".$data_arr[$i]["SCHREGNO"]."' ";

                    $db->query($query);
                    $cnt++;
                }
            }
        }

        $db->commit();
        Query::dbCheckIn($db);

        return $cnt;
    }

    //存在チェック(得点)
    function checkRecordScoreDat($model, $testcd, $subclasscd, $schregno) {

        $query   = "  SELECT ";
        $query  .= "    COUNT(*) ";
        $query  .= "  FROM ";
        $query  .= "    RECORD_SCORE_DAT ";
        $query  .= "  WHERE ";
        $query  .= "    YEAR          = '".CTRL_YEAR."' AND ";
        $query  .= "    SEMESTER      = '".$model->field["SEMESTER"]."' AND ";
        $query  .= "    TESTKINDCD || TESTITEMCD || SCORE_DIV  = '".$testcd."' AND ";
        if ($model->Properties["useCurriculumcd"] == "1") {
            $query  .= "    CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD = '".$subclasscd."' AND ";
        } else {
            $query  .= "    SUBCLASSCD = '".$subclasscd."' AND ";
        }
        $query  .= "    SCHREGNO      = '".$schregno."' ";
    }

    //存在チェック（在籍）
    function checkRegdDat($year, $semester, $schregno)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR     = '".$year."' AND ";
        $query .= "     SEMESTER = '".$semester."' AND ";
        $query .= "     SCHREGNO = '".$schregno."' ";

        return $query;
    }

    //存在チェック(科目)
    function checkSubclassMst($model, $subclasscd) {
        $query   = "  SELECT ";
        $query  .= "    COUNT(*) ";
        $query  .= "  FROM ";
        $query  .= "    SUBCLASS_MST ";
        $query  .= "  WHERE ";
        if ($model->Properties["useCurriculumcd"] == "1") {
            $query  .= "    CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD = '".$subclasscd."' ";
        } else {
            $query  .= "    SUBCLASSCD = '".$subclasscd."' ";
        }

        return $query;
    }

    //存在チェック(講座)
    function checkChairDat($year, $semester, $chaircd) {
        $query   = "  SELECT ";
        $query  .= "    COUNT(*) ";
        $query  .= "  FROM ";
        $query  .= "    CHAIR_DAT ";
        $query  .= "  WHERE ";
        $query  .= "    YEAR = '".$year."' ";
        $query  .= "    AND SEMESTER = '".$semester."' ";
        $query  .= "    AND CHAIRCD = '".$chaircd."' ";

        return $query;
    }

    //エラーＤＢへの追加
    function insertQueryErr(&$db, $record_no, $check_error)
    {
        $data1["PROGRAMID"][TEXT] = PROGRAMID;
        $data1["MSGROW"][NUMBER]    = $record_no;
        $data1["MSGREMARK"][TEXT]   = $check_error;

        $query = Query::insertSQL($data1, "W_CSVMSG_PRG_DAT");
        $result = $db->query($query);
    }

    //CSVデータ出力
    function selectMainQuery($model)
    {
        $query   = "  WITH SCORE_D AS ( ";
        $query  .= "    SELECT ";
        $query  .= "      T1.YEAR, ";
        $query  .= "      T1.SEMESTER, ";
        if ($model->Properties["useCurriculumcd"] == "1") {
            $query  .= "      T1.CLASSCD, ";
            $query  .= "      T1.SCHOOL_KIND, ";
            $query  .= "      T1.CURRICULUM_CD, ";
        }
        $query  .= "      T1.SUBCLASSCD, ";
        $query  .= "      T1.SCHREGNO, ";
        $query  .= "      T1.CHAIRCD, ";
        $query  .= "      T2.GRADE, ";
        $query  .= "      T2.HR_CLASS, ";
        $query  .= "      T2.ATTENDNO ";
        foreach ($model->testKindValList as $testkindcd) {
            $query  .= "      , MAX( CASE T1.TESTKINDCD || T1.TESTITEMCD || T1.SCORE_DIV  WHEN '".$testkindcd."' THEN (CASE WHEN VALUE_DI LIKE '%*%' THEN T1.VALUE_DI ELSE CAST(T1.SCORE AS VARCHAR(3)) END) ELSE NULL END ) AS TEST_SCORE_".$testkindcd." ";     //テスト種別コンボで選択されたテストのスコアが出る
        }
        $query  .= "    FROM ";
        $query  .= "      RECORD_SCORE_DAT T1 ";
        $query  .= "      INNER JOIN SCHREG_REGD_DAT T2 ";
        $query  .= "        ON T2.SCHREGNO = T1.SCHREGNO ";
        $query  .= "        AND T2.YEAR = T1.YEAR ";
        $query  .= "        AND T1.SEMESTER = '".$model->recordSemester."' ";
        $query  .= "        AND T2.SEMESTER = '".$model->alterSemester."' ";
        $query  .= "    WHERE ";
        $query  .= "      T1.TESTKINDCD || T1.TESTITEMCD || T1.SCORE_DIV IN ('".implode("','", $model->testKindValList)."') ";                               //テスト種別コンボの値
        $query  .= "    GROUP BY ";
        $query  .= "      T1.YEAR, ";
        $query  .= "      T1.SEMESTER, ";
        if ($model->Properties["useCurriculumcd"] == "1") {
            $query  .= "      T1.CLASSCD, ";
            $query  .= "      T1.SCHOOL_KIND, ";
            $query  .= "      T1.CURRICULUM_CD, ";
            $query  .= "      T1.SUBCLASSCD, ";
        }
        $query  .= "      T1.SUBCLASSCD, ";
        $query  .= "      T1.SCHREGNO, ";
        $query  .= "      T1.CHAIRCD, ";
        $query  .= "      T2.GRADE, ";
        $query  .= "      T2.HR_CLASS, ";
        $query  .= "      T2.ATTENDNO ";
        $query  .= "  ) ";
        $query  .= "  SELECT ";
        if ($model->Properties["useCurriculumcd"] == "1") {
            $query  .= "    T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD AS SUBCLASSCD, ";
        } else {
            $query  .= "    T1.SUBCLASSCD, ";
        }
        $query  .= "    T1.SUBCLASSNAME, ";
        $query  .= "    T2.CHAIRCD, ";
        $query  .= "    T2.CHAIRNAME, ";
        $query  .= "    T5.SCHREGNO, ";
        $query  .= "    T4.HR_NAME || '-' || T6.ATTENDNO AS HR_ATTENDNO, ";
        $query  .= "    T7.NAME ";
        foreach ($model->testKindValList as $testkindcd) {
            $query  .= "      ,TEST_SCORE_".$testkindcd;
        }
        $query  .= "  FROM ";
        $query  .= "    SUBCLASS_MST T1 ";
        $query  .= "    INNER JOIN CHAIR_DAT T2 ";
        $query  .= "     ON T2.YEAR  = '".CTRL_YEAR."' AND ";
        $query  .= "    T2.SEMESTER = '".$model->alterSemester."' ";
        if ($model->Properties["useCurriculumcd"] == "1") {
            $query  .= "    AND T2.CLASSCD || '-' || T2.SCHOOL_KIND || '-' || T2.CURRICULUM_CD || '-' || T2.SUBCLASSCD = T1.CLASSCD ";
            $query  .= "       || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD ";
        } else {
            $query  .= "    AND T2.CLASSCD = T1.SUBCLASSCD ";
        }
        $query  .= "    INNER JOIN CHAIR_CLS_DAT T3 ";
        $query  .= "      ON T2.YEAR = T3.YEAR ";
        $query  .= "      AND T2.SEMESTER = T3.SEMESTER ";
        $query  .= "      AND T2.CHAIRCD = T3.CHAIRCD ";
        $query  .= "      AND T2.GROUPCD = T3.GROUPCD ";
        $query  .= "    INNER JOIN SCHREG_REGD_HDAT T4 ";
        $query  .= "      ON T3.YEAR = T4.YEAR ";
        $query  .= "      AND T3.SEMESTER = T4.SEMESTER ";
        $query  .= "      AND T3.TRGTGRADE = T4.GRADE ";
        $query  .= "      AND T3.TRGTCLASS = T4.HR_CLASS ";
        $query  .= "    INNER JOIN ( ";
        $query  .= "      SELECT ";
        $query  .= "        YEAR ";
        $query  .= "        , SEMESTER ";
        $query  .= "        , CHAIRCD ";
        $query  .= "        , SCHREGNO ";
        $query  .= "      FROM ";
        $query  .= "        CHAIR_STD_DAT ";
        $query  .= "      GROUP BY ";
        $query  .= "        YEAR ";
        $query  .= "        , SEMESTER ";
        $query  .= "        , CHAIRCD ";
        $query  .= "        , SCHREGNO ";
        $query  .= "    ) T5 ";
        $query  .= "      ON T2.YEAR = T5.YEAR ";
        $query  .= "      AND T2.SEMESTER = T5.SEMESTER ";
        $query  .= "      AND T2.CHAIRCD = T5.CHAIRCD ";
        $query  .= "    INNER JOIN SCHREG_REGD_DAT T6 ";
        $query  .= "      ON T2.YEAR = T6.YEAR ";
        $query  .= "      AND T2.SEMESTER = T6.SEMESTER ";
        $query  .= "      AND T5.SCHREGNO = T6.SCHREGNO ";
        $query  .= "      AND T4.YEAR = T6.YEAR ";
        $query  .= "      AND T4.SEMESTER = T6.SEMESTER ";
        $query  .= "      AND T4.GRADE = T6.GRADE ";
        $query  .= "      AND T4.HR_CLASS = T6.HR_CLASS ";
        $query  .= "    LEFT JOIN SCHREG_BASE_MST T7 ";
        $query  .= "      ON T6.SCHREGNO = T7.SCHREGNO ";
        $query  .= "    LEFT JOIN SCORE_D T8 ";
        $query  .= "      ON T8.YEAR = '".CTRL_YEAR."' ";
        if ($model->Properties["useCurriculumcd"] == "1") {
            $query  .= "      AND T2.CLASSCD = T8.CLASSCD ";
            $query  .= "      AND T2.SCHOOL_KIND = T8.SCHOOL_KIND ";
            $query  .= "      AND T2.CURRICULUM_CD = T8.CURRICULUM_CD ";
        }
        $query  .= "      AND T2.SUBCLASSCD = T8.SUBCLASSCD ";
        $query  .= "      AND T6.SCHREGNO = T8.SCHREGNO ";
        $query  .= "      AND T2.CHAIRCD = T8.CHAIRCD ";
        $query  .= "      AND T6.GRADE = T8.GRADE ";
        $query  .= "      AND T6.HR_CLASS = T8.HR_CLASS ";
        $query  .= "      AND T6.ATTENDNO = T8.ATTENDNO ";
        $query  .= "  WHERE ";
        if ($model->Properties["useCurriculumcd"] == "1") {
            $query  .= "      T2.CLASSCD || '-' || T2.SCHOOL_KIND || '-' || T2.CURRICULUM_CD || '-' || T2.SUBCLASSCD = '".$model->field["SUBCLASSCD"]."' ";     //科目コンボの値
        } else {
            $query  .= "      T2.SUBCLASSCD = '".$model->field["SUBCLASSCD"]."' ";
        }
        $query  .= "      AND T4.GRADE IN ('".implode("','", $model->trgtGradeList)."') ";                                                                       //学年コンボの値
        $query  .= "      AND T2.CHAIRCD IN ('".implode("','", $model->chairCdList)."') ";                                                                       //講座コンボの値
        $query  .= "  ORDER BY ";
        if ($model->Properties["useCurriculumcd"] == "1") {
            $query  .= "    T1.CLASSCD, ";
            $query  .= "    T1.SCHOOL_KIND, ";
            $query  .= "    T1.CURRICULUM_CD, ";
        }
        $query  .= "    T1.SUBCLASSCD, ";
        $query  .= "    T2.CHAIRCD, ";
        $query  .= "    T4.GRADE, ";
        $query  .= "    T4.HR_CLASS, ";
        $query  .= "    T6.ATTENDNO, ";
        $query  .= "    T6.SCHREGNO ";

        return $query;
    }

    //学期一覧取得
    function getSemesterList()
    {
        $query  = " SELECT ";
        $query .= "     SEMESTER AS VALUE, ";
        $query .= "     SEMESTERNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SEMESTER_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }


}
?>
