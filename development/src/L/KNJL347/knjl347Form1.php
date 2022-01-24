<?php

require_once('for_php7.php');

class knjl347Form1
{
    function main(&$model) {

        $objForm = new form;

        //フォーム作成
        $arg["start"]   = $objForm->get_start("knjl347Form1", "POST", "knjl347index.php", "", "knjl347Form1");

        //DB接続
        $db = Query::dbCheckOut();

        $arg["TOP"]["YEAR"] = $model->ObjYear;

        //入試制度コンボ
        $query = knjl347Query::GetName($model->ObjYear, "L003");
        $extra = "";
        makeCmb($objForm, $arg, $db, $query, $model->field["APPLICANTDIV"], "APPLICANTDIV", $extra, 1, "");

        //入試区分コンボ
        $query = knjl347Query::getTestdivMst($model->ObjYear);
        $extra = "";
        makeCmb($objForm, $arg, $db, $query, $model->field["TESTDIV"], "TESTDIV", $extra, 1, "");

        //帳票パターンラジオ（1:得点ゾーンあり、2:得点あり）
        $opt = array(1, 2);
        $model->field["TARGET_DIV"] = ($model->field["TARGET_DIV"] == "") ? "1" : $model->field["TARGET_DIV"];
        $extra = array();
        foreach($opt as $key => $val) {
            array_push($extra, " id=\"TARGET_DIV{$val}\" ");
        }
        $radioArray = knjCreateRadio($objForm, "TARGET_DIV", $model->field["TARGET_DIV"], $extra, $opt, get_count($opt));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

        //checkbox
        $extra = " checked id=\"SAITEI_NODISP\" ";
        $arg["data"]["SAITEI_NODISP"] = knjCreateCheckBox($objForm, "SAITEI_NODISP", "1", $extra);

        //受験番号
        $extra = " onblur=\"this.value=toInteger(this.value)\"";
        $arg["data"]["EXAMNO"] = knjCreateTextBox($objForm, $model->field["EXAMNO"], "EXAMNO", 5, 5, $extra);

        //印刷ボタンを作成する
        $extra = "onclick=\"return newwin('" . SERVLET_URL . "');\"";
        $arg["button"]["btn_print"] = knjCreateBtn($objForm, "btn_print", "プレビュー／印刷", $extra);

        //終了ボタンを作成する
        $extra = "onclick=\"closeWin();\"";
        $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);

        //hiddenを作成する
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "YEAR", $model->ObjYear);
        knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
        knjCreateHidden($objForm, "PRGID", "KNJL347");

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjl347Form1.html", $arg); 
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
        if ($value == $row["VALUE"]) $value_flg = true;
    }
    $value = ($value && $value_flg) ? $value : $opt[0]["value"];
    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);

    $result->free();
}
?>
