<?php

require_once('for_php7.php');

class knjb1211Form2 {

    function main(&$model) {

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("edit", "POST", "knjb1211index.php", "", "edit");

        //権限チェック
        if (AUTHORITY != DEF_UPDATABLE){
           $arg["jscript"] = "OnAuthError();";
        }

        //db接続
        $db = Query::dbCheckOut();

        //科目名取得
        $arg["SUBCLASSNAME"] = $db->getOne(knjb1211Query::getSubclassName($model));

        //教科書一覧
        $query = knjb1211Query::getRow($model);
        $result = $db->query($query); 
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            array_walk($row, "htmlspecialchars_array");

            $extra = $row["TEXT_CHECK"] ? " checked" : "";
            $row["TEXTBOOKCD_CHECKBOX"] = knjCreateCheckBox($objForm, "TEXTBOOKCD", $row["TEXTBOOKCD"], $extra, "1");
            $extra = $row["NOT_DEFAULT"] ? " checked" : "";
            $row["NOT_DEFAULT"] = knjCreateCheckBox($objForm, "NOT_DEFAULT".$row["TEXTBOOKCD"], "1", $extra, "");
            $row["TEXTBOOKPRICE"] = is_numeric($row["TEXTBOOKPRICE"]) ? number_format($row["TEXTBOOKPRICE"]) : "";

            $arg["data"][] = $row; 
        }

        //ボタン作成
        makeBtn($objForm, $arg, $model);

        //hidden作成
        makeHidden($objForm);

        //DB切断
        Query::dbCheckIn($db);

        if (VARS::get("cmd") != "edit"){
            $arg["reload"]  = "parent.left_frame.location.href='knjb1211index.php?cmd=list';";
        }

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjb1211Form2.html", $arg); 
    }
}

//ボタン作成
function makeBtn(&$objForm, &$arg, $model) {

    //更新
    $extra = (AUTHORITY >= DEF_UPDATE_RESTRICT && $model->classcd && $model->school_kind && $model->curriculum_cd && $model->subclasscd) ? "onclick=\"return btn_submit('update');\"" : "disabled";
    $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);
    //取消
    $extra = "onclick=\"return btn_submit('reset');\"";
    $arg["button"]["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);
    //終了
    $extra = "onclick=\"closeWin();\"";
    $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);
}

//Hidden作成
function makeHidden(&$objForm) {
    knjCreateHidden($objForm, "cmd");
}
?>
