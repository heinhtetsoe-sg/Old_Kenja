<?php

require_once('for_php7.php');

class knjd132dIkkatsu {
    function main(&$model) {
        //オブジェクト作成
        $objForm        = new form;

        //フォーム作成
        $arg["start"]   = $objForm->get_start("sel", "POST", "knjd132dindex.php", "", "sel");

        $arg["jscript"] = "";

        //DB接続
        $db = Query::dbCheckOut();

        if (isset($model->warning)) {
            $Row = $model->field;
        } else {
            $Row = $db->getRow(knjd132dQuery::getHreportremarkDatIkkatsu($model), DB_FETCHMODE_ASSOC);
        }

        //チェックボックス
        for ($i = 0; $i < 5; $i++) {
            $name   = "CHECK".$i;
            $value  = "1";
            $extra  = "";
            $cheked = ($model->ikkatsu_data["check"][$i] == "1") ? " checked" : "";
            $extra  = "id=\"CHECK{$i}\"".$cheked;
            if ($i == 0) {
                $name   = "CHECK_ALL";
                $value  = "ALL";
                $chkAll = ($model->ikkatsu_data["check"][$i] == "ALL") ? " checked" : "";
                $extra  = "id=\"CHECK_ALL\" onClick=\"return check_all(this);\"".$chkAll;
            }
            $arg["data"][$name] = knjCreateCheckBox($objForm, $name, $value, $extra);
        }

        //テキストボックス
        //内容
        $extra = " id=\"TOTALSTUDYTIME\" onkeyup=\"charCount(this.value, {$model->totalstudytime_gyou}, ({$model->totalstudytime_moji} * 2), true);\"";
        $arg["data"]["TOTALSTUDYTIME"] = knjCreateTextArea($objForm, "TOTALSTUDYTIME", $model->totalstudytime_gyou, ($model->totalstudytime_moji * 2), "soft", $extra, $Row["TOTALSTUDYTIME"]);
        $arg["data"]["TOTALSTUDYTIME_COMMENT"] = "(全角".$model->totalstudytime_moji."文字X".$model->totalstudytime_gyou."行まで)";
        $extra  = "onclick=\"return btn_submit('teikei1');\"";
        $arg["btn_teikei1"] = knjCreateBtn($objForm, "btn_teikei1", "定型文選択", $extra);

        //取り組みのようす
        $extra = " id=\"REMARK1\" onkeyup=\"charCount(this.value, {$model->remark1_gyou}, ({$model->remark1_moji} * 2), true);\"";
        $arg["data"]["REMARK1"] = knjCreateTextArea($objForm, "REMARK1", $model->remark1_gyou, ($model->remark1_moji * 2), "soft", $extra, $Row["REMARK1"]);
        $arg["data"]["REMARK1_COMMENT"] = "(全角".$model->remark1_moji."文字X".$model->remark1_gyou."行まで)";
        $extra  = "onclick=\"return btn_submit('teikei2');\"";
        $arg["btn_teikei2"] = knjCreateBtn($objForm, "btn_teikei2", "定型文選択", $extra);

        //出席のようす備考
        $extra = "id=\"ATTENDREC_REMARK\" onkeyup=\"charCount(this.value, {$model->attendrec_remark_gyou}, ({$model->attendrec_remark_moji} * 2), true);\"";
        $arg["data"]["ATTENDREC_REMARK"] = knjCreateTextArea($objForm, "ATTENDREC_REMARK", $model->attendrec_remark_gyou, ($model->attendrec_remark_moji * 2), "soft", $extra, $Row["ATTENDREC_REMARK"]);
        $arg["data"]["ATTENDREC_REMARK_COMMENT"] = "(全角".$model->attendrec_remark_moji."文字X".$model->attendrec_remark_gyou."行まで)";

        //学校から
        $extra = "id=\"COMMUNICATION\" onkeyup=\"charCount(this.value, {$model->communication_gyou}, ({$model->communication_moji} * 2), true);\"";
        $arg["data"]["COMMUNICATION"] = knjCreateTextArea($objForm, "COMMUNICATION", $model->communication_gyou, ($model->communication_moji * 2), "soft", $extra, $Row["COMMUNICATION"]);
        $arg["data"]["COMMUNICATION_COMMENT"] = "(全角".$model->communication_moji."文字X".$model->communication_gyou."行まで)";

        //ボタン作成
        //更新ボタン
        $extra = "onclick=\"return doSubmit('ikkatsu_update')\"";
        $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);

        //戻るボタン
        $link = REQUESTROOT."/D/KNJD132D/knjd132dindex.php?cmd=back&ini2=1";
        $extra = "onclick=\"window.open('$link','_self');\"";
        $arg["button"]["btn_back"] = knjCreateBtn($objForm, "btn_back", "戻 る", $extra);

        //リスト作成
        //生徒一覧
        $opt_left = $opt_right = array();
        $array = explode(",", $model->ikkatsu_data["selectdata"]);
        //リストが空であれば置換処理選択時の生徒を加える
        if ($array[0]=="") $array[0] = $model->schregno;

        //生徒情報
        $result = $db->query(knjd132dQuery::GetStudent($model));
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            if (!in_array($row["SCHREGNO"], $array)){
                $opt_right[] = array("label" => $row["ATTENDNO"]."  ".$row["SCHREGNO"]."  ".$row["NAME_SHOW"],
                                     "value" => $row["SCHREGNO"]);
            } else {
                $opt_left[]  = array("label" => $row["ATTENDNO"]."  ".$row["SCHREGNO"]."  ".$row["NAME_SHOW"],
                                     "value" => $row["SCHREGNO"]);
            }
        }

        $result->free();

        //対象生徒
        $extra = "multiple STYLE=\"WIDTH:100%\" WIDTH=\"100%\" ondblclick=\"move('right','left_select','right_select',1)\"";
        $arg["main_part"]["LEFT_PART"] = knjCreateCombo($objForm, "left_select", "left", $opt_left, $extra, 20);

        //その他の生徒
        $extra = "multiple STYLE=\"WIDTH:100%\" WIDTH=\"100%\" ondblclick=\"move('left','left_select','right_select',1)\"";
        $arg["main_part"]["RIGHT_PART"] = knjCreateCombo($objForm, "right_select", "right", $opt_right, $extra, 20);

        //全追加
        $extra = "onclick=\"return move('sel_add_all','left_select','right_select',1);\"";
        $arg["main_part"]["SEL_ADD_ALL"] = knjCreateBtn($objForm, "sel_add_all", "≪", $extra);
        //追加
        $extra = "onclick=\"return move('left','left_select','right_select',1);\"";
        $arg["main_part"]["SEL_ADD"] = knjCreateBtn($objForm, "sel_add", "＜", $extra);
        //削除
        $extra = "onclick=\"return move('right','left_select','right_select',1);\"";
        $arg["main_part"]["SEL_DEL"] = knjCreateBtn($objForm, "sel_del", "＞", $extra);
        //全削除
        $extra = "onclick=\"return move('sel_del_all','left_select','right_select',1);\"";
        $arg["main_part"]["SEL_DEL_ALL"] = knjCreateBtn($objForm, "sel_del_all", "≫", $extra);

        $hr_name = $semeName = "";
        //年組名
        $hr_name = $db->getOne(knjd132dQuery::getHR_Name($model));
        //学期名
        $semeName = $db->getOne(knjd132dQuery::getSemeName($model->ikkatsuSeme));
        $arg["info"] = array("LEFTTOP"    =>  CTRL_YEAR."年度 ".$semeName,
                             "RIGHTTOP"   =>  sprintf(" 対象クラス  %s", $hr_name),
                             "LEFT_LIST"  => "対象者一覧",
                             "RIGHT_LIST" => "生徒一覧");

        //hidden
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "selectdata");
        knjCreateHidden($objForm, "SCHREGNO", $model->ikkatsu_data["selectdata"]);

        //DB切断
        Query::dbCheckIn($db);

        //インラインフレーム用Javascriptタグ生成
        $arg["IFRAME"] = View::setIframeJs();

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjd132dIkkatsu.html", $arg);
    }
}
?>
