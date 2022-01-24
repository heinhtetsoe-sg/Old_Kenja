<?php

require_once('for_php7.php');

class knjm260aQuery extends Query {

    //学期名
    function getSemesterName()
    {
        $query  = " SELECT ";
        $query .= "     SEMESTERNAME ";
        $query .= " FROM ";
        $query .= "     SEMESTER_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND SEMESTER = '".CTRL_SEMESTER."' ";

        return $query;
    }

    //生徒情報取得
    function getStudentInfoData($model)
    {
        $query .= " SELECT ";
        $query .= "     REGD.SCHREGNO, ";
        $query .= "     REGD.GRADE, ";
        $query .= "     REGD.COURSECD, ";
        $query .= "     REGD.MAJORCD, ";
        $query .= "     REGD.COURSECODE, ";
        $query .= "     REGDH.TR_CD1, ";
        $query .= "     BASE.NAME ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT REGD  ";
        $query .= "     LEFT JOIN SCHREG_BASE_MST BASE ON REGD.SCHREGNO = BASE.SCHREGNO ";
        $query .= "     LEFT JOIN SCHREG_REGD_HDAT REGDH ON REGD.YEAR = REGDH.YEAR ";
        $query .= "          AND REGD.SEMESTER = REGDH.SEMESTER ";
        $query .= "          AND REGD.GRADE = REGDH.GRADE ";
        $query .= "          AND REGD.HR_CLASS = REGDH.HR_CLASS ";
        $query .= " WHERE ";
        $query .= "     REGD.YEAR = '".CTRL_YEAR."' AND ";
        $query .= "     REGD.SEMESTER = '".CTRL_SEMESTER."' AND ";
        $query .= "     REGD.SCHREGNO = '".$model->schregno."' ";

        return $query;
    }

    //担当名取得
    function getStaffMst($staffCd)
    {
        $query  = " SELECT ";
        $query .= "     STAFFNAME ";
        $query .= " FROM ";
        $query .= "     STAFF_MST ";
        $query .= " WHERE ";
        $query .= "     STAFFCD = '{$staffCd}' ";

        return $query;
    }

    //名称マスタ
    function getNameMst($namecd1)
    {
        $query  = " SELECT ";
        $query .= "     NAMECD2 AS VALUE, ";
        $query .= "     NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND NAMECD1 = '{$namecd1}' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    function getTestData($model)
    {
        $query .= " WITH CHAIR_T AS ( ";
        $query .= " SELECT ";
        $query .= "     CSTD.SCHREGNO, ";
        $query .= "     SUBM.CLASSCD || '-' || SUBM.CURRICULUM_CD AS CLASSCD_SHOW, ";
        $query .= "     SUBM.CLASSCD || '-' || SUBM.SCHOOL_KIND || '-' || SUBM.CURRICULUM_CD || '-' || SUBM.SUBCLASSCD AS SUBCLASSCD, ";
        $query .= "     MAX(SUBM.SUBCLASSNAME) AS SUBCLASSNAME, ";
        $query .= "     MAX(CRE.CREDITS) AS CREDITS, ";
        $query .= "     MAX(CORRES_SEME.REPO_MAX_CNT) AS REPO_MAX_CNT, ";
        $query .= "     MAX(CORRES_SEME.USE_MEDIA1) AS USE_MEDIA1 ";
        $query .= " FROM ";
        $query .= "     CHAIR_STD_DAT CSTD ";
        $query .= "     INNER JOIN CHAIR_DAT CHAIRD ON CSTD.YEAR = CHAIRD.YEAR ";
        $query .= "           AND CSTD.SEMESTER = CHAIRD.SEMESTER ";
        $query .= "           AND CSTD.CHAIRCD= CHAIRD.CHAIRCD ";
        $query .= "     INNER JOIN SUBCLASS_MST SUBM ON CHAIRD.CLASSCD = SUBM.CLASSCD ";
        $query .= "           AND CHAIRD.SCHOOL_KIND = SUBM.SCHOOL_KIND ";
        $query .= "           AND CHAIRD.CURRICULUM_CD = SUBM.CURRICULUM_CD ";
        $query .= "           AND CHAIRD.SUBCLASSCD = SUBM.SUBCLASSCD ";
        $query .= "     INNER JOIN CHAIR_CORRES_SEMES_DAT CORRES_SEME ON CHAIRD.YEAR = CORRES_SEME.YEAR ";
        $query .= "           AND CHAIRD.SEMESTER = CORRES_SEME.SEMESTER ";
        $query .= "           AND CHAIRD.CHAIRCD = CORRES_SEME.CHAIRCD ";
        $query .= "           AND CHAIRD.CLASSCD = CORRES_SEME.CLASSCD ";
        $query .= "           AND CHAIRD.SCHOOL_KIND = CORRES_SEME.SCHOOL_KIND ";
        $query .= "           AND CHAIRD.CURRICULUM_CD = CORRES_SEME.CURRICULUM_CD ";
        $query .= "           AND CHAIRD.SUBCLASSCD = CORRES_SEME.SUBCLASSCD ";
        $query .= "     LEFT JOIN CREDIT_MST CRE ON CHAIRD.YEAR = CRE.YEAR ";
        $query .= "          AND CRE.COURSECD = '{$model->courseCd}' ";
        $query .= "          AND CRE.MAJORCD = '{$model->majorCd}' ";
        $query .= "          AND CRE.GRADE = '{$model->grade}' ";
        $query .= "          AND CRE.COURSECODE = '{$model->courseCode}' ";
        $query .= "          AND CHAIRD.CLASSCD = CRE.CLASSCD ";
        $query .= "          AND CHAIRD.SCHOOL_KIND = CRE.SCHOOL_KIND ";
        $query .= "          AND CHAIRD.CURRICULUM_CD = CRE.CURRICULUM_CD ";
        $query .= "          AND CHAIRD.SUBCLASSCD = CRE.SUBCLASSCD ";
        if ($model->Properties["printSubclassLastChairStd"] == "1") {
            $query .= "         INNER JOIN SEMESTER_MST T3 ON T3.YEAR  = CSTD.YEAR ";
            $query .= "              AND T3.SEMESTER = CSTD.SEMESTER ";
            $query .= "              AND T3.EDATE    = CSTD.APPENDDATE ";
        }
        $query .= " WHERE ";
        $query .= "     CSTD.YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND CSTD.SEMESTER = '".CTRL_SEMESTER."' ";
        $query .= "     AND CSTD.SCHREGNO = '{$model->schregno}' ";
        $query .= " GROUP BY ";
        $query .= "     CSTD.SCHREGNO, ";
        $query .= "     SUBM.CLASSCD, ";
        $query .= "     SUBM.SCHOOL_KIND, ";
        $query .= "     SUBM.CURRICULUM_CD, ";
        $query .= "     SUBM.SUBCLASSCD ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     CHAIR_T.SCHREGNO, ";
        $query .= "     CHAIR_T.CLASSCD_SHOW, ";
        $query .= "     CHAIR_T.SUBCLASSCD, ";
        $query .= "     CHAIR_T.SUBCLASSNAME, ";
        $query .= "     R1.VAL_NUMERIC AS R_VAL1, ";
        $query .= "     R2.VAL_NUMERIC AS R_VAL2, ";
        $query .= "     R3.VAL_NUMERIC AS R_VAL3, ";
        $query .= "     T1.VAL_NUMERIC AS T_VAL1, ";
        $query .= "     S1.VAL_NUMERIC AS S_VAL1, ";
        $query .= "     CHAIR_T.CREDITS, ";
        $query .= "     CHAIR_T.REPO_MAX_CNT, ";
        $query .= "     CHAIR_T.USE_MEDIA1 ";
        $query .= " FROM ";
        $query .= "     CHAIR_T ";
        $query .= "     LEFT OUTER JOIN SUBCLASS_CORRES_RST_SEMES_DAT R1 ON R1.YEAR = '".CTRL_YEAR."' ";
        $query .= "          AND R1.SEMESTER = '".CTRL_SEMESTER."' ";
        $query .= "          AND CHAIR_T.SUBCLASSCD = R1.CLASSCD || '-' || R1.SCHOOL_KIND  || '-' || R1.CURRICULUM_CD  || '-' || R1.SUBCLASSCD ";
        $query .= "          AND R1.RST_DIV = 'R' ";
        $query .= "          AND R1.SEQ = 1 ";
        $query .= "          AND CHAIR_T.SCHREGNO = R1.SCHREGNO ";
        $query .= "     LEFT OUTER JOIN SUBCLASS_CORRES_RST_SEMES_DAT R2 ON R2.YEAR = '".CTRL_YEAR."' ";
        $query .= "          AND R2.SEMESTER = '".CTRL_SEMESTER."' ";
        $query .= "          AND CHAIR_T.SUBCLASSCD = R2.CLASSCD || '-' || R2.SCHOOL_KIND  || '-' || R2.CURRICULUM_CD  || '-' || R2.SUBCLASSCD ";
        $query .= "          AND R2.RST_DIV = 'R' ";
        $query .= "          AND R2.SEQ = 2 ";
        $query .= "          AND CHAIR_T.SCHREGNO = R2.SCHREGNO ";
        $query .= "     LEFT OUTER JOIN SUBCLASS_CORRES_RST_SEMES_DAT R3 ON R3.YEAR = '".CTRL_YEAR."' ";
        $query .= "          AND R3.SEMESTER = '".CTRL_SEMESTER."' ";
        $query .= "          AND CHAIR_T.SUBCLASSCD = R3.CLASSCD || '-' || R3.SCHOOL_KIND  || '-' || R3.CURRICULUM_CD  || '-' || R3.SUBCLASSCD ";
        $query .= "          AND R3.RST_DIV = 'R' ";
        $query .= "          AND R3.SEQ = 3 ";
        $query .= "          AND CHAIR_T.SCHREGNO = R3.SCHREGNO ";
        $query .= "     LEFT OUTER JOIN SUBCLASS_CORRES_RST_SEMES_DAT T1 ON T1.YEAR = '".CTRL_YEAR."' ";
        $query .= "          AND T1.SEMESTER = '".CTRL_SEMESTER."' ";
        $query .= "          AND CHAIR_T.SUBCLASSCD = T1.CLASSCD || '-' || T1.SCHOOL_KIND  || '-' || T1.CURRICULUM_CD  || '-' || T1.SUBCLASSCD ";
        $query .= "          AND T1.RST_DIV = 'T' ";
        $query .= "          AND T1.SEQ = 1 ";
        $query .= "          AND CHAIR_T.SCHREGNO = T1.SCHREGNO ";
        $query .= "     LEFT OUTER JOIN SUBCLASS_CORRES_RST_SEMES_DAT S1 ON S1.YEAR = '".CTRL_YEAR."' ";
        $query .= "          AND S1.SEMESTER = '".CTRL_SEMESTER."' ";
        $query .= "          AND CHAIR_T.SUBCLASSCD = S1.CLASSCD || '-' || S1.SCHOOL_KIND  || '-' || S1.CURRICULUM_CD  || '-' || S1.SUBCLASSCD ";
        $query .= "          AND S1.RST_DIV = 'S' ";
        $query .= "          AND S1.SEQ = 1 ";
        $query .= "          AND CHAIR_T.SCHREGNO = S1.SCHREGNO ";
        $query .= " ORDER BY ";
        $query .= "     CHAIR_T.SUBCLASSCD ";

        return $query;
    }

    //追加
    function &InsertQuery($model)
    {
        $db = Query::dbCheckOut();

        foreach ($model->setData as $key => $val) {
            $query = knjm260aQuery::getInsSql($model, "R", 1, $key, $val["R_VAL1"]);
            $db->query($query);

            $query = knjm260aQuery::getInsSql($model, "R", 2, $key, $val["R_VAL2"]);
            $db->query($query);

            $query = knjm260aQuery::getInsSql($model, "R", 3, $key, $val["R_VAL3"]);
            $db->query($query);

            $query = knjm260aQuery::getInsSql($model, "S", 1, $key, $val["S_VAL1"]);
            $db->query($query);

            $query = knjm260aQuery::getInsSql($model, "T", 1, $key, $val["T_VAL1"]);
            $db->query($query);
        }
        $db->commit();

        Query::dbCheckIn($db);
    }

    //削除
    function &getInsSql($model, $rstDiv, $seq, $key, $setVal)
    {
        list($classcd, $school_kind, $curriculum_cd, $subclasscd) = preg_split("/-/", $key);

        $data = array();
        $data["YEAR"][TEXT]                 = CTRL_YEAR;
        $data["SEMESTER"][TEXT]             = CTRL_SEMESTER;
        $data["CLASSCD"][TEXT]              = $classcd;
        $data["SCHOOL_KIND"][TEXT]          = $school_kind;
        $data["CURRICULUM_CD"][TEXT]        = $curriculum_cd;
        $data["SUBCLASSCD"][TEXT]           = $subclasscd;
        $data["RST_DIV"][TEXT]              = $rstDiv;
        $data["SEQ"][NUMBER]                = $seq;
        $data["SCHREGNO"][TEXT]             = $model->field["SCHREGNO"];
        $data["VAL_NUMERIC"][NUMBER]        = $setVal;
        $data["REGISTERCD"][TEXT]           = STAFFCD;
        $data["UPDATED"][FUNC]              = "sysdate()";

        $query = Query::insertSQL($data, "SUBCLASS_CORRES_RST_SEMES_DAT");

        return $query;
    }

    //削除
    function &DeleteQuery($model)
    {
        $db = Query::dbCheckOut();

        $query  = " DELETE FROM SUBCLASS_CORRES_RST_SEMES_DAT ";
        $query .= " WHERE YEAR  = '" .CTRL_YEAR ."' ";
        $query .= "       AND SEMESTER = '".CTRL_SEMESTER."' ";
        $query .= "       AND SCHREGNO = '".$model->field["SCHREGNO"]."' ";
        $db->query($query);

        Query::dbCheckIn($db);
    }

}
?>
