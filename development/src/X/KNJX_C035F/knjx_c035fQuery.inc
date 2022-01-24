<?php

require_once('for_php7.php');
class knjx_c035fQuery extends Query
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

    //MIN権限グループ取得
    public function getMinGroupcd($model, $sk)
    {
        $query  = " SELECT ";
        $query .= "     SUBSTR(MIN((CASE GROUPCD WHEN '9999' THEN '0' ELSE '1' END) || GROUPCD),2) ";
        $query .= " FROM ";
        $query .= "     USERGROUP_DAT T1 ";
        $query .= " WHERE ";
        $query .= "     YEAR        = '".CTRL_YEAR."' AND ";
        if ($model->Properties["use_prg_schoolkind"] == "1" || $model->Properties["useSchool_KindField"] == "1") {
            $query .= "     SCHOOLCD    = '".sprintf("%012d", SCHOOLCD)."' AND ";
            $query .= "     SCHOOL_KIND = '".SCHOOLKIND."' AND ";
        } elseif ($model->Properties["use_prg_schoolkind"] == "1" || $model->Properties["useSchool_KindField"] == "1") {
            $query .= "     SCHOOLCD    = '".sprintf("%012d", SCHOOLCD)."' AND ";
            $query .= "     SCHOOL_KIND = '".$sk."' AND ";
        }
        $query .= "     STAFFCD     = '".STAFFCD."' AND ";
        $query .= "     EXISTS (SELECT ";
        $query .= "                 'X' ";
        $query .= "             FROM ";
        $query .= "                 ADMIN_CONTROL_ATTEND_DAT E1 ";
        $query .= "             WHERE ";
        $query .= "                 T1.YEAR         = E1.YEAR AND ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            $query .= "                 T1.SCHOOL_KIND = '".SCHOOLKIND."' AND ";
        } elseif ($model->Properties["useSchool_KindField"] == "1") {
            $query .= "                 T1.SCHOOL_KIND  = E1.SCHOOL_KIND AND ";
        }
        $query .= "                 E1.PROGRAMID    = '".$model->getPrgId."' AND ";
        $query .= "                 T1.GROUPCD      = E1.GROUPCD ";
        $query .= "             ) ";

        return $query;
    }

    //出欠項目名一覧取得
    public function getAttendNameList($model)
    {
        $query  = " SELECT ";
        $query .= "     NAMECD1, ";
        $query .= "     NAMECD2, ";
        $query .= "     NAME1 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR    = '".CTRL_YEAR."' AND ";
        $query .= "     NAMECD1 = 'A004' ";
        $query .= " UNION  ";
        $query .= " SELECT ";
        $query .= "     NAMECD1, ";
        $query .= "     NAMECD2, ";
        $query .= "     NAME1 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR    = '".CTRL_YEAR."' AND ";
        $query .= "     NAMECD1 = 'C001' ";

        return $query;
    }

    //出欠項目名一覧取得
    public function getAdminControlAttendItemnameDat($model, $sk, $cm)
    {
        //学校種別
        $school_kind = '00';
        if ($model->Properties["use_prg_schoolkind"] == "1" || $model->Properties["useSchool_KindField"] == "1") {
            $school_kind = $sk;
        }

        $query  = " SELECT ";
        $query .= "     ATTEND_ITEM, ";
        $query .= "     ATTEND_ITEMNAME ";
        $query .= " FROM ";
        $query .= "     ADMIN_CONTROL_ATTEND_ITEMNAME_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR        = '".CTRL_YEAR."' AND ";
        $query .= "     SCHOOL_KIND = '".$school_kind."' AND ";
        $query .= "     ATTEND_DIV  = '2' AND ";
        $query .= "     GRADE       = '00'  AND ";
        $query .= "     COURSECD || MAJORCD = '".$cm."' ";

        return $query;
    }

    //出欠管理者コントロール取得
    public function getAdminControlAttendDat($model, $sk, $cm)
    {
        $school_kind = '00';
        if ($model->Properties["use_prg_schoolkind"] == "1" || $model->Properties["useSchool_KindField"] == "1") {
            $school_kind = $sk;
        }

        $query  = " SELECT ";
        $query .= "     CONTROL_DIV, ";
        $query .= "     ATTEND_ITEM, ";
        $query .= "     SHOWORDER, ";
        $query .= "     INPUT_FLG ";
        $query .= " FROM ";
        $query .= "     ADMIN_CONTROL_ATTEND_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR        = '".CTRL_YEAR."' AND ";
        $query .= "     SCHOOL_KIND = '".$school_kind."' AND ";
        $query .= "     ATTEND_DIV  = '2' AND ";
        $query .= "     GRADE       = '00'  AND ";
        $query .= "     COURSECD || MAJORCD = '".$cm."' AND ";
        $query .= "     PROGRAMID   = '".$model->getPrgId."' AND ";
        $query .= "     GROUPCD     = '".$model->groupcd."' ";
        $query .= " ORDER BY ";
        $query .= "     INT(SHOWORDER) ";

        return $query;
    }

    //学校種別の取得
    public function getNameMstA023($model)
    {
        $query  = " SELECT ";
        $query .= "     NAME1 AS VALUE, ";
        $query .= "     ABBV1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR    = '".CTRL_YEAR."' AND ";
        $query .= "     NAMECD1 = 'A023' ";
        if ($model->selectSchoolKind) {
            $query .= "     AND NAME1 IN ('".implode(explode(':', $model->selectSchoolKind), "','")."') ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //課程学科コンボ取得
    public function getCourseMajor($model, $sk)
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     T1.COURSECD || T1.MAJORCD AS VALUE, ";
        $query .= "     T1.COURSECD || T1.MAJORCD || ':' || S1.COURSENAME || S1.MAJORNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1 ";
        $query .= "     INNER JOIN V_COURSE_MAJOR_MST S1 ";
        $query .= "          ON T1.YEAR         = S1.YEAR ";
        $query .= "         AND T1.COURSECD     = S1.COURSECD ";
        $query .= "         AND T1.MAJORCD      = S1.MAJORCD ";
        if ($model->Properties["use_prg_schoolkind"] == "1" || $model->Properties["useSchool_KindField"] == "1") {
            $query .= "     INNER JOIN SCHREG_REGD_GDAT S2 ";
            $query .= "          ON T1.YEAR         = S2.YEAR ";
            $query .= "         AND T1.GRADE        = S2.GRADE ";
            $query .= "         AND S2.SCHOOL_KIND  = '".$sk."' ";
        }
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //年度学期一覧
    public function getSelectFieldSQL($model)
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     T1.YEAR || T1.SEMESTER AS VALUE, ";
        $query .= "     T1.YEAR || '年度 ' || T2.SEMESTERNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     CHAIR_STF_DAT T1, ";
        $query .= "     SEMESTER_MST T2, ";
        $query .= "     CHAIR_STD_DAT T3, ";
        $query .= "     SCHREG_REGD_DAT T4 ";
        if ($model->Properties["use_prg_schoolkind"] == "1" || $model->Properties["useSchool_KindField"] == "1") {
            $query .= "     INNER JOIN SCHREG_REGD_GDAT G1 ";
            $query .= "              ON T4.YEAR         = G1.YEAR ";
            $query .= "             AND T4.GRADE        = G1.GRADE ";
            $query .= "             AND G1.SCHOOL_KIND  = '".$model->field["SCHOOL_KIND"]."' ";
        }
        $query .= " WHERE ";
        $query .= "     T1.YEAR     = T2.YEAR AND ";
        $query .= "     T1.YEAR     = T3.YEAR AND ";
        $query .= "     T1.YEAR     = T4.YEAR AND ";
        $query .= "     T1.SEMESTER = T2.SEMESTER AND ";
        $query .= "     T1.SEMESTER = T3.SEMESTER AND ";
        $query .= "     T1.SEMESTER = T4.SEMESTER AND ";
        $query .= "     T1.CHAIRCD  = T3.CHAIRCD AND ";
        $query .= "     T3.SCHREGNO = T4.SCHREGNO ";
        if ($model->Properties["use_school_detail_gcm_dat"] == "1") {
            $query .= " AND T4.COURSECD || T4.MAJORCD = '".$model->field["COURSE_MAJOR"]."' ";
        }
        //権限（制限付）
        if ($model->auth == DEF_REFER_RESTRICT || $model->auth == DEF_UPDATE_RESTRICT) {
            $query .= " AND T1.STAFFCD = '".STAFFCD."' ";
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
            $query .= "     T3.CLASSCD || '-' || T3.SCHOOL_KIND || '-' ||  T3.CURRICULUM_CD || '-' || T3.SUBCLASSCD || ' ' || VALUE(T3.SUBCLASSABBV, '') AS LABEL ";
        } else {
            $query .= "     T3.SUBCLASSCD AS VALUE, ";
            $query .= "     T3.SUBCLASSCD || VALUE(T3.SUBCLASSABBV, '') AS LABEL ";
        }
        $query .= " FROM ";
        $query .= "     CHAIR_DAT T1, ";
        $query .= "     CHAIR_STF_DAT T2, ";
        $query .= "     SUBCLASS_MST T3, ";
        $query .= "     CHAIR_STD_DAT T4, ";
        $query .= "     SCHREG_REGD_DAT T5 ";
        if ($model->Properties["use_prg_schoolkind"] == "1" || $model->Properties["useSchool_KindField"] == "1") {
            $query .= "     INNER JOIN SCHREG_REGD_GDAT G1 ";
            $query .= "              ON T5.YEAR         = G1.YEAR ";
            $query .= "             AND T5.GRADE        = G1.GRADE ";
            $query .= "             AND G1.SCHOOL_KIND  = '".$model->field["SCHOOL_KIND"]."' ";
        }
        $query .= " WHERE ";
        $query .= "     T1.YEAR || T1.SEMESTER = '".$model->field["YEAR"]."' AND ";
        $query .= "     T1.YEAR         = T2.YEAR AND ";
        $query .= "     T1.YEAR         = T4.YEAR AND ";
        $query .= "     T1.YEAR         = T5.YEAR AND ";
        $query .= "     T1.SEMESTER     = T2.SEMESTER AND ";
        $query .= "     T1.SEMESTER     = T4.SEMESTER AND ";
        $query .= "     T1.SEMESTER     = T5.SEMESTER AND ";
        $query .= "     T1.CHAIRCD      = T2.CHAIRCD AND ";
        $query .= "     T1.CHAIRCD      = T4.CHAIRCD AND ";
        //権限（制限付）
        if ($model->auth == DEF_REFER_RESTRICT || $model->auth == DEF_UPDATE_RESTRICT) {
            $query .= "     T2.STAFFCD      = '".STAFFCD."' AND ";
        }
        if ($model->Properties["useCurriculumcd"] == "1") {
            if ($model->Properties["use_prg_schoolkind"] == "1" || $model->Properties["useSchool_KindField"] == "1") {
                $query .= "     T1.SCHOOL_KIND  = '".$model->field["SCHOOL_KIND"]."' AND ";
            }
            $query .= "     T1.CLASSCD          = T3.CLASSCD AND ";
            $query .= "     T1.SCHOOL_KIND      = T3.SCHOOL_KIND AND ";
            $query .= "     T1.CURRICULUM_CD    = T3.CURRICULUM_CD AND ";
        }
        $query .= "     T1.SUBCLASSCD   = T3.SUBCLASSCD AND ";
        $query .= "     T4.SCHREGNO     = T5.SCHREGNO ";
        if ($model->Properties["use_school_detail_gcm_dat"] == "1") {
            $query .= " AND T5.COURSECD || T5.MAJORCD = '".$model->field["COURSE_MAJOR"]."' ";
        }
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
        $query .= "     T1.CHAIRCD || ' ' || VALUE(T1.CHAIRNAME, '') AS LABEL ";
        $query .= " FROM ";
        $query .= "     CHAIR_DAT T1, ";
        $query .= "     CHAIR_STF_DAT T2, ";
        $query .= "     CHAIR_STD_DAT T3, ";
        $query .= "     SCHREG_REGD_DAT T4 ";
        if ($model->Properties["use_prg_schoolkind"] == "1" || $model->Properties["useSchool_KindField"] == "1") {
            $query .= "     INNER JOIN SCHREG_REGD_GDAT G1 ";
            $query .= "              ON T4.YEAR         = G1.YEAR ";
            $query .= "             AND T4.GRADE        = G1.GRADE ";
            $query .= "             AND G1.SCHOOL_KIND  = '".$model->field["SCHOOL_KIND"]."' ";
        }
        $query .= " WHERE ";
        $query .= "     T1.YEAR || T1.SEMESTER = '".$model->field["YEAR"]."' AND ";
        $query .= "     T1.YEAR     = T2.YEAR AND ";
        $query .= "     T1.YEAR     = T3.YEAR AND ";
        $query .= "     T1.YEAR     = T4.YEAR AND ";
        $query .= "     T1.SEMESTER = T2.SEMESTER AND ";
        $query .= "     T1.SEMESTER = T3.SEMESTER AND ";
        $query .= "     T1.SEMESTER = T4.SEMESTER AND ";
        $query .= "     T1.CHAIRCD  = T2.CHAIRCD AND ";
        $query .= "     T1.CHAIRCD  = T3.CHAIRCD AND ";
        //権限（制限付）
        if ($model->auth == DEF_REFER_RESTRICT || $model->auth == DEF_UPDATE_RESTRICT) {
            $query .= "     T2.STAFFCD  = '".STAFFCD."' AND ";
        }
        if ($model->Properties["useCurriculumcd"] == "1") {
            $query .= "     T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' ||  T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD = '".$model->field["SUBCLASSCD"]."' AND ";
        } else {
            $query .= "     T1.SUBCLASSCD = '".$model->field["SUBCLASSCD"]."' AND ";
        }
        $query .= "     T3.SCHREGNO = T4.SCHREGNO ";
        if ($model->Properties["use_school_detail_gcm_dat"] == "1") {
            $query .= " AND T4.COURSECD || T4.MAJORCD = '".$model->field["COURSE_MAJOR"]."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     T1.CHAIRCD ";

        return $query;
    }

    //学期情報取得
    public function selectSemesAll($year_sem, $flg = "")
    {
        $query  = " SELECT ";
        $query .= "     SEMESTER, ";
        $query .= "     SEMESTERNAME, ";
        if (!$flg) {
            $query .= "     CASE WHEN MONTH(SDATE) < 4 ";
            $query .= "          THEN MONTH(SDATE) + 12 ";
            $query .= "          ELSE MONTH(SDATE) END AS S_MONTH, ";
            $query .= "     CASE WHEN MONTH(EDATE) < 4 ";
            $query .= "          THEN MONTH(EDATE) + 12 ";
            $query .= "          ELSE MONTH(EDATE) END AS E_MONTH ";
        } else {
            $query .= "     MONTH(SDATE) AS S_MONTH, ";
            $query .= "     DAY(SDATE) AS S_DAY, ";
            $query .= "     MONTH(EDATE) AS E_MONTH, ";
            $query .= "     DAY(EDATE) AS E_DAY ";
        }
        $query .= " FROM ";
        $query .= "     SEMESTER_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR || SEMESTER = '".$year_sem."' ";
        $query .= " ORDER BY ";
        $query .= "     SEMESTER ";

        return $query;
    }

    //対象月データの取得
    public function selectMonthQuery($year, $month, $model)
    {
        $setNameCd = "Z005";
        if ($model->Properties["use_prg_schoolkind"] == "1" || $model->Properties["useSchool_KindField"] == "1") {
            $setNameCd = "Z".$model->field["SCHOOL_KIND"]."05";
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
        if ($model->Properties["use_prg_schoolkind"] == "1" || $model->Properties["useSchool_KindField"] == "1") {
            $query .= "                     SCHOOL_KIND = '".$model->field["SCHOOL_KIND"]."' AND ";
        }
        $query .= "                     CONTROL_FLG = '2') AND ";
        $query .= "     NAMECD2 = '".sprintf('%02d', $month)."' ";
        $query .= " ORDER BY ";
        $query .= "     NAMESPARE1 ";

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
        $query .= "         NAME NOT IN ('APPOINTED_DAY', 'REGISTERCD', 'UPDATED') ";
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
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $subclass_field = array();
        //フィールド一覧取得（ATTEND_SUBCLASS_DAT）
        $query = knjx_c035fQuery::getFieldList("ATTEND_SUBCLASS_DAT");
        $subclass_field = $db->getCol($query);

        $cnt = 0;   //処理件数
        for ($i = 0; $i < get_count($data_arr); $i++) {
            $appointed_day = $db->getOne(knjx_c035fQuery::getAppointedDay($model, $data_arr[$i]["YEAR"], $data_arr[$i]["MONTH"], $data_arr[$i]["SEMESTER"]));

            $data = array();
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
            $data["APPOINTED_DAY"][TEXT]        = $appointed_day;
            foreach ($model->item_array as $order => $val) {
                //入力可の項目が更新対象
                if (in_array($val["item"], $subclass_field) && $val["input"]) {
                    $data[$val["item"]][NUMBER] = $data_arr[$i][$val["item"]];
                }
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
                $check .= "     AND CLASSCD       = '".$data_arr[$i]["CLASSCD"]."' ";
                $check .= "     AND SUBCLASSCD    = '".$data_arr[$i]["SUBCLASSCD"]."' ";
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
                $query .= "     AND CLASSCD       = '".$data_arr[$i]["CLASSCD"]."' ";
                $query .= "     AND SUBCLASSCD    = '".$data_arr[$i]["SUBCLASSCD"]."' ";
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
        if ($model->Properties["use_prg_schoolkind"] == "1" || $model->Properties["useSchool_KindField"] == "1") {
            $query .= " AND GRADE IN (  SELECT ";
            $query .= "                     GRADE ";
            $query .= "                 FROM ";
            $query .= "                     SCHREG_REGD_GDAT ";
            $query .= "                 WHERE ";
            $query .= "                     YEAR        = '".$year."' AND ";
            $query .= "                     SCHOOL_KIND = '".$model->field["SCHOOL_KIND"]."' ";
            $query .= "              ) ";
        }
        if ($model->Properties["use_school_detail_gcm_dat"] == "1") {
            $query .= " AND COURSECD || MAJORCD = '".$model->field["COURSE_MAJOR"]."' ";
        }

        return $query;
    }

    //存在チェック（名簿）
    public function checkChairStdDat($year, $semester, $chaircd, $schregno, $model)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     CHAIR_STD_DAT T1, ";
        $query .= "     SCHREG_REGD_DAT T2 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR         = T2.YEAR AND ";
        $query .= "     T1.YEAR         = '".$year."' AND ";
        $query .= "     T1.SEMESTER     = T2.SEMESTER AND ";
        $query .= "     T1.SEMESTER     = '".$semester."' AND ";
        $query .= "     T1.CHAIRCD      = '".$chaircd."' AND ";
        $query .= "     T1.SCHREGNO     = T2.SCHREGNO AND ";
        $query .= "     T1.SCHREGNO     = '".$schregno."' ";
        if ($model->Properties["use_school_detail_gcm_dat"] == "1") {
            $query .= " AND T2.COURSECD || T2.MAJORCD   = '".$model->field["COURSE_MAJOR"]."' ";
        }

        return $query;
    }

    //異動データ（退学・転学・卒業）取得
    public function getIdouData($schregno, $date1, $date2)
    {
        $query  = " SELECT ";
        $query .= "     CASE WHEN GRD_DATE < '".$date2."' THEN 1 ELSE 0 END ";
        $query .= " FROM ";
        $query .= "     SCHREG_BASE_MST ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO = '".$schregno."' AND ";
        $query .= "     GRD_DIV IN ('1', '2', '3', '6', '7') ";

        return $query;
    }

    //異動データ（留学・休学）取得
    public function getTransferData($schregno, $date1, $date2)
    {
        $query  = " SELECT ";
        $query .= "     SUM(CASE WHEN TRANSFER_SDATE <= '".$date2."' AND '".$date1."' <= CASE WHEN TRANSFER_EDATE IS NULL THEN '".(CTRL_YEAR+1)."' || '-03-31' ELSE TRANSFER_EDATE END THEN 1 ELSE 0 END) ";
        $query .= " FROM ";
        $query .= "     SCHREG_TRANSFER_DAT ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO = '".$schregno."' AND ";
        $query .= "     TRANSFERCD IN ('1', '2') ";

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
        $query .= "         CHAIR_STF_DAT T3, ";
        $query .= "         SCHREG_REGD_DAT T4 ";
        $query .= "     WHERE ";
        $query .= "         T1.YEAR         = T2.YEAR AND ";
        $query .= "         T1.YEAR         = T3.YEAR AND ";
        $query .= "         T1.YEAR         = T4.YEAR AND ";
        $query .= "         T1.YEAR         = '".$data["YEAR"]."' AND ";
        $query .= "         T1.SEMESTER     = T2.SEMESTER AND ";
        $query .= "         T1.SEMESTER     = T3.SEMESTER AND ";
        $query .= "         T1.SEMESTER     = T4.SEMESTER AND ";
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
        $query .= "         T2.SCHREGNO     = T4.SCHREGNO AND ";
        $query .= "         T2.SCHREGNO     = '".$data["SCHREGNO"]."' AND ";
        $query .= "         T3.STAFFCD      = '".STAFFCD."' ";
        if ($model->Properties["use_school_detail_gcm_dat"] == "1") {
            $query .= "     AND T4.COURSECD || T4.MAJORCD   = '".$model->field["COURSE_MAJOR"]."' ";
        }
        $query .= " ) ";

        $query .= " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     MAIN ";

        return $query;
    }

    //締め日の取得
    public function getAppointedDay($model, $year, $month, $semester)
    {
        $query  = " SELECT ";
        $query .= "     APPOINTED_DAY ";
        $query .= " FROM ";
        $query .= "     APPOINTED_DAY_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR     = '".$year."' AND ";
        if ($model->Properties["useSchool_KindField"] == "1") {
            $query .= "     SCHOOL_KIND     = '".$model->field["SCHOOL_KIND"]."' AND ";
        }
        $query .= "     MONTH    = '".$month."' AND ";
        $query .= "     SEMESTER = '".$semester."' ";

        return $query;
    }

    //エラーＤＢへの追加
    public function insertQueryErr(&$db, $record_no, $check_error)
    {
        $data1 = array();
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

    //CSVデータ出力
    public function selectMainQuery($model)
    {
        //累積期間月を配列にする。
        $range_month = array("04" => "'04'",
                             "05" => "'04','05'",
                             "06" => "'04','05','06'",
                             "07" => "'04','05','06','07'",
                             "08" => "'04','05','06','07','08'",
                             "09" => "'04','05','06','07','08','09'",
                             "10" => "'04','05','06','07','08','09','10'",
                             "11" => "'04','05','06','07','08','09','10','11'",
                             "12" => "'04','05','06','07','08','09','10','11','12'",
                             "01" => "'04','05','06','07','08','09','10','11','12','01'",
                             "02" => "'04','05','06','07','08','09','10','11','12','01','02'",
                             "03" => "'04','05','06','07','08','09','10','11','12','01','02','03'"
                             );

        $query  = " WITH SCH_INFO AS ( ";
        $query .= "     SELECT DISTINCT ";
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
        $query .= "         T1.HR_CLASS = T3.HR_CLASS ";
        if ($model->field["CHAIRCD"] != 'all') {
            $query .= "         AND T4.CHAIRCD  = '".$model->field["CHAIRCD"]."' ";
        }
        if ($model->Properties["use_school_detail_gcm_dat"] == "1") {
            $query .= "         AND T1.COURSECD || T1.MAJORCD  = '".$model->field["COURSE_MAJOR"]."' ";
        }
        if ($model->field["MONTH"]) {
            $query .= "         AND ".intval($model->field["MONTH"])." + CASE WHEN ".intval($model->field["MONTH"])." < 4 THEN 12 ELSE 0 END ";
            $query .= "         BETWEEN ";
            $query .= "             MONTH(T4.APPDATE) + CASE WHEN MONTH(T4.APPDATE) < 4 THEN 12 ELSE 0 END  ";
            $query .= "             AND ";
            $query .= "             MONTH(T4.APPENDDATE) + CASE WHEN MONTH(T4.APPENDDATE) < 4 THEN 12 ELSE 0 END ";
        }
        if ($model->Properties["use_prg_schoolkind"] == "1" || $model->Properties["useSchool_KindField"] == "1") {
            $query .= " AND T1.GRADE IN (SELECT ";
            $query .= "                     GRADE ";
            $query .= "                 FROM ";
            $query .= "                     SCHREG_REGD_GDAT ";
            $query .= "                 WHERE ";
            $query .= "                     YEAR        = '".substr($model->field["YEAR"], 0, 4)."' AND ";
            $query .= "                     SCHOOL_KIND = '".$model->field["SCHOOL_KIND"]."' ";
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
        if ($model->field["CHAIRCD"] != 'all') {
            $query .= "         T1.CHAIRCD = '".$model->field["CHAIRCD"]."' AND ";
        }
        if ($model->Properties["useCurriculumcd"] == "1") {
            $query .= "         T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' ||  T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD   = '".$model->field["SUBCLASSCD"]."' ";
        } else {
            $query .= "         T1.SUBCLASSCD   = '".$model->field["SUBCLASSCD"]."' ";
        }
        $query .= " ) ";

        $query .= " , SUM_ATTEND_SEMES AS ( ";
        $query .= "     SELECT ";
        foreach ($model->itemR_array as $key => $val) {
            $query .= "    SUM({$val["key"]}) AS {$val["item"]}, ";
        }
        $query .= "         SCHREGNO ";
        $query .= "     FROM ";
        $query .= "         ATTEND_SUBCLASS_DAT ";
        $query .= "     WHERE ";
        $query .= "         YEAR = '".CTRL_YEAR."' AND ";
        $query .= "     MONTH IN( ".$range_month[$model->field["MONTH"]].") AND ";
        $query .= "     SEMESTER <= '".$model->field["SEMESTER"]."' AND ";
        if ($model->Properties["useCurriculumcd"] == "1") {
            $query .= "                                         CLASSCD || '-' || SCHOOL_KIND || '-' ||  CURRICULUM_CD || '-' || SUBCLASSCD   = '".$model->field["SUBCLASSCD"]."' ";
        } else {
            $query .= "                                         CLASSCD      = '".substr($model->field["SUBCLASSCD"], 0, 2)."' AND ";
            $query .= "                                         SUBCLASSCD   = '".$model->field["SUBCLASSCD"]."' ";
        }
        $query .= "     GROUP BY ";
        $query .= "         SCHREGNO ";
        $query .= " ) ";

        $query .= " SELECT ";
        if ($model->Properties["useCurriculumcd"] == "1") {
            $query .= "     AT_SUB.CLASSCD, ";
            $query .= "     AT_SUB.SCHOOL_KIND, ";
            $query .= "     AT_SUB.CURRICULUM_CD, ";
            $query .= "     AT_SUB.SUBCLASSCD, ";
        } else {
            $query .= "     AT_SUB.CLASSCD, ";
            $query .= "     AT_SUB.SUBCLASSCD, ";
        }
        $query .= "     C_INFO.SUBCLASSNAME, ";
        $query .= "     C_INFO.CHAIRCD, ";
        $query .= "     C_INFO.CHAIRNAME, ";
        $query .= "     SCH_INFO.YEAR, ";
        $query .= "     AT_SUB.MONTH, ";
        $query .= "     AT_SUB.SEMESTER, ";
        $query .= "     SCH_INFO.SCHREGNO, ";
        $query .= "     SCH_INFO.HR_NAME, ";
        $query .= "     SCH_INFO.ATTENDNO, ";
        $query .= "     SCH_INFO.NAME, ";
        $query .= "     AT_SUB.APPOINTED_DAY, ";
        foreach ($model->item_array as $key => $val) {
            $query .= "     AT_SUB.".$val["item"].", ";
        }
        foreach ($model->itemR_array as $key => $val) {
            $query .= "     SUMAD.{$val["item"]}, ";
        }
        $query .= "     '".$model->lastColumn."' AS ".$model->lastColumn." ";
        $query .= " FROM ";
        $query .= "     SCH_INFO ";
        $query .= "     LEFT JOIN ATTEND_SUBCLASS_DAT AT_SUB ON SCH_INFO.SCHREGNO = AT_SUB.SCHREGNO AND ";
        $query .= "                                         SCH_INFO.YEAR         = AT_SUB.YEAR AND  ";
        $query .= "                                         SCH_INFO.SEMESTER     = AT_SUB.SEMESTER AND ";
        $query .= "                                         AT_SUB.MONTH          = '".$model->field["MONTH"]."' AND ";
        if ($model->Properties["useCurriculumcd"] == "1") {
            $query .= "                                         AT_SUB.CLASSCD || '-' || AT_SUB.SCHOOL_KIND || '-' ||  AT_SUB.CURRICULUM_CD || '-' || AT_SUB.SUBCLASSCD   = '".$model->field["SUBCLASSCD"]."' ";
        } else {
            $query .= "                                         AT_SUB.CLASSCD      = '".substr($model->field["SUBCLASSCD"], 0, 2)."' AND ";
            $query .= "                                         AT_SUB.SUBCLASSCD   = '".$model->field["SUBCLASSCD"]."' ";
        }
        $query .= "     INNER JOIN CHAIR_INFO C_INFO ON AT_SUB.SUBCLASSCD   = C_INFO.SUBCLASSCD ";
        if ($model->Properties["useCurriculumcd"] == "1") {
            $query .= "                            AND AT_SUB.CLASSCD       = C_INFO.CLASSCD ";
            $query .= "                            AND AT_SUB.SCHOOL_KIND   = C_INFO.SCHOOL_KIND ";
            $query .= "                            AND AT_SUB.CURRICULUM_CD = C_INFO.CURRICULUM_CD ";
        }
        $query .= "     INNER JOIN CHAIR_STD_DAT CSTD ON  SCH_INFO.YEAR     = CSTD.YEAR ";
        $query .= "                                  AND SCH_INFO.SEMESTER  = CSTD.SEMESTER ";
        $query .= "                                  AND C_INFO.CHAIRCD     = CSTD.CHAIRCD ";
        if ($model->field["CHAIRCD"] != 'all') {
            $query .= "          AND CSTD.CHAIRCD = '".$model->field["CHAIRCD"]."' ";
        }
        $query .= "          AND SCH_INFO.SCHREGNO     = CSTD.SCHREGNO ";
        if ($model->field["MONTH"]) {
            $query .= "          AND ".intval($model->field["MONTH"])." + CASE WHEN ".intval($model->field["MONTH"])." < 4 THEN 12 ELSE 0 END ";
            $query .= "          BETWEEN ";
            $query .= "              MONTH(CSTD.APPDATE) + CASE WHEN MONTH(CSTD.APPDATE) < 4 THEN 12 ELSE 0 END  ";
            $query .= "              AND ";
            $query .= "              MONTH(CSTD.APPENDDATE) + CASE WHEN MONTH(CSTD.APPENDDATE) < 4 THEN 12 ELSE 0 END ";
        }
        $query .= "     LEFT JOIN SUM_ATTEND_SEMES SUMAD ";
        $query .= "          ON SUMAD.SCHREGNO  = SCH_INFO.SCHREGNO ";
        $query .= " ORDER BY ";
        $query .= "         SCH_INFO.GRADE, ";
        $query .= "         SCH_INFO.HR_CLASS, ";
        $query .= "         SCH_INFO.ATTENDNO, ";
        $query .= "         SCH_INFO.SCHREGNO ";

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
