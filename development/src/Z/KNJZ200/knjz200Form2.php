<?php

require_once('for_php7.php');

class knjz200Form2
{
    public function main(&$model)
    {
        //権限チェック
        if (AUTHORITY != DEF_UPDATABLE) {
            $arg["jscript"] = "OnAuthError();";
        }
        $db = Query::dbCheckOut();
        $objForm = new form();
        //フォーム作成
        $arg["start"]   = $objForm->get_start("edit", "POST", "knjz200index.php", "", "edit");

        //警告メッセージを表示しない場合
        if (!isset($model->warning)) {
            $Row = knjz200Query::getRow($model, $db);
        } else {
            $Row =& $model->field;
        }

        //授業時数のフラグ  欠課数上限値の入力可、付加の判定に使う
        $query = knjz200Query::getJugyouJisuFlg();
        $jugyou_jisu_flg = $db->getOne($query); //1:法定授業 2:実授業

        //実授業の場合、欠課数上限値は表示しない
        if ($jugyou_jisu_flg != '2') {
            $arg["show"]["ABSENCE_HIGH_SHOW"] = "show";
        }
        knjCreateHidden($objForm, "JUGYOU_JISU_FLG", $jugyou_jisu_flg);

        //学籍在籍データ件数
        $regd_cnt = $db->getOne(knjz200Query::getRegdDatCnt());
        $flg = ($regd_cnt > 0) ? "" : 1;

        //科目コンボ設定
        $opt = array();
        $value_flg = false;
        $value = $model->field["SUBCLASSCD"];
        $query = knjz200Query::getSubclass($model);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[] = array('label' => $row["LABEL"],
                           'value' => $row["VALUE"]);
            if ($value == $row["VALUE"]) {
                $value_flg = true;
            }
        }
        $value = ($value && $value_flg) ? $value : $opt[0]["value"];
        $extra = "";
        $arg["data"]["SUBCLASSCD"] = knjCreateCombo($objForm, "SUBCLASSCD", $value, $opt, $extra, 1);

        //欠課数オーバーのタイトル
        if (in_array("1", $model->control["SEMESTER"])) {
            $arg["title"]["ABSENCE_WARN"]  = $model->control["学期名"]["1"];
        }
        if (in_array("2", $model->control["SEMESTER"])) {
            $arg["title"]["ABSENCE_WARN2"] = $model->control["学期名"]["2"];
        }
        if (in_array("3", $model->control["SEMESTER"])) {
            $arg["title"]["ABSENCE_WARN3"] = $model->control["学期名"]["3"];
        }
        //欠課数オーバーの前警告
        $query = knjz200Query::getNameMst("C042", "01"); // 1:回、1以外:週間
        $namespare1 = $db->getOne($query);
        $arg["data"]["ABSENCE_WARN_KAI"] = ($namespare1 == "1") ? "回" : "週間";

        /********************/
        /* テキストボックス */
        /********************/
        //単位数
        $extra = "onblur=\"this.value=toInteger(this.value)\"";
        $arg["data"]["CREDITS"] = knjCreateTextBox($objForm, $Row["CREDITS"], "CREDITS", 5, 2, $extra);
        //欠時数上限値(履修)
        $extra = "onblur=\"checkDecimal(this)\"";
        if ($jugyou_jisu_flg == '2') {
            $extra .= " disabled";
        }
        $arg["data"]["ABSENCE_HIGH"] = knjCreateTextBox($objForm, $Row["ABSENCE_HIGH"], "ABSENCE_HIGH", 5, 4, $extra);
        //欠時数上限値(修得)
        $extra = "onblur=\"checkDecimal(this)\"";
        if ($jugyou_jisu_flg == '2') {
            $extra .= " disabled";
        }
        $arg["data"]["GET_ABSENCE_HIGH"] = knjCreateTextBox($objForm, $Row["GET_ABSENCE_HIGH"], "GET_ABSENCE_HIGH", 5, 4, $extra);
        //欠課数オーバ
        if (in_array("1", $model->control["SEMESTER"])) {
            $extra = "onblur=\"this.value=toInteger(this.value)\"";
            $arg["data"]["ABSENCE_WARN"] = knjCreateTextBox($objForm, $Row["ABSENCE_WARN"], "ABSENCE_WARN", 2, 2, $extra);
        }
        if (in_array("2", $model->control["SEMESTER"])) {
            $extra = "onblur=\"this.value=toInteger(this.value)\"";
            $arg["data"]["ABSENCE_WARN2"] = knjCreateTextBox($objForm, $Row["ABSENCE_WARN2"], "ABSENCE_WARN2", 2, 2, $extra);
        }
        if (in_array("3", $model->control["SEMESTER"])) {
            $extra = "onblur=\"this.value=toInteger(this.value)\"";
            $arg["data"]["ABSENCE_WARN3"] = knjCreateTextBox($objForm, $Row["ABSENCE_WARN3"], "ABSENCE_WARN3", 2, 2, $extra);
        }

        //必履修区分コンボ設定
        $opt       = array();
        $opt[]     = array("label" => "","value" => "");
        $result    = $db->query(knjz200Query::getRequireName($model));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[] = array("label" => $row["LABEL"],
                           "value" => $row["VALUE"]);
        }
        $result->free();


        //必履修区分コンボ
        $objForm->ae(array("type"        => "select",
                            "name"        => "REQUIRE_FLG",
                            "size"        => "1",
                            "value"       => $Row["REQUIRE_FLG"],
                            "options"     => $opt ));

        $arg["data"]["REQUIRE_FLG"] = $objForm->ge("REQUIRE_FLG");

        //半期認定フラグ
        $check=($Row["AUTHORIZE_FLG"]=="1")? "checked":"";
        $objForm->ae(array("type"        => "checkbox",
                            "name"        => "AUTHORIZE_FLG",
                            "value"       => "1",
                            "extrahtml"   => $check." onclick=\"Check_a('on');\""));

        $arg["data"]["AUTHORIZE_FLG"] = $objForm->ge("AUTHORIZE_FLG");
        $arg["data"]["style_a"]=($check!="checked")?"":"半期";

        //無条件履修修得フラグ
        $check=($Row["COMP_UNCONDITION_FLG"]=="1")? "checked":"";
        $objForm->ae(array("type"        => "checkbox",
                            "name"        => "COMP_UNCONDITION_FLG",
                            "value"       => "1",
                            "extrahtml"   => $check." onclick=\"Check_c('on');\""));

        $arg["data"]["COMP_UNCONDITION_FLG"] = $objForm->ge("COMP_UNCONDITION_FLG");
        $arg["data"]["style_c"]=($check!="checked")?"":"無条件";

        //時間単位
        if ($model->Properties["useTimeUnit"] == '1') {
            $arg["useTimeUnit"] = "1";
            $extra = "onblur=\"checkDecimal(this)\"";
            $arg["data"]["TIME_UNIT"] = knjCreateTextBox($objForm, $Row["TIME_UNIT"], "TIME_UNIT", 5, 5, $extra);
        }

        //掛け率
        if ($model->Properties["useMultiplicationRate"] == '1') {
            $arg["useRate"] = "1";
            $extra = "onblur=\"checkDecimal(this)\"";
            $arg["data"]["RATE"] = knjCreateTextBox($objForm, $Row["RATE"], "RATE", 3, 3, $extra);
        }

        /**********/
        /* ボタン */
        /**********/
        //追加ボタンを作成する
        $extra = "onclick=\"return btn_submit('add');\"";
        $arg["button"]["btn_add"]    = knjCreateBtn($objForm, "btn_add", "追 加", $extra);
        //修正ボタンを作成する
        $extra = "onclick=\"return btn_submit('update');\"";
        $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);
        //削除ボタンを作成する
        $extra = "onclick=\"return btn_submit('delete');\"";
        $arg["button"]["btn_del"]    = knjCreateBtn($objForm, "btn_del", "削 除", $extra);
        //クリアボタンを作成する
        $extra = "onclick=\"return btn_submit('reset');\"";
        $arg["button"]["btn_reset"]  = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);
        //終了ボタンを作成する
        $extra = "onclick=\"closeWin();\"";
        $arg["button"]["btn_back"]   = knjCreateBtn($objForm, "btn_back", "終 了", $extra);

        //一括処理ボタン
        $link = REQUESTROOT."/Z/KNJZ200/knjz200index.php?cmd=replace";
        $extra = "onclick=\"Page_jumper('{$link}');\"";
        $arg["button"]["btn_replace"] = KnjCreateBtn($objForm, "btn_replace", "一括処理", $extra);

        //CSVファイルアップロードコントロール
        makeCsv($objForm, $arg, $model);

        /**********/
        /* hidden */
        /**********/
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "UPDATED", $Row["UPDATED"]);

        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();
        if (VARS::get("cmd") != "edit" && !isset($model->warning)) {
            $arg["reload"]  = "parent.left_frame.location.href='knjz200index.php?cmd=list';";
        }

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjz200Form2.html", $arg);
    }
}

//ＣＳＶ作成
function makeCsv(&$objForm, &$arg, $model)
{
    //出力取込種別ラジオボタン 1:取込 2:書出 3:エラー書出 4:見本
    $opt_shubetsu = array(1, 2, 3, 4);
    $model->field["OUTPUT"] = ($model->field["OUTPUT"]) ? $model->field["OUTPUT"] : "1";
    $click = " onclick=\"return changeRadio(this);\"";
    $extra = array("id=\"OUTPUT1\"".$click, "id=\"OUTPUT2\"".$click, "id=\"OUTPUT3\"".$click, "id=\"OUTPUT4\"".$click);
    $radioArray = knjCreateRadio($objForm, "OUTPUT", $model->field["OUTPUT"], $extra, $opt_shubetsu, get_count($opt_shubetsu));
    foreach ($radioArray as $key => $val) {
        $arg["data"][$key] = $val;
    }

    //ファイルからの取り込み
    $extra = ($model->field["OUTPUT"] == "1") ? "" : "disabled";
    $arg["FILE"] = knjCreateFile($objForm, "FILE", $extra, 1024000);

    //ヘッダ有チェックボックス
    $check_header  = "checked id=\"HEADER\"";
    $arg["data"]["HEADER"] = knjCreateCheckBox($objForm, "HEADER", "on", $check_header, "");

    //実行ボタン
    $extra = "onclick=\"return btn_submit('exec');\"";
    $arg["btn_exec"] = knjCreateBtn($objForm, "btn_exec", "実 行", $extra);
}
