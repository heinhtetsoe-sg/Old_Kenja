<?php

require_once('for_php7.php');
class knjc032eQuery extends Query {
    //学級取得
    function getHrClass($model) {
        $query  = " SELECT ";
        $query .= "     T1.GRADE || '-' || T1.HR_CLASS AS VALUE, ";
        $query .= "     T1.HR_NAME AS LABEL";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_HDAT T1 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR     = '".CTRL_YEAR."' AND ";
        $query .= "     T1.SEMESTER = '".CTRL_SEMESTER."' ";
        if (AUTHORITY != DEF_UPDATABLE) {
            $query .= "     AND (T1.TR_CD1     = '".STAFFCD."' OR ";
            $query .= "          T1.TR_CD2     = '".STAFFCD."' OR ";
            $query .= "          T1.TR_CD3     = '".STAFFCD."' OR ";
            $query .= "          T1.SUBTR_CD1  = '".STAFFCD."' OR ";
            $query .= "          T1.SUBTR_CD2  = '".STAFFCD."' OR ";
            $query .= "          T1.SUBTR_CD3  = '".STAFFCD."') ";
        }
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= " AND GRADE IN (  SELECT ";
                $query .= "                     GRADE ";
                $query .= "                 FROM ";
                $query .= "                     SCHREG_REGD_GDAT S1 ";
                $query .= "                 WHERE ";
                $query .= "                     S1.YEAR         = T1.YEAR AND ";
                $query .= "                     S1.SCHOOL_KIND  IN ('".implode("','", explode(":", $model->selectSchoolKind))."') ";
                $query .= "                 ) ";
            }
        } else if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND GRADE IN (  SELECT ";
            $query .= "                     GRADE ";
            $query .= "                 FROM ";
            $query .= "                     SCHREG_REGD_GDAT S1 ";
            $query .= "                 WHERE ";
            $query .= "                     S1.YEAR         = T1.YEAR AND ";
            $query .= "                     S1.SCHOOL_KIND  = '".SCHOOLKIND."' ";
            $query .= "                 ) ";
        }

        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //複式学級取得
    function getGroupHrClass($model) {
        $query  = " SELECT ";
        $query .= "     GHR_CD AS VALUE, ";
        $query .= "     GHR_NAME AS LABEL";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_GHR_HDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR     = '".CTRL_YEAR."' AND ";
        $query .= "     SEMESTER = '".CTRL_SEMESTER."' ";
        if (AUTHORITY != DEF_UPDATABLE) {
            $query .= "     AND (TR_CD1     = '".STAFFCD."' OR ";
            $query .= "          TR_CD2     = '".STAFFCD."' OR ";
            $query .= "          TR_CD3     = '".STAFFCD."' OR ";
            $query .= "          SUBTR_CD1  = '".STAFFCD."' OR ";
            $query .= "          SUBTR_CD2  = '".STAFFCD."' OR ";
            $query .= "          SUBTR_CD3  = '".STAFFCD."') ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //校種取得
    function getSchoolKind($model) {
        $db = Query::dbCheckOut();

        $query .= "  SELECT ";
        $query .= "    SCHOOL_KIND ";
        $query .= "  FROM ";
        $query .= "    SCHREG_REGD_DAT SD ";
        $query .= "    INNER JOIN ";
        $query .= "    SCHREG_REGD_GDAT SGD";
        $query .= "      ON SD.YEAR     = SGD.YEAR ";
        $query .= "      AND SD.GRADE   = SGD.GRADE ";
        $query .= "  WHERE ";
        $query .= "    SD.SCHREGNO      = '". $model->field["schregno"] ."' ";
        $query .= "    AND SD.YEAR      = '". CTRL_YEAR ."' ";
        $query .= "    AND SD.SEMESTER  = '". CTRL_SEMESTER ."' ";

        $school_kind = $db->getOne($query);
        Query::dbCheckIn($db);

        return $school_kind;
    }

    //生徒取得
    function getStudent($model) {
        if ($model->Properties["useSpecial_Support_Hrclass"] == 1 && $model->field["SELECT_CLASS_TYPE"] == 2) {
            //複式学級
            $query  = " SELECT ";
            $query .= "     T1.SCHREGNO AS VALUE, ";
            $query .= "     T1.GHR_ATTENDNO || ' ' || T2.NAME_SHOW AS LABEL ";
            $query .= " FROM ";
            $query .= "     SCHREG_REGD_GHR_DAT T1 ";
            $query .= "     LEFT OUTER JOIN SCHREG_BASE_MST T2 ON T1.SCHREGNO = T2.SCHREGNO ";
            $query .= " WHERE ";
            $query .= "     T1.YEAR     = '".CTRL_YEAR."' AND ";
            $query .= "     T1.SEMESTER = '".CTRL_SEMESTER."' AND ";
            $query .= "     T1.GHR_CD   = '".$model->field["GROUP_HR_CLASS"]."' ";
            $query .= " ORDER BY ";
            $query .= "     GHR_ATTENDNO ";
        } else {
            $query  = " SELECT ";
            $query .= "     T1.SCHREGNO AS VALUE, ";
            $query .= "     T1.ATTENDNO || ' ' || T2.NAME_SHOW AS LABEL ";
            $query .= " FROM ";
            $query .= "     SCHREG_REGD_DAT T1 ";
            $query .= "     LEFT OUTER JOIN SCHREG_BASE_MST T2 ON T1.SCHREGNO = T2.SCHREGNO ";
            $query .= " WHERE ";
            $query .= "     T1.YEAR     = '".CTRL_YEAR."' AND ";
            $query .= "     T1.SEMESTER = '".CTRL_SEMESTER."' AND ";
            $query .= "     T1.GRADE    = '".$model->field["grade"]."' AND ";
            $query .= "     T1.HR_CLASS = '".$model->field["class"]."' ";
            $query .= " ORDER BY ";
            $query .= "     ATTENDNO ";
        }

        return $query;
    }

    //欠課種別取得
    function getSickDiv() {
        $query  = " SELECT ";
        $query .= "     NAMECD2 AS VALUE, ";
        $query .= "     NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR    = '".CTRL_YEAR."' AND ";
        $query .= "     NAMECD1 = 'C001' AND ";
        $query .= "     NAMECD2 IN ('4', '5', '6') ";
        $query .= " ORDER BY ";
        $query .= "    VALUE ";

        return $query;
    }

    //名称マスタ取得
    function getNameMst($namecd1) {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR    = '".CTRL_YEAR."' AND ";
        $query .= "     NAMECD1 = '".$namecd1."' ";
        $query .= " ORDER BY ";
        $query .= "     NAMECD2 ";

        return $query;
    }

    //学校マスタ取得
    function getSchoolMst() {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     SCHOOL_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";

        return $query;
    }

    //出欠月別累積データ・管理者コントロールデータ
    function selectAttendQuery($model, $month, $sem, $schoolMst) {
        $setNameCd = "Z005";
        if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $setNameCd = "Z".SCHOOLKIND."05";
        }
        $query  = " SELECT ";
        $query .= "     T2.YEAR, ";
        $query .= "     T1.NAMECD2 AS MONTH, ";
        $query .= "     T1.NAME1 AS MONTH_NAME, ";
        $query .= "     T1.NAMESPARE1, ";
        $query .= "     T2.SEMESTER, ";
        $query .= "     T4.APPOINTED_DAY, ";
        $query .= "     T2.LESSON, ";
        $query .= "     T2.OFFDAYS, ";
        $query .= "     T2.ABROAD, ";
        $query .= "     T2.ABSENT, ";
        $query .= "     T2.SUSPEND, ";
        if ($model->Properties["useKoudome"] == "true") {
            $query .= "     T2.KOUDOME, ";
        }
        if ($model->Properties["useVirus"] == "true") {
            $query .= "     T2.VIRUS, ";
        }
        $query .= "     T2.MOURNING, ";
        $query .= "     T2.SICK, ";
        $query .= "     T2.NOTICE, ";
        $query .= "     T2.NONOTICE, ";
        $query .= "     T2.LATE, ";
        $query .= "     T2.EARLY, ";
        if ($model->Properties["use_Attend_zero_hyoji"] == "1") {
            $query .= "     L1.CNT AS DETAIL_001, ";
            $query .= "     L2.CNT AS DETAIL_002, ";
            $query .= "     L3.CNT AS DETAIL_003, ";
            $query .= "     L4.CNT AS DETAIL_004, ";
        } else {
            $query .= "     T2.REIHAI_KEKKA AS DETAIL_001, ";
            $query .= "     T2.M_KEKKA_JISU AS DETAIL_002, ";
            $query .= "     T2.REIHAI_TIKOKU AS DETAIL_003, ";
            $query .= "     T2.JYUGYOU_TIKOKU AS DETAIL_004, ";
        }
        $query .= "     T5.REMARK1 AS REMARK, ";
        $query .= "     T3.CONTROL_CODE, ";
        $query .= "     T2.SCHREGNO AS SEM_SCHREGNO ";
        $query .= " FROM ";
        $query .= "     (SELECT DISTINCT ";
        $query .= "         NAMECD2, ";
        $query .= "         NAME1, ";
        $query .= "         NAMESPARE1 ";
        $query .= "     FROM ";
        $query .= "         NAME_MST ";
        $query .= "     WHERE ";
        $query .= "         NAMECD1 = '{$setNameCd}' AND ";
        $query .= "         NAMECD2 = '".sprintf('%02d',$month)."' ";
        $query .= "     ORDER BY ";
        $query .= "         NAMESPARE1 ";
        $query .= "     ) T1 ";
        $query .= "     LEFT JOIN ( SELECT ";
        $query .= "                     * ";
        $query .= "                 FROM ";
        $query .= "                     V_ATTEND_SEMES_DAT ";
        $query .= "                 WHERE ";
        $query .= "                     YEAR        = '".CTRL_YEAR."' AND ";
        $query .= "                     SEMESTER    = '".$sem."' AND ";
        $query .= "                     SCHREGNO    = '".$model->field["schregno"]."' ";
        $query .= "                 ) T2 ON T2.MONTH = T1.NAMECD2 ";
        $query .= "     LEFT JOIN ( SELECT ";
        $query .= "                     CONTROL_CODE ";
        $query .= "                 FROM ";
        $query .= "                     ADMIN_CONTROL_DAT ";
        $query .= "                 WHERE ";
        $query .= "                     YEAR        = '".CTRL_YEAR."' AND ";
        if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= "                     SCHOOL_KIND = '".SCHOOLKIND."' AND ";
        }
        $query .= "                     CONTROL_FLG = '2'  ";
        $query .= "                 ) T3 ON T1.NAMECD2 = T3.CONTROL_CODE ";
        $query .= "     LEFT JOIN ( SELECT ";
        $query .= "                     MONTH, ";
        $query .= "                     APPOINTED_DAY ";
        $query .= "                 FROM ";
        $query .= "                     APPOINTED_DAY_MST ";
        $query .= "                 WHERE ";
        $query .= "                     YEAR        = '".CTRL_YEAR."' AND ";
        if ($model->Properties["useSchool_KindField"] == "1") {
            $query .= "                     SCHOOL_KIND = '".$model->school_kind."' AND ";
        }
        $query .= "                     SEMESTER    = '".$sem."' ";
        $query .= "                 ) T4 ON T1.NAMECD2 = T4.MONTH ";
        $query .= "     LEFT JOIN ATTEND_SEMES_REMARK_DAT T5 ON T2.YEAR     = T5.YEAR ";
        $query .= "                                         AND T2.SEMESTER = T5.SEMESTER ";
        $query .= "                                         AND T1.NAMECD2  = T5.MONTH ";
        $query .= "                                         AND T2.SCHREGNO = T5.SCHREGNO ";
        $query .= "     LEFT JOIN ATTEND_SEMES_DETAIL_DAT L1 ";
        $query .= "          ON T2.COPYCD   = L1.COPYCD ";
        $query .= "         AND T2.YEAR     = L1.YEAR ";
        $query .= "         AND T2.MONTH    = L1.MONTH ";
        $query .= "         AND T2.SEMESTER = L1.SEMESTER ";
        $query .= "         AND T2.SCHREGNO = L1.SCHREGNO ";
        $query .= "         AND L1.SEQ      = '001' ";
        $query .= "     LEFT JOIN ATTEND_SEMES_DETAIL_DAT L2 ";
        $query .= "          ON T2.COPYCD   = L2.COPYCD ";
        $query .= "         AND T2.YEAR     = L2.YEAR ";
        $query .= "         AND T2.MONTH    = L2.MONTH ";
        $query .= "         AND T2.SEMESTER = L2.SEMESTER ";
        $query .= "         AND T2.SCHREGNO = L2.SCHREGNO ";
        $query .= "         AND L2.SEQ      = '002' ";
        $query .= "     LEFT JOIN ATTEND_SEMES_DETAIL_DAT L3 ";
        $query .= "          ON T2.COPYCD   = L3.COPYCD ";
        $query .= "         AND T2.YEAR     = L3.YEAR ";
        $query .= "         AND T2.MONTH    = L3.MONTH ";
        $query .= "         AND T2.SEMESTER = L3.SEMESTER ";
        $query .= "         AND T2.SCHREGNO = L3.SCHREGNO ";
        $query .= "         AND L3.SEQ      = '003' ";
        $query .= "     LEFT JOIN ATTEND_SEMES_DETAIL_DAT L4 ";
        $query .= "          ON T2.COPYCD   = L4.COPYCD ";
        $query .= "         AND T2.YEAR     = L4.YEAR ";
        $query .= "         AND T2.MONTH    = L4.MONTH ";
        $query .= "         AND T2.SEMESTER = L4.SEMESTER ";
        $query .= "         AND T2.SCHREGNO = L4.SCHREGNO ";
        $query .= "         AND L4.SEQ      = '004' ";
        $query .= " ORDER BY ";
        $query .= "     T1.NAMESPARE1 ";

        return $query;
    }

    //締め日の取得
    function getAppointedDay($month, $semester, $model) {
        $query  = " SELECT ";
        $query .= "     APPOINTED_DAY ";
        $query .= " FROM ";
        $query .= "     APPOINTED_DAY_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR        = '".CTRL_YEAR."' AND ";
        if ($model->Properties["useSchool_KindField"] == "1") {
            $query .= "     SCHOOL_KIND = '".$school_kind."' AND ";
        }
        $query .= "     MONTH       = '".$month."' AND ";
        $query .= "     SEMESTER    = '".$semester."' ";

        return $query;
    }

    //異動データ（退学・転学・卒業）取得
    function getIdouData($schregno, $date) {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     SCHREG_BASE_MST ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO = '".$schregno."' AND ";
        $query .= "     GRD_DIV IN ('1', '2', '3', '6') AND ";
        $query .= "     GRD_DATE < '".$date."' ";

        return $query;
    }

    //異動データ（留学・休学）取得
    function getTransferData1($schregno, $date) {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     SCHREG_TRANSFER_DAT ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO = '".$schregno."' AND ";
        $query .= "     TRANSFERCD IN ('1', '2') AND ";
        $query .= "     '".$date."' BETWEEN TRANSFER_SDATE AND CASE WHEN TRANSFER_EDATE IS NULL THEN '".(CTRL_YEAR+1)."' || '-03-31' ELSE TRANSFER_EDATE END ";

        return $query;
    }

    //異動日付（留学・休学）取得
    function getTransferData2($schregno, $month) {
        $query  = " SELECT ";
        if($month == "s") {
            $query .= "     MONTH(TRANSFER_SDATE) AS S_MONTH ";
        } else {
            $query .= "     MONTH(TRANSFER_EDATE) AS E_MONTH ";
        }
        $query .= " FROM ";
        $query .= "     SCHREG_TRANSFER_DAT ";
        $query .= " WHERE ";
        $query .= "     SCHREGNO = '".$schregno."' AND ";
        $query .= "     TRANSFERCD IN ('1', '2') AND ";
        $query .= "     TRANSFER_SDATE BETWEEN '".CTRL_YEAR."-04-01' AND '".(CTRL_YEAR+1)."-03-31' ";

        return $query;
    }

    //学期・月範囲取得
    function selectSemesAll($seme="") {
        $query  = " SELECT ";
        $query .= "     SEMESTER, ";
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
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        if (!$seme) {
            $query .= "     AND SEMESTER <> '9' ";
        } else {
            $query .= "     AND SEMESTER = '".$seme."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     SEMESTER ";

        return $query;
    }

    //出欠累積データ学期計・累積計
    function selectAttendSemester($model, $semester) {
        if ($semester == 0 && !$model->field["schregno"]) {
            $query  = "VALUES ('','','','','','','','','','','','','','')";
        } else if ($semester > 0 && !$model->field["schregno"]) {
            $query  = "SELECT ";
            $query .= "     SEMESTERNAME || '計' AS MONTH_NAME ";
            $query .= "FROM ";
            $query .= "     SEMESTER_MST ";
            $query .= "WHERE ";
            $query .= "     YEAR     = '".CTRL_YEAR."' AND ";
            $query .= "     SEMESTER = '".$semester."' ";
        } else {
            $query  = " SELECT ";
            if ($semester > 0) {
                $query .= "     T1.SEMESTERNAME || '計' AS MONTH_NAME, ";
            }
            $query .= "     '' AS APPOINTED_DAY, ";
            $query .= "     sum(T2.LESSON) AS LESSON, ";
            $query .= "     sum(T2.OFFDAYS) AS OFFDAYS, ";
            $query .= "     sum(T2.ABROAD) AS ABROAD, ";
            $query .= "     sum(T2.ABSENT) AS ABSENT, ";
            $query .= "     sum(T2.SUSPEND) AS SUSPEND, ";
            $query .= "     sum(T2.KOUDOME) AS KOUDOME, ";
            $query .= "     sum(T2.VIRUS) AS VIRUS, ";
            $query .= "     sum(T2.MOURNING) AS MOURNING, ";
            $query .= "     0 AS CLASSDAYS2, ";
            $query .= "     sum(T2.SICK) AS SICK, ";
            $query .= "     sum(T2.NOTICE) AS NOTICE, ";
            $query .= "     sum(T2.NONOTICE) AS NONOTICE, ";
            $query .= "     0 AS CLASSDAYS3, ";
            $query .= "     sum(T2.LATE) AS LATE, ";
            $query .= "     sum(T2.EARLY) AS EARLY, ";
            $query .= "     sum(T2.REIHAI_KEKKA) AS DETAIL_001, ";
            $query .= "     sum(T2.M_KEKKA_JISU) AS DETAIL_002, ";
            $query .= "     sum(T2.REIHAI_TIKOKU) AS DETAIL_003, ";
            $query .= "     sum(T2.JYUGYOU_TIKOKU) AS DETAIL_004, ";
            $query .= "     '' AS REMARK ";
            $query .= " FROM ";
            $query .= "     SEMESTER_MST T1 ";
            $query .= "     LEFT JOIN V_ATTEND_SEMES_DAT T2 ON T1.YEAR     = T2.YEAR ";
            $query .= "                                    AND T1.SEMESTER = T2.SEMESTER ";
            $query .= "                                    AND T2.SCHREGNO = '".$model->field["schregno"]."' ";
            $query .= " WHERE ";
            $query .= "     T1.YEAR = '".CTRL_YEAR."' ";
            if ($semester > 0) {
                $query .= "     AND T1.SEMESTER = '".$semester."' ";
                $query .= " GROUP BY ";
                $query .= "     T1.SEMESTERNAME ";
            }
        }

        return $query;
    }

    //フィールド一覧取得
    function getFieldList() {
        $query  = " WITH MAIN AS ( ";
        $query .= "     SELECT ";
        $query .= "         COLNO, ";
        $query .= "         NAME ";
        $query .= "     FROM ";
        $query .= "         SYSIBM.SYSCOLUMNS ";
        $query .= "     WHERE ";
        $query .= "         TBNAME  = 'ATTEND_SEMES_DAT' AND ";
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

    //出欠月別累積データの更新
    function getUpdateQuery($model) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //フィールド取得
        $query = knjc032eQuery::getFieldList();
        $table_field = array();
        $table_field = $db->getCol($query);

        foreach ($model->field["MONTH"] as $key => $month) {
            $monthAr = preg_split("/-/", $month);
            //更新対象データが存在するかをチェック
            $query  = " SELECT ";
            $query .= "     COUNT(*) ";
            $query .= " FROM ";
            $query .= "     ATTEND_SEMES_DAT ";
            $query .= " WHERE ";
            $query .= "     YEAR     = '".CTRL_YEAR."' AND ";
            $query .= "     SEMESTER = '".$monthAr[1]."' AND ";
            $query .= "     MONTH    = '".$monthAr[0]."' AND ";
            $query .= "     SCHREGNO = '".$model->field["schregno"]."' ";

            $flag = $db->getOne($query);
            $data = array();

            if ($flag) {
                $where  = " WHERE ";
                $where .= "     YEAR     = '".CTRL_YEAR."' AND ";
                $where .= "     SEMESTER = '".$monthAr[1]."' AND ";
                $where .= "     MONTH    = '".$monthAr[0]."' AND ";
                $where .= "     SCHREGNO = '".$model->field["schregno"]."' ";
            } else {
                /* 更新対象データがない時 - INSERT */
                $data["COPYCD"][TEXT]   = "0";
                $data["YEAR"][TEXT]     = CTRL_YEAR;
                $data["MONTH"][TEXT]    = $monthAr[0];
                $data["SEMESTER"][TEXT] = $monthAr[1];
                $data["SCHREGNO"][TEXT] = $model->field["schregno"];
            }
            /* 更新データをセット */
            $data["APPOINTED_DAY"][TEXT] = $model->field["APPOINTED_DAY"][$key];     //締め日
            $data["LESSON"][NUMBER]      = ($model->field["LESSON"][$key]) ? $model->field["LESSON"][$key] : 0;      //授業日数
            foreach ($table_field as $fld) {
                if ($model->Properties["use_Attend_zero_hyoji"] == "1") {
                    $data[$fld][NUMBER]      = $model->field[$fld][$key];
                } else {
                    $data[$fld][NUMBER]      = ($model->field[$fld][$key]) ? $model->field[$fld][$key] : 0;
                }
            }
            $data["REGISTERCD"][TEXT]    = STAFFCD ;            //登録者コード
            $data["UPDATED"][FUNC]       = "sysdate()";         //更新日付

            if ("" != $model->field["APPOINTED_DAY"][$key]) {
                //更新対象データがある時 - UPDATE
                if($flag) {
                    $query = Query::updateSQL($data, "ATTEND_SEMES_DAT", $where);
                //更新対象データがない時 - INSERT
                }  else {
                    $query = Query::insertSQL($data, "ATTEND_SEMES_DAT");
                }
            } else {
                if ($flag) {
                    $query = " DELETE FROM ATTEND_SEMES_DAT ".$where;
                }
            }
            $db->query($query);
        }

        $db->commit();
        Query::dbCheckIn($db);
        return true;
    }
}
?>
