<?php

require_once('for_php7.php');

class knjd183cSubForm1
{
    public function main(&$model)
    {
        //オブジェクト作成
        $objForm = new form();

        //フォーム作成
        $arg["start"] = $objForm->get_start("sel", "POST", "knjd183cindex.php", "", "sel");

        //DB接続
        $db = Query::dbCheckOut();

        //対象年度・学期・年組・テスト情報
        $year = CTRL_YEAR.'年度';
        $chairname = $db->getOne(knjd183cQuery::getChairName($model));
        $arg["INFO"] = $year.'　　'.$chairname;

        //生徒リストToリスト作成
        makeListToList($objForm, $arg, $db, $model);

        //チェックボックス
        $extra = "onClick=\"return check_all(this);\"";
        $checked = ($model->replace_data["check_all"] == "1") ? " checked" : "";
        $arg["data"]["RCHECK5"] = knjCreateCheckBox($objForm, "RCHECK5", "1", $extra.$checked, "");

        //オプションの値を取得
        $nyuryokuPattern = "";
        $result = $db->query(knjd183cQuery::getNyuryokuPattern());
        while ($row1 = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $nyuryokuPattern= $row1["REMARK1"];
        }
        $result->free();

        $soutanMojiA = 55;  //「総探のみ」Aパターン入力制限数
        $soutanMojiB = 35;  //「総探のみ」Bパターン入力制限数
        $doubleMojiA = 25;  //「両方使用」Aパターン入力制限数
        $doubleMojiB = 15;  //「両方使用」Bパターン入力制限数

        $maxTextCommentTemp = "(全角num1文字×4行まで、自立活動欄対象者は全角num2文字×4行まで)";
        $maxTextComment  = "";
        $gyou = 4;
        $moji = 0;
        if ($nyuryokuPattern == '1') {
            //総探のみ使用1行55文字のテキストエリアを表示するために横幅を拡張
            $allwidthPattern = 1150;
            $maxTextComment = str_replace("num1", $soutanMojiA, $maxTextCommentTemp);
            $maxTextComment = str_replace("num2", $soutanMojiB, $maxTextComment);
            $moji = $soutanMojiA;
        } elseif ($nyuryokuPattern == '3') {
            //両方使用
            $maxTextComment = str_replace("num1", $doubleMojiA, $maxTextCommentTemp);
            $maxTextComment = str_replace("num2", $doubleMojiB, $maxTextComment);
            $moji = $doubleMojiA;
        }

        $arg["item"] = array();
        //表示名
        for ($i = 0; $i < get_count($model->itemMst); $i++) {
            $colname = $model->itemMst[$i]["COLUMNNAME"];
            if ($model->itemMst[$i]["PATTERN_SHOW_FLG"] == "1") {
                $extra  = "onclick=\"return btn_submit('teikei', '{$colname}', '{$model->itemMst[$i]["PATTERN_DATA_DIV"]}', '{$model->itemMst[$i]["ITEMNAME"]}');\"";
                $model->itemMst[$i]["btn_teikei"] = knjCreateBtn($objForm, "btn_teikei", "定型文選択", $extra);
            }

            $extra = "";
            $checked = ($model->replace_data["check"][$colname] == "1") ? "checked" : "";
            $model->itemMst[$i]["RCHECK"] = knjCreateCheckBox($objForm, "RCHECK_".$colname, "1", $extra.$checked, "");

            $model->itemMst[$i]["COMMENT"] = $maxTextComment;

            //テキストエリア
            if ($moji != 0) {
                $height = (int)$gyou * 13.5 + ((int)$gyou - 1) * 3 + 5;
                $extra = "style=\"height:{$height}px;\"";
                $model->itemMst[$i]["DATA"] = KnjCreateTextArea($objForm, $colname, $gyou, (int)$moji * 2 + 1, "soft", $extra, $model->replace_data["field"][$colname]);
                $arg["item"][] = $model->itemMst[$i];
            }
        }

        knjCreateHidden($objForm, "SEMESTER", $model->semester);
        knjCreateHidden($objForm, "CHAIRCD", $model->chaircd);
        knjCreateHidden($objForm, "KNJD183C_semesCombo", $model->Properties["KNJD183C_semesCombo"]);

        //ボタン作成
        makeBtn($objForm, $arg);

        //hidden作成
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "selectdata");
        knjCreateHidden($objForm, "useFrameLock", $model->Properties["useFrameLock"]);
        knjCreateHidden($objForm, "itemMstJson", $model->itemMstJson);

        //DB切断
        Query::dbCheckIn($db);

        $arg["IFRAME"] = VIEW::setIframeJs();

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjd183cSubForm1.html", $arg);
    }
}

//リストtoリスト作成
function makeListToList(&$objForm, &$arg, $db, $model)
{
    //学籍処理日が学期範囲外の場合、学期終了日を使用する。
    if ($model->Properties["KNJD183C_semesCombo"] == "1") {
        $setSemster = $model->semester;
    } else {
        $setSemster = CTRL_SEMESTER;
    }
    $sdate = str_replace("/", "-", $model->control["学期開始日付"][$setSemster]);
    $edate = str_replace("/", "-", $model->control["学期終了日付"][$setSemster]);
    if ($sdate <= CTRL_DATE && CTRL_DATE <= $edate) {
        $execute_date = CTRL_DATE;  //初期値
    } else {
        $execute_date = $edate;     //初期値
    }

    //対象者リストを作成する
    $query = knjd183cQuery::getStudent($model, $execute_date);
    $result = $db->query($query);
    $opt1 = array();
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt1[] = array('label' => $row["ATTENDNO"]."　".$row["NAME_SHOW"],
                        'value' => $row["VALUE"]);
    }
    $result->free();

    $extra = "multiple style=\"width:230px\" ondblclick=\"move1('left')\"";
    $arg["main_part"]["CATEGORY_NAME"] = knjCreateCombo($objForm, "category_name", "", $opt1, $extra, 20);

    //生徒一覧リストを作成する//
    $query = knjd183cQuery::getStudent($model, $execute_date, "1");
    $result = $db->query($query);
    $opt2 = array();
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt2[] = array('label' => $row["ATTENDNO"]."　".$row["NAME_SHOW"],
                        'value' => $row["VALUE"]);
    }
    $result->free();

    $extra = "multiple style=\"width:230px\" ondblclick=\"move1('right')\"";
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
function makeBtn(&$objForm, &$arg)
{
    //更新ボタンを作成する
    $extra = "onclick=\"return doSubmit()\"";
    $arg["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);

    //戻るボタンを作成する
    $extra = "onclick=\"return btn_submit('back');\"";
    $arg["btn_back"] = knjCreateBtn($objForm, "btn_back", "戻 る", $extra);
}
