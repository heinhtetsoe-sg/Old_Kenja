<?php

require_once('for_php7.php');

class knjc020aQuery extends Query
{

    //学期取得
    public function getTerm($year, $day)
    {
        return " VALUES TERM_GET('".$year."',DATE('".$day."'),'N') ";
    }

    //校時名称の取得
    public function getNamecd($year, $cd)
    {
        return " SELECT namecd2,name1 FROM v_name_mst WHERE year = '".$year."' AND namecd1 = '".$cd."' ";
    }

    //アップデート
    public function &getUpdateQuery(&$model, $termIs)
    {
        $ary = array();
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $ary = explode(",", $model->chosen_id);

        //エラー回避
        if (trim($ary[3] == "m")) {
            $ary[3] = $ary[4];
        }

        if (trim($ary[3]) == "red" || $model->cmd == "confirm") {
            $b_or_r = "1";
        } else {
            $b_or_r = "0";
        }

        $data["ATTESTOR"][TEXT] = STAFFCD ;         //出席確認者
        $data["SEMESTER"][TEXT] = $termIs;          //処理学期
        $data["EXECUTED"][TEXT] = $b_or_r ;         //実施フラグ
        $data["UPDATED"][FUNC] = "sysdate()";       //更新日付
        $where  = " WHERE executedate = DATE('";
        $where .=                           str_replace("/", "-", $model->cntl_dt_key) ;
        $where .= "                         ') ";
        $where .= "   AND PERIODCD = '";
        $where .=                   $ary[0] ;
        $where .= "'  AND YEAR = '";
        $where .=                   $model->cntl_dt_year ;
        $where .= " '  AND CHAIRCD = '";
        $where .=                   $model->stock_chaircd;
        $where .= " ' ";

        $query = Query::updateSQL($data, "SCH_CHR_DAT", $where);
        $db->query($query);
        $db->commit();

        $row = $db->getRow(knjc020aQuery::getGroupCd($model->cntl_dt_key, $ary[0], $model->stock_chaircd, $model->cntl_dt_year), DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);

        knjc020aQuery::deleteInsHrateDat($model, $row, "DEL");
        if ($b_or_r == "1") {
            knjc020aQuery::deleteInsHrateDat($model, $row, "INS");
        }
        return true;
    }

    //更新用群コード取得SQL
    public function getGroupCd($date, $period, $chair, $year)
    {
        $query  = "WITH SCH_CHR AS ( ";
        $query .= "SELECT ";
        $query .= "    * ";
        $query .= "FROM ";
        $query .= "    SCH_CHR_DAT ";
        $query .= "WHERE ";
        $query .= "    EXECUTEDATE = DATE('".str_replace("/", "-", $date)."') ";
        $query .= "    AND PERIODCD = '".$period."' ";
        $query .= "    AND CHAIRCD = '".$chair."' ";
        $query .= "    AND YEAR = '".$year."' ";
        $query .= ") ";
        $query .= "SELECT ";
        $query .= "    T2.EXECUTEDATE, ";
        $query .= "    T2.PERIODCD, ";
        $query .= "    T1.YEAR, ";
        $query .= "    T1.SEMESTER, ";
        $query .= "    T1.GROUPCD, ";
        $query .= "    T1.CHAIRCD ";
        $query .= "FROM ";
        $query .= "    CHAIR_DAT T1, ";
        $query .= "    SCH_CHR T2 ";
        $query .= "WHERE ";
        $query .= "    T1.YEAR = T2.YEAR ";
        $query .= "    AND T1.SEMESTER = T2.SEMESTER ";
        $query .= "    AND T1.CHAIRCD = T2.CHAIRCD ";

        return $query;
    }

    //更新用年組コード取得SQL
    public function getChrClass($row, $div = "")
    {
        $query  = "SELECT ";
        if ($div == "COLOR") {
            $query .= "    COUNT(*) ";
        } else {
            $query .= "    TRGTGRADE, ";
            $query .= "    TRGTCLASS ";
        }
        $query .= "FROM ";
        $query .= "    CHAIR_CLS_DAT ";
        $query .= "WHERE ";
        $query .= "    YEAR = '".$row["YEAR"]."' ";
        $query .= "    AND SEMESTER = '".$row["SEMESTER"]."' ";
        if ($row["GROUPCD"] > 0) {
            $query .= "    AND GROUPCD = '".$row["GROUPCD"]."' ";
        } else {
            $query .= "    AND CHAIRCD = '".$row["CHAIRCD"]."' ";
        }

        return $query;
    }

    //更新用年組コード取得SQL
    public function getChrClass2($model, $row)
    {
        $query .= " SELECT DISTINCT ";
        $query .= "     T3.GRADE, ";
        $query .= "     T3.HR_CLASS ";
        $query .= " FROM ";
        $query .= "     SCH_CHR_DAT T1 ";
        $query .= "     INNER JOIN CHAIR_STD_DAT T2 ON T2.YEAR = '{$model->cntl_dt_year}' ";
        $query .= "           AND T2.SEMESTER = '".CTRL_SEMESTER."' ";
        $query .= "           AND T2.CHAIRCD = T1.CHAIRCD ";
        $query .= "           AND T1.EXECUTEDATE BETWEEN T2.APPDATE AND T2.APPENDDATE ";
        $query .= "     INNER JOIN SCHREG_REGD_DAT T3 ON T3.YEAR = T2.YEAR ";
        $query .= "           AND T3.SEMESTER = T2.SEMESTER ";
        $query .= "           AND T3.SCHREGNO = T2.SCHREGNO ";
        $query .= " WHERE ";
        $query .= "     T1.EXECUTEDATE = '".str_replace("/", "-", $row["EXECUTEDATE"])."' ";
        $query .= "     AND T1.PERIODCD = '{$row["PERIODCD"]}' ";
        $query .= "     AND T1.CHAIRCD = '{$row["CHAIRCD"]}' ";

        return $query;
    }

    //ホームルーム出席取得SQL
    public function getHrateClass($row, $date, $period, $chair)
    {
        $query .= "SELECT ";
        $query .= "    COUNT(*) AS CNT, ";
        $query .= "    SUM(SMALLINT(EXECUTED)) AS SUMEXECUTED ";
        $query .= "FROM ";
        $query .= "    SCH_CHR_HRATE_DAT ";
        $query .= "WHERE ";
        $query .= "    EXECUTEDATE = '".$date."' ";
        $query .= "    AND PERIODCD = '".$period."' ";
        $query .= "    AND CHAIRCD = '".$chair."' ";

        return $query;
    }

    //削除、追加処理
    public function deleteInsHrateDat($model, $row, $mode)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $result = $db->query(knjc020aQuery::getChrClass2($model, $row));
        while ($delData = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            if ($mode == "DEL") {
                $query  = "DELETE FROM SCH_CHR_HRATE_DAT ";
                $query .= "WHERE ";
                $query .= "    EXECUTEDATE = '".$row["EXECUTEDATE"]."' ";
                $query .= "    AND PERIODCD = '".$row["PERIODCD"]."' ";
                $query .= "    AND CHAIRCD = '".$row["CHAIRCD"]."' ";
            } else {
                $data["EXECUTEDATE"][DATE] = $row["EXECUTEDATE"];   //出席確認者
                $data["PERIODCD"][TEXT]    = $row["PERIODCD"];      //処理学期
                $data["CHAIRCD"][TEXT]     = $row["CHAIRCD"];       //処理学期
                $data["GRADE"][TEXT]       = $delData["GRADE"];     //処理学期
                $data["HR_CLASS"][TEXT]    = $delData["HR_CLASS"];  //処理学期
                $data["EXECUTED"][TEXT]    = "1" ;                  //実施フラグ
                $data["ATTESTOR"][TEXT]    = STAFFCD ;              //出席確認者
                $data["REGISTERCD"][TEXT]  = STAFFCD ;              //更新者
                $data["UPDATED"][FUNC]     = "sysdate()";           //更新日付
                $query = Query::insertSQL($data, "SCH_CHR_HRATE_DAT");
            }
            $db->query($query);
            $db->commit();
        }
        $result->free();

        Query::dbCheckIn($db);
    }

    //スタッフの確認
    public function getStaffCount()
    {
        $query  = " SELECT COUNT(*) FROM V_STAFF_MST T1 ";
        $query .= " WHERE T1.YEAR ='".CTRL_YEAR."' AND T1.CHARGECLASSCD = '1' ";

        $db = Query::dbCheckOut();
        $row = $db->getOne($query);
        Query::dbCheckIn($db);

        return $row;
    }

    //ログインスタッフ情報取得
    public function getLoginStaffInfo()
    {
        return " SELECT SECTIONCD FROM V_STAFF_MST WHERE STAFFCD = '".STAFFCD."' AND YEAR ='".CTRL_YEAR."' ";
    }

    //スタッフ基本データの取得
    public function getStaffData(&$model)
    {
        $query  = " SELECT ";
        $query .= "  T1.STAFFCD, T1.STAFFNAME_SHOW as SHOW_NAME, T1.SECTIONCD, T2.SECTIONABBV AS SECTIONNAME ";
        $query .= "  FROM ";
        $query .= "  V_STAFF_MST T1 ";
        $query .= "  LEFT JOIN SECTION_MST T2  ON T2.SECTIONCD = T1.SECTIONCD ";
        $query .= "  WHERE ";
        $query .= "  T1.YEAR ='".$model->cntl_dt_year."' AND T1.CHARGECLASSCD = '1' ";

        if ($model->SUBJECT != "" && $model->SUBJECT != "-1") {
            $query .= "  AND T1.SECTIONCD = '".$model->SUBJECT."' ";
        }
        if ($model->Properties["knjc020RestrictedShowLoginStaffOnly"] == '1') {
            if (AUTHORITY == DEF_REFER_RESTRICT|| AUTHORITY == DEF_UPDATE_RESTRICT) {
                $query .= "  AND T1.STAFFCD = '".STAFFCD."' ";
            }
        }
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= " AND (T1.STAFFCD IN (SELECT STAFFCD FROM USERGROUP_DAT ";
                $query .= "                    WHERE YEAR = '".CTRL_YEAR."' ";
                $query .= "                      AND SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind), "','")."') ";
                $query .= "                    ) ";
                $query .= "      OR T1.STAFFCD IN (SELECT STAFFCD FROM USERAUTH_DAT ";
                $query .= "                    WHERE SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind), "','")."') ";
                $query .= "                    ) ";
                $query .= "     )  ";
            }
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND (T1.STAFFCD IN (SELECT STAFFCD FROM USERGROUP_DAT ";
            $query .= "                    WHERE YEAR = '".CTRL_YEAR."' ";
            $query .= "                      AND SCHOOL_KIND = '".SCHOOLKIND."' ";
            $query .= "                    ) ";
            $query .= "      OR T1.STAFFCD IN (SELECT STAFFCD FROM USERAUTH_DAT ";
            $query .= "                    WHERE SCHOOL_KIND = '".SCHOOLKIND."'  ";
            $query .= "                    ) ";
            $query .= "     )  ";
        }

        $query .= "  ORDER BY ";
        $query .= "  T1.SECTIONCD ";

        return $query;
    }

    //入力取消前に情報をチェック
    public function chkAttenddata(&$model)
    {
        $return_val = false;
        $ary = array();
        $ary = explode(",", $model->chosen_id);

        //エラー回避
        if (trim($ary[3] == "m")) {
            $ary[3] = $ary[4];
        }

        if (trim($ary[3]) != "red") {
            $db = Query::dbCheckOut();

#            $query  = " SELECT COUNT(*) AS COUNT FROM attend_dat ";
#            $query .= "  WHERE attenddate = DATE('".$model->cntl_dt_key."') ";
#            $query .= "    AND periodcd   =  '".$ary[0]."' ";
#            $query .= "    AND schregno IN (";
#            $query .= "                     SELECT schregno FROM chair_std_dat ";
#            $query .= "                      WHERE year    = '".$model->cntl_dt_year."' ";
#            $query .= "                        AND chaircd = '".$model->stock_chaircd."') ";

            $query  = " SELECT COUNT(*) AS COUNT FROM attend_dat ";
            $query .= "  WHERE attenddate = DATE('".$model->cntl_dt_key."') ";
            $query .= "    AND periodcd   =  '".$ary[0]."' ";
            $query .= "    AND chaircd    = '".$model->stock_chaircd."' ";

            $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
            Query::dbCheckIn($db);

            if ($row["COUNT"] != 0) {
                $return_val = true ;
            }
        }
        return $return_val;
    }

    //入力取消前処理 情報を削除
    public function deleteAttenddata(&$model)
    {
        $ary = array();
        $ary = explode(",", $model->chosen_id);

        //エラー回避
        if (trim($ary[3] == "m")) {
            $ary[3] = $ary[4];
        }

        if (trim($ary[3]) != "red") {
            $db = Query::dbCheckOut();

#            $query  = " DELETE FROM attend_dat ";
#            $query .= "  WHERE attenddate = DATE('".$model->cntl_dt_key."') ";
#            $query .= "    AND periodcd   =  '".$ary[0]."' ";
#            $query .= "    AND schregno IN (";
#            $query .= "                     SELECT schregno FROM chair_std_dat ";
#            $query .= "                      WHERE year    =  '".$model->cntl_dt_year."' ";
#            $query .= "                        AND chaircd = '".$model->stock_chaircd."') ";

            $query  = " DELETE FROM attend_dat ";
            $query .= "  WHERE attenddate = DATE('".$model->cntl_dt_key."') ";
            $query .= "    AND periodcd   = '".$ary[0]."' ";
            $query .= "    AND chaircd    = '".$model->stock_chaircd."' ";

            $results = $db->query($query);
            Query::dbCheckIn($db);
        }
        return $results;
    }

    //科目基本データの取得
    public function findClass($model)
    {
        return "SELECT SECTIONCD, SECTIONABBV FROM V_SECTION_MST where YEAR='{$model->cntl_dt_year}'";
    }

    //表示用データを取得
    public function readQuery(&$model, $term, $useTestCountflg)
    {

        //テスト項目マスタの集計フラグの表
        $query  = " WITH TEST_COUNTFLG AS ( ";
        $query .= "     SELECT ";
        $query .= "         T1.EXECUTEDATE, ";
        $query .= "         T1.PERIODCD, ";
        $query .= "         T1.CHAIRCD, ";
        $query .= "         T2.COUNTFLG "; //0：集計しない 0以外：集計する
        $query .= "     FROM ";
        $query .= "         SCH_CHR_TEST T1, ";

        if ($useTestCountflg == "TESTITEM_MST_COUNTFLG_NEW_SDIV") {
            $query .= "         TESTITEM_MST_COUNTFLG_NEW_SDIV T2 ";
        } elseif ($useTestCountflg == "TESTITEM_MST_COUNTFLG") {
            $query .= "         TESTITEM_MST_COUNTFLG T2 ";
        } else {
            $query .= "         TESTITEM_MST_COUNTFLG_NEW T2 ";
        }
        $query .= "     WHERE ";
        $query .= "             T1.EXECUTEDATE = DATE('".$model->cntl_dt_key."') ";
        $query .= "         AND T2.YEAR       = T1.YEAR ";
        if ($useTestCountflg == "TESTITEM_MST_COUNTFLG_NEW_SDIV") {
            $query .= "         AND T2.SEMESTER   = T1.SEMESTER ";
            $query .= "         AND T2.SCORE_DIV  = '01' ";
        } elseif ($useTestCountflg == "TESTITEM_MST_COUNTFLG") {
        } else {
            $query .= "         AND T2.SEMESTER   = T1.SEMESTER ";
        }
        $query .= "         AND T2.TESTKINDCD = T1.TESTKINDCD ";
        $query .= "         AND T2.TESTITEMCD = T1.TESTITEMCD ";
        $query .= "     GROUP BY ";
        $query .= "         T1.EXECUTEDATE, ";
        $query .= "         T1.PERIODCD, ";
        $query .= "         T1.CHAIRCD, ";
        $query .= "         T2.COUNTFLG ";
        $query .= " ) ";

        $query .= "  , MAIN AS (";
        $query .= "  SELECT";
        $query .= "      T1.periodcd,";
        $query .= "      T1.chaircd,";
        $query .= "      T1.groupcd,";
        $query .= "      T1.attendcd,";     //executed(出欠区分)のこと
        $query .= "      T1.chargediv,";
//        $query .= "      T1.staffcd,";
        $query .= "      CASE WHEN T5.staffcd is not null then T5.staffcd else T1.staffcd end as staffcd,";    //2006.02.02 alp m-yama
        $query .= "      T4.hr_nameabbv AS TARGETCLASS,";
        if ($model->Properties["useCurriculumcd"] == "1") {
            $query .= "      T1.CLASSCD,";
            $query .= "      T1.SCHOOL_KIND,";
            $query .= "      T1.CURRICULUM_CD,";
        }
        $query .= "      T1.SUBCLASSCD,";
        $query .= "      T1.subclassabbv,";
        $query .= "      substr(T1.chairname,1,9) AS CHAIRNAME,";
        $query .= "      T3.trgtgrade, ";
        $query .= "      T3.trgtclass, ";
        $query .= "      CASE WHEN T1.DATADIV='2' THEN TEST.COUNTFLG ELSE T6.COUNTFLG END AS COUNTFLG, ";
        $query .= "      N1.NAMECD2 AS LESSON_MODE ";
        //講座情報を抽出(担当職員含む)
        $query .= "  FROM ( SELECT t1.periodcd, t1.executed AS attendcd, t1.DATADIV,";
        $query .= "                t1.chaircd, t2.groupcd, t2.chairname, ";
        if ($model->Properties["useCurriculumcd"] == "1") {
            $query .= "                t2.CLASSCD, t2.SCHOOL_KIND, t2.CURRICULUM_CD, ";
        }
        $query .= "                t2.SUBCLASSCD, t5.subclassabbv,";
        $query .= "                t3.staffcd, t3.chargediv";
        $query .= "           FROM sch_chr_dat   t1 ";
        $query .= "                INNER JOIN chair_dat t2 ON t2.year        = t1.year AND";
        $query .= "                    t2.semester    = t1.semester AND";
        $query .= "                    t2.chaircd     = t1.chaircd ";
        $query .= "                LEFT OUTER JOIN subclass_mst t5 ON t2.SUBCLASSCD = t5.SUBCLASSCD ";
        if ($model->Properties["useCurriculumcd"] == "1") {
            $query .= "                AND t2.CLASSCD = t5.CLASSCD ";
            $query .= "                AND t2.SCHOOL_KIND = t5.SCHOOL_KIND ";
            $query .= "                AND t2.CURRICULUM_CD = t5.CURRICULUM_CD ";
        }
        $query .= "                INNER JOIN chair_stf_dat t3 ON t3.year        = t2.year AND";
        $query .= "                t3.semester    = t2.semester AND";
        $query .= "                t3.chaircd     = t2.chaircd ";
        $query .= "                INNER JOIN v_staff_mst       t4 ON t3.year = t4.year AND t3.staffcd = t4.staffcd AND '1' = t4.chargeclasscd";
        $query .= "          WHERE t1.executedate = DATE('".$model->cntl_dt_key."') AND";
        $query .= "                t1.year        = '".CTRL_YEAR."' AND";
        $query .= "                t1.semester    = '".$term."' ";
        $query .= "       ) T1 ";
        //受講クラスを抽出
        $query .= "       LEFT OUTER JOIN ( SELECT t1.year, t1.semester, t2.chaircd, ";
        if ($model->Properties["useCurriculumcd"] == "1") {
            $query .= "                         t2.CLASSCD, t2.SCHOOL_KIND, t2.CURRICULUM_CD, ";
        }
        $query .= "                         t2.SUBCLASSCD, t2.groupcd, t1.trgtgrade, t1.trgtclass";
        $query .= "                           FROM chair_cls_dat t1, chair_dat t2";
        $query .= "                          WHERE t1.year        = '".CTRL_YEAR."' AND";
        $query .= "                                t1.semester    = '" .$term. "' AND";
        $query .= "                                t1.chaircd     = '0000000' AND";
        $query .= "                                t2.year        = t1.year AND";
        $query .= "                                t2.semester    = t1.semester AND";
        $query .= "                                t2.groupcd     = t1.groupcd";
        $query .= "                          UNION  ";
        $query .= "                          SELECT t1.year, t1.semester, t2.chaircd, ";
        if ($model->Properties["useCurriculumcd"] == "1") {
            $query .= "                         t2.CLASSCD, t2.SCHOOL_KIND, t2.CURRICULUM_CD, ";
        }
        $query .= "                         t2.SUBCLASSCD, t2.groupcd, t1.trgtgrade, t1.trgtclass";
        $query .= "                           FROM chair_cls_dat t1, chair_dat t2";
        $query .= "                          WHERE t1.year        = '".CTRL_YEAR."' AND";
        $query .= "                                t1.semester    = '" .$term. "' AND";
        $query .= "                                t2.year        = t1.year AND";
        $query .= "                                t2.semester    = t1.semester AND";
        $query .= "                                t2.groupcd     = t1.groupcd AND";
        $query .= "                                t2.chaircd     = t1.chaircd";
        $query .= "                       ) T3 ON T1.chaircd = T3.chaircd ";
        $query .= "       LEFT JOIN schreg_regd_hdat T4 ON T3.year = T4.year AND T3.semester = T4.semester AND T3.trgtgrade = T4.grade AND T3.trgtclass = T4.hr_class ";
        $query .= "       LEFT JOIN SCH_STF_DAT T5 ON T1.PERIODCD = T5.PERIODCD AND T1.CHAIRCD = T5.CHAIRCD AND T5.executedate = DATE('".$model->cntl_dt_key."') ";    //2006.02.02 alp m-yama
        $query .= "       LEFT JOIN SCH_CHR_COUNTFLG T6 ON T6.EXECUTEDATE = '".$model->cntl_dt_key."' ";
        $query .= "            AND T1.PERIODCD = T6.PERIODCD ";
        $query .= "            AND T1.CHAIRCD = T6.CHAIRCD ";
        $query .= "            AND T3.TRGTGRADE = T6.GRADE ";
        $query .= "            AND T3.TRGTCLASS = T6.HR_CLASS ";
        $query .= "       LEFT JOIN NAME_MST N1 ON N1.NAMECD1 = 'B002' ";
        $query .= "            AND T6.LESSON_MODE = N1.NAMECD2 ";
        $query .= "       LEFT JOIN TEST_COUNTFLG TEST ";
        $query .= "           ON  TEST.EXECUTEDATE = DATE('".$model->cntl_dt_key."') ";
        $query .= "           AND TEST.PERIODCD    = T1.PERIODCD ";
        $query .= "           AND TEST.CHAIRCD     = T1.CHAIRCD ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= " WHERE T1.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind), "','")."') ";
            }
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " WHERE T1.SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= "  )";
        $query .= "  SELECT";
        $query .= "      periodcd,";
        $query .= "      chaircd,";
        $query .= "      groupcd,";
        $query .= "      attendcd,";
        $query .= "      chargediv,";
        $query .= "      staffcd,";
        $query .= "      TARGETCLASS,";
        if ($model->Properties["useCurriculumcd"] == "1") {
            $query .= "      CLASSCD,";
            $query .= "      SCHOOL_KIND,";
            $query .= "      CURRICULUM_CD,";
        }
        $query .= "      SUBCLASSCD,";
        $query .= "      subclassabbv,";
        $query .= "      CHAIRNAME,";
        $query .= "      MAX(VALUE(COUNTFLG, '1')) AS COUNTFLG, "; //0：集計しない 0以外：集計する
        $query .= "      MAX(VALUE(LESSON_MODE, '0')) AS LESSON_MODE ";
        $query .= "  FROM";
        $query .= "      MAIN";
        if ($model->Properties["knjc020RestrictedShowLoginStaffOnly"] == '1') {
            if (AUTHORITY == DEF_REFER_RESTRICT|| AUTHORITY == DEF_UPDATE_RESTRICT) {
                $query .= " WHERE ";
                $query .= "     staffcd = '".STAFFCD."' ";
            }
        }
        $query .= "  GROUP BY";
        $query .= "      periodcd,";
        $query .= "      chaircd,";
        $query .= "      groupcd,";
        $query .= "      attendcd,";
        $query .= "      chargediv,";
        $query .= "      staffcd,";
        $query .= "      TARGETCLASS,";
        if ($model->Properties["useCurriculumcd"] == "1") {
            $query .= "      CLASSCD,";
            $query .= "      SCHOOL_KIND,";
            $query .= "      CURRICULUM_CD,";
        }
        $query .= "      SUBCLASSCD,";
        $query .= "      subclassabbv,";
        $query .= "      CHAIRNAME,";
        $query .= "      trgtgrade,";
        $query .= "      trgtclass";
        $query .= "  ORDER BY";
        $query .= "      staffcd,";
        $query .= "      periodcd,";
        $query .= "      chargediv,";
        $query .= "      chaircd,";
        $query .= "      trgtgrade || trgtclass || TARGETCLASS";    //NO002

        //echo $query;
        return $query;
    }

    public function getAttendCntAll($model)
    {
        $query  = " SELECT ";
        $query .= "     PERIODCD, ";
        $query .= "     CHAIRCD, ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     ATTEND_DAT ";
        $query .= " WHERE ";
        $query .= "     ATTENDDATE = '{$model->cntl_dt_key}' ";
        $query .= " GROUP BY ";
        $query .= "     PERIODCD, ";
        $query .= "     CHAIRCD ";
        return $query;
    }

    public function getAttendCnt($model, $periodcd, $chaircd)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     ATTEND_DAT ";
        $query .= " WHERE ";
        $query .= "     ATTENDDATE = '{$model->cntl_dt_key}' ";
        $query .= "     AND PERIODCD = '{$periodcd}' ";
        $query .= "     AND CHAIRCD = '{$chaircd}' ";
        return $query;
    }

    public function getAttendCnt2($model)
    {
        $query  = " SELECT ";
        $query .= "     PERIODCD, CHAIRCD, COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     ATTEND_DAT ";
        $query .= " WHERE ";
        $query .= "     ATTENDDATE = '{$model->cntl_dt_key}' ";
        $query .= " GROUP BY ";
        $query .= "     PERIODCD, CHAIRCD ";
        return $query;
    }

    public function getLessonName($lessonMode)
    {
        $query  = " SELECT ";
        $query .= "     ABBV1 ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = 'B002' ";
        $query .= "     AND NAMECD2 = '".$lessonMode."' ";

        return $query;
    }

    public function getLessonName2()
    {
        $query  = " SELECT ";
        $query .= "     NAMECD2, ABBV1 ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "     NAMECD1 = 'B002' ";

        return $query;
    }

    //データを移動
    public function keyMoverQuery($key, $move_key = "")
    {
        $handle_end = "";

        switch ($move_key) {
            case "next":
                $max_min = " MIN";
                $handle = " > ";
                break;
            case "before":
                $max_min = " MAX";
                $handle = " < ";
                break;
            default:
                $max_min = " MIN";
                $handle = " BETWEEN ";
                $handle_end = " AND DATE('".(CTRL_YEAR + 1)."-3-31') ";
                break;
        }

        $query  = "SELECT ";
        $query  .= $max_min."(EXECUTEDATE) AS EXECUTEDATE ";
        $query  .= "FROM ";
        $query  .= " SCH_CHR_DAT ";
        $query  .= "WHERE ";
        $query  .= " EXECUTEDATE ".$handle." DATE('".$key."') ";
        $query  .= $handle_end ;

        $db  = Query::dbCheckOut();
        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);

        return $row["EXECUTEDATE"];
    }

    //表示項目の取得
    public function getDispCol()
    {
        $query  = " SELECT COALESCE(MAX(NAMECD2),'1') AS DISP_COL  ";
        $query .= " FROM V_NAME_MST ";
        $query .= " WHERE YEAR    = '".CTRL_YEAR."' AND ";
        $query .= "       NAMECD1 = 'C000' ";
        return $query;
    }

#    function getPath(){
#        return " SELECT PROGRAMPATH FROM MENU_MST WHERE PROGRAMID = 'KNJC010' ";
#    }
}
