<?php

require_once('for_php7.php');

class knja120bQuery extends Query {

    //京都判定
    function getNameMst($cd)
    {
        $query  = " SELECT ";
        $query .= "     NAME1 ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = '".$cd."' ";
        $query .= " AND NAMECD2 = '00' ";

        return $query;
    }
    

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
    
    //3年次以上の情報取得
    function getNameMstGradecdCheck($flg)
    {
        $query  = " SELECT ";
        if ($flg === 'start') {
            $query .= "     NAMESPARE2 ";
        } else {
            $query .= "     NAMESPARE3 ";
        }
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = 'A023' ";
        $query .= " AND NAME1 = 'H' ";

        return $query;
    }

    //学年取得
    function getGradeCd($model) {
        
        $query  = " SELECT ";
        $query .= "     L1.GRADE_CD ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1";
        $query .= "     LEFT JOIN SCHREG_REGD_GDAT L1 ON L1.YEAR = T1.YEAR ";
        $query .= "                                  AND L1.GRADE = T1.GRADE ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '{$model->exp_year}' ";
        $query .= " AND T1.SCHREGNO = '{$model->schregno}' ";
        $query .= " AND T1.SEMESTER = '{$model->exp_semester}' ";

        return $query;
    }

    //調査書取込(3年次)
    function getHexamEntremark($model)
    {
        $query  = " SELECT ";
        $query .= "     T1.ATTENDREC_REMARK, ";
        $query .= "     T1.SPECIALACTREC, ";
        $query .= "     T1.TRAIN_REF, ";
        $query .= "     T1.TRAIN_REF1, ";
        $query .= "     T1.TRAIN_REF2, ";
        $query .= "     T1.TRAIN_REF3, ";
        //年度用
        $query .= "     T1.TOTALSTUDYACT AS TOTALSTUDYACT_YEAR, ";
        $query .= "     T1.TOTALSTUDYVAL AS TOTALSTUDYVAL_YEAR, ";
        //通年用
        $query .= "     L1.TOTALSTUDYACT, ";
        $query .= "     L1.TOTALSTUDYVAL ";
        $query .= " FROM ";
        $query .= "     HEXAM_ENTREMARK_DAT T1 ";
        $query .= "     LEFT JOIN HEXAM_ENTREMARK_HDAT L1 ON L1.SCHREGNO = T1.SCHREGNO ";
        $query .= " WHERE ";
        $query .= "       T1.YEAR      = '{$model->exp_year}' ";
        $query .= "   AND T1.SCHREGNO  = '{$model->schregno}' ";
        return $query;
    }

    //通知票取込(1, 2年次)
    function get_record_totalstudytime_dat($model) {
    
        $setyear = "";
        $setyear = $model->exp_year == "" ? CTRL_YEAR : $model->exp_year;
        
        $query  = " SELECT ";
        $query .= "     TOTALSTUDYTIME, ";
        $query .= "     TOTALSTUDYACT ";
        $query .= " FROM ";
        $query .= "     RECORD_TOTALSTUDYTIME_DAT ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO = '{$model->schregno}' ";
        $query .= " AND YEAR =  '{$setyear}' ";
        //通知表取込ボタンでのデータ取得（9学期のみのプロパティ)
        if ($model->Properties["tutisyoSougoushokengakki"] == '1') {
            $query .= " AND SEMESTER = '9' ";
        }
        $query .= " ORDER BY ";
        $query .= "     YEAR, ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     CLASSCD, ";
            $query .= "     SCHOOL_KIND, ";
            $query .= "     CURRICULUM_CD, ";
        }
        $query .= "     SUBCLASSCD, ";
        $query .= "     SEMESTER ";
        return $query;
    }

    //１レコード取得
    function getTrainRow($model, $flg)
    {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     HTRAINREMARK_DAT ";
        $query .= " WHERE ";
        if ($flg !== 'sanshou') {
            $query .= "     YEAR     = '".$model->exp_year."' AND ";
        }
        $query .= "     SCHREGNO = '".$model->schregno."' ";
        if ($flg === 'sanshou') {
            $query .= " ORDER BY ";
            $query .= "     YEAR ";
        }

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

    //出欠備考 -- 通知票取込
    function getHreportremarkDetailDat($db, $model) {

        $year = ($model->exp_year == "") ? CTRL_YEAR : $model->exp_year;

        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     HREPORTREMARK_DETAIL_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR        = '".$year."' AND ";
        $query .= "     SEMESTER    = '9' AND ";
        $query .= "     SCHREGNO    = '".$model->schregno."' AND ";
        $query .= "     DIV         = '06' AND ";
        $query .= "     CODE IN ('01','02','03','04','05') AND ";
        $query .= "     REMARK2     = '1' ";
        $query .= " ORDER BY ";
        $query .= "     CODE ";

        $result = $db->query($query);
        $set_remark = "";
        $sep = "";
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            if (strlen($row["REMARK1"]) > 0) {
                $set_remark .= $sep.$row["REMARK1"];
                $sep = "、";
            }
        }

        return $set_remark;
    }

}
?>
