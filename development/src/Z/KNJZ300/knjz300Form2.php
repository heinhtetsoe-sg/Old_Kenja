<?php

require_once('for_php7.php');

class knjz300Form2
{
    public function main(&$model)
    {
        $objForm = new form();

        //フォーム作成
        $arg["start"]   = $objForm->get_start("edit", "POST", "knjz300index.php", "", "edit");

        if ($model->isChgPwdUse) {
            $arg["chgPwd"] = 1;
        }

        //警告メッセージを表示しない場合
        if (isset($model->userscd) && !isset($model->warning)) {
            //データを取得
            $Row = knjz300Query::getRow($model->userscd, $model->year, $model);
        } else {
            $Row =& $model->field;
            $Row["STAFFCD"] = $model->field["USERSCD"];
        }

        //職員コード
        $arg["data"]["USERSCD"] = $Row["STAFFCD"];
        //職員氏名
        $arg["data"]["STAFFNAME"] = $Row["STAFFNAME"];
        //職員カナ
        $arg["data"]["STAFFKANA"] = $Row["STAFFKANA"];
        //ユーザーID
        $extra = "onblur=\"moji_hantei(this);\" STYLE=\"ime-mode:disabled\"";
        $arg["data"]["USERID"] = knjCreateTextBox($objForm, $Row["USERID"], "USERID", 32, 32, $extra);

        //パスワード表示設定
        if ($Row["PASSWD"]) {
            $password = "**********";
        } else {
            $password = "";
        }

        //パスワード
        $extra = "onblur=\"moji_hantei(this); pwdMojiCheck(this);\" STYLE=\"ime-mode:disabled\"";
        $arg["data"]["PASSWD"] = knjCreateTextBox($objForm, $password, "PASSWD", 32, 32, $extra);

        //無効フラグ
        $check = ($Row["INVALID_FLG"] == "1") ? "checked" : "";
        $extra = $check." onclick=\"CheckDispVal(this, 'on1');\"";
        $arg["data"]["INVALID_FLG"] = knjCreateCheckBox($objForm, "INVALID_FLG", $Row["INVALID_FLG"], $extra);

        if ($check != "checked") {
            $arg["data"]["style"] = "visibility:hidden";
        }

        //パスワード有効期限
        $check1 = ($Row["PWDTERMCHK_FLG"] == "1") ? "checked" : "";
        $extra = $check1." onclick=\"CheckDispVal(this, 'on2');\"";
        $arg["data"]["PWDTERMCHK_FLG"] = knjCreateCheckBox($objForm, "PWDTERMCHK_FLG", $Row["PWDTERMCHK_FLG"], $extra);

        if ($check1 != "checked") {
            $arg["data"]["style1"] = "visibility:hidden";
        }

        //次回パスワード強制変更
        $check2 = ($Row["CHG_PWD_FLG"] == "1") ? "checked" : "";
        $extra = $check2." onclick=\"CheckDispVal(this, 'on3');\"";
        $arg["data"]["CHG_PWD_FLG"] = knjCreateCheckBox($objForm, "CHG_PWD_FLG", $Row["CHG_PWD_FLG"], $extra);

        if ($check2 != "checked") {
            $arg["data"]["style2"] = "visibility:hidden";
        }

        $disabledExtra = sprintf("%010d", $Row["STAFFCD"]) == "0000999999" ? " disabled " : "";
 
        //更新ボタンを作成する
        $extra = $disabledExtra."onclick=\"return btn_submit('update');\"";
        $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_udpate", "更 新", $extra);

        //削除ボタンを作成する
        $extra = $disabledExtra."onclick=\"return btn_submit('delete');\"";
        $arg["button"]["btn_del"] = knjCreateBtn($objForm, "btn_del", "削 除", $extra);

        //取消ボタンを作成する
        $extra = $disabledExtra."onclick=\"return btn_submit('reset')\"";
        $arg["button"]["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);

        //終了ボタンを作成する
        $extra = "onclick=\"closeWin();\"";
        $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);

        //CSVファイルアップロードコントロール
        makeCsv($objForm, $arg, $model);

        //hiddenを作成する
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "UPDATED", $Row["UPDATED"]);
        knjCreateHidden($objForm, "STAFFNAME", $Row["STAFFNAME"]);
        knjCreateHidden($objForm, "STAFFKANA", $Row["STAFFKANA"]);
        knjCreateHidden($objForm, "USERSCD", $model->userscd);
        knjCreateHidden($objForm, "year", $model->year);

        $arg["finish"]  = $objForm->get_finish();

        $arg["reload"]="";
        if (VARS::get("cmd") != "edit") {
            $arg["reload"]  = "window.open('knjz300index.php?cmd=list','left_frame');";
        }

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjz300Form2.html", $arg);
    }
}
//ＣＳＶ作成
function makeCsv(&$objForm, &$arg, $model)
{
    //出力取込種別ラジオボタン 1:取込 2:書出 3:エラー書出 4:見本
    $opt_shubetsu = array(1, 2, 3, 4);
    $model->field["OUTPUT"] = ($model->field["OUTPUT"]) ? $model->field["OUTPUT"] : "1";
    $click = " onclick=\"return changeRadio(this);\"";
    $extra = array("id=\"OUTPUT1\"".$click, "id=\"OUTPUT2\"".$click, "id=\"OUTPUT3\"".$click, "id=\"OUTPUT4\"".$click);
    $radioArray = knjCreateRadio($objForm, "OUTPUT", $model->field["OUTPUT"], $extra, $opt_shubetsu, get_count($opt_shubetsu));
    foreach ($radioArray as $key => $val) {
        $arg["data"][$key] = $val;
    }

    //ファイルからの取り込み
    $extra = ($model->field["OUTPUT"] == "1") ? "" : "disabled";
    $arg["FILE"] = knjCreateFile($objForm, "FILE", $extra, 1024000);

    //ヘッダ有チェックボックス
    $check_header  = "checked id=\"HEADER\"";
    $arg["data"]["HEADER"] = knjCreateCheckBox($objForm, "HEADER", "on", $check_header, "");

    //実行ボタン
    $extra = "onclick=\"return btn_submit('exec');\"";
    $arg["btn_exec"] = knjCreateBtn($objForm, "btn_exec", "実 行", $extra);
}
