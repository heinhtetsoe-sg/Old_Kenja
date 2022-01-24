<?php

require_once('for_php7.php');
//ファイルアップロードオブジェクト
require_once("csvfile.php");

class knjl017dForm1
{
    public function main(&$model)
    {
        //オブジェクト作成
        $objForm = new form();

        //フォーム作成
        $arg["start"] = $objForm->get_start("main", "POST", "knjl017dindex.php", "", "main");

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["TOP"]["YEAR"] = $model->ObjYear;

        //受験種類コンボボックス
        $extra = "onchange=\"return btn_submit('main');\" tabindex=-1";
        $query = knjl017dQuery::getNameMst("L004", $model->ObjYear);
        makeCmb($objForm, $arg, $db, $query, "TESTDIV", $model->testdiv, $extra, 1);

        //クラブ情報コンボボックス
        $query = knjl017dQuery::getCmbClubList($model);
        $default = 0;
        $opt = getClubCmbList($db, $query, $default, "BLANK");

        //テキスト名
        $text_name = array("1" => "JUDGE_KIND"
                          ,"2" => "SUB_ORDER");
        $setTextField = "";
        $textSep = "";
        foreach ($text_name as $code => $col) {
            $setTextField .= $textSep.$col."-";
            $textSep = ",";
        }
        //一覧表示
        $arr_receptno = array();
        if ($model->applicantdiv != "" && $model->testdiv != "") {
            //データ取得
            $searchCnt = $db->getOne(knjl017dQuery::SelectQuery($model, "COUNT"));
            if ($searchCnt == 0) {
                $model->setMessage("MSG303");
            } else {
                $result = $db->query(knjl017dQuery::SelectQuery($model, $model->sorttype));
                $count = 0;
                while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                    array_walk($row, "htmlspecialchars_array");

                    //HIDDENに保持する用
                    $arr_receptno[] = $row["EXAMNO"].'-'.$count;
                    //エラー時は画面の値をセット
                    if (isset($model->warning)) {
                        $row["JUDGE_KIND"] = $model->judgekind[$count];
                        $row["CLUBCD"] = $model->clubcd[$count];
                        $row["SUB_ORDER"] = $model->suborder[$count];
                    }
                    //入力項目
                    $disablechkval = $row["JUDGE_KIND"];
                    $extra = " onblur=\"return checkJudgeKind(this);\" onPaste=\"return showPaste(this);\" onKeyDown=\"keyChangeEntToTab2(this, '{$setTextField}', {$count});\"";
                    $row["JUDGE_KIND"] = knjCreateTextBox($objForm, $row["JUDGE_KIND"], "JUDGE_KIND-".$count, 8, 30, $extra);
                    if ($disablechkval === "1" || $disablechkval === "2" || $disablechkval === "3") {
                        $extra = "";
                        $row["CLUBCD"] = makeTableCmb($objForm, $arg, $opt, $default, "CLUBCD-".$count, $row["CLUBCD"], $extra, 1);
                    } else {
                        $extra = " disabled=\"disabled\"";
                        $ignoreval = "";
                        $row["CLUBCD"] = makeTableCmb($objForm, $arg, $opt, $default, "CLUBCD-".$count, $ignoreval, $extra, 1);
                    }
                    $extra = " onPaste=\"return showPaste(this);\" onKeyDown=\"keyChangeEntToTab2(this, '{$setTextField}', {$count});\"";
                    $row["SUB_ORDER"] = knjCreateTextBox($objForm, $row["SUB_ORDER"], "SUB_ORDER-".$count, 8, 30, $extra);

                    $arg["data"][] = $row;
                    $count++;
                }
            }
        }

        $arg["BOTTOM"]["JUDGE_TOTAL"] = $model->getDecisionInfoStr($db);

        knjCreateHidden($objForm, "COUNT", $count);

        //ボタン作成
        makeBtn($objForm, $arg, $model);

        //hidden作成
        makeHidden($objForm, $model, $arr_receptno);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjl017dForm1.html", $arg);
    }
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

    $arg["TOP"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}

function getClubCmbList($db, $query, &$default, $blank = "")
{
    $opt = array();
    if ($blank == "BLANK") {
        $opt[] = array("label" => "", "value" => "");
    }
    $i = $default = 0;
    $default_flg = true;

    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);

        if ($default_flg) {
            $default = $i;
            $default_flg = false;
        } else {
            $i++;
        }
    }

    $result->free();
    return $opt;
}

//表内コンボ作成
function makeTableCmb(&$objForm, &$arg, $opt, $default, $name, &$value, $extra, $size)
{
    $value_flg = false;
    foreach ($opt as $row) {
        if ($value == $row["value"]) {
            $value_flg = true;
        }
    }
    $value = (!is_null($value) && $value_flg) ? $value : $opt[$default]["value"];
    return knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}

//ボタン作成
function makeBtn(&$objForm, &$arg, $model)
{
    //ソートボタン(タイトル文字列)
    $arg["TOP"]["J_LANG_STR"] = View::alink("#", "<font color=\"white\"><span style=\"text-decoration: underline;\">" . judgeOrderStr('sort_j_lang', $model) . "国語</span></font>", "onclick=\"return sortConfirm('sort_j_lang');\"");
    $arg["TOP"]["MATH_STR"] = View::alink("#", "<font color=\"white\"><span style=\"text-decoration: underline;\">" . judgeOrderStr('sort_math', $model) . "数学</span></font>", "onclick=\"return sortConfirm('sort_math');\"");
    $arg["TOP"]["E_LANG_STR"] = View::alink("#", "<font color=\"white\"><span style=\"text-decoration: underline;\">" . judgeOrderStr('sort_e_lang', $model) . "英語</span></font>", "onclick=\"return sortConfirm('sort_e_lang');\"");
    $arg["TOP"]["TOTAL_STR"] = View::alink("#", "<font color=\"white\"><span style=\"text-decoration: underline;\">" . judgeOrderStr('sort_total', $model) . "合計</span></font>", "onclick=\"return sortConfirm('sort_total');\"");
    //更新ボタン
    $extra = "onclick=\"return btn_submit('update');\"";
    $arg["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);
    //取消ボタン
    $extra = "onclick=\"return btn_submit('reset');\"";
    $arg["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);
    //終了ボタン
    $extra = "onclick=\"closeWin();\"";
    $arg["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);
    //プレビュー印刷ボタン
    $extra = "onclick=\"return newwin('" . SERVLET_URL . "');\"";
    $arg["btn_print"] = knjCreateBtn($objForm, "btn_print", "プレビュー／印刷", $extra);
    //CSV出力ボタン
    $extra = "onclick=\"btn_submit('csv');\"";
    $arg["btn_csv"] = knjCreateBtn($objForm, "btn_csv", "CSV出力", $extra);
}

function judgeOrderStr($typestr, $model)
{
    $retstr = "";
    if ($typestr === "sort_j_lang") {
        if ($model->sorttype === "J_LANG_ASC") {
            $retstr = "▲";
        } elseif ($model->sorttype === "J_LANG_DESC") {
            $retstr = "▼";
        }
    } elseif ($typestr === "sort_math") {
        if ($model->sorttype === "MATH_ASC") {
            $retstr = "▲";
        } elseif ($model->sorttype === "MATH_DESC") {
            $retstr = "▼";
        }
    } elseif ($typestr === "sort_e_lang") {
        if ($model->sorttype === "E_LANG_ASC") {
            $retstr = "▲";
        } elseif ($model->sorttype === "E_LANG_DESC") {
            $retstr = "▼";
        }
    } elseif ($typestr === "sort_total") {
        if ($model->sorttype === "TOTAL_ASC") {
            $retstr = "▲";
        } elseif ($model->sorttype === "TOTAL_DESC") {
            $retstr = "▼";
        }
    }
    return $retstr;
}

//hidden作成
function makeHidden(&$objForm, $model, $arr_receptno)
{
    knjCreateHidden($objForm, "cmd");
    knjCreateHidden($objForm, "HID_RECEPTNO", implode(",", $arr_receptno));
//    knjCreateHidden($objForm, "HID_TESTDIV");
    knjCreateHidden($objForm, "SCHOOLCD", $model->schoolcd);
    knjCreateHidden($objForm, "SCHOOLKIND", $model->schoolkind);

    knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
    knjCreateHidden($objForm, "PRGID", "KNJL017D");

    knjCreateHidden($objForm, "ENTEXAMYEAR", $model->ObjYear);
    knjCreateHidden($objForm, "APPLICANTDIV", $model->applicantdiv);
    knjCreateHidden($objForm, "LOGIN_YEAR", CTRL_YEAR);
    knjCreateHidden($objForm, "LOGIN_SEMESTER", CTRL_SEMESTER);
    knjCreateHidden($objForm, "LOGIN_DATE", CTRL_DATE);

    knjCreateHidden($objForm, "APP_HOLD", $model->applicantdiv);
    knjCreateHidden($objForm, "SORT_TYPE", $model->sorttype);
}
