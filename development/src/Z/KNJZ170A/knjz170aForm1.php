<?php

require_once('for_php7.php');

class knjz170aForm1
{
    function main(&$model){
        //権限チェック
        if (AUTHORITY != DEF_UPDATABLE){
            $arg["jscript"] = "OnAuthError();";
        }

        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("list", "POST", "knjz170aindex.php", "", "edit");

        //DB接続
        $db     = Query::dbCheckOut();

        //年度コンボ
        $query = knjz170aQuery::getYear($model);
        $extra = "onchange=\"return btn_submit('list');\"";
        makeCmb($objForm, $arg, $db, $query, $model->selectYear, "SELECT_YEAR", $extra, 1);

        //前年度からコピー
        $extra = "onclick=\"return btn_submit('copy');\"";
        $arg["button"]["copy"] = knjCreateBtn($objForm, "copy", "前年度からコピー", $extra);

        //データ表示
        $result = $db->query(knjz170aQuery::selectQuery($model));
        while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
             //レコードを連想配列のまま配列$arg[data]に追加していく。 
            array_walk($row, "htmlspecialchars_array");
            if ($row["GROUPCD"] == $model->groupcd) {
                $row["GROUPNAME"] = ($row["GROUPNAME"]) ? $row["GROUPNAME"] : "　";
                $row["GROUPNAME"] = "<a name=\"target\">{$row["GROUPNAME"]}</a><script>location.href='#target';</script>";
            }
            $arg["data"][] = $row; 
        }
        $result->free();
        Query::dbCheckIn($db);
    
        //hiddenを作成する
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "cmd"
                            ) );

        $arg["finish"]  = $objForm->get_finish();
        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjz170aForm1.html", $arg);
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
    if ($name == "SELECT_YEAR") {
        $value = ($value && $value_flg) ? $value : CTRL_YEAR;
        $arg[$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
    } else {
        $value = ($value && $value_flg) ? $value : $opt[0]["value"];
        $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
    }

    $result->free();
}

?>
