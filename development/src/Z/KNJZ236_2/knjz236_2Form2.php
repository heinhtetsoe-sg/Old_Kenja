<?php

require_once('for_php7.php');

class knjz236_2Form2 {

    function main(&$model) {

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("sel", "POST", "knjz236_2index.php", "", "sel");

        //DB接続
        $db = Query::dbCheckOut();

        //タイトル設定
        makeTitle($arg, $db, $model);

        //学科リストToリスト作成
        makeMajorList($objForm, $arg, $db, $model);

        //ボタン作成
        makeButton($objForm, $arg);

        //hidden作成
        makeHidden($objForm);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //更新できたら左のリストを再読込
        if (isset($model->message)) {
            $arg["reload"] = "window.open('knjz236_2index.php?cmd=list&init=1', 'left_frame')";
        }

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjz236_2Form2.html", $arg); 
    }
}

/***************************************** 以下関数 *********************************************/

//タイトル設定
function makeTitle(&$arg, $db, $model)
{
    $ChosenData1 = $ChosenData2 = array();
    $ChosenData1 = $db->getRow(knjz236_2Query::getChosenData($model, $model->sub_class, $model->sub_schoolkind, $model->sub_curriculumcd, $model->sub_subclass), DB_FETCHMODE_ASSOC);
    $ChosenData2 = $db->getRow(knjz236_2Query::getChosenData($model, $model->att_class, $model->att_schoolkind, $model->att_curriculumcd, $model->att_subclass), DB_FETCHMODE_ASSOC);

    $arg["info"] = array("TOP_SUB"      => "代替先科目 : ".$ChosenData1["VALUE"]."&nbsp;&nbsp;".$ChosenData1["SUBCLASSNAME"],
                         "TOP_ATT"      => "代替元科目 : ".$ChosenData2["VALUE"]."&nbsp;&nbsp;".$ChosenData2["SUBCLASSNAME"],
                         "LEFT_LIST"    => "対<br>象<br>学<br>科<br>一<br>覧",
                         "RIGHT_LIST"   => "学<br>科<br>一<br>覧" );
    return;
}

//学科リストToリスト作成
function makeMajorList(&$objForm, &$arg, $db, &$model)
{
    $opt_left = $opt_right = array();
    if ($model->sub_subclass && $model->att_subclass) {
        //対象学科一覧
        $query = knjz236_2Query::selectQuery2($model, $model->sub_subclass, $model->att_subclass);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt_left[] = array("label" => "{$row["GRADE"]}-{$row["COURSECD"]}{$row["MAJORCD"]}{$row["COURSECODE"]}　{$row["GRADE_NAME1"]}{$row["MAJORNAME"]}／{$row["COURSECODENAME"]}", 
                                "value" => "{$row["GRADE"]}_{$row["COURSECD"]}_{$row["MAJORCD"]}_{$row["COURSECODE"]}");
        }

        //学科一覧
        $query = knjz236_2Query::selectQuery2($model, $model->sub_subclass, $model->att_subclass, "NOT");
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt_right[] = array("label" => "{$row["GRADE"]}-{$row["COURSECD"]}{$row["MAJORCD"]}{$row["COURSECODE"]}　{$row["GRADE_NAME1"]}{$row["MAJORNAME"]}／{$row["COURSECODENAME"]}", 
                                 "value" => "{$row["GRADE"]}_{$row["COURSECD"]}_{$row["MAJORCD"]}_{$row["COURSECODE"]}");
        }
        $result->free();
    }

    //対象学科一覧
    $extra = "multiple STYLE=\"WIDTH:100%\" WIDTH=\"100%\" ondblclick=\"move('right')\"";
    $arg["main_part"]["LEFT_PART"]   = knjCreateCombo($objForm, "left_select", "right", $opt_left, $extra, 17);
    //学科一覧
    $extra = "multiple STYLE=\"WIDTH:100%\" WIDTH=\"100%\" ondblclick=\"move('left')\"";
    $arg["main_part"]["RIGHT_PART"]  = knjCreateCombo($objForm, "right_select", "left", $opt_right, $extra, 17);

    //各種ボタン
    $arg["main_part"]["SEL_ADD_ALL"] = knjCreateBtn($objForm, "sel_add_all", "≪", "style=\"height:20px;width:30px\" onclick=\"return moves('sel_add_all');\"");
    $arg["main_part"]["SEL_ADD"]     = knjCreateBtn($objForm, "sel_add", "↓", "style=\"height:20px;width:30px\" onclick=\"return move('left');\"");
    $arg["main_part"]["SEL_DEL"]     = knjCreateBtn($objForm, "sel_del", "↑", "style=\"height:20px;width:30px\" onclick=\"return move('right');\"");
    $arg["main_part"]["SEL_DEL_ALL"] = knjCreateBtn($objForm, "sel_del_all", "≫", "style=\"height:20px;width:30px\" onclick=\"return moves('sel_del_all');\"");

    return;
}

//ボタン作成
function makeButton(&$objForm, &$arg)
{
    //更新ボタン
    $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", "onclick=\"return doSubmit();\"");
    //取消ボタン
    $arg["button"]["btn_clear"]  = knjCreateBtn($objForm, "btn_clear", "取 消", "onclick=\"return btn_submit('clear');\"");
    //戻るボタン
    //代替先科目
    //教育課程対応
    if ($model->Properties["useCurriculumcd"] == '1') {
        $param_subclasscd = $model->param_class."-".$model->param_schoolkind."-".$model->param_curriculumcd."-".$model->param_subclass;
    } else {
        $param_subclasscd = $model->param_subclass;
    }
    //リンク元のプログラムＩＤ
    $prgid = PROGRAMID;
    //リンク先のURL
    $jump = REQUESTROOT."/Z/KNJZ236/knjz236index.php";
    //URLパラメータ
    $param  = "?PROGRAMID={$prgid}";
    $param .= "&VALUE={$param_subclasscd}";
    $extra = "onclick=\"openOyagamen('{$jump}');\"";
    $arg["button"]["btn_end"]    = knjCreateBtn($objForm, "btn_end", "戻 る", $extra);
}

//hidden作成
function makeHidden(&$objForm) {
    knjCreateHidden($objForm, "cmd");
    knjCreateHidden($objForm, "selectdata");
}
?>
