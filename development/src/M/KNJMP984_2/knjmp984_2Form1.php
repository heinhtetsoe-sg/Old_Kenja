<?php

require_once('for_php7.php');

class knjmp984_2Form1
{
    function main(&$model)
    {
        //権限チェック
        if (AUTHORITY != DEF_UPDATABLE){
            $arg["jscript"] = "OnAuthError();";
        }

        $objForm = new form;
        //フォーム作成
        $arg["start"] = $objForm->get_start("knjmp984_2Form1", "POST", "knjmp984_2index.php", "", "knjmp984_2Form1");

        $db = Query::dbCheckOut();

        //年度コンボ
        $extra = "onchange=\"return btn_submit('');\"";
        $query = knjmp984_2Query::selectYearQuery($model);
        makeCombo($objForm, $arg, $db, $query, $model->field["YEAR"], "YEAR", $extra, 1, "", $model);

        //年度テキスト
        $extra = "onblur=\"this.value=toInteger(this.value);\"";
        $arg["data"]["year_add"] = knjCreateTextBox($objForm, "", "year_add", 5, 4, $extra);
        
        //年度追加
        $extra = "onclick=\"return add('');\"";
        $arg["button"]["btn_year_add"] = knjCreateBtn($objForm, "btn_year_add", "年度追加", $extra);
        
        //グループコンボ
        $extra = "onchange=\"return btn_submit('');\"";
        $query = knjmp984_2Query::selectGroupQuery($model);
        makeCombo($objForm, $arg, $db, $query, $model->field["LEVY_GROUP_CD"], "LEVY_GROUP_CD", $extra, 1, "", $model);

        //グループ設定
        $opt_left = array();
        $opt_right = array();
        //グループに所属する一覧取得
        $result = $db->query(knjmp984_2Query::selectQuery($model));   
        $opt_left_id = $opt_left = array();
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            $opt_left[] = array("label" => $row["LEVY_L_CD"]."  ".$row["LEVY_L_NAME"], 
                                "value" => $row["LEVY_L_CD"]);
            $opt_left_id[] = $row["LEVY_L_CD"];
        }
        $opt_right = array();
        //グループに所属しない一覧取得
        if (is_array($opt_left_id)){
            $result = $db->query(knjmp984_2Query::selectNoGroupQuery($opt_left_id,$model));   
            while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
                $opt_right[] = array("label" => $row["LEVY_L_CD"]."  ".$row["LEVY_L_NAME"], 
                                     "value" => $row["LEVY_L_CD"]);
            }
        }
        $result->free();

        $arg["info"] = array("LEFT_LIST"  => "設定済み会計科目一覧",
                             "RIGHT_LIST" => "グループ未設定の会計科目一覧");

        //グループに所属する
        $extra = "multiple STYLE=\"WIDTH:100%\" WIDTH=\"100%\" ondblclick=\"move('right','isGroup','noGroup',1)\"";
        $arg["data"]["isGroup"] = knjCreateCombo($objForm, "isGroup", "", $opt_left, $extra, 20);
                    
        //グループに所属しない
        $extra = "multiple STYLE=\"WIDTH:100%\" WIDTH=\"100%\" ondblclick=\"move('left','isGroup','noGroup',1)\"";
        $arg["data"]["noGroup"] = knjCreateCombo($objForm, "noGroup", "", $opt_right, $extra, 20);
        
        //対象取消ボタン（全部）
        $extra = "style=\"height:20px;width:40px\" onclick=\"return move('sel_del_all','isGroup','noGroup',1);\"";
        $arg["button"]["btn_rights"] = knjcreateBtn($objForm, "btn_rights", ">>", $extra);

        //対象選択ボタン（全部）
        $extra = "style=\"height:20px;width:40px\" onclick=\"return move('sel_add_all','isGroup','noGroup',1);\"";
        $arg["button"]["btn_lefts"] = knjcreateBtn($objForm, "btn_lefts", "<<", $extra);

        //対象取消ボタン（一部）
        $extra = "style=\"height:20px;width:40px\" onclick=\"return move('right','isGroup','noGroup',1);\"";
        $arg["button"]["btn_right1"] = knjcreateBtn($objForm, "btn_right1", "＞", $extra);

        //対象選択ボタン（一部）
        $extra = "style=\"height:20px;width:40px\" onclick=\"return move('left','isGroup','noGroup',1);\"";
        $arg["button"]["btn_left1"] = knjcreateBtn($objForm, "btn_left1", "＜", $extra);

        //更新ボタン
        $extra = "onclick=\"return doSubmit();\"";
        $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);

        //取消ボタン
        $extra = "onclick=\"return btn_submit('clear');\"";
        $arg["button"]["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);
                            
        //終了ボタンを作成する
        $link = REQUESTROOT."/M/KNJMP984/knjmp984index.php";
        $extra = "onclick=\"document.location.href='$link'\"";
        $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "戻 る", $extra);

        //hiddenを作成する
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "selectdata");

        Query::dbCheckIn($db);
                             
        $arg["finish"]  = $objForm->get_finish();

        $arg["TITLE"] = "予算グループマスタ";
        
        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjmp984_2Form1.html", $arg);
    }
}

//コンボ作成
function makeCombo(&$objForm, &$arg, $db, $query, &$value, $name, $extra, $size, $blank = "", $model) {
    $opt = array();
    $value_flg = false;
    if ($blank == "BLANK") {
        $opt[] = array ("label" => "",
                        "value" => "");
    }
    $result = $db->query($query);

    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
        $opt[] = array ("label" => $row["LABEL"],
                        "value" => $row["VALUE"]);
        if ($value == $row["VALUE"]) $value_flg = true;
    }
    $result->free();
    if ($name === 'YEAR') {
        $value = ($value && $value_flg) ? $value : $model->year;
    } else {
        $value = ($value && $value_flg) ? $value : $model->groupcd;
    }

    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}

?>
