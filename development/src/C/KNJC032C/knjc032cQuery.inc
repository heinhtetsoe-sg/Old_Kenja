<?php

require_once('for_php7.php');
class knjc032cQuery extends Query {

    /* 学期名取得 */
    function getSemesterName($year, $semester) {
        $query .= " SELECT ";
        $query .= "     SEMESTERNAME ";
        $query .= " FROM ";
        $query .= "     SEMESTER_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$year}' ";
        $query .= " AND SEMESTER = '{$semester}' ";

        return $query;
    }

    /* 学級名称取得 */
    function selectHrClass($model)
    {
        $query  = " SELECT ";
        $query .= "     T1.GRADE || '-' || T1.HR_CLASS AS VALUE, ";
        $query .= "     T1.HR_NAME AS LABEL";
        $query .= " FROM ";
        $query .= "     schreg_regd_hdat T1 ";
        $query .= " WHERE ";
        $query .= "     T1.year = '".$model->ctrl_year."' AND";
        $query .= "     T1.semester = '".$model->ctrl_semester."' ";
        if(AUTHORITY != DEF_UPDATABLE){
            $query .= "     AND (T1.TR_CD1 = '".STAFFCD."' ";
            $query .= "          OR T1.TR_CD2 = '".STAFFCD."' ";
            $query .= "          OR T1.TR_CD3 = '".STAFFCD."' ";
            $query .= "          OR T1.SUBTR_CD1 = '".STAFFCD."' ";
            $query .= "          OR T1.SUBTR_CD2 = '".STAFFCD."' ";
            $query .= "          OR T1.SUBTR_CD3 = '".STAFFCD."') ";
        }
        
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= " AND GRADE IN (  SELECT ";
                $query .= "                     GRADE ";
                $query .= "                 FROM ";
                $query .= "                     SCHREG_REGD_GDAT S1 ";
                $query .= "                 WHERE ";
                $query .= "                     S1.YEAR         = T1.YEAR AND ";
                $query .= "                     S1.SCHOOL_KIND  IN ('".implode("','", explode(":", $model->selectSchoolKind))."') ";
                $query .= "                 ) ";
            }
        } else if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND GRADE IN (  SELECT ";
            $query .= "                     GRADE ";
            $query .= "                 FROM ";
            $query .= "                     SCHREG_REGD_GDAT S1 ";
            $query .= "                 WHERE ";
            $query .= "                     S1.YEAR         = T1.YEAR AND ";
            $query .= "                     S1.SCHOOL_KIND  = '".SCHOOLKIND."' ";
            $query .= "                 ) ";
        }

        return $query;
    }

    /* 校種取得 */
    function getSchoolKind($model) {
        $db = Query::dbCheckOut();

        $query = "  SELECT ";
        $query .= "    SCHOOL_KIND ";
        $query .= "  FROM ";
        $query .= "    SCHREG_REGD_HDAT SHD ";
        $query .= "  INNER JOIN ";
        $query .= "    SCHREG_REGD_GDAT SGD ";
        $query .= "    ON SHD.YEAR = SGD.YEAR AND ";
        $query .= "    SHD.GRADE = SGD.GRADE ";
        $query .= "  WHERE ";
        $query .= "    SHD.YEAR     = '". CTRL_YEAR ."' AND ";
        $query .= "    SHD.SEMESTER = '". CTRL_SEMESTER ."' AND ";
        $query .= "    SHD.GRADE    = '". $model->field["grade"] ."' AND ";
        $query .= "    SHD.HR_CLASS = '". $model->field["class"] ."' ";

        $school_kind = $db->getOne($query);
        Query::dbCheckIn($db);

        return $school_kind;
    }

    /* 生徒データの取得 */
    function selectStudent($model)
    {
        $query  = " SELECT ";
        $query .= "     sd.schregno AS VALUE, ";
        $query .= "     sd.attendno || ' ' || sm.name_show AS LABEL";
        $query .= " FROM ";
        $query .= "     schreg_regd_dat AS sd ";
        $query .= " LEFT OUTER JOIN ";
        $query .= "     schreg_base_mst AS sm ";
        $query .= " ON sd.schregno = sm.schregno ";
        $query .= " WHERE ";
        $query .= "     sd.year = '".$model->ctrl_year."' AND ";
        $query .= "     sd.semester = '".$model->ctrl_semester."' AND ";
        $query .= "     sd.grade = '".$model->field["grade"]."' AND ";
        $query .= "     sd.hr_class = '".$model->field["class"]."' ";
        $query .= " ORDER BY ";
        $query .= "     attendno ";

        return $query;
    }

    //欠課種別取得
    function getSickDiv()
    {
        $query  = "SELECT ";
        $query .= "    NAMECD2 AS VALUE, ";
        $query .= "    NAME1 AS LABEL ";
        $query .= "FROM ";
        $query .= "    V_NAME_MST ";
        $query .= "WHERE ";
        $query .= "    YEAR = '".CTRL_YEAR."' ";
        $query .= "    AND NAMECD1 = 'C001' ";
        $query .= "    AND NAMECD2 IN ('4', '5', '6') ";
        $query .= "ORDER BY ";
        $query .= "    NAMECD2 ";

        return $query;
    }

    //詳細テーブル用種別
    function getDetailDiv()
    {
        $query  = " SELECT ";
        $query .= "     NAMECD2 AS VALUE, ";
        $query .= "     NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND NAMECD1 = 'C002' ";
        $query .= " ORDER BY ";
        $query .= "     NAMECD2 ";

        return $query;
    }

    //学校マスタの取得
    function getSchoolMst() {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     SCHOOL_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '". CTRL_YEAR ."' ";

        return $query;
    }

    /* 出欠月別累積データ・管理者コントロールデータ */
    function selectAttendQuery($model, $month, $sem, $schoolMst)
    {
        $setNameCd = "Z005";
        if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $setNameCd = "Z".SCHOOLKIND."05";
        }
        $query  = " SELECT ";
        $query .= "     AD.YEAR, ";
        $query .= "     NAMECD2 AS MONTH, ";
        $query .= "     NAME1 AS MONTH_NAME, ";
        $query .= "     NAMESPARE1, ";
        $query .= "     AD.SEMESTER, ";
        $query .= "     AM.APPOINTED_DAY, ";
        $query .= "     LESSON, ";
        $query .= "     OFFDAYS, ";
        $query .= "     ABROAD, ";
        $query .= "     ABSENT, ";
        $query .= "     SUSPEND, ";
        if ($model->Properties["useKoudome"] == "true") {
            $query .= "     KOUDOME, ";
        }
        if ($model->Properties["useVirus"] == "true") {
            $query .= "     VIRUS, ";
        }
        $query .= "     MOURNING, ";
        $query .= "     VALUE(LESSON, 0) - VALUE(SUSPEND, 0) - VALUE(MOURNING, 0) - VALUE(OFFDAYS, 0) - VALUE(ABROAD, 0) ";
        if ($schoolMst["SEM_OFFDAYS"] == "1") {
            $query .= "     + VALUE(OFFDAYS, 0) ";
        }
        if ($model->Properties["useKoudome"] == "true") {
            $query .= "     - VALUE(KOUDOME, 0) ";
        }
        if ($model->Properties["useVirus"] == "true") {
            $query .= "     - VALUE(VIRUS, 0) ";
        }
        $query .= "     AS CLASSDAYS2, ";
        $query .= "     SICK, ";
        $query .= "     NOTICE, ";
        $query .= "     NONOTICE, ";
        $query .= "     VALUE(LESSON, 0) - VALUE(SUSPEND, 0) - VALUE(MOURNING, 0) - VALUE(OFFDAYS, 0) - VALUE(ABROAD, 0) - VALUE(SICK, 0) - VALUE(NOTICE, 0) - VALUE(NONOTICE, 0) ";
        if ($model->Properties["useKoudome"] == "true") {
            $query .= "     - VALUE(KOUDOME, 0) ";
        }
        if ($model->Properties["useVirus"] == "true") {
            $query .= "     - VALUE(VIRUS, 0) ";
        }
        $query .= "     AS CLASSDAYS3, ";
        $query .= "     LATE, ";
        $query .= "     EARLY, ";
        if ($model->Properties["use_Attend_zero_hyoji"] == "1") {
            $query .= "     L1.CNT AS DETAIL_001, ";
            $query .= "     L2.CNT AS DETAIL_002, ";
            $query .= "     L3.CNT AS DETAIL_003, ";
            $query .= "     L4.CNT AS DETAIL_004, ";
        } else {
            $query .= "     REIHAI_KEKKA AS DETAIL_001, ";
            $query .= "     M_KEKKA_JISU AS DETAIL_002, ";
            $query .= "     REIHAI_TIKOKU AS DETAIL_003, ";
            $query .= "     JYUGYOU_TIKOKU AS DETAIL_004, ";
        }
        $query .= "     CONTROL_CODE, ";
        $query .= "     AD.SCHREGNO AS SEM_SCHREGNO ";
        $query .= " FROM ";
        $query .= "     (SELECT DISTINCT ";
        $query .= "         NAMECD2, ";
        $query .= "         NAME1, ";
        $query .= "         NAMESPARE1 ";
        $query .= "     FROM ";
        $query .= "         NAME_MST ";
        $query .= "     WHERE ";
        $query .= "         NAMECD1 = '{$setNameCd}' AND ";
        $query .= "         NAMECD2 = '".sprintf('%02d',$month)."' ";
        $query .= "     ORDER BY ";
        $query .= "         NAMESPARE1 ";
        $query .= "     ) AS NM ";
        $query .= " LEFT OUTER JOIN ( ";
        $query .= "         SELECT ";
        $query .= "             * ";
        $query .= "         FROM ";
        $query .= "             V_ATTEND_SEMES_DAT ";
        $query .= "         WHERE ";
        $query .= "             YEAR = '".$model->ctrl_year."' AND ";
        $query .= "             SEMESTER = '".$sem."' AND ";
        $query .= "             SCHREGNO = '".$model->field["schregno"]."' ";
        $query .= " ) AS AD ";
        $query .= " ON AD.MONTH = NM.NAMECD2 LEFT OUTER JOIN ( ";
        $query .= "         SELECT ";
        $query .= "             CONTROL_CODE ";
        $query .= "         FROM ";
        $query .= "             ADMIN_CONTROL_DAT ";
        $query .= "         WHERE ";
        $query .= "             YEAR = '".$model->ctrl_year."' AND ";
        if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= "                     SCHOOL_KIND = '".SCHOOLKIND."' AND ";
        }
        $query .= "             CONTROL_FLG = '2'  ";
        $query .= " ) AS AC ON NM.NAMECD2 = AC.CONTROL_CODE LEFT OUTER JOIN ( ";
        $query .= "         SELECT ";
        $query .= "             MONTH, ";
        $query .= "             APPOINTED_DAY ";
        $query .= "         FROM ";
        $query .= "             APPOINTED_DAY_MST ";
        $query .= "         WHERE ";
        $query .= "             YEAR        = '".$model->ctrl_year."' AND ";
        if ($model->Properties["useSchool_KindField"] == "1") {
            $query .= "             SCHOOL_KIND = '".$model->school_kind."' AND ";
        }
        $query .= "             SEMESTER    = '".$sem."' ";
        $query .= " ) AS AM ON NM.NAMECD2 = AM.MONTH ";
        $query .= "     LEFT JOIN ATTEND_SEMES_DETAIL_DAT L1 ";
        $query .= "          ON AD.COPYCD   = L1.COPYCD ";
        $query .= "         AND AD.YEAR     = L1.YEAR ";
        $query .= "         AND AD.MONTH    = L1.MONTH ";
        $query .= "         AND AD.SEMESTER = L1.SEMESTER ";
        $query .= "         AND AD.SCHREGNO = L1.SCHREGNO ";
        $query .= "         AND L1.SEQ      = '001' ";
        $query .= "     LEFT JOIN ATTEND_SEMES_DETAIL_DAT L2 ";
        $query .= "          ON AD.COPYCD   = L2.COPYCD ";
        $query .= "         AND AD.YEAR     = L2.YEAR ";
        $query .= "         AND AD.MONTH    = L2.MONTH ";
        $query .= "         AND AD.SEMESTER = L2.SEMESTER ";
        $query .= "         AND AD.SCHREGNO = L2.SCHREGNO ";
        $query .= "         AND L2.SEQ      = '002' ";
        $query .= "     LEFT JOIN ATTEND_SEMES_DETAIL_DAT L3 ";
        $query .= "          ON AD.COPYCD   = L3.COPYCD ";
        $query .= "         AND AD.YEAR     = L3.YEAR ";
        $query .= "         AND AD.MONTH    = L3.MONTH ";
        $query .= "         AND AD.SEMESTER = L3.SEMESTER ";
        $query .= "         AND AD.SCHREGNO = L3.SCHREGNO ";
        $query .= "         AND L3.SEQ      = '003' ";
        $query .= "     LEFT JOIN ATTEND_SEMES_DETAIL_DAT L4 ";
        $query .= "          ON AD.COPYCD   = L4.COPYCD ";
        $query .= "         AND AD.YEAR     = L4.YEAR ";
        $query .= "         AND AD.MONTH    = L4.MONTH ";
        $query .= "         AND AD.SEMESTER = L4.SEMESTER ";
        $query .= "         AND AD.SCHREGNO = L4.SCHREGNO ";
        $query .= "         AND L4.SEQ      = '004' ";
        $query .= " ORDER BY NAMESPARE1 ";

        return $query;
    }

    //締め日の取得
    function getAppointedDay($month, $semester, $model) {
        $query  = " SELECT ";
        $query .= "     APPOINTED_DAY ";
        $query .= " FROM ";
        $query .= "     APPOINTED_DAY_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '". CTRL_YEAR ."' AND ";
        if ($model->Properties["useSchool_KindField"] == "1") {
            $query .= "     SCHOOL_KIND = '". $model->school_kind ."' AND ";
        }
        $query .= "     MONTH = '".$month."' AND ";
        $query .= "     SEMESTER = '".$semester."' ";

        return $query;
    }

    //異動データ取得
    function getIdouData($schregno, $date) {

        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     SCHREG_BASE_MST ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO = '".$schregno."' AND ";
        $query .= "     GRD_DIV IN ('1', '2', '3', '6') AND ";
        $query .= "     GRD_DATE < '".$date."' ";

        return $query;
    }

    //異動データ（留学・休学）取得
    function getTransferData1($schregno, $date) {

        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     SCHREG_TRANSFER_DAT ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO = '".$schregno."' AND ";
        $query .= "     TRANSFERCD IN ('1', '2') AND ";
        $query .= "     '".$date."' BETWEEN TRANSFER_SDATE AND CASE WHEN TRANSFER_EDATE IS NULL THEN '".(CTRL_YEAR+1)."' || '-03-31' ELSE TRANSFER_EDATE END ";

        return $query;
    }

    //異動日付（留学・休学）取得
    function getTransferData2($schregno, $month) {

        $query  = " SELECT ";
        if($month == "s") {
            $query .= "     MONTH(TRANSFER_SDATE) AS S_MONTH ";
        } else {
            $query .= "     MONTH(TRANSFER_EDATE) AS E_MONTH ";
        }
        $query .= " FROM ";
        $query .= "     SCHREG_TRANSFER_DAT ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO = '".$schregno."' AND ";
        $query .= "     TRANSFERCD IN ('1', '2') AND ";
        $query .= "     TRANSFER_SDATE BETWEEN '".CTRL_YEAR."-04-01' AND '".(CTRL_YEAR+1)."-03-31' ";

        return $query;
    }

    //メインデータ作成
    function selectSemesAll($seme = "")
    {
        $query  = " SELECT ";
        $query .= "     SEMESTER, ";
        if (!$seme) {
            $query .= "     CASE WHEN MONTH(SDATE) < 4 ";
            $query .= "          THEN MONTH(SDATE) + 12 ";
            $query .= "          ELSE MONTH(SDATE) END AS S_MONTH, ";
            $query .= "     CASE WHEN MONTH(EDATE) < 4 ";
            $query .= "          THEN MONTH(EDATE) + 12 ";
            $query .= "          ELSE MONTH(EDATE) END AS E_MONTH ";
        } else {
            $query .= "     SEMESTERNAME, ";
            $query .= "     MONTH(SDATE) AS S_MONTH, ";
            $query .= "     DAY(SDATE) AS S_DAY, ";
            $query .= "     MONTH(EDATE) AS E_MONTH, ";
            $query .= "     DAY(EDATE) AS E_DAY ";
        }
        $query .= " FROM ";
        $query .= "     SEMESTER_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        if (!$seme) {
            $query .= "     AND SEMESTER <> '9' ";
        } else {
            $query .= "     AND SEMESTER = '".$seme."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     SEMESTER ";

        return $query;
    }

    /* 出欠累積データ学期計・累積計 */
    function selectAttendSemester($model, $semester, $kahenField, $schoolMst)
    {
        if ($semester == 0 && !$model->field["schregno"]) {
            $query  = "VALUES ('','','','','','','','','','','','','','')";
        } else if ($semester > 0 && !$model->field["schregno"]) {
            $query  = "SELECT ";
            $query .= "     SEMESTERNAME || '計' AS MONTH_NAME ";
            $query .= "FROM ";
            $query .= "     SEMESTER_MST ";
            $query .= "WHERE ";
            $query .= "     YEAR = '".CTRL_YEAR."' ";
            $query .= "     AND SEMESTER = '".$semester."' ";
        } else {
            $query  = " SELECT ";
            if ($semester > 0) {
                $query .= "     L1.SEMESTERNAME || '計' AS MONTH_NAME, ";
            }
            $query .= "     '' AS APPOINTED_DAY, ";
            $query .= "     sum(T1.LESSON) AS LESSON, ";
            $query .= "     sum(T1.OFFDAYS) AS OFFDAYS, ";
            $query .= "     sum(T1.ABROAD) AS ABROAD, ";
            $query .= "     sum(T1.ABSENT) AS ABSENT, ";
            $query .= "     sum(T1.SUSPEND) AS SUSPEND, ";
            if ($model->Properties["useKoudome"] == "true") {
                $query .= "     sum(T1.KOUDOME) AS KOUDOME, ";
            }
            if ($model->Properties["useVirus"] == "true") {
                $query .= "     sum(T1.VIRUS) AS VIRUS, ";
            }
            $query .= "     sum(T1.MOURNING) AS MOURNING, ";
            $query .= "     (sum(VALUE(T1.LESSON, 0)) - (sum(VALUE(T1.SUSPEND, 0)) + sum(VALUE(T1.MOURNING, 0)) + sum(VALUE(T1.OFFDAYS, 0)) + sum(VALUE(T1.ABROAD, 0)))) ";
            if ($schoolMst["SEM_OFFDAYS"] == "1") {
                $query .= "     + sum(VALUE(T1.OFFDAYS, 0)) ";
            }
            if ($model->Properties["useKoudome"] == "true") {
                $query .= "     - sum(VALUE(T1.KOUDOME, 0)) ";
            }
            if ($model->Properties["useVirus"] == "true") {
                $query .= "     - sum(VALUE(T1.VIRUS, 0)) ";
            }
            $query .= "      AS CLASSDAYS2, ";

            $setFieldName = array("4" => "SICK", "5" => "NOTICE", "6" => "NONOTICE");
            $kahenField["C001"] = is_array($kahenField["C001"]) ? $kahenField["C001"] : array();
            foreach ($kahenField["C001"] as $c001Key => $c001Val) {
                $query .= "     sum(T1.".$setFieldName[$c001Val].") AS ".$setFieldName[$c001Val].", ";
            }

            $query .= "     ((sum(VALUE(T1.LESSON, 0)) - (sum(VALUE(T1.SUSPEND, 0)) + sum(VALUE(T1.MOURNING, 0)) + sum(VALUE(T1.OFFDAYS, 0)) + sum(VALUE(T1.ABROAD, 0)) ";
            if ($model->Properties["useKoudome"] == "true") {
                $query .= "     + sum(VALUE(T1.KOUDOME, 0)) ";
            }
            if ($model->Properties["useVirus"] == "true") {
                $query .= "     + sum(VALUE(T1.VIRUS, 0)) ";
            }
            $query .= "     )) - (sum(VALUE(T1.SICK, 0)) + sum(VALUE(T1.NOTICE, 0)) + sum(VALUE(T1.NONOTICE, 0)))) AS CLASSDAYS3, ";
            $query .= "     sum(T1.LATE) AS LATE, ";
            $query .= "     sum(T1.EARLY) AS EARLY ";

            $setDetailName = array("001" => "REIHAI_KEKKA", "002" => "KEKKA_JISU", "003" => "REIHAI_TIKOKU", "004" => "JYUGYOU_TIKOKU");
            $kahenField["C002"] = is_array($kahenField["C002"]) ? $kahenField["C002"] : array();
            foreach ($kahenField["C002"] as $c002Key => $c002Val) {
                if ($setDetailName[$c002Val]) {
                    $query .= "     ,sum(T1.".$setDetailName[$c002Val].") AS DETAIL_".$c002Val." ";
                }
            }

            $query .= " FROM ";
            $query .= "     V_ATTEND_SEMES_DAT T1 ";
            $query .= "     LEFT JOIN SEMESTER_MST L1 ON L1.YEAR = T1.YEAR ";
            $query .= "     AND L1.SEMESTER = T1.SEMESTER ";
            $query .= " WHERE ";
            $query .= "     T1.schregno = '".$model->field["schregno"]."' ";
            $query .= "     AND T1.year = '".CTRL_YEAR."' ";
            if ($semester > 0) {
                $query .= "     AND T1.semester = '".$semester."' ";
                $query .= " GROUP BY ";
                $query .= "     L1.SEMESTERNAME ";
            }
        }
        return $query;
    }

    /* 出欠月別累積データの更新 */
    function getUpdateQuery($model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        foreach($model->field["MONTH"] as $key => $month){
            $monthAr = preg_split("/-/", $month);
            /* 更新対象データが存在するかをチェック */
            $query  = " SELECT ";
            $query .= "     COUNT(*) ";
            $query .= " FROM ";
            $query .= "     attend_semes_dat ";
            $query .= " WHERE ";
            $query .= "     year = '".$model->ctrl_year."' AND ";
            $query .= "     SEMESTER = '".$monthAr[1]."' AND ";
            $query .= "     month = '".$monthAr[0]."' AND ";
            $query .= "     schregno = '".$model->field["schregno"]."' ";

            $flag = $db->getOne($query);
            $data = array();

            if ($flag) {
                $where  = " WHERE ";
                $where .= "     year = '".$model->ctrl_year."' AND ";
                $where .= "     SEMESTER = '".$monthAr[1]."' AND ";
                $where .= "     month = '".$monthAr[0]."' AND ";
                $where .= "     schregno = '".$model->field["schregno"]."' ";
            } else {
                /* 更新対象データがない時 - INSERT */
                $data["COPYCD"][TEXT]   = "0";
                $data["YEAR"][TEXT]     = $model->ctrl_year;
                $data["MONTH"][TEXT]    = $monthAr[0];
                $data["SEMESTER"][TEXT] = $monthAr[1];
                $data["SCHREGNO"][TEXT] = $model->field["schregno"];
            }
            /* 更新データをセット */
            $data["APPOINTED_DAY"][TEXT] = $model->field["APPOINTED_DAY"][$key];     //締め日
            $data["LESSON"][NUMBER]      = ($model->field["LESSON"][$key]) ?   $model->field["LESSON"][$key] : 0;      //授業日数
            if ($model->Properties["use_Attend_zero_hyoji"] == "1") {
                $data["OFFDAYS"][NUMBER]    = $model->field["OFFDAYS"][$key];       //休学日数
                $data["ABROAD"][NUMBER]     = $model->field["ABROAD"][$key];        //留学日数
                $data["ABSENT"][NUMBER]     = $model->field["ABSENT"][$key];        //公欠日数
                $data["SUSPEND"][NUMBER]    = $model->field["SUSPEND"][$key];       //出停日数
                $data["MOURNING"][NUMBER]   = $model->field["MOURNING"][$key];      //忌引日数
                $data["SICK"][NUMBER]       = $model->field["SICK"][$key];          //病欠
                $data["NOTICE"][NUMBER]     = $model->field["NOTICE"][$key];        //事故欠届
                $data["NONOTICE"][NUMBER]   = $model->field["NONOTICE"][$key];      //事故欠無
                $data["LATE"][NUMBER]       = $model->field["LATE"][$key];          //遅刻回数
                $data["EARLY"][NUMBER]      = $model->field["EARLY"][$key];         //早退回数
                if ($model->Properties["useVirus"] == "true") {
                    $data["VIRUS"][NUMBER]      = $model->field["VIRUS"][$key];         //出停（伝染病）日数
                }
                if ($model->Properties["useKoudome"] == "true") {
                    $data["KOUDOME"][NUMBER]    = $model->field["KOUDOME"][$key];       //出停（交止）日数
                }
            } else {
                $data["OFFDAYS"][NUMBER]     = ($model->field["OFFDAYS"][$key]) ?  $model->field["OFFDAYS"][$key] : 0;     //休学日数
                $data["ABROAD"][NUMBER]      = ($model->field["ABROAD"][$key]) ?   $model->field["ABROAD"][$key] : 0;      //留学日数
                $data["ABSENT"][NUMBER]      = ($model->field["ABSENT"][$key]) ?   $model->field["ABSENT"][$key] : 0;      //公欠日数
                $data["SUSPEND"][NUMBER]     = ($model->field["SUSPEND"][$key]) ?  $model->field["SUSPEND"][$key] : 0;     //出停日数
                $data["MOURNING"][NUMBER]    = ($model->field["MOURNING"][$key]) ? $model->field["MOURNING"][$key] : 0;    //忌引日数
                $data["SICK"][NUMBER]        = ($model->field["SICK"][$key]) ?     $model->field["SICK"][$key] : 0;        //病欠
                $data["NOTICE"][NUMBER]      = ($model->field["NOTICE"][$key]) ?   $model->field["NOTICE"][$key] : 0;      //事故欠届
                $data["NONOTICE"][NUMBER]    = ($model->field["NONOTICE"][$key]) ? $model->field["NONOTICE"][$key] : 0;    //事故欠無
                $data["LATE"][NUMBER]        = ($model->field["LATE"][$key]) ?     $model->field["LATE"][$key] : 0;        //遅刻回数
                $data["EARLY"][NUMBER]       = ($model->field["EARLY"][$key]) ?    $model->field["EARLY"][$key] : 0;       //早退回数
                if ($model->Properties["useVirus"] == "true") {
                    $data["VIRUS"][NUMBER]       = ($model->field["VIRUS"][$key]) ?    $model->field["VIRUS"][$key] : 0;       //出停（伝染病）日数
                }
                if ($model->Properties["useKoudome"] == "true") {
                    $data["KOUDOME"][NUMBER]     = ($model->field["KOUDOME"][$key]) ?  $model->field["KOUDOME"][$key] : 0;     //出停（交止）日数
                }
                if ($model->Properties["useKekkaJisu"] == "true") {
                    $data["KEKKA_JISU"][NUMBER]  = 0;     //使用しない
                }
                if ($model->Properties["useKekka"] == "true") {
                    $data["KEKKA"][NUMBER]       = 0;     //使用しない
                }
                if ($model->Properties["useLatedetail"] == "true") {
                    $data["LATEDETAIL"][NUMBER]  = 0;     //使用しない
                }
            }
            $data["REGISTERCD"][TEXT]    = STAFFCD ;                                 //登録者コード
            $data["UPDATED"][FUNC]       = "sysdate()";                              //更新日付

            if ("" != $model->field["APPOINTED_DAY"][$key]) {
                /* 更新対象データがある時 - UPDATE */
                if($flag){
                    $query = Query::updateSQL($data, "ATTEND_SEMES_DAT", $where);
                }
                /* 更新対象データがない時 - INSERT */
                else{
                    $query = Query::insertSQL($data, "ATTEND_SEMES_DAT");
                }
            } else {
                if ($flag) {
                    $query = "DELETE FROM ATTEND_SEMES_DAT ".$where;
                }
            }
            $db->query($query);

            //DETAIL更新
            $c002Field = preg_split("{,}", $model->set_detail_field);
            $c002Field = is_array($c002Field) ? $c002Field : array();
            foreach ($c002Field as $c002Key => $c002Val) {
                $c002Val = str_replace("DETAIL_", "", $c002Val);
                $c002Val = str_replace("[]", "", $c002Val);
                /* 更新対象データが存在するかをチェック */
                $query  = " SELECT ";
                $query .= "     COUNT(*) ";
                $query .= " FROM ";
                $query .= "     ATTEND_SEMES_DETAIL_DAT ";
                $query .= " WHERE ";
                $query .= "     YEAR = '".CTRL_YEAR."' ";
                $query .= "     AND MONTH = '".$monthAr[0]."' ";
                $query .= "     AND SEMESTER = '".$monthAr[1]."' ";
                $query .= "     AND SCHREGNO = '".$model->field["schregno"]."' ";
                $query .= "     AND SEQ = '".$c002Val."' ";

                $flag = $db->getOne($query);

                $data = array();

                /* 更新対象データがある時 - UPDATE */
                if ($flag) {
                    $where  = " WHERE ";
                    $where .= "     YEAR = '".CTRL_YEAR."' ";
                    $where .= "     AND MONTH = '".$monthAr[0]."' ";
                    $where .= "     AND SEMESTER = '".$monthAr[1]."' ";
                    $where .= "     AND SCHREGNO = '".$model->field["schregno"]."' ";
                    $where .= "     AND SEQ = '".$c002Val."' ";
                } else {
                    $data["COPYCD"][TEXT]   = "0";
                    $data["YEAR"][TEXT]     = CTRL_YEAR;
                    $data["MONTH"][TEXT]    = $monthAr[0];
                    $data["SEMESTER"][TEXT] = $monthAr[1];
                    $data["SCHREGNO"][TEXT] = $model->field["schregno"];
                    $data["SEQ"][TEXT]      = $c002Val;
                }

                /* 更新データをセット */
                if ($model->Properties["use_Attend_zero_hyoji"] == "1") {
                    $data["CNT"][NUMBER]        = $model->field["DETAIL_".$c002Val][$key];
                } else {
                    $data["CNT"][NUMBER]        = ($model->field["DETAIL_".$c002Val][$key])  ?  $model->field["DETAIL_".$c002Val][$key] : 0;
                }
                $data["REGISTERCD"][TEXT]   = STAFFCD;
                $data["UPDATED"][FUNC]      = "sysdate()";

                /* 更新対象データがある時 - UPDATE */
                if($flag){
                    $query = Query::updateSQL($data, "ATTEND_SEMES_DETAIL_DAT", $where);
                }
                /* 更新対象データがない時 - INSERT */
                else{
                    $query = Query::insertSQL($data, "ATTEND_SEMES_DETAIL_DAT");
                }
                $db->query($query);

            }

        }

        $db->commit();
        Query::dbCheckIn($db);
        return true;
    }
}
?>
