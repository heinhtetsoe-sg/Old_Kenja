<?php

require_once('for_php7.php');

class knjc034aQuery extends Query {

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

    //科目データの取得
    function selectSubclassQuery($model) {
        $query  = "   SELECT ";
        if ($model->Properties["useCurriculumcd"] == "1") {
            $query .= "       t3.CLASSCD || '-' || t3.SCHOOL_KIND || '-' ||  t3.CURRICULUM_CD || '-' || t3.SUBCLASSCD AS VALUE, ";
            $query .= "       t3.CLASSCD || '-' || t3.SCHOOL_KIND || '-' ||  t3.CURRICULUM_CD || '-' || t3.SUBCLASSCD || VALUE(t3.SUBCLASSABBV, '') AS LABEL ";
        } else {
            $query .= "       t3.SUBCLASSCD AS VALUE, ";
            $query .= "       t3.SUBCLASSCD || VALUE(t3.SUBCLASSABBV, '') AS LABEL ";
        }
        $query .= "   FROM ";
        $query .= "       CHAIR_DAT t1, ";
        $query .= "       CHAIR_STF_DAT t2, ";
        $query .= "       SUBCLASS_MST t3 ";
        $query .= "   WHERE ";
        $query .= "       t1.YEAR = '".CTRL_YEAR."' ";
        $query .= "       AND t1.YEAR = t2.YEAR ";
        $query .= "       AND t1.SEMESTER = '".CTRL_SEMESTER."' ";
        $query .= "       AND t1.SEMESTER = t2.SEMESTER ";
        $query .= "       AND t1.CHAIRCD = t2.CHAIRCD ";
        //更新可能(制限付)
        if(AUTHORITY != DEF_UPDATABLE){
            $query .= "         AND t2.STAFFCD = '".STAFFCD."' ";
        }
        if ($model->Properties["useCurriculumcd"] == "1") {
            $query .= "       AND t1.CLASSCD = t3.CLASSCD ";
            $query .= "       AND t1.SCHOOL_KIND = t3.SCHOOL_KIND ";
            $query .= "       AND t1.CURRICULUM_CD = t3.CURRICULUM_CD ";
        }
        $query .= "       AND t1.SUBCLASSCD = t3.SUBCLASSCD ";
        $query .= "   GROUP BY ";
        if ($model->Properties["useCurriculumcd"] == "1") {
            $query .= "      t3.CLASSCD,";
            $query .= "      t3.SCHOOL_KIND,";
            $query .= "      t3.CURRICULUM_CD,";
        }
        $query .= "       t3.SUBCLASSCD, ";
        $query .= "       t3.SUBCLASSABBV ";
        $query .= "   ORDER BY ";
        if ($model->Properties["useCurriculumcd"] == "1") {
            $query .= "      t3.CLASSCD,";
            $query .= "      t3.SCHOOL_KIND,";
            $query .= "      t3.CURRICULUM_CD,";
        }
        $query .= "       t3.SUBCLASSCD ";
        return $query;
    }

    //講座データの取得
    function selectChairQuery(&$model)
    {
        $query  = "   SELECT DISTINCT";
        $query .= "       t1.CHAIRCD AS VALUE, ";
        $query .= "       t1.CHAIRCD || VALUE(t1.CHAIRNAME, '') AS LABEL ";
        $query .= "   FROM ";
        $query .= "       CHAIR_DAT t1, ";
        $query .= "       CHAIR_STF_DAT t2 ";
        $query .= "   WHERE ";
        $query .= "       t1.YEAR = '".CTRL_YEAR."' ";
        $query .= "       AND t1.YEAR = t2.YEAR ";
        $query .= "       AND t1.SEMESTER = t2.SEMESTER ";
        $query .= "       AND t1.CHAIRCD = t2.CHAIRCD ";
        //更新可能(制限付)
        if (AUTHORITY != DEF_UPDATABLE) {
            $query .= "         AND t2.STAFFCD = '".STAFFCD."' ";
        }
        if ($model->Properties["useCurriculumcd"] == "1") {
            $query .= "       AND t1.CLASSCD || '-' || t1.SCHOOL_KIND || '-' ||  t1.CURRICULUM_CD || '-' || t1.SUBCLASSCD = '".$model->field["SUBCLASSCD"]."' ";
        } else {
            $query .= "       AND t1.SUBCLASSCD = '".$model->field["SUBCLASSCD"]."' ";
        }
        $query .= "   ORDER BY ";
        $query .= "       t1.CHAIRCD ";
        return $query;
    }

    /* 生徒データの取得 */
    function selectStudent($model)
    {
        $query  = " SELECT ";
        $query .= "     t1.schregno AS VALUE, ";
        $query .= "     VALUE(t3.hr_nameabbv, '') || VALUE(t1.name_show,'') || VALUE(t4.attendno, '') AS LABEL ";
        $query .= " FROM ";
        $query .= "     schreg_base_mst t1, chair_std_dat t2, schreg_regd_hdat t3, schreg_regd_dat t4 ";
        $query .= " WHERE ";
        $query .= "     t1.schregno = t2.schregno ";
        $query .= "     AND t1.schregno = t4.schregno ";
        $query .= "     AND t2.year = '".CTRL_YEAR."' ";
        $query .= "     AND t2.year = t3.year ";
        $query .= "     AND t2.year = t4.year ";
        $query .= "     AND t2.chaircd = '".$model->field["CHAIRCD"]."' ";
        $query .= "     AND t3.grade = t4.grade ";
        $query .= "     AND t3.hr_class = t4.hr_class ";
        $query .= "     AND t3.semester = '".CTRL_SEMESTER."' ";
        $query .= "     AND t3.semester = t4.semester ";
        $query .= " GROUP BY ";
        $query .= "     t1.schregno, t1.name_show, t3.hr_nameabbv, t3.grade, t3.hr_class, t4.attendno ";
        $query .= " ORDER BY ";
        $query .= "     t3.grade, t3.hr_class, t4.attendno ";

        return $query;
    }

    /* 生徒学年 */
    function getStudentGrade($model)
    {
        $query  = " SELECT ";
        $query .= "     GRADE ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND SEMESTER = '".CTRL_SEMESTER."' ";
        $query .= "     AND SCHREGNO = '{$model->field["SCHREGNO"]}' ";

        return $query;
    }

    /* 出欠月別累積データ・管理者コントロールデータ */
    function selectAttendQuery($model, $month, $sem)
    {
        if (null == $model->field["SCHREGNO"] || "" == $model->field["SCHREGNO"]) {
            return knjc034aQuery::selectAttendQueryDummy($month);
        }
        $query  = " SELECT ";
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
        $query .= "     SICK, ";
        $query .= "     NOTICE, ";
        $query .= "     NONOTICE, ";
        $query .= "     LATE, ";
        $query .= "     EARLY, ";
        $query .= "     CONTROL_CODE ";
        $query .= " FROM ";
        $query .= "     (SELECT DISTINCT ";
        $query .= "         NAMECD2, ";
        $query .= "         NAME1, ";
        $query .= "         NAMESPARE1 ";
        $query .= "      FROM ";
        $query .= "         NAME_MST ";
        $query .= "      WHERE ";
        $query .= "         NAMECD1 = 'Z005' ";
        $query .= "         AND NAMECD2 = '".sprintf('%02d',$month)."' ";
        $query .= "      ORDER BY ";
        $query .= "         NAMESPARE1 ";
        $query .= "     ) AS NM ";
        $query .= " LEFT OUTER JOIN ";
        $query .= "     (SELECT ";
        $query .= "         * ";
        $query .= "      FROM ";
        $query .= "         ATTEND_SUBCLASS_DAT ";
        $query .= "      WHERE ";
        $query .= "         COPYCD = '0' ";
        $query .= "         AND YEAR = '".CTRL_YEAR."' ";
        $query .= "         AND MONTH = '".sprintf('%02d',$month)."' ";
        $query .= "         AND SEMESTER = '".$sem."' ";
        $query .= "         AND SCHREGNO = '".$model->field["SCHREGNO"]."' ";
        if ($model->Properties["useCurriculumcd"] == "1") {
            $query .= "         AND CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD = '".$model->field["SUBCLASSCD"]."' ";
        } else {
            $query .= "         AND CLASSCD = '".substr($model->field["SUBCLASSCD"], 0, 2)."' ";
            $query .= "         AND SUBCLASSCD = '".$model->field["SUBCLASSCD"]."' ";
        }
        $query .= "     ) AS AD ";
        $query .= " ON AD.MONTH = NM.NAMECD2 ";
        $query .= " LEFT OUTER JOIN ";
        $query .= "     (SELECT ";
        $query .= "         CONTROL_CODE ";
        $query .= "      FROM ";
        $query .= "         ADMIN_CONTROL_DAT ";
        $query .= "      WHERE ";
        $query .= "         YEAR = '".CTRL_YEAR."' AND ";
        $query .= "         CONTROL_FLG = '2' ";
        $query .= "      ) AS AC ";
        $query .= " ON NM.NAMECD2 = AC.CONTROL_CODE LEFT OUTER JOIN ( ";
        $query .= "         SELECT ";
        $query .= "             MONTH, ";
        $query .= "             APPOINTED_DAY ";
        $query .= "         FROM ";
        $query .= "             V_APPOINTED_DAY_GRADE_MST ";
        $query .= "         WHERE ";
        $query .= "             YEAR = '".CTRL_YEAR."' ";
        $query .= "             AND SEMESTER = '".$sem."' ";
        $query .= "             AND GRADE = '".$model->schGrade."' ";
        $query .= " ) AS AM ON NM.NAMECD2 = AM.MONTH ";
        $query .= " ORDER BY ";
        $query .="      NAMESPARE1 ";

        return $query;
    }

    /* 出欠月別累積データDUMMY */
    function selectAttendQueryDummy($month)
    {
        $query  = " SELECT ";
        $query .= "     '' AS YEAR, ";
        $query .= "     NAMECD2 AS MONTH, ";
        $query .= "     NAME1 AS MONTH_NAME, ";
        $query .= "     NAMESPARE1, ";
        $query .= "     '' AS SEMESTER, ";
        $query .= "     '' AS APPOINTED_DAY, ";
        $query .= "     '' AS LESSON, ";
        $query .= "     '' AS OFFDAYS, ";
        $query .= "     '' AS ABROAD, ";
        $query .= "     '' AS ABSENT, ";
        $query .= "     '' AS SUSPEND, ";
        $query .= "     '' AS VIRUS, ";
        $query .= "     '' AS MOURNING, ";
        $query .= "     '' AS SICK, ";
        $query .= "     '' AS NOTICE, ";
        $query .= "     '' AS NONOTICE, ";
        $query .= "     '' AS LATE, ";
        $query .= "     '' AS EARLY, ";
        $query .= "     '' AS CONTROL_CODE ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = 'Z005' ";
        $query .= "     AND NAMECD2 = '".sprintf('%02d',$month)."' ";
        return $query;
    }

    //メインデータ作成
    function selectSemesAll($model, $seme = "")
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
        $query .= "     V_SEMESTER_GRADE_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        if (!$seme) {
            $query .= "     AND SEMESTER <> '9' ";
        } else {
            $query .= "     AND SEMESTER = '".$seme."' ";
        }
        $query .= "     AND GRADE = '".$model->schGrade."' ";
        $query .= " ORDER BY ";
        $query .= "     SEMESTER ";

        return $query;
    }

    /* 出欠累積データ学期計・累積計 */
    function selectAttendSemester($model, $semester)
    {
        if ($semester == 0 && !$model->field["SCHREGNO"]) {
            $query  = "VALUES ('','','','','','','','','','','','','')";
        } else if ($semester > 0 && !$model->field["SCHREGNO"]) {
            $query  = "SELECT ";
            $query .= "     SEMESTERNAME || '計' AS MONTH_NAME ";
            $query .= "FROM ";
            $query .= "     V_SEMESTER_GRADE_MST ";
            $query .= "WHERE ";
            $query .= "     YEAR = '".CTRL_YEAR."' ";
            $query .= "     AND SEMESTER = '".$semester."' ";
            $query .= "     AND GRADE = '{$model->schGrade}' ";
        } else {
            $query .= " SELECT ";
            if ($semester > 0) {
                $query .= "     L1.SEMESTERNAME || '計' AS MONTH_NAME, ";
                $query .= "     M1.SCHREGNO, ";
            }
            $query .= "     '' AS APPOINTED_DAY, ";
            $query .= "     sum(T1.LESSON) AS LESSON, ";
            $query .= "     sum(T1.OFFDAYS) AS OFFDAYS, ";
            $query .= "     sum(T1.ABROAD) AS ABROAD, ";
            $query .= "     sum(T1.ABSENT) AS ABSENT, ";
            $query .= "     sum(T1.SUSPEND) AS SUSPEND, ";
            $query .= "     sum(T1.VIRUS) AS VIRUS, ";
            $query .= "     sum(T1.MOURNING) AS MOURNING, ";
            $query .= "     sum(T1.SICK) AS SICK, ";
            $query .= "     sum(T1.NOTICE) AS NOTICE, ";
            $query .= "     sum(T1.NONOTICE) AS NONOTICE, ";
            $query .= "     sum(T1.LATE) AS LATE, ";
            $query .= "     sum(T1.EARLY) AS EARLY ";
            $query .= " FROM ";
            $query .= "     SCHREG_BASE_MST M1 ";
            $query .= "     LEFT JOIN ATTEND_SUBCLASS_DAT T1 ON T1.COPYCD = '0' ";
            $query .= "     AND T1.YEAR = '".CTRL_YEAR."' ";
            if ($semester > 0) {
                $query .= "     AND T1.SEMESTER = '".$semester."' ";
            }
            $query .= "     AND T1.SCHREGNO = M1.SCHREGNO";
            if ($model->Properties["useCurriculumcd"] == "1") {
                $query .= "     AND CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD = '".$model->field["SUBCLASSCD"]."' ";
            } else {
                $query .= "     AND CLASSCD = '".substr($model->field["SUBCLASSCD"], 0, 2)."' ";
                $query .= "     AND SUBCLASSCD = '".$model->field["SUBCLASSCD"]."' ";
            }
            $query .= "     LEFT JOIN V_SEMESTER_GRADE_MST L1 ON L1.YEAR = '".CTRL_YEAR."' ";
            if ($semester > 0) {
                $query .= "     AND L1.SEMESTER = '".$semester."' ";
            } else {
                $query .= "     AND L1.SEMESTER = T1.SEMESTER ";
            }
            $query .= "          AND L1.GRADE = '{$model->schGrade}' ";
            $query .= " WHERE ";
            $query .= "     M1.SCHREGNO = '".$model->field["SCHREGNO"]."' ";
            if ($semester > 0) {
                $query .= " GROUP BY ";
                $query .= "     M1.SCHREGNO ";
                $query .= "     ,L1.SEMESTERNAME ";
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
            $query .= "     ATTEND_SUBCLASS_DAT ";
            $query .= " WHERE ";
            $query .= "     COPYCD = '0' ";
            $query .= "     AND YEAR = '".CTRL_YEAR."' ";
            $query .= "     AND SEMESTER = '".$monthAr[1]."' ";
            $query .= "     AND MONTH = '".$monthAr[0]."' ";
            $query .= "     AND SCHREGNO = '".$model->field["SCHREGNO"]."' ";
            if ($model->Properties["useCurriculumcd"] == "1") {
                $query .= "     AND CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD = '".$model->field["SUBCLASSCD"]."' ";
            } else {
                $query .= "     AND CLASSCD = '".substr($model->field["SUBCLASSCD"], 0, 2)."' ";
                $query .= "     AND SUBCLASSCD = '".$model->field["SUBCLASSCD"]."' ";
            }

            $flag = $db->getOne($query);
            $data = array();

            if ($flag) {
                /* 更新対象データがある時 - UPDATE */
                $where  = " WHERE ";
                $where .= "     COPYCD = '0' ";
                $where .= "     AND YEAR = '".CTRL_YEAR."' ";
                $where .= "     AND SEMESTER = '".$monthAr[1]."' ";
                $where .= "     AND MONTH = '".$monthAr[0]."' ";
                $where .= "     AND SCHREGNO = '".$model->field["SCHREGNO"]."' ";
                if ($model->Properties["useCurriculumcd"] == "1") {
                    $where .= "     AND CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD = '".$model->field["SUBCLASSCD"]."' ";
                } else {
                    $where .= "     AND CLASSCD = '".substr($model->field["SUBCLASSCD"], 0, 2)."' ";
                    $where .= "     AND SUBCLASSCD = '".$model->field["SUBCLASSCD"]."' ";
                }
            } else {
                /* 更新対象データがない時 - INSERT */
                $data["COPYCD"][TEXT]     = "0";
                $data["YEAR"][TEXT]       = CTRL_YEAR;
                $data["MONTH"][TEXT]      = $monthAr[0];
                $data["SEMESTER"][TEXT]   = $monthAr[1];
                $data["SCHREGNO"][TEXT]   = $model->field["SCHREGNO"];
                if ($model->Properties["useCurriculumcd"] == "1") {
                    list($classCd, $schoolKind, $curriculumCd, $subclassCD) = preg_split("/-/", $model->field["SUBCLASSCD"]);
                    $data["CLASSCD"][TEXT]       = $classCd;
                    $data["SCHOOL_KIND"][TEXT]   = $schoolKind;
                    $data["CURRICULUM_CD"][TEXT] = $curriculumCd;
                    $data["SUBCLASSCD"][TEXT]    = $subclassCD;
                } else {
                    $data["CLASSCD"][TEXT]    = substr($model->field["SUBCLASSCD"], 0, 2);
                    $data["SUBCLASSCD"][TEXT] = $model->field["SUBCLASSCD"];
                }
                $data["NURSEOFF"][NUMBER] = 0;      //保健室欠課回数
            }
            /* 更新データをセット */
            $data["APPOINTED_DAY"][TEXT] = $model->field["APPOINTED_DAY"][$key]; //締め日
            $data["LESSON"][NUMBER]      = ($model->field["LESSON"][$key]) ?   $model->field["LESSON"][$key] : 0;   //授業日数
            $data["OFFDAYS"][NUMBER]     = ($model->field["OFFDAYS"][$key]) ?  $model->field["OFFDAYS"][$key] : 0;  //休学日数
            $data["ABROAD"][NUMBER]      = ($model->field["ABROAD"][$key]) ?   $model->field["ABROAD"][$key] : 0;   //留学日数
            $data["ABSENT"][NUMBER]      = ($model->field["ABSENT"][$key]) ?   $model->field["ABSENT"][$key] : 0;   //公欠日数
            $data["SUSPEND"][NUMBER]     = ($model->field["SUSPEND"][$key]) ?  $model->field["SUSPEND"][$key] : 0;  //出停日数
            $data["VIRUS"][NUMBER]       = ($model->field["VIRUS"][$key]) ?    $model->field["VIRUS"][$key] : 0;    //伝染病日数
            $data["MOURNING"][NUMBER]    = ($model->field["MOURNING"][$key]) ? $model->field["MOURNING"][$key] : 0; //忌引日数
            $data["SICK"][NUMBER]        = ($model->field["SICK"][$key]) ?     $model->field["SICK"][$key] : 0;     //病欠
            $data["NOTICE"][NUMBER]      = ($model->field["NOTICE"][$key]) ?   $model->field["NOTICE"][$key] : 0;   //事故欠届
            $data["NONOTICE"][NUMBER]    = ($model->field["NONOTICE"][$key]) ? $model->field["NONOTICE"][$key] : 0; //事故欠無
            $data["LATE"][NUMBER]        = ($model->field["LATE"][$key]) ?     $model->field["LATE"][$key] : 0;     //遅刻回数
            $data["EARLY"][NUMBER]       = ($model->field["EARLY"][$key]) ?    $model->field["EARLY"][$key] : 0;    //早退回数
            $data["REGISTERCD"][TEXT]    = STAFFCD ;    //登録者コード
            $data["UPDATED"][FUNC]       = "sysdate()"; //更新日付

            //ATTEND_SEMES_DATチェック
            $query  = " SELECT ";
            $query .= "     COUNT(*) ";
            $query .= " FROM ";
            $query .= "     ATTEND_SEMES_DAT ";
            $query .= " WHERE ";
            $query .= "     COPYCD = '0' ";
            $query .= "     AND YEAR = '".CTRL_YEAR."' ";
            $query .= "     AND MONTH = '".$monthAr[0]."' ";
            $query .= "     AND SEMESTER = '".$monthAr[1]."' ";
            $query .= "     AND SCHREGNO = '".$model->field["SCHREGNO"]."' ";

            $flg2 = $db->getOne($query);
            $data2 = array();

            //ATTEND_SEMES_DATセット
            $data2["COPYCD"][TEXT]          = "0";
            $data2["YEAR"][TEXT]            = CTRL_YEAR;
            $data2["MONTH"][TEXT]           = $monthAr[0];
            $data2["SEMESTER"][TEXT]        = $monthAr[1];
            $data2["SCHREGNO"][TEXT]        = $model->field["SCHREGNO"];
            $data2["APPOINTED_DAY"][TEXT]   = $model->field["APPOINTED_DAY"][$key];
            $data2["LESSON"][NUMBER]        = 0;
            $data2["OFFDAYS"][NUMBER]       = 0;
            $data2["ABSENT"][NUMBER]        = 0;
            $data2["SUSPEND"][NUMBER]       = 0;
            $data2["MOURNING"][NUMBER]      = 0;
            $data2["ABROAD"][NUMBER]        = 0;
            $data2["SICK"][NUMBER]          = 0;
            $data2["NOTICE"][NUMBER]        = 0;
            $data2["NONOTICE"][NUMBER]      = 0;
            $data2["LATE"][NUMBER]          = 0;
            $data2["EARLY"][NUMBER]         = 0;
            $data2["KEKKA_JISU"][NUMBER]    = 0;
            $data2["KEKKA"][NUMBER]         = 0;
            $data2["LATEDETAIL"][NUMBER]    = 0;
            $data2["VIRUS"][NUMBER]         = 0;
            $data2["REGISTERCD"][TEXT]      = STAFFCD;
            $data2["UPDATED"][FUNC]         = "SYSDATE()";

            if ("" != $model->field["APPOINTED_DAY"][$key]) {
                /* 更新対象データがある時 - UPDATE */
                if($flag){
                    $query = Query::updateSQL($data, "ATTEND_SUBCLASS_DAT", $where);
                }
                /* 更新対象データがない時 - INSERT */
                else{
                    $query = Query::insertSQL($data, "ATTEND_SUBCLASS_DAT");
                }

                //ATTEND_SEMES_DAT追加
                if(!$flg2) {
                    $query2 = Query::insertSQL($data2, ATTEND_SEMES_DAT);
                    $db->query($query2);
                }
            } else {
                if ($flag) {
                    $query = "DELETE FROM ATTEND_SUBCLASS_DAT ".$where;
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
