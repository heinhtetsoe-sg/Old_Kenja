<?php

require_once('for_php7.php');

class knje360aForm1 {

    function main(&$model) {

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("edit", "POST", "knje360aindex.php", "", "edit");

        //DB接続
        $db = Query::dbCheckOut();

        // 入試カレンダーの使用フラグ
        $arg["useCollegeExamCalendar"] = "";
        if ($model->Properties["useCollegeExamCalendar"] === '1') {
            $arg["useCollegeExamCalendar"] = "1";
        }

        //年度
        $arg["YEAR"] = CTRL_YEAR;

        //データ選択ラジオボタン 1:入力済み 2:未入力 3:全て
        $opt = array(1, 2, 3);
        $model->data_select = ($model->data_select == "") ? "2" : $model->data_select;
        $click = " onclick=\"return btn_submit('edit');\"";
        $extra = array("id=\"DATA_SELECT1\"".$click, "id=\"DATA_SELECT2\"".$click, "id=\"DATA_SELECT3\"".$click);
        $radioArray = knjCreateRadio($objForm, "DATA_SELECT", $model->data_select, $extra, $opt, get_count($opt));
        foreach($radioArray as $key => $val) $arg[$key] = $val;

        //初期化
        if ($model->cmd == "") {
            $model->sort = "hr_name";
            $model->asc_or_desc = 1;
        }

        //受験番号でのソート（昇順・降順）
        if ($model->schoolcd != "") {
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

            //年組番でのソート（昇順・降順）
            $linkData = array("cmd" => "sort", "sort" => "hr_name", "SCH_CD" => $model->schoolcd, "SEND_DATA_SELECT" => $model->data_select);
            $label_mark = ($model->sort == "hr_name") ? $mark : "";
            $arg["HR_NAME_LABEL"] = View::alink("knje360aindex.php", "<font color='white'>クラス{$label_mark}</font>", "", $linkData);

            //受験番号でのソート（昇順・降順）
            $linkData = array("cmd" => "sort", "sort" => "examno", "SCH_CD" => $model->schoolcd, "SEND_DATA_SELECT" => $model->data_select);
            $label_mark = ($model->sort == "examno") ? $mark : "";
            $arg["EXAMNO_LABEL"] = View::alink("knje360aindex.php", "<font color='white'>受験番号{$label_mark}</font>", "", $linkData);

            $model->KeepSort = $model->sort;

        } else {
            $arg["HR_NAME_LABEL"]   = "<font color='white'>クラス</font>";
            $arg["EXAMNO_LABEL"]    = "<font color='white'>受験番号</font>";
        }

        //エラー時のセット用データ
        if (isset($model->warning)) {
            $dataTmp = $dataItem = array();
            $seq_array = preg_split("/,/", $model->seq_list);

            foreach ($model->fields as $key => $val) {
                $dataItem[] = $key;

                foreach ($seq_array as $skey) {
                    $dataTmp[$skey][$key] = $model->fields[$key][$skey];
                }
            }
        }

        //データを取得
        $setval = array();
        $tmpSeq = "";
        if ($model->schoolcd != "") {
            $query = knje360aQuery::getDataList($model, $asc_or_desc);
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $setval[$row["SEQ"]] = $row;

                //エラー時、データ置き換え
                if (isset($model->warning)) {
                    foreach ($dataItem as $key) {
                        $setval[$row["SEQ"]][$key] = $dataTmp[$row["SEQ"]][$key];
                    }
                }
                $tmpSeq .= ($tmpSeq == "") ? $row["SEQ"] : ",".$row["SEQ"];
            }
            $result->free();
        }
        knjCreateHidden($objForm, "SEQ_LIST", $tmpSeq);

        //リンク対象項目名リスト
        $list = array();
        $list["DECISION"] = "受験結果";
        $list["PLANSTAT"] = "進路状況";

        //項目名セット
        foreach ($list as $item => $label) {
            if ($model->schoolcd != "" && $tmpSeq > 1) {
                $linkData = "loadwindow('knje360aindex.php?cmd=replace&REP_ITEM=".$item."&REP_ITEM_LABEL=".$label."&SEND_DATA_SELECT=".$model->data_select."&SEQ_LIST=".$tmpSeq."',0,0,380,220)";
                $arg[$item."_LABEL"] = View::alink("#", $label, "onclick=\"$linkData\"");
            } else {
                $arg[$item."_LABEL"] = $label;
            }
        }

        //checkbox
        $extra = " onClick=\"allCheck(this)\" ";
        $arg["TAISYOU_ALL"] = knjCreateCheckBox($objForm, "TAISYOU_ALL", "1", $extra);

        //データ一覧を表示
        $setData = array();
        $counter = 0;
        $color_flg = false;
        foreach ($setval as $seq => $Row) {
            //checkbox
            $extra = "";
            $setData["TAISYOU"] = knjCreateCheckBox($objForm, "TAISYOU"."-".$seq, "1", $extra);

            //卒業年度
            $Row["GRD_YEAR"] = ($Row["GRD_YEAR"] == "9999") ? "" : $Row["GRD_YEAR"];
            $setData["GRD_YEAR"] = $Row["GRD_YEAR"];

            //年組
            $setData["HR_NAME"] = $Row["HR_NAME"];

            //出席番号
            $setData["ATTENDNO"] = $Row["ATTENDNO"];

            //学籍番号
            $setData["SCHREGNO"] = $Row["SCHREGNO"];

            //氏名
            $setData["NAME_SHOW"] = $Row["NAME_SHOW"];

            //受験結果
            $extra = "";
            $query = knje360aQuery::getNameMst('E005');
            $setData["DECISION"] = makeCmbReturn($objForm, $arg, $db, $query, "DECISION"."-".$seq, $Row["DECISION"], $extra, 1, "BLANK");

            //進路状況
            $extra = "";
            $query = knje360aQuery::getNameMst('E006');
            $setData["PLANSTAT"] = makeCmbReturn($objForm, $arg, $db, $query, "PLANSTAT"."-".$seq, $Row["PLANSTAT"], $extra, 1, "BLANK");

            //受験方式
            $setData["HOWTOEXAM"] = $db->getOne(knje360aQuery::getHowtoexamName($Row["HOWTOEXAM"]));

            //学校コード
            $setData["SCHOOL_CD"] = $Row["SCHOOL_CD"];
            knjCreateHidden($objForm, "SCHOOL_CD"."-".$seq, $Row["SCHOOL_CD"]);
            //学校名
            $setData["SCHOOL_NAME"] = $Row["SCHOOL_NAME"];

            //学部
            $setData["FACULTYNAME"] = $Row["FACULTYNAME"];
            knjCreateHidden($objForm, "FACULTYCD"."-".$seq, $Row["FACULTYCD"]);

            //学科
            $setData["DEPARTMENTNAME"] = $Row["DEPARTMENTNAME"];
            knjCreateHidden($objForm, "DEPARTMENTCD"."-".$seq, $Row["DEPARTMENTCD"]);

            //受験番号
            $extra = "";
            $setData["EXAMNO"] = knjCreateTextBox($objForm, $Row["EXAMNO"], "EXAMNO"."-".$seq, 10, 10, $extra);

            if ($model->Properties["useCollegeExamCalendar"] === '1') {
                //募集区分
                $setData["ADVERTISE_NAME"] = $Row["ADVERTISE_NAME"];
                knjCreateHidden($objForm, "ADVERTISE_DIV"."-".$seq, $Row["ADVERTISE_DIV"]);
                //日程
                $setData["PROGRAM_NAME"] = $Row["PROGRAM_NAME"];
                knjCreateHidden($objForm, "PROGRAM_CD"."-".$seq, $Row["PROGRAM_CD"]);
                //方式
                $setData["FORM_NAME"] = $Row["FORM_NAME"];
                knjCreateHidden($objForm, "FORM_CD"."-".$seq, $Row["FORM_CD"]);
                //大分類
                $setData["L_NAME"] = $Row["L_NAME"];
                knjCreateHidden($objForm, "L_CD"."-".$seq, $Row["L_CD"]);
                //小分類
                $setData["S_NAME"] = $Row["S_NAME"];
                knjCreateHidden($objForm, "S_CD"."-".$seq, $Row["S_CD"]);
            }

            //合格発表日
            $setData["STAT_DATE3"] = str_replace("-", "/", $Row["STAT_DATE3"]);

            //背景色
            if ($counter % 5 == 0) $color_flg = !$color_flg;
            $setData["BGCOLOR"] = ($color_flg == true) ? "#ffffff" : "#cccccc";

            $arg["data"][] = $setData;
            $counter++;
        }

        //ボタン作成
        makeBtn($objForm, $arg, $model);

        //hidden作成
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "useFrameLock", $model->Properties["useFrameLock"]);
        knjCreateHidden($objForm, "SCHOOLCD", $model->schoolcd);
        knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
        knjCreateHidden($objForm, "PRGID", "KNJE360A");
        knjCreateHidden($objForm, "CTRL_YEAR", CTRL_YEAR);
        knjCreateHidden($objForm, "CTRL_SEMESTER", CTRL_SEMESTER);
        knjCreateHidden($objForm, "CTRL_DATE", CTRL_DATE);
        knjCreateHidden($objForm, "param_asc_or_desc", $asc_or_desc);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"] = $objForm->get_finish();

        //インラインフレーム用Javascriptタグ生成
        $arg["IFRAME"] = View::setIframeJs();

        View::toHTML($model, "knje360aForm1.html", $arg); 
    }
}

//コンボ作成（表内）
function makeCmbReturn(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $blank="") {
    $opt = array();
    $value_flg = false;
    if ($blank) $opt[] = array("label" => "", "value" => "");
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array ("label" => $row["LABEL"],
                        "value" => $row["VALUE"]);
        if ($value == $row["VALUE"]) $value_flg = true;
    }
    $result->free();

    $value = ($value != "" && $value_flg) ? $value : $opt[0]["value"];

    return knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}

//ボタン作成
function makeBtn(&$objForm, &$arg, $model) {
    //学校検索
    $linkData = "loadwindow('knje360aindex.php?cmd=select&SEND_DATA_SELECT=".$model->data_select."',0,0,450,430)";
    $extra = "style=\"height:30px;font:bold;\" onclick=\"$linkData\"";
    $arg["button"]["btn_select"] = knjCreateBtn($objForm, "btn_select", "学校選択", $extra);

    //更新
    $extra = "onclick=\"return btn_submit('update');\"";
    $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);
    //取消
    $extra = "onclick=\"return btn_submit('reset');\"";
    $arg["button"]["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);
    //印刷ボタン
    $extra = "onclick=\"return newwin('" . SERVLET_URL . "');\"";
    $arg["button"]["btn_print"] = knjCreateBtn($objForm, "btn_print", "プレビュー／印刷", $extra);
    //終了
    $extra = "onclick=\"closeWin();\"";
    $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);
}
?>
