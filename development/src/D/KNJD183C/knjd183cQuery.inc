<?php

require_once('for_php7.php');

class knjd183cquery extends Query
{

    //マスタ取得
    public function getItemMst($subclasscd)
    {
        list($classcd, $schoolKind, $curriculumcd, $subclasscd_) = explode("-", $subclasscd);
        $query .= " SELECT  ";
        $query .= "     CLASSCD, ";
        $query .= "     SCHOOL_KIND, ";
        $query .= "     COLUMNNAME, ";
        $query .= "     ITEMNAME, ";
        $query .= "     SHOWORDER, ";
        $query .= "     SHOW_FLG, ";
        $query .= "     DATA_SIZE, ";
        $query .= "     PATTERN_DATA_DIV, ";
        $query .= "     PATTERN_SHOW_FLG ";
        $query .= " FROM  ";
        $query .= "     RECORD_TOTALSTUDYTIME_ITEM_MST  ";
        $query .= " WHERE ";
        $query .= "     CLASSCD = '{$classcd}' ";
        $query .= "     AND SCHOOL_KIND = '{$schoolKind}' ";
        $query .= "     AND SHOW_FLG = '1' ";
        $query .= " ORDER BY  ";
        $query .= "     VALUE(SHOWORDER, 999), ";
        $query .= "     ITEMNAME ";

        return $query;
    }

    //学期取得
    public function getSemesterCmb($target = "")
    {
        $query  = " SELECT ";
        if ($target) {
            $query .= "     SEMESTERNAME ";
        } else {
            $query .= "     SEMESTER AS VALUE, ";
            $query .= "     SEMESTERNAME AS LABEL ";
        }
        $query .= " FROM ";
        $query .= "     SEMESTER_MST ";
        $query .= " WHERE ";
        $query .= "         YEAR     = '".CTRL_YEAR."' ";
        if ($target) {
            $query .= "     AND SEMESTER = '{$target}' ";
        } else {
            $query .= "     AND SEMESTER <> '9' ";
        }

        return $query;
    }

    //科目データの取得
    public function selectSubclassQuery($model)
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     T3.CLASSCD || '-' || T3.SCHOOL_KIND || '-' || T3.CURRICULUM_CD || '-' || T3.SUBCLASSCD AS VALUE, ";
        $query .= "     T3.CLASSCD || '-' || T3.SCHOOL_KIND || '-' || T3.CURRICULUM_CD || '-' || T3.SUBCLASSCD || ' ' || VALUE(T3.SUBCLASSNAME,'') AS LABEL ";
        $query .= " FROM ";
        $query .= "     CHAIR_DAT T1, ";
        $query .= "     CHAIR_STD_DAT T2, ";
        $query .= "     SUBCLASS_MST T3, ";
        $query .= "     CHAIR_STF_DAT T4 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR         = T2.YEAR AND ";
        $query .= "     T1.YEAR         = T4.YEAR AND ";
        $query .= "     T1.YEAR         = '".CTRL_YEAR."' AND ";
        $query .= "     T1.SEMESTER     = T2.SEMESTER AND ";
        $query .= "     T1.SEMESTER     = T4.SEMESTER AND ";
        if ($model->Properties["KNJD183C_semesCombo"] == "1") {
            $query .= "     T1.SEMESTER     = '{$model->semester}' AND ";
        } else {
            $query .= "     T1.SEMESTER     = '".CTRL_SEMESTER."' AND ";
        }
        $query .= "     T1.CHAIRCD      = T2.CHAIRCD AND ";
        $query .= "     T1.CHAIRCD      = T4.CHAIRCD AND ";
        $query .= "    (SUBSTR(T3.SUBCLASSCD,1,2) IN (SELECT N1.NAMECD2 FROM NAME_MST N1 WHERE N1.NAMECD1 = 'D008') OR ";
        $query .= "     T3.SUBCLASSCD LIKE '90%') AND ";
        //権限(制限付)
        if (AUTHORITY == DEF_REFER_RESTRICT || AUTHORITY == DEF_UPDATE_RESTRICT) {
            $query .= "     T4.STAFFCD  = '".STAFFCD."' AND ";
        }
        $query .= "     T1.CLASSCD      = T3.CLASSCD AND ";
        $query .= "     T1.SCHOOL_KIND  = T3.SCHOOL_KIND AND ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= "     T1.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind), "','")."') AND ";
            }
        } elseif ($model->Properties["useSchool_KindField"] == "1") {
            $query .= "     T1.SCHOOL_KIND = '" .SCHOOLKIND ."' AND ";
        }
        $query .= "     T1.CURRICULUM_CD = T3.CURRICULUM_CD AND ";
        $query .= "     T1.SUBCLASSCD   = T3.SUBCLASSCD ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //講座データの取得
    public function selectChairQuery($model)
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     T1.CHAIRCD AS VALUE, ";
        $query .= "     T1.CHAIRCD || ' ' || VALUE(T1.CHAIRNAME,'') AS LABEL ";
        $query .= " FROM ";
        $query .= "     CHAIR_DAT T1, ";
        $query .= "     CHAIR_STD_DAT T2, ";
        $query .= "     CHAIR_STF_DAT T3 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR       = T2.YEAR AND ";
        $query .= "     T1.YEAR       = T3.YEAR AND ";
        $query .= "     T1.YEAR       = '".CTRL_YEAR."' AND ";
        $query .= "     T1.SEMESTER   = T2.SEMESTER AND ";
        $query .= "     T1.SEMESTER   = T3.SEMESTER AND ";
        if ($model->Properties["KNJD183C_semesCombo"] == "1") {
            $query .= "     T1.SEMESTER     = '{$model->semester}' AND ";
        } else {
            $query .= "     T1.SEMESTER   = '".CTRL_SEMESTER."' AND ";
        }
        $query .= "     T1.CHAIRCD    = T2.CHAIRCD AND ";
        $query .= "     T1.CHAIRCD    = T3.CHAIRCD AND ";
        //権限(制限付)
        if (AUTHORITY == DEF_REFER_RESTRICT || AUTHORITY == DEF_UPDATE_RESTRICT) {
            $query .= "     T3.STAFFCD      = '".STAFFCD."' AND ";
        }
        $query .= "     T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD = '".$model->field["SUBCLASSCD"]."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //学期取得
    public function getSemester($model)
    {
        $query  = " SELECT ";
        $query .= "     SDATE, ";
        $query .= "     EDATE ";
        $query .= " FROM ";
        $query .= "     SEMESTER_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR     = '".CTRL_YEAR."' AND ";
        if ($model->Properties["KNJD183C_semesCombo"] == "1") {
            $query .= "     SEMESTER = '{$model->semester}' ";
        } else {
            $query .= "     SEMESTER = '".CTRL_SEMESTER."' ";
        }

        return $query;
    }

    //生徒名称取得
    public function getNameShow($schregno)
    {
        $query  = "    SELECT NAME_SHOW ";
        $query .= "      FROM SCHREG_BASE_MST ";
        $query .= "     WHERE SCHREGNO = '".$schregno."'";

        return $query;
    }

    //全体のデータの取得
    public function selectQuery($model, $execute_date)
    {
        //総合的な学習の時間の表
        $query  = " WITH T_REMARK AS ( ";
        $query .= "     SELECT ";
        $query .= "         * ";
        $query .= "     FROM ";
        $query .= "         RECORD_TOTALSTUDYTIME_DAT ";
        $query .= "     WHERE ";
        $query .= "         YEAR        = '".CTRL_YEAR."' AND ";
        if ($model->Properties["KNJD183C_semesCombo"] == "1") {
            $query .= "         SEMESTER    = '{$model->semester}' AND ";
        } else {
            $query .= "         SEMESTER    = '9' AND ";
        }
        $query .= "         CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD = '".$model->field["SUBCLASSCD"]."' ";
        $query .= " ) ";

        $query .= " SELECT ";
        $query .= "     W1.SCHREGNO, ";
        $query .= "     R1.CLASSCD, ";
        $query .= "     R1.SCHOOL_KIND, ";
        $query .= "     R1.CURRICULUM_CD, ";
        $query .= "     R1.SUBCLASSCD, ";
        $query .= "     W2.GRADE, ";
        $query .= "     W2.HR_CLASS, ";
        $query .= "     W2.HR_NAME, ";
        $query .= "     W2.ATTENDNO, ";
        $query .= "     W2.NAME_SHOW, ";
        $query .= "     CASE WHEN R1.SCHREGNO IS NULL THEN 0 ELSE 1 END AS FLG ";
        foreach ($model->itemMst as $m) {
            $query .= "     ,R1.{$m["COLUMNNAME"]} ";
        }
        $query .= " FROM ";
        $query .= "    (SELECT ";
        $query .= "         SCHREGNO  ";
        $query .= "     FROM ";
        $query .= "         CHAIR_STD_DAT  ";
        $query .= "     WHERE ";
        $query .= "         YEAR    = '".CTRL_YEAR."' AND ";
        $query .= "         CHAIRCD = '".$model->field["CHAIRCD"]."' AND ";
        $query .= "         '".$execute_date."' BETWEEN APPDATE AND APPENDDATE ";
        $query .= "    ) W1 ";
        $query .= "     INNER JOIN (SELECT ";
        $query .= "                     T1.GRADE, ";
        $query .= "                     T0.HR_NAME, ";
        $query .= "                     T1.HR_CLASS, ";
        $query .= "                     T1.ATTENDNO, ";
        $query .= "                     T2.NAME_SHOW, ";
        $query .= "                     T2.SCHREGNO ";
        $query .= "                 FROM ";
        $query .= "                     SCHREG_REGD_HDAT T0, ";
        $query .= "                     SCHREG_REGD_DAT T1, ";
        $query .= "                     SCHREG_BASE_MST T2 ";
        $query .= "                 WHERE ";
        $query .= "                     T0.YEAR     = T1.YEAR AND ";
        $query .= "                     T0.SEMESTER = T1.SEMESTER AND ";
        $query .= "                     T0.GRADE    = T1.GRADE AND ";
        $query .= "                     T0.HR_CLASS = T1.HR_CLASS AND ";
        $query .= "                     T1.YEAR     = '".CTRL_YEAR."' AND ";
        if ($model->Properties["KNJD183C_semesCombo"] == "1") {
            $query .= "                     T1.SEMESTER = '{$model->semester}' AND ";
        } else {
            $query .= "                     T1.SEMESTER = '".CTRL_SEMESTER."' AND ";
        }
        $query .= "                     T1.SCHREGNO = T2.SCHREGNO ) W2 ON W1.SCHREGNO = W2.SCHREGNO ";
        $query .= "     LEFT JOIN T_REMARK R1 ON R1.SCHREGNO = W1.SCHREGNO ";
        $query .= " ORDER BY ";
        $query .= "     W2.GRADE, ";
        $query .= "     W2.HR_CLASS, ";
        $query .= "     W2.ATTENDNO ";

        return $query;
    }

    /* データ更新処理 */
    public function update($model)
    {
        $db = Query::dbCheckOut();

        for ($i = 0; $i < get_count($model->data["SCHREGNO"]); $i++) {
            list($classcd, $school_kind, $curriculum_cd, $subclasscd) = preg_split("/-/", $model->field["SUBCLASSCD"]);

            /*** RECORD_TOTALSTUDYTIME_DAT ***/
            $year = CTRL_YEAR;
            if ($model->Properties["KNJD183C_semesCombo"] == "1") {
                $semcd = $model->semester;
            } else {
                $semcd = 9;
            }
            $schno = $model->data["SCHREGNO"][$i];
            //項目
            $data = array();
            $data["YEAR"][TEXT]             = $year;
            $data["SEMESTER"][TEXT]         = $semcd;
            $data["CLASSCD"][TEXT]          = $classcd;
            $data["SCHOOL_KIND"][TEXT]      = $school_kind;
            $data["CURRICULUM_CD"][TEXT]    = $curriculum_cd;
            $data["SUBCLASSCD"][TEXT]       = $subclasscd;
            $data["SCHREGNO"][TEXT]         = $schno;
            foreach ($model->itemMst as $m) {
                $data[$m["COLUMNNAME"]][TEXT]    = $model->fields[$m["COLUMNNAME"]][$i];
            }
            $data["REGISTERCD"][TEXT]       = STAFFCD ;
            $data["UPDATED"][FUNC]          = "sysdate()";
            //存在するか？
            $query  = "SELECT COUNT(*) FROM RECORD_TOTALSTUDYTIME_DAT";
            $query .= " WHERE YEAR          = '{$year}'";
            $query .= "   AND SEMESTER      = '{$semcd}'";
            $query .= "   AND CLASSCD       = '{$classcd}' ";
            $query .= "   AND SCHOOL_KIND   = '{$school_kind}' ";
            $query .= "   AND CURRICULUM_CD = '{$curriculum_cd}' ";
            $query .= "   AND SUBCLASSCD    = '{$subclasscd}' ";
            $query .= "   AND SCHREGNO      = '{$schno}'";
            if (0 < $db->getOne($query)) {
                //更新
                $where  = " WHERE YEAR          = '{$year}'";
                $where .= "   AND SEMESTER      = '{$semcd}'";
                $where .= "   AND CLASSCD       = '{$classcd}' ";
                $where .= "   AND SCHOOL_KIND   = '{$school_kind}' ";
                $where .= "   AND CURRICULUM_CD = '{$curriculum_cd}' ";
                $where .= "   AND SUBCLASSCD    = '{$subclasscd}' ";
                $where .= "   AND SCHREGNO      = '{$schno}'";
                $query = Query::updateSQL($data, "RECORD_TOTALSTUDYTIME_DAT", $where);
            } else {
                //追加
                $query = Query::insertSQL($data, "RECORD_TOTALSTUDYTIME_DAT");
            }
            $db->query($query);
        }

        Query::dbCheckIn($db);
    }

    /*===================================一括更新画面===================================*/

    //講座名取得
    public function getChairName($model)
    {
        $query  = " SELECT ";
        $query .= "     T2.SUBCLASSNAME || '　　' || T1.CHAIRNAME AS CHAIRNAME ";
        $query .= " FROM ";
        $query .= "     CHAIR_DAT T1 ";
        $query .= "     LEFT JOIN SUBCLASS_MST T2 ON T1.CLASSCD         = T2.CLASSCD ";
        $query .= "                              AND T1.SCHOOL_KIND     = T2.SCHOOL_KIND ";
        $query .= "                              AND T1.CURRICULUM_CD   = T2.CURRICULUM_CD ";
        $query .= "                              AND T1.SUBCLASSCD      = T2.SUBCLASSCD ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR     = '".CTRL_YEAR."' AND ";
        if ($model->Properties["KNJD183C_semesCombo"] == "1") {
            $query .= "     T1.SEMESTER = '{$model->semester}' AND ";
        } else {
            $query .= "     T1.SEMESTER = '".CTRL_SEMESTER."' AND ";
        }
        $query .= "     T1.CHAIRCD  = '".$model->chaircd."' ";

        return $query;
    }

    //置換画面用の生徒一覧取得
    public function getStudent($model, $execute_date, $flg = "")
    {
        $selectdata = str_replace(",", "','", $model->replace_data["selectdata"]);

        $query  = " WITH SCHINFO AS ( ";
        $query .= "     SELECT ";
        $query .= "         T1.SCHREGNO, ";
        $query .= "         T1.GRADE || T1.HR_CLASS || T1.ATTENDNO || '-' || T1.SCHREGNO AS VALUE, ";
        $query .= "         T3.HR_NAME || ' ' || T1.ATTENDNO || '番' AS ATTENDNO, ";
        $query .= "         T2.NAME_SHOW ";
        $query .= "     FROM ";
        $query .= "         SCHREG_REGD_DAT T1 ";
        $query .= "         LEFT OUTER JOIN SCHREG_BASE_MST T2 ON T1.SCHREGNO = T2.SCHREGNO ";
        $query .= "         LEFT OUTER JOIN SCHREG_REGD_HDAT T3 ON T1.YEAR = T3.YEAR AND ";
        $query .= "                 T1.SEMESTER = T3.SEMESTER AND T1.GRADE = T3.GRADE AND T1.HR_CLASS = T3.HR_CLASS ";
        $query .= "     WHERE ";
        $query .= "         T1.YEAR     = '".CTRL_YEAR."' AND ";
        if ($model->Properties["KNJD183C_semesCombo"] == "1") {
            $query .= "         T1.SEMESTER = '{$model->semester}' ";
        } else {
            $query .= "         T1.SEMESTER = '".CTRL_SEMESTER."' ";
        }
        $query .= " ), CHR_STD AS ( ";
        $query .= "     SELECT ";
        $query .= "         SCHREGNO ";
        $query .= "     FROM ";
        $query .= "         CHAIR_STD_DAT ";
        $query .= "     WHERE ";
        $query .= "         YEAR        = '".CTRL_YEAR."' AND ";
        if ($model->Properties["KNJD183C_semesCombo"] == "1") {
            $query .= "         SEMESTER    = '{$model->semester}' AND ";
        } else {
            $query .= "         SEMESTER    = '".CTRL_SEMESTER."' AND ";
        }
        $query .= "         CHAIRCD     = '".$model->chaircd."' AND ";
        $query .= "         '".$execute_date."' BETWEEN APPDATE AND APPENDDATE ";
        $query .= " ) ";

        $query .= " SELECT ";
        $query .= "     T2.VALUE, ";
        $query .= "     T2.ATTENDNO, ";
        $query .= "     T2.NAME_SHOW ";
        $query .= " FROM ";
        $query .= "     CHR_STD T1, ";
        $query .= "     SCHINFO T2 ";
        $query .= " WHERE ";
        $query .= "     T1.SCHREGNO = T2.SCHREGNO AND ";
        if ($flg == "1") {
            $query .= "     T1.SCHREGNO IN ('".$selectdata."') ";
        } else {
            $query .= "     T1.SCHREGNO NOT IN ('".$selectdata."') ";
        }
        $query .= " ORDER BY";
        $query .= "     T2.VALUE ";

        return $query;
    }

    //一括更新
    public function replaceUpdate($model)
    {
        $db = Query::dbCheckOut();
        $model->replace_data["selectdata"] = explode(",", $model->replace_data["selectdata"]);

        for ($i = 0; $i < get_count($model->replace_data["selectdata"]); $i++) {
            $year = CTRL_YEAR;
            if ($model->Properties["KNJD183C_semesCombo"] == "1") {
                $semester = $model->semester;
            } else {
                $semester = '9';
            }
            $schregno = $model->replace_data["selectdata"][$i];
            list($classcd, $school_kind, $curriculum_cd, $subclasscd) = preg_split("/-/", $model->subclasscd);

            if (in_array("1", array_values($model->replace_data["check"]))) {
                //項目
                $data = array();
                $data["YEAR"][TEXT]          = $year;
                $data["SEMESTER"][TEXT]      = $semester;
                $data["CLASSCD"][TEXT]       = $classcd;
                $data["SCHOOL_KIND"][TEXT]   = $school_kind;
                $data["CURRICULUM_CD"][TEXT] = $curriculum_cd;
                $data["SUBCLASSCD"][TEXT]    = $subclasscd;
                $data["SCHREGNO"][TEXT]      = $schregno;
                foreach ($model->itemMst as $m) {
                    if ($model->replace_data["check"][$m["COLUMNNAME"]] == "1") {
                        $data[$m["COLUMNNAME"]][TEXT] = $model->replace_data["field"][$m["COLUMNNAME"]];
                    }
                }
                $data["REGISTERCD"][TEXT]    = STAFFCD ;
                $data["UPDATED"][FUNC]       = "sysdate()";

                //存在チェック
                $query  = " SELECT COUNT(*) FROM RECORD_TOTALSTUDYTIME_DAT ";
                $query .= " WHERE YEAR          = '".$year."' AND ";
                $query .= "       SEMESTER      = '".$semester."' AND ";
                $query .= "       CLASSCD       = '".$classcd."' AND ";
                $query .= "       SCHOOL_KIND   = '".$school_kind."' AND ";
                $query .= "       CURRICULUM_CD = '".$curriculum_cd."' AND ";
                $query .= "       SUBCLASSCD    = '".$subclasscd."' AND ";
                $query .= "       SCHREGNO      = '".$schregno."' ";
                if (0 < $db->getOne($query)) {
                    //更新
                    $where  = " WHERE YEAR          = '".$year."' AND ";
                    $where .= "       SEMESTER      = '".$semester."' AND ";
                    $where .= "       CLASSCD       = '".$classcd."' AND ";
                    $where .= "       SCHOOL_KIND   = '".$school_kind."' AND ";
                    $where .= "       CURRICULUM_CD = '".$curriculum_cd."' AND ";
                    $where .= "       SUBCLASSCD    = '".$subclasscd."' AND ";
                    $where .= "       SCHREGNO      = '".$schregno."'";
                    $query = Query::updateSQL($data, "RECORD_TOTALSTUDYTIME_DAT", $where);
                } else {
                    //追加
                    $query = Query::insertSQL($data, "RECORD_TOTALSTUDYTIME_DAT");
                }
                $db->query($query);
            }
        }
        Query::dbCheckIn($db);
    }


    /*===================================定型文選択===================================*/

    //学年取得
    public function getGrade($model)
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     T2.GRADE AS VALUE, ";
        $query .= "     T2.GRADE_NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1, ";
        $query .= "     SCHREG_REGD_GDAT T2, ";
        $query .= "     CHAIR_STD_DAT T3 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR     = T2.YEAR AND ";
        $query .= "     T1.YEAR     = T3.YEAR AND ";
        $query .= "     T1.YEAR     = '".CTRL_YEAR."' AND ";
        $query .= "     T1.SEMESTER = T3.SEMESTER AND ";
        if ($model->Properties["KNJD183C_semesCombo"] == "1") {
            $query .= "     T1.SEMESTER = '{$model->semester}' AND ";
        } else {
            $query .= "     T1.SEMESTER = '".CTRL_SEMESTER."' AND ";
        }
        $query .= "     T1.GRADE    = T2.GRADE AND ";
        $query .= "     T1.SCHREGNO = T3.SCHREGNO AND ";
        $query .= "     T3.CHAIRCD  = '".$model->chr."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //定型文マスタ一覧取得
    public function getHtrainRemarkTempDat($model)
    {
        if ($model->Properties["KNJD183C_semesCombo"] == "1") {
            $query  = " SELECT ";
            $query .= "     * ";
            $query .= " FROM ";
            $query .= "     HTRAINREMARK_TEMP_SEMES_DAT ";
            $query .= " WHERE ";
            $query .= "         YEAR     = '".CTRL_YEAR."' ";
            $query .= "     AND SEMESTER = '".$model->semester."' ";
            $query .= "     AND GRADE    = '".$model->subField["GRADE"]."' ";
            $query .= "     AND DATA_DIV = '".$model->patternDataDiv."' ";
            $query .= " ORDER BY ";
            $query .= "     PATTERN_CD ";
        } else {
            $query  = " SELECT ";
            $query .= "     * ";
            $query .= " FROM ";
            $query .= "     HTRAINREMARK_TEMP_DAT ";
            $query .= " WHERE ";
            $query .= "     YEAR        = '".CTRL_YEAR."' AND ";
            $query .= "     GRADE       = '".$model->subField["GRADE"]."' AND ";
            $query .= "     DATA_DIV    = '".$model->patternDataDiv."' ";
            $query .= " ORDER BY ";
            $query .= "     PATTERN_CD ";
        }
        return $query;
    }

    public function getHandCap($SCHREGNO)
    {
        $query .= "     SELECT ";
        $query .= "         HANDICAP ";
        $query .= "     FROM";
        $query .= "         SCHREG_BASE_MST";
        $query .= "     WHERE";
        $query .= "         SCHREGNO='".$SCHREGNO."'";

        return $query;
    }

    public function getNyuryokuPattern()
    {
        $query .= " SELECT REMARK1";
        $query .= "   FROM HREPORT_CONDITION_DAT";
        $query .= "  WHERE YEAR ='".CTRL_YEAR."'";
        $query .= "    AND SEQ  ='012'";

        return $query;
    }
}
