<?php

require_once('for_php7.php');
class knjx_c035eQuery extends Query
{
    public function getSecurityHigh($model)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     MENU_HIGH_SECURITY_MST ";
        $query .= " WHERE ";
        $query .= "     PROGRAMID = '{$model->getPrgId}' ";
        $query .= "     AND INVALID_FLG = '0' ";

        return $query;
    }

    public function getSchoolCd()
    {
        $query  = " SELECT ";
        $query .= "     NAME2 ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = 'Z010' ";
        $query .= "     AND NAMECD2 = '00' ";

        return $query;
    }

    //年度学期一覧
    public function getSelectFieldSQL($model)
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     T1.YEAR || T1.SEMESTER AS VALUE, ";
        $query .= "     T1.YEAR || '年度 ' || T2.SEMESTERNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     CHAIR_STF_DAT T1 ";
        $query .= "     INNER JOIN SEMESTER_MST T2 ON T1.YEAR = T2.YEAR ";
        $query .= "                               AND T1.SEMESTER = T2.SEMESTER ";
        //権限（制限付）
        if ($model->auth == DEF_REFER_RESTRICT || $model->auth == DEF_UPDATE_RESTRICT) {
            $query .= " WHERE ";
            $query .= "     T1.STAFFCD  = '".STAFFCD."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //科目取得
    public function getSubclasscd($model)
    {
        $query  = " SELECT ";
        if ($model->Properties["useCurriculumcd"] == "1") {
            $query .= "     T3.CLASSCD || '-' || T3.SCHOOL_KIND || '-' ||  T3.CURRICULUM_CD || '-' || T3.SUBCLASSCD AS VALUE, ";
            $query .= "     T3.CLASSCD || '-' || T3.SCHOOL_KIND || '-' ||  T3.CURRICULUM_CD || '-' || T3.SUBCLASSCD || VALUE(T3.SUBCLASSABBV, '') AS LABEL ";
        } else {
            $query .= "     T3.SUBCLASSCD AS VALUE, ";
            $query .= "     T3.SUBCLASSCD || VALUE(T3.SUBCLASSABBV, '') AS LABEL ";
        }
        $query .= " FROM ";
        $query .= "     CHAIR_DAT T1, ";
        $query .= "     CHAIR_STF_DAT T2, ";
        $query .= "     SUBCLASS_MST T3 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR || T1.SEMESTER = '".$model->field["YEAR"]."' AND ";
        $query .= "     T1.YEAR     = T2.YEAR AND ";
        $query .= "     T1.SEMESTER = T2.SEMESTER AND ";
        $query .= "     T1.CHAIRCD  = T2.CHAIRCD AND ";
        //権限（制限付）
        if ($model->auth == DEF_REFER_RESTRICT || $model->auth == DEF_UPDATE_RESTRICT) {
            $query .= "     T2.STAFFCD  = '".STAFFCD."' AND ";
        }
        if ($model->Properties["useCurriculumcd"] == "1") {
            if ($model->Properties["use_prg_schoolkind"] == "1" && SCHOOLKIND != "") {
                if ($model->selectSchoolKind) {
                    $query .= "     T1.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind), "','")."') AND ";
                }
            } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
                $query .= "     T1.SCHOOL_KIND      = '".SCHOOLKIND."' AND ";
            }
            $query .= "     T1.CLASSCD          = T3.CLASSCD AND ";
            $query .= "     T1.SCHOOL_KIND      = T3.SCHOOL_KIND AND ";
            $query .= "     T1.CURRICULUM_CD    = T3.CURRICULUM_CD AND ";
        }
        $query .= "     T1.SUBCLASSCD = T3.SUBCLASSCD ";
        $query .= " GROUP BY ";
        if ($model->Properties["useCurriculumcd"] == "1") {
            $query .= "     T3.CLASSCD, ";
            $query .= "     T3.SCHOOL_KIND, ";
            $query .= "     T3.CURRICULUM_CD, ";
        }
        $query .= "     T3.SUBCLASSCD, ";
        $query .= "     T3.SUBCLASSABBV ";
        $query .= " ORDER BY ";
        if ($model->Properties["useCurriculumcd"] == "1") {
            $query .= "     T3.CLASSCD, ";
            $query .= "     T3.SCHOOL_KIND, ";
            $query .= "     T3.CURRICULUM_CD, ";
        }
        $query .= "     T3.SUBCLASSCD ";

        return $query;
    }

    //講座取得
    public function getChaircd(&$model)
    {
        $query  = " SELECT DISTINCT";
        $query .= "     T1.CHAIRCD AS VALUE, ";
        $query .= "     T1.CHAIRCD || VALUE(T1.CHAIRNAME, '') AS LABEL ";
        $query .= " FROM ";
        $query .= "     CHAIR_DAT T1, ";
        $query .= "     CHAIR_STF_DAT T2 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR || T1.SEMESTER = '".$model->field["YEAR"]."' AND ";
        $query .= "     T1.YEAR     = T2.YEAR AND ";
        $query .= "     T1.SEMESTER = T2.SEMESTER AND ";
        $query .= "     T1.CHAIRCD  = T2.CHAIRCD AND ";
        //権限（制限付）
        if ($model->auth == DEF_REFER_RESTRICT || $model->auth == DEF_UPDATE_RESTRICT) {
            $query .= "     T2.STAFFCD  = '".STAFFCD."' AND ";
        }
        if ($model->Properties["useCurriculumcd"] == "1") {
            $query .= "     T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' ||  T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD = '".$model->field["SUBCLASSCD"]."' ";
        } else {
            $query .= "     T1.SUBCLASSCD = '".$model->field["SUBCLASSCD"]."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     T1.CHAIRCD ";

        return $query;
    }

    //メインデータ作成
    public function selectSemesAll($model)
    {
        $query  = " SELECT ";
        $query .= "     SEMESTER, ";
        $query .= "     SEMESTERNAME, ";
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
        $query .= "     YEAR || SEMESTER = '".$model->field["YEAR"]."' ";
        $query .= " ORDER BY ";
        $query .= "     SEMESTER ";

        return $query;
    }

    //対象月データの取得
    public function selectMonthQuery($year, $month, $model)
    {
        $setNameCd = "Z005";
        if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $setNameCd = "Z".SCHOOLKIND."05";
        }
        $query  = " SELECT DISTINCT ";
        $query .= "     NAMECD2, ";
        $query .= "     NAME1, ";
        $query .= "     NAMESPARE1 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR    = '".$year."' AND ";
        $query .= "     NAMECD1 = '{$setNameCd}' AND ";
        $query .= "     NAMECD2 IN (SELECT ";
        $query .= "                     CONTROL_CODE ";
        $query .= "                 FROM ";
        $query .= "                     ADMIN_CONTROL_DAT ";
        $query .= "                 WHERE ";
        $query .= "                     YEAR = '".$year."' AND ";
        if ($model->Properties["use_prg_schoolkind"] == "1" && SCHOOLKIND != "") {
            if ($model->selectSchoolKind) {
                $query .= "                     SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind), "','")."') AND ";
            }
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= "                     SCHOOL_KIND = '".SCHOOLKIND."' AND ";
        }
        $query .= "                     CONTROL_FLG = '2') AND ";
        $query .= "     NAMECD2 = '".sprintf('%02d', $month)."' ";
        $query .= " ORDER BY ";
        $query .= "     NAMESPARE1 ";

        return $query;
    }

    //A004
    public function getA004($model)
    {
        $query  = " SELECT ";
        $query .= "     NAMECD2, ";
        $query .= "     NAME1 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR    = '".substr($model->field["YEAR"], 0, 4)."' AND ";
        $query .= "     NAMECD1 = 'A004' ";
        $query .= " ORDER BY ";
        $query .= "     NAMECD2 ";

        return $query;
    }

    //C001
    public function getC001($model)
    {
        $query  = " SELECT ";
        $query .= "     NAMECD2, ";
        $query .= "     NAME1 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR    = '".substr($model->field["YEAR"], 0, 4)."' AND ";
        $query .= "     NAMECD1 = 'C001' ";
        $query .= " ORDER BY ";
        $query .= "     NAMECD2 ";

        return $query;
    }

    //フィールド一覧取得
    public function getFieldList($table)
    {
        $query  = " WITH MAIN AS ( ";
        $query .= "     SELECT ";
        $query .= "         COLNO, ";
        $query .= "         NAME ";
        $query .= "     FROM ";
        $query .= "         SYSIBM.SYSCOLUMNS ";
        $query .= "     WHERE ";
        $query .= "         TBNAME  = '".$table."' AND ";
        $query .= "         NULLS   = 'Y' AND ";
        $query .= "         NAME NOT IN ('APPOINTED_DAY', 'LESSON', 'REGISTERCD', 'UPDATED') ";
        $query .= "     ORDER BY ";
        $query .= "         COLNO ";
        $query .= " ) ";

        $query .= " SELECT ";
        $query .= "     NAME ";
        $query .= " FROM ";
        $query .= "     MAIN ";
        $query .= " ORDER BY ";
        $query .= "     COLNO ";

        return $query;
    }

    //マスタの追加（ＣＳＶデータより読込）
    public function insertQueryCsv($model, &$data_arr)
    {
        $data = array();

        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $subclass_field = array();
        //フィールド一覧取得（ATTEND_SUBCLASS_DAT）
        $query = knjx_c035eQuery::getFieldList("ATTEND_SUBCLASS_DAT");
        $subclass_field = $db->getCol($query);

        $cnt = 0;   //処理件数
        for ($i = 0; $i < get_count($data_arr); $i++) {
            $appointed_day = $db->getOne(knjx_c035eQuery::getAppointedDay($model, $data_arr[$i]["YEAR"], $data_arr[$i]["SCHOOL_KIND"], $data_arr[$i]["MONTH"], $data_arr[$i]["SEMESTER"]));

            $data["COPYCD"][TEXT]           = '0';
            $data["YEAR"][TEXT]             = $data_arr[$i]["YEAR"];
            $data["MONTH"][TEXT]            = $data_arr[$i]["MONTH"];
            $data["SEMESTER"][TEXT]         = $data_arr[$i]["SEMESTER"];
            $data["SCHREGNO"][TEXT]         = $data_arr[$i]["SCHREGNO"];
            if ($model->Properties["useCurriculumcd"] == "1") {
                $data["CLASSCD"][TEXT]          = $data_arr[$i]["CLASSCD"];
                $data["SCHOOL_KIND"][TEXT]      = $data_arr[$i]["SCHOOL_KIND"];
                $data["CURRICULUM_CD"][TEXT]    = $data_arr[$i]["CURRICULUM_CD"];
                $data["SUBCLASSCD"][TEXT]       = $data_arr[$i]["SUBCLASSCD"];
            } else {
                $data["CLASSCD"][TEXT]          = $data_arr[$i]["CLASSCD"];
                $data["SUBCLASSCD"][TEXT]       = $data_arr[$i]["SUBCLASSCD"];
            }
            $data["APPOINTED_DAY"][TEXT]    = $appointed_day;
            $data["LESSON"][NUMBER]         = $data_arr[$i]["LESSON"];
            foreach ($subclass_field as $subfld) {
                $data[$subfld][NUMBER]      = $data_arr[$i][$subfld];
            }
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][NUMBER]        = "SYSDATE()";

            $check  = " SELECT COUNT(*) FROM ATTEND_SUBCLASS_DAT ";
            $check .= " WHERE COPYCD        = '0' ";
            $check .= "       AND YEAR      = '".$data_arr[$i]["YEAR"]."' ";
            $check .= "       AND MONTH     = '".$data_arr[$i]["MONTH"]."' ";
            $check .= "       AND SEMESTER  = '".$data_arr[$i]["SEMESTER"]."' ";
            $check .= "       AND SCHREGNO  = '".$data_arr[$i]["SCHREGNO"]."' ";
            if ($model->Properties["useCurriculumcd"] == "1") {
                $check .= "     AND CLASSCD       = '".$data_arr[$i]["CLASSCD"]."' ";
                $check .= "     AND SCHOOL_KIND   = '".$data_arr[$i]["SCHOOL_KIND"]."' ";
                $check .= "     AND CURRICULUM_CD = '".$data_arr[$i]["CURRICULUM_CD"]."' ";
                $check .= "     AND SUBCLASSCD    = '".$data_arr[$i]["SUBCLASSCD"]."' ";
            } else {
                $check .= "     AND CLASSCD      = '".$data_arr[$i]["CLASSCD"]."' ";
                $check .= "     AND SUBCLASSCD   = '".$data_arr[$i]["SUBCLASSCD"]."' ";
            }

            if (1 > $db->getOne($check)) {
                $query = Query::insertSQL($data, "ATTEND_SUBCLASS_DAT");
            } else {
                $where  = " WHERE ";
                $where .= "     COPYCD          = '0' ";
                $where .= "     AND YEAR        = '".$data_arr[$i]["YEAR"]."' ";
                $where .= "     AND MONTH       = '".$data_arr[$i]["MONTH"]."' ";
                $where .= "     AND SEMESTER    = '".$data_arr[$i]["SEMESTER"]."' ";
                $where .= "     AND SCHREGNO    = '".$data_arr[$i]["SCHREGNO"]."' ";
                if ($model->Properties["useCurriculumcd"] == "1") {
                    $where .= "     AND CLASSCD       = '".$data_arr[$i]["CLASSCD"]."' ";
                    $where .= "     AND SCHOOL_KIND   = '".$data_arr[$i]["SCHOOL_KIND"]."' ";
                    $where .= "     AND CURRICULUM_CD = '".$data_arr[$i]["CURRICULUM_CD"]."' ";
                    $where .= "     AND SUBCLASSCD    = '".$data_arr[$i]["SUBCLASSCD"]."' ";
                } else {
                    $where .= "     AND CLASSCD      = '".$data_arr[$i]["CLASSCD"]."' ";
                    $where .= "     AND SUBCLASSCD   = '".$data_arr[$i]["SUBCLASSCD"]."' ";
                }

                $query = Query::updateSQL($data, "ATTEND_SUBCLASS_DAT", $where);
            }
            $db->query($query);
            $cnt++;
        }
        $db->commit();
        Query::dbCheckIn($db);

        return $cnt;
    }

    //削除（ＣＳＶデータより読込）
    public function deleteQueryCsv($model, &$data_arr)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $cnt = 0;   //処理件数
        for ($i = 0; $i < get_count($data_arr); $i++) {
            $query  = " DELETE FROM ";
            $query .= "     ATTEND_SUBCLASS_DAT ";
            $query .= " WHERE ";
            $query .= "     COPYCD          = '0' ";
            $query .= "     AND YEAR        = '".$data_arr[$i]["YEAR"]."' ";
            $query .= "     AND MONTH       = '".$data_arr[$i]["MONTH"]."' ";
            $query .= "     AND SEMESTER    = '".$data_arr[$i]["SEMESTER"]."' ";
            $query .= "     AND SCHREGNO    = '".$data_arr[$i]["SCHREGNO"]."' ";
            if ($model->Properties["useCurriculumcd"] == "1") {
                $query .= "     AND CLASSCD       = '".$data_arr[$i]["CLASSCD"]."' ";
                $query .= "     AND SCHOOL_KIND   = '".$data_arr[$i]["SCHOOL_KIND"]."' ";
                $query .= "     AND CURRICULUM_CD = '".$data_arr[$i]["CURRICULUM_CD"]."' ";
                $query .= "     AND SUBCLASSCD    = '".$data_arr[$i]["SUBCLASSCD"]."' ";
            } else {
                $query .= "     AND CLASSCD      = '".$data_arr[$i]["CLASSCD"]."' ";
                $query .= "     AND SUBCLASSCD   = '".$data_arr[$i]["SUBCLASSCD"]."' ";
            }

            $db->query($query);
            $cnt++;
        }
        $db->commit();
        Query::dbCheckIn($db);

        return $cnt;
    }

    //存在チェック（在籍）
    public function checkRegdDat($year, $semester, $schregno, $model)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR     = '".$year."' AND ";
        $query .= "     SEMESTER = '".$semester."' AND ";
        $query .= "     SCHREGNO = '".$schregno."' ";
        if ($model->Properties["use_prg_schoolkind"] == "1" && SCHOOLKIND != "") {
            if ($model->selectSchoolKind) {
                $query .= " AND GRADE IN (  SELECT ";
                $query .= "                     GRADE ";
                $query .= "                 FROM ";
                $query .= "                     SCHREG_REGD_GDAT ";
                $query .= "                 WHERE ";
                $query .= "                     YEAR        = '".$year."' AND ";
                $query .= "                     SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind), "','")."') ";
                $query .= "              ) ";
            }
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND GRADE IN (  SELECT ";
            $query .= "                     GRADE ";
            $query .= "                 FROM ";
            $query .= "                     SCHREG_REGD_GDAT ";
            $query .= "                 WHERE ";
            $query .= "                     YEAR        = '".$year."' AND ";
            $query .= "                     SCHOOL_KIND = '".SCHOOLKIND."' ";
            $query .= "              ) ";
        }

        return $query;
    }

    //存在チェック（名簿）
    public function checkChairStdDat($year, $semester, $chaircd, $schregno)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     CHAIR_STD_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR        = '".$year."' AND ";
        $query .= "     SEMESTER    = '".$semester."' AND ";
        $query .= "     CHAIRCD     = '".$chaircd."' AND ";
        $query .= "     SCHREGNO    = '".$schregno."' ";

        return $query;
    }

    //制限チェック
    public function checkRestrict($model, $data)
    {
        $query  = " WITH MAIN AS ( ";
        $query .= "     SELECT ";
        $query .= "         T1.CHAIRCD ";
        $query .= "     FROM ";
        $query .= "         CHAIR_DAT T1, ";
        $query .= "         CHAIR_STD_DAT T2, ";
        $query .= "         CHAIR_STF_DAT T3 ";
        $query .= "     WHERE ";
        $query .= "         T1.YEAR         = T2.YEAR AND ";
        $query .= "         T1.YEAR         = T3.YEAR AND ";
        $query .= "         T1.YEAR         = '".$data["YEAR"]."' AND ";
        $query .= "         T1.SEMESTER     = T2.SEMESTER AND ";
        $query .= "         T1.SEMESTER     = T3.SEMESTER AND ";
        $query .= "         T1.SEMESTER     = '".$data["SEMESTER"]."' AND ";
        $query .= "         T1.CHAIRCD      = T2.CHAIRCD AND ";
        $query .= "         T1.CHAIRCD      = T3.CHAIRCD AND ";
        $query .= "         T1.CHAIRCD      = '".$data["CHAIRCD"]."' AND ";
        $query .= "         T1.CLASSCD      = '".$data["CLASSCD"]."' AND ";
        if ($model->Properties["useCurriculumcd"] == "1") {
            $query .= "         T1.SCHOOL_KIND      = '".$data["SCHOOL_KIND"]."' AND ";
            $query .= "         T1.CURRICULUM_CD    = '".$data["CURRICULUM_CD"]."' AND ";
        }
        $query .= "         T1.SUBCLASSCD   = '".$data["SUBCLASSCD"]."' AND ";
        $query .= "         T2.SCHREGNO     = '".$data["SCHREGNO"]."' AND ";
        $query .= "         T3.STAFFCD      = '".STAFFCD."' ";
        $query .= " ) ";

        $query .= " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     MAIN ";

        return $query;
    }

    //締め日の取得
    public function getAppointedDay($model, $year, $school_kind, $month, $semester)
    {
        $query  = " SELECT ";
        $query .= "     APPOINTED_DAY ";
        $query .= " FROM ";
        $query .= "     APPOINTED_DAY_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR        = '". $year ."' AND ";
        if ($model->Properties["useSchool_KindField"] == "1") {
            $query .= "     SCHOOL_KIND = '". $school_kind ."' AND ";
        }
        $query .= "     MONTH       = '".$month."' AND ";
        $query .= "     SEMESTER    = '".$semester."' ";

        return $query;
    }

    //エラーＤＢへの追加
    public function insertQueryErr(&$db, $record_no, $check_error)
    {
        $data1["PROGRAMID"][TEXT]   = PROGRAMID;
        $data1["MSGROW"][NUMBER]    = $record_no;
        $data1["MSGREMARK"][TEXT]   = $check_error;

        $query = Query::insertSQL($data1, "W_CSVMSG_PRG_DAT");
        $result = $db->query($query);
    }

    //学校種別取得
    public function getSchoolKind()
    {
        $query  = " SELECT ";
        $query .= "     NAME1, ";
        $query .= "     ABBV1 ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = 'A023' ";
        $query .= " ORDER BY ";
        $query .= "     NAME1 ";

        return $query;
    }

    //学校マスタ取得
    public function getSchoolMst($model)
    {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     V_SCHOOL_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".substr($model->field["YEAR"], 0, 4)."' ";
        if ($model->Properties["useSchool_KindField"] == "1") {
            list($classcd, $school_kind, $curriculum_cd, $subclasscd) = explode('-', $model->field["SUBCLASSCD"]);
            $schoolkind = ($school_kind) ? $school_kind : SCHOOLKIND;
            $query .= " AND SCHOOLCD    = '".sprintf("%012d", SCHOOLCD)."' ";
            $query .= " AND SCHOOL_KIND = '".$schoolkind."' ";
        }

        return $query;
    }

    //CSVデータ出力
    public function selectMainQuery($model, $knjSchoolMst)
    {
        $query  = " WITH SCH_INFO AS ( ";
        $query .= "     SELECT DISTINCT";
        $query .= "         T1.GRADE, ";
        $query .= "         T1.HR_CLASS, ";
        $query .= "         T3.HR_NAME, ";
        $query .= "         T1.ATTENDNO, ";
        $query .= "         T2.NAME, ";
        $query .= "         T1.YEAR, ";
        $query .= "         T1.SEMESTER, ";
        $query .= "         T1.SCHREGNO ";
        $query .= "     FROM ";
        $query .= "         SCHREG_REGD_DAT T1, ";
        $query .= "         SCHREG_BASE_MST T2, ";
        $query .= "         SCHREG_REGD_HDAT T3, ";
        $query .= "         CHAIR_STD_DAT T4 ";
        $query .= "     WHERE ";
        $query .= "         T1.YEAR || T1.SEMESTER = '".$model->field["YEAR"]."' AND ";
        $query .= "         T1.YEAR     = T3.YEAR AND ";
        $query .= "         T1.YEAR     = T4.YEAR AND ";
        $query .= "         T1.SEMESTER = T3.SEMESTER AND ";
        $query .= "         T1.SEMESTER = T4.SEMESTER AND ";
        $query .= "         T1.SCHREGNO = T2.SCHREGNO AND ";
        $query .= "         T1.SCHREGNO = T4.SCHREGNO AND ";
        $query .= "         T1.GRADE    = T3.GRADE AND ";
        $query .= "         T1.HR_CLASS = T3.HR_CLASS AND ";
        $query .= "         T4.CHAIRCD  = '".$model->field["CHAIRCD"]."' ";
        if ($model->field["MONTH"]) {
            $query .= "         AND MONTH(T4.APPDATE) <= ".intval($model->field["MONTH"])." + CASE WHEN ".intval($model->field["MONTH"])." < 4 THEN 12 ELSE 0 END AND ";
            $query .= "         ".intval($model->field["MONTH"])." <= MONTH(T4.APPENDDATE) + CASE WHEN MONTH(T4.APPENDDATE) < 4 THEN 12 ELSE 0 END ";
        }
        if ($model->Properties["use_prg_schoolkind"] == "1" && SCHOOLKIND != "") {
            if ($model->selectSchoolKind) {
                $query .= " AND T1.GRADE IN (  SELECT ";
                $query .= "                     GRADE ";
                $query .= "                 FROM ";
                $query .= "                     SCHREG_REGD_GDAT ";
                $query .= "                 WHERE ";
                $query .= "                     YEAR        = '".substr($model->field["YEAR"], 0, 4)."' AND ";
                $query .= "                     SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind), "','")."') ";
                $query .= "              ) ";
            }
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND T1.GRADE IN (SELECT ";
            $query .= "                     GRADE ";
            $query .= "                 FROM ";
            $query .= "                     SCHREG_REGD_GDAT ";
            $query .= "                 WHERE ";
            $query .= "                     YEAR        = '".substr($model->field["YEAR"], 0, 4)."' AND ";
            $query .= "                     SCHOOL_KIND = '".SCHOOLKIND."' ";
            $query .= "              ) ";
        }
        $query .= " ), CHAIR_INFO AS ( ";
        $query .= "     SELECT ";
        if ($model->Properties["useCurriculumcd"] == "1") {
            $query .= "         T1.CLASSCD, ";
            $query .= "         T1.SCHOOL_KIND, ";
            $query .= "         T1.CURRICULUM_CD, ";
        }
        $query .= "         T1.SUBCLASSCD, ";
        $query .= "         T2.SUBCLASSNAME, ";
        $query .= "         T1.CHAIRCD, ";
        $query .= "         T1.CHAIRNAME ";
        $query .= "     FROM ";
        $query .= "         CHAIR_DAT T1 ";
        $query .= "         LEFT JOIN SUBCLASS_MST T2 ON T1.SUBCLASSCD      = T2.SUBCLASSCD ";
        if ($model->Properties["useCurriculumcd"] == "1") {
            $query .= "                                  AND T1.CLASSCD         = T2.CLASSCD ";
            $query .= "                                  AND T1.SCHOOL_KIND     = T2.SCHOOL_KIND ";
            $query .= "                                  AND T1.CURRICULUM_CD   = T2.CURRICULUM_CD ";
        }
        $query .= "     WHERE ";
        $query .= "         T1.YEAR || T1.SEMESTER = '".$model->field["YEAR"]."' AND ";
        $query .= "         T1.CHAIRCD = '".$model->field["CHAIRCD"]."' AND ";
        if ($model->Properties["useCurriculumcd"] == "1") {
            $query .= "         T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' ||  T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD   = '".$model->field["SUBCLASSCD"]."' ";
        } else {
            $query .= "         T1.SUBCLASSCD   = '".$model->field["SUBCLASSCD"]."' ";
        }
        $query .= " ) ";

        $query .= " SELECT ";
        if ($model->Properties["useCurriculumcd"] == "1") {
            $query .= "     T2.CLASSCD, ";
            $query .= "     T2.SCHOOL_KIND, ";
            $query .= "     T2.CURRICULUM_CD, ";
            $query .= "     T2.SUBCLASSCD, ";
        } else {
            $query .= "     T2.CLASSCD, ";
            $query .= "     T2.SUBCLASSCD, ";
        }
        $query .= "     T3.SUBCLASSNAME, ";
        $query .= "     T3.CHAIRCD, ";
        $query .= "     T3.CHAIRNAME, ";
        $query .= "     T1.YEAR, ";
        $query .= "     T2.MONTH, ";
        $query .= "     T2.SEMESTER, ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     T1.HR_NAME, ";
        $query .= "     T1.ATTENDNO, ";
        $query .= "     T1.NAME, ";
        $query .= "     T2.APPOINTED_DAY, ";
        $query .= "     T2.LESSON, ";
        if ($model->titleValA004["2"]) {
            $query .= "     T2.OFFDAYS, ";
        }
        if ($model->titleValA004["1"]) {
            $query .= "     T2.ABROAD, ";
        }
        if ($model->titleValC001["1"]) {
            $query .= "     T2.ABSENT, ";
        }
        if ($model->titleValC001["2"]) {
            $query .= "     T2.SUSPEND, ";
        }
        if ($model->titleValC001["25"] && $model->Properties["useKoudome"] == "true") {
            $query .= "     T2.KOUDOME, ";
        }
        if ($model->titleValC001["19"] && $model->Properties["useVirus"] == "true") {
            $query .= "     T2.VIRUS, ";
        }
        if ($model->titleValC001["3"]) {
            $query .= "     T2.MOURNING, ";
        }
        //出席すべき時数
        $query .= "     VALUE(T2.LESSON, 0) ";
        $A004Array = array("1" => "ABROAD", "2" => "OFFDAYS");
        foreach ($model->titleValA004 as $key => $val) {
            if ($A004Array[$key]) {
                $query .= "     - VALUE(T2.{$A004Array[$key]}, 0) ";
            }
            if ($A004Array[$key] == "OFFDAYS" && $knjSchoolMst["SUB_OFFDAYS"] == "1") {
                $query .= "     + VALUE(T2.{$A004Array[$key]}, 0) ";
            }
        }
        $C001Array = array("2" => "SUSPEND", "3" => "MOURNING");
        if ($model->Properties["useVirus"] == "true") {
            $C001Array["19"] = "VIRUS";
        }
        if ($model->Properties["useKoudome"] == "true") {
            $C001Array["25"] = "KOUDOME";
        }
        foreach ($model->titleValC001 as $key => $val) {
            if ($C001Array[$key] && $knjSchoolMst["SUB_".$val] != "1") {
                $query .= "     - VALUE(T2.{$C001Array[$key]}, 0) ";
            }
        }
        $query .= "     AS ATTEND, ";
        if ($model->titleValC001["4"]) {
            $query .= "     T2.SICK, ";
        }
        if ($model->titleValC001["5"]) {
            $query .= "     T2.NOTICE, ";
        }
        if ($model->titleValC001["6"]) {
            $query .= "     T2.NONOTICE, ";
        }
        if ($model->titleValC001["14"]) {
            $query .= "     T2.NURSEOFF, ";
        }
        if ($model->titleValC001["15"]) {
            $query .= "     T2.LATE, ";
        }
        if ($model->titleValC001["16"]) {
            $query .= "     T2.EARLY, ";
        }
        $query .= "     '".$model->lastColumn."' AS ".$model->lastColumn." ";
        $query .= " FROM ";
        $query .= "     SCH_INFO T1 ";
        $query .= "     LEFT JOIN ATTEND_SUBCLASS_DAT T2 ON T1.SCHREGNO     = T2.SCHREGNO AND ";
        $query .= "                                         T1.YEAR         = T2.YEAR AND  ";
        $query .= "                                         T1.SEMESTER     = T2.SEMESTER AND ";
        $query .= "                                         T2.MONTH        = '".$model->field["MONTH"]."' AND ";
        if ($model->Properties["useCurriculumcd"] == "1") {
            $query .= "                                         T2.CLASSCD || '-' || T2.SCHOOL_KIND || '-' ||  T2.CURRICULUM_CD || '-' || T2.SUBCLASSCD   = '".$model->field["SUBCLASSCD"]."' ";
        } else {
            $query .= "                                         T2.CLASSCD      = '".substr($model->field["SUBCLASSCD"], 0, 2)."' AND ";
            $query .= "                                         T2.SUBCLASSCD   = '".$model->field["SUBCLASSCD"]."' ";
        }
        $query .= "     LEFT JOIN CHAIR_INFO T3 ON T2.SUBCLASSCD    = T3.SUBCLASSCD ";
        if ($model->Properties["useCurriculumcd"] == "1") {
            $query .= "                            AND T2.CLASSCD       = T3.CLASSCD ";
            $query .= "                            AND T2.SCHOOL_KIND   = T3.SCHOOL_KIND ";
            $query .= "                            AND T2.CURRICULUM_CD = T3.CURRICULUM_CD ";
        }
        $query .= " ORDER BY ";
        $query .= "         T1.GRADE, ";
        $query .= "         T1.HR_CLASS, ";
        $query .= "         T1.ATTENDNO, ";
        $query .= "         T1.SCHREGNO ";
        return $query;
    }

    //科目名、講座名
    public function getName($model)
    {
        $query  = " SELECT ";
        $query .= "     T1.SUBCLASSNAME, ";
        $query .= "     T2.CHAIRNAME ";
        $query .= " FROM ";
        $query .= "     V_SUBCLASS_MST T1, ";
        $query .= "     CHAIR_DAT T2";
        $query .= " WHERE ";
        $query .= "     T2.YEAR || T2.SEMESTER = '".$model->field["YEAR"]."' ";
        $query .= " AND T2.CHAIRCD = '".$model->field["CHAIRCD"]."' ";
        $query .= " AND T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' ||  T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD   = '".$model->field["SUBCLASSCD"]."' ";
        $query .= " AND T1.YEAR          = T2.YEAR ";
        $query .= " AND T1.CLASSCD       = T2.CLASSCD ";
        $query .= " AND T1.SCHOOL_KIND   = T2.SCHOOL_KIND ";
        $query .= " AND T1.CURRICULUM_CD = T2.CURRICULUM_CD ";
        $query .= " AND T1.SUBCLASSCD    = T2.SUBCLASSCD ";

        return $query;
    }
}
