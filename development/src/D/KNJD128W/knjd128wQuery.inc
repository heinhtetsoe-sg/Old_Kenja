<?php

require_once('for_php7.php');

class knjd128wquery extends Query
{
    public function getSecurityHigh()
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     MENU_HIGH_SECURITY_MST ";
        $query .= " WHERE ";
        $query .= "     PROGRAMID = 'KNJD128W' ";
        $query .= "     AND INVALID_FLG = '0' ";

        return $query;
    }

    public function getSchoolCd()
    {
        $query  = " SELECT ";
        $query .= "     NAME2 ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = 'Z010' ";
        $query .= "     AND NAMECD2 = '00' ";

        return $query;
    }

    //校種一覧取得
    public function getSchoolKindList($model)
    {
        $query  = " SELECT ";
        $query .= "     NAME1 AS VALUE, ";
        $query .= "     ABBV1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR    = '".CTRL_YEAR."' AND ";
        $query .= "     NAMECD1 = 'A023' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= " AND NAME1 IN ('".implode(explode(':', $model->selectSchoolKind), "','")."') ";
            }
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    public function getTestCd($model, $testCd = "")
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     T1.SEMESTER || '-' || T1.TESTKINDCD || '-' || T1.TESTITEMCD || '-' || T1.SCORE_DIV AS VALUE, ";
        $query .= "     T2.SEMESTERNAME || ':' || T3.TESTITEMABBV1 AS LABEL, ";
        $query .= "     T2.SDATE, ";
        $query .= "     T2.EDATE, ";
        $query .= "     T3.TESTITEMABBV1 AS TESTITEMNAME, ";
        $query .= "     T3.SIDOU_INPUT, ";
        $query .= "     T3.SIDOU_INPUT_INF ";
        $query .= " FROM ";
        $query .= " ADMIN_CONTROL_SDIV_DAT T1 ";
        $query .= "     LEFT JOIN SEMESTER_MST T2 ";
        $query .= "         ON  T2.YEAR         = T1.YEAR ";
        $query .= "         AND T2.SEMESTER     = T1.SEMESTER ";
        $query .= " LEFT JOIN TESTITEM_MST_COUNTFLG_NEW_SDIV T3 ";
        $query .= "         ON  T3.YEAR         = T1.YEAR ";
        $query .= "         AND T3.SEMESTER     = T1.SEMESTER ";
        $query .= "         AND T3.TESTKINDCD   = T1.TESTKINDCD ";
        $query .= "         AND T3.TESTITEMCD   = T1.TESTITEMCD ";
        $query .= "         AND T3.SCORE_DIV    = T1.SCORE_DIV ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND T1.SEMESTER || T1.TESTKINDCD || T1.TESTITEMCD || T1.SCORE_DIV NOT IN ('9990009') "; //この画面では学年評定を参照しない
        if ($testCd) {
            $query .= "     AND T1.SEMESTER || '-' || T1.TESTKINDCD || '-' || T1.TESTITEMCD || '-' || T1.SCORE_DIV = '{$testCd}' ";
        }
        $query .= " AND T1.SCHOOL_KIND = '".$model->field["SCHOOL_KIND"]."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //各種設定を取得
    public function getNameMstSettei($namecd1, $namecd2)
    {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = '{$namecd1}' ";
        $query .= "     AND NAMECD2 = '{$namecd2}' ";

        return $query;
    }

    //CSV出力
    public function getSelectDataSQL($model)
    {
        $query  = "";
        $query .= " WITH MAIN_T AS ( ";
        $query .= " SELECT ";
        $query .= "     SUB_M.CLASSCD || '-' || SUB_M.SCHOOL_KIND || '-' || SUB_M.CURRICULUM_CD || '-' || SUB_M.SUBCLASSCD AS SUBCALSSCD, ";
        $query .= "     SUB_M.SUBCLASSNAME AS SUBCLASSNAME, ";
        $query .= "     CHAIR.CHAIRCD, ";
        $query .= "     CHAIR.CHAIRNAME, ";
        $query .= "     CHAIR_STD.SCHREGNO ";
        $query .= " FROM ";
        $query .= "     CHAIR_DAT CHAIR ";
        $query .= "     INNER JOIN CHAIR_STD_DAT CHAIR_STD ";
        $query .= "          ON CHAIR_STD.YEAR       = CHAIR.YEAR ";
        $query .= "         AND CHAIR_STD.SEMESTER   = CHAIR.SEMESTER ";
        $query .= "         AND CHAIR_STD.CHAIRCD    = CHAIR.CHAIRCD ";
        $query .= "     INNER JOIN CHAIR_STF_DAT CHAIR_STF ";
        $query .= "          ON CHAIR_STF.YEAR       = CHAIR.YEAR ";
        $query .= "         AND CHAIR_STF.SEMESTER   = CHAIR.SEMESTER ";
        $query .= "         AND CHAIR_STF.CHAIRCD    = CHAIR.CHAIRCD ";
        if ($model->torikomi == "1") {
            $query .= "         AND CHAIR_STF.STAFFCD = '".STAFFCD."' ";
        }
        $query .= "     INNER JOIN SUBCLASS_MST SUB_M ON ";
        $query .= "         SUB_M.CLASSCD = CHAIR.CLASSCD AND ";
        $query .= "         SUB_M.SCHOOL_KIND = CHAIR.SCHOOL_KIND AND ";
        $query .= "         SUB_M.CURRICULUM_CD = CHAIR.CURRICULUM_CD AND ";
        $query .= "         SUB_M.SUBCLASSCD = CHAIR.SUBCLASSCD ";
        $query .= " WHERE ";
        $query .= "         CHAIR.YEAR       = '".CTRL_YEAR."' ";
        $query .= "     AND CHAIR.SEMESTER   = '".CTRL_SEMESTER."' ";
        $query .= "     AND SUB_M.SCHOOL_KIND = '".$model->field["SCHOOL_KIND"]."' ";
        //1:総学非表示（９０未満を表示とする）
        if ($model->noUseSouGaku == '1') {
            $query .= "     AND (SUB_M.SUBCLASSCD NOT LIKE '9%' ) ";
        } else {
            $query .= "     AND (SUB_M.SUBCLASSCD NOT LIKE '9%' OR SUB_M.SUBCLASSCD LIKE '90%' ) ";
        }
        $query .= " GROUP BY ";
        $query .= "     SUB_M.CLASSCD, ";
        $query .= "     SUB_M.SCHOOL_KIND, ";
        $query .= "     SUB_M.CURRICULUM_CD, ";
        $query .= "     SUB_M.SUBCLASSCD, ";
        $query .= "     SUB_M.SUBCLASSNAME, ";
        $query .= "     CHAIR.CHAIRCD, ";
        $query .= "     CHAIR.CHAIRNAME, ";
        $query .= "     CHAIR_STD.SCHREGNO ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     MAIN_T.SUBCALSSCD, ";
        $query .= "     MAIN_T.SUBCLASSNAME, ";
        $query .= "     MAIN_T.CHAIRCD, ";
        $query .= "     MAIN_T.CHAIRNAME, ";
        $query .= "     MAIN_T.SCHREGNO, ";
        $query .= "     REGDH.HR_NAME || REGD.ATTENDNO || '番' AS HR_NAME, ";
        $query .= "     BASE.NAME, ";
        $query .= "     CASE WHEN RECSCORE.VALUE_DI = '*' ";
        $query .= "          THEN RECSCORE.VALUE_DI ";
        $query .= "          ELSE CAST(RECSCORE.SCORE AS CHAR(3)) ";
        $query .= "     END AS SCORE ";
        $query .= " FROM ";
        $query .= "     MAIN_T ";
        $query .= "     LEFT JOIN SCHREG_BASE_MST BASE ON MAIN_T.SCHREGNO = BASE.SCHREGNO ";
        $query .= "     LEFT JOIN SCHREG_REGD_DAT REGD ON REGD.YEAR = '".CTRL_YEAR."' ";
        $query .= "          AND REGD.SEMESTER = '".CTRL_SEMESTER."' ";
        $query .= "          AND MAIN_T.SCHREGNO = REGD.SCHREGNO ";
        $query .= "     LEFT JOIN SCHREG_REGD_HDAT REGDH ON REGD.YEAR = REGDH.YEAR  ";
        $query .= "          AND REGD.SEMESTER = REGDH.SEMESTER  ";
        $query .= "          AND REGD.GRADE = REGDH.GRADE ";
        $query .= "          AND REGD.HR_CLASS = REGDH.HR_CLASS ";
        $query .= "     LEFT JOIN RECORD_SCORE_DAT RECSCORE ON RECSCORE.YEAR = '".CTRL_YEAR."' ";
        $query .= "          AND RECSCORE.SEMESTER || '-' || RECSCORE.TESTKINDCD || '-' || RECSCORE.TESTITEMCD || '-' || RECSCORE.SCORE_DIV = '{$model->field["TESTCD"]}' ";
        $query .= "          AND MAIN_T.SUBCALSSCD = RECSCORE.CLASSCD || '-' || RECSCORE.SCHOOL_KIND || '-' || RECSCORE.CURRICULUM_CD || '-' || RECSCORE.SUBCLASSCD ";
        $query .= "          AND MAIN_T.SCHREGNO = RECSCORE.SCHREGNO ";
        $query .= " ORDER BY ";
        $query .= "     MAIN_T.SUBCALSSCD, ";
        $query .= "     MAIN_T.CHAIRCD, ";
        $query .= "     REGDH.HR_CLASS, ";
        $query .= "     REGD.ATTENDNO, ";
        $query .= "     MAIN_T.SCHREGNO ";
        return $query;
    }

    //成績有無
    public function getExistCheck($model, $data)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     RECORD_SCORE_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND SEMESTER || '-' || TESTKINDCD || '-' || TESTITEMCD || '-' || SCORE_DIV = '{$model->field["TESTCD"]}' ";
        $query .= "     AND CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD = '{$data["SUBCLASSCD"]}' ";
        $query .= "     AND SCHREGNO = '{$data["SCHREGNO"]}' ";

        return $query;
    }

    //マスタの追加（ＣＳＶデータより読込）
    public function insertQueryCsv($model, $data)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);
        for ($i = 0; $i < get_count($data); $i++) {
            list($classCd, $schoolKind, $curriculumCd, $subclassCd) = explode("-", $data[$i]["SUBCLASSCD"]);
            list($semester, $testkindCd, $testitemCd, $scoreDiv) = explode("-", $model->field["TESTCD"]);
            $data1 = array();
            $data1["YEAR"][TEXT]            = CTRL_YEAR;
            $data1["SEMESTER"][TEXT]        = $semester;
            $data1["TESTKINDCD"][TEXT]      = $testkindCd;
            $data1["TESTITEMCD"][TEXT]      = $testitemCd;
            $data1["SCORE_DIV"][TEXT]       = $scoreDiv;
            $data1["CLASSCD"][TEXT]         = $classCd;
            $data1["SCHOOL_KIND"][TEXT]     = $schoolKind;
            $data1["CURRICULUM_CD"][TEXT]   = $curriculumCd;
            $data1["SUBCLASSCD"][TEXT]      = $subclassCd;
            $data1["SCHREGNO"][TEXT]        = $data[$i]["SCHREGNO"];
            $data1["CHAIRCD"][TEXT]         = $data[$i]["CHAIRCD"];
            if ($data["SCORE"] === "*") {
                $data1["SCORE"][NUMBER]         = null;
                $data1["VALUE_DI"][TEXT]        = $data[$i]["SCORE"];
            } else {
                $data1["SCORE"][NUMBER]         = $data[$i]["SCORE"];
                $data1["VALUE_DI"][TEXT]        = null;
            }
            $data1["REGISTERCD"][TEXT]      = STAFFCD;
            $data1["UPDATED"][NUMBER]       = "sysdate()";

            //成績データ有無
            $query = knjd128wQuery::getExistCheck($model, $data[$i]);
            $dataCnt = $db->getOne($query);
            if ($dataCnt > 0) {
                $query = knjd128wQuery::updateQueryCsv($model, $data[$i]);
            } else {
                $query = Query::insertSQL($data1, "RECORD_SCORE_DAT");
            }
            $db->query($query);
        }
        $db->commit();
        Query::dbCheckIn($db);
        return $query;
    }

    //マスタの更新（ＣＳＶデータより読込）
    public function updateQueryCsv($model, $data)
    {
        list($classCd, $schoolKind, $curriculumCd, $subclassCd) = explode("-", $data["SUBCLASSCD"]);
        list($semester, $testkindCd, $testitemCd, $scoreDiv) = explode("-", $model->field["TESTCD"]);
        $data1["CHAIRCD"][TEXT]         = $data["CHAIRCD"];
        if ($data["SCORE"] === "*") {
            $data1["SCORE"][NUMBER]         = null;
            $data1["VALUE_DI"][TEXT]        = $data["SCORE"];
        } else {
            $data1["SCORE"][NUMBER]         = $data["SCORE"];
            $data1["VALUE_DI"][TEXT]        = null;
        }
        $data1["REGISTERCD"][TEXT]      = STAFFCD;
        $data1["UPDATED"][NUMBER]       = "sysdate()";

        $where  = " WHERE ";
        $where .= "     YEAR = '".CTRL_YEAR."' ";
        $where .= "     AND SEMESTER || '-' || TESTKINDCD || '-' || TESTITEMCD || '-' || SCORE_DIV = '{$model->field["TESTCD"]}' ";
        $where .= "     AND CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD = '{$data["SUBCLASSCD"]}' ";
        $where .= "     AND SCHREGNO = '{$data["SCHREGNO"]}' ";

        $query = Query::updateSQL($data1, "RECORD_SCORE_DAT", $where);

        return $query;
    }

    //エラーＤＢへの追加
    public function insertQueryErr(&$db, $record_no, $check_error)
    {
        $data1["PROGRAMID"][TEXT]       = "KNJD128W";
        $data1["MSGROW"][NUMBER]        = $record_no;
        $data1["MSGREMARK"][TEXT]       = $check_error;
        $query = Query::insertSQL($data1, "W_CSVMSG_PRG_DAT");

        $result = $db->query($query);
    }
}
