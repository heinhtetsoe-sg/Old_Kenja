<?php
class knjj521Form1
{
    function main(&$model)
    {
        $objForm = new form;

        //フォーム作成
        $arg["start"]    = $objForm->get_start("form1", "POST", "knjj521index.php", "", "form1");

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["YEAR"] = CTRL_YEAR;

        //年組コンボ
        $query = knjj521Query::getHrClass($model);
        $extra = "onchange=\"return btn_submit('form1')\"";
        makeCmb($objForm, $arg, $db, $query, "GRADE_HR_CLASS", $model->field["GRADE_HR_CLASS"], $extra, 1, "blank");

        //測定日付
        $date = $db->getOne(knjj521Query::getScoreDate($model));
        $model->field["DATE"] = (strlen($date)) ? str_replace("-", "/", $date) : str_replace("-", "/", CTRL_DATE);
        $arg["DATE"] = View::popUpCalendar($objForm, "DATE", $model->field["DATE"]);


        //ヘッダ作成
        $item_abbv = $item_unit = "";

        //問診コード
        $inquiryWidth = 40;
        $arg["INQUIRY_WIDTH"] = $inquiryWidth;
        $inquiryAllWidth = $model->maxInquiryNum * $inquiryWidth;
        $item_abbv .= "<th width=\"{$inquiryAllWidth}\" colspan=\"{$model->maxInquiryNum}\"><font size=\"2\">問診項目</font></th>";
        $inquiryCdArray = range(1, $model->maxInquiryNum);
        foreach ($inquiryCdArray as $inquiryCd) {
            //単位
            $item_unit .= "<th width=\"{$inquiryWidth}\"><font size=\"2\">{$inquiryCd}</font></th>";
        }

        //種目コード(MAX9)
        $item_key = array();
        $item_cnt = 0;
        $result = $db->query(knjj521Query::getSportsItemMst());
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            $item_cnt++;
            if ($item_cnt > 9) break;
            $item_key[$item_cnt] = $row["ITEMCD"];
            //略称名
            $item_abbv .= "<th width=\"70\"><font size=\"2\">{$row["ITEMABBV"]}</font></th>";
            //単位
            $item_unit .= "<th width=\"70\"><font size=\"2\">{$row["UNIT"]}</font></th>";
        }
        for ($i = 0; $i < (9 - count($item_key)); $i++) {
            $item_abbv .= "<th width=\"70\">&nbsp;</th>";
            $item_unit .= "<th width=\"70\">&nbsp;</th>";
        }
        $arg["ITEMABBV"] = $item_abbv;
        $arg["UNIT"] = $item_unit;
        //総合判定コード
        //if ($item_cnt > 0) $item_key[99] = "999";

        //初期化
        $model->data=array();
        $counter = 0;
        $colorFlg = false;
        $disable = "disabled";

        //一覧表示
        $result = $db->query(knjj521Query::getList($model, $item_key));
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            //学籍番号を配列で取得
            $model->data["SCHREGNO"][] = $row["SCHREGNO"];

            //５行毎に背景色を変える
            if ($counter % 5 == 0) {
                $colorFlg = !$colorFlg;
            }
            $row["COLOR"] = $colorFlg ? "#ffffff" : "#cccccc";

            //除籍者は背景色を黄色に変える
            if ($row["JOSEKI"] == "yes") {
                $row["COLOR"] = "#ffff00";
            }

            //各問診項目を作成
            foreach ($inquiryCdArray as $inquiryCd) {
                //テキストボックスを作成
                $name = "INQUIRY".$inquiryCd;
                $extra = "STYLE=\"text-align: right\" onChange=\"this.style.background='#ccffcc'\" onblur=\"this.value=toInteger(this.value);\" onPaste=\"return showPaste(this);\"";
                $row[$name] = knjCreateTextBox($objForm, $row[$name], $name."-".$counter, 2, 1, $extra);
            }

            //各項目を作成
            foreach ($item_key as $lenNo => $itemCd) {
                //各コードを取得
                $model->data["RECORD"][$lenNo] = $itemCd;
                //テキストボックスを作成
                $name = "RECORD".$lenNo;
                $extra = "STYLE=\"text-align: right\" onChange=\"this.style.background='#ccffcc'\" onblur=\"calc(this);\" onPaste=\"return showPaste(this);\"";
                $value = ($row[$name] != "") ? sprintf("%.1f", $row[$name]) : "";
                $row[$name] = knjCreateTextBox($objForm, $value, $name."-".$counter, 5, 5, $extra);
            }

            //総合計
            $name = "TOTAL";
            $extra = "STYLE=\"text-align: center\" onChange=\"this.style.background='#ccffcc'\" onblur=\"this.value=toInteger(this.value);\" onPaste=\"return showPaste(this);\"";
            $row[$name] = knjCreateTextBox($objForm, $row[$name], $name."-".$counter, 3, 2, $extra);

            //総合判定
            $name = "VALUE";
            $extra = "STYLE=\"text-align: center\" onChange=\"this.style.background='#ccffcc'\" onblur=\"calc(this);\" onPaste=\"return showPaste(this);\"";
            $row[$name] = knjCreateTextBox($objForm, $row[$name], $name."-".$counter, 3, 1, $extra);

            //性別(算出時使用)
            knjCreateHidden($objForm, "SEX-".$counter, $row["SEX"]);
            //誕生日(算出時使用)
            knjCreateHidden($objForm, "BIRTHDAY-".$counter, $row["BIRTHDAY"]);

            //更新ボタンのＯＮ／ＯＦＦ
            $disable = "";
            $counter++;
            $arg["data"][] = $row;
        }

        Query::dbCheckIn($db);

        //ボタン
        $extra = "onclick=\"return btn_submit('calc')\" ";
        $arg["btn_calc"] = knjCreateBtn($objForm, "btn_calc", "算 出", $extra);

        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_update",
                            "value"       => "更 新",
                            "extrahtml"   => "onclick=\"return btn_submit('update', '".$electdiv."');\"".$disable ) );
        $arg["btn_update"] = $objForm->ge("btn_update");

        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_reset",
                            "value"       => "取 消",
                            "extrahtml"   => "onclick=\"return btn_submit('reset');\"" ) );
        $arg["btn_reset"] = $objForm->ge("btn_reset");

        $objForm->ae( array("type" => "button",
                            "name"        => "btn_end",
                            "value"       => "終 了",
                            "extrahtml"   => "onclick=\"closeWin();\"" ) );
        $arg["btn_end"] = $objForm->ge("btn_end");

        $objForm->ae( array("type"      => "hidden",
                            "name"      => "DBNAME",
                            "value"      => DB_DATABASE ) );

        $objForm->ae( array("type"      => "hidden",
                            "name"      => "cmd") );

        $objForm->ae( array("type"      => "hidden",
                            "name"      => "PRGID",
                            "value"     => "KNJJ521" )  );

        $objForm->ae( array("type"      => "hidden",
                            "name"      => "CTRL_Y",
                            "value"     => CTRL_YEAR )  );

        $objForm->ae( array("type"      => "hidden",
                            "name"      => "SEMESTER2",
                            "value"     => $model->field["SEMESTER2"] )  );

        $objForm->ae( array("type"      => "hidden",
                            "name"      => "CTRL_D",
                            "value"     => $execute_date )  );

        //クリップボードの中身のチェック用
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "ELECTDIV",
                            "value"     => $electdiv )  );

        $arg["finish"]  = $objForm->get_finish();
        View::toHTML($model, "knjj521Form1.html", $arg);
    }
}
//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $blank="")
{
    $opt = array();
    $value_flg = false;
    if($blank) $opt[] = array('label' => "", 'value' => "");
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {

        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);

        if ($value == $row["VALUE"]) $value_flg = true;
    }
    $result->free();
    if($name == "SEMESTER"){
        $value = ($value && $value_flg) ? $value : CTRL_SEMESTER;
    } else {
        $value = ($value && $value_flg) ? $value : $opt[0]["value"];
    }

    $arg[$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}
?>
