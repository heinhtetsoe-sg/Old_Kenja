<?php

require_once('for_php7.php');

class knjs010Query extends Query {

    /**********/
    /* コンボ */
    /**********/

    /**
     ** データ区分
     **     権限「更新可能」の場合のみ、学校用を表示する。
     **     担任用を１番目に表示する。
     **/
    function selectDataDiv($model) {
        $query  = " WITH DATA(VALUE, LABEL) AS ( ";
        $query .= "     VALUES('2','2:担任用') ";
        if ($model->auth == DEF_UPDATABLE) {
            $query .= "     UNION ALL ";
            $query .= "     VALUES('1','1:学校用') ";
        }
        $query .= " ) ";

        $query .= " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     DATA ";
        //KNJS030からコールされた時、渡されたパラメータの値のみとする。
        if ($model->getPrgId) {
            $query .= " WHERE ";
            $query .= "     VALUE = '{$model->field["DATA_DIV"]}' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE DESC ";

        return $query;
    }

    /**
     ** 学年 or 年組
     **     「1:学校用」は学年、「2:担任用」は年組を表示する。
     **     「1:学校用」の場合のHR_CLASSは、'000'とする。
     **/
    function selectGradeHrClass($model) {
        $query  = " SELECT DISTINCT ";
        if ($model->field["DATA_DIV"] == "1") {
            $query .= "     REGD_G.GRADE || '-' || '000' AS VALUE, ";
            $query .= "     REGD_G.GRADE_NAME1 AS LABEL ";
        } else {
            $query .= "     REGD_H.GRADE || '-' || REGD_H.HR_CLASS AS VALUE, ";
            $query .= "     REGD_H.HR_NAME AS LABEL ";
            //$query .= "     REGD_H.GRADE || '-' || REGD_H.HR_CLASS  || ':' || REGD_H.HR_NAME AS LABEL ";
        }
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_GDAT REGD_G, ";
        $query .= "     SCHREG_REGD_HDAT REGD_H ";
        $query .= " WHERE ";
        $query .= "     REGD_G.YEAR = REGD_H.YEAR ";
        $query .= "     AND REGD_G.GRADE = REGD_H.GRADE ";
        //$query .= "     AND REGD_G.SCHOOL_KIND = 'P' "; //TODO:小学校のみとするか？
        $query .= "     AND REGD_H.YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND REGD_H.SEMESTER = '".CTRL_SEMESTER."' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= " AND REGD_G.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
            }
        } elseif ($model->Properties["useSchool_KindField"] == "1") {
            $query .= "     AND REGD_G.SCHOOL_KIND = '" .SCHOOLKIND ."' ";
        }
        if ($model->auth != DEF_UPDATABLE) {
            $query .= "     AND (REGD_H.TR_CD1 = '".STAFFCD."' ";
            $query .= "          OR REGD_H.TR_CD2 = '".STAFFCD."' ";
            $query .= "          OR REGD_H.TR_CD3 = '".STAFFCD."' ";
            $query .= "          OR REGD_H.SUBTR_CD1 = '".STAFFCD."' ";
            $query .= "          OR REGD_H.SUBTR_CD2 = '".STAFFCD."' ";
            $query .= "          OR REGD_H.SUBTR_CD3 = '".STAFFCD."') ";
        }
        //KNJS030からコールされた時、渡されたパラメータの値のみとする。
        if ($model->getPrgId) {
            $query .= "     AND REGD_H.GRADE || '-' || REGD_H.HR_CLASS = '{$model->field["GRADE_HR_CLASS"]}' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    /**
     ** 教科名
     **/
    function selectSubclasscd($model) {
        list($grade, $hrClass) = explode("-", $model->field["GRADE_HR_CLASS"]);
        $query  = " SELECT ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "    UNITB.CLASSCD || '-' || ";
            $query .= "    UNITB.SCHOOL_KIND || '-' || ";
            $query .= "    UNITB.CURRICULUM_CD || '-' || ";
        }
        $query .= "     UNITB.SUBCLASSCD AS VALUE, ";
        $query .= "     SUBD.SUBCLASSNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     UNIT_STUDY_TEXT_BOOK_DAT UNITB ";
        $query .= "     INNER JOIN SUBCLASS_MST SUBD ON SUBD.SUBCLASSCD = UNITB.SUBCLASSCD ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "                             AND SUBD.CLASSCD = UNITB.CLASSCD ";
            $query .= "                             AND SUBD.SCHOOL_KIND = UNITB.SCHOOL_KIND ";
            $query .= "                             AND SUBD.CURRICULUM_CD = UNITB.CURRICULUM_CD ";
        }
        $query .= " WHERE ";
        $query .= "     UNITB.YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND UNITB.GRADE = '{$grade}' ";
        //KNJS030からコールされた時、渡されたパラメータの値のみとする。
        if ($model->getPrgId) {
            $query .= "     AND ";
            //教育課程対応
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "     UNITB.CLASSCD || '-' || ";
                $query .= "     UNITB.SCHOOL_KIND || '-' || ";
                $query .= "     UNITB.CURRICULUM_CD || '-' || ";
            }
            $query .= "         UNITB.SUBCLASSCD = '{$model->field["SUBCLASSCD"]}' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    /****************/
    /* 表示(ヘッダ) */
    /****************/

    /**
     ** 担任名
     **     「1:学校用(学年)」はログイン職員、「2:担任用(年組)」は正担任１を表示する。
     **/
    function getStaffName($model) {
        if ($model->field["DATA_DIV"] == "1") {
            $query  = " SELECT ";
            $query .= "     STFD.STAFFCD, ";
            $query .= "     STFD.STAFFNAME ";
            $query .= " FROM ";
            $query .= "     STAFF_MST STFD ";
            $query .= " WHERE ";
            $query .= "     STFD.STAFFCD = '".STAFFCD."' ";
        } else {
            $query  = " SELECT ";
            $query .= "     STFD.STAFFCD, ";
            $query .= "     STFD.STAFFNAME ";
            $query .= " FROM ";
            $query .= "     SCHREG_REGD_HDAT REGD ";
            $query .= "     LEFT JOIN STAFF_MST STFD ON STFD.STAFFCD = REGD.TR_CD1 ";
            $query .= " WHERE ";
            $query .= "     YEAR = '".CTRL_YEAR."' ";
            $query .= "     AND SEMESTER = '".CTRL_SEMESTER."' ";
            $query .= "     AND REGD.GRADE || '-' || REGD.HR_CLASS = '{$model->field["GRADE_HR_CLASS"]}' ";
        }

        return $query;
    }

    /**
     ** 出版社
     **/
    function getIssuecompanyName($model) {
        list($grade, $hrClass) = explode("-", $model->field["GRADE_HR_CLASS"]);
        $query  = " SELECT ";
        $query .= "     UNITB.ISSUECOMPANYCD, ";
        $query .= "     ISSD.ISSUECOMPANYNAME, ";
        $query .= "     ISSD.ISSUECOMPANYABBV ";
        $query .= " FROM ";
        $query .= "     UNIT_STUDY_TEXT_BOOK_DAT UNITB ";
        $query .= "     LEFT JOIN ISSUECOMPANY_MST ISSD ON ISSD.ISSUECOMPANYCD = UNITB.ISSUECOMPANYCD ";
        $query .= " WHERE ";
        $query .= "     UNITB.YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND UNITB.GRADE = '{$grade}' ";
        $query .= "     AND ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     UNITB.CLASSCD || '-' || ";
            $query .= "     UNITB.SCHOOL_KIND || '-' || ";
            $query .= "     UNITB.CURRICULUM_CD || '-' || ";
        }
        $query .= "         UNITB.SUBCLASSCD = '{$model->field["SUBCLASSCD"]}' ";

        return $query;
    }

    /**
     ** 時間割ランクデータ(マスタおよび学校用)
     **     ・学期マスタから学期を取得。
     **     ・「１学期１０月、２学期１０月」のような場合、１０月の学期はブランクとする。
     **/
    function getAllotmentSemester($allotmentMonth) {
        $year = (int)$allotmentMonth < 4 ? CTRL_YEAR + 1 : CTRL_YEAR;
        $yearMonth = $year .sprintf("%02d", $allotmentMonth);

        $query  = " WITH MONTH_CNT AS ( ";
        $query .= " SELECT ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     SEMESTER_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND SEMESTER < '9' ";
        $query .= "     AND '{$yearMonth}' BETWEEN CAST(YEAR(SDATE) AS CHAR(4)) || CASE WHEN MONTH(SDATE) < 10 THEN '0' || CAST(MONTH(SDATE) AS CHAR(1)) ELSE CAST(MONTH(SDATE) AS CHAR(2)) END ";
        $query .= "                            AND CAST(YEAR(EDATE) AS CHAR(4)) || CASE WHEN MONTH(EDATE) < 10 THEN '0' || CAST(MONTH(EDATE) AS CHAR(1)) ELSE CAST(MONTH(EDATE) AS CHAR(2)) END ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     SEMESTER ";
        $query .= " FROM ";
        $query .= "     SEMESTER_MST ";
        $query .= "     INNER JOIN MONTH_CNT ON CNT = 1 ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND SEMESTER < '9' ";
        $query .= "     AND '{$yearMonth}' BETWEEN CAST(YEAR(SDATE) AS CHAR(4)) || CASE WHEN MONTH(SDATE) < 10 THEN '0' || CAST(MONTH(SDATE) AS CHAR(1)) ELSE CAST(MONTH(SDATE) AS CHAR(2)) END ";
        $query .= "                            AND CAST(YEAR(EDATE) AS CHAR(4)) || CASE WHEN MONTH(EDATE) < 10 THEN '0' || CAST(MONTH(EDATE) AS CHAR(1)) ELSE CAST(MONTH(EDATE) AS CHAR(2)) END ";

        return $query;
    }

    /**
     ** 時間割ランクデータ(担任用)
     **     ・UNIT_SCH_CHR_RANK_DATから学期と月を取得(ランク順にソート)。
     **     ・科目コードと講座コードは、１対１の関係であることが前提。
     **/
    function getListUnitRankDat($model) {
        list($grade, $hrClass) = explode("-", $model->field["GRADE_HR_CLASS"]);

        $query  = " WITH T_CHAIR AS ( ";
        $query .= "     SELECT DISTINCT ";
        $query .= "         W1.YEAR, ";
        $query .= "         W1.SEMESTER, ";
        $query .= "         W1.CHAIRCD, ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     W1.CLASSCD || '-' || ";
            $query .= "     W1.SCHOOL_KIND || '-' || ";
            $query .= "     W1.CURRICULUM_CD || '-' || ";
        }
        $query .= "         W1.SUBCLASSCD AS SUBCLASSCD, ";
        $query .= "         W2.APPDATE, ";
        $query .= "         W2.APPENDDATE ";
        $query .= "     FROM ";
        $query .= "         CHAIR_DAT W1 ";
        $query .= "         INNER JOIN CHAIR_STD_DAT W2 ";
        $query .= "             ON  W2.YEAR     = W1.YEAR ";
        $query .= "             AND W2.SEMESTER = W1.SEMESTER ";
        $query .= "             AND W2.CHAIRCD  = W1.CHAIRCD ";
        $query .= "         INNER JOIN SCHREG_REGD_DAT W3 ";
        $query .= "             ON  W3.YEAR     = W2.YEAR ";
        $query .= "             AND W3.SEMESTER = W2.SEMESTER ";
        $query .= "             AND W3.SCHREGNO = W2.SCHREGNO ";
        $query .= "     WHERE ";
        $query .= "             W1.YEAR     = '".CTRL_YEAR."' ";
        $query .= "         AND ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "         W1.CLASSCD || '-' || ";
            $query .= "         W1.SCHOOL_KIND || '-' || ";
            $query .= "         W1.CURRICULUM_CD || '-' || ";
        }
        $query .= "             W1.SUBCLASSCD = '{$model->field["SUBCLASSCD"]}' ";
        $query .= "         AND W3.GRADE    = '{$grade}' ";
        $query .= "         AND W3.HR_CLASS = '{$hrClass}' ";
        $query .= "     ) ";

        $query .= " SELECT ";
        $query .= "     T1.EXECUTEDATE, ";
        $query .= "     MONTH(T1.EXECUTEDATE) AS MONTH, ";
        $query .= "     T1.SEMESTER, ";
        $query .= "     L2.SEMESTERNAME, ";
        $query .= "     T1.RANK, ";
        $query .= "     T1.CHAIRCD, ";
        $query .= "     L1.SUBCLASSCD ";
        $query .= " FROM ";
        $query .= "     UNIT_SCH_CHR_RANK_DAT T1 ";
        $query .= "     INNER JOIN T_CHAIR L1 ";
        $query .= "         ON  L1.YEAR = T1.YEAR ";
        $query .= "         AND L1.SEMESTER = T1.SEMESTER ";
        $query .= "         AND L1.CHAIRCD = T1.CHAIRCD ";
        $query .= "         AND T1.EXECUTEDATE BETWEEN L1.APPDATE AND L1.APPENDDATE ";
        $query .= "     LEFT JOIN SEMESTER_MST L2 ";
        $query .= "         ON  L2.YEAR = T1.YEAR ";
        $query .= "         AND L2.SEMESTER = T1.SEMESTER ";
        $query .= " ORDER BY ";
        $query .= "     T1.RANK ";

        return $query;
    }

    /**
     ** 単元データ（学校用・担任用）
     **     ①初期値：単元マスタを読込
     **     ②初期値：単元（学校用）を読込
     **     ③通常：単元（学校用・担任用）を読込
     **/
    function getListUnitDat($model) {
        list($grade, $hrClass) = explode("-", $model->field["GRADE_HR_CLASS"]);
        $unitTable = ($model->cmd == "def" && $model->field["DATA_DIV"] == "1") ? "UNIT_MST" : "UNIT_DAT";

        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     {$unitTable} ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND GRADE = '{$grade}' ";
        $query .= "     AND ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     CLASSCD || '-' || ";
            $query .= "     SCHOOL_KIND || '-' || ";
            $query .= "     CURRICULUM_CD || '-' || ";
        }
        $query .= "         SUBCLASSCD = '{$model->field["SUBCLASSCD"]}' ";
        $query .= "     AND ISSUECOMPANYCD = '{$model->field["ISSUECOMPANYCD"]}' "; //TODO:この条件は必要か？
        //初期値：単元マスタを読込
        if ($model->cmd == "def" && $model->field["DATA_DIV"] == "1") {
        //初期値：単元（学校用）を読込
        } else if ($model->cmd == "def" && $model->field["DATA_DIV"] == "2") {
            $query .= "     AND DATA_DIV = '1' "; //1:学校用 2:担任用
            $query .= "     AND HR_CLASS = '000' "; //学校用の場合は、'000'
        //通常：単元（学校用・担任用）を読込
        } else {
            $query .= "     AND DATA_DIV = '{$model->field["DATA_DIV"]}' "; //1:学校用 2:担任用
            $query .= "     AND HR_CLASS = '{$hrClass}' "; //学校用の場合は、'000'
        }
        $query .= " ORDER BY ";
        $query .= "     SEQ ";

        return $query;
    }

    /**
     ** 単元データの更新
     **/
    function getUpdUnitDat($db, $model) {
        //変数
        list($grade, $hrClass) = explode("-", $model->field["GRADE_HR_CLASS"]);
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            list($classcd, $school_kind, $curriculum_cd, $subclasscd) = explode("-", $model->field["SUBCLASSCD"]);
        }
        //削除
        $query = knjs010Query::getDelUnitDat($model);
        $db->query($query);
        //追加
        foreach ($model->updUnitDatArray as $key => $setArray) {
            $data = array();
            //キー
            $data["YEAR"][TEXT]             = CTRL_YEAR;
            $data["DATA_DIV"][TEXT]         = $model->field["DATA_DIV"];
            $data["GRADE"][TEXT]            = $grade;
            $data["HR_CLASS"][TEXT]         = $hrClass;
            //教育課程対応
            if ($model->Properties["useCurriculumcd"] == '1') {
                $data["CLASSCD"][TEXT]          = $classcd;
                $data["SCHOOL_KIND"][TEXT]      = $school_kind;
                $data["CURRICULUM_CD"][TEXT]    = $curriculum_cd;
                $data["SUBCLASSCD"][TEXT]       = $subclasscd;
            } else {
                $data["SUBCLASSCD"][TEXT]       = $model->field["SUBCLASSCD"];
            }
            $data["ISSUECOMPANYCD"][TEXT]   = $model->field["ISSUECOMPANYCD"]; //TODO:これは必要か？
            $data["REGISTERCD"][TEXT]       = STAFFCD ;
            $data["UPDATED"][FUNC]          = "sysdate()";
            //並びNo
            $data["SEQ"][NUMBER]            = $key + 1;
            //入力データ
            //DB(保持データ)
            foreach ($setArray as $name => $value) {
                if (in_array($name,array("SEQ"))) {
                } else if (in_array($name,array("ALLOTMENT_MONTH"))) {
                    $data[$name][TEXT]      = strlen($value) ? sprintf("%02d",$value) : "";
                } else if (in_array($name,array("L_TITOL"))) {
                    $data[$name][NUMBER]    = $value;
                } else {
                    $data[$name][TEXT]      = $value;
                }
            }

            $query = Query::insertSQL($data, "UNIT_DAT");
            $db->query($query);
        }

        return true;
    }

    /**
     ** 単元データの削除
     **/
    function getDelUnitDat($model) {
        list($grade, $hrClass) = explode("-", $model->field["GRADE_HR_CLASS"]);

        $query  = " DELETE ";
        $query .= " FROM ";
        $query .= "     UNIT_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND DATA_DIV = '{$model->field["DATA_DIV"]}' "; //1:学校用 2:担任用
        $query .= "     AND GRADE = '{$grade}' ";
        $query .= "     AND HR_CLASS = '{$hrClass}' "; //学校用の場合は、'000'
        $query .= "     AND ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     CLASSCD || '-' || ";
            $query .= "     SCHOOL_KIND || '-' || ";
            $query .= "     CURRICULUM_CD || '-' || ";
        }
        $query .= "         SUBCLASSCD = '{$model->field["SUBCLASSCD"]}' ";
        $query .= "     AND ISSUECOMPANYCD = '{$model->field["ISSUECOMPANYCD"]}' "; //TODO:この条件は必要か？

        return $query;
    }

}
?>
