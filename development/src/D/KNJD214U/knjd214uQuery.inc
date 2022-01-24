<?php

require_once('for_php7.php');

class knjd214uQuery extends Query {

    //追指導入力設定があるか
    function getSidouInputCount($model) {
        $query  = " SELECT ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     TESTITEM_MST_COUNTFLG_NEW_SDIV T1 ";
        $query .= " WHERE ";
        $query .= "         T1.YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND T1.SIDOU_INPUT = '1' "; //1:追指導入力
        return $query;
    }

    //年組リスト
    function getAuthClass($model) {
        $query  = "SELECT T1.GRADE || T1.HR_CLASS AS VALUE,T1.HR_NAME AS LABEL ";
        $query .= "FROM SCHREG_REGD_HDAT T1 ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= "      INNER JOIN SCHREG_REGD_GDAT T2 ON T2.YEAR = T1.YEAR ";
                $query .= "           AND T2.GRADE = T1.GRADE ";
                $query .= "           AND T2.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
            }
        } else if ($model->Properties["useSchool_KindField"] == "1") {
            $query .= "      INNER JOIN SCHREG_REGD_GDAT T2 ON T2.YEAR = T1.YEAR ";
            $query .= "           AND T2.GRADE = T1.GRADE ";
            $query .= "           AND T2.SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= "WHERE T1.YEAR = '" .CTRL_YEAR."'";
        $query .= "  AND T1.SEMESTER = '".$model->field["SEMESTER"]."' ";
        if ($model->field["GRADE"] != "999") {
            $query .= "  AND T1.GRADE = '".$model->field["GRADE"] ."'";
        }
        $query .= "ORDER BY VALUE ";

        return $query;
    }

    //生徒取得
    function getAuthStudent($model) {
        $query  = "SELECT ";
        $query .= "    T1.SCHREGNO || '-' || T1.ATTENDNO AS VALUE, ";
        $query .= "    T1.SCHREGNO || '　' || T1.ATTENDNO ||  '番　' || L1.NAME_SHOW AS LABEL ";
        $query .= "FROM ";
        $query .= "    SCHREG_REGD_DAT T1 ";
        $query .= "    INNER JOIN SCHREG_BASE_MST L1 ON L1.SCHREGNO = T1.SCHREGNO ";
        $query .= "WHERE ";
        $query .= "    T1.YEAR = '".CTRL_YEAR."' ";
        $query .= "    AND T1.SEMESTER = '".$model->field["SEMESTER"]."' ";
        $query .= "    AND T1.GRADE || T1.HR_CLASS = '".$model->field["GRADE_HR_CLASS"]."' ";
        $query .= "ORDER BY ";
        $query .= "    T1.ATTENDNO ";

        return $query;
    }

    //科目リスト
    function getListKamoku($model) {
        $query  = " WITH T_REGD AS ( ";
        $query .= "     SELECT ";
        $query .= "         T1.SCHREGNO ";
        $query .= "     FROM ";
        $query .= "         SCHREG_REGD_DAT T1 ";
        $query .= "         INNER JOIN SCHREG_BASE_MST B1 ON B1.SCHREGNO = T1.SCHREGNO ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= "         INNER JOIN SCHREG_REGD_GDAT T2 ON T2.YEAR = T1.YEAR ";
                $query .= "             AND T2.GRADE = T1.GRADE ";
                $query .= "             AND T2.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
            }
        } else if ($model->Properties["useSchool_KindField"] == "1") {
            $query .= "         INNER JOIN SCHREG_REGD_GDAT T2 ON T2.YEAR = T1.YEAR ";
            $query .= "             AND T2.GRADE = T1.GRADE ";
            $query .= "             AND T2.SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= "     WHERE ";
        $query .= "         T1.YEAR = '".CTRL_YEAR."' ";
        $query .= "         AND T1.SEMESTER = '".$model->field["SEMESTER"]."' ";
        if ($model->field["GRADE"] != "999") {
            $query .= "         AND T1.GRADE = '".$model->field["GRADE"]."' ";
        }
        if ($model->field["CHOICE"] == "2") {
            $query .= "         AND T1.GRADE || T1.HR_CLASS = '".$model->field["GRADE_HR_CLASS"]."' ";
        }
        $query .= " ) ";
        $query .= " SELECT DISTINCT ";
        $query .= "     S1.CLASSCD || '-' || S1.SCHOOL_KIND || '-' || S1.CURRICULUM_CD || '-' || S1.SUBCLASSCD AS VALUE, ";
        $query .= "     S1.CLASSCD || '-' || S1.SCHOOL_KIND || '-' || S1.CURRICULUM_CD || '-' || S1.SUBCLASSCD || ':' || S1.SUBCLASSNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     CHAIR_STD_DAT C1 ";
        $query .= "     INNER JOIN CHAIR_DAT C2 ON C2.YEAR = C1.YEAR ";
        $query .= "         AND C2.SEMESTER = C1.SEMESTER ";
        $query .= "         AND C2.CHAIRCD = C1.CHAIRCD ";
        $query .= "     INNER JOIN V_SUBCLASS_MST S1 ON S1.YEAR = C2.YEAR ";
        $query .= "         AND S1.CLASSCD = C2.CLASSCD ";
        $query .= "         AND S1.SCHOOL_KIND = C2.SCHOOL_KIND ";
        $query .= "         AND S1.CURRICULUM_CD = C2.CURRICULUM_CD ";
        $query .= "         AND S1.SUBCLASSCD = C2.SUBCLASSCD ";
        $query .= "     INNER JOIN T_REGD T1 ON T1.SCHREGNO = C1.SCHREGNO ";
        $query .= " WHERE ";
        $query .= "     C1.YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND C1.SEMESTER = '".$model->field["SEMESTER"]."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

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
    function getSelectGrade($model)
    {
        $query  = "SELECT DISTINCT ";
        $query .= "    GRADE_NAME1 AS LABEL, ";
        $query .= "    GRADE AS VALUE ";
        $query .= "FROM ";
        $query .= "    SCHREG_REGD_GDAT ";
        $query .= "WHERE YEAR = '".CTRL_YEAR."' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= "     AND SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind),"','")."') ";
            }
        } else if ($model->Properties["useSchool_KindField"] == "1") {
            $query .= "     AND SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= "ORDER BY VALUE ";

        return $query;
    }

    //学期取得
    function getSemester($cmbFlg = "")
    {
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
    function getTestItem($model, $flg = "")
    {
        $query  = " SELECT ";
        $query .= "     T1.SEMESTER || T1.TESTKINDCD || T1.TESTITEMCD || T1.SCORE_DIV AS VALUE, ";
        $query .= "     T1.SEMESTER || T1.TESTKINDCD || T1.TESTITEMCD || T1.SCORE_DIV || ':' || L1.SEMESTERNAME || '-' || T1.TESTITEMNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     TESTITEM_MST_COUNTFLG_NEW_SDIV T1 ";
        $query .= "     LEFT JOIN SEMESTER_MST L1 ON L1.YEAR = T1.YEAR AND L1.SEMESTER = T1.SEMESTER ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' ";
        if ($flg == "kari") {
            //(保存先になる仮評定)仮評定を表示する
            $query .= "     AND T1.SCORE_DIV = '09' ";
        } else {
            //(コピー元になる評価)評価と仮評定を表示する
            $query .= "     AND T1.SCORE_DIV IN ('08','09') ";
        }
        //学年評定は表示しない
        $query .= "         AND T1.SEMESTER || T1.TESTKINDCD || T1.TESTITEMCD || T1.SCORE_DIV <> '{$model->field["COPY_SAKI_CD"]}' ";
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
        $calcRow = $db->getRow(knjd214uQuery::getCalcDateTime(), DB_FETCHMODE_ASSOC);
        $calcDate = $calcRow["CALC_DATE"];//実行日付
        $calcTime = $calcRow["CALC_TIME"];//実行時間
        //実行履歴データ・追加
        $query = knjd214uQuery::getInsertRireki($calcDate, $calcTime, $model);
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
        if (strlen($model->selectdata2) > 500) {
            $data["SELECT_HR_CLASS"][TEXT]      = substr($model->selectdata2, 0, 500);
        } else {
            $data["SELECT_HR_CLASS"][TEXT]      = $model->selectdata2; //03001,03002 //20010003,20010004
        }
        if (strlen($model->selectdataKamoku) > 500) {
            $data["SELECT_SUBCLASS"][TEXT]      = substr($model->selectdataKamoku, 0, 500);
        } else {
            $data["SELECT_SUBCLASS"][TEXT]      = $model->selectdataKamoku;
        }
        $data["SHORI_DIV"][TEXT]            = $model->field["SHORI"];
        $data["SAKI_TESTCD"][TEXT]          = $model->field["COPY_SAKI_CD"];
        if ($model->field["SHORI"] == "1") {
            //クリア
            $data["SOUGAKU_FLG"][TEXT]          = $model->field["SOUGAKU"];
        } else {
            //コピー
            $data["MOTO_TESTCD"][TEXT]          = $model->field["TESTKINDCD"];
            $data["KARI_DIV"][TEXT]             = $model->field["KARI_DIV"];
            $data["KARI_TESTCD"][TEXT]          = $model->field["KARI_TESTCD"];
            $data["CONVERT_FLG"][TEXT]          = $model->field["CONVERT"];
            $data["KEEKA_OVER_FLG"][TEXT]       = $model->field["KEEKA_OVER"];
            $data["SIDOU_FLG"][TEXT]            = $model->field["SIDOU"];
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
            $query = knjd214uQuery::getDeleteRireki($calcDate, $calcTime, $model);
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
        $array  = (strlen($model->selectdata2) ? explode(",", $model->selectdata2) : array());
        $gr_cl  = implode("','", $array);
        //対象科目リスト
        $subcdArray  = (strlen($model->selectdataKamoku) ? explode(",", $model->selectdataKamoku) : array());
        $subcdIn = implode("','", $subcdArray);

        $query  = "";
        $query .= " SELECT ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= " T3.CLASSCD, ";
            $query .= " T3.SCHOOL_KIND, ";
            $query .= " T3.CURRICULUM_CD, ";
        }
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
        if ($model->field["CHOICE"] == "2") {
            $query .= "     T1.SCHREGNO IN ('".$gr_cl."') ";
        } else {
            $query .= "     T1.GRADE || T1.HR_CLASS IN ('".$gr_cl."') ";
        }
        //処理対象科目
        $query .= "     AND T3.CLASSCD || '-' || T3.SCHOOL_KIND || '-' || T3.CURRICULUM_CD || '-' || T3.SUBCLASSCD IN ('".$subcdIn."') ";
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

        $query  = knjd214uQuery::getClearSql($model, CTRL_YEAR);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            //教育課程対応
            if ($model->Properties["useCurriculumcd"] == '1') {
                $classCd        = $row["CLASSCD"];
                $schoolKind     = $row["SCHOOL_KIND"];
                $curriculumCd   = $row["CURRICULUM_CD"];
            }
            $subCd      = $row["SUBCLASSCD"];
            $schno      = $row["SCHREGNO"];
            //RECORD_SCORE_DATのレコードを削除。
            $query = knjd214uQuery::getDeleteRecordDat(CTRL_YEAR, $sem, $kind, $item, $div, $subCd, $schno, $classCd, $schoolKind, $curriculumCd, $model);
            $db->query($query);

            //RECORD_PROV_FLG_DATのレコードを削除
            $query = knjd214uQuery::delRecordProvFlgDat(CTRL_YEAR, $subCd, $schno, $classCd, $schoolKind, $curriculumCd, $model);
            $db->query($query);
        }
        $result->free();

        $db->commit();
        Query::dbCheckIn($db);
    }

    //クリア処理が行われたかチェック
    function getClearExists($model, $year) {
        //対象者(左のリスト）
        $array  = (strlen($model->selectdata2) ? explode(",", $model->selectdata2) : array());
        $gr_cl  = implode("','", $array);
        //対象科目リスト
        $subcdArray  = (strlen($model->selectdataKamoku) ? explode(",", $model->selectdataKamoku) : array());
        $subcdIn = implode("','", $subcdArray);

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
        if ($model->field["CHOICE"] == "2") {
            $query .= "     T1.SCHREGNO IN ('".$gr_cl."') ";
        } else {
            $query .= "     T1.GRADE || T1.HR_CLASS IN ('".$gr_cl."') ";
        }
        //処理対象科目
        $query .= "     AND T3.CLASSCD || '-' || T3.SCHOOL_KIND || '-' || T3.CURRICULUM_CD || '-' || T3.SUBCLASSCD IN ('".$subcdIn."') ";
        return $query;
    }

    //評価の算出方法(1:四捨五入 2:切り上げ 3:切り捨て)
    function getCalcMethod() {
        $query  = " SELECT ";
        $query .= "     SCHOOL_REMARK1  ";
        $query .= " FROM ";
        $query .= "     SCHOOL_DETAIL_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' AND ";
        $query .= "     SCHOOL_SEQ = '007' ";

        $db = Query::dbCheckOut();
        $rtnRow = array();
        $rtnRow = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);

        return $rtnRow;
    }

    //処理対象レコード（コピー）
    function getCopySql($model, $year) {
        //対象者(左のリスト）
        $array  = (strlen($model->selectdata2) ? explode(",", $model->selectdata2) : array());
        $gr_cl  = implode("','", $array);
        //対象科目リスト
        $subcdArray  = (strlen($model->selectdataKamoku) ? explode(",", $model->selectdataKamoku) : array());
        $subcdIn = implode("','", $subcdArray);
        $assess = ($model->field["CONVERT"] == "on") ? "CASE WHEN R2.CNT IS NOT NULL AND 0 < R2.CNT THEN R1.ASSESSLEVEL ELSE L1.ASSESSLEVEL END" : "T3.SCORE"; //評定換算する／しない
        $isRelaCnt = "CASE WHEN R2.CNT IS NOT NULL AND 0 < R2.CNT THEN 1 ELSE 0 END"; //相対評定マスタの設定があるか

        $query  = "";
        //相対評定マスタ
        $query .= "WITH RELA_CNT AS ( ";
        $query .= "    SELECT ";
        $query .= "        ASSESSCD, ";
        $query .= "        GRADE, ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "    CLASSCD, ";
            $query .= "    SCHOOL_KIND, ";
            $query .= "    CURRICULUM_CD, ";
        }
        $query .= "        SUBCLASSCD, ";
        $query .= "        COUNT(*) AS CNT ";
        $query .= "    FROM ";
        $query .= "        RELATIVEASSESS_MST ";
        $query .= "    GROUP BY ";
        $query .= "        ASSESSCD, ";
        $query .= "        GRADE, ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "    CLASSCD, ";
            $query .= "    SCHOOL_KIND, ";
            $query .= "    CURRICULUM_CD, ";
        }
        $query .= "        SUBCLASSCD ";
        $query .= "    ) ";

        $query .= " SELECT ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= " T3.CLASSCD, ";
            $query .= " T3.SCHOOL_KIND, ";
            $query .= " T3.CURRICULUM_CD, ";
        }
        $query .= "     T3.SUBCLASSCD, ";
        $query .= "     T3.SCHREGNO, ";
        $query .= "     T1.GRADE, ";
        $query .= "     T1.COURSECD || T1.MAJORCD || T1.COURSECODE AS COURSE, ";
        $query .= "     T3.VALUE_DI, ";
        $query .= "     T4.CREDITS, ";
        $query .= "     ".$isRelaCnt." AS IS_RELA_CNT, ";
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
        $query .= "     INNER JOIN RECORD_SCORE_DAT T3 ON T3.SCHREGNO = T1.SCHREGNO ";
        $query .= "                             AND T3.YEAR = '{$year}' ";
        $query .= "                             AND T3.SEMESTER || T3.TESTKINDCD || T3.TESTITEMCD || T3.SCORE_DIV = '{$model->field["TESTKINDCD"]}' ";
        //総学等は処理対象外
        $query .= "                             AND T3.SUBCLASSCD not like '90%' ";
        $query .= "     LEFT JOIN CREDIT_MST T4 ON T4.YEAR = '{$year}' ";
        $query .= "                            AND T4.COURSECD = T1.COURSECD ";
        $query .= "                            AND T4.MAJORCD = T1.MAJORCD ";
        $query .= "                            AND T4.GRADE = T1.GRADE ";
        $query .= "                            AND T4.COURSECODE = T1.COURSECODE ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "                        AND T4.CLASSCD = T3.CLASSCD ";
            $query .= "                        AND T4.SCHOOL_KIND = T3.SCHOOL_KIND ";
            $query .= "                        AND T4.CURRICULUM_CD = T3.CURRICULUM_CD ";
        }
        $query .= "                            AND T4.SUBCLASSCD = T3.SUBCLASSCD ";
        $query .= "     LEFT JOIN ASSESS_MST L1 ON L1.ASSESSCD = '3' ";
        $query .= "                            AND T3.SCORE BETWEEN L1.ASSESSLOW AND L1.ASSESSHIGH ";
        $query .= "     LEFT JOIN RELATIVEASSESS_MST R1 ON R1.ASSESSCD = '3' ";
        $query .= "                            AND T3.SCORE BETWEEN R1.ASSESSLOW AND R1.ASSESSHIGH ";
        $query .= "                            AND R1.GRADE = T1.GRADE ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "                        AND R1.CLASSCD = T3.CLASSCD ";
            $query .= "                        AND R1.SCHOOL_KIND = T3.SCHOOL_KIND ";
            $query .= "                        AND R1.CURRICULUM_CD = T3.CURRICULUM_CD ";
        }
        $query .= "                            AND R1.SUBCLASSCD = T3.SUBCLASSCD ";
        $query .= "     LEFT JOIN RELA_CNT R2 ON R2.ASSESSCD = '3' ";
        $query .= "                            AND R2.GRADE = T1.GRADE ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "                        AND R2.CLASSCD = T3.CLASSCD ";
            $query .= "                        AND R2.SCHOOL_KIND = T3.SCHOOL_KIND ";
            $query .= "                        AND R2.CURRICULUM_CD = T3.CURRICULUM_CD ";
        }
        $query .= "                            AND R2.SUBCLASSCD = T3.SUBCLASSCD ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '{$year}' AND ";
        $query .= "     T1.SEMESTER = '{$model->field["SEMESTER"]}' AND ";
        if ($model->field["CHOICE"] == "2") {
            $query .= "     T1.SCHREGNO IN ('".$gr_cl."') ";
        } else {
            $query .= "     T1.GRADE || T1.HR_CLASS IN ('".$gr_cl."') ";
        }
        //処理対象科目
        $query .= "     AND T3.CLASSCD || '-' || T3.SCHOOL_KIND || '-' || T3.CURRICULUM_CD || '-' || T3.SUBCLASSCD IN ('".$subcdIn."') ";
        return $query;
    }

    //処理対象レコード（合併先科目の単位自動計算）
    function getCombinedCopySql($model, $year) {
        //対象者(左のリスト）
        $array  = (strlen($model->selectdata2) ? explode(",", $model->selectdata2) : array());
        $gr_cl  = implode("','", $array);
        //対象科目リスト
        $subcdArray  = (strlen($model->selectdataKamoku) ? explode(",", $model->selectdataKamoku) : array());
        $subcdIn = implode("','", $subcdArray);

        $query  = "";
        $query .= " WITH T_COMBINED AS ( ";
        $query .= "     SELECT ";
        $query .= "            T2.SCHREGNO, ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "        T1.COMBINED_CLASSCD, ";
            $query .= "        T1.COMBINED_SCHOOL_KIND, ";
            $query .= "        T1.COMBINED_CURRICULUM_CD, ";
        }
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
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "   AND  T2.CLASSCD = T1.ATTEND_CLASSCD ";
            $query .= "   AND  T2.SCHOOL_KIND = T1.ATTEND_SCHOOL_KIND ";
            $query .= "   AND  T2.CURRICULUM_CD = T1.ATTEND_CURRICULUM_CD ";
        }
        $query .= "       AND  T2.SUBCLASSCD = T1.ATTEND_SUBCLASSCD ";
        $query .= "     GROUP BY ";
        $query .= "            T2.SCHREGNO, ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "        T1.COMBINED_CLASSCD, ";
            $query .= "        T1.COMBINED_SCHOOL_KIND, ";
            $query .= "        T1.COMBINED_CURRICULUM_CD, ";
        }
        $query .= "            T1.COMBINED_SUBCLASSCD ";
        $query .= "     ) ";
        $query .= " , T_COMBINED_FLG AS ( ";
        $query .= "     SELECT ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "        T1.COMBINED_CLASSCD, ";
            $query .= "        T1.COMBINED_SCHOOL_KIND, ";
            $query .= "        T1.COMBINED_CURRICULUM_CD, ";
        }
        $query .= "            T1.COMBINED_SUBCLASSCD, ";
        $query .= "            T1.CALCULATE_CREDIT_FLG AS FLG, ";
        $query .= "            COUNT(T1.ATTEND_SUBCLASSCD) AS CNT ";
        $query .= "     FROM   SUBCLASS_REPLACE_COMBINED_DAT T1 ";
        $query .= "     WHERE  T1.YEAR = '{$year}' ";
        $query .= "     GROUP BY ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "        T1.COMBINED_CLASSCD, ";
            $query .= "        T1.COMBINED_SCHOOL_KIND, ";
            $query .= "        T1.COMBINED_CURRICULUM_CD, ";
        }
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
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= " T3.CLASSCD, ";
            $query .= " T3.SCHOOL_KIND, ";
            $query .= " T3.CURRICULUM_CD, ";
        }
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
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "                             AND T6.COMBINED_CLASSCD = T3.CLASSCD ";
            $query .= "                             AND T6.COMBINED_SCHOOL_KIND = T3.SCHOOL_KIND ";
            $query .= "                             AND T6.COMBINED_CURRICULUM_CD = T3.CURRICULUM_CD ";
        }
        $query .= "     LEFT JOIN T_COMBINED T5 ON T5.SCHREGNO = T3.SCHREGNO ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "                        AND T5.COMBINED_CLASSCD = T3.CLASSCD ";
            $query .= "                        AND T5.COMBINED_SCHOOL_KIND = T3.SCHOOL_KIND ";
            $query .= "                        AND T5.COMBINED_CURRICULUM_CD = T3.CURRICULUM_CD ";
        }
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
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "                        AND T4.CLASSCD = T3.CLASSCD ";
            $query .= "                        AND T4.SCHOOL_KIND = T3.SCHOOL_KIND ";
            $query .= "                        AND T4.CURRICULUM_CD = T3.CURRICULUM_CD ";
        }
        $query .= "                            AND T4.SUBCLASSCD = T3.SUBCLASSCD ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '{$year}' AND ";
        $query .= "     T1.SEMESTER = '{$model->field["SEMESTER"]}' AND ";
        if ($model->field["CHOICE"] == "2") {
            $query .= "     T1.SCHREGNO IN ('".$gr_cl."') ";
        } else {
            $query .= "     T1.GRADE || T1.HR_CLASS IN ('".$gr_cl."') ";
        }
        //処理対象科目
        $query .= "     AND T3.CLASSCD || '-' || T3.SCHOOL_KIND || '-' || T3.CURRICULUM_CD || '-' || T3.SUBCLASSCD IN ('".$subcdIn."') ";

        return $query;
    }

    //処理対象レコード（仮評定）
    function getProvCopySql($model, $year) {
        //対象者(左のリスト）
        $array  = (strlen($model->selectdata2) ? explode(",", $model->selectdata2) : array());
        $gr_cl  = implode("','", $array);
        //対象科目リスト
        $subcdArray  = (strlen($model->selectdataKamoku) ? explode(",", $model->selectdataKamoku) : array());
        $subcdIn = implode("','", $subcdArray);

        $query  = "";
        $query .= " SELECT ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= " T3.CLASSCD, ";
            $query .= " T3.SCHOOL_KIND, ";
            $query .= " T3.CURRICULUM_CD, ";
        }
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
        if ($model->field["CHOICE"] == "2") {
            $query .= "     T1.SCHREGNO IN ('".$gr_cl."') ";
        } else {
            $query .= "     T1.GRADE || T1.HR_CLASS IN ('".$gr_cl."') ";
        }
        //処理対象科目
        $query .= "     AND T3.CLASSCD || '-' || T3.SCHOOL_KIND || '-' || T3.CURRICULUM_CD || '-' || T3.SUBCLASSCD IN ('".$subcdIn."') ";
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

        //欠課数上限値が法定時数(1)か実時数(2)か
        $query = knjd214uQuery::getVSchoolMst(CTRL_YEAR);
        $schoolRow = $db->getRow($query, DB_FETCHMODE_ASSOC);

        $query  = knjd214uQuery::getCopySql($model, CTRL_YEAR);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            //追指導データ参照（評定１、欠席の場合のみ）
            if ($model->field["SIDOU"] == "on" && ($row["SCORE"] == "1" || $row["VALUE_DI"] == "*")) {
                //コピー元になる評価・・・追指導入力が記号(1)か点数(2)か
                $query = knjd214uQuery::getSidouInputInf($model, CTRL_YEAR);
                $sidouRow = $db->getRow($query, DB_FETCHMODE_ASSOC);
                if ($sidouRow["SIDOU_INPUT"] == "1") {
                    //記号(1)
                    if ($sidouRow["SIDOU_INPUT_INF"] == "1") {
                        $query = knjd214uQuery::getRecordSlumpMark($model, CTRL_YEAR, $row);
                        $slumpRow = $db->getRow($query, DB_FETCHMODE_ASSOC);
                        //追指導（記号）が全て○の場合、評定２をセット
                        if (0 < $slumpRow["CNT_MARU"] && 0 == $slumpRow["CNT_MARUIGAI"]) {
                            $row["SCORE"]       = "2";
                            $row["COMP_CREDIT"] = $row["CREDITS"];
                            $row["GET_CREDIT"]  = $row["CREDITS"];
                        }
                    }
                    //点数(2)
                    if ($sidouRow["SIDOU_INPUT_INF"] == "2") {
                        $query = knjd214uQuery::getRecordSlumpScore($model, CTRL_YEAR, $row);
                        $slumpRow = $db->getRow($query, DB_FETCHMODE_ASSOC);
                        //追指導（点数）が評定２の下限値以上の場合、評定２をセット
                        if (0 < $slumpRow["CNT"]) {
                            $row["SCORE"]       = "2";
                            $row["COMP_CREDIT"] = $row["CREDITS"];
                            $row["GET_CREDIT"]  = $row["CREDITS"];
                        }
                    }
                }
            }

            //欠課時数オーバーは未履修にする（本評定の場合のみ）
            if ($model->field["KEEKA_OVER"] == "on" && $model->field["KARI_DIV"] == "2" && strlen($row["SCORE"])) {
                //欠課数上限値
                $query = knjd214uQuery::getKeekaOver($model, CTRL_YEAR, $row, $schoolRow);
                $overRow = $db->getRow($query, DB_FETCHMODE_ASSOC);
                if (!strlen($overRow["COMP_ABSENCE_HIGH"])) $overRow["COMP_ABSENCE_HIGH"] = 9999;
                //欠課数
                $query = knjd214uQuery::getKeeka($model, CTRL_YEAR, $row, $schoolRow);
                $keekaRow = $db->getRow($query, DB_FETCHMODE_ASSOC);
                if (!strlen($keekaRow["NOTICE_LATE"])) $keekaRow["NOTICE_LATE"] = 0;

                //欠課数が上限値をオーバーした場合、履修・修得単位に０をセット
                if ($overRow["COMP_ABSENCE_HIGH"] < $keekaRow["NOTICE_LATE"]) {
                    $row["SCORE"]       = "";
                    $row["COMP_CREDIT"] = 0;
                    $row["GET_CREDIT"]  = 0;
                }
            }

            //教育課程対応
            if ($model->Properties["useCurriculumcd"] == '1') {
                $classCd        = $row["CLASSCD"];
                $schoolKind     = $row["SCHOOL_KIND"];
                $curriculumCd   = $row["CURRICULUM_CD"];
            }
            $subCd      = $row["SUBCLASSCD"];
            $schno      = $row["SCHREGNO"];
            $gradValue  = $row["SCORE"];
            $compCredit = $row["COMP_CREDIT"];
            $getCredit  = $row["GET_CREDIT"];
            //評価を学年評定にコピーする。単位もコピーする。
            $query = knjd214uQuery::getInsertRecordDat(CTRL_YEAR, $sem, $kind, $item, $div, $subCd, $schno, $gradValue, $compCredit, $getCredit, $classCd, $schoolKind, $curriculumCd, $model);
            $db->query($query);

            //評定フラグが仮評定の場合
            if ($model->field["KARI_DIV"] == '1') {
                //RECORD_PROV_FLG_DATのレコードを削除
                $query = knjd214uQuery::delRecordProvFlgDat(CTRL_YEAR, $subCd, $schno, $classCd, $schoolKind, $curriculumCd, $model);
                $db->query($query);
                //RECORD_PROV_FLG_DATのレコードを追加
                $query = knjd214uQuery::insRecordProvFlgDat(CTRL_YEAR, $subCd, $schno, $classCd, $schoolKind, $curriculumCd, $model);
                $db->query($query);
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

        $query  = knjd214uQuery::getCombinedCopySql($model, CTRL_YEAR);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            //教育課程対応
            if ($model->Properties["useCurriculumcd"] == '1') {
                $classCd        = $row["CLASSCD"];
                $schoolKind     = $row["SCHOOL_KIND"];
                $curriculumCd   = $row["CURRICULUM_CD"];
            }
            $subCd      = $row["SUBCLASSCD"];
            $schno      = $row["SCHREGNO"];
            $gradValue  = $row["SCORE"];
            $compCredit = $row["COMP_CREDIT"];
            $getCredit  = $row["GET_CREDIT"];
            //再処理のため、RECORD_SCORE_DATのレコードを一旦削除。
            $query = knjd214uQuery::getDeleteRecordDat(CTRL_YEAR, $sem, $kind, $item, $div, $subCd, $schno, $classCd, $schoolKind, $curriculumCd, $model);
            $db->query($query);
            //評価を学年評定にコピーする。単位もコピーする。
            $query = knjd214uQuery::getInsertRecordDat(CTRL_YEAR, $sem, $kind, $item, $div, $subCd, $schno, $gradValue, $compCredit, $getCredit, $classCd, $schoolKind, $curriculumCd, $model);
            $db->query($query);

            //再処理のため、RECORD_PROV_FLG_DATのレコードを一旦削除
            $query = knjd214uQuery::delRecordProvFlgDat(CTRL_YEAR, $subCd, $schno, $classCd, $schoolKind, $curriculumCd, $model);
            $db->query($query);
            //評定フラグが仮評定の場合
            if ($model->field["KARI_DIV"] == '1') {
                //RECORD_PROV_FLG_DATのレコードを追加
                $query = knjd214uQuery::insRecordProvFlgDat(CTRL_YEAR, $subCd, $schno, $classCd, $schoolKind, $curriculumCd, $model);
                $db->query($query);
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
        $query  = knjd214uQuery::getProvCopySql($model, CTRL_YEAR);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            //教育課程対応
            if ($model->Properties["useCurriculumcd"] == '1') {
                $classCd        = $row["CLASSCD"];
                $schoolKind     = $row["SCHOOL_KIND"];
                $curriculumCd   = $row["CURRICULUM_CD"];
            }
            $subCd      = $row["SUBCLASSCD"];
            $schno      = $row["SCHREGNO"];
            $gradValue  = $row["SCORE"];
            $compCredit = $row["COMP_CREDIT"];
            $getCredit  = $row["GET_CREDIT"];
            //レコードを一旦削除。
            $query = knjd214uQuery::getDeleteRecordDat(CTRL_YEAR, $sem, $kind, $item, $div, $subCd, $schno, $classCd, $schoolKind, $curriculumCd, $model);
            $db->query($query);
            //仮評定にコピー。
            $query = knjd214uQuery::getInsertRecordDat(CTRL_YEAR, $sem, $kind, $item, $div, $subCd, $schno, $gradValue, $compCredit, $getCredit, $classCd, $schoolKind, $curriculumCd, $model);
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
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= " AND CLASSCD = '{$classCd}' ";
            $query .= " AND SCHOOL_KIND = '{$schoolKind}' ";
            $query .= " AND CURRICULUM_CD = '{$curriculumCd}' ";
        }
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
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $data["CLASSCD"][TEXT]       = $classCd;
            $data["SCHOOL_KIND"][TEXT]   = $schoolKind;
            $data["CURRICULUM_CD"][TEXT] = $curriculumCd;
        }
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
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= " AND CLASSCD = '{$classCd}' ";
            $query .= " AND SCHOOL_KIND = '{$schoolKind}' ";
            $query .= " AND CURRICULUM_CD = '{$curriculumCd}' ";
        }
        $query .= "     AND SUBCLASSCD = '{$subCd}' ";
        $query .= "     AND SCHREGNO = '{$schno}' ";
        return $query;
    }

    //RECORD_PROV_FLG_DATのレコードを追加
    function insRecordProvFlgDat($year, $subCd, $schno, $classCd, $schoolKind, $curriculumCd, $model) {
        $data = array();
        $data["YEAR"][TEXT]         = $year;
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $data["CLASSCD"][TEXT]       = $classCd;
            $data["SCHOOL_KIND"][TEXT]   = $schoolKind;
            $data["CURRICULUM_CD"][TEXT] = $curriculumCd;
        }
        $data["SUBCLASSCD"][TEXT]   = $subCd;
        $data["SCHREGNO"][TEXT]     = $schno;
        //評定フラグ・・・仮評定(1)、本評定(NULL)
        $data["PROV_FLG"][TEXT]     = "1";
        $data["REGISTERCD"][TEXT]   = STAFFCD;
        $data["UPDATED"][FUNC]      = "SYSDATE()";

        $query = Query::insertSQL($data, "RECORD_PROV_FLG_DAT");
        return $query;
    }

    //コピー元になる評価・・・追指導入力が記号か点数か
    function getSidouInputInf($model, $year) {
        $query  = " SELECT ";
        $query .= "     T1.SIDOU_INPUT, ";
        $query .= "     T1.SIDOU_INPUT_INF ";
        $query .= " FROM ";
        $query .= "     TESTITEM_MST_COUNTFLG_NEW_SDIV T1 ";
        $query .= "     LEFT JOIN SEMESTER_MST L1 ON L1.YEAR = T1.YEAR AND L1.SEMESTER = T1.SEMESTER ";
        $query .= " WHERE ";
        $query .= "         T1.YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND T1.SEMESTER || T1.TESTKINDCD || T1.TESTITEMCD || T1.SCORE_DIV = '{$model->field["TESTKINDCD"]}' ";
        return $query;
    }

    //追指導データ（点数）
    //追指導（点数）が評定２の下限値以上の場合、評定２をセット
    function getRecordSlumpScore($model, $year, $row) {
        $query  = "";
        $query .= " SELECT ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     RECORD_SLUMP_SDIV_DAT SLUMP ";
        $query .= "     INNER JOIN TESTITEM_MST_COUNTFLG_NEW_SDIV TEST ";
        $query .= "         ON  TEST.YEAR         = SLUMP.YEAR ";
        $query .= "         AND TEST.SEMESTER     = SLUMP.SEMESTER ";
        $query .= "         AND TEST.TESTKINDCD   = SLUMP.TESTKINDCD ";
        $query .= "         AND TEST.TESTITEMCD   = SLUMP.TESTITEMCD ";
        $query .= "         AND TEST.SCORE_DIV    = SLUMP.SCORE_DIV ";
        $query .= "         AND TEST.SIDOU_INPUT  = '1' ";      //追指導入力
        $query .= "         AND TEST.SIDOU_INPUT_INF  = '2' ";  //追指導（点数）
        //相対評定マスタの設定があるか
        if (strlen($row["IS_RELA_CNT"]) && 0 < $row["IS_RELA_CNT"]) {
            $query .= "     INNER JOIN RELATIVEASSESS_MST ASSESS ";
            $query .= "         ON  ASSESS.ASSESSCD = '3' ";
            $query .= "         AND ASSESS.ASSESSLEVEL = 2 ";
            $query .= "         AND ASSESS.ASSESSLOW <= SLUMP.SCORE ";
            $query .= "         AND ASSESS.GRADE = '{$row["GRADE"]}' ";
            //教育課程対応
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "     AND ASSESS.CLASSCD        = '{$row["CLASSCD"]}' ";
                $query .= "     AND ASSESS.SCHOOL_KIND    = '{$row["SCHOOL_KIND"]}' ";
                $query .= "     AND ASSESS.CURRICULUM_CD  = '{$row["CURRICULUM_CD"]}' ";
                $query .= "     AND ASSESS.SUBCLASSCD     = '{$row["SUBCLASSCD"]}' ";
            } else {
                $query .= "     AND ASSESS.SUBCLASSCD     = '{$row["SUBCLASSCD"]}' ";
            }
        } else {
            $query .= "     INNER JOIN ASSESS_MST ASSESS ";
            $query .= "         ON  ASSESS.ASSESSCD = '3' ";
            $query .= "         AND ASSESS.ASSESSLEVEL = 2 ";
            $query .= "         AND ASSESS.ASSESSLOW <= SLUMP.SCORE ";
        }
        $query .= " WHERE ";
        $query .= "         SLUMP.YEAR = '{$year}' ";
        $query .= "     AND SLUMP.SEMESTER || SLUMP.TESTKINDCD || SLUMP.TESTITEMCD || SLUMP.SCORE_DIV = '{$model->field["TESTKINDCD"]}' ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     AND SLUMP.CLASSCD        = '{$row["CLASSCD"]}' ";
            $query .= "     AND SLUMP.SCHOOL_KIND    = '{$row["SCHOOL_KIND"]}' ";
            $query .= "     AND SLUMP.CURRICULUM_CD  = '{$row["CURRICULUM_CD"]}' ";
            $query .= "     AND SLUMP.SUBCLASSCD     = '{$row["SUBCLASSCD"]}' ";
        } else {
            $query .= "     AND SLUMP.SUBCLASSCD     = '{$row["SUBCLASSCD"]}' ";
        }
        $query .= "     AND SLUMP.SCHREGNO = '{$row["SCHREGNO"]}' ";
        return $query;
    }

    //追指導データ（記号）
    //追指導（記号）が全て○の場合、評定２をセット
    function getRecordSlumpMark($model, $year, $row) {
        $query  = "";
        $query .= " SELECT ";
        $query .= "     COUNT(CASE WHEN SLUMP.MARK  = '1' THEN 1 END) AS CNT_MARU, ";
        $query .= "     COUNT(CASE WHEN SLUMP.MARK != '1' THEN 1 END) AS CNT_MARUIGAI ";
        $query .= " FROM ";
        $query .= "     RECORD_SLUMP_SDIV_DAT SLUMP ";
        $query .= "     INNER JOIN TESTITEM_MST_COUNTFLG_NEW_SDIV TEST ";
        $query .= "         ON  TEST.YEAR         = SLUMP.YEAR ";
        $query .= "         AND TEST.SEMESTER     = SLUMP.SEMESTER ";
        $query .= "         AND TEST.TESTKINDCD   = SLUMP.TESTKINDCD ";
        $query .= "         AND TEST.TESTITEMCD   = SLUMP.TESTITEMCD ";
        $query .= "         AND TEST.SCORE_DIV    = SLUMP.SCORE_DIV ";
        $query .= "         AND TEST.SIDOU_INPUT  = '1' ";      //追指導入力
        $query .= "         AND TEST.SIDOU_INPUT_INF  = '1' ";  //追指導（記号）
        $query .= " WHERE ";
        $query .= "         SLUMP.YEAR = '{$year}' ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     AND SLUMP.CLASSCD        = '{$row["CLASSCD"]}' ";
            $query .= "     AND SLUMP.SCHOOL_KIND    = '{$row["SCHOOL_KIND"]}' ";
            $query .= "     AND SLUMP.CURRICULUM_CD  = '{$row["CURRICULUM_CD"]}' ";
            $query .= "     AND SLUMP.SUBCLASSCD     = '{$row["SUBCLASSCD"]}' ";
        } else {
            $query .= "     AND SLUMP.SUBCLASSCD     = '{$row["SUBCLASSCD"]}' ";
        }
        $query .= "     AND SLUMP.SCHREGNO = '{$row["SCHREGNO"]}' ";
        $query .= "     AND SLUMP.MARK IS NOT NULL ";
        return $query;
    }

    //V学校マスタ
    function getVSchoolMst($year) {
        $query  = "";
        $query .= " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     V_SCHOOL_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$year}' ";

        return $query;
    }

    //欠課数上限値
    function getKeekaOver($model, $year, $row, $schoolRow) {
        $query  = "";

        //法定時数(1)
        if ($schoolRow["JUGYOU_JISU_FLG"] == "1") {
            $query .= " SELECT ";
            $query .= "     ABSENCE_HIGH AS COMP_ABSENCE_HIGH ";
            $query .= " FROM ";
            $query .= "     CREDIT_MST ";
            $query .= " WHERE ";
            $query .= "     YEAR = '{$year}' ";
            $query .= "     AND GRADE = '{$row["GRADE"]}' ";
            $query .= "     AND COURSECD || MAJORCD || COURSECODE = '{$row["COURSE"]}' ";
            //教育課程対応
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "     AND CLASSCD        = '{$row["CLASSCD"]}' ";
                $query .= "     AND SCHOOL_KIND    = '{$row["SCHOOL_KIND"]}' ";
                $query .= "     AND CURRICULUM_CD  = '{$row["CURRICULUM_CD"]}' ";
                $query .= "     AND SUBCLASSCD     = '{$row["SUBCLASSCD"]}' ";
            } else {
                $query .= "     AND SUBCLASSCD     = '{$row["SUBCLASSCD"]}' ";
            }

        //実時数(2)
        } else {
            $query .= " SELECT ";
            $query .= "     COMP_ABSENCE_HIGH ";
            $query .= " FROM ";
            $query .= "     SCHREG_ABSENCE_HIGH_DAT ";
            $query .= " WHERE ";
            $query .= "     YEAR = '{$year}' ";
            $query .= "     AND DIV = '1' "; //年間
            //教育課程対応
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "     AND CLASSCD        = '{$row["CLASSCD"]}' ";
                $query .= "     AND SCHOOL_KIND    = '{$row["SCHOOL_KIND"]}' ";
                $query .= "     AND CURRICULUM_CD  = '{$row["CURRICULUM_CD"]}' ";
                $query .= "     AND SUBCLASSCD     = '{$row["SUBCLASSCD"]}' ";
            } else {
                $query .= "     AND SUBCLASSCD     = '{$row["SUBCLASSCD"]}' ";
            }
            $query .= "     AND SCHREGNO = '{$row["SCHREGNO"]}' ";

        }
        return $query;
    }

    //欠課数
    function getKeeka($model, $year, $row, $schoolRow) {
        $query  = "";
        //学期ごとに清算(1,3)
        if ($schoolRow["ABSENT_COV"] == "1" || $schoolRow["ABSENT_COV"] == "3") {
            $query = knjd214uQuery::getKeekaYear($model, CTRL_YEAR, $row, $schoolRow);
        //年間で清算(0,2,4,5)
        } else {
            $query = knjd214uQuery::getKeekaSemester($model, CTRL_YEAR, $row, $schoolRow);
        }
        return $query;
    }

    //欠課数(年間ごとに集計）
    function getKeekaYear($model, $year, $row, $schoolRow) {
        //ABSENT_COV:欠課数換算方法
        //ABSENT_COV_LATE:遅刻X回につき欠課1回
        //AMARI_KURIAGE:余りY回で繰上げ

        //遅刻・早退
        $late_early = "SUM(value(LATE,0)) + SUM(value(EARLY,0))";
        //欠課
        $notice = "SUM(value(NOTICE,0)) + SUM(value(NONOTICE,0)) + SUM(value(SICK,0)) + SUM(value(NURSEOFF,0))";
        //学校マスタの各フラグを参照し「休学・公欠・出廷・忌引・出廷（伝染病）」を欠課に含める処理。
        if ($schoolRow["SUB_OFFDAYS"]  == "1") $notice .= " + SUM(value(OFFDAYS,0))";
        if ($schoolRow["SUB_ABSENT"]   == "1") $notice .= " + SUM(value(ABSENT,0))";
        if ($schoolRow["SUB_SUSPEND"]  == "1") $notice .= " + SUM(value(SUSPEND,0))";
        if ($schoolRow["SUB_MOURNING"] == "1") $notice .= " + SUM(value(MOURNING,0))";
        if ($schoolRow["SUB_VIRUS"]    == "1") $notice .= " + SUM(value(VIRUS,0))";
        if ($schoolRow["SUB_KOUDOME"]  == "1") $notice .= " + SUM(value(KOUDOME,0))";

        //SQL
        $query  = "SELECT SCHREGNO, SUM(NOTICE_LATE) AS NOTICE_LATE ";
        $query .= " FROM ( ";
        $query .= "SELECT SCHREGNO ";
        //4.年間で清算（小数点あり）
        if ($schoolRow["ABSENT_COV"] == "4" && (is_numeric($schoolRow["ABSENT_COV_LATE"]) && (int)$schoolRow["ABSENT_COV_LATE"] != 0)) {
            $query .= "   ,DECIMAL((FLOAT({$late_early}) / {$schoolRow["ABSENT_COV_LATE"]}) + ({$notice}),4,1) AS NOTICE_LATE ";
        //5.年間で清算（余りで換算）
        } elseif ($schoolRow["ABSENT_COV"] == "5" && (is_numeric($schoolRow["ABSENT_COV_LATE"]) && (int)$schoolRow["ABSENT_COV_LATE"] != 0)) {
            $query .= "   ,(({$late_early}) / {$schoolRow["ABSENT_COV_LATE"]}) + ({$notice}) ";
            $query .= "     + (CASE WHEN MOD({$late_early} , {$schoolRow["ABSENT_COV_LATE"]}) >= {$schoolRow["AMARI_KURIAGE"]} ";
            $query .= "             THEN 1 ";
            $query .= "             ELSE 0 ";
            $query .= "     END) AS NOTICE_LATE ";
        //2.年間で清算
        } elseif ($schoolRow["ABSENT_COV"] == "2" && (is_numeric($schoolRow["ABSENT_COV_LATE"]) && (int)$schoolRow["ABSENT_COV_LATE"] != 0)) {
            $query .= "   ,(({$late_early}) / {$schoolRow["ABSENT_COV_LATE"]}) + ({$notice}) AS NOTICE_LATE ";
        //0.年間で清算（換算なし）
        } else {
            $query .= "   ,{$notice}  AS NOTICE_LATE ";
        }
        $query .= "  FROM ATTEND_SUBCLASS_DAT  ";
        $query .= " WHERE COPYCD     = '0' ";
        $query .= "   AND YEAR       = '{$year}' ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "   AND CLASSCD       = '{$row["CLASSCD"]}' ";
            $query .= "   AND SCHOOL_KIND   = '{$row["SCHOOL_KIND"]}' ";
            $query .= "   AND CURRICULUM_CD = '{$row["CURRICULUM_CD"]}' ";
            $query .= "   AND SUBCLASSCD    = '{$row["SUBCLASSCD"]}' ";
        } else {
            $query .= "   AND SUBCLASSCD    = '{$row["SUBCLASSCD"]}' ";
        }
        $query .= "   AND SCHREGNO  = '{$row["SCHREGNO"]}' ";
        $query .= " GROUP BY SCHREGNO";
        $query .= " UNION ALL ";
        $query .= "SELECT SCHREGNO ";
        //4.年間で清算（小数点あり）
        if ($schoolRow["ABSENT_COV"] == "4" && (is_numeric($schoolRow["ABSENT_COV_LATE"]) && (int)$schoolRow["ABSENT_COV_LATE"] != 0)) {
            $query .= "   ,DECIMAL((FLOAT({$late_early}) / {$schoolRow["ABSENT_COV_LATE"]}) + ({$notice}),4,1) AS NOTICE_LATE ";
        //5.年間で清算（余りで換算）
        } elseif ($schoolRow["ABSENT_COV"] == "5" && (is_numeric($schoolRow["ABSENT_COV_LATE"]) && (int)$schoolRow["ABSENT_COV_LATE"] != 0)) {
            $query .= "   ,(({$late_early}) / {$schoolRow["ABSENT_COV_LATE"]}) + ({$notice}) ";
            $query .= "     + (CASE WHEN MOD({$late_early} , {$schoolRow["ABSENT_COV_LATE"]}) >= {$schoolRow["AMARI_KURIAGE"]} ";
            $query .= "             THEN 1 ";
            $query .= "             ELSE 0 ";
            $query .= "     END) AS NOTICE_LATE ";
        //2.年間で清算
        } elseif ($schoolRow["ABSENT_COV"] == "2" && (is_numeric($schoolRow["ABSENT_COV_LATE"]) && (int)$schoolRow["ABSENT_COV_LATE"] != 0)) {
            $query .= "   ,(({$late_early}) / {$schoolRow["ABSENT_COV_LATE"]}) + ({$notice}) AS NOTICE_LATE ";
        //0.年間で清算（換算なし）
        } else {
            $query .= "   ,{$notice}  AS NOTICE_LATE ";
        }
        $query .= "  FROM ATTEND_SUBCLASS_DAT T1 ";
        $query .= "  INNER JOIN SUBCLASS_REPLACE_COMBINED_DAT T2 ON T2.YEAR = T1.YEAR ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "       AND T2.COMBINED_CLASSCD       = '{$row["CLASSCD"]}' ";
            $query .= "       AND T2.COMBINED_SCHOOL_KIND   = '{$row["SCHOOL_KIND"]}' ";
            $query .= "       AND T2.COMBINED_CURRICULUM_CD = '{$row["CURRICULUM_CD"]}' ";
            $query .= "       AND T2.COMBINED_SUBCLASSCD    = '{$row["SUBCLASSCD"]}' ";
            $query .= "       AND T2.ATTEND_CLASSCD         = T1.CLASSCD ";
            $query .= "       AND T2.ATTEND_SCHOOL_KIND     = T1.SCHOOL_KIND ";
            $query .= "       AND T2.ATTEND_CURRICULUM_CD   = T1.CURRICULUM_CD ";
            $query .= "       AND T2.ATTEND_SUBCLASSCD      = T1.SUBCLASSCD ";
        } else {
            $query .= "       AND T2.COMBINED_SUBCLASSCD    = '{$row["SUBCLASSCD"]}' ";
            $query .= "       AND T2.ATTEND_SUBCLASSCD      = T1.SUBCLASSCD ";
        }
        $query .= " WHERE COPYCD     = '0' ";
        $query .= "   AND T1.YEAR       = '{$year}' ";
        $query .= "   AND SCHREGNO  = '{$row["SCHREGNO"]}' ";
        $query .= " GROUP BY SCHREGNO";
        $query .= " ) T1 GROUP BY T1.SCHREGNO";

        return $query;
    }

    //欠課数(学期ごとに集計）
    function getKeekaSemester($model, $year, $row, $schoolRow) {
        //ABSENT_COV:欠課数換算方法
        //ABSENT_COV_LATE:遅刻X回につき欠課1回
        //AMARI_KURIAGE:余りY回で繰上げ

        //遅刻・早退
        $late_early = "SUM(value(LATE,0)) + SUM(value(EARLY,0))";
        //欠課
        $notice = "SUM(value(NOTICE,0)) + SUM(value(NONOTICE,0)) + SUM(value(SICK,0)) + SUM(value(NURSEOFF,0))";
        //学校マスタの各フラグを参照し「休学・公欠・出廷・忌引・出廷（伝染病）」を欠課に含める処理。
        if ($schoolRow["SUB_OFFDAYS"]  == "1") $notice .= " + SUM(value(OFFDAYS,0))";
        if ($schoolRow["SUB_ABSENT"]   == "1") $notice .= " + SUM(value(ABSENT,0))";
        if ($schoolRow["SUB_SUSPEND"]  == "1") $notice .= " + SUM(value(SUSPEND,0))";
        if ($schoolRow["SUB_MOURNING"] == "1") $notice .= " + SUM(value(MOURNING,0))";
        if ($schoolRow["SUB_VIRUS"]    == "1") $notice .= " + SUM(value(VIRUS,0))";
        if ($schoolRow["SUB_KOUDOME"]  == "1") $notice .= " + SUM(value(KOUDOME,0))";

        //SQL
        $query  = "SELECT SCHREGNO ";
        $query .= "       ,SUM(NOTICE_LATE) AS NOTICE_LATE";
        $query .= "  FROM ";
        $query .= "       (SELECT  T1.SCHREGNO ";
        $query .= "               ,T1.SEMESTER ";
        //3.学期ごとに清算（小数点あり）
        if ($schoolRow["ABSENT_COV"] == "3" && (is_numeric($schoolRow["ABSENT_COV_LATE"]) && (int)$schoolRow["ABSENT_COV_LATE"] != 0)) {
            $query .= "           ,DECIMAL((FLOAT({$late_early}) / {$schoolRow["ABSENT_COV_LATE"]}) + ({$notice}),4,1) AS NOTICE_LATE ";
        //1.学期ごとに清算
        } elseif ($schoolRow["ABSENT_COV"] == "1" && (is_numeric($schoolRow["ABSENT_COV_LATE"]) && (int)$schoolRow["ABSENT_COV_LATE"] != 0)) {
            $query .= "           ,(({$late_early}) / {$schoolRow["ABSENT_COV_LATE"]}) + ({$notice}) AS NOTICE_LATE ";
        //0.学期ごとに清算（換算なし）
        } else {
            $query .= "           ,{$notice} AS NOTICE_LATE ";
        }
        $query .= "          FROM ATTEND_SUBCLASS_DAT T1   ";
        $query .= "         WHERE COPYCD        = '0' ";
        $query .= "           AND T1.YEAR       = '{$year}' ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "       AND T1.CLASSCD       = '{$row["CLASSCD"]}' ";
            $query .= "       AND T1.SCHOOL_KIND   = '{$row["SCHOOL_KIND"]}' ";
            $query .= "       AND T1.CURRICULUM_CD = '{$row["CURRICULUM_CD"]}' ";
            $query .= "       AND T1.SUBCLASSCD    = '{$row["SUBCLASSCD"]}' ";
        } else {
            $query .= "       AND T1.SUBCLASSCD    = '{$row["SUBCLASSCD"]}' ";
        }
        $query .= "           AND T1.SCHREGNO   = '{$row["SCHREGNO"]}' ";
        $query .= "         GROUP BY T1.SCHREGNO, T1.SEMESTER ";
        // 合併先
        $query .= " UNION ALL ";
        $query .= "        SELECT  T1.SCHREGNO ";
        $query .= "               ,T1.SEMESTER ";
        //3.学期ごとに清算（小数点あり）
        if ($schoolRow["ABSENT_COV"] == "3" && (is_numeric($schoolRow["ABSENT_COV_LATE"]) && (int)$schoolRow["ABSENT_COV_LATE"] != 0)) {
            $query .= "           ,DECIMAL((FLOAT({$late_early}) / {$schoolRow["ABSENT_COV_LATE"]}) + ({$notice}),4,1) AS NOTICE_LATE ";
        //1.学期ごとに清算
        } elseif ($schoolRow["ABSENT_COV"] == "1" && (is_numeric($schoolRow["ABSENT_COV_LATE"]) && (int)$schoolRow["ABSENT_COV_LATE"] != 0)) {
            $query .= "           ,(({$late_early}) / {$schoolRow["ABSENT_COV_LATE"]}) + ({$notice}) AS NOTICE_LATE ";
        //0.学期ごとに清算（換算なし）
        } else {
            $query .= "           ,{$notice} AS NOTICE_LATE ";
        }
        $query .= "          FROM ATTEND_SUBCLASS_DAT T1   ";
        $query .= "          INNER JOIN SUBCLASS_REPLACE_COMBINED_DAT T2 ON T2.YEAR = T1.YEAR ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "       AND T2.COMBINED_CLASSCD       = '{$row["CLASSCD"]}' ";
            $query .= "       AND T2.COMBINED_SCHOOL_KIND   = '{$row["SCHOOL_KIND"]}' ";
            $query .= "       AND T2.COMBINED_CURRICULUM_CD = '{$row["CURRICULUM_CD"]}' ";
            $query .= "       AND T2.COMBINED_SUBCLASSCD    = '{$row["SUBCLASSCD"]}' ";
            $query .= "       AND T2.ATTEND_CLASSCD         = T1.CLASSCD ";
            $query .= "       AND T2.ATTEND_SCHOOL_KIND     = T1.SCHOOL_KIND ";
            $query .= "       AND T2.ATTEND_CURRICULUM_CD   = T1.CURRICULUM_CD ";
            $query .= "       AND T2.ATTEND_SUBCLASSCD      = T1.SUBCLASSCD ";
        } else {
            $query .= "       AND T2.COMBINED_SUBCLASSCD    = '{$row["SUBCLASSCD"]}' ";
            $query .= "       AND T2.ATTEND_SUBCLASSCD      = T1.SUBCLASSCD ";
        }
        $query .= "         WHERE COPYCD        = '0' ";
        $query .= "           AND T1.YEAR       = '{$year}' ";
        $query .= "           AND T1.SCHREGNO   = '{$row["SCHREGNO"]}' ";
        $query .= "         GROUP BY T1.SCHREGNO, T1.SEMESTER) T1 ";
        $query .= " GROUP BY SCHREGNO ";

        return $query;
    }
}
?>
