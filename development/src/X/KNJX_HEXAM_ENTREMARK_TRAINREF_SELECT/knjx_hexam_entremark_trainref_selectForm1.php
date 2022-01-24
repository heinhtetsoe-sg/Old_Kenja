<?php
class knjx_hexam_entremark_trainref_selectForm1
{
    public function main(&$model)
    {
        $objForm = new form();
        $arg["start"] = $objForm->get_start("detail", "POST", "index.php", "", "detail");

        //DB OPEN
        $db = Query::dbCheckOut();

        //年度コンボ
        $query = knjx_hexam_entremark_trainref_selectQuery::getGrade($model);
        $extra = "onChange=\"return btn_submitYear('')\"";
        makeCmb($objForm, $arg, $db, $query, "GRADE_YEAR", $model->year, $extra, 1, "");

        //タイトル
        $arg["data"]["SCHREGNO"] = $model->schregno;
        $query = knjx_hexam_entremark_trainref_selectQuery::getName($model->schregno);
        $schName = $db->getOne($query);
        $arg["TITLE"] = $model->schregno.'　'.$schName;

        //6分割
        $subTitleArray = array();

        $seqbase = 100;
        if ($model->shojikouFlg == "1") {
            $seqbase = 0;
        }
        $subTitleArray[sprintf("%03d", $seqbase + 1)] = "(1)学習における特徴等";
        $subTitleArray[sprintf("%03d", $seqbase + 2)] = "(2)行動の特徴，特技等";
        $subTitleArray[sprintf("%03d", $seqbase + 3)] = "(3)部活動，ボランティア活動，<br>&nbsp;&nbsp;&nbsp;留学・海外経験等";
        $subTitleArray[sprintf("%03d", $seqbase + 4)] = "(4)取得資格，検定等";
        $subTitleArray[sprintf("%03d", $seqbase + 5)] = "(5)表彰・顕彰等の記録";
        $subTitleArray[sprintf("%03d", $seqbase + 6)] = "(6)その他";

        $dispRow = array();

        $query = knjx_hexam_entremark_trainref_selectQuery::getHexamTrainRef($model);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $dispRow[$row["TRAIN_SEQ"]] = $row;
        }

        $rowCnt = 0;
        for ($i = 1; $i <= 6; $i++) {
            $trainSeq = sprintf("%03d", $seqbase + $i);

            $extra = " id=\"CHECK_TRAIN_REF{$i}\" ";
            $dispRow[$trainSeq]["CHECK"] = knjCreateCheckBox($objForm, "CHECK", $dispRow[$trainSeq]["REMARK"], $extra.$disabled, "1");
            $dispRow[$trainSeq]["SUBTITLE"] = $subTitleArray[$trainSeq];

            $dispGyo = 4;
            $height = ($dispGyo) * 13.5 + ($dispGyo - 1 ) * 3 + 5;
            $extra = "style=\"height:{$height}px;\" readonly ".$disabled;
            $dispRow[$trainSeq]["TRAINREF_REMARK"] = KnjCreateTextArea($objForm, "TRAINREF_REMARK".$trainSeq, ($model->gyou + 1), ($model->moji * 2 + 1), 'soft', $extra, $dispRow[$trainSeq]["REMARK"]);

            $arg["list"][] = $dispRow[$trainSeq];
            $rowCnt++;
        }

        //終了ボタンを作成する
        $extra = "onclick=\"return parent.closeit()\"";
        $arg["button"]["btn_back"] = knjCreateBtn($objForm, "btn_back", "戻 る", $extra);
        $extra = "onclick=\"return btn_submit('".$rowCnt."')\"";
        $arg["button"]["btn_sentaku"] = knjCreateBtn($objForm, "btn_sentaku", "選 択", $extra);

        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
        knjCreateHidden($objForm, "PRGID", "KNJX_HEXAM_ENTREMARK_TRAINREF_SELECT");
        knjCreateHidden($objForm, "SCHREGNO", $model->schregno);
        knjCreateHidden($objForm, "TRAINREF_TARGET", $model->target);
        knjCreateHidden($objForm, "TOTALREMARK_KETA", $model->keta);
        knjCreateHidden($objForm, "TOTALREMARK_GYO", $model->gyo);
        knjCreateHidden($objForm, "TORIKOMI_MULTI", $model->torikomiMulti);
        knjCreateHidden($objForm, "SHOJIKOU_FLG", $model->shojikouFlg);


        $arg["finish"] = $objForm->get_finish();

        //テンプレートのHTMLを$arg経由で渡す
        View::toHTML($model, "knjx_hexam_entremark_trainref_selectForm1.html", $arg);
    }
}
/********************************************** 以下関数 ******************************************************/
//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size)
{
    $opt = array();
    $value_flg = false;
    $value_flg_year = false;
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);

        if ($value == $row["VALUE"]) {
            $value_flg = true;
        }
        if (CTRL_YEAR == $row["VALUE"]) {
            $value_flg_year = true;
        }
    }
    $result->free();

    if ($name == "SEMESTER") {
        $value = ($value && $value_flg) ? $value : CTRL_SEMESTER;
    } elseif ($name == "YEAR") {
        if (!($value && $value_flg)) {
            $value = (CTRL_YEAR && $value_flg_year) ? CTRL_YEAR : $opt[0]["value"];
        }
    } else {
        $value = ($value && $value_flg) ? $value : $opt[0]["value"];
    }

    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}
