<?php

require_once("for_php7.php");

class knja125jForm2
{
    public function main(&$model)
    {
        //オブジェクト作成
        $objForm = new form();

        //フォーム作成
        $arg["start"] = $objForm->get_start("form2", "POST", "knja125jindex.php", "", "form2");

        //DB接続
        $db = Query::dbCheckOut();

        //生徒情報
        $arg["NAME_SHOW"] = $model->schregno."　".$model->name;

        //記録の取得
        $Row = $row = array();
        $result = $db->query(knja125jQuery::getBehavior($model));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $scd = $row["DIV"] .$row["CODE"];
            $Row["RECORD"][$scd] = $row["RECORD"];
        }
        $result->free();

        //特別活動の記録の観点取得
        $row = $db->getRow(knja125jQuery::getTrainRow($model, ""), DB_FETCHMODE_ASSOC);

        //警告メッセージがある時と、更新の際はモデルの値を参照する
        if (isset($model->warning)) {
            $Row =& $model->record;
            $row =& $model->field;
        }

        //行動の記録チェックボックス
        for ($i=1; $i<11; $i++) {
            $ival = "1" . sprintf("%02d", $i);
            $check_val = $Row["RECORD"][$ival];

            //通知票から取込
            if ($model->cmd == "form2_torikomi") {
                $torikomi = $db->getRow(knja125jQuery::getBehaviorSemesDat($model, "9", sprintf("%02d", $i)), DB_FETCHMODE_ASSOC);
                $check_val = $torikomi["RECORD"];
            }

            $check1 = ($check_val == "1") ? "checked" : "";
            $extra = $check1." id=\"RECORD".$ival."\"";
            $arg["RECORD".$ival]= knjCreateCheckBox($objForm, "RECORD".$ival, "1", $extra, "");
        }

        //特別活動の記録チェックボックス
        for ($i=1; $i<5; $i++) {
            $ival = "2" . sprintf("%02d", $i);
            $check1 = ($Row["RECORD"][$ival] == "1") ? "checked" : "";
            $extra = $check1." id=\"RECORD".$ival."\"";
            $arg["RECORD".$ival]= knjCreateCheckBox($objForm, "RECORD".$ival, "1", $extra, "");
        }

        //特別活動の記録の観点
        if ($model->Properties["Specialactremark_3disp_J"] == "1") {
            $arg["showSpecialactremark_3disp_J"] = "1";
            $moji = 17;
            $gyo = 3;
            $extra = "style=\"height:45px;\"onkeyup=\"charCount(this.value, ".$gyo.", (".$moji." * 2), true);\"";
            $arg["CLASSACT"] = knjCreateTextArea($objForm, "CLASSACT", $gyo, $moji * 2, "soft", $extra, $row["CLASSACT"]);
            $arg["STUDENTACT"] = knjCreateTextArea($objForm, "STUDENTACT", $gyo, $moji * 2, "soft", $extra, $row["STUDENTACT"]);
            $arg["SCHOOLEVENT"] = knjCreateTextArea($objForm, "SCHOOLEVENT", $gyo, $moji * 2, "soft", $extra, $row["SCHOOLEVENT"]);
        } else {
            $arg["not_showSpecialactremark_3disp_J"] = "1";
            $extra = "style=\"height:145px;\"onkeyup=\"charCount(this.value, 10, (17 * 2), true);\"";
            $arg["SPECIALACTREMARK"] = knjCreateTextArea($objForm, "SPECIALACTREMARK", 10, 35, "soft", $extra, $row["SPECIALACTREMARK"]);

            //特別活動用の定型ボタン作成 (プロパティ設定時)
            if ($model->Properties["SpecialAct_HTRAINREMARK_TEMP_DAT"] == "1") {
                $arg["TEIKEI_FLG"] = "1";
                $model->createTeikeiBtn($arg, $objForm, "14-15", "特別活動", "SPECIALACTREMARK");
            }
        }

        /*  通知表参照  */
        /****************/
        //通知表参照設定
        if ($model->Properties["sanshouReport"] == "1") {
            //学期名表示
            for ($i=1; $i<=$model->control["学期数"]; $i++) {
                $arg["SEM_NAME".$i] = $model->control["学期名"][$i];
            }

            if ($model->control["学期数"] == "3") {
                $arg["semester"] = 1;
            }
            $arg["setCol"] = $model->control["学期数"] + 1;
            $arg["setCol2"] = ($model->control["学期数"] + 1) * 2;

            //行動の記録項目名取得
            $result = $db->query(knja125jQuery::getNameMst($model, "D035"));
            $rfActItem = array();
            while ($rowItem = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $rfActItem[$rowItem["NAMECD2"]] = $rowItem["NAME1"];
            }

            //行動の記録取得
            $rfActRow = array();
            for ($h=1; $h<11; $h++) {
                $rcd = sprintf("%02d", $h);
                for ($i=1; $i<=$model->control["学期数"]; $i++) {
                    $result = $db->query(knja125jQuery::getBehaviorSemesDat($model, $i, $rcd));
                    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                        $rfActRow["RECORD".$i][$rcd] = $row["RECORD"];
                    }
                    $result->free();
                }
            }

            //特別活動の記録項目名取得
            $result = $db->query(knja125jQuery::getNameMst($model, "D034"));
            $rfSpeItem = array();
            while ($rowItem = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $rfSpeItem[$rowItem["NAMECD2"]] = $rowItem["NAME1"];
            }

            //特別活動の記録取得
            $rfSpeRow = array();
            for ($h=1; $h<6; $h++) {
                $rcd = sprintf("%02d", $h);
                for ($i=1; $i<=$model->control["学期数"]; $i++) {
                    $result = $db->query(knja125jQuery::getHreportremarkDetailDat($model, $i, "01", $rcd));
                    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                        $rfSpeRow["REMARK".$i][$rcd] = $row["REMARK1"];
                    }
                    $result->free();
                }
            }

            //活動の記録／部活動・その他取得
            for ($h=1; $h<3; $h++) {
                $rcd = sprintf("%02d", $h);
                $result = $db->query(knja125jQuery::getHreportremarkDetailDat($model, "9", "02", $rcd));
                while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                    $rfSpeRow["REMARK9"][$rcd] = $row["REMARK1"];
                }
                $result->free();
            }

            //データ出力
            $referData = array();
            for ($h=1; $h<11; $h++) {
                $rcd = sprintf("%02d", $h);
                $referData["RFACT_ITEMNAME"] = $rfActItem[$rcd];
                $referData["RFSPE_ITEMNAME"] = $rfSpeItem[$rcd];

                if ($h < 6) {
                    $referData["HreportRemark"]  = "<th align=\"center\" class=\"no_search\" nowrap>" .$referData["RFSPE_ITEMNAME"] ."</th>";
                } elseif ($h == 6) {
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

                for ($i=1; $i<=$model->control["学期数"]; $i++) {
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
                    } elseif ($h == 6) {
                    } else {
                        $referData["HreportRemark"] = "";
                    }
                }
                $arg["rdata"][] = $referData;
            }
            $arg["Report"] = "1";
        }

        if ($model->Properties["KoudouNoKirokuTorikomi"] == "1") {
            //通知票取込ボタン
            $extra = "onclick=\"return btn_submit('form2_torikomi')\"";
            $arg["button"]["btn_torikomi"] = KnjCreateBtn($objForm, "btn_torikomi", "通知票取込", $extra);
        }

        //学校種別
        $schoolkind = $db->getOne(knja125jQuery::getSchoolKind($model));

        //更新ボタン
        $extra = (AUTHORITY < DEF_UPDATE_RESTRICT || $schoolkind != 'J') ? "disabled" : "onclick=\"return btn_submit('update2')\"";
        $arg["button"]["btn_update"] = KnjCreateBtn($objForm, "btn_update", "更 新", $extra);

        //取消ボタン
        $extra = "onclick=\"return btn_submit('clear2')\"";
        $arg["button"]["btn_reset"] = KnjCreateBtn($objForm, "btn_reset", "取 消", $extra);

        //戻るボタン
        $extra = "onclick=\"return top.main_frame.right_frame.closeit()\"";
        $arg["button"]["btn_back"] = KnjCreateBtn($objForm, "btn_back", "戻 る", $extra);

        //既入力内容参照（行動の記録・特別活動の記録）
        $extra = "onclick=\"loadwindow('" .REQUESTROOT ."/A/KNJA125J_SHOKEN/knja125j_shokenindex.php?cmd=edit&SCHREGNO={$model->schregno}&frame='+getFrameName(self), event.clientX + function () {var scrollX = document.documentElement.scrollLeft || document.body.scrollLeft;return scrollX;}(), event.clientY + function () {var scrollY = document.documentElement.scrollTop || document.body.scrollTop;return scrollY;}(), 700, 350)\"";
        $arg["button"]["shokenlist_prg"] = knjCreateBtn($objForm, "shokenlist_prg", "既入力内容の参照", $extra);

        //hidden
        knjCreateHidden($objForm, "cmd");

        //インラインフレーム用Javascriptタグ生成
        $arg["IFRAME"] = View::setIframeJs();

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knja125jForm2.html", $arg);
    }
}
