<?php

require_once('for_php7.php');


class knja430sForm2
{
    function main(&$model)
    {
        $objForm = new form;
        $arg["start"]   = $objForm->get_start("edit", "POST", "knja430sindex.php", "", "edit");

        //警告メッセージを表示しない場合
        if (!isset($model->warning) && $model->stampNo != "")
        {
            $Row = knja430sQuery::getStampRow($model->stampNo);
        } else if ($model->maxStampNo != "") {
            $Row["STAMP_NO"] = $model->maxStampNo;
            $Row["START_DATE"] = CTRL_DATE;
        } else {
            $Row =& $model->field;
        }

        //DB接続
        $db = Query::dbCheckOut();

        //印影の表示
        if (isset($Row["DATE"])) {
            $query = knja430sQuery::getIneiFlg($Row["STAMP_NO"]);
            $ineiFlg = $db->getOne($query);
            if ($ineiFlg) {
                $stampFile = $Row["STAMP_NO"].".bmp";
                $src = REQUESTROOT ."/image/stamp/" .$stampFile;
                $arg["data"]["IMAGE"] = '<image src="'.$src.'" alt="'.$stampFile.'" width="60" height="60">';
            }
        }

        //職員コード
        $arg["data"]["STAFFCD"] = $model->staffcd;

        //職員氏名
        $arg["data"]["STAFFNAME"] = $Row["STAFFNAME"];

        //印鑑番号
        $extra = "readonly=\"readonly\"";
        $arg["data"]["STAMP_NO"] = knjCreateTextBox($objForm, $Row["STAMP_NO"], "STAMP_NO", 8, 6, $extra);

        //校長フラグ
        $extra = ($Row["DIST"]) ? "checked" : "";
        $arg["data"]["DIST"] = knjCreateCheckBox($objForm, "DIST", "on", $extra);

        //登録事由
        $extra = "";
        $arg["data"]["START_REASON"] = knjCreateTextBox($objForm, $Row["START_REASON"], "START_REASON", 20, 10, $extra);

        //除印事由
        $arg["data"]["STOP_REASON"] = knjCreateTextBox($objForm, $Row["STOP_REASON"], "STOP_REASON", 20, 10, $extra);

        //登録日付
        $Row["START_DATE"] = str_replace("-","/",$Row["START_DATE"]);
        $arg["data"]["START_DATE"] = View::popUpCalendar($objForm, "START_DATE" ,$Row["START_DATE"]);

        //除印日付
        $Row["STOP_DATE"] = str_replace("-","/",$Row["STOP_DATE"]);
        $arg["data"]["STOP_DATE"] = View::popUpCalendar($objForm, "STOP_DATE" ,$Row["STOP_DATE"]);

        //走査日付
        $extra = "readonly=\"readonly\"";
        $Row["DATE"] = str_replace("-","/",$Row["DATE"]);
        $arg["data"]["DATE"] = knjCreateTextBox($objForm, $Row["DATE"], "DATE", 12, 10, $extra);

        //ボタン
        makeBtn($objForm, $arg, $model);

        //hiddenを作成する
        makeHidden($objForm, $model);

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();

        if (VARS::get("cmd") != "edit"){
            $arg["reload"]  = "parent.right_frame.location.href='knja430sindex.php?cmd=list';";
        }
                                
        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knja430sForm2.html", $arg);
    }
}

//ボタン作成
function makeBtn(&$objForm, &$arg, &$model)
{
    $disAdd = ($model->maxStampNo != "") ? "" : " disabled";
    $disDel = ($model->stampNo != "") ? "" : " disabled";

    //走査
    $pass = REQUESTROOT ."/A/KNJA430S_2/knja430s_2index.php?inkan_no=" .$model->stampNo ."&inkan_Path=";
    $extra = "onclick=\"wopen('{$pass}','knja430s_2Inkan',0,0,screen.availWidth,screen.availHeight);\"" .$disDel;
    $arg["button"]["btn_inkan"] = knjCreateBtn($objForm, "btn_inkan", "ｽｷｬﾅｰ走査", $extra);

    //印鑑登録原票印刷
    $extra = "onclick=\"return newwin1('" . SERVLET_URL . "');\"" .$disDel;
    $arg["button"]["btn_print1"] = knjCreateBtn($objForm, "btn_print1", "原票印刷", $extra);

    //印鑑登録確認票印刷
    $extra = "onclick=\"return newwin2('" . SERVLET_URL . "');\"" .$disDel;
    $arg["button"]["btn_print2"] = knjCreateBtn($objForm, "btn_print2", "確認票印刷", $extra);

    //新規
    $extra = "onclick=\"return btn_submit('new');\"";
    $arg["button"]["btn_new"] = knjCreateBtn($objForm, "btn_new", "新 規", $extra);

    //追加
    $extra = "onclick=\"return btn_submit('add');\"" .$disAdd;
    $arg["button"]["btn_add"] = knjCreateBtn($objForm, "btn_add", "追 加", $extra);

    //更新
    $extra = "onclick=\"return btn_submit('update');\"" .$disDel;
    $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);

    //削除
    //$extra = "onclick=\"return btn_submit('delete');\"" .$disDel;
    //$arg["button"]["btn_del"] = knjCreateBtn($objForm, "btn_del", "削 除", $extra);

    //取消
    $extra = "onclick=\"return btn_submit('reset');\"";
    $arg["button"]["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);

    //終了
    $extra = "onclick=\"closeWin();\"";
    $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);
}

//Hidden作成
function makeHidden(&$objForm, &$model)
{
    knjCreateHidden($objForm, "cmd");
    knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
    knjCreateHidden($objForm, "PRGID", "KNJA430S");
    knjCreateHidden($objForm, "FORMID");
    knjCreateHidden($objForm, "DOCUMENTROOT", DOCUMENTROOT);
    knjCreateHidden($objForm, "HID_STAFFCD", $model->staffcd);
    knjCreateHidden($objForm, "HID_MAX_STAMP_NO", $model->maxStampNo);
    knjCreateHidden($objForm, "HID_STAMP_NO", $model->stampNo);
}

?>
