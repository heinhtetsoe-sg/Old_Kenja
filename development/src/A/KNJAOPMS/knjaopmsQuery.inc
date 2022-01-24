<?php

require_once('for_php7.php');

class knjaopmsQuery extends Query {

    //学年取得
    function getYear()
    {
        $setAnd = "";
        $query  = " SELECT ";
        $query .= "     YEAR AS VALUE, ";
        $query .= "     YEAR || '年度' AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHOOL_MST ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //学期取得
    function getSemester($model)
    {
        $setAnd = "";
        $query  = " SELECT ";
        $query .= "     YEAR || '-' || SEMESTER AS VALUE, ";
        $query .= "     YEAR || '-' || SEMESTERNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SEMESTER_MST ";
        $query .= " WHERE ";
        if ($model->field["YEAR"]) {
            $query .= "     YEAR = '".$model->field["YEAR"]."' ";
            $setAnd = "AND";
        }
        $query .= "     {$setAnd} SEMESTER <> '9' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //年取得（権限チェック）
    function getGrade($semester, $model, $div = "")
    {
        $setAnd = "";
        //参照・更新可
        $query  = " SELECT DISTINCT ";
        if ($div == "GRADE") {
            $query .= "     YEAR || '-' || GRADE AS VALUE, ";
            $query .= "     YEAR || '-' || GRADE_NAME1 AS LABEL ";
        }
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_GDAT ";
        $query .= " WHERE ";
        if ($model->field["YEAR"]) {
            $query .= "     YEAR = '".$model->field["YEAR"]."' ";
            $setAnd = "AND";
        }
        $query .= "     {$setAnd} SCHOOL_KIND = 'H' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //年組取得（権限チェック）
    function getGradeHrClass($model)
    {
        $setAnd = "";
        //参照・更新可
        $query  = " SELECT DISTINCT ";
        $query .= "     YEAR || '-' || SEMESTER || '-' || GRADE || '-' || HR_CLASS AS VALUE, ";
        $query .= "     YEAR || '-' || SEMESTER || '-' || GRADE || '-' || HR_NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_HDAT ";
        if ($model->field["YEAR"] || $model->field["SEMESTER"] || $model->field["GRADE"]) {
            $query .= " WHERE ";
        }
        if ($model->field["YEAR"]) {
            $query .= "     YEAR = '".$model->field["YEAR"]."' ";
            $setAnd = "AND";
        }
        if ($model->field["SEMESTER"]) {
            list($year, $semester) = preg_split("/-/", $model->field["SEMESTER"]);
            $query .= "     {$setAnd} SEMESTER = '".$semester."' ";
            $setAnd = "AND";
        }
        if ($model->field["GRADE"]) {
            list($year, $grade) = preg_split("/-/", $model->field["GRADE"]);
            $query .= "     {$setAnd} GRADE = '".$grade."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    function getNameMst($namecd1) {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = '".$namecd1."' ";
        $query .= "     AND NAMECD2 = '00' ";

        return $query;
    }

    function getStudent2($model) {
        $query  = " WITH SCH_T AS ( ";
        $query .= " SELECT ";
        $query .= "     T1.YEAR, ";
        $query .= "     T1.SCHREGNO ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1 ";
        $query .= " WHERE ";
        $query .= "     EXISTS( ";
        $query .= "         SELECT ";
        $query .= "             'x' ";
        $query .= "         FROM ";
        $query .= "             SCHREG_REGD_GDAT E1 ";
        $query .= "         WHERE ";
        $query .= "             T1.YEAR = E1.YEAR ";
        $query .= "             AND T1.GRADE = E1.GRADE ";
        $query .= "             AND E1.SCHOOL_KIND = 'H' ";
        $query .= "     ) ";
        if ($model->field["YEAR"]) {
            $query .= "     AND T1.YEAR = '{$model->field["YEAR"]}' ";
        }
        $query .= " GROUP BY ";
        $query .= "     T1.YEAR, ";
        $query .= "     T1.SCHREGNO ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     T1.YEAR, ";
        $query .= "     T1.SCHREGNO ";
        $query .= " FROM ";
        $query .= "     ATTEST_OPINIONS_WK T1 ";
        $query .= " WHERE ";
        $query .= "     EXISTS( ";
        $query .= "         SELECT ";
        $query .= "             'x' ";
        $query .= "         FROM ";
        $query .= "             SCH_T E1 ";
        $query .= "         WHERE ";
        $query .= "             T1.YEAR = E1.YEAR ";
        $query .= "             AND T1.SCHREGNO = E1.SCHREGNO ";
        $query .= "     ) ";

        return $query;
    }

    function getStudent($model, $hrClass) {
        $query  = " SELECT DISTINCT ";
        $query .= "     YEAR, ";
        $query .= "     SEMESTER, ";
        $query .= "     GRADE, ";
        $query .= "     HR_CLASS, ";
        $query .= "     SCHREGNO ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR || '-' || SEMESTER || '-' || GRADE || '-' || HR_CLASS ".$hrClass." ";
        return $query;
    }

    //１レコード取得
    function getTrainRow($model)
    {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     HTRAINREMARK_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR        = '".$model->year."' AND ";
        $query .= "     SCHREGNO    = '".$model->schregno."' ";

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

    //担任データ
    function getOpinionsSignature($model) {
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
        $query .= "     T1.YEAR = '".$model->year."' ";
        $query .= "     AND T1.SCHREGNO = '".$model->schregno."' ";

        return $query;
    }

    //インサート
    function &getInsertSql($db, $model, $seq, $opinion, $signature) {

        if ($seq["CHAGE_OPI_SEQ"]) {
            $data["STAFFCD"][TEXT]     = STAFFCD;
            $data["CERT_NO"][NUMBER]   = 123456;
            $data["OPINION"][TEXT]     = $opinion;   //ハッシュ値
            $data["SIGNATURE"][TEXT]   = $signature; //署名
            $data["RESULT"][NUMBER]    = 0;
            $data["SCHREGNO"][TEXT]    = $model->schregno;
            $data["ACTION"][NUMBER]    = 1;
            $data["STATUS"][NUMBER]    = 1;
            $data["REGISTERCD"][TEXT]  = STAFFCD;
            $data["UPDATED"][FUNC]     = "sysdate()";

            $where  = " WHERE ";
            $where .= "     YEAR = '".$model->year."' ";
            $where .= "     AND SEQ = ".$seq["CHAGE_OPI_SEQ"]." ";

            $query = Query::updateSQL($data, "ATTEST_OPINIONS_DAT", $where);
            $db->query($query);
        }

        if ($seq["LAST_OPI_SEQ"]) {
            $data["STAFFCD"][TEXT]     = STAFFCD;
            $data["CERT_NO"][NUMBER]   = 123456;
            $data["OPINION"][TEXT]     = $opinion;   //ハッシュ値
            $data["SIGNATURE"][TEXT]   = $signature; //署名
            $data["RESULT"][NUMBER]    = 0;
            $data["SCHREGNO"][TEXT]    = $model->schregno;
            $data["ACTION"][NUMBER]    = 1;
            $data["STATUS"][NUMBER]    = 1;
            $data["REGISTERCD"][TEXT]  = STAFFCD;
            $data["UPDATED"][FUNC]     = "sysdate()";

            $where  = " WHERE ";
            $where .= "     YEAR = '".$model->year."' ";
            $where .= "     AND SEQ = ".$seq["LAST_OPI_SEQ"]." ";

            $query = Query::updateSQL($data, "ATTEST_OPINIONS_DAT", $where);
            $db->query($query);
        }

    }

    function getDeleteWk($model) {
        $query = "DELETE FROM ATTEST_OPINIONS_WK WHERE YEAR = '".CTRL_YEAR."' AND SCHREGNO = '".$model->schregno."'";

        return $query;
    }

    function getSeqNo($model) {
        $query = "SELECT CHAGE_OPI_SEQ, LAST_OPI_SEQ FROM ATTEST_OPINIONS_WK WHERE YEAR = '".$model->year."' AND SCHREGNO = '".$model->schregno."'";

        return $query;
    }

    //更新処理
    function &getInsUpdSql($db, $model, $seq, $opinion, $signature) {

        $data["YEAR"][TEXT]        = $model->year;
        $data["SEQ"][NUMBER]       = $seq;
        $data["STAFFCD"][TEXT]     = STAFFCD;
        $data["CERT_NO"][NUMBER]   = 123456;
        $data["OPINION"][TEXT]     = $opinion;   //ハッシュ値
        $data["SIGNATURE"][TEXT]   = $signature; //署名
        $data["RESULT"][NUMBER]    = 0;
        $data["SCHREGNO"][TEXT]    = $model->schregno;
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
        $where .= "     YEAR = '".$model->year."' ";
        $where .= "     AND SCHREGNO = '".$model->schregno."' ";

        $db->query(Query::updateSQL($data, "ATTEST_OPINIONS_WK", $where));

    }


    //学習記録データ取得
    function getStudyRec($model) {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     SCHREG_STUDYREC_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR     = '".$model->year."' AND ";
        $query .= "     SCHREGNO = '".$model->schregno."' ";

        return $query;
    }

    //ホームルーム出席履歴データ取得
    function getHrAttend($model) {
        $query  = " SELECT ";
        $query .= "     COUNT(DISTINCT EXECUTEDATE) ";
        $query .= " FROM ";
        $query .= "     HR_ATTEND_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR     = '".$model->year."' AND ";
        $query .= "     SCHREGNO = '".$model->schregno."' ";

        return $query;
    }
}
?>
