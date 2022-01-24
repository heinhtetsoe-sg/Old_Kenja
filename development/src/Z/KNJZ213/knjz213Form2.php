<?php

require_once('for_php7.php');

class knjz213Form2
{
    function main(&$model)
    {
        $objForm = new form;

        //フォーム作成
        $arg["start"]   = $objForm->get_start("edit", "POST", "knjz213index.php", "", "edit");

        //DB接続
        $db     = Query::dbCheckOut();

        //読替先科目表示
        
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $arg["useCurriculumcd"] = "1";
            $combShow = $model->combined_classcd."-".$model->combined_schoolkind."-".$model->combined_curriculumcd."-".$model->combined_subclasscd."　".$db->getOne(knjz213Query::getCombinedSubclass($model));
        } else {
            $arg["NoCurriculumcd"] = "1";
            $combShow = $model->combined_subclasscd."　".$db->getOne(knjz213Query::getCombinedSubclass($model));
        }
        
        $arg["COMBINED_SUBCLASSCD_SHOW"] = $combShow;

        //対象教科表示
        $classCd = substr($model->combined_subclasscd, 0, 2);
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $classShow = $model->combined_classcd."-".$model->combined_schoolkind."　".$db->getOne(knjz213Query::getClassName($classCd));
        } else {
            $classShow = $classCd."　".$db->getOne(knjz213Query::getClassName($classCd));
        }
        $arg["CLASS_SHOW"] = $classShow;

        //明細
        makeSubclassData($objForm, $arg, $db, $model);

        //ボタン作成
        makeBtn($objForm, $arg);

        //hidden作成
        makeHidden($objForm);

        if (VARS::get("cmd") != "edit"){
            $arg["reload"]  = "parent.left_frame.location.href='knjz213index.php?cmd=list2';";
        }

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjz213Form2.html", $arg); 
    }
}

//明細
function makeSubclassData(&$objForm, &$arg, $db, $model)
{
    //extraセット
    $extraInt = "onblur=\"this.value=toInteger(this.value)\";";
    $extraRight = "STYLE=\"text-align: right\"";

    $query  = knjz213Query::getAttendSubclass($model);
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        //チェックボックス

        $row["KAKUSI"] = "<input type=\"hidden\" name=\"SUBCLASSCD[]\" value=\"".$row["SUBCLASSCD"]."\">";
        $row["WEIGHTING"] = knjCreateTextBox($objForm, $row["WEIGHTING"], "WEIGHTING[]", 3, 3, $extraRight.$extraInt);

        $arg["data"][] = $row;
    }
    $result->free();
}

function makeBtn(&$objForm, &$arg) {

    //更新ボタン
    $extra = "onclick=\"return btn_submit('update');\"";
    $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);
    //削除ボタン
    $extra = "onclick=\"return btn_submit('delete');\"";
    $arg["button"]["btn_del"] = knjCreateBtn($objForm, "btn_del", "削 除", $extra);
    //終了ボタンを作成する
    $extra = "onclick=\"closeWin();\"";
    $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);
}

function makeHidden(&$objForm) {
    knjCreateHidden($objForm, "cmd");
}

?>
