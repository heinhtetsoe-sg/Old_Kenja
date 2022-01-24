<?php
class knjl610hForm2
{
    public function main(&$model)
    {
        //権限チェック
        if (AUTHORITY != DEF_UPDATABLE) {
            $arg["jscript"] = "OnAuthError();";
        }

        $objForm = new form();
        $arg["start"]   = $objForm->get_start("edit", "POST", "knjl610hindex.php", "", "edit");

        //DB接続
        $db = Query::dbCheckOut();

        //警告メッセージを表示しない場合
        if (($model->examno != "") && !isset($model->warning) && $model->cmd != 'search') {
            $query = knjl610hQuery::getRow($model);
            $Row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        } else {
            $Row =& $model->field;
            $Row["EXAMNO"] = $model->examno;
        }

        //新規ボタン押下
        if ($model->cmd == 'new') {
            $model->examno = "";
            $query = knjl610hQuery::getRow($model);
            $Row = $db->getRow($query, DB_FETCHMODE_ASSOC);
            $Row["EXAMNO"] = sprintf("%04d", $model->maxExamno);
        }

        //結合サイズ
        $arg["data"]["COLSPAN"] = "8";
        $arg["data"]["COLSPAN2"] = "4";
        $arg["data"]["COLSPAN3"] = "4";

        //登録番号
        $arg["data"]["EXAMNO"] = $Row["EXAMNO"];
        knjCreateHidden($objForm, "EXAMNO", $Row["EXAMNO"]);

        //受付年月日
        $arg["data"]["RECEPTDATE"] = View::popUpCalendarAlp($objForm, "RECEPTDATE", str_replace("-", "/", $Row["RECEPTDATE"]), $disabled, "");

        //欠席チェックボックス
        $extra = " id=\"DECLINE\" ";
        if ($Row["DECLINE"] == "1") {
            $extra .= " checked='checked' ";
        }
        $arg["data"]["DECLINE"] = knjCreateCheckBox($objForm, "DECLINE", "1", $extra);
        knjCreateHidden($objForm, "JUDGEMENT", $Row["JUDGEMENT"]);

        //入試日程
        $query = knjl610hQuery::getTestDiv($model);
        $extra = "";
        makeCmb($objForm, $arg, $db, $query, $Row["TESTDIV"], "TESTDIV", $extra, 1);

        //チャレンジ受験チェックボックス
        $extra = " id=\"SLIDE_FLG\" ";
        if ($Row["SLIDE_FLG"] == "1") {
            $extra .= " checked='checked' ";
        }
        $arg["data"]["SLIDE_FLG"] = knjCreateCheckBox($objForm, "SLIDE_FLG", "1", $extra);

        //類別
        $query = knjl610hQuery::getEntexamClassifyMst($model);
        $extra = "";
        makeCmb($objForm, $arg, $db, $query, $Row["TESTDIV1"], "TESTDIV1", $extra, 1);

        //氏名
        $extra = "";
        $arg["data"]["NAME"] = knjCreateTextBox($objForm, $Row["NAME"], "NAME", $model->nameKeta, $model->nameKeta, $extra);
        $arg["data"]["NAME_KETA"] = "(全角{$model->nameKeta}文字)";

        //氏名かな
        $extra = "";
        $arg["data"]["NAME_KANA"] = knjCreateTextBox($objForm, $Row["NAME_KANA"], "NAME_KANA", $model->nameKanaKeta, $model->nameKanaKeta, $extra);
        $arg["data"]["NAME_KANA_KETA"] = "(全角{$model->nameKanaKeta}文字)";

        //生年月日
        $arg["data"]["BIRTHDAY"] = View::popUpCalendarAlp($objForm, "BIRTHDAY", str_replace("-", "/", $Row["BIRTHDAY"]), $disabled, "");

        //性別
        $query = knjl610hQuery::getNameMst($model, "Z002");
        $extra = "";
        makeCmb($objForm, $arg, $db, $query, $Row["SEX"], "SEX", $extra, 1);

        //出身学校コード
        $extra = "onblur=\"this.value=toInteger(this.value)\" onChange=\"change_flg();\" onkeydown=\"keyChangeEntToTab(this)\" id=\"FINSCHOOLCD_ID\" ";
        $arg["data"]["FINSCHOOLCD"] = knjCreateTextBox($objForm, $Row["FINSCHOOLCD"], "FINSCHOOLCD", $model->fsCdKeta, $model->fsCdKeta, $extra);

        //学校名
        $query = knjl610hQuery::getFinschoolName($Row["FINSCHOOLCD"]);
        $fsArray = $db->getRow($query, DB_FETCHMODE_ASSOC);
        $arg["data"]["FINSCHOOLNAME"] = $fsArray["FINSCHOOL_DISTCD_NAME"].$fsArray["FINSCHOOL_NAME"];

        //かな検索ボタン（出身学校）
        $extra = "style=\"width:70px\" onclick=\"loadwindow('" .REQUESTROOT ."/X/KNJXSEARCH_FINSCHOOL/knjwfin_searchindex.php?cmd=searchMain&fscdname=FINSCHOOLCD_ID&fsname=FINSCHOOLNAME_ID&fsaddr=&school_div=', event.clientX + function () {var scrollX = document.documentElement.scrollLeft || document.body.scrollLeft;return scrollX;}(), event.clientY + function () {var scrollY = document.documentElement.scrollTop || document.body.scrollTop;return scrollY;}(), 500, 380)\"";
        $arg["button"]["btn_fin_kana_reference"] = knjCreateBtn($objForm, "btn_fin_kana_reference", "検 索", $extra);

        //資格
        $extra = " style='width:540px'";
        $arg["data"]["REMARK8"] = knjCreateTextArea($objForm, "REMARK8", "2", "80", "soft", $extra, $Row["REMARK8"]);
        $arg["data"]["REMARK8_KETA"] = "(全角{$model->remark8Keta}文字)";

        //併願校
        $extra = " style='width:540px'";
        $arg["data"]["REMARK9"] = knjCreateTextArea($objForm, "REMARK9", "2", "80", "soft", $extra, $Row["REMARK9"]);
        $arg["data"]["REMARK9_KETA"] = "(全角{$model->remark9Keta}文字)";

        //備考
        $extra = " style='width:540px'";
        $arg["data"]["REMARK10"] = knjCreateTextArea($objForm, "REMARK10", "2", "80", "soft", $extra, $Row["REMARK10"]);
        $arg["data"]["REMARK10_KETA"] = "(全角{$model->remark10Keta}文字)";

        //内申点(教科名)
        $i = 1;
        $query = knjl610hQuery::getEntexamSettingMst($model, "L008");
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            array_walk($row, "htmlspecialchars_array");
            $field = "SUBCLASSNAME".$i;
            $arg["data"]["$field"] = $row["NAME1"];
            $i++;
        }
        $result->free();

        //内申点(各教科・各合計)
        $extra = "onblur=\"this.value=toInteger(this.value)\"";
        $arg["data"]["CONFIDENTIAL_RPT01"] = knjCreateTextBox($objForm, $Row["CONFIDENTIAL_RPT01"], "CONFIDENTIAL_RPT01", 1, 1, $extra);
        $arg["data"]["CONFIDENTIAL_RPT02"] = knjCreateTextBox($objForm, $Row["CONFIDENTIAL_RPT02"], "CONFIDENTIAL_RPT02", 1, 1, $extra);
        $arg["data"]["CONFIDENTIAL_RPT03"] = knjCreateTextBox($objForm, $Row["CONFIDENTIAL_RPT03"], "CONFIDENTIAL_RPT03", 1, 1, $extra);
        $arg["data"]["CONFIDENTIAL_RPT04"] = knjCreateTextBox($objForm, $Row["CONFIDENTIAL_RPT04"], "CONFIDENTIAL_RPT04", 1, 1, $extra);
        $arg["data"]["CONFIDENTIAL_RPT05"] = knjCreateTextBox($objForm, $Row["CONFIDENTIAL_RPT05"], "CONFIDENTIAL_RPT05", 1, 1, $extra);
        $arg["data"]["TOTAL5"] = $Row["TOTAL5"];
        $arg["data"]["TOTAL3"] = $Row["TOTAL3"];
        $arg["data"]["TOTAL_ALL"] = knjCreateTextBox($objForm, $Row["TOTAL_ALL"], "TOTAL_ALL", 2, 2, $extra);

        //合計の算出で必要
        knjCreateHidden($objForm, "CONFIDENTIAL_RPT06", $Row["CONFIDENTIAL_RPT06"]);
        knjCreateHidden($objForm, "CONFIDENTIAL_RPT07", $Row["CONFIDENTIAL_RPT07"]);
        knjCreateHidden($objForm, "CONFIDENTIAL_RPT08", $Row["CONFIDENTIAL_RPT08"]);
        knjCreateHidden($objForm, "CONFIDENTIAL_RPT09", $Row["CONFIDENTIAL_RPT09"]);
        knjCreateHidden($objForm, "CONFIDENTIAL_RPT10", $Row["CONFIDENTIAL_RPT10"]);
        knjCreateHidden($objForm, "CONFIDENTIAL_RPT11", $Row["CONFIDENTIAL_RPT11"]);
        knjCreateHidden($objForm, "CONFIDENTIAL_RPT12", $Row["CONFIDENTIAL_RPT12"]);

        //CSVファイルアップロードコントロール
        makeCsv($objForm, $arg, $model);

        //ボタン作成
        makeBtn($objForm, $arg);
        knjCreateHidden($objForm, "SORT", $model->sort);

        //hidden
        knjCreateHidden($objForm, "cmd");

        //フォーム作成
        $arg["start"]   = $objForm->get_start("edit", "POST", "knjl610hindex.php", "", "edit");
        $arg["finish"]  = $objForm->get_finish();
        Query::dbCheckIn($db);
        if (VARS::get("cmd") != "edit" && !$model->warning) {
            $model->year = CTRL_YEAR+1;
            $arg["reload"]  = "parent.left_frame.location.href='knjl610hindex.php?cmd=list&HID_SORT=".$model->sort."';";
        }

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjl610hForm2.html", $arg);
    }
}
//makeCmb
function makeCmb(&$objForm, &$arg, $db, $query, &$value, $name, $extra, $size, $blank = "")
{
    $opt = array();
    if ($blank == "BLANK") {
        $opt[] = array("label" => "", "value" => "");
    }
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);
        if ($value === $row["VALUE"]) {
            $value_flg = true;
        }
    }
    $value = ($value != "" && $value_flg) ? $value : $opt[0]["value"];
    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);

    $result->free();
}

//CSV作成
function makeCsv(&$objForm, &$arg, $model)
{
    //出力取込種別ラジオボタン 1:取込 2:書出
    $opt_shubetsu = array(1, 2, 3);
    $model->field["OUTPUT"] = ($model->field["OUTPUT"]) ? $model->field["OUTPUT"] : "1";
    $extra = array("id=\"OUTPUT1\"", "id=\"OUTPUT2\"", "id=\"OUTPUT3\"");
    $radioArray = knjCreateRadio($objForm, "OUTPUT", $model->field["OUTPUT"], $extra, $opt_shubetsu, count($opt_shubetsu));
    foreach ($radioArray as $key => $val) {
        $arg["data"][$key] = $val;
    }

    //ファイルからの取り込み
    $extra = "";
    $arg["FILE"] = knjCreateFile($objForm, "FILE", $extra, 1024000);

    //ヘッダ有チェックボックス
    $check_header  = "checked id=\"HEADER\"";
    $arg["data"]["HEADER"] = knjCreateCheckBox($objForm, "HEADER", "on", $check_header, "");

    //実行ボタン
    $extra = "onclick=\"return btn_submit('csv');\"";
    $arg["button"]["btn_exec"] = knjCreateBtn($objForm, "btn_exec", "実 行", $extra);
}
//ボタン作成
function makeBtn(&$objForm, &$arg)
{
    //新規ボタン
    $extra = "onclick=\"return btn_submit('new');\"";
    $arg["button"]["btn_new"] = knjCreateBtn($objForm, "btn_new", "新 規", $extra);

    //追加ボタン
    $extra = "onclick=\"return btn_submit('add');\"";
    $arg["button"]["btn_add"] = knjCreateBtn($objForm, "btn_add", "追 加", $extra);

    //修正ボタン
    $extra = "onclick=\"return btn_submit('update');\"";
    $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);

    //削除ボタン
    $extra = "onclick=\"return btn_submit('delete');\"";
    $arg["button"]["btn_del"] = knjCreateBtn($objForm, "btn_del", "削 除", $extra);

    //クリアボタン
    $extra = "onclick=\"return btn_submit('reset');\"";
    $arg["button"]["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);

    //終了ボタン
    $extra = "onclick=\"closeWin();\"";
    $arg["button"]["btn_back"] = knjCreateBtn($objForm, "btn_back", "終 了", $extra);
}
