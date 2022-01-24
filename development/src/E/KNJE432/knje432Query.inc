<?php

require_once('for_php7.php');
class knje432Query extends Query
{

    //教育委員会用の学校コード取得
    public function getSchoolMst($model)
    {
        $query  = " SELECT ";
        $query .= "      PREF_CD, ";
        $query .= "      KYOUIKU_IINKAI_SCHOOLCD ";
        $query .= " FROM ";
        $query .= "      V_SCHOOL_MST ";
        $query .= " WHERE ";
        $query .= "      YEAR    = '".$model->field["YEAR"]."' ";

        return $query;
    }

    //年度取得
    public function getYear()
    {
        $query  = " SELECT ";
        $query .= "      YEAR AS LABEL, ";
        $query .= "      YEAR AS VALUE ";
        $query .= " FROM ";
        $query .= "      SCHOOL_MST ";
        $query .= " ORDER BY ";
        $query .= "      VALUE DESC ";

        return $query;
    }

    //文書番号
    public function getTuutatu($model)
    {
        $query  = " SELECT ";
        $query .= "     T1.DOC_NUMBER AS VALUE, ";
        $query .= "     T1.DOC_NUMBER AS LABEL ";
        $query .= " FROM ";
        $query .= "     AFT_SEARCH_REPORT_SCHOOL_DAT T1 ";
        $query .= "     INNER JOIN AFT_SEARCH_REPORT_DAT L1 ON T1.YEAR = L1.YEAR ";
        $query .= "           AND T1.DOC_NUMBER = L1.DOC_NUMBER ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".$model->field["YEAR"]."' ";
        $query .= "     AND T1.EDBOARD_SCHOOLCD = '{$model->schoolcd}' ";
        $query .= "     AND L1.REQUEST_ANSWER_PRG = 'KNJE432' ";
        $query .= "     AND L1.SUBMISSION_DATE IS NOT NULL ";
        $query .= " ORDER BY ";
        $query .= "     T1.DOC_NUMBER ";

        return $query;
    }

    //就職進路
    public function getIndustry()
    {
        $query  = " WITH INDUSTRY (INDUSTRY_LCD, INDUSTRY_LNAME, SORT) AS ( ";
        $query .= "     SELECT ";
        $query .= "         INDUSTRY_LCD, ";
        $query .= "         INDUSTRY_LNAME, ";
        $query .= "         INDUSTRY_LCD ";
        $query .= "     FROM ";
        $query .= "         INDUSTRY_L_MST ";
        $query .= "     UNION ";
        $query .= "         VALUES('', '計', 'ZZ') ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     INDUSTRY ";
        $query .= " ORDER BY ";
        $query .= "     SORT ";

        return $query;
    }

    //データ表示
    public function readQuery($model, $flg = "")
    {
        $query  = "";
        //学科・性別
        $query .= " WITH T_COURSE AS ( ";
        $query .= "     SELECT ";
        $query .= "         T1.COURSECD, ";
        $query .= "         T1.MAJORCD, ";
        $query .= "         T3.MAJORNAME ";
        $query .= "     FROM ";
        $query .= "         SCHREG_REGD_DAT T1 ";
        $query .= "         INNER JOIN SCHREG_BASE_MST T2 ";
        $query .= "             ON  T2.SCHREGNO = T1.SCHREGNO ";
        $query .= "         INNER JOIN V_COURSE_MAJOR_MST T3 ";
        $query .= "             ON  T3.YEAR     = T1.YEAR ";
        $query .= "             AND T3.COURSECD = T1.COURSECD ";
        $query .= "             AND T3.MAJORCD  = T1.MAJORCD ";
        $query .= "     WHERE ";
        $query .= "             T1.YEAR     = '".$model->field["YEAR"]."' ";
        $query .= "         AND T1.SEMESTER = '".CTRL_SEMESTER."' ";
        $query .= "     GROUP BY ";
        $query .= "         T1.COURSECD, ";
        $query .= "         T1.MAJORCD, ";
        $query .= "         T3.MAJORNAME ";
        $query .= "     ) ";
        $query .= " , T_SEX (SEX, SEX_NAME) AS ( ";
        $query .= "     SELECT ";
        $query .= "         NAMECD2, ";
        $query .= "         ABBV1 ";
        $query .= "     FROM ";
        $query .= "         NAME_MST ";
        $query .= "     WHERE ";
        $query .= "         NAMECD1 = 'Z002' ";
        $query .= "     ) ";
        $query .= " , T_COURSE_SEX AS ( ";
        $query .= "     SELECT ";
        $query .= "         T1.*, ";
        $query .= "         T2.* ";
        $query .= "     FROM ";
        $query .= "         T_COURSE T1, ";
        $query .= "         T_SEX T2 ";
        $query .= "     WHERE ";
        $query .= "         T2.SEX != '9' ";
        $query .= "     ) ";
        if ($model->cmd == "recalc") {
            //再計算
            $query .= " , T_RECALC AS ( ";
            $query .= "     SELECT ";
            $query .= "         T1.COURSECD, ";
            $query .= "         T1.MAJORCD, ";
            $query .= "         T2.SEX, ";
            $i = 1;
            foreach ($model->industryData as $key => $indust) {
                $indL = $indust["LCD"];
                $aftCd = $indust["TOUROKU_CD"];
                if (!$indL) {
                    break;
                }
                $query .= "         SUM(CASE WHEN VALUE(IND.INDUSTRY_LCD, '99') = '{$indL}' AND VALUE(L1.PREF_CD, '99') = '{$model->prefcd}' THEN 1 ELSE 0 END) AS COUNT{$i}, ";
                $i++;
                $query .= "         SUM(CASE WHEN VALUE(IND.INDUSTRY_LCD, '99') = '{$indL}' AND VALUE(L1.PREF_CD, '99') != '{$model->prefcd}' THEN 1 ELSE 0 END) AS COUNT{$i}, ";
                $i++;
            }
            $query .= "         SUM(CASE WHEN VALUE(L1.PREF_CD, '99') = '{$model->prefcd}' THEN 1 ELSE 0 END) AS COUNT98, ";
            $i++;
            $query .= "         SUM(CASE WHEN VALUE(L1.PREF_CD, '99') != '{$model->prefcd}' THEN 1 ELSE 0 END) AS COUNT99 ";
            $query .= "     FROM ";
            $query .= "         SCHREG_REGD_DAT T1 ";
            $query .= "         INNER JOIN SCHREG_BASE_MST T2 ";
            $query .= "             ON  T2.SCHREGNO = T1.SCHREGNO ";
            $query .= "         INNER JOIN SCHREG_REGD_GDAT T3 ";
            $query .= "             ON  T3.YEAR     = T1.YEAR ";
            $query .= "             AND T3.GRADE    = T1.GRADE ";
            $query .= "         INNER JOIN AFT_GRAD_COURSE_DAT L1 ";
            $query .= "             ON  L1.YEAR     = T1.YEAR ";
            $query .= "             AND L1.SCHREGNO = T1.SCHREGNO ";
            $query .= "             AND L1.SENKOU_KIND = '1' ";
            $query .= "             AND L1.PLANSTAT = '1' ";
            $query .= "         LEFT JOIN COMPANY_MST COMPANY ON L1.STAT_CD = COMPANY.COMPANY_CD ";
            $query .= "         LEFT JOIN INDUSTRY_L_MST IND ";
            $query .= "             ON  COMPANY.INDUSTRY_LCD = IND.INDUSTRY_LCD ";
            $query .= "     WHERE ";
            $query .= "             T1.YEAR     = '".$model->field["YEAR"]."' ";
            $query .= "         AND T1.SEMESTER = '".CTRL_SEMESTER."' ";
            $query .= "     GROUP BY ";
            $query .= "         GROUPING SETS ((T1.COURSECD, T1.MAJORCD, T2.SEX), (T2.SEX), ()) ";
            $query .= "     ) ";
        } else {
            //登録テーブル
            $query .= " , T_ADDITION1 AS ( ";
            $query .= " SELECT ";
            $query .= "     T1.EDBOARD_SCHOOLCD, ";
            $query .= "     T1.YEAR, ";
            $query .= "     T1.COURSECD, ";
            $query .= "     T1.MAJORCD, ";
            $query .= "     T1.SEX, ";
            for ($industryCnt = 1; $industryCnt <= (get_count($model->industryData) - 1) * 2; $industryCnt++) {
                $query .= "     L{$industryCnt}.COUNT AS COUNT{$industryCnt}, ";
            }
            $query .= "     L98.COUNT AS COUNT98, ";
            $query .= "     L99.COUNT AS COUNT99 ";
            $query .= " FROM ";
            $query .= "     ( ";
            $query .= "     SELECT ";
            $query .= "         EDBOARD_SCHOOLCD, ";
            $query .= "         YEAR, ";
            $query .= "         COURSECD, ";
            $query .= "         MAJORCD, ";
            $query .= "         SEX ";
            $query .= "     FROM ";
            $query .= "         AFT_DISEASE_ADDITION432_DAT ";
            $query .= "     WHERE ";
            $query .= "             YEAR = '".$model->field["YEAR"]."' ";
            $query .= "             AND EDBOARD_SCHOOLCD = '".$model->schoolcd."' ";
            $query .= "     GROUP BY ";
            $query .= "         EDBOARD_SCHOOLCD, ";
            $query .= "         YEAR, ";
            $query .= "         COURSECD, ";
            $query .= "         MAJORCD, ";
            $query .= "         SEX ";
            $query .= "     ) T1 ";
            $setCnt = 1;
            for ($industryCnt = 1; $industryCnt <= (get_count($model->industryData) - 1); $industryCnt++) {
                $setDiv = $industryCnt < 10 ? "0".$industryCnt : $industryCnt;
                //県内
                $query .= "     LEFT JOIN AFT_DISEASE_ADDITION432_DAT L{$setCnt} ON T1.EDBOARD_SCHOOLCD = L{$setCnt}.EDBOARD_SCHOOLCD ";
                $query .= "          AND T1.YEAR = L{$setCnt}.YEAR ";
                $query .= "          AND T1.COURSECD = L{$setCnt}.COURSECD ";
                $query .= "          AND T1.MAJORCD = L{$setCnt}.MAJORCD ";
                $query .= "          AND T1.SEX = L{$setCnt}.SEX ";
                $query .= "          AND L{$setCnt}.LARGE_DIV = '{$setDiv}' ";
                $query .= "          AND L{$setCnt}.SMALL_DIV = '001' ";
                $setCnt++;
                //県外
                $query .= "     LEFT JOIN AFT_DISEASE_ADDITION432_DAT L{$setCnt} ON T1.EDBOARD_SCHOOLCD = L{$setCnt}.EDBOARD_SCHOOLCD ";
                $query .= "          AND T1.YEAR = L{$setCnt}.YEAR ";
                $query .= "          AND T1.COURSECD = L{$setCnt}.COURSECD ";
                $query .= "          AND T1.MAJORCD = L{$setCnt}.MAJORCD ";
                $query .= "          AND T1.SEX = L{$setCnt}.SEX ";
                $query .= "          AND L{$setCnt}.LARGE_DIV = '{$setDiv}' ";
                $query .= "          AND L{$setCnt}.SMALL_DIV = '002' ";
                $setCnt++;
            }
            $query .= "     LEFT JOIN AFT_DISEASE_ADDITION432_DAT L98 ON T1.EDBOARD_SCHOOLCD = L98.EDBOARD_SCHOOLCD ";
            $query .= "          AND T1.YEAR = L98.YEAR ";
            $query .= "          AND T1.COURSECD = L98.COURSECD ";
            $query .= "          AND T1.MAJORCD = L98.MAJORCD ";
            $query .= "          AND T1.SEX = L98.SEX ";
            $query .= "          AND L98.LARGE_DIV = '99' ";
            $query .= "          AND L98.SMALL_DIV = '001' ";
            $query .= "     LEFT JOIN AFT_DISEASE_ADDITION432_DAT L99 ON T1.EDBOARD_SCHOOLCD = L99.EDBOARD_SCHOOLCD ";
            $query .= "          AND T1.YEAR = L99.YEAR ";
            $query .= "          AND T1.COURSECD = L99.COURSECD ";
            $query .= "          AND T1.MAJORCD = L99.MAJORCD ";
            $query .= "          AND T1.SEX = L99.SEX ";
            $query .= "          AND L99.LARGE_DIV = '99' ";
            $query .= "          AND L99.SMALL_DIV = '002' ";
            $query .= "     ) ";
        }

        //メイン
        $query .= " SELECT DISTINCT ";
        if ($flg) {
            $query .= "     T1.COURSECD, ";
            $query .= "     T1.MAJORCD, ";
            $query .= "     T1.SEX, ";
        } else {
            $query .= "     T1.COURSECD, ";
            $query .= "     T1.MAJORCD, ";
            $query .= "     T1.SEX, ";
            $query .= "     T1.MAJORNAME, ";
            $query .= "     T1.SEX_NAME, ";
        }
        for ($industryCnt = 1; $industryCnt <= (get_count($model->industryData) - 1) * 2; $industryCnt++) {
            $query .= "     L1.COUNT{$industryCnt} AS COUNT{$industryCnt}, ";
        }
        $query .= "     L1.COUNT98, ";
        $query .= "     L1.COUNT99 ";
        if ($flg) {
            $query .= "     ,'".$model->lastColumn."' ";
        }
        $query .= " FROM ";
        $query .= "     T_COURSE_SEX T1 ";
        if ($model->cmd == "recalc") {
            $query .= "     LEFT JOIN T_RECALC L1 ON L1.COURSECD = T1.COURSECD ";
            $query .= "          AND L1.MAJORCD = T1.MAJORCD ";
            $query .= "          AND L1.SEX = T1.SEX ";
        } else {
            $query .= "     LEFT JOIN T_ADDITION1 L1 ON L1.COURSECD = T1.COURSECD ";
            $query .= "          AND L1.MAJORCD = T1.MAJORCD ";
            $query .= "          AND L1.SEX = T1.SEX ";
        }
        $query .= " ORDER BY ";
        $query .= "     T1.COURSECD, ";
        $query .= "     T1.MAJORCD, ";
        $query .= "     T1.SEX ";

        return $query;
    }

    //UPDATE
    public function &getUpdateQuery($model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //DELETE
        $query  = "DELETE FROM AFT_DISEASE_ADDITION432_DAT ";
        $query .= " WHERE EDBOARD_SCHOOLCD  = '".$model->schoolcd."'";
        $query .= "   AND YEAR              = '".$model->field["YEAR"]."'";
        $db->query($query);

        //INSERT
        for ($i = 0; $i < get_count($model->fields["CODE"]); $i++) {
            //課程・学科・性別
            $code_arr = explode("-", $model->fields["CODE"][$i]);

            $totalVal1 = 0;
            $totalVal2 = 0;
            $setCnt = 1;
            for ($industryCnt = 1; $industryCnt <= (get_count($model->industryData) - 1); $industryCnt++) {
                $setDiv = $industryCnt < 10 ? "0".$industryCnt : $industryCnt;
                for ($set2 = 1; $set2 <= 2; $set2++) {
                    $data = array(); //初期化
                    $data["EDBOARD_SCHOOLCD"][TEXT]     = $model->schoolcd;
                    $data["YEAR"][TEXT]                 = $model->field["YEAR"];
                    $data["COURSECD"][TEXT]             = $code_arr[0];
                    $data["MAJORCD"][TEXT]              = $code_arr[1];
                    $data["LARGE_DIV"][TEXT]            = $setDiv;
                    $data["MIDDLE_DIV"][TEXT]           = "99";
                    $data["SMALL_DIV"][TEXT]            = "00".$set2;
                    $data["SEX"][TEXT]                  = $code_arr[2];
                    //人数
                    $data["COUNT"][NUMBER]              = $model->fields["COUNT".$setCnt][$i];

                    $data["REGISTERCD"][TEXT]           = STAFFCD;
                    $data["UPDATED"][FUNC]              = " sysdate()";

                    if ($set2 == "1") {
                        $totalVal1 += $model->fields["COUNT".$setCnt][$i];
                    } else {
                        $totalVal2 += $model->fields["COUNT".$setCnt][$i];
                    }
                    $query = Query::insertSQL($data, "AFT_DISEASE_ADDITION432_DAT");
                    $db->query($query);
                    $setCnt++;
                }
            }

            for ($totalCnt = 1; $totalCnt <= 2; $totalCnt++) {
                $data = array(); //初期化
                $setDiv = $totalCnt;
                $data["EDBOARD_SCHOOLCD"][TEXT]     = $model->schoolcd;
                $data["YEAR"][TEXT]                 = $model->field["YEAR"];
                $data["COURSECD"][TEXT]             = $code_arr[0];
                $data["MAJORCD"][TEXT]              = $code_arr[1];
                $data["LARGE_DIV"][TEXT]            = "99";
                $data["MIDDLE_DIV"][TEXT]           = "99";
                $data["SMALL_DIV"][TEXT]            = "00".$totalCnt;
                $data["SEX"][TEXT]                  = $code_arr[2];
                //人数
                $data["COUNT"][NUMBER]              = $totalCnt == "1" ? $totalVal1 : $totalVal2;

                $data["REGISTERCD"][TEXT]           = STAFFCD;
                $data["UPDATED"][FUNC]              = " sysdate()";

                $query = Query::insertSQL($data, "AFT_DISEASE_ADDITION432_DAT");
                $db->query($query);
            }
        }

        $db->commit();
        Query::dbCheckIn($db);
        return;
    }

    //県への報告履歴コンボ
    public function getReport($model)
    {
        $query  = " SELECT ";
        $query .= "     T1.COURSECD || ':' || T1.MAJORCD AS VALUE, ";
        $query .= "     T1.COURSECD || T1.MAJORCD || '　' || L1.MAJORNAME || '　' || REPLACE(CHAR(T1.EXECUTE_DATE), '-', '/') AS LABEL ";
        $query .= " FROM ";
        $query .= "     REPORT_AFT_DISEASE_ADDITION432_DAT T1 ";
        $query .= "     INNER JOIN V_COURSE_MAJOR_MST L1 ";
        $query .= "         ON  L1.YEAR     = T1.YEAR ";
        $query .= "         AND L1.COURSECD = T1.COURSECD ";
        $query .= "         AND L1.MAJORCD  = T1.MAJORCD ";
        $query .= " WHERE ";
        $query .= "         T1.EDBOARD_SCHOOLCD = '".$model->schoolcd."' ";
        $query .= "     AND T1.YEAR             = '".$model->field["YEAR"]."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE DESC ";

        return $query;
    }

    //学校側から県側へコピーするデータ取得（疾病等結果一覧）
    public function getAddition432Dat($model)
    {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     AFT_DISEASE_ADDITION432_DAT ";
        $query .= " WHERE ";
        $query .= "     EDBOARD_SCHOOLCD = '".$model->schoolcd."' ";
        $query .= "     AND YEAR = '".$model->field["YEAR"]."' ";

        return $query;
    }

    public function getRepAddition432Dat($model)
    {
        $query  = " SELECT DISTINCT";
        $query .= "     EDBOARD_SCHOOLCD, ";
        $query .= "     YEAR, ";
        $query .= "     COURSECD, ";
        $query .= "     MAJORCD ";
        $query .= " FROM ";
        $query .= "     AFT_DISEASE_ADDITION432_DAT ";
        $query .= " WHERE ";
        $query .= "     EDBOARD_SCHOOLCD = '".$model->schoolcd."' ";
        $query .= "     AND YEAR = '".$model->field["YEAR"]."' ";

        return $query;
    }

    //UPDATE
    public function &getUpdateReport($model)
    {
        $db = Query::dbCheckOut();
        $db2 = Query::dbCheckOut2();
        $db->autoCommit(false);
        $db2->autoCommit(false);

        //DELETE
        //県側・就職進路状況テーブル
        $query  = "DELETE FROM AFT_DISEASE_ADDITION432_DAT ";
        $query .= " WHERE EDBOARD_SCHOOLCD  = '".$model->schoolcd."'";
        $query .= "   AND YEAR              = '".$model->field["YEAR"]."'";
        $db2->query($query);
        //県側、学校側・報告テーブル
        $query  = "DELETE FROM REPORT_AFT_DISEASE_ADDITION432_DAT ";
        $query .= " WHERE EDBOARD_SCHOOLCD  = '".$model->schoolcd."'";
        $query .= "   AND YEAR              = '".$model->field["YEAR"]."'";
        $db->query($query);
        $db2->query($query);

        //INSERT
        //学校側から県側へコピーするデータ取得（就職進路状況）
        $query = knje432Query::getAddition432Dat($model);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            //就職進路状況テーブル
            //キー
            $data = array(); //初期化
            $data["EDBOARD_SCHOOLCD"][TEXT]     = $row["EDBOARD_SCHOOLCD"];
            $data["YEAR"][TEXT]                 = $row["YEAR"];
            $data["COURSECD"][TEXT]             = $row["COURSECD"];
            $data["MAJORCD"][TEXT]              = $row["MAJORCD"];
            $data["LARGE_DIV"][TEXT]            = $row["LARGE_DIV"];
            $data["MIDDLE_DIV"][TEXT]           = $row["MIDDLE_DIV"];
            $data["SMALL_DIV"][TEXT]            = $row["SMALL_DIV"];
            $data["SEX"][TEXT]                  = $row["SEX"];
            //人数
            $data["COUNT"][NUMBER]              = $row["COUNT"];

            $data["REGISTERCD"][TEXT]           = STAFFCD;
            $data["UPDATED"][FUNC]              = " sysdate()";
            $query  = Query::insertSQL($data, "AFT_DISEASE_ADDITION432_DAT");
            $db2->query($query);
        }
        $result->free();

        //報告用データ取得
        $query = knje432Query::getRepAddition432Dat($model);
        $result2 = $db->query($query);
        while ($row2 = $result2->fetchRow(DB_FETCHMODE_ASSOC)) {
            //県側、学校側・報告テーブル
            $data = array(); //初期化
            $data["EDBOARD_SCHOOLCD"][TEXT]     = $row2["EDBOARD_SCHOOLCD"];
            $data["YEAR"][TEXT]                 = $row2["YEAR"];
            $data["COURSECD"][TEXT]             = $row2["COURSECD"];
            $data["MAJORCD"][TEXT]              = $row2["MAJORCD"];
            $data["EXECUTE_DATE"][DATE]         = $model->field["EXECUTE_DATE"];
            $data["REGISTERCD"][TEXT]           = STAFFCD;
            $data["UPDATED"][FUNC]              = " sysdate()";
            $query  = Query::insertSQL($data, "REPORT_AFT_DISEASE_ADDITION432_DAT");

            $db->query($query);
            $db2->query($query);
        }
        $result2->free();

        //報告処理
        $data = array();
        $data["ANSWER_FLG"][TEXT]      = "1";
        $data["ANSWER_DATE"][FUNC]     = "SYSDATE()";
        $data["REGISTERCD"][TEXT]      = STAFFCD;
        $data["UPDATED"][FUNC]         = "SYSDATE()";

        $where  = " WHERE ";
        $where .= "     YEAR = '".$model->field["YEAR"]."' ";
        $where .= "     AND DOC_NUMBER = {$model->field["DOC_NUMBER"]} ";
        $where .= "     AND EDBOARD_SCHOOLCD = '{$model->schoolcd}' ";

        $query = Query::updateSQL($data, "AFT_SEARCH_REPORT_SCHOOL_DAT", $where);
        $db2->query($query);

        $db->commit();
        $db2->commit();
        Query::dbCheckIn($db);
        Query::dbCheckIn($db2);
        return;
    }

    //CSVデータ処理
    //更新データチェック処理
    public function upCheckQuery($model, $setData, $largeDiv, $middleDiv, $smallDiv)
    {
        $query  = " SELECT ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM  ";
        $query .= "     AFT_DISEASE_ADDITION432_DAT ";
        $query .= " WHERE EDBOARD_SCHOOLCD  = '".$model->schoolcd."'";
        $query .= "   AND YEAR              = '".$model->field["YEAR"]."'";
        $query .= "   AND COURSECD          = '".$setData["COURSECD"]."'";
        $query .= "   AND MAJORCD           = '".$setData["MAJORCD"]."'";
        $query .= "   AND SEX               = '".$setData["SEX"]."'";
        $query .= "   AND LARGE_DIV         = '".$largeDiv."'";
        $query .= "   AND MIDDLE_DIV        = '".$middleDiv."'";
        $query .= "   AND SMALL_DIV         = '".$smallDiv."'";

        return $query;
    }

    //共通処理
    public function insertQuery($db, $model, $setData, $largeDiv, $middleDiv, $smallDiv, $setName)
    {
        //データチェック
        $getCount = $db->getOne(knje432Query::upCheckQuery($model, $setData, $largeDiv, $middleDiv, $smallDiv));

        $data = array(); //初期化
        $data["COUNT"][NUMBER]              = $setData[$setName];
        $data["REGISTERCD"][TEXT]           = STAFFCD;
        $data["UPDATED"][NUMBER]            =" sysdate()";
        if ($getCount == 0) {
            $data["EDBOARD_SCHOOLCD"][TEXT]     = $model->schoolcd;
            $data["YEAR"][TEXT]                 = $model->field["YEAR"];
            $data["COURSECD"][TEXT]             = $setData["COURSECD"];
            $data["MAJORCD"][TEXT]              = $setData["MAJORCD"];
            $data["SEX"][TEXT]                  = $setData["SEX"];
            $data["LARGE_DIV"][TEXT]            = $largeDiv;
            $data["MIDDLE_DIV"][TEXT]           = $middleDiv;
            $data["SMALL_DIV"][TEXT]            = $smallDiv;
            $query  = Query::insertSQL($data, "AFT_DISEASE_ADDITION432_DAT");
            $db->query($query);
        } else {
            $where  = " WHERE EDBOARD_SCHOOLCD  = '".$model->schoolcd."'";
            $where .= "   AND YEAR              = '".$model->field["YEAR"]."'";
            $where .= "   AND COURSECD          = '".$setData["COURSECD"]."'";
            $where .= "   AND MAJORCD           = '".$setData["MAJORCD"]."'";
            $where .= "   AND SEX               = '".$setData["SEX"]."'";
            $where .= "   AND LARGE_DIV         = '".$largeDiv."'";
            $where .= "   AND MIDDLE_DIV        = '".$middleDiv."'";
            $where .= "   AND SMALL_DIV         = '".$smallDiv."'";
            $query  = Query::updateSQL($data, "AFT_DISEASE_ADDITION432_DAT", $where);
            $db->query($query);
        }

        return;
    }

    //CSVファイルよりDBへインサート
    public function insertQueryCsv($db, $model, $data_arr)
    {
        $cnt = 0;    //処理件数
        for ($i = 0; $i < get_count($data_arr); $i++) {
            /*各県内、県外の人数*/
            $setCnt = 1;
            for ($industryCnt = 1; $industryCnt <= (get_count($model->industryData) - 1); $industryCnt++) {
                $setDiv = $industryCnt < 10 ? "0".$industryCnt : $industryCnt;
                for ($set2 = 1; $set2 <= 2; $set2++) {
                    knje432Query::insertQuery($db, $model, $data_arr[$i], $setDiv, "99", "00".$set2, "COUNT".$setCnt);
                    $setCnt++;
                }
            }

            /*県内、県外の合計人数*/
            for ($totalCnt = 1; $totalCnt <= 2; $totalCnt++) {
                if ($totalCnt == "1") {
                    $setCount = "98";
                } else {
                    $setCount = "99";
                }
                knje432Query::insertQuery($db, $model, $data_arr[$i], "99", "99", "00".$totalCnt, "COUNT".$setCount);
            }
            $cnt++;
        }
        return $cnt;
    }

    //エラーＤＢへの追加
    public function insertQueryErr(&$db, $record_no, $check_error)
    {
        $data1["PROGRAMID"][TEXT] = PROGRAMID;
        $data1["MSGROW"][NUMBER]  = $record_no;
        $data1["MSGREMARK"][TEXT] = $check_error;

        $query = Query::insertSQL($data1, "W_CSVMSG_PRG_DAT");
        $result = $db->query($query);
    }
}
