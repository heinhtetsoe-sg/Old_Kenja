<?php

require_once('for_php7.php');

class knjl510fQuery extends Query 
{


    //MAX管理番号＋１を取得
    function getMaxExamnoQuery($model) {
        $db = Query::dbCheckOut();

        $query  = " SELECT ";
        $query .= "     MAX(EXAMNO) AS EXAMNO ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_KANRI_NO_DAT ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR = '".$model->year."' ";
        $query .= "     AND APPLICANTDIV = '1' ";//1:中学
        $maxExam = $db->getOne($query);

        if (!strlen($maxExam)) {
            $query  = " SELECT ";
            $query .= "     MAX(EXAMNO) AS EXAMNO ";
            $query .= " FROM ";
            $query .= "     ENTEXAM_APPLICANTBASE_DAT ";
            $query .= " WHERE ";
            $query .= "     ENTEXAMYEAR = '".$model->year."' ";
            $query .= "     AND APPLICANTDIV = '1' ";//1:中学
            $maxExam = $db->getOne($query);
        }

        if (!strlen($maxExam)) {
            $maxExam = "00001";
        } else {
            $maxExam++;
        }

        Query::dbCheckIn($db);
        return sprintf("%05d", $maxExam);
    }

    //MAX更新
    function insEntexamKanriNoDat($model, $maxExam) {
        $db = Query::dbCheckOut();

        $query  = " SELECT ";
        $query .= "     EXAMNO ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_KANRI_NO_DAT ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR = '".$model->year."' ";
        $query .= "     AND APPLICANTDIV = '1' ";//1:中学
        $tabExam = $db->getOne($query);
        if (strlen($tabExam)) {
            $query  = " UPDATE ENTEXAM_KANRI_NO_DAT SET (EXAMNO, REGISTERCD, UPDATED) = ('".$maxExam."', '".STAFFCD."', current timestamp)";
            $query .= " WHERE ";
            $query .= "     ENTEXAMYEAR = '".$model->year."' ";
            $query .= "     AND APPLICANTDIV = '1' ";//1:中学
        } else {
            $query = " INSERT INTO ENTEXAM_KANRI_NO_DAT VALUES('".$model->year."', '1', '".$maxExam."', '".STAFFCD."', current timestamp)";
        }
        $db->query($query);

        Query::dbCheckIn($db);
    }

    //入試区分配列取得
    function getTestdivArray($model) {
        $db = Query::dbCheckOut();

        $testdivArray = array();
        $query = knjl510fQuery::get_name_cd($model->year, "L024");
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $testdivArray[] = array("TESTDIV" => $row["NAMECD2"], "TESTDIV_NAME" => $row["NAME1"], "TESTDIV_DATE" => $row["NAMESPARE1"], "KIKOKU_FLG" => $row["ABBV2"]);
        }
        $result->free();

        Query::dbCheckIn($db);
        return $testdivArray;
    }

    //名称マスタよりデータを取得
    function get_name_cd($year, $cd, $sitei = "")
    {
        $query  = " SELECT ";
        $query .= "     NAMECD2 || ':' || NAME1 AS LABEL, ";
        $query .= "     NAMECD2 AS VALUE, ";
        $query .= "     NAMECD2, ";
        $query .= "     NAME1, ";
        $query .= "     NAME2, ";
        $query .= "     ABBV1, ";
        $query .= "     ABBV2, ";
        $query .= "     NAMESPARE1, ";
        $query .= "     NAMESPARE2 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$year."' ";
        $query .= "     AND NAMECD1 = '".$cd."' ";
        if ($sitei) {
            $query .= "     AND NAMECD2 = '".$sitei."' ";
        }
        $query .= " ORDER BY ";
        if ($cd == "L024") {
            $query .= "     INT(VALUE) ";
        } else {
            $query .= "     VALUE ";
        }

        return $query;
    }

    //英語見なし得点
    function getEiken($year, $applicantdiv) {
        $query  = "";
        $query .= " SELECT ";
        $query .= "     NAMECD2 || ':' || NAME1 AS LABEL, ";
        $query .= "     NAMECD2 AS VALUE ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "         YEAR = '{$year}' ";
        $query .= "     AND NAMECD1 = 'L055' ";
        $query .= "     AND NAMESPARE1 = '{$applicantdiv}' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //奨学区分
    function getShougaku($year, $applicantdiv) {
        $query  = "";
        $query .= " SELECT ";
        $query .= "     NAMECD2 || ':' || NAME1 AS LABEL, ";
        $query .= "     NAMECD2 AS VALUE ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "         YEAR = '{$year}' ";
        $query .= "     AND NAMECD1 = 'L025' ";
        $query .= "     AND NAMESPARE1 = '{$applicantdiv}' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //受験型
    function getExamType($year, $applicantdiv, $testdiv, $cntFlg = "") {
        $query  = "";
        $query .= " SELECT ";
        if ($cntFlg == "CNT") {
            $query .= "     COUNT(*) AS CNT ";
        } else {
            $query .= "     NAMECD2 || ':' || NAME1 AS LABEL, ";
            $query .= "     NAMECD2 AS VALUE ";
        }
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "         YEAR = '{$year}' ";
        $query .= "     AND NAMECD1 = 'L005' ";
        $query .= "     AND NAMECD2 IN ( ";
        $query .= "         SELECT ";
        $query .= "             MEMO_KISO ";
        $query .= "         FROM ";
        $query .= "             ENTEXAM_NO_DAT ";
        $query .= "         WHERE ";
        $query .= "                 ENTEXAMYEAR  = '{$year}' ";
        $query .= "             AND APPLICANTDIV = '{$applicantdiv}' ";
        $query .= "             AND TESTDIV      = '{$testdiv}' ";
        $query .= "         GROUP BY ";
        $query .= "             MEMO_KISO ";
        $query .= "     ) ";
        return $query;
    }

    //志望コース
    function getExamcourse($year, $applicantdiv, $testdiv, $examcoursecd = "") {
        $query  = "";
        $query .= " SELECT ";
        $query .= "     T1.COURSECD || '-' || T1.MAJORCD || '-' || T1.EXAMCOURSECD AS VALUE, ";
        $query .= "     T1.EXAMCOURSECD || ':' || T1.EXAMCOURSE_NAME AS LABEL ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_COURSE_MST T1 ";
        $query .= " WHERE ";
        $query .= "         T1.ENTEXAMYEAR  = '{$year}' ";
        $query .= "     AND T1.APPLICANTDIV = '{$applicantdiv}' ";
        $query .= "     AND T1.TESTDIV      = '1' ";//固定
        if (strlen($examcoursecd)) {
            $query .= "     AND T1.EXAMCOURSECD = '{$examcoursecd}' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";
        return $query;
    }

    //名称マスタより和暦の元号を取得
    function get_calendarno($year, $namecd2 = "") {
        $query  = " SELECT ";
        $query .= "     NAMECD2, ";
        $query .= "     NAME1, ";
        $query .= "     NAMESPARE1, ";
        $query .= "     NAMESPARE2, ";
        $query .= "     NAMESPARE3 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$year."' ";
        $query .= "     AND NAMECD1 = 'L007' ";
        if (strlen($namecd2)) {
            $query .= "     AND NAMECD2 = '".$namecd2."' ";
        }

        return $query;
    }

    //メインデータ取得
    function get_edit_data($model)
    {
        $db = Query::dbCheckOut();

        $query  = "";
        $query .= " SELECT ";
        $query .= "     T1.APPLICANTDIV, ";
        $query .= "     T1.EXAMNO, ";
        //010:入試区分,011:受験型,012:受験番号
        foreach ($model->testdivArray as $key => $codeArray) {
            $testdiv = $codeArray["TESTDIV"];
            $query .= "     BASE_D_010.REMARK{$testdiv} AS TESTDIV{$testdiv}, ";
            $query .= "     BASE_D_011.REMARK{$testdiv} AS EXAM_TYPE{$testdiv}, ";
            $query .= "     BASE_D_012.REMARK{$testdiv} AS RECEPTNO{$testdiv}, ";
        }
        $query .= "     T1.RECEPTDATE, ";
        $query .= "     T1.SHDIV, ";
        $query .= "     BASE_D_001.REMARK8 || '-' || BASE_D_001.REMARK9 || '-' || BASE_D_001.REMARK10 AS EXAMCOURSE, ";
        $query .= "     BASE_D_017.REMARK1 || '-' || BASE_D_017.REMARK2 || '-' || BASE_D_017.REMARK3 AS EXAMCOURSE2, ";
        $query .= "     T1.NAME, ";
        $query .= "     T1.NAME_KANA, ";
        $query .= "     T1.SEX, ";
        $query .= "     T1.BIRTHDAY, ";
        $query .= "     T1.FS_CD, ";
        $query .= "     FIN.FINSCHOOL_NAME AS FS_NAME, ";
        $query .= "     T1.FS_GRDYEAR, ";
        $query .= "     T1.FS_Y, ";
        $query .= "     T1.FS_M, ";
        $query .= "     T1.FS_GRDDIV, ";
        $query .= "     T1.FS_DAY, ";
        $query .= "     T2.ZIPCD, ";
        $query .= "     T2.ADDRESS1, ";
        $query .= "     T2.ADDRESS2, ";
        $query .= "     T2.TELNO, ";
        $query .= "     T2.GNAME, ";
        $query .= "     T2.GKANA, ";
        $query .= "     T2.RELATIONSHIP, ";
        $query .= "     T2.GZIPCD, ";
        $query .= "     T2.GADDRESS1, ";
        $query .= "     T2.GADDRESS2, ";
        $query .= "     T2.GTELNO, ";
        $query .= "     T3.NAME1, ";
        for ($kai = 1; $kai <= 2; $kai++) {
            $seq = ($kai == 1) ? "038" : "039";
            $query .= "     BASE_D_{$seq}.REMARK1 AS PRE_KOKUGO{$kai}, ";
            $query .= "     BASE_D_{$seq}.REMARK2 AS PRE_SANSUU{$kai}, ";
            $query .= "     BASE_D_{$seq}.REMARK3 AS PRE_EIGO{$kai}, ";
            $query .= "     BASE_D_{$seq}.REMARK4 AS PRE_2KA{$kai}, ";
            $query .= "     BASE_D_{$seq}.REMARK5 AS PRE_3KA{$kai}, ";
            $query .= "     BASE_D_{$seq}.REMARK6 AS PRE_TOTAL{$kai}, ";
        }
        $query .= "     BASE_D_005.REMARK1 AS EIKEN_SHUTOKU_KYUU, ";
        $query .= "     BASE_D_005.REMARK2 AS SHOUGAKU, ";
        $query .= "     BASE_D_005.REMARK3 AS TOKU_KATU, ";
        $query .= "     BASE_D_005.REMARK4 AS BIKOU ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_APPLICANTBASE_DAT T1 ";
        $query .= " LEFT JOIN ENTEXAM_APPLICANTADDR_DAT T2 ON ";
        $query .= "     T1.ENTEXAMYEAR = T2.ENTEXAMYEAR AND ";
        $query .= "     T1.APPLICANTDIV = T2.APPLICANTDIV AND ";
        $query .= "     T1.EXAMNO = T2.EXAMNO ";
        $query .= " LEFT JOIN V_NAME_MST T3 ON ";
        $query .= "     T1.ENTEXAMYEAR = T3.YEAR AND ";
        $query .= "     T1.ERACD = T3.NAMECD2 AND ";
        $query .= "     T3.NAMECD1 = 'L007' ";
        //入試区分(TESTDIV)
        $query .= " LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT BASE_D_010 ";
        $query .= "      ON BASE_D_010.ENTEXAMYEAR = T1.ENTEXAMYEAR ";
        $query .= "     AND BASE_D_010.APPLICANTDIV = T1.APPLICANTDIV ";
        $query .= "     AND BASE_D_010.EXAMNO = T1.EXAMNO ";
        $query .= "     AND BASE_D_010.SEQ = '010' ";
        //受験型(EXAM_TYPE)
        $query .= " LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT BASE_D_011 ";
        $query .= "      ON BASE_D_011.ENTEXAMYEAR = T1.ENTEXAMYEAR ";
        $query .= "     AND BASE_D_011.APPLICANTDIV = T1.APPLICANTDIV ";
        $query .= "     AND BASE_D_011.EXAMNO = T1.EXAMNO ";
        $query .= "     AND BASE_D_011.SEQ = '011' ";
        //受験番号(RECEPTNO)
        $query .= " LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT BASE_D_012 ";
        $query .= "      ON BASE_D_012.ENTEXAMYEAR = T1.ENTEXAMYEAR ";
        $query .= "     AND BASE_D_012.APPLICANTDIV = T1.APPLICANTDIV ";
        $query .= "     AND BASE_D_012.EXAMNO = T1.EXAMNO ";
        $query .= "     AND BASE_D_012.SEQ = '012' ";
        $query .= " LEFT JOIN FINSCHOOL_MST FIN ON T1.FS_CD = FIN.FINSCHOOLCD ";
        //第一志望コース
        $query .= " LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT BASE_D_001 ";
        $query .= "      ON BASE_D_001.ENTEXAMYEAR = T1.ENTEXAMYEAR ";
        $query .= "     AND BASE_D_001.APPLICANTDIV = T1.APPLICANTDIV ";
        $query .= "     AND BASE_D_001.EXAMNO = T1.EXAMNO ";
        $query .= "     AND BASE_D_001.SEQ = '001' ";
        //第二志望コース
        $query .= " LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT BASE_D_017 ";
        $query .= "      ON BASE_D_017.ENTEXAMYEAR = T1.ENTEXAMYEAR ";
        $query .= "     AND BASE_D_017.APPLICANTDIV = T1.APPLICANTDIV ";
        $query .= "     AND BASE_D_017.EXAMNO = T1.EXAMNO ";
        $query .= "     AND BASE_D_017.SEQ = '017' ";
        //英語見なし得点(REMARK1)、奨学区分(REMARK2)、特別活動(REMARK3)、備考(REMARK4)
        $query .= " LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT BASE_D_005 ";
        $query .= "      ON BASE_D_005.ENTEXAMYEAR = T1.ENTEXAMYEAR ";
        $query .= "     AND BASE_D_005.APPLICANTDIV = T1.APPLICANTDIV ";
        $query .= "     AND BASE_D_005.EXAMNO = T1.EXAMNO ";
        $query .= "     AND BASE_D_005.SEQ = '005' ";
        //プレテスト得点(第1回)(第2回)
        for ($kai = 1; $kai <= 2; $kai++) {
            $seq = ($kai == 1) ? "038" : "039";
            $query .= " LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT BASE_D_{$seq} ";
            $query .= "      ON BASE_D_{$seq}.ENTEXAMYEAR = T1.ENTEXAMYEAR ";
            $query .= "     AND BASE_D_{$seq}.APPLICANTDIV = T1.APPLICANTDIV ";
            $query .= "     AND BASE_D_{$seq}.EXAMNO = T1.EXAMNO ";
            $query .= "     AND BASE_D_{$seq}.SEQ = '{$seq}' ";
        }

        $query .= " WHERE ";
        $query .= "     T1.ENTEXAMYEAR = '".$model->year."' AND ";
        $query .= "     T1.APPLICANTDIV = '1' AND ";//1:中学
        if ($model->cmd == "back" || $model->cmd == "back1") {
            $query.= " T1.EXAMNO < '".$model->examno."'";
            $query.= " ORDER BY ";
            $query.= "     T1.EXAMNO DESC ";
        } elseif ($model->cmd == "next" || $model->cmd == "next1") {
            $query.= " T1.EXAMNO > '".sprintf("%05d", $model->examno)."'";
            $query.= " ORDER BY ";
            $query.= "     T1.EXAMNO ASC ";
        } else {
            $query.= " T1.EXAMNO = '".$model->examno."'";
        }

        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);

        return $row;
    }

    //出身学校マスタよりデータを取得
    function getFinschoolName($cd)
    {
        $query  = " SELECT  T1.FINSCHOOL_NAME, T1.DISTRICTCD, VALUE(L3.NAME1, '') AS FINSCHOOL_DISTCD_NAME, ";
        $query .= "         case when L4.NAME1 is not null then '(' || L4.NAME1 || ')' else '' end FINSCHOOL_TYPE_NAME ";
        $query .= "   FROM  FINSCHOOL_MST T1 ";
        $query .= "         LEFT JOIN NAME_MST L3 ON  L3.NAMECD1 = 'L001' ";
        $query .= "                               AND L3.NAMECD2 = T1.FINSCHOOL_DISTCD ";
        $query .= "         LEFT JOIN NAME_MST L4 ON  L4.NAMECD1 = 'L019' ";
        $query .= "                               AND L4.NAMECD2 = T1.FINSCHOOL_TYPE ";
        $query .= "  WHERE  T1.FINSCHOOLCD = '".$cd."' ";
        return $query;
    }

    //存在チェック
    function Existdata($model)
    {

        $query = "  SELECT
                        EXAMNO
                    FROM
                        ENTEXAM_APPLICANTBASE_DAT
                    WHERE
                        ENTEXAMYEAR = '".$model->year."' AND     
                        APPLICANTDIV = '".$model->field["APPLICANTDIV"]."' AND
                        EXAMNO = '".$model->examno."'";

        return $query;
    }

    //受験番号範囲チェック
    function getEntexamNoDatCnt($model, $testdiv, $exam_type, $receptno) {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_NO_DAT ";
        $query .= " WHERE ";
        $query .= "         ENTEXAMYEAR     = '{$model->year}' ";
        $query .= "     AND APPLICANTDIV    = '{$model->field["APPLICANTDIV"]}' ";
        $query .= "     AND TESTDIV         = '{$testdiv}' ";
        $query .= "     AND MEMO_KISO       = '{$exam_type}' ";
        $query .= "     AND '{$receptno}' BETWEEN EXAMNO_FROM AND EXAMNO_TO ";
        //入力画面で追加するのは、窓口・郵送のみとする。
        if ($model->cmd == "add") {
//            $query .= "     AND REMARK_DIV      = '2' ";//1:Web出願　2:窓口・郵送
        }

//        echo $query;
        return $query;
    }

    //受験番号重複チェック
    function checkNumber($model, $testdiv, $receptno) {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_APPLICANTBASE_DETAIL_DAT ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR = '".$model->year."' ";
        $query .= "     AND APPLICANTDIV = '{$model->field["APPLICANTDIV"]}' ";
        $query .= "     AND EXAMNO <> '".$model->examno."' ";
        $query .= "     AND SEQ = '012' ";
        $query .= "     AND REMARK{$testdiv} = '{$receptno}' ";
        return $query;
    }

    //--- INSERT
    function &getInsertQuery($model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //変換用情報取得(和暦→西暦)
        $result = $db->query(knjl510fQuery::get_calendarno($model->year));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $eracd[] = $row["NAMECD2"];
            $defyear[] = $row["NAMESPARE1"];
        }
        $result->free();

        //卒業日の変換(和暦→西暦)
        $fs_date = $fs_grdyear = "";
        if (strlen($model->field["FS_ERACD"]) && strlen($model->field["FS_Y"]) && strlen($model->field["FS_M"])) {
            for ($i=0; $i<get_count($eracd); $i++) {
                if ($model->field["FS_ERACD"] == $eracd[$i]) {
                    $fs_grdyear = ((int)$defyear[$i]) + ((int)$model->field["FS_Y"] - 1);
                    $fs_date = $fs_grdyear. "-".sprintf("%02d",$model->field["FS_M"])."-01";
                    break;
                }
            }
        }

        //ENTEXAM_APPLICANTBASE_DAT
        $data = array();
        $data["ENTEXAMYEAR"][TEXT]   = $model->year;
        $data["APPLICANTDIV"][TEXT]  = $model->field["APPLICANTDIV"];
        $data["EXAMNO"][TEXT]        = $model->examno;
        $data["TESTDIV"][TEXT]       = "2"; //固定2
        $data["SHDIV"][TEXT]         = $model->field["SHDIV"]; //専併区分
        $data["DESIREDIV"][TEXT]     = "1";
        $data["RECEPTDATE"][TEXT]    = str_replace("/", "-", $model->field["RECEPTDATE"]);
        $data["NAME"][TEXT]          = $model->field["NAME"];
        $data["NAME_KANA"][TEXT]     = $model->field["NAME_KANA"];
        $data["SEX"][TEXT]           = $model->field["SEX"]; //性別
        $data["BIRTHDAY"][TEXT]      = str_replace("/", "-", $model->field["BIRTHDAY"]);
        $data["FS_CD"][TEXT]         = (strlen($model->field["FS_CD"]) ? sprintf("%07d",$model->field["FS_CD"]) : "");
        $data["FS_ERACD"][TEXT]      = $model->field["FS_ERACD"];
        $data["FS_Y"][TEXT]          = $model->field["FS_Y"]? sprintf("%02d",$model->field["FS_Y"]) : "";
        $data["FS_M"][TEXT]          = $model->field["FS_M"]? sprintf("%02d",$model->field["FS_M"]) : "";
        $data["FS_GRDDIV"][TEXT]     = $model->field["FS_GRDDIV"];
        $data["FS_GRDYEAR"][TEXT]    = $fs_grdyear;
        $data["FS_DAY"][TEXT]        = $fs_date;
        $data["REGISTERCD"][TEXT]    = STAFFCD;
        $data["UPDATED"][FUNC]       = "sysdate()";

        $query = Query::insertSQL($data, "ENTEXAM_APPLICANTBASE_DAT");
        $db->query($query);

        //ENTEXAM_APPLICANTADDR_DAT
        $data = array();
        $data["ENTEXAMYEAR"][TEXT]   = $model->year;
        $data["APPLICANTDIV"][TEXT]  = $model->field["APPLICANTDIV"];
        $data["EXAMNO"][TEXT]        = $model->examno;
        $data["ZIPCD"][TEXT]         = $model->field["ZIPCD"];
        $data["ADDRESS1"][TEXT]      = $model->field["ADDRESS1"];
        $data["ADDRESS2"][TEXT]      = $model->field["ADDRESS2"];
        $data["TELNO"][TEXT]         = $model->field["TELNO"];
        $data["GNAME"][TEXT]         = $model->field["GNAME"];
        $data["GKANA"][TEXT]         = $model->field["GKANA"];
        $data["GZIPCD"][TEXT]        = $model->field["GZIPCD"];
        $data["GADDRESS1"][TEXT]     = $model->field["GADDRESS1"];
        $data["GADDRESS2"][TEXT]     = $model->field["GADDRESS2"];
        $data["GTELNO"][TEXT]        = $model->field["GTELNO"];
        $data["RELATIONSHIP"][TEXT]  = $model->field["RELATIONSHIP"];
        $data["REGISTERCD"][TEXT]    = STAFFCD;
        $data["UPDATED"][FUNC]       = "sysdate()";

        $query = Query::insertSQL($data, "ENTEXAM_APPLICANTADDR_DAT");
        $db->query($query);

        //ENTEXAM_APPLICANTBASE_DETAIL_DAT 010:入試区分,011:受験型,012:受験番号
        //DELETE
        $query = " DELETE FROM ENTEXAM_APPLICANTBASE_DETAIL_DAT ";
        $query.= " WHERE ENTEXAMYEAR = '".$model->year."' ";
        $query.= "   AND APPLICANTDIV = '".$model->field["APPLICANTDIV"]."' ";
        $query.= "   AND EXAMNO = '".$model->examno."' ";
        $query.= "   AND SEQ IN ('010','011','012') ";
        $db->query($query);
        //INSERT
        $testCnt = 0;
        foreach ($model->testdivArray as $key => $codeArray) {
            $testdiv = $codeArray["TESTDIV"];
            //入試区分チェックありのみ処理
            if ($model->field["TESTDIV".$testdiv] == "") continue;
            //010:入試区分,011:受験型,012:受験番号
            for ($i = 10; $i <= 12; $i++) {
                $seq = sprintf("%03d", $i);
                $name = "";
                if ($seq == "010") $name = "TESTDIV";
                if ($seq == "011") $name = "EXAM_TYPE";
                if ($seq == "012") $name = "RECEPTNO";
                $data = array();
                $data["ENTEXAMYEAR"][TEXT]      = $model->year;
                $data["APPLICANTDIV"][TEXT]     = $model->field["APPLICANTDIV"];
                $data["EXAMNO"][TEXT]           = $model->examno;
                $data["SEQ"][TEXT]              = $seq;
                $data["REMARK".$testdiv][TEXT]  = $model->field[$name.$testdiv];
                $data["REGISTERCD"][TEXT]       = STAFFCD;
                $data["UPDATED"][FUNC]          = "sysdate()";
                if ($testCnt == 0) {
                    $query = Query::insertSQL($data, "ENTEXAM_APPLICANTBASE_DETAIL_DAT");
                } else {
                    $where  = " WHERE ENTEXAMYEAR = '".$model->year."' ";
                    $where .= "   AND APPLICANTDIV = '".$model->field["APPLICANTDIV"]."' ";
                    $where .= "   AND EXAMNO = '".$model->examno."' ";
                    $where .= "   AND SEQ = '{$seq}' ";
                    $query = Query::updateSQL($data, "ENTEXAM_APPLICANTBASE_DETAIL_DAT", $where);
                }
                $db->query($query);
            }
            $testCnt++;
        }

        //第１志望コース
        $course_array = array();
        $course_array = explode("-", $model->field["EXAMCOURSE"]);
        //ENTEXAM_APPLICANTBASE_DETAIL_DAT SEQ=001
        $data = array();
        $data["ENTEXAMYEAR"][TEXT]  = $model->year;
        $data["APPLICANTDIV"][TEXT] = $model->field["APPLICANTDIV"];
        $data["EXAMNO"][TEXT]       = $model->examno;
        $data["SEQ"][TEXT]          = "001";
        $data["REMARK8"][TEXT]      = $course_array[0];
        $data["REMARK9"][TEXT]      = $course_array[1];
        $data["REMARK10"][TEXT]     = $course_array[2];
        $data["REGISTERCD"][TEXT]   = STAFFCD;
        $data["UPDATED"][FUNC]      = "sysdate()";
        $where  = " WHERE ENTEXAMYEAR = '".$model->year."' ";
        $where .= "       AND APPLICANTDIV = '".$model->field["APPLICANTDIV"]."' ";
        $where .= "       AND EXAMNO = '".$model->examno."'";
        $where .= "       AND SEQ = '001'";
        $dataCnt = $db->getOne(" SELECT COUNT(*) FROM ENTEXAM_APPLICANTBASE_DETAIL_DAT ".$where);
        if ($dataCnt == 0) {
            $query = Query::insertSQL($data, "ENTEXAM_APPLICANTBASE_DETAIL_DAT");
        } else {
            $query = Query::updateSQL($data, "ENTEXAM_APPLICANTBASE_DETAIL_DAT", $where);
        }
        $db->query($query);

        //第２志望コース
        $course_array = array();
        $course_array = explode("-", $model->field["EXAMCOURSE2"]);
        //ENTEXAM_APPLICANTBASE_DETAIL_DAT SEQ=017
        $data = array();
        $data["ENTEXAMYEAR"][TEXT]  = $model->year;
        $data["APPLICANTDIV"][TEXT] = $model->field["APPLICANTDIV"];
        $data["EXAMNO"][TEXT]       = $model->examno;
        $data["SEQ"][TEXT]          = "017";
        $data["REMARK1"][TEXT]      = $course_array[0];
        $data["REMARK2"][TEXT]      = $course_array[1];
        $data["REMARK3"][TEXT]      = $course_array[2];
        $data["REGISTERCD"][TEXT]   = STAFFCD;
        $data["UPDATED"][FUNC]      = "sysdate()";
        $where  = " WHERE ENTEXAMYEAR = '".$model->year."' ";
        $where .= "       AND APPLICANTDIV = '".$model->field["APPLICANTDIV"]."' ";
        $where .= "       AND EXAMNO = '".$model->examno."'";
        $where .= "       AND SEQ = '017'";
        $dataCnt = $db->getOne(" SELECT COUNT(*) FROM ENTEXAM_APPLICANTBASE_DETAIL_DAT ".$where);
        if ($dataCnt == 0) {
            $query = Query::insertSQL($data, "ENTEXAM_APPLICANTBASE_DETAIL_DAT");
        } else {
            $query = Query::updateSQL($data, "ENTEXAM_APPLICANTBASE_DETAIL_DAT", $where);
        }
        $db->query($query);

        //英語見なし得点(REMARK1)、奨学区分(REMARK2)、特別活動(REMARK3)、備考(REMARK4)
        //ENTEXAM_APPLICANTBASE_DETAIL_DAT SEQ=005
        $data = array();
        $data["ENTEXAMYEAR"][TEXT]  = $model->year;
        $data["APPLICANTDIV"][TEXT] = $model->field["APPLICANTDIV"];
        $data["EXAMNO"][TEXT]       = $model->examno;
        $data["SEQ"][TEXT]          = "005";
        $data["REMARK1"][TEXT]      = $model->field["EIKEN_SHUTOKU_KYUU"];
        $data["REMARK2"][TEXT]      = $model->field["SHOUGAKU"];
        $data["REMARK3"][TEXT]      = $model->field["TOKU_KATU"];
        $data["REMARK4"][TEXT]      = $model->field["BIKOU"];
        $data["REGISTERCD"][TEXT]   = STAFFCD;
        $data["UPDATED"][FUNC]      = "sysdate()";
        $where  = " WHERE ENTEXAMYEAR = '".$model->year."' ";
        $where .= "       AND APPLICANTDIV = '".$model->field["APPLICANTDIV"]."' ";
        $where .= "       AND EXAMNO = '".$model->examno."'";
        $where .= "       AND SEQ = '005'";
        $dataCnt = $db->getOne(" SELECT COUNT(*) FROM ENTEXAM_APPLICANTBASE_DETAIL_DAT ".$where);
        if ($dataCnt == 0) {
            $query = Query::insertSQL($data, "ENTEXAM_APPLICANTBASE_DETAIL_DAT");
        } else {
            $query = Query::updateSQL($data, "ENTEXAM_APPLICANTBASE_DETAIL_DAT", $where);
        }
        $db->query($query);

        //プレテスト得点(第1回)(第2回)
        //ENTEXAM_APPLICANTBASE_DETAIL_DAT SEQ=038(第1回),039(第2回)
        for ($kai = 1; $kai <= 2; $kai++) {
            $seq = ($kai == 1) ? "038" : "039";
            $data = array();
            $data["ENTEXAMYEAR"][TEXT]  = $model->year;
            $data["APPLICANTDIV"][TEXT] = $model->field["APPLICANTDIV"];
            $data["EXAMNO"][TEXT]       = $model->examno;
            $data["SEQ"][TEXT]          = $seq;
            $data["REMARK1"][TEXT]      = $model->field["PRE_KOKUGO".$kai];
            $data["REMARK2"][TEXT]      = $model->field["PRE_SANSUU".$kai];
            $data["REMARK3"][TEXT]      = $model->field["PRE_EIGO".$kai];
            $flg2ka = $flg3ka = false;
            $total2ka = $total3ka = 0;
            $cnt3ka = 0;
            if ($model->field["PRE_KOKUGO".$kai]) {
                $total2ka += $model->field["PRE_KOKUGO".$kai];
                $total3ka += $model->field["PRE_KOKUGO".$kai];
                $flg2ka = true;
                $flg3ka = true;
                $cnt3ka++;
            }
            if ($model->field["PRE_SANSUU".$kai]) {
                $total2ka += $model->field["PRE_SANSUU".$kai];
                $total3ka += $model->field["PRE_SANSUU".$kai];
                $flg2ka = true;
                $flg3ka = true;
                $cnt3ka++;
            }
            if ($model->field["PRE_EIGO".$kai]) {
                $total3ka += $model->field["PRE_EIGO".$kai];
                $flg3ka = true;
                $cnt3ka++;
            }
            $data["REMARK4"][TEXT]      = ($flg2ka) ? $total2ka : "";
            $data["REMARK5"][TEXT]      = ($cnt3ka == 3) ? round($total3ka * 2 / 2.5) : "";
            $data["REMARK6"][TEXT]      = ($flg3ka) ? $total3ka : "";
            $data["REGISTERCD"][TEXT]   = STAFFCD;
            $data["UPDATED"][FUNC]      = "sysdate()";
            $where  = " WHERE ENTEXAMYEAR = '".$model->year."' ";
            $where .= "       AND APPLICANTDIV = '".$model->field["APPLICANTDIV"]."' ";
            $where .= "       AND EXAMNO = '".$model->examno."'";
            $where .= "       AND SEQ = '{$seq}'";
            $dataCnt = $db->getOne(" SELECT COUNT(*) FROM ENTEXAM_APPLICANTBASE_DETAIL_DAT ".$where);
            if ($dataCnt == 0) {
                $query = Query::insertSQL($data, "ENTEXAM_APPLICANTBASE_DETAIL_DAT");
            } else {
                $query = Query::updateSQL($data, "ENTEXAM_APPLICANTBASE_DETAIL_DAT", $where);
            }
            $db->query($query);
        }



        $db->commit();
        Query::dbCheckIn($db);
        return;
    }

    //--- UPDATE
    function &getUpdateQuery($model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //変換用情報取得(和暦→西暦)
        $result = $db->query(knjl510fQuery::get_calendarno($model->year));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $eracd[] = $row["NAMECD2"];
            $defyear[] = $row["NAMESPARE1"];
        }
        $result->free();

        //卒業日の変換(和暦→西暦)
        $fs_date = $fs_grdyear = "";
        if (strlen($model->field["FS_ERACD"]) && strlen($model->field["FS_Y"]) && strlen($model->field["FS_M"])) {
            for ($i=0; $i<get_count($eracd); $i++) {
                if ($model->field["FS_ERACD"] == $eracd[$i]) {
                    $fs_grdyear = ((int)$defyear[$i]) + ((int)$model->field["FS_Y"] - 1);
                    $fs_date = $fs_grdyear. "-".sprintf("%02d",$model->field["FS_M"])."-01";
                    break;
                }
            }
        }

        //ENTEXAM_APPLICANTBASE_DAT
        $data = array();
        $data["ENTEXAMYEAR"][TEXT]   = $model->year;
        $data["APPLICANTDIV"][TEXT]  = $model->field["APPLICANTDIV"];
        $data["EXAMNO"][TEXT]        = $model->examno;
        $data["TESTDIV"][TEXT]       = "2"; //固定2
        $data["SHDIV"][TEXT]         = $model->field["SHDIV"]; //専併区分
        $data["DESIREDIV"][TEXT]     = "1";
        $data["RECEPTDATE"][TEXT]    = str_replace("/", "-", $model->field["RECEPTDATE"]);
        $data["NAME"][TEXT]          = $model->field["NAME"];
        $data["NAME_KANA"][TEXT]     = $model->field["NAME_KANA"];
        $data["SEX"][TEXT]           = $model->field["SEX"]; //性別
        $data["BIRTHDAY"][TEXT]      = str_replace("/", "-", $model->field["BIRTHDAY"]);
        $data["FS_CD"][TEXT]         = (strlen($model->field["FS_CD"]) ? sprintf("%07d",$model->field["FS_CD"]) : "");
        $data["FS_ERACD"][TEXT]      = $model->field["FS_ERACD"];
        $data["FS_Y"][TEXT]          = $model->field["FS_Y"]? sprintf("%02d",$model->field["FS_Y"]) : "";
        $data["FS_M"][TEXT]          = $model->field["FS_M"]? sprintf("%02d",$model->field["FS_M"]) : "";
        $data["FS_GRDDIV"][TEXT]     = $model->field["FS_GRDDIV"];
        $data["FS_GRDYEAR"][TEXT]    = $fs_grdyear;
        $data["FS_DAY"][TEXT]        = $fs_date;
        $data["REGISTERCD"][TEXT]    = STAFFCD;
        $data["UPDATED"][FUNC]       = "sysdate()";

        $where  = " WHERE ENTEXAMYEAR = '".$model->year."' AND";
        $where .= "       APPLICANTDIV = '".$model->field["APPLICANTDIV"]."' AND";
        $where .= "       EXAMNO = '".$model->examno."'";

        $query = Query::updateSQL($data, "ENTEXAM_APPLICANTBASE_DAT", $where);
        $db->query($query);

        //ENTEXAM_APPLICANTADDR_DAT
        //DELETE
        $query = " DELETE FROM ENTEXAM_APPLICANTADDR_DAT ";
        $query.= " WHERE ENTEXAMYEAR = '".$model->year."' AND";
        $query.= "       APPLICANTDIV = '".$model->field["APPLICANTDIV"]."' AND";
        $query.= "       EXAMNO = '".$model->examno."'";
        $db->query($query);
        //INSERT
        $data = array();
        $data["ENTEXAMYEAR"][TEXT]   = $model->year;
        $data["APPLICANTDIV"][TEXT]  = $model->field["APPLICANTDIV"];
        $data["EXAMNO"][TEXT]        = $model->examno;
        $data["ZIPCD"][TEXT]         = $model->field["ZIPCD"];
        $data["ADDRESS1"][TEXT]      = $model->field["ADDRESS1"];
        $data["ADDRESS2"][TEXT]      = $model->field["ADDRESS2"];
        $data["TELNO"][TEXT]         = $model->field["TELNO"];
        $data["GNAME"][TEXT]         = $model->field["GNAME"];
        $data["GKANA"][TEXT]         = $model->field["GKANA"];
        $data["GZIPCD"][TEXT]        = $model->field["GZIPCD"];
        $data["GADDRESS1"][TEXT]     = $model->field["GADDRESS1"];
        $data["GADDRESS2"][TEXT]     = $model->field["GADDRESS2"];
        $data["GTELNO"][TEXT]        = $model->field["GTELNO"];
        $data["RELATIONSHIP"][TEXT]  = $model->field["RELATIONSHIP"];
        $data["REGISTERCD"][TEXT]    = STAFFCD;
        $data["UPDATED"][FUNC]       = "sysdate()";

        $query = Query::insertSQL($data, "ENTEXAM_APPLICANTADDR_DAT");
        $db->query($query);

        //ENTEXAM_APPLICANTBASE_DETAIL_DAT 010:入試区分,011:受験型,012:受験番号
        //DELETE
        $query = " DELETE FROM ENTEXAM_APPLICANTBASE_DETAIL_DAT ";
        $query.= " WHERE ENTEXAMYEAR = '".$model->year."' ";
        $query.= "   AND APPLICANTDIV = '".$model->field["APPLICANTDIV"]."' ";
        $query.= "   AND EXAMNO = '".$model->examno."' ";
        $query.= "   AND SEQ IN ('010','011','012') ";
        $db->query($query);
        //INSERT
        $testCnt = 0;
        foreach ($model->testdivArray as $key => $codeArray) {
            $testdiv = $codeArray["TESTDIV"];
            //入試区分チェックありのみ処理
            if ($model->field["TESTDIV".$testdiv] == "") continue;
            //010:入試区分,011:受験型,012:受験番号
            for ($i = 10; $i <= 12; $i++) {
                $seq = sprintf("%03d", $i);
                $name = "";
                if ($seq == "010") $name = "TESTDIV";
                if ($seq == "011") $name = "EXAM_TYPE";
                if ($seq == "012") $name = "RECEPTNO";
                $data = array();
                $data["ENTEXAMYEAR"][TEXT]      = $model->year;
                $data["APPLICANTDIV"][TEXT]     = $model->field["APPLICANTDIV"];
                $data["EXAMNO"][TEXT]           = $model->examno;
                $data["SEQ"][TEXT]              = $seq;
                $data["REMARK".$testdiv][TEXT]  = $model->field[$name.$testdiv];
                $data["REGISTERCD"][TEXT]       = STAFFCD;
                $data["UPDATED"][FUNC]          = "sysdate()";
                if ($testCnt == 0) {
                    $query = Query::insertSQL($data, "ENTEXAM_APPLICANTBASE_DETAIL_DAT");
                } else {
                    $where  = " WHERE ENTEXAMYEAR = '".$model->year."' ";
                    $where .= "   AND APPLICANTDIV = '".$model->field["APPLICANTDIV"]."' ";
                    $where .= "   AND EXAMNO = '".$model->examno."' ";
                    $where .= "   AND SEQ = '{$seq}' ";
                    $query = Query::updateSQL($data, "ENTEXAM_APPLICANTBASE_DETAIL_DAT", $where);
                }
                $db->query($query);
            }
            $testCnt++;
        }

        //第１志望コース
        $course_array = array();
        $course_array = explode("-", $model->field["EXAMCOURSE"]);
        //ENTEXAM_APPLICANTBASE_DETAIL_DAT SEQ=001
        $data = array();
        $data["ENTEXAMYEAR"][TEXT]  = $model->year;
        $data["APPLICANTDIV"][TEXT] = $model->field["APPLICANTDIV"];
        $data["EXAMNO"][TEXT]       = $model->examno;
        $data["SEQ"][TEXT]          = "001";
        $data["REMARK8"][TEXT]      = $course_array[0];
        $data["REMARK9"][TEXT]      = $course_array[1];
        $data["REMARK10"][TEXT]     = $course_array[2];
        $data["REGISTERCD"][TEXT]   = STAFFCD;
        $data["UPDATED"][FUNC]      = "sysdate()";
        $where  = " WHERE ENTEXAMYEAR = '".$model->year."' ";
        $where .= "       AND APPLICANTDIV = '".$model->field["APPLICANTDIV"]."' ";
        $where .= "       AND EXAMNO = '".$model->examno."'";
        $where .= "       AND SEQ = '001'";
        $dataCnt = $db->getOne(" SELECT COUNT(*) FROM ENTEXAM_APPLICANTBASE_DETAIL_DAT ".$where);
        if ($dataCnt == 0) {
            $query = Query::insertSQL($data, "ENTEXAM_APPLICANTBASE_DETAIL_DAT");
        } else {
            $query = Query::updateSQL($data, "ENTEXAM_APPLICANTBASE_DETAIL_DAT", $where);
        }
        $db->query($query);

        //第２志望コース
        $course_array = array();
        $course_array = explode("-", $model->field["EXAMCOURSE2"]);
        //ENTEXAM_APPLICANTBASE_DETAIL_DAT SEQ=017
        $data = array();
        $data["ENTEXAMYEAR"][TEXT]  = $model->year;
        $data["APPLICANTDIV"][TEXT] = $model->field["APPLICANTDIV"];
        $data["EXAMNO"][TEXT]       = $model->examno;
        $data["SEQ"][TEXT]          = "017";
        $data["REMARK1"][TEXT]      = $course_array[0];
        $data["REMARK2"][TEXT]      = $course_array[1];
        $data["REMARK3"][TEXT]      = $course_array[2];
        $data["REGISTERCD"][TEXT]   = STAFFCD;
        $data["UPDATED"][FUNC]      = "sysdate()";
        $where  = " WHERE ENTEXAMYEAR = '".$model->year."' ";
        $where .= "       AND APPLICANTDIV = '".$model->field["APPLICANTDIV"]."' ";
        $where .= "       AND EXAMNO = '".$model->examno."'";
        $where .= "       AND SEQ = '017'";
        $dataCnt = $db->getOne(" SELECT COUNT(*) FROM ENTEXAM_APPLICANTBASE_DETAIL_DAT ".$where);
        if ($dataCnt == 0) {
            $query = Query::insertSQL($data, "ENTEXAM_APPLICANTBASE_DETAIL_DAT");
        } else {
            $query = Query::updateSQL($data, "ENTEXAM_APPLICANTBASE_DETAIL_DAT", $where);
        }
        $db->query($query);

        //英語見なし得点(REMARK1)、奨学区分(REMARK2)、特別活動(REMARK3)、備考(REMARK4)
        //ENTEXAM_APPLICANTBASE_DETAIL_DAT SEQ=005
        $data = array();
        $data["ENTEXAMYEAR"][TEXT]  = $model->year;
        $data["APPLICANTDIV"][TEXT] = $model->field["APPLICANTDIV"];
        $data["EXAMNO"][TEXT]       = $model->examno;
        $data["SEQ"][TEXT]          = "005";
        $data["REMARK1"][TEXT]      = $model->field["EIKEN_SHUTOKU_KYUU"];
        $data["REMARK2"][TEXT]      = $model->field["SHOUGAKU"];
        $data["REMARK3"][TEXT]      = $model->field["TOKU_KATU"];
        $data["REMARK4"][TEXT]      = $model->field["BIKOU"];
        $data["REGISTERCD"][TEXT]   = STAFFCD;
        $data["UPDATED"][FUNC]      = "sysdate()";
        $where  = " WHERE ENTEXAMYEAR = '".$model->year."' ";
        $where .= "       AND APPLICANTDIV = '".$model->field["APPLICANTDIV"]."' ";
        $where .= "       AND EXAMNO = '".$model->examno."'";
        $where .= "       AND SEQ = '005'";
        $dataCnt = $db->getOne(" SELECT COUNT(*) FROM ENTEXAM_APPLICANTBASE_DETAIL_DAT ".$where);
        if ($dataCnt == 0) {
            $query = Query::insertSQL($data, "ENTEXAM_APPLICANTBASE_DETAIL_DAT");
        } else {
            $query = Query::updateSQL($data, "ENTEXAM_APPLICANTBASE_DETAIL_DAT", $where);
        }
        $db->query($query);

        //プレテスト得点(第1回)(第2回)
        //ENTEXAM_APPLICANTBASE_DETAIL_DAT SEQ=038(第1回),039(第2回)
        for ($kai = 1; $kai <= 2; $kai++) {
            $seq = ($kai == 1) ? "038" : "039";
            $data = array();
            $data["ENTEXAMYEAR"][TEXT]  = $model->year;
            $data["APPLICANTDIV"][TEXT] = $model->field["APPLICANTDIV"];
            $data["EXAMNO"][TEXT]       = $model->examno;
            $data["SEQ"][TEXT]          = $seq;
            $data["REMARK1"][TEXT]      = $model->field["PRE_KOKUGO".$kai];
            $data["REMARK2"][TEXT]      = $model->field["PRE_SANSUU".$kai];
            $data["REMARK3"][TEXT]      = $model->field["PRE_EIGO".$kai];
            $flg2ka = $flg3ka = false;
            $total2ka = $total3ka = 0;
            $cnt3ka = 0;
            if ($model->field["PRE_KOKUGO".$kai]) {
                $total2ka += $model->field["PRE_KOKUGO".$kai];
                $total3ka += $model->field["PRE_KOKUGO".$kai];
                $flg2ka = true;
                $flg3ka = true;
                $cnt3ka++;
            }
            if ($model->field["PRE_SANSUU".$kai]) {
                $total2ka += $model->field["PRE_SANSUU".$kai];
                $total3ka += $model->field["PRE_SANSUU".$kai];
                $flg2ka = true;
                $flg3ka = true;
                $cnt3ka++;
            }
            if ($model->field["PRE_EIGO".$kai]) {
                $total3ka += $model->field["PRE_EIGO".$kai];
                $flg3ka = true;
                $cnt3ka++;
            }
            $data["REMARK4"][TEXT]      = ($flg2ka) ? $total2ka : "";
            $data["REMARK5"][TEXT]      = ($cnt3ka == 3) ? round($total3ka * 2 / 2.5) : "";
            $data["REMARK6"][TEXT]      = ($flg3ka) ? $total3ka : "";
            $data["REGISTERCD"][TEXT]   = STAFFCD;
            $data["UPDATED"][FUNC]      = "sysdate()";
            $where  = " WHERE ENTEXAMYEAR = '".$model->year."' ";
            $where .= "       AND APPLICANTDIV = '".$model->field["APPLICANTDIV"]."' ";
            $where .= "       AND EXAMNO = '".$model->examno."'";
            $where .= "       AND SEQ = '{$seq}'";
            $dataCnt = $db->getOne(" SELECT COUNT(*) FROM ENTEXAM_APPLICANTBASE_DETAIL_DAT ".$where);
            if ($dataCnt == 0) {
                $query = Query::insertSQL($data, "ENTEXAM_APPLICANTBASE_DETAIL_DAT");
            } else {
                $query = Query::updateSQL($data, "ENTEXAM_APPLICANTBASE_DETAIL_DAT", $where);
            }
            $db->query($query);
        }



        $db->commit();
        Query::dbCheckIn($db);
        return;
    }

    //--- DELETE
    function &getDeleteQuery($model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //ENTEXAM_APPLICANTBASE_DAT
        $query  = " DELETE FROM ENTEXAM_APPLICANTBASE_DAT ";
        $query .= " WHERE ENTEXAMYEAR = '".$model->year."' AND";
        $query .= "       APPLICANTDIV = '".$model->field["APPLICANTDIV"]."' AND";
        $query .= "       EXAMNO      = '".$model->examno."'";
        $db->query($query);

        //ENTEXAM_APPLICANTBASE_DETAIL_DAT
        $query  = " DELETE FROM ENTEXAM_APPLICANTBASE_DETAIL_DAT ";
        $query .= " WHERE ENTEXAMYEAR = '".$model->year."' AND";
        $query .= "       APPLICANTDIV = '".$model->field["APPLICANTDIV"]."' AND";
        $query .= "       EXAMNO      = '".$model->examno."'";
        $db->query($query);

        //ENTEXAM_APPLICANTADDR_DAT
        $query  = " DELETE FROM ENTEXAM_APPLICANTADDR_DAT ";
        $query .= " WHERE ENTEXAMYEAR = '".$model->year."' AND";
        $query .= "       APPLICANTDIV = '".$model->field["APPLICANTDIV"]."' AND";
        $query .= "       EXAMNO      = '".$model->examno."'";
        $db->query($query);

        //ENTEXAM_APPLICANTCONFRPT_DAT
        $query  = " DELETE FROM ENTEXAM_APPLICANTCONFRPT_DAT ";
        $query .= " WHERE ENTEXAMYEAR = '".$model->year."' AND";
        $query .= "       APPLICANTDIV = '".$model->field["APPLICANTDIV"]."' AND";
        $query .= "       EXAMNO      = '".$model->examno."'";
        $db->query($query);

        //ENTEXAM_SCORE_DAT
        $query  = " DELETE FROM ENTEXAM_SCORE_DAT ";
        $query .= " WHERE (ENTEXAMYEAR, APPLICANTDIV, TESTDIV, EXAM_TYPE, RECEPTNO) IN  (";
        $query .= "         SELECT ENTEXAMYEAR, APPLICANTDIV, TESTDIV, EXAM_TYPE, RECEPTNO ";
        $query .= "           FROM ENTEXAM_RECEPT_DAT ";
        $query .= "          WHERE ENTEXAMYEAR = '".$model->year."' AND";
        $query .= "                APPLICANTDIV = '".$model->field["APPLICANTDIV"]."' AND";
        $query .= "                EXAMNO      = '".$model->examno."'";
        $query .= "       )";
        $db->query($query);

        //ENTEXAM_RECEPT_DETAIL_DAT
        $query  = " DELETE FROM ENTEXAM_RECEPT_DETAIL_DAT ";
        $query .= " WHERE (ENTEXAMYEAR, APPLICANTDIV, TESTDIV, EXAM_TYPE, RECEPTNO) IN  (";
        $query .= "         SELECT ENTEXAMYEAR, APPLICANTDIV, TESTDIV, EXAM_TYPE, RECEPTNO ";
        $query .= "           FROM ENTEXAM_RECEPT_DAT ";
        $query .= "          WHERE ENTEXAMYEAR = '".$model->year."' AND";
        $query .= "                APPLICANTDIV = '".$model->field["APPLICANTDIV"]."' AND";
        $query .= "                EXAMNO      = '".$model->examno."'";
        $query .= "       )";
        $db->query($query);

        //ENTEXAM_RECEPT_DAT
        $query  = "DELETE FROM ENTEXAM_RECEPT_DAT ";
        $query .= " WHERE ENTEXAMYEAR = '".$model->year."' AND";
        $query .= "       APPLICANTDIV = '".$model->field["APPLICANTDIV"]."' AND";
        $query .= "       EXAMNO      = '".$model->examno."'";
        $db->query($query);

        $db->commit();
        Query::dbCheckIn($db);
        return;
    }
}
?>