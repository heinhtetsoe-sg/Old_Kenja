<?php

require_once('for_php7.php');

class knjxselect_staff
{
    public function main(&$model)
    {
        //オブジェクト作成
        $objForm = new form();

        //フォーム作成
        $arg["start"] = $objForm->get_start("knjxselect_staff", "POST", "knjxselect_staffindex.php", "", "knjxselect_staff");

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["YEAR"] = CTRL_YEAR;

        //職員データ表示
        $query = knjxselect_staffQuery::getStaffDate();
        $result = $db->query($query);
        $counter = 0;
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            //チェックボックスのvalueにSTAFFCDをセット
            $extra = "id=\"STAFFCHECK-".$counter."\"; onChange=\"changeCheck(this)\"";
            $item = knjCreateCheckBox($objForm, "STAFFCHECK", $row["STAFFCD"], $extra, "");
            //hiddenフィールドにSTAFFNAMEをセット
            $item .= knjCreateHidden($objForm, "STAFFNAME-".$counter, $row["STAFFNAME"], $extra, "");
            $row["item"] = $item;
            $arg["data"][] = $row;
            $counter++;
        }
        $result->free();

        //選択ボタン
        $extra = "onclick=\"goWin()\"";
        $arg["button"]["btn_select"] = knjCreateBtn($objForm, "btn_select", "選 択", $extra);

        //戻るボタン
        $extra = "onclick=\"closePop();\"";
        $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "戻 る", $extra);

        //hidden
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "COUNTER", $counter);
        knjCreateHidden($objForm, "TEXT_CD", $model->textCd);
        knjCreateHidden($objForm, "TEXT_NAME", $model->textName);

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"] = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjxselect_staff.html", $arg);
    }
}
//makeCmb
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $blank = "")
{
    $opt = array();
    $value_flg = false;
    if ($blank == "BLANK") {
        $opt[] = array("label" => "", "value" => "");
    }
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);
        if ($value === $row["VALUE"]) {
            $value_flg = true;
        }
    }
    $value = ($value != "" && $value_flg) ? $value : $opt[0]["value"];
    $arg[$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);

    $result->free();
}
