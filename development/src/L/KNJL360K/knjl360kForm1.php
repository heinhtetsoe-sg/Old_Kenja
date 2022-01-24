<?php

require_once('for_php7.php');

/********************************************************************/
/* スカラシップ認定者名簿                           山城 2006/01/15 */
/*                                                                  */
/* 変更履歴                                                         */
/* NO001 : スカラシップCSVのCDを'00'固定にする。    山城 2006/01/20 */
/* NO002 : ソート順指定を追加。                     山城 2006/01/30 */
/* NO003 : ソート順に成績順を追加。                 山城 2006/02/04 */
/* NO004 : 出力対象を選択可能にする。               山城 2006/02/11 */
/********************************************************************/

class knjl360kForm1
{
    function main(&$model){

    $objForm = new form;
    //フォーム作成
    $arg["start"]   = $objForm->get_start("knjl360kForm1", "POST", "knjl360kindex.php", "", "knjl360kForm1");
    $db = Query::dbCheckOut();

    $opt=array();

    $arg["data"]["YEAR"] = $model->ObjYear;

    //試験区分を作成する
    $opt_testdiv = array();
    $testcnt = 0;

    $result = $db->query(knjl360kQuery::GetTestdiv($model));
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
        $opt_testdiv[] = array("label" => $row["NAME1"],
                               "value" => $row["NAMECD2"]);
        $testcnt++;
    }
    
    if (!$model->testdiv) $model->testdiv = $opt_testdiv[0]["value"];

    $result->free();
    $objForm->ae( array("type"      => "select",
                        "name"      => "TESTDIV",
                        "size"      => 1,
                        "value"     => $model->testdiv,
                        "extrahtml" => "onchange=\" return btn_submit('knjl360k');\"",
                        "options"   => $opt_testdiv ) );

    $arg["data"]["TESTDIV"] = $objForm->ge("TESTDIV");

    //特別理由区分
    $opt = array();
    $value_flg = false;
    $query = knjl360kQuery::getSpecialReasonDiv($model);
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

    //スカラシップコンボボックスを作成する
    $opt_scalashipdiv = array();

    $result = $db->query(knjl360kQuery::GetScalashipdiv($model,"1"));
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
        $opt_scalashipdiv[] = array("label" => $row["NAME1"],
                                    "value" => $row["NAMECD2"]);
    }

    if (!$model->scalashipdiv) $model->scalashipdiv = $opt_scalashipdiv[0]["value"];

    $result->free();
    $objForm->ae( array("type"      => "select",
                        "name"      => "SCALASHIPDIV",
                        "size"      => 1,
                        "value"     => $model->scalashipdiv,
                        "extrahtml" => "",
                        "options"   => $opt_scalashipdiv ) );

    $arg["data"]["SCALASHIPDIV"] = $objForm->ge("SCALASHIPDIV");

    //得点テキストボックスを作成する
    if (!$model->score) $model->score = 380;

    $objForm->ae( array("type"      => "text",
                        "name"      => "SCORE",
                        "size"      => 3,
                        "maxlength" => 3,
                        "value"     => $model->score,
                        "extrahtml" => "onblur=\"this.value=toInteger(this.value)\"" ) );

    $arg["data"]["SCORE"] = $objForm->ge("SCORE");

    //出力対象チェックボックスを作成する NO004
    $objForm->ae( array("type"      => "checkbox",
                        "name"      => "OUTPUT",
                        "extrahtml" => "checked",
                        "value"     => "on") );

    $arg["data"]["OUTPUT"] = $objForm->ge("OUTPUT");

    //中高判別フラグを作成する
    $jhflg = 0;
    $row = $db->getOne(knjl360kQuery::GetJorH($model));
    if ($row == 1){
        $jhflg = 1;
    }else {
        $jhflg = 2;
    }
    $objForm->ae( array("type" => "hidden",
                        "name" => "JHFLG",
                        "value"=> $jhflg ) );

//NO002↓
    //各種帳票ラジオ（1:受験番号順,2:かな氏名順,3:成績順）//NO003
    $opt_srt[0]=1;
    $opt_srt[1]=2;
    $opt_srt[2]=3;  //NO003

//  if (!$model->srt) $model->srt = 3;

    $objForm->ae( array("type"      => "radio",
                        "name"      => "SORT",
                        "value"     => 3,
                        "extrahtml" => "",
                        "multiple"  => $opt_srt));
    $arg["SORT1"] = $objForm->ge("SORT",1);
    $arg["SORT2"] = $objForm->ge("SORT",2);
    $arg["SORT3"] = $objForm->ge("SORT",3); //NO003

    //選択ソート一覧
    $opt_sort = $opt_left = $opt_right = array();
    $opt_sort[] = array("label" => "男女別",   "value" => "1");
    $opt_sort[] = array("label" => "専／併別", "value" => "2");
    $opt_sort[] = array("label" => "コース別", "value" => "3");

    $selectdata = ($model->selectdata != "") ? explode(",",$model->selectdata) : array();
    for ($i = 0; $i < get_count($selectdata); $i++) {
        $opt_left[]  = array("label" => $opt_sort[$selectdata[$i]-1]["label"],
                             "value" => $opt_sort[$selectdata[$i]-1]["value"]);
    }
    for ($i = 0; $i < get_count($opt_sort); $i++) {
        if (in_array($opt_sort[$i]["value"],$selectdata)) continue;
        $opt_right[] = array("label" => $opt_sort[$i]["label"],
                             "value" => $opt_sort[$i]["value"]);
    }
    //選択ソート一覧
    $objForm->ae( array("type"      => "select",
                        "name"      => "L_COURSE",
                        "size"      => "7",
                        "value"     => "left",
                        "extrahtml" => "multiple STYLE=\"WIDTH:100%\" WIDTH=\"100%\" ondblclick=\"move1('left')\"",
                        "options"   => $opt_left));
    //ソート一覧
    $objForm->ae( array("type"      => "select",
                        "name"      => "R_COURSE",
                        "size"      => "7",
                        "value"     => "left",
                        "extrahtml" => "multiple STYLE=\"WIDTH:100%\" WIDTH=\"100%\" ondblclick=\"move1('right')\"",
                        "options"   => $opt_right));
    //追加削除ボタン
    $objForm->ae( array("type"      => "button",
                        "name"      => "sel_add_all",
                        "value"     => "≪",
                        "extrahtml"   => "onclick=\"return move('sel_add_all');\"" ) );
    $objForm->ae( array("type"      => "button",
                        "name"      => "sel_add",
                        "value"     => "＜",
                        "extrahtml" => "onclick=\"return move('left');\"" ) );
    $objForm->ae( array("type"      => "button",
                        "name"      => "sel_del",
                        "value"     => "＞",
                        "extrahtml" => "onclick=\"return move('right');\"" ) );
    $objForm->ae( array("type"      => "button",
                        "name"      => "sel_del_all",
                        "value"     => "≫",
                        "extrahtml" => "onclick=\"return move('sel_del_all');\"" ) );

    $arg["main_part"] = array( "LEFT_PART"   => $objForm->ge("L_COURSE"),
                               "RIGHT_PART"  => $objForm->ge("R_COURSE"),
                               "SEL_ADD_ALL" => $objForm->ge("sel_add_all"),
                               "SEL_ADD"     => $objForm->ge("sel_add"),
                               "SEL_DEL"     => $objForm->ge("sel_del"),
                               "SEL_DEL_ALL" => $objForm->ge("sel_del_all"));

    $objForm->ae( array("type"  => "hidden",
                        "name"  => "selectdata" ) );

//NO002↑
    //CSV
    $objForm->ae( array("type"      => "button",
                        "name"      => "btn_exec",
                        "value"     => "ＣＳＶ出力",
                        "extrahtml" => "onclick=\"return btn_submit('exec');\"" ));
    $arg["button"]["btn_exec"] = $objForm->ge("btn_exec");

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
                        "value" => "KNJL360K"
                        ) );

    $objForm->ae( array("type"  => "hidden",
                        "name"  => "cmd"
                        ) );

    Query::dbCheckIn($db);
    $arg["finish"]  = $objForm->get_finish();
    //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
    View::toHTML($model, "knjl360kForm1.html", $arg); 
    }
}
?>
