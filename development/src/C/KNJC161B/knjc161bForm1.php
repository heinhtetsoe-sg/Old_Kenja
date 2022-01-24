<?php

require_once('for_php7.php');

class knjc161bForm1
{
    public function main(&$model)
    {
        $objForm        = new form();
        $db = Query::dbCheckOut();
        $arg["start"]   = $objForm->get_start("main", "POST", "knjc161bindex.php", "", "main");
        $arg["jscript"] = "";
        $arg["Closing"] = "";
        
        //権限チェック
        if (AUTHORITY != DEF_UPDATABLE) {
            $arg["jscript"] = "OnAuthError();";
        }
        //事前処理チェック
        if (!knjc161bQuery::checkToStart($db)) {
            $arg["Closing"] = " closing_window(2);";
        }

        //処理年度
        $model->year = CTRL_YEAR;
        $arg["data"]["YEAR"] = $model->year;

        //学期
        $arg["data"]["SEMESTER"] = $model->ctrl["学期名"][CTRL_SEMESTER];

        //ラジオボタン
        $radioValue = array(1, 2);
        if (!$model->dispType) {
            $model->dispType = 1;
        }
        $click = "onclick =\" return btn_submit('main');\"";
        $extra = array("id=\"OUTPUT1\"".$click, "id=\"OUTPUT2\"".$click);
        $radioArray = knjCreateRadio($objForm, "selDispType", $model->dispType, $extra, $radioValue, get_count($radioValue));
        foreach ($radioArray as $key => $val) {
            $arg["data"][$key] = $val;
        }

        //処理月(各学期の期間の月のみをコンボにセット
        $query = knjc161bQuery::getSemesterMonth($model->year);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            for ($i = 4; $i < 16; $i++) {
                $mon = ($i<13) ? $i : ($i-12);
                
                if ($mon < 4) {
                    $year = $model->year + 1;
                } else {
                    $year = $model->year;
                }

                //年と月を合わせて比較する
                if ((int)($year.sprintf("%02d", $mon)) >= (int)strftime("%Y%m", strtotime($row["SDATE"]))
                && ((int)$year.sprintf("%02d", $mon)) <= (int)strftime("%Y%m", strtotime($row["EDATE"]))) {
                    //月が学期の開始月または終了月かチェック
                    //開始月の場合は開始日以降その月末日まで集計
                    //開始月の場合は開始日以降翌月の１日まで集計
                    if ($mon == (int)strftime("%m", strtotime($row["SDATE"]))) {
                        $flg = "1";
                    
                    //終了月の場合はその月の１日から終了日まで集計
                    //終了月の場合はその月の２日から終了日まで集計
                    } elseif ($mon == (int)strftime("%m", strtotime($row["EDATE"]))) {
                        $flg = "2";
                    
                    //それ以外はその月の１日から月末日まで集計
                    //それ以外はその月の２日から翌月の１日まで集計
                    } else {
                        $flg = "0";
                    }
                    
                    //初期値(学籍処理日の月にする）
                    if ($model->month == "") {
                        if ($mon == strftime("%m", strtotime(CTRL_DATE))) {
                            $model->month = $row["SEMESTER"]."-".sprintf("%02d", $mon)."-".$flg;
                        }
                    }
                        
                    $opt[] = array("label"    =>$mon."月 ( ".$row["SEMESTERNAME"]." )",
                                   "value"    => $row["SEMESTER"]."-".sprintf("%02d", $mon)."-".$flg);
                }
            }
        }
        $arg["data"]["MONTH"] = knjCreateCombo($objForm, "month", $model->month, $opt, "STYLE=\"WIDTH:120\"", "1");

        if ($model->dispType == "1") {
            $arg["dispType1"] = "1";
        } else {
            $arg["dispType2"] = "1";
        }
        $query = knjc161bQuery::getSelectClass($model);
        if ($model->dispType == "1") {
            $extra = "";
        } else {
            $extra = "onchange=\"btn_submit('main')\"";
        }
        makeCmb($objForm, $arg, $db, $query, "GRADE_HR_CLASS", $model->selGrHrCls, $extra, 1);

        //リストToリスト作成
        if ($model->dispType == "2") {
            makeListToList($objForm, $arg, $db, $model);
        }

        Query::dbCheckIn($db);

        //ボタン
        $extra = "onclick=\"return newwin('" . SERVLET_URL . "');\"";
        $arg["button"]["btn_print"] = knjCreateBtn($objForm, "btn_print", "プレビュー／印刷", $extra);
        $extra = "onclick=\"closeWin();\"";
        $arg["button"]["btn_cancel"] = knjCreateBtn($objForm, "btn_cancel", "終 了", $extra);

        //HIDDEN
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "cmd2");
        knjCreateHidden($objForm, "useCurriculumcd", $model->Properties["useCurriculumcd"]);
        knjCreateHidden($objForm, "useVirus", $model->Properties["useVirus"]);
        knjCreateHidden($objForm, "useKekkaJisu", $model->Properties["useKekkaJisu"]);
        knjCreateHidden($objForm, "useKekka", $model->Properties["useKekka"]);
        knjCreateHidden($objForm, "useLatedetail", $model->Properties["useLatedetail"]);
        knjCreateHidden($objForm, "useKoudome", $model->Properties["useKoudome"]);
        knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
        knjCreateHidden($objForm, "PRGID", "KNJC161B");
        knjCreateHidden($objForm, "YEAR", CTRL_YEAR);
        knjCreateHidden($objForm, "DOCUMENTROOT", DOCUMENTROOT);

        $arg["finish"]  = $objForm->get_finish();

        View::toHTML($model, "knjc161bForm1.html", $arg);
    }
}

function makeListToList(&$objForm, &$arg, $db, $model)
{

    //表示切替
    $arg["data"]["TITLE_LEFT"]  = "出力対象一覧";
    $arg["data"]["TITLE_RIGHT"] = "生徒一覧";

    //対象外の生徒取得
    $opt_idou = array();
    if (strlen($model->month) > 4) {  //指定月の文字列が取得できる場合しかSQL実行しない
        $query = knjc161bQuery::getSchnoIdou($model);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt_idou[] = $row["SCHREGNO"];
        }
        $result->free();
    }

    $opt_left = $opt_right = array();
    $query = knjc161bQuery::getStudent($model, $seme);
    $result = $db->query($query);
    $selectdata = ($model->selectdata) ? explode(',', $model->selectdata) : array();
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $idou = (in_array($row["SCHREGNO"], $opt_idou)) ? "●" : "　";
        if (in_array($row["SCHREGNO"], $selectdata)) {
            $opt_left[] = array('label' => $row["SCHREGNO_SHOW"].$idou.$row["ATTENDNO"]."番".$idou.$row["NAME_SHOW"],
                                'value' => $row["SCHREGNO"]);
        } else {
            $opt_right[] = array('label' => $row["SCHREGNO_SHOW"].$idou.$row["ATTENDNO"]."番".$idou.$row["NAME_SHOW"],
                                 'value' => $row["SCHREGNO"]);
        }
    }
    $result->free();

    //一覧リスト（右）
    $extra = "multiple style=\"width:230px\" width:\"230px\" ondblclick=\"move1('left')\"";
    $arg["data"]["CATEGORY_NAME"] = knjCreateCombo($objForm, "CATEGORY_NAME", "", $opt_right, $extra, 20);
        
    //出力対象一覧リスト（左）
    $extra = "multiple style=\"width:230px\" width:\"230px\" ondblclick=\"move1('right')\"";
    $arg["data"]["CATEGORY_SELECTED"] = knjCreateCombo($objForm, "CATEGORY_SELECTED", "", $opt_left, $extra, 20);

    //対象選択ボタンを作成する
    $extra = "style=\"height:20px;width:40px\" onclick=\"moves('right');\"";
    $arg["button"]["btn_rights"] = knjCreateBtn($objForm, "btn_rights", ">>", $extra);
    //対象取消ボタンを作成する
    $extra = "style=\"height:20px;width:40px\" onclick=\"moves('left');\"";
    $arg["button"]["btn_lefts"] = knjCreateBtn($objForm, "btn_lefts", "<<", $extra);
    //対象選択ボタンを作成する
    $extra = "style=\"height:20px;width:40px\" onclick=\"move1('right');\"";
    $arg["button"]["btn_right1"] = knjCreateBtn($objForm, "btn_right1", "＞", $extra);
    //対象取消ボタンを作成する
    $extra = "style=\"height:20px;width:40px\" onclick=\"move1('left');\"";
    $arg["button"]["btn_left1"] = knjCreateBtn($objForm, "btn_left1", "＜", $extra);
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $blank = "")
{
    $opt = array();
    if ($blank == "BLANK") {
        $opt[] = array("label" => "", "value" => "");
    }
    $value_flg = false;
    $i = $default = 0;
    $default_flg = true;

    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);

        if ($value == $row["VALUE"]) {
            $value_flg = true;
        }

        if ($row["NAMESPARE2"] && $default_flg) {
            $default = $i;
            $default_flg = false;
        } else {
            $i++;
        }
    }

    $result->free();
    $value = ($value && $value_flg) ? $value : $opt[$default]["value"];

    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}
