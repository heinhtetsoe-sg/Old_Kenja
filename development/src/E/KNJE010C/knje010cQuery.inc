<?php

require_once('for_php7.php');

class knje010cQuery extends Query {

    function getSecurityHigh() {
        $query  = " SELECT ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     MENU_HIGH_SECURITY_MST ";
        $query .= " WHERE ";
        $query .= "     PROGRAMID = 'KNJE010C' ";
        $query .= "     AND INVALID_FLG = '0' ";

        return $query;
    }

    //学校名取得の取得(テストコンボで使う)
    function getSchoolName($JH="") {
        $query  = " SELECT ";
        $query .= "     NAME1, ";
        $query .= "     NAME2 ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = 'Z010' AND ";
        $query .= "     NAMECD2 = '00' ";
        if($JH){
            $query .= "     AND NAMESPARE2 IS NOT NULL ";
        }

        return $query;
    }

    //RECORD_TOTALSTUDYTIME_DAT から読込む
    function get_record_totalstudytime_dat($model) {
        $query  = " SELECT ";
        $query .= "     TOTALSTUDYTIME, ";
        $query .= "     TOTALSTUDYACT ";
        $query .= " FROM ";
        $query .= "     RECORD_TOTALSTUDYTIME_DAT ";
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

    function getYouroku($model) {
        $query .= " SELECT ";
        $query .= "     TOTALSTUDYACT, ";
        $query .= "     TOTALSTUDYVAL ";
        $query .= " FROM ";
        $query .= "     HTRAINREMARK_DAT ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO = '{$model->schregno}' ";
        $query .= " ORDER BY ";
        $query .= "     YEAR ";

        return $query;
    }

    //フォーム2の値を取得
    function selectQueryForm2($model) {
        $query  = " SELECT ";
        $query .= "   YEAR, ";
        $query .= "   SCHREGNO, ";
        $query .= "   ANNUAL, ";
        $query .= "   ATTENDREC_REMARK, ";
        $query .= "   SPECIALACTREC, ";
        $query .= "   TOTALSTUDYACT, ";
        $query .= "   TOTALSTUDYVAL, ";
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
        $query .= "       YEAR     = '{$model->annual["YEAR"]}' ";
        $query .= "   AND SCHREGNO = '{$model->schregno}' ";

        return $query;
    }
    
//教育課程対応
    //成績参照画面
    function selectQueryForm3($model, $JH="") {
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
        $query .= "                   T1.YEAR           = T2.YEAR AND  ";
        $query .= "                   T1.SCHREGNO       = T2.SCHREGNO AND  ";
        $query .= "                   T1.CLASSCD        = T2.CLASSCD AND  ";
        $query .= "                   T1.SUBCLASSCD     = T2.SUBCLASSCD ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "               AND T1.SCHOOL_KIND    = T2.SCHOOL_KIND ";
            $query .= "               AND T1.CURRICULUM_CD  = T2.CURRICULUM_CD";
        }
        $query .= " LEFT OUTER JOIN CLASS_MST    T3 ON  ";
        $query .= "                   T1.CLASSCD        = T3.CLASSCD ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "               AND T1.SCHOOL_KIND    = T3.SCHOOL_KIND ";
        }
        $query .= " LEFT OUTER JOIN SUBCLASS_MST T4 ON  ";
        $query .= "                   T1.SUBCLASSCD     = T4.SUBCLASSCD  ";
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
        if($JH){
            $query .= "   T1.ANNUAL >= '04' AND ";
        }
        $query .= "     T1.SCHREGNO = '{$model->schregno}' ";
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

    //学年（年次）コンボボックス用
    function selectQueryAnnual($model, $JH="") {
        $tableName = ($model->mode == "grd") ? "GRD_STUDYREC_DAT" : "SCHREG_STUDYREC_DAT";

        $query  = " SELECT DISTINCT ";
        $query .= "     YEAR, ";
        $query .= "     ANNUAL ";
        $query .= " FROM ";
        $query .= "     {$tableName} ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO = '{$model->schregno}' ";
        if($JH){
            $query .= "     AND ANNUAL >= '04' ";
        }
        $query .= " ORDER BY ";
        $query .= "     YEAR ";

        return $query;
    }

    //「出欠の記録～」の学年（年次）コンボボックス
    function selectQueryAnnual_knje010cForm2($model) {
        $query  = " WITH SUB_MAIN AS ( ";
        $query .= " SELECT DISTINCT ";
        $query .= "     T1.YEAR, ";
        $query .= "     T1.ANNUAL, ";
        $query .= "     T1.SCHREGNO ";
        $query .= " FROM ";
        $query .= "     SCHREG_STUDYREC_DAT T1 ";
        $query .= " UNION ";
        $query .= " SELECT DISTINCT ";
        $query .= "     T2.YEAR, ";
        $query .= "     T2.ANNUAL, ";
        $query .= "     T2.SCHREGNO ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T2 ";
        $query .= " ) ";
        $query .= " SELECT DISTINCT ";
        $query .= "     YEAR, ";
        $query .= "     ANNUAL ";
        $query .= " FROM ";
        $query .= "     SUB_MAIN ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO = '{$model->schregno}' ";
        $query .= " ORDER BY ";
        $query .= "     YEAR, ";
        $query .= "     ANNUAL ";

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

    /* 更新処理 */
    function update(&$db, $model) {
        //レコードを一旦削除する
        if ($model->mode == "grd") {     //卒業
            $tableName = "GRD_HEXAM_ENTREMARK_HDAT";
        } else {
            $tableName = "HEXAM_ENTREMARK_HDAT";
        }

        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     {$tableName} ";
        $query .= " WHERE ";
        $query .= "       SCHREGNO  = '{$model->schregno}' ";

        $row_data = $db->getRow($query, DB_FETCHMODE_ASSOC);

        if ($model->Properties["tyousasyoSougouHyoukaNentani"] != 1) {
            $row_data["TOTALSTUDYACT"] = $model->field["TOTALSTUDYACT"];
            $row_data["TOTALSTUDYVAL"] = $model->field["TOTALSTUDYVAL"];
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
        $data["TOTALSTUDYACT"][TEXT]  = $row_data["TOTALSTUDYACT"];
        $data["TOTALSTUDYVAL"][TEXT]  = $row_data["TOTALSTUDYVAL"];
        $data["REMARK"][TEXT]         = isset($model->no_comments) ? $model->no_comments_label : $model->field["REMARK"];
        $data["COMMENTEX_A_CD"][TEXT] = $model->field["COMMENTEX_A_CD"];
        $data["REGISTERCD"][TEXT]     = STAFFCD;

        $query = Query::insertSQL($data, $tableName);
        $db->query($query);
    }

    /* 更新処理 */
    function update2(&$db, $model) {
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
        $query .= "       YEAR      = '{$model->annual["YEAR"]}' ";
        $query .= "   AND SCHREGNO  = '{$model->schregno}' ";

        $row_data = $db->getRow($query, DB_FETCHMODE_ASSOC);

        if ($model->Properties["useSyojikou3"] == "1") {
            $row_data["TRAIN_REF1"]       = $model->field2["TRAIN_REF1"];
            $row_data["TRAIN_REF2"]       = $model->field2["TRAIN_REF2"];
            $row_data["TRAIN_REF3"]       = $model->field2["TRAIN_REF3"];
        } else {
            $row_data["TRAIN_REF"]        = $model->field2["TRAIN_REF"];
        }

        if ($model->Properties["tyousasyoSougouHyoukaNentani"] == 1) {
            $row_data["TOTALSTUDYACT"] = $model->field2["TOTALSTUDYACT"];
            $row_data["TOTALSTUDYVAL"] = $model->field2["TOTALSTUDYVAL"];
        }

        //レコードを一旦削除する
        $query  = " DELETE FROM ";
        $query .= "     {$tableName} ";
        $query .= " WHERE ";
        $query .= "       YEAR      = '{$model->annual["YEAR"]}' ";
        $query .= "   AND SCHREGNO  = '{$model->schregno}' ";

        $db->query($query);

        $data = array();
        $data["YEAR"][TEXT]             = $model->annual["YEAR"];
        $data["SCHREGNO"][TEXT]         = $model->schregno;
        $data["ANNUAL"][TEXT]           = $model->annual["ANNUAL"];
        $data["SPECIALACTREC"][TEXT]    = $model->field2["SPECIALACTREC"];
        $data["TRAIN_REF"][TEXT]        = $row_data["TRAIN_REF"];
        $data["TRAIN_REF1"][TEXT]       = $row_data["TRAIN_REF1"];
        $data["TRAIN_REF2"][TEXT]       = $row_data["TRAIN_REF2"];
        $data["TRAIN_REF3"][TEXT]       = $row_data["TRAIN_REF3"];

        $data["TOTALSTUDYACT"][TEXT]    = $row_data["TOTALSTUDYACT"];
        $data["TOTALSTUDYVAL"][TEXT]    = $row_data["TOTALSTUDYVAL"];
        $data["ATTENDREC_REMARK"][TEXT] = $model->field2["ATTENDREC_REMARK"];
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
    function selectQuery_Htrainremark_Dat($model) {
        $query  = " SELECT ";
        $query .= "     YEAR, ";
        $query .= "     ANNUAL, ";
        $query .= "     SPECIALACTREMARK AS SPECIALACTREC, ";
        $query .= "     TOTALREMARK AS TRAIN_REF, ";
        $query .= "     ATTENDREC_REMARK, ";
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

    //指導要録データ、調査書旧データ
    function sansyou_data($model) {
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
        $query .= "         T1.YEAR = '{$model->annual["YEAR"]}' ";
        $query .= "     ) OR ( ";
        $query .= "         L1.SCHREGNO = '{$model->schregno}' AND ";
        $query .= "         L1.YEAR = '{$model->annual["YEAR"]}' ";
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

    //部活動参照
    function getClub($model)
    {
        $sdate = $model->annual["YEAR"] . '-04-01';
        $edate = ($model->annual["YEAR"] + 1) . '-03-31';

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
        $query .= "     T1.YEAR     = '".$model->annual["YEAR"]."' AND ";
        $query .= "     T1.SCHREGNO = '".$model->schregno."' ";
        $query .= " ORDER BY ";
        $query .= "     T1.GRADE, ";
        $query .= "     T1.SEMESTER, ";
        $query .= "     T1.SEQ ";

        return $query;
    }
    
    //セット日々出欠備考
    function getSemesRemark($model, $db, $year) {
    
        $query  = "  SELECT ";
        $query .= "      REMARK1, ";
        $query .= "      CASE WHEN MONTH = '01' THEN '13' WHEN MONTH = '02' THEN '14' WHEN MONTH = '03' THEN '15' ELSE MONTH END AS SORT_MONTH ";
        $query .= "  FROM ";
        $query .= "      ATTEND_SEMES_REMARK_DAT ";
        $query .= "  WHERE ";
        $query .= "      COPYCD = '0' ";
        $query .= "  AND YEAR = '".$year."' ";
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
        
        return $set_remark;
    }

//教育課程対応
    //学籍資格データよりデータを取得
    function getAward($model) {
        $sdate = $model->annual["YEAR"] . '-04-01';
        $edate = ($model->annual["YEAR"] + 1) . '-03-31';
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
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "                                     AND T1.classcd = T2.classcd ";
                $query .= "                                     AND T1.SCHOOL_KIND = T2.SCHOOL_KIND ";
                $query .= "                                     AND T1.CURRICULUM_CD = T2.CURRICULUM_CD ";
            }
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
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "                            AND T5.SCHOOL_KIND = T1.SCHOOL_KIND ";
                $query .= "                            AND T5.CURRICULUM_CD = T1.CURRICULUM_CD ";
            }
            $query .= " WHERE ";
            $query .= "     T1.SCHREGNO = '{$model->schregno}' AND ";
            $query .= "     T1.REGDDATE BETWEEN '{$sdate}' AND '{$edate}' ";
            $query .= " ORDER BY " ;
            $query .= " T1.regddate, " ;
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= " T1.CLASSCD, " ; 
                $query .= " T1.SCHOOL_KIND, " ; 
                $query .= " T1.CURRICULUM_CD, " ; 
            }
            $query .= " T1.subclasscd, " ; 
            $query .= " T1.SEQ ";
        }
        return $query;
    }
}
?>
