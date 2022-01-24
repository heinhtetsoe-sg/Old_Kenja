<?php
/********************************************************************/
/* 入学試験志願者名索引                             山城 2005/07/21 */
/*                                                                  */
/* 変更履歴                                                         */
/********************************************************************/

class knjl311kForm1
{
    function main(&$model){

    //オブジェクト作成
    $objForm = new form;

    //フォーム作成
    $arg["start"]   = $objForm->get_start("knjl311kForm1", "POST", "knjl311kindex.php", "", "knjl311kForm1");
    $db = Query::dbCheckOut();

    //年度
    $arg["data"]["YEAR"] = $model->ObjYear;

    //試験区分を作成する
    $opt_testdiv = array();
    $testcnt = 0;

    $result = $db->query(knjl311kQuery::GetTestdiv($model));
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
        $opt_testdiv[] = array("label" => $row["NAME1"],
                               "value" => $row["NAMECD2"]);
        $testcnt++;
    }
    if ($testcnt == 0){
        $opt_testdiv[$testcnt] = array("label" => "　　",
                                       "value" => "99");
    }else {
        $opt_testdiv[$testcnt] = array("label" => "全て",
                                       "value" => "99");
    }

    if (!$model->testdiv) $model->testdiv = $opt_testdiv[0]["value"];

    $result->free();
    $objForm->ae( array("type"      => "select",
                        "name"      => "TESTDIV",
                        "size"      => 1,
                        "value"     => $model->testdiv,
                        "extrahtml" => "",
                        "options"   => $opt_testdiv ) );

    $arg["data"]["TESTDIV"] = $objForm->ge("TESTDIV");

    //特別理由区分
    $opt = array();
    $value_flg = false;
    $query = knjl311kQuery::getSpecialReasonDiv($model);
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

    //一覧リスト作成する
    $gojuon = "あ,い,う,え,お,か[が],き[ぎ],く[ぐ],け[げ],こ[ご],さ[ざ],し[じ],す[ず],せ[ぜ],そ[ぞ],た[だ],ち[ぢ],つ[づ],て[で],と[ど],な,に,ぬ,ね,の,は[ば][ぱ],ひ[び][ぴ],ふ[ぶ][ぷ],へ[べ][ぺ],ほ[ぼ][ぽ],ま,み,む,め,も,や,ゆ,よ,ら,り,る,れ,ろ,わ,を,ん";
    $opt_data = explode(",",$gojuon);
    foreach ($opt_data as $key=>$val){
        $opt_gojuon[$key] = array('label' => $val,
                                  'value' => $key);
    }
    $objForm->ae( array("type"       => "select",
                        "name"       => "DATA_NAME",
                        "extrahtml"  => "multiple style=\"width=150px\" width=\"150px\" ondblclick=\"move1('left')\"",
                        "size"       => "15",
                        "options"    => isset($opt_gojuon)?$opt_gojuon:array()));

    $arg["data"]["DATA_NAME"] = $objForm->ge("DATA_NAME");

    //出力対象クラスリストを作成する
    $objForm->ae( array("type"       => "select",
                        "name"       => "DATA_SELECTED",
                        "extrahtml"  => "multiple style=\"width=150px\" width=\"150px\" ondblclick=\"move1('right')\"",
                        "size"       => "15",
                        "options"    => array()));

    $arg["data"]["DATA_SELECTED"] = $objForm->ge("DATA_SELECTED");

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

    //中高判別フラグを作成する
    $jhflg = 0;
    $row = $db->getOne(knjl311kQuery::GetJorH());
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
                        "value"     => "KNJL311K"
                        ) );

    $objForm->ae( array("type"      => "hidden",
                        "name"      => "cmd"
                        ) );

    Query::dbCheckIn($db);
    $arg["finish"]  = $objForm->get_finish();
    //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
    View::toHTML($model, "knjl311kForm1.html", $arg);
    }
}
?>
