<?php

require_once('for_php7.php');

//ビュー作成用クラス
class knje010cForm1 {
    function main(&$model) {
        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("edit", "POST", "knje010cindex.php", "", "edit");
        $db = Query::dbCheckOut();

        if (!isset($model->warning) && $model->cmd != 'reload3') {
            $query = knje010cQuery::selectQuery($model);
            $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
            $arg["NOT_WARNING"] = 1;
        } else {
            $row = $model->field;
        }

        if ($model->Properties["useMaruA_avg"] == "") {
            $arg["UnUseMaruA_avg"] = 1;
        }

        $arg["NAME_SHOW"] = $model->schregno."　".$model->name;

        //「総合的な学習の時間の内容・評価」 を表示するかしないかのフラグ
        if ($model->Properties["tyousasyoSougouHyoukaNentani"] != 1) {
            $arg["tyousasyoSougouHyoukaNentani"] = 1;
        } else {
            $arg["tyousasyoSougouHyoukaNentani_for_title"] = 1;
        }

        //読込みボタンが押された時の通知書より読込む
        if ($model->cmd == 'reload3') {
            $totalstudytimeArray = array();
            $totalstudyactArray  = array();
            $query = knje010cQuery::get_record_totalstudytime_dat($model);
            $db = Query::dbCheckOut();
            $result = $db->query($query);
            while ($total_row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                if ($total_row["TOTALSTUDYTIME"] != '') {
                    $totalstudytimeArray[] = $total_row["TOTALSTUDYTIME"];
                }
                if ($total_row["TOTALSTUDYACT"] != '') {
                    $totalstudyactArray[] = $total_row["TOTALSTUDYACT"];
                }
            }
            $row["TOTALSTUDYVAL"] = implode("\n", $totalstudytimeArray);
            $row["TOTALSTUDYACT"] = implode("\n", $totalstudyactArray);
            Query::dbCheckIn($db);
        }

        /******************/
        /* テキストエリア */
        /******************/
        //全体の行数をセット
        $checkgyousu = "10";
        //「総合的な学習の時間の内容・評価」 を表示するかしないかのフラグ
        if ($model->Properties["tyousasyoSougouHyoukaNentani"] != 1) {
            if ($model->cmd == "yomikomi") {
                $query = knje010cQuery::getYouroku($model);
                $resultYouroku = $db->query($query);
                $kaigyou = "";
                $row["TOTALSTUDYACT"] = "";
                $row["TOTALSTUDYVAL"] = "";
                while ($rowYouroku = $resultYouroku->fetchRow(DB_FETCHMODE_ASSOC)) {
                    $row["TOTALSTUDYACT"] .= $kaigyou.$rowYouroku["TOTALSTUDYACT"];
                    $row["TOTALSTUDYVAL"] .= $kaigyou.$rowYouroku["TOTALSTUDYVAL"];
                    $kaigyou = "\r\n";
                }
            }
            //活動内容
            $height = $model->totalstudyact_gyou * 13.5 + ($model->totalstudyact_gyou -1 ) * 3 + 5;
            $arg["TOTALSTUDYACT"] = KnjCreateTextArea($objForm, "TOTALSTUDYACT", $model->totalstudyact_gyou, ($model->totalstudyact_moji * 2 + 1), "soft", "style=\"height:{$height}px;\" onkeyup=\"charCount(this.value, $model->totalstudyact_gyou, ($model->totalstudyact_moji * 2), true);zentaicheck($checkgyousu);\" oncontextmenu =\"charCount(this.value, $model->totalstudyact_gyou, ($model->totalstudyact_moji * 2), true);zentaicheck($checkgyousu);\"", $row["TOTALSTUDYACT"]);
            $arg["TOTALSTUDYACT_TYUI"] = "(全角{$model->totalstudyact_moji}文字{$model->totalstudyact_gyou}行まで)";
            //評価
            $height = $model->totalstudyval_gyou * 13.5 + ($model->totalstudyval_gyou -1 ) * 3 + 5;
            $arg["TOTALSTUDYVAL"] = KnjCreateTextArea($objForm, "TOTALSTUDYVAL", $model->totalstudyval_gyou, ($model->totalstudyval_moji * 2 + 1), "soft", "style=\"height:{$height}px;\" onkeyup=\"charCount(this.value, $model->totalstudyval_gyou, ($model->totalstudyval_moji * 2), true);zentaicheck($checkgyousu);\" oncontextmenu =\"charCount(this.value, $model->totalstudyval_gyou, ($model->totalstudyval_moji * 2), true);zentaicheck($checkgyousu);\"", $row["TOTALSTUDYVAL"]);
            $arg["TOTALSTUDYVAL_TYUI"] = "(全角{$model->totalstudyval_moji}文字{$model->totalstudyval_gyou}行まで)";
            //指導要録より読込
            if ($model->Properties["tyousasyo_SidoYorokYoriYomikomiHyoujiFlg"] == "1") {
                $arg["tyousasyo_SidoYorokYoriYomikomiHyoujiFlg"] = "1";
                //プロパティ「tyousasyoSougouHyoukaNentani」が設定されている場合、帯内にボタンを移動する
                if ($model->Properties["tyousasyoSougouHyoukaNentani"] != "") {
                    $arg["SidoYorokYoriYomikomiHyouji_idou"] = "1";
                    $setname = "指導要録"."\n"."より読込";
                    $extra = " style=\"width:65px; text-align=left\"";
                } else {
                    $arg["SidoYorokYoriYomikomiHyouji_not_idou"] = "1";
                    $setname = "指導要録より読込";
                    $extra = "";
                }
            }
            $extra .= " onclick=\"return btn_submit('yomikomi');\"";
            $arg["button"]["btn_yomikomi"] = knjCreateBtn($objForm, "btn_yomikomi", $setname, $extra);
        }
        //備考
        $arg["REMARK"] = KnjCreateTextArea($objForm, "REMARK", 5, 83, "soft", "style=\"height:77px;\" onkeyup=\"charCount(this.value, 5, (41 * 2), true);zentaicheck($checkgyousu);\" oncontextmenu=\"charCount(this.value, 5, (41 * 2), true);zentaicheck($checkgyousu);\"", $row["REMARK"]);
        if ($model->schoolName == 'tottori') {
            $arg["REMARK_TYUI"] = "(全角41文字X4行まで)";
        } else {
            $arg["REMARK_TYUI"] = "(全角41文字X5行まで)";
        }

        /********************/
        /* チェックボックス */
        /********************/
        //学習成績概評チェックボックス
        $objForm->ae( array("type"      => "checkbox",
                            "name"      => "COMMENTEX_A_CD",
                            "checked"   => ($row["COMMENTEX_A_CD"]==1)? true:false,
                            "value"     => 1,
                            "extrahtml" => "id=\"comment\""));
        $arg["COMMENTEX_A_CD"] = $objForm->ge("COMMENTEX_A_CD");

        //特記事項なしチェックボックス
        $extra  = ($model->field["NO_COMMENTS"] == "1") ? "checked" : "";
        $extra .= " id=\"NO_COMMENTS\" onclick=\"return CheckRemark();\"";
        $arg["NO_COMMENTS"] = knjCreateCheckBox($objForm, "NO_COMMENTS", "1", $extra, "");

        //特記事項なし
        knjCreateHidden($objForm, "NO_COMMENTS_LABEL", $model->no_comments_label);
        $arg["NO_COMMENTS_LABEL"] = $model->no_comments_label;

        /**********/
        /* ボタン */
        /**********/
        //特別な活動～ボタンを作成する
        $extra = "onclick=\"return btn_submit('form2_first');\" style=\"width:520px\"";
        if ($model->Properties["tyousasyoSougouHyoukaNentani"] == 1) {
            $title = '出欠の記録 & 特別活動の記録 & 指導上参考になる諸事項 & 総合的な学習の時間';
        } else {
            $title = '出欠の記録 ＆ 特別活動の記録 ＆ 指導上参考になる諸事項';
        }
        $arg["btn_form2"] = knjCreateBtn($objForm, "btn_form2", $title, $extra);
        //通知書より読込み
        $extra = "onclick=\"return btn_submit('reload3');\"";
        $arg["btn_reload3"] = knjCreateBtn($objForm, "btn_reload3", "通知票より読込", $extra);
        //成績参照ボタンを作成する
        $extra = "onclick=\"return btn_submit('form3_first');\" style=\"width:70px\"";
        $arg["btn_form3"] = knjCreateBtn($objForm, "btn_form3", "成績参照", $extra);
        //指導要録参照画面ボタンを作成する
        if ($model->Properties["sidouyourokuSansyou"] == 1) {
            $extra = "onclick=\"return btn_submit('form6_first');\" style=\"width:90px\"";
        } else {
            $extra = "onclick=\"return btn_submit('form4_first');\" style=\"width:90px\"";
        }
        $arg["btn_form4"] = knjCreateBtn($objForm, "btn_form4", "指導要録参照", $extra);
        //更新ボタンを作成する
        $extra = "onclick=\"return btn_submit('update');\"";
        $arg["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);
        //更新後前の生徒へボタン
        $arg["btn_up_next"]    = View::updateNext($model, $objForm, 'btn_update');
        //取消しボタンを作成する
        $extra = "onclick=\"return btn_submit('reset')\"";
        $arg["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);
        //終了ボタンを作成する
        $extra = "onclick=\"closeWin();\"";
        $arg["btn_back"] = knjCreateBtn($objForm, "btn_back", "終 了", $extra);

        //セキュリティーチェック
        $securityCnt = $db->getOne(knje010cQuery::getSecurityHigh());
        $csvSetName = "CSV";
        if ($model->Properties["useXLS"]) {
            $csvSetName = "エクセル";
        }
        if ($model->getPrgId || !$model->Properties["useXLS"] || $securityCnt == 0) {
            //データCSVボタン
            $extra = "onClick=\" wopen('".REQUESTROOT."/X/KNJX190/knjx190index.php?program_id=".PROGRAMID."&mode={$model->mode}&SEND_PRGID=KNJE010C&SEND_AUTH={$model->auth}','SUBWIN2',0,0,screen.availWidth,screen.availHeight);\"";
            $arg["button"]["btn_check1"] = knjCreateBtn($objForm, "btn_check1", "データ".$csvSetName, $extra);
            //ヘッダデータCSVボタン
            $extra = "onClick=\" wopen('".REQUESTROOT."/X/KNJX191/knjx191index.php?program_id=".PROGRAMID."&mode={$model->mode}&SEND_PRGID=KNJE010C&SEND_AUTH={$model->auth}','SUBWIN2',0,0,screen.availWidth,screen.availHeight);\"";
            $arg["button"]["btn_check2"] = knjCreateBtn($objForm, "btn_check2", "ヘッダデータ".$csvSetName, $extra);
        }

        /**********/
        /* hidden */
        /**********/
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "nextURL", $model->nextURL);
        knjCreateHidden($objForm, "SCHREGNO", $model->schregno);
        knjCreateHidden($objForm, "mode", $model->mode);
        knjCreateHidden($objForm, "GRD_YEAR", $model->grd_year);
        knjCreateHidden($objForm, "GRD_SEMESTER", $model->grd_semester);
        knjCreateHidden($objForm, "PROGRAMID", PROGRAMID);
        knjCreateHidden($objForm, "useSyojikou3", $model->Properties["useSyojikou3"]);
        knjCreateHidden($objForm, "tyousasyoTokuBetuFieldSize", $model->Properties["tyousasyoTokuBetuFieldSize"]);

        if(get_count($model->warning)== 0 && $model->cmd !="reset") {
            $arg["next"] = "NextStudent(0);";
        } elseif($model->cmd =="reset") {
            $arg["next"] = "NextStudent(1);";
        }

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();
        //インラインフレーム用Javascriptタグ生成
        $arg["IFRAME"] = View::setIframeJs();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knje010cForm1.html", $arg);
    }
}
?>
