<?php

require_once('for_php7.php');

class knjz439Form1
{
    function main(&$model)
    {
        $objForm = new form;
        $arg["start"]   = $objForm->get_start("main", "POST", "knjz439index.php", "", "main");
        $db = Query::dbCheckOut();

        //教育課程コンボ
        $query = knjz439Query::getCurriculumCD();
        $extra = "onChange=\"return btn_submit('main');\"";
        makeCombo($objForm, $arg, $db, $query, $model->field["CURRICULUM_CD"], "CURRICULUM_CD", $extra, 1, 'BLANK');

        //リスト表示
        $model->data=array();
        if ($model->field["CURRICULUM_CD"]) {
            makeList($objForm, $arg, $db, $model);
        }

        //事前準備の文言
        if ($model->Properties["useClassDetailDat"] == '1') {
            $arg["LABEL"] = '教科マスタの詳細登録を設定して下さい。';
        } else {
            $arg["LABEL"] = '名称マスタ「D031」に教科を登録して下さい。';
        }

        //ボタン作成
        makeButton($objForm, $arg, $model);

        //hidden
        makeHidden($objForm);

        $arg["finish"] = $objForm->get_finish();
        Query::dbCheckIn($db);
        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjz439Form1.html", $arg); 
    }
}
/***************************************    これ以下は関数    **************************************************/
////////////////////////
////////////////////////コンボ作成
////////////////////////
function makeCombo(&$objForm, &$arg, $db, $query, &$value, $name, $extra, $size, $blank = "")
{
    $opt = array();
    if ($blank == "BLANK") {
        $opt[] = array ("label" => "",
                        "value" => "");
    }
    $flg = 0;
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
        $opt[] = array ("label" => $row["LABEL"],
                        "value" => $row["VALUE"]);

        if ($row["VALUE"] == $value) {
            $flg = 1;
        }
    }
    $result->free();

    $value = $flg == 1 ? $value : $opt[0]["value"];
    $arg[$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}
////////////////////////
////////////////////////リスト表示
////////////////////////
function makeList(&$objForm, &$arg, $db, &$model) {
    $counter = 0;
    $dataFlg = false;
    $query = knjz439Query::getList($model);
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        //科目コード保管
        $model->data["SUBCLASSCD"][] = $row["VALUE"];
        //標準単位数テキストボックス
        $extra = "style=\"text-align: right\" onblur=\"this.value=toInteger(this.value)\" ";
        $name = "CREDITS";
        $row[$name] = knjCreateTextBox($objForm, $row[$name], $name."-".$counter, 4, 2, $extra);
        //標準科目チェックボックス
        $name = "UPDATE_DATA";
        $extra = strlen($row["CHECK"]) ? "checked" : "";
        $row[$name] = knjCreateCheckBox($objForm, $name."-".$counter, "1", $extra);

        $counter++;
        $dataFlg = true;
        $arg["data"][] = $row;
    }
    $result->free();
}
////////////////////////
////////////////////////ボタン作成
////////////////////////
function makeButton(&$objForm, &$arg, &$model) {
    //更新ボタン
    $extra  = (0 < get_count($model->data)) ? "" : "disabled ";
    $extra .= "onclick=\"return btn_submit('update');\"";
    $arg["button"]["btn_updte"] = knjCreateBtn($objForm, "btn_updte", "更 新", $extra);

    //終了ボタン
    $extra = "onclick=\"return closeWin();\"";
    $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);
}
////////////////////////
////////////////////////hidden作成
////////////////////////
function makeHidden(&$objForm) {
    knjCreateHidden($objForm, "cmd");
}
?>
