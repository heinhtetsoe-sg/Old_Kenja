<?php

require_once('for_php7.php');

class knja121c_shokenForm1 {
    function main(&$model) {
        $objForm = new form;
        
        $arg["start"] = $objForm->get_start("knja121c_shoken", "POST", "knja121c_shokenindex.php", "", "knja121c_shoken");

        //DB接続
        $db = Query::dbCheckOut();

        //年度・学年コンボ
        $opt = array();
        $opt[] = array('label' => "",'value' => "");
        $value_flg = false;
        $query = knja121c_shokenQuery::getTrainRow($model, "year_anuual");
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[] = array('label' => $row["LABEL"],
                           'value' => $row["VALUE"]);
            if ($model->field["YEAR_ANNUAL"] == $row["VALUE"]) $value_flg = true;
        }
        $model->field["YEAR_ANNUAL"] = ($model->field["YEAR_ANNUAL"] && $value_flg) ? $model->field["YEAR_ANNUAL"] : $opt[0];
        $extra = "onchange=\"return btn_submit('edit')\"";
        $arg["YEAR_ANUUAL"] = knjCreateCombo($objForm, "YEAR_ANNUAL", $model->field["YEAR_ANNUAL"], $opt, $extra, 1);

        //生徒名
        $getName = $db->getOne(knja121c_shokenQuery::getName($model));

        //生徒情報
        $arg["NAME_SHOW"] = $model->schregno."　".$getName;

        //記録の取得
        $Row = $row = array();
        $result = $db->query(knja121c_shokenQuery::getBehavior($model));
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            $scd = $row["DIV"] .$row["CODE"];
            $Row["RECORD"][$scd] = $row["RECORD"];
        }
        $result->free();

        //特別活動の記録の観点取得
        $row = $db->getRow(knja121c_shokenQuery::getTrainRow($model, "setyear"), DB_FETCHMODE_ASSOC);
        //警告メッセージがある時と、更新の際はモデルの値を参照する
        if(isset($model->warning)){
            $Row =& $model->record;
            $row =& $model->field;
        }

        //行動の記録チェックボックス
        for($i=1; $i<11; $i++)
        {
            $ival = "1" . sprintf("%02d", $i);
            $check1 = ($Row["RECORD"][$ival] == "1") ? "checked" : "";
            $extra = $check1." id=\"RECORD".$ival."\"";
            $extra .= "disabled";
            $arg["RECORD".$ival]= knjCreateCheckBox($objForm, "RECORD".$ival, "1", $extra, "");
        }

        //特別活動の記録チェックボックス
        for($i=1; $i<5; $i++)
        {
            $ival = "2" . sprintf("%02d", $i);
            $check1 = ($Row["RECORD"][$ival] == "1") ? "checked" : "";
            $extra = $check1." id=\"RECORD".$ival."\"";
            $extra .= "disabled";
            $arg["RECORD".$ival]= knjCreateCheckBox($objForm, "RECORD".$ival, "1", $extra, "");
        }

        //特別活動の記録の観点
        if ($model->Properties["TokubetuKatudoKanten_Not_Hyouji"] != "1") {
            $arg["TokubetuKatudoKantenHyouji"] = "1";
            $extra = "style=\"height:145px;\"onkeyup=\"charCount(this.value, 10, (17 * 2), true);\"";
            $arg["SPECIALACTREMARK"] = knjCreateTextArea($objForm, "SPECIALACTREMARK", 10, 35, "soft", $extra, $row["SPECIALACTREMARK"]);
        }

        /****************/
        /*  通知表参照  */
        /****************/
        //通知表参照設定
        if ($model->Properties["sanshouReport"] == "1") {
            //学期名表示
            for($i=1; $i<=$model->control["学期数"]; $i++) {
                $arg["SEM_NAME".$i] = $model->control["学期名"][$i];
            }

            if($model->control["学期数"] == "3") $arg["semester"] = 1;
            $arg["setCol"] = $model->control["学期数"] + 1;
            $arg["setCol2"] = ((int)$model->control["学期数"] + 1) * 2;

            //行動の記録項目名取得
            $result = $db->query(knja121c_shokenQuery::getNameMst($model, "D035"));
            $rfActItem = array();
            while ($rowItem = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $rfActItem[$rowItem["NAMECD2"]] = $rowItem["NAME1"];

            }

            //行動の記録取得
            $rfActRow = array();
            for($h=1; $h<11; $h++) {
                $rcd = sprintf("%02d", $h);
                for($i=1; $i<=$model->control["学期数"]; $i++) {
                    $result = $db->query(knja121c_shokenQuery::getBehaviorSemesDat($model, $i, $rcd));
                    while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                        $rfActRow["RECORD".$i][$rcd] = $row["RECORD"];
                    }
                    $result->free();
                }
            }

            //特別活動の記録項目名取得
            $result = $db->query(knja121c_shokenQuery::getNameMst($model, "D034"));
            $rfSpeItem = array();
            while ($rowItem = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $rfSpeItem[$rowItem["NAMECD2"]] = $rowItem["NAME1"];
            }

            //特別活動の記録取得
            $rfSpeRow = array();
            for($h=1; $h<6; $h++) {
                $rcd = sprintf("%02d", $h);
                for($i=1; $i<=$model->control["学期数"]; $i++) {
                    $result = $db->query(knja121c_shokenQuery::getHreportremarkDetailDat($model, $i, "01", $rcd));
                    while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                        $rfSpeRow["REMARK".$i][$rcd] = $row["REMARK1"];
                    }
                    $result->free();
                }
            }

            //活動の記録／部活動・その他取得
            for($h=1; $h<3; $h++) {
                $rcd = sprintf("%02d", $h);
                $result = $db->query(knja121c_shokenQuery::getHreportremarkDetailDat($model, "9", "02", $rcd));
                while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                    $rfSpeRow["REMARK9"][$rcd] = $row["REMARK1"];
                }
                $result->free();
            }

            //データ出力
            $referData = array();
            for($h=1; $h<11; $h++) {
                $rcd = sprintf("%02d", $h);
                $referData["RFACT_ITEMNAME"] = $rfActItem[$rcd];
                $referData["RFSPE_ITEMNAME"] = $rfSpeItem[$rcd];

                if ($h < 6) {
                    $referData["HreportRemark"]  = "<th align=\"center\" class=\"no_search\" nowrap>" .$referData["RFSPE_ITEMNAME"] ."</th>";
                } else if($h == 6) {
                    $extra = "style=\"height:100px;\" disabled";
                    $katsudo = knjCreateTextArea($objForm, "REMARK901", 6, 33, "soft", $extra, $rfSpeRow["REMARK9"]["01"]);
                    $sonota  = knjCreateTextArea($objForm, "REMARK902", 6, 33, "soft", $extra, $rfSpeRow["REMARK9"]["02"]);

                    $referData["HreportRemark"]  = "<th class=\"no_search_line\" rowspan=\"5\" colspan=".$arg["setCol"]." nowrap>";
                    $referData["HreportRemark"] .= "  <table width=\"100%\"  height=\"100%\" border=\"0\" cellspacing=\"1\" cellpadding=\"3\">";
                    $referData["HreportRemark"] .= "    <tr>";
                    $referData["HreportRemark"] .= "      <th align=\"center\" class=\"no_search\" height=\"30\" nowrap>活動の記録</th>";
                    $referData["HreportRemark"] .= "      <th align=\"center\" class=\"no_search\" height=\"30\" nowrap>部活動・その他</th>";
                    $referData["HreportRemark"] .= "    </tr>";
                    $referData["HreportRemark"] .= "    <tr>";
                    $referData["HreportRemark"] .= "      <td align=\"center\" bgcolor=\"#ffffff\" nowrap>" .$katsudo ."</td>";
                    $referData["HreportRemark"] .= "      <td align=\"center\" bgcolor=\"#ffffff\" nowrap>" .$sonota ."</td>";
                    $referData["HreportRemark"] .= "    </tr>";
                    $referData["HreportRemark"] .= "  </table>";
                    $referData["HreportRemark"] .= "</th>";
                }

                for($i=1; $i<=$model->control["学期数"]; $i++) {

                    //行動の記録
                    if ($model->Properties["knjdBehaviorsd_UseText"] == "1") {
                        $referData["RECORD_VAL".$i] = $rfActRow["RECORD".$i][$rcd];
                    } else {
                        $check1 = ($rfActRow["RECORD".$i][$rcd] == "1") ? "checked" : "";
                        $extra = $check1." disabled";
                        $referData["RECORD_VAL".$i] = knjCreateCheckBox($objForm, "RECORD".$i.$rcd, "1", $extra, "");
                    }
                    //特別活動の記録
                    if ($h < 6) {
                        if ($model->Properties["knjdHreportRemark_d2_UseText"] == "1") {
                            $referData["REMARK_VAL".$i] = knjCreateTextBox($objForm, $rfSpeRow["REMARK".$i][$rcd], "REMARK".$i.$rcd, 34, 66, "disabled");
                        } else {
                            $check1 = ($rfSpeRow["REMARK".$i][$rcd] == "1") ? "checked" : "";
                            $extra = $check1." disabled";
                            $referData["REMARK_VAL".$i] = knjCreateCheckBox($objForm, "REMARK".$i.$rcd, "1", $extra, "");
                        }
                        $referData["HreportRemark"] .= "<td bgcolor=\"#ffffff\">" .$referData["REMARK_VAL".$i] ."</th>";
                    } else if($h == 6) {
                    } else {
                        $referData["HreportRemark"] = "";
                    }
                }
                $arg["rdata"][] = $referData;
            }
            $arg["Report"] = "1";
        }

        //戻るボタン
        $extra = "onclick=\"return parent.closeit()\"";
        $arg["button"]["btn_back"] = KnjCreateBtn($objForm, "btn_back", "戻 る", $extra);

        //hidden
        knjCreateHidden($objForm, "cmd");

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knja121c_shokenForm1.html", $arg);
    }
}
?>