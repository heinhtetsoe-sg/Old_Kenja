<?php

require_once('for_php7.php');

class knje360bForm1
{
    public function main(&$model)
    {
        //オブジェクト作成
        $objForm = new form();

        //フォーム作成
        $arg["start"] = $objForm->get_start("edit", "POST", "knje360bindex.php", "", "edit");

        //DB接続
        $db = Query::dbCheckOut();

        // 入試カレンダーの使用フラグ
        $arg["useCollegeExamCalendar"] = "";
        if ($model->Properties["useCollegeExamCalendar"] === '1') {
            $arg["useCollegeExamCalendar"] = "1";
        }
        knjCreateHidden($objForm, "useCollegeExamCalendar", $arg["useCollegeExamCalendar"]);

        //生徒情報取得
        $info = $db->getRow(knje360bQuery::getSchInfo($model), DB_FETCHMODE_ASSOC);
        $ban = ($info["ATTENDNO"]) ? '番　' : '　';
        $arg["SCHINFO"] = $info["HR_NAME"].' '.$info["ATTENDNO"].$ban.$info["NAME_SHOW"];

        //顔写真
        $arg["img"]["FACE_IMG"] = REQUESTROOT."/".$model->control["LargePhotoPath"]."/P".$model->schregno.".".$model->control["Extension"];
        $arg["img"]["IMG_PATH"] = REQUESTROOT."/".$model->control["LargePhotoPath"]."/P".$model->schregno.".".$model->control["Extension"];

        if ($model->Properties["useDispUnDispPicture"] === '1') {
            $arg["unDispPicture"] = "1";
        } else {
            $arg["dispPicture"]   = "1";
        }

        //部クラブ名取得
        $setClubName = "";
        $setComma    = "";
        $query = knje360bQuery::getClubName($model);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $setClubName .= $setComma.$row["CLUBNAME"];
            $setComma     = ",";
        }
        $arg["data"]["CLUB"] = $setClubName;

        //委員会取得
        $model->getname = $db->getOne(knje360bQuery::getHyoujiset());
        $commi = $db->getRow(knje360bQuery::getCommitName($model), DB_FETCHMODE_ASSOC);
        if ($commi["COMMITTEENAME"] && $commi["CHARGENAME"]) {
            $arg["data"]["COMMI_CHARGE"] = $commi["COMMITTEENAME"]."<br>".$commi["CHARGENAME"];
        } else {
            $arg["data"]["COMMI_CHARGE"] = $commi["COMMITTEENAME"].$commi["CHARGENAME"];
        }

        //評定平均取得
        $average = $db->getOne(knje360bQuery::getValueAverage($model));
        $arg["data"]["AVERAGE"] = $average;

        //出身学校名取得
        $finschool = $db->getOne(knje360bQuery::getFinschoolName($model));
        $arg["data"]["FINSCHOOL"] = $finschool;

        //出力データ選択ラジオボタン 1:全て 2:受験報告（進学） 3.受験報告（就職）
        $def = $model->Properties["knje360bShubetDefault"];
        if ($def != "1" && $def != "3") {
            $def = "2";
        }
        $opt = array(1, 2, 3);
        $model->output = ($model->output == "") ? $def : $model->output;
        $click = " onclick=\"return btn_submit('edit');\"";
        $extra = array("id=\"OUTPUT1\"".$click, "id=\"OUTPUT2\"".$click, "id=\"OUTPUT3\"".$click);
        $radioArray = knjCreateRadio($objForm, "OUTPUT", $model->output, $extra, $opt, get_count($opt));
        foreach ($radioArray as $key => $val) {
            $arg["data"][$key] = $val;
        }

        //初期化
        if ($model->sort == "") {
            $model->sort        = "date";
            $model->asc_or_desc = -1;
            $asc_or_desc        = "DESC";
        }

        //受験番号でのソート（昇順・降順）
        if ($model->schregno != "" && $model->output == "2") {
            if ($model->cmd == "sort" && $model->KeepSort == $model->sort) {
                $model->asc_or_desc = (int)$model->asc_or_desc * -1;
            }

            if ($model->asc_or_desc == 1) {
                $mark = '▲';
                $asc_or_desc = "ASC";
            } else {
                $mark = '▼';
                $asc_or_desc = "DESC";
            }

            //日付でのソート（昇順・降順）
            $linkData = array("cmd" => "sort", "sort" => "date", "SEND_SCHREGNO" => $model->schregno, "SEND_OUTPUT" => $model->output);
            $label_mark = ($model->sort == "date") ? $mark : "";
            $arg["DATE_LABEL"] = View::alink("knje360bindex.php", "日付{$label_mark}", " style=\"text-decoration:underline; color:white;\" ", $linkData);

            //学校でのソート（昇順・降順）
            $linkData = array("cmd" => "sort", "sort" => "college", "SEND_SCHREGNO" => $model->schregno, "SEND_OUTPUT" => $model->output);
            $label_mark = ($model->sort == "college") ? $mark : "";
            $label = View::alink("knje360bindex.php", "学校名{$label_mark}", " style=\"text-decoration:underline; color:white;\" ", $linkData);

            $model->KeepSort = $model->sort;
        } else {
            $arg["DATE_LABEL"]  = "日付";
            $label              = "学校名";
        }

        //項目表示
        $labelArray = array();
        // 入試カレンダーの使用
        if ($model->Properties["useCollegeExamCalendar"] === '1') {
            $labelArray["1"] = array("1" => "１",   "2" => "進路",      "3" => "進路");
            $labelArray["2"] = array("1" => "２",   "2" => $label,      "3" => "会社名");
            $labelArray["3"] = array("1" => "３",   "2" => "学部名",    "3" => "");
            $labelArray["4"] = array("1" => "４",   "2" => "学科名",    "3" => "");
            $labelArray["5"] = array("1" => "５",   "2" => "日程",      "3" => "");
            $labelArray["6"] = array("1" => "６",   "2" => "受験方式",  "3" => "");
            $labelArray["7"] = array("1" => "７",   "2" => "受験結果",  "3" => "");
            $labelArray["8"] = array("1" => "８",   "2" => "進路状況",  "3" => "");
            $labelArray["9"] = array("1" => "９",   "2" => "方式",      "3" => "");
        } else {
            $labelArray["1"] = array("1" => "１",   "2" => "進路",      "3" => "進路");
            $labelArray["2"] = array("1" => "２",   "2" => $label,      "3" => "会社名");
            $labelArray["3"] = array("1" => "３",   "2" => "学部名",    "3" => "");
            $labelArray["4"] = array("1" => "４",   "2" => "学科名",    "3" => "");
            $labelArray["5"] = array("1" => "５",   "2" => "受験方式",  "3" => "");
            $labelArray["6"] = array("1" => "６",   "2" => "受験結果",  "3" => "");
            $labelArray["7"] = array("1" => "７",   "2" => "進路状況",  "3" => "");
            $labelArray["8"] = array("1" => "",     "2" => "",         "3" => "");
            $labelArray["9"] = array("1" => "",     "2" => "",         "3" => "");
        }
        for ($i = 1; $i <= 9; $i++) {
            $arg["LABEL".$i] = $labelArray[$i][$model->output];
        }

        //ALLチェック
        $arg["CHECKALL"] = knjCreateCheckBox($objForm, "CHECKALL", "", "onClick=\"return check_all(this);\"", "");

        //データを取得
        $setval = array();
        $firstflg = true;   //初回フラグ
        $cnt = get_count($db->getcol(knje360bQuery::selectQuery($model, $asc_or_desc)));
        if ($model->schregno && $cnt) {
            $result = $db->query(knje360bQuery::selectQuery($model, $asc_or_desc));
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $row["DATE"] = str_replace("-", "/", $row["ENTRYDATE"]);
                $row["DATE"] = View::alink(
                    "knje360bindex.php",
                    $row["DATE"],
                    "target=_self tabindex=\"-1\"",
                    array("SCHREGNO"        => $row["SCHREGNO"],
                                                  "ENTRYDATE"       => $row["ENTRYDATE"],
                                                  "SEQ"             => $row["SEQ"],
                                                  "cmd"             => "subform".$row["KINDCD"]."A",
                                                  "TYPE"            => "list")
                );

                if ($firstflg) {
                    $setval = $row;
                    $firstflg = false;
                } else {
                    $entry = $setval["ENTRYDATE"].':'.$setval["SEQ"].':'.$setval["KINDCD"];
                    $setval["CHECKED"] = knjCreateCheckBox($objForm, "CHECKED", $entry, "", "1");
                    $arg["list"][] = $setval;
                    $setval = $row;
                }
            }
            $entry = $setval["ENTRYDATE"].':'.$setval["SEQ"].':'.$setval["KINDCD"];
            $setval["CHECKED"] = knjCreateCheckBox($objForm, "CHECKED", $entry, "", "1");

            $arg["list"][] = $setval;
        }

        //ボタン作成
        makeBtn($objForm, $arg, $model);

        //hidden作成
        makeHidden($objForm, $model);

        //DB切断
        Query::dbCheckIn($db);

        $arg["IFRAME"] = VIEW::setIframeJs();

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knje360bForm1.html", $arg);
    }
}

//ボタン作成
function makeBtn(&$objForm, &$arg, $model)
{
    $disabled = ($model->schregno) ? "": " disabled";
    //進路調査ボタン
    $extra = "style=\"height:30px;background:#00FFFF;color:#000080;font:bold\" onclick=\"return btn_submit('subform1');\"";
    $arg["button"]["btn_subform1"] = KnjCreateBtn($objForm, "btn_subform1", "進路調査", $extra.$disabled);
    //受験報告（進学）ボタン
    $extra = "style=\"height:30px;background:#ADFF2F;color:#006400;font:bold\" onclick=\"return btn_submit('subform2');\"";
    $arg["button"]["btn_subform2"] = KnjCreateBtn($objForm, "btn_subform2", "受験報告（進学）", $extra.$disabled);
    //受験報告（就職）ボタン
    $extra = "style=\"height:30px;background:#FFFF00;color:#FF8C00;font:bold\" onclick=\"return btn_submit('subform3');\"";
    $arg["button"]["btn_subform3"] = KnjCreateBtn($objForm, "btn_subform3", "受験報告（就職）", $extra.$disabled);
    //その他ボタン
    $extra = "style=\"height:30px;background:#C0FFFF;color:#1E90FF;font:bold\" onclick=\"return btn_submit('subform5');\"";
    $arg["button"]["btn_subform5"] = KnjCreateBtn($objForm, "btn_subform5", "その他", $extra.$disabled);

    //進路相談ボタン
    $extra = "style=\"height:30px;background:#FFE4E1;color:#FF0000;font:bold\" onclick=\"return btn_submit('subform4');\"";
    $arg["button"]["btn_subform4"] = KnjCreateBtn($objForm, "btn_subform4", "進路相談", $extra.$disabled);

    //受験報告（進学）一括入力ボタン
    $extra = "style=\"height:30px;background:#ADFF2F;color:#006400;font:bold\" onclick=\"return btn_submit('subform6');\"";
    $arg["button"]["btn_subform6"] = KnjCreateBtn($objForm, "btn_subform6", "受験報告（進学）一括入力", $extra.$disabled);

    //削除ボタンを作成する
    $extra = "style=\"height:30px\" onclick=\"return btn_submit('delete');\"";
    $arg["button"]["btn_del"] = knjCreateBtn($objForm, "btn_del", "削 除", $extra);
    //終了ボタンを作成する
    $extra = "style=\"height:30px\" onclick=\"closeWin();\"";
    $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);
}

//hidden作成
function makeHidden(&$objForm, $model)
{
    knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
    knjCreateHidden($objForm, "SCHREGNO", $model->schregno);
    knjCreateHidden($objForm, "cmd");
}
