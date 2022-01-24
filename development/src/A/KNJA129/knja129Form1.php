<?php

require_once('for_php7.php');
class knja129Form1
{
    public function main(&$model)
    {
        $objForm = new form();
        $arg["start"] = $objForm->get_start("edit", "POST", "knja129index.php", "", "edit");
        //警告メッセージを表示しない場合
        if ((isset($model->schregno) && !isset($model->warning)) || !isset($model->schregno)) {
            if ($model->cmd !== 'reload') {
                $row = knja129Query::getTrainRow($model->schregno, $model->exp_year, $model);
            } else {
                $row =& $model->field;
            }
            $arg["NOT_WARNING"] = 1;
        } else {
            $row =& $model->field;
        }

        $arg["data"]["SCHOOL_KIND_STUDENT_KIND"] = $model->jidoOrSeito;

        $model->disabled = $model->schregno ? "" : " disabled ";

        $arg["SCHREGNO"] = $model->schregno;
        $arg["NAME"]     = $model->name;

        foreach ($model->inputs as $fieldname => $input) {
            if ($input["TYPE"] == "TEXTAREA") {
                $height = $input["GYOU"] * 13.5 + ($input["GYOU"] - 1) * 3 + 5;
                $extra = " style=\"height: {$height}px; \" ";
                $arg["data"][$fieldname] = KnjCreateTextArea($objForm, $fieldname, $input["GYOU"], $input["MOJI"] * 2, "soft", $extra, $row[$fieldname]);
                $arg["data"][$fieldname."_TYUI"] = "(全角{$input["MOJI"]}文字X{$input["GYOU"]}行まで)";
            } elseif ($input["TYPE"] == "TEXTINT") {
                $extra = " onblur=\"this.value = toIntegerKnja129(this.value)\" style=\"text-align: right;\" ";
                $arg["data"][$fieldname] = KnjCreateTextBox($objForm, $row[$fieldname], $fieldname, $input["MOJI"], $input["MOJI"], $extra);
            }
        }

        $arg["IFRAME"] = VIEW::setIframeJs();

        makeButton($objForm, $arg, $model);

        makeHidden($objForm, $model);

        $arg["finish"]  = $objForm->get_finish();

        View::toHTML($model, "knja129Form1.html", $arg);
    }
}

//選択ボタン
function makeSelectBtn(&$objForm, $model, $div, $name, $label, $target, $disabled = "")
{
    if (!$div || !$name || !$label || !$target) {
        return;
    }
    $sizeW = 600;
    $sizeH = 350;
    if ($div == "club") {                   //部活動
        $w = "/X/KNJX_CLUB_SELECT/knjx_club_selectindex.php";
        $sizeW = 800;
    } elseif ($div == "committee") {       //委員会
        $w = "/X/KNJX_COMMITTEE_SELECT/knjx_committee_selectindex.php";
        $sizeW = 700;
    } elseif ($div == "qualified") {       //検定
        $w = "/X/KNJX_QUALIFIED_SELECT/knjx_qualified_selectindex.php";
        $sizeW = ($model->Properties["useQualifiedMst"] == "1") ? 800 : 670;
        $sizeH = 500;
    } elseif ($div == "hyosyo") {          //賞
        $w = "/X/KNJX_HYOSYO_SELECT/knjx_hyosyo_selectindex.php";
    } elseif ($div == "batsu") {          //罰
        $w = "/X/KNJX_BATSU_SELECT/knjx_batsu_selectindex.php";
    } elseif ($div == "kirokubikou") {     //記録備考
        $w = "/X/KNJX_CLUB_KIROKUBIKOU_SELECT/knjx_club_kirokubikou_selectindex.php";
        $sizeW = 800;
    } elseif ($div == "reason_collection") {   //年間出欠備考
        $w = "/X/KNJX_REASON_COLLECTION_SELECT/knjx_reason_collection_selectindex.php";
        $sizeW = 800;
    } elseif ($div == "marathon") {   //マラソン大会選択
        $w = "/X/KNJX_MARATHON_SELECT/knjx_marathon_selectindex.php";
        $sizeW = 800;
    }
    $arg = "program_id=".PROGRAMID."&SEND_PRGID=".PROGRAMID."&EXP_YEAR={$model->exp_year}&EXP_SEMESTER={$model->exp_semester}&SCHREGNO={$model->schregno}&NAME={$model->name}&TARGET={$target}";
    $extra = $disabled." onclick=\"loadwindow('".REQUESTROOT."".$w."?".$arg.",0,document.documentElement.scrollTop || document.body.scrollTop,{$sizeW},{$sizeH}); \"";
    return knjCreateBtn($objForm, $name, $label, $extra);
}

//定型ボタン作成
function createTeikeiBtn(&$arg, &$objForm, $model, $property, $title, $textbox)
{
    $sendDataDivArr = explode("-", $property);
    if (get_count($sendDataDivArr) != 2) {
        return;
    }

    for ($i = 0; $i < 2; $i++) {
        $sendDataDiv = $sendDataDivArr[$i];
        $bangou = $i + 1;

        $extra  = " onclick=\"loadwindow('../../X/KNJX_TEIKEIBUN/knjx_teikeibunindex.php?";
        $extra .= "cmd=teikei&EXP_YEAR={$model->exp_year}&GRADE={$model->grade}&DATA_DIV={$sendDataDiv}&TITLE={$title}{$bangou}&TEXTBOX={$textbox}'";
        $extra .= ", event.clientX + function () {var scrollX = document.documentElement.scrollLeft || document.body.scrollLeft;return scrollX;}(), event.clientY + function () {var scrollY = document.documentElement.scrollTop || document.body.scrollTop;return scrollY;}(), 550, 350);\" ";
        $arg["button"]["btn_teikei".$bangou."_".$textbox] = knjCreateBtn($objForm, "btn_teikei".$bangou, "定型文選択".$bangou, $extra);
    }
}

function makeButton(&$objForm, &$arg, &$model)
{

    $extra = "onclick=\"return btn_submit('update');\"";
    $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更新", $model->disabled.$extra);

    //CSV処理
    $mojiSizeArray = array();
    $gyouSizeArray = array();
    foreach ($model->inputs as $fieldname => $input) {
        if ($input["TYPE"] == "TEXTAREA") {
            $mojiSizeArray[] = $fieldname."=".($input["MOJI"] * 3 * $input["GYOU"]);
            $gyouSizeArray[]  = $fieldname."=".$input["GYOU"];
        } elseif ($input["TYPE"] == "TEXTINT") {
            $mojiSizeArray[] = $fieldname."=".($input["MOJI"]);
        }
    }

    //セキュリティーチェック
    $db = Query::dbCheckOut();
    $securityCnt = $db->getOne(knja129Query::getSecurityHigh());
    Query::dbCheckIn($db);
    $csvSetName = "ＣＳＶ";
    if ($model->Properties["useXLS"]) {
        $csvSetName = "エクセル";
    }
    $fieldSize = implode(",", $mojiSizeArray);
    $gyouSize = implode(",", $gyouSizeArray);
    if ($model->getPrgId || !$model->Properties["useXLS"] || $securityCnt == 0) {
        $extra = " onClick=\" wopen('".REQUESTROOT."/X/KNJX_A129/knjx_a129index.php?FIELDSIZE=".$fieldSize."&GYOUSIZE=".$gyouSize."&SEND_PRGID=KNJA129&SEND_AUTH={$model->auth}','SUBWIN2',0,0,screen.availWidth,screen.availHeight);\"";
        $arg["button"]["btn_csv"] = knjCreateBtn($objForm, "btn_csv", $csvSetName."出力", $model->disabled.$extra);
    }

    //プレビュー／印刷
    if ($model->Properties["sidouyourokuShokenPreview"] == '1') {
        $extra =  "onclick=\"return newwin('".SERVLET_URL."');\"";
        $arg["button"]["btn_print"] = KnjCreateBtn($objForm, "btn_print", "プレビュー／印刷", $model->disabled.$extra);
    }

    //更新後前の生徒へボタン
    $setUpNext = View::updateNext($model, $objForm, 'btn_update');
    $arg["button"]["btn_up_next"] = str_replace("onclick", $model->disabled."onclick", $setUpNext);

    $extra = "onclick=\"return btn_submit('clear');\"";
    $arg["button"]["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $model->disabled.$extra, "reset");

    $extra = "onclick=\"closeWin();\"";
    $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);

    if (get_count($model->warning)== 0 && $model->cmd !="clear") {
        $arg["nextjs"] = "updateNextStudent('{$model->schregno}', 0);";
    } elseif ($model->cmd =="clear") {
        $arg["nextjs"] = "updateNextStudent('{$model->schregno}', 1);";
    }
    //画面のリロード
    if ($model->cmd == "updEdit") {
        $arg["reloadjs"] = "parent.left_frame.btn_submit('list');";
    }
}

function makeHidden(&$objForm, &$model)
{
    knjCreateHidden($objForm, "cmd");
    knjCreateHidden($objForm, "SCHREGNO", $model->schregno);
    knjCreateHidden($objForm, "PRGID", "KNJA129");
    knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
    knjCreateHidden($objForm, "DOCUMENTROOT", DOCUMENTROOT);
    knjCreateHidden($objForm, "CTRL_YEAR", CTRL_YEAR);
    knjCreateHidden($objForm, "CTRL_SEMESTER", CTRL_SEMESTER);
}
