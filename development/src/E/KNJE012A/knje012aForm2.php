<?php

require_once('for_php7.php');

//ビュー作成用クラス
class knje012aForm2 {
    function main(&$model) {
        $objForm = new form;
        $arg = array();
        //フォーム作成
        $arg["start"]   = $objForm->get_start("form2", "POST", "knje012aindex.php", "", "form2");

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
        $query = knje012aQuery::selectQueryAnnual_knje012aForm2($model);
        $result = $db->query($query);
        while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            $opt[] = array("label" => $row["YEAR"] ."年度　" .(int) $row["ANNUAL"] ."学年(年次)",
                           "value" => $row["YEAR"] ."," .$row["ANNUAL"]
                          );
            if (!isset($model->annual["YEAR"]) || $model->cmd == "form2_first" && $model->exp_year == $row["YEAR"]){
                $model->annual["YEAR"]   = $row["YEAR"];
                $model->annual["ANNUAL"] = $row["ANNUAL"];
            }
            $disabled = "";
        }
        if (!strlen($model->annual["YEAR"]) || !strlen($model->annual["ANNUAL"])) {
            list($model->annual["YEAR"], $model->annual["ANNUAL"]) = preg_split("/,/", $opt[0]["value"]);
        }
        if (!isset($model->warning)) {
            $query = knje012aQuery::selectQueryForm2($model);
            $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
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

        //学習活動
        $extra = "style=\"height:118px;\"";
        $arg["data"]["TOTALSTUDYACT"] = knjCreateTextArea($objForm, "TOTALSTUDYACT", 8, (5 * 2 + 1), "soft", $extra, $row["TOTALSTUDYACT"]);

        //観点
        $arg["data"]["VIEWREMARK"] = knjCreateTextArea($objForm, "VIEWREMARK", 8, (10 * 2 + 1), "soft", $extra, $row["VIEWREMARK"]);

        //評価
        $arg["data"]["TOTALSTUDYVAL"] = knjCreateTextArea($objForm, "TOTALSTUDYVAL", 8, (15 * 2 + 1), "soft", $extra, $row["TOTALSTUDYVAL"]);
        
        //特別活動の記録の観点
        $extra = "style=\"height:145px;\"";
        $arg["data"]["SPECIALACTREMARK"] = knjCreateTextArea($objForm, "SPECIALACTREMARK", 10, (17 * 2 + 1), "soft", $extra, $row["SPECIALACTREMARK"]);
        
        //総合所見及び指導上参考となる諸事項
        $extra = "style=\"height:180px;\"";
        $arg["data"]["TOTALREMARK"] = KnjCreateTextArea($objForm, "TOTALREMARK", 13, (44 * 2 + 1), "soft", $extra, $row["TOTALREMARK"]);

        //出欠の記録備考
        $extra = "style=\"height:40px;\"";
        $arg["data"]["ATTENDREC_REMARK"] = KnjCreateTextArea($objForm, "ATTENDREC_REMARK", 2, (30 * 2 + 1), "soft", $extra, $row["ATTENDREC_REMARK"]);

        /**********/
        /* ボタン */
        /**********/
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
        View::toHTML($model, "knje012aForm2.html", $arg);
    }
}
?>