<?php

require_once('for_php7.php');

class knjc010aSyousai
{
    public function main(&$model)
    {
        $objForm = new form();
        //フォーム作成
        $arg["start"]   = $objForm->get_start("edit", "POST", "knjc010aindex.php", "", "edit");

        /* データベース接続 */
        $db = Query::dbCheckOut();

        $query = knjc010aQuery::getSchregInfo($model);
        $schInfo = $db->getRow($query, DB_FETCHMODE_ASSOC);

        $arg["HR_ATTENDNO"] = $schInfo["HR_NAME"]."-".$schInfo["ATTENDNO"]."番";
        $arg["NAME"] = $schInfo["NAME"];

        $schregs = preg_split("/,/", $model->syousaiHiddenSchreg);
        foreach ($schregs as $schPeri) {
            list($schregNo, $period) = preg_split("/_/", str_replace("SCH", "", $schPeri));

            $schregNos = "('".$schregNo."')";
            $query = knjc010aQuery::getSchChrInfo($model, $schregNos);
            $schChrInfos = array();
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $schChrInfos[$row["PERIODCD"]] = $row;
            }
            $result->free();

            $petition = array();
            $query = knjc010aQuery::getPetition($model, $schregNos);
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $petition[$row["PERIODCD"]]["DI_REMARK"] = $row["DI_REMARK"];
            }

            if ($model->field["SYOUSAI_SCHREGNO"] == $schregNo) {
                $query = knjc010aQuery::getExistSchChr($model, $schregNo, $period);
                $schChrCnt = $db->getOne($query);
                if ($schChrCnt == 0 || !in_array($period, $model->renZoku)) {
                    continue;
                }

                $query = knjc010aQuery::getB001Syousai($period);
                $periName = $db->getOne($query);
                $setSyouSai["SYOUSAI_PERIOD"] = $periName;

                $query = knjc010aQuery::getRemarkSyousai($model, $schregNo, $period);
                $remarkVal = $db->getRow($query, DB_FETCHMODE_ASSOC);
                //makeCmb2
                $query = knjc010aQuery::getC901Syousai();
                $extra = "";
                $setSyouSai["SYOUSAI_REMARK_CD"] = makeCmb($objForm, $arg, $db, $query, $remarkVal["DI_REMARK_CD"], "SYOUSAI_REMARK_CD_{$period}", $extra, 1, "BLANK");
                //textbox
                $extra = "";
                if ($remarkVal["DI_REMARK"] == '' && $petition[$period]["DI_REMARK"] != '' && $schChrInfo[$period]["EXECUTED"] != '1') {
                    $remarkVal["DI_REMARK"] = $petition[$period]["DI_REMARK"];
                    $extra .= " style=\"color: red\" ";
                }
                $setSyouSai["SYOUSAI_REMARK"] = knjCreateTextBox($objForm, $remarkVal["DI_REMARK"], "SYOUSAI_REMARK_{$period}", 40, 40, $extra);

                $arg["data"][] = $setSyouSai;
            }
        }

        //更新ボタン
        $extra = "onclick=\"return btn_submit('updateSyouSai');\"";
        $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);
        //終了ボタン
        $extra = "onclick=\"closeMethod();\"";
        $arg["button"]["btn_back"] = knjCreateBtn($objForm, "btn_back", "戻 る", $extra);

        makeHidden($objForm, $arg, $model);

        /* データベース接続切断 */
        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjc010aSyousai.html", $arg);
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
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);
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
    knjCreateHidden($objForm, "SYOUSAI_SCHREGNO", $model->field["SYOUSAI_SCHREGNO"]);
    knjCreateHidden($objForm, "SYOUSAI_HIDDEN_SCHREG", $model->syousaiHiddenSchreg);
}
