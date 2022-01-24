<?php

require_once('for_php7.php');

class knjz178form1 {
    function main(&$model) {
        $objForm = new form;
        //フォーム作成
        $arg["start"] = $objForm->get_start("main", "POST", "knjz178index.php", "", "main");
        //DB接続
        $db = Query::dbCheckOut();

        $model->field["SEMESTER"] = $model->field["SEMESTER"] ? $model->field["SEMESTER"] : CTRL_SEMESTER;

        /******************/
        /* ↓↓リスト↓↓ */
        /******************/
        if ($model->cmd == 'level') {
            //初期値として直前の学期の値を取得するが、
            //9学期に関しては直前の8学期存在しないので、
            //その場合はダミーとして9学期以外の最大学期の値を元にする
            $query = knjz178Query::getDummy_semester($model);
            $target_semester = $db->getOne($query);

            $query = knjz178Query::getList_before($target_semester, $model); //リストデータ取得
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $before_semester[] = $row;
            }
        }
        $model->field["GRADE"] = $model->field["GRADE"] ? $model->field["GRADE"] : '01';
        $query = knjz178Query::getList($model->field["GRADE"]); //リストデータ取得
        $result = $db->query($query);
        $firstFlg  = false; //一回目のループかどうかのフラグ
        $rowCntFlg = false; //行数を監視するべきかのフラグ
        $cnt = 0;
        $mainlist = '';
        $assesslevelcnt = 0; //評定段階数の初期値
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            //レコードを出力せず次のレコードに行く条件
            if (($model->cmd != 'level' && !$row["COUNT"]) ||
                ($model->cmd == 'level' && $row["SEMESTER"] == $model->field["SEMESTER"] && $model->field["ASSESSLEVELCNT"] == '0') ||
                ($model->cmd == 'level' && $row["SEMESTER"] != $model->field["SEMESTER"] && !$row["COUNT"])
                ) {
                continue;
            }

            //学期が変わったとき、直前の学期の残りを出力して、学期名を出力する
            if ($semester != $row["SEMESTER"]) { //学期名が変わったら
                if ($firstFlg && $semester == $model->field["SEMESTER"] && $model->cmd == 'level') {
                    for ($i = $cnt; $i < $model->field["ASSESSLEVELCNT"]; $i++) {
                        $mainlist .= "\t<td>".($i+1)."</td>\n";
                        $mainlist .= "\t<td>".knjCreateTextBox($objForm, $before_semester[$i]["ASSESSMARK"], "ASSESSMARK_{$semester}_".($i+1), 6, 3, "")."</td>\n";
                        $mainlist .= "\t<td>".knjCreateTextBox($objForm, $before_semester[$i]["ASSESSLOW"], "ASSESSLOW_{$semester}_" .($i+1), 4, 4, "onChange=\"changeVal_low(this);\"")."</td>\n";
                        $mainlist .= "\t<td>".knjCreateTextBox($objForm, $before_semester[$i]["ASSESSHIGH"], "ASSESSHIGH_{$semester}_" .($i+1), 4, 4, "onChange=\"changeVal_high(this);\"")."</td>\n";
                        $mainlist .= "</tr>\n";
                        $mainlist .= "<tr align=\"center\" bgcolor=\"#ffffff\">\n";

                        $arg["data"][]["MAINLIST"] = $mainlist;
                        $mainlist = "";
                    }
                }
                $cnt = 0;
                if ($row["SEMESTER"] == $model->field["SEMESTER"]) { //学期名
                    if ($model->cmd == 'level') {
                        $mainlist .= "<td rowspan='{$model->field["ASSESSLEVELCNT"]}'>{$row["SEMESTERNAME"]}</td>\n";
                        $assesslevelcnt = $model->field["ASSESSLEVELCNT"];
                    } else {
                        $mainlist .= "<td rowspan='{$row["COUNT"]}'>{$row["SEMESTERNAME"]}</td>\n";
                        $assesslevelcnt = $row["COUNT"];
                    }
                } else {
                    $mainlist .= "<td rowspan='{$row["COUNT"]}'>{$row["SEMESTERNAME"]}</td>\n";
                }
            }

            //<td>タグの部分だけど、評定段階数が実際のレコードの数より少なかったら表示する行数を少なくする
            if ($row["SEMESTER"] == $model->field["SEMESTER"]) {
                if ($model->cmd == 'level') {
                    $row["ASSESSLEVEL"] = $row["ASSESSLEVEL"] ? $row["ASSESSLEVEL"] : ($cnt + 1);
                    $row["ASSESSMARK"]  = knjCreateTextBox($objForm,  $before_semester[$cnt]["ASSESSMARK"],  "ASSESSMARK_{$row["SEMESTER"]}_". ($cnt+1), 6, 3, "");
                    $row["ASSESSLOW"]   = knjCreateTextBox($objForm,  $before_semester[$cnt]["ASSESSLOW"],   "ASSESSLOW_{$row["SEMESTER"]}_" . ($cnt+1), 4, 4, "onChange=\"changeVal_low(this);\"");
                    $row["ASSESSHIGH"]  = knjCreateTextBox($objForm,  $before_semester[$cnt]["ASSESSHIGH"],  "ASSESSHIGH_{$row["SEMESTER"]}_" . ($cnt+1), 4, 4, "onChange=\"changeVal_high(this);\"");
                    if ($cnt < $model->field["ASSESSLEVELCNT"]) {
                        $mainlist .= "\t<td>{$row["ASSESSLEVEL"]}</td>\n";
                        $mainlist .= "\t<td>{$row["ASSESSMARK"]}</td>\n";
                        $mainlist .= "\t<td>{$row["ASSESSLOW"]}</td>\n";
                        $mainlist .= "\t<td>{$row["ASSESSHIGH"]}</td>\n";
                    }
                } else {
                    $row["ASSESSLEVEL"] = $row["ASSESSLEVEL"] ? $row["ASSESSLEVEL"] : ($cnt + 1);
                    $row["ASSESSMARK"]  = knjCreateTextBox($objForm,  $row["ASSESSMARK"],  "ASSESSMARK_{$row["SEMESTER"]}_". ($cnt+1), 6, 3, "");
                    $row["ASSESSLOW"]   = knjCreateTextBox($objForm,  $row["ASSESSLOW"],   "ASSESSLOW_{$row["SEMESTER"]}_" . ($cnt+1), 4, 4, "onChange=\"changeVal_low(this);\"");
                    $row["ASSESSHIGH"]  = knjCreateTextBox($objForm,  $row["ASSESSHIGH"],  "ASSESSHIGH_{$row["SEMESTER"]}_" . ($cnt+1), 4, 4, "onChange=\"changeVal_high(this);\"");
                    $mainlist .= "\t<td>{$row["ASSESSLEVEL"]}</td>\n";
                    $mainlist .= "\t<td>{$row["ASSESSMARK"]}</td>\n";
                    $mainlist .= "\t<td>{$row["ASSESSLOW"]}</td>\n";
                    $mainlist .= "\t<td>{$row["ASSESSHIGH"]}</td>\n";
                }
            } else {
                $mainlist .= "\t<td>{$row["ASSESSLEVEL"]}</td>\n";
                $mainlist .= "\t<td>{$row["ASSESSMARK"]}</td>\n";
                $mainlist .= "\t<td>{$row["ASSESSLOW"]}</td>\n";
                $mainlist .= "\t<td>{$row["ASSESSHIGH"]}</td>\n";
            }

            $arg["data"][]["MAINLIST"] = $mainlist;

            $mainlist = ""; //初期化
            if (($row["SEMESTER"] != $model->field["SEMESTER"] || $model->cmd != "level" || $cnt < $model->field["ASSESSLEVELCNT"])) {
                if ($row["SEMESTER"] == $model->field["SEMESTER"] && $row["SEMESTER"] == '9' && $cnt + 1 >= $model->field["ASSESSLEVELCNT"] && $model->cmd == 'level') {
                    //どうif分を書いていいかわからないのでこのままにしておきます。
                    //やりたかったことは一番最後の「tr」は出力したくないってことです。
                } else {
                    $mainlist .= "</tr>\n";
                    $mainlist .= "<tr align=\"center\" bgcolor=\"#ffffff\">\n";
                }
            }

            $semester = $row["SEMESTER"];

            $firstFlg = true;
            $cnt++;
        }
        //最後の学期はwhileのループから抜けてしまうので足りないフィールドを追加する処理を別途実行しています。
        if ($firstFlg && $semester == $model->field["SEMESTER"] && $model->cmd == 'level') {
            for ($i = $cnt; $i < $model->field["ASSESSLEVELCNT"]; $i++) {
                if ($i == $model->field["ASSESSLEVELCNT"] - 1) { //上限の最大値は10.0である
                    $row["ASSESSHIGH"] = '10.0';
                }
                $mainlist  = "</tr>\n";
                $mainlist .= "<tr align=\"center\" bgcolor=\"#ffffff\">\n";
                $mainlist .= "\t<td>".($i+1)."</td>\n";
                $mainlist .= "\t<td>".knjCreateTextBox($objForm, $before_semester[$i]["ASSESSMARK"], "ASSESSMARK_{$semester}_".($i+1), 6, 3, "")."</td>\n";
                $mainlist .= "\t<td>".knjCreateTextBox($objForm, $before_semester[$i]["ASSESSLOW"],  "ASSESSLOW_{$semester}_" .($i+1), 4, 4, "onChange=\"changeVal_low(this);\"")."</td>\n";
                $mainlist .= "\t<td>".knjCreateTextBox($objForm, $before_semester[$i]["ASSESSHIGH"], "ASSESSHIGH_{$semester}_".($i+1), 4, 4, "onChange=\"changeVal_high(this);\"")."</td>\n";

                $arg["data"][]["MAINLIST"] = $mainlist;
            }
        }
        /******************/
        /* ↑↑リスト↑↑ */
        /******************/

        /**********/
        /* コンボ */
        /**********/
        //学年コンボ
        $extra = "onChange=\"return btn_submit('change');\" ";
        $query = knjz178Query::getGrade();
        makeCmb($objForm, $arg, $db, $query, $model->field["GRADE"], "GRADE", $extra, 1);
        //学期コンボ
        $extra = "onChange=\"return btn_submit('change');\" ";
        $query = knjz178Query::getSemester();
        makeCmb($objForm, $arg, $db, $query, $model->field["SEMESTER"], "SEMESTER", $extra, 1);

        /********************/
        /* テキストボックス */
        /********************/
        //評定段階数
        $extra = " STYLE=\"text-align: right\"; onblur=\"this.value=toFloat(this.value)\"";
        $arg["sepa"]["ASSESSLEVELCNT"] = knjCreateTextBox($objForm, $assesslevelcnt, "ASSESSLEVELCNT", 2, 2, $extra);

        /**********/
        /* ボタン */
        /**********/
        //確定ボタン
        $extra = "onclick=\"return level({$assesslevelcnt});\" ";
        $arg["sepa"]["btn_kakutei"] = knjCreateBtn($objForm, "btn_kakutei", "確 定", $extra);
        //更新ボタン
        $extra = "onclick=\"return btn_submit('update');\" ";
        $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);
        //取消ボタン
        $extra = "onclick=\"return btn_submit('clear');\" ";
        $arg["button"]["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);
        //終了ボタン
        $extra = "onclick=\"return closeWin();\" ";
        $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);

        /**********/
        /* hidden */
        /**********/
        knjCreateHidden($objForm, "cmd");

        //DB切断
        Query::dbCheckIn($db);
        $arg["finish"]  = $objForm->get_finish();

        if(AUTHORITY != DEF_UPDATABLE) {
            $arg["Closing"] = " closing_window(); " ;
        }

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjz178Form1.html", $arg);
    }
}
/********************************************** 以下関数 **********************************************/
//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, &$value, $name, $extra, $size, $blank = "") {
    $opt = array();
    if ($blank == "BLANK") {
        $opt[] = array("label" => "",
                       "value" => "");
    }

    if ($query) {
        $value_flg = false;
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[] = array("label" => $row["LABEL"],
                           "value" => $row["VALUE"]);
            if ($value == $row["VALUE"]) $value_flg = true;
        }
        $result->free();
        $value = ($value && $value_flg) ? $value : $opt[0]["value"];
    }
    $arg["sepa"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}
?>
