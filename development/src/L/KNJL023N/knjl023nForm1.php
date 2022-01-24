<?php

require_once('for_php7.php');

class knjl023nForm1
{
    function main(&$model)
    {
        $objForm      = new form;
        $arg["start"] = $objForm->get_start("main", "POST", "knjl023nindex.php", "", "main");

        //一覧表示
        if (!isset($model->warning)) {
            //データを取得
            $Row = knjl023nQuery::get_edit_data($model);
        } else {
            $Row =& $model->field;
        }

        //DB接続
        $db = Query::dbCheckOut();

        //入試年度
        $arg["TOP"]["YEAR"] = $model->year;

        //受験番号
        $arg["TOP"]["EXAMNO"] = $model->examno;

        //氏名(志願者)
        $arg["TOP"]["NAME"] = $Row["NAME"];

        //入試制度
        $applicantdiv_name = $db->getOne(knjl023nQuery::get_name_cd($model->year, "L003", $Row["APPLICANTDIV"]));
        $arg["data"]["APPLICANTDIV"] = $applicantdiv_name;

        //入試区分
        $test_name = $db->getOne(knjl023nQuery::get_name_cd($model->year, "L004", $Row["TESTDIV"]));
        $arg["data"]["TESTDIV"] = $test_name;

        //状態
        $arg["data"]["JUDGEMENT_INFO"] = $Row["JUDGEMENT_INFO"];

        //更新ボタンをグレーアウトフラグ ※合格者は更新不可です。1:合格 3:まわし合格
        $disBtn = ($Row["JUDGEMENT"] == "1" || $Row["JUDGEMENT"] == "3") ? " disabled" : "";

        //4:欠席・5:辞退コンボ
        $extra = "";
        $query = knjl023nQuery::get_name_cd($model->year, "L013");
        makeCmb($objForm, $arg, $db, $query, $Row["JUDGEMENT"], "JUDGEMENT", $extra, 1, "BLANK");

        //更新ボタン
        $extra = "onclick=\"return btn_submit('update');\"" .$disBtn;
        $arg["button"]["btn_udpate"] = knjCreateBtn($objForm, "btn_udpate", "更 新", $extra);

        //取消ボタン
        $extra = "onclick=\"return btn_submit('reset');\"";
        $arg["button"]["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);

        //戻るボタン
        $link = REQUESTROOT."/L/KNJL011N/knjl011nindex.php?cmd=reference&SEND_PRGID=KNJL023N&SEND_AUTH=".$model->auth."&SEND_APPLICANTDIV=".$model->applicantdiv."&SEND_EXAMNO=".$model->examno;
        $extra = "onClick=\" Page_jumper('{$link}');\"";
        $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "戻 る", $extra);

        //hidden
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "year", $model->year);

        //インラインフレーム用Javascriptタグ生成
        $arg["IFRAME"] = View::setIframeJs();

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();
        View::toHTML($model, "knjl023nForm1.html", $arg);
    }
}
//makeCmb
function makeCmb(&$objForm, &$arg, $db, $query, &$value, $name, $extra, $size, $blank = "") {
    $opt = array();
    if ($blank == "BLANK") {
        $opt[] = array("label" => "", "value" => "");
    }
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);
        if ($value == $row["VALUE"]) $value_flg = true;
    }
    $result->free();
    $value = ($value && $value_flg) ? $value : $opt[0]["value"];
    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}
?>