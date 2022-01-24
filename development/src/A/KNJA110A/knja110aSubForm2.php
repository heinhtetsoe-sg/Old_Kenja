<?php

require_once('for_php7.php');


class knja110aSubForm2
{
    public function main(&$model)
    {
        $objForm = new form();
        $arg = array();
        //フォーム作成
        $arg["start"]   = $objForm->get_start("knja110aSubForm2", "POST", "knja110aindex.php", "", "knja110aSubForm2");

        //変更開始日付
        $arg["data"]["E_APPDATE"] = View::popUpCalendar($objForm, "E_APPDATE", str_replace("-", "/", CTRL_DATE), "");

        //DB接続
        $db = Query::dbCheckOut();

        //ボタン作成
        makeBtn($objForm, $arg, $model);

        $schregnos = explode(',', $model->schregno_flg);

        //学籍履歴データ取得
        $last_day = CTRL_YEAR.'/04/01';
        for ($i = 0; $i < get_count($schregnos); $i++) {
            //入学日付と、最終有効期間を取得 (おそらく次の履歴の開始日付を作成するためと思われる)
            $query = knja110aQuery::getEappDate($schregnos[$i]);
            $sch_eApp_ent[] = $db->getRow($query, DB_FETCHMODE_ASSOC);

            //更新するときに履歴に入れるデータ
            $query = knja110aQuery::getStudentDataBefore($model, $schregnos[$i]);
            $histArray[] = $db->getRow($query, DB_FETCHMODE_ASSOC); //今までのデータ

            //履歴の最終日
            $last_hist = CTRL_YEAR.'/04/01';
            $query = knja110aQuery::getLastHistExpiredate($schregnos[$i]);
            $last_hist_expiredate = $db->getOne($query);
            $last_hist_expiredate = str_replace("-", "/", $last_hist_expiredate);
            if ($last_hist_expiredate > $last_hist) {
                $last_hist = $last_hist_expiredate;
            }

            //入学日付
            $query = knja110aQuery::getEntDate($schregnos[$i]);
            $ent_date = $db->getOne($query);
            if ($ent_date > $last_ent) {
                $last_ent = str_replace("-", "/", $ent_date);
            }
        }

        $last_hist = str_replace("-", "/", $last_hist);
        $last_hist = date("Y/m/d", strtotime("1 day", strtotime($last_hist)));

        if ($last_ent > $last_hist) {
            $last_day = $last_ent;
        } else {
            $last_day = $last_hist;
        }

        if (!$last_day) {
            $last_day = CTRL_YEAR.'/04/01';
        }
        //入力日付の確認に使う
        knjCreateHidden($objForm, "LAST_DAY", $last_day);

        $checkArray["GRADE_FLG"]                      = $model->subFrm["GRADE_FLG"];
        $checkArray["HR_CLASS_FLG"]                   = $model->subFrm["HR_CLASS_FLG"];
        $checkArray["ATTENDNO_FLG"]                   = $model->subFrm["ATTENDNO_FLG"];
        $checkArray["ANNUAL_FLG"]                     = $model->subFrm["ANNUAL_FLG"];
        $checkArray["COURSECD_FLG"]                   = $model->subFrm["COURSECD_FLG"];
        $checkArray["MAJORCD_FLG"]                    = $model->subFrm["MAJORCD_FLG"];
        $checkArray["COURSECODE_FLG"]                 = $model->subFrm["COURSECODE_FLG"];
        $checkArray["NAME_FLG"]                       = $model->subFrm["NAME_FLG"];
        $checkArray["NAME_SHOW_FLG"]                  = $model->subFrm["NAME_SHOW_FLG"];
        $checkArray["NAME_KANA_FLG"]                  = $model->subFrm["NAME_KANA_FLG"];
        $checkArray["NAME_ENG_FLG"]                   = $model->subFrm["NAME_ENG_FLG"];
        $checkArray["REAL_NAME_FLG"]                  = $model->subFrm["REAL_NAME_FLG"];
        $checkArray["REAL_NAME_KANA_FLG"]             = $model->subFrm["REAL_NAME_KANA_FLG"];
        $checkArray["HANDICAP_FLG"]                   = $model->subFrm["HANDICAP_FLG"];
        $checkArray["NATIONALITY2_FLG"]               = $model->subFrm["NATIONALITY2_FLG"];
        $checkArray["NATIONALITY_NAME_FLG"]           = $model->subFrm["NATIONALITY_NAME_FLG"];
        $checkArray["NATIONALITY_NAME_KANA_FLG"]      = $model->subFrm["NATIONALITY_NAME_KANA_FLG"];
        $checkArray["NATIONALITY_NAME_ENG_FLG"]       = $model->subFrm["NATIONALITY_NAME_ENG_FLG"];
        $checkArray["NATIONALITY_REAL_NAME_FLG"]      = $model->subFrm["NATIONALITY_REAL_NAME_FLG"];
        $checkArray["NATIONALITY_REAL_NAME_KANA_FLG"] = $model->subFrm["NATIONALITY_REAL_NAME_KANA_FLG"];

        $checked = array();
        foreach ($checkArray as $key => $val) {
            $extra = 1 == $val ? " checked " : "";
            $arg["dataCheck"][$key] = knjCreateCheckBox($objForm, $key, "1", "");
        }

        //hidden
        makeHidden($objForm, $model, $sch_eApp_ent);

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knja110aSubForm2.html", $arg);
    }
}

//ボタン作成
function makeBtn(&$objForm, &$arg, $model)
{
    //更新
    if ($model->cmd == 'subReplaceForm2') { //一括更新画面より呼び出されたとき
        $extra = "onclick=\"return btn_submit('subReplace_update');\"";
        $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);
    } else { //今の所一括更新以外は普通の画面だけ
        $extra = "onclick=\"return btn_submit('subUpdate');\"";
        $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);
    }

    //終了
    $extra = "onclick=\"return btn_submit('subEnd');\"";
    $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);
}

//Hidden作成
function makeHidden(&$objForm, $model, $sch_eApp_ent)
{
    knjCreateHidden($objForm, "cmd");
    knjCreateHidden($objForm, "TODAY", str_replace("-", "/", CTRL_DATE));

    for ($i = 0; $i < get_count($sch_eApp_ent); $i++) {
        knjCreateHidden($objForm, "SCHREGNO{$i}", $sch_eApp_ent[$i]["SCHREGNO"]);

        if ($sch_eApp_ent[$i]["EXPIREDATE"]) {
            list($year, $month, $day) = preg_split("/-/", $sch_eApp_ent[$i]["EXPIREDATE"]);
            $s_appdate = date("Y/m/d", mktime(0, 0, 0, $month, $day+2, $year));
            knjCreateHidden($objForm, "S_APPDATE{$i}", $s_appdate);
        } else {
            list($year, $month, $day) = preg_split("/-/", $sch_eApp_ent[$i]["ENT_DATE"]);
            $ent_date = date("Y/m/d", mktime(0, 0, 0, $month, $day+1, $year));
            knjCreateHidden($objForm, "S_APPDATE{$i}", str_replace("-", "/", $ent_date));
        }
    }
    knjCreateHidden($objForm, "COUNT", $i);
}
