<?php

require_once('for_php7.php');

//ビュー作成用クラス
class knje012yForm1 {
    function main(&$model) {

        $objForm = new form;

        //フォーム作成
        $arg["start"]   = $objForm->get_start("edit", "POST", "knje012yindex.php", "", "edit");

        //DB接続
        $db = Query::dbCheckOut();

        //年度(年次)取得コンボ
        if ($model->form1_first == "on") { // すでに別の生徒を開いていた場合そのときの値が保持されているので
            $model->annual["YEAR"]   = "";  // 最初の呼出ならば、年度と年次をクリアする
            $model->annual["ANNUAL"] = "";
        }
        $opt = array();
        $disabled = "disabled";
        $query = knje012yQuery::getYearAnnual($model);
        $result = $db->query($query);
        while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            $opt[] = array("label" => $row["YEAR"] ."年度　" .(int) $row["ANNUAL"] ."学年(年次)",
                           "value" => $row["YEAR"] ."," .$row["ANNUAL"]
                          );
            if (!isset($model->annual["YEAR"]) || ($model->form1_first == "on" && 
               (($model->mode == "ungrd" && $model->exp_year == $row["YEAR"]) || ($model->mode == "grd" && $model->grd_year == $row["YEAR"])))) {
                $model->annual["YEAR"]   = $row["YEAR"];
                $model->annual["ANNUAL"] = $row["ANNUAL"];
            }
            $disabled = "";
        }
        if (!strlen($model->annual["YEAR"]) || !strlen($model->annual["ANNUAL"])) {
            list($model->annual["YEAR"], $model->annual["ANNUAL"]) = preg_split("/,/", $opt[0]["value"]);
        }
        $value = $model->annual["YEAR"] ."," .$model->annual["ANNUAL"];
        $extra = "onChange=\"return btn_submit('edit');\"";
        $arg["ANNUAL"] = knjCreateCombo($objForm, "ANNUAL", $value, $opt, $extra, "1");

        //1レコード取得
        if (!isset($model->warning) && $model->cmd != 'reload3') {
            //DAT
            $query = knje012yQuery::getHexamEntremarkDat($model);
            $Row = $db->getRow($query, DB_FETCHMODE_ASSOC);
            //HDAT
            $query = knje012yQuery::getHexamEntremarkHdat($model);
            $Row2 = $db->getRow($query, DB_FETCHMODE_ASSOC);
            $Row["REMARK"] = $Row2["REMARK"];
            $arg["NOT_WARNING"] = 1;
        } else {
            $Row = $model->field;
        }
        //指導要録所見データ
        $query = knje012yQuery::getHtrainremarkDat($model);
        $sidouRow = $db->getRow($query, DB_FETCHMODE_ASSOC);

        //氏名
        $arg["NAME_SHOW"] = $model->schregno."　".$model->name;

        /******************/
        /* テキストエリア */
        /******************/
        //３．指導要録所見データ(表示)(全角20文字X4行)
        $moji = 20;
        $gyou = 4;
        $name = "SIDOU_TOTALSTUDYVAL";
        $cols = $moji * 2 + 1;//幅
        $height = $gyou * 13.5 + ($gyou - 1) * 3 + 5;//高さ
        $extra = "style=\"background-color:#D0D0D0;height:{$height}px;\"";
        $arg[$name] = knjCreateTextArea($objForm, $name, $gyou, $cols, "soft", $extra, $sidouRow[$name]);
        //３．総合的な学習の時間の記録(全角31文字X4行)
        $moji = 31;
        $gyou = 4;
        $name = "TOTALSTUDYVAL";
        $cols = $moji * 2 + 1;//幅
        $height = $gyou * 13.5 + ($gyou - 1) * 3 + 5;//高さ
        $extra = "style=\"height:{$height}px;\"";
        $arg[$name] = knjCreateTextArea($objForm, $name, $gyou, $cols, "soft", $extra, $Row[$name]);
        $arg[$name."_TYUI"] = "(全角{$moji}文字{$gyou}行まで)";
        //４．特別活動等の記録
        //学級活動(全角8文字X4行)
        $moji = 8;
        $gyou = 4;
        $name = "CALSSACT";
        $cols = $moji * 2 + 1;//幅
        $height = $gyou * 13.5 + ($gyou - 1) * 3 + 5;//高さ
        $extra = "style=\"height:{$height}px;\"";
        $arg[$name] = knjCreateTextArea($objForm, $name, $gyou, $cols, "soft", $extra, $Row[$name]);
        $arg[$name."_TYUI"] = "(全角{$moji}文字{$gyou}行まで)";
        //生徒会活動(全角8文字X4行)
        $moji = 8;
        $gyou = 4;
        $name = "STUDENTACT";
        $cols = $moji * 2 + 1;//幅
        $height = $gyou * 13.5 + ($gyou - 1) * 3 + 5;//高さ
        $extra = "style=\"height:{$height}px;\"";
        $arg[$name] = knjCreateTextArea($objForm, $name, $gyou, $cols, "soft", $extra, $Row[$name]);
        $arg[$name."_TYUI"] = "(全角{$moji}文字{$gyou}行まで)";
        //学校行事(全角8文字X4行)
        $moji = 8;
        $gyou = 4;
        $name = "SCHOOLEVENT";
        $cols = $moji * 2 + 1;//幅
        $height = $gyou * 13.5 + ($gyou - 1) * 3 + 5;//高さ
        $extra = "style=\"height:{$height}px;\"";
        $arg[$name] = knjCreateTextArea($objForm, $name, $gyou, $cols, "soft", $extra, $Row[$name]);
        $arg[$name."_TYUI"] = "(全角{$moji}文字{$gyou}行まで)";
        //その他の活動(全角8文字X4行)
        $moji = 8;
        $gyou = 4;
        $name = "CLUBACT";
        $cols = $moji * 2 + 1;//幅
        $height = $gyou * 13.5 + ($gyou - 1) * 3 + 5;//高さ
        $extra = "style=\"height:{$height}px;\"";
        $arg[$name] = knjCreateTextArea($objForm, $name, $gyou, $cols, "soft", $extra, $Row[$name]);
        $arg[$name."_TYUI"] = "(全角{$moji}文字{$gyou}行まで)";
        //６．出欠の記録（主な欠席理由）(全角24文字X1行)
        $moji = 24;
        $gyou = 1;
        $name = "ATTENDREC_REMARK";
        $cols = $moji * 2 + 1;//幅
        $height = $gyou * 13.5 + ($gyou - 1) * 3 + 5;//高さ
        $extra = "style=\"height:{$height}px;\"";
        $arg[$name] = knjCreateTextArea($objForm, $name, $gyou, $cols, "soft", $extra, $Row[$name]);
        $arg[$name."_TYUI"] = "(全角{$moji}文字{$gyou}行まで)";
        //７．参考となる諸事項等の記録
        if (CTRL_YEAR < "2014") {
            //７．指導要録所見データ(表示)(全角15文字X29行)
            $moji = 15;
            $gyou = 12;
            $name = "SIDOU_TOTALREMARK";
            $cols = $moji * 2 + 1;//幅
            $height = $gyou * 13.5 + ($gyou - 1) * 3 + 5;//高さ
            $extra = "style=\"background-color:#D0D0D0;height:{$height}px;\"";
            $arg[$name] = knjCreateTextArea($objForm, $name, $gyou, $cols, "soft", $extra, $sidouRow[$name]);
            //７．参考となる諸事項等の記録(全角31文字X12行)
            $moji = 31;
            $gyou = 12;
            $name = "REMARK";
            $cols = $moji * 2 + 1;//幅
            $height = $gyou * 13.5 + ($gyou - 1) * 3 + 5;//高さ
            $extra = "style=\"height:{$height}px;\"";
            $arg[$name] = knjCreateTextArea($objForm, $name, $gyou, $cols, "soft", $extra, $Row[$name]);
            $arg[$name."_TYUI"] = "(全角{$moji}文字{$gyou}行まで)";
            $arg[$name."_old"] = 1;
        } else {
            //７．指導要録所見データ(表示)(全角44文字X13行)
            $moji = 44;
            $gyou = 3;
            $name = "SIDOU_TOTALREMARK";
            $cols = $moji * 2 + 1;//幅
            $height = $gyou * 13.5 + ($gyou - 1) * 3 + 5;//高さ
            $extra = "style=\"background-color:#D0D0D0;height:{$height}px;\"";
            $arg[$name] = knjCreateTextArea($objForm, $name, $gyou, $cols, "soft", $extra, $sidouRow[$name]);
            //７．参考となる諸事項等の記録(全角58文字X8行)
            $moji = 58;
            $gyou = 8;
            $name = "REMARK";
            $cols = $moji * 2 + 1;//幅
            $height = $gyou * 13.5 + ($gyou - 1) * 3 + 5;//高さ
            $extra = "style=\"height:{$height}px;\"";
            $arg[$name] = knjCreateTextArea($objForm, $name, $gyou, $cols, "soft", $extra, $Row[$name]);
            $arg[$name."_TYUI"] = "(全角{$moji}文字{$gyou}行まで)";
            $arg[$name."_new"] = 1;
        }

        /**********/
        /* ボタン */
        /**********/
        //出欠備考参照ボタン
        $sdate = $model->annual["YEAR"].'-04-01';
        $edate = ($model->annual["YEAR"]+1).'-03-31';
        if ($model->Properties["useAttendSemesRemarkDat"] == 1) {
            $extra = $disabled ." onclick=\"loadwindow('../../X/KNJXATTEND_REMARK/index.php?YEAR={$model->annual["YEAR"]}&SCHREGNO={$model->schregno}&SDATE={$sdate}&EDATE={$edate}&SEMESFLG=1',0,document.documentElement.scrollTop || document.body.scrollTop,420,300);return;\"";
            $arg["btn_attend_remark"] = KnjCreateBtn($objForm, "btn_attend_remark", "まとめ出欠備考参照", $extra);
        } else {
            $extra = $disabled ." onclick=\"loadwindow('../../X/KNJXATTEND_REMARK/index.php?YEAR={$model->annual["YEAR"]}&SCHREGNO={$model->schregno}&SDATE={$sdate}&EDATE={$edate}',0,document.documentElement.scrollTop || document.body.scrollTop,420,300);return;\"";
            $arg["btn_attend_remark"] = KnjCreateBtn($objForm, "btn_attend_remark", "日々出欠備考参照", $extra);
        }
        //要録の出欠備考参照ボタン
        $extra = $disabled ." onclick=\"loadwindow('../../X/KNJXATTEND_HTRAINREMARK/index.php?YEAR={$model->annual["YEAR"]}&SCHREGNO={$model->schregno}',0,document.documentElement.scrollTop || document.body.scrollTop,360,180);return;\" style=\"width:195px;\"";
        $arg["btn_attend_htrainremark"] = KnjCreateBtn($objForm, "btn_attend_htrainremark", "要録の出欠の記録備考参照", $extra);
        //通知表所見参照ボタン
        $extra = "onclick=\"return btn_submit('subform1');\"";
        $arg["btn_hreportremark"] = KnjCreateBtn($objForm, "btn_hreportremark", "通知表所見参照", $extra);
        //更新ボタンを作成する
        $extra = "onclick=\"return btn_submit('update');\"";
        $arg["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);
        //更新後前の生徒へボタン
        $arg["btn_up_next"] = View::updateNext2($model, $objForm, $model->schregno, "SCHREGNO", "updEdit", "update");
        //取消しボタンを作成する
        $extra = "onclick=\"return btn_submit('reset')\"";
        $arg["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);
        //終了ボタンを作成する
        $extra = "onclick=\"closeWin();\"";
        $arg["btn_back"] = knjCreateBtn($objForm, "btn_back", "終 了", $extra);

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
        knjCreateHidden($objForm, "useFrameLock", $model->Properties["useFrameLock"]);

        if(get_count($model->warning)== 0 && $model->cmd !="reset") {
            $arg["next"] = "NextStudent2(0);";
        } elseif($model->cmd =="reset") {
            $arg["next"] = "NextStudent2(1);";
        }

        //画面のリロード
        if ($model->cmd == "updEdit") {
            $arg["reload"] = "parent.left_frame.btn_submit('list');";
        }

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();
        //インラインフレーム用Javascriptタグ生成
        $arg["IFRAME"] = View::setIframeJs();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knje012yForm1.html", $arg);
    }
}
?>
