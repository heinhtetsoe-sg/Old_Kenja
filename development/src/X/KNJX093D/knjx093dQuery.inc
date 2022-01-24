<?php

require_once('for_php7.php');

class knjx093dquery extends Query
{
    //年度・学期取得
    public function getYearSemester()
    {
        $query  = " SELECT ";
        $query .= "     YEAR || '-' || SEMESTER AS VALUE, ";
        $query .= "     YEAR || '年度　' || SEMESTERNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     SEMESTER_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR IN ('".CTRL_YEAR."','".(CTRL_YEAR + 1)."') AND ";
        $query .= "     SEMESTER <> '9' ";
        $query .= " ORDER BY ";
        $query .= "     YEAR DESC, ";
        $query .= "     SEMESTER ";

        return $query;
    }

    //学年・コース取得
    public function getGradeCourse($model)
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     T1.GRADE || '-' || T1.COURSECD || '-' || T1.MAJORCD || '-' || T1.COURSECODE AS VALUE, ";
        $query .= "     T1.GRADE, ";
        $query .= "     T2.GRADE_NAME1, ";
        $query .= "     T1.COURSECD || T1.MAJORCD AS COURSE_MAJORCD, ";
        $query .= "     T3.COURSENAME || T3.MAJORNAME AS COURSE_MAJORNAME, ";
        $query .= "     T1.COURSECODE, ";
        $query .= "     T4.COURSECODENAME ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1, ";
        $query .= "     SCHREG_REGD_GDAT T2, ";
        $query .= "     V_COURSE_MAJOR_MST T3, ";
        $query .= "     V_COURSECODE_MST T4 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR || '-' || T1.SEMESTER = '".$model->field["YEAR_SEMESTER"]."' AND ";
        $query .= "     T1.YEAR     = T2.YEAR AND ";
        $query .= "     T1.YEAR     = T3.YEAR AND ";
        $query .= "     T1.YEAR     = T4.YEAR AND ";
        $query .= "     T1.GRADE    = T2.GRADE AND ";
        $query .= "     T1.COURSECD = T3.COURSECD AND ";
        $query .= "     T1.MAJORCD  = T3.MAJORCD AND ";
        $query .= "     T1.COURSECODE = T4.COURSECODE ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= " AND T2.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind), "','")."') ";
            }
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND T2.SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }
    //エラーＤＢへの追加
    public function insertQueryErr(&$db, $record_no, $check_error)
    {
        $data1 = array();
        $data1["PROGRAMID"][TEXT] = PROGRAMID;
        $data1["MSGROW"][NUMBER]  = $record_no;
        $data1["MSGREMARK"][TEXT] = $check_error;

        $query = Query::insertSQL($data1, "W_CSVMSG_PRG_DAT");
        $result = $db->query($query);
    }

    /**************/
    /* ＣＳＶ出力 */
    /**************/
    //ヘッダー用データ取得
    public function getHeaderData($model)
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     T1.GROUPCD, ";
        $query .= "     L1.GROUPNAME, ";
        $query .= "     T1.CHAIRCD, ";
        $query .= "     T1.CHAIRNAME ";
        $query .= " FROM ";
        $query .= "     CHAIR_DAT T1 ";
        $query .= "     LEFT JOIN ELECTCLASS_DAT L1 ON T1.YEAR = L1.YEAR AND T1.GROUPCD = L1.GROUPCD, ";
        $query .= "     CHAIR_CLS_DAT T2 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR     = T2.YEAR AND ";
        $query .= "     T1.SEMESTER = T2.SEMESTER AND ";
        $query .= "     T1.YEAR || '-' || T1.SEMESTER = '".$model->field["YEAR_SEMESTER"]."' AND ";
        $query .= "     T1.GROUPCD  = T2.GROUPCD AND ";
        $query .= "     T1.GROUPCD != '0000' AND ";
        $query .= "     T2.CHAIRCD  = '0000000' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= " AND T1.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind), "','")."') ";
                $query .= " AND T2.TRGTGRADE IN (SELECT ";
                $query .= "                         G1.GRADE ";
                $query .= "                     FROM ";
                $query .= "                         SCHREG_REGD_GDAT G1 ";
                $query .= "                     WHERE ";
                $query .= "                         G1.YEAR         = T2.YEAR AND ";
                $query .= "                         G1.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind), "','")."') ";
                $query .= "                     ) ";
            }
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND T1.SCHOOL_KIND = '".SCHOOLKIND."' ";
            $query .= " AND T2.TRGTGRADE IN (SELECT ";
            $query .= "                         G1.GRADE ";
            $query .= "                     FROM ";
            $query .= "                         SCHREG_REGD_GDAT G1 ";
            $query .= "                     WHERE ";
            $query .= "                         G1.YEAR         = T2.YEAR AND ";
            $query .= "                         G1.SCHOOL_KIND  = '".SCHOOLKIND."' ";
            $query .= "                     ) ";
        }
        $query .= " ORDER BY ";
        $query .= "     T1.GROUPCD, ";
        $query .= "     T1.CHAIRCD ";

        return $query;
    }

    //生徒一覧取得
    public function getSchList($model)
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     T1.YEAR, ";
        $query .= "     T1.SEMESTER, ";
        $query .= "     T1.GRADE, ";
        $query .= "     T1.HR_CLASS, ";
        $query .= "     T1.ATTENDNO, ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     L1.NAME, ";
        $query .= "     T1.COURSECD || '-' || T1.MAJORCD || '-' || T1.COURSECODE AS COURSE, ";
        $query .= "     L2.MAJORNAME || L3.COURSECODENAME AS COURSENAME, ";
        $query .= "     L4.SDATE AS APPDATE ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1 ";
        $query .= "     INNER JOIN SCHREG_BASE_MST L1 ";
        $query .= "              ON T1.SCHREGNO     = L1.SCHREGNO ";
        $query .= "     LEFT JOIN V_COURSE_MAJOR_MST L2 ";
        $query .= "              ON T1.YEAR         = L2.YEAR ";
        $query .= "             AND T1.COURSECD     = L2.COURSECD ";
        $query .= "             AND T1.MAJORCD      = L2.MAJORCD ";
        $query .= "     LEFT JOIN V_COURSECODE_MST L3 ";
        $query .= "              ON T1.YEAR         = L3.YEAR ";
        $query .= "             AND T1.COURSECODE   = L3.COURSECODE ";
        $query .= "     LEFT JOIN SEMESTER_MST L4 ";
        $query .= "              ON T1.YEAR         = L4.YEAR ";
        $query .= "             AND T1.SEMESTER     = L4.SEMESTER ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR || '-' || T1.SEMESTER = '".$model->field["YEAR_SEMESTER"]."' AND ";
        $query .= "     T1.GRADE || '-' || T1.COURSECD || '-' || T1.MAJORCD || '-' || T1.COURSECODE = '".$model->field["GRADE_COURSE"]."' ";
        $query .= " ORDER BY ";
        $query .= "     T1.GRADE, ";
        $query .= "     T1.HR_CLASS, ";
        $query .= "     T1.ATTENDNO, ";
        $query .= "     T1.SCHREGNO ";

        return $query;
    }

    //CHAIR_STD_DAT取得
    public function getChairStdDat($year_sem)
    {
        $query  = " SELECT ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     T1.CHAIRCD, ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     CHAIR_STD_DAT T1, ";
        $query .= "     SEMESTER_MST T2 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR     = T2.YEAR AND ";
        $query .= "     T1.SEMESTER = T2.SEMESTER AND ";
        $query .= "     T1.YEAR || '-' || T1.SEMESTER = '".$year_sem."' AND ";
        $query .= "     T1.APPENDDATE = T2.EDATE ";
        $query .= " GROUP BY ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     T1.CHAIRCD ";

        return $query;
    }

    /******************/
    /* エラーチェック */
    /******************/
    //存在チェック(群コード)
    public function checkGroupcd($year, $semester, $groupcd)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     CHAIR_DAT T1, ";
        $query .= "     CHAIR_CLS_DAT T2 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR     = T2.YEAR AND ";
        $query .= "     T1.YEAR     = '".$year."' AND ";
        $query .= "     T1.SEMESTER = T2.SEMESTER AND ";
        $query .= "     T1.SEMESTER = '".$semester."' AND ";
        $query .= "     T1.GROUPCD  = T2.GROUPCD AND ";
        $query .= "     T1.GROUPCD  = '".$groupcd."' ";

        return $query;
    }

    //存在チェック(講座コード)
    public function checkChaircd($model, $year, $semester, $groupcd, $chaircd)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     CHAIR_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR        = '".$year."' AND ";
        $query .= "     SEMESTER    = '".$semester."' AND ";
        $query .= "     GROUPCD     = '".$groupcd."' AND ";
        $query .= "     CHAIRCD     = '".$chaircd."' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= " AND SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind), "','")."') ";
            }
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND SCHOOL_KIND = '".SCHOOLKIND."' ";
        }

        return $query;
    }

    //名簿の終了日が学期最終日の講座を取得
    public function getChaircd($model, $year_sem, $schregno)
    {
        $query  = " SELECT ";
        $query .= "     T1.CHAIRCD, ";
        $query .= "     T1.APPDATE ";
        $query .= " FROM ";
        $query .= "     CHAIR_STD_DAT T1, ";
        $query .= "     SEMESTER_MST T2 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR     = T2.YEAR AND ";
        $query .= "     T1.SEMESTER = T2.SEMESTER AND ";
        $query .= "     T1.YEAR || '-' || T1.SEMESTER = '".$year_sem."' AND ";
        $query .= "     T1.SCHREGNO = '".$schregno."' AND ";
        $query .= "     T1.APPENDDATE = T2.EDATE ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= " AND T1.CHAIRCD IN ( SELECT ";
                $query .= "                         S1.CHAIRCD ";
                $query .= "                     FROM ";
                $query .= "                         CHAIR_DAT S1 ";
                $query .= "                     WHERE ";
                $query .= "                         S1.YEAR         = T1.YEAR AND ";
                $query .= "                         S1.SEMESTER     = T1.SEMESTER AND ";
                $query .= "                         S1.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind), "','")."') ";
                $query .= "                    ) ";
            }
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND T1.CHAIRCD IN ( SELECT ";
            $query .= "                         S1.CHAIRCD ";
            $query .= "                     FROM ";
            $query .= "                         CHAIR_DAT S1 ";
            $query .= "                     WHERE ";
            $query .= "                         S1.YEAR         = T1.YEAR AND ";
            $query .= "                         S1.SEMESTER     = T1.SEMESTER AND ";
            $query .= "                         S1.SCHOOL_KIND  = '".SCHOOLKIND."' ";
            $query .= "                    ) ";
        }

        return $query;
    }

    //変更可能な日付範囲取得
    public function getDateRange($year, $semester, $schregno)
    {
        $query  = " WITH SEM AS ( ";
        $query .= "     SELECT ";
        $query .= "         * ";
        $query .= "     FROM ";
        $query .= "         SEMESTER_MST ";
        $query .= "     WHERE ";
        $query .= "         YEAR     = '".$year."' AND ";
        $query .= "         SEMESTER = '".$semester."' ";
        $query .= " ), CHAIR_STD AS ( ";
        $query .= "     SELECT ";
        $query .= "         T1.* ";
        $query .= "     FROM ";
        $query .= "         CHAIR_STD_DAT T1, ";
        $query .= "         CHAIR_DAT T2 ";
        $query .= "     WHERE ";
        $query .= "         T1.YEAR     = '".$year."' AND ";
        $query .= "         T1.SEMESTER = '".$semester."' AND ";
        $query .= "         T1.SCHREGNO = '".$schregno."' AND ";
        $query .= "         T1.YEAR     = T2.YEAR AND ";
        $query .= "         T1.SEMESTER = T2.SEMESTER AND ";
        $query .= "         T1.CHAIRCD  = T2.CHAIRCD AND ";
        $query .= "         T2.GROUPCD != '0000' AND ";
        $query .= "         T1.APPDATE  = ( SELECT ";
        $query .= "                             MAX(APPDATE) AS APPDATE ";
        $query .= "                         FROM ";
        $query .= "                             CHAIR_STD_DAT S1 ";
        $query .= "                         WHERE ";
        $query .= "                             T1.YEAR     = S1.YEAR AND ";
        $query .= "                             T1.SEMESTER = S1.SEMESTER AND ";
        $query .= "                             T1.CHAIRCD  = S1.CHAIRCD AND ";
        $query .= "                             T1.SCHREGNO = S1.SCHREGNO ";
        $query .= "                         ) ";
        $query .= " ), SCH_CHR AS ( ";
        $query .= "     SELECT ";
        $query .= "         CHAIRCD, ";
        $query .= "         EXECUTEDATE ";
        $query .= "     FROM ";
        $query .= "         SCH_CHR_DAT ";
        $query .= "     WHERE ";
        $query .= "         YEAR        = '".$year."' AND ";
        $query .= "         SEMESTER    = '".$semester."' AND ";
        $query .= "         EXECUTED    = '1' ";
        $query .= "     UNION  ";
        $query .= "     SELECT ";
        $query .= "         T1.CHAIRCD, ";
        $query .= "         T2.EXECUTEDATE ";
        $query .= "     FROM ";
        $query .= "         SCH_CHR_DAT T1, ";
        $query .= "         SCH_CHR_HRATE_DAT T2 ";
        $query .= "     WHERE ";
        $query .= "         T1.YEAR         = '".$year."' AND ";
        $query .= "         T1.SEMESTER     = '".$semester."' AND ";
        $query .= "         T1.CHAIRCD      = T2.CHAIRCD AND ";
        $query .= "         T1.EXECUTEDATE  = T2.EXECUTEDATE AND ";
        $query .= "         T1.PERIODCD     = T2.PERIODCD AND ";
        $query .= "         T2.EXECUTED     = '1' AND ";
        $query .= "         EXISTS (SELECT ";
        $query .= "                     'X' ";
        $query .= "                 FROM ";
        $query .= "                     SCHREG_REGD_DAT S1 ";
        $query .= "                 WHERE ";
        $query .= "                     T1.YEAR     = S1.YEAR AND ";
        $query .= "                     T1.SEMESTER = S1.SEMESTER AND ";
        $query .= "                     T2.GRADE    = S1.GRADE AND ";
        $query .= "                     T2.HR_CLASS = S1.HR_CLASS AND ";
        $query .= "                     S1.SCHREGNO = '".$schregno."' ";
        $query .= "                 ) ";
        $query .= " ), ATTEND AS ( ";
        $query .= "     SELECT ";
        $query .= "         CHAIRCD, ";
        $query .= "         MAX(EXECUTEDATE) AS EXECUTEDATE ";
        $query .= "     FROM ";
        $query .= "         SCH_CHR ";
        $query .= "     GROUP BY ";
        $query .= "         CHAIRCD ";
        $query .= " ), SDATELIST AS ( ";
        $query .= "     SELECT ";
        $query .= "         T2.CHAIRCD, ";
        $query .= "         T1.SDATE ";
        $query .= "     FROM ";
        $query .= "         SEM T1, ";
        $query .= "         CHAIR_DAT T2 ";
        $query .= "     WHERE ";
        $query .= "         T1.YEAR     = T2.YEAR AND ";
        $query .= "         T1.SEMESTER = T2.SEMESTER AND ";
        $query .= "         T2.GROUPCD != '0000' ";
        $query .= "     UNION    ";
        $query .= "     SELECT ";
        $query .= "         T1.CHAIRCD, ";
        $query .= "         CASE WHEN T2.EDATE != T1.APPENDDATE THEN (T1.APPENDDATE +1 DAY) ";
        $query .= "              ELSE T1.APPDATE END AS SDATE ";
        $query .= "     FROM ";
        $query .= "         CHAIR_STD T1, ";
        $query .= "         SEM T2 ";
        $query .= "     WHERE ";
        $query .= "         T1.YEAR     = T2.YEAR AND ";
        $query .= "         T1.SEMESTER = T2.SEMESTER ";
        $query .= "     UNION       ";
        $query .= "     SELECT CHAIRCD, (EXECUTEDATE +1 DAY) AS SDATE FROM ATTEND ";
        $query .= " ), MAX_SDATE AS ( ";
        $query .= "     SELECT ";
        $query .= "         CHAIRCD, ";
        $query .= "         MAX(SDATE) AS SDATE ";
        $query .= "     FROM ";
        $query .= "         SDATELIST ";
        $query .= "     GROUP BY ";
        $query .= "         CHAIRCD ";
        $query .= " ) ";

        $query .= " SELECT DISTINCT ";
        $query .= "     T2.CHAIRCD, ";
        $query .= "     T2.SDATE, ";
        $query .= "     T1.EDATE ";
        $query .= " FROM ";
        $query .= "     SEM T1, ";
        $query .= "     MAX_SDATE T2 ";
        $query .= " ORDER BY ";
        $query .= "     T2.CHAIRCD, ";
        $query .= "     T2.SDATE, ";
        $query .= "     T1.EDATE ";

        return $query;
    }

    //学籍番号取得
    public function getSchregno($model, $year, $semester)
    {
        $query  = " SELECT ";
        $query .= "     SCHREGNO ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1 ";
        $query .= " WHERE ";
        $query .= "     YEAR        = '".$year."' AND ";
        $query .= "     SEMESTER    = '".$semester."' ";
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

        return $query;
    }

    //学期最終日取得
    public function getSemDate($year, $semester)
    {
        $query  = " SELECT ";
        $query .= "     SDATE, ";
        $query .= "     EDATE ";
        $query .= " FROM ";
        $query .= "     SEMESTER_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR     = '".$year."' AND ";
        $query .= "     SEMESTER = '".$semester."' ";

        return $query;
    }

    /**************/
    /* ＣＳＶ取込 */
    /**************/
    //更新処理
    public function insertQueryCsv($model, $data_arrH, $data_arr)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $cnt = $totalcnt = 0;   //件数
        for ($i = 0; $i < get_count($data_arr); $i++) {
            //学期の開始日・終了日取得
            $seminfo = $db->getRow(knjx093dQuery::getSemDate($data_arr[$i]["YEAR"], $data_arr[$i]["SEMESTER"]), DB_FETCHMODE_ASSOC);

            //名簿に登録された講座取得
            $regd_chaircd = $regd_chrAppdate = array();
            $year_sem = $data_arr[$i]["YEAR"].'-'.$data_arr[$i]["SEMESTER"];
            $result = $db->query(knjx093dQuery::getChaircd($model, $year_sem, $data_arr[$i]["SCHREGNO"]));
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $regd_chaircd[] = $row["CHAIRCD"];
                $regd_chrAppdate[$row["CHAIRCD"]] = $row["APPDATE"];
            }
            $result->free();

            $subcnt = 0;  //件数
            for ($j = 0; $j < get_count($data_arrH["CHAIRCD"]); $j++) {
                //OFF→ONに変更があった講座
                if ($data_arr[$i]["FLG"][$j] == "1" && !in_array($data_arrH["CHAIRCD"][$j], $regd_chaircd)) {
                    //追加
                    $data = array();
                    $data["YEAR"][TEXT]         = $data_arr[$i]["YEAR"];
                    $data["SEMESTER"][TEXT]     = $data_arr[$i]["SEMESTER"];
                    $data["CHAIRCD"][TEXT]      = $data_arrH["CHAIRCD"][$j];
                    $data["SCHREGNO"][TEXT]     = $data_arr[$i]["SCHREGNO"];
                    $data["APPDATE"][TEXT]      = $data_arr[$i]["APPDATE"];
                    $data["APPENDDATE"][TEXT]   = $seminfo["EDATE"];
                    $data["REGISTERCD"][TEXT]   = STAFFCD;
                    $data["UPDATED"][NUMBER]    = "SYSDATE()";

                    $query = Query::insertSQL($data, "CHAIR_STD_DAT");
                    $db->query($query);

                    $subcnt++;

                //ON→OFFに変更があった講座
                } elseif ($data_arr[$i]["FLG"][$j] != "1" && in_array($data_arrH["CHAIRCD"][$j], $regd_chaircd)) {
                    //指定開始日が学期開始日または講座開始日と同じとき削除する
                    if (($data_arr[$i]["APPDATE"] == $seminfo["SDATE"]) || ($data_arr[$i]["APPDATE"] == $regd_chrAppdate[$data_arrH["CHAIRCD"][$j]])) {
                        $query  = " DELETE FROM ";
                        $query .= "     CHAIR_STD_DAT ";
                        $query .= " WHERE ";
                        $query .= "     YEAR        = '".$data_arr[$i]["YEAR"]."' AND ";
                        $query .= "     SEMESTER    = '".$data_arr[$i]["SEMESTER"]."' AND ";
                        $query .= "     CHAIRCD     = '".$data_arrH["CHAIRCD"][$j]."' AND ";
                        $query .= "     SCHREGNO    = '".$data_arr[$i]["SCHREGNO"]."' AND ";
                        $query .= "     APPDATE     = '".$data_arr[$i]["APPDATE"]."' ";
                        $db->query($query);
                    } else {
                        //更新
                        $data = array();
                        $data["APPENDDATE"][TEXT]   = date("Y-m-d", strtotime("{$data_arr[$i]["APPDATE"]} -1 day"));
                        $data["REGISTERCD"][TEXT]   = STAFFCD;
                        $data["UPDATED"][NUMBER]    = "SYSDATE()";

                        $where  = " WHERE ";
                        $where .= "     YEAR        = '".$data_arr[$i]["YEAR"]."' AND ";
                        $where .= "     SEMESTER    = '".$data_arr[$i]["SEMESTER"]."' AND ";
                        $where .= "     CHAIRCD     = '".$data_arrH["CHAIRCD"][$j]."' AND ";
                        $where .= "     SCHREGNO    = '".$data_arr[$i]["SCHREGNO"]."' AND ";
                        $where .= "     APPENDDATE  = '".$seminfo["EDATE"]."' ";

                        $query = Query::updateSQL($data, "CHAIR_STD_DAT", $where);
                        $db->query($query);
                    }
                    $subcnt++;
                }
            }
            if ($subcnt > 0) {
                $cnt++;
            }
            $totalcnt++;
        }
        $db->commit();
        Query::dbCheckIn($db);

        return $cnt.' / '.$totalcnt;
    }
}
