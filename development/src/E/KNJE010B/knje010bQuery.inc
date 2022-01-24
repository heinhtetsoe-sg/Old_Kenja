<?php

require_once('for_php7.php');

class knje010bQuery extends Query {
    //高セキュリティーチェック
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

    //学年判定(３学年)
    function getGradeCd($model) {

        $query  = " SELECT ";
        $query .= "     L1.GRADE_CD, ";
        $query .= "     L1.SCHOOL_KIND ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1";
        $query .= "     LEFT JOIN SCHREG_REGD_GDAT L1 ON L1.YEAR = T1.YEAR ";
        $query .= "          AND L1.GRADE = T1.GRADE ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '{$model->exp_year}' ";
        $query .= " AND T1.SCHREGNO = '{$model->schregno}' ";
        $query .= " AND T1.SEMESTER = '{$model->exp_semester}' ";

        return $query;
    }

    //教務主任等マスタチェック
    function getPositionCheck() {
        $query  = " SELECT ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     STAFF_DETAIL_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '". CTRL_YEAR ."' ";
        $query .= " AND STAFFCD = '".STAFFCD."' ";
        $query .= " AND ((STAFF_SEQ = '005'  ";
        $query .= "       AND FIELD1 = '0100') ";
        $query .= "   OR (STAFF_SEQ = '006'  ";
        $query .= "       AND FIELD1 = '0100') ";
        $query .= "   OR (STAFF_SEQ = '007'  ";
        $query .= "       AND FIELD1 = '0100') ";
        $query .= " ) ";

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

    //SCHOOLDIV を取得する(0:学年制、1:単位制)
    function getSchoolDiv() {
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

    //通知票取込（3年次）
    function get_record_totalstudytime_dat($model, $getYear="") {
    
        $setyear = "";
        if (!$getYear) {
            $setyear = $model->exp_year == "" ? CTRL_YEAR : $model->exp_year;
        } else {
            $setyear = $getYear;
        }
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

    //フォーム2の値を取得
    function selectQueryForm2($model, $year) {
        $query = " SELECT ";
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
        $query .= "     AND GRADE_CD <= 3 ";

        return $query;
    }

    //学年（年次）コンボボックス用
    function selectQueryAnnual($model) {
        if ($model->mode == "grd") {     //卒業
            $year     = $model->grd_year;
            $semester = $model->grd_semester;
            $tableName = "GRD_REGD_DAT";
        } else {
            $year     = CTRL_YEAR;
            $semester = CTRL_SEMESTER;
            $tableName = "SCHREG_REGD_DAT";
        }
        $query  = " WITH GRADE_T AS ( ";
        $query .= "     SELECT ";
        $query .= "         T1.YEAR, ";
        $query .= "         T1.GRADE, ";
        $query .= "         T1.SCHOOL_KIND, ";
        $query .= "         T1.GRADE_NAME1 ";
        $query .= "     FROM ";
        $query .= "         SCHREG_REGD_GDAT T1 ";
        $query .= "     WHERE ";
        $query .= "         T1.SCHOOL_KIND = '{$model->schoolKind}' ";
        $query .= "     AND INT(T1.GRADE_CD) <= 3 ";
        $query .= " ), REGD AS ( ";
        $query .= "   SELECT SCHREGNO, YEAR, SEMESTER, GRADE, ANNUAL ";
        $query .= "   FROM {$tableName} TBL  ";
        $query .= "   WHERE (SCHREGNO, YEAR, GRADE, SEMESTER) IN ( ";
        $query .= "     SELECT ";
        $query .= "          SCHREGNO, YEAR, GRADE, MAX(SEMESTER) ";
        $query .= "     FROM {$tableName} CHK  ";
        $query .= "     WHERE  ";
        $query .= "       (SCHREGNO, GRADE, YEAR) IN ( ";
        $query .= "          SELECT SCHREGNO, GRADE, MAX(YEAR) ";
        $query .= "          FROM {$tableName} WK ";
        $query .= "          WHERE ";
        $query .= "              (WK.SCHREGNO, WK.YEAR, WK.SEMESTER) IN ( ";
        $query .= "               SELECT WK2.SCHREGNO, WK2.YEAR, MAX(WK2.SEMESTER) ";
        $query .= "               FROM {$tableName} WK2 ";
        $query .= "               WHERE WK2.YEAR <= '".CTRL_YEAR."' ";
        $query .= "                 AND WK2.SCHREGNO = '{$model->schregno}' ";
        $query .= "               GROUP BY WK2.SCHREGNO, WK2.YEAR ";
        $query .= "               ) ";
        $query .= "          GROUP BY WK.SCHREGNO, WK.GRADE ) ";
        $query .= "     GROUP BY CHK.SCHREGNO, CHK.YEAR, CHK.GRADE ";
        $query .= "    ) ";
        $query .= " ) ";

        $query .= " SELECT DISTINCT ";
        $query .= "     T1.YEAR, ";
        $query .= "     T1.GRADE, ";
        $query .= "     T1.ANNUAL ";
        $query .= "     , T2.GRADE_NAME1 ";
        $query .= " FROM ";
        $query .= "     REGD T1 ";
        $query .= "     INNER JOIN GRADE_T T2 ON ";
        $query .= "         T1.YEAR = T2.YEAR ";
        $query .= "         AND T1.GRADE = T2.GRADE ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR <= '".CTRL_YEAR."' ";

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

        //削除
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
        $data["REMARK"][TEXT]         = isset($model->no_comments) ? $model->no_comments_label : $model->field["REMARK"];
        $data["REMARK2"][TEXT]        = $row_data["REMARK2"];
        $data["COMMENTEX_A_CD"][TEXT] = $model->field["COMMENTEX_A_CD"];
        if ($model->Properties["useTotalstudySlashFlg"] == 1) {
            $data["TOTALSTUDYACT_SLASH_FLG"][TEXT]  = $model->field["TOTALSTUDYACT_SLASH_FLG"];
            $data["TOTALSTUDYVAL_SLASH_FLG"][TEXT]  = $model->field["TOTALSTUDYVAL_SLASH_FLG"];
        }
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
        $data["ATTENDREC_REMARK"][TEXT] = isset($model->field2[$val["YEAR"]]["INS_COMMENTS"]) ? $model->field2[$val["YEAR"]]["INS_COMMENTS_LABEL"] : $model->field2[$val["YEAR"]]["ATTENDREC_REMARK"];
        $data["TOTALSTUDYACT"][TEXT]    = $model->field2[$val["YEAR"]]["TOTALSTUDYACT"];
        $data["TOTALSTUDYVAL"][TEXT]    = $model->field2[$val["YEAR"]]["TOTALSTUDYVAL"];
        if ($model->Properties["useTotalstudySlashFlg"] == 1) {
            $data["TOTALSTUDYACT_SLASH_FLG"][TEXT]  = $model->field2[$val["YEAR"]]["TOTALSTUDYACT_SLASH_FLG"];
            $data["TOTALSTUDYVAL_SLASH_FLG"][TEXT]  = $model->field2[$val["YEAR"]]["TOTALSTUDYVAL_SLASH_FLG"];
        }
        if ($model->Properties["useAttendrecRemarkSlashFlg"] == 1) {
            $data["ATTENDREC_REMARK_SLASH_FLG"][TEXT]  = $model->field2[$val["YEAR"]]["ATTENDREC_REMARK_SLASH_FLG"];
        }
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
        $query .= "     T1.YEAR, ";
        $query .= "     T1.ANNUAL, ";
        $query .= "     T1.SPECIALACTREMARK AS SPECIALACTREC, ";
        $query .= "     T1.TOTALREMARK AS TRAIN_REF, ";
        $query .= "     T1.TOTALSTUDYACT, ";
        $query .= "     T1.TOTALSTUDYVAL, ";
        $query .= "     T1.ATTENDREC_REMARK ";
        if ($model->schoolName === 'tokiwa' || $model->schoolName === 'bunkyo' || $model->Properties["useSyojikou3_torikomi"] == "1") {
            $query .= "     , L1.TRAIN_REF1 ";
            $query .= "     , L1.TRAIN_REF2 ";
            $query .= "     , L1.TRAIN_REF3 ";
        }
        $query .= " FROM ";
        if ($model->mode == "grd") {     //卒業
            $query .= "   GRD_HTRAINREMARK_DAT T1 ";
        } else {
            $query .= "   HTRAINREMARK_DAT T1 ";
        }
        if ($model->schoolName === 'tokiwa' || $model->schoolName === 'bunkyo' || $model->Properties["useSyojikou3_torikomi"] == "1") {
            $query .= "   LEFT JOIN HTRAINREMARK_DETAIL_DAT L1 ON L1.YEAR = T1.YEAR ";
            $query .= "                                       AND L1.SCHREGNO = T1.SCHREGNO ";
        }
        $query .= " WHERE ";
        $query .= "         T1.YEAR      = '{$year}' ";
        $query .= "     AND T1.SCHREGNO  = '{$model->schregno}' ";

        return $query;
    }

    //学習指導要録より再読込(通年の総合的な学習の時間の内容・評価のセット用)
    function getYourokuDat($model) {
        $query  = " SELECT ";
        $query .= "     T1.TOTALSTUDYACT, ";
        $query .= "     T1.TOTALSTUDYVAL ";
        $query .= " FROM ";
        if ($model->mode == "grd") {     //卒業
            $query .= "   GRD_HTRAINREMARK_DAT T1 ";
        } else {
            $query .= "   HTRAINREMARK_DAT T1 ";
        }
        $query .= " WHERE ";
        if ($model->readYear !== '0000') {
            $query .= "      T1.YEAR      = '{$model->readYear}' AND ";
        } else {
            $query .= "      T1.YEAR      IN ($model->allYear) AND ";
        }
        $query .= "      T1.SCHREGNO  = '{$model->schregno}' ";
        $query .= " ORDER BY ";
        $query .= "     T1.YEAR ";

        return $query;
    }

    //指導要録データ
    function sansyou_data($model, $year) {
        $query  = " SELECT ";
        $query .= "     TOTALREMARK ";
        $query .= " FROM ";
        $query .= "     HTRAINREMARK_DAT ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO = '{$model->schregno}' AND ";
        $query .= "     YEAR     = '{$year}' ";

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

    function selectQuery_Htrainremark_datTotal($model) {
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
        $query .= "     SCHREGNO  = '{$model->schregno}' ";
        $query .= " ORDER BY ";
        $query .= "     YEAR ";

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
        if ($model->Properties["useSchool_KindField"] == "1") {
            $query .= "         T2.SCHOOL_KIND = '".$model->schoolKind."' AND ";
        }
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

    //出欠備考 -- 通知票取込
    function getHreportremarkDetailDat($db, $model, $year) {
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
