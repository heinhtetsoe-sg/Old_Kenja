<?php

require_once('for_php7.php');

class knji100c_15Query extends Query {

    //CSVヘッダ(SCORE)
    function getHeaderSubclassName($model) {
        $query  = " SELECT DISTINCT ";
        $query .= "     MSM.MOCK_SUBCLASS_CD ";
        $query .= "     , MSM.SUBCLASS_NAME ";
        $query .= " FROM ";
        $query .= "     MOCK_SUBCLASS_MST MSM ";
        $query .= "     INNER JOIN MOCK_RANK_RANGE_DAT MRRD ";
        $query .= "         ON MRRD.MOCK_SUBCLASS_CD = MSM.MOCK_SUBCLASS_CD ";
        $query .= " WHERE ";
        $query .= "     MRRD.YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND MRRD.MOCKCD IN ('".str_replace(",", "','", $model->selectdata_r)."') ";
        $query .= " ORDER BY ";
        $query .= "     MSM.MOCK_SUBCLASS_CD ";

        return $query;
    }

    //生徒情報取得
    function getStudentInfo($schregno) {
        $query  = " SELECT ";
        $query .= "     SCH.SCHREGNO ";
        $query .= "     , BASE.NAME_SHOW ";
        $query .= "     , SCH.GRADE ";
        $query .= "     , SCH.HR_CLASS ";
        $query .= "     , SCH.ATTENDNO ";
        $query .= "     , BASE.SEX ";
        $query .= "     , CASE BASE.SEX ";
        $query .= "         WHEN 1 THEN '男性' ";
        $query .= "         WHEN 2 THEN '女性' ";
        $query .= "       END SEX_NAME ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT SCH ";
        $query .= "     INNER JOIN SCHREG_BASE_MST BASE ";
        $query .= "         ON BASE.SCHREGNO = SCH.SCHREGNO ";
        $query .= " WHERE ";
        $query .= "     SCH.YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND SCH.SEMESTER = '".CTRL_SEMESTER."' ";
        $query .= "     AND SCH.SCHREGNO = '".$schregno."' ";

        return $query;
    }

    function selectDataQuery ($mockCd, $schregno) {
        $query  = " SELECT ";
        $query .= "     MST.MOCKNAME1 ";
        $query .= "     , DAT1.MOCK_SUBCLASS_CD ";
        $query .= "     , DAT1.SCORE ";
        $query .= "     , DAT1.GTZ ";
        $query .= "     , DAT1.DEVIATION AS ALL_DEVIATION ";
        $query .= "     , DAT2.DEVIATION AS SCHOOL_DEVIATION ";
        $query .= "     , DAT1.RANK AS ALL_RANK ";
        $query .= "     , DAT2.RANK AS SCHOOL_RANK ";
        $query .= " FROM ";
        $query .= "     MOCK_RANK_RANGE_DAT DAT1 ";
        $query .= "     LEFT JOIN MOCK_RANK_RANGE_DAT DAT2 ";
        $query .= "         ON DAT2.YEAR = DAT1.YEAR ";
        $query .= "         AND DAT2.MOCKCD = DAT1.MOCKCD ";
        $query .= "         AND DAT2.SCHREGNO = DAT1.SCHREGNO ";
        $query .= "         AND DAT2.MOCK_SUBCLASS_CD = DAT1.MOCK_SUBCLASS_CD ";
        $query .= "         AND DAT2.RANK_DIV = DAT1.RANK_DIV ";
        $query .= "         AND DAT2.MOCKDIV = DAT1.MOCKDIV ";
        $query .= "         AND DAT2.RANK_RANGE = '2' ";
        $query .= "     LEFT JOIN MOCK_MST MST ";
        $query .= "         ON MST.MOCKCD = DAT1.MOCKCD ";
        $query .= " WHERE ";
        $query .= "     DAT1.YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND DAT1.MOCKCD = '".$mockCd."' ";
        $query .= "     AND DAT1.SCHREGNO = '".$schregno."' ";
        $query .= "     AND DAT1.RANK_RANGE = '1' ";

        return $query;
    }

    //学年
    function getGrade() {
        $query  = "SELECT ";
        $query .= "    GRADE AS VALUE, ";
        $query .= "    GRADE_NAME1 AS LABEL ";
        $query .= "FROM ";
        $query .= "    SCHREG_REGD_GDAT ";
        $query .= "WHERE ";
        $query .= "    YEAR = '".CTRL_YEAR."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //年組取得（権限チェック）
    function getAuth($model) {
        //参照・更新可
        if (AUTHORITY == DEF_REFERABLE || AUTHORITY == DEF_UPDATABLE){
            $query  = "SELECT ";
            $query .= "    GRADE || '_' ||  HR_CLASS AS VALUE, ";
            $query .= "    HR_NAME AS LABEL ";
            $query .= "FROM ";
            $query .= "    SCHREG_REGD_HDAT ";
            $query .= "WHERE ";
            $query .= "    YEAR = '".CTRL_YEAR."' ";
            $query .= "    AND SEMESTER = '". CTRL_SEMESTER ."' ";
            $query .= "    AND GRADE = '". $model->grade ."' ";
            $query .= " ORDER BY ";
            $query .= "     VALUE ";
        }
        //参照・更新可（制限付き）
        if (AUTHORITY == DEF_REFER_RESTRICT || AUTHORITY == DEF_UPDATE_RESTRICT){
            $query  = "SELECT ";
            $query .= "    GRADE || '_' ||  HR_CLASS AS VALUE, ";
            $query .= "    HR_NAME AS LABEL ";
            $query .= "FROM ";
            $query .= "    SCHREG_REGD_HDAT ";
            $query .= "WHERE ";
            $query .= "    YEAR = '".CTRL_YEAR."' ";
            $query .= "    AND SEMESTER = '". CTRL_SEMESTER ."' ";
            $query .= "    AND GRADE = '". $model->grade ."' ";
            $query .= "    AND (TR_CD1 = '" .STAFFCD ."' ";
            $query .= "         OR TR_CD2 = '" .STAFFCD ."' ";
            $query .= "         OR TR_CD3 = '" .STAFFCD ."') ";
            $query .= " ORDER BY ";
            $query .= "     VALUE ";
        }

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

    //リストtoリスト右（生徒）
    function getStudent_right($model) {
        $query  = " SELECT ";
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


    //リストtoリスト左（生徒）
    function getStudent_left($model) {
        $query  = " SELECT ";
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

    //業者コード取得
    function getCompanycd() {
        $query  = " SELECT ";
        $query .= "     NAMESPARE1 AS VALUE ";
        $query .= "     , NAMESPARE1 || ':' || NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND NAMECD1 = 'H313' ";
        $query .= "     AND NAMECD2 = '0002' ";

        return $query;
    }

    //リストtoリスト右（項目）
    function getItem_right($model) {
        $query  = " SELECT ";
        $query .= "     MOCKNAME1 AS LABEL ";
        $query .= "     , MOCKCD AS VALUE ";
        $query .= " FROM ";
        $query .= "     MOCK_MST ";
        $query .= " WHERE ";
        $query .= "     COMPANYCD = '".$model->companycd."' ";

        return $query;
    }

    //リストtoリスト右（項目）
    function getItem_left($model) {
        $query  = " SELECT ";
        $query .= "     MOCKNAME1 AS LABEL ";
        $query .= "     , MOCKCD AS VALUE ";
        $query .= " FROM ";
        $query .= "     MOCK_MST ";
        $query .= " WHERE ";
        $query .= "     COMPANYCD = '".$model->companycd."' ";
        $query .= "     AND MOCKCD IN ('".str_replace(",", "','", $model->selectdata_r)."') ";

        return $query;
    }
}
?>