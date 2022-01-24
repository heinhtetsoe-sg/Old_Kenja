<?php

require_once('for_php7.php');

class knja127hQuery extends Query
{
    //高セキュリティー
    public function getSecurityHigh()
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     MENU_HIGH_SECURITY_MST ";
        $query .= " WHERE ";
        $query .= "     PROGRAMID = 'KNJA127H' AND ";
        $query .= "     INVALID_FLG = '0' ";

        return $query;
    }

    //PROGRAMPATH取得
    public function getProgrampath()
    {
        $query  = " WITH MENU AS ( ";
        $query .= "     SELECT ";
        $query .= "         MIN(MENUID) AS MENUID ";
        $query .= "     FROM ";
        $query .= "         MENU_HIGH_SECURITY_MST ";
        $query .= "     WHERE ";
        $query .= "         PROGRAMID = 'KNJA127H' ";
        $query .= " ) ";

        $query .= " SELECT ";
        $query .= "     PROGRAMPATH ";
        $query .= " FROM ";
        $query .= "     MENU_HIGH_SECURITY_MST T1, ";
        $query .= "     MENU T2 ";
        $query .= " WHERE ";
        $query .= "     T1.MENUID    = T2.MENUID AND ";
        $query .= "     T1.PROGRAMID = 'KNJA127H' ";

        return $query;
    }

    //１レコード取得 -- HTRAINREMARK_DAT
    public function getTrainRow($model)
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

    //１レコード取得 -- HTRAINREMARK_HDAT
    public function getTrainHRow($model)
    {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     HTRAINREMARK_HDAT ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO = '".$model->schregno."' ";

        return $query;
    }

    //１レコード取得 -- HTRAINREMARK_DETAIL2
    public function getTrainDetailRow($model)
    {
        $query  = " SELECT ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     L2.REMARK1 AS ENT_DISABILITY_REMARK, ";
        $query .= "     L3.REMARK2 AS JUGYO_JISU ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1 ";
        $query .= "     LEFT JOIN HTRAINREMARK_DETAIL2_HDAT L2 ON T1.SCHREGNO   = L2.SCHREGNO ";
        $query .= "                                           AND L2.HTRAIN_SEQ = '001' ";
        $query .= "     LEFT JOIN HTRAINREMARK_DETAIL2_DAT L3 ON T1.SCHREGNO   = L3.SCHREGNO ";
        $query .= "                                           AND L3.YEAR = '".$model->exp_year."' ";
        $query .= "                                           AND L3.HTRAIN_SEQ = '001' ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR     = '".$model->exp_year."' AND ";
        $query .= "     T1.SEMESTER = '".$model->exp_semester."' AND ";
        $query .= "     T1.SCHREGNO = '".$model->schregno."' ";

        return $query;
    }

    //学年取得
    public function getGradeCd($model)
    {
        $query  = " SELECT ";
        $query .= "     L1.GRADE_CD ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1";
        $query .= "     LEFT JOIN SCHREG_REGD_GDAT L1 ON L1.YEAR = T1.YEAR ";
        $query .= "                                  AND L1.GRADE = T1.GRADE ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR     = '".$model->exp_year."' AND ";
        $query .= "     T1.SCHREGNO = '".$model->schregno."' AND ";
        $query .= "     T1.SEMESTER = '".$model->exp_semester."' ";

        return $query;
    }

    //調査書より読込み
    public function getHexamEntremark($model)
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
        $query .= "     T1.YEAR      = '".$model->exp_year."' AND ";
        $query .= "     T1.SCHREGNO  = '".$model->schregno."' ";

        return $query;
    }

    //セット日々出欠備考
    public function getSemesRemark($model)
    {
        $query  = " SELECT ";
        $query .= "     REMARK1, ";
        $query .= "     CASE WHEN MONTH = '01' THEN '13' ";
        $query .= "          WHEN MONTH = '02' THEN '14' ";
        $query .= "          WHEN MONTH = '03' THEN '15' ";
        $query .= "          ELSE MONTH END AS SORT_MONTH ";
        $query .= " FROM ";
        $query .= "     ATTEND_SEMES_REMARK_DAT ";
        $query .= " WHERE ";
        $query .= "     COPYCD      = '0' AND ";
        $query .= "     YEAR        = '".CTRL_YEAR."' AND ";
        $query .= "     SCHREGNO    = '".$model->schregno."' ";
        $query .= " ORDER BY ";
        $query .= "     YEAR, ";
        $query .= "     SEMESTER, ";
        $query .= "     SORT_MONTH ";

        return $query;
    }

    //学校種別取得
    public function getSchoolKind($model)
    {
        $query  = " SELECT ";
        $query .= "     T2.SCHOOL_KIND ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1, ";
        $query .= "     SCHREG_REGD_GDAT T2 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR     = T2.YEAR AND ";
        $query .= "     T1.YEAR     = '$model->exp_year' AND ";
        $query .= "     T1.SEMESTER = '$model->exp_semester' AND ";
        $query .= "     T1.GRADE    = T2.GRADE AND ";
        $query .= "     T1.SCHREGNO = '$model->schregno' ";

        return $query;
    }

    //年組取得
    public function getGradeHrclass($model)
    {
        $query  = " SELECT ";
        $query .= "     GRADE || '-' || HR_CLASS AS VALUE";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1";
        $query .= " WHERE ";
        $query .= "     T1.YEAR     = '".$model->exp_year."' AND ";
        $query .= "     T1.SEMESTER = '".$model->exp_semester."' AND ";
        $query .= "     T1.SCHREGNO = '".$model->schregno."' ";

        return $query;
    }

    //障害の状態を取得
    public function getChallengedProfile($model)
    {
        $query  = "SELECT ";
        $query .= "    CHALLENGED_NAMES, ";
        $query .= "    CHALLENGED_STATUS ";
        $query .= "FROM ";
        $query .= "    SCHREG_CHALLENGED_PROFILE_MAIN_DAT ";
        $query .= "WHERE ";
        $query .= "    SCHREGNO = '".$model->schregno."' ";
        $query .= "AND RECORD_DATE = 'NEW' ";

        return $query;
    }

    //INSERT -- HTRAINREMARK_DAT
    public function &getInsertQuery($fields, $model)
    {
        $db = Query::dbCheckOut();

        //年次の取得
        $query  = " SELECT ";
        $query .= "     ANNUAL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO    = '".$model->schregno."' AND ";
        $query .= "     YEAR        = '".$model->exp_year."' AND ";
        $query .= "     SEMESTER    = '".$model->exp_semester."' ";

        $annual = $db->getOne($query);

        //INSERT -- HTRAINREMARK_DAT
        $data = array();
        $data["YEAR"][TEXT]                 = $model->exp_year;
        $data["SCHREGNO"][TEXT]             = $model->schregno;
        $data["ANNUAL"][TEXT]               = $annual;
        $data["TOTALSTUDYACT"][TEXT]        = $fields["TOTALSTUDYACT"];
        $data["TOTALSTUDYVAL"][TEXT]        = $fields["TOTALSTUDYVAL"];
        $data["TOTALREMARK"][TEXT]          = $fields["TOTALREMARK"];
        $data["ATTENDREC_REMARK"][TEXT]     = $fields["ATTENDREC_REMARK"];
        $data["REGISTERCD"][TEXT]           = STAFFCD;
        $data["UPDATED"][FUNC]              = "sysdate()";

        $db->query(Query::insertSQL($data, "HTRAINREMARK_DAT"));

        //INSERT -- HTRAINREMARK_DETAIL2_HDAT
        $data = array();
        $data["SCHREGNO"][TEXT]             = $model->schregno;
        $data["HTRAIN_SEQ"][TEXT]           = '001';
        $data["REMARK1"][TEXT]              = $fields["ENT_DISABILITY_REMARK"];
        $data["REGISTERCD"][TEXT]           = STAFFCD;
        $data["UPDATED"][FUNC]              = "sysdate()";

        $db->query(Query::insertSQL($data, "HTRAINREMARK_DETAIL2_HDAT"));

        //INSERT -- HTRAINREMARK_DETAIL2_DAT
        $data = array();
        $data["YEAR"][TEXT]                 = $model->exp_year;
        $data["SCHREGNO"][TEXT]             = $model->schregno;
        $data["HTRAIN_SEQ"][TEXT]           = '001';
        $data["REMARK2"][TEXT]              = $fields["JUGYO_JISU"];
        $data["REGISTERCD"][TEXT]           = STAFFCD;
        $data["UPDATED"][FUNC]              = "sysdate()";

        $db->query(Query::insertSQL($data, "HTRAINREMARK_DETAIL2_DAT"));

        Query::dbCheckIn($db);
        return;
    }

    //DELETE
    public function &getDeleteQuery($model)
    {
        $db = Query::dbCheckOut();

        //DELETE -- HTRAINREMARK_DAT
        $query  = " DELETE FROM ";
        $query .= "     HTRAINREMARK_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR        = '".$model->exp_year."' AND ";
        $query .= "     SCHREGNO    = '".$model->schregno."' ";

        $db->query($query);

        //DELETE -- HTRAINREMARK_DETAIL2_HDAT
        $query  = " DELETE FROM ";
        $query .= "     HTRAINREMARK_DETAIL2_HDAT ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO    = '".$model->schregno."' AND ";
        $query .= "     HTRAIN_SEQ  = '001' ";

        $db->query($query);

        //DELETE -- HTRAINREMARK_DETAIL2_DAT
        $query  = " DELETE FROM ";
        $query .= "     HTRAINREMARK_DETAIL2_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR        = '".$model->exp_year."' AND ";
        $query .= "     SCHREGNO    = '".$model->schregno."' AND ";
        $query .= "     HTRAIN_SEQ  = '001' ";

        $db->query($query);

        Query::dbCheckIn($db);
        return;
    }
}
