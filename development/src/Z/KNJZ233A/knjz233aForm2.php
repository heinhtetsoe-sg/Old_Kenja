<?php

require_once('for_php7.php');

class knjz233aForm2 {

    function main(&$model) {

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("edit", "POST", "knjz233aindex.php", "", "edit");

        //DB接続
        $db = Query::dbCheckOut();

        //読替先科目表示
        $combShow = $model->combined_subclasscd."　".$db->getOne(knjz233aQuery::getCombinedSubclass($model));
        $arg["COMBINED_SUBCLASSCD_SHOW"] = $combShow;

        //対象教科表示
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $subclass_array = array();
            $subclass_array = explode("-", $model->combined_subclasscd);
            $classCd = $subclass_array[0].'-'.$subclass_array[1];
        } else {
            $classCd = substr($model->combined_subclasscd, 0, 2);
        }
        $classShow = $classCd."　".$db->getOne(knjz233aQuery::getClassName($classCd, $model));
        $arg["CLASS_SHOW"] = $classShow;

        //SEQテキスト
        $model->field["SEQ"] = ($model->field["SEQ"] == "" || $model->field["SEQ"] == "00" || $model->cmd == "reset") ? $model->seq : $model->field["SEQ"];
        $extra = "style=\"text-align: right\" onblur=\"this.value=toInteger(this.value)\"";
        $arg["SEQ"] = knjCreateTextBox($objForm, $model->field["SEQ"], "SEQ", 3, 2, $extra);

        //明細
        makeSubclassData($objForm, $arg, $db, $model);

        //ボタン作成
        makeBtn($objForm, $arg, $model);

        //hidden作成
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "BUNKYO", $db->getOne(knjz233aQuery::checkBunkyo()));

        if (VARS::get("cmd") != "edit"){
            $arg["reload"]  = "parent.left_frame.location.href='knjz233aindex.php?cmd=list2';";
        }

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjz233aForm2.html", $arg); 
    }
}

//明細
function makeSubclassData(&$objForm, &$arg, $db, $model) {
    //extraセット
    if ($model->Properties["weightingHyouki"] == "1") {
        $extraSet = "onblur=\"calcProperties(this);\"";
        $size = "5";
    } else {
        $extraSet = "onblur=\"this.value=toInteger(this.value);\"";
        $size = "3";
    }
    $extraRight = "STYLE=\"text-align: right\"";

    $query  = knjz233aQuery::getAttendSubclass($model);
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
            $row["KAKUSI"] = "<input type=\"hidden\" name=\"SUBCLASSCD[]\" value=\"".$row["CLASSCD"].'-'.$row["SCHOOL_KIND"].'-'.$row["CURRICULUM_CD"].'-'.$row["SUBCLASSCD"]."\">";
        } else {
            $row["KAKUSI"] = "<input type=\"hidden\" name=\"SUBCLASSCD[]\" value=\"".$row["SUBCLASSCD"]."\">";
        }
        $row["WEIGHTING"] = knjCreateTextBox($objForm, $row["WEIGHTING"], "WEIGHTING[]", $size, $size, $extraRight.$extraSet);

        $arg["data"][] = $row;
    }
    $result->free();
}

//ボタン作成
function makeBtn(&$objForm, &$arg, $model) {
    $disabled = ($model->send_flg) ? "" : " disabled";
    //追加ボタン
    $disabled = ($model->send_flg) ? "" : " disabled";
    $extra = "onclick=\"return btn_submit('insert');\"".$disabled;
    $arg["button"]["btn_insert"] = knjCreateBtn($objForm, "btn_insert", "追 加", $extra);
    //更新ボタン
    $disabled = ($model->send_flg == "SEQ") ? "" : " disabled";
    $extra = "onclick=\"return btn_submit('update');\"".$disabled;
    $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);
    //削除ボタン
    $disabled = ($model->send_flg == "SEQ") ? "" : " disabled";
    $extra = "onclick=\"return btn_submit('delete');\"".$disabled;
    $arg["button"]["btn_del"] = knjCreateBtn($objForm, "btn_del", "削 除", $extra);
    //取消ボタン
    $disabled = ($model->send_flg == "SEQ") ? "" : " disabled";
    $extra = "onclick=\"return btn_submit('reset');\"".$disabled;
    $arg["button"]["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);
    //終了ボタン
    $extra = "onclick=\"closeWin();\"";
    $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);
}
?>
