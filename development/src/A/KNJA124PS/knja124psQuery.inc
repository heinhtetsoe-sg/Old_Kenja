<?php

require_once('for_php7.php');

class knja124psQuery extends Query {

    //年組
    function getHrClass($model) {
        $query  = " SELECT ";
        $query .= "     T1.GRADE || T1.HR_CLASS AS VALUE,";
        $query .= "     T1.HR_NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_HDAT T1 ";
        $query .= "     INNER JOIN STAFF_MST T2 ON T2.STAFFCD = T1.TR_CD1 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '{$model->left_year}' ";
        $query .= "     AND T1.SEMESTER = '{$model->left_semester}' ";
        if ($model->sendAuth == DEF_UPDATE_RESTRICT || $model->sendAuth == DEF_REFER_RESTRICT) {
            $query .= " AND (T1.TR_CD1 = '". STAFFCD ."' OR ";
            $query .= "      T1.TR_CD2 = '". STAFFCD ."' OR ";
            $query .= "      T1.TR_CD3 = '". STAFFCD ."' OR ";
            $query .= "      T1.SUBTR_CD1 = '". STAFFCD ."' OR ";
            $query .= "      T1.SUBTR_CD2 = '". STAFFCD ."' OR ";
            $query .= "      T1.SUBTR_CD3 = '". STAFFCD ."') ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";
        return $query;
    }

    //生徒
    function getStudents($model) {
        $query .= " SELECT ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     T1.ATTENDNO, ";
        $query .= "     T2.NAME_SHOW, ";
        $query .= "     T3.HR_NAME, ";
        $query .= "     T3.HR_NAMEABBV ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1 ";
        $query .= "     INNER JOIN SCHREG_BASE_MST T2 ON T2.SCHREGNO = T1.SCHREGNO ";
        $query .= "     INNER JOIN SCHREG_REGD_HDAT T3 ON T3.YEAR = T1.YEAR AND T3.SEMESTER = T1.SEMESTER AND T3.GRADE = T1.GRADE AND T3.HR_CLASS = T1.HR_CLASS ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '{$model->left_year}' ";
        $query .= "     AND T1.SEMESTER = '{$model->left_semester}' ";
        $query .= "     AND T1.GRADE || T1.HR_CLASS = '{$model->hr_class}' ";
        $query .= " ORDER BY ";
        $query .= "     T1.GRADE, ";
        $query .= "     T1.HR_CLASS, ";
        $query .= "     T1.ATTENDNO ";
        return $query;
    }

    function getNameMst($namecd1) {

        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = '".$namecd1."' AND ";
        $query .= "     NAMECD2 = '00' ";

        return $query;
    }

    //１レコード取得
    function getTrainRow($model, $schregno)
    {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     HTRAINREMARK_P_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR        = '".$model->left_year."' AND ";
        $query .= "     SCHREGNO    = '".$schregno."' ";

        return $query;
    }

    //行動記録の取得
    function getBehavior($model, $schregno)
    {
		$query  = " SELECT ";
		$query .= "     DIV, ";
		$query .= "     CODE, ";
		$query .= "     ANNUAL, ";
		$query .= "     RECORD ";
		$query .= " FROM ";
		$query .= "     BEHAVIOR_DAT ";
		$query .= " WHERE ";
		$query .= "     YEAR        = '".$model->left_year."' AND ";
		$query .= "     SCHREGNO    = '".$schregno."' AND ";
		$query .= "     DIV IN ('3', '4') ";
		$query .= " ORDER BY ";
		$query .= "     DIV, ";
		$query .= "     CODE ";

        return $query;
    }

    function getInkanUmu() {
        $query  = " SELECT ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     ATTEST_INKAN_DAT ";
        $query .= " WHERE ";
        $query .= "     '".str_replace("/", "-", CTRL_DATE)."' BETWEEN START_DATE AND VALUE(STOP_DATE, '9999-12-31') ";
        $query .= "     AND STAFFCD = '".STAFFCD."' ";

        return $query;
    }

    function getIneiFlg($model, $schregno) {
        $query  = " WITH INKAN_T AS ( ";
        $query .= " SELECT ";
        $query .= "     MAX(STAMP_NO) AS STAMP_NO, ";
        $query .= "     STAFFCD ";
        $query .= " FROM ";
        $query .= "     ATTEST_INKAN_DAT ";
        $query .= " WHERE ";
        $query .= "     '".str_replace("/", "-", CTRL_DATE)."' BETWEEN START_DATE AND VALUE(STOP_DATE, '9999-12-31') ";
        $query .= " GROUP BY ";
        $query .= "     STAFFCD ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     T1.CHAGE_OPI_SEQ, ";
        $query .= "     IN1.STAMP_NO AS CHAGE_STAMP, ";
        $query .= "     T1.LAST_OPI_SEQ, ";
        $query .= "     IN2.STAMP_NO AS LAST_STAMP ";
        $query .= " FROM ";
        $query .= "     ATTEST_OPINIONS_WK T1 ";
        $query .= "     LEFT JOIN INKAN_T IN1 ON T1.CHAGE_STAFFCD = IN1.STAFFCD ";
        $query .= "     LEFT JOIN INKAN_T IN2 ON T1.LAST_STAFFCD = IN2.STAFFCD ";
        $query .= " WHERE ";
        $query .= "     YEAR     = '{$model->left_year}' AND ";
        $query .= "     SCHREGNO = '{$schregno}' ";

        return $query;
    }

    //担任データ
    function getOpinionsSignature($model, $schregno) {
        $query  = " SELECT ";
        $query .= "     L1.OPINION AS CHAGE_OPI, ";
        $query .= "     L2.OPINION AS LAST_OPI ";
        $query .= " FROM ";
        $query .= "     ATTEST_OPINIONS_WK T1 ";
        $query .= "     LEFT JOIN ATTEST_OPINIONS_DAT L1 ON T1.YEAR = L1.YEAR ";
        $query .= "          AND T1.CHAGE_OPI_SEQ = L1.SEQ ";
        $query .= "          AND T1.SCHREGNO = L1.SCHREGNO ";
        $query .= "     LEFT JOIN ATTEST_OPINIONS_DAT L2 ON T1.YEAR = L2.YEAR ";
        $query .= "          AND T1.LAST_OPI_SEQ = L2.SEQ ";
        $query .= "          AND T1.SCHREGNO = L2.SCHREGNO ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".$model->left_year."' ";
        $query .= "     AND T1.SCHREGNO = '".$schregno."' ";

        return $query;
    }

    //インサート
    function &getInsertSql($db, $model, $seq, $opinion, $signature, $schregno) {

        $data["YEAR"][TEXT]        = $model->left_year;
        $data["SEQ"][NUMBER]       = $seq;
        $data["STAFFCD"][TEXT]     = STAFFCD;
        $data["CERT_NO"][NUMBER]   = 123456;
        $data["OPINION"][TEXT]     = $opinion;   //ハッシュ値
        $data["SIGNATURE"][TEXT]   = $signature; //署名
        $data["RESULT"][NUMBER]    = 0;
        $data["SCHREGNO"][TEXT]    = $schregno;
        $data["ACTION"][NUMBER]    = 1;
        $data["STATUS"][NUMBER]    = 1;
        $data["REGISTERCD"][TEXT]  = STAFFCD;
        $data["UPDATED"][FUNC]     = "sysdate()";

        $db->query(Query::insertSQL($data, "ATTEST_OPINIONS_DAT"));

        $query = knja124psQuery::getDeleteWk($model, $schregno);
        $db->query($query);

        $data = array();

        $data["YEAR"][TEXT]            = $model->left_year;
        $data["SCHREGNO"][TEXT]        = $schregno;
        $data["CHAGE_OPI_SEQ"][NUMBER] = $seq;
        $data["CHAGE_STAFFCD"][TEXT]   = STAFFCD;
        $data["LAST_OPI_SEQ"][NUMBER]  = '';
        $data["LAST_STAFFCD"][TEXT]    = '';
        $data["REGISTERCD"][TEXT]      = STAFFCD;
        $data["UPDATED"][FUNC]         = "sysdate()";

        $db->query(Query::insertSQL($data, "ATTEST_OPINIONS_WK"));

    }

    function getDeleteWk($model, $schregno) {
        $query = "DELETE FROM ATTEST_OPINIONS_WK WHERE YEAR = '".$model->left_year."' AND SCHREGNO = '".$schregno."'";

        return $query;
    }

    function getSeqNo($model) {
        $query = "SELECT MAX(SEQ) FROM ATTEST_OPINIONS_DAT WHERE YEAR = '{$model->left_year}'";

        return $query;
    }

    //更新処理
    function &getInsUpdSql($db, $model, $seq, $opinion, $signature, $schregno) {

        $data["YEAR"][TEXT]        = $model->left_year;
        $data["SEQ"][NUMBER]       = $seq;
        $data["STAFFCD"][TEXT]     = STAFFCD;
        $data["CERT_NO"][NUMBER]   = 123456;
        $data["OPINION"][TEXT]     = $opinion;   //ハッシュ値
        $data["SIGNATURE"][TEXT]   = $signature; //署名
        $data["RESULT"][NUMBER]    = 0;
        $data["SCHREGNO"][TEXT]    = $schregno;
        $data["ACTION"][NUMBER]    = 1;
        $data["STATUS"][NUMBER]    = 1;
        $data["REGISTERCD"][TEXT]  = STAFFCD;
        $data["UPDATED"][FUNC]     = "sysdate()";

        $db->query(Query::insertSQL($data, "ATTEST_OPINIONS_DAT"));

        $data = array();

        $data["LAST_OPI_SEQ"][NUMBER]  = $seq;
        $data["LAST_STAFFCD"][TEXT]    = STAFFCD;
        $data["REGISTERCD"][TEXT]      = STAFFCD;
        $data["UPDATED"][FUNC]         = "sysdate()";

        $where  = " WHERE ";
        $where .= "     YEAR = '".$model->left_year."' ";
        $where .= "     AND SCHREGNO = '".$schregno."' ";

        $db->query(Query::updateSQL($data, "ATTEST_OPINIONS_WK", $where));

    }

    //学習記録データ取得
    function getStudyRec($model, $schregno) {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     SCHREG_STUDYREC_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR     = '".$model->left_year."' AND ";
        $query .= "     SCHREGNO = '".$schregno."' ";

        return $query;
    }

    //出欠記録データ取得
    function getAttendRec($model, $schregno) {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     SCHREG_ATTENDREC_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR     = '".$model->left_year."' AND ";
        $query .= "     SCHREGNO = '".$schregno."' ";

        return $query;
    }

}
?>
