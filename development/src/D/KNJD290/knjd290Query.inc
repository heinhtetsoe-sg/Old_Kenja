<?php

require_once('for_php7.php');

class knjd290Query extends Query {
    //所属取得
    function getSelectGrade($model) {
        if ($model->restrict){
            //参照・更新可（制限付き）
            $query  = " SELECT DISTINCT ";
            $query .= "     I1.GRADE AS VALUE, ";
            $query .= "     I1.GRADE_NAME1 AS LABEL ";
            $query .= " FROM ";
            $query .= "     SCHREG_REGD_HDAT T1 ";
            $query .= "     INNER JOIN SCHREG_REGD_GDAT I1 ON T1.YEAR = I1.YEAR ";
            $query .= "           AND T1.GRADE = I1.GRADE ";
            $query .= " WHERE ";
            $query .= "     T1.YEAR = '".CTRL_YEAR."' ";
            $query .= "     AND T1.SEMESTER = '".CTRL_SEMESTER ."' ";
            $query .= "     AND (T1.TR_CD1 = '".STAFFCD."' OR ";
            $query .= "          T1.TR_CD2 = '".STAFFCD."' OR ";
            $query .= "          T1.TR_CD3 = '".STAFFCD."') ";
            $query .= " ORDER BY ";
            $query .= "     VALUE ";
        } else {
            //参照・更新可
            $query  = " SELECT DISTINCT ";
            $query .= "     I1.GRADE AS VALUE, ";
            $query .= "     I1.GRADE_NAME1 AS LABEL ";
            $query .= " FROM ";
            $query .= "     SCHREG_REGD_HDAT T1 ";
            $query .= "     INNER JOIN SCHREG_REGD_GDAT I1 ON T1.YEAR = I1.YEAR ";
            $query .= "           AND T1.GRADE = I1.GRADE ";
            $query .= " WHERE ";
            $query .= "     T1.YEAR = '".CTRL_YEAR."' ";
            $query .= "     AND T1.SEMESTER = '".CTRL_SEMESTER ."' ";
            $query .= " ORDER BY ";
            $query .= "     VALUE";
        }
        return $query;
    }

    //名称マスタ取得
    function getNameMst($year, $namecd1, $namecd2)
    {
        $query  = " SELECT ";
        $query .= "     NAME1 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '" . $year . "' ";
        $query .= "     AND NAMECD1 = '" . $namecd1 . "' ";
        $query .= "     AND NAMECD2 = '" . $namecd2 . "' ";
        
        return $query;
    }

    //学校マスタ取得
    function getSchoolMst()
    {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     SCHOOL_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";

        return $query;
    }

    //学校マスタの校種有無チェック
    function checkSchoolMst() {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     SYSIBM.SYSCOLUMNS ";
        $query .= " WHERE ";
        $query .= "     TBNAME  = 'SCHOOL_MST' AND ";
        $query .= "     NAME    = 'SCHOOL_KIND' ";

        return $query;
    }

    //単位マスタ取得
    function getCreditMst()
    {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     CREDIT_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";

        return $query;
    }

    //学習記録データ取得
    function getStudyRec($schregno)
    {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     SCHREG_STUDYREC_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR < '".CTRL_YEAR."' ";
        $query .= "     AND SCHREGNO = '".$schregno."' ";
        $query .= "     AND VALUE(GET_CREDIT, 0) + VALUE(ADD_CREDIT, 0) > 0 ";

        return $query;
    }

    //更新対象となる生徒の情報を取得
    function getStudent($model, $grade) {
        $semester = CTRL_SEMESTER;
        $year = CTRL_YEAR;

        $query  = " SELECT ";
        $query .= "     T1.YEAR, ";
        $query .= "     T1.GRADE, ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     T1.COURSECD, ";
        $query .= "     T1.MAJORCD, ";
        $query .= "     T1.COURSECODE, ";
        $query .= "     T1.COURSECD || T1.MAJORCD || T1.GRADE || T1.COURSECODE AS KEY ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1 ";
        $query .= " WHERE ";
        $query .= "         T1.YEAR     = '{$year}' ";
        $query .= "     AND T1.SEMESTER = '{$semester}' ";
        $query .= "     AND T1.GRADE    = '{$grade}' ";

        return $query;
   }

    //必履修パターンの取得
    function getHituRisyu($model) {
        $query  = " SELECT ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     SCHOOL_KIND, ";
        }
        $query .= "     CLASSCD, ";
        $query .= "     SUBCLASSCD, ";
        $query .= "     COURSECD, ";
        $query .= "     MAJORCD, ";
        $query .= "     SEQ ";
        $query .= " FROM ";
        $query .= "     SUBCLASS_REQUIRED_STUDY_DAT ";
        $query .= " ORDER BY ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     SCHOOL_KIND, ";
        }
        $query .= "     COURSECD, ";
        $query .= "     MAJORCD, ";
        $query .= "     CLASSCD, ";
        $query .= "     INT(SEQ), ";
        $query .= "     SUBCLASSCD ";

        return $query;
    }

    //必履修パターンの取得
    function getStandardCredit($model) {
        $query  = " SELECT ";
        $query .= "     CLASSCD, ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     SCHOOL_KIND, ";
        }
        $query .= "     SUBCLASSCD, ";
        $query .= "     VALUE(CREDITS, 0) AS CREDITS ";
        $query .= " FROM ";
        $query .= "     STANDARD_CREDIT_MST ";

        return $query;
    }

    /**
     * DELETE
     */
    function getDeleteQuery($schregno) {
        $query  = " DELETE FROM CLASS_REQUIRED_DAT ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO = '".$schregno."' ";

        return $query;
    }

    /**
     * INSERT
     */
    function getInsertQuery($schregno, $setData, $model) {
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            list($classcd, $errFlg, $school_kind, $courseCd, $majorCd, $setSeq) = preg_split("/:/", $setData);
        } else {
            list($classcd, $errFlg, $courseCd, $majorCd, $setSeq) = preg_split("/:/", $setData);
        }
        $data = array();

        $data["SCHREGNO"][TEXT]     = $schregno;
        $data["CLASSCD"][TEXT]      = $classcd;
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $data["SCHOOL_KIND"][TEXT]    = $school_kind;
        }
        $data["ERR_FLG"][TEXT]      = $errFlg;
        if ($errFlg == "0") {
            $data["COURSECD"][TEXT]         = $courseCd;
            $data["MAJORCD"][TEXT]          = $majorCd;
            $data["SEQ"][TEXT]              = $setSeq;
        }
        $data["REGISTERCD"][TEXT]   = STAFFCD;
        $data["UPDATED"][FUNC]      = "sysdate()";

        $query = Query::insertSQL($data, "CLASS_REQUIRED_DAT");
        return $query;
    }

    /**
     * 必履修パターン未登録教科
     */
    function getInsertHituRisyuMiTouroku($schregno, $model) {
        $query  = " INSERT INTO CLASS_REQUIRED_DAT ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= " (SCHREGNO, CLASSCD, SCHOOL_KIND, ERR_FLG, REGISTERCD, UPDATED) ";
        } else {
            $query .= " (SCHREGNO, CLASSCD, ERR_FLG, REGISTERCD, UPDATED) ";
        }
        if ($model->Properties["useClassDetailDat"] == '1') {
            $query .= " SELECT ";
            $query .= "     '{$schregno}', S1.CLASSCD, S1.SCHOOL_KIND, '1', '".STAFFCD."', sysdate() ";
            $query .= " FROM ";
            $query .= "     CLASS_DETAIL_DAT S1 ";
            $query .= " WHERE ";
            $query .= "     S1.YEAR = '".CTRL_YEAR."' AND ";
            $query .= "     S1.CLASS_SEQ = '005' AND ";
            $query .= "     NOT EXISTS (SELECT ";
            $query .= "                     'x' ";
            $query .= "                 FROM ";
            $query .= "                     CLASS_REQUIRED_DAT E1 ";
            $query .= "                 WHERE ";
            $query .= "                     SCHREGNO = '{$schregno}' AND ";
            $query .= "                     E1.CLASSCD = S1.CLASSCD AND ";
            $query .= "                     E1.SCHOOL_KIND = S1.SCHOOL_KIND ";
            $query .= "                 ) ";
        } else {
            $query .= " SELECT ";
            //教育課程対応
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "     '{$schregno}', S1.NAME1, 'H', '1', '".STAFFCD."', sysdate() ";
            } else {
                $query .= "     '{$schregno}', S1.NAME1, '1', '".STAFFCD."', sysdate() ";
            }
            $query .= " FROM ";
            $query .= "     NAME_MST S1 ";
            $query .= " WHERE ";
            $query .= "     S1.NAMECD1 = 'D031' ";
            $query .= "     AND NOT EXISTS (SELECT ";
            $query .= "                         'x' ";
            $query .= "                     FROM ";
            $query .= "                         CLASS_REQUIRED_DAT E1 ";
            $query .= "                     WHERE ";
            $query .= "                         SCHREGNO='{$schregno}' ";
            //教育課程対応
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "                         AND E1.CLASSCD = S1.NAME1 ";
                $query .= "                         AND E1.SCHOOL_KIND = 'H' ";
                $query .= "                     ) ";
            } else {
                $query .= "                         AND E1.CLASSCD = S1.NAME1) ";
            }
        }

        return $query;
    }
}
?>
