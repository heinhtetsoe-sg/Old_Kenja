<?php

require_once('for_php7.php');

class knjj120aForm2 {

    function main(&$model) {

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("edit", "POST", "knjj120aindex.php", "", "edit");

        //DB接続
        $db = Query::dbCheckOut();

        if ($model->cmd == "edit3") {
            if ($model->clubcd != $model->field["CLUBCD"]) unset($model->clubcd);
            $model->selectdata = array();
        }

        //校種取得
        if ($model->Properties["use_prg_schoolkind"] == "1" && !$model->schkind) {
            $model->schkind = $db->getOne(knjj120aQuery::getSchkind($model));
        }

        //警告メッセージを表示しない場合
        if (!isset($model->warning) && ($model->cmd != "edit2") && ($model->cmd != "edit3") && $model->clubcd && $model->date && $model->seq) {
            $query = knjj120aQuery::getRow($model);
            $Row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        } else {
            $Row =& $model->field;
        }

        //クラブコンボ作成
        $query = knjj120aQuery::getClubList($model, "1");
        $extra = "onchange=\"return btn_submit('edit3');\"";
        makeCmb($objForm, $arg, $db, $query, "CLUBCD", $Row["CLUBCD"], $extra, 1, $model, "blank");

        //校種コンボ
        if ($model->Properties["useClubMultiSchoolKind"] == "1") {
            //部活動校種取得
            $model->multi_schkind = $db->getOne(knjj120aQuery::getClubMultiSchoolKind($model, $Row["CLUBCD"]));

            $arg["schkind"] = "1";
            $query = knjj120aQuery::getSchkind($model);
            $extra = "onchange=\"return btn_submit('edit3');\"";
            makeCmb($objForm, $arg, $db, $query, "DETAIL_SCHKIND", $model->detail_schkind, $extra, 1, $model);
        }

        //日付作成
        $arg["data"]["DETAIL_DATE"] = View::popUpCalendar($objForm, "DETAIL_DATE", str_replace("-", "/", $Row["DETAIL_DATE"]));

        //DETAIL_SEQ表示
        $arg["data"]["DETAIL_SEQ"] = ($model->seq) ? 'No.'.$model->seq : "";
        knjCreateHidden($objForm, "DETAIL_SEQ", $model->seq);

        //大会コンボ
        $query = knjj120aQuery::getMeetList($model);
        $extra = "";
        makeCmb($objForm, $arg, $db, $query, "MEETLIST", $Row["MEETLIST"], $extra, 1, $model, "blank");

        //大会反映ボタン
        $arg["button"]["btn_refl"] = knjCreateBtn($objForm, "btn_refl", "反 映", "onclick=\"return refl('');\"");

        //大会名称テキストボックス
        $arg["data"]["MEET_NAME"] = knjCreateTextBox($objForm, $Row["MEET_NAME"], "MEET_NAME", 60, 60, "");

        //区分ラジオボタン 1:個人 2:団体
        $opt_div = array(1, 2);
        $Row["DIV"] = ($Row["DIV"] == "") ? "1" : $Row["DIV"];
        $extra = array("id=\"DIV1\" onclick=\"return btn_submit('edit3');\"", "id=\"DIV2\" onclick=\"return btn_submit('edit3');\"");
        $radioArray = knjCreateRadio($objForm, "DIV", $Row["DIV"], $extra, $opt_div, get_count($opt_div));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

        //年組のMAX文字数取得
        $max_len = 0;
        $query = knjj120aQuery::getSchInfo($model, $Row["CLUBCD"]);
        $result = $db->query($query);
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            $zenkaku = (strlen($row["HR_NAME"]) - mb_strlen($row["HR_NAME"])) / 2;
            $hankaku = ($zenkaku > 0) ? mb_strlen($row["HR_NAME"]) - $zenkaku : mb_strlen($row["HR_NAME"]);
            $max_len = ($zenkaku * 2 + $hankaku > $max_len) ? $zenkaku * 2 + $hankaku : $max_len;
        }

        //生徒選択
        if ($Row["DIV"] == "1") {
            //生徒コンボ作成
            $arg["kojin"] = "1";

            $opt = array();
            $value_flg = false;
            $opt[] = array('label' => "", 'value' => "");
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                //クラス名称調整
                $zenkaku = (strlen($row["HR_NAME"]) - mb_strlen($row["HR_NAME"])) / 2;
                $hankaku = ($zenkaku > 0) ? mb_strlen($row["HR_NAME"]) - $zenkaku : mb_strlen($row["HR_NAME"]);
                $len = $zenkaku * 2 + $hankaku;
                $hr_name = $row["HR_NAME"];
                for ($j=0; $j < ($max_len - ($zenkaku * 2 + $hankaku)); $j++) $hr_name .= "&nbsp;";

                $opt[] = array('label' => $hr_name." ".$row["ATTENDNO"]."番 ".$row["NAME_SHOW"],
                               'value' => $row["VALUE"]);

                if ($Row["SCHREGNO"] == $row["VALUE"]) $value_flg = true;
            }
            $result->free();

            $Row["SCHREGNO"] = ($Row["SCHREGNO"] && $value_flg) ? $Row["SCHREGNO"] : $opt[0]["value"];
            $extra = "";
            $arg["data"]["SCHREGNO"] = knjCreateCombo($objForm, "SCHREGNO", $Row["SCHREGNO"], $opt, $extra, 1);
        } else {
            $arg["dantai"] = "1";

            $sch_list = array();
            if ($model->cmd == "edit" && !isset($model->warning)) {
                $query = knjj120aQuery::getGroupSchList($model);
                $sch_list = $db->getCol($query);
            }

            $selectData = array();
            $sch_list = (implode(",", $model->selectdata)) ? $model->selectdata : $sch_list;
            foreach ($sch_list as $key => $val) {
                $selectData[$val] = "1";
            }

            //リストToリスト作成
            makeListToList($objForm, $arg, $db, $model, $Row["CLUBCD"], $selectData, $max_len);
        }

        //開催地域コンボ作成
        $query = knjj120aQuery::getClubHost($model);
        makeCmb($objForm, $arg, $db, $query, "HOSTCD", $Row["HOSTCD"], "", 1, $model, 1);

        //種目コンボ作成
        $extra = "onchange=\"return btn_submit('edit2')\"";
        $query = knjj120aQuery::getClubItem($model, $Row["CLUBCD"]);
        makeCmb($objForm, $arg, $db, $query, "ITEMCD", $Row["ITEMCD"], $extra, 1, $model, 1);

        //種目種類コンボ作成
        $extra = ($Row["ITEMCD"] == "") ? "disabled" : "";
        $query = knjj120aQuery::getClubItemKind($model, $Row["ITEMCD"]);
        makeCmb($objForm, $arg, $db, $query, "KINDCD", $Row["KINDCD"], $extra, 1, $model, 1);

        //成績コンボ作成
        $query = knjj120aQuery::getClubRecord($model);
        makeCmb($objForm, $arg, $db, $query, "RECORDCD", $Row["RECORDCD"], "", 1, $model, 1);

        //記録テキストボックス
        $arg["data"]["DOCUMENT"] = knjCreateTextBox($objForm, $Row["DOCUMENT"], "DOCUMENT", 40, 40, "");

        //備考テキストボックス
        $arg["data"]["DETAIL_REMARK"] = knjCreateTextBox($objForm, $Row["DETAIL_REMARK"], "DETAIL_REMARK", 40, 40, "");

        //ボタン作成
        makeBtn($objForm, $arg, $model);

        //hidden作成
        makeHidden($objForm, $model);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"] = $objForm->get_finish();

        if (VARS::get("cmd") == "edit" || $model->cmd == "edit2" || $model->cmd == "edit3") {
        } else {
            if ($model->Properties["use_prg_schoolkind"] == "1") {
                $arg["reload"] = "parent.left_frame.location.href='knjj120aindex.php?cmd=list2&SEND_CLUBLIST=".$model->clublist2."&SCHKIND=".$model->schkind."';";
            } else {
                $arg["reload"] = "parent.left_frame.location.href='knjj120aindex.php?cmd=list2&SEND_CLUBLIST=".$model->clublist2."';";
            }
        }

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjj120aForm2.html", $arg);
    }
}
//リストtoリスト作成
function makeListToList(&$objForm, &$arg, $db, $model, $clubcd, $selectData, $max_len) {
    $optData = $optSelect = array();

    //生徒一覧
    $query = knjj120aQuery::getClubSchList($model, $clubcd);
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        if ($selectData[$row["VALUE"]] != "1") {
            //クラス名称調整
            $zenkaku = (strlen($row["HR_NAME"]) - mb_strlen($row["HR_NAME"])) / 2;
            $hankaku = ($zenkaku > 0) ? mb_strlen($row["HR_NAME"]) - $zenkaku : mb_strlen($row["HR_NAME"]);
            $len = $zenkaku * 2 + $hankaku;
            $hr_name = $row["HR_NAME"];
            for ($j=0; $j < ($max_len - ($zenkaku * 2 + $hankaku)); $j++) $hr_name .= "&nbsp;";

            $optData[]= array('label' => $hr_name." ".$row["ATTENDNO"]."番 ".$row["NAME_SHOW"],
                              'value' => $row["VALUE"]);
        }
    }
    $result->free();

    //対象生徒一覧
    foreach ($selectData as $key => $val) {
        list ($gha, $schregno) = explode('-', $key);
        $query = knjj120aQuery::getClubSchList($model, $clubcd, $schregno);
        $otpSet = $db->getRow($query, DB_FETCHMODE_ASSOC);
        if (is_array($otpSet)) {
            //クラス名称調整
            $zenkaku = (strlen($otpSet["HR_NAME"]) - mb_strlen($otpSet["HR_NAME"])) / 2;
            $hankaku = ($zenkaku > 0) ? mb_strlen($otpSet["HR_NAME"]) - $zenkaku : mb_strlen($otpSet["HR_NAME"]);
            $len = $zenkaku * 2 + $hankaku;
            $hr_name = $otpSet["HR_NAME"];
            for ($j=0; $j < ($max_len - ($zenkaku * 2 + $hankaku)); $j++) $hr_name .= "&nbsp;";

            $optSelect[]= array('label' => $hr_name." ".$otpSet["ATTENDNO"]."番 ".$otpSet["NAME_SHOW"],
                                'value' => $otpSet["VALUE"]);
        }
    }

    //所属生徒一覧
    $extra = "multiple style=\"width:230px\" width=\"230px\" ondblclick=\"move1('left')\"";
    $arg["data"]["CATEGORY_NAME"] = knjCreateCombo($objForm, "CATEGORY_NAME", "", $optData, $extra, 15);

    //対象生徒一覧
    $extra = "multiple style=\"width:230px\" width=\"230px\" ondblclick=\"move1('right')\"";
    $arg["data"]["CATEGORY_SELECTED"] = knjCreateCombo($objForm, "CATEGORY_SELECTED", "", $optSelect, $extra, 15);

    //対象選択ボタンを作成する
    $extra = "style=\"height:20px;width:40px\" onclick=\"moves('right');\"";
    $arg["button"]["btn_rights"] = knjCreateBtn($objForm, "btn_rights", ">>", $extra);
    //対象取消ボタンを作成する
    $extra = "style=\"height:20px;width:40px\" onclick=\"moves('left');\"";
    $arg["button"]["btn_lefts"] = knjCreateBtn($objForm, "btn_lefts", "<<", $extra);
    //対象選択ボタンを作成する
    $extra = "style=\"height:20px;width:40px\" onclick=\"move1('right');\"";
    $arg["button"]["btn_right1"] = knjCreateBtn($objForm, "btn_right1", "＞", $extra);
    //対象取消ボタンを作成する
    $extra = "style=\"height:20px;width:40px\" onclick=\"move1('left');\"";
    $arg["button"]["btn_left1"] = knjCreateBtn($objForm, "btn_left1", "＜", $extra);
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $model, $blank="") {
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
function makeBtn(&$objForm, &$arg, $model) {
    //ＣＳＶ処理ボタン
    $extra = "onClick=\" wopen('".REQUESTROOT."/X/KNJX_J120A/knjx_j120aindex.php?PROGRAMID=".PROGRAMID."&SEND_PRGID=KNJ120A&SEND_AUTH=".AUTHORITY."','SUBWIN2',0,0,screen.availWidth,screen.availHeight);\"";
    $arg["button"]["btn_csv"] = knjCreateBtn($objForm, "btn_csv", "ＣＳＶ処理", $extra);

    if (AUTHORITY < DEF_UPDATE_RESTRICT) {
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
    $arg["button"]["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", "onclick=\"return btn_submit('clear');\"");
    //終了ボタン
    $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", "onclick=\"closeWin();\"");
}

//Hidden作成
function makeHidden(&$objForm, $model) {
    knjCreateHidden($objForm, "cmd");
    knjCreateHidden($objForm, "SEND_CLUBLIST", $model->clublist2);
    knjCreateHidden($objForm, "selectdata");
    knjCreateHidden($objForm, "YEAR", CTRL_YEAR);
    knjCreateHidden($objForm, "SCHKIND", $model->schkind);
}
?>
