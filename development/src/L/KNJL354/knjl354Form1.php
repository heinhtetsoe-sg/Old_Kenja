<?php
/********************************************************************/
/* 補欠合格対象者一覧表                               下地 2020/10/16 */
/*                                                                  */
/* 変更履歴                                                         */
/********************************************************************/

class knjl354Form1
{
    public function main(&$model)
    {
        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("knjl354Form1", "POST", "knjl354index.php", "", "knjl354Form1");

        //DB接続
        $db = Query::dbCheckOut();

        $opt=array();

        $arg["TOP"]["YEAR"] = $model->ObjYear;

        //入試制度コンボの設定
        $opt_apdv_typ = array();
        $result = $db->query(knjl354Query::get_apct_div("L003", $model->ObjYear));

        while ($Rowtyp = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt_apdv_typ[]= array("label" => $Rowtyp["NAME1"],
                               "value" => $Rowtyp["NAMECD2"]);
        }

        $result->free();

        if (!isset($model->field["APDIV"])) {
            $model->field["APDIV"] = $opt_apdv_typ[0]["value"];
        }

        $objForm->ae(array("type"       => "select",
                        "name"       => "APDIV",
                        "size"       => "1",
                        "value"      => $model->field["APDIV"],
                        "extrahtml"  => " onChange=\"return btn_submit('knjl354');\"",
                        "options"    => $opt_apdv_typ));

        $arg["data"]["APDIV"] = $objForm->ge("APDIV");

        //指定範囲テキスト作成
        $objForm->ae(array("type"       => "text",
                        "name"       => "TESTSCR",
                        "size"       => 3,
                        "maxlength"  => 3,
                        "extrahtml"  => "STYLE=\"text-align: right\" onblur=\"this.value=toInteger(this.value)\"",
                        "value"      => isset($model->field["TESTSCR"])?$model->field["TESTSCR"]:"0"));

        $arg["data"]["TESTSCR"] = $objForm->ge("TESTSCR");

        //印刷ボタンを作成する
        $objForm->ae(array("type" => "button",
                        "name"        => "btn_print",
                        "value"       => "プレビュー／印刷",
                        "extrahtml"   => "onclick=\"return newwin('" . SERVLET_URL . "');\"" ));

        $arg["button"]["btn_print"] = $objForm->ge("btn_print");

        //終了ボタンを作成する
        $objForm->ae(array("type" => "button",
                        "name"        => "btn_end",
                        "value"       => "終 了",
                        "extrahtml"   => "onclick=\"closeWin();\"" ));

        $arg["button"]["btn_end"] = $objForm->ge("btn_end");

        //hiddenを作成する
        $objForm->ae(array("type"      => "hidden",
                        "name"      => "YEAR",
                        "value"     => $model->ObjYear
                         ));

        $objForm->ae(array("type"      => "hidden",
                        "name"      => "DBNAME",
                        "value"     => DB_DATABASE
                         ));

        $objForm->ae(array("type"      => "hidden",
                        "name"      => "PRGID",
                        "value"     => "KNJL354"
                        ));

        $objForm->ae(array("type"      => "hidden",
                        "name"      => "cmd"
                        ));

        //帳票種類（４科計）
        knjCreateHidden($objForm, "OUTPUT", 2);

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();
        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjl354Form1.html", $arg);
    }
}
