<?php

require_once('for_php7.php');

class knjd143SubForm1
{
    function main(&$model)
    {
        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"]  = $objForm->get_start("sel", "POST", "knjd143index.php", "", "sel");

        //DB接続
        $db = Query::dbCheckOut();

        //対象年度・学期・年組・テスト情報
        $year = CTRL_YEAR.'年度';
        $chairname = $db->getOne(knjd143Query::getChairName($model));
        $arg["INFO"] = $year.'　　'.$chairname;

        //生徒リストToリスト作成
        makeListToList($objForm, $arg, $db, $model);

        //学習内容、評価のテキストエリア表示
        if ($model->Properties["TOTALSTUDYACT_STUDYTIME_UseTextFlg"] == '1') {
            $s_checkno = "2";
        } else {
            $arg["usetext"] = 1;
            $s_checkno = "0";
        }

        //チェックボックス
        for ($i=0; $i<6; $i++)
        {
            $extra = ($i == 5) ? "onClick=\"return check_all(this, '".$s_checkno."');\"" : "";
            $checked = ($model->replace_data["check"][$i] == "1") ? " checked" : "";
            $arg["data"]["RCHECK".$i] = knjCreateCheckBox($objForm, "RCHECK".$i, "1", $extra.$checked, "");
        }

        //学習内容テキストエリア
        $extra = "style=\"height:36px;\" id=\"TOTALSTUDYACT\"";
        $arg["data"]["TOTALSTUDYACT"] = KnjCreateTextArea($objForm, "TOTALSTUDYACT", 2, 51, "soft", $extra, $model->replace_data["field"]["TOTALSTUDYACT"]);
        //学習内容定型文選択
        if ($model->Properties["TOTALSTUDYACT_STUDYTIME_UseTextFlg"] != '1') {
            if ($model->Properties["tutisyoTeikei_Button_Hyouji"] == "1") {
                $extra  = "onclick=\"return btn_submit('teikei');\"";
                $arg["btn_teikei"] = "<br>".knjCreateBtn($objForm, "btn_teikei", "定型文選択", $extra);
            }
        }

        //評価テキストエリア
        $extra = "style=\"height:50px;\"";
        $arg["data"]["TOTALSTUDYTIME"] = KnjCreateTextArea($objForm, "TOTALSTUDYTIME", 3, 51, "soft", $extra, $model->replace_data["field"]["TOTALSTUDYTIME"]);

        //学年評定テキストボックス
        $extra = " onBlur=\"this.value=toInteger(this.value);\" STYLE=\"text-align:right;\"";
        $arg["data"]["GRAD_VALUE"] = knjCreateTextBox($objForm, $model->replace_data["field"]["GRAD_VALUE"], "GRAD_VALUE", 3, 2, $extra);

        //履修単位テキストボックス
        $extra = " onBlur=\"this.value=toInteger(this.value);\" STYLE=\"text-align:right;\"";
        $arg["data"]["COMP_CREDIT"] = knjCreateTextBox($objForm, $model->replace_data["field"]["COMP_CREDIT"], "COMP_CREDIT", 3, 2, $extra);

        //修得単位テキストボックス
        $extra = " onBlur=\"this.value=toInteger(this.value);\" STYLE=\"text-align:right;\"";
        $arg["data"]["GET_CREDIT"] = knjCreateTextBox($objForm, $model->replace_data["field"]["GET_CREDIT"], "GET_CREDIT", 3, 2, $extra);

        //ボタン作成
        makeBtn($objForm, $arg);

        //hidden作成
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "selectdata");
        knjCreateHidden($objForm, "useFrameLock", $model->Properties["useFrameLock"]);

        $arg["IFRAME"] = VIEW::setIframeJs();

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjd143SubForm1.html", $arg);
    }
}

//リストtoリスト作成
function makeListToList(&$objForm, &$arg, $db, $model) {

    //学籍処理日が学期範囲外の場合、学期終了日を使用する。
    $sdate = str_replace("/", "-", $model->control["学期開始日付"][CTRL_SEMESTER]);
    $edate = str_replace("/", "-", $model->control["学期終了日付"][CTRL_SEMESTER]);
    if ($sdate <= CTRL_DATE && CTRL_DATE <= $edate) {
        $execute_date = CTRL_DATE;  //初期値
    } else {
        $execute_date = $edate;     //初期値
    }

    //対象者リストを作成する
    $query = knjd143Query::getStudent($model, $execute_date);
    $result = $db->query($query);
    $opt1 = array();
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt1[] = array('label' => $row["ATTENDNO"]."　".$row["NAME_SHOW"],
                        'value' => $row["VALUE"]);
    }
    $result->free();

    $extra = "multiple style=\"width:100%\" width=\"100%\" ondblclick=\"move1('left')\"";
    $arg["main_part"]["CATEGORY_NAME"] = knjCreateCombo($objForm, "category_name", "", $opt1, $extra, 20);

    //生徒一覧リストを作成する//
    $query = knjd143Query::getStudent($model, $execute_date, "1");
    $result = $db->query($query);
    $opt2 = array();
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt2[] = array('label' => $row["ATTENDNO"]."　".$row["NAME_SHOW"],
                        'value' => $row["VALUE"]);
    }
    $result->free();

    $extra = "multiple style=\"width:100%\" width=\"100%\" ondblclick=\"move1('right')\"";
    $arg["main_part"]["CATEGORY_SELECTED"] = knjCreateCombo($objForm, "category_selected", "", $opt2, $extra, 20);

    //対象選択ボタンを作成する
    $extra = "style=\"height:20px;width:35px\" onclick=\"moves('right');\"";
    $arg["main_part"]["SEL_DEL_ALL"] = knjCreateBtn($objForm, "sel_del_all", ">>", $extra);
    //対象取消ボタンを作成する
    $extra = "style=\"height:20px;width:35px\" onclick=\"moves('left');\"";
    $arg["main_part"]["SEL_ADD_ALL"] = knjCreateBtn($objForm, "sel_add_all", "<<", $extra);
    //対象選択ボタンを作成する
    $extra = "style=\"height:20px;width:35px\" onclick=\"move1('right');\"";
    $arg["main_part"]["SEL_DEL"] = knjCreateBtn($objForm, "sel_del", "＞", $extra);
    //対象取消ボタンを作成する
    $extra = "style=\"height:20px;width:35px\" onclick=\"move1('left');\"";
    $arg["main_part"]["SEL_ADD"] = knjCreateBtn($objForm, "sel_add", "＜", $extra);
}

//ボタン作成
function makeBtn(&$objForm, &$arg) {

    //更新ボタンを作成する
    $extra = "onclick=\"return doSubmit()\"";
    $arg["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);

    //戻るボタンを作成する
    $extra = "onclick=\"return btn_submit('back');\"";
    $arg["btn_back"] = knjCreateBtn($objForm, "btn_back", "戻 る", $extra);
}
?>
