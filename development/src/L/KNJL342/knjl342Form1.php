<?php

require_once('for_php7.php');

class knjl342Form1
{
    function main(&$model){

        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("knjl342Form1", "POST", "knjl342index.php", "", "knjl342Form1");

        $opt=array();

        $arg["TOP"]["YEAR"] = $model->ObjYear;

        //入試制度コンボの設定
        $opt_apdv_typ = array();
        $db = Query::dbCheckOut();
        $result = $db->query(knjl342Query::get_apct_div("L003",$model->ObjYear));

        while($Rowtyp = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            $opt_apdv_typ[]= array("label" => $Rowtyp["NAME1"], 
                                   "value" => $Rowtyp["NAMECD2"]);
        }

        $result->free();
        Query::dbCheckIn($db);

        if (!isset($model->field["APDIV"])) {
            $model->field["APDIV"] = $opt_apdv_typ[0]["value"];
        }

        $objForm->ae( array("type"      => "select",
                            "name"      => "APDIV",
                            "size"      => "1",
                            "value"     => $model->field["APDIV"],
                            "extrahtml" => " onChange=\"return btn_submit('knjl342');\"",
                            "options"   => $opt_apdv_typ));

        $arg["data"]["APDIV"] = $objForm->ge("APDIV");

        //出力時ソート指定ラジオを作成
        $opt = array(1, 2);
        $model->field["OUTPUT"] = ($model->field["OUTPUT"] == "") ? "1" : $model->field["OUTPUT"];
        $extra = array("id=\"OUTPUT1\"", "id=\"OUTPUT2\"");
        $radioArray = knjCreateRadio($objForm, "OUTPUT", $model->field["OUTPUT"], $extra, $opt, get_count($opt));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

        //帳票種別ラジオを作成
        $opt = array(1, 2);
        $model->field["FORM_DIV"] = ($model->field["FORM_DIV"] == "") ? "1" : $model->field["FORM_DIV"];
        $extra = array("id=\"FORM_DIV1\"", "id=\"FORM_DIV2\"");
        $radioArray = knjCreateRadio($objForm, "FORM_DIV", $model->field["FORM_DIV"], $extra, $opt, get_count($opt));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

        //印刷ボタンを作成する
        $objForm->ae( array("type"      => "button",
                            "name"      => "btn_print",
                            "value"     => "プレビュー／印刷",
                            "extrahtml" => "onclick=\"return newwin('" . SERVLET_URL . "');\"" ) );

        $arg["button"]["btn_print"] = $objForm->ge("btn_print");

        //終了ボタンを作成する
        $objForm->ae( array("type"      => "button",
                            "name"      => "btn_end",
                            "value"     => "終 了",
                            "extrahtml" => "onclick=\"closeWin();\"" ) );

        $arg["button"]["btn_end"] = $objForm->ge("btn_end");

        //hiddenを作成する
        $objForm->ae( array("type"  => "hidden",
                            "name"  => "YEAR",
                            "value" => $model->ObjYear
                             ) );

        $objForm->ae( array("type"  => "hidden",
                            "name"  => "DBNAME",
                            "value" => DB_DATABASE
                             ) );

        $objForm->ae( array("type"  => "hidden",
                            "name"  => "PRGID",
                            "value" => "KNJL342"
                            ) );

        $objForm->ae( array("type"  => "hidden",
                            "name"  => "cmd"
                            ) );

        $arg["finish"]  = $objForm->get_finish();
        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjl342Form1.html", $arg); 
    }
}
?>
