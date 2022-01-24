<?php

require_once('for_php7.php');

//ビュー作成用クラス
class knjd425n_1Ziritu
{
    function main(&$model)
    {
        $objForm = new form;
        $arg = array();
        //フォーム作成
        $arg["start"]   = $objForm->get_start("subform", "POST", "knjd425n_1index.php", "", "subform");

        //DB接続
        $db = Query::dbCheckOut();

        //対象ラジオ
        //重点目標①～④
        $extra = "onChange=\"btn_submit('changeTarget')\"";
        makeTargetRadio($objForm, $arg, $model, 1, 4, $extra);
        //指導内容①～④
        $sidouNaiyou = $db->getOne(knjd425n_1Query::getSidouNaiyou($model));
        if ($sidouNaiyou === null) $extra .= " disabled";
        makeTargetRadio($objForm, $arg, $model, 5, 8, $extra);

        //リスト取得
        $query = knjd425n_1Query::getHreportGuidanceSelfrelianceMst($model);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $cnt = $db->getOne(knjd425n_1Query::getHreportGuidanceSchregSelfrelianceDat($model, $row["SELF_DIV"], $row["SELF_SEQ"]));
            $extra = $cnt ? "checked" : "";
            $row["CHECK"] = knjCreateCheckBox($objForm, "CHECK-".$row["SELF_DIV"]."-".$row["SELF_SEQ"], $row["SELF_DIV"].":".$row["SELF_SEQ"], $extra);

            $arg["list"][] = $row;
        }

        //DB切断
        Query::dbCheckIn($db);

        //登録ボタンを作成
        $extra = "onclick=\"return btn_submit('zirituInsert');\"";
        $arg["btn_insert"] = KnjCreateBtn($objForm, "btn_insert", "登 録", $extra);

        //戻るボタンを作成
        $extra = "onclick=\"parent.closeit();\"";
        $arg["btn_back"] = KnjCreateBtn($objForm, "btn_back", "戻 る", $extra);

        //hidden
        knjCreateHidden($objForm, "cmd");

        if ($model->cmd == "zirituInsertEnd") {
            $arg["parent_reload"] = "top.main_frame.right_frame.btn_submit('updateEnd');";
        }

        $arg["IFRAME"] = VIEW::setIframeJs();

        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML5($model, "knjd425n_1Ziritu.html", $arg);
    }
}
//対象ラジオ作成
function makeTargetRadio(&$objForm, &$arg, $model, $startOpt, $endOpt, $extra) {
    for ($i = $startOpt; $i <= $endOpt; $i++) {
        $opt[] = $i;
        $selfTarget = sprintf("%02d", $i);
        $arg["TARGET_NAME".$i] = $model->target[$selfTarget];
    }
    $ret = array();
    $model->field["TARGET"] = $model->field["TARGET"] ? $model->field["TARGET"] : 1;
    for ($i = $startOpt; $i <= get_count($opt) + $startOpt; $i++) {
        $objForm->ae( array("type"      => "radio",
            "name"      => "TARGET",
            "value"     => $model->field["TARGET"],
            "extrahtml" => $extra,
            "multiple"  => $opt));
        $ret["TARGET".$i] = $objForm->ge("TARGET", $i);
    }
    foreach($ret as $key => $val) $arg[$key] = $val;
}
?>
