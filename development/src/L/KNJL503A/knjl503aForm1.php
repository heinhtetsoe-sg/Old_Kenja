<?php

require_once('for_php7.php');

class knjl503aForm1
{
    function main(&$model)
    {
        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("list", "POST", "knjl503aindex.php", "", "edit");

        $db = Query::dbCheckOut();

        if ($model->isChgPwdUse) {
            $arg["chgPwd"] = 1;
        }

        //リンク先設定
        $link = REQUESTROOT."/Z/KNJL503A_2/knjl503a_2index.php";

        //校種
        $query = knjl503aQuery::getFinschoolType();
        $extra = "onChange=\"return btn_submit('list');\"";
        makeCmb($objForm, $arg, $db, $query, $model->finschoolType, "FINSCHOOL_TYPE", $extra, 1, "ALL");

        //コピーボタン
        $extra = "onclick=\"return btn_submit('copy');\"";
        $arg["button"]["copy"] = knjCreateBtn($objForm, "btn_copy", "前年度からデータをコピー", $extra);

        $arg["year"]["VAL"] = $model->examyear;

        //リスト内データ取得
        $query = knjl503aQuery::getList($model);

        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            //レコードを連想配列のまま配列$arg[data]に追加していく。
            array_walk($row, "htmlspecialchars_array");

            $row["FINSCHOOLCD"] = View::alink("knjl503aindex.php", $row["FINSCHOOLCD"], "target=\"right_frame\"",
                                               array("cmd"     => "edit",
                                                     "ENTEXAM_SCHOOLCD" => $row["ENTEXAM_SCHOOLCD"],
                                                     "FINSCHOOL_TYPE"   => $model->finschoolType,
                                               ));
            $arg["data"][] = $row;
        }

        //hiddenを作成する
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "SEND_selectSchoolKind", $model->selectSchoolKind);

        if ($model->cmd == "change_kind") {
            $arg["jscript"] = "window.open('knjl503aindex.php?cmd=edit','right_frame');";
        }

        if (VARS::get("cmd") != "edit") {
            $arg["reload"]  = "window.open('knjl503aindex.php?cmd=list','left_frame');";
        }

        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();
        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjl503aForm1.html", $arg); 
    }
}
//makeCmb
function makeCmb(&$objForm, &$arg, $db, $query, &$value, $name, $extra, $size, $blank = "")
{
    $opt = array();
    if ($blank == "ALL") {
        $opt[] = array("label" => "全て", "value" => "ALL");
    }
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
            'value' => $row["VALUE"]);
        if ($value == $row["VALUE"]) $value_flg = true;
    }
    $value = ($value && $value_flg) ? $value : $opt[0]["value"];
    $arg[$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);

    $result->free();
}
?>
