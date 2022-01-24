<?php

require_once('for_php7.php');

//ビュー作成用クラス
class knje011oForm2 {
    function main(&$model) {
        $objForm = new form;
        $arg = array();
        //フォーム作成
        $arg["start"]   = $objForm->get_start("form2", "POST", "knje011oindex.php", "", "form2");

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
        $query = knje011oQuery::selectQueryAnnual($model);
        $result = $db->query($query);
        while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            $opt[] = array("label" => $row["YEAR"] ."年度　" .(int) $row["ANNUAL"] ."学年(年次)",
                           "value" => $row["YEAR"] ."," .$row["ANNUAL"]
                          );
            if (!isset($model->annual["YEAR"]) || ($model->cmd == "form2_first" && $model->exp_year == $row["YEAR"])){
                $model->annual["YEAR"]   = $row["YEAR"];
                $model->annual["ANNUAL"] = $row["ANNUAL"];
            }

            $disabled = "";
        }
        if (!isset($model->warning)) {
            if ($model->cmd == "reload2") {
                $query = knje011oQuery::selectQuery_Htrainremark_Dat($model);
            } else {
                $query = knje011oQuery::selectQueryForm2($model);
            }
            $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
            if ($model->cmd == "reload2" && $model->useSyojikou3 == "1") {
                $row["READ_TRAIN_REF"] = $row["TRAIN_REF"];
                $row["TRAIN_REF"] = $model->field2["TRAIN_REF"];
                $row["TRAIN_REF2"] = $model->field2["TRAIN_REF2"];
                $row["TRAIN_REF3"] = $model->field2["TRAIN_REF3"];
            }
        } else {
            $row = $model->field2;
        }
        $objForm->ae( array("type"       => "select",
                            "name"       => "ANNUAL",
                            "size"       => "1",
                            "value"      => $model->annual["YEAR"] ."," .$model->annual["ANNUAL"],
                            "extrahtml"  => "onChange=\"return btn_submit('form2');\"",
                            "options"    => $opt));

        $arg["ANNUAL"] = $objForm->ge("ANNUAL");

        /******************/
        /* テキストエリア */
        /******************/
        //出欠の記録備考
        $arg["ATTENDREC_REMARK"] = KnjCreateTextArea($objForm, "ATTENDREC_REMARK", 3, 11, "soft", "style=\"height:48px;\"", $row["ATTENDREC_REMARK"]);
        //特別活動の記録
        $arg["SPECIALACTREC"]    = KnjCreateTextArea($objForm, "SPECIALACTREC", 9, 23, "soft", "style=\"height:119px;\"", $row["SPECIALACTREC"]);
        //指導上参考となる諸事項]
        if ($model->useSyojikou3 == "1") {
            $arg["TRAIN_REF"] = KnjCreateTextArea($objForm, "TRAIN_REF", 5, 29, "soft", "style=\"height:77px;\"", $row["TRAIN_REF"]);
            $arg["TRAIN_REF2"] = KnjCreateTextArea($objForm, "TRAIN_REF2", 5, 29, "soft", "style=\"height:77px;\"", $row["TRAIN_REF2"]);
            $arg["TRAIN_REF3"] = KnjCreateTextArea($objForm, "TRAIN_REF3", 5, 29, "soft", "style=\"height:77px;\"", $row["TRAIN_REF3"]);
            $arg["READ_SYOJIKOU"] = KnjCreateTextArea($objForm, "READ_SYOJIKOU", 5, 83, "soft", "style=\"background-color:#D0D0D0;height:60px;\"", $row["READ_TRAIN_REF"]);
            $arg["useSyojikou3"] = $model->useSyojikou3;
            $arg["COLSPAN2"] = "colspan=\"2\"";
            $arg["COLSPAN_CHANGE"] = "colspan=\"3\"";
            $arg["TRAIN_REF_COMMENT"] = "(全角14文字X5行まで)";
        } else {
            $arg["TRAIN_REF"] = KnjCreateTextArea($objForm, "TRAIN_REF", 5, 83, "soft", "style=\"height:77px;\"", $row["TRAIN_REF"]);
            $arg["COLSPAN_TRAIN_REF"] = "colspan=\"2\"";
            $arg["COLSPAN_CHANGE"] = "colspan=\"2\"";
            $arg["TRAIN_REF_COMMENT"] = "(全角41文字X5行まで)";
        }

        /**********/
        /* ボタン */
        /**********/
        //学習指導要録より読込ボタンを作成する
        $extra = "onclick=\" return btn_submit('reload2');\"";
        $arg["btn_reload2"] = KnjCreateBtn($objForm, "btn_reload2", "学習指導要録より読込", $extra);
        //出欠備考参照ボタン
        $extra = "onclick=\"loadwindow('../../X/KNJXATTEND_REMARK/index.php?YEAR={$model->annual["YEAR"]}&SCHREGNO={$model->schregno}',0,0,420,300);return;\"";
        $arg["SANSYO"] = KnjCreateBtn($objForm, "SANSYO", "出欠備考参照", $extra);
        //要録の出欠備考参照ボタン
        $extra = "onclick=\"loadwindow('../../X/KNJXATTEND_HTRAINREMARK/index.php?YEAR={$model->annual["YEAR"]}&SCHREGNO={$model->schregno}',0,0,360,180);return;\" style=\"width:210px;\"";
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
        View::toHTML($model, "knje011oForm2.html", $arg);
    }
}
?>