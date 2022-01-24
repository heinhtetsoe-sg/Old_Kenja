<?php

require_once('for_php7.php');

class knjl384jForm1
{
    public function main(&$model)
    {
        //権限チェック
        if (AUTHORITY != DEF_UPDATABLE) {
            $arg["jscript"] = "OnAuthError();";
        }

        $objForm = new form();

        //今年度及びタイトルの表示
        $arg["data"]["YEAR_SEMESTER"] = $model->examyear."年度　　ＣＳＶ出力／取込";

        //処理名コンボボックス
        $opt_shori      = array();
        $opt_shori[]    = array("label" => "更新","value" => "1");
        $opt_shori[]    = array("label" => "削除","value" => "2");

        $objForm->ae(array("type"        => "select",
                            "name"        => "SHORI_MEI",
                            "size"        => "1",
                            "extrahtml"   => "style=\"width:60px;\"",
                            "value"       => $model->field["SHORI_MEI"],
                            "options"     => $opt_shori ));

        $arg["data"]["SHORI_MEI"] = $objForm->ge("SHORI_MEI");

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

        $objForm->ae(array("type"         => "checkbox",
                            "name"      => "HEADER",
                            "value"        => "on",
                            "extrahtml"    =>$check_header ));

        $arg["data"]["HEADER"] = $objForm->ge("HEADER");

        //出力取込種別ラジオボタン
        $opt_shubetsu[0]=1;        //ヘッダ出力
        $opt_shubetsu[1]=2;        //データ取込
        $opt_shubetsu[2]=3;        //エラー出力
        $opt_shubetsu[3]=4;        //データ出力

        if ($model->field["OUTPUT"]=="") {
            $model->field["OUTPUT"] = "1";
        }

        $objForm->ae(array("type"       => "radio",
                            "name"       => "OUTPUT",
                            "value"      => $model->field["OUTPUT"],
                            "extrahtml"   => "",
                            "multiple"   => $opt_shubetsu));

        $arg["data"]["OUTPUT1"] = $objForm->ge("OUTPUT", 1);
        $arg["data"]["OUTPUT2"] = $objForm->ge("OUTPUT", 2);
        $arg["data"]["OUTPUT3"] = $objForm->ge("OUTPUT", 3);
        $arg["data"]["OUTPUT4"] = $objForm->ge("OUTPUT", 4);

        //ファイルからの取り込み
        $objForm->add_element(array("type"      => "file",
                                    "name"      => "FILE",
                                    "size"      => 1024000,
                                    "extrahtml" => "" ));

        $arg["FILE"] = $objForm->ge("FILE");

        //実行ボタン
        $objForm->ae(array("type"         => "button",
                            "name"      => "btn_exec",
                            "value"     => "実 行",
                            "extrahtml" => "onclick=\"return btn_submit('exec');\"" ));

        $arg["btn_exec"] = $objForm->ge("btn_exec");

        //終了ボタンを作成する
        $objForm->ae(array("type"         => "button",
                            "name"      => "btn_end",
                            "value"     => "終 了",
                            "extrahtml" => "onclick=\"closeWin();\"" ));

        $arg["btn_end"] = $objForm->ge("btn_end");

        //hiddenを作成する
        $objForm->ae(array("type"      => "hidden",
                            "name"      => "cmd"
                            ));

        //フォーム作成
        $arg["start"]   = $objForm->get_start("main", "POST", "knjl384jindex.php", "", "main");
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjl384jForm1.html", $arg);
    }
}
