<?php

require_once('for_php7.php');

class knjmp714Form1
{
    function main(&$model)
    {
        $objForm        = new form;
        $arg["start"]   = $objForm->get_start("edit", "POST", "knjmp714index.php", "", "edit");
        //DB接続
        $db = Query::dbCheckOut();

        //処理年度コンボ
        $query = knjmp714Query::getYear($model);
        $extra = "onChange=\"return btn_submit('edit');\"";
        $model->year = $model->year ? $model->year : CTRL_YEAR;
        $arg["YEAR"] = makeCmbReturn($objForm, $arg, $db, $query, $model->year, "YEAR", $extra, 1, "");

        //学期
        $extra = "onChange=\"btn_submit('edit');\"";
        $query = knjmp714Query::getSemester($model);
        $arg["SEMESTER"] = makeCmbReturn($objForm, $arg, $db, $query, $model->semester, "SEMESTER", $extra, 1, "BLANK");

        //radio
        if ($model->semester == "1") {
            $arg["tennyuShow"] = "";
        } else {
            $arg["tennyuShow"] = "1";
        }
        $opt = array(1, 2, 3);
        $model->dataDiv = ($model->dataDiv == "") ? "1" : $model->dataDiv;
        $extra = array();
        foreach($opt as $key => $val) {
            array_push($extra, " id=\"DATADIV{$val}\" onClick=\"btn_submit('')\"");
        }
        $radioArray = knjCreateRadio($objForm, "DATADIV", $model->dataDiv, $extra, $opt, get_count($opt));
        foreach($radioArray as $key => $val) $arg[$key] = $val;

        //年組
        $extra = "onChange=\"btn_submit('edit');\"";
        $query = knjmp714Query::getHrClass($model);
        $arg["HR_CLASS"] = makeCmbReturn($objForm, $arg, $db, $query, $model->hr_class, "HR_CLASS", $extra, 1, "BLANK");

        //請求表示
        $opt = array(1, 2);
        $model->seikyuuDisp = ($model->seikyuuDisp == "") ? "1" : $model->seikyuuDisp;
        $extra = array();
        foreach ($opt as $key => $val) {
            array_push($extra, " id=\"SEIKYUU_DISP{$val}\" onClick=\"btn_submit('')\"");
        }
        $radioArray = knjCreateRadio($objForm, "SEIKYUU_DISP", $model->seikyuuDisp, $extra, $opt, get_count($opt));
        foreach ($radioArray as $key => $val) $arg[$key] = $val;


        //更新
        $extra = " id=\"UPDATE_CHK_ALL\" onClick=\"return check_all(this);\" ";
        $arg["UPDATE_CHK_ALL"] = knjCreateCheckBox($objForm, "UPDATE_CHK_ALL", "1", $extra);

        //一覧を取得
        $model->setList = array();
        if (!isset($model->warning)) {
            $query = knjmp714Query::getSchregList($model);
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $model->setList[] = $row;
            }
            $result->free();
        }

        $query = knjmp714Query::getGroupList($model);
        $result = $db->query($query);
        $optGrp = array();
        $optGrp[] = array ("label" => "",
                           "value" => "");
        $moneyArray = array();
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $optGrp[] = array ("label" => $row["LABEL"],
                               "value" => $row["VALUE"]);
            $moneyArray[$row["VALUE"]] = $row["TMONEY"];
            //hidden
            knjCreateHidden($objForm, "GROUP_".$row["VALUE"], $row["TMONEY"]);
        }
        $result->free();

        $query = knjmp714Query::getJugyouryou($model);
        $result = $db->query($query);
        $optJugyou = array();
        $optJugyou[] = array ("label" => "",
                           "value" => "");
        $model->jugyouRyouArray = array();
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $optJugyou[] = array ("label" => $row["LABEL"]."({$row["COLLECT_M_MONEY"]})",
                                  "value" => $row["VALUE"]);
            //hidden
            knjCreateHidden($objForm, "JUGYOU_".$row["VALUE"], $row["COLLECT_M_MONEY"]);
            $model->jugyouRyouArray[$row["VALUE"]] = $row["COLLECT_M_MONEY"];
        }
        $result->free();

        //一覧を表示
        $paidFlg = false;
        $dataAriColor = " style=\"background-color:pink\"";
        foreach ($model->setList as $counter => $Row) {
            $setData = array();
            $setData["ATTENDNO"] = $Row["HR_NAME"]."<BR>".$Row["ATTENDNO"]."番";
            $setData["NAME_SHOW"] = $Row["SCHREGNO"]."　".$Row["NAME"];
            $setData["SCHREGNO"] = $Row["SCHREGNO"];
            $setData["ENT_NAME"] = $Row["ENT_NAME"];
            $setData["INOUT_NAME"] = $Row["INOUT_NAME"];

            $setKey = $Row["SCHREGNO"].":".$Row["SLIP_NO"];
            $setData["SET_KEY"] = $setKey;
            $setData["SLIP_NO"] = $Row["SLIP_NO"];
            $claimDisp = $Row["CLAIM_CNT"] > 0 ? " disabled " : "";
            $setData["BG_COLOR"] = $Row["SLIP_NO"] ? "#FFFFFF" : "#CCFFFF";

            //更新
            $extra = " ";
            $setData["UPDATE_CHK"] = knjCreateCheckBox($objForm, "UPDATE_CHK_{$setKey}", "1", $claimDisp.$extra);

            $extra = "onChange=\"changeJugyouRyou(this, '{$setKey}')\"";
            if ($Row["SYOKEIHI_CNT"] > 0) {
                $extra = $dataAriColor.$extra;
                $setData["SYOKEIHI_DISP"] = $Row["SYOKEIHI_DISP"];
            }
            $setData["GROPCD"] = knjCreateCombo($objForm, "GROPCD_{$setKey}", $Row["COLLECT_GRP_CD"], $optGrp, $extra, "1");

            $extra = "onChange=\"changeJugyouRyou(this, '{$setKey}')\"";
            if ($Row["JUGYOU_CNT"] > 0) {
                $extra = $dataAriColor.$extra;
                $setData["JUGYOURYOU_DISP"] = $Row["JUGYOURYOU_DISP"];
            }
            $setData["JUGYOU"] = knjCreateCombo($objForm, "JUGYOU_{$setKey}", $Row["JUGYOU"], $optJugyou, $extra, "1");

            $setData["T_MONEY"] = number_format($Row["T_MONEY"]);
            if ($model->Properties["collectSlipM_def_cnt"] == "CREDIT" && $Row["SLIP_NO"] == "") {
                $setData["COLLECT_CNT"] = $Row["CREDITS"];
            } else {
                $setData["COLLECT_CNT"] = $Row["COLLECT_CNT"];
            }
            //数量
            $extra = "style=\"text-align:right\" onblur=\"this.value=toInteger(this.value); changeJugyouRyou(this, '{$setKey}')\" ";
            $setData["COLLECT_CNT"] = knjCreateTextBox($objForm, $setData["COLLECT_CNT"], "COLLECT_CNT_{$setKey}", 2, 2, $extra);

            $setData["FUKUGAKU"] = $Row["FUKUGAKU"] > 0 ? "レ" : "";
            $setData["SPORT"] = $Row["SPORT"] > 0 ? "レ" : "";
            $setData["ZENSEKI_SPORT"] = $Row["ZENSEKI_SPORT"] > 0 ? "レ" : "";

            $arg["data"][] = $setData;
        } //foreach

        //ボタン作成
        makeBtn($objForm, $arg, $model, $paidFlg);

        //hidden作成
        makeHidden($objForm, $model);

        if ($paidFlg) {
            $arg["jscript"] = "alert('入金済みの生徒が存在する為。更新できません。');";
        }

        //DB切断
        Query::dbCheckIn($db);
        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();
        //インラインフレーム用Javascriptタグ生成
        $arg["IFRAME"] = View::setIframeJs();

        View::toHTML($model, "knjmp714Form1.html", $arg); 
    }
}
//コンボ作成
function makeCmbReturn(&$objForm, &$arg, $db, $query, &$value, $name, $extra, $size, $blank = "") {
    $opt = array();
    $value_flg = false;
    if ($blank == "BLANK") {
        $opt[] = array ("label" => "",
                        "value" => "");
    }

    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
        $opt[] = array ("label" => $row["LABEL"],
                        "value" => $row["VALUE"]);
        if ($value == $row["VALUE"]) $value_flg = true;
    }
    $result->free();

    $value = ($value && $value_flg) ? $value : $opt[0]["value"];

    return knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}
//ボタン作成
function makeBtn(&$objForm, &$arg, $model, $paidFlg) {
    $btnSize = "";
    //更新
    $extra = "onclick=\"return btn_submit('update');\"";
    $extra .= $paidFlg ? " disabled " : "";
    $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra.$btnSize);
    //取消
    $extra = "onclick=\"return btn_submit('reset');\"";
    $arg["button"]["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra.$btnSize);
    //終了
    $extra = "onclick=\"closeWin();\"";
    $arg["button"]["btn_back"] = knjCreateBtn($objForm, "btn_back", "終 了", $extra.$btnSize);
}
//hidden作成
function makeHidden(&$objForm, $model) {
    knjCreateHidden($objForm, "cmd");
    knjCreateHidden($objForm, "printKenkouSindanIppan", $model->Properties["printKenkouSindanIppan"]);
    knjCreateHidden($objForm, "H_HR_CLASS");
}
?>
