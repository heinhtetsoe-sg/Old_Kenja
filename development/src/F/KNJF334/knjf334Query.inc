<?php

require_once('for_php7.php');

class knjf334Query extends Query {

    //教育委員会判定
    function z010Abbv1() {
        $query  = " SELECT ";
        $query .= "      ABBV1 ";
        $query .= " FROM ";
        $query .= "      NAME_MST ";
        $query .= " WHERE ";
        $query .= "      NAMECD1 = 'Z010' ";
        $query .= "      AND NAMECD2 = '00' ";

        return $query;
    }

    //教育委員会用の学校コード取得
    function getSchoolMst() {

        $db = Query::dbCheckOut();

        //教育委員会判定
        $query = knjf334Query::z010Abbv1();
        $z010Abbv1 = $db->getOne($query);

        $retSchool = "";
        if ($z010Abbv1 == "1" || $z010Abbv1 == "2") {
            //V_SCHOOL_MSTから学校コードを取得
            $query  = " SELECT ";
            $query .= "      KYOUIKU_IINKAI_SCHOOLCD ";
            $query .= " FROM ";
            $query .= "      V_SCHOOL_MST ";
            $query .= " WHERE ";
            $query .= "      YEAR    = '".CTRL_YEAR."' ";

            $retSchool = $db->getOne($query);
        } else {
            $retSchool = "000000000000";
        }

        Query::dbCheckIn($db);

        return $retSchool;
    }

    //Z010
    function getZ010()
    {
        $query  = "SELECT NAME1, NAMESPARE2 FROM NAME_MST WHERE NAMECD1 = 'Z010' AND NAMECD2 = '00' ";
        return $query;
    }

    //学年の取得
    function getSchGDatName($model) {
        $query  = " SELECT ";
        $query .= "   GRADE_NAME ";
        $query .= " FROM ";
        $query .= "   SCHREG_REGD_GDAT ";
        $query .= " WHERE ";
        $query .= "   YEAR = '".CTRL_YEAR."' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= "   AND SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
            }
        } else if ($model->Properties["useSchool_KindField"] == "1") {
            $query .= "   AND SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= " ORDER BY ";
        $query .= "   SCHOOL_KIND DESC, ";
        $query .= "   GRADE ";
        
        return $query;
    }

    //学習記録データの存在確認
    function getRecSchregno()
    {
        $query  = "SELECT COUNT(schregno) FROM schreg_studyrec_dat ";
        $query .= " WHERE year = '".CTRL_YEAR."'";
        return $query;
    }
    //学籍在籍データの存在確認
    function getSchSchregno()
    {
        $query  = "SELECT COUNT(schregno) FROM schreg_regd_dat ";
        $query .= " WHERE year     = '".CTRL_YEAR."'";
        $query .= "   AND semester = '".CTRL_SEMESTER."'";
        return $query;
    }
    //評定マスタの存在確認
    function getAssesscd()
    {
        $query  = "SELECT COUNT(assesscd) FROM assess_mst ";
        $query .= " WHERE assesscd = '4' ";
        return $query;
    }

    //データ表示
    function ReadQuery($model) {
        $query  = "";
        // 他のKNJF331等の真似をしようとしたが、そもそも利用が年齢ではなく学年になるので、扱いが全く違う。
        // 人数として途中学期から転校してきた生徒も対象にする?
        // ->対象と考えて、当年度最小学期のデータを引っ張る。(当年度の学年は、その年の最初の学期の学年で検診のはず)
        $query .= " WITH SCHREG_MINSEMES_TBL AS ( ";
        $query .= "   SELECT ";
        $query .= "     YEAR, ";
        $query .= "     MIN(SEMESTER) AS M_SEMES, ";
        $query .= "     SCHREGNO, ";
        $query .= "     GRADE ";
        $query .= "   FROM ";
        $query .= "     SCHREG_REGD_DAT ";
        $query .= "   WHERE ";
        $query .= "     YEAR = '2006' ";
        $query .= "   GROUP BY ";
        $query .= "     YEAR, ";
        $query .= "     SCHREGNO, ";
        $query .= "     GRADE ";
        $query .= " ), T_GRADE AS ( ";
        $query .= "   SELECT ";
        $query .= "     SCHOOL_KIND, ";
        $query .= "     GRADE, ";
        $query .= "     GRADE_CD, ";
        $query .= "     GRADE_NAME1";
        $query .= "   FROM ";
        $query .= "     SCHREG_REGD_GDAT ";
        $query .= "   WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= "           AND SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
            }
        } else if ($model->Properties["useSchool_KindField"] == "1") {
            $query .= "           AND SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= " UNION ";
        $query .= " SELECT DISTINCT ";
        $query .= "   '' AS SCHOOL_KIND, ";
        $query .= "   TV1.GRADE, ";
        $query .= "   '' AS GRADE_CD, ";
        $query .= "   '合計' AS GRADE_NAME1 ";
        $query .= " FROM ";
        if ($model->fixedData) {
            $query .= "         MEDEXAM_DISEASE_ADDITION334_FIXED_DAT TV1 ";
        } else {
            $query .= "         MEDEXAM_DISEASE_ADDITION334_DAT TV1 ";
        }
        $query .= " WHERE ";
        $query .= "   TV1.YEAR = '".CTRL_YEAR."' ";
        $query .= "   AND TV1.GRADE = '99' ";

        $query .= " ) ";
        if ($model->cmd == "recalc") {
            $query .= " , T_RECALC AS ( ";
            //再計算
            $query .= "     SELECT ";
            $query .= "         T1.GRADE, ";
            $query .= "         SUM(1) AS DATA000_02, ";
            $query .= "         SUM(CASE WHEN L1.SCHREGNO IS NOT NULL THEN 1 ELSE 0 END) AS DATA000_01, ";  //在籍生徒数(2019/04/01時点の在籍として計上。その後の異動は手入力可のため、無視。)
            $query .= "         0 AS DATA001_02, ";  //非計算(手入力)項目
            $query .= "         0 AS DATA001_03, ";  //非計算(手入力)項目
            $query .= "         0 AS DATA001_04, ";  //非計算(手入力)項目
            $query .= "         SUM(CASE WHEN F120.ABBV3 = '10' OR F130.ABBV3 = '10' THEN 1 ELSE 0 END) AS DATA010_05, ";
            $query .= "         SUM(CASE WHEN F100.ABBV3 <> '20' THEN 1 ELSE 0 END) AS DATA011_06, ";  //KNJF331には無い。
            $query .= "         SUM(CASE WHEN F100.ABBV3 = '20' THEN 1 ELSE 0 END) AS DATA011_07, ";
            $query .= "         SUM(CASE WHEN F100.ABBV3 = '10' THEN 1 ELSE 0 END) AS DATA011_08 ";
            $query .= "     FROM ";
            $query .= "         SCHREG_MINSEMES_TBL T1 ";
            $query .= "         INNER JOIN SCHREG_BASE_MST T2 ";
            $query .= "             ON  T2.SCHREGNO = T1.SCHREGNO ";
            $query .= "         INNER JOIN SCHREG_REGD_GDAT T3 ";
            $query .= "             ON  T3.YEAR     = T1.YEAR ";
            $query .= "             AND T3.GRADE    = T1.GRADE ";
            if ($model->Properties["use_prg_schoolkind"] == "1") {
                if ($model->selectSchoolKind) {
                    $query .= "             AND T3.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
                }
            } else if ($model->Properties["useSchool_KindField"] == "1") {
                $query .= "             AND T3.SCHOOL_KIND    = '".SCHOOLKIND."' ";
            }
            $query .= "         LEFT JOIN MEDEXAM_DET_DAT L1 ";
            $query .= "             ON  L1.YEAR     = T1.YEAR ";
            $query .= "             AND L1.SCHREGNO = T1.SCHREGNO ";
            $query .= "         LEFT JOIN NAME_MST F120 ON F120.NAMECD1 = 'F120' ";
            $query .= "              AND F120.NAMECD2 = L1.TB_NAMECD ";
            $query .= "         LEFT JOIN NAME_MST F130 ON F130.NAMECD1 = 'F130' ";
            $query .= "              AND F130.NAMECD2 = L1.TB_ADVISECD ";
            $query .= "         LEFT JOIN NAME_MST F100 ON F100.NAMECD1 = 'F100' ";
            $query .= "              AND F100.NAMECD2 = L1.TB_REMARKCD ";
            $query .= "     WHERE ";
            $query .= "             T1.YEAR     = '".CTRL_YEAR."' ";
            $query .= "     GROUP BY ";
            $query .= "         GROUPING SETS ((T1.GRADE), ()) ";
            $query .= "     ) ";
        } else {
            //登録テーブル
            $query .= " , T_ADDITION1 AS ( ";
            $query .= "     SELECT ";
            $query .= "         * ";
            $query .= "     FROM ";
            if ($model->fixedData) {
                $query .= "         MEDEXAM_DISEASE_ADDITION334_FIXED_DAT ";
            } else {
                $query .= "         MEDEXAM_DISEASE_ADDITION334_DAT ";
            }
            $query .= "     WHERE ";
            $query .= "         EDBOARD_SCHOOLCD = '".$model->schoolcd."' ";
            $query .= "         AND YEAR = '".CTRL_YEAR."' ";
            if ($model->fixedData) {
                $query .= "         AND FIXED_DATE = '".str_replace("/", "-", $model->fixedData)."' ";
            }
            $query .= "     ) ";
        }

        //メイン
        $query .= " SELECT ";
        $query .= "     T1.GRADE, ";
        $query .= "     T1.SCHOOL_KIND, ";
        $query .= "     T1.GRADE_CD, ";
        $query .= "     T1.GRADE_NAME1 ";
        foreach ($model->dataField as $key => $divSeq) {
            if ($model->cmd == "recalc") {
                $query .= "     ,L1.DATA{$divSeq} ";
            } else {
                $query .= "     ,L{$divSeq}.INT_VAL AS DATA{$divSeq} ";
            }
        }
        $query .= " FROM ";
        $query .= "     T_GRADE T1 ";
        if ($model->cmd == "recalc") {
            $query .= "     LEFT JOIN T_RECALC L1 ON L1.GRADE = T1.GRADE ";
        } else {
            foreach ($model->dataField as $key => $divSeq) {
                list($dataDiv, $seq) = preg_split("/_/", $divSeq);
                $query .= "     LEFT JOIN T_ADDITION1 L{$divSeq} ON L{$divSeq}.GRADE = T1.GRADE ";
                $query .= "          AND L{$divSeq}.SEQ = '{$seq}' ";
            }
        }
        $query .= " ORDER BY ";
        $query .= "     T1.SCHOOL_KIND DESC, ";
        $query .= "     T1.GRADE ";

        return $query;
    }

    //UPDATE
    function &getUpdateQuery($model) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //DELETE
        foreach ($model->dataField as $key => $divSeq) {
            list($dataDiv, $seq) = preg_split("/_/", $divSeq);
            $query  = "DELETE FROM MEDEXAM_DISEASE_ADDITION334_DAT ";
            $query .= " WHERE EDBOARD_SCHOOLCD  = '".$model->schoolcd."'";
            $query .= "   AND YEAR     = '".CTRL_YEAR."'";
            $query .= "   AND SEQ      = '{$seq}'";
            $db->query($query);
        }

        //INSERT
        foreach ($model->updField as $key => $val) {
            list($dataDiv, $seq, $grade) = preg_split("/_/", $key);
            $schoolkind = $model->hidField["SCHKIND_DATA".$key];
            $gradecd = $model->hidField["GCD_DATA".$key];

            $data = array(); //初期化
            $data["EDBOARD_SCHOOLCD"][TEXT]     = $model->schoolcd;
            $data["YEAR"][TEXT]                 = CTRL_YEAR;
            $data["GRADE"][TEXT]                = $grade;
            $data["SCHOOL_KIND"][TEXT]          = $schoolkind;
            $data["GRADE_CD"][TEXT]             = $gradecd;
            $data["SEQ"][TEXT]                  = $seq;

            //理由は解らないが、サーバーにより
            //$valが配列になる場合とならない場合がある。
            //tokioでは、配列となっているが73サーバーではならない
            $data["INT_VAL"][NUMBER]            = is_array($val) ? $val[0] : $val;

            $data["REGISTERCD"][TEXT]           = STAFFCD;
            $data["UPDATED"][NUMBER]            =" sysdate()";

            $query  = Query::insertSQL($data, "MEDEXAM_DISEASE_ADDITION334_DAT");
            $db->query($query);
        }

        $db->commit();
        Query::dbCheckIn($db);
        return;
    }

    //確定UPDATE
    function &getFixedUpdateQuery($model) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //県側、学校側・報告テーブル
        $query  = "DELETE FROM MEDEXAM_DISEASE_ADDITION334_FIXED_DAT ";
        $query .= " WHERE EDBOARD_SCHOOLCD  = '".$model->schoolcd."'";
        $query .= "   AND YEAR              = '".CTRL_YEAR."'";
        $query .= "   AND FIXED_DATE        = '".str_replace("/", "-", $model->fixedDate)."'";
        $db->query($query);

        //INSERT
        $query = knjf334Query::getKensinDat($model);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            //キー
            $data = array(); //初期化
            $data["EDBOARD_SCHOOLCD"][TEXT]     = $row["EDBOARD_SCHOOLCD"];
            $data["YEAR"][TEXT]                 = $row["YEAR"];
            $data["FIXED_DATE"][DATE]           = $model->fixedDate;
            $data["GRADE"][TEXT]                = $row["GRADE"];
            $data["SCHOOL_KIND"][TEXT]          = $row["SCHOOL_KIND"];
            $data["GRADE_CD"][TEXT]             = $row["GRADE_CD"];
            $data["SEQ"][TEXT]                  = $row["SEQ"];
            //人数
            $data["INT_VAL"][NUMBER]            = $row["INT_VAL"];
            $data["CHAR_VAL"][TEXT]             = $row["CHAR_VAL"];
            $data["REGISTERCD"][TEXT]           = STAFFCD;
            $data["UPDATED"][NUMBER]            =" sysdate()";
            $query  = Query::insertSQL($data, "MEDEXAM_DISEASE_ADDITION334_FIXED_DAT");
            $db->query($query);

        }
        $result->free();

        $db->commit();
        Query::dbCheckIn($db);
        return;
    }

    //県への報告履歴コンボ
    function getReport($model) {
        $query  = " SELECT ";
        $query .= "     T1.EXECUTE_DATE AS LABEL, ";
        $query .= "     T1.EXECUTE_DATE AS VALUE ";
        $query .= " FROM ";
        $query .= "     REPORT_DISEASE_ADDITION334_DAT T1";
        $query .= " WHERE ";
        $query .= "         T1.EDBOARD_SCHOOLCD = '".$model->schoolcd."' ";
        $query .= "     AND T1.YEAR             = '".CTRL_YEAR."' ";
        $query .= " ORDER BY ";
        $query .= "     T1.EXECUTE_DATE DESC ";

        return $query;
    }

    //確定コンボ
    function getFixed($model) {
        $query  = " SELECT ";
        $query .= "     T1.FIXED_DATE AS LABEL, ";
        $query .= "     T1.FIXED_DATE AS VALUE ";
        $query .= " FROM ";
        $query .= "     MEDEXAM_DISEASE_ADDITION334_FIXED_DAT T1";
        $query .= " WHERE ";
        $query .= "         T1.EDBOARD_SCHOOLCD = '".$model->schoolcd."' ";
        $query .= "     AND T1.YEAR             = '".CTRL_YEAR."' ";
        $query .= " GROUP BY ";
        $query .= "     T1.FIXED_DATE ";
        $query .= " ORDER BY ";
        $query .= "     T1.FIXED_DATE DESC ";

        return $query;
    }

    //学校側から県側へコピーするデータ取得（疾病等結果一覧）
    function getKensinDat($model) {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     MEDEXAM_DISEASE_ADDITION334_DAT ";
        $query .= " WHERE ";
        $query .= "     EDBOARD_SCHOOLCD = '".$model->schoolcd."' ";
        $query .= "     AND YEAR = '".CTRL_YEAR."' ";

        return $query;
    }

    //学校側から県側へコピーするデータ取得（疾病等結果一覧）
    function getKensinFixedDat($model) {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     MEDEXAM_DISEASE_ADDITION334_FIXED_DAT ";
        $query .= " WHERE ";
        $query .= "     EDBOARD_SCHOOLCD = '".$model->schoolcd."' ";
        $query .= "     AND YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND FIXED_DATE = '".str_replace("/", "-", $model->fixedData)."' ";

        return $query;
    }

    //UPDATE
    function &getUpdateReport($model) {
        $db = Query::dbCheckOut();
        $db2 = Query::dbCheckOut2();
        $db->autoCommit(false);
        $db2->autoCommit(false);

        //県側、学校側・報告テーブル
        $query  = "DELETE FROM REPORT_DISEASE_ADDITION334_DAT ";
        $query .= " WHERE EDBOARD_SCHOOLCD  = '".$model->schoolcd."'";
        $query .= "   AND YEAR              = '".CTRL_YEAR."'";
        $query .= "   AND EXECUTE_DATE = '".str_replace("/", "-", $model->execute_date)."' ";
        $db->query($query);
        $db2->query($query);

        //県側、学校側・報告テーブル
        //キー
        $data = array(); //初期化
        $data["EDBOARD_SCHOOLCD"][TEXT]     = $model->schoolcd;
        $data["YEAR"][TEXT]                 = CTRL_YEAR;
        $data["EXECUTE_DATE"][DATE]         = $model->execute_date;
        $data["FIXED_DATE"][DATE]           = $model->fixedData;
        $data["REGISTERCD"][TEXT]           = STAFFCD;
        $data["UPDATED"][NUMBER]            =" sysdate()";
        $query  = Query::insertSQL($data, "REPORT_DISEASE_ADDITION334_DAT");
        $db->query($query);
        $db2->query($query);

        //DELETE
        //県側・疾病等結果一覧テーブル
        $query  = "DELETE FROM MEDEXAM_DISEASE_ADDITION334_FIXED_DAT ";
        $query .= " WHERE EDBOARD_SCHOOLCD  = '".$model->schoolcd."'";
        $query .= "   AND YEAR              = '".CTRL_YEAR."'";
        $query .= "   AND FIXED_DATE = '".str_replace("/", "-", $model->fixedData)."' ";
        $db2->query($query);

        //INSERT
        //学校側から県側へコピーするデータ取得（疾病等結果一覧）
        $query = knjf334Query::getKensinFixedDat($model);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            //県側・疾病等結果一覧テーブル
            //キー
            $data = array(); //初期化
            $data["EDBOARD_SCHOOLCD"][TEXT]     = $row["EDBOARD_SCHOOLCD"];
            $data["YEAR"][TEXT]                 = $row["YEAR"];
            $data["FIXED_DATE"][DATE]           = $row["FIXED_DATE"];
            $data["GRADE"][TEXT]                = $row["GRADE"];
            $data["SCHOOL_KIND"][TEXT]          = $row["SCHOOL_KIND"];
            $data["GRADE_CD"][TEXT]             = $row["GRADE_CD"];
            $data["SEQ"][TEXT]                  = $row["SEQ"];
            //人数
            $data["INT_VAL"][NUMBER]            = $row["INT_VAL"];
            $data["CHAR_VAL"][TEXT]             = $row["CHAR_VAL"];
            $data["REGISTERCD"][TEXT]           = STAFFCD;
            $data["UPDATED"][NUMBER]            =" sysdate()";
            $query  = Query::insertSQL($data, "MEDEXAM_DISEASE_ADDITION334_FIXED_DAT");
            $db2->query($query);
        }
        $result->free();

        $chkVal = $db2->getOne(knjf334Query::getE045PrgDataSql($model));
        $retCnt = $db2->getOne(knjf334Query::getSearchReportSchoolCntSql($model, $chkVal));

        //登録時にデータが無い場合は、委員会側で学校データの登録が必要。
        //少なくとも、登録時には文書番号に対応するREQUEST_ANSWER_PRGに"KNJF334"の登録が必要。
        if ($retCnt > 0 && $chkVal != "") {
            //報告処理
            $data = array();
            $data["ANSWER_FLG"][TEXT]  = "1";
            $data["ANSWER_DATE"][FUNC] = "SYSDATE()";
            $data["REGISTERCD"][TEXT]  = STAFFCD;
            $data["UPDATED"][FUNC]     = "SYSDATE()";

            $where .= " WHERE ";
            $where .= "     YEAR = '".CTRL_YEAR."' AND ";
            $where .= "     DOC_NUMBER = ".$chkVal." AND ";
            $where .= "     EDBOARD_SCHOOLCD = '{$model->schoolcd}' ";

            $query = Query::updateSQL($data, "AFT_SEARCH_REPORT_SCHOOL_DAT", $where);
            $db2->query($query);
        }

        $db->commit();
        $db2->commit();
        Query::dbCheckIn($db);
        Query::dbCheckIn($db2);
        return;
    }

    function getSearchReportSchoolCnt($model, $chkVal) {
        if ($chkVal == "") return 0;
        $db2 = Query::dbCheckOut2();
        $retCnt = $db2->getOne(knjf334Query::getSearchReportSchoolCntSql($model, $chkVal));
        Query::dbCheckIn($db2);
        return $retCnt;
    }

    function getSearchReportSchoolCntSql($model, $chkVal) {
        if ($chkVal == "") $chkVal = 9999;
        $query  = " SELECT COUNT(*) AS CNT FROM AFT_SEARCH_REPORT_SCHOOL_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' AND ";
        $query .= "     DOC_NUMBER = ".$chkVal." AND ";
        $query .= "     EDBOARD_SCHOOLCD = '{$model->schoolcd}' ";
        return $query;
    }

    function getE045PrgData($model) {
        $db2 = Query::dbCheckOut2();
        $retCnt = $db2->getOne(knjf334Query::getE045PrgDataSql($model));
        Query::dbCheckIn($db2);
        return $retCnt;
    }

    function getE045PrgDataSql($model) {
        $query  = "SELECT NAMECD2 FROM NAME_MST WHERE NAMECD1 = 'E045' AND NAME3 = 'KNJF334' ";
        return $query;
    }

    //CSV
    function selectCsvQuery($model) {
        $query  = "";
        // 他のKNJF331等の真似をしようとしたが、そもそも利用が年齢ではなく学年になるので、扱いが全く違う。
        // 人数として途中学期から転校してきた生徒も対象にする?
        // ->対象と考えて、当年度最小学期のデータを引っ張る。(当年度の学年は、その年の最初の学期の学年で検診のはず)
        $query .= " WITH SCHREG_MINSEMES_TBL AS ( ";
        $query .= "   SELECT ";
        $query .= "     YEAR, ";
        $query .= "     MIN(SEMESTER) AS M_SEMES, ";
        $query .= "     SCHREGNO, ";
        $query .= "     GRADE ";
        $query .= "   FROM ";
        $query .= "     SCHREG_REGD_DAT ";
        $query .= "   WHERE ";
        $query .= "     YEAR = '2006' ";
        $query .= "   GROUP BY ";
        $query .= "     YEAR, ";
        $query .= "     SCHREGNO, ";
        $query .= "     GRADE ";
        $query .= " ), T_GRADE AS ( ";
        $query .= "   SELECT ";
        $query .= "     SCHOOL_KIND, ";
        $query .= "     GRADE, ";
        $query .= "     GRADE_CD, ";
        $query .= "     GRADE_NAME1";
        $query .= "   FROM ";
        $query .= "     SCHREG_REGD_GDAT ";
        $query .= "   WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= "           AND SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
            }
        } else if ($model->Properties["useSchool_KindField"] == "1") {
            $query .= "           AND SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= " UNION ";
        $query .= " SELECT DISTINCT ";
        $query .= "   '' AS SCHOOL_KIND, ";
        $query .= "   TV1.GRADE, ";
        $query .= "   '' AS GRADE_CD, ";
        $query .= "   '合計' AS GRADE_NAME1 ";
        $query .= " FROM ";
        if ($model->fixedData) {
            $query .= "         MEDEXAM_DISEASE_ADDITION334_FIXED_DAT TV1 ";
        } else {
            $query .= "         MEDEXAM_DISEASE_ADDITION334_DAT TV1 ";
        }
        $query .= " WHERE ";
        $query .= "   TV1.YEAR = '".CTRL_YEAR."' ";
        $query .= "   AND TV1.GRADE = '99' ";

        $query .= " ) ";
        //登録テーブル
        $query .= " , T_ADDITION1 AS ( ";
        $query .= "     SELECT ";
        $query .= "         * ";
        $query .= "     FROM ";
        if ($model->fixedData) {
            $query .= "         MEDEXAM_DISEASE_ADDITION334_FIXED_DAT ";
        } else {
            $query .= "         MEDEXAM_DISEASE_ADDITION334_DAT ";
        }
        $query .= "     WHERE ";
        $query .= "         EDBOARD_SCHOOLCD = '".$model->schoolcd."' ";
        $query .= "         AND YEAR = '".CTRL_YEAR."' ";
        if ($model->fixedData) {
            $query .= "         AND FIXED_DATE = '".str_replace("/", "-", $model->fixedData)."' ";
        }
        $query .= "     ) ";

        //メイン
        $query .= " SELECT ";
        $query .= "     T1.GRADE_NAME1 ";
        foreach ($model->dataField as $key => $divSeq) {
            $query .= "     ,L{$divSeq}.INT_VAL AS DATA{$divSeq} ";
        }
        $query .= " FROM ";
        $query .= "     T_GRADE T1 ";
        foreach ($model->dataField as $key => $divSeq) {
            list($dataDiv, $seq) = preg_split("/_/", $divSeq);
            $query .= "     LEFT JOIN T_ADDITION1 L{$divSeq} ON L{$divSeq}.GRADE = T1.GRADE ";
            $query .= "          AND L{$divSeq}.SEQ = '{$seq}' ";
        }
        $query .= " ORDER BY ";
        $query .= "     T1.SCHOOL_KIND DESC, ";
        $query .= "     T1.GRADE ";

        return $query;
    }
}
?>
