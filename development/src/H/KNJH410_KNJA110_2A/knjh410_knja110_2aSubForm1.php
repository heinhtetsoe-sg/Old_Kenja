<?php

require_once('for_php7.php');

class knjh410_knja110_2aSubForm1 {
    function main(&$model) {
        $objForm = new form;
        $arg = array();
        //フォーム作成
        $arg["start"]   = $objForm->get_start("knjh410_knja110_2aSubForm1", "POST", "knjh410_knja110_2aindex.php", "", "knjh410_knja110_2aSubForm1");

        //DB接続
        $db = Query::dbCheckOut();

        //変更開始日付
        //対象フレームは「parent」じゃないとうまく動作しない(フレームが多すぎ！)
        $arg["data"]["E_APPDATE"] = str_replace("'+getFrameName(self) + '", 'parent', View::popUpCalendar($objForm, "E_APPDATE",str_replace("-","/",CTRL_DATE),""));

        /**********/
        /* ボタン */
        /**********/
        //更新
        $cmd = ($model->infoDiv == "4") ? "subUpdate2" : "subUpdate";
        $extra = "onclick=\"return btn_submit('".$cmd."');\"";
        $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);
        //終了
        $extra = "onclick=\"return btn_submit('subEnd');\"";
        $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);

        /********************/
        /* チェックボックス */
        /********************/
        $checkArray = array();
        if ($model->infoDiv == "4") {
            //保証人
            $checkArray["GUARANTOR_RELATIONSHIP_FLG"]   = '';
            $checkArray["GUARANTOR_NAME_FLG"]           = '';
            $checkArray["GUARANTOR_KANA_FLG"]           = '';
            $checkArray["GUARANTOR_REAL_NAME_FLG"]      = '';
            $checkArray["GUARANTOR_REAL_KANA_FLG"]      = '';
            $checkArray["GUARANTOR_SEX_FLG"]            = '';
            $arg["infoDiv4"] = 1;
        } else {
            //保護者
            $checkArray["RELATIONSHIP_FLG"]    = '';
            $checkArray["GUARD_NAME_FLG"]      = '';
            $checkArray["GUARD_KANA_FLG"]      = '';
            $checkArray["GUARD_REAL_NAME_FLG"] = '';
            $checkArray["GUARD_REAL_KANA_FLG"] = '';
            $checkArray["GUARD_SEX_FLG"]       = '';
            $checkArray["GUARD_BIRTHDAY_FLG"]  = '';
            $arg["infoDiv2"] = 1;
        }
        $checked = array();
        foreach ($checkArray as $key => $val) {
            $extra  = (1 == $val) ? " checked " : "";
            $extra .= " id=\"{$key}\"";
            $arg["dataCheck"][$key] = knjCreateCheckBox($objForm, $key, "1", $extra, "");
        }

        $query = knjh410_knja110_2aQuery::get_last_expiredate($model, $model->schregno);
        $last_expiredate = $db->getOne($query);

        $query = knjh410_knja110_2aQuery::get_ent_date($model->schregno);
        $ent_date = $db->getOne($query);
        if (strlen($last_expiredate)) {
            $last_day = str_replace("-", "/", $last_expiredate);
            $last_day = date("Y/m/d",strtotime("1 day" ,strtotime($last_day)));
        } elseif (strlen($ent_date)) {
            $last_day = str_replace("-", "/", $ent_date);
            $last_day = date("Y/m/d",strtotime($last_day));
        } else {
            $last_day = CTRL_YEAR.'/04/01';
            $last_day = date("Y/m/d",strtotime($last_day));
        }

        /**********/
        /* hidden */
        /**********/
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "TODAY", str_replace("-","/", CTRL_DATE));
        knjCreateHidden($objForm, "LAST_DAY", $last_day);

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjh410_knja110_2aSubForm1.html", $arg);
    }
}
?>
