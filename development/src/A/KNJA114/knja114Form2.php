<?php

require_once('for_php7.php');

class knja114Form2 {
    function main(&$model) {
        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("edit", "POST", "knja114index.php", "", "edit");

        //DB接続
        $db = Query::dbCheckOut();

        //警告メッセージを表示しない場合
        if (!isset($model->warning)) {
            $Row = $db->getRow(knja114Query::getRow($model, $model->systemId), DB_FETCHMODE_ASSOC);
        } else {
            $Row =& $model->field;
        }

        //年組番
        $arg["data"]["GRADE_HR_CLASS"] = $Row["GRADE_HR_CLASS"];
        knjCreateHidden($objForm, "GRADE_HR_CLASS", $Row["GRADE_HR_CLASS"]);

        //氏名
        $arg["data"]["NAME"] = $Row["NAME"];
        knjCreateHidden($objForm, "NAME", $Row["NAME"]);

        //氏名かな
        $arg["data"]["NAME_KANA"] = $Row["NAME_KANA"];
        knjCreateHidden($objForm, "NAME_KANA", $Row["NAME_KANA"]);

        //ユーザーＩＤ
        $extra = "";
        $arg["data"]["LOGINID"] = knjCreateTextBox($objForm, $Row["LOGINID"], "LOGINID", 27, 26, $extra);

        //パスワード
        knjCreateHidden($objForm, "HID_PASSWORD", $Row["PASSWORD"]);
        $fuseji = "*";
        $ume = "";
        $cnt = mb_strlen($Row["PASSWORD"]);
        for ($umecnt = 1; $umecnt <= $cnt; $umecnt++) {
            $ume .= $fuseji;
        }
        knjCreateHidden($objForm, "UME_PASSWORD", $ume);
        $Row["PASSWORD"] = $ume;
        $extra = "";
        $arg["data"]["PASSWORD"] = knjCreateTextBox($objForm, $Row["PASSWORD"], "PASSWORD", 33, 32, $extra);

        //パスワード表示チェックボックス
        $extra = "id=\"PASSWORD_HYOUJI\" onclick=\"return passHyouji(this);\"";
        $arg["data"]["PASSWORD_HYOUJI"] = knjCreateCheckBox($objForm, "PASSWORD_HYOUJI", "1", $extra);

        //ＣＳＶ出入力機能
        //出力取込種別ラジオボタン(1:ヘッダ出力 2:データ取込 3:エラー出力 4:データ出力)
        $opt = array(1, 2, 3, 4);
        $model->field["OUTPUT"] = ($model->field["OUTPUT"] == "") ? "1" : $model->field["OUTPUT"];
        $click = " onclick=\"return changeRadio(this);\"";
        $extra = array("id=\"OUTPUT1\"".$click, "id=\"OUTPUT2\"".$click, "id=\"OUTPUT3\"".$click, "id=\"OUTPUT4\"".$click);
        $radioArray = knjCreateRadio($objForm, "OUTPUT", $model->field["OUTPUT"], $extra, $opt, get_count($opt));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

        //ファイルからの取り込み
        $extra = ($model->field["OUTPUT"] == "2") ? "" : "disabled";
        $arg["FILE"] = knjCreateFile($objForm, "FILE", $extra, 1024000);
        //ヘッダ有チェックボックス
        $check_header  = "checked id=\"HEADER\"";
        $arg["data"]["HEADER"] = knjCreateCheckBox($objForm, "HEADER", "on", $check_header, "");
        //実行ボタン
        $extra = "onclick=\"return btn_submit('exec');\"";
        $arg["btn_exec"] = knjCreateBtn($objForm, "btn_exec", "実 行", $extra);

        /********/
        /*ボタン*/
        /********/
        $disabled = ($model->schregNo == "") ? " disabled": "";
        //更新
        $extra = "onclick=\"return btn_submit('update');\"".$disabled;
        $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);
        //削除
        $extra = "onclick=\"return btn_submit('delete');\"".$disabled;
        $arg["button"]["btn_del"] = knjCreateBtn($objForm, "btn_del", "削 除", $extra);
        //取消
        $extra = "onclick=\"return btn_submit('reset');\"".$disabled;
        $arg["button"]["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);
        //終了
        $extra = "onclick=\"closeWin();\"";
        $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);

        //hidden
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "selectdata");

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();

        if (VARS::get("cmd") != "edit") {
            $arg["reload"]  = "parent.left_frame.location.href='knja114index.php?cmd=list';";
        }

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knja114Form2.html", $arg); 
    }
}
?>
