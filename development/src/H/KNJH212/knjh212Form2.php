<?php

require_once('for_php7.php');

class knjh212Form2 {

    function main(&$model) {

        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("edit", "POST", "knjh212index.php", "", "edit");

        //警告メッセージを表示しない場合
        //すでにあるデータの更新の場合
        if (isset($model->gakusekino) && isset($model->domicd) && isset($model->enterdate) && !isset($model->warning)) {
            $Row1 = knjh212Query::getDomitoryHistory_DatEdit($model);
        } else {
            $Row1 =& $model->field;
        }

        //DB接続
        $db = Query::dbCheckOut();

        //寮（左側で選ばれた寮に合わせる）
        $queryN = knjh212Query::getDomitoryNameList($model);
        $value_flg = false;
        $resultN = $db->query($queryN);
        while ($rowN = $resultN->fetchRow(DB_FETCHMODE_ASSOC)) {
            $optN[] = array('label' => $rowN["DOMI_NAME"],
                            'value' => $rowN["DOMI_CD"]);
            if ($model->SelectDomi === $rowN["DOMI_CD"]) $value_flg = true;
        }
        $resultN->free();

        $selectdomi = ($model->SelectDomi && $value_flg) ? $model->SelectDomi : $optN[0]["value"];
        //寮コード
        $arg["data"]["DOMI_CODE1"] = knjCreateTextBox($objForm, $selectdomi, "DOMI_CODE1", 5, 4, "disabled");

        //学生情報・入力番号検索ボタン・在校生検索ボタン
        if ($model->domiflg == "ON") {
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
        $querystu = knjh212Query::getStudent_data_One(CTRL_YEAR, CTRL_SEMESTER, $GAKUNO);
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
        $extra = "onclick=\"wopen('../../X/KNJXSEARCH2/index.php?PATH=/H/KNJH212/knjh212index.php&cmd=&target=KNJH212','search', 0, 0, 700, 600);\"";
        $arg["button"]["btn_zaigaku"] = knjCreateBtn($objForm, "btn_zaigaku", "在校生検索", $extra);

        //入寮日
        if (isset($Row1["DOMI_ENTDAY"])) {
            $arg["data"]["DOMI_ENTDAY"] = View::popUpCalendar($objForm, "DOMI_ENTDAY", str_replace("-","/",$Row1["DOMI_ENTDAY"]),"");
        } else {
            $arg["data"]["DOMI_ENTDAY"] = View::popUpCalendar($objForm, "DOMI_ENTDAY", "","");
        }

        //退寮日
        if (isset($Row1["DOMI_OUTDAY"])) {
            $arg["data"]["DOMI_OUTDAY"] = View::popUpCalendar($objForm, "DOMI_OUTDAY", str_replace("-","/",$Row1["DOMI_OUTDAY"]),"");
        } else {
            $arg["data"]["DOMI_OUTDAY"] = View::popUpCalendar($objForm, "DOMI_OUTDAY", "","");
        }

        //役職区分コンボボックス
        $queryR  = knjh212Query::getName_Data($model);
        $resultR = $db->query($queryR);
        $opt_rolecd = array();
        $opt_rolecd[0] = array("label" => "", "value" => "");
        while($rowR = $resultR->fetchRow(DB_FETCHMODE_ASSOC)){
            $opt_rolecd[] = array("label" => htmlspecialchars($rowR["NAME"]),
                                  "value" => $rowR["NAMECD2"]);
        }
        $resultR->free();

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

        //入寮一括更新ボタン
        $extra = "onclick=\"return btn_submit('subform1');\"";
        $arg["button"]["btn_subform1"] = KnjCreateBtn($objForm, "btn_subform1", "入寮一括更新", $extra);

        //退寮一括更新ボタン
        $extra = "onclick=\"return btn_submit('subform2');\"";
        $arg["button"]["btn_subform2"] = KnjCreateBtn($objForm, "btn_subform2", "退寮一括更新", $extra);

        //hidden作成
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "DOMI_CODEALL", $selectdomi);
        if (isset($Row1["UPDATED"])) {
            knjCreateHidden($objForm, "UPDATED", $Row1["UPDATED"]);
        }

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"] = $objForm->get_finish();

        if (VARS::get("cmd") != "edit" && VARS::post("cmd") != "search") {
            $arg["reload"] = "window.open('knjh212index.php?cmd=list&RELOADDOMI=".$selectdomi."','left_frame');";
        }

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjh212Form2.html", $arg); 
    }
}
?>
