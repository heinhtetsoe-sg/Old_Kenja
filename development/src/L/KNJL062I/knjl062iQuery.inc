<?php

class knjl062iquery extends Query
{
    //名称マスタL007取得
    public function getNameMst($model, $nameCd1)
    {
        $query  = "   SELECT ";
        $query .= "       NAMECD2, ";
        $query .= "       NAME1, ";
        $query .= "       NAME2, ";
        $query .= "       NAMESPARE1, ";
        $query .= "       NAMESPARE2, ";
        $query .= "       NAMESPARE3 ";
        $query .= "   FROM ";
        $query .= "       V_NAME_MST ";
        $query .= "   WHERE ";
        $query .= "       YEAR = '".(CTRL_YEAR + 1)."' ";
        $query .= "       AND NAMECD1 = '{$nameCd1}' ";

        return $query;
    }

    //入試設定マスタ取得
    public function getEntSettingMst($applicantDiv, $settingCd, $seq = "")
    {
        $query  = " SELECT ";
        $query .= "     SEQ VALUE, ";
        $query .= "     SEQ || ':' || NAME1 LABEL, ";
        $query .= "     NAME1 ";
        $query .= " FROM ";
        $query .= "     ENTEXAM_SETTING_MST ";
        $query .= " WHERE ";
        $query .= "     ENTEXAMYEAR         = '".(CTRL_YEAR + 1)."' ";
        $query .= "     AND APPLICANTDIV    = '{$applicantDiv}' ";
        $query .= "     AND SETTING_CD      = '{$settingCd}' ";
        if ($seq != "") {
            $query .= "     AND SEQ             = '{$seq}' ";
        }

        return $query;
    }

    //名称マスタ取得(入試制度)
    public function getApplicantDivMst()
    {
        $query  = "";
        $query .= " SELECT ";
        $query .= "     NAMECD2 VALUE ";
        $query .= "   , NAMECD2 || ':' || NAME1 LABEL ";
        $query .= " FROM V_NAME_MST ";
        $query .= " WHERE YEAR = '".(CTRL_YEAR + 1)."' ";
        $query .= "   AND NAMECD1 = 'L003' ";
        $query .= "   AND NAMESPARE3 = '". SCHOOLKIND ."' ";
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //入試制度名の取得
    public function getApplicantDivName($applicantDiv)
    {
        $query  = "";
        $query .= " SELECT ";
        $query .= "     NAME1 ";
        $query .= " FROM V_NAME_MST ";
        $query .= " WHERE YEAR = '".(CTRL_YEAR + 1)."' ";
        $query .= "   AND NAMECD1 = 'L003' ";
        $query .= "   AND NAMECD2 = '".$applicantDiv."' ";

        return $query;
    }

    //入試区分マスタ取得
    public function getEntexamTestDivMst($applicantDiv)
    {
        $query  = "";
        $query .= " SELECT ";
        $query .= "     TESTDIV VALUE ";
        $query .= "   , TESTDIV || ':' || TESTDIV_ABBV LABEL ";
        $query .= " FROM ENTEXAM_TESTDIV_MST ";
        $query .= " WHERE ENTEXAMYEAR = '".(CTRL_YEAR + 1)."' ";
        if ($applicantDiv != "") {
            $query .= "   AND APPLICANTDIV = '{$applicantDiv}' ";
        }
        $query .= " ORDER BY ";
        $query .= "     VALUE ";

        return $query;
    }

    //入試区分マスタ取得
    public function getEntexamTestDivName($applicantDiv, $testDiv)
    {
        $query  = "";
        $query .= " SELECT ";
        $query .= "     TESTDIV_ABBV ";
        $query .= " FROM ENTEXAM_TESTDIV_MST ";
        $query .= " WHERE ENTEXAMYEAR = '".(CTRL_YEAR + 1)."' ";
        if ($applicantDiv != "") {
            $query .= "   AND APPLICANTDIV = '{$applicantDiv}' ";
        }
        if ($testDiv != "") {
            $query .= "   AND TESTDIV = '{$testDiv}' ";
        }
        return $query;
    }



    //データの取得
    public function selectQuery($model)
    {
        $query  = "";
        $query .= " SELECT ";
        $query .= "     T1.ENTEXAMYEAR ";
        $query .= "   , T1.APPLICANTDIV ";
        $query .= "   , T1.TESTDIV ";
        $query .= "   , T1.EXAMNO ";
        $query .= "   , T1.NAME ";
        $query .= "   , T1.NAME_KANA ";
        $query .= "   , T1.SEX ";
        $query .= "   , T1.BIRTHDAY ";
        $query .= "   , T3.ZIPCD ";
        $query .= "   , T3.ADDRESS1 ";
        $query .= "   , T3.ADDRESS2 ";
        $query .= "   , T3.TELNO ";
        $query .= "   , T3.GNAME ";
        $query .= "   , T3.GKANA ";
        $query .= "   , T3.GZIPCD ";
        $query .= "   , T3.GADDRESS1 ";
        $query .= "   , T3.GADDRESS2 ";
        $query .= "   , T3.GTELNO ";
        $query .= "   , T3.GTELNO2 ";
        $query .= "   , T1.FS_CD ";
        $query .= "   , T1.FS_DAY ";
        $query .= "   , T1.FS_GRDDIV ";
        $query .= "   , T2.REMARK1 RANK ";
        $query .= "   , T2.REMARK2 SCHOOLCD1 ";
        $query .= "   , T2.REMARK3 SCHOOLCD2 ";
        $query .= "   , '' SEND_ZIPCD ";
        $query .= "   , '' SEND_ADDRESS1 ";
        $query .= "   , '' SEND_ADDRESS2 ";
        $query .= "   , '' SEND_TELNO ";
        $query .= " FROM ";
        $query .= "   ENTEXAM_APPLICANTBASE_DAT T1 ";
        $query .= "   LEFT JOIN ENTEXAM_APPLICANTBASE_DETAIL_DAT T2 ";
        $query .= "     ON T2.ENTEXAMYEAR = T1.ENTEXAMYEAR ";
        $query .= "     AND T2.APPLICANTDIV = T1.APPLICANTDIV ";
        $query .= "     AND T2.EXAMNO = T1.EXAMNO ";
        $query .= "     AND T2.SEQ = '005' ";
        $query .= "   LEFT JOIN ENTEXAM_APPLICANTADDR_DAT T3 ";
        $query .= "     ON T3.ENTEXAMYEAR = T1.ENTEXAMYEAR ";
        $query .= "     AND T3.APPLICANTDIV = T1.APPLICANTDIV ";
        $query .= "     AND T3.EXAMNO = T1.EXAMNO ";
        
        $query .= " WHERE ";
        $query .= "     T1.ENTEXAMYEAR = '".(CTRL_YEAR + 1)."' ";
        $query .= " AND T1.APPLICANTDIV = '{$model->field["APPLICANTDIV"]}' ";
        $query .= " AND T1.TESTDIV = '{$model->field["TESTDIV"]}' ";

        $query .= " ORDER BY ";
        $query .= "     T1.ENTEXAMYEAR ";
        $query .= "   , T1.APPLICANTDIV ";
        $query .= "   , T1.TESTDIV ";
        $query .= "   , T1.EXAMNO ";

        return $query;
    }

    /* データ更新処理 */
    public function update($model)
    {
        $db = Query::dbCheckOut();

        //和暦情報取得(※入力日付の形式はYYYY-MM-DD)
        $getEraInfo = function ($targetDate, $nameMstL007) {
            $eraCd = "";
            $eraYear = "";
            foreach ($nameMstL007 as $nameCd2 => $eraInfo) {
                if ($eraInfo["ERA_SDATE"] <= $targetDate && $targetDate <= $eraInfo["ERA_EDATE"]) {
                    $eraCd = $nameCd2;
                    $y = substr($targetDate, 0, 4);
                    $m = substr($targetDate, 5, 2);
                    $d = substr($targetDate, 8, 2);
                    $tmp = ($y + 1) - $eraInfo["ERA_SYEAR"];
                    $eraYear = sprintf("%02d", $tmp);
                    $eraMonth = $m;
                    $eraDay = $d;
                }
            }
            return array($eraCd, $eraYear, $eraMonth, $eraDay);
        };

        foreach ($model->entexamApplicantList as $applicant) {

            /*** ENTEXAM_APPLICANTBASE_DAT ***/
            $data = array();
            $data["ENTEXAMYEAR"][TEXT]  = $applicant["ENTEXAMYEAR"];
            $data["APPLICANTDIV"][TEXT] = $applicant["APPLICANTDIV"];
            $data["TESTDIV"][TEXT]      = $applicant["TESTDIV"];
            $data["SHDIV"][TEXT]        = "1";
            $data["DESIREDIV"][TEXT]    = "1";

            $applicant["EXAMNO"] = substr($applicant["EXAMNO"], 6);
            $data["EXAMNO"][TEXT]       = $applicant["EXAMNO"];

            $data["NAME"][TEXT]         = $applicant["NAME"];
            $data["NAME_KANA"][TEXT]    = $applicant["NAME_KANA"];
            $data["SEX"][TEXT]          = $applicant["SEX"];


            //日付整形(誕生日)
            $applicant["BIRTHDAY"] = str_replace("/", "-", $applicant["BIRTHDAY"]);
            list($eraCd, $eraYear, $eraMonth, $eraDay) = $getEraInfo($applicant["BIRTHDAY"], $model->nameMstL007);
            $data["BIRTHDAY"][TEXT] = $applicant["BIRTHDAY"];
            $data["ERACD"][TEXT]    = $eraCd;
            $data["BIRTH_Y"][TEXT]  = $eraYear;
            $data["BIRTH_M"][TEXT]  = $eraMonth;
            $data["BIRTH_D"][TEXT]  = $eraDay;

            // //日付整形(卒業日付)
            $applicant["FS_DAY"]  = str_replace("/", "-", $applicant["FS_YM"]."/31");
            list($eraCd, $eraYear, $eraMonth, $eraDay) = $getEraInfo($applicant["FS_DAY"], $model->nameMstL007);
            $data["FS_DAY"][TEXT]       = $applicant["FS_DAY"];
            $data["FS_GRDYEAR"][TEXT]   = substr($applicant["FS_YM"], 0, 4);
            $data["FS_ERACD"][TEXT]     = $eraCd;
            $data["FS_Y"][TEXT]         = $eraYear;
            $data["FS_M"][TEXT]         = $eraMonth;

            $data["FS_CD"][TEXT]        = $applicant["FS_CD"];
            $data["FS_GRDDIV"][TEXT]    = $applicant["FS_GRDDIV"];

            $data["REGISTERCD"][TEXT]   = STAFFCD ;
            $data["UPDATED"][FUNC]      = "sysdate()";

            //存在チェック
            $query = knjl062iQuery::selectEntexamApplicantBaseDat($applicant);
            $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
            if ($row) {
                //更新
                $where  = " WHERE ENTEXAMYEAR = '{$applicant["ENTEXAMYEAR"]}'";
                $where .= "   AND APPLICANTDIV = '{$applicant["APPLICANTDIV"]}'";
                $where .= "   AND EXAMNO = '{$applicant["EXAMNO"]}'";
                $query = Query::updateSQL($data, "ENTEXAM_APPLICANTBASE_DAT", $where);
                $db->query($query);
            } else {
                //追加
                $query = Query::insertSQL($data, "ENTEXAM_APPLICANTBASE_DAT");
                $db->query($query);
            }

            /*** ENTEXAM_APPLICANTBASE_DETAIL_DAT ***/
            $data = array();
            $data["ENTEXAMYEAR"][TEXT]  = $applicant["ENTEXAMYEAR"];
            $data["APPLICANTDIV"][TEXT] = $applicant["APPLICANTDIV"];
            $data["EXAMNO"][TEXT]       = $applicant["EXAMNO"];
            $data["SEQ"][TEXT]          = "005";
            $data["REMARK1"][TEXT]      = $applicant["RANK"];
            $data["REMARK2"][TEXT]      = $applicant["SCHOOLCD1"];
            $data["REMARK3"][TEXT]      = $applicant["SCHOOLCD2"];
            $data["REGISTERCD"][TEXT]   = STAFFCD ;
            $data["UPDATED"][FUNC]      = "sysdate()";

            if ($row) {
                //更新
                $where  = " WHERE ENTEXAMYEAR = '{$applicant["ENTEXAMYEAR"]}'";
                $where .= "   AND APPLICANTDIV = '{$applicant["APPLICANTDIV"]}'";
                $where .= "   AND EXAMNO = '{$applicant["EXAMNO"]}'";
                $query = Query::updateSQL($data, "ENTEXAM_APPLICANTBASE_DETAIL_DAT", $where);
                $db->query($query);
            } else {
                //追加
                $query = Query::insertSQL($data, "ENTEXAM_APPLICANTBASE_DETAIL_DAT");
                $db->query($query);
            }

            /*** ENTEXAM_APPLICANTADDR_DAT ***/
            $data = array();
            $data["ENTEXAMYEAR"][TEXT]  = $applicant["ENTEXAMYEAR"];
            $data["APPLICANTDIV"][TEXT] = $applicant["APPLICANTDIV"];
            $data["EXAMNO"][TEXT]       = $applicant["EXAMNO"];

            $data["ZIPCD"][TEXT]        = $applicant["ZIPCD"];
            $data["ADDRESS1"][TEXT]     = $applicant["ADDRESS1"];
            $data["ADDRESS2"][TEXT]     = $applicant["ADDRESS2"];
            $data["TELNO"][TEXT]        = $applicant["TELNO"];

            $data["GNAME"][TEXT]        = $applicant["GNAME"];
            $data["GKANA"][TEXT]        = $applicant["GKANA"];
            $data["GZIPCD"][TEXT]       = $applicant["GZIPCD"];
            $data["GADDRESS1"][TEXT]    = $applicant["GADDRESS1"];
            $data["GADDRESS2"][TEXT]    = $applicant["GADDRESS2"];
            $data["GTELNO"][TEXT]       = $applicant["GTELNO"];
            $data["GTELNO2"][TEXT]      = $applicant["GTELNO2"];

            $data["SEND_ZIPCD"][TEXT]    = $applicant["SEND_ZIPCD"];
            $data["SEND_ADDRESS1"][TEXT] = $applicant["SEND_ADDRESS1"];
            $data["SEND_ADDRESS2"][TEXT] = $applicant["SEND_ADDRESS2"];
            $data["SEND_TELNO"][TEXT]    = $applicant["SEND_TELNO"];

            $data["REGISTERCD"][TEXT]   = STAFFCD ;
            $data["UPDATED"][FUNC]      = "sysdate()";

            if ($row) {
                //更新
                $where  = " WHERE ENTEXAMYEAR = '{$applicant["ENTEXAMYEAR"]}'";
                $where .= "   AND APPLICANTDIV = '{$applicant["APPLICANTDIV"]}'";
                $where .= "   AND EXAMNO = '{$applicant["EXAMNO"]}'";
                $query = Query::updateSQL($data, "ENTEXAM_APPLICANTADDR_DAT", $where);
                $db->query($query);
            } else {
                //追加
                $query = Query::insertSQL($data, "ENTEXAM_APPLICANTADDR_DAT");
                $db->query($query);
            }
        }

        Query::dbCheckIn($db);
    }

    /* データ削除処理 */
    public function delete($model)
    {
        $db = Query::dbCheckOut();

        foreach ($model->entexamApplicantList as $applicant) {

            /*** ENTEXAM_APPLICANTBASE_DAT ***/
            $data = array();
            $data["ENTEXAMYEAR"][TEXT]  = $applicant["ENTEXAMYEAR"];
            $data["APPLICANTDIV"][TEXT] = $applicant["APPLICANTDIV"];
            $data["TESTDIV"][TEXT]      = $applicant["TESTDIV"];

            $applicant["EXAMNO"] = substr($applicant["EXAMNO"], 6);
            $data["EXAMNO"][TEXT]       = $applicant["EXAMNO"];


            $query  = " DELETE FROM ENTEXAM_APPLICANTBASE_DAT ";
            $query .= " WHERE ENTEXAMYEAR = '{$applicant["ENTEXAMYEAR"]}}'";
            $query .= "   AND APPLICANTDIV = '{$applicant["APPLICANTDIV"]}'";
            $query .= "   AND EXAMNO = '{$applicant["EXAMNO"]}'";
            $db->query($query);

            /*** ENTEXAM_APPLICANTBASE_DETAIL_DAT ***/
            $query  = " DELETE FROM ENTEXAM_APPLICANTBASE_DETAIL_DAT ";
            $query .= " WHERE ENTEXAMYEAR = '{$applicant["ENTEXAMYEAR"]}}'";
            $query .= "   AND APPLICANTDIV = '{$applicant["APPLICANTDIV"]}'";
            $query .= "   AND EXAMNO = '{$applicant["EXAMNO"]}'";
            $db->query($query);

            /*** ENTEXAM_APPLICANTADDR_DAT ***/
            $query  = " DELETE FROM ENTEXAM_APPLICANTADDR_DAT ";
            $query .= " WHERE ENTEXAMYEAR = '{$applicant["ENTEXAMYEAR"]}}'";
            $query .= "   AND APPLICANTDIV = '{$applicant["APPLICANTDIV"]}'";
            $query .= "   AND EXAMNO = '{$applicant["EXAMNO"]}'";
            $db->query($query);
        }

        Query::dbCheckIn($db);
    }

    /*================================== CSV処理 ==================================*/

    //存在チェック用
    public function selectEntexamApplicantBaseDat($applicant)
    {
        $query  = "";
        $query .= " SELECT  ";
        $query .= "     ENTEXAMYEAR ";
        $query .= "   , APPLICANTDIV ";
        $query .= "   , TESTDIV ";
        $query .= "   , EXAMNO ";
        $query .= " FROM ENTEXAM_APPLICANTBASE_DAT ";

        $query .= " WHERE ENTEXAMYEAR = '{$applicant["ENTEXAMYEAR"]}' ";
        $query .= "   AND APPLICANTDIV = '{$applicant["APPLICANTDIV"]}' ";
        $query .= "   AND EXAMNO = '{$applicant["EXAMNO"]}' ";

        return $query;
    }

    //サンプルデータの取得（見本）
    public function selectSampleQuery($model)
    {
        $query  = "";
        $query .= " WITH SAMPLE_DAT( ";
        $query .= "     ENTEXAMYEAR ";
        $query .= "   , APPLICANTDIV ";
        $query .= "   , TESTDIV ";
        $query .= "   , EXAMNO ";
        $query .= "   , NAME ";
        $query .= "   , NAME_KANA ";
        $query .= "   , SEX ";
        $query .= "   , BIRTHDAY ";
        $query .= "   , ZIPCD ";
        $query .= "   , ADDRESS1 ";
        $query .= "   , ADDRESS2 ";
        $query .= "   , TELNO ";
        $query .= "   , GNAME ";
        $query .= "   , GKANA ";
        $query .= "   , GZIPCD ";
        $query .= "   , GADDRESS1 ";
        $query .= "   , GADDRESS2 ";
        $query .= "   , GTELNO ";
        $query .= "   , GTELNO2 ";
        $query .= "   , FS_CD ";
        $query .= "   , FS_YM ";
        $query .= "   , FS_GRDDIV ";
        $query .= "   , RANK ";
        $query .= "   , SCHOOLCD1 ";
        $query .= "   , SCHOOLCD2 ";
        $query .= "   , SEND_ZIPCD ";
        $query .= "   , SEND_ADDRESS1 ";
        $query .= "   , SEND_ADDRESS2 ";
        $query .= "   , SEND_TELNO ";
        $query .= " ) AS ( ";
        $query .= "   VALUES ( ";
        $query .= "       '".(CTRL_YEAR + 1)."' ";
        $query .= "     , '{$model->field["APPLICANTDIV"]}' ";
        $query .= "     , '{$model->field["TESTDIV"]}' ";
        $query .= "     , '0000000001' ";

        $query .= "     , '志願者　太郎' ";
        $query .= "     , 'シガンシャ　タロウ' ";
        $query .= "     , '1' ";
        $query .= "     , '".CTRL_DATE."' ";

        $query .= "     , '662-0000' ";
        $query .= "     , '兵庫県　西宮市' ";
        $query .= "     , 'ＸＸＸＸ町１－１－１００　〇〇マンション' ";
        $query .= "     , '0000-00-0000' ";

        $query .= "     , '保護者　太郎' ";
        $query .= "     , 'ホゴシャ　タロウ' ";
        $query .= "     , '662-0000' ";
        $query .= "     , '兵庫県　西宮市' ";
        $query .= "     , 'ＸＸＸＸ町Ｘ－Ｘ－ＸＸＸ　ＸＸマンション' ";
        $query .= "     , '0000-00-0000' ";
        $query .= "     , '0000-00-0000' ";

        $query .= "     , '2800001' ";
        $query .= "     , '".(CTRL_YEAR + 1)."-03' ";
        $query .= "     , '1' ";

        $query .= "     , '1' ";
        $query .= "     , '2800001' ";
        $query .= "     , '2800001' ";

        $query .= "     , '662-0000' ";
        $query .= "     , '兵庫県　西宮市' ";
        $query .= "     , 'ＸＸＸＸ町Ｘ－Ｘ－ＸＸＸ　ＸＸマンション' ";
        $query .= "     , '0000-00-0000' ";

        $query .= "   ) ";
        $query .= " ) ";
        $query .= " SELECT * FROM SAMPLE_DAT ";

        return $query;
    }


    /*================================== エラーデータ処理 ==================================*/
    //エラーデータの削除
    public function deleteCsvMsgPrgDat()
    {
        $query  = "DELETE FROM W_CSVMSG_PRG_DAT ";
        $query .= "WHERE PROGRAMID = 'KNJL062I'";
        return $query;
    }

    //エラーＤＢへの追加
    public function insertCsvMsgPrgDat(&$db, $line, $check_error)
    {
        $data1 = array();
        $data1["PROGRAMID"][TEXT] = 'KNJL062I';
        $data1["MSGROW"][NUMBER]  = $line;
        $data1["MSGREMARK"][TEXT] = (strlen($check_error) > 119) ? substr($check_error, 0, 119)." " : $check_error." ";

        $query = Query::insertSQL($data1, "W_CSVMSG_PRG_DAT");
        $db->query($query);

        return;
    }

    //CVS作成用のQUERY
    public function selectCsvMsgPrgDat()
    {
        $query  = "SELECT MSGROW, MSGREMARK ";
        $query .= "FROM W_CSVMSG_PRG_DAT ";
        $query .= "WHERE PROGRAMID = 'KNJL062I' ";
        return $query;
    }
}
