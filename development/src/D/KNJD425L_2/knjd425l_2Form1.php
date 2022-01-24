<?php

require_once('for_php7.php');

//ビュー作成用クラス
class knjd425l_2Form1
{
    function main(&$model) {
        $objForm = new form;
        $arg["start"] = $objForm->get_start("edit", "POST", "knjd425l_2index.php", "", "edit");

        $db = Query::dbCheckOut();

        //画面タイトル
        $query = knjd425l_2Query::getHreportGuidanceKindNameHdat($model);
        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        $arg["TITLE"] = $row["KIND_NAME"];

        //生徒情報
        $arg["SCHREGNO"] = $model->schregno;
        $arg["NAME"]     = $model->name;

        //項目内容取得
        if ($model->cmd != "check") {
            $query = knjd425l_2Query::getHreportGuidanceSchregRemark($model, $model->selKindNo, "");
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $Row["REMARK_{$row["SEQ"]}"] = $row["REMARK"];
            }
            $result->free();
        } else {
            $Row =& $model->field;
        }

        //テキストエリア生成
        $remarkList = array();
        foreach ($model->remarkTitle as $seq => $remarkTitle) {
            $remarkData = array();

            $remarkData["REMARK_TITLE"] = $remarkTitle["REMARK_TITLE"];

            $moji = $model->remarkTextLimit[$seq]["moji"];
            $gyou = $model->remarkTextLimit[$seq]["gyou"];

            $extra = "id=\"REMARK_".$seq."\" aria-label=\"".$remarkTitle["REMARK_TITLE"]."\" ";
            $remarkData["REMARK"] = knjCreateTextArea($objForm, "REMARK_".$seq, $gyou, ($moji * 2), "", $extra, $Row["REMARK_".$seq]);
            $remarkData["REMARK_SIZE"] .= "<font size=2, color=\"red\">(全角".$moji."文字X".$gyou."行まで)</font>";
            $remarkList[] = $remarkData;
        }
        $arg["REMARK_LIST"] = $remarkList;

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
        View::toHTML($model, "knjd425l_2Form1.html", $arg);
    }
}
?>
