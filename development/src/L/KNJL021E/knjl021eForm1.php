<?php

require_once('for_php7.php');

class knjl021eForm1 {

    function main(&$model) {

        $objForm = new form;

        $arg["start"] = $objForm->get_start("main", "POST", "knjl021eindex.php", "", "main");

        //DB接続
        $db = Query::dbCheckOut();

        //一覧表示
        if (!isset($model->warning)) {
            //データを取得
            $Row = $db->getRow(knjl021eQuery::getEditData($model), DB_FETCHMODE_ASSOC);
            if ($model->cmd == 'back' || $model->cmd == 'next' || $model->cmd == 'back1' || $model->cmd == 'next1') {
                if (!is_array($Row)) {
                    if ($model->cmd == 'back' || $model->cmd == 'next') {
                        $model->setWarning("MSG303",'更新しましたが、移動先のデータが存在しません。');
                    } else {
                        $model->setWarning("MSG303");
                    }
                    $model->cmd = "main";
                    $Row = $db->getRow(knjl021eQuery::getEditData($model), DB_FETCHMODE_ASSOC);
                }
                $model->examno  = $Row["EXAMNO"];
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

        if (isset($Row["EXAMNO"])) {
            $model->checkexam = $Row["EXAMNO"];
        }

        //入試年度
        $arg["TOP"]["YEAR"] = $model->year;

        //入試制度コンボ
        $query = knjl021eQuery::getNameMst($model->year, "L003");
        $extra = " onChange=\"return btn_submit('main')\"";
        makeCmb($objForm, $arg, $db, $query, $model->applicantdiv, "APPLICANTDIV", $extra, 1, "");

        //入試区分コンボ
        $query = knjl021eQuery::getNameMst($model->year, "L004");
        $extra = "onChange=\"change_flg(); return btn_submit('main')\"";
        makeCmb($objForm, $arg, $db, $query, $model->field["TESTDIV"], "TESTDIV", $extra, 1, "");

        //受験番号
        $extra = "onChange=\"btn_disabled();\" onblur=\"this.value=toAlphaNumber(this.value);\"";
        $arg["data"]["EXAMNO"] = knjCreateTextBox($objForm, $model->examno, "EXAMNO", 7, 7, $extra);

        //検索ボタン
        $extra = " onclick=\"return btn_submit('reference');\"";
        $arg["button"]["btn_reference"] = knjCreateBtn($objForm, "btn_reference", "検 索", $extra);

        global $sess;
        //カナ検索ボタン
        $extra = " style=\"width:80px\" onclick=\"loadwindow('" .REQUESTROOT ."/L/KNJL021E/search_name.php?cmd=search&year='+document.forms[0]['year'].value+'&applicantdiv='+document.forms[0]['applicantdiv'].value+'&testdiv='+document.forms[0]['TESTDIV'].value+'&examno='+document.forms[0]['EXAMNO'].value+'&NAME_SESSID={$sess->id}&frame='+getFrameName(self), event.clientX + function () {var scrollX = document.documentElement.scrollLeft || document.body.scrollLeft;return scrollX;}(), event.clientY + function () {var scrollY = document.documentElement.scrollTop || document.body.scrollTop;return scrollY;}(), 320, 260)\"";
        $arg["button"]["btn_kana_reference"] = knjCreateBtn($objForm, "btn_kana_reference", "カナ検索", $extra);

        //前の志願者検索ボタン
        $extra = " style=\"width:32px; padding-left:0px; padding-right:0px;\" onClick=\"btn_submit('back1');\"";
        $arg["button"]["btn_back_next"] = knjCreateBtn($objForm, "btn_back", " << ", $extra);

        //次の志願者検索ボタン
        $extra = " style=\"width:32px; padding-left:0px; padding-right:0px;\" onClick=\"btn_submit('next1');\"";
        $arg["button"]["btn_back_next"] .= knjCreateBtn($objForm, "btn_next", " >> ", $extra);

        //画面クリアボタン
        $extra = "style=\"width:90px\" onclick=\"return btn_submit('disp_clear');\"";
        $arg["button"]["btn_clear"] = knjCreateBtn($objForm, "btn_clear", "画面クリア", $extra);

        //------------------------------志願者情報-------------------------------------
        $arg["data"]["DESIREDIV"]       = $Row["DESIREDIV"];
        $arg["data"]["NAME"]            = $Row["NAME"];
        $arg["data"]["NAME_KANA"]       = $Row["NAME_KANA"];
        $arg["data"]["SHDIV"]           = $Row["SHDIV"];
        $arg["data"]["SEX"]             = $Row["SEX"];
        $arg["data"]["BIRTHDAY"]        = $Row["BIRTHDAY"];
        $arg["data"]["FS_CD"]           = $Row["FS_CD"];
        $arg["data"]["FINSCHOOLNAME"]   = $Row["FINSCHOOLNAME"];
        $arg["data"]["FS_GRDYEAR"]      = $Row["FS_GRDYEAR"];

        //------------------------------内申-------------------------------------
        //各項目の教科名称取得
        $result = $db->query(knjl021eQuery::getNameMst($model->year, "L008"));
        $kyouka_count = 0;
        $kyouka5 = array();
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $arg["data"]["NAME1_".$row["VALUE"]] = $row["NAME1"];
            $kyouka_count++;
            if ($row["NAMESPARE1"] == '1') $kyouka5[] = $row["VALUE"];
        }
        knjCreateHidden($objForm, "kyouka_count", $kyouka_count);
        knjCreateHidden($objForm, "kyouka5", implode(',', $kyouka5));

        //エンター押下時の移動対象一覧
        $setTextField = array();
        $array = array();
        $array["CONF1_RPT"] = "";
        $array["CONF2_RPT"] = "";
        $array["CONFIDENTIAL_RPT"] = "";
        for ($i = 1; $i <= $kyouka_count; $i++) {
            foreach ($array as $key => $val) {
                $setTextField[] = $key.sprintf("%02d", $i);
            }
        }
        $setTextField[] = "ABSENCE_DAYS";
        $setTextField[] = "ABSENCE_DAYS2";
        $setTextField[] = "ABSENCE_DAYS3";
        $setTextField[] = "DE003REMARK1";
        $setTextField[] = "REMARK1";
        knjCreateHidden($objForm, "setTextField", implode(',', $setTextField));

        //教科(1年)
        for ($i = 1; $i <= $kyouka_count; $i++) {
            $num = sprintf("%02d", $i);
            $extra = " onChange=\"change_flg();\" id=\"CONF1_RPT{$num}\" STYLE=\"text-align:right;\" onblur=\"this.value=toInteger(this.value); Culc();\" onKeyDown=\"keyChangeEntToTab(this);\"";
            $arg["data"]["CONF1_RPT".$num] = knjCreateTextBox($objForm, $Row["CONF1_RPT".$num], "CONF1_RPT".$num, 3, 1, $extra);
        }
        //教科(2年)
        for ($i = 1; $i <= $kyouka_count; $i++) {
            $num = sprintf("%02d", $i);
            $extra = " onChange=\"change_flg();\" id=\"CONF2_RPT{$num}\" STYLE=\"text-align:right;\" onblur=\"this.value=toInteger(this.value); Culc();\" onKeyDown=\"keyChangeEntToTab(this);\"";
            $arg["data"]["CONF2_RPT".$num] = knjCreateTextBox($objForm, $Row["CONF2_RPT".$num], "CONF2_RPT".$num, 3, 1, $extra);
        }
        //教科(3年)
        for ($i = 1; $i <= $kyouka_count; $i++) {
            $num = sprintf("%02d", $i);
            $extra = " onChange=\"change_flg();\" id=\"CONFIDENTIAL_RPT{$num}\" STYLE=\"text-align:right;\" onblur=\"this.value=toInteger(this.value); Culc();\" onKeyDown=\"keyChangeEntToTab(this);\"";
            $arg["data"]["CONFIDENTIAL_RPT".$num] = knjCreateTextBox($objForm, $Row["CONFIDENTIAL_RPT".$num], "CONFIDENTIAL_RPT".$num, 3, 1, $extra);
        }

        $kyouka5_count = get_count($kyouka5) > 0 ?get_count($kyouka5) : 5;

        //5科、全体平均(1年)
        $arg["data"]["CONF1_AVG5"]  = round($Row["CONF1_RPT10"] / $kyouka5_count, 1);
        $arg["data"]["CONF1_AVG9"]  = round($Row["CONF1_RPT11"] / $kyouka_count, 1);
        knjCreateHidden($objForm, "CONF1_RPT10", $Row["CONF1_RPT10"]);
        knjCreateHidden($objForm, "CONF1_RPT11", $Row["CONF1_RPT11"]);

        //5科、全体平均(2年)
        $arg["data"]["CONF2_AVG5"]  = round($Row["CONF2_RPT10"] / $kyouka5_count, 1);
        $arg["data"]["CONF2_AVG9"]  = round($Row["CONF2_RPT11"] / $kyouka_count, 1);
        knjCreateHidden($objForm, "CONF2_RPT10", $Row["CONF2_RPT10"]);
        knjCreateHidden($objForm, "CONF2_RPT11", $Row["CONF2_RPT11"]);

        //5科、全体平均(3年)
        $arg["data"]["CONF3_AVG5"]  = round($Row["TOTAL5"] / $kyouka5_count, 1);
        $arg["data"]["CONF3_AVG9"]  = round($Row["TOTAL_ALL"] / $kyouka_count, 1);
        knjCreateHidden($objForm, "TOTAL5", $Row["TOTAL5"]);
        knjCreateHidden($objForm, "TOTAL_ALL", $Row["TOTAL_ALL"]);

        //各教科の３年間の平均
        for ($i = 1; $i <= $kyouka_count; $i++) {
            $num = sprintf("%02d", $i);
            $arg["data"]["SUBAVG".$num] = round(($Row["CONF1_RPT".$num] + $Row["CONF2_RPT".$num] + $Row["CONFIDENTIAL_RPT".$num]) / 3, 1);
        }
        $arg["data"]["CONF_AVG5"] = round(($Row["CONF1_RPT10"] + $Row["CONF2_RPT10"] + $Row["TOTAL5"]) / (3 * $kyouka5_count), 1);
        $arg["data"]["CONF_AVG9"] = round(($Row["CONF1_RPT11"] + $Row["CONF2_RPT11"] + $Row["TOTAL_ALL"]) / (3 * $kyouka_count), 1);

        //欠席数(１年)
        $extra = " onChange=\"change_flg();\" id=\"ABSENCE_DAYS\" STYLE=\"text-align:right;\" onblur=\"this.value=toFloat(this.value); CulcAbs();\" onKeyDown=\"keyChangeEntToTab(this);\"";
        $arg["data"]["ABSENCE_DAYS"] = knjCreateTextBox($objForm, $Row["ABSENCE_DAYS"], "ABSENCE_DAYS", 3, 3, $extra);

        //欠席数(２年)
        $extra = " onChange=\"change_flg();\" id=\"ABSENCE_DAYS2\" STYLE=\"text-align:right;\" onblur=\"this.value=toFloat(this.value); CulcAbs();\" onKeyDown=\"keyChangeEntToTab(this);\"";
        $arg["data"]["ABSENCE_DAYS2"] = knjCreateTextBox($objForm, $Row["ABSENCE_DAYS2"], "ABSENCE_DAYS2", 3, 3, $extra);

        //欠席数(３年)
        $extra = " onChange=\"change_flg();\" id=\"ABSENCE_DAYS3\" STYLE=\"text-align:right;\" onblur=\"this.value=toFloat(this.value); CulcAbs();\" onKeyDown=\"keyChangeEntToTab(this);\"";
        $arg["data"]["ABSENCE_DAYS3"] = knjCreateTextBox($objForm, $Row["ABSENCE_DAYS3"], "ABSENCE_DAYS3", 3, 3, $extra);

        $arg["data"]["ABSENCE_DAYS_TOTAL"] = $Row["ABSENCE_DAYS"] + $Row["ABSENCE_DAYS2"] + $Row["ABSENCE_DAYS3"];

        //行動の記録
        $extra = " onChange=\"change_flg();\" style=\"text-align:right\" onblur=\"this.value=toInteger(this.value);\" onKeyDown=\"keyChangeEntToTab(this);\"";
        $arg["data"]["DE003REMARK1"] = knjCreateTextBox($objForm, $Row["DE003REMARK1"], "DE003REMARK1", 3, 3, $extra);

        //備考
        $extra = " onChange=\"change_flg();\" onKeyDown=\"keyChangeEntToTab(this);\"";
        $arg["data"]["REMARK1"] = knjCreateTextArea($objForm, "REMARK1", "2", "41", "soft", $extra, $Row["REMARK1"]);

        //checkbox
        $extra = " id=\"REMARK2\" ";
        $extra .= $Row["REMARK2"] == "1" ? " checked " : "";
        $arg["data"]["REMARK2"] = knjCreateCheckBox($objForm, "REMARK2", "1", $extra);

        /****************/
        /*  ボタン作成  */
        /****************/
        //更新ボタン
        $extra = "$disabled onclick=\"return btn_submit('update');\"";
        $arg["button"]["btn_udpate"] = knjCreateBtn($objForm, "btn_udpate", "更 新", $extra);
        //更新ボタン(更新後前の志願者)
        $extra = "$disabled style=\"width:150px\" onclick=\"return btn_submit('back');\"";
        $arg["button"]["btn_up_next"] = knjCreateBtn($objForm, "btn_up_pre", "更新後前の志願者", $extra);
        //更新ボタン(更新後次の志願者)
        $extra = "$disabled style=\"width:150px\" onclick=\"return btn_submit('next');\"";
        $arg["button"]["btn_up_next"] .= knjCreateBtn($objForm, "btn_up_next", "更新後次の志願者", $extra);
        //取消ボタン
        $extra = "onclick=\"return btn_submit('reset');\"";
        $arg["button"]["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);
        //終了ボタン
        $extra = "onclick=\"closeWin();\"";
        $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);

        /****************/
        /*  hidden作成  */
        /****************/
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "year", $model->year);
        knjCreateHidden($objForm, "applicantdiv", $model->applicantdiv);

        //インラインフレーム用Javascriptタグ生成
        $arg["IFRAME"] = View::setIframeJs();

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"] = $objForm->get_finish();

        View::toHTML($model, "knjl021eForm1.html", $arg);
    }
}
//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, &$value, $name, $extra, $size, $blank="") {
    $opt = array();
    $value_flg = false;
    if ($blank) $opt[] = array("label" => "", "value" => "");
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);
        if ($name == 'TESTDIV') {
            if ($value == "" && $row["NAMESPARE2"] == '1') $value = $row["VALUE"];
        }
        if ($value === $row["VALUE"]) $value_flg = true;
    }
    $value = ($value && $value_flg) ? $value : $opt[0]["value"];
    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);

    $result->free();
}
?>
