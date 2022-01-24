<?php

require_once('for_php7.php');

class knjf160SubForm5
{
    function main(&$model)
    {
        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("subform5", "POST", "knjf160index.php", "", "subform5");

        //DB接続
        $db = Query::dbCheckOut();

        //生徒情報
        $hr_name = $db->getOne(knjf160Query::getHrName($model));
        $attendno = ($model->attendno) ? $model->attendno.'番' : "";
        $name = htmlspecialchars($model->name);
        $arg["SCHINFO"] = $hr_name.$attendno.'　'.$name;

        //年度(MAX7)
        $year_key = array();
        $date_key = array();
        $grade_item = array();
        $date_item = array();
        $year_cnt = 0;
        $grade_html = "";
        $date_html = "";
        $mark_html = "";

        //extra
        $extra_year = "STYLE=\"text-align: right\" onBlur=\"this.value=toInteger(this.value); YearCheck(this);\"";
        $extra_month = "STYLE=\"text-align: right\" onBlur=\"this.value=toInteger(this.value); MonthCheck(this);\"";

        $result = $db->query(knjf160Query::selectInvestDat($model));
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {

            $year_cnt++;
            if ($year_cnt > 7) break;   //MAX7
            $year_key[$year_cnt] = $row["YEAR"];
            $date_key[$year_cnt] = $row["E_DATE"];

            list($row["E_YEAR"], $row["E_MONTH"]) = explode("-", $row["E_DATE"]);
            $model->field["E_YEAR".$year_cnt] = ($row["E_YEAR"]) ? $row["E_YEAR"] : "";
            $model->field["E_MONTH".$year_cnt] = ($row["E_MONTH"]) ? $row["E_MONTH"] : "";
            $row["E_YEAR"] = knjCreateTextBox($objForm, $model->field["E_YEAR".$year_cnt], "E_YEAR".$year_cnt, 4, 4, $extra_year);
            $row["E_MONTH"] = knjCreateTextBox($objForm, $model->field["E_MONTH".$year_cnt], "E_MONTH".$year_cnt, 2, 2, $extra_month);
            $date_item[$year_cnt] = $row["E_YEAR"].'年<br>'.$row["E_MONTH"].'月';


            //学年コンボ
            $year = ($row["YEAR"]) ? $row["YEAR"] : "";
            $query = knjf160Query::getRegdGrade($model, $year);
            $opt = array();
            $opt[] = array('label' => "", 'value' => "");
            $value_flg = false;
            $model->field["GRADE".$year_cnt] = ($row["YEAR"]) ? $row["YEAR"] : "";
            $result1 = $db->query($query);
            while ($row1 = $result1->fetchRow(DB_FETCHMODE_ASSOC)) {

                $opt[] = array('label' => $row1["LABEL"],
                               'value' => $row1["VALUE"]);
                if ($model->field["GRADE".$year_cnt] == $row1["VALUE"]) $value_flg = true;
            }

            $model->field["GRADE".$year_cnt] = ($model->field["GRADE".$year_cnt] && $value_flg) ? $model->field["GRADE".$year_cnt] : $opt[0]["value"];
            $grade_item[$year_cnt] = knjCreateCombo($objForm, "GRADE".$year_cnt, $model->field["GRADE".$year_cnt], $opt, "style=\"width:65px;\"", 1);


            //項目欄
            if ($year_cnt < 7) {
                $grade_html .= "<th width=\"75\">".$grade_item[$year_cnt]."</th>";
                $date_html .= "<th width=\"75\">".$date_item[$year_cnt]."</th>";
                $mark_html .= "<th width=\"75\">○ △ 無</th>";
            } else {
                $grade_html .= "<th width=\"*\">".$grade_item[$year_cnt]."</th>";
                $date_html .= "<th width=\"*\">".$date_item[$year_cnt]."</th>";
                $mark_html .= "<th width=\"*\">○ △ 無</th>";
            }

            knjCreateHidden($objForm, "YEAR".$year_cnt, $row["YEAR"]);
            knjCreateHidden($objForm, "E_DATE".$year_cnt, $row["E_DATE"]);
        }

        $year_cnt = (int)$year_cnt+1;

        //学年コンボ作成
        $query = knjf160Query::getRegdGrade($model);
        $opt = array();
        $opt[] = array('label' => "", 'value' => "");
        $value_flg = false;
        $result1 = $db->query($query);
        while ($row1 = $result1->fetchRow(DB_FETCHMODE_ASSOC)) {

            $opt[] = array('label' => $row1["LABEL"],
                           'value' => $row1["VALUE"]);
            if ($row["YEAR"] == $row1["VALUE"]) $value_flg = true;
        }
        $row["YEAR"] = ($row["YEAR"] && $value_flg) ? $row["YEAR"] : $opt[0]["value"];

        if ($year_cnt < 7) {
            $grade_item[$year_cnt] = knjCreateCombo($objForm, "GRADE", $model->field["GRADE"], $opt, "style=\"width:65px;\"", 1);
            $grade_html .= "<th width=\"75\">$grade_item[$year_cnt]</th>";

            $row["E_YEAR"] = knjCreateTextBox($objForm, $model->field["E_YEAR"], "E_YEAR", 4, 4, $extra_year);
            $row["E_MONTH"] = knjCreateTextBox($objForm, $model->field["E_MONTH"], "E_MONTH", 2, 2, $extra_month);
            $date_item[$year_cnt] = $row["E_YEAR"].'年<br>'.$row["E_MONTH"]."月";
            $date_html .= "<th width=\"75\">$date_item[$year_cnt]</th>";

            $mark_html .= "<th width=\"75\">○ △ 無</th>";

        } else {
            $grade_item[$year_cnt] = knjCreateCombo($objForm, "GRADE", $model->field["GRADE"], $opt, "style=\"width:65px;\"", 1);
            $grade_html .= "<th width=\"*\">$grade_item[$year_cnt]</th>";

            $row["E_YEAR"] = knjCreateTextBox($objForm, $model->field["E_YEAR"], "E_YEAR", 4, 4, $extra_year);
            $row["E_MONTH"] = knjCreateTextBox($objForm, $model->field["E_MONTH"], "E_MONTH", 2, 2, $extra_month);
            $date_item[$year_cnt] = $row["E_YEAR"].'年<br>'.$row["E_MONTH"]."月";
            $date_html .= "<th width=\"*\">$date_item[$year_cnt]</th>";

            $mark_html .= "<th width=\"*\">○ △ 無</th>";

        }

        for ($i=1; $i<(7-$year_cnt); $i++){
            $grade_html .= "<th width=\"75\">&nbsp;</th>";
            $date_html .= "<th width=\"75\">&nbsp;</th>";
            $mark_html .= "<th width=\"75\">&nbsp;</th>";
        }
        if ($year_cnt < 7){
            $grade_html .= "<th width=\"*\">&nbsp;</th>";
            $date_html .= "<th width=\"*\">&nbsp;</th>";
            $mark_html .= "<th width=\"*\">&nbsp;</th>";
        }

        $arg["grade_html"] = $grade_html;
        $arg["date_html"] = $date_html;
        $arg["mark_html"] = $mark_html;

        //初期化
        $model->data = array();
        $counter = 0;

        //一覧表示
        $result = $db->query(knjf160Query::selectQuery5($model, $year_key));
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            //質問コードを配列で取得
            $model->data["QUESTIONCD"][] = $row["QUESTIONCD"];

            //各項目を作成
            foreach ($year_key as $code => $col)
            {
                //各年度を取得
                $model->data["YEAR"][$code] = $col;

                //回答ラジオボタン 1:ある 2:ときどきある 3:ない
                $opt_answer = array(1, 2, 3);
                $row["ANSWER".$code] = ($row["ANSWER".$code] == "") ? "" : $row["ANSWER".$code];

                $objForm->ae( array("type"       => "radio",
                                    "name"       => "ANSWER".$code."-".$counter,
                                    "value"      => $row["ANSWER".$code],
                                    "multiple"   => $opt_answer,
                                    "extrahtml"	 => "" ) );

                $row["ANSWER".$code."1"] = $objForm->ge("ANSWER".$code."-".$counter,1);
                $row["ANSWER".$code."2"] = $objForm->ge("ANSWER".$code."-".$counter,2);
                $row["ANSWER".$code."3"] = $objForm->ge("ANSWER".$code."-".$counter,3);

            }

            $code2 = ($code) ? (int)$code+1 : "1";

            //回答ラジオボタン 1:ある 2:ときどきある 3:ない
            $opt_answer = array(1, 2, 3);
            $row["ANSWER".$code2] = ($row["ANSWER".$code2] == "") ? "" : $row["ANSWER".$code2];

            $objForm->ae( array("type"       => "radio",
                                "name"       => "ANSWER".$code2."-".$counter,
                                "value"      => $row["ANSWER".$code2],
                                "multiple"   => $opt_answer,
                                "extrahtml"	 => "" ) );

            $row["ANSWER".$code2."1"] = $objForm->ge("ANSWER".$code2."-".$counter,1);
            $row["ANSWER".$code2."2"] = $objForm->ge("ANSWER".$code2."-".$counter,2);
            $row["ANSWER".$code2."3"] = $objForm->ge("ANSWER".$code2."-".$counter,3);

            $row["COLOR"]="#ffffff";

            $counter++;
            $arg["data"][] = $row;
        }

        //警告メッセージを表示しない場合
        if (isset($model->schregno) && !isset($model->warning)){
            $Row = $db->getRow(knjf160Query::getInvestAttention($model), DB_FETCHMODE_ASSOC);
        } else {
            $Row =& $model->field;
        }

        //生活上注意すべき点
        $extra = "style=\"height:62px;\"";
        $arg["data1"]["ATTENTION"] = knjCreateTextArea($objForm, "ATTENTION", 4, 101, "", $extra, $Row["ATTENTION"]);

        //ボタン作成
        makeBtn($objForm, $arg, $model);

        //hidden作成
        makeHidden($objForm, $model);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjf160SubForm5.html", $arg);
    }
}

//ボタン作成
function makeBtn(&$objForm, &$arg, $model)
{
    //更新ボタンを作成する
    $extra = "onclick=\"return btn_submit('subform5_update');\"";
    $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);
    //クリアボタンを作成する
    $extra = "onclick=\"return btn_submit('subform5_clear');\"";
    $arg["button"]["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);
    //戻るボタン
    $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "戻 る", "onclick=\"return btn_submit('edit');\"");
}

//hidden作成
function makeHidden(&$objForm, $model)
{
    knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
    knjCreateHidden($objForm, "SCHREGNO", $model->schregno);
    knjCreateHidden($objForm, "cmd");
}
?>
