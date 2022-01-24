<?php

require_once('for_php7.php');

class knjz010cForm2
{
    function main(&$model)
    {
        //権限チェック
        if (AUTHORITY != DEF_UPDATABLE){
            $arg["jscript"] = "OnAuthError();";
        }

        $db = Query::dbCheckOut();

        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("edit", "POST", "knjz010cindex.php", "", "edit");

        //警告メッセージを表示しない場合
        if (!isset($model->warning)){
            $query = knjz010cQuery::getRow($model->year,$model->examcoursecd,$model->applicantdiv,$model->testdiv,$model->coursecd,$model->majorcd);
            $Row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        }else{
            $Row =& $model->field;
        }

    /**************/
    /**コンボ作成**/
    /**************/
        //入試制度コンボ
        $query = knjz010cQuery::selectApplicantdiv($model->year);
        $extra = "";
        makeCombo($objForm, $arg, $db, $query, $model->applicantdiv, "APPLICANTDIV", $extra, 1, "blank");

        //入試区分コンボ
        $query = knjz010cQuery::selectSchooldiv($model->year);
        $extra = "";
        makeCombo($objForm, $arg, $db, $query, $model->testdiv, "TESTDIV", $extra, 1, "blank");

        //課程学科コンボ
        $query = knjz010cQuery::selectTotalcd($model->year);
        $extra = "";
        makeCombo($objForm, $arg, $db, $query, $model->totalcd, "TOTALCD", $extra, 1, "blank");


    /****************/
    /**テキスト作成**/
    /****************/
        //コースコードテキストボックス
        $extra = "onblur=\"this.value=toInteger(this.value)\"";
        $arg["data"]["EXAMCOURSECD"] = knjCreateTextBox($objForm, $Row["EXAMCOURSECD"], "EXAMCOURSECD", 4, 4, $extra);

        //コース名テキストボックス
        $extra = "";
        $arg["data"]["EXAMCOURSE_NAME"] = knjCreateTextBox($objForm, $Row["EXAMCOURSE_NAME"], "EXAMCOURSE_NAME", 20, 30, $extra);
        
        //コース記号テキストボックス
        $extra = "onblur=\"this.value=toAlpha(this.value)\"";
        $arg["data"]["EXAMCOURSE_MARK"] = knjCreateTextBox($objForm, $Row["EXAMCOURSE_MARK"], "EXAMCOURSE_MARK", 1, 1, $extra);

        //コース定員テキストボックス
        $extra = "onblur=\"this.value=toInteger(this.value)\"";
        $arg["data"]["CAPACITY"] = knjCreateTextBox($objForm, $Row["CAPACITY"], "CAPACITY", 3, 3, $extra);


    /**************/
    /**ボタン作成**/
    /**************/
        //追加ボタン
        $extra = "onclick=\"return btn_submit('add');\"";
        $arg["button"]["btn_add"] = knjCreateBtn($objForm, 'btn_add', '追 加', $extra);

        //修正ボタン
        $extra = "onclick=\"return btn_submit('update');\"";
        $arg["button"]["btn_update"] = knjCreateBtn($objForm, 'btn_update', '更 新', $extra);

        //削除ボタン
        $extra = "onclick=\"return btn_submit('delete');\"";
        $arg["button"]["btn_del"] = knjCreateBtn($objForm, 'btn_del', '削 除', $extra);

        //取消ボタン
        $extra = "onclick=\"return btn_submit('reset');\"";
        $arg["button"]["btn_reset"] = knjCreateBtn($objForm, 'btn_reset', '取 消', $extra);

        //終了ボタン
        $extra = "onclick=\"closeWin();\"";
        $arg["button"]["btn_back"] = knjCreateBtn($objForm, 'btn_back', '終 了', $extra);


    /**************/
    /**hidden作成**/
    /**************/
        //hidden
        $query = knjz010cQuery::getChecker($model->year);//入試制度と入試区分の組合せのチェックに使用
        makeHidden($objForm, $Row["UPDATED"], $model->year, $query, $db);



        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();
        if (VARS::get("cmd") != "edit"){
            $arg["reload"]  = "parent.left_frame.location.href='knjz010cindex.php?cmd=list"
                            . "&year=" .$model->year."';";
        }
        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjz010cForm2.html", $arg);
    }
}/***********************ここまでがクラス*****************/
//#######################コンボ作成#######################
function makeCombo(&$objForm, &$arg, $db, $query, &$value, $name, $extra, $size, $blank = false) {
    $opt = array();
    $result = $db->query($query);

    if ($blank == "blank" ) {
        $opt[] = array("label" => "", "value" => "");
    }

    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array ("label" => $row["LABEL"],
                        "value" => $row["VALUE"]);
    }
    $result->free();


    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}

//#######################hidden作成#######################
function makeHidden(&$objForm, $update, $year, $query, $db) {
    knjCreateHidden($objForm, "UPDATED", $update);
    knjCreateHidden($objForm, "year", $year);
    knjCreateHidden($objForm, "cmd", "");
    if ($query) {
        $opt = array();
        $result = $db->query($query);
        $flag = false;
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            if ($flag) {
                $check .= ',';
            }
            $check .= $row["CHECKER"];
            $flag = true;
        }
        $result->free();

        knjCreateHidden($objForm, "checker", $check);
    }
}


?>
