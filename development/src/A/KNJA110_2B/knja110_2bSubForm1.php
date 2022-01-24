<?php

require_once('for_php7.php');

class knja110_2bSubForm1 {
    function main(&$model) {
        $objForm = new form;
        $arg = array();
        //フォーム作成
        $arg["start"]   = $objForm->get_start("knja110_2bSubForm1", "POST", "knja110_2bindex.php", "", "knja110_2bSubForm1");

        //DB接続
        $db = Query::dbCheckOut();

        //変更開始日付
        //対象フレームは「parent」じゃないとうまく動作しない(フレームが多すぎ！)
        $arg["data"]["E_APPDATE"] = str_replace("'+getFrameName(self) + '", 'parent', View::popUpCalendar($objForm, "E_APPDATE",str_replace("-","/",CTRL_DATE),""));

        /**********/
        /* ボタン */
        /**********/
        //更新
        $extra = "onclick=\"return btn_submit('subUpdate');\"";
        $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);
        //終了
        $extra = "onclick=\"return btn_submit('subEnd');\"";
        $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);

        $checkArray["RELATIONSHIP_FLG"]    = '';
        $checkArray["GUARD_NAME_FLG"]      = '';
        $checkArray["GUARD_KANA_FLG"]      = '';
        $checkArray["GUARD_REAL_NAME_FLG"] = '';
        $checkArray["GUARD_REAL_KANA_FLG"] = '';
        $checkArray["GUARD_SEX_FLG"]       = '';
        $checkArray["GUARD_BIRTHDAY_FLG"]  = '';

        $checked = array();
        foreach ($checkArray as $key => $val) {
            $extra = 1 == $val ? " checked " : "";
            $arg["dataCheck"][$key] = knjCreateCheckBox($objForm, $key, "1", "");
        }

        $query = knja110_2bQuery::get_last_expiredate($model, $model->schregno);
        $last_expiredate = $db->getOne($query);

        $query = knja110_2bQuery::get_ent_date($model->schregno);
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
        View::toHTML($model, "knja110_2bSubForm1.html", $arg);
    }
}
?>
