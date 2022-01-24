<?php

require_once('for_php7.php');

class knjl216yForm1
{
    function main(&$model){

        $objForm = new form;
        
        //フォーム作成
        $arg["start"] = $objForm->get_start("knjl216yForm1", "POST", "knjl216yindex.php", "", "knjl216yForm1");

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["TOP"]["YEAR"] = $model->ObjYear;

        //履歴一覧
        $query = knjl216yQuery::getList($model);
        $result = $db->query($query);
        while ($rirekiRow = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $arg["data2"][] = $rirekiRow;
        }
        $result->free();

        //履歴から選択時
        if (!isset($model->warning) && ($model->cmd == "select")) {
            $query = knjl216yQuery::getList($model, "select");
            $Row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        } else {
            $Row =& $model->field;
        }

        //権限チェック
        if (AUTHORITY == DEF_UPDATABLE) {
            $arg["DEF_UPDATABLE"] = "1";
            $arg["BUTTON_COLSPAN"] = "2";
        } else {
            $arg["BUTTON_COLSPAN"] = "4";
        }

        //受験番号
        $arg["data"]["EXAMNO"] = $model->examno;
        // 生徒氏名
        $arg["data"]["APPL_NAME"] = $db->getOne(knjl216yQuery::get_appl_name($model));

        //氏名
        $extra = "STYLE=\"ime-mode: active;\"";
        $arg["data"]["NAME"] = knjCreateTextBox($objForm, $Row["NAME"], "NAME", 40, 60, $extra);

        //年齢
        $extra = "style=\"text-align:right;ime-mode: inactive;\" onblur=\"this.value = toInteger(this.value)\"";
        $arg["data"]["AGE"] = knjCreateTextBox($objForm, $Row["AGE"], "AGE", 3, 3, $extra);

        //続柄
        $opt = array();
        $opt[] = array('label' => "", 'value' => "");
        $value_flg = false;
        $result = $db->query(knjl216yQuery::get_name_cd($model, "H201"));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[] = array('label' => $row["LABEL"],
                           'value' => $row["VALUE"]);
            if ($Row["RELATIONSHIP"] == $row["VALUE"]) $value_flg = true;
        }
        $result->free();
        $Row["RELATIONSHIP"] = ($Row["RELATIONSHIP"] && $value_flg) ? $Row["RELATIONSHIP"] : $opt[0]["value"];
        $extra = "";
        $arg["data"]["RELATIONSHIP"] = knjCreateCombo($objForm, "RELATIONSHIP", $Row["RELATIONSHIP"], $opt, $extra, 1);

        //備考
        $extra = "STYLE=\"ime-mode: active;\"";
        $arg["data"]["REMARK"] = knjCreateTextBox($objForm, $Row["REMARK"], "REMARK", 40, 60, $extra);

        //新規ボタン
        $extra = "onclick=\"return btn_submit('insert');\"";
        $arg["button"]["btn_insert"] = knjCreateBtn($objForm, "btn_insert", "新 規", $extra);

        //更新ボタン
        $extra = "onclick=\"return btn_submit('update');\"";
        $arg["button"]["btn_udpate"] = knjCreateBtn($objForm, "btn_udpate", "更 新", $extra);

        //削除ボタン
        $extra = "onclick=\"return btn_submit('delete');\"";
        $arg["button"]["btn_delete"] = knjCreateBtn($objForm, "btn_delete", "削 除", $extra);

        //戻るボタン
        $link = REQUESTROOT."/L/KNJL211Y/knjl211yindex.php?cmd=reference&SEND_PRGID=KNJL216Y&SEND_AUTH=".$model->auth."&SEND_APPLICANTDIV=".$model->applicantdiv."&SEND_EXAMNO=".$model->examno;
        $extra = "onClick=\" Page_jumper('{$link}');\"";
        $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "戻 る", $extra);

        //hidden
        knjCreateHidden($objForm, "YEAR", $model->ObjYear);
        knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
        knjCreateHidden($objForm, "PRGID", "KNJL216Y");
        knjCreateHidden($objForm, "cmd");

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();
        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjl216yForm1.html", $arg); 
        
    }
}
?>
