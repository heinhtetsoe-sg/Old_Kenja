<?php

require_once('for_php7.php');
class knjz900Form1
{
    function main(&$model){

        //権限チェック
        if (AUTHORITY != DEF_UPDATABLE) {
           $arg["jscript"] = "OnAuthError();";
        }

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("knjz900Form1", "POST", "knjz900index.php", "", "knjz900Form1");

        //DB接続
        $db = Query::dbCheckOut();

        if($model->cmd == "main"){
           $model->prg_id = "";
           $model->name = "";
           $model->menuid = "";
           $model->value = "";
           $model->field = "";
        }

        //年度
        $arg["data"]["YEAR"] = CTRL_YEAR;

        //プロパティー一覧取得
        $bifKey = "";
        $result = $db->query(knjz900Query::getList());
        while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            //レコードを連想配列のまま配列$arg[data]に追加していく。
            array_walk($row, "htmlspecialchars_array");

            if ($bifKey != $row["MENUID"].$row["PRG_ID"]) {
                $cnt = $db->getOne(knjz900Query::getPrgNameCnt($row["PRG_ID"]));
                $row["ROWSPAN"] = ($cnt > 0) ? $cnt : 1;
            }
            $bifKey = $row["MENUID"].$row["PRG_ID"];
            $row["backcolor"] = ($row["PRG_ID"]) ? "#ffffff" : "#ccffcc";
            $arg["data"][] = $row;
        }
        $result->free();

        //警告メッセージを表示しない場合
        if (!isset($model->warning) && $model->prg_id && $model->name && $model->menuid) {
            $flg = (substr($model->menuid,0,2) == 'ZZ') ? "1" : "";
            $Row = $db->getRow(knjz900Query::getRow($model, $flg), DB_FETCHMODE_ASSOC);
            $Row["INPUT"] = (substr($model->menuid,0,2) == 'ZZ') ? "2" : "1";
            $Row["PROGRAMID"] = (substr($model->menuid,0,2) == 'ZZ') ? $Row["PROGRAMID"] : "";
        }else{
            $Row =& $model->field;
        }

        /****************/
        /* ラジオボタン */
        /****************/
        //プログラム選択方法ラジオボタン 1:コンボボックス 2:テキストボックス
        $opt_input = array(1, 2);
        $Row["INPUT"] = ($Row["INPUT"] == "") ? "1" : $Row["INPUT"];
        $extra = array("id=\"INPUT1\" onclick=\"OptionUse('this');\"", "id=\"INPUT2\" onclick=\"OptionUse('this');\"");
        $radioArray = knjCreateRadio($objForm, "INPUT", $Row["INPUT"], $extra, $opt_input, get_count($opt_input));
        foreach($radioArray as $key => $val) $arg["data1"][$key] = $val;

        /******************/
        /* コンボボックス */
        /******************/
        //プログラム
        $opt       = array();
        $opt[]     = array("label" => "", "value" => "");
        $value_flg = false;
        $result    = $db->query(knjz900Query::getMenuMst());
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[] = array("label" => $row["PRG_ID"]." ".$row["MENUNAME"],
                           "value" => $row["MENUID"]);

            if ($model->menuid == $row["MENUID"]) $value_flg = true;
        }
        $result->free();

        $Row["MENUID"] = ($Row["MENUID"] && $value_flg) ? $Row["MENUID"] : $opt[0]["MENUID"];
        $extra = ($Row["INPUT"] == "1") ? "" : "disabled";
        $arg["data1"]["MENUID"] = knjCreateCombo($objForm, "MENUID", $Row["MENUID"], $opt, $extra, 1);

        /********************/
        /* テキストボックス */
        /********************/
        //プログラムＩＤ入力
        $extra  = "onblur=\"this.value=toAlphanumeric(this.value)\"";
        $extra .= ($Row["INPUT"] == "1" || $model->cmd == "") ? " disabled STYLE=\"background-color:#D3D3D3\"" : "";
        $arg["data1"]["PROGRAMID"] = knjCreateTextBox($objForm, $Row["PROGRAMID"], "PROGRAMID", 20, 20, $extra);
        //パラメータ
        $extra = "onblur=\"this.value=toAlphanumeric(this.value)\"";
        $arg["data1"]["NAME"] = knjCreateTextBox($objForm, $Row["NAME"], "NAME", 50, 50, $extra);
        //値
        $arg["data1"]["VALUE"] = knjCreateTextBox($objForm, $Row["VALUE"], "VALUE", 100, 300, "");
        //備考
        $arg["data1"]["REMARK"] = knjCreateTextBox($objForm, $Row["REMARK"], "REMARK", 100, 300, "");

        //ボタン作成
        makeBtn($objForm, $arg);

        //hidden作成
        makeHidden($objForm, $model);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjz900Form1.html", $arg);
    }
}

//ボタン作成
function makeBtn(&$objForm, &$arg)
{
    //追加ボタンを作成する
    $extra = "onclick=\"return btn_submit('insert');\"";
    $arg["button"]["btn_insert"]    = knjCreateBtn($objForm, "btn_insert", "追 加", $extra);
    //修正ボタンを作成する
    $extra = "onclick=\"return btn_submit('update');\"";
    $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);
    //削除ボタンを作成する
    $extra = "onclick=\"return btn_submit('delete');\"";
    $arg["button"]["btn_del"]    = knjCreateBtn($objForm, "btn_del", "削 除", $extra);
    //クリアボタンを作成する
    $extra = "onclick=\"return btn_submit('reset');\"";
    $arg["button"]["btn_reset"]  = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);
    //終了ボタンを作成する
    $extra = "onclick=\"closeWin();\"";
    $arg["button"]["btn_end"]   = knjCreateBtn($objForm, "btn_end", "終 了", $extra);

    //一括処理ボタン
    $extra = "onclick=\"return btn_submit('subform1');\"";
    $arg["button"]["btn_subform1"] = KnjCreateBtn($objForm, "btn_subform1", "一括処理", $extra);
}

//hidden作成
function makeHidden(&$objForm, $model)
{
    knjCreateHidden($objForm, "YEAR", CTRL_YEAR);
    knjCreateHidden($objForm, "SEMESTER", CTRL_SEMESTER);
    knjCreateHidden($objForm, "CTRL_DATE", CTRL_DATE);
    knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
    knjCreateHidden($objForm, "cmd");
    knjCreateHidden($objForm, "PRGID", "KNJZ900");
    knjCreateHidden($objForm, "STAFFCD", STAFFCD);
}
?>
