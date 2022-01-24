<?php

require_once('for_php7.php');

class knjl110dForm1 {
    function main(&$model) {
        $objForm      = new form;

        //一覧表示
        if ((!isset($model->warning)) && (!is_array($existdata))) {
            //ゼロ埋め
            if ($model->visitNo) {
                $model->visitNo = sprintf("%03d", $model->visitNo);
            }
            //データを取得
            $Row = knjl110dQuery::get_edit_data($model);
            if ($model->cmd == 'back1' || $model->cmd == 'next1') {
                if (!is_array($Row)) {
                    $model->cmd = "main";
                    $Row = knjl110dQuery::get_edit_data($model);
                }
                $model->visitNo = $Row["VISIT_NO"];
            }
            if (!is_array($Row)) {
                if ($model->cmd == 'reference') {
                    $model->setWarning("MSG303");
                }
            }
        } else {
            $Row =& $model->field;
        }

        $arg["TOP"]["YEAR"] = $model->year;

        if (isset($Row["VISIT_NO"])) {
            $model->checkVisit = $Row["VISIT_NO"];
        }

        //DB接続
        $db = Query::dbCheckOut();

        //エンター押下時の移動対象一覧
        $setTextField = array();
        $setTextField[] = "NAME";
        $setTextField[] = "NAME_KANA";
        $setTextField[] = "SEX";
        $setTextField[] = "ERACD";
        $setTextField[] = "BIRTH_Y";
        $setTextField[] = "BIRTH_M";
        $setTextField[] = "BIRTH_D";
        $setTextField[] = "FINSCHOOLCD";
        $setTextField[] = "FS_ERACD";
        $setTextField[] = "FS_Y";
        $setTextField[] = "FS_M";
        $setTextField[] = "ZIPCD";
        $setTextField[] = "ADDRESS1";
        $setTextField[] = "ADDRESS2";
        $setTextField[] = "TELNO";
        knjCreateHidden($objForm, "setTextField", implode(',', $setTextField));

        //相談日
        $Row["VISIT_DATE"] = str_replace("-", "/" ,($Row["VISIT_DATE"]) ? $Row["VISIT_DATE"]: CTRL_DATE);
        $arg["data"]["VISIT_DATE"] = View::popUpCalendarAlp($objForm, "VISIT_DATE", $Row["VISIT_DATE"], $disabledE, "");

        //事前相談番号
        $extra = " STYLE=\"ime-mode: inactive\" onblur=\"this.value=toAlphaNumber(this.value);\"";
        $arg["data"]["VISIT_NO"] = knjCreateTextBox($objForm, $model->visitNo, "VISIT_NO", 3, 3, $extra);

        //------------------------------志願者情報-------------------------------------
        //氏名(志願者)
        $extra = "STYLE=\"ime-mode: active\" onChange=\"change_flg();\" id=\"NAME\" onkeyup=\"keySet('NAME', 'NAME_KANA', 'H');\" onkeydown=\"changeEnterToTab(this)\"";
        $arg["data"]["NAME"] = knjCreateTextBox($objForm, $Row["NAME"], "NAME", 40, 60, $extra);

        //氏名かな(志願者)
        $extra = "STYLE=\"ime-mode: active\" onChange=\"change_flg();\" id=\"NAME_KANA\" onkeydown=\"changeEnterToTab(this)\"";
        $arg["data"]["NAME_KANA"] = knjCreateTextBox($objForm, $Row["NAME_KANA"], "NAME_KANA", 40, 120, $extra);

        //性別(志願者)
        $query = knjl110dQuery::get_name_cd($model->year, "Z002");
        $extra = "onChange=\"change_flg();\" onkeydown=\"changeEnterToTab(this)\"";
        makeCmb($objForm, $arg, $db, $query, $Row["SEX"], "SEX", $extra, 1, "");

        //元号
        $query = knjl110dQuery::get_name_cd($model->year, "L007");
        $extra = "onChange=\"change_flg();\" onkeydown=\"changeEnterToTab(this)\"";
        $Row["ERACD"] = ($Row["ERACD"]) ? $Row["ERACD"]: "4";//デフォルト4:平成
        makeCmb($objForm, $arg, $db, $query, $Row["ERACD"], "ERACD", $extra, 1, "");
        //年
        $extra = "STYLE=\"ime-mode: inactive\" onchange=\"change_flg()\" style=\"text-align:right\" onblur=\"this.value=toInteger(this.value);\" onkeydown=\"changeEnterToTab(this)\"";
        $arg["data"]["BIRTH_Y"] = knjCreateTextBox($objForm, $Row["BIRTH_Y"], "BIRTH_Y", 2, 2, $extra);
        //月
        $extra = "STYLE=\"ime-mode: inactive\" onchange=\"change_flg()\" style=\"text-align:right\" onblur=\"this.value=toInteger(this.value);\" onkeydown=\"changeEnterToTab(this)\"";
        $arg["data"]["BIRTH_M"] = knjCreateTextBox($objForm, $Row["BIRTH_M"], "BIRTH_M", 2, 2, $extra);
        //日
        $extra = "STYLE=\"ime-mode: inactive\" onchange=\"change_flg()\" style=\"text-align:right\" onblur=\"this.value=toInteger(this.value);\" onkeydown=\"changeEnterToTab(this)\"";
        $arg["data"]["BIRTH_D"] = knjCreateTextBox($objForm, $Row["BIRTH_D"], "BIRTH_D", 2, 2, $extra);

        //出身学校
        //出身学校コード
        $extra = "STYLE=\"ime-mode: inactive\" onblur=\"this.value=toInteger(this.value)\" onChange=\"change_flg();\" onkeydown=\"changeEnterToTab(this)\" id=\"FINSCHOOLCD_ID\" ";
        $arg["data"]["FINSCHOOLCD"] = knjCreateTextBox($objForm, $Row["FS_CD"], "FINSCHOOLCD", 7, 7, $extra);
        //検索ボタン
        $extra = "style=\"width:70px\" onclick=\"loadwindow('" .REQUESTROOT ."/X/KNJXSEARCH_FINSCHOOL/knjwfin_searchindex.php?cmd=searchMain&setSchoolKind=3&fscdname=FINSCHOOLCD&fsname=FINSCHOOLNAME_ID&fsaddr=&school_div=', event.clientX + function () {var scrollX = document.documentElement.scrollLeft || document.body.scrollLeft;return scrollX;}(), event.clientY + function () {var scrollY = document.documentElement.scrollTop || document.body.scrollTop;return scrollY;}(), 500, 380)\"";
        $arg["button"]["btn_fin_kana_reference1"] = knjCreateBtn($objForm, "btn_fin_kana_reference1", "検 索", $extra);
        //学校名
        $query = knjl110dQuery::getFinschoolName($Row["FS_CD"]);
        $fsArray = $db->getRow($query, DB_FETCHMODE_ASSOC);
        $arg["data"]["FINSCHOOLNAME"] = $fsArray["FINSCHOOL_NAME"];
        //卒業元号
        $query = knjl110dQuery::get_name_cd($model->year, "L007");
        $extra = "onChange=\"change_flg();\" onkeydown=\"changeEnterToTab(this)\"";
        $Row["FS_ERACD"] = ($Row["FS_ERACD"]) ? $Row["FS_ERACD"]: "4";//デフォルト4:平成
        makeCmb($objForm, $arg, $db, $query, $Row["FS_ERACD"], "FS_ERACD", $extra, 1, "");
        //卒業年
        $extra = "STYLE=\"ime-mode: inactive\" onchange=\"change_flg()\" style=\"text-align:right\" onblur=\"this.value=toInteger(this.value);\" onkeydown=\"changeEnterToTab(this)\"";
        $arg["data"]["FS_Y"] = knjCreateTextBox($objForm, $Row["FS_Y"], "FS_Y", 2, 2, $extra);
        //卒業月
        $extra = "STYLE=\"ime-mode: inactive\" onchange=\"change_flg()\" style=\"text-align:right\" onblur=\"this.value=toInteger(this.value);\" onkeydown=\"changeEnterToTab(this)\"";
        $Row["FS_M"] = ($Row["FS_M"]) ? $Row["FS_M"]: "03";
        $arg["data"]["FS_M"] = knjCreateTextBox($objForm, $Row["FS_M"], "FS_M", 2, 2, $extra);

        global $sess;
        //郵便番号入力支援
        $extra = "STYLE=\"ime-mode: inactive\" onblur=\"isZipcd(this)\" onChange=\"change_flg();\" onkeydown=\"changeEnterToTab(this)\"";
        $arg["data"]["ZIPCD"] = knjCreateTextBox($objForm, $Row["ZIPCD"], "ZIPCD", 10, "", $extra);
        //読込ボタンを作成する
        $extra = "style=\"width:140px\"onclick=\"loadwindow('" .REQUESTROOT ."/common/search_zipcd.php?cmd=search&addrname=ADDRESS1&zipname=ZIPCD&ZIP_SESSID={$sess->id}&frame='+getFrameName(self), event.clientX + function () {var scrollX = document.documentElement.scrollLeft || document.body.scrollLeft;return scrollX;}(), event.clientY + function () {var scrollY = document.documentElement.scrollTop || document.body.scrollTop;return scrollY;}(), 320, 260)\"";
        $arg["data"]["ZIPCD"] .= knjCreateBtn($objForm, "btn_zip", "郵便番号入力支援", $extra);
        //確定ボタンを作成する
        $extra = "onclick=\"hiddenWin('" .REQUESTROOT ."/common/search_zipcd.php?cmd=apply&addrname=ADDRESS1&zipname=ZIPCD&zip='+document.forms[0]['ZIPCD'].value+'&frame='+getFrameName(self))\"";
        $arg["data"]["ZIPCD"] .= knjCreateBtn($objForm, "btn_apply", "確定", $extra);

        //住所
        $extra = "onChange=\"change_flg();\" onkeydown=\"changeEnterToTab(this)\"";
        $arg["data"]["ADDRESS1"] = knjCreateTextBox($objForm, $Row["ADDRESS1"], "ADDRESS1", 60, 150, $extra);

        //方書
        $extra = "onChange=\"change_flg();\" onkeydown=\"changeEnterToTab(this)\"";
        $arg["data"]["ADDRESS2"] = knjCreateTextBox($objForm, $Row["ADDRESS2"], "ADDRESS2", 60, 150, $extra);

        //電話番号
        $extra = "STYLE=\"ime-mode: inactive\" onblur=\"this.value=toTelNo(this.value)\" onChange=\"change_flg();\" onkeydown=\"changeEnterToTab(this)\"";
        $arg["data"]["TELNO"] = knjCreateTextBox($objForm, $Row["TELNO"], "TELNO", 14, 14, $extra);

        //名称マスタより和暦の元号を取得
        $result = $db->query(knjl110dQuery::get_calendarno($model->year));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            if ($calno == "") {
                $calno = $row["NAMECD2"];
                $spare = $row["NAMESPARE1"];
                $spare2 = $row["NAMESPARE2"];
                $spare3 = $row["NAMESPARE3"];
            } else {
                $calno.= "," . $row["NAMECD2"];
                $spare.= "," . $row["NAMESPARE1"];
                $spare2.= "," . $row["NAMESPARE2"];
                $spare3.= "," . $row["NAMESPARE3"];
            }
            $arg["data2"][] = array("eracd" => $row["NAMECD2"], "wname" => $row["NAME1"]);
        }

        //-------------------------------- ボタン作成 ------------------------------------
        //検索ボタン（受験番号）
        $extra = "onclick=\"return btn_submit('reference');\"";
        $arg["button"]["btn_reference"] = knjCreateBtn($objForm, "btn_reference", "検 索", $extra);

        //かな検索ボタン
        $extra = "style=\"width:80px\" onclick=\"loadwindow('" .REQUESTROOT ."/L/KNJL110D/search_name.php?cmd=search&year='+document.forms[0]['year'].value+'&visitNo='+document.forms[0]['VISIT_NO'].value+'&NAME_SESSID={$sess->id}&frame='+getFrameName(self), event.clientX + function () {var scrollX = document.documentElement.scrollLeft || document.body.scrollLeft;return scrollX;}(), event.clientY + function () {var scrollY = document.documentElement.scrollTop || document.body.scrollTop;return scrollY;}(), 320, 260)\"";
        $arg["button"]["btn_kana_reference"] = knjCreateBtn($objForm, "btn_kana_reference", "かな検索", $extra);

        //前の志願者検索ボタン
        $extra = "style=\"width:32px; padding-left:0px; padding-right:0px;\" onClick=\"btn_submit('back1');\"";
        $arg["button"]["btn_back_next"] = knjCreateBtn($objForm, "btn_back", " << ", $extra);

        //次の志願者検索ボタン
        $extra = "style=\"width:32px; padding-left:0px; padding-right:0px;\" onClick=\"btn_submit('next1');\"";
        $arg["button"]["btn_back_next"] .= knjCreateBtn($objForm, "btn_next", " >> ", $extra);

        //画面クリアボタン
        $extra = "style=\"width:90px\" onclick=\"return btn_submit('disp_clear');\"";
        $arg["button"]["btn_clear"] = knjCreateBtn($objForm, "btn_clear", "画面クリア", $extra);

        //追加ボタン
        $extra = " onclick=\"return btn_submit('add');\"";
        $arg["button"]["btn_add"] = knjCreateBtn($objForm, "btn_add", "追 加", $extra);
        //更新ボタン
        $extra = " onclick=\"return btn_submit('update');\"";
        $arg["button"]["btn_udpate"] = knjCreateBtn($objForm, "btn_udpate", "更 新", $extra);
        //削除ボタン
        $extra = " onclick=\"return btn_submit('delete');\"";
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

        $arg["start"] = $objForm->get_start("main", "POST", "knjl110dindex.php", "", "main");
        $arg["finish"]  = $objForm->get_finish();
        View::toHTML($model, "knjl110dForm1.html", $arg);
    }
}
//makeCmb
function makeCmb(&$objForm, &$arg, $db, $query, &$value, $name, $extra, $size, $blank = "") {
    $opt = array();
    if ($blank == "BLANK") {
        $opt[] = array("label" => "", "value" => "");
    }
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);
        if ($value == $row["VALUE"]) $value_flg = true;
    }
    $value = ($value && $value_flg) ? $value : $opt[0]["value"];
    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);

    $result->free();
}
?>