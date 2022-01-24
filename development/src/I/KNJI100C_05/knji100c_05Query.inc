<?php

require_once('for_php7.php');
class knji100c_05Query extends Query {

    //模試区分
    function getMockSyubetu()
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     NAMECD2 AS VALUE, ";
        $query .= "     NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND NAMECD1 = 'H308' ";
        $query .= "     AND NAMESPARE1 = '1' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //模試区分
    function getMockMst($model)
    {
        $query  = " SELECT ";
        $query .= "     MOCKCD AS VALUE, ";
        $query .= "     MOCKNAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     MOCK_MST ";
        $query .= " WHERE ";
        $query .= "     substr(MOCKCD, 1, 1) = '".$model->mockSyubetu."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //タイトル用科目
    function selectSubclassTitleQuery($model)
    {
        $query .= " SELECT DISTINCT ";
        $query .= "     T1.YEAR, ";
        $query .= "     T1.MOCKCD, ";
        $query .= "     L2.MOCKNAME1, ";
        $query .= "     T1.MOCK_SUBCLASS_CD, ";
        $query .= "     CASE WHEN T1.MOCK_SUBCLASS_CD = '999999' ";
        $query .= "          THEN '総合' ";
        $query .= "          ELSE L1.SUBCLASS_NAME ";
        $query .= "     END AS SUBCLASS_NAME ";
        $query .= " FROM ";
        $query .= "     MOCK_RANK_DAT T1 ";
        $query .= "     LEFT JOIN MOCK_SUBCLASS_MST L1 ON T1.MOCK_SUBCLASS_CD = L1.MOCK_SUBCLASS_CD ";
        $query .= "     LEFT JOIN MOCK_MST L2 ON T1.MOCKCD = L2.MOCKCD ";
        $query .= " WHERE ";
        $query .= "     T1.MOCKCD IN ('" .str_replace(",", "','", $model->selectdata_r) ."') ";
        $query .= "     AND T1.MOCK_SUBCLASS_CD NOT IN ('333333', '555555') ";
        $query .= "     AND T1.SCHREGNO IN ('" .str_replace(",", "','", $model->selectdata_l) ."') ";
        $query .= "     AND T1.MOCKDIV = '".$model->mockDiv."' ";
        $query .= " ORDER BY ";
        $query .= "     T1.YEAR, ";
        $query .= "     T1.MOCKCD, ";
        $query .= "     T1.MOCK_SUBCLASS_CD ";

        return $query;
    }

    //CSV
    function selectMockRankQuery($model, $year, $mockCd, $mockSubclassCd, $schregno, $headFlg)
    {
        $query  = " SELECT ";
        if ($headFlg) {
            $query .= "     REGD.SCHREGNO, ";
            $query .= "     BASE.NAME, ";
            $query .= "     REGD.GRADE, ";
            $query .= "     REGD.HR_CLASS, ";
            $query .= "     REGD.ATTENDNO, ";
            if ($model->output == OUT_CODE_NAME) {       //コード＋名称
                $query .= "  BASE.SEX, ";
                $query .= "  Z002.NAME1,";
            } else if ($model->output == OUT_CODE_ONLY) { //コードのみ
                $query .= "  BASE.SEX, ";
            } else if ($model->output == OUT_NAME_ONLY) { //名称のみ
                $query .= "  Z002.NAME1,";
            }
        }
        $query .= "     MOCK_RANK.MOCK_SUBCLASS_CD, ";
        if ($model->mockDiv == "2") {
            $query .= "     MOCK_RANK.AVG AS SCORE, ";
        } else {
            $query .= "     MOCK_RANK.SCORE AS SCORE, ";
        }
        $query .= "     MOCK_RANK.GRADE_RANK, ";
        $query .= "     MOCK_RANK.CLASS_RANK, ";
        $query .= "     MOCK_RANK.COURSE_RANK ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT REGD ";
        $query .= "     LEFT JOIN SCHREG_BASE_MST BASE ON REGD.SCHREGNO = BASE.SCHREGNO ";
        $query .= "     LEFT JOIN NAME_MST Z002 ON Z002.NAMECD1 = 'Z002' ";
        $query .= "          AND BASE.SEX = Z002.NAMECD2 ";
        $query .= "     LEFT JOIN MOCK_RANK_DAT MOCK_RANK ON MOCK_RANK.YEAR = '".$year."' ";
        $query .= "          AND MOCK_RANK.MOCKCD = '".$mockCd."' ";
        $query .= "          AND REGD.SCHREGNO = MOCK_RANK.SCHREGNO ";
        $query .= "          AND MOCK_RANK.MOCK_SUBCLASS_CD = '".$mockSubclassCd."' ";
        $query .= "          AND MOCK_RANK.MOCKDIV = '".$model->mockDiv."' ";
        $query .= " WHERE ";
        $query .= "     REGD.YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND REGD.SEMESTER = '".CTRL_SEMESTER."' ";
        $query .= "     AND REGD.SCHREGNO = '".$schregno."' ";
        $query .= "ORDER BY ";
        $query .= "  REGD.GRADE, ";
        $query .= "  REGD.HR_CLASS, ";
        $query .= "  REGD.ATTENDNO ";

        return $query;
    }

    //名称マスタ
    function getMeisyou($namecd1, $namecd2) {
        $query  = "SELECT ";
        $query .= "    NAME1 ";
        $query .= "FROM ";
        $query .= "    NAME_MST ";
        $query .= "WHERE ";
        $query .= "    NAMECD1 = '".$namecd1."' ";
        $query .= "    AND NAMECD2 = '".$namecd2."' ";

        return $query;
    }

    //課程学科取得
    function getCourseMajor($model) {
        $query  = " SELECT ";
        $query .= "     COURSECD || MAJORCD AS VALUE, ";
        $query .= "     COURSENAME || MAJORNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     V_COURSE_MAJOR_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //学年
    function getGrade($model) {
        $query  = "SELECT ";
        $query .= "    GRADE AS VALUE, ";
        $query .= "    GRADE_NAME1 AS LABEL ";
        $query .= "FROM ";
        $query .= "    SCHREG_REGD_GDAT ";
        $query .= "WHERE ";
        $query .= "    YEAR = '".CTRL_YEAR."' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= "     AND GRADE IN (SELECT ";
                $query .= "                      REGD_G.GRADE ";
                $query .= "                   FROM ";
                $query .= "                      SCHREG_REGD_GDAT REGD_G ";
                $query .= "                   WHERE ";
                $query .= "                      REGD_G.YEAR = '".CTRL_YEAR."' ";
                $query .= "                      AND REGD_G.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."')) ";
            }
        } elseif ($model->Properties["useSchool_KindField"] == "1") {
            $query .= "     AND GRADE IN (SELECT ";
            $query .= "                      REGD_G.GRADE ";
            $query .= "                   FROM ";
            $query .= "                      SCHREG_REGD_GDAT REGD_G ";
            $query .= "                   WHERE ";
            $query .= "                      REGD_G.YEAR = '".CTRL_YEAR."' ";
            $query .= "                      AND REGD_G.SCHOOL_KIND = '" .SCHOOLKIND ."') ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //性別取得
    function getSex($model) {
        $query  = " SELECT ";
        $query .= "     NAMECD2 AS VALUE, ";
        $query .= "     ABBV1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR    = '".CTRL_YEAR."' AND ";
        $query .= "     NAMECD1 = 'Z002' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //年組取得（権限チェック）
    function getAuth($model) {
        $query  = " SELECT ";
        $query .= "     GRADE || '_' ||  HR_CLASS AS VALUE, ";
        $query .= "     HR_NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_HDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR        = '".CTRL_YEAR."' AND ";
        $query .= "     SEMESTER    = '".CTRL_SEMESTER."' AND ";
        $query .= "     GRADE       = '".$model->grade."' ";
        //参照・更新可（制限付き）
        if (AUTHORITY == DEF_REFER_RESTRICT || AUTHORITY == DEF_UPDATE_RESTRICT){
            $query .= " AND '".STAFFCD."' IN (TR_CD1, TR_CD2, TR_CD3) ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //対象外の生徒取得
    function getSchnoIdou($model)
    {
        $date = str_replace("/","-",$model->date);

        $query  = "";
        $query .= " SELECT T1.SCHREGNO, T1.ATTENDNO ";
        $query .= " FROM   SCHREG_REGD_DAT T1, SEMESTER_MST T2 ";
        $query .= " WHERE  T1.YEAR = '".CTRL_YEAR."' AND ";
        $query .= "        T1.SEMESTER = '".CTRL_SEMESTER."' AND ";
        $query .= "        T1.YEAR = T2.YEAR AND ";
        $query .= "        T1.SEMESTER = T2.SEMESTER AND ";
                           //卒業(1)・転学(2)・退学(3)者 但し異動日が学期終了日または異動基準日より小さい場合
        $query .= "        EXISTS(SELECT 'X' FROM SCHREG_BASE_MST S1 ";
        $query .= "                WHERE S1.SCHREGNO = T1.SCHREGNO AND ";
        $query .= "                    (S1.GRD_DIV IN('1','2','3') AND S1.GRD_DATE < ";
        $query .= "                         CASE WHEN T2.EDATE < '".$date."' ";
        $query .= "                              THEN T2.EDATE ELSE '".$date."' END)) ";
        $query .= " ORDER BY T1.ATTENDNO ";

        return $query;
    }

    //リストtoリスト右
    function getStudent_right($model) {
        $query .= " SELECT ";
        $query .= "     T2.HR_NAME, ";
        $query .= "     T1.ATTENDNO, ";
        $query .= "     L1.NAME_SHOW, ";
        $query .= "     T1.GRADE || T1.HR_CLASS || T1.ATTENDNO || '-' || T1.SCHREGNO AS VALUE ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1 ";
        $query .= " LEFT JOIN SCHREG_REGD_HDAT T2 ";
        $query .= "        ON T2.YEAR = T1.YEAR ";
        $query .= "       AND T2.SEMESTER = T1.SEMESTER ";
        $query .= "       AND T2.GRADE = T1.GRADE ";
        $query .= "       AND T2.HR_CLASS = T1.HR_CLASS ";
        $query .= " LEFT JOIN SCHREG_BASE_MST L1 ON L1.SCHREGNO = T1.SCHREGNO ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' AND ";
        $query .= "     T1.SEMESTER = '". CTRL_SEMESTER ."' ";
        if ($model->coursemajor) {
            $query .= " AND T1.COURSECD || T1.MAJORCD = '{$model->coursemajor}' ";
        }
        if ($model->grade) {
            $query .= " AND T1.GRADE = '{$model->grade}' ";
        }
        if ($model->sex) {
            $query .= " AND L1.SEX = '{$model->sex}' ";
        }
        if ($model->grade_hr_class) {
            $query .= " AND T1.GRADE || '_' || T1.HR_CLASS = '{$model->grade_hr_class}' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }


    //リストtoリスト左
    function getStudent_left($model) {
        $query .= " SELECT ";
        $query .= "     T2.HR_NAME, ";
        $query .= "     T1.ATTENDNO, ";
        $query .= "     L1.NAME_SHOW, ";
        $query .= "     T1.GRADE || T1.HR_CLASS || T1.ATTENDNO || '-' || T1.SCHREGNO AS VALUE  ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1 ";
        $query .= " LEFT JOIN SCHREG_REGD_HDAT T2 ";
        $query .= "        ON T2.YEAR = T1.YEAR ";
        $query .= "       AND T2.SEMESTER = T1.SEMESTER ";
        $query .= "       AND T2.GRADE = T1.GRADE ";
        $query .= "       AND T2.HR_CLASS = T1.HR_CLASS ";
        $query .= " LEFT JOIN SCHREG_BASE_MST L1 ON L1.SCHREGNO = T1.SCHREGNO ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' AND ";
        $query .= "     T1.SEMESTER = '". CTRL_SEMESTER ."' AND ";
        $query .= "     T1.GRADE    = '".$model->grade."' AND ";
        $query .= "     T1.SCHREGNO IN ('" .str_replace(",", "','", $model->selectdata_l) ."') ";
        $query .= " ORDER BY ";
        $query .= "     T1.GRADE, ";
        $query .= "     T1.HR_CLASS, ";
        $query .= "     T1.ATTENDNO ";

        return $query;
    }

    //サブシステム名取得
    function getSubSystem() {
        $query  = " SELECT ";
        $query .= "     NAMECD2 AS VALUE, ";
        $query .= "     NAMECD2 || '：' || NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' AND ";
        $query .= "     NAMECD1 = 'I001' ";
        $query .= " ORDER BY ";
        $query .= "     NAMECD2 ";

        return $query;
    }

    //選択フィールド取得
    function getFieldSql($model) {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     KNJI100C_KAKIDASHI_LIST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND DATA_DIV = '05' ";
        $query .= " ORDER BY ";
        $query .= "     SEQ ";

        return $query;
    }

    //選択フィールド削除
    function getDelFieldSql($model) {
        $query  = " DELETE FROM ";
        $query .= "     KNJI100C_KAKIDASHI_LIST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND DATA_DIV = '05' ";

        return $query;
    }

    //選択フィールド保存
    function getFieldInsSql($model, $setSeq, $val) {

        //入金予定項目データ
        $data = array();
        $data["YEAR"][TEXT]            = CTRL_YEAR;
        $data["DATA_DIV"][TEXT]        = "05";
        $data["SEQ"][NUMBER]           = $setSeq;
        $data["FIELD_NAME"][TEXT]      = $val;
        $data["REGISTERCD"][TEXT]      = STAFFCD;
        $data["UPDATED"][FUNC]         = "sysdate()";

        $query = Query::insertSQL($data, "KNJI100C_KAKIDASHI_LIST");
        return $query;
    }
}
?>