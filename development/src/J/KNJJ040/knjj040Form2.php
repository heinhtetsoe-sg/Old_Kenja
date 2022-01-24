<?php

require_once('for_php7.php');

class knjj040Form2 {

    function main(&$model) {

        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("edit", "POST", "knjj040index.php", "", "edit");

        //警告メッセージを表示しない場合
        //すでにあるデータの更新の場合
        if (isset($model->gakusekino) && isset($model->clubcd) && isset($model->enterdate) && !isset($model->warning)) {
            $Row1 = knjj040Query::getClubHistory_DatEdit($model);
        } else {
            $Row1 =& $model->field;
        }

        //DB接続
        $db = Query::dbCheckOut();

        //部クラブ（左側で選ばれた部クラブに合わせる）
        $queryN = knjj040Query::getClubNameList($model);
        $value_flg = false;
        $resultN = $db->query($queryN);
        while ($rowN = $resultN->fetchRow(DB_FETCHMODE_ASSOC)) {
            $optN[] = array('label' => $rowN["CLUBNAME"],
                            'value' => $rowN["CLUBCD"]);
            if ($model->SelectClub === $rowN["CLUBCD"]) $value_flg = true;
        }
        $resultN->free();

        $selectclub = ($model->SelectClub && $value_flg) ? $model->SelectClub : $optN[0]["value"];
        //部クラブコード
        $arg["data"]["CLUBCODE1"] = knjCreateTextBox($objForm, $selectclub, "CLUBCODE1", 5, 4, "disabled");

        //学生情報・入力番号検索ボタン・在校生検索ボタン
        if ($model->clubflg == "ON") {
            $GAKUNO = "";  //学籍番号クリア
            $Row1 = "";    //詳細をクリアする
        } else {
            if (VARS::get("cmd") == "") {   //検索ボタンが押された場合
                $GAKUNO = $model->field["SCHREGNO"];

                if (VARS::post("cmd") == "update" || VARS::post("cmd") == "add") {
                } else {
                    $Row1 = "";     //詳細をクリアする
                }
            } else if (VARS::get("cmd") == "edit") {    //左側で選択されたレコードの詳細の場合
                $GAKUNO = $model->gakusekino;
            }
        }

        //生徒の情報取得
        $querystu = knjj040Query::getStudent_data_One(CTRL_YEAR, CTRL_SEMESTER, $GAKUNO);
        $resultstu = $db->query($querystu);
        $rowstu = $resultstu->fetchRow(DB_FETCHMODE_ASSOC);
        $resultstu->free();

        //学籍番号
        $arg["data"]["SCHREGNO"] = knjCreateTextBox($objForm, $GAKUNO, "SCHREGNO", 9, 8, "");
        //氏名
        $arg["data"]["NAME"] = isset($rowstu["STUDENTNAME"]) ? $rowstu["STUDENTNAME"] : "";
        //年組
        $arg["data"]["HR_CLASS"] = isset($rowstu["NENKUMI"]) ? $rowstu["NENKUMI"] : "";

        //入力番号検索ボタン
        $extra = "onclick=\"btn_submit('search');\"";
        $arg["button"]["btn_input"] = knjCreateBtn($objForm, "btn_input", "入力番号検索", $extra);

        //在学生検索ボタン
        $extra = "onclick=\"wopen('../../X/KNJXSEARCH2/index.php?PATH=/J/KNJJ040/knjj040index.php&cmd=&target=KNJJ040&SEND_SchoolKind={$model->schkind}','search', 0, 0, 700, 600);\"";
        $arg["button"]["btn_zaigaku"] = knjCreateBtn($objForm, "btn_zaigaku", "在校生検索", $extra);

        //入部日付
        if (isset($Row1["SDATE"])) {
            $arg["data"]["SDATE"] = View::popUpCalendar($objForm, "SDATE", str_replace("-","/",$Row1["SDATE"]),"");
        } else {
            $arg["data"]["SDATE"] = View::popUpCalendar($objForm, "SDATE", "","");
        }

        //退部日付
        if (isset($Row1["EDATE"])) {
            $arg["data"]["EDATE"] = View::popUpCalendar($objForm, "EDATE", str_replace("-","/",$Row1["EDATE"]),"");
        } else {
            $arg["data"]["EDATE"] = View::popUpCalendar($objForm, "EDATE", "","");
        }

        //役職区分コンボボックス
        $queryR  = knjj040Query::getName_Data($model);
        $resultR = $db->query($queryR);
        $opt_rolecd = array();
        $opt_rolecd[0] = array("label" => "", "value" => "");
        while($rowR = $resultR->fetchRow(DB_FETCHMODE_ASSOC)){
            $opt_rolecd[] = array("label" => htmlspecialchars($rowR["NAME"]),
                                  "value" => $rowR["NAMECD2"]);
        }
        $resultR->free();

        $YAKU = (isset($Row1["EXECUTIVECD"])) ? $Row1["EXECUTIVECD"] : $opt_rolecd[0]["vakue"];
        $arg["data"]["EXECUTIVECD"] = knjCreateCombo($objForm, "EXECUTIVECD", $YAKU, $opt_rolecd, "", 1);

        //備考
        $extra = "";
        if (isset($Row1["REMARK"])) {
            $arg["data"]["REMARK"] = knjCreateTextBox($objForm, $Row1["REMARK"], "REMARK", 40, 40, $extra);
        } else {
            $arg["data"]["REMARK"] = knjCreateTextBox($objForm, "", "REMARK", 40, 40, $extra);
        }

        //追加ボタン
        $extra = (AUTHORITY == DEF_UPDATABLE || AUTHORITY == DEF_UPDATE_RESTRICT) ? "onclick=\"return btn_submit('add')\"" : "disabled";
        $arg["button"]["btn_add"] = knjCreateBtn($objForm, "btn_add", "登 録", $extra);
        //更新ボタン
        $extra = (AUTHORITY == DEF_UPDATABLE || AUTHORITY == DEF_UPDATE_RESTRICT) ? "onclick=\"return btn_submit('update')\"" : "disabled";
        $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_udpate", "更 新", $extra);
        //削除ボタン
        $extra = (AUTHORITY == DEF_UPDATABLE || AUTHORITY == DEF_UPDATE_RESTRICT) ? "onclick=\"return btn_submit('delete')\"" : "disabled";
        $arg["button"]["btn_del"] = knjCreateBtn($objForm, "btn_del", "削 除", $extra);
        //取消ボタン
        $extra = "onclick=\"return ShowConfirm('edit')\"";
        $arg["button"]["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);
        //終了ボタン
        $extra = "onclick=\"closeWin();\"";
        $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);

        //記録詳細入力ボタン
        $extra = ($GAKUNO) ? "onclick=\" wopen('".REQUESTROOT."/X/KNJXCLUB_DETAIL/knjxclub_detailindex.php?PROGRAMID=".PROGRAMID."&SCHREGNO=".$GAKUNO."&G_CLUBCD=".$selectclub."&SEND_schKind={$model->schkind}','SUBWIN2',0,0,screen.availWidth,screen.availHeight);\"" : "disabled";
        $arg["button"]["btn_detail"] = KnjCreateBtn($objForm, "btn_detail", "記録備考入力", $extra);

        //ＣＳＶ処理ボタン
        $extra = "onClick=\" wopen('".REQUESTROOT."/X/KNJX_J040/knjx_j040index.php?PROGRAMID=".PROGRAMID."&SEND_PRGID=KNJJ040&SEND_AUTH=".AUTHORITY."&SEND_schKind={$model->schkind}','SUBWIN2',0,0,screen.availWidth,screen.availHeight);\"";
        $arg["button"]["btn_csv"] = knjCreateBtn($objForm, "btn_csv", "ＣＳＶ処理", $extra);

        //入部一括更新ボタン
        $extra = "onclick=\"return btn_submit('subform1');\"";
        $arg["button"]["btn_subform1"] = KnjCreateBtn($objForm, "btn_subform1", "入部一括更新", $extra);

        //退部一括更新ボタン
        $extra = "onclick=\"return btn_submit('subform2');\"";
        $arg["button"]["btn_subform2"] = KnjCreateBtn($objForm, "btn_subform2", "退部一括更新", $extra);

        //hidden作成
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "CLUBCODEALL", $selectclub);
        if (isset($Row1["UPDATED"])) {
            knjCreateHidden($objForm, "UPDATED", $Row1["UPDATED"]);
        }

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"] = $objForm->get_finish();

        if (VARS::get("cmd") != "edit" && VARS::post("cmd") != "search") {
            $arg["reload"] = "window.open('knjj040index.php?cmd=list&RELOADCLUB=".$selectclub."','left_frame');";
        }

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjj040Form2.html", $arg); 
    }
}
?>
