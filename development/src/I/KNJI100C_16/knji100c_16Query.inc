<?php

require_once('for_php7.php');

class knji100c_16Query extends Query {

/*
        性別区分 .........Z002
        役職区分 .........J001

        使用テーブル
        SCHREG_BASE_MST........基礎情報取得用
        SCHREG_REGD_DAT........学籍情報取得用
        SCHREG_TRAINHIST_DAT...個人指導情報取得用
*/
    //ＣＳＶ書き出し
    function selectQuery($model) {
        $query  = "";
        $query .= " WITH BASE_MST AS ( ";
        $query .= "     SELECT ";
        $query .= "         * ";
        $query .= "     FROM ";
        $query .= "         SCHREG_BASE_MST ";
        $query .= "     WHERE ";
        $query .= "         SCHREGNO IN ('" .str_replace(",", "','", $model->selectdata_l) ."') ";
        $query .= " ), TRAIN_HIST AS ( ";
        $query .= "     SELECT ";
        $query .= "         T1.TRAINDATE, ";
        $query .= "         T1.SCHREGNO, ";
        $query .= "         T1.PATIENTCD, ";
        $query .= "         MEISYOU_GET(T1.PATIENTCD,'H301',1) AS PATIENT, ";
        $query .= "         T1.STAFFCD, ";
        $query .= "         T2.STAFFNAME_SHOW AS STAFFNAME_SHOW, ";
        $query .= "         T1.HOWTOTRAINCD, ";
        $query .= "         MEISYOU_GET(T1.HOWTOTRAINCD,'H302',1) AS HOWTOTRAIN, ";
        $query .= "         T1.CONTENT ";
        $query .= "     FROM ";
        $query .= "         SCHREG_TRAINHIST_DAT T1 LEFT OUTER JOIN V_STAFF_MST T2  ";
        $query .= "         ON T1.YEAR = T2.YEAR AND T1.STAFFCD = T2.STAFFCD ";
        $query .= "     WHERE ";
        $query .= "         SCHREGNO IN ('" .str_replace(",", "','", $model->selectdata_l) ."') ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     T1.NAME, ";
        $query .= "     T2.GRADE, ";
        $query .= "     T2.HR_CLASS, ";
        $query .= "     T2.ATTENDNO, ";
        /*
        if ($model->output == OUT_CODE_NAME) {          //コード＋名称
            $query .= "     VALUE(T1.SEX,'') || ',' || ";
            $query .= "     VALUE(N1.NAME1,'') AS SEX, ";
        } else if ($model->output == OUT_NAME_ONLY) {   //名称のみ
            $query .= "     N1.NAME1 AS SEX, ";
        }
        */
        $query .= "     L1.TRAINDATE, ";
        if ($model->output == 1) {          //コード＋名称
            $query .= "     L1.PATIENTCD, ";
            $query .= "     L1.PATIENT, ";
            $query .= "     L1.STAFFCD, ";
            $query .= "     L1.STAFFNAME_SHOW, ";
            $query .= "     L1.HOWTOTRAINCD, ";
            $query .= "     L1.HOWTOTRAIN, ";
        } else if ($model->output == 2) {   //名称のみ
            $query .= "     L1.PATIENT, ";
            $query .= "     L1.STAFFNAME_SHOW, ";
            $query .= "     L1.HOWTOTRAIN, ";
        }
        $query .= "     L1.PATIENT, ";
        $query .= "     L1.STAFFNAME_SHOW, ";
        $query .= "     L1.HOWTOTRAIN, ";
        $query .= "     L1.CONTENT ";
        $query .= " FROM ";
        $query .= "     BASE_MST T1 ";
        $query .= "     LEFT JOIN TRAIN_HIST L1 ON L1.SCHREGNO = T1.SCHREGNO ";
        $query .= "     INNER JOIN SCHREG_REGD_DAT T2 ON  T2.SCHREGNO = T1.SCHREGNO ";
        $query .= "                                   AND T2.YEAR     = '".CTRL_YEAR."' ";
        $query .= "                                   AND T2.SEMESTER = '".CTRL_SEMESTER."' ";
        $query .= " ORDER BY ";
        $query .= "     T2.GRADE, ";
        $query .= "     T2.HR_CLASS, ";
        $query .= "     T2.ATTENDNO, ";
        $query .= "     L1.TRAINDATE ";

        return $query;
    }


    //履歴件数取得
    function getHistCount($model) {

        $query  = " SELECT ";
        $query .= "     MAX(T1.CNT) ";
        $query .= " FROM ";
        $query .= "     (SELECT ";
        $query .= "         SCHREGNO, ";
        $query .= "         COUNT(*) AS CNT ";
        $query .= "     FROM ";
        $query .= "         SCHREG_TRAINHIST_DAT ";
        $query .= "     WHERE ";
        $query .= "         SCHREGNO IN ('" .str_replace(",", "','", $model->selectdata_l) ."') ";
        $query .= "     GROUP BY ";
        $query .= "         SCHREGNO ";
        $query .= "     ) T1 ";

        return $query;
    }

    //学年取得
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
        $date = str_replace("/","-",CTRL_DATE);

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

    //生徒リストtoリスト右
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
        $query .= "     T1.SEMESTER = '". CTRL_SEMESTER ."' AND ";
        $query .= "     T1.GRADE || '_' || T1.HR_CLASS = '{$model->grade_hr_class}' ";
        $query .= " ORDER BY ";
        $query .= "     T1.ATTENDNO ";

        return $query;
    }

    //生徒リストtoリスト左
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
        $query .= "     T1.SCHREGNO IN ('" .str_replace(",", "','", $model->selectdata_l) ."') ";
        $query .= " ORDER BY ";
        $query .= "     T1.GRADE, ";
        $query .= "     T1.HR_CLASS, ";
        $query .= "     T1.ATTENDNO ";

        return $query;
    }

    //サブシステム取得
    function getSubSystem() {
        $query  = " SELECT ";
        $query .= "     NAMECD2 AS VALUE, ";
        $query .= "     NAMECD2 || '：' || NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' AND ";
        $query .= "     NAMECD1 = 'I001' ";

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
        $query .= "     AND DATA_DIV = '09' ";
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
        $query .= "     AND DATA_DIV = '09' ";

        return $query;
    }

    //選択フィールド保存
    function getFieldInsSql($model, $setSeq, $val) {

        //入金予定項目データ
        $data = array();
        $data["YEAR"][TEXT]            = CTRL_YEAR;
        $data["DATA_DIV"][TEXT]        = "09";
        $data["SEQ"][NUMBER]           = $setSeq;
        $data["FIELD_NAME"][TEXT]      = $val;
        $data["REGISTERCD"][TEXT]      = STAFFCD;
        $data["UPDATED"][FUNC]         = "sysdate()";

        $query = Query::insertSQL($data, "KNJI100C_KAKIDASHI_LIST");
        return $query;
    }
}
?>