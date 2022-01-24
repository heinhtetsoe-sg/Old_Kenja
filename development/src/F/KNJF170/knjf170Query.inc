<?php

require_once('for_php7.php');

class knjf170Query extends Query
{
    //年度取得
    public function getYear($model, $year="")
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     YEAR AS VALUE, ";
        $query .= "     YEAR AS LABEL, ";
        $query .= "     SCHOOLNAME2 ";
        $query .= " FROM ";
        $query .= "     SCHOOL_MST ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            $query .= " WHERE ";
            $query .= "     SCHOOLCD    = '".sprintf("%012d", SCHOOLCD)."' ";
            //特別支援学校以外は、校種で絞る
            if ($model->Properties["useSpecial_Support_School"] != "1") {
                $query .= " AND SCHOOL_KIND = '".$model->schoolkind."' ";
            }
            if ($year) {
                $query .= " AND YEAR        = '".$year."' ";
            }
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " WHERE ";
            $query .= "     SCHOOLCD    = '".sprintf("%012d", SCHOOLCD)."' ";
            //特別支援学校以外は、校種で絞る
            if ($model->Properties["useSpecial_Support_School"] != "1") {
                $query .= " AND SCHOOL_KIND = '".SCHOOLKIND."' ";
            }
            if ($year) {
                $query .= " AND YEAR        = '".$year."' ";
            }
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE DESC ";

        return $query;
    }

    //校種取得
    public function getSchkind($model)
    {
        $query  = " SELECT ";
        $query .= "     NAME1 AS VALUE, ";
        $query .= "     ABBV1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "         YEAR    = '".CTRL_YEAR."' ";
        $query .= "     AND NAMECD1 = 'A023' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= "     AND NAME1 IN ('".implode(explode(':', $model->selectSchoolKind), "','")."') ";
            }
        }
        $query .= " ORDER BY ";
        $query .= "     NAME1 ";

        return $query;
    }

    //名称マスタ（Z010）
    public function getZ010()
    {
        $query  = " SELECT ";
        $query .= "     NAME1 ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = 'Z010' AND ";
        $query .= "     NAMECD2 = '00'";
        return $query;
    }

    //リスト取得
    public function getList($model)
    {
        $query  = " WITH ";
        if (($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") || $model->Properties["use_prg_schoolkind"] == "1") {
            $query .= " SCH_INFO AS( ";
            $query .= "     SELECT DISTINCT ";
            $query .= "         T1.SCHREGNO, ";
            $query .= "         T2.SCHOOL_KIND ";
            $query .= "     FROM ";
            $query .= "         GRD_BASE_MST T1, ";
            $query .= "         SCHREG_REGD_GDAT T2 ";
            $query .= "     WHERE ";
            $query .= "         T1.GRD_GRADE    = T2.GRADE AND ";
            $query .= "         T2.YEAR         = '".$model->year."' ";
            $query .= "     UNION ";
            $query .= "     SELECT DISTINCT ";
            $query .= "         T1.SCHREGNO, ";
            $query .= "         T2.SCHOOL_KIND ";
            $query .= "     FROM ";
            $query .= "         SCHREG_REGD_DAT T1, ";
            $query .= "         SCHREG_REGD_GDAT T2 ";
            $query .= "     WHERE ";
            $query .= "         T1.YEAR     = T2.YEAR AND ";
            $query .= "         T1.YEAR     = '".$model->year."' AND ";
            $query .= "         T1.GRADE    = T2.GRADE AND ";
            $query .= "         T1.SCHREGNO NOT IN (SELECT SCHREGNO FROM GRD_BASE_MST) ";
            $query .= " ), ";
        }
        $query .= " DATE AS( ";
        $query .= "     SELECT ";
        $query .= "         DATE ";
        $query .= "     FROM ";
        $query .= "         NURSEOFF_DIARY_DAT ";
        $query .= "     WHERE ";
        $query .= "         DATE BETWEEN '".$model->year."-04-01' AND '".((int)$model->year+1)."-03-31' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            $query .= "     AND SCHOOLCD    = '".sprintf("%012d", SCHOOLCD)."' ";
            //特別支援学校以外は、校種で絞る
            if ($model->Properties["useSpecial_Support_School"] != "1") {
                $query .= "     AND SCHOOL_KIND = '".$model->schoolkind."' ";
            }
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= "     AND SCHOOLCD    = '".sprintf("%012d", SCHOOLCD)."' ";
            //特別支援学校以外は、校種で絞る
            if ($model->Properties["useSpecial_Support_School"] != "1") {
                $query .= "     AND SCHOOL_KIND = '".SCHOOLKIND."' ";
            }
        }
        $query .= "     UNION ";
        $query .= "     SELECT ";
        $query .= "         VISIT_DATE AS DATE ";
        $query .= "     FROM ";
        $query .= "         NURSEOFF_VISITREC_DAT ";
        $query .= "     WHERE ";
        $query .= "         VISIT_DATE BETWEEN '".$model->year."-04-01' AND '".((int)$model->year+1)."-03-31' ";
        if (($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") || $model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->Properties["useSpecial_Support_School"] == "1") {
                //特別支援学校の場合、校種で絞らない
                $query .= "     AND SCHREGNO IN (SELECT SCHREGNO FROM SCH_INFO) ";
            } else {
                //特別支援学校以外は、校種で絞る
                if ($model->Properties["use_prg_schoolkind"] == "1") {
                    $query .= "     AND SCHREGNO IN (SELECT SCHREGNO FROM SCH_INFO WHERE SCHOOL_KIND = '".$model->schoolkind."') ";
                } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
                    $query .= "     AND SCHREGNO IN (SELECT SCHREGNO FROM SCH_INFO WHERE SCHOOL_KIND = '".SCHOOLKIND."') ";
                }
            }
        }
        $query .= "     ORDER BY ";
        $query .= "         DATE ";
        $query .= " ), SCH_CNT AS( ";
        $query .= "     SELECT ";
        $query .= "         VISIT_DATE AS DATE, ";
        $query .= "         TYPE, ";
        $query .= "         COUNT(SCHREGNO) AS CNT ";
        $query .= "     FROM ";
        $query .= "         NURSEOFF_VISITREC_DAT ";
        $query .= "     WHERE ";
        $query .= "         VISIT_DATE BETWEEN '".$model->year."-04-01' AND '".((int)$model->year+1)."-03-31' ";
        if (($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") || $model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->Properties["useSpecial_Support_School"] == "1") {
                //特別支援学校の場合、校種で絞らない
                $query .= "     AND SCHREGNO IN (SELECT SCHREGNO FROM SCH_INFO) ";
            } else {
                //特別支援学校以外は、校種で絞る
                if ($model->Properties["use_prg_schoolkind"] == "1") {
                    $query .= "     AND SCHREGNO IN (SELECT SCHREGNO FROM SCH_INFO WHERE SCHOOL_KIND = '".$model->schoolkind."') ";
                } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
                    $query .= "     AND SCHREGNO IN (SELECT SCHREGNO FROM SCH_INFO WHERE SCHOOL_KIND = '".SCHOOLKIND."') ";
                }
            }
        }
        $query .= "     GROUP BY ";
        $query .= "         VISIT_DATE, ";
        $query .= "         TYPE ";
        $query .= " ) ";

        if ($model->sort["SRT_D"] == 1) {//初期値は降順
            $OrderBy1 = " DESC ";
        } elseif ($model->sort["SRT_D"] == -1) {
            $OrderBy1 = " ASC ";
        }
        $query .= " SELECT ";
        $query .= "     T1.DATE, ";
        $query .= "     S1.CNT AS TYPE1, ";
        $query .= "     S2.CNT AS TYPE2, ";
        $query .= "     S3.CNT AS TYPE3, ";
        $query .= "     S4.CNT AS TYPE4, ";
        $query .= "     S5.CNT AS TYPE5, ";
        $query .= "     T2.DATE AS DIARY, ";
        $query .= "     '".$model->year."' AS YEAR, ";
        $query .= "     '".$model->schoolkind."' AS SCHKIND ";
        $query .= " FROM ";
        $query .= "     DATE T1 ";
        $query .= "     LEFT JOIN SCH_CNT S1 ON T1.DATE = S1.DATE AND S1.TYPE = '1' ";
        $query .= "     LEFT JOIN SCH_CNT S2 ON T1.DATE = S2.DATE AND S2.TYPE = '2' ";
        $query .= "     LEFT JOIN SCH_CNT S3 ON T1.DATE = S3.DATE AND S3.TYPE = '3' ";
        $query .= "     LEFT JOIN SCH_CNT S4 ON T1.DATE = S4.DATE AND S4.TYPE = '4' ";
        $query .= "     LEFT JOIN SCH_CNT S5 ON T1.DATE = S5.DATE AND S5.TYPE = '5' ";
        $query .= "     LEFT JOIN NURSEOFF_DIARY_DAT T2 ON T1.DATE = T2.DATE ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            $query .= "             AND T2.SCHOOLCD    = '".sprintf("%012d", SCHOOLCD)."' ";
            //特別支援学校以外は、校種で絞る
            if ($model->Properties["useSpecial_Support_School"] != "1") {
                $query .= "             AND T2.SCHOOL_KIND = '".$model->schoolkind."' ";
            }
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= "             AND T2.SCHOOLCD    = '".sprintf("%012d", SCHOOLCD)."' ";
            //特別支援学校以外は、校種で絞る
            if ($model->Properties["useSpecial_Support_School"] != "1") {
                $query .= "             AND T2.SCHOOL_KIND = '".SCHOOLKIND."' ";
            }
        }
        $query .= " ORDER BY  ";
        $query .= "     T1.DATE {$OrderBy1} ";
        return $query;
    }

    //１レコード取得
    public function getRow($model, $date)
    {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     NURSEOFF_DIARY_DAT ";
        $query .= " WHERE ";
        $query .= "     DATE = '".$date."' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            $query .= " AND SCHOOLCD    = '".sprintf("%012d", SCHOOLCD)."' ";
            //特別支援学校以外は、校種で絞る
            if ($model->Properties["useSpecial_Support_School"] != "1") {
                $query .= " AND SCHOOL_KIND = '".$model->sendSchoolKind."' ";
            }
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND SCHOOLCD    = '".sprintf("%012d", SCHOOLCD)."' ";
            //特別支援学校以外は、校種で絞る
            if ($model->Properties["useSpecial_Support_School"] != "1") {
                $query .= " AND SCHOOL_KIND = '".SCHOOLKIND."' ";
            }
        }

        return $query;
    }

    //名称取得
    public function getNameMst($model)
    {
        $query  = " SELECT ";
        $query .= "     NAMECD2 AS VALUE, ";
        $query .= "     NAMECD2 || ' ' || NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$model->year."' AND ";
        $query .= "     NAMECD1 = 'A006' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //学年取得
    public function getGrade($model, $year, $schoolkind)
    {
        $query  = " SELECT ";
        $query .= "     GRADE AS VALUE, ";
        $query .= "     GRADE_NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_GDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$year."' ";
        //特別支援学校以外は、校種で絞る
        if ($model->Properties["useSpecial_Support_School"] != "1") {
            if ($model->Properties["use_prg_schoolkind"] == "1") {
                $query .= " AND SCHOOL_KIND = '".$schoolkind."' ";
            } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
                $query .= " AND SCHOOL_KIND = '".SCHOOLKIND."' ";
            }
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
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            $query .= " AND SCHOOLCD    = '".sprintf("%012d", SCHOOLCD)."' ";
            //特別支援学校以外は、校種で絞る
            if ($model->Properties["useSpecial_Support_School"] != "1") {
                $query .= " AND SCHOOL_KIND = '".$model->sendSchoolKind."' ";
            }
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND SCHOOLCD    = '".sprintf("%012d", SCHOOLCD)."' ";
            //特別支援学校以外は、校種で絞る
            if ($model->Properties["useSpecial_Support_School"] != "1") {
                $query .= " AND SCHOOL_KIND = '".SCHOOLKIND."' ";
            }
        }

        return $query;
    }

    //INSERT
    public function &getInsertQuery($model)
    {
        if ($model->cmd == "update") {
            knjf170Query::getDeleteQuery($model);
        }

        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $date = str_replace("/", "-", $model->field["DATE"]);

        $data = array();
        if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $data["SCHOOLCD"][TEXT]     = sprintf("%012d", SCHOOLCD);
            $data["SCHOOL_KIND"][TEXT]  = $model->sendSchoolKind;
        }
        $data["DATE"][TEXT]         = $date;
        $data["WEATHER"][TEXT]      = $model->field["WEATHER"];
        $data["WEATHER_TEXT"][TEXT] = $model->field["WEATHER_TEXT"];
        $data["TEMPERATURE"][TEXT]  = $model->field["TEMPERATURE"];
        $data["EVENT"][TEXT]        = $model->field["EVENT"];
        $data["DIARY"][TEXT]        = $model->field["DIARY"];
        $data["YEAR"][TEXT]         = $model->year;
        $data["REGISTERCD"][TEXT]   = STAFFCD;
        $data["UPDATED"][FUNC]      = "sysdate()";

        $query = Query::insertSQL($data, "NURSEOFF_DIARY_DAT");
        $db->query($query);

        if ($model->Properties["useNurseoffAttend"] == "1") {
            //欠席状況更新
            foreach ($model->di_cd as $dkey => $dval) {
                foreach ($model->grade as $gkey => $gval) {
                    $data = array();
                    if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
                        $data["SCHOOLCD"][TEXT]     = sprintf("%012d", SCHOOLCD);
                        $data["SCHOOL_KIND"][TEXT]  = $model->sendSchoolKind;
                    }
                    $data["DATE"][TEXT]         = $date;
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

        $db->commit();
        Query::dbCheckIn($db);
        return;
    }

    //DELETE
    public function &getDeleteQuery($model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $query  = " DELETE FROM ";
        $query .= "     NURSEOFF_DIARY_DAT ";
        $query .= " WHERE ";
        $query .= "     DATE        = '".str_replace("/", "-", $model->field["DATE"])."' ";
        if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND SCHOOLCD    = '".sprintf("%012d", SCHOOLCD)."' ";
            $query .= " AND SCHOOL_KIND = '".$model->sendSchoolKind."' ";
        }

        $db->query($query);

        if ($model->Properties["useNurseoffAttend"] == "1") {
            //欠席状況
            $query  = " DELETE FROM ";
            $query .= "     NURSEOFF_ATTEND_DAT ";
            $query .= " WHERE ";
            $query .= "     DATE        = '".str_replace("/", "-", $model->field["DATE"])."' ";
            if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
                $query .= " AND SCHOOLCD    = '".sprintf("%012d", SCHOOLCD)."' ";
                $query .= " AND SCHOOL_KIND = '".$model->sendSchoolKind."' ";
            }

            $db->query($query);
        }

        $db->commit();
        Query::dbCheckIn($db);
        return;
    }

    //テーブル存在チェック
    public function checkTableExist()
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     SYSIBM.SYSTABLES ";
        $query .= " WHERE ";
        $query .= "     NAME = 'SETTING_DAT' ";

        return $query;
    }

    //生徒項目名取得
    public function getSchName($model)
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     REMARK1, ";
        $query .= "     SCHOOLCD ";
        $query .= " FROM ";
        $query .= "     SETTING_DAT ";
        $query .= " WHERE ";
        $query .= "     SEQ = '001' ";
        $query .= " AND SCHOOLCD    = '".sprintf("%012d", SCHOOLCD)."' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            $query .= " AND SCHOOL_KIND = '".$model->schoolkind."' ";
        } else {
            $query .= " AND SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     SCHOOLCD ";

        return $query;
    }
}
