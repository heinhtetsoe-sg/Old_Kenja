<?php

require_once('for_php7.php');

class knjz406Query extends Query {

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
        $query .= "     VALUES('2','2:担任・教科担当用') ";
        if ($model->auth == DEF_UPDATABLE) {
            $query .= "     UNION ALL ";
            $query .= "     VALUES('1','1:学校用') ";
        }
        $query .= " ) ";

        $query .= " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     DATA ";
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
    
        $tmpSql = knjz406Query::kyoukaTantouSql();
    
        $query  .= " SELECT DISTINCT ";
        if ($model->field["DATA_DIV"] == "1") {
            $query .= "     REGD_G.GRADE || '-' || '000' AS VALUE, ";
            $query .= "     REGD_G.GRADE_NAME1 AS LABEL ";
        } else {
            $query .= "     REGD_H.GRADE || '-' || REGD_H.HR_CLASS AS VALUE, ";
            $query .= "     REGD_H.HR_NAME AS LABEL ";
        }
        if ($model->field["DATA_DIV"] == "2") {
            $query  .= "    , CASE ";
            $query  .= "      WHEN EXISTS ( ".$tmpSql." ) THEN 1 ELSE 0 END AS KYOUKA_TANTOU_FLG";
        }
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_GDAT REGD_G, ";
        $query .= "     SCHREG_REGD_HDAT REGD_H ";
        $query .= " WHERE ";
        $query .= "     REGD_G.YEAR = REGD_H.YEAR ";
        $query .= "     AND REGD_G.GRADE = REGD_H.GRADE ";
        $query .= "     AND REGD_H.YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND REGD_H.SEMESTER = '".CTRL_SEMESTER."' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= "     AND REGD_G.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
            }
        } elseif ($model->Properties["useSchool_KindField"] == "1") {
            $query .= "     AND REGD_G.SCHOOL_KIND = '" .SCHOOLKIND ."' ";
        }
        if ($model->auth != DEF_UPDATABLE) {
            //担任の場合、担当している年組を絞る
            $query .= "     AND ( ";
            $query .= "             (REGD_H.TR_CD1 = '".STAFFCD."' ";
            $query .= "              OR REGD_H.TR_CD2 = '".STAFFCD."' ";
            $query .= "              OR REGD_H.TR_CD3 = '".STAFFCD."' ";
            $query .= "              OR REGD_H.SUBTR_CD1 = '".STAFFCD."' ";
            $query .= "              OR REGD_H.SUBTR_CD2 = '".STAFFCD."' ";
            $query .= "              OR REGD_H.SUBTR_CD3 = '".STAFFCD."') ";

            $query  .= "        OR ";

            //教科担当の場合、担当している講座に対応する年組を絞る
            $query  .= "      EXISTS ( ".$tmpSql." ) ";
            $query  .= "    ) ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    function kyoukaTantouSql() {
            $query   = "        SELECT ";
            $query  .= "          T1.YEAR ";
            $query  .= "          , T1.SEMESTER ";
            $query  .= "          , T2.STAFFCD ";
            $query  .= "          , T3.TRGTGRADE ";
            $query  .= "          , T3.TRGTCLASS ";
            $query  .= "        FROM ";
            $query  .= "          CHAIR_DAT T1 ";
            $query  .= "          INNER JOIN CHAIR_STF_DAT T2 ";
            $query  .= "            ON T2.YEAR = T1.YEAR ";
            $query  .= "            AND T2.SEMESTER = T1.SEMESTER ";
            $query  .= "            AND T2.CHAIRCD = T1.CHAIRCD ";
            $query  .= "          INNER JOIN CHAIR_CLS_DAT T3 ";
            $query  .= "            ON T3.YEAR = T1.YEAR ";
            $query  .= "            AND T3.SEMESTER = T1.SEMESTER ";
            $query  .= "            AND T3.CHAIRCD = T1.CHAIRCD ";
            $query  .= "            AND T3.GROUPCD = T1.GROUPCD ";
            $query  .= "        WHERE ";
            $query  .= "          T1.YEAR = REGD_H.YEAR ";
            $query  .= "          AND T1.SEMESTER = REGD_H.SEMESTER ";
            $query  .= "          AND T3.TRGTGRADE = REGD_H.GRADE ";
            $query  .= "          AND T3.TRGTCLASS = REGD_H.HR_CLASS ";
            $query  .= "          AND T2.STAFFCD = '".STAFFCD."' ";
            $query  .= "        GROUP BY ";
            $query  .= "          T1.YEAR ";
            $query  .= "          , T1.SEMESTER ";
            $query  .= "          , T2.STAFFCD ";
            $query  .= "          , T3.TRGTGRADE ";
            $query  .= "          , T3.TRGTCLASS ";

            return $query;
    }

    function selectSchoolKind($grade) {
        $query  = " SELECT ";
        $query .= "     REGD_G.SCHOOL_KIND ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_GDAT REGD_G ";
        $query .= " WHERE ";
        $query .= "     REGD_G.YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND REGD_G.GRADE = '{$grade}' ";

        return $query;
    }

    /**
     ** 教科名
     **/
    function selectSubclasscd($model) {
        list($grade, $hrClass) = explode("-", $model->field["GRADE_HR_CLASS"]);

        $query  = " SELECT DISTINCT ";
        if ($model->Properties["useCurriculumcd"] == "1") {
            $query .= "     JGY.CLASSCD || '-' || JGY.SCHOOL_KIND || '-' || JGY.CURRICULUM_CD || '-' || JGY.SUBCLASSCD AS VALUE, ";
        } else {
            $query .= "     JGY.SUBCLASSCD AS VALUE, ";
        }
        $query .= "     SM.SUBCLASSNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     JVIEWNAME_GRADE_YDAT JGY ";
        $query .= "     INNER JOIN JVIEWNAME_GRADE_MST JGM ";
        $query .= "              ON JGM.GRADE = JGY.GRADE ";
        $query .= "             AND JGM.SUBCLASSCD = JGY.SUBCLASSCD ";
        if ($model->Properties["useCurriculumcd"] == "1") {
            $query .= "             AND JGM.CLASSCD = JGY.CLASSCD ";
            $query .= "             AND JGM.SCHOOL_KIND = JGY.SCHOOL_KIND ";
            $query .= "             AND JGM.CURRICULUM_CD = JGY.CURRICULUM_CD ";
        }
        $query .= "     INNER JOIN SUBCLASS_MST SM ";
        $query .= "              ON SM.SUBCLASSCD = JGY.SUBCLASSCD ";
        if ($model->Properties["useCurriculumcd"] == "1") {
            $query .= "             AND SM.CLASSCD = JGY.CLASSCD ";
            $query .= "             AND SM.SCHOOL_KIND = JGY.SCHOOL_KIND ";
            $query .= "             AND SM.CURRICULUM_CD = JGY.CURRICULUM_CD ";
        }

        if (in_array($model->field["GRADE_HR_CLASS"], $model->kyoukaTantouList)) {
            $query .= "     INNER JOIN CHAIR_DAT CD ";
            $query .= "              ON CD.YEAR       = JGY.YEAR ";
            $query .= "             AND CD.SEMESTER   = '".CTRL_SEMESTER."' ";
            $query .= "             AND CD.SUBCLASSCD = JGY.SUBCLASSCD ";
            if ($model->Properties["useCurriculumcd"] == "1") {
                $query .= "             AND CD.CLASSCD = JGY.CLASSCD ";
                $query .= "             AND CD.SCHOOL_KIND = JGY.SCHOOL_KIND ";
                $query .= "             AND CD.CURRICULUM_CD = JGY.CURRICULUM_CD ";
            }
            $query .= "     INNER JOIN CHAIR_CLS_DAT CCD ";
            $query .= "              ON CCD.YEAR     = CD.YEAR ";
            $query .= "             AND CCD.SEMESTER = CD.SEMESTER ";
            $query .= "             AND CCD.CHAIRCD =  CD.CHAIRCD ";
            $query .= "             AND CCD.GROUPCD =  CD.GROUPCD ";
            $query .= "             AND CCD.TRGTGRADE = '".$grade."' ";
            $query .= "             AND CCD.TRGTCLASS = '".$hrClass."' ";
            $query  .= "    INNER JOIN CHAIR_STF_DAT CSD ";
            $query  .= "      ON CSD.YEAR = CD.YEAR ";
            $query  .= "      AND CSD.SEMESTER = CD.SEMESTER ";
            $query  .= "      AND CSD.CHAIRCD = CD.CHAIRCD ";
            $query  .= "      AND CSD.STAFFCD = '".STAFFCD."' ";
        }

        $query .= " WHERE ";
        $query .= "     JGY.YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND JGY.GRADE = '{$grade}' ";
        if ($model->Properties["useCurriculumcd"] == "1") {
            $query .= "     AND JGY.CLASSCD <= '91' ";//教科コード'91'以下
        } else {
            $query .= "     AND SUBSTR(JGY.SUBCLASSCD, 1, 2) <= '91' ";//教科コード'91'以下
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    function getSemester($date) {
        $db = Query::dbCheckOut();
        $date = str_replace("/", "-", $date);
        $query  = " SELECT ";
        $query .= "     SEMESTER ";
        $query .= " FROM ";
        $query .= "     SEMESTER_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND SEMESTER <> '9' ";
        $query .= "     AND '{$date}' BETWEEN SDATE AND EDATE ";
        $retSeme = $db->getOne($query);
        Query::dbCheckIn($db);

        return $retSeme;
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
     ** 単元データ（学校用・担任用）
     **     ②初期値：単元（学校用）を読込
     **     ③通常：単元（学校用・担任用）を読込
     **/
    function getListUnitDat($model) {
        list($grade, $hrClass) = explode("-", $model->field["GRADE_HR_CLASS"]);

        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     UNIT_TEST_DAT ";//変更前：UNIT_DAT
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND GRADE = '{$grade}' ";
        if ($model->Properties["useCurriculumcd"] == "1") {
            $query .= "     AND CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD = '{$model->field["SUBCLASSCD"]}' ";
        } else {
            $query .= "     AND SUBCLASSCD = '{$model->field["SUBCLASSCD"]}' ";
        }
        //初期値：単元（学校用）を読込
        if ($model->cmd == "def" && $model->field["DATA_DIV"] == "2") {
            $query .= "     AND DATA_DIV = '1' "; //1:学校用 2:担任用
            $query .= "     AND HR_CLASS = '000' "; //学校用の場合は、'000'
        //通常：単元（学校用・担任用）を読込
        } else {
            $query .= "     AND DATA_DIV = '{$model->field["DATA_DIV"]}' "; //1:学校用 2:担任用
            $query .= "     AND HR_CLASS = '{$hrClass}' "; //学校用の場合は、'000'
        }
        $query .= " ORDER BY ";
        $query .= "     SORT ";//変更前：SEQ

        return $query;
    }

    /**
     ** UNIT_TEST_INPUTSEQ_DATがあるか・・・初期値ボタンのメッセージ用
     **/
    function getInputseqDataCnt($model) {
        list($grade, $hrClass) = explode("-", $model->field["GRADE_HR_CLASS"]);

        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     UNIT_TEST_INPUTSEQ_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND GRADE = '{$grade}' ";
        $query .= "     AND HR_CLASS = '{$hrClass}' ";
        if ($model->Properties["useCurriculumcd"] == "1") {
            $query .= "     AND CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD = '{$model->field["SUBCLASSCD"]}' ";
        } else {
            $query .= "     AND SUBCLASSCD = '{$model->field["SUBCLASSCD"]}' ";
        }

        return $query;
    }

    /**
     ** 単元データの更新
     **/
    function getUpdUnitDat($db, $model) {
        //変数
        list($grade, $hrClass) = explode("-", $model->field["GRADE_HR_CLASS"]);
        list($classcd, $school_kind, $curriculum_cd, $subclasscd) = explode("-", $model->field["SUBCLASSCD"]);

        //UNIT_TEST_DATからMAXのSEQを取得
        $maxSeq = $db->getOne(knjz406Query::getMaxUnitDat($model));
        if (!strlen($maxSeq)) $maxSeq = 0;

        //UNIT_TEST_DAT削除
        $query = knjz406Query::getDelUnitDat($model);
        $db->query($query);

        //UNIT_TEST_DAT追加
        foreach ($model->updUnitDatArray as $key => $setArray) {
            $data = array();
            //キー
            $data["YEAR"][TEXT]             = CTRL_YEAR;
            $data["DATA_DIV"][TEXT]         = $model->field["DATA_DIV"];
            $data["GRADE"][TEXT]            = $grade;
            $data["HR_CLASS"][TEXT]         = $hrClass;
            if ($model->Properties["useCurriculumcd"] == "1") {
                $data["CLASSCD"][TEXT]          = $classcd;
                $data["SCHOOL_KIND"][TEXT]      = $school_kind;
                $data["CURRICULUM_CD"][TEXT]    = $curriculum_cd;
                $data["SUBCLASSCD"][TEXT]       = $subclasscd;
            } else {
                $data["SUBCLASSCD"][TEXT]       = $model->field["SUBCLASSCD"];
            }
            //SEQ・・・レコードがない時、MAXのSEQ+1をセット
            $data["SEQ"][NUMBER]            = !strlen($setArray["SEQ"]) ? ++$maxSeq : $setArray["SEQ"];
            //並びNo
            $data["SORT"][NUMBER]           = $key + 1;
            //入力データ
            $data["UNIT_L_NAME"][TEXT]      = $setArray["UNIT_L_NAME"];
            $data["UNIT_TEST_DATE"][TEXT]   = str_replace("/", "-", $setArray["UNIT_TEST_DATE"]);
            //表示データ
            $data["SEMESTER"][TEXT]         = $setArray["SEMESTER"];
            $data["REGISTERCD"][TEXT]       = STAFFCD ;
            $data["UPDATED"][FUNC]          = "sysdate()";

            //レコード追加
            $query = Query::insertSQL($data, "UNIT_TEST_DAT");
            $db->query($query);
        }

        //UNIT_TEST_INPUTSEQ_DAT削除・・・担任用の場合のみ
        if ($model->field["DATA_DIV"] == "2") {
            $query = knjz406Query::getDelUnitTestInputseqDat($model);
            $db->query($query);
        }

        return true;
    }

    /**
     ** 単元データのMAXのSEQを取得
     **/
    function getMaxUnitDat($model) {
        list($grade, $hrClass) = explode("-", $model->field["GRADE_HR_CLASS"]);

        $query  = " SELECT ";
        $query .= "     MAX(SEQ) AS MAX_SEQ ";
        $query .= " FROM ";
        $query .= "     UNIT_TEST_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND DATA_DIV = '{$model->field["DATA_DIV"]}' ";
        $query .= "     AND GRADE = '{$grade}' ";
        $query .= "     AND HR_CLASS = '{$hrClass}' ";
        if ($model->Properties["useCurriculumcd"] == "1") {
            $query .= "     AND CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD = '{$model->field["SUBCLASSCD"]}' ";
        } else {
            $query .= "     AND SUBCLASSCD = '{$model->field["SUBCLASSCD"]}' ";
        }

        return $query;
    }

    /**
     ** 単元データの削除
     **/
    function getDelUnitDat($model) {
        list($grade, $hrClass) = explode("-", $model->field["GRADE_HR_CLASS"]);

        $query  = " DELETE ";
        $query .= " FROM ";
        $query .= "     UNIT_TEST_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND DATA_DIV = '{$model->field["DATA_DIV"]}' "; //1:学校用 2:担任用
        $query .= "     AND GRADE = '{$grade}' ";
        $query .= "     AND HR_CLASS = '{$hrClass}' "; //学校用の場合は、'000'
        if ($model->Properties["useCurriculumcd"] == "1") {
            $query .= "     AND CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD = '{$model->field["SUBCLASSCD"]}' ";
        } else {
            $query .= "     AND SUBCLASSCD = '{$model->field["SUBCLASSCD"]}' ";
        }

        return $query;
    }

    /**
     ** UNIT_TEST_INPUTSEQ_DATの削除・・・担任用の場合のみ
     **/
    function getDelUnitTestInputseqDat($model) {
        list($grade, $hrClass) = explode("-", $model->field["GRADE_HR_CLASS"]);

        $query  = " DELETE ";
        $query .= " FROM ";
        $query .= "     UNIT_TEST_INPUTSEQ_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND GRADE = '{$grade}' ";
        $query .= "     AND HR_CLASS = '{$hrClass}' ";
        if ($model->Properties["useCurriculumcd"] == "1") {
            $query .= "     AND CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD = '{$model->field["SUBCLASSCD"]}' ";
        } else {
            $query .= "     AND SUBCLASSCD = '{$model->field["SUBCLASSCD"]}' ";
        }
        $query .= "     AND SEQ NOT IN ( ";
        $query .= "         SELECT ";
        $query .= "             SEQ ";
        $query .= "         FROM ";
        $query .= "             UNIT_TEST_DAT ";
        $query .= "         WHERE ";
        $query .= "             YEAR = '".CTRL_YEAR."' ";
        $query .= "             AND DATA_DIV = '{$model->field["DATA_DIV"]}' ";
        $query .= "             AND GRADE = '{$grade}' ";
        $query .= "             AND HR_CLASS = '{$hrClass}' ";
        if ($model->Properties["useCurriculumcd"] == "1") {
            $query .= "             AND CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD = '{$model->field["SUBCLASSCD"]}' ";
        } else {
            $query .= "             AND SUBCLASSCD = '{$model->field["SUBCLASSCD"]}' ";
        }
        $query .= "     ) ";

        return $query;
    }

}
?>
