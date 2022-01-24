<?php

require_once('for_php7.php');

class knjd132fForm1 {
    function main(&$model) {
        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("edit", "POST", "knjd132findex.php", "", "edit");

        //DB接続
        $db = Query::dbCheckOut();

        //学期カウント
        $model->semeCnt = $db->getOne(knjd132fQuery::getSemester("CNT"));

        //警告メッセージを表示しない場合
        $row1 = $row2 = array();
        if ((isset($model->schregno) && !isset($model->warning)) || !isset($model->schregno)) {
            for ($seme = 1; $seme <= $model->semeCnt; $seme++) {
                $rowA =  $db->getRow(knjd132fQuery::getRow($model, $seme), DB_FETCHMODE_ASSOC);
                $row1["SPECIALACTREMARK".$seme] = $rowA["SPECIALACTREMARK"];
            }
            $row2 = $db->getRow(knjd132fQuery::getRow($model, "9", "FRG"), DB_FETCHMODE_ASSOC);
            $row  = array_merge((array)$row1,(array)$row2);
            $arg["NOT_WARNING"] = 1;
        } else {
            $row =& $model->field;
        }

        $arg["SCHREGNO"] = $model->schregno;
        $arg["NAME"]     = $model->name;

        //学期名称
        $query = knjd132fQuery::getSemester();
        $result = $db->query($query);
        $cntFlg = 1;
        while ($seme = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            if ($cntFlg == "1") {
                $seme["ROWSPAN"] = $model->semeCnt;
            }
            //特別活動の記録
            $disabled = ($seme["SEMESTER"] == CTRL_SEMESTER) ? "" : "disabled";
            $extra = " id=\"SPECIALACTREMARK{$cntFlg}\" onkeyup=\"charCount(this.value, {$model->specialactremark_gyou}, ({$model->specialactremark_moji} * 2), true);\"".$disabled;
            $seme["SPECIALACTREMARK"] = knjCreateTextArea($objForm, "SPECIALACTREMARK".$cntFlg, $model->specialactremark_gyou, ($model->specialactremark_moji * 2), "soft", $extra, $row["SPECIALACTREMARK".$cntFlg]);
            //文字数コメント
            $seme["SPSETMOJI"] = "(全角で ".$model->specialactremark_moji."文字X".$model->specialactremark_gyou."行)";

            //反映用
            $target = "SPECIALACTREMARK".$cntFlg;

            //部活動選択ボタン
            $extra = "onclick=\"return sansyouWindow('club', '{$target}');\"";
            $seme["btn_club"] = knjCreateBtn($objForm, "btn_club".$cntFlg, "部活動選択", $extra.$disabled);
            //委員会選択ボタン
            $extra = "onclick=\"return sansyouWindow('committee', '{$target}');\"";
            $seme["btn_committee"] = knjCreateBtn($objForm, "btn_committee".$cntFlg, "委員会選択", $extra.$disabled);
            //記録備考選択ボタン
            if ($model->Properties["club_kirokubikou"] == 1) {
                $extra = "onclick=\"return sansyouWindow('clubhdetail', '{$target}');\"";
                $seme["btn_clubhdetail"] = knjCreateBtn($objForm, "btn_clubhdetail".$cntFlg, "記録備考選択", $extra.$disabled);
            }
            //検定選択ボタン
            $extra = "onclick=\"return sansyouWindow('qualified', '{$target}');\"";
            $seme["btn_qualified"] = knjCreateBtn($objForm, "btn_qualified".$cntFlg, "検定選択", $extra.$disabled);

            $cntFlg++;
            $arg["data"][] = $seme;
        }

        //道徳
        $extra = " id=\"MORAL\" onkeyup=\"charCount(this.value, {$model->moral_gyou}, ({$model->moral_moji} * 2), true);\"";
        $arg["data2"]["MORAL"] = knjCreateTextArea($objForm, "MORAL", $model->moral_gyou, ($model->moral_moji * 2), "soft", $extra, $row["MORAL"]);
        $arg["data2"]["MORALSETMOJI"] = "(全角で ".$model->moral_moji."文字X".$model->moral_gyou."行)";

        //備考
        $extra = " id=\"COMMUNICATION\" onkeyup=\"charCount(this.value, {$model->communication_gyou}, ({$model->communication_moji} * 2), true);\"";
        $arg["data2"]["COMMUNICATION"] = knjCreateTextArea($objForm, "COMMUNICATION", $model->communication_gyou, ($model->communication_moji * 2), "soft", $extra, $row["COMMUNICATION"]);
        $arg["data2"]["BIKOSETMOJI"] = "(全角で ".$model->communication_moji."文字X".$model->communication_gyou."行)";

        $arg["IFRAME"] = VIEW::setIframeJs();

        //ボタン
        //更新
        $extra = "onclick=\"return btn_submit('update');\"";
        $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);
        //更新後前の生徒へボタン
        $arg["button"]["btn_up_next"] = View::updateNext2($model, $objForm, $model->schregno, "SCHREGNO", "edit", "update");
        //取消
        $extra = "onclick=\"return btn_submit('clear');\"";
        $arg["button"]["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);
        //終了
        $extra = "onclick=\"closeWin();\"";
        $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);

        //hidden
        knjCreateHidden($objForm, "cmd", "VALUE");
        knjCreateHidden($objForm, "SCHREGNO", $model->schregno);
        knjCreateHidden($objForm, "useQualifiedMst", $model->Properties["useQualifiedMst"]);

        if (get_count($model->warning) == 0 && $model->cmd !="clear") {
            $arg["next"] = "NextStudent2(0);";
        } else if ($model->cmd =="clear") {
            $arg["next"] = "NextStudent2(1);";
        }

        //画面のリロード
        if ($model->cmd == "updEdit") {
            $arg["reload"] = "parent.left_frame.btn_submit('list');";
        }

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        View::toHTML($model, "knjd132fForm1.html", $arg);
    }
}
?>
