<?php

require_once('for_php7.php');

class knjl031cForm1
{
    function main(&$model)
    {
        //権限チェック
        if (AUTHORITY != DEF_UPDATABLE){
            $arg["jscript"] = "OnAuthError();";
        }

        $objForm = new form;
        //フォーム作成
        $arg["start"] = $objForm->get_start("knjl031cForm1", "POST", "knjl031cindex.php", "", "knjl031cForm1");

        //年度
        $arg["TOP"]["YEAR"] = $model->ObjYear;

        //DBに接続
        $db = Query::dbCheckOut();

        $dummy = "a";//中身は空です。エラーが出るのでただ渡すだけの変数です。


    /**************/
    /**コンボ作成**/
    /**************/
        //入試制度コンボの設定
        $query = knjl031cQuery::get_apct_div("L003",$model->ObjYear);
        $extra = " onChange=\"return btn_submit('knjl031cForm1'),AllClearList();\"";
        makeCombo($objForm, $arg, $db, $query, $model->field["APDIV"], "APDIV", $extra, 1);

        //入試区分コンボの設定
        $query = knjl031cQuery::get_testdiv_div("L004", $model->ObjYear);
        $extra = " onChange=\"return btn_submit('knjl031cForm1'),AllClearList();\"";
        makeCombo($objForm, $arg, $db, $query, $model->field["TESTDIV"], "TESTDIV", $extra, 1);

        //出力対象一覧(左側)コンボボックスを作成する
        $query = knjl031cQuery::select_subclass_div("L009",$model);
        $extra = "multiple STYLE=\"WIDTH:100%\" WIDTH=\"100%\" ondblclick=\"move('right','category_name','category_selected',1)\"";
        makeCombo($objForm, $arg, $db, $query, $dummy, "category_name", $extra, 20);

        //試験科目一覧(右側)コンボボックスを作成する
        $query = knjl031cQuery::get_subclasslist_div("L009",$model);
        $extra = "multiple STYLE=\"WIDTH:100%\" WIDTH=\"100%\" ondblclick=\"move('left','category_name','category_selected','')\"";
        makeCombo($objForm, $arg, $db, $query, $dummy, "category_selected", $extra, 20);


    /**************/
    /**ボタン作成**/
    /**************/
        //全件追加ボタンを作成する
        $extra = "style=\"height:20px;width:40px\" onclick=\"return move('sel_add_all','category_name','category_selected','');\"";
        $arg["button"]["sel_add_all"] = knjCreateBtn($objForm, 'sel_add_all', '≪', $extra);

        //一件追加ボタンを作成する
        $extra = "style=\"height:20px;width:40px\" onclick=\"return move('left','category_name','category_selected','');\"";
        $arg["button"]["sel_add"]     = knjCreateBtn($objForm, 'sel_add', '＜', $extra);

        //一件削除ボタンを作成する
        $extra = "style=\"height:20px;width:40px\" onclick=\"return move('right','category_name','category_selected',1);\"";
        $arg["button"]["sel_del"]     = knjCreateBtn($objForm, 'sel_del', '＞', $extra);

        //全件削除ボタンを作成する
        $extra = "style=\"height:20px;width:40px\" onclick=\"return move('sel_del_all','category_name','category_selected',1);\"";
        $arg["button"]["sel_del_all"] = knjCreateBtn($objForm, 'sel_del_all', '≫', $extra);

        //更新ボタンを作成する
        $extra = "onclick=\"return doSubmit();\"";
        $arg["button"]["BTN_OK"]      = knjCreateBtn($objForm, 'btn_keep', '更 新', $extra);

        //取消ボタンを作成する
        $extra = "onclick=\"return btn_submit('clear');\"";
        $arg["button"]["BTN_CLEAR"]   = knjCreateBtn($objForm, 'btn_clear', '取 消', $extra);

        //終了ボタンを作成する
        $extra = "onclick=\"closeWin();\"";
        $arg["button"]["BTN_END"]     = knjCreateBtn($objForm, 'btn_end', '終 了', $extra);


    /**************/
    /**hidden作成**/
    /**************/
        makeHidden($objForm, $model->ObjYear);

        $arg["finish"]  = $objForm->get_finish();

        //DBの切断
        Query::dbCheckIn($db);

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjl031cForm1.html", $arg); 
    }
}/***********************ここまでがクラス*****************/

//#######################コンボ作成#######################
function makeCombo(&$objForm, &$arg, $db, $query, &$value, $name, $extra, $size) {
    $opt    = array();
    $flag   = false;
    $result = $db->query($query);

    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array ("label" => $row["LABEL"],
                        "value" => $row["VALUE"]);
        $flag = ($row["VALUE"] == $value) ? true : $flag;
    }
    $result->free();

    $value = ($value && $flag) ? $value : $opt[0]["value"];

    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}

//#######################hidden作成#######################
function makeHidden(&$objForm, $year) {
    knjCreateHidden($objForm, "DBNAME"     , DB_DATABASE);
    knjCreateHidden($objForm, "selectdata" , "");
    knjCreateHidden($objForm, "cmd"        , "");
    knjCreateHidden($objForm, "YEAR"       , $year);//年度データ
}

?>
