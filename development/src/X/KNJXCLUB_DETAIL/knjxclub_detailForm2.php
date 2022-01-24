<?php

require_once('for_php7.php');

class knjxclub_detailForm2
{
    function main(&$model)
    {

        //オブジェクト作成
        $objForm = new form;

        //DB接続
        $db = Query::dbCheckOut();

        //生徒情報表示
        $arg["SCHINFO"] = $db->getOne(knjxclub_detailQuery::getSchinfo($model));

        $btn_disableflg = false;
        if ($model->Properties["useClubMultiSchoolKind"] == "1") {
            //選択状態を判断
            if ($model->clubcd != "" && $model->seq != "") {
               //選択データに保持する校種を取得する
                $model->schreg_schkind = $db->getOne(knjxclub_detailQuery::getDetailSchKind($model));
                $schkwk = $db->getOne(knjxclub_detailQuery::getSchKind($model));
                if ($schkwk != $model->schreg_schkind) {
                    $btn_disableflg = true;
                }
            } else {
                //生徒の校種を保持する
                $model->schreg_schkind = $db->getOne(knjxclub_detailQuery::getSchKind($model));
            }
        }

        //警告メッセージを表示しない場合
        if (!isset($model->warning) && ($model->cmd != "edit2") && $model->clubcd && $model->date && $model->seq){
            $query = knjxclub_detailQuery::getRow($model);
            $Row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        } else {
            $Row =& $model->field;
        }

        //クラブコンボ作成
        $blank = ($model->programid == "KNJJ040") ? "" : "1" ;
        $extra = "onchange=\"return btn_submit('edit2');\"";
        $query = knjxclub_detailQuery::getClubName($model);
        makeCmb($objForm, $arg, $db, $query, "CLUBCD", $Row["CLUBCD"], $extra, 1, $model, $blank);

        //日付作成
        $Row["DETAIL_DATE"] = ($Row["DETAIL_DATE"]) ? $Row["DETAIL_DATE"] : CTRL_DATE;
        $arg["data"]["DETAIL_DATE"] = View::popUpCalendar($objForm, "DETAIL_DATE", str_replace("-", "/", $Row["DETAIL_DATE"]));

        //大会コンボ
        $query = knjxclub_detailQuery::getMeetList($model);
        $extra = "";
        makeCmb($objForm, $arg, $db, $query, "MEETLIST", $Row["MEETLIST"], $extra, 1, $model, "blank");

        //大会反映ボタン
        $arg["button"]["btn_refl"] = knjCreateBtn($objForm, "btn_refl", "反 映", "onclick=\"return refl('');\"");

        //大会名称テキストボックス
        $arg["data"]["MEET_NAME"] = knjCreateTextBox($objForm, $Row["MEET_NAME"], "MEET_NAME", 60, 60, "");

        //区分ラジオボタン 1:個人 2:団体
        $opt_div = array(1, 2);
        $Row["DIV"] = ($Row["DIV"] == "") ? "1" : $Row["DIV"];
        $extra = array("id=\"DIV1\"", "id=\"DIV2\"");
        $radioArray = knjCreateRadio($objForm, "DIV", $Row["DIV"], $extra, $opt_div, get_count($opt_div));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

        //開催地域コンボ作成
        $query = knjxclub_detailQuery::getClubHost($model);
        makeCmb($objForm, $arg, $db, $query, "HOSTCD", $Row["HOSTCD"], "", 1, $model, 1);

        //種目コンボ作成
        $extra = "onchange=\"return btn_submit('edit2')\"";
        $query = knjxclub_detailQuery::getClubItem($model, $Row["CLUBCD"]);
        makeCmb($objForm, $arg, $db, $query, "ITEMCD", $Row["ITEMCD"], $extra, 1, $model, 1);

        //種目種類コンボ作成
        $extra = ($Row["ITEMCD"] == "") ? "disabled" : "";
        $query = knjxclub_detailQuery::getClubItemKind($model, $Row["ITEMCD"]);
        makeCmb($objForm, $arg, $db, $query, "KINDCD", $Row["KINDCD"], $extra, 1, $model, 1);

        //成績コンボ作成
        $query = knjxclub_detailQuery::getClubRecord($model);
        makeCmb($objForm, $arg, $db, $query, "RECORDCD", $Row["RECORDCD"], "", 1, $model, 1);

        //記録テキストボックス
        $arg["data"]["DOCUMENT"] = knjCreateTextBox($objForm, $Row["DOCUMENT"], "DOCUMENT", 40, 40, "");

        //備考テキストボックス
        $arg["data"]["DETAIL_REMARK"] = knjCreateTextBox($objForm, $Row["DETAIL_REMARK"], "DETAIL_REMARK", 40, 40, "");

        //ボタン作成
        makeBtn($objForm, $arg, $model, $Row, $btn_disable);

        //hiddenを作成する
        makeHidden($objForm, $model);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム作成
        $arg["start"]   = $objForm->get_start("edit", "POST", "knjxclub_detailindex.php", "", "edit");

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        if (VARS::get("cmd") != "edit"){
            $arg["reload"]  = "parent.left_frame.location.href='knjxclub_detailindex.php?cmd=list';";
        }

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjxclub_detailForm2.html", $arg); 
    }
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $model, $blank="")
{
    $opt = array();
    $value_flg = false;
    if($blank) $opt[] = array('label' => "", 'value' => "");
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {

        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);

        if ($value == $row["VALUE"]) $value_flg = true;
    }
    $result->free();

    $value = ($value && $value_flg) ? $value : $opt[0]["value"];

    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}

//ボタン作成
function makeBtn(&$objForm, &$arg, $model, $Row, $btn_disableflg)
{
    //(PDF)参照...、PDF参照、削除ボタン
    if ($Row["CLUBCD"] !== "" && (!is_null($Row["DETAIL_DATE"]) && $Row["DETAIL_DATE"] !== "") && $model->seq !== "") {
        $fchkstr = DOCUMENTROOT."/pdf_download/".SCHOOLCD.SCHOOLKIND.$model->schregno.$Row["CLUBCD"].str_replace("/","-",$Row["DETAIL_DATE"]).$model->seq.".pdf";
        if (file_exists($fchkstr)) {
            $disableprm = "";
        } else {
            $disableprm = "disabled";
        }
    } else {
        $disableprm = "disabled";
    }
    $arg["data"]["FILESEL"] = knjCreateFile($objForm, "FILESEL", "", 512000);
    $arg["button"]["btn_viewpdf"] = knjCreateBtn($objForm, "btn_viewpdf", "PDF 参照", $disableprm." onclick=\"return btn_submit('viewpdf');\"");
    $arg["button"]["btn_delpdf"] = knjCreateBtn($objForm, "btn_delpdf", "削 除", $disableprm." onclick=\"return btn_submit('delpdf');\"");

    if($btn_disableflg || common::SecurityCheck(STAFFCD, $model->programid) < DEF_UPDATE_RESTRICT){
        //追加ボタン
        $arg["button"]["btn_add"] = knjCreateBtn($objForm, "btn_add", "追 加", "disabled");
        //修正ボタン
        $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", "disabled");
        //削除ボタン
        $arg["button"]["btn_del"] = knjCreateBtn($objForm, "btn_del", "削 除", "disabled");
    } else {
        //追加ボタン
        $arg["button"]["btn_add"] = knjCreateBtn($objForm, "btn_add", "追 加", " onclick=\"return btn_submit('add');\"");
        //修正ボタン
        $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", "onclick=\"return btn_submit('update');\"");
        //削除ボタン
        $arg["button"]["btn_del"] = knjCreateBtn($objForm, "btn_del", "削 除", "onclick=\"return btn_submit('delete');\"");
    }
    //取消ボタン
    $setdis = "";
    if ($btn_disableflg) {
        $setdis = " disabled";
    }
    $arg["button"]["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", "onclick=\"return btn_submit('clear');\"$setdis");
    //終了ボタン
    $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "戻 る", "onclick=\"closeWin();\"");
}

//Hidden作成
function makeHidden(&$objForm, $model)
{
    knjCreateHidden($objForm, "cmd");
    knjCreateHidden($objForm, "DETAIL_SEQ", $model->seq);
}
?>
