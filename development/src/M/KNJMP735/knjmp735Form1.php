<?php

require_once('for_php7.php');

class knjmp735Form1
{
    function main(&$model) {
        $objForm      = new form;
        $arg["start"] = $objForm->get_start("edit", "POST", "knjmp735index.php", "", "edit");
        //DB接続
        $db = Query::dbCheckOut();

        //日付
        $date = preg_split("/-/", CTRL_DATE);
        $arg["TODAY"] = $date[0]."年".$date[1]."月".$date[2]."日現在";

        //生徒データ表示
        if ($model->cmd == "search") {
            makeStudentInfo($objForm, $arg, $db, $model);
        }

        //ボタン作成
        makeBtn($objForm, $arg, $model, $db);

        //hidden
        makeHidden($objForm, $model);

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();
        $arg["IFRAME"] = View::setIframeJs();
        View::toHTML($model, "knjmp735Form1.html", $arg);
    }
}

//生徒データ表示
function makeStudentInfo(&$objForm, &$arg, $db, $model)
{
    //年組表示追加
    if ($model->search["HR_CLASS_HYOUJI_FLG"] === '1') {
        $arg["HR_CLASS_HYOUJI"] = "1";
    } else {
        $arg["NOT_HR_CLASS_HYOUJI"] = "1";
    }
    $query = knjmp735Query::getStudentInfoData($model);

    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {

        $dis_paid = "";
        if ($row["PAID_MONEY"] > 0) {
            $dis_paid = "disabled ";
        }
        $extra  = ($row["PAID_MONEY"] > 0) ? "checked" : "";
        $data_paid = $row["SLIP_NO"].":".$row["SCHREGNO"];
        $row["PAID_FLG"] = knjCreateCheckBox($objForm, "PAID_FLG[]", $data_paid, $dis_paid.$extra, "");

        $row["MONEY_DUE"] = number_format($row["MONEY_DUE"]);

        $arg["data"][] = $row;
    }
    $result->free();
}

//ボタン作成
function makeBtn(&$objForm, &$arg, $model, $db)
{

    $extra = $disabled."onclick=\"return btn_submit('update');\"";
    $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);
    //終了
    $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", "onclick=\"closeWin();\"");
}

//Hidden作成
function makeHidden(&$objForm, $model)
{
    knjCreateHidden($objForm, "cmd");
}

?>
