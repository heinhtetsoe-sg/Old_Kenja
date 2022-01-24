<?php

require_once('for_php7.php');

//ビュー作成用クラス
class knje012bForm1
{
    public function main(&$model)
    {
        $objForm = new form();

        //フォーム作成
        $arg["start"]   = $objForm->get_start("edit", "POST", "knje012bindex.php", "", "edit");

        //DB接続
        $db = Query::dbCheckOut();

        //年度(年次)取得コンボ
        if ($model->form1_first == "on") { // すでに別の生徒を開いていた場合そのときの値が保持されているので
            $model->annual["YEAR"]   = "";  // 最初の呼出ならば、年度と年次をクリアする
            $model->annual["ANNUAL"] = "";
        }
        $opt = array();
        $disabled = "disabled";
        $query = knje012bQuery::getYearAnnual($model);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
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
        $extra = "onChange=\"return btn_submit('edit');\"" . $disabled;
        $arg["ANNUAL"] = knjCreateCombo($objForm, "ANNUAL", $value, $opt, $extra, "1");

        //1レコード取得
        if (!isset($model->warning) && $model->cmd != 'reload3' && $model->cmd != 'zentorikomi') {
            $Row = $db->getRow(knje012bQuery::selectQuery($model, $model->annual["ANNUAL"]), DB_FETCHMODE_ASSOC);
            $result = $db->query(knje012bQuery::selectQuery2($model));
            while ($row2 = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $Row['TOKUBETU_ROWS'][]=$row2;
            }
            $arg["NOT_WARNING"] = 1;
        } else {
            $Row = $model->field;
        }

        //氏名
        $arg["NAME_SHOW"] = $model->schregno."　".$model->name;

        /******************/
        /* テキストエリア */
        /******************/
        //１．文部科学省実用英語検定試験等
        $name = "ZITUYOUSIKEN";
        $moji = $model->fieldSize[$name]['moji'];
        $gyou = $model->fieldSize[$name]['gyou'];
        $cols = $moji * 2 + 1;//幅
        $height = $gyou * 13.5 + ($gyou - 1) * 3 + 5;//高さ
        $extra = "style=\"height:{$height}px;\"";
        $arg[$name] = knjCreateTextArea($objForm, $name, $gyou, $cols, "soft", $extra, $Row[$name]);
        $arg[$name."_TYUI"] = "(全角{$moji}文字{$gyou}行まで)";
        //２．学校生活全般の総合所見
        $name = "SOUGOUSYOKEN";
        $moji = $model->fieldSize[$name]['moji'];
        $gyou = $model->fieldSize[$name]['gyou'];
        $cols = $moji * 2 + 1;//幅
        $height = $gyou * 13.5 + ($gyou - 1) * 3 + 5;//高さ
        $extra = "style=\"height:{$height}px;\"";
        $arg[$name] = knjCreateTextArea($objForm, $name, $gyou, $cols, "soft", $extra, $Row[$name]);
        $arg[$name."_TYUI"] = "(全角{$moji}文字{$gyou}行まで)";
        //３．出欠の記録（主な欠席理由）
        $name = "ATTENDREC_REMARK";
        $moji = $model->fieldSize[$name]['moji'];
        $gyou = $model->fieldSize[$name]['gyou'];
        $cols = $moji * 2 + 1;//幅
        $height = $gyou * 13.5 + ($gyou - 1) * 3 + 5;//高さ
        $extra = "style=\"height:{$height}px;\"";
        $arg[$name] = knjCreateTextArea($objForm, $name, $gyou, $cols, "soft", $extra, $Row[$name]);
        $arg[$name."_TYUI"] = "(全角{$moji}文字{$gyou}行まで)";
        //４．特別活動等の記録
        //生徒会・学校行事・HR活動
        if ($Row['SPECIALACTREC'] == '' || $model->cmd == 'zentorikomi') {
            $specalactText = '';
            $result = $db->query(knje012bQuery::getKaikinData($model));
            while ($specalactrow = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $specalactText .= $specalactrow['KAIKIN']."\n";
            }

            //委員会
            $result = $db->query(knje012bQuery::getCommittee($model));
            while ($committeerow = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $specalactText .= trim($committeerow['DATA'])."\n";
            }

            //検定
            $result = $db->query(knje012bQuery::getAward($model, $db));
            while ($awardrow = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $specalactText .= trim($awardrow['DATA']);
                $specalactText .= " ".str_replace("-", "/", $awardrow["REGDDATE"]);
                $specalactText .= "\n";
            }

            //賞
            $detail_div = "1";
            $namecd1    = "H303";
            $result = $db->query(knje012bQuery::getHyosyo($model, $detail_div, $namecd1));
            while ($hyosyorow = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $specalactText .= str_replace("-", "/", $hyosyorow["DETAIL_SDATE"]);
                $specalactText .= " ".trim($hyosyorow['DATA']);
                $specalactText .= "\n";
            }

            //罰
            $detail_div = "2";
            $namecd1    = "H304";
            $result = $db->query(knje012bQuery::getBatsu($model, $detail_div, $namecd1));
            while ($batsurow = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $specalactText .= str_replace("-", "/", $batsurow["DETAIL_SDATE"]);
                $specalactText .= " ".trim($batsurow['DATA']);
                $specalactText .= "\n";
            }

            $Row['SPECIALACTREC'] = $specalactText;
        }
        $specalactClass = ($specalactText != '') ? ' class="ActiveColor"' : '';

        //生徒会・学校行事・HR活動
        $name = "SPECIALACTREC";
        $moji = $model->fieldSize[$name]['moji'];
        $gyou = $model->fieldSize[$name]['gyou'];
        $cols = $moji * 2 + 1;//幅
        $height = $gyou * 13.5 + ($gyou - 1) * 3 + 5;//高さ
        $extra = "style=\"height:{$height}px;\"". $specalactClass;
        $arg[$name] = knjCreateTextArea($objForm, $name, $gyou, $cols, "soft", $extra, $Row[$name]);
        $arg[$name."_TYUI"] = "(全角{$moji}文字{$gyou}行まで)";

        if ($Row['CLUBACT'] == '' || $model->cmd == 'zentorikomi') {
            $clubText = '';
            $result = $db->query(knje012bQuery::getClubData($model));
            while ($clubrow = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $clubText .= trim($clubrow['DATA'])."\n";
            }

            $result = $db->query(knje012bQuery::getSchregClubHdetailDat($model));
            while ($clubrow3 = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $meetShow  = $clubrow3["MEET_NAME"];
                $meetShow .= ((strlen($meetShow) > 0 && strlen($clubrow3["KINDNAME"]) > 0) ? " " : "").$clubrow3["KINDNAME"];
                $meetShow .= ((strlen($meetShow) > 0 && strlen($clubrow3["RECORDNAME"]) > 0) ? " " : "").$clubrow3["RECORDNAME"];
                $meetShow .= ((strlen($meetShow) > 0 && strlen($clubrow3["DOCUMENT"]) > 0) ? " " : "").$clubrow3["DOCUMENT"];

                $club3Text  = $clubrow3["CLUB_SHOW"];
                $club3Text .= " ".str_replace("-", "/", $clubrow3["DETAIL_DATE"]);
                $club3Text .= " ".$clubrow3["DIV_NAME"];
                $club3Text .= " ".$meetShow;
                $club3Text .= " ".$clubrow3["DETAIL_REMARK"];

                $clubText .= trim($club3Text);
                $clubText .= "\n";
            }

            $Row['CLUBACT'] = $clubText;
        }
        $clubClass = ($clubText != '') ? ' class="ActiveColor"' : '';

        //クラブ活動
        $name = "CLUBACT";
        $moji = $model->fieldSize[$name]['moji'];
        $gyou = $model->fieldSize[$name]['gyou'];
        $cols = $moji * 2 + 1;//幅
        $height = $gyou * 13.5 + ($gyou - 1) * 3 + 5;//高さ
        $extra = "style=\"height:{$height}px;\"". $clubClass;
        $arg[$name] = knjCreateTextArea($objForm, $name, $gyou, $cols, "soft", $extra, $Row[$name]);
        $arg[$name."_TYUI"] = "(全角{$moji}文字{$gyou}行まで)";

        //５．特別活動等全体の総評
        $classactClass = ' class="checkbox"';
        $studentactClass = ' class="checkbox"';
        $schooleventClass = ' class="checkbox"';

        if ($Row['STUDENTACT'] == '' || $Row['CALSSACT'] == '' || $Row['SCHOOLEVENT'] == '') {
            $result = $db->query(knje012bQuery::getBehavior($model));
            while ($behaviorrow = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                if ($behaviorrow["CODE"] == "01") {
                    if ($Row['CALSSACT'] != "1" && $behaviorrow["RECORD"] == "1") {
                        $Row['CALSSACT'] = $behaviorrow["RECORD"];
                        $classactClass = ' class="ActiveColorCb"';
                    }
                } elseif ($behaviorrow["CODE"] == "02") {
                    if ($Row['STUDENTACT'] != "1" && $behaviorrow["RECORD"] == "1") {
                        $Row['STUDENTACT'] = $behaviorrow["RECORD"];
                        $studentactClass = ' class="ActiveColorCb"';
                    }
                } elseif ($behaviorrow["CODE"] == "03") {
                    if ($Row['SCHOOLEVENT'] != "1" && $behaviorrow["RECORD"] == "1") {
                        $Row['SCHOOLEVENT'] = $behaviorrow["RECORD"];
                        $schooleventClass = ' class="ActiveColorCb"';
                    }
                }
            }
        }

        //生徒会活動
        $extra  = ($Row['STUDENTACT'] == "1") ? " checked='checked'" : "";
        $extra .= " id=\"STUDENTACT\"" . $disabled;
        $arg["STUDENTACT"] = knjCreateCheckBox($objForm, "STUDENTACT", "1", $extra, "");
        $arg["studentactClass"] = $studentactClass;
        //学級活動
        $extra  = ($Row['CALSSACT'] == "1") ? " checked='checked'" : "";
        $extra .= " id=\"CALSSACT\"" . $disabled;
        $arg["CALSSACT"] = knjCreateCheckBox($objForm, "CALSSACT", "1", $extra, "");
        $arg["classactClass"] = $classactClass;
        //学校行事
        $extra  = ($Row['SCHOOLEVENT'] == "1") ? " checked='checked'" : "";
        $extra .= " id=\"SCHOOLEVENT\"" . $disabled;
        $arg["SCHOOLEVENT"] = knjCreateCheckBox($objForm, "SCHOOLEVENT", "1", $extra, "");
        $arg["schooleventClass"] = $schooleventClass;

        /**********/
        /* ボタン */
        /**********/

        $disabled = ($model->schregno) ? "" : "disabled";

        //出欠備考参照ボタン
        $sdate = $model->annual["YEAR"].'-04-01';
        $edate = ($model->annual["YEAR"]+1).'-03-31';

        //出欠の記録参照ボタン
        if ($model->Properties["tyousasyoSyukketsuKirokuBtn"] == 1) {
            $extra = $disabled." onclick=\"return btn_submit('syukketsu');\"" . $disabled;
            $arg["btn_syukketu"] = knjCreateBtn($objForm, "btn_subform2", "出欠の記録参照", $extra);
        } else {
            $extra = $disabled." onclick=\"return btn_submit('subform2');\"" . $disabled;
            $arg["btn_syukketu"] = KnjCreateBtn($objForm, "btn_subform2", "出欠の記録参照", $extra);
        }

        //特別活動の記録：全取込
        $extra = "onclick=\"return btn_submit('zentorikomi');\"" . $disabled;
        $arg["btn_zentorikomi"] = KnjCreateBtn($objForm, "btn_zentorikomi", "全 取 込", $extra);

        //指導要録総合所見参照
        $extra = " onclick=\"loadwindow('../../X/KNJX_SOGOSOKEN/knjx_sogosokenindex.php?YEAR={$model->annual["YEAR"]}&SCHREGNO={$model->schregno}&NAME={$model->name}&ANNUAL={$model->annual["ANNUAL"]}',0,document.documentElement.scrollTop || document.body.scrollTop,750,650);return;\"" . $disabled;
        $arg["btn_sougousyoken_sidou_sansyou"] = KnjCreateBtn($objForm, "btn_sougousyoken_sidou_sansyou", "指導要録総合所見参照", $extra);
        $arg["btn_tokubetu_sidou_sansyou"] = KnjCreateBtn($objForm, "btn_tokubetu_sidou_sansyou", "指導要録総合所見参照", $extra);

        //既入力内容参照（総合的な学習時間）
        $extra = " onclick=\"return btn_submit('shokenlist1');\"" . $disabled;
        $arg["btn_tokubetu_sansyou"] = knjCreateBtn($objForm, "shokenlist1", "既入力内容の参照", $extra);
        //既入力内容参照（総合所見）
        $extra = " onclick=\"return btn_submit('shokenlist2');\"" . $disabled;
        $arg["btn_sougousyoken_sansyou"] = knjCreateBtn($objForm, "shokenlist2", "既入力内容の参照", $extra);
        //既入力内容参照（出欠の記録備考）
        $extra = " onclick=\"return btn_submit('shokenlist3');\"" . $disabled;
        $arg["btn_syukketu_sansyou"] = knjCreateBtn($objForm, "shokenlist3", "既入力内容の参照", $extra);

        //検定選択ボタン
        $arg["btn_qualified"] = makeSelectBtn($objForm, $model, "qualified", "btn_qualified", "検定選択", "ZITUYOUSIKEN", $disabled);

        //部活動選択ボタン
        $arg["btn_club"] = makeSelectBtn($objForm, $model, "club", "btn_club", "部活動選択", "SOUGOUSYOKEN", $disabled);

        //記録備考選択ボタン
        if ($model->Properties["club_kirokubikou"] == 1) {
            $arg["btn_club_kirokubikou"] = makeSelectBtn($objForm, $model, "kirokubikou", "btn_club_kirokubikou", "記録備考選択", "SOUGOUSYOKEN", $disabled);
        }

        //委員会選択ボタン
        $arg["btn_committee"] = makeSelectBtn($objForm, $model, "committee", "btn_committee", "委員会選択", "SOUGOUSYOKEN", $disabled);

        //検定選択ボタン
        $arg["btn_qualified2"] = makeSelectBtn($objForm, $model, "qualified", "btn_qualified2", "検定選択", "SOUGOUSYOKEN", $disabled);

        //委員会選択ボタン
        $arg["btn_committee2"] = makeSelectBtn($objForm, $model, "committee", "btn_committee2", "委員会選択", "SPECIALACTREC", $disabled);

        //検定選択ボタン
        $arg["btn_qualified3"] = makeSelectBtn($objForm, $model, "qualified", "btn_qualified3", "検定選択", "SPECIALACTREC", $disabled);

        if ($model->Properties["useHyosyoSansyoButton_J"] == '1') {
            //賞選択ボタン
            $arg["btn_hyosyo"] = makeSelectBtn($objForm, $model, "hyosyo", "btn_hyosyo", "賞選択", "SPECIALACTREC", $disabled);
            //罰選択ボタン
            $arg["btn_batsu"] = makeSelectBtn($objForm, $model, "batsu", "btn_batsu", "罰選択", "SPECIALACTREC", $disabled);
        }
        //部活動選択ボタン
        $arg["btn_club2"] = makeSelectBtn($objForm, $model, "club", "btn_club", "部活動選択", "CLUBACT", $disabled);

        //記録備考選択ボタン
        if ($model->Properties["club_kirokubikou"] == 1) {
            $arg["btn_club_kirokubikou2"] = makeSelectBtn($objForm, $model, "kirokubikou", "btn_club_kirokubikou", "記録備考選択", "CLUBACT", $disabled);
        }
        //CSV処理
        $fieldSize = '';
        foreach ($model->fieldSize as $key => $value) {
            $fieldSize  .= $key."=".($value['moji'] * $value['gyou'] * 3 + ($value['gyou'] - 1)).",";
        }
        $fieldSize = trim($fieldSize, ',');
        //CSVボタン
        $extra = "onClick=\" wopen('".REQUESTROOT."/X/KNJX_E012B/knjx_e012bindex.php?FIELDSIZE=".$fieldSize."&AUTH=".AUTHORITY."&PRGID=".PROGRAMID."','SUBWIN2',0,0,screen.availWidth,screen.availHeight);\"";
        $arg["btn_csv"] = KnjCreateBtn($objForm, "btn_csv", "データCSV", $extra);

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

        if (get_count($model->warning)== 0 && $model->cmd !="reset") {
            $arg["next"] = "NextStudent2(0);";
        } elseif ($model->cmd =="reset") {
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
        View::toHTML($model, "knje012bForm1.html", $arg);
    }
}
function makeSelectBtn(&$objForm, $model, $div, $name, $label, $target, $disabled = "")
{
    if (!$div || !$name || !$label || !$target) {
        return;
    } else {
        if ($div == "club") {                   //部活動
            $extra = $disabled." onclick=\"loadwindow('".REQUESTROOT."/X/KNJX_CLUB_SELECT/knjx_club_selectindex.php?program_id=".PROGRAMID."&SEND_PRGID=".PROGRAMID."&EXP_YEAR={$model->exp_year}&EXP_SEMESTER={$model->exp_semester}&SCHREGNO={$model->schregno}&NAME={$model->name}&TARGET={$target}',0,document.documentElement.scrollTop || document.body.scrollTop,800,350);\"";
        } elseif ($div == "committee") {       //委員会
            $extra = $disabled." onclick=\"loadwindow('".REQUESTROOT."/X/KNJX_COMMITTEE_SELECT/knjx_committee_selectindex.php?program_id=".PROGRAMID."&SEND_PRGID=".PROGRAMID."&EXP_YEAR={$model->exp_year}&EXP_SEMESTER={$model->exp_semester}&SCHREGNO={$model->schregno}&NAME={$model->name}&TARGET={$target}',0,document.documentElement.scrollTop || document.body.scrollTop,700,350);\"";
        } elseif ($div == "qualified") {       //検定
            $sizeW = ($model->Properties["useQualifiedMst"] == "1") ? 800 : 670;
            $extra = $disabled." onclick=\"loadwindow('".REQUESTROOT."/X/KNJX_QUALIFIED_SELECT/knjx_qualified_selectindex.php?program_id=".PROGRAMID."&SEND_PRGID=".PROGRAMID."&EXP_YEAR={$model->exp_year}&EXP_SEMESTER={$model->exp_semester}&SCHREGNO={$model->schregno}&NAME={$model->name}&TARGET={$target}',0,document.documentElement.scrollTop || document.body.scrollTop,{$sizeW},500);\"";
        } elseif ($div == "hyosyo") {          //賞
            $extra = $disabled." onclick=\"loadwindow('".REQUESTROOT."/X/KNJX_HYOSYO_SELECT/knjx_hyosyo_selectindex.php?program_id=".PROGRAMID."&SEND_PRGID=".PROGRAMID."&EXP_YEAR={$model->exp_year}&EXP_SEMESTER={$model->exp_semester}&SCHREGNO={$model->schregno}&NAME={$model->name}&TARGET={$target}',0,document.documentElement.scrollTop || document.body.scrollTop,600,350);\"";
        } elseif ($div == "batsu") {           //罰
            $extra = $disabled." onclick=\"loadwindow('".REQUESTROOT."/X/KNJX_BATSU_SELECT/knjx_batsu_selectindex.php?program_id=".PROGRAMID."&SEND_PRGID=".PROGRAMID."&EXP_YEAR={$model->exp_year}&EXP_SEMESTER={$model->exp_semester}&SCHREGNO={$model->schregno}&NAME={$model->name}&TARGET={$target}',0,document.documentElement.scrollTop || document.body.scrollTop,600,350);\"";
        } elseif ($div == "kirokubikou") {     //記録備考
            $extra = $disabled." onclick=\"loadwindow('".REQUESTROOT."/X/KNJX_CLUB_KIROKUBIKOU_SELECT/knjx_club_kirokubikou_selectindex.php?program_id=".PROGRAMID."&SEND_PRGID=".PROGRAMID."&EXP_YEAR={$model->exp_year}&EXP_SEMESTER={$model->exp_semester}&SCHREGNO={$model->schregno}&NAME={$model->name}&TARGET={$target}',0,document.documentElement.scrollTop || document.body.scrollTop,800,350);\"";
        } elseif ($div == "reason_collection") {   //年間出欠備考
            $extra = $disabled." onclick=\"loadwindow('".REQUESTROOT."/X/KNJX_REASON_COLLECTION_SELECT/knjx_reason_collection_selectindex.php?program_id=".PROGRAMID."&SEND_PRGID=".PROGRAMID."&EXP_YEAR={$model->exp_year}&EXP_SEMESTER={$model->exp_semester}&SCHREGNO={$model->schregno}&NAME={$model->name}&TARGET={$target}',0,document.documentElement.scrollTop || document.body.scrollTop,800,350);\"";
        } elseif ($div == "tuuchihyousansyou") {
            $extra = $disabled." onclick=\"loadwindow('".REQUESTROOT."/X/KNJX_TUUCHISYOKEN_SELECT/knjx_tuuchisyoken_selectindex.php?program_id=".PROGRAMID."&SEND_PRGID=".PROGRAMID."&EXP_YEAR={$model->exp_year}&EXP_SEMESTER={$model->exp_semester}&SCHREGNO={$model->schregno}&NAME={$model->name}&TARGET={$target}',0,document.documentElement.scrollTop || document.body.scrollTop,900,350);\"";
        }
        return knjCreateBtn($objForm, $name, $label, $extra);
    }
}
