<?php

require_once('for_php7.php');
class knjd214vQuery extends Query
{

    //追指導入力設定があるか
    public function getSidouInputCount($model)
    {
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
    public function getAuth($model)
    {
        $query  = "SELECT T1.GRADE || T1.HR_CLASS AS VALUE,T1.HR_NAME AS LABEL ";
        $query .= "FROM SCHREG_REGD_HDAT T1 ";
        $query .= " LEFT JOIN SCHREG_REGD_GDAT T2 ON T2.YEAR  = T1.YEAR ";
        $query .= "                              AND T2.GRADE = T1.GRADE ";
        $query .= "WHERE T1.YEAR = '" .CTRL_YEAR."'";
        $query .= "  AND T1.SEMESTER = '".$model->field["SEMESTER"]."' ";
        if ($model->field["GRADE"] != "999") {
            $query .= "  AND T1.GRADE = '".$model->field["GRADE"] ."'";
        }
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= " AND T2.SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind), "','")."') ";
            }
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND T2.SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= "ORDER BY VALUE ";

        return $query;
    }
    //年組(履歴用)
    public function getRirekiHrName($year, $semester, $hrClass)
    {
        $query  = "SELECT HR_NAME ";
        $query .= "FROM SCHREG_REGD_HDAT ";
        $query .= "WHERE YEAR = '{$year}'";
        $query .= "  AND SEMESTER = '{$semester}' ";
        $query .= "  AND GRADE || HR_CLASS = '{$hrClass}'";
        return $query;
    }

    //学年コンボ
    public function getSelectGrade($model)
    {
        $query  = "SELECT DISTINCT ";
        $query .= "    GRADE_NAME1 AS LABEL, ";
        $query .= "    GRADE AS VALUE ";
        $query .= "FROM ";
        $query .= "    SCHREG_REGD_GDAT ";
        $query .= "WHERE YEAR = '".CTRL_YEAR."' ";
        if ($model->Properties["use_prg_schoolkind"] == "1") {
            if ($model->selectSchoolKind) {
                $query .= " AND SCHOOL_KIND IN ('".implode(explode(':', $model->selectSchoolKind), "','")."') ";
            }
        } elseif ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND SCHOOL_KIND = '".SCHOOLKIND."' ";
        }
        $query .= "ORDER BY VALUE ";

        return $query;
    }

    //学期取得
    public function getSemester($cmbFlg = "")
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
    public function getRirekiSemesterName($year, $semester)
    {
        $query  = "SELECT SEMESTERNAME ";
        $query .= "FROM SEMESTER_MST ";
        $query .= "WHERE YEAR = '{$year}'";
        $query .= "  AND SEMESTER = '{$semester}' ";
        return $query;
    }

    //テスト種別取得（コピー元になる評価コンボ）
    public function getTestItem($model, $flg = "")
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
            $query .= "     AND (T1.SCORE_DIV IN ('08','09') ";
            $query .= "         OR T1.SEMESTER || '-' || T1.TESTKINDCD || '-' || T1.TESTITEMCD || '-' || T1.SCORE_DIV IN (SELECT NAME1 FROM V_NAME_MST WHERE YEAR = '".CTRL_YEAR."' AND NAMECD1 = 'D111')) ";
        }
        //学年評定は表示しない
        $query .= "         AND T1.SEMESTER || T1.TESTKINDCD || T1.TESTITEMCD || T1.SCORE_DIV <> '{$model->field["COPY_SAKI_CD"]}' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }
    //テスト種別(履歴用)
    public function getRirekiTestName($year, $testcd)
    {
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

    //処理対象科目
    public function getHankiCnt($model, $sem)
    {
        $query  = "";
        $query .= " SELECT ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM ";
        $query .= "     SUBCLASS_DETAIL_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND SUBCLASS_SEQ = '012' ";
        $query .= "     AND SUBCLASS_REMARK{$sem} = '1' ";
        return $query;
    }

    //処理対象科目SQL
    public function getHankiSql($model)
    {
        $query  = "";

        $sqlArray = array();
        for ($s = 0; $s <= 3; $s++) {
            if ($model->field["SUBCLASS_REMARK".$s] == "on") {
                if ($s == 0) {
                    $in = "not in";
                    $subRem = "W2.SUBCLASS_REMARK1 = '1' OR W2.SUBCLASS_REMARK2 = '1' OR W2.SUBCLASS_REMARK3 = '1'";
                } else {
                    $in = "in";
                    $subRem = "W2.SUBCLASS_REMARK{$s} = '1'";
                }
                //sql
                //教育課程対応
                if ($model->Properties["useCurriculumcd"] == '1') {
                    $sqlArray[$s] = " T3.CLASSCD || T3.SCHOOL_KIND || T3.CURRICULUM_CD || T3.SUBCLASSCD {$in} (SELECT W2.CLASSCD || W2.SCHOOL_KIND || W2.CURRICULUM_CD || W2.SUBCLASSCD FROM SUBCLASS_DETAIL_DAT W2 WHERE W2.YEAR = '".CTRL_YEAR."' AND W2.SUBCLASS_SEQ = '012' AND ({$subRem})) ";
                } else {
                    $sqlArray[$s] = " T3.SUBCLASSCD {$in} (SELECT W2.SUBCLASSCD FROM SUBCLASS_DETAIL_DAT W2 WHERE W2.YEAR = '".CTRL_YEAR."' AND W2.SUBCLASS_SEQ = '012' AND ({$subRem})) ";
                }
            }
        }
        $or = "";
        foreach ($sqlArray as $s => $sql) {
            $query .= $or .$sql;
            $or = " OR ";
        }
//echo $query;

        return $query;
    }

    //履歴一覧
    public function getListRireki($model)
    {
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
    public function executeRireki($model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //実行日付・時間を取得
        $calcRow = $db->getRow(knjd214vQuery::getCalcDateTime(), DB_FETCHMODE_ASSOC);
        $calcDate = $calcRow["CALC_DATE"];//実行日付
        $calcTime = $calcRow["CALC_TIME"];//実行時間
        //実行履歴データ・追加
        $query = knjd214vQuery::getInsertRireki($calcDate, $calcTime, $model);
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
        $data["PROGRAMID"][TEXT]            = PROGRAMID;
        $data["YEAR"][TEXT]                 = CTRL_YEAR;
        $data["SEMESTER"][TEXT]             = $model->field["SEMESTER"];
        //カンマ区切りでセットしておく
        $data["SELECT_HR_CLASS"][TEXT]      = $model->selectdata; //03001,03002
        $subRemark = "";
        $seq = "";
        for ($s = 0; $s <= 3; $s++) {
            if ($model->field["SUBCLASS_REMARK".$s] == "on") {
                $subRemark .= $seq . $s;
                $seq = ",";
            }
        }
        $data["SELECT_SUBCLASS"][TEXT]      = $subRemark; //0,1,2,3
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
    public function deleteRireki($model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        foreach ($model->del_check as $val) {
            $arrDel = explode(",", $val);
            $calcDate = str_replace("/", "-", $arrDel[0]);
            $calcTime = $arrDel[1];
            //レコード削除は、心配なので、無効フラグを追加して、チェックボックスonの時は、無効フラグ’1’をセツトする。
            $query = knjd214vQuery::getDeleteRireki($calcDate, $calcTime, $model);
            $db->query($query);
        }

        $db->commit();
        Query::dbCheckIn($db);

        return true;
    }
    //レコード削除は、心配なので、無効フラグを追加して、チェックボックスonの時は、無効フラグ’1’をセツトする。
    public function getDeleteRireki($calcDate, $calcTime, $model)
    {
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
    public function getClearSql($model, $year)
    {
        //対象者(左のリスト）
        $array  = (strlen($model->selectdata) ? explode(",", $model->selectdata) : array());
        $gr_cl  = implode("','", $array);

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
        if ($model->field["SOUGAKU"] != "on" && $model->Properties["knjd214vCopyClassCd90"] != '1') {
            $query .= "                         AND T3.SUBCLASSCD not like '90%' ";
        }
        $query .= "     LEFT JOIN V_NAME_MST D008 ON D008.YEAR = '{$year}' ";
        $query .= "                              AND D008.NAMECD1 = CASE WHEN EXISTS (SELECT 'X' FROM V_NAME_MST WHERE YEAR = '{$year}' AND NAMECD1 = 'D' || T3.SCHOOL_KIND || '08') THEN 'D' || T3.SCHOOL_KIND || '08' ";
        $query .= "                                                      ELSE 'D008' ";
        $query .= "                                                 END ";
        $query .= "                              AND D008.NAMECD2 = substr(T3.SUBCLASSCD,1,2) ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '{$year}' AND ";
        $query .= "     T1.SEMESTER = '{$model->field["SEMESTER"]}' AND ";
        $query .= "     T1.GRADE || T1.HR_CLASS IN ('".$gr_cl."') ";
        if ($model->field["SOUGAKU"] != "on") {
            $query .= " AND D008.NAMECD2 IS NULL ";
        }
        //処理対象科目
        if ($model->field["SUBCLASS_REMARK0"] == "on" || $model->field["SUBCLASS_REMARK1"] == "on" || $model->field["SUBCLASS_REMARK2"] == "on" || $model->field["SUBCLASS_REMARK3"] == "on") {
            $query .= " AND (" . knjd214vQuery::getHankiSql($model) . ") ";
        }

        return $query;
    }

    //クリア処理
    public function getClearQuery($model)
    {
        //変数
        $code   = $model->field["COPY_SAKI_CD"];
        $sem    = substr($code, 0, 1);
        $kind   = substr($code, 1, 2);
        $item   = substr($code, 3, 2);
        $div    = substr($code, 5, 2);

        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $query  = knjd214vQuery::getClearSql($model, CTRL_YEAR);
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
            $query = knjd214vQuery::getDeleteRecordDat(CTRL_YEAR, $sem, $kind, $item, $div, $subCd, $schno, $classCd, $schoolKind, $curriculumCd, $model);
            $db->query($query);

            //RECORD_PROV_FLG_DATのレコードを削除
            $query = knjd214vQuery::delRecordProvFlgDat(CTRL_YEAR, $subCd, $schno, $classCd, $schoolKind, $curriculumCd, $model);
            $db->query($query);
        }
        $result->free();

        $db->commit();
        Query::dbCheckIn($db);
    }

    //クリア処理が行われたかチェック
    public function getClearExists($model, $year)
    {
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
        $query .= "     LEFT JOIN V_NAME_MST D008 ON D008.YEAR = '{$year}' ";
        $query .= "                              AND D008.NAMECD1 = CASE WHEN EXISTS (SELECT 'X' FROM V_NAME_MST WHERE YEAR = '{$year}' AND NAMECD1 = 'D' || T3.SCHOOL_KIND || '08') THEN 'D' || T3.SCHOOL_KIND || '08' ";
        $query .= "                                                      ELSE 'D008' ";
        $query .= "                                                 END ";
        $query .= "                              AND D008.NAMECD2 = substr(T3.SUBCLASSCD,1,2) ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '{$year}' AND ";
        $query .= "     T1.SEMESTER = '{$model->field["SEMESTER"]}' AND ";
        $query .= "     T1.GRADE || T1.HR_CLASS IN ('".$gr_cl."') ";
        $query .= "     AND D008.NAMECD2 IS NULL ";
        //処理対象科目
        if ($model->field["SUBCLASS_REMARK0"] == "on" || $model->field["SUBCLASS_REMARK1"] == "on" || $model->field["SUBCLASS_REMARK2"] == "on" || $model->field["SUBCLASS_REMARK3"] == "on") {
            $query .= " AND (" . knjd214vQuery::getHankiSql($model) . ") ";
        }

        return $query;
    }

    //評価の算出方法(1:四捨五入 2:切り上げ 3:切り捨て)
    public function getCalcMethod()
    {
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

    //事前設定チェック1
    public function getCntAssessAvgMst($model)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     ASSESS_AVG_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        return $query;
    }

    //事前設定チェック2
    public function getCntAssessAvgCalcMst($model)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     ASSESS_AVG_CALC_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND GRADE = '{$model->field["GRADE"]}' ";
        $query .= "     AND SEMESTER || TESTKINDCD || TESTITEMCD || SCORE_DIV = '{$model->field["TESTKINDCD"]}' ";
        return $query;
    }

    //処理対象レコード（コピー）
    public function getCopySql($model, $year)
    {
        //対象者(左のリスト）
        $array  = (strlen($model->selectdata) ? explode(",", $model->selectdata) : array());
        $gr_cl  = implode("','", $array);
        if ($model->Properties["useAssessAvgMst"] == "1") {
            // 相対評定マスタの設定がない場合に評定マスタを使用しない
            $assess = ($model->field["CONVERT"] == "on") ? "R1.ASSESSLEVEL" : "T3.SCORE"; //評定換算する／しない
            $isRelaCnt = "CASE WHEN 0 < R1.ASSESSLEVEL THEN 1 ELSE 0 END"; //相対評定マスタの設定があるか
        } elseif ($model->Properties["useAssessTestcountMst"] == "1") {
            // 相対評定マスタの設定がない場合に評定マスタを使用しない
            $assess = ($model->field["CONVERT"] == "on") ? "CASE WHEN R2.YEAR IS NOT NULL AND 0 < R2.ASSESSLEVEL THEN R2.ASSESSLEVEL ELSE R1.ASSESSLEVEL END" : "T3.SCORE"; //評定換算する／しない
            $isRelaCnt = "CASE WHEN R2.YEAR IS NOT NULL AND 0 < R2.ASSESSLEVEL THEN 1 ELSE 0 END"; //相対評定マスタの設定があるか
        } else {
            $assess = ($model->field["CONVERT"] == "on") ? "CASE WHEN R2.CNT IS NOT NULL AND 0 < R2.CNT THEN R1.ASSESSLEVEL ELSE L1.ASSESSLEVEL END" : "T3.SCORE"; //評定換算する／しない
            $isRelaCnt = "CASE WHEN R2.CNT IS NOT NULL AND 0 < R2.CNT THEN 1 ELSE 0 END"; //相対評定マスタの設定があるか
        }

        $query  = "";
        //相対評定マスタ
        $query .= "WITH OTHER_CNT AS ( ";
        if ($model->Properties["useAssessSubclassMst"] == "1") {
            $query .= "    SELECT ";
            $query .= "        '3' AS ASSESSCD, ";
            $query .= "        GRADE, ";
            $query .= "        COURSECD, ";
            $query .= "        MAJORCD, ";
            $query .= "        COURSECODE, ";
            $query .= "        CLASSCD, ";
            $query .= "        SCHOOL_KIND, ";
            $query .= "        CURRICULUM_CD, ";
            $query .= "        SUBCLASSCD, ";
            $query .= "        COUNT(*) AS CNT ";
            $query .= "    FROM ";
            $query .= "        ASSESS_SUBCLASS_MST ";
            $query .= "    WHERE ";
            $query .= "        YEAR = '{$year}' ";
            $query .= "    GROUP BY ";
            $query .= "        GRADE, ";
            $query .= "        COURSECD, ";
            $query .= "        MAJORCD, ";
            $query .= "        COURSECODE, ";
            $query .= "        CLASSCD, ";
            $query .= "        SCHOOL_KIND, ";
            $query .= "        CURRICULUM_CD, ";
            $query .= "        SUBCLASSCD ";
        } elseif ($model->Properties["useAssessCourseMst"] == "1") {
            $query .= "    SELECT ";
            $query .= "        ASSESSCD, ";
            $query .= "        COURSECD, ";
            $query .= "        MAJORCD, ";
            $query .= "        COURSECODE, ";
            $query .= "        COUNT(*) AS CNT ";
            $query .= "    FROM ";
            $query .= "        ASSESS_COURSE_MST ";
            $query .= "    GROUP BY ";
            $query .= "        ASSESSCD, ";
            $query .= "        COURSECD, ";
            $query .= "        MAJORCD, ";
            $query .= "        COURSECODE ";
        } else {
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
        }
        $query .= "    ) ";

        if ($model->Properties["useAssessAvgMst"] == "1") {
            $query .= " , RECORD_RANK_CHAIR AS ( ";
            $query .= "     SELECT ";
            $query .= "         CLASSCD, ";
            $query .= "         SCHOOL_KIND, ";
            $query .= "         CURRICULUM_CD, ";
            $query .= "         SUBCLASSCD, ";
            $query .= "         MAX(CHAIRCD) AS CHAIRCD, ";
            $query .= "         SCHREGNO ";
            $query .= "     FROM ";
            $query .= "         RECORD_RANK_CHAIR_SDIV_DAT ";
            $query .= "     WHERE ";
            $query .= "         YEAR = '{$year}' ";
            $query .= "         AND SEMESTER || TESTKINDCD || TESTITEMCD || SCORE_DIV = '{$model->field["TESTKINDCD"]}' ";
            $query .= "     GROUP BY ";
            $query .= "         CLASSCD, ";
            $query .= "         SCHOOL_KIND, ";
            $query .= "         CURRICULUM_CD, ";
            $query .= "         SUBCLASSCD, ";
            $query .= "         SCHREGNO ";
            $query .= " ) ";
            $query .= " , RECORD_SCORE_ASSESSAVG AS ( ";
            $query .= "     SELECT ";
            $query .= "         T3.CLASSCD, ";
            $query .= "         T3.SCHOOL_KIND, ";
            $query .= "         T3.CURRICULUM_CD, ";
            $query .= "         T3.SUBCLASSCD, ";
            $query .= "         T3.SCHREGNO, ";
            $query .= "         T3.VALUE_DI, ";
            $query .= "         T3.SCORE, ";
            $query .= "         CASE WHEN AC.ASSESS_TBL_DIV = '2' THEN T3.SCORE ";
            $query .= "              WHEN AC.ASSESS_TBL_DIV = '1' AND AC.CALC_UNIT_DIV = '1' THEN ";
            $query .= "                   CASE WHEN SMALLINT(ROUND(RA1.AVG,0)) >= 80 THEN T3.SCORE ";
            $query .= "                        WHEN SMALLINT(ROUND(RA1.AVG,0)) <= 40 THEN T3.SCORE + (40 - SMALLINT(ROUND(RA1.AVG,0))) ";
            $query .= "                        ELSE T3.SCORE ";
            $query .= "                   END ";
            $query .= "              WHEN AC.ASSESS_TBL_DIV = '1' AND AC.CALC_UNIT_DIV = '2' THEN ";
            $query .= "                   CASE WHEN SMALLINT(ROUND(RA3.AVG,0)) >= 80 THEN T3.SCORE ";
            $query .= "                        WHEN SMALLINT(ROUND(RA3.AVG,0)) <= 40 THEN T3.SCORE + (40 - SMALLINT(ROUND(RA3.AVG,0))) ";
            $query .= "                        ELSE T3.SCORE ";
            $query .= "                   END ";
            $query .= "              ELSE T3.SCORE ";
            $query .= "         END AS ASSESSSCORE, ";
            $query .= "         CASE WHEN AC.ASSESS_TBL_DIV = '2' THEN 99 ";
            $query .= "              WHEN AC.ASSESS_TBL_DIV = '1' AND AC.CALC_UNIT_DIV = '1' THEN ";
            $query .= "                   CASE WHEN SMALLINT(ROUND(RA1.AVG,0)) >= 80 THEN 79 ";
            $query .= "                        WHEN SMALLINT(ROUND(RA1.AVG,0)) <= 40 THEN 40 ";
            $query .= "                        ELSE SMALLINT(ROUND(RA1.AVG,0)) ";
            $query .= "                   END ";
            $query .= "              WHEN AC.ASSESS_TBL_DIV = '1' AND AC.CALC_UNIT_DIV = '2' THEN ";
            $query .= "                   CASE WHEN SMALLINT(ROUND(RA3.AVG,0)) >= 80 THEN 79 ";
            $query .= "                        WHEN SMALLINT(ROUND(RA3.AVG,0)) <= 40 THEN 40 ";
            $query .= "                        ELSE SMALLINT(ROUND(RA3.AVG,0)) ";
            $query .= "                   END ";
            $query .= "              ELSE NULL ";
            $query .= "         END AS ASSESSAVG ";
            $query .= "     FROM ";
            $query .= "         SCHREG_REGD_DAT T1 ";
            $query .= "         INNER JOIN RECORD_SCORE_DAT T3 ";
            $query .= "              ON T3.YEAR = T1.YEAR ";
            $query .= "             AND T3.SEMESTER || T3.TESTKINDCD || T3.TESTITEMCD || T3.SCORE_DIV = '{$model->field["TESTKINDCD"]}' ";
            $query .= "             AND T3.SCHREGNO = T1.SCHREGNO ";
            $query .= "         LEFT JOIN ASSESS_AVG_CALC_MST AC ";
            $query .= "              ON AC.YEAR = T3.YEAR ";
            $query .= "             AND AC.SEMESTER || AC.TESTKINDCD || AC.TESTITEMCD || AC.SCORE_DIV = '{$model->field["TESTKINDCD"]}' ";
            $query .= "             AND AC.GRADE = T1.GRADE ";
            $query .= "             AND AC.CLASSCD = T3.CLASSCD ";
            $query .= "             AND AC.SCHOOL_KIND = T3.SCHOOL_KIND ";
            $query .= "             AND AC.CURRICULUM_CD = T3.CURRICULUM_CD ";
            $query .= "             AND AC.SUBCLASSCD = T3.SUBCLASSCD ";
            $query .= "         LEFT JOIN RECORD_RANK_CHAIR RR1 ";
            $query .= "              ON RR1.SCHREGNO = T3.SCHREGNO ";
            $query .= "             AND RR1.CLASSCD = T3.CLASSCD ";
            $query .= "             AND RR1.SCHOOL_KIND = T3.SCHOOL_KIND ";
            $query .= "             AND RR1.CURRICULUM_CD = T3.CURRICULUM_CD ";
            $query .= "             AND RR1.SUBCLASSCD = T3.SUBCLASSCD ";
            $query .= "         LEFT JOIN RECORD_AVERAGE_CHAIR_SDIV_DAT RA1 ";
            $query .= "              ON RA1.YEAR = T3.YEAR ";
            $query .= "             AND RA1.SEMESTER || RA1.TESTKINDCD || RA1.TESTITEMCD || RA1.SCORE_DIV = '{$model->field["TESTKINDCD"]}' ";
            $query .= "             AND RA1.CLASSCD = T3.CLASSCD ";
            $query .= "             AND RA1.SCHOOL_KIND = T3.SCHOOL_KIND ";
            $query .= "             AND RA1.CURRICULUM_CD = T3.CURRICULUM_CD ";
            $query .= "             AND RA1.SUBCLASSCD = T3.SUBCLASSCD ";
            $query .= "             AND RA1.CHAIRCD = RR1.CHAIRCD ";
            $query .= "             AND RA1.AVG_DIV = '1' ";
            $query .= "             AND RA1.GRADE = T1.GRADE ";
            $query .= "             AND RA1.HR_CLASS = '000' ";
            $query .= "             AND RA1.COURSECD = '0' ";
            $query .= "             AND RA1.MAJORCD = '000' ";
            $query .= "             AND RA1.COURSECODE = '0000' ";
            $query .= "         LEFT JOIN RECORD_AVERAGE_SDIV_DAT RA3 ";
            $query .= "              ON RA3.YEAR = T3.YEAR ";
            $query .= "             AND RA3.SEMESTER || RA3.TESTKINDCD || RA3.TESTITEMCD || RA3.SCORE_DIV = '{$model->field["TESTKINDCD"]}' ";
            $query .= "             AND RA3.CLASSCD = T3.CLASSCD ";
            $query .= "             AND RA3.SCHOOL_KIND = T3.SCHOOL_KIND ";
            $query .= "             AND RA3.CURRICULUM_CD = T3.CURRICULUM_CD ";
            $query .= "             AND RA3.SUBCLASSCD = T3.SUBCLASSCD ";
            $query .= "             AND RA3.AVG_DIV = '3' ";
            $query .= "             AND RA3.GRADE = T1.GRADE ";
            $query .= "             AND RA3.HR_CLASS = '000' ";
            $query .= "             AND RA3.COURSECD = T1.COURSECD ";
            $query .= "             AND RA3.MAJORCD = T1.MAJORCD ";
            $query .= "             AND RA3.COURSECODE = T1.COURSECODE ";
            $query .= "     WHERE ";
            $query .= "         T1.YEAR = '{$year}' ";
            $query .= "         AND T1.SEMESTER = '{$model->field["SEMESTER"]}' ";
            $query .= "         AND T1.GRADE || T1.HR_CLASS IN ('".$gr_cl."') ";
            $query .= " ) ";
        } elseif ($model->Properties["useAssessTestcountMst"] == "1") {
            $query .= "  , T_TESTCOUNT AS ( ";
            $query .= "     SELECT DISTINCT ";
            $query .= "         T1.YEAR, T1.SEMESTER, T1.TESTKINDCD, T1.TESTITEMCD, T1.SCORE_DIV ";
            $query .= "     FROM ";
            $query .= "         ADMIN_CONTROL_SDIV_DAT T1 ";
            $query .= "         INNER JOIN TESTITEM_MST_COUNTFLG_NEW_SDIV L1 ON T1.YEAR = L1.YEAR ";
            $query .= "                                                AND T1.SEMESTER = L1.SEMESTER ";
            $query .= "                                                AND T1.TESTKINDCD = L1.TESTKINDCD ";
            $query .= "                                                AND T1.TESTITEMCD = L1.TESTITEMCD ";
            $query .= "                                                AND T1.SCORE_DIV = L1.SCORE_DIV ";
            $query .= " WHERE ";
            $query .= "         T1.YEAR         = '".CTRL_YEAR."' ";
            $query .= "     AND T1.SEMESTER     <> '9' ";
            $query .= "     AND T1.TESTITEMCD   = '01' ";
            $query .= "     AND T1.SCORE_DIV    = '08' ";
            $query .= "     AND T1.SEMESTER || T1.TESTKINDCD || T1.TESTITEMCD || T1.SCORE_DIV <= '".$model->field["TESTKINDCD"]."' ";
            $query .= "  ) ";
            $query .= "  , T_SCHREG_TESTCOUNT_SCORE AS ( ";
            $query .= "     SELECT ";
            $query .= "         T1.YEAR ";
            $query .= "       , T1.CLASSCD, T1.SCHOOL_KIND, T1.CURRICULUM_CD, T1.SUBCLASSCD ";
            $query .= "       , T1.SCHREGNO ";
            $query .= "       , SUM(T1.SCORE) AS SCORE ";
            $query .= "       , COUNT(T1.SCORE) AS TESTCOUNT ";
            $query .= "       , CAST(NULL AS VARCHAR(1)) AS VALUE_DI ";
            $query .= "     FROM ";
            $query .= "         RECORD_SCORE_DAT T1 ";
            $query .= "         INNER JOIN T_TESTCOUNT T2 ON T2.YEAR = T1.YEAR ";
            $query .= "             AND T2.SEMESTER   = T1.SEMESTER   ";
            $query .= "             AND T2.TESTKINDCD = T1.TESTKINDCD ";
            $query .= "             AND T2.TESTITEMCD = T1.TESTITEMCD ";
            $query .= "             AND T2.SCORE_DIV  = T1.SCORE_DIV  ";
            $query .= "     GROUP BY ";
            $query .= "         T1.YEAR ";
            $query .= "       , T1.CLASSCD, T1.SCHOOL_KIND, T1.CURRICULUM_CD, T1.SUBCLASSCD ";
            $query .= "       , T1.SCHREGNO ";
            $query .= "  ) ";
        }

        if ($model->useD116) {
            // RECORD_SCORE_DATのレコードがないもしくは成績なしで、かつ名称マスタ「D116」に登録された合併元科目の講座名簿
            // → 合併先科目に単位を加算する
            $query .= " , CHAIR_STD_D116 AS ( ";
            $query .= " SELECT DISTINCT ";
            $query .= "     CHR.CLASSCD, ";
            $query .= "     CHR.SCHOOL_KIND, ";
            $query .= "     CHR.CURRICULUM_CD, ";
            $query .= "     CHR.SUBCLASSCD, ";
            $query .= "     CSTD.SCHREGNO, ";
            $query .= "     T1.GRADE, ";
            $query .= "     T1.COURSECD || T1.MAJORCD || T1.COURSECODE AS COURSE, ";
            $query .= "     D116.NAMECD1 AS D116_FLG, ";
            $query .= "     T4.CREDITS ";
            $query .= " FROM ";
            $query .= "     SCHREG_REGD_DAT T1 ";
            $query .= "     INNER JOIN CHAIR_STD_DAT CSTD ";
            $query .= "         ON CSTD.YEAR = T1.YEAR ";
            $query .= "        AND CSTD.SEMESTER = T1.SEMESTER ";
            $query .= "        AND CSTD.SCHREGNO = T1.SCHREGNO ";
            $query .= "     INNER JOIN CHAIR_DAT CHR ";
            $query .= "         ON CHR.YEAR = CSTD.YEAR ";
            $query .= "        AND CHR.SEMESTER = CSTD.SEMESTER ";
            $query .= "        AND CHR.CHAIRCD = CSTD.CHAIRCD ";
            $query .= "     INNER JOIN V_NAME_MST D116 ON D116.YEAR = T1.YEAR ";
            $query .= "                              AND D116.NAMECD1 = 'D116' ";
            $query .= "                              AND D116.NAME1 = CHR.CLASSCD || '-' || CHR.SCHOOL_KIND || '-' || CHR.CURRICULUM_CD || '-' || CHR.SUBCLASSCD ";
            $query .= "     INNER JOIN SCHREG_BASE_MST T2 ON T2.SCHREGNO = T1.SCHREGNO ";
            $query .= "                                  AND (T2.GRD_DATE IS NULL OR ";
            $query .= "                                       T2.GRD_DIV IS NULL OR ";
            $query .= "                                       T2.GRD_DIV = '4') ";
            $query .= "     LEFT JOIN RECORD_SCORE_DAT T3 ON T3.SCHREGNO = T1.SCHREGNO ";
            $query .= "                             AND T3.YEAR = T1.YEAR ";
            $query .= "                             AND T3.SEMESTER || T3.TESTKINDCD || T3.TESTITEMCD || T3.SCORE_DIV = '{$model->field["TESTKINDCD"]}' ";
            $query .= "                             AND T3.CLASSCD = CHR.CLASSCD ";
            $query .= "                             AND T3.SCHOOL_KIND = CHR.SCHOOL_KIND ";
            $query .= "                             AND T3.CURRICULUM_CD = CHR.CURRICULUM_CD ";
            $query .= "                             AND T3.SUBCLASSCD = CHR.SUBCLASSCD ";
            $query .= "     LEFT JOIN CREDIT_MST T4 ON T4.YEAR = T1.YEAR ";
            $query .= "                            AND T4.COURSECD = T1.COURSECD ";
            $query .= "                            AND T4.MAJORCD = T1.MAJORCD ";
            $query .= "                            AND T4.GRADE = T1.GRADE ";
            $query .= "                            AND T4.COURSECODE = T1.COURSECODE ";
            $query .= "                            AND T4.CLASSCD = CHR.CLASSCD ";
            $query .= "                            AND T4.SCHOOL_KIND = CHR.SCHOOL_KIND ";
            $query .= "                            AND T4.CURRICULUM_CD = CHR.CURRICULUM_CD ";
            $query .= "                            AND T4.SUBCLASSCD = CHR.SUBCLASSCD ";
            $query .= " WHERE ";
            $query .= "     T1.YEAR = '{$year}' AND ";
            $query .= "     T1.SEMESTER = '{$model->field["SEMESTER"]}' AND ";
            $query .= "     T1.GRADE || T1.HR_CLASS IN ('".$gr_cl."') ";
            $query .= "     AND T3.SCORE IS NULL ";
            $query .= "     AND D116.NAMECD2 IS NOT NULL ";
            $query .= "     AND (CHR.CLASSCD, CHR.SCHOOL_KIND, CHR.CURRICULUM_CD, CHR.SUBCLASSCD) IN (SELECT ";
            $query .= "          T1.ATTEND_CLASSCD, T1.ATTEND_SCHOOL_KIND, T1.ATTEND_CURRICULUM_CD, T1.ATTEND_SUBCLASSCD ";
            $query .= "          FROM ";
            $query .= "              SUBCLASS_REPLACE_COMBINED_DAT T1 ";
            $query .= "          WHERE ";
            $query .= "              YEAR = '{$year}' ";
            $query .= "         ) ";
            $query .= " ) ";
        }

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
        $query .= "     D065.SUBCD AS D065_SUBCD, ";
        $query .= "     T3.SCORE   AS D065_SUBCD_SCORE, ";
        $query .= "     D116.NAMECD1 AS D116_FLG, ";
        $query .= "     T4.CREDITS, ";
        $query .= "     ".$isRelaCnt." AS IS_OTHER_CNT, ";
        $query .= "     ".$assess." AS SCORE, ";
        $query .= "     case when D116.NAME1 is not null then T4.CREDITS ";
        $query .= "          when 1 <= ".$assess." then T4.CREDITS ";
        $query .= "          else NULL end AS COMP_CREDIT, ";
        $query .= "     case when D116.NAME1 is not null then T4.CREDITS ";
        $query .= "          when 1 < ".$assess." then T4.CREDITS ";
        $query .= "          when 1 = ".$assess." then 0 ";
        $query .= "          else NULL end AS GET_CREDIT ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1 ";
        $query .= "     LEFT JOIN SCHREG_REGD_GDAT GDAT ON GDAT.YEAR = T1.YEAR ";
        $query .= "                                    AND GDAT.GRADE = T1.GRADE ";
        $query .= "     INNER JOIN SCHREG_BASE_MST T2 ON T2.SCHREGNO = T1.SCHREGNO ";
        $query .= "                                  AND (T2.GRD_DATE IS NULL OR ";
        $query .= "                                       T2.GRD_DIV IS NULL OR ";
        $query .= "                                       T2.GRD_DIV = '4') ";
        if ($model->Properties["useAssessAvgMst"] == "1") {
            $query .= "     INNER JOIN RECORD_SCORE_ASSESSAVG T3 ON T3.SCHREGNO = T1.SCHREGNO ";
        } elseif ($model->Properties["useAssessTestcountMst"] == "1") {
            $query .= "     INNER JOIN T_SCHREG_TESTCOUNT_SCORE T3 ON T3.SCHREGNO = T1.SCHREGNO ";
            $query .= "                             AND T3.YEAR = '{$year}' ";
        } else {
            $query .= "     INNER JOIN RECORD_SCORE_DAT T3 ON T3.SCHREGNO = T1.SCHREGNO ";
            $query .= "                             AND T3.YEAR = '{$year}' ";
            $query .= "                             AND T3.SEMESTER || T3.TESTKINDCD || T3.TESTITEMCD || T3.SCORE_DIV = '{$model->field["TESTKINDCD"]}' ";
        }
        //D065にある科目は評定換算しない
        $query .= "     LEFT JOIN ( ";
        $query .= "         SELECT ";
        $query .= "             NAME1 AS SUBCD ";
        $query .= "         FROM ";
        $query .= "             V_NAME_MST ";
        $query .= "         WHERE ";
        $query .= "             YEAR = '{$year}' ";
        $query .= "             AND NAMECD1 = 'D065' ";
        $query .= "         GROUP BY ";
        $query .= "             NAME1 ";
        $query .= "     ) D065 ON D065.SUBCD = T3.CLASSCD || '-' || T3.SCHOOL_KIND || '-' || T3.CURRICULUM_CD || '-' || T3.SUBCLASSCD ";
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
        if ($model->Properties["useAssessAvgMst"] == "1") {
            $query .= "     LEFT JOIN ASSESS_AVG_MST R1 ON R1.YEAR = '{$year}' ";
            $query .= "                            AND R1.ASSESSAVG = T3.ASSESSAVG ";
            $query .= "                            AND T3.ASSESSAVG BETWEEN R1.ASSESSLOW AND R1.ASSESSHIGH ";
        } elseif ($model->Properties["useAssessTestcountMst"] == "1") {
            $query .= "     LEFT JOIN ASSESS_TESTCOUNT_MST R1 ON R1.YEAR = '{$year}' ";
            $query .= "                            AND R1.ASSESSCD = '5' ";
            $query .= "                            AND R1.TESTCOUNT = T3.TESTCOUNT ";
            $query .= "                            AND T3.SCORE BETWEEN R1.ASSESSLOW AND R1.ASSESSHIGH ";
            $query .= "                            AND R1.CLASSCD = '00' ";
            $query .= "                            AND R1.SCHOOL_KIND = GDAT.SCHOOL_KIND ";
            $query .= "                            AND R1.CURRICULUM_CD = '00' ";
            $query .= "                            AND R1.SUBCLASSCD = '000000' ";
        } elseif ($model->Properties["useAssessSubclassMst"] == "1") {
            $query .= "     LEFT JOIN ASSESS_SUBCLASS_MST R1 ON R1.YEAR = '{$year}' ";
            $query .= "                            AND T3.SCORE BETWEEN R1.ASSESSLOW AND R1.ASSESSHIGH ";
            $query .= "                            AND R1.GRADE = T1.GRADE ";
            $query .= "                            AND R1.COURSECD = T1.COURSECD ";
            $query .= "                            AND R1.MAJORCD = T1.MAJORCD ";
            $query .= "                            AND R1.COURSECODE = T1.COURSECODE ";
            $query .= "                            AND R1.CLASSCD = T3.CLASSCD ";
            $query .= "                            AND R1.SCHOOL_KIND = T3.SCHOOL_KIND ";
            $query .= "                            AND R1.CURRICULUM_CD = T3.CURRICULUM_CD ";
            $query .= "                            AND R1.SUBCLASSCD = T3.SUBCLASSCD ";
        } elseif ($model->Properties["useAssessCourseMst"] == "1") {
            $query .= "     LEFT JOIN ASSESS_COURSE_MST R1 ON R1.ASSESSCD = '3' ";
            $query .= "                            AND T3.SCORE BETWEEN R1.ASSESSLOW AND R1.ASSESSHIGH ";
            $query .= "                            AND R1.COURSECD = T1.COURSECD ";
            $query .= "                            AND R1.MAJORCD = T1.MAJORCD ";
            $query .= "                            AND R1.COURSECODE = T1.COURSECODE ";
        } else {
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
        }
        if ($model->Properties["useAssessAvgMst"] == "1") {
        } elseif ($model->Properties["useAssessTestcountMst"] == "1") {
            $query .= "     LEFT JOIN ASSESS_TESTCOUNT_MST R2 ON R2.YEAR = '{$year}' ";
            $query .= "                            AND R2.ASSESSCD = '5' ";
            $query .= "                            AND R2.TESTCOUNT = T3.TESTCOUNT ";
            $query .= "                            AND T3.SCORE BETWEEN R2.ASSESSLOW AND R2.ASSESSHIGH ";
            $query .= "                            AND R2.CLASSCD = T3.CLASSCD ";
            $query .= "                            AND R2.SCHOOL_KIND = GDAT.SCHOOL_KIND ";
            $query .= "                            AND R2.CURRICULUM_CD = T3.CURRICULUM_CD ";
            $query .= "                            AND R2.SUBCLASSCD = T3.SUBCLASSCD ";
        } elseif ($model->Properties["useAssessSubclassMst"] == "1") {
            $query .= "     LEFT JOIN OTHER_CNT R2 ON R2.ASSESSCD = '3' ";
            $query .= "                            AND R2.GRADE = T1.GRADE ";
            $query .= "                            AND R2.COURSECD = T1.COURSECD ";
            $query .= "                            AND R2.MAJORCD = T1.MAJORCD ";
            $query .= "                            AND R2.COURSECODE = T1.COURSECODE ";
            $query .= "                            AND R2.CLASSCD = T3.CLASSCD ";
            $query .= "                            AND R2.SCHOOL_KIND = T3.SCHOOL_KIND ";
            $query .= "                            AND R2.CURRICULUM_CD = T3.CURRICULUM_CD ";
            $query .= "                            AND R2.SUBCLASSCD = T3.SUBCLASSCD ";
        } elseif ($model->Properties["useAssessCourseMst"] == "1") {
            $query .= "     LEFT JOIN OTHER_CNT R2 ON R2.ASSESSCD = '3' ";
            $query .= "                            AND R2.COURSECD = T1.COURSECD ";
            $query .= "                            AND R2.MAJORCD = T1.MAJORCD ";
            $query .= "                            AND R2.COURSECODE = T1.COURSECODE ";
        } else {
            $query .= "     LEFT JOIN OTHER_CNT R2 ON R2.ASSESSCD = '3' ";
            $query .= "                            AND R2.GRADE = T1.GRADE ";
            //教育課程対応
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= "                        AND R2.CLASSCD = T3.CLASSCD ";
                $query .= "                        AND R2.SCHOOL_KIND = T3.SCHOOL_KIND ";
                $query .= "                        AND R2.CURRICULUM_CD = T3.CURRICULUM_CD ";
            }
            $query .= "                            AND R2.SUBCLASSCD = T3.SUBCLASSCD ";
        }
        $query .= "     LEFT JOIN V_NAME_MST D008 ON D008.YEAR = '{$year}' ";
        $query .= "                              AND D008.NAMECD1 = CASE WHEN EXISTS (SELECT 'X' FROM V_NAME_MST WHERE YEAR = '{$year}' AND NAMECD1 = 'D' || T3.SCHOOL_KIND || '08') THEN 'D' || T3.SCHOOL_KIND || '08' ";
        $query .= "                                                      ELSE 'D008' ";
        $query .= "                                                 END ";
        $query .= "                              AND D008.NAMECD2 = substr(T3.SUBCLASSCD,1,2) ";
        $query .= "     LEFT JOIN V_NAME_MST D116 ON D116.YEAR = '{$year}' ";
        $query .= "                              AND D116.NAMECD1 = 'D116' ";
        $query .= "                              AND D116.NAME1 = T3.CLASSCD || '-' || T3.SCHOOL_KIND || '-' || T3.CURRICULUM_CD || '-' || T3.SUBCLASSCD ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '{$year}' AND ";
        $query .= "     T1.SEMESTER = '{$model->field["SEMESTER"]}' AND ";
        $query .= "     T1.GRADE || T1.HR_CLASS IN ('".$gr_cl."') ";
        $query .= "     AND D008.NAMECD2 IS NULL ";
        //処理対象科目
        if ($model->field["SUBCLASS_REMARK0"] == "on" || $model->field["SUBCLASS_REMARK1"] == "on" || $model->field["SUBCLASS_REMARK2"] == "on" || $model->field["SUBCLASS_REMARK3"] == "on") {
            $query .= " AND (" . knjd214vQuery::getHankiSql($model) . ") ";
        }
        if ($model->Properties["knjd214vCopyClassCd90"] == "1") {
            //総学等は処理対象
        } else {
            //総学等は処理対象外
            $query .= "                             AND T3.SUBCLASSCD not like '90%' ";
        }
        if ($model->useD116) {
            $query .= " UNION ";
            $query .= " SELECT ";
            $query .= "     T1.CLASSCD, ";
            $query .= "     T1.SCHOOL_KIND, ";
            $query .= "     T1.CURRICULUM_CD, ";
            $query .= "     T1.SUBCLASSCD, ";
            $query .= "     T1.SCHREGNO, ";
            $query .= "     T1.GRADE, ";
            $query .= "     T1.COURSE, ";
            $query .= "     CAST(NULL AS VARCHAR(1)) AS VALUE_DI, ";
            $query .= "     CAST(NULL AS VARCHAR(1)) AS D065_SUBCD, ";
            $query .= "     CAST(NULL AS VARCHAR(1)) AS D065_SUBCD_SCORE, ";
            $query .= "     T1.D116_FLG, ";
            $query .= "     T1.CREDITS, ";
            $query .= "     0 AS IS_OTHER_CNT, ";
            $query .= "     CAST(NULL AS SMALLINT) AS SCORE, ";
            $query .= "     T1.CREDITS AS COMP_CREDIT, ";
            $query .= "     T1.CREDITS AS GET_CREDIT ";
            $query .= " FROM ";
            $query .= "     CHAIR_STD_D116 T1 ";
        }

        return $query;
    }

    //処理対象レコード（合併先科目の単位自動計算）
    public function getCombinedCopySql($model, $year)
    {
        //対象者(左のリスト）
        $array  = (strlen($model->selectdata) ? explode(",", $model->selectdata) : array());
        $gr_cl  = implode("','", $array);

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
        $query .= "            SUM(case when D116.NAME1 is not null then T2.GET_CREDIT ";
        $query .= "                     when 1 < T2.SCORE then T2.GET_CREDIT ";
        $query .= "                     when 1 = T2.SCORE then T2.COMP_CREDIT end) AS GET_CREDIT_Y ";
        $query .= "     FROM   SUBCLASS_REPLACE_COMBINED_DAT T1 ";
        $query .= "            INNER JOIN RECORD_SCORE_DAT T2 ON T2.YEAR = T1.YEAR ";
        $query .= "               AND  T2.SEMESTER || T2.TESTKINDCD || T2.TESTITEMCD || T2.SCORE_DIV = '{$model->field["COPY_SAKI_CD"]}' ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "           AND  T2.CLASSCD = T1.ATTEND_CLASSCD ";
            $query .= "           AND  T2.SCHOOL_KIND = T1.ATTEND_SCHOOL_KIND ";
            $query .= "           AND  T2.CURRICULUM_CD = T1.ATTEND_CURRICULUM_CD ";
        }
        $query .= "               AND  T2.SUBCLASSCD = T1.ATTEND_SUBCLASSCD ";
        $query .= "     LEFT JOIN V_NAME_MST D116 ON D116.YEAR = '{$year}' ";
        $query .= "                              AND D116.NAMECD1 = 'D116' ";
        $query .= "                              AND D116.NAME1 = T2.CLASSCD || '-' || T2.SCHOOL_KIND || '-' || T2.CURRICULUM_CD || '-' || T2.SUBCLASSCD ";
        $query .= "     WHERE  T1.YEAR = '{$year}' ";
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
        if ($model->Properties["knjd214vCopyClassCd90"] == "1") {
            //総学等は処理対象
        } else {
            //総学等は処理対象外
            $query .= "                             AND T3.SUBCLASSCD not like '90%' ";
        }
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
        $query .= "     T1.GRADE || T1.HR_CLASS IN ('".$gr_cl."') ";
        //処理対象科目
        if ($model->field["SUBCLASS_REMARK0"] == "on" || $model->field["SUBCLASS_REMARK1"] == "on" || $model->field["SUBCLASS_REMARK2"] == "on" || $model->field["SUBCLASS_REMARK3"] == "on") {
            $query .= " AND (" . knjd214vQuery::getHankiSql($model) . ") ";
        }

        return $query;
    }

    //処理対象レコード（仮評定）
    public function getProvCopySql($model, $year)
    {
        //対象者(左のリスト）
        $array  = (strlen($model->selectdata) ? explode(",", $model->selectdata) : array());
        $gr_cl  = implode("','", $array);

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
        if ($model->Properties["knjd214vCopyClassCd90"] == "1") {
            //総学等は処理対象
        } else {
            //総学等は処理対象外
            $query .= "                             AND T3.SUBCLASSCD not like '90%' ";
        }
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '{$year}' AND ";
        $query .= "     T1.SEMESTER = '{$model->field["SEMESTER"]}' AND ";
        $query .= "     T1.GRADE || T1.HR_CLASS IN ('".$gr_cl."') ";
        //処理対象科目
        if ($model->field["SUBCLASS_REMARK0"] == "on" || $model->field["SUBCLASS_REMARK1"] == "on" || $model->field["SUBCLASS_REMARK2"] == "on" || $model->field["SUBCLASS_REMARK3"] == "on") {
            $query .= " AND (" . knjd214vQuery::getHankiSql($model) . ") ";
        }
        return $query;
    }

    //コピー処理
    public function getCopyQuery($model)
    {
        //変数
        $code   = $model->field["COPY_SAKI_CD"];
        $sem    = substr($code, 0, 1);
        $kind   = substr($code, 1, 2);
        $item   = substr($code, 3, 2);
        $div    = substr($code, 5, 2);

        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //欠課数上限値が法定時数(1)か実時数(2)か
        $schoolKind = "";
        if ($db->getOne(knjd214vQuery::hasVSchoolMstSchoolKind()) > 0) {
            $schoolKind = $db->getOne(knjd214vQuery::getSchoolKind($model));
        }
        $query = knjd214vQuery::getVSchoolMst(CTRL_YEAR, $schoolKind);
        $schoolRow = $db->getRow($query, DB_FETCHMODE_ASSOC);

        $model->useD116 = 0 < $db->getRow(knjd214vQuery::getD116Count(CTRL_YEAR));
        $query  = knjd214vQuery::getCopySql($model, CTRL_YEAR);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            //D065にある科目は評定換算しない
            if (strlen($row["D065_SUBCD"])) {
                $row["SCORE"]       = $row["D065_SUBCD_SCORE"];
                $row["COMP_CREDIT"] = strlen($row["D065_SUBCD_SCORE"]) ? $row["CREDITS"] : "";
                $row["GET_CREDIT"]  = strlen($row["D065_SUBCD_SCORE"]) ? $row["CREDITS"] : "";
            }

            //追指導データ参照（評定１、欠席の場合のみ）
            if ($model->field["SIDOU"] == "on" && ($row["SCORE"] == "1" || $row["VALUE_DI"] == "*")) {
                //コピー元になる評価・・・追指導入力が記号(1)か点数(2)か
                $query = knjd214vQuery::getSidouInputInf($model, CTRL_YEAR);
                $sidouRow = $db->getRow($query, DB_FETCHMODE_ASSOC);
                if ($sidouRow["SIDOU_INPUT"] == "1") {
                    //記号(1)
                    if ($sidouRow["SIDOU_INPUT_INF"] == "1") {
                        $query = knjd214vQuery::getRecordSlumpMark($model, CTRL_YEAR, $row);
                        $slumpRow = $db->getRow($query, DB_FETCHMODE_ASSOC);
                        //追指導（記号）が○の場合、評定２をセット
                        if (0 < $slumpRow["CNT_MARU"] && 0 == $slumpRow["CNT_MARUIGAI"]) {
                            $row["SCORE"]       = "2";
                            $row["COMP_CREDIT"] = $row["CREDITS"];
                            $row["GET_CREDIT"]  = $row["CREDITS"];
                        }
                    }
                    //点数(2)
                    if ($sidouRow["SIDOU_INPUT_INF"] == "2") {
                        $query = knjd214vQuery::getRecordSlumpScore($model, CTRL_YEAR, $row);
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
            if ($model->field["KEEKA_OVER"] == "on" && $model->field["KARI_DIV"] == "2" && (strlen($row["SCORE"]) || strlen($row["D116_FLG"]))) {
                //欠課数上限値
                $query = knjd214vQuery::getKeekaOver($model, CTRL_YEAR, $row, $schoolRow);
                $overRow = $db->getRow($query, DB_FETCHMODE_ASSOC);
                if (!strlen($overRow["COMP_ABSENCE_HIGH"])) {
                    $overRow["COMP_ABSENCE_HIGH"] = 9999;
                }
                //欠課数
                $query = knjd214vQuery::getKeeka($model, CTRL_YEAR, $row, $schoolRow);
                $keekaRow = $db->getRow($query, DB_FETCHMODE_ASSOC);
                if (!strlen($keekaRow["NOTICE_LATE"])) {
                    $keekaRow["NOTICE_LATE"] = 0;
                }

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
            $query = knjd214vQuery::getInsertRecordDat(CTRL_YEAR, $sem, $kind, $item, $div, $subCd, $schno, $gradValue, $compCredit, $getCredit, $classCd, $schoolKind, $curriculumCd, $model);
            $db->query($query);

            //評定フラグが仮評定の場合
            if ($model->field["KARI_DIV"] == '1') {
                //RECORD_PROV_FLG_DATのレコードを削除
                $query = knjd214vQuery::delRecordProvFlgDat(CTRL_YEAR, $subCd, $schno, $classCd, $schoolKind, $curriculumCd, $model);
                $db->query($query);
                //RECORD_PROV_FLG_DATのレコードを追加
                $query = knjd214vQuery::insRecordProvFlgDat(CTRL_YEAR, $subCd, $schno, $classCd, $schoolKind, $curriculumCd, $model);
                $db->query($query);
            }
        }
        $result->free();

        $db->commit();
        Query::dbCheckIn($db);
    }

    //コピー処理（合併先科目の単位自動計算）
    public function getCombinedCopyQuery($model)
    {
        //変数
        $code   = $model->field["COPY_SAKI_CD"];
        $sem    = substr($code, 0, 1);
        $kind   = substr($code, 1, 2);
        $item   = substr($code, 3, 2);
        $div    = substr($code, 5, 2);

        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $query  = knjd214vQuery::getCombinedCopySql($model, CTRL_YEAR);
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
            $query = knjd214vQuery::getDeleteRecordDat(CTRL_YEAR, $sem, $kind, $item, $div, $subCd, $schno, $classCd, $schoolKind, $curriculumCd, $model);
            $db->query($query);
            //評価を学年評定にコピーする。単位もコピーする。
            $query = knjd214vQuery::getInsertRecordDat(CTRL_YEAR, $sem, $kind, $item, $div, $subCd, $schno, $gradValue, $compCredit, $getCredit, $classCd, $schoolKind, $curriculumCd, $model);
            $db->query($query);

            //再処理のため、RECORD_PROV_FLG_DATのレコードを一旦削除
            $query = knjd214vQuery::delRecordProvFlgDat(CTRL_YEAR, $subCd, $schno, $classCd, $schoolKind, $curriculumCd, $model);
            $db->query($query);
            //評定フラグが仮評定の場合
            if ($model->field["KARI_DIV"] == '1') {
                //RECORD_PROV_FLG_DATのレコードを追加
                $query = knjd214vQuery::insRecordProvFlgDat(CTRL_YEAR, $subCd, $schno, $classCd, $schoolKind, $curriculumCd, $model);
                $db->query($query);
            }
        }
        $result->free();

        $db->commit();
        Query::dbCheckIn($db);
    }

    //コピー処理（仮評定）
    public function getProvCopyQuery($model)
    {
        //変数
        $code   = $model->field["KARI_TESTCD"];
        $sem    = substr($code, 0, 1);
        $kind   = substr($code, 1, 2);
        $item   = substr($code, 3, 2);
        $div    = substr($code, 5, 2); //09:仮評定

        $db = Query::dbCheckOut();
        $db->autoCommit(false);
        $query  = knjd214vQuery::getProvCopySql($model, CTRL_YEAR);
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
            $query = knjd214vQuery::getDeleteRecordDat(CTRL_YEAR, $sem, $kind, $item, $div, $subCd, $schno, $classCd, $schoolKind, $curriculumCd, $model);
            $db->query($query);
            //仮評定にコピー。
            $query = knjd214vQuery::getInsertRecordDat(CTRL_YEAR, $sem, $kind, $item, $div, $subCd, $schno, $gradValue, $compCredit, $getCredit, $classCd, $schoolKind, $curriculumCd, $model);
            $db->query($query);
        }
        $result->free();
        $db->commit();
        Query::dbCheckIn($db);
    }

    //RECORD_SCORE_DATのレコードを削除。
    public function getDeleteRecordDat($year, $sem, $kind, $item, $div, $subCd, $schno, $classCd, $schoolKind, $curriculumCd, $model)
    {
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
    public function getInsertRecordDat($year, $sem, $kind, $item, $div, $subCd, $schno, $gradValue, $compCredit, $getCredit, $classCd, $schoolKind, $curriculumCd, $model)
    {
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
    public function delRecordProvFlgDat($year, $subCd, $schno, $classCd, $schoolKind, $curriculumCd, $model)
    {
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
    public function insRecordProvFlgDat($year, $subCd, $schno, $classCd, $schoolKind, $curriculumCd, $model)
    {
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
    public function getSidouInputInf($model, $year)
    {
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
    public function getRecordSlumpScore($model, $year, $row)
    {
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
        if (strlen($row["IS_OTHER_CNT"]) && 0 < $row["IS_OTHER_CNT"]) {
            if ($model->Properties["useAssessSubclassMst"] == "1") {
                $query .= "     INNER JOIN ASSESS_SUBCLASS_MST ASSESS ";
                $query .= "         ON  ASSESS.YEAR = '{$year}' ";
                $query .= "         AND ASSESS.ASSESSLEVEL = 2 ";
                $query .= "         AND ASSESS.ASSESSLOW <= SLUMP.SCORE ";
                $query .= "         AND ASSESS.GRADE = '{$row["GRADE"]}' ";
                $query .= "         AND ASSESS.COURSECD || ASSESS.MAJORCD || ASSESS.COURSECODE = '{$row["COURSE"]}' ";
                $query .= "         AND ASSESS.CLASSCD        = '{$row["CLASSCD"]}' ";
                $query .= "         AND ASSESS.SCHOOL_KIND    = '{$row["SCHOOL_KIND"]}' ";
                $query .= "         AND ASSESS.CURRICULUM_CD  = '{$row["CURRICULUM_CD"]}' ";
                $query .= "         AND ASSESS.SUBCLASSCD     = '{$row["SUBCLASSCD"]}' ";
            } elseif ($model->Properties["useAssessCourseMst"] == "1") {
                $query .= "     INNER JOIN ASSESS_COURSE_MST ASSESS ";
                $query .= "         ON  ASSESS.ASSESSCD = '3' ";
                $query .= "         AND ASSESS.ASSESSLEVEL = 2 ";
                $query .= "         AND ASSESS.ASSESSLOW <= SLUMP.SCORE ";
                $query .= "         AND ASSESS.COURSECD || ASSESS.MAJORCD || ASSESS.COURSECODE = '{$row["COURSE"]}' ";
            } else {
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
    //追指導（記号）が○の場合、評定２をセット
    public function getRecordSlumpMark($model, $year, $row)
    {
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
        $query .= "     AND SLUMP.MARK IS NOT NULL ";
        return $query;
    }

    //指定学年の校種
    public function getSchoolKind($model)
    {
        $query  = "";
        $query .= " SELECT ";
        $query .= "     SCHOOL_KIND ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_GDAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "  AND GRADE = '".$model->field["GRADE"]."' ";

        return $query;
    }

    //学校マスタに校種があるか
    public function hasVSchoolMstSchoolKind()
    {
        $query  = "";
        $query .= " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     SYSCAT.COLUMNS ";
        $query .= " WHERE ";
        $query .= "     TABNAME = 'V_SCHOOL_MST' ";
        $query .= "  AND COLNAME = 'SCHOOL_KIND' ";

        return $query;
    }

    //V学校マスタ
    public function getVSchoolMst($year, $schoolKind)
    {
        $query  = "";
        $query .= " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     V_SCHOOL_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$year}' ";
        if ($schoolKind) {
            $query .= "     AND SCHOOL_KIND = '{$schoolKind}' ";
        }

        return $query;
    }

    public function getD116Count($year)
    {
        $query  = "";
        $query .= " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$year}' ";
        $query .= "     AND NAMECD1 = 'D116' ";
        return $query;
    }

    //欠課数上限値
    public function getKeekaOver($model, $year, $row, $schoolRow)
    {
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
    public function getKeeka($model, $year, $row, $schoolRow)
    {
        $query  = "";
        //学期ごとに清算(1,3)
        if ($schoolRow["ABSENT_COV"] == "1" || $schoolRow["ABSENT_COV"] == "3") {
            $query = knjd214vQuery::getKeekaYear($model, CTRL_YEAR, $row, $schoolRow);
        //年間で清算(0,2,4,5)
        } else {
            $query = knjd214vQuery::getKeekaSemester($model, CTRL_YEAR, $row, $schoolRow);
        }
        return $query;
    }

    //欠課数(年間ごとに集計）
    public function getKeekaYear($model, $year, $row, $schoolRow)
    {
        //ABSENT_COV:欠課数換算方法
        //ABSENT_COV_LATE:遅刻X回につき欠課1回
        //AMARI_KURIAGE:余りY回で繰上げ

        //遅刻・早退
        $late_early = "SUM(value(LATE,0)) + SUM(value(EARLY,0))";
        //欠課
        $notice = "SUM(value(NOTICE,0)) + SUM(value(NONOTICE,0)) + SUM(value(SICK,0)) + SUM(value(NURSEOFF,0))";
        //学校マスタの各フラグを参照し「休学・公欠・出廷・忌引・出廷（伝染病）」を欠課に含める処理。
        if ($schoolRow["SUB_OFFDAYS"]  == "1") {
            $notice .= " + SUM(value(OFFDAYS,0))";
        }
        if ($schoolRow["SUB_ABSENT"]   == "1") {
            $notice .= " + SUM(value(ABSENT,0))";
        }
        if ($schoolRow["SUB_SUSPEND"]  == "1") {
            $notice .= " + SUM(value(SUSPEND,0))";
        }
        if ($schoolRow["SUB_MOURNING"] == "1") {
            $notice .= " + SUM(value(MOURNING,0))";
        }
        if ($schoolRow["SUB_VIRUS"]    == "1") {
            $notice .= " + SUM(value(VIRUS,0))";
        }
        if ($schoolRow["SUB_KOUDOME"]  == "1") {
            $notice .= " + SUM(value(KOUDOME,0))";
        }

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
    public function getKeekaSemester($model, $year, $row, $schoolRow)
    {
        //ABSENT_COV:欠課数換算方法
        //ABSENT_COV_LATE:遅刻X回につき欠課1回
        //AMARI_KURIAGE:余りY回で繰上げ

        //遅刻・早退
        $late_early = "SUM(value(LATE,0)) + SUM(value(EARLY,0))";
        //欠課
        $notice = "SUM(value(NOTICE,0)) + SUM(value(NONOTICE,0)) + SUM(value(SICK,0)) + SUM(value(NURSEOFF,0))";
        //学校マスタの各フラグを参照し「休学・公欠・出廷・忌引・出廷（伝染病）」を欠課に含める処理。
        if ($schoolRow["SUB_OFFDAYS"]  == "1") {
            $notice .= " + SUM(value(OFFDAYS,0))";
        }
        if ($schoolRow["SUB_ABSENT"]   == "1") {
            $notice .= " + SUM(value(ABSENT,0))";
        }
        if ($schoolRow["SUB_SUSPEND"]  == "1") {
            $notice .= " + SUM(value(SUSPEND,0))";
        }
        if ($schoolRow["SUB_MOURNING"] == "1") {
            $notice .= " + SUM(value(MOURNING,0))";
        }
        if ($schoolRow["SUB_VIRUS"]    == "1") {
            $notice .= " + SUM(value(VIRUS,0))";
        }
        if ($schoolRow["SUB_KOUDOME"]  == "1") {
            $notice .= " + SUM(value(KOUDOME,0))";
        }

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
