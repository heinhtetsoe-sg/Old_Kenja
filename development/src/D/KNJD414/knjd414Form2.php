<?php

require_once('for_php7.php');

class knjd414Form2 {

    function main(&$model) {

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("edit", "POST", "knjd414index.php", "", "edit");

        //各フィールド取得
        if (isset($model->year) && isset($model->school_kind) && isset($model->classcd) && isset($model->school_kind) && isset($model->step_cd) && isset($model->learning_content_cd) && !isset($model->warning)) {
            if ($model->cmd === 'edit2') {
                $Row = knjd414Query::GuidanceContentsMst($model);
                $model->field["CLASSCD_RIGHT"] = $model->classcd;
                $model->field["STEP_CD_RIGHT"] = $model->step_cd;
                $model->field["LEARNING_CONTENT_CD_RIGHT"] = $model->learning_content_cd;
            } else {
                $Row =& $model->field;
                $Row2 =& $model->fields;
                if ($model->cmd === 'clear' && $model->left != true) {
                    // $Row = "";
                    foreach($Row as $key => $value){
                        $Row[$key] = NULL;
                    }
                }
            }
        } else {
            $Row =& $model->field;
            $Row2 =& $model->fields;
        }
        if($model->cmd === 'delete_after'){
            // $Row = "";
            foreach($Row as $key => $value){
                $Row[$key] = NULL;
            }
        }

        //DB接続
        $db = Query::dbCheckOut();

        //教科コンボ
        $query = knjd414Query::getClassMst($model);
        $extra = "onchange=\"return btn_submit('combo2');\"";
        makeCmb($objForm, $arg, $db, $query, "CLASSCD", $Row["CLASSCD"], $extra, 1, $model);
        knjCreateHidden($objForm, "CLASSCD_RIGHT", $model->field["CLASSCD_RIGHT"]);

        //段階コンボ
        $query = knjd414Query::getNameMst($model, 'right');
        $extra = "onchange=\"return btn_submit('combo2');\"";
        makeCmb($objForm, $arg, $db, $query, "STEP_CD", $Row["STEP_CD"], $extra, 1, $model);
        knjCreateHidden($objForm, "STEP_CD_RIGHT", $model->field["STEP_CD_RIGHT"]);

        //学習内容コード
        $extra = "";
        $arg["LEARNING_CONTENT_CD"] = knjCreateTextBox($objForm, $Row["LEARNING_CONTENT_CD"], "LEARNING_CONTENT_CD", 2, 2, $extra);
        knjCreateHidden($objForm, "LEARNING_CONTENT_CD_RIGHT", $model->field["LEARNING_CONTENT_CD_RIGHT"]);

        //学習内容
        $arg["LEARNING_CONTENT"] = knjCreateTextBox($objForm, $Row["LEARNING_CONTENT"], "LEARNING_CONTENT", 100, 200, $extra);
        //指導内容数
        if($model->cmd == 'edit2'){
            $Row["GUIDANCE_CONTENT_CD"] = $db->getOne(knjd414Query::OneGuidanceContentsMst($model, $i, 'cnt'));
        }
        $arg["GUIDANCE_CONTENT_CD"] = knjCreateTextBox($objForm, $Row["GUIDANCE_CONTENT_CD"], "GUIDANCE_CONTENT_CD", 2, 2, $extra);

        //指導内容
        if($model->cmd == 'read' || $model->cmd == 'edit2' || $model->cmd == 'button_after' || $model->cmd == 'clear'){
            $record  = "";
            if($model->cmd == 'edit2'){
                for ($i = 1; $i <= $Row["GUIDANCE_CONTENT_CD"]; $i++) {
                    $Row2["GUIDANCE_CONTENT".$i] = $db->getOne(knjd414Query::OneGuidanceContentsMst($model, $i, 'one'));
                    $record .= "<tr height='30'><th align='right' class='no_search' nowrap=''><b>指導内容".$i."</b></th>";
                    $record .= "\n<td bgcolor='#ffffff'>&nbsp;".knjCreateTextBox($objForm, $Row2["GUIDANCE_CONTENT".$i], "GUIDANCE_CONTENT".$i, 100, 100, $extra)."</td></tr>";
                }
            } elseif($model->cmd == 'read' || $model->cmd == 'button_after' || $model->left == true){
                $guidance_content_cd = $model->guidance_content_cd;
                for ($i = 1; $i <= $guidance_content_cd; $i++) {
                    if($model->left == true){
                        $Row2["GUIDANCE_CONTENT".$i] = $db->getOne(knjd414Query::OneGuidanceContentsMst($model, $i, 'one'));
                        $record .= "<tr height='30'><th align='right' class='no_search' nowrap=''><b>指導内容".$i."</b></th>";
                        $record .= "\n<td bgcolor='#ffffff'>&nbsp;".knjCreateTextBox($objForm, $Row2["GUIDANCE_CONTENT".$i], "GUIDANCE_CONTENT".$i, 100, 100, $extra)."</td></tr>";    
                    } else{
                        $record .= "<tr height='30'><th align='right' class='no_search' nowrap=''><b>指導内容".$i."</b></th>";
                        $record .= "\n<td bgcolor='#ffffff'>&nbsp;".knjCreateTextBox($objForm, $Row2["GUIDANCE_CONTENT".$i], "GUIDANCE_CONTENT".$i, 100, 100, $extra)."</td></tr>";
                    }
                }    
            }
            $arg["data"] = array("RECORD" => $record);
        }

        //ボタン作成
        makeBtn($objForm, $arg, $model);

        //hiddenを作成する
        makeHidden($objForm);

        if (VARS::post("cmd") == "add" || VARS::post("cmd") == "update" || VARS::post("cmd") == "delete") {
            $arg["jscript"] = "window.open('knjd414index.php?cmd=list&shori=update','left_frame');";
        }

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"] = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjd414Form2.html", $arg);
    }
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size) {
    $opt = array();
    $value_flg = false;
    $result = $db->query($query);
    $opt[] = array('label' => "",'value' => "");
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);
        if ($value == $row["VALUE"]) $value_flg = true;
    }
    $result->free();

    $value = ($value && $value_flg) ? $value : $opt[0]["value"];

    $arg[$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}

//ボタン作成
function makeBtn(&$objForm, &$arg, $model) {
    //読込
    $extra = "onclick=\"return btn_submit('read');\"";
    $arg["button"]["btn_read"] = knjCreateBtn($objForm, "btn_read", "読 込", $extra);    
    //追加
    $extra = "onclick=\"return btn_submit('add');\"";
    $arg["button"]["btn_add"] = knjCreateBtn($objForm, "btn_add", "追 加", $extra);
    //更新
    $extra = "onclick=\"return btn_submit('update');\"";
    $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);
    //削除
    $extra = "onclick=\"return btn_submit('delete');\"";
    $arg["button"]["btn_delete"] = knjCreateBtn($objForm, "btn_delete", "削 除", $extra);
    //取消
    $extra = "onclick=\"return btn_submit('clear');\"";
    $arg["button"]["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);
    //終了
    $extra = "onclick=\"closeWin();\"";
    $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);
}

//Hidden作成
function makeHidden(&$objForm) {
    knjCreateHidden($objForm, "cmd");
}
?>
