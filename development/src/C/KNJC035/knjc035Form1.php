<?php

require_once('for_php7.php');

class knjc035Form1
{
    public function main(&$model)
    {
        //権限チェック
        if (AUTHORITY != DEF_UPDATABLE) {
            $arg["jscript"] = "OnAuthError();";
        }

        $objForm = new form();

        //DB接続
        $db = Query::dbCheckOut();

        //今年度・今学期名及びタイトルの表示
        $arg["data"]["YEAR_SEMESTER"] = CTRL_YEAR."年度　" .CTRL_SEMESTERNAME ."　ＣＳＶ出力／取込";

        //処理名コンボボックス
        $opt_shori      = array();
        $opt_shori[]    = array("label" => "更新","value" => "1");

        $objForm->ae(array("type"        => "select",
                            "name"        => "SHORI_MEI",
                            "size"        => "1",
                            "extrahtml"   => "style=\"width:60px;\"",
                            "value"       => $model->field["SHORI_MEI"],
                            "options"     => $opt_shori ));

        $arg["data"]["SHORI_MEI"] = $objForm->ge("SHORI_MEI");

        //勤怠コンボ
        $query = knjc035Query::getDicd();
        $extra = "";
        makeCmb($objForm, $arg, $db, $query, "KINTAI", $model->field["KINTAI"], $extra, 1, "");

        //ヘッダ有チェックボックス
        if ($model->field["HEADER"] == "on") {
            $check_header = "checked";
        } else {
            if ($model->cmd == "") {
                $check_header = "checked";
            } else {
                $check_header = "";
            }
        }

        $objForm->ae(array("type"      => "checkbox",
                            "name"      => "HEADER",
                            "value"     => "on",
                            "extrahtml" => $check_header." id=\"HEADER\"" ));

        $arg["data"]["HEADER"] = $objForm->ge("HEADER");

        //出力取込種別ラジオボタン 1:ヘッダ出力 2:データ取込 3:エラー出力
        $opt_shubetsu = array(1, 2, 3);
        $model->field["OUTPUT"] = ($model->field["OUTPUT"] == "") ? "1" : $model->field["OUTPUT"];
        $extra = array("id=\"OUTPUT1\"", "id=\"OUTPUT2\"", "id=\"OUTPUT3\"");
        $radioArray = knjCreateRadio($objForm, "OUTPUT", $model->field["OUTPUT"], $extra, $opt_shubetsu, get_count($opt_shubetsu));
        foreach ($radioArray as $key => $val) {
            $arg["data"][$key] = $val;
        }

        //ファイルからの取り込み
        $objForm->add_element(array("type"      => "file",
                                    "name"      => "FILE",
                                    "size"      => 1024000,
                                    "extrahtml" => "" ));

        $arg["FILE"] = $objForm->ge("FILE");

        //実行ボタン
        $objForm->ae(array("type"      => "button",
                            "name"      => "btn_exec",
                            "value"     => "実 行",
                            "extrahtml" => "onclick=\"return btn_submit('exec');\"" ));

        $arg["btn_exec"] = $objForm->ge("btn_exec");

        //終了ボタンを作成する
        $objForm->ae(array("type"      => "button",
                            "name"      => "btn_end",
                            "value"     => "終 了",
                            "extrahtml" => "onclick=\"closeWin();\"" ));

        $arg["btn_end"] = $objForm->ge("btn_end");

        //hiddenを作成する
        $objForm->ae(array("type"      => "hidden",
                            "name"      => "cmd"
                            ));

        //DB切断
        Query::dbCheckIn($db);

        //フォーム作成
        $arg["start"]   = $objForm->get_start("main", "POST", "knjc035index.php", "", "main");
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjc035Form1.html", $arg);
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
    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);

    $result->free();
}
