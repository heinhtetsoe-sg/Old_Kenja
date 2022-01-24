<?php

require_once('for_php7.php');

class knjl230yForm2
{
    function main(&$model) {

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("main", "POST", "knjl230yindex.php", "", "main");

        //DB接続
        $db = Query::dbCheckOut();

        //グループデータ
        if ($model->isWarning()) {
            $Row =& $model->field;
        } else {
            $query = knjl230yQuery::selectQuery($model);
            $Row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        }

        //グループ番号
        if ($model->mode == "update") {
            $extra = "style=\"background-color: gray;\" readonly";
        } else {
            $extra = "STYLE=\"ime-mode: inactive;\" onblur=\"this.value=toInteger(this.value);\" ";
        }
        $arg["EXAMHALLCD"] = knjCreateTextBox($objForm, $Row["EXAMHALLCD"], "EXAMHALLCD", 5, 4, $extra);

        //グループ名
        $extra = "STYLE=\"ime-mode: active;\" ";
        $arg["EXAMHALL_NAME"] = knjCreateTextBox($objForm, $Row["EXAMHALL_NAME"], "EXAMHALL_NAME", 31, 31, $extra);

        //人数(表示のみ)
        $arg["CAPA_CNT_SHOW"] = $Row["CAPA_CNT"];

        //--------------------------------------------------

        //対象者一覧
        //追加・更新画面の左リスト
        //指定グループデータのリスト
        $opt_left = $tmp_id = array();
        $result = $db->query(knjl230yQuery::getListHallorRecept($model, "left"));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $row["BIRTHDAY"] = str_replace("-", "/", $row["BIRTHDAY"]);
            $opt_left[] = array("label" => $row["BIRTHDAY"]."：".$row["RECEPTNO"]."：".$row["NAME"], "value" => $row["BIRTHDAY"].":".$row["RECEPTNO"]);
            $tmp_id[]   = $row["RECEPTNO"];
        }
        $result->free();
        $extra = "multiple STYLE=\"WIDTH:100%\" WIDTH=\"100%\" ondblclick=\"move3('right','SPECIALS','APPROVED',1);\"";
        $arg["main_part"]["LEFT_PART"] = knjCreateCombo($objForm, "SPECIALS", "left", $opt_left, $extra, 20);

        //志願者一覧
        //追加・更新画面の右リスト
        //受付データからグループデータを除いたリスト
        $opt_right = array();
        $result = $db->query(knjl230yQuery::getListHallorRecept($model, "right"));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            if (!in_array($row["RECEPTNO"], $tmp_id)) {
                $row["BIRTHDAY"] = str_replace("-", "/", $row["BIRTHDAY"]);
                $opt_right[] = array("label" => $row["BIRTHDAY"]."：".$row["RECEPTNO"]."：".$row["NAME"], "value" => $row["BIRTHDAY"].":".$row["RECEPTNO"]);
            }
        }
        $result->free();
        $extra = "multiple STYLE=\"WIDTH:100%\" WIDTH=\"100%\" ondblclick=\"move3('left','SPECIALS','APPROVED',1);\"";
        $arg["main_part"]["RIGHT_PART"] = knjCreateCombo($objForm, "APPROVED", "right", $opt_right, $extra, 20);

        //追加ボタン
        $extra = "onclick=\"return move3('sel_add_all','SPECIALS','APPROVED',1);\"";
        $arg["main_part"]["SEL_ADD_ALL"] = knjCreateBtn($objForm, "sel_add_all", "≪", $extra);

        $extra = "onclick=\"return move3('left','SPECIALS','APPROVED',1);\"";
        $arg["main_part"]["SEL_ADD"] = knjCreateBtn($objForm, "sel_add", "＜", $extra);

        $extra = "onclick=\"return move3('right','SPECIALS','APPROVED',1);\"";
        $arg["main_part"]["SEL_DEL"] = knjCreateBtn($objForm, "sel_del", "＞", $extra);

        $extra = "onclick=\"return move3('sel_del_all','SPECIALS','APPROVED',1);\"";
        $arg["main_part"]["SEL_DEL_ALL"] = knjCreateBtn($objForm, "sel_del_all", "≫", $extra);

        //--------------------------------------------------

        //追加・更新ボタン
        if ($model->mode == "update"){
            $value = "更 新";
        }else{
            $value = "追 加";
        }
        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_update",
                            "value"       => $value,
                            "extrahtml"   => "onclick=\"return btn_submit('".$model->mode ."')\"" ) );
        $arg["btn_update"]  = $objForm->ge("btn_update");

        //戻るボタン
        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_back",
                            "value"       => "戻 る",
                            "extrahtml"   => "onclick=\"top.main_frame.closeit()\"" ) );
        $arg["btn_back"]  = $objForm->ge("btn_back");

        //hiddenを作成する
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "selectdata");
        knjCreateHidden($objForm, "selectdata2");

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjl230yForm2.html", $arg); 
    }
}
?>
