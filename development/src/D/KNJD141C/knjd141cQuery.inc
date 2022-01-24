<?php

require_once('for_php7.php');


class knjd141cQuery extends Query
{

    //満点で偏差値計算を判別（1人でも科目の満点が複数ある場合、合算科目の偏差値は、各科目の偏差値の平均を算出する）
    public function getPerfectCnt($model)
    {
        $query  = "";
        $query .= " SELECT ";
        $query .= "     COUNT(SCHREGNO) ";
        $query .= " FROM ";
        $query .= "     ( ";
        $query .= "     SELECT ";
        $query .= "         T1.SCHREGNO, ";
        $query .= "         COUNT(DISTINCT VALUE(L1.PERFECT, 100)) AS PERFECT_CNT ";
        $query .= "     FROM ";
        $query .= "         RECORD_SCORE_DAT T1 ";
        $query .= "         INNER JOIN CLASS_YDAT CYDAT ON CYDAT.YEAR = T1.YEAR ";
        $query .= "             AND CYDAT.CLASSCD = T1.CLASSCD ";
        $query .= "             AND CYDAT.SCHOOL_KIND = T1.SCHOOL_KIND ";
        $query .= "         INNER JOIN SUBCLASS_YDAT SYDAT ON SYDAT.YEAR = T1.YEAR ";
        $query .= "             AND SYDAT.CLASSCD = T1.CLASSCD ";
        $query .= "             AND SYDAT.SCHOOL_KIND = T1.SCHOOL_KIND ";
        $query .= "             AND SYDAT.CURRICULUM_CD = T1.CURRICULUM_CD ";
        $query .= "             AND SYDAT.SUBCLASSCD = T1.SUBCLASSCD ";
        $query .= "         INNER JOIN SCHREG_REGD_DAT T2 ON T2.SCHREGNO = T1.SCHREGNO ";
        $query .= "             AND T2.YEAR = '".CTRL_YEAR."' ";
        $query .= "             AND T2.SEMESTER = '".$model->seme_sch."' ";
        $query .= "             AND T2.GRADE = '".$model->grade."' ";
        //指定された課程学科の生徒
        if ($model->Properties["use_school_detail_gcm_dat"] == '1') {
            $query .= "             AND T2.COURSECD || '-' || T2.MAJORCD = '".$model->course_major."' ";
        }
        $query .= "         LEFT JOIN PERFECT_RECORD_SDIV_DAT L1 ON L1.YEAR = T1.YEAR ";
        $query .= "             AND L1.SEMESTER = T1.SEMESTER ";
        $query .= "             AND L1.TESTKINDCD = T1.TESTKINDCD ";
        $query .= "             AND L1.TESTITEMCD = T1.TESTITEMCD ";
        $query .= "             AND L1.SCORE_DIV = T1.SCORE_DIV ";
        $query .= "             AND L1.CLASSCD = T1.CLASSCD ";
        $query .= "             AND L1.SCHOOL_KIND = T1.SCHOOL_KIND ";
        $query .= "             AND L1.CURRICULUM_CD = T1.CURRICULUM_CD ";
        $query .= "             AND L1.SUBCLASSCD = T1.SUBCLASSCD ";
        $query .= "             AND L1.GRADE = CASE WHEN L1.DIV = '01' THEN '00' ELSE T2.GRADE END ";
        $query .= "             AND L1.COURSECD || L1.MAJORCD || L1.COURSECODE = CASE WHEN L1.DIV IN ('01','02') THEN '00000000' ELSE T2.COURSECD || T2.MAJORCD || T2.COURSECODE END ";
        $query .= "     WHERE ";
        $query .= "         T1.YEAR = '".CTRL_YEAR."' ";
        $query .= "         AND T1.SEMESTER = '".$model->seme."' ";
        $query .= "         AND T1.TESTKINDCD = '".$model->kind."' ";
        $query .= "         AND T1.TESTITEMCD = '".$model->item."' ";
        $query .= "         AND T1.SCORE_DIV = '".$model->sdiv."' ";
        $query .= "     GROUP BY ";
        $query .= "         T1.SCHREGNO ";
        $query .= "     ) M1 ";
        $query .= " WHERE ";
        $query .= "     PERFECT_CNT > 1 ";

        $db = Query::dbCheckOut();
        $rtnCnt = $db->getOne($query);
        Query::dbCheckIn($db);
        return $rtnCnt;
    }

    //存在チェック（在籍データ）
    public function checktoSchregno($model)
    {
        $db = Query::dbCheckOut();

        $query  = " SELECT COUNT(*) ";
        $query .= " FROM   SCHREG_REGD_DAT ";
        $query .= " WHERE  YEAR='".CTRL_YEAR."' AND SEMESTER='".$model->seme_sch."' AND GRADE='".$model->grade."' ";
        $query .= "    AND HR_CLASS IS NOT NULL ";
        $query .= "    AND COURSECD IS NOT NULL ";
        $query .= "    AND MAJORCD IS NOT NULL ";
        $query .= "    AND COURSECODE IS NOT NULL ";

        $ret_val = $db->getOne($query);

        Query::dbCheckIn($db);
        return $ret_val;
    }

    //存在チェック（成績データ）
    public function checktoRecordScore($model)
    {
        $db = Query::dbCheckOut();

        $query  = " SELECT COUNT(*) ";
        $query .= " FROM   RECORD_SCORE_DAT T1 ";
        $query .= "        INNER JOIN CLASS_YDAT CYDAT ON CYDAT.YEAR = T1.YEAR ";
        $query .= "            AND CYDAT.CLASSCD = T1.CLASSCD ";
        $query .= "            AND CYDAT.SCHOOL_KIND = T1.SCHOOL_KIND ";
        $query .= "        INNER JOIN SUBCLASS_YDAT SYDAT ON SYDAT.YEAR = T1.YEAR ";
        $query .= "            AND SYDAT.CLASSCD = T1.CLASSCD ";
        $query .= "            AND SYDAT.SCHOOL_KIND = T1.SCHOOL_KIND ";
        $query .= "            AND SYDAT.CURRICULUM_CD = T1.CURRICULUM_CD ";
        $query .= "            AND SYDAT.SUBCLASSCD = T1.SUBCLASSCD ";
        $query .= " WHERE ";
        $query .= "        T1.YEAR = '".CTRL_YEAR."' AND ";
        $query .= "        T1.SEMESTER = '".$model->seme."' AND ";
        $query .= "        T1.TESTKINDCD = '".$model->kind."' AND ";
        $query .= "        T1.TESTITEMCD = '".$model->item."' AND ";
        $query .= "        T1.SCORE_DIV = '".$model->sdiv."' ";

        $ret_val = $db->getOne($query);

        Query::dbCheckIn($db);
        return $ret_val;
    }

    // 奈良県で欠課時数超過の生徒を取得する
    // 取得対象の生徒・科目の成績は生徒の合計点・平均点に含めない
    public function getNarakenKekkaOverSchregnoSubclass($model)
    {
        // 奈良県の評価(SCORE_DIV=08)のみ実行する
        if (!($model->schoolName == "naraken" && $model->sdiv == "08")) {
            return "";
        }
        $db = Query::dbCheckOut();

        // 指定学年の校種
        $query = " SELECT SCHOOL_KIND FROM SCHREG_REGD_GDAT WHERE YEAR = '".CTRL_YEAR."' AND GRADE = '".$model->grade."' ";
        $gradeSchoolKind = $db->getOne($query);
        if ($gradeSchoolKind != "H") {
            Query::dbCheckIn($db);
            return "";
        }

        // 指定学年のHR
        $categorySelected = array();
        $query = " SELECT GRADE, HR_CLASS FROM SCHREG_REGD_HDAT WHERE YEAR = '".CTRL_YEAR."' AND SEMESTER = '".CTRL_SEMESTER."' AND GRADE = '".$model->grade."' ";
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $categorySelected[] = $row["GRADE"].$row["HR_CLASS"];
        }

        // 校種フィールドチェック
        $query = " SELECT COLNAME FROM (SELECT COLNAME FROM SYSCAT.COLUMNS WHERE TABNAME = 'V_SCHOOL_MST' AND COLNAME = 'SCHOOL_KIND') ";
        $schoolMstHasSchoolKind = $db->getOne($query);

        $query = " SELECT * FROM V_SCHOOL_MST WHERE YEAR = '".CTRL_YEAR."' ";
        if ($schoolMstHasSchoolKind) {
            $query .= " AND SCHOOL_KIND = '".$gradeSchoolKind."' ";
        }

        $schoolMst = $db->getRow($query, DB_FETCHMODE_ASSOC);
        $isHoutei = $schoolMst["JUGYOU_JISU_FLG"] == "" || $schoolMst["JUGYOU_JISU_FLG"] == "1";

        $query = " SELECT MAX(SEMESTER) FROM SEMESTER_MST WHERE YEAR = '".CTRL_YEAR."' AND SEMESTER <> '9' ";
        $maxSemester = $db->getOne($query);

        $refusalSchregnos = array(); // 不登校生徒学籍番号 (最終学期か学年末のみ)
        $startSeme = ""; // 集計開始日付学期
        $sWeek; // 週数
        if (in_array($model->seme, array($maxSemester, "9"))) {
            // 最終学期か、学年末なら年度開始日から
            $startSeme = "1";
            $sWeek = $schoolMst["JITU_SYUSU"];

            // 不登校生徒
            $query = " SELECT SCHREGNO FROM SCHREG_SCHOOL_REFUSAL_DAT ";
            $query .= " WHERE YEAR = '".CTRL_YEAR."' AND SCHREGNO IN (SELECT SCHREGNO FROM SCHREG_REGD_DAT WHERE YEAR = '".CTRL_YEAR."' AND SEMESTER = '".CTRL_SEMESTER."' AND GRADE = '".$model->grade."') ";
            $refusalSchregnos = $db->getCol($query);
        } else {
            // それ以外は指定学期開始日から
            $startSeme = $model->seme;
            $sWeek = $schoolMst["HOUTEI_SYUSU_SEMESTER".$model->seme];
        }
        $query = " SELECT SDATE FROM SEMESTER_MST WHERE YEAR = '".CTRL_YEAR."' AND SEMESTER = '".$startSeme."' ";
        $sdate = $db->getOne($query);
        Query::dbCheckIn($db);

        if (preg_match('/http/', SERVLET_URL)) {
            $url = SERVLET_URL ."/KNJZ"; // 本番用
        } else {
            $url = (empty($_SERVER["HTTPS"]) ? "http://" : "https://") . $_SERVER["HTTP_HOST"] .SERVLET_URL ."/KNJZ"; // 沖縄確認用
        }

        // 帳票のAttendAccumulateで欠課時数取得
        $data = array('CALL_DEST_JAVA_PRGID' => 'servletpack.KNJZ.detail.dao.AttendAccumulateIO',
                      'METHOD_NAME'     => 'json',
                      'DBNAME'          => DB_DATABASE,
                      'SDATE'           => str_replace('/', '-', $sdate),
                      'DATE'            => str_replace('/', '-', $model->chairdate),
                      'ATTEND_DIV'      => 'ATTEND_SUBCLASS_DAT',
                      'CTRL_YEAR'       => CTRL_YEAR,
                      'CTRL_SEMESTER'   => CTRL_SEMESTER,
                      'GRADE_HR_CLASS'  => implode(",", $categorySelected),
                      'DOCUMENTROOT'    => DOCUMENTROOT
        );

        // use key 'http' even if you send the request to https://...
        $options = array(
            'http' => array(
                'header'  => "Content-type: application/x-www-form-urlencoded\r\n",
                'method'  => 'POST',
                'content' => http_build_query($data)
            )
        );
        $target = array();
        $debug = array();
        $context  = stream_context_create($options);
        $result = file_get_contents($url, false, $context);
        if ($result != false) {
            $arr = json_decode($result, true);
            foreach ($arr as $schregno => $records) {
                foreach ($records as $record) {
                    $jisu = 0;
                    if ($isHoutei) {
                        // 単位マスタ単位と週数から授業時数から算出する
                        if ($record["CREDITS"] && $sWeek) {
                            $jisu = $record["CREDITS"] * $sWeek; // 学期・年間の授業時数
                        }
                    } else {
                        $jisu = $record["LESSON"]; // 日付までの集計時数
                    }
                    if ($jisu != 0) {
                        $jougenchi = 0;
                        if (in_array($schregno, $refusalSchregnos)) {
                            // 不登校生徒の上限値は授業時数 / 2
                            $jougenchi = $jisu / 2;
                        } else {
                            $bunshi = $schoolMst["SYUTOKU_BUNSI"];
                            $bunbo = $schoolMst["SYUTOKU_BUNBO"];
                            if ($bunshi && $bunbo > 0) {
                                // 授業時数 × 修得上限値分子 / 修得上限値分母
                                $jougenchi = $jisu * $bunshi * 1.0 / $bunbo;
                            }
                        }
                        // 上限値超過
                        if ($jougenchi && $jougenchi < $record["SICK2"]) {
                            $target[] = "('".implode("','", explode("-", $record["SUBCLASSCD"]))."', '".$schregno."') ";
                            $debug[] = " schregno ".$schregno." ".$record["SUBCLASSCD"].", jougen ".$jougenchi." ".$record["SICK2"];
                        }
                    }
                }
            }
        }
        //echo "[[debug]] <br>".implode("<br>", $debug);
        if (empty($target)) {
            return "";
        }
        $rtn = implode(", ", $target);
        return $rtn;
    }

    //課程学科コンボ取得
    public function getCourseMajor($model)
    {
        $query  = " SELECT DISTINCT ";
        $query .= "     VCM.COURSECD || VCM.MAJORCD || ':' || VCM.COURSENAME || VCM.MAJORNAME AS LABEL, ";
        $query .= "     VCM.COURSECD || '-' || VCM.MAJORCD AS VALUE ";
        $query .= " FROM ";
        $query .= "     TESTITEM_MST_COUNTFLG_NEW_GCM_SDIV TEST_GCM ";
        $query .= "     INNER JOIN V_COURSE_MAJOR_MST VCM ON TEST_GCM.YEAR = VCM.YEAR ";
        $query .= "           AND TEST_GCM.COURSECD = VCM.COURSECD ";
        $query .= "           AND TEST_GCM.MAJORCD = VCM.MAJORCD ";
        $query .= " WHERE ";
        $query .= "     TEST_GCM.YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND TEST_GCM.SCHOOLCD = '".SCHOOLCD."' ";
        $query .= "     AND TEST_GCM.SCHOOL_KIND = '".SCHOOLKIND."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //処理学期
    public function getSemester($seme = "")
    {
        $query  = " SELECT SEMESTER AS VALUE,SEMESTERNAME AS LABEL,SDATE,EDATE ";
        $query .= " FROM SEMESTER_MST ";
        $query .= " WHERE  YEAR = '".CTRL_YEAR."' ";
        if (strlen($seme)) {
            $query .= "     AND SEMESTER = '{$seme}' ";
        }
        $query .= " ORDER BY VALUE ";
        return $query;
    }

    //処理学年
    public function getGrade($model, $grade = "")
    {
        $query  = " SELECT GRADE AS VALUE, GRADE_NAME1 AS LABEL, SCHOOL_KIND ";
        $query .= " FROM SCHREG_REGD_GDAT ";
        $query .= " WHERE  YEAR = '".CTRL_YEAR."' ";
        if (strlen($grade)) {
            $query .= "     AND GRADE = '{$grade}' ";
        }
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= "     AND SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind), "','")."') ";
            }
        } elseif ($model->Properties["useSchool_KindField"] == "1") {
            $query .= "     AND SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= " ORDER BY VALUE ";
        return $query;
    }

    //処理種別(成績)
    public function getName($model, $seme)
    {
        $query  = "SELECT T1.TESTKINDCD || '-' || T1.TESTITEMCD || '-' || T1.SCORE_DIV AS VALUE,T1.TESTITEMNAME AS LABEL ";
        $query .= "FROM   ";
        if ($model->Properties["use_school_detail_gcm_dat"] == '1') {
            $query .= "     TESTITEM_MST_COUNTFLG_NEW_GCM_SDIV T1 ";
        } else {
            $query .= "     TESTITEM_MST_COUNTFLG_NEW_SDIV T1 ";
        }
        $query .= "WHERE  T1.YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND T1.SEMESTER = '{$seme}' ";
        $query .= "     AND T1.JYORETSU_FLG = '1' "; //1:序列対象
        if ($model->Properties["use_school_detail_gcm_dat"] == '1') {
            $query .= " AND T1.SCHOOLCD = '".SCHOOLCD."' ";
            $query .= " AND T1.SCHOOL_KIND = '".SCHOOLKIND."' ";
            $query .= " AND T1.COURSECD || '-' || T1.MAJORCD = '{$model->course_major}' ";
        }
        $query .= "ORDER BY VALUE ";
        return $query;
    }

    //学校
    public function getNameMstZ010()
    {
        $query  = " SELECT NAME1 FROM NAME_MST WHERE NAMECD1 = 'Z010' AND NAMECD2 = '00' ";
        return $query;
    }

    //合算科目の母集団から除くフラグ 1:合併元除く 2:合併先除く
    public function getNameMstD070($model, $namecd1)
    {
        $testcd = $model->seme.$model->kind.$model->item.$model->sdiv;
        $query  = " SELECT NAMESPARE1 FROM NAME_MST WHERE NAMECD1 = '{$namecd1}' AND NAME1 = '{$testcd}' ";
        return $query;
    }

    //追指導
    public function getSidouInputInf($model)
    {
        $query  = " SELECT ";
        $query .= "     SIDOU_INPUT_INF "; //1:記号 2:得点
        $query .= " FROM ";
        $query .= "     TESTITEM_MST_COUNTFLG_NEW_SDIV ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND SEMESTER = '".$model->seme."' ";
        $query .= "     AND TESTKINDCD = '".$model->kind."' ";
        $query .= "     AND TESTITEMCD = '".$model->item."' ";
        $query .= "     AND SCORE_DIV = '".$model->sdiv."' ";
        return $query;
    }

    //処理科目
    public function getSubclasscd($model, $school_kind)
    {
        $year = CTRL_YEAR;
        $seme = ($model->seme != "9") ? $model->seme : CTRL_SEMESTER;
        $staffcd = STAFFCD;

        $query  = " SELECT ";
        $query .= "     CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD || ':' || SUBCLASSNAME AS LABEL, ";
        $query .= "     CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD AS VALUE ";
        $query .= " FROM ";
        $query .= "     V_SUBCLASS_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$year}' ";
        $query .= "     AND SCHOOL_KIND = '{$school_kind}' ";
        //更新可能(制限付)
        if (AUTHORITY != DEF_UPDATABLE) {
            $query .= "     AND CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD IN ( ";
            $query .= "         SELECT ";
            $query .= "             T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD ";
            $query .= "         FROM ";
            $query .= "             CHAIR_DAT T1, ";
            $query .= "             CHAIR_STD_DAT T2, ";
            $query .= "             CHAIR_STF_DAT T4 ";
            $query .= "         WHERE ";
            $query .= "                 T1.YEAR       = '{$year}' ";
            $query .= "             AND T1.SEMESTER   = '{$seme}' ";
            $query .= "             AND SUBSTR(T1.SUBCLASSCD, 1 ,2) <= '90' ";
            $query .= "             AND T2.YEAR       = T1.YEAR ";
            $query .= "             AND T2.SEMESTER   = T1.SEMESTER ";
            $query .= "             AND T2.CHAIRCD    = T1.CHAIRCD ";
            $query .= "             AND T4.YEAR       = T1.YEAR ";
            $query .= "             AND T4.SEMESTER   = T1.SEMESTER ";
            $query .= "             AND T4.CHAIRCD    = T1.CHAIRCD ";
            $query .= "             AND T4.STAFFCD    = '{$staffcd}' "; //ログインした先生
            //指定された課程学科の生徒
            if ($model->Properties["use_school_detail_gcm_dat"] == '1') {
                $query .= "         AND T2.SCHREGNO IN ( ";
                $query .= "             SELECT W2.SCHREGNO ";
                $query .= "               FROM SCHREG_REGD_DAT W2 ";
                $query .= "              WHERE W2.YEAR = '{$year}' ";
                $query .= "                AND W2.SEMESTER = '{$seme}' ";
                $query .= "                AND W2.COURSECD || '-' || W2.MAJORCD = '{$model->course_major}' ";
                $query .= "         ) ";
            }
            $query .= "         GROUP BY ";
            $query .= "             T1.CLASSCD, ";
            $query .= "             T1.SCHOOL_KIND, ";
            $query .= "             T1.CURRICULUM_CD, ";
            $query .= "             T1.SUBCLASSCD ";
            $query .= "     ) ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }


    //履歴一覧
    public function getListRireki($model)
    {
        $year = CTRL_YEAR;
        $query  = " SELECT ";
        $query .= "     T1.CALC_DATE, ";
        $query .= "     T1.CALC_TIME, ";
        $query .= "     T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD AS SUBCLASSCD, ";
        $query .= "     L1.SUBCLASSNAME, ";
        $query .= "     L2.GRADE_NAME1, ";
        $query .= "     L3.SEMESTERNAME, ";
        $query .= "     L4.TESTITEMNAME, ";
        if ($model->Properties["use_school_detail_gcm_dat"] == '1') {
            $query .= " L5.COURSENAME || L5.MAJORNAME AS COURSE_MAJOR_NAME, ";
        }
        $query .= "     T1.CHAIRDATE, ";
        $query .= "     T1.ELECTDIV_FLG ";
        $query .= " FROM ";
        if ($model->Properties["use_school_detail_gcm_dat"] == '1') {
            $query .= " RECORD_RANK_EXEC_GCM_SDIV_DAT T1 ";
        } else {
            $query .= " RECORD_RANK_EXEC_SDIV_DAT T1 ";
        }
        $query .= "     LEFT JOIN SUBCLASS_MST L1 ON L1.SUBCLASSCD = T1.SUBCLASSCD ";
        $query .= "                              AND L1.CLASSCD = T1.CLASSCD ";
        $query .= "                              AND L1.SCHOOL_KIND = T1.SCHOOL_KIND ";
        $query .= "                              AND L1.CURRICULUM_CD = T1.CURRICULUM_CD ";
        $query .= "     LEFT JOIN SCHREG_REGD_GDAT L2 ON L2.YEAR = T1.YEAR AND L2.GRADE = T1.GRADE ";
        $query .= "     LEFT JOIN SEMESTER_MST L3 ON L3.YEAR = T1.YEAR AND L3.SEMESTER = T1.SEMESTER ";
        if ($model->Properties["use_school_detail_gcm_dat"] == '1') {
            $query .= " LEFT JOIN TESTITEM_MST_COUNTFLG_NEW_GCM_SDIV L4 ON L4.YEAR = T1.YEAR ";
        } else {
            $query .= " LEFT JOIN TESTITEM_MST_COUNTFLG_NEW_SDIV L4 ON L4.YEAR = T1.YEAR ";
        }
        $query .= "                                                AND L4.SEMESTER = T1.SEMESTER ";
        $query .= "                                                AND L4.TESTKINDCD = T1.TESTKINDCD ";
        $query .= "                                                AND L4.TESTITEMCD = T1.TESTITEMCD ";
        $query .= "                                                AND L4.SCORE_DIV = T1.SCORE_DIV ";
        if ($model->Properties["use_school_detail_gcm_dat"] == '1') {
            $query .= "                                            AND L4.SCHOOLCD = T1.SCHOOLCD ";
            $query .= "                                            AND L4.SCHOOL_KIND = T1.SCHOOL_KIND ";
            $query .= "                                            AND L4.COURSECD || L4.MAJORCD = T1.COURSECD || T1.MAJORCD ";
            $query .= " LEFT JOIN V_COURSE_MAJOR_MST L5 ON L5.YEAR = T1.YEAR ";
            $query .= "                                AND L5.COURSECD || L5.MAJORCD = T1.COURSECD || T1.MAJORCD ";
        }
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '{$year}' ";
        $query .= "     AND T1.SEMESTER = '{$model->seme}' ";
        $query .= "     AND T1.TESTKINDCD || '-' || T1.TESTITEMCD || '-' || T1.SCORE_DIV = '{$model->exam}' ";
        $query .= "     AND T1.GRADE = '{$model->grade}' ";
        if ($model->Properties["use_school_detail_gcm_dat"] == '1') {
            $query .= " AND T1.SCHOOLCD = '".SCHOOLCD."' ";
            $query .= " AND T1.SCHOOL_KIND = '".SCHOOLKIND."' ";
            $query .= " AND T1.COURSECD || '-' || T1.MAJORCD = '{$model->course_major}' ";
        }
        $query .= " ORDER BY ";
        $query .= "     T1.CALC_DATE DESC, ";
        $query .= "     T1.CALC_TIME DESC ";
        return $query;
    }
    /* 実行履歴 */
    public function executeRireki($model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //実行日付・時間を取得
        $calcRow = $db->getRow(knjd141cQuery::getCalcDateTime(), DB_FETCHMODE_ASSOC);
        $calcDate = $calcRow["CALC_DATE"];//実行日付
        $calcTime = $calcRow["CALC_TIME"];//実行時間
        //実行履歴データ・追加
        $query = knjd141cQuery::getInsertRireki($calcDate, $calcTime, $model);
        $db->query($query);

        $db->commit();
        Query::dbCheckIn($db);
        return true;
    }
    //実行日付・時間を取得
    public function getCalcDateTime()
    {
        $query  = " with t_date_time (CALC_DATE,CALC_TIME) as ( ";
        $query .= " values( ";
        $query .= "     date(sysdate()), ";
        $query .= "     time(sysdate()) ";
        $query .= " )) ";
        $query .= "  ";
        $query .= " select * from t_date_time ";
        return $query;
    }
    //実行履歴データ・追加
    public function getInsertRireki($calcDate, $calcTime, $model)
    {
        $data = array();
        $data["CALC_DATE"][TEXT]            = $calcDate;
        $data["CALC_TIME"][TEXT]            = $calcTime;

        //処理科目(全て または 総合計のみ)
        if ($model->subclasscd == '999999' || $model->subclasscd == '000000') {
            //ダミーデータセット(00)
            $data["CLASSCD"][TEXT]          = "00";
            $data["SCHOOL_KIND"][TEXT]      = $model->school_kind;
            $data["CURRICULUM_CD"][TEXT]    = "00";
            $data["SUBCLASSCD"][TEXT]       = $model->subclasscd;
        //処理科目(各科目のみ)
        } elseif ($model->subclasscd != '') {
            $subArray = array();
            $subArray = explode("-", $model->subclasscd);
            $data["CLASSCD"][TEXT]          = $subArray[0];
            $data["SCHOOL_KIND"][TEXT]      = $subArray[1];
            $data["CURRICULUM_CD"][TEXT]    = $subArray[2];
            $data["SUBCLASSCD"][TEXT]       = $subArray[3];
        }

        $data["YEAR"][TEXT]                 = CTRL_YEAR;
        $data["SEMESTER"][TEXT]             = $model->seme;
        $data["TESTKINDCD"][TEXT]           = $model->kind;
        $data["TESTITEMCD"][TEXT]           = $model->item;
        $data["SCORE_DIV"][TEXT]            = $model->sdiv;
        $data["GRADE"][TEXT]                = $model->grade;
        $data["CHAIRDATE"][TEXT]            = str_replace("/", "-", $model->chairdate);
        $data["ELECTDIV_FLG"][TEXT]         = $model->electdiv;
        $data["REGISTERCD"][TEXT]           = STAFFCD;
        $data["UPDATED"][FUNC]              = "sysdate()";
        if ($model->Properties["use_school_detail_gcm_dat"] == '1') {
            $cdArray = array();
            $cdArray = explode("-", $model->course_major);
            $data["SCHOOLCD"][TEXT]         = SCHOOLCD;
            $data["COURSECD"][TEXT]         = $cdArray[0];
            $data["MAJORCD"][TEXT]          = $cdArray[1];
            $query = Query::insertSQL($data, "RECORD_RANK_EXEC_GCM_SDIV_DAT");
        } else {
            $query = Query::insertSQL($data, "RECORD_RANK_EXEC_SDIV_DAT");
        }
        return $query;
    }


    /* 実行 */
    public function executeQuery(&$model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        // -- アルファベット順に更新するようにした。--

        // RECORD_AVERAGE_CHAIR_DAT---各講座の合計・最高点・最低点・人数・平均・標準偏差を保持
        knjd141cQuery::insertSubclassQuery($db, $model, "record_average_chair");
        $db->commit();

        // RECORD_AVERAGE_DAT---各科目の合計・最高点・最低点・人数・平均・標準偏差を保持
        knjd141cQuery::insertSubclassQuery($db, $model, "record_average");
        $db->commit();

        // RECORD_RANK_CHAIR_DAT---各講座の順位・偏差値を保持
        knjd141cQuery::insertSubclassQuery($db, $model, "record_rank_chair");
        $db->commit();

        // RECORD_RANK_DAT---各科目の順位・偏差値を保持
        knjd141cQuery::insertSubclassQuery($db, $model, "record_rank");
        $db->commit();

        // -- アルファベット順に更新するようにした。--

        Query::dbCheckIn($db);
        return true;
    }

    /* 科目別の処理 */
    public function insertSubclassQuery(&$db, $model, $table)
    {
        $query  = "";
        //削除クエリー
        $query  = knjd141cQuery::getDeleteQuery($model, $table);
        $db->query($query);
        //削除クエリー(DETAIL_DAT)
        if ($model->Properties["useRecordRankDetail"] == '1' && ($table == "record_rank" || $table == "record_rank_chair")) {
            $query = knjd141cQuery::delRecordRankDetailDat($model, $table);
            $db->query($query);
        }
        //追加クエリー
        $query  = knjd141cQuery::getInsertSubclassQuery($model, $table);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            //プロパティー「use_school_detail_gcm_dat」が"1"の時、AVG_DIVが"1"のデータをセットしない。
            if ($table == "record_average" || $table == "record_average_chair") {
                if ($model->Properties["use_school_detail_gcm_dat"] == '1' && $row["AVG_DIV"] == "1") {
                    continue;
                }
                //削除クエリー(1レコード)
                if ($model->Properties["use_school_detail_gcm_dat"] == '1' && $row["AVG_DIV"] == "6") {
                    $query  = knjd141cQuery::getDeleteAvg($model, $table, $row);
                    $db->query($query);
                }
            }
            //項目
            $data = knjd141cQuery::getFieldData($model, $table, $row);
            //追加
            $query = Query::insertSQL($data, $table .$model->tableStr);
            $db->query($query);

            //プロパティー「useRecordRankDetail = 1」の時、RANK_DIV = A,B,C,D,E,Fの処理を追加
            if ($model->Properties["useRecordRankDetail"] == '1' && ($table == "record_rank" || $table == "record_rank_chair")) {
                knjd141cQuery::insRecordRankDetailDat($db, $model, $table, $row);
            }
        }
        return;
    }

    //削除クエリー(DETAIL_DAT)
    public function delRecordRankDetailDat($model, $table)
    {
        //削除テーブル
        $delTable = $table."_detail".$model->tableStr;

        $query  = " DELETE FROM " .$delTable;
        $query .= " where YEAR       = '".CTRL_YEAR."' ";
        $query .= "  and SEMESTER   = '".$model->seme."' ";
        $query .= "  and TESTKINDCD = '".$model->kind."' ";
        $query .= "  and TESTITEMCD = '".$model->item."' ";
        $query .= "  and SCORE_DIV  = '".$model->sdiv."' ";
        $query .= "  and SCHREGNO   in (SELECT SCHREGNO FROM ( ";
        $query .= knjd141cQuery::getSchregQuery($model);
        $query .= "  ) t1 ) ";
        //処理科目(総合計のみ)
        if ($model->subclasscd == '999999') {
            $query .= "  and SUBCLASSCD in ('333333','555555','999999','99999A','99999B') ";
        //処理科目(各科目のみ)
        } elseif ($model->subclasscd != '000000') {
            $query .= "  and CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD = '".$model->subclasscd."' ";
        }
        return $query;
    }

    //プロパティー「useRecordRankDetail = 1」の時、RANK_DIV = A,B,C,D,E,Fの処理を追加
    //A:同クラス・同コースグループ生徒内での順位
    //B:同クラス・同コース生徒内での順位
    //C:同クラス・同コース・同高入生徒内での順位
    //D:同クラス・同コース・同一貫生徒内での順位
    //E:同学年・同コース・同高入生徒内での順位
    //F:同学年・同コース・同一貫生徒内での順位
    //プロパティー「knjd141c_deviationTotalRank = 1」の時、RANK_DIV = Gの処理を追加
    //G:同学年生徒内での順位
    public function insRecordRankDetailDat($db, $model, $table, $row)
    {
        //登録テーブル
        $insTable = $table."_detail".$model->tableStr;

        // インサートの速度改善
        $sql = "";
        $sqlCnt = 0;

        //追加
        if ($model->Properties["knjd141c_deviationTotalRank"] == '1') {
            $rankDivArray = array("A", "B", "C", "D", "E", "F", "G");
        } else {
            $rankDivArray = array("A", "B", "C", "D", "E", "F");
        }
        foreach ($rankDivArray as $key => $val) {
            if ($val == "G") {
                //順位がNULLの場合は追加しない(NULLレコードは作成しない)
                if (!strlen($row["GRADE_DEVIATION_TOTAL"]) && !strlen($row["GRADE_DEVIATION_TOTAL_RANK"])) {
                    continue;
                }
                //SUBCLASSCDがALL3,5,9,9A,9Bのみ作成する
                if ($row["SUBCLASSCD"] != "333333" && $row["SUBCLASSCD"] != "555555" && $row["SUBCLASSCD"] != "999999" && $row["SUBCLASSCD"] != "99999A" && $row["SUBCLASSCD"] != "99999B") {
                    continue;
                }
            } else {
                $rankName = ($val == "A") ? "COURSE_GROUP_CL" : $val;
                //順位がNULLの場合は追加しない(NULLレコードは作成しない)
                if (!strlen($row[$rankName."_RANK"]) && !strlen($row[$rankName."_AVG_RANK"]) && !strlen($row[$rankName."_DEVIATION"]) && !strlen($row[$rankName."_DEVIATION_RANK"])) {
                    continue;
                }
            }

            $data = array();
            $data["YEAR"]             = CTRL_YEAR;
            $data["SEMESTER"]         = $model->seme;
            $data["TESTKINDCD"]       = $model->kind;
            $data["TESTITEMCD"]       = $model->item;
            $data["SCORE_DIV"]        = $model->sdiv;
            $data["CLASSCD"]          = $row["CLASSCD"];
            $data["SCHOOL_KIND"]      = $row["SCHOOL_KIND"];
            $data["CURRICULUM_CD"]    = $row["CURRICULUM_CD"];
            $data["SUBCLASSCD"]       = $row["SUBCLASSCD"];
            if ($table == "record_rank_chair") {
                $data["CHAIRCD"]          = $row["CHAIRCD"];
            }
            $data["SCHREGNO"]         = $row["SCHREGNO"];
            $data["RANK_DIV"]         = $val;
            if ($val == "G") {
                $data["RANK"]           = "NULL";
                $data["AVG_RANK"]       = "NULL";
                $data["DEVIATION"]      = !strlen($row["GRADE_DEVIATION_TOTAL"]) ? "NULL" : $row["GRADE_DEVIATION_TOTAL"];
                $data["DEVIATION_RANK"] = !strlen($row["GRADE_DEVIATION_TOTAL_RANK"]) ? "NULL" : $row["GRADE_DEVIATION_TOTAL_RANK"];
            } else {
                $data["RANK"]           = !strlen($row[$rankName."_RANK"]) ? "NULL" : $row[$rankName."_RANK"];
                $data["AVG_RANK"]       = !strlen($row[$rankName."_AVG_RANK"]) ? "NULL" : $row[$rankName."_AVG_RANK"];
                $data["DEVIATION"]      = !strlen($row[$rankName."_DEVIATION"]) ? "NULL" : $row[$rankName."_DEVIATION"];
                $data["DEVIATION_RANK"] = !strlen($row[$rankName."_DEVIATION_RANK"]) ? "NULL" : $row[$rankName."_DEVIATION_RANK"];
            }
            $data["REGISTERCD"]       = STAFFCD;
            $data["UPDATED"]          = "SYSDATE()";

            $sql .= knjd141cQuery::getInsertSQL($model, $data, $table, $sqlCnt);
            $sqlCnt++;
        }
        // インサートの速度改善
        if ($sql != "") {
            $db->query($sql);
        }
    }

    // インサートの速度改善(insert文作成)
    public function getInsertSQL($model, $data, $table, $sqlCnt)
    {
        //登録テーブル
        $insTable = $table."_detail".$model->tableStr;

        if ($sqlCnt == 0) {
            $sql  = " INSERT INTO ".$insTable;
            $sql .= " ( ";
            $sql .= "     YEAR, ";
            $sql .= "     SEMESTER, ";
            $sql .= "     TESTKINDCD, ";
            $sql .= "     TESTITEMCD, ";
            $sql .= "     SCORE_DIV, ";
            $sql .= "     CLASSCD, ";
            $sql .= "     SCHOOL_KIND, ";
            $sql .= "     CURRICULUM_CD, ";
            $sql .= "     SUBCLASSCD, ";
            if ($table == "record_rank_chair") {
                $sql .= "     CHAIRCD, ";
            }
            $sql .= "     SCHREGNO, ";
            $sql .= "     RANK_DIV, ";
            $sql .= "     RANK, ";
            $sql .= "     AVG_RANK, ";
            $sql .= "     DEVIATION, ";
            $sql .= "     DEVIATION_RANK, ";
            $sql .= "     REGISTERCD, ";
            $sql .= "     UPDATED ";
            $sql .= " ) ";
        } else {
            $sql  = " UNION ALL ";
        }
        $sql .= " VALUES( ";
        $sql .= "     '{$data["YEAR"]}', ";
        $sql .= "     '{$data["SEMESTER"]}', ";
        $sql .= "     '{$data["TESTKINDCD"]}', ";
        $sql .= "     '{$data["TESTITEMCD"]}', ";
        $sql .= "     '{$data["SCORE_DIV"]}', ";
        $sql .= "     '{$data["CLASSCD"]}', ";
        $sql .= "     '{$data["SCHOOL_KIND"]}', ";
        $sql .= "     '{$data["CURRICULUM_CD"]}', ";
        $sql .= "     '{$data["SUBCLASSCD"]}', ";
        if ($table == "record_rank_chair") {
            $sql .= "     '{$data["CHAIRCD"]}', ";
        }
        $sql .= "     '{$data["SCHREGNO"]}', ";
        $sql .= "     '{$data["RANK_DIV"]}', ";
        $sql .= "     {$data["RANK"]}, ";
        $sql .= "     {$data["AVG_RANK"]}, ";
        $sql .= "     {$data["DEVIATION"]}, ";
        $sql .= "     {$data["DEVIATION_RANK"]}, ";
        $sql .= "     '".STAFFCD."', ";
        $sql .= "     SYSDATE() ";
        $sql .= " ) ";
        return $sql;
    }

    //削除クエリー(1レコード)
    public function getDeleteAvg($model, $table, $row)
    {
        $query  = "";
        if ($table == "record_average" || $table == "record_average_chair") {
            $query .= "DELETE FROM " .$table .$model->tableStr ." ";
            $query .= "where YEAR       = '".CTRL_YEAR."' ";
            $query .= "  and SEMESTER   = '".$model->seme."' ";
            $query .= "  and TESTKINDCD = '".$model->kind."' ";
            $query .= "  and TESTITEMCD = '".$model->item."' ";
            $query .= "  and SCORE_DIV  = '".$model->sdiv."' ";
            $query .= "  and AVG_DIV    = '".$row["AVG_DIV"]."' ";
            $query .= "  and GRADE      = '".$row["GRADE"]."' ";
            $query .= "  and HR_CLASS   = '".$row["HR_CLASS"]."' ";
            $query .= "  and COURSECD   = '".$row["COURSECD"]."' ";
            $query .= "  and MAJORCD    = '".$row["MAJORCD"]."' ";
            $query .= "  and COURSECODE = '".$row["COURSECODE"]."' ";
            //教育課程対応
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "  and CLASSCD        = '".$row["CLASSCD"]."' ";
                $query .= "  and SCHOOL_KIND    = '".$row["SCHOOL_KIND"]."' ";
                $query .= "  and CURRICULUM_CD  = '".$row["CURRICULUM_CD"]."' ";
            }
            $query .= "  and SUBCLASSCD = '".$row["SUBCLASSCD"]."' ";
            if ($table == "record_average_chair") {
                $query .= "  and CHAIRCD = '".$row["CHAIRCD"]."' ";
            }
        }
        return $query;
    }

    //削除クエリー
    public function getDeleteQuery($model, $table)
    {
        $query  = "";
        if ($table == "record_average" || $table == "record_average_chair") {
            $query .= "DELETE FROM " .$table .$model->tableStr ." ";
            $query .= "where YEAR       = '".CTRL_YEAR."' ";
            $query .= "  and SEMESTER   = '".$model->seme."' ";
            $query .= "  and TESTKINDCD = '".$model->kind."' ";
            $query .= "  and TESTITEMCD = '".$model->item."' ";
            $query .= "  and SCORE_DIV  = '".$model->sdiv."' ";
            $query .= "  and GRADE      = '".$model->grade."' ";
            //処理科目(総合計のみ)
            if ($model->subclasscd == '999999') {
                $query .= "  and SUBCLASSCD in ('333333','555555','999999','99999A','99999B') ";
            //処理科目(各科目のみ)
            } elseif ($model->subclasscd != '000000') {
                $query .= "  and CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD = '".$model->subclasscd."' ";
            }
            //指定された課程学科の生徒
            if ($model->Properties["use_school_detail_gcm_dat"] == '1') {
                $query .= "  and (AVG_DIV,GRADE,HR_CLASS,COURSECD,MAJORCD,COURSECODE) in (SELECT AVG_DIV,GRADE,HR_CLASS,COURSECD,MAJORCD,COURSECODE FROM ( ";
                $query .= knjd141cQuery::getDelAvgSchregQuery($model);
                $query .= "  ) t1 ) ";
            }
        }
        if ($table == "record_rank" || $table == "record_rank_chair") {
            $query .= "DELETE FROM " .$table .$model->tableStr ." ";
            $query .= "where YEAR       = '".CTRL_YEAR."' ";
            $query .= "  and SEMESTER   = '".$model->seme."' ";
            $query .= "  and TESTKINDCD = '".$model->kind."' ";
            $query .= "  and TESTITEMCD = '".$model->item."' ";
            $query .= "  and SCORE_DIV  = '".$model->sdiv."' ";
            $query .= "  and SCHREGNO   in (SELECT SCHREGNO FROM ( ";
            $query .= knjd141cQuery::getSchregQuery($model);
            $query .= "  ) t1 ) ";
            //処理科目(総合計のみ)
            if ($model->subclasscd == '999999') {
                $query .= "  and SUBCLASSCD in ('333333','555555','999999','99999A','99999B') ";
            //処理科目(各科目のみ)
            } elseif ($model->subclasscd != '000000') {
                $query .= "  and CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD = '".$model->subclasscd."' ";
            }
        }
        return $query;
    }

    //成績平均データ削除用クエリー
    public function getDelAvgSchregQuery($model)
    {
        //1:学年
        $query .= "SELECT ";
        $query .= "    '1' as AVG_DIV, ";
        $query .= "    GRADE, ";
        $query .= "    '000' as HR_CLASS, ";
        $query .= "    '0' as COURSECD, ";
        $query .= "    '000' as MAJORCD, ";
        $query .= "    '0000' as COURSECODE ";
        $query .= "FROM ";
        $query .= "     ( ";
        $query .= knjd141cQuery::getSchregQuery($model);
        $query .= "     ) t1 ";
        $query .= "GROUP BY ";
        $query .= "    GRADE ";
        //2:クラス
        $query .= "UNION ALL ";
        $query .= "SELECT ";
        $query .= "    '2' as AVG_DIV, ";
        $query .= "    GRADE, ";
        $query .= "    HR_CLASS, ";
        $query .= "    '0' as COURSECD, ";
        $query .= "    '000' as MAJORCD, ";
        $query .= "    '0000' as COURSECODE ";
        $query .= "FROM ";
        $query .= "     ( ";
        $query .= knjd141cQuery::getSchregQuery($model);
        $query .= "     ) t1 ";
        $query .= "GROUP BY ";
        $query .= "    GRADE, ";
        $query .= "    HR_CLASS ";
        //3:コース
        $query .= "UNION ALL ";
        $query .= "SELECT ";
        $query .= "    '3' as AVG_DIV, ";
        $query .= "    GRADE, ";
        $query .= "    '000' as HR_CLASS, ";
        $query .= "    COURSECD, ";
        $query .= "    MAJORCD, ";
        $query .= "    COURSECODE ";
        $query .= "FROM ";
        $query .= "     ( ";
        $query .= knjd141cQuery::getSchregQuery($model);
        $query .= "     ) t1 ";
        $query .= "GROUP BY ";
        $query .= "    GRADE, ";
        $query .= "    COURSECD, ";
        $query .= "    MAJORCD, ";
        $query .= "    COURSECODE ";
        //4:学科
        $query .= "UNION ALL ";
        $query .= "SELECT ";
        $query .= "    '4' as AVG_DIV, ";
        $query .= "    GRADE, ";
        $query .= "    '000' as HR_CLASS, ";
        $query .= "    COURSECD, ";
        $query .= "    MAJORCD, ";
        $query .= "    '0000' AS COURSECODE ";
        $query .= "FROM ";
        $query .= "     ( ";
        $query .= knjd141cQuery::getSchregQuery($model);
        $query .= "     ) t1 ";
        $query .= "GROUP BY ";
        $query .= "    GRADE, ";
        $query .= "    COURSECD, ";
        $query .= "    MAJORCD ";
        //5:コースグループ
        if ($model->Properties["useRankCourseGroup"] == '1' || $model->Properties["useRankCourseGroupVersion2"] == '1' || $model->Properties["useRecordRankDetail"] == '1') {
            $query .= "UNION ALL ";
            $query .= "SELECT ";
            $query .= "    '5' as AVG_DIV, ";
            $query .= "    GRADE, ";
            $query .= "    '000' as HR_CLASS, ";
            $query .= "    '0' as COURSECD, ";
            $query .= "    COURSE_GROUP as MAJORCD, ";
            $query .= "    '0000' AS COURSECODE ";
            $query .= "FROM ";
            $query .= "     ( ";
            $query .= knjd141cQuery::getSchregQuery($model);
            $query .= "     ) t1 ";
            $query .= "GROUP BY ";
            $query .= "    GRADE, ";
            $query .= "    COURSE_GROUP ";
        }
        //A:クラス別コースグループ
        if ($model->Properties["useRecordRankDetail"] == '1') {
            $query .= "UNION ALL ";
            $query .= "SELECT ";
            $query .= "    'A' as AVG_DIV, ";
            $query .= "    GRADE, ";
            $query .= "    HR_CLASS, ";
            $query .= "    '0' as COURSECD, ";
            $query .= "    COURSE_GROUP as MAJORCD, ";
            $query .= "    '0000' AS COURSECODE ";
            $query .= "FROM ";
            $query .= "     ( ";
            $query .= knjd141cQuery::getSchregQuery($model);
            $query .= "     ) t1 ";
            $query .= "GROUP BY ";
            $query .= "    GRADE, ";
            $query .= "    HR_CLASS, ";
            $query .= "    COURSE_GROUP ";
        }
        //7:寮生
        if ($model->Properties["useAvgRyousei"] == '1') {
            $query .= "UNION ALL ";
            $query .= "SELECT ";
            $query .= "    '7' as AVG_DIV, ";
            $query .= "    GRADE, ";
            $query .= "    '000' as HR_CLASS, ";
            $query .= "    '0' as COURSECD, ";
            $query .= "    '000' as MAJORCD, ";
            $query .= "    '0000' AS COURSECODE ";
            $query .= "FROM ";
            $query .= "     ( ";
            $query .= knjd141cQuery::getSchregQuery($model);
            $query .= "     ) t1 ";
            $query .= "GROUP BY ";
            $query .= "    GRADE ";
        }
        //DIV = B,C,D,E,F
        if ($model->Properties["useRecordRankDetail"] == '1') {
            $rankDivArray = array("B", "C", "D", "E", "F");
            foreach ($rankDivArray as $key => $val) {
                $query .= "UNION ALL ";
                $query .= "SELECT ";
                $query .= "    '{$val}' as AVG_DIV, ";
                $query .= "    GRADE, ";
                if ($val == "B" || $val == "C" || $val == "D") {
                    $query .= "    HR_CLASS, ";
                } elseif ($val == "E" || $val == "F") {
                    $query .= "    '000' as HR_CLASS, ";
                }
                $query .= "    COURSECD, ";
                $query .= "    MAJORCD, ";
                $query .= "    COURSECODE ";
                $query .= "FROM ";
                $query .= "     ( ";
                $query .= knjd141cQuery::getSchregQuery($model);
                $query .= "     ) t1 ";
                $query .= "GROUP BY ";
                $query .= "    GRADE, ";
                if ($val == "B" || $val == "C" || $val == "D") {
                    $query .= "    HR_CLASS, ";
                }
                $query .= "    COURSECD, ";
                $query .= "    MAJORCD, ";
                $query .= "    COURSECODE ";
            }
        }
        return $query;
    }

    //項目
    public function getFieldData($model, $table, $data2)
    {
        $data = array();
        if ($table == "record_average" || $table == "record_average_chair") {
            $data["YEAR"][TEXT]         = CTRL_YEAR;
            $data["SEMESTER"][TEXT]     = $model->seme;
            $data["TESTKINDCD"][TEXT]   = $model->kind;
            $data["TESTITEMCD"][TEXT]   = $model->item;
            $data["SCORE_DIV"][TEXT]    = $model->sdiv;
            //教育課程対応
            if ($model->Properties["useCurriculumcd"] == '1') {
                $data["CLASSCD"][TEXT]   = $data2["CLASSCD"];
                $data["SCHOOL_KIND"][TEXT]   = $data2["SCHOOL_KIND"];
                $data["CURRICULUM_CD"][TEXT]   = $data2["CURRICULUM_CD"];
            }
            $data["SUBCLASSCD"][TEXT]   = $data2["SUBCLASSCD"];
            if ($table == "record_average_chair") {
                $data["CHAIRCD"][TEXT]   = $data2["CHAIRCD"];
            }
            $data["AVG_DIV"][TEXT]      = $data2["AVG_DIV"];
            $data["GRADE"][TEXT]        = $data2["GRADE"];
            $data["HR_CLASS"][TEXT]     = $data2["HR_CLASS"];
            $data["COURSECD"][TEXT]     = $data2["COURSECD"];
            $data["MAJORCD"][TEXT]      = $data2["MAJORCD"];
            $data["COURSECODE"][TEXT]   = $data2["COURSECODE"];
            $data["SCORE"][NUMBER]      = $data2["SCORE"];
            $data["HIGHSCORE"][NUMBER]  = $data2["HIGHSCORE"];
            $data["LOWSCORE"][NUMBER]   = $data2["LOWSCORE"];
            $data["COUNT"][NUMBER]      = $data2["COUNT"];
            $data["AVG"][NUMBER]        = $data2["AVG"];
            $data["STDDEV"][NUMBER]     = $data2["STDDEV"];
            if ($table == "record_average_chair") {
                $data["CHAIRDATE"][TEXT]    = str_replace("/", "-", $model->chairdate);
            }
            if ($model->Properties["useRecordRankDetail"] == '1') {
                $data["HIGHSCORE_AVG"][NUMBER]  = $data2["HIGHSCORE_AVG"];
                $data["LOWSCORE_AVG"][NUMBER]   = $data2["LOWSCORE_AVG"];
            }
            $data["REGISTERCD"][TEXT]   = STAFFCD;
            $data["UPDATED"][FUNC]      = "SYSDATE()";
        }
        if ($table == "record_rank" || $table == "record_rank_chair") {
            $data["YEAR"][TEXT]         = CTRL_YEAR;
            $data["SEMESTER"][TEXT]     = $model->seme;
            $data["TESTKINDCD"][TEXT]   = $model->kind;
            $data["TESTITEMCD"][TEXT]   = $model->item;
            $data["SCORE_DIV"][TEXT]    = $model->sdiv;
            //教育課程対応
            if ($model->Properties["useCurriculumcd"] == '1') {
                $data["CLASSCD"][TEXT]   = $data2["CLASSCD"];
                $data["SCHOOL_KIND"][TEXT]   = $data2["SCHOOL_KIND"];
                $data["CURRICULUM_CD"][TEXT]   = $data2["CURRICULUM_CD"];
            }
            $data["SUBCLASSCD"][TEXT]   = $data2["SUBCLASSCD"];
            if ($table == "record_rank_chair") {
                $data["CHAIRCD"][TEXT]   = $data2["CHAIRCD"];
            }
            $data["SCHREGNO"][TEXT]     = $data2["SCHREGNO"];
            if ($model->schoolName == "nichi-ni" && $model->sdiv == '08' && ($model->school_kind == 'J' || $model->school_kind == 'H' && $model->seme == '9')) {
                $data["SCORE"][NUMBER]      = round($data2["SCORE"]);
            } else {
                $data["SCORE"][NUMBER]      = $data2["SCORE"];
            }
            $data["AVG"][NUMBER]        = $data2["AVG"];
            //プロパティー「use_school_detail_gcm_dat」が"1"の時、GRADE_XXXのデータをセットしない。
            if ($model->Properties["use_school_detail_gcm_dat"] != '1') {
                $data["GRADE_RANK"][NUMBER]           = $data2["GRADE_RANK"];
                $data["GRADE_AVG_RANK"][NUMBER]       = $data2["GRADE_AVG_RANK"];
                $data["GRADE_DEVIATION"][NUMBER]      = $data2["GRADE_DEVIATION"];
                $data["GRADE_DEVIATION_RANK"][NUMBER] = $data2["GRADE_DEVIATION_RANK"];
            }
            $data["CLASS_RANK"][NUMBER]           = $data2["CLASS_RANK"];
            $data["CLASS_AVG_RANK"][NUMBER]       = $data2["CLASS_AVG_RANK"];
            $data["CLASS_DEVIATION"][NUMBER]      = $data2["CLASS_DEVIATION"];
            $data["CLASS_DEVIATION_RANK"][NUMBER] = $data2["CLASS_DEVIATION_RANK"];
            $data["COURSE_RANK"][NUMBER]          = $data2["COURSE_RANK"];
            $data["COURSE_AVG_RANK"][NUMBER]      = $data2["COURSE_AVG_RANK"];
            $data["COURSE_DEVIATION"][NUMBER]     = $data2["COURSE_DEVIATION"];
            $data["COURSE_DEVIATION_RANK"][NUMBER]= $data2["COURSE_DEVIATION_RANK"];
            //0)プロパティー「useRecordRankDetail」が"1"の時、
            //    MAJOR_XXXに学科をセットする。
            //    COURSE_GROUP_XXXにコースグループをセットする。
            //    COURSE_GROUP_CL_XXXにクラス別コースグループをセットする。・・・別テーブルRECORD_RANK_DETAIL_SDIV_DAT
            //1)プロパティー「useRankCourseGroupVersion2」が"1"の時、
            //    MAJOR_XXXに学科をセットする。
            //    COURSE_GROUP_XXXにコースグループをセットする。
            //2)プロパティー「useRankCourseGroup」が"1"の時、
            //    MAJOR_XXXにコースグループをセットする。
            //3)上記以外の時、
            //    MAJOR_XXXに学科をセットする。
            if ($model->Properties["useRecordRankDetail"] == '1') {
                $data["MAJOR_RANK"][NUMBER]                  = $data2["MAJOR_RANK"];
                $data["MAJOR_AVG_RANK"][NUMBER]              = $data2["MAJOR_AVG_RANK"];
                $data["MAJOR_DEVIATION"][NUMBER]             = $data2["MAJOR_DEVIATION"];
                $data["MAJOR_DEVIATION_RANK"][NUMBER]        = $data2["MAJOR_DEVIATION_RANK"];
                $data["COURSE_GROUP_RANK"][NUMBER]           = $data2["COURSE_GROUP_RANK"];
                $data["COURSE_GROUP_AVG_RANK"][NUMBER]       = $data2["COURSE_GROUP_AVG_RANK"];
                $data["COURSE_GROUP_DEVIATION"][NUMBER]      = $data2["COURSE_GROUP_DEVIATION"];
                $data["COURSE_GROUP_DEVIATION_RANK"][NUMBER] = $data2["COURSE_GROUP_DEVIATION_RANK"];
            } elseif ($model->Properties["useRankCourseGroupVersion2"] == '1') {
                $data["MAJOR_RANK"][NUMBER]                  = $data2["MAJOR_RANK"];
                $data["MAJOR_AVG_RANK"][NUMBER]              = $data2["MAJOR_AVG_RANK"];
                $data["MAJOR_DEVIATION"][NUMBER]             = $data2["MAJOR_DEVIATION"];
                $data["MAJOR_DEVIATION_RANK"][NUMBER]        = $data2["MAJOR_DEVIATION_RANK"];
                $data["COURSE_GROUP_RANK"][NUMBER]           = $data2["COURSE_GROUP_RANK"];
                $data["COURSE_GROUP_AVG_RANK"][NUMBER]       = $data2["COURSE_GROUP_AVG_RANK"];
                $data["COURSE_GROUP_DEVIATION"][NUMBER]      = $data2["COURSE_GROUP_DEVIATION"];
                $data["COURSE_GROUP_DEVIATION_RANK"][NUMBER] = $data2["COURSE_GROUP_DEVIATION_RANK"];
            } elseif ($model->Properties["useRankCourseGroup"] == '1') {
                $data["MAJOR_RANK"][NUMBER]             = $data2["COURSE_GROUP_RANK"];
                $data["MAJOR_AVG_RANK"][NUMBER]         = $data2["COURSE_GROUP_AVG_RANK"];
                $data["MAJOR_DEVIATION"][NUMBER]        = $data2["COURSE_GROUP_DEVIATION"];
                $data["MAJOR_DEVIATION_RANK"][NUMBER]   = $data2["COURSE_GROUP_DEVIATION_RANK"];
            } else {
                $data["MAJOR_RANK"][NUMBER]             = $data2["MAJOR_RANK"];
                $data["MAJOR_AVG_RANK"][NUMBER]         = $data2["MAJOR_AVG_RANK"];
                $data["MAJOR_DEVIATION"][NUMBER]        = $data2["MAJOR_DEVIATION"];
                $data["MAJOR_DEVIATION_RANK"][NUMBER]   = $data2["MAJOR_DEVIATION_RANK"];
            }
            //6:講座グループ
            if ($model->Properties["useRankChairGroup"] == '1') {
                $data["CHAIR_GROUP_RANK"][NUMBER]      = $data2["CHAIR_GROUP_RANK"];
                $data["CHAIR_GROUP_AVG_RANK"][NUMBER]  = $data2["CHAIR_GROUP_AVG_RANK"];
                $data["CHAIR_GROUP_DEVIATION"][NUMBER] = $data2["CHAIR_GROUP_DEVIATION"];
            }
            if ($table == "record_rank_chair") {
                $data["CHAIRDATE"][TEXT]    = str_replace("/", "-", $model->chairdate);
            }
            $data["REGISTERCD"][TEXT]   = STAFFCD;
            $data["UPDATED"][FUNC]      = "SYSDATE()";
        }
        return $data;
    }

    //（共通）追加クエリー
    public function getInsertCommonQuery($model, $table)
    {
        $query  = "";
        //在籍
        $query .= "WITH T_SCHREG AS ( ";
        $query .= knjd141cQuery::getSchregQuery($model);
        $query .= "    ) ";
        //３科・５科のグループデータ
        $query .= ",T_GROUP AS ( ";
        $query .= "    SELECT ";
        $query .= "        T1.SCHREGNO, ";
        $query .= "        T2.GROUP_DIV, "; //3:３科 5:５科
        $query .= "        T2.CLASSCD, ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "    T2.SCHOOL_KIND, ";
            $query .= "    T2.CURRICULUM_CD, ";
        }
        $query .= "        T2.SUBCLASSCD ";
        $query .= "    FROM ";
        $query .= "        T_SCHREG T1 ";
        $query .= "        LEFT JOIN REC_SUBCLASS_GROUP_DAT T2 ";
        $query .= "             ON T2.YEAR = '".CTRL_YEAR."' ";
        $query .= "            AND T2.GRADE = T1.GRADE ";
        $query .= "            AND T2.COURSECD = T1.COURSECD ";
        $query .= "            AND T2.MAJORCD = T1.MAJORCD ";
        $query .= "            AND T2.COURSECODE = T1.COURSECODE ";
        $query .= "    ) ";
        if ($model->narakenKekkaOverSubclass) {
            $query .= ", NARAKEN_KEKKA_OVER_SUBCLASS (CLASSCD, SCHOOL_KIND, CURRICULUM_CD, SUBCLASSCD, SCHREGNO) AS ( ";
            $query .= " VALUES ".$model->narakenKekkaOverSubclass;
            $query .= ")";
        }
        //KNJD129Qにて、平均点欄を表示・・・入力不可（表示のみ）・・・駿台甲府要望
        //(9-9900-08)各科目のRECORD_RANK_SDIV_DAT.AVGに保持しておく。
        $testcd = $model->seme.$model->kind.$model->item.$model->sdiv;
        if ($model->Properties["showHeikinran"] == '1' && $testcd == '9990008') {
            //平均点
            $query .= " , RECORD_AVG AS ( ";
            $query .= "     SELECT ";
            //教育課程対応
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "         SCORE_D.CLASSCD, ";
                $query .= "         SCORE_D.SCHOOL_KIND, ";
                $query .= "         SCORE_D.CURRICULUM_CD, ";
            }
            $query .= "         SCORE_D.SUBCLASSCD, ";
            $query .= "         SCORE_D.SCHREGNO, ";
            if ($model->schoolName == "nichi-ni" && $model->sdiv == '08' && ($model->school_kind == 'J' || $model->school_kind == 'H' && $model->seme == '9')) {
                $query .= "         SUM(SCORE_DT.DEC_VAL1) AS SCHNO_TOTAL, ";
                $query .= "         DECIMAL(ROUND(AVG(FLOAT(SCORE_DT.DEC_VAL1))*{$model->AvgDecimalPoint["waru"]},0)/{$model->AvgDecimalPoint["waru"]},{$model->AvgDecimalPoint["allketa"]},{$model->AvgDecimalPoint["keta"]}) AS SCHNO_AVG, ";
                $query .= "         COUNT(SCORE_DT.DEC_VAL1) AS SCORE_CNT ";
            } else {
                $query .= "         SUM(SCORE_D.SCORE) AS SCHNO_TOTAL, ";
                $query .= "         DECIMAL(ROUND(AVG(FLOAT(SCORE_D.SCORE))*{$model->AvgDecimalPoint["waru"]},0)/{$model->AvgDecimalPoint["waru"]},{$model->AvgDecimalPoint["allketa"]},{$model->AvgDecimalPoint["keta"]}) AS SCHNO_AVG, ";
                $query .= "         COUNT(SCORE_D.SCORE) AS SCORE_CNT ";
            }
            $query .= "     FROM ";
            $query .= "         RECORD_SCORE_DAT SCORE_D ";
            $query .= "         INNER JOIN CLASS_YDAT CYDAT ON CYDAT.YEAR = SCORE_D.YEAR ";
            $query .= "             AND CYDAT.CLASSCD = SCORE_D.CLASSCD ";
            $query .= "             AND CYDAT.SCHOOL_KIND = SCORE_D.SCHOOL_KIND ";
            $query .= "         INNER JOIN SUBCLASS_YDAT SYDAT ON SYDAT.YEAR = SCORE_D.YEAR ";
            $query .= "             AND SYDAT.CLASSCD = SCORE_D.CLASSCD ";
            $query .= "             AND SYDAT.SCHOOL_KIND = SCORE_D.SCHOOL_KIND ";
            $query .= "             AND SYDAT.CURRICULUM_CD = SCORE_D.CURRICULUM_CD ";
            $query .= "             AND SYDAT.SUBCLASSCD = SCORE_D.SUBCLASSCD ";
            if ($model->schoolName == "nichi-ni" && $model->sdiv == '08' && ($model->school_kind == 'J' || $model->school_kind == 'H' && $model->seme == '9')) {
                $query .= "         LEFT JOIN RECORD_SCORE_DETAIL_DAT SCORE_DT ON SCORE_D.YEAR = SCORE_DT.YEAR ";
                $query .= "              AND SCORE_D.SEMESTER       = SCORE_DT.SEMESTER ";
                $query .= "              AND SCORE_D.TESTKINDCD     = SCORE_DT.TESTKINDCD ";
                $query .= "              AND SCORE_D.TESTITEMCD     = SCORE_DT.TESTITEMCD ";
                $query .= "              AND SCORE_D.SCORE_DIV      = SCORE_DT.SCORE_DIV ";
                $query .= "              AND SCORE_D.CLASSCD        = SCORE_DT.CLASSCD ";
                $query .= "              AND SCORE_D.SCHOOL_KIND    = SCORE_DT.SCHOOL_KIND ";
                $query .= "              AND SCORE_D.CURRICULUM_CD  = SCORE_DT.CURRICULUM_CD ";
                $query .= "              AND SCORE_D.SUBCLASSCD     = SCORE_DT.SUBCLASSCD ";
                $query .= "              AND SCORE_D.SCHREGNO       = SCORE_DT.SCHREGNO ";
                $query .= "              AND SCORE_DT.DETAIL_SEQ    = '001' ";
            }
            $query .= "     WHERE ";
            $query .= "         SCORE_D.YEAR = '".CTRL_YEAR."' ";
            //算出元テスト
            $query .= "         AND SCORE_D.SEMESTER || SCORE_D.TESTKINDCD || SCORE_D.TESTITEMCD || SCORE_D.SCORE_DIV IN ( ";
            $query .= "             SELECT ";
            $query .= "                 MOTO_SEMESTER || MOTO_TESTKINDCD || MOTO_TESTITEMCD || MOTO_SCORE_DIV AS MOTO_TESTCD ";
            $query .= "             FROM ";
            if ($model->Properties["use_school_detail_gcm_dat"] == '1') {
                $query .= "             ADMIN_CONTROL_CALC_GCM_SDIV_DAT T1 ";
            } else {
                $query .= "             ADMIN_CONTROL_CALC_SDIV_DAT T1 ";
            }
            $query .= "             WHERE ";
            $query .= "                 YEAR = '".CTRL_YEAR."' ";
            $query .= "                 AND SEMESTER || TESTKINDCD || TESTITEMCD || SCORE_DIV = '{$testcd}' "; //算出先テスト
            //教育課程対応
            if ($model->Properties["useCurriculumcd"] == '1') {
                $subclassAll = "00-".$model->school_kind."-00-000000";
                $subclassAll_array = array();
                $subclassAll_array = explode("-", $subclassAll);
                $query .= "             AND CLASSCD        = '".$subclassAll_array[0]."' ";
                $query .= "             AND SCHOOL_KIND    = '".$subclassAll_array[1]."' ";
                $query .= "             AND CURRICULUM_CD  = '".$subclassAll_array[2]."' ";
                $query .= "             AND SUBCLASSCD     = '".$subclassAll_array[3]."' ";
            } else {
                $subclassAll = "000000";
                $query .= "             AND SUBCLASSCD     = '".$subclassAll."' ";
            }
            if ($model->Properties["use_school_detail_gcm_dat"] == '1') {
                $query .= "             AND T1.SCHOOLCD = '".SCHOOLCD."' ";
                $query .= "             AND T1.SCHOOL_KIND = '".SCHOOLKIND."' ";
                $query .= "             AND T1.COURSECD || '-' || T1.MAJORCD = '{$model->course_major}' ";
            }
            $query .= "         ) ";
            $query .= "     GROUP BY ";
            //教育課程対応
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "         SCORE_D.CLASSCD, ";
                $query .= "         SCORE_D.SCHOOL_KIND, ";
                $query .= "         SCORE_D.CURRICULUM_CD, ";
            }
            $query .= "         SCORE_D.SUBCLASSCD, ";
            $query .= "         SCORE_D.SCHREGNO ";
            $query .= " ) ";
        }
        //講座グループ
        if ($model->Properties["useRankChairGroup"] == '1') {
            $query .= knjd141cQuery::getChairGroup($model);
        }
        //成績
        $query .= ",T_SCORE AS ( ";
        $query .= knjd141cQuery::getRecordQuery($model);
        $query .= "    ) ";
        if ($table == "record_average" || $table == "record_rank") {
            //SCOREがNULLの学籍番号を取得
            $query .= ",T_SCORE_NULL AS ( ";
            $query .= knjd141cQuery::getRecordNullQuery($model, "9");
            $query .= "    ) ";
            $query .= ",T_SCORE_NULL3 AS ( ";
            $query .= knjd141cQuery::getRecordNullQuery($model, "3");
            $query .= "    ) ";
            $query .= ",T_SCORE_NULL5 AS ( ";
            $query .= knjd141cQuery::getRecordNullQuery($model, "5");
            $query .= "    ) ";
            $query .= ",T_SCORE_NULLA AS ( ";
            $query .= knjd141cQuery::getRecordNullQuery($model, "A");
            $query .= "    ) ";
            $query .= ",T_SCORE_NULLB AS ( ";
            $query .= knjd141cQuery::getRecordNullQuery($model, "B");
            $query .= "    ) ";
            //各生徒の３科目・５科目・全科目のSCOREの合計を取得
            $query .= ",T_SCORE_SUM AS ( ";
            $query .= knjd141cQuery::getRecordScoreSumQuery($model);
            $query .= "    ) ";
        }
        //在籍&成績２
        $query .= ",T_RANK AS ( ";
        $query .= knjd141cQuery::getRecordSchregQuery($model, $table);
        $query .= "    ) ";

        return $query;
    }

    //在籍クエリー
    public function getSchregQuery($model)
    {
        $query  = "";
        $query .= "    SELECT ";
        $query .= "        W1.SCHREGNO, ";
        $query .= "        W1.GRADE, ";
        $query .= "        W1.HR_CLASS, ";
        $query .= "        W1.COURSECD,W1.MAJORCD,W1.COURSECODE, ";
        $query .= "        W1.COURSECD||W1.MAJORCD||W1.COURSECODE AS COURSE, ";
        $query .= "        W1.COURSECD||W1.MAJORCD||'0000' AS MAJOR ";
        //5:コースグループ
        if ($model->Properties["useRankCourseGroup"] == '1' || $model->Properties["useRankCourseGroupVersion2"] == '1' || $model->Properties["useRecordRankDetail"] == '1') {
            $query .= "       ,VALUE(W2.GROUP_CD, '000') AS COURSE_GROUP ";
        }
        //7:寮生
        if ($model->Properties["useAvgRyousei"] == '1') {
            $query .= "       ,CASE WHEN N1.NAMESPARE1 = '4' THEN '1' END AS RYOUSEI_FLG ";
        }
        //DIV = B,C,D,E,F
        //Jがあれば一貫(中学入学)、それ以外は高入(高校入学)
        if ($model->Properties["useRecordRankDetail"] == '1') {
            $query .= "       ,CASE WHEN SEGHD.SCHOOL_KIND = 'J' THEN 'J' ELSE 'H' END AS ENT_JH "; //H:高入(高校入学) J:一貫(中学入学)
        }
        $query .= "    FROM ";
        $query .= "        SCHREG_REGD_DAT W1 ";
        //5:コースグループ
        if ($model->Properties["useRankCourseGroup"] == '1' || $model->Properties["useRankCourseGroupVersion2"] == '1' || $model->Properties["useRecordRankDetail"] == '1') {
            $query .= "        LEFT JOIN COURSE_GROUP_CD_DAT W2 ";
            $query .= "             ON W2.YEAR = W1.YEAR ";
            $query .= "            AND W2.GRADE = W1.GRADE ";
            $query .= "            AND W2.COURSECD = W1.COURSECD ";
            $query .= "            AND W2.MAJORCD = W1.MAJORCD ";
            $query .= "            AND W2.COURSECODE = W1.COURSECODE ";
        }
        //7:寮生
        if ($model->Properties["useAvgRyousei"] == '1') {
            $query .= "        LEFT JOIN SCHREG_REGD_GDAT S3 ";
            $query .= "             ON S3.YEAR         = W1.YEAR ";
            $query .= "            AND S3.GRADE        = W1.GRADE ";
            $query .= "        LEFT JOIN SCHREG_BRANCH_DAT S4 ";
            $query .= "             ON S4.SCHOOLCD     = '000000000000' ";
            $query .= "            AND S4.SCHOOL_KIND  = S3.SCHOOL_KIND ";
            $query .= "            AND S4.YEAR         = W1.YEAR ";
            $query .= "            AND S4.SCHREGNO     = W1.SCHREGNO ";
            $query .= "        LEFT JOIN V_NAME_MST N1 ";
            $query .= "             ON N1.YEAR         = W1.YEAR ";
            $query .= "            AND N1.NAMECD1      = 'J008' ";
            $query .= "            AND N1.NAMECD2      = S4.RESIDENTCD ";
        }
        //DIV = B,C,D,E,F
        if ($model->Properties["useRecordRankDetail"] == '1') {
            $query .= "        LEFT JOIN SCHREG_ENT_GRD_HIST_DAT SEGHD ";
            $query .= "             ON SEGHD.SCHREGNO = W1.SCHREGNO ";
            $query .= "            AND SEGHD.SCHOOL_KIND = 'J' ";
        }
        $query .= "    WHERE ";
        $query .= "        W1.YEAR = '".CTRL_YEAR."' AND ";
        $query .= "        W1.SEMESTER = '".$model->seme_sch."' AND ";
        $query .= "        W1.GRADE = '".$model->grade."' ";
        //指定された課程学科の生徒
        if ($model->Properties["use_school_detail_gcm_dat"] == '1') {
            $query .= "    AND W1.COURSECD || '-' || W1.MAJORCD = '{$model->course_major}' ";
        }
        return $query;
    }

    //成績クエリー
    public function getRecordQuery($model)
    {
        $query  = "";
        $query .= "    SELECT ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "    W1.CLASSCD, ";
            $query .= "    W1.SCHOOL_KIND, ";
            $query .= "    W1.CURRICULUM_CD, ";
        }
        $query .= "        W1.SUBCLASSCD, ";
        $query .= "        W1.SCHREGNO, ";
        //TODO1の箇所で使用
        if ($model->schoolName == "nichi-ni" && $model->sdiv == '08' && ($model->school_kind == 'J' || $model->school_kind == 'H' && $model->seme == '9')) {
            $query .= "        SCORE_DT.DEC_VAL1 as SCORE ";
            $query .= "       ,SCORE_DT.DEC_VAL1 as SCORE_NULL ";
        } elseif ($model->Properties["useRank_record_slump_sdiv_dat"] == '1' && $model->sidouInputInf == '2') {
            $query .= "        VALUE(SLUMP.SCORE, W1.SCORE) as SCORE ";
            $query .= "       ,VALUE(SLUMP.SCORE, W1.SCORE) as SCORE_NULL ";
        } else {
            $query .= "        W1.SCORE as SCORE ";
            $query .= "       ,W1.SCORE as SCORE_NULL ";
        }
        $query .= "       ,W1.VALUE_DI ";

        //TODO2の箇所で使用
        if ($model->seme == "9") {
            $query .= "   ,W1.COMP_CREDIT ";
            $query .= "   ,W1.GET_CREDIT ";
        }
        if ($model->Properties["useRankChairGroup"] == '1') {
            $query .= "   ,VALUE(CG.CHAIR_GROUP_CD, '000') AS CHAIR_GROUP_CD "; //MIN講座グループ（下３桁）
        }
        $query .= "       ,VALUE(L1.PERFECT, 100) AS PERFECT ";
        $query .= "    FROM ";
        $query .= "        RECORD_SCORE_DAT W1 ";
        $query .= "        INNER JOIN CLASS_YDAT CYDAT ON CYDAT.YEAR = W1.YEAR ";
        $query .= "            AND CYDAT.CLASSCD = W1.CLASSCD ";
        $query .= "            AND CYDAT.SCHOOL_KIND = W1.SCHOOL_KIND ";
        $query .= "        INNER JOIN SUBCLASS_YDAT SYDAT ON SYDAT.YEAR = W1.YEAR ";
        $query .= "            AND SYDAT.CLASSCD = W1.CLASSCD ";
        $query .= "            AND SYDAT.SCHOOL_KIND = W1.SCHOOL_KIND ";
        $query .= "            AND SYDAT.CURRICULUM_CD = W1.CURRICULUM_CD ";
        $query .= "            AND SYDAT.SUBCLASSCD = W1.SUBCLASSCD ";
        if ($model->schoolName == "nichi-ni" && $model->sdiv == '08' && ($model->school_kind == 'J' || $model->school_kind == 'H' && $model->seme == '9')) {
            $query .= "         LEFT JOIN RECORD_SCORE_DETAIL_DAT SCORE_DT ON W1.YEAR = SCORE_DT.YEAR ";
            $query .= "              AND W1.SEMESTER           = SCORE_DT.SEMESTER ";
            $query .= "              AND W1.TESTKINDCD         = SCORE_DT.TESTKINDCD ";
            $query .= "              AND W1.TESTITEMCD         = SCORE_DT.TESTITEMCD ";
            $query .= "              AND W1.SCORE_DIV          = SCORE_DT.SCORE_DIV ";
            $query .= "              AND W1.CLASSCD            = SCORE_DT.CLASSCD ";
            $query .= "              AND W1.SCHOOL_KIND        = SCORE_DT.SCHOOL_KIND ";
            $query .= "              AND W1.CURRICULUM_CD      = SCORE_DT.CURRICULUM_CD ";
            $query .= "              AND W1.SUBCLASSCD         = SCORE_DT.SUBCLASSCD ";
            $query .= "              AND W1.SCHREGNO           = SCORE_DT.SCHREGNO ";
            $query .= "              AND SCORE_DT.DETAIL_SEQ   = '001' ";
        } elseif ($model->Properties["useRank_record_slump_sdiv_dat"] == '1' && $model->sidouInputInf == '2') {
            $query .= "         LEFT JOIN RECORD_SLUMP_SDIV_DAT SLUMP ON W1.YEAR = SLUMP.YEAR ";
            $query .= "              AND W1.SEMESTER           = SLUMP.SEMESTER ";
            $query .= "              AND W1.TESTKINDCD         = SLUMP.TESTKINDCD ";
            $query .= "              AND W1.TESTITEMCD         = SLUMP.TESTITEMCD ";
            $query .= "              AND W1.SCORE_DIV          = SLUMP.SCORE_DIV ";
            $query .= "              AND W1.CLASSCD            = SLUMP.CLASSCD ";
            $query .= "              AND W1.SCHOOL_KIND        = SLUMP.SCHOOL_KIND ";
            $query .= "              AND W1.CURRICULUM_CD      = SLUMP.CURRICULUM_CD ";
            $query .= "              AND W1.SUBCLASSCD         = SLUMP.SUBCLASSCD ";
            $query .= "              AND W1.SCHREGNO           = SLUMP.SCHREGNO ";
        }
        $query .= "        INNER JOIN T_SCHREG W2 ON W2.SCHREGNO = W1.SCHREGNO ";
        $query .= "        LEFT JOIN PERFECT_RECORD_SDIV_DAT L1 ON L1.YEAR = W1.YEAR ";
        $query .= "             AND L1.SEMESTER = W1.SEMESTER ";
        $query .= "             AND L1.TESTKINDCD = W1.TESTKINDCD ";
        $query .= "             AND L1.TESTITEMCD = W1.TESTITEMCD ";
        $query .= "             AND L1.SCORE_DIV = W1.SCORE_DIV ";
        $query .= "             AND L1.CLASSCD = W1.CLASSCD ";
        $query .= "             AND L1.SCHOOL_KIND = W1.SCHOOL_KIND ";
        $query .= "             AND L1.CURRICULUM_CD = W1.CURRICULUM_CD ";
        $query .= "             AND L1.SUBCLASSCD = W1.SUBCLASSCD ";
        $query .= "             AND L1.GRADE = CASE WHEN L1.DIV = '01' THEN '00' ELSE W2.GRADE END ";
        $query .= "             AND L1.COURSECD || L1.MAJORCD || L1.COURSECODE = CASE WHEN L1.DIV IN ('01','02') THEN '00000000' ELSE W2.COURSECD || W2.MAJORCD || W2.COURSECODE END ";
        if ($model->Properties["useRankChairGroup"] == '1') {
            $query .= "        LEFT JOIN T_CHAIR_GROUP CG ON ";
            //教育課程対応
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "    CG.CLASSCD = W1.CLASSCD AND  ";
                $query .= "    CG.SCHOOL_KIND = W1.SCHOOL_KIND AND  ";
                $query .= "    CG.CURRICULUM_CD = W1.CURRICULUM_CD AND  ";
            }
            $query .= "        CG.SUBCLASSCD = W1.SUBCLASSCD AND CG.SCHREGNO = W1.SCHREGNO ";
        }
        $query .= "    WHERE ";
        $query .= "        W1.YEAR = '".CTRL_YEAR."' AND ";
        $query .= "        W1.SEMESTER = '".$model->seme."' AND ";
        $query .= "        W1.TESTKINDCD = '".$model->kind."' AND ";
        $query .= "        W1.TESTITEMCD = '".$model->item."' AND ";
        $query .= "        W1.SCORE_DIV = '".$model->sdiv."' ";
        return $query;
    }

    //(SCOREがNULL)かつ(VALUE_DIが*)の学籍番号を取得するクエリー
    public function getRecordNullQuery($model, $div)
    {
        $query  = "";
        $query .= "    SELECT ";
        $query .= "        T1.SCHREGNO ";
        $query .= "    FROM ";
        $query .= "        T_SCORE T1 ";
        $query .= "    WHERE ";
        //東大阪は公欠(+)忌引(-)のマークも参照する
        if ($model->schoolName == "higashiosaka") {
            $query .= "    T1.SCORE IS NULL AND T1.VALUE_DI IN ('*','+','-') ";
        } else {
            $query .= "    T1.SCORE IS NULL AND T1.VALUE_DI IN ('*', '**') ";
        }
        //洛南、土佐塾学年評価は合算科目の母集団から合併先科目を除く
        if ($model->schoolName == "rakunan" || $model->schoolName == "tosa" && $model->seme == '9' && $model->kind == '99' && $model->item == '00' && $model->sdiv == '08') {
            $query .= "    AND NOT EXISTS (SELECT 'X' FROM SUBCLASS_REPLACE_COMBINED_DAT W0 ";
            $query .= "           WHERE W0.YEAR = '".CTRL_YEAR."' ";
            //教育課程対応
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "         AND W0.COMBINED_CLASSCD = T1.CLASSCD ";
                $query .= "         AND W0.COMBINED_SCHOOL_KIND = T1.SCHOOL_KIND ";
                $query .= "         AND W0.COMBINED_CURRICULUM_CD = T1.CURRICULUM_CD ";
            }
            $query .= "             AND W0.COMBINED_SUBCLASSCD = T1.SUBCLASSCD) ";
        }
        //宮城県:合算科目の母集団から除く 1:合併元除く 2:合併先除く
        if ($model->d070Namespare1 == "1") {
            $query .= "    AND NOT EXISTS (SELECT 'X' FROM SUBCLASS_REPLACE_COMBINED_DAT W0 ";
            $query .= "           WHERE W0.YEAR = '".CTRL_YEAR."' ";
            //教育課程対応
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "         AND W0.ATTEND_CLASSCD = T1.CLASSCD ";
                $query .= "         AND W0.ATTEND_SCHOOL_KIND = T1.SCHOOL_KIND ";
                $query .= "         AND W0.ATTEND_CURRICULUM_CD = T1.CURRICULUM_CD ";
            }
            $query .= "             AND W0.ATTEND_SUBCLASSCD = T1.SUBCLASSCD) ";
        }
        if ($model->d070Namespare1 == "2") {
            $query .= "    AND NOT EXISTS (SELECT 'X' FROM SUBCLASS_REPLACE_COMBINED_DAT W0 ";
            $query .= "           WHERE W0.YEAR = '".CTRL_YEAR."' ";
            //教育課程対応
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "         AND W0.COMBINED_CLASSCD = T1.CLASSCD ";
                $query .= "         AND W0.COMBINED_SCHOOL_KIND = T1.SCHOOL_KIND ";
                $query .= "         AND W0.COMBINED_CURRICULUM_CD = T1.CURRICULUM_CD ";
            }
            $query .= "             AND W0.COMBINED_SUBCLASSCD = T1.SUBCLASSCD) ";
        }
        //選択科目を除く
        if ($model->electdiv == "1") {
            $query .= " AND NOT EXISTS( ";
            $query .= "        SELECT ";
            $query .= "            'X' ";
            $query .= "        FROM ";
            $query .= "            SUBCLASS_MST W1 ";
            $query .= "        WHERE ";
            $query .= "            W1.ELECTDIV = '1' AND ";
            //教育課程対応
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "        W1.CLASSCD = T1.CLASSCD AND ";
                $query .= "        W1.SCHOOL_KIND = T1.SCHOOL_KIND AND ";
                $query .= "        W1.CURRICULUM_CD = T1.CURRICULUM_CD AND ";
            }
            $query .= "            W1.SUBCLASSCD = T1.SUBCLASSCD ";
            $query .= "        ) ";
        }
        //３科目・５科目
        if ($div != "9" && $div != "A" && $div != "B") {
            $query .= " AND EXISTS( ";
            $query .= "        SELECT ";
            $query .= "            'X' ";
            $query .= "        FROM ";
            $query .= "            T_GROUP W1 ";
            $query .= "        WHERE ";
            $query .= "            W1.GROUP_DIV = '".$div."' AND ";
            $query .= "            W1.SCHREGNO = T1.SCHREGNO AND ";
            //教育課程対応
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "        W1.CLASSCD = T1.CLASSCD AND ";
                $query .= "        W1.SCHOOL_KIND = T1.SCHOOL_KIND AND ";
                $query .= "        W1.CURRICULUM_CD = T1.CURRICULUM_CD AND ";
            }
            $query .= "            W1.SUBCLASSCD = T1.SUBCLASSCD ";
            $query .= "        ) ";
        //全科目
        } else {
            //99999A：前期のみ科目
            if ($div == "A") {
                $query .= " AND T1.CLASSCD || T1.SCHOOL_KIND || T1.CURRICULUM_CD || T1.SUBCLASSCD     in (SELECT W2.CLASSCD || W2.SCHOOL_KIND || W2.CURRICULUM_CD || W2.SUBCLASSCD FROM SUBCLASS_DETAIL_DAT W2 WHERE W2.YEAR = '".CTRL_YEAR."' AND W2.SUBCLASS_SEQ = '012' AND SUBCLASS_REMARK1 = '1') ";
            }
            //99999B：後期のみ科目　＋　通年科目　（つまり、前期のみ科目を除く）
            if ($div == "B") {
                $query .= " AND T1.CLASSCD || T1.SCHOOL_KIND || T1.CURRICULUM_CD || T1.SUBCLASSCD not in (SELECT W2.CLASSCD || W2.SCHOOL_KIND || W2.CURRICULUM_CD || W2.SUBCLASSCD FROM SUBCLASS_DETAIL_DAT W2 WHERE W2.YEAR = '".CTRL_YEAR."' AND W2.SUBCLASS_SEQ = '012' AND SUBCLASS_REMARK1 = '1') ";
            }
            //教科科目名称マスタのマスタ化
            if ($model->Properties["useClassDetailDat"] == '1') {
                $query .= " AND T1.CLASSCD || T1.SCHOOL_KIND || T1.CURRICULUM_CD || T1.SUBCLASSCD not in (SELECT W2.CLASSCD || W2.SCHOOL_KIND || W2.CURRICULUM_CD || W2.SUBCLASSCD FROM SUBCLASS_DETAIL_DAT W2 WHERE W2.YEAR = '".CTRL_YEAR."' AND W2.SUBCLASS_SEQ = '004') ";
            } else {
                //名称マスタ「D017」NAME1に登録されている科目を除く。
                $query .= " AND T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD not in (SELECT W2.NAME1 FROM V_NAME_MST W2 WHERE W2.YEAR = '".CTRL_YEAR."' AND W2.NAMECD1 = 'D017') ";
            }
            //名称マスタ「D065」NAME1に登録されている科目を除く。
            $query .= " AND T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD not in (SELECT W2.NAME1 FROM V_NAME_MST W2 WHERE W2.YEAR = '".CTRL_YEAR."' AND W2.NAMECD1 = 'D065') ";
            //教科９０以上は除く。つまり、教科９０未満が対象
            $query .= " AND T1.SUBCLASSCD not like '9%' ";
            //「学年末」の場合、履修単位・修得単位の両方 NULL が対象・・・TODO2
            if ($model->seme == "9") {
                $query .= " AND T1.COMP_CREDIT IS NULL ";
                $query .= " AND T1.GET_CREDIT IS NULL ";
            }
            //素点・評価の両方 NULL が対象 TODO1
            $query .= " AND T1.SCORE_NULL IS NULL ";
        }
        $query .= "    GROUP BY T1.SCHREGNO ";
        return $query;
    }

    //各生徒の３科目・５科目・全科目のSCOREの合計を取得するクエリー
    public function getRecordScoreSumQuery($model)
    {
        $query  = "";
        $query .= "    SELECT  ";
        //教育課程対応・・・TODO：とりあえずこうしておく
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "    '33' as CLASSCD, ";
            $query .= "    max(T1.SCHOOL_KIND) as SCHOOL_KIND, ";
            $query .= "    '99' as CURRICULUM_CD, ";
        }
        $query .= "        '333333' as SUBCLASSCD, ";
        $query .= "        T1.SCHREGNO, ";
        $query .= "        DECIMAL(ROUND(SUM(FLOAT(T1.SCORE))/SUM(FLOAT(T1.PERFECT))*100*{$model->AvgDecimalPoint["waru"]},0)/{$model->AvgDecimalPoint["waru"]},{$model->AvgDecimalPoint["allketa"]},{$model->AvgDecimalPoint["keta"]}) AS AVG, ";
        $query .= "        SUM(CASE WHEN T1.SCORE IS NOT NULL THEN 1 ELSE 0 END) AS SCORE_CNT, ";
        $query .= "        SUM(T1.SCORE) AS SCORE ";
        $query .= "    FROM ";
        $query .= "        T_SCORE T1 ";
        $query .= "    WHERE ";
        $query .= "        EXISTS( ";
        $query .= "            SELECT ";
        $query .= "                'X' ";
        $query .= "            FROM ";
        $query .= "                T_GROUP W1 ";
        $query .= "            WHERE ";
        $query .= "                W1.GROUP_DIV = '3' AND ";
        $query .= "                W1.SCHREGNO = T1.SCHREGNO AND ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "            W1.CLASSCD = T1.CLASSCD AND ";
            $query .= "            W1.SCHOOL_KIND = T1.SCHOOL_KIND AND ";
            $query .= "            W1.CURRICULUM_CD = T1.CURRICULUM_CD AND ";
        }
        $query .= "                W1.SUBCLASSCD = T1.SUBCLASSCD ";
        $query .= "            ) AND ";
        $query .= "        T1.SCORE IS NOT NULL ";
        //洛南、土佐塾学年評価は合算科目の母集団から合併先科目を除く
        if ($model->schoolName == "rakunan" || $model->schoolName == "tosa" && $model->seme == '9' && $model->kind == '99' && $model->item == '00' && $model->sdiv == '08') {
            $query .= "    AND NOT EXISTS (SELECT 'X' FROM SUBCLASS_REPLACE_COMBINED_DAT W0 ";
            $query .= "           WHERE W0.YEAR = '".CTRL_YEAR."' ";
            //教育課程対応
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "         AND W0.COMBINED_CLASSCD = T1.CLASSCD ";
                $query .= "         AND W0.COMBINED_SCHOOL_KIND = T1.SCHOOL_KIND ";
                $query .= "         AND W0.COMBINED_CURRICULUM_CD = T1.CURRICULUM_CD ";
            }
            $query .= "             AND W0.COMBINED_SUBCLASSCD = T1.SUBCLASSCD) ";
        }
        //宮城県:合算科目の母集団から除く 1:合併元除く 2:合併先除く
        if ($model->d070Namespare1 == "1") {
            $query .= "    AND NOT EXISTS (SELECT 'X' FROM SUBCLASS_REPLACE_COMBINED_DAT W0 ";
            $query .= "           WHERE W0.YEAR = '".CTRL_YEAR."' ";
            //教育課程対応
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "         AND W0.ATTEND_CLASSCD = T1.CLASSCD ";
                $query .= "         AND W0.ATTEND_SCHOOL_KIND = T1.SCHOOL_KIND ";
                $query .= "         AND W0.ATTEND_CURRICULUM_CD = T1.CURRICULUM_CD ";
            }
            $query .= "             AND W0.ATTEND_SUBCLASSCD = T1.SUBCLASSCD) ";
        }
        if ($model->d070Namespare1 == "2") {
            $query .= "    AND NOT EXISTS (SELECT 'X' FROM SUBCLASS_REPLACE_COMBINED_DAT W0 ";
            $query .= "           WHERE W0.YEAR = '".CTRL_YEAR."' ";
            //教育課程対応
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "         AND W0.COMBINED_CLASSCD = T1.CLASSCD ";
                $query .= "         AND W0.COMBINED_SCHOOL_KIND = T1.SCHOOL_KIND ";
                $query .= "         AND W0.COMBINED_CURRICULUM_CD = T1.CURRICULUM_CD ";
            }
            $query .= "             AND W0.COMBINED_SUBCLASSCD = T1.SUBCLASSCD) ";
        }
        if ($model->Properties["knjd141cGroupSubclassNotTargetScoreNull"] == "1") {
            $query .= "    AND NOT EXISTS (SELECT 'X' ";
            $query .= "           FROM T_GROUP W1 ";
            $query .= "           LEFT JOIN T_SCORE W0 ON ";
            $query .= "             W0.SCHREGNO = W1.SCHREGNO ";
            //教育課程対応
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "         AND W0.CLASSCD = W1.CLASSCD ";
                $query .= "         AND W0.SCHOOL_KIND = W1.SCHOOL_KIND ";
                $query .= "         AND W0.CURRICULUM_CD = W1.CURRICULUM_CD ";
            }
            $query .= "             AND W0.SUBCLASSCD = W1.SUBCLASSCD ";
            $query .= "           WHERE W1.GROUP_DIV = '3' ";
            $query .= "             AND (W0.SCORE IS NULL OR W0.VALUE_DI IS NOT NULL) ";
            $query .= "             AND W1.SCHREGNO = T1.SCHREGNO ";
            $query .= "           ) ";
        }
        if ($model->narakenKekkaOverSubclass) {
            $query .= "    AND (T1.CLASSCD, T1.SCHOOL_KIND, T1.CURRICULUM_CD, T1.SUBCLASSCD, T1.SCHREGNO) NOT IN (SELECT * FROM NARAKEN_KEKKA_OVER_SUBCLASS) ";
        }
        $query .= "    GROUP BY T1.SCHREGNO ";
        $query .= "    UNION ALL ";
        $query .= "    SELECT  ";
        //教育課程対応・・・TODO：とりあえずこうしておく
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "    '55' as CLASSCD, ";
            $query .= "    max(T1.SCHOOL_KIND) as SCHOOL_KIND, ";
            $query .= "    '99' as CURRICULUM_CD, ";
        }
        $query .= "        '555555' as SUBCLASSCD, ";
        $query .= "        T1.SCHREGNO, ";
        $query .= "        DECIMAL(ROUND(SUM(FLOAT(T1.SCORE))/SUM(FLOAT(T1.PERFECT))*100*{$model->AvgDecimalPoint["waru"]},0)/{$model->AvgDecimalPoint["waru"]},{$model->AvgDecimalPoint["allketa"]},{$model->AvgDecimalPoint["keta"]}) AS AVG, ";
        $query .= "        SUM(CASE WHEN T1.SCORE IS NOT NULL THEN 1 ELSE 0 END) AS SCORE_CNT, ";
        $query .= "        SUM(T1.SCORE) AS SCORE ";
        $query .= "    FROM ";
        $query .= "        T_SCORE T1 ";
        $query .= "    WHERE ";
        $query .= "        EXISTS( ";
        $query .= "            SELECT ";
        $query .= "                'X' ";
        $query .= "            FROM ";
        $query .= "                T_GROUP W1 ";
        $query .= "            WHERE ";
        $query .= "                W1.GROUP_DIV = '5' AND ";
        $query .= "                W1.SCHREGNO = T1.SCHREGNO AND ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "            W1.CLASSCD = T1.CLASSCD AND ";
            $query .= "            W1.SCHOOL_KIND = T1.SCHOOL_KIND AND ";
            $query .= "            W1.CURRICULUM_CD = T1.CURRICULUM_CD AND ";
        }
        $query .= "                W1.SUBCLASSCD = T1.SUBCLASSCD ";
        $query .= "            ) AND ";
        $query .= "        T1.SCORE IS NOT NULL ";
        //洛南、土佐塾学年評価は合算科目の母集団から合併先科目を除く
        if ($model->schoolName == "rakunan" || $model->schoolName == "tosa" && $model->seme == '9' && $model->kind == '99' && $model->item == '00' && $model->sdiv == '08') {
            $query .= "    AND NOT EXISTS (SELECT 'X' FROM SUBCLASS_REPLACE_COMBINED_DAT W0 ";
            $query .= "           WHERE W0.YEAR = '".CTRL_YEAR."' ";
            //教育課程対応
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "         AND W0.COMBINED_CLASSCD = T1.CLASSCD ";
                $query .= "         AND W0.COMBINED_SCHOOL_KIND = T1.SCHOOL_KIND ";
                $query .= "         AND W0.COMBINED_CURRICULUM_CD = T1.CURRICULUM_CD ";
            }
            $query .= "             AND W0.COMBINED_SUBCLASSCD = T1.SUBCLASSCD) ";
        }
        //宮城県:合算科目の母集団から除く 1:合併元除く 2:合併先除く
        if ($model->d070Namespare1 == "1") {
            $query .= "    AND NOT EXISTS (SELECT 'X' FROM SUBCLASS_REPLACE_COMBINED_DAT W0 ";
            $query .= "           WHERE W0.YEAR = '".CTRL_YEAR."' ";
            //教育課程対応
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "         AND W0.ATTEND_CLASSCD = T1.CLASSCD ";
                $query .= "         AND W0.ATTEND_SCHOOL_KIND = T1.SCHOOL_KIND ";
                $query .= "         AND W0.ATTEND_CURRICULUM_CD = T1.CURRICULUM_CD ";
            }
            $query .= "             AND W0.ATTEND_SUBCLASSCD = T1.SUBCLASSCD) ";
        }
        if ($model->d070Namespare1 == "2") {
            $query .= "    AND NOT EXISTS (SELECT 'X' FROM SUBCLASS_REPLACE_COMBINED_DAT W0 ";
            $query .= "           WHERE W0.YEAR = '".CTRL_YEAR."' ";
            //教育課程対応
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "         AND W0.COMBINED_CLASSCD = T1.CLASSCD ";
                $query .= "         AND W0.COMBINED_SCHOOL_KIND = T1.SCHOOL_KIND ";
                $query .= "         AND W0.COMBINED_CURRICULUM_CD = T1.CURRICULUM_CD ";
            }
            $query .= "             AND W0.COMBINED_SUBCLASSCD = T1.SUBCLASSCD) ";
        }
        if ($model->Properties["knjd141cGroupSubclassNotTargetScoreNull"] == "1") {
            $query .= "    AND NOT EXISTS (SELECT 'X' ";
            $query .= "           FROM T_GROUP W1 ";
            $query .= "           LEFT JOIN T_SCORE W0 ON ";
            $query .= "             W0.SCHREGNO = W1.SCHREGNO ";
            //教育課程対応
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "         AND W0.CLASSCD = W1.CLASSCD ";
                $query .= "         AND W0.SCHOOL_KIND = W1.SCHOOL_KIND ";
                $query .= "         AND W0.CURRICULUM_CD = W1.CURRICULUM_CD ";
            }
            $query .= "             AND W0.SUBCLASSCD = W1.SUBCLASSCD ";
            $query .= "           WHERE W1.GROUP_DIV = '5' ";
            $query .= "             AND (W0.SCORE IS NULL OR W0.VALUE_DI IS NOT NULL) ";
            $query .= "             AND W1.SCHREGNO = T1.SCHREGNO ";
            $query .= "           ) ";
        }
        if ($model->narakenKekkaOverSubclass) {
            $query .= "    AND (T1.CLASSCD, T1.SCHOOL_KIND, T1.CURRICULUM_CD, T1.SUBCLASSCD, T1.SCHREGNO) NOT IN (SELECT * FROM NARAKEN_KEKKA_OVER_SUBCLASS) ";
        }
        $query .= "    GROUP BY T1.SCHREGNO ";
        $query .= "    UNION ALL ";
        $query .= "    SELECT  ";
        //教育課程対応・・・TODO：とりあえずこうしておく
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "    '99' as CLASSCD, ";
            $query .= "    max(T1.SCHOOL_KIND) as SCHOOL_KIND, ";
            $query .= "    '99' as CURRICULUM_CD, ";
        }
        $query .= "        '999999' as SUBCLASSCD, ";
        $query .= "        T1.SCHREGNO, ";
        $query .= "        DECIMAL(ROUND(SUM(FLOAT(T1.SCORE))/SUM(FLOAT(T1.PERFECT))*100*{$model->AvgDecimalPoint["waru"]},0)/{$model->AvgDecimalPoint["waru"]},{$model->AvgDecimalPoint["allketa"]},{$model->AvgDecimalPoint["keta"]}) AS AVG, ";
        $query .= "        SUM(CASE WHEN T1.SCORE IS NOT NULL THEN 1 ELSE 0 END) AS SCORE_CNT, ";
        $query .= "        SUM(T1.SCORE) AS SCORE ";
        $query .= "    FROM ";
        $query .= "        T_SCORE T1 ";
        $query .= "    WHERE ";
        $query .= "        T1.SCORE IS NOT NULL ";
        //選択科目を除く
        if ($model->electdiv == "1") {
            $query .= " AND NOT EXISTS( ";
            $query .= "        SELECT ";
            $query .= "            'X' ";
            $query .= "        FROM ";
            $query .= "            SUBCLASS_MST W1 ";
            $query .= "        WHERE ";
            $query .= "            W1.ELECTDIV = '1' AND ";
            //教育課程対応
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "        W1.CLASSCD = T1.CLASSCD AND ";
                $query .= "        W1.SCHOOL_KIND = T1.SCHOOL_KIND AND ";
                $query .= "        W1.CURRICULUM_CD = T1.CURRICULUM_CD AND ";
            }
            $query .= "            W1.SUBCLASSCD = T1.SUBCLASSCD ";
            $query .= "        ) ";
        }
        //教科科目名称マスタのマスタ化
        if ($model->Properties["useClassDetailDat"] == '1') {
            $query .= "     AND T1.CLASSCD || T1.SCHOOL_KIND || T1.CURRICULUM_CD || T1.SUBCLASSCD not in (SELECT W2.CLASSCD || W2.SCHOOL_KIND || W2.CURRICULUM_CD || W2.SUBCLASSCD FROM SUBCLASS_DETAIL_DAT W2 WHERE W2.YEAR = '".CTRL_YEAR."' AND W2.SUBCLASS_SEQ = '004') ";
        } else {
            //名称マスタ「D017」NAME1に登録されている科目を除く。
            $query .= "     AND T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD not in (SELECT W2.NAME1 FROM V_NAME_MST W2 WHERE W2.YEAR = '".CTRL_YEAR."' AND W2.NAMECD1 = 'D017') ";
        }
        //名称マスタ「D065」NAME1に登録されている科目を除く。
        $query .= " AND T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD not in (SELECT W2.NAME1 FROM V_NAME_MST W2 WHERE W2.YEAR = '".CTRL_YEAR."' AND W2.NAMECD1 = 'D065') ";
        //洛南、土佐塾学年評価は合算科目の母集団から合併先科目を除く
        if ($model->schoolName == "rakunan" || $model->schoolName == "tosa" && $model->seme == '9' && $model->kind == '99' && $model->item == '00' && $model->sdiv == '08') {
            $query .= "    AND NOT EXISTS (SELECT 'X' FROM SUBCLASS_REPLACE_COMBINED_DAT W0 ";
            $query .= "           WHERE W0.YEAR = '".CTRL_YEAR."' ";
            //教育課程対応
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "         AND W0.COMBINED_CLASSCD = T1.CLASSCD ";
                $query .= "         AND W0.COMBINED_SCHOOL_KIND = T1.SCHOOL_KIND ";
                $query .= "         AND W0.COMBINED_CURRICULUM_CD = T1.CURRICULUM_CD ";
            }
            $query .= "             AND W0.COMBINED_SUBCLASSCD = T1.SUBCLASSCD) ";
        }
        //宮城県:合算科目の母集団から除く 1:合併元除く 2:合併先除く
        if ($model->d070Namespare1 == "1") {
            $query .= "    AND NOT EXISTS (SELECT 'X' FROM SUBCLASS_REPLACE_COMBINED_DAT W0 ";
            $query .= "           WHERE W0.YEAR = '".CTRL_YEAR."' ";
            //教育課程対応
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "         AND W0.ATTEND_CLASSCD = T1.CLASSCD ";
                $query .= "         AND W0.ATTEND_SCHOOL_KIND = T1.SCHOOL_KIND ";
                $query .= "         AND W0.ATTEND_CURRICULUM_CD = T1.CURRICULUM_CD ";
            }
            $query .= "             AND W0.ATTEND_SUBCLASSCD = T1.SUBCLASSCD) ";
        }
        if ($model->d070Namespare1 == "2") {
            $query .= "    AND NOT EXISTS (SELECT 'X' FROM SUBCLASS_REPLACE_COMBINED_DAT W0 ";
            $query .= "           WHERE W0.YEAR = '".CTRL_YEAR."' ";
            //教育課程対応
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "         AND W0.COMBINED_CLASSCD = T1.CLASSCD ";
                $query .= "         AND W0.COMBINED_SCHOOL_KIND = T1.SCHOOL_KIND ";
                $query .= "         AND W0.COMBINED_CURRICULUM_CD = T1.CURRICULUM_CD ";
            }
            $query .= "             AND W0.COMBINED_SUBCLASSCD = T1.SUBCLASSCD) ";
        }
        if ($model->narakenKekkaOverSubclass) {
            $query .= "    AND (T1.CLASSCD, T1.SCHOOL_KIND, T1.CURRICULUM_CD, T1.SUBCLASSCD, T1.SCHREGNO) NOT IN (SELECT * FROM NARAKEN_KEKKA_OVER_SUBCLASS) ";
        }
        $query .= "    GROUP BY T1.SCHREGNO ";

        $query .= "    UNION ALL ";
        $query .= "    SELECT  ";
        //教育課程対応・・・TODO：とりあえずこうしておく
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "    '99' as CLASSCD, ";
            $query .= "    max(T1.SCHOOL_KIND) as SCHOOL_KIND, ";
            $query .= "    '99' as CURRICULUM_CD, ";
        }
        $query .= "        '99999A' as SUBCLASSCD, ";
        $query .= "        T1.SCHREGNO, ";
        $query .= "        DECIMAL(ROUND(SUM(FLOAT(T1.SCORE))/SUM(FLOAT(T1.PERFECT))*100*{$model->AvgDecimalPoint["waru"]},0)/{$model->AvgDecimalPoint["waru"]},{$model->AvgDecimalPoint["allketa"]},{$model->AvgDecimalPoint["keta"]}) AS AVG, ";
        $query .= "        SUM(CASE WHEN T1.SCORE IS NOT NULL THEN 1 ELSE 0 END) AS SCORE_CNT, ";
        $query .= "        SUM(T1.SCORE) AS SCORE ";
        $query .= "    FROM ";
        $query .= "        T_SCORE T1 ";
        $query .= "    WHERE ";
        $query .= "        T1.SCORE IS NOT NULL ";
        //99999A：前期のみ科目
        $query .= "     AND T1.CLASSCD || T1.SCHOOL_KIND || T1.CURRICULUM_CD || T1.SUBCLASSCD in (SELECT W2.CLASSCD || W2.SCHOOL_KIND || W2.CURRICULUM_CD || W2.SUBCLASSCD FROM SUBCLASS_DETAIL_DAT W2 WHERE W2.YEAR = '".CTRL_YEAR."' AND W2.SUBCLASS_SEQ = '012' AND SUBCLASS_REMARK1 = '1') ";
        //選択科目を除く
        if ($model->electdiv == "1") {
            $query .= " AND NOT EXISTS( ";
            $query .= "        SELECT ";
            $query .= "            'X' ";
            $query .= "        FROM ";
            $query .= "            SUBCLASS_MST W1 ";
            $query .= "        WHERE ";
            $query .= "            W1.ELECTDIV = '1' AND ";
            //教育課程対応
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "        W1.CLASSCD = T1.CLASSCD AND ";
                $query .= "        W1.SCHOOL_KIND = T1.SCHOOL_KIND AND ";
                $query .= "        W1.CURRICULUM_CD = T1.CURRICULUM_CD AND ";
            }
            $query .= "            W1.SUBCLASSCD = T1.SUBCLASSCD ";
            $query .= "        ) ";
        }
        //教科科目名称マスタのマスタ化
        if ($model->Properties["useClassDetailDat"] == '1') {
            $query .= "     AND T1.CLASSCD || T1.SCHOOL_KIND || T1.CURRICULUM_CD || T1.SUBCLASSCD not in (SELECT W2.CLASSCD || W2.SCHOOL_KIND || W2.CURRICULUM_CD || W2.SUBCLASSCD FROM SUBCLASS_DETAIL_DAT W2 WHERE W2.YEAR = '".CTRL_YEAR."' AND W2.SUBCLASS_SEQ = '004') ";
        } else {
            //名称マスタ「D017」NAME1に登録されている科目を除く。
            $query .= "     AND T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD not in (SELECT W2.NAME1 FROM V_NAME_MST W2 WHERE W2.YEAR = '".CTRL_YEAR."' AND W2.NAMECD1 = 'D017') ";
        }
        //名称マスタ「D065」NAME1に登録されている科目を除く。
        $query .= " AND T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD not in (SELECT W2.NAME1 FROM V_NAME_MST W2 WHERE W2.YEAR = '".CTRL_YEAR."' AND W2.NAMECD1 = 'D065') ";
        //洛南、土佐塾学年評価は合算科目の母集団から合併先科目を除く
        if ($model->schoolName == "rakunan" || $model->schoolName == "tosa" && $model->seme == '9' && $model->kind == '99' && $model->item == '00' && $model->sdiv == '08') {
            $query .= "    AND NOT EXISTS (SELECT 'X' FROM SUBCLASS_REPLACE_COMBINED_DAT W0 ";
            $query .= "           WHERE W0.YEAR = '".CTRL_YEAR."' ";
            //教育課程対応
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "         AND W0.COMBINED_CLASSCD = T1.CLASSCD ";
                $query .= "         AND W0.COMBINED_SCHOOL_KIND = T1.SCHOOL_KIND ";
                $query .= "         AND W0.COMBINED_CURRICULUM_CD = T1.CURRICULUM_CD ";
            }
            $query .= "             AND W0.COMBINED_SUBCLASSCD = T1.SUBCLASSCD) ";
        }
        //宮城県:合算科目の母集団から除く 1:合併元除く 2:合併先除く
        if ($model->d070Namespare1 == "1") {
            $query .= "    AND NOT EXISTS (SELECT 'X' FROM SUBCLASS_REPLACE_COMBINED_DAT W0 ";
            $query .= "           WHERE W0.YEAR = '".CTRL_YEAR."' ";
            //教育課程対応
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "         AND W0.ATTEND_CLASSCD = T1.CLASSCD ";
                $query .= "         AND W0.ATTEND_SCHOOL_KIND = T1.SCHOOL_KIND ";
                $query .= "         AND W0.ATTEND_CURRICULUM_CD = T1.CURRICULUM_CD ";
            }
            $query .= "             AND W0.ATTEND_SUBCLASSCD = T1.SUBCLASSCD) ";
        }
        if ($model->d070Namespare1 == "2") {
            $query .= "    AND NOT EXISTS (SELECT 'X' FROM SUBCLASS_REPLACE_COMBINED_DAT W0 ";
            $query .= "           WHERE W0.YEAR = '".CTRL_YEAR."' ";
            //教育課程対応
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "         AND W0.COMBINED_CLASSCD = T1.CLASSCD ";
                $query .= "         AND W0.COMBINED_SCHOOL_KIND = T1.SCHOOL_KIND ";
                $query .= "         AND W0.COMBINED_CURRICULUM_CD = T1.CURRICULUM_CD ";
            }
            $query .= "             AND W0.COMBINED_SUBCLASSCD = T1.SUBCLASSCD) ";
        }
        if ($model->narakenKekkaOverSubclass) {
            $query .= "    AND (T1.CLASSCD, T1.SCHOOL_KIND, T1.CURRICULUM_CD, T1.SUBCLASSCD, T1.SCHREGNO) NOT IN (SELECT * FROM NARAKEN_KEKKA_OVER_SUBCLASS) ";
        }
        $query .= "    GROUP BY T1.SCHREGNO ";

        $query .= "    UNION ALL ";
        $query .= "    SELECT  ";
        //教育課程対応・・・TODO：とりあえずこうしておく
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "    '99' as CLASSCD, ";
            $query .= "    max(T1.SCHOOL_KIND) as SCHOOL_KIND, ";
            $query .= "    '99' as CURRICULUM_CD, ";
        }
        $query .= "        '99999B' as SUBCLASSCD, ";
        $query .= "        T1.SCHREGNO, ";
        $query .= "        DECIMAL(ROUND(SUM(FLOAT(T1.SCORE))/SUM(FLOAT(T1.PERFECT))*100*{$model->AvgDecimalPoint["waru"]},0)/{$model->AvgDecimalPoint["waru"]},{$model->AvgDecimalPoint["allketa"]},{$model->AvgDecimalPoint["keta"]}) AS AVG, ";
        $query .= "        SUM(CASE WHEN T1.SCORE IS NOT NULL THEN 1 ELSE 0 END) AS SCORE_CNT, ";
        $query .= "        SUM(T1.SCORE) AS SCORE ";
        $query .= "    FROM ";
        $query .= "        T_SCORE T1 ";
        $query .= "    WHERE ";
        $query .= "        T1.SCORE IS NOT NULL ";
        //99999B：後期のみ科目　＋　通年科目　（つまり、前期のみ科目を除く）
        $query .= "     AND T1.CLASSCD || T1.SCHOOL_KIND || T1.CURRICULUM_CD || T1.SUBCLASSCD not in (SELECT W2.CLASSCD || W2.SCHOOL_KIND || W2.CURRICULUM_CD || W2.SUBCLASSCD FROM SUBCLASS_DETAIL_DAT W2 WHERE W2.YEAR = '".CTRL_YEAR."' AND W2.SUBCLASS_SEQ = '012' AND SUBCLASS_REMARK1 = '1') ";
        //選択科目を除く
        if ($model->electdiv == "1") {
            $query .= " AND NOT EXISTS( ";
            $query .= "        SELECT ";
            $query .= "            'X' ";
            $query .= "        FROM ";
            $query .= "            SUBCLASS_MST W1 ";
            $query .= "        WHERE ";
            $query .= "            W1.ELECTDIV = '1' AND ";
            //教育課程対応
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "        W1.CLASSCD = T1.CLASSCD AND ";
                $query .= "        W1.SCHOOL_KIND = T1.SCHOOL_KIND AND ";
                $query .= "        W1.CURRICULUM_CD = T1.CURRICULUM_CD AND ";
            }
            $query .= "            W1.SUBCLASSCD = T1.SUBCLASSCD ";
            $query .= "        ) ";
        }
        //教科科目名称マスタのマスタ化
        if ($model->Properties["useClassDetailDat"] == '1') {
            $query .= "     AND T1.CLASSCD || T1.SCHOOL_KIND || T1.CURRICULUM_CD || T1.SUBCLASSCD not in (SELECT W2.CLASSCD || W2.SCHOOL_KIND || W2.CURRICULUM_CD || W2.SUBCLASSCD FROM SUBCLASS_DETAIL_DAT W2 WHERE W2.YEAR = '".CTRL_YEAR."' AND W2.SUBCLASS_SEQ = '004') ";
        } else {
            //名称マスタ「D017」NAME1に登録されている科目を除く。
            $query .= "     AND T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD not in (SELECT W2.NAME1 FROM V_NAME_MST W2 WHERE W2.YEAR = '".CTRL_YEAR."' AND W2.NAMECD1 = 'D017') ";
        }
        //名称マスタ「D065」NAME1に登録されている科目を除く。
        $query .= " AND T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD not in (SELECT W2.NAME1 FROM V_NAME_MST W2 WHERE W2.YEAR = '".CTRL_YEAR."' AND W2.NAMECD1 = 'D065') ";
        //洛南、土佐塾学年評価は合算科目の母集団から合併先科目を除く
        if ($model->schoolName == "rakunan" || $model->schoolName == "tosa" && $model->seme == '9' && $model->kind == '99' && $model->item == '00' && $model->sdiv == '08') {
            $query .= "    AND NOT EXISTS (SELECT 'X' FROM SUBCLASS_REPLACE_COMBINED_DAT W0 ";
            $query .= "           WHERE W0.YEAR = '".CTRL_YEAR."' ";
            //教育課程対応
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "         AND W0.COMBINED_CLASSCD = T1.CLASSCD ";
                $query .= "         AND W0.COMBINED_SCHOOL_KIND = T1.SCHOOL_KIND ";
                $query .= "         AND W0.COMBINED_CURRICULUM_CD = T1.CURRICULUM_CD ";
            }
            $query .= "             AND W0.COMBINED_SUBCLASSCD = T1.SUBCLASSCD) ";
        }
        //宮城県:合算科目の母集団から除くフラグ 1:合併元除く 2:合併先除く
        if ($model->d070Namespare1 == "1") {
            $query .= "    AND NOT EXISTS (SELECT 'X' FROM SUBCLASS_REPLACE_COMBINED_DAT W0 ";
            $query .= "           WHERE W0.YEAR = '".CTRL_YEAR."' ";
            //教育課程対応
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "         AND W0.ATTEND_CLASSCD = T1.CLASSCD ";
                $query .= "         AND W0.ATTEND_SCHOOL_KIND = T1.SCHOOL_KIND ";
                $query .= "         AND W0.ATTEND_CURRICULUM_CD = T1.CURRICULUM_CD ";
            }
            $query .= "             AND W0.ATTEND_SUBCLASSCD = T1.SUBCLASSCD) ";
        }
        if ($model->d070Namespare1 == "2") {
            $query .= "    AND NOT EXISTS (SELECT 'X' FROM SUBCLASS_REPLACE_COMBINED_DAT W0 ";
            $query .= "           WHERE W0.YEAR = '".CTRL_YEAR."' ";
            //教育課程対応
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "         AND W0.COMBINED_CLASSCD = T1.CLASSCD ";
                $query .= "         AND W0.COMBINED_SCHOOL_KIND = T1.SCHOOL_KIND ";
                $query .= "         AND W0.COMBINED_CURRICULUM_CD = T1.CURRICULUM_CD ";
            }
            $query .= "             AND W0.COMBINED_SUBCLASSCD = T1.SUBCLASSCD) ";
        }
        if ($model->narakenKekkaOverSubclass) {
            $query .= "    AND (T1.CLASSCD, T1.SCHOOL_KIND, T1.CURRICULUM_CD, T1.SUBCLASSCD, T1.SCHREGNO) NOT IN (SELECT * FROM NARAKEN_KEKKA_OVER_SUBCLASS) ";
        }
        $query .= "    GROUP BY T1.SCHREGNO ";

        return $query;
    }

    //成績平均データクエリー
    public function getRecordAverageQuery($model, $table)
    {
        $field_name  = ($table == "record_rank_chair") ? "CHAIRCD," : "";
        $table_name  = ($table == "record_rank_chair") ? "record_average_chair" : "record_average";
        $query  = "";
        $query .= "    SELECT ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "    CLASSCD, ";
            $query .= "    SCHOOL_KIND, ";
            $query .= "    CURRICULUM_CD, ";
        }
        $query .= "        SUBCLASSCD, ";
        $query .= $field_name;
        $query .= "        AVG_DIV, ";
        $query .= "        GRADE, ";
        $query .= "        HR_CLASS, ";
        $query .= "        COURSECD||MAJORCD||COURSECODE AS COURSE, ";
        $query .= "        COURSECD||MAJORCD||'0000' AS MAJOR, ";
        //5:コースグループ
        if ($model->Properties["useRankCourseGroup"] == '1' || $model->Properties["useRankCourseGroupVersion2"] == '1' || $model->Properties["useRecordRankDetail"] == '1') {
            $query .= "        MAJORCD AS COURSE_GROUP, ";
        }
        //6:講座グループ
        if ($model->Properties["useRankChairGroup"] == '1') {
            $query .= "        MAJORCD AS CHAIR_GROUP_CD, ";
        }
        $query .= "        DECIMAL(ROUND(FLOAT(AVG)*10,0)/10,5,1) AS AVG,STDDEV ";
        $query .= "    FROM ";
        $query .= $table_name .$model->tableStr;
        $query .= "    WHERE ";
        $query .= "        YEAR = '".CTRL_YEAR."' AND ";
        $query .= "        SEMESTER = '".$model->seme."' AND ";
        $query .= "        TESTKINDCD = '".$model->kind."' AND ";
        $query .= "        TESTITEMCD = '".$model->item."' AND ";
        $query .= "        SCORE_DIV = '".$model->sdiv."' AND ";
        $query .= "        GRADE = '".$model->grade."' ";
        return $query;
    }

    //在籍&成績２クエリー
    public function getRecordSchregQuery($model, $table)
    {
        $query  = "";
        $query .= "    SELECT ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "    W1.CLASSCD, ";
            $query .= "    W1.SCHOOL_KIND, ";
            $query .= "    W1.CURRICULUM_CD, ";
        }
        $query .= "        W1.SUBCLASSCD, ";
        $query .= "        W1.SCHREGNO, ";
        $query .= "        SCORE, ";
        //KNJD129Qにて、平均点欄を表示・・・入力不可（表示のみ）・・・駿台甲府要望
        //(9-9900-08)各科目のRECORD_RANK_SDIV_DAT.AVGに保持しておく。
        $testcd = $model->seme.$model->kind.$model->item.$model->sdiv;
        if ($model->Properties["showHeikinran"] == '1' && $testcd == '9990008') {
            $query .= "        REC.SCHNO_AVG as AVG, ";
        } else {
            $query .= "        cast(null as decimal) as AVG, ";
        }
        $query .= "        GRADE, ";
        $query .= "        HR_CLASS, ";
        $query .= "        COURSECD,MAJORCD,COURSECODE, ";
        $query .= "        COURSE, ";
        $query .= "        MAJOR ";
        //5:コースグループ
        if ($model->Properties["useRankCourseGroup"] == '1' || $model->Properties["useRankCourseGroupVersion2"] == '1' || $model->Properties["useRecordRankDetail"] == '1') {
            $query .= "       ,COURSE_GROUP ";
        }
        //6:講座グループ
        if ($model->Properties["useRankChairGroup"] == '1') {
            $query .= "       ,W1.CHAIR_GROUP_CD ";
        }
        //7:寮生
        if ($model->Properties["useAvgRyousei"] == '1') {
            $query .= "       ,RYOUSEI_FLG ";
        }
        //DIV = B,C,D,E,F
        if ($model->Properties["useRecordRankDetail"] == '1') {
            $query .= "       ,ENT_JH ";
        }
        $query .= "    FROM ";
        $query .= "        T_SCORE W1 ";
        $query .= "        INNER JOIN T_SCHREG W2 ON W2.SCHREGNO = W1.SCHREGNO ";
        //KNJD129Qにて、平均点欄を表示・・・入力不可（表示のみ）・・・駿台甲府要望
        //(9-9900-08)各科目のRECORD_RANK_SDIV_DAT.AVGに保持しておく。
        if ($model->Properties["showHeikinran"] == '1' && $testcd == '9990008') {
            $query .= "    LEFT JOIN RECORD_AVG REC ON REC.SCHREGNO = W1.SCHREGNO ";
            //教育課程対応
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "        AND REC.CLASSCD       = W1.CLASSCD ";
                $query .= "        AND REC.SCHOOL_KIND   = W1.SCHOOL_KIND ";
                $query .= "        AND REC.CURRICULUM_CD = W1.CURRICULUM_CD ";
            }
            $query .= "            AND REC.SUBCLASSCD    = W1.SUBCLASSCD ";
        }
        $query .= "    WHERE ";
        $query .= "        SCORE IS NOT NULL ";
        if ($model->narakenKekkaOverSubclass) {
            $query .= "    AND (W1.CLASSCD, W1.SCHOOL_KIND, W1.CURRICULUM_CD, W1.SUBCLASSCD, W1.SCHREGNO) NOT IN (SELECT * FROM NARAKEN_KEKKA_OVER_SUBCLASS) ";
        }
        if ($table == "record_average" || $table == "record_rank") {
            $query .= "    UNION ALL ";
            $query .= "    SELECT ";
            //教育課程対応
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "    W1.CLASSCD, ";
                $query .= "    W1.SCHOOL_KIND, ";
                $query .= "    W1.CURRICULUM_CD, ";
            }
            $query .= "        W1.SUBCLASSCD, ";
            $query .= "        W1.SCHREGNO, ";
            $query .= "        SCORE, ";
            $query .= "        AVG, ";
            $query .= "        GRADE, ";
            $query .= "        HR_CLASS, ";
            $query .= "        COURSECD,MAJORCD,COURSECODE, ";
            $query .= "        COURSE, ";
            $query .= "        MAJOR ";
            //5:コースグループ
            if ($model->Properties["useRankCourseGroup"] == '1' || $model->Properties["useRankCourseGroupVersion2"] == '1' || $model->Properties["useRecordRankDetail"] == '1') {
                $query .= "       ,COURSE_GROUP ";
            }
            //6:講座グループ
            if ($model->Properties["useRankChairGroup"] == '1') {
                $query .= "       ,'000' AS CHAIR_GROUP_CD ";
            }
            //7:寮生
            if ($model->Properties["useAvgRyousei"] == '1') {
                $query .= "       ,RYOUSEI_FLG ";
            }
            //DIV = B,C,D,E,F
            if ($model->Properties["useRecordRankDetail"] == '1') {
                $query .= "       ,ENT_JH ";
            }
            $query .= "    FROM ";
            $query .= "        T_SCORE_SUM W1, ";
            $query .= "        T_SCHREG W2 ";
            $query .= "    WHERE ";
            $query .= "        W1.SCHREGNO = W2.SCHREGNO ";
            $query .= "     AND ( ";
            $query .= "         (W1.SUBCLASSCD  = '333333' AND NOT EXISTS(SELECT 'X' FROM T_SCORE_NULL3 W3 WHERE W3.SCHREGNO = W2.SCHREGNO)) ";
            $query .= "      OR (W1.SUBCLASSCD  = '555555' AND NOT EXISTS(SELECT 'X' FROM T_SCORE_NULL5 W3 WHERE W3.SCHREGNO = W2.SCHREGNO)) ";
            $query .= "      OR (W1.SUBCLASSCD  = '999999' AND NOT EXISTS(SELECT 'X' FROM T_SCORE_NULL  W3 WHERE W3.SCHREGNO = W2.SCHREGNO)) ";
            $query .= "      OR (W1.SUBCLASSCD  = '99999A' AND NOT EXISTS(SELECT 'X' FROM T_SCORE_NULLA W3 WHERE W3.SCHREGNO = W2.SCHREGNO)) ";
            $query .= "      OR (W1.SUBCLASSCD  = '99999B' AND NOT EXISTS(SELECT 'X' FROM T_SCORE_NULLB W3 WHERE W3.SCHREGNO = W2.SCHREGNO)) ";
            $query .= "         ) ";
        }
        return $query;
    }

    //成績平均データの追加クエリー
    public function getInsertAverageQuery($model, $table)
    {
        $field_name  = ($table == "record_average_chair") ? "CHAIRCD," : "";
        $table_name  = ($table == "record_average_chair") ? "_CHAIR" : "";
        $query  = "";
        //1:学年
        $query .= "SELECT ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "    CLASSCD, ";
            $query .= "    SCHOOL_KIND, ";
            $query .= "    CURRICULUM_CD, ";
        }
        $query .= "    SUBCLASSCD, ";
        $query .= $field_name;
        $query .= "    '1' as AVG_DIV, ";
        $query .= "    GRADE, ";
        $query .= "    '000' as HR_CLASS, ";
        $query .= "    '0' as COURSECD, ";
        $query .= "    '000' as MAJORCD, ";
        $query .= "    '0000' as COURSECODE, ";
        $query .= "    SUM(SCORE) AS SCORE, ";
        $query .= "    MAX(SCORE) AS HIGHSCORE, ";
        $query .= "    MIN(SCORE) AS LOWSCORE, ";
        if ($model->Properties["useRecordRankDetail"] == '1') {
            $query .= "    MAX(AVG) AS HIGHSCORE_AVG, ";
            $query .= "    MIN(AVG) AS LOWSCORE_AVG, ";
        }
        $query .= "    COUNT(SCHREGNO) AS COUNT, ";
        $query .= "    DECIMAL(ROUND(AVG(FLOAT(SCORE))*{$model->AvgDecimalPoint["waru"]},0)/{$model->AvgDecimalPoint["waru"]},{$model->AvgDecimalPoint["allketa"]},{$model->AvgDecimalPoint["keta"]}) AS AVG, ";
        $query .= "    DECIMAL(ROUND(STDDEV(FLOAT(SCORE))*10,0)/10,5,1) AS STDDEV ";
        $query .= "FROM ";
        $query .= "    T_RANK" .$table_name ." ";
        //処理科目(総合計のみ)
        if ($model->subclasscd == '999999') {
            $query .= "  WHERE SUBCLASSCD in ('333333','555555','999999','99999A','99999B') ";
        //処理科目(各科目のみ)
        } elseif ($model->subclasscd != '000000') {
            $query .= "  WHERE CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD = '".$model->subclasscd."' ";
        }
        $query .= "GROUP BY ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "    CLASSCD, ";
            $query .= "    SCHOOL_KIND, ";
            $query .= "    CURRICULUM_CD, ";
        }
        $query .= "    SUBCLASSCD, ";
        $query .= $field_name;
        $query .= "    GRADE ";
        //2:クラス
        $query .= "UNION ALL ";
        $query .= "SELECT ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "    CLASSCD, ";
            $query .= "    SCHOOL_KIND, ";
            $query .= "    CURRICULUM_CD, ";
        }
        $query .= "    SUBCLASSCD, ";
        $query .= $field_name;
        $query .= "    '2' as AVG_DIV, ";
        $query .= "    GRADE, ";
        $query .= "    HR_CLASS, ";
        $query .= "    '0' as COURSECD, ";
        $query .= "    '000' as MAJORCD, ";
        $query .= "    '0000' as COURSECODE, ";
        $query .= "    SUM(SCORE) AS SCORE, ";
        $query .= "    MAX(SCORE) AS HIGHSCORE, ";
        $query .= "    MIN(SCORE) AS LOWSCORE, ";
        if ($model->Properties["useRecordRankDetail"] == '1') {
            $query .= "    MAX(AVG) AS HIGHSCORE_AVG, ";
            $query .= "    MIN(AVG) AS LOWSCORE_AVG, ";
        }
        $query .= "    COUNT(SCHREGNO) AS COUNT, ";
        $query .= "    DECIMAL(ROUND(AVG(FLOAT(SCORE))*{$model->AvgDecimalPoint["waru"]},0)/{$model->AvgDecimalPoint["waru"]},{$model->AvgDecimalPoint["allketa"]},{$model->AvgDecimalPoint["keta"]}) AS AVG, ";
        $query .= "    DECIMAL(ROUND(STDDEV(FLOAT(SCORE))*10,0)/10,5,1) AS STDDEV ";
        $query .= "FROM ";
        $query .= "    T_RANK" .$table_name ." ";
        //処理科目(総合計のみ)
        if ($model->subclasscd == '999999') {
            $query .= "  WHERE SUBCLASSCD in ('333333','555555','999999','99999A','99999B') ";
        //処理科目(各科目のみ)
        } elseif ($model->subclasscd != '000000') {
            $query .= "  WHERE CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD = '".$model->subclasscd."' ";
        }
        $query .= "GROUP BY ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "    CLASSCD, ";
            $query .= "    SCHOOL_KIND, ";
            $query .= "    CURRICULUM_CD, ";
        }
        $query .= "    SUBCLASSCD, ";
        $query .= $field_name;
        $query .= "    GRADE, ";
        $query .= "    HR_CLASS ";
        //3:コース
        $query .= "UNION ALL ";
        $query .= "SELECT ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "    CLASSCD, ";
            $query .= "    SCHOOL_KIND, ";
            $query .= "    CURRICULUM_CD, ";
        }
        $query .= "    SUBCLASSCD, ";
        $query .= $field_name;
        $query .= "    '3' as AVG_DIV, ";
        $query .= "    GRADE, ";
        $query .= "    '000' as HR_CLASS, ";
        $query .= "    COURSECD, ";
        $query .= "    MAJORCD, ";
        $query .= "    COURSECODE, ";
        $query .= "    SUM(SCORE) AS SCORE, ";
        $query .= "    MAX(SCORE) AS HIGHSCORE, ";
        $query .= "    MIN(SCORE) AS LOWSCORE, ";
        if ($model->Properties["useRecordRankDetail"] == '1') {
            $query .= "    MAX(AVG) AS HIGHSCORE_AVG, ";
            $query .= "    MIN(AVG) AS LOWSCORE_AVG, ";
        }
        $query .= "    COUNT(SCHREGNO) AS COUNT, ";
        $query .= "    DECIMAL(ROUND(AVG(FLOAT(SCORE))*{$model->AvgDecimalPoint["waru"]},0)/{$model->AvgDecimalPoint["waru"]},{$model->AvgDecimalPoint["allketa"]},{$model->AvgDecimalPoint["keta"]}) AS AVG, ";
        $query .= "    DECIMAL(ROUND(STDDEV(FLOAT(SCORE))*10,0)/10,5,1) AS STDDEV ";
        $query .= "FROM ";
        $query .= "    T_RANK" .$table_name ." ";
        //処理科目(総合計のみ)
        if ($model->subclasscd == '999999') {
            $query .= "  WHERE SUBCLASSCD in ('333333','555555','999999','99999A','99999B') ";
        //処理科目(各科目のみ)
        } elseif ($model->subclasscd != '000000') {
            $query .= "  WHERE CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD = '".$model->subclasscd."' ";
        }
        $query .= "GROUP BY ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "    CLASSCD, ";
            $query .= "    SCHOOL_KIND, ";
            $query .= "    CURRICULUM_CD, ";
        }
        $query .= "    SUBCLASSCD, ";
        $query .= $field_name;
        $query .= "    GRADE, ";
        $query .= "    COURSECD, ";
        $query .= "    MAJORCD, ";
        $query .= "    COURSECODE ";
        //4:学科
        $query .= "UNION ALL ";
        $query .= "SELECT ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "    CLASSCD, ";
            $query .= "    SCHOOL_KIND, ";
            $query .= "    CURRICULUM_CD, ";
        }
        $query .= "    SUBCLASSCD, ";
        $query .= $field_name;
        $query .= "    '4' as AVG_DIV, ";
        $query .= "    GRADE, ";
        $query .= "    '000' as HR_CLASS, ";
        $query .= "    COURSECD, ";
        $query .= "    MAJORCD, ";
        $query .= "    '0000' AS COURSECODE, ";
        $query .= "    SUM(SCORE) AS SCORE, ";
        $query .= "    MAX(SCORE) AS HIGHSCORE, ";
        $query .= "    MIN(SCORE) AS LOWSCORE, ";
        if ($model->Properties["useRecordRankDetail"] == '1') {
            $query .= "    MAX(AVG) AS HIGHSCORE_AVG, ";
            $query .= "    MIN(AVG) AS LOWSCORE_AVG, ";
        }
        $query .= "    COUNT(SCHREGNO) AS COUNT, ";
        $query .= "    DECIMAL(ROUND(AVG(FLOAT(SCORE))*{$model->AvgDecimalPoint["waru"]},0)/{$model->AvgDecimalPoint["waru"]},{$model->AvgDecimalPoint["allketa"]},{$model->AvgDecimalPoint["keta"]}) AS AVG, ";
        $query .= "    DECIMAL(ROUND(STDDEV(FLOAT(SCORE))*10,0)/10,5,1) AS STDDEV ";
        $query .= "FROM ";
        $query .= "    T_RANK" .$table_name ." ";
        //処理科目(総合計のみ)
        if ($model->subclasscd == '999999') {
            $query .= "  WHERE SUBCLASSCD in ('333333','555555','999999','99999A','99999B') ";
        //処理科目(各科目のみ)
        } elseif ($model->subclasscd != '000000') {
            $query .= "  WHERE CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD = '".$model->subclasscd."' ";
        }
        $query .= "GROUP BY ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "    CLASSCD, ";
            $query .= "    SCHOOL_KIND, ";
            $query .= "    CURRICULUM_CD, ";
        }
        $query .= "    SUBCLASSCD, ";
        $query .= $field_name;
        $query .= "    GRADE, ";
        $query .= "    COURSECD, ";
        $query .= "    MAJORCD ";
        //5:コースグループ
        if ($model->Properties["useRankCourseGroup"] == '1' || $model->Properties["useRankCourseGroupVersion2"] == '1' || $model->Properties["useRecordRankDetail"] == '1') {
            $query .= "UNION ALL ";
            $query .= "SELECT ";
            //教育課程対応
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "    CLASSCD, ";
                $query .= "    SCHOOL_KIND, ";
                $query .= "    CURRICULUM_CD, ";
            }
            $query .= "    SUBCLASSCD, ";
            $query .= $field_name;
            $query .= "    '5' as AVG_DIV, ";
            $query .= "    GRADE, ";
            $query .= "    '000' as HR_CLASS, ";
            $query .= "    '0' as COURSECD, ";
            $query .= "    COURSE_GROUP as MAJORCD, ";
            $query .= "    '0000' AS COURSECODE, ";
            $query .= "    SUM(SCORE) AS SCORE, ";
            $query .= "    MAX(SCORE) AS HIGHSCORE, ";
            $query .= "    MIN(SCORE) AS LOWSCORE, ";
            if ($model->Properties["useRecordRankDetail"] == '1') {
                $query .= "    MAX(AVG) AS HIGHSCORE_AVG, ";
                $query .= "    MIN(AVG) AS LOWSCORE_AVG, ";
            }
            $query .= "    COUNT(SCHREGNO) AS COUNT, ";
            $query .= "    DECIMAL(ROUND(AVG(FLOAT(SCORE))*{$model->AvgDecimalPoint["waru"]},0)/{$model->AvgDecimalPoint["waru"]},{$model->AvgDecimalPoint["allketa"]},{$model->AvgDecimalPoint["keta"]}) AS AVG, ";
            $query .= "    DECIMAL(ROUND(STDDEV(FLOAT(SCORE))*10,0)/10,5,1) AS STDDEV ";
            $query .= "FROM ";
            $query .= "    T_RANK" .$table_name ." ";
            //処理科目(総合計のみ)
            if ($model->subclasscd == '999999') {
                $query .= "  WHERE SUBCLASSCD in ('333333','555555','999999','99999A','99999B') ";
            //処理科目(各科目のみ)
            } elseif ($model->subclasscd != '000000') {
                $query .= "  WHERE CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD = '".$model->subclasscd."' ";
            }
            $query .= "GROUP BY ";
            //教育課程対応
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "    CLASSCD, ";
                $query .= "    SCHOOL_KIND, ";
                $query .= "    CURRICULUM_CD, ";
            }
            $query .= "    SUBCLASSCD, ";
            $query .= $field_name;
            $query .= "    GRADE, ";
            $query .= "    COURSE_GROUP ";
        }
        //A:クラス別コースグループ
        if ($model->Properties["useRecordRankDetail"] == '1') {
            $query .= "UNION ALL ";
            $query .= "SELECT ";
            //教育課程対応
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "    CLASSCD, ";
                $query .= "    SCHOOL_KIND, ";
                $query .= "    CURRICULUM_CD, ";
            }
            $query .= "    SUBCLASSCD, ";
            $query .= $field_name;
            $query .= "    'A' as AVG_DIV, ";
            $query .= "    GRADE, ";
            $query .= "    HR_CLASS, ";
            $query .= "    '0' as COURSECD, ";
            $query .= "    COURSE_GROUP as MAJORCD, ";
            $query .= "    '0000' AS COURSECODE, ";
            $query .= "    SUM(SCORE) AS SCORE, ";
            $query .= "    MAX(SCORE) AS HIGHSCORE, ";
            $query .= "    MIN(SCORE) AS LOWSCORE, ";
            if ($model->Properties["useRecordRankDetail"] == '1') {
                $query .= "    MAX(AVG) AS HIGHSCORE_AVG, ";
                $query .= "    MIN(AVG) AS LOWSCORE_AVG, ";
            }
            $query .= "    COUNT(SCHREGNO) AS COUNT, ";
            $query .= "    DECIMAL(ROUND(AVG(FLOAT(SCORE))*{$model->AvgDecimalPoint["waru"]},0)/{$model->AvgDecimalPoint["waru"]},{$model->AvgDecimalPoint["allketa"]},{$model->AvgDecimalPoint["keta"]}) AS AVG, ";
            $query .= "    DECIMAL(ROUND(STDDEV(FLOAT(SCORE))*10,0)/10,5,1) AS STDDEV ";
            $query .= "FROM ";
            $query .= "    T_RANK" .$table_name ." ";
            //処理科目(総合計のみ)
            if ($model->subclasscd == '999999') {
                $query .= "  WHERE SUBCLASSCD in ('333333','555555','999999','99999A','99999B') ";
            //処理科目(各科目のみ)
            } elseif ($model->subclasscd != '000000') {
                $query .= "  WHERE CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD = '".$model->subclasscd."' ";
            }
            $query .= "GROUP BY ";
            //教育課程対応
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "    CLASSCD, ";
                $query .= "    SCHOOL_KIND, ";
                $query .= "    CURRICULUM_CD, ";
            }
            $query .= "    SUBCLASSCD, ";
            $query .= $field_name;
            $query .= "    GRADE, ";
            $query .= "    HR_CLASS, ";
            $query .= "    COURSE_GROUP ";
        }
        //6:講座グループ
        if ($model->Properties["useRankChairGroup"] == '1') {
            $query .= "UNION ALL ";
            $query .= "SELECT ";
            //教育課程対応
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "    CLASSCD, ";
                $query .= "    SCHOOL_KIND, ";
                $query .= "    CURRICULUM_CD, ";
            }
            $query .= "    SUBCLASSCD, ";
            $query .= $field_name;
            $query .= "    '6' as AVG_DIV, ";
            $query .= "    GRADE, ";
            $query .= "    '000' as HR_CLASS, ";
            $query .= "    '0' as COURSECD, ";
            $query .= "    CHAIR_GROUP_CD as MAJORCD, ";
            $query .= "    '0000' as COURSECODE, ";
            $query .= "    SUM(SCORE) AS SCORE, ";
            $query .= "    MAX(SCORE) AS HIGHSCORE, ";
            $query .= "    MIN(SCORE) AS LOWSCORE, ";
            if ($model->Properties["useRecordRankDetail"] == '1') {
                $query .= "    MAX(AVG) AS HIGHSCORE_AVG, ";
                $query .= "    MIN(AVG) AS LOWSCORE_AVG, ";
            }
            $query .= "    COUNT(SCHREGNO) AS COUNT, ";
            $query .= "    DECIMAL(ROUND(AVG(FLOAT(SCORE))*{$model->AvgDecimalPoint["waru"]},0)/{$model->AvgDecimalPoint["waru"]},{$model->AvgDecimalPoint["allketa"]},{$model->AvgDecimalPoint["keta"]}) AS AVG, ";
            $query .= "    DECIMAL(ROUND(STDDEV(FLOAT(SCORE))*10,0)/10,5,1) AS STDDEV ";
            $query .= "FROM ";
            $query .= "    T_RANK" .$table_name ." ";
            //処理科目(総合計のみ)
            if ($model->subclasscd == '999999') {
                $query .= "  WHERE SUBCLASSCD in ('333333','555555','999999','99999A','99999B') ";
            //処理科目(各科目のみ)
            } elseif ($model->subclasscd != '000000') {
                $query .= "  WHERE CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD = '".$model->subclasscd."' ";
            }
            $query .= "GROUP BY ";
            //教育課程対応
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "    CLASSCD, ";
                $query .= "    SCHOOL_KIND, ";
                $query .= "    CURRICULUM_CD, ";
            }
            $query .= "    SUBCLASSCD, ";
            $query .= $field_name;
            $query .= "    GRADE, ";
            $query .= "    CHAIR_GROUP_CD ";
        }
        //7:寮生
        if ($model->Properties["useAvgRyousei"] == '1') {
            $query .= "UNION ALL ";
            $query .= "SELECT ";
            //教育課程対応
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "    CLASSCD, ";
                $query .= "    SCHOOL_KIND, ";
                $query .= "    CURRICULUM_CD, ";
            }
            $query .= "    SUBCLASSCD, ";
            $query .= $field_name;
            $query .= "    '7' as AVG_DIV, ";
            $query .= "    GRADE, ";
            $query .= "    '000' as HR_CLASS, ";
            $query .= "    '0' as COURSECD, ";
            $query .= "    '000' as MAJORCD, ";
            $query .= "    '0000' as COURSECODE, ";
            $query .= "    SUM(SCORE) AS SCORE, ";
            $query .= "    MAX(SCORE) AS HIGHSCORE, ";
            $query .= "    MIN(SCORE) AS LOWSCORE, ";
            if ($model->Properties["useRecordRankDetail"] == '1') {
                $query .= "    MAX(AVG) AS HIGHSCORE_AVG, ";
                $query .= "    MIN(AVG) AS LOWSCORE_AVG, ";
            }
            $query .= "    COUNT(SCHREGNO) AS COUNT, ";
            $query .= "    DECIMAL(ROUND(AVG(FLOAT(SCORE))*{$model->AvgDecimalPoint["waru"]},0)/{$model->AvgDecimalPoint["waru"]},{$model->AvgDecimalPoint["allketa"]},{$model->AvgDecimalPoint["keta"]}) AS AVG, ";
            $query .= "    DECIMAL(ROUND(STDDEV(FLOAT(SCORE))*10,0)/10,5,1) AS STDDEV ";
            $query .= "FROM ";
            $query .= "    T_RANK" .$table_name ." ";
            $query .= "WHERE ";
            $query .= "    RYOUSEI_FLG = '1' ";
            //処理科目(総合計のみ)
            if ($model->subclasscd == '999999') {
                $query .= "  AND SUBCLASSCD in ('333333','555555','999999','99999A','99999B') ";
            //処理科目(各科目のみ)
            } elseif ($model->subclasscd != '000000') {
                $query .= "  AND CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD = '".$model->subclasscd."' ";
            }
            $query .= "GROUP BY ";
            //教育課程対応
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "    CLASSCD, ";
                $query .= "    SCHOOL_KIND, ";
                $query .= "    CURRICULUM_CD, ";
            }
            $query .= "    SUBCLASSCD, ";
            $query .= $field_name;
            $query .= "    GRADE ";
        }
        //DIV = B,C,D,E,F
        if ($model->Properties["useRecordRankDetail"] == '1') {
            $rankDivArray = array("B", "C", "D", "E", "F");
            foreach ($rankDivArray as $key => $val) {
                $query .= "UNION ALL ";
                $query .= "SELECT ";
                $query .= "    CLASSCD, ";
                $query .= "    SCHOOL_KIND, ";
                $query .= "    CURRICULUM_CD, ";
                $query .= "    SUBCLASSCD, ";
                $query .= $field_name;
                $query .= "    '{$val}' as AVG_DIV, ";
                $query .= "    GRADE, ";
                if ($val == "B" || $val == "C" || $val == "D") {
                    $query .= "    HR_CLASS, ";
                } elseif ($val == "E" || $val == "F") {
                    $query .= "    '000' as HR_CLASS, ";
                }
                $query .= "    COURSECD, ";
                $query .= "    MAJORCD, ";
                $query .= "    COURSECODE, ";
                $query .= "    SUM(SCORE) AS SCORE, ";
                $query .= "    MAX(SCORE) AS HIGHSCORE, ";
                $query .= "    MIN(SCORE) AS LOWSCORE, ";
                if ($model->Properties["useRecordRankDetail"] == '1') {
                    $query .= "    MAX(AVG) AS HIGHSCORE_AVG, ";
                    $query .= "    MIN(AVG) AS LOWSCORE_AVG, ";
                }
                $query .= "    COUNT(SCHREGNO) AS COUNT, ";
                $query .= "    DECIMAL(ROUND(AVG(FLOAT(SCORE))*{$model->AvgDecimalPoint["waru"]},0)/{$model->AvgDecimalPoint["waru"]},{$model->AvgDecimalPoint["allketa"]},{$model->AvgDecimalPoint["keta"]}) AS AVG, ";
                $query .= "    DECIMAL(ROUND(STDDEV(FLOAT(SCORE))*10,0)/10,5,1) AS STDDEV ";
                $query .= "FROM ";
                $query .= "    T_RANK" .$table_name ." ";
                if ($val == "C" || $val == "E") {
                    $query .= "    WHERE ENT_JH = 'H' ";
                } elseif ($val == "D" || $val == "F") {
                    $query .= "    WHERE ENT_JH = 'J' ";
                }
                $whereOrAnd = ($val == "B") ? "WHERE" : "AND";
                //処理科目(総合計のみ)
                if ($model->subclasscd == '999999') {
                    $query .= "  {$whereOrAnd} SUBCLASSCD in ('333333','555555','999999','99999A','99999B') ";
                //処理科目(各科目のみ)
                } elseif ($model->subclasscd != '000000') {
                    $query .= "  {$whereOrAnd} CLASSCD || '-' || SCHOOL_KIND || '-' || CURRICULUM_CD || '-' || SUBCLASSCD = '".$model->subclasscd."' ";
                }
                $query .= "GROUP BY ";
                $query .= "    CLASSCD, ";
                $query .= "    SCHOOL_KIND, ";
                $query .= "    CURRICULUM_CD, ";
                $query .= "    SUBCLASSCD, ";
                $query .= $field_name;
                $query .= "    GRADE, ";
                if ($val == "B" || $val == "C" || $val == "D") {
                    $query .= "    HR_CLASS, ";
                }
                $query .= "    COURSECD, ";
                $query .= "    MAJORCD, ";
                $query .= "    COURSECODE ";
            }
        }
        return $query;
    }

    //成績席次データの追加クエリー
    public function getInsertRankQuery($model, $table)
    {
        $field_name  = $table_name  = $where_name2 = $where_name3 = $where_name4 = $where_name5 = $where_name6 = $where_name7 = $where_name8 = "";
        $where_nameB = $where_nameC = $where_nameD = $where_nameE = $where_nameF = "";
        if ($table == "record_rank_chair") {
            $field_name  = "W1.CHAIRCD,";
            $table_name  = "_CHAIR";
            $where_name2 = "W2.CHAIRCD = W1.CHAIRCD AND ";
            $where_name3 = "W3.CHAIRCD = W1.CHAIRCD AND ";
            $where_name4 = "W4.CHAIRCD = W1.CHAIRCD AND ";
            $where_name5 = "W5.CHAIRCD = W1.CHAIRCD AND ";
            $where_name6 = "W6.CHAIRCD = W1.CHAIRCD AND ";
            $where_name7 = "W7.CHAIRCD = W1.CHAIRCD AND ";
            $where_name8 = "W8.CHAIRCD = W1.CHAIRCD AND ";
            $where_nameB = "WB.CHAIRCD = W1.CHAIRCD AND ";
            $where_nameC = "WC.CHAIRCD = W1.CHAIRCD AND ";
            $where_nameD = "WD.CHAIRCD = W1.CHAIRCD AND ";
            $where_nameE = "WE.CHAIRCD = W1.CHAIRCD AND ";
            $where_nameF = "WF.CHAIRCD = W1.CHAIRCD AND ";
        }
        //教育課程対応
        $fieldCrc = $whereCrc2 = $whereCrc3 = $whereCrc4 = $whereCrc5 = $whereCrc6 = $whereCrc7 = $whereCrc8 = "";
        $whereCrcB = $whereCrcC = $whereCrcD = $whereCrcE = $whereCrcF = "";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $fieldCrc   = "W1.CLASSCD, W1.SCHOOL_KIND, W1.CURRICULUM_CD, ";
            $whereCrc2  = "W2.CLASSCD = W1.CLASSCD AND W2.SCHOOL_KIND = W1.SCHOOL_KIND AND W2.CURRICULUM_CD = W1.CURRICULUM_CD AND ";
            $whereCrc3  = "W3.CLASSCD = W1.CLASSCD AND W3.SCHOOL_KIND = W1.SCHOOL_KIND AND W3.CURRICULUM_CD = W1.CURRICULUM_CD AND ";
            $whereCrc4  = "W4.CLASSCD = W1.CLASSCD AND W4.SCHOOL_KIND = W1.SCHOOL_KIND AND W4.CURRICULUM_CD = W1.CURRICULUM_CD AND ";
            $whereCrc5  = "W5.CLASSCD = W1.CLASSCD AND W5.SCHOOL_KIND = W1.SCHOOL_KIND AND W5.CURRICULUM_CD = W1.CURRICULUM_CD AND ";
            $whereCrc6  = "W6.CLASSCD = W1.CLASSCD AND W6.SCHOOL_KIND = W1.SCHOOL_KIND AND W6.CURRICULUM_CD = W1.CURRICULUM_CD AND ";
            $whereCrc7  = "W7.CLASSCD = W1.CLASSCD AND W7.SCHOOL_KIND = W1.SCHOOL_KIND AND W7.CURRICULUM_CD = W1.CURRICULUM_CD AND ";
            $whereCrc8  = "W8.CLASSCD = W1.CLASSCD AND W8.SCHOOL_KIND = W1.SCHOOL_KIND AND W8.CURRICULUM_CD = W1.CURRICULUM_CD AND ";
            $whereCrcB  = "WB.CLASSCD = W1.CLASSCD AND WB.SCHOOL_KIND = W1.SCHOOL_KIND AND WB.CURRICULUM_CD = W1.CURRICULUM_CD AND ";
            $whereCrcC  = "WC.CLASSCD = W1.CLASSCD AND WC.SCHOOL_KIND = W1.SCHOOL_KIND AND WC.CURRICULUM_CD = W1.CURRICULUM_CD AND ";
            $whereCrcD  = "WD.CLASSCD = W1.CLASSCD AND WD.SCHOOL_KIND = W1.SCHOOL_KIND AND WD.CURRICULUM_CD = W1.CURRICULUM_CD AND ";
            $whereCrcE  = "WE.CLASSCD = W1.CLASSCD AND WE.SCHOOL_KIND = W1.SCHOOL_KIND AND WE.CURRICULUM_CD = W1.CURRICULUM_CD AND ";
            $whereCrcF  = "WF.CLASSCD = W1.CLASSCD AND WF.SCHOOL_KIND = W1.SCHOOL_KIND AND WF.CURRICULUM_CD = W1.CURRICULUM_CD AND ";
        }
        $query  = "";

        if ($table == "record_rank" && $model->calcDeviationFlg == "1") {
            //各科目の偏差値
            $query .= ",T_DEVIATION_SUB AS ( ";
            $query .= "     SELECT ";
            $query .= "         {$fieldCrc}W1.SUBCLASSCD, ";
            $query .= $field_name;
            $query .= "         W1.SCHREGNO, ";
            //5:コースグループ
            if ($model->Properties["useRankCourseGroup"] == '1' || $model->Properties["useRankCourseGroupVersion2"] == '1' || $model->Properties["useRecordRankDetail"] == '1') {
                $query .= "     DECIMAL(ROUND((10*(case when 0 < W6.STDDEV then (W1.SCORE-W6.AVG)/W6.STDDEV else 0 end)+50)*10,0)/10,5,1) AS COURSE_GROUP_DEVIATION, ";
            }
            //A:クラス別コースグループ
            if ($model->Properties["useRecordRankDetail"] == '1') {
                $query .= "     DECIMAL(ROUND((10*(case when 0 < W8.STDDEV then (W1.SCORE-W8.AVG)/W8.STDDEV else 0 end)+50)*10,0)/10,5,1) AS COURSE_GROUP_CL_DEVIATION, ";
            }
            //6:講座グループ
            if ($model->Properties["useRankChairGroup"] == '1') {
                $query .= "     DECIMAL(ROUND((10*(case when 0 < W7.STDDEV then (W1.SCORE-W7.AVG)/W7.STDDEV else 0 end)+50)*10,0)/10,5,1) AS CHAIR_GROUP_DEVIATION, ";
            }
            //DIV = B,C,D,E,F
            if ($model->Properties["useRecordRankDetail"] == '1') {
                $query .= "     DECIMAL(ROUND((10*(case when 0 < WB.STDDEV then (W1.SCORE-WB.AVG)/WB.STDDEV else 0 end)+50)*10,0)/10,5,1) AS B_DEVIATION, ";
                $query .= "     CASE WHEN W1.ENT_JH = 'H' THEN DECIMAL(ROUND((10*(case when 0 < WC.STDDEV then (W1.SCORE-WC.AVG)/WC.STDDEV else 0 end)+50)*10,0)/10,5,1) ELSE NULL END AS C_DEVIATION, ";
                $query .= "     CASE WHEN W1.ENT_JH = 'J' THEN DECIMAL(ROUND((10*(case when 0 < WD.STDDEV then (W1.SCORE-WD.AVG)/WD.STDDEV else 0 end)+50)*10,0)/10,5,1) ELSE NULL END AS D_DEVIATION, ";
                $query .= "     CASE WHEN W1.ENT_JH = 'H' THEN DECIMAL(ROUND((10*(case when 0 < WE.STDDEV then (W1.SCORE-WE.AVG)/WE.STDDEV else 0 end)+50)*10,0)/10,5,1) ELSE NULL END AS E_DEVIATION, ";
                $query .= "     CASE WHEN W1.ENT_JH = 'J' THEN DECIMAL(ROUND((10*(case when 0 < WF.STDDEV then (W1.SCORE-WF.AVG)/WF.STDDEV else 0 end)+50)*10,0)/10,5,1) ELSE NULL END AS F_DEVIATION, ";
            }
            $query .= "         DECIMAL(ROUND((10*(case when 0 < W2.STDDEV then (W1.SCORE-W2.AVG)/W2.STDDEV else 0 end)+50)*10,0)/10,5,1) AS GRADE_DEVIATION, ";
            $query .= "         DECIMAL(ROUND((10*(case when 0 < W3.STDDEV then (W1.SCORE-W3.AVG)/W3.STDDEV else 0 end)+50)*10,0)/10,5,1) AS CLASS_DEVIATION, ";
            $query .= "         DECIMAL(ROUND((10*(case when 0 < W4.STDDEV then (W1.SCORE-W4.AVG)/W4.STDDEV else 0 end)+50)*10,0)/10,5,1) AS COURSE_DEVIATION, ";
            $query .= "         DECIMAL(ROUND((10*(case when 0 < W5.STDDEV then (W1.SCORE-W5.AVG)/W5.STDDEV else 0 end)+50)*10,0)/10,5,1) AS MAJOR_DEVIATION ";
            $query .= "     FROM ";
            $query .= "         T_RANK" .$table_name ." W1 ";
            $query .= "         LEFT JOIN T_AVERAGE W2 ON W2.AVG_DIV = '1' AND {$whereCrc2}W2.SUBCLASSCD = W1.SUBCLASSCD AND " .$where_name2 ."W2.GRADE = W1.GRADE ";
            $query .= "         LEFT JOIN T_AVERAGE W3 ON W3.AVG_DIV = '2' AND {$whereCrc3}W3.SUBCLASSCD = W1.SUBCLASSCD AND " .$where_name3 ."W3.GRADE = W1.GRADE AND W3.HR_CLASS = W1.HR_CLASS ";
            $query .= "         LEFT JOIN T_AVERAGE W4 ON W4.AVG_DIV = '3' AND {$whereCrc4}W4.SUBCLASSCD = W1.SUBCLASSCD AND " .$where_name4 ."W4.GRADE = W1.GRADE AND W4.COURSE = W1.COURSE ";
            $query .= "         LEFT JOIN T_AVERAGE W5 ON W5.AVG_DIV = '4' AND {$whereCrc5}W5.SUBCLASSCD = W1.SUBCLASSCD AND " .$where_name5 ."W5.GRADE = W1.GRADE AND W5.MAJOR = W1.MAJOR ";
            //5:コースグループ
            if ($model->Properties["useRankCourseGroup"] == '1' || $model->Properties["useRankCourseGroupVersion2"] == '1' || $model->Properties["useRecordRankDetail"] == '1') {
                $query .= "     LEFT JOIN T_AVERAGE W6 ON W6.AVG_DIV = '5' AND {$whereCrc6}W6.SUBCLASSCD = W1.SUBCLASSCD AND " .$where_name6 ."W6.GRADE = W1.GRADE AND W6.COURSE_GROUP = W1.COURSE_GROUP ";
            }
            //A:クラス別コースグループ
            if ($model->Properties["useRecordRankDetail"] == '1') {
                $query .= "     LEFT JOIN T_AVERAGE W8 ON W8.AVG_DIV = 'A' AND {$whereCrc8}W8.SUBCLASSCD = W1.SUBCLASSCD AND " .$where_name8 ."W8.GRADE = W1.GRADE AND W8.HR_CLASS = W1.HR_CLASS AND W8.COURSE_GROUP = W1.COURSE_GROUP ";
            }
            //6:講座グループ
            if ($model->Properties["useRankChairGroup"] == '1') {
                $query .= "     LEFT JOIN T_AVERAGE W7 ON W7.AVG_DIV = '6' AND {$whereCrc7}W7.SUBCLASSCD = W1.SUBCLASSCD AND " .$where_name7 ."W7.GRADE = W1.GRADE AND W7.CHAIR_GROUP_CD = W1.CHAIR_GROUP_CD ";
            }
            //DIV = B,C,D,E,F
            if ($model->Properties["useRecordRankDetail"] == '1') {
                $query .= "     LEFT JOIN T_AVERAGE WB ON WB.AVG_DIV = 'B' AND {$whereCrcB}WB.SUBCLASSCD = W1.SUBCLASSCD AND " .$where_nameB ."WB.GRADE = W1.GRADE AND WB.HR_CLASS = W1.HR_CLASS AND WB.COURSE = W1.COURSE ";
                $query .= "     LEFT JOIN T_AVERAGE WC ON WC.AVG_DIV = 'C' AND {$whereCrcC}WC.SUBCLASSCD = W1.SUBCLASSCD AND " .$where_nameC ."WC.GRADE = W1.GRADE AND WC.HR_CLASS = W1.HR_CLASS AND WC.COURSE = W1.COURSE ";
                $query .= "     LEFT JOIN T_AVERAGE WD ON WD.AVG_DIV = 'D' AND {$whereCrcD}WD.SUBCLASSCD = W1.SUBCLASSCD AND " .$where_nameD ."WD.GRADE = W1.GRADE AND WD.HR_CLASS = W1.HR_CLASS AND WD.COURSE = W1.COURSE ";
                $query .= "     LEFT JOIN T_AVERAGE WE ON WE.AVG_DIV = 'E' AND {$whereCrcE}WE.SUBCLASSCD = W1.SUBCLASSCD AND " .$where_nameE ."WE.GRADE = W1.GRADE AND WE.COURSE = W1.COURSE ";
                $query .= "     LEFT JOIN T_AVERAGE WF ON WF.AVG_DIV = 'F' AND {$whereCrcF}WF.SUBCLASSCD = W1.SUBCLASSCD AND " .$where_nameF ."WF.GRADE = W1.GRADE AND WF.COURSE = W1.COURSE ";
            }
            $query .= "     WHERE ";
            $query .= "         W1.SUBCLASSCD not in ('333333','555555','999999','99999A','99999B') ";
            $query .= "    ) ";
            //３科・５科・全科の偏差値
            $query .= ",T_DEVIATION_ALL AS ( ";
            $query .= "    SELECT  ";
            //教育課程対応・・・TODO：とりあえずこうしておく
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "    '33' as CLASSCD, ";
                $query .= "    max(T1.SCHOOL_KIND) as SCHOOL_KIND, ";
                $query .= "    '99' as CURRICULUM_CD, ";
            }
            $query .= "        '333333' as SUBCLASSCD, ";
            $query .= "        T1.SCHREGNO, ";
            //5:コースグループ
            if ($model->Properties["useRankCourseGroup"] == '1' || $model->Properties["useRankCourseGroupVersion2"] == '1' || $model->Properties["useRecordRankDetail"] == '1') {
                $query .= "        DECIMAL(ROUND(AVG(T1.COURSE_GROUP_DEVIATION)*10,0)/10,5,1) AS COURSE_GROUP_DEVIATION, ";
            }
            //A:クラス別コースグループ
            if ($model->Properties["useRecordRankDetail"] == '1') {
                $query .= "        DECIMAL(ROUND(AVG(T1.COURSE_GROUP_CL_DEVIATION)*10,0)/10,5,1) AS COURSE_GROUP_CL_DEVIATION, ";
            }
            //6:講座グループ
            if ($model->Properties["useRankChairGroup"] == '1') {
                $query .= "        DECIMAL(ROUND(AVG(T1.CHAIR_GROUP_DEVIATION) *10,0)/10,5,1) AS CHAIR_GROUP_DEVIATION, ";
            }
            //DIV = B,C,D,E,F
            if ($model->Properties["useRecordRankDetail"] == '1') {
                $query .= "        DECIMAL(ROUND(AVG(T1.B_DEVIATION)*10,0)/10,5,1) AS B_DEVIATION, ";
                $query .= "        DECIMAL(ROUND(AVG(T1.C_DEVIATION)*10,0)/10,5,1) AS C_DEVIATION, ";
                $query .= "        DECIMAL(ROUND(AVG(T1.D_DEVIATION)*10,0)/10,5,1) AS D_DEVIATION, ";
                $query .= "        DECIMAL(ROUND(AVG(T1.E_DEVIATION)*10,0)/10,5,1) AS E_DEVIATION, ";
                $query .= "        DECIMAL(ROUND(AVG(T1.F_DEVIATION)*10,0)/10,5,1) AS F_DEVIATION, ";
            }
            //DIV = G
            if ($model->Properties["useRecordRankDetail"] == '1' && $model->Properties["knjd141c_deviationTotalRank"] == '1') {
                $query .= "        SUM(T1.GRADE_DEVIATION) AS GRADE_DEVIATION_TOTAL, ";
            }
            $query .= "        DECIMAL(ROUND(AVG(T1.GRADE_DEVIATION) *10,0)/10,5,1) AS GRADE_DEVIATION, ";
            $query .= "        DECIMAL(ROUND(AVG(T1.CLASS_DEVIATION) *10,0)/10,5,1) AS CLASS_DEVIATION, ";
            $query .= "        DECIMAL(ROUND(AVG(T1.COURSE_DEVIATION)*10,0)/10,5,1) AS COURSE_DEVIATION, ";
            $query .= "        DECIMAL(ROUND(AVG(T1.MAJOR_DEVIATION) *10,0)/10,5,1) AS MAJOR_DEVIATION ";
            $query .= "    FROM ";
            $query .= "        T_DEVIATION_SUB T1 ";
            $query .= "    WHERE ";
            $query .= "        EXISTS( ";
            $query .= "            SELECT ";
            $query .= "                'X' ";
            $query .= "            FROM ";
            $query .= "                T_GROUP W1 ";
            $query .= "            WHERE ";
            $query .= "                W1.GROUP_DIV = '3' AND ";
            $query .= "                W1.SCHREGNO = T1.SCHREGNO AND ";
            //教育課程対応
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "            W1.CLASSCD = T1.CLASSCD AND ";
                $query .= "            W1.SCHOOL_KIND = T1.SCHOOL_KIND AND ";
                $query .= "            W1.CURRICULUM_CD = T1.CURRICULUM_CD AND ";
            }
            $query .= "                W1.SUBCLASSCD = T1.SUBCLASSCD ";
            $query .= "            ) ";
            //洛南、土佐塾学年評価は合算科目の母集団から合併先科目を除く
            if ($model->schoolName == "rakunan" || $model->schoolName == "tosa" && $model->seme == '9' && $model->kind == '99' && $model->item == '00' && $model->sdiv == '08') {
                $query .= "    AND NOT EXISTS (SELECT 'X' FROM SUBCLASS_REPLACE_COMBINED_DAT W0 ";
                $query .= "           WHERE W0.YEAR = '".CTRL_YEAR."' ";
                //教育課程対応
                if ($model->Properties["useCurriculumcd"] == '1') {
                    $query .= "         AND W0.COMBINED_CLASSCD = T1.CLASSCD ";
                    $query .= "         AND W0.COMBINED_SCHOOL_KIND = T1.SCHOOL_KIND ";
                    $query .= "         AND W0.COMBINED_CURRICULUM_CD = T1.CURRICULUM_CD ";
                }
                $query .= "             AND W0.COMBINED_SUBCLASSCD = T1.SUBCLASSCD) ";
            }
            //宮城県:合算科目の母集団から除く 1:合併元除く 2:合併先除く
            if ($model->d070Namespare1 == "1") {
                $query .= "    AND NOT EXISTS (SELECT 'X' FROM SUBCLASS_REPLACE_COMBINED_DAT W0 ";
                $query .= "           WHERE W0.YEAR = '".CTRL_YEAR."' ";
                //教育課程対応
                if ($model->Properties["useCurriculumcd"] == '1') {
                    $query .= "         AND W0.ATTEND_CLASSCD = T1.CLASSCD ";
                    $query .= "         AND W0.ATTEND_SCHOOL_KIND = T1.SCHOOL_KIND ";
                    $query .= "         AND W0.ATTEND_CURRICULUM_CD = T1.CURRICULUM_CD ";
                }
                $query .= "             AND W0.ATTEND_SUBCLASSCD = T1.SUBCLASSCD) ";
            }
            if ($model->d070Namespare1 == "2") {
                $query .= "    AND NOT EXISTS (SELECT 'X' FROM SUBCLASS_REPLACE_COMBINED_DAT W0 ";
                $query .= "           WHERE W0.YEAR = '".CTRL_YEAR."' ";
                //教育課程対応
                if ($model->Properties["useCurriculumcd"] == '1') {
                    $query .= "         AND W0.COMBINED_CLASSCD = T1.CLASSCD ";
                    $query .= "         AND W0.COMBINED_SCHOOL_KIND = T1.SCHOOL_KIND ";
                    $query .= "         AND W0.COMBINED_CURRICULUM_CD = T1.CURRICULUM_CD ";
                }
                $query .= "             AND W0.COMBINED_SUBCLASSCD = T1.SUBCLASSCD) ";
            }
            $query .= "    GROUP BY T1.SCHREGNO ";
            $query .= "    UNION ALL ";
            $query .= "    SELECT  ";
            //教育課程対応・・・TODO：とりあえずこうしておく
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "    '55' as CLASSCD, ";
                $query .= "    max(T1.SCHOOL_KIND) as SCHOOL_KIND, ";
                $query .= "    '99' as CURRICULUM_CD, ";
            }
            $query .= "        '555555' as SUBCLASSCD, ";
            $query .= "        T1.SCHREGNO, ";
            //5:コースグループ
            if ($model->Properties["useRankCourseGroup"] == '1' || $model->Properties["useRankCourseGroupVersion2"] == '1' || $model->Properties["useRecordRankDetail"] == '1') {
                $query .= "        DECIMAL(ROUND(AVG(T1.COURSE_GROUP_DEVIATION)*10,0)/10,5,1) AS COURSE_GROUP_DEVIATION, ";
            }
            //A:クラス別コースグループ
            if ($model->Properties["useRecordRankDetail"] == '1') {
                $query .= "        DECIMAL(ROUND(AVG(T1.COURSE_GROUP_CL_DEVIATION)*10,0)/10,5,1) AS COURSE_GROUP_CL_DEVIATION, ";
            }
            //6:講座グループ
            if ($model->Properties["useRankChairGroup"] == '1') {
                $query .= "        DECIMAL(ROUND(AVG(T1.CHAIR_GROUP_DEVIATION) *10,0)/10,5,1) AS CHAIR_GROUP_DEVIATION, ";
            }
            //DIV = B,C,D,E,F
            if ($model->Properties["useRecordRankDetail"] == '1') {
                $query .= "        DECIMAL(ROUND(AVG(T1.B_DEVIATION)*10,0)/10,5,1) AS B_DEVIATION, ";
                $query .= "        DECIMAL(ROUND(AVG(T1.C_DEVIATION)*10,0)/10,5,1) AS C_DEVIATION, ";
                $query .= "        DECIMAL(ROUND(AVG(T1.D_DEVIATION)*10,0)/10,5,1) AS D_DEVIATION, ";
                $query .= "        DECIMAL(ROUND(AVG(T1.E_DEVIATION)*10,0)/10,5,1) AS E_DEVIATION, ";
                $query .= "        DECIMAL(ROUND(AVG(T1.F_DEVIATION)*10,0)/10,5,1) AS F_DEVIATION, ";
            }
            //DIV = G
            if ($model->Properties["useRecordRankDetail"] == '1' && $model->Properties["knjd141c_deviationTotalRank"] == '1') {
                $query .= "        SUM(T1.GRADE_DEVIATION) AS GRADE_DEVIATION_TOTAL, ";
            }
            $query .= "        DECIMAL(ROUND(AVG(T1.GRADE_DEVIATION) *10,0)/10,5,1) AS GRADE_DEVIATION, ";
            $query .= "        DECIMAL(ROUND(AVG(T1.CLASS_DEVIATION) *10,0)/10,5,1) AS CLASS_DEVIATION, ";
            $query .= "        DECIMAL(ROUND(AVG(T1.COURSE_DEVIATION)*10,0)/10,5,1) AS COURSE_DEVIATION, ";
            $query .= "        DECIMAL(ROUND(AVG(T1.MAJOR_DEVIATION) *10,0)/10,5,1) AS MAJOR_DEVIATION ";
            $query .= "    FROM ";
            $query .= "        T_DEVIATION_SUB T1 ";
            $query .= "    WHERE ";
            $query .= "        EXISTS( ";
            $query .= "            SELECT ";
            $query .= "                'X' ";
            $query .= "            FROM ";
            $query .= "                T_GROUP W1 ";
            $query .= "            WHERE ";
            $query .= "                W1.GROUP_DIV = '5' AND ";
            $query .= "                W1.SCHREGNO = T1.SCHREGNO AND ";
            //教育課程対応
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "            W1.CLASSCD = T1.CLASSCD AND ";
                $query .= "            W1.SCHOOL_KIND = T1.SCHOOL_KIND AND ";
                $query .= "            W1.CURRICULUM_CD = T1.CURRICULUM_CD AND ";
            }
            $query .= "                W1.SUBCLASSCD = T1.SUBCLASSCD ";
            $query .= "            ) ";
            //洛南、土佐塾学年評価は合算科目の母集団から合併先科目を除く
            if ($model->schoolName == "rakunan" || $model->schoolName == "tosa" && $model->seme == '9' && $model->kind == '99' && $model->item == '00' && $model->sdiv == '08') {
                $query .= "    AND NOT EXISTS (SELECT 'X' FROM SUBCLASS_REPLACE_COMBINED_DAT W0 ";
                $query .= "           WHERE W0.YEAR = '".CTRL_YEAR."' ";
                //教育課程対応
                if ($model->Properties["useCurriculumcd"] == '1') {
                    $query .= "         AND W0.COMBINED_CLASSCD = T1.CLASSCD ";
                    $query .= "         AND W0.COMBINED_SCHOOL_KIND = T1.SCHOOL_KIND ";
                    $query .= "         AND W0.COMBINED_CURRICULUM_CD = T1.CURRICULUM_CD ";
                }
                $query .= "             AND W0.COMBINED_SUBCLASSCD = T1.SUBCLASSCD) ";
            }
            //宮城県:合算科目の母集団から除く 1:合併元除く 2:合併先除く
            if ($model->d070Namespare1 == "1") {
                $query .= "    AND NOT EXISTS (SELECT 'X' FROM SUBCLASS_REPLACE_COMBINED_DAT W0 ";
                $query .= "           WHERE W0.YEAR = '".CTRL_YEAR."' ";
                //教育課程対応
                if ($model->Properties["useCurriculumcd"] == '1') {
                    $query .= "         AND W0.ATTEND_CLASSCD = T1.CLASSCD ";
                    $query .= "         AND W0.ATTEND_SCHOOL_KIND = T1.SCHOOL_KIND ";
                    $query .= "         AND W0.ATTEND_CURRICULUM_CD = T1.CURRICULUM_CD ";
                }
                $query .= "             AND W0.ATTEND_SUBCLASSCD = T1.SUBCLASSCD) ";
            }
            if ($model->d070Namespare1 == "2") {
                $query .= "    AND NOT EXISTS (SELECT 'X' FROM SUBCLASS_REPLACE_COMBINED_DAT W0 ";
                $query .= "           WHERE W0.YEAR = '".CTRL_YEAR."' ";
                //教育課程対応
                if ($model->Properties["useCurriculumcd"] == '1') {
                    $query .= "         AND W0.COMBINED_CLASSCD = T1.CLASSCD ";
                    $query .= "         AND W0.COMBINED_SCHOOL_KIND = T1.SCHOOL_KIND ";
                    $query .= "         AND W0.COMBINED_CURRICULUM_CD = T1.CURRICULUM_CD ";
                }
                $query .= "             AND W0.COMBINED_SUBCLASSCD = T1.SUBCLASSCD) ";
            }
            $query .= "    GROUP BY T1.SCHREGNO ";
            $query .= "    UNION ALL ";
            $query .= "    SELECT  ";
            //教育課程対応・・・TODO：とりあえずこうしておく
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "    '99' as CLASSCD, ";
                $query .= "    max(T1.SCHOOL_KIND) as SCHOOL_KIND, ";
                $query .= "    '99' as CURRICULUM_CD, ";
            }
            $query .= "        '999999' as SUBCLASSCD, ";
            $query .= "        T1.SCHREGNO, ";
            //5:コースグループ
            if ($model->Properties["useRankCourseGroup"] == '1' || $model->Properties["useRankCourseGroupVersion2"] == '1' || $model->Properties["useRecordRankDetail"] == '1') {
                $query .= "        DECIMAL(ROUND(AVG(T1.COURSE_GROUP_DEVIATION)*10,0)/10,5,1) AS COURSE_GROUP_DEVIATION, ";
            }
            //A:クラス別コースグループ
            if ($model->Properties["useRecordRankDetail"] == '1') {
                $query .= "        DECIMAL(ROUND(AVG(T1.COURSE_GROUP_CL_DEVIATION)*10,0)/10,5,1) AS COURSE_GROUP_CL_DEVIATION, ";
            }
            //6:講座グループ
            if ($model->Properties["useRankChairGroup"] == '1') {
                $query .= "        DECIMAL(ROUND(AVG(T1.CHAIR_GROUP_DEVIATION) *10,0)/10,5,1) AS CHAIR_GROUP_DEVIATION, ";
            }
            //DIV = B,C,D,E,F
            if ($model->Properties["useRecordRankDetail"] == '1') {
                $query .= "        DECIMAL(ROUND(AVG(T1.B_DEVIATION)*10,0)/10,5,1) AS B_DEVIATION, ";
                $query .= "        DECIMAL(ROUND(AVG(T1.C_DEVIATION)*10,0)/10,5,1) AS C_DEVIATION, ";
                $query .= "        DECIMAL(ROUND(AVG(T1.D_DEVIATION)*10,0)/10,5,1) AS D_DEVIATION, ";
                $query .= "        DECIMAL(ROUND(AVG(T1.E_DEVIATION)*10,0)/10,5,1) AS E_DEVIATION, ";
                $query .= "        DECIMAL(ROUND(AVG(T1.F_DEVIATION)*10,0)/10,5,1) AS F_DEVIATION, ";
            }
            //DIV = G
            if ($model->Properties["useRecordRankDetail"] == '1' && $model->Properties["knjd141c_deviationTotalRank"] == '1') {
                $query .= "        SUM(T1.GRADE_DEVIATION) AS GRADE_DEVIATION_TOTAL, ";
            }
            $query .= "        DECIMAL(ROUND(AVG(T1.GRADE_DEVIATION) *10,0)/10,5,1) AS GRADE_DEVIATION, ";
            $query .= "        DECIMAL(ROUND(AVG(T1.CLASS_DEVIATION) *10,0)/10,5,1) AS CLASS_DEVIATION, ";
            $query .= "        DECIMAL(ROUND(AVG(T1.COURSE_DEVIATION)*10,0)/10,5,1) AS COURSE_DEVIATION, ";
            $query .= "        DECIMAL(ROUND(AVG(T1.MAJOR_DEVIATION) *10,0)/10,5,1) AS MAJOR_DEVIATION ";
            $query .= "    FROM ";
            $query .= "        T_DEVIATION_SUB T1 ";
            $query .= "    WHERE ";
            //教科科目名称マスタのマスタ化
            if ($model->Properties["useClassDetailDat"] == '1') {
                $query .= "     T1.CLASSCD || T1.SCHOOL_KIND || T1.CURRICULUM_CD || T1.SUBCLASSCD not in (SELECT W2.CLASSCD || W2.SCHOOL_KIND || W2.CURRICULUM_CD || W2.SUBCLASSCD FROM SUBCLASS_DETAIL_DAT W2 WHERE W2.YEAR = '".CTRL_YEAR."' AND W2.SUBCLASS_SEQ = '004') ";
            } else {
                //名称マスタ「D017」NAME1に登録されている科目を除く。
                $query .= "     T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD not in (SELECT W2.NAME1 FROM V_NAME_MST W2 WHERE W2.YEAR = '".CTRL_YEAR."' AND W2.NAMECD1 = 'D017') ";
            }
            //名称マスタ「D065」NAME1に登録されている科目を除く。
            $query .= " AND T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD not in (SELECT W2.NAME1 FROM V_NAME_MST W2 WHERE W2.YEAR = '".CTRL_YEAR."' AND W2.NAMECD1 = 'D065') ";
            //選択科目を除く
            if ($model->electdiv == "1") {
                $query .= " AND NOT EXISTS( ";
                $query .= "        SELECT ";
                $query .= "            'X' ";
                $query .= "        FROM ";
                $query .= "            SUBCLASS_MST W1 ";
                $query .= "        WHERE ";
                $query .= "            W1.ELECTDIV = '1' AND ";
                //教育課程対応
                if ($model->Properties["useCurriculumcd"] == '1') {
                    $query .= "        W1.CLASSCD = T1.CLASSCD AND ";
                    $query .= "        W1.SCHOOL_KIND = T1.SCHOOL_KIND AND ";
                    $query .= "        W1.CURRICULUM_CD = T1.CURRICULUM_CD AND ";
                }
                $query .= "            W1.SUBCLASSCD = T1.SUBCLASSCD ";
                $query .= "        ) ";
            }
            //洛南、土佐塾学年評価は合算科目の母集団から合併先科目を除く
            if ($model->schoolName == "rakunan" || $model->schoolName == "tosa" && $model->seme == '9' && $model->kind == '99' && $model->item == '00' && $model->sdiv == '08') {
                $query .= "    AND NOT EXISTS (SELECT 'X' FROM SUBCLASS_REPLACE_COMBINED_DAT W0 ";
                $query .= "           WHERE W0.YEAR = '".CTRL_YEAR."' ";
                //教育課程対応
                if ($model->Properties["useCurriculumcd"] == '1') {
                    $query .= "         AND W0.COMBINED_CLASSCD = T1.CLASSCD ";
                    $query .= "         AND W0.COMBINED_SCHOOL_KIND = T1.SCHOOL_KIND ";
                    $query .= "         AND W0.COMBINED_CURRICULUM_CD = T1.CURRICULUM_CD ";
                }
                $query .= "             AND W0.COMBINED_SUBCLASSCD = T1.SUBCLASSCD) ";
            }
            //宮城県:合算科目の母集団から除く 1:合併元除く 2:合併先除く
            if ($model->d070Namespare1 == "1") {
                $query .= "    AND NOT EXISTS (SELECT 'X' FROM SUBCLASS_REPLACE_COMBINED_DAT W0 ";
                $query .= "           WHERE W0.YEAR = '".CTRL_YEAR."' ";
                //教育課程対応
                if ($model->Properties["useCurriculumcd"] == '1') {
                    $query .= "         AND W0.ATTEND_CLASSCD = T1.CLASSCD ";
                    $query .= "         AND W0.ATTEND_SCHOOL_KIND = T1.SCHOOL_KIND ";
                    $query .= "         AND W0.ATTEND_CURRICULUM_CD = T1.CURRICULUM_CD ";
                }
                $query .= "             AND W0.ATTEND_SUBCLASSCD = T1.SUBCLASSCD) ";
            }
            if ($model->d070Namespare1 == "2") {
                $query .= "    AND NOT EXISTS (SELECT 'X' FROM SUBCLASS_REPLACE_COMBINED_DAT W0 ";
                $query .= "           WHERE W0.YEAR = '".CTRL_YEAR."' ";
                //教育課程対応
                if ($model->Properties["useCurriculumcd"] == '1') {
                    $query .= "         AND W0.COMBINED_CLASSCD = T1.CLASSCD ";
                    $query .= "         AND W0.COMBINED_SCHOOL_KIND = T1.SCHOOL_KIND ";
                    $query .= "         AND W0.COMBINED_CURRICULUM_CD = T1.CURRICULUM_CD ";
                }
                $query .= "             AND W0.COMBINED_SUBCLASSCD = T1.SUBCLASSCD) ";
            }
            $query .= "    GROUP BY T1.SCHREGNO ";

            $query .= "    UNION ALL ";
            $query .= "    SELECT  ";
            //教育課程対応・・・TODO：とりあえずこうしておく
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "    '99' as CLASSCD, ";
                $query .= "    max(T1.SCHOOL_KIND) as SCHOOL_KIND, ";
                $query .= "    '99' as CURRICULUM_CD, ";
            }
            $query .= "        '99999A' as SUBCLASSCD, ";
            $query .= "        T1.SCHREGNO, ";
            //5:コースグループ
            if ($model->Properties["useRankCourseGroup"] == '1' || $model->Properties["useRankCourseGroupVersion2"] == '1' || $model->Properties["useRecordRankDetail"] == '1') {
                $query .= "        DECIMAL(ROUND(AVG(T1.COURSE_GROUP_DEVIATION)*10,0)/10,5,1) AS COURSE_GROUP_DEVIATION, ";
            }
            //A:クラス別コースグループ
            if ($model->Properties["useRecordRankDetail"] == '1') {
                $query .= "        DECIMAL(ROUND(AVG(T1.COURSE_GROUP_CL_DEVIATION)*10,0)/10,5,1) AS COURSE_GROUP_CL_DEVIATION, ";
            }
            //6:講座グループ
            if ($model->Properties["useRankChairGroup"] == '1') {
                $query .= "        DECIMAL(ROUND(AVG(T1.CHAIR_GROUP_DEVIATION) *10,0)/10,5,1) AS CHAIR_GROUP_DEVIATION, ";
            }
            //DIV = B,C,D,E,F
            if ($model->Properties["useRecordRankDetail"] == '1') {
                $query .= "        DECIMAL(ROUND(AVG(T1.B_DEVIATION)*10,0)/10,5,1) AS B_DEVIATION, ";
                $query .= "        DECIMAL(ROUND(AVG(T1.C_DEVIATION)*10,0)/10,5,1) AS C_DEVIATION, ";
                $query .= "        DECIMAL(ROUND(AVG(T1.D_DEVIATION)*10,0)/10,5,1) AS D_DEVIATION, ";
                $query .= "        DECIMAL(ROUND(AVG(T1.E_DEVIATION)*10,0)/10,5,1) AS E_DEVIATION, ";
                $query .= "        DECIMAL(ROUND(AVG(T1.F_DEVIATION)*10,0)/10,5,1) AS F_DEVIATION, ";
            }
            //DIV = G
            if ($model->Properties["useRecordRankDetail"] == '1' && $model->Properties["knjd141c_deviationTotalRank"] == '1') {
                $query .= "        SUM(T1.GRADE_DEVIATION) AS GRADE_DEVIATION_TOTAL, ";
            }
            $query .= "        DECIMAL(ROUND(AVG(T1.GRADE_DEVIATION) *10,0)/10,5,1) AS GRADE_DEVIATION, ";
            $query .= "        DECIMAL(ROUND(AVG(T1.CLASS_DEVIATION) *10,0)/10,5,1) AS CLASS_DEVIATION, ";
            $query .= "        DECIMAL(ROUND(AVG(T1.COURSE_DEVIATION)*10,0)/10,5,1) AS COURSE_DEVIATION, ";
            $query .= "        DECIMAL(ROUND(AVG(T1.MAJOR_DEVIATION) *10,0)/10,5,1) AS MAJOR_DEVIATION ";
            $query .= "    FROM ";
            $query .= "        T_DEVIATION_SUB T1 ";
            $query .= "    WHERE ";
            //99999A：前期のみ科目
            $query .= "     T1.CLASSCD || T1.SCHOOL_KIND || T1.CURRICULUM_CD || T1.SUBCLASSCD in (SELECT W2.CLASSCD || W2.SCHOOL_KIND || W2.CURRICULUM_CD || W2.SUBCLASSCD FROM SUBCLASS_DETAIL_DAT W2 WHERE W2.YEAR = '".CTRL_YEAR."' AND W2.SUBCLASS_SEQ = '012' AND SUBCLASS_REMARK1 = '1') ";
            //選択科目を除く
            if ($model->electdiv == "1") {
                $query .= " AND NOT EXISTS( ";
                $query .= "        SELECT ";
                $query .= "            'X' ";
                $query .= "        FROM ";
                $query .= "            SUBCLASS_MST W1 ";
                $query .= "        WHERE ";
                $query .= "            W1.ELECTDIV = '1' AND ";
                //教育課程対応
                if ($model->Properties["useCurriculumcd"] == '1') {
                    $query .= "        W1.CLASSCD = T1.CLASSCD AND ";
                    $query .= "        W1.SCHOOL_KIND = T1.SCHOOL_KIND AND ";
                    $query .= "        W1.CURRICULUM_CD = T1.CURRICULUM_CD AND ";
                }
                $query .= "            W1.SUBCLASSCD = T1.SUBCLASSCD ";
                $query .= "        ) ";
            }
            //教科科目名称マスタのマスタ化
            if ($model->Properties["useClassDetailDat"] == '1') {
                $query .= "     AND T1.CLASSCD || T1.SCHOOL_KIND || T1.CURRICULUM_CD || T1.SUBCLASSCD not in (SELECT W2.CLASSCD || W2.SCHOOL_KIND || W2.CURRICULUM_CD || W2.SUBCLASSCD FROM SUBCLASS_DETAIL_DAT W2 WHERE W2.YEAR = '".CTRL_YEAR."' AND W2.SUBCLASS_SEQ = '004') ";
            } else {
                //名称マスタ「D017」NAME1に登録されている科目を除く。
                $query .= "     AND T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD not in (SELECT W2.NAME1 FROM V_NAME_MST W2 WHERE W2.YEAR = '".CTRL_YEAR."' AND W2.NAMECD1 = 'D017') ";
            }
            //名称マスタ「D065」NAME1に登録されている科目を除く。
            $query .= " AND T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD not in (SELECT W2.NAME1 FROM V_NAME_MST W2 WHERE W2.YEAR = '".CTRL_YEAR."' AND W2.NAMECD1 = 'D065') ";
            //洛南、土佐塾学年評価は合算科目の母集団から合併先科目を除く
            if ($model->schoolName == "rakunan" || $model->schoolName == "tosa" && $model->seme == '9' && $model->kind == '99' && $model->item == '00' && $model->sdiv == '08') {
                $query .= "    AND NOT EXISTS (SELECT 'X' FROM SUBCLASS_REPLACE_COMBINED_DAT W0 ";
                $query .= "           WHERE W0.YEAR = '".CTRL_YEAR."' ";
                //教育課程対応
                if ($model->Properties["useCurriculumcd"] == '1') {
                    $query .= "         AND W0.COMBINED_CLASSCD = T1.CLASSCD ";
                    $query .= "         AND W0.COMBINED_SCHOOL_KIND = T1.SCHOOL_KIND ";
                    $query .= "         AND W0.COMBINED_CURRICULUM_CD = T1.CURRICULUM_CD ";
                }
                $query .= "             AND W0.COMBINED_SUBCLASSCD = T1.SUBCLASSCD) ";
            }
            //宮城県:合算科目の母集団から除く 1:合併元除く 2:合併先除く
            if ($model->d070Namespare1 == "1") {
                $query .= "    AND NOT EXISTS (SELECT 'X' FROM SUBCLASS_REPLACE_COMBINED_DAT W0 ";
                $query .= "           WHERE W0.YEAR = '".CTRL_YEAR."' ";
                //教育課程対応
                if ($model->Properties["useCurriculumcd"] == '1') {
                    $query .= "         AND W0.ATTEND_CLASSCD = T1.CLASSCD ";
                    $query .= "         AND W0.ATTEND_SCHOOL_KIND = T1.SCHOOL_KIND ";
                    $query .= "         AND W0.ATTEND_CURRICULUM_CD = T1.CURRICULUM_CD ";
                }
                $query .= "             AND W0.ATTEND_SUBCLASSCD = T1.SUBCLASSCD) ";
            }
            if ($model->d070Namespare1 == "2") {
                $query .= "    AND NOT EXISTS (SELECT 'X' FROM SUBCLASS_REPLACE_COMBINED_DAT W0 ";
                $query .= "           WHERE W0.YEAR = '".CTRL_YEAR."' ";
                //教育課程対応
                if ($model->Properties["useCurriculumcd"] == '1') {
                    $query .= "         AND W0.COMBINED_CLASSCD = T1.CLASSCD ";
                    $query .= "         AND W0.COMBINED_SCHOOL_KIND = T1.SCHOOL_KIND ";
                    $query .= "         AND W0.COMBINED_CURRICULUM_CD = T1.CURRICULUM_CD ";
                }
                $query .= "             AND W0.COMBINED_SUBCLASSCD = T1.SUBCLASSCD) ";
            }
            $query .= "    GROUP BY T1.SCHREGNO ";

            $query .= "    UNION ALL ";
            $query .= "    SELECT  ";
            //教育課程対応・・・TODO：とりあえずこうしておく
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "    '99' as CLASSCD, ";
                $query .= "    max(T1.SCHOOL_KIND) as SCHOOL_KIND, ";
                $query .= "    '99' as CURRICULUM_CD, ";
            }
            $query .= "        '99999B' as SUBCLASSCD, ";
            $query .= "        T1.SCHREGNO, ";
            //5:コースグループ
            if ($model->Properties["useRankCourseGroup"] == '1' || $model->Properties["useRankCourseGroupVersion2"] == '1' || $model->Properties["useRecordRankDetail"] == '1') {
                $query .= "        DECIMAL(ROUND(AVG(T1.COURSE_GROUP_DEVIATION)*10,0)/10,5,1) AS COURSE_GROUP_DEVIATION, ";
            }
            //A:クラス別コースグループ
            if ($model->Properties["useRecordRankDetail"] == '1') {
                $query .= "        DECIMAL(ROUND(AVG(T1.COURSE_GROUP_CL_DEVIATION)*10,0)/10,5,1) AS COURSE_GROUP_CL_DEVIATION, ";
            }
            //6:講座グループ
            if ($model->Properties["useRankChairGroup"] == '1') {
                $query .= "        DECIMAL(ROUND(AVG(T1.CHAIR_GROUP_DEVIATION) *10,0)/10,5,1) AS CHAIR_GROUP_DEVIATION, ";
            }
            //DIV = B,C,D,E,F
            if ($model->Properties["useRecordRankDetail"] == '1') {
                $query .= "        DECIMAL(ROUND(AVG(T1.B_DEVIATION)*10,0)/10,5,1) AS B_DEVIATION, ";
                $query .= "        DECIMAL(ROUND(AVG(T1.C_DEVIATION)*10,0)/10,5,1) AS C_DEVIATION, ";
                $query .= "        DECIMAL(ROUND(AVG(T1.D_DEVIATION)*10,0)/10,5,1) AS D_DEVIATION, ";
                $query .= "        DECIMAL(ROUND(AVG(T1.E_DEVIATION)*10,0)/10,5,1) AS E_DEVIATION, ";
                $query .= "        DECIMAL(ROUND(AVG(T1.F_DEVIATION)*10,0)/10,5,1) AS F_DEVIATION, ";
            }
            //DIV = G
            if ($model->Properties["useRecordRankDetail"] == '1' && $model->Properties["knjd141c_deviationTotalRank"] == '1') {
                $query .= "        SUM(T1.GRADE_DEVIATION) AS GRADE_DEVIATION_TOTAL, ";
            }
            $query .= "        DECIMAL(ROUND(AVG(T1.GRADE_DEVIATION) *10,0)/10,5,1) AS GRADE_DEVIATION, ";
            $query .= "        DECIMAL(ROUND(AVG(T1.CLASS_DEVIATION) *10,0)/10,5,1) AS CLASS_DEVIATION, ";
            $query .= "        DECIMAL(ROUND(AVG(T1.COURSE_DEVIATION)*10,0)/10,5,1) AS COURSE_DEVIATION, ";
            $query .= "        DECIMAL(ROUND(AVG(T1.MAJOR_DEVIATION) *10,0)/10,5,1) AS MAJOR_DEVIATION ";
            $query .= "    FROM ";
            $query .= "        T_DEVIATION_SUB T1 ";
            $query .= "    WHERE ";
            //99999B：後期のみ科目　＋　通年科目　（つまり、前期のみ科目を除く）
            $query .= "     T1.CLASSCD || T1.SCHOOL_KIND || T1.CURRICULUM_CD || T1.SUBCLASSCD not in (SELECT W2.CLASSCD || W2.SCHOOL_KIND || W2.CURRICULUM_CD || W2.SUBCLASSCD FROM SUBCLASS_DETAIL_DAT W2 WHERE W2.YEAR = '".CTRL_YEAR."' AND W2.SUBCLASS_SEQ = '012' AND SUBCLASS_REMARK1 = '1') ";
            //選択科目を除く
            if ($model->electdiv == "1") {
                $query .= " AND NOT EXISTS( ";
                $query .= "        SELECT ";
                $query .= "            'X' ";
                $query .= "        FROM ";
                $query .= "            SUBCLASS_MST W1 ";
                $query .= "        WHERE ";
                $query .= "            W1.ELECTDIV = '1' AND ";
                //教育課程対応
                if ($model->Properties["useCurriculumcd"] == '1') {
                    $query .= "        W1.CLASSCD = T1.CLASSCD AND ";
                    $query .= "        W1.SCHOOL_KIND = T1.SCHOOL_KIND AND ";
                    $query .= "        W1.CURRICULUM_CD = T1.CURRICULUM_CD AND ";
                }
                $query .= "            W1.SUBCLASSCD = T1.SUBCLASSCD ";
                $query .= "        ) ";
            }
            //教科科目名称マスタのマスタ化
            if ($model->Properties["useClassDetailDat"] == '1') {
                $query .= "     AND T1.CLASSCD || T1.SCHOOL_KIND || T1.CURRICULUM_CD || T1.SUBCLASSCD not in (SELECT W2.CLASSCD || W2.SCHOOL_KIND || W2.CURRICULUM_CD || W2.SUBCLASSCD FROM SUBCLASS_DETAIL_DAT W2 WHERE W2.YEAR = '".CTRL_YEAR."' AND W2.SUBCLASS_SEQ = '004') ";
            } else {
                //名称マスタ「D017」NAME1に登録されている科目を除く。
                $query .= "     AND T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD not in (SELECT W2.NAME1 FROM V_NAME_MST W2 WHERE W2.YEAR = '".CTRL_YEAR."' AND W2.NAMECD1 = 'D017') ";
            }
            //名称マスタ「D065」NAME1に登録されている科目を除く。
            $query .= " AND T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD not in (SELECT W2.NAME1 FROM V_NAME_MST W2 WHERE W2.YEAR = '".CTRL_YEAR."' AND W2.NAMECD1 = 'D065') ";
            //洛南、土佐塾学年評価は合算科目の母集団から合併先科目を除く
            if ($model->schoolName == "rakunan" || $model->schoolName == "tosa" && $model->seme == '9' && $model->kind == '99' && $model->item == '00' && $model->sdiv == '08') {
                $query .= "    AND NOT EXISTS (SELECT 'X' FROM SUBCLASS_REPLACE_COMBINED_DAT W0 ";
                $query .= "           WHERE W0.YEAR = '".CTRL_YEAR."' ";
                //教育課程対応
                if ($model->Properties["useCurriculumcd"] == '1') {
                    $query .= "         AND W0.COMBINED_CLASSCD = T1.CLASSCD ";
                    $query .= "         AND W0.COMBINED_SCHOOL_KIND = T1.SCHOOL_KIND ";
                    $query .= "         AND W0.COMBINED_CURRICULUM_CD = T1.CURRICULUM_CD ";
                }
                $query .= "             AND W0.COMBINED_SUBCLASSCD = T1.SUBCLASSCD) ";
            }
            //宮城県:合算科目の母集団から除くフラグ 1:合併元除く 2:合併先除く
            if ($model->d070Namespare1 == "1") {
                $query .= "    AND NOT EXISTS (SELECT 'X' FROM SUBCLASS_REPLACE_COMBINED_DAT W0 ";
                $query .= "           WHERE W0.YEAR = '".CTRL_YEAR."' ";
                //教育課程対応
                if ($model->Properties["useCurriculumcd"] == '1') {
                    $query .= "         AND W0.ATTEND_CLASSCD = T1.CLASSCD ";
                    $query .= "         AND W0.ATTEND_SCHOOL_KIND = T1.SCHOOL_KIND ";
                    $query .= "         AND W0.ATTEND_CURRICULUM_CD = T1.CURRICULUM_CD ";
                }
                $query .= "             AND W0.ATTEND_SUBCLASSCD = T1.SUBCLASSCD) ";
            }
            if ($model->d070Namespare1 == "2") {
                $query .= "    AND NOT EXISTS (SELECT 'X' FROM SUBCLASS_REPLACE_COMBINED_DAT W0 ";
                $query .= "           WHERE W0.YEAR = '".CTRL_YEAR."' ";
                //教育課程対応
                if ($model->Properties["useCurriculumcd"] == '1') {
                    $query .= "         AND W0.COMBINED_CLASSCD = T1.CLASSCD ";
                    $query .= "         AND W0.COMBINED_SCHOOL_KIND = T1.SCHOOL_KIND ";
                    $query .= "         AND W0.COMBINED_CURRICULUM_CD = T1.CURRICULUM_CD ";
                }
                $query .= "             AND W0.COMBINED_SUBCLASSCD = T1.SUBCLASSCD) ";
            }
            $query .= "    GROUP BY T1.SCHREGNO ";
            $query .= "    ) ";
        }

        //各科目
        $query .= "SELECT ";
        $query .= "    {$fieldCrc}W1.SUBCLASSCD, ";
        $query .= $field_name;
        $query .= "    W1.SCHREGNO, ";
        $query .= "    W1.SCORE, ";
        $query .= "    W1.AVG, ";
        $query .= "    RANK() OVER(PARTITION BY {$fieldCrc}W1.SUBCLASSCD," .$field_name ."W1.GRADE ORDER BY W1.SCORE DESC) AS GRADE_RANK, ";
        $query .= "    RANK() OVER(PARTITION BY {$fieldCrc}W1.SUBCLASSCD," .$field_name ."W1.HR_CLASS ORDER BY W1.SCORE DESC) AS CLASS_RANK, ";
        $query .= "    RANK() OVER(PARTITION BY {$fieldCrc}W1.SUBCLASSCD," .$field_name ."W1.COURSE ORDER BY W1.SCORE DESC) AS COURSE_RANK, ";
        $query .= "    RANK() OVER(PARTITION BY {$fieldCrc}W1.SUBCLASSCD," .$field_name ."W1.MAJOR ORDER BY W1.SCORE DESC) AS MAJOR_RANK, ";
        $query .= "    RANK() OVER(PARTITION BY {$fieldCrc}W1.SUBCLASSCD," .$field_name ."W1.GRADE ORDER BY W1.SCORE DESC) AS GRADE_AVG_RANK, ";
        $query .= "    RANK() OVER(PARTITION BY {$fieldCrc}W1.SUBCLASSCD," .$field_name ."W1.HR_CLASS ORDER BY W1.SCORE DESC) AS CLASS_AVG_RANK, ";
        $query .= "    RANK() OVER(PARTITION BY {$fieldCrc}W1.SUBCLASSCD," .$field_name ."W1.COURSE ORDER BY W1.SCORE DESC) AS COURSE_AVG_RANK, ";
        $query .= "    RANK() OVER(PARTITION BY {$fieldCrc}W1.SUBCLASSCD," .$field_name ."W1.MAJOR ORDER BY W1.SCORE DESC) AS MAJOR_AVG_RANK, ";
        //5:コースグループ
        if ($model->Properties["useRankCourseGroup"] == '1' || $model->Properties["useRankCourseGroupVersion2"] == '1' || $model->Properties["useRecordRankDetail"] == '1') {
            $query .= "    RANK() OVER(PARTITION BY {$fieldCrc}W1.SUBCLASSCD," .$field_name ."W1.COURSE_GROUP ORDER BY W1.SCORE DESC) AS COURSE_GROUP_RANK, ";
            $query .= "    RANK() OVER(PARTITION BY {$fieldCrc}W1.SUBCLASSCD," .$field_name ."W1.COURSE_GROUP ORDER BY W1.SCORE DESC) AS COURSE_GROUP_AVG_RANK, ";
            $query .= "    DECIMAL(ROUND(((case when 0 < W6.STDDEV then 10*(W1.SCORE-W6.AVG)/W6.STDDEV else 0 end)+50)*10,0)/10,5,1) AS COURSE_GROUP_DEVIATION, ";
            $query .= "    RANK() OVER(PARTITION BY {$fieldCrc}W1.SUBCLASSCD," .$field_name ."W1.COURSE_GROUP ORDER BY DECIMAL(ROUND((case when 0 < W6.STDDEV then 10*(W1.SCORE-W6.AVG)/W6.STDDEV else 0 end +50)*10,0)/10,5,1) DESC) AS COURSE_GROUP_DEVIATION_RANK, ";
        }
        //A:クラス別コースグループ
        if ($model->Properties["useRecordRankDetail"] == '1') {
            $query .= "    RANK() OVER(PARTITION BY {$fieldCrc}W1.SUBCLASSCD," .$field_name ."W1.HR_CLASS,W1.COURSE_GROUP ORDER BY W1.SCORE DESC) AS COURSE_GROUP_CL_RANK, ";
            $query .= "    RANK() OVER(PARTITION BY {$fieldCrc}W1.SUBCLASSCD," .$field_name ."W1.HR_CLASS,W1.COURSE_GROUP ORDER BY W1.SCORE DESC) AS COURSE_GROUP_CL_AVG_RANK, ";
            $query .= "    DECIMAL(ROUND(((case when 0 < W8.STDDEV then 10*(W1.SCORE-W8.AVG)/W8.STDDEV else 0 end)+50)*10,0)/10,5,1) AS COURSE_GROUP_CL_DEVIATION, ";
            $query .= "    RANK() OVER(PARTITION BY {$fieldCrc}W1.SUBCLASSCD," .$field_name ."W1.HR_CLASS,W1.COURSE_GROUP ORDER BY DECIMAL(ROUND((case when 0 < W8.STDDEV then 10*(W1.SCORE-W8.AVG)/W8.STDDEV else 0 end +50)*10,0)/10,5,1) DESC) AS COURSE_GROUP_CL_DEVIATION_RANK, ";
        }
        //6:講座グループ
        if ($model->Properties["useRankChairGroup"] == '1') {
            $query .= "    RANK() OVER(PARTITION BY {$fieldCrc}W1.SUBCLASSCD," .$field_name ."W1.CHAIR_GROUP_CD ORDER BY W1.SCORE DESC) AS CHAIR_GROUP_RANK, ";
            $query .= "    RANK() OVER(PARTITION BY {$fieldCrc}W1.SUBCLASSCD," .$field_name ."W1.CHAIR_GROUP_CD ORDER BY W1.SCORE DESC) AS CHAIR_GROUP_AVG_RANK, ";
            $query .= "    DECIMAL(ROUND(((case when 0 < W7.STDDEV then 10*(W1.SCORE-W7.AVG)/W7.STDDEV else 0 end)+50)*10,0)/10,5,1) AS CHAIR_GROUP_DEVIATION, ";
        }
        //DIV = B,C,D,E,F
        if ($model->Properties["useRecordRankDetail"] == '1') {
            //B:同クラス・同コース生徒内での順位
            $query .= "    RANK() OVER(PARTITION BY {$fieldCrc}W1.SUBCLASSCD," .$field_name ."W1.HR_CLASS,W1.COURSE ORDER BY W1.SCORE DESC) AS B_RANK, ";
            $query .= "    RANK() OVER(PARTITION BY {$fieldCrc}W1.SUBCLASSCD," .$field_name ."W1.HR_CLASS,W1.COURSE ORDER BY W1.SCORE DESC) AS B_AVG_RANK, ";
            $query .= "    DECIMAL(ROUND(((case when 0 < WB.STDDEV then 10*(W1.SCORE-WB.AVG)/WB.STDDEV else 0 end)+50)*10,0)/10,5,1) AS B_DEVIATION, ";
            $query .= "    RANK() OVER(PARTITION BY {$fieldCrc}W1.SUBCLASSCD," .$field_name ."W1.HR_CLASS,W1.COURSE ORDER BY DECIMAL(ROUND((case when 0 < WB.STDDEV then 10*(W1.SCORE-WB.AVG)/WB.STDDEV else 0 end +50)*10,0)/10,5,1) DESC) AS B_DEVIATION_RANK, ";
            //C:同クラス・同コース・同高入生徒内での順位
            $query .= "    CASE WHEN W1.ENT_JH = 'H' THEN RANK() OVER(PARTITION BY {$fieldCrc}W1.SUBCLASSCD," .$field_name ."W1.HR_CLASS,W1.COURSE,W1.ENT_JH ORDER BY W1.SCORE DESC) ELSE NULL END AS C_RANK, ";
            $query .= "    CASE WHEN W1.ENT_JH = 'H' THEN RANK() OVER(PARTITION BY {$fieldCrc}W1.SUBCLASSCD," .$field_name ."W1.HR_CLASS,W1.COURSE,W1.ENT_JH ORDER BY W1.SCORE DESC) ELSE NULL END AS C_AVG_RANK, ";
            $query .= "    CASE WHEN W1.ENT_JH = 'H' THEN DECIMAL(ROUND(((case when 0 < WC.STDDEV then 10*(W1.SCORE-WC.AVG)/WC.STDDEV else 0 end)+50)*10,0)/10,5,1) ELSE NULL END AS C_DEVIATION, ";
            $query .= "    CASE WHEN W1.ENT_JH = 'H' THEN RANK() OVER(PARTITION BY {$fieldCrc}W1.SUBCLASSCD," .$field_name ."W1.HR_CLASS,W1.COURSE,W1.ENT_JH ORDER BY DECIMAL(ROUND((case when 0 < WC.STDDEV then 10*(W1.SCORE-WC.AVG)/WC.STDDEV else 0 end +50)*10,0)/10,5,1) DESC) ELSE NULL END AS C_DEVIATION_RANK, ";
            //D:同クラス・同コース・同一貫生徒内での順位
            $query .= "    CASE WHEN W1.ENT_JH = 'J' THEN RANK() OVER(PARTITION BY {$fieldCrc}W1.SUBCLASSCD," .$field_name ."W1.HR_CLASS,W1.COURSE,W1.ENT_JH ORDER BY W1.SCORE DESC) ELSE NULL END AS D_RANK, ";
            $query .= "    CASE WHEN W1.ENT_JH = 'J' THEN RANK() OVER(PARTITION BY {$fieldCrc}W1.SUBCLASSCD," .$field_name ."W1.HR_CLASS,W1.COURSE,W1.ENT_JH ORDER BY W1.SCORE DESC) ELSE NULL END AS D_AVG_RANK, ";
            $query .= "    CASE WHEN W1.ENT_JH = 'J' THEN DECIMAL(ROUND(((case when 0 < WD.STDDEV then 10*(W1.SCORE-WD.AVG)/WD.STDDEV else 0 end)+50)*10,0)/10,5,1) ELSE NULL END AS D_DEVIATION, ";
            $query .= "    CASE WHEN W1.ENT_JH = 'J' THEN RANK() OVER(PARTITION BY {$fieldCrc}W1.SUBCLASSCD," .$field_name ."W1.HR_CLASS,W1.COURSE,W1.ENT_JH ORDER BY DECIMAL(ROUND((case when 0 < WD.STDDEV then 10*(W1.SCORE-WD.AVG)/WD.STDDEV else 0 end +50)*10,0)/10,5,1) DESC) ELSE NULL END AS D_DEVIATION_RANK, ";
            //E:同学年・同コース・同高入生徒内での順位
            $query .= "    CASE WHEN W1.ENT_JH = 'H' THEN RANK() OVER(PARTITION BY {$fieldCrc}W1.SUBCLASSCD," .$field_name ."W1.GRADE,W1.COURSE,W1.ENT_JH ORDER BY W1.SCORE DESC) ELSE NULL END AS E_RANK, ";
            $query .= "    CASE WHEN W1.ENT_JH = 'H' THEN RANK() OVER(PARTITION BY {$fieldCrc}W1.SUBCLASSCD," .$field_name ."W1.GRADE,W1.COURSE,W1.ENT_JH ORDER BY W1.SCORE DESC) ELSE NULL END AS E_AVG_RANK, ";
            $query .= "    CASE WHEN W1.ENT_JH = 'H' THEN DECIMAL(ROUND(((case when 0 < WE.STDDEV then 10*(W1.SCORE-WE.AVG)/WE.STDDEV else 0 end)+50)*10,0)/10,5,1) ELSE NULL END AS E_DEVIATION, ";
            $query .= "    CASE WHEN W1.ENT_JH = 'H' THEN RANK() OVER(PARTITION BY {$fieldCrc}W1.SUBCLASSCD," .$field_name ."W1.GRADE,W1.COURSE,W1.ENT_JH ORDER BY DECIMAL(ROUND((case when 0 < WE.STDDEV then 10*(W1.SCORE-WE.AVG)/WE.STDDEV else 0 end +50)*10,0)/10,5,1) DESC) ELSE NULL END AS E_DEVIATION_RANK, ";
            //F:同学年・同コース・同一貫生徒内での順位
            $query .= "    CASE WHEN W1.ENT_JH = 'J' THEN RANK() OVER(PARTITION BY {$fieldCrc}W1.SUBCLASSCD," .$field_name ."W1.GRADE,W1.COURSE,W1.ENT_JH ORDER BY W1.SCORE DESC) ELSE NULL END AS F_RANK, ";
            $query .= "    CASE WHEN W1.ENT_JH = 'J' THEN RANK() OVER(PARTITION BY {$fieldCrc}W1.SUBCLASSCD," .$field_name ."W1.GRADE,W1.COURSE,W1.ENT_JH ORDER BY W1.SCORE DESC) ELSE NULL END AS F_AVG_RANK, ";
            $query .= "    CASE WHEN W1.ENT_JH = 'J' THEN DECIMAL(ROUND(((case when 0 < WF.STDDEV then 10*(W1.SCORE-WF.AVG)/WF.STDDEV else 0 end)+50)*10,0)/10,5,1) ELSE NULL END AS F_DEVIATION, ";
            $query .= "    CASE WHEN W1.ENT_JH = 'J' THEN RANK() OVER(PARTITION BY {$fieldCrc}W1.SUBCLASSCD," .$field_name ."W1.GRADE,W1.COURSE,W1.ENT_JH ORDER BY DECIMAL(ROUND((case when 0 < WF.STDDEV then 10*(W1.SCORE-WF.AVG)/WF.STDDEV else 0 end +50)*10,0)/10,5,1) DESC) ELSE NULL END AS F_DEVIATION_RANK, ";
        }
        //DIV = G
        if ($model->Properties["useRecordRankDetail"] == '1' && $model->Properties["knjd141c_deviationTotalRank"] == '1') {
            //G:同学年生徒内での順位
            $query .= "    NULL AS GRADE_DEVIATION_TOTAL, ";
            $query .= "    NULL AS GRADE_DEVIATION_TOTAL_RANK, ";
        }
        $query .= "    DECIMAL(ROUND(((case when 0 < W2.STDDEV then 10*(W1.SCORE-W2.AVG)/W2.STDDEV else 0 end)+50)*10,0)/10,5,1) AS GRADE_DEVIATION, ";
        $query .= "    DECIMAL(ROUND(((case when 0 < W3.STDDEV then 10*(W1.SCORE-W3.AVG)/W3.STDDEV else 0 end)+50)*10,0)/10,5,1) AS CLASS_DEVIATION, ";
        $query .= "    DECIMAL(ROUND(((case when 0 < W4.STDDEV then 10*(W1.SCORE-W4.AVG)/W4.STDDEV else 0 end)+50)*10,0)/10,5,1) AS COURSE_DEVIATION, ";
        $query .= "    DECIMAL(ROUND(((case when 0 < W5.STDDEV then 10*(W1.SCORE-W5.AVG)/W5.STDDEV else 0 end)+50)*10,0)/10,5,1) AS MAJOR_DEVIATION, ";
        $query .= "    RANK() OVER(PARTITION BY {$fieldCrc}W1.SUBCLASSCD," .$field_name ."W1.GRADE    ORDER BY DECIMAL(ROUND((case when 0 < W2.STDDEV then 10*(W1.SCORE-W2.AVG)/W2.STDDEV else 0 end +50)*10,0)/10,5,1) DESC) AS GRADE_DEVIATION_RANK, ";
        $query .= "    RANK() OVER(PARTITION BY {$fieldCrc}W1.SUBCLASSCD," .$field_name ."W1.HR_CLASS ORDER BY DECIMAL(ROUND((case when 0 < W3.STDDEV then 10*(W1.SCORE-W3.AVG)/W3.STDDEV else 0 end +50)*10,0)/10,5,1) DESC) AS CLASS_DEVIATION_RANK, ";
        $query .= "    RANK() OVER(PARTITION BY {$fieldCrc}W1.SUBCLASSCD," .$field_name ."W1.COURSE   ORDER BY DECIMAL(ROUND((case when 0 < W4.STDDEV then 10*(W1.SCORE-W4.AVG)/W4.STDDEV else 0 end +50)*10,0)/10,5,1) DESC) AS COURSE_DEVIATION_RANK, ";
        $query .= "    RANK() OVER(PARTITION BY {$fieldCrc}W1.SUBCLASSCD," .$field_name ."W1.MAJOR    ORDER BY DECIMAL(ROUND((case when 0 < W5.STDDEV then 10*(W1.SCORE-W5.AVG)/W5.STDDEV else 0 end +50)*10,0)/10,5,1) DESC) AS MAJOR_DEVIATION_RANK ";

        $query .= "FROM ";
        $query .= "    T_RANK" .$table_name ." W1 ";
        $query .= "    LEFT JOIN T_AVERAGE W2 ON W2.AVG_DIV = '1' AND {$whereCrc2}W2.SUBCLASSCD = W1.SUBCLASSCD AND " .$where_name2 ."W2.GRADE = W1.GRADE ";
        $query .= "    LEFT JOIN T_AVERAGE W3 ON W3.AVG_DIV = '2' AND {$whereCrc3}W3.SUBCLASSCD = W1.SUBCLASSCD AND " .$where_name3 ."W3.GRADE = W1.GRADE AND W3.HR_CLASS = W1.HR_CLASS ";
        $query .= "    LEFT JOIN T_AVERAGE W4 ON W4.AVG_DIV = '3' AND {$whereCrc4}W4.SUBCLASSCD = W1.SUBCLASSCD AND " .$where_name4 ."W4.GRADE = W1.GRADE AND W4.COURSE = W1.COURSE ";
        $query .= "    LEFT JOIN T_AVERAGE W5 ON W5.AVG_DIV = '4' AND {$whereCrc5}W5.SUBCLASSCD = W1.SUBCLASSCD AND " .$where_name5 ."W5.GRADE = W1.GRADE AND W5.MAJOR = W1.MAJOR ";
        //5:コースグループ
        if ($model->Properties["useRankCourseGroup"] == '1' || $model->Properties["useRankCourseGroupVersion2"] == '1' || $model->Properties["useRecordRankDetail"] == '1') {
            $query .= "    LEFT JOIN T_AVERAGE W6 ON W6.AVG_DIV = '5' AND {$whereCrc6}W6.SUBCLASSCD = W1.SUBCLASSCD AND " .$where_name6 ."W6.GRADE = W1.GRADE AND W6.COURSE_GROUP = W1.COURSE_GROUP ";
        }
        //A:クラス別コースグループ
        if ($model->Properties["useRecordRankDetail"] == '1') {
            $query .= "    LEFT JOIN T_AVERAGE W8 ON W8.AVG_DIV = 'A' AND {$whereCrc8}W8.SUBCLASSCD = W1.SUBCLASSCD AND " .$where_name8 ."W8.GRADE = W1.GRADE AND W8.HR_CLASS = W1.HR_CLASS AND W8.COURSE_GROUP = W1.COURSE_GROUP ";
        }
        //6:講座グループ
        if ($model->Properties["useRankChairGroup"] == '1') {
            $query .= "    LEFT JOIN T_AVERAGE W7 ON W7.AVG_DIV = '6' AND {$whereCrc7}W7.SUBCLASSCD = W1.SUBCLASSCD AND " .$where_name7 ."W7.GRADE = W1.GRADE AND W7.CHAIR_GROUP_CD = W1.CHAIR_GROUP_CD ";
        }
        //DIV = B,C,D,E,F
        if ($model->Properties["useRecordRankDetail"] == '1') {
            $query .= "    LEFT JOIN T_AVERAGE WB ON WB.AVG_DIV = 'B' AND {$whereCrcB}WB.SUBCLASSCD = W1.SUBCLASSCD AND " .$where_nameB ."WB.GRADE = W1.GRADE AND WB.HR_CLASS = W1.HR_CLASS AND WB.COURSE = W1.COURSE ";
            $query .= "    LEFT JOIN T_AVERAGE WC ON WC.AVG_DIV = 'C' AND {$whereCrcC}WC.SUBCLASSCD = W1.SUBCLASSCD AND " .$where_nameC ."WC.GRADE = W1.GRADE AND WC.HR_CLASS = W1.HR_CLASS AND WC.COURSE = W1.COURSE ";
            $query .= "    LEFT JOIN T_AVERAGE WD ON WD.AVG_DIV = 'D' AND {$whereCrcD}WD.SUBCLASSCD = W1.SUBCLASSCD AND " .$where_nameD ."WD.GRADE = W1.GRADE AND WD.HR_CLASS = W1.HR_CLASS AND WD.COURSE = W1.COURSE ";
            $query .= "    LEFT JOIN T_AVERAGE WE ON WE.AVG_DIV = 'E' AND {$whereCrcE}WE.SUBCLASSCD = W1.SUBCLASSCD AND " .$where_nameE ."WE.GRADE = W1.GRADE AND WE.COURSE = W1.COURSE ";
            $query .= "    LEFT JOIN T_AVERAGE WF ON WF.AVG_DIV = 'F' AND {$whereCrcF}WF.SUBCLASSCD = W1.SUBCLASSCD AND " .$where_nameF ."WF.GRADE = W1.GRADE AND WF.COURSE = W1.COURSE ";
        }
        $query .= "WHERE ";
        $query .= "    W1.SUBCLASSCD not in ('333333','555555','999999','99999A','99999B') ";
        //処理科目(総合計のみ)
        if ($model->subclasscd == '999999') {
            $query .= "  and W1.SUBCLASSCD in ('333333','555555','999999','99999A','99999B') ";
        //処理科目(各科目のみ)
        } elseif ($model->subclasscd != '000000') {
            $query .= "  and W1.CLASSCD || '-' || W1.SCHOOL_KIND || '-' || W1.CURRICULUM_CD || '-' || W1.SUBCLASSCD = '".$model->subclasscd."' ";
        }
        if ($table == "record_rank") {
            //３科・５科・全科
            $query .= "UNION ALL ";
            $query .= "SELECT ";
            $query .= "    {$fieldCrc}W1.SUBCLASSCD, ";
            $query .= $field_name;
            $query .= "    W1.SCHREGNO, ";
            $query .= "    W1.SCORE, ";
            $query .= "    W1.AVG, ";
            $query .= "    RANK() OVER(PARTITION BY {$fieldCrc}W1.SUBCLASSCD," .$field_name ."W1.GRADE ORDER BY W1.SCORE DESC) AS GRADE_RANK, ";
            $query .= "    RANK() OVER(PARTITION BY {$fieldCrc}W1.SUBCLASSCD," .$field_name ."W1.HR_CLASS ORDER BY W1.SCORE DESC) AS CLASS_RANK, ";
            $query .= "    RANK() OVER(PARTITION BY {$fieldCrc}W1.SUBCLASSCD," .$field_name ."W1.COURSE ORDER BY W1.SCORE DESC) AS COURSE_RANK, ";
            $query .= "    RANK() OVER(PARTITION BY {$fieldCrc}W1.SUBCLASSCD," .$field_name ."W1.MAJOR ORDER BY W1.SCORE DESC) AS MAJOR_RANK, ";
            $query .= "    RANK() OVER(PARTITION BY {$fieldCrc}W1.SUBCLASSCD," .$field_name ."W1.GRADE ORDER BY W1.AVG DESC) AS GRADE_AVG_RANK, ";
            $query .= "    RANK() OVER(PARTITION BY {$fieldCrc}W1.SUBCLASSCD," .$field_name ."W1.HR_CLASS ORDER BY W1.AVG DESC) AS CLASS_AVG_RANK, ";
            $query .= "    RANK() OVER(PARTITION BY {$fieldCrc}W1.SUBCLASSCD," .$field_name ."W1.COURSE ORDER BY W1.AVG DESC) AS COURSE_AVG_RANK, ";
            $query .= "    RANK() OVER(PARTITION BY {$fieldCrc}W1.SUBCLASSCD," .$field_name ."W1.MAJOR ORDER BY W1.AVG DESC) AS MAJOR_AVG_RANK, ";
            //5:コースグループ
            if ($model->Properties["useRankCourseGroup"] == '1' || $model->Properties["useRankCourseGroupVersion2"] == '1' || $model->Properties["useRecordRankDetail"] == '1') {
                $query .= "    RANK() OVER(PARTITION BY {$fieldCrc}W1.SUBCLASSCD," .$field_name ."W1.COURSE_GROUP ORDER BY W1.SCORE DESC) AS COURSE_GROUP_RANK, ";
                $query .= "    RANK() OVER(PARTITION BY {$fieldCrc}W1.SUBCLASSCD," .$field_name ."W1.COURSE_GROUP ORDER BY W1.AVG DESC) AS COURSE_GROUP_AVG_RANK, ";
                if ($model->calcDeviationFlg == "1") {
                    $query .= "    DV.COURSE_GROUP_DEVIATION AS COURSE_GROUP_DEVIATION, ";
                    $query .= "    RANK() OVER(PARTITION BY {$fieldCrc}W1.SUBCLASSCD," .$field_name ."W1.COURSE_GROUP ORDER BY DV.COURSE_GROUP_DEVIATION DESC) AS COURSE_GROUP_DEVIATION_RANK, ";
                } else {
                    $query .= "    case when 0 < W6.STDDEV then DECIMAL(ROUND((10*(W1.SCORE-W6.AVG)/W6.STDDEV+50)*10,0)/10,5,1) end AS COURSE_GROUP_DEVIATION, ";
                    $query .= "    RANK() OVER(PARTITION BY {$fieldCrc}W1.SUBCLASSCD," .$field_name ."W1.COURSE_GROUP ORDER BY DECIMAL(ROUND((case when 0 < W6.STDDEV then 10*(W1.SCORE-W6.AVG)/W6.STDDEV else 0 end +50)*10,0)/10,5,1) DESC) AS COURSE_GROUP_DEVIATION_RANK, ";
                }
            }
            //A:クラス別コースグループ
            if ($model->Properties["useRecordRankDetail"] == '1') {
                $query .= "    RANK() OVER(PARTITION BY {$fieldCrc}W1.SUBCLASSCD," .$field_name ."W1.HR_CLASS,W1.COURSE_GROUP ORDER BY W1.SCORE DESC) AS COURSE_GROUP_CL_RANK, ";
                $query .= "    RANK() OVER(PARTITION BY {$fieldCrc}W1.SUBCLASSCD," .$field_name ."W1.HR_CLASS,W1.COURSE_GROUP ORDER BY W1.AVG DESC) AS COURSE_GROUP_CL_AVG_RANK, ";
                if ($model->calcDeviationFlg == "1") {
                    $query .= "    DV.COURSE_GROUP_CL_DEVIATION AS COURSE_GROUP_CL_DEVIATION, ";
                    $query .= "    RANK() OVER(PARTITION BY {$fieldCrc}W1.SUBCLASSCD," .$field_name ."W1.HR_CLASS,W1.COURSE_GROUP ORDER BY DV.COURSE_GROUP_CL_DEVIATION DESC) AS COURSE_GROUP_CL_DEVIATION_RANK, ";
                } else {
                    $query .= "    case when 0 < W8.STDDEV then DECIMAL(ROUND((10*(W1.SCORE-W8.AVG)/W8.STDDEV+50)*10,0)/10,5,1) end AS COURSE_GROUP_CL_DEVIATION, ";
                    $query .= "    RANK() OVER(PARTITION BY {$fieldCrc}W1.SUBCLASSCD," .$field_name ."W1.HR_CLASS,W1.COURSE_GROUP ORDER BY DECIMAL(ROUND((case when 0 < W8.STDDEV then 10*(W1.SCORE-W8.AVG)/W8.STDDEV else 0 end +50)*10,0)/10,5,1) DESC) AS COURSE_GROUP_CL_DEVIATION_RANK, ";
                }
            }
            //6:講座グループ
            if ($model->Properties["useRankChairGroup"] == '1') {
                $query .= "    RANK() OVER(PARTITION BY {$fieldCrc}W1.SUBCLASSCD," .$field_name ."W1.CHAIR_GROUP_CD ORDER BY W1.SCORE DESC) AS CHAIR_GROUP_RANK, ";
                $query .= "    RANK() OVER(PARTITION BY {$fieldCrc}W1.SUBCLASSCD," .$field_name ."W1.CHAIR_GROUP_CD ORDER BY W1.AVG DESC) AS CHAIR_GROUP_AVG_RANK, ";
                if ($model->calcDeviationFlg == "1") {
                    $query .= "    DV.CHAIR_GROUP_DEVIATION AS CHAIR_GROUP_DEVIATION, ";
                } else {
                    $query .= "    case when 0 < W7.STDDEV then DECIMAL(ROUND((10*(W1.SCORE-W7.AVG)/W7.STDDEV+50)*10,0)/10,5,1) end AS CHAIR_GROUP_DEVIATION, ";
                }
            }
            //DIV = B,C,D,E,F
            if ($model->Properties["useRecordRankDetail"] == '1') {
                //B:同クラス・同コース生徒内での順位
                $query .= "    RANK() OVER(PARTITION BY {$fieldCrc}W1.SUBCLASSCD," .$field_name ."W1.HR_CLASS,W1.COURSE ORDER BY W1.SCORE DESC) AS B_RANK, ";
                $query .= "    RANK() OVER(PARTITION BY {$fieldCrc}W1.SUBCLASSCD," .$field_name ."W1.HR_CLASS,W1.COURSE ORDER BY W1.AVG DESC) AS B_AVG_RANK, ";
                if ($model->calcDeviationFlg == "1") {
                    $query .= "    DV.B_DEVIATION AS B_DEVIATION, ";
                    $query .= "    RANK() OVER(PARTITION BY {$fieldCrc}W1.SUBCLASSCD," .$field_name ."W1.HR_CLASS,W1.COURSE ORDER BY DV.B_DEVIATION DESC) AS B_DEVIATION_RANK, ";
                } else {
                    $query .= "    case when 0 < WB.STDDEV then DECIMAL(ROUND((10*(W1.SCORE-WB.AVG)/WB.STDDEV+50)*10,0)/10,5,1) end AS B_DEVIATION, ";
                    $query .= "    RANK() OVER(PARTITION BY {$fieldCrc}W1.SUBCLASSCD," .$field_name ."W1.HR_CLASS,W1.COURSE ORDER BY DECIMAL(ROUND((case when 0 < WB.STDDEV then 10*(W1.SCORE-WB.AVG)/WB.STDDEV else 0 end +50)*10,0)/10,5,1) DESC) AS B_DEVIATION_RANK, ";
                }
                //C:同クラス・同コース・同高入生徒内での順位
                $query .= "    CASE WHEN W1.ENT_JH = 'H' THEN RANK() OVER(PARTITION BY {$fieldCrc}W1.SUBCLASSCD," .$field_name ."W1.HR_CLASS,W1.COURSE,W1.ENT_JH ORDER BY W1.SCORE DESC) ELSE NULL END AS C_RANK, ";
                $query .= "    CASE WHEN W1.ENT_JH = 'H' THEN RANK() OVER(PARTITION BY {$fieldCrc}W1.SUBCLASSCD," .$field_name ."W1.HR_CLASS,W1.COURSE,W1.ENT_JH ORDER BY W1.AVG DESC) ELSE NULL END AS C_AVG_RANK, ";
                if ($model->calcDeviationFlg == "1") {
                    $query .= "    CASE WHEN W1.ENT_JH = 'H' THEN DV.C_DEVIATION ELSE NULL END AS C_DEVIATION, ";
                    $query .= "    CASE WHEN W1.ENT_JH = 'H' THEN RANK() OVER(PARTITION BY {$fieldCrc}W1.SUBCLASSCD," .$field_name ."W1.HR_CLASS,W1.COURSE,W1.ENT_JH ORDER BY DV.C_DEVIATION DESC) ELSE NULL END AS C_DEVIATION_RANK, ";
                } else {
                    $query .= "    CASE WHEN W1.ENT_JH = 'H' THEN case when 0 < WC.STDDEV then DECIMAL(ROUND((10*(W1.SCORE-WC.AVG)/WC.STDDEV+50)*10,0)/10,5,1) end ELSE NULL END AS C_DEVIATION, ";
                    $query .= "    CASE WHEN W1.ENT_JH = 'H' THEN RANK() OVER(PARTITION BY {$fieldCrc}W1.SUBCLASSCD," .$field_name ."W1.HR_CLASS,W1.COURSE,W1.ENT_JH ORDER BY DECIMAL(ROUND((case when 0 < WC.STDDEV then 10*(W1.SCORE-WC.AVG)/WC.STDDEV else 0 end +50)*10,0)/10,5,1) DESC) ELSE NULL END AS C_DEVIATION_RANK, ";
                }
                //D:同クラス・同コース・同一貫生徒内での順位
                $query .= "    CASE WHEN W1.ENT_JH = 'J' THEN RANK() OVER(PARTITION BY {$fieldCrc}W1.SUBCLASSCD," .$field_name ."W1.HR_CLASS,W1.COURSE,W1.ENT_JH ORDER BY W1.SCORE DESC) ELSE NULL END AS D_RANK, ";
                $query .= "    CASE WHEN W1.ENT_JH = 'J' THEN RANK() OVER(PARTITION BY {$fieldCrc}W1.SUBCLASSCD," .$field_name ."W1.HR_CLASS,W1.COURSE,W1.ENT_JH ORDER BY W1.AVG DESC) ELSE NULL END AS D_AVG_RANK, ";
                if ($model->calcDeviationFlg == "1") {
                    $query .= "    CASE WHEN W1.ENT_JH = 'J' THEN DV.D_DEVIATION ELSE NULL END AS D_DEVIATION, ";
                    $query .= "    CASE WHEN W1.ENT_JH = 'J' THEN RANK() OVER(PARTITION BY {$fieldCrc}W1.SUBCLASSCD," .$field_name ."W1.HR_CLASS,W1.COURSE,W1.ENT_JH ORDER BY DV.D_DEVIATION DESC) ELSE NULL END AS D_DEVIATION_RANK, ";
                } else {
                    $query .= "    CASE WHEN W1.ENT_JH = 'J' THEN case when 0 < WD.STDDEV then DECIMAL(ROUND((10*(W1.SCORE-WD.AVG)/WD.STDDEV+50)*10,0)/10,5,1) end ELSE NULL END AS D_DEVIATION, ";
                    $query .= "    CASE WHEN W1.ENT_JH = 'J' THEN RANK() OVER(PARTITION BY {$fieldCrc}W1.SUBCLASSCD," .$field_name ."W1.HR_CLASS,W1.COURSE,W1.ENT_JH ORDER BY DECIMAL(ROUND((case when 0 < WD.STDDEV then 10*(W1.SCORE-WD.AVG)/WD.STDDEV else 0 end +50)*10,0)/10,5,1) DESC) ELSE NULL END AS D_DEVIATION_RANK, ";
                }
                //E:同学年・同コース・同高入生徒内での順位
                $query .= "    CASE WHEN W1.ENT_JH = 'H' THEN RANK() OVER(PARTITION BY {$fieldCrc}W1.SUBCLASSCD," .$field_name ."W1.GRADE,W1.COURSE,W1.ENT_JH ORDER BY W1.SCORE DESC) ELSE NULL END AS E_RANK, ";
                $query .= "    CASE WHEN W1.ENT_JH = 'H' THEN RANK() OVER(PARTITION BY {$fieldCrc}W1.SUBCLASSCD," .$field_name ."W1.GRADE,W1.COURSE,W1.ENT_JH ORDER BY W1.AVG DESC) ELSE NULL END AS E_AVG_RANK, ";
                if ($model->calcDeviationFlg == "1") {
                    $query .= "    CASE WHEN W1.ENT_JH = 'H' THEN DV.E_DEVIATION ELSE NULL END AS E_DEVIATION, ";
                    $query .= "    CASE WHEN W1.ENT_JH = 'H' THEN RANK() OVER(PARTITION BY {$fieldCrc}W1.SUBCLASSCD," .$field_name ."W1.GRADE,W1.COURSE,W1.ENT_JH ORDER BY DV.E_DEVIATION DESC) ELSE NULL END AS E_DEVIATION_RANK, ";
                } else {
                    $query .= "    CASE WHEN W1.ENT_JH = 'H' THEN case when 0 < WE.STDDEV then DECIMAL(ROUND((10*(W1.SCORE-WE.AVG)/WE.STDDEV+50)*10,0)/10,5,1) end ELSE NULL END AS E_DEVIATION, ";
                    $query .= "    CASE WHEN W1.ENT_JH = 'H' THEN RANK() OVER(PARTITION BY {$fieldCrc}W1.SUBCLASSCD," .$field_name ."W1.GRADE,W1.COURSE,W1.ENT_JH ORDER BY DECIMAL(ROUND((case when 0 < WE.STDDEV then 10*(W1.SCORE-WE.AVG)/WE.STDDEV else 0 end +50)*10,0)/10,5,1) DESC) ELSE NULL END AS E_DEVIATION_RANK, ";
                }
                //F:同学年・同コース・同一貫生徒内での順位
                $query .= "    CASE WHEN W1.ENT_JH = 'J' THEN RANK() OVER(PARTITION BY {$fieldCrc}W1.SUBCLASSCD," .$field_name ."W1.GRADE,W1.COURSE,W1.ENT_JH ORDER BY W1.SCORE DESC) ELSE NULL END AS F_RANK, ";
                $query .= "    CASE WHEN W1.ENT_JH = 'J' THEN RANK() OVER(PARTITION BY {$fieldCrc}W1.SUBCLASSCD," .$field_name ."W1.GRADE,W1.COURSE,W1.ENT_JH ORDER BY W1.AVG DESC) ELSE NULL END AS F_AVG_RANK, ";
                if ($model->calcDeviationFlg == "1") {
                    $query .= "    CASE WHEN W1.ENT_JH = 'J' THEN DV.F_DEVIATION ELSE NULL END AS F_DEVIATION, ";
                    $query .= "    CASE WHEN W1.ENT_JH = 'J' THEN RANK() OVER(PARTITION BY {$fieldCrc}W1.SUBCLASSCD," .$field_name ."W1.GRADE,W1.COURSE,W1.ENT_JH ORDER BY DV.F_DEVIATION DESC) ELSE NULL END AS F_DEVIATION_RANK, ";
                } else {
                    $query .= "    CASE WHEN W1.ENT_JH = 'J' THEN case when 0 < WF.STDDEV then DECIMAL(ROUND((10*(W1.SCORE-WF.AVG)/WF.STDDEV+50)*10,0)/10,5,1) end ELSE NULL END AS F_DEVIATION, ";
                    $query .= "    CASE WHEN W1.ENT_JH = 'J' THEN RANK() OVER(PARTITION BY {$fieldCrc}W1.SUBCLASSCD," .$field_name ."W1.GRADE,W1.COURSE,W1.ENT_JH ORDER BY DECIMAL(ROUND((case when 0 < WF.STDDEV then 10*(W1.SCORE-WF.AVG)/WF.STDDEV else 0 end +50)*10,0)/10,5,1) DESC) ELSE NULL END AS F_DEVIATION_RANK, ";
                }
            }
            //DIV = G
            if ($model->Properties["useRecordRankDetail"] == '1' && $model->Properties["knjd141c_deviationTotalRank"] == '1') {
                //G:同学年生徒内での順位
                if ($model->calcDeviationFlg == "1") {
                    $query .= "    DV.GRADE_DEVIATION_TOTAL AS GRADE_DEVIATION_TOTAL, ";
                    $query .= "    RANK() OVER(PARTITION BY {$fieldCrc}W1.SUBCLASSCD," .$field_name ."W1.GRADE ORDER BY DV.GRADE_DEVIATION_TOTAL DESC) AS GRADE_DEVIATION_TOTAL_RANK, ";
                } else {
                    $query .= "    NULL AS GRADE_DEVIATION_TOTAL, ";
                    $query .= "    NULL AS GRADE_DEVIATION_TOTAL_RANK, ";
                }
            }
            if ($model->calcDeviationFlg == "1") {
                $query .= "    DV.GRADE_DEVIATION  AS GRADE_DEVIATION, ";
                $query .= "    DV.CLASS_DEVIATION  AS CLASS_DEVIATION, ";
                $query .= "    DV.COURSE_DEVIATION AS COURSE_DEVIATION, ";
                $query .= "    DV.MAJOR_DEVIATION  AS MAJOR_DEVIATION, ";
                $query .= "    RANK() OVER(PARTITION BY {$fieldCrc}W1.SUBCLASSCD," .$field_name ."W1.GRADE    ORDER BY DV.GRADE_DEVIATION  DESC) AS GRADE_DEVIATION_RANK, ";
                $query .= "    RANK() OVER(PARTITION BY {$fieldCrc}W1.SUBCLASSCD," .$field_name ."W1.HR_CLASS ORDER BY DV.CLASS_DEVIATION  DESC) AS CLASS_DEVIATION_RANK, ";
                $query .= "    RANK() OVER(PARTITION BY {$fieldCrc}W1.SUBCLASSCD," .$field_name ."W1.COURSE   ORDER BY DV.COURSE_DEVIATION DESC) AS COURSE_DEVIATION_RANK, ";
                $query .= "    RANK() OVER(PARTITION BY {$fieldCrc}W1.SUBCLASSCD," .$field_name ."W1.MAJOR    ORDER BY DV.MAJOR_DEVIATION  DESC) AS MAJOR_DEVIATION_RANK ";
            } else {
                $query .= "    case when 0 < W2.STDDEV then DECIMAL(ROUND((10*(W1.SCORE-W2.AVG)/W2.STDDEV+50)*10,0)/10,5,1) end AS GRADE_DEVIATION, ";
                $query .= "    case when 0 < W3.STDDEV then DECIMAL(ROUND((10*(W1.SCORE-W3.AVG)/W3.STDDEV+50)*10,0)/10,5,1) end AS CLASS_DEVIATION, ";
                $query .= "    case when 0 < W4.STDDEV then DECIMAL(ROUND((10*(W1.SCORE-W4.AVG)/W4.STDDEV+50)*10,0)/10,5,1) end AS COURSE_DEVIATION, ";
                $query .= "    case when 0 < W5.STDDEV then DECIMAL(ROUND((10*(W1.SCORE-W5.AVG)/W5.STDDEV+50)*10,0)/10,5,1) end AS MAJOR_DEVIATION, ";
                $query .= "    RANK() OVER(PARTITION BY {$fieldCrc}W1.SUBCLASSCD," .$field_name ."W1.GRADE    ORDER BY DECIMAL(ROUND((case when 0 < W2.STDDEV then 10*(W1.SCORE-W2.AVG)/W2.STDDEV else 0 end +50)*10,0)/10,5,1) DESC) AS GRADE_DEVIATION_RANK, ";
                $query .= "    RANK() OVER(PARTITION BY {$fieldCrc}W1.SUBCLASSCD," .$field_name ."W1.HR_CLASS ORDER BY DECIMAL(ROUND((case when 0 < W3.STDDEV then 10*(W1.SCORE-W3.AVG)/W3.STDDEV else 0 end +50)*10,0)/10,5,1) DESC) AS CLASS_DEVIATION_RANK, ";
                $query .= "    RANK() OVER(PARTITION BY {$fieldCrc}W1.SUBCLASSCD," .$field_name ."W1.COURSE   ORDER BY DECIMAL(ROUND((case when 0 < W4.STDDEV then 10*(W1.SCORE-W4.AVG)/W4.STDDEV else 0 end +50)*10,0)/10,5,1) DESC) AS COURSE_DEVIATION_RANK, ";
                $query .= "    RANK() OVER(PARTITION BY {$fieldCrc}W1.SUBCLASSCD," .$field_name ."W1.MAJOR    ORDER BY DECIMAL(ROUND((case when 0 < W5.STDDEV then 10*(W1.SCORE-W5.AVG)/W5.STDDEV else 0 end +50)*10,0)/10,5,1) DESC) AS MAJOR_DEVIATION_RANK ";
            }

            $query .= "FROM ";
            $query .= "    T_RANK" .$table_name ." W1 ";
            if ($model->calcDeviationFlg == "1") {
                $query .= "    LEFT JOIN T_DEVIATION_ALL DV ON DV.SUBCLASSCD = W1.SUBCLASSCD AND DV.SCHREGNO = W1.SCHREGNO ";
            } else {
                $query .= "    LEFT JOIN T_AVERAGE W2 ON W2.AVG_DIV = '1' AND {$whereCrc2}W2.SUBCLASSCD = W1.SUBCLASSCD AND " .$where_name2 ."W2.GRADE = W1.GRADE ";
                $query .= "    LEFT JOIN T_AVERAGE W3 ON W3.AVG_DIV = '2' AND {$whereCrc3}W3.SUBCLASSCD = W1.SUBCLASSCD AND " .$where_name3 ."W3.GRADE = W1.GRADE AND W3.HR_CLASS = W1.HR_CLASS ";
                $query .= "    LEFT JOIN T_AVERAGE W4 ON W4.AVG_DIV = '3' AND {$whereCrc4}W4.SUBCLASSCD = W1.SUBCLASSCD AND " .$where_name4 ."W4.GRADE = W1.GRADE AND W4.COURSE = W1.COURSE ";
                $query .= "    LEFT JOIN T_AVERAGE W5 ON W5.AVG_DIV = '4' AND {$whereCrc5}W5.SUBCLASSCD = W1.SUBCLASSCD AND " .$where_name5 ."W5.GRADE = W1.GRADE AND W5.MAJOR = W1.MAJOR ";
                //5:コースグループ
                if ($model->Properties["useRankCourseGroup"] == '1' || $model->Properties["useRankCourseGroupVersion2"] == '1' || $model->Properties["useRecordRankDetail"] == '1') {
                    $query .= "    LEFT JOIN T_AVERAGE W6 ON W6.AVG_DIV = '5' AND {$whereCrc6}W6.SUBCLASSCD = W1.SUBCLASSCD AND " .$where_name6 ."W6.GRADE = W1.GRADE AND W6.COURSE_GROUP = W1.COURSE_GROUP ";
                }
                //A:クラス別コースグループ
                if ($model->Properties["useRecordRankDetail"] == '1') {
                    $query .= "    LEFT JOIN T_AVERAGE W8 ON W8.AVG_DIV = 'A' AND {$whereCrc8}W8.SUBCLASSCD = W1.SUBCLASSCD AND " .$where_name8 ."W8.GRADE = W1.GRADE AND W8.HR_CLASS = W1.HR_CLASS AND W8.COURSE_GROUP = W1.COURSE_GROUP ";
                }
                //6:講座グループ
                if ($model->Properties["useRankChairGroup"] == '1') {
                    $query .= "    LEFT JOIN T_AVERAGE W7 ON W7.AVG_DIV = '6' AND {$whereCrc7}W7.SUBCLASSCD = W1.SUBCLASSCD AND " .$where_name7 ."W7.GRADE = W1.GRADE AND W7.CHAIR_GROUP_CD = W1.CHAIR_GROUP_CD ";
                }
                //DIV = B,C,D,E,F
                if ($model->Properties["useRecordRankDetail"] == '1') {
                    $query .= "    LEFT JOIN T_AVERAGE WB ON WB.AVG_DIV = 'B' AND {$whereCrcB}WB.SUBCLASSCD = W1.SUBCLASSCD AND " .$where_nameB ."WB.GRADE = W1.GRADE AND WB.HR_CLASS = W1.HR_CLASS AND WB.COURSE = W1.COURSE ";
                    $query .= "    LEFT JOIN T_AVERAGE WC ON WC.AVG_DIV = 'C' AND {$whereCrcC}WC.SUBCLASSCD = W1.SUBCLASSCD AND " .$where_nameC ."WC.GRADE = W1.GRADE AND WC.HR_CLASS = W1.HR_CLASS AND WC.COURSE = W1.COURSE ";
                    $query .= "    LEFT JOIN T_AVERAGE WD ON WD.AVG_DIV = 'D' AND {$whereCrcD}WD.SUBCLASSCD = W1.SUBCLASSCD AND " .$where_nameD ."WD.GRADE = W1.GRADE AND WD.HR_CLASS = W1.HR_CLASS AND WD.COURSE = W1.COURSE ";
                    $query .= "    LEFT JOIN T_AVERAGE WE ON WE.AVG_DIV = 'E' AND {$whereCrcE}WE.SUBCLASSCD = W1.SUBCLASSCD AND " .$where_nameE ."WE.GRADE = W1.GRADE AND WE.COURSE = W1.COURSE ";
                    $query .= "    LEFT JOIN T_AVERAGE WF ON WF.AVG_DIV = 'F' AND {$whereCrcF}WF.SUBCLASSCD = W1.SUBCLASSCD AND " .$where_nameF ."WF.GRADE = W1.GRADE AND WF.COURSE = W1.COURSE ";
                }
            }
            $query .= "WHERE ";
            $query .= "    W1.SUBCLASSCD in ('333333','555555','999999','99999A','99999B') ";
            //処理科目(総合計のみ)
            if ($model->subclasscd == '999999') {
                $query .= "  and W1.SUBCLASSCD in ('333333','555555','999999','99999A','99999B') ";
            //処理科目(各科目のみ)
            } elseif ($model->subclasscd != '000000') {
                $query .= "  and W1.CLASSCD || '-' || W1.SCHOOL_KIND || '-' || W1.CURRICULUM_CD || '-' || W1.SUBCLASSCD = '".$model->subclasscd."' ";
            }
        }
        return $query;
    }

    //科目別の処理の追加クエリー
    public function getInsertSubclassQuery($model, $table)
    {
        $query  = "";
        //（共通）追加クエリー
        $query .= knjd141cQuery::getInsertCommonQuery($model, $table);

        if ($table == "record_average_chair" || $table == "record_rank_chair") {
            $query .= knjd141cQuery::getChairQuery($model);
        }

        //メイン
        if ($table == "record_average" || $table == "record_average_chair") {
            $query .= knjd141cQuery::getInsertAverageQuery($model, $table);
        } elseif ($table == "record_rank" || $table == "record_rank_chair") {
            //成績平均データ
            $query .= ",T_AVERAGE AS ( ";
            $query .= knjd141cQuery::getRecordAverageQuery($model, $table);
            $query .= "    ) ";

            $query .= knjd141cQuery::getInsertRankQuery($model, $table);
        }
        return $query;
    }

    //講座クエリー
    public function getChairQuery($model)
    {
        //教育課程対応
        $fieldCrc = $whereCrc2 = "";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $fieldCrc   = "W1.CLASSCD, W1.SCHOOL_KIND, W1.CURRICULUM_CD, ";
            $whereCrc2  = "W2.CLASSCD = W1.CLASSCD AND W2.SCHOOL_KIND = W1.SCHOOL_KIND AND W2.CURRICULUM_CD = W1.CURRICULUM_CD AND ";
        }
        $query  = "";
        $query .= ",T_CHAIR AS ( ";
        $query .= "SELECT ";
        $query .= "    {$fieldCrc}W1.SUBCLASSCD, W2.CHAIRCD, W2.SCHREGNO ";
        $query .= "FROM ";
        $query .= "    CHAIR_DAT W1, ";
        $query .= "    CHAIR_STD_DAT W2 ";
        $query .= "WHERE ";
        $query .= "    W1.YEAR = '".CTRL_YEAR."' AND ";
        $query .= "    W2.YEAR = W1.YEAR AND ";
        $query .= "    W2.SEMESTER = W1.SEMESTER AND ";
        $query .= "    W2.CHAIRCD = W1.CHAIRCD AND ";
        $query .= "    '".str_replace("/", "-", $model->chairdate)."' BETWEEN W2.APPDATE AND W2.APPENDDATE ";
        $query .= "GROUP BY ";
        $query .= "    {$fieldCrc}W1.SUBCLASSCD, W2.CHAIRCD, W2.SCHREGNO ";
        $query .= ")  ";
        $query .= ",T_RANK_CHAIR AS ( ";
        $query .= "SELECT ";
        $query .= "    {$fieldCrc}W1.SUBCLASSCD, W2.CHAIRCD, W1.SCHREGNO, ";
        $query .= "    W1.SCORE, W1.AVG, ";
        $query .= "    W1.GRADE, W1.HR_CLASS, ";
        $query .= "    W1.COURSECD, W1.MAJORCD, W1.COURSECODE, ";
        $query .= "    W1.COURSE, ";
        $query .= "    W1.MAJOR ";
        //5:コースグループ
        if ($model->Properties["useRankCourseGroup"] == '1' || $model->Properties["useRankCourseGroupVersion2"] == '1' || $model->Properties["useRecordRankDetail"] == '1') {
            $query .= "   ,W1.COURSE_GROUP ";
        }
        //6:講座グループ
        if ($model->Properties["useRankChairGroup"] == '1') {
            $query .= "   ,W1.CHAIR_GROUP_CD ";
        }
        //7:寮生
        if ($model->Properties["useAvgRyousei"] == '1') {
            $query .= "   ,RYOUSEI_FLG ";
        }
        //DIV = B,C,D,E,F
        if ($model->Properties["useRecordRankDetail"] == '1') {
            $query .= "   ,ENT_JH ";
        }
        $query .= "FROM ";
        $query .= "    T_RANK W1 ";
        $query .= "    INNER JOIN T_CHAIR W2 ON {$whereCrc2}W2.SUBCLASSCD=W1.SUBCLASSCD AND W2.SCHREGNO = W1.SCHREGNO ";
        $query .= ")  ";
        return $query;
    }

    //講座グループ
    public function getChairGroup($model)
    {
        //教育課程対応
        $fieldCrc = $whereCrc2 = "";
        if ($model->Properties["useCurriculumcd"] == '1') {
            $fieldCrc   = "W1.CLASSCD, W1.SCHOOL_KIND, W1.CURRICULUM_CD, ";
            $whereCrc2  = "W3.CLASSCD = W1.CLASSCD AND W3.SCHOOL_KIND = W1.SCHOOL_KIND AND W3.CURRICULUM_CD = W1.CURRICULUM_CD AND ";
        }
        $query  = "";
        //講座グループ設定
        if ($model->Properties["use_CHAIR_GROUP_SDIV_DAT"] === '1') {
            $query .= ",CHAIR_GROUP_SDIV AS ( ";
            $query .= " SELECT ";
            $query .= "     0 AS PRIORITY, ";
            $query .= "     W1.CHAIR_GROUP_CD, ";
            $query .= "     W2.SEMESTER, ";
            $query .= "     W2.TESTKINDCD, ";
            $query .= "     W2.TESTITEMCD, ";
            $query .= "     W2.SCORE_DIV, ";
            $query .= "     W2.CHAIRCD ";
            $query .= " FROM ";
            $query .= "     CHAIR_GROUP_MST W1, ";
            $query .= "     CHAIR_GROUP_SDIV_DAT W2 ";
            $query .= " WHERE ";
            $query .= "     W1.YEAR = '".CTRL_YEAR."' ";
            if ($model->seme == "9") {
                $query .= "     AND W1.SEMESTER = '".CTRL_SEMESTER."' "; //ログイン学期
            } else {
                $query .= "     AND W1.SEMESTER = '".$model->seme."' ";
            }
            $query .= "     AND W2.YEAR = W1.YEAR ";
            $query .= "     AND W2.SEMESTER = W1.SEMESTER ";
            $query .= "     AND W2.CHAIR_GROUP_CD = W1.CHAIR_GROUP_CD ";
            $query .= "     AND W2.TESTKINDCD   = '".$model->kind."' ";
            $query .= "     AND W2.TESTITEMCD   = '".$model->item."' ";
            $query .= "     AND W2.SCORE_DIV    = '".$model->sdiv."' ";
            $query .= " UNION ALL ";
            $query .= " SELECT ";
            $query .= "     1 AS PRIORITY, ";
            $query .= "     W1.CHAIR_GROUP_CD, ";
            $query .= "     W2.SEMESTER, ";
            $query .= "     W2.TESTKINDCD, ";
            $query .= "     W2.TESTITEMCD, ";
            $query .= "     W2.SCORE_DIV, ";
            $query .= "     W2.CHAIRCD ";
            $query .= " FROM ";
            $query .= "     CHAIR_GROUP_MST W1, ";
            $query .= "     CHAIR_GROUP_SDIV_DAT W2 ";
            $query .= " WHERE ";
            $query .= "     W1.YEAR = '".CTRL_YEAR."' ";
            if ($model->seme == "9") {
                $query .= "     AND W1.SEMESTER = '".CTRL_SEMESTER."' "; //ログイン学期
            } else {
                $query .= "     AND W1.SEMESTER = '".$model->seme."' ";
            }
            $query .= "     AND W2.YEAR = W1.YEAR ";
            $query .= "     AND W2.SEMESTER = W1.SEMESTER ";
            $query .= "     AND W2.CHAIR_GROUP_CD = W1.CHAIR_GROUP_CD ";
            $query .= "     AND W2.TESTKINDCD   = '00' ";
            $query .= "     AND W2.TESTITEMCD   = '00' ";
            $query .= "     AND W2.SCORE_DIV    = '00' ";
            $query .= ") ";
        }
        $query .= ",CHAIR_GROUP AS ( ";
        $query .= " SELECT ";
        $query .= "     SUBSTR(W1.CHAIR_GROUP_CD, LENGTH(W1.CHAIR_GROUP_CD)-2) AS CHAIR_GROUP_CD, "; //下３桁
        $query .= "     {$fieldCrc} W1.SUBCLASSCD, ";
        $query .= "     W2.CHAIRCD ";
        $query .= " FROM ";
        if ($model->Properties["use_CHAIR_GROUP_SDIV_DAT"] === '1') {
            $query .= "     CHAIR_GROUP_MST W1 ";
            $query .= "     INNER JOIN CHAIR_GROUP_SDIV_DAT W2 ON ";
            $query .= "             W2.YEAR = W1.YEAR ";
            $query .= "         AND W2.SEMESTER  = W1.SEMESTER ";
            $query .= "         AND W2.CHAIR_GROUP_CD = W1.CHAIR_GROUP_CD ";
            $query .= "     INNER JOIN ( ";
            $query .= "         SELECT ";
            $query .= "             CHAIRCD, ";
            $query .= "             MIN(PRIORITY) AS PRIORITY ";
            $query .= "         FROM ";
            $query .= "             CHAIR_GROUP_SDIV T1 ";
            $query .= "         GROUP BY ";
            $query .= "             CHAIRCD ";
            $query .= "     ) W3 ON W3.CHAIRCD = W2.CHAIRCD ";
            $query .= "     INNER JOIN CHAIR_GROUP_SDIV W4 ON ";
            $query .= "             W4.CHAIRCD = W3.CHAIRCD ";
            $query .= "         AND W4.PRIORITY = W3.PRIORITY ";
            $query .= "         AND W4.SEMESTER = W2.SEMESTER ";
            $query .= "         AND W4.TESTKINDCD = W2.TESTKINDCD ";
            $query .= "         AND W4.TESTITEMCD = W2.TESTITEMCD ";
            $query .= "         AND W4.SCORE_DIV = W2.SCORE_DIV ";
            $query .= " WHERE ";
            $query .= "     W1.YEAR = '".CTRL_YEAR."' ";
            if ($model->seme == "9") {
                $query .= "     AND W1.SEMESTER = '".CTRL_SEMESTER."' "; //ログイン学期
            } else {
                $query .= "     AND W1.SEMESTER = '".$model->seme."' ";
            }
        } else {
            $query .= "     CHAIR_GROUP_MST W1, ";
            $query .= "     CHAIR_GROUP_DAT W2 ";
            $query .= " WHERE ";
            $query .= "     W1.YEAR = '".CTRL_YEAR."' ";
            $query .= "     AND W1.SEMESTER = '".CTRL_SEMESTER."' "; //ログイン学期
            $query .= "     AND W2.YEAR = W1.YEAR ";
            $query .= "     AND W2.SEMESTER = W1.SEMESTER ";
            $query .= "     AND W2.CHAIR_GROUP_CD = W1.CHAIR_GROUP_CD ";
            $query .= "     AND W2.TESTKINDCD || W2.TESTITEMCD = '0000' "; //固定:0000
        }
        $query .= ")  ";
        //講座名簿とリンクし、生徒別科目別の講座グループを取得
        $query .= ",T_CHAIR_GROUP AS ( ";
        $query .= "SELECT ";
        $query .= "    {$fieldCrc} W1.SUBCLASSCD, W2.SCHREGNO, ";
        $query .= "    MIN(W3.CHAIR_GROUP_CD) AS CHAIR_GROUP_CD "; //MIN講座グループ
        $query .= "FROM ";
        $query .= "    CHAIR_DAT W1, ";
        $query .= "    CHAIR_STD_DAT W2, ";
        $query .= "    CHAIR_GROUP W3 ";
        $query .= "WHERE ";
        $query .= "    W1.YEAR = '".CTRL_YEAR."' AND ";
        $query .= "    W2.YEAR = W1.YEAR AND ";
        $query .= "    W2.SEMESTER = W1.SEMESTER AND ";
        $query .= "    W2.CHAIRCD = W1.CHAIRCD AND ";
        $query .= "    '".str_replace("/", "-", $model->chairdate)."' BETWEEN W2.APPDATE AND W2.APPENDDATE AND ";
        $query .= "    {$whereCrc2} W3.SUBCLASSCD = W1.SUBCLASSCD AND W3.CHAIRCD = W2.CHAIRCD ";
        $query .= "GROUP BY ";
        $query .= "    {$fieldCrc} W1.SUBCLASSCD, W2.SCHREGNO ";
        $query .= ")  ";
        return $query;
    }

    /********************↓↓↓相対評定マスタ作成↓↓↓********************/

    //【処理条件：画面】
    //・プロパティー「knjd141c_useRelativeAssessCheckbox = 1」の時、「評定マスタ作成」チェックボックスを表示する。
    //・以下の時、「評定マスタ作成」チェックボックスを選択可能とする。
    //・考査種別小分類が「０８評価」の時（例．学年評価）
    //・処理科目コンボが「全て」「各科目」の時

    //【処理条件：データ部】
    //・5段階法評価設定が設定されていること
    //・対象科目が存在すること（RECORD_AVERAGE_SDIV_DAT）

    /***
    //調査用SQL
    //1)各評定人数(割合から計算、四捨五入)
    SELECT
        T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD AS SUBCLASSCD,
        T1.COUNT,
        T2.ASSESSLEVEL,
        T2.ASSESSRATE,
        ROUND(FLOAT(T1.COUNT)*FLOAT(T2.ASSESSRATE)/100,0) AS RATE_CNT
    FROM
        RECORD_AVERAGE_SDIV_DAT T1
        INNER JOIN RELATIVEASSESS_RATE_MST T2 ON T2.YEAR = T1.YEAR
        INNER JOIN V_SUBCLASS_MST T3 ON T3.YEAR = T1.YEAR
            AND T3.CLASSCD      = T1.CLASSCD
            AND T3.SCHOOL_KIND  = T1.SCHOOL_KIND
            AND T3.CURRICULUM_CD= T1.CURRICULUM_CD
            AND T3.SUBCLASSCD   = T1.SUBCLASSCD
    WHERE
        T1.YEAR = '2006'
        AND T1.SEMESTER     = '9'
        AND T1.TESTKINDCD   = '99'
        AND T1.TESTITEMCD   = '00'
        AND T1.SCORE_DIV    = '08'
        AND T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD = '11-H-2-110005'
        AND T1.AVG_DIV      = '1'
        AND T1.GRADE        = '05'
        AND T1.HR_CLASS     = '000'
        AND T1.COURSECD     = '0'
        AND T1.MAJORCD      = '000'
        AND T1.COURSECODE   = '0000'
    ORDER BY
        SUBCLASSCD,
        T2.ASSESSLEVEL DESC
    ***/

    //相対評定マスタ作成
    public function executeAssess($model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //評定配列（降順）を取得　5段階法評価設定
        $defAssArray = knjd141cQuery::getAssArray($db);
        //存在チェック
        if (count($defAssArray) < 5) {
            return false;
        }

        //科目配列を取得
        $subArray = knjd141cQuery::getSubArray($db, $model);
        //存在チェック
        if (count($subArray) == 0) {
            return false;
        }

        //科目ループ
        foreach ($subArray as $subcd => $subRow) {
            //得点配列を取得
            $scoreArray = knjd141cQuery::getScoreArray($db, $model, $subcd);
            //全体人数
            $allCnt = 0;
            foreach ($scoreArray as $sco => $scoreRow) {
                $allCnt += (int) $scoreRow["CNT"];
            }
            //科目ごとの評定配列
            $assArray = array();
            foreach ($defAssArray as $level => $assRow) {
                //1)各評定人数(割合から計算、四捨五入)
                $rateCnt = (int) round($allCnt * $assRow["ASSESSRATE"] / 100);
                $assArray[$level] = array("ASSESSLEVEL" => $assRow["ASSESSLEVEL"],
                                          "ASSESSRATE"  => $assRow["ASSESSRATE"],
                                          "RATE_CNT"    => $rateCnt,
                                          "ASSESSLOW"   => 0,
                                          "ASSESSHIGH"  => 0
                                    );
            }
            //2)各評定の成績範囲(下限値、上限値)
            //・評定5～2で割合100%になる前提
            //・評定2の下限値(最小値)は1・・・//TODO:高倉さん
            $scoreCnt = 0;
            $scoreMax = 100; //評定5の上限値(初期値)
            $lev = count($defAssArray); //評定5
            foreach ($scoreArray as $sco => $scoreRow) {
                if ($lev == 1) {
                    continue;
                }
                $score      = (int) $scoreRow["SCORE"];
                $scoreCnt  += (int) $scoreRow["CNT"];
                if ($score > $scoreMax) {
                    $scoreMax = $score;
                }
                if ($score == 0) {
                    continue;
                } //0点はセットしない
                //下限値
                $assArray[$lev]["ASSESSLOW"] = $score; //評定5～2の下限値
                //各評定人数以上になったら、次の評定へカウントダウン
                if ($scoreCnt >= $assArray[$lev]["RATE_CNT"]) {
                    $scoreCnt = 0;
                    $lev--;
                }
            }
            if ($assArray[5]["ASSESSLOW"] < 4) {
                $assArray[5]["ASSESSLOW"] = 4;
            } //評定5の下限値(最小値)は4
            if ($assArray[4]["ASSESSLOW"] < 3) {
                $assArray[4]["ASSESSLOW"] = 3;
            } //評定4の下限値(最小値)は3
            if ($assArray[3]["ASSESSLOW"] < 2) {
                $assArray[3]["ASSESSLOW"] = 2;
            } //評定3の下限値(最小値)は2
            if ($assArray[2]["ASSESSLOW"] < 1) {
                $assArray[2]["ASSESSLOW"] = 1;
            } //評定2の下限値(最小値)は1・・・//TODO:高倉さん
            $assArray[1]["ASSESSLOW"] = 0; //評定1の下限値
            //上限値
            foreach ($defAssArray as $level => $assRow) {
                if ($level == 5) {
                    $assArray[$level]["ASSESSHIGH"] = $scoreMax; //評定5の上限値
                } else {
                    $assArray[$level]["ASSESSHIGH"] = $assArray[$level + 1]["ASSESSLOW"] - 1; //評定4～1の上限値
                }
            }
            //削除・追加　RELATIVEASSESS_MST
            knjd141cQuery::delInsRelativeAssessMst($db, $model, $subcd, $assArray);
        }

        $db->commit();
        Query::dbCheckIn($db);
        return true;
    }

    //評定配列（降順）
    public function getAssArray($db)
    {
        $rtnArray = array();
        $result = $db->query(knjd141cQuery::getAssessList());
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $key = $row["ASSESSLEVEL"];
            $rtnArray[$key] = array("ASSESSLEVEL"   => $row["ASSESSLEVEL"],
                                    "ASSESSRATE"    => $row["ASSESSRATE"]
                                );
        }
        $result->free();
        return $rtnArray;
    }

    //科目配列
    public function getSubArray($db, $model)
    {
        $rtnArray = array();
        $result = $db->query(knjd141cQuery::getSubcdList($model));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $key = $row["SUBCLASSCD"];
            $rtnArray[$key] = array("SUBCLASSCD"    => $row["SUBCLASSCD"]
                                );
        }
        $result->free();
        return $rtnArray;
    }

    //得点配列（降順）
    public function getScoreArray($db, $model, $subcd)
    {
        $rtnArray = array();
        $result = $db->query(knjd141cQuery::getScoreCntList($model, $subcd));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $key = $row["SCORE"];
            $rtnArray[$key] = array("SCORE"     => $row["SCORE"],
                                    "CNT"       => $row["CNT"]
                                );
        }
        $result->free();
        return $rtnArray;
    }

    //5段階法評価設定
    public function getAssessList()
    {
        $query  = " SELECT ";
        $query .= "     ASSESSLEVEL, ";
        $query .= "     ASSESSRATE ";
        $query .= " FROM ";
        $query .= "     RELATIVEASSESS_RATE_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= " ORDER BY ";
        $query .= "     ASSESSLEVEL DESC ";
        return $query;
    }

    //処理科目取得　科目、科目ごとの人数
    public function getSubcdList($model)
    {
        $query  = " SELECT ";
        $query .= "     T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD AS SUBCLASSCD ";
        $query .= " FROM ";
        $query .= "     RECORD_AVERAGE_SDIV_DAT T1 ";
        $query .= "     INNER JOIN V_SUBCLASS_MST T3 ON T3.YEAR = T1.YEAR ";
        $query .= "         AND T3.CLASSCD      = T1.CLASSCD ";
        $query .= "         AND T3.SCHOOL_KIND  = T1.SCHOOL_KIND ";
        $query .= "         AND T3.CURRICULUM_CD= T1.CURRICULUM_CD ";
        $query .= "         AND T3.SUBCLASSCD   = T1.SUBCLASSCD ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND T1.SEMESTER     = '".$model->seme."' ";
        $query .= "     AND T1.TESTKINDCD   = '".$model->kind."' ";
        $query .= "     AND T1.TESTITEMCD   = '".$model->item."' ";
        $query .= "     AND T1.SCORE_DIV    = '".$model->sdiv."' ";
        //処理科目(各科目のみ)
        if ($model->subclasscd != '000000') {
            $query .= "     AND T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD = '".$model->subclasscd."' ";
        }
        $query .= "     AND T1.AVG_DIV      = '1' ";
        $query .= "     AND T1.GRADE        = '".$model->grade."' ";
        $query .= "     AND T1.HR_CLASS     = '000' ";
        $query .= "     AND T1.COURSECD     = '0' ";
        $query .= "     AND T1.MAJORCD      = '000' ";
        $query .= "     AND T1.COURSECODE   = '0000' ";
        $query .= " ORDER BY ";
        $query .= "     SUBCLASSCD ";
        return $query;
    }

    //処理成績取得　得点、得点ごとの人数
    public function getScoreCntList($model, $subcd)
    {
        $query  = " SELECT ";
        $query .= "     T1.SCORE, ";
        $query .= "     COUNT(T1.SCHREGNO) AS CNT ";
        $query .= " FROM ";
        $query .= "     RECORD_RANK_SDIV_DAT T1 ";
        $query .= "     INNER JOIN SCHREG_REGD_DAT T2 ON T2.SCHREGNO = T1.SCHREGNO ";
        $query .= "         AND T2.YEAR     = '".CTRL_YEAR."' ";
        $query .= "         AND T2.SEMESTER = '".$model->seme_sch."' ";
        $query .= "         AND T2.GRADE    = '".$model->grade."' ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND T1.SEMESTER     = '".$model->seme."' ";
        $query .= "     AND T1.TESTKINDCD   = '".$model->kind."' ";
        $query .= "     AND T1.TESTITEMCD   = '".$model->item."' ";
        $query .= "     AND T1.SCORE_DIV    = '".$model->sdiv."' ";
        $query .= "     AND T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD = '".$subcd."' ";
        $query .= " GROUP BY ";
        $query .= "     T1.SCORE ";
        $query .= " ORDER BY ";
        $query .= "     T1.SCORE DESC ";
        return $query;
    }

    //削除・追加　RELATIVEASSESS_MST
    public function delInsRelativeAssessMst($db, $model, $subcd, $assArray)
    {
        //科目
        $subArray = array();
        $subArray = explode("-", $subcd);

        //削除　RELATIVEASSESS_MST
        $query  = " DELETE FROM ";
        $query .= "     RELATIVEASSESS_MST ";
        $query .= " WHERE ";
        $query .= "     GRADE = '".$model->grade."' ";
        $query .= "     AND CLASSCD         = '".$subArray[0]."' ";
        $query .= "     AND SCHOOL_KIND     = '".$subArray[1]."' ";
        $query .= "     AND CURRICULUM_CD   = '".$subArray[2]."' ";
        $query .= "     AND SUBCLASSCD      = '".$subArray[3]."' ";
        $query .= "     AND ASSESSCD        = '3' ";
        $db->query($query);

        //追加　RELATIVEASSESS_MST
        foreach ($assArray as $level => $assRow) {
            $data = array();
            $data["GRADE"][TEXT]            = $model->grade;
            $data["CLASSCD"][TEXT]          = $subArray[0];
            $data["SCHOOL_KIND"][TEXT]      = $subArray[1];
            $data["CURRICULUM_CD"][TEXT]    = $subArray[2];
            $data["SUBCLASSCD"][TEXT]       = $subArray[3];
            $data["ASSESSCD"][TEXT]         = "3";
            $data["ASSESSLEVEL"][NUMBER]    = $level;
            $data["ASSESSMARK"][TEXT]       = $level;
            $data["ASSESSLOW"][NUMBER]      = $assRow["ASSESSLOW"];
            $data["ASSESSHIGH"][NUMBER]     = $assRow["ASSESSHIGH"];
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][FUNC]          = "sysdate()";
            $query = Query::insertSQL($data, "RELATIVEASSESS_MST");
            $db->query($query);
        }

        return true;
    }

    //教科別序列--------------------------------------------

    //テーブル存在するか？
    public function existsTable($tbname)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     SYSIBM.SYSCOLUMNS ";
        $query .= " WHERE ";
        $query .= "     TBNAME = '{$tbname}' ";
        return $query;
    }

    //評定平均値・席次データ更新
    public function updRecordRankClassDat($model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //削除
        $query = knjd141cQuery::delRecordRankClassDat($model);
        $db->query($query);

        //教科別評定平均値
        $classDiv = "1";
        $query = knjd141cQuery::getRecordRankClassDat($model, $classDiv);
        knjd141cQuery::insRecordRankClassDat($db, $query, $model, $classDiv);

        //全科目の評定平均値
        $classDiv = "9";
        $query = knjd141cQuery::getRecordRankClassDat($model, $classDiv);
        knjd141cQuery::insRecordRankClassDat($db, $query, $model, $classDiv);

        $db->commit();
        Query::dbCheckIn($db);
    }

    public function delRecordRankClassDat($model)
    {
        $query  = " DELETE FROM ";
        $query .= "      RECORD_RANK_CLASS_SDIV_DAT T1 ";
        $query .= " WHERE ";
        $query .= "      T1.YEAR = '".CTRL_YEAR."' ";
        $query .= "      AND T1.SEMESTER = '".$model->seme."' ";
        $query .= "      AND T1.TESTKINDCD = '".$model->kind."' ";
        $query .= "      AND T1.TESTITEMCD = '".$model->item."' ";
        $query .= "      AND T1.SCORE_DIV = '".$model->sdiv."' ";
        $query .= "      AND T1.SCHOOL_KIND = '".$model->school_kind."' ";
        $query .= "      AND EXISTS( ";
        $query .= "              SELECT ";
        $query .= "                  'x' ";
        $query .= "              FROM ";
        $query .= "                  SCHREG_REGD_DAT E1 ";
        $query .= "              WHERE ";
        $query .= "                  E1.SCHREGNO = T1.SCHREGNO ";
        $query .= "                  AND E1.YEAR = '".CTRL_YEAR."' ";
        $query .= "                  AND E1.SEMESTER = '".$model->seme_sch."' ";
        $query .= "                  AND E1.GRADE = '".$model->grade."' ";
        $query .= "          ) ";

        return $query;
    }

    public function getRecordRankClassDat($model, $classDiv)
    {
        $query  = "";
        $query .= " WITH T_SCHNO AS ( ";
        $query .= "     SELECT ";
        $query .= "         SCHREGNO, ";
        $query .= "         GRADE, ";
        $query .= "         HR_CLASS, ";
        $query .= "         COURSECD, ";
        $query .= "         MAJORCD, ";
        $query .= "         COURSECODE ";
        $query .= "     FROM ";
        $query .= "         SCHREG_REGD_DAT ";
        $query .= "     WHERE ";
        $query .= "         YEAR = '".CTRL_YEAR."' ";
        $query .= "         AND SEMESTER = '".$model->seme_sch."' ";
        $query .= "         AND GRADE = '".$model->grade."' ";
        $query .= " ) ";

        $query .= " , T_RECORD AS ( ";
        $query .= "     SELECT ";
        if ($classDiv == "1") {
            $query .= "         T1.CLASSCD, ";
        } else {
            $query .= "         '00' AS CLASSCD, ";
        }
        $query .= "         T1.SCHOOL_KIND, ";
        $query .= "         T1.SCHREGNO, ";
        $query .= "         T2.GRADE, ";
        $query .= "         T2.HR_CLASS, ";
        $query .= "         T2.COURSECD, ";
        $query .= "         T2.MAJORCD, ";
        $query .= "         T2.COURSECODE, ";
        $query .= "         SUM(T1.SCORE) AS SCORE, ";
        $query .= "         DECIMAL(ROUND(AVG(FLOAT(T1.SCORE))*{$model->AvgDecimalPoint["waru"]},0)/{$model->AvgDecimalPoint["waru"]},{$model->AvgDecimalPoint["allketa"]},{$model->AvgDecimalPoint["keta"]}) AS AVG, ";
        $query .= "         COUNT(T1.SCORE) AS COUNT ";
        $query .= "     FROM ";
        $query .= "         RECORD_RANK_SDIV_DAT T1 ";
        $query .= "         LEFT JOIN T_SCHNO T2 ON T2.SCHREGNO = T1.SCHREGNO ";
        $query .= "     WHERE ";
        $query .= "         T1.YEAR = '".CTRL_YEAR."' ";
        $query .= "         AND T1.SEMESTER = '".$model->seme."' ";
        $query .= "         AND T1.TESTKINDCD = '".$model->kind."' ";
        $query .= "         AND T1.TESTITEMCD = '".$model->item."' ";
        $query .= "         AND T1.SCORE_DIV = '".$model->sdiv."' ";
        $query .= "         AND T1.CLASSCD < '90' ";
        $query .= "         AND T1.SCHOOL_KIND = '".$model->school_kind."' ";
        $query .= "         AND T1.SUBCLASSCD NOT IN ('333333','555555','999999','99999A','99999B') ";
        $query .= "         AND T1.SCHREGNO IN (SELECT SCHREGNO FROM T_SCHNO) ";
        $query .= "         AND T1.SCORE > 0 ";
        $query .= "         AND T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD NOT IN (SELECT W2.NAME1 FROM V_NAME_MST W2 WHERE W2.YEAR = '".CTRL_YEAR."' AND W2.NAMECD1 = 'D065') ";
        $query .= "         AND NOT EXISTS ( ";
        $query .= "             SELECT 'X' FROM SUBCLASS_REPLACE_COMBINED_DAT W0 ";
        $query .= "               WHERE W0.YEAR = '".CTRL_YEAR."' ";
        $query .= "                 AND W0.ATTEND_CLASSCD = T1.CLASSCD ";
        $query .= "                 AND W0.ATTEND_SCHOOL_KIND = T1.SCHOOL_KIND ";
        $query .= "                 AND W0.ATTEND_CURRICULUM_CD = T1.CURRICULUM_CD ";
        $query .= "                 AND W0.ATTEND_SUBCLASSCD = T1.SUBCLASSCD ";
        $query .= "         ) ";
        $query .= "     GROUP BY ";
        if ($classDiv == "1") {
            $query .= "         T1.CLASSCD, ";
        }
        $query .= "         T1.SCHOOL_KIND, ";
        $query .= "         T1.SCHREGNO, ";
        $query .= "         T2.GRADE, ";
        $query .= "         T2.HR_CLASS, ";
        $query .= "         T2.COURSECD, ";
        $query .= "         T2.MAJORCD, ";
        $query .= "         T2.COURSECODE ";
        $query .= " ) ";

        $query .= " SELECT ";
        $query .= "     T1.CLASSCD, ";
        $query .= "     T1.SCHOOL_KIND, ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     T1.SCORE, ";
        $query .= "     T1.AVG, ";
        $query .= "     T1.COUNT, ";

        $query .= "     RANK() OVER(PARTITION BY T1.CLASSCD, T1.SCHOOL_KIND, T1.GRADE ORDER BY T1.SCORE DESC) AS GRADE_SCORE_RANK, ";
        $query .= "     RANK() OVER(PARTITION BY T1.CLASSCD, T1.SCHOOL_KIND, T1.GRADE, T1.HR_CLASS ORDER BY T1.SCORE DESC) AS CLASS_SCORE_RANK, ";
        $query .= "     RANK() OVER(PARTITION BY T1.CLASSCD, T1.SCHOOL_KIND, T1.GRADE, T1.COURSECD, T1.MAJORCD, T1.COURSECODE ORDER BY T1.SCORE DESC) AS COURSE_SCORE_RANK, ";
        $query .= "     RANK() OVER(PARTITION BY T1.CLASSCD, T1.SCHOOL_KIND, T1.GRADE, T1.COURSECD, T1.MAJORCD ORDER BY T1.SCORE DESC) AS MAJOR_SCORE_RANK, ";

        $query .= "     RANK() OVER(PARTITION BY T1.CLASSCD, T1.SCHOOL_KIND, T1.GRADE ORDER BY T1.AVG DESC) AS GRADE_AVG_RANK, ";
        $query .= "     RANK() OVER(PARTITION BY T1.CLASSCD, T1.SCHOOL_KIND, T1.GRADE, T1.HR_CLASS ORDER BY T1.AVG DESC) AS CLASS_AVG_RANK, ";
        $query .= "     RANK() OVER(PARTITION BY T1.CLASSCD, T1.SCHOOL_KIND, T1.GRADE, T1.COURSECD, T1.MAJORCD, T1.COURSECODE ORDER BY T1.AVG DESC) AS COURSE_AVG_RANK, ";
        $query .= "     RANK() OVER(PARTITION BY T1.CLASSCD, T1.SCHOOL_KIND, T1.GRADE, T1.COURSECD, T1.MAJORCD ORDER BY T1.AVG DESC) AS MAJOR_AVG_RANK ";
        $query .= " FROM ";
        $query .= "     T_RECORD T1 ";
        $query .= " ORDER BY ";
        $query .= "     T1.CLASSCD, ";
        $query .= "     T1.SCHOOL_KIND, ";
        $query .= "     T1.AVG DESC ";

        return $query;
    }

    public function insRecordRankClassDat($db, $query, $model, $classDiv)
    {
        $rankDivArray = array("1" => "GRADE", "2" => "CLASS", "3" => "COURSE", "4" => "MAJOR");
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            foreach ($rankDivArray as $key => $val) {
                $data = array();
                $data["YEAR"][TEXT]             = CTRL_YEAR;
                $data["SEMESTER"][TEXT]         = $model->seme;
                $data["TESTKINDCD"][TEXT]       = $model->kind;
                $data["TESTITEMCD"][TEXT]       = $model->item;
                $data["SCORE_DIV"][TEXT]        = $model->sdiv;
                $data["CLASS_DIV"][TEXT]        = $classDiv;
                $data["CLASSCD"][TEXT]          = $row["CLASSCD"];
                $data["SCHOOL_KIND"][TEXT]      = $row["SCHOOL_KIND"];
                $data["SCHREGNO"][TEXT]         = $row["SCHREGNO"];
                $data["RANK_DIV"][TEXT]         = $key;
                $data["SCORE"][NUMBER]          = $row["SCORE"];
                $data["AVG"][NUMBER]            = $row["AVG"];
                $data["COUNT"][NUMBER]          = $row["COUNT"];
                $data["SCORE_RANK"][NUMBER]     = $row[$val."_SCORE_RANK"];
                $data["AVG_RANK"][NUMBER]       = $row[$val."_AVG_RANK"];
                $data["REGISTERCD"][TEXT]       = STAFFCD;
                $data["UPDATED"][FUNC]          = "SYSDATE()";

                $query = Query::insertSQL($data, "RECORD_RANK_CLASS_SDIV_DAT");
                $db->query($query);
            }
        }
        $result->free();
    }

    //評定平均値・平均データ更新
    //評定平均値・席次データから算出
    public function updRecordAvgClassDat($model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //削除
        $query = knjd141cQuery::delRecordAvgClassDat($model);
        $db->query($query);

        //教科別評定平均値（平均値から算出）
        $classDiv = "1";
        $query = knjd141cQuery::getRecordAvgClassDat($model, $classDiv);
        knjd141cQuery::insRecordAvgClassDat($db, $query, $model, $classDiv);

        //全科目の評定平均値（平均値から算出）
        $classDiv = "9";
        $query = knjd141cQuery::getRecordAvgClassDat($model, $classDiv);
        knjd141cQuery::insRecordAvgClassDat($db, $query, $model, $classDiv);

        //全科目の評定平均値（合計値から算出）
        $classDiv = "8";
        $query = knjd141cQuery::getRecordAvgClassDat($model, $classDiv);
        knjd141cQuery::insRecordAvgClassDat($db, $query, $model, $classDiv);

        $db->commit();
        Query::dbCheckIn($db);
    }

    public function delRecordAvgClassDat($model)
    {
        $query  = " DELETE FROM ";
        $query .= "      RECORD_AVERAGE_CLASS_SDIV_DAT T1 ";
        $query .= " WHERE ";
        $query .= "      T1.YEAR = '".CTRL_YEAR."' ";
        $query .= "      AND T1.SEMESTER = '".$model->seme."' ";
        $query .= "      AND T1.TESTKINDCD = '".$model->kind."' ";
        $query .= "      AND T1.TESTITEMCD = '".$model->item."' ";
        $query .= "      AND T1.SCORE_DIV = '".$model->sdiv."' ";
        $query .= "      AND T1.SCHOOL_KIND = '".$model->school_kind."'";
        $query .= "      AND T1.GRADE = '".$model->grade."'";

        return $query;
    }

    public function getRecordAvgClassDat($model, $classDiv)
    {
        $query  = "";
        $query .= " WITH T_SCHNO AS ( ";
        $query .= "     SELECT ";
        $query .= "         SCHREGNO, ";
        $query .= "         GRADE, ";
        $query .= "         HR_CLASS, ";
        $query .= "         COURSECD, ";
        $query .= "         MAJORCD, ";
        $query .= "         COURSECODE ";
        $query .= "     FROM ";
        $query .= "         SCHREG_REGD_DAT ";
        $query .= "     WHERE ";
        $query .= "         YEAR = '".CTRL_YEAR."' ";
        $query .= "         AND SEMESTER = '".$model->seme_sch."' ";
        $query .= "         AND GRADE = '".$model->grade."' ";
        $query .= " ) ";

        $query .= " , T_RECORD AS ( ";
        $query .= "     SELECT ";
        $query .= "         T1.CLASSCD, ";
        $query .= "         T1.SCHOOL_KIND, ";
        $query .= "         T1.SCHREGNO, ";
        $query .= "         T2.GRADE, ";
        $query .= "         T2.HR_CLASS, ";
        $query .= "         T2.COURSECD, ";
        $query .= "         T2.MAJORCD, ";
        $query .= "         T2.COURSECODE, ";
        if ($classDiv == "8") {
            $query .= "         T1.SCORE AS VALUE, ";
        } else {
            $query .= "         T1.AVG AS VALUE, ";
        }
        $query .= "         T1.SCORE, ";
        $query .= "         T1.AVG, ";
        $query .= "         T1.COUNT ";
        $query .= "     FROM ";
        $query .= "         RECORD_RANK_CLASS_SDIV_DAT T1 ";
        $query .= "         LEFT JOIN T_SCHNO T2 ON T2.SCHREGNO = T1.SCHREGNO ";
        $query .= "     WHERE ";
        $query .= "         T1.YEAR = '".CTRL_YEAR."' ";
        $query .= "         AND T1.SEMESTER = '".$model->seme."' ";
        $query .= "         AND T1.TESTKINDCD = '".$model->kind."' ";
        $query .= "         AND T1.TESTITEMCD = '".$model->item."' ";
        $query .= "         AND T1.SCORE_DIV = '".$model->sdiv."' ";
        if ($classDiv == "8") {
            $query .= "         AND T1.CLASS_DIV = '9' ";
        } else {
            $query .= "         AND T1.CLASS_DIV = '{$classDiv}' ";
        }
        $query .= "         AND T1.SCHOOL_KIND = '".$model->school_kind."' ";
        $query .= "         AND T1.SCHREGNO IN (SELECT SCHREGNO FROM T_SCHNO) ";
        $query .= "         AND T1.RANK_DIV = '1' ";
        $query .= " ) ";

        $query .= " SELECT ";
        $query .= "     T1.CLASSCD, ";
        $query .= "     T1.SCHOOL_KIND, ";
        $query .= "     '1' AS AVG_DIV, ";
        $query .= "     T1.GRADE, ";
        $query .= "     '000' AS HR_CLASS, ";
        $query .= "     '0' AS COURSECD, ";
        $query .= "     '000' AS MAJORCD, ";
        $query .= "     '0000' AS COURSECODE, ";
        $query .= "     SUM(T1.VALUE) AS SCORE, ";
        $query .= "     DECIMAL(ROUND(AVG(FLOAT(T1.VALUE))*{$model->AvgDecimalPoint["waru"]},0)/{$model->AvgDecimalPoint["waru"]},{$model->AvgDecimalPoint["allketa"]},{$model->AvgDecimalPoint["keta"]}) AS AVG, ";
        $query .= "     COUNT(T1.VALUE) AS COUNT ";
        $query .= " FROM ";
        $query .= "     T_RECORD T1 ";
        $query .= " GROUP BY ";
        $query .= "     T1.CLASSCD, ";
        $query .= "     T1.SCHOOL_KIND, ";
        $query .= "     T1.GRADE ";

        $query .= " UNION ALL ";
        $query .= " SELECT ";
        $query .= "     T1.CLASSCD, ";
        $query .= "     T1.SCHOOL_KIND, ";
        $query .= "     '2' AS AVG_DIV, ";
        $query .= "     T1.GRADE, ";
        $query .= "     T1.HR_CLASS, ";
        $query .= "     '0' AS COURSECD, ";
        $query .= "     '000' AS MAJORCD, ";
        $query .= "     '0000' AS COURSECODE, ";
        $query .= "     SUM(T1.VALUE) AS SCORE, ";
        $query .= "     DECIMAL(ROUND(AVG(FLOAT(T1.VALUE))*{$model->AvgDecimalPoint["waru"]},0)/{$model->AvgDecimalPoint["waru"]},{$model->AvgDecimalPoint["allketa"]},{$model->AvgDecimalPoint["keta"]}) AS AVG, ";
        $query .= "     COUNT(T1.VALUE) AS COUNT ";
        $query .= " FROM ";
        $query .= "     T_RECORD T1 ";
        $query .= " GROUP BY ";
        $query .= "     T1.CLASSCD, ";
        $query .= "     T1.SCHOOL_KIND, ";
        $query .= "     T1.GRADE, ";
        $query .= "     T1.HR_CLASS ";

        $query .= " UNION ALL ";
        $query .= " SELECT ";
        $query .= "     T1.CLASSCD, ";
        $query .= "     T1.SCHOOL_KIND, ";
        $query .= "     '3' AS AVG_DIV, ";
        $query .= "     T1.GRADE, ";
        $query .= "     '000' AS HR_CLASS, ";
        $query .= "     T1.COURSECD, ";
        $query .= "     T1.MAJORCD, ";
        $query .= "     T1.COURSECODE, ";
        $query .= "     SUM(T1.VALUE) AS SCORE, ";
        $query .= "     DECIMAL(ROUND(AVG(FLOAT(T1.VALUE))*{$model->AvgDecimalPoint["waru"]},0)/{$model->AvgDecimalPoint["waru"]},{$model->AvgDecimalPoint["allketa"]},{$model->AvgDecimalPoint["keta"]}) AS AVG, ";
        $query .= "     COUNT(T1.VALUE) AS COUNT ";
        $query .= " FROM ";
        $query .= "     T_RECORD T1 ";
        $query .= " GROUP BY ";
        $query .= "     T1.CLASSCD, ";
        $query .= "     T1.SCHOOL_KIND, ";
        $query .= "     T1.GRADE, ";
        $query .= "     T1.COURSECD, ";
        $query .= "     T1.MAJORCD, ";
        $query .= "     T1.COURSECODE ";

        $query .= " UNION ALL ";
        $query .= " SELECT ";
        $query .= "     T1.CLASSCD, ";
        $query .= "     T1.SCHOOL_KIND, ";
        $query .= "     '4' AS AVG_DIV, ";
        $query .= "     T1.GRADE, ";
        $query .= "     '000' AS HR_CLASS, ";
        $query .= "     T1.COURSECD, ";
        $query .= "     T1.MAJORCD, ";
        $query .= "     '0000' AS COURSECODE, ";
        $query .= "     SUM(T1.VALUE) AS SCORE, ";
        $query .= "     DECIMAL(ROUND(AVG(FLOAT(T1.VALUE))*{$model->AvgDecimalPoint["waru"]},0)/{$model->AvgDecimalPoint["waru"]},{$model->AvgDecimalPoint["allketa"]},{$model->AvgDecimalPoint["keta"]}) AS AVG, ";
        $query .= "     COUNT(T1.VALUE) AS COUNT ";
        $query .= " FROM ";
        $query .= "     T_RECORD T1 ";
        $query .= " GROUP BY ";
        $query .= "     T1.CLASSCD, ";
        $query .= "     T1.SCHOOL_KIND, ";
        $query .= "     T1.GRADE, ";
        $query .= "     T1.COURSECD, ";
        $query .= "     T1.MAJORCD ";

        $query .= " ORDER BY ";
        $query .= "     CLASSCD, ";
        $query .= "     SCHOOL_KIND, ";
        $query .= "     AVG_DIV ";

        return $query;
    }

    public function insRecordAvgClassDat($db, $query, $model, $classDiv)
    {
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $data = array();
            $data["YEAR"][TEXT]             = CTRL_YEAR;
            $data["SEMESTER"][TEXT]         = $model->seme;
            $data["TESTKINDCD"][TEXT]       = $model->kind;
            $data["TESTITEMCD"][TEXT]       = $model->item;
            $data["SCORE_DIV"][TEXT]        = $model->sdiv;
            $data["CLASS_DIV"][TEXT]        = $classDiv;
            $data["CLASSCD"][TEXT]          = $row["CLASSCD"];
            $data["SCHOOL_KIND"][TEXT]      = $row["SCHOOL_KIND"];
            $data["AVG_DIV"][TEXT]          = $row["AVG_DIV"];
            $data["GRADE"][TEXT]            = $row["GRADE"];
            $data["HR_CLASS"][TEXT]         = $row["HR_CLASS"];
            $data["COURSECD"][TEXT]         = $row["COURSECD"];
            $data["MAJORCD"][TEXT]          = $row["MAJORCD"];
            $data["COURSECODE"][TEXT]       = $row["COURSECODE"];
            $data["SCORE"][NUMBER]          = $row["SCORE"];
            $data["AVG"][NUMBER]            = $row["AVG"];
            $data["COUNT"][NUMBER]          = $row["COUNT"];
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][FUNC]          = "SYSDATE()";

            $query = Query::insertSQL($data, "RECORD_AVERAGE_CLASS_SDIV_DAT");
            $db->query($query);
        }
        $result->free();
    }

    //基準点設定　桜花学園から
    public function updStandardScoreDat($model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //削除
        $query = knjd141cQuery::delStandardScoreDat($model);
        $db->query($query);

        //追加
        $query = knjd141cQuery::getStandardScoreDat($model);
        knjd141cQuery::insStandardScoreDat($db, $query, $model);

        $db->commit();
        Query::dbCheckIn($db);
    }

    public function delStandardScoreDat($model)
    {
        $query  = " DELETE FROM ";
        $query .= "      STANDARD_SCORE_DAT T1 ";
        $query .= " WHERE ";
        $query .= "      T1.YEAR = '".CTRL_YEAR."' ";
        $query .= "      AND T1.SEMESTER = '".$model->seme."' ";
        $query .= "      AND T1.TESTKINDCD = '".$model->kind."' ";
        $query .= "      AND T1.TESTITEMCD = '".$model->item."' ";
        $query .= "      AND T1.SCORE_DIV = '".$model->sdiv."' ";
        $query .= "      AND T1.GRADE = '".$model->grade."'";
        //処理科目(各科目のみ)
        if ($model->subclasscd != '000000' && $model->subclasscd != '999999') {
            $query .= "      AND T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD = '".$model->subclasscd."' ";
        }

        return $query;
    }

    public function getStandardScoreDat($model)
    {
        $query  = "";
        $query .= " SELECT ";
        $query .= "     T1.CLASSCD, ";
        $query .= "     T1.SCHOOL_KIND, ";
        $query .= "     T1.CURRICULUM_CD, ";
        $query .= "     T1.SUBCLASSCD, ";
        $query .= "     T2.COURSECD, ";
        $query .= "     T2.MAJORCD, ";
        $query .= "     T2.COURSECODE, ";
        $query .= "     ROUND(AVG(FLOAT(T1.SCORE)), 1) * 0.6 AS STANDARD_SCORE ";
        $query .= " FROM ";
        $query .= "     RECORD_SCORE_DAT T1 ";
        $query .= "     INNER JOIN CLASS_YDAT CYDAT ON CYDAT.YEAR = T1.YEAR ";
        $query .= "         AND CYDAT.CLASSCD = T1.CLASSCD ";
        $query .= "         AND CYDAT.SCHOOL_KIND = T1.SCHOOL_KIND ";
        $query .= "     INNER JOIN SUBCLASS_YDAT SYDAT ON SYDAT.YEAR = T1.YEAR ";
        $query .= "         AND SYDAT.CLASSCD = T1.CLASSCD ";
        $query .= "         AND SYDAT.SCHOOL_KIND = T1.SCHOOL_KIND ";
        $query .= "         AND SYDAT.CURRICULUM_CD = T1.CURRICULUM_CD ";
        $query .= "         AND SYDAT.SUBCLASSCD = T1.SUBCLASSCD ";
        $query .= "     INNER JOIN SCHREG_REGD_DAT T2 ON T2.SCHREGNO = T1.SCHREGNO ";
        $query .= "         AND T2.YEAR = '".CTRL_YEAR."' ";
        $query .= "         AND T2.SEMESTER = '".$model->seme_sch."' ";
        $query .= "         AND T2.GRADE = '".$model->grade."' ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND T1.SEMESTER = '".$model->seme."' ";
        $query .= "     AND T1.TESTKINDCD = '".$model->kind."' ";
        $query .= "     AND T1.TESTITEMCD = '".$model->item."' ";
        $query .= "     AND T1.SCORE_DIV = '".$model->sdiv."' ";
        //処理科目(各科目のみ)
        if ($model->subclasscd != '000000' && $model->subclasscd != '999999') {
            $query .= "      AND T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD = '".$model->subclasscd."' ";
        }
        $query .= "     AND T1.SCORE IS NOT NULL ";
        $query .= " GROUP BY ";
        $query .= "     T1.CLASSCD, ";
        $query .= "     T1.SCHOOL_KIND, ";
        $query .= "     T1.CURRICULUM_CD, ";
        $query .= "     T1.SUBCLASSCD, ";
        $query .= "     T2.COURSECD, ";
        $query .= "     T2.MAJORCD, ";
        $query .= "     T2.COURSECODE ";

        return $query;
    }

    public function insStandardScoreDat($db, $query, $model)
    {
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $data = array();
            $data["YEAR"][TEXT]             = CTRL_YEAR;
            $data["SEMESTER"][TEXT]         = $model->seme;
            $data["TESTKINDCD"][TEXT]       = $model->kind;
            $data["TESTITEMCD"][TEXT]       = $model->item;
            $data["SCORE_DIV"][TEXT]        = $model->sdiv;
            $data["CLASSCD"][TEXT]          = $row["CLASSCD"];
            $data["SCHOOL_KIND"][TEXT]      = $row["SCHOOL_KIND"];
            $data["CURRICULUM_CD"][TEXT]    = $row["CURRICULUM_CD"];
            $data["SUBCLASSCD"][TEXT]       = $row["SUBCLASSCD"];
            $data["COURSECD"][TEXT]         = $row["COURSECD"];
            $data["MAJORCD"][TEXT]          = $row["MAJORCD"];
            $data["COURSECODE"][TEXT]       = $row["COURSECODE"];
            $data["GRADE"][TEXT]            = $model->grade;
            $data["STANDARD_SCORE"][NUMBER] = ceil($row["STANDARD_SCORE"]);
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][FUNC]          = "SYSDATE()";

            $query = Query::insertSQL($data, "STANDARD_SCORE_DAT");
            $db->query($query);
        }
        $result->free();
    }

    //換算点算出処理　青山学院高等部
    public function updRecordScoreKansan($model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //(学期別・学年末)換算点
        //追加・更新
        $query = knjd141cQuery::getRecordScoreKansanDat($model);
        //echo "RECORD_SCORE_KANSAN_DAT"."<BR>";
        //echo $query."<BR>";
        knjd141cQuery::insUpdRecordScoreKansanDat($db, $query, $model);

        //(通算)換算点
        //追加・更新
        $query = knjd141cQuery::getRecordScoreKansanTotalDat($model);
        //echo "RECORD_SCORE_KANSAN_TOTAL_DAT"."<BR>";
        //echo $query."<BR>";
        knjd141cQuery::insUpdRecordScoreKansanTotalDat($db, $query, $model);

        $db->commit();
        Query::dbCheckIn($db);
    }

    public function getRecordScoreKansanDat($model)
    {
        $query  = "";
        $query .= " WITH T_SCHNO AS ( ";
        $query .= "     SELECT ";
        $query .= "         SCHREGNO, ";
        $query .= "         GRADE, ";
        $query .= "         COURSECD, ";
        $query .= "         MAJORCD, ";
        $query .= "         COURSECODE ";
        $query .= "     FROM ";
        $query .= "         SCHREG_REGD_DAT ";
        $query .= "     WHERE ";
        $query .= "         YEAR = '".CTRL_YEAR."' ";
        $query .= "         AND SEMESTER = '".$model->seme_sch."' ";
        $query .= "         AND GRADE = '".$model->grade."' ";
        $query .= " ), T_KANSAN AS ( ";
        $query .= "     SELECT ";
        $query .= "         T1.SCHREGNO, ";
        $query .= "         SUM(CASE WHEN T1.SCORE >= 4 THEN T1.SCORE * C1.CREDITS ELSE 0 END) AS KANSAN_SCORE ";
        $query .= "     FROM ";
        $query .= "         RECORD_SCORE_DAT T1 ";
        $query .= "         INNER JOIN T_SCHNO T2 ON T2.SCHREGNO = T1.SCHREGNO ";
        $query .= "         INNER JOIN CLASS_YDAT CYDAT ON CYDAT.YEAR = T1.YEAR ";
        $query .= "             AND CYDAT.CLASSCD = T1.CLASSCD ";
        $query .= "             AND CYDAT.SCHOOL_KIND = T1.SCHOOL_KIND ";
        $query .= "         INNER JOIN SUBCLASS_YDAT SYDAT ON SYDAT.YEAR = T1.YEAR ";
        $query .= "             AND SYDAT.CLASSCD = T1.CLASSCD ";
        $query .= "             AND SYDAT.SCHOOL_KIND = T1.SCHOOL_KIND ";
        $query .= "             AND SYDAT.CURRICULUM_CD = T1.CURRICULUM_CD ";
        $query .= "             AND SYDAT.SUBCLASSCD = T1.SUBCLASSCD ";
        $query .= "         LEFT JOIN CREDIT_MST C1 ON C1.YEAR = T1.YEAR ";
        $query .= "             AND C1.COURSECD = T2.COURSECD ";
        $query .= "             AND C1.MAJORCD = T2.MAJORCD ";
        $query .= "             AND C1.GRADE = T2.GRADE ";
        $query .= "             AND C1.COURSECODE = T2.COURSECODE ";
        $query .= "             AND C1.CLASSCD = T1.CLASSCD ";
        $query .= "             AND C1.SCHOOL_KIND = T1.SCHOOL_KIND ";
        $query .= "             AND C1.CURRICULUM_CD = T1.CURRICULUM_CD ";
        $query .= "             AND C1.SUBCLASSCD = T1.SUBCLASSCD ";
        $query .= "     WHERE ";
        $query .= "         T1.YEAR = '".CTRL_YEAR."' ";
        $query .= "         AND T1.SEMESTER = '".$model->seme."' ";
        $query .= "         AND T1.TESTKINDCD = '".$model->kind."' ";
        $query .= "         AND T1.TESTITEMCD = '".$model->item."' ";
        $query .= "         AND T1.SCORE_DIV = '".$model->sdiv."' ";
        $query .= "         AND T1.SCORE IS NOT NULL ";
        $query .= "     GROUP BY ";
        $query .= "         T1.SCHREGNO ";
        $query .= " ), T_RANK AS ( ";
        $query .= "     SELECT ";
        $query .= "         T1.SCHREGNO, ";
        $query .= "         RANK() OVER(ORDER BY T1.KANSAN_SCORE DESC) AS RANK ";
        $query .= "     FROM ";
        $query .= "         T_KANSAN T1 ";
        $query .= "     WHERE ";
        $query .= "         T1.KANSAN_SCORE IS NOT NULL ";
        $query .= " ) ";

        $query .= " SELECT ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     T1.KANSAN_SCORE, ";
        $query .= "     T2.RANK, ";
        $query .= "     CASE WHEN K1.SCHREGNO IS NOT NULL THEN 1 ELSE 0 END AS RECORD_CNT ";
        $query .= " FROM ";
        $query .= "     T_KANSAN T1 ";
        $query .= "     LEFT JOIN T_RANK T2 ON T2.SCHREGNO = T1.SCHREGNO ";
        $query .= "     LEFT JOIN RECORD_SCORE_KANSAN_DAT K1 ON K1.YEAR = '".CTRL_YEAR."' ";
        $query .= "         AND K1.GRADE = '".$model->grade."' ";
        $query .= "         AND K1.SEMESTER = '".$model->seme."' ";
        $query .= "         AND K1.SCHREGNO = T1.SCHREGNO ";

        return $query;
    }

    public function getRecordScoreKansanTotalDat($model)
    {
        $query  = "";
        $query .= " WITH T_SCHNO AS ( ";
        $query .= "     SELECT ";
        $query .= "         SCHREGNO, ";
        $query .= "         GRADE, ";
        $query .= "         COURSECD, ";
        $query .= "         MAJORCD, ";
        $query .= "         COURSECODE ";
        $query .= "     FROM ";
        $query .= "         SCHREG_REGD_DAT ";
        $query .= "     WHERE ";
        $query .= "         YEAR = '".CTRL_YEAR."' ";
        $query .= "         AND SEMESTER = '".$model->seme_sch."' ";
        $query .= "         AND GRADE = '".$model->grade."' ";
        $query .= " ), T_KANSAN_ALL AS ( ";
        //(今回)換算点
        $query .= "     SELECT ";
        $query .= "         T1.SCHREGNO, ";
        $query .= "         T1.KANSAN_SCORE ";
        $query .= "     FROM ";
        $query .= "         RECORD_SCORE_KANSAN_DAT T1 ";
        $query .= "         INNER JOIN T_SCHNO T2 ON T2.SCHREGNO = T1.SCHREGNO ";
        $query .= "     WHERE ";
        $query .= "         T1.YEAR = '".CTRL_YEAR."' ";
        $query .= "         AND T1.GRADE = '".$model->grade."' ";
        $query .= "         AND T1.SEMESTER = '".$model->seme."' ";
        $query .= "         AND T1.KANSAN_SCORE IS NOT NULL ";
        //(過去)学年末換算点
        $query .= "     UNION ALL ";
        $query .= "     SELECT ";
        $query .= "         T1.SCHREGNO, ";
        $query .= "         SUM(T1.KANSAN_SCORE) AS KANSAN_SCORE ";
        $query .= "     FROM ";
        $query .= "         RECORD_SCORE_KANSAN_DAT T1 ";
        $query .= "         INNER JOIN T_SCHNO T2 ON T2.SCHREGNO = T1.SCHREGNO ";
        $query .= "     WHERE ";
        $query .= "         T1.YEAR < '".CTRL_YEAR."' ";
        $query .= "         AND T1.GRADE < '".$model->grade."' ";
        $query .= "         AND T1.SEMESTER = '9' ";
        $query .= "         AND T1.KANSAN_SCORE IS NOT NULL ";
        $query .= "     GROUP BY ";
        $query .= "         T1.SCHREGNO ";
        $query .= " ), T_KANSAN AS ( ";
        $query .= "     SELECT ";
        $query .= "         T1.SCHREGNO, ";
        $query .= "         SUM(T1.KANSAN_SCORE) AS KANSAN_SCORE ";
        $query .= "     FROM ";
        $query .= "         T_KANSAN_ALL T1 ";
        $query .= "     GROUP BY ";
        $query .= "         T1.SCHREGNO ";
        $query .= " ), T_RANK AS ( ";
        $query .= "     SELECT ";
        $query .= "         T1.SCHREGNO, ";
        $query .= "         RANK() OVER(ORDER BY T1.KANSAN_SCORE DESC) AS RANK ";
        $query .= "     FROM ";
        $query .= "         T_KANSAN T1 ";
        $query .= "     WHERE ";
        $query .= "         T1.KANSAN_SCORE IS NOT NULL ";
        $query .= " ) ";

        $query .= " SELECT ";
        $query .= "     T1.SCHREGNO, ";
        $query .= "     T1.KANSAN_SCORE, ";
        $query .= "     T2.RANK, ";
        $query .= "     CASE WHEN K1.SCHREGNO IS NOT NULL THEN 1 ELSE 0 END AS RECORD_CNT ";
        $query .= " FROM ";
        $query .= "     T_KANSAN T1 ";
        $query .= "     LEFT JOIN T_RANK T2 ON T2.SCHREGNO = T1.SCHREGNO ";
        $query .= "     LEFT JOIN RECORD_SCORE_KANSAN_TOTAL_DAT K1 ON K1.YEAR = '".CTRL_YEAR."' ";
        $query .= "         AND K1.GRADE = '".$model->grade."' ";
        $query .= "         AND K1.SCHREGNO = T1.SCHREGNO ";

        return $query;
    }

    public function insUpdRecordScoreKansanDat($db, $query, $model)
    {
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $data = array();
            $data["YEAR"][TEXT]             = CTRL_YEAR;
            $data["GRADE"][TEXT]            = $model->grade;
            $data["SEMESTER"][TEXT]         = $model->seme;
            $data["SCHREGNO"][TEXT]         = $row["SCHREGNO"];
            $data["KANSAN_SCORE"][NUMBER]   = $row["KANSAN_SCORE"];
            $data["RANK"][NUMBER]           = $row["RANK"];
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][FUNC]          = "SYSDATE()";
            if ($row["RECORD_CNT"] == 0) {
                $query = Query::insertSQL($data, "RECORD_SCORE_KANSAN_DAT");
            } else {
                //where
                $where  = " WHERE ";
                $where .= "      YEAR = '".CTRL_YEAR."' ";
                $where .= "      AND GRADE = '".$model->grade."'";
                $where .= "      AND SEMESTER = '".$model->seme."' ";
                $where .= "      AND SCHREGNO = '".$row["SCHREGNO"]."'";
                $query = Query::updateSQL($data, "RECORD_SCORE_KANSAN_DAT", $where);
            }
            $db->query($query);
        }
        $result->free();
    }

    public function insUpdRecordScoreKansanTotalDat($db, $query, $model)
    {
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $data = array();
            $data["YEAR"][TEXT]             = CTRL_YEAR;
            $data["GRADE"][TEXT]            = $model->grade;
            $data["SCHREGNO"][TEXT]         = $row["SCHREGNO"];
            $data["KANSAN_SCORE"][NUMBER]   = $row["KANSAN_SCORE"];
            $data["RANK"][NUMBER]           = $row["RANK"];
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][FUNC]          = "SYSDATE()";
            if ($row["RECORD_CNT"] == 0) {
                $query = Query::insertSQL($data, "RECORD_SCORE_KANSAN_TOTAL_DAT");
            } else {
                //where
                $where  = " WHERE ";
                $where .= "      YEAR = '".CTRL_YEAR."' ";
                $where .= "      AND GRADE = '".$model->grade."'";
                $where .= "      AND SCHREGNO = '".$row["SCHREGNO"]."'";
                $query = Query::updateSQL($data, "RECORD_SCORE_KANSAN_TOTAL_DAT", $where);
            }
            $db->query($query);
        }
        $result->free();
    }
}
