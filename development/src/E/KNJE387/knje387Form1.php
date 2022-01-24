<?php

require_once('for_php7.php');
class knje387Form1
{
    function main(&$model) {
        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("main", "POST", "knje387index.php", "", "main");

        $arg["IFRAME"] = View::setIframeJs();

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["TOP"]["YEAR"] = $model->ObjYear;

        //学年コンボボックス
        $extra = "onchange=\"return btn_submit('main');\" tabindex=-1";
        $query = knje387Query::getGrade($model);
        makeCmb($objForm, $arg, $db, $query, "GRADE", $model->grade, $extra, 1, "BLANK");

        //テキスト名
        $text_name = array("1" => "INPUT_ADJUST");
        $setTextField = "";
        $textSep = "";
        foreach ($text_name as $code => $col) {
            $setTextField .= $textSep.$col."-";
            $textSep = ",";
        }

        $model->sorttype = $model->sorttype == "" ? TOTAL_DESC : $model->sorttype;

        //一覧表示
        $arr_schregno = array();
        if ($model->grade != "") {
            //データ取得
            $searchCnt = $db->getOne(knje387Query::SelectQuery($model, "COUNT"));
            if ($searchCnt == 0) {
                $model->setMessage("MSG303");
            } else {
                $result = $db->query(knje387Query::SelectQuery($model, $model->sorttype));
                $count = 0;
                while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                    array_walk($row, "htmlspecialchars_array");

                    //合計
                    $row["SCORE_TOTAL"] = "<font color=\"black\" id=\"SCORE_TOTAL-".$count."\">".$row["SCORE_TOTAL"]."</font>";
                    //(隠し)入力箇所以外の合計
                    knjCreateHidden($objForm, "SCORE_SUBTOTAL-".$count, $row["SCORE_SUBTOTAL"]);
                    $row["HRCLASS_ATTEND"] = $row["HR_NAME"] . "-" . substr($row["ATTENDNO"], 1) . "番";
                    //HIDDENに保持する用
                    $arr_schregno[] = $row["SCHREGNO"].'-'.$count;
                    //エラー時は画面の値をセット
                    if (isset($model->warning)) {
                        $row["INPUT_ADJUST"] = $model->inputadjust[$count];
                    }
                    //入力項目
                    //$disablechkval = $row["INPUT_ADJUST"];
                    $extra = " onblur=\"return calcTotal(this, ".$count.", true);\" onPaste=\"return showPaste(this);\" onKeyDown=\"keyChangeEntToTab2(this, '{$setTextField}', {$count});\"";
                    $row["INPUT_ADJUST"] = knjCreateTextBox($objForm, $row["INPUT_ADJUST"], "INPUT_ADJUST-".$count, 4, 4, $extra);

                    $arg["data"][] = $row;
                    $count++;
                }
            }
        }

        knjCreateHidden($objForm, "COUNT", $count);

        //ボタン作成
        makeBtn($objForm, $arg, $model);

        //hidden作成
        makeHidden($objForm, $model, $arr_schregno);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knje387Form1.html", $arg);
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

        if ($value == $row["VALUE"]) $value_flg = true;

        if ($row["NAMESPARE2"] && $default_flg){
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

        if ($default_flg){
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
    foreach($opt as $row) {
        if ($value == $row["value"]) $value_flg = true;
    }
    $value = (!is_null($value) && $value_flg) ? $value : $opt[$default]["value"];
    return knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}

//ボタン作成
function makeBtn(&$objForm, &$arg, $model) {
    //ソートボタン(タイトル文字列)
    $arg["TOP"]["HRCLASS_ATTENDNO"] = View::alink("#", "<font color=\"white\"><span style=\"text-decoration: underline;\">" . judgeOrderStr('sort_class', $model) . "年組-番</span></font>", "onclick=\"return sortConfirm('sort_class');\"");
    $arg["TOP"]["TOTAL_STR"] = View::alink("#", "<font color=\"white\"><span style=\"text-decoration: underline;\">" . judgeOrderStr('sort_total', $model) . "総学力点</span></font>", "onclick=\"return sortConfirm('sort_total');\"");
    $arg["TOP"]["SCORE_TITLE1"] = View::alink("#", "<font color=\"white\"><span style=\"text-decoration: underline;\">" . judgeOrderStr('score1_sort', $model) . "教科点計</span></font>", "onclick=\"return sortConfirm('score1_sort');\"");
    $arg["TOP"]["SCORE_TITLE2"] = View::alink("#", "<font color=\"white\"><span style=\"text-decoration: underline;\">" . judgeOrderStr('score2_sort', $model) . "学力点計</span></font>", "onclick=\"return sortConfirm('score2_sort');\"");
    $arg["TOP"]["SCORE_TITLE3"] = View::alink("#", "<font color=\"white\"><span style=\"text-decoration: underline;\">" . judgeOrderStr('score3_sort', $model) . "TOEFL-ITP計</span></font>", "onclick=\"return sortConfirm('score3_sort');\"");
    $arg["TOP"]["SCORE_TITLE4"] = View::alink("#", "<font color=\"white\"><span style=\"text-decoration: underline;\">" . judgeOrderStr('score4_sort', $model) . "資格点計</span></font>", "onclick=\"return sortConfirm('score4_sort');\"");
    $arg["TOP"]["SCORE_TITLE5"] = View::alink("#", "<font color=\"white\"><span style=\"text-decoration: underline;\">" . judgeOrderStr('score5_sort', $model) . "加減点計</span></font>", "onclick=\"return sortConfirm('score5_sort');\"");
    //更新ボタン
    $extra = "onclick=\"return btn_submit('update');\"";
    $arg["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);
    //取消ボタン
    $extra = "onclick=\"return btn_submit('reset');\"";
    $arg["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);
    //終了ボタン
    $extra = "onclick=\"closeWin();\"";
    $arg["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);
    //総学力点算出ボタン
    $link  = REQUESTROOT."/E/KNJE386/knje386index.php?";
    $link .= "SEND_PRGID=KNJE387&YEAR=".CTRL_YEAR."&GRADE=".$model->grade;
    $extra = " onclick=\"window.open('$link','_self');\"";
    //onclick=\"loadwindow('../../E/KNJE386/index.php?SEND_PRGID=KNJE387&YEAR=".CTRL_YEAR."',0,document.documentElement.scrollTop || document.body.scrollTop,420,300);return;\"";
    //$arg["btn_fixrank"] = knjCreateBtn($objForm, "btn_fixrank", "総学力点算出", $extra);
    //CSV出力ボタン
    $link  = REQUESTROOT."/E/KNJE389/knje389index.php?";
    $link .= "SEND_PRGID=KNJE387&YEAR=".CTRL_YEAR."&GRADE=".$model->grade;
    //$extra = " onclick=\"loadwindow('../../E/KNJE389/index.php?SEND_PRGID=KNJE387&YEAR=".CTRL_YEAR."',0,document.documentElement.scrollTop || document.body.scrollTop,420,300);return;\"";
    $extra = " onclick=\"window.open('$link','_self');\"";
    //$arg["btn_csv"] = knjCreateBtn($objForm, "btn_csv", "CSV出力", $extra);
}

function judgeOrderStr($typestr, $model) {
    $retstr = "";
    if ($typestr === "score1_sort") {
        if ($model->sorttype === "SCORE1SORT_ASC") {
            $retstr = "▲";
        } else if ($model->sorttype === "SCORE1SORT_DESC") {
            $retstr = "▼";
        }
    } else if ($typestr === "score2_sort") {
        if ($model->sorttype === "SCORE2SORT_ASC") {
            $retstr = "▲";
        } else if ($model->sorttype === "SCORE2SORT_DESC") {
            $retstr = "▼";
        }
    } else if ($typestr === "score3_sort") {
        if ($model->sorttype === "SCORE3SORT_ASC") {
            $retstr = "▲";
        } else if ($model->sorttype === "SCORE3SORT_DESC") {
            $retstr = "▼";
        }
    } else if ($typestr === "score4_sort") {
        if ($model->sorttype === "SCORE4SORT_ASC") {
            $retstr = "▲";
        } else if ($model->sorttype === "SCORE4SORT_DESC") {
            $retstr = "▼";
        }
    } else if ($typestr === "score5_sort") {
        if ($model->sorttype === "SCORE5SORT_ASC") {
            $retstr = "▲";
        } else if ($model->sorttype === "SCORE5SORT_DESC") {
            $retstr = "▼";
        }
    } else if ($typestr === "sort_total") {
        if ($model->sorttype === "TOTAL_ASC") {
            $retstr = "▲";
        } else if ($model->sorttype === "TOTAL_DESC") {
            $retstr = "▼";
        }
    } else if ($typestr === "sort_class") {
        if ($model->sorttype === "CLASS_ASC") {
            $retstr = "▲";
        } else if ($model->sorttype === "CLASS_DESC") {
            $retstr = "▼";
        }
    }
    return $retstr;
}

//hidden作成
function makeHidden(&$objForm, $model, $arr_schregno) {
    knjCreateHidden($objForm, "cmd");
    knjCreateHidden($objForm, "HID_SCHREGNO", implode(",",$arr_schregno));
    knjCreateHidden($objForm, "SCHOOLCD", $model->schoolcd);
    knjCreateHidden($objForm, "SCHOOLKIND", $model->schoolkind);

    knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
    knjCreateHidden($objForm, "PRGID", "KNJE387");
    
    knjCreateHidden($objForm, "YEAR", $model->ObjYear);
    knjCreateHidden($objForm, "LOGIN_YEAR", CTRL_YEAR);
    knjCreateHidden($objForm, "LOGIN_SEMESTER", CTRL_SEMESTER);
    knjCreateHidden($objForm, "LOGIN_DATE", CTRL_DATE);

    knjCreateHidden($objForm, "SORT_TYPE", $model->sorttype);
}
?>
