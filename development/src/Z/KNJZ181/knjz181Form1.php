<?php

require_once('for_php7.php');

class knjz181form1
{
    function main(&$model)
    {
        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("main", "POST", "knjz181index.php", "", "main");

        $db = Query::dbCheckOut();

        //対象年度
        $query = knjz181Query::getTargetYear();
        $extra = "onChange=\"return btn_submit('changeYear')\"";
        $model->year = $model->year ? $model->year : CTRL_YEAR;
        makeCmb($objForm, $arg, $db, $query, $model->year, "YEAR", $extra, 1);

        //参照年度コンボ
        $query = knjz181Query::getCopyYear();
        $extra = "";
        $model->copyYear = $model->copyYear ? $model->copyYear : ($model->year - 1);
        makeCmb($objForm, $arg, $db, $query, $model->copyYear, "COPY_YEAR", $extra, 1, "BLANK");

        //コピーボタン
        $extra = "onclick=\"return btn_submit('copy');\"";
        $arg["button"]["btn_copy"] = knjCreateBtn($objForm, "btn_copy", "左の年度データをコピー", $extra);

        //対象学年
        $query = knjz181Query::getGrade($model->year);
        $extra = "onChange=\"return btn_submit('change')\"";
        makeCmb($objForm, $arg, $db, $query, $model->grade, "GRADE", $extra, 1, "BLANK");

        //対象科目
        $query = knjz181Query::getMockSubclass();
        $extra = "onChange=\"return btn_submit('change')\"";
        makeCmb($objForm, $arg, $db, $query, $model->mockSubclassCd, "MOCK_SUBCLASS_CD", $extra, 1, "BLANK");

        $query = knjz181Query::getAssessLevelCnt($model);
        $assesslevelcnt = $db->getOne($query);

        if ($model->cmd == "level" || ($model->cmd == "main" && $model->level != "")) {
            $assesslevelcnt = $model->level;
        }
        $cnt = $assesslevelcnt;

        //段階数
        $extra = "";
        $arg["sepa"]["ASSESSLEVELCNT"] = knjCreateTextBox($objForm, $assesslevelcnt, "ASSESSLEVELCNT", 6, 3, $extra);

        //確定ボタン
        $extra = "onclick=\"return level(".$cnt.");\"";
        $arg["button"]["btn_level"] = knjCreateBtn($objForm, "btn_level", "確 定", $extra);

        //警告メッセージを表示しない場合
        if (!isset($model->warning)){
            $query = knjz181Query::selectQuery($model);
            $result = $db->query($query);
        }else{
            $row =& $model->field;
            $result = "";
        }

        for ($i = 1; $i <= $cnt; $i++) {
            if($result != ""){
                if ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                    //レコードを連想配列のまま配列$arg[data]に追加していく。 
                    array_walk($row, "htmlspecialchars_array");

                    //評定区分1と2は小数点を取り除く。
                    $ar =explode(".",$row["ASSESSLOW"]);
                    $row["ASSESSLOW"] = $ar[0];
                    $ar =explode(".",$row["ASSESSHIGH"]);
                    $row["ASSESSHIGH"] = $ar[0];
                }
            }
            $row["ASSESSLEVEL"] = $i;

            //下限
            $lowVal = "";
            if ($model->cmd !="level") {
                if ($result != "") {
                    $lowVal = $row["ASSESSLOW"];
                } else {
                    $lowVal = $row["ASSESSLOW".$i];
                }
            }
            if ($row["ASSESSLEVEL"] == $cnt) {
                $extra = "STYLE=\"text-align: right\"";
            } else {
                $extra = "onblur=\"this.value=toInteger(this.value);isNumb(this,".($row["ASSESSLEVEL"] + 1).",'ELSE');\" STYLE=\"text-align: right\"";
            }
            $row["ASSESSLOWTEXT"] = knjCreateTextBox($objForm, $lowVal, "ASSESSLOW".$row["ASSESSLEVEL"], 4, 3, $extra);

            //上限部分作成
            if ($row["ASSESSLEVEL"] == "1") {
                $highVal = "";
                if ($model->cmd != "level") {
                    if ($result != "") {
                        $highVal = $row["ASSESSHIGH"];
                    } else {
                        $highVal = $row["ASSESSHIGH".$i];
                    }
                }
                $extra = "onblur=\"toNumber(this.value);\" STYLE=\"text-align: right\"";
                $row["ASSESSHIGHTEXT"] = knjCreateTextBox($objForm, $highVal, "ASSESSHIGH".$row["ASSESSLEVEL"], 4, 3, $extra);

            } else {
                $row["ASSESSHIGHTEXT"]  = "<span id=\"strID";
                $row["ASSESSHIGHTEXT"] .= $row["ASSESSLEVEL"];
                $row["ASSESSHIGHTEXT"] .= "\">";
                if ($result != "") {
                    if ($model->cmd != "level") {
                        $row["ASSESSHIGHTEXT"] .= $row["ASSESSHIGH"];
                    }
                } else {
                    $row["ASSESSHIGHTEXT"] .= ($row["ASSESSLOW".($i + 1)] - (($model->sepa == 4)? 0.1 : 1));
                }
                $row["ASSESSHIGHTEXT"] .= "</span>";
            }
            //記号部分作成
            $markVal = "";
            if ($model->cmd != "level") {
                if ($result != "") {
                    $markVal = $row["ASSESSMARK"];
                } else {
                    $markVal = $row["ASSESSMARK".$i];
                }              
            }
            $extra = "STYLE=\"text-align: right\"";
            $row["ASSESSMARKTEXT"] = knjCreateTextBox($objForm, $markVal, "ASSESSMARK".$row["ASSESSLEVEL"], 8, 6, $extra);

            $arg["data"][] = $row;
        }

        //更新ボタン
        $extra = "onclick=\"return btn_submit('update');\"";
        $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);

        //取消ボタン
        $extra = "onclick=\"return btn_submit('clear');\"";
        $arg["button"]["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);

        //終了ボタン
        $extra = "onclick=\"return closeWin();\"";
        $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);

        //hidden
        knjCreateHidden($objForm, "cmd");

        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjz181Form1.html", $arg);
    }
}

//makeCmb
function makeCmb(&$objForm, &$arg, $db, $query, &$value, $name, $extra, $size, $blank = "")
{

    $opt = array();
    if ($blank == "BLANK") {
        $opt[] = array("label" => "", "value" => "");
    }
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);
        if ($value == $row["VALUE"]) $value_flg = true;
    }
    $value = ($value && $value_flg) ? $value : $opt[0]["value"];
    $arg["sepa"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);

    $result->free();
}

?>
