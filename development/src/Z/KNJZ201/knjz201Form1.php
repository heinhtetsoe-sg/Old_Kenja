<?php

require_once('for_php7.php');

class knjz201Form1 {
    function main(&$model) {
        $db = Query::dbCheckOut();
        $objForm = new form;
        $arg["start"]   = $objForm->get_start("main", "POST", "knjz201index.php", "", "main");

        //権限チェック:更新可
        if (AUTHORITY != DEF_UPDATABLE) {
            $arg["jscript"] = "OnAuthError();";
        }

        //年度学期表示
        $arg["YEAR"] = CTRL_YEAR;

        /********************/
        /* チェックボックス */
        /********************/
        $extra = ($model->field["ABSENCE_WARN_CHECK"] == "on") ? "checked='checked' " : "";
        $arg["data"]["ABSENCE_WARN_CHECK"] = knjCreateCheckBox($objForm, "ABSENCE_WARN_CHECK", "on", $extra);

        $extra = ($model->field["ABSENCE_WARN_CHECK2"] == "on") ? "checked='checked' " : "";
        $arg["data"]["ABSENCE_WARN_CHECK2"] = knjCreateCheckBox($objForm, "ABSENCE_WARN_CHECK2", "on", $extra);

        $extra = ($model->field["ABSENCE_WARN_CHECK3"] == "on") ? "checked='checked' " : "";
        $arg["data"]["ABSENCE_WARN_CHECK3"] = knjCreateCheckBox($objForm, "ABSENCE_WARN_CHECK3", "on", $extra);

        /************/
        /* 固定文字 */
        /************/
        //欠課数オーバーのタイトル
        if (in_array("1", $model->control["SEMESTER"])) {
            $arg["title"]["ABSENCE_WARN"]  = $model->control["学期名"]["1"];
        }
        if (in_array("2", $model->control["SEMESTER"])) {
            $arg["title"]["ABSENCE_WARN2"] = $model->control["学期名"]["2"];
        }
        if (in_array("3", $model->control["SEMESTER"])) {
            $arg["title"]["ABSENCE_WARN3"] = $model->control["学期名"]["3"];
        }

        /********************/
        /* テキストボックス */
        /********************/
        //欠課数オーバ
        if (in_array("1", $model->control["SEMESTER"])) {
            $extra = "onblur=\"this.value=toInteger(this.value)\"";
            $arg["data"]["ABSENCE_WARN"] = knjCreateTextBox($objForm, $model->field["ABSENCE_WARN"], "ABSENCE_WARN", 2, 2, $extra);
        }
        if (in_array("2", $model->control["SEMESTER"])) {
            $extra = "onblur=\"this.value=toInteger(this.value)\"";
            $arg["data"]["ABSENCE_WARN2"] = knjCreateTextBox($objForm, $model->field["ABSENCE_WARN2"], "ABSENCE_WARN2", 2, 2, $extra);
        }
        if (in_array("3", $model->control["SEMESTER"])) {
            $extra = "onblur=\"this.value=toInteger(this.value)\"";
            $arg["data"]["ABSENCE_WARN3"] = knjCreateTextBox($objForm, $model->field["ABSENCE_WARN3"], "ABSENCE_WARN3", 2, 2, $extra);
        }


        /**********/
        /* ボタン */
        /**********/
        //実行
        $extra = "onclick=\"return btn_submit('execute');\"";
        $arg["btn_exec"] = knjCreateBtn($objForm, "btn_exec", "実 行", $extra);
        //終了
        $extra = "onclick=\"closeWin();\"";
        $arg["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);

        /**********/
        /* hidden */
        /**********/
        knjCreateHidden($objForm, "cmd");
        $query = knjz201Query::countGetAbsenceHigh(); //既に修得/履修上限値が登録されているかチェックする
        $cnt = $db->getOne($query);
        $query = knjz201Query::countGetAbsenceHighSpecial(); //既に修得/履修上限値が登録されているかチェックする
        $cnt_special = $db->getOne($query);
        if ($cnt > 0 || $cnt_special > 0) {
            $exists_flg = 'aru';
        } else {
            $exists_flg = 'nai';
        }
        knjCreateHidden($objForm, "EXISTS_FLG", $exists_flg);

        $query = knjz201Query::countGetAbsenceWarn($model); //既に欠課数オーバーが登録されているかチェックする
        $cnt = $db->getOne($query);
        $query = knjz201Query::countGetAbsenceWarnSpecial($model); //既に欠課数オーバーが登録されているかチェックする
        $cnt_special = $db->getOne($query);
        if ($cnt > 0 || $cnt_special > 0) {
            $exists_flg = 'aru';
        } else {
            $exists_flg = 'nai';
        }
        knjCreateHidden($objForm, "ABSENCE_WARN_EXISTS_FLG", $exists_flg);

        $query = knjz201Query::checkDetailDat(); //SCHOOL_DETAIL_DATにデータが登録されているのかどうかをチェックする
        $v_school_row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        if (!is_array($v_school_row)) {
            $jugyou_jisu_flg = "touroku_sarete_nai";
        } else {
            if ($v_school_row["JUGYOU_JISU_FLG"]) {
                $jugyou_jisu_flg = $v_school_row["JUGYOU_JISU_FLG"];
            } else {
                $jugyou_jisu_flg = "flg_null";
            }
        }
        knjCreateHidden($objForm, "JUGYOU_JISU_FLG", $jugyou_jisu_flg);

        //3学期があるか？
        $exists_check3 = (in_array("3", $model->control["SEMESTER"])) ? "aru" : "nai";
        knjCreateHidden($objForm, "EXISTS_CHECK3", $exists_check3);


        $arg["finish"]  = $objForm->get_finish();
        Query::dbCheckIn($db);
        View::toHTML($model, "knjz201Form1.html", $arg);
    }
}
?>
