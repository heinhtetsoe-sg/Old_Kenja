<?php

require_once('for_php7.php');

class knjd155SubForm1
{
    function main(&$model)
    {
        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"]  = $objForm->get_start("sel", "POST", "knjd155index.php", "", "sel");

        //DB接続
        $db = Query::dbCheckOut();

        //対象年度・学期・年組・テスト情報
        $year = CTRL_YEAR.'年度';
        $semester = $model->control["学期名"][$model->semester];
        $hr_name = $db->getOne(knjd155Query::getHrName($model));
        $testname = $db->getOne(knjd155Query::getTestName($model));
        $arg["INFO"] = $year.'　　'.$semester.'　　'.$hr_name.'　　'.$testname;

        //生徒リストToリスト作成
        makeListToList($objForm, $arg, $db, $model);

        //チェックボックス
        for ($i=0; $i<2; $i++)
        {
            $extra = ($i == 1) ? "onClick=\"return check_all(this);\"" : "";
            $checked = ($model->replace_data["check"][$i] == "1") ? " checked" : "";
            $arg["data"]["RCHECK".$i] = knjCreateCheckBox($objForm, "RCHECK".$i, "1", $extra.$checked, "");
        }

        //項目名
        $arg["REMARK1_TYUI"] = "(全角{$model->hexam_record_remark_dat_remark1_moji}文字{$model->hexam_record_remark_dat_remark1_gyou}行まで)";
        $arg["REMARK_TITLE"] = $model->Properties["kojinSeisekihyouSyokenTitle"] ? $model->Properties["kojinSeisekihyouSyokenTitle"] : '通信欄';

        //通信欄テキストエリア
        $height = $model->hexam_record_remark_dat_remark1_gyou * 13.5 + ($model->hexam_record_remark_dat_remark1_gyou -1 ) * 3 + 5;
        $extra = "style=\"height:{$height}px;\"";
        $arg["data"]["REMARK"] = KnjCreateTextArea($objForm, "REMARK", $model->hexam_record_remark_dat_remark1_gyou, ($model->hexam_record_remark_dat_remark1_moji * 2 + 1), "soft", $extra, $model->replace_data["field"]["REMARK"]);

        //ボタン作成
        makeBtn($objForm, $arg);

        //hidden作成
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "selectdata");

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjd155SubForm1.html", $arg);
    }
}

//リストtoリスト作成
function makeListToList(&$objForm, &$arg, $db, $model) {

    //対象者リストを作成する
    $query = knjd155Query::getStudent($model);
    $result = $db->query($query);
    $opt1 = array();
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt1[] = array('label' => $row["ATTENDNO"]."番　".$row["NAME_SHOW"],
                        'value' => $row["SCHREGNO"]);
    }
    $result->free();

    $extra = "multiple style=\"width:165px\" width=\"165px\" ondblclick=\"move1('left')\"";
    $arg["main_part"]["CATEGORY_NAME"] = knjCreateCombo($objForm, "category_name", "", $opt1, $extra, 20);

    //生徒一覧リストを作成する//
    $query = knjd155Query::getStudent($model, "1");
    $result = $db->query($query);
    $opt2 = array();
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt2[] = array('label' => $row["ATTENDNO"]."番　".$row["NAME_SHOW"],
                        'value' => $row["SCHREGNO"]);
    }
    $result->free();

    $extra = "multiple style=\"width:165px\" width=\"165px\" ondblclick=\"move1('right')\"";
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
