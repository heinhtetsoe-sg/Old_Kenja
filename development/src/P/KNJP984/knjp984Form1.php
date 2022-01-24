<?php

require_once('for_php7.php');

class knjp984Form1
{
    function main(&$model)
    {
        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("list", "POST", "knjp984index.php", "", "edit");

        $db = Query::dbCheckOut();

        //校種コンボ
        $arg["schkind"] = "1";
        $query = knjp984Query::getSchkind($model);
        $extra = "onchange=\"return btn_submit('change');\"";
        makeCmb($objForm, $arg, $db, $query, $model->schoolKind, "SCHOOL_KIND", $extra, 1, "");

        //マスタ一覧取得
        $result = $db->query(knjp984Query::selectQuery($model));
        while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            //レコードを連想配列のまま配列$arg[data]に追加していく。 
             array_walk($row, "htmlspecialchars_array");
             $row["link"] = View::alink(REQUESTROOT."/P/KNJP984_2/knjp984_2index.php", "設定" , "target=\"_parent\" ",
                                        array("SEND_LEVY_GROUP_CD"      => $row["LEVY_GROUP_CD"],
                                              "SEND_LEVY_GROUP_NAME"    => $row["LEVY_GROUP_NAME"],
                                              "SCHOOL_KIND"             => $model->schoolKind,
                                              "SEND_YEAR"               => CTRL_YEAR));
             $arg["data"][] = $row; 
        }

        $result->free();
        Query::dbCheckIn($db);
        //hiddenを作成する
        knjCreateHidden($objForm, "cmd");

        if (!isset($model->warning) && $model->cmd == "change") {
            $arg["reload"] = "parent.right_frame.location.href='knjp984index.php?cmd=edit"
                           . "&year=".$model->year."&SCHOOL_KIND=".$model->schoolKind."';";

            if ($model->cmd == "change") {
                unset($model->levygroupcd);
            }
        }

        $arg["finish"]  = $objForm->get_finish();
        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjp984Form1.html", $arg);
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
        if ($value === $row["VALUE"]) $value_flg = true;
    }
    if ($name == "SCHOOL_KIND" && SCHOOLKIND) {
        $value = ($value && $value_flg) ? $value : SCHOOLKIND;
    } else {
        $value = ($value && $value_flg) ? $value : $opt[0]["value"];
    }
    $arg[$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);

    $result->free();
}
?>
