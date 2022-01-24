<?php

require_once('for_php7.php');

class knjxSearch {
    function main(&$model) {

        $objForm = new form;
        $arg = array();
        //フォーム作成
        $arg["start"]   = $objForm->get_start("knjxSearch", "POST", "knjxsearch_bankindex.php", "", "knjxSearch");

        //DB接続
        $db = Query::dbCheckOut();

        //年組コンボボックス
        $result = $db->query(knjxsearch_bankQuery::getHr_class($model));
        $opt = array();
        $opt[] = array("label" => "  ", "value" => "");
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[] = array("label"  => $row["HR_NAME"],
                            "value" => $row["GRADE"]."-".$row["HR_CLASS"]);
        }

        $extra = "";
        $arg["data"]["HR_CLASS"] = knjCreateCombo($objForm, "HR_CLASS", $value, $opt, $extra, 1);

        //学籍番号
        $arg["data"]["SRCH_SCHREGNO"] = knjCreateTextBox($objForm, "", "SRCH_SCHREGNO", 8, 8, "");

        //氏名
        $arg["data"]["NAME"]      = knjCreateTextBox($objForm, "", "NAME", 40, 40, "");
        //氏名表示用
        $arg["data"]["NAME_SHOW"] = knjCreateTextBox($objForm, "", "NAME_SHOW", 40, 40, "");
        //氏名かな
        $arg["data"]["NAME_KANA"] = knjCreateTextBox($objForm, "", "NAME_KANA", 40, 40, "");
        //英字氏名
        $extra = "onBlur=\"this.value=toAlpha(this.value);\"";
        $arg["data"]["NAME_ENG"]  = knjCreateTextBox($objForm, "", "NAME_ENG", 40, 40, $extra);

        $opt = array();
        $opt2 = array();
        $opt[] = array("label" => "   ", "value" => "");
        $opt2[] = array("label" => "   ", "value" => "");

        $result = $db->query(knjxsearch_bankQuery::selectBankcd($model));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[$row["BANKCD"]] = array("label"     => $row["BANKCD"] ."：" .htmlspecialchars($row["BANKNAME"]) ."(".htmlspecialchars($row["BANKNAME_KANA"]).")",
                                         "value"     => $row["BANKCD"]);
            $opt2[$row["BANKCD"] ."-" .$row["BRANCHCD"]]
                                 = array("label"     => $row["BRANCHCD"] ."：" .htmlspecialchars($row["BRANCHNAME"]) ."(".htmlspecialchars($row["BRANCHNAME_KANA"]).")",
                                         "value"     => $row["BANKCD"] ."-" .$row["BRANCHCD"]);
        }

        //銀行コード
        $extra = "onchange=\"chgBankcd(this)\"";
        $arg["data"]["BANKCD"] = knjCreateCombo($objForm, "BANKCD", "", $opt, $extra, 1);

        //支店コード
        $extra = "";
        $arg["data"]["BRANCHCD"] = knjCreateCombo($objForm, "BRANCHCD", "", $opt2, $extra, 1);

        //預金種目
        $opt = array();
        $result = $db->query(knjxsearch_bankQuery::nameGet("G203"));

        $opt[] = array("label" => "",
                       "value" => "");
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
               $opt[] = array("label" => $row["NAMECD2"]."：".$row["NAME1"],
                              "value" => $row["NAMECD2"]);
        }

        $extra = "";
        $arg["data"]["DEPOSIT_ITEM"] = knjCreateCombo($objForm, "DEPOSIT_ITEM", "", $opt, $extra, 1);


        //口座番号
        $extra = "onBlur=\"this.value=toInteger(this.value);\"";
        $arg["data"]["ACCOUNTNO"] = knjCreateTextBox($objForm, "", "ACCOUNTNO", 7, 7, $extra);

        //実行ボタン
        $objForm->ae( array("type"        => "button",
                            "name"        => "BTN_OK",
                            "value"       => "実行",
                            "extrahtml"   => "onclick=\"return search_submit();\"" ));

        $arg["button"]["BTN_OK"] = $objForm->ge("BTN_OK");

        //閉じるボタン
        $objForm->ae( array("type"        => "button",
                            "name"        => "BTN_END",
                            "value"       => "閉じる",
                            "extrahtml"   => "onclick=\"closeWin();\"" ));

        $arg["button"]["BTN_END"] = $objForm->ge("BTN_END");

        //hidden
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "cmd" ) );

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();

        View::toHTML($model, "knjxSearch.html", $arg);
    }
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size)
{
    $opt = array();
    $value_flg = false;
    $opt[] = array("label" => "   ", "value" => "");
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {

        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);

        if ($value == $row["VALUE"]) $value_flg = true;
    }
    $result->free();

    $value = ($value && $value_flg) ? $value : $opt[0]["value"];
    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}
?>
