<?php

require_once('for_php7.php');

class knjl343wquery extends Query {
    //学校情報取得
    function getSchoolInfo($model) {
        $query  = " SELECT ";
        $query .= "     T1.KYOUIKU_IINKAI_SCHOOLCD, ";
        $query .= "     T2.FINSCHOOL_NAME ";
        $query .= " FROM ";
        $query .= "     V_SCHOOL_MST T1, ";
        $query .= "     FINSCHOOL_MST T2 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".CTRL_YEAR."' AND ";
        $query .= "     T1.KYOUIKU_IINKAI_SCHOOLCD = T2.FINSCHOOLCD ";
        if ($model->Properties["useSchool_KindField"] == "1" && SCHOOLKIND != "") {
            $query .= " AND T1.SCHOOLCD     = '".sprintf("%012d", SCHOOLCD)."' ";
            $query .= " AND T1.SCHOOL_KIND  = '".SCHOOLKIND."' ";
        }

        return $query;
    }

    //課程名取得
    function getCourseName($model) {
        $query  = " SELECT ";
        $query .= "     COURSENAME ";
        $query .= " FROM ";
        $query .= "     COURSE_MST ";
        $query .= " WHERE ";
        $query .= "     COURSECD IN (SELECT ";
        $query .= "                     MIN(COURSECD) AS COURSECD ";
        $query .= "                 FROM ";
        $query .= "                     V_COURSE_MST ";
        $query .= "                 WHERE ";
        $query .= "                     YEAR = '".CTRL_YEAR."' ";
        $query .= "                 ) ";

        return $query;
    }

    //CSVデータ出力
    function selectCsvQuery($model) {
        $query  = " WITH MAIN AS ( ";
        $query .= " SELECT ";
        $query .= "     T1.TESTDIV, ";
        $query .= "     T1.TESTDIV2, ";
        $query .= "     VALUE(T1.TESTDIV2, '0') || T1.LAST_DAI1_COURSECD || T1.LAST_DAI1_MAJORCD || T1.LAST_DAI1_COURSECODE AS KAI_PAGE_KEY, ";
        $query .= "     CASE WHEN T1.TESTDIV2 = '1' ";
        $query .= "          THEN N2.NAME1 || '追検査' ";
        $query .= "          ELSE N2.NAME1 ";
        $query .= "     END AS TESTDIV_NAME, ";
        $query .= "     M1.MAJORNAME || C1.EXAMCOURSE_NAME AS DAI1_NAME, ";
        $query .= "     T1.EXAMNO, ";
        $query .= "     FIN.FINSCHOOL_NAME, ";
        $query .= "     N1.ABBV1 AS SEX_NAME, ";
        //得点
        for ($i = 1; $i <= 6; $i++) {
            $query .= "     S{$i}.SCORE AS SCORE{$i}, ";
        }
        $query .= "     ST.TOTAL AS SCORET, ";
        //評定
        for ($i = 1; $i <= 9; $i++) {
            $num = sprintf("%02d", $i);
            $query .= "     CF0.CONFIDENTIAL_RPT{$num} AS CONF3_RPT{$num}, ";
        }
        $query .= "     CF0.TOTAL_ALL AS CONF3_TOTAL, ";
        //調査書諸記録の100%
        $query .= "     CASE WHEN VALUE(S100.SCORE, 0) >= 1 ";
        $query .= "          THEN '○' ";
        $query .= "          ELSE '' ";
        $query .= "     END AS CONF3_TOTAL100, ";
        //学力検査等得点合計の80%
        $query .= "     CASE WHEN VALUE(S80.SCORE, 0) >= 1 ";
        $query .= "          THEN '○' ";
        $query .= "          ELSE '' ";
        $query .= "     END AS SCORET80, ";
        //合否
        $query .= "     CASE WHEN T1.TESTDIV = '4' AND T1.JUDGEMENT = '3' THEN '否' "; //スポーツ特別選抜で不合格、前期選抜で合格
        $query .= "          WHEN N3.NAMESPARE1 = '1' THEN '合' ";
        $query .= "          WHEN T1.JUDGEMENT = '2' THEN '否' ";
        $query .= "          ELSE '' ";
        $query .= "     END AS JUDGE, ";
        //第２志望合格者（合格学科名）
        $query .= "     CASE WHEN T1.LAST_DAI2_COURSECD || T1.LAST_DAI2_MAJORCD || T1.LAST_DAI2_COURSECODE = T1.SUC_COURSECD || T1.SUC_MAJORCD || T1.SUC_COURSECODE ";
        $query .= "          THEN M2.MAJORNAME ";
        $query .= "          ELSE '' ";
        $query .= "     END AS DAI2_SUC_MAJOR_NAME, ";
        //１年の評定合計
        //２年の評定合計
        $query .= "     CF1.REMARK12 AS CONF1_TOTAL, ";
        $query .= "     CF2.REMARK12 AS CONF2_TOTAL, ";
        //校外願変
        $query .= "     T1.JUDGEMENT, ";
        //別紙様式３の備考
        $query .= "     BD032.REMARK1 AS YOUSIKI3_REMARK, ";
        //各内容をつなげて備考にセット
        $query .= "     CASE WHEN T1.FS_GRDDIV='2' ";
        $query .= "          THEN '1' ";
        $query .= "          ELSE '' ";
        $query .= "     END AS REMARK_SOTUGYOU, ";
        $query .= "     CASE WHEN T1.ENTDIV='2' ";
        $query .= "          THEN '1' ";
        $query .= "          ELSE '' ";
        $query .= "     END AS REMARK_JITAI, ";
        $query .= "     CASE WHEN T1.KAIGAI_KIKOKUSEI_NADO='1' ";
        $query .= "          THEN '1' ";
        $query .= "          ELSE '' ";
        $query .= "     END AS REMARK_TOKU_WAKU, ";
        $query .= "     CASE WHEN T1.BIRTHDAY IS NULL ";
        $query .= "          THEN '' ";
        $query .= "          WHEN YEAR('".$model->ObjYear."-04-01' - T1.BIRTHDAY) >= 20 ";
        $query .= "          THEN '1' ";
        $query .= "          ELSE '' ";
        $query .= "     END AS REMARK_OVER_AGE, ";
        $query .= "     CASE WHEN T1.JUDGEMENT = '4' ";
        $query .= "          THEN '1' ";
        $query .= "          ELSE '' ";
        $query .= "     END AS REMARK_KESSEKI ";
        $query .= " FROM ";
        $query .= "     V_ENTEXAM_APPLICANTBASE_DAT T1 ";
        $query .= "     LEFT JOIN FINSCHOOL_MST FIN ON FIN.FINSCHOOLCD = T1.FS_CD ";
        $query .= "     LEFT JOIN NAME_MST N1 ON N1.NAMECD1 = 'Z002' AND N1.NAMECD2 = T1.SEX ";
        $query .= "     LEFT JOIN NAME_MST N2 ON N2.NAMECD1 = 'L004' AND N2.NAMECD2 = T1.TESTDIV ";
        $query .= "     LEFT JOIN NAME_MST N3 ON N3.NAMECD1 = 'L013' AND N3.NAMECD2 = T1.JUDGEMENT ";
        $query .= "     LEFT JOIN MAJOR_MST M1 ";
        $query .= "          ON M1.COURSECD = T1.LAST_DAI1_COURSECD ";
        $query .= "         AND M1.MAJORCD = T1.LAST_DAI1_MAJORCD ";
        $query .= "     LEFT JOIN MAJOR_MST M2 ";
        $query .= "          ON M2.COURSECD = T1.SUC_COURSECD ";
        $query .= "         AND M2.MAJORCD = T1.SUC_MAJORCD ";
        $query .= "     LEFT JOIN ENTEXAM_COURSE_MST C1 ";
        $query .= "          ON C1.ENTEXAMYEAR = T1.ENTEXAMYEAR ";
        $query .= "         AND C1.APPLICANTDIV = T1.APPLICANTDIV ";
        $query .= "         AND C1.TESTDIV = T1.TESTDIV ";
        $query .= "         AND C1.COURSECD = T1.LAST_DAI1_COURSECD ";
        $query .= "         AND C1.MAJORCD = T1.LAST_DAI1_MAJORCD ";
        $query .= "         AND C1.EXAMCOURSECD = T1.LAST_DAI1_COURSECODE ";
        for ($i = 1; $i <= 6; $i++) {
            $query .= "     LEFT JOIN ENTEXAM_SCORE_DAT S{$i} ";
            $query .= "          ON S{$i}.ENTEXAMYEAR = T1.ENTEXAMYEAR ";
            $query .= "         AND S{$i}.APPLICANTDIV = T1.APPLICANTDIV ";
            $query .= "         AND S{$i}.TESTDIV = T1.TESTDIV ";
            $query .= "         AND S{$i}.RECEPTNO = T1.EXAMNO ";
            $query .= "         AND S{$i}.TESTSUBCLASSCD = '{$i}' ";
        }

        $query .= "     LEFT JOIN ENTEXAM_SCORE_DAT S100 ";
        $query .= "          ON S100.ENTEXAMYEAR = T1.ENTEXAMYEAR ";
        $query .= "         AND S100.APPLICANTDIV = T1.APPLICANTDIV ";
        $query .= "         AND S100.TESTDIV = T1.TESTDIV ";
        $query .= "         AND S100.RECEPTNO = T1.EXAMNO ";
        $query .= "         AND S100.TESTSUBCLASSCD = '7' ";

        $query .= "     LEFT JOIN ENTEXAM_SCORE_DAT S80 ";
        $query .= "          ON S80.ENTEXAMYEAR = T1.ENTEXAMYEAR ";
        $query .= "         AND S80.APPLICANTDIV = T1.APPLICANTDIV ";
        $query .= "         AND S80.TESTDIV = T1.TESTDIV ";
        $query .= "         AND S80.RECEPTNO = T1.EXAMNO ";
        $query .= "         AND S80.TESTSUBCLASSCD = '8' ";

        $query .= "     LEFT JOIN ( ";
        $query .= "         SELECT ";
        $query .= "             ENTEXAMYEAR, ";
        $query .= "             APPLICANTDIV, ";
        $query .= "             TESTDIV, ";
        $query .= "             RECEPTNO, ";
        $query .= "             SUM(SCORE) AS TOTAL ";
        $query .= "         FROM ";
        $query .= "             ENTEXAM_SCORE_DAT ";
        $query .= "         WHERE ";
        $query .= "             ENTEXAMYEAR = '".$model->ObjYear."' ";
        $query .= "             AND TESTSUBCLASSCD <= '6' ";
        $query .= "         GROUP BY ";
        $query .= "             ENTEXAMYEAR, ";
        $query .= "             APPLICANTDIV, ";
        $query .= "             TESTDIV, ";
        $query .= "             RECEPTNO ";
        $query .= "     ) ST ON ST.ENTEXAMYEAR = T1.ENTEXAMYEAR ";
        $query .= "         AND ST.APPLICANTDIV = T1.APPLICANTDIV ";
        $query .= "         AND ST.TESTDIV = T1.TESTDIV ";
        $query .= "         AND ST.RECEPTNO = T1.EXAMNO ";
        $query .= "     LEFT JOIN ENTEXAM_APPLICANTCONFRPT_DAT CF0 ";
        $query .= "          ON CF0.ENTEXAMYEAR = T1.ENTEXAMYEAR ";
        $query .= "         AND CF0.APPLICANTDIV = T1.APPLICANTDIV ";
        $query .= "         AND CF0.EXAMNO = T1.EXAMNO ";
        $query .= "     LEFT JOIN ENTEXAM_APPLICANTCONFRPT_DETAIL_DAT CF1 ";
        $query .= "          ON CF1.ENTEXAMYEAR = T1.ENTEXAMYEAR ";
        $query .= "         AND CF1.APPLICANTDIV = T1.APPLICANTDIV ";
        $query .= "         AND CF1.EXAMNO = T1.EXAMNO ";
        $query .= "         AND CF1.SEQ = '001' ";
        $query .= "     LEFT JOIN ENTEXAM_APPLICANTCONFRPT_DETAIL_DAT CF2 ";
        $query .= "          ON CF2.ENTEXAMYEAR = T1.ENTEXAMYEAR ";
        $query .= "         AND CF2.APPLICANTDIV = T1.APPLICANTDIV ";
        $query .= "         AND CF2.EXAMNO = T1.EXAMNO ";
        $query .= "         AND CF2.SEQ = '002' ";
        $query .= "     LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT BD032 ";
        $query .= "          ON BD032.ENTEXAMYEAR = T1.ENTEXAMYEAR ";
        $query .= "         AND BD032.APPLICANTDIV = T1.APPLICANTDIV ";
        $query .= "         AND BD032.EXAMNO = T1.EXAMNO ";
        $query .= "         AND BD032.SEQ = '032' ";
        $query .= " WHERE ";
        $query .= "     T1.ENTEXAMYEAR               = '".$model->ObjYear."' ";
        $query .= " ) ";

        //MAIN
        $query .= " SELECT ";
        $query .= "     T1.TESTDIV || T1.KAI_PAGE_KEY AS KAI_PAGE_KEY, ";
        $query .= "     T1.TESTDIV_NAME, ";
        $query .= "     T1.DAI1_NAME, ";
        $query .= "     T1.EXAMNO, ";
        $query .= "     T1.FINSCHOOL_NAME, ";
        $query .= "     T1.SEX_NAME, ";
        //得点
        for ($i = 1; $i <= 6; $i++) {
            $query .= "     T1.SCORE{$i}, ";
        }
        $query .= "     T1.SCORET, ";
        //評定
        for ($i = 1; $i <= 9; $i++) {
            $num = sprintf("%02d", $i);
            $query .= "     T1.CONF3_RPT{$num}, ";
        }
        $query .= "     T1.CONF3_TOTAL, ";
        //調査書諸記録の100%
        $query .= "     T1.CONF3_TOTAL100, ";
        //学力検査等得点合計の80%
        $query .= "     T1.SCORET80, ";
        //合否
        $query .= "     T1.JUDGE, ";
        //第２志望合格者（合格学科名）
        $query .= "     T1.DAI2_SUC_MAJOR_NAME, ";
        //１年の評定合計
        //２年の評定合計
        $query .= "     T1.CONF1_TOTAL, ";
        $query .= "     T1.CONF2_TOTAL, ";
        //校外願変
        $query .= "     T1.JUDGEMENT, ";
        //別紙様式３の備考
        $query .= "     T1.YOUSIKI3_REMARK, ";
        //各内容をつなげて備考にセット
        $query .= "     T1.REMARK_SOTUGYOU, ";
        $query .= "     T1.REMARK_JITAI, ";
        $query .= "     T1.REMARK_TOKU_WAKU, ";
        $query .= "     T1.REMARK_OVER_AGE, ";
        $query .= "     T1.REMARK_KESSEKI ";
        $query .= " FROM ";
        $query .= "     MAIN T1 ";

        //スポーツ特別選抜（TESTDIV='4'）の受検者で、前期選抜合格者（JUDGEMENT='3'）と不合格者（JUDGEMENT='2'）は、前期選抜（TESTDIV='1'）にも印字する
        $query .= " UNION ALL ";
        $query .= " SELECT ";
        $query .= "     '1' || T1.KAI_PAGE_KEY AS KAI_PAGE_KEY, ";
        $query .= "     CASE WHEN T1.TESTDIV2 = '1' ";
        $query .= "          THEN N2.NAME1 || '追検査' ";
        $query .= "          ELSE N2.NAME1 ";
        $query .= "     END AS TESTDIV_NAME, ";
        $query .= "     T1.DAI1_NAME, ";
        $query .= "     T1.EXAMNO, ";
        $query .= "     T1.FINSCHOOL_NAME, ";
        $query .= "     T1.SEX_NAME, ";
        //得点
        for ($i = 1; $i <= 6; $i++) {
            $query .= "     T1.SCORE{$i}, ";
        }
        $query .= "     T1.SCORET, ";
        //評定
        for ($i = 1; $i <= 9; $i++) {
            $num = sprintf("%02d", $i);
            $query .= "     T1.CONF3_RPT{$num}, ";
        }
        $query .= "     T1.CONF3_TOTAL, ";
        //調査書諸記録の100%
        $query .= "     T1.CONF3_TOTAL100, ";
        //学力検査等得点合計の80%
        $query .= "     T1.SCORET80, ";
        //合否
        $query .= "     CASE WHEN T1.JUDGEMENT = '2' THEN '否' "; //スポーツ特別選抜で不合格、前期選抜で不合格
        $query .= "          WHEN T1.JUDGEMENT = '3' THEN '合' "; //スポーツ特別選抜で不合格、前期選抜で合格
        $query .= "          ELSE '' ";
        $query .= "     END AS JUDGE, ";
        //第２志望合格者（合格学科名）
        $query .= "     T1.DAI2_SUC_MAJOR_NAME, ";
        //１年の評定合計
        //２年の評定合計
        $query .= "     T1.CONF1_TOTAL, ";
        $query .= "     T1.CONF2_TOTAL, ";
        //校外願変
        $query .= "     T1.JUDGEMENT, ";
        //別紙様式３の備考
        $query .= "     T1.YOUSIKI3_REMARK, ";
        //各内容をつなげて備考にセット
        $query .= "     T1.REMARK_SOTUGYOU, ";
        $query .= "     T1.REMARK_JITAI, ";
        $query .= "     T1.REMARK_TOKU_WAKU, ";
        $query .= "     T1.REMARK_OVER_AGE, ";
        $query .= "     T1.REMARK_KESSEKI ";
        $query .= " FROM ";
        $query .= "     MAIN T1 ";
        $query .= "     LEFT JOIN NAME_MST N2 ON N2.NAMECD1 = 'L004' AND N2.NAMECD2 = '1' ";
        $query .= " WHERE ";
        $query .= "     T1.TESTDIV = '4' ";
        $query .= "     AND T1.JUDGEMENT IN ('2','3') ";

        $query .= " ORDER BY ";
        $query .= "     KAI_PAGE_KEY, ";
        $query .= "     EXAMNO ";

        return $query;
    }

    //県への報告取得
    function getReport($model) {
        $query  = " SELECT ";
        $query .= "     T1.EXECUTE_DATE, ";
        $query .= "     L1.STAFFNAME ";
        $query .= " FROM ";
        $query .= "     REPORT_ENTEXAM_L340W_DAT T1 ";
        $query .= "     LEFT JOIN STAFF_MST L1 ON T1.REGISTERCD = L1.STAFFCD ";
        $query .= " WHERE ";
        $query .= "     T1.EDBOARD_SCHOOLCD = '".$model->schoolcd."' AND ";
        $query .= "     T1.ENTEXAMYEAR      = '".$model->ObjYear."' AND ";
        $query .= "     T1.CSV_PRG          = 3 AND ";
        $query .= "     T1.CSVDIV           = 0 ";

        return $query;
    }

    //フィールド取得
    function getFieldName($table) {
        $query  = " SELECT ";
        $query .= "     COLUMN_NAME, ";
        $query .= "     DATA_TYPE ";
        $query .= " FROM ";
        $query .= "     SYSIBM.COLUMNS ";
        $query .= " WHERE ";
        $query .= "     TABLE_NAME = '".$table."' AND ";
        $query .= "     COLUMN_NAME NOT IN ('REGISTERCD', 'UPDATED') ";
        $query .= " ORDER BY ";
        $query .= "     ORDINAL_POSITION ";

        return $query;
    }

    //データ取得
    function getEntexamApplicantbaseDat($model) {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_APPLICANTBASE_DAT ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR = '".$model->ObjYear."' ";

        return $query;
    }

    //学校側・データ取得
    function getDataQuery($model, $table, $where="") {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .=       $table." T1 ";
        if ($where) {
            $query .= " WHERE ".$where;
        }

        return $query;
    }

    //県側・削除
    function getDeleteQuery($model, $table, $where="") {
        $query  = " DELETE FROM ";
        $query .= "     EDBOARD_".$table." T1 ";
        $query .= " WHERE ";
        $query .= "     EDBOARD_SCHOOLCD = '".$model->schoolcd."' ";
        if ($where) {
            $query .= " AND ".$where;
        }

        return $query;
    }

    //UPDATE
    function &getUpdateReport($model) {
        $db = Query::dbCheckOut();
        $db2 = Query::dbCheckOut2();
        $db2->autoCommit(false);

        /**********************************/
        /*  学校側から県側へデータコピー  */
        /**********************************/

        //対象テーブル一覧／条件
        $table_array = array();
        $table_array["COURSE_MST"]      = "";
        $table_array["COURSE_YDAT"]     = " YEAR IN ('".CTRL_YEAR."', '".$model->ObjYear."') ";
        $table_array["MAJOR_MST"]       = "";
        $table_array["ENTEXAM_APPLICANTBASE_DAT"]           = " ENTEXAMYEAR = '".$model->ObjYear."' ";
        $table_array["ENTEXAM_APPLICANTBASE_DETAIL_DAT"]    = " ENTEXAMYEAR = '".$model->ObjYear."' ";
        $table_array["ENTEXAM_APPLICANTCONFRPT_DAT"]        = " ENTEXAMYEAR = '".$model->ObjYear."' ";
        $table_array["ENTEXAM_APPLICANTCONFRPT_DETAIL_DAT"] = " ENTEXAMYEAR = '".$model->ObjYear."' ";

        foreach ($table_array as $table => $where) {
            //フィールド一覧取得
            $field_array = array();
            $query = knjl343wQuery::getFieldName($table);
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                if ($row["DATA_TYPE"] == "CHARACTER VARYING") {
                    $data_type = TEXT;
                } else if (in_array($row["DATA_TYPE"], array("INTEGER", "SMALLINT", "DECIMAL"))) {
                    $data_type = NUMBER;
                } else {
                    $data_type = $row["DATA_TYPE"];
                }
                $field_array[$row["COLUMN_NAME"]] = $data_type;
            }
            $result->free();

            //DELETE -- 県側
            $query = knjl343wQuery::getDeleteQuery($model, $table, $where);
            $db2->query($query);

            //INSERT -- 学校側から県側へコピー
            $query = knjl343wQuery::getDataQuery($model, $table, $where);
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $data = array(); //初期化
                $data["EDBOARD_SCHOOLCD"][TEXT]     = $model->schoolcd;
                foreach ($field_array as $field => $type) {
                    $data[$field][$type]            = $row[$field];
                }
                $data["REGISTERCD"][TEXT]           = STAFFCD;
                $data["UPDATED"][NUMBER]            = "sysdate()";
                $query  = Query::insertSQL($data, "EDBOARD_".$table);
                $db2->query($query);
            }
            $result->free();
        }

        /******************/
        /*  報告テーブル  */
        /******************/

        //DELETE -- 県側
        $query  = " DELETE FROM REPORT_ENTEXAM_L340W_DAT ";
        $query .= " WHERE EDBOARD_SCHOOLCD  = '".$model->schoolcd."' ";
        $query .= "   AND ENTEXAMYEAR       = '".$model->ObjYear."' ";
        $query .= "   AND CSV_PRG           = 3 ";
        $query .= "   AND CSVDIV            = 0 ";
        $db2->query($query);

        //INSERT -- 県側
        $data = array(); //初期化
        $data["EDBOARD_SCHOOLCD"][TEXT]     = $model->schoolcd;
        $data["ENTEXAMYEAR"][TEXT]          = $model->ObjYear;
        $data["CSV_PRG"][NUMBER]            = 3;
        $data["CSVDIV"][NUMBER]             = 0;
        $data["EXECUTE_DATE"][NUMBER]       = "sysdate()";
        $data["REGISTERCD"][TEXT]           = STAFFCD;
        $data["UPDATED"][NUMBER]            = "sysdate()";
        $query  = Query::insertSQL($data, "REPORT_ENTEXAM_L340W_DAT");
        $db2->query($query);

        $db2->commit();
        Query::dbCheckIn($db);
        Query::dbCheckIn($db2);
        return;
    }
}
?>
