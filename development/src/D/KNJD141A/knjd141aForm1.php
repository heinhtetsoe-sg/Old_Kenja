<?php

require_once('for_php7.php');

class knjd141aForm1
{
    public function main(&$model)
    {

        //オブジェクト作成
        $objForm = new form();

        //フォーム作成
        $arg["start"] = $objForm->get_start("main", "POST", "knjd141aindex.php", "", "main");

        // Add by PP for CurrentCursor and PC-Talker 2020-01-20 start
        $arg["TITLE_LABEL"] = "講座別欠時数情報入力画面";
        echo "<script>var TITLE= '".$arg["TITLE_LABEL"]."';
              </script>";
        // Add by PP for CurrentCursor and PC-Talker 2020-01-31 end

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["YEAR"] = CTRL_YEAR;

        //学期
        $arg["SEMESTER"] = CTRL_SEMESTERNAME;
        
        $query = knjd141aQuery::getYearList($model);
        $extra = "onChange=\"btn_submit('edit')\"";
        makeCmb($objForm, $arg, $db, $query, $model->field["sansyouYear"], "sansyouYear", $extra, 1, "BLANK");
        
        $query = knjd141aQuery::getSemesterList($model, $model->field["sansyouYear"]);
        $extra = "onChange=\"btn_submit('edit')\"";
        makeCmb($objForm, $arg, $db, $query, $model->field["sansyouSemester"], "sansyouSemester", $extra, 1, "BLANK");
        
        $query = knjd141aQuery::getKousaList($model, $model->field["sansyouYear"], $model->field["sansyouSemester"]);
        $extra = "onChange=\"btn_submit('edit')\"";
        makeCmb($objForm, $arg, $db, $query, $model->field["sansyouKousa"], "sansyouKousa", $extra, 1, "BLANK");
        
        $query = knjd141aQuery::getKamokuList($model, $model->field["sansyouYear"]);
        $extra = "onChange=\"btn_submit('edit')\"";
        makeCmb($objForm, $arg, $db, $query, $model->field["sansyouKamoku"], "sansyouKamoku", $extra, 1, "BLANK");

        //コピーボタン
        $extra = "onClick=\"btn_submit('copy')\"";
        $arg["btn_copy"] = knjCreateBtn($objForm, "btn_copy", '左の設定データを対象考査にコピー', $extra);
        
        $query = knjd141aQuery::getSemesterList($model, $model->year);
        $extra = "onChange=\"btn_submit('edit')\"";
        makeCmb($objForm, $arg, $db, $query, $model->field["taisyouSemester"], "taisyouSemester", $extra, 1, "BLANK");
        
        $query = knjd141aQuery::getKousaList($model, $model->year, $model->field["taisyouSemester"]);
        $extra = "onChange=\"btn_submit('edit')\"";
        makeCmb($objForm, $arg, $db, $query, $model->field["taisyouKousa"], "taisyouKousa", $extra, 1, "BLANK");
        
        $query = knjd141aQuery::getKamokuList($model, $model->year);
        $extra = "onChange=\"btn_submit('edit')\"";
        makeCmb($objForm, $arg, $db, $query, $model->field["taisyouKamoku"], "taisyouKamoku", $extra, 1, "BLANK");



        if ($model->field["taisyouSemester"] != '' && $model->field["taisyouKousa"] != '' && $model->field["taisyouKamoku"] != '') {
            $sep = '';
            $chaircds = '';
            $cnt = 0;
            $result = $db->query(knjd141aQuery::selectQuery($model));
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $result2 = $db->query(knjd141aQuery::selectStaff($model, $row['CHAIRCD']));
                $cnt2 = 0;
                while ($row2 = $result2->fetchRow(DB_FETCHMODE_ASSOC)) {
                    if ($cnt2 == 0) {
                        $row['STAFF_ONE'] = $row2;
                    } else {
                        $row['STAFF'][] = $row2;
                    }
                    $cnt2++;
                }
                $row['STAFFCNT'] = $cnt2;
                $extra = "onblur=\"this.value=toInteger(this.value);attentoinScoreCheck(this,this.value)\"";
                $row['ATTENTION_SCORE'] = knjCreateTextBox($objForm, $row['ATTENTION_SCORE'], "ATTENTION_SCORE-" . $cnt, 3, 3, $extra);
                $arg['data'][] = $row;
                
                $chaircds .= $sep . $row['CHAIRCD'];
                $sep = ',';
                $cnt++;
            }
            knjCreateHidden($objForm, "chaircds", $chaircds);
            knjCreateHidden($objForm, "maxcnt", $cnt);
        }
        
        //hidden作成
        knjCreateHidden($objForm, "cmd");

        makeButton($objForm, $arg, $db, $model);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"] = $objForm->get_finish();

        //テンプレート呼び出し
        View::toHTML($model, "knjd141aForm1.html", $arg);
    }
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, &$value, $name, $extra, $size, $blank = "")
{
    $opt = array();
    if ($blank == "BLANK") {
        $opt[] = array("label" => "", "value" => "");
    }
    if ($name == 'sansyouKamoku' || $name == 'taisyouKamoku') {
        $opt[] = array("label" => '全科目', "value" => "all");
        if ($value == "all") {
            $value_flg = true;
        }
    }
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);
        if ($value == $row["VALUE"]) {
            $value_flg = true;
        }
    }
    $value = ($value && $value_flg) ? $value : $opt[0]["value"];
    $arg[$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);

    $result->free();
}

//ボタン作成
function makeButton(&$objForm, &$arg, $db, $model)
{
    //保存ボタン
    $extra = "onclick=\"return btn_submit('update');\"";
    $arg["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);
    
    //取消ボタン
    $extra = "onclick=\"return btn_submit('clear');\"";
    $arg["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);
    
    //終了ボタン
    $extra = "onclick=\"closeWin();\"";
    $arg["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);
}
