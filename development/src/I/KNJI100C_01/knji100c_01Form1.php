<?php

require_once('for_php7.php');

class knji100c_01Form1
{
    public function main(&$model)
    {

        //オブジェクト作成
        $objForm      = new form();

        //フォーム作成
        $arg["start"] = $objForm->get_start("edit", "POST", "knji100c_01index.php", "", "edit");

        //DB接続
        $db = Query::dbCheckOut();

        /********************************************************************************/
        /********************************************************************************/
        /*******        *****************************************************************/
        /******* 左半分 *****************************************************************/
        /*******        *****************************************************************/
        /********************************************************************************/
        /********************************************************************************/

        //課程学科コンボボックス
        $query = knji100c_01Query::getCourseMajor($model);
        $extra = "onChange=\"return btn_submit('edit')\"";
        makeCmb($objForm, $arg, $db, $query, $model->coursemajor, "COURSEMAJOR", $extra, 1, "BLANK");

        //学年コンボボックス
        $query = knji100c_01Query::getGrade($model);
        $extra = "onChange=\"return btn_submit('edit')\"";
        makeCmb($objForm, $arg, $db, $query, $model->grade, "GRADE", $extra, 1, "BLANK");

        //性別コンボボックス
        $query = knji100c_01Query::getSex($model);
        $extra = "onChange=\"return btn_submit('edit')\"";
        makeCmb($objForm, $arg, $db, $query, $model->sex, "SEX", $extra, 1, "BLANK");

        //年組コンボボックス
        $query = knji100c_01Query::getAuth($model);
        $extra = "onChange=\"return btn_submit('edit')\"";
        makeCmb($objForm, $arg, $db, $query, $model->grade_hr_class, "GRADE_HR_CLASS", $extra, 1, "BLANK");

        //異動対象日付作成
        $model->date = ($model->date == "") ? str_replace("-", "/", CTRL_DATE) : $model->date;
        $arg["data"]["DATE"] = View::popUpCalendar2($objForm, "DATE", $model->date, "", "btn_submit('edit')", "");

        $schregno = array();
        if (isset($model->selectdata_l)) {
            $schregno = explode(",", $model->selectdata_l);
        }

        $opt_right = $opt_left = array();
        if ($model->coursemajor || $model->grade || $model->sex || $model->grade_hr_class) {
            //対象外の生徒取得
            $opt_idou = array();
            $result = $db->query(knji100c_01Query::getSchnoIdou($model));
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $opt_idou[] = $row["SCHREGNO"];
            }
            $result->free();

            //リストtoリスト右
            $result = $db->query(knji100c_01Query::getStudent_right($model));
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                if (!in_array(substr($row["VALUE"], 9), $schregno)) {
                    $idou = (in_array(substr($row["VALUE"], 9), $opt_idou)) ? "●" : "　";
                    $opt_right[] = array('label' => $row["HR_NAME"].$idou.$row["ATTENDNO"].'番'.$idou.$row["NAME_SHOW"],
                                         'value' => $row["VALUE"]);
                }
            }
            $result->free();

            //リストtoリスト左
            if ($model->selectdata_l) {
                $result = $db->query(knji100c_01Query::getStudent_left($model));
                while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                    $idou = (in_array(substr($row["VALUE"], 9), $opt_idou)) ? "●" : "　";
                    $opt_left[] = array('label' => $row["HR_NAME"].$idou.$row["ATTENDNO"].'番'.$idou.$row["NAME_SHOW"], 'value' => $row["VALUE"]);
                }
                $result->free();
            }
        }

        //出力対象生徒一覧
        $extra = "multiple STYLE=\"WIDTH:100%;\" ondblclick=\"move1('right', 'l')\"";
        $arg["main_part"]["LEFT_PART_L"] = knjCreateCombo($objForm, "left_select_l", "", $opt_left, $extra, 30);
        //生徒一覧
        $extra = "multiple STYLE=\"WIDTH:100%;\" ondblclick=\"move1('left', 'l')\"";
        $arg["main_part"]["RIGHT_PART_L"] = knjCreateCombo($objForm, "right_select_l", "", $opt_right, $extra, 30);

        //全て追加
        $extra = "onclick=\"moves('left', 'l');\"";
        $arg["main_part"]["SEL_ADD_ALL_L"] = knjCreateBtn($objForm, "sel_add_all_l", "≪", $extra);
        //追加
        $extra = "onclick=\"move1('left', 'l');\"";
        $arg["main_part"]["SEL_ADD_L"] = knjCreateBtn($objForm, "sel_add_l", "＜", $extra);
        //削除
        $extra = "onclick=\"move1('right', 'l');\"";
        $arg["main_part"]["SEL_DEL_L"] = knjCreateBtn($objForm, "sel_del_l", "＞", $extra);
        //全て削除
        $extra = "onclick=\"moves('right', 'l');\"";
        $arg["main_part"]["SEL_DEL_ALL_L"] = knjCreateBtn($objForm, "sel_del_all_l", "≫", $extra);

        /********************************************************************************/
        /********************************************************************************/
        /*******        *****************************************************************/
        /******* 右半分 *****************************************************************/
        /*******        *****************************************************************/
        /********************************************************************************/
        /********************************************************************************/

        //サブシステムコンボボックス
        $query = knji100c_01Query::getSubSystem();
        $requestroot = REQUESTROOT;
        $extra = " onChange=\"Page_jumper('{$requestroot}', '{$model->selectSchoolKind}');\"";
        makeCmb($objForm, $arg, $db, $query, $model->subsystem, "SUBSYSTEM", $extra, 1, "BLANK");

        $opt_left = $opt_right = $item = $item_array = array();

        //項目一覧
        foreach ($model->item as $key => $val) {
            foreach ($val as $field => $label) {
                $item_array[$field] = array("order" => sprintf("%03d", $key), "label" => $label);
            }
        }

        if ($model->cmd != "edit") {
            //保存したフィードを取得
            $query = knji100c_01Query::getFieldSql($model);
            $result = $db->query($query);
            $model->selectdata_r = "";
            $sep = "";
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $model->selectdata_r .= $sep.$row["FIELD_NAME"];
                $sep = ",";
            }
            $result->free();
        }

        if (isset($model->selectdata_r)) {
            $item = explode(",", $model->selectdata_r);
        }

        //書き出し項目一覧
        if ($model->selectdata_r) {
            foreach ($item as $key) {
                $opt_left[] = array("label" => $item_array[$key]["label"], "value" => $item_array[$key]["order"]."-".$key);
            }
        }

        //項目一覧
        foreach ($model->item as $key => $val) {
            foreach ($val as $field => $label) {
                if (!in_array($field, $item)) {
                    $opt_right[] = array("label" => $label, "value" => sprintf("%03d", $key)."-".$field);
                }
            }
        }

        //書き出し項目一覧
        $extra = "multiple STYLE=\"WIDTH:100%;\" ondblclick=\"move1('right', 'r')\" ";
        $arg["main_part"]["LEFT_PART_R"] = knjCreateCombo($objForm, "left_select_r", "", $opt_left, $extra, 30);
        //項目一覧
        $extra = "multiple STYLE=\"WIDTH:100%;\" ondblclick=\"move1('left', 'r')\" ";
        $arg["main_part"]["RIGHT_PART_R"] = knjCreateCombo($objForm, "right_select_r", "", $opt_right, $extra, 30);

        //全て追加
        $extra = "onclick=\"moves('left', 'r');\"";
        $arg["main_part"]["SEL_ADD_ALL_R"] = knjCreateBtn($objForm, "sel_add_all_r", "≪", $extra);
        //追加
        $extra = "onclick=\"move1('left', 'r');\"";
        $arg["main_part"]["SEL_ADD_R"] = knjCreateBtn($objForm, "sel_add_r", "＜", $extra);
        //削除
        $extra = "onclick=\"move1('right', 'r');\"";
        $arg["main_part"]["SEL_DEL_R"] = knjCreateBtn($objForm, "sel_del_r", "＞", $extra);
        //全て削除
        $extra = "onclick=\"moves('right', 'r');\"";
        $arg["main_part"]["SEL_DEL_ALL_R"] = knjCreateBtn($objForm, "sel_del_all_r", "≫", $extra);

        /**********************************************************************************/
        /**********************************************************************************/
        /*******          *****************************************************************/
        /******* ここまで *****************************************************************/
        /*******          *****************************************************************/
        /**********************************************************************************/
        /**********************************************************************************/
        //出力設定ラジオボタン作成
        $output = array(OUT_CODE_NAME, OUT_CODE_ONLY, OUT_NAME_ONLY);
        $model->output = ($model->output== "") ? OUT_CODE_NAME : $model->output;
        foreach ($output as $key => $val) {
            $name = "RADIO".($key+1);
            $objForm->ae(array("type"       => "radio",
                                "name"       => "OUTPUT",
                                "value"      => $model->output,
                                "extrahtml"  => "id=".$name ));

            $arg[$name] = $objForm->ge("OUTPUT", $val);
        }

        //住所１チェックボックス
        $extra  = ($model->addr1_div) ? "checked " : "";
        $extra .= " id=\"ADDR1_DIV\"";
        $arg["ADDR1_DIV"] = knjCreateCheckBox($objForm, "ADDR1_DIV", "1", $extra, "");

        //氏名分割チェックボックス
        $extra  = ($model->name_div) ? "checked " : "";
        $extra .= " id=\"NAME_DIV\"";
        $arg["NAME_DIV"] = knjCreateCheckBox($objForm, "NAME_DIV", "1", $extra, "");

        //CSVボタン
        $extra = "onclick=\"return doSubmit();\"";
        $arg["button"]["BTN_CSV"] = knjCreateBtn($objForm, "btn_csv", "ＣＳＶ書出し", $extra);

        //終了ボタン
        $extra = "onclick=\"closeWin();\"";
        $arg["button"]["BTN_END"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);

        /**********/
        /* hidden */
        /**********/
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "selectdata_r");
        knjCreateHidden($objForm, "selectdata_l");
        knjCreateHidden($objForm, "selectdata");
        knjCreateHidden($objForm, "SCHREGNO");

        Query::dbCheckIn($db);
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knji100c_01Form1.html", $arg);
    }
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, &$value, $name, $extra, $size, $blank = "")
{
    $opt = array();
    $value_flg = false;
    if ($blank) {
        $opt[] = array("label" => "", "value" => "");
    }
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);
        if ($value == $row["VALUE"]) {
            $value_flg = true;
        }
    }
    $result->free();

    $value = ($value && $value_flg) ? $value : $opt[0]["value"];
    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}
