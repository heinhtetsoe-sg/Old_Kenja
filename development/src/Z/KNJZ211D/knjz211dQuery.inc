<?php

require_once('for_php7.php');
class knjz211dQuery extends Query {
    //校種取得
    function getSchKind($model, $val="") {
        $query  = " SELECT DISTINCT ";
        $query .= "     ABBV1 AS LABEL, ";
        $query .= "     NAME1 AS VALUE ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "         YEAR    = '".CTRL_YEAR."' ";
        $query .= "     AND NAMECD1 = 'A023' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= "     AND NAME1 IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
            }
        } else if ($model->Properties["useSchool_KindField"] == "1") {
            $query .= " AND NAME1 = '".SCHOOLKIND."' ";
        }
        if ($val) {
            $query .= " AND NAME1 = '".$val."' ";
        }
        $query .= "     AND NAME1 IN ( SELECT DISTINCT SCHOOL_KIND FROM JVIEWSTAT_LEVEL_PATTERN_YMST WHERE YEAR = '".CTRL_YEAR."') ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //学年取得
    function getGrade($model) {
        $query  = " SELECT ";
        $query .= "     GRADE AS VALUE, ";
        $query .= "     GRADE_NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_GDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= " AND SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
            }
        } else if ($model->Properties["useSchool_KindField"] == "1") {
            $query .= " AND SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        if ($model->schoolkind != "") {
            $query .= " AND SCHOOL_KIND = '".$model->schoolkind."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //科目取得
    function getSubclasscd($model) {
        $query  = " SELECT ";
        $query .= "     CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD AS VALUE, ";
        $query .= "     CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD || '：' || SUBCLASSNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     V_SUBCLASS_MST T1 ";
        $query .= " WHERE ";
        $query .= "     YEAR        = '".CTRL_YEAR."' AND ";
        $query .= "     SCHOOL_KIND = '".$model->schoolkind."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //学期取得
    function getSemester() {
        $query  = " SELECT ";
        $query .= "     SEMESTER, ";
        $query .= "     SEMESTERNAME ";
        $query .= " FROM ";
        $query .= "     SEMESTER_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR        = '".CTRL_YEAR."' AND ";
        $query .= "     SEMESTER   <> '9' ";
        $query .= " ORDER BY ";
        $query .= "     SEMESTER ";

        return $query;
    }

    //パターン取得
    function getPattern($model) {
        $query  = " SELECT ";
        $query .= "     PATTERN_CD AS VALUE, ";
        $query .= "     PATTERN_CD || '：' || PATTERN_NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     JVIEWSTAT_LEVEL_PATTERN_YMST ";
        $query .= " WHERE ";
        $query .= "     YEAR        = '".CTRL_YEAR."' AND ";
        $query .= "     SCHOOL_KIND = '".$model->schoolkind."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //一覧表示
    function selectQuery($model) {
        $query  = " SELECT ";
        $query .= "     T1.VIEWCD, ";
        $query .= "     T2.VIEWNAME, ";
        $query .= "     T2.SHOWORDER, ";
        $query .= "     P1.PATTERN_CD ";
        foreach ($model->semester as $sem => $semname) {
            $query .= "    ,L".$sem.".VIEWFLG AS VIEWFLG".$sem;
        }
        $query .= " FROM ";
        $query .= "     JVIEWNAME_GRADE_YDAT T1 ";
        $query .= "     INNER JOIN JVIEWNAME_GRADE_MST T2 ";
        $query .= "          ON T1.GRADE            = T2.GRADE ";
        $query .= "         AND T1.CLASSCD          = T2.CLASSCD ";
        $query .= "         AND T1.SCHOOL_KIND      = T2.SCHOOL_KIND ";
        $query .= "         AND T1.CURRICULUM_CD    = T2.CURRICULUM_CD ";
        $query .= "         AND T1.SUBCLASSCD       = T2.SUBCLASSCD ";
        $query .= "         AND T1.VIEWCD           = T2.VIEWCD ";
        $query .= "     LEFT JOIN JVIEWSTAT_SUBCLASS_PATTERN_DAT P1 ";
        $query .= "          ON T1.YEAR             = P1.YEAR ";
        $query .= "         AND T1.GRADE            = P1.GRADE ";
        $query .= "         AND T1.CLASSCD          = P1.CLASSCD ";
        $query .= "         AND T1.SCHOOL_KIND      = P1.SCHOOL_KIND ";
        $query .= "         AND T1.CURRICULUM_CD    = P1.CURRICULUM_CD ";
        $query .= "         AND T1.SUBCLASSCD       = P1.SUBCLASSCD ";
        $query .= "         AND T1.VIEWCD           = P1.VIEWCD ";
        foreach ($model->semester as $sem => $semname) {
            $query .= "     LEFT JOIN JVIEWSTAT_INPUTSEQ_DAT L".$sem;
            $query .= "          ON T1.YEAR             = L".$sem.".YEAR ";
            $query .= "         AND T1.GRADE            = L".$sem.".GRADE ";
            $query .= "         AND T1.CLASSCD          = L".$sem.".CLASSCD ";
            $query .= "         AND T1.SCHOOL_KIND      = L".$sem.".SCHOOL_KIND ";
            $query .= "         AND T1.CURRICULUM_CD    = L".$sem.".CURRICULUM_CD ";
            $query .= "         AND T1.SUBCLASSCD       = L".$sem.".SUBCLASSCD ";
            $query .= "         AND T1.VIEWCD           = L".$sem.".VIEWCD ";
            $query .= "         AND L".$sem.".SEMESTER  = '".$sem."' ";
        }
        $query .= " WHERE ";
        $query .= "     T1.YEAR     = '".CTRL_YEAR."' AND ";
        $query .= "     T1.GRADE    = '".$model->grade."' AND ";
        $query .= "     T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD = '".$model->subclass."' ";
        $query .= " ORDER BY ";
        $query .= "     T2.SHOWORDER, ";
        $query .= "     T1.VIEWCD ";

        return $query;
    }

    //存在チェック -- JVIEWSTAT_SUBCLASS_PATTERN_DAT
    function checkExistsSubclassPattern($year, $school_kind, $pattern_cd="") {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     JVIEWSTAT_SUBCLASS_PATTERN_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR        = '".$year."' AND ";
        $query .= "     SCHOOL_KIND = '".$school_kind."' ";
        if ($pattern_cd) {
            $query .= " AND PATTERN_CD  = '".$pattern_cd."' ";
        }

        return $query;
    }

    //存在チェック -- 
    function checkExistsGradeYDat($year, $school_kind, $grade) {
        $query  = " SELECT DISTINCT ";
        $query .= "  T1.GRADE ";
        $query .= " FROM ";
        $query .= "  JVIEWNAME_GRADE_YDAT T1 ";
        $query .= "  INNER JOIN JVIEWNAME_GRADE_MST T2 ";
        $query .= "  ON T1.GRADE = T2.GRADE ";
        $query .= "  AND T1.CLASSCD = T2.CLASSCD ";
        $query .= "  AND T1.SCHOOL_KIND = T2.SCHOOL_KIND ";
        $query .= "  AND T1.CURRICULUM_CD = T2.CURRICULUM_CD ";
        $query .= "  AND T1.SUBCLASSCD = T2.SUBCLASSCD ";
        $query .= "  AND T1.VIEWCD = T2.VIEWCD ";
        $query .= " WHERE ";
        $query .= "  T1.YEAR = '".$year."' ";
        $query .= "  AND T1.SCHOOL_KIND = '".$school_kind."' ";
        $query .= "  AND T1.GRADE = '".$grade."' ";

        return $query;
    }

    //更新処理
    function getUpdateQuery(&$model) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //科目コードの分割
        list ($classcd, $school_kind, $curriculum_cd, $subclasscd) = explode('-', $model->subclass);

        //削除対象テーブル
        $tableArray = array("JVIEWSTAT_SUBCLASS_PATTERN_DAT","JVIEWSTAT_INPUTSEQ_DAT");

        foreach ($tableArray as $no => $table) {
            //削除
            $query  = " DELETE FROM ";
            $query .=       $table;
            $query .= " WHERE ";
            $query .= "     YEAR    = '".CTRL_YEAR."' AND ";
            $query .= "     GRADE   = '".$model->grade."' AND ";
            $query .= "     CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD = '".$model->subclass."' ";
            $db->query($query);
        }

        //追加
        foreach ($model->setViewCd as $key => $ViewCd) {
            // JVIEWSTAT_SUBCLASS_PATTERN_DAT
            if (strlen($model->arrPatternCd[$ViewCd])) {
                $data = array();
                $data["YEAR"][TEXT]             = CTRL_YEAR;
                $data["GRADE"][TEXT]            = $model->grade;
                $data["CLASSCD"][TEXT]          = $classcd;
                $data["SCHOOL_KIND"][TEXT]      = $school_kind;
                $data["CURRICULUM_CD"][TEXT]    = $curriculum_cd;
                $data["SUBCLASSCD"][TEXT]       = $subclasscd;
                $data["VIEWCD"][TEXT]           = $ViewCd;
                $data["PATTERN_CD"][TEXT]       = $model->arrPatternCd[$ViewCd];
                $data["REGISTERCD"][TEXT]       = STAFFCD;
                $data["UPDATED"][FUNC]          = "SYSDATE()";

                $db->query(Query::insertSQL($data, "JVIEWSTAT_SUBCLASS_PATTERN_DAT"));
            }

            // JVIEWSTAT_INPUTSEQ_DAT
            foreach ($model->semester as $semester => $semestername) {
                $data = array();
                $data["YEAR"][TEXT]             = CTRL_YEAR;
                $data["CLASSCD"][TEXT]          = $classcd;
                $data["SCHOOL_KIND"][TEXT]      = $school_kind;
                $data["CURRICULUM_CD"][TEXT]    = $curriculum_cd;
                $data["SUBCLASSCD"][TEXT]       = $subclasscd;
                $data["VIEWCD"][TEXT]           = $ViewCd;
                $data["GRADE"][TEXT]            = $model->grade;
                $data["SEMESTER"][TEXT]         = $semester;
                $data["VIEWFLG"][TEXT]          = $model->arrViewFlg[$ViewCd][$semester];
                $data["REGISTERCD"][TEXT]       = STAFFCD;
                $data["UPDATED"][FUNC]          = "SYSDATE()";

                $db->query(Query::insertSQL($data, "JVIEWSTAT_INPUTSEQ_DAT"));
            }
        }

        $db->commit();
        Query::dbCheckIn($db);
        return;
    }

    //削除処理
    function &getDeleteQuery($model, $school_kind) {
        $db = Query::dbCheckOut();

        // JVIEWSTAT_SUBCLASS_PATTERN_DAT
        $query  = " DELETE FROM ";
        $query .= "     JVIEWSTAT_SUBCLASS_PATTERN_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR        = '".CTRL_YEAR."' AND ";
        $query .= "     SCHOOL_KIND = '".$school_kind."' ";
        $db->query($query);

        // JVIEWSTAT_INPUTSEQ_DAT
        $query  = " DELETE FROM ";
        $query .= "     JVIEWSTAT_INPUTSEQ_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR        = '".CTRL_YEAR."' AND ";
        $query .= "     SCHOOL_KIND = '".$school_kind."' ";
        $db->query($query);

        Query::dbCheckIn($db);
        return;
    }

    //コピー用データ(そのまま挿入)
    function getCopySubclassPatternData($model, $year) {
        //where句で前年度を指定して、取得した年度に+1して当年度としてINSERTしている。
        $query  = " INSERT INTO ";
        $query .= "     JVIEWSTAT_SUBCLASS_PATTERN_DAT ";
        $query .= " SELECT ";
        $query .= "     CAST(CAST((int(YEAR)+1) AS CHAR(4)) AS VARCHAR(4)), ";
        $query .= "     GRADE, ";
        $query .= "     CLASSCD, ";
        $query .= "     SCHOOL_KIND, ";
        $query .= "     CURRICULUM_CD, ";
        $query .= "     SUBCLASSCD, ";
        $query .= "     VIEWCD, ";
        $query .= "     PATTERN_CD, ";
        $query .= "     '".STAFFCD."' AS REGISTERCD, ";
        $query .= "     sysdate() ";
        $query .= " FROM ";
        $query .= "     JVIEWSTAT_SUBCLASS_PATTERN_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$year."' ";
        $query .= "     AND SCHOOL_KIND = '".$model->schoolkind."' ";

        return $query;
    }

    //コピー用データ(そのまま挿入)
    function getCopyInputSeqData($model, $year) {
        //where句で前年度を指定して、取得した年度に+1して当年度としてINSERTしている。
        $query  = " INSERT into ";
        $query .= "     JVIEWSTAT_INPUTSEQ_DAT ";
        $query .= " SELECT ";
        $query .= "     CAST(CAST((int(YEAR)+1) AS CHAR(4)) AS VARCHAR(4)), ";
        $query .= "     CLASSCD, ";
        $query .= "     SCHOOL_KIND, ";
        $query .= "     CURRICULUM_CD, ";
        $query .= "     SUBCLASSCD, ";
        $query .= "     VIEWCD, ";
        $query .= "     GRADE, ";
        $query .= "     SEMESTER, ";
        $query .= "     VIEWFLG, ";
        $query .= "     '".STAFFCD."' AS REGISTERCD, ";
        $query .= "     sysdate() ";
        $query .= " FROM ";
        $query .= "     JVIEWSTAT_INPUTSEQ_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$year."' ";
        $query .= "     AND SCHOOL_KIND = '".$model->schoolkind."' ";

        return $query;
    }

    //コピー処理
    function &getCopyQuery($model) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //前年度からコピー(JVIEWSTAT_SUBCLASS_PATTERN_DAT)
        $query = knjz211dQuery::getCopySubclassPatternData($model, (CTRL_YEAR - 1));
        $db->query($query);

        //前年度からコピー(JVIEWSTAT_INPUTSEQ_DAT)
        $query = knjz211dQuery::getCopyInputSeqData($model, (CTRL_YEAR - 1));
        $db->query($query);

        $db->commit();
        Query::dbCheckIn($db);
        return;
    }
}
?>
