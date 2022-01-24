<?php

require_once('for_php7.php');

/********************************************************************/
/* 通信制時間割講座データCSV取込み                  山城 2005/05/24 */
/*                                                                  */
/* 変更履歴                                                         */
/* ･NO001：変更内容                                 name yyyy/mm/dd */
/********************************************************************/
class knjx130Form1
{
    public function main(&$model)
    {
        //権限チェック
        if (AUTHORITY != DEF_UPDATABLE) {
            $arg["jscript"] = "OnAuthError();";
        }

        $objForm = new form();

        //今年度・今学期名及びタイトルの表示
        $arg["data"]["YEAR_SEMESTER"] = CTRL_YEAR."年度　" .CTRL_SEMESTERNAME ."　ＣＳＶ出力／取込";

        //処理名コンボボックス
        $opt_shori  	= array();
        $opt_shori[]    = array("label" => "更新","value" => "1");
        $opt_shori[]    = array("label" => "削除","value" => "2");

        $objForm->ae(array("type"        => "select",
                            "name"        => "SHORI_MEI",
                            "size"        => "1",
                            "extrahtml"   => "style=\"width:60px;\"",
                            "value"       => $model->field["SHORI_MEI"],
                            "options"     => $opt_shori ));

        $arg["data"]["SHORI_MEI"] = $objForm->ge("SHORI_MEI");

        $db         = Query::dbCheckOut();
        $optnull    = array("label" => "(全て出力)","value" => "");   //初期値：空白項目

        //実施日付一覧コンボボックス
        $result     = $db->query(knjx130query::getSelectFieldSQL());
        $opt_date  = array();
        $opt_date[] = $optnull;
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt_date[] = array("label" => str_replace("-", "/", $row["EXECUTEDATE"]),
                                    "value" => $row["EXECUTEDATE"]);
        }

        //講座一覧コンボボックス
        $result     = $db->query(knjx130query::getSelectFieldSQL2($model));
        $opt_chaircd  = array();
        $opt_chaircd[] = $optnull;
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt_chaircd[] = array("label" => $row["CHAIRCD"]." ".$row["CHAIRNAME"],
                                    "value" => $row["CHAIRCD"]);
        }

        $result->free();
        Query::dbCheckIn($db);

        //実施日付一覧コンボボックス
        $objForm->ae(array("type"        => "select",
                            "name"        => "EXECUTEDATE",
                            "size"        => "1",
                            "extrahtml"   => "onchange=\"btn_submit('');\"",
                            "value"       => $model->field["EXECUTEDATE"],
                            "options"     => $opt_date ));

        $arg["data"]["EXECUTEDATE"] = $objForm->ge("EXECUTEDATE");

        //講座一覧コンボボックス
        $objForm->ae(array("type"        => "select",
                            "name"        => "CHAIRCD",
                            "size"        => "1",
                            "extrahtml"   => "",
                            "value"       => $model->field["CHAIRCD"],
                            "options"     => $opt_chaircd ));

        $arg["data"]["CHAIRCD"] = $objForm->ge("CHAIRCD");

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

        $objForm->ae(array("type"       => "checkbox",
                            "name"      => "HEADER",
                            "value"     => "on",
                            "extrahtml" =>$check_header ));

        $arg["data"]["HEADER"] = $objForm->ge("HEADER");

        //出力取込種別ラジオボタン
        $opt_shubetsu[0]=1; //ヘッダ出力
        $opt_shubetsu[1]=2; //データ取込
        $opt_shubetsu[2]=3; //エラー出力
        $opt_shubetsu[3]=4; //データ出力

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
        $objForm->ae(array("type"       => "button",
                            "name"      => "btn_exec",
                            "value"     => "実 行",
                            "extrahtml" => "onclick=\"return btn_submit('exec');\"" ));

        $arg["btn_exec"] = $objForm->ge("btn_exec");

        //終了ボタンを作成する
        $objForm->ae(array("type"       => "button",
                            "name"      => "btn_end",
                            "value"     => "終 了",
                            "extrahtml" => "onclick=\"closeWin();\"" ));

        $arg["btn_end"] = $objForm->ge("btn_end");

        //hiddenを作成する
        $objForm->ae(array("type"      => "hidden",
                            "name"      => "cmd"
                            ));

        //フォーム作成
        $arg["start"]   = $objForm->get_start("main", "POST", "knjx130index.php", "", "main");
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjx130Form1.html", $arg);
    }
}
