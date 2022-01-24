<?php

require_once('for_php7.php');
class knjc032aQuery extends Query {

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
        $query .= "     SHD.GRADE || '-' || SHD.HR_CLASS AS VALUE, ";
        $query .= "     SHD.HR_NAME AS LABEL";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_HDAT SHD";
        $query .= " INNER JOIN ";
        $query .= "     SCHREG_REGD_GDAT SGD";
        $query .= "     ON ";
        $query .= "     SHD.YEAR = SGD.YEAR AND ";
        $query .= "     SHD.GRADE = SGD.GRADE  ";
        $query .= " WHERE ";
        $query .= "     SHD.YEAR = '".$model->ctrl_year."' AND";
        $query .= "     SHD.SEMESTER = '".$model->ctrl_semester."' ";
        if(AUTHORITY != DEF_UPDATABLE){
            $query .= "     AND (TR_CD1 = '".STAFFCD."' ";
            $query .= "          OR TR_CD2 = '".STAFFCD."' ";
            $query .= "          OR TR_CD3 = '".STAFFCD."' ";
            $query .= "          OR SUBTR_CD1 = '".STAFFCD."' ";
            $query .= "          OR SUBTR_CD2 = '".STAFFCD."' ";
            $query .= "          OR SUBTR_CD3 = '".STAFFCD."') ";
        }

        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= "     AND SGD.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
            }
        } else if ($model->Properties["useSchool_KindField"] == "1") {
            $query .= "     AND SGD.SCHOOL_KIND IN ('".SCHOOLKIND."') ";
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
        $query  = " SELECT DISTINCT";
        $query .= "     YEAR, ";
        $query .= "     NAMECD2 AS MONTH, ";
        $query .= "     NAME1 AS MONTH_NAME, ";
        $query .= "     NAMESPARE1, ";
        $query .= "     SEMESTER, ";
        $query .= "     AM.APPOINTED_DAY, ";
        $query .= "     LESSON, ";
        $query .= "     OFFDAYS, ";
        $query .= "     ABROAD, ";
        $query .= "     ABSENT, ";
        $query .= "     SUSPEND, ";
        $query .= "     VIRUS, ";
        $query .= "     MOURNING, ";
        $query .= "     LESSON - SUSPEND - MOURNING - OFFDAYS - ABROAD - VIRUS ";
        if ($schoolMst["SEM_OFFDAYS"] == "1") {
            $query .= "     + OFFDAYS ";
        }
        $query .= "     AS CLASSDAYS2, ";
        $query .= "     SICK, ";
        $query .= "     NOTICE, ";
        $query .= "     NONOTICE, ";
        $query .= "     ((LESSON - (SUSPEND + MOURNING + OFFDAYS + ABROAD + VIRUS)) - (SICK + NOTICE + NONOTICE)) AS CLASSDAYS3, ";
        $query .= "     LATEDETAIL, ";
        $query .= "     KEKKA_JISU, ";
        $query .= "     KEKKA, ";
        $query .= "     CONTROL_CODE ";
        $query .= " FROM ";
        $query .= "     (SELECT DISTINCT ";
        $query .= "         NAMECD2, ";
        $query .= "         NAME1, ";
        $query .= "         NAMESPARE1 ";
        $query .= "     FROM ";
        $query .= "         NAME_MST ";
        $query .= "     WHERE ";
        $query .= "         NAMECD1 = 'Z005' AND ";
        $query .= "         NAMECD2 = '".sprintf('%02d',$month)."' ";
        $query .= "     ORDER BY ";
        $query .= "         NAMESPARE1 ";
        $query .= "     ) AS NM ";
        $query .= " LEFT OUTER JOIN ( ";
        $query .= "         SELECT ";
        $query .= "             * ";
        $query .= "         FROM ";
        $query .= "             ATTEND_SEMES_DAT ";
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
        $query .= "             CONTROL_FLG = '2'  ";
        $query .= " ) AS AC ON NM.NAMECD2 = AC.CONTROL_CODE LEFT OUTER JOIN ( ";
        $query .= "         SELECT ";
        $query .= "             MONTH, ";
        $query .= "             APPOINTED_DAY ";
        $query .= "         FROM ";
        $query .= "             APPOINTED_DAY_MST ";
        $query .= "         WHERE ";
        $query .= "             YEAR = '".$model->ctrl_year."' AND ";
        if ($model->Properties["useSchool_KindField"] == "1") {
            $query .= "             SCHOOL_KIND = '". $model->school_kind ."' AND ";
        }
        $query .= "             SEMESTER = '".$sem."' ";
        $query .= " ) AS AM ON NM.NAMECD2 = AM.MONTH ";
        $query .= " ORDER BY NAMESPARE1 ";

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
    function selectAttendSemester($model, $semester)
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
            $query .= "     sum(T1.VIRUS) AS VIRUS, ";
            $query .= "     sum(T1.MOURNING) AS MOURNING, ";
            $query .= "     sum(T1.LESSON) - sum(T1.SUSPEND) - sum(T1.MOURNING) - sum(T1.OFFDAYS) - sum(T1.ABROAD) - sum(T1.VIRUS) ";
            if ($schoolMst["SEM_OFFDAYS"] == "1") {
                $query .= "     + sum(T1.OFFDAYS) ";
            }
            $query .= "     AS CLASSDAYS2, ";
            $query .= "     sum(T1.SICK) AS SICK, ";
            $query .= "     sum(T1.NOTICE) AS NOTICE, ";
            $query .= "     sum(T1.NONOTICE) AS NONOTICE, ";
            $query .= "     ((sum(T1.LESSON) - (sum(T1.SUSPEND) + sum(T1.MOURNING) + sum(T1.OFFDAYS) + sum(T1.ABROAD) + sum(T1.VIRUS))) - (sum(T1.SICK) + sum(T1.NOTICE) + sum(T1.NONOTICE))) AS CLASSDAYS3, ";
            $query .= "     sum(T1.LATEDETAIL) AS LATEDETAIL, ";
            $query .= "     sum(T1.KEKKA_JISU) AS KEKKA_JISU, ";
            $query .= "     sum(T1.KEKKA) AS KEKKA ";
            $query .= " FROM ";
            $query .= "     attend_semes_dat T1 ";
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
                /* 更新対象データがある時 - UPDATE */
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
                $data["LATE"][NUMBER]   = 0;  /*今までの遅刻回数*/    /* 今までの遅刻回数と早退回数は    */
                $data["EARLY"][NUMBER]  = 0;  /*今までの早退回数*/    /* インサートの時だけ0をセットする */
            }

            /* 更新データをセット */
            $data["APPOINTED_DAY"][TEXT] = $model->field["APPOINTED_DAY"][$key]; //締め日
            $data["LESSON"][NUMBER]      = ($model->field["LESSON"][$key]) ?     $model->field["LESSON"][$key] : 0;     //授業日数
            $data["OFFDAYS"][NUMBER]     = ($model->field["OFFDAYS"][$key]) ?    $model->field["OFFDAYS"][$key] : 0;    //休学日数
            $data["ABROAD"][NUMBER]      = ($model->field["ABROAD"][$key]) ?     $model->field["ABROAD"][$key] : 0;     //留学日数
            $data["ABSENT"][NUMBER]      = ($model->field["ABSENT"][$key]) ?     $model->field["ABSENT"][$key] : 0;     //公欠日数
            $data["SUSPEND"][NUMBER]     = ($model->field["SUSPEND"][$key]) ?    $model->field["SUSPEND"][$key] : 0;    //出停日数
            $data["VIRUS"][NUMBER]       = ($model->field["VIRUS"][$key]) ?      $model->field["VIRUS"][$key] : 0;      //出停日数
            $data["MOURNING"][NUMBER]    = ($model->field["MOURNING"][$key]) ?   $model->field["MOURNING"][$key] : 0;   //忌引日数
            $data["SICK"][NUMBER]        = ($model->field["SICK"][$key]) ?       $model->field["SICK"][$key] : 0;       //病欠
            $data["NOTICE"][NUMBER]      = ($model->field["NOTICE"][$key]) ?     $model->field["NOTICE"][$key] : 0;     //事故欠届
            $data["NONOTICE"][NUMBER]    = ($model->field["NONOTICE"][$key]) ?   $model->field["NONOTICE"][$key] : 0;   //事故欠無
            $data["LATEDETAIL"][NUMBER]  = ($model->field["LATEDETAIL"][$key]) ? $model->field["LATEDETAIL"][$key] : 0; //遅刻回数
            $data["KEKKA_JISU"][NUMBER]  = ($model->field["KEKKA_JISU"][$key]) ? $model->field["KEKKA_JISU"][$key] : 0; //欠課時数
            $data["KEKKA"][NUMBER]       = ($model->field["KEKKA"][$key]) ?      $model->field["KEKKA"][$key] : 0;      //早退回数
            $data["REGISTERCD"][TEXT]    = STAFFCD ;    //登録者コード
            $data["UPDATED"][FUNC]       = "sysdate()"; //更新日付

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
        }

        $db->commit();
        Query::dbCheckIn($db);
        return true;
    }
}
?>
