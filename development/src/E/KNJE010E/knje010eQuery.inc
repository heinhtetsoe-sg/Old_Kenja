<?php

require_once("for_php7.php");

class knje010eQuery extends Query
{
    //高セキュリティーチェック
    public function getSecurityHigh()
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     MENU_HIGH_SECURITY_MST ";
        $query .= " WHERE ";
        $query .= "     PROGRAMID = 'KNJE010E' ";
        $query .= "     AND INVALID_FLG = '0' ";

        return $query;
    }

    //京都判定
    public function getNameMst($cd)
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
    public function getGradeCd($model)
    {

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
    public function getPositionCheck()
    {
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

    //臘八摂心皆勤
    public function getRouhatsuKaikin($model, $year)
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
        $query .= "       WHERE YEAR = '{$year}' ";
        $query .= "     ) ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "   COUNT(*) REC_CNT ";
        $query .= "   , SUM(CASE WHEN KAIKIN_FLG = '1' THEN 1 ELSE 0 END) KAIKIN_CNT ";
        $query .= " FROM SCHREGYEAR ";
        $query .= " LEFT JOIN ROU_HATSU_SESSIN_KAI_DAT ROUHATSU ";
        $query .= "    ON SCHREGYEAR.YEAR = ROUHATSU.YEAR ";
        $query .= "   AND ROUHATSU.SCHREGNO = '".$model->schregno."' ";
        $query .= " WHERE SCHREGYEAR.YEAR <= '{$year}' ";

        return $query;
    }

    //学校名取得の取得(テストコンボで使う)
    public function getSchoolName()
    {
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
    public function getSchoolDiv($model)
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
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= " AND T1.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind), "','")."') ";
            }
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND T1.SCHOOL_KIND = '".SCHOOLKIND."' ";
        }

        return $query;
    }

    // 諸事項取込み事前チェック
    public function getHrShojikouTorikomiDataCheck($model)
    {
        $query  = " SELECT ";
        $query .= "   T1.HR_NAME ";
        $query .= "  ,T3.YEAR ";
        $query .= "  ,COUNT(DISTINCT T3.SCHREGNO) AS COUNT "; // 1項目でもデータのある生徒の数
        $query .= " FROM ";
        if ($model->mode == "grd") {     //卒業
            $query .= "   GRD_REGD_HDAT T1 ";
            $query .= "   INNER JOIN GRD_REGD_DAT T2 ON ";
        } else {
            $query .= "   SCHREG_REGD_HDAT T1 ";
            $query .= "   INNER JOIN SCHREG_REGD_DAT T2 ON ";
        }
        $query .= "           T2.YEAR = T1.YEAR ";
        $query .= "           AND T2.SEMESTER = T1.SEMESTER ";
        $query .= "           AND T2.GRADE = T1.GRADE ";
        $query .= "           AND T2.HR_CLASS = T1.HR_CLASS ";
        $query .= "       LEFT JOIN HEXAM_ENTREMARK_TRAINREF_DAT T3 ON T3.SCHREGNO = T2.SCHREGNO ";
        //$query .= "           AND T3.YEAR = T2.YEAR ";
        $query .= "           AND T3.TRAIN_SEQ IN ('101', '102', '103', '104', '105', '106') ";
        $query .= "           AND T3.REMARK IS NOT NULL ";
        $query .= " WHERE ";
        $query .= "       T1.YEAR     = '{$model->exp_year}' ";
        $query .= "   AND T1.SEMESTER = '{$model->exp_semester}' ";
        $query .= "   AND T1.GRADE || '-' || T1.HR_CLASS = '{$model->gradeHrClass}' ";
        $query .= " GROUP BY ";
        $query .= "       T1.HR_NAME ";
        $query .= "      ,T3.YEAR ";
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

    // 備考
    public function selectHexamEntremarkRemarkHdatQuery($model)
    {
        $query  =" SELECT ";
        $query .= "   REMARK ";
        $query .= " FROM ";
        $query .= "   HEXAM_ENTREMARK_REMARK_HDAT ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO = '{$model->schregno}' ";

        return $query;
    }

    // 指導上参考となる諸事項
    public function getHexamEntremarkTrainrefDat($model, $year, $trainSeq)
    {
        $query  =" SELECT ";
        $query .= "   YEAR, TRAIN_SEQ, REMARK ";
        $query .= " FROM ";
        $query .= "   HEXAM_ENTREMARK_TRAINREF_DAT ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO = '{$model->schregno}' ";
        $query .= "     AND YEAR = '{$year}' ";
        $query .= "     AND TRAIN_SEQ = '{$trainSeq}' ";
        return $query;
    }

    // 備考取込み
    public function getHexamEntremarkLearningDat($model)
    {
        $query  =" SELECT ";
        $query .= "   YEAR, REMARK ";
        $query .= " FROM ";
        $query .= "   HEXAM_ENTREMARK_LEARNING_DAT ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO = '{$model->schregno}' ";
        $query .= " ORDER BY ";
        $query .= "     YEAR ";
        return $query;
    }

    //通知票取込（3年次）
    public function get_record_totalstudytime_dat($model, $getYear = "")
    {

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
    public function selectQueryForm2($model, $year)
    {
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

    //成績参照画面
    public function selectQueryForm3($model)
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
    public function getSchoolKind($model)
    {
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
    public function getGdat($model)
    {
        if ($model->mode == "grd") {
            $year = $model->grd_year;
        } else {
            $year = CTRL_YEAR;
        }

        $query  = " SELECT DISTINCT ";
        $query .= "     GRADE ";
        $query .= "   , GRADE_CD ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_GDAT ";
        $query .= " WHERE ";
        $query .= "     INT(YEAR) BETWEEN ".((int) $year - 3)." AND {$year} ";
        $query .= "     AND SCHOOL_KIND = '{$model->schoolKind}' ";
        $query .= "     AND INT(GRADE_CD) <= 3 ";
        $query .= " ORDER BY ";
        $query .= "     GRADE_CD ";

        return $query;
    }

    //学年（年次）コンボボックス用
    public function selectQueryAnnual($model)
    {
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
        $data["REMARK"][TEXT]         = $row_data["REMARK"];
        $data["REMARK2"][TEXT]        = $row_data["REMARK2"];
        $data["COMMENTEX_A_CD"][TEXT] = $model->field["COMMENTEX_A_CD"];
        if ($model->Properties["useTotalstudySlashFlg"] == 1) {
            $data["TOTALSTUDYACT_SLASH_FLG"][TEXT]  = $model->field["TOTALSTUDYACT_SLASH_FLG"];
            $data["TOTALSTUDYVAL_SLASH_FLG"][TEXT]  = $model->field["TOTALSTUDYVAL_SLASH_FLG"];
        }
        $data["REGISTERCD"][TEXT]     = STAFFCD;

        $query = Query::insertSQL($data, $tableName);
        $db->query($query);

        $tableName = "HEXAM_ENTREMARK_REMARK_HDAT";
        //削除
        $query  = " DELETE FROM ";
        $query .= "     {$tableName} ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO = '{$model->schregno}' ";

        $db->query($query);

        $data = array();
        $data["SCHREGNO"][TEXT]       = $model->schregno;
        $data["REMARK"][TEXT]         = isset($model->remark_no_comments) ? $model->no_comments_label : $model->field["REMARK"];
        $data["REGISTERCD"][TEXT]     = STAFFCD;
        $query = Query::insertSQL($data, $tableName);
        $db->query($query);
    }

    /* 更新処理 */
    public function update2(&$db, $model, $val)
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
        $query .= "       YEAR      = '{$val["YEAR"]}' ";
        $query .= "   AND SCHREGNO  = '{$model->schregno}' ";
        if ($model->control["学校区分"] == "0") {     //学年制
            $query .= "   AND ANNUAL    = '{$val["ANNUAL"]}' ";
        }

        $row_data = $db->getRow($query, DB_FETCHMODE_ASSOC);

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

        //レコードを一旦削除する
        $tableName = "HEXAM_ENTREMARK_TRAINREF_DAT";

        for ($i = 1; $i <= 6; $i++) {
            $seq = "10".$i;

            $query  = " SELECT ";
            $query .= "     * ";
            $query .= " FROM ";
            $query .= "     {$tableName} ";
            $query .= " WHERE ";
            $query .= "       SCHREGNO  = '{$model->schregno}' ";
            $query .= "       AND YEAR = '{$val["YEAR"]}' ";
            $query .= "       AND TRAIN_SEQ = '{$seq}' ";

            $row = $db->getRow($query, DB_FETCHMODE_ASSOC);

            //削除
            $query  = " DELETE FROM ";
            $query .= "     {$tableName} ";
            $query .= " WHERE ";
            $query .= "     SCHREGNO = '{$model->schregno}' ";
            $query .= "     AND YEAR = '{$val["YEAR"]}' ";
            $query .= "     AND TRAIN_SEQ = '{$seq}' ";

            $db->query($query);

            $data = array();
            $data["YEAR"][TEXT]           = $val["YEAR"];
            $data["SCHREGNO"][TEXT]       = $model->schregno;
            $data["TRAIN_SEQ"][TEXT]      = $seq;
            $data["REMARK"][TEXT]         = isset($model->field2[$val["YEAR"]]["TRAIN_REF".$i."_NO_COMM"]) ? $model->no_comments_label : $model->field2[$val["YEAR"]]["TRAIN_REF".($i == 1 ? "" : $i)];
            $data["REGISTERCD"][TEXT]     = STAFFCD;

            $query = Query::insertSQL($data, $tableName);
            $db->query($query);
        }
    }

//    //保健より読み込み
//    function selectQueryHealth($model) {
//         if ($model->mode == "grd") {     //卒業
//            $year = $model->grd_year;
//        } else {
//            $year = $model->year;
//        }
//        $query  = " SELECT ";
//        $query .= "     MEISYOU_GET(OTHERDISEASECD,'F140',1) AS OTHERDISEASECD, ";
//        $query .= "     DOC_REMARK ";
//        $query .= " FROM ";
//        if ($model->mode == "grd") {     //卒業
//            $query .= "   GRD_MEDEXAM_DET_DAT ";
//        } else {
//            $query .= "   MEDEXAM_DET_DAT ";
//        }
//        $query .= " WHERE ";
//        $query .= "     YEAR     = '{$year}' AND ";
//        $query .= "     SCHREGNO = '{$model->schregno}' ";
//
//        return $query;
//    }

    //学習指導要録より再読込
    public function selectQuery_Htrainremark_Dat($model, $year)
    {
        $query  = " SELECT ";
        $query .= "     T1.YEAR, ";
        $query .= "     T1.ANNUAL, ";
        $query .= "     T1.SPECIALACTREMARK AS SPECIALACTREC, ";
        $query .= "     T1.TOTALREMARK AS TRAIN_REF, ";
        $query .= "     T1.TOTALSTUDYACT, ";
        $query .= "     T1.TOTALSTUDYVAL, ";
        $query .= "     T1.ATTENDREC_REMARK ";
        $query .= " FROM ";
        if ($model->mode == "grd") {     //卒業
            $query .= "   GRD_HTRAINREMARK_DAT T1 ";
        } else {
            $query .= "   HTRAINREMARK_DAT T1 ";
        }
        $query .= " WHERE ";
        $query .= "         T1.YEAR      = '{$year}' ";
        $query .= "     AND T1.SCHREGNO  = '{$model->schregno}' ";

        return $query;
    }

    //学習指導要録より再読込(通年の総合的な学習の時間の内容・評価のセット用)
    public function getYourokuDat($model)
    {
        $query  = " SELECT ";
        $query .= "     T3.GRADE_NAME1, ";
        $query .= "     T1.TOTALSTUDYACT, ";
        $query .= "     T1.TOTALSTUDYVAL ";
        $query .= " FROM ";
        if ($model->mode == "grd") {     //卒業
            $query .= "   GRD_HTRAINREMARK_DAT T1 ";
        } else {
            $query .= "   HTRAINREMARK_DAT T1 ";
        }
        $query .= "   LEFT JOIN (SELECT DISTINCT SCHREGNO, YEAR, GRADE ";
        $query .= "              FROM SCHREG_REGD_DAT ";
        $query .= "              WHERE SCHREGNO = '{$model->schregno}') T2 ON T2.SCHREGNO = T1.SCHREGNO AND T2.YEAR = T1.YEAR ";
        $query .= "   LEFT JOIN SCHREG_REGD_GDAT T3 ON T3.YEAR = T1.YEAR AND T3.GRADE = T2.GRADE ";
        $query .= " WHERE ";
        if ($model->readYear !== '0000') {
            $query .= "      T1.YEAR      = '{$model->readYear}' AND ";
        } else {
            $query .= "      T1.YEAR      IN ($model->allYear) AND ";
        }
        $query .= "      T1.SCHREGNO  = '{$model->schregno}' ";
        $query .= "      AND VALUE(T3.SCHOOL_KIND, '') <> 'J' ";
        $query .= " ORDER BY ";
        $query .= "     T1.YEAR ";

        return $query;
    }

    //指導要録データ
    public function yoroku_trainref123($model, $year)
    {
        $query  = " SELECT ";
        $query .= "     TRAIN_REF1 ";
        $query .= "   , TRAIN_REF2 ";
        $query .= "   , TRAIN_REF3 ";
        $query .= " FROM ";
        $query .= "     HTRAINREMARK_DETAIL_DAT ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO = '{$model->schregno}' AND ";
        $query .= "     YEAR     = '{$year}' ";

        return $query;
    }

    //指導要録データ
    public function yoroku_sogoshoken($model, $year)
    {
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

    public function selectQuery_Htrainremark_datTotal($model)
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
        $query .= "     SCHREGNO  = '{$model->schregno}' ";
        $query .= " ORDER BY ";
        $query .= "     YEAR ";

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
        while ( $row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
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
    public function getHreportremarkDetailDat($db, $model, $year)
    {
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


    /* HR諸事項一括取込み */
    public function updateHrShojikou(&$db, $model)
    {

        $query  = " SELECT DISTINCT ";
        $query .= "   T2.SCHREGNO ";
        $query .= "  ,T4.YEAR ";
        $query .= " FROM ";
        if ($model->mode == "grd") {     //卒業
            $query .= "   GRD_REGD_HDAT T1 ";
            $query .= "   INNER JOIN GRD_REGD_DAT T2 ON ";
            $query .= "           T2.YEAR = T1.YEAR ";
            $query .= "           AND T2.SEMESTER = T1.SEMESTER ";
            $query .= "           AND T2.GRADE = T1.GRADE ";
            $query .= "           AND T2.HR_CLASS = T1.HR_CLASS ";
            $query .= "   LEFT JOIN GRD_REGD_DAT T4 ON T4.SCHREGNO = T2.SCHREGNO ";
        } else {
            $query .= "   SCHREG_REGD_HDAT T1 ";
            $query .= "   INNER JOIN SCHREG_REGD_DAT T2 ON ";
            $query .= "           T2.YEAR = T1.YEAR ";
            $query .= "           AND T2.SEMESTER = T1.SEMESTER ";
            $query .= "           AND T2.GRADE = T1.GRADE ";
            $query .= "           AND T2.HR_CLASS = T1.HR_CLASS ";
            $query .= "   LEFT JOIN SCHREG_REGD_DAT T4 ON T4.SCHREGNO = T2.SCHREGNO ";
        }
        $query .= "   LEFT JOIN SCHREG_REGD_GDAT GDAT ON GDAT.YEAR = T1.YEAR ";
        $query .= "           AND GDAT.GRADE = T1.GRADE ";
        $query .= " WHERE ";
        $query .= "       T1.YEAR     = '{$model->exp_year}' ";
        $query .= "   AND T1.SEMESTER = '{$model->exp_semester}' ";
        $query .= "   AND T1.GRADE || '-' || T1.HR_CLASS = '{$model->gradeHrClass}' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            $query .= "     AND GDAT.SCHOOL_KIND = '".$model->school_kind."' ";
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= "     AND GDAT.SCHOOL_KIND = '".SCHOOLKIND."' ";
        }

        $schregnoYears = array();
        $resultYearSchregno = $db->query($query);
        while ($rys = $resultYearSchregno->fetchRow(DB_FETCHMODE_ASSOC)) {
            $schregnoYears[] = "('".$rys["SCHREGNO"]."', '".$rys["YEAR"]."')";
        }
        $resultYearSchregno->free();

        if (!empty($schregnoYears)) {
            $query  = " DELETE FROM HEXAM_ENTREMARK_TRAINREF_DAT ";
            $query .= " WHERE ";
            $query .= "     (SCHREGNO, YEAR) IN (VALUES ".implode($schregnoYears, ", ").")";
            $query .= "     AND TRAIN_SEQ IN ('101', '102', '103', '104', '105', '106') ";

            $db->query($query);
        }

        $query  = " SELECT SCHREGNO, YEAR, REMARK ";
        $query .= "      , CASE TRAIN_SEQ WHEN '001' THEN '101' ";
        $query .= "                       WHEN '002' THEN '102' ";
        $query .= "                       WHEN '003' THEN '103' ";
        $query .= "                       WHEN '004' THEN '104' ";
        $query .= "                       WHEN '005' THEN '105' ";
        $query .= "                       WHEN '006' THEN '106' ";
        $query .= "        END AS TRAIN_SEQ ";
        $query .= " FROM HEXAM_ENTREMARK_TRAINREF_DAT ";
        $query .= " WHERE ";
        $query .= "     (SCHREGNO, YEAR) IN (VALUES ".implode($schregnoYears, ", ").")";
        $query .= "     AND TRAIN_SEQ IN ('001', '002', '003', '004', '005', '006') ";

        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $data = array();
            $data["YEAR"][TEXT]             = $row["YEAR"];
            $data["SCHREGNO"][TEXT]         = $row["SCHREGNO"];
            $data["TRAIN_SEQ"][TEXT]        = $row["TRAIN_SEQ"];
            $data["REMARK"][TEXT]           = $row["REMARK"];
            $data["REGISTERCD"][TEXT]       = STAFFCD;

            $query = Query::insertSQL($data, "HEXAM_ENTREMARK_TRAINREF_DAT");

            $db->query($query);
        }
        $result->free();
    }
}
