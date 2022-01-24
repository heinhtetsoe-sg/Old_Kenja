<?php

require_once('for_php7.php');

/**************************/
/* 学習状況通知票 KNJM500 */
/**************************/

class knjm550m_1Form1
{
    public function main(&$model)
    {

        //オブジェクト作成
        $objForm = new form();

        //フォーム作成
        $arg["start"]   = $objForm->get_start("knjm550m_1Form1", "POST", "knjm550mindex.php", "", "knjm550m_1Form1");

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["data"]["YEAR"] = CTRL_YEAR;

        //学期
        $arg["data"]["GAKKI"] = CTRL_SEMESTER;

        //出力データラジオボタン 1:学習状況通知票 2:宛名封筒 3:生徒住所タックシール
        $opt_data = array(1, 2, 3);
        $model->type_div = ($model->type_div == "") ? "1" : $model->type_div;
        $extra = array(" onClick=\"btn_submit('knjm550m_1')\"", " onClick=\"btn_submit('knjm550m_2')\"", " onClick=\"btn_submit('knjm550m_3')\"");
        $radioArray = knjCreateRadio($objForm, "TYPE_DIV", $model->type_div, $extra, $opt_data, get_count($opt_data));
        foreach ($radioArray as $key => $val) {
            $arg["data"][$key] = $val;
        }

        //抽出条件ラジオボタン 1:AND検索 2:OR検索
        $opt_mSearc = array(1, 2);
        $model->mainSearch = ($model->mainSearch == "") ? "1" : $model->mainSearch;
        $extra = array(" onClick=\"btn_submit('knjm550m_1')\"", " onClick=\"btn_submit('knjm550m_1')\"");
        $radioArray = knjCreateRadio($objForm, "MAIN_SEARCH", $model->mainSearch, $extra, $opt_mSearc, get_count($opt_mSearc));
        foreach ($radioArray as $key => $val) {
            $arg["data"][$key] = $val;
        }

        //パネル作成
        for ($i = 1; $i <= $model->panelCnt; $i++) {
            //タイトル
            $setData["JOUKEN"] = "条件".$i;

            //受講科目コンボ3
            $query = knjm550mQuery::getChairStd($model);
            $setData["SUBCLASS"] = makeCombo($objForm, $arg, $db, $query, $model->searchField["SUBCLASS".$i], "SUBCLASS".$i, "", 1, "BLANK");

            //検索方法ラジオボタン 1:AND 2:OR
            $searchType = array(1, 2);
            $model->searchField["SEARCH_S".$i] = ($model->searchField["SEARCH_S".$i] == "") ? "1" : $model->searchField["SEARCH_S".$i];
            $searchArray = knjCreateRadio($objForm, "SEARCH_S".$i, $model->searchField["SEARCH_S".$i], "", $searchType, get_count($searchType));
            $searchCnt = 1;
            foreach ($searchArray as $key => $val) {
                $setData["SEARCH_S".$searchCnt] = $val;
                $setData["FOR_SEARCH".$searchCnt] = "SEARCH_S".$i.$searchCnt;
                $searchCnt++;
            }

            //レポートコンボ
            $query = knjm550mQuery::getKaisu();
            $setData["REPORT"] = makeCombo($objForm, $arg, $db, $query, $model->searchField["REPORT".$i], "REPORT".$i, "", 1, "BLANK");

            //レポートラジオボタン 1:回まで提出済み 2:回まで合格 3:回までに不合格・未提出あり
            $repSType = array(1, 2, 3);
            $model->searchField["REPORT_S".$i] = ($model->searchField["REPORT_S".$i] == "") ? "1" : $model->searchField["REPORT_S".$i];
            $repSArray = knjCreateRadio($objForm, "REPORT_S".$i, $model->searchField["REPORT_S".$i], "", $repSType, get_count($repSType));
            $repSCnt = 1;
            foreach ($repSArray as $key => $val) {
                $setData["REPORT_S".$repSCnt] = $val;
                $setData["FOR_REP".$repSCnt] = "REPORT_S".$i.$repSCnt;
                $repSCnt++;
            }

            //出席回数
            $extraRight = "STYLE=\"text-align: right\"";
            $extraInt = "onblur=\"this.value=toInteger(this.value)\";";
            $setData["SCHOOLING"] = knjCreateTextBox($objForm, $model->searchField["SCHOOLING".$i], "SCHOOLING".$i, 2, 2, $extraInt.$extraRight);

            //出席回数ラジオボタン 1:回に等しい 2:回以上 3:回以下
            $schoolingSType = array(1, 2, 3);
            $model->searchField["SCHOOLING_S".$i] = ($model->searchField["SCHOOLING_S".$i] == "") ? "1" : $model->searchField["SCHOOLING_S".$i];
            $schoolingSArray = knjCreateRadio($objForm, "SCHOOLING_S".$i, $model->searchField["SCHOOLING_S".$i], "", $schoolingSType, get_count($schoolingSType));
            $schoolingSCnt = 1;
            foreach ($schoolingSArray as $key => $val) {
                $setData["SCHOOLING_S".$schoolingSCnt] = $val;
                $setData["FOR_SCHOOLING".$schoolingSCnt] = "SCHOOLING_S".$i.$schoolingSCnt;
                $schoolingSCnt++;
            }

            //得点
            $setData["SCORE"] = knjCreateTextBox($objForm, $model->searchField["SCORE".$i], "SCORE".$i, 3, 3, $extraInt.$extraRight);

            //得点ラジオボタン 1:点に等しい 2:点以上 3:点以下
            $scoreSType = array(1, 2, 3);
            $model->searchField["SCORE_S".$i] = ($model->searchField["SCORE_S".$i] == "") ? "1" : $model->searchField["SCORE_S".$i];
            $scoreSArray = knjCreateRadio($objForm, "SCORE_S".$i, $model->searchField["SCORE_S".$i], "", $scoreSType, get_count($scoreSType));
            $scoreSCnt = 1;
            foreach ($scoreSArray as $key => $val) {
                $setData["SCORE_S".$scoreSCnt] = $val;
                $setData["FOR_SCORE".$scoreSCnt] = "SCORE_S".$i.$scoreSCnt;
                $scoreSCnt++;
            }

            //評定
            $setData["HYOUTEI"] = knjCreateTextBox($objForm, $model->searchField["HYOUTEI".$i], "HYOUTEI".$i, 3, 3, $extraInt.$extraRight);

            //評定ラジオボタン 1:に等しい 2:以上 3:以下
            $hyouteiSType = array(1, 2, 3);
            $model->searchField["HYOUTEI_S".$i] = ($model->searchField["HYOUTEI_S".$i] == "") ? "1" : $model->searchField["HYOUTEI_S".$i];
            $hyouteiSArray = knjCreateRadio($objForm, "HYOUTEI_S".$i, $model->searchField["HYOUTEI_S".$i], "", $hyouteiSType, get_count($hyouteiSType));
            $hyouteiSCnt = 1;
            foreach ($hyouteiSArray as $key => $val) {
                $setData["HYOUTEI_S".$hyouteiSCnt] = str_replace("name='HYOUTEI_S".$i."'", "name='HYOUTEI_S".$i."'", $val);
                $setData["FOR_HYOUTEI".$hyouteiSCnt] = "HYOUTEI_S".$i.$hyouteiSCnt;
                $hyouteiSCnt++;
            }
            $arg["searchData"][] = $setData;
        }

        //対象者リストを作成する
        $opt1 = array();
        $opt_left = array();
        $selectleft = explode(",", $model->selectleft);

        if (($model->cmd == "knjm550m_1Search" || $model->cmd == "read" || $model->cmd == "meibo") &&
            ($model->searchField["SUBCLASS1"] || $model->searchField["SUBCLASS2"] || $model->searchField["SUBCLASS3"])
        ) {
            $query = knjm550mQuery::getSch($model);
            $result = $db->query($query);

            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $model->select_opt[$row["VALUE"]] = array('label' => $row["LABEL"],
                                                             'value' => $row["VALUE"]);
                if ($model->cmd == 'read' || $model->cmd == "meibo") {
                    if (!in_array($row["VALUE"], $selectleft)) {
                        $opt1[]= array('label' =>  $row["LABEL"],
                                       'value' => $row["VALUE"]);
                    }
                } else {
                    $opt1[]= array('label' =>  $row["LABEL"],
                                   'value' => $row["VALUE"]);
                }
                //左リストで選択されたものを再セット
            }
            if ($model->cmd == 'read' || $model->cmd == "meibo") {
                foreach ($model->select_opt as $key => $val) {
                    if (in_array($key, $selectleft)) {
                        $opt_left[] = $val;
                    }
                }
            }
            $result->free();
        }
        $objForm->ae(array("type"       => "select",
                            "name"       => "category_name",
                            "extrahtml"  => "multiple style=\"width:100%\" width:\"100%\" ondblclick=\"move1('left')\"",
                            "size"       => "20",
                            "options"    => isset($opt1)?$opt1:array()));

        $arg["data"]["CATEGORY_NAME"] = $objForm->ge("category_name");
        $arg["data"]["CATEGORY_CNT"] = get_count($opt1);

        //生徒一覧リストを作成する
        $objForm->ae(array("type"       => "select",
                            "name"       => "category_selected",
                            "extrahtml"  => "multiple style=\"width:100%\" width:\"100%\" ondblclick=\"move1('right')\"",
                            "size"       => "20",
                            "options"    => $opt_left));

        $arg["data"]["CATEGORY_SELECTED"] = $objForm->ge("category_selected");

        //対象選択ボタンを作成する（全部）
        $extra = "style=\"height:20px;width:60px\" onclick=\"moves('right');\"";
        $arg["button"]["btn_rights"] = knjCreateBtn($objForm, "btn_rights", ">>", $extra);
        //対象取消ボタンを作成する（全部）
        $extra = "style=\"height:20px;width:60px\" onclick=\"moves('left');\"";
        $arg["button"]["btn_lefts"] = knjCreateBtn($objForm, "btn_lefts", "<<", $extra);
        //対象選択ボタンを作成する（一部）
        $extra = "style=\"height:20px;width:60px\" onclick=\"move1('right');\"";
        $arg["button"]["btn_right1"] = knjCreateBtn($objForm, "btn_right1", "＞", $extra);
        //対象取消ボタンを作成する（一部）
        $extra = "style=\"height:20px;width:60px\" onclick=\"move1('left');\"";
        $arg["button"]["btn_left1"] = knjCreateBtn($objForm, "btn_left1", "＜", $extra);

        //レポート集計基準日付データ
        if ($model->field["KIJUN"] == "") {
            $model->field["KIJUN"] = str_replace("-", "/", CTRL_DATE);
        }
        $arg["data"]["KIJUN"] = View::popUpCalendar($objForm, "KIJUN", str_replace("-", "/", $model->field["KIJUN"]));

        //コメントデータ取得
        if ($model->cmd != 'clschange') {
            $query = knjm550mQuery::getComment();

            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $model->field["COMMENT".$row["REMARKID"]] = $row["REMARK"];
            }
            $result->free();
        }

        //コメントデータ
        $arg["data"]["COMMENT1"] = knjCreateTextBox($objForm, $model->field["COMMENT1"], "COMMENT1", 70, 50, "");
        $arg["data"]["COMMENT2"] = knjCreateTextBox($objForm, $model->field["COMMENT2"], "COMMENT2", 70, 50, "");
        $arg["data"]["COMMENT3"] = knjCreateTextBox($objForm, $model->field["COMMENT3"], "COMMENT3", 70, 50, "");
        $arg["data"]["COMMENT4"] = knjCreateTextBox($objForm, $model->field["COMMENT4"], "COMMENT4", 70, 50, "");
        $arg["data"]["COMMENT5"] = knjCreateTextBox($objForm, $model->field["COMMENT5"], "COMMENT5", 70, 50, "");
        $arg["data"]["COMMENT6"] = knjCreateTextBox($objForm, $model->field["COMMENT6"], "COMMENT6", 70, 50, "");

        //パネルのデフォルト
        $arg["panel"]["ACCINDEX"] = $model->accIndex != null ? $model->accIndex : 0;

        //ボタンを作成する
        makeBtn($objForm, $arg);

        //hiddenを作成する(必須)
        makeHidden($objForm, $arg, $model);

        if (!isset($model->warning) && ($model->cmd == 'read' || $model->cmd == 'meibo')) {
            $setCmd = $model->cmd == 'read' ? "KNJM500M" : "KNJM550M";
            $model->cmd = 'knjm550m_1';
            $arg["printgo"] = "newwin('" . SERVLET_URL . "', '".$setCmd."')";
        }

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjm550m_1Form1.html", $arg);
    }
}

//コンボ作成
function makeCombo(&$objForm, &$arg, $db, $query, &$value, $name, $extra, $size, $blank = "")
{
    $opt = array();
    if ($blank == "BLANK") {
        $opt[] = array("label" => "",
                        "value" => "");
    }
    $result = $db->query($query);

    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array("label" => $row["LABEL"],
                        "value" => $row["VALUE"]);
    }
    $result->free();

    $value = ($value) ? $value : $opt[0]["value"];
    return knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}

//ボタン作成
function makeBtn(&$objForm, &$arg)
{
    //CVSボタン
    $extra = "onClick=\"btn_submit('csv');\"";
    $arg["button"]["btn_csv"] = knjCreateBtn($objForm, "btn_csv", "CSV出力", $extra);
    //名簿印刷ボタン
    $extra = "onClick=\"btn_submit('meibo');\"";
    $arg["button"]["btn_meibo"] = knjCreateBtn($objForm, "btn_meibo", "名簿印刷", $extra);
    //検索ボタン
    $extra = "onClick=\"btn_submit('search');\"";
    $arg["button"]["btn_search"] = knjCreateBtn($objForm, "btn_search", "検 索", $extra);
    //印刷ボタン
    $extra = "onClick=\"btn_submit('update');\"";
    $arg["button"]["btn_print"] = knjCreateBtn($objForm, "btn_print", "プレビュー／印刷", $extra);
    //終了
    $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", "onclick=\"closeWin();\"");
}

function makeHidden(&$objForm, &$arg, $model)
{
    $arg["TOP"]["YEAR"] = knjCreateHidden($objForm, "YEAR", CTRL_YEAR);
    $arg["TOP"]["GAKKI"] = knjCreateHidden($objForm, "GAKKI", CTRL_SEMESTER);
    $arg["TOP"]["CTRL_DATE"] = knjCreateHidden($objForm, "CTRL_DATE", CTRL_DATE);
    $arg["TOP"]["DBNAME"] = knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
    $arg["TOP"]["PRGID"] = knjCreateHidden($objForm, "PRGID", "KNJM500");
    $arg["TOP"]["PANEL_CNT"] = knjCreateHidden($objForm, "PANEL_CNT", $model->panelCnt);
    $arg["TOP"]["useCurriculumcd"] = knjCreateHidden($objForm, "useCurriculumcd", $model->Properties["useCurriculumcd"]);
    knjCreateHidden($objForm, "cmd");
    knjCreateHidden($objForm, "selectleft");
    knjCreateHidden($objForm, "ACCINDEX");
}
