<?php

require_once('for_php7.php');
class knjx_c033kquery extends Query
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
        $query .= "     SCHREG_REGD_HDAT T1 ";
        $query .= "     INNER JOIN SEMESTER_MST T2 ON T1.YEAR = T2.YEAR AND T1.SEMESTER = T2.SEMESTER ";
        if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= "     INNER JOIN SCHREG_REGD_GDAT G1 ";
            $query .= "          ON G1.YEAR         = T1.YEAR ";
            $query .= "         AND G1.GRADE        = T1.GRADE ";
            $query .= "         AND G1.SCHOOL_KIND  = '".SCHOOLKIND."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //科目一覧取得
    public function getSubclassList($model)
    {
        $query  = " SELECT ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     T3.CLASSCD || '-' || T3.SCHOOL_KIND || '-' || T3.CURRICULUM_CD || '-' || T3.SUBCLASSCD AS VALUE, ";
            $query .= "     T3.CLASSCD || '-' || T3.SCHOOL_KIND || '-' || T3.CURRICULUM_CD || '-' || T3.SUBCLASSCD || ' ' || VALUE(T3.SUBCLASSABBV, '') AS LABEL ";
        } else {
            $query .= "     T3.SUBCLASSCD AS VALUE, ";
            $query .= "     T3.SUBCLASSCD || ' ' || VALUE(T3.SUBCLASSABBV, '') AS LABEL ";
        }
        $query .= " FROM ";
        $query .= "     CHAIR_DAT T1, ";
        $query .= "     CHAIR_STF_DAT T2, ";
        $query .= "     SUBCLASS_MST T3 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = T2.YEAR AND ";
        $query .= "     T1.SEMESTER = T2.SEMESTER AND ";
        $query .= "     T1.YEAR || T1.SEMESTER = '".$model->field["YEAR"]."' AND ";
        $query .= "     T1.CHAIRCD = T2.CHAIRCD AND ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
                $query .= " T1.SCHOOL_KIND   = '".SCHOOLKIND."' AND ";
            }
            $query .= "     T1.CLASSCD       = T3.CLASSCD AND ";
            $query .= "     T1.SCHOOL_KIND   = T3.SCHOOL_KIND AND ";
            $query .= "     T1.CURRICULUM_CD = T3.CURRICULUM_CD AND ";
        }
        $query .= "     T1.SUBCLASSCD = T3.SUBCLASSCD ";
        $query .= " GROUP BY ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     T3.CLASSCD, ";
            $query .= "     T3.SCHOOL_KIND, ";
            $query .= "     T3.CURRICULUM_CD, ";
        }
        $query .= "     T3.SUBCLASSCD, ";
        $query .= "     T3.SUBCLASSABBV ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //講座一覧取得
    public function getChairList($model)
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     T1.CHAIRCD AS VALUE, ";
        $query .= "     T1.CHAIRCD || ' ' || VALUE(T1.CHAIRNAME, '') AS LABEL ";
        $query .= " FROM ";
        $query .= "     CHAIR_DAT T1, ";
        $query .= "     CHAIR_STF_DAT T2 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = T2.YEAR AND ";
        $query .= "     T1.SEMESTER = T2.SEMESTER AND ";
        $query .= "     T1.YEAR || T1.SEMESTER = '".$model->field["YEAR"]."' AND ";
        $query .= "     T1.CHAIRCD = T2.CHAIRCD AND ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD = '".$model->field["SUBCLASSCD"]."' ";
        } else {
            $query .= "     T1.SUBCLASSCD = '".$model->field["SUBCLASSCD"]."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //欠課種別取得
    public function getSickDiv($year, $namecd2 = "")
    {
        $query  = " SELECT ";
        $query .= "     T1.NAMECD2 AS VALUE, ";
        $query .= "     T1.NAMECD2 || ' ' || L1.NAME1 AS LABEL, ";
        $query .= "     T1.NAMESPARE1 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST T1 ";
        $query .= " INNER JOIN ";
        $query .= "     V_NAME_MST L1 ON  L1.YEAR    = T1.YEAR ";
        $query .= "                   AND L1.NAMECD1 = 'C001' ";
        $query .= "                   AND L1.NAMECD2 = T1.NAMECD2 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".$year."' AND ";
        $query .= "     T1.NAMECD1 = 'C041' AND ";
        $query .= "     T1.NAMECD2 IN ('1', '2', '3', '4', '5', '6', '15', '16', '19', '25') ";
        if ($namecd2) {
            $query .= " AND T1.NAMECD2 = '".$namecd2."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     INT(VALUE) ";

        return $query;
    }

    //学期取得
    public function selectSemesAll($year, $semester = "")
    {
        $query  = " SELECT ";
        $query .= "     SEMESTER, ";
        $query .= "     SEMESTERNAME, ";
        $query .= "     CASE WHEN MONTH(SDATE) < 4 ";
        $query .= "          THEN MONTH(SDATE) + 12 ";
        $query .= "          ELSE MONTH(SDATE) END AS S_MONTH, ";
        $query .= "     CASE WHEN MONTH(EDATE) < 4 ";
        $query .= "          THEN MONTH(EDATE) + 12 ";
        $query .= "          ELSE MONTH(EDATE) END AS E_MONTH ";
        $query .= " FROM ";
        $query .= "     SEMESTER_MST ";
        $query .= " WHERE ";
        if ($semester == "") {
            $query .= "     YEAR || SEMESTER = '".$year."' ";
        } else {
            $query .= "     YEAR = '".$year."' AND ";
            $query .= "     SEMESTER = '".$semester."' ";
        }
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
        $query .= "     NAMECD2, NAME1, NAMESPARE1 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR  = '".$year."' ";
        $query .= "     AND NAMECD1 = '{$setNameCd}' ";
        $query .= "     AND NAMECD2 IN (SELECT CONTROL_CODE FROM ADMIN_CONTROL_DAT WHERE YEAR = '".$year."' AND ";
        if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= "                     SCHOOL_KIND = '".SCHOOLKIND."' AND ";
        }
        $query .= "     CONTROL_FLG = '2') ";
        $query .= "     AND NAMECD2 = '".sprintf('%02d', $month)."' ";
        $query .= " ORDER BY ";
        $query .= "     NAMESPARE1 ";

        return $query;
    }

    //マスタの追加（ＣＳＶデータより読込）
    public function insertQueryCsv($model, &$data_arr)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $cnt = 0;   //処理件数
        for ($i = 0; $i < get_count($data_arr); $i++) {
            $appointed_day = $db->getOne(knjx_c033kQuery::getAppointedDay($data_arr[$i]["YEAR"], $data_arr[$i]["MONTH"], $data_arr[$i]["SEMESTER"]));
            $sick_div = $model->sickdiv[$data_arr[$i]["SICK_DIV"]];

            //データ有無チェック
            $check  = " SELECT COUNT(*) FROM ATTEND_SUBCLASS_DAT ";
            $check .= " WHERE COPYCD        = '0' AND ";
            $check .= "       YEAR          = '".$data_arr[$i]["YEAR"]."' AND ";
            $check .= "       MONTH         = '".$data_arr[$i]["MONTH"]."' AND ";
            $check .= "       SEMESTER      = '".$data_arr[$i]["SEMESTER"]."' AND ";
            $check .= "       SCHREGNO      = '".$data_arr[$i]["SCHREGNO"]."' AND ";
            if ($model->Properties["useCurriculumcd"] == '1') {
                $check .= "       CLASSCD       = '".$data_arr[$i]["CLASSCD"]."' AND ";
                $check .= "       SCHOOL_KIND   = '".$data_arr[$i]["SCHOOL_KIND"]."' AND ";
                $check .= "       CURRICULUM_CD = '".$data_arr[$i]["CURRICULUM_CD"]."' AND ";
            } else {
                $check .= "       CLASSCD       = '".substr($data_arr[$i]["SUBCLASSCD"], 0, 2)."' AND ";
            }
            $check .= "       SUBCLASSCD    = '".$data_arr[$i]["SUBCLASSCD"]."' ";

            //データセット
            $data = array();
            $data["COPYCD"][TEXT]           = '0';
            $data["YEAR"][TEXT]             = $data_arr[$i]["YEAR"];
            $data["MONTH"][TEXT]            = $data_arr[$i]["MONTH"];
            $data["SEMESTER"][TEXT]         = $data_arr[$i]["SEMESTER"];
            $data["SCHREGNO"][TEXT]         = $data_arr[$i]["SCHREGNO"];
            if ($model->Properties["useCurriculumcd"] == '1') {
                $data["CLASSCD"][TEXT]          = $data_arr[$i]["CLASSCD"];
                $data["SCHOOL_KIND"][TEXT]      = $data_arr[$i]["SCHOOL_KIND"];
                $data["CURRICULUM_CD"][TEXT]    = $data_arr[$i]["CURRICULUM_CD"];
            } else {
                $data["CLASSCD"][TEXT]          = substr($data_arr[$i]["SUBCLASSCD"], 0, 2);
            }
            $data["SUBCLASSCD"][TEXT]       = $data_arr[$i]["SUBCLASSCD"];
            $data["APPOINTED_DAY"][TEXT]    = $appointed_day;
            $data["LESSON"][NUMBER]         = $data_arr[$i]["LESSON"];

            $query  = knjx_c033kQuery::getFieldList("ATTEND_SUBCLASS_DAT");
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                if ($model->Properties["use_Attend_zero_hyoji"] == '1') {
                    if ($sick_div == $row["NAME"]) {
                        $data[$sick_div][NUMBER] = $data_arr[$i]["SICK_DATA"];
                    }
                } else {
                    if ($sick_div == $row["NAME"]) {
                        $data[$sick_div][NUMBER] = ($data_arr[$i]["SICK_DATA"]) ? $data_arr[$i]["SICK_DATA"] : 0;
                    } elseif (1 > $db->getOne($check)) {
                        $data[$row["NAME"]][NUMBER] = 0;
                    }
                }
            }
            $result->free();

            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][NUMBER]        = "SYSDATE()";

            if (1 > $db->getOne($check)) {
                $query = Query::insertSQL($data, "ATTEND_SUBCLASS_DAT");
            } else {
                $where  = " WHERE ";
                $where .= "     YEAR        = '".$data_arr[$i]["YEAR"]."' AND ";
                $where .= "     MONTH       = '".$data_arr[$i]["MONTH"]."' AND ";
                $where .= "     SEMESTER    = '".$data_arr[$i]["SEMESTER"]."' AND ";
                $where .= "     SCHREGNO    = '".$data_arr[$i]["SCHREGNO"]."' AND ";
                if ($model->Properties["useCurriculumcd"] == '1') {
                    $where .= "     CLASSCD         = '".$data_arr[$i]["CLASSCD"]."' AND ";
                    $where .= "     SCHOOL_KIND     = '".$data_arr[$i]["SCHOOL_KIND"]."' AND ";
                    $where .= "     CURRICULUM_CD   = '".$data_arr[$i]["CURRICULUM_CD"]."' AND ";
                } else {
                    $where .= "     CLASSCD     = '".substr($data_arr[$i]["SUBCLASSCD"], 0, 2)."' AND ";
                }
                $where .= "     SUBCLASSCD      = '".$data_arr[$i]["SUBCLASSCD"]."' AND ";
                $where .= "     COPYCD      = '0' ";

                $query = Query::updateSQL($data, "ATTEND_SUBCLASS_DAT", $where);
            }
            $db->query($query);
            $cnt++;

            //ATTEND_SEMES_DATチェック
            $query  = " SELECT ";
            $query .= "     COUNT(*) ";
            $query .= " FROM ";
            $query .= "     ATTEND_SEMES_DAT ";
            $query .= " WHERE ";
            $query .= "     COPYCD        = '0' AND ";
            $query .= "     YEAR          = '".$data_arr[$i]["YEAR"]."' AND ";
            $query .= "     MONTH         = '".$data_arr[$i]["MONTH"]."' AND ";
            $query .= "     SEMESTER      = '".$data_arr[$i]["SEMESTER"]."' AND ";
            $query .= "     SCHREGNO      = '".$data_arr[$i]["SCHREGNO"]."' ";

            $semes_check = $db->getOne($query);
            $data = array();

            //ATTEND_SEMES_DAT追加
            if (!$semes_check) {
                $data["COPYCD"][TEXT]           = '0';
                $data["YEAR"][TEXT]             = $data_arr[$i]["YEAR"];
                $data["MONTH"][TEXT]            = $data_arr[$i]["MONTH"];
                $data["SEMESTER"][TEXT]         = $data_arr[$i]["SEMESTER"];
                $data["SCHREGNO"][TEXT]         = $data_arr[$i]["SCHREGNO"];
                $data["APPOINTED_DAY"][TEXT]    = $appointed_day;
                $data["LESSON"][NUMBER]         = 0;
                if ($model->Properties["use_Attend_zero_hyoji"] != '1') {
                    $data["OFFDAYS"][NUMBER]        = 0;
                    $data["ABSENT"][NUMBER]         = 0;
                    $data["SUSPEND"][NUMBER]        = 0;
                    if ($model->Properties["useKekkaJisu"] == "true") {
                        $data["KEKKA_JISU"][NUMBER] = 0; //使用しない
                    }
                    if ($model->Properties["useKekka"] == "true") {
                        $data["KEKKA"][NUMBER]      = 0; //使用しない
                    }
                    if ($model->Properties["useLatedetail"] == "true") {
                        $data["LATEDETAIL"][NUMBER] = 0; //使用しない
                    }
                    if ($model->Properties["useKoudome"] == "true") {
                        $data["KOUDOME"][NUMBER]      = 0;
                    }
                    if ($model->Properties["useVirus"] == "true") {
                        $data["VIRUS"][NUMBER]        = 0;
                    }
                    $data["MOURNING"][NUMBER]       = 0;
                    $data["ABROAD"][NUMBER]         = 0;
                    $data["SICK"][NUMBER]           = 0;
                    $data["NOTICE"][NUMBER]         = 0;
                    $data["NONOTICE"][NUMBER]       = 0;
                    $data["LATE"][NUMBER]           = 0;
                    $data["EARLY"][NUMBER]          = 0;
                }
                $data["REGISTERCD"][TEXT]       = STAFFCD;
                $data["UPDATED"][NUMBER]        = "SYSDATE()";

                $query = Query::insertSQL($data, "ATTEND_SEMES_DAT");
                $db->query($query);
            }
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
            $query .= "     YEAR        = '".$data_arr[$i]["YEAR"]."' AND ";
            $query .= "     MONTH       = '".$data_arr[$i]["MONTH"]."' AND ";
            $query .= "     SEMESTER    = '".$data_arr[$i]["SEMESTER"]."' AND ";
            $query .= "     SCHREGNO    = '".$data_arr[$i]["SCHREGNO"]."' AND ";
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "     CLASSCD         = '".$data_arr[$i]["CLASSCD"]."' AND ";
                $query .= "     SCHOOL_KIND     = '".$data_arr[$i]["SCHOOL_KIND"]."' AND ";
                $query .= "     CURRICULUM_CD   = '".$data_arr[$i]["CURRICULUM_CD"]."' AND ";
            } else {
                $query .= "     CLASSCD     = '".substr($data_arr[$i]["SUBCLASSCD"], 0, 2)."' AND ";
            }
            $query .= "     SUBCLASSCD      = '".$data_arr[$i]["SUBCLASSCD"]."' AND ";
            $query .= "     COPYCD      = '0' ";

            $db->query($query);
            $cnt++;
        }
        $db->commit();
        Query::dbCheckIn($db);

        return $cnt;
    }

    //存在チェック（在籍）
    public function checkRegdDat($model, $year, $semester, $schregno)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1 ";
        $query .= " WHERE ";
        $query .= "     YEAR     = '".$year."' AND ";
        $query .= "     SEMESTER = '".$semester."' AND ";
        $query .= "     SCHREGNO = '".$schregno."' ";
        if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND GRADE IN (  SELECT ";
            $query .= "                     G1.GRADE ";
            $query .= "                 FROM ";
            $query .= "                     SCHREG_REGD_GDAT G1 ";
            $query .= "                 WHERE ";
            $query .= "                     G1.YEAR         = T1.YEAR AND ";
            $query .= "                     G1.SCHOOL_KIND  = '".SCHOOLKIND."' ";
            $query .= "                 ) ";
        }

        return $query;
    }

    //存在チェック（講座）
    public function checkChairDat($year, $semester, $schregno, $subclasscd, $chaircd, $model)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     CHAIR_STD_DAT T1, ";
        $query .= "     CHAIR_DAT T2 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR     = T2.YEAR AND ";
        $query .= "     T1.YEAR     = '".$year."' AND ";
        $query .= "     T1.SEMESTER = T2.SEMESTER AND ";
        $query .= "     T2.SEMESTER = '".$semester."' AND ";
        $query .= "     T1.SCHREGNO = '".$schregno."' AND ";
        $query .= "     T1.CHAIRCD  = T2.CHAIRCD AND ";
        $query .= "     T1.CHAIRCD  = '".$chaircd."' AND ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     T2.CLASSCD          = '".$subclasscd[0]."' AND ";
            $query .= "     T2.SCHOOL_KIND      = '".$subclasscd[1]."' AND ";
            $query .= "     T2.CURRICULUM_CD    = '".$subclasscd[2]."' AND ";
            $query .= "     T2.SUBCLASSCD       = '".$subclasscd[3]."' ";
        } else {
            $query .= "     T2.SUBCLASSCD = '".$subclasscd."' ";
        }

        return $query;
    }

    //締め日の取得
    public function getAppointedDay($year, $month, $semester)
    {
        $query  = " SELECT ";
        $query .= "     APPOINTED_DAY ";
        $query .= " FROM ";
        $query .= "     APPOINTED_DAY_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '". $year ."' AND ";
        $query .= "     MONTH = '".$month."' AND ";
        $query .= "     SEMESTER = '".$semester."' ";

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

    //出欠累積データ取得
    public function getAttendSubclassDat($year, $semester, $month, $schregno, $subclasscd, $field, $model)
    {
        $query  = " SELECT ";
        $query .= "     LESSON, ";
        $query .= "     ".$field." AS SICK_DATA ";
        $query .= " FROM ";
        $query .= "     ATTEND_SUBCLASS_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR        = '".$year."' AND ";
        $query .= "     SEMESTER    = '".$semester."' AND ";
        $query .= "     MONTH       = '".$month."' AND ";
        $query .= "     SCHREGNO    = '".$schregno."' AND ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     CLASSCD         = '".$subclasscd[0]."' AND ";
            $query .= "     SCHOOL_KIND     = '".$subclasscd[1]."' AND ";
            $query .= "     CURRICULUM_CD   = '".$subclasscd[2]."' AND ";
            $query .= "     SUBCLASSCD      = '".$subclasscd[3]."' ";
        } else {
            $query .= "     SUBCLASSCD  = '".$subclasscd."' ";
        }
        return $query;
    }

    //CSVデータ出力
    public function selectMainQuery($model)
    {
        $query  = " WITH SCHINFO AS ( ";
        $query .= "     SELECT ";
        $query .= "         T1.YEAR, ";
        $query .= "         T1.SEMESTER, ";
        $query .= "         T1.SCHREGNO, ";
        $query .= "         T1.GRADE, ";
        $query .= "         T1.HR_CLASS, ";
        $query .= "         T1.ATTENDNO, ";
        $query .= "         S2.HR_NAME, ";
        $query .= "         S1.NAME ";
        $query .= "     FROM ";
        $query .= "         SCHREG_REGD_DAT T1 ";
        $query .= "     LEFT JOIN SCHREG_BASE_MST S1 ON T1.SCHREGNO = S1.SCHREGNO  ";
        $query .= "     LEFT JOIN SCHREG_REGD_HDAT S2 ON T1.YEAR = S2.YEAR ";
        $query .= "                                  AND T1.SEMESTER = S2.SEMESTER ";
        $query .= "                                  AND T1.GRADE = S2.GRADE ";
        $query .= "                                  AND T1.HR_CLASS = S2.HR_CLASS ";
        if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= "     INNER JOIN SCHREG_REGD_GDAT G1 ";
            $query .= "          ON G1.YEAR         = T1.YEAR ";
            $query .= "         AND G1.GRADE        = T1.GRADE ";
            $query .= "         AND G1.SCHOOL_KIND  = '".SCHOOLKIND."' ";
        }
        $query .= "     WHERE ";
        $query .= "         T1.YEAR || T1.SEMESTER = '".$model->field["YEAR"]."' ";
        $query .= " ), MAIN AS ( ";
        $query .= "     SELECT DISTINCT ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "         T3.CLASSCD , ";
            $query .= "         T3.SCHOOL_KIND, ";
            $query .= "         T3.CURRICULUM_CD, ";
        }
        $query .= "         T3.SUBCLASSCD, ";
        $query .= "         S1.SUBCLASSNAME, ";
        $query .= "         T2.CHAIRCD, ";
        $query .= "         T3.CHAIRNAME, ";
        $query .= "         T1.YEAR, ";
        $query .= "         '' AS MONTH, ";
        $query .= "         T1.SEMESTER, ";
        $query .= "         T1.SCHREGNO, ";
        $query .= "         T1.HR_NAME, ";
        $query .= "         T1.GRADE, ";
        $query .= "         T1.HR_CLASS, ";
        $query .= "         T1.ATTENDNO, ";
        $query .= "         T1.NAME, ";
        $query .= "         '' AS APPOINTED_DAY, ";
        $query .= "         '' AS LESSON, ";
        $query .= "         '' AS SICK_DIV, ";
        $query .= "         '' AS SICK_DATA, ";
        $query .= "         '".$model->lastColumn."' AS ".$model->lastColumn." ";
        $query .= "     FROM ";
        $query .= "         SCHINFO T1, ";
        $query .= "         CHAIR_STD_DAT T2, ";
        $query .= "         CHAIR_DAT T3 ";
        $query .= "         LEFT JOIN SUBCLASS_MST S1 ON T3.SUBCLASSCD = S1.SUBCLASSCD ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "                                  AND T3.CLASSCD         = S1.CLASSCD ";
            $query .= "                                  AND T3.SCHOOL_KIND     = S1.SCHOOL_KIND ";
            $query .= "                                  AND T3.CURRICULUM_CD   = S1.CURRICULUM_CD ";
        }
        $query .= "     WHERE ";
        $query .= "         T1.YEAR = T2.YEAR AND ";
        $query .= "         T1.YEAR = T3.YEAR AND ";
        $query .= "         T1.SEMESTER = T2.SEMESTER AND ";
        $query .= "         T1.SEMESTER = T3.SEMESTER AND ";
        $query .= "         T1.SCHREGNO = T2.SCHREGNO AND ";
        $query .= "         T2.CHAIRCD = T3.CHAIRCD AND ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "         T3.CLASSCD || '-' || T3.SCHOOL_KIND || '-' || T3.CURRICULUM_CD || '-' || T3.SUBCLASSCD = '".$model->field["SUBCLASSCD"]."' AND ";
        } else {
            $query .= "         T3.SUBCLASSCD = '".$model->field["SUBCLASSCD"]."' AND ";
        }
        $query .= "         T3.CHAIRCD = '".$model->field["CHAIRCD"]."' ";
        $query .= " ) ";

        $query .= " SELECT ";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     CLASSCD , ";
            $query .= "     SCHOOL_KIND, ";
            $query .= "     CURRICULUM_CD, ";
        }
        $query .= "     SUBCLASSCD, ";
        $query .= "     SUBCLASSNAME, ";
        $query .= "     CHAIRCD, ";
        $query .= "     CHAIRNAME, ";
        $query .= "     YEAR, ";
        $query .= "     MONTH, ";
        $query .= "     SEMESTER, ";
        $query .= "     SCHREGNO, ";
        $query .= "     HR_NAME, ";
        $query .= "     ATTENDNO, ";
        $query .= "     NAME, ";
        $query .= "     APPOINTED_DAY, ";
        $query .= "     LESSON, ";
        $query .= "     SICK_DIV, ";
        $query .= "     SICK_DATA, ";
        $query .= "     ".$model->lastColumn." ";
        $query .= " FROM ";
        $query .= "     MAIN ";
        $query .= " ORDER BY ";
        $query .= "     GRADE, ";
        $query .= "     HR_CLASS, ";
        $query .= "     ATTENDNO, ";
        $query .= "     SCHREGNO ";

        return $query;
    }

    //学校種別取得
    public function getSchoolKind($model)
    {
        $query  = " SELECT ";
        $query .= "     NAME1, ";
        $query .= "     ABBV1 ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = 'A023' ";
        if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND NAME1 = '".SCHOOLKIND."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     NAME1 ";

        return $query;
    }

    //教育課程コード取得
    public function getCurriculumCd()
    {
        $query  = " SELECT ";
        $query .= "     NAMECD2 ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = 'Z018' ";

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
}
