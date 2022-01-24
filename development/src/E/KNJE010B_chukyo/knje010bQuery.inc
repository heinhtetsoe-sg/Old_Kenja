<?php

require_once('for_php7.php');

class knje010bQuery extends Query {

    function getSecurityHigh() {
        $query  = " SELECT ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     MENU_HIGH_SECURITY_MST ";
        $query .= " WHERE ";
        $query .= "     PROGRAMID = 'KNJE010B' ";
        $query .= "     AND INVALID_FLG = '0' ";

        return $query;
    }

    //学校名取得の取得(テストコンボで使う)
    function getSchoolName() {
        $query  = " SELECT ";
        $query .= "     NAME1, ";
        $query .= "     NAME2 ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = 'Z010' AND ";
        $query .= "     NAMECD2 = '00' ";

        return $query;
    }

    function selectQuery($model) {
        $query  =" SELECT ";
        $query .= "   * ";
        $query .= " FROM ";
        if ($model->mode == "grd") {     //卒業
            $query .= "   GRD_HEXAM_ENTREMARK_HDAT ";
        } else {
            $query .= "   HEXAM_ENTREMARK_HDAT ";
        }
        $query .= " WHERE ";
        $query .= "     SCHREGNO = '{$model->schregno}' ";

        return $query;
    }

    //HREPORTREMARK_DAT から読込む
    function get_hreportremark_dat($model) {
        $query  = " SELECT ";
        $query .= "     TOTALSTUDYTIME, ";
        $query .= "     TOTALSTUDYACT ";
        $query .= " FROM ";
        $query .= "     HREPORTREMARK_DAT ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO = '{$model->schregno}' ";
        if ($model->Properties["tutisyoSougoushokengakki"] == '1') {
            $query .= " AND SEMESTER = '9' ";
        }
        $query .= " ORDER BY ";
        $query .= "     YEAR, ";
        $query .= "     SEMESTER ";

        return $query;
    }

    //フォーム2の値を取得
    function selectQueryForm2($model, $year) {
        $query = " SELECT ";
        $query .= "   YEAR, ";
        $query .= "   SCHREGNO, ";
        $query .= "   ANNUAL, ";
        $query .= "   ATTENDREC_REMARK, ";
        $query .= "   SPECIALACTREC, ";
        $query .= "   TRAIN_REF, ";
        $query .= "   TRAIN_REF1, ";
        $query .= "   TRAIN_REF2, ";
        $query .= "   TRAIN_REF3 ";
        $query .= " FROM ";
        if ($model->mode == "grd") {     //卒業
            $query .= "   GRD_HEXAM_ENTREMARK_DAT ";
        } else {
            $query .= "   HEXAM_ENTREMARK_DAT ";
        }
        $query .= " WHERE ";
        $query .= "       YEAR     = '{$year}' ";
        $query .= "   AND SCHREGNO = '{$model->schregno}' ";

        return $query;
    }

    function cntSchregBaseMst($model) {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     SCHREG_BASE_MST ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO = '{$model->schregno}' AND ";
        $query .= "     FISCALYEAR(GRD_DATE) <= '2009' ";

        return $query;
    }

    function getTrain_ref($model) {
        $query .= " WITH REGD_DAT AS ( ";
        $query .= " SELECT ";
        $query .= "     YEAR, ";
        $query .= "     SCHREGNO, ";
        $query .= "     ANNUAL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT ";
        $query .= " GROUP BY ";
        $query .= "     YEAR, ";
        $query .= "     SCHREGNO, ";
        $query .= "     ANNUAL ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     HEXAM_ENTREMARK_DAT T1 ";
        $query .= " LEFT JOIN ";
        $query .= "     REGD_DAT L1 ON  L1.SCHREGNO = T1.SCHREGNO ";
        $query .= "                 AND L1.YEAR     = T1.YEAR ";
        $query .= "                 AND L1.ANNUAL   = T1.ANNUAL ";
        $query .= " WHERE ";
        $query .= "     T1.SCHREGNO = '{$model->schregno}' ";

        return $query;
    }
    
    //成績参照画面
    function selectQueryForm3($model) {
        $query  = " SELECT ";
        $query .= "     T1.ANNUAL, ";
        $query .= "     CASE WHEN T1.CLASSABBV IS NULL OR T1.CLASSABBV = '' ";
        $query .= "         THEN T3.CLASSABBV ";
        $query .= "         ELSE T1.CLASSABBV ";
        $query .= "     END AS CLASSNAME, ";
        $query .= "     CASE WHEN T1.SUBCLASSNAME IS NULL OR T1.SUBCLASSNAME = '' ";
        $query .= "         THEN T4.SUBCLASSNAME ";
        $query .= "         ELSE T1.SUBCLASSNAME ";
        $query .= "     END AS SUBCLASSNAME, ";
        $query .= "     T1.VALUATION, ";
        $query .= "     T1.GET_CREDIT, ";
        $query .= "     T1.ADD_CREDIT, ";
        $query .= "     T1.CLASSCD, ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     T1.SCHOOL_KIND, ";
            $query .= "     T1.CURRICULUM_CD, ";
        }   
        $query .= "     T1.SUBCLASSCD ";
        $query .= " FROM ";
        if ($model->mode == "grd") {     //卒業
            $query .= "     GRD_STUDYREC_DAT T1 ";
            $query .= " LEFT OUTER JOIN GRD_STUDYRECREMARK_DAT T2 ON  ";
        } else {
            $query .= "     SCHREG_STUDYREC_DAT T1 ";
            $query .= " LEFT OUTER JOIN STUDYRECREMARK_DAT T2 ON  ";
        }
        $query .= "                   T1.YEAR       = T2.YEAR AND  ";
        $query .= "                   T1.SCHREGNO   = T2.SCHREGNO AND  ";
        $query .= "                   T1.CLASSCD    = T2.CLASSCD AND  ";
        $query .= "                   T1.SUBCLASSCD = T2.SUBCLASSCD ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "               AND T1.SCHOOL_KIND    = T2.SCHOOL_KIND ";
            $query .= "               AND T1.CURRICULUM_CD  = T2.CURRICULUM_CD";
        }
        $query .= " LEFT OUTER JOIN CLASS_MST    T3 ON  ";
        $query .= "                   T1.CLASSCD    = T3.CLASSCD  ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "               AND T1.SCHOOL_KIND    = T3.SCHOOL_KIND ";
        }
        $query .= " LEFT OUTER JOIN SUBCLASS_MST T4 ON  ";
        $query .= "                   T1.SUBCLASSCD = T4.SUBCLASSCD  ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "               AND T1.CLASSCD        = T4.CLASSCD ";
            $query .= "               AND T1.SCHOOL_KIND    = T4.SCHOOL_KIND ";
            $query .= "               AND T1.CURRICULUM_CD  = T4.CURRICULUM_CD";
        }
        $query .= " WHERE ";
        $query .= "   T1.SCHOOLCD = '0' AND ";
        if ($model->annual["YEAR"] != "0" || $model->annual["ANNUAL"] != "0") {
            $query .= "     T1.YEAR   = '{$model->annual["YEAR"]}' AND ";
            $query .= "     T1.ANNUAL = '{$model->annual["ANNUAL"]}' AND";
        }
        $query .= "     T1.SCHREGNO = '{$model->schregno}' ";
        $query .= " ORDER BY ";
        $query .= "     T1.CLASSCD, ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     T1.SCHOOL_KIND, ";
            $query .= "     T1.CURRICULUM_CD, ";
        }
        $query .= "     T1.SUBCLASSCD, ";
        $query .= "     T1.YEAR, ";
        $query .= "     T1.ANNUAL ";
        return $query;
    }

    //学年記号取得
    function getSchoolKind($model) {
        if ($model->mode == "grd") {
            $year = $model->grd_year;
        } else {
            $year = CTRL_YEAR;
        }

        $query  = " SELECT ";
        $query .= "     SCHOOL_KIND ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_GDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$year}' ";
        $query .= "     AND GRADE = '{$model->grade}' ";

        return $query;
    }

    //対象学年取得
    function getGdat($model) {
        if ($model->mode == "grd") {
            $year = $model->grd_year;
        } else {
            $year = CTRL_YEAR;
        }

        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_GDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$year}' ";
        $query .= "     AND SCHOOL_KIND = '{$model->schoolKind}' ";

        return $query;
    }

    //学年（年次）コンボボックス用
    function selectQueryAnnual($model) {
        if ($model->mode == "grd") {     //卒業
            $year     = $model->grd_year;
            $semester = $model->grd_semester;
            $tableName = "V_GRD_REGDYEAR_GRADE_DAT";
        } else {
            $year     = CTRL_YEAR;
            $semester = CTRL_SEMESTER;
            $tableName = "V_REGDYEAR_GRADE_DAT";
        }
        $query  = " WITH GRADE_T AS ( ";
        $query .= " SELECT ";
        $query .= "     T1.YEAR, ";
        $query .= "     T1.GRADE, ";
        $query .= "     T1.SCHOOL_KIND ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_GDAT T1 ";
        $query .= " WHERE ";
        $query .= "     T1.SCHOOL_KIND = '{$model->schoolKind}' ";
        $query .= " ) ";
        $query .= " SELECT DISTINCT ";
        $query .= "     T1.YEAR, ";
        $query .= "     T1.GRADE, ";
        $query .= "     T1.ANNUAL ";
        $query .= " FROM ";
        $query .= "     {$tableName} T1, ";
        $query .= "     GRADE_T T2 ";
        $query .= " WHERE ";
        $query .= "     T1.SCHREGNO = '{$model->schregno}' ";
        $query .= "     AND T1.YEAR = T2.YEAR ";
        $query .= "     AND T1.GRADE = T2.GRADE ";

        return $query;
    }

    /* 更新処理 */
    function update(&$db, $model) {
        //レコードを一旦削除する
        if ($model->mode == "grd") {     //卒業
            $tableName = "GRD_HEXAM_ENTREMARK_HDAT";
        } else {
            $tableName = "HEXAM_ENTREMARK_HDAT";
        }

        $query  = " DELETE FROM ";
        $query .= "     {$tableName} ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO = '{$model->schregno}' ";

        $db->query($query);

        $data = array();
        $data["SCHREGNO"][TEXT]       = $model->schregno;
        $data["DISEASE"][TEXT]        = $model->field["DISEASE"];
        $data["DOC_REMARK"][TEXT]     = $model->field["DOC_REMARK"];
        $data["TR_REMARK"][TEXT]      = $model->field["TR_REMARK"];
        $data["TOTALSTUDYACT"][TEXT]  = $model->field["TOTALSTUDYACT"];
        $data["TOTALSTUDYVAL"][TEXT]  = $model->field["TOTALSTUDYVAL"];
        $data["REMARK"][TEXT]         = $model->field["REMARK"];
        $data["COMMENTEX_A_CD"][TEXT] = $model->field["COMMENTEX_A_CD"];
        $data["REGISTERCD"][TEXT]     = STAFFCD;

        $query = Query::insertSQL($data, $tableName);
        $db->query($query);
    }

    /* 更新処理 */
    function update2(&$db, $model, $val) {
        if ($model->mode == "grd") {     //卒業
            $tableName = "GRD_HEXAM_ENTREMARK_DAT";
        } else {
            $tableName = "HEXAM_ENTREMARK_DAT";
        }

        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     {$tableName} ";
        $query .= " WHERE ";
        $query .= "       YEAR      = '{$val["YEAR"]}' ";
        $query .= "   AND SCHREGNO  = '{$model->schregno}' ";
        if ($model->control["学校区分"] == "0") {     //学年制
            $query .= "   AND ANNUAL    = '{$val["ANNUAL"]}' ";
        }

        $row_data = $db->getRow($query, DB_FETCHMODE_ASSOC);

        if ($model->Properties["useSyojikou3"] == "1") {
            $row_data["TRAIN_REF1"]       = $model->field2[$val["YEAR"]]["TRAIN_REF"];
            $row_data["TRAIN_REF2"]       = $model->field2[$val["YEAR"]]["TRAIN_REF2"];
            $row_data["TRAIN_REF3"]       = $model->field2[$val["YEAR"]]["TRAIN_REF3"];
        } else {
            $row_data["TRAIN_REF"]        = $model->field2[$val["YEAR"]]["TRAIN_REF"];
        }

        //レコードを一旦削除する
        $query  = " DELETE FROM ";
        $query .= "     {$tableName} ";
        $query .= " WHERE ";
        $query .= "       YEAR      = '{$val["YEAR"]}' ";
        $query .= "   AND SCHREGNO  = '{$model->schregno}' ";
        if ($model->control["学校区分"] == "0") {     //学年制
            $query .= "   AND ANNUAL    = '{$val["ANNUAL"]}' ";
        }

        $db->query($query);

        $data = array();
        $data["YEAR"][TEXT]             = $val["YEAR"];
        $data["SCHREGNO"][TEXT]         = $model->schregno;
        $data["ANNUAL"][TEXT]           = $val["ANNUAL"];
        $data["SPECIALACTREC"][TEXT]    = $model->field2[$val["YEAR"]]["SPECIALACTREC"];
        $data["TRAIN_REF"][TEXT]        = $row_data["TRAIN_REF"];
        $data["TRAIN_REF1"][TEXT]       = $row_data["TRAIN_REF1"];
        $data["TRAIN_REF2"][TEXT]       = $row_data["TRAIN_REF2"];
        $data["TRAIN_REF3"][TEXT]       = $row_data["TRAIN_REF3"];
        $data["ATTENDREC_REMARK"][TEXT] = $model->field2[$val["YEAR"]]["ATTENDREC_REMARK"];
        $data["REGISTERCD"][TEXT]       = STAFFCD;

        $query = Query::insertSQL($data, $tableName);
        $db->query($query);
    }

    //保健より読み込み
    function selectQueryHealth($model) {
         if ($model->mode == "grd") {     //卒業
            $year = $model->grd_year;
        } else {
            $year = $model->year;
        }
        $query  = " SELECT ";
        $query .= "     MEISYOU_GET(OTHERDISEASECD,'F140',1) AS OTHERDISEASECD, ";
        $query .= "     DOC_REMARK ";
        $query .= " FROM ";
        if ($model->mode == "grd") {     //卒業
            $query .= "   GRD_MEDEXAM_DET_DAT ";
        } else {
            $query .= "   MEDEXAM_DET_DAT ";
        }
        $query .= " WHERE ";
        $query .= "     YEAR     = '{$year}' AND ";
        $query .= "     SCHREGNO = '{$model->schregno}' ";

        return $query;
    }

    //学習指導要録より再読込
    function selectQuery_Htrainremark_Dat($model, $year) {
        $query  = " SELECT ";
        $query .= "     YEAR, ";
        $query .= "     ANNUAL, ";
        $query .= "     SPECIALACTREMARK AS SPECIALACTREC, ";
        $query .= "     TOTALREMARK AS TRAIN_REF, ";
        $query .= "     ATTENDREC_REMARK ";
        $query .= " FROM ";
        if ($model->mode == "grd") {     //卒業
            $query .= "   GRD_HTRAINREMARK_DAT ";
        } else {
            $query .= "   HTRAINREMARK_DAT ";
        }
        $query .= " WHERE ";
        $query .= "         YEAR      = '{$year}' ";
        $query .= "     AND SCHREGNO  = '{$model->schregno}' ";

        return $query;
    }

    //指導要録データ、調査書旧データ
    function sansyou_data($model, $year) {
        $query  = " SELECT ";
        $query .= "     T1.TRAIN_REF, ";
        $query .= "     L1.TOTALREMARK ";
        $query .= " FROM ";
        $query .= "     HEXAM_ENTREMARK_DAT T1 ";
        $query .= " FULL JOIN ";
        $query .= "     HTRAINREMARK_DAT L1 ON  L1.SCHREGNO = T1.SCHREGNO ";
        $query .= "                         AND L1.YEAR     = T1.YEAR ";
        $query .= " WHERE ";
        $query .= "     ( ";
        $query .= "         T1.SCHREGNO = '{$model->schregno}' AND ";
        $query .= "         T1.YEAR = '{$year}' ";
        $query .= "     ) OR ( ";
        $query .= "         L1.SCHREGNO = '{$model->schregno}' AND ";
        $query .= "         L1.YEAR = '{$year}' ";
        $query .= "     ) ";

        return $query;
    }

    //指導要録参照画面
    function selectQuery_Htrainremark_Hdat($model) {
        $query  = " SELECT ";
        $query .= "     TOTALSTUDYACT, ";
        $query .= "     TOTALSTUDYVAL ";
        $query .= " FROM ";
        if ($model->mode == "grd") {     //卒業
            $query .= "   GRD_HTRAINREMARK_HDAT ";
        } else {
            $query .= "   HTRAINREMARK_HDAT ";
        }
        $query .= " WHERE ";
        $query .= "     SCHREGNO  = '{$model->schregno}' ";

        return $query;
    }

    //「指導要録参照」の学年（年次）コンボボックス
    function selectQueryAnnual_HTRAINREMARK_DAT($model) {
        if($model->mode == "grd") {
            $tableName1 = "GRD_REGD_DAT";
            $tableName2 = "GRD_HTRAINREMARK_DAT";
        } else {
            $tableName1 = "SCHREG_REGD_DAT";
            $tableName2 = "HTRAINREMARK_DAT";
        }

        $query  = " WITH SCHREG_YEAR AS ( ";
        $query .= "     SELECT DISTINCT ";
        $query .= "         T1.YEAR ";
        $query .= "     FROM ";
        $query .= "         ".$tableName1." T1, ";
        $query .= "         SCHREG_REGD_GDAT T2 ";
        $query .= "     WHERE ";
        $query .= "         T1.YEAR     = T2.YEAR AND ";
        $query .= "         T1.GRADE    = T2.GRADE AND ";
        $query .= "         T1.SCHREGNO = '".$model->schregno."' AND ";
        $query .= "         T2.SCHOOL_KIND IN (SELECT DISTINCT ";
        $query .= "                                 S2.SCHOOL_KIND ";
        $query .= "                             FROM ";
        $query .= "                                 ".$tableName1." S1, ";
        $query .= "                                 SCHREG_REGD_GDAT S2 ";
        $query .= "                             WHERE ";
        $query .= "                                 S1.YEAR     = S2.YEAR AND ";
        if($model->mode == "grd") {
            $query .= "                                 S1.YEAR     = (SELECT ";
            $query .= "                                                     MAX(SS1.YEAR) ";
            $query .= "                                                 FROM ";
            $query .= "                                                     ".$tableName1." SS1 ";
            $query .= "                                                 WHERE ";
            $query .= "                                                     SS1.SCHREGNO = S1.SCHREGNO ) AND ";
        } else {
            $query .= "                                 S1.YEAR     = '".CTRL_YEAR."' AND ";
        }
        $query .= "                                 S1.GRADE    = S2.GRADE AND ";
        $query .= "                                 S1.SCHREGNO = T1.SCHREGNO ";
        $query .= "                             ) ";
        $query .= " ) ";

        $query .= " SELECT DISTINCT ";
        $query .= "     T1.YEAR, ";
        $query .= "     T1.ANNUAL ";
        $query .= " FROM ";
        $query .= "     ".$tableName2." T1, ";
        $query .= "     SCHREG_YEAR T2 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR     = T2.YEAR AND ";
        $query .= "     T1.SCHREGNO = '".$model->schregno."' ";
        $query .= " ORDER BY ";
        $query .= "     T1.YEAR ";

        return $query;
    }

    //指導要録参照画面
    function selectQuery_Htrainremark_dat2($model) {
        $query  = " SELECT ";
        $query .= "     TOTALSTUDYACT, ";
        $query .= "     TOTALSTUDYVAL ";
        $query .= " FROM ";
        if ($model->mode == "grd") {     //卒業
            $query .= "   GRD_HTRAINREMARK_DAT ";
        } else {
            $query .= "   HTRAINREMARK_DAT ";
        }
        $query .= " WHERE ";
        $query .= "         YEAR      = '{$model->annual["YEAR"]}' ";
        $query .= "     AND SCHREGNO  = '{$model->schregno}' ";

        return $query;
    }

    //部活動参照
    function getClub($model)
    {
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
        $query .= "     T1.SCHREGNO = '".$model->schregno."' ";
        $query .= " ORDER BY ";
        $query .= "     T1.SDATE ";

        return $query;
    }

    //委員会活動参照
    function getCommittee($model)
    {
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
        $query .= "     T1.YEAR     = '".VARS::get("YEAR")."' AND ";
        $query .= "     T1.SCHREGNO = '".$model->schregno."' ";
        $query .= " ORDER BY ";
        $query .= "     T1.GRADE, ";
        $query .= "     T1.SEMESTER, ";
        $query .= "     T1.SEQ ";

        return $query;
    }

//教育課程対応
    //学籍資格データよりデータを取得
    function getAward($model) {
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
            $query .= "     T1.SCHREGNO = '{$model->schregno}' ";
            $query .= " ORDER BY ";
            $query .= "     T1.REGDDATE, ";
            //教育課程対応
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= " T1.CLASSCD, ";
                $query .= " T1.SCHOOL_KIND, ";
                $query .= " T1.CURRICULUM_CD, ";
            }
            $query .= "     T1.SUBCLASSCD, ";
            $query .= "     T1.SEQ ";
        } else {
            $query  = " SELECT ";
            $query .="    T1.year, ";
            $query .="    T1.regddate, ";
            $query .="    T1.subclasscd, ";
            $query .="    CASE WHEN T1.CONDITION_DIV = '1' ";
            $query .="              THEN '資格' ";
            $query .="              ELSE 'その他' ";
            $query .="         END AS CONDITION_DIV, ";
            $query .="    T1.SEQ, ";
            $query .="    T1.contents, ";
            $query .="    T1.remark, ";
            $query .="    T1.credits, ";
            $query .="    T5.CREDITS AS CREDIT ";
            $query .=" FROM  schreg_qualified_hobby_dat T1 ";
            $query .="       LEFT OUTER JOIN subclass_mst T2 ON T1.subclasscd = T2.subclasscd ";
            $query .="       LEFT JOIN SCHREG_REGD_DAT T4 ON T4.SCHREGNO = T1.SCHREGNO ";
            $query .="                                   AND T4.YEAR     = T1.year ";
            $query .="                                   AND T4.SEMESTER = '".CTRL_SEMESTER."' ";
            $query .="       LEFT JOIN CREDIT_MST T5 ON T5.YEAR       = T1.year ";
            $query .="                              AND T5.COURSECD   = T4.COURSECD ";
            $query .="                              AND T5.MAJORCD    = T4.MAJORCD ";
            $query .="                              AND T5.GRADE      = T4.GRADE ";
            $query .="                              AND T5.COURSECODE = T4.COURSECODE ";
            $query .="                              AND T5.CLASSCD    = SUBSTR(T1.SUBCLASSCD,1,2) ";
            $query .="                              AND T5.SUBCLASSCD = T1.SUBCLASSCD ";
            //教育課程対応
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "                            AND T5.SCHOOL_KIND   = T1.SCHOOL_KIND ";
                $query .= "                            AND T5.CURRICULUM_CD = T1.CURRICULUM_CD ";
            }
            $query .="WHERE T1.schregno = '{$model->schregno}' ";
            $query .="ORDER BY  ";
            $query .="  T1.regddate,  ";
            //教育課程対応
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= " T1.CLASSCD, ";
                $query .= " T1.SCHOOL_KIND, ";
                $query .= " T1.CURRICULUM_CD, ";
            }
            $query .="  T1.subclasscd,  ";
            $query .="  T1.SEQ ";
        }
        return $query;
    }

    //記録備考参照
    function getSchregClubHdetailDat($model) {
        $query  = " SELECT ";
        $query .= "     T1.CLUBCD, ";
        $query .= "     L1.CLUBNAME, ";
        $query .= "     T1.HOSTCD, ";
        $query .= "     L2.HOSTNAME, ";
        $query .= "     T1.MEET_NAME, ";
        $query .= "     T1.DETAIL_DATE, ";
        $query .= "     T1.DETAIL_SEQ, ";
        $query .= "     CASE T1.DIV WHEN '1' THEN '個人' WHEN '2' THEN '団体' ELSE '' END AS DIV_NAME, ";
        $query .= "     T1.RECORDCD, ";
        $query .= "     L3.RECORDNAME, ";
        $query .= "     T1.DOCUMENT, ";
        $query .= "     T1.DETAIL_REMARK ";
        $query .= " FROM ";
        $query .= "     SCHREG_CLUB_HDETAIL_DAT T1 ";
        $query .= "     LEFT JOIN CLUB_MST L1 ON T1.CLUBCD = L1.CLUBCD ";
        $query .= "     LEFT JOIN CLUB_HOST_MST L2 ON T1.HOSTCD = L2.HOSTCD ";
        $query .= "     LEFT JOIN CLUB_RECORD_MST L3 ON T1.RECORDCD = L3.RECORDCD ";
        $query .= " WHERE ";
        $query .= "     T1.SCHREGNO = '".$model->schregno."' ";
        $query .= " ORDER BY ";
        $query .= "     T1.HOSTCD, ";
        $query .= "     T1.DETAIL_DATE DESC, ";
        $query .= "     T1.DETAIL_SEQ ";

        return $query;
    }
}
?>
