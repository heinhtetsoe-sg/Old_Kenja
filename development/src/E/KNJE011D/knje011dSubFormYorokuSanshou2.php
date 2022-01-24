<?php

require_once('for_php7.php');

//ビュー作成用クラス
class knje011dSubFormYorokuSanshou2 {
    function main(&$model) {
        $objForm = new form;
        $arg = array();
        //フォーム作成
        $arg["start"]   = $objForm->get_start("formYorokuSanshou2", "POST", "knje011dindex.php", "", "formYorokuSanshou2");

        $arg["NAME_SHOW"] = $model->schregno."　".$model->name;

        //DB接続
        $db = Query::dbCheckOut();

        //SQL文発行
        //年度（年次）取得
        if ($model->cmd == "formYorokuSanshou2_first") { // すでに別の生徒を開いていた場合そのときの値が保持されているので
            unset($model->annual["YEAR"]);  // 最初の呼出ならば、年度と年次をクリアする
            unset($model->annual["ANNUAL"]);
        }
        $opt = array();
        $disabled = "disabled";
        $query = knje011dQuery::selectQueryAnnual_HTRAINREMARK_DAT($model);
        $result = $db->query($query);
        while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            $opt[] = array("label" => $row["YEAR"] ."年度　" .(int) $row["ANNUAL"] ."学年(年次)",
                           "value" => $row["YEAR"] ."," .$row["ANNUAL"]
                          );
            if (!isset($model->annual["YEAR"]) || ($model->cmd == "formYorokuSanshou2_first" && $model->exp_year == $row["YEAR"])){
                $model->annual["YEAR"]   = $row["YEAR"];
                $model->annual["ANNUAL"] = $row["ANNUAL"];
            }

            $disabled = "";
        }

        $objForm->ae( array("type"       => "select",
                            "name"       => "ANNUAL",
                            "size"       => "1",
                            "value"      => $model->annual["YEAR"] ."," .$model->annual["ANNUAL"],
                            "extrahtml"  => "onChange=\"return btn_submit('formYorokuSanshou2');\"",
                            "options"    => $opt));
        $arg["ANNUAL"] = $objForm->ge("ANNUAL");

        //指導要録所見（総合的な学習の時間）取得
        $query = knje011dQuery::selectQuery_Htrainremark_dat2($model);
        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);

        /******************/
        /* テキストエリア */
        /******************/
        $extra = "onblur=\"this.value=this.defaultValue\" onchange=\"this.value=this.defaultValue\" onkeydown=\"return false\"";
        //活動内容
        $height = ((int)$model->mojigyou["SIDO_TOTALSTUDYACT"]["gyou"] - 1) * 14 + 20;
        $extra2 = " style=\"height:{$height}px;\" ";
        $arg["TOTALSTUDYACT"] = KnjCreateTextArea($objForm, "TOTALSTUDYACT", $model->mojigyou["SIDO_TOTALSTUDYACT"]["gyou"], ((int)$model->mojigyou["SIDO_TOTALSTUDYACT"]["moji"] * 2 + 1), "soft", $extra.$extra2, $row["TOTALSTUDYACT"]);

        //評価
        $height = ((int)$model->mojigyou["SIDO_TOTALSTUDYVAL"]["gyou"] - 1) * 14 + 20;
        $extra2 = " style=\"height:{$height}px;\" ";
        $arg["TOTALSTUDYVAL"] = KnjCreateTextArea($objForm, "TOTALSTUDYVAL", $model->mojigyou["SIDO_TOTALSTUDYVAL"]["gyou"], ((int)$model->mojigyou["SIDO_TOTALSTUDYVAL"]["moji"] * 2 + 1), "soft", $extra.$extra2, $row["TOTALSTUDYVAL"]);

        /**********/
        /* ボタン */
        /**********/
        //終了
        $extra = "onclick=\"return parent.closeit()\"";
        $arg["btn_back"] = KnjCreateBtn($objForm, "btn_back", "戻る", $extra);

        /**********/
        /* hidden */
        /**********/
        knjCreateHidden($objForm, "cmd");

        //DB切断
        Query::dbCheckIn($db);
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knje011dSubFormYorokuSanshou2.html", $arg);
    }
}
?>