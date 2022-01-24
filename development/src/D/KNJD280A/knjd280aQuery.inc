<?php

require_once('for_php7.php');

class knjd280aQuery extends Query {

    //年組取得（権限チェック）
    function getClass($model)
    {
        //参照・更新可
        if (AUTHORITY == DEF_REFERABLE || AUTHORITY == DEF_UPDATABLE){
            $query  = " SELECT DISTINCT ";
            $query .= "     GRADE || HR_CLASS AS VALUE, ";
            $query .= "     HR_NAME AS LABEL ";
            $query .= " FROM ";
            $query .= "     SCHREG_REGD_HDAT ";
            $query .= " WHERE ";
            $query .= "     YEAR = '".CTRL_YEAR."' ";
            $query .= "     AND SEMESTER = '".CTRL_SEMESTER."' ";
            $query .= " ORDER BY ";
            $query .= "     VALUE ";
        }
        //参照・更新可（制限付き）
        if (AUTHORITY == DEF_REFER_RESTRICT || AUTHORITY == DEF_UPDATE_RESTRICT){
            $query  = " SELECT DISTINCT ";
            $query .= "     GRADE || HR_CLASS AS VALUE, ";
            $query .= "     HR_NAME AS LABEL ";
            $query .= " FROM ";
            $query .= "     SCHREG_REGD_HDAT ";
            $query .= " WHERE ";
            $query .= "     YEAR = '".CTRL_YEAR."' ";
            $query .= "     AND SEMESTER = '".CTRL_SEMESTER."' ";
            $query .= "     AND (TR_CD1 = '" .STAFFCD ."' ";
            $query .= "          OR TR_CD2 = '" .STAFFCD ."' ";
            $query .= "          OR TR_CD3 = '" .STAFFCD ."') ";
            $query .= " ORDER BY ";
            $query .= "     VALUE ";
        }

        return $query;
    }

    //年組取得（権限チェック）
    function getHrName($db, $classSelected)
    {
        $classSelectedSql = "'".implode("','", explode(",", $classSelected))."'";

        $query  = " SELECT ";
        $query .= "     HR_NAME ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_HDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND SEMESTER = '".CTRL_SEMESTER."' ";
        $query .= "     AND GRADE || HR_CLASS IN (".$classSelectedSql.") ";
        $query .= " ORDER BY ";
        $query .= "     GRADE || HR_CLASS ";

        return implode("<br>", $db->getCol($query));
    }


    //学期
    function getSemester()
    {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     SEMESTER_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= " ORDER BY ";
        $query .= "     SEMESTER ";

        return $query;
    }

    //名称マスタ
    function getM006()
    {
        $query  = " SELECT ";
        $query .= "     NAMECD2, NAMESPARE1, NAMESPARE2, NAMESPARE3 "; // コースごと、科目ごとに満点を設定する
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND NAMECD1 = 'M006' ";

        return $query;
    }

    /* 実行履歴 */
    function executeRireki($model) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //実行日付・時間を取得
        $calcRow = $db->getRow(knjd280aQuery::getCalcDateTime(), DB_FETCHMODE_ASSOC);
        $calcDate = $calcRow["CALC_DATE"];//実行日付
        $calcTime = $calcRow["CALC_TIME"];//実行時間
        //実行履歴データ・追加
        knjd280aQuery::getInsertRireki($db, $calcDate, $calcTime, $model);

        $db->commit();
        Query::dbCheckIn($db);
        return true;
    }
    //実行日付・時間を取得
    function getCalcDateTime() {
        $query  = " with t_date_time (CALC_DATE,CALC_TIME) as ( ";
        $query .= " values( ";
        $query .= "     date(sysdate()), ";
        $query .= "     time(sysdate()) ";
        $query .= " )) ";
        $query .= "  ";
        $query .= " select * from t_date_time ";
        return $query;
    }
    //履歴一覧
    function getListRireki($model) {
        $year = CTRL_YEAR;
        $semester = CTRL_SEMESTER;
        $query  = " SELECT ";
        $query .= "     T1.*, ";
        $query .= "     L1.STAFFNAME ";
        $query .= " FROM ";
        $query .= "     RECORD_APPROVE_CREDIT_EXEC_DAT T1 ";
        $query .= "     LEFT JOIN STAFF_MST L1 ON L1.STAFFCD = T1.REGISTERCD ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '{$year}' ";
        $query .= "     AND T1.SEMESTER = '{$semester}' ";
        $query .= " ORDER BY ";
        $query .= "     T1.CALC_DATE DESC, ";
        $query .= "     T1.CALC_TIME DESC ";
        return $query;
    }
    //実行履歴データ・追加
    function getInsertRireki($db, $calcDate, $calcTime, $model) {
        $data = array();
        $data["CALC_DATE"][TEXT]              = $calcDate;
        $data["CALC_TIME"][TEXT]              = $calcTime;
        $data["YEAR"][TEXT]                   = CTRL_YEAR;
        $data["SEMESTER"][TEXT]               = CTRL_SEMESTER;
        $data["SELECT_HR_CLASS"][TEXT]        = implode(",", $model->field["CLASS_SELECTED"]);
        $data["ATTEND_CALC_DATE"][TEXT]       = str_replace('/', '-', $model->field["DATE"]);
        $data["APPROVE_FLG"][TEXT]            = $model->field["NINTEI"];
        $data["APPROVE_SEMESTER1_FLG"][TEXT]  = $model->field["NINTEI_SEME1"];
        $data["APPROVE_SEMESTER2_FLG"][TEXT]  = $model->field["NINTEI_SEME2"];
        $data["SET_PROV_FLG"][TEXT]           = $model->field["KARI"];
        $data["SET_PROV_SEMESTER1_FLG"][TEXT] = $model->field["KARI_SEME1"];
        $data["SET_PROV_SEMESTER2_FLG"][TEXT] = $model->field["KARI_SEME2"];
        $data["REGISTERCD"][TEXT]             = STAFFCD;
        $data["UPDATED"][FUNC]                = "sysdate()";

        $query = Query::insertSQL($data, "RECORD_APPROVE_CREDIT_EXEC_DAT");
        $db->query($query);
    }

    function execute($model) {
        $this->attendInfo = array();

        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        // 平常点算出
        knjd280aQuery::updateHeijo($db, $model);

        // 出席点算出
        knjd280aQuery::updateAttend($db, $model);

        // 学期評価算出
        knjd280aQuery::updateGakkiHyoka($db, $model);

        // 単位認定 & 仮評定設定
        if ("1" == $this->field["NINTEI"]) {
            knjd280aQuery::nintei($db, $model);
        }

        $db->commit();
        Query::dbCheckIn($db);
        $model->setMessage("MSG201");
    }

    function deleteRecordScoreDatSql($semester, $scoreDiv, $model, $takeSemesFlg = '') {
        $query = "";
        $query .= " DELETE FROM RECORD_SCORE_DAT T1 ";
        $query .= "   WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND SEMESTER ='".$semester."' ";
        $query .= "     AND TESTKINDCD = '99' ";
        $query .= "     AND TESTITEMCD = '00' ";
        $query .= "     AND SCORE_DIV = '{$scoreDiv}' ";
        if ($takeSemesFlg) {
            $query .= "     AND (T1.SCHREGNO, T1.CLASSCD, T1.SCHOOL_KIND, T1.CURRICULUM_CD, T1.SUBCLASSCD) IN (SELECT DISTINCT ";
            $query .= "                     I1.SCHREGNO ";
            $query .= "                     , I3.CLASSCD ";
            $query .= "                     , I3.SCHOOL_KIND ";
            $query .= "                     , I3.CURRICULUM_CD ";
            $query .= "                     , I3.SUBCLASSCD ";
            $query .= "                 FROM SCHREG_REGD_DAT I1 ";
            $query .= "                 LEFT JOIN CHAIR_STD_DAT I2 ON I2.YEAR = '".CTRL_YEAR."' ";
            $query .= "                     AND I2.SEMESTER <= '".CTRL_SEMESTER."' ";
            $query .= "                     AND I2.SCHREGNO = I1.SCHREGNO ";
            $query .= "                 LEFT JOIN CHAIR_DAT I3 ON I3.YEAR = '".CTRL_YEAR."' ";
            $query .= "                     AND I3.SEMESTER = I2.SEMESTER ";
            $query .= "                     AND I3.CHAIRCD = I2.CHAIRCD ";
            $query .= "                     AND I3.CLASSCD = T1.CLASSCD ";
            $query .= "                     AND I3.SCHOOL_KIND = T1.SCHOOL_KIND ";
            $query .= "                     AND I3.CURRICULUM_CD = T1.CURRICULUM_CD ";
            $query .= "                     AND I3.SUBCLASSCD = T1.SUBCLASSCD ";
        } else {
            $query .= "     AND (T1.SCHREGNO) IN (SELECT DISTINCT ";
            $query .= "                     I1.SCHREGNO ";
            $query .= "                 FROM SCHREG_REGD_DAT I1 ";
        }
        $query .= "                 WHERE ";
        $query .= "                     T1.SCHREGNO = I1.SCHREGNO ";
        $query .= "                     AND T1.YEAR = I1.YEAR ";
        $query .= "                     AND I1.SEMESTER = '".CTRL_SEMESTER."' ";
        $query .= "                     AND I1.GRADE || I1.HR_CLASS IN ('".implode("','", $model->field["CLASS_SELECTED"])."') ";
        if ($takeSemesFlg) {
            if ($takeSemesFlg == '1') {
                $query .= "                 AND I3.TAKESEMES = '1' ";
            } else if ($takeSemesFlg == '2') {
                $query .= "                 AND VALUE(I3.TAKESEMES, '0') <> '1' ";
            }
        }
        $query .= "                ) ";
        return $query;
    }

    function insertRecordScoreDatSql($db, $semester, $scoreDiv, $subclasscd_, $schregno, $score, $credit = '') {
        list ($classcd, $schoolKind, $curriculumCd, $subclasscd) = explode("-", $subclasscd_);
        $sql  = " SELECT YEAR, SCORE FROM RECORD_SCORE_DAT WHERE ";
        $sql .= " YEAR = '".CTRL_YEAR."' ";
        $sql .= " AND SEMESTER = '".$semester."' ";
        $sql .= " AND TESTKINDCD = '99' ";
        $sql .= " AND TESTITEMCD = '00' ";
        $sql .= " AND CLASSCD = '".$classcd."' ";
        $sql .= " AND SCHOOL_KIND= '".$schoolKind."' ";
        $sql .= " AND CURRICULUM_CD = '".$curriculumCd."' ";
        $sql .= " AND SUBCLASSCD = '".$subclasscd."' ";
        $sql .= " AND SCHREGNO = '".$schregno."' ";
        $sql .= " AND SCORE_DIV = '".$scoreDiv."' ";
        $rec = $db->getRow($sql);
        if ($rec) {
            //echo " insert ".$semester.", ".$scoreDiv.", ".$subclasscd_.", ".$schregno.", ".$score." / ".$rec["SCORE"]." delete.<br>";
            $sql  = " DELETE FROM RECORD_SCORE_DAT WHERE ";
            $sql .= " YEAR = '".CTRL_YEAR."' ";
            $sql .= " AND SEMESTER = '".$semester."' ";
            $sql .= " AND TESTKINDCD = '99' ";
            $sql .= " AND TESTITEMCD = '00' ";
            $sql .= " AND CLASSCD = '".$classcd."' ";
            $sql .= " AND SCHOOL_KIND= '".$schoolKind."' ";
            $sql .= " AND CURRICULUM_CD = '".$curriculumCd."' ";
            $sql .= " AND SUBCLASSCD = '".$subclasscd."' ";
            $sql .= " AND SCHREGNO = '".$schregno."' ";
            $sql .= " AND SCORE_DIV = '".$scoreDiv."' ";
            $db->query($sql);
        }

        $data = array();
        $data["YEAR"][TEXT] = CTRL_YEAR;
        $data["SEMESTER"][TEXT] = $semester;
        $data["TESTKINDCD"][TEXT] = "99";
        $data["TESTITEMCD"][TEXT] = "00";
        $data["CLASSCD"][TEXT] = $classcd;
        $data["SCHOOL_KIND"][TEXT] = $schoolKind;
        $data["CURRICULUM_CD"][TEXT] = $curriculumCd;
        $data["SUBCLASSCD"][TEXT] = $subclasscd;
        $data["SCHREGNO"][TEXT] = $schregno;
        $data["SCORE_DIV"][TEXT] = $scoreDiv;
        if ($score !== '') {
            $data["SCORE"][NUMBER] = $score;
        }
        if ($credit !== '') {
            $data["GET_CREDIT"][NUMBER] = $credit;
            $data["COMP_CREDIT"][NUMBER] = $credit;
        }
        $data["REGISTERCD"][TEXT] = STAFFCD;
        $data["UPDATED"][FUNC] = "sysdate()";

        $query = Query::insertSQL($data, "RECORD_SCORE_DAT");
        $db->query($query);
        return;
    }

    function deleteRecordProvFlgDatSql($model, $takeSemesFlg) {
        $query = "";
        $query .= " DELETE FROM RECORD_PROV_FLG_DAT T1 ";
        $query .= "   WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND (SCHREGNO, CLASSCD, SCHOOL_KIND, CURRICULUM_CD, SUBCLASSCD) IN ";
        $query .= "         (SELECT T1.SCHREGNO, I3.CLASSCD, I3.SCHOOL_KIND, I3.CURRICULUM_CD, I3.SUBCLASSCD ";
        $query .= "                 FROM SCHREG_REGD_DAT I1 ";
        $query .= "                 LEFT JOIN CHAIR_STD_DAT I2 ON I2.YEAR = T1.YEAR ";
        $query .= "                     AND I2.SEMESTER <= I1.SEMESTER ";
        $query .= "                     AND I2.SCHREGNO = I1.SCHREGNO ";
        $query .= "                 LEFT JOIN CHAIR_DAT I3 ON I3.YEAR = I2.YEAR ";
        $query .= "                     AND I3.SEMESTER = I2.SEMESTER ";
        $query .= "                     AND I3.CHAIRCD = I2.CHAIRCD ";
        $query .= "                     AND I3.CLASSCD = T1.CLASSCD ";
        $query .= "                     AND I3.SCHOOL_KIND = T1.SCHOOL_KIND ";
        $query .= "                     AND I3.CURRICULUM_CD = T1.CURRICULUM_CD ";
        $query .= "                     AND I3.SUBCLASSCD = T1.SUBCLASSCD ";
        $query .= "                 WHERE ";
        $query .= "                     T1.SCHREGNO = I1.SCHREGNO ";
        $query .= "                     AND T1.YEAR = I1.YEAR ";
        $query .= "                     AND I1.SEMESTER = '".CTRL_SEMESTER."' ";
        $query .= "                     AND I1.GRADE || I1.HR_CLASS IN ('".implode("','", $model->field["CLASS_SELECTED"])."') ";
        if ($takeSemesFlg == '1') {
            $query .= "                 AND I3.TAKESEMES = '1' ";
        } else if ($takeSemesFlg == '2') {
            $query .= "                 AND VALUE(I3.TAKESEMES, '0') <> '1' ";
        }
        $query .= "                ) ";
        return $query;
    }

     function insertRecordProvFlgDatSql($subclasscd_, $schregno, $provSemester) {
        list ($classcd, $schoolKind, $curriculumCd, $subclasscd) = explode("-", $subclasscd_);
        $data = array();
        $data["YEAR"][TEXT] = CTRL_YEAR;
        $data["CLASSCD"][TEXT] = $classcd;
        $data["SCHOOL_KIND"][TEXT] = $schoolKind;
        $data["CURRICULUM_CD"][TEXT] = $curriculumCd;
        $data["SUBCLASSCD"][TEXT] = $subclasscd;
        $data["SCHREGNO"][TEXT] = $schregno;
        $data["PROV_FLG"][TEXT] = "1";
        $data["PROV_SEMESTER"][TEXT] = $provSemester;
        $data["PROV_TESTKINDCD"][TEXT] = "99";
        $data["PROV_TESTITEMCD"][TEXT] = "00";
        $data["PROV_SCORE_DIV"][TEXT] = "08";
        $data["REGISTERCD"][TEXT] = STAFFCD;
        $data["UPDATED"][FUNC] = "sysdate()";

        $query = Query::insertSQL($data, "RECORD_PROV_FLG_DAT");
        return $query;
    }

    // 平常点を更新
    function updateHeijo($db, $model) {
        $scoreDiv = "02";
        // 平常点を削除
        $query = knjd280aQuery::deleteRecordScoreDatSql(CTRL_SEMESTER, $scoreDiv, $model);
        $db->query($query);

        $m006Heijo = array();
        $query = knjd280aQuery::getM006();

        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $m006Heijo[$row["NAMECD2"]]["1"] = $row["NAMESPARE1"];
            $m006Heijo[$row["NAMECD2"]]["2"] = $row["NAMESPARE2"];
            $m006Heijo[$row["NAMECD2"]]["3"] = $row["NAMESPARE3"];
        }
        $result->free();

        $query = knjd280aQuery::getCorresSql($model);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $heijo = $m006Heijo[$row["VAL_NUMERIC"]][$row["M006_ABBV"]];
            if ($heijo == '') {
                //echo "平常点無し ".$row["VAL_NUMERIC"]."<br>";
            } else {
                $subclasscd = $row["CLASSCD"]."-".$row["SCHOOL_KIND"]."-".$row["CURRICULUM_CD"]."-".$row["SUBCLASSCD"];
                knjd280aQuery::insertRecordScoreDatSql($db, CTRL_SEMESTER, $scoreDiv, $subclasscd, $row["SCHREGNO"], $heijo);
            }
        }
        $result->free();
    }

    // 平常点算出母集団
    function getCorresSql($model) {
        $query  = "";
        $query .= " SELECT ";
        $query .= "    T1.CLASSCD ";
        $query .= "   ,T1.SCHOOL_KIND ";
        $query .= "   ,T1.CURRICULUM_CD ";
        $query .= "   ,T1.SUBCLASSCD";
        $query .= "   ,T1.SCHREGNO ";
        $query .= "   ,T1.VAL_NUMERIC ";
        $query .= "   , VALUE(M022.ABBV2, M022_2.ABBV2) AS M006_ABBV ";
        $query .= " FROM SUBCLASS_CORRES_RST_SEMES_DAT T1 ";
        $query .= " INNER JOIN SCHREG_REGD_DAT REGD ON ";
        $query .= "     REGD.SCHREGNO = T1.SCHREGNO ";
        $query .= "     AND REGD.YEAR = T1.YEAR ";
        $query .= "     AND REGD.SEMESTER = T1.SEMESTER ";
        $query .= "     AND REGD.GRADE || REGD.HR_CLASS IN ('".implode("','", $model->field["CLASS_SELECTED"])."') ";
        $query .= " LEFT JOIN V_NAME_MST M021 ON M021.YEAR = '".CTRL_YEAR."' AND M021.NAMECD1 = 'M021' ";
        $query .= "     AND M021.NAME1 = REGD.COURSECD ";
        $query .= "     AND M021.NAME2 = REGD.MAJORCD ";
        $query .= "     AND M021.ABBV1 = T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD ";
        $query .= " LEFT JOIN V_NAME_MST M022 ON M022.YEAR = '".CTRL_YEAR."' AND M022.NAMECD1 = 'M022' ";
        $query .= "     AND M022.NAMECD2 = M021.ABBV2 ";
        $query .= " LEFT JOIN V_NAME_MST M021_2 ON M021_2.YEAR = '".CTRL_YEAR."' AND M021_2.NAMECD1 = 'M021' ";
        $query .= "     AND M021_2.NAME1 = REGD.COURSECD ";
        $query .= "     AND M021_2.NAME2 = REGD.MAJORCD ";
        $query .= "     AND M021_2.ABBV1 IS NULL ";
        $query .= " LEFT JOIN V_NAME_MST M022_2 ON M022_2.YEAR = '".CTRL_YEAR."' AND M022_2.NAMECD1 = 'M022' ";
        $query .= "     AND M022_2.NAMECD2 = M021_2.ABBV2 ";
        $query .= " WHERE ";
        $query .= "    T1.YEAR = '".CTRL_YEAR."' ";
        $query .= "    AND T1.SEMESTER = '".CTRL_SEMESTER."' ";
        $query .= "    AND T1.RST_DIV = 'T' ";
        $query .= "    AND T1.SEQ = 1 ";
        $query .= "    AND T1.VAL_NUMERIC IS NOT NULL ";
        $query .= "    AND T1.CLASSCD <= '90' ";
        return $query;
    }

    function getSchAttendDatSql($model) {
        $query  = " SELECT ";
        $query .= "       T1.SCHREGNO ";
        $query .= "     , CHR.CLASSCD || '-' || CHR.SCHOOL_KIND || '-' || CHR.CURRICULUM_CD || '-' || CHR.SUBCLASSCD AS SUBCLASSCD ";
        $query .= "     , T1.SCHOOLINGKINDCD ";
        $query .= "     , SUM(T1.CREDIT_TIME) AS CREDIT_TIME ";
        $query .= " FROM SCH_ATTEND_DAT T1 ";
        $query .= " INNER JOIN SEMESTER_MST SEME ON SEME.YEAR = T1.YEAR ";
        $query .= "                             AND SEME.SEMESTER <> '9' ";
        $query .= "                             AND T1.EXECUTEDATE BETWEEN SEME.SDATE AND SEME.EDATE ";
        $query .= " INNER JOIN SCHREG_REGD_DAT REGD ON REGD.YEAR = T1.YEAR ";
        $query .= "                             AND REGD.SEMESTER = SEME.SEMESTER ";
        $query .= "                             AND REGD.SCHREGNO = T1.SCHREGNO ";
        $query .= "                             AND REGD.GRADE || REGD.HR_CLASS IN ('".implode("','", $model->field["CLASS_SELECTED"])."') ";
        $query .= " INNER JOIN CHAIR_DAT CHR ON CHR.YEAR = T1.YEAR ";
        $query .= "                         AND CHR.SEMESTER = SEME.SEMESTER ";
        $query .= "                         AND CHR.CHAIRCD = T1.CHAIRCD ";
        $query .= " INNER JOIN SUBCLASS_MST SUB_M ON SUB_M.CLASSCD = CHR.CLASSCD ";
        $query .= "                              AND SUB_M.SCHOOL_KIND = CHR.SCHOOL_KIND ";
        $query .= "                              AND SUB_M.CURRICULUM_CD = CHR.CURRICULUM_CD ";
        $query .= "                              AND SUB_M.SUBCLASSCD = CHR.SUBCLASSCD ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND SEME.SEMESTER <= '".CTRL_SEMESTER."' ";
        $query .= "     AND T1.EXECUTEDATE <= '".str_replace('/', '-', $model->field["DATE"])."' ";
        $query .= "     AND CHR.CLASSCD <= '90' ";
        $query .= " GROUP BY ";
        $query .= "       T1.SCHREGNO ";
        $query .= "     , CHR.CLASSCD || '-' || CHR.SCHOOL_KIND || '-' || CHR.CURRICULUM_CD || '-' || CHR.SUBCLASSCD ";
        $query .= "     , T1.SCHOOLINGKINDCD ";
        $query .= " HAVING ";
        $query .= "     SUM(T1.CREDIT_TIME) IS NOT NULL ";
        return $query;
    }

    // 出席点を更新
    function updateAttend($db, $model) {
        $scoreDiv = "07";
        // 出席点を削除
        $query = knjd280aQuery::deleteRecordScoreDatSql(CTRL_SEMESTER, $scoreDiv, $model); 
        $db->query($query);
        // 出欠を取得
        $attend = knjd280aQuery::getAttendSubclassDat($model);

        $attendPerfect = array();
        $query = knjd280aQuery::getAttendPerfect($model);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            if ($row["SUBCLASSCD"] == '') {
                $row["SUBCLASSCD"] = "000000";
            }
            $attendPerfect[$row["SCHREGNO"]][$row["SUBCLASSCD"]] = $row["ATTEND_PERFECT"];
        }
        $result->free();

        // 出席点を更新
        foreach ($attend as $schregno => $subclasscdrecords) {
            foreach ($subclasscdrecords as $subclasscd => $record) {
                $p= $attendPerfect[$schregno][$subclasscd];
                if ($p == '') {
                    $p = $attendPerfect[$schregno]["000000"];
                }
                $lesson = $record[0];
                $sick = $record[1];
                $model->attendInfo[$schregno][$subclasscd] = $lesson - $sick;
                if ($p == '') {
                    continue;
                }
                if (0 < $lesson && 0 <= $lesson - $sick) {
                    $score = $p * 1.0 * ($lesson - $sick) / $lesson;
                    $score = ceil($score);
                    knjd280aQuery::insertRecordScoreDatSql($db, CTRL_SEMESTER, $scoreDiv, $subclasscd, $schregno, $score); 
                }
            }
        }

        // スクーリング
        $query = knjd280aQuery::getSchAttendDatSql($model);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $model->attendInfo[$row["SCHREGNO"]][$row["SUBCLASSCD"]] += $row["CREDIT_TIME"];
        }
        $result->free();
    }

    function getAttendSubclassDat($model) {

        //$url = (empty($_SERVER["HTTPS"]) ? "http://" : "https://") . $_SERVER["HTTP_HOST"] .SERVLET_URL ."/KNJZ";
        $url = SERVLET_URL ."/KNJZ";
        $data = array('CALL_DEST_JAVA_PRGID' => 'servletpack.KNJZ.detail.dao.AttendAccumulateIO',
                      'METHOD_NAME'     => 'json',
                      'DBNAME'          => DB_DATABASE,
                      'DATE'            => str_replace('/', '-', $model->field["DATE"]),
                      'ATTEND_DIV'      => 'ATTEND_SUBCLASS_DAT',
                      'CTRL_YEAR'       => CTRL_YEAR,
                      'CTRL_SEMESTER'   => CTRL_SEMESTER,
                      'GRADE_HR_CLASS'  => implode(",", $model->field["CLASS_SELECTED"]),
                      'useCurriculumcd' => $model->Properties["useCurriculumcd"],
                      'useVirus'        => $model->Properties["useVirus"],
                      'useKoudome'      => $model->Properties["useKoudome"],
                      'useTestCountflg' => $model->Properties["useTestCountflg"],
        );

        // use key 'http' even if you send the request to https://...
        $options = array(
            'http' => array(
                'header'  => "Content-type: application/x-www-form-urlencoded\r\n",
                'method'  => 'POST',
                'content' => http_build_query($data)
            )
        );
        $arr = array();
        $context  = stream_context_create($options);
        $result = file_get_contents($url, false, $context);
        if ($result != FALSE) {
            $arr = json_decode($result, true);
            foreach ($arr as $schregno => $records) {
                foreach ($records as $record) {
                    $arr[$schregno][$record["SUBCLASSCD"]] = array($record["LESSON"], $record["SICK2"]);
                }
            }
        }
        return $arr;
    }

    // 出席点対象母集団
    function getAttendPerfect($model) {
        $query = "";
        $query .= " SELECT REGD.SCHREGNO, REGD.COURSECD, REGD.MAJORCD, M021.ABBV1 AS SUBCLASSCD, M022.ABBV1 AS ATTEND_PERFECT  ";
        $query .= " FROM SCHREG_REGD_DAT REGD ";
        $query .= " LEFT JOIN V_NAME_MST M021 ON M021.YEAR = '".CTRL_YEAR."' AND M021.NAMECD1 = 'M021' ";
        $query .= "     AND M021.NAME1 = REGD.COURSECD ";
        $query .= "     AND M021.NAME2 = REGD.MAJORCD ";
        $query .= " LEFT JOIN V_NAME_MST M022 ON M022.YEAR = '".CTRL_YEAR."' AND M022.NAMECD1 = 'M022' ";
        $query .= "     AND M022.NAMECD2 = M021.ABBV2 ";
        $query .= " WHERE ";
        $query .= "     REGD.YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND REGD.SEMESTER = '".CTRL_SEMESTER."' ";
        $query .= "     AND REGD.GRADE || REGD.HR_CLASS IN ('".implode("','", $model->field["CLASS_SELECTED"])."') ";
        return $query;
    }

    // 学期評価算出の母集団
    function getRecordScoreDatSql($model, $div, $takeSemesFlg = "") {
        $query = "";
        $query .= " WITH REGD AS (";
        $query .= "     SELECT REGD.SCHREGNO, REGD.GRADE, REGD.COURSECD, REGD.MAJORCD, REGD.COURSECODE ";
        $query .= "     FROM SCHREG_REGD_DAT REGD ";
        $query .= "     WHERE ";
        $query .= "         REGD.YEAR = '".CTRL_YEAR."' ";
        $query .= "         AND REGD.SEMESTER = '".CTRL_SEMESTER."' ";
        $query .= "         AND REGD.GRADE || REGD.HR_CLASS IN ('".implode("','", $model->field["CLASS_SELECTED"])."') ";
        $query .= " ) ";
        if ($div == 'EXAM_SCORE') {
            // 成績点
            $query .= " SELECT ";
            $query .= "    T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD AS SUBCLASSCD ";
            $query .= "   ,T1.SCHREGNO ";
            $query .= "   ,1.0 * SUM(T1.SCORE) * FLOAT(VALUE(M022.ABBV3, M022_2.ABBV3, '0.0')) / COUNT(*) AS SCORE ";
            $query .= " FROM RECORD_SCORE_DAT T1 ";
            $query .= " INNER JOIN REGD ON REGD.SCHREGNO = T1.SCHREGNO ";
            $query .= " LEFT JOIN V_NAME_MST M021 ON M021.YEAR = '".CTRL_YEAR."' AND M021.NAMECD1 = 'M021' ";
            $query .= "     AND M021.NAME1 = REGD.COURSECD ";
            $query .= "     AND M021.NAME2 = REGD.MAJORCD ";
            $query .= "     AND M021.ABBV1 = T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD ";
            $query .= " LEFT JOIN V_NAME_MST M022 ON M022.YEAR = '".CTRL_YEAR."' AND M022.NAMECD1 = 'M022' ";
            $query .= "     AND M022.NAMECD2 = M021.ABBV2 ";
            $query .= " LEFT JOIN V_NAME_MST M021_2 ON M021_2.YEAR = '".CTRL_YEAR."' AND M021_2.NAMECD1 = 'M021' ";
            $query .= "     AND M021_2.NAME1 = REGD.COURSECD ";
            $query .= "     AND M021_2.NAME2 = REGD.MAJORCD ";
            $query .= "     AND M021_2.ABBV1 IS NULL ";
            $query .= " LEFT JOIN V_NAME_MST M022_2 ON M022_2.YEAR = '".CTRL_YEAR."' AND M022_2.NAMECD1 = 'M022' ";
            $query .= "     AND M022_2.NAMECD2 = M021_2.ABBV2 ";
            $query .= " WHERE ";
            $query .= "     T1.YEAR = '".CTRL_YEAR."' ";
            $query .= "     AND T1.SEMESTER <='".CTRL_SEMESTER."' "; // 指定学期までの平均
            $query .= "     AND (VALUE(M022.NAMESPARE1, M022_2.NAMESPARE1, '') = '1' AND T1.TESTKINDCD = '02' ";
            $query .= "       OR VALUE(M022.NAMESPARE1, M022_2.NAMESPARE1, '') = ''  AND T1.TESTKINDCD IN ('01', '02') ";
            $query .= "         ) ";
            $query .= "     AND T1.TESTITEMCD = '01' ";
            $query .= "     AND T1.SCORE_DIV = '01' "; // 素点
            $query .= "     AND (T1.SCORE IS NOT NULL OR T1.VALUE_DI = '*') "; 
            $query .= "     AND T1.CLASSCD <= '90' ";
            $query .= " GROUP BY ";
            $query .= "    T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD ";
            $query .= "   ,VALUE(M022.ABBV3, M022_2.ABBV3, '0.0') ";
            $query .= "   ,T1.SCHREGNO ";
            $query .= " HAVING ";
            $query .= "    COUNT(*) > 0 ";
        } else if ($div == 'HEIJO') {
            // 平常点
            if ($takeSemesFlg) {
                $query .= " , CHAIR_SUBCLASS AS ( ";
                $query .= "   SELECT ";
                $query .= "    T2.CLASSCD, T2.SCHOOL_KIND, T2.CURRICULUM_CD, T2.SUBCLASSCD ";
                $query .= "   ,T1.SCHREGNO ";
                $query .= "   FROM CHAIR_STD_DAT T1 ";
                $query .= "   INNER JOIN CHAIR_DAT T2 ON T2.YEAR = T1.YEAR ";
                $query .= "       AND T2.SEMESTER = T1.SEMESTER ";
                $query .= "       AND T2.CHAIRCD = T1.CHAIRCD ";
                if ($model->Properties["printSubclassLastChairStd"] == "1") {
                    $query .= "   INNER JOIN SEMESTER_MST SEME ON SEME.YEAR = T1.YEAR ";
                    $query .= "       AND SEME.SEMESTER = T1.SEMESTER ";
                    $query .= "       AND SEME.EDATE = T1.APPENDDATE ";
                }
                $query .= "   INNER JOIN REGD ON REGD.SCHREGNO = T1.SCHREGNO ";
                $query .= "   WHERE ";
                $query .= "       T1.YEAR = '".CTRL_YEAR."' ";
                $query .= "       AND T1.SEMESTER <= '".CTRL_SEMESTER."' ";
                if ($takeSemesFlg == "1") {
                    // 前期科目
                    $query .= "       AND T2.TAKESEMES = '1' ";
                } else {
                    // 前期科目以外 = 後期科目、通年科目
                    $query .= "       AND VALUE(T2.TAKESEMES, '') <> '1' ";
                }
                $query .= "   GROUP BY ";
                $query .= "    T2.CLASSCD, T2.SCHOOL_KIND, T2.CURRICULUM_CD, T2.SUBCLASSCD ";
                $query .= "   ,T1.SCHREGNO ";
                $query .= " ) ";
            }
            $query .= "   SELECT ";
            $query .= "    T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD AS SUBCLASSCD ";
            $query .= "   ,T1.SCHREGNO ";
            $query .= "   ,T1.SCORE ";
            $query .= " FROM RECORD_SCORE_DAT T1 ";
            $query .= " INNER JOIN REGD T2 ON T2.SCHREGNO = T1.SCHREGNO ";
            if ($takeSemesFlg) {
                $query .= " INNER JOIN CHAIR_SUBCLASS T3 ON T3.SCHREGNO = T1.SCHREGNO ";
                $query .= "     AND T3.CLASSCD = T1.CLASSCD ";
                $query .= "     AND T3.SCHOOL_KIND = T1.SCHOOL_KIND ";
                $query .= "     AND T3.CURRICULUM_CD = T1.CURRICULUM_CD ";
                $query .= "     AND T3.SUBCLASSCD = T1.SUBCLASSCD ";
            }
            $query .= "   WHERE ";
            $query .= "     YEAR = '".CTRL_YEAR."' ";
            $query .= "     AND SEMESTER ='".CTRL_SEMESTER."' ";
            $query .= "     AND TESTKINDCD = '99' ";
            $query .= "     AND TESTITEMCD = '00' ";
            $query .= "     AND SCORE_DIV = '02' "; 
            $query .= "     AND T1.CLASSCD <= '90' ";
        } else if ($div == 'ATTEND') {
            // 出席点
            $query .= "   SELECT ";
            $query .= "    T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD AS SUBCLASSCD ";
            $query .= "   ,T1.SCHREGNO ";
            $query .= "   ,T1.SCORE ";
            $query .= " FROM RECORD_SCORE_DAT T1 ";
            $query .= " INNER JOIN REGD T2 ON T2.SCHREGNO = T1.SCHREGNO ";
            $query .= "   WHERE ";
            $query .= "     YEAR = '".CTRL_YEAR."' ";
            $query .= "     AND SEMESTER ='".CTRL_SEMESTER."' ";
            $query .= "     AND TESTKINDCD = '99' ";
            $query .= "     AND TESTITEMCD = '00' ";
            $query .= "     AND SCORE_DIV = '07' "; 
            $query .= "     AND CLASSCD <= '90' ";
        } else if ($div == 'GAKKI_HYOKA_KANSAN') {
            // 講座 (履修期間区分)
            $query .= " , CHAIR_SUBCLASS AS ( ";
            $query .= "   SELECT ";
            $query .= "    T2.CLASSCD, T2.SCHOOL_KIND, T2.CURRICULUM_CD, T2.SUBCLASSCD ";
            $query .= "   ,T1.SCHREGNO ";
            $query .= "   , MAX(T3.REPO_LIMIT_CNT) AS REPO_LIMIT_CNT ";
            $query .= "   , MAX(T3.SCHOOLING_LIMIT_CNT) AS SCHOOLING_LIMIT_CNT ";
            $query .= "   , MAX(T3.USE_MEDIA1) AS USE_MEDIA1 ";
            $query .= "   , MAX(T2.TAKESEMES) AS TAKESEMES ";
            $query .= "   FROM CHAIR_STD_DAT T1 ";
            $query .= "   INNER JOIN CHAIR_DAT T2 ON T2.YEAR = T1.YEAR ";
            $query .= "       AND T2.SEMESTER = T1.SEMESTER ";
            $query .= "       AND T2.CHAIRCD = T1.CHAIRCD ";
            if ($model->Properties["printSubclassLastChairStd"] == "1") {
                $query .= "   INNER JOIN SEMESTER_MST SEME ON SEME.YEAR = T1.YEAR ";
                $query .= "       AND SEME.SEMESTER = T1.SEMESTER ";
                $query .= "       AND SEME.EDATE = T1.APPENDDATE ";
            }
            $query .= "   INNER JOIN REGD ON REGD.SCHREGNO = T1.SCHREGNO ";
            $query .= "   LEFT JOIN CHAIR_CORRES_SEMES_DAT T3 ON T3.YEAR = T1.YEAR ";
            $query .= "    AND T3.SEMESTER = T1.SEMESTER ";
            $query .= "    AND T3.CHAIRCD = T1.CHAIRCD ";
            $query .= "    AND T3.CLASSCD = T2.CLASSCD ";
            $query .= "    AND T3.SCHOOL_KIND = T2.SCHOOL_KIND ";
            $query .= "    AND T3.CURRICULUM_CD = T2.CURRICULUM_CD ";
            $query .= "    AND T3.SUBCLASSCD = T2.SUBCLASSCD ";
            $query .= "   WHERE ";
            $query .= "       T1.YEAR = '".CTRL_YEAR."' ";
            $query .= "       AND T1.SEMESTER <= '".CTRL_SEMESTER."' ";
            if ($takeSemesFlg == "1") {
                // 前期科目
                $query .= "       AND T2.TAKESEMES = '1' ";
            } else {
                // 前期科目以外 = 後期科目、通年科目
                $query .= "       AND VALUE(T2.TAKESEMES, '') <> '1' ";
            }
            $query .= "   GROUP BY ";
            $query .= "    T2.CLASSCD, T2.SCHOOL_KIND, T2.CURRICULUM_CD, T2.SUBCLASSCD ";
            $query .= "   ,T1.SCHREGNO ";
            $query .= " ) ";
            // 学期評価
            $query .= "   SELECT ";
            $query .= "    T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD AS SUBCLASSCD ";
            $query .= "   ,T1.SCHREGNO ";
            $query .= "   ,T1.SCORE ";
            $query .= "   ,CASE WHEN ACM.ABBV3 IS NOT NULL THEN ACM.NAMESPARE1 ELSE ACM_2.NAMESPARE1 END AS ASSESSLEVEL ";
            $query .= "   ,CRE.CREDITS ";
            $query .= "   ,M006.ABBV3 AS HYOTEI_TAISHOGAI_FLG ";
            $query .= "   ,T3.TAKESEMES ";
            $query .= "   ,T3.REPO_LIMIT_CNT ";
            $query .= "   ,T3.SCHOOLING_LIMIT_CNT ";
            $query .= "   ,T3.USE_MEDIA1 ";
            $query .= "   ,L5R.VAL_NUMERIC AS R_VAL1 ";
            $query .= "   ,L5T.VAL_NUMERIC AS T_VAL1 ";
            $query .= "   ,L5S.VAL_NUMERIC AS S_VAL1 ";
            $query .= "   ,KIMATSU.SCORE AS KIMATSU_SCORE ";
            $query .= "   ,KIMATSU_SAI.SCORE AS KIMATSU_SAI_SCORE ";
            $query .= "   ,M024.ABBV2 AS KIMATSU_SAI_PASS_SCORE ";
            $query .= "   ,VALUE(M022.NAMESPARE2, M022_2.NAMESPARE2) AS KIMATSU_SCORE_NO_CHECK ";
            $query .= " FROM RECORD_SCORE_DAT T1 ";
            $query .= " INNER JOIN REGD ON REGD.SCHREGNO = T1.SCHREGNO ";
            $query .= " INNER JOIN CHAIR_SUBCLASS T3 ON T3.SCHREGNO = T1.SCHREGNO ";
            $query .= "     AND T3.CLASSCD = T1.CLASSCD ";
            $query .= "     AND T3.SCHOOL_KIND = T1.SCHOOL_KIND ";
            $query .= "     AND T3.CURRICULUM_CD = T1.CURRICULUM_CD ";
            $query .= "     AND T3.SUBCLASSCD = T1.SUBCLASSCD ";
            $query .= " LEFT JOIN V_NAME_MST ACM ON ACM.YEAR = '".CTRL_YEAR."' AND ACM.NAMECD1 = 'M023' ";
            $query .= "     AND ACM.NAME1 = REGD.COURSECD ";
            $query .= "     AND ACM.NAME2 = REGD.MAJORCD ";
            $query .= "     AND ACM.ABBV3 = T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD ";
            $query .= "     AND T1.SCORE BETWEEN INT(ACM.NAMESPARE2) AND INT(ACM.NAMESPARE3) ";
            $query .= " LEFT JOIN V_NAME_MST ACM_2 ON ACM_2.YEAR = '".CTRL_YEAR."' AND ACM_2.NAMECD1 = 'M023' ";
            $query .= "     AND ACM_2.NAME1 = REGD.COURSECD ";
            $query .= "     AND ACM_2.NAME2 = REGD.MAJORCD ";
            $query .= "     AND ACM_2.ABBV3 IS NULL  ";
            $query .= "     AND T1.SCORE BETWEEN INT(ACM_2.NAMESPARE2) AND INT(ACM_2.NAMESPARE3) ";
            $query .= " LEFT JOIN CREDIT_MST CRE ON CRE.YEAR = T1.YEAR ";
            $query .= "     AND CRE.COURSECD = REGD.COURSECD ";
            $query .= "     AND CRE.GRADE = REGD.GRADE ";
            $query .= "     AND CRE.MAJORCD = REGD.MAJORCD ";
            $query .= "     AND CRE.COURSECODE = REGD.COURSECODE ";
            $query .= "     AND CRE.CLASSCD = T1.CLASSCD ";
            $query .= "     AND CRE.SCHOOL_KIND = T1.SCHOOL_KIND ";
            $query .= "     AND CRE.CURRICULUM_CD = T1.CURRICULUM_CD ";
            $query .= "     AND CRE.SUBCLASSCD = T1.SUBCLASSCD ";
            $query .= " LEFT JOIN SUBCLASS_CORRES_RST_SEMES_DAT L5R ON ";
            $query .= "      L5R.YEAR = T1.YEAR ";
            $query .= "    AND L5R.SEMESTER = T1.SEMESTER ";
            $query .= "    AND L5R.CLASSCD = T1.CLASSCD ";
            $query .= "    AND L5R.SCHOOL_KIND = T1.SCHOOL_KIND ";
            $query .= "    AND L5R.CURRICULUM_CD = T1.CURRICULUM_CD ";
            $query .= "    AND L5R.SUBCLASSCD = T1.SUBCLASSCD ";
            $query .= "    AND L5R.RST_DIV = 'R' ";
            $query .= "    AND L5R.SEQ = 1 ";
            $query .= "    AND L5R.SCHREGNO = T1.SCHREGNO ";
            $query .= " LEFT JOIN SUBCLASS_CORRES_RST_SEMES_DAT L5T ON ";
            $query .= "      L5T.YEAR = T1.YEAR ";
            $query .= "    AND L5T.SEMESTER = T1.SEMESTER ";
            $query .= "    AND L5T.CLASSCD = T1.CLASSCD ";
            $query .= "    AND L5T.SCHOOL_KIND = T1.SCHOOL_KIND ";
            $query .= "    AND L5T.CURRICULUM_CD = T1.CURRICULUM_CD ";
            $query .= "    AND L5T.SUBCLASSCD = T1.SUBCLASSCD ";
            $query .= "    AND L5T.RST_DIV = 'T' ";
            $query .= "    AND L5T.SEQ = 1 ";
            $query .= "    AND L5T.SCHREGNO = T1.SCHREGNO ";
            $query .= " LEFT JOIN SUBCLASS_CORRES_RST_SEMES_DAT L5S ON ";
            $query .= "      L5S.YEAR = T1.YEAR ";
            $query .= "    AND L5S.SEMESTER = T1.SEMESTER ";
            $query .= "    AND L5S.CLASSCD = T1.CLASSCD ";
            $query .= "    AND L5S.SCHOOL_KIND = T1.SCHOOL_KIND ";
            $query .= "    AND L5S.CURRICULUM_CD = T1.CURRICULUM_CD ";
            $query .= "    AND L5S.SUBCLASSCD = T1.SUBCLASSCD ";
            $query .= "    AND L5S.RST_DIV = 'S' ";
            $query .= "    AND L5S.SEQ = 1 ";
            $query .= "    AND L5S.SCHREGNO = T1.SCHREGNO ";
            $query .= " LEFT JOIN NAME_MST M006 ON M006.NAMECD1 = 'M006' ";
            $query .= "    AND M006.NAMECD2 = CAST(L5T.VAL_NUMERIC AS VARCHAR(1)) ";
            $query .= " LEFT JOIN RECORD_SCORE_DAT KIMATSU ON KIMATSU.YEAR = T1.YEAR ";
            $query .= "    AND KIMATSU.SEMESTER = T1.SEMESTER ";
            $query .= "    AND KIMATSU.TESTKINDCD = '02' ";
            $query .= "    AND KIMATSU.TESTITEMCD = '01' ";
            $query .= "    AND KIMATSU.SCORE_DIV = '01' ";
            $query .= "    AND KIMATSU.CLASSCD = T1.CLASSCD ";
            $query .= "    AND KIMATSU.SCHOOL_KIND = T1.SCHOOL_KIND ";
            $query .= "    AND KIMATSU.CURRICULUM_CD = T1.CURRICULUM_CD ";
            $query .= "    AND KIMATSU.SUBCLASSCD = T1.SUBCLASSCD ";
            $query .= "    AND KIMATSU.SCHREGNO = T1.SCHREGNO ";
            $query .= " LEFT JOIN RECORD_SLUMP_SDIV_DAT KIMATSU_SAI ON KIMATSU_SAI.YEAR = T1.YEAR ";
            $query .= "    AND KIMATSU_SAI.SEMESTER = T1.SEMESTER ";
            $query .= "    AND KIMATSU_SAI.TESTKINDCD = '02' ";
            $query .= "    AND KIMATSU_SAI.TESTITEMCD = '01' ";
            $query .= "    AND KIMATSU_SAI.SCORE_DIV = '01' ";
            $query .= "    AND KIMATSU_SAI.CLASSCD = T1.CLASSCD ";
            $query .= "    AND KIMATSU_SAI.SCHOOL_KIND = T1.SCHOOL_KIND ";
            $query .= "    AND KIMATSU_SAI.CURRICULUM_CD = T1.CURRICULUM_CD ";
            $query .= "    AND KIMATSU_SAI.SUBCLASSCD = T1.SUBCLASSCD ";
            $query .= "    AND KIMATSU_SAI.SCHREGNO = T1.SCHREGNO ";
            $query .= " LEFT JOIN V_NAME_MST M024 ON M024.YEAR = '".CTRL_YEAR."' AND M024.NAMECD1 = 'M024' ";
            $query .= "     AND M024.NAME1 = REGD.COURSECD ";
            $query .= "     AND M024.NAME2 = REGD.MAJORCD ";
            $query .= "     AND M024.ABBV1 = T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD ";
            $query .= " LEFT JOIN V_NAME_MST M021 ON M021.YEAR = '".CTRL_YEAR."' AND M021.NAMECD1 = 'M021' ";
            $query .= "     AND M021.NAME1 = REGD.COURSECD ";
            $query .= "     AND M021.NAME2 = REGD.MAJORCD ";
            $query .= "     AND M021.ABBV1 = T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD ";
            $query .= " LEFT JOIN V_NAME_MST M022 ON M022.YEAR = '".CTRL_YEAR."' AND M022.NAMECD1 = 'M022' ";
            $query .= "     AND M022.NAMECD2 = M021.ABBV2 ";
            $query .= " LEFT JOIN V_NAME_MST M021_2 ON M021_2.YEAR = '".CTRL_YEAR."' AND M021_2.NAMECD1 = 'M021' ";
            $query .= "     AND M021_2.NAME1 = REGD.COURSECD ";
            $query .= "     AND M021_2.NAME2 = REGD.MAJORCD ";
            $query .= "     AND M021_2.ABBV1 IS NULL ";
            $query .= " LEFT JOIN V_NAME_MST M022_2 ON M022_2.YEAR = '".CTRL_YEAR."' AND M022_2.NAMECD1 = 'M022' ";
            $query .= "     AND M022_2.NAMECD2 = M021_2.ABBV2 ";
            $query .= " WHERE ";
            $query .= "     T1.YEAR = '".CTRL_YEAR."' ";
            if ($takeSemesFlg == "1") {
                $query .= "     AND T1.SEMESTER ='1' ";
            } else {
                $query .= "     AND T1.SEMESTER ='".CTRL_SEMESTER."' ";
            }
            $query .= "     AND T1.TESTKINDCD = '99' ";
            $query .= "     AND T1.TESTITEMCD = '00' ";
            $query .= "     AND T1.SCORE_DIV = '08' "; 
            $query .= "     AND T1.CLASSCD <= '90' ";
        }
        $query .= "  ORDER BY "; 
        $query .= "     T1.SCHREGNO, T1.CLASSCD || '-' || T1.SCHOOL_KIND || '-' || T1.CURRICULUM_CD || '-' || T1.SUBCLASSCD ";
        return $query;
    }

    // 学期評価を更新
    function updateGakkiHyoka($db, $model) {
        $scoreDiv = '08';
        // 学期成績を削除
        $query = knjd280aQuery::deleteRecordScoreDatSql(CTRL_SEMESTER, $scoreDiv, $model);
        $db->query($query);

        $scores = array();
        $divs = array("EXAM_SCORE", "HEIJO", "ATTEND");
        foreach ($divs as $div) {
            $query = knjd280aQuery::getRecordScoreDatSql($model, $div);
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $score = $row["SCORE"];
                if ($div == "EXAM_SCORE") {
                    $score = round($score, 0, PHP_ROUND_HALF_UP);
                }
                $scores[$row["SCHREGNO"]][$row["SUBCLASSCD"]][$div] = $score;
            }
            $result->free();
        }

        foreach ($scores as $schregno => $subclassScores) {
            foreach ($subclassScores as $subclasscd => $divscore) {
                $attend    = $divscore["ATTEND"]; // 出席点
                $heijo     = $divscore["HEIJO"]; // 平常点
                $examScore = $divscore["EXAM_SCORE"]; // 試験点
                $gakkiHyoka = $attend + $heijo + $examScore;
                knjd280aQuery::insertRecordScoreDatSql($db, CTRL_SEMESTER, $scoreDiv, $subclasscd, $schregno, $gakkiHyoka);
            }
        }
    }

    function nintei($db, $model) {

        $takeSemesFlgs = array();
        if ($model->field["NINTEI_SEME1"] == "1") {
            $takeSemesFlgs[] = "1";
        }
        if ($model->field["NINTEI_SEME2"] == "1") {
            $takeSemesFlgs[] = "2";
        }
        foreach ($takeSemesFlgs as $takeSemesFlg) {
            // 評定を削除
            $query = knjd280aQuery::deleteRecordScoreDatSql("9", "08", $model, $takeSemesFlg);
            $db->query($query);
            $query = knjd280aQuery::deleteRecordScoreDatSql("9", "09", $model, $takeSemesFlg);
            $db->query($query);
            $query = knjd280aQuery::deleteRecordProvFlgDatSql($model, $takeSemesFlg);
            $db->query($query);

            $scores = array();
            $divs = array("GAKKI_HYOKA_KANSAN" => array("SCORE", "ASSESSLEVEL", "CREDITS", "HYOTEI_TAISHOGAI_FLG", "TAKESEMES", "REPO_LIMIT_CNT", "SCHOOLING_LIMIT_CNT", "USE_MEDIA1", "R_VAL1", "T_VAL1", "S_VAL1", "KIMATSU_SCORE", "KIMATSU_SAI_SCORE", "KIMATSU_SAI_PASS_SCORE", "KIMATSU_SCORE_NO_CHECK"),
                "HEIJO" => array("SCORE")
                         );

            foreach ($divs as $div => $vals) {
                $query = knjd280aQuery::getRecordScoreDatSql($model, $div, $takeSemesFlg);
                $result = $db->query($query);
                while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                    foreach ($vals as $val) {
                        $scores[$row["SCHREGNO"]][$row["SUBCLASSCD"]][$div][$val] = $row[$val];
                    }
                }
                $result->free();
            }

            foreach ($scores as $schregno => $subclassScores) {
                foreach ($subclassScores as $subclasscd => $divscore) {
                    $heijo = $divscore["HEIJO"]["SCORE"]; // 平常点
                    $credits = $divscore["GAKKI_HYOKA_KANSAN"]["CREDITS"]; // 単位数
                    $reportGokaku = $divscore["GAKKI_HYOKA_KANSAN"]["R_VAL1"]; // レポート合格数
                    $takesemes = $divscore["GAKKI_HYOKA_KANSAN"]["TAKESEMES"]; // 履修学期
                    $reportKitei = $divscore["GAKKI_HYOKA_KANSAN"]["REPO_LIMIT_CNT"]; // レポート規定数
                    $shussekiKitei = $divscore["GAKKI_HYOKA_KANSAN"]["SCHOOLING_LIMIT_CNT"]; // 出席規定数
                    $useMedia1 = $divscore["GAKKI_HYOKA_KANSAN"]["USE_MEDIA1"]; // ネット視聴あり
                    $netMaru = $divscore["GAKKI_HYOKA_KANSAN"]["S_VAL1"]; // ネット〇
                    $tval1 = $divscore["GAKKI_HYOKA_KANSAN"]["T_VAL1"]; // 評価A-E
                    $adIgai = $divscore["GAKKI_HYOKA_KANSAN"]["HYOTEI_TAISHOGAI_FLG"]; // A-D以外
                    $kimatsu = $divscore["GAKKI_HYOKA_KANSAN"]["KIMATSU_SCORE"]; // 期末試験
                    $kimatsuSai = $divscore["GAKKI_HYOKA_KANSAN"]["KIMATSU_SAI_SCORE"]; // 期末再試験
                    $kimatsuSaiGoukaku = $divscore["GAKKI_HYOKA_KANSAN"]["KIMATSU_SAI_PASS_SCORE"]; // 期末再試験合格点
                    $kimatsuScoreNoCheck = $divscore["GAKKI_HYOKA_KANSAN"]["KIMATSU_SCORE_NO_CHECK"]; // 単位認定で期末試験をチェックしない
                    $hyotei = $divscore["GAKKI_HYOKA_KANSAN"]["ASSESSLEVEL"];
                    if ($kimatsuSaiGoukaku == '') {
                        $kimatsuSaiGoukaku = 40;
                    }
                    $hyoteinasi = "";
                    $taishougaiArray = array(); // 更新対象チェック
                    $debugArray = array();
                    if ($takeSemesFlg == "1") {
                        // 前期科目
                        if ($model->field["KARI_SEME1"] != '1') {
                            // レポートの合格回数が規定数未満
                            if ($reportKitei && $reportGokaku < $reportKitei) { $taishougaiArray[] = "report ".$reportGokaku. " < ".$reportKitei ; }
                            // 出席回数が規定回数未満
                            if ($shussekiKitei && $model->attendInfo[$schregno][$subclasscd] < $shussekiKitei) { $taishougaiArray[] = "attend ".$model->attendInfo[$schregno][$subclasscd]." < ".$shussekiKitei; }
                            // ネット視聴ありの科目に〇なし
                            if ($useMedia1 == '1' && $netMaru == '') { $taishougaiArray[] = "net maru nashi"; }
                            // A-D以外 (= E)
                            if ($adIgai == '1') { $taishougaiArray[] = "a-d igai"; }
                            // 平常点がない
                            if ($heijo == '') { $taishougaiArray[] = "heijo nashi"; }
                            // 期末試験が40未満
                            if ($kimatsu == '' || $kimatsu < 40) {
                                $moto = $hyotei;
                                //if ($tval1 == '4') { // 評価がD => 再試験受験資格なし
                                //    $taishougaiArray[] = "kimatsu [".$kimatsu."] tval1= [".$tval1."]";
                                //} else 
                                if ($kimatsuSai >= $kimatsuSaiGoukaku) { // 再試験が合格点以上
                                    // 再試験受験者で合格者は評定2
                                    $hyotei = 2;
                                } else if ($kimatsuSai == '' || $kimatsuSai < $kimatsuSaiGoukaku) { // 再試験が合格点未満
                                    $taishougaiArray[] = "kimatsu [".$kimatsu."] kimatsu sai = [".$kimatsuSai."] kimatsu sai gokaku = [".$kimatsuSaiGoukaku."]  hyotei ".$hyotei." => 1";
                                }
                            }
                        }
                    } else if ($takeSemesFlg == "2") {
                        // 後期、通年科目
                        if ($model->field["KARI_SEME2"] != '1') {
                            if (CTRL_SEMESTER == "1") { // ログイン学期が前期
                                if ($takesemes == '0') { // 通年
                                    // 平常点がない
                                    if ($heijo == '') { $taishougaiArray[] = "heijo nashi"; }
                                }
                            } else {
                                // レポートの合格回数が規定数未満
                                if ($reportGokaku < $reportKitei) { $taishougaiArray[] = "report ".$reportGokaku. " < ".$reportKitei ; }
                                // 出席回数が規定回数未満
                                if ($model->attendInfo[$schregno][$subclasscd] < $shussekiKitei) { $taishougaiArray[] = "attend ".$model->attendInfo[$schregno][$subclasscd]." < ".$shussekiKitei; }
                                // ネット視聴ありの科目に〇なし
                                if ($useMedia1 == '1' && $netMaru == '') { $taishougaiArray[] = "net maru nashi"; }
                                // A-D以外 (= E)
                                if ($adIgai == '1') { $taishougaiArray[] = "a-d igai"; }
                                $classcd = substr($subclasscd, 0, 2);
                                if ($classcd == '90') {
                                    // 平常点をチェックしない
                                } else {
                                    // 平常点がない
                                    if ($heijo == '') { $taishougaiArray[] = "heijo nashi"; }
                                }
                                if ($classcd == '90' || $kimatsuScoreNoCheck == '1') {
                                    // 期末試験をチェックしない
                                } else {
                                    // 期末試験が40未満
                                    if ($kimatsu == '' || $kimatsu < 40) {
                                        $moto = $hyotei;
                                        //if ($tval1 == '4') { // 評価がD => 再試験受験資格なし
                                        //    $taishougaiArray[] = "kimatsu [".$kimatsu."] tval1= [".$tval1."]";
                                        //} else
                                        if ($kimatsuSai >= $kimatsuSaiGoukaku) { // 再試験が合格点以上
                                            // 再試験受験者で合格者は評定2
                                            $hyotei = 2;
                                        } else if ($kimatsuSai == '' || $kimatsuSai < $kimatsuSaiGoukaku) { // 再試験が合格点未満
                                            $taishougaiArray[] = "kimatsu [".$kimatsu."] kimatsu sai = [".$kimatsuSai."] kimatsu sai gokaku = [".$kimatsuSaiGoukaku."]  hyotei ".$hyotei." => 1";
                                        }
                                    }
                                }
                            }
                        }
                    }
                    if (get_count($taishougaiArray) > 0) {
                        //echo $subclasscd." ".$schregno." ";
                        //var_dump($taishougaiArray);
                        //echo "<br>";
                        $hyotei = 1;
                        $credits = 0;
                    }
                    if (get_count($taishougaiArray) == 0) {
                        knjd280aQuery::insertRecordScoreDatSql($db, "9", "08", $subclasscd, $schregno, $divscore["GAKKI_HYOKA_KANSAN"]["SCORE"]);
                    }
                    if ($hyoteinasi == '') {
                        list ($classcd, $sc, $curcd, $subccd) = explode('-', $subclasscd);
                        if ($classcd == '90') {
                            // 総合的な学習の時間は評定をセットしない
                            $hyotei = '';
                        }
                        $query = knjd280aQuery::insertRecordScoreDatSql($db, "9", "09", $subclasscd, $schregno, $hyotei, $credits);
                        if ($takeSemesFlg == "1" && $model->field["KARI_SEME1"] == '1' || $takeSemesFlg == "2" && $model->field["KARI_SEME2"] == '1') {
                            $query = knjd280aQuery::insertRecordProvFlgDatSql($subclasscd, $schregno, CTRL_SEMESTER);
                            $db->query($query);
                        }
                    }
                }
            }
        }
    }
}
?>
