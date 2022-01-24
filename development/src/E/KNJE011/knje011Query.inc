<?php

require_once('for_php7.php');
class knje011Query extends Query
{
    public function getSecurityHigh()
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     MENU_HIGH_SECURITY_MST ";
        $query .= " WHERE ";
        $query .= "     PROGRAMID = 'KNJE011' ";
        $query .= "     AND INVALID_FLG = '0' ";

        return $query;
    }

    //学年判定(GRADE_CD取得)
    public function getGradeCd($model, $flg = "")
    {
        $query  = " SELECT ";
        $query .= "     L1.GRADE_CD, ";
        $query .= "     L1.SCHOOL_KIND ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1";
        $query .= "     LEFT JOIN SCHREG_REGD_GDAT L1 ON L1.YEAR = T1.YEAR ";
        $query .= "          AND L1.GRADE = T1.GRADE ";
        if ($model->Properties["useSchool_KindField"] == "1") {
            $query .= "          AND L1.SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= " WHERE ";
        if (!$flg) {
            $query .= "     T1.YEAR = '{$model->exp_year}' ";
        } else {
            $query .= "     T1.YEAR = '{$model->annual["YEAR"]}' ";
            $query .= " AND T1.ANNUAL = '{$model->annual["ANNUAL"]}' ";
        }
        $query .= " AND T1.SCHREGNO = '{$model->schregno}' ";
        $query .= " AND T1.SEMESTER = '{$model->exp_semester}' ";

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
        $query .= "         PROGRAMID ='KNJE011' ";
        $query .= " ) ";

        $query .= " SELECT ";
        $query .= "     PROGRAMPATH ";
        $query .= " FROM ";
        $query .= "     MENU_HIGH_SECURITY_MST T1, ";
        $query .= "     MENU T2 ";
        $query .= " WHERE ";
        $query .= "     T1.MENUID = T2.MENUID AND ";
        $query .= "     T1.PROGRAMID ='KNJE011' ";

        return $query;
    }

    //追加可能年度取得
    public function selectYearQuery($model)
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     YEAR AS VALUE, ";
        $query .= "     YEAR AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHOOL_MST T1 ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            $query .= " WHERE ";
            $query .= "         SCHOOLCD    = '".sprintf("%012d", SCHOOLCD)."' ";
            $query .= "     AND SCHOOL_KIND = '".$model->school_kind."' ";
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " WHERE ";
            $query .= "     SCHOOLCD    = '".sprintf("%012d", SCHOOLCD)."' AND ";
            $query .= "     SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= "     AND YEAR < '".$model->entYear."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE DESC ";
        return $query;
    }

    //追加可能学年取得
    public function selectGradeQuery($model)
    {
        $query .= " SELECT DISTINCT ";
        $query .= "     T2.GRADE AS VALUE, ";
        $query .= "     S1.GRADE_NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T2 ";
        $query .= "     INNER JOIN SCHREG_REGD_GDAT S1 ";
        $query .= "                  ON T2.YEAR         = S1.YEAR ";
        $query .= "                 AND T2.GRADE        = S1.GRADE ";
        $query .= "                 AND S1.SCHOOL_KIND  = 'H' ";
        $query .= " WHERE ";
        $query .= "     T2.YEAR = '{$model->addYear}' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //SCHREG_BASE_MST 取得
    public function getSchregBase($model, $name, $fmt = "")
    {
        $query .= " SELECT";
        if ($fmt == "") {
            $query .= " ".$name." ";
        } elseif ($fmt == "year") {
            $query .= " YEAR(".$name.") ";
        }
        $query .= " FROM ";
        $query .= "     SCHREG_BASE_MST ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO = '{$model->schregno}' ";

        return $query;
    }

    //学校名取得の取得(テストコンボで使う)
    public function getSchoolName($JH = "")
    {
        $query  = " SELECT ";
        $query .= "     NAME1, ";
        $query .= "     NAME2 ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = 'Z010' AND ";
        $query .= "     NAMECD2 = '00' ";
        if ($JH) {
            $query .= "     AND NAMESPARE2 IS NOT NULL ";
        }

        return $query;
    }

    //SCHOOLDIV を取得する(0:学年制、1:単位制)
    public function getSchoolDiv()
    {
        $query  = " SELECT ";
        $query .= "     T1.SCHOOLDIV, ";
        $query .= "     VALUE(T2.NAMESPARE2,'0') AS NEN, ";
        $query .= "     VALUE(T3.NAMESPARE3,'0') AS IS_TUSIN ";
        $query .= " FROM ";
        $query .= "     SCHOOL_MST T1 ";
        $query .= "     LEFT JOIN V_NAME_MST T2 ON T2.YEAR = T1.YEAR AND T2.NAMECD1 = 'Z001' AND ";
        $query .= "             T2.NAMECD2 = T1.SCHOOLDIV AND T2.NAMESPARE2 IN ('3','4','6') ";
        $query .= "     LEFT JOIN V_NAME_MST T3 ON T3.YEAR = T1.YEAR AND T3.NAMECD1 = 'Z001' AND ";
        $query .= "             T3.NAMECD2 = T1.SCHOOLDIV ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '". CTRL_YEAR ."' ";

        return $query;
    }

    //RECORD_TOTALSTUDYTIME_DAT から読込む
    public function get_record_totalstudytime_dat($model)
    {
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

    public function selectQuery($model)
    {
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

    public function getYouroku($model)
    {
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

    public function getYourokuDetail($model)
    {
        $query .= " SELECT ";
        $query .= "     YEAR ";
        $query .= "   , TRAIN_REF1 ";
        $query .= "   , TRAIN_REF2 ";
        $query .= "   , TRAIN_REF3 ";
        $query .= " FROM ";
        $query .= "     HTRAINREMARK_DETAIL_DAT ";
        $query .= " WHERE YEAR = '{$model->annual["YEAR"]}' ";
        $query .= "   AND SCHREGNO = '{$model->schregno}' ";

        $query .= " ORDER BY ";
        $query .= "     YEAR ";

        return $query;
    }

    //フォーム2の値を取得
    public function selectQueryForm2($model)
    {
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
        if ($model->Properties["useTotalstudySlashFlg"] == 1) {
            $query .= "     , TOTALSTUDYACT_SLASH_FLG ";
            $query .= "     , TOTALSTUDYVAL_SLASH_FLG ";
        }
        if ($model->Properties["useAttendrecRemarkSlashFlg"] == 1) {
            $query .= "     , ATTENDREC_REMARK_SLASH_FLG ";
        }
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

    //成績参照画面
    public function selectQueryForm3($model, $JH = "")
    {
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
        if ($JH) {
            $query .= "   T1.ANNUAL >= '04' AND ";
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

    //学年（年次）コンボボックス用
    public function selectQueryAnnual($model, $JH = "")
    {
        $tableName = ($model->mode == "grd") ? "GRD_STUDYREC_DAT" : "SCHREG_STUDYREC_DAT";

        $query  = " SELECT DISTINCT ";
        $query .= "     YEAR, ";
        $query .= "     ANNUAL ";
        $query .= " FROM ";
        $query .= "     {$tableName} ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO = '{$model->schregno}' ";
        if ($JH) {
            $query .= "     AND ANNUAL >= '04' ";
        }
        $query .= " ORDER BY ";
        $query .= "     YEAR ";

        return $query;
    }

    //「出欠の記録～」の学年（年次）コンボボックス
    public function selectQueryAnnual_knje011Form2($model)
    {
        $query  = " WITH SUB_MAIN AS ( ";
        $query .= " SELECT DISTINCT ";
        $query .= "     T1.YEAR, ";
        $query .= "     T1.ANNUAL, ";
        $query .= "     T1.SCHREGNO ";
        $query .= " FROM ";
        $query .= "     SCHREG_STUDYREC_DAT T1 ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= " WHERE ";
            $query .= "     T1.SCHOOL_KIND = 'H' ";
        }
        $query .= " UNION ";
        $query .= " SELECT DISTINCT ";
        $query .= "     T2.YEAR, ";
        $query .= "     T2.ANNUAL, ";
        $query .= "     T2.SCHREGNO ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T2 ";
        $query .= "     INNER JOIN SCHREG_REGD_GDAT S1 ";
        $query .= "                  ON T2.YEAR         = S1.YEAR ";
        $query .= "                 AND T2.GRADE        = S1.GRADE ";
        $query .= "                 AND S1.SCHOOL_KIND  = 'H' ";
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
    public function selectQueryAnnual_HTRAINREMARK_DAT($model)
    {
        if ($model->mode == "grd") {
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
        if ($model->Properties["useSchool_KindField"] == "1") {
            $query .= "         T2.SCHOOL_KIND = '".SCHOOLKIND."' AND ";
        }
        $query .= "         T1.SCHREGNO = '".$model->schregno."' AND ";
        $query .= "         T2.SCHOOL_KIND IN (SELECT DISTINCT ";
        $query .= "                                 S2.SCHOOL_KIND ";
        $query .= "                             FROM ";
        $query .= "                                 ".$tableName1." S1, ";
        $query .= "                                 SCHREG_REGD_GDAT S2 ";
        $query .= "                             WHERE ";
        $query .= "                                 S1.YEAR     = S2.YEAR AND ";
        if ($model->mode == "grd") {
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
    public function selectQuery_Htrainremark_dat2($model)
    {
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
    public function update(&$db, $model)
    {
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
            $row_data["TOTALSTUDYACT"]              = $model->field["TOTALSTUDYACT"];
            $row_data["TOTALSTUDYVAL"]              = $model->field["TOTALSTUDYVAL"];
            $row_data["TOTALSTUDYACT_SLASH_FLG"]    = $model->field["TOTALSTUDYACT_SLASH_FLG"];
            $row_data["TOTALSTUDYVAL_SLASH_FLG"]    = $model->field["TOTALSTUDYVAL_SLASH_FLG"];
        }
        
        if ($model->Properties["useHexamRemark2Flg"] == 1) {
            $row_data["REMARK2"]    = $model->field["REMARK2"];
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
        $data["REMARK2"][TEXT]        = $row_data["REMARK2"];
        $data["COMMENTEX_A_CD"][TEXT] = $model->field["COMMENTEX_A_CD"];
        if ($model->Properties["useTotalstudySlashFlg"] == 1) {
            $data["TOTALSTUDYACT_SLASH_FLG"][TEXT]  = $row_data["TOTALSTUDYACT_SLASH_FLG"];
            $data["TOTALSTUDYVAL_SLASH_FLG"][TEXT]  = $row_data["TOTALSTUDYVAL_SLASH_FLG"];
        }
        $data["REGISTERCD"][TEXT]     = STAFFCD;

        $query = Query::insertSQL($data, $tableName);
        $db->query($query);
    }

    /* 更新処理 */
    public function update2(&$db, $model)
    {
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
            $row_data["TOTALSTUDYACT"]              = $model->field2["TOTALSTUDYACT"];
            $row_data["TOTALSTUDYVAL"]              = $model->field2["TOTALSTUDYVAL"];
            $row_data["TOTALSTUDYACT_SLASH_FLG"]    = $model->field2["TOTALSTUDYACT_SLASH_FLG"];
            $row_data["TOTALSTUDYVAL_SLASH_FLG"]    = $model->field2["TOTALSTUDYVAL_SLASH_FLG"];
        }
        if ($model->Properties["useAttendrecRemarkSlashFlg"] == 1) {
            $row_data["ATTENDREC_REMARK_SLASH_FLG"] = $model->field2["ATTENDREC_REMARK_SLASH_FLG"];
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
        $data["ATTENDREC_REMARK"][TEXT] = isset($model->field2["INS_COMMENTS"]) ? $model->field2["INS_COMMENTS_LABEL"] : $model->field2["ATTENDREC_REMARK"];
        if ($model->Properties["useTotalstudySlashFlg"] == 1) {
            $data["TOTALSTUDYACT_SLASH_FLG"][TEXT]  = $row_data["TOTALSTUDYACT_SLASH_FLG"];
            $data["TOTALSTUDYVAL_SLASH_FLG"][TEXT]  = $row_data["TOTALSTUDYVAL_SLASH_FLG"];
        }
        if ($model->Properties["useAttendrecRemarkSlashFlg"] == 1) {
            $data["ATTENDREC_REMARK_SLASH_FLG"][TEXT]  = $row_data["ATTENDREC_REMARK_SLASH_FLG"];
        }
        $data["REGISTERCD"][TEXT]       = STAFFCD;

        $query = Query::insertSQL($data, $tableName);

        $db->query($query);
    }

    //保健より読み込み
    public function selectQueryHealth($model)
    {
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
    public function selectQuery_Htrainremark_Dat($model)
    {
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

    //指導要録データ
    public function sansyou_data($model)
    {
        $query  = " SELECT ";
        $query .= "     TOTALREMARK ";
        $query .= "     , ATTENDREC_REMARK ";
        $query .= "     , SPECIALACTREMARK ";
        $query .= " FROM ";
        $query .= "     HTRAINREMARK_DAT ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO = '{$model->schregno}' AND ";
        $query .= "     YEAR     = '{$model->annual["YEAR"]}' ";

        return $query;
    }

    //指導要録参照画面
    public function selectQuery_Htrainremark_Hdat($model)
    {
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

    //セット日々出欠備考
    public function getSemesRemark($model, $db, $year)
    {
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
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
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

    //出欠備考 -- 通知票取込
    public function getHreportremarkDetailDat($db, $model)
    {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     HREPORTREMARK_DETAIL_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR        = '".$model->annual["YEAR"]."' AND ";
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

    //校種取得
    public function getSchoolKind($model)
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     T2.SCHOOL_KIND ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1, ";
        $query .= "     SCHREG_REGD_GDAT T2 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR     = T2.YEAR AND ";
        $query .= "     T1.YEAR     = '".$model->annual["YEAR"]."' AND ";
        $query .= "     T1.GRADE    = T2.GRADE AND ";
        $query .= "     T1.SCHREGNO = '".$model->schregno."' AND ";
        $query .= "     T1.SEMESTER IN (SELECT ";
        $query .= "                         MAX(SEMESTER) ";
        $query .= "                     FROM ";
        $query .= "                         SCHREG_REGD_DAT S1 ";
        $query .= "                     WHERE ";
        $query .= "                         T1.YEAR     = S1.YEAR AND ";
        $query .= "                         T1.SCHREGNO = S1.SCHREGNO ";
        $query .= "                     ) ";

        return $query;
    }

    //臘八摂心皆勤
    public function getRouhatsuKaikin($model)
    {
        $query .= " WITH SCHREG AS ( ";
        $query .= "   SELECT DISTINCT ";
        $query .= "     T1.YEAR ";
        $query .= "     , T2.SCHOOL_KIND ";
        $query .= "   FROM ";
        $query .= "     SCHREG_REGD_DAT T1 ";
        $query .= "     INNER JOIN SCHREG_REGD_GDAT T2 ";
        $query .= "       ON T1.YEAR = T2.YEAR ";
        $query .= "       AND T1.GRADE = T2.GRADE ";
        $query .= "   WHERE ";
        $query .= "     T1.SCHREGNO = '".$model->schregno."' ";
        $query .= " ) ";
        $query .= " , SCHREGYEAR AS ( ";
        $query .= "   SELECT YEAR ";
        $query .= "   FROM SCHREG ";
        $query .= "   WHERE SCHOOL_KIND IN ( ";
        $query .= "       SELECT SCHOOL_KIND ";
        $query .= "       FROM SCHREG ";
        $query .= "       WHERE YEAR = '{$model->annual["YEAR"]}' ";
        $query .= "     ) ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "   COUNT(*) REC_CNT ";
        $query .= "   , SUM(CASE WHEN KAIKIN_FLG = '1' THEN 1 ELSE 0 END) KAIKIN_CNT ";
        $query .= " FROM SCHREGYEAR ";
        $query .= " LEFT JOIN ROU_HATSU_SESSIN_KAI_DAT ROUHATSU ";
        $query .= "    ON SCHREGYEAR.YEAR = ROUHATSU.YEAR ";
        $query .= "   AND ROUHATSU.SCHREGNO = '".$model->schregno."' ";
        $query .= " WHERE SCHREGYEAR.YEAR <= '{$model->annual["YEAR"]}' ";

        return $query;
    }
}
