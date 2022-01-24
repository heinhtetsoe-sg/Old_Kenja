<?php

require_once('for_php7.php');

class knjl011eQuery extends Query {
    //名称マスタよりデータを取得
    function get_name_cd($year, $namecd1, $sitei = "") {
        $query  = " SELECT ";
        $query .= "     NAMECD2 || ':' || NAME1 AS LABEL, ";
        $query .= "     NAMECD2 AS VALUE, ";
        $query .= "     ABBV1, ";
        $query .= "     NAMESPARE1, ";
        $query .= "     NAMESPARE2, ";
        $query .= "     NAMESPARE3 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "         YEAR    = '".$year."' ";
        $query .= "     AND NAMECD1 = '".$namecd1."' ";
        if ($sitei) {
            $query .= "     AND NAMECD2 = '".$sitei."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //名称マスタ
    function getNameMstList($year, $namecd1In) {
        $query  = " SELECT ";
        $query .= "     NAMECD1, ";
        $query .= "     NAMECD2, ";
        $query .= "     NAMECD2 || ':' || NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "         YEAR     = '{$year}' ";
        $query .= "     AND NAMECD1 IN {$namecd1In} ";

        return $query;
    }

    //メインデータ取得
    function get_edit_data($model) {
        $db = Query::dbCheckOut();

        $query  = "";
        $query .= " SELECT ";
        $query .= "     BASE.APPLICANTDIV, ";
        $query .= "     BASE.TESTDIV, ";
        $query .= "     BASE.TESTDIV1, ";
        $query .= "     BASE.DESIREDIV, ";
        $query .= "     BASE.SHDIV, ";
        $query .= "     BASE.EXAMNO, ";
        $query .= "     SEQ002.REMARK1 AS LOGIN_ID, ";
        $query .= "     BASE.NAME, ";
        $query .= "     BASE.NAME_KANA, ";
        $query .= "     BASE.SEX, ";
        $query .= "     BASE.BIRTHDAY, ";
        $query .= "     BASE.FS_CD as FINSCHOOLCD, ";
        $query .= "     BASE.FS_DAY, ";
        $query .= "     BASE.FS_GRDDIV, ";
        $query .= "     ADDR.ZIPCD, ";
        $query .= "     ADDR.ADDRESS1, ";
        $query .= "     ADDR.ADDRESS2, ";
        $query .= "     ADDR.TELNO, ";
        $query .= "     ADDR.GNAME, ";
        $query .= "     ADDR.GKANA, ";
        $query .= "     ADDR.GZIPCD, ";
        $query .= "     ADDR.GADDRESS1, ";
        $query .= "     ADDR.GADDRESS2, ";
        $query .= "     ADDR.GTELNO, ";
        $query .= "     ADDR.RELATIONSHIP, ";
        $query .= "     SEQ005.REMARK5 AS ENT_MONEY_STATUS, ";
        $query .= "     SEQ005.REMARK6 AS RECOMMENDATION, ";
        $query .= "     SEQ033.REMARK1 AS HOPE1, ";
        $query .= "     SEQ033.REMARK2 AS HOPE2, ";
        $query .= "     SEQ030.REMARK6 AS HOPE, ";
        $query .= "     SEQ033.REMARK3 AS NOMAL_SHDIV, ";
        $query .= "     SEQ034.REMARK10 AS QUALIFIED_CONTENT, ";
        $query .= "     SEQ034.REMARK2 AS QUALIFIED, ";
        $query .= "     SEQ034.REMARK3 AS PRACTICE, ";
        $query .= "     SEQ034.REMARK4 AS POSSIBLE ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_APPLICANTBASE_DAT BASE ";
        $query .= "     LEFT JOIN ENTEXAM_APPLICANTADDR_DAT ADDR ";
        $query .= "          ON ADDR.ENTEXAMYEAR  = BASE.ENTEXAMYEAR ";
        $query .= "         AND ADDR.APPLICANTDIV = BASE.APPLICANTDIV ";
        $query .= "         AND ADDR.EXAMNO       = BASE.EXAMNO ";
        $query .= "     LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT SEQ002 ";
        $query .= "          ON SEQ002.ENTEXAMYEAR  = BASE.ENTEXAMYEAR ";
        $query .= "         AND SEQ002.APPLICANTDIV = BASE.APPLICANTDIV ";
        $query .= "         AND SEQ002.EXAMNO       = BASE.EXAMNO ";
        $query .= "         AND SEQ002.SEQ          = '002' ";
        $query .= "     LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT SEQ005 ";
        $query .= "          ON SEQ005.ENTEXAMYEAR  = BASE.ENTEXAMYEAR ";
        $query .= "         AND SEQ005.APPLICANTDIV = BASE.APPLICANTDIV ";
        $query .= "         AND SEQ005.EXAMNO       = BASE.EXAMNO ";
        $query .= "         AND SEQ005.SEQ          = '005' ";
        $query .= "     LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT SEQ030 ";
        $query .= "          ON SEQ030.ENTEXAMYEAR  = BASE.ENTEXAMYEAR ";
        $query .= "         AND SEQ030.APPLICANTDIV = BASE.APPLICANTDIV ";
        $query .= "         AND SEQ030.EXAMNO       = BASE.EXAMNO ";
        $query .= "         AND SEQ030.SEQ          = '030' ";
        $query .= "     LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT SEQ033 ";
        $query .= "          ON SEQ033.ENTEXAMYEAR  = BASE.ENTEXAMYEAR ";
        $query .= "         AND SEQ033.APPLICANTDIV = BASE.APPLICANTDIV ";
        $query .= "         AND SEQ033.EXAMNO       = BASE.EXAMNO ";
        $query .= "         AND SEQ033.SEQ          = '033' ";
        $query .= "     LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT SEQ034 ";
        $query .= "          ON SEQ034.ENTEXAMYEAR  = BASE.ENTEXAMYEAR ";
        $query .= "         AND SEQ034.APPLICANTDIV = BASE.APPLICANTDIV ";
        $query .= "         AND SEQ034.EXAMNO       = BASE.EXAMNO ";
        $query .= "         AND SEQ034.SEQ          = '034' ";
        $query .= " WHERE ";
        $query .= "         BASE.ENTEXAMYEAR  = '".$model->year."' ";
        $query .= "     AND BASE.APPLICANTDIV = '".$model->field["APPLICANTDIV"]."' ";
        $query .= "     AND BASE.TESTDIV      = '".$model->field["TESTDIV"]."' ";

        if ($model->cmd == "back" || $model->cmd == "back1") {
            $query.= "     AND BASE.EXAMNO < '".$model->examno."'";
            $query.= " ORDER BY ";
            $query.= "     BASE.EXAMNO DESC ";
        } elseif ($model->cmd == "next" || $model->cmd == "next1") {
            $query.= "     AND BASE.EXAMNO > '".$model->examno."'";
            $query.= " ORDER BY ";
            $query.= "     BASE.EXAMNO ASC ";
        } else {
            $query.= "     AND BASE.EXAMNO = '".$model->examno."'";
        }

        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        Query::dbCheckIn($db);

        return $row;
    }

    //併願状況取得
    function getHeiganData($model, $loginId) {
        $query  = " SELECT ";
        $query .= "     BASE.TESTDIV, ";
        $query .= "     BASE.DESIREDIV, ";
        $query .= "     BASE.EXAMNO ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_APPLICANTBASE_DAT BASE ";
        $query .= "     INNER JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT SEQ002 ";
        $query .= "          ON SEQ002.ENTEXAMYEAR  = BASE.ENTEXAMYEAR ";
        $query .= "         AND SEQ002.APPLICANTDIV = BASE.APPLICANTDIV ";
        $query .= "         AND SEQ002.EXAMNO       = BASE.EXAMNO ";
        $query .= "         AND SEQ002.SEQ          = '002' ";
        $query .= "         AND SEQ002.REMARK1      = '{$loginId}' ";
        $query .= " WHERE ";
        $query .= "         BASE.ENTEXAMYEAR  = '".$model->year."' ";
        $query .= "     AND BASE.APPLICANTDIV = '".$model->field["APPLICANTDIV"]."' ";
        $query .= "     AND BASE.EXAMNO      <> '".$model->examno."' ";
        $query .= " ORDER BY ";
        $query .= "     BASE.TESTDIV, ";
        $query .= "     BASE.EXAMNO ";

        return $query;
    }

    //出身学校マスタよりデータを取得
    function getFinschoolName($cd) {
        $query  = " SELECT  T1.FINSCHOOL_NAME ";
        $query .= "   FROM  FINSCHOOL_MST T1 ";
        $query .= "  WHERE  T1.FINSCHOOLCD = '".$cd."' ";
        return $query;
    }

/***** 追加更新処理 *****/
    //存在チェック
    function getExistdataCnt($model) {
        $query  = " SELECT ";
        $query .= "     COUNT(EXAMNO) ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_APPLICANTBASE_DAT ";
        $query .= " WHERE ";
        $query .= "         ENTEXAMYEAR  = '".$model->year."' ";
        $query .= "     AND APPLICANTDIV = '".$model->field["APPLICANTDIV"]."' ";
        $query .= "     AND EXAMNO       = '".$model->examno."' ";

        return $query;
    }

    //和暦変換
    function getWarekiCd($model, $setDay) {
        $query  = " SELECT ";
        $query .= "     NAMECD2, ";
        $query .= "     NAMESPARE1 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "         YEAR    = '".$model->year."' ";
        $query .= "     AND NAMECD1 = 'L007' ";
        $query .= "     AND '{$setDay}' BETWEEN NAMESPARE2 AND NAMESPARE3 ";

        return $query;
    }

    //追加・更新
    function &getUpdateQuery($model) {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //生年月日セット（西暦→和暦）
        $birthRow = $db->getRow(knjl011eQuery::getWarekiCd($model, $model->field["BIRTHDAY"]), DB_FETCHMODE_ASSOC);
        $eraCd    = $birthRow['NAMECD2']; //元号
        list($bYear, $bMonth, $bDay) = explode('/', $model->field["BIRTHDAY"]);
        $eraYear  = sprintf("%02d", $bYear - $birthRow['NAMESPARE1'] + 1);

        //卒業月日セット（西暦→和暦）
        $fsRow = $db->getRow(knjl011eQuery::getWarekiCd($model, $model->field["FS_DAY"]), DB_FETCHMODE_ASSOC);
        $fsEraCd   = $fsRow['NAMECD2']; //元号
        list($fsYear, $fsMonth, $fsDay) = explode('/', $model->field["FS_DAY"]);
        $fsEraYear = sprintf("%02d", $fsYear - $fsRow['NAMESPARE1'] + 1);

        //ENTEXAM_APPLICANTBASE_DAT
        $data = array();
        $data["NAME"][TEXT]          = $model->field["NAME"];
        $data["NAME_KANA"][TEXT]     = $model->field["NAME_KANA"];
        $data["SEX"][TEXT]           = $model->field["SEX"];
        $data["ERACD"][TEXT]         = $eraCd;
        $data["BIRTH_Y"][TEXT]       = $eraYear;
        $data["BIRTH_M"][TEXT]       = $bMonth;
        $data["BIRTH_D"][TEXT]       = $bDay;
        $data["BIRTHDAY"][TEXT]      = str_replace('/', '-', $model->field["BIRTHDAY"]);
        $data["FS_CD"][TEXT]         = (strlen($model->field["FINSCHOOLCD"])) ? sprintf("%07s",$model->field["FINSCHOOLCD"]) : "";
        $data["FS_ERACD"][TEXT]      = $fsEraCd;
        $data["FS_Y"][TEXT]          = $fsEraYear;
        $data["FS_M"][TEXT]          = $fsMonth;
        $data["FS_DAY"][TEXT]        = str_replace('/', '-', $model->field["FS_DAY"]);
        $data["FS_GRDDIV"][TEXT]     = $model->field["FS_GRDDIV"];
        $data["REGISTERCD"][TEXT]    = STAFFCD;
        $data["UPDATED"][FUNC]       = "sysdate()";

        $where  = " WHERE ENTEXAMYEAR  = '".$model->year."' ";
        $where .= "   AND APPLICANTDIV = '".$model->field["APPLICANTDIV"]."' ";
        $where .= "   AND EXAMNO       = '".$model->examno."' ";

        $query = Query::updateSQL($data, "ENTEXAM_APPLICANTBASE_DAT", $where);
        $db->query($query);

        //ENTEXAM_APPLICANTADDR_DAT
        //cnt
        $getCountAddr = $db->getOne(knjl011eQuery::getCountAddr($model));

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

        if ($getCountAddr == 0) {
            $query = Query::insertSQL($data, "ENTEXAM_APPLICANTADDR_DAT");
            $db->query($query);
        } else {
            $where  = " WHERE ENTEXAMYEAR  = '".$model->year."' ";
            $where .= "   AND APPLICANTDIV = '".$model->field["APPLICANTDIV"]."' ";
            $where .= "   AND EXAMNO       = '".$model->examno."' ";

            $query = Query::updateSQL($data, "ENTEXAM_APPLICANTADDR_DAT", $where);
            $db->query($query);
        }

        //ENTEXAM_APPLICANTBASE_DETAIL_DAT
        //cnt
        $getCount005 = $db->getOne(knjl011eQuery::getCountDetail($model, "005"));
        //where
        $getWhere005 = knjl011eQuery::getWhereDetail($model, "005");

        $data = array();
        $data["REMARK5"][TEXT]              = $model->field["ENT_MONEY_STATUS"];    //入学金支払状況
        $data["REMARK6"][TEXT]              = $model->field["RECOMMENDATION"];      //本校同窓会推薦書
        $data["REGISTERCD"][TEXT]           = STAFFCD;
        $data["UPDATED"][FUNC]              = "sysdate()";

        if ($getCount005 == 0) {
            $data["ENTEXAMYEAR"][TEXT]      = $model->year;
            $data["APPLICANTDIV"][TEXT]     = $model->field["APPLICANTDIV"];
            $data["EXAMNO"][TEXT]           = $model->examno;
            $data["SEQ"][TEXT]              = "005";
            $query = Query::insertSQL($data, "ENTEXAM_APPLICANTBASE_DETAIL_DAT");
        } else {
            $query = Query::updateSQL($data, "ENTEXAM_APPLICANTBASE_DETAIL_DAT", $getWhere005);
        }
        $db->query($query);

        //音楽専攻科
        if ($model->musicFlg) {
            //cnt
            $getCount033 = $db->getOne(knjl011eQuery::getCountDetail($model, "033"));
            //where
            $getWhere033 = knjl011eQuery::getWhereDetail($model, "033");

            $data = array();
            $data["REMARK1"][TEXT]              = $model->field["HOPE1"];       //第一希望
            $data["REMARK2"][TEXT]              = $model->field["HOPE2"];       //第二希望
            $data["REMARK3"][TEXT]              = $model->field["NOMAL_SHDIV"]; //普通科併願
            $data["REGISTERCD"][TEXT]           = STAFFCD;
            $data["UPDATED"][FUNC]              = "sysdate()";

            if ($getCount033 == 0) {
                $data["ENTEXAMYEAR"][TEXT]      = $model->year;
                $data["APPLICANTDIV"][TEXT]     = $model->field["APPLICANTDIV"];
                $data["EXAMNO"][TEXT]           = $model->examno;
                $data["SEQ"][TEXT]              = "033";
                $query = Query::insertSQL($data, "ENTEXAM_APPLICANTBASE_DETAIL_DAT");
            } else {
                $query = Query::updateSQL($data, "ENTEXAM_APPLICANTBASE_DETAIL_DAT", $getWhere033);
            }
            $db->query($query);

            //cnt
            $getCount030 = $db->getOne(knjl011eQuery::getCountDetail($model, "030"));
            //where
            $getWhere030 = knjl011eQuery::getWhereDetail($model, "030");

            $data = array();
            $data["REMARK6"][TEXT]              = $model->field["HOPE"];    //出願専攻
            $data["REGISTERCD"][TEXT]           = STAFFCD;
            $data["UPDATED"][FUNC]              = "sysdate()";

            if ($getCount030 == 0) {
                $data["ENTEXAMYEAR"][TEXT]      = $model->year;
                $data["APPLICANTDIV"][TEXT]     = $model->field["APPLICANTDIV"];
                $data["EXAMNO"][TEXT]           = $model->examno;
                $data["SEQ"][TEXT]              = "030";
                $query = Query::insertSQL($data, "ENTEXAM_APPLICANTBASE_DETAIL_DAT");
            } else {
                $query = Query::updateSQL($data, "ENTEXAM_APPLICANTBASE_DETAIL_DAT", $getWhere030);
            }
            $db->query($query);
        }

        //推薦資格審査
        if ($model->suisenFlg) {
            //cnt
            $getCount034 = $db->getOne(knjl011eQuery::getCountDetail($model, "034"));
            //where
            $getWhere034 = knjl011eQuery::getWhereDetail($model, "034");

            $data = array();
            $data["REMARK10"][TEXT]             = $model->field["QUALIFIED_CONTENT"];   // 資格内容
            $data["REMARK2"][TEXT]              = $model->field["QUALIFIED"];           // 資格（1:実績 2:推薦）
            $data["REMARK3"][TEXT]              = $model->field["PRACTICE"];            // 実技有無（1:未設定 2:有り 3:無し）
            $data["REMARK4"][TEXT]              = $model->field["POSSIBLE"];            // 判定（1:未設定 2:可 3:不可）
            $data["REGISTERCD"][TEXT]           = STAFFCD;
            $data["UPDATED"][FUNC]              = "sysdate()";

            if ($getCount034 == 0) {
                $data["ENTEXAMYEAR"][TEXT]      = $model->year;
                $data["APPLICANTDIV"][TEXT]     = $model->field["APPLICANTDIV"];
                $data["EXAMNO"][TEXT]           = $model->examno;
                $data["SEQ"][TEXT]              = "034";
                $query = Query::insertSQL($data, "ENTEXAM_APPLICANTBASE_DETAIL_DAT");
            } else {
                $query = Query::updateSQL($data, "ENTEXAM_APPLICANTBASE_DETAIL_DAT", $getWhere034);
            }
            $db->query($query);
        }

        $db->commit();
        Query::dbCheckIn($db);
        return;
    }

    //カウントAddr
    function getCountAddr($model) {
        $query  = " SELECT ";
        $query .= "     COUNT(*) ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_APPLICANTADDR_DAT ";
        $query .= " WHERE ";
        $query .= "         ENTEXAMYEAR     = '{$model->year}' ";
        $query .= "     AND APPLICANTDIV    = '{$model->field["APPLICANTDIV"]}' ";
        $query .= "     AND EXAMNO          = '{$model->examno}' ";

        return $query;
    }

    //カウントDetail
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

    //WHERE 条件セット Detail
    function getWhereDetail($model, $seq) {
        $query  = " WHERE ";
        $query .= "         ENTEXAMYEAR     = '{$model->year}' ";
        $query .= "     AND APPLICANTDIV    = '{$model->field["APPLICANTDIV"]}' ";
        $query .= "     AND EXAMNO          = '{$model->examno}' ";
        $query .= "     AND SEQ             = '{$seq}' ";
        return $query;
    }
}
?>