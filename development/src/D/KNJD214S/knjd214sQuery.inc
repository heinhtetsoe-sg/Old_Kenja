<?php

require_once('for_php7.php');

class knjd214sQuery extends Query {

    //年組リスト
    function getAuth($model) {
        $query  = "SELECT GRADE || HR_CLASS AS VALUE,HR_NAME AS LABEL ";
        $query .= "FROM SCHREG_REGD_HDAT ";
        $query .= "WHERE YEAR = '" .CTRL_YEAR."'";
        $query .= "  AND SEMESTER = '".$model->field["SEMESTER"]."' ";
        if ($model->field["GRADE"] != "999") {
            $query .= "  AND GRADE = '".$model->field["GRADE"] ."'";
        }
        $query .= "ORDER BY GRADE, HR_CLASS ";

        return $query;
    }
    //年組(履歴用)
    function getRirekiHrName($year, $semester, $hrClass) {
        $query  = "SELECT HR_NAME ";
        $query .= "FROM SCHREG_REGD_HDAT ";
        $query .= "WHERE YEAR = '{$year}'";
        $query .= "  AND SEMESTER = '{$semester}' ";
        $query .= "  AND GRADE || HR_CLASS = '{$hrClass}'";
        return $query;
    }

    //学年コンボ
    function getSelectGrade($grade = "") {
        $query  = "SELECT ";
        $query .= "    GRADE_NAME1 AS LABEL, ";
        $query .= "    GRADE AS VALUE, ";
        $query .= "    SCHOOL_KIND ";
        $query .= "FROM ";
        $query .= "    SCHREG_REGD_GDAT ";
        $query .= "WHERE YEAR = '".CTRL_YEAR."' ";
        $query .= "  AND SCHOOL_KIND IN ('J','H') "; //中学・高校のみ表示
        if (strlen($grade)) {
            $query .= "     AND GRADE = '{$grade}' ";
        }
        $query .= "ORDER BY GRADE ";

        return $query;
    }

    //学期取得
    function getSemester($cmbFlg = "") {
        $query  = "SELECT ";
        $query .= "    SEMESTERNAME AS LABEL, ";
        $query .= "    SEMESTER AS VALUE ";
        $query .= "FROM ";
        $query .= "    SEMESTER_MST ";
        $query .= "WHERE ";
        $query .= "    YEAR = '".CTRL_YEAR."' ";
        if ($cmbFlg == "seme") {
            $query .= "    AND SEMESTER <> '9' ";
        }
        $query .= "ORDER BY ";
        $query .= "    SEMESTER ";

        return $query;
    }
    //学期(履歴用)
    function getRirekiSemesterName($year, $semester) {
        $query  = "SELECT SEMESTERNAME ";
        $query .= "FROM SEMESTER_MST ";
        $query .= "WHERE YEAR = '{$year}'";
        $query .= "  AND SEMESTER = '{$semester}' ";
        return $query;
    }

    //テスト種別取得（コピー元になる評価コンボ）
    function getTestItem($model, $flg = "") {
        $query  = " SELECT ";
        $query .= "     T1.SEMESTER || T1.TESTKINDCD || T1.TESTITEMCD || T1.SCORE_DIV AS VALUE, ";
        $query .= "     T1.SEMESTER || T1.TESTKINDCD || T1.TESTITEMCD || T1.SCORE_DIV || ':' || L1.SEMESTERNAME || '-' || T1.TESTITEMNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     TESTITEM_MST_COUNTFLG_NEW_SDIV T1 ";
        $query .= "     LEFT JOIN SEMESTER_MST L1 ON L1.YEAR = T1.YEAR AND L1.SEMESTER = T1.SEMESTER ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' ";
//        if ($flg == "kari") {
//            //(保存先になる仮評定)仮評定を表示する
//            $query .= "     AND T1.SCORE_DIV = '09' ";
//        } else {
//            //(コピー元になる評価)評価と仮評定を表示する
            $query .= "     AND T1.SCORE_DIV IN ('08') "; //08のみ表示
//        }
        //学年評定は表示しない
        $query .= "         AND T1.SEMESTER || T1.TESTKINDCD || T1.TESTITEMCD || T1.SCORE_DIV <> '{$model->field["COPY_SAKI_CD"]}' ";
        //基本設定されたパーツ
        $query .= "     AND T1.SEMESTER || T1.TESTKINDCD || T1.TESTITEMCD || T1.SCORE_DIV IN ( ";
        $query .= "         SELECT ";
        $query .= "             T3.SEMESTER || T3.TESTKINDCD || T3.TESTITEMCD || T3.SCORE_DIV ";
        $query .= "         FROM ";
        $query .= "             ADMIN_CONTROL_SDIV_DAT T3 ";
        $query .= "         WHERE ";
        $query .= "             T3.YEAR = '".CTRL_YEAR."' ";
        $query .= "         AND T3.CLASSCD = '00' ";
        $query .= "         AND T3.SCHOOL_KIND = '{$model->school_kind}' ";
        $query .= "         AND T3.CURRICULUM_CD = '00' ";
        $query .= "         AND T3.SUBCLASSCD = '000000' ";
        $query .= "         ) ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }
    //テスト種別(履歴用)
    function getRirekiTestName($year, $testcd) {
        $query  = " SELECT ";
        $query .= "     L1.SEMESTERNAME || '-' || T1.TESTITEMNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     TESTITEM_MST_COUNTFLG_NEW_SDIV T1 ";
        $query .= "     LEFT JOIN SEMESTER_MST L1 ON L1.YEAR = T1.YEAR AND L1.SEMESTER = T1.SEMESTER ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '{$year}' ";
        $query .= "     AND T1.SEMESTER || T1.TESTKINDCD || T1.TESTITEMCD || T1.SCORE_DIV = '{$testcd}' ";
        return $query;
    }

    //履歴一覧
    function getListRireki($model) {
        $programid = PROGRAMID;
        $year = CTRL_YEAR;
        $query  = " SELECT ";
        $query .= "     T1.* ";
        $query .= " FROM ";
        $query .= "     RECORD_COPY_EXEC_DAT T1 ";
        $query .= " WHERE ";
        $query .= "     T1.PROGRAMID = '{$programid}' ";
        $query .= "     AND T1.YEAR = '{$year}' ";
        $query .= "     AND VALUE(T1.DELETE_FLG, '0') != '1' "; //無効フラグ'1'は、一覧に表示しない。
        $query .= " ORDER BY ";
        $query .= "     T1.CALC_DATE DESC, ";
        $query .= "     T1.CALC_TIME DESC ";
        return $query;
    }
    /* 実行履歴 */
    function executeRireki($model) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //実行日付・時間を取得
        $calcRow = $db->getRow(knjd214sQuery::getCalcDateTime(), DB_FETCHMODE_ASSOC);
        $calcDate = $calcRow["CALC_DATE"];//実行日付
        $calcTime = $calcRow["CALC_TIME"];//実行時間
        //実行履歴データ・追加
        $query = knjd214sQuery::getInsertRireki($calcDate, $calcTime, $model);
        $db->query($query);

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
    //実行履歴データ・追加
    function getInsertRireki($calcDate, $calcTime, $model) {
        $data = array();
        $data["CALC_DATE"][TEXT]            = $calcDate;
        $data["CALC_TIME"][TEXT]            = $calcTime;
        $data["PROGRAMID"][TEXT]            = PROGRAMID;
        $data["YEAR"][TEXT]                 = CTRL_YEAR;
        $data["SEMESTER"][TEXT]             = $model->field["SEMESTER"];
        //カンマ区切りでセットしておく
        $data["SELECT_HR_CLASS"][TEXT]      = $model->selectdata; //03001,03002
        $data["SHORI_DIV"][TEXT]            = $model->field["SHORI"];
        $data["SAKI_TESTCD"][TEXT]          = $model->field["COPY_SAKI_CD"];
        if ($model->field["SHORI"] == "1") {
            //クリア
            $data["SOUGAKU_FLG"][TEXT]          = $model->field["SOUGAKU"];
        } else {
            //コピー
            $data["MOTO_TESTCD"][TEXT]          = $model->field["TESTKINDCD"];
            //仮評定フラグ対応
            if ($model->Properties["useProvFlg"] == '1') {
                $data["KARI_DIV"][TEXT]             = $model->field["KARI_DIV"];
            }
            $data["KARI_TESTCD"][TEXT]          = $model->field["KARI_TESTCD"];
            $data["CONVERT_FLG"][TEXT]          = $model->field["CONVERT"];
        }
        $data["REGISTERCD"][TEXT]           = STAFFCD;
        $data["UPDATED"][FUNC]              = "sysdate()";
        $query = Query::insertSQL($data, "RECORD_COPY_EXEC_DAT");
        return $query;
    }
    //実行履歴データ削除
    function deleteRireki($model) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        foreach ($model->del_check as $val) {
            $arrDel = explode(",", $val);
            $calcDate = str_replace("/", "-", $arrDel[0]);
            $calcTime = $arrDel[1];
            //レコード削除は、心配なので、無効フラグを追加して、チェックボックスonの時は、無効フラグ’1’をセツトする。
            $query = knjd214sQuery::getDeleteRireki($calcDate, $calcTime, $model);
            $db->query($query);
        }

        $db->commit();
        Query::dbCheckIn($db);

        return true;
    }
    //レコード削除は、心配なので、無効フラグを追加して、チェックボックスonの時は、無効フラグ’1’をセツトする。
    function getDeleteRireki($calcDate, $calcTime, $model) {
        $data = array();
        $data["DELETE_FLG"][TEXT] = "1";
        $data["REGISTERCD"][TEXT] = STAFFCD;
        $data["UPDATED"][FUNC]    = "sysdate()";
        $programid = PROGRAMID;
        $year = CTRL_YEAR;
        $where  = " WHERE ";
        $where .= "     PROGRAMID = '{$programid}' ";
        $where .= "     AND YEAR = '{$year}' ";
        $where .= "     AND CALC_DATE = '{$calcDate}' ";
        $where .= "     AND CALC_TIME = '{$calcTime}' ";
        $query = Query::updateSQL($data, "RECORD_COPY_EXEC_DAT", $where);
        return $query;
    }

    //処理対象レコード
    function getClearSql($model, $year) {
        //対象者(左のリスト）
        $array  = (strlen($model->selectdata) ? explode(",", $model->selectdata) : array());
        $gr_cl  = implode("','", $array);

        $query  = "";
        $query .= " SELECT ";
        $query .= "     T3.CLASSCD, ";
        $query .= "     T3.SCHOOL_KIND, ";
        $query .= "     T3.CURRICULUM_CD, ";
        $query .= "     T3.SUBCLASSCD, ";
        $query .= "     T3.SCHREGNO ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1 ";
        $query .= "     INNER JOIN SCHREG_BASE_MST T2 ON T2.SCHREGNO = T1.SCHREGNO ";
        $query .= "                                  AND (T2.GRD_DATE IS NULL OR ";
        $query .= "                                       T2.GRD_DIV IS NULL OR ";
        $query .= "                                       T2.GRD_DIV = '4') ";
        $query .= "     INNER JOIN RECORD_SCORE_DAT T3 ON T3.SCHREGNO = T1.SCHREGNO ";
        $query .= "                             AND T3.YEAR = '{$year}' ";
        $query .= "                             AND T3.SEMESTER || T3.TESTKINDCD || T3.TESTITEMCD || T3.SCORE_DIV = '{$model->field["COPY_SAKI_CD"]}' ";
        //on:総学等も含む on以外:総学等は処理対象外
        if ($model->field["SOUGAKU"] != "on") {
            $query .= "                         AND T3.SUBCLASSCD not like '90%' ";
        }
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '{$year}' AND ";
        $query .= "     T1.SEMESTER = '{$model->field["SEMESTER"]}' AND ";
        $query .= "     T1.GRADE || T1.HR_CLASS IN ('".$gr_cl."') ";

        return $query;
    }

    //クリア処理
    function getClearQuery($model) {
        //変数
        $code   = $model->field["COPY_SAKI_CD"];
        $sem    = substr($code, 0, 1);
        $kind   = substr($code, 1, 2);
        $item   = substr($code, 3, 2);
        $div    = substr($code, 5, 2);

        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $query  = knjd214sQuery::getClearSql($model, CTRL_YEAR);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $classCd        = $row["CLASSCD"];
            $schoolKind     = $row["SCHOOL_KIND"];
            $curriculumCd   = $row["CURRICULUM_CD"];
            $subCd          = $row["SUBCLASSCD"];
            $schno      = $row["SCHREGNO"];
            //RECORD_SCORE_DATのレコードを削除。
            $query = knjd214sQuery::getDeleteRecordDat(CTRL_YEAR, $sem, $kind, $item, $div, $subCd, $schno, $classCd, $schoolKind, $curriculumCd, $model);
            $db->query($query);

            //仮評定フラグ対応
            if ($model->Properties["useProvFlg"] == '1') {
                //RECORD_PROV_FLG_DATのレコードを削除
                $query = knjd214sQuery::delRecordProvFlgDat(CTRL_YEAR, $subCd, $schno, $classCd, $schoolKind, $curriculumCd, $model);
                $db->query($query);
            }
        }
        $result->free();

        $db->commit();
        Query::dbCheckIn($db);
    }

    //クリア処理が行われたかチェック
    function getClearExists($model, $year) {
        //対象者(左のリスト）
        $array  = (strlen($model->selectdata) ? explode(",", $model->selectdata) : array());
        $gr_cl  = implode("','", $array);

        $query  = "";
        $query .= " SELECT ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1 ";
        $query .= "     INNER JOIN SCHREG_BASE_MST T2 ON T2.SCHREGNO = T1.SCHREGNO ";
        $query .= "                                  AND (T2.GRD_DATE IS NULL OR ";
        $query .= "                                       T2.GRD_DIV IS NULL OR ";
        $query .= "                                       T2.GRD_DIV = '4') ";
        $query .= "     INNER JOIN RECORD_SCORE_DAT T3 ON T3.SCHREGNO = T1.SCHREGNO ";
        $query .= "                             AND T3.YEAR = '{$year}' ";
        $query .= "                             AND T3.SEMESTER || T3.TESTKINDCD || T3.TESTITEMCD || T3.SCORE_DIV = '{$model->field["COPY_SAKI_CD"]}' ";
        //総学等は処理対象外
        $query .= "                             AND T3.SUBCLASSCD not like '90%' ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '{$year}' AND ";
        $query .= "     T1.SEMESTER = '{$model->field["SEMESTER"]}' AND ";
        $query .= "     T1.GRADE || T1.HR_CLASS IN ('".$gr_cl."') ";

        return $query;
    }

    //処理対象レコード（コピー）
    function getCopySql($model, $year) {
        //対象者(左のリスト）
        $array  = (strlen($model->selectdata) ? explode(",", $model->selectdata) : array());
        $gr_cl  = implode("','", $array);
        $assess = ($model->field["CONVERT"] == "on") ? "R1.ASSESSLEVEL" : "T3.SCORE"; //評定換算する／しない

        $query  = "";
        $query .= " SELECT ";
        $query .= "     T3.CLASSCD, ";
        $query .= "     T3.SCHOOL_KIND, ";
        $query .= "     T3.CURRICULUM_CD, ";
        $query .= "     T3.SUBCLASSCD, ";
        $query .= "     T3.SCHREGNO, ";
        $query .= "     ".$assess." AS SCORE, ";
        $query .= "     case when 1 <= ".$assess." then T4.CREDITS ";
        $query .= "          else NULL end AS COMP_CREDIT, ";
        $query .= "     case when 1 < ".$assess." then T4.CREDITS ";
        $query .= "          when 1 = ".$assess." then 0 ";
        $query .= "          else NULL end AS GET_CREDIT ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1 ";
        $query .= "     INNER JOIN SCHREG_BASE_MST T2 ON T2.SCHREGNO = T1.SCHREGNO ";
        $query .= "                                  AND (T2.GRD_DATE IS NULL OR ";
        $query .= "                                       T2.GRD_DIV IS NULL OR ";
        $query .= "                                       T2.GRD_DIV = '4') ";
        //累計
        $recordTable = ($model->school_kind == 'H') ? "RECORD_RANK_RUIKEI_SDIV_DAT" : "RECORD_SCORE_DAT";
        $query .= "     INNER JOIN {$recordTable} T3 ON T3.SCHREGNO = T1.SCHREGNO ";
        $query .= "             AND T3.YEAR = '{$year}' ";
        $query .= "             AND T3.SEMESTER || T3.TESTKINDCD || T3.TESTITEMCD || T3.SCORE_DIV = '{$model->field["TESTKINDCD"]}' ";
        $query .= "             AND T3.SUBCLASSCD not like '90%' "; //総学等は処理対象外
        $query .= "     INNER JOIN V_SUBCLASS_MST S1 ON S1.YEAR = '{$year}' ";
        $query .= "             AND S1.CLASSCD = T3.CLASSCD ";
        $query .= "             AND S1.SCHOOL_KIND = T3.SCHOOL_KIND ";
        $query .= "             AND S1.CURRICULUM_CD = T3.CURRICULUM_CD ";
        $query .= "             AND S1.SUBCLASSCD = T3.SUBCLASSCD ";
        $query .= "     LEFT JOIN CREDIT_MST T4 ON T4.YEAR = '{$year}' ";
        $query .= "             AND T4.COURSECD = T1.COURSECD ";
        $query .= "             AND T4.MAJORCD = T1.MAJORCD ";
        $query .= "             AND T4.GRADE = T1.GRADE ";
        $query .= "             AND T4.COURSECODE = T1.COURSECODE ";
        $query .= "             AND T4.CLASSCD = T3.CLASSCD ";
        $query .= "             AND T4.SCHOOL_KIND = T3.SCHOOL_KIND ";
        $query .= "             AND T4.CURRICULUM_CD = T3.CURRICULUM_CD ";
        $query .= "             AND T4.SUBCLASSCD = T3.SUBCLASSCD ";
        if ($model->school_kind == 'H') {
            $query .= "     LEFT JOIN COURSE_GROUP_CD_DAT G1 ON G1.YEAR = T1.YEAR ";
            $query .= "             AND G1.GRADE = T1.GRADE ";
            $query .= "             AND G1.COURSECD = T1.COURSECD ";
            $query .= "             AND G1.MAJORCD = T1.MAJORCD ";
            $query .= "             AND G1.COURSECODE = T1.COURSECODE ";
        }
        if ($model->school_kind == 'H') { //H:高校
            $query .= "     LEFT JOIN ASSESS_LEVEL_SDIV_MST R1 ON R1.YEAR = '{$year}' ";
            $query .= "             AND R1.SEMESTER || R1.TESTKINDCD || R1.TESTITEMCD || R1.SCORE_DIV = '{$model->field["TESTKINDCD"]}' ";
            $query .= "             AND R1.CLASSCD = T3.CLASSCD ";
            $query .= "             AND R1.SCHOOL_KIND = T3.SCHOOL_KIND ";
            $query .= "             AND R1.CURRICULUM_CD = T3.CURRICULUM_CD ";
            $query .= "             AND R1.SUBCLASSCD = T3.SUBCLASSCD ";
            $query .= "             AND R1.RUISEKI_DIV = '3' "; //3:累積（通年）
            $query .= "             AND R1.DIV = '5' "; //5:コースグループ
            $query .= "             AND R1.GRADE = T1.GRADE ";
            $query .= "             AND R1.HR_CLASS = '000' ";
            $query .= "             AND R1.COURSECD = '0' ";
            $query .= "             AND R1.MAJORCD = VALUE(G1.GROUP_CD, '000') ";
            $query .= "             AND R1.COURSECODE = '0000' ";
        } else { //J:中学
            $query .= "     LEFT JOIN ASSESS_SUBCLASS_MST R1 ON R1.YEAR = '{$year}' ";
            $query .= "             AND R1.GRADE = T1.GRADE ";
            $query .= "             AND R1.COURSECD = T1.COURSECD ";
            $query .= "             AND R1.MAJORCD = T1.MAJORCD ";
            $query .= "             AND R1.COURSECODE = T1.COURSECODE ";
            $query .= "             AND R1.CLASSCD = T3.CLASSCD ";
            $query .= "             AND R1.SCHOOL_KIND = T3.SCHOOL_KIND ";
            $query .= "             AND R1.CURRICULUM_CD = T3.CURRICULUM_CD ";
            $query .= "             AND R1.SUBCLASSCD = T3.SUBCLASSCD ";
        }
        $query .= "             AND T3.SCORE BETWEEN R1.ASSESSLOW AND R1.ASSESSHIGH ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '{$year}' AND ";
        $query .= "     T1.SEMESTER = '{$model->field["SEMESTER"]}' AND ";
        $query .= "     T1.GRADE || T1.HR_CLASS IN ('".$gr_cl."') ";

        return $query;
    }

    //処理対象レコード（合併先科目の単位自動計算）
    function getCombinedCopySql($model, $year) {
        //対象者(左のリスト）
        $array  = (strlen($model->selectdata) ? explode(",", $model->selectdata) : array());
        $gr_cl  = implode("','", $array);

        $query  = "";
        $query .= " WITH T_COMBINED AS ( ";
        $query .= "     SELECT ";
        $query .= "            T2.SCHREGNO, ";
        $query .= "            T1.COMBINED_CLASSCD, ";
        $query .= "            T1.COMBINED_SCHOOL_KIND, ";
        $query .= "            T1.COMBINED_CURRICULUM_CD, ";
        $query .= "            T1.COMBINED_SUBCLASSCD, ";
        $query .= "            COUNT(T2.SUBCLASSCD) AS MOTO_CNT, ";
        $query .= "            SUM(case when T2.COMP_CREDIT IS NOT NULL OR T2.GET_CREDIT IS NOT NULL then 1 else 0 end) AS CREDIT_CNT, ";
        $query .= "            SUM(T2.COMP_CREDIT) AS COMP_CREDIT, ";
        $query .= "            SUM(T2.GET_CREDIT) AS GET_CREDIT, ";
        $query .= "            SUM(case when 1 < T2.SCORE then T2.GET_CREDIT ";
        $query .= "                     when 1 = T2.SCORE then T2.COMP_CREDIT end) AS GET_CREDIT_Y ";
        $query .= "     FROM   SUBCLASS_REPLACE_COMBINED_DAT T1, ";
        $query .= "            RECORD_SCORE_DAT T2 ";
        $query .= "     WHERE  T1.YEAR = '{$year}' ";
        $query .= "       AND  T2.YEAR = '{$year}' ";
        $query .= "       AND  T2.SEMESTER || T2.TESTKINDCD || T2.TESTITEMCD || T2.SCORE_DIV = '{$model->field["COPY_SAKI_CD"]}' ";
        $query .= "       AND  T2.CLASSCD = T1.ATTEND_CLASSCD ";
        $query .= "       AND  T2.SCHOOL_KIND = T1.ATTEND_SCHOOL_KIND ";
        $query .= "       AND  T2.CURRICULUM_CD = T1.ATTEND_CURRICULUM_CD ";
        $query .= "       AND  T2.SUBCLASSCD = T1.ATTEND_SUBCLASSCD ";
        $query .= "     GROUP BY ";
        $query .= "            T2.SCHREGNO, ";
        $query .= "            T1.COMBINED_CLASSCD, ";
        $query .= "            T1.COMBINED_SCHOOL_KIND, ";
        $query .= "            T1.COMBINED_CURRICULUM_CD, ";
        $query .= "            T1.COMBINED_SUBCLASSCD ";
        $query .= "     ) ";
        $query .= " , T_COMBINED_FLG AS ( ";
        $query .= "     SELECT ";
        $query .= "            T1.COMBINED_CLASSCD, ";
        $query .= "            T1.COMBINED_SCHOOL_KIND, ";
        $query .= "            T1.COMBINED_CURRICULUM_CD, ";
        $query .= "            T1.COMBINED_SUBCLASSCD, ";
        $query .= "            T1.CALCULATE_CREDIT_FLG AS FLG, ";
        $query .= "            COUNT(T1.ATTEND_SUBCLASSCD) AS CNT ";
        $query .= "     FROM   SUBCLASS_REPLACE_COMBINED_DAT T1 ";
        $query .= "     WHERE  T1.YEAR = '{$year}' ";
        $query .= "     GROUP BY ";
        $query .= "            T1.COMBINED_CLASSCD, ";
        $query .= "            T1.COMBINED_SCHOOL_KIND, ";
        $query .= "            T1.COMBINED_CURRICULUM_CD, ";
        $query .= "            T1.COMBINED_SUBCLASSCD, ";
        $query .= "            T1.CALCULATE_CREDIT_FLG ";
        $query .= "     ) ";

        //評定を設定しない科目
        if ($model->Properties["useClassDetailDat"] == '1') {
            $noValue = "(DETAIL.CLASSCD = T3.CLASSCD or '90' = T3.CLASSCD)";
        } else {
            $noValue = "(D008.NAMECD2 = substr(T3.SUBCLASSCD,1,2) or '90' = substr(T3.SUBCLASSCD,1,2))";
        }
        $noCnt1 = "0 < T5.CREDIT_CNT and T6.CNT = T5.MOTO_CNT";
        $noCnt2 = "0 < T5.CREDIT_CNT and 0 < T5.MOTO_CNT";

        $query .= " SELECT ";
        $query .= "     T3.CLASSCD, ";
        $query .= "     T3.SCHOOL_KIND, ";
        $query .= "     T3.CURRICULUM_CD, ";
        $query .= "     T3.SUBCLASSCD, ";
        $query .= "     T3.SCHREGNO, ";
        $query .= "     case when ".$noValue." then NULL ";
        $query .= "          else T3.SCORE end AS SCORE, ";
        $query .= "     case when ".$noValue." and T6.FLG = '1' and ".$noCnt1." then T4.CREDITS ";
        $query .= "          when ".$noValue." and T6.FLG = '2' and ".$noCnt2." then T5.COMP_CREDIT ";
        $query .= "          when ".$noValue." then NULL ";
        $query .= "          when 1 <= T3.SCORE and T6.FLG = '1' then T4.CREDITS ";
        $query .= "          when 1 <= T3.SCORE and T6.FLG = '2' then T5.COMP_CREDIT ";
        $query .= "          else NULL end AS COMP_CREDIT, ";
        $query .= "     case when ".$noValue." and T6.FLG = '1' and ".$noCnt1." then T4.CREDITS ";
        $query .= "          when ".$noValue." and T6.FLG = '2' and ".$noCnt2." then T5.GET_CREDIT ";
        $query .= "          when ".$noValue." then NULL ";
        $query .= "          when 1 < T3.SCORE and T6.FLG = '1' then T4.CREDITS ";
        $query .= "          when 1 < T3.SCORE and T6.FLG = '2' and D015.NAMESPARE1 = 'Y' then T5.GET_CREDIT_Y ";
        $query .= "          when 1 < T3.SCORE and T6.FLG = '2' then T5.GET_CREDIT ";
        $query .= "          when 1 = T3.SCORE then 0 ";
        $query .= "          else NULL end AS GET_CREDIT ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1 ";
        $query .= "     INNER JOIN SCHREG_BASE_MST T2 ON T2.SCHREGNO = T1.SCHREGNO ";
        $query .= "                                  AND (T2.GRD_DATE IS NULL OR ";
        $query .= "                                       T2.GRD_DIV IS NULL OR ";
        $query .= "                                       T2.GRD_DIV = '4') ";
        $query .= "     INNER JOIN RECORD_SCORE_DAT T3 ON T3.SCHREGNO = T1.SCHREGNO ";
        $query .= "                             AND T3.YEAR = '{$year}' ";
        $query .= "                             AND T3.SEMESTER || T3.TESTKINDCD || T3.TESTITEMCD || T3.SCORE_DIV = '{$model->field["COPY_SAKI_CD"]}' ";
        //総学等は処理対象外
        $query .= "                             AND T3.SUBCLASSCD not like '90%' ";
        $query .= "     INNER JOIN T_COMBINED_FLG T6 ON T6.COMBINED_SUBCLASSCD = T3.SUBCLASSCD ";
        $query .= "                                 AND T6.COMBINED_CLASSCD = T3.CLASSCD ";
        $query .= "                                 AND T6.COMBINED_SCHOOL_KIND = T3.SCHOOL_KIND ";
        $query .= "                                 AND T6.COMBINED_CURRICULUM_CD = T3.CURRICULUM_CD ";
        $query .= "     LEFT JOIN T_COMBINED T5 ON T5.SCHREGNO = T3.SCHREGNO ";
        $query .= "                            AND T5.COMBINED_CLASSCD = T3.CLASSCD ";
        $query .= "                            AND T5.COMBINED_SCHOOL_KIND = T3.SCHOOL_KIND ";
        $query .= "                            AND T5.COMBINED_CURRICULUM_CD = T3.CURRICULUM_CD ";
        $query .= "                            AND T5.COMBINED_SUBCLASSCD = T3.SUBCLASSCD ";
        $query .= "     LEFT JOIN V_NAME_MST D015 ON D015.YEAR = T1.YEAR ";
        $query .= "                              AND D015.NAMECD1 = 'D015' ";
        $query .= "                              AND D015.NAMECD2 = '01' ";
        if ($model->Properties["useClassDetailDat"] == '1') {
            $query .= "     LEFT JOIN CLASS_DETAIL_DAT DETAIL ON DETAIL.YEAR = T1.YEAR ";
            $query .= "                                      AND DETAIL.CLASS_SEQ = '003' ";
            $query .= "                                      AND DETAIL.CLASSCD = T3.CLASSCD ";
            $query .= "                                      AND DETAIL.SCHOOL_KIND = T3.SCHOOL_KIND ";
        } else {
            $query .= "     LEFT JOIN V_NAME_MST D008 ON D008.YEAR = T1.YEAR ";
            $query .= "                              AND D008.NAMECD1 = CASE WHEN EXISTS (SELECT 'X' FROM V_NAME_MST WHERE YEAR = '{$year}' AND NAMECD1 = 'D' || T3.SCHOOL_KIND || '08') THEN 'D' || T3.SCHOOL_KIND || '08' ";
            $query .= "                                                      ELSE 'D008' ";
            $query .= "                                                 END ";
            $query .= "                              AND D008.NAMECD2 = substr(T3.SUBCLASSCD,1,2) ";
        }
        $query .= "     LEFT JOIN CREDIT_MST T4 ON T4.YEAR = '{$year}' ";
        $query .= "                            AND T4.COURSECD = T1.COURSECD ";
        $query .= "                            AND T4.MAJORCD = T1.MAJORCD ";
        $query .= "                            AND T4.GRADE = T1.GRADE ";
        $query .= "                            AND T4.COURSECODE = T1.COURSECODE ";
        $query .= "                            AND T4.CLASSCD = T3.CLASSCD ";
        $query .= "                            AND T4.SCHOOL_KIND = T3.SCHOOL_KIND ";
        $query .= "                            AND T4.CURRICULUM_CD = T3.CURRICULUM_CD ";
        $query .= "                            AND T4.SUBCLASSCD = T3.SUBCLASSCD ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '{$year}' AND ";
        $query .= "     T1.SEMESTER = '{$model->field["SEMESTER"]}' AND ";
        $query .= "     T1.GRADE || T1.HR_CLASS IN ('".$gr_cl."') ";

        return $query;
    }

    //処理対象レコード（仮評定）
    function getProvCopySql($model, $year) {
        //対象者(左のリスト）
        $array  = (strlen($model->selectdata) ? explode(",", $model->selectdata) : array());
        $gr_cl  = implode("','", $array);

        $query  = "";
        $query .= " SELECT ";
        $query .= "     T3.CLASSCD, ";
        $query .= "     T3.SCHOOL_KIND, ";
        $query .= "     T3.CURRICULUM_CD, ";
        $query .= "     T3.SUBCLASSCD, ";
        $query .= "     T3.SCHREGNO, ";
        $query .= "     T3.SCORE, ";
        $query .= "     T3.COMP_CREDIT, ";
        $query .= "     T3.GET_CREDIT ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1 ";
        $query .= "     INNER JOIN SCHREG_BASE_MST T2 ON T2.SCHREGNO = T1.SCHREGNO ";
        $query .= "                                  AND (T2.GRD_DATE IS NULL OR ";
        $query .= "                                       T2.GRD_DIV IS NULL OR ";
        $query .= "                                       T2.GRD_DIV = '4') ";
        $query .= "     INNER JOIN RECORD_SCORE_DAT T3 ON T3.SCHREGNO = T1.SCHREGNO ";
        $query .= "                             AND T3.YEAR = '{$year}' ";
        $query .= "                             AND T3.SEMESTER || T3.TESTKINDCD || T3.TESTITEMCD || T3.SCORE_DIV = '{$model->field["COPY_SAKI_CD"]}' ";
        //総学等は処理対象外
        $query .= "                             AND T3.SUBCLASSCD not like '90%' ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '{$year}' AND ";
        $query .= "     T1.SEMESTER = '{$model->field["SEMESTER"]}' AND ";
        $query .= "     T1.GRADE || T1.HR_CLASS IN ('".$gr_cl."') ";

        return $query;
    }

    //コピー処理
    function getCopyQuery($model) {
        //変数
        $code   = $model->field["COPY_SAKI_CD"];
        $sem    = substr($code, 0, 1);
        $kind   = substr($code, 1, 2);
        $item   = substr($code, 3, 2);
        $div    = substr($code, 5, 2);

        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $query  = knjd214sQuery::getCopySql($model, CTRL_YEAR);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $classCd        = $row["CLASSCD"];
            $schoolKind     = $row["SCHOOL_KIND"];
            $curriculumCd   = $row["CURRICULUM_CD"];
            $subCd          = $row["SUBCLASSCD"];
            $schno      = $row["SCHREGNO"];
            $gradValue  = $row["SCORE"];
            $compCredit = $row["COMP_CREDIT"];
            $getCredit  = $row["GET_CREDIT"];
            //評価を学年評定にコピーする。単位もコピーする。
            $query = knjd214sQuery::getInsertRecordDat(CTRL_YEAR, $sem, $kind, $item, $div, $subCd, $schno, $gradValue, $compCredit, $getCredit, $classCd, $schoolKind, $curriculumCd, $model);
            $db->query($query);

            //仮評定フラグ対応
            if ($model->Properties["useProvFlg"] == '1') {
                //RECORD_PROV_FLG_DATのレコードを削除
                $query = knjd214sQuery::delRecordProvFlgDat(CTRL_YEAR, $subCd, $schno, $classCd, $schoolKind, $curriculumCd, $model);
                $db->query($query);
                //評定フラグが仮評定の場合
                if ($model->field["KARI_DIV"] == '1') {
                    //RECORD_PROV_FLG_DATのレコードを追加
                    $query = knjd214sQuery::insRecordProvFlgDat(CTRL_YEAR, $subCd, $schno, $classCd, $schoolKind, $curriculumCd, $model);
                    $db->query($query);
                }
            }
        }
        $result->free();

        $db->commit();
        Query::dbCheckIn($db);
    }

    //コピー処理（合併先科目の単位自動計算）
    function getCombinedCopyQuery($model) {
        //変数
        $code   = $model->field["COPY_SAKI_CD"];
        $sem    = substr($code, 0, 1);
        $kind   = substr($code, 1, 2);
        $item   = substr($code, 3, 2);
        $div    = substr($code, 5, 2);

        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $query  = knjd214sQuery::getCombinedCopySql($model, CTRL_YEAR);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $classCd        = $row["CLASSCD"];
            $schoolKind     = $row["SCHOOL_KIND"];
            $curriculumCd   = $row["CURRICULUM_CD"];
            $subCd          = $row["SUBCLASSCD"];
            $schno      = $row["SCHREGNO"];
            $gradValue  = $row["SCORE"];
            $compCredit = $row["COMP_CREDIT"];
            $getCredit  = $row["GET_CREDIT"];
            //再処理のため、RECORD_SCORE_DATのレコードを一旦削除。
            $query = knjd214sQuery::getDeleteRecordDat(CTRL_YEAR, $sem, $kind, $item, $div, $subCd, $schno, $classCd, $schoolKind, $curriculumCd, $model);
            $db->query($query);
            //評価を学年評定にコピーする。単位もコピーする。
            $query = knjd214sQuery::getInsertRecordDat(CTRL_YEAR, $sem, $kind, $item, $div, $subCd, $schno, $gradValue, $compCredit, $getCredit, $classCd, $schoolKind, $curriculumCd, $model);
            $db->query($query);

            //仮評定フラグ対応
            if ($model->Properties["useProvFlg"] == '1') {
                //再処理のため、RECORD_PROV_FLG_DATのレコードを一旦削除
                $query = knjd214sQuery::delRecordProvFlgDat(CTRL_YEAR, $subCd, $schno, $classCd, $schoolKind, $curriculumCd, $model);
                $db->query($query);
                //評定フラグが仮評定の場合
                if ($model->field["KARI_DIV"] == '1') {
                    //RECORD_PROV_FLG_DATのレコードを追加
                    $query = knjd214sQuery::insRecordProvFlgDat(CTRL_YEAR, $subCd, $schno, $classCd, $schoolKind, $curriculumCd, $model);
                    $db->query($query);
                }
            }
        }
        $result->free();

        $db->commit();
        Query::dbCheckIn($db);
    }

    //コピー処理（仮評定）
    function getProvCopyQuery($model) {
        //変数
        $code   = $model->field["KARI_TESTCD"];
        $sem    = substr($code, 0, 1);
        $kind   = substr($code, 1, 2);
        $item   = substr($code, 3, 2);
        $div    = substr($code, 5, 2); //09:仮評定

        $db = Query::dbCheckOut();
        $db->autoCommit(false);
        $query  = knjd214sQuery::getProvCopySql($model, CTRL_YEAR);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $classCd        = $row["CLASSCD"];
            $schoolKind     = $row["SCHOOL_KIND"];
            $curriculumCd   = $row["CURRICULUM_CD"];
            $subCd          = $row["SUBCLASSCD"];
            $schno      = $row["SCHREGNO"];
            $gradValue  = $row["SCORE"];
            $compCredit = $row["COMP_CREDIT"];
            $getCredit  = $row["GET_CREDIT"];
            //レコードを一旦削除。
            $query = knjd214sQuery::getDeleteRecordDat(CTRL_YEAR, $sem, $kind, $item, $div, $subCd, $schno, $classCd, $schoolKind, $curriculumCd, $model);
            $db->query($query);
            //仮評定にコピー。
            $query = knjd214sQuery::getInsertRecordDat(CTRL_YEAR, $sem, $kind, $item, $div, $subCd, $schno, $gradValue, $compCredit, $getCredit, $classCd, $schoolKind, $curriculumCd, $model);
            $db->query($query);
        }
        $result->free();
        $db->commit();
        Query::dbCheckIn($db);
    }

    //RECORD_SCORE_DATのレコードを削除。
    function getDeleteRecordDat($year, $sem, $kind, $item, $div, $subCd, $schno, $classCd, $schoolKind, $curriculumCd, $model) {
        $query  = "";
        $query .= " DELETE FROM RECORD_SCORE_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$year}' ";
        $query .= "     AND SEMESTER = '{$sem}' ";
        $query .= "     AND TESTKINDCD = '{$kind}' ";
        $query .= "     AND TESTITEMCD = '{$item}' ";
        $query .= "     AND SCORE_DIV = '{$div}' ";
        $query .= "     AND CLASSCD = '{$classCd}' ";
        $query .= "     AND SCHOOL_KIND = '{$schoolKind}' ";
        $query .= "     AND CURRICULUM_CD = '{$curriculumCd}' ";
        $query .= "     AND SUBCLASSCD = '{$subCd}' ";
        $query .= "     AND SCHREGNO = '{$schno}' ";
        return $query;
    }

    //追加
    function getInsertRecordDat($year, $sem, $kind, $item, $div, $subCd, $schno, $gradValue, $compCredit, $getCredit, $classCd, $schoolKind, $curriculumCd, $model) {
        $data = array();
        $data["YEAR"][TEXT]         = $year;
        $data["SEMESTER"][TEXT]     = $sem;
        $data["TESTKINDCD"][TEXT]   = $kind;
        $data["TESTITEMCD"][TEXT]   = $item;
        $data["SCORE_DIV"][TEXT]    = $div;
        $data["CLASSCD"][TEXT]       = $classCd;
        $data["SCHOOL_KIND"][TEXT]   = $schoolKind;
        $data["CURRICULUM_CD"][TEXT] = $curriculumCd;
        $data["SUBCLASSCD"][TEXT]   = $subCd;
        $data["SCHREGNO"][TEXT]     = $schno;
        //学年評定
        $data["SCORE"][NUMBER]      = $gradValue;
        $data["COMP_CREDIT"][NUMBER]= $compCredit;
        $data["GET_CREDIT"][NUMBER] = $getCredit;
        $data["REGISTERCD"][TEXT]   = STAFFCD;
        $data["UPDATED"][FUNC]      = "SYSDATE()";

        $query = Query::insertSQL($data, "RECORD_SCORE_DAT");
        return $query;
    }

    //RECORD_PROV_FLG_DATのレコードを削除。
    function delRecordProvFlgDat($year, $subCd, $schno, $classCd, $schoolKind, $curriculumCd, $model) {
        $query  = "";
        $query .= " DELETE FROM RECORD_PROV_FLG_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$year}' ";
        $query .= "     AND CLASSCD = '{$classCd}' ";
        $query .= "     AND SCHOOL_KIND = '{$schoolKind}' ";
        $query .= "     AND CURRICULUM_CD = '{$curriculumCd}' ";
        $query .= "     AND SUBCLASSCD = '{$subCd}' ";
        $query .= "     AND SCHREGNO = '{$schno}' ";
        return $query;
    }

    //RECORD_PROV_FLG_DATのレコードを追加
    function insRecordProvFlgDat($year, $subCd, $schno, $classCd, $schoolKind, $curriculumCd, $model) {
        $data = array();
        $data["YEAR"][TEXT]         = $year;
        $data["CLASSCD"][TEXT]       = $classCd;
        $data["SCHOOL_KIND"][TEXT]   = $schoolKind;
        $data["CURRICULUM_CD"][TEXT] = $curriculumCd;
        $data["SUBCLASSCD"][TEXT]   = $subCd;
        $data["SCHREGNO"][TEXT]     = $schno;
        //評定フラグ・・・仮評定(1)、本評定(NULL)
        $data["PROV_FLG"][TEXT]     = "1";
        $data["REGISTERCD"][TEXT]   = STAFFCD;
        $data["UPDATED"][FUNC]      = "SYSDATE()";

        $query = Query::insertSQL($data, "RECORD_PROV_FLG_DAT");
        return $query;
    }

}
?>
