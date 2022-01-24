<?php

require_once('for_php7.php');

class knjb0010Query extends Query {

    //同時展開群リスト
    public function selectQuery($model, $flg)
    {
        if ($flg == 1) {
            $query  = " SELECT DISTINCT ";
            $query .= "     T1.groupcd ";
        } else {
            $query  = " SELECT ";
            $query .= "     t1.groupcd, ";
            $query .= "     t1.groupname, ";
            $query .= "     t2.lessoncnt, ";
            $query .= "     t2.framecnt, ";
            $query .= "     T2.l_cnt, ";
            $query .= "     T2.f_cnt, ";
            $query .= "     T3.chaircd, ";
            $query .= "     T3.chairname, ";
            $query .= "     T5.hr_name ";
        }
        $query .= " FROM ";
        $query .= "     (SELECT * FROM v_electclass_mst WHERE year = '".CTRL_YEAR."') T1  ";
        $query .= "     LEFT JOIN ( SELECT ";
        $query .= "                     year, ";
        $query .= "                     groupcd,  ";
        $query .= "                     MAX(lessoncnt) AS lessoncnt, ";
        $query .= "                     MAX(framecnt) AS framecnt, ";
        $query .= "                     COUNT(DISTINCT lessoncnt) AS l_cnt, ";
        $query .= "                     COUNT(DISTINCT framecnt) AS f_cnt ";
        $query .= "                 FROM ";
        $query .= "                     chair_dat ";
        $query .= "                 WHERE ";
        $query .= "                     year     = '".CTRL_YEAR."' ";
        $query .= "                 AND semester = '".CTRL_SEMESTER."' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= "             AND SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind), "','")."') ";
            }
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= "             AND SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= "                 GROUP BY ";
        $query .= "                     year, ";
        $query .= "                     groupcd) T2 ";
        $query .= "              ON t1.year     = T2.year ";
        $query .= "             AND t1.groupcd  = t2.groupcd ";
        $query .= "     LEFT JOIN chair_dat T3 ";
        $query .= "              ON T1.year     = t3.year ";
        $query .= "             AND t1.groupcd  = T3.groupcd ";
        $query .= "             AND t3.semester = '".CTRL_SEMESTER."' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= "         AND T3.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind), "','")."') ";
            }
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= "         AND T3.SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= "     LEFT JOIN ( SELECT ";
        $query .= "                     T1.groupcd, ";
        $query .= "                     T2.hr_name ";
        $query .= "                 FROM ";
        $query .= "                     chair_cls_dat T1, ";
        $query .= "                     schreg_regd_hdat T2 ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= "                 INNER JOIN SCHREG_REGD_GDAT G1 ";
                $query .= "                          ON T2.YEAR         = G1.YEAR ";
                $query .= "                         AND T2.GRADE        = G1.GRADE ";
                $query .= "                         AND G1.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind), "','")."') ";
            }
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= "                 INNER JOIN SCHREG_REGD_GDAT G1 ";
            $query .= "                          ON T2.YEAR         = G1.YEAR ";
            $query .= "                         AND T2.GRADE        = G1.GRADE ";
            $query .= "                         AND G1.SCHOOL_KIND  = '".SCHOOLKIND."' ";
        }
        $query .= "                 WHERE ";
        $query .= "                     T2.year     = '".CTRL_YEAR."' ";
        $query .= "                 AND T2.semester = '".CTRL_SEMESTER."' ";
        $query .= "                 AND T2.grade || '-' || T2.hr_class = T1.trgtgrade || '-' || T1.trgtclass ";
        $query .= "                 AND T1.year     = T2.year ";
        $query .= "                 AND T1.semester = T2.semester ) T5 ";
        $query .= "              ON T1.groupcd = T5.groupcd ";
        $query .= " WHERE ";
        $query .= "     T1.groupcd <> '0000' ";
        $query .= " ORDER BY ";
        $query .= "     T1.groupcd ";

        return $query;
    }

    //受講クラス
    public function selectQueryChairClsDat($model, $chaircd, $groupcd)
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     T1.TRGTGRADE, ";
        $query .= "     T1.TRGTCLASS, ";
        $query .= "     T2.HR_NAME ";
        $query .= " FROM ";
        $query .= "     chair_cls_dat T1, ";
        $query .= "     schreg_regd_hdat T2 ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= " INNER JOIN SCHREG_REGD_GDAT L1 ";
                $query .= "          ON L1.YEAR         = T2.YEAR ";
                $query .= "         AND L1.GRADE        = T2.GRADE ";
                $query .= "         AND L1.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind), "','")."') ";
            }
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " INNER JOIN SCHREG_REGD_GDAT L1 ";
            $query .= "          ON L1.YEAR         = T2.YEAR ";
            $query .= "         AND L1.GRADE        = T2.GRADE ";
            $query .= "         AND L1.SCHOOL_KIND  = '".SCHOOLKIND."' ";
        }
        $query .= " WHERE ";
        $query .= "     T1.year         = T2.year ";
        $query .= " AND T1.semester     = T2.semester ";
        $query .= " AND T1.trgtgrade    = T2.grade ";
        $query .= " AND T1.trgtclass    = T2.hr_class ";
        $query .= " AND T1.year         = '".CTRL_YEAR."' ";
        $query .= " AND T1.semester     = '".CTRL_SEMESTER."' ";
        $query .= " AND T1.groupcd      = '".$groupcd."' ";
        $query .= " ORDER BY ";
        $query .= "     T1.TRGTGRADE, ";
        $query .= "     T1.TRGTCLASS ";

        return $query;
    }

    //各群情報
    public function getGroup($model, $groupcd)
    {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     (SELECT ";
        $query .= "         chaircd, ";
        $query .= "         chairname ";
        $query .= "     FROM ";
        $query .= "         chair_dat ";
        $query .= "     WHERE ";
        $query .= "         year     = '".CTRL_YEAR."' ";
        $query .= "     AND semester = '".CTRL_SEMESTER."' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= " AND SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind), "','")."') ";
            }
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= "     AND groupcd  = '".$groupcd."') T1, ";
        $query .= "     (SELECT ";
        $query .= "         MAX(lessoncnt) AS lessoncnt, ";
        $query .= "         MAX(framecnt) AS framecnt ";
        $query .= "     FROM ";
        $query .= "         chair_dat ";
        $query .= "     WHERE ";
        $query .= "         year     = '".CTRL_YEAR."' ";
        $query .= "     AND semester = '".CTRL_SEMESTER."' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= " AND SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind), "','")."') ";
            }
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= "     AND groupcd  = '".$groupcd."') T2  ";
        $query .= " ORDER BY ";
        $query .= "     chaircd ";

        return $query;
    }

    //講座一覧
    public function getChair($model, $chaircd)
    {
        $query  = " SELECT ";
        $query .= "     chaircd, ";
        $query .= "     chairname ";
        $query .= " FROM ";
        $query .= "     chair_dat ";
        $query .= " WHERE ";
        $query .= "     year        = '".CTRL_YEAR."' ";
        $query .= " AND semester    = '".CTRL_SEMESTER."' ";
        $query .= " AND groupcd     = '0000' ";
        $query .= " AND chaircd NOT IN ('" . implode("','", $chaircd) . "') ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= " AND SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind), "','")."') ";
            }
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     chaircd ";

        return $query;
    }

    //クラス一覧
    public function getClass($model, $class)
    {
        $query .= " SELECT ";
        $query .= "     grade, ";
        $query .= "     hr_class, ";
        $query .= "     hr_name ";
        $query .= " FROM ";
        $query .= "     schreg_regd_hdat T1 ";
        $query .= " WHERE ";
        $query .= "     year     = '".CTRL_YEAR."' ";
        $query .= " AND semester = '".CTRL_SEMESTER."' ";
        $query .= " AND grade || '-' || hr_class NOT IN ('" . implode("','", $class) . "') ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= " AND GRADE IN (  SELECT ";
                $query .= "                     G1.GRADE ";
                $query .= "                 FROM ";
                $query .= "                     SCHREG_REGD_GDAT G1 ";
                $query .= "                 WHERE ";
                $query .= "                     G1.YEAR         = T1.YEAR AND ";
                $query .= "                     G1.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind), "','")."') ";
                $query .= "                 ) ";
            }
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND GRADE IN (  SELECT ";
            $query .= "                     G1.GRADE ";
            $query .= "                 FROM ";
            $query .= "                     SCHREG_REGD_GDAT G1 ";
            $query .= "                 WHERE ";
            $query .= "                     G1.YEAR         = T1.YEAR AND ";
            $query .= "                     G1.SCHOOL_KIND  = '".SCHOOLKIND."' ";
            $query .= "                 ) ";
        }
        $query .= " ORDER BY ";
        $query .= "     grade, ";
        $query .= "     hr_class ";

        return $query;
    }

    //前学期からコピー(前学期にデータがあって今学期のデータがまだ作成されていない場合のみ)
    public function &copyQuery($model)
    {
        $db = Query::dbCheckOut();
        $result = $db->query(knjb0010Query::selectQuery($model, 1));

        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $cnt = array();
            //今学期が最初学期の場合は去年の最終学期を求めて条件にする
            if (CTRL_SEMESTER == $model->ctrl["SEMESTER"][0]) {
                $where = " WHERE year     = '".(CTRL_YEAR - 1)."'
                            AND groupcd  = '".$row["GROUPCD"]."'
                            AND semester = (SELECT MAX(semester) FROM semester_mst
                                           WHERE year = '".(CTRL_YEAR - 1)."' 
                                             AND semester <> '9')";
            //前学期を条件にする
            } else {
                $where = " WHERE year     = '".CTRL_YEAR."'
                              AND semester = '".$model->ctrl["SEMESTER"][(CTRL_SEMESTER - 2)]."'
                              AND groupcd  = '".$row["GROUPCD"]."'";
            }

            $where2 = "";
            if ($model->Properties["use_prg_schoolkind"] == "1") {
                if ($model->selectSchoolKind) {
                    $where2 .= " AND SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind), "','")."') ";
                }
            } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
                $where2 .= " AND SCHOOL_KIND = '".SCHOOLKIND."' ";
            }

            #### 講座データ ####

            $cnt = knjb0010Query::getDataCount($db, "chair_dat", $where, $row["GROUPCD"], $where2);
            if ((int)$cnt["LAST_CNT"] > 0 && (int)$cnt["CUR_CNT"] == 0) {
                $query  = " INSERT INTO CHAIR_DAT ";
                $query .= "    (SELECT ";
                $query .= "         '".CTRL_YEAR."', ";
                $query .= "         '".CTRL_SEMESTER."', ";
                $query .= "         CHAIRCD, ";
                $query .= "         GROUPCD, ";
                if ($model->Properties["useCurriculumcd"] == '1') {
                    $query .= "         CLASSCD, ";
                    $query .= "         SCHOOL_KIND, ";
                    $query .= "         CURRICULUM_CD, ";
                }
                $query .= "         SUBCLASSCD, ";
                $query .= "         CHAIRNAME, ";
                $query .= "         CHAIRABBV, ";
                $query .= "         TAKESEMES, ";
                $query .= "         LESSONCNT, ";
                $query .= "         FRAMECNT, ";
                $query .= "         COUNTFLG, ";
                $query .= "         '".STAFFCD."', ";
                $query .= "         SYSDATE() ";
                $query .= "     FROM ";
                $query .= "         CHAIR_DAT ";
                $query .= $where;
                $query .= $where2;
                $query .= "    ) ";

                $db->query($query);
            }

            $where2 = "";
            if ($model->Properties["use_prg_schoolkind"] == "1") {
                if ($model->selectSchoolKind) {
                    $where2 .= "   AND TRGTGRADE IN (SELECT ";
                    $where2 .= "                        GRADE ";
                    $where2 .= "                    FROM ";
                    $where2 .= "                        SCHREG_REGD_GDAT ";
                    $where2 .= "                    WHERE ";
                    $where2 .= "                        YEAR         = '".CTRL_YEAR."' AND ";
                    $where2 .= "                        SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind), "','")."') ";
                    $where2 .= "                    ) ";
                }
            } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
                $where2 .= "   AND TRGTGRADE IN (SELECT ";
                $where2 .= "                        GRADE ";
                $where2 .= "                    FROM ";
                $where2 .= "                        SCHREG_REGD_GDAT ";
                $where2 .= "                    WHERE ";
                $where2 .= "                        YEAR         = '".CTRL_YEAR."' AND ";
                $where2 .= "                        SCHOOL_KIND  = '".SCHOOLKIND."' ";
                $where2 .= "                    ) ";
            }

            #### 講座受講クラスデータ ####

            $cnt = knjb0010Query::getDataCount($db, "chair_cls_dat", $where, $row["GROUPCD"], $where2);
            if ((int)$cnt["LAST_CNT"] > 0 && (int)$cnt["CUR_CNT"] == 0) {
                $query = "INSERT INTO chair_cls_dat
                           (SELECT '".CTRL_YEAR."','".CTRL_SEMESTER."', chaircd, groupcd, trgtgrade, trgtclass, '".STAFFCD."', SYSDATE()
                             FROM chair_cls_dat ";
                $db->query($query.$where.$where2.")");
            }
        }
        Query::dbCheckIn($db);
    }

    //前学期と今学期のデータ件数を取得
    public function getDataCount($db, $table, $where, $groupcd, $where2)
    {
        $count = array();
        $query = "SELECT t1.last_cnt, t2.cur_cnt 
                    FROM (SELECT COUNT(*) AS last_cnt FROM ".$table." ".$where." ".$where2." ) T1,
                         (SELECT COUNT(*) AS cur_cnt  FROM ".$table."
                           WHERE year     = '".CTRL_YEAR."' 
                             AND semester = '".CTRL_SEMESTER."' 
                             AND groupcd  = '".$groupcd."' ".$where2." ) T2 ";

        return $db->getRow($query, DB_FETCHMODE_ASSOC, $cnt);
    }

    //更新
    public function &updateQuery($model)
    {
        $db = Query::dbCheckOut();

        #### 講座データ ####

        $db->autoCommit(false);
        //一度群からはずす
        $query = "UPDATE chair_dat 
                     SET groupcd = '0000',
                         registercd = '".STAFFCD."',
                         updated    = SYSDATE() 
                   WHERE year     = '".CTRL_YEAR."'
                     AND semester = '".CTRL_SEMESTER."'
                     AND groupcd  = '".$model->groupcd."'";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= " AND SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind), "','")."') ";
            }
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $db->query($query);

        $query = "UPDATE chair_dat 
                     SET groupcd    = '".$model->groupcd."'";
        $query .= "     ,lessoncnt  = ".((strlen($model->field["LESSONCNT"])) ? sprintf("%d", $model->field["LESSONCNT"]) : "CAST(NULL AS SMALLINT)");
        $query .= "     ,framecnt   = ".((strlen($model->field["FRAMECNT"])) ? sprintf("%d", $model->field["FRAMECNT"]) : "CAST(NULL AS SMALLINT)");
        $query .= "     ,registercd = '".STAFFCD."', 
                         updated    = SYSDATE()
                   WHERE year     = '".CTRL_YEAR."'
                     AND semester = '".CTRL_SEMESTER."'
                     AND chaircd  IN ('" . str_replace(",", "','", $model->field["CHAIRCD"]) . "')";
        $db->query($query);

        #### 講座受講クラスデータ ####

        $query = "DELETE FROM chair_cls_dat 
                   WHERE year     = '".CTRL_YEAR."'
                     AND semester = '".CTRL_SEMESTER."'
                     AND (groupcd  = '".$model->groupcd."' OR chaircd  IN ('" . str_replace(",", "','", $model->field["CHAIRCD"]) . "')) ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= " AND TRGTGRADE IN (  SELECT ";
                $query .= "                         GRADE ";
                $query .= "                     FROM ";
                $query .= "                         SCHREG_REGD_GDAT ";
                $query .= "                     WHERE ";
                $query .= "                         YEAR         = '".CTRL_YEAR."' AND ";
                $query .= "                         SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind), "','")."') ";
                $query .= "                   ) ";
            }
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND TRGTGRADE IN (  SELECT ";
            $query .= "                         GRADE ";
            $query .= "                     FROM ";
            $query .= "                         SCHREG_REGD_GDAT ";
            $query .= "                     WHERE ";
            $query .= "                         YEAR         = '".CTRL_YEAR."' AND ";
            $query .= "                         SCHOOL_KIND  = '".SCHOOLKIND."' ";
            $query .= "                   ) ";
        }

        $db->query($query);

        if ($model->field["ATTENDCLASS"]) {
            $class = explode(",", $model->field["ATTENDCLASS"]);
            foreach ($class as $val) {
                $cd = explode("-", $val);
                $query = "INSERT INTO chair_cls_dat 
                          VALUES ('".CTRL_YEAR."'
                                 ,'".CTRL_SEMESTER."'
                                 ,'0000000'
                                 ,'".$model->groupcd."'
                                 ,'" . sprintf("%02d", $cd[0]) . "'
                                 ,'" . sprintf("%03s", $cd[1]) . "'
                                 ,'".STAFFCD."'
                                 ,SYSDATE())";
                $db->query($query);
            }
        }

        $db->commit();
        Query::dbCheckIn($db);
    }


    //削除
    public function deleteQuery($staffcd)
    {
        $db = Query::dbCheckOut();
        Query::dbCheckIn($db);
    }
}
?>
