<?php

require_once('for_php7.php');

class knjl011pQuery extends Query 
{
    //学校コードを取得
    function getNameMstZ010() {
        $query  = " SELECT ";
        $query .= "     NAME2 ";
        $query .= " FROM ";
        $query .= "     NAME_MST ";
        $query .= " WHERE ";
        $query .= "         NAMECD1 = 'Z010' ";
        $query .= "     AND NAMECD2 = '00' ";

        $db = Query::dbCheckOut();
        $rtnRow = array();
        $rtnRow = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);

        return $rtnRow;
    }

    //MAX受験番号＋１を取得
    function getMaxExamnoQuery($model) {
        $db = Query::dbCheckOut();

        $query  = " SELECT ";
        $query .= "     MAX(EXAMNO) AS EXAMNO ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_APPLICANTBASE_DAT ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR = '".$model->year."' ";
        $query .= "     AND APPLICANTDIV = '{$model->field["APPLICANTDIV"]}' ";
        $query .= "     AND TESTDIV = '{$model->field["TESTDIV"]}' ";

        $maxExam = $db->getOne($query);
        if (strlen($maxExam)) {
            $maxExam++;
        } else {
            $query = knjl011pQuery::getDefNumber($model);
            $maxExam = $db->getOne($query);
        }

        Query::dbCheckIn($db);
        return sprintf("%06d", $maxExam);
    }

    //受験番号(初期値)
    function getDefNumber($model) {
        $query  = " SELECT ";
        $query .= "     EXAMNO_FROM ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_NO_DAT ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR = '".$model->year."' ";
        $query .= "     AND APPLICANTDIV = '{$model->field["APPLICANTDIV"]}' ";
        $query .= "     AND TESTDIV = '{$model->field["TESTDIV"]}' ";
        return $query;
    }

    //名称マスタよりデータを取得
    function get_name_cd($year, $namecd1, $sitei = "") {
        $query  = " SELECT ";
        $query .= "     NAMECD2 || ':' || NAME1 AS LABEL, ";
        $query .= "     NAMECD2 AS VALUE, ";
        $query .= "     ABBV1, ";
        $query .= "     NAMESPARE2 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR = '".$year."' ";
        $query .= "     AND NAMECD1 = '".$namecd1."' ";
        if ($sitei) {
            $query .= "     AND NAMECD2 = '".$sitei."' ";
        }
        if ($namecd1 == "L003") {
//            $query .= " AND NAMESPARE3 = '".SCHOOLKIND."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //受験番号帯の受験区分取得
    function getKubun($model) {
        $cd1 = ($model->field["APPLICANTDIV"] == "2") ? "L004" : "L024";

        $query  = " SELECT ";
        $query .= "     T1.*, ";
        $query .= "     T3.NAME1 AS KUBUN_TESTDIV ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_NO_DAT T1 ";
        $query .= "     LEFT JOIN NAME_MST T3 ON T3.NAMECD1 = '{$cd1}' AND T3.NAMECD2 = T1.TESTDIV ";
        $query .= " WHERE ";
        $query .= "     T1.ENTEXAMYEAR = '".$model->year."' ";
        $query .= "     AND T1.APPLICANTDIV = '".$model->field["APPLICANTDIV"]."' ";
        $query .= "     AND '".$model->examno."' BETWEEN T1.EXAMNO_FROM AND T1.EXAMNO_TO ";
        return $query;
    }

    //試験会場
    function getHall($year, $applicantdiv, $testdiv) {
        $cd1 = ($model->field["APPLICANTDIV"] == "2") ? "L004" : "L024";

        $query  = " SELECT ";
        $query .= "     EXAMHALL_NAME AS LABEL, ";
        $query .= "     EXAMHALLCD AS VALUE ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_HALL_YDAT ";
        $query .= " WHERE ";
        $query .= "         ENTEXAMYEAR = '{$year}' ";
        $query .= "     AND APPLICANTDIV= '{$applicantdiv}' ";
        $query .= "     AND TESTDIV     = '{$testdiv}' ";
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
    function get_edit_data($model) {
        $db = Query::dbCheckOut();

        $query  = "";
        $query .= " SELECT ";
        $query .= "     T1.APPLICANTDIV, ";
        $query .= "     T1.EXAMNO, ";
        $query .= "     T1.TESTDIV, ";
        $query .= "     T1.SIKEN_KAIJOU_CD1, ";
        $query .= "     T1.RECEPTDATE, ";
        $query .= "     T1.DORMITORY_FLG, ";
        $query .= "     T1.SIMAI_NAME, ";
        $query .= "     T1.REMARK1, ";
        $query .= "     T1.REMARK2, ";
        $query .= "     T1.DAI1_RECEPTNO, ";
        $query .= "     T1.JUKUCD AS PRISCHOOLCD, ";
        $query .= "     T1.KYOUSHITSU_CD AS PRISCHOOL_CLASS_CD, ";
        $query .= "     T1.NAME, ";
        $query .= "     T1.NAME_KANA, ";
        $query .= "     T1.SEX, ";
        $query .= "     T1.BIRTHDAY, ";
        $query .= "     T1.FS_CD, ";
        $query .= "     FIN.FINSCHOOL_NAME AS FS_NAME, ";
        $query .= "     T1.FS_GRDYEAR, ";
        $query .= "     T1.FS_ERACD, ";
        $query .= "     N1.NAME1 AS FS_WNAME, ";
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
        $query .= "     T2.GTELNO2, ";
        $query .= "     T3.NAME1 ";
        $query .= " FROM ";
        $query .= "     V_ENTEXAM_APPLICANTBASE_DAT T1 ";
        $query .= "     LEFT JOIN ENTEXAM_APPLICANTADDR_DAT T2 ";
        $query .= "          ON T2.ENTEXAMYEAR = T1.ENTEXAMYEAR ";
        $query .= "         AND T2.APPLICANTDIV = T1.APPLICANTDIV ";
        $query .= "         AND T2.EXAMNO = T1.EXAMNO ";
        $query .= "     LEFT JOIN V_NAME_MST T3 ";
        $query .= "          ON T3.YEAR = T1.ENTEXAMYEAR ";
        $query .= "         AND T3.NAMECD1 = 'L007' ";
        $query .= "         AND T3.NAMECD2 = T1.ERACD ";
        $query .= "     LEFT JOIN FINSCHOOL_MST FIN ";
        $query .= "          ON T1.FS_CD = FIN.FINSCHOOLCD ";
        $query .= "     LEFT JOIN V_NAME_MST N1 ";
        $query .= "          ON N1.YEAR = T1.ENTEXAMYEAR ";
        $query .= "         AND N1.NAMECD1 = 'L007' ";
        $query .= "         AND N1.NAMECD2 = T1.FS_ERACD ";
        $query .= " WHERE ";
        $query .= "     T1.ENTEXAMYEAR = '".$model->year."' AND ";
        $query .= "     T1.APPLICANTDIV = '".$model->applicantdiv."' AND ";

        if ($model->cmd == "back" || $model->cmd == "back1") {
            $query.= "     T1.EXAMNO < '".$model->examno."'";
            $query.= " ORDER BY ";
            $query.= "     T1.EXAMNO DESC ";
        } elseif ($model->cmd == "next" || $model->cmd == "next1") {
            $query.= "     T1.EXAMNO > '".$model->examno."'";
            $query.= " ORDER BY ";
            $query.= "     T1.EXAMNO ASC ";
        } else {
            $query.= "     T1.EXAMNO = '".$model->examno."'";
        }

        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);

        return $row;
    }

    //出身学校マスタよりデータを取得
    function getFinschoolName($cd) {
        $query  = " SELECT  T1.FINSCHOOL_NAME, VALUE(L3.NAME1, '') AS FINSCHOOL_DISTCD_NAME ";
        $query .= "   FROM  FINSCHOOL_MST T1 ";
        $query .= "         LEFT JOIN NAME_MST L3 ON  L3.NAMECD1 = 'L001' ";
        $query .= "                               AND L3.NAMECD2 = T1.FINSCHOOL_DISTCD ";
        $query .= "  WHERE  T1.FINSCHOOLCD = '".$cd."' ";
        return $query;
    }

    //塾名
    function getPriSchoolName($priSchoolCd) {
        $query  = " SELECT ";
        $query .= "     PRISCHOOL_NAME ";
        $query .= " FROM ";
        $query .= "     PRISCHOOL_MST ";
        $query .= " WHERE ";
        $query .= "     PRISCHOOLCD = '{$priSchoolCd}' ";
        return $query;
    }
    //教室名
    function getPriSchoolClassName($priSchoolCd, $priSchoolClassCd) {
        $query  = " SELECT ";
        $query .= "     PRISCHOOL_NAME ";
        $query .= " FROM ";
        $query .= "     PRISCHOOL_CLASS_MST ";
        $query .= " WHERE ";
        $query .= "     PRISCHOOLCD = '{$priSchoolCd}' ";
        $query .= "     AND PRISCHOOL_CLASS_CD = '{$priSchoolClassCd}' ";
        return $query;
    }

    //存在チェック
    function getExistdataCnt($model) {
        $query  = " SELECT ";
        $query .= "     COUNT(EXAMNO) ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_APPLICANTBASE_DAT ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR = '".$model->year."' ";
        $query .= "     AND APPLICANTDIV = '".$model->field["APPLICANTDIV"]."' ";
        $query .= "     AND EXAMNO = '".$model->examno."' ";
        return $query;
    }

    //受験番号帯に含まれているか確認
    function getExistNoCnt($model, $div = "") {
        $query  = " SELECT ";
        if ($div == "1") {
            $query .= "     * ";
        } else {
            $query .= "     COUNT(*) ";
        }
        $query .= " FROM ";
        $query .= "     ENTEXAM_NO_DAT ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR = '".$model->year."' ";
        $query .= "     AND APPLICANTDIV = '".$model->field["APPLICANTDIV"]."' ";
        $query .= "     AND '".$model->examno."' BETWEEN EXAMNO_FROM AND EXAMNO_TO ";
        $query .= "     AND TESTDIV = '{$model->field["TESTDIV"]}' ";
        return $query;
    }

    //前期受験番号チェック
    function getExistZenkiNoCnt($model, $div = "") {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_APPLICANTBASE_DAT ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR = '".$model->year."' ";
        $query .= "     AND APPLICANTDIV = '1' ";
        $query .= "     AND TESTDIV = '2' ";
        $query .= "     AND EXAMNO = '".$model->field["DAI1_RECEPTNO"]."' ";
        return $query;
    }

    //追加・更新
    function &getUpdateQuery($model) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //受験番号範囲設定にある項目(追加ボタンを押した時のみセット)
        //if ($model->cmd == "add") {
            //受験区分取得
            $kubunRow = $db->getRow(knjl011pQuery::getExistNoCnt($model, "1"), DB_FETCHMODE_ASSOC);
        //}

        $result = $db->query(knjl011pQuery::get_calendarno($model->year));
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
        $data["SHDIV"][TEXT]         = "1";
        $data["DESIREDIV"][TEXT]     = "1";
        $data["RECEPTDATE"][TEXT]    = str_replace("/", "-", $model->field["RECEPTDATE"]);
        $data["NAME"][TEXT]          = $model->field["NAME"];
        $data["NAME_KANA"][TEXT]     = $model->field["NAME_KANA"];
        $data["SEX"][TEXT]           = $model->field["SEX"];
//後で調査。
//        $data["ERACD"][TEXT]         = $eracd;
//        $data["BIRTH_Y"][TEXT]         = $birth_y;
//        $data["BIRTH_M"][TEXT]         = $birth_m;
//        $data["BIRTH_D"][TEXT]         = $birth_d;
        $data["BIRTHDAY"][TEXT]      = str_replace("/", "-", $model->field["BIRTHDAY"]);
        $data["FS_CD"][TEXT]         = (strlen($model->field["FS_CD"]) ? sprintf("%07d",$model->field["FS_CD"]) : "");
        $data["FS_ERACD"][TEXT]      = $model->field["FS_ERACD"];
        $data["FS_Y"][TEXT]          = $model->field["FS_Y"]? sprintf("%02d",$model->field["FS_Y"]) : "";
        $data["FS_M"][TEXT]          = $model->field["FS_M"]? sprintf("%02d",$model->field["FS_M"]) : "";
        $data["FS_GRDDIV"][TEXT]     = $model->field["FS_GRDDIV"];
        $data["FS_GRDYEAR"][TEXT]    = $fs_grdyear;
        $data["FS_DAY"][TEXT]        = $fs_date;
        $data["DORMITORY_FLG"][TEXT] = $model->field["DORMITORY_FLG"];
        $data["REMARK1"][TEXT]       = $model->field["REMARK1"];
        $data["REMARK2"][TEXT]       = $model->field["REMARK2"];
        $data["REGISTERCD"][TEXT]    = STAFFCD;
        $data["UPDATED"][FUNC]       = "sysdate()";
        if ($model->cmd == "add") {
            $data["ENTEXAMYEAR"][TEXT]   = $model->year;
            $data["APPLICANTDIV"][TEXT]  = $model->field["APPLICANTDIV"];
            $data["EXAMNO"][TEXT]        = $model->examno;
            //受験番号範囲設定にある項目(追加ボタンを押した時のみセット)
            //$data["TESTDIV"][TEXT]       = $kubunRow["TESTDIV"];
            $data["TESTDIV"][TEXT]       = $model->field["TESTDIV"];
            $query = Query::insertSQL($data, "ENTEXAM_APPLICANTBASE_DAT");
            $db->query($query);
        } else {
            $where  = " WHERE ENTEXAMYEAR = '".$model->year."' ";
            $where .= "   AND APPLICANTDIV = '".$model->field["APPLICANTDIV"]."' ";
            $where .= "   AND EXAMNO = '".$model->examno."' ";
            $query = Query::updateSQL($data, "ENTEXAM_APPLICANTBASE_DAT", $where);
            $db->query($query);
        }

        //ENTEXAM_APPLICANTADDR_DAT
        //DELETE
        if ($model->cmd != "add") {
            $query  = " DELETE FROM ENTEXAM_APPLICANTADDR_DAT ";
            $query .= " WHERE ENTEXAMYEAR = '".$model->year."' ";
            $query .= "   AND APPLICANTDIV = '".$model->field["APPLICANTDIV"]."' ";
            $query .= "   AND EXAMNO = '".$model->examno."' ";
            $db->query($query);
        }
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
        $data["GTELNO2"][TEXT]       = $model->field["GTELNO2"]; //電話番号(保護者)(携帯)
        $data["RELATIONSHIP"][TEXT]  = $model->field["RELATIONSHIP"];
        $data["REGISTERCD"][TEXT]    = STAFFCD;
        $data["UPDATED"][FUNC]       = "sysdate()";
        $query = Query::insertSQL($data, "ENTEXAM_APPLICANTADDR_DAT");
        $db->query($query);

        //ENTEXAM_APPLICANTBASE_DETAIL_DAT
        //cnt
        $getCount001 = $db->getOne(knjl011pQuery::getCountDetail($model, "001"));
        $getCount008 = $db->getOne(knjl011pQuery::getCountDetail($model, "008"));
        $getCount012 = $db->getOne(knjl011pQuery::getCountDetail($model, "012"));
        $getCount014 = $db->getOne(knjl011pQuery::getCountDetail($model, "014"));
        $getCount024 = $db->getOne(knjl011pQuery::getCountDetail($model, "024"));
        //where
        $getWhere001 = knjl011pQuery::getWhereDetail($model, "001");
        $getWhere008 = knjl011pQuery::getWhereDetail($model, "008");
        $getWhere012 = knjl011pQuery::getWhereDetail($model, "012");
        $getWhere014 = knjl011pQuery::getWhereDetail($model, "014");
        $getWhere024 = knjl011pQuery::getWhereDetail($model, "024");

        //志望コース　ENTEXAM_COURSE_MSTから取得しセットする。
        $courseRow = $db->getRow(knjl011pQuery::getExamcourse($model, $model->field["TESTDIV"]), DB_FETCHMODE_ASSOC);
        $data = array();
        $data["REMARK8"][TEXT]              = $courseRow["COURSECD"];
        $data["REMARK9"][TEXT]              = $courseRow["MAJORCD"];
        $data["REMARK10"][TEXT]             = $courseRow["EXAMCOURSE"];
        $data["REGISTERCD"][TEXT]           = STAFFCD;
        $data["UPDATED"][FUNC]              = "sysdate()";
        if ($getCount001 == 0) {
            $data["ENTEXAMYEAR"][TEXT]      = $model->year;
            $data["APPLICANTDIV"][TEXT]     = $model->field["APPLICANTDIV"];
            $data["EXAMNO"][TEXT]           = $model->examno;
            $data["SEQ"][TEXT]              = "001";
            $query = Query::insertSQL($data, "ENTEXAM_APPLICANTBASE_DETAIL_DAT");
        } else {
            $query = Query::updateSQL($data, "ENTEXAM_APPLICANTBASE_DETAIL_DAT", $getWhere001);
        }
        $db->query($query);

        //塾・教室(PRISCHOOL_MST、PRISCHOOL_CLASS_MST)
        $data = array();
        $data["REMARK1"][TEXT]              = $model->field["PRISCHOOLCD"];
        $data["REMARK3"][TEXT]              = $model->field["PRISCHOOL_CLASS_CD"];
        $data["REGISTERCD"][TEXT]           = STAFFCD;
        $data["UPDATED"][FUNC]              = "sysdate()";
        if ($getCount008 == 0) {
            $data["ENTEXAMYEAR"][TEXT]      = $model->year;
            $data["APPLICANTDIV"][TEXT]     = $model->field["APPLICANTDIV"];
            $data["EXAMNO"][TEXT]           = $model->examno;
            $data["SEQ"][TEXT]              = "008";
            $query = Query::insertSQL($data, "ENTEXAM_APPLICANTBASE_DETAIL_DAT");
        } else {
            $query = Query::updateSQL($data, "ENTEXAM_APPLICANTBASE_DETAIL_DAT", $getWhere008);
        }
        $db->query($query);

        //前期受験番号
        $data = array();
        $data["REMARK1"][TEXT]              = $model->field["DAI1_RECEPTNO"];
        $data["REGISTERCD"][TEXT]           = STAFFCD;
        $data["UPDATED"][FUNC]              = "sysdate()";
        if ($getCount012 == 0) {
            $data["ENTEXAMYEAR"][TEXT]      = $model->year;
            $data["APPLICANTDIV"][TEXT]     = $model->field["APPLICANTDIV"];
            $data["EXAMNO"][TEXT]           = $model->examno;
            $data["SEQ"][TEXT]              = "012";
            $query = Query::insertSQL($data, "ENTEXAM_APPLICANTBASE_DETAIL_DAT");
        } else {
            $query = Query::updateSQL($data, "ENTEXAM_APPLICANTBASE_DETAIL_DAT", $getWhere012);
        }
        $db->query($query);

        //本校在籍又は卒業の保護者・兄弟・姉妹
        $data = array();
        $data["REMARK2"][TEXT]              = $model->field["SIMAI_NAME"];
        $data["REGISTERCD"][TEXT]           = STAFFCD;
        $data["UPDATED"][FUNC]              = "sysdate()";
        if ($getCount014 == 0) {
            $data["ENTEXAMYEAR"][TEXT]      = $model->year;
            $data["APPLICANTDIV"][TEXT]     = $model->field["APPLICANTDIV"];
            $data["EXAMNO"][TEXT]           = $model->examno;
            $data["SEQ"][TEXT]              = "014";
            $query = Query::insertSQL($data, "ENTEXAM_APPLICANTBASE_DETAIL_DAT");
        } else {
            $query = Query::updateSQL($data, "ENTEXAM_APPLICANTBASE_DETAIL_DAT", $getWhere014);
        }
        $db->query($query);

        //試験会場
        $data = array();
        $data["REMARK1"][TEXT]              = $model->field["SIKEN_KAIJOU_CD1"];
        $data["REGISTERCD"][TEXT]           = STAFFCD;
        $data["UPDATED"][FUNC]              = "sysdate()";
        if ($getCount024 == 0) {
            $data["ENTEXAMYEAR"][TEXT]      = $model->year;
            $data["APPLICANTDIV"][TEXT]     = $model->field["APPLICANTDIV"];
            $data["EXAMNO"][TEXT]           = $model->examno;
            $data["SEQ"][TEXT]              = "024";
            $query = Query::insertSQL($data, "ENTEXAM_APPLICANTBASE_DETAIL_DAT");
        } else {
            $query = Query::updateSQL($data, "ENTEXAM_APPLICANTBASE_DETAIL_DAT", $getWhere024);
        }
        $db->query($query);

        $db->commit();
        Query::dbCheckIn($db);
        return;
    }

    function getCountDetail($model, $seq) {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_APPLICANTBASE_DETAIL_DAT ";
        $query .= " WHERE ";
        $query .= "         ENTEXAMYEAR     = '{$model->year}' ";
        $query .= "     AND APPLICANTDIV    = '{$model->field["APPLICANTDIV"]}' ";
        $query .= "     AND EXAMNO          = '{$model->examno}' ";
        $query .= "     AND SEQ             = '{$seq}' ";
        return $query;
    }

    function getWhereDetail($model, $seq) {
        $query  = " WHERE ";
        $query .= "         ENTEXAMYEAR     = '{$model->year}' ";
        $query .= "     AND APPLICANTDIV    = '{$model->field["APPLICANTDIV"]}' ";
        $query .= "     AND EXAMNO          = '{$model->examno}' ";
        $query .= "     AND SEQ             = '{$seq}' ";
        return $query;
    }

    //志望コース
    function getExamcourse($model, $testdiv) {
        $query  = "";
        $query .= " SELECT ";
        $query .= "     T1.COURSECD, ";
        $query .= "     T1.MAJORCD, ";
        $query .= "     T1.EXAMCOURSECD ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_COURSE_MST T1 ";
        $query .= " WHERE ";
        $query .= "         T1.ENTEXAMYEAR  = '{$model->year}' ";
        $query .= "     AND T1.APPLICANTDIV = '{$model->field["APPLICANTDIV"]}' ";
        $query .= "     AND T1.TESTDIV      = '{$testdiv}' ";
        return $query;
    }

    //--- DELETE
    function &getDeleteQuery($model) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //ENTEXAM_APPLICANTBASE_DAT
        $query  = " DELETE FROM ENTEXAM_APPLICANTBASE_DAT ";
        $query .= " WHERE ENTEXAMYEAR = '".$model->year."' ";
        $query .= "   AND APPLICANTDIV = '".$model->field["APPLICANTDIV"]."' ";
        $query .= "   AND EXAMNO      = '".$model->examno."'";
        $db->query($query);

        //ENTEXAM_APPLICANTBASE_DETAIL_DAT
        $query  = " DELETE FROM ENTEXAM_APPLICANTBASE_DETAIL_DAT ";
        $query .= " WHERE ENTEXAMYEAR = '".$model->year."' ";
        $query .= "   AND APPLICANTDIV = '".$model->field["APPLICANTDIV"]."' ";
        $query .= "   AND EXAMNO      = '".$model->examno."'";
        $db->query($query);

        //ENTEXAM_APPLICANTADDR_DAT
        $query  = " DELETE FROM ENTEXAM_APPLICANTADDR_DAT ";
        $query .= " WHERE ENTEXAMYEAR = '".$model->year."' ";
        $query .= "   AND APPLICANTDIV = '".$model->field["APPLICANTDIV"]."' ";
        $query .= "   AND EXAMNO      = '".$model->examno."'";
        $db->query($query);

        //ENTEXAM_APPLICANTCONFRPT_DAT
        $query  = " DELETE FROM ENTEXAM_APPLICANTCONFRPT_DAT ";
        $query .= " WHERE ENTEXAMYEAR = '".$model->year."' ";
        $query .= "   AND APPLICANTDIV = '".$model->field["APPLICANTDIV"]."' ";
        $query .= "   AND EXAMNO      = '".$model->examno."'";
        $db->query($query);

        //ENTEXAM_APPLICANTCONFRPT_DETAIL_DAT
        $query  = " DELETE FROM ENTEXAM_APPLICANTCONFRPT_DETAIL_DAT ";
        $query .= " WHERE ENTEXAMYEAR = '".$model->year."' ";
        $query .= "   AND APPLICANTDIV = '".$model->field["APPLICANTDIV"]."' ";
        $query .= "   AND EXAMNO      = '".$model->examno."'";
        $db->query($query);

        //ENTEXAM_INTERVIEW_DAT
        $query  = " DELETE FROM ENTEXAM_INTERVIEW_DAT ";
        $query .= " WHERE ENTEXAMYEAR = '".$model->year."' ";
        $query .= "   AND APPLICANTDIV = '".$model->field["APPLICANTDIV"]."' ";
        $query .= "   AND EXAMNO      = '".$model->examno."'";
        $db->query($query);

        //ENTEXAM_SCORE_DAT
        $query  = " DELETE FROM ENTEXAM_SCORE_DAT ";
        $query .= " WHERE (ENTEXAMYEAR, APPLICANTDIV, TESTDIV, EXAM_TYPE, RECEPTNO) IN  (";
        $query .= "         SELECT ENTEXAMYEAR, APPLICANTDIV, TESTDIV, EXAM_TYPE, RECEPTNO ";
        $query .= "           FROM ENTEXAM_RECEPT_DAT ";
        $query .= "          WHERE ENTEXAMYEAR = '".$model->year."' ";
        $query .= "            AND APPLICANTDIV = '".$model->field["APPLICANTDIV"]."' ";
        $query .= "            AND EXAMNO      = '".$model->examno."'";
        $query .= "       )";
        $db->query($query);

        //ENTEXAM_RECEPT_DAT
        $query  = "DELETE FROM ENTEXAM_RECEPT_DAT ";
        $query .= " WHERE ENTEXAMYEAR = '".$model->year."' ";
        $query .= "   AND APPLICANTDIV = '".$model->field["APPLICANTDIV"]."' ";
        $query .= "   AND EXAMNO      = '".$model->examno."'";
        $db->query($query);

        $db->commit();
        Query::dbCheckIn($db);
        return;
    }
}
?>