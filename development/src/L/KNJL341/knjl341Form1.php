<?php

require_once('for_php7.php');

/********************************************************************/
/* 4科目成績一覧表                                  山城 2004/12/15 */
/*                                                                  */
/* 変更履歴                                                         */
/* ･NO001：抽出条件に年度を追加                     山城 2005/01/15 */
/********************************************************************/

class knjl341Form1
{
    function main(&$model){

        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("knjl341Form1", "POST", "knjl341index.php", "", "knjl341Form1");

        $db = Query::dbCheckOut();

        $opt=array();

        $arg["TOP"]["YEAR"] = $model->ObjYear;

        //入試制度コンボの設定
        $opt_apdv_typ = array();
        $result = $db->query(knjl341Query::get_apct_div("L003",$model->ObjYear));   /* NO001 */

        while($Rowtyp = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            $opt_apdv_typ[]= array("label" => $Rowtyp["NAME1"], 
                                   "value" => $Rowtyp["NAMECD2"]);
        }

        $result->free();

        if (!isset($model->field["APDIV"])) {
            $model->field["APDIV"] = $opt_apdv_typ[0]["value"];
        }

        $objForm->ae( array("type"       => "select",
                            "name"       => "APDIV",
                            "size"       => "1",
                            "value"      => $model->field["APDIV"],
                            "extrahtml"  => " onChange=\"return btn_submit('knjl341');\"",
                            "options"    => $opt_apdv_typ));

        $arg["data"]["APDIV"] = $objForm->ge("APDIV");

        //入試区分・・・特別アップ合格通知書のみ指定可能
        $opt    = array();
        $result = $db->query(knjl341Query::getTestdivMst($model->ObjYear));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            if ($row["NAMESPARE2"] != "1") continue;
            $opt[] = array("label" => $row["NAMECD2"]."：".$row["NAME1"], "value" => $row["NAMECD2"]);
        }
        $result->free();
        $opt[] = array("label" => "9:全体", "value" => "9");
        $model->field["TESTDIV"] = (!strlen($model->field["TESTDIV"])) ? $opt[0]["value"] : $model->field["TESTDIV"];
        $extra = "";
        $arg["data"]["TESTDIV"] = knjCreateCombo($objForm, "TESTDIV", $model->field["TESTDIV"], $opt, $extra, 1);

        //通知日付
        $value = isset($model->field["NOTICEDAY"])?$model->field["NOTICEDAY"]:str_replace("-","/",$model->control["学籍処理日"]);
        $arg["el"]["NOTICEDAY"] = View::popUpCalendar($objForm, "NOTICEDAY", $value);

        //帳票種類ラジオボタン作成
        $disp_onoff = "";
        $opt_opt[] = array();
        $opt_opt[] = 1;
        $opt_opt[] = 2;
        $opt_opt[] = 3;
        $opt_opt[] = 4;
        if (!isset($model->field["OUTPUT"])){
            $model->field["OUTPUT"] = 1;
        }
        $objForm->ae( array("type"       => "radio",
                            "name"       => "OUTPUT",
                            "value"      => $model->field["OUTPUT"],
                            "extrahtml"  => " onclick=\"return btn_submit('knjl341');\"",
                            "multiple"   => $opt_opt));

        $arg["data"]["OUTPUT1"] = $objForm->ge("OUTPUT",1);
        $arg["data"]["OUTPUT2"] = $objForm->ge("OUTPUT",2);
        $arg["data"]["OUTPUT3"] = $objForm->ge("OUTPUT",3);
        $arg["data"]["OUTPUT4"] = $objForm->ge("OUTPUT",4);

        //帳票出力Aラジオボタン作成
        $opt_opta[] = array();
        $opt_opta[] = 1;
        $opt_opta[] = 2;
        if ($model->field["OUTPUT"] == 1){
            $disp_onoff = "";
        } else {
            $disp_onoff = "disabled";
        }
        if (!isset($model->field["OUTPUTA"])){
            $model->field["OUTPUTA"] = 1;
        }
        $objForm->ae( array("type"       => "radio",
                            "name"       => "OUTPUTA",
                            "value"      => $model->field["OUTPUTA"],
                            "extrahtml"  => "$disp_onoff onclick=\"return btn_submit('knjl341');\"",
                            "multiple"   => $opt_opta));

        $arg["data"]["OUTPUTA1"] = $objForm->ge("OUTPUTA",1);
        $arg["data"]["OUTPUTA2"] = $objForm->ge("OUTPUTA",2);

        //disabled設定
        if (($model->field["OUTPUTA"] == 2)&&($disp_onoff == "")){
            $disp_onoff = "";
        } else {
            $disp_onoff = "disabled";
        }

        //受験番号A
        if (!isset($model->field["EXAMNOA"])){
            $model->field["EXAMNOA"] = "";
        }
        $objForm->ae( array("type"       => "text",
                            "name"       => "EXAMNOA",
                            "size"       => 5,
                            "maxlength"  => 5,
                            "value"      => $model->field["EXAMNOA"],
                            "extrahtml"  => "$disp_onoff onblur=\"this.value=toInteger(this.value)\""));

        $arg["data"]["EXAMNOA"] = $objForm->ge("EXAMNOA");

        //帳票出力Bラジオボタン作成
        $opt_optb[] = array();
        $opt_optb[] = 1;
        $opt_optb[] = 2;
        if ($model->field["OUTPUT"] == 2){
            $disp_onoff = "";
        } else {
            $disp_onoff = "disabled";
        }
        if (!isset($model->field["OUTPUTB"])){
            $model->field["OUTPUTB"] = 1;
        }
        $objForm->ae( array("type"       => "radio",
                            "name"       => "OUTPUTB",
                            "value"      => $model->field["OUTPUTB"],
                            "extrahtml"  => "$disp_onoff onclick=\"return btn_submit('knjl341');\"",
                            "multiple"   => $opt_optb));

        $arg["data"]["OUTPUTB1"] = $objForm->ge("OUTPUTB",1);
        $arg["data"]["OUTPUTB2"] = $objForm->ge("OUTPUTB",2);

        //disabled設定
        if (($model->field["OUTPUTB"] == 2)&&($disp_onoff == "")){
            $disp_onoff = "";
        } else {
            $disp_onoff = "disabled";
        }
        //受験番号B
        if (!isset($model->field["EXAMNOB"])){
            $model->field["EXAMNOB"] = "";
        }
        $objForm->ae( array("type"       => "text",
                            "name"       => "EXAMNOB",
                            "size"       => 5,
                            "maxlength"  => 5,
                            "value"      => $model->field["EXAMNOB"],
                            "extrahtml"  => "$disp_onoff onblur=\"this.value=toInteger(this.value)\""));

        $arg["data"]["EXAMNOB"] = $objForm->ge("EXAMNOB");

        //帳票出力Cラジオボタン作成
        $opt_optc[] = array();
        $opt_optc[] = 1;
        $opt_optc[] = 2;
        if ($model->field["OUTPUT"] == 3){
            $disp_onoff = "";
        } else {
            $disp_onoff = "disabled";
        }
        if (!isset($model->field["OUTPUTC"])){
            $model->field["OUTPUTC"] = 1;
        }
        $objForm->ae( array("type"       => "radio",
                            "name"       => "OUTPUTC",
                            "value"      => $model->field["OUTPUTC"],
                            "extrahtml"  => "$disp_onoff onclick=\"return btn_submit('knjl341');\"",
                            "multiple"   => $opt_optc));

        $arg["data"]["OUTPUTC1"] = $objForm->ge("OUTPUTC",1);
        $arg["data"]["OUTPUTC2"] = $objForm->ge("OUTPUTC",2);

        //disabled設定
        if (($model->field["OUTPUTC"] == 2)&&($disp_onoff == "")){
            $disp_onoff = "";
        } else {
            $disp_onoff = "disabled";
        }
        //受験番号C
        if (!isset($model->field["EXAMNOC"])){
            $model->field["EXAMNOC"] = "";
        }
        $objForm->ae( array("type"       => "text",
                            "name"       => "EXAMNOC",
                            "size"       => 5,
                            "maxlength"  => 5,
                            "value"      => $model->field["EXAMNOC"],
                            "extrahtml"  => "$disp_onoff onblur=\"this.value=toInteger(this.value)\""));

        $arg["data"]["EXAMNOC"] = $objForm->ge("EXAMNOC");


        //帳票出力Dラジオボタン作成
        if ($model->field["OUTPUT"] == 4){
            $disp_onoff = "";
        } else {
            $disp_onoff = "disabled";
        }
        if (!isset($model->field["OUTPUTD"])){
            $model->field["OUTPUTD"] = 1;
        }
        $opt = array(1, 2);
        $extra = array("id=\"OUTPUTD1\" {$disp_onoff} onclick=\"return btn_submit('knjl341');\"", "id=\"OUTPUTD2\" {$disp_onoff} onclick=\"return btn_submit('knjl341');\"");
        $radioArray = knjCreateRadio($objForm, "OUTPUTD", $model->field["OUTPUTD"], $extra, $opt, get_count($opt));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

        //disabled設定
        if (($model->field["OUTPUTD"] == 2) && ($disp_onoff == "")){
            $disp_onoff = "";
        } else {
            $disp_onoff = "disabled";
        }

        //受験番号D
        if (!isset($model->field["EXAMNOD"])){
            $model->field["EXAMNOD"] = "";
        }
        $extra = "$disp_onoff onblur=\"this.value=toInteger(this.value)\"";
        $arg["data"]["EXAMNOD"] = knjCreateTextBox($objForm, $model->field["EXAMNOD"], "EXAMNOD", 5, 5, $extra);

        //印刷ボタンを作成する
        $objForm->ae( array("type" => "button",
                            "name"        => "btn_print",
                            "value"       => "プレビュー／印刷",
                            "extrahtml"   => "onclick=\"return newwin('" . SERVLET_URL . "');\"" ) );

        $arg["button"]["btn_print"] = $objForm->ge("btn_print");

        //終了ボタンを作成する
        $objForm->ae( array("type" => "button",
                            "name"        => "btn_end",
                            "value"       => "終 了",
                            "extrahtml"   => "onclick=\"closeWin();\"" ) );

        $arg["button"]["btn_end"] = $objForm->ge("btn_end");

        //hiddenを作成する
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "YEAR",
                            "value"     => $model->ObjYear
                            ) );

        $objForm->ae( array("type"      => "hidden",
                            "name"      => "DBNAME",
                            "value"     => DB_DATABASE
                            ) );

        $objForm->ae( array("type"      => "hidden",
                            "name"      => "PRGID",
                            "value"     => "KNJL341"
                            ) );

        $objForm->ae( array("type"      => "hidden",
                            "name"      => "cmd"
                            ) );
        //以下のhiddenは、印刷処理時の判定用
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "OUT",
                            "value"     => $model->field["OUTPUT"]
                            ) );

        $objForm->ae( array("type"      => "hidden",
                            "name"      => "OUTA",
                            "value"     => $model->field["OUTPUTA"]
                            ) );

        $objForm->ae( array("type"      => "hidden",
                            "name"      => "OUTB",
                            "value"     => $model->field["OUTPUTB"]
                            ) );

        $objForm->ae( array("type"      => "hidden",
                            "name"      => "OUTC",
                            "value"     => $model->field["OUTPUTC"]
                            ) );

        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();
        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjl341Form1.html", $arg); 
    }
}
?>
