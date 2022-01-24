<?php

require_once('for_php7.php');

class knjc010aSchChrList
{
    public function main(&$model)
    {
        $objForm = new form();
        //フォーム作成
        $arg["start"]   = $objForm->get_start("edit", "POST", "knjc010aindex.php", "", "edit");

        /* データベース接続 */
        $db = Query::dbCheckOut();

        /* ログイン職員 */
        $query = knjc010aQuery::getStaff();
        $tStaff = $db->getOne($query);
        $arg["TITLE_STAFF_NAME"] = $tStaff;

        // 日付
        $model->schChrExecuteDate = $model->schChrExecuteDate ? $model->schChrExecuteDate : CTRL_DATE;
        $extra = "extra=dateChange(f.document.forms[0][\\'SCH_CHR_EXECUTEDATE\\'].value);";
        $arg["TITLE_EXECUTEDATE"] = View::popUpCalendar($objForm, "SCH_CHR_EXECUTEDATE", str_replace("-", "/", $model->schChrExecuteDate), $extra);

        // 出欠制御日時
        $tDate = $model->attndCntlDt;
        $tWeekArray = array("日", "月", "火", "水", "木", "金", "土");
        $tTime = strtotime($tDate);
        $tWeek = date("w", $tTime);
        $arg["TITLE_ATTEND_CTRL_DATE"] = (str_replace("-", "/", $tDate))."({$tWeekArray[$tWeek]})";

        // データ取得
        $query = knjc010aQuery::getSchChrList($model, $model->schChrExecuteDate);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $row["HR_CLASS"] = "";
            $sep = "";
            $query = knjc010aQuery::getChrHrClassList($model, $row["CHAIRCD"]);
            $result2 = $db->query($query);
            while ($row2 = $result2->fetchRow(DB_FETCHMODE_ASSOC)) {
                $row["HR_CLASS"] .= $sep.$row2["HR_NAME"];
                $sep = ",";
            }
            $arg["data"][] = $row;
        }
        $result->free();

        //更新ボタン
        $extra = "onclick=\"return btn_submit('schChrSelect');\"";
        $arg["button"]["btn_select"] = knjCreateBtn($objForm, "btn_select", "選 択", $extra);
        //終了ボタン
        $extra = "onclick=\"closeMethod();\"";
        $arg["button"]["btn_back"] = knjCreateBtn($objForm, "btn_back", "戻 る", $extra);

        makeHidden($objForm, $arg, $model);

        /* データベース接続切断 */
        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjc010aSchChrList.html", $arg);
    }
}
//makeCmb
function makeCmb(&$objForm, &$arg, $db, $query, &$value, $name, $extra, $size, $blank = "")
{
    $opt = array();
    if ($blank == "BLANK") {
        $opt[] = array("label" => "", "value" => "");
    }
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"], 'value' => $row["VALUE"]);
        if ($value === $row["VALUE"]) {
            $value_flg = true;
        }
    }
    $value = ($value != "" && $value_flg) ? $value : $opt[0]["value"];
    return knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}

function makeHidden(&$objForm, &$arg, $model)
{

    //hidden
    knjCreateHidden($objForm, "cmd");
    knjCreateHidden($objForm, "SCH_CHR_PERIODCD");
    knjCreateHidden($objForm, "SCH_CHR_CHAIRCD");
}
