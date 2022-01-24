<?php

require_once('for_php7.php');

class knjp714Form1
{
    function main(&$model)
    {
        $objForm        = new form;
        $arg["start"]   = $objForm->get_start("edit", "POST", "knjp714index.php", "", "edit");
        //DB接続
        $db = Query::dbCheckOut();

        //処理年度コンボ
        $query = knjp714Query::getYear($model);
        $extra = "onChange=\"return btn_submit('edit');\"";
        $model->year = $model->year ? $model->year : CTRL_YEAR;
        $arg["YEAR"] = makeCmbReturn($objForm, $arg, $db, $query, $model->year, "YEAR", $extra, 1, "");

        //学期
        $extra = "onChange=\"btn_submit('edit');\"";
        $query = knjp714Query::getSemester($model);
        $arg["SEMESTER"] = makeCmbReturn($objForm, $arg, $db, $query, $model->semester, "SEMESTER", $extra, 1, "BLANK");

        //校種コンボ
        $arg["schkind"] = "1";
        $query = knjp714Query::getSchkind($model);
        $extra = "onchange=\"return btn_submit('edit');\"";
        $arg["SCHOOL_KIND"] = makeCmbReturn($objForm, $arg, $db, $query, $model->schoolKind, "SCHOOL_KIND", $extra, 1, "");

        //radio
        if ($model->semester == "1") {
            $arg["tennyuShow"] = "";
        } else {
            $arg["tennyuShow"] = "1";
        }
        $opt = array(1, 2, 3, 4);
        $model->dataDiv = ($model->dataDiv == "") ? "1" : $model->dataDiv;
        $extra = array();
        foreach($opt as $key => $val) {
            array_push($extra, " id=\"DATADIV{$val}\" onClick=\"btn_submit('')\"");
        }
        $radioArray = knjCreateRadio($objForm, "DATADIV", $model->dataDiv, $extra, $opt, get_count($opt));
        foreach($radioArray as $key => $val) $arg[$key] = $val;

        //年組
        $extra = "onChange=\"btn_submit('edit');\"";
        $query = knjp714Query::getHrClass($model);
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


        //一覧を取得
        $model->setList = array();
        if (!isset($model->warning)) {
            $query = knjp714Query::getSchregList($model);
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $model->setList[] = $row;
            }
            $result->free();
        }

        $query = knjp714Query::getGroupList($model);
        $result = $db->query($query);
        $optGrpLabel = array();
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $optGrpLabel[$row["VALUE"]] = $row["LABEL"];
        }
        $result->free();

        //一覧を表示
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

            //入金グループ
            $setData["GROPCD_LABEL"] = $optGrpLabel[$Row["COLLECT_GRP_CD"]];
            //履修単位数
            $setData["CREDITS"] = $Row["CREDITS"];

            if ($Row["SYOKEIHI_CNT"] > 0) {
                $setData["SYOKEIHI_DISP"] = $Row["SYOKEIHI_DISP"];
            }
            if ($Row["JUGYOU_CNT"] > 0) {
                $setData["JUGYOURYOU_DISP"] = $Row["JUGYOURYOU_DISP"];
            }
            $setData["T_MONEY"] = number_format($Row["T_MONEY"]);

            $setData["FUKUGAKU"] = $Row["FUKUGAKU"] > 0 ? "レ" : "";
            $setData["SPORT"] = $Row["SPORT"] > 0 ? "レ" : "";
            $setData["ZENSEKI_SPORT"] = $Row["ZENSEKI_SPORT"] > 0 ? "レ" : "";

            $arg["data"][] = $setData;
        } //foreach

        //ボタン作成
        makeBtn($objForm, $arg, $model);

        //hidden作成
        makeHidden($objForm, $model);

        //DB切断
        Query::dbCheckIn($db);
        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();
        //インラインフレーム用Javascriptタグ生成
        $arg["IFRAME"] = View::setIframeJs();

        View::toHTML($model, "knjp714Form1.html", $arg); 
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

    if ($name == "SCHOOL_KIND" && SCHOOLKIND) {
        $value = ($value && $value_flg) ? $value : SCHOOLKIND;
    } else {
        $value = ($value && $value_flg) ? $value : $opt[0]["value"];
    }

    return knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}
//ボタン作成
function makeBtn(&$objForm, &$arg, $model) {
    $btnSize = "";
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
