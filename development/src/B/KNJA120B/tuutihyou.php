<?php
class tuutihyou {
    function main(&$model) {
        $objForm = new form;
        $arg["start"] = $objForm->get_start("detail", "POST", "knja120bindex.php", "", "detail");

        //DB OPEN
        $db = Query::dbCheckOut();

        //生徒情報
        $arg["NAME_SHOW"] = $model->schregno."  :  ".$model->name;

        //年度コンボ
        $query = knja120bQuery::getYear($model->schregno);
        $extra = "onchange=\"return btn_submit('tuutihyou');\"";
        makeCmb($objForm, $arg, $db, $query, "YEAR", $model->year, $extra, 1, $model);

        //prgInfo.propertiesの使用項目
        $field_name_array = array('HREPORTREMARK_DAT__SPECIALACTREMARK',
                                  'HREPORTREMARK_DAT__TOTALSTUDYTIME',
                                  'HREPORTREMARK_DAT__COMMUNICATION',
                                  'HREPORTREMARK_DAT__REMARK1',
                                  'HREPORTREMARK_DAT__REMARK2',
                                  'HREPORTREMARK_DAT__REMARK3');

        //通知票所見
        $query = knja120bQuery::getRemark($model->schregno, $model->year);
        $result = $db->query($query);
        while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            $extra = "readonly style=\"height:145px;\"";
            //特別活動の記録
            $row["SPECIALACTREMARK"] = knjCreateTextArea($objForm, "SPECIALACTREMARK", 10, 21, "soft", $extra, $row["SPECIALACTREMARK"]);
            //総合的な学習の時間
            $row["TOTALSTUDYTIME"] = knjCreateTextArea($objForm, "TOTALSTUDYTIME", 10, 21, "soft", $extra, $row["TOTALSTUDYTIME"]);
            //通信欄
            $row["COMMUNICATION"] = knjCreateTextArea($objForm, "COMMUNICATION", 10, 71, "soft", $extra, $row["COMMUNICATION"]);
            //備考１
            $row["REMARK1"] = knjCreateTextArea($objForm, "REMARK1", 10, 21, "soft", $extra, $row["REMARK1"]);
            //備考２
            $row["REMARK2"] = knjCreateTextArea($objForm, "REMARK2", 10, 21, "soft", $extra, $row["REMARK2"]);
            //備考３
            $row["REMARK3"] = knjCreateTextArea($objForm, "REMARK3", 10, 21, "soft", $extra, $row["REMARK3"]);

            $semester = $db->getone(knja120bQuery::getSemester($model->year, $row["SEMESTER"]));

            $sub_arg = array();
            $sub_arg["datas"][] = array("DATA" => $semester);
            foreach ($model->Properties as $key => $val) {
                if (in_array($key, $field_name_array) && strlen($val)) {
                    list($table_name, $filed_name) = split('__', $key);
                    $sub_arg["datas"][] = array("DATA" => $row[$filed_name]);
                }
            }
            $arg["data_array"][] = $sub_arg;
        }
        $colspan_count = 0;
        //項目名表示
        foreach ($model->Properties as $key => $val) {
            if (in_array($key, $field_name_array) && strlen($val)) {
                list($table_name, $filed_name) = split('__', $key);
                $arg["LABELS"][] = array("LABEL" => $val);
                $colspan_count++;
            }
        }
        $arg["colspan"] = $colspan_count;

        //戻るボタンを作成する
        $extra = "onclick=\"return parent.closeit()\"";
        $arg["btn_back"] = knjCreateBtn($objForm, "btn_back", "戻 る", $extra);

        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
        knjCreateHidden($objForm, "PRGID", "KNJA120B");
        knjCreateHidden($objForm, "SCHREGNO", "$model->schregno");

        $arg["finish"] = $objForm->get_finish();

        //テンプレートのHTMLを$arg経由で渡す
        View::toHTML($model, "tuutihyou.html", $arg);
    }
}
/********************************************** 以下関数 ******************************************************/
//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $model) {
    $opt = array();
    $value_flg = false;
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {

        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);

        if ($value == $row["VALUE"]) $value_flg = true;
    }
    $result->free();

    if($name == "YEAR"){
        $value = ($value && $value_flg) ? $value : $model->exp_year;
    } else {
        $value = ($value && $value_flg) ? $value : $opt[0]["value"];
    }

    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}

?>