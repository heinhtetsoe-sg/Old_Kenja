<?php

require_once('for_php7.php');

/****************************/
/* 住所タックシール KNJA190 */
/****************************/

class knjm550_3Form1
{
    public function main(&$model)
    {
        $objForm = new form();
        //フォーム作成
        $arg["start"]   = $objForm->get_start("knjm550_3Form1", "POST", "knjm550index.php", "", "knjm550_3Form1");

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["data"]["YEAR"] = CTRL_YEAR;

        //学期
        $arg["data"]["GAKKI"] = CTRL_SEMESTER;

        //出力データラジオボタン 1:学習状況通知票 2:宛名封筒 3:生徒住所タックシール
        $opt_data = array(1, 2, 3);
        $model->type_div = ($model->type_div == "") ? "1" : $model->type_div;
        $extra = array(" onClick=\"btn_submit('knjm550_1')\"", " onClick=\"btn_submit('knjm550_2')\"", " onClick=\"btn_submit('knjm550_3')\"");
        $radioArray = knjCreateRadio($objForm, "TYPE_DIV", $model->type_div, $extra, $opt_data, get_count($opt_data));
        foreach ($radioArray as $key => $val) {
            $arg["data"][$key] = $val;
        }

        //抽出条件ラジオボタン 1:AND検索 2:OR検索
        $opt_mSearc = array(1, 2);
        $model->mainSearch = ($model->mainSearch == "") ? "1" : $model->mainSearch;
        $extra = array(" onClick=\"btn_submit('knjm550_1')\"", " onClick=\"btn_submit('knjm550_1')\"");
        $radioArray = knjCreateRadio($objForm, "MAIN_SEARCH", $model->mainSearch, $extra, $opt_mSearc, get_count($opt_mSearc));
        foreach ($radioArray as $key => $val) {
            $arg["data"][$key] = $val;
        }

        //パネル作成
        for ($i = 1; $i <= $model->panelCnt; $i++) {
            //タイトル
            $setData["JOUKEN"] = "条件".$i;

            //受講科目コンボ3
            $query = knjm550Query::getChairStd($model);
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
            $query = knjm550Query::getKaisu();
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

        //出力データラジオボタン
        $opt_data = array(1, 2, 3);
        $model->type_div = ($model->type_div == "") ? "1" : $model->type_div;
        $extra = array("id=\"TYPE_DIV1\" onClick=\"btn_submit('knjm550_1')\"", "id=\"TYPE_DIV2\" onClick=\"btn_submit('knjm550_2')\"", "id=\"TYPE_DIV3\" onClick=\"btn_submit('knjm550_3')\"");
        $radioArray = knjCreateRadio($objForm, "TYPE_DIV", $model->type_div, $extra, $opt_data, get_count($opt_data));
        foreach ($radioArray as $key => $val) {
            $arg["data"][$key] = $val;
        }

        //生徒選択コンボボックスを作成する
        $opt1 = array();
        $opt_left = array();
        $selectleft = explode(",", $model->selectleft);

        if ($model->cmd == "knjm550_3Search" &&
            ($model->searchField["SUBCLASS1"] || $model->searchField["SUBCLASS2"] || $model->searchField["SUBCLASS3"])
        ) {
            $query = knjm550Query::getSch($model);

            $result = $db->query($query);

            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $model->select_opt[$row["VALUE"]] = array('label' => $row["LABEL"],
                                                          'value' => $row["VALUE"]);
                if ($model->cmd == 'read') {
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
            if ($model->cmd == 'read') {
                foreach ($model->select_opt as $key => $val) {
                    if (in_array($key, $selectleft)) {
                        $opt_left[] = $val;
                    }
                }
            }
            $result->free();
        }

        //対象者リストを作成する
        $objForm->ae(array("type"       => "select",
                            "name"       => "category_name",
                            "extrahtml"  => "multiple style=\"width:100%\" ondblclick=\"move('right')\"",
                            "size"       => "20",
                            "options"    => $opt_left));

        $arg["data"]["CATEGORY_NAME"] = $objForm->ge("category_name");
        $arg["data"]["CATEGORY_CNT"] = get_count($opt1);

        //生徒一覧リストを作成する
        $objForm->ae(array("type"       => "select",
                            "name"       => "category_selected",
                            "extrahtml"  => "multiple style=\"width:100%\" ondblclick=\"move('left')\"",
                            "size"       => "20",
                            "options"    => $opt1));

        $arg["data"]["CATEGORY_SELECTED"] = $objForm->ge("category_selected");

        //対象取り消しボタンを作成する(個別)
        $extra = "style=\"height:20px;width:60px\" onclick=\"move('right');\"";
        $arg["button"]["btn_right1"] = knjCreateBtn($objForm, "btn_right1", "＞", $extra);
        //対象取り消しボタンを作成する(全て)
        $extra = "style=\"height:20px;width:60px\" onclick=\"move('rightall');\"";
        $arg["button"]["btn_right2"] = knjCreateBtn($objForm, "btn_right2", "≫", $extra);
        //対象選択ボタンを作成する(個別)
        $extra = "style=\"height:20px;width:60px\" onclick=\"move('left');\"";
        $arg["button"]["btn_left1"] = knjCreateBtn($objForm, "btn_left1", "＜", $extra);
        //対象選択ボタンを作成する(全て)
        $extra = "style=\"height:20px;width:60px\" onclick=\"move('leftall');\"";
        $arg["button"]["btn_left2"] = knjCreateBtn($objForm, "btn_left2", "≪", $extra);

        //印刷対象ラジオボタンを作成する////////////////////////////////////////////////////////////////////////////////////
        $opt2[0]=1;
        $opt2[1]=2;

        if (!$model->field["OUTPUT"]) {
            $model->field["OUTPUT"] = 1;
        }

        $objForm->ae(array("type"       => "radio",
                            "name"       => "OUTPUT",
                            "value"      => $model->field["OUTPUT"],
                            "extrahtml"  => "onclick=\" dischange();\"",    //NO001
                            "multiple"   => $opt2));

        $arg["data"]["OUTPUT1"] = $objForm->ge("OUTPUT", 1);
        $arg["data"]["OUTPUT2"] = $objForm->ge("OUTPUT", 2);

        //印刷対象ラジオボタンを作成する NO001
        $opt3[0]=1;
        $opt3[1]=2;

        if (!$model->field["OUTPUT2"]) {
            $model->field["OUTPUT2"] = 1;
        }

        $objForm->ae(array("type"       => "radio",
                            "name"       => "OUTPUT2",
                            "value"      => $model->field["OUTPUT2"],
                            "extra"      => "onclick=\" dischange();\"",
                            "multiple"   => $opt3));

        $arg["data"]["OUTPUT3"] = $objForm->ge("OUTPUT2", 1);
        $arg["data"]["OUTPUT4"] = $objForm->ge("OUTPUT2", 2);


        //生徒名チェックボックスを作成する NO001
        if ($model->cmd == "" || $model->field["OUTPUT"] == 1) {
            $abled = "" ;
        } else {
            $abled = "disabled" ;
        }

        if ($model->field["CHECK1"] == "on") {
            $check = "checked"  ;
        } else {
            $check = ""  ;
        }

        $objForm->ae(array("type"       => "checkbox",
                            "name"       => "CHECK1",
                            "value"      => "on",
                            "extrahtml"  => "$abled"." $check" ));

        $arg["data"]["CHECK1"] = $objForm->ge("CHECK1");

        //学籍番号チェックボックスを作成する NO001

        if ($model->field["OUTPUT"] == 2) {
            $abled = "" ;
        } else {
            $abled = "disabled" ;
        }

        if ($model->field["CHECK2"] == "on") {
            $check = "checked"  ;
        } else {
            $check = ""  ;
        }

        $objForm->ae(array("type"       => "checkbox",
                            "name"       => "CHECK2",
                            "value"      => "on",
                            "extrahtml"  => "$abled"." $check" ));

        $arg["data"]["CHECK2"] = $objForm->ge("CHECK2");

        //開始位置（行）コンボボックスを作成する///////////////////////////////////////////////////////////////////////
        $row = array(array('label' => "１行",'value' => 1),
                    array('label' => "２行",'value' => 2),
                    array('label' => "３行",'value' => 3),
                    array('label' => "４行",'value' => 4),
                    array('label' => "５行",'value' => 5),
                    array('label' => "６行",'value' => 6),
                    );

        $objForm->ae(array("type"       => "select",
                            "name"       => "POROW",
                            "size"       => "1",
                            "value"      => $model->field["POROW"],
                            "options"    => isset($row)?$row:array()));

        $arg["data"]["POROW"] = $objForm->ge("POROW");


        //開始位置（列）コンボボックスを作成する
        $col = array(array('label' => "１列",'value' => 1),
                    array('label' => "２列",'value' => 2),
                    array('label' => "３列",'value' => 3),
                    );


        $objForm->ae(array("type"       => "select",
                            "name"       => "POCOL",
                            "size"       => "1",
                            "value"      => $model->field["POCOL"],
                            "options"    => isset($col)?$col:array()));

        $arg["data"]["POCOL"] = $objForm->ge("POCOL");

        //パネルのデフォルト
        $arg["panel"]["ACCINDEX"] = $model->accIndex != null ? $model->accIndex : 0;

        //ボタンを作成する
        makeBtn($objForm, $arg);

        //hiddenを作成する
        makeHidden($objForm, $model);

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();
        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjm550_3Form1.html", $arg);
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
    $extra = "onclick=\"return newwin('" . SERVLET_URL . "', 'KNJM550');\"";
    $arg["button"]["btn_meibo"] = knjCreateBtn($objForm, "btn_meibo", "名簿印刷", $extra);
    //検索ボタン
    $extra = "onClick=\"btn_submit('search');\"";
    $arg["button"]["btn_search"] = knjCreateBtn($objForm, "btn_search", "検 索", $extra);
    //印刷ボタン
    $extra = "onclick=\"return newwin('" . SERVLET_URL . "', 'KNJA190');\"";
    $arg["button"]["btn_print"] = knjCreateBtn($objForm, "btn_print", "プレビュー／印刷", $extra);
    //終了
    $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", "onclick=\"closeWin();\"");
}

function makeHidden(&$objForm, $model)
{
    knjCreateHidden($objForm, "YEAR", CTRL_YEAR);
    knjCreateHidden($objForm, "GAKKI", CTRL_SEMESTER);
    knjCreateHidden($objForm, "CTRL_DATE", CTRL_DATE);
    knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
    knjCreateHidden($objForm, "PRGID", "KNJA190");
    knjCreateHidden($objForm, "cmd");
    knjCreateHidden($objForm, "ACCINDEX");
    knjCreateHidden($objForm, "selectleft");
    knjCreateHidden($objForm, "PANEL_CNT", $model->panelCnt);
    knjCreateHidden($objForm, "useCurriculumcd", $model->Properties["useCurriculumcd"]);
}
