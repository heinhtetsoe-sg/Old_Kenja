<?php

require_once('for_php7.php');


class knjm380mForm2
{
    public function main(&$model)
    {
        $objForm = new form();
        //フォーム作成
        $arg["start"]   = $objForm->get_start("edit", "POST", "knjm380mindex.php", "", "edit");

        $db = Query::dbCheckOut();

        if (isset($model->warning) || !$model->chaircd) {
            $row = $model->field;
        } else {
            $query = knjm380mQuery::selectQuery($model, '');
            $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        }
        $arg["data"] = $row;
        if (is_array($row)) {
            $model->AddorUp = "up";
        } else {
            $model->AddorUp = "add";
        }
        $arg["NAME"] = $model->chaircd_show;
        $arg["CHAIRCD"] = $model->chaircd;
        $arg["data"]["KAMOKU"] = $model->chaircd_show;

        if (isset($model->warning) || !$model->chaircd) {
            $row["SCH_SEQ_ALL"] = $row["SCHCNT"];
        }
        $objForm->ae(array("type"        => "text",
                            "name"        => "SCHCNT",
                            "size"        => 2,
                            "maxlength"   => 2,
                            "extrahtml"   => "onblur=\"this.value=toInteger(this.value);check(this)\"",
                            "value"       => $row["SCH_SEQ_ALL"]));

        $arg["data"]["SCHCNT"] = $objForm->ge("SCHCNT");

        if (isset($model->warning) || !$model->chaircd) {
            $row["SCH_SEQ_MIN"] = $row["CHECKCNT"];
        }
        $objForm->ae(array("type"        => "text",
                            "name"        => "CHECKCNT",
                            "size"        => 2,
                            "maxlength"   => 2,
                            "extrahtml"   => "onblur=\"this.value=toInteger(this.value);check(this)\"",
                            "value"       => $row["SCH_SEQ_MIN"]));

        $arg["data"]["CHECKCNT"] = $objForm->ge("CHECKCNT");

        Query::dbCheckIn($db);

        $arg["CHAIRCD"]  = $model->chaircd;

        //修正ボタンを作成する
        $objForm->ae(array("type" => "button",
                            "name"        => "btn_update",
                            "value"       => "登 録",
                            "extrahtml"   => " onclick=\"return btn_submit('update');\"" ));

        $arg["button"]["btn_update"] = $objForm->ge("btn_update");

        //クリアボタンを作成する
        $objForm->ae(array("type" => "button",
                            "name"        => "btn_reset",
                            "value"       => "取 消",
                            "extrahtml"   => " onclick=\"return btn_submit('reset');\""));

        $arg["button"]["btn_reset"] = $objForm->ge("btn_reset");

        //終了ボタンを作成する
        $objForm->ae(array("type"        => "button",
                            "name"        => "btn_end",
                            "value"       => "終 了",
                            "extrahtml"   => "onclick=\"closeWin();\"" ));

        $arg["button"]["btn_end"] = $objForm->ge("btn_end");

        //CSVファイルアップロードコントロール
        makeCsv($objForm, $arg, $model);

        //hiddenを作成する
        $objForm->ae(array("type"      => "hidden",
                            "name"      => "cmd"
                            ));

        $arg["finish"]  = $objForm->get_finish();

        if (VARS::get("cmd") != "edit" && $model->cmd != "reset" && !isset($model->warning)) {
            $arg["reload"] = "parent.left_frame.btn_submit('list');";
        }

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjm380mForm2.html", $arg);
    }
}
//CSV作成
function makeCsv(&$objForm, &$arg, $model)
{
    //出力取込種別ラジオボタン 1:取込 2:書出 3:エラー書出 4:見本
    $opt_shubetsu = array(1, 2, 3, 4);
    $model->field["OUTPUT"] = ($model->field["OUTPUT"]) ? $model->field["OUTPUT"] : "1";
    $click = " onclick=\"return changeRadio(this);\"";
    $extra = array("id=\"OUTPUT1\"".$click, "id=\"OUTPUT2\"".$click, "id=\"OUTPUT3\"".$click, "id=\"OUTPUT4\"".$click);
    $radioArray = knjCreateRadio($objForm, "OUTPUT", $model->field["OUTPUT"], $extra, $opt_shubetsu, get_count($opt_shubetsu));
    foreach ($radioArray as $key => $val) {
        $arg["data"][$key] = $val;
    }

    //ファイルからの取り込み
    $extra = ($model->field["OUTPUT"] == "1") ? "" : "disabled";
    $arg["FILE"] = knjCreateFile($objForm, "FILE", $extra, 1024000);

    //ヘッダ有チェックボックス
    $check_header = "checked id=\"HEADER\"";
    $arg["data"]["HEADER"] = knjCreateCheckBox($objForm, "HEADER", "on", $check_header, "");

    //実行ボタン
    $extra = "onclick=\"return btn_submit('exec');\"";
    $arg["btn_exec"] = knjCreateBtn($objForm, "btn_exec", "実 行", $extra);
}
