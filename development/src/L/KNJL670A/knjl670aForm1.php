<?php

require_once('for_php7.php');


class knjl670aForm1
{
    public function main(&$model)
    {

        //オブジェクト作成
        $objForm = new form();

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["TOP"]["YEAR"] = $model->ObjYear;

        //入試制度コンボボックス
        $extra = "onchange=\"return btn_submit('app');\" tabindex=-1";
        $query = knjl670aQuery::getNameMst("L003", $model->ObjYear);
        makeCmb($objForm, $arg, $db, $query, "APPLICANTDIV", $model->applicantdiv, $extra, 1);

        //入試制度が確定したので、教育課程コード系を確定する。
        $model->setCourseCdMajorCd();

        //入試区分コンボボックス
        $extra = "onchange=\"return btn_submit('main');\" tabindex=-1";
        $model->testdiv = ($model->applicantdiv == $model->appHold) ? $model->testdiv : "";
        $namecd1 = ($model->applicantdiv == "1") ? "L024" : "L004";
        $query = knjl670aQuery::getNameMst($namecd1, $model->ObjYear);
        makeCmb($objForm, $arg, $db, $query, "TESTDIV", $model->testdiv, $extra, 1);

        //受験コースコンボボックス
        $query = knjl670aQuery::getCourseCode($model);
        $extra = "onchange=\"return btn_submit('main');\" tabindex=-1";
        makeCmb($objForm, $arg, $db, $query, "COURSECODE", $model->coursecode, $extra, 1);

        //得点絞り込み
        //画面最上部の値が切り替わるイベントだったらクリア
        if ($model->cmd == "app" || $model->cmd == "main") {
            $model->leastScore = "";
            $model->topScore = "";
        }
        $extra = "id=\"LEAST_SCORE\" onblur=\"return chkScore(this);\"";
        $arg["TOP"]["LEAST_SCORE"] = knjCreateTextBox($objForm, $model->leastScore, "LEAST_SCORE", 3, 3, $extra);
        $extra = "id=\"TOP_SCORE\" onblur=\"return chkScore(this);\"";
        $arg["TOP"]["TOP_SCORE"] = knjCreateTextBox($objForm, $model->topScore, "TOP_SCORE", 3, 3, $extra);

        //合否入力済みを除外
        //画面最上部の値が切り替わるイベントだったらクリア
        if ($model->cmd == "app" || $model->cmd == "main") {
            $model->rejectPass = "";
            $model->rejectUnPass = "";
        }
        $extra  = "id=\"REJECT_PASS\"";
        $extra .= ($model->rejectPass == "1") ? " checked": "";
        $arg["TOP"]["REJECT_PASS"] = knjCreateCheckBox($objForm, "REJECT_PASS", "1", $extra);

        $extra  = "id=\"REJECT_UNPASS\"";
        $extra .= ($model->rejectUnPass == "1") ? " checked": "";
        $arg["TOP"]["REJECT_UNPASS"] = knjCreateCheckBox($objForm, "REJECT_UNPASS", "1", $extra);

        //一括入力
        $extra = "";
        $query = knjl670aQuery::getNameMst("L013", $model->ObjYear);
        makeCmb($objForm, $arg, $db, $query, "SEL_SETRESULT", $model->selSetResult, $extra, 1);

        //特別判定表示フラグ
        $isSpecialShow = ($model->applicantdiv == "2" && $model->testdiv == "1") ? true : false;

        //判定名
        //JAVASCRIPTで変更時にラベル表示する用。
        $arrJudgeName = array();
        $judgediv_name = $seq = "";
        $result = $db->query(knjl670aQuery::getNameMst2("L013", $model->ObjYear));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $arrJudgeName[$row["NAMECD2"]] = $row["NAME1"];
            $arg["data2"][] = array("judgediv_cd" => $row["NAMECD2"], "judgediv_name" => $row["NAME1"]);
            $judgediv_name .= $seq .$row["NAMECD2"].":".$row["NAME1"];
            $seq = ",";
        }
        $arg["TOP"]["JUDGE"] = $judgediv_name;

        //一覧表示
        $arr_receptno = array();
        if ($model->applicantdiv != "" && $model->testdiv != "" && $model->coursecode != "") {
            if (!$model->isWarning()) {
                $model->score = array();
            }

            //ソート
            $model->sort = ($model->sort == "") ? "0" : $model->sort;
            //データ取得
            $result = $db->query(knjl670aQuery::SelectQuery($model));

            if ($result->numRows() == 0 ) {
                $model->setMessage("MSG303");
                $arg["POINT_SORT"] = "<span STYLE=\"color:white\">総合得点{$setOrder}</span>";
            } else {
                //更新チェックボックス(ヘッダ)
                if ($model->cmd == "app" || $model->cmd == "main" || $model->cmd == "load" || $model->cmd == "reload") {
                    $checked = "";
                } else {
                    $checked = ($model->checkAll == "1") ? " checked": "";
                }
                $extra = " id=\"CHECKALL\" onchange=\"setSelChk(this);\" ".$checked;
                $arg["CHECKALL"] = knjCreateCheckBox($objForm, "CHECKALL", "1", $extra);

                //ソート表示文字作成
                $order[0] = "";
                $order[1] = "▼";
                $order[2] = "▲";
                $setOrder = $order[$model->sort];
                $arg["POINT_SORT"] = "<a href=\"javascript:postSort();\" STYLE=\"color:white\">総合得点{$setOrder}</a>";
            }

            $rcnt = 0;
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                array_walk($row, "htmlspecialchars_array");

                //HIDDENに保持する用
                $arr_receptno[] = $row["RECEPTNO"];

                $value = ($model->isWarning()) ? $model->score[$row["RECEPTNO"]]["JUDGEDIV"] : $row["JUDGEDIV"];
                $disflg = $model->applicantdiv == '2' && $row["FIRST_PASSCOURSE"] != "" ? " disabled" : "";
                $extra = "OnChange=\"Setflg(this);\" id=\"JUDGEDIV-".$row["RECEPTNO"]."\" style=\"text-align:center;\" onblur=\"this.value = toInteger(this.value);setName(this,".$row["RECEPTNO"].",'0', '".$value."');\" onKeyDown=\"keyChangeEntToTab(this);\" onPaste=\"return showPaste(this, $rcnt);\"".$disflg;
                $row["JUDGEDIV"] = knjCreateTextBox($objForm, $value, "JUDGEDIV-".$row["RECEPTNO"], 3, 3, $extra);

                //innerHTML用ID
                $row["JUDGEDIV_ID"] = "JUDGEDIV_NAME" .$row["RECEPTNO"];

                if ($model->isWarning()) {
                    $row["JUDGEDIV_NAME"] = $arrJudgeName[$model->score[$row["RECEPTNO"]]["JUDGEDIV"]];
                }

                //更新チェックボックス(ヘッダ)
                $checked = ($model->checkAll == "1" || $model->data["CHECKED"][$row["RECEPTNO"]] == "1") ? " checked": "";
                $extra = "id=\"CHECKED_{$row["RECEPTNO"]}\"".$checked.$disflg;
                $row["CHECKED"] = knjCreateCheckBox($objForm, "CHECKED_".$row["RECEPTNO"], "1", $extra);

                $arg["data"][] = $row;
                $rcnt++;
            }
        }

        //ボタン作成
        makeBtn($objForm, $arg);

        //hidden作成
        makeHidden($objForm, $model, $arr_receptno, $result->numRows());

        //DB切断
        Query::dbCheckIn($db);

        //フォーム作成
        $arg["start"] = $objForm->get_start("main", "POST", "knjl670aindex.php", "", "main");

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjl670aForm1.html", $arg);
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
        if ($blank == "BLANK") {
            continue;
        }
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

//ボタン作成
function makeBtn(&$objForm, &$arg)
{
    //読込ボタン
    $extra = "onclick=\"return btn_submit('load');\"";
    $arg["btn_load"] = knjCreateBtn($objForm, "btn_load", "読 込", $extra);
    //反映ボタン
    $extra = "onclick=\"return valReplace();\"";
    $arg["btn_replace"] = knjCreateBtn($objForm, "btn_replace", "反 映", $extra);
    //更新ボタン
    $extra = "onclick=\"return btn_submit('update');\"";
    $arg["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);
    //取消ボタン
    $extra = "onclick=\"return btn_submit('reset');\"";
    $arg["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);
    //終了ボタン
    $extra = "onclick=\"closeWin();\"";
    $arg["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);
}

//hidden作成
function makeHidden(&$objForm, $model, $arr_receptno, $cnt)
{
    knjCreateHidden($objForm, "cmd");
    knjCreateHidden($objForm, "HID_RECEPTNO", implode(",", $arr_receptno));
    knjCreateHidden($objForm, "HID_APPLICANTDIV");
    knjCreateHidden($objForm, "HID_TESTDIV");
    knjCreateHidden($objForm, "HID_COURSECODE");
    knjCreateHidden($objForm, "HID_SORT", $model->sort);
    knjCreateHidden($objForm, "ALL_COUNT", $cnt);

    knjCreateHidden($objForm, "APP_HOLD", $model->applicantdiv);
    knjCreateHidden($objForm, "CHGFLG", 0); //入力変更フラグ。javascriptでのみ利用。
}
