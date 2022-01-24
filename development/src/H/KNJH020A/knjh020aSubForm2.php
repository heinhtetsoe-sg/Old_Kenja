<?php

require_once('for_php7.php');

class knjh020aSubForm2
{
    public function main(&$model)
    {
        $objForm = new form();
        $arg = array();
        //フォーム作成
        $arg["start"]   = $objForm->get_start("knjh020aSubForm2", "POST", "knjh020aindex.php", "", "knjh020aSubForm2");

        //DB接続
        $db = Query::dbCheckOut();

        //保護者変更開始日付
        //対象フレームは「parent」じゃないとうまく動作しない(フレームが多すぎ！)
        $arg["data"]["E_APPDATE"] = str_replace("'+getFrameName(self) + '", 'parent', View::popUpCalendar($objForm, "E_APPDATE", str_replace("-", "/", CTRL_DATE), ""));

        //保証人変更開始日付
        //対象フレームは「parent」じゃないとうまく動作しない(フレームが多すぎ！)
        $arg["data"]["E_APPDATE2"] = str_replace("'+getFrameName(self) + '", 'parent', View::popUpCalendar($objForm, "E_APPDATE2", str_replace("-", "/", CTRL_DATE), ""));

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

        $checkArray["GUARANTOR_RELATIONSHIP_FLG"]    = '';
        $checkArray["GUARANTOR_NAME_FLG"]      = '';
        $checkArray["GUARANTOR_KANA_FLG"]      = '';
        $checkArray["GUARANTOR_REAL_NAME_FLG"] = '';
        $checkArray["GUARANTOR_REAL_KANA_FLG"] = '';
        $checkArray["GUARANTOR_SEX_FLG"]       = '';

        $checked = array();
        foreach ($checkArray as $key => $val) {
            $extra = 1 == $val ? " checked " : "";
            $arg["dataCheck"][$key] = knjCreateCheckBox($objForm, $key, "1", "");
        }

        $query = knjh020aQuery::getLastExpireDate($model->schregno);
        $last_expiredate = $db->getOne($query);

        $query = knjh020aQuery::getLastExpireDate2($model->schregno);
        $last_expiredate2 = $db->getOne($query);

        $query = knjh020aQuery::getEntDate($model->schregno);
        $ent_date = $db->getOne($query);
        if (strlen($last_expiredate)) {
            $last_day = str_replace("-", "/", $last_expiredate);
            $last_day = date("Y/m/d", strtotime("1 day", strtotime($last_day)));
        } elseif (strlen($ent_date)) {
            $last_day = str_replace("-", "/", $ent_date);
            $last_day = date("Y/m/d", strtotime($last_day));
        } else {
            $last_day = CTRL_YEAR.'/04/01';
            $last_day = date("Y/m/d", strtotime($last_day));
        }

        if (strlen($last_expiredate2)) {
            $last_day2 = str_replace("-", "/", $last_expiredate2);
            $last_day2 = date("Y/m/d", strtotime("1 day", strtotime($last_day2)));
        } elseif (strlen($ent_date)) {
            $last_day2 = str_replace("-", "/", $ent_date);
            $last_day2 = date("Y/m/d", strtotime($last_day2));
        } else {
            $last_day2 = CTRL_YEAR.'/04/01';
            $last_day2 = date("Y/m/d", strtotime($last_day2));
        }

        /**********/
        /* hidden */
        /**********/
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "TODAY", str_replace("-", "/", CTRL_DATE));
        knjCreateHidden($objForm, "LAST_DAY", $last_day);
        knjCreateHidden($objForm, "LAST_DAY2", $last_day2);

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjh020aSubForm2.html", $arg);
    }
}
