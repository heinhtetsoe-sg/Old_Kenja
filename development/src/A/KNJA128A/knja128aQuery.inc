<?php

require_once('for_php7.php');

class knja128aQuery extends Query
{
    //高セキュリティー
    public function getSecurityHigh()
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     MENU_HIGH_SECURITY_MST ";
        $query .= " WHERE ";
        $query .= "     PROGRAMID = 'KNJA128A' AND ";
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
        $query .= "         PROGRAMID = 'KNJA128A' ";
        $query .= " ) ";

        $query .= " SELECT ";
        $query .= "     PROGRAMPATH ";
        $query .= " FROM ";
        $query .= "     MENU_HIGH_SECURITY_MST T1, ";
        $query .= "     MENU T2 ";
        $query .= " WHERE ";
        $query .= "     T1.MENUID    = T2.MENUID AND ";
        $query .= "     T1.PROGRAMID = 'KNJA128A' ";

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

    //１レコード取得 -- HTRAINREMARK_DAT
    public function getTrainHRow($model)
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

    //１レコード取得 -- HTRAINREMARK_DETAIL2
    public function getTrainDetailRow($model)
    {
        $query  = " SELECT ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     L1.REMARK1 AS INDEPENDENT_REMARK, ";
        $query .= "     L2.REMARK1 AS ENT_DISABILITY_REMARK ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1 ";
        $query .= "     LEFT JOIN HTRAINREMARK_DETAIL2_DAT L1 ON T1.YEAR        = L1.YEAR ";
        $query .= "                                          AND T1.SCHREGNO    = L1.SCHREGNO ";
        $query .= "                                          AND L1.HTRAIN_SEQ  = '001' ";
        $query .= "     LEFT JOIN HTRAINREMARK_DETAIL2_HDAT L2 ON T1.SCHREGNO   = L2.SCHREGNO ";
        $query .= "                                           AND L2.HTRAIN_SEQ = '001' ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR     = '".$model->exp_year."' AND ";
        $query .= "     T1.SEMESTER = '".$model->exp_semester."' AND ";
        $query .= "     T1.SCHREGNO = '".$model->schregno."' ";

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
        $data["SPECIALACTREMARK"][TEXT]     = $fields["SPECIALACTREMARK"];
        $data["TOTALREMARK"][TEXT]          = $fields["TOTALREMARK"];
        $data["ATTENDREC_REMARK"][TEXT]     = $fields["ATTENDREC_REMARK"];
        $data["REGISTERCD"][TEXT]           = STAFFCD;
        $data["UPDATED"][FUNC]              = "sysdate()";

        $db->query(Query::insertSQL($data, "HTRAINREMARK_DAT"));

        //INSERT -- HTRAINREMARK_DETAIL2_DAT
        $data = array();
        $data["YEAR"][TEXT]                 = $model->exp_year;
        $data["SCHREGNO"][TEXT]             = $model->schregno;
        $data["HTRAIN_SEQ"][TEXT]           = '001';
        $data["REMARK1"][TEXT]              = $fields["INDEPENDENT_REMARK"];
        $data["REGISTERCD"][TEXT]           = STAFFCD;
        $data["UPDATED"][FUNC]              = "sysdate()";

        $db->query(Query::insertSQL($data, "HTRAINREMARK_DETAIL2_DAT"));

        //INSERT -- HTRAINREMARK_DETAIL2_HDAT
        $data = array();
        $data["SCHREGNO"][TEXT]             = $model->schregno;
        $data["HTRAIN_SEQ"][TEXT]           = '001';
        $data["REMARK1"][TEXT]              = $fields["ENT_DISABILITY_REMARK"];
        $data["REGISTERCD"][TEXT]           = STAFFCD;
        $data["UPDATED"][FUNC]              = "sysdate()";

        $db->query(Query::insertSQL($data, "HTRAINREMARK_DETAIL2_HDAT"));

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

        //DELETE -- HTRAINREMARK_DETAIL2_DAT
        $query  = " DELETE FROM ";
        $query .= "     HTRAINREMARK_DETAIL2_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR        = '".$model->exp_year."' AND ";
        $query .= "     SCHREGNO    = '".$model->schregno."' AND ";
        $query .= "     HTRAIN_SEQ  = '001' ";

        $db->query($query);

        //DELETE -- HTRAINREMARK_DETAIL2_HDAT
        $query  = " DELETE FROM ";
        $query .= "     HTRAINREMARK_DETAIL2_HDAT ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO    = '".$model->schregno."' AND ";
        $query .= "     HTRAIN_SEQ  = '001' ";

        $db->query($query);

        Query::dbCheckIn($db);
        return;
    }

    //学年（年次）取得(Subform4)
    public function selectQueryAnnual($model)
    {
        if ($model->control["学校区分"] == "0") {   //学年制
            $table = "V_REGDYEAR_GRADE_DAT";
        } elseif ($model->control["学校区分"] == "1") {    //単位制
            $table = "V_REGDYEAR_UNIT_DAT";
        }

        $query  = " SELECT DISTINCT ";
        $query .= "     T1.YEAR, ";
        $query .= "     T1.ANNUAL ";
        $query .= " FROM ";
        $query .= "     ".$table." T1, ";
        $query .= "     NAME_MST T2 ";
        $query .= " WHERE ";
        $query .= "     T1.SCHREGNO = '" .$model->schregno ."' AND ";
        $query .= "     T2.NAMECD1  = 'A023' AND ";
        $query .= "     T2.NAME1    = 'H' AND ";
        $query .= "     T1.GRADE BETWEEN T2.NAME2 AND T2.NAME3 ";

        return $query;
    }

    //成績参照画面(Subform4)
    public function selectQuerySubForm4($model)
    {
        $query  = " SELECT ";
        $query .= "   T1.ANNUAL, ";
        $query .= "   CASE WHEN T1.CLASSABBV IS NULL OR T1.CLASSABBV = '' THEN T3.CLASSABBV ELSE T1.CLASSABBV END AS CLASSNAME, ";
        $query .= "   CASE WHEN T1.SUBCLASSNAME IS NULL OR T1.SUBCLASSNAME = '' THEN T4.SUBCLASSNAME ELSE T1.SUBCLASSNAME END AS SUBCLASSNAME, ";
        $query .= "   T1.VALUATION, ";
        $query .= "   T1.GET_CREDIT, ";
        $query .= "   T1.ADD_CREDIT, ";
        $query .= "   T1.CLASSCD, ";
        $query .= "   T1.SCHOOL_KIND, ";
        $query .= "   T1.CURRICULUM_CD, ";
        $query .= "   T1.SUBCLASSCD, ";
        $query .= "   T2.REMARK ";
        $query .= " FROM ";
        $query .= "   SCHREG_STUDYREC_DAT T1 ";
        $query .= "   LEFT JOIN STUDYRECREMARK_DAT T2 ON T1.YEAR            = T2.YEAR ";
        $query .= "                                  AND T1.SCHREGNO        = T2.SCHREGNO ";
        $query .= "                                  AND T1.CLASSCD         = T2.CLASSCD ";
        $query .= "                                  AND T1.SCHOOL_KIND     = T2.SCHOOL_KIND ";
        $query .= "                                  AND T1.CURRICULUM_CD   = T2.CURRICULUM_CD ";
        $query .= "                                  AND T1.SUBCLASSCD      = T2.SUBCLASSCD ";
        $query .= "   LEFT JOIN CLASS_MST T3 ON T1.CLASSCD      = T3.CLASSCD ";
        $query .= "                         AND T1.SCHOOL_KIND  = T3.SCHOOL_KIND ";
        $query .= "   LEFT JOIN SUBCLASS_MST T4 ON T1.SUBCLASSCD    = T4.SUBCLASSCD ";
        $query .= "                            AND T1.CLASSCD       = T4.CLASSCD  ";
        $query .= "                            AND T1.SCHOOL_KIND   = T4.SCHOOL_KIND  ";
        $query .= "                            AND T1.CURRICULUM_CD = T4.CURRICULUM_CD  ";
        $query .= " WHERE ";
        $query .= "     T1.SCHOOLCD = '0' AND ";
        if ($model->annual["YEAR"] != "0" || $model->annual["ANNUAL"] != "0") {
            $query .= "     T1.YEAR   = '".$model->annual["YEAR"]."' AND ";
            $query .= "     T1.ANNUAL = '".$model->annual["ANNUAL"]."' AND ";
        }
        $query .= "     T1.SCHREGNO = '".$model->schregno."' ";
        $query .= " ORDER BY ";
        $query .= "     T1.CLASSCD, ";
        $query .= "     T1.SCHOOL_KIND, ";
        $query .= "     T1.CURRICULUM_CD, ";
        $query .= "     T1.SUBCLASSCD, ";
        $query .= "     T1.YEAR, ";
        $query .= "     T1.ANNUAL ";

        return $query;
    }
    //既入力内容の参照2
    public function selectKinyuuyroku1($model, $annual)
    {
        $query .= " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     HTRAINREMARK_DAT T1 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '{$model->exp_year}' ";
        $query .= "     AND T1.SCHREGNO = '{$model->schregno}' ";
        $query .= "     AND T1.ANNUAL = '{$annual}' ";

        return $query;
    }
    //既入力内容の参照2
    public function selectKinyuuyroku2($model, $grade)
    {
        $query .= " SELECT DISTINCT ";
        $query .= "     T2.* ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1 ";
        $query .= " LEFT JOIN ";
        $query .= "     HTRAINREMARK_DETAIL2_DAT T2 ";
        $query .= "     ON T1.YEAR = T2.YEAR ";
        $query .= "     AND T1.SCHREGNO = T2.SCHREGNO ";
        $query .= "     AND T2.HTRAIN_SEQ = '001' ";
        $query .= " WHERE ";
        $query .= "     T1.SCHREGNO = '{$model->schregno}' ";
        $query .= "     AND T1.GRADE = '{$grade}' ";

        return $query;
    }
    //年度の取得
    public function selectGrade($model)
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     T1.YEAR, ";
        $query .= "     T1.GRADE, ";
        $query .= "     T1.GRADE_NAME1 ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_GDAT T1 ";
        $query .= " WHERE ";
        $query .= "     T1.SCHOOL_KIND = 'A' ";
        $query .= "     AND YEAR = '{$model->exp_year}' ";
        $query .= " ORDER BY ";
        $query .= "     T1.GRADE ";
        $query .= " FETCH FIRST 2 ROWS ONLY ";

        return $query;
    }
}
