<?php

require_once('for_php7.php');

//変更開始日を入力する画面(ポップアップ)
class knja050SubForm1
{
    public function main(&$model)
    {
        $objForm = new form();
        $arg = array();

        //DB接続
        $db = Query::dbCheckOut();
        
        //変更開始日付
        $arg["data"]["E_APPDATE"] = View::popUpCalendar($objForm, "E_APPDATE", str_replace("-", "/", CTRL_DATE), "");

        //ボタン作成
        makeBtn($objForm, $arg, $model);

        $schregnos = explode(',', $model->schregno);

        //学籍履歴データ取得
        $query = knja050Query::getSchregNoForHistDat($model);
        $result = $db->query($query);
        $sch_eApp_ent = array();
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $query = knja050Query::getEappDate($row["SCHREGNO"], $model);
            $sch_eApp_ent[] = $db->getRow($query, DB_FETCHMODE_ASSOC);
        }

        //hidden
        makeHidden($objForm, $model, $sch_eApp_ent);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム作成
        $arg["start"]   = $objForm->get_start("knja050SubForm1", "POST", "knja050index.php", "", "knja050SubForm1");
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knja050SubForm1.html", $arg);
    }
}
/************************************************** 以下関数 **************************************************/
//ボタン作成
function makeBtn(&$objForm, &$arg, $model)
{
    //更新
    $extra = "onclick=\"return btn_submit('subExecute');\"";
    $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);

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
        if ($sch_eApp_ent[$i]["E_APPDATE"]) {
            list($year, $month, $day) = preg_split("/-/", $sch_eApp_ent[$i]["E_APPDATE"]);
            $s_appdate = date("Y/m/d", mktime(0, 0, 0, $month, (int)$day+2, $year));
            knjCreateHidden($objForm, "S_APPDATE{$i}", $s_appdate);
        } elseif ($sch_eApp_ent[$i]["ENT_DATE"]) {
            list($year, $month, $day) = preg_split("/-/", $sch_eApp_ent[$i]["ENT_DATE"]);
            $ent_date = date("Y/m/d", mktime(0, 0, 0, $month, (int)$day+1, $year));
            knjCreateHidden($objForm, "S_APPDATE{$i}", str_replace("-", "/", $ent_date));
        } else {
            knjCreateHidden($objForm, "S_APPDATE{$i}", "");
        }
    }
    knjCreateHidden($objForm, "COUNT", $i);
}
