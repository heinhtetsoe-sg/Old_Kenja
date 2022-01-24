<?php

require_once('for_php7.php');

class knjd425l_1Form1 {
    function main(&$model) {
        $objForm = new form;
        $arg["start"] = $objForm->get_start("edit", "POST", "knjd425l_1index.php", "", "edit");

        $db = Query::dbCheckOut();

        //画面タイトル
        $arg["TITLE"] = $db->getOne(knjd425l_1Query::getHreportGuidanceKindNameHdat($model));

        //生徒情報
        $arg["SCHREGNO"] = $model->schregno;
        $arg["NAME"]     = $model->name;

        //項目内容取得
        if ($model->cmd != "check") {
            $query = knjd425l_1Query::getHreportGuidanceSchregRemark($model, $model->selKindNo, "");
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $Row["REMARK_{$row["SEQ"]}"] = $row["REMARK"];
            }
            $result->free();

            $query = knjd425l_1Query::getHreportGuidanceSchregSelfreliance($model, "00", "", "");
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $Row["SELF_{$row["SELF_DIV"]}_{$row["SELF_SEQ"]}"] = "1";
            }
            $result->free();
        } else {
            $Row =& $model->field;
        }

        //テキストエリア生成
        foreach ($model->remarkTextLimit as $key => $textLimit) {
            $arg["TITLE_".$key] = $model->remarkTitle[$key];

            $moji = $textLimit["moji"];
            $gyou = $textLimit["gyou"];

            $extra = "id=\"REMARK_".$key."\"";
            $arg["REMARK_".$key] = knjCreateTextArea($objForm, "REMARK_".$key, $gyou, ($moji * 2), "", $extra, $Row["REMARK_".$key]);
            $arg["REMARK_".$key."_SIZE"] .= "<font size=2, color=\"red\">(全角".$moji."文字X".$gyou."行まで)</font>";
        }

        //チェックボックス生成
        foreach ($model->selfrelianceList as $key => $selfreliance) {
            $selfData = array();

            for ($i=0; $i < get_count($selfreliance); $i++) { 
                $seq = $selfreliance[$i];
                $checked = "";
                if ($Row["SELF_{$key}_{$seq}"] == "1") {
                    $checked = " checked ";
                }
                $name = "SELF_".$key."_".$seq;
                $extra = "id=\"".$name."\"";
                $arg[$name] = knjCreateCheckBox($objForm, $name, "1", $extra.$checked, "");

                $checkItem = array();
                $checkItem["CHECK_TITLE"] = $model->selfrelianceSubTitle[$key][$seq];
                $checkItem["CHECK"] = knjCreateCheckBox($objForm, $name, "1", $extra.$checked, "");
                $selfData["CHECK_LIST"][] = $checkItem;
            }
            $selfData["SELF_TITLE"] = $model->selfrelianceTitle[$key];
            $selfData["COLSPAN"] = get_count($selfData["CHECK_LIST"]);

            $arg["SELF_LIST"][] = $selfData;

        }

        //hidden
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "SCHREGNO", $model->schregno);
        knjCreateHidden($objForm, "RECORD_DATE", $model->recordDate);

        /**********/
        /* ボタン */
        /**********/
        //更新
        $extra = "id=\"update\" aria-label=\"更新\" onclick=\"current_cursor('update');return btn_submit('update');\"";
        $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);

        //戻る
        $link = REQUESTROOT."/D/KNJD425L/knjd425lindex.php?cmd=edit&SEND_PRGID={$model->sendPrgId}&SEND_AUTH={$model->auth}&SCHREGNO={$model->schregno}&EXP_YEAR={$model->exp_year}&EXP_SEMESTER={$model->exp_semester}&GRADE={$model->grade}&NAME={$model->name}";
        $extra = "onclick=\"window.open('$link','_self');\"";
        $arg["button"]["btn_end"] = KnjCreateBtn($objForm, "btn_back", "戻 る", $extra);

        Query::dbCheckIn($db);

        $arg["IFRAME"] = VIEW::setIframeJs();
        $arg["finish"]  = $objForm->get_finish();
        View::toHTML($model, "knjd425l_1Form1.html", $arg);
    }
}

?>
