<?php

require_once('for_php7.php');

//ビュー作成用クラス
class knje010cForm2 {
    function main(&$model) {
        $objForm = new form;
        $arg = array();
        //フォーム作成
        $arg["start"]   = $objForm->get_start("form2", "POST", "knje010cindex.php", "", "form2");

        $arg["NAME_SHOW"] = $model->schregno."　".$model->name;

        //DB接続
        $db = Query::dbCheckOut();

        //SQL文発行
        //年度(年次)取得コンボ
        if ($model->cmd == "form2_first") { // すでに別の生徒を開いていた場合そのときの値が保持されているので
            $model->annual["YEAR"]   = "";  // 最初の呼出ならば、年度と年次をクリアする
            $model->annual["ANNUAL"] = "";
        }
        $opt = array();
        $disabled = "disabled";
        $query = knje010cQuery::selectQueryAnnual_knje010cForm2($model);
        $result = $db->query($query);
        while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            $opt[] = array("label" => $row["YEAR"] ."年度　" .(int) $row["ANNUAL"] ."学年(年次)",
                           "value" => $row["YEAR"] ."," .$row["ANNUAL"]
                          );
            if (!isset($model->annual["YEAR"]) || ($model->cmd == "form2_first" && 
               (($model->mode == "ungrd" && $model->exp_year == $row["YEAR"]) || ($model->mode == "grd" && $model->grd_year == $row["YEAR"])))){
                $model->annual["YEAR"]   = $row["YEAR"];
                $model->annual["ANNUAL"] = $row["ANNUAL"];
            }

            $disabled = "";
        }
        if (!strlen($model->annual["YEAR"]) || !strlen($model->annual["ANNUAL"])) {
            list($model->annual["YEAR"], $model->annual["ANNUAL"]) = preg_split("/,/", $opt[0]["value"]);
        }
        if (!isset($model->warning) && $model->cmd != 'reload4') {
            if ($model->cmd == "reload2") {
                $query = knje010cQuery::selectQuery_Htrainremark_Dat($model);
                $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
            } else {
                if ($model->cmd !== 'torikomi3') {
                    $query = knje010cQuery::selectQueryForm2($model);
                    $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
                } else {
                    $row = $model->field2;
                }
            }
            if ($model->cmd == "reload2" && $model->Properties["useSyojikou3"] == "1") {
                $row["TRAIN_REF1"] = $model->field2["TRAIN_REF1"];
                $row["TRAIN_REF2"] = $model->field2["TRAIN_REF2"];
                $row["TRAIN_REF3"] = $model->field2["TRAIN_REF3"];
            }
        } else {
            $row = $model->field2;
        }

        //指導要録データ、調査書旧データ
        if ($model->Properties["useSyojikou3"] == "1") {
            $query = knje010cQuery::sansyou_data($model);
            $sansyou = $db->getRow($query, DB_FETCHMODE_ASSOC);
            $row["TRAIN_REF"]   = $sansyou["TRAIN_REF"];
            $row["TOTALREMARK"] = $sansyou["TOTALREMARK"];
        }

        if ($model->cmd == 'reload4') {
            $totalstudytimeArray = array();
            $totalstudyactArray  = array();
            $query = knje010cQuery::get_record_totalstudytime_dat($model);
            $db = Query::dbCheckOut();
            $result = $db->query($query);
            while ($total_row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                if ($total_row["TOTALSTUDYACT"] != '') {
                    $totalstudyactArray[] = $total_row["TOTALSTUDYACT"];
                }
                if ($total_row["TOTALSTUDYTIME"] != '') {
                    $totalstudytimeArray[] = $total_row["TOTALSTUDYTIME"];
                }
            }
            $row["TOTALSTUDYACT"] = implode("\n", $totalstudyactArray);
            $row["TOTALSTUDYVAL"] = implode("\n", $totalstudytimeArray);
            Query::dbCheckIn($db);
        }

        $objForm->ae( array("type"       => "select",
                            "name"       => "ANNUAL",
                            "size"       => "1",
                            "value"      => $model->annual["YEAR"] ."," .$model->annual["ANNUAL"],
                            "extrahtml"  => "onChange=\"return btn_submit('form2');\"",
                            "options"    => $opt));

        $arg["ANNUAL"] = $objForm->ge("ANNUAL");
        
        //出欠の記録備考取込
        if ($model->cmd === 'torikomi3') {
            $set_remark = knje010cQuery::getSemesRemark($model, $db, $model->annual["YEAR"]);
            $row["ATTENDREC_REMARK"] = $set_remark;
        }

        /******************/
        /* テキストエリア */
        /******************/
        //出欠の記録備考
        $arg["ATTENDREC_REMARK"] = KnjCreateTextArea($objForm, "ATTENDREC_REMARK", ($model->attendrec_remark_gyou + 1), ($model->attendrec_remark_moji * 2 + 1), "soft", "", $row["ATTENDREC_REMARK"]);
        $arg["ATTENDREC_REMARK_TYUI"] = "(全角{$model->attendrec_remark_moji}文字{$model->attendrec_remark_gyou}行まで)";
        //特別活動の記録
        $arg["SPECIALACTREC"] = KnjCreateTextArea($objForm, "SPECIALACTREC", ($model->specialactrec_gyou + 1), ($model->specialactrec_moji * 2 + 1), "soft", "", $row["SPECIALACTREC"]);
        $arg["SPECIALACTREC_TYUI"] = "(全角{$model->specialactrec_moji}文字{$model->specialactrec_gyou}行まで)";
        //指導上参考となる諸事項
        if ($model->Properties["useSyojikou3"] == "1") {
            $arg["useSyojikou3"] = $model->Properties["useSyojikou3"];
            $height1 = $model->train_ref1_gyou * 13.5 + ($model->train_ref1_gyou -1 ) * 3 + 5;
            $height2 = $model->train_ref2_gyou * 13.5 + ($model->train_ref2_gyou -1 ) * 3 + 5;
            $height3 = $model->train_ref3_gyou * 13.5 + ($model->train_ref3_gyou -1 ) * 3 + 5;
            $arg["TRAIN_REF1"] = KnjCreateTextArea($objForm, "TRAIN_REF1", ($model->train_ref1_gyou + 1), ($model->train_ref1_moji * 2 + 1), "soft", "style=\"height:{$height1}px;\"", $row["TRAIN_REF1"]);
            $arg["TRAIN_REF2"] = KnjCreateTextArea($objForm, "TRAIN_REF2", ($model->train_ref2_gyou + 1), ($model->train_ref2_moji * 2 + 1), "soft", "style=\"height:{$height2}px;\"", $row["TRAIN_REF2"]);
            $arg["TRAIN_REF3"] = KnjCreateTextArea($objForm, "TRAIN_REF3", ($model->train_ref3_gyou + 1), ($model->train_ref3_moji * 2 + 1), "soft", "style=\"height:{$height3}px;\"", $row["TRAIN_REF3"]);
            $arg["TRAIN_REF"]  = KnjCreateTextArea($objForm, "TRAIN_REF", 5, 83, "soft", "style=\"background-color:#D0D0D0;height:60px;\"", $row["TRAIN_REF"]);
            $arg["TOTALREMARK"]  = KnjCreateTextArea($objForm, "TOTALREMARK", 5, 83, "soft", "style=\"background-color:#D0D0D0;height:60px;\"", $row["TOTALREMARK"]);
            $arg["COLSPAN2"] = "colspan=\"2\"";
            $arg["COLSPAN_CHANGE"] = "colspan=\"3\"";
            $arg["TRAIN_REF1_COMMENT"] = "(全角{$model->train_ref1_moji}文字{$model->train_ref1_gyou}行まで)";
            $arg["TRAIN_REF2_COMMENT"] = "(全角{$model->train_ref2_moji}文字{$model->train_ref2_gyou}行まで)";
            $arg["TRAIN_REF3_COMMENT"] = "(全角{$model->train_ref3_moji}文字{$model->train_ref3_gyou}行まで)";
        } else {
            $arg["no_useSyojikou3"] = '1';
            $arg["COLSPAN_TRAIN_REF"] = "colspan=\"2\"";
            $arg["COLSPAN_CHANGE"] = "colspan=\"2\"";
            if ($model->Properties["tyousasyoTokuBetuFieldSize"] == 1) {
                $arg["TRAIN_REF"] = KnjCreateTextArea($objForm, "TRAIN_REF", 7, 117, "soft", "style=\"height:105px;\"", $row["TRAIN_REF"]);
                $arg["TRAIN_REF_COMMENT"] = "(全角58文字X7行まで)";
            } else {
                $arg["TRAIN_REF"] = KnjCreateTextArea($objForm, "TRAIN_REF", 5, 83, "soft", "style=\"height:77px;\"", $row["TRAIN_REF"]);
                $arg["TRAIN_REF_COMMENT"] = "(全角41文字X5行まで)";
            }
        }
        //「総合的な学習の時間の内容・評価」 を表示するかしないかのフラグ
        if ($model->Properties["tyousasyoSougouHyoukaNentani"] == 1) {
            //HTML側で表示・非表示の判定に使う
            $arg["tyousasyoSougouHyoukaNentani"] = 1;
            //活動内容
            $height = $model->totalstudyact_gyou * 13.5 + ($model->totalstudyact_gyou -1 ) * 3 + 5;
            $arg["TOTALSTUDYACT"] = KnjCreateTextArea($objForm, "TOTALSTUDYACT", $model->totalstudyact_gyou, ($model->totalstudyact_moji * 2 + 1), "soft", "style=\"height:{$height}px;\"", $row["TOTALSTUDYACT"]);
            $arg["TOTALSTUDYACT_TYUI"] = "(全角{$model->totalstudyact_moji}文字{$model->totalstudyact_gyou}行まで)";
            //評価
            $height = $model->totalstudyval_gyou * 13.5 + ($model->totalstudyval_gyou -1 ) * 3 + 5;
            $arg["TOTALSTUDYVAL"] = KnjCreateTextArea($objForm, "TOTALSTUDYVAL", $model->totalstudyval_gyou, ($model->totalstudyval_moji * 2 + 1), "soft", "style=\"height:{$height}px;\"", $row["TOTALSTUDYVAL"]);
            $arg["TOTALSTUDYVAL_TYUI"] = "(全角{$model->totalstudyval_moji}文字{$model->totalstudyval_gyou}行まで)";
        }

        /**********/
        /* ボタン */
        /**********/
        //生徒指導要録より読込ボタンを作成する
        $extra = "onclick=\" return btn_submit('reload2');\"";
        $arg["btn_reload2"] = KnjCreateBtn($objForm, "btn_reload2", "生徒指導要録より読込", $extra);
        //部活動参照ボタンを作成する
        $extra = $disabled ." onclick=\" return btn_submit('subform3');\"";
        $arg["btn_club"] = KnjCreateBtn($objForm, "btn_club", "部活動参照", $extra);
        //委員会参照ボタンを作成する
        $extra = $disabled ." onclick=\" return btn_submit('subform4');\"";
        $arg["btn_committee"] = KnjCreateBtn($objForm, "btn_committee", "委員会参照", $extra);
        //資格参照
        $extra = $disabled ." onclick=\"return btn_submit('subform5');\""; //KNJA120,KNJA120A の名残でform5とします。
        $arg["button"]["SIKAKU_SANSYO"] = knjCreateBtn($objForm, "SIKAKU_SANSYO", "資格参照", $extra);
        //出欠備考参照ボタン
        $sdate = $model->annual["YEAR"].'-04-01';
        $edate = ($model->annual["YEAR"]+1).'-03-31';
        if ($model->Properties["useAttendSemesRemarkDat"] == 1) {
            //まとめ出欠備考を取込みへ変更する
            if ($model->Properties["useTorikomiAttendSemesRemarkDat"] == 1) {
                $setname = 'まとめ出欠備考取込';
                $extra = $disabled ." style=\"color:#1E90FF;font:bold\" onclick=\"return btn_submit('torikomi3');\"";
            } else {
                $setname = 'まとめ出欠備考参照';
                $extra = $disabled ." onclick=\"loadwindow('../../X/KNJXATTEND_REMARK/index.php?YEAR={$model->annual["YEAR"]}&SCHREGNO={$model->schregno}&SDATE={$sdate}&EDATE={$edate}&SEMESFLG=1',0,document.documentElement.scrollTop || document.body.scrollTop,420,300);return;\"";
            }
            $arg["SANSYO"] = KnjCreateBtn($objForm, "SANSYO", $setname, $extra);
        } else {
            $extra = $disabled ." onclick=\"loadwindow('../../X/KNJXATTEND_REMARK/index.php?YEAR={$model->annual["YEAR"]}&SCHREGNO={$model->schregno}&SDATE={$sdate}&EDATE={$edate}',0,document.documentElement.scrollTop || document.body.scrollTop,420,300);return;\"";
            $arg["SANSYO"] = KnjCreateBtn($objForm, "SANSYO", "日々出欠備考参照", $extra);
        }
        //要録の出欠備考参照ボタン
        $extra = $disabled ." onclick=\"loadwindow('../../X/KNJXATTEND_HTRAINREMARK/index.php?YEAR={$model->annual["YEAR"]}&SCHREGNO={$model->schregno}',0,document.documentElement.scrollTop || document.body.scrollTop,360,180);return;\" style=\"width:210px;\"";
        $arg["YOROKU_SANSYO"] = KnjCreateBtn($objForm, "YOROKU_SANSYO", "要録の出欠の記録備考参照", $extra);
        //更新ボタンを作成する
        $extra = $disabled ." onclick=\"return btn_submit('update2');\"";
        $arg["btn_update"] = KnjCreateBtn($objForm, "btn_update", "更 新", $extra);
        //クリアボタンを作成する
        $extra = $disabled ." onclick=\"return btn_submit('reset')\"";
        $arg["btn_reset"] = KnjCreateBtn($objForm, "btn_reset", "取 消", $extra);
        //終了ボタンを作成する
        $extra = "onclick=\"return parent.closeit()\"";
        $arg["btn_back"] = KnjCreateBtn($objForm, "btn_back", "戻 る", $extra);
        //「総合的な学習の時間の内容・評価」 を表示するかしないかのフラグ
        if ($model->Properties["tyousasyoSougouHyoukaNentani"] == 1) {
            //通知書より読込み
            $extra = "onclick=\"return btn_submit('reload4');\"";
            $arg["btn_reload4"] = knjCreateBtn($objForm, "btn_reload4", "通知票より読込", $extra);
        }

        /**********/
        /* hidden */
        /**********/
        knjCreateHidden($objForm, "cmd");

        //DB切断
        Query::dbCheckIn($db);
        $arg["finish"]  = $objForm->get_finish();
        //インラインフレーム用Javascriptタグ生成
        $arg["IFRAME"] = View::setIframeJs();
        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knje010cForm2.html", $arg);
    }
}
?>