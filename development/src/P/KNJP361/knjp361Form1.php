<?php

require_once('for_php7.php');

/********************************************************************/
/* 授業料軽減補助金                                 山城 2005/11/27 */
/*                                                                  */
/* 変更履歴                                                         */
/* ･NO001：軽減特殊コンボ/補助金を追加              山城 2005/01/21 */
/* ･NO002：CSV出力を追加                            山城 2006/02/16 */
/* ･NO003：授業料軽減補助金一覧を追加               山城 2006/02/16 */
/********************************************************************/

class knjp361Form1
{
    function main(&$model){

    $objForm = new form;
    //フォーム作成
    $arg["start"]   = $objForm->get_start("knjp361Form1", "POST", "knjp361index.php", "", "knjp361Form1");

    $opt=array();

    $arg["data"]["YEAR"] = CTRL_YEAR;

    //帳票種別 1:国 2:府県
    $opt_output = array();
    $opt_output[0] = 1;
    $opt_output[1] = 2;

    if (!$model->output) $model->output = $opt_output[0];

    $objForm->ae( array("type"         => "radio",
                        "name"         => "OUTPUT",
                        "value"        => $model->output,
                        "extrahtml"    => "onclick=\"return diable_set(this);\"",
                        "options"    => $opt_output) );

    $arg["data"]["OUTPUT1"] = $objForm->ge("OUTPUT",1);
    $arg["data"]["OUTPUT2"] = $objForm->ge("OUTPUT",2);

    //disabled
    $disabled = ($model->output == 1) ? " disabled" : "";

    //軽減特殊コード NO001
    $opt_reduc = array();
    $db = Query::dbCheckOut();

    $result = $db->query(knjp361Query::GetReduc_rare_case_cd());
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
        $opt_reduc[] = array("label" => $row["CD"].":".$row["NAME"],
                             "value" => $row["CD"]);
    }
    $opt_reduc[] = array("label" => "全て",
                         "value" => "99");

    $result->free();
    Query::dbCheckIn($db);

    if (!$model->reduc) $model->reduc = $opt_reduc[0]["value"];

    $objForm->ae( array("type"         => "select",
                        "name"        => "REDUC_RARE_CASE_CD",
                        "size"        => 1,
                        "value"        => $model->reduc,
                        "extrahtml"    => $disabled,
                        "options"    => $opt_reduc ) );

    $arg["data"]["REDUC_RARE_CASE_CD"] = $objForm->ge("REDUC_RARE_CASE_CD");

    //補助額 NO001
    $objForm->ae( array("type"         => "text",
                        "name"        => "REDUCTIONMONEY",
                        "size"        => 7,
                        "maxlength"    => 7,
                        "value"        => "",
                        "extrahtml"    => "style=\"text-align:right\" onblur=\"this.value=toInteger(this.value)\"" .$disabled) );

    $arg["data"]["REDUCTIONMONEY"] = $objForm->ge("REDUCTIONMONEY");

    //クラスデータ
    $opt_class = array();
    $db = Query::dbCheckOut();

    $result = $db->query(knjp361Query::GetClasscd($model));
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
        $opt_class[] = array("label" => $row["NAME"],
                             "value" => $row["CD"]);
    }

    $result->free();
    Query::dbCheckIn($db);

    $objForm->ae( array("type"       => "select",
                        "name"       => "CLASS_NAME",
                        "extrahtml"  => "multiple style=\"width:180px\" ondblclick=\"move1('left')\"",
                        "size"       => "20",
                        "options"    => $opt_class));

    $arg["data"]["CLASS_NAME"] = $objForm->ge("CLASS_NAME");

    //出力対象クラスリストを作成する
    $objForm->ae( array("type"       => "select",
                        "name"       => "CLASS_SELECTED",
                        "extrahtml"  => "multiple style=\"width:180px\" ondblclick=\"move1('right')\"",
                        "size"       => "20",
                        "options"    => array()));

    $arg["data"]["CLASS_SELECTED"] = $objForm->ge("CLASS_SELECTED");

    //対象選択ボタンを作成する（全部）
    $objForm->ae( array("type" => "button",
                        "name"        => "btn_rights",
                        "value"       => ">>",
                        "extrahtml"   => "style=\"height:20px;width:40px\" onclick=\"moves('right');\"" ) );

    $arg["button"]["btn_rights"] = $objForm->ge("btn_rights");


    //対象取消ボタンを作成する（全部）
    $objForm->ae( array("type" => "button",
                        "name"        => "btn_lefts",
                        "value"       => "<<",
                        "extrahtml"   => "style=\"height:20px;width:40px\" onclick=\"moves('left');\"" ) );

    $arg["button"]["btn_lefts"] = $objForm->ge("btn_lefts");

    //対象選択ボタンを作成する（一部）
    $objForm->ae( array("type" => "button",
                        "name"        => "btn_right1",
                        "value"       => "＞",
                        "extrahtml"   => "style=\"height:20px;width:40px\" onclick=\"move1('right');\"" ) );

    $arg["button"]["btn_right1"] = $objForm->ge("btn_right1");

    //対象取消ボタンを作成する（一部）
    $objForm->ae( array("type" => "button",
                        "name"        => "btn_left1",
                        "value"       => "＜",
                        "extrahtml"   => "style=\"height:20px;width:40px\" onclick=\"move1('left');\"" ) );

    $arg["button"]["btn_left1"] = $objForm->ge("btn_left1");

    Query::dbCheckIn($db);

    //印刷ボタンを作成する
    $objForm->ae( array("type" => "button",
                        "name"        => "btn_print",
                        "value"       => "プレビュー／印刷",
                        "extrahtml"   => "onclick=\"return newwin('" . SERVLET_URL . "');\"" ) );

    $arg["button"]["btn_print"] = $objForm->ge("btn_print");

    //CSV NO002
    $objForm->ae( array("type"        => "button",
                        "name"        => "btn_exec",
                        "value"           => "ＣＳＶ出力",
                        "extrahtml"    => "onclick=\"return btn_submit('exec');\"" ));
    $arg["button"]["btn_exec"] = $objForm->ge("btn_exec");

    //終了ボタンを作成する
    $objForm->ae( array("type" => "button",
                        "name"        => "btn_end",
                        "value"       => "終 了",
                        "extrahtml"   => "onclick=\"closeWin();\"" ) );

    $arg["button"]["btn_end"] = $objForm->ge("btn_end");

    //hiddenを作成する
    $objForm->ae( array("type"      => "hidden",
                        "name"      => "YEAR",
                        "value"     => CTRL_YEAR
                         ) );

    $objForm->ae( array("type"      => "hidden",
                        "name"      => "SEMESTER",
                        "value"     => CTRL_SEMESTER
                         ) );

    $objForm->ae( array("type"      => "hidden",
                        "name"      => "DBNAME",
                        "value"     => DB_DATABASE
                         ) );

    $objForm->ae( array("type"      => "hidden",
                        "name"      => "PRGID",
                        "value"     => "KNJP361"
                        ) );

    $objForm->ae( array("type"      => "hidden",
                        "name"      => "cmd"
                        ) );

    //NO002
    $objForm->ae( array("type"      => "hidden",
                        "name"      => "selectdata") );  

    $arg["finish"]  = $objForm->get_finish();
    //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
    View::toHTML($model, "knjp361Form1.html", $arg); 
    }
}
?>
