<?php

require_once('for_php7.php');

class knjl350wQuery extends Query {

    //権限チェック（入試管理者）
    function getAdminFlg() {
        $query  = " SELECT ";
        $query .= "     FIELD1 ";
        $query .= " FROM ";
        $query .= "     STAFF_DETAIL_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".CTRL_YEAR."' ";
        $query .= "     AND STAFFCD = '".STAFFCD."' ";
        $query .= "     AND STAFF_SEQ = '009' ";

        $db = Query::dbCheckOut();
        $rtnFlg = $db->getOne($query);
        Query::dbCheckIn($db);

        return $rtnFlg;
    }

    //教育委員会用の学校コード取得
    function getSchoolMst($model) {
        $query  = " SELECT ";
        $query .= "      KYOUIKU_IINKAI_SCHOOLCD, ";
        $query .= "      PREF_CD ";
        $query .= " FROM ";
        $query .= "      V_SCHOOL_MST ";
        $query .= " WHERE ";
        $query .= "      YEAR    = '".$model->field["YEAR"]."' ";

        return $query;
    }

    //年度取得
    function getYear() {
        $query  = " SELECT ";
        $query .= "      YEAR AS LABEL, ";
        $query .= "      YEAR AS VALUE ";
        $query .= " FROM ";
        $query .= "      SCHOOL_MST ";
        $query .= " WHERE ";
        $query .= "      YEAR = '".(CTRL_YEAR + 1)."' ";
        $query .= " ORDER BY ";
        $query .= "      VALUE DESC ";

        return $query;
    }

    //名称マスタよりデータを取得
    function get_name_cd($year, $cd, $sitei = "")
    {
        $query  = " SELECT ";
        $query .= "     NAMECD2 || ':' || NAME1 AS LABEL, ";
        $query .= "     NAMECD2 AS VALUE, ";
        $query .= "     NAME1, ";
        $query .= "     ABBV1, ";
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
        $query .= "     VALUE ";

        return $query;
    }

    //受検コースの入試区分を取得
    function getCourseTestDiv($model) {

        $query  = " SELECT DISTINCT ";
        $query .= "     T1.TESTDIV ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_COURSE_MST T1 ";
        $query .= " WHERE ";
        $query .= "     T1.ENTEXAMYEAR = '{$model->field["YEAR"]}' ";
        $query .= "     AND T1.APPLICANTDIV = '{$model->field["APPLICANTDIV"]}' ";

        return $query;
    }

    //TITLEデータ
    function get_name_cdAft($model, $namecd1, $aftField) {

        $query  = " SELECT DISTINCT ";
        $query .= "     T1.{$aftField} AS VALUE, ";
        if ($aftField == "TESTDIV") {
            $query .= "     L1.NAME2 AS LABEL ";
        } else {
            $query .= "     T1.{$aftField} || ':' || L1.NAME1 AS LABEL ";
        }
        $query .= " FROM ";
        $query .= "     AFT_SINRO_ADDITION_L350W_FIELD_DAT T1 ";
        $query .= "     INNER JOIN V_NAME_MST L1 ON T1.YEAR = L1.YEAR ";
        $query .= "          AND L1.NAMECD1 = '{$namecd1}' ";
        $query .= "          AND T1.{$aftField} = L1.NAMECD2 ";
        $query .= " WHERE ";
        $query .= "     T1.YEAR = '".$model->field["YEAR"]."' ";
        $query .= "     AND T1.APPLICANTDIV = '".$model->field["APPLICANTDIV"]."' ";
        if ($aftField == "TESTDIV") {
            $query .= "     AND T1.{$aftField} NOT IN ('4') ";
            $query .= "     AND T1.{$aftField} IN ({$model->courseTestDiv}) ";
        }
        if ($aftField == "RUIKEI_DIV") {
            $query .= "     AND T1.TESTDIV = '".$model->field["TESTDIV"]."' ";
            $query .= "     AND T1.TESTDIV2 = '".$model->field["TESTDIV2"]."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //TITLEデータ
    function getTitleQuery($model) {

        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     AFT_SINRO_ADDITION_L350W_FIELD_DAT ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$model->field["YEAR"]."' ";
        $query .= "     AND APPLICANTDIV = '".$model->field["APPLICANTDIV"]."' ";
        $query .= "     AND TESTDIV = '".$model->field["TESTDIV"]."' ";
        $query .= "     AND RUIKEI_DIV = '".$model->field["RUIKEI_DIV"]."' ";
        $query .= "     AND TESTDIV2 = '".$model->field["TESTDIV2"]."' ";
        $query .= " ORDER BY ";
        $query .= "     LARGE_DIV ";

        return $query;
    }

    //学科コース
    function getCMC($model) {

        $query  = " WITH TOTAL_T (COURSECD, MAJORCD, COURSECODE, CMC_NAME) AS ( ";
        $query .= "     VALUES('9', '999', '9999', '合計') ";
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     W1.COURSECD , ";
        $query .= "     W1.MAJORCD, ";
        $query .= "     W1.EXAMCOURSECD AS COURSECODE, ";
        $query .= "     T1.MAJORNAME || W1.EXAMCOURSE_NAME AS CMC_NAME ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_COURSE_MST W1 ";
        $query .= "     LEFT JOIN V_MAJOR_MST T1 ON W1.ENTEXAMYEAR = T1.YEAR ";
        $query .= "         AND W1.COURSECD = T1.COURSECD ";
        $query .= "         AND W1.MAJORCD = T1.MAJORCD ";
        $query .= " WHERE ";
        $query .= "     W1.ENTEXAMYEAR = '{$model->field["YEAR"]}' ";
        $query .= "     AND W1.APPLICANTDIV = '{$model->field["APPLICANTDIV"]}' ";
        $query .= "     AND W1.TESTDIV = '{$model->field["TESTDIV"]}' ";
        $query .= " UNION ";
        $query .= " SELECT ";
        $query .= "     COURSECD , ";
        $query .= "     MAJORCD, ";
        $query .= "     COURSECODE, ";
        $query .= "     CMC_NAME ";
        $query .= " FROM ";
        $query .= "     TOTAL_T ";
        $query .= " ORDER BY ";
        $query .= "     COURSECD , ";
        $query .= "     MAJORCD, ";
        $query .= "     COURSECODE ";

        return $query;
    }

    //表示データ
    function getReadQuery($model, $cmc) {

        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     V_AFT_SINRO_ADDITION_L350W_DAT ";
        $query .= " WHERE ";
        $query .= "     EDBOARD_SCHOOLCD = '".$model->schoolcd."' ";
        $query .= "     AND YEAR = '".$model->field["YEAR"]."' ";
        $query .= "     AND APPLICANTDIV = '".$model->field["APPLICANTDIV"]."' ";
        $query .= "     AND TESTDIV = '".$model->field["TESTDIV"]."' ";
        $query .= "     AND RUIKEI_DIV = '".$model->field["RUIKEI_DIV"]."' ";
        $query .= "     AND TESTDIV2 = '".$model->field["TESTDIV2"]."' ";
        $query .= "     AND COURSECD = '".$cmc["COURSECD"]."' ";
        $query .= "     AND MAJORCD = '".$cmc["MAJORCD"]."' ";
        $query .= "     AND COURSECODE = '".$cmc["COURSECODE"]."' ";

        return $query;
    }

    //入学定員/募集人数
    function getCapacityQuery($model, $cmc) {

        $query .= " SELECT ";
        $query .= "     T1.CAPACITY AS NYUUGAKU_TEIIN, ";
        $query .= "     T1.CAPACITY2 AS BOSYUU_TEIIN ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_COURSE_MST T1 ";
        $query .= " WHERE ";
        $query .= "     T1.ENTEXAMYEAR = '{$model->field["YEAR"]}' ";
        $query .= "     AND T1.APPLICANTDIV = '{$model->field["APPLICANTDIV"]}' ";
        $query .= "     AND T1.TESTDIV = '{$model->field["TESTDIV"]}' ";
        $query .= "     AND T1.COURSECD = '{$cmc["COURSECD"]}' ";
        $query .= "     AND T1.MAJORCD = '{$cmc["MAJORCD"]}' ";
        $query .= "     AND T1.EXAMCOURSECD = '{$cmc["COURSECODE"]}' ";

        return $query;
    }

    //表示データ
    function getRecalcQuery($model, $cmc) {

        $query .= " WITH MAIN_T AS ( ";
        $query .= " SELECT ";
        $query .= "     T1.* ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_COURSE_MST T1 ";
        $query .= " WHERE ";
        $query .= "     T1.ENTEXAMYEAR = '{$model->field["YEAR"]}' ";
        $query .= "     AND T1.APPLICANTDIV = '{$model->field["APPLICANTDIV"]}' ";
        $query .= "     AND T1.TESTDIV = '{$model->field["TESTDIV"]}' ";
        $query .= "     AND T1.COURSECD = '{$cmc["COURSECD"]}' ";
        $query .= "     AND T1.MAJORCD = '{$cmc["MAJORCD"]}' ";
        $query .= "     AND T1.EXAMCOURSECD = '{$cmc["COURSECODE"]}' ";
        $query .= " ), TAISYOU AS ( ";
        $query .= " SELECT ";
        $query .= "     VBASE.* ";
        $query .= " FROM ";
        $query .= "     V_ENTEXAM_APPLICANTBASE_DAT VBASE ";
        $query .= " WHERE ";
        $query .= "     VBASE.ENTEXAMYEAR = '{$model->field["YEAR"]}' ";
        $query .= "     AND VBASE.APPLICANTDIV = '{$model->field["APPLICANTDIV"]}' ";
        if ($model->field["TESTDIV"] == "1") {
            $query .= "     AND VBASE.TESTDIV IN ('1', '4') ";
        } else {
            $query .= "     AND VBASE.TESTDIV = '{$model->field["TESTDIV"]}' ";
        }
        //2018/02/07 濱崎修正　追検査は5:後期選抜、8:秋季選抜の時のみで、1:前期選抜、2:中高一貫、3:特別選抜の追検査は合算する
        if ($model->field["TESTDIV"] == "5" || $model->field["TESTDIV"] == "8") {
            $query .= "     AND VALUE(VBASE.TESTDIV2, '0') = '{$model->field["TESTDIV2"]}' ";
        }
        if ($model->field["RUIKEI_DIV"] == "02" || $model->field["RUIKEI_DIV"] == "03" || $model->field["RUIKEI_DIV"] == "05") {
            $query .= "     AND VALUE(VBASE.JUDGEMENT, '0') <> '5' ";
        }
        if ($model->field["RUIKEI_DIV"] == "03") {
            $query .= "     AND VALUE(VBASE.JUDGEMENT, '0') <> '4' ";
        } else if ($model->field["RUIKEI_DIV"] == "04" || $model->field["RUIKEI_DIV"] == "05") {
            $query .= "     AND VBASE.JUDGEMENT IN (SELECT NAMECD2 FROM NAME_MST WHERE NAMECD1 = 'L013' AND NAMESPARE1 = '1') ";
        }
        //TAISYOUとの違いは、TESTDIVを条件に含めない
        $query .= " ), GOUKAKU_TAISYOU AS ( ";
        $query .= " SELECT ";
        $query .= "     VBASE.* ";
        $query .= " FROM ";
        $query .= "     V_ENTEXAM_APPLICANTBASE_DAT VBASE ";
        $query .= " WHERE ";
        $query .= "     VBASE.ENTEXAMYEAR = '{$model->field["YEAR"]}' ";
        $query .= "     AND VBASE.APPLICANTDIV = '{$model->field["APPLICANTDIV"]}' ";
        //2018/02/07 濱崎修正　追検査は5:後期選抜、8:秋季選抜の時のみで、1:前期選抜、2:中高一貫、3:特別選抜の追検査は合算する
        if ($model->field["TESTDIV"] == "5" || $model->field["TESTDIV"] == "8") {
            $query .= "     AND VALUE(VBASE.TESTDIV2, '0') = '{$model->field["TESTDIV2"]}' ";
        }
        if ($model->field["RUIKEI_DIV"] == "03") {
            $query .= "     AND VALUE(VBASE.JUDGEMENT, '0') <> '4' ";
        } else if ($model->field["RUIKEI_DIV"] == "04" || $model->field["RUIKEI_DIV"] == "05") {
            $query .= "     AND VBASE.JUDGEMENT IN (SELECT NAMECD2 FROM NAME_MST WHERE NAMECD1 = 'L013' AND NAMESPARE1 = '1') ";
        }
        //2018/03/15 濱崎修正　5:後期選抜の時、累計区分=05（合格者数）のときは「合格者総数」には前期選抜の追検査(TESTDIV2=1)の合格者も含めるようする
        if ($model->field["TESTDIV"] == "5" && $model->field["RUIKEI_DIV"] == "05") {
            $query .= " UNION ";
            $query .= " SELECT ";
            $query .= "     VBASE.* ";
            $query .= " FROM ";
            $query .= "     V_ENTEXAM_APPLICANTBASE_DAT VBASE ";
            $query .= " WHERE ";
            $query .= "     VBASE.ENTEXAMYEAR = '{$model->field["YEAR"]}' ";
            $query .= "     AND VBASE.APPLICANTDIV = '{$model->field["APPLICANTDIV"]}' ";
            $query .= "     AND VBASE.TESTDIV IN ('1', '2', '3', '4') ";
            $query .= "     AND VALUE(VBASE.TESTDIV2, '0') = '1' ";
            $query .= "     AND VBASE.JUDGEMENT IN (SELECT NAMECD2 FROM NAME_MST WHERE NAMECD1 = 'L013' AND NAMESPARE1 = '1') ";
        }
        $query .= " ), SIGAN AS ( ";
        $query .= " SELECT ";
        $query .= "     SUM(CASE WHEN SEX = '1' THEN 1 ELSE 0 END) AS SIGAN_M, ";
        $query .= "     SUM(CASE WHEN SEX = '2' THEN 1 ELSE 0 END) AS SIGAN_W, ";
        $query .= "     SUM(CASE WHEN SEX = '9' THEN 1 ELSE 0 END) AS SIGAN_K, ";
        $query .= "     COUNT(*) AS SIGAN_T ";
        $query .= " FROM ";
        $query .= "     TAISYOU ";
        $query .= " WHERE ";
        if ($model->field["RUIKEI_DIV"] == "04" || $model->field["RUIKEI_DIV"] == "05") {
            $query .= "     SUC_COURSECD = '{$cmc["COURSECD"]}' ";
            $query .= "     AND SUC_MAJORCD = '{$cmc["MAJORCD"]}' ";
            $query .= "     AND SUC_COURSECODE = '{$cmc["COURSECODE"]}' ";
        } else {
            $query .= "     DAI1_COURSECD = '{$cmc["COURSECD"]}' ";
            $query .= "     AND DAI1_MAJORCD = '{$cmc["MAJORCD"]}' ";
            $query .= "     AND DAI1_COURSECODE = '{$cmc["COURSECODE"]}' ";
        }
        $query .= " ), OBOEGAKI AS ( ";
        $query .= " SELECT ";
        $query .= "     SUM(CASE WHEN SEX = '1' THEN 1 ELSE 0 END) AS OBOEGAKI_M, ";
        $query .= "     SUM(CASE WHEN SEX = '2' THEN 1 ELSE 0 END) AS OBOEGAKI_W, ";
        $query .= "     SUM(CASE WHEN SEX = '9' THEN 1 ELSE 0 END) AS OBOEGAKI_K, ";
        $query .= "     COUNT(*) AS OBOEGAKI_T ";
        $query .= " FROM ";
        $query .= "     TAISYOU ";
        $query .= " WHERE ";
        //2018/03/16 濱崎修正　累計区分=04,05(合格内数、合格者数)の時、合格コースを母集団とする
        if ($model->field["RUIKEI_DIV"] == "04" || $model->field["RUIKEI_DIV"] == "05") {
            $query .= "     SUC_COURSECD = '{$cmc["COURSECD"]}' ";
            $query .= "     AND SUC_MAJORCD = '{$cmc["MAJORCD"]}' ";
            $query .= "     AND SUC_COURSECODE = '{$cmc["COURSECODE"]}' ";
        } else {
            $query .= "     DAI1_COURSECD = '{$cmc["COURSECD"]}' ";
            $query .= "     AND DAI1_MAJORCD = '{$cmc["MAJORCD"]}' ";
            $query .= "     AND DAI1_COURSECODE = '{$cmc["COURSECODE"]}' ";
        }
        $query .= "     AND OBOEGAKI_SIGANSYA = '1' ";
        $query .= " ), KAIGAI AS ( ";
        $query .= " SELECT ";
        $query .= "     SUM(CASE WHEN SEX = '1' THEN 1 ELSE 0 END) AS KAIGAI_M, ";
        $query .= "     SUM(CASE WHEN SEX = '2' THEN 1 ELSE 0 END) AS KAIGAI_W, ";
        $query .= "     SUM(CASE WHEN SEX = '9' THEN 1 ELSE 0 END) AS KAIGAI_K, ";
        $query .= "     COUNT(*) AS KAIGAI_T ";
        $query .= " FROM ";
        $query .= "     TAISYOU ";
        $query .= " WHERE ";
        //2018/03/16 濱崎修正　累計区分=04,05(合格内数、合格者数)の時、合格コースを母集団とする
        if ($model->field["RUIKEI_DIV"] == "04" || $model->field["RUIKEI_DIV"] == "05") {
            $query .= "     SUC_COURSECD = '{$cmc["COURSECD"]}' ";
            $query .= "     AND SUC_MAJORCD = '{$cmc["MAJORCD"]}' ";
            $query .= "     AND SUC_COURSECODE = '{$cmc["COURSECODE"]}' ";
        } else {
            $query .= "     DAI1_COURSECD = '{$cmc["COURSECD"]}' ";
            $query .= "     AND DAI1_MAJORCD = '{$cmc["MAJORCD"]}' ";
            $query .= "     AND DAI1_COURSECODE = '{$cmc["COURSECODE"]}' ";
        }
        $query .= "     AND KAIGAI_KIKOKUSEI_NADO = '1' ";
        $query .= " ), KISOTSU AS ( ";
        $query .= " SELECT ";
        $query .= "     SUM(CASE WHEN SEX = '1' THEN 1 ELSE 0 END) AS KISOTSU_M, ";
        $query .= "     SUM(CASE WHEN SEX = '2' THEN 1 ELSE 0 END) AS KISOTSU_W, ";
        $query .= "     SUM(CASE WHEN SEX = '9' THEN 1 ELSE 0 END) AS KISOTSU_K, ";
        $query .= "     COUNT(*) AS KISOTSU_T ";
        $query .= " FROM ";
        $query .= "     TAISYOU ";
        $query .= " WHERE ";
        //2018/03/16 濱崎修正　累計区分=04,05(合格内数、合格者数)の時、合格コースを母集団とする
        if ($model->field["RUIKEI_DIV"] == "04" || $model->field["RUIKEI_DIV"] == "05") {
            $query .= "     SUC_COURSECD = '{$cmc["COURSECD"]}' ";
            $query .= "     AND SUC_MAJORCD = '{$cmc["MAJORCD"]}' ";
            $query .= "     AND SUC_COURSECODE = '{$cmc["COURSECODE"]}' ";
        } else {
            $query .= "     DAI1_COURSECD = '{$cmc["COURSECD"]}' ";
            $query .= "     AND DAI1_MAJORCD = '{$cmc["MAJORCD"]}' ";
            $query .= "     AND DAI1_COURSECODE = '{$cmc["COURSECODE"]}' ";
        }
        $query .= "     AND FS_GRDDIV = '2' ";
        $query .= " ), SEIJIN AS ( ";
        $query .= " SELECT ";
        $query .= "     SUM(CASE WHEN SEX = '1' THEN 1 ELSE 0 END) AS SEIJIN_M, ";
        $query .= "     SUM(CASE WHEN SEX = '2' THEN 1 ELSE 0 END) AS SEIJIN_W, ";
        $query .= "     SUM(CASE WHEN SEX = '9' THEN 1 ELSE 0 END) AS SEIJIN_K, ";
        $query .= "     COUNT(*) AS SEIJIN_T ";
        $query .= " FROM ";
        $query .= "     TAISYOU ";
        $query .= " WHERE ";
        //2018/03/16 濱崎修正　累計区分=04,05(合格内数、合格者数)の時、合格コースを母集団とする
        if ($model->field["RUIKEI_DIV"] == "04" || $model->field["RUIKEI_DIV"] == "05") {
            $query .= "     SUC_COURSECD = '{$cmc["COURSECD"]}' ";
            $query .= "     AND SUC_MAJORCD = '{$cmc["MAJORCD"]}' ";
            $query .= "     AND SUC_COURSECODE = '{$cmc["COURSECODE"]}' ";
        } else {
            $query .= "     DAI1_COURSECD = '{$cmc["COURSECD"]}' ";
            $query .= "     AND DAI1_MAJORCD = '{$cmc["MAJORCD"]}' ";
            $query .= "     AND DAI1_COURSECODE = '{$cmc["COURSECODE"]}' ";
        }
        $query .= "     AND CASE WHEN BIRTHDAY IS NOT NULL THEN YEAR('{$model->field["YEAR"]}-04-01' - BIRTHDAY) END >= 20 ";
        $query .= " ), TSUUGAKUGAI AS ( ";
        $query .= " SELECT ";
        $query .= "     SUM(CASE WHEN SEX = '1' THEN 1 ELSE 0 END) AS TSUUGAKUGAI_M, ";
        $query .= "     SUM(CASE WHEN SEX = '2' THEN 1 ELSE 0 END) AS TSUUGAKUGAI_W, ";
        $query .= "     SUM(CASE WHEN SEX = '9' THEN 1 ELSE 0 END) AS TSUUGAKUGAI_K, ";
        $query .= "     COUNT(*) AS TSUUGAKUGAI_T ";
        $query .= " FROM ";
        $query .= "     TAISYOU ";
        $query .= " WHERE ";
        //2018/03/16 濱崎修正　累計区分=04,05(合格内数、合格者数)の時、合格コースを母集団とする
        if ($model->field["RUIKEI_DIV"] == "04" || $model->field["RUIKEI_DIV"] == "05") {
            $query .= "     SUC_COURSECD = '{$cmc["COURSECD"]}' ";
            $query .= "     AND SUC_MAJORCD = '{$cmc["MAJORCD"]}' ";
            $query .= "     AND SUC_COURSECODE = '{$cmc["COURSECODE"]}' ";
        } else {
            $query .= "     DAI1_COURSECD = '{$cmc["COURSECD"]}' ";
            $query .= "     AND DAI1_MAJORCD = '{$cmc["MAJORCD"]}' ";
            $query .= "     AND DAI1_COURSECODE = '{$cmc["COURSECODE"]}' ";
        }
        $query .= "     AND TUUGAKU_KUIKIGAI_KYOKA = '1' ";
        $query .= " ), SPORTTOKU AS ( ";
        $query .= " SELECT ";
        $query .= "     SUM(CASE WHEN SEX = '1' THEN 1 ELSE 0 END) AS SPORTTOKU_M, ";
        $query .= "     SUM(CASE WHEN SEX = '2' THEN 1 ELSE 0 END) AS SPORTTOKU_W, ";
        $query .= "     SUM(CASE WHEN SEX = '9' THEN 1 ELSE 0 END) AS SPORTTOKU_K, ";
        $query .= "     COUNT(*) AS SPORTTOKU_T ";
        $query .= " FROM ";
        $query .= "     TAISYOU ";
        $query .= " WHERE ";
        //2018/03/16 濱崎修正　累計区分=04,05(合格内数、合格者数)の時、合格コースを母集団とする
        if ($model->field["RUIKEI_DIV"] == "04" || $model->field["RUIKEI_DIV"] == "05") {
            $query .= "     SUC_COURSECD = '{$cmc["COURSECD"]}' ";
            $query .= "     AND SUC_MAJORCD = '{$cmc["MAJORCD"]}' ";
            $query .= "     AND SUC_COURSECODE = '{$cmc["COURSECODE"]}' ";
        } else {
            $query .= "     DAI1_COURSECD = '{$cmc["COURSECD"]}' ";
            $query .= "     AND DAI1_MAJORCD = '{$cmc["MAJORCD"]}' ";
            $query .= "     AND DAI1_COURSECODE = '{$cmc["COURSECODE"]}' ";
        }
        $query .= "     AND TESTDIV = '4' ";
        if ($model->field["RUIKEI_DIV"] == "04" || $model->field["RUIKEI_DIV"] == "05") {
            $query .= "     AND JUDGEMENT = '1' ";
        }
        $query .= " ), GOUKAKU AS ( ";
        $query .= " SELECT ";
        $query .= "     SUM(CASE WHEN SEX = '1' THEN 1 ELSE 0 END) AS GOUKAKU_M, ";
        $query .= "     SUM(CASE WHEN SEX = '2' THEN 1 ELSE 0 END) AS GOUKAKU_W, ";
        $query .= "     SUM(CASE WHEN SEX = '9' THEN 1 ELSE 0 END) AS GOUKAKU_K, ";
        $query .= "     COUNT(*) AS GOUKAKU_T ";
        $query .= " FROM ";
        $query .= "     GOUKAKU_TAISYOU ";
        $query .= " WHERE ";
        $query .= "     SUC_COURSECD = '{$cmc["COURSECD"]}' ";
        $query .= "     AND SUC_MAJORCD = '{$cmc["MAJORCD"]}' ";
        $query .= "     AND SUC_COURSECODE = '{$cmc["COURSECODE"]}' ";
        $query .= "     AND JUDGEMENT IN (SELECT NAMECD2 FROM NAME_MST WHERE NAMECD1 = 'L013' AND NAMESPARE1 = '1') ";
        $query .= " ), HOSHOUNIN AS ( ";
        $query .= " SELECT ";
        $query .= "     SUM(CASE WHEN SEX = '1' THEN 1 ELSE 0 END) AS HOSHOUNIN_M, ";
        $query .= "     SUM(CASE WHEN SEX = '2' THEN 1 ELSE 0 END) AS HOSHOUNIN_W, ";
        $query .= "     SUM(CASE WHEN SEX = '9' THEN 1 ELSE 0 END) AS HOSHOUNIN_K, ";
        $query .= "     COUNT(*) AS HOSHOUNIN_T ";
        $query .= " FROM ";
        $query .= "     TAISYOU ";
        $query .= " WHERE ";
        //2018/03/16 濱崎修正　累計区分=04,05(合格内数、合格者数)の時、合格コースを母集団とする
        if ($model->field["RUIKEI_DIV"] == "04" || $model->field["RUIKEI_DIV"] == "05") {
            $query .= "     SUC_COURSECD = '{$cmc["COURSECD"]}' ";
            $query .= "     AND SUC_MAJORCD = '{$cmc["MAJORCD"]}' ";
            $query .= "     AND SUC_COURSECODE = '{$cmc["COURSECODE"]}' ";
        } else {
            $query .= "     DAI1_COURSECD = '{$cmc["COURSECD"]}' ";
            $query .= "     AND DAI1_MAJORCD = '{$cmc["MAJORCD"]}' ";
            $query .= "     AND DAI1_COURSECODE = '{$cmc["COURSECODE"]}' ";
        }
        $query .= "     AND HOSHOUNIN_TODOKE = '1' ";
        $query .= " ), INNER_TO_OUT_J AS ( ";
        $query .= " SELECT ";
        $query .= "     SUM(CASE WHEN SEX = '1' THEN 1 ELSE 0 END) AS INNER_TO_OUT_J_M, ";
        $query .= "     SUM(CASE WHEN SEX = '2' THEN 1 ELSE 0 END) AS INNER_TO_OUT_J_W, ";
        $query .= "     SUM(CASE WHEN SEX = '9' THEN 1 ELSE 0 END) AS INNER_TO_OUT_J_K, ";
        $query .= "     COUNT(*) AS INNER_TO_OUT_J_T ";
        $query .= " FROM ";
        $query .= "     TAISYOU ";
        $query .= " WHERE ";
        if ($model->field["RUIKEI_DIV"] == "04" || $model->field["RUIKEI_DIV"] == "05") {
            $query .= "     SUC_COURSECD = '{$cmc["COURSECD"]}' ";
            $query .= "     AND SUC_MAJORCD = '{$cmc["MAJORCD"]}' ";
            $query .= "     AND SUC_COURSECODE = '{$cmc["COURSECODE"]}' ";
        } else {
            $query .= "     DAI1_COURSECD = '{$cmc["COURSECD"]}' ";
            $query .= "     AND DAI1_MAJORCD = '{$cmc["MAJORCD"]}' ";
            $query .= "     AND DAI1_COURSECODE = '{$cmc["COURSECODE"]}' ";
        }
        $query .= "     AND KENGAI_CHUUGAKKOU_SHUSSHIN = '1' ";
        if ($model->field["RUIKEI_DIV"] == "04" || $model->field["RUIKEI_DIV"] == "05") {
            $query .= "     AND JUDGEMENT = '1' ";
        }
        $query .= " ), TOKUREI3 AS ( ";
        $query .= " SELECT ";
        $query .= "     SUM(CASE WHEN SEX = '1' THEN 1 ELSE 0 END) AS TOKUREI3_M, ";
        $query .= "     SUM(CASE WHEN SEX = '2' THEN 1 ELSE 0 END) AS TOKUREI3_W, ";
        $query .= "     SUM(CASE WHEN SEX = '9' THEN 1 ELSE 0 END) AS TOKUREI3_K, ";
        $query .= "     COUNT(*) AS TOKUREI3_T ";
        $query .= " FROM ";
        $query .= "     TAISYOU ";
        $query .= " WHERE ";
        //2018/03/16 濱崎修正　累計区分=04,05(合格内数、合格者数)の時、合格コースを母集団とする
        if ($model->field["RUIKEI_DIV"] == "04" || $model->field["RUIKEI_DIV"] == "05") {
            $query .= "     SUC_COURSECD = '{$cmc["COURSECD"]}' ";
            $query .= "     AND SUC_MAJORCD = '{$cmc["MAJORCD"]}' ";
            $query .= "     AND SUC_COURSECODE = '{$cmc["COURSECODE"]}' ";
        } else {
            $query .= "     DAI1_COURSECD = '{$cmc["COURSECD"]}' ";
            $query .= "     AND DAI1_MAJORCD = '{$cmc["MAJORCD"]}' ";
            $query .= "     AND DAI1_COURSECODE = '{$cmc["COURSECODE"]}' ";
        }
        $query .= "     AND KENGAI_CHUUGAKKOU_SHUSSHIN2 = '1' ";
        if ($model->field["RUIKEI_DIV"] == "04" || $model->field["RUIKEI_DIV"] == "05") {
            $query .= "     AND JUDGEMENT = '1' ";
        }
        $query .= " ), OUTER_DAT AS ( ";
        $query .= " SELECT ";
        $query .= "     SUM(CASE WHEN SEX = '1' THEN 1 ELSE 0 END) AS OUTER_M, ";
        $query .= "     SUM(CASE WHEN SEX = '2' THEN 1 ELSE 0 END) AS OUTER_W, ";
        $query .= "     SUM(CASE WHEN SEX = '9' THEN 1 ELSE 0 END) AS OUTER_K, ";
        $query .= "     COUNT(*) AS OUTER_T ";
        $query .= " FROM ";
        $query .= "     TAISYOU ";
        $query .= " WHERE ";
        //2018/03/16 濱崎修正　累計区分=04,05(合格内数、合格者数)の時、合格コースを母集団とする
        if ($model->field["RUIKEI_DIV"] == "04" || $model->field["RUIKEI_DIV"] == "05") {
            $query .= "     SUC_COURSECD = '{$cmc["COURSECD"]}' ";
            $query .= "     AND SUC_MAJORCD = '{$cmc["MAJORCD"]}' ";
            $query .= "     AND SUC_COURSECODE = '{$cmc["COURSECODE"]}' ";
        } else {
            $query .= "     DAI1_COURSECD = '{$cmc["COURSECD"]}' ";
            $query .= "     AND DAI1_MAJORCD = '{$cmc["MAJORCD"]}' ";
            $query .= "     AND DAI1_COURSECODE = '{$cmc["COURSECODE"]}' ";
        }
        $query .= "     AND KENGAI_ZAIJUU = '1' ";
        if ($model->field["RUIKEI_DIV"] == "04" || $model->field["RUIKEI_DIV"] == "05") {
            $query .= "     AND JUDGEMENT IN (SELECT NAMECD2 FROM NAME_MST WHERE NAMECD1 = 'L013' AND NAMESPARE1 = '1') ";
        }
        $query .= " ) ";
        $query .= " SELECT ";
        $query .= "     T1.CAPACITY AS NYUUGAKU_TEIIN, ";
        $query .= "     T1.CAPACITY2 AS BOSYUU_TEIIN, ";
        $query .= "     SIGAN_M, ";
        $query .= "     SIGAN_W, ";
        $query .= "     SIGAN_K, ";
        $query .= "     SIGAN_T, ";
        $query .= "     OBOEGAKI_M, ";
        $query .= "     OBOEGAKI_W, ";
        $query .= "     OBOEGAKI_K, ";
        $query .= "     OBOEGAKI_T, ";
        $query .= "     KAIGAI_M, ";
        $query .= "     KAIGAI_W, ";
        $query .= "     KAIGAI_K, ";
        $query .= "     KAIGAI_T, ";
        $query .= "     KISOTSU_M, ";
        $query .= "     KISOTSU_W, ";
        $query .= "     KISOTSU_K, ";
        $query .= "     KISOTSU_T, ";
        $query .= "     SEIJIN_M, ";
        $query .= "     SEIJIN_W, ";
        $query .= "     SEIJIN_K, ";
        $query .= "     SEIJIN_T, ";
        $query .= "     TSUUGAKUGAI_M, ";
        $query .= "     TSUUGAKUGAI_W, ";
        $query .= "     TSUUGAKUGAI_K, ";
        $query .= "     TSUUGAKUGAI_T, ";
        $query .= "     SPORTTOKU_M, ";
        $query .= "     SPORTTOKU_W, ";
        $query .= "     SPORTTOKU_K, ";
        $query .= "     SPORTTOKU_T, ";
        $query .= "     GOUKAKU_M, ";
        $query .= "     GOUKAKU_W, ";
        $query .= "     GOUKAKU_K, ";
        $query .= "     GOUKAKU_T, ";
        $query .= "     HOSHOUNIN_M, ";
        $query .= "     HOSHOUNIN_W, ";
        $query .= "     HOSHOUNIN_K, ";
        $query .= "     HOSHOUNIN_T, ";
        $query .= "     INNER_TO_OUT_J_M, ";
        $query .= "     INNER_TO_OUT_J_W, ";
        $query .= "     INNER_TO_OUT_J_K, ";
        $query .= "     INNER_TO_OUT_J_T, ";
        $query .= "     TOKUREI3_M, ";
        $query .= "     TOKUREI3_W, ";
        $query .= "     TOKUREI3_K, ";
        $query .= "     TOKUREI3_T, ";
        $query .= "     OUTER_M, ";
        $query .= "     OUTER_W, ";
        $query .= "     OUTER_K, ";
        $query .= "     OUTER_T ";
        $query .= " FROM ";
        $query .= "     MAIN_T T1, ";
        $query .= "     SIGAN, ";
        $query .= "     OBOEGAKI, ";
        $query .= "     KAIGAI, ";
        $query .= "     KISOTSU, ";
        $query .= "     SEIJIN, ";
        $query .= "     TSUUGAKUGAI, ";
        $query .= "     SPORTTOKU, ";
        $query .= "     GOUKAKU, ";
        $query .= "     HOSHOUNIN, ";
        $query .= "     INNER_TO_OUT_J, ";
        $query .= "     TOKUREI3, ";
        $query .= "     OUTER_DAT ";

        return $query;
    }

    //UPDATE
    function &getUpdateQuery($model) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //DELETE
        $query  = "DELETE FROM AFT_SINRO_ADDITION_L350W_DAT ";
        $query .= " WHERE EDBOARD_SCHOOLCD  = '".$model->schoolcd."'";
        $query .= "   AND YEAR          = '".$model->field["YEAR"]."'";
        $query .= "   AND APPLICANTDIV  = '".$model->field["APPLICANTDIV"]."'";
        $query .= "   AND TESTDIV       = '".$model->field["TESTDIV"]."'";
        $query .= "   AND RUIKEI_DIV    = '".$model->field["RUIKEI_DIV"]."'";
        $query .= "   AND TESTDIV2      = '".$model->field["TESTDIV2"]."'";
        $db->query($query);

        $setKeyData["NYUUGAKU_TEIIN"]   = array("LARGE_DIV" => "98", "SEX" => "9");
        $setKeyData["BOSYUU_TEIIN"]     = array("LARGE_DIV" => "99", "SEX" => "9");
        $setKeyData["SIGAN_M"]          = array("LARGE_DIV" => "01", "SEX" => "1");
        $setKeyData["SIGAN_W"]          = array("LARGE_DIV" => "01", "SEX" => "2");
        $setKeyData["SIGAN_K"]          = array("LARGE_DIV" => "01", "SEX" => "0");
        $setKeyData["SIGAN_T"]          = array("LARGE_DIV" => "01", "SEX" => "9");
        $setKeyData["OBOEGAKI_M"]       = array("LARGE_DIV" => "02", "SEX" => "1");
        $setKeyData["OBOEGAKI_W"]       = array("LARGE_DIV" => "02", "SEX" => "2");
        $setKeyData["OBOEGAKI_K"]       = array("LARGE_DIV" => "02", "SEX" => "0");
        $setKeyData["OBOEGAKI_T"]       = array("LARGE_DIV" => "02", "SEX" => "9");
        $setKeyData["KAIGAI_M"]         = array("LARGE_DIV" => "03", "SEX" => "1");
        $setKeyData["KAIGAI_W"]         = array("LARGE_DIV" => "03", "SEX" => "2");
        $setKeyData["KAIGAI_K"]         = array("LARGE_DIV" => "03", "SEX" => "0");
        $setKeyData["KAIGAI_T"]         = array("LARGE_DIV" => "03", "SEX" => "9");
        $setKeyData["KISOTSU_M"]        = array("LARGE_DIV" => "04", "SEX" => "1");
        $setKeyData["KISOTSU_W"]        = array("LARGE_DIV" => "04", "SEX" => "2");
        $setKeyData["KISOTSU_K"]        = array("LARGE_DIV" => "04", "SEX" => "0");
        $setKeyData["KISOTSU_T"]        = array("LARGE_DIV" => "04", "SEX" => "9");
        $setKeyData["SEIJIN_M"]         = array("LARGE_DIV" => "05", "SEX" => "1");
        $setKeyData["SEIJIN_W"]         = array("LARGE_DIV" => "05", "SEX" => "2");
        $setKeyData["SEIJIN_K"]         = array("LARGE_DIV" => "05", "SEX" => "0");
        $setKeyData["SEIJIN_T"]         = array("LARGE_DIV" => "05", "SEX" => "9");
        $setKeyData["TSUUGAKUGAI_M"]    = array("LARGE_DIV" => "06", "SEX" => "1");
        $setKeyData["TSUUGAKUGAI_W"]    = array("LARGE_DIV" => "06", "SEX" => "2");
        $setKeyData["TSUUGAKUGAI_K"]    = array("LARGE_DIV" => "06", "SEX" => "0");
        $setKeyData["TSUUGAKUGAI_T"]    = array("LARGE_DIV" => "06", "SEX" => "9");
        $setKeyData["SPORTTOKU_M"]      = array("LARGE_DIV" => "07", "SEX" => "1");
        $setKeyData["SPORTTOKU_W"]      = array("LARGE_DIV" => "07", "SEX" => "2");
        $setKeyData["SPORTTOKU_K"]      = array("LARGE_DIV" => "07", "SEX" => "0");
        $setKeyData["SPORTTOKU_T"]      = array("LARGE_DIV" => "07", "SEX" => "9");
        $setKeyData["GOUKAKU_M"]        = array("LARGE_DIV" => "08", "SEX" => "1");
        $setKeyData["GOUKAKU_W"]        = array("LARGE_DIV" => "08", "SEX" => "2");
        $setKeyData["GOUKAKU_K"]        = array("LARGE_DIV" => "08", "SEX" => "0");
        $setKeyData["GOUKAKU_T"]        = array("LARGE_DIV" => "08", "SEX" => "9");
        $setKeyData["HOSHOUNIN_M"]      = array("LARGE_DIV" => "09", "SEX" => "1");
        $setKeyData["HOSHOUNIN_W"]      = array("LARGE_DIV" => "09", "SEX" => "2");
        $setKeyData["HOSHOUNIN_K"]      = array("LARGE_DIV" => "09", "SEX" => "0");
        $setKeyData["HOSHOUNIN_T"]      = array("LARGE_DIV" => "09", "SEX" => "9");
        $setKeyData["INNER_TO_OUT_J_M"] = array("LARGE_DIV" => "10", "SEX" => "1");
        $setKeyData["INNER_TO_OUT_J_W"] = array("LARGE_DIV" => "10", "SEX" => "2");
        $setKeyData["INNER_TO_OUT_J_K"] = array("LARGE_DIV" => "10", "SEX" => "0");
        $setKeyData["INNER_TO_OUT_J_T"] = array("LARGE_DIV" => "10", "SEX" => "9");
        $setKeyData["TOKUREI3_M"]       = array("LARGE_DIV" => "11", "SEX" => "1");
        $setKeyData["TOKUREI3_W"]       = array("LARGE_DIV" => "11", "SEX" => "2");
        $setKeyData["TOKUREI3_K"]       = array("LARGE_DIV" => "11", "SEX" => "0");
        $setKeyData["TOKUREI3_T"]       = array("LARGE_DIV" => "11", "SEX" => "9");
        $setKeyData["OUTER_M"]          = array("LARGE_DIV" => "12", "SEX" => "1");
        $setKeyData["OUTER_W"]          = array("LARGE_DIV" => "12", "SEX" => "2");
        $setKeyData["OUTER_K"]          = array("LARGE_DIV" => "12", "SEX" => "0");
        $setKeyData["OUTER_T"]          = array("LARGE_DIV" => "12", "SEX" => "9");

        foreach ($model->fields as $cmcKey => $cmcVal) {
            list($courseCd, $majorCd, $courseCode) = explode("_", $cmcKey);
            foreach ($cmcVal as $name => $setVal) {
                $data = array();
                $data["EDBOARD_SCHOOLCD"][TEXT]     = $model->schoolcd;
                $data["YEAR"][TEXT]                 = $model->field["YEAR"];
                $data["APPLICANTDIV"][TEXT]         = $model->field["APPLICANTDIV"];
                $data["TESTDIV"][TEXT]              = $model->field["TESTDIV"];
                $data["RUIKEI_DIV"][TEXT]           = $model->field["RUIKEI_DIV"];
                $data["TESTDIV2"][TEXT]             = $model->field["TESTDIV2"];
                $data["COURSECD"][TEXT]             = $courseCd;
                $data["MAJORCD"][TEXT]              = $majorCd;
                $data["COURSECODE"][TEXT]           = $courseCode;
                $data["LARGE_DIV"][TEXT]            = $setKeyData[$name]["LARGE_DIV"];
                $data["SEX"][TEXT]                  = $setKeyData[$name]["SEX"];
                $data["COUNT"][NUMBER]              = $setVal ? $setVal : 0;
                $data["REGISTERCD"][TEXT]           = STAFFCD;
                $data["UPDATED"][NUMBER]            = "sysdate()";

                $query  = Query::insertSQL($data, "AFT_SINRO_ADDITION_L350W_DAT");
                $db->query($query);
            }
            $this->fields[$setCmc][$name] = $totalVal;
        }
        $db->commit();
        Query::dbCheckIn($db);
    }

    //県への報告履歴コンボ
    function getReport($model) {
        $query  = " SELECT ";
        $query .= "     T1.EXECUTE_DATE, ";
        $query .= "     T1.APPLICANTDIV, ";
        $query .= "     T1.TESTDIV, ";
        $query .= "     T1.RUIKEI_DIV, ";
        $query .= "     T1.TESTDIV2, ";
        $query .= "     T1.EXECUTE_DATE, ";
        $query .= "     STAFF.STAFFNAME, ";
        $query .= "     MAX(T1.FIXED_FLG) AS FIXED_FLG, ";
        $query .= "     L2.NAME1 || '　' || L3.NAME1 ";
        $query .= "            || '　' || CASE WHEN T1.TESTDIV2 = '1' THEN '追検査' ELSE '' END AS LABEL ";
        $query .= " FROM ";
        $query .= "     REPORT_AFT_SINRO_ADDITION_L350W_DAT T1";
        $query .= "     LEFT JOIN NAME_MST L1 ON L1.NAMECD1 = 'L003' ";
        $query .= "          AND L1.NAMECD2 = T1.APPLICANTDIV ";
        $query .= "     LEFT JOIN NAME_MST L2 ON L2.NAMECD1 = 'L004' ";
        $query .= "          AND L2.NAMECD2 = T1.TESTDIV ";
        $query .= "     LEFT JOIN NAME_MST L3 ON L3.NAMECD1 = 'L040' ";
        $query .= "          AND L3.NAMECD2 = T1.RUIKEI_DIV ";
        $query .= "     LEFT JOIN STAFF_MST STAFF ON T1.REGISTERCD = STAFF.STAFFCD ";
        $query .= " WHERE ";
        $query .= "         T1.EDBOARD_SCHOOLCD = '".$model->schoolcd."' ";
        $query .= "     AND T1.YEAR             = '".$model->field["YEAR"]."' ";
        $query .= "     AND T1.APPLICANTDIV     = '".$model->field["APPLICANTDIV"]."' ";
        $query .= "     AND T1.TESTDIV          = '".$model->field["TESTDIV"]."' ";
        $query .= "     AND T1.RUIKEI_DIV       = '".$model->field["RUIKEI_DIV"]."' ";
        $query .= "     AND T1.TESTDIV2         = '".($model->field["TESTDIV2"] ? $model->field["TESTDIV2"] : "0")."' ";
        $query .= " GROUP BY ";
        $query .= "     T1.EXECUTE_DATE, ";
        $query .= "     T1.APPLICANTDIV, ";
        $query .= "     T1.TESTDIV, ";
        $query .= "     T1.RUIKEI_DIV, ";
        $query .= "     T1.TESTDIV2, ";
        $query .= "     T1.EXECUTE_DATE, ";
        $query .= "     STAFF.STAFFNAME, ";
        $query .= "     L2.NAME1 || '　' || L3.NAME1 ";
        $query .= "            || '　' || CASE WHEN T1.TESTDIV2 = '1' THEN '追検査' ELSE '' END ";
        $query .= " ORDER BY ";
        $query .= "     T1.EXECUTE_DATE DESC, ";
        $query .= "     T1.APPLICANTDIV, ";
        $query .= "     T1.TESTDIV, ";
        $query .= "     T1.RUIKEI_DIV, ";
        $query .= "     T1.TESTDIV2 ";

        return $query;
    }

    //学校側から県側へコピーするデータ取得
    function getAddition1Dat($model) {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     AFT_SINRO_ADDITION_L350W_DAT ";
        $query .= " WHERE ";
        $query .= "     EDBOARD_SCHOOLCD = '".$model->schoolcd."' ";
        $query .= "     AND YEAR = '".$model->field["YEAR"]."' ";
        $query .= "     AND APPLICANTDIV = '".$model->field["APPLICANTDIV"]."' ";
        $query .= "     AND TESTDIV = '".$model->field["TESTDIV"]."' ";
        $query .= "     AND RUIKEI_DIV = '".$model->field["RUIKEI_DIV"]."' ";
        $query .= "     AND TESTDIV2 = '".$model->field["TESTDIV2"]."' ";

        return $query;
    }

    function getAddition2Dat($model) {
        $query  = " SELECT DISTINCT";
        $query .= "     EDBOARD_SCHOOLCD, ";
        $query .= "     YEAR, ";
        $query .= "     APPLICANTDIV, ";
        $query .= "     TESTDIV, ";
        $query .= "     RUIKEI_DIV, ";
        $query .= "     TESTDIV2, ";
        $query .= "     COURSECD, ";
        $query .= "     MAJORCD, ";
        $query .= "     COURSECODE ";
        $query .= " FROM ";
        $query .= "     AFT_SINRO_ADDITION_L350W_DAT ";
        $query .= " WHERE ";
        $query .= "     EDBOARD_SCHOOLCD = '".$model->schoolcd."' ";
        $query .= "     AND YEAR = '".$model->field["YEAR"]."' ";
        $query .= "     AND APPLICANTDIV = '".$model->field["APPLICANTDIV"]."' ";
        $query .= "     AND TESTDIV = '".$model->field["TESTDIV"]."' ";
        $query .= "     AND RUIKEI_DIV = '".$model->field["RUIKEI_DIV"]."' ";
        $query .= "     AND TESTDIV2 = '".$model->field["TESTDIV2"]."' ";

        return $query;
    }

    //UPDATE
    function &getUpdateReportFixed($model) {
        $db = Query::dbCheckOut();
        $db2 = Query::dbCheckOut2();
        $db->autoCommit(false);
        $db2->autoCommit(false);

        //県側、学校側・報告テーブル
        $query  = "DELETE FROM REPORT_AFT_SINRO_ADDITION_L350W_DAT ";
        $query .= " WHERE EDBOARD_SCHOOLCD  = '".$model->schoolcd."'";
        $query .= "   AND YEAR          = '".$model->field["YEAR"]."'";
        $query .= "   AND APPLICANTDIV  = '".$model->field["APPLICANTDIV"]."'";
        $query .= "   AND TESTDIV       = '".$model->field["TESTDIV"]."'";
        $query .= "   AND RUIKEI_DIV    = '".$model->field["RUIKEI_DIV"]."'";
        $query .= "   AND TESTDIV2      = '".$model->field["TESTDIV2"]."'";
        $db2->query($query);

        //報告用データ取得
        $query = knjl350wQuery::getAddition2Dat($model);
        $result = $db->query($query);
        while ($row2 = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            //県側、学校側・報告テーブル
            $data = array(); //初期化
            $data["EDBOARD_SCHOOLCD"][TEXT]     = $row2["EDBOARD_SCHOOLCD"];
            $data["YEAR"][TEXT]                 = $row2["YEAR"];
            $data["APPLICANTDIV"][TEXT]         = $row2["APPLICANTDIV"];
            $data["TESTDIV"][TEXT]              = $row2["TESTDIV"];
            $data["RUIKEI_DIV"][TEXT]           = $row2["RUIKEI_DIV"];
            $data["TESTDIV2"][TEXT]             = $row2["TESTDIV2"];
            $data["COURSECD"][TEXT]             = $row2["COURSECD"];
            $data["MAJORCD"][TEXT]              = $row2["MAJORCD"];
            $data["COURSECODE"][TEXT]           = $row2["COURSECODE"];
            $data["FIXED_FLG"][TEXT]            = "1";
            $data["REGISTERCD"][TEXT]           = STAFFCD;
            $data["UPDATED"][NUMBER]            =" sysdate()";
            $query  = Query::insertSQL($data, "REPORT_AFT_SINRO_ADDITION_L350W_DAT");
            $db2->query($query);
        }
        $result->free();

        $db->commit();
        $db2->commit();
        Query::dbCheckIn($db);
        Query::dbCheckIn($db2);
        return;
    }

    //UPDATE
    function &getUpdateReport($model) {
        $db = Query::dbCheckOut();
        $db2 = Query::dbCheckOut2();
        $db->autoCommit(false);
        $db2->autoCommit(false);

        //DELETE
        $query  = "DELETE FROM AFT_SINRO_ADDITION_L350W_DAT ";
        $query .= " WHERE EDBOARD_SCHOOLCD  = '".$model->schoolcd."'";
        $query .= "   AND YEAR          = '".$model->field["YEAR"]."'";
        $query .= "   AND APPLICANTDIV  = '".$model->field["APPLICANTDIV"]."'";
        $query .= "   AND TESTDIV       = '".$model->field["TESTDIV"]."'";
        $query .= "   AND RUIKEI_DIV    = '".$model->field["RUIKEI_DIV"]."'";
        $query .= "   AND TESTDIV2      = '".$model->field["TESTDIV2"]."'";
        $db2->query($query);

        //県側、学校側・報告テーブル
        $query  = "DELETE FROM REPORT_AFT_SINRO_ADDITION_L350W_DAT ";
        $query .= " WHERE EDBOARD_SCHOOLCD  = '".$model->schoolcd."'";
        $query .= "   AND YEAR          = '".$model->field["YEAR"]."'";
        $query .= "   AND APPLICANTDIV  = '".$model->field["APPLICANTDIV"]."'";
        $query .= "   AND TESTDIV       = '".$model->field["TESTDIV"]."'";
        $query .= "   AND RUIKEI_DIV    = '".$model->field["RUIKEI_DIV"]."'";
        $query .= "   AND TESTDIV2      = '".$model->field["TESTDIV2"]."'";
        $db2->query($query);

        //INSERT
        //学校側から県側へコピーするデータ取得
        $query = knjl350wQuery::getAddition1Dat($model);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $data = array();
            $data["EDBOARD_SCHOOLCD"][TEXT]     = $row["EDBOARD_SCHOOLCD"];
            $data["YEAR"][TEXT]                 = $row["YEAR"];
            $data["APPLICANTDIV"][TEXT]         = $row["APPLICANTDIV"];
            $data["TESTDIV"][TEXT]              = $row["TESTDIV"];
            $data["RUIKEI_DIV"][TEXT]           = $row["RUIKEI_DIV"];
            $data["TESTDIV2"][TEXT]             = $row["TESTDIV2"];
            $data["COURSECD"][TEXT]             = $row["COURSECD"];
            $data["MAJORCD"][TEXT]              = $row["MAJORCD"];
            $data["COURSECODE"][TEXT]           = $row["COURSECODE"];
            $data["LARGE_DIV"][TEXT]            = $row["LARGE_DIV"];
            $data["SEX"][TEXT]                  = $row["SEX"];
            $data["COUNT"][NUMBER]              = $row["COUNT"];
            $data["REGISTERCD"][TEXT]           = STAFFCD;
            $data["UPDATED"][NUMBER]            = "sysdate()";

            $query  = Query::insertSQL($data, "AFT_SINRO_ADDITION_L350W_DAT");
            $db2->query($query);
        }
        $result->free();

        //報告用データ取得
        $query = knjl350wQuery::getAddition2Dat($model);
        $result = $db->query($query);
        while ($row2 = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            //県側、学校側・報告テーブル
            $data = array(); //初期化
            $data["EDBOARD_SCHOOLCD"][TEXT]     = $row2["EDBOARD_SCHOOLCD"];
            $data["YEAR"][TEXT]                 = $row2["YEAR"];
            $data["APPLICANTDIV"][TEXT]         = $row2["APPLICANTDIV"];
            $data["TESTDIV"][TEXT]              = $row2["TESTDIV"];
            $data["RUIKEI_DIV"][TEXT]           = $row2["RUIKEI_DIV"];
            $data["TESTDIV2"][TEXT]             = $row2["TESTDIV2"];
            $data["COURSECD"][TEXT]             = $row2["COURSECD"];
            $data["MAJORCD"][TEXT]              = $row2["MAJORCD"];
            $data["COURSECODE"][TEXT]           = $row2["COURSECODE"];
            $data["EXECUTE_DATE"][NUMBER]       = "sysdate()";
            $data["FIXED_FLG"][TEXT]            = "1";
            $data["REGISTERCD"][TEXT]           = STAFFCD;
            $data["UPDATED"][NUMBER]            = "sysdate()";
            $query  = Query::insertSQL($data, "REPORT_AFT_SINRO_ADDITION_L350W_DAT");
            $db2->query($query);
        }
        $result->free();

        //対象テーブル一覧／条件
        $table_array = array();

        $courseIn = "";
        $sep = "";
        foreach ($model->cmc as $key => $cmc) {
            $courseIn .= "{$sep}'{$cmc["COURSECD"]}'";
            $sep = ",";
        }
        $table_array["COURSE_MST"]      = " COURSECD IN ({$courseIn}) ";
        $table_array["COURSE_YDAT"]     = " YEAR IN ('".CTRL_YEAR."', '".$model->field["YEAR"]."') AND COURSECD IN ({$courseIn}) ";

        $majorIn = "";
        $sep = "";
        foreach ($model->cmc as $key => $cmc) {
            $majorIn .= "{$sep}'{$cmc["COURSECD"]}{$cmc["MAJORCD"]}'";
            $sep = ",";
        }
        $table_array["MAJOR_MST"]       = " COURSECD || MAJORCD IN ({$majorIn}) ";
        $table_array["MAJOR_YDAT"]      = " YEAR = '".$model->field["YEAR"]."' AND COURSECD || MAJORCD IN ({$majorIn}) ";

        $courseCodeIn = "";
        $sep = "";
        foreach ($model->cmc as $key => $cmc) {
            $courseCodeIn .= "{$sep}'{$cmc["COURSECODE"]}'";
            $sep = ",";
        }
        $table_array["COURSECODE_MST"]  = " COURSECODE IN ({$courseCodeIn}) ";
        $table_array["COURSECODE_YDAT"] = " YEAR = '".$model->field["YEAR"]."' AND COURSECODE IN ({$courseCodeIn}) ";
        $table_array["ENTEXAM_COURSE_MST"] = " ENTEXAMYEAR = '".$model->field["YEAR"]."' ";

        foreach ($table_array as $table => $where) {
            //フィールド一覧取得
            $field_array = array();
            $query = knjl350wQuery::getFieldName($table);
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
            $query = knjl350wQuery::getDeleteQuery($model, $table, $where);
            $db2->query($query);

            //INSERT -- 学校側から県側へコピー
            $query = knjl350wQuery::getDataQuery($model, $table, $where);
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
        $db->commit();
        $db2->commit();
        Query::dbCheckIn($db);
        Query::dbCheckIn($db2);
        return;
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

//CSVデータ処理
    //更新データチェック処理
    function upCheckQuery($model, $setData, $largeDiv, $middleDiv, $smallDiv) {
        $query  = " SELECT ";
        $query .= "     COUNT(*) AS CNT ";
        $query .= " FROM  ";
        $query .= "     AFT_DISEASE_ADDITION420_DAT ";
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
    function insertQuery($db, $model, $setData, $largeDiv, $middleDiv, $smallDiv, $setName) {
        //データチェック
        $getCount = $db->getOne(knjl350wQuery::upCheckQuery($model, $setData, $largeDiv, $middleDiv, $smallDiv));
        
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
            $query  = Query::insertSQL($data, "AFT_DISEASE_ADDITION420_DAT");
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
            $query  = Query::updateSQL($data, "AFT_DISEASE_ADDITION420_DAT", $where);
            $db->query($query);
        }
        
        return;
    }

    //CSVファイルよりDBへインサート
    function insertQueryCsv($db, $model, $data_arr) {
        $cnt = 0;    //処理件数
        for ($i = 0; $i < get_count($data_arr); $i++) {

            /*卒業予定者数*/
            knjl350wQuery::insertQuery($db, $model, $data_arr[$i], "99", "99", "999", "ZENTAI_GOUKEI");
        
            /*進学希望者数*/
            knjl350wQuery::insertQuery($db, $model, $data_arr[$i], "10", "99", "999", "SHINGAKU_GOUKEI");

            /*就職希望者*/
            //学校紹介県内
            knjl350wQuery::insertQuery($db, $model, $data_arr[$i], "20", "01", "001", "SHUSHOKU_KIBOU_KENNAI_GAKKOU");

            //学校紹介県外
            knjl350wQuery::insertQuery($db, $model, $data_arr[$i], "20", "01", "002", "SHUSHOKU_KIBOU_KENGAI_GAKKOU");

            //学校紹介合計
            knjl350wQuery::insertQuery($db, $model, $data_arr[$i], "20", "01", "999", "SHUSHOKU_KIBOU_TOTAL_GAKKOU");

            //自己・縁故県内
            knjl350wQuery::insertQuery($db, $model, $data_arr[$i], "20", "02", "001", "SHUSHOKU_KIBOU_KENNAI_JIBUN");

            //自己・縁故県外
            knjl350wQuery::insertQuery($db, $model, $data_arr[$i], "20", "02", "002", "SHUSHOKU_KIBOU_KENGAI_JIBUN");
            
            //自己・縁故合計
            knjl350wQuery::insertQuery($db, $model, $data_arr[$i], "20", "02", "999", "SHUSHOKU_KIBOU_TOTAL_JIBUN");
            
            //公務員
            //公務員県内
            knjl350wQuery::insertQuery($db, $model, $data_arr[$i], "20", "03", "001", "SHUSHOKU_KIBOU_KENNAI_KOUMUIN");

            //公務員県外
            knjl350wQuery::insertQuery($db, $model, $data_arr[$i], "20", "03", "002", "SHUSHOKU_KIBOU_KENGAI_KOUMUIN");
            
            //公務員希望合計
            knjl350wQuery::insertQuery($db, $model, $data_arr[$i], "20", "03", "999", "SHUSHOKU_KIBOU_TOTAL_KOUMUIN");

            //県内就職希望者合計
            knjl350wQuery::insertQuery($db, $model, $data_arr[$i], "20", "99", "001", "SHUSHOKU_KIBOU_KENNAI_GOUKEI");

            //県外就職希望者合計
            knjl350wQuery::insertQuery($db, $model, $data_arr[$i], "20", "99", "002", "SHUSHOKU_KIBOU_KENGAI_GOUKEI");

            //就職希望者合計
            knjl350wQuery::insertQuery($db, $model, $data_arr[$i], "20", "99", "999", "SHUSHOKU_KIBOU_TOTAL_GOUKEI");

            //その他卒業後の計画有
            knjl350wQuery::insertQuery($db, $model, $data_arr[$i], "21", "99", "003", "SONOTA_KEIKAKU_ARI");

            //その他卒業後の計画無
            knjl350wQuery::insertQuery($db, $model, $data_arr[$i], "21", "99", "004", "SONOTA_KEIKAKU_NASHI");

            /*就職内定者*/
            //学校紹介
            //学校紹介県内
            knjl350wQuery::insertQuery($db, $model, $data_arr[$i], "22", "01", "001", "SHUSHOKU_NAITEI_KENNAI_GAKKOU");

            //学校紹介県外
            knjl350wQuery::insertQuery($db, $model, $data_arr[$i], "22", "01", "002", "SHUSHOKU_NAITEI_KENGAI_GAKKOU");

            //学校紹介合計
            knjl350wQuery::insertQuery($db, $model, $data_arr[$i], "22", "01", "999", "SHUSHOKU_NAITEI_TOTAL_GAKKOU");

            //自己・縁故
            //自己・縁故県内
            knjl350wQuery::insertQuery($db, $model, $data_arr[$i], "22", "02", "001", "SHUSHOKU_NAITEI_KENNAI_JIBUN");

            //自己・縁故県外
            knjl350wQuery::insertQuery($db, $model, $data_arr[$i], "22", "02", "002", "SHUSHOKU_NAITEI_KENGAI_JIBUN");

            //自己・縁故合計
            knjl350wQuery::insertQuery($db, $model, $data_arr[$i], "22", "02", "999", "SHUSHOKU_NAITEI_TOTAL_JIBUN");

            //公務員
            //公務員県内
            knjl350wQuery::insertQuery($db, $model, $data_arr[$i], "22", "03", "001", "SHUSHOKU_NAITEI_KENNAI_KOUMUIN");

            //公務員県外
            knjl350wQuery::insertQuery($db, $model, $data_arr[$i], "22", "03", "002", "SHUSHOKU_NAITEI_KENGAI_KOUMUIN");

            //公務員内定合計
            knjl350wQuery::insertQuery($db, $model, $data_arr[$i], "22", "03", "999", "SHUSHOKU_NAITEI_TOTAL_KOUMUIN");

            //県内就職内定者合計
            knjl350wQuery::insertQuery($db, $model, $data_arr[$i], "22", "99", "001", "SHUSHOKU_NAITEI_KENNAI_GOUKEI");

            //県外就職内定者合計
            knjl350wQuery::insertQuery($db, $model, $data_arr[$i], "22", "99", "002", "SHUSHOKU_NAITEI_KENGAI_GOUKEI");

            //就職内定者合計
            knjl350wQuery::insertQuery($db, $model, $data_arr[$i], "22", "99", "999", "SHUSHOKU_NAITEI_TOTAL_GOUKEI");

            //進学就職
            knjl350wQuery::insertQuery($db, $model, $data_arr[$i], "23", "99", "999", "SHINGAKU_SHUSHOKU");

            //就職希望者の内未内定者数
            //就職希望者の内未内定者　県内
            knjl350wQuery::insertQuery($db, $model, $data_arr[$i], "24", "99", "001", "SHUSHOKU_KIBOU_KENNAI_MINAITEI");

            //就職希望者の内未内定者　県外
            knjl350wQuery::insertQuery($db, $model, $data_arr[$i], "24", "99", "002", "SHUSHOKU_KIBOU_KENGAI_MINAITEI");

            //就職希望者の内の未内定者　合計
            knjl350wQuery::insertQuery($db, $model, $data_arr[$i], "24", "99", "999", "SHUSHOKU_KIBOU_TOTAL_MINAITEI");

            //進学希望以外の進路未決定者数
            knjl350wQuery::insertQuery($db, $model, $data_arr[$i], "30", "99", "999", "SHINGAKU_IGAI_MIKKETEI");

            $cnt++;
        }
        return $cnt;
    }

    //エラーデータの削除
    function deleteQueryErr() {
        $query  = " DELETE FROM W_CSVMSG_PRG_DAT WHERE PROGRAMID='KNJL350W'";

        return $query;
    }
    
    //エラー出力
    function getErrorCsvData() {
        $query  = "SELECT MSGROW,MSGREMARK FROM W_CSVMSG_PRG_DAT WHERE PROGRAMID='KNJL350W'";

        return $query;
    }

    //エラーＤＢへの追加
    function insertQueryErr(&$db, $record_no, $check_error) {
        $data1["PROGRAMID"][TEXT] = 'KNJL350W';
        $data1["MSGROW"][NUMBER]  = $record_no;
        $data1["MSGREMARK"][TEXT] = $check_error;

        $query = Query::insertSQL($data1, "W_CSVMSG_PRG_DAT");
        $result = $db->query($query);
    }
}
?>
