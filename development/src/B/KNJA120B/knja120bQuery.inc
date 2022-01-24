<?php
class knja120bQuery extends Query {

    //近大判定用
    function getKindaiJudgment($model)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     SCHOOL_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$model->exp_year."' AND ";
        $query .= "     SCHOOLNAME1 LIKE '%近畿大学%' ";

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

        $query = knja120bQuery::getAnnual($model);

        $data["YEAR"][TEXT]                 = $model->exp_year;
        $data["SCHREGNO"][TEXT]             = $model->schregno;
        $data["ANNUAL"][TEXT]               = $db->getOne($query);
        $data["TOTALSTUDYACT"][TEXT]        = $fields["TOTALSTUDYACT"];
        $data["TOTALSTUDYVAL"][TEXT]        = $fields["TOTALSTUDYVAL"];
        $data["SPECIALACTREMARK"][TEXT]     = $fields["SPECIALACTREMARK"];
        $data["TOTALREMARK"][TEXT]          = $fields["TOTALREMARK"];
        $data["ATTENDREC_REMARK"][TEXT]     = $fields["ATTENDREC_REMARK"];
        $data["REGISTERCD"][TEXT]           = STAFFCD;
        $data["UPDATED"][FUNC]            = "sysdate()";

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

    //部活動参照
    function getClub($model) {
        $sdate = $model->exp_year . '-04-01';
        $edate = ($model->exp_year + 1) . '-03-31';

        $query  = " SELECT ";
        $query .= "     T2.CLUBNAME, ";
        $query .= "     T1.SDATE, ";
        $query .= "     T1.EDATE, ";
        $query .= "     T3.NAME1 ";
        $query .= " FROM ";
        $query .= "     SCHREG_CLUB_HIST_DAT T1 ";
        $query .= " LEFT OUTER JOIN ";
        $query .= "     CLUB_MST T2 ON  T1.CLUBCD = T2.CLUBCD ";
        $query .= " LEFT OUTER JOIN ";
        $query .= "     NAME_MST T3 ON  T3.NAMECD1     = 'J001' ";
        $query .= "                 AND T1.EXECUTIVECD = T3.NAMECD2 ";
        $query .= " WHERE ";
        $query .= "     T1.SCHREGNO = '{$model->schregno}' AND ";
        $query .= "     ( ";
        $query .= "         (T1.SDATE                      <= '{$sdate}' AND VALUE(T1.EDATE, '9999-12-31') >= '{$edate}') OR ";
        $query .= "         (T1.SDATE                      >= '{$sdate}' AND T1.SDATE                      <= '{$edate}') OR ";
        $query .= "         (VALUE(T1.EDATE, '9999-12-31') >= '{$sdate}' AND VALUE(T1.EDATE, '9999-12-31') <= '{$edate}') ";
        $query .= "     ) ";
        $query .= " ORDER BY ";
        $query .= "     T1.SDATE ";

        return $query;
    }

    //委員会活動参照
    function getCommittee($model) {
        $query  = " SELECT ";
        $query .= "     T1.GRADE, ";
        $query .= "     T1.SEMESTER, ";
        $query .= "     T4.NAME1 AS SEMESTERNAME, ";
        $query .= "     T1.SEQ, ";
        $query .= "     T2.COMMITTEENAME, ";
        $query .= "     T1.CHARGENAME, ";
        $query .= "     T3.NAME1 ";
        $query .= " FROM ";
        $query .= "     SCHREG_COMMITTEE_HIST_DAT T1 ";
        $query .= " LEFT OUTER JOIN ";
        $query .= "     COMMITTEE_MST T2 ON  T1.COMMITTEE_FLG = T2.COMMITTEE_FLG ";
        $query .= "                      AND T1.COMMITTEECD   = T2.COMMITTEECD ";
        $query .= " LEFT OUTER JOIN ";
        $query .= "     NAME_MST T3 ON  T3.NAMECD1     = 'J002' ";
        $query .= "                 AND T1.EXECUTIVECD = T3.NAMECD2 ";
        $query .= " LEFT OUTER JOIN ";
        $query .= "     NAME_MST T4 ON  T4.NAMECD1      = 'J004' ";
        $query .= "                 AND T1.SEMESTER     = T4.NAMECD2 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR     = '{$model->exp_year}' AND ";
        $query .= "     T1.SCHREGNO = '{$model->schregno}' ";
        $query .= " ORDER BY ";
        $query .= "     T1.GRADE, ";
        $query .= "     T1.SEMESTER, ";
        $query .= "     T1.SEQ ";

        return $query;
    }

    //学籍資格データよりデータを取得
    function getAward($model) {
        $sdate = $model->exp_year . '-04-01';
        $edate = ($model->exp_year + 1) . '-03-31';

        if ($model->Properties["useQualifiedMst"] == '1') {
            $query  = " SELECT ";
            $query .= "     T1.YEAR, ";
            $query .= "     T1.SCHREGNO, ";
            $query .= "     T1.SEQ, ";
            $query .= "     T1.REGDDATE, ";
            $query .= "     T1.SUBCLASSCD, ";
            $query .= "     CASE L1.CONDITION_DIV ";
            $query .= "          WHEN '1' ";
            $query .= "          THEN '国家資格' ";
            $query .= "          WHEN '2' ";
            $query .= "          THEN '公的資格' ";
            $query .= "          WHEN '3' ";
            $query .= "          THEN '民間資格' ";
            $query .= "          ELSE 'その他' ";
            $query .= "     END AS CONDITION_DIV, ";
            $query .= "     T1.QUALIFIED_CD, ";
            $query .= "     L1.QUALIFIED_NAME, ";
            $query .= "     L1.PROMOTER, ";
            $query .= "     T1.CONTENTS, ";
            $query .= "     L2.NAME1 AS RANK, ";
            $query .= "     T1.REMARK, ";
            $query .= "     T1.CREDITS ";
            $query .= " FROM ";
            $query .= "     SCHREG_QUALIFIED_HOBBY_DAT T1 ";
            $query .= " LEFT JOIN ";
            $query .= "     QUALIFIED_MST L1 ON L1.QUALIFIED_CD = T1.QUALIFIED_CD ";
            $query .= " LEFT JOIN ";
            $query .= "     NAME_MST L2 ON  L2.NAMECD2 = T1.RANK ";
            $query .= "                 AND L2.NAMECD1 = 'H312' ";
            $query .= " WHERE ";
            $query .= "     T1.SCHREGNO = '{$model->schregno}' AND ";
            $query .= "     T1.REGDDATE BETWEEN '{$sdate}' AND '{$edate}' ";
            $query .= " ORDER BY ";
            $query .= "     T1.REGDDATE, ";
            $query .= "     T1.SUBCLASSCD, ";
            $query .= "     T1.SEQ ";
        } else {
            $query  = " SELECT ";
            $query .= "     T1.year, ";
            $query .= "     T1.regddate, ";
            $query .= "     T1.subclasscd, ";
            $query .= "     CASE WHEN T1.CONDITION_DIV = '1' ";
            $query .= "               THEN '資格' ";
            $query .= "               ELSE 'その他' ";
            $query .= "          END AS CONDITION_DIV, ";
            $query .= "     T1.SEQ, ";
            $query .= "     T1.contents, ";
            $query .= "     T1.remark, ";
            $query .= "     T1.credits, ";
            $query .= "     T5.CREDITS AS CREDIT ";
            $query .= " FROM ";
            $query .= "     schreg_qualified_hobby_dat T1 ";
            $query .= "     LEFT OUTER JOIN subclass_mst T2 ON T1.subclasscd = T2.subclasscd ";
            $query .= "     LEFT JOIN SCHREG_REGD_DAT T4 ON T4.SCHREGNO = T1.SCHREGNO ";
            $query .= "                                 AND T4.YEAR     = T1.year ";
            $query .= "                                 AND T4.SEMESTER = '".CTRL_SEMESTER."' ";
            $query .= "     LEFT JOIN CREDIT_MST T5 ON T5.YEAR       = T1.year ";
            $query .= "                            AND T5.COURSECD   = T4.COURSECD ";
            $query .= "                            AND T5.MAJORCD    = T4.MAJORCD ";
            $query .= "                            AND T5.GRADE      = T4.GRADE ";
            $query .= "                            AND T5.COURSECODE = T4.COURSECODE ";
            $query .= "                            AND T5.CLASSCD    = SUBSTR(T1.SUBCLASSCD,1,2) ";
            $query .= "                            AND T5.SUBCLASSCD = T1.SUBCLASSCD ";
            $query .= " WHERE ";
            $query .= "     T1.SCHREGNO = '{$model->schregno}' AND ";
            $query .= "     T1.REGDDATE BETWEEN '{$sdate}' AND '{$edate}' ";
            $query .= " ORDER BY T1.regddate, T1.subclasscd, T1.SEQ ";
        }

        return $query;
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

    //名前
    function getName($schregno) {
        $query  = " SELECT ";
        $query .= "     NAME_SHOW ";
        $query .= " FROM ";
        $query .= "     SCHREG_BASE_MST ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO = '{$schregno}' ";

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

    //調査書の年度
    function getYear_tyousasyo($model) {
        $query  = " SELECT ";
        $query .= "     YEAR AS LABEL, ";
        $query .= "     YEAR AS VALUE ";
        $query .= " FROM ";
        $query .= "     HEXAM_ENTREMARK_DAT ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO = '{$model->schregno}' ";
        $query .= " ORDER BY ";
        $query .= "     YEAR ";

        return $query;
    }

    //調査書(進学用)出欠の記録参照
    function getAttendrecRemark($model) {
        $query  = " SELECT ";
        $query .= "     ATTENDREC_REMARK ";
        $query .= " FROM ";
        $query .= "     HEXAM_ENTREMARK_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$model->year}' AND ";
        $query .= "     SCHREGNO = '{$model->schregno}' ";

        return $query;
    }

    //学習記録データ取得
    function getStudyRec($model) {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     SCHREG_STUDYREC_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR     = '".$model->exp_year."' AND ";
        $query .= "     SCHREGNO = '".$model->schregno."' ";

        return $query;
    }

    //出欠記録データ取得
    function getAttendRec($model) {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     SCHREG_ATTENDREC_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR     = '".$model->exp_year."' AND ";
        $query .= "     SCHREGNO = '".$model->schregno."' ";

        return $query;
    }

    //名称取得
    function getNameMst($namecd1, $namecd2) {
        $query  = " SELECT ";
        $query .= "     NAME1 ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = '".$namecd1."' AND ";
        $query .= "     NAMECD2 = '".$namecd2."' ";

        return $query;
    }
}
?>
