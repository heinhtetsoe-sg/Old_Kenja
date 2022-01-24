<?php

require_once('for_php7.php');

class knje360aSubform1 {

    function main(&$model) {

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("subform1", "POST", "knje360aindex.php", "", "subform1");

        //DB接続
        $db = Query::dbCheckOut();

        //検索方法（1:学校名, 2:学校コード）
        $opt = array(1, 2);
        $model->search_div = ($model->search_div == "") ? "1" : $model->search_div;
        $extra = array();
        foreach($opt as $key => $val) {
            array_push($extra, " id=\"SEARCH_DIV{$val}\" onClick=\"btn_submit('select')\"");
        }
        $radioArray = knjCreateRadio($objForm, "SEARCH_DIV", $model->search_div, $extra, $opt, get_count($opt));
        foreach($radioArray as $key => $val) $arg[$key] = $val;

        if ($model->search_div == "1") {
            //学校名入力
            $arg["SEARCH_DIV_1"] = "1";
            $arg["SEARCH_DIV_2"] = "";
            $extra = "onkeydown=\"return keydownEvent('select_search');\"";
            $arg["data"]["SEARCH_TXT"] = knjCreateTextBox($objForm, $model->select["field"]["SEARCH_TXT"], "SEARCH_TXT", 20, 20, $extra);
        } else {
            //学校コード入力
            $arg["SEARCH_DIV_1"] = "";
            $arg["SEARCH_DIV_2"] = "1";
            $extra = "onkeydown=\"return keydownEvent('select_search');\" onblur=\"this.value=toInteger2(this.value);\" id=\"eisuFuka\"";
            $arg["data"]["SEARCH_NO"] = knjCreateTextBox($objForm, $model->select["field"]["SEARCH_NO"], "SEARCH_NO", 20, "", $extra);
        }

        //検索ボタン
        $extra = "onclick=\"return btn_submit('select_search');\"";
        $arg["button"]["btn_search"] = knjCreateBtn($objForm, "btn_search", "検 索", $extra);

        //ALLチェック
        $extra = "onClick=\"check_all(this);\"";
        $arg["CHECKALL"] = knjCreateCheckBox($objForm, "CHECKALL", "", $extra, "");

        //検索結果（学校一覧表示）
        $counter = 0;
        if (($model->select["field"]["SEARCH_TXT"] || $model->select["field"]["SEARCH_NO"]) && $model->cmd == "select_search") {
            $query = knje360aQuery::getSchoolList($model);
            if ($query) {
                $result = $db->query($query);
                while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                    if (is_array($model->replace["data_chk"])) {
                        $extra = (in_array($row["SCHOOL_CD"], $model->replace["data_chk"])) ? "checked" : "";
                    } else {
                        $extra = "";
                    }
                    $extra .= " onclick=\"OptionUse();\"";
                    $row["CHECKED"] = knjCreateCheckBox($objForm, "CHECKED", $row["SCHOOL_CD"], $extra, "1");

                    //背景色
                    $row["BGCOLOR"] = ($counter % 2 == 0) ? "#ffffff" : "#cccccc";

                    $arg["list"][] = $row;
                    $counter++;
                }
                $result->free();
            }
        }

        //高さ
        $arg["HEIGHT"] = ($counter > 8) ? "height:200;" : "";

        //検索結果件数
        $arg["COUNTER"] = $counter;

        //ボタン作成
        makeBtn($objForm, $arg, $model);

        //hidden作成
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "SCHOOLCD");
        knjCreateHidden($objForm, "DATA_SELECT", $model->data_select);

        //DB切断
        Query::dbCheckIn($db);

        //選択ボタン押下後は画面を閉じる
        if ($model->cmd == "select_school"){
            $arg["jscript"] = "parent.closeit();";
            $arg["reload"]  = "parent.location.href='knje360aindex.php?cmd=edit&SCH_CD=".$model->schoolcd."&SEND_DATA_SELECT=".$model->data_select."'";
        }

        //フォーム終わり
        $arg["finish"] = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knje360aSubform1.html", $arg); 
    }
}

//ボタン作成
function makeBtn(&$objForm, &$arg, $model) {
    //選択ボタン
    $extra = "disabled onclick=\"return btn_submit('select_school');\"";
    $arg["button"]["btn_select"] = knjCreateBtn($objForm, "btn_select", "選 択", $extra);
    //戻るボタン
    $extra = "onclick=\"return parent.closeit()\"";
    $arg["button"]["btn_back"] = knjCreateBtn($objForm, "btn_back", "戻 る", $extra);
}
?>
