<?php

require_once('for_php7.php');

class knja121bQuery extends Query {

    //１レコード取得
    function getTrainRow($model)
    {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     HTRAINREMARK_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR     = '".$model->exp_year."' AND ";
        $query .= "     SCHREGNO = '".$model->schregno."' ";

        return $query;
    }

    //署名チェック
    function getOpinionsWk($model)
    {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     ATTEST_OPINIONS_WK ";
        $query .= " WHERE ";
        $query .= "     YEAR     = '".$model->exp_year."' AND ";
        $query .= "     SCHREGNO = '".$model->schregno."' ";

        return $query;
    }

    //ハッシュ値取得
    function getOpinionsDat($model)
    {
        $query  = " SELECT ";
        $query .= "     T1.CHAGE_OPI_SEQ, ";
        $query .= "     T2.OPINION ";
        $query .= " FROM ";
        $query .= "     ATTEST_OPINIONS_WK T1, ";
        $query .= "     ATTEST_OPINIONS_DAT T2 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = T2.YEAR AND ";
        $query .= "     T1.YEAR = '".$model->exp_year."' AND ";
        $query .= "     T1.SCHREGNO = '".$model->schregno."' AND ";
        $query .= "     T1.CHAGE_OPI_SEQ = T2.SEQ ";

        return $query;
    }

    //インサート
    function &getInsertSql($db, $model, $fields) {

        $query = knja121bQuery::getAnnual($model);

        $data["YEAR"][TEXT]                 = $model->exp_year;
        $data["SCHREGNO"][TEXT]             = $model->schregno;
        $data["ANNUAL"][TEXT]               = $db->getOne($query);
        $data["TOTALSTUDYACT"][TEXT]        = $fields["TOTALSTUDYACT"];
        $data["TOTALSTUDYVAL"][TEXT]        = $fields["TOTALSTUDYVAL"];
        $data["VIEWREMARK"][TEXT]           = $fields["VIEWREMARK"];
        $data["TOTALREMARK"][TEXT]          = $fields["TOTALREMARK"];
        $data["ATTENDREC_REMARK"][TEXT]     = $fields["ATTENDREC_REMARK"];
        $data["REGISTERCD"][TEXT]           = STAFFCD;
        $data["UPDATED"][FUNC]              = "sysdate()";

        $db->query(Query::insertSQL($data, "HTRAINREMARK_DAT"));
    }

    //年次取得
    function getAnnual($model) {

        $query  = " SELECT";
        $query .= "     ANNUAL ";
        $query .= " FROM";
        $query .= "     SCHREG_REGD_DAT ";
        $query .= " WHERE";
        $query .= "     YEAR = '".$model->exp_year."' AND ";
        $query .= "     SEMESTER = '".$model->exp_semester."' AND ";
        $query .= "     SCHREGNO = '".$model->schregno."'";

        return $query;
    }

    //DELETE
    function &getDeleteSql($db, $model)
    {
        $query  = " DELETE FROM HTRAINREMARK_DAT WHERE YEAR = '".$model->exp_year."' AND SCHREGNO = '".$model->schregno."' ";

        $db->query($query);
        return ;
    }

    //全体のデータの取得
    function get_hreportremark_dat($model, $semester) {
        $query  = " WITH COMMUNICATION AS ( ";
        $query .= "     SELECT ";
        $query .= "         YEAR, ";
        $query .= "         SCHREGNO, ";
        $query .= "         COMMUNICATION ";
        $query .= "     FROM ";
        $query .= "         HREPORTREMARK_DAT ";
        $query .= "     WHERE ";
        $query .= "         SEMESTER = '{$model->field["SEMESTER"]}' ";

        $query .= " ), SCHINFO AS ( ";
        $query .= "     SELECT ";
        $query .= "         T1.SCHREGNO, ";
        $query .= "         T1.ATTENDNO, ";
        $query .= "         T2.NAME ";
        $query .= "     FROM ";
        $query .= "         SCHREG_REGD_DAT T1, ";
        $query .= "         SCHREG_BASE_MST T2 ";
        $query .= "     WHERE ";
        $query .= "         T1.SEMESTER = '".$semester."' AND ";
        $query .= "         T1.GRADE || T1.HR_CLASS = '".$model->field["GRADE_HR_CLASS"]."' AND ";
        $query .= "         T1.SCHREGNO = T2.SCHREGNO ";
        $query .= " ) ";

        $query .= " SELECT ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     T1.ATTENDNO, ";
        $query .= "     T1.NAME, ";
        $query .= "     T2.COMMUNICATION AS REMARK";
        $query .= " FROM ";
        $query .= "     SCHINFO T1 ";
        $query .= " LEFT JOIN COMMUNICATION T2 ON T1.SCHREGNO = T2.SCHREGNO ";
        $query .= " ORDER BY ";
        $query .= "     T1.ATTENDNO ";

        return $query;
    }

    //年度コンボ
    function getYear($schregno) {
        $query  = " SELECT ";
        $query .= "     YEAR AS LABEL, ";
        $query .= "     YEAR AS VALUE ";
        $query .= " FROM ";
        $query .= "     HREPORTREMARK_DAT ";
        $query .= " WHERE ";
        $query .= "       SCHREGNO = '{$schregno}' ";
        $query .= " GROUP BY ";
        $query .= "     YEAR ";
        $query .= " ORDER BY ";
        $query .= "     YEAR ";

        return $query;
    }

    //学期名称取得
    function getSemester($year, $semester) {
        $query  = " SELECT ";
        $query .= "     SEMESTERNAME ";
        $query .= " FROM ";
        $query .= "     SEMESTER_MST T1 ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$year."' AND ";
        $query .= "     SEMESTER = '".$semester."' ";

        return $query;
    }

    //通知票所見
    function getRemark($schregno, $year) {
        $query  = " SELECT ";
        $query .= "     SEMESTER, ";
        $query .= "     TOTALSTUDYTIME, ";
        $query .= "     SPECIALACTREMARK, ";
        $query .= "     COMMUNICATION, ";
        $query .= "     REMARK1, ";
        $query .= "     REMARK2, ";
        $query .= "     REMARK3 ";
        $query .= " FROM ";
        $query .= "     HREPORTREMARK_DAT ";
        $query .= " WHERE ";
        $query .= "       YEAR     = '{$year}' ";
        $query .= "   AND SCHREGNO = '{$schregno}' ";

        return $query;
    }

    //行動記録の取得
    function getBehavior($model)
    {
		$query  = " SELECT ";
		$query .= "     DIV, ";
		$query .= "     CODE, ";
		$query .= "     ANNUAL, ";
		$query .= "     RECORD ";
		$query .= " FROM ";
		$query .= "     BEHAVIOR_DAT ";
		$query .= " WHERE ";
		$query .= "     YEAR = '".$model->exp_year."' AND ";
		$query .= "     SCHREGNO = '".$model->schregno."' ";
		$query .= " ORDER BY ";
		$query .= "     DIV, ";
		$query .= "     CODE ";

        return $query;
    }

    //INSERT
    function &getInsertQuery_koudou($model)
    {
        $db = Query::dbCheckOut();

        //年次の取得
        $query  = " SELECT ANNUAL ";
        $query .= " FROM SCHREG_REGD_DAT ";
        $query .= " WHERE SCHREGNO = '".$model->schregno."'";
        $query .= "   AND YEAR = '".$model->exp_year."'";
        $query .= "   AND SEMESTER = '".$model->exp_semester."'";

        $annual = $db->getOne($query);

        //行動の記録
        for($i=1; $i<11; $i++)
        {
            $ival = "1" . sprintf("%02d", $i);
            if ($model->record["RECORD"][$ival] != "1") continue;

            $data["YEAR"][TEXT]         = $model->exp_year;
            $data["SCHREGNO"][TEXT]     = $model->schregno;
            $data["DIV"][TEXT]          = "1";
            $data["CODE"][TEXT]         = sprintf("%02d", $i);
            $data["ANNUAL"][TEXT]       = $annual;
            $data["RECORD"][TEXT]       = "1";
            $data["REGISTERCD"][TEXT]   = STAFFCD;
            $data["UPDATED"][FUNC]      = "sysdate()";

            $db->query(Query::insertSQL($data, "BEHAVIOR_DAT"));
        }
        //特別活動の記録
        for($i=1; $i<4; $i++)
        {
            $ival = "2" . sprintf("%02d", $i);
            if ($model->record["RECORD"][$ival] != "1") continue;

            $data["YEAR"][TEXT]         = $model->exp_year;
            $data["SCHREGNO"][TEXT]     = $model->schregno;
            $data["DIV"][TEXT]          = "2";
            $data["CODE"][TEXT]         = sprintf("%02d", $i);
            $data["ANNUAL"][TEXT]       = $annual;
            $data["RECORD"][TEXT]       = "1";
            $data["REGISTERCD"][TEXT]   = STAFFCD;
            $data["UPDATED"][FUNC]      = "sysdate()";

            $db->query(Query::insertSQL($data, "BEHAVIOR_DAT"));
        }

        Query::dbCheckIn($db);
        return ;
    }

    //DELETE
    function &getDeleteQuery_koudou($model)
    {
        $db = Query::dbCheckOut();

        $query  = " DELETE FROM BEHAVIOR_DAT ";
        $query .= " WHERE YEAR = '".$model->exp_year."'";
        $query .= "   AND SCHREGNO = '".$model->schregno."'";

        $db->query($query);

        Query::dbCheckIn($db);
        return;
    }
}
?>
