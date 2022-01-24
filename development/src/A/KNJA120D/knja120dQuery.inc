<?php

require_once('for_php7.php');
class knja120dQuery extends Query {

    function getSecurityHigh() {
        $query  = " SELECT ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     MENU_HIGH_SECURITY_MST ";
        $query .= " WHERE ";
        $query .= "     PROGRAMID = 'KNJA120D' ";
        $query .= "     AND INVALID_FLG = '0' ";

        return $query;
    }

    //近大判定用
    function getKindaiJudgment($model)
    {
        $db = Query::dbCheckOut();
        $query = "SELECT COUNT(*) FROM SCHOOL_MST WHERE YEAR = '".$model->exp_year."' AND SCHOOLNAME1 LIKE '%近畿大学%' ";
        $cnt = $db->getOne($query);
        Query::dbCheckIn($db);

        return $cnt;
    }

    //学校名取得の取得
    function getSchoolName($flg="") {
        $query  = " SELECT ";
        if ($flg) {
            $query .= "     NAME1 ";
        } else {
            $query .= "     NAME1, ";
            $query .= "     NAME2 ";
        }
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = 'Z010' AND ";
        $query .= "     NAMECD2 = '00' ";

        return $query;
    }

    //卒業可能学年か判定
    function getGraduationGrade($model) {
        //DB接続
        $db = Query::dbCheckOut();

        $query  = " SELECT ";
        $query .= "     L1.SCHOOL_KIND, ";
        $query .= "     CASE WHEN T1.GRADE BETWEEN L2.NAMESPARE2 AND L2.NAMESPARE3 THEN 1 ELSE 0 END AS FLG ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1 ";
        $query .= "     LEFT JOIN SCHREG_REGD_GDAT L1 ";
        $query .= "          ON L1.YEAR     = T1.YEAR ";
        $query .= "         AND L1.GRADE    = T1.GRADE ";
        $query .= "     LEFT JOIN NAME_MST L2 ";
        $query .= "          ON L2.NAMECD1  = 'A023' ";
        $query .= "         AND L2.NAME1    = L1.SCHOOL_KIND ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR     = '{$model->exp_year}' ";
        $query .= " AND T1.SCHREGNO = '{$model->schregno}' ";
        $query .= " AND T1.SEMESTER = '{$model->exp_semester}' ";

        $ret = $db->getRow($query, DB_FETCHMODE_ASSOC);

        Query::dbCheckIn($db);

        return $ret;
    }

    //調査書より読込
    function getHexamEntremark($model)
    {
        $db = Query::dbCheckOut();

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

        $row = $db->getRow($query,DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);
        return $row;
    }

    //調査書より読込み(6分割)
    function getHexamTrainRef($model)
    {
        $query  = "     SELECT ";
        $query .= "         TRAIN_SEQ, REMARK ";
        $query .= "     FROM ";
        $query .= "         HEXAM_ENTREMARK_TRAINREF_DAT ";
        $query .= "     WHERE ";
        $query .= "         YEAR = '{$model->exp_year}' ";
        $query .= "         AND SCHREGNO = '{$model->schregno}' ";
        $query .= "         AND TRAIN_SEQ BETWEEN '101' AND '106' ";
        $query .= "     ORDER BY TRAIN_SEQ ";

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
    function getTrainRow($schregno, $exp_year)
    {
        $db = Query::dbCheckOut();

        $query  = " SELECT ";
        $query .= "   T1.*, ";
        $query .= "   L1.TRAIN_REF1, ";
        $query .= "   L1.TRAIN_REF2, ";
        $query .= "   L1.TRAIN_REF3 ";
        $query .= " FROM ";
        $query .= "     HTRAINREMARK_DAT T1 ";
        $query .= "     LEFT JOIN HTRAINREMARK_DETAIL_DAT L1 ON T1.YEAR = L1.YEAR ";
        $query .= "          AND T1.SCHREGNO = L1.SCHREGNO ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".$exp_year."'";
        $query .= "     AND T1.SCHREGNO = '".$schregno."'";

        $row = $db->getRow($query,DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);
        return $row;
    }

    //学年名取得
    function getGradeName($model) {
        $query  = " SELECT ";
        $query .= "     GRADE_NAME1 ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_GDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR        = '".$model->exp_year."' AND ";
        $query .= "     GRADE       = '".$model->grade."' ";

        return $query;
    }

    //定型分マスター一覧取得
    function getHtrainRemarkTempDat($model) {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     HTRAINREMARK_TEMP_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR        = '".$model->exp_year."' AND ";
        $query .= "     GRADE       = '".$model->grade."' AND ";
        if ($model->cmd === 'teikei_act') {
            $query .= "     DATA_DIV    = '01' ";
        } else {
            $query .= "     DATA_DIV    = '02' ";
        }
        $query .= " ORDER BY ";
        $query .= "     PATTERN_CD ";

        return $query;
    }

    //INSERT
    function &getInsertQuery($fields, $schregno, $exp_year, $exp_semester)
    {
        $db = Query::dbCheckOut();

        $query = knja120dQuery::getDataCnt($schregno, $exp_year);
        $dataCnt = $db->getOne($query);

        //年次の取得
        $query  = "SELECT annual FROM schreg_regd_dat ";
        $query .= " WHERE schregno = '".$schregno."'";
        $query .= "   AND year = '".$exp_year."'";
        $query .= "   AND semester = '".$exp_semester."'";
        $setAnnual = $db->getOne($query);
        $data = array();
        if ($dataCnt > 0) {
            $data["ANNUAL"][TEXT]               = $setAnnual;
            $data["TOTALSTUDYACT"][TEXT]        = $fields["TOTALSTUDYACT"];
            $data["TOTALSTUDYVAL"][TEXT]        = $fields["TOTALSTUDYVAL"];
            $data["SPECIALACTREMARK"][TEXT]     = $fields["SPECIALACTREMARK"];
            $data["ATTENDREC_REMARK"][TEXT]     = $fields["ATTENDREC_REMARK"];
            $data["REGISTERCD"][TEXT]           = STAFFCD;
            $data["UPDATED"][FUNC]            = "sysdate()";

            $where  = " WHERE ";
            $where .= "   YEAR = '".$exp_year."' ";
            $where .= "   AND SCHREGNO = '".$schregno."' ";

            $db->query(Query::updateSQL($data, "HTRAINREMARK_DAT", $where));
        } else {
            $data["YEAR"][TEXT]                 = $exp_year;
            $data["SCHREGNO"][TEXT]             = $schregno;
            $data["ANNUAL"][TEXT]               = $setAnnual;
            $data["TOTALSTUDYACT"][TEXT]        = $fields["TOTALSTUDYACT"];
            $data["TOTALSTUDYVAL"][TEXT]        = $fields["TOTALSTUDYVAL"];
            $data["SPECIALACTREMARK"][TEXT]     = $fields["SPECIALACTREMARK"];
            $data["ATTENDREC_REMARK"][TEXT]     = $fields["ATTENDREC_REMARK"];
            $data["REGISTERCD"][TEXT]           = STAFFCD;
            $data["UPDATED"][FUNC]            = "sysdate()";

            $db->query(Query::insertSQL($data, "HTRAINREMARK_DAT"));
        }

        //詳細
        $query = knja120dQuery::getDetailDataCnt($schregno, $exp_year);
        $dataCnt = $db->getOne($query);

        $data = array();
        $fields["TRAIN_REF3"] = isset($fields["INS_COMMENTS"]) ? $fields["INS_COMMENTS_LABEL"] : $fields["TRAIN_REF3"];
        if ($dataCnt > 0) {
            $data["TRAIN_REF1"][TEXT]         = $fields["TRAIN_REF1"];
            $data["TRAIN_REF2"][TEXT]         = $fields["TRAIN_REF2"];
            $data["TRAIN_REF3"][TEXT]         = $fields["TRAIN_REF3"];
            $data["REGISTERCD"][TEXT]         = STAFFCD;
            $data["UPDATED"][FUNC]            = "sysdate()";

            $where  = " WHERE ";
            $where .= "   YEAR = '".$exp_year."' ";
            $where .= "   AND SCHREGNO = '".$schregno."' ";

            $db->query(Query::updateSQL($data, "HTRAINREMARK_DETAIL_DAT", $where));
        } else {
            $data["YEAR"][TEXT]                 = $exp_year;
            $data["SCHREGNO"][TEXT]             = $schregno;
            $data["TRAIN_REF1"][TEXT]           = $fields["TRAIN_REF1"];
            $data["TRAIN_REF2"][TEXT]           = $fields["TRAIN_REF2"];
            $data["TRAIN_REF3"][TEXT]           = $fields["TRAIN_REF3"];
            $data["REGISTERCD"][TEXT]           = STAFFCD;
            $data["UPDATED"][FUNC]            = "sysdate()";

            $db->query(Query::insertSQL($data, "HTRAINREMARK_DETAIL_DAT"));
        }

        Query::dbCheckIn($db);
        return ;
    }

    //DATA有無
    function getDataCnt($schregno, $exp_year)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     HTRAINREMARK_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$exp_year."'";
        $query .= "     AND SCHREGNO = '".$schregno."'";

        return $query;
    }

    //DETAILDATA有無
    function getDetailDataCnt($schregno, $exp_year)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     HTRAINREMARK_DETAIL_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$exp_year."'";
        $query .= "     AND SCHREGNO = '".$schregno."'";

        return $query;
    }

    //通知表所見より年度を取得
    function selectQueryYear($model)
    {
        $query  = "SELECT distinct t1.year ";
        $query .= "  FROM hreportremark_dat t1  ";
        $query .= " WHERE t1.schregno = '" .$model->schregno ."'  ";
        $query .= " ORDER BY t1.year desc ";

        return $query;
    }

    //通知表所見より再読込
    function selectQueryGuide($model)
    {
        $query  = " SELECT t1.totalstudytime,t1.specialactremark,t1.communication, t1.remark1, t1.remark2, t1.semester ";
        $query .= "   FROM hreportremark_dat t1 ";
        $query .= "  WHERE t1.schregno = '" .$model->schregno ."' ";
        $query .= "    and t1.year     = '" .$model->year_cmb ."' ";
        $query .= "  ORDER BY t1.semester";

        return $query;
    }

    //セット日々出欠備考
    function getSemesRemark($model) {
    
        //DB接続
        $db = Query::dbCheckOut();

        $query  = "  SELECT ";
        $query .= "      REMARK1, ";
        $query .= "      CASE WHEN MONTH = '01' THEN '13' WHEN MONTH = '02' THEN '14' WHEN MONTH = '03' THEN '15' ELSE MONTH END AS SORT_MONTH ";
        $query .= "  FROM ";
        $query .= "      ATTEND_SEMES_REMARK_DAT ";
        $query .= "  WHERE ";
        $query .= "      COPYCD = '0' ";
        $query .= "  AND YEAR = '".CTRL_YEAR."' ";
        $query .= "  AND SCHREGNO = '{$model->schregno}' ";
        $query .= "  ORDER BY ";
        $query .= "      YEAR, ";
        $query .= "      SEMESTER, ";
        $query .= "      SORT_MONTH ";
        
        $result = $db->query($query);
        $set_remark = "";
        $count = 0;
        while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            if ($count == 0) {
                $set_remark .= $row["REMARK1"];
            } else {
                if ($row["REMARK1"] != "") {
                    $set_remark .= "／".$row["REMARK1"];
                }
            }
            $count++;
        }        
        Query::dbCheckIn($db);
        
        return $set_remark;
    }
    
    //学年（年次）コンボボックス用(Subform4)
    function selectQueryAnnual($model, $JH="")
    {
        if ($model->control["学校区分"] == "0"){     //学年制
            $table = "V_REGDYEAR_GRADE_DAT";
        }else if ($model->control["学校区分"] == "1"){     //単位制
            $table = "V_REGDYEAR_UNIT_DAT";
        }
        $query  = " SELECT DISTINCT ";
        $query .= "     T1.YEAR, ";
        $query .= "     T1.ANNUAL ";
        $query .= " FROM ";
        $query .= "     ".$table." T1 ";
        $query .= " WHERE ";
        $query .= "     T1.SCHREGNO = '" .$model->schregno ."'";
        if($JH){
            $query .= "     AND T1.ANNUAL >= '04' ";
        }

        return $query;
    }

    //成績参照画面(Subform4)
    function selectQuerySubForm4($model, $JH="") {
        $query = " SELECT ";
        $query .= "   T1.ANNUAL, ";
        $query .= "   CASE WHEN T1.CLASSABBV IS NULL OR T1.CLASSABBV = '' THEN T3.CLASSABBV ELSE T1.CLASSABBV END AS CLASSNAME, ";
        $query .= "   CASE WHEN T1.SUBCLASSNAME IS NULL OR T1.SUBCLASSNAME = '' THEN T4.SUBCLASSNAME ELSE T1.SUBCLASSNAME END AS SUBCLASSNAME, ";
        $query .= "   T1.VALUATION, ";
        $query .= "   T1.GET_CREDIT, ";
        $query .= "   T1.ADD_CREDIT, ";
        $query .= "   T1.CLASSCD, ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "   T1.SCHOOL_KIND, ";
            $query .= "   T1.CURRICULUM_CD, ";
        }
        $query .= "   T1.SUBCLASSCD, ";
        $query .= "   T2.REMARK ";
        $query .= " FROM ";
        $query .= "   SCHREG_STUDYREC_DAT T1 ";
        $query .= "   LEFT OUTER JOIN STUDYRECREMARK_DAT T2 ON  ";
        $query .= "                   T1.YEAR       = T2.YEAR AND  ";
        $query .= "                   T1.SCHREGNO   = T2.SCHREGNO AND  ";
        $query .= "                   T1.CLASSCD    = T2.CLASSCD AND  ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "                   T1.SCHOOL_KIND    = T2.SCHOOL_KIND AND  ";
            $query .= "                   T1.CURRICULUM_CD  = T2.CURRICULUM_CD AND  ";
        }
        $query .= "                   T1.SUBCLASSCD = T2.SUBCLASSCD ";
        $query .= "   LEFT OUTER JOIN CLASS_MST    T3 ON  ";
        $query .= "                   T1.CLASSCD    = T3.CLASSCD  ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "               AND T1.SCHOOL_KIND    = T3.SCHOOL_KIND  ";
        }
        $query .= "   LEFT OUTER JOIN SUBCLASS_MST T4 ON  ";
        $query .= "                   T1.SUBCLASSCD = T4.SUBCLASSCD  ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "               AND T1.CLASSCD        = T4.CLASSCD  ";
            $query .= "               AND T1.SCHOOL_KIND    = T4.SCHOOL_KIND  ";
            $query .= "               AND T1.CURRICULUM_CD  = T4.CURRICULUM_CD  ";
        }
        $query .= " WHERE ";
        $query .= "   T1.SCHOOLCD = '0' AND ";
        if ($model->annual["YEAR"] != "0" || $model->annual["ANNUAL"] != "0"){
            $query .= "   T1.YEAR = '" .$model->annual["YEAR"] ."' AND ";
            $query .= "   T1.ANNUAL = '" .$model->annual["ANNUAL"]."' AND";
        }
        if($JH){
            $query .= "   T1.ANNUAL >= '04' AND ";
        }
        $query .= "   T1.SCHREGNO = '" .$model->schregno ."' ";
        $query .= " ORDER BY ";
        $query .= "     T1.CLASSCD, ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     T1.SCHOOL_KIND, ";
            $query .= "     T1.CURRICULUM_CD, ";
        }
        $query .= "     T1.SUBCLASSCD, ";
        $query .= "     T1.YEAR, ";
        $query .= "     T1.ANNUAL ";

        return $query;
    }

    //中高一貫校
    function getJH() {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = 'Z010' AND ";
        $query .= "     NAMECD2 = '00' AND ";
        $query .= "     NAMESPARE2 IS NOT NULL ";

        return $query;
    }

    //明治判定
    function getMeijiHantei() {
        $db = Query::dbCheckOut();

        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = 'Z010' AND ";
        $query .= "     NAMECD2 = '00' AND ";
        $query .= "     NAME1   = 'meiji' ";

        $meiji = $db->getOne($query);
        Query::dbCheckIn($db);
        return $meiji;
    }

    //学校判定用
    function getSchoolHantei() {
        $db = Query::dbCheckOut();

        $query  = " SELECT ";
        $query .= "     NAME1 ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = 'Z010' AND ";
        $query .= "     NAMECD2 = '00' ";

        $schoolName = $db->getOne($query);
        Query::dbCheckIn($db);
        return $schoolName;
    }

    //行動の記録取得
    function getActionDocumentDat($model) {
        $query  = " SELECT ";
        $query .= "     T1.ACTIONDATE, ";
        $query .= "     T1.SEQ, ";
        $query .= "     T1.ACTIONTIME, ";
        $query .= "     L1.NAME1 AS DIVIDECD, ";
        $query .= "     T1.TITLE, ";
        $query .= "     T1.TEXT ";
        $query .= " FROM ";
        $query .= "     ACTION_DOCUMENT_DAT T1 ";
        $query .= "     LEFT JOIN V_NAME_MST L1 ";
        $query .= "          ON L1.YEAR     = '".$model->exp_year."' ";
        $query .= "         AND L1.NAMECD1  = 'H307' ";
        $query .= "         AND L1.NAMECD2  = T1.DIVIDECD ";
        $query .= " WHERE ";
        $query .= "     T1.SCHREGNO = '".$model->schregno."' ";
        $query .= " ORDER BY ";
        $query .= "     T1.ACTIONDATE DESC, ";
        $query .= "     T1.ACTIONTIME DESC, ";
        $query .= "     T1.SEQ ";

        return $query;
    }
}
?>
