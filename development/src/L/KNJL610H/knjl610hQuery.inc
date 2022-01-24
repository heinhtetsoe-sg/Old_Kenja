<?php
class knjl610hQuery extends Query
{

    //名称マスタ
    public function getNameMst($model, $namecd1, $namecd2 = "")
    {
        $query  = " SELECT ";
        $query .= "     NAMECD2 AS VALUE, ";
        $query .= "     NAMECD2 || ':' || NAME1 AS LABEL ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR    = '".$model->year."' ";
        $query .= " AND NAMECD1 = '".$namecd1."' ";
        if ($namecd2) {
            $query .= " AND NAMECD2 = '".$namecd2."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";
        return $query;
    }

    //一覧取得
    public function getList($model)
    {
        $query  = " SELECT ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_APPLICANTBASE_DAT ";
        $query .= " WHERE  ";
        $query .= "     ENTEXAMYEAR  = '".$model->year."' ";
        $query .= " AND APPLICANTDIV = '".$model->applicantdiv."' ";
        $query .= " ORDER BY ";
        if ($model->sort == "2") {
            $query .= "     EXAMNO, ";
        }
        $query .= "     NAME_KANA ";
        return $query;
    }

    //１レコード取得
    public function getRow($model)
    {
        $query  = " SELECT ";
        $query .= "     B1.EXAMNO, ";
        $query .= "     B1.RECEPTDATE, ";
        $query .= "     B1.TESTDIV1, ";
        $query .= "     B1.NAME, ";
        $query .= "     B1.NAME_KANA, ";
        $query .= "     B1.BIRTHDAY, ";
        $query .= "     B1.SEX, ";
        $query .= "     B1.FS_CD AS FINSCHOOLCD, ";
        $query .= "     BD1.REMARK8, ";
        $query .= "     BD1.REMARK9, ";
        $query .= "     BD1.REMARK10, ";
        $query .= "     B1.TESTDIV, ";
        $query .= "     B1.SLIDE_FLG, ";
        $query .= "     CASE WHEN B1.JUDGEMENT = '3' ";
        $query .= "          THEN '1' ";
        $query .= "          ELSE '' ";
        $query .= "     END AS DECLINE, ";
        $query .= "     B1.JUDGEMENT, ";
        $query .= "     CONF1.CONFIDENTIAL_RPT01, ";
        $query .= "     CONF1.CONFIDENTIAL_RPT02, ";
        $query .= "     CONF1.CONFIDENTIAL_RPT03, ";
        $query .= "     CONF1.CONFIDENTIAL_RPT04, ";
        $query .= "     CONF1.CONFIDENTIAL_RPT05, ";
        $query .= "     CONF1.CONFIDENTIAL_RPT06, ";
        $query .= "     CONF1.CONFIDENTIAL_RPT07, ";
        $query .= "     CONF1.CONFIDENTIAL_RPT08, ";
        $query .= "     CONF1.CONFIDENTIAL_RPT09, ";
        $query .= "     CONF1.CONFIDENTIAL_RPT10, ";
        $query .= "     CONF1.CONFIDENTIAL_RPT11, ";
        $query .= "     CONF1.CONFIDENTIAL_RPT12, ";
        $query .= "     CONF1.TOTAL5, ";
        $query .= "     CONF1.TOTAL3, ";
        $query .= "     CONF1.TOTAL_ALL ";
        $query .= " FROM  ";
        $query .= "     ENTEXAM_APPLICANTBASE_DAT B1 ";
        $query .= "     LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT BD1 ";
        $query .= "            ON BD1.ENTEXAMYEAR  = B1.ENTEXAMYEAR ";
        $query .= "           AND BD1.APPLICANTDIV = B1.APPLICANTDIV ";
        $query .= "           AND BD1.EXAMNO       = B1.EXAMNO ";
        $query .= "           AND BD1.SEQ          = '031' ";
        $query .= "     LEFT JOIN ENTEXAM_APPLICANTCONFRPT_DAT CONF1 ";
        $query .= "            ON CONF1.ENTEXAMYEAR  = B1.ENTEXAMYEAR ";
        $query .= "           AND CONF1.APPLICANTDIV = B1.APPLICANTDIV ";
        $query .= "           AND CONF1.EXAMNO       = B1.EXAMNO ";
        $query .= " WHERE  ";
        $query .= "     B1.ENTEXAMYEAR  = '".$model->year."' ";
        $query .= " AND B1.APPLICANTDIV = '".$model->applicantdiv."' ";
        $query .= " AND B1.EXAMNO       = '".$model->examno."' ";
        return $query;
    }

    //ENTEXAM_SETTING_MSTより取得
    public function getEntexamSettingMst($model, $namecd1, $seq = "")
    {
        $query  = " SELECT ";
        $query .= "     SEQ AS VALUE, ";
        $query .= "     SEQ || ':' || NAME1 AS LABEL, ";
        $query .= "     * ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_SETTING_MST ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR  = '".$model->year."' ";
        $query .= " AND APPLICANTDIV = '".$model->applicantdiv."' ";
        $query .= " AND SETTING_CD   = '".$namecd1."' ";
        if ($seq) {
            $query .= " AND SEQ = '".$seq."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";
        return $query;
    }

    //入試区分取得
    public function getTestDiv($model, $testdiv = "")
    {
        $query  = " SELECT ";
        $query .= "     TESTDIV || ':' || VALUE(TESTDIV_NAME, '') AS LABEL,  ";
        $query .= "     TESTDIV AS VALUE ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_TESTDIV_MST ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR  = '".$model->year."' ";
        $query .= " AND APPLICANTDIV = '".$model->applicantdiv."' ";
        if ($testdiv) {
            $query .= " AND TESTDIV = '".$testdiv."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";
        return $query;
    }

    //類別マスタ取得
    public function getEntexamClassifyMst($model, $classifyCd = "")
    {
        $query  = " SELECT ";
        $query .= "     CLASSIFY_CD || ':' || VALUE(CLASSIFY_NAME, '') AS LABEL,  ";
        $query .= "     CLASSIFY_CD AS VALUE ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_CLASSIFY_MST ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR  = '".$model->year."' ";
        $query .= " AND APPLICANTDIV = '".$model->applicantdiv."' ";
        if ($classifyCd) {
            $query .= " AND CLASSIFY_CD = '".$classifyCd."' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";
        return $query;
    }

    //出身学校マスタよりデータを取得
    public function getFinschoolName($cd)
    {
        $query  = " SELECT  T1.FINSCHOOL_NAME, VALUE(L3.NAME1, '') AS FINSCHOOL_DISTCD_NAME ";
        $query .= "   FROM  FINSCHOOL_MST T1 ";
        $query .= "         LEFT JOIN NAME_MST L3 ON  L3.NAMECD1 = 'L001' ";
        $query .= "                               AND L3.NAMECD2 = T1.FINSCHOOL_DISTCD ";
        $query .= "  WHERE  T1.FINSCHOOLCD = '".$cd."' ";
        return $query;
    }

    //登録番号の最大取得
    public function getMaxExamno($model, $year)
    {
        $query  = " SELECT ";
        $query .= "     MAX(EXAMNO) AS EXAMNO ";
        $query .= " FROM  ";
        $query .= "     ENTEXAM_APPLICANTBASE_DAT ";
        $query .= " WHERE  ";
        $query .= "     ENTEXAMYEAR  = '".$year."' ";
        $query .= " AND APPLICANTDIV = '".$model->applicantdiv."' ";
        return $query;
    }

    //和暦変換
    public function getWarekiCd($model, $setDay)
    {
        $query  = " SELECT ";
        $query .= "     NAMECD2, ";
        $query .= "     NAMESPARE1 ";
        $query .= " FROM ";
        $query .= "     V_NAME_MST ";
        $query .= " WHERE ";
        $query .= "     YEAR    = '".$model->year."' ";
        $query .= " AND NAMECD1 = 'L007' ";
        $query .= " AND '{$setDay}' BETWEEN NAMESPARE2 AND NAMESPARE3 ";

        return $query;
    }

    //INSERT
    public function &getInsertQuery($model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $judgement = ($model->field["DECLINE"] == "1") ? "3" : "";
        $model->field["FINSCHOOLCD"] = (strlen($model->field["FINSCHOOLCD"])) ? sprintf("%07s", $model->field["FINSCHOOLCD"]) : "";

        //生年月日セット（西暦→和暦）
        $birthRow = $db->getRow(knjl610hQuery::getWarekiCd($model, $model->field["BIRTHDAY"]), DB_FETCHMODE_ASSOC);
        $eraCd    = $birthRow['NAMECD2']; //元号
        list($bYear, $bMonth, $bDay) = explode('/', $model->field["BIRTHDAY"]);
        $eraYear  = sprintf("%02d", $bYear - $birthRow['NAMESPARE1'] + 1);

        $data = array();
        $data["ENTEXAMYEAR"][TEXT]      = $model->year;
        $data["APPLICANTDIV"][TEXT]     = $model->applicantdiv;
        $data["EXAMNO"][TEXT]           = sprintf("%04d", $model->maxExamno);
        $data["SHDIV"][TEXT]            = "1";
        $data["DESIREDIV"][TEXT]        = "1";
        $data["RECEPTDATE"][DATE]       = str_replace("/", "-", $model->field["RECEPTDATE"]);
        $data["JUDGEMENT"][TEXT]        = $judgement;
        $data["TESTDIV"][TEXT]          = $model->field["TESTDIV"];
        $data["SLIDE_FLG"][TEXT]        = $model->field["SLIDE_FLG"];
        $data["TESTDIV1"][TEXT]         = $model->field["TESTDIV1"];
        $data["NAME"][TEXT]             = $model->field["NAME"];
        $data["NAME_KANA"][TEXT]        = $model->field["NAME_KANA"];
        $data["ERACD"][TEXT]            = $eraCd;
        $data["BIRTH_Y"][TEXT]          = $eraYear;
        $data["BIRTH_M"][TEXT]          = $bMonth;
        $data["BIRTH_D"][TEXT]          = $bDay;
        $data["BIRTHDAY"][DATE]         = str_replace("/", "-", $model->field["BIRTHDAY"]);
        $data["SEX"][TEXT]              = $model->field["SEX"];
        $data["FS_CD"][TEXT]            = $model->field["FINSCHOOLCD"];
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][NUMBER]        = "sysdate()";
        $query = Query::insertSQL($data, "ENTEXAM_APPLICANTBASE_DAT");
        $db->query($query);

        //更新 ENTEXAM_APPLICANTBASE_DETAIL_DAT
        knjl610hQuery::getUpdateEntexamApplicantbaseDetailDat($db, $model, $model->maxExamno);

        //更新 ENTEXAM_APPLICANTCONFRPT_DAT
        knjl610hQuery::getUpdateEntexamApplicantconfrptDat($db, $model, $model->maxExamno);

        $db->commit();
        Query::dbCheckIn($db);

        return true;
    }

    //UPDATE
    public function &getUpdateQuery($model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $judgement = ($model->field["DECLINE"] == "1") ? "3" : "";
        $model->field["FINSCHOOLCD"] = (strlen($model->field["FINSCHOOLCD"])) ? sprintf("%07s", $model->field["FINSCHOOLCD"]) : "";

        //生年月日セット（西暦→和暦）
        $birthRow = $db->getRow(knjl610hQuery::getWarekiCd($model, $model->field["BIRTHDAY"]), DB_FETCHMODE_ASSOC);
        $eraCd    = $birthRow['NAMECD2']; //元号
        list($bYear, $bMonth, $bDay) = explode('/', $model->field["BIRTHDAY"]);
        $eraYear  = sprintf("%02d", $bYear - $birthRow['NAMESPARE1'] + 1);

        $data = array();
        $data["RECEPTDATE"][DATE]       = str_replace("/", "-", $model->field["RECEPTDATE"]);
        $data["JUDGEMENT"][TEXT]        = $judgement;
        $data["TESTDIV"][TEXT]          = $model->field["TESTDIV"];
        $data["SLIDE_FLG"][TEXT]        = $model->field["SLIDE_FLG"];
        $data["TESTDIV1"][TEXT]         = $model->field["TESTDIV1"];
        $data["NAME"][TEXT]             = $model->field["NAME"];
        $data["NAME_KANA"][TEXT]        = $model->field["NAME_KANA"];
        $data["ERACD"][TEXT]            = $eraCd;
        $data["BIRTH_Y"][TEXT]          = $eraYear;
        $data["BIRTH_M"][TEXT]          = $bMonth;
        $data["BIRTH_D"][TEXT]          = $bDay;
        $data["BIRTHDAY"][DATE]         = str_replace("/", "-", $model->field["BIRTHDAY"]);
        $data["SEX"][TEXT]              = $model->field["SEX"];
        $data["FS_CD"][TEXT]            = $model->field["FINSCHOOLCD"];
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][NUMBER]        = "sysdate()";

        $where  = " WHERE ";
        $where .= "     ENTEXAMYEAR  = '".$model->year."' ";
        $where .= " AND APPLICANTDIV = '".$model->applicantdiv."' ";
        $where .= " AND EXAMNO       = '".$model->examno."' ";
        $query = Query::updateSQL($data, "ENTEXAM_APPLICANTBASE_DAT", $where);
        $db->query($query);

        //更新 ENTEXAM_APPLICANTBASE_DETAIL_DAT
        knjl610hQuery::getUpdateEntexamApplicantbaseDetailDat($db, $model, $model->examno);

        //更新 ENTEXAM_APPLICANTCONFRPT_DAT
        knjl610hQuery::getUpdateEntexamApplicantconfrptDat($db, $model, $model->examno);

        $db->commit();
        Query::dbCheckIn($db);

        return true;
    }

    //UPDATE -- ENTEXAM_APPLICANTBASE_DETAIL_DAT
    public function &getUpdateEntexamApplicantbaseDetailDat($db, $model, $examno)
    {
        $seq = "031";
        $where  = " WHERE ";
        $where .= "     ENTEXAMYEAR  = '".$model->year."' ";
        $where .= " AND APPLICANTDIV = '".$model->applicantdiv."' ";
        $where .= " AND EXAMNO       = '".$examno."' ";
        $where .= " AND SEQ          = '".$seq."' ";

        //件数取得
        $cnt = $db->getOne("SELECT COUNT(*) AS COUNT FROM ENTEXAM_APPLICANTBASE_DETAIL_DAT ".$where);

        $data = array();
        $data["ENTEXAMYEAR"][TEXT]      = $model->year;
        $data["APPLICANTDIV"][TEXT]     = $model->applicantdiv;
        $data["EXAMNO"][TEXT]           = sprintf("%04d", $examno);
        $data["SEQ"][TEXT]              = $seq;
        $data["REMARK8"][TEXT]          = $model->field["REMARK8"];
        $data["REMARK9"][TEXT]          = $model->field["REMARK9"];
        $data["REMARK10"][TEXT]         = $model->field["REMARK10"];
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][NUMBER]        = "sysdate()";

        if ($cnt == 0) {
            //新規
            $query = Query::insertSQL($data, "ENTEXAM_APPLICANTBASE_DETAIL_DAT");
            $db->query($query);
        } else {
            //更新
            $query = Query::updateSQL($data, "ENTEXAM_APPLICANTBASE_DETAIL_DAT", $where);
            $db->query($query);
        }
    }

    //UPDATE -- ENTEXAM_APPLICANTCONFRPT_DAT
    public function &getUpdateEntexamApplicantconfrptDat($db, $model, $examno)
    {
        $where  = " WHERE ";
        $where .= "     ENTEXAMYEAR  = '".$model->year."' ";
        $where .= " AND APPLICANTDIV = '".$model->applicantdiv."' ";
        $where .= " AND EXAMNO       = '".$examno."' ";

        //件数取得
        $cnt = $db->getOne("SELECT COUNT(*) AS COUNT FROM ENTEXAM_APPLICANTCONFRPT_DAT ".$where);

        $data = array();
        $data["ENTEXAMYEAR"][TEXT]              = $model->year;
        $data["APPLICANTDIV"][TEXT]             = $model->applicantdiv;
        $data["EXAMNO"][TEXT]                   = sprintf("%04d", $examno);
        $data["CONFIDENTIAL_RPT01"][NUMBER]     = $model->field["CONFIDENTIAL_RPT01"];
        $data["CONFIDENTIAL_RPT02"][NUMBER]     = $model->field["CONFIDENTIAL_RPT02"];
        $data["CONFIDENTIAL_RPT03"][NUMBER]     = $model->field["CONFIDENTIAL_RPT03"];
        $data["CONFIDENTIAL_RPT04"][NUMBER]     = $model->field["CONFIDENTIAL_RPT04"];
        $data["CONFIDENTIAL_RPT05"][NUMBER]     = $model->field["CONFIDENTIAL_RPT05"];
        $data["TOTAL3"][NUMBER]                 = $model->field["TOTAL3"];
        $data["TOTAL5"][NUMBER]                 = $model->field["TOTAL5"];
        $data["TOTAL_ALL"][NUMBER]              = $model->field["TOTAL_ALL"];
        $data["REGISTERCD"][TEXT]               = STAFFCD;
        $data["UPDATED"][NUMBER]                = "sysdate()";

        if ($cnt == 0) {
            //新規
            $query = Query::insertSQL($data, "ENTEXAM_APPLICANTCONFRPT_DAT");
            $db->query($query);
        } else {
            //更新
            $query = Query::updateSQL($data, "ENTEXAM_APPLICANTCONFRPT_DAT", $where);
            $db->query($query);
        }
    }

    //DELETE
    public function &getDeleteQuery($model)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //削除 -- ENTEXAM_APPLICANTBASE_DAT
        $query  = " DELETE FROM ";
        $query .= "     ENTEXAM_APPLICANTBASE_DAT ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR  = '".$model->year."' ";
        $query .= " AND APPLICANTDIV = '".$model->applicantdiv."' ";
        $query .= " AND EXAMNO       = '".$model->examno."' ";
        $db->query($query);

        //削除 -- ENTEXAM_APPLICANTBASE_DETAIL_DAT
        $seq = "031";
        $query  = " DELETE FROM ";
        $query .= "     ENTEXAM_APPLICANTBASE_DETAIL_DAT ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR  = '".$model->year."' ";
        $query .= " AND APPLICANTDIV = '".$model->applicantdiv."' ";
        $query .= " AND EXAMNO       = '".$model->examno."' ";
        $query .= " AND SEQ          = '".$seq."' ";
        $db->query($query);

        //削除 -- ENTEXAM_APPLICANTCONFRPT_DAT
        $query  = " DELETE FROM ";
        $query .= "     ENTEXAM_APPLICANTCONFRPT_DAT ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR  = '".$model->year."' ";
        $query .= " AND APPLICANTDIV = '".$model->applicantdiv."' ";
        $query .= " AND EXAMNO       = '".$model->examno."' ";
        $db->query($query);

        $db->commit();
        Query::dbCheckIn($db);

        return true;
    }

    //エラーデータの削除
    public function deleteQueryErr()
    {
        $query  = " DELETE FROM W_CSVMSG_PRG_DAT WHERE PROGRAMID = '".PROGRAMID."' ";
        return $query;
    }

    //エラーＤＢへの追加
    public function insertQueryErr(&$db, $record_no, $check_error)
    {
        $data1 = array();
        $data1["PROGRAMID"][TEXT]   = PROGRAMID;
        $data1["MSGROW"][NUMBER]    = $record_no;
        $data1["MSGREMARK"][TEXT]   = $check_error;

        $query = Query::insertSQL($data1, "W_CSVMSG_PRG_DAT");
        $result = $db->query($query);
    }

    //CSV取込
    public function insertQueryCsv($db, $model, $data_arr)
    {
        $data = array();
        $db->autoCommit(false);

        $cnt = 0; //処理件数
        $insCnt = 0;
        for ($i = 0; $i < count($data_arr); $i++) {
            $data_arr[$i]["EXAMNO"] = sprintf("%04d", $data_arr[$i]["EXAMNO"]);
            $data_arr[$i]["FINSCHOOLCD"] = (strlen($data_arr[$i]["FINSCHOOLCD"])) ? sprintf("%07s", $data_arr[$i]["FINSCHOOLCD"]) : "";

            //生年月日セット（西暦→和暦）
            $birthRow = $db->getRow(knjl610hQuery::getWarekiCd($model, $data_arr[$i]["BIRTHDAY"]), DB_FETCHMODE_ASSOC);
            $eraCd    = $birthRow['NAMECD2']; //元号
            str_replace("/", "-", $data_arr[$i]["BIRTHDAY"]);
            list($bYear, $bMonth, $bDay) = explode('-', $data_arr[$i]["BIRTHDAY"]);
            $eraYear  = sprintf("%02d", $bYear - $birthRow['NAMESPARE1'] + 1);

            //データセット
            $data = array();
            $data["RECEPTDATE"][DATE]       = str_replace("/", "-", $data_arr[$i]["RECEPTDATE"]);
            $data["TESTDIV"][TEXT]          = $data_arr[$i]["TESTDIV"];
            $data["TESTDIV1"][TEXT]         = $data_arr[$i]["TESTDIV1"];
            $data["NAME"][TEXT]             = $data_arr[$i]["NAME"];
            $data["NAME_KANA"][TEXT]        = $data_arr[$i]["NAME_KANA"];
            $data["ERACD"][TEXT]            = $eraCd;
            $data["BIRTH_Y"][TEXT]          = $eraYear;
            $data["BIRTH_M"][TEXT]          = $bMonth;
            $data["BIRTH_D"][TEXT]          = $bDay;
            $data["BIRTHDAY"][DATE]         = $data_arr[$i]["BIRTHDAY"];
            $data["SEX"][TEXT]              = $data_arr[$i]["SEX"];
            $data["FS_CD"][TEXT]            = $data_arr[$i]["FINSCHOOLCD"];
            $data["SLIDE_FLG"][TEXT]        = $data_arr[$i]["SLIDE_FLG"];
            $data["REGISTERCD"][TEXT]       = STAFFCD;
            $data["UPDATED"][NUMBER]        = "sysdate()";

            $where  = " WHERE ";
            $where .= "     ENTEXAMYEAR  = '".$data_arr[$i]["ENTEXAMYEAR"]."' ";
            $where .= " AND APPLICANTDIV = '".$model->applicantdiv."' ";
            $where .= " AND EXAMNO       = '".$data_arr[$i]["EXAMNO"]."' ";

            $insFlg = false;
            $getCnt = $db->getOne("SELECT COUNT(*) AS COUNT FROM ENTEXAM_APPLICANTBASE_DAT ".$where);
            if ($getCnt == 0) {
                //登録番号が未登録の場合、追加
                $insFlg = true;
            }

            if ($insFlg) {
                //追加
                $data["ENTEXAMYEAR"][TEXT]      = $data_arr[$i]["ENTEXAMYEAR"];
                $data["APPLICANTDIV"][TEXT]     = $model->applicantdiv;
                $data["EXAMNO"][TEXT]           = $data_arr[$i]["EXAMNO"];
                $data["SHDIV"][TEXT]            = "1";
                $data["DESIREDIV"][TEXT]        = "1";
                $query = Query::insertSQL($data, "ENTEXAM_APPLICANTBASE_DAT");
            } else {
                //更新
                $query = Query::updateSQL($data, "ENTEXAM_APPLICANTBASE_DAT", $where);
            }
            $db->query($query);
            
            
            //更新 ENTEXAM_APPLICANTBASE_DETAIL_DAT
            knjl610hQuery::getCsvUpdateEntexamApplicantbaseDetailDat($db, $model, $data_arr[$i]);

            //更新 ENTEXAM_APPLICANTCONFRPT_DAT
            knjl610hQuery::getCsvUpdateEntexamApplicantconfrptDat($db, $model, $data_arr[$i]);
            
            $cnt++;
        }
        $db->commit();

        return $cnt;
    }

    //CSV取込み -- ENTEXAM_APPLICANTBASE_DETAIL_DAT
    public function &getCsvUpdateEntexamApplicantbaseDetailDat($db, $model, $field)
    {
        $seq = "031";
        $where  = " WHERE ";
        $where .= "     ENTEXAMYEAR  = '".$field["ENTEXAMYEAR"]."' ";
        $where .= " AND APPLICANTDIV = '".$model->applicantdiv."' ";
        $where .= " AND EXAMNO       = '".$field["EXAMNO"]."' ";
        $where .= " AND SEQ          = '".$seq."' ";

        //件数取得
        $cnt = $db->getOne("SELECT COUNT(*) AS COUNT FROM ENTEXAM_APPLICANTBASE_DETAIL_DAT ".$where);

        $data = array();
        $data["ENTEXAMYEAR"][TEXT]      = $field["ENTEXAMYEAR"];
        $data["APPLICANTDIV"][TEXT]     = $model->applicantdiv;
        $data["EXAMNO"][TEXT]           = $field["EXAMNO"];
        $data["SEQ"][TEXT]              = $seq;
        $data["REMARK8"][TEXT]          = $field["REMARK8"];
        $data["REMARK9"][TEXT]          = $field["REMARK9"];
        $data["REMARK10"][TEXT]         = $field["REMARK10"];
        $data["REGISTERCD"][TEXT]       = STAFFCD;
        $data["UPDATED"][NUMBER]        = "sysdate()";

        if ($cnt == 0) {
            //新規
            $query = Query::insertSQL($data, "ENTEXAM_APPLICANTBASE_DETAIL_DAT");
            $db->query($query);
        } else {
            //更新
            $query = Query::updateSQL($data, "ENTEXAM_APPLICANTBASE_DETAIL_DAT", $where);
            $db->query($query);
        }
    }

    //CSV取込み -- ENTEXAM_APPLICANTCONFRPT_DAT
    public function &getCsvUpdateEntexamApplicantconfrptDat($db, $model, $field)
    {
        $where  = " WHERE ";
        $where .= "     ENTEXAMYEAR  = '".$field["ENTEXAMYEAR"]."' ";
        $where .= " AND APPLICANTDIV = '".$model->applicantdiv."' ";
        $where .= " AND EXAMNO       = '".$field["EXAMNO"]."' ";

        //合計の算出
        $query = " SELECT * FROM ENTEXAM_APPLICANTCONFRPT_DAT ".$where;
        $Row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        $field["CONFIDENTIAL_RPT06"] = $Row["CONFIDENTIAL_RPT06"];
        $field["CONFIDENTIAL_RPT07"] = $Row["CONFIDENTIAL_RPT07"];
        $field["CONFIDENTIAL_RPT08"] = $Row["CONFIDENTIAL_RPT08"];
        $field["CONFIDENTIAL_RPT09"] = $Row["CONFIDENTIAL_RPT09"];
        $field["CONFIDENTIAL_RPT10"] = $Row["CONFIDENTIAL_RPT10"];
        $field["CONFIDENTIAL_RPT11"] = $Row["CONFIDENTIAL_RPT11"];
        $field["CONFIDENTIAL_RPT12"] = $Row["CONFIDENTIAL_RPT12"];
        $kyoka5_array = explode(",", $model->hid_5kyokaseq);
        $kyoka3_array = explode(",", $model->hid_3kyokaseq);
        $total5 = 0;
        foreach ($kyoka5_array as $kyokaseq) {
            $total5 += (int)$field["CONFIDENTIAL_RPT".$kyokaseq];
        }
        $total3 = 0;
        foreach ($kyoka3_array as $kyokaseq) {
            $total3 += (int)$field["CONFIDENTIAL_RPT".$kyokaseq];
        }
        $field["TOTAL5"] = $total5;
        $field["TOTAL3"] = $total3;

        //件数取得
        $cnt = $db->getOne("SELECT COUNT(*) AS COUNT FROM ENTEXAM_APPLICANTCONFRPT_DAT ".$where);

        $data = array();
        $data["ENTEXAMYEAR"][TEXT]              = $field["ENTEXAMYEAR"];
        $data["APPLICANTDIV"][TEXT]             = $model->applicantdiv;
        $data["EXAMNO"][TEXT]                   = $field["EXAMNO"];
        $data["CONFIDENTIAL_RPT01"][NUMBER]     = $field["CONFIDENTIAL_RPT01"];
        $data["CONFIDENTIAL_RPT02"][NUMBER]     = $field["CONFIDENTIAL_RPT02"];
        $data["CONFIDENTIAL_RPT03"][NUMBER]     = $field["CONFIDENTIAL_RPT03"];
        $data["CONFIDENTIAL_RPT04"][NUMBER]     = $field["CONFIDENTIAL_RPT04"];
        $data["CONFIDENTIAL_RPT05"][NUMBER]     = $field["CONFIDENTIAL_RPT05"];
        $data["TOTAL3"][NUMBER]                 = $field["TOTAL3"];
        $data["TOTAL5"][NUMBER]                 = $field["TOTAL5"];
        $data["TOTAL_ALL"][NUMBER]              = $field["TOTAL_ALL"];
        $data["REGISTERCD"][TEXT]               = STAFFCD;
        $data["UPDATED"][NUMBER]                = "sysdate()";

        if ($cnt == 0) {
            //新規
            $query = Query::insertSQL($data, "ENTEXAM_APPLICANTCONFRPT_DAT");
            $db->query($query);
        } else {
            //更新
            $query = Query::updateSQL($data, "ENTEXAM_APPLICANTCONFRPT_DAT", $where);
            $db->query($query);
        }
    }

    //エラー出力
    public function getErrorCsvData()
    {
        $query  = " SELECT ";
        $query .= "     MSGROW, ";
        $query .= "     MSGREMARK ";
        $query .= " FROM ";
        $query .= "     W_CSVMSG_PRG_DAT ";
        $query .= " WHERE ";
        $query .= "     PROGRAMID = '".PROGRAMID."' ";

        return $query;
    }

    //CSV書出データ取得
    public function getCsvData($model)
    {
        $query  = " SELECT ";
        $query .= "     B1.ENTEXAMYEAR, ";
        $query .= "     B1.EXAMNO, ";
        $query .= "     B1.RECEPTDATE, ";
        $query .= "     B1.TESTDIV1, ";
        $query .= "     B1.NAME, ";
        $query .= "     B1.NAME_KANA, ";
        $query .= "     B1.BIRTHDAY, ";
        $query .= "     B1.SEX, ";
        $query .= "     B1.FS_CD AS FINSCHOOLCD, ";
        $query .= "     L1.FINSCHOOL_NAME, ";
        $query .= "     BD1.REMARK8, ";
        $query .= "     BD1.REMARK9, ";
        $query .= "     BD1.REMARK10, ";
        $query .= "     B1.TESTDIV, ";
        $query .= "     B1.SLIDE_FLG, ";
        $query .= "     CASE WHEN B1.JUDGEMENT = '3' ";
        $query .= "          THEN '1' ";
        $query .= "          ELSE '' ";
        $query .= "     END AS DECLINE, ";
        $query .= "     B1.JUDGEMENT, ";
        $query .= "     CONF1.CONFIDENTIAL_RPT01, ";
        $query .= "     CONF1.CONFIDENTIAL_RPT02, ";
        $query .= "     CONF1.CONFIDENTIAL_RPT03, ";
        $query .= "     CONF1.CONFIDENTIAL_RPT04, ";
        $query .= "     CONF1.CONFIDENTIAL_RPT05, ";
        $query .= "     CONF1.CONFIDENTIAL_RPT06, ";
        $query .= "     CONF1.CONFIDENTIAL_RPT07, ";
        $query .= "     CONF1.CONFIDENTIAL_RPT08, ";
        $query .= "     CONF1.CONFIDENTIAL_RPT09, ";
        $query .= "     CONF1.CONFIDENTIAL_RPT10, ";
        $query .= "     CONF1.CONFIDENTIAL_RPT11, ";
        $query .= "     CONF1.CONFIDENTIAL_RPT12, ";
        $query .= "     CONF1.TOTAL5, ";
        $query .= "     CONF1.TOTAL3, ";
        $query .= "     CONF1.TOTAL_ALL ";
        $query .= " FROM  ";
        $query .= "     ENTEXAM_APPLICANTBASE_DAT B1 ";
        $query .= "     LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT BD1 ";
        $query .= "            ON BD1.ENTEXAMYEAR  = B1.ENTEXAMYEAR ";
        $query .= "           AND BD1.APPLICANTDIV = B1.APPLICANTDIV ";
        $query .= "           AND BD1.EXAMNO       = B1.EXAMNO ";
        $query .= "           AND BD1.SEQ          = '031' ";
        $query .= "     LEFT JOIN ENTEXAM_APPLICANTCONFRPT_DAT CONF1 ";
        $query .= "            ON CONF1.ENTEXAMYEAR  = B1.ENTEXAMYEAR ";
        $query .= "           AND CONF1.APPLICANTDIV = B1.APPLICANTDIV ";
        $query .= "           AND CONF1.EXAMNO       = B1.EXAMNO ";
        $query .= "     LEFT JOIN FINSCHOOL_MST L1 ";
        $query .= "            ON L1.FINSCHOOLCD = B1.FS_CD ";
        $query .= " WHERE  ";
        $query .= "     B1.ENTEXAMYEAR  = '".$model->year."' ";
        $query .= " AND B1.APPLICANTDIV = '".$model->applicantdiv."' ";
        $query .= " ORDER BY ";
        $query .= "     EXAMNO ";

        return $query;
    }
}
