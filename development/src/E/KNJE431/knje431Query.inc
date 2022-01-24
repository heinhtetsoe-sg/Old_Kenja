<?php

require_once('for_php7.php');
class knje431Query extends Query
{

    //教育委員会用の学校コード取得
    public function getSchoolMst($model)
    {
        $query  = " SELECT ";
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
        $query .= "     AND T1.EDBOARD_SCHOOLCD = '".$model->schoolcd."' ";
        $query .= "     AND L1.REQUEST_ANSWER_PRG = 'KNJE431' ";
        $query .= "     AND L1.SUBMISSION_DATE IS NOT NULL ";
        $query .= " ORDER BY ";
        $query .= "     T1.DOC_NUMBER ";

        return $query;
    }

    //都道府県
    public function getPref()
    {
        $query  = " WITH PREF (PREF_CD, PREF_NAME) AS ( ";
        $query .= "     SELECT ";
        $query .= "         PREF_CD, ";
        $query .= "         PREF_NAME ";
        $query .= "     FROM ";
        $query .= "         PREF_MST ";
        $query .= "     UNION ";
        $query .= "         VALUES('98', 'その他') ";
        $query .= "     UNION ";
        $query .= "         VALUES('99', '計') ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     PREF ";
        $query .= " ORDER BY ";
        $query .= "     PREF_CD ";

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
            for ($i = 1; $i <= 47; $i++) {
                $setPref = $i < 10 ? "0".$i : $i;
                $query .= "         SUM(CASE WHEN VALUE(L1.PREF_CD, '99') = '{$setPref}' THEN 1 ELSE 0 END) AS COUNT{$i}, ";
            }
            $query .= "         SUM(CASE WHEN VALUE(L1.PREF_CD, '99') > '47' THEN 1 ELSE 0 END) AS COUNT98, ";
            $query .= "         COUNT(*) AS COUNT99 ";
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
            for ($i = 1; $i <= 47; $i++) {
                $query .= "     L{$i}.COUNT AS COUNT{$i}, ";
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
            $query .= "         AFT_DISEASE_ADDITION431_DAT ";
            $query .= "     WHERE ";
            $query .= "         YEAR = '".$model->field["YEAR"]."' ";
            $query .= "     AND EDBOARD_SCHOOLCD = '".$model->schoolcd."' ";
            $query .= "     GROUP BY ";
            $query .= "         EDBOARD_SCHOOLCD, ";
            $query .= "         YEAR, ";
            $query .= "         COURSECD, ";
            $query .= "         MAJORCD, ";
            $query .= "         SEX ";
            $query .= "     ) T1 ";
            for ($i = 1; $i <= 47; $i++) {
                $setDiv = $i < 10 ? "0".$i : $i;
                $query .= "     LEFT JOIN AFT_DISEASE_ADDITION431_DAT L{$i} ON T1.EDBOARD_SCHOOLCD = L{$i}.EDBOARD_SCHOOLCD ";
                $query .= "          AND T1.YEAR = L{$i}.YEAR ";
                $query .= "          AND T1.COURSECD = L{$i}.COURSECD ";
                $query .= "          AND T1.MAJORCD = L{$i}.MAJORCD ";
                $query .= "          AND T1.SEX = L{$i}.SEX ";
                $query .= "          AND L{$i}.LARGE_DIV = '{$setDiv}' ";
            }
            $query .= "     LEFT JOIN AFT_DISEASE_ADDITION431_DAT L98 ON T1.EDBOARD_SCHOOLCD = L98.EDBOARD_SCHOOLCD ";
            $query .= "          AND T1.YEAR = L98.YEAR ";
            $query .= "          AND T1.COURSECD = L98.COURSECD ";
            $query .= "          AND T1.MAJORCD = L98.MAJORCD ";
            $query .= "          AND T1.SEX = L98.SEX ";
            $query .= "          AND L98.LARGE_DIV = '98' ";
            $query .= "     LEFT JOIN AFT_DISEASE_ADDITION431_DAT L99 ON T1.EDBOARD_SCHOOLCD = L99.EDBOARD_SCHOOLCD ";
            $query .= "          AND T1.YEAR = L99.YEAR ";
            $query .= "          AND T1.COURSECD = L99.COURSECD ";
            $query .= "          AND T1.MAJORCD = L99.MAJORCD ";
            $query .= "          AND T1.SEX = L99.SEX ";
            $query .= "          AND L99.LARGE_DIV = '99' ";
            $query .= "     ) ";
        }

        //メイン
        $query .= " SELECT ";
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
        for ($i = 1; $i <= 47; $i++) {
            $query .= "     L1.COUNT{$i} AS COUNT{$i}, ";
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
        $query  = "DELETE FROM AFT_DISEASE_ADDITION431_DAT ";
        $query .= " WHERE EDBOARD_SCHOOLCD  = '".$model->schoolcd."'";
        $query .= "   AND YEAR              = '".$model->field["YEAR"]."'";
        $db->query($query);

        //INSERT
        for ($i = 0; $i < get_count($model->fields["CODE"]); $i++) {
            //学年・性別
            $code_arr = explode("-", $model->fields["CODE"][$i]);

            $totalVal = 0;
            for ($prefCnt = 1; $prefCnt <= 47; $prefCnt++) {
                $data = array(); //初期化
                $setDiv = $prefCnt < 10 ? "0".$prefCnt : $prefCnt;
                $data["EDBOARD_SCHOOLCD"][TEXT]     = $model->schoolcd;
                $data["YEAR"][TEXT]                 = $model->field["YEAR"];
                $data["COURSECD"][TEXT]             = $code_arr[0];
                $data["MAJORCD"][TEXT]              = $code_arr[1];
                $data["LARGE_DIV"][TEXT]            = $setDiv;
                $data["MIDDLE_DIV"][TEXT]           = "99";
                $data["SMALL_DIV"][TEXT]            = "999";
                $data["SEX"][TEXT]                  = $code_arr[2];
                //人数
                $data["COUNT"][NUMBER]              = $model->fields["COUNT".$prefCnt][$i];

                $data["REGISTERCD"][TEXT]           = STAFFCD;
                $data["UPDATED"][FUNC]              = " sysdate()";

                $totalVal += $model->fields["COUNT".$prefCnt][$i];
                $query = Query::insertSQL($data, "AFT_DISEASE_ADDITION431_DAT");
                $db->query($query);
            }

            for ($prefCnt = 98; $prefCnt <= 99; $prefCnt++) {
                $data = array(); //初期化
                $setDiv = $prefCnt;
                $data["EDBOARD_SCHOOLCD"][TEXT]     = $model->schoolcd;
                $data["YEAR"][TEXT]                 = $model->field["YEAR"];
                $data["COURSECD"][TEXT]             = $code_arr[0];
                $data["MAJORCD"][TEXT]              = $code_arr[1];
                $data["LARGE_DIV"][TEXT]            = $setDiv;
                $data["MIDDLE_DIV"][TEXT]           = "99";
                $data["SMALL_DIV"][TEXT]            = "999";
                $data["SEX"][TEXT]                  = $code_arr[2];
                //人数
                $data["COUNT"][NUMBER]              = $prefCnt == "98" ? $model->fields["COUNT".$prefCnt][$i] : $totalVal;

                $data["REGISTERCD"][TEXT]           = STAFFCD;
                $data["UPDATED"][FUNC]              = " sysdate()";

                $totalVal += $model->fields["COUNT".$prefCnt][$i];
                $query = Query::insertSQL($data, "AFT_DISEASE_ADDITION431_DAT");
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
        $query .= "     REPORT_AFT_DISEASE_ADDITION431_DAT T1";
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
    public function getAddition431Dat($model)
    {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     AFT_DISEASE_ADDITION431_DAT ";
        $query .= " WHERE ";
        $query .= "     EDBOARD_SCHOOLCD = '".$model->schoolcd."' ";
        $query .= "     AND YEAR = '".$model->field["YEAR"]."' ";

        return $query;
    }

    public function getRepAddition431Dat($model)
    {
        $query  = " SELECT DISTINCT";
        $query .= "     EDBOARD_SCHOOLCD, ";
        $query .= "     YEAR, ";
        $query .= "     COURSECD, ";
        $query .= "     MAJORCD ";
        $query .= " FROM ";
        $query .= "     AFT_DISEASE_ADDITION431_DAT ";
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
        $query  = "DELETE FROM AFT_DISEASE_ADDITION431_DAT ";
        $query .= " WHERE EDBOARD_SCHOOLCD  = '".$model->schoolcd."'";
        $query .= "   AND YEAR              = '".$model->field["YEAR"]."'";
        $db2->query($query);
        //県側、学校側・報告テーブル
        $query  = "DELETE FROM REPORT_AFT_DISEASE_ADDITION431_DAT ";
        $query .= " WHERE EDBOARD_SCHOOLCD  = '".$model->schoolcd."'";
        $query .= "   AND YEAR              = '".$model->field["YEAR"]."'";
        $db->query($query);
        $db2->query($query);

        //INSERT
        //学校側から県側へコピーするデータ取得（就職進路状況）
        $query = knje431Query::getAddition431Dat($model);
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
            $query  = Query::insertSQL($data, "AFT_DISEASE_ADDITION431_DAT");
            $db2->query($query);
        }
        $result->free();

        //報告用データ取得
        $query = knje431Query::getRepAddition431Dat($model);
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
            $query  = Query::insertSQL($data, "REPORT_AFT_DISEASE_ADDITION431_DAT");

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
        $query .= "     AFT_DISEASE_ADDITION431_DAT ";
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
        $getCount = $db->getOne(knje431Query::upCheckQuery($model, $setData, $largeDiv, $middleDiv, $smallDiv));
        
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
            $query  = Query::insertSQL($data, "AFT_DISEASE_ADDITION431_DAT");
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
            $query  = Query::updateSQL($data, "AFT_DISEASE_ADDITION431_DAT", $where);
            $db->query($query);
        }
        
        return;
    }

    //CSVファイルよりDBへインサート
    public function insertQueryCsv($db, $model, $data_arr)
    {
        $cnt = 0;    //処理件数
        for ($i = 0; $i < get_count($data_arr); $i++) {
            /*各都道府県の人数*/
            for ($prefCnt = 1; $prefCnt <= 47; $prefCnt++) {
                $setDiv = $prefCnt < 10 ? "0".$prefCnt : $prefCnt;
                knje431Query::insertQuery($db, $model, $data_arr[$i], $setDiv, "99", "999", "COUNT".$prefCnt);
            }

            /*その他、合計人数*/
            for ($prefCnt = 98; $prefCnt <= 99; $prefCnt++) {
                $setDiv = $prefCnt;
                knje431Query::insertQuery($db, $model, $data_arr[$i], $setDiv, "99", "999", "COUNT".$prefCnt);
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
