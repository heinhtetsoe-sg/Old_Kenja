<?php

require_once('for_php7.php');
class knjh400_SeitoKankyouForm1
{
    public function main(&$model)
    {
        $objForm = new form();

        if (isset($model->schregno) && !isset($model->warning) && VARS::post("cmd")!="update") {
            $row = knjh400_SeitoKankyouQuery::getSchregEnvirDat($model);
            if ($row["COMMUTE_HOURS"]) {
                $row["COMMUTE_HOURS"] = sprintf("%d", $row["COMMUTE_HOURS"]);
            }
            if ($row["COMMUTE_MINUTES"]) {
                $row["COMMUTE_MINUTES"] = sprintf("%d", $row["COMMUTE_MINUTES"]);
            }
        } else {
            $row =& $model->field;
        }
        $db = Query::dbCheckOut();
        if ($model->schregno) {
            $nameShow = $db->getOne(knjh400_SeitoKankyouQuery::getName($model));
            $arg["name"] = $model->schregno."&nbsp;&nbsp;：&nbsp;&nbsp;".$nameShow;
        } else {
            $arg["name"] = "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;:";
        }


        //入学以前通っていた塾
        $extra = '';
        $arg["data"]["OLD_CRAM"] = knjCreateTextArea($objForm, "OLD_CRAM", 2, 20, "hard", $extra, $row["OLD_CRAM"]);

        //現在通っている塾(コンボ)
        $query = knjh400_SeitoKankyouQuery::getVNameMst("H101", $model);
        $extra = '';
        makeCmb($objForm, $arg, $db, $query, "CUR_CRAMCD", $row["CUR_CRAMCD"], $extra, 1, "0");

        //現在通っている塾(塾名)
        $extra = "";
        $arg["data"]["CUR_CRAM"] = knjCreateTextBox($objForm, $row["CUR_CRAM"], "CUR_CRAM", 20, 10, $extra);

        //学習時間(コンボ)
        $query = knjh400_SeitoKankyouQuery::getVNameMst("H103", $model);
        $extra = '';
        makeCmb($objForm, $arg, $db, $query, "STUDYTIME", $row["STUDYTIME"], $extra, 1, "0");

        //けいこごと(コンボ)
        $query = knjh400_SeitoKankyouQuery::getVNameMst("H102", $model);
        $extra = '';
        makeCmb($objForm, $arg, $db, $query, "LESSONCD", $row["LESSONCD"], $extra, 1, "0");

        //けいこごと(テキストボックス)
        $extra = "";
        $arg["data"]["LESSON"] = knjCreateTextBox($objForm, $row["LESSON"], "LESSON", 20, 10, $extra);

        //賞罰・検定・その他
        $extra = '';
        $arg["data"]["PRIZES"] = knjCreateTextArea($objForm, "PRIZES", 4, 20, "hard", $extra, $row["PRIZES"]);

        //兄弟姉妹調査(コンボ)
        $query = knjh400_SeitoKankyouQuery::getVNameMst("H107", $model);
        $extra = '';
        makeCmb($objForm, $arg, $db, $query, "BRO_SISCD", $row["BRO_SISCD"], $extra, 1, "0");

        //住居調査(コンボ)
        $query = knjh400_SeitoKankyouQuery::getVNameMst("H108", $model);
        $extra = '';
        makeCmb($objForm, $arg, $db, $query, "RESIDENTCD", $row["RESIDENTCD"], $extra, 1, "0");
        //住居調査(REMARK1)
        $extra = "";
        $arg["data"]["REMARK1"] = knjCreateTextBox($objForm, $row["REMARK1"], "REMARK1", 20, 20, $extra);
        //住居調査(REMARK2)
        $extra = "onBlur=\"return to_Integer(this);\"";
        $arg["data"]["REMARK2"] = knjCreateTextBox($objForm, $row["REMARK2"], "REMARK2", 7, 5, $extra);

        //スポーツ
        $extra = '';
        $arg["data"]["SPORTS"] = knjCreateTextArea($objForm, "SPORTS", 2, 20, "hard", $extra, $row["SPORTS"]);

        //交友
        $extra = '';
        $arg["data"]["FRIENDSHIP"] = knjCreateTextArea($objForm, "FRIENDSHIP", 2, 20, "hard", $extra, $row["FRIENDSHIP"]);

        //卒業後の進路：進学
        $extra = '';
        $arg["data"]["PLANUNIV"] = knjCreateTextArea($objForm, "PLANUNIV", 2, 20, "hard", $extra, $row["PLANUNIV"]);

        //卒業後の進路：就職
        $extra = '';
        $arg["data"]["PLANJOB"] = knjCreateTextArea($objForm, "PLANJOB", 2, 20, "hard", $extra, $row["PLANJOB"]);

        //特別教育活動
        $extra = '';
        $arg["data"]["ED_ACT"] = knjCreateTextArea($objForm, "ED_ACT", 2, 20, "hard", $extra, $row["ED_ACT"]);

        //備考
        $extra = '';
        $arg["data"]["REMARK"] = knjCreateTextArea($objForm, "REMARK", 4, 20, "hard", $extra, $row["REMARK"]);

        //災害時帰宅グループ番号
        $query = knjh400_SeitoKankyouQuery::getGoHome();
        $extra = '';
        makeCmb($objForm, $arg, $db, $query, "GO_HOME_GROUP_NO", $row["GO_HOME_GROUP_NO"], $extra, 1, "BLANK");

        //責任者
        $query = knjh400_SeitoKankyouQuery::getResponsibility();
        $extra = '';
        makeCmb($objForm, $arg, $db, $query, "RESPONSIBILITY", $row["RESPONSIBILITY"], $extra, 1, "BLANK");

        //通学：所要時間
        $extra = "onBlur=\"return to_Integer(this);\"";
        $arg["data"]["COMMUTE_HOURS"] = knjCreateTextBox($objForm, $row["COMMUTE_HOURS"], "COMMUTE_HOURS", 2, 2, $extra);

        //通学：所要分
        $extra = "onBlur=\"return to_Integer(this);\"";
        $arg["data"]["COMMUTE_MINUTES"] = knjCreateTextBox($objForm, $row["COMMUTE_MINUTES"], "COMMUTE_MINUTES", 2, 2, $extra);

        //通学方法
        $query = knjh400_SeitoKankyouQuery::getVNameMst("H100", $model);
        $extra = '';
        makeCmb($objForm, $arg, $db, $query, "HOWTOCOMMUTECD", $row["HOWTOCOMMUTECD"], $extra, 1, "0");

        //radio
        $opt = array(1, 2);
        $row["UP_DOWN"] = ($row["UP_DOWN"] == "") ? "1" : $row["UP_DOWN"];
        $extra = array();
        foreach ($opt as $key => $val) {
            array_push($extra, " id=\"UP_DOWN{$val}\" ");
        }
        $radioArray = knjCreateRadio($objForm, "UP_DOWN", $row["UP_DOWN"], $extra, $opt, count($opt));
        foreach ($radioArray as $key => $val) {
            $arg["data"][$key] = $val;
        }


        //途中経由駅
        for ($i = 1; $i <= 7; $i++) {
            $josya = "JOSYA_" . $i;
            $rosen = "ROSEN_" . $i;
            $gesya = "GESYA_" . $i;
            $flg   = "FLG_" . $i;
            $extra = "disabled=\"true\" class=\"right_side\" style=\"width:270px;\"";

            $hidden_josya = "HIDDEN_JOSYA_" . $i;
            $hidden_rosen = "HIDDEN_ROSEN_" . $i;
            $hidden_gesya = "HIDDEN_GESYA_" . $i;


            if ($row[$flg] == '1') {
                $josya_cd = $row[$hidden_josya] ? $row[$hidden_josya] : $row[$josya];
                $rosen_cd = $row[$hidden_rosen] ? $row[$hidden_rosen] : $row[$rosen];
                $gesya_cd = $row[$hidden_gesya] ? $row[$hidden_gesya] : $row[$gesya];

                $query = knjh400_SeitoKankyouQuery::getStationName($josya_cd);
                list($josya_mei, $rosen_mei) = $db->getRow($query);
                $query = knjh400_SeitoKankyouQuery::getStationName($gesya_cd);
                list($gesya_mei, $rosen_mei) = $db->getRow($query);
            } elseif ($row[$flg] == '3') {
                $rosen_cd = $row[$hidden_rosen] ? $row[$hidden_rosen] : $row[$rosen];

                $query = knjh400_SeitoKankyouQuery::getBusCourse($rosen_cd);
                list($rosen_cd, $rosen_mei) = $db->getRow($query);

                $josya_mei = $row[$josya];
                $gesya_mei = $row[$gesya];
            } else {
                $josya_mei = $row[$josya];
                $rosen_mei = $row[$rosen];
                $gesya_mei = $row[$gesya];
            }

            $arg["data"][$josya] = knjCreateTextBox($objForm, $josya_mei, $josya, 20, 20, $extra);
            $arg["data"][$rosen] = knjCreateTextBox($objForm, $rosen_mei, $rosen, 20, 20, $extra);
            $arg["data"][$gesya] = knjCreateTextBox($objForm, $gesya_mei, $gesya, 20, 20, $extra);
            knjCreateHidden($objForm, $flg, $row[$flg]);
            knjCreateHidden($objForm, $hidden_josya, $josya_cd);
            knjCreateHidden($objForm, $hidden_rosen, $rosen_cd);
            knjCreateHidden($objForm, $hidden_gesya, $gesya_cd);
        }

        /**************/
        /* ボタン作成 */
        /**************/
        //性質・学業・他
        $link = REQUESTROOT."/H/KNJH400_SEITOKANKYOU2/knjh400_SeitoKankyou2index.php?cmd=subform1&SCHREGNO=".$model->schregno."&PRG=".VARS::get("PRG");
        $extra = "onclick=\"Page_jumper('{$link}');\"";
        $arg["button"]["OTHER"] = knjCreateBtn($objForm, 'OTHER', '性質・学業・他', $extra);

        //更新ボタン
        $extra = "onclick=\"return btn_submit('update');\"";
        $arg["button"]["btn_update"] = knjCreateBtn($objForm, 'btn_update', '更 新', $extra);

        //更新後前の生徒へボタン
        $arg["button"]["btn_up_next"]    = View::updateNext($model, $objForm, 'btn_update');

        //削除ボタン
        $extra = "onclick=\"return btn_submit('delete');\"";
        $arg["button"]["btn_del"] = knjCreateBtn($objForm, 'btn_del', '削 除', $extra);

        //取消ボタン
        $extra = "onclick=\"return btn_submit('reset');\"";
        $arg["button"]["btn_reset"] = knjCreateBtn($objForm, 'btn_reset', '取 消', $extra);

        //終了ボタン
        $extra = "onclick=\"closeWin();\"";
        $arg["button"]["btn_back"] = knjCreateBtn($objForm, 'btn_back', '戻 る', $extra);

        /********/
        /*hidden*/
        /********/
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "SCHREGNO", $model->schregno);
        knjCreateHidden($objForm, "REQUESTROOT", REQUESTROOT);

        //通学方法（スクールバス）
        $school_bus = $db->getCol(knjh400_SeitoKankyouQuery::getSchoolBus($model));
        knjCreateHidden($objForm, "SCHOOL_BUS", implode(',', $school_bus ? $school_bus : array()));

        if (get_count($model->warning) == 0 && $model->cmd != "reset") {
            $arg["next"] = "NextStudent(0);";
        } elseif ($model->cmd =="reset") {
            $arg["next"] = "NextStudent(1);";
        }

        Query::dbCheckIn($db);

        $arg["start"] = $objForm->get_start("edit", "POST", "knjh400_SeitoKankyouindex.php", "", "edit");
        $arg["finish"] = $objForm->get_finish();
        //インラインフレーム用Javascriptタグ生成
        $arg["IFRAME"] = View::setIframeJs();
        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す
        View::toHTML($model, "knjh400_SeitoKankyouForm1.html", $arg);
    }
}
/********************************************** 以下関数 **********************************************************/
//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, $value, $extra, $size, $blank = "")
{
    $opt = array();
    if ($blank == "BLANK") {
        $opt[] = array('label' => "",
                       'value' => "");
    } elseif ($blank == "0") {
        $opt[] = array('label' => "",
                       'value' => "0");
    }
    $value_flg = false;
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);

        if ($value == $row["VALUE"]) {
            $value_flg = true;
        }
    }
    $result->free();

    if ($name == "SEMESTER") {
        $value = ($value && $value_flg) ? $value : CTRL_SEMESTER;
    } else {
        $value = ($value && $value_flg) ? $value : $opt[0]["value"];
    }

    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}
