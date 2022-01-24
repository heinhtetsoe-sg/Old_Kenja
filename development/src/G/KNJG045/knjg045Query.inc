<?php

require_once('for_php7.php');

class knjg045Query extends Query
{

    //校種
    public function getA023($model)
    {
        $query .= " SELECT ";
        $query .= "     NAME1 AS VALUE, ";
        $query .= "     ABBV1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND NAMECD1 = 'A023' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= " AND NAME1 IN ('".implode(explode(':', $model->selectSchoolKind), "','")."') ";
            }
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND NAME1 = '".SCHOOLKIND."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     NAMECD2 ";

        return $query;
    }

    //日誌取得
    public function getDiary($model, $flg = "")
    {
        $query  = " SELECT ";
        if ($flg) {
            $query .= "     COUNT(*) AS CNT ";
        } else {
            $query .= "     T1.* ";
        }
        $query .= " FROM ";
        $query .= "     SCHOOL_DIARY_DAT T1 ";
        $query .= " WHERE ";
        if ($flg) {
            $query .= "     T1.DIARY_DATE = '".str_replace('/', '-', $model->setDiaryDate)."' ";
        } else {
            $query .= "     T1.DIARY_DATE = '".str_replace('/', '-', $model->diaryDate)."' ";
        }
        if ($model->Properties["useSchool_KindField"] == "1") {
            $query .= " AND SCHOOLCD    = '".sprintf("%012d", SCHOOLCD)."' ";
            $query .= " AND SCHOOL_KIND = '".$model->field["SCHOOL_KIND"]."' ";
        }
        return $query;
    }

    //天気取得
    public function getWeather($model)
    {
        $query  = " SELECT ";
        $query .= "     NAMECD2 AS VALUE, ";
        $query .= "     NAMECD2 || ':' || NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = 'A006' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //年組取得
    public function getGradeHrClass($model)
    {
        $query  = " SELECT ";
        $query .= "     GRADE || HR_CLASS AS VALUE, ";
        $query .= "     HR_NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_HDAT T1 ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."'";
        $query .= " AND SEMESTER = '".CTRL_SEMESTER."' ";
        $query .= " AND EXISTS (SELECT 'X' ";
        $query .= "             FROM ";
        $query .= "                 SCHREG_REGD_GDAT S1 ";
        $query .= "             WHERE ";
        $query .= "                 T1.YEAR         = S1.YEAR AND ";
        $query .= "                 T1.GRADE        = S1.GRADE AND ";
        if ($model->Properties["useSchool_KindField"] == "1") {
            $query .= "                 S1.SCHOOL_KIND = '{$model->field["SCHOOL_KIND"]}') ";
        } else {
            $query .= "                 S1.SCHOOL_KIND IN ('H','J')) ";
        }

        return $query;
    }

    //欠席者、遅参者、早退者、出張者情報または補欠授業、その他補欠情報取得
    public function getStaffData($model, $ghr = "")
    {
        $query .= " SELECT ";
        $query .= "     T1.*, ";
        $query .= "     L2.STAFFNAME_SHOW ";
        $query .= " FROM ";
        $query .= "     SCHOOL_DIARY_DETAIL_DAT T1 ";
        $query .= "     LEFT JOIN V_STAFF_MST L2 ON L2.STAFFCD = T1.STAFFCD ";
        $query .= " WHERE ";
        $query .= "     L2.YEAR = '".CTRL_YEAR."' ";
        $query .= " AND T1.DIARY_DATE = '".str_replace('/', '-', $model->diaryDate)."' ";
        if ($ghr != "") {
            $query .= " AND T1.GRADE || HR_CLASS = '".$ghr."' ";
        } else {
            $query .= " AND T1.GRADE || HR_CLASS = '00000' ";
        }
        if ($model->Properties["useSchool_KindField"] == "1") {
            $query .= " AND T1.SCHOOLCD    = '".sprintf("%012d", SCHOOLCD)."' ";
            $query .= " AND T1.SCHOOL_KIND = '".$model->field["SCHOOL_KIND"]."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     T1.STAFF_DIV, ";
        $query .= "     T1.STAFFCD ";

        return $query;
    }

    //職員取得(選択済み)
    public function getSelectSubQuery($model, $setDiaryDate, $grade_hr_class = "")
    {
        $query  = "SELECT ";
        if ($model->cmd == 'etc_hoketsu' || $model->cmd == 'hoketsu') {
            $query .= "    L1.COUNT, ";
        }
        $query .= "    CASE WHEN L1.STAFFCD IS NOT NULL THEN '1' ELSE '' END AS CHECK, ";
        $query .= "    T1.STAFFCD, ";
        $query .= "    T1.STAFFNAME_SHOW ";
        $query .= "FROM ";
        $query .= "    V_STAFF_MST T1 ";
        $query .= "    LEFT JOIN SCHOOL_DIARY_DETAIL_DAT L1 ON L1.STAFFCD = T1.STAFFCD ";
        $query .= "                                        AND L1.DIARY_DATE = '".str_replace('/', '-', $setDiaryDate)."' ";
        if ($model->cmd == 'kesseki') {
            $query .= "                             AND L1.STAFF_DIV = '1' ";
            $query .= "                             AND L1.GRADE || HR_CLASS = '00000' ";
        } elseif ($model->cmd == 'chikoku') {
            $query .= "                             AND L1.STAFF_DIV = '2' ";
            $query .= "                             AND L1.GRADE || HR_CLASS = '00000' ";
        } elseif ($model->cmd == 'soutai') {
            $query .= "                             AND L1.STAFF_DIV = '3' ";
            $query .= "                             AND L1.GRADE || HR_CLASS = '00000' ";
        } elseif ($model->cmd == 'shuchou') {
            $query .= "                             AND L1.STAFF_DIV = '4' ";
            $query .= "                             AND L1.GRADE || HR_CLASS = '00000' ";
        } elseif ($model->cmd == 'hoketsu') {
            $query .= "                             AND L1.STAFF_DIV = '5' ";
            $query .= "                             AND L1.GRADE || HR_CLASS = '".$grade_hr_class."' ";
        } elseif ($model->cmd == 'etc_hoketsu') {
            $query .= "                             AND L1.STAFF_DIV = '6' ";
            $query .= "                             AND L1.GRADE || HR_CLASS = '00000' ";
        }
        if ($model->Properties["useSchool_KindField"] == "1") {
            $query .= "                             AND L1.SCHOOLCD     = '".sprintf("%012d", SCHOOLCD)."' ";
            $query .= "                             AND L1.SCHOOL_KIND  = '".$model->field["SCHOOL_KIND"]."' ";
        }
        $query .= " WHERE ";
        $query .= "    T1.YEAR = '".CTRL_YEAR."' ";
        $query .= " ORDER BY ";
        $query .= "    CASE WHEN L1.STAFFCD IS NOT NULL ";
        $query .= "         THEN 0 ";
        $query .= "         ELSE 1 ";
        $query .= "    END, ";
        $query .= "    T1.STAFFCD, ";
        $query .= "    T1.SECTIONCD ";
        return $query;
    }

    //学年取得
    public function getGrade($model, $year)
    {
        $query  = " SELECT ";
        $query .= "     GRADE AS VALUE, ";
        $query .= "     GRADE_NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_GDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$year."' ";
        if ($model->Properties["useSchool_KindField"] == "1") {
            $query .= " AND SCHOOL_KIND = '".$model->field["SCHOOL_KIND"]."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //出欠データ取得
    public function getNurseoffAttendDat($model, $date)
    {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     NURSEOFF_ATTEND_DAT ";
        $query .= " WHERE ";
        $query .= "     DATE = '".$date."' ";
        if ($model->Properties["useSchool_KindField"] == "1") {
            $query .= " AND SCHOOLCD    = '".sprintf("%012d", SCHOOLCD)."' ";
            $query .= " AND SCHOOL_KIND = '".$model->field["SCHOOL_KIND"]."' ";
        }

        return $query;
    }

    /**
     * INSERT
     */
    public function &getInsertQuery($model)
    {
        $db = Query::dbCheckOut();

        $db->autoCommit(false);

        //SCHOOL_DIARY_DAT
        $data = array();
        if ($model->Properties["useSchool_KindField"] == "1") {
            $data["SCHOOLCD"][TEXT]                     = sprintf("%012d", SCHOOLCD);
            $data["SCHOOL_KIND"][TEXT]                  = $model->field["SCHOOL_KIND"];
        }
        $data["DIARY_DATE"][DATE]                   = $model->diaryDate;
        $data["WEATHER"][TEXT]                      = $model->field["WEATHER"];
        $data["WEATHER2"][TEXT]                     = $model->field["WEATHER2"];
        $data["NEWS"][TEXT]                         = $model->field["NEWS"];
        $data["REGISTERCD"][TEXT]                   = STAFFCD;
        $data["UPDATED"][FUNC]                      = "SYSDATE()";
        $query = Query::insertSQL($data, "SCHOOL_DIARY_DAT");
        $db->query($query);

        if ($model->Properties["knjg045updateWeatherJH"] == "1") {
            knjg045Query::updateWeather($db, $model, "insert");
        }

        //NURSEOFF_ATTEND_DAT
        if ($model->Properties["useNurseoffAttend"] == "1") {
            //欠席状況更新
            foreach ($model->di_cd as $dkey => $dval) {
                foreach ($model->grade as $gkey => $gval) {
                    $data = array();
                    if ($model->Properties["useSchool_KindField"] == "1") {
                        $data["SCHOOLCD"][TEXT]     = sprintf("%012d", SCHOOLCD);
                        $data["SCHOOL_KIND"][TEXT]  = $model->field["SCHOOL_KIND"];
                    }
                    $data["DATE"][DATE]         = $model->diaryDate;
                    $data["GRADE"][TEXT]        = $gkey;
                    $data["DI_CD"][TEXT]        = $dkey;
                    $data["CNT"][NUMBER]        = $model->att_field[$dkey][$gkey];
                    $data["REGISTERCD"][TEXT]   = STAFFCD;
                    $data["UPDATED"][NUMBER]    = "sysdate()";

                    $query = Query::insertSQL($data, "NURSEOFF_ATTEND_DAT");
                    $db->query($query);
                }
            }
        }

        $db->commit(); // トランザクションをコミットする。
        Query::dbCheckIn($db);

        return ;
    }

    public function getShowWeather($model)
    {
        $query  = "SELECT ";
        $query .= "     SCHOOL_KIND ";
        $query .= "   , WEATHER ";
        $query .= "   , WEATHER2 ";
        $query .= " FROM SCHOOL_DIARY_DAT ";
        $query .= " WHERE ";
        $query .= "     DIARY_DATE = '".str_replace('/', '-', $model->diaryDate)."' ";
        $query .= "     AND (WEATHER IS NOT NULL OR WEATHER2 IS NOT NULL) ";
        $query .= " ORDER BY ";
        $query .= "     CASE WHEN SCHOOL_KIND = '".$model->field["SCHOOL_KIND"]."' THEN 1 ELSE 2 END ";
        return $query;
    }

    public function &updateWeather($db, $model, $flg)
    {
        foreach (array("J", "H") as $schoolKind) {
            if ($model->field["SCHOOL_KIND"] == $schoolKind) {
                continue;
            }
            //SCHOOL_DIARY_DAT
            $query  = "SELECT ";
            $query .= "   * ";
            $query .= " FROM SCHOOL_DIARY_DAT ";
            $query .= " WHERE ";
            $query .= "     DIARY_DATE = '".str_replace('/', '-', $model->diaryDate)."' ";
            if ($model->Properties["useSchool_KindField"] == "1") {
                $query .= " AND SCHOOLCD    = '".sprintf("%012d", SCHOOLCD)."' ";
                $query .= " AND SCHOOL_KIND = '".$schoolKind."' ";
            }
            $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
            if ($flg == "insert") {
                if (is_array($row)) {
                    $data = array();
                    $data["WEATHER"][TEXT]                      = $model->field["WEATHER"];
                    $data["WEATHER2"][TEXT]                     = $model->field["WEATHER2"];
                    //$data["REGISTERCD"][TEXT]                   = STAFFCD;
                    //$data["UPDATED"][FUNC]    = "SYSDATE()";
                    $where  = "WHERE DIARY_DATE ='".str_replace('/', '-', $model->diaryDate)."'";
                    $where .= " AND SCHOOLCD    = '".sprintf("%012d", SCHOOLCD)."' ";
                    $where .= " AND SCHOOL_KIND = '".$schoolKind."' ";
                    $query = Query::updateSQL($data, "SCHOOL_DIARY_DAT", $where);
                    $db->query($query);
                } else {
                    $data = array();
                    $data["SCHOOLCD"][TEXT]                     = sprintf("%012d", SCHOOLCD);
                    $data["SCHOOL_KIND"][TEXT]                  = $schoolKind;
                    $data["DIARY_DATE"][DATE]                   = $model->diaryDate;
                    $data["WEATHER"][TEXT]                      = $model->field["WEATHER"];
                    $data["WEATHER2"][TEXT]                     = $model->field["WEATHER2"];
                    //$data["REGISTERCD"][TEXT]                   = STAFFCD;
                    //$data["UPDATED"][FUNC]                      = "SYSDATE()";
                    $query = Query::insertSQL($data, "SCHOOL_DIARY_DAT");
                    $db->query($query);
                }
            } elseif ($flg == "delete") {
                if (is_array($row)) {
                    if ($row["NEWS"] || $row["STAFFNEWS"]) {
                        $data = array();
                        $data["WEATHER"][TEXT]                      = "";
                        $data["WEATHER2"][TEXT]                     = "";
                        //$data["REGISTERCD"][TEXT]                   = STAFFCD;
                        //$data["UPDATED"][FUNC]    = "SYSDATE()";
                        $where  = "WHERE DIARY_DATE ='".str_replace('/', '-', $model->diaryDate)."'";
                        $where .= " AND SCHOOLCD    = '".sprintf("%012d", SCHOOLCD)."' ";
                        $where .= " AND SCHOOL_KIND = '".$schoolKind."' ";
                        $query = Query::updateSQL($data, "SCHOOL_DIARY_DAT", $where);
                        $db->query($query);
                    } else {
                        $query  = "DELETE ";
                        $query .= " FROM SCHOOL_DIARY_DAT ";
                        $query .= " WHERE ";
                        $query .= "     DIARY_DATE = '".str_replace('/', '-', $model->diaryDate)."' ";
                        $query .= " AND SCHOOLCD    = '".sprintf("%012d", SCHOOLCD)."' ";
                        $query .= " AND SCHOOL_KIND = '".$schoolKind."' ";
                        $db->query($query);
                    }
                }
            }
        }
    }

    public function &getDeleteQuery($model, $flg = "")
    {
        $db = Query::dbCheckOut();

        $db->autoCommit(false);

        //SCHOOL_DIARY_DAT
        $query  = "DELETE ";
        $query .= " FROM SCHOOL_DIARY_DAT ";
        $query .= " WHERE ";
        $query .= "     DIARY_DATE = '".str_replace('/', '-', $model->diaryDate)."' ";
        if ($model->Properties["useSchool_KindField"] == "1") {
            $query .= " AND SCHOOLCD    = '".sprintf("%012d", SCHOOLCD)."' ";
            $query .= " AND SCHOOL_KIND = '".$model->field["SCHOOL_KIND"]."' ";
        }
        $db->query($query);

        if ($model->Properties["knjg045updateWeatherJH"] == "1") {
            knjg045Query::updateWeather($db, $model, "delete");
        }

        if ($flg == "delete") {
            //SCHOOL_DIARY_DETAIL_DAT
            $query  = "DELETE ";
            $query .= " FROM SCHOOL_DIARY_DETAIL_DAT ";
            $query .= " WHERE ";
            $query .= "     DIARY_DATE = '".str_replace('/', '-', $model->diaryDate)."' ";
            if ($model->Properties["useSchool_KindField"] == "1") {
                $query .= " AND SCHOOLCD    = '".sprintf("%012d", SCHOOLCD)."' ";
                $query .= " AND SCHOOL_KIND = '".$model->field["SCHOOL_KIND"]."' ";
            }
            $db->query($query);
        }

        //NURSEOFF_ATTEND_DAT
        if ($model->Properties["useNurseoffAttend"] == "1") {
            //欠席状況
            $query  = " DELETE FROM ";
            $query .= "     NURSEOFF_ATTEND_DAT ";
            $query .= " WHERE ";
            $query .= "     DATE = '".str_replace("/", "-", $model->diaryDate)."' ";
            if ($model->Properties["useSchool_KindField"] == "1") {
                $query .= " AND SCHOOLCD    = '".sprintf("%012d", SCHOOLCD)."' ";
                $query .= " AND SCHOOL_KIND = '".$model->field["SCHOOL_KIND"]."' ";
            }
            $db->query($query);
        }

        $db->commit(); // トランザクションをコミットする。
        Query::dbCheckIn($db);

        return ;
    }

    //更新
    public function getUpdateDetail($model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //メインデータのデータ有無
        $query = knjg045Query::getDiary($model, "count");
        $diaryData = $db->getOne($query);
        if ($diaryData == 0) {
            //SCHOOL_DIARY_DAT
            $data = array();
            if ($model->Properties["useSchool_KindField"] == "1") {
                $data["SCHOOLCD"][DATE]                     = sprintf("%012d", SCHOOLCD);
                $data["SCHOOL_KIND"][DATE]                  = $model->field["SCHOOL_KIND"];
            }
            $data["DIARY_DATE"][DATE]                   = $model->setDiaryDate;
            $data["REGISTERCD"][TEXT]                   = STAFFCD;
            $data["UPDATED"][FUNC]                      = "SYSDATE()";
            $query = Query::insertSQL($data, "SCHOOL_DIARY_DAT");
            $db->query($query);
        }

        $query  = "DELETE ";
        $query .= " FROM SCHOOL_DIARY_DETAIL_DAT ";
        $query .= " WHERE ";
        $query .= "     DIARY_DATE = '".str_replace('/', '-', $model->setDiaryDate)."' ";
        $query .= " AND STAFF_DIV = '".$model->setStaffDiv."' ";
        if ($model->setStaffDiv === '5') {
            $query .= " AND GRADE || HR_CLASS = '{$model->setGradeHrClass}' ";
        } else {
            $query .= " AND GRADE || HR_CLASS = '00000' ";
        }
        if ($model->Properties["useSchool_KindField"] == "1") {
            $query .= " AND SCHOOLCD    = '".sprintf("%012d", SCHOOLCD)."' ";
            $query .= " AND SCHOOL_KIND = '".$model->field["SCHOOL_KIND"]."' ";
        }
        $db->query($query);

        for ($i = 0; $i < $model->setcounter; $i++) {
            //更新チェックＯＮのみ処理する
            if ($model->fields["CHECK"][$i] != "1") {
                continue;
            }

            $setCount = "0";
            $data = array();
            if ($model->Properties["useSchool_KindField"] == "1") {
                $data["SCHOOLCD"][DATE]                     = sprintf("%012d", SCHOOLCD);
                $data["SCHOOL_KIND"][DATE]                  = $model->field["SCHOOL_KIND"];
            }
            $data["DIARY_DATE"][DATE]                   = $model->setDiaryDate;
            $data["STAFF_DIV"][TEXT]                    = $model->setStaffDiv;
            if ($model->setStaffDiv === '5') {
                $data["GRADE"][TEXT]                        = substr($model->setGradeHrClass, 0, 2);
                $data["HR_CLASS"][TEXT]                     = substr($model->setGradeHrClass, 2, 3);
            } else {
                $data["GRADE"][TEXT]                        = '00';
                $data["HR_CLASS"][TEXT]                     = '000';
            }
            $data["STAFFCD"][TEXT]                      = $model->fields["STAFFCD"][$i];
            if ($model->setStaffDiv === '5' || $model->setStaffDiv === '6') {
                if ($model->fields["COUNT_CHECK1"][$i]) {
                    $setCount++;
                }
                if ($model->fields["COUNT_CHECK2"][$i]) {
                    $setCount++;
                }
                if ($model->fields["COUNT_CHECK3"][$i]) {
                    $setCount++;
                }
                if ($model->setStaffDiv === '6') {
                    if ($model->fields["COUNT_CHECK4"][$i]) {
                        $setCount++;
                    }
                    if ($model->fields["COUNT_CHECK5"][$i]) {
                        $setCount++;
                    }
                }
                $data["COUNT"][NUMBER]                  = $setCount;
            }
            $data["REGISTERCD"][TEXT]                   = STAFFCD;
            $data["UPDATED"][FUNC]                      = "SYSDATE()";

            $query = Query::insertSQL($data, "SCHOOL_DIARY_DETAIL_DAT");
            $db->query($query);
        }//for

        $db->commit();
        Query::dbCheckIn($db);
        return;
    }
}
