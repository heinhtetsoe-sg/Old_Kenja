<?php

require_once('for_php7.php');


class knjl353Form1
{
    function main(&$model) {

        $objForm = new form;
        
        //フォーム作成
        $arg["start"]   = $objForm->get_start("knjl353Form1", "POST", "knjl353index.php", "", "knjl353Form1");

        //DB接続
        $db = Query::dbCheckOut();

        $arg["TOP"]["YEAR"] = $model->ObjYear;

        //入試制度コンボ
        $query = knjl353Query::GetName($model->ObjYear, "L003");
        $extra = "";
        makeCmb($objForm, $arg, $db, $query, $model->field["APPLICANTDIV"], "APPLICANTDIV", $extra, 1, "");
        
        //塾名
        $extra = "";
        $arg["data"]["JYUKU_NAME"] = knjCreateTextBox($objForm, $model->field["JYUKU_NAME"], "JYUKU_NAME", 20, 20, $extra);
        
        //教室名
        $extra = "";
        $arg["data"]["CLASS_ROOM"] = knjCreateTextBox($objForm, $model->field["CLASS_ROOM"], "CLASS_ROOM", 20, 20, $extra);

        //印刷ボタン
        $extra = "onClick=\"return newwin('" . SERVLET_URL . "');\"";
        $arg["button"]["btn_print"] = knjCreateBtn($objForm, "btn_print", "プレビュー／印刷", $extra);

        //終了
        $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", "onclick=\"closeWin();\"");

        knjCreateHidden($objForm, "YEAR", $model->ObjYear);
        knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
        knjCreateHidden($objForm, "PRGID", "KNJL353");
        knjCreateHidden($objForm, "CTRL_DATE", CTRL_DATE);
        knjCreateHidden($objForm, "cmd");

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();
        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjl353Form1.html", $arg); 
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
