<?php

require_once('for_php7.php');


class knjh130Form1
{
    function main(&$model){

        //権限チェック
        if (AUTHORITY != DEF_UPDATABLE){
            $arg["jscript"] = "OnAuthError();";
        }

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("knjh130Form1", "POST", "knjh130index.php", "", "knjh130Form1");

        //年度を作成する

        $arg["data"]["YEAR"] = CTRL_YEAR;

        //出力対象ラジオボタン 1:保護者 2:負担者 3:生徒
        $opt_taisyou = array(1, 2, 3);
        $label = array("OUTPUTA1" => "OUTPUT1", "OUTPUTA2" => "OUTPUT2", "OUTPUTA3" => "OUTPUT6");
        $extra = array("id=\"OUTPUT1\"", "id=\"OUTPUT2\"", "id=\"OUTPUT6\"");
        $radioArray = knjCreateRadio($objForm, "OUTPUTA", "1", $extra, $opt_taisyou, get_count($opt_taisyou));
        foreach($radioArray as $key => $val) $arg["data"][$label[$key]] = $val;

        //出力対象ラジオボタン 1:保護者 2:負担者 3:生徒
        $opt_syurui = array(1, 2, 3);
        $label = array("OUTPUTB1" => "OUTPUT3", "OUTPUTB2" => "OUTPUT4", "OUTPUTB3" => "OUTPUT5");
        $extra = array("id=\"OUTPUT3\"", "id=\"OUTPUT4\"", "id=\"OUTPUT5\"");
        $radioArray = knjCreateRadio($objForm, "OUTPUTB", "1", $extra, $opt_syurui, get_count($opt_syurui));
        foreach($radioArray as $key => $val) $arg["data"][$label[$key]] = $val;

        //checkbox
        $extra = "id=\"OUTPUTADDR\"";
        $arg["data"]["OUTPUTADDR"] = knjCreateCheckBox($objForm, "OUTPUTADDR", "1", $extra);

        //印刷ボタンを作成する
        $extra = "onclick=\"return newwin('" . SERVLET_URL . "');\"";
        $arg["button"]["btn_print"] = knjCreateBtn($objForm, "btn_print", "プレビュー／印刷", $extra);

        //終了ボタンを作成する
        $extra = "onclick=\"closeWin();\"";
        $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);

        //hidden
        knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
        knjCreateHidden($objForm, "PRGID", "KNJH130");
        knjCreateHidden($objForm, "YEAR", CTRL_YEAR);
        knjCreateHidden($objForm, "GAKKI", CTRL_SEMESTER);
        knjCreateHidden($objForm, "CTRL_DATE", CTRL_DATE);
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "useSchool_KindField", $model->Properties["useSchool_KindField"]);
        knjCreateHidden($objForm, "SCHOOLKIND", SCHOOLKIND);

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjh130Form1.html", $arg); 
    }
}
?>
