<?php

class knjl324kForm1
{
    function main(&$model){

    $objForm = new form;
    //フォーム作成
    $arg["start"]   = $objForm->get_start("knjl324kForm1", "POST", "knjl324kindex.php", "", "knjl324kForm1");
    $db = Query::dbCheckOut();

    $opt=array();

    //入試年度
    $arg["data"]["YEAR"] = $model->ObjYear;

    //特別理由区分
    $opt = array();
    $value_flg = false;
    $query = knjl324kQuery::getSpecialReasonDiv($model);
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);
        if ($model->special_reason_div == $row["VALUE"]) $value_flg = true;

        if ($row["NAMESPARE1"] == '1') {
            $special_reason_div = $row["VALUE"];
        }
    }
    $model->special_reason_div = (strlen($model->special_reason_div) && $value_flg) ? $model->special_reason_div : $special_reason_div;
    $extra = "";
    $arg["data"]["SPECIAL_REASON_DIV"] = knjCreateCombo($objForm, "SPECIAL_REASON_DIV", $model->special_reason_div, $opt, $extra, 1);

    //出力帳票ラジオ---1:理数科・国際科,2:特進(専),3:特進(併),4:進学(専),5:進学(併)
    $opt[0]=1;
    $opt[1]=2;
    $opt[2]=3;
    $opt[3]=4;
    $opt[4]=5;

    if (!$model->output) $model->output = 1;

    $objForm->ae( array("type"       => "radio",
                        "name"       => "OUTPUT",
                        "value"      => $model->output,
                        "extrahtml"  => "",
                        "multiple"   => $opt));

    $arg["data"]["OUTPUT1"] = $objForm->ge("OUTPUT",1);
    $arg["data"]["OUTPUT2"] = $objForm->ge("OUTPUT",2);
    $arg["data"]["OUTPUT3"] = $objForm->ge("OUTPUT",3);
    $arg["data"]["OUTPUT4"] = $objForm->ge("OUTPUT",4);
    $arg["data"]["OUTPUT5"] = $objForm->ge("OUTPUT",5);

    //ボーダー
    $objForm->ae( array("type"      => "text",
                        "name"      => "BORDER",
                        "size"      => 4,
                        "maxlength" => 3,
                        "extrahtml" => "onblur=\"this.value=toInteger(this.value)\"",
                        "value"     => ($model->border) ? $model->border : "420") );

    $arg["data"]["BORDER"] = $objForm->ge("BORDER");

    //合格者を除く含むラジオ---1:除く,2:含む---2005.10.28Add
    $opt_pass[0]=1;
    $opt_pass[1]=2;

    if (!$model->passflg) $model->passflg = 1;

    $objForm->ae( array("type"       => "radio",
                        "name"       => "PASSFLG",
                        "value"      => $model->passflg,
                        "extrahtml"  => "",
                        "multiple"   => $opt_pass));

    $arg["data"]["PASSFLG1"] = $objForm->ge("PASSFLG",1);
    $arg["data"]["PASSFLG2"] = $objForm->ge("PASSFLG",2);

    //中高判別フラグを作成する
    $jhflg = 0;
    $row = $db->getOne(knjl324kQuery::GetJorH());
    if ($row == 1){
        $jhflg = 1;
    }else {
        $jhflg = 2;
    }
    $objForm->ae( array("type" => "hidden",
                        "name" => "JHFLG",
                        "value"=> $jhflg ) );

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
                        "value"     => "KNJL324K"
                        ) );

    $objForm->ae( array("type"      => "hidden",
                        "name"      => "cmd"
                        ) );

    Query::dbCheckIn($db);
    $arg["finish"]  = $objForm->get_finish();
    //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
    View::toHTML($model, "knjl324kForm1.html", $arg); 
    }
}
?>
