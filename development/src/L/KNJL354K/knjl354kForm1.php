<?php
/********************************************************************/
/*  出身学校別合格者一覧／指導要録受領一覧          山城 2005/12/21 */
/*                                                                  */
/* 変更履歴                                                         */
/*  ・NO001 : 中学高校で、指示画面切替え            山城 2006/01/15 */
/********************************************************************/

class knjl354kForm1
{
    function main(&$model){

        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("knjl354kForm1", "POST", "knjl354kindex.php", "", "knjl354kForm1");
        $db = Query::dbCheckOut();

        $opt=array();

        $arg["data"]["YEAR"] = $model->ObjYear;
/*
        //試験区分を作成する
        $opt_testdiv = array();
        $testcnt = 0;

        $result = $db->query(knjl354kQuery::GetTestdiv($model));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            $opt_testdiv[] = array( "label" => $row["NAME1"],
                                    "value" => $row["NAMECD2"]);
            $testcnt++;
        }
        if ($testcnt == 0){
            $opt_testdiv[$testcnt] = array( "label" => "　　",
                                            "value" => "99");
        }

        if (!$model->testdiv) $model->testdiv = $opt_testdiv[0]["value"];

        $result->free();
        $objForm->ae( array("type"      => "select",
                            "name"      => "TESTDIV",
                            "size"      => 1,
                            "value"     => $model->testdiv,
                            "extrahtml" => "onchange =\" return btn_submit('knjl354k');\"",
                            "options"   => $opt_testdiv ) );

        $arg["data"]["TESTDIV"] = $objForm->ge("TESTDIV");
*/

        //特別理由区分
        $opt = array();
        $value_flg = false;
        $query = knjl354kQuery::getSpecialReasonDiv($model);
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


        //中高判別フラグを作成する
        $jhflg = 0;
        $row = $db->getOne(knjl354kQuery::GetJorH());
        if ($row == 1){
            $jhflg = 1;
        }else {
            $jhflg = 2;
        }
        $objForm->ae( array("type" => "hidden",
                            "name" => "JHFLG",
                            "value"=> $jhflg ) );

        //出力対象ラジオ（1:出身学校別合格者一覧,2:指導要録受領一覧）

        //NO001
        if ($jhflg == 1){
            $arg["dispj"] = $jhflg;
        }else {
            $arg["disph"] = $jhflg;
        }

        $opt_out2[0]=1;
        $opt_out2[1]=2;

        if (!$model->output) $model->output = 1;

        if ($model->output == 2) $model->output = 1;
        $objForm->ae( array("type"      => "radio",
                            "name"      => "OUTPUT",
                            "value"     => $model->output,
                            "multiple"  => $opt_out2));

        $arg["data"]["OUTPUT_1"] = $objForm->ge("OUTPUT",1);
        $arg["data"]["OUTPUT_2"] = $objForm->ge("OUTPUT",3);

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
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "YEAR",
                            "value"     => $model->ObjYear) );

        $objForm->ae( array("type"      => "hidden",
                            "name"      => "DBNAME",
                            "value"     => DB_DATABASE) );

        $objForm->ae( array("type"      => "hidden",
                            "name"      => "PRGID",
                            "value"     => "KNJL354K") );

        $objForm->ae( array("type"      => "hidden",
                            "name"      => "cmd") );

        Query::dbCheckIn($db);
        $arg["finish"]  = $objForm->get_finish();
        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjl354kForm1.html", $arg); 
    }
}
?>
