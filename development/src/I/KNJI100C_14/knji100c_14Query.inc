<?php

require_once('for_php7.php');

class knji100c_14Query extends Query {
    //CSV出力データ取得
    function selectUngrdQuery($model) {
        $query  = " SELECT ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     T1.GRADE, ";
        $query .= "     T1.HR_CLASS, ";
        $query .= "     T1.ATTENDNO, ";
        $query .= "     T2.NAME, ";
        $query .= "     L1.GO_HOME_GROUP_NO, ";
        $query .= "     L1.RESPONSIBILITY, ";
        $query .= "     N1.NAME1 AS HOWTOCOMMUTECD, ";
        $query .= "     L1.UP_DOWN, ";
        $query .= "     L1.COMMUTE_HOURS, ";
        $query .= "     L1.COMMUTE_MINUTES, ";
        $query .= "     L1.OTHERHOWTOCOMMUTE ";
        for ($i = 1; $i <= 7; $i++) {
            $query .= "     ,L1.JOSYA_".$i." ";
            $query .= "     ,L1.ROSEN_".$i." ";
            $query .= "     ,L1.GESYA_".$i." ";
            $query .= "     ,L1.FLG_".$i." ";
        }
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1 ";
        $query .= "     INNER JOIN SCHREG_BASE_MST T2 ON T2.SCHREGNO = T1.SCHREGNO ";
        $query .= "     LEFT JOIN SCHREG_ENVIR_DAT L1 ON L1.SCHREGNO = T1.SCHREGNO ";
        $query .= "     LEFT JOIN NAME_MST N1 ON N1.NAMECD1 = 'H100' AND N1.NAMECD2 = L1.HOWTOCOMMUTECD ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR     = '".CTRL_YEAR."' AND ";
        $query .= "     T1.SEMESTER = '".CTRL_SEMESTER."' AND ";
        $query .= "     T1.SCHREGNO IN ('" .str_replace(",", "','", $model->selectdata_l) ."') ";
        $query .= " ORDER BY ";
        $query .= "     T1.GRADE, ";
        $query .= "     T1.HR_CLASS, ";
        $query .= "     T1.ATTENDNO ";

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
    function getSchnoIdou($model) {
        $date = str_replace("/","-",$model->date);

        $query  = " SELECT ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     T1.ATTENDNO ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1, ";
        $query .= "     SEMESTER_MST T2 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR     = '".CTRL_YEAR."' AND ";
        $query .= "     T1.SEMESTER = '".CTRL_SEMESTER."' AND ";
        $query .= "     T1.YEAR     = T2.YEAR AND ";
        $query .= "     T1.SEMESTER = T2.SEMESTER AND ";
                        //卒業(1)・転学(2)・退学(3)者 但し異動日が学期終了日または異動基準日より小さい場合
        $query .= "     EXISTS(SELECT 'X' FROM SCHREG_BASE_MST S1 ";
        $query .= "             WHERE   S1.SCHREGNO = T1.SCHREGNO AND ";
        $query .= "                     S1.GRD_DIV IN ('1','2','3') AND ";
        $query .= "                     S1.GRD_DATE < (CASE WHEN T2.EDATE < '".$date."' THEN T2.EDATE ELSE '".$date."' END)) ";
        $query .= " ORDER BY T1.ATTENDNO ";

        return $query;
    }

    //リストtoリスト右
    function getStudent_right($model) {
        $query  = " SELECT ";
        $query .= "     T2.HR_NAME, ";
        $query .= "     T1.ATTENDNO, ";
        $query .= "     L1.NAME_SHOW, ";
        $query .= "     T1.GRADE || T1.HR_CLASS || T1.ATTENDNO || '-' || T1.SCHREGNO AS VALUE ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1 ";
        $query .= " LEFT JOIN SCHREG_REGD_HDAT T2 ";
        $query .= "        ON T2.YEAR       = T1.YEAR ";
        $query .= "       AND T2.SEMESTER   = T1.SEMESTER ";
        $query .= "       AND T2.GRADE      = T1.GRADE ";
        $query .= "       AND T2.HR_CLASS   = T1.HR_CLASS ";
        $query .= " LEFT JOIN SCHREG_BASE_MST L1 ON L1.SCHREGNO = T1.SCHREGNO ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR     = '".CTRL_YEAR."' AND ";
        $query .= "     T1.SEMESTER = '". CTRL_SEMESTER ."' AND ";
        $query .= "     T1.GRADE || '_' || T1.HR_CLASS = '{$model->grade_hr_class}' ";
        $query .= " ORDER BY ";
        $query .= "     T1.ATTENDNO ";

        return $query;
    }

    //リストtoリスト左
    function getStudent_left($model) {
        $query  = " SELECT ";
        $query .= "     T2.HR_NAME, ";
        $query .= "     T1.ATTENDNO, ";
        $query .= "     L1.NAME_SHOW, ";
        $query .= "     T1.GRADE || T1.HR_CLASS || T1.ATTENDNO || '-' || T1.SCHREGNO AS VALUE  ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1 ";
        $query .= " LEFT JOIN SCHREG_REGD_HDAT T2 ";
        $query .= "        ON T2.YEAR       = T1.YEAR ";
        $query .= "       AND T2.SEMESTER   = T1.SEMESTER ";
        $query .= "       AND T2.GRADE      = T1.GRADE ";
        $query .= "       AND T2.HR_CLASS   = T1.HR_CLASS ";
        $query .= " LEFT JOIN SCHREG_BASE_MST L1 ON L1.SCHREGNO = T1.SCHREGNO ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR     = '".CTRL_YEAR."' AND ";
        $query .= "     T1.SEMESTER = '". CTRL_SEMESTER ."' AND ";
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

    //駅ネットマスタ取得
    function getStationNetMst($model) {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     STATION_NETMST ";

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
        $query .= "     AND DATA_DIV = '14' ";
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
        $query .= "     AND DATA_DIV = '14' ";

        return $query;
    }

    //選択フィールド保存
    function getFieldInsSql($model, $setSeq, $val) {
        $data = array();
        $data["YEAR"][TEXT]            = CTRL_YEAR;
        $data["DATA_DIV"][TEXT]        = "14";
        $data["SEQ"][NUMBER]           = $setSeq;
        $data["FIELD_NAME"][TEXT]      = $val;
        $data["REGISTERCD"][TEXT]      = STAFFCD;
        $data["UPDATED"][FUNC]         = "sysdate()";

        $query = Query::insertSQL($data, "KNJI100C_KAKIDASHI_LIST");
        return $query;
    }
}
?>