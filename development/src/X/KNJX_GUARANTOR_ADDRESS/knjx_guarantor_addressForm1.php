<?php

require_once('for_php7.php');

class knjx_guarantor_addressForm1
{
    public function main(&$model)
    {
        //フォーム作成
        $objForm = new form();
        $arg["start"]   = $objForm->get_start("edit", "POST", "knjx_guarantor_addressindex.php", "", "edit");
        $arg["reload"] = "";
        $db = Query::dbCheckOut();

        //起動チェック
        if (!knjx_guarantor_addressQuery::checkToStart($db)) {
            $link = REQUESTROOT."/A/KNJA110A/knja110aindex.php?cmd=edit&schregno=".$model->schregno;
            $arg["close"] = "closing_window('$link');";
        }

        if ($model->cmd == "list2") {
            $link = REQUESTROOT."/X/KNJX_GUARANTOR_ADDRESS/knjx_guarantor_addressindex.php?cmd=edit2&schregno=".$model->schregno."&INFO_DIV=".$model->infoDiv;
            $arg["close"] = "openEdit('$link');";
        }

        //学籍基礎マスタより学籍番号と名前を取得
        $Row         = $db->getRow(knjx_guarantor_addressQuery::getSchregNoName($model), DB_FETCHMODE_ASSOC);
        $arg["NO"]   = $Row["SCHREGNO"];
        $arg["NAME"] = $Row["NAME"];

        //学籍住所データよりデータを取得
        if ($model->schregno) {
            $row2 = $db->getRow(knjx_guarantor_addressQuery::getEmergencyInfo($model->schregno), DB_FETCHMODE_ASSOC);
            $result = $db->query(knjx_guarantor_addressQuery::getAddressAll($model));

            while ($row1 = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $row  = array_merge((array)$row1, (array)$row2);

                $name1 = $db->getOne(knjx_guarantor_addressQuery::listAreaCd($row["AREACD"]));
                $row["AREA_CD"]          = $row["AREACD"].":".$name1;
                $row["ISSUEDATE"]        = str_replace("-", "/", $row["ISSUEDATE"]);
                $row["EXPIREDATE"]       = str_replace("-", "/", $row["EXPIREDATE"]);
                $row["GUARANTOR_ISSUEDATE"]  = str_replace("-", "/", $row["GUARANTOR_ISSUEDATE"]);
                $row["GUARANTOR_EXPIREDATE"] = str_replace("-", "/", $row["GUARANTOR_EXPIREDATE"]);
                $row["GUARANTOR_ADDR_FLG"]   = $row["GUARANTOR_ADDR_FLG"] == "1" ? "可" : "";
                $row["ADDR_FLG"]         = $row["ADDR_FLG"] == "1" ? "可" : "";
                $arg["rereki_data"][] = $row;
            }
        }

        //生徒情報の取得        (SCHREG_ADDRESS_DAT)
        $row1 = $db->getRow(knjx_guarantor_addressQuery::getRowAddress($model->schregno, $model->issuedate), DB_FETCHMODE_ASSOC);
        //保護者情報の取得      (GUARDIAN_DAT、GUARANTOR_ADDRESS_DAT)
        $row2 = $db->getRow(knjx_guarantor_addressQuery::getGuarantorAddr($model->schregno, $model->guarantor_issuedate), DB_FETCHMODE_ASSOC);
        //緊急連絡先情報の取得  (SCHREG_BASE_MST)
        $row3 = $db->getRow(knjx_guarantor_addressQuery::getEmergencyInfo($model->schregno), DB_FETCHMODE_ASSOC);

        $model->form2 = array_merge((array)$row1, (array)$row2, (array)$row3);

        if (!$model->isWarning()) {
            $row  = array_merge((array)$row1, (array)$row2, (array)$row3);
        } else {
            $row =& $model->field;
        }

        knjCreateHidden($objForm, "CHECK_GUARANTOR_ISSUEDATE", $model->form2["GUARANTOR_ISSUEDATE"]);
        knjCreateHidden($objForm, "CHECK_GUARANTOR_EXPIREDATE", $model->form2["GUARANTOR_EXPIREDATE"]);

        if (!$model->issuedate) {
            $model->issuedate = str_replace("-", "/", $row["ISSUEDATE"]);
        }

        if (!$model->guarantor_issuedate) {
            $model->guarantor_issuedate = str_replace("-", "/", $row["GUARANTOR_ISSUEDATE"]);
        }

        /********************************************************************************/
        /********************************************************************************/
        /*******        *****************************************************************/
        /******* 保護者 *****************************************************************/
        /*******        *****************************************************************/
        /********************************************************************************/
        /********************************************************************************/
        //有効期間開始日付
        $arg["data"]["GUARANTOR_ISSUEDATE"] = str_replace("input ", "input ", View::popUpCalendar($objForm, "GUARANTOR_ISSUEDATE", str_replace("-", "/", $row["GUARANTOR_ISSUEDATE"]), ""));
        //有効期間開始日付
        $arg["data"]["GUARANTOR_EXPIREDATE"] = str_replace("input ", "input ", View::popUpCalendar($objForm, "GUARANTOR_EXPIREDATE", str_replace("-", "/", $row["GUARANTOR_EXPIREDATE"]), ""));
        //郵便番号
        $arg["data"]["GUARANTOR_ZIPCD"] = str_replace("input ", "input {$guarantor_disabled} ", View::popUpZipCode($objForm, "GUARANTOR_ZIPCD", $row["GUARANTOR_ZIPCD"], "GUARANTOR_ADDR1"));

        /********************/
        /* テキストボックス */
        /********************/
        //保護者氏名
        $arg["data"]["GUARANTOR_NAME"] = $row["GUARANTOR_NAME"];
        //保護者かな
        $arg["data"]["GUARANTOR_KANA"] = $row["GUARANTOR_KANA"];
        //保護者氏名
        $arg["data"]["GUARANTOR_REAL_NAME"] = $row["GUARANTOR_REAL_NAME"];
        //保護者かな
        $arg["data"]["GUARANTOR_REAL_KANA"] = $row["GUARANTOR_REAL_KANA"];

        //住所
        $arg["data"]["GUARANTOR_ADDR1"] = knjCreateTextBox($objForm, $row["GUARANTOR_ADDR1"], "GUARANTOR_ADDR1", 50, 90, $extra);
        //方書き(アパート名等)
        $arg["data"]["GUARANTOR_ADDR2"] = knjCreateTextBox($objForm, $row["GUARANTOR_ADDR2"], "GUARANTOR_ADDR2", 50, 90, $extra);
        //方書きを住所1とする
        $extra = $row["GUARANTOR_ADDR_FLG"] == "1" ? " checked " : "";
        $arg["data"]["GUARANTOR_ADDR_FLG"] = knjCreateCheckBox($objForm, "GUARANTOR_ADDR_FLG", "1", $extra);
        //電話番号
        $arg["data"]["GUARANTOR_TELNO"] = knjCreateTextBox($objForm, $row["GUARANTOR_TELNO"], "GUARANTOR_TELNO", 16, 14, $extra);

        //保証人等の勤務先タイトル名称設定
        $arg["data"]["PUBLIC_OFFICE_TITLE"] = $model->publicOffice_TitleName;
        $arg["data"]["PUBLIC_OFFICE"]  = $row["PUBLIC_OFFICE"];

        /******************/
        /* コンボボックス */
        /******************/
        //名称マスタよりコンボボックスのデータを取得
        $opt = array();
        $opt["Z002"][] = array("label" => "","value" => "0");
        $opt["H201"][] = array("label" => "","value" => "00");
        $opt["H202"][] = array("label" => "","value" => "00");
        $query = knjx_guarantor_addressQuery::getNameMsts();
        $result = $db->query($query);
        while ($row3 = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[$row3["NAMECD1"]][$row3["NAMECD2"]] = array("label" => $row3["NAMECD2"]."&nbsp;".$row3["NAME1"],
                                                             "value" => $row3["NAMECD2"]);
        }
        $arg["data"]["GUARANTOR_SEX"] = $opt["Z002"][$row["GUARANTOR_SEX"]]["label"];
        $arg["data"]["RELATIONSHIP"] = $opt["H201"][$row["RELATIONSHIP"]]["label"];
        $arg["data"]["GUARANTOR_JOBCD"] = $opt["H202"][$row["GUARANTOR_JOBCD"]]["label"];

        /**********/
        /* ボタン */
        /**********/
        //追加ボタン
        $extra = "onclick=\"return btn_submit('add2');\"";
        $arg["button"]["btn_add2"]     = knjCreateBtn($objForm, "btn_add2", "追加", $extra);
        //更新ボタン
        $extra = "onclick=\"return btn_submit('update2');\"";
        $arg["button"]["btn_update2"]  = knjCreateBtn($objForm, "btn_update2", "更新", $extra);

        //削除ボタン
        $extra = "onclick=\"return btn_submit('delete2');\"";
        $arg["button"]["btn_del2"]     = knjCreateBtn($objForm, "btn_del2", "削除", $extra);
        //取消ボタン
        $extra = "onclick=\"return btn_submit('clear');\"";
        $arg["button"]["btn_reset2"]   = knjCreateBtn($objForm, "btn_reset2", "取消", $extra);
        //戻るボタン
        $link = REQUESTROOT."/H/KNJH020A/knjh020aindex.php?cmd=edit&SCHREGNO=".$model->schregno."&SEND_PRGID=KNJX_GUARANTOR_ADDRESS&SEND_AUTH=".$model->auth;
        $extra = "onclick=\"modoru('$link');\"";
        $arg["button"]["btn_end2"] = knjCreateBtn($objForm, "btn_end2", "戻る", $extra);

        $result->free();

        /**********/
        /* hidden */
        /**********/
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "UPDATED", $row["UPDATED"]);
        knjCreateHidden($objForm, "GUARANTOR_UPDATED", $row["GUARANTOR_UPDATED"]);
        knjCreateHidden($objForm, "REQUESTROOT", REQUESTROOT);

        knjCreateHidden($objForm, "CLICK_GUARANTOR_ISSUEDATE");

        Query::dbCheckIn($db);
        $arg["finish"] = $objForm->get_finish();

        if (VARS::get("cmd") != "edit") {
            $arg["reload"]  = "parent.top_frame.location.href='knjx_guarantor_addressindex.php?cmd=list';";
        }

        View::toHTML($model, "knjx_guarantor_addressForm1.html", $arg);
    }
}
