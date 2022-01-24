<?php

require_once('for_php7.php');

class knjd214cQuery extends Query {

    //年組リスト
    function getAuth($model)
    {
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
    function getSelectGrade($model)
    {
        $query  = "SELECT DISTINCT ";
        $query .= "    CAST(SMALLINT(GRADE) AS CHAR(1)) || '学年' AS LABEL, ";
        $query .= "    GRADE AS VALUE ";
        $query .= "FROM ";
        $query .= "    SCHREG_REGD_HDAT ";
        $query .= "WHERE YEAR = '".CTRL_YEAR."' ";
        $query .= "  AND SEMESTER = '".$model->field["SEMESTER"]."' ";
        $query .= "ORDER BY GRADE ";

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

    //テスト種別取得
    function getTestItem($model, $sem)
    {
        $query  = " SELECT ";
        $query .= "     TESTKINDCD || TESTITEMCD AS VALUE, ";
        $query .= "     TESTITEMNAME AS LABEL ";
//        $query .= "     rtrim(ltrim(substr(TESTITEMNAME,1,9))) as LABEL "; //頭全角３文字
        $query .= " FROM ";
        $query .=       $model->testTable ;
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        if ($model->testTable == "TESTITEM_MST_COUNTFLG_NEW") {
            $query .= "     AND SEMESTER = '".$sem."' ";
        }
        $query .= " AND TESTKINDCD || TESTITEMCD NOT IN ('9900') ";
        //処理対象 1:学期末評価 2:学年評定
        if ($model->field["COPY_SAKI_DIV"] == "1") $query .= " AND TESTKINDCD NOT IN ('99') ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }
    //テスト種別(履歴用)
    function getRirekiTestName($model, $year, $testcd) {
        $sem      = substr($testcd, 0, 1);
        $kindItem = substr($testcd, 1, 4);

        $query  = " SELECT ";
        $query .= "     L1.SEMESTERNAME || T1.TESTITEMNAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     {$model->testTable} T1 ";
        $query .= "     LEFT JOIN SEMESTER_MST L1 ON L1.YEAR = T1.YEAR AND L1.SEMESTER = '{$sem}' ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '{$year}' ";
        if ($model->testTable == "TESTITEM_MST_COUNTFLG_NEW") {
            $query .= "     AND T1.SEMESTER = '{$sem}' ";
        }
        $query .= "     AND T1.TESTKINDCD || T1.TESTITEMCD = '{$kindItem}' ";
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
        $calcRow = $db->getRow(knjd214cQuery::getCalcDateTime(), DB_FETCHMODE_ASSOC);
        $calcDate = $calcRow["CALC_DATE"];//実行日付
        $calcTime = $calcRow["CALC_TIME"];//実行時間
        //実行履歴データ・追加
        $query = knjd214cQuery::getInsertRireki($calcDate, $calcTime, $model);
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
        $data["SAKI_TESTCD"][TEXT]          = "99900";//TODO
        if ($model->field["SHORI"] == "1") {
            //クリア
            $data["SOUGAKU_FLG"][TEXT]          = $model->field["SOUGAKU"];
        } else {
            //コピー
            $data["MOTO_TESTCD"][TEXT]          = $model->field["TESTKINDCD"];//TODO
            $data["KARI_DIV"][TEXT]             = $model->field["KARI_DIV"];
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
            $query = knjd214cQuery::getDeleteRireki($calcDate, $calcTime, $model);
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
    function getClearSql($model, $year, $sem) {
        //対象者(左のリスト）
        $array  = (strlen($model->selectdata) ? explode(",", $model->selectdata) : array());
        $gr_cl  = implode("','", $array);
        //処理対象 1:学期末評価 2:学年評定
        $semRec = $model->field["COPY_SAKI_DIV"] == '1' ? $sem : '9';

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
        $query .= "                             AND T3.SEMESTER = '{$semRec}' ";
        $query .= "                             AND T3.TESTKINDCD = '99' ";
        $query .= "                             AND T3.TESTITEMCD = '00' ";
        $query .= "                             AND T3.SCORE_DIV = '00' ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '{$year}' AND ";
        $query .= "     T1.SEMESTER = '{$sem}' AND ";
        $query .= "     T1.GRADE || T1.HR_CLASS IN ('".$gr_cl."') ";
        //on:総学等も含む on以外:総学等は処理対象外
        if ($model->field["SOUGAKU"] != "on") {
            $query .= "     AND T3.SUBCLASSCD NOT LIKE '90%' ";
            //教科科目名称マスタのマスタ化
            if ($model->Properties["useClassDetailDat"] == '1') {
                $query .= " AND SUBSTR(T3.SUBCLASSCD,1,2) || '-' || T3.SCHOOL_KIND NOT IN (SELECT N1.CLASSCD || '-' || N1.SCHOOL_KIND AS SETCD FROM CLASS_DETAIL_DAT N1 WHERE N1.YEAR = '".CTRL_YEAR."' AND N1.CLASS_SEQ = '003') ";
            } else {
                $query .= " AND SUBSTR(T3.SUBCLASSCD,1,2) NOT IN (SELECT N1.NAMECD2 FROM NAME_MST N1 WHERE N1.NAMECD1 = CASE WHEN EXISTS (SELECT 'X' FROM V_NAME_MST WHERE YEAR = '{$year}' AND NAMECD1 = 'D' || T3.SCHOOL_KIND || '08') THEN 'D' || T3.SCHOOL_KIND || '08' ELSE 'D008' END) ";
            }
        }

        return $query;
    }

    //クリア処理
    function getClearQuery($model) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);
        $sem = $model->field["SEMESTER"];
        //処理対象 1:学期末評価 2:学年評定
        $semRec = $model->field["COPY_SAKI_DIV"] == '1' ? $sem : '9';

        $query  = knjd214cQuery::getClearSql($model, CTRL_YEAR, $sem);
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
            $query = knjd214cQuery::getDeleteRecordDat(CTRL_YEAR, $semRec, "99", "00", "00", $subCd, $schno, $classCd, $schoolKind, $curriculumCd, $model);
            $db->query($query);

            //処理対象が学年評定
            if ($model->field["COPY_SAKI_DIV"] == '2') {
                //RECORD_PROV_FLG_DATのレコードを削除
                $query = knjd214cQuery::delRecordProvFlgDat(CTRL_YEAR, $subCd, $schno, $classCd, $schoolKind, $curriculumCd, $model);
                $db->query($query);
            }
        }
        $result->free();

        $db->commit();
        Query::dbCheckIn($db);
    }

    //クリア処理が行われたかチェック
    function getClearExists($model, $year, $sem) {
        //対象者(左のリスト）
        $array  = (strlen($model->selectdata) ? explode(",", $model->selectdata) : array());
        $gr_cl  = implode("','", $array);
        //処理対象 1:学期末評価 2:学年評定
        $semRec = $model->field["COPY_SAKI_DIV"] == '1' ? $sem : '9';

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
        $query .= "                             AND T3.SEMESTER = '{$semRec}' ";
        $query .= "                             AND T3.TESTKINDCD = '99' ";
        $query .= "                             AND T3.TESTITEMCD = '00' ";
        $query .= "                             AND T3.SCORE_DIV = '00' ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '{$year}' AND ";
        $query .= "     T1.SEMESTER = '{$sem}' AND ";
        $query .= "     T1.GRADE || T1.HR_CLASS IN ('".$gr_cl."') ";
        //総学等は処理対象外
        $query .= "     AND T3.SUBCLASSCD NOT LIKE '90%' ";
        //教科科目名称マスタのマスタ化
        if ($model->Properties["useClassDetailDat"] == '1') {
            $query .= " AND SUBSTR(T3.SUBCLASSCD,1,2) || '-' || T3.SCHOOL_KIND NOT IN (SELECT N1.CLASSCD || '-' || N1.SCHOOL_KIND AS SETCD FROM CLASS_DETAIL_DAT N1 WHERE N1.YEAR = '".CTRL_YEAR."' AND N1.CLASS_SEQ = '003') ";
        } else {
            $query .= " AND SUBSTR(T3.SUBCLASSCD,1,2) NOT IN (SELECT N1.NAMECD2 FROM NAME_MST N1 WHERE N1.NAMECD1 = CASE WHEN EXISTS (SELECT 'X' FROM V_NAME_MST WHERE YEAR = '{$year}' AND NAMECD1 = 'D' || T3.SCHOOL_KIND || '08') THEN 'D' || T3.SCHOOL_KIND || '08' ELSE 'D008' END) ";
        }

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
    function getCopySql($model, $year, $sem) {
        //対象者(左のリスト）
        $array  = (strlen($model->selectdata) ? explode(",", $model->selectdata) : array());
        $gr_cl  = implode("','", $array);
        $code   = $model->field["TESTKINDCD"];
        $code1  = substr($code, 0, 1);
        $code4  = substr($code, 1, 4);
        $div    = (substr($code, 1, 2) == "99") ? "00" : "01";
        $assess = ($model->field["CONVERT"] == "on") ? "L1.ASSESSLEVEL" : "T3.VALUE"; //評定換算する／しない

        $query  = "";
        //RECORD_SCORE_DAT
        $query .= " WITH T_RECORD_SCORE AS ( ";
        $query .= "     SELECT ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= "     T3.CLASSCD, ";
            $query .= "     T3.SCHOOL_KIND, ";
            $query .= "     T3.CURRICULUM_CD, ";
        }
        $query .= "         T3.SUBCLASSCD, ";
        $query .= "         T3.SCHREGNO, ";
        //コピー元が『9900：学期評価』のみ、指定学期分の平均をコピーする
        if ($model->Properties["useSiteiGakkibunHyoukaAvg"] == '1' && $code4 == '9900') {
            //評価の算出方法
            if ($model->calcMethod == "3") {
                $query .= " SMALLINT(FLOOR(AVG(FLOAT(T3.VALUE)))) AS VALUE ";   //3:切り捨て
            } else if ($model->calcMethod == "2") {
                $query .= " SMALLINT(CEIL(AVG(FLOAT(T3.VALUE)))) AS VALUE ";    //2:切り上げ
            } else {
                $query .= " SMALLINT(ROUND(AVG(FLOAT(T3.VALUE)),0)) AS VALUE "; //1:四捨五入(初期値)
            }
        } else {
            $query .= "     T3.VALUE ";
        }
        $query .= "     FROM ";
        $query .= "         RECORD_SCORE_DAT T3 ";
        $query .= "     WHERE ";
        $query .= "             T3.YEAR = '{$year}' ";
        //コピー元が『9900：学期評価』のみ、指定学期分の平均をコピーする(例．１・２学期評価の平均)
        if ($model->Properties["useSiteiGakkibunHyoukaAvg"] == '1' && $code4 == '9900') {
            $query .= "     AND T3.SEMESTER <= '{$code1}' ";
            $query .= "     AND T3.TESTKINDCD || T3.TESTITEMCD = '{$code4}' ";
        } else {
            $query .= "     AND T3.SEMESTER || T3.TESTKINDCD || T3.TESTITEMCD = '{$code}' ";
        }
        $query .= "         AND T3.SCORE_DIV = '{$div}' ";
        //総学等は処理対象外
        $query .= "         AND T3.SUBCLASSCD not like '90%' ";
        //教科科目名称マスタのマスタ化
        if ($model->Properties["useClassDetailDat"] == '1') {
            $query .= "     AND SUBSTR(T3.SUBCLASSCD,1,2) || '-' || T3.SCHOOL_KIND NOT IN (SELECT N1.CLASSCD || '-' || N1.SCHOOL_KIND AS SETCD FROM CLASS_DETAIL_DAT N1 WHERE N1.YEAR = '".CTRL_YEAR."' AND N1.CLASS_SEQ = '003') ";
        } else {
            $query .= "     AND SUBSTR(T3.SUBCLASSCD,1,2) NOT IN (SELECT N1.NAMECD2 FROM NAME_MST N1 WHERE N1.NAMECD1 = CASE WHEN EXISTS (SELECT 'X' FROM V_NAME_MST WHERE YEAR = '{$year}' AND NAMECD1 = 'D' || T3.SCHOOL_KIND || '08') THEN 'D' || T3.SCHOOL_KIND || '08' ELSE 'D008' END) ";
        }
        //コピー元が『9900：学期評価』のみ、指定学期分の平均をコピーする
        if ($model->Properties["useSiteiGakkibunHyoukaAvg"] == '1' && $code4 == '9900') {
            $query .= " GROUP BY ";
            //教育課程対応
            if ($model->Properties["useCurriculumcd"] == '1') {
                $query .= " T3.CLASSCD, ";
                $query .= " T3.SCHOOL_KIND, ";
                $query .= " T3.CURRICULUM_CD, ";
            }
            $query .= "     T3.SUBCLASSCD, ";
            $query .= "     T3.SCHREGNO ";
        }
        $query .= " ) ";

        $query .= " SELECT ";
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query .= " T3.CLASSCD, ";
            $query .= " T3.SCHOOL_KIND, ";
            $query .= " T3.CURRICULUM_CD, ";
        }
        $query .= "     T3.SUBCLASSCD, ";
        $query .= "     T3.SCHREGNO, ";
        $query .= "     ".$assess." AS VALUE, ";
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
        $query .= "     INNER JOIN T_RECORD_SCORE T3 ON T3.SCHREGNO = T1.SCHREGNO ";
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
        $query .= "                            AND T3.VALUE BETWEEN L1.ASSESSLOW AND L1.ASSESSHIGH ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '{$year}' AND ";
        $query .= "     T1.SEMESTER = '{$sem}' AND ";
        $query .= "     T1.GRADE || T1.HR_CLASS IN ('".$gr_cl."') ";

        return $query;
    }

    //処理対象レコード（合併先科目の単位自動計算）
    function getCombinedCopySql($model, $year, $sem) {
        //対象者(左のリスト）
        $array  = (strlen($model->selectdata) ? explode(",", $model->selectdata) : array());
        $gr_cl  = implode("','", $array);
        //処理対象 1:学期末評価 2:学年評定
        $semRec = $model->field["COPY_SAKI_DIV"] == '1' ? $sem : '9';

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
        $query .= "            SUM(case when 1 < T2.VALUE then T2.GET_CREDIT ";
        $query .= "                     when 1 = T2.VALUE then T2.COMP_CREDIT end) AS GET_CREDIT_Y ";
        $query .= "     FROM   SUBCLASS_REPLACE_COMBINED_DAT T1, ";
        $query .= "            RECORD_SCORE_DAT T2 ";
        $query .= "     WHERE  T1.YEAR = '{$year}' ";
        $query .= "       AND  T2.YEAR = '{$year}' ";
        $query .= "       AND  T2.SEMESTER = '{$semRec}' ";
        $query .= "       AND  T2.TESTKINDCD = '99' ";
        $query .= "       AND  T2.TESTITEMCD = '00' ";
        $query .= "       AND  T2.SCORE_DIV = '00' ";
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
        $query .= "          else T3.VALUE end AS VALUE, ";
        $query .= "     case when ".$noValue." and T6.FLG = '1' and ".$noCnt1." then T4.CREDITS ";
        $query .= "          when ".$noValue." and T6.FLG = '2' and ".$noCnt2." then T5.COMP_CREDIT ";
        $query .= "          when ".$noValue." then NULL ";
        $query .= "          when 1 <= T3.VALUE and T6.FLG = '1' then T4.CREDITS ";
        $query .= "          when 1 <= T3.VALUE and T6.FLG = '2' then T5.COMP_CREDIT ";
        $query .= "          else NULL end AS COMP_CREDIT, ";
        $query .= "     case when ".$noValue." and T6.FLG = '1' and ".$noCnt1." then T4.CREDITS ";
        $query .= "          when ".$noValue." and T6.FLG = '2' and ".$noCnt2." then T5.GET_CREDIT ";
        $query .= "          when ".$noValue." then NULL ";
        $query .= "          when 1 < T3.VALUE and T6.FLG = '1' then T4.CREDITS ";
        $query .= "          when 1 < T3.VALUE and T6.FLG = '2' and D015.NAMESPARE1 = 'Y' then T5.GET_CREDIT_Y ";
        $query .= "          when 1 < T3.VALUE and T6.FLG = '2' then T5.GET_CREDIT ";
        $query .= "          when 1 = T3.VALUE then 0 ";
        $query .= "          else NULL end AS GET_CREDIT ";
        $query .= " FROM ";
        $query .= "     SCHREG_REGD_DAT T1 ";
        $query .= "     INNER JOIN SCHREG_BASE_MST T2 ON T2.SCHREGNO = T1.SCHREGNO ";
        $query .= "                                  AND (T2.GRD_DATE IS NULL OR ";
        $query .= "                                       T2.GRD_DIV IS NULL OR ";
        $query .= "                                       T2.GRD_DIV = '4') ";
        $query .= "     INNER JOIN RECORD_SCORE_DAT T3 ON T3.SCHREGNO = T1.SCHREGNO ";
        $query .= "                             AND T3.YEAR = '{$year}' ";
        $query .= "                             AND T3.SEMESTER = '{$semRec}' ";
        $query .= "                             AND T3.TESTKINDCD = '99' ";
        $query .= "                             AND T3.TESTITEMCD = '00' ";
        $query .= "                             AND T3.SCORE_DIV = '00' ";
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
        $query .= "     T1.SEMESTER = '{$sem}' AND ";
        $query .= "     T1.GRADE || T1.HR_CLASS IN ('".$gr_cl."') ";
        //総学等は処理対象外
        $query .= "     AND T3.SUBCLASSCD NOT LIKE '90%' ";
        //教科科目名称マスタのマスタ化
        if ($model->Properties["useClassDetailDat"] == '1') {
            $query .= " AND SUBSTR(T3.SUBCLASSCD,1,2) || '-' || T3.SCHOOL_KIND NOT IN (SELECT N1.CLASSCD || '-' || N1.SCHOOL_KIND AS SETCD FROM CLASS_DETAIL_DAT N1 WHERE N1.YEAR = '".CTRL_YEAR."' AND N1.CLASS_SEQ = '003') ";
        } else {
            $query .= " AND SUBSTR(T3.SUBCLASSCD,1,2) NOT IN (SELECT N1.NAMECD2 FROM NAME_MST N1 WHERE N1.NAMECD1 = CASE WHEN EXISTS (SELECT 'X' FROM V_NAME_MST WHERE YEAR = '{$year}' AND NAMECD1 = 'D' || T3.SCHOOL_KIND || '08') THEN 'D' || T3.SCHOOL_KIND || '08' ELSE 'D008' END) ";
        }

        return $query;
    }

    //処理対象レコード（履歴）
    function getProvCopySql($model, $year, $sem) {
        //対象者(左のリスト）
        $array  = (strlen($model->selectdata) ? explode(",", $model->selectdata) : array());
        $gr_cl  = implode("','", $array);
        //処理対象 1:学期末評価 2:学年評定
        $semRec = $model->field["COPY_SAKI_DIV"] == '1' ? $sem : '9';

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
        $query .= "     T3.VALUE, ";
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
        $query .= "                             AND T3.SEMESTER = '{$semRec}' ";
        $query .= "                             AND T3.TESTKINDCD = '99' ";
        $query .= "                             AND T3.TESTITEMCD = '00' ";
        $query .= "                             AND T3.SCORE_DIV = '00' ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '{$year}' AND ";
        $query .= "     T1.SEMESTER = '{$sem}' AND ";
        $query .= "     T1.GRADE || T1.HR_CLASS IN ('".$gr_cl."') ";
        //総学等は処理対象外
        $query .= "     AND T3.SUBCLASSCD NOT LIKE '90%' ";
        //教科科目名称マスタのマスタ化
        if ($model->Properties["useClassDetailDat"] == '1') {
            $query .= " AND SUBSTR(T3.SUBCLASSCD,1,2) || '-' || T3.SCHOOL_KIND NOT IN (SELECT N1.CLASSCD || '-' || N1.SCHOOL_KIND AS SETCD FROM CLASS_DETAIL_DAT N1 WHERE N1.YEAR = '".CTRL_YEAR."' AND N1.CLASS_SEQ = '003') ";
        } else {
            $query .= " AND SUBSTR(T3.SUBCLASSCD,1,2) NOT IN (SELECT N1.NAMECD2 FROM NAME_MST N1 WHERE N1.NAMECD1 = CASE WHEN EXISTS (SELECT 'X' FROM V_NAME_MST WHERE YEAR = '{$year}' AND NAMECD1 = 'D' || T3.SCHOOL_KIND || '08') THEN 'D' || T3.SCHOOL_KIND || '08' ELSE 'D008' END) ";
        }

        return $query;
    }

    //コピー処理
    function getCopyQuery($model) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);
        $sem = $model->field["SEMESTER"];
        //処理対象 1:学期末評価 2:学年評定
        $semRec = $model->field["COPY_SAKI_DIV"] == '1' ? $sem : '9';

        $query  = knjd214cQuery::getCopySql($model, CTRL_YEAR, $sem);
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
            $gradValue  = $row["VALUE"];
            $compCredit = $row["COMP_CREDIT"];
            $getCredit  = $row["GET_CREDIT"];
            //評価を学年評定にコピーする。単位もコピーする。
            $query = knjd214cQuery::getInsertRecordDat(CTRL_YEAR, $semRec, "99", "00", "00", $subCd, $schno, $gradValue, $compCredit, $getCredit, $classCd, $schoolKind, $curriculumCd, $model);
            $db->query($query);

            //処理対象が学年評定
            if ($model->field["COPY_SAKI_DIV"] == '2') {
                //評定フラグが仮評定の場合
                if ($model->field["KARI_DIV"] == '1') {
                    //RECORD_PROV_FLG_DATのレコードを追加
                    $query = knjd214cQuery::insRecordProvFlgDat(CTRL_YEAR, $subCd, $schno, $classCd, $schoolKind, $curriculumCd, $model);
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
        $db = Query::dbCheckOut();
        $db->autoCommit(false);
        $sem = $model->field["SEMESTER"];
        //処理対象 1:学期末評価 2:学年評定
        $semRec = $model->field["COPY_SAKI_DIV"] == '1' ? $sem : '9';

        $query  = knjd214cQuery::getCombinedCopySql($model, CTRL_YEAR, $sem);
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
            $gradValue  = $row["VALUE"];
            $compCredit = $row["COMP_CREDIT"];
            $getCredit  = $row["GET_CREDIT"];
            //再処理のため、RECORD_SCORE_DATのレコードを一旦削除。
            $query = knjd214cQuery::getDeleteRecordDat(CTRL_YEAR, $semRec, "99", "00", "00", $subCd, $schno, $classCd, $schoolKind, $curriculumCd, $model);
            $db->query($query);
            //評価を学年評定にコピーする。単位もコピーする。
            $query = knjd214cQuery::getInsertRecordDat(CTRL_YEAR, $semRec, "99", "00", "00", $subCd, $schno, $gradValue, $compCredit, $getCredit, $classCd, $schoolKind, $curriculumCd, $model);
            $db->query($query);

            //処理対象が学年評定
            if ($model->field["COPY_SAKI_DIV"] == '2') {
                //再処理のため、RECORD_PROV_FLG_DATのレコードを一旦削除
                $query = knjd214cQuery::delRecordProvFlgDat(CTRL_YEAR, $subCd, $schno, $classCd, $schoolKind, $curriculumCd, $model);
                $db->query($query);
                //評定フラグが仮評定の場合
                if ($model->field["KARI_DIV"] == '1') {
                    //RECORD_PROV_FLG_DATのレコードを追加
                    $query = knjd214cQuery::insRecordProvFlgDat(CTRL_YEAR, $subCd, $schno, $classCd, $schoolKind, $curriculumCd, $model);
                    $db->query($query);
                }
            }
        }
        $result->free();

        $db->commit();
        Query::dbCheckIn($db);
    }

    //コピー処理（履歴）
    function getProvCopyQuery($model) {
        //変数
        $code   = $model->field["TESTKINDCD"]; //TODO
        $sem    = substr($code, 0, 1);
        $kind   = substr($code, 1, 2);
        $item   = substr($code, 3, 2);

        $db = Query::dbCheckOut();
        $db->autoCommit(false);
        $query  = knjd214cQuery::getProvCopySql($model, CTRL_YEAR, $model->field["SEMESTER"]);
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
            $gradValue  = $row["VALUE"];
            $compCredit = $row["COMP_CREDIT"];
            $getCredit  = $row["GET_CREDIT"];
            //レコードを一旦削除。
            $query = knjd214cQuery::getDeleteProv(CTRL_YEAR, $sem, $kind, $item, $subCd, $schno, $classCd, $schoolKind, $curriculumCd, $model);
            $db->query($query);
            //履歴にコピー。
            $query = knjd214cQuery::getInsertProv(CTRL_YEAR, $sem, $kind, $item, $subCd, $schno, $gradValue, $compCredit, $getCredit, $classCd, $schoolKind, $curriculumCd, $model);
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
        $data["VALUE"][NUMBER]      = $gradValue;
        //処理対象 1:学期末評価 2:学年評定
        if ($model->field["COPY_SAKI_DIV"] == '2') {
            $data["COMP_CREDIT"][NUMBER]= $compCredit;
            $data["GET_CREDIT"][NUMBER] = $getCredit;
        }
        $data["REGISTERCD"][TEXT]   = STAFFCD;
        $data["UPDATED"][FUNC]      = "SYSDATE()";

        $query = Query::insertSQL($data, "RECORD_SCORE_DAT");
        return $query;
    }

    //RECORD_PROV_RATE_DATのレコードを削除。
    function getDeleteProv($year, $sem, $kind, $item, $subCd, $schno, $classCd, $schoolKind, $curriculumCd, $model) {
        $query  = "";
        $query .= " DELETE FROM RECORD_PROV_RATE_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '{$year}' ";
        $query .= "     AND SEMESTER = '{$sem}' ";
        $query .= "     AND TESTKINDCD = '{$kind}' ";
        $query .= "     AND TESTITEMCD = '{$item}' ";
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
    function getInsertProv($year, $sem, $kind, $item, $subCd, $schno, $gradValue, $compCredit, $getCredit, $classCd, $schoolKind, $curriculumCd, $model) {
        $data = array();
        $data["YEAR"][TEXT]         = $year;
        $data["SEMESTER"][TEXT]     = $sem;
        $data["TESTKINDCD"][TEXT]   = $kind;
        $data["TESTITEMCD"][TEXT]   = $item;
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $data["CLASSCD"][TEXT]       = $classCd;
            $data["SCHOOL_KIND"][TEXT]   = $schoolKind;
            $data["CURRICULUM_CD"][TEXT] = $curriculumCd;
        }
        $data["SUBCLASSCD"][TEXT]   = $subCd;
        $data["SCHREGNO"][TEXT]     = $schno;
        //仮評定
        $data["VALUE"][NUMBER]      = $gradValue;
        //処理対象 1:学期末評価 2:学年評定
        if ($model->field["COPY_SAKI_DIV"] == '2') {
            $data["COMP_CREDIT"][NUMBER]= $compCredit;
            $data["GET_CREDIT"][NUMBER] = $getCredit;
        }
        $data["REGISTERCD"][TEXT]   = STAFFCD;
        $data["UPDATED"][FUNC]      = "SYSDATE()";

        $query = Query::insertSQL($data, "RECORD_PROV_RATE_DAT");
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
}
?>
