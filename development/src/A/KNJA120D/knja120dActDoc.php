<?php

require_once('for_php7.php');

class knja120dActDoc {

    function main(&$model) {

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("list", "POST", "knja120dindex.php", "", "edit");

        //DB接続
        $db = Query::dbCheckOut();

        //生徒情報
        $arg["SCHREGNO"] = $model->schregno;
        $arg["NAME"]     = $model->name;

        //日付欄の幅
        $arg["ACTIONDATE_WIDTH"] = ($model->Properties["useWarekiHyoji"] == "1") ? 130 : 90;

        //行動の記録データ取得
        if ($model->schregno) {
            $result = $db->query(knja120dQuery::getActionDocumentDat($model));
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {

                $row = str_replace("\r\n", "", $row);
                $row = str_replace("\r"  , "", $row);
                $row = str_replace("\n"  , "", $row);

                $row["ACTIONDATE"] = str_replace("-", "/", $row["ACTIONDATE"]);
                //和暦表示
                if ($model->Properties["useWarekiHyoji"] == "1") {
                    $row["ACTIONDATE"] = common::DateConv1($row["ACTIONDATE"], 0);
                }

                if ($row["ACTIONTIME"]) {
                    $time = preg_split("/:/", $row["ACTIONTIME"]);
                    $row["ACTIONTIME"] = $time[0].":".$time[1];
                }

                $arg["data"][] = $row;
            }
        }

        //終了ボタン
        $extra = "onclick=\"return parent.closeit()\"";
        $arg["btn_back"] = knjCreateBtn($objForm, "btn_back", "戻 る", $extra);

        //hidden
        knjCreateHidden($objForm, "cmd");

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"] = $objForm->get_finish();

        if (VARS::get("cmd") == "right_list") {
            $arg["reload"] = "parent.edit_frame.location.href='knja120dindex.php?cmd=edit'";
        }

        View::toHTML($model, "knja120dActDoc.html", $arg);
    }
}
?>
