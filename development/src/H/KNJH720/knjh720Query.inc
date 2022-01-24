<?php

require_once('for_php7.php');
class knjh720query extends Query
{
    //名称マスタ取得
    public function getNameMst($namecd1)
    {
        $query  = "    SELECT MST.NAME1   AS LABEL ";
        $query .= "         , YDT.NAMECD2 AS VALUE ";
        $query .= "      FROM NAME_YDAT YDT ";
        $query .= " LEFT JOIN NAME_MST  MST ";
        $query .= "        ON MST.NAMECD1 = YDT.NAMECD1 ";
        $query .= "       AND MST.NAMECD2 = YDT.NAMECD2 ";
        $query .= "     WHERE YDT.YEAR    = '".CTRL_YEAR."' ";
        $query .= "       AND YDT.NAMECD1 = '".$namecd1."' ";
        $query .= "  ORDER BY VALUE ";

        return $query;
    }

    //教科マスタ取得
    public function getClasscd()
    {
        $query  = "   SELECT CLASSNAME AS LABEL ";
        $query .= "        , CLASSCD   AS VALUE ";
        $query .= "     FROM CLASS_MST ";
        $query .= "    WHERE SCHOOL_KIND = '".SCHOOLKIND."' ";
        $query .= " ORDER BY VALUE ";

        return $query;
    }

    //科目データ取得
    public function getSubclassMst($model)
    {
        $query  = "    SELECT ASD.CLASSCD";
        $query .= "         , SCS.SCHOOL_KIND ";
        $query .= "         , SCS.CURRICULUM_CD ";
        $query .= "         , ASD.SUBCLASSCD ";
        $query .= "         , ASD.SUBCLASSNAME ";
        $query .= "         , ASD.SUBCLASSCD AS VALUE ";
        $query .= "      FROM ACADEMICTEST_SUBCLASS_DAT ASD ";
        $query .= " LEFT JOIN CLASS_MST CLS ";
        $query .= "        ON CLS.CLASSCD     = ASD.CLASSCD ";
        $query .= "       AND CLS.SCHOOL_KIND = '".SCHOOLKIND."' ";
        $query .= " LEFT JOIN SUBCLASS_MST SCS ";
        $query .= "        ON SCS.CLASSCD     = ASD.CLASSCD ";
        $query .= "       AND SCS.SUBCLASSCD  = ASD.SUBCLASSCD ";
        $query .= "       AND SCS.SCHOOL_KIND = CLS.SCHOOL_KIND ";
        $query .= "     WHERE ASD.YEAR        = '".CTRL_YEAR."' ";
        $query .= "       AND ASD.CLASSCD     = '".$model->field["CLASSCD"]."' ";
        $query .= "       AND ASD.TESTDIV     = '".$model->field["TESTDIV"]."' ";
        $query .= "  GROUP BY ASD.CLASSCD";
        $query .= "         , SCS.SCHOOL_KIND ";
        $query .= "         , SCS.CURRICULUM_CD ";
        $query .= "         , ASD.SUBCLASSCD ";
        $query .= "         , ASD.SUBCLASSNAME ";
        $query .= "         , ASD.SUBCLASSCD ";
        $query .= "  ORDER BY VALUE ";

        return $query;
    }

    //教室取得
    public function getFacility($model)
    {
        $query  = "    SELECT ";
        $query .= "  DISTINCT STF.FACCD        AS VALUE ";
        $query .= "         , FAC.FACILITYNAME AS LABEL ";
        $query .= "      FROM ACADEMICTEST_STF_DAT STF ";
        $query .= " LEFT JOIN FACILITY_MST FAC ";
        $query .= "        ON FAC.FACCD   = STF.FACCD ";
        $query .= " LEFT JOIN ACADEMICTEST_MST MST ";
        $query .= "        ON MST.YEAR    = STF.YEAR ";
        $query .= "       AND MST.TESTID  = STF.TESTID ";
        $query .= "     WHERE STF.YEAR    = '".CTRL_YEAR."' ";
        $query .= "       AND MST.TESTDIV = '".$model->field["TESTDIV"]."' ";
        $query .= "  ORDER BY VALUE";

        return $query;
    }

    //全体のデータの取得
    public function selectQuery()
    {
        $query .= "    SELECT SRD.SCHREGNO ";
        $query .= "         , SRD.GRADE ";
        $query .= "         , SRD.HR_CLASS ";
        $query .= "         , SRH.HR_NAMEABBV ";
        $query .= "         , SRD.ATTENDNO ";
        $query .= "         , SBM.SEX ";
        $query .= "         , SBM.NAME ";
        $query .= "         , SBM.NAME_KANA ";
        $query .= "      FROM SCHREG_REGD_DAT SRD ";
        $query .= " LEFT JOIN SCHREG_REGD_HDAT SRH ";
        $query .= "        ON SRH.YEAR     = SRD.YEAR ";
        $query .= "       AND SRH.SEMESTER = SRD.SEMESTER ";
        $query .= "       AND SRH.GRADE    = SRD.GRADE ";
        $query .= "       AND SRH.HR_CLASS = SRD.HR_CLASS ";
        $query .= " LEFT JOIN SCHREG_BASE_MST SBM ";
        $query .= "        ON SBM.SCHREGNO = SRD.SCHREGNO ";
        $query .= "     WHERE SRD.YEAR     = '".CTRL_YEAR."' ";
        $query .= "       AND SRD.SEMESTER = '".CTRL_SEMESTER."' ";
        $query .= "       AND SRD.GRADE    = '03' ";
        $query .= "  ORDER BY SRD.GRADE ";
        $query .= "         , SRD.HR_CLASS ";
        $query .= "         , SRD.ATTENDNO ";

        return $query;
    }

    //成績データ
    public function getScore($model)
    {
        $query  = " WITH SELECT_T AS ( ".knjh720Query::selectQuery().") ";
        $query .= "    SELECT SRD.* ";
        $query .= "         , ASD.SCORE_1 ";
        $query .= "         , ASD.ABSENCE_FLG_1 ";
        $query .= "         , (SELECT STAFFNAME_SHOW FROM STAFF_MST WHERE STAFFCD = ASD.REGISTER_1) AS STAFF1 ";
        $query .= "         , ASD.SCORE_2 ";
        $query .= "         , ASD.ABSENCE_FLG_2 ";
        $query .= "         , (SELECT STAFFNAME_SHOW FROM STAFF_MST WHERE STAFFCD = ASD.REGISTER_2) AS STAFF2 ";
        $query .= "      FROM SCHREG_ACADEMICTEST_DAT SAD ";
        if ($model->field["FACCD"] != "") {
            $query .= " INNER JOIN ACADEMICTEST_FAC_DAT AFD ";
            $query .= "        ON AFD.YEAR       = '".CTRL_YEAR."' ";
            $query .= "       AND AFD.SCHREGNO   = SAD.SCHREGNO ";
            $query .= "       AND AFD.TESTID     LIKE '".$model->field["TESTDIV"]."%' ";
            $query .= "       AND AFD.FACCD      = '".$model->field["FACCD"]."' ";
            $query .= "       AND AFD.SUBCLASSCD = '".$model->field["SUBCLASSCD"]."' ";
        }
        $query .= " LEFT JOIN SELECT_T SRD ";
        $query .= "        ON SRD.SCHREGNO   = SAD.SCHREGNO ";
        $query .= " LEFT JOIN ACADEMICTEST_SCORE_DAT ASD ";
        $query .= "        ON ASD.YEAR       = '".CTRL_YEAR."' ";
        $query .= "       AND ASD.TESTDIV    = '".$model->field["TESTDIV"]."' ";
        $query .= "       AND ASD.CLASSCD    = SAD.CLASSCD ";
        $query .= "       AND ASD.SUBCLASSCD = SAD.SUBCLASSCD ";
        $query .= "       AND ASD.SCHREGNO   = SAD.SCHREGNO ";
        $query .= "     WHERE SAD.CLASSCD    = '".$model->field["CLASSCD"]."' ";
        $query .= "       AND SAD.SUBCLASSCD = '".$model->field["SUBCLASSCD"]."' ";
        $query .= "       AND SAD.DECLINE_FLG <> '1' ";  //辞退者以外
        $query .= "  ORDER BY SRD.GRADE ";
        $query .= "         , SRD.HR_CLASS ";
        $query .= "         , SRD.ATTENDNO ";

        return $query;
    }

    //成績データ(CSV書出)
    public function getScoreCSV($model, $input)
    {
        $query  = " WITH SELECT_T AS ( ".knjh720Query::selectQuery().") ";
        $query .= "    SELECT SRD.* ";
        $query .= "         , ASD.YEAR ";
        $query .= "         , ASD.TESTDIV ";
        $query .= "         , SAD.CLASSCD ";
        $query .= "         , SAD.SUBCLASSCD ";
        if ($input == "1") {
            //入力回数：1回目
            $query .= "         , ASD.SCORE_1       AS SCORE ";
            $query .= "         , ASD.ABSENCE_FLG_1 AS ABSENCE ";
        } else {
            //入力回数：2回目
            $query .= "         , ASD.SCORE_2       AS SCORE ";
            $query .= "         , ASD.ABSENCE_FLG_2 AS ABSENCE ";
        }
        $query .= "         , 'LASTCOLUMN' AS LASTCOLUMN ";
        $query .= "      FROM SCHREG_ACADEMICTEST_DAT SAD ";
        if ($model->field["FACCD"] != "") {
            $query .= " INNER JOIN ACADEMICTEST_FAC_DAT AFD ";
            $query .= "        ON AFD.YEAR       = '".CTRL_YEAR."' ";
            $query .= "       AND AFD.SCHREGNO   = SAD.SCHREGNO ";
            $query .= "       AND AFD.TESTID     LIKE '".$model->field["TESTDIV"]."%' ";
            $query .= "       AND AFD.FACCD      = '".$model->field["FACCD"]."' ";
            $query .= "       AND AFD.SUBCLASSCD = '".$model->field["SUBCLASSCD"]."' ";
        }
        $query .= " LEFT JOIN SELECT_T SRD ";
        $query .= "        ON SRD.SCHREGNO   = SAD.SCHREGNO ";
        $query .= " LEFT JOIN ACADEMICTEST_SCORE_DAT ASD ";
        $query .= "        ON ASD.YEAR       = '".CTRL_YEAR."' ";
        $query .= "       AND ASD.TESTDIV    = '".$model->field["TESTDIV"]."' ";
        $query .= "       AND ASD.CLASSCD    = SAD.CLASSCD ";
        $query .= "       AND ASD.SUBCLASSCD = SAD.SUBCLASSCD ";
        $query .= "       AND ASD.SCHREGNO   = SAD.SCHREGNO ";
        $query .= "     WHERE SAD.CLASSCD    = '".$model->field["CLASSCD"]."' ";
        $query .= "       AND SAD.SUBCLASSCD = '".$model->field["SUBCLASSCD"]."' ";
        $query .= "       AND SAD.DECLINE_FLG <> '1' ";  //辞退者以外
        $query .= "  ORDER BY SRD.GRADE ";
        $query .= "         , SRD.HR_CLASS ";
        $query .= "         , SRD.ATTENDNO ";

        return $query;
    }

    //登録・更新
    public function updInsRecordScore($flg, $model, $schno, $score1, $absence1, $score2, $absence2, $db)
    {
        $data = array();
        if ($flg == "insert") {
            $data["YEAR"][TEXT]       = CTRL_YEAR;
            $data["TESTDIV"][TEXT]    = $model->field["TESTDIV"];
            $data["CLASSCD"][TEXT]    = $model->field["CLASSCD"];
            $data["SUBCLASSCD"][TEXT] = $model->field["SUBCLASSCD"];
            $data["SCHREGNO"][TEXT]   = $schno;
        }
        if ($model->field["INPUT_NUM"] == "1") {
            //入力1回目
            $data["SCORE_1"][NUMBER]     = $score1;
            $data["ABSENCE_FLG_1"][TEXT] = $absence1;
            $data["REGISTER_1"][TEXT]    = STAFFCD;
        } else {
            //入力2回目
            $data["SCORE_2"][NUMBER]     = $score2;
            $data["ABSENCE_FLG_2"][TEXT] = $absence2;
            $data["REGISTER_2"][TEXT]    = STAFFCD;
        }
        $data["REGISTERCD"][TEXT] = STAFFCD;
        $data["UPDATED"][FUNC]    = "sysdate()";

        if ($flg == "insert") {
            $query = Query::insertSQL($data, "ACADEMICTEST_SCORE_DAT");
            $db->query($query);
        } else {
            $where  = " WHERE YEAR       = '".CTRL_YEAR."' ";
            $where .= "   AND TESTDIV    = '".$model->field["TESTDIV"]."' ";
            $where .= "   AND CLASSCD    = '".$model->field["CLASSCD"]."' ";
            $where .= "   AND SUBCLASSCD = '".$model->field["SUBCLASSCD"]."' ";
            $where .= "   AND SCHREGNO   = '".$schno."' ";

            //変更があった場合のみ更新
            $oldScore1   = "";
            $oldAbsence1 = "";
            $oldScore2   = "";
            $oldAbsence2 = "";
            $scoreQuery = knjh720Query::scoreData($schno, $model);
            $result = $db->query($scoreQuery);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                //登録されているデータ
                $oldScore1   = $row["SCORE_1"];
                $oldAbsence1 = $row["ABSENCE_FLG_1"];
                $oldScore2   = $row["SCORE_2"];
                $oldAbsence2 = $row["ABSENCE_FLG_2"];
            }
            if ($model->field["INPUT_NUM"] == "1"
                && ($score1 != $oldScore1 || $absence1 != $oldAbsence1)) {
                $query = Query::updateSQL($data, "ACADEMICTEST_SCORE_DAT", $where);
                $db->query($query);
            } else if ($model->field["INPUT_NUM"] == "2"
                       && ($score2 != $oldScore2 || $absence2 != $oldAbsence2)) {
                $query = Query::updateSQL($data, "ACADEMICTEST_SCORE_DAT", $where);
                $db->query($query);
            }
        }
    }

    //データ有無チェック
    public function checkExistData($schno, $model)
    {
        $query  = " SELECT COUNT(*) ";
        $query .= "   FROM ACADEMICTEST_SCORE_DAT ";
        $query .= "  WHERE YEAR       = '".CTRL_YEAR."' ";
        $query .= "    AND TESTDIV    = '".$model->field["TESTDIV"]."' ";
        $query .= "    AND CLASSCD    = '".$model->field["CLASSCD"]."' ";
        $query .= "    AND SUBCLASSCD = '".$model->field["SUBCLASSCD"]."' ";
        $query .= "    AND SCHREGNO   = '".$schno."' ";

        return $query;
    }

    //登録済み点数データ
    public function scoreData($schno, $model)
    {
        $query  = " SELECT * ";
        $query .= "   FROM ACADEMICTEST_SCORE_DAT ";
        $query .= "  WHERE YEAR       = '".CTRL_YEAR."' ";
        $query .= "    AND TESTDIV    = '".$model->field["TESTDIV"]."' ";
        $query .= "    AND CLASSCD    = '".$model->field["CLASSCD"]."' ";
        $query .= "    AND SUBCLASSCD = '".$model->field["SUBCLASSCD"]."' ";
        $query .= "    AND SCHREGNO   = '".$schno."' ";

        return $query;
    }

    //マスタ(存在チェック用)
    public function getMasterCheck($data)
    {
        $query  = " SELECT COUNT(*) ";
        $query .= "   FROM SCHREG_REGD_DAT ";
        $query .= "  WHERE SCHREGNO = '".$data["SCHREGNO"]."' ";
        $query .= "    AND YEAR     = '".$data["YEAR"]."' ";
        $query .= "    AND GRADE    = '03' ";

        return $query;
    }

    //主キー取得
    public function getPrimaryKeyData($tableName)
    {
        $query  = " SELECT NAME ";
        $query .= "   FROM SYSIBM.SYSCOLUMNS ";
        $query .= "  WHERE TBNAME = '".$tableName."' ";
        $query .= "    AND KEYSEQ > 0 ";
        $query .= "  ORDER BY KEYSEQ ASC ";

        return $query;
    }
}
