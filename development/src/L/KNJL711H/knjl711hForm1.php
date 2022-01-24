<?php
class knjl711hForm1
{
    function main(&$model)
    {
        $objForm      = new form;
        $arg["start"] = $objForm->get_start("main", "POST", "knjl711hindex.php", "", "main");

        //一覧表示
        if ((!isset($model->warning)) && (!is_array($existdata))) {
            //データを取得
            $Row = knjl711hQuery::get_edit_data($model);
            if ($model->cmd == 'back' || $model->cmd == 'next' || $model->cmd == 'back1' || $model->cmd == 'next1') {
                if (!is_array($Row)) {
                    if ($model->cmd == 'back' || $model->cmd == 'next') {
                        $model->setWarning("MSG303","更新しましたが、移動先のデータが存在しません。");
                    }
                    $model->cmd = "main";
                    $Row = knjl711hQuery::get_edit_data($model);
                }
                $model->examNo = $Row["EXAMNO"];
            }
            $disabled = "";
            if (!is_array($Row)) {
                $disabled = "disabled";
                if ($model->cmd == 'reference') {
                    $model->setWarning("MSG303");
                }
            }
        } else {
            $Row =& $model->field;
        }

        $arg["TOP"]["YEAR"] = $model->year;


        if (isset($Row["EXAMNO"])) {
            $model->checkexam = $Row["EXAMNO"];
        }

        //DB接続
        $db = Query::dbCheckOut();

        //学校種別コンボ
        $query = knjl711hQuery::get_name_cd($model->year, "L003");
        $extra = "onChange=\"change_flg(); return btn_submit('changeApp');\"";
        makeCmb($objForm, $arg, $db, $query, $model->applicantDiv, "APPLICANTDIV", $extra, 1, "");
        if ($model->applicantDiv == "1") {
            $arg["APPLICANTDIV_J"] = "1";            
        } else {
            $arg["APPLICANTDIV_H"] = "1";
        }

        //入試区分
        $model->testDiv = ($model->cmd == "changeApp") ? "" : $model->testDiv;
        $query = knjl711hQuery::getTestDiv($model->year, $model->applicantDiv);
        $extra = "onChange=\"change_flg(); return btn_submit('changeTestDiv');\"";
        makeCmb($objForm, $arg, $db, $query, $model->testDiv, "TESTDIV", $extra, 1, "");

        //入試種別
        $model->kindDiv = ($model->cmd == "changeTestDiv") ? "" : $model->kindDiv;
        $query = knjl711hQuery::getKindDiv($model->year, $model->applicantDiv);
        $extra = "onChange=\"change_flg();\"";
        makeCmb($objForm, $arg, $db, $query, $model->kindDiv, "KINDDIV", $extra, 1, "");

        //受験番号
        $extra = "onChange=\"btn_disabled();\" onblur=\"this.value=toAlphaNumber(this.value);\" onkeydown=\"keyChangeEntToTab(this)\"";
        $arg["data"]["EXAMNO"] = knjCreateTextBox($objForm, $model->examNo, "EXAMNO", 6, 4, $extra);

        //入試区分
        $arg["data"]["TESTDIV_ABBV"] = $Row["TESTDIV_ABBV"];        

        //------------------------------志願者情報-------------------------------------

        //氏名(志願者)
        $extra = "onChange=\"change_flg();\" id=\"NAME\" onkeydown=\"keyChangeEntToTab(this)\"";
        $arg["data"]["NAME"] = knjCreateTextBox($objForm, $Row["NAME"], "NAME", 41, 40, $extra);

        //氏名カナ(志願者)
        $extra = "onChange=\"change_flg();\" id=\"NAME_KANA\" onkeydown=\"keyChangeEntToTab(this)\"";
        $arg["data"]["NAME_KANA"] = knjCreateTextBox($objForm, $Row["NAME_KANA"], "NAME_KANA", 41, 80, $extra);

        //性別
        $query = knjl711hQuery::get_name_cd($model->year, "Z002");
        $extra = "onChange=\"change_flg();\"";
        makeCmb($objForm, $arg, $db, $query, $Row["SEX"], "SEX", $extra, 1, "BLANK");

        //生年月日（西暦）
        $extra = " onchange=\"change_flg()\"";
        $arg["data"]["BIRTHDAY"] = View::popUpCalendar2($objForm, "BIRTHDAY", str_replace("-", "/", $Row["BIRTHDAY"]), "", "", $extra);

        global $sess;
        //郵便番号入力支援(志願者)
        $extra = "onblur=\"isZipcd(this)\" onChange=\"change_flg();\" onkeydown=\"keyChangeEntToTab(this)\"";
        $arg["data"]["ZIPCD"] = knjCreateTextBox($objForm, $Row["ZIPCD"], "ZIPCD", 10, "", $extra);

        //読込ボタンを作成する
        $extra = "style=\"width:140px\"onclick=\"loadwindow('" .REQUESTROOT ."/common/search_zipcd.php?cmd=search&addrname=ADDRESS1&zipname=ZIPCD&ZIP_SESSID={$sess->id}&frame='+getFrameName(self), event.clientX + function () {var scrollX = document.documentElement.scrollLeft || document.body.scrollLeft;return scrollX;}(), event.clientY + function () {var scrollY = document.documentElement.scrollTop || document.body.scrollTop;return scrollY;}(), 320, 260)\"";
        $arg["data"]["ZIPCD"] .= knjCreateBtn($objForm, "btn_zip", "郵便番号入力支援", $extra);

        //確定ボタンを作成する
        $extra = "onclick=\"hiddenWin('" .REQUESTROOT ."/common/search_zipcd.php?cmd=apply&addrname=ADDRESS1&zipname=ZIPCD&zip='+document.forms[0]['ZIPCD'].value+'&frame='+getFrameName(self))\"";
        $arg["data"]["ZIPCD"] .= knjCreateBtn($objForm, "btn_apply", "確定", $extra);

        //住所(志願者)
        $extra = " onChange=\"change_flg();\" onkeydown=\"keyChangeEntToTab(this)\"";
        $arg["data"]["ADDRESS1"] = knjCreateTextBox($objForm, $Row["ADDRESS1"], "ADDRESS1", 61, 60, $extra);

        //方書(志願者)
        $extra = "onChange=\"change_flg();\" onkeydown=\"keyChangeEntToTab(this)\"";
        $arg["data"]["ADDRESS2"] = knjCreateTextBox($objForm, $Row["ADDRESS2"], "ADDRESS2", 61, 60, $extra);

        //電話番号(志願者)
        $extra = "onblur=\"this.value=toTelNo(this.value)\" onChange=\"change_flg();\" onkeydown=\"keyChangeEntToTab(this)\"";
        $arg["data"]["TELNO"] = knjCreateTextBox($objForm, $Row["TELNO"], "TELNO", 14, 14, $extra);

        //出身学校コード
        $extra = "onblur=\"this.value=toInteger(this.value)\" onChange=\"change_flg();\" onkeydown=\"keyChangeEntToTab(this)\" id=\"FINSCHOOLCD_ID\" ";
        $arg["data"]["FINSCHOOLCD"] = knjCreateTextBox($objForm, $Row["FS_CD"], "FINSCHOOLCD", 8, 8, $extra);

        //学校名
        $query = knjl711hQuery::getFinschoolName($Row["FS_CD"]);
        $fsArray = $db->getRow($query, DB_FETCHMODE_ASSOC);
        $arg["data"]["FINSCHOOLNAME"] = $fsArray["FINSCHOOL_DISTCD_NAME"].$fsArray["FINSCHOOL_NAME"];

        //かな検索ボタン（出身学校）
        $extra = "style=\"width:70px\" onclick=\"loadwindow('" .REQUESTROOT ."/X/KNJXSEARCH_FINSCHOOL/knjwfin_searchindex.php?cmd=searchMain&fscdname=FINSCHOOLCD_ID&fsname=FINSCHOOLNAME_ID&fsaddr=&school_div=', event.clientX + function () {var scrollX = document.documentElement.scrollLeft || document.body.scrollLeft;return scrollX;}(), event.clientY + function () {var scrollY = document.documentElement.scrollTop || document.body.scrollTop;return scrollY;}(), 500, 380)\"";
        $arg["button"]["btn_fin_kana_reference"] = knjCreateBtn($objForm, "btn_fin_kana_reference", "検 索", $extra);

        //------------------------------保護者情報-------------------------------------
        //氏名(保護者)
        $extra = "onChange=\"change_flg()\" id=\"GNAME\" onkeydown=\"keyChangeEntToTab(this)\"";
        $arg["data"]["GNAME"] = knjCreateTextBox($objForm, $Row["GNAME"], "GNAME", 41, 40, $extra);

        //氏名カナ(保護者)
        $extra = "onChange=\"change_flg();\" id=\"GKANA\" onkeydown=\"keyChangeEntToTab(this)\"";
        $arg["data"]["GKANA"] = knjCreateTextBox($objForm, $Row["GKANA"], "GKANA", 41, 80, $extra);

        //電話番号(保護者)
        $extra = "onblur=\"this.value=toTelNo(this.value)\" onChange=\"change_flg();\" onkeydown=\"keyChangeEntToTab(this)\"";
        $arg["data"]["GTELNO"] = knjCreateTextBox($objForm, $Row["GTELNO"], "GTELNO", 14, 14, $extra);

        //------------------------------資格-------------------------------------

        //資格1
        $query = knjl711hQuery::getQualifiedMst($model->year, $model->applicantDiv, "01");
        $extra = " onChange=\"change_flg();\" onkeydown=\"keyChangeEntToTab(this)\"";
        makeCmb($objForm, $arg, $db, $query, $Row["QUALIFIED_ENG_CD"], "QUALIFIED_ENG_CD", $extra, 1, "blank");

        //資格2
        $query = knjl711hQuery::getQualifiedMst($model->year, $model->applicantDiv, "02");
        $extra = " onChange=\"change_flg();\" onkeydown=\"keyChangeEntToTab(this)\"";
        makeCmb($objForm, $arg, $db, $query, $Row["QUALIFIED_KANJI_CD"], "QUALIFIED_KANJI_CD", $extra, 1, "blank");

        //資格3
        $query = knjl711hQuery::getQualifiedMst($model->year, $model->applicantDiv, "03");
        $extra = " onChange=\"change_flg();\" onkeydown=\"keyChangeEntToTab(this)\"";
        makeCmb($objForm, $arg, $db, $query, $Row["QUALIFIED_MATH_CD"], "QUALIFIED_MATH_CD", $extra, 1, "blank");

        //資格4
        $query = knjl711hQuery::getQualifiedMst($model->year, $model->applicantDiv, "04");
        $extra = " onChange=\"change_flg();\" onkeydown=\"keyChangeEntToTab(this)\"";
        makeCmb($objForm, $arg, $db, $query, $Row["QUALIFIED_READING_CD"], "QUALIFIED_READING_CD", $extra, 1, "blank");

        //運動
        $query = knjl711hQuery::getQualifiedMst($model->year, $model->applicantDiv, "05");
        $extra = " onChange=\"change_flg();\" onkeydown=\"keyChangeEntToTab(this)\" style=\"width:200px;\" ";
        makeCmb($objForm, $arg, $db, $query, $Row["ATHLETIC_PRIZE_CD"], "ATHLETIC_PRIZE_CD", $extra, 1, "blank");

        //文化
        $query = knjl711hQuery::getQualifiedMst($model->year, $model->applicantDiv, "06");
        $extra = " onChange=\"change_flg();\" onkeydown=\"keyChangeEntToTab(this)\" style=\"width:200px;\" ";
        makeCmb($objForm, $arg, $db, $query, $Row["CULTURE_PRIZE_CD"], "CULTURE_PRIZE_CD", $extra, 1, "blank");
       
        //生徒会
        $query = knjl711hQuery::getQualifiedMst($model->year, $model->applicantDiv, "07");
        $extra = " onChange=\"change_flg();\" onkeydown=\"keyChangeEntToTab(this)\" style=\"width:200px;\" ";
        makeCmb($objForm, $arg, $db, $query, $Row["SCH_COUNCIL_PRIZE_CD"], "SCH_COUNCIL_PRIZE_CD", $extra, 1, "blank");

        //加点
        $extra = "onblur=\"this.value=toInteger(this.value)\" onChange=\"change_flg();\" onkeydown=\"keyChangeEntToTab(this)\"";
        $arg["data"]["PLUS_POINT_TOTAL"] = knjCreateTextBox($objForm, $Row["PLUS_POINT_TOTAL"], "PLUS_POINT_TOTAL", 3, 3, $extra);

        //-------------------------------- その他------------------------------------

        //備考
        $extra = " onChange=\"change_flg();\" onkeydown=\"keyChangeEntToTab(this)\"";
        $arg["data"]["BIKOU_REMARK"] = knjCreateTextBox($objForm, $Row["BIKOU_REMARK"], "BIKOU_REMARK", 81, 80, $extra);

        //重複出願フラグ(同時)
        $checked = ($Row["CONCURRENT_APP_FLG"] == "1") ? " checked " : "";
        $extra = " onChange=\"change_flg();\" onkeydown=\"keyChangeEntToTab(this)\"".$checked;
        $arg["data"]["CONCURRENT_APP_FLG"] = knjCreateCheckBox($objForm, "CONCURRENT_APP_FLG", "1", $extra, "");

        //重複出願フラグ(複数)
        $checked = ($Row["MULTI_APP_FLG"] == "1") ? " checked " : "";
        $extra = " onChange=\"change_flg();\" onkeydown=\"keyChangeEntToTab(this)\"".$checked;
        $arg["data"]["MULTI_APP_FLG"] = knjCreateCheckBox($objForm, "MULTI_APP_FLG", "1", $extra, "");

        //年齢超過フラグ
        $checked = ($Row["OVER_AGE_FLG"] == "1") ? " checked " : "";
        $extra = " onChange=\"change_flg();\" onkeydown=\"keyChangeEntToTab(this)\"".$checked;
        $arg["data"]["OVER_AGE_FLG"] = knjCreateCheckBox($objForm, "OVER_AGE_FLG", "1", $extra, "");

        //-------------------------------- ボタン作成 ------------------------------------
        $zip = $Row["ZIPCD"];
        $zadd = $Row["ADDRESS1"];

        //検索ボタン
        $extra = "onclick=\"return btn_submit('reference', '".$zip."', '".$zadd."');\"";
        $arg["button"]["btn_reference"] = knjCreateBtn($objForm, "btn_reference", "検 索", $extra);

        //かな検索ボタン
        $extra = "style=\"width:80px\" onclick=\"loadwindow('" .REQUESTROOT ."/L/KNJL711H/search_name.php?cmd=search&year='+document.forms[0]['year'].value+'&applicantdiv='+document.forms[0]['APPLICANTDIV'].value+'&testdiv={$model->testDiv}&examno='+document.forms[0]['EXAMNO'].value+'&NAME_SESSID={$sess->id}&frame='+getFrameName(self), event.clientX + function () {var scrollX = document.documentElement.scrollLeft || document.body.scrollLeft;return scrollX;}(), event.clientY + function () {var scrollY = document.documentElement.scrollTop || document.body.scrollTop;return scrollY;}(), 320, 260)\"";
        $arg["button"]["btn_kana_reference"] = knjCreateBtn($objForm, "btn_kana_reference", "かな検索", $extra);

        //前の志願者検索ボタン
        $extra = "style=\"width:32px; padding-left:0px; padding-right:0px;\" onClick=\"btn_submit('back1', '".$zip."', '".$zadd."');\"";
        $arg["button"]["btn_back_next"] = knjCreateBtn($objForm, "btn_back", " << ", $extra);

        //次の志願者検索ボタン
        $extra = "style=\"width:32px; padding-left:0px; padding-right:0px;\" onClick=\"btn_submit('next1', '".$zip."', '".$zadd."');\"";
        $arg["button"]["btn_back_next"] .= knjCreateBtn($objForm, "btn_next", " >> ", $extra);

        //画面クリアボタン
        $extra = "style=\"width:90px\" onclick=\"return btn_submit('disp_clear');\"";
        $arg["button"]["btn_clear"] = knjCreateBtn($objForm, "btn_clear", "画面クリア", $extra);

        //更新ボタン
        $extra = "$disabled onclick=\"return doSubmit();\"";
        $arg["button"]["btn_udpate"] = knjCreateBtn($objForm, "btn_udpate", "更 新", $extra);

        //更新ボタン(更新後前の志願者)
        $extra = "$disabled style=\"width:150px\" onclick=\"return btn_submit('back');\"";
        $arg["button"]["btn_up_next"] = knjCreateBtn($objForm, "btn_up_pre", "更新後前の志願者", $extra);

        //更新ボタン(更新後次の志願者)
        $extra = "$disabled style=\"width:150px\" onclick=\"return btn_submit('next');\"";
        $arg["button"]["btn_up_next"] .= knjCreateBtn($objForm, "btn_up_next", "更新後次の志願者", $extra);

        //削除ボタン
        $extra = "$disabled onclick=\"return btn_submit('delete');\"";
        $arg["button"]["btn_del"] = knjCreateBtn($objForm, "btn_del", "削 除", $extra);

        //取消ボタン
        $extra = "onclick=\"return btn_submit('reset');\"";
        $arg["button"]["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);

        //終了ボタン
        $extra = "onclick=\"closeWin();\"";
        $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);

        //hidden
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "year", $model->year);
        knjCreateHidden($objForm, "CTRL_YEAR", CTRL_YEAR);
        knjCreateHidden($objForm, "CTRL_SEMESTER", CTRL_SEMESTER);

        //インラインフレーム用Javascriptタグ生成
        $arg["IFRAME"] = View::setIframeJs();

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();
        View::toHTML($model, "knjl711hForm1.html", $arg);
    }
}
//makeCmb
function makeCmb(&$objForm, &$arg, $db, $query, &$value, $name, $extra, $size, $blank = "") {
    $opt = array();
    if ($blank == "blank") {
        $opt[] = array("label" => "", "value" => "");
    }
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);
        if ($name == 'APPLICANTDIV') {
            if ($value == "" && $row["NAMESPARE2"] == '1') $value = $row["VALUE"];
        }
        if ($value == $row["VALUE"]) $value_flg = true;
    }

    $value = ($value && $value_flg) ? $value : $opt[0]["value"];
    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);

    $result->free();
}


?>